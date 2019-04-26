package com.example.googlemapexample;

import android.content.Context;
import android.util.Log;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class LocationPeriodicWorker extends Worker {
    final private String TAG = "WORKER";
    FileLog mFileLog;

    public LocationPeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Periodic worker");

        if (mFileLog == null) {
            mFileLog = new FileLog(getApplicationContext(),  "worker.txt", "WORKER");
        }

        mFileLog.logString("Periodical work " + new Date().toString());

        return Result.success();
    }
}
