package com.example.googlemapexample;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;


import static com.example.googlemapexample.LocationUpdatesService.TAG;

public class LocationListener extends LiveData<Location> {
    private static LocationListener instance;

    public static LocationListener getInstance(Context appContext) {
        if (instance == null) {
            instance = new LocationListener(appContext);
        }

        return instance;
    }

    private LocationListener(Context appContext) {
        buildGoogleApiClient(appContext);
    }

    private synchronized void buildGoogleApiClient(Context appContext) {
        Log.d(TAG, "Build google api client");
    }

    void update(Location location) {
        Log.d(TAG, "update");
        postValue(location);
    }

    @Override
    protected void onActive() {
        Log.d(TAG, "onActive");
        Location location = new Location("location onActive");
        setValue(location);
    }

    @Override
    protected void onInactive() {
        Log.d(TAG, "onInactive");
        Location location = new Location("location onInactive");
        setValue(location);
    }

}
