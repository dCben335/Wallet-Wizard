package com.example.walletwizard.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.walletwizard.Utils.ApiCall;
import com.example.walletwizard.Utils.LocationHandler;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            if (isCurrentLocation()) replaceOwnPositionMarker();
            setCamera();

            handleApiCall();
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

    private void handleApiCall() {
        int resultPerPage = 500;
        String format = "json";

        String url = "http://api.worldbank.org/v2/country/all?per_page=" + resultPerPage + "&format=" + format;
        ApiCall.RequestType requestType = ApiCall.RequestType.ARRAY;

        ApiCall.handleRequest(requestType, url, new ApiCall.ApiCallback() {
            @Override
            public void onSuccess(Object response) {
                try {
                    if (response instanceof JSONArray) {
                        JSONArray bankArray = ((JSONArray) response).getJSONArray(1);

                        if (bankArray == null) {
                            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        addBankMarkers(bankArray);

                    } else Toast.makeText(context, "", Toast.LENGTH_SHORT).show();


                } catch (JSONException e) {
                    Toast.makeText(context, "All the bank were not displayed, please retry later" , Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, "API call failed, please try again later" + errorMessage, Toast.LENGTH_SHORT).show();
            }

        }, context);
    }

    private void addBankMarkers(JSONArray banks) throws JSONException {
        for (int i = 0; i < banks.length(); i++) {
            JSONObject bank = banks.getJSONObject(i);

            String title = bank.getString("name");
            String city = bank.getString("capitalCity");

            double longitude = convertToDouble(bank.getString("longitude"));
            double latitude = convertToDouble(bank.getString("latitude"));

            if (isLocation(longitude, latitude) && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(city)) {
                Icon currentPositionIcon = getMarkerIcon(R.drawable.marker);
                createMarker(
                        currentPositionIcon,
                        latitude,
                        longitude,
                        title,
                        "You can find this bank in the city of " + city
                );
            }
        }
    }

    private double convertToDouble(String string) {
        double value = 0.0;

        if (!TextUtils.isEmpty(string)) {
            value = Double.parseDouble(string);
        }

        return value;
    }


    private void setCamera() {
        double initialCameraLat = isCurrentLocation() ? currentLatitude : 1;
        double initialCameraLong = isCurrentLocation() ? currentLongitude : 1;
        int initialZoom =  isCurrentLocation() ? 10 : 1;

        map.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(initialCameraLat, initialCameraLong))
                .zoom(initialZoom)
                .build()
        );
    }


    private Icon getMarkerIcon(int src) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), src);

        int width = 50;
        int height = 50;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

        return IconFactory.getInstance(context).fromBitmap(resizedBitmap);
    }

    private Marker createMarker(Icon customMarkerIcon, double latitude, double longitude, String title, String description) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(title)
                .snippet(description)
                .icon(customMarkerIcon);


        return map.addMarker(markerOptions);
    }

    public boolean isCurrentLocation() {
        return isLocation(currentLatitude, currentLongitude);
    }

    private boolean isLocation(double latitude, double longitude) {
        return latitude != 0.0 && longitude != 0.0;
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
