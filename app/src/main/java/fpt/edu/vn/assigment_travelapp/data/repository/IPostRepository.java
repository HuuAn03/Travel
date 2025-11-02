package fpt.edu.vn.assigment_travelapp.data.repository;

import android.net.Uri;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;

public interface IPostRepository {
    void createPost(String imageUrl, String caption, String location, OnPostCreationCompleteListener listener);
    void createPostWithUri(Uri imageUri, String caption, String location, OnPostCreationCompleteListener listener);
    void getAllPosts(OnGetAllPostsCompleteListener listener);
    void getPostsByUser(String userId, OnGetAllPostsCompleteListener listener);
    void getBookmarkedPosts(String userId, OnGetAllPostsCompleteListener listener);
    void getPostsByFollowedUsers(String userId, OnGetAllPostsCompleteListener listener);
    void searchPosts(String query, OnGetAllPostsCompleteListener listener);
    void updatePost(String postId, String caption, String location, OnPostUpdateCompleteListener listener);
    void updatePostImage(String postId, Uri imageUri, OnPostUpdateCompleteListener listener);
    void deletePost(String postId, OnPostDeleteCompleteListener listener);
    void addComment(String postId, Comment comment, OnCommentAddCompleteListener listener);
    void deleteComment(String postId, String commentId, OnCommentDeleteCompleteListener listener);
    void getCommentsWithUsers(String postId, OnGetCommentsWithUsersCompleteListener listener);

    interface OnPostCreationCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnPostUpdateCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnPostDeleteCompleteListener {
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

    interface OnCommentDeleteCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnGetCommentsWithUsersCompleteListener {
        void onSuccess(List<CommentWithUser> comments);
        void onFailure(String errorMessage);
    }
}

