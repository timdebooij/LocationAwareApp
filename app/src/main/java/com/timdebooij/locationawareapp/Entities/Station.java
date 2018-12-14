package com.timdebooij.locationawareapp.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Station {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "station_name")
    private String name;
    @ColumnInfo(name = "station_long")
    private double longitude;
    @ColumnInfo(name = "station_lat")
    private double latitude;

    public Station(int uid,String name, double longitude, double latitude) {
        this.uid = uid;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Station(){

    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}