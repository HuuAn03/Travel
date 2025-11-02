package fpt.edu.vn.assigment_travelapp.data.model;

public class Notification {
    private String notificationId;
    private String userId; // User who receives the notification
    private String fromUserId; // User who triggered the notification
    private String type; // "like", "comment", "follow"
    private String postId; // For like/comment notifications
    private String message;
    private long timestamp;
    private boolean read;

    public Notification() {
    }

    public Notification(String notificationId, String userId, String fromUserId, String type, String postId, String message, long timestamp) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.fromUserId = fromUserId;
        this.type = type;
        this.postId = postId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

