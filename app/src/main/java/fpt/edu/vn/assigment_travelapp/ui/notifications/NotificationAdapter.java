package fpt.edu.vn.assigment_travelapp.ui.notifications;

import android.content.Context;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.concurrent.TimeUnit;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvMessage.setText(Html.fromHtml(notification.getMessage()));
        holder.tvTime.setText(getRelativeTime(notification.getTimestamp()));

        loadImage(holder.ivAvatar, notification.getTriggeringUserAvatar());
        loadImage(holder.ivPostImage, notification.getPostImageUrl());

        if (notification.getType() != null) {
            switch (notification.getType()) {
                case "like":
                    holder.ivNotificationType.setImageResource(R.drawable.ic_like_filled);
                    break;
                case "comment":
                    holder.ivNotificationType.setImageResource(R.drawable.ic_comment);
                    break;
                case "bookmark":
                    holder.ivNotificationType.setImageResource(R.drawable.ic_bookmark_filled);
                    break;
                default:
                    holder.ivNotificationType.setVisibility(View.GONE);
                    break;
            }
        } else {
            holder.ivNotificationType.setVisibility(View.GONE);
        }
    }

    private void loadImage(ImageView imageView, String imageSource) {
        if (imageSource == null || imageSource.isEmpty()) {
            imageView.setVisibility(View.GONE);
            return;
        }
        imageView.setVisibility(View.VISIBLE);

        // If it looks like a URL/URI, let Glide handle it.
        if (imageSource.startsWith("http://") || imageSource.startsWith("https://") || imageSource.startsWith("content://") || imageSource.startsWith("file://")) {
            Glide.with(context).load(imageSource).into(imageView);
        } else {
            // Otherwise, assume it's Base64 (either raw or with data URI)
            String base64String = imageSource;
            if (imageSource.startsWith("data:image")) {
                // It's a data URI, extract the Base64 part
                String[] parts = imageSource.split(",");
                if (parts.length == 2) {
                    base64String = parts[1];
                } else {
                    imageView.setVisibility(View.GONE);
                    return;
                }
            }

            try {
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                Glide.with(context)
                        .asBitmap()
                        .load(decodedBytes)
                        .into(imageView);
            } catch (IllegalArgumentException e) {
                // Failed to decode. Hide image to avoid loading garbage.
                imageView.setVisibility(View.GONE);
            }
        }
    }

    private String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return seconds + "s ago";
        }
    }


    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notificationList.clear();
        this.notificationList.addAll(newNotifications);
        notifyDataSetChanged();
    }

    public String getNotificationId(int position) {
        return notificationList.get(position).getId();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        ImageView ivAvatar, ivPostImage, ivNotificationType;
        TextView tvMessage, tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivPostImage = itemView.findViewById(R.id.iv_post_image);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivNotificationType = itemView.findViewById(R.id.iv_notification_type);
        }
    }
}
