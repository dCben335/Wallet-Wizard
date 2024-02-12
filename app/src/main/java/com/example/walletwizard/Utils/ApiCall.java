package com.example.walletwizard.Utils;


import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class ApiCall {

    public static void makeApiCall(String url, ApiCallback callbackFunctions, Context context) {
        JsonObjectRequest request = new JsonObjectRequest(
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callbackFunctions.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callbackFunctions.onError(error.getMessage());
                    }
                }
        );

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }


    public static interface ApiCallback {
        void onSuccess(JSONObject response);

        void onError(String errorMessage);
    }
}