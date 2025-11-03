package fpt.edu.vn.assigment_travelapp.ui.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private String userId;

    public ViewPagerAdapter(@NonNull Fragment fragment, String userId) {
        super(fragment);
        this.userId = userId;
    }

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return MyPostsFragment.newInstance(userId);
            case 1:
                return LikesFragment.newInstance(userId);
            case 2:
                return BookmarksFragment.newInstance(userId);
            default:
                return MyPostsFragment.newInstance(userId);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
