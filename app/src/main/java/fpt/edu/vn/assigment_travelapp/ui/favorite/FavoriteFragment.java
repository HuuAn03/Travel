package fpt.edu.vn.assigment_travelapp.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.BookingAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentFavoriteBinding;
import fpt.edu.vn.assigment_travelapp.ui.profile.ProfileViewModel;

public class FavoriteFragment extends Fragment implements BookingAdapter.OnBookingClickListener {

    private FragmentFavoriteBinding binding;
    private ProfileViewModel profileViewModel;
    private BookingAdapter bookingAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookingAdapter = new BookingAdapter(new ArrayList<>());
        bookingAdapter.setOnBookingClickListener(this);
        binding.recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFavorites.setAdapter(bookingAdapter);

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            profileViewModel.loadBookings(currentUser.getUid());
        }

        profileViewModel.getBookings().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null) {
                bookingAdapter.setBookings(bookings);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
                actionBar.setTitle("My Bookings");
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onBookingClick(Booking booking) {
        FavoriteFragmentDirections.ActionFavoriteFragmentToBookingDetailFragment action =
                FavoriteFragmentDirections.actionFavoriteFragmentToBookingDetailFragment(booking);
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }
}
