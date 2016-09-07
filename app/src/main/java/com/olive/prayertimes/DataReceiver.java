package com.olive.prayertimes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by frkn on 07.09.2016.
 */
public class DataReceiver extends AsyncTask<List<Map.Entry<String, String>>, Void, List<Map.Entry<String, Integer>>> {

    private IlkKurulum activity;

    public DataReceiver(IlkKurulum activity) {
        this.activity = activity;
    }


    @Override
    protected List<Map.Entry<String, Integer>> doInBackground(List<Map.Entry<String, String>>... params) {
        if (params[0].size() == 0) {
            Log.d("doInBackground", "call for getCountries");
            return getCountries();
        } else if (params[0].size() == 1) {
            Log.d("doInBackground", "call for getStates");
            return getStates(params[0].get(0).getValue().toString());
        } else if (params[0].size() == 2) {
            if (activity.secilenUlke) {
                Log.d("doInBackground", "call for getDistricts");
                return getDictricts(params[0].get(0).getValue().toString(), params[0].get(1).getValue().toString());
            } else {
                Log.d("doInBackground", "call for getPrayerTimes");
                getPrayerTimes(params[0].get(0).getValue().toString(), params[0].get(1).getValue().toString(), "");
            }
        } else if (params[0].size() == 3) {
            Log.d("doInBackground", "call for getPrayerTimes");
            getPrayerTimes(params[0].get(0).getValue().toString(), params[0].get(1).getValue().toString(), params[0].get(2).getValue().toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Map.Entry<String, Integer>> list) {
        super.onPostExecute(list);
        if (activity.parameters.size() == 0) {
            Log.d("onPostExecute", "Countries are listing..");
            activity.countries_list = list;
            activity.temp = list;
            activity.fillListView(activity.convertMap2List(list));
        } else if (activity.parameters.size() == 1) {
            Log.d("onPostExecute", "States are listing..");
            activity.states_list = list;
            activity.temp = list;
            activity.fillListView(activity.convertMap2List(list));
        } else if (activity.parameters.size() == 2) {
            if (activity.secilenUlke) {
                Log.d("onPostExecute", "Districts are listing..");
                activity.districts_list = list;
                activity.temp = list;
                activity.fillListView(activity.convertMap2List(list));
            } else {
                Log.d("onPostExecute", "Times received..");
                activity.startMainAct();
            }
        } else if (activity.parameters.size() == 3) {
            Log.d("onPostExecute", "Times received..");
            activity.startMainAct();
        }

    }

    private List<Map.Entry<String, Integer>> getCountries() {
        Log.d("ASYNC", "getCuontries() starting..");
        org.jsoup.nodes.Document doc = null;
        List<Map.Entry<String, Integer>> pairList = new ArrayList<>();
        try {

            doc = Jsoup.connect(activity.url)
                    .userAgent("Mozilla")
                    .timeout(10 * 1000)
                    .post();

            for (Element opt : doc.select("span").get(12).child(0).children()) {
                pairList.add(new AbstractMap.SimpleEntry<String, Integer>(opt.text(), Integer.parseInt(opt.attr("value").toString())));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return pairList;
    }

    private List<Map.Entry<String, Integer>> getStates(String country_value) {
        Log.d("ASYNC", "getStates() starting..");
        org.jsoup.nodes.Document doc = null;
        List<Map.Entry<String, Integer>> pairList = new ArrayList<>();
        try {

            doc = Jsoup.connect(activity.url)
                    .data("Country", country_value)
                    .userAgent("Mozilla")
                    .timeout(10 * 1000)
                    .post();

            for (Element opt : doc.select("span").get(14).child(0).children()) {
                if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                    continue;
                pairList.add(new AbstractMap.SimpleEntry<String, Integer>(opt.text(), Integer.parseInt(opt.attr("value").toString())));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return pairList;
    }

    private List<Map.Entry<String, Integer>> getDictricts(String country_value, String state_value) {
        Log.d("ASYNC", "getDistricts() starting..");
        org.jsoup.nodes.Document doc = null;
        List<Map.Entry<String, Integer>> pairList = new ArrayList<>();
        try {

            doc = Jsoup.connect(activity.url)
                    .data("Country", country_value)
                    .data("State", state_value)
                    .userAgent("Mozilla")
                    .timeout(10 * 1000)
                    .post();

            for (Element opt : doc.select("span").get(16).child(0).children()) {
                if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                    continue;
                pairList.add(new AbstractMap.SimpleEntry<String, Integer>(opt.text(), Integer.parseInt(opt.attr("value").toString())));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return pairList;
    }

    private void getPrayerTimes(String country_value, String state_value, String district_value) {
        Log.d("ASYNC", "getPrayerTimes() starting..");
        String period_value = "Haftalik";
        org.jsoup.nodes.Document doc = null;
        List<List<String>> listOfList = new ArrayList<>();
        try {

            if (district_value == null) {
                doc = Jsoup.connect(activity.url)
                        .data("Country", country_value)
                        .data("State", state_value)
                        .data("period", period_value)
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();
            } else {
                doc = Jsoup.connect(activity.url)
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

            SaveData.writeToFile(activity.getApplicationContext(), getDistrictNameByValue(district_value) + ":" + district_value + "\n");
            SaveData.writeToFile(activity.getApplicationContext(), getStateNameByValue(state_value) + ":" + state_value + "\n");
            SaveData.writeToFile(activity.getApplicationContext(), getCountryNameByValue(country_value) + ":" + country_value + "\n");


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getCountryNameByValue(String value) {
        for (Map.Entry<String, Integer> entr : activity.countries_list) {
            if (Integer.parseInt(value) == entr.getValue()) {
                return entr.getKey().toString();
            }
        }
        //System.out.println("Country size: " + countries_list.size());
        return "";
    }

    private String getStateNameByValue(String value) {
        for (Map.Entry<String, Integer> entr : activity.states_list) {
            if (Integer.parseInt(value) == entr.getValue()) {
                return entr.getKey().toString();
            }
        }
        //System.out.println("State size: " + states_list.size());
        return "";
    }

    private String getDistrictNameByValue(String value) {
        if (value != null) {
            for (Map.Entry<String, Integer> entr : activity.districts_list) {
                if (Integer.parseInt(value) == entr.getValue()) {
                    return entr.getKey().toString();
                }
            }
            //System.out.println("District size: " + districts_list.size());
        }
        return "";
    }


}
