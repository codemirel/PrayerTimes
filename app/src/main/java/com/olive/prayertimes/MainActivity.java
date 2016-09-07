package com.olive.prayertimes;

import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Map<String, TextView[]> vakitler;
    TextView[] arr_tv_map;
    static Calendar calendar;
    List<String> data;
    Map<String, List<String>> timesOfDays;
    List<String> currentDay;
    LinearLayout[] arr_layout;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        changeTextFonts();

        timesOfDays = new HashMap<>();
        currentDay = new ArrayList<>();
        calendar = Calendar.getInstance();

        arr_layout = new LinearLayout[6];
        arr_layout[0] = (LinearLayout) findViewById(R.id.sabahLay);
        arr_layout[1] = (LinearLayout) findViewById(R.id.gunesLay);
        arr_layout[2] = (LinearLayout) findViewById(R.id.ogleLay);
        arr_layout[3] = (LinearLayout) findViewById(R.id.ikindiLay);
        arr_layout[4] = (LinearLayout) findViewById(R.id.aksamLay);
        arr_layout[5] = (LinearLayout) findViewById(R.id.yatsiLay);

        fillTimesOfDays();
        updateTimes();
        updateState();
        checkTimer();
    }

    /**
     * Vakitler okunur
     * timesOfDays Map'i doldurulur
     * currentDay tarihleri alınır
     */
    private void fillTimesOfDays() {
        data = new ArrayList<>(SaveData.readFromFile(getApplicationContext()));

        List<String> temp = new ArrayList<>();
        for (int i = 0; i < data.size() - 3; i++) {
            if ((i + 9) % 9 == 0) {
                for (int j = 1; j <= 7; j++)
                    temp.add(data.get(i + j));
                timesOfDays.put(data.get(i), new ArrayList<String>(temp));
                temp.clear();
            }
        }

        currentDay = timesOfDays.get(getCurrentDate());
    }

    // Vakitler update edilir
    private void updateTimes() {
        for (int i = 1; i <= 6; i++) {
            arr_tv_map[2 * (i - 1) + 1].setText(currentDay.get(i - 1));
        }
        arr_tv_map[16].setText(getCurrentDate());
    }

    // State/Country update edilir
    private void updateState() {
        String ulkeSehirIlce = "";
        if (data.get(data.size() - 3) != null)
            ulkeSehirIlce += data.get(data.size() - 3) + "/";
        ulkeSehirIlce += data.get(data.size() - 2) + "/" + data.get(data.size() - 1);
        arr_tv_map[12].setText(ulkeSehirIlce);
    }

    private String getCurrentTime() {
        calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return timeFormat.format(calendar.getTime());

    }

    public static String getCurrentDate() {
        calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(calendar.getTime());
    }

    private String getPrevXdaysDate(int x) {
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -x);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String ret = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, x);
        return ret;
    }

    private String getNextXdaysDate(int x) {
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, x);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String ret = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -x);
        return ret;
    }

    public static String suankiVakit = "";

    // Su anki namaz vaktinin layer ini boyayan method
    private void paintLayer(int ind) {
        arr_layout[ind].setBackgroundResource(R.color.ayrim);

        for (int i = 0; i < 6; i++) {
            if (i != ind) {
                arr_layout[i].setBackgroundResource(0);
            }
        }

        // En ustteki TextView in guncellenmesi
        switch (ind) {
            case 0:
                suankiVakit = "İmsak'a kalan süre:";
                break;
            case 1:
                suankiVakit = "Güneş'e kalan süre:";
                break;
            case 2:
                suankiVakit = "Öğle'ye kalan süre:";
                break;
            case 3:
                suankiVakit = "İkindi'ye kalan süre:";
                break;
            case 4:
                suankiVakit = "Akşam'a kalan süre:";
                break;
            case 5:
                suankiVakit = "Yatsı'ya kalan süre:";
                break;
        }
        arr_tv_map[14].setText(suankiVakit);
    }

    Intent mServiceIntent;
    long diff;
    String remaining;

    private int calcDiffInTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf_forCurrent = new SimpleDateFormat("HH:mm:ss");
        Calendar c = Calendar.getInstance();
        String formattedDate = sdf_forCurrent.format(c.getTime());
        try {
            Date date2 = sdf_forCurrent.parse(formattedDate);
            //currentDay = timesOfDays.get(getCurrentDate());
            for (int i = 0; i <= 5; i++) {
                Date date1 = simpleDateFormat.parse(currentDay.get(i));
                diff = date2.getTime() - date1.getTime();
                if (diff < 0) {
                    remaining = calculations(diff).toString();
                    arr_tv_map[15].setText(remaining);
                    paintLayer(i);
                    makeBold(i * 2, i * 2 + 1);
                    return i;
                }
            }

            Date date1 = simpleDateFormat.parse(timesOfDays.get(getNextXdaysDate(1)).get(0)); //1 sonraki günün imsak vaktini alıyor
            diff = (date2.getTime() - simpleDateFormat.parse("24:00").getTime()) + (simpleDateFormat.parse("00:00").getTime() - date1.getTime());
            remaining = calculations(diff).toString();
            arr_tv_map[15].setText(remaining);
            paintLayer(5);
            makeBold(10, 11);
            suankiVakit = "İmsak'a Kalan Vakit:";
            return 5;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void send_Notification() {
        if (remaining != null) {
            mServiceIntent = new Intent(this, BackgroundServices.class);
            mServiceIntent.setData(Uri.parse(suankiVakit + " " + remaining.substring(0, 2) + " saat " + remaining.substring(3, 5) + " dakika"));
            startService(mServiceIntent);
        }
    }

    private String calculations(long diff) {
        int diffSeconds = (int) diff / 1000 % 60;
        int diffMinutes = (int) diff / (60 * 1000) % 60;
        int diffHours = (int) diff / (60 * 60 * 1000);
        int hh = Math.abs(diffHours);
        int mm = Math.abs(diffMinutes);
        int ss = Math.abs(diffSeconds);

        String hhS = String.valueOf(hh);
        String mmS = String.valueOf(mm);
        String ssS = String.valueOf(ss);

        if (hh < 10)
            hhS = "0" + hhS;
        if (mm < 10)
            mmS = "0" + mmS;
        if (ss < 10)
            ssS = "0" + ssS;

        String dateString = hhS + ":" + mmS + ":" + ssS;
        return dateString;
    }

    private void fillMap() {
        vakitler.put("sabah", new TextView[]{arr_tv_map[0], arr_tv_map[1]});
        vakitler.put("gunes", new TextView[]{arr_tv_map[2], arr_tv_map[3]});
        vakitler.put("ogle", new TextView[]{arr_tv_map[4], arr_tv_map[5]});
        vakitler.put("ikindi", new TextView[]{arr_tv_map[6], arr_tv_map[7]});
        vakitler.put("aksam", new TextView[]{arr_tv_map[8], arr_tv_map[9]});
        vakitler.put("yatsi", new TextView[]{arr_tv_map[10], arr_tv_map[11]});
    }

    private void checkTimer() {
        handler = new Handler();
        handler.postDelayed(runnable, 0);
    }

    int timeCount = 0;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */

            if (getCurrentTime().equals("00:00:00")) {
                fillTimesOfDays();
                updateTimes();
            }
            calcDiffInTime();
            if (timeCount % 30 == 0) {
                timeCount = 0;
                send_Notification();
            }
            timeCount++;

      /* and here comes the "trick" */
            handler.postDelayed(this, 1000);
        }
    };

    // Su anki namaz vaktinin yazi tipi bold yapan method
    private void makeBold(int id1, int id2) {
        Typeface tf_for_vakitler = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Light.ttf");
        Typeface tf_for_sehirandtarih = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Medium.ttf");

        arr_tv_map[id1].setTypeface(tf_for_sehirandtarih);
        arr_tv_map[id2].setTypeface(tf_for_sehirandtarih);

        for (int i = 0; i < 12; i++) {
            if (i != id1 && i != id2) {
                arr_tv_map[i].setTypeface(tf_for_vakitler);
            }
        }
    }

    private void changeTextFonts() {
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
                (TextView) findViewById(R.id.nextVakit),
                (TextView) findViewById(R.id.tarihText)};
        for (int i = 0; i < 12; i++) {
            arr_tv[i].setTypeface(tf_for_vakitler);
        }
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
