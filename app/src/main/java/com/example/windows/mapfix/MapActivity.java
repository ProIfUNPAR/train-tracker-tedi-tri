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
import android.widget.AdapterView;
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
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.windows.mapfix.R.id.chkMetricUnits;
import static com.example.windows.mapfix.R.id.logo_only;

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
    private Spinner your_train;
    private Spinner your_position;
    private Spinner your_destination;
    private Button buttonrute;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private Location currentLocation;
    public Stasiun[] stasiun = new Stasiun[97];
    public Train[] Trains = new Train[5];

    private HashMap<String, Stasiun> hash = new HashMap<String, Stasiun>();
	
	DatabaseReference markerStasiun = FirebaseDatabase.getInstance().getReference().child("Stasiun");
    DatabaseReference markerKereta = FirebaseDatabase.getInstance().getReference().child("Kereta");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //Firebase.setAndroidContext(this);
        initData();
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


        your_train = (Spinner) findViewById(R.id.your_train);
        your_train.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                 @Override
                                                 public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                                     addItemStasiun(your_train.getSelectedItemPosition());
                                                 }

                                                 @Override
                                                 public void onNothingSelected(AdapterView<?> adapterView) {

                                                 }

                                             });
        your_position = (Spinner) findViewById(R.id.your_position);
        your_destination = (Spinner) findViewById(R.id.your_destination);
        buttonrute = (Button) findViewById(R.id.buttonrute);
        buttonrute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doTrip(your_position.getSelectedItemPosition(),your_destination.getSelectedItemPosition(),your_train.getSelectedItemPosition());
                //sendRequest(Stasiun origin, Stasiun destination);
            }
        });


    }

    //@Override
    //public void onWindowFocusChanged(boolean hasFocus){
      public void initSpinner(){
        //super.onWindowFocusChanged(hasFocus);
        //if(hasFocus){
            //this.addItemsOnSpinner1();

            this.addItemKereta();
            this.addItemStasiun(0);
        //}
    }

    private void doTrip(int start, int stop,int index) {
        Gmap.clear();

        int count= stop-start;
        double speed = 12.5;
        double eta = 0;
        double etaH = 0;
        double etaM = 0;
        double totaldistance=0;
        for (int i = start; i < count; i++) {
            findPath(Trains[index].getStop(i),Trains[index].getStop(i+1));
            addMarker(Trains[index].getStop(i));
            Log.d(TAG, "doTrip: cari path dari "+Trains[index].getStop(i).getNama() + " "+i+" "+Trains[index].getStop(i+1).getNama()+" "+(i+1) );
            totaldistance += findDistance(Trains[index].getStop(i),Trains[index].getStop(i+1));

        }
        double totalDistanceKM = Math.floor(totaldistance/1000);
        Toast.makeText(this,"total distance : "+ totalDistanceKM + " KM",Toast.LENGTH_SHORT).show();
        eta = totaldistance/12.5;
        etaH = eta/3600;
        etaH = Math.floor(etaH);
        etaM = (eta%3600)/60;
        etaM = Math.floor(etaM);
        Toast.makeText(this,"estimated time of arrival " + etaH +" jam " + etaM + " menit",Toast.LENGTH_SHORT).show();
    }
    public void addMarker(Stasiun st){
        Gmap.addMarker(new MarkerOptions()
                .position(new LatLng(st.getLatitude(), st.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(st.getNama()));
    }
    public void findPath(Stasiun origin, Stasiun destination){


        Gmap.addMarker(new MarkerOptions()
                .position(new LatLng(origin.getLatitude(),origin.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(origin.getNama()));



        Gmap.addMarker(new MarkerOptions()
                .position(new LatLng(destination.getLatitude(),destination.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(destination.getNama()));


         Gmap.addPolyline(new PolylineOptions()
                .add(new LatLng(origin.getLatitude(), origin.getLongitude()),(new LatLng(destination.getLatitude(),destination.getLongitude())))
                .width(5)
                .color(Color.RED));



    }

    private double findDistance(Stasiun origin, Stasiun destination){
        String provider = location_provider.toString();
        Location curr=new Location("provider");
        curr.setLatitude(origin.getLatitude());
        curr.setLongitude(origin.getLongitude());

        Location dest = new Location("provider");
        dest.setLatitude(destination.getLatitude());
        dest.setLongitude(destination.getLongitude());

        return curr.distanceTo(dest);
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
        fmt.format(Locale.UK, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String strUnits = "km/jam";
        if(this.useMetricUnits()){
            strUnits = "meter/detik";
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

    private void initData() {
        location_provider = LocationServices.getFusedLocationProviderClient(this);
        final String stationlocation = location_provider.toString();
        ///////////////init stasiun////////////////

        markerStasiun.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                int i = 0;
                for (com.google.firebase.database.DataSnapshot ds : dataSnapshot.getChildren()) {
                    String nama = ds.child("Nama").getValue(String.class);
                    double latitude = ds.child("Latitude").getValue(Double.class);
                    double longitude = ds.child("Longitude").getValue(Double.class);
                    Log.d("nama", nama);
                    Log.d("lat", String.valueOf(latitude));
                    stasiun[i] = new Stasiun(nama, latitude, longitude, stationlocation);
                    Log.d("Latitude", String.valueOf(stasiun[i].getLatitude()));
                    Log.d("Latitude", String.valueOf(stasiun[i].getLatitude()));
                    i++;
                    Log.d(TAG, "array ke : "+i+" "+nama);
                }

                for (i = 0; i < stasiun.length; i++) {
                    double latitude = stasiun[i].getLatitude();
                    double longitude = stasiun[i].getLongitude();
                    String nama = stasiun[i].getNama();

                }
                initKereta();
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initKereta() {

        markerKereta.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                int mark=0;
                for(com.google.firebase.database.DataSnapshot ds : dataSnapshot.getChildren()) {
                    Integer x=1;
                    Integer temp=0;


                    String nama = ds.child("Nama").getValue(String.class);
                    long childCount=ds.getChildrenCount();
                    Log.d("asddasdas", childCount +"" );
                    Trains[mark]=new Train(nama);

                    for (int j = 0; j < childCount-1; j++) {
                        String xString = x + "";
                        temp=ds.child("Stasiun"+xString).getValue(Integer.class);
                        Trains[mark].addStasiun(stasiun[temp]);

                        Log.d(TAG, "Stasiun : "+ stasiun[temp].getNama()+" ditambahkan ke "+Trains[mark].getNama()+ " "+ mark);
                        Log.d(TAG, "onDataChange: "+ds.child("Stasiun"+xString).getValue(Integer.class));
                        x++;

                    }
                mark++;

                }
                initSpinner();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /**stasiun[0]=new Stasiun("Stasiun Hall Bandung",-6.9146455,107.6023063,stationlocation);
        stasiun[1]=new Stasiun("Stasiun Ciroyom",-6.914000, 107.590145,stationlocation);
        stasiun[2]=new Stasiun("Stasiun Cimindi",-6.895880, 107.561183,stationlocation);
        stasiun[3]=new Stasiun("Stasiun Cikudapateuh", -6.918831, 107.625903,stationlocation);
        stasiun[4]=new Stasiun("Stasiun Kiaracondong",-6.924929, 107.646303,stationlocation);
        stasiun[5]=new Stasiun("Stasiun Gedebage", -6.940873, 107.689515,stationlocation);
        stasiun[6]=new Stasiun("Stasiun Andir", -6.907938, 107.579256,stationlocation);
        stasiun[7]=new Stasiun("Stasiun Cimahi", -6.885427, 107.536122,stationlocation);
        stasiun[8]=new Stasiun("Stasiun Cicalengka",  -6.981199, 107.832652,stationlocation);
        stasiun[9]=new Stasiun("stasiun rancaekek",-6.963572, 107.755793,stationlocation);
        for(int i = 0;i<stasiun.length;i++){
            hash.put(stasiun[i].getNama(), stasiun[i]);
        }**/

        /////////////end init stasiun//////////////

        ////////////init kereta///////////////////
        /**Trains[0]=new Train("patas bandung");
        Trains[0].addStasiun(stasiun[4]);
        Trains[0].addStasiun(stasiun[5]);
        Trains[0].addStasiun(stasiun[9]);
        Trains[0].addStasiun(stasiun[8]);**/

        /**for (int i = 0; i <Trains[0].stasiun.size()-1 ; i++) {
            Stasiun temp=Trains[0].getStop(i);
            Gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(temp.getLatitude(),temp.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(temp.getNama()));
        }*/
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
        initData();
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
        this.desPos = findViewById(R.id.your_destination);
        String[] namaStasiun = null;
        for (int i=0; i<Trains.length; i++){
            if (i==your_train.getSelectedItemPosition()){
                namaStasiun=new String[Trains[i].getStasiun().size()];
                for (int j=0; j<Trains[i].getStasiun().size(); j++){
                    namaStasiun[j]=Trains[i].getStop(j).getNama();
                }
            }
        }
        ArrayAdapter<String> firstPosition = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, namaStasiun);
        this.firstPos.setAdapter(firstPosition);

        ArrayAdapter<String> destinationPosition = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, namaStasiun);
        this.desPos.setAdapter(destinationPosition);
    }

    public void addItemKereta() {
        this.your_train = findViewById(R.id.your_train);
        String[] namaKereta=new String[2];
        if (namaKereta==null){
            Log.d("Trains", "null");
            addItemKereta();
        }
        else {
            for(int i=0; i<namaKereta.length; i++){
                namaKereta[i]=Trains[i].getNama();
                Log.d("Train"+i, Trains[i].getNama()+"");
            }
        }
        ArrayAdapter<String> trainList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, namaKereta);
        this.your_train.setAdapter(trainList);
    }

    public void addItemStasiun(int index) {
        this.your_position = findViewById(R.id.your_position);
        this.your_destination=findViewById(R.id.your_destination);

        String[] namaStasiun=new String[Trains[index].getStasiun().size()];

            for(int i=0; i<Trains[index].getStasiun().size(); i++){
                namaStasiun[i]=Trains[index].getStop(i).getNama();
                Log.d("Train spinner"+i, Trains[index].getStop(i).getNama()+"");
            }

        ArrayAdapter<String> trainList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, namaStasiun);
        this.your_position.setAdapter(trainList);
        this.your_destination.setAdapter(trainList);
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
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
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
