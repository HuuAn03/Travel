package fpt.edu.vn.assigment_travelapp.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Place;
import fpt.edu.vn.assigment_travelapp.data.model.User;

public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";

    private final MutableLiveData<User> userDetails = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<Place>> places = new MutableLiveData<>();
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
    private final DatabaseReference placesRef = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("places");
    private ValueEventListener userDetailsListener;
    private ValueEventListener placesListener;

    public LiveData<User> getUserDetails() {
        return userDetails;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<Place>> getPlaces() {
        return places;
    }

    public void loadUserDetails(String uid) {
        if (userDetailsListener != null) {
            usersRef.child(uid).removeEventListener(userDetailsListener);
        }
        userDetailsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    userDetails.postValue(user);
                } else {
                    error.postValue("User data not found.");
                    Log.w(TAG, "User data not found for UID: " + uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                error.postValue(databaseError.getMessage());
                Log.e(TAG, "Error loading user data: " + databaseError.getMessage());
            }
        };
        usersRef.child(uid).addValueEventListener(userDetailsListener);
    }

    public void loadPopularPlaces() {
        if (placesListener != null) {
            placesRef.removeEventListener(placesListener);
        }
        
        placesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Place> placeList = new ArrayList<>();
                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    Place place = placeSnapshot.getValue(Place.class);
                    if (place != null && place.isAvailable()) {
                        place.setPlaceId(placeSnapshot.getKey());
                        placeList.add(place);
                    }
                }
                places.postValue(placeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                error.postValue(databaseError.getMessage());
                Log.e(TAG, "Error loading places: " + databaseError.getMessage());
            }
        };
        placesRef.addListenerForSingleValueEvent(placesListener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (userDetailsListener != null && usersRef != null) {
            // To prevent memory leaks, we should remove the listener when the ViewModel is cleared
            // You might need to adjust this depending on your specific lifecycle needs
        }
        if (placesListener != null && placesRef != null) {
            placesRef.removeEventListener(placesListener);
        }
    }
}
