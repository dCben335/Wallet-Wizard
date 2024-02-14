package com.example.walletwizard.Utils;


import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;


public class ApiCall {

    public enum RequestType {
        OBJECT,
        ARRAY
    }

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

    private static void makeObjectRequest(String url, ApiCallback callbackFunctions, Context context) {
        JsonObjectRequest request = new JsonObjectRequest(
                url,
                null,
                callbackFunctions::onSuccess,
                error -> callbackFunctions.onError(error.getMessage())
        );

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    private static void makeArrayRequest(String url, ApiCallback callbackFunctions, Context context) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                callbackFunctions::onSuccess,
                error -> callbackFunctions.onError(error.getMessage())
        );

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    public interface ApiCallback {
        void onSuccess(Object response);
        void onError(String errorMessage);
    }
}
