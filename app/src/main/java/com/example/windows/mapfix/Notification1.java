package com.example.windows.mapfix;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Asus on 27/04/2018.
 */

public class Notification1 extends AppCompatActivity {
    private Vibrator vibrator;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void bringNotif(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // prepare intent which is triggered if the
// notification is selected

        Intent intent = new Intent(this, Notification1.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

// build notification
// the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("New mail from " + "test@gmail.com")
                .setContentText("Subject")
                .setSmallIcon(R.drawable.icon1)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.icon1, "Call", pIntent)
                .addAction(R.drawable.icon1, "More", pIntent)
                .addAction(R.drawable.icon1, "And more", pIntent).build();


        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
    }
}
