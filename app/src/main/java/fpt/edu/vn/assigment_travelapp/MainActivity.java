package fpt.edu.vn.assigment_travelapp;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import fpt.edu.vn.assigment_travelapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_my_trip,
                R.id.navigation_explore,
                R.id.navigation_favorite,
                R.id.navigation_profile
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        
        BottomNavigationView navView = binding.navView;
        NavigationUI.setupWithNavController(navView, navController);

        // Setup FloatingActionButton for creating new post
        FloatingActionButton fab = binding.fab;
        if (fab != null) {
            fab.setOnClickListener(view -> {
                if (navController != null) {
                    navController.navigate(R.id.action_global_newPostFragment);
                }
            });
        }

        // Listen to navigation changes to show/hide FAB and BottomNavigationView
        if (navController != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // Hide FAB and BottomNav on certain screens
                boolean shouldShow = destination.getId() != R.id.signInFragment
                        && destination.getId() != R.id.signUpFragment
                        && destination.getId() != R.id.splashFragment
                        && destination.getId() != R.id.newPostFragment
                        && destination.getId() != R.id.commentFragment
                        && destination.getId() != R.id.postDetailsFragment;

                if (navView != null) {
                    navView.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                }
                if (fab != null) {
                    fab.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}

