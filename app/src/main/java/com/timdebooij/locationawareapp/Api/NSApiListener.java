package com.timdebooij.locationawareapp.Api;

import com.google.maps.model.DirectionsResult;
import com.timdebooij.locationawareapp.Entities.Station;

import java.util.List;

public interface NSApiListener {

    void onTimeAvailable(String time);
    void onStationsAvailable(List<Station> stations);
    void onRouteAvailable(DirectionsResult directionsResult);
}
