package fpt.edu.vn.assigment_travelapp.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Notification;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
    private final DatabaseReference notificationsRef;
    private final FirebaseAuth mAuth;

    public NotificationsViewModel() {
        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        notificationsRef = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("notifications").child(currentUserId);
    }

    public LiveData<List<Notification>> getNotifications() {
        return notifications;
    }

    public void loadNotifications() {
        notificationsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Notification> notificationList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null) {
                        notificationList.add(notification);
                    }
                }
                Collections.reverse(notificationList);
                notifications.setValue(notificationList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public void deleteNotification(String notificationId) {
        notificationsRef.child(notificationId).removeValue();
    }
}
