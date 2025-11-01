package fpt.edu.vn.assigment_travelapp.ui.register;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.IUserRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentSignUpBinding;

public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private IUserRepository userRepository;
    private NavController navController;
    private FirebaseAuth mAuth;
    private static final String TAG = "SignUpFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        userRepository = new UserRepository();
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        binding.btnSignUp.setOnClickListener(v -> registerUser());

        binding.tvLogin.setOnClickListener(v -> navController.navigate(R.id.action_signUpFragment_to_signInFragment));
    }

    private void registerUser() {
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validation checks
        if (TextUtils.isEmpty(firstName)) {
            binding.etFirstName.setError("First name is required.");
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            binding.etLastName.setError("Last name is required.");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required.");
            return;
        }
        if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match.");
            return;
        }

        // Step 1: Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity(), authTask -> {
                if (authTask.isSuccessful()) {
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        String name = firstName + " " + lastName;
                        User newUser = new User(name, email, ""); // photoUrl is empty

                        // Step 2: Save user info to Realtime DB with UID as key
                        userRepository.saveUser(uid, newUser).addOnCompleteListener(dbTask -> {
                            if (isAdded()) {
                                if (dbTask.isSuccessful()) {
                                    Log.d(TAG, "User data saved to database.");
                                    Toast.makeText(requireContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        if (isAdded()) {
                                            navController.navigate(R.id.action_signUpFragment_to_signInFragment);
                                        }
                                    }, 1000);
                                } else {
                                    Toast.makeText(requireContext(), "Failed to save user data.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } else {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Authentication failed: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
