package com.example.windows.mapfix;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.windows.mapfix.java.time.Stops;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.getDefaultSize;
import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class Fragment2 extends Fragment {

    protected static ArrayList<Stops> stops;
    static ListView list;


    public Fragment2() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View RootView = inflater.inflate(R.layout.fragment2, container, false);
        return RootView;

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        list = (ListView) getView().findViewById(R.id.simpleListView);
        //rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        //rv.setLayoutManager(llm);
        initData();
        initAdapter();
    }

        private void initData(){
        Fragment1.next_stop.add(new Stops(new Stasiun("anda belum memulai perjalanan, silahkan pilih melalui menu di samping",0,0,"asd"),0.0,0.0));

        stops=Fragment1.next_stop;
    }

    public void changeCard() {
        Log.d(TAG, "tes: waks");
        //Fragment1.next_stop.clear();
        stops = Fragment1.next_stop;
        Adapter adapter = new Adapter(getContext(), stops);
        list.setAdapter(adapter);

    }


    public void initAdapter(){
        Adapter adapter = new Adapter(getContext(),stops);
        list.setAdapter(adapter);
    }
}