package com.olive.prayertimes;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    JSONObject par;
    boolean secilen=false;

    public DataReceiver(Context context) {
        this.context = context;
        View v = LayoutInflater.from(context).inflate(R.layout.activity_splash, null);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar2);
        progressText = (TextView) v.findViewById(R.id.progressText2);
        parameters = new JSONObject();
        countries_list = new JSONObject();
        states_list = new JSONObject();
        districts_list = new JSONObject();
        par = new JSONObject();
    }

    public void runForManuel() {
        new postForManuel().execute(parameters);
    }


    public void runForGps(){
        new postForGps().execute(par);
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
                Log.d("doInBackground", "call for getCountries");
                return getCountries();
            } else if (params[0].length() == 1) {
                Log.d("doInBackground", "call for getStates");
                try {
                    return getStates(params[0].getString("Country"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (params[0].length() == 2) {
                if (activity.secilenUlke) {
                    Log.d("doInBackground", "call for getDistricts");
                    try {
                        return getDictricts(params[0].getString("Country"), params[0].getString("State"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("doInBackground", "call for getPrayerTimes");
                    try {
                        getPrayerTimes(params[0].getString("Country"), params[0].getString("State"), "");
                        return null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (params[0].length() == 3) {
                Log.d("doInBackground", "call for getPrayerTimes");
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
                Log.d("onPostExecute", "Countries are listing..");
                countries_list = json_list;
                activity.fillListView(activity.json2List(json_list));
            } else if (parameters.length() == 1) {
                Log.d("onPostExecute", "States are listing..");
                states_list = json_list;
                activity.fillListView(activity.json2List(json_list));
            } else if (parameters.length() == 2) {
                if (activity.secilenUlke) {
                    Log.d("onPostExecute", "Districts are listing..");
                    districts_list = json_list;
                    activity.fillListView(activity.json2List(json_list));
                } else {
                    Log.d("onPostExecute", "Times received..");
                    activity.startMainAct();
                }
            } else if (parameters.length() == 3) {
                Log.d("onPostExecute", "Times received..");
                activity.startMainAct();
            }

        }

        private JSONObject getCountries() {
            Log.d("ASYNC", "getCuontries() starting..");
            org.jsoup.nodes.Document doc = null;
            JSONObject ret = new JSONObject();
            //List<Map.Entry<String, Integer>> pairList = new ArrayList<>();
            try {

                doc = Jsoup.connect(url)
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();

                for (Element opt : doc.select("span").get(12).child(0).children()) {
                    ret.put(opt.text(), opt.attr("value").toString());
                    //pairList.add(new AbstractMap.SimpleEntry<String, Integer>(opt.text(), Integer.parseInt(opt.attr("value").toString())));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ret;
            //return pairList;
        }

        private JSONObject getStates(String country_value) {
            Log.d("ASYNC", "getStates() starting..");
            org.jsoup.nodes.Document doc = null;
            JSONObject ret = new JSONObject();
            //List<Map.Entry<String, Integer>> pairList = new ArrayList<>();
            try {

                doc = Jsoup.connect(url)
                        .data("Country", country_value)
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();

                for (Element opt : doc.select("span").get(14).child(0).children()) {
                    if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                        continue;
                    ret.put(opt.text(), opt.attr("value").toString());
                    //pairList.add(new AbstractMap.SimpleEntry<String, Integer>(opt.text(), Integer.parseInt(opt.attr("value").toString())));
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
            //List<Map.Entry<String, Integer>> pairList = new ArrayList<>();
            try {

                doc = Jsoup.connect(url)
                        .data("Country", country_value)
                        .data("State", state_value)
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();

                for (Element opt : doc.select("span").get(16).child(0).children()) {
                    if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                        continue;
                    ret.put(opt.text(), opt.attr("value").toString());
                    //pairList.add(new AbstractMap.SimpleEntry<String, Integer>(opt.text(), Integer.parseInt(opt.attr("value").toString())));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ret;
            //return pairList;
        }

        private void getPrayerTimes(String country_value, String state_value, String district_value) {
            Log.d("ASYNC", "getPrayerTimes() starting..");
            String period_value = "Haftalik";
            org.jsoup.nodes.Document doc = null;
            List<List<String>> listOfList = new ArrayList<>();
            try {

                if (district_value.equals("") || district_value == null) {
                    doc = Jsoup.connect(url)
                            .data("Country", country_value)
                            .data("State", state_value)
                            .data("period", period_value)
                            .userAgent("Mozilla")
                            .timeout(10 * 1000)
                            .post();
                } else {
                    doc = Jsoup.connect(url)
                            .data("Country", country_value)
                            .data("State", state_value)
                            .data("City", district_value)
                            .data("period", period_value)
                            .userAgent("Mozilla")
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

                for (List<String> l : listOfList) {
                    if (l.size() != 0) {
                        for (String s : l) {
                            //System.out.println(s);
                            SaveData.writeToFile(activity.getApplicationContext(), s + "\n");
                        }
                        SaveData.writeToFile(activity.getApplicationContext(), "----------------------\n");
                        //System.out.println("-----------------------");
                    }
                }

                SaveData.writeToFile(activity.getApplicationContext(), getDistrictNameById(district_value) + ":" + district_value + "\n");
                SaveData.writeToFile(activity.getApplicationContext(), getStateNameById(state_value) + ":" + state_value + "\n");
                SaveData.writeToFile(activity.getApplicationContext(), getCountryNameById(country_value) + ":" + country_value + "\n");


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public class postForGps extends AsyncTask<JSONObject, Void, JSONObject>{

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
                Log.d("doInBackground", "call for getCountries");
                return getCountries();
            } else if (params[0].length() == 1) {
                Log.d("doInBackground", "call for getStates");
                try {
                    return getStates(params[0].getString("Country"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (params[0].length() == 2) {
                if (secilen) {
                    Log.d("doInBackground", "call for getDistricts");
                    try {
                        return getDictricts(params[0].getString("Country"), params[0].getString("State"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("doInBackground", "call for getPrayerTimes");
                    try {
                        getPrayerTimes(params[0].getString("Country"), params[0].getString("State"), "");
                        return null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (params[0].length() == 3) {
                Log.d("doInBackground", "call for getPrayerTimes");
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
            if (par.length() == 0) {
                Log.d("onPostExecute", "Countries are listing..");
                countries_list = json_list;
                try {
                    String id = getCountryIdByName(parameters.getString("Country"));
                    if(id.equals("2") || id.equals("33") || id.equals("52"))
                        secilen = true;
                    par.put("Country", id);
                    new postForGps().execute(par);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (par.length() == 1) {
                Log.d("onPostExecute", "States are listing..");
                states_list = json_list;
                try {
                    String id = getStateIdByName(parameters.getString("State"));
                    par.put("State", id);
                    new postForGps().execute(par);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (par.length() == 2) {
                if (secilen) {
                    Log.d("onPostExecute", "Districts are listing..");
                    districts_list = json_list;
                    try {
                        String id = getDistrictIdByName(parameters.getString("District"));
                        if(id.equals("")){
                            id = getDistrictIdByName(parameters.getString("State"));
                        }
                        par.put("District", id);
                        new postForGps().execute(par);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("onPostExecute", "Times received..");
                    startMainAct();
                }
            } else if (par.length() == 3) {
                Log.d("onPostExecute", "Times received..");
                startMainAct();
            }

        }

        private JSONObject getCountries() {
            Log.d("ASYNC", "getCuontries() starting..");
            org.jsoup.nodes.Document doc = null;
            JSONObject ret = new JSONObject();
            try {

                doc = Jsoup.connect(url)
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();

                for (Element opt : doc.select("span").get(12).child(0).children()) {
                    ret.put(opt.text(), opt.attr("value").toString());
                    //pairList.add(new AbstractMap.SimpleEntry<String, Integer>(opt.text(), Integer.parseInt(opt.attr("value").toString())));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ret;
            //return pairList;
        }

        private JSONObject getStates(String country_value) {
            Log.d("ASYNC", "getStates() starting..");
            org.jsoup.nodes.Document doc = null;
            JSONObject ret = new JSONObject();
            //List<Map.Entry<String, Integer>> pairList = new ArrayList<>();
            try {

                doc = Jsoup.connect(url)
                        .data("Country", country_value)
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();

                for (Element opt : doc.select("span").get(14).child(0).children()) {
                    if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                        continue;
                    ret.put(opt.text(), opt.attr("value").toString());
                    //pairList.add(new AbstractMap.SimpleEntry<String, Integer>(opt.text(), Integer.parseInt(opt.attr("value").toString())));
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
            //List<Map.Entry<String, Integer>> pairList = new ArrayList<>();
            try {

                doc = Jsoup.connect(url)
                        .data("Country", country_value)
                        .data("State", state_value)
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();

                for (Element opt : doc.select("span").get(16).child(0).children()) {
                    if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                        continue;
                    ret.put(opt.text(), opt.attr("value").toString());
                    //pairList.add(new AbstractMap.SimpleEntry<String, Integer>(opt.text(), Integer.parseInt(opt.attr("value").toString())));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ret;
            //return pairList;
        }

        private void getPrayerTimes(String country_value, String state_value, String district_value) {
            Log.d("ASYNC", "getPrayerTimes() starting..");
            String period_value = "Haftalik";
            org.jsoup.nodes.Document doc = null;
            List<List<String>> listOfList = new ArrayList<>();
            try {

                if (district_value.equals("") || district_value == null) {
                    doc = Jsoup.connect(url)
                            .data("Country", country_value)
                            .data("State", state_value)
                            .data("period", period_value)
                            .userAgent("Mozilla")
                            .timeout(10 * 1000)
                            .post();
                } else {
                    doc = Jsoup.connect(url)
                            .data("Country", country_value)
                            .data("State", state_value)
                            .data("City", district_value)
                            .data("period", period_value)
                            .userAgent("Mozilla")
                            .timeout(10 * 1000)
                            .post();
                }

                //System.out.println(doc);

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
                        for (String s : l) {
                            //System.out.println(s);
                            SaveData.writeToFile(context, s + "\n");
                        }
                        SaveData.writeToFile(context, "----------------------\n");
                        //System.out.println("-----------------------");
                    }
                }

                SaveData.writeToFile(context, getDistrictNameById(district_value) + ":" + district_value + "\n");
                SaveData.writeToFile(context. getApplicationContext(), getStateNameById(state_value) + ":" + state_value + "\n");
                SaveData.writeToFile(context. getApplicationContext(), getCountryNameById(country_value) + ":" + country_value + "\n");


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void startMainAct() {
        Intent mainAct = new Intent(context, MainActivity.class);
        mainAct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainAct);
    }

}
