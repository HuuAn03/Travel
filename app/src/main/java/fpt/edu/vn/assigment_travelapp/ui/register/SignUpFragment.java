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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import at.favre.lib.crypto.bcrypt.BCrypt;
import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentSignUpBinding;

public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private DatabaseReference mDatabase;
    private NavController navController;
    private static final String TAG = "SignUpFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        binding.btnSignUp.setOnClickListener(v -> {
            String firstName = binding.etFirstName.getText().toString().trim();
            String lastName = binding.etLastName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

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

            // Hash the password before saving
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

            // Create a new User object with the hashed password
            User user = new User(firstName, lastName, email, hashedPassword);

            // Save the user to the database
            mDatabase.child("users").child(email.replace(".", ",")).setValue(user)
                    .addOnCompleteListener(task -> {
                        if (isAdded()) {
                           if (task.isSuccessful()) {
                                Log.d(TAG, "User registration successful.");
                                Toast.makeText(requireContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    if (isAdded()) {
                                        navController.navigate(R.id.action_signUpFragment_to_signInFragment);
                                    }
                                }, 2000); // 2 seconds delay
                            } else {
                                String errorMessage = "Registration failed: " + task.getException().getMessage();
                                Log.e(TAG, "Registration failed", task.getException());
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        binding.tvLogin.setOnClickListener(v -> navController.navigate(R.id.action_signUpFragment_to_signInFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
