package fpt.edu.vn.assigment_travelapp.data.repository;

import com.google.android.gms.tasks.Task;

import fpt.edu.vn.assigment_travelapp.data.model.User;

public interface IUserRepository {
    void getUser(String userId, OnGetUserCompleteListener listener);
    Task<Void> saveUser(String uid, User user);

    interface OnGetUserCompleteListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }
}
