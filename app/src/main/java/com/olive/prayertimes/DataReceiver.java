package com.olive.prayertimes;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by frkn on 07.09.2016.
 */
public class DataReceiver {

    private ManuelKurulum activity;
    private Context context;
    private ProgressBar progressBar;
    private TextView progressText;

    private static final String url = "http://www.diyanet.gov.tr/tr/PrayerTime/PrayerTimesList";

    public JSONObject parameters;
    public JSONObject countries_list;
    public JSONObject states_list;
    public JSONObject districts_list;

    public DataReceiver(ManuelKurulum activity) {
        this.activity = activity;
        progressBar = (ProgressBar) activity.findViewById(R.id.progressBar);
        progressText = (TextView) activity.findViewById(R.id.progressText);
        parameters = new JSONObject();
        countries_list = new JSONObject();
        states_list = new JSONObject();
        districts_list = new JSONObject();
    }

    boolean secilen = false;

    public DataReceiver(Context context) {
        this.context = context;
        parameters = new JSONObject();
        countries_list = new JSONObject();
        states_list = new JSONObject();
        districts_list = new JSONObject();
    }

    public void runForManuel() {
        new postForManuel().execute(parameters);
    }

    public void runForGps() {
        new postForGps().execute(parameters);
    }

    public void runForBackground() {
        new postForBackGround().execute(parameters);
    }

