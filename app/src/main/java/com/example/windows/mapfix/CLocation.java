package com.example.windows.mapfix;

import android.location.Location;

/**
 * Created by Andreas on 2/8/2018.
 */

public class CLocation extends Location{
    private boolean bUseMetricUnits = false;
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
            //convert meters/second to miles/hour
            nSpeed =nSpeed*2.2369362920544f;

        }
        return nSpeed;
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

    public CLocation(Location location){
        this(location, true);
    }

    public CLocation(Location location, boolean bUseMetricUnits) {
    super(location);
    this.bUseMetricUnits=bUseMetricUnits;
    }

    public boolean getUseMetricUnits(){
        return this.bUseMetricUnits;
    }
    public void setUseMetricUnits(boolean b){
        this.bUseMetricUnits=bUseMetricUnits;
    }

}
