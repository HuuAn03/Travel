package fpt.edu.vn.assigment_travelapp.ui.newpost;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
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

public class NewPostFragment extends Fragment {

    private FragmentNewPostBinding binding;
    private NewPostViewModel viewModel;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String base64Image;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NewPostViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

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

        loadUserAvatar();
        observeViewModel();

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
    }

    private void loadUserAvatar() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && getContext() != null) {
            mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && getContext() != null) {
                        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                            Glide.with(getContext()).load(user.getPhotoUrl()).into(binding.ivAvatar);
                        } else {
                            binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load user image.", Toast.LENGTH_SHORT).show();
                }
            });
        }
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

        viewModel.createPost(base64Image, caption);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
