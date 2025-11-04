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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final List<Booking> bookingList;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
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
        Booking booking = bookingList.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameTextView;
        private final TextView addressTextView;
        private final TextView checkInTextView;
        private final TextView checkOutTextView;
        private final TextView priceTextView;
        private final TextView statusTextView;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.booking_image);
            nameTextView = itemView.findViewById(R.id.booking_place_name);
            addressTextView = itemView.findViewById(R.id.booking_address);
            checkInTextView = itemView.findViewById(R.id.booking_checkin);
            checkOutTextView = itemView.findViewById(R.id.booking_checkout);
            priceTextView = itemView.findViewById(R.id.booking_price);
            statusTextView = itemView.findViewById(R.id.booking_status);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookingClick(bookingList.get(position));
                }
            });
        }

        void bind(Booking booking) {
            nameTextView.setText(booking.getPlaceName());
            addressTextView.setText(booking.getPlaceAddress());
            priceTextView.setText(String.format("$%.2f", booking.getTotalPrice()));
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            checkInTextView.setText("Check-in: " + sdf.format(new Date(booking.getCheckInDate())));
            checkOutTextView.setText("Check-out: " + sdf.format(new Date(booking.getCheckOutDate())));
            
            String status = booking.getStatus();
            statusTextView.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            
            // Set status color
            int statusColor = R.color.black;
            switch (status) {
                case "confirmed":
                    statusColor = R.color.green_brand;
                    break;
                case "checked_in":
                    statusColor = android.R.color.holo_blue_dark;
                    break;
                case "checked_out":
                    statusColor = android.R.color.darker_gray;
                    break;
                case "cancelled":
                    statusColor = android.R.color.holo_red_dark;
                    break;
            }
            statusTextView.setTextColor(itemView.getContext().getResources().getColor(statusColor));

            if (booking.getPlaceImageUrl() != null && !booking.getPlaceImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(booking.getPlaceImageUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_profile);
            }
        }
    }
}

