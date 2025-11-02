package fpt.edu.vn.assigment_travelapp.ui.newpost;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;

public class NewPostViewModel extends ViewModel {

    private final IPostRepository postRepository;
    private final MutableLiveData<Boolean> postCreated = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public NewPostViewModel() {
        postRepository = new PostRepository();
    }

    public void createPostWithUri(Uri imageUri, String caption, String location) {
        postRepository.createPostWithUri(imageUri, caption, location, new IPostRepository.OnPostCreationCompleteListener() {
            @Override
            public void onSuccess() {
                postCreated.postValue(true);
            }

            @Override
            public void onFailure(String errorMessage) {
                NewPostViewModel.this.errorMessage.postValue(errorMessage);
                postCreated.postValue(false);
            }
        });
    }

    public LiveData<Boolean> getPostCreated() {
        return postCreated;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}

