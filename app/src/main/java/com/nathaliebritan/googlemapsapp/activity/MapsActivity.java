package com.nathaliebritan.googlemapsapp.activity;


import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nathaliebritan.googlemapsapp.R;


import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MapsActivity";
    private final static int MAX_PINS = 26;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;

    private GoogleMap mMap;
    private  SupportMapFragment mapFragment;
    private Button btnAddPin;
    private TextView txtLocation;

    private int currPinNumber = 0;
    private double longitude;
    private double latitude;
    private Random mRandom;

    private AdView mAdView;
    private AdRequest mAdRequest;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        btnAddPin = (Button) findViewById(R.id.btn_add);
        txtLocation = (TextView) findViewById(R.id.txt_location);
        mAdView = (AdView) findViewById(R.id.adView);

        mAdRequest = new AdRequest.Builder().build();
        mAdView.loadAd(mAdRequest);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mTracker = getDefaultTracker();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Maps Activity")
                .setAction("Created")
                .setValue(1)
                .build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

        if (mAdView != null) {
            mAdView.resume();
        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Maps Activity")
                .setAction("Resumed")
                .setValue(1)
                .build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            txtLocation.setText("Location is " + Double.toString(mLocation.getLatitude()) +
                    ", " + Double.toString(mLocation.getLongitude()));
        }

        if (mLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);}
        else {
            txtLocation.setText("Location is " + Double.toString(mLocation.getLatitude()) +
                    ", " + Double.toString(mLocation.getLongitude()));
        }

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(50, 50, 10, 50);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mRandom = new Random();
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();

        btnAddPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Button")
                        .setAction("add pin")
                        .setValue(1)
                        .build());

                if (currPinNumber < MAX_PINS) {
                    currPinNumber++;
                    latitude = mRandom.nextInt(49410658 - 49400828);
                    longitude = mRandom.nextInt(32075820 - 32049041);
                    LatLng tmpCoord = new LatLng(latitude / 1000000 + 49.400828, longitude / 1000000 + 32.049041);

                    mMap.addMarker(new MarkerOptions().position(tmpCoord).title(Character.toString((char) (currPinNumber + 65))));

                    builder.include(tmpCoord);
                    LatLngBounds bounds = builder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 26));
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        txtLocation.setText("Location is " + Double.toString(location.getLatitude()) +
                ", " + Double.toString(location.getLongitude()));
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(location.getLatitude(),location.getLongitude()))
                .title("I am here!");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));

    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.layout.activity_maps);
        }
        return mTracker;
    }


}
