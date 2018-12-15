package com.timdebooij.locationawareapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.timdebooij.locationawareapp.Api.DirectionApiManager;
import com.timdebooij.locationawareapp.Api.NSApiListener;
import com.timdebooij.locationawareapp.Entities.Station;

import java.util.ArrayList;
import java.util.List;

public class MainScreen extends  FragmentActivity implements NSApiListener {

    private LocationManager locationManager;
    private LocationListener locationListener;
    public GoogleMap map;
    private DirectionApiManager manager;
    private LatLng myLoc;
    private ArrayList<Station> stations;
    private String wayOfTransport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Intent intent = getIntent();
        this.manager = new DirectionApiManager(this, this);
        stations = intent.getParcelableArrayListExtra("stations");
        myLoc = new LatLng(intent.getDoubleExtra("lat", 0), intent.getDoubleExtra("lon", 0));
        this.wayOfTransport = intent.getStringExtra("transport");
        Log.i("info", wayOfTransport);
        this.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                startListening();
                LatLng endpoint = new LatLng(51.5947713, 4.781993);
                map.addMarker(new MarkerOptions().position(myLoc).title("My location"));
                map.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(myLoc.latitude, myLoc.longitude), 16));

                for(Station s : stations){
                    LatLng statLoc = new LatLng(s.getLatitude(), s.getLongitude());
                    map.addMarker(new MarkerOptions().position(statLoc).title(s.getName()));
                }

            }
        });
    }

    public void mapButton(View view){
        manager.getRoute(new LatLng(myLoc.latitude, myLoc.longitude), new LatLng(stations.get(0).getLatitude(), stations.get(0).getLongitude()), wayOfTransport);
    }

    public void startListening(){
        if(Build.VERSION.SDK_INT < 23){
            startListening();
        }
        else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null) {

                }
            }
        }
    }

    private void makeRoute(DirectionsResult result)
    {
        ArrayList path = new ArrayList();


        try
        {
            DirectionsResult res = result;
            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0)
            {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null)
                {
                    for (int i = 0; i < route.legs.length; i++)
                    {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null)
                        {
                            for (int j = 0; j < leg.steps.length; j++)
                            {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0)
                                {
                                    for (int k = 0; k < step.steps.length; k++)
                                    {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null)
                                        {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1)
                                            {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else
                                {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null)
                                    {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords)
                                        {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Log.i("info", ex.getLocalizedMessage());
        }

        Log.i("infoapi", "amount of lines " + path.size());
        //Draw the polyline
        if (path.size() > 0)
        {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(8);
            map.addPolyline(opts);
        }

        map.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onTimeAvailable(String time) {

    }

    @Override
    public void onStationsAvailable(List<Station> stations) {

    }

    @Override
    public void onRouteAvailable(DirectionsResult directionsResult) {
        makeRoute(directionsResult);
    }
}
