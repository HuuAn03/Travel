package fpt.edu.vn.assigment_travelapp.ui.nearbydestinations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.DestinationAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentNearbyDestinationsBinding;

public class NearbyDestinationsFragment extends Fragment {

    private FragmentNearbyDestinationsBinding binding;
    private DestinationAdapter destinationAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNearbyDestinationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        setupClickListeners();
        loadNearbyDestinations();

        return root;
    }

    private void setupRecyclerView() {
        destinationAdapter = new DestinationAdapter(new ArrayList<>(), new DestinationAdapter.OnDestinationClickListener() {
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

        binding.rvNearbyDestinations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNearbyDestinations.setAdapter(destinationAdapter);
    }

    private void setupClickListeners() {
        binding.tvSeeAll.setOnClickListener(v -> {
            // Show all nearby destinations
            // Could navigate to a full list screen
        });

        binding.ivFilter.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_nearbyDestinationsFragment_to_filterServiceFragment);
        });

        binding.btnMyLocation.setOnClickListener(v -> {
            // Center map on user's current location
            // TODO: Implement location centering
        });
    }

    private void loadNearbyDestinations() {
        // TODO: Load from repository based on current location
        List<Destination> nearbyDestinations = new ArrayList<>();
        
        nearbyDestinations.add(new Destination(
                "8", "Piazza del Campo", "Italy", "Siena, Italy",
                "Historic square in Siena", 4.4, 234, 83.00,
                "", null, "Adventure", 43.3188, 11.3316));

        nearbyDestinations.add(new Destination(
                "9", "Leaning Tower of Pisa", "Italy", "Pisa, Italy",
                "Famous leaning tower", 4.4, 567, 83.00,
                "", null, "Adventure", 43.7230, 10.3966));

        nearbyDestinations.add(new Destination(
                "10", "Colosseum", "Italy", "Rome, Italy",
                "Ancient Roman amphitheater", 4.6, 1234, 95.00,
                "", null, "Adventure", 41.8902, 12.4922));

        destinationAdapter.updateDestinations(nearbyDestinations);
    }

    private void navigateToDetail(Destination destination) {
        Bundle bundle = new Bundle();
        bundle.putString("destinationId", destination.getDestinationId());
        Navigation.findNavController(binding.getRoot())
                .navigate(R.id.action_nearbyDestinationsFragment_to_destinationDetailFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

