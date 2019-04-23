package com.example.googlemapexample;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

class LocationUpdateWork extends Worker {

    final private String TAG = "WORKER";
    FileLog mFileLog;

    public LocationUpdateWork(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        //mFileLog = new FileLog(this, "worker.txt", "WORKER");
        Log.d(TAG, "doWork");
        try {
            Log.d(TAG, "doWork success");
            return Result.retry();//Result.success();
        } catch (Throwable throwable) {
            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error applying blur", throwable);
            return Result.failure();
        }
    }
}
