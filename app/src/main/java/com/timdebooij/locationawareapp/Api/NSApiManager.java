package com.timdebooij.locationawareapp.Api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.timdebooij.locationawareapp.Api.NSApiListener;
import com.timdebooij.locationawareapp.Entities.DepartureInformation;
import com.timdebooij.locationawareapp.Entities.Station;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Base64;
import org.json.XML;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NSApiManager {
    private String username;
    private String passWord;
    private RequestQueue queue;
    private NSApiListener listener;
    private Gson gson;

    public NSApiManager(Context context, NSApiListener listener){
        queue = Volley.newRequestQueue(context);
        this.listener = listener;
        this.username = "timdebooij9@hotmail.com";
        this.passWord = "dA61c7XUUWzxIDMAMfb4QHwQw5fpprht1tGvq6oYWBJ0U4VpYy5s1Q";
        this.gson = new Gson();
    }

    public void getTimes(String station){
        String url = "https://webservices.ns.nl/ns-api-avt?station=" + station;

        ArrayList<DepartureInformation> departureInformation = new ArrayList<>();
        StringRequest request = new StringRequest(0, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i("infores", "length of response " + response.length());
                //Log.i("infores", "response before: " + response);
                String res = response;
                if(res.length() > 12000){
                    String check = "<VertrekkendeTrein>";
                    int index = res.lastIndexOf(check);
                    Log.i("infores", "index: " + index);
                    res = res.substring(0, index);
                    Log.i("infores","length after " + res.length());
                }
                try {
                    JSONObject object = XML.toJSONObject(response);
                    Log.i("infores", "reached");
                    JSONObject dispatchTimes = object.getJSONObject("ActueleVertrekTijden");
                    JSONArray trains = dispatchTimes.getJSONArray("VertrekkendeTrein");

                    for(int i = 0; i<trains.length(); i++){
                        JSONObject train = trains.getJSONObject(i);
                        String endStation = train.getString("EindBestemming");
                        int number = train.getInt("RitNummer");
                        String time = train.getString("VertrekTijd");
                        String delay = "";
                        if(train.has("VertrekVertragingTekst")) {
                            delay = train.getString("VertrekVertragingTekst");
                        }
                        String sort = train.getString("TreinSoort");
                        String carrier = train.getString("Vervoerder");
                        JSONObject trackobject = train.getJSONObject("VertrekSpoor");
                        int track = trackobject.getInt("content");
                        String route = "";
                        if(train.has("RouteTekst")){
                            route = train.getString("RouteTekst");
                        }
                        DepartureInformation dep = new DepartureInformation(number, time, delay, endStation, route, sort,carrier, track);
                        departureInformation.add(dep);

                    }
                    listener.onTimeAvailable(departureInformation);
                    //Log.i("info", object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onTimeAvailable(departureInformation);
                    Log.i("info", e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = username + ":" + passWord;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }

        };
        this.queue.add(request);
    }

    public void getStations(){
        String url = "http://webservices.ns.nl/ns-api-stations-v2?_ga=2.884721.199041428.1544620328-659085999.1544620328";
        Log.i("info", "reached");
        StringRequest request = new StringRequest(0, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    List<Station> stationList = new ArrayList<>();
                    JSONObject object = XML.toJSONObject(response);
                    JSONObject stations = object.getJSONObject("Stations");
                    JSONArray stationArray = stations.getJSONArray("Station");
                    for(int i = 0; i<stationArray.length(); i++){
                        JSONObject station = stationArray.getJSONObject(i);
                        JSONObject names = station.getJSONObject("Namen");
                        String name = names.getString("Lang");
                        double lon = station.getDouble("Lon");
                        double lat = station.getDouble("Lat");
                        int uid = station.getInt("UICCode");
                        Station s = new Station(uid,name, lon,lat);
                        stationList.add(s);
                    }
                    Log.i("info", "amount of stations before sending: " + stationList.size());
                    listener.onStationsAvailable(stationList);
                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("infoerror", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = username + ":" + passWord;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        this.queue.add(request);
    }
}
