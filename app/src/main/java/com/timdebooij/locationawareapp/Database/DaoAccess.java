package com.timdebooij.locationawareapp.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.timdebooij.locationawareapp.Entities.Station;

import java.util.List;

@Dao
public interface DaoAccess {
    @Query("SELECT * FROM station")
    List<Station> getAll();

    @Query("SELECT * FROM station WHERE station_name LIKE :name LIMIT 1")
    Station findByName(String name);

    @Insert
    void insertStation(Station station);

    @Insert
    void AddAll(Station... stations);

    @Delete
    void deleteStation(Station station);

    @Delete
    void DeleteAll(List<Station> stations);
}
