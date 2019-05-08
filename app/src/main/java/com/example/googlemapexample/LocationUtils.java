package com.example.googlemapexample;

import android.content.Context;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

public class LocationUtils {
    final static String LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    final private static long UPDATE_INTERVAL_IN_MILLISECONDS = 20_000;
    final private static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    static private FusedLocationProviderClient mFusedLocationClient;
    static private LocationSettingsRequest mLocationSettingsRequest;
    static private LocationRequest mLocationRequest;


    private static volatile LocationUtils ourInstance;

    private LocationUtils() {
        //Prevent form the reflection api.
        if (ourInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static LocationUtils getInstance(Context context) {
        if (ourInstance == null) {
            synchronized (LocationUtils.class) {
                if (ourInstance == null) {
                    ourInstance = new LocationUtils();
                }
            }
            // Init Google Location API
            initLocationUtils(context);
            setRequestingLocationUpdates(context, true);
            createLocationRequest();
        }
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

        /*LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
*/
    }

    static void requestLocationUpdates(Context context, LocationCallback locationCallback) {
        setRequestingLocationUpdates(context, true);
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    locationCallback, Looper.myLooper());
            setRequestingLocationUpdates(context, true);
        } catch (SecurityException unlikely) {
            setRequestingLocationUpdates(context, false);
        }
    }

    static void removeLocationUpdates(Context context, LocationCallback locationCallback) {
        try {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            setRequestingLocationUpdates(context, false);
        } catch (SecurityException unlikely) {
            setRequestingLocationUpdates(context, true);
        }
    }

}
