package fpt.edu.vn.assigment_travelapp.data.model;

public class Comment {
    private String commentId;
    private String postId;
    private String userId;
    private String text;
    private long timestamp;
    private String parentCommentId; // Null if it's a top-level comment

    public Comment() {
        // Default constructor for Firebase
    }

    public Comment(String commentId, String postId, String userId, String text, long timestamp, String parentCommentId) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
        this.parentCommentId = parentCommentId;
    }

    // Getters and Setters
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}
