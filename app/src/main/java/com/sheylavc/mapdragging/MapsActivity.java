package com.sheylavc.mapdragging;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>,
        GoogleMap.OnCameraIdleListener,
        LocationListener,
        OnMapReadyCallback{

    private static final String TAG = MapsActivity.class.getName();




    // Códigos de petición
    int PERMISSION_CODE_1 = 23;
    public static final int REQUEST_LOCATION = 1;
    public static final int REQUEST_CHECK_SETTINGS = 2;

    private static final String LOCATION_KEY = "location-key";

    private TrackGPS gps;



//    public static final CameraPosition BONDI =
//            new CameraPosition.Builder().target(new LatLng(-11.8930656,-76.8825468))
//                    .zoom(15.5f)
//                    .bearing(300)
//                    .tilt(50)
//                    .build();
//
//    public static final CameraPosition CLIENTE =
//            new CameraPosition.Builder().target(new LatLng(-33.87365, 151.20689))
//                    .zoom(15.5f)
//                    .bearing(0)
//                    .tilt(25)
//                    .build();


    private GoogleMap mMap;
    private TextView markerText;
    private TextView Address;

    // Location API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LatLng mLastLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        markerText = (TextView) findViewById(R.id.locationMarkertext);
        Address = (TextView) findViewById(R.id.adressText);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        gps = new TrackGPS(MapsActivity.this);



        if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION, Toast.LENGTH_SHORT).show();

                    requestpermisions();

                } else {

                    // No explanation needed, we can request the permission.

                    requestpermisions();

                }
            }

            if(gps.canGetLocation()){

                String stringLatitude = String.valueOf(gps.latitude);
                String stringLongitude = String.valueOf(gps.longitude);
                Toast.makeText(getApplicationContext(),"Longitude:"+stringLongitude +"\nLatitude:"+stringLatitude, Toast.LENGTH_SHORT).show();


            }
            else
            {

                gps.showSettingsAlert();
            }

        } else {
            if(gps.canGetLocation()){

                String stringLatitude = String.valueOf(gps.latitude);
                String stringLongitude = String.valueOf(gps.longitude);
                Toast.makeText(getApplicationContext(),"Longitude:"+stringLongitude +"\nLatitude:"+stringLatitude, Toast.LENGTH_SHORT).show();


            }
            else
            {

                gps.showSettingsAlert();
            }
        }



        // Establecer punto de entrada para la API de ubicación
        buildGoogleApiClient();

        // Crear opciones de peticiones
        buildLocationSettingsRequest();

        updateValuesFromBundle(savedInstanceState);

        // Verificar ajustes de ubicación actuales
        checkLocationSettings();



    }

    public void requestpermisions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE_1);

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Protegemos la ubicación actual antes del cambio de configuración
        outState.putParcelable(LOCATION_KEY, mLastLocation);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d(TAG, "El usuario permitió el cambio de ajustes de ubicación.");

                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d(TAG, "El usuario no permitió el cambio de ajustes de ubicación");
                        break;
                }
                break;
        }
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


        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnCameraIdleListener(this);


        LatLng sydney = new LatLng(-12.0123946,-77.0984446);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18));


    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .enableAutoManage(this, this)
                .build();
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest)
                .setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    /**
     *
     */
    @Override
    public void onCameraIdle() {
        CameraPosition currentCameraPosition = mMap.getCameraPosition();
        mLastLocation = new LatLng(currentCameraPosition.target.latitude, currentCameraPosition.target.longitude);

        updateLocationUI();
//        CameraPosition currentCameraPosition = mMap.getCameraPosition();
//        Address.setText(currentCameraPosition.target.latitude + " / "+ currentCameraPosition.target.longitude);

 }



    private void updateLocationUI() {

//        CameraPosition currentCameraPosition = mMap.getCameraPosition();
//        Address.setText(currentCameraPosition.target.latitude + " / "+ currentCameraPosition.target.longitude);

//        Address.setText(String.valueOf(mLastLocation.latitude) + " / "+String.valueOf(mLastLocation.longitude));
        Address.setText(getAddress(this));

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LOCATION_KEY)) {
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);

                updateLocationUI();
            }

        }
    }

    public  String getAddress(Context context){
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<android.location.Address> list = geocoder.getFromLocation(mLastLocation.latitude , mLastLocation.longitude, 1);
            if (!list.isEmpty()) {
                Address address = list.get(0);
                Log.i("Mi direcci—n es:"," \n" + address.getAddressLine(0));
                return address.getAddressLine(0);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    private void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient, mLocationSettingsRequest
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "Los ajustes de ubicación satisfacen la configuración.");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            Log.d(TAG, "Los ajustes de ubicación no satisfacen la configuración. " +
                                    "Se mostrará un diálogo de ayuda.");
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.d(TAG, "El Intent del diálogo no funcionó.");
                            // Sin operaciones
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG, "Los ajustes de ubicación no son apropiados.");
                        break;

                }
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "Detección de actividad iniciada");

        } else {
            Log.e(TAG, "Error al iniciar/remover la detección de actividad: "
                    + status.getStatusMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}


