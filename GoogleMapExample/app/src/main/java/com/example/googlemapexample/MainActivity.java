package com.example.googlemapexample;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = "googleMaps";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationResult mLocationUpdate;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        if (servicesOK()) {
            setLocationsCallbacks();
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
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
    }

    private void setLocationsCallbacks() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLocation = location;
                        }
                    }
                });
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    mLocationUpdate = null;
                    return;
                }
                mLocationUpdate = locationResult;
                gotoLocation(mLocationUpdate.getLastLocation().getLatitude(),
                        mLocationUpdate.getLastLocation().getLongitude(), 17);
            }
        };
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

        if (mLocation != null) {
            gotoLocation(mLocation.getLatitude(), mLocation.getLongitude(), 17);
        } else {
            gotoLocation(15.646596, 73.716619, 10);
        }
        // Batch Location Request
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setMaxWaitTime(6000);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        Toast.makeText(this, "On Map Ready", Toast.LENGTH_LONG).show();
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
        Toast.makeText(this, "New coordinates arrived", Toast.LENGTH_LONG).show();
        // Move the camera
        LatLng location = new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Your Position"));
    }

    private LatLng getCurrentLocation() {
        return new LatLng(mLocationUpdate.getLastLocation().getLatitude(),
                mLocationUpdate.getLastLocation().getLongitude());
    }

    public boolean servicesOK() {
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

    public void showCurrentLocation(MenuItem item) {

    }
}
