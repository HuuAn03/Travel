package fpt.edu.vn.assigment_travelapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.BookingAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;

public class BookingsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {

    private static final String ARG_USER_ID = "userId";
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private ProfileViewModel profileViewModel;
    private String userId;

    public static BookingsFragment newInstance(String userId) {
        BookingsFragment fragment = new BookingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);
        recyclerView = view.findViewById(R.id.rv_bookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingAdapter(new ArrayList<>());
        adapter.setOnBookingClickListener(this);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        if (userId != null) {
            profileViewModel.loadBookings(userId);
        }

        profileViewModel.getBookings().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null) {
                ((BookingAdapter) recyclerView.getAdapter()).setBookings(bookings);
            }
        });
    }

    @Override
    public void onBookingClick(Booking booking) {
        // Handle booking click event if needed
    }
}
