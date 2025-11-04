package fpt.edu.vn.assigment_travelapp.ui.booking;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.BookingAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentMyBookingsBinding;

public class MyBookingsFragment extends Fragment {

    private FragmentMyBookingsBinding binding;
    private MyBookingsViewModel viewModel;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList = new ArrayList<>();
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyBookingsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MyBookingsViewModel.class);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        observeViewModel();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentUser != null) {
                viewModel.loadUserBookings(currentUser.getUid());
            }
        });

        if (currentUser != null) {
            viewModel.loadUserBookings(currentUser.getUid());
        } else {
            Toast.makeText(getContext(), "Please login to view bookings", Toast.LENGTH_SHORT).show();
        }

        binding.btnHome.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            // Clear back stack up to and including navigation_home, then navigate to home
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_home, true)
                    .build();
            navController.navigate(R.id.navigation_home, null, navOptions);
        });
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(bookingList);
        bookingAdapter.setOnBookingClickListener(booking -> {
            Bundle bundle = new Bundle();
            bundle.putString("bookingId", booking.getBookingId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_myBookingsFragment_to_bookingDetailsFragment, bundle);
        });
        binding.recyclerViewBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewBookings.setAdapter(bookingAdapter);
    }

    private void observeViewModel() {
        viewModel.getBookings().observe(getViewLifecycleOwner(), bookings -> {
            bookingList.clear();
            if (bookings != null && !bookings.isEmpty()) {
                bookingList.addAll(bookings);
                binding.tvEmpty.setVisibility(View.GONE);
                binding.recyclerViewBookings.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.recyclerViewBookings.setVisibility(View.GONE);
            }
            bookingAdapter.notifyDataSetChanged();
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.recyclerViewBookings.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerViewBookings.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

