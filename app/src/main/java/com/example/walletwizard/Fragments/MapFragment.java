package com.example.walletwizard.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.walletwizard.Utils.ApiCall;
import com.example.walletwizard.Utils.LoadingScreen;
import com.example.walletwizard.Utils.LocationHandler;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.walletwizard.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.WellKnownTileServer;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapFragment extends Fragment  {
    // View and Context variables
    private View rootView;
    private Context context;
    private LoadingScreen loadingScreen;

    // Map settings variables
    private String mapStyleUrl;
    private MapView mapView;
    private MapboxMap map;

    // Location handling variables
    private LocationHandler locationHandler;
    private double currentLatitude;
    private double currentLongitude;
    private Marker ownMarker;

    // Bank information variables
    private JSONArray banks;
    private boolean banksPlaced = false;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize the context and Set up the Map style URL
        context = requireContext();
        mapStyleUrl = initiateMapSettings();

        return rootView = inflater.inflate(R.layout.fragment_map, container, false);
    }

    // Called immediately after onCreateView()
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set map and location
        mapView = rootView.findViewById(R.id.mapView);
        loadingScreen = new LoadingScreen(context);
        injectMap(mapStyleUrl);
        locationHandler = new LocationHandler(context, this);
        locationHandler.startLocationUpdates();

        // Set listeners for buttons
        setLocateBtnListener();
        setBankBtnListener();
    }

    // Method to initialize Map settings
    protected String initiateMapSettings() {
        // Map Settings
        String apiKey = "uBctdXB4pPaJOqBcg0O8";
        String mapId = "a58748e5-0742-4a46-912f-02da61abd2f1";
        String styleUrl = "https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + apiKey;

        // Initialize Mapbox and return style
        Mapbox.getInstance(context, apiKey, WellKnownTileServer.MapLibre);
        return styleUrl;
    }

    // Method to inject Map
    protected void injectMap(String styleUrl) {
        // Load the Map style
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(new Style.Builder().fromUrl(styleUrl), style -> {
            // Once the style is loaded, Set camera position and own position maker
            map = mapboxMap;
            if (isCurrentLocation()) replaceOwnPositionMarker();
            setCamera();
        }));
    }

    // Method to replace own position marker on the Map
    private void replaceOwnPositionMarker() {
        // Remove existing own marker if it exists
        if (ownMarker != null) {
            map.removeMarker(ownMarker);
        }

        // Get icon and Create marker for own position
        Icon currentPositionIcon = getMarkerIcon(R.drawable.marker_own_position);
        ownMarker = createMarker(currentPositionIcon, currentLatitude, currentLongitude, "Votre Position", "");
    }

    // Method to update current location
    public void updateLocation(@NonNull Location location) {
        // Update current location
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        // If map is available, update marker and camera position
        if (map != null) {
            replaceOwnPositionMarker();
            setCamera();
        }
    }

    // Method to handle API call to retrieve bank information
    private void handleAPICall() {
        // API options
        int resultPerPage = 500;
        String format = "json";
        String url = "http://api.worldbank.org/v2/country/all?per_page=" + resultPerPage + "&format=" + format;
        ApiCall.RequestType requestType = ApiCall.RequestType.ARRAY;

        // Handling API call using custom API Class
        ApiCall.handleRequest(requestType, url, new ApiCall.ApiCallback() {
            @Override
            public void onSuccess(Object response) {
                // Handle API call success
                if (response instanceof JSONArray) {
                    try {
                        JSONArray bankArray = ((JSONArray) response).getJSONArray(1);

                        if (bankArray == null) {
                            // Inform user if there is no bank Array
                            Toast.makeText(context, "Something went wrong, retry later", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Store bank data and add markers
                        banks = bankArray;
                        addBankMarkers();

                    } catch (JSONException | InterruptedException e) {
                        // Inform user if there is a problem
                        Toast.makeText(context, "All the bank were not displayed, please try again later" , Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else Toast.makeText(context, "Something went wrong, please try again later", Toast.LENGTH_SHORT).show();

                // Remove Loading screen
                loadingScreen.dismiss();
            }

            @Override
            public void onError(String errorMessage) {
                // Remove Loading screen and inform the user
                loadingScreen.dismiss();
                Toast.makeText(context, "API call failed, please try again later" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, context);
    }

    // Method to add bank markers on the Map
    private void addBankMarkers() throws JSONException, InterruptedException {
        for (int i = 0; i < banks.length(); i++) {
            JSONObject bank = banks.getJSONObject(i);

            // Extract bank information
            String title = bank.getString("name");
            String city = bank.getString("capitalCity");
            double longitude = convertToDouble(bank.getString("longitude"));
            double latitude = convertToDouble(bank.getString("latitude"));

            // Check if location is valid and add marker
            if (isLocation(longitude, latitude) && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(city)) {
                Icon bankIcon = getMarkerIcon(R.drawable.marker_bank);
                createMarker(
                        bankIcon,
                        latitude,
                        longitude,
                        title,
                        "You can find this bank in the city of " + city
                );
            }
        }

        // Mark that banks have been placed
        banksPlaced = true;
    }

    // Method to convert String to double
    private double convertToDouble(String string) {
        double value = 0.0;
        if (!TextUtils.isEmpty(string)) {
            value = Double.parseDouble(string);
        }
        return value;
    }

    // Method to set camera position on the Map
    private void setCamera() {
        // Set initial camera position and zoom level variables
        double initialCameraLat = isCurrentLocation() ? currentLatitude : 1;
        double initialCameraLong = isCurrentLocation() ? currentLongitude : 1;
        int initialZoom =  isCurrentLocation() ? 5 : 1;

        // Set camera position
        map.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(initialCameraLat, initialCameraLong))
                .zoom(initialZoom)
                .build()
        );
    }

    // Method to get marker icon from resource
    private Icon getMarkerIcon(int src) {
        // Decode the bitmap from the resource
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), src);

        // Resize the bitmap
        int width = 50;
        int height = 50;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

        // Return the icon from the resized bitmap
        return IconFactory.getInstance(context).fromBitmap(resizedBitmap);
    }

    // Method to create a marker on the Map
    private Marker createMarker(Icon customMarkerIcon, double latitude, double longitude, String title, String description) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(title)
                .snippet(description)
                .icon(customMarkerIcon);

        return map.addMarker(markerOptions);
    }

    // Method to set listener for locate button
    protected void setLocateBtnListener() {
        rootView.findViewById(R.id.btn_locate).setOnClickListener(v -> {
            locationHandler.startLocationUpdates();
            setCamera();
        });
    }

    // Method to set listener for bank button
    protected void setBankBtnListener() {
        rootView.findViewById(R.id.btn_bank).setOnClickListener(v -> {
            // If bank already placed, do nothing
            if (banksPlaced) return;

            // If banks are not loaded, initiate API call, else add bank markers
            if (banks != null) {
                try {
                    addBankMarkers();
                } catch (JSONException | InterruptedException e) {
                    // Inform user if there is a problem
                    Toast.makeText(context, "All the bank were not displayed, please try again later" , Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }

            } else {
                // Show the loading screen and make the Call
                loadingScreen.show();
                handleAPICall();
            }

            // Set the background to indicate that the banks markers are added.
            v.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.accent_color));

            setCamera();
        });
    }

    // Method to check if current location is available
    public boolean isCurrentLocation() {
        return isLocation(currentLatitude, currentLongitude);
    }

    // Method to check if a location is available
    private boolean isLocation(double latitude, double longitude) {
        return latitude != 0.0 && longitude != 0.0;
    }


    // Lifecycle methods
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        // Put banksPlaced to false because when I'll go back on the Fragment, the bank marker won't be placed anymore
        banksPlaced = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // Put banksPlaced to false because when I'll go back on the Fragment, the bank marker won't be placed anymore
        banksPlaced = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        // Put banksPlaced to false because when I'll go back on the Fragment, the bank marker won't be placed anymore
        banksPlaced = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        // Put banksPlaced to false because when I'll go back on the Fragment, the bank marker won't be placed anymore
        banksPlaced = false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        // Put banksPlaced to false because when I'll go back on the Fragment, the bank marker won't be placed anymore
        banksPlaced = false;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
        // Put banksPlaced to false because when I'll go back on the Fragment, the bank marker won't be placed anymore
        banksPlaced = false;
    }
}
