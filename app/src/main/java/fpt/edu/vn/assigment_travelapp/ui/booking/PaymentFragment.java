package fpt.edu.vn.assigment_travelapp.ui.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.data.repository.BookingRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.IBookingRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentPaymentBinding;

public class PaymentFragment extends Fragment {

    private FragmentPaymentBinding binding;
    private Booking currentBooking;
    private BookingRepository bookingRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPaymentBinding.inflate(inflater, container, false);
        bookingRepository = new BookingRepository();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String bookingId = getArguments() != null ? getArguments().getString("bookingId") : null;
        if (bookingId == null) {
            Toast.makeText(getContext(), "Booking ID is required", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        loadBooking(bookingId);
        setupListeners();
    }

    private void loadBooking(String bookingId) {
        bookingRepository.getBookingById(bookingId, new IBookingRepository.OnBookingFetchedListener() {
            @Override
            public void onSuccess(Booking booking) {
                currentBooking = booking;
                displayBookingInfo();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to load booking: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBookingInfo() {
        if (currentBooking == null) return;

        binding.tvPlaceName.setText(currentBooking.getPlaceName());
        binding.tvPlaceAddress.setText(currentBooking.getPlaceAddress());
        binding.tvTotalAmount.setText(String.format("$%.2f", currentBooking.getTotalPrice()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.tvCheckIn.setText(sdf.format(new Date(currentBooking.getCheckInDate())));
        binding.tvCheckOut.setText(sdf.format(new Date(currentBooking.getCheckOutDate())));
        binding.tvNumberOfGuests.setText(String.valueOf(currentBooking.getNumberOfGuests()));

        if (currentBooking.getPlaceImageUrl() != null && !currentBooking.getPlaceImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentBooking.getPlaceImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.placeImage);
        }
    }

    private void setupListeners() {
        binding.btnPayNow.setOnClickListener(v -> {
            if (currentBooking == null) {
                Toast.makeText(getContext(), "Booking information not loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            // Process payment - simplified version
            processPayment();
        });

        binding.btnHome.setOnClickListener(v -> {
            androidx.navigation.NavController navController = NavHostFragment.findNavController(this);
            // Clear back stack up to and including navigation_home, then navigate to home
            androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_home, true)
                    .build();
            navController.navigate(R.id.navigation_home, null, navOptions);
        });
    }

    private void processPayment() {
        // In a real app, this would integrate with a payment gateway
        // For now, we just update the payment status
        bookingRepository.updatePaymentStatus(currentBooking.getBookingId(), "paid", 
            new IBookingRepository.OnBookingCompleteListener() {
                @Override
                public void onSuccess(Booking booking) {
                    Toast.makeText(getContext(), "Payment successful!", Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("bookingId", booking.getBookingId());
                    NavHostFragment.findNavController(PaymentFragment.this)
                            .navigate(R.id.action_paymentFragment_to_bookingDetailsFragment, bundle);
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Payment failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

