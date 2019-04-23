package com.example.googlemapexample;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LocationUpdatesService extends Service {

    public static final String EXTRA_LOCATION = Objects.requireNonNull(LocationUpdatesService.class.getPackage()).getName() + ".location";
    public static final String ACTION_BROADCAST = Objects.requireNonNull(LocationUpdatesService.class.getPackage()).getName() + ".broadcast";
    public static final String TAG = "LOCATION_SERVICE";
    public static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    public static final String LOCATION_UPDATES_NOTIFICATIONS = "location_updates_notifications";
    private static final int NOTIFICATION_ID = 1008;
    private static long UPDATE_INTERVAL_IN_MILLISECONDS = 120000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = Objects.requireNonNull(LocationUpdatesService.class.getPackage()).getName() +
            ".started_from_notification";
    private final IBinder mBinder = new LocationBinder();
    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback;
    private Location mLocation;
    private NotificationManager mNotificationManager;
    private boolean mChangingConfiguration = false;
    private LocationRequest mLocationRequest;

    /**
     * Stores the location updates state in SharedPreferences.
     *
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Returns the {@code location} object as a human readable string.
     *
     * @param location The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.d(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.d(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Last client unbound from service");
        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && requestingLocationUpdates(this)) {
            Log.d(TAG, "Starting foreground service");
            /*
            // TODO(developer). If targeting O, use the following code.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                mNotificationManager.startServiceInForeground(new Intent(this,
                        LocationUpdatesService.class), NOTIFICATION_ID, getNotification());
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }
             */
            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Intent intent = new Intent(ACTION_BROADCAST);
                mLocation = locationResult.getLastLocation();
                intent.putExtra(EXTRA_LOCATION, locationResult.getLastLocation());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                // Update notification content if running as a foreground service.
                if (serviceIsRunningInForeground(LocationUpdatesService.this)) {
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification());
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };

        createLocationRequest();
        getLastLocation();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Request location after starting the service
        requestLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startNewThread();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startNewThread() {
        new Thread() {
            @Override
            public void run() {
                super.run();
            }
        };
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        return new NotificationCompat.Builder(this, LOCATION_UPDATES_NOTIFICATIONS)
                .addAction(R.drawable.ic_launcher_foreground, "add_action_1",
                        activityPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "add_action_2",
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(mLocation.getLatitude() + "")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis()).build();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.e("Error", "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException e) {
            Log.e("Exception", "Last location permission." + e);
        }
    }

    public void removeLocationUpdates() {
        Log.d(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    public void requestLocationUpdates() {
        Log.d(TAG, "Requesting location updates");
        setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    public class LocationBinder extends Binder {
        public LocationUpdatesService getLocationUpdateService() {
            return LocationUpdatesService.this;
        }
    }

}
