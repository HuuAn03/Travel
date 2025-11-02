package fpt.edu.vn.assigment_travelapp.ui.onboarding;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import fpt.edu.vn.assigment_travelapp.R;
import fpt.edu.vn.assigment_travelapp.databinding.FragmentChooseLocationBinding;

public class ChooseLocationFragment extends Fragment {

    private static final String TAG = "ChooseLocationFragment";
    private FragmentChooseLocationBinding binding;
    private MapView map;
    private Marker locationMarker;
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            getCurrentLocation();
        } else {
            Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChooseLocationBinding.inflate(inflater, container, false);

        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

        map = binding.map;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(9.5);

        GeoPoint startPoint = new GeoPoint(10.7769, 106.7009); // Ho Chi Minh City
        map.getController().setCenter(startPoint);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            double latitude = bundle.getDouble("latitude");
            double longitude = bundle.getDouble("longitude");
            GeoPoint selectedPoint = new GeoPoint(latitude, longitude);

            updateMapWithLocation(selectedPoint, "Selected Location");
            saveLocationToDatabase(latitude, longitude);
        });

        binding.btnSetLocationOnMap.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_chooseLocationFragment_to_fullScreenMapFragment);
        });

        binding.btnUseCurrentLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        binding.btnConfirm.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_chooseLocationFragment_to_navigation_home);
        });
    }

    private void saveLocationToDatabase(double latitude, double longitude) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String addressString = addresses.get(0).getAddressLine(0);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                databaseReference.child(userId).child("location").setValue(addressString)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Location saved successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to save location", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to save location to database", e);
                        });

            } else {
                Toast.makeText(getContext(), "Could not find address for the location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed", e);
            Toast.makeText(getContext(), "Error getting address. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMapWithLocation(GeoPoint point, String title) {
        map.getController().setCenter(point);
        map.getController().setZoom(15.0);
        if (locationMarker == null) {
            locationMarker = new Marker(map);
            locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(locationMarker);
        }
        locationMarker.setPosition(point);
        locationMarker.setTitle(title);
        map.invalidate();
    }


    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // This should be handled by the caller, but check again.
            return;
        }

        Toast.makeText(getContext(), "Fetching current location...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                    updateMapWithLocation(currentLocation, "Current Location");
                    saveLocationToDatabase(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(getContext(), "Could not get current location. Please ensure location is enabled.", Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(requireActivity(), e -> {
                Log.e(TAG, "Failed to get current location.", e);
                Toast.makeText(getContext(), "Failed to get current location. Please try again.", Toast.LENGTH_LONG).show();
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
