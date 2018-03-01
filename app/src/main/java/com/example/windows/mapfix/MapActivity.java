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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import static com.example.windows.mapfix.R.id.chMetricUnits;

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
    public Stasiun[] stasiun = new Stasiun[9];
    public Train[] Trains = new Train[5];

    DatabaseReference markerStasiun = FirebaseDatabase.getInstance().getReference().child("Stasiun");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //Firebase.setAndroidContext(this);
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

        CheckBox chUseMetric = (CheckBox) this.findViewById(R.id.chMetricUnits);
        chUseMetric.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MapActivity.this.updateSpeed(null);
            }
        });

        addItemsOnSpinner1();
        your_position = (Spinner) findViewById(R.id.your_position);
        your_destination = (Spinner) findViewById(R.id.your_destination);
        buttonrute = (Button) findViewById(R.id.buttonrute);
        buttonrute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
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
        CheckBox chUseMetricUnits = (CheckBox) this.findViewById(chMetricUnits);
        return chUseMetricUnits.isChecked();
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
        final String stationlocation=location_provider.toString();


        markerStasiun.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                int i=0;
                for(com.google.firebase.database.DataSnapshot ds : dataSnapshot.getChildren()) {
                    String nama = ds.child("Nama").getValue(String.class);
                    double latitude = ds.child("Latitude").getValue(Double.class);
                    double longitude = ds.child("Longitude").getValue(Double.class);
                    Log.d("nama",nama);
                    Log.d("lat", String.valueOf(latitude));
                    stasiun[i] = new Stasiun(nama, latitude, longitude, stationlocation);
                    Log.d("Latitude", String.valueOf(stasiun[i].getLatitude()));
                    Log.d("Latitude", String.valueOf(stasiun[i].getLatitude()));
                    i++;
                }

                for (i = 0; i < stasiun.length; i++) {
                    double latitude = stasiun[i].getLatitude();
                    double longitude = stasiun[i].getLongitude();
                    String nama = stasiun[i].getNama();
                    Gmap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title(nama));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendRequest(){
        Log.d("asd", your_position.getSelectedItem().toString());
        String origin = your_position.getSelectedItem().toString();
        String destination = your_destination.getSelectedItem().toString();
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
        //ProgressDialog progressDialog = ProgressDialog.show(this, "Hang on a sec",
          //      "Finding route....", true);
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
        Log.d("asdwakanda", "asdwakandaaaaa");
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Rute route : routes) {
            Gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            //((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            //((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(Gmap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(Gmap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
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
