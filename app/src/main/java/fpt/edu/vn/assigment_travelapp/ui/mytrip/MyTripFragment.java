package fpt.edu.vn.assigment_travelapp.ui.mytrip;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.PostAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentMyTripBinding;

public class MyTripFragment extends Fragment implements PostAdapter.OnPostActionListener {

    private FragmentMyTripBinding binding;
    private MyTripViewModel viewModel;
    private PostAdapter postAdapter;
    private List<PostWithUser> postList = new ArrayList<>();
    private FirebaseUser currentUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyTripBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MyTripViewModel.class);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
                actionBar.setTitle("My Trip");
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        observeViewModel();

        binding.fabNewPost.setOnClickListener(v -> {
            NavHostFragment.findNavController(MyTripFragment.this)
                    .navigate(R.id.action_navigation_my_trip_to_newPostFragment);
        });

        binding.btnViewBookings.setOnClickListener(v -> {
            NavHostFragment.findNavController(MyTripFragment.this)
                    .navigate(R.id.action_navigation_my_trip_to_myBookingsFragment);
        });

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.fetchAllPosts();
        });

        viewModel.fetchAllPosts();
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(postList, currentUser != null ? currentUser.getUid() : "", null);
        postAdapter.setOnPostActionListener(this);
        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPosts.setAdapter(postAdapter);
    }

    private void observeViewModel() {
        viewModel.getPostFetchState().observe(getViewLifecycleOwner(), state -> {
            switch (state.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.recyclerViewPosts.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerViewPosts.setVisibility(View.VISIBLE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    postList.clear();
                    postList.addAll(state.getPosts());
                    postAdapter.notifyDataSetChanged();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Error: " + state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                postAdapter.setCurrentUser(user);
            }
        });

        viewModel.getLikeUpdate().observe(getViewLifecycleOwner(), likeUpdate -> {
            if (likeUpdate == null) return;
            for (int i = 0; i < postList.size(); i++) {
                PostWithUser pwu = postList.get(i);
                if (pwu.getPost().getPostId().equals(likeUpdate.postId)) {
                    if (likeUpdate.isLiked) {
                        pwu.getPost().getLikes().put(currentUser.getUid(), true);
                    } else {
                        pwu.getPost().getLikes().remove(currentUser.getUid());
                    }
                    postAdapter.notifyItemChanged(i);
                    break;
                }
            }
        });

        viewModel.getBookmarkUpdate().observe(getViewLifecycleOwner(), bookmarkUpdate -> {
            if (bookmarkUpdate == null) return;
            for (int i = 0; i < postList.size(); i++) {
                PostWithUser pwu = postList.get(i);
                if (pwu.getPost().getPostId().equals(bookmarkUpdate.postId)) {
                    if (bookmarkUpdate.isBookmarked) {
                        pwu.getPost().getBookmarks().put(currentUser.getUid(), true);
                    } else {
                        pwu.getPost().getBookmarks().remove(currentUser.getUid());
                    }
                    postAdapter.notifyItemChanged(i);
                    break;
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onLikeClick(int position) {
        if (currentUser != null) {
            PostWithUser postWithUser = postList.get(position);
            viewModel.toggleLike(postWithUser.getPost().getPostId(), currentUser.getUid());
        }
    }

    @Override
    public void onSaveClick(int position) {
        if (currentUser != null) {
            PostWithUser postWithUser = postList.get(position);
            viewModel.toggleBookmark(postWithUser.getPost().getPostId(), currentUser.getUid());
        }
    }

    @Override
    public void onCommentClick(int position) {
        PostWithUser postWithUser = postList.get(position);
        String postId = postWithUser.getPost().getPostId();
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        NavHostFragment.findNavController(MyTripFragment.this)
                .navigate(R.id.action_navigation_my_trip_to_commentFragment, bundle);
    }

    @Override
    public void onEditClick(int position) {
        PostWithUser postWithUser = postList.get(position);
        String postId = postWithUser.getPost().getPostId();
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        NavHostFragment.findNavController(MyTripFragment.this).navigate(R.id.action_navigation_my_trip_to_newPostFragment, bundle);
    }

    @Override
    public void onDeleteClick(int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    PostWithUser postWithUser = postList.get(position);
                    String postId = postWithUser.getPost().getPostId();
                    viewModel.deletePost(postId);
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onUserClick(String userId) {
        if (currentUser != null && userId.equals(currentUser.getUid())) {
            NavHostFragment.findNavController(this).navigate(R.id.navigation_profile);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_my_trip_to_userProfileFragment, bundle);
        }
    }
}
