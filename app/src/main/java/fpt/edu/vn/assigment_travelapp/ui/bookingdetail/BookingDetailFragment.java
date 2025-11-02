package fpt.edu.vn.assigment_travelapp.ui.bookingdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;
import fpt.edu.vn.assigment_travelapp.data.repository.BookingRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.DestinationRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentBookingDetailBinding;

public class BookingDetailFragment extends Fragment {

    private FragmentBookingDetailBinding binding;
    private Booking booking;
    private Destination destination;
    private SimpleDateFormat dateFormat;
    private BookingRepository bookingRepository;
    private DestinationRepository destinationRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookingDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        bookingRepository = new BookingRepository();
        destinationRepository = new DestinationRepository();

        // Get booking ID from arguments
        String bookingId = getArguments() != null ?
                getArguments().getString("bookingId") : null;

        // Load booking data
        loadBookingData(bookingId);

        setupClickListeners();
        updateActionButtons();

        return root;
    }

    private void loadBookingData(String bookingId) {
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid booking ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load booking from Firebase
        bookingRepository.getBookingById(bookingId, new BookingRepository.OnGetBookingCompleteListener() {
            @Override
            public void onSuccess(Booking fetchedBooking) {
                booking = fetchedBooking;
                // Load destination info from repository
                loadDestinationInfo(booking.getDestinationId());
            }

            @Override
            public void onFailure(String errorMessage) {
                android.util.Log.e("BookingDetailFragment", "Failed to load booking: " + errorMessage);
                Toast.makeText(getContext(), "Failed to load booking details", Toast.LENGTH_SHORT).show();
                // Fallback to sample data if load fails
                loadSampleBookingData(bookingId);
            }
        });
    }

    private void loadDestinationInfo(String destinationId) {
        if (destinationId == null || destinationId.isEmpty()) {
            updateUI();
            updateActionButtons();
            return;
        }

        destinationRepository.getDestinationById(destinationId, new DestinationRepository.OnGetDestinationCompleteListener() {
            @Override
            public void onSuccess(Destination fetchedDestination) {
                destination = fetchedDestination;
                updateUI();
                updateActionButtons();
            }

            @Override
            public void onFailure(String errorMessage) {
                android.util.Log.e("BookingDetailFragment", "Failed to load destination: " + errorMessage);
                // Continue with null destination - UI will handle it
                updateUI();
                updateActionButtons();
            }
        });
    }

    private void loadSampleBookingData(String bookingId) {
        // Fallback sample data
        Calendar calendar = Calendar.getInstance();
        Date checkIn = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 4);
        Date checkOut = calendar.getTime();

        booking = new Booking(
                bookingId,
                "3",
                "userId123",
                checkIn,
                checkOut,
                5,
                "I would like to ask for help to provide 1 wheelchair if possible",
                325.00,
                "paymentMethodId123",
                "Confirmed",
                System.currentTimeMillis()
        );

        loadDestinationInfo("3");
        updateUI();
        updateActionButtons();
    }

    private void updateUI() {
        if (booking == null || destination == null) return;

        binding.tvBookingId.setText("#" + booking.getBookingId());
        binding.tvBookingStatus.setText(booking.getStatus());
        binding.tvDestinationName.setText(destination.getName());
        binding.tvLocation.setText(destination.getLocation());
        binding.tvTotalPrice.setText("$" + String.format("%.2f", booking.getTotalPrice()));
        binding.tvNumberOfPeople.setText(String.valueOf(booking.getNumberOfPeople()));
        binding.tvMessage.setText(booking.getMessage());

        if (booking.getCheckInDate() != null) {
            binding.tvCheckinDate.setText(dateFormat.format(booking.getCheckInDate()));
        }
        if (booking.getCheckOutDate() != null) {
            binding.tvCheckoutDate.setText(dateFormat.format(booking.getCheckOutDate()));
        }

        if (destination.getImageUrl() != null && !destination.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(destination.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(binding.ivDestinationImage);
        }

        // Set status color
        String status = booking.getStatus();
        switch (status) {
            case "Confirmed":
                binding.tvBookingStatus.setTextColor(getResources().getColor(R.color.green_brand));
                break;
            case "Checked In":
                binding.tvBookingStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "Pending":
                binding.tvBookingStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "Completed":
                binding.tvBookingStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                break;
            case "Cancelled":
                binding.tvBookingStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                binding.tvBookingStatus.setTextColor(getResources().getColor(android.R.color.black));
                break;
        }
    }

    private void updateActionButtons() {
        if (booking == null) {
            binding.btnCheckIn.setVisibility(View.GONE);
            binding.btnCheckOut.setVisibility(View.GONE);
            return;
        }

        Date now = new Date();
        Date checkIn = booking.getCheckInDate();
        Date checkOut = booking.getCheckOutDate();
        String status = booking.getStatus();

        // Reset button visibility
        binding.btnCheckIn.setVisibility(View.GONE);
        binding.btnCheckOut.setVisibility(View.GONE);

        // Only show buttons for Confirmed or Checked In status (not Completed or Cancelled)
        if ("Confirmed".equals(status) || "Checked In".equals(status)) {
            
            // Check if current time is within check-in and check-out period
            boolean canCheckIn = false;
            boolean canCheckOut = false;
            
            if (checkIn != null && checkOut != null) {
                // Allow check-in from the check-in date onwards
                // Allow check-out anytime after check-in (even early checkout)
                boolean isAfterCheckIn = now.equals(checkIn) || now.after(checkIn);
                boolean isBeforeCheckOut = now.before(checkOut) || now.equals(checkOut);
                
                if (isAfterCheckIn && isBeforeCheckOut) {
                    // Within booking period
                    if ("Confirmed".equals(status)) {
                        canCheckIn = true;
                    } else if ("Checked In".equals(status)) {
                        canCheckOut = true;
                    }
                } else if (isAfterCheckIn && now.after(checkOut)) {
                    // Check-out date has passed
                    if ("Checked In".equals(status)) {
                        canCheckOut = true; // Still allow checkout if checked in
                    } else {
                        // Auto-complete if checkout date has passed and not checked in
                        booking.setStatus("Completed");
                        binding.tvBookingStatus.setText("Completed");
                        binding.tvBookingStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                }
            }

            // Show appropriate button(s)
            if (canCheckIn && !canCheckOut) {
                // Only check-in button
                binding.btnCheckIn.setVisibility(View.VISIBLE);
            } else if (canCheckOut && !canCheckIn) {
                // Only check-out button
                binding.btnCheckOut.setVisibility(View.VISIBLE);
            } else if (canCheckIn && canCheckOut) {
                // Both buttons (shouldn't happen, but just in case)
                binding.btnCheckIn.setVisibility(View.VISIBLE);
                binding.btnCheckOut.setVisibility(View.VISIBLE);
            }
        } else if ("Completed".equals(status) || "Cancelled".equals(status)) {
            // No buttons for completed or cancelled bookings
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        binding.btnCheckIn.setOnClickListener(v -> {
            performCheckIn();
        });

        binding.btnCheckOut.setOnClickListener(v -> {
            performCheckOut();
        });
    }

    private void performCheckIn() {
        if (booking == null) return;

        // Validate check-in date
        Date now = new Date();
        Date checkIn = booking.getCheckInDate();
        
        if (checkIn != null && now.before(checkIn)) {
            Toast.makeText(getContext(), "Check-in date has not arrived yet", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update booking status to "Checked In" in Firebase
        bookingRepository.updateBookingStatus(booking.getBookingId(), "Checked In")
                .addOnSuccessListener(aVoid -> {
                    // Update local object and UI only after successful save
                    booking.setStatus("Checked In");
                    binding.tvBookingStatus.setText("Checked In");
                    binding.tvBookingStatus.setTextColor(getResources().getColor(R.color.green_brand));
                    
                    // Hide Check In button, show Check Out button
                    binding.btnCheckIn.setVisibility(View.GONE);
                    binding.btnCheckOut.setVisibility(View.VISIBLE);
                    
                    Toast.makeText(getContext(), "Check-in successful! Enjoy your trip!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Show error if save failed
                    android.util.Log.e("BookingDetailFragment", "Failed to update check-in status: ", e);
                    Toast.makeText(getContext(), 
                            "Failed to save check-in. Please try again.", 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void performCheckOut() {
        if (booking == null) return;

        // Validate that user has checked in first
        if (!"Checked In".equals(booking.getStatus())) {
            Toast.makeText(getContext(), "Please check in first before checking out", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate checkout date (should allow early checkout)
        Date now = new Date();
        Date checkOut = booking.getCheckOutDate();
        
        if (checkOut != null && now.before(checkOut)) {
            // Allow early checkout with confirmation
            // In production, you might want to show a confirmation dialog
        }

        // Update booking status to "Completed" in Firebase
        bookingRepository.updateBookingStatus(booking.getBookingId(), "Completed")
                .addOnSuccessListener(aVoid -> {
                    // Update local object and UI only after successful save
                    booking.setStatus("Completed");
                    binding.tvBookingStatus.setText("Completed");
                    binding.tvBookingStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    
                    // Hide both buttons
                    binding.btnCheckIn.setVisibility(View.GONE);
                    binding.btnCheckOut.setVisibility(View.GONE);
                    
                    Toast.makeText(getContext(), 
                            "Check-out successful! Thank you for your visit. We hope to see you again!", 
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    // Show error if save failed
                    android.util.Log.e("BookingDetailFragment", "Failed to update check-out status: ", e);
                    Toast.makeText(getContext(), 
                            "Failed to save check-out. Please try again.", 
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload booking data to get latest status from Firebase
        String bookingId = getArguments() != null ? getArguments().getString("bookingId") : null;
        if (bookingId != null && !bookingId.isEmpty()) {
            loadBookingData(bookingId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

