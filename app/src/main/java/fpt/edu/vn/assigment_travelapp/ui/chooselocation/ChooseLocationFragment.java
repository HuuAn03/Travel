package fpt.edu.vn.assigment_travelapp.ui.chooselocation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentChooseLocationBinding;

public class ChooseLocationFragment extends Fragment {

    private FragmentChooseLocationBinding binding;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedLocationName = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChooseLocationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupClickListeners();
        getCurrentLocation();

        return root;
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        binding.btnSetLocationMap.setOnClickListener(v -> {
            // Navigate to map selection screen
            // For now, just show a message
            Toast.makeText(getContext(), "Map selection feature coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnUseCurrentLocation.setOnClickListener(v -> {
            // Use selected location and go back
            if (selectedLocationName.isEmpty()) {
                Toast.makeText(getContext(), "Please select a location", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Save selected location to shared preferences or viewmodel
            // Then navigate back
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        // Search bar click
        binding.searchBar.setOnClickListener(v -> {
            // Show search dialog or navigate to search
            Toast.makeText(getContext(), "Search location", Toast.LENGTH_SHORT).show();
        });
    }

    private void getCurrentLocation() {
        // TODO: Implement actual location fetching using FusedLocationProviderClient
        // For now, use sample location
        selectedLocationName = "Beverly rd";
        selectedLatitude = 40.7128; // Sample coordinates
        selectedLongitude = -74.0060;

        binding.tvLocationLabel.setText(selectedLocationName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

