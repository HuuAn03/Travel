package fpt.edu.vn.assigment_travelapp.ui.login;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.IUserRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;

public class SignInViewModel extends ViewModel {

    private static final String TAG = "SignInViewModel";
    private final FirebaseAuth mAuth;
    private final IUserRepository userRepository;

    private final MutableLiveData<Boolean> _signInSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> signInSuccess = _signInSuccess;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    public SignInViewModel() {
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
    }

    public void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        fetchUserData(firebaseUser.getUid());
                    } else {
                         _signInSuccess.setValue(true); // Should not happen
                    }
                } else {
                    if (task.getException() != null) {
                        _errorMessage.setValue(task.getException().getMessage());
                    } else {
                        _errorMessage.setValue("An unknown error occurred.");
                    }
                    _signInSuccess.setValue(false);
                }
            });
    }

    public void signInWithGoogle(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        userRepository.getUser(firebaseUser.getUid(), new IUserRepository.OnGetUserCompleteListener() {
                            @Override
                            public void onSuccess(User user) {
                                // User exists, login successful
                                _signInSuccess.setValue(true);
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                if ("User not found".equals(errorMessage)) {
                                    // User is new, save their info to Realtime DB
                                    String name = firebaseUser.getDisplayName();
                                    String email = firebaseUser.getEmail();
                                    String photoUrl = (firebaseUser.getPhotoUrl() != null) ? firebaseUser.getPhotoUrl().toString() : "";
                                    User newUser = new User(firebaseUser.getUid(), name, email, photoUrl); // role is "user" by default
                                    saveUserToDatabase(firebaseUser.getUid(), newUser);
                                } else {
                                    _errorMessage.setValue(errorMessage);
                                    _signInSuccess.setValue(false);
                                }
                            }
                        });
                    } else {
                        _signInSuccess.setValue(true); // Should not happen
                    }
                } else {
                    if (task.getException() != null) {
                        _errorMessage.setValue(task.getException().getMessage());
                    } else {
                        _errorMessage.setValue("An unknown error occurred.");
                    }
                    _signInSuccess.setValue(false);
                }
            });
    }

    private void fetchUserData(String uid) {
        userRepository.getUser(uid, new IUserRepository.OnGetUserCompleteListener() {
            @Override
            public void onSuccess(User user) {
                // User data is fetched, login is successful
                _signInSuccess.setValue(true);
            }

            @Override
            public void onFailure(String errorMessage) {
                _errorMessage.setValue(errorMessage);
                _signInSuccess.setValue(false);
            }
        });
    }

    private void saveUserToDatabase(String uid, User user) {
        userRepository.saveUser(uid, user)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User data saved to database.");
                    _signInSuccess.setValue(true);
                } else {
                    Log.w(TAG, "Failed to save user data.", task.getException());
                    _errorMessage.setValue("Failed to save user data.");
                    _signInSuccess.setValue(false);
                }
            });
    }
}
