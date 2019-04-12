package com.example.googlemapexample;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceAdmin extends DeviceAdminReceiver {
    public static final String TAG = "DeviceAdmin";

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.d(TAG, "admin_receiver_status_enabled");
        // admin rights
        App.getPreferences().edit().putBoolean(App.ADMIN_ENABLED, true).commit(); //App.getPreferences() returns the sharedPreferences

    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "admin_receiver_status_disable_warning";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.d(TAG, "admin_receiver_status_disabled");
        // admin rights removed
        App.getPreferences().edit().putBoolean(App.ADMIN_ENABLED, false).commit(); //App.getPreferences() returns the sharedPreferences
    }
}
