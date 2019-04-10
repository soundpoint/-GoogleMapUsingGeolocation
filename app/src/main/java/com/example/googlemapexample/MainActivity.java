package com.example.googlemapexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationResult;
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

import androidx.fragment.app.FragmentActivity;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = "googleMaps";
    private final int ZOOM = 17;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        if (isGoogleAPIok()) {
            setContentView(R.layout.activity_maps);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            assert mapFragment != null;
            mapFragment.getMapAsync(this);
        } else {
            setContentView(R.layout.activity_main);
        }

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        logString("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        logString("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        logString("onPause");

    }

    @Override
    protected void onStart() {
        super.onStart();
        logString("onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        logString("onStop");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        logString("onSaveInstanceState");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*if (mLocationUpdate != null) {
            gotoLocation(mLocationUpdate.getLastLocation().getLatitude(),
                    mLocationUpdate.getLastLocation().getLongitude(), ZOOM);
            Toast.makeText(this, "On Map Ready: Location Update", Toast.LENGTH_LONG).show();
        } else if (mLocation != null) {
            gotoLocation(mLocation.getLatitude(), mLocation.getLongitude(), ZOOM);
            Toast.makeText(this, "On Map Ready: Last location", Toast.LENGTH_LONG).show();
        }*/

        Toast.makeText(this, "On Map Ready", Toast.LENGTH_LONG).show();
        logString("On Map Ready");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling code

        return super.onOptionsItemSelected(item);
    }

    private void gotoLocation(double lat, double lng, float zoom) {
        //Toast.makeText(this, "Location Update", Toast.LENGTH_LONG).show();
        // Move the camera
        LatLng location = new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Your Position"));
    }

    public boolean isGoogleAPIok() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int isAvailable = googleAPI.isGooglePlayServicesAvailable(this);

        if (isAvailable != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(isAvailable)) {
                googleAPI.getErrorDialog(this, isAvailable,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, "Can't connect to mapping services",
                        Toast.LENGTH_LONG).show();
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }

        return true;
    }

    private void logString(String logString) {
        Log.d(TAG, logString);
        storeRecordInFile("locations.txt", logString);
    }

    private void logLocation(LocationResult location) {
        final double latitude = location.getLastLocation().getLatitude();
        final double longitude = location.getLastLocation().getLongitude();
        final String ts = (new Date()).toString();
        Log.d(TAG, "Current location is" + location.toString());
        Date location_measurement_ts = new Date(location.getLastLocation().getTime());
        storeRecordInFile("locations.txt", String.format(Locale.US,
                "{\"utcTime\":\"%s\", \"measurementUtcTime\":\"%s\", " +
                        "\"type\":\"updated\", \"provider\":\"%s\", \"accuracy\":%f, " +
                        "\"latitude\":%f, \"longitude\":%f},",
                ts, location_measurement_ts.toString(), location.getLastLocation().getProvider(),
                location.getLastLocation().getAccuracy(), latitude, longitude));

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

    class LocationUpdatesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "LocationUpdatesReceiver: " + intent.getAction());

        }
    }

}
