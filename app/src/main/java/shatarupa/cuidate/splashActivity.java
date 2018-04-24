package shatarupa.cuidate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class splashActivity extends AppCompatActivity {

    private PrefManager prefManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context splashScreenContext=getApplicationContext();
        prefManager = new PrefManager(this);
        int delay=4000;
        setContentView(R.layout.activity_splash);
        if (!prefManager.isFirstTimeLaunch()) {
                delay=1000;
        }
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
            public void run() {
                Intent mainIntent = new Intent(splashScreenContext, WelcomeActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, delay);

    }
}