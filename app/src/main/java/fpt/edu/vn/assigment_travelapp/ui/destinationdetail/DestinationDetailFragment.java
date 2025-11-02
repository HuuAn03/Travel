package fpt.edu.vn.assigment_travelapp.ui.destinationdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;
import fpt.edu.vn.assigment_travelapp.data.repository.DestinationRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentDestinationDetailBinding;

public class DestinationDetailFragment extends Fragment {

    private FragmentDestinationDetailBinding binding;
    private Destination destination;
    private DestinationRepository destinationRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDestinationDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        destinationRepository = new DestinationRepository();

        // Get destination ID from arguments
        String destinationId = getArguments() != null ? 
                getArguments().getString("destinationId") : null;

        // Load destination data
        loadDestinationData(destinationId);

        setupClickListeners();
        setupGallery();

        return root;
    }

    private void loadDestinationData(String destinationId) {
        if (destinationId == null || destinationId.isEmpty()) {
            android.widget.Toast.makeText(getContext(), "Invalid destination ID", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        destinationRepository.getDestinationById(destinationId, new DestinationRepository.OnGetDestinationCompleteListener() {
            @Override
            public void onSuccess(Destination fetchedDestination) {
                destination = fetchedDestination;
                updateUI();
            }

            @Override
            public void onFailure(String errorMessage) {
                android.util.Log.e("DestinationDetailFragment", "Failed to load destination: " + errorMessage);
                android.widget.Toast.makeText(getContext(), "Failed to load destination", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (destination == null) return;

        binding.tvDestinationName.setText(destination.getName());
        binding.tvLocation.setText(destination.getAddress());
        binding.tvRating.setText(destination.getRating() + " Rating (" + destination.getReviewCount() + " Review)");
        binding.tvDescription.setText(destination.getDescription());
        binding.tvPrice.setText("$" + String.format("%.2f", destination.getPricePerPerson()));

        if (destination.getImageUrl() != null && !destination.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(destination.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(binding.ivHeaderImage);
        }
    }

    private void setupGallery() {
        // Sample gallery images
        List<String> galleryImages = new ArrayList<>();
        galleryImages.add("");
        galleryImages.add("");
        galleryImages.add("");
        galleryImages.add("");

        LinearLayout galleryLayout = binding.layoutGallery;
        galleryLayout.removeAllViews();

        for (int i = 0; i < Math.min(galleryImages.size(), 4); i++) {
            ImageView imageView = new ImageView(getContext());
            int size = (int) (getResources().getDisplayMetrics().density * 80);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginEnd((int) (getResources().getDisplayMetrics().density * 8));
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(R.drawable.image_placeholder);

            if (i == 3 && galleryImages.size() > 4) {
                // Show "12+" overlay for more images
                TextView overlay = new TextView(getContext());
                overlay.setText("12+");
                overlay.setBackgroundColor(0x80000000);
                // Add overlay logic here
            }

            galleryLayout.addView(imageView);
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        binding.tvReadMore.setOnClickListener(v -> {
            // Toggle description expansion
            if (binding.tvDescription.getMaxLines() == 3) {
                binding.tvDescription.setMaxLines(Integer.MAX_VALUE);
                binding.tvReadMore.setText("Read Less..");
            } else {
                binding.tvDescription.setMaxLines(3);
                binding.tvReadMore.setText("Read More..");
            }
        });

        binding.tvSeeAllGalleries.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("destinationId", destination != null ? destination.getDestinationId() : "");
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_destinationDetailFragment_to_galleryFragment, bundle);
        });

        // Book Now button (inside card)
        binding.btnBookNow.setOnClickListener(v -> {
            navigateToBooking();
        });

        binding.btnMenu.setOnClickListener(v -> {
            // Show share dialog or menu
            Bundle bundle = new Bundle();
            bundle.putString("destinationId", destination != null ? destination.getDestinationId() : "");
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_destinationDetailFragment_to_shareDestinationFragment, bundle);
        });
    }

    private void navigateToBooking() {
        Bundle bundle = new Bundle();
        bundle.putString("destinationId", destination != null ? destination.getDestinationId() : "");
        Navigation.findNavController(binding.getRoot())
                .navigate(R.id.action_destinationDetailFragment_to_formScheduleFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

