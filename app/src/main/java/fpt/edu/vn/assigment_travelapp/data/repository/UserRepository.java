package fpt.edu.vn.assigment_travelapp.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fpt.edu.vn.assigment_travelapp.data.model.User;

public class UserRepository implements IUserRepository {
    private final DatabaseReference databaseReference;

    public UserRepository() {
        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
    }

    @Override
    public Task<Void> saveUser(User user) {
        // We use the user's email as the key in the database, replacing '.' with ','
        return databaseReference.child(user.getEmail().replace(".", ",")).setValue(user);
    }

    @Override
    public Task<DataSnapshot> getUserByEmail(String email) {
        // Query the database for a user with the given email (with '.' replaced by ',')
        return databaseReference.child(email.replace(".", ",")).get();
    }
}
