package fpt.edu.vn.assigment_travelapp.ui.comment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.Post;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.IUserRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;

public class CommentViewModel extends ViewModel {
    private final IPostRepository postRepository;
    private final IUserRepository userRepository;
    private final MutableLiveData<CommentFetchState> commentFetchState = new MutableLiveData<>();
    private final MutableLiveData<CommentAddState> commentAddState = new MutableLiveData<>();
    private final MutableLiveData<Post> post = new MutableLiveData<>();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLiked = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBookmarked = new MutableLiveData<>();
    private final MutableLiveData<Integer> likeCount = new MutableLiveData<>();
    private final MutableLiveData<User> currentUserDetails = new MutableLiveData<>();
    private final MutableLiveData<PostDeleteState> postDeleteState = new MutableLiveData<>();


    public CommentViewModel() {
        this.postRepository = new PostRepository();
        this.userRepository = new UserRepository();
    }

    public LiveData<CommentFetchState> getCommentFetchState() {
        return commentFetchState;
    }

    public LiveData<CommentAddState> getCommentAddState() {
        return commentAddState;
    }

    public LiveData<Post> getPost() {
        return post;
    }

    public LiveData<User> getUser() { return user; }

    public LiveData<Boolean> getIsLiked() { return isLiked; }

    public LiveData<Boolean> getIsBookmarked() { return isBookmarked; }

    public LiveData<Integer> getLikeCount() { return likeCount; }

    public LiveData<User> getCurrentUserDetails() { return currentUserDetails; }

    public LiveData<PostDeleteState> getPostDeleteState() {
        return postDeleteState;
    }

    public void fetchPost(String postId) {
        postRepository.getPost(postId, new IPostRepository.OnGetPostCompleteListener() {
            @Override
            public void onSuccess(Post fetchedPost) {
                post.setValue(fetchedPost);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle error, maybe post a value to another LiveData to show an error message
            }
        });
    }


    public void fetchComments(String postId) {
        commentFetchState.setValue(CommentFetchState.LOADING);
        postRepository.getCommentsWithUsers(postId, new IPostRepository.OnGetCommentsWithUsersCompleteListener() {
            @Override
            public void onSuccess(List<CommentWithUser> flatList) {
                List<CommentWithUser> nestedList = buildNestedList(flatList);
                commentFetchState.setValue(CommentFetchState.success(nestedList));
            }

            @Override
            public void onFailure(String errorMessage) {
                commentFetchState.setValue(CommentFetchState.error(errorMessage));
            }
        });
    }

    private List<CommentWithUser> buildNestedList(List<CommentWithUser> flatList) {
        List<CommentWithUser> topLevelComments = new ArrayList<>();
        Map<String, CommentWithUser> commentMap = new HashMap<>();

        // First pass: map all comments by their ID and identify top-level comments
        for (CommentWithUser cwu : flatList) {
            if (cwu.getComment() != null && cwu.getComment().getCommentId() != null) {
                commentMap.put(cwu.getComment().getCommentId(), cwu);
                if (cwu.getComment().getParentCommentId() == null || cwu.getComment().getParentCommentId().isEmpty()) {
                    topLevelComments.add(cwu);
                }
            }
        }

        // Second pass: link replies to their parents
        for (CommentWithUser cwu : flatList) {
            if (cwu.getComment() != null) {
                String parentId = cwu.getComment().getParentCommentId();
                if (parentId != null && !parentId.isEmpty()) {
                    CommentWithUser parent = commentMap.get(parentId);
                    if (parent != null) {
                        parent.getReplies().add(cwu);
                    }
                }
            }
        }
        return topLevelComments;
    }


