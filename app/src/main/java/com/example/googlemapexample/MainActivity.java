package com.example.googlemapexample;

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = "googleMaps";
    private final int ZOOM = 17;
    private GoogleMap mMap;
    private LocationUpdatesService mLocationUpdatesService;
    private LocationReceiver mLocationReceiver;
    private Location mLocation;
    FileLog mFileLog;
    private WorkManager mWorkManager;

    // Tracks the bound state of the service.
    private boolean mBound = false;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocationUpdatesService = ((LocationUpdatesService.LocationBinder) service).getLocationUpdateService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationUpdatesService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFileLog = new FileLog(this, "location.txt", TAG);
        mWorkManager = WorkManager.getInstance();
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
        mFileLog.logString("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFileLog.logString("onResume");

        LocationUpdate locationUpdate = new LocationUpdate(this);
        locationUpdate.setLocationCallbacks();
        //mWorkManager.enqueue(OneTimeWorkRequest.from(LocationUpdateWork.class));

        //WorkRequest locationWork = new WorkRequest(LocationUpdateWork.class)
        //OneTimeWorkRequest o = new OneTimeWorkRequest(LocationUpdateWork.class).build();

        /*if (mLocationUpdatesService == null) {
            mLocationUpdatesService = new LocationUpdatesService();
        }

        mLocationReceiver = new LocationReceiver();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));*/
    }

    @Override
    protected void onPause() {
        mFileLog.logString("onPause");
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationReceiver);
        super.onPause();
    }

    @Override
    protected void onStart() {
        mFileLog.logString("onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        mFileLog.logString("onStop");
        /*if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }*/
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mFileLog.logString("onSaveInstanceState");
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
        mFileLog.logString("On Map Ready");
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

    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                mLocation = location;
                mFileLog.logLocation(mLocation);
            }
        }
    }

}
