package com.olive.prayertimes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class Splash extends Activity {

    private static final String PREFS_NAME = "MyPrefsFile";

    protected final static int PERMISSION_REQ = 10;
    String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};


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

            if(!hasPermissions(this, PERMISSIONS)){
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ);
            }


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

    private boolean hasPermissions(Context context, String... permissions){
        // Here, thisActivity is the current activity
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    System.out.println("All permission taked");
                    MyGeocoder geo = new MyGeocoder(this);
                    geo.GeoAddress();
                }
            }
        }
    }
}
