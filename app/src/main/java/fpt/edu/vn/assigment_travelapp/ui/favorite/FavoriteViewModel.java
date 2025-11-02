package fpt.edu.vn.assigment_travelapp.ui.favorite;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;

public class FavoriteViewModel extends ViewModel {

    private final IPostRepository postRepository;
    private final MutableLiveData<List<PostWithUser>> bookmarkedPosts = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public FavoriteViewModel() {
        postRepository = new PostRepository();
    }

    public void fetchBookmarkedPosts(String userId) {
        postRepository.getBookmarkedPosts(userId, new IPostRepository.OnGetAllPostsCompleteListener() {
            @Override
            public void onSuccess(List<PostWithUser> posts) {
                bookmarkedPosts.postValue(posts);
            }

            @Override
            public void onFailure(String errorMessage) {
                FavoriteViewModel.this.errorMessage.postValue(errorMessage);
            }
        });
    }

    public LiveData<List<PostWithUser>> getBookmarkedPosts() {
        return bookmarkedPosts;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}

