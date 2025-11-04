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
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.data.repository.BookingRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.IBookingRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentBookingDetailsBinding;

public class BookingDetailsFragment extends Fragment {

    private FragmentBookingDetailsBinding binding;
    private Booking currentBooking;
    private BookingRepository bookingRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingDetailsBinding.inflate(inflater, container, false);
        bookingRepository = new BookingRepository();
        return binding.getRoot();
    }

    private String bookingId;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookingId = getArguments() != null ? getArguments().getString("bookingId") : null;
        if (bookingId == null) {
            Toast.makeText(getContext(), "Booking ID is required", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        loadBooking(bookingId);
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload booking data when fragment resumes to get latest status
        if (bookingId != null) {
            loadBooking(bookingId);
        }
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

        String status = currentBooking.getStatus();
        String paymentStatus = currentBooking.getPaymentStatus() != null ? currentBooking.getPaymentStatus() : "pending";
        String statusText = "Status: " + status.substring(0, 1).toUpperCase() + status.substring(1);
        binding.tvBookingStatus.setText(statusText);

        // Show/hide buttons based on status and payment status
        binding.btnPayNow.setVisibility(View.GONE);
        binding.btnCheckIn.setVisibility(View.GONE);
        binding.btnCheckOut.setVisibility(View.GONE);

        if ("pending".equals(status) && "pending".equals(paymentStatus)) {
            // Show Pay Now button when booking is pending and payment is pending
            binding.btnPayNow.setVisibility(View.VISIBLE);
        } else if ("confirmed".equals(status) || ("pending".equals(status) && "paid".equals(paymentStatus))) {
            // Show Check In button when booking is confirmed or paid but pending
            binding.btnCheckIn.setVisibility(View.VISIBLE);
        } else if ("checked_in".equals(status)) {
            // Show Check Out button when checked in
            binding.btnCheckOut.setVisibility(View.VISIBLE);
        }

        if (currentBooking.getPlaceImageUrl() != null && !currentBooking.getPlaceImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentBooking.getPlaceImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.placeImage);
        }
    }

    private void setupListeners() {
        binding.btnPayNow.setOnClickListener(v -> {
            if (currentBooking == null) return;
            
            // Navigate to PaymentFragment
            Bundle bundle = new Bundle();
            bundle.putString("bookingId", currentBooking.getBookingId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_bookingDetailsFragment_to_paymentFragment, bundle);
        });

        binding.btnCheckIn.setOnClickListener(v -> {
            if (currentBooking == null) return;
            
            bookingRepository.checkIn(currentBooking.getBookingId(), new IBookingRepository.OnBookingCompleteListener() {
                @Override
                public void onSuccess(Booking booking) {
                    currentBooking = booking;
                    displayBookingInfo();
                    Toast.makeText(getContext(), "Checked in successfully!", Toast.LENGTH_SHORT).show();
                    // Reload to get updated status
                    if (bookingId != null) {
                        loadBooking(bookingId);
                    }
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Check-in failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnCheckOut.setOnClickListener(v -> {
            if (currentBooking == null) return;
            
            bookingRepository.checkOut(currentBooking.getBookingId(), new IBookingRepository.OnBookingCompleteListener() {
                @Override
                public void onSuccess(Booking booking) {
                    currentBooking = booking;
                    displayBookingInfo();
                    Toast.makeText(getContext(), "Checked out successfully!", Toast.LENGTH_SHORT).show();
                    // Reload to get updated status
                    if (bookingId != null) {
                        loadBooking(bookingId);
                    }
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Check-out failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnHome.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            // Clear back stack up to and including navigation_home, then navigate to home
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_home, true)
                    .build();
            navController.navigate(R.id.navigation_home, null, navOptions);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

