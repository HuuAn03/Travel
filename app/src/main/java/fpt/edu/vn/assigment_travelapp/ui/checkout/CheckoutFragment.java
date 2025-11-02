package fpt.edu.vn.assigment_travelapp.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;
import fpt.edu.vn.assigment_travelapp.data.repository.BookingRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.DestinationRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentCheckoutBinding;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private Destination destination;
    private int numberOfPeople;
    private String dateVisit;
    private double pricePerPerson;
    private BookingRepository bookingRepository;
    private DestinationRepository destinationRepository;
    private static final double APP_FEE_RATE = 0.01; // 1% app fee
    private static final double MIN_APP_FEE = 2.50;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        bookingRepository = new BookingRepository();
        destinationRepository = new DestinationRepository();

        // Get data from arguments
        Bundle args = getArguments();
        if (args != null) {
            String destinationId = args.getString("destinationId");
            numberOfPeople = args.getInt("numberOfPeople", 1);
            dateVisit = args.getString("dateVisit", "");

            // Load destination data
            loadDestinationData(destinationId);
        }

        setupClickListeners();
        calculateAndDisplayPrice();

        return root;
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        binding.cardPaymentMethod.setOnClickListener(v -> {
            // Navigate to payment method selection
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_checkoutFragment_to_paymentMethodFragment);
        });

        binding.btnPayNow.setOnClickListener(v -> {
            // Save booking first, then navigate to payment success
            saveBooking();
        });
    }

    private void loadDestinationData(String destinationId) {
        if (destinationId == null || destinationId.isEmpty()) {
            android.widget.Toast.makeText(getContext(), "Invalid destination ID", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        destinationRepository.getDestinationById(destinationId, new DestinationRepository.OnGetDestinationCompleteListener() {
            @Override
            public void onSuccess(Destination fetchedDestination) {
                destination = fetchedDestination;
                pricePerPerson = destination.getPricePerPerson();
                updateUI();
            }

            @Override
            public void onFailure(String errorMessage) {
                android.util.Log.e("CheckoutFragment", "Failed to load destination: " + errorMessage);
                android.widget.Toast.makeText(getContext(), "Failed to load destination", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (destination == null) return;

        binding.tvDestinationName.setText(destination.getName());
        binding.tvLocation.setText(destination.getLocation());
        binding.tvRating.setText(destination.getRating() + " Rating (" + destination.getReviewCount() + " Review)");
        binding.tvNumberOfPeople.setText(String.valueOf(numberOfPeople));
        binding.tvDates.setText(dateVisit);

        if (destination.getImageUrl() != null && !destination.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(destination.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(binding.ivDestinationImage);
        }
    }

    private void calculateAndDisplayPrice() {
        double basePrice = pricePerPerson * numberOfPeople;
        double appFee = Math.max(basePrice * APP_FEE_RATE, MIN_APP_FEE);
        double totalPrice = basePrice + appFee;

        binding.tvPrice.setText("$" + String.format("%.2f", basePrice));
        binding.tvAppsFee.setText("$" + String.format("%.2f", appFee));
        binding.tvTotalPrice.setText("$" + String.format("%.2f", totalPrice));
    }

    private void saveBooking() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || destination == null) {
            android.widget.Toast.makeText(getContext(), "Please login to complete booking", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse date from string (format: "dd MMM, yyyy")
        Date checkInDate = parseDate(dateVisit);
        if (checkInDate == null) {
            android.widget.Toast.makeText(getContext(), "Invalid date format", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate checkout date (for now, same as check-in, but could be based on destination type)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(checkInDate);
        calendar.add(Calendar.DAY_OF_MONTH, 4); // Default 4 days stay
        Date checkOutDate = calendar.getTime();

        // Calculate total price
        double basePrice = pricePerPerson * numberOfPeople;
        double appFee = Math.max(basePrice * APP_FEE_RATE, MIN_APP_FEE);
        double totalPrice = basePrice + appFee;

        // Create booking
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(
                bookingId,
                destination.getDestinationId(),
                currentUser.getUid(),
                checkInDate,
                checkOutDate,
                numberOfPeople,
                getArguments() != null ? getArguments().getString("message", "") : "",
                totalPrice,
                "paymentMethodId123", // TODO: Get from selected payment method
                "Confirmed",
                System.currentTimeMillis()
        );

        // Save booking to Firebase
        try {
            bookingRepository.saveBooking(booking)
                    .addOnSuccessListener(aVoid -> {
                        // Booking saved successfully, navigate to payment completed
                        Bundle bundle = new Bundle();
                        bundle.putString("bookingId", bookingId);
                        bundle.putDouble("totalPrice", totalPrice);
                        Navigation.findNavController(binding.getRoot())
                                .navigate(R.id.action_checkoutFragment_to_paymentCompletedFragment, bundle);
                    })
                    .addOnFailureListener(e -> {
                        // Log error for debugging
                        android.util.Log.e("CheckoutFragment", "Failed to save booking: ", e);
                        // Show error message but don't crash
                        if (getContext() != null && isAdded()) {
                            android.widget.Toast.makeText(getContext(), 
                                    "Failed to save booking. Please try again.", 
                                    android.widget.Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            // Catch any unexpected exceptions
            android.util.Log.e("CheckoutFragment", "Unexpected error saving booking: ", e);
            if (getContext() != null && isAdded()) {
                android.widget.Toast.makeText(getContext(), 
                        "An error occurred. Please try again.", 
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Date parseDate(String dateString) {
        try {
            // Try to parse "dd MMM, yyyy" format
            SimpleDateFormat format = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            return format.parse(dateString);
        } catch (Exception e) {
            // If parsing fails, try other formats or return null
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

