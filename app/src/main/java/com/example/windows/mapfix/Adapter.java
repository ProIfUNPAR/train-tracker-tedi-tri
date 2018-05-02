package com.example.windows.mapfix;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.windows.mapfix.java.time.Stops;

import java.util.ArrayList;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * Created by Asus on 11/04/2018.
 */




public class Adapter extends BaseAdapter{
    LayoutInflater Inflter ;
    ArrayList<Stops> stops;
    Context context;
    public Adapter(Context context, ArrayList<Stops> stops) {
        this.stops = stops;
        this.context = context;
        Inflter.from(context);
    }
    @Override
    public int getCount() {
        return stops.size();
    }

    @Override
    public Object getItem(int i) {
        return stops.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater lInflater = (LayoutInflater)context.getSystemService(
                Activity.LAYOUT_INFLATER_SERVICE);
        view = lInflater.inflate(R.layout.cardview, null);

        TextView nama = (TextView)view.findViewById(R.id.nama);
        TextView jarak = (TextView)view.findViewById(R.id.jarak);
        TextView eta = (TextView)view.findViewById(R.id.eta);
        nama.setText(stops.get(i).getStasiun().getNama());
        if(stops.get(i).getJarak()<1000){
            jarak.setText(String.format("%.0f",stops.get(i).getJarak()) + " M");
        }
        else {
            jarak.setText(String.format("%.0f", Math.floor(stops.get(i).getJarak())/1000) + " KM");
        }
        eta.setText(String.format("%.0f",stops.get(i).getEtaH()) + " H " + String.format("%.0f",stops.get(i).getEtaM()) + " M");

        return view;
    }

    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }


}
