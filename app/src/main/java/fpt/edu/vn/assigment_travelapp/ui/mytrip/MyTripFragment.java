package fpt.edu.vn.assigment_travelapp.ui.mytrip;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.BookingAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.data.model.Destination;
import fpt.edu.vn.assigment_travelapp.data.repository.BookingRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.DestinationRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentMyTripBinding;

public class MyTripFragment extends Fragment {

    private FragmentMyTripBinding binding;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookings = new ArrayList<>();
    private List<Destination> destinations = new ArrayList<>();
    private FirebaseUser currentUser;
    private BookingRepository bookingRepository;
    private DestinationRepository destinationRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyTripBinding.inflate(inflater, container, false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        bookingRepository = new BookingRepository();
        destinationRepository = new DestinationRepository();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadBookings();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadBookings();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload bookings when returning to this fragment (e.g., after payment completed)
        loadBookings();
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(bookings, destinations, new BookingAdapter.OnBookingClickListener() {
            @Override
            public void onBookingClick(Booking booking, Destination destination) {
                // Navigate to booking detail
                Bundle bundle = new Bundle();
                bundle.putString("bookingId", booking.getBookingId());
                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_navigation_my_trip_to_bookingDetailFragment, bundle);
            }
        });

        binding.rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBookings.setAdapter(bookingAdapter);
    }

    private void loadBookings() {
        if (currentUser == null) {
            // No user logged in, show empty state
            bookings.clear();
            destinations.clear();
            bookingAdapter.updateBookings(bookings, destinations);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvBookings.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);
            return;
        }

        // Load bookings from Firebase for current user
        bookingRepository.getUserBookings(currentUser.getUid(), new BookingRepository.OnGetBookingsCompleteListener() {
            @Override
            public void onSuccess(List<Booking> fetchedBookings) {
                bookings.clear();
                bookings.addAll(fetchedBookings);

                // Load destinations for all bookings
                loadDestinationsForBookings(bookings);
            }

            @Override
            public void onFailure(String errorMessage) {
                android.widget.Toast.makeText(getContext(), 
                        "Failed to load bookings: " + errorMessage, 
                        android.widget.Toast.LENGTH_SHORT).show();
                
                // Show empty state on error
                bookings.clear();
                destinations.clear();
                bookingAdapter.updateBookings(bookings, destinations);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.rvBookings.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadDestinationsForBookings(List<Booking> bookings) {
        destinations.clear();
        
        if (bookings.isEmpty()) {
            bookingAdapter.updateBookings(bookings, destinations);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvBookings.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);
            return;
        }

        // Load all unique destinations
        java.util.Set<String> destinationIds = new java.util.HashSet<>();
        for (Booking booking : bookings) {
            if (booking.getDestinationId() != null && !booking.getDestinationId().isEmpty()) {
                destinationIds.add(booking.getDestinationId());
            }
        }

        if (destinationIds.isEmpty()) {
            bookingAdapter.updateBookings(bookings, destinations);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvBookings.setVisibility(View.VISIBLE);
            binding.swipeRefreshLayout.setRefreshing(false);
            return;
        }

        // Load each destination
        int[] loadedCount = {0};
        int totalCount = destinationIds.size();

        for (String destinationId : destinationIds) {
            destinationRepository.getDestinationById(destinationId, new DestinationRepository.OnGetDestinationCompleteListener() {
                @Override
                public void onSuccess(Destination fetchedDestination) {
                    if (fetchedDestination != null && !destinations.contains(fetchedDestination)) {
                        destinations.add(fetchedDestination);
                    }
                    loadedCount[0]++;
                    if (loadedCount[0] == totalCount) {
                        // All destinations loaded, update adapter
                        bookingAdapter.updateBookings(bookings, destinations);
                        
                        // Show/hide empty state
                        if (bookings.isEmpty()) {
                            binding.layoutEmptyState.setVisibility(View.VISIBLE);
                            binding.rvBookings.setVisibility(View.GONE);
                        } else {
                            binding.layoutEmptyState.setVisibility(View.GONE);
                            binding.rvBookings.setVisibility(View.VISIBLE);
                        }

                        binding.swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    android.util.Log.e("MyTripFragment", "Failed to load destination " + destinationId + ": " + errorMessage);
                    loadedCount[0]++;
                    if (loadedCount[0] == totalCount) {
                        // Continue even if some destinations failed to load
                        bookingAdapter.updateBookings(bookings, destinations);
                        binding.layoutEmptyState.setVisibility(View.GONE);
                        binding.rvBookings.setVisibility(View.VISIBLE);
                        binding.swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
