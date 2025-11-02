package fpt.edu.vn.assigment_travelapp.ui.comment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.CommentAdapter;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentCommentBinding;

public class CommentFragment extends Fragment {

    private FragmentCommentBinding binding;
    private CommentViewModel viewModel;
    private CommentAdapter adapter;
    private FirebaseUser currentUser;
    private String postId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSendButton();
        observeViewModel();

        if (postId != null) {
            viewModel.fetchComments(postId);
        }
    }

    private void setupRecyclerView() {
        adapter = new CommentAdapter(new ArrayList<>());
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewComments.setAdapter(adapter);
    }

    private void setupSendButton() {
        binding.buttonSend.setOnClickListener(v -> {
            String commentText = binding.editTextComment.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) {
                Toast.makeText(getContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser != null && postId != null) {
                viewModel.addComment(postId, currentUser.getUid(), commentText);
                binding.editTextComment.setText("");
            }
        });
    }

    private void observeViewModel() {
        viewModel.getComments().observe(getViewLifecycleOwner(), comments -> {
            if (comments != null) {
                adapter.updateComments(comments);
            }
        });

        viewModel.getCommentAdded().observe(getViewLifecycleOwner(), added -> {
            if (added != null && added) {
                // Comment added successfully, UI will update automatically
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

