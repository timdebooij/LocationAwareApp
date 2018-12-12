package com.timdebooij.locationawareapp;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity implements NSApiListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NSApiManager manager = new NSApiManager(this, this);
        manager.getTimes();
    }

    @Override
    public void onTimeAvailable(String time) {

    }

    @Override
    public void onStationsAvailable(String stations) {
        
    }
}
