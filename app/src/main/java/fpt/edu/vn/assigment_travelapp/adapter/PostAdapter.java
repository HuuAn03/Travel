package fpt.edu.vn.assigment_travelapp.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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

    private final List<PostWithUser> postList;
    private final String currentUserId;
    private final User currentUser;
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onLikeClick(int position);
        void onSaveClick(int position);
        void onCommentClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnPostActionListener(OnPostActionListener listener) {
        this.listener = listener;
    }

    public PostAdapter(List<PostWithUser> postList, String currentUserId, User currentUser) {
        this.postList = postList;
        this.currentUserId = currentUserId;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostWithUser postWithUser = postList.get(position);
        holder.bind(postWithUser);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPostImage;
        private final CircleImageView ivAvatar;
        private final TextView tvUsername;
        private final ImageView likeIcon;
        private final TextView likeCount;
        private final ImageView commentIcon;
        private final TextView commentCount;
        private final ImageView saveIcon;
        private final TextView tvPostCaption;
        private final TextView tvPostDate;
        private final ImageView editIcon;
        private final ImageView deleteIcon;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPostImage = itemView.findViewById(R.id.post_image);
            ivAvatar = itemView.findViewById(R.id.profile_image);
            tvUsername = itemView.findViewById(R.id.user_name);
            likeIcon = itemView.findViewById(R.id.like_icon);
            likeCount = itemView.findViewById(R.id.like_count);
            commentIcon = itemView.findViewById(R.id.comment_icon);
            commentCount = itemView.findViewById(R.id.comment_count);
            saveIcon = itemView.findViewById(R.id.save_icon);
            tvPostCaption = itemView.findViewById(R.id.post_caption);
            tvPostDate = itemView.findViewById(R.id.post_date);
            editIcon = itemView.findViewById(R.id.edit_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon);

            likeIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onLikeClick(position);
                }
            });

            commentIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onCommentClick(position);
                }
            });

            saveIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onSaveClick(position);
                }
            });

            editIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(position);
                }
            });

            deleteIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(position);
                }
            });
        }

        public void bind(PostWithUser postWithUser) {
            if (postWithUser == null) return;

            Post post = postWithUser.getPost();
            User user = postWithUser.getUser();

            if (post == null || user == null) return;

            tvUsername.setText(user.getName() != null ? user.getName() : "Unknown User");

            if (!TextUtils.isEmpty(post.getImageUrl())) {
                try {
                    byte[] decodedString = Base64.decode(post.getImageUrl(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivPostImage.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    ivPostImage.setImageResource(R.drawable.image_placeholder);
                }
            } else {
                ivPostImage.setImageResource(R.drawable.image_placeholder);
            }

            if (!TextUtils.isEmpty(user.getPhotoUrl())) {
                Glide.with(itemView.getContext()).load(user.getPhotoUrl()).into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            // Set timestamp
            tvPostDate.setText(getRelativeTime(post.getTimestamp()));

            // Set caption with 'see more' functionality
            setSeeMoreText(tvPostCaption, post.getCaption());

            likeIcon.setSelected(post.getLikes() != null && post.getLikes().containsKey(currentUserId));
            saveIcon.setSelected(post.getBookmarks() != null && post.getBookmarks().containsKey(currentUserId));

            int numLikes = post.getLikes() != null ? post.getLikes().size() : 0;
            likeCount.setText(String.valueOf(numLikes));

            int numComments = post.getComments() != null ? post.getComments().size() : 0;
            commentCount.setText(String.valueOf(numComments));

            // Show/hide edit and delete icons
            if (currentUser != null && ("admin".equals(currentUser.getRole()) || post.getUserId().equals(currentUserId))) {
                editIcon.setVisibility(View.VISIBLE);
                deleteIcon.setVisibility(View.VISIBLE);
            } else {
                editIcon.setVisibility(View.GONE);
                deleteIcon.setVisibility(View.GONE);
            }
        }

        private String getRelativeTime(long timestamp) {
            return DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        }

        private void setSeeMoreText(final TextView textView, final String text) {
            if (TextUtils.isEmpty(text)) {
                textView.setVisibility(View.GONE);
                return;
            }
            textView.setVisibility(View.VISIBLE);
            textView.setText(text); // Set full text first
            textView.post(() -> {
                final int maxLines = 2;
                if (textView.getLineCount() > maxLines) {
                    int lineEndIndex = textView.getLayout().getLineEnd(maxLines - 1);
                    String seeMore = "... see more";
                    String textToShow = text.substring(0, lineEndIndex - seeMore.length()) + seeMore;

                    SpannableString spannableString = new SpannableString(textToShow);
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            textView.setMaxLines(Integer.MAX_VALUE);
                            textView.setText(text);
                            textView.setMovementMethod(null);
                        }
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            ds.setColor(ds.linkColor);
                        }
                    };
                    spannableString.setSpan(clickableSpan, textToShow.length() - seeMore.length(), textToShow.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(spannableString);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    textView.setMaxLines(maxLines); // Set max lines after setting the spannable
                }
            });
        }
    }
}
