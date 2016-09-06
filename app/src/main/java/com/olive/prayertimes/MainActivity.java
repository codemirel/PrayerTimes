package com.olive.prayertimes;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Map<String, TextView[]> vakitler;
    TextView[] arr_tv_map;
    Calendar currentMoment;
    List<String> times;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        changeTextFonts();
        currentMoment = Calendar.getInstance();
        Log.d("onCreate", "Data is reading..");
        times = new ArrayList<>(SaveData.readFromFile(getApplicationContext()));
        for(int i=1;i<=6;i++){
            arr_tv_map[2*(i-1)+1].setText(times.get(i));
        }
        arr_tv_map[16].setText(getCurrentDate());
        String ulkeSehirIlce = "";
        if(times.get(times.size() - 3) != null)
            ulkeSehirIlce += times.get(times.size() - 3) + "/";
        ulkeSehirIlce += times.get(times.size() - 2) + "/" + times.get(times.size() - 1);
        arr_tv_map[12].setText(ulkeSehirIlce);
        calcDiffInTime();

    }

    private String getCurrentTime(){

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(currentMoment.getTime());

    }

    private String getCurrentDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(currentMoment.getTime());
    }

    private int calcDiffInTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            Date date2 = simpleDateFormat.parse(getCurrentTime());
            for(int i = 1; i <= 6; i++){
                Date date1 = simpleDateFormat.parse(times.get(i));
                long diff = date2.getTime() - date1.getTime();
                if(diff < 0){
                    int days = (int) (diff / (1000*60*60*24));
                    int hours = (int) ((diff - (1000*60*60*24*days)) / (1000*60*60));
                    int minutes = (int) (diff - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
                    arr_tv_map[15].setText(Math.abs(hours) + ":" + Math.abs(minutes));
                    return i;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;

    }


    public void fillMap() {
        vakitler.put("sabah", new TextView[]{arr_tv_map[0], arr_tv_map[1]});
        vakitler.put("gunes", new TextView[]{arr_tv_map[2], arr_tv_map[3]});
        vakitler.put("ogle", new TextView[]{arr_tv_map[4], arr_tv_map[5]});
        vakitler.put("ikindi", new TextView[]{arr_tv_map[6], arr_tv_map[7]});
        vakitler.put("aksam", new TextView[]{arr_tv_map[8], arr_tv_map[9]});
        vakitler.put("yatsi", new TextView[]{arr_tv_map[10], arr_tv_map[11]});
    }

    public void changeTextFonts() {
        Typeface tf_for_vakitler = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Light.ttf");
        Typeface tf_for_sehirandtarih = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Medium.ttf");
        Typeface tf_for_kalanvakit = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Black.ttf");
        Typeface tf_for_currentVakandSure = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Medium.ttf");
        TextView[] arr_tv = {
                (TextView) findViewById(R.id.sabahSol),
                (TextView) findViewById(R.id.sabahSag),
                (TextView) findViewById(R.id.gunesSol),
                (TextView) findViewById(R.id.gunesSag),
                (TextView) findViewById(R.id.ogleSol),
                (TextView) findViewById(R.id.ogleSag),
                (TextView) findViewById(R.id.ikindiSol),
                (TextView) findViewById(R.id.ikindiSag),
                (TextView) findViewById(R.id.aksamSol),
                (TextView) findViewById(R.id.aksamSag),
                (TextView) findViewById(R.id.yatsiSol),
                (TextView) findViewById(R.id.yatsiSag),
                (TextView) findViewById(R.id.sehirText),
                (TextView) findViewById(R.id.nextVakit),
                (TextView) findViewById(R.id.currentVakit),
                (TextView) findViewById(R.id.kalanVakit),
                (TextView) findViewById(R.id.tarihText)};
        for (int i = 0; i < 12; i++) {
            arr_tv[i].setTypeface(tf_for_vakitler);
        }
        arr_tv[2].setTypeface(tf_for_sehirandtarih);
        arr_tv[3].setTypeface(tf_for_sehirandtarih);
        arr_tv[12].setTypeface(tf_for_sehirandtarih);
        arr_tv[13].setTypeface(tf_for_kalanvakit);
        arr_tv[14].setTypeface(tf_for_currentVakandSure);
        arr_tv[15].setTypeface(tf_for_currentVakandSure);
        arr_tv[16].setTypeface(tf_for_sehirandtarih);

        arr_tv_map = arr_tv;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void aboutClick(MenuItem item) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(getString(R.string.aboutString));
        dlgAlert.setTitle("About Olive");
        dlgAlert.setPositiveButton("CONTACT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendEmail();
            }
        });
        dlgAlert.setNegativeButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public void sendEmail() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"olivegamestr@gmail.com"});
        try {
            startActivity(Intent.createChooser(i, "Send e-mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
