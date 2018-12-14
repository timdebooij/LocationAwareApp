package com.timdebooij.locationawareapp.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.timdebooij.locationawareapp.Entities.Station;

@Database(entities = {Station.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DaoAccess daoAccess();
}
