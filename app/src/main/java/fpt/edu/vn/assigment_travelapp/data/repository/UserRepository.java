package fpt.edu.vn.assigment_travelapp.data.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fpt.edu.vn.assigment_travelapp.data.model.User;

public class UserRepository implements IUserRepository {

    private final DatabaseReference mDatabase;

    public UserRepository() {
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
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
    public Task<Void> saveUser(String uid, User user) {
        return mDatabase.child("users").child(uid).setValue(user);
    }
}
