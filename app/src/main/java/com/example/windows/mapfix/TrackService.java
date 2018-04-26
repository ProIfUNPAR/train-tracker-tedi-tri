package com.example.windows.mapfix;

/**
 * Created by rifqi on 2/13/2018.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import java.util.Formatter;
import java.util.Locale;

public class TrackService extends Service
{
    public static int distance = 500;
    private static final String TAG = "Location_Update_Service";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 5000;
    private static final float LOCATION_DISTANCE = 1f;
    GoogleMap Gmap;
    private Vibrator vibrator;

    private class LocationListener implements android.location.LocationListener
    {
        Location LastLocation;


        public LocationListener(String provider,GoogleMap googleMap)
        {
            Log.e(TAG, "LocationListener " + provider);
            LastLocation = new Location(provider);
            Gmap=googleMap;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);

            LastLocation.set(location);
            Toast.makeText(getApplicationContext(),"location changed "+LastLocation.getLatitude(), Toast.LENGTH_SHORT).show();
            //Gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude()), 15f));
            Log.d(TAG, "onLocationChanged: camera following");
            Fragment1.currentSpeed = location.getSpeed();
            double speed = (Fragment1.currentSpeed * 3600)/1000;
            String speedtxt;
            speedtxt = String.format("%.1f", speed);
            double distanceTraveled = speed * LOCATION_INTERVAL;
            //Fragment1.txtCurrentSpeed.setText(speedtxt);
            Fragment1.changeSpeed(speedtxt);
            Toast.makeText(getApplicationContext(), "current speed " + speedtxt + " km/jam", Toast.LENGTH_SHORT).show();
            for(int i = 0;i<Fragment1.next_stop.size();i++){
                Fragment1.next_stop.get(i).setJarak(distanceTraveled);
                Fragment1.next_stop.get(i).setEta(LOCATION_INTERVAL);
            }
            distance-=100;
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
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

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
}