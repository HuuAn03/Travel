package fpt.edu.vn.assigment_travelapp.ui.newpost;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentNewPostBinding;
import fpt.edu.vn.assigment_travelapp.ui.profile.ProfileViewModel;

public class NewPostFragment extends Fragment {

    private static final String TAG = "NewPostFragment";
    private FragmentNewPostBinding binding;
    private NewPostViewModel viewModel;
    private ProfileViewModel profileViewModel;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String base64Image;
    private String postId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NewPostViewModel.class);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            binding.ivPostImage.setImageURI(selectedImageUri);
                            try {
                                InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Compress image
                                byte[] byteArray = baos.toByteArray();
                                base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNewPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTitle();
        loadUserAvatar();
        observeViewModel();

        if (postId != null) {
            viewModel.getPost(postId);
        }

        binding.ivClose.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        View.OnClickListener pickImageClickListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        };

        binding.ivPostImage.setOnClickListener(pickImageClickListener);
        binding.btnChangeImage.setOnClickListener(pickImageClickListener);

        binding.btnShare.setOnClickListener(v -> {
            sharePost();
        });
    }

    private void setupTitle() {
        if (postId != null) {
            binding.tvToolbarTitle.setText("Edit Post");
        } else {
            binding.tvToolbarTitle.setText("New Post");
        }
    }

    private void observeViewModel() {
        viewModel.getPostCreationState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnShare.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnShare.setEnabled(true);
                    Toast.makeText(getContext(), "Post shared successfully!", Toast.LENGTH_SHORT).show();
                    profileViewModel.refreshData();
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnShare.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to share post: " + state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getPost().observe(getViewLifecycleOwner(), post -> {
            if (post != null) {
                binding.etCaption.setText(post.getCaption());
                if (post.getImageUrl() != null) {
                    base64Image = post.getImageUrl();
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    binding.ivPostImage.setImageBitmap(decodedByte);
                }
            }
        });
    }

    private void loadUserAvatar() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isAdded()) { // Check if fragment is still added
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                            String photoData = user.getPhotoUrl();
                            if (photoData.startsWith("http://") || photoData.startsWith("https://")) {
                                Glide.with(requireContext())
                                        .load(photoData)
                                        .listener(createGlideListener())
                                        .into(binding.ivAvatar);
                            } else {
                                try {
                                    byte[] decodedString;
                                    int commaIndex = photoData.indexOf(',');
                                    if (commaIndex != -1) {
                                        String base64part = photoData.substring(commaIndex + 1);
                                        decodedString = Base64.decode(base64part, Base64.DEFAULT);
                                    } else {
                                        decodedString = Base64.decode(photoData, Base64.DEFAULT);
                                    }
                                    Glide.with(requireContext())
                                            .load(decodedString)
                                            .listener(createGlideListener())
                                            .into(binding.ivAvatar);
                                } catch (IllegalArgumentException e) {
                                    Log.e(TAG, "Invalid Base64 string for avatar", e);
                                    binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                                }
                            }
                        } else {
                            binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (isAdded()) {
                        Log.e(TAG, "Firebase onCancelled", error.toException());
                        Toast.makeText(getContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private RequestListener<Drawable> createGlideListener() {
        return new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (isAdded()) {
                    Log.e(TAG, "Glide load failed", e);
                    Toast.makeText(getContext(), "Failed to load user avatar.", Toast.LENGTH_SHORT).show();
                    binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        };
    }


    private void sharePost() {
        String caption = binding.etCaption.getText().toString().trim();
        if (TextUtils.isEmpty(caption)) {
            Toast.makeText(getContext(), "Please enter a caption.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(base64Image)) {
            Toast.makeText(getContext(), "Please select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (postId != null) {
            viewModel.updatePost(postId, base64Image, caption);
        } else {
            viewModel.createPost(base64Image, caption);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
