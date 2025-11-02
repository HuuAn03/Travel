package fpt.edu.vn.assigment_travelapp.data.repository;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.Post;
import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.User;

public class PostRepository implements IPostRepository {

    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;
    private final StorageReference mStorageRef;

    public PostRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void createPost(String imageUrl, String caption, String location, OnPostCreationCompleteListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        String userId = currentUser.getUid();
        String postId = mDatabase.child("posts").push().getKey();
        long timestamp = System.currentTimeMillis();

        Post post = new Post(postId, imageUrl, caption, userId, timestamp, location);

        if (postId != null) {
            mDatabase.child("posts").child(postId).setValue(post)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        }
    }

    @Override
    public void createPostWithUri(Uri imageUri, String caption, String location, OnPostCreationCompleteListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        String userId = currentUser.getUid();
        String postId = mDatabase.child("posts").push().getKey();
        if (postId == null) {
            listener.onFailure("Could not generate post ID");
            return;
        }

        // Upload image to Firebase Storage
        StorageReference imageRef = mStorageRef.child("post_images").child(postId + ".jpg");
        imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        createPost(downloadUri.toString(), caption, location, listener);
                    } else {
                        listener.onFailure(task.getException().getMessage());
                    }
                });
    }

    @Override
    public void getAllPosts(OnGetAllPostsCompleteListener listener) {
        mDatabase.child("posts").orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot postsSnapshot) {
                if (!postsSnapshot.exists()) {
                    listener.onSuccess(new ArrayList<>());
                    return;
                }

                List<PostWithUser> postsWithUser = new ArrayList<>();
                AtomicInteger postCount = new AtomicInteger((int) postsSnapshot.getChildrenCount());

                for (DataSnapshot postDataSnapshot : postsSnapshot.getChildren()) {
                    Post post = postDataSnapshot.getValue(Post.class);
                    if (post != null && post.getUserId() != null) {
                        mDatabase.child("users").child(post.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    postsWithUser.add(new PostWithUser(post, user));
                                }

                                if (postCount.decrementAndGet() == 0) {
                                    // Sort by timestamp descending
                                    Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                    listener.onSuccess(postsWithUser);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                if (postCount.decrementAndGet() == 0) {
                                    Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                    listener.onSuccess(postsWithUser);
                                }
                            }
                        });
                    } else {
                        if (postCount.decrementAndGet() == 0) {
                            Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                            listener.onSuccess(postsWithUser);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getPostsByUser(String userId, OnGetAllPostsCompleteListener listener) {
        Query query = mDatabase.child("posts").orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot postsSnapshot) {
                List<PostWithUser> postsWithUser = new ArrayList<>();
                if (!postsSnapshot.exists()) {
                    listener.onSuccess(postsWithUser);
                    return;
                }

                AtomicInteger postCount = new AtomicInteger((int) postsSnapshot.getChildrenCount());

                for (DataSnapshot postDataSnapshot : postsSnapshot.getChildren()) {
                    Post post = postDataSnapshot.getValue(Post.class);
                    if (post != null && post.getUserId() != null) {
                        mDatabase.child("users").child(post.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    postsWithUser.add(new PostWithUser(post, user));
                                }

                                if (postCount.decrementAndGet() == 0) {
                                    Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                    listener.onSuccess(postsWithUser);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                if (postCount.decrementAndGet() == 0) {
                                    Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                    listener.onSuccess(postsWithUser);
                                }
                            }
                        });
                    } else {
                        if (postCount.decrementAndGet() == 0) {
                            Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                            listener.onSuccess(postsWithUser);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getBookmarkedPosts(String userId, OnGetAllPostsCompleteListener listener) {
        mDatabase.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot postsSnapshot) {
                List<PostWithUser> postsWithUser = new ArrayList<>();
                if (!postsSnapshot.exists()) {
                    listener.onSuccess(postsWithUser);
                    return;
                }

                AtomicInteger postCount = new AtomicInteger(0);
                AtomicInteger processedCount = new AtomicInteger(0);

                for (DataSnapshot postDataSnapshot : postsSnapshot.getChildren()) {
                    Post post = postDataSnapshot.getValue(Post.class);
                    if (post != null && post.getBookmarks() != null && post.getBookmarks().containsKey(userId)) {
                        postCount.incrementAndGet();
                        if (post.getUserId() != null) {
                            mDatabase.child("users").child(post.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    User user = userSnapshot.getValue(User.class);
                                    if (user != null) {
                                        postsWithUser.add(new PostWithUser(post, user));
                                    }

                                    if (processedCount.incrementAndGet() == postCount.get()) {
                                        Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                        listener.onSuccess(postsWithUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    if (processedCount.incrementAndGet() == postCount.get()) {
                                        Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                        listener.onSuccess(postsWithUser);
                                    }
                                }
                            });
                        } else {
                            if (processedCount.incrementAndGet() == postCount.get()) {
                                Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                listener.onSuccess(postsWithUser);
                            }
                        }
                    }
                }

                if (postCount.get() == 0) {
                    listener.onSuccess(postsWithUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getPostsByFollowedUsers(String userId, OnGetAllPostsCompleteListener listener) {
        // First get the list of users that the current user follows
        mDatabase.child("users").child(userId).child("following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot followingSnapshot) {
                if (!followingSnapshot.exists()) {
                    listener.onSuccess(new ArrayList<>());
                    return;
                }

                List<String> followingUserIds = new ArrayList<>();
                for (DataSnapshot userIdSnapshot : followingSnapshot.getChildren()) {
                    followingUserIds.add(userIdSnapshot.getKey());
                }

                // Now get all posts and filter by followed users
                mDatabase.child("posts").orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot postsSnapshot) {
                        List<PostWithUser> postsWithUser = new ArrayList<>();
                        if (!postsSnapshot.exists()) {
                            listener.onSuccess(postsWithUser);
                            return;
                        }

                        AtomicInteger postCount = new AtomicInteger(0);
                        AtomicInteger processedCount = new AtomicInteger(0);

                        for (DataSnapshot postDataSnapshot : postsSnapshot.getChildren()) {
                            Post post = postDataSnapshot.getValue(Post.class);
                            if (post != null && followingUserIds.contains(post.getUserId())) {
                                postCount.incrementAndGet();
                                if (post.getUserId() != null) {
                                    mDatabase.child("users").child(post.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                            User user = userSnapshot.getValue(User.class);
                                            if (user != null) {
                                                postsWithUser.add(new PostWithUser(post, user));
                                            }

                                            if (processedCount.incrementAndGet() == postCount.get()) {
                                                Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                                listener.onSuccess(postsWithUser);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            if (processedCount.incrementAndGet() == postCount.get()) {
                                                Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                                listener.onSuccess(postsWithUser);
                                            }
                                        }
                                    });
                                } else {
                                    if (processedCount.incrementAndGet() == postCount.get()) {
                                        Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                        listener.onSuccess(postsWithUser);
                                    }
                                }
                            }
                        }

                        if (postCount.get() == 0) {
                            listener.onSuccess(postsWithUser);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void searchPosts(String query, OnGetAllPostsCompleteListener listener) {
        mDatabase.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot postsSnapshot) {
                List<PostWithUser> postsWithUser = new ArrayList<>();
                if (!postsSnapshot.exists()) {
                    listener.onSuccess(postsWithUser);
                    return;
                }

                String lowerQuery = query.toLowerCase();
                AtomicInteger postCount = new AtomicInteger(0);
                AtomicInteger processedCount = new AtomicInteger(0);

                for (DataSnapshot postDataSnapshot : postsSnapshot.getChildren()) {
                    Post post = postDataSnapshot.getValue(Post.class);
                    if (post != null) {
                        boolean matches = false;
                        if (post.getCaption() != null && post.getCaption().toLowerCase().contains(lowerQuery)) {
                            matches = true;
                        }
                        if (post.getLocation() != null && post.getLocation().toLowerCase().contains(lowerQuery)) {
                            matches = true;
                        }

                        if (matches) {
                            postCount.incrementAndGet();
                            if (post.getUserId() != null) {
                                mDatabase.child("users").child(post.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                        User user = userSnapshot.getValue(User.class);
                                        if (user != null) {
                                            postsWithUser.add(new PostWithUser(post, user));
                                        }

                                        if (processedCount.incrementAndGet() == postCount.get()) {
                                            Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                            listener.onSuccess(postsWithUser);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        if (processedCount.incrementAndGet() == postCount.get()) {
                                            Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                            listener.onSuccess(postsWithUser);
                                        }
                                    }
                                });
                            } else {
                                if (processedCount.incrementAndGet() == postCount.get()) {
                                    Collections.sort(postsWithUser, (p1, p2) -> Long.compare(p2.getPost().getTimestamp(), p1.getPost().getTimestamp()));
                                    listener.onSuccess(postsWithUser);
                                }
                            }
                        }
                    }
                }

                if (postCount.get() == 0) {
                    listener.onSuccess(postsWithUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void updatePost(String postId, String caption, String location, OnPostUpdateCompleteListener listener) {
        DatabaseReference postRef = mDatabase.child("posts").child(postId);
        postRef.child("caption").setValue(caption);
        postRef.child("location").setValue(location)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void updatePostImage(String postId, Uri imageUri, OnPostUpdateCompleteListener listener) {
        StorageReference imageRef = mStorageRef.child("post_images").child(postId + ".jpg");
        imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        mDatabase.child("posts").child(postId).child("imageUrl").setValue(downloadUri.toString())
                                .addOnSuccessListener(aVoid -> listener.onSuccess())
                                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                    } else {
                        listener.onFailure(task.getException().getMessage());
                    }
                });
    }

    @Override
    public void deletePost(String postId, OnPostDeleteCompleteListener listener) {
        // Delete post image from storage
        StorageReference imageRef = mStorageRef.child("post_images").child(postId + ".jpg");
        imageRef.delete().addOnCompleteListener(task -> {
            // Delete post from database
            mDatabase.child("posts").child(postId).removeValue()
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        });
    }

    @Override
    public void addComment(String postId, Comment comment, OnCommentAddCompleteListener listener) {
        String commentId = mDatabase.child("posts").child(postId).child("comments").push().getKey();
        if (commentId == null) {
            listener.onFailure("Couldn't generate a comment ID");
            return;
        }

        comment.setCommentId(commentId);
        mDatabase.child("posts").child(postId).child("comments").child(commentId).setValue(comment)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void deleteComment(String postId, String commentId, OnCommentDeleteCompleteListener listener) {
        mDatabase.child("posts").child(postId).child("comments").child(commentId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void getCommentsWithUsers(String postId, OnGetCommentsWithUsersCompleteListener listener) {
        mDatabase.child("posts").child(postId).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot commentsSnapshot) {
                if (!commentsSnapshot.exists()) {
                    listener.onSuccess(new ArrayList<>());
                    return;
                }

                List<CommentWithUser> commentsWithUser = new ArrayList<>();
                AtomicInteger commentCount = new AtomicInteger((int) commentsSnapshot.getChildrenCount());

                for (DataSnapshot commentDataSnapshot : commentsSnapshot.getChildren()) {
                    Comment comment = commentDataSnapshot.getValue(Comment.class);
                    if (comment != null && comment.getUserId() != null) {
                        mDatabase.child("users").child(comment.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    commentsWithUser.add(new CommentWithUser(comment, user));
                                }

                                if (commentCount.decrementAndGet() == 0) {
                                    Collections.sort(commentsWithUser, (c1, c2) -> Long.compare(c1.getComment().getTimestamp(), c2.getComment().getTimestamp()));
                                    listener.onSuccess(commentsWithUser);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                if (commentCount.decrementAndGet() == 0) {
                                    Collections.sort(commentsWithUser, (c1, c2) -> Long.compare(c1.getComment().getTimestamp(), c2.getComment().getTimestamp()));
                                    listener.onSuccess(commentsWithUser);
                                }
                            }
                        });
                    } else {
                        if (commentCount.decrementAndGet() == 0) {
                            Collections.sort(commentsWithUser, (c1, c2) -> Long.compare(c1.getComment().getTimestamp(), c2.getComment().getTimestamp()));
                            listener.onSuccess(commentsWithUser);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }
}

