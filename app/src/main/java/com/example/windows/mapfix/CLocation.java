package com.example.windows.mapfix;

import android.location.Location;

/**
 * Created by Andreas on 2/8/2018.
 */

public class CLocation extends Location{
    private boolean bUseMetricUnits = false;

    public CLocation(Location location){
        this(location, true);
    }

    public CLocation(Location location, boolean bUseMetricUnits) {
    super(location);
    this.bUseMetricUnits=bUseMetricUnits;
    }

    public boolean getUseMetricUnits()
    {
        return this.bUseMetricUnits;
    }
    public void setUseMetricunits(boolean bUseMetricUnits)
    {
        this.bUseMetricUnits=bUseMetricUnits;
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);
        if(!this.getUseMetricUnits()){
            //convert metric to feet
            nDistance = nDistance * 3.28083989501312f;
        }
        return nDistance;
    }

    @Override
    public float getAccuracy() {
        float nAccuracy =super.getAccuracy();
        if(!this.getUseMetricUnits()){
            //convert metric to feet
            nAccuracy = nAccuracy * 3.28083989501312f;
        }
        return nAccuracy;
    }

    @Override
    public double getAltitude() {
        double nAltitude= super.getAltitude();
        if(!this.getUseMetricUnits()){
            //convert meters to feet
            nAltitude = nAltitude*3.28083989501312d;
        }
        return nAltitude;
    }

    @Override
    public float getSpeed() {
        float nSpeed = super.getSpeed();
        if(!this.getUseMetricUnits()){
            //convert meters/second to km/hour
            nSpeed =nSpeed*3.6f;

        }
        return nSpeed;
    }
}
