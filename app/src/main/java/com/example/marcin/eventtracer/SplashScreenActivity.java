package com.example.marcin.eventtracer;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreenActivity extends AppCompatActivity {

    private TextView tv;
    private ImageView iv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        tv =  (TextView) findViewById(R.id.splashTextView);
        iv = (ImageView) findViewById(R.id.splashLogoImgView);
        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.splashtransition);
        tv.startAnimation(myanim);
        iv.startAnimation(myanim);

        final Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(2500);

                } catch (InterruptedException e) {
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
