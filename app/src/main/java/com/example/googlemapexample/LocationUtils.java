package com.example.googlemapexample;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationUtils {
    final static String LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    final private static long UPDATE_INTERVAL_IN_MILLISECONDS = 120_000;
    final private static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    static FusedLocationProviderClient mFusedLocationClient;
    static LocationRequest mLocationRequest;


    private static final LocationUtils ourInstance = new LocationUtils();

    private LocationUtils() {
    }

    public static LocationUtils getInstance() {
        return ourInstance;
    }

    static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    static void initLocationUtils(Context context) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

    }

    static void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    static void requestLocationUpdates(Context context, LocationCallback locationCallback) {
        setRequestingLocationUpdates(context, true);
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    locationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            setRequestingLocationUpdates(context, false);
          //  Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }
}
