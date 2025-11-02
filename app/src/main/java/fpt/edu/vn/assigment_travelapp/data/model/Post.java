package fpt.edu.vn.assigment_travelapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Post implements Parcelable {
    private String postId;
    private String imageUrl;
    private String caption;
    private String userId;
    private long timestamp;
    private Map<String, Boolean> likes = new HashMap<>();
    private Map<String, Boolean> bookmarks = new HashMap<>();
    private Map<String, Comment> comments = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String postId, String imageUrl, String caption, String userId, long timestamp) {
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    protected Post(Parcel in) {
        postId = in.readString();
        imageUrl = in.readString();
        caption = in.readString();
        userId = in.readString();
        timestamp = in.readLong();
        in.readMap(likes, Map.class.getClassLoader());
        in.readMap(bookmarks, Map.class.getClassLoader());
        in.readMap(comments, Comment.class.getClassLoader());
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public Map<String, Boolean> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(Map<String, Boolean> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public Map<String, Comment> getComments() {
        return comments;
    }

    public void setComments(Map<String, Comment> comments) {
        this.comments = comments;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postId);
        dest.writeString(imageUrl);
        dest.writeString(caption);
        dest.writeString(userId);
        dest.writeLong(timestamp);
        dest.writeMap(likes);
        dest.writeMap(bookmarks);
        dest.writeMap(comments);
    }
}
