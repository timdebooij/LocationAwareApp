package com.timdebooij.locationawareapp;

public interface NSApiListener {

    void onTimeAvailable(String time);
    void onStationsAvailable(String stations);
}
