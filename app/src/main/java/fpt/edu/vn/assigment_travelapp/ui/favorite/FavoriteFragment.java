package fpt.edu.vn.assigment_travelapp.ui.favorite;

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

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.PostAdapter;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentFavoriteBinding;

public class FavoriteFragment extends Fragment {

    private FragmentFavoriteBinding binding;
    private FavoriteViewModel viewModel;
    private PostAdapter adapter;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
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
            viewModel.fetchBookmarkedPosts(currentUser.getUid());
        }
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(new ArrayList<>(),
                currentUser != null ? currentUser.getUid() : null,
                new PostAdapter.OnPostActionListener() {
                    @Override
                    public void onLikeClick(String postId) {
                        Toast.makeText(getContext(), "Like clicked", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCommentClick(String postId) {
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", postId);
                        NavHostFragment.findNavController(FavoriteFragment.this)
                                .navigate(R.id.action_navigation_favorite_to_commentFragment, bundle);
                    }

                    @Override
                    public void onBookmarkClick(String postId) {
                        Toast.makeText(getContext(), "Bookmark clicked", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onEditClick(String postId) {}

                    @Override
                    public void onDeleteClick(String postId) {}

                    @Override
                    public void onPostClick(String postId) {
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", postId);
                        NavHostFragment.findNavController(FavoriteFragment.this)
                                .navigate(R.id.action_navigation_favorite_to_postDetailsFragment, bundle);
                    }
                });

        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPosts.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (currentUser != null) {
                viewModel.fetchBookmarkedPosts(currentUser.getUid());
            }
        });
    }

    private void observeViewModel() {
        viewModel.getBookmarkedPosts().observe(getViewLifecycleOwner(), posts -> {
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

