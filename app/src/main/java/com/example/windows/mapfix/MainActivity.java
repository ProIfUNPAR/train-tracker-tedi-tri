package com.example.windows.mapfix;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG= "MainActivity";

    private static final int ERROR_DIALOG_REQUEST=9001;
    Button btnNotif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                                .setContentText("Stasiun Berikutnya : ")
                                .setSmallIcon(R.drawable.icon1)
                                .setContentIntent(pIntent).getNotification();

                        noti.flags=Notification.FLAG_AUTO_CANCEL;
                        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                        nm.notify(0, noti);
                    }
                }
        );


        if(isServicesok()){


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