    public void addToParams(String region, String id) {
        try {
            parameters.put(region, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCountryNameById(String id) {
        for (Iterator<String> iterator = countries_list.keys(); iterator.hasNext(); ) {
            try {
                String key = (String) iterator.next();
                if (id.equals(countries_list.getString(key))) {
                    return key;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String getStateNameById(String id) {
        for (Iterator<String> iterator = states_list.keys(); iterator.hasNext(); ) {
            try {
                String key = (String) iterator.next();
                if (id.equals(states_list.getString(key))) {
                    return key;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String getDistrictNameById(String id) {
        if (id != null) {
            for (Iterator<String> iterator = districts_list.keys(); iterator.hasNext(); ) {
                try {
                    String key = (String) iterator.next();
                    if (id.equals(districts_list.getString(key))) {
                        return key;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    public String getCountryIdByName(String name) {
        if (countries_list.has(name)) {
            try {
                return countries_list.getString(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public String getStateIdByName(String name) {
        if (states_list.has(name)) {
            try {
                return states_list.getString(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public String getDistrictIdByName(String name) {
        if (districts_list.has(name)) {
            try {
                return districts_list.getString(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    private JSONObject getCountries() {
        Log.d("ASYNC", "getCuontries() starting..");
        org.jsoup.nodes.Document doc = null;
        JSONObject ret = new JSONObject();
        try {

            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(10 * 1000)
                    .post();

            for (Element opt : doc.select("span").get(12).child(0).children()) {
                ret.put(opt.text(), opt.attr("value").toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private JSONObject getStates(String country_value) {
        Log.d("ASYNC", "getStates() starting..");
        org.jsoup.nodes.Document doc = null;
        JSONObject ret = new JSONObject();
        try {

            doc = Jsoup.connect(url)
                    .data("Country", country_value)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(10 * 1000)
                    .post();

            for (Element opt : doc.select("span").get(14).child(0).children()) {
                if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                    continue;
                ret.put(opt.text(), opt.attr("value").toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private JSONObject getDictricts(String country_value, String state_value) {
        Log.d("ASYNC", "getDistricts() starting..");
        org.jsoup.nodes.Document doc = null;
        JSONObject ret = new JSONObject();
        try {

            doc = Jsoup.connect(url)
                    .data("Country", country_value)
                    .data("State", state_value)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(10 * 1000)
                    .post();

            for (Element opt : doc.select("span").get(16).child(0).children()) {
                if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                    continue;
                ret.put(opt.text(), opt.attr("value").toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private void getPrayerTimes(String country_value, String state_value, String district_value) {
        Log.d("ASYNC", "getPrayerTimes() starting..");
        String period_value = "Aylik";
        org.jsoup.nodes.Document doc = null;
        List<List<String>> listOfList = new ArrayList<>();
        try {

            if (district_value.equals("") || district_value == null || district_value.equals("-1")) {
                doc = Jsoup.connect(url)
                        .data("Country", country_value)
                        .data("State", state_value)
                        .data("period", period_value)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                        .referrer("http://www.google.com")
                        .timeout(10 * 1000)
                        .post();

                SaveData.writeToFile(activity.getApplicationContext(), "null" + ":" + "-1" + "\n");

            } else {
                doc = Jsoup.connect(url)
                        .data("Country", country_value)
                        .data("State", state_value)
                        .data("City", district_value)
                        .data("period", period_value)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                        .referrer("http://www.google.com")
                        .timeout(10 * 1000)
                        .post();

                SaveData.writeToFile(activity.getApplicationContext(), getDistrictNameById(district_value) + ":" + district_value + "\n");
            }

            SaveData.writeToFile(activity.getApplicationContext(), getStateNameById(state_value) + ":" + state_value + "\n");
            SaveData.writeToFile(activity.getApplicationContext(), getCountryNameById(country_value) + ":" + country_value + "\n");

            List<String> gun;
            for (Element tr : doc.select("tbody").first().children()) {
                gun = new ArrayList<>();
                for (Element td : tr.select("td")) {
                    if (td.text() != "")
                        gun.add(td.text());
                }
                listOfList.add(gun);
            }


            for (List<String> l : listOfList) {
                if (l.size() != 0) {
                    System.out.println(l.get(0) + " added to data");
                    for (String s : l) {
                        SaveData.writeToFile(activity.getApplicationContext(), s + "\n");
                    }
                    SaveData.writeToFile(activity.getApplicationContext(), "----------------------\n");
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class postForManuel extends AsyncTask<JSONObject, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("Info loading..");
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            if (params[0].length() == 0) {
                Log.d("postForManuel", "call for getCountries");
                return getCountries();
            } else if (params[0].length() == 1) {
                Log.d("postForManuel", "call for getStates");
                try {
                    return getStates(params[0].getString("Country"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (params[0].length() == 2) {
                if (activity.secilenUlke) {
                    Log.d("postForManuel", "call for getDistricts");
                    try {
                        return getDictricts(params[0].getString("Country"), params[0].getString("State"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("postForManuel", "call for getPrayerTimes");
                    try {
                        getPrayerTimes(params[0].getString("Country"), params[0].getString("State"), "");
                        return null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (params[0].length() == 3) {
                Log.d("postForManuel", "call for getPrayerTimes");
                try {
                    getPrayerTimes(params[0].getString("Country"), params[0].getString("State"), params[0].getString("District"));
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json_list) {
            super.onPostExecute(json_list);
            progressBar.setVisibility(View.INVISIBLE);
            progressText.setVisibility(View.INVISIBLE);
            if (parameters.length() == 0) {
                Log.d("postForManuel", "Countries are listing..");
                countries_list = json_list;
                activity.fillListView(activity.json2List(json_list));
            } else if (parameters.length() == 1) {
                Log.d("postForManuel", "States are listing..");
                states_list = json_list;
                activity.fillListView(activity.json2List(json_list));
            } else if (parameters.length() == 2) {
                if (activity.secilenUlke) {
                    Log.d("postForManuel", "Districts are listing..");
                    districts_list = json_list;
                    activity.fillListView(activity.json2List(json_list));
                } else {
                    Log.d("postForManuel", "Times received..");
                    activity.startMainAct();
                }
            } else if (parameters.length() == 3) {
                Log.d("postForManuel", "Times received..");
                activity.startMainAct();
            }

        }

    }

    public class postForGps extends AsyncTask<JSONObject, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            Log.d("postForGps", "onPreExecute");
            progressBar.setVisibility(View.VISIBLE);
            /*progressText.setText("Info loading..");
            progressText.setVisibility(View.VISIBLE);*/
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            if (params[0].length() == 0) {
                Log.d("postForGps", "call for getCountries");
                return getCountries();
            } else if (params[0].length() == 1) {
                Log.d("postForGps", "call for getStates");
                try {
                    return getStates(params[0].getString("Country"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (params[0].length() == 2) {
                if (secilen) {
                    Log.d("postForGps", "call for getDistricts");
                    try {
                        return getDictricts(params[0].getString("Country"), params[0].getString("State"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("postForGps", "call for getPrayerTimes");
                    try {
                        getPrayerTimes(params[0].getString("Country"), params[0].getString("State"), "");
                        return null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (params[0].length() == 3) {
                Log.d("postForGps", "call for getPrayerTimes");
                try {
                    getPrayerTimes(params[0].getString("Country"), params[0].getString("State"), params[0].getString("District"));
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json_list) {
            super.onPostExecute(json_list);
            progressBar.setVisibility(View.INVISIBLE);
            //progressText.setVisibility(View.INVISIBLE);
            if (parameters.length() == 0) {
                Log.d("postForGps", "Countries are listing..");
                countries_list = json_list;
                try {
                    String id = getCountryIdByName(parameters.getString("Country"));
                    if (id.equals("2") || id.equals("33") || id.equals("52"))
                        secilen = true;
                    parameters.put("Country", id);
                    new postForGps().execute(parameters);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (parameters.length() == 1) {
                Log.d("postForGps", "States are listing..");
                states_list = json_list;
                try {
                    String id = getStateIdByName(parameters.getString("State"));
                    parameters.put("State", id);
                    new postForGps().execute(parameters);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (parameters.length() == 2) {
                if (secilen) {
                    Log.d("postForGps", "Districts are listing..");
                    districts_list = json_list;
                    try {
                        String id = getDistrictIdByName(parameters.getString("District"));
                        if (id.equals("")) {
                            id = getDistrictIdByName(parameters.getString("State"));
                        }
                        parameters.put("District", id);
                        new postForGps().execute(parameters);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("postForGps", "Times received..");
                    startMainAct();
                }
            } else if (parameters.length() == 3) {
                Log.d("postForGps", "Times received..");
                startMainAct();
            }

        }

    }

    public class postForBackGround extends AsyncTask<JSONObject, Void, JSONObject> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            Log.d("postForBackGround", "call for getPrayerTimes");
            try {
                this.getPrayerTimes(params[0].getString("Country"), params[0].getString("State"), params[0].getString("District"));
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json_list) {
            super.onPostExecute(json_list);
            Log.d("postForBackGround", "Times received..");
        }

        private void getPrayerTimes(String country_value, String state_value, String district_value) {
            Log.d("ASYNC", "getPrayerTimes() starting..");
            String period_value = "Aylik";
            org.jsoup.nodes.Document doc = null;
            List<List<String>> listOfList = new ArrayList<>();
            try {

                if (district_value.equals("") || district_value == null || district_value.equals("-1")) {
                    doc = Jsoup.connect(url)
                            .data("Country", country_value)
                            .data("State", state_value)
                            .data("period", period_value)
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                            .referrer("http://www.google.com")
                            .timeout(10 * 1000)
                            .post();
                } else {
                    doc = Jsoup.connect(url)
                            .data("Country", country_value)
                            .data("State", state_value)
                            .data("City", district_value)
                            .data("period", period_value)
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                            .referrer("http://www.google.com")
                            .timeout(10 * 1000)
                            .post();
                }

                List<String> gun;
                for (Element tr : doc.select("tbody").first().children()) {
                    gun = new ArrayList<>();
                    for (Element td : tr.select("td")) {
                        if (td.text() != "")
                            gun.add(td.text());
                    }
                    listOfList.add(gun);
                }

                checkForUpdate(listOfList);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void checkForUpdate(List<List<String>> listOfList){
            List<String> data = new ArrayList<>(SaveData.readFromFile(context));
            Map<String, List<String>> timesOfDays = new HashMap<>();

            List<String> temp = new ArrayList<>();
            for (int i = 3; i < data.size(); i++) {
                if (((i-3) + 9) % 9 == 0) {
                    for (int j = 1; j <= 7; j++)
                        temp.add(data.get(i + j));
                    timesOfDays.put(data.get(i), new ArrayList<String>(temp));
                    temp.clear();
                }
            }

            for (List<String> l : listOfList) {
                if (l.size() != 0 && !timesOfDays.containsKey(l.get(0))) {
                    System.out.println(l.get(0) + " added to data");
                    Toast.makeText(context, l.get(0) + " added to data", Toast.LENGTH_SHORT).show();
                    for (String s : l) {
                        SaveData.writeToFile(activity.getApplicationContext(), s + "\n");
                    }
                    SaveData.writeToFile(activity.getApplicationContext(), "----------------------\n");
                } else if(l.size() != 0) {
                    System.out.println(l.get(0) + " already in data");
                }
            }

        }

    }

    public void startMainAct() {
        Intent mainAct = new Intent(context, MainActivity.class);
        mainAct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainAct);
    }

}
