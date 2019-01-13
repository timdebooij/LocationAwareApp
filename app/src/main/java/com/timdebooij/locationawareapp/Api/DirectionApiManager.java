package com.timdebooij.locationawareapp.Api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.GeocodedWaypoint;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DirectionApiManager {
    private Context context;
    private NSApiListener directionsApiListener;
    private RequestQueue queue;
    private Gson gson;

    public DirectionApiManager(Context context, NSApiListener directionsApiListener)
    {
        this.context = context;
        this.directionsApiListener = directionsApiListener;
        this.queue = Volley.newRequestQueue(context);
        this.gson = new Gson();
    }

    public void getRoute(LatLng origin, LatLng destination, String transport)
    {
        String url = "https://maps.moviecast.io/directions?origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude + "&mode=" + transport + "&key=1030a017-840a-423d-8ae7-e20ef8e9bed9";

        //String url2 = "https://maps.moviecast.io/directions?origin=Disneyland&destination=Universal+Studios+Hollywood&key=d314dcbd-9358-4e95-96e2-14e9b29f7e5f";

        Log.i("infoapi", url);
        final JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("infoapi", response.toString());
                        DirectionsResult directionsResult = new DirectionsResult();
                        try
                        {



                            JSONArray routeArray = response.getJSONArray("routes");
                            JSONArray geoCodeArray = response.getJSONArray("geocoded_waypoints");
                            JSONObject poly = routeArray.getJSONObject(0);
                            JSONObject polyline = poly.getJSONObject("overview_polyline");
                            String pointline = polyline.getString("points");

                            List<LatLng> ll = decodePoly(pointline);


                            DirectionsRoute[] routes = new DirectionsRoute[routeArray.length()];
                            for(int i = 0; i<routeArray.length(); i++){
                                routes[i] =  gson.fromJson(routeArray.get(i).toString(), DirectionsRoute.class);
                            }
                            GeocodedWaypoint[] waypoints = new GeocodedWaypoint[geoCodeArray.length()];
                            for(int i = 0; i<geoCodeArray.length(); i++){
                                waypoints[i] = gson.fromJson(geoCodeArray.get(i).toString(), GeocodedWaypoint.class);
                            }
                            directionsResult.geocodedWaypoints = waypoints;
                            directionsResult.routes = routes;
                            directionsApiListener.onRouteAvailable(directionsResult);

                        }
                        catch (JSONException e)
                        {
                            Log.i("infoapi", "error in parsen");
                            e.printStackTrace();
                        }


                    }
                },

                error ->
                {
                    Log.d("VOLLEY_TAG", error.toString());
                }
        );
        queue.add(request);
    }
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
