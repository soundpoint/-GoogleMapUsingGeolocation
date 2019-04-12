package com.example.googlemapexample;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class LocationUpdatesService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "LOCATION_SERVICE";
    public static final int NOTIFICATION_ID = 9101;
    public static LocationUpdatesService locationService;
    private static GoogleApiClient mGoogleApiClient;
    private final IBinder mBinder = new LocationBinder();
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedProviderClient;
    private MyLocationCallback mMyLocationCallback;
    private Location curLocation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.d(TAG, "in onBind()");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.d(TAG, "in onRebind()");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Last client unbound from service");
        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "in onCreate()");
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            showNotificationAndStartForegroundService();

        locationService = this;
        buildGoogleApiClient();
    }

    //Google location Api build
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void requestUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedProviderClient.requestLocationUpdates(mLocationRequest, mMyLocationCallback,
                Looper.myLooper());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        buildGoogleApiClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        buildGoogleApiClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mFusedProviderClient = LocationServices.getFusedLocationProviderClient(LocationUpdatesService.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY;
        }
        mFusedProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                curLocation = location;
            }
        });

        if (mGoogleApiClient.isConnected()) {
            createLocationRequest();
        } else {
            buildGoogleApiClient();
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        startService();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService();
    }

    public void startService() {
        startService(new Intent(LocationUpdatesService.this, LocationUpdatesService.class));
    }

    protected void createLocationRequest() {
        mMyLocationCallback = new MyLocationCallback();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5.0f);

        requestUpdate();
    }

    //Start Foreground Service and Show Notification to user for Android O and higher Version
    private void showNotificationAndStartForegroundService() {

        final String CHANNEL_ID = BuildConfig.APPLICATION_ID.concat("_notification_id");
        final int REQUEST_CODE = 1;

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                REQUEST_CODE, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(false)
                .setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void logLocation(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final String ts = (new Date()).toString();
        Log.d(TAG, "Current location is" + location.toString());
        Date location_measurement_ts = new Date(location.getTime());
        storeRecordInFile("locations.txt", String.format(Locale.US,
                "{\"utcTime\":\"%s\", \"measurementUtcTime\":\"%s\", " +
                        "\"type\":\"updated\", \"provider\":\"%s\", \"accuracy\":%f, " +
                        "\"latitude\":%f, \"longitude\":%f},",
                ts, location_measurement_ts.toString(), location.getProvider(),
                location.getAccuracy(), latitude, longitude));

        storeRecordInFile("locations.txt", String.format(Locale.US,
                "https://www.google.com/maps/search/?api=1&query=%f,%f",
                latitude, longitude));

        Log.d(TAG, String.format(Locale.US,
                "Received location: Latitude:%f, Longitude:%f", latitude, longitude));
        Log.d(TAG, String.format(Locale.US,
                "Location link: https://www.google.com/maps/search/?api=1&query=%f,%f",
                latitude, longitude));
    }

    private boolean storeRecordInFile(String filename, String msg) {
        File m_recordFile = prepareLogFile(filename);
        if (m_recordFile != null) {
            try {
                FileOutputStream f = new FileOutputStream(m_recordFile, true);
                PrintWriter pw = new PrintWriter(f);
                pw.println(msg);
                pw.close();
                f.close();
            } catch (FileNotFoundException ex) {
                Log.e(TAG, "Failed to open the file. " + ex);
                return false;
            } catch (IOException ex) {
                Log.e(TAG, "Failed to write the data into the file. " + ex);
                return false;
            }
            return true;
        } else {
            Log.e(TAG, "File for storing the data doesn't exist");
            return false;
        }
    }

    private File prepareLogFile(String filename) {
        if (isExternalStorageWritable()) {
            File dir = new File(Environment.getExternalStorageDirectory() +
                    "/Android/data/" + this.getPackageName());
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory " + dir.getAbsolutePath());
                    return null;
                }
            }
            return new File(dir, filename);
        } else {
            Log.e(TAG, "External storage is not writable. Exiting application");
            return null;
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    public class MyLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //get your location here
            if (locationResult.getLastLocation() != null) {
                for (Location location : locationResult.getLocations()) {
                    curLocation = location;
                    logLocation(location);
                }
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    }

    private class LocationBinder extends Binder {
        LocationUpdatesService getLocationUpdateService() {
            return LocationUpdatesService.this;
        }
    }

}
