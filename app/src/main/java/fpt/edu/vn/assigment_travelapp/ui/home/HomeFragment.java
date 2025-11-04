package fpt.edu.vn.assigment_travelapp.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.PlaceAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Place;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;

    private DatabaseReference userRef;
    private ValueEventListener userListener;
    private HomeViewModel viewModel;
    private PlaceAdapter placeAdapter;
    private List<Place> placeList = new ArrayList<>();
    private String selectedCategory = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.locationContainer.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Change Location")
                .setMessage("Do you want to change your current location?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_chooseLocationFragment);
                })
                .setNegativeButton("No", null)
                .show();
        });

        binding.searchBar.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_searchLocationFragment);
        });

        binding.searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_searchLocationFragment);
            }
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        } else {
            // Handle case where user is not logged in
            binding.userName.setText("Hi, Guest");
            binding.userLocation.setText("Unknown location");
            binding.profileImage.setImageResource(R.drawable.ic_profile);
        }

        View.OnClickListener categoryClickListener = v -> {
            v.setSelected(!v.isSelected());
        };

        setupRecyclerView();
        setupCategoryButtons();
        setupFabBooking();
        observeViewModel();
        
        // Load popular places
        viewModel.loadPopularPlaces();
    }

    private void setupFabBooking() {
        binding.fabBooking.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_navigation_home_to_searchLocationFragment);
        });
    }

    private void setupRecyclerView() {
        placeAdapter = new PlaceAdapter(placeList);
        placeAdapter.setOnPlaceClickListener(place -> {
            // Navigate directly to booking fragment with placeId
            Bundle bundle = new Bundle();
            bundle.putString("placeId", place.getPlaceId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_navigation_home_to_bookingFragment, bundle);
        });
        binding.recyclerViewPopular.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewPopular.setAdapter(placeAdapter);
    }

    private void setupCategoryButtons() {
        binding.btnAdventure.setOnClickListener(v -> {
            filterByCategory("Adventure");
            updateButtonSelection(binding.btnAdventure, binding.btnBeach, binding.btnFoodDrink);
        });
        
        binding.btnBeach.setOnClickListener(v -> {
            filterByCategory("Beach");
            updateButtonSelection(binding.btnBeach, binding.btnAdventure, binding.btnFoodDrink);
        });
        
        binding.btnFoodDrink.setOnClickListener(v -> {
            filterByCategory("Food & Drink");
            updateButtonSelection(binding.btnFoodDrink, binding.btnAdventure, binding.btnBeach);
        });
    }

    private void updateButtonSelection(View selected, View... others) {
        selected.setSelected(true);
        for (View other : others) {
            other.setSelected(false);
        }
    }

    private void filterByCategory(String category) {
        selectedCategory = category;
        viewModel.loadPopularPlaces();
    }

    private void observeViewModel() {
        viewModel.getPlaces().observe(getViewLifecycleOwner(), places -> {
            placeList.clear();
            if (places != null) {
                if (selectedCategory != null) {
                    for (Place place : places) {
                        if (place.getCategory() != null && place.getCategory().equalsIgnoreCase(selectedCategory)) {
                            placeList.add(place);
                        }
                    }
                } else {
                    // Show top 10 most popular (by rating or all if less than 10)
                    List<Place> sortedPlaces = new ArrayList<>(places);
                    sortedPlaces.sort((p1, p2) -> Integer.compare(p2.getRating(), p1.getRating()));
                    placeList.addAll(sortedPlaces.subList(0, Math.min(10, sortedPlaces.size())));
                }
            }
            placeAdapter.notifyDataSetChanged();
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error loading places: " + error);
            }
        });
    }

    private void loadUserData(String uid) {
        userRef = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(uid);

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null || !snapshot.exists()) {
                    Log.w(TAG, "User data not found or context is null.");
                    return;
                }

                User user = snapshot.getValue(User.class);
                if (user != null) {
                    binding.userName.setText("Hi, " + user.getName());
                    if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                        binding.userLocation.setText(user.getLocation());
                    } else {
                        binding.userLocation.setText("Unknown location");
                    }

                    String photoUrl = user.getPhotoUrl();
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                            Glide.with(requireContext())
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(binding.profileImage);
                        } else {
                            try {
                                byte[] decodedString = Base64.decode(photoUrl, Base64.DEFAULT);
                                Glide.with(requireContext())
                                        .asBitmap()
                                        .load(decodedString)
                                        .placeholder(R.drawable.ic_profile)
                                        .into(binding.profileImage);
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "Error decoding Base64 string: ", e);
                                binding.profileImage.setImageResource(R.drawable.ic_profile);
                            }
                        }
                    } else {
                        binding.profileImage.setImageResource(R.drawable.ic_profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read user value.", error.toException());
            }
        };
        userRef.addValueEventListener(userListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null && userRef != null) {
            userRef.removeEventListener(userListener);
        }
        binding = null;
    }
}
