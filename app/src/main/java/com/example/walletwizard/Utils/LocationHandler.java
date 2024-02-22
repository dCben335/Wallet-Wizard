package com.example.walletwizard.Utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.example.walletwizard.Fragments.MapFragment;

// Utility class for handling location-related operations
public class LocationHandler {

    // Context and MapFragment instance
    private final Context context;
    private final MapFragment mapFragment;

    // LocationManager instance
    private final LocationManager locationManager;

    // Request code for location permission
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    // Constructor
    public LocationHandler(Context context, MapFragment mapFragment) {
        this.context = context;
        this.mapFragment = mapFragment;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    // Method to start location updates
    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        // If already displaying current location, return
        if (mapFragment.isCurrentLocation()) return;

        // If location permission is not granted, request it
        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
            return;
        }

        // If location is not enabled, prompt user to enable it
        if (!isLocationEnabled()) {
            if (!getLastKnownLocation()) {
                // If unable to get last known location, notify user
                Toast.makeText(context, "Please enable your Location to get the full feature experience", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Get last known location and request location updates
        getLastKnownLocation();
        requestLocationUpdates();
    }

    // Method to retrieve last known location
    @SuppressLint("MissingPermission")
    private boolean getLastKnownLocation() {
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            // If last known location is available, update map with it
            locationListener.onLocationChanged(lastKnownLocation);
            return true;
        }
        return false;
    }

    // Method to request location updates
    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                1000,
                locationListener
        );
    }

    // Method to request location permission
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                (Activity) context,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                REQUEST_LOCATION_PERMISSION
        );
    }

    // Method to check if location is enabled
    private boolean isLocationEnabled() {
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // Method to check if location permission is granted
    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // LocationListener implementation to handle location updates
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            // When location changes, update map with new location
            mapFragment.updateLocation(location);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            // When provider is enabled, if not displaying current location, request updates
            if (mapFragment.isCurrentLocation()) return;
            getLastKnownLocation();
            requestLocationUpdates();
        }
    };
}
