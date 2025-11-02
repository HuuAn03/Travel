package fpt.edu.vn.assigment_travelapp.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fpt.edu.vn.assigment_travelapp.data.model.Notification;

public class NotificationRepository {
    private final DatabaseReference mDatabase;

    public NotificationRepository() {
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    public void createNotification(String userId, String fromUserId, String type, String postId, String message) {
        String notificationId = mDatabase.child("notifications").child(userId).push().getKey();
        if (notificationId != null) {
            Notification notification = new Notification(
                    notificationId,
                    userId,
                    fromUserId,
                    type,
                    postId,
                    message,
                    System.currentTimeMillis()
            );
            mDatabase.child("notifications").child(userId).child(notificationId).setValue(notification);
        }
    }

    public void markAsRead(String userId, String notificationId) {
        mDatabase.child("notifications").child(userId).child(notificationId).child("read").setValue(true);
    }
}

