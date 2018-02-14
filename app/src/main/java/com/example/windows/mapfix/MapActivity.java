package com.example.windows.mapfix;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.windows.mapfix.R;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Windows on 06/02/2018.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback , IBaseGpsListener {

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this,"Map Ready",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        Gmap=googleMap;

        if(locPermission){
            getDeviceLocation();
            Gmap.setMyLocationEnabled(true);

        }
    }
    private static final String TAG="MApActivity";

    private static final String fine_loc="Manifest.permission.ACCESS_FINE_LOCATION";
    private static final String coarse_loc="Manifest.permission.ACCESS_COARSE_LOCATION";
    private static final int Location_permission_request_code=1234;
    private boolean locPermission=false;
    private GoogleMap Gmap;
    private FusedLocationProviderClient location_provider;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getLocationPermission();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this );
        this.updateSpeed(null);

        CheckBox chUseMetric = (CheckBox) this.findViewById(R.id.chMetricUnits);
        chUseMetric.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MapActivity.this.updateSpeed(null);
            }
        });
    }

    public void finish(){
        super.finish();
        System.exit(0);
    }

    private void updateSpeed(CLocation location) {
        float nCurrentSpeed =0;

        if(location != null){
            location.setUseMetricUnits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String strUnits = "miles/hour";
        if(this.useMetricUnits()){
            strUnits = "meters/second";
        }
        TextView txtCurrentSpeed = (TextView) this.findViewById(R.id.txtCurrentSpeed);
        txtCurrentSpeed.setText(strCurrentSpeed + " "+ strUnits);
    }

    private boolean useMetricUnits() {
        CheckBox chUseMetricUnits = (CheckBox) this.findViewById(R.id.chMetricUnits);
        return chUseMetricUnits.isChecked();
    }


    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: get current location");

        location_provider= LocationServices.getFusedLocationProviderClient(this);

        try{
            if(locPermission){
                Task location=location_provider.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: location found");
                            Location currentLocation=(Location)task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),15f);

                        }else{
                            Log.d(TAG, "onComplete: location not found");
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
   }


    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: camera move to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        Gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        String[] permissions={Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                fine_loc)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    coarse_loc)==PackageManager.PERMISSION_GRANTED){
                locPermission=true;
                initMap();
            }
            else{
                ActivityCompat.requestPermissions(this,permissions,Location_permission_request_code);
            }


        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    Location_permission_request_code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locPermission=false;

        switch(requestCode){
            case Location_permission_request_code:{
                if(grantResults.length>0){
                    for (int i = 0; i < grantResults.length ; i++) {
                        if(grantResults[i] !=PackageManager.PERMISSION_GRANTED){
                            locPermission=false;
                            return;
                        }
                    }
                    locPermission=true;
                    //init map
                    initMap();
                }
            }
        }
    }


}
