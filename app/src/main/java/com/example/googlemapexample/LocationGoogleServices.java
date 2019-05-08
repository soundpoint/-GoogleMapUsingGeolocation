package com.example.googlemapexample;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LocationGoogleServices {
    final private static String TAG = "LOCATION";
    private final static String WORKER_TAG = "WORKER_TAG";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final int REQUEST_CHECK_SETTINGS = 6003;
    private Context context;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private FileLog mFileLog;

    LocationGoogleServices(final Context context) {
        this.context = context;
        //locationRequestSetup(context);
        mFileLog = new FileLog(context, "listenableWorker.txt", TAG);
        subscribeToLocationUpdate();
    }

    private void subscribeToLocationUpdate() {
        LocationListener.getInstance(context).observeForever(new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                Log.d(TAG, "onChanged: location updated " + location);
                // do your stuff

                mFileLog.logLocation(location);
            }
        });
    }

    void stop() {

    }

    void locationRequestSetup(final Context context) {

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mSettingsClient = LocationServices.getSettingsClient(context);

        locationRequest = LocationRequest.create();
        //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(2 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        mLocationSettingsRequest = builder.build();

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        Task<LocationSettingsResponse> task;
        task = LocationServices.getSettingsClient(context).checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    Log.d(TAG, "Device GPS is on");

                    setLocationCallbacks();

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.e(TAG, "Device GPS need to be switched on!");
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        (Activity) context,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            Log.e(TAG, "Couldn't change GPS setting. Please set in on user!");
                            break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isGoogleAPIok() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int isAvailable = googleAPI.isGooglePlayServicesAvailable(context);

        if (isAvailable != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(isAvailable)) {
                googleAPI.getErrorDialog((Activity) context, isAvailable,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    void workInfo() throws ExecutionException, InterruptedException {
        WorkManager wm = WorkManager.getInstance();
        ListenableFuture<List<WorkInfo>> future = wm.getWorkInfosForUniqueWork(LocationListenableWorker.UNIQUE_WORK_NAME);
        //ListenableFuture<List<WorkInfo>> future = wm.getWorkInfosByTag(LocationListenableWorker.UNIQUE_WORK_NAME);
        List<WorkInfo> list = future.get();
        int cnt = 0;
        for (WorkInfo workInfo : list) {
            WorkInfo.State state = workInfo.getState();
            if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                // find current process
                Log.d("", "tt");
            }
            cnt++;
        }

        Log.d(TAG, "WorkInfos");

    }

    void setLocationCallbacks() throws ExecutionException, InterruptedException {
        workInfo();
        stopLocationWorker();
        stopListanableWorker();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LocationListenableWorker.class)
                .setInitialDelay(1, TimeUnit.SECONDS).build();
        WorkManager.getInstance().enqueueUniqueWork(LocationListenableWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE, request);
    }

    void stopListanableWorker() {
        WorkManager.getInstance().cancelAllWorkByTag(LocationListenableWorker.UNIQUE_WORK_NAME);
    }

    void startLocationWorker() {
        stopLocationWorker();

        PeriodicWorkRequest.Builder locationPeriodicBuilder =
                new PeriodicWorkRequest.Builder(LocationPeriodicWorker.class, 1, TimeUnit.MINUTES);

        PeriodicWorkRequest periodicWorkRequest = locationPeriodicBuilder.build();
        // Start worker
        WorkManager.getInstance().enqueueUniquePeriodicWork(WORKER_TAG,
                ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
    }


    void stopLocationWorker() {
        WorkManager.getInstance().cancelAllWorkByTag(WORKER_TAG);
    }

}
