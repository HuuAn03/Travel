package fpt.edu.vn.assigment_travelapp.ui.postdetails;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.data.model.Post;
import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentPostDetailsBinding;
import fpt.edu.vn.assigment_travelapp.ui.mytrip.MyTripViewModel;

public class PostDetailsFragment extends Fragment {

    private FragmentPostDetailsBinding binding;
    private PostDetailsViewModel viewModel;
    private MyTripViewModel myTripViewModel;
    private String postId;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostDetailsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(PostDetailsViewModel.class);
        myTripViewModel = new ViewModelProvider(this).get(MyTripViewModel.class);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (postId != null) {
            viewModel.fetchPostDetails(postId);
        }

        observeViewModel();

        binding.likeIcon.setOnClickListener(v -> {
            if (currentUser != null && postId != null) {
                myTripViewModel.toggleLike(postId, currentUser.getUid());
            }
        });

        binding.commentIcon.setOnClickListener(v -> {
            if (postId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("postId", postId);
                NavHostFragment.findNavController(PostDetailsFragment.this)
                        .navigate(R.id.action_postDetailsFragment_to_commentFragment, bundle);
            }
        });

        binding.saveIcon.setOnClickListener(v -> {
            if (currentUser != null && postId != null) {
                myTripViewModel.toggleBookmark(postId, currentUser.getUid());
            }
        });
    }

    private void observeViewModel() {
        viewModel.getPostDetailsState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.contentLayout.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.contentLayout.setVisibility(View.VISIBLE);
                    PostWithUser postWithUser = state.getPostWithUser();
                    if (postWithUser != null) {
                        bindPostData(postWithUser);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.contentLayout.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void bindPostData(PostWithUser postWithUser) {
        Post post = postWithUser.getPost();
        fpt.edu.vn.assigment_travelapp.data.model.User user = postWithUser.getUser();

        if (post == null || user == null) return;

        binding.tvUsername.setText(user.getName() != null ? user.getName() : "Unknown User");
        binding.tvPostCaption.setText(post.getCaption());
        binding.tvPostDate.setText(android.text.format.DateUtils.getRelativeTimeSpanString(
                post.getTimestamp(), System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS).toString());

        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            binding.tvLocation.setText(post.getLocation());
            binding.tvLocation.setVisibility(View.VISIBLE);
        } else {
            binding.tvLocation.setVisibility(View.GONE);
        }

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(post.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(binding.ivPostImage);
        } else {
            binding.ivPostImage.setImageResource(R.drawable.image_placeholder);
        }

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(this).load(user.getPhotoUrl()).into(binding.ivAvatar);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        if (currentUser != null) {
            Map<String, Boolean> likes = post.getLikes();
            binding.likeIcon.setSelected(likes != null && likes.containsKey(currentUser.getUid()));

            Map<String, Boolean> bookmarks = post.getBookmarks();
            binding.saveIcon.setSelected(bookmarks != null && bookmarks.containsKey(currentUser.getUid()));
        }

        int numLikes = post.getLikes() != null ? post.getLikes().size() : 0;
        binding.likeCount.setText(String.valueOf(numLikes));

        int numComments = post.getComments() != null ? post.getComments().size() : 0;
        binding.commentCount.setText(String.valueOf(numComments));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

