package com.example.walletwizard.Utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

// Utility class for making API calls using Volley library
public class ApiCall {

    // Enum for different types of API request (Object or Array)
    public enum RequestType {
        OBJECT, // Request for a single JSON object
        ARRAY   // Request for a JSON array
    }

    // Method to handle API request based on request type
    public static void handleRequest(RequestType type, String url, ApiCallback callbackFunctions, Context context) {
        switch (type) {
            case OBJECT:
                makeObjectRequest(url, callbackFunctions, context);
                break;
            case ARRAY:
                makeArrayRequest(url, callbackFunctions, context);
                break;
        }
    }

    // Method to make a request for a single JSON object
    private static void makeObjectRequest(String url, ApiCallback callbackFunctions, Context context) {
        JsonObjectRequest request = new JsonObjectRequest(
                url,
                null,
                callbackFunctions::onSuccess, // Callback on success
                error -> callbackFunctions.onError(error.getMessage()) // Callback on error
        );

        // add request to queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request); // Add request to the queue
    }

    // Method to make a request for a JSON array
    private static void makeArrayRequest(String url, ApiCallback callbackFunctions, Context context) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                callbackFunctions::onSuccess, // Callback on success
                error -> callbackFunctions.onError(error.getMessage()) // Callback on error
        );

        // add request to queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request); // Add request to the queue
    }

    // Callback interface for API response
    public interface ApiCallback {
        void onSuccess(Object response); // Called on successful response
        void onError(String errorMessage); // Called on error
    }
}
