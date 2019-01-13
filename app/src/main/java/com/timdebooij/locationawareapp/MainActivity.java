package com.timdebooij.locationawareapp;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.constraint.Guideline;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.maps.model.DirectionsResult;
import com.timdebooij.locationawareapp.Api.NSApiListener;
import com.timdebooij.locationawareapp.Api.NSApiManager;
import com.timdebooij.locationawareapp.Database.DatabaseManager;
import com.timdebooij.locationawareapp.Entities.DepartureInformation;
import com.timdebooij.locationawareapp.Entities.Station;

import java.util.ArrayList;
import java.util.List;


/*
To do:
        Possibility to view detail of the departureinformation and mayby trainstation(distance in km for example)
        Button to make route to selected station and update the time list to only trains you can make.
 */

public class MainActivity extends AppCompatActivity implements NSApiListener {

    DatabaseManager databaseManager;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private List<Station> stations;
    private String wayOfTransport = "walking";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NSApiManager manager = new NSApiManager(this, this);

        Guideline guideline = findViewById(R.id.guideline);
        guideline.setGuidelinePercent(0.5f);

        databaseManager = DatabaseManager.getInstance();
        databaseManager.setDatabase(this);
        stations = new ArrayList<>();
        stations = databaseManager.getStations();
        Log.i("infostart", "aantal stations bij opstart: " + stations.size());
        if(!(stations.size() > 0)){
            manager.getStations();
            Log.i("infostart", "executed");
        }
        this.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        //startListening(stations);

        //manager.getTimes();
    }

    public Location startListening(List<Station> stations){
        if(Build.VERSION.SDK_INT < 23){
            startListening(stations);
        }
        else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else{
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null) {
                    return location;
                }
            }
        }
        Location test = new Location("");
        test.setLongitude(5.104480);
        test.setLatitude(52.092876);
        return startListening(stations);
    }

    public ArrayList<Station> getClosestStations(List<Station> stations, Location location){
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double closest = 100;
        Station sclose = null;
        Log.i("infoStations", "amount of stations: " + stations.size());
        ArrayList<Station> sList = new ArrayList<>();
        for(Station s : stations){
            double dif = Math.abs(lat - s.getLatitude()) + Math.abs(lon - s.getLongitude());
            if(dif < closest){
                closest = dif;
                sclose = s;
            }
            if(dif < 0.051){
                sList.add(s);
                Location st = new Location("");
                st.setLongitude(s.getLongitude());
                st.setLatitude(s.getLatitude());
                int distanceInMeters =  (int)(Math.floor(st.distanceTo(location)));
                Log.i("info", "close station: " + s.getName() + " with " + distanceInMeters + " meter distance and " + dif);
            }
        }
        if(sList.size()==0){
            sList.add(sclose);
        }
        Log.i("info", "closest station is: " + sclose.getName() + " at " + closest);
        for(Station s : sList) {

        }
        Log.i("info", "amount of stations in area: " + sList.size());
        return sList;
    }

    public void differentTransport(View view){
        TextView text = findViewById(R.id.transportInformationTextView);
        String tag = view.getTag().toString();
        if(tag.equals("driving")){
            text.setText(R.string.driving);
            wayOfTransport = "driving";
        }
        if(tag.equals("cycling")){
            text.setText(R.string.cycling);
            wayOfTransport = "bicycling";
        }
        if(tag.equals("walking")){
            text.setText(R.string.walking);
            wayOfTransport = "walking";
        }
    }

    public void clickNext(View view){
        Location ut = new Location("");
        ut.setLatitude(52.092876);
        ut.setLongitude(5.104480);
        Location myLoc = startListening(stations);
        Log.i("info", "location: " + myLoc.getLatitude() + " + " + myLoc.getLongitude());
        ArrayList<Station> stationClose = getClosestStations(stations, myLoc);
        Intent intent = new Intent(view.getContext(), MainScreen.class);
        intent.putParcelableArrayListExtra("stations", stationClose);
        intent.putExtra("lat", myLoc.getLatitude());
        intent.putExtra("lon", myLoc.getLongitude());
        intent.putExtra("transport", wayOfTransport);
        view.getContext().startActivity(intent);
    }

    @Override
    public void onTimeAvailable(ArrayList<DepartureInformation> departureInformations) {

    }

    @Override
    public void onStationsAvailable(List<Station> stations) {
        for(Station s : stations) {
            databaseManager.AddStation(s);
        }
        List<Station> stationsCheck = new ArrayList<>();
        stationsCheck = databaseManager.getStations();
        Log.i("info", "aantal stations na ophalen: " + stationsCheck.size());
    }

    @Override
    public void onRouteAvailable(DirectionsResult directionsResult) {

    }
}
