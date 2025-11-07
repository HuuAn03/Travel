package fpt.edu.vn.assigment_travelapp.ui.booking;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.PlaceAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Place;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentSearchLocationBinding;

public class SearchLocationFragment extends Fragment {

    private FragmentSearchLocationBinding binding;
    private SearchLocationViewModel viewModel;
    private PlaceAdapter placeAdapter;
    private List<Place> placeList = new ArrayList<>();
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchLocationBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(SearchLocationViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearch();
        checkUserRole();
        setupManagePlacesButton();
        observeViewModel();

        viewModel.getAllPlaces();
    }

    private void checkUserRole() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && "admin".equals(user.getRole())) {
                        binding.btnManagePlaces.setVisibility(View.VISIBLE);
                    } else {
                        binding.btnManagePlaces.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    binding.btnManagePlaces.setVisibility(View.GONE);
                }
            });
        } else {
            binding.btnManagePlaces.setVisibility(View.GONE);
        }
    }

    private void setupManagePlacesButton() {
        binding.btnManagePlaces.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_searchLocationFragment_to_managePlacesFragment);
        });
    }

    private void setupRecyclerView() {
        placeAdapter = new PlaceAdapter(placeList);
        placeAdapter.setOnPlaceClickListener(place -> {
            Bundle bundle = new Bundle();
            bundle.putString("placeId", place.getPlaceId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_searchLocationFragment_to_bookingFragment, bundle);
        });
        binding.recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPlaces.setAdapter(placeAdapter);
    }

    private void setupSearch() {
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    performSearch();
                } else {
                    viewModel.getAllPlaces();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            performSearch();
        });
    }

    private void performSearch() {
        String query = binding.etSearch.getText().toString();
        viewModel.searchPlaces(query);
    }

    private void observeViewModel() {
        viewModel.getPlaces().observe(getViewLifecycleOwner(), places -> {
            placeList.clear();
            if (places != null && !places.isEmpty()) {
                placeList.addAll(places);
                binding.tvEmpty.setVisibility(View.GONE);
                binding.recyclerViewPlaces.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.recyclerViewPlaces.setVisibility(View.GONE);
            }
            placeAdapter.notifyDataSetChanged();
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
                binding.recyclerViewPlaces.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerViewPlaces.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
