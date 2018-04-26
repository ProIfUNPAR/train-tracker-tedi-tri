package com.example.windows.mapfix;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by ASUS-A555LB on 4/3/2018.
 */

public class SplashScreen extends AppCompatActivity{
    private TextView textView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.transition_splash);
        textView.startAnimation(animation);
        imageView.startAnimation(animation);

        final Intent i = new Intent(this,MainActivity.class);

        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(1700);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
                finally {
                    startActivity(i);
                    finish();
                }
            }
        };
        timer.start();
    }
}