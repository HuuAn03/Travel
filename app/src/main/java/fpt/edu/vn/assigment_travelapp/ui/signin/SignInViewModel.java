package fpt.edu.vn.assigment_travelapp.ui.signin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthResult;

import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.IUserRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;

public class SignInViewModel extends ViewModel {

    private final IUserRepository userRepository;
    private final MutableLiveData<Boolean> signInSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SignInViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<Boolean> getSignInSuccess() {
        return signInSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setSignInResult(boolean success, String error) {
        signInSuccess.postValue(success);
        if (error != null) {
            errorMessage.postValue(error);
        }
    }
}

