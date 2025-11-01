package fpt.edu.vn.assigment_travelapp.ui.profile;

import android.os.Bundle;
import android.util.Log; // <-- IMPORT QUAN TRỌNG
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
// ViewModel không còn được sử dụng nữa
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // <-- Import
import com.google.firebase.database.DataSnapshot; // <-- Import
import com.google.firebase.database.DatabaseError; // <-- Import
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener; // <-- Import

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.User; // <-- Import
import fpt.edu.vn.assigment_travelapp.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment"; // <-- Tag để debug
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            binding.tvName.setText(currentUser.getDisplayName());
            binding.tvEmail.setText(currentUser.getEmail());
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this).load(currentUser.getPhotoUrl()).into(binding.ivProfile);
            }
        }
    }


    private void setupAuthStateListener() {
        Log.d(TAG, "Đang thiết lập AuthStateListener...");

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // Người dùng đã đăng nhập
                    Log.i(TAG, "AuthStateListener: ĐÃ TÌM THẤY người dùng: " + firebaseUser.getEmail());
                    loadProfileData(firebaseUser);
                } else {
                    // Người dùng đã đăng xuất
                    Log.w(TAG, "AuthStateListener: KHÔNG TÌM THẤY người dùng (đã đăng xuất).");
                }
            }
        };

        // Gắn listener
        mAuth.addAuthStateListener(authStateListener);
    }

    private void loadProfileData(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Lỗi: Email của người dùng bị rỗng.");
            return;
        }

        String dbKey = email.replace(".", ",");
        currentUserRef = mDatabase.child(dbKey);

        Log.d(TAG, "Bắt đầu lắng nghe database tại key: " + dbKey);

        // Tạo Database Listener
        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Biến dbKey đã được định nghĩa ở bên ngoài hàm này
                String dbKey = mAuth.getCurrentUser().getEmail().replace(".", ",");

                if (!snapshot.exists()) {
                    Log.e(TAG, "LỖI DATABASE: Không tìm thấy dữ liệu (snapshot.exists() == false) tại key: " + dbKey);
                    Toast.makeText(getContext(), "Lỗi: Không tìm thấy data", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = snapshot.getValue(User.class);

                if (user == null) {
                    Log.e(TAG, "LỖI MAP DATA: Data tồn tại nhưng không map được (user == null). Kiểm tra lại file User.java và database.");
                    Toast.makeText(getContext(), "Lỗi: Map data thất bại", Toast.LENGTH_SHORT).show();
                    return;
                }

                // THÀNH CÔNG!
                Log.i(TAG, "THÀNH CÔNG: Tải và map data: " + user.getEmail());

                // Đảm bảo fragment vẫn còn "sống"
                if (binding != null) {

                    // === CÁC DÒNG ĐÃ SỬA LỖI ===
                    binding.tvProfileName.setText(user.getFirstName() + " " + user.getLastName());
                    binding.tvProfileEmail.setText(user.getEmail());
                    // =========================

                    Glide.with(ProfileFragment.this)
                            .load(user.getImageUrl())
                            .placeholder(R.drawable.ic_profile)
                            .circleCrop()
                            .into(binding.ivProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "LỖI DATABASE: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi DB: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        // Gắn listener vào database
        currentUserRef.addValueEventListener(databaseListener);
    }

    private void logout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.mobile_navigation, true)
                    .build();
            navController.navigate(R.id.signInFragment, null, navOptions);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        // Gỡ bỏ các listener để tránh rò rỉ bộ nhớ
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
            Log.d(TAG, "Đã gỡ AuthStateListener.");
        }
        if (databaseListener != null && currentUserRef != null) {
            currentUserRef.removeEventListener(databaseListener);
            Log.d(TAG, "Đã gỡ ValueEventListener.");
        }
    }
}