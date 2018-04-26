package com.example.windows.mapfix;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.location.GeofencingClient;

/**
 * Created by Asus on 25/04/2018.
 */

public class GeofenceTransitionsIntentService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
