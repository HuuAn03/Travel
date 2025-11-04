package fpt.edu.vn.assigment_travelapp.ui.booking;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentBookingDetailBinding;

public class BookingDetailFragment extends Fragment {

    private FragmentBookingDetailBinding binding;

    public BookingDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookingDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            BookingDetailFragmentArgs args = BookingDetailFragmentArgs.fromBundle(getArguments());
            Booking booking = args.getBooking();
            if (booking != null) {
                displayBookingDetails(booking);
            }
        }
    }

    private void displayBookingDetails(Booking booking) {
        // Place details
        Glide.with(this).load(booking.getPlaceImageUrl()).into(binding.imgPlace);
        binding.tvPlaceName.setText(booking.getPlaceName());
        binding.tvPlaceAddress.setText(booking.getPlaceAddress());

        // Date formatting
        SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy", Locale.getDefault());

        // Booking dates
        binding.tvCheckInDate.setText(sdf.format(new Date(booking.getCheckInDate())));
        binding.tvCheckOutDate.setText(sdf.format(new Date(booking.getCheckOutDate())));

        // Guest and status
        binding.tvNumberOfGuests.setText(String.valueOf(booking.getNumberOfGuests()));

        binding.tvBookingStatus.setText(booking.getStatus());
        if ("confirmed".equalsIgnoreCase(booking.getStatus())) {
            binding.tvBookingStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if ("cancelled".equalsIgnoreCase(booking.getStatus())) {
            binding.tvBookingStatus.setTextColor(Color.parseColor("#F44336")); // Red
        } else {
            binding.tvBookingStatus.setTextColor(Color.parseColor("#FFC107")); // Amber
        }

        // Guest details
        binding.tvGuestName.setText(booking.getGuestName());
        binding.tvGuestEmail.setText(booking.getGuestEmail());
        binding.tvGuestPhone.setText(booking.getGuestPhone());

        // Payment details
        binding.tvPaymentStatus.setText(booking.getPaymentStatus());
         if ("paid".equalsIgnoreCase(booking.getPaymentStatus())) {
            binding.tvPaymentStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            binding.tvPaymentStatus.setTextColor(Color.parseColor("#F44336")); // Red
        }

        binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "%,d VND", (int) booking.getTotalPrice()));

        // Booking ID and date
        binding.tvBookingId.setText(booking.getBookingId());
        binding.tvBookingCreatedAt.setText(sdf.format(new Date(booking.getCreatedAt())));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
