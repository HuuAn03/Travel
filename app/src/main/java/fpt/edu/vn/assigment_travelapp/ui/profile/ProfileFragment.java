package fpt.edu.vn.assigment_travelapp.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;

    private ProfileViewModel profileViewModel;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabase;
    private ValueEventListener databaseListener;
    private DatabaseReference currentUserRef;
    private User currentUser;
    private ActivityResultLauncher<Intent> bannerImagePickerLauncher;
    private ActivityResultLauncher<Intent> profileImagePickerLauncher;
    private ViewPagerAdapter viewPagerAdapter;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable menu handling
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        FirebaseUser fUser = mAuth.getCurrentUser();
        String currentUserId = (fUser != null) ? fUser.getUid() : null;
        boolean isViewingOwnProfile = (userId == null || userId.isEmpty() || userId.equals(currentUserId));

        setupActionBar(isViewingOwnProfile);

        bannerImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();
                        updateBannerImage(imageUri);
                    }
                }
        );

        profileImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();
                        updateProfileImage(imageUri);
                    }
                }
        );

        if (isViewingOwnProfile) {
            userId = currentUserId;
            binding.btnChangeBanner.setVisibility(View.VISIBLE);
            binding.btnEditProfile.setVisibility(View.VISIBLE);
            binding.ivProfileImage.setOnClickListener(v -> showProfileImageOptions());
            binding.btnChangeBanner.setOnClickListener(v -> openImageChooser(bannerImagePickerLauncher));
            binding.btnEditProfile.setOnClickListener(v -> showEditBioDialog());
        } else {
            binding.btnChangeBanner.setVisibility(View.GONE);
            binding.btnEditProfile.setVisibility(View.GONE);
            binding.ivProfileImage.setOnClickListener(v -> showViewImageDialog());
        }

        if (userId != null) {
            loadProfileData(userId);
            setupViewPager();
            setupPostObservers();
        } else {
            Log.w(TAG, "Not logged in and no user ID to display profile.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser fUser = mAuth.getCurrentUser();
        String currentUserId = (fUser != null) ? fUser.getUid() : null;
        boolean isViewingOwnProfile = (userId == null || userId.isEmpty() || userId.equals(currentUserId));
        setupActionBar(isViewingOwnProfile);
    }

    private void setupActionBar(boolean isOwnProfile) {
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
                actionBar.setTitle("Profile");
                actionBar.setDisplayHomeAsUpEnabled(!isOwnProfile);
            }
        }
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this, userId);
        binding.viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    FirebaseUser fUser = mAuth.getCurrentUser();
                    boolean isViewingOwnProfile = (fUser != null && userId != null && userId.equals(fUser.getUid()));
                    switch (position) {
                        case 0:
                            tab.setText(isViewingOwnProfile ? "My Posts" : "Posts");
                            break;
                        case 1:
                            tab.setText("Likes");
                            break;
                        case 2:
                            tab.setText("Bookmarks");
                            break;
                        case 3:
                            tab.setText("My Bookings");
                            break;
                    }
                }).attach();
    }

    private void setupPostObservers() {
        profileViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileData(String uid) {
        currentUserRef = mDatabase.child(uid);

        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || getContext() == null) {
                    return;
                }

                currentUser = snapshot.getValue(User.class);
                if (currentUser == null) {
                    return;
                }

                binding.tvProfileName.setText(currentUser.getName());
                binding.tvBio.setText(currentUser.getBio());

                if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
                    try {
                        byte[] imageBytes = Base64.decode(currentUser.getPhotoUrl(), Base64.DEFAULT);
                        Glide.with(getContext()).asBitmap().load(imageBytes).placeholder(R.drawable.ic_profile).into(binding.ivProfileImage);
                    } catch (Exception e) {
                        Glide.with(getContext()).load(currentUser.getPhotoUrl()).placeholder(R.drawable.ic_profile).into(binding.ivProfileImage);
                    }
                }

                if (currentUser.getBackground() != null && !currentUser.getBackground().isEmpty()) {
                    try {
                        byte[] imageBytes = Base64.decode(currentUser.getBackground(), Base64.DEFAULT);
                        Glide.with(getContext()).asBitmap().load(imageBytes).placeholder(android.R.color.darker_gray).into(binding.ivBanner);
                    } catch(IllegalArgumentException e) {
                        Glide.with(getContext()).load(currentUser.getBackground()).placeholder(android.R.color.darker_gray).into(binding.ivBanner);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        };
        currentUserRef.addValueEventListener(databaseListener);
    }

    private void showProfileImageOptions() {
        final CharSequence[] options = {"View Photo", "Change Photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Profile Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("View Photo")) {
                showViewImageDialog();
            } else if (options[item].equals("Change Photo")) {
                openImageChooser(profileImagePickerLauncher);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showViewImageDialog() {
        if (currentUser == null || currentUser.getPhotoUrl() == null || currentUser.getPhotoUrl().isEmpty()) {
            Toast.makeText(getContext(), "No profile image to view.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_view_image, null);
        builder.setView(dialogView);

        ImageView ivFullImage = dialogView.findViewById(R.id.iv_full_image);
        try {
            byte[] imageBytes = Base64.decode(currentUser.getPhotoUrl(), Base64.DEFAULT);
            Glide.with(this).asBitmap().load(imageBytes).into(ivFullImage);
        } catch (Exception e) {
            Glide.with(this).load(currentUser.getPhotoUrl()).into(ivFullImage);
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditBioDialog() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(getContext(), "You must be logged in to edit your profile.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_bio, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final EditText etName = dialogView.findViewById(R.id.et_name);
        final EditText etBio = dialogView.findViewById(R.id.et_bio);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        etName.setText(binding.tvProfileName.getText().toString());
        etBio.setText(binding.tvBio.getText().toString());

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString();
            String newBio = etBio.getText().toString();

            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = firebaseUser.getUid();
            DatabaseReference userRef = mDatabase.child(uid);

            Map<String, Object> profileUpdates = new HashMap<>();
            profileUpdates.put("name", newName);
            profileUpdates.put("bio", newBio);

            userRef.updateChildren(profileUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    });
        });

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void openImageChooser(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launcher.launch(intent);
    }

    private void updateProfileImage(Uri imageUri) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(getContext(), "You must be logged in to change your profile image.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream imageStream = requireActivity().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] byteArray = baos.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            String uid = firebaseUser.getUid();
            mDatabase.child(uid).child("photoUrl").setValue(base64Image)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Log.e(TAG, "Error converting image to Base64", e);
            Toast.makeText(getContext(), "Failed to process image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBannerImage(Uri imageUri) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(getContext(), "You must be logged in to change the banner.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream imageStream = requireActivity().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] byteArray = baos.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            String uid = firebaseUser.getUid();
            mDatabase.child(uid).child("background").setValue(base64Image)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Banner updated successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update banner: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Log.e(TAG, "Error converting image to Base64", e);
            Toast.makeText(getContext(), "Failed to process image.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            mAuth.signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
                NavHostFragment.findNavController(ProfileFragment.this).navigate(R.id.signInFragment);
            });
            return true;
        } else if (itemId == android.R.id.home) {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
        if (databaseListener != null && currentUserRef != null) {
            currentUserRef.removeEventListener(databaseListener);
        }
    }
}
