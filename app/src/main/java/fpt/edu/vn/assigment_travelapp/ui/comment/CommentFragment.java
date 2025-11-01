package fpt.edu.vn.assigment_travelapp.ui.comment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.CommentAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Comment;
import fpt.edu.vn.assigment_travelapp.data.model.CommentWithUser;
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
                // Reset parentCommentId after sending
                parentCommentId = null;
                binding.tvReplyingTo.setVisibility(View.GONE);
            }
        });

        if (postId != null) {
            viewModel.fetchComments(postId);
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
            Glide.with(this)
                .load(currentUser.getPhotoUrl())
                .placeholder(R.drawable.ic_default_avatar)
                .into(binding.ivCurrentUserAvatar);
        }
    }

    private void observeViewModel() {
        viewModel.getCommentFetchState().observe(getViewLifecycleOwner(), state -> {
            if (state.getStatus() == CommentViewModel.CommentFetchState.Status.SUCCESS) {
                commentList.clear();
                commentList.addAll(state.getComments());
                commentAdapter.notifyDataSetChanged();
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
        binding.tvReplyingTo.setVisibility(View.VISIBLE);
        binding.etComment.requestFocus();
    }
}
