package fpt.edu.vn.assigment_travelapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.User;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<CommentWithUser> comments;

    public CommentAdapter(List<CommentWithUser> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentWithUser commentWithUser = comments.get(position);
        holder.bind(commentWithUser);
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public void updateComments(List<CommentWithUser> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivAvatar;
        private TextView tvUsername;
        private TextView tvCommentText;
        private TextView tvCommentDate;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvCommentText = itemView.findViewById(R.id.tv_comment_text);
            tvCommentDate = itemView.findViewById(R.id.tv_comment_date);
        }

        public void bind(CommentWithUser commentWithUser) {
            if (commentWithUser == null) return;

            Comment comment = commentWithUser.getComment();
            User user = commentWithUser.getUser();

            if (comment == null || user == null) return;

            tvUsername.setText(user.getName() != null ? user.getName() : "Unknown User");
            tvCommentText.setText(comment.getText());

            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(user.getPhotoUrl()).into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            tvCommentDate.setText(getRelativeTime(comment.getTimestamp()));
        }

        private String getRelativeTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < 60000) {
                return "just now";
            } else if (diff < 3600000) {
                return (diff / 60000) + "m ago";
            } else if (diff < 86400000) {
                return (diff / 3600000) + "h ago";
            } else if (diff < 604800000) {
                return (diff / 86400000) + "d ago";
            } else {
                return android.text.format.DateUtils.getRelativeTimeSpanString(
                        timestamp, now, android.text.format.DateUtils.WEEK_IN_MILLIS).toString();
            }
        }
    }
}

