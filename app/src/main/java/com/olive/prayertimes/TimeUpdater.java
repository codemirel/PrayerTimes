package com.olive.prayertimes;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by frkn on 07.09.2016.
 */
public class TimeUpdater {

    private MainActivity activity;
    public String country_value, state_value, district_value;
    private Calendar calendar;

    List<Map.Entry<String, String>> params;

    List<String> data;
    Map<String, List<String>> timesOfDays;
    List<String> currentDay;

    public TimeUpdater(MainActivity activity) {
        this.activity = activity;
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
        data = new ArrayList<>(SaveData.readFromFile(activity.getApplicationContext()));

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
