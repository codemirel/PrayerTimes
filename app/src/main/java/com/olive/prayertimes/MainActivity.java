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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Map<String, TextView[]> vakitler;
    TextView[] arr_tv_map;
    Calendar calendar;
    List<String> data;
    Map<String, List<String>> timesOfDays;
    List<String> currentDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        timesOfDays = new HashMap<>();
        currentDay = new ArrayList<>();

        changeTextFonts();
        calendar = Calendar.getInstance();
        Log.d("onCreate", "Data is reading..");
        data = new ArrayList<>(SaveData.readFromFile(getApplicationContext()));

        List<String> temp = new ArrayList<>();
        for(int i = 0; i < data.size() - 3; i++){
            if((i+9) % 9 == 0){
                for(int j = 1; j <= 7; j++)
                    temp.add(data.get(i+j));
                timesOfDays.put(data.get(i), new ArrayList<String>(temp));
                temp.clear();
            }
        }

        /*for(String s : timesOfDays.keySet()){
            System.out.println("date: " + s);
            for(String t : timesOfDays.get(s))
                System.out.println(t);
        }*/

        currentDay = timesOfDays.get(getCurrentDate());

        for(String s : currentDay)
            System.out.println(s);

        for(int i = 1; i <= 6; i++){
            arr_tv_map[2*(i-1)+1].setText(currentDay.get(i-1));
        }
        arr_tv_map[16].setText(getCurrentDate());
        String ulkeSehirIlce = "";
        if(data.get(data.size() - 3) != null)
            ulkeSehirIlce += data.get(data.size() - 3) + "/";
        ulkeSehirIlce += data.get(data.size() - 2) + "/" + data.get(data.size() - 1);
        arr_tv_map[12].setText(ulkeSehirIlce);
        arr_tv_map[14].setText(currentVakit(calcDiffInTime()));

        System.out.println(calcDiffInTime());

    }

    private String getCurrentTime(){

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(calendar.getTime());

    }

    private String getCurrentDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(calendar.getTime());
    }

    private String getTomorrowDate(){
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String ret = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return ret;
    }

    private int calcDiffInTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date date1, date2;
        try {
            date2 = simpleDateFormat.parse(getCurrentTime());
            for(int i = 0; i <= 5; i++){
                date1 = simpleDateFormat.parse(currentDay.get(i));
                long diff = date2.getTime() - date1.getTime();
                if(diff < 0){
                    int days = (int) (diff / (1000*60*60*24));
                    int hours = (int) ((diff - (1000*60*60*24*days)) / (1000*60*60));
                    int minutes = (int) (diff - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
                    System.out.println(days + ", " + hours + ", " + minutes);
                    arr_tv_map[15].setText(Math.abs(hours) + ":" + Math.abs(minutes));
                    return i+1;
                }
            }

            date1 = simpleDateFormat.parse(timesOfDays.get(getTomorrowDate()).get(0));
            long diff = (date2.getTime() - simpleDateFormat.parse("24:00").getTime()) + (simpleDateFormat.parse("00:00").getTime() - date1.getTime());
            int days = (int) (diff / (1000*60*60*24));
            int hours = (int) ((diff - (1000*60*60*24*days)) / (1000*60*60));
            int minutes = (int) (diff - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
            arr_tv_map[15].setText(Math.abs(hours) + ":" + Math.abs(minutes));
            return 7;


        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;

    }

    private String currentVakit(int v){
        switch (v){
            case 1:
                return "Yatsı";
            case 2:
                return "Sabah";
            case 3:
                return "Güneş";
            case 4:
                return "Öğle";
            case 5:
                return "İkindi";
            case 6:
                return "Akşam";
            case 7:
                return "Yatsı";
            case -1:
                return "Hatalı";
        }
        return "Hatalı";
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
