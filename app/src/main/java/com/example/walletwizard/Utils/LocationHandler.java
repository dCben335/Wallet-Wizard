package com.example.walletwizard.Utils;


// LocationManager.java
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LocationHandler {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private final Context context;
    private final LocationListener locationListener;

    public LocationHandler(Context context, LocationListener locationListener) {
        this.context = context;
        this.locationListener = locationListener;
    }

    public void startLocationUpdates() {
        android.location.LocationManager locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    locationListener.onLocationChanged(lastKnownLocation);
                }
            }
        }
    }
}
