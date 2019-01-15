package com.timdebooij.locationawareapp.Entities;

import android.os.Parcel;
import android.os.Parcelable;

public class DepartureInformation implements Parcelable {
    public int driveNumber;
    public String departureTime;
    public String departureDelay;
    public String endStation;
    public String stopStations;
    public String trainSort;
    public String carrier;
    public int departureTrack;

    protected DepartureInformation(Parcel in){

        driveNumber = in.readInt();
        departureTime = in.readString();
        departureDelay = in.readString();
        endStation = in.readString();
        stopStations = in.readString();
        trainSort = in.readString();
        carrier = in.readString();
        departureTrack = in.readInt();
    }

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

    public static final Creator<DepartureInformation> CREATOR = new Creator<DepartureInformation>() {
        @Override
        public DepartureInformation createFromParcel(Parcel in) {
            return new DepartureInformation(in);
        }

        @Override
        public DepartureInformation[] newArray(int size) {
            return new DepartureInformation[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(driveNumber);
        parcel.writeString(departureTime);
        parcel.writeString(departureDelay);
        parcel.writeString(endStation);
        parcel.writeString(stopStations);
        parcel.writeString(trainSort);
        parcel.writeString(carrier);
        parcel.writeInt(departureTrack);
    }
}
