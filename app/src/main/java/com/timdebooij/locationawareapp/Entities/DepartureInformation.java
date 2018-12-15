package com.timdebooij.locationawareapp.Entities;

public class DepartureInformation {
    public int driveNumber;
    public String departureTime;
    public String departureDelay;
    public String endStation;
    public String stopStations;
    public String trainSort;
    public String carrier;
    public int departureTrack;

    public DepartureInformation(int driveNumber, String departureTime, String departureDelay, String endStation, String stopStations, String trainSort, String carrier, int departureTrack) {
        this.driveNumber = driveNumber;
        this.departureTime = departureTime;
        this.departureDelay = departureDelay;
        this.endStation = endStation;
        this.stopStations = stopStations;
        this.trainSort = trainSort;
        this.carrier = carrier;
        this.departureTrack = departureTrack;
    }

    public String getTime(){
        String[] parts = departureTime.split("T");
        String[] timeParts = parts[1].split("\\+");
        int index = timeParts[0].lastIndexOf(":");

        return timeParts[0].substring(0,index);
    }

    @Override
    public String toString() {
        return endStation + ": " + getTime() + " at track: " + departureTrack;
    }
}
