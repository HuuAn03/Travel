package fpt.edu.vn.assigment_travelapp.ui.mytrip;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.IUserRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;

public class MyTripViewModel extends ViewModel {
    private final IPostRepository postRepository;
    private final IUserRepository userRepository;
    private final MutableLiveData<PostFetchState> postFetchState = new MutableLiveData<>();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final DatabaseReference postsRef;

    public MyTripViewModel() {
        this.postRepository = new PostRepository();
        this.userRepository = new UserRepository();
        this.postsRef = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("posts");
    }

    public LiveData<PostFetchState> getPostFetchState() {
        return postFetchState;
    }

    public LiveData<User> getUser() {
        return user;
    }

    public void getUser(String userId) {
        userRepository.getUser(userId, new IUserRepository.OnGetUserCompleteListener() {
            @Override
            public void onSuccess(User result) {
                user.setValue(result);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle error
            }
        });
    }

    public void fetchAllPosts() {
        postFetchState.setValue(PostFetchState.LOADING);
        postRepository.getAllPosts(new IPostRepository.OnGetAllPostsCompleteListener() {
            @Override
            public void onSuccess(List<PostWithUser> posts) {
                postFetchState.setValue(PostFetchState.success(posts));
            }

            @Override
            public void onFailure(String errorMessage) {
                postFetchState.setValue(PostFetchState.error(errorMessage));
            }
        });
    }

    public void toggleLike(String postId, String userId) {
        postsRef.child(postId).child("likes").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    postsRef.child(postId).child("likes").child(userId).removeValue();
                } else {
                    postsRef.child(postId).child("likes").child(userId).setValue(true);
                }
            }
        });
    }

    public void toggleBookmark(String postId, String userId) {
        postsRef.child(postId).child("bookmarks").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    postsRef.child(postId).child("bookmarks").child(userId).removeValue();
                } else {
                    postsRef.child(postId).child("bookmarks").child(userId).setValue(true);
                }
            }
        });
    }

    public void deletePost(String postId) {
        postRepository.deletePost(postId, new IPostRepository.OnDeletePostCompleteListener() {
            @Override
            public void onSuccess() {
                fetchAllPosts();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle error
            }
        });
    }

    public static class PostFetchState {
        public enum Status {
            SUCCESS,
            ERROR,
            LOADING
        }

        private final Status status;
        private final List<PostWithUser> posts;
        private final String errorMessage;

        private PostFetchState(Status status, List<PostWithUser> posts, String errorMessage) {
            this.status = status;
            this.posts = posts;
            this.errorMessage = errorMessage;
        }

        public Status getStatus() {
            return status;
        }

        public List<PostWithUser> getPosts() {
            return posts;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static PostFetchState LOADING = new PostFetchState(Status.LOADING, null, null);
        public static PostFetchState success(List<PostWithUser> posts) {
            return new PostFetchState(Status.SUCCESS, posts, null);
        }
        public static PostFetchState error(String errorMessage) {
            return new PostFetchState(Status.ERROR, null, errorMessage);
        }
    }
}
