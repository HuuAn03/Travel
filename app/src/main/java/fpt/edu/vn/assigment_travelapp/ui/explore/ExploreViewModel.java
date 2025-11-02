package fpt.edu.vn.assigment_travelapp.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;

public class ExploreViewModel extends ViewModel {

    private final IPostRepository postRepository;
    private final MutableLiveData<List<PostWithUser>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ExploreViewModel() {
        postRepository = new PostRepository();
    }

    public void searchPosts(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResults.postValue(null);
            return;
        }

        postRepository.searchPosts(query.trim(), new IPostRepository.OnGetAllPostsCompleteListener() {
            @Override
            public void onSuccess(List<PostWithUser> posts) {
                searchResults.postValue(posts);
            }

            @Override
            public void onFailure(String errorMessage) {
                ExploreViewModel.this.errorMessage.postValue(errorMessage);
            }
        });
    }

    public LiveData<List<PostWithUser>> getSearchResults() {
        return searchResults;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}

