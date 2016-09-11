package com.olive.prayertimes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by frkn on 07.09.2016.
 */
public class TimeUpdater {

    public Context context;
    public String country_value, state_value, district_value;
    private Calendar calendar;

    List<Map.Entry<String, String>> params;

    List<String> data;
    Map<String, List<String>> timesOfDays;
    List<String> currentDay;

    public TimeUpdater(Context context) {
        this.context = context;
        params = new ArrayList<>();
        timesOfDays = new HashMap<>();
        data = new ArrayList<>();
        currentDay = new ArrayList<>();
        country_value = state_value = district_value = "-1";
    }

    /**
     * Vakitler okunur
     * timesOfDays Map'i doldurulur
     * currentDay vakitleri alınır
     */
    public void fillTimesOfDays() {
        data = new ArrayList<>(SaveData.readFromFile(context));

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

    public String getStateInfo(){
        String ulkeSehirIlce = "";
        int index;

        if (!data.get(data.size() - 3).equals("" + ":" + "")){
            index = data.get(data.size() - 3).indexOf(':');
            ulkeSehirIlce += data.get(data.size() - 3).substring(0, index) + "/";
            district_value = data.get(data.size() - 3).substring(index+1);
        }
        index = data.get(data.size() - 2).indexOf(':');
        ulkeSehirIlce += data.get(data.size() - 2).substring(0, index) + "/";
        state_value = data.get(data.size() - 2).substring(index+1);

        index = data.get(data.size() - 1).indexOf(':');
        ulkeSehirIlce += data.get(data.size() - 1).substring(0, index);
        country_value = data.get(data.size() - 1).substring(index+1);

        return ulkeSehirIlce;
    }

    public void receiveAndUpdateTimes(){
        params.add(new AbstractMap.SimpleEntry<String, String>("Country", country_value));
        params.add(new AbstractMap.SimpleEntry<String, String>("State", state_value));
        if(!district_value.equals("-1"))
            params.add(new AbstractMap.SimpleEntry<String, String>("City", district_value));
        for(int i=0;i<params.size();i++)
            System.out.println(params.get(i).getKey() + ": " + params.get(i).getValue());
        //new DataReceiver(IlkKurulum.this).execute(params);
    }

    long diff;
    public String remaining, toNotif;

    private String switchCurrentVakit(int ind) {

        switch (ind) {
            case 0:
                return "İmsak'a kalan süre";
            case 1:
                return "Güneş'e kalan süre";
            case 2:
                return "Öğle'ye kalan süre";
            case 3:
                return "İkindi'ye kalan süre";
            case 4:
                return "Akşam'a kalan süre";
            case 5:
                return "Yatsı'ya kalan süre";
            case 6:
                return "İmsak'a kalan süre";
        }
        return "Hatalı";
    }

    public int calcDiffInTime() {
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
                    toNotif = switchCurrentVakit(i) + " >> " + remaining;
                    return i;
                }
            }

            Date date1 = simpleDateFormat.parse(timesOfDays.get(getNextXdaysDate(1)).get(0)); //1 sonraki günün imsak vaktini alıyor
            diff = (date2.getTime() - simpleDateFormat.parse("24:00").getTime()) + (simpleDateFormat.parse("00:00").getTime() - date1.getTime());
            remaining = calculations(diff).toString();
            toNotif = switchCurrentVakit(6) + " >> " + remaining;
            return 6;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
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

    public String getCurrentTime() {
        calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return timeFormat.format(calendar.getTime());

    }

    public String getCurrentDate() {
        calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(calendar.getTime());
    }

    public String getPrevXdaysDate(int x) {
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -x);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String ret = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, x);
        return ret;
    }

    public String getNextXdaysDate(int x) {
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, x);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String ret = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -x);
        return ret;
    }


}
