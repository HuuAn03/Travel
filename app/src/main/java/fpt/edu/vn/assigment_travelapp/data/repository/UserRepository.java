package fpt.edu.vn.assigment_travelapp.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fpt.edu.vn.assigment_travelapp.data.model.User;

public class UserRepository {
    private final DatabaseReference databaseReference;

    public UserRepository() {
        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public Task<Void> saveUser(User user) {
        // We use the user's UID as the key in the database
        return databaseReference.child(user.getUid()).setValue(user);
    }
}
