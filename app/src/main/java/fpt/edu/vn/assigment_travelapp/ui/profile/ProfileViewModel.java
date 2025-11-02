package fpt.edu.vn.assigment_travelapp.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.IUserRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;

public class ProfileViewModel extends ViewModel {

    private final IUserRepository userRepository;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public ProfileViewModel() {
        userRepository = new UserRepository();
    }

    public void fetchUser(String userId) {
        userRepository.getUser(userId, new IUserRepository.OnGetUserCompleteListener() {
            @Override
            public void onSuccess(User userData) {
                user.postValue(userData);
            }

            @Override
            public void onFailure(String errorMessage) {
                ProfileViewModel.this.errorMessage.postValue(errorMessage);
            }
        });
    }

    public void updateUser(String userId, User userData) {
        userRepository.updateUser(userId, userData, new IUserRepository.OnUpdateUserCompleteListener() {
            @Override
            public void onSuccess() {
                updateSuccess.postValue(true);
                user.postValue(userData);
            }

            @Override
            public void onFailure(String errorMessage) {
                ProfileViewModel.this.errorMessage.postValue(errorMessage);
                updateSuccess.postValue(false);
            }
        });
    }

    public void updateAvatar(String userId, Uri avatarUri) {
        userRepository.updateUserAvatar(userId, avatarUri, new IUserRepository.OnUpdateUserCompleteListener() {
            @Override
            public void onSuccess() {
                updateSuccess.postValue(true);
                fetchUser(userId); // Refresh user data
            }

            @Override
            public void onFailure(String errorMessage) {
                ProfileViewModel.this.errorMessage.postValue(errorMessage);
                updateSuccess.postValue(false);
            }
        });
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }
}

