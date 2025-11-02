package fpt.edu.vn.assigment_travelapp.data.repository;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

import fpt.edu.vn.assigment_travelapp.data.model.User;

public interface IUserRepository {
    void getUser(String userId, OnGetUserCompleteListener listener);
    void getUserByEmail(String email, OnGetUserCompleteListener listener);
    Task<Void> saveUser(String uid, User user);
    void updateUser(String userId, User user, OnUpdateUserCompleteListener listener);
    void updateUserAvatar(String userId, Uri avatarUri, OnUpdateUserCompleteListener listener);
    void followUser(String currentUserId, String targetUserId, OnFollowCompleteListener listener);
    void unfollowUser(String currentUserId, String targetUserId, OnFollowCompleteListener listener);
    void getFollowers(String userId, OnGetUsersCompleteListener listener);
    void getFollowing(String userId, OnGetUsersCompleteListener listener);
    boolean isFollowing(String currentUserId, String targetUserId, OnCheckFollowCompleteListener listener);

    interface OnGetUserCompleteListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    interface OnUpdateUserCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnFollowCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnGetUsersCompleteListener {
        void onSuccess(java.util.List<User> users);
        void onFailure(String errorMessage);
    }

    interface OnCheckFollowCompleteListener {
        void onResult(boolean isFollowing);
    }
}

