package fpt.edu.vn.assigment_travelapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.ProfilePostAdapter;
import fpt.edu.vn.assigment_travelapp.data.model.Post;

public class BookmarksFragment extends Fragment implements ProfilePostAdapter.OnItemClickListener {

    private static final String ARG_USER_ID = "userId";
    private RecyclerView recyclerView;
    private ProfilePostAdapter adapter;
    private ProfileViewModel profileViewModel;
    private String userId;

    public static BookmarksFragment newInstance(String userId) {
        BookmarksFragment fragment = new BookmarksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        recyclerView = view.findViewById(R.id.rv_posts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ProfilePostAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        if (userId != null) {
            profileViewModel.loadBookmarkedPosts(userId);
        }

        profileViewModel.bookmarkedPosts.observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter.setPosts(posts);
            }
        });
    }

    @Override
    public void onItemClick(Post post) {
        Bundle bundle = new Bundle();
        bundle.putString("postId", post.getPostId());
        NavHostFragment.findNavController(requireParentFragment()).navigate(R.id.commentFragment, bundle);
    }
}
