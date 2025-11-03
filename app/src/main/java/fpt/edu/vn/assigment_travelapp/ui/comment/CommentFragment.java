package fpt.edu.vn.assigment_travelapp.ui.comment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.CommentAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.Post;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentCommentBinding;

public class CommentFragment extends Fragment implements CommentAdapter.OnCommentActionListener {

    private FragmentCommentBinding binding;
    private CommentViewModel viewModel;
    private CommentAdapter commentAdapter;
    private List<CommentWithUser> commentList = new ArrayList<>();
    private String postId;
    private FirebaseUser currentUser;
    private String parentCommentId = null; // To store the ID of the comment being replied to

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCommentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        setupRecyclerView();
        loadCurrentUserAvatar();
        observeViewModel();

        binding.btnPostComment.setOnClickListener(v -> {
            String commentText = binding.etComment.getText().toString().trim();
            if (!commentText.isEmpty() && currentUser != null) {
                Comment comment = new Comment(null, postId, currentUser.getUid(), commentText, System.currentTimeMillis(), parentCommentId);
                viewModel.addComment(postId, comment);
                // Reset after sending
                cancelReply();
            }
        });

        binding.btnCancelReply.setOnClickListener(v -> cancelReply());

        if (postId != null) {
            viewModel.fetchComments(postId);
            viewModel.fetchPost(postId);
            viewModel.checkIfPostIsLiked(postId, currentUser.getUid());
            viewModel.checkIfPostIsBookmarked(postId, currentUser.getUid());
            viewModel.fetchCurrentUserDetails(currentUser.getUid());
        }
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(commentList);
        commentAdapter.setOnCommentActionListener(this);
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewComments.setAdapter(commentAdapter);
    }

    private void loadCurrentUserAvatar() {
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            loadImage(binding.ivCurrentUserAvatar, currentUser.getPhotoUrl().toString());
        }
    }

    private void observeViewModel() {
        viewModel.getPost().observe(getViewLifecycleOwner(), this::bindPostData);

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.postItemContainer.userName.setText(user.getName());
                binding.postItemContainer.userHandle.setText("@" + user.getEmail().split("@")[0]);
                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    loadImage(binding.postItemContainer.profileImage, user.getPhotoUrl());
                }
            }
        });

        viewModel.getCommentFetchState().observe(getViewLifecycleOwner(), state -> {
            if (state.getStatus() == CommentViewModel.CommentFetchState.Status.SUCCESS) {
                commentList.clear();
                commentList.addAll(state.getComments());
                commentAdapter.notifyDataSetChanged();
                binding.postItemContainer.commentCount.setText(String.valueOf(commentList.size()));
            } else if (state.getStatus() == CommentViewModel.CommentFetchState.Status.ERROR) {
                Toast.makeText(getContext(), "Error: " + state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCommentAddState().observe(getViewLifecycleOwner(), state -> {
            if (state.getStatus() == CommentViewModel.CommentAddState.Status.SUCCESS) {
                binding.etComment.setText("");
                viewModel.fetchComments(postId); // Refresh comments after adding a new one
            } else if (state.getStatus() == CommentViewModel.CommentAddState.Status.ERROR) {
                Toast.makeText(getContext(), "Error: " + state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLiked().observe(getViewLifecycleOwner(), isLiked -> {
            binding.postItemContainer.likeIcon.setSelected(isLiked);
        });

        viewModel.getIsBookmarked().observe(getViewLifecycleOwner(), isBookmarked -> {
            binding.postItemContainer.saveIcon.setSelected(isBookmarked);
        });

        viewModel.getLikeCount().observe(getViewLifecycleOwner(), likeCount -> {
            binding.postItemContainer.likeCount.setText(String.valueOf(likeCount));
        });

        viewModel.getCurrentUserDetails().observe(getViewLifecycleOwner(), user -> {
            // We have the current user's details, now we can safely bind post data
            if (viewModel.getPost().getValue() != null) {
                bindPostData(viewModel.getPost().getValue());
            }
        });

        viewModel.getPostDeleteState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case SUCCESS:
                    Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigateUp();
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Error: " + state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void bindPostData(Post post) {
        if (post != null) {
            User currentUserDetails = viewModel.getCurrentUserDetails().getValue();
            if (currentUserDetails != null) {
                boolean isAdmin = "admin".equals(currentUserDetails.getRole());
                boolean isOwner = post.getUserId().equals(currentUser.getUid());
                binding.postItemContainer.editIcon.setVisibility(isAdmin || isOwner ? View.VISIBLE : View.GONE);
                binding.postItemContainer.deleteIcon.setVisibility(isAdmin || isOwner ? View.VISIBLE : View.GONE);
            }

            binding.postItemContainer.postCaption.setText(post.getCaption());
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                binding.postItemContainer.postImageCard.setVisibility(View.VISIBLE);
                loadImage(binding.postItemContainer.postImage, post.getImageUrl());
            } else {
                binding.postItemContainer.postImageCard.setVisibility(View.GONE);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a・MMM dd, yyyy", Locale.getDefault());
            binding.postItemContainer.postDate.setText(sdf.format(new Date(post.getTimestamp())));

            viewModel.fetchUserDetails(post.getUserId());
            viewModel.fetchLikeCount(post.getPostId());

            binding.postItemContainer.likeIcon.setOnClickListener(v -> {
                viewModel.toggleLike(post.getPostId(), currentUser.getUid());
            });

            binding.postItemContainer.saveIcon.setOnClickListener(v -> {
                viewModel.toggleBookmark(post.getPostId(), currentUser.getUid());
            });

            binding.postItemContainer.editIcon.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("postId", post.getPostId());
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_commentFragment_to_newPostFragment, bundle);
            });

            binding.postItemContainer.deleteIcon.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            viewModel.deletePost(post.getPostId());
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

        }
    }

    private void loadImage(ImageView imageView, String imageString) {
        if (imageString.startsWith("http")) {
            Glide.with(this).load(imageString).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else {
            byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
            Glide.with(this).asBitmap().load(imageBytes).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onReplyClick(int position) {
        CommentWithUser commentWithUser = commentList.get(position);
        parentCommentId = commentWithUser.getComment().getCommentId();
        binding.tvReplyingTo.setText("Replying to @" + commentWithUser.getUser().getEmail().split("@")[0]);
        binding.replyingToLayout.setVisibility(View.VISIBLE);
        binding.etComment.requestFocus();
    }

    private void cancelReply() {
        parentCommentId = null;
        binding.replyingToLayout.setVisibility(View.GONE);
        binding.etComment.clearFocus();
    }
}
