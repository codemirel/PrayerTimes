package com.olive.prayertimes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

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
            /*Intent first_time_act = new Intent(this, ManuelKurulum.class);
            startActivity(first_time_act);
            finish();*/

        } else{
            Intent mainAct = new Intent(this, MainActivity.class);
            startActivity(mainAct);
            finish();
        }
    }





}
