package com.example.windows.mapfix;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG= "MainActivity";
    protected Spinner selectTrain;
    protected TextView textView;
   // Vibrator vibrator;

    public static int selected;
    public static FusedLocationProviderClient location_provider;

    DatabaseReference markerStasiun = FirebaseDatabase.getInstance().getReference().child("Stasiun");
    DatabaseReference markerKereta = FirebaseDatabase.getInstance().getReference().child("Kereta");

    public static Stasiun[] ArrayStasiun = new Stasiun[99];
    public static Train[] ArrayTrain = new Train[21];
    private static final int ERROR_DIALOG_REQUEST=9001;
    //Button btnNotif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.selectTrain = findViewById(R.id.spinner);


        if(isServicesok()) {
            init();
        }
        initData();

    }





    private void init(){


        Button btnMap = (Button) findViewById(R.id.btnMap);

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, ScreenSlideActivity.class);
                selectTrain = (Spinner) findViewById(R.id.spinner);
                selected = selectTrain.getSelectedItemPosition();


                startActivity(intent);
            }
        });
        askPermissions();

        /*
        btnNotif = (Button)findViewById(R.id.buttonNotification);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        btnNotif.setOnClickListener(
                new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        PendingIntent pIntent = PendingIntent.getActivity(com.example.windows.mapfix.MainActivity.this,0,intent,0 );
                        Notification noti = new Notification.Builder(com.example.windows.mapfix.MainActivity.this)
                                .setTicker("TickerTitle")
                                .setContentTitle("Train Tracker")
                                .setContentText("Stasiun Berikutnya :                                           "+"Jarak   : ")
                                .setSmallIcon(R.drawable.icon1)
                                .setContentIntent(pIntent).getNotification();
                        vibrator.vibrate(10000);
                        noti.flags=Notification.FLAG_AUTO_CANCEL;
                        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                        nm.notify(0, noti);
                    }
                }
        );
        */

    }



    public boolean isServicesok(){
        Log.d(TAG, "isServicesok: cek versi google service");
        int versi = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if(versi== ConnectionResult.SUCCESS){
            //bisa request
            Log.d(TAG, "isServicesok: google service ok");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(versi)){
            Log.d(TAG, "isServicesok: error");
            Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,versi,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this,"cant make map request",Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    public void askPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ){
            //completed = true;
            //button.setText("Permissions supplied. Press to continue");
        }

    }
    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            //start an intent to the class above. Do this because the app does not have enough permissions

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
                    ArrayStasiun[i] = new Stasiun(nama, latitude, longitude, stationlocation);
                    Log.d("Latitude", String.valueOf(ArrayStasiun[i].getLatitude()));
                    Log.d("Latitude", String.valueOf(ArrayStasiun[i].getLatitude()));
                    i++;
                    Log.d(TAG, "array ke : "+i+" "+nama);
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
                    char x='A';
                    Integer temp=0;


                    String nama = ds.child("Nama").getValue(String.class);
                    long childCount=ds.getChildrenCount();

                    ArrayTrain[mark]=new Train(nama);

                    Log.d(TAG, "onDataChange: kereta "+ nama);
                    for (int j = 0; j < childCount-1; j++) {
                        String xString = x + "";
                        Log.d(TAG, "onDataChange: xstring"+xString);
                        temp=ds.child("Stasiun"+xString).getValue(Integer.class);
                        Log.d(TAG, "onDataChange: temp"+temp);
                        ArrayTrain[mark].addStasiun(ArrayStasiun[temp-1]);

                        Log.d(TAG, "Stasiun : "+ ArrayStasiun[temp-1].getNama()+" ditambahkan ke "+ArrayTrain[mark].getNama()+ " "+ mark);
                        Log.d(TAG, "onDataChange: "+ds.child("Stasiun"+xString).getValue(Integer.class));
                        x++;

                    }
                    mark++;

                }
                addItemKereta();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //addItemKereta();

    }


    public void addItemKereta() {

        String[] namaKereta=new String[ArrayTrain.length];

        for(int i=0; i<namaKereta.length; i++){
            namaKereta[i]=ArrayTrain[i].getNama();
            Log.d("Train"+i, ArrayTrain[i].getNama()+"");
        }

        ArrayAdapter<String> trainList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, namaKereta);
        this.selectTrain.setAdapter(trainList);

    }
}
