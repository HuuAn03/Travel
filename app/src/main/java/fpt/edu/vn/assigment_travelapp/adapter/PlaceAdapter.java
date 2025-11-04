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
import fpt.edu.vn.assigment_travelapp.data.model.Place;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private final List<Place> placeList;
    private OnPlaceClickListener listener;

    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
    }

    public void setOnPlaceClickListener(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    public PlaceAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameTextView;
        private final TextView addressTextView;
        private final TextView priceTextView;
        private final TextView ratingTextView;
        private final TextView categoryTextView;

        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.place_image);
            nameTextView = itemView.findViewById(R.id.place_name);
            addressTextView = itemView.findViewById(R.id.place_address);
            priceTextView = itemView.findViewById(R.id.place_price);
            ratingTextView = itemView.findViewById(R.id.place_rating);
            categoryTextView = itemView.findViewById(R.id.place_category);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPlaceClick(placeList.get(position));
                }
            });
        }

        void bind(Place place) {
            nameTextView.setText(place.getName());
            addressTextView.setText(place.getAddress());
            priceTextView.setText(String.format("$%.2f/day", place.getPricePerDay()));
            ratingTextView.setText(String.valueOf(place.getRating()));
            categoryTextView.setText(place.getCategory());

            if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(place.getImageUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_profile);
            }
        }
    }
}

