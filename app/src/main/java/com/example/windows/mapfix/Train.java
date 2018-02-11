package com.example.windows.mapfix;

import android.location.Location;

import java.util.LinkedList;

/**
 * Created by Windows on 11/02/2018.
 */

public class Train {

    private String nama;
    public LinkedList stasiun = new LinkedList<Stasiun>();


    public Train(String nama){

        this.nama=nama;


    }
    public String getNama(){
        return this.nama;
    }


    public LinkedList getStasiun(){
        return this.stasiun;
    }

    public void addStasiun(Stasiun stasiun){
        this.stasiun.add(stasiun);
    }

    public Stasiun getStop(int index){
        Stasiun stop=Stasiun.class.cast(this.stasiun.get(index));
        return stop;
    }

    }

