package fpt.edu.vn.assigment_travelapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.DestinationAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private DestinationAdapter destinationAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        setupClickListeners();
        
        // Observe destinations from ViewModel
        homeViewModel.getDestinations().observe(getViewLifecycleOwner(), destinations -> {
            if (destinations != null) {
                destinationAdapter.updateDestinations(destinations);
            }
        });

        // Load sample data for demo
        loadSampleDestinations();

        return root;
    }

    private void setupRecyclerView() {
        destinationAdapter = new DestinationAdapter(new ArrayList<>(), new DestinationAdapter.OnDestinationClickListener() {
            @Override
            public void onDestinationClick(Destination destination) {
                // Navigate to destination detail
                Bundle bundle = new Bundle();
                bundle.putString("destinationId", destination.getDestinationId());
                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_navigation_home_to_destinationDetailFragment, bundle);
            }

            @Override
            public void onFavoriteClick(Destination destination) {
                // Toggle favorite
                homeViewModel.toggleFavorite(destination);
            }

            @Override
            public void onActionClick(Destination destination) {
                // Navigate to destination detail
                onDestinationClick(destination);
            }
        });

        binding.rvDestinations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvDestinations.setAdapter(destinationAdapter);
    }

    private void setupClickListeners() {
        binding.searchBar.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_home_to_searchFragment);
        });

        binding.btnAdventure.setOnClickListener(v -> filterByCategory("Adventure"));
        binding.btnBeach.setOnClickListener(v -> filterByCategory("Beach"));
        binding.btnFoodDrink.setOnClickListener(v -> filterByCategory("Food & Drink"));
    }

    private void filterByCategory(String category) {
        // Reset all buttons
        resetCategoryButtons();
        
        // Highlight selected button
        switch (category) {
            case "Adventure":
                binding.btnAdventure.setBackgroundTintList(getResources().getColorStateList(R.color.green_brand));
                binding.btnAdventure.setTextColor(getResources().getColor(R.color.white));
                break;
            case "Beach":
                binding.btnBeach.setBackgroundTintList(getResources().getColorStateList(R.color.green_brand));
                binding.btnBeach.setTextColor(getResources().getColor(R.color.white));
                break;
            case "Food & Drink":
                binding.btnFoodDrink.setBackgroundTintList(getResources().getColorStateList(R.color.green_brand));
                binding.btnFoodDrink.setTextColor(getResources().getColor(R.color.white));
                break;
        }

        // Filter destinations
        homeViewModel.filterByCategory(category);
    }

    private void resetCategoryButtons() {
        binding.btnAdventure.setBackgroundTintList(getResources().getColorStateList(R.color.white));
        binding.btnAdventure.setTextColor(getResources().getColor(R.color.black));
        binding.btnBeach.setBackgroundTintList(getResources().getColorStateList(R.color.white));
        binding.btnBeach.setTextColor(getResources().getColor(R.color.black));
        binding.btnFoodDrink.setBackgroundTintList(getResources().getColorStateList(R.color.white));
        binding.btnFoodDrink.setTextColor(getResources().getColor(R.color.black));
    }

    private void loadSampleDestinations() {
        List<Destination> sampleDestinations = new ArrayList<>();
        
        // Sample data - can be replaced with real data from repository
        sampleDestinations.add(new Destination(
                "1", "Venice Grand Canal Cruise", "Venice, Italy", "Grand Canal, Venice",
                "Experience the beauty of Venice from the water", 4.5, 234, 139.40,
                "", new ArrayList<>(), "Adventure", 45.4378, 12.3315));

        sampleDestinations.add(new Destination(
                "2", "Tahitian Island Getaway", "French Polynesia", "Bora Bora, Tahiti",
                "Paradise on Earth", 4.5, 156, 299.99,
                "", new ArrayList<>(), "Beach", -16.5004, -151.7415));

        sampleDestinations.add(new Destination(
                "3", "Buckingham Palace", "London, UK", "Westminster, London",
                "Visit the official residence of the British monarch", 4.5, 532, 64.50,
                "", new ArrayList<>(), "Adventure", 51.5014, -0.1419));

        homeViewModel.setDestinations(sampleDestinations);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}