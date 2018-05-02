package com.example.windows.mapfix;

/**
 * Created by rifqi on 2/13/2018.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import java.util.Formatter;
import java.util.Locale;
import java.util.Random;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.content.ContentValues.TAG;


public class TrackService extends Service
{
    static final String ACTION_LOCATION_BROADCAST = TrackService.class.getName() + "TrackService";
    Fragment1 frag = new Fragment1();
    private int iterator = 0;
    private int iteratorJarak = 0;
    private int iteratorTujuan = 0;
    static double speed;
    static String speedtxt;
    public ScreenSlideActivity act = new ScreenSlideActivity();
    public int distance = 500;
    private static final String TAG = "Location_Update_Service";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 5000;
    private static final float LOCATION_DISTANCE = 50f;
    GoogleMap Gmap;
    private Notification1 notif = new Notification1();
    private static Vibrator vibrator;
    private static Location location1;
    private static Location location2;
    double speedzero = 0.0;


    public class LocalBinder extends Binder {
        public TrackService getService() {
            return TrackService.this;
        }
    }

    private IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: service bound");
        return binder;
    }



    private class LocationListener implements android.location.LocationListener {
        Location LastLocation;


        public LocationListener(String provider,GoogleMap googleMap)
        {
            Log.e(TAG, "LocationListener " + provider);
            LastLocation = new Location(provider);
            Gmap=googleMap;
          // NotificationBroadcastReceiver br=new NotificationBroadcastReceiver();
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onLocationChanged(Location location)
        {
            double speedholder  = 25.0;
            int it = 1;
            Log.e(TAG, "onLocationChanged: " + location);

            LastLocation.set(location);
            //Toast.makeText(getApplicationContext(),"location changed "+LastLocation.getLatitude(), Toast.LENGTH_SHORT).show();
            //Gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude()), 15f));
            //Log.d(TAG, "onLocationChanged: camera following");
            speed = (location.getSpeed() * 3600)/1000;

            double distanceTraveled = speed * LOCATION_INTERVAL;

            //Toast.makeText(getApplicationContext(), "current speed " + speedtxt + " km/jam", Toast.LENGTH_SHORT).show();

            for(int i = 0;i<Fragment1.next_stop.size();i++){
                Fragment1.next_stop.get(i).setJarak(distanceTraveled);
                Fragment1.next_stop.get(i).setEta(LOCATION_INTERVAL/1000);
            }



            if(speed > 0.0){
                speedtxt = String.format("%.0f", speed);
                Fragment1.totaldistance-=distanceTraveled;
            }
            else{
                speedtxt = String.format("%.0f", speedzero);
                Fragment1.totaldistance -= LOCATION_DISTANCE;
            }
            //distance-=100;
            //Log.d("distance", "distance " + distance);
            if(Fragment1.next_stop.size()!=0){
                int n = Fragment1.next_stop.size();
                location1 = new Location("provider");
                location1.setLatitude(Fragment1.next_stop.get(n-1).getStasiun().getLatitude());
                location1.setLongitude(Fragment1.next_stop.get(n-1).getStasiun().getLongitude());
                double distance = location.distanceTo(location1);

                location2 = new Location("provider");
                location2.setLatitude(Fragment1.next_stop.get(iterator).getStasiun().getLatitude());
                location2.setLongitude(Fragment1.next_stop.get(iterator).getStasiun().getLongitude());

                if(distance<2000 && iteratorJarak == 0) {
                    notif();
                    iteratorJarak++;
                }
                if(distance <= 50 && iteratorTujuan == 0){
                    notifSampai();
                    iteratorTujuan++;
                }
                if(location.distanceTo(location2) <= 50){
                    Fragment1.next_stop.remove(iterator);
                    iterator++;
                }
            }
            Fragment2.stops = Fragment1.next_stop;

            if(Fragment1.adapter!=null) {
                Fragment1.adapter.notifyDataSetChanged();
            }

        }


        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER,Gmap),
            new LocationListener(LocationManager.NETWORK_PROVIDER,Gmap)
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {

            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "request gagal", ex);
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "request gagal", ex);
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "remove location listener gagal ", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
    public void notifSampai(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_NOTIFICATION_ID, 1);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon1)
                        .setContentTitle("Train Tracker")
                        .setContentText("You have arrived on your destination. Thank You!")
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setVibrate(new long[]{0,5000})
                        .setAutoCancel(true);

        builder.setDeleteIntent(pendingIntent);
        //builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        /**Intent targetIntent = new Intent(this, ScreenSlideActivity.class);
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         */
/*
        Intent ignore = new Intent();
        ignore.putExtra("message", "Abaikan");
        ignore.setAction("IGNORE_MESSAGE_ACTION");
        PendingIntent ignoreIntent  = PendingIntent.getBroadcast(this,123,ignore, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(ignoreIntent);
*/
        /**vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         vibrator.vibrate(6000);
         */
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(1, builder.build());
    }

    public void notif(){
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.putExtra(EXTRA_NOTIFICATION_ID, 0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


                NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon1)
                        .setContentTitle("Train Tracker")
                        .setContentText("You are less than 2 KM way from your destination. get ready!")
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setVibrate(new long[]{0,5000})
                        .setAutoCancel(true);

        builder.setDeleteIntent(pendingIntent);
        //builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        /**Intent targetIntent = new Intent(this, ScreenSlideActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         */
/*
        Intent ignore = new Intent();
        ignore.putExtra("message", "Abaikan");
        ignore.setAction("IGNORE_MESSAGE_ACTION");
        PendingIntent ignoreIntent  = PendingIntent.getBroadcast(this,123,ignore, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(ignoreIntent);
*/
        /**vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(6000);
         */
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(1, builder.build());
    }
    public static class NotificationBroadcastReceiver extends BroadcastReceiver {



        public NotificationBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: getar");
            NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(0);
            manager.cancel(1);

            Intent in = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(in);
        }

    }

}