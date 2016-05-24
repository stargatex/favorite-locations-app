package com.example.lkj.mylocator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, LocationListener {

    LocationManager locationManager;
    String provider;
    Double mLat, mLng;
    Firebase firebase;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mgoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        mLat = i.getDoubleExtra("lat", 0);
        mLng = i.getDoubleExtra("lng", 0);
        Log.i("LJ_Place_lat", Double.toString(i.getDoubleExtra("lat", 0)));
        Log.i("LJ_Place_lng", Double.toString(i.getDoubleExtra("lng", 0)));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        checkPermission();
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLat == 0 || mLng == 0) {
            checkPermission();
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        if (mLat != 0 && mLng != 0) {
            checkPermission();
            LatLng markerLocation = new LatLng(mLat, mLng);
            locationManager.removeUpdates(this);
            String label = geoCoderLocationData(markerLocation);

            addMapMarker(markerLocation, label, Constants.USER_LOCATION_FAV_MARKER);

        } else {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }

    }

    private void addMapMarker(LatLng markerLocation, String label, int userLocationMarker) {
        // mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.addMarker(new MarkerOptions().position(markerLocation).title(label).icon(BitmapDescriptorFactory.defaultMarker(makerColor(userLocationMarker))));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 10.0f), 400, null);
    }

    private float makerColor(int userLocationMarker) {
        switch (userLocationMarker) {
            case Constants.USER_LOCATION_MARKER:
                return BitmapDescriptorFactory.HUE_GREEN;
            case Constants.USER_LOCATION_ADD_MARKER:
                return BitmapDescriptorFactory.HUE_RED;
            case Constants.USER_LOCATION_FAV_MARKER:
                return BitmapDescriptorFactory.HUE_BLUE;
            default:
                return 0;
        }
    }


    @Override
    public void onMapLongClick(LatLng latLng) {

        String label = geoCoderLocationData(latLng);
        Log.i("LJ_ad", label);

        firebase = new Firebase(Constants.FIREBASE_URL);
        Firebase firebaseUser = new Firebase(Constants.FIREBASE_USERS + firebase.getAuth().getUid());
        GeoFire geoFire = new GeoFire(firebaseUser);
        geoFire.setLocation(label, new GeoLocation(latLng.latitude, latLng.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, FirebaseError error) {
                if (error != null) {
                    Toast.makeText(getApplicationContext(), "There was an error saving the location  " + error, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Location saved on server successfully!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mMap.clear();
        addMapMarker(latLng, label, Constants.USER_LOCATION_ADD_MARKER);
    }

    private String geoCoderLocationData(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String label = "Unknown location ".concat(new Date().toString());
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addressList != null && addressList.size() > 0) {
                label = addressList.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return label;
    }

    @Override
    public void onLocationChanged(Location userloc) {
        if (mMap != null) {
            mMap.clear();
            LatLng updatedLocation = new LatLng(userloc.getLatitude(), userloc.getLongitude());
            String label = geoCoderLocationData(updatedLocation);
            addMapMarker(updatedLocation, "You're at " + label, Constants.USER_LOCATION_MARKER);

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        checkPermission();
        locationManager.removeUpdates(this);
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

}
