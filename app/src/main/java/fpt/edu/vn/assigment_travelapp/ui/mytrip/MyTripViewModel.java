package fpt.edu.vn.assigment_travelapp.ui.mytrip;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;

public class MyTripViewModel extends ViewModel {

    private final IPostRepository postRepository;
    private final MutableLiveData<List<PostWithUser>> myPosts = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();

    public MyTripViewModel() {
        postRepository = new PostRepository();
    }

    public void fetchMyPosts(String userId) {
        postRepository.getPostsByUser(userId, new IPostRepository.OnGetAllPostsCompleteListener() {
            @Override
            public void onSuccess(List<PostWithUser> posts) {
                myPosts.postValue(posts);
            }

            @Override
            public void onFailure(String errorMessage) {
                MyTripViewModel.this.errorMessage.postValue(errorMessage);
            }
        });
    }

    public void toggleLike(String postId, String userId) {
        // This would need to be implemented in PostRepository
        // For now, just refresh posts
        fetchMyPosts(userId);
    }

    public void toggleBookmark(String postId, String userId) {
        // This would need to be implemented in PostRepository
        // For now, just refresh posts
        fetchMyPosts(userId);
    }

    public void updatePost(String postId, String caption, String location) {
        postRepository.updatePost(postId, caption, location, new IPostRepository.OnPostUpdateCompleteListener() {
            @Override
            public void onSuccess() {
                updateSuccess.postValue(true);
            }

            @Override
            public void onFailure(String errorMessage) {
                MyTripViewModel.this.errorMessage.postValue(errorMessage);
                updateSuccess.postValue(false);
            }
        });
    }

    public void deletePost(String postId) {
        postRepository.deletePost(postId, new IPostRepository.OnPostDeleteCompleteListener() {
            @Override
            public void onSuccess() {
                deleteSuccess.postValue(true);
                // Remove from list
                List<PostWithUser> currentPosts = myPosts.getValue();
                if (currentPosts != null) {
                    currentPosts.removeIf(post -> post.getPost().getPostId().equals(postId));
                    myPosts.postValue(currentPosts);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                MyTripViewModel.this.errorMessage.postValue(errorMessage);
                deleteSuccess.postValue(false);
            }
        });
    }

    public LiveData<List<PostWithUser>> getMyPosts() {
        return myPosts;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
    }
}

