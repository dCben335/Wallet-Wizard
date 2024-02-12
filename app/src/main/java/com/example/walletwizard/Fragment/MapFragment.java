package com.example.walletwizard.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.walletwizard.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.WellKnownTileServer;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class MapFragment extends Fragment {
    private View rootView;
    private Context context;


    private MapView mapView;
    private MapboxMap map;
    private String mapStyleUrl;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public double currentLatitude;
    public double currentLongitude;




    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = requireContext();

        mapStyleUrl = initiateMapSettings();
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = setLocationListener();


        if (!isLocationEnabled(context)) {
            showLocationSettingsDialog(context);
        }


        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                (Activity) context,
                new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
                },
                REQUEST_LOCATION_PERMISSION
            );
        } else {
            startLocationUpdates();
        }


        injectMap(mapStyleUrl);
    }




    protected String initiateMapSettings() {
        String apiKey = "uBctdXB4pPaJOqBcg0O8";
        String mapId = "a58748e5-0742-4a46-912f-02da61abd2f1";
        String styleUrl = "https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + apiKey;

        Mapbox.getInstance(context, apiKey, WellKnownTileServer.MapLibre);

        return styleUrl;
    }


    protected void injectMap(String styleUrl) {
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(new Style.Builder().fromUrl(styleUrl), style -> {
            map = mapboxMap;
            if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                Icon currentPositionIcon = getMarkerIcon(R.drawable.marker);
                Log.d("current lat", String.valueOf(currentLatitude));
                Log.d("current long", String.valueOf(currentLongitude));

                createMarker(currentPositionIcon, currentLatitude, currentLongitude, "Votre Position", "");
            }

            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(47, 5))
                .zoom(10)
                .build()
            );
        }));
    }

    private void createMarker(Icon customMarkerIcon, double latitude, double longitude, String title, String description) {
        addMarker(
            new LatLng(latitude, longitude),
            title,
            description,
            customMarkerIcon
        );
    }






    public static void showLocationSettingsDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("La localisation est recommandée pour utiliser cette partie de l'application");
        builder.setPositiveButton("Activer", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        });

        builder.setNegativeButton("Continuer", (dialog, which) -> dialog.dismiss());
        builder.setCancelable(true);
        builder.show();
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }



    protected LocationListener setLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                Log.d("current lat", String.valueOf(currentLatitude));
                Log.d("current long", "dqzddqzdkzqdqbhdqd");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };
    }

    private void startLocationUpdates() {
        try {

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                locationListener.onLocationChanged(lastKnownLocation);
            }


            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                locationListener
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }




    private void addMarker(LatLng latLng, String title, String description, Icon icon) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(description)
                .icon(icon);

        map.addMarker(markerOptions);

        map.setOnMarkerClickListener(marker -> {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.0));
            return true;
        });
    }
    private Icon getMarkerIcon(int src) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), src);

        int width = 50;
        int height = 50;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

        Icon customMarkerIcon = IconFactory.getInstance(context).fromBitmap(resizedBitmap);
        return customMarkerIcon;
    }
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
