package com.example.walletwizard.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.walletwizard.Utils.LocationHandler;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.walletwizard.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.WellKnownTileServer;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import java.text.DecimalFormat;

public class MapFragment extends Fragment  {
    private View rootView;
    private String mapStyleUrl;
    private Context context;
    private MapView mapView;
    private MapboxMap map;
    private double currentLatitude;
    private double currentLongitude;

    public LocationHandler locationHandler;
    private Marker ownMarker;
    private Marker openedMarker;

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

        injectMap(mapStyleUrl);

        locationHandler = new LocationHandler(context, this);
        locationHandler.startLocationUpdates();
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
            if (isLocation()) replaceOwnPositionMarker();
            setCamera();
        }));
    }

    private void replaceOwnPositionMarker() {
        if (ownMarker != null) {
            map.removeMarker(ownMarker);
        }

        Icon currentPositionIcon = getMarkerIcon(R.drawable.marker);
        ownMarker = createMarker(currentPositionIcon, currentLatitude, currentLongitude, "Votre Position", "");
    }


    public void updateLocation(@NonNull Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        if (map != null) {
            replaceOwnPositionMarker();
            setCamera();
        }
    }

    private void setCamera() {
        double initialCameraLat = isLocation() ? currentLatitude : 1;
        double initialCameraLong = isLocation() ? currentLongitude : 1;
        int initalZoom =  isLocation() ? 10 : 1;

        map.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(initialCameraLat, initialCameraLong))
                .zoom(initalZoom)
                .build()
        );
    }

    private Marker createMarker(Icon customMarkerIcon, double latitude, double longitude, String title, String description) {
        return addMarker(
            new LatLng(latitude, longitude),
            title,
            description,
            customMarkerIcon
        );
    }

    private Marker addMarker(LatLng latLng, String title, String description, Icon icon) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(description)
                .icon(icon);

        Marker newMarker = map.addMarker(markerOptions);

        map.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow(map, mapView);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12.0));
            return true;
        });

        return newMarker;
    }
    private Icon getMarkerIcon(int src) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), src);

        int width = 50;
        int height = 50;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

        return IconFactory.getInstance(context).fromBitmap(resizedBitmap);
    }



    public boolean isLocation() {
        return currentLatitude != 0.0 && currentLongitude != 0.0;
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
