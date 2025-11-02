package fpt.edu.vn.assigment_travelapp.ui.newpost;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import fpt.edu.vn.assigment_travelapp.data.model.Post;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;

public class NewPostViewModel extends ViewModel {
    private final IPostRepository postRepository;
    private final MutableLiveData<PostCreationState> postCreationState = new MutableLiveData<>();
    private final MutableLiveData<Post> post = new MutableLiveData<>();

    public NewPostViewModel() {
        this.postRepository = new PostRepository();
    }

    public LiveData<PostCreationState> getPostCreationState() {
        return postCreationState;
    }

    public LiveData<Post> getPost() {
        return post;
    }

    public void createPost(String base64Image, String caption) {
        postCreationState.setValue(PostCreationState.LOADING);
        postRepository.createPost(base64Image, caption, new IPostRepository.OnPostCreationCompleteListener() {
            @Override
            public void onSuccess() {
                postCreationState.setValue(PostCreationState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                postCreationState.setValue(PostCreationState.error(errorMessage));
            }
        });
    }

    public void getPost(String postId) {
        postRepository.getPost(postId, new IPostRepository.OnGetPostCompleteListener() {
            @Override
            public void onSuccess(Post result) {
                post.setValue(result);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle error
            }
        });
    }

    public void updatePost(String postId, String base64Image, String caption) {
        postCreationState.setValue(PostCreationState.LOADING);
        postRepository.updatePost(postId, base64Image, caption, new IPostRepository.OnPostUpdateCompleteListener() {
            @Override
            public void onSuccess() {
                postCreationState.setValue(PostCreationState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                postCreationState.setValue(PostCreationState.error(errorMessage));
            }
        });
    }

    public static class PostCreationState {
        public enum Status {
            SUCCESS,
            ERROR,
            LOADING
        }

        private final Status status;
        private final String errorMessage;

        private PostCreationState(Status status, String errorMessage) {
            this.status = status;
            this.errorMessage = errorMessage;
        }

        public Status getStatus() {
            return status;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static PostCreationState SUCCESS = new PostCreationState(Status.SUCCESS, null);
        public static PostCreationState LOADING = new PostCreationState(Status.LOADING, null);
        public static PostCreationState error(String errorMessage) {
            return new PostCreationState(Status.ERROR, errorMessage);
        }
    }
}
