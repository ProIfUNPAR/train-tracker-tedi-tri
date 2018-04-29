package com.example.windows.mapfix;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.windows.mapfix.java.time.Stops;

import java.util.ArrayList;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * Created by Asus on 11/04/2018.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.CardViewHolder>{

    ArrayList<Stops> stops;

    Adapter(ArrayList<Stops> stops){
        this.stops = stops;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nama;
        TextView jarak;
        TextView eta;


        CardViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.cardview);
            nama = (TextView)itemView.findViewById(R.id.nama);
            jarak = (TextView)itemView.findViewById(R.id.jarak);
            eta = (TextView)itemView.findViewById(R.id.eta);

        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup vg, int i) {
        View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.cardview, vg, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.nama.setText(stops.get(position).getStasiun().getNama());
        holder.jarak.setText(String.format("%.0f",stops.get(position).getJarak())+" KM");
        holder.eta.setText(String.format("%.0f", stops.get(position).getEtaH()) + " H " +String.format("%.0f", stops.get(position).getEtaM()) + " M");
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        Log.d(TAG, "onAttachedToRecyclerView: "+getItemCount());
        super.onAttachedToRecyclerView(recyclerView);
    }



}
