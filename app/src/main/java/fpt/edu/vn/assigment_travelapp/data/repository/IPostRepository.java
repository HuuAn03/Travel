package fpt.edu.vn.assigment_travelapp.data.repository;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;

public interface IPostRepository {
    void createPost(String base64Image, String caption, OnPostCreationCompleteListener listener);
    void getAllPosts(OnGetAllPostsCompleteListener listener);
    void addComment(String postId, Comment comment, OnCommentAddCompleteListener listener);
    void getCommentsWithUsers(String postId, OnGetCommentsWithUsersCompleteListener listener);

    interface OnPostCreationCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnGetAllPostsCompleteListener {
        void onSuccess(List<PostWithUser> posts);
        void onFailure(String errorMessage);
    }

    interface OnCommentAddCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnGetCommentsWithUsersCompleteListener {
        void onSuccess(List<CommentWithUser> comments);
        void onFailure(String errorMessage);
    }
}
