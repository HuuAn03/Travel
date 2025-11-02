package fpt.edu.vn.assigment_travelapp.data.model;

public class NotificationWithUser {
    private Notification notification;
    private User fromUser;

    public NotificationWithUser() {
    }

    public NotificationWithUser(Notification notification, User fromUser) {
        this.notification = notification;
        this.fromUser = fromUser;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }
}

