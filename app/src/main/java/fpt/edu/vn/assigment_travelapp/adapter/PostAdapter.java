package fpt.edu.vn.assigment_travelapp.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
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

import de.hdodenhof.circleimageview.CircleImageView;
import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Post;
import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.User;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<PostWithUser> posts;
    private String currentUserId;
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onLikeClick(String postId);
        void onCommentClick(String postId);
        void onBookmarkClick(String postId);
        void onEditClick(String postId);
        void onDeleteClick(String postId);
        void onPostClick(String postId);
    }

    public PostAdapter(List<PostWithUser> posts, String currentUserId, OnPostActionListener listener) {
        this.posts = posts;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostWithUser postWithUser = posts.get(position);
        holder.bind(postWithUser);
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    public void updatePosts(List<PostWithUser> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    public void removePost(String postId) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getPost().getPostId().equals(postId)) {
                posts.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, posts.size());
                break;
            }
        }
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivAvatar;
        private TextView tvUsername;
        private ImageView ivPostImage;
        private TextView tvPostCaption;
        private TextView tvPostDate;
        private ImageView likeIcon;
        private TextView likeCount;
        private ImageView commentIcon;
        private TextView commentCount;
        private ImageView saveIcon;
        private ImageView editIcon;
        private ImageView deleteIcon;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            ivPostImage = itemView.findViewById(R.id.iv_post_image);
            tvPostCaption = itemView.findViewById(R.id.tv_post_caption);
            tvPostDate = itemView.findViewById(R.id.tv_post_date);
            likeIcon = itemView.findViewById(R.id.like_icon);
            likeCount = itemView.findViewById(R.id.like_count);
            commentIcon = itemView.findViewById(R.id.comment_icon);
            commentCount = itemView.findViewById(R.id.comment_count);
            saveIcon = itemView.findViewById(R.id.save_icon);
            editIcon = itemView.findViewById(R.id.edit_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }

        public void bind(PostWithUser postWithUser) {
            if (postWithUser == null) return;

            Post post = postWithUser.getPost();
            User user = postWithUser.getUser();

            if (post == null || user == null) return;

            tvUsername.setText(user.getName() != null ? user.getName() : "Unknown User");

            if (!TextUtils.isEmpty(post.getImageUrl())) {
                // Check if it's a Base64 string or a URL
                if (post.getImageUrl().startsWith("data:image") || post.getImageUrl().length() > 1000) {
                    // Base64 image (legacy support)
                    try {
                        byte[] decodedString = Base64.decode(post.getImageUrl(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        ivPostImage.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        ivPostImage.setImageResource(R.drawable.image_placeholder);
                    }
                } else {
                    // Firebase Storage URL
                    Glide.with(itemView.getContext())
                            .load(post.getImageUrl())
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.image_placeholder)
                            .into(ivPostImage);
                }
            } else {
                ivPostImage.setImageResource(R.drawable.image_placeholder);
            }

            if (!TextUtils.isEmpty(user.getPhotoUrl())) {
                Glide.with(itemView.getContext()).load(user.getPhotoUrl()).into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            tvPostDate.setText(getRelativeTime(post.getTimestamp()));

            setSeeMoreText(tvPostCaption, post.getCaption());

            likeIcon.setSelected(post.getLikes() != null && post.getLikes().containsKey(currentUserId));
            saveIcon.setSelected(post.getBookmarks() != null && post.getBookmarks().containsKey(currentUserId));

            int numLikes = post.getLikes() != null ? post.getLikes().size() : 0;
            likeCount.setText(String.valueOf(numLikes));

            int numComments = post.getComments() != null ? post.getComments().size() : 0;
            commentCount.setText(String.valueOf(numComments));

            // Show edit/delete buttons only for posts owned by current user
            boolean isOwner = post.getUserId() != null && post.getUserId().equals(currentUserId);
            if (editIcon != null) {
                editIcon.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            }
            if (deleteIcon != null) {
                deleteIcon.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            }

            // Click listeners
            likeIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClick(post.getPostId());
                }
            });

            commentIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentClick(post.getPostId());
                }
            });

            saveIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookmarkClick(post.getPostId());
                }
            });

            if (editIcon != null) {
                editIcon.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditClick(post.getPostId());
                    }
                });
            }

            if (deleteIcon != null) {
                deleteIcon.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClick(post.getPostId());
                    }
                });
            }

            ivPostImage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post.getPostId());
                }
            });
        }

        private void setSeeMoreText(TextView textView, String text) {
            if (TextUtils.isEmpty(text)) {
                textView.setText("");
                return;
            }

            textView.setText(text);
            textView.setMaxLines(Integer.MAX_VALUE);
        }

        private String getRelativeTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < 60000) { // Less than 1 minute
                return "just now";
            } else if (diff < 3600000) { // Less than 1 hour
                return (diff / 60000) + "m ago";
            } else if (diff < 86400000) { // Less than 1 day
                return (diff / 3600000) + "h ago";
            } else if (diff < 604800000) { // Less than 1 week
                return (diff / 86400000) + "d ago";
            } else {
                return android.text.format.DateUtils.getRelativeTimeSpanString(
                        timestamp, now, android.text.format.DateUtils.WEEK_IN_MILLIS).toString();
            }
        }
    }
}

