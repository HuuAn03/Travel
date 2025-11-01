package fpt.edu.vn.assigment_travelapp.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import fpt.edu.vn.assigment_travelapp.data.model.User;

public interface IUserRepository {
    Task<Void> saveUser(User user);
    Task<DataSnapshot> getUserByEmail(String email);
}
