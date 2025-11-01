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
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.IUserRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;

public class CommentViewModel extends ViewModel {
    private final IPostRepository postRepository;
    private final IUserRepository userRepository;
    private final MutableLiveData<CommentFetchState> commentFetchState = new MutableLiveData<>();
    private final MutableLiveData<CommentAddState> commentAddState = new MutableLiveData<>();

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
}
