package fpt.edu.vn.assigment_travelapp.ui.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Place;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentManagePlacesBinding;

public class ManagePlacesFragment extends Fragment {

    private FragmentManagePlacesBinding binding;
    private DatabaseReference placesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagePlacesBinding.inflate(inflater, container, false);
        placesRef = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("places");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAddPlace.setOnClickListener(v -> addPlace());
        binding.btnAddSampleData.setOnClickListener(v -> addSamplePlaces());
    }

    private void addPlace() {
        String name = binding.etPlaceName.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String imageUrl = binding.etImageUrl.getText().toString().trim();
        String latitudeStr = binding.etLatitude.getText().toString().trim();
        String longitudeStr = binding.etLongitude.getText().toString().trim();
        String priceStr = binding.etPricePerDay.getText().toString().trim();
        String category = binding.etCategory.getText().toString().trim();
        String ratingStr = binding.etRating.getText().toString().trim();

        // Validation
        if (name.isEmpty() || address.isEmpty() || priceStr.isEmpty() || category.isEmpty() || ratingStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double latitude = latitudeStr.isEmpty() ? 0.0 : Double.parseDouble(latitudeStr);
            double longitude = longitudeStr.isEmpty() ? 0.0 : Double.parseDouble(longitudeStr);
            double price = Double.parseDouble(priceStr);
            int rating = Integer.parseInt(ratingStr);

            if (rating < 1 || rating > 5) {
                Toast.makeText(getContext(), "Rating must be between 1 and 5", Toast.LENGTH_SHORT).show();
                return;
            }

            String placeId = placesRef.push().getKey();
            if (placeId == null) {
                Toast.makeText(getContext(), "Failed to create place ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Place place = new Place(placeId, name, description, address, imageUrl, latitude, longitude, price, category, rating);

            placesRef.child(placeId).setValue(place)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Place added successfully!", Toast.LENGTH_SHORT).show();
                        clearForm();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to add place: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        binding.etPlaceName.setText("");
        binding.etDescription.setText("");
        binding.etAddress.setText("");
        binding.etImageUrl.setText("");
        binding.etLatitude.setText("");
        binding.etLongitude.setText("");
        binding.etPricePerDay.setText("");
        binding.etCategory.setText("");
        binding.etRating.setText("");
    }

    private void addSamplePlaces() {
        // Sample Place 1 - Adventure
        Place place1 = new Place(
                null, "Ha Long Bay Adventure",
                "Experience the stunning beauty of Ha Long Bay with kayaking and cave exploration.",
                "Ha Long Bay, Quang Ninh, Vietnam",
                "https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=800",
                20.9101, 107.1839,
                1500000, "Adventure", 5
        );

        // Sample Place 2 - Beach
        Place place2 = new Place(
                null, "Nha Trang Beach Resort",
                "Beautiful beachfront resort with crystal clear water and white sandy beach.",
                "Nha Trang, Khanh Hoa, Vietnam",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
                12.2388, 109.1967,
                2000000, "Beach", 5
        );

        // Sample Place 3 - Food & Drink
        Place place3 = new Place(
                null, "Hanoi Street Food Tour",
                "Explore the vibrant street food scene in Hanoi's Old Quarter.",
                "Hanoi Old Quarter, Hoan Kiem, Hanoi, Vietnam",
                "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
                21.0285, 105.8542,
                500000, "Food & Drink", 4
        );

        // Sample Place 4 - Adventure
        Place place4 = new Place(
                null, "Sapa Mountain Trekking",
                "Trek through terraced rice fields and visit ethnic minority villages.",
                "Sapa, Lao Cai, Vietnam",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800",
                22.3369, 103.8443,
                1200000, "Adventure", 5
        );

        // Sample Place 5 - Beach
        Place place5 = new Place(
                null, "Phu Quoc Island Paradise",
                "Relax on pristine beaches and enjoy tropical island vibes.",
                "Phu Quoc Island, Kien Giang, Vietnam",
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800",
                10.2899, 103.9840,
                1800000, "Beach", 5
        );

        // Sample Place 6 - Food & Drink
        Place place6 = new Place(
                null, "Ho Chi Minh City Food Tour",
                "Discover authentic Vietnamese cuisine in the bustling streets of Saigon.",
                "District 1, Ho Chi Minh City, Vietnam",
                "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800",
                10.7769, 106.7009,
                600000, "Food & Drink", 4
        );

        // Sample Place 7 - Adventure
        Place place7 = new Place(
                null, "Da Lat Waterfall Adventure",
                "Visit stunning waterfalls and enjoy outdoor activities in the cool mountain air.",
                "Da Lat, Lam Dong, Vietnam",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800",
                11.9404, 108.4583,
                800000, "Adventure", 4
        );

        // Sample Place 8 - Beach
        Place place8 = new Place(
                null, "Da Nang Beach Resort",
                "Modern beach resort with amazing ocean views and great amenities.",
                "My Khe Beach, Da Nang, Vietnam",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
                16.0471, 108.2068,
                1600000, "Beach", 5
        );

        // Add all places individually
        Place[] places = {place1, place2, place3, place4, place5, place6, place7, place8};
        final int[] count = {0};
        final int total = places.length;

        for (Place place : places) {
            String placeId = placesRef.push().getKey();
            if (placeId != null) {
                place.setPlaceId(placeId);
                placesRef.child(placeId).setValue(place)
                        .addOnSuccessListener(aVoid -> {
                            count[0]++;
                            if (count[0] == total) {
                                Toast.makeText(getContext(), "Sample places added successfully!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to add place: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

