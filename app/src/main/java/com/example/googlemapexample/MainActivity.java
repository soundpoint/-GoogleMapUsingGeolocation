package com.example.googlemapexample;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "googleMaps";
    private LocationUpdatesService mLocationUpdatesService;
    private Location mLocation;

    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logString("onCreate");
        super.onCreate(savedInstanceState);

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminName = new ComponentName(this, DeviceAdmin.class);

        if (mDPM.isAdminActive(mAdminName)) {
            startService(new Intent(this, LocationUpdatesService.class));
        } else {
            adminPermission();
        }

    }

    public void adminPermission() {
        try {
            if (!mDPM.isAdminActive(mAdminName)) {
                try {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check Permission is granted or not
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startService(new Intent(this, LocationUpdatesService.class));
            } else {
                if (!mDPM.isAdminActive(mAdminName)) {
                    adminPermission();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        logString("onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        logString("onPause");
        super.onPause();
    }

    @Override
    protected void onStart() {
        logString("onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        logString("onStop");
        super.onStop();
    }

    private void logString(String logString) {
        Log.d(TAG, logString);
        //storeRecordInFile("locations.txt", logString);
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

}
