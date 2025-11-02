package fpt.edu.vn.assigment_travelapp.ui.comment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.repository.IPostRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PostRepository;

public class CommentViewModel extends ViewModel {

    private final IPostRepository postRepository;
    private final MutableLiveData<List<CommentWithUser>> comments = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> commentAdded = new MutableLiveData<>();

    public CommentViewModel() {
        postRepository = new PostRepository();
    }

    public void fetchComments(String postId) {
        postRepository.getCommentsWithUsers(postId, new IPostRepository.OnGetCommentsWithUsersCompleteListener() {
            @Override
            public void onSuccess(List<CommentWithUser> commentsList) {
                comments.postValue(commentsList);
            }

            @Override
            public void onFailure(String errorMessage) {
                CommentViewModel.this.errorMessage.postValue(errorMessage);
            }
        });
    }

    public void addComment(String postId, String userId, String text) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setText(text);
        comment.setTimestamp(System.currentTimeMillis());

        postRepository.addComment(postId, comment, new IPostRepository.OnCommentAddCompleteListener() {
            @Override
            public void onSuccess() {
                commentAdded.postValue(true);
                fetchComments(postId); // Refresh comments
            }

            @Override
            public void onFailure(String errorMessage) {
                CommentViewModel.this.errorMessage.postValue(errorMessage);
                commentAdded.postValue(false);
            }
        });
    }

    public LiveData<List<CommentWithUser>> getComments() {
        return comments;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getCommentAdded() {
        return commentAdded;
    }
}

