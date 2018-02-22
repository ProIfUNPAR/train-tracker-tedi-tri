package com.example.windows.mapfix;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG= "MainActivity";
    protected Spinner selectTrain;
    protected TextView textView;

    private static final int ERROR_DIALOG_REQUEST=9001;
    Button btnNotif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.selectTrain = findViewById(R.id.select_train);
        ArrayAdapter<CharSequence> trainselect = ArrayAdapter.createFromResource(this, R.array.train, android.R.layout.simple_spinner_item);
        trainselect.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.selectTrain.setAdapter(trainselect);
        this.textView=findViewById(R.id.textForSelectedTrain);
        AdapterView.OnItemSelectedListener onItemSelectedListener1 =
                new AdapterView.OnItemSelectedListener(){

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        String type = selectTrain.getSelectedItem().toString().trim();
                        textView.setText(type);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}

                };
        this.selectTrain.setOnItemSelectedListener(onItemSelectedListener1);

        if(isServicesok()) {
            init();
        }

    }

    private void init(){
        Button btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        btnNotif = (Button)findViewById(R.id.buttonNotification);

        btnNotif.setOnClickListener(
                new View.OnClickListener() {
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
                        noti.flags=Notification.FLAG_AUTO_CANCEL;
                        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                        nm.notify(0, noti);
                    }
                }
        );
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

}
