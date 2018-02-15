package com.example.windows.mapfix;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by no one on 2/8/2018.
 */

public interface IBaseGpsListener extends LocationListener, GpsStatus.Listener{

    public void
    onLocationChanged(Location location);

    public void onProviderDisabled(String provider);

    public void onProviderEnabled(String provider);

    public void onStatusChanged(String provider, int status, Bundle extras);

    public void onGpsStatusChanged(int event);

}
