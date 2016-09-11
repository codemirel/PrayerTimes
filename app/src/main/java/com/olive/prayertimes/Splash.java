package com.olive.prayertimes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Splash extends Activity {

    private static final String PREFS_NAME = "MyPrefsFile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d("Girdi", "checkFirstTime basladi");
        checkFirstTime();
        Log.d("Girdi", "checkFirstTime bitti");

    }

    public void checkFirstTime() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);


        if (settings.getBoolean("my_first_time", true)) {

            MyGeocoder geo = new MyGeocoder(this);
            geo.GeoAddress();

            settings.edit().putBoolean("my_first_time", false).commit();
            Intent first_time_act = new Intent(this, IlkKurulum.class);
            startActivity(first_time_act);
            finish();

        } else{
            Intent mainAct = new Intent(this, MainActivity.class);
            startActivity(mainAct);
            finish();
        }
    }





}
