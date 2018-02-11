package com.example.windows.mapfix;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Windows on 06/02/2018.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    protected Spinner firstPos, desPos;// dropdown first position, destination position


    private static final String TAG="MapActivity";

    private static final String fine_loc="Manifest.permission.ACCESS_FINE_LOCATION";
    private static final String coarse_loc="Manifest.permission.ACCESS_COARSE_LOCATION";
    private static final int Location_permission_request_code=1234;
    private boolean locPermission=false;
    private GoogleMap Gmap;
    private FusedLocationProviderClient location_provider;

    public Stasiun[] stasiun=new Stasiun[7];
    public Train[] Trains=new Train[5];

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this,"Map Ready",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        Gmap=googleMap;


        if(locPermission){

            getDeviceLocation();

            //Gmap.setMyLocationEnabled(true);

        }
    }

    private void initStation() {
        location_provider= LocationServices.getFusedLocationProviderClient(this);
        String stationlocation=location_provider.toString();
        ///////////////init stasiun////////////////

        stasiun[0]=new Stasiun("Stasiun Hall Bandung",-6.9146455,107.6023063,stationlocation);
        stasiun[1]=new Stasiun("Stasiun Ciroyom",-6.914000, 107.590145,stationlocation);
        stasiun[2]=new Stasiun("Stasiun Cimindi",-6.895880, 107.561183,stationlocation);
        stasiun[3]=new Stasiun("Stasiun Cikudapateuh", -6.918831, 107.625903,stationlocation);
        stasiun[4]=new Stasiun("Stasiun Kiaracondong",-6.924929, 107.646303,stationlocation);
        stasiun[5]=new Stasiun("Stasiun Gedebage", -6.940873, 107.689515,stationlocation);
        stasiun[6]=new Stasiun("Stasiun Andir", -6.907938, 107.579256,stationlocation);
        stasiun[7]=new Stasiun("Stasiun Cimahi", -6.885427, 107.536122,stationlocation);
        stasiun[8]=new Stasiun("Stasiun Cicalengka",  -6.981199, 107.832652,stationlocation);
        stasiun[9]=new Stasiun("stasiun rancaekek",-6.963572, 107.755793,stationlocation);


        /////////////end init stasiun//////////////

        ////////////init kereta///////////////////
        Trains[0]=new Train("patas bandung");
        Trains[0].addStasiun(stasiun[4]);
        Trains[0].addStasiun(stasiun[5]);
        Trains[0].addStasiun(stasiun[9]);
        Trains[0].addStasiun(stasiun[8]);

        for (int i = 0; i <Trains[0].stasiun.size()-1 ; i++) {
            Gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(stasiun[i].getLatitude(), stasiun[i].getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(stasiun[i].getNama()));
        }




        /////////////////////////////////////////

      /**for (int i = 0; i <stasiun.length-1 ; i++) {
            Gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(stasiun[i].getLatitude(), stasiun[i].getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(stasiun[i].getNama()));
        }*/

    }




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getLocationPermission();

        this.addItemsOnSpinner1();
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

                            Gmap.addMarker(new MarkerOptions()
                                    .position(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .title("current location"));
                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),15f);
                            initStation();
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

    public void addItemsOnSpinner1(){
        this.firstPos=findViewById(R.id.your_position);
        ArrayAdapter<CharSequence> firstPosition=ArrayAdapter.createFromResource(this, R.array.station, android.R.layout.simple_spinner_item);
        firstPosition.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.firstPos.setAdapter(firstPosition);

        this.desPos=findViewById(R.id.your_destination);
        ArrayAdapter<CharSequence> destinationPosition=ArrayAdapter.createFromResource(this, R.array.station, android.R.layout.simple_spinner_item);
        destinationPosition.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.desPos.setAdapter(destinationPosition);
    }
}
