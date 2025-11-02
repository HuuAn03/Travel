package fpt.edu.vn.assigment_travelapp.ui.signup;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.IUserRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentSignUpBinding;

public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private SignUpViewModel viewModel;
    private FirebaseAuth mAuth;
    private IUserRepository userRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSignUp.setOnClickListener(v -> signUp());
        binding.textViewSignIn.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_signUpFragment_to_signInFragment);
        });

        observeViewModel();
    }

    private void signUp() {
        String name = binding.editTextName.getText().toString().trim();
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button and show loading
        binding.buttonSignUp.setEnabled(false);
        binding.buttonSignUp.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User(name, email, null);
                            user.setUserId(firebaseUser.getUid());
                            
                            // Save user to database and wait for completion
                            userRepository.saveUser(firebaseUser.getUid(), user)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully saved user
                                        viewModel.setSignUpResult(true, null);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to save user, but auth succeeded
                                        Toast.makeText(getContext(), "Account created but failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        // Still navigate to home since auth is successful
                                        viewModel.setSignUpResult(true, null);
                                    });
                        } else {
                            binding.buttonSignUp.setEnabled(true);
                            binding.buttonSignUp.setText("Sign Up");
                            viewModel.setSignUpResult(false, "Failed to get user information");
                        }
                    } else {
                        // Enable button and reset text on failure
                        binding.buttonSignUp.setEnabled(true);
                        binding.buttonSignUp.setText("Sign Up");
                        String errorMessage = task.getException() != null 
                                ? task.getException().getMessage() 
                                : "Sign up failed";
                        viewModel.setSignUpResult(false, errorMessage);
                    }
                });
    }

    private void observeViewModel() {
        viewModel.getSignUpSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                // Reset button state
                binding.buttonSignUp.setEnabled(true);
                binding.buttonSignUp.setText("Sign Up");
                
                Toast.makeText(getContext(), "Sign up successful", Toast.LENGTH_SHORT).show();
                
                // Navigate to home
                try {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_signUpFragment_to_navigation_home);
                } catch (Exception e) {
                    // Fallback navigation if action not found
                    Toast.makeText(getContext(), "Account created! Please sign in.", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_signUpFragment_to_signInFragment);
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                // Reset button state
                binding.buttonSignUp.setEnabled(true);
                binding.buttonSignUp.setText("Sign Up");
                
                // Show error message with better formatting
                String displayError = error;
                if (error.contains("email address is already in use")) {
                    displayError = "This email is already registered. Please sign in instead.";
                } else if (error.contains("invalid email")) {
                    displayError = "Please enter a valid email address.";
                } else if (error.contains("weak password")) {
                    displayError = "Password is too weak. Please use a stronger password.";
                } else if (error.contains("operation is not allowed")) {
                    displayError = "Email/Password sign-in is not enabled in Firebase Console.\nPlease enable it in Firebase Authentication settings.";
                } else if (error.contains("network")) {
                    displayError = "Network error. Please check your internet connection.";
                }
                Toast.makeText(getContext(), displayError, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

