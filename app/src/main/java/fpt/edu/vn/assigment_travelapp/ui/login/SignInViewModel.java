package fpt.edu.vn.assigment_travelapp.ui.login;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import at.favre.lib.crypto.bcrypt.BCrypt;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;

public class SignInViewModel extends ViewModel {

    private static final String TAG = "SignInViewModel";
    private final FirebaseAuth mAuth;
    private final UserRepository userRepository;

    private final MutableLiveData<Boolean> _signInSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> signInSuccess = _signInSuccess;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    public SignInViewModel() {
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
    }

    public void signInWithEmailAndPassword(String email, String password) {
        userRepository.getUserByEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                    if (result.verified) {
                        _signInSuccess.setValue(true);
                    } else {
                        _errorMessage.setValue("Invalid password.");
                        _signInSuccess.setValue(false);
                    }
                } else {
                    _errorMessage.setValue("User not found.");
                    _signInSuccess.setValue(false);
                }
            } else {
                _errorMessage.setValue("An error occurred: " + task.getException().getMessage());
                _signInSuccess.setValue(false);
            }
        });
    }


    public void signInWithGoogle(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    _signInSuccess.setValue(true); // Set success to trigger navigation
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String displayName = firebaseUser.getDisplayName();
                        String firstName = "";
                        String lastName = "";
                        if (displayName != null && !displayName.isEmpty()) {
                            String[] nameParts = displayName.split(" ", 2);
                            firstName = nameParts[0];
                            if (nameParts.length > 1) {
                                lastName = nameParts[1];
                            }
                        }

                        // Create a new user object
                        User user = new User(
                            firstName,
                            lastName,
                            firebaseUser.getEmail(),
                            "" // No password for Google Sign-In
                        );

                        if (firebaseUser.getPhotoUrl() != null) {
                            user.setImageUrl(firebaseUser.getPhotoUrl().toString());
                        }
                        
                        // Save the user to Realtime Database in the background
                        saveUserToDatabase(user);
                    } else {
                         Log.w(TAG, "Google Sign In successful, but failed to get user information to save to database.");
                    }
                } else {
                    _errorMessage.setValue(task.getException().getMessage());
                    _signInSuccess.setValue(false);
                }
            });
    }

    private void saveUserToDatabase(User user) {
        userRepository.saveUser(user)
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Failed to save user data.", task.getException());
                    _errorMessage.setValue("Failed to save user data.");
                } else {
                    Log.d(TAG, "User data saved to database.");
                }
            });
    }
}
