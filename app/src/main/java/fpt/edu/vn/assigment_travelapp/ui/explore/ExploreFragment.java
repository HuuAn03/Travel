package fpt.edu.vn.assigment_travelapp.ui.explore;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.PostAdapter;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentExploreBinding;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private ExploreViewModel viewModel;
    private PostAdapter adapter;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchBar();
        observeViewModel();
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
                        NavHostFragment.findNavController(ExploreFragment.this)
                                .navigate(R.id.action_navigation_explore_to_commentFragment, bundle);
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
                        NavHostFragment.findNavController(ExploreFragment.this)
                                .navigate(R.id.action_navigation_explore_to_postDetailsFragment, bundle);
                    }
                });

        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewPosts.setAdapter(adapter);
    }

    private void setupSearchBar() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    viewModel.searchPosts(query);
                } else if (query.isEmpty()) {
                    adapter.updatePosts(new ArrayList<>());
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter.updatePosts(posts);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

