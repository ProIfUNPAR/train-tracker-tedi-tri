package com.example.windows.mapfix;

import android.location.Location;

/**
 * Created by ASUS-A555LB on 2/8/2018.
 */

public class Stasiun {
    private String nama;
    private Location lokasi;

    public Stasiun(double x, double y){
        this.lokasi.setLatitude(x);
        this.lokasi.setLongitude(y);
    }

    public String getNama(){
        return this.nama;
    }

    public double getLatitude(){
        return this.lokasi.getLatitude();
    }

    public double getLongitude(){
        return this.lokasi.getLongitude();
    }

    public void setLocation(double x, double y){
        this.lokasi.setLatitude(x);
        this.lokasi.setLongitude(y);
    }
}
