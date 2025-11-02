package fpt.edu.vn.assigment_travelapp.ui.formschedule;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;
import fpt.edu.vn.assigment_travelapp.data.repository.DestinationRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentFormScheduleBinding;

public class FormScheduleFragment extends Fragment {

    private FragmentFormScheduleBinding binding;
    private Destination destination;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private DestinationRepository destinationRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFormScheduleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        destinationRepository = new DestinationRepository();

        // Get destination ID from arguments
        String destinationId = getArguments() != null ?
                getArguments().getString("destinationId") : null;

        // Load destination data
        loadDestinationData(destinationId);

        setupClickListeners();
        setupDatePicker();

        return root;
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
                updateUI();
            }

            @Override
            public void onFailure(String errorMessage) {
                android.util.Log.e("FormScheduleFragment", "Failed to load destination: " + errorMessage);
                android.widget.Toast.makeText(getContext(), "Failed to load destination", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (destination == null) return;

        binding.tvDestinationName.setText(destination.getName());
        binding.tvLocation.setText(destination.getLocation());
        binding.tvRating.setText(destination.getRating() + " Rating (" + destination.getReviewCount() + " Review)");

        if (destination.getImageUrl() != null && !destination.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(destination.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(binding.ivDestinationThumb);
        }

        // Set default date to today
        binding.etDateVisit.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupDatePicker() {
        binding.etDateVisit.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            binding.etDateVisit.setText(dateFormat.format(calendar.getTime()));
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void setupClickListeners() {
        binding.btnConfirmBooking.setOnClickListener(v -> {
            // Get form data
            String dateVisit = binding.etDateVisit.getText().toString();
            String numberOfPeopleStr = binding.etNumberOfPeople.getText().toString();
            String message = binding.etMessage.getText().toString();

            // Validation
            if (dateVisit.isEmpty()) {
                binding.etDateVisit.setError("Please select a date");
                return;
            }

            if (numberOfPeopleStr.isEmpty()) {
                binding.etNumberOfPeople.setError("Please enter number of people");
                return;
            }

            int numberOfPeople = Integer.parseInt(numberOfPeopleStr);
            if (numberOfPeople <= 0) {
                binding.etNumberOfPeople.setError("Number of people must be greater than 0");
                return;
            }

            // Pass data to checkout fragment (for payment)
            Bundle bundle = new Bundle();
            bundle.putString("destinationId", destination != null ? destination.getDestinationId() : "");
            bundle.putString("dateVisit", dateVisit);
            bundle.putInt("numberOfPeople", numberOfPeople);
            bundle.putString("message", message);

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_formScheduleFragment_to_checkoutFragment, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

