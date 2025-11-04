package fpt.edu.vn.assigment_travelapp.ui.booking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.data.model.Place;
import fpt.edu.vn.assigment_travelapp.data.repository.BookingRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.IBookingRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentBookingBinding;

public class BookingFragment extends Fragment {

    private FragmentBookingBinding binding;
    private BookingViewModel viewModel;
    private Place currentPlace;
    private Calendar checkInCalendar = Calendar.getInstance();
    private Calendar checkOutCalendar = Calendar.getInstance();
    private BookingRepository bookingRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        bookingRepository = new BookingRepository();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String placeId = getArguments() != null ? getArguments().getString("placeId") : null;
        if (placeId == null) {
            Toast.makeText(getContext(), "Place ID is required", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        setupDatePickers();
        setupListeners();
        observeViewModel();

        viewModel.loadPlace(placeId);
    }

    private void setupDatePickers() {
        binding.etCheckInDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        checkInCalendar.set(year, month, dayOfMonth);
                        String dateStr = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                        binding.etCheckInDate.setText(dateStr);
                        calculateTotal();
                    },
                    checkInCalendar.get(Calendar.YEAR),
                    checkInCalendar.get(Calendar.MONTH),
                    checkInCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        binding.etCheckOutDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        checkOutCalendar.set(year, month, dayOfMonth);
                        String dateStr = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                        binding.etCheckOutDate.setText(dateStr);
                        calculateTotal();
                    },
                    checkOutCalendar.get(Calendar.YEAR),
                    checkOutCalendar.get(Calendar.MONTH),
                    checkOutCalendar.get(Calendar.DAY_OF_MONTH)
            );
            if (checkInCalendar.getTimeInMillis() > 0) {
                datePickerDialog.getDatePicker().setMinDate(checkInCalendar.getTimeInMillis());
            } else {
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            }
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        binding.etNumberOfGuests.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                calculateTotal();
            }
        });

        binding.btnContinue.setOnClickListener(v -> {
            if (validateInput()) {
                createBooking();
            }
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

    private void observeViewModel() {
        viewModel.getPlace().observe(getViewLifecycleOwner(), place -> {
            if (place != null) {
                currentPlace = place;
                displayPlaceInfo();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPlaceInfo() {
        if (currentPlace == null) return;

        binding.tvPlaceName.setText(currentPlace.getName());
        binding.tvPlaceAddress.setText(currentPlace.getAddress());
        binding.tvPricePerDay.setText(String.format("Price: $%.2f/day", currentPlace.getPricePerDay()));

        if (currentPlace.getImageUrl() != null && !currentPlace.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentPlace.getImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.placeImage);
        }
    }

    private void calculateTotal() {
        if (currentPlace == null || checkInCalendar.getTimeInMillis() == 0 || checkOutCalendar.getTimeInMillis() == 0) {
            binding.tvTotalPrice.setText("Total: $0.00");
            return;
        }

        long diffInMillis = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        if (diffInDays <= 0) {
            binding.tvTotalPrice.setText("Total: $0.00");
            return;
        }

        String guestsStr = binding.etNumberOfGuests.getText().toString();
        int numberOfGuests = guestsStr.isEmpty() ? 1 : Integer.parseInt(guestsStr);
        if (numberOfGuests <= 0) numberOfGuests = 1;

        double totalPrice = currentPlace.getPricePerDay() * diffInDays;
        binding.tvTotalPrice.setText(String.format("Total: $%.2f", totalPrice));
    }

    private boolean validateInput() {
        if (binding.etCheckInDate.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please select check-in date", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.etCheckOutDate.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please select check-out date", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (checkInCalendar.getTimeInMillis() >= checkOutCalendar.getTimeInMillis()) {
            Toast.makeText(getContext(), "Check-out date must be after check-in date", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.etNumberOfGuests.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter number of guests", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.etGuestName.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter guest name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.etGuestPhone.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.etGuestEmail.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter email", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createBooking() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        long diffInMillis = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        int numberOfGuests = Integer.parseInt(binding.etNumberOfGuests.getText().toString());
        double totalPrice = currentPlace.getPricePerDay() * diffInDays;

        Booking booking = new Booking(
                null,
                currentUser.getUid(),
                currentPlace.getPlaceId(),
                currentPlace.getName(),
                currentPlace.getAddress(),
                currentPlace.getImageUrl(),
                checkInCalendar.getTimeInMillis(),
                checkOutCalendar.getTimeInMillis(),
                numberOfGuests,
                totalPrice,
                binding.etGuestName.getText().toString().trim(),
                binding.etGuestPhone.getText().toString().trim(),
                binding.etGuestEmail.getText().toString().trim()
        );

        bookingRepository.createBooking(booking, new IBookingRepository.OnBookingCompleteListener() {
            @Override
            public void onSuccess(Booking createdBooking) {
                Bundle bundle = new Bundle();
                bundle.putString("bookingId", createdBooking.getBookingId());
                NavHostFragment.findNavController(BookingFragment.this)
                        .navigate(R.id.action_bookingFragment_to_paymentFragment, bundle);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to create booking: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

