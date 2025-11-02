
package fpt.edu.vn.assigment_travelapp.ui.onboarding;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import fpt.edu.vn.assigment_travelapp.databinding.FragmentFullScreenMapBinding;

public class FullScreenMapFragment extends Fragment {

    private FragmentFullScreenMapBinding binding;
    private MapView map;
    private Marker selectedLocationMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFullScreenMapBinding.inflate(inflater, container, false);

        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

        map = binding.fullMap;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(9.5);

        GeoPoint startPoint = new GeoPoint(10.7769, 106.7009); // Ho Chi Minh City
        map.getController().setCenter(startPoint);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (selectedLocationMarker == null) {
                    selectedLocationMarker = new Marker(map);
                    selectedLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(selectedLocationMarker);
                }
                selectedLocationMarker.setPosition(p);
                map.invalidate();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));

        binding.btnBackFromMap.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        binding.btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLocationMarker != null) {
                GeoPoint selectedPoint = selectedLocationMarker.getPosition();
                Bundle result = new Bundle();
                result.putDouble("latitude", selectedPoint.getLatitude());
                result.putDouble("longitude", selectedPoint.getLongitude());
                getParentFragmentManager().setFragmentResult("requestKey", result);
            }
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
