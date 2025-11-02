package fpt.edu.vn.assigment_travelapp.data.model;

import java.util.HashMap;
import java.util.Map;

public class Post {
    private String postId;
    private String imageUrl;
    private String caption;
    private String userId;
    private long timestamp;
    private String location;
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

    public Post(String postId, String imageUrl, String caption, String userId, long timestamp, String location) {
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.userId = userId;
        this.timestamp = timestamp;
        this.location = location;
    }

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

