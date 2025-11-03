package fpt.edu.vn.assigment_travelapp.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Post;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;

public class ProfileViewModel extends ViewModel {

    private final FirebaseAuth mAuth;
    private final UserRepository userRepository;
    private final IPostRepository postRepository;

    private FirebaseAuth.AuthStateListener authStateListener;

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public LiveData<User> user = _user;

    private final MutableLiveData<List<Post>> _userPosts = new MutableLiveData<>();
    public LiveData<List<Post>> userPosts = _userPosts;

    private final MutableLiveData<List<Post>> _likedPosts = new MutableLiveData<>();
    public LiveData<List<Post>> likedPosts = _likedPosts;

    private final MutableLiveData<List<Post>> _bookmarkedPosts = new MutableLiveData<>();
    public LiveData<List<Post>> bookmarkedPosts = _bookmarkedPosts;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public ProfileViewModel() {
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
        postRepository = new PostRepository();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    loadAllData(firebaseUser);
                } else {
                    _error.setValue("Không có người dùng nào đang đăng nhập.");
                }
            }
        };
        mAuth.addAuthStateListener(authStateListener);
    }

    public void refreshData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadAllData(currentUser);
        }
    }

    private void loadAllData(FirebaseUser firebaseUser) {
        loadUserProfileData(firebaseUser);
        loadUserPosts(firebaseUser.getUid());
        loadLikedPosts(firebaseUser.getUid());
        loadBookmarkedPosts(firebaseUser.getUid());
    }

    private void loadUserProfileData(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        if (email != null) {
            userRepository.getUserByEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User dbUser = task.getResult().getValue(User.class);
                    if (dbUser != null) {
                        _user.setValue(dbUser);
                        _error.setValue(null);
                    } else {
                        _error.setValue("Không tìm thấy dữ liệu người dùng trong database.");
                    }
                } else {
                    _error.setValue("Lỗi khi tải dữ liệu: " + task.getException().getMessage());
                }
            });
        } else {
            _error.setValue("Email người dùng bị null.");
        }
    }

    public void loadUserPosts(String userId) {
        postRepository.getPostsByUserId(userId, new IPostRepository.OnGetPostsCompleteListener() {
            @Override
            public void onSuccess(List<Post> posts) {
                _userPosts.setValue(posts);
            }

            @Override
            public void onFailure(String errorMessage) {
                _error.setValue(errorMessage);
            }
        });
    }

    public void loadLikedPosts(String userId) {
        postRepository.getLikedPosts(userId, new IPostRepository.OnGetPostsCompleteListener() {
            @Override
            public void onSuccess(List<Post> posts) {
                _likedPosts.setValue(posts);
            }

            @Override
            public void onFailure(String errorMessage) {
                _error.setValue(errorMessage);
            }
        });
    }

    public void loadBookmarkedPosts(String userId) {
        postRepository.getBookmarkedPosts(userId, new IPostRepository.OnGetPostsCompleteListener() {
            @Override
            public void onSuccess(List<Post> posts) {
                _bookmarkedPosts.setValue(posts);
            }

            @Override
            public void onFailure(String errorMessage) {
                _error.setValue(errorMessage);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
}
