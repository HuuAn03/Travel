package fpt.edu.vn.assigment_travelapp.ui.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return MyPostsFragment.newInstance();
            case 1:
                // Assuming LikesFragment and BookmarksFragment will be updated similarly
                return LikesFragment.newInstance();
            case 2:
                return BookmarksFragment.newInstance();
            default:
                return MyPostsFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
