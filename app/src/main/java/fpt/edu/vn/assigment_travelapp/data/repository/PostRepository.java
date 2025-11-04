package fpt.edu.vn.assigment_travelapp.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.Post;
import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.User;

public class PostRepository implements IPostRepository {

    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;

    public PostRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    private void createNotification(String postOwnerId, String postId, String postImageUrl, String message, String type) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || currentUser.getUid().equals(postOwnerId)) {
            return;
        }
        String currentUserId = currentUser.getUid();

        DatabaseReference notificationsRef = mDatabase.child("notifications").child(postOwnerId);
        String notificationId = notificationsRef.push().getKey();

        mDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User triggeringUser = snapshot.getValue(User.class);
                if (triggeringUser != null) {
                    String finalMessage = "<b>" + triggeringUser.getName() + "</b> " + message;
                    long timestamp = System.currentTimeMillis();

                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("id", notificationId);
                    notificationData.put("type", type);
                    notificationData.put("triggeringUserId", currentUserId);
                    notificationData.put("triggeringUserAvatar", triggeringUser.getPhotoUrl());
                    notificationData.put("message", finalMessage);
                    notificationData.put("postId", postId);
                    notificationData.put("postImageUrl", postImageUrl);
                    notificationData.put("timestamp", timestamp);
                    notificationData.put("read", false);

                    if (notificationId != null) {
                        notificationsRef.child(notificationId).setValue(notificationData);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void deleteNotification(String postOwnerId, String postId, String type) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String currentUserId = currentUser.getUid();
        DatabaseReference notificationsRef = mDatabase.child("notifications").child(postOwnerId);
        Query notificationQuery = notificationsRef.orderByChild("triggeringUserId").equalTo(currentUserId);
        notificationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (postId.equals(snapshot.child("postId").getValue(String.class)) && type.equals(snapshot.child("type").getValue(String.class))) {
                        snapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    @Override
    public void createPost(String base64Image, String caption, OnPostCreationCompleteListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        String userId = currentUser.getUid();
        String postId = mDatabase.child("posts").push().getKey();
        long timestamp = System.currentTimeMillis();

        Post post = new Post(postId, base64Image, caption, userId, timestamp);

        if (postId != null) {
            mDatabase.child("posts").child(postId).setValue(post)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        }
    }

    @Override
    public void getAllPosts(OnGetAllPostsCompleteListener listener) {
        mDatabase.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    if (user.getUserId() == null) {
                                        user.setUserId(userSnapshot.getKey());
                                    }
                                    postsWithUser.add(new PostWithUser(post, user));
                                }

                                if (postCount.decrementAndGet() == 0) {
                                    listener.onSuccess(postsWithUser);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                if (postCount.decrementAndGet() == 0) {
                                    listener.onSuccess(postsWithUser); // Still return what we have
                                }
                            }
                        });
                    } else {
                        if (postCount.decrementAndGet() == 0) {
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
    public void addComment(String postId, Comment comment, OnCommentAddCompleteListener listener) {
        String commentId = mDatabase.child("posts").child(postId).child("comments").push().getKey();
        if (commentId == null) {
            listener.onFailure("Couldn't generate a comment ID");
            return;
        }

        comment.setCommentId(commentId);
        mDatabase.child("posts").child(postId).child("comments").child(commentId).setValue(comment)
                .addOnSuccessListener(aVoid -> {
                    getPost(postId, new OnGetPostCompleteListener() {
                        @Override
                        public void onSuccess(Post post) {
                            createNotification(post.getUserId(), postId, post.getImageUrl(), "commented on your post: \"" + comment.getText() + "\"", "comment");
                        }
                        @Override
                        public void onFailure(String error) {}
                    });
                    listener.onSuccess();
                })
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
                                    if (user.getUserId() == null) {
                                        user.setUserId(userSnapshot.getKey());
                                    }
                                    commentsWithUser.add(new CommentWithUser(comment, user));
                                }

                                if (commentCount.decrementAndGet() == 0) {
                                    listener.onSuccess(commentsWithUser);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                if (commentCount.decrementAndGet() == 0) {
                                    listener.onSuccess(commentsWithUser);
                                }
                            }
                        });
                    } else {
                        if (commentCount.decrementAndGet() == 0) {
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

    @Override
    public void deletePost(String postId, OnDeletePostCompleteListener listener) {
        mDatabase.child("posts").child(postId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void getPost(String postId, OnGetPostCompleteListener listener) {
        mDatabase.child("posts").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    listener.onSuccess(post);
                } else {
                    listener.onFailure("Post not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void updatePost(String postId, String base64Image, String caption, OnPostUpdateCompleteListener listener) {
        Map<String, Object> postUpdates = new HashMap<>();
        postUpdates.put("imageUrl", base64Image);
        postUpdates.put("caption", caption);

        mDatabase.child("posts").child(postId).updateChildren(postUpdates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void getPostsByUserId(String userId, OnGetPostsCompleteListener listener) {
        mDatabase.child("posts").orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> userPosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        userPosts.add(post);
                    }
                }
                listener.onSuccess(userPosts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getLikedPosts(String userId, OnGetPostsCompleteListener listener) {
        mDatabase.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> likedPosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null && post.getLikes() != null && post.getLikes().containsKey(userId)) {
                        likedPosts.add(post);
                    }
                }
                listener.onSuccess(likedPosts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getBookmarkedPosts(String userId, OnGetPostsCompleteListener listener) {
        mDatabase.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> bookmarkedPosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null && post.getBookmarks() != null && post.getBookmarks().containsKey(userId)) {
                        bookmarkedPosts.add(post);
                    }
                }
                listener.onSuccess(bookmarkedPosts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getLikeCount(String postId, OnLikeCountCompleteListener listener) {
        mDatabase.child("posts").child(postId).child("likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onComplete(snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void isLiked(String postId, String userId, OnIsLikedCompleteListener listener) {
        mDatabase.child("posts").child(postId).child("likes").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onComplete(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void toggleLike(String postId, String userId, OnLikeToggleCompleteListener listener) {
        DatabaseReference postRef = mDatabase.child("posts").child(postId);
        postRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post post = mutableData.getValue(Post.class);
                if (post == null) {
                    return Transaction.success(mutableData);
                }

                if (post.getLikes().containsKey(userId)) {
                    post.getLikes().remove(userId);
                } else {
                    post.getLikes().put(userId, true);
                }

                mutableData.setValue(post);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, @NonNull DataSnapshot currentData) {
                if (committed) {
                    Post post = currentData.getValue(Post.class);
                    if (post != null) {
                        boolean isLiked = post.getLikes().containsKey(userId);
                        listener.onComplete(isLiked, post.getLikes().size());
                        if (isLiked) {
                            createNotification(post.getUserId(), postId, post.getImageUrl(), "liked your post", "like");
                        } else {
                            deleteNotification(post.getUserId(), postId, "like");
                        }
                    }
                }
            }
        });
    }

    @Override
    public void isBookmarked(String postId, String userId, OnIsBookmarkedCompleteListener listener) {
        mDatabase.child("posts").child(postId).child("bookmarks").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onComplete(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void toggleBookmark(String postId, String userId, OnBookmarkToggleCompleteListener listener) {
        DatabaseReference postRef = mDatabase.child("posts").child(postId);
        postRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post post = mutableData.getValue(Post.class);
                if (post == null) {
                    return Transaction.success(mutableData);
                }

                if (post.getBookmarks().containsKey(userId)) {
                    post.getBookmarks().remove(userId);
                } else {
                    post.getBookmarks().put(userId, true);
                }

                mutableData.setValue(post);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, @NonNull DataSnapshot currentData) {
                if (committed) {
                    Post post = currentData.getValue(Post.class);
                    if (post != null) {
                        boolean isBookmarked = post.getBookmarks().containsKey(userId);
                        listener.onComplete(isBookmarked);
                        if (isBookmarked) {
                            createNotification(post.getUserId(), postId, post.getImageUrl(), "bookmarked your post", "bookmark");
                        } else {
                            deleteNotification(post.getUserId(), postId, "bookmark");
                        }
                    }
                }
            }
        });
    }
}
