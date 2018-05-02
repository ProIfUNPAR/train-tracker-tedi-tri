package com.example.windows.mapfix;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.windows.mapfix.ScreenSlideActivity;
import com.example.windows.mapfix.java.time.Stops;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.windows.mapfix.Fragment2.stops;
import static com.google.android.gms.cast.CastRemoteDisplayLocalService.startService;

public class Fragment1 extends Fragment implements IBaseGpsListener {
    static Adapter adapter;
    private boolean locPermission = true;
    private static final String TAG = "MapActivity";
    private Spinner your_train;
    private Spinner your_position;
    private Spinner your_destination;
    private Button buttonrute;
    public static double currentSpeed = 0.0;
    public int distance = 500;
    public static double totaldistance = 0;

    public TextView txtCurrentSpeed;
    public static String curSpeed;
    protected Spinner firstPos, desPos;
    MapView gMapView;
    private Location currentLocation;
    private GoogleMap Gmap;
    private FusedLocationProviderClient location_provider;
    private GeofencingClient mGeofencingClient;
    public Notification1 notif = new Notification1();

    static ArrayList<Stops> next_stop = new ArrayList();

    TrackService tService;

    boolean isBound = false;

    public Fragment1() {

    }

    private ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TrackService.LocalBinder binder=(TrackService.LocalBinder) iBinder;
            tService=binder.getService();
            isBound=true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound=false;
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getContext(), TrackService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(mConnection);
        isBound = false;
    }

    public String getSpeed(){
        if(isBound){
            String speed=TrackService.speedtxt;
            return speed;
        }else{
            return "0";
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Handler handler =new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 2000);
                String speed= getSpeed();
                if (speed != null){
                    txtCurrentSpeed.setText(speed + " KM/H");
                }
                else{
                    txtCurrentSpeed.setText("0 KM/H");
                }
                Log.d(TAG, "run: speed is"+speed);
            }
        };
        handler.postDelayed(r, 0000);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment1, container, false);

        gMapView = rootView.findViewById(R.id.mapView);
        gMapView.onCreate(savedInstanceState);
        gMapView.onResume(); // needed to get the map to display immediately
        //getLocationPermission();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        gMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                Gmap = mMap;
                Intent intent = new Intent(getActivity(), TrackService.class);
                getActivity().startService(intent);
                // For showing a move to my location button
                Log.d(TAG, "onMapReady: cari current ");

                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        });




        /*
        chkUseMetricUnits.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                com.example.windows.mapfix.Fragment1.updateSpeed(null);
            }
        });
        */

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        txtCurrentSpeed = (TextView) getView().findViewById(R.id.txtCurrentSpeed);
        Log.d("textview",getView().findViewById(R.id.txtCurrentSpeed)+"");

        //this.updateSpeed(null);
        //CheckBox chkUseMetricUnits = (CheckBox) getView().findViewById(R.id.chkMetricUnits);
        getDeviceLocation();
        your_train = getView().findViewById(R.id.your_train);
        your_train.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                addItemStasiun(your_train.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
        addItemKereta();
        your_position = getView().findViewById(R.id.your_position);
        your_destination = getView().findViewById(R.id.your_destination);
        buttonrute = getView().findViewById(R.id.buttonrute);
        buttonrute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(next_stop.size()!=0){
                    next_stop.clear();
                }
                Gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f));
                Gmap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(MainActivity.ArrayTrain[your_train.getSelectedItemPosition()].getStop(your_position.getSelectedItemPosition()).getLatitude(),MainActivity.ArrayTrain[your_train.getSelectedItemPosition()].getStop(your_position.getSelectedItemPosition()).getLongitude())));
                doTrip(your_position.getSelectedItemPosition(), your_destination.getSelectedItemPosition(), your_train.getSelectedItemPosition());
            }
        });
        //int n = savedInstanceState.getInt("value");
        //your_train.setSelection(n);
    }


    @Override
    public void onResume() {
        super.onResume();
        gMapView.onResume();
        Intent intent = new Intent(getActivity(), TrackService.class);
        getActivity().startService(intent);

    }

    @Override
    public void onPause() {
        super.onPause();
        gMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gMapView.onDestroy();

        Log.d(TAG, "onDestroy: destroy");

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        gMapView.onLowMemory();
    }


    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: get current location");
        location_provider= LocationServices.getFusedLocationProviderClient(getActivity());
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
                                Toast.makeText(getContext(),"cannot found location",Toast.LENGTH_LONG).show();

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





    public void addItemKereta() {
        this.your_train = getView().findViewById(R.id.your_train);
        String[] namaKereta=new String[MainActivity.ArrayTrain.length];

        for(int i=0; i<namaKereta.length; i++){
            namaKereta[i]=MainActivity.ArrayTrain[i].getNama();
            Log.d("Train"+i, MainActivity.ArrayTrain[i].getNama()+"");
        }

        ArrayAdapter<String> trainList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, namaKereta);
        this.your_train.setAdapter(trainList);

        this.your_train.setSelection(MainActivity.selected);
    }

    public void addItemStasiun(int index) {
        this.your_position = getView().findViewById(R.id.your_position);
        this.your_destination=getView().findViewById(R.id.your_destination);

        String[] namaStasiun=new String[MainActivity.ArrayTrain[index].getStasiun().size()];

        for(int i=0; i<MainActivity.ArrayTrain[index].getStasiun().size(); i++){
            namaStasiun[i]=MainActivity.ArrayTrain[index].getStop(i).getNama();
            Log.d("Train spinner"+i, MainActivity.ArrayTrain[index].getStop(i).getNama()+"");
        }

        ArrayAdapter<String> trainList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, namaStasiun);
        this.your_position.setAdapter(trainList);
        this.your_destination.setAdapter(trainList);
    }



    private void doTrip(int start, int stop,int index) {
        Gmap.clear();
        next_stop.clear();
        int[]indexs = new int[Math.abs(start-stop)];
        double tempDistance = 0;
        //int count= stop-start;
        double speed = 19.5;
        double tempETA = 0;
        double eta = 0;
        double etaH = 0;
        double etaM = 0;
        //Log.d(TAG, "doTrip: start:"+start+"stop: "+stop+"count: "+count);
        if(start > stop){
            totaldistance = 0;
            tempETA = 0;
            for (int j = start; j > stop; j--) {
                findPath(MainActivity.ArrayTrain[index].getStop(j), MainActivity.ArrayTrain[index].getStop(j - 1));
                addMarker(MainActivity.ArrayTrain[index].getStop(j));
                //Log.d(TAG, "doTrip: cari path dari "+MainActivity.ArrayTrain[index].getStop(i).getNama() + " "+i+" "+MainActivity.ArrayTrain[index].getStop(i+1).getNama()+" "+(i+1) );
                tempDistance = findDistance(MainActivity.ArrayTrain[index].getStop(j), MainActivity.ArrayTrain[index].getStop(j - 1));
                totaldistance += tempDistance;
                tempETA = totaldistance / speed;
                Log.d("tempeta", tempETA + "");
                next_stop.add(new Stops(MainActivity.ArrayTrain[index].getStop(j-1), totaldistance , tempETA));
//                Log.d(TAG, "doTrip: next stop:" + next_stop.get(j - 1).getStasiun().getNama());
                tempDistance = 0;
                tempETA = 0;
            }

        }
        else {
            totaldistance = 0;
            tempETA = 0;
            for (int i = start; i < stop; i++) {
                findPath(MainActivity.ArrayTrain[index].getStop(i), MainActivity.ArrayTrain[index].getStop(i + 1));
                addMarker(MainActivity.ArrayTrain[index].getStop(i));
                //Log.d(TAG, "doTrip: cari path dari "+MainActivity.ArrayTrain[index].getStop(i).getNama() + " "+i+" "+MainActivity.ArrayTrain[index].getStop(i+1).getNama()+" "+(i+1) );
                tempDistance = findDistance(MainActivity.ArrayTrain[index].getStop(i), MainActivity.ArrayTrain[index].getStop(i + 1));
                totaldistance += tempDistance;
                tempETA = totaldistance / speed;
                next_stop.add(new Stops(MainActivity.ArrayTrain[index].getStop(i + 1), totaldistance, tempETA));
                Log.d(TAG, "doTrip: next stop:" + next_stop.get(i - start).getStasiun().getNama());
                tempDistance = 0;
                tempETA = 0;
            }
        }
        double totalDistanceKM = Math.floor(totaldistance/1000);

        eta = totaldistance/speed;
        etaH = eta/3600;
        etaH = Math.floor(etaH);
        etaM = Math.ceil((eta%3600)/60);
        if(totaldistance<1000){
            Toast.makeText(getActivity(),"total distance : "+ String.format("%.0f",totaldistance) + " M",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getActivity(),"total distance : "+ totalDistanceKM + " KM",Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(getActivity(),"estimated time of arrival " + String.format("%.0f",etaH) +" hour(s) " + String.format("%.0f",etaM) + " minute(s)",Toast.LENGTH_SHORT).show();
        for (int i = 0; i < next_stop.size() ; i++) {
            Log.d(TAG, "doTrip: ns"+next_stop.get(i).getStasiun().getNama());
        }
        changeData();
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



    private void updateSpeed(CLocation location) {
        float nCurrentSpeed =0;

        if(location != null){
           // location.setUseMetricunits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.UK, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String strUnits = "km/jam";
        /*if(this.useMetricUnits()){
            strUnits = "meter/detik";
        }*/
        TextView txtCurrentSpeed = (TextView) getView().findViewById(R.id.txtCurrentSpeed);
        txtCurrentSpeed.setText(strCurrentSpeed + " "+ strUnits);
    }

    /*
    private boolean useMetricUnits() {
        CheckBox chkUseMetricUnits = (CheckBox) getView().findViewById(R.id.chkMetricUnits);
        return chkUseMetricUnits.isChecked();
    }*/

    public void onLocationChanged(Location location){
        if(location != null){
            CLocation myLocation = new CLocation(location);
            this.updateSpeed(myLocation);
        }
        //Log.d(TAG, "onLocationChanged: kkk");
        txtCurrentSpeed.setText(curSpeed);



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
    /**public static  void setText(String text){
        txtCurrentSpeed.setText(text);
    }*/

    public void changeData(){
        //next_stop.clear();
        Fragment2.stops=Fragment1.next_stop;
        adapter=new Adapter(getContext(),stops);
        Fragment2.list.setAdapter(adapter);
        Log.d(TAG, "changeData: ");
    }

}



