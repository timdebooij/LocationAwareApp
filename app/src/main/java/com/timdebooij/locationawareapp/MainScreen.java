package com.timdebooij.locationawareapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.timdebooij.locationawareapp.Api.DirectionApiManager;
import com.timdebooij.locationawareapp.Api.NSApiListener;
import com.timdebooij.locationawareapp.Api.NSApiManager;
import com.timdebooij.locationawareapp.Entities.DepartureInformation;
import com.timdebooij.locationawareapp.Entities.Station;
import com.timdebooij.locationawareapp.Entities.Time;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainScreen extends  FragmentActivity implements NSApiListener {

    private LocationManager locationManager;
    private LocationListener locationListener;
    public GoogleMap map;
    private DirectionApiManager manager;
    private NSApiManager nsApiManager;
    private LatLng myLoc;
    private ArrayList<Station> stations;
    private String wayOfTransport;
    public ArrayAdapter<String> spinnerAdapter;
    private Spinner spinner;
    private RecyclerView recyclerView;
    private InformationAdapter adapter;
    private ArrayList<DepartureInformation> departureInformation;
    private Location lastLocation;
    private boolean locationPermissionGranted;
    private Polyline polyline;
    private ArrayList<LatLng> routePath = new ArrayList<>();
    private boolean routeMade = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main_screen);
        departureInformation = new ArrayList<>();
        nsApiManager = new NSApiManager(this,this);
        Intent intent = getIntent();
        this.manager = new DirectionApiManager(this, this);
        stations = intent.getParcelableArrayListExtra("stations");

        setUpRecyclerView();
        myLoc = new LatLng(intent.getDoubleExtra("lat", 0), intent.getDoubleExtra("lon", 0));
        this.wayOfTransport = intent.getStringExtra("transport");
        this.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
                updateRoute();
                //Log.i("infoloc", "new location by manager");
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
                getLocationPermission();
                map = googleMap;
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String name = marker.getTitle();
                        spinner.setSelection(spinnerAdapter.getPosition(name));
                        return false;
                    }
                });
                setUpSpinner();
                startListening();
                LatLng endpoint = new LatLng(51.5947713, 4.781993);
                //map.addMarker(new MarkerOptions().position(myLoc).title("My location"));
                map.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(myLoc.latitude, myLoc.longitude), 13));

                for(Station s : stations){
                    LatLng statLoc = new LatLng(s.getLatitude(), s.getLongitude());
                    map.addMarker(new MarkerOptions().position(statLoc).title(s.getName()));
                }
                try {
                    if (locationPermissionGranted) {
                        map.setMyLocationEnabled(true);

                        //map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));
                    } else {
                        map.setMyLocationEnabled(false);

                        lastLocation = null;
                    }
                } catch (SecurityException e) {
                    Log.e("Exception: %s", e.getMessage());
                }
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(myLoc.latitude, myLoc.longitude), 13));
                if(routePath != null && routePath.size()>0){
                    routeMade = true;
                    PolylineOptions opts = new PolylineOptions().addAll(routePath).color(Color.BLUE).width(8);
                    polyline = map.addPolyline(opts);
                }
            }
        });

        if(savedInstanceState!=null){
            routePath = savedInstanceState.getParcelableArrayList("path");
            List<DepartureInformation> newList = savedInstanceState.getParcelableArrayList("dep");
            departureInformation.clear();
            departureInformation.addAll(newList);
            adapter.notifyDataSetChanged();
        }
    }

    public void setUpSpinner(){
        ArrayList<String> names = new ArrayList<>();
        for(Station s : stations){
            names.add(s.getName());
        }
        spinner = ((Fragment)getSupportFragmentManager().findFragmentById(R.id.informationFragment)).getView().findViewById(R.id.stationSpinner);
        spinner.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String stat = spinner.getSelectedItem().toString();
                double lat = 0;
                double lon = 0;
                for(Station s : stations){
                    if(stat.equals(s.getName())){
                        lat = s.getLatitude();
                        lon = s.getLongitude();
                    }
                }
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lat, lon), 15));
                if(!routeMade) {
                    nsApiManager.getTimes(stat);
                }
                routeMade = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, names);
        spinner.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();

    }

    public void updateRoute(){
        if(locationPermissionGranted && routePath.size() != 0 && lastLocation != null) {
            int index = 0;
            LatLng checkPos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            for (LatLng p : routePath) {
                Location loc = new Location("");
                loc.setLatitude(checkPos.latitude);
                loc.setLongitude(checkPos.longitude);
                Location pLoc = new Location("");
                pLoc.setLatitude(p.latitude);
                pLoc.setLongitude(p.longitude);
                int dist = Math.round(pLoc.distanceTo(loc));
                if (dist < 20) {
                    index = routePath.indexOf(p);
                }
            }
            int fix = 1;
            if (index == 0)
                fix = 0;
            if(polyline != null){
                polyline.remove();
            }
            List<LatLng> newPath = routePath.subList(index-fix, routePath.size()-1);
            PolylineOptions polyOptions = new PolylineOptions().addAll(newPath).color(Color.BLUE).width(8);
            polyline = map.addPolyline(polyOptions);
        }
    }

    public void setUpRecyclerView(){
        recyclerView = ((Fragment)getSupportFragmentManager().findFragmentById(R.id.informationFragment)).getView().findViewById(R.id.informationRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InformationAdapter(this, departureInformation);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void mapButton(View view){
        String stat = spinner.getSelectedItem().toString();
        double lat = 0;
        double lon = 0;
        for(Station s : stations){
            if(stat.equals(s.getName())){
                lat = s.getLatitude();
                lon = s.getLongitude();
            }
        }
        manager.getRoute(new LatLng(myLoc.latitude, myLoc.longitude), new LatLng(lat, lon), wayOfTransport);
    }

    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationPermissionGranted = true;
        else
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
                    lastLocation = location;
                }
            }
        }
    }

    private void makeRoute(DirectionsResult result, int seconds)
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

        //Draw the polyline
        if (path.size() > 0)
        {
            if(polyline!= null){
                polyline.remove();
            }
            routePath = path;
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(8);
            polyline = map.addPolyline(opts);
        }

        map.getUiSettings().setZoomControlsEnabled(true);
        updateTrainList(seconds);
    }

    public void updateTrainList(int time){
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm");
        Date currentTime = Calendar.getInstance().getTime();
        //Date currentDate = Calendar.getInstance(TimeZone.getTimeZone("MET, GMT +1")).getTime();
        String timeU = mdformat.format(currentTime);
        List<DepartureInformation> reachableInfo = new ArrayList<>();
        for(DepartureInformation d : departureInformation){
            String timeAtStation = getTime(timeU, time);
            String depTime = d.getTime();
            boolean reachable = compareTimes(depTime, timeAtStation);
            if(reachable){
                reachableInfo.add(d);
            }
        }
        departureInformation.clear();
        departureInformation.addAll(reachableInfo);
        adapter.notifyDataSetChanged();
    }

    public boolean compareTimes(String timeTrain, String timeArrival){
        String[] partsTrain = timeTrain.split(":");
        int minTrain = Integer.parseInt(partsTrain[1]);
        int hourTrain = Integer.parseInt(partsTrain[0]);
        String[] partsArrival = timeArrival.split(":");
        int minArrival = Integer.parseInt(partsArrival[1]);
        int hourArrival = Integer.parseInt(partsArrival[0]);
        if(hourArrival > hourTrain){
            return false;
        }
        else if(hourArrival == hourTrain){
            if(minArrival>minTrain){
                return false;
            }
            else
                return true;
        }
        else
            return true;
    }

    public String getTime(String time, int travelTime){
        String[] parts = time.split(":");
        int min = Integer.parseInt(parts[1]);
        int hour = (int)Double.parseDouble(parts[0]);
        int travelMin = travelTime/60;
        min = min + travelMin;
        if(min>=60){
            min = min - 60;
            hour++;
        }
        return hour + ":" + min;
    }

    @Override
    public void onTimeAvailable(ArrayList<DepartureInformation> departureInformations) {
        if(!routeMade) {
            departureInformation.clear();
            departureInformation.addAll(departureInformations);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStationsAvailable(List<Station> stations) {

    }

    @Override
    public void onRouteAvailable(DirectionsResult directionsResult, int seconds) {
        makeRoute(directionsResult, seconds);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("path", routePath);
        outState.putParcelableArrayList("dep", departureInformation);
        super.onSaveInstanceState(outState);
    }
}
