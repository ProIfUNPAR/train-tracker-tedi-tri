package com.example.windows.mapfix;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import static com.example.windows.mapfix.R.id.chkMetricUnits;

/**
 * Created by Windows on 06/02/2018.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, RouteFinderListener, IBaseGpsListener {
    protected Spinner firstPos, desPos;// dropdown first position, destination position
    private static final String TAG = "MapActivity";
    private static final String fine_loc = "Manifest.permission.ACCESS_FINE_LOCATION";
    private static final String coarse_loc = "Manifest.permission.ACCESS_COARSE_LOCATION";
    private static final int Location_permission_request_code = 1234;
    private boolean locPermission = false;
    private GoogleMap Gmap;
    private FusedLocationProviderClient location_provider;
    private Spinner your_position;
    private Spinner your_destination;
    private Button buttonrute;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private Location currentLocation;
    public Stasiun[] stasiun = new Stasiun[15];
    public Train[] Trains = new Train[5];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getLocationPermission();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);

        CheckBox chkUseMetricUnits = (CheckBox) this.findViewById(R.id.chkMetricUnits);
        chkUseMetricUnits.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MapActivity.this.updateSpeed(null);
            }
        });

        addItemsOnSpinner1();
    }
    public void finish(){
        super.finish();
        System.exit(0);
    }

    private void updateSpeed(CLocation location) {
        float nCurrentSpeed =0;

        if(location != null){
            location.setUseMetricunits(this.useMetricUnits());
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
        CheckBox chkUseMetricUnits = (CheckBox) this.findViewById(R.id.chkMetricUnits);
        return chkUseMetricUnits.isChecked();
    }

    @Override
    public void onLocationChanged(Location location){
        if(location != null){
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this,"Map Ready",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        Gmap = googleMap;
        Intent intent = new Intent(this, TrackService.class);
        startService(intent);
        Toast.makeText(this, "tracking location", Toast.LENGTH_SHORT).show();
        if (locPermission) {

            getDeviceLocation();

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
            Gmap.setMyLocationEnabled(true);
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

        /**for (int i = 0; i <Trains[0].stasiun.size()-1 ; i++) {
            Stasiun temp=Trains[0].getStop(i);
            Gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(temp.getLatitude(),temp.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(temp.getNama()));
        }*/




        /////////////////////////////////////////

      /**for (int i = 0; i <stasiun.length-1 ; i++) {
            Gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(stasiun[i].getLatitude(), stasiun[i].getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(stasiun[i].getNama()));
        }*/

    }

    private void sendRequest(){
        String origin = your_position.getSelectedItem().toString();
        String destination = your_destination.getSelectedItem().toString();
        if(origin.isEmpty()){
            Toast.makeText(this, "Please select origin station!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(destination.isEmpty()){
            Toast.makeText(this, "Please select destination station!", Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            new Route(this, origin, destination).execute();
        }
        catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
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
                            currentLocation=(Location)task.getResult();
                            if(currentLocation!=null) {
                                Gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f));
                                initStation();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"cannot found location",Toast.LENGTH_LONG).show();

                            }
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


    /**private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: camera move to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        Gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }*/

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

    public void addItemsOnSpinner1() {
        this.firstPos = findViewById(R.id.your_position);
        ArrayAdapter<CharSequence> firstPosition = ArrayAdapter.createFromResource(this, R.array.station, android.R.layout.simple_spinner_item);
        firstPosition.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.firstPos.setAdapter(firstPosition);

        this.desPos = findViewById(R.id.your_destination);
        ArrayAdapter<CharSequence> destinationPosition = ArrayAdapter.createFromResource(this, R.array.station, android.R.layout.simple_spinner_item);
        destinationPosition.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.desPos.setAdapter(destinationPosition);
    }

    @Override
    public void onRouteFinderStart() {
        ProgressDialog progressDialog = ProgressDialog.show(this, "Hang on a sec",
                "Finding route....", true);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onRouteFinderSuccess(List<Rute> routes) {
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Rute route : routes) {
            Gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            //((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            //((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(Gmap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(Gmap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(Gmap.addPolyline(polylineOptions));
        }
    }

}
