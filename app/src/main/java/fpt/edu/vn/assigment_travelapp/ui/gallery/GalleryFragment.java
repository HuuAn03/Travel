package fpt.edu.vn.assigment_travelapp.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentGalleryBinding;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private GalleryAdapter galleryAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        loadGalleryImages();

        return root;
    }

    private void setupRecyclerView() {
        galleryAdapter = new GalleryAdapter(new ArrayList<>());
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.rvGallery.setLayoutManager(layoutManager);
        binding.rvGallery.setAdapter(galleryAdapter);
    }

    private void loadGalleryImages() {
        // TODO: Load from repository using destinationId
        List<String> images = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            images.add(""); // Placeholder URLs
        }
        galleryAdapter.updateImages(images);
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
        private List<String> images;

        public GalleryAdapter(List<String> images) {
            this.images = images;
        }

        public void updateImages(List<String> newImages) {
            this.images = newImages;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_gallery_image, parent, false);
            return new GalleryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
            // TODO: Load image with Glide
            holder.imageView.setBackgroundResource(R.drawable.image_placeholder);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class GalleryViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView imageView;

            public GalleryViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.iv_gallery_image);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

