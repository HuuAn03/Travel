package fpt.edu.vn.assigment_travelapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.adapter.ProfilePostAdapter;

public class MyPostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfilePostAdapter adapter;
    private ProfileViewModel profileViewModel;

    public static MyPostsFragment newInstance() {
        return new MyPostsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);
        recyclerView = view.findViewById(R.id.rv_posts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ProfilePostAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        profileViewModel.userPosts.observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter.setPosts(posts);
            }
        });
    }
}
