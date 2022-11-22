package com.example.trymapbox;

import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.trymapbox.databinding.ActivityMainBinding;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin2;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private MapView mapView;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        navController.navigate(R.id.SelectionExampleFragment);


        if(isGooglePlayAvailable()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }



    private boolean isGooglePlayAvailable(){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        if(api == null) {
            Log.d("MapFragment", "GoogleApiAvailability is null");
            return false;
        }
        int resultCode = api.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            Log.d("MapFragment", "GoogleApiAvailability is NOT available.");
            return false;
        }
        Log.d("MapFragment", "Google play services is available.");
        return true;
    }

}