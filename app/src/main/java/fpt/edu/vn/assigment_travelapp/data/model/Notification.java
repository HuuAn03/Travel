package fpt.edu.vn.assigment_travelapp.data.model;

public class Notification {
    private String id;
    private String type;
    private String triggeringUserId;
    private String triggeringUserAvatar;
    private String message;
    private String postId;
    private String postImageUrl;
    private long timestamp;
    private boolean read;

    public Notification() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggeringUserId() {
        return triggeringUserId;
    }

    public void setTriggeringUserId(String triggeringUserId) {
        this.triggeringUserId = triggeringUserId;
    }

    public String getTriggeringUserAvatar() {
        return triggeringUserAvatar;
    }

    public void setTriggeringUserAvatar(String triggeringUserAvatar) {
        this.triggeringUserAvatar = triggeringUserAvatar;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
