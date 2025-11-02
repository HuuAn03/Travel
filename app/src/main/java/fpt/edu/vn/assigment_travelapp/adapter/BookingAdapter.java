package fpt.edu.vn.assigment_travelapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings;
    private List<Destination> destinations;
    private OnBookingClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking, Destination destination);
    }

    public BookingAdapter(List<Booking> bookings, List<Destination> destinations, OnBookingClickListener listener) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        this.destinations = destinations != null ? destinations : new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        Destination destination = findDestinationById(booking.getDestinationId());
        holder.bind(booking, destination);
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    public void updateBookings(List<Booking> newBookings, List<Destination> newDestinations) {
        this.bookings = newBookings != null ? newBookings : new ArrayList<>();
        this.destinations = newDestinations != null ? newDestinations : new ArrayList<>();
        notifyDataSetChanged();
    }

    private Destination findDestinationById(String destinationId) {
        for (Destination dest : destinations) {
            if (dest.getDestinationId().equals(destinationId)) {
                return dest;
            }
        }
        return null;
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivDestinationImage;
        private TextView tvDestinationName;
        private TextView tvLocation;
        private TextView tvBookingDate;
        private TextView tvNumberOfPeople;
        private TextView tvStatus;
        private TextView tvTotalPrice;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDestinationImage = itemView.findViewById(R.id.iv_destination_image);
            tvDestinationName = itemView.findViewById(R.id.tv_destination_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvBookingDate = itemView.findViewById(R.id.tv_booking_date);
            tvNumberOfPeople = itemView.findViewById(R.id.tv_number_of_people);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Booking booking = bookings.get(position);
                        Destination destination = findDestinationById(booking.getDestinationId());
                        listener.onBookingClick(booking, destination);
                    }
                }
            });
        }

        public void bind(Booking booking, Destination destination) {
            if (destination != null) {
                tvDestinationName.setText(destination.getName());
                tvLocation.setText(destination.getLocation());
                
                if (destination.getImageUrl() != null && !destination.getImageUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(destination.getImageUrl())
                            .placeholder(R.drawable.image_placeholder)
                            .into(ivDestinationImage);
                } else {
                    ivDestinationImage.setImageResource(R.drawable.image_placeholder);
                }
            } else {
                tvDestinationName.setText("Unknown Destination");
                tvLocation.setText("");
            }

            // Format dates
            if (booking.getCheckInDate() != null) {
                String dateRange = dateFormat.format(booking.getCheckInDate());
                if (booking.getCheckOutDate() != null) {
                    dateRange += " - " + dateFormat.format(booking.getCheckOutDate());
                }
                tvBookingDate.setText(dateRange);
            }

            tvNumberOfPeople.setText(booking.getNumberOfPeople() + " Person(s)");
            tvTotalPrice.setText("$" + String.format("%.2f", booking.getTotalPrice()));

            // Set status with color
            String status = booking.getStatus();
            tvStatus.setText(status);
            switch (status) {
                case "Confirmed":
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.green_brand));
                    break;
                case "Pending":
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                    break;
                case "Completed":
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                    break;
                case "Cancelled":
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    break;
                default:
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.black));
                    break;
            }
        }
    }
}

