package fpt.edu.vn.assigment_travelapp;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        createAdminUserIfNeeded();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_my_trip, R.id.navigation_favorite, R.id.navigation_profile)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        
        // Custom navigation handling to clear booking fragments when navigating between top-level destinations
        binding.navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            // When navigating to top-level destinations, always clear back stack to ensure
            // no booking fragments remain in the stack
            if (itemId == R.id.navigation_home || itemId == R.id.navigation_my_trip || 
                 itemId == R.id.navigation_favorite || itemId == R.id.navigation_profile) {
                
                // Check current destination
                int currentDestId = navController.getCurrentDestination() != null ? 
                        navController.getCurrentDestination().getId() : -1;
                
                // If already on the target destination, don't navigate
                if (currentDestId == itemId) {
                    return true;
                }
                
                // Check if current destination is a booking fragment
                boolean isBookingFragment = currentDestId == R.id.searchLocationFragment ||
                        currentDestId == R.id.bookingFragment ||
                        currentDestId == R.id.paymentFragment ||
                        currentDestId == R.id.bookingDetailsFragment ||
                        currentDestId == R.id.myBookingsFragment;
                
                // If coming from a booking fragment or navigating between top-level destinations,
                // clear back stack to ensure clean navigation
                if (isBookingFragment || 
                    (currentDestId == R.id.navigation_home || currentDestId == R.id.navigation_my_trip || 
                     currentDestId == R.id.navigation_favorite || currentDestId == R.id.navigation_profile)) {
                    
                    // Pop to the start destination to clear all fragments, then navigate to target
                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(navController.getGraph().getStartDestination(), true)
                            .build();
                    navController.navigate(itemId, null, navOptions);
                } else {
                    // Use default navigation behavior
                    NavigationUI.onNavDestinationSelected(item, navController);
                }
            } else {
                // Use default navigation behavior for other destinations
                NavigationUI.onNavDestinationSelected(item, navController);
            }
            
            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Handle menu visibility
            if (optionsMenu != null) {
                MenuItem logoutItem = optionsMenu.findItem(R.id.action_logout);
                if (logoutItem != null) {
                    logoutItem.setVisible(destination.getId() == R.id.navigation_profile);
                }
            }

            // Handle splash screen
            if (destination.getId() == R.id.splashFragment) {
                 FirebaseUser currentUser = mAuth.getCurrentUser();
                 if (currentUser != null) {
                    navController.navigate(R.id.navigation_home);
                 }
            } 
            // Hide navigation bar and action bar for certain fragments
            else if (destination.getId() == R.id.signInFragment || destination.getId() == R.id.signUpFragment || 
                       destination.getId() == R.id.newPostFragment || destination.getId() == R.id.fullScreenMapFragment || 
                       destination.getId() == R.id.chooseLocationFragment || destination.getId() == R.id.notificationsFragment ||
                       destination.getId() == R.id.managePlacesFragment) {
                navView.setVisibility(View.GONE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            } 
            // Show action bar with back button and navigation bar for booking fragments
            else if (destination.getId() == R.id.searchLocationFragment || 
                       destination.getId() == R.id.bookingFragment || 
                       destination.getId() == R.id.paymentFragment ||
                       destination.getId() == R.id.bookingDetailsFragment ||
                       destination.getId() == R.id.myBookingsFragment) {
                // Show navigation bar for booking fragments
                navView.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }
            } 
            // Show navigation bar and hide back button for main fragments
            else {
                navView.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                    // Check if it's a top-level destination
                    boolean isTopLevelDestination = destination.getId() == R.id.navigation_home ||
                            destination.getId() == R.id.navigation_my_trip ||
                            destination.getId() == R.id.navigation_favorite ||
                            destination.getId() == R.id.navigation_profile;
                    getSupportActionBar().setDisplayHomeAsUpEnabled(!isTopLevelDestination);
                }
            }
        });
    }

    private void createAdminUserIfNeeded() {
        String adminEmail = "admin@travel.com";
        String adminPassword = "123456";
        String adminName = "admin";

        mAuth.fetchSignInMethodsForEmail(adminEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                if (isNewUser) {
                    mAuth.createUserWithEmailAndPassword(adminEmail, adminPassword)
                            .addOnCompleteListener(authTask -> {
                                if (authTask.isSuccessful()) {
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String uid = firebaseUser.getUid();
                                        User newUser = new User(uid, adminName, adminEmail, "", "admin");

                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
                                        databaseReference.child("users").child(uid).setValue(newUser)
                                                .addOnCompleteListener(dbTask -> {
                                                    if (dbTask.isSuccessful()) {
                                                        Log.d("MainActivity", "Admin user created and saved successfully.");
                                                    } else {
                                                        Log.e("MainActivity", "Failed to save admin user data.", dbTask.getException());
                                                    }
                                                });
                                        mAuth.signOut();
                                    }
                                } else {
                                    Log.e("MainActivity", "Failed to create admin user in Auth.", authTask.getException());
                                }
                            });
                } else {
                    Log.d("MainActivity", "Admin user already exists.");
                }
            } else {
                Log.e("MainActivity", "Error fetching sign-in methods.", task.getException());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.mobile_navigation, true)
                    .build();
            navController.navigate(R.id.signInFragment, null, navOptions);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
