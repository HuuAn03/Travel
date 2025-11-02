package fpt.edu.vn.assigment_travelapp.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Destination;

public class DestinationRepository {

    private final DatabaseReference mDatabase;

    public DestinationRepository() {
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    /**
     * Get destination by ID
     */
    public void getDestinationById(String destinationId, OnGetDestinationCompleteListener listener) {
        // TODO: Load from Firebase database
        // For now, using sample data based on destinationId
        Destination destination = getSampleDestinationById(destinationId);
        
        if (destination != null) {
            listener.onSuccess(destination);
        } else {
            listener.onFailure("Destination not found");
        }
    }

    /**
     * Get all destinations
     */
    public void getAllDestinations(OnGetDestinationsCompleteListener listener) {
        // TODO: Load from Firebase database
        // For now, return sample data
        List<Destination> destinations = new ArrayList<>();
        destinations.add(getSampleDestinationById("1"));
        destinations.add(getSampleDestinationById("2"));
        destinations.add(getSampleDestinationById("3"));
        listener.onSuccess(destinations);
    }

    /**
     * Get sample destination by ID (should be replaced with Firebase load)
     */
    private Destination getSampleDestinationById(String destinationId) {
        if (destinationId == null) return null;

        switch (destinationId) {
            case "1":
                return new Destination(
                        "1", "Venice Grand Canal Cruise", "Venice, Italy", "Grand Canal, Venice",
                        "Experience the beauty of Venice from the water", 4.5, 234, 139.40,
                        "", new ArrayList<>(), "Adventure", 45.4378, 12.3315);
            case "2":
                return new Destination(
                        "2", "Tahitian Island Getaway", "French Polynesia", "Bora Bora, Tahiti",
                        "Paradise on Earth", 4.5, 156, 299.99,
                        "", new ArrayList<>(), "Beach", -16.5004, -151.7415);
            case "3":
                return new Destination(
                        "3", "Buckingham Palace", "London, UK", "Westminster, London, United Kingdom",
                        "Visit the official residence of the British monarch", 4.5, 532, 64.50,
                        "", new ArrayList<>(), "Adventure", 51.5014, -0.1419);
            default:
                return null;
        }
    }

    public interface OnGetDestinationCompleteListener {
        void onSuccess(Destination destination);
        void onFailure(String errorMessage);
    }

    public interface OnGetDestinationsCompleteListener {
        void onSuccess(List<Destination> destinations);
        void onFailure(String errorMessage);
    }
}

