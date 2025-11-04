package fpt.edu.vn.assigment_travelapp.data.repository;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.Post;
import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;

public interface IPostRepository {
    void createPost(String base64Image, String caption, OnPostCreationCompleteListener listener);
    void getAllPosts(OnGetAllPostsCompleteListener listener);
    void getPostsByUserId(String userId, OnGetPostsCompleteListener listener);
    void getLikedPosts(String userId, OnGetPostsCompleteListener listener);
    void getBookmarkedPosts(String userId, OnGetPostsCompleteListener listener);
    void addComment(String postId, Comment comment, OnCommentAddCompleteListener listener);
    void getCommentsWithUsers(String postId, OnGetCommentsWithUsersCompleteListener listener);
    void deletePost(String postId, OnDeletePostCompleteListener listener);
    void getPost(String postId, OnGetPostCompleteListener listener);
    void updatePost(String postId, String base64Image, String caption, OnPostUpdateCompleteListener listener);

    void getLikeCount(String postId, OnLikeCountCompleteListener listener);
    void isLiked(String postId, String userId, OnIsLikedCompleteListener listener);
    void isBookmarked(String postId, String userId, OnIsBookmarkedCompleteListener listener);
    void toggleLike(String postId, String userId, OnLikeToggleCompleteListener listener);
    void toggleBookmark(String postId, String userId, OnBookmarkToggleCompleteListener listener);


    interface OnPostCreationCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnGetAllPostsCompleteListener {
        void onSuccess(List<PostWithUser> posts);
        void onFailure(String errorMessage);
    }

    interface OnGetPostsCompleteListener {
        void onSuccess(List<Post> posts);
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

    interface OnDeletePostCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnGetPostCompleteListener {
        void onSuccess(Post post);
        void onFailure(String errorMessage);
    }

    interface OnPostUpdateCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    interface OnLikeCountCompleteListener {
        void onComplete(long count);
    }

    interface OnIsLikedCompleteListener {
        void onComplete(boolean isLiked);
    }

    interface OnIsBookmarkedCompleteListener {
        void onComplete(boolean isBookmarked);
    }

    interface OnLikeToggleCompleteListener {
        void onComplete(boolean isSet, int newCount);
    }

    interface OnBookmarkToggleCompleteListener {
        void onComplete(boolean isSet);
    }
}