    public void addComment(String postId, Comment comment) {
        commentAddState.setValue(CommentAddState.LOADING);
        postRepository.addComment(postId, comment, new IPostRepository.OnCommentAddCompleteListener() {
            @Override
            public void onSuccess() {
                commentAddState.setValue(CommentAddState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                commentAddState.setValue(CommentAddState.error(errorMessage));
            }
        });
    }

    public void fetchUserDetails(String userId) {
        userRepository.getUser(userId, new IUserRepository.OnGetUserCompleteListener() {
            @Override
            public void onSuccess(User fetchedUser) {
                user.setValue(fetchedUser);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle error
            }
        });
    }

    public void fetchCurrentUserDetails(String userId) {
        userRepository.getUser(userId, new IUserRepository.OnGetUserCompleteListener() {
            @Override
            public void onSuccess(User fetchedUser) {
                currentUserDetails.setValue(fetchedUser);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle error
            }
        });
    }

    public void fetchLikeCount(String postId) {
        postRepository.getLikeCount(postId, count -> likeCount.postValue((int) count));
    }

    public void checkIfPostIsLiked(String postId, String userId) {
        postRepository.isLiked(postId, userId, isLiked::postValue);
    }

    public void checkIfPostIsBookmarked(String postId, String userId) {
        postRepository.isBookmarked(postId, userId, isBookmarked::postValue);
    }

    public void toggleLike(String postId, String userId) {
        postRepository.toggleLike(postId, userId, (isSet, newCount) -> {
            isLiked.postValue(isSet);
            likeCount.postValue(newCount);
        });
    }

    public void toggleBookmark(String postId, String userId) {
        postRepository.toggleBookmark(postId, userId, isBookmarked::postValue);
    }

    public void deletePost(String postId) {
        postDeleteState.setValue(PostDeleteState.LOADING);
        postRepository.deletePost(postId, new IPostRepository.OnDeletePostCompleteListener() {
            @Override
            public void onSuccess() {
                postDeleteState.setValue(PostDeleteState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                postDeleteState.setValue(PostDeleteState.error(errorMessage));
            }
        });
    }

    // Inner classes for state management

    public static class CommentFetchState {
        public enum Status { SUCCESS, ERROR, LOADING }
        private final Status status;
        private final List<CommentWithUser> comments;
        private final String errorMessage;

        private CommentFetchState(Status status, List<CommentWithUser> comments, String errorMessage) {
            this.status = status;
            this.comments = comments;
            this.errorMessage = errorMessage;
        }

        public Status getStatus() { return status; }
        public List<CommentWithUser> getComments() { return comments; }
        public String getErrorMessage() { return errorMessage; }

        public static final CommentFetchState LOADING = new CommentFetchState(Status.LOADING, null, null);
        public static CommentFetchState success(List<CommentWithUser> comments) {
            return new CommentFetchState(Status.SUCCESS, comments, null);
        }
        public static CommentFetchState error(String errorMessage) {
            return new CommentFetchState(Status.ERROR, null, errorMessage);
        }
    }

    public static class CommentAddState {
        public enum Status { SUCCESS, ERROR, LOADING }
        private final Status status;
        private final String errorMessage;

        private CommentAddState(Status status, String errorMessage) {
            this.status = status;
            this.errorMessage = errorMessage;
        }

        public Status getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }

        public static final CommentAddState LOADING = new CommentAddState(Status.LOADING, null);
        public static final CommentAddState SUCCESS = new CommentAddState(Status.SUCCESS, null);
        public static CommentAddState error(String errorMessage) {
            return new CommentAddState(Status.ERROR, errorMessage);
        }
    }

    public static class PostDeleteState {
        public enum Status { SUCCESS, ERROR, LOADING }
        private final Status status;
        private final String errorMessage;

        private PostDeleteState(Status status, String errorMessage) {
            this.status = status;
            this.errorMessage = errorMessage;
        }

        public Status getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }

        public static final PostDeleteState LOADING = new PostDeleteState(Status.LOADING, null);
        public static final PostDeleteState SUCCESS = new PostDeleteState(Status.SUCCESS, null);
        public static PostDeleteState error(String errorMessage) {
            return new PostDeleteState(Status.ERROR, errorMessage);
        }
    }
}
