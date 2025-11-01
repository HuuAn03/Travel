package fpt.edu.vn.assigment_travelapp.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.User;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<CommentWithUser> commentList;
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onReplyClick(int position);
    }

    public void setOnCommentActionListener(OnCommentActionListener listener) {
        this.listener = listener;
    }

    public CommentAdapter(List<CommentWithUser> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentWithUser commentWithUser = commentList.get(position);
        holder.bind(commentWithUser);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView ivAvatar;
        private final TextView tvUsername;
        private final TextView tvHandle;
        private final TextView tvCommentContent;
        private final TextView tvTimestamp;
        private final ImageView ivReply;
        private final TextView tvViewReplies;
        private final RecyclerView rvReplies;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvHandle = itemView.findViewById(R.id.tv_handle);
            tvCommentContent = itemView.findViewById(R.id.tv_comment_content);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivReply = itemView.findViewById(R.id.iv_reply);
            tvViewReplies = itemView.findViewById(R.id.tv_view_replies);
            rvReplies = itemView.findViewById(R.id.rv_replies);

            ivReply.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onReplyClick(position);
                }
            });
        }

        public void bind(CommentWithUser commentWithUser) {
            User user = commentWithUser.getUser();
            Comment comment = commentWithUser.getComment();

            tvUsername.setText(user.getName());
            tvHandle.setText("@" + user.getEmail().split("@")[0]); // Simplified handle
            tvCommentContent.setText(comment.getText());

            // Format and set timestamp
            tvTimestamp.setText(getRelativeTime(comment.getTimestamp()));

            Glide.with(itemView.getContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(ivAvatar);

            // Handle replies
            List<CommentWithUser> replies = commentWithUser.getReplies();
            if (replies != null && !replies.isEmpty()) {
                tvViewReplies.setVisibility(View.VISIBLE);
                tvViewReplies.setText("View " + replies.size() + " replies");

                CommentAdapter replyAdapter = new CommentAdapter(replies);
                replyAdapter.setOnCommentActionListener(listener);
                rvReplies.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                rvReplies.setAdapter(replyAdapter);

                tvViewReplies.setOnClickListener(v -> {
                    if (rvReplies.getVisibility() == View.GONE) {
                        rvReplies.setVisibility(View.VISIBLE);
                        tvViewReplies.setText("Hide replies");
                    } else {
                        rvReplies.setVisibility(View.GONE);
                        tvViewReplies.setText("View " + replies.size() + " replies");
                    }
                });

            } else {
                tvViewReplies.setVisibility(View.GONE);
                rvReplies.setVisibility(View.GONE);
            }
        }

        private String getRelativeTime(long timestamp) {
            return DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        }
    }
}
