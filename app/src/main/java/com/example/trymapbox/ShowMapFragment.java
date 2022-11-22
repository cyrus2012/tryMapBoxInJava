package com.example.trymapbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.trymapbox.databinding.FragmentShowMapBinding;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.PuckBearingSource;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin2;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import org.jetbrains.annotations.NotNull;

//reference to https://docs.mapbox.com/android/maps/examples/location-tracking/
public class ShowMapFragment extends Fragment {

    private FragmentShowMapBinding binding;
    private MapView mapView;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentShowMapBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = binding.mapView;

        binding.myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded(){
            @Override
            public void onStyleLoaded(Style style){
                enableLocationDisplayed();
                addIndicatorListener();
                setupGesturesListener();
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        binding = null;
    }

    private void enableLocationDisplayed(){
        LocationComponentPlugin2 locationComponentPlugin = mapView.getPlugin(Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID);
        //LocationComponentPlugin2 locationComponentPlugin = LocationComponentUtils.getLocationComponent2(mapView); //alternative method to get locationComponentPlugin
        locationComponentPlugin.setEnabled(true);

        LocationPuck2D puck2D = LocationComponentUtils.createDefault2DPuck(locationComponentPlugin, getContext(), true);
        locationComponentPlugin.setLocationPuck(puck2D);
        locationComponentPlugin.setPuckBearingEnabled(true); // enable the puck to rotate to track the device bearing
        locationComponentPlugin.setPuckBearingSource(PuckBearingSource.HEADING);

    }

    private void addIndicatorListener(){
        LocationComponentPlugin2 locationComponentPlugin = mapView.getPlugin(Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID);
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
    }


    void setupGesturesListener(){
        GesturesPlugin gesturesPlugin = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
        gesturesPlugin.addOnMoveListener(onMoveListener);
    }

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener(){
        @Override
        public void onIndicatorPositionChanged(Point point){
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).build());
            GesturesPlugin gesturesPlugin = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
            gesturesPlugin.setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
        }
    };

    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = new OnIndicatorBearingChangedListener(){
        @Override
        public void onIndicatorBearingChanged(double bearing){
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(bearing).build());
        }
    };

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NotNull MoveGestureDetector moveGestureDetector) {
            onCameraTrackingDismissed();
        }

        @Override
        public boolean onMove(@NotNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NotNull MoveGestureDetector moveGestureDetector) {

        }
    };

    void onCameraTrackingDismissed(){
        LocationComponentPlugin locationComponentPlugin = mapView.getPlugin(Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID);
        locationComponentPlugin.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        locationComponentPlugin.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
        GesturesPlugin gesturesPlugin = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
        gesturesPlugin.removeOnMoveListener(onMoveListener);
    }
}