package fpt.edu.vn.assigment_travelapp.ui.home;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.PostAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.PostWithUser;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private PostAdapter adapter;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
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
            viewModel.fetchFeedPosts(currentUser.getUid());
        } else {
            viewModel.fetchFeedPosts(null);
        }
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(new ArrayList<>(), 
                currentUser != null ? currentUser.getUid() : null,
                new PostAdapter.OnPostActionListener() {
                    @Override
                    public void onLikeClick(String postId) {
                        // Handle like
                        Toast.makeText(getContext(), "Like clicked", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCommentClick(String postId) {
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", postId);
                        NavHostFragment.findNavController(HomeFragment.this)
                                .navigate(R.id.action_navigation_home_to_commentFragment, bundle);
                    }

                    @Override
                    public void onBookmarkClick(String postId) {
                        // Handle bookmark
                        Toast.makeText(getContext(), "Bookmark clicked", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onEditClick(String postId) {
                        // Only for own posts
                    }

                    @Override
                    public void onDeleteClick(String postId) {
                        // Only for own posts
                    }

                    @Override
                    public void onPostClick(String postId) {
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", postId);
                        NavHostFragment.findNavController(HomeFragment.this)
                                .navigate(R.id.action_navigation_home_to_postDetailsFragment, bundle);
                    }
                });

        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPosts.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (currentUser != null) {
                viewModel.fetchFeedPosts(currentUser.getUid());
            } else {
                viewModel.fetchFeedPosts(null);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter.updatePosts(posts);
                
                // Show/hide empty state
                if (posts.isEmpty()) {
                    binding.recyclerViewPosts.setVisibility(View.GONE);
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerViewPosts.setVisibility(View.VISIBLE);
                    binding.layoutEmptyState.setVisibility(View.GONE);
                }
            }
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

