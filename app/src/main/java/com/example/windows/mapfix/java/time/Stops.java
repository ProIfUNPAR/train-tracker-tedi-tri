package com.example.windows.mapfix.java.time;

import com.example.windows.mapfix.Stasiun;

/**
 * Created by Asus on 19/03/2018.
 */

public class Stops {
    private Stasiun stasion;
    private double eta;
    private double jarak;
    private double etaH;
    private double etaM;

    public Stops(Stasiun stas, double jarak, double eta) {
        this.stasion = stas;
        this.jarak = jarak;
        this.eta = eta;
        this.etaH = this.eta/3600;
        this.etaM = (this.etaH%3600)/60;
    }
    public Stasiun getStasiun(){
        return this.stasion;
    }
    public double getEta(){
        return this.eta;
    }
    public double getJarak(){
        return this.jarak;
    }
    public double getEtaH(){
        return this.etaH;
    }
    public double getEtaM(){
        return this.etaM;
    }
}
