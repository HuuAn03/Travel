package fpt.edu.vn.assigment_travelapp.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;

public class HomeViewModel extends ViewModel {

    private final IPostRepository postRepository;
    private final MutableLiveData<List<PostWithUser>> posts = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel() {
        postRepository = new PostRepository();
    }

    public void fetchFeedPosts(String userId) {
        if (userId == null || userId.isEmpty()) {
            // If no user ID, get all posts
            postRepository.getAllPosts(new IPostRepository.OnGetAllPostsCompleteListener() {
                @Override
                public void onSuccess(List<PostWithUser> postsList) {
                    posts.postValue(postsList);
                }

                @Override
                public void onFailure(String errorMessage) {
                    HomeViewModel.this.errorMessage.postValue(errorMessage);
                }
            });
        } else {
            // Get posts from followed users
            postRepository.getPostsByFollowedUsers(userId, new IPostRepository.OnGetAllPostsCompleteListener() {
                @Override
                public void onSuccess(List<PostWithUser> postsList) {
                    posts.postValue(postsList);
                }

                @Override
                public void onFailure(String errorMessage) {
                    HomeViewModel.this.errorMessage.postValue(errorMessage);
                }
            });
        }
    }

    public LiveData<List<PostWithUser>> getPosts() {
        return posts;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}

