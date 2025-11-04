package fpt.edu.vn.assigment_travelapp.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Place;

public class PlaceRepository implements IPlaceRepository {

    private final DatabaseReference mDatabase;

    public PlaceRepository() {
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    @Override
    public void searchPlaces(String query, OnPlacesFetchedListener listener) {
        mDatabase.child("places").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Place> places = new ArrayList<>();
                String lowerQuery = query.toLowerCase();
                
                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    Place place = placeSnapshot.getValue(Place.class);
                    if (place != null) {
                        // Search by name, address, or category
                        if (place.getName().toLowerCase().contains(lowerQuery) ||
                            place.getAddress().toLowerCase().contains(lowerQuery) ||
                            (place.getCategory() != null && place.getCategory().toLowerCase().contains(lowerQuery))) {
                            place.setPlaceId(placeSnapshot.getKey());
                            places.add(place);
                        }
                    }
                }
                listener.onSuccess(places);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getAllPlaces(OnPlacesFetchedListener listener) {
        mDatabase.child("places").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Place> places = new ArrayList<>();
                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    Place place = placeSnapshot.getValue(Place.class);
                    if (place != null) {
                        place.setPlaceId(placeSnapshot.getKey());
                        places.add(place);
                    }
                }
                listener.onSuccess(places);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getPlaceById(String placeId, OnPlaceFetchedListener listener) {
        mDatabase.child("places").child(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Place place = snapshot.getValue(Place.class);
                    if (place != null) {
                        place.setPlaceId(snapshot.getKey());
                        listener.onSuccess(place);
                    } else {
                        listener.onFailure("Place data is null");
                    }
                } else {
                    listener.onFailure("Place not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }
}

