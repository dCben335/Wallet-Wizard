package com.example.walletwizard.Utils;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.walletwizard.Fragment.MapFragment;

public class LocationHandler {

    private final Context context;
    private final MapFragment mapFragment;
    private final LocationManager locationManager;
    private boolean isLocated = false;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;


    public LocationHandler(Context context, MapFragment mapFragment) {
        this.context = context;
        this.mapFragment = mapFragment;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (mapFragment.isLocation()) return;

        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
            return;
        }

        if (!isLocationEnabled()) {
            if (!getLastKnowLocation()) {
                displayLocationDialog();
            }
            return;
        }

        getLastKnowLocation();
        requestLocationUpdates();
        isLocated = true;

    }



    private void displayLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("La localisation est recommandée pour utiliser cette partie de l'application");
        builder.setPositiveButton("Activer", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
            dialog.dismiss();
        });

        builder.setNegativeButton("Continuer", (dialog, which) -> dialog.dismiss());
        builder.setCancelable(true);
        builder.show();
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            1000,
            locationListener
        );
    }

    @SuppressLint("MissingPermission")
    private boolean getLastKnowLocation() {
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            locationListener.onLocationChanged(lastKnownLocation);
            return true;
        }

        return false;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
            (Activity) context,
            new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION
            },
            REQUEST_LOCATION_PERMISSION
        );
    }
    private boolean isLocationEnabled() {
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }




    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            mapFragment.updateLocation(location);
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (mapFragment.isLocation()) return;
            getLastKnowLocation();
            requestLocationUpdates();
        }
    };
}
