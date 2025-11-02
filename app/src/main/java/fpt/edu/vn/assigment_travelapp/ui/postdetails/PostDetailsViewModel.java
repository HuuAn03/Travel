package fpt.edu.vn.assigment_travelapp.ui.postdetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;

public class PostDetailsViewModel extends ViewModel {

    private final IPostRepository postRepository;
    private final MutableLiveData<PostDetailsState> postDetailsState = new MutableLiveData<>();

    public PostDetailsViewModel() {
        postRepository = new PostRepository();
    }

    public void fetchPostDetails(String postId) {
        postDetailsState.postValue(new PostDetailsState(PostDetailsState.Status.LOADING, null, null));

        postRepository.getAllPosts(new IPostRepository.OnGetAllPostsCompleteListener() {
            @Override
            public void onSuccess(java.util.List<PostWithUser> posts) {
                for (PostWithUser postWithUser : posts) {
                    if (postWithUser.getPost().getPostId().equals(postId)) {
                        postDetailsState.postValue(new PostDetailsState(PostDetailsState.Status.SUCCESS, postWithUser, null));
                        return;
                    }
                }
                postDetailsState.postValue(new PostDetailsState(PostDetailsState.Status.ERROR, null, "Post not found"));
            }

            @Override
            public void onFailure(String errorMessage) {
                postDetailsState.postValue(new PostDetailsState(PostDetailsState.Status.ERROR, null, errorMessage));
            }
        });
    }

    public LiveData<PostDetailsState> getPostDetailsState() {
        return postDetailsState;
    }

    public static class PostDetailsState {
        public enum Status {
            LOADING, SUCCESS, ERROR
        }

        private final Status status;
        private final PostWithUser postWithUser;
        private final String errorMessage;

        public PostDetailsState(Status status, PostWithUser postWithUser, String errorMessage) {
            this.status = status;
            this.postWithUser = postWithUser;
            this.errorMessage = errorMessage;
        }

        public Status getStatus() {
            return status;
        }

        public PostWithUser getPostWithUser() {
            return postWithUser;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

