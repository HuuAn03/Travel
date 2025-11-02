package fpt.edu.vn.assigment_travelapp.ui.mytrip;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.PostAdapter;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentMyTripBinding;

public class MyTripFragment extends Fragment {

    private FragmentMyTripBinding binding;
    private MyTripViewModel viewModel;
    private PostAdapter adapter;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyTripBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MyTripViewModel.class);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSwipeRefresh();
        observeViewModel();

        if (currentUser != null) {
            viewModel.fetchMyPosts(currentUser.getUid());
        }
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(new ArrayList<>(),
                currentUser != null ? currentUser.getUid() : null,
                new PostAdapter.OnPostActionListener() {
                    @Override
                    public void onLikeClick(String postId) {
                        if (currentUser != null) {
                            viewModel.toggleLike(postId, currentUser.getUid());
                        }
                    }

                    @Override
                    public void onCommentClick(String postId) {
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", postId);
                        NavHostFragment.findNavController(MyTripFragment.this)
                                .navigate(R.id.action_navigation_my_trip_to_commentFragment, bundle);
                    }

                    @Override
                    public void onBookmarkClick(String postId) {
                        if (currentUser != null) {
                            viewModel.toggleBookmark(postId, currentUser.getUid());
                        }
                    }

                    @Override
                    public void onEditClick(String postId) {
                        showEditDialog(postId);
                    }

                    @Override
                    public void onDeleteClick(String postId) {
                        showDeleteDialog(postId);
                    }

                    @Override
                    public void onPostClick(String postId) {
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", postId);
                        NavHostFragment.findNavController(MyTripFragment.this)
                                .navigate(R.id.action_navigation_my_trip_to_postDetailsFragment, bundle);
                    }
                });

        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPosts.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (currentUser != null) {
                viewModel.fetchMyPosts(currentUser.getUid());
            }
        });
    }

    private void showEditDialog(String postId) {
        // Implementation for edit dialog
        Toast.makeText(getContext(), "Edit functionality", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteDialog(String postId) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deletePost(postId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getMyPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter.updatePosts(posts);
            }
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

