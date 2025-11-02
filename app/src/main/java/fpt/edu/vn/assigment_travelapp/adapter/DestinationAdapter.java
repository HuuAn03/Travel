package fpt.edu.vn.assigment_travelapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder> {

    private List<Destination> destinations;
    private OnDestinationClickListener listener;

    public interface OnDestinationClickListener {
        void onDestinationClick(Destination destination);
        void onFavoriteClick(Destination destination);
        void onActionClick(Destination destination);
    }

    public DestinationAdapter(List<Destination> destinations, OnDestinationClickListener listener) {
        this.destinations = destinations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destination, parent, false);
        return new DestinationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DestinationViewHolder holder, int position) {
        Destination destination = destinations.get(position);
        holder.bind(destination);
    }

    @Override
    public int getItemCount() {
        return destinations != null ? destinations.size() : 0;
    }

    public void updateDestinations(List<Destination> newDestinations) {
        this.destinations = newDestinations;
        notifyDataSetChanged();
    }

    class DestinationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivDestinationImage;
        private TextView tvLocation;
        private TextView tvRating;
        private ImageView ivFavorite;
        private TextView tvDestinationName;
        private TextView tvPrice;
        private View fabAction;

        public DestinationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDestinationImage = itemView.findViewById(R.id.iv_destination_image);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvRating = itemView.findViewById(R.id.tv_rating);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            tvDestinationName = itemView.findViewById(R.id.tv_destination_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            fabAction = itemView.findViewById(R.id.fab_action);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDestinationClick(destinations.get(getAdapterPosition()));
                }
            });

            ivFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(destinations.get(getAdapterPosition()));
                }
            });

            fabAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActionClick(destinations.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Destination destination) {
            tvDestinationName.setText(destination.getName());
            tvLocation.setText(destination.getLocation());
            tvRating.setText(String.valueOf(destination.getRating()));
            tvPrice.setText("$" + String.format("%.2f", destination.getPricePerPerson()) + "/Person");

            if (destination.getImageUrl() != null && !destination.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(destination.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(ivDestinationImage);
            } else {
                ivDestinationImage.setImageResource(R.drawable.image_placeholder);
            }
        }
    }
}

