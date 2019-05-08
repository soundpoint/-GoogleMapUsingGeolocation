package com.example.googlemapexample;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.ResolvableFuture;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.TimeUnit;

public class LocationListenableWorker extends ListenableWorker {
    static final String UNIQUE_WORK_NAME = "LocationWorker";
    private static final String TAG = "LocationWorker";
    private ResolvableFuture<Result> mFuture;
    private LocationCallback mLocationCallback;
    //private FileLog mFileLog;

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public LocationListenableWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Log.d(TAG, "Starting work " + getId());
        // Starting log
        //mFileLog = new FileLog(getApplicationContext(), "listenableWorker.txt", TAG);
        //mFileLog.logString("Starting work " + getId());
        mFuture = ResolvableFuture.create();
        // Instantiate static locations object
        LocationUtils.getInstance(getApplicationContext());

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LocationUtils.removeLocationUpdates(getApplicationContext(), mLocationCallback);

                //mFileLog.logString("onLocationResult: Work "  + getId());
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    //mFileLog.logLocation(location);
                    LocationListener.getInstance(getApplicationContext()).update(location);
                }

                if (true) {
                    // Rescheduling work
                    OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LocationListenableWorker.class)
                            .setInitialDelay(2, TimeUnit.MINUTES)
                            .build();
                    WorkManager.getInstance().enqueueUniqueWork(LocationListenableWorker.UNIQUE_WORK_NAME,
                            ExistingWorkPolicy.APPEND, request);
                    Log.d(TAG, "Rescheduling work. New ID: " + request.getId());
                }
                // Always set the result as the last operation
                mFuture.set(Result.success());
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                //mFileLog.logString("onLocationAvailability: Work " + getId());
                //mFileLog.logString("onLocationAvailability: isLocationAvailable() = "
                //       + locationAvailability.isLocationAvailable());
                super.onLocationAvailability(locationAvailability);
            }
        };

        LocationUtils.requestLocationUpdates(getApplicationContext(), mLocationCallback);

        /*LocationUtils.getInstance(getApplicationContext()).
                requestSingleUpdate(mLocationCallback, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure ");
                        e.printStackTrace();
                        mFuture.set(Result.failure());
                    }
                });*/

        return mFuture;
    }
}
