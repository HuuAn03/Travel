package fpt.edu.vn.assigment_travelapp.data.repository;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fpt.edu.vn.assigment_travelapp.data.model.User;

public class UserRepository implements IUserRepository {

    private final DatabaseReference mDatabase;
    private final StorageReference mStorageRef;

    public UserRepository() {
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public Task<DataSnapshot> getUserByEmail(String email) {
        String dbKey = email.replace(".", ",");
        return mDatabase.child("users").child(dbKey).get();
    }

    @Override
    public void getUser(String userId, OnGetUserCompleteListener listener) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    listener.onSuccess(user);
                } else {
                    listener.onFailure("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getUserByEmail(String email, OnGetUserCompleteListener listener) {
        String dbKey = email.replace(".", ",");
        mDatabase.child("users").child(dbKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    listener.onSuccess(user);
                } else {
                    listener.onFailure("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public Task<Void> saveUser(String uid, User user) {
        return mDatabase.child("users").child(uid).setValue(user);
    }

    @Override
    public void updateUser(String userId, User user, OnUpdateUserCompleteListener listener) {
        mDatabase.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void updateUserAvatar(String userId, Uri avatarUri, OnUpdateUserCompleteListener listener) {
        StorageReference avatarRef = mStorageRef.child("avatars").child(userId + ".jpg");
        avatarRef.putFile(avatarUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return avatarRef.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        mDatabase.child("users").child(userId).child("photoUrl").setValue(downloadUri.toString())
                                .addOnSuccessListener(aVoid -> listener.onSuccess())
                                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                    } else {
                        listener.onFailure(task.getException().getMessage());
                    }
                });
    }

    @Override
    public void followUser(String currentUserId, String targetUserId, OnFollowCompleteListener listener) {
        // Add to current user's following list
        mDatabase.child("users").child(currentUserId).child("following").child(targetUserId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Add to target user's followers list
                    mDatabase.child("users").child(targetUserId).child("followers").child(currentUserId).setValue(true)
                            .addOnSuccessListener(aVoid1 -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void unfollowUser(String currentUserId, String targetUserId, OnFollowCompleteListener listener) {
        // Remove from current user's following list
        mDatabase.child("users").child(currentUserId).child("following").child(targetUserId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Remove from target user's followers list
                    mDatabase.child("users").child(targetUserId).child("followers").child(currentUserId).removeValue()
                            .addOnSuccessListener(aVoid1 -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void getFollowers(String userId, OnGetUsersCompleteListener listener) {
        mDatabase.child("users").child(userId).child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot followersSnapshot) {
                if (!followersSnapshot.exists()) {
                    listener.onSuccess(new ArrayList<>());
                    return;
                }

                List<String> followerIds = new ArrayList<>();
                for (DataSnapshot followerIdSnapshot : followersSnapshot.getChildren()) {
                    followerIds.add(followerIdSnapshot.getKey());
                }

                List<User> users = new ArrayList<>();
                AtomicInteger userCount = new AtomicInteger(followerIds.size());

                if (followerIds.isEmpty()) {
                    listener.onSuccess(users);
                    return;
                }

                for (String followerId : followerIds) {
                    mDatabase.child("users").child(followerId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                users.add(user);
                            }

                            if (userCount.decrementAndGet() == 0) {
                                listener.onSuccess(users);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (userCount.decrementAndGet() == 0) {
                                listener.onSuccess(users);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getFollowing(String userId, OnGetUsersCompleteListener listener) {
        mDatabase.child("users").child(userId).child("following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot followingSnapshot) {
                if (!followingSnapshot.exists()) {
                    listener.onSuccess(new ArrayList<>());
                    return;
                }

                List<String> followingIds = new ArrayList<>();
                for (DataSnapshot followingIdSnapshot : followingSnapshot.getChildren()) {
                    followingIds.add(followingIdSnapshot.getKey());
                }

                List<User> users = new ArrayList<>();
                AtomicInteger userCount = new AtomicInteger(followingIds.size());

                if (followingIds.isEmpty()) {
                    listener.onSuccess(users);
                    return;
                }

                for (String followingId : followingIds) {
                    mDatabase.child("users").child(followingId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                users.add(user);
                            }

                            if (userCount.decrementAndGet() == 0) {
                                listener.onSuccess(users);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (userCount.decrementAndGet() == 0) {
                                listener.onSuccess(users);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public boolean isFollowing(String currentUserId, String targetUserId, OnCheckFollowCompleteListener listener) {
        mDatabase.child("users").child(currentUserId).child("following").child(targetUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onResult(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onResult(false);
            }
        });
        return false; // This is async, return value is not meaningful
    }
}

