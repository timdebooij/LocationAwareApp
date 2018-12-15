package com.timdebooij.locationawareapp.Database;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.timdebooij.locationawareapp.Entities.Station;

import java.util.List;

public class DatabaseManager {

    private static DatabaseManager instance;
    private AppDatabase database;

    public static DatabaseManager getInstance() {

        if (instance == null)
            instance = new DatabaseManager();

        return instance;
    }

    private DatabaseManager() {

    }

    public void setDatabase(Context context) {
        database = Room.databaseBuilder(context, AppDatabase.class, "database")
                .allowMainThreadQueries()
                .build();
    }

    public List<Station> getStations() {
        return database.daoAccess().getAll();
    }

    public void AddStation(Station station) {
        database.daoAccess().insertStation(station);
    }

    public void clearDatabase() {
        database.daoAccess().DeleteAll(getStations());
    }
}