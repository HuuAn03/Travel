package fpt.edu.vn.assigment_travelapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable {
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

    protected Comment(Parcel in) {
        commentId = in.readString();
        postId = in.readString();
        userId = in.readString();
        text = in.readString();
        timestamp = in.readLong();
        parentCommentId = in.readString();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(commentId);
        dest.writeString(postId);
        dest.writeString(userId);
        dest.writeString(text);
        dest.writeLong(timestamp);
        dest.writeString(parentCommentId);
    }
}
