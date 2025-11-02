package fpt.edu.vn.assigment_travelapp.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.DestinationAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentSearchBinding;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private List<String> lastSearches;
    private DestinationAdapter recentlyViewedAdapter;
    private DestinationAdapter popularAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        lastSearches = new ArrayList<>();
        setupRecyclerViews();
        setupClickListeners();
        loadSampleData();

        return root;
    }

    private void setupRecyclerViews() {
        // Recently Viewed RecyclerView
        recentlyViewedAdapter = new DestinationAdapter(new ArrayList<>(), new DestinationAdapter.OnDestinationClickListener() {
            @Override
            public void onDestinationClick(Destination destination) {
                navigateToDetail(destination);
            }

            @Override
            public void onFavoriteClick(Destination destination) {
                // Toggle favorite
            }

            @Override
            public void onActionClick(Destination destination) {
                navigateToDetail(destination);
            }
        });

        binding.rvRecentlyViewed.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecentlyViewed.setAdapter(recentlyViewedAdapter);

        // Popular Destinations RecyclerView (Horizontal)
        popularAdapter = new DestinationAdapter(new ArrayList<>(), new DestinationAdapter.OnDestinationClickListener() {
            @Override
            public void onDestinationClick(Destination destination) {
                navigateToDetail(destination);
            }

            @Override
            public void onFavoriteClick(Destination destination) {
                // Toggle favorite
            }

            @Override
            public void onActionClick(Destination destination) {
                navigateToDetail(destination);
            }
        });

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvPopularDestinations.setLayoutManager(horizontalLayoutManager);
        binding.rvPopularDestinations.setAdapter(popularAdapter);
    }

    private void setupClickListeners() {
        binding.ivFilter.setOnClickListener(v -> {
            // Navigate to filter screen
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_searchFragment_to_filterServiceFragment);
        });

        binding.tvClearAll.setOnClickListener(v -> {
            lastSearches.clear();
            updateLastSearchChips();
        });

        binding.tvViewAllRecent.setOnClickListener(v -> {
            // Show all recently viewed
        });

        binding.tvViewAllPopular.setOnClickListener(v -> {
            // Show all popular destinations
        });
    }

    private void loadSampleData() {
        // Last searches
        lastSearches = new ArrayList<>(Arrays.asList("Montain", "Armadianan Hotel", "Island", "River"));
        updateLastSearchChips();

        // Recently viewed destinations
        List<Destination> recentlyViewed = new ArrayList<>();
        recentlyViewed.add(new Destination(
                "4", "Moraine Lake", "Canada", "Banff, Canada",
                "Beautiful turquoise lake", 4.4, 123, 83.00,
                "", null, "Adventure", 51.3, -115.9));

        recentlyViewed.add(new Destination(
                "5", "Queen Anne Beach", "Hawaii", "Hawaii, USA",
                "Tropical paradise", 4.5, 234, 78.00,
                "", null, "Beach", 21.3, -157.8));

        recentlyViewedAdapter.updateDestinations(recentlyViewed);

        // Popular destinations
        List<Destination> popular = new ArrayList<>();
        popular.add(new Destination(
                "6", "French Polynesia", "French Polynesia", "Bora Bora",
                "Stunning tropical landscape", 4.5, 345, 299.99,
                "", null, "Beach", -16.5, -151.7));

        popular.add(new Destination(
                "7", "Venice", "Venice, Italy", "Venice",
                "City of canals", 4.5, 456, 139.40,
                "", null, "Adventure", 45.4, 12.3));

        popularAdapter.updateDestinations(popular);
    }

    private void updateLastSearchChips() {
        LinearLayout layout = binding.layoutLastSearch;
        layout.removeAllViews();

        for (String search : lastSearches) {
            Chip chip = new Chip(getContext());
            chip.setText(search);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                lastSearches.remove(search);
                updateLastSearchChips();
            });
            chip.setOnClickListener(v -> {
                binding.etSearch.setText(search);
                // Perform search
            });
            layout.addView(chip);
        }
    }

    private void navigateToDetail(Destination destination) {
        Bundle bundle = new Bundle();
        bundle.putString("destinationId", destination.getDestinationId());
        Navigation.findNavController(binding.getRoot())
                .navigate(R.id.action_searchFragment_to_destinationDetailFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

