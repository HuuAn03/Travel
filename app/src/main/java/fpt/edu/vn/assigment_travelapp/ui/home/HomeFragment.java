package fpt.edu.vn.assigment_travelapp.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;

    private DatabaseReference userRef;
    private ValueEventListener userListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle(R.string.title_home);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.locationContainer.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Change Location")
                .setMessage("Do you want to change your current location?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_chooseLocationFragment);
                })
                .setNegativeButton("No", null)
                .show();
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        } else {
            // Handle case where user is not logged in
            binding.userName.setText("Hi, Guest");
            binding.userLocation.setText("Unknown location");
            binding.profileImage.setImageResource(R.drawable.ic_profile);
        }

        View.OnClickListener categoryClickListener = v -> {
            v.setSelected(!v.isSelected());
        };

        binding.btnAdventure.setOnClickListener(categoryClickListener);
        binding.btnBeach.setOnClickListener(categoryClickListener);
        binding.btnFoodDrink.setOnClickListener(categoryClickListener);
    }

    private void loadUserData(String uid) {
        userRef = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(uid);

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null || !snapshot.exists()) {
                    Log.w(TAG, "User data not found or context is null.");
                    return;
                }

                User user = snapshot.getValue(User.class);
                if (user != null) {
                    binding.userName.setText("Hi, " + user.getName());
                    if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                        binding.userLocation.setText(user.getLocation());
                    } else {
                        binding.userLocation.setText("Unknown location");
                    }

                    String photoUrl = user.getPhotoUrl();
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                            Glide.with(requireContext())
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(binding.profileImage);
                        } else {
                            try {
                                byte[] decodedString = Base64.decode(photoUrl, Base64.DEFAULT);
                                Glide.with(requireContext())
                                        .asBitmap()
                                        .load(decodedString)
                                        .placeholder(R.drawable.ic_profile)
                                        .into(binding.profileImage);
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "Error decoding Base64 string: ", e);
                                binding.profileImage.setImageResource(R.drawable.ic_profile);
                            }
                        }
                    } else {
                        binding.profileImage.setImageResource(R.drawable.ic_profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read user value.", error.toException());
            }
        };
        userRef.addValueEventListener(userListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null && userRef != null) {
            userRef.removeEventListener(userListener);
        }
        binding = null;
    }
}
