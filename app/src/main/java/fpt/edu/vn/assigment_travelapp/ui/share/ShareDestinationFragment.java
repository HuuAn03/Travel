package fpt.edu.vn.assigment_travelapp.ui.share;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentShareDestinationBinding;

public class ShareDestinationFragment extends Fragment {

    private FragmentShareDestinationBinding binding;
    private Destination destination;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShareDestinationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get destination ID from arguments
        String destinationId = getArguments() != null ?
                getArguments().getString("destinationId") : null;

        loadDestinationData(destinationId);
        setupClickListeners();

        return root;
    }

    private void loadDestinationData(String destinationId) {
        // TODO: Load from repository
        destination = new Destination(
                "3", "Buckingham Palace", "London, UK", "Westminster, London, United Kingdom",
                "Visit the official residence", 4.5, 532, 64.50,
                "", null, "Adventure", 51.5014, -0.1419);

        updateUI();
    }

    private void updateUI() {
        if (destination == null) return;

        binding.tvDestinationName.setText(destination.getName());
        binding.tvLocation.setText(destination.getLocation());
        binding.tvRating.setText(destination.getRating() + " Rating (" + destination.getReviewCount() + " Review)");
        binding.tvShareLink.setText("Travely.com/detail/" + destination.getDestinationId().toLowerCase() + "...");

        if (destination.getImageUrl() != null && !destination.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(destination.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(binding.ivDestinationThumb);
        }
    }

    private void setupClickListeners() {
        binding.btnClose.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        binding.btnCopy.setOnClickListener(v -> {
            // Copy link to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(
                    "Share Link", binding.tvShareLink.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Link copied!", Toast.LENGTH_SHORT).show();
        });

        binding.btnAirdrop.setOnClickListener(v -> shareViaPlatform("Airdrop"));
        binding.btnWhatsapp.setOnClickListener(v -> shareViaPlatform("WhatsApp"));
        binding.btnFacebook.setOnClickListener(v -> shareViaPlatform("Facebook"));
        binding.btnInstagram.setOnClickListener(v -> shareViaPlatform("Instagram"));
    }

    private void shareViaPlatform(String platform) {
        Toast.makeText(requireContext(), "Sharing via " + platform, Toast.LENGTH_SHORT).show();
        // TODO: Implement actual sharing logic
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

