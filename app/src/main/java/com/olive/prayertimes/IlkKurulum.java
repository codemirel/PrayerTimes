package com.olive.prayertimes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IlkKurulum extends AppCompatActivity {

    ArrayAdapter<String> adapter;
    public static final String url = "http://www.diyanet.gov.tr/tr/PrayerTime/PrayerTimesList";
    List<Map.Entry<String, Integer>> countries_list;
    List<Map.Entry<String, Integer>> states_list;
    List<Map.Entry<String, Integer>> districts_list;
    List<Map.Entry<String, String>> parameters;
    List<Map.Entry<String, Integer>> temp;
    ListView lv;
    final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    boolean secilenUlke = false;

    EditText searchTextBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ilk_kurulum);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initialize();
        //SaveData.clearFile(getApplicationContext());

        new JsoupPost(this).execute(parameters);

        searchTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                IlkKurulum.this.adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final String[] selectedUlke = {null};
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<Map.Entry<String, Integer>> useList = null;
                String region = null;
                String selected = (String) lv.getItemAtPosition(position);

                switch (parameters.size()) {
                    case 0: {
                        selectedUlke[0] = selected;
                        useList = temp;
                        region = "Country";
                        if (selectedUlke[0].equals("ABD") || selectedUlke[0].equals("TÜRKİYE") || selectedUlke[0].equals("KANADA")) {
                            secilenUlke = true;

                        } else {
                            secilenUlke = false;
                        }
                    }
                    case 1: {
                        useList = temp;
                        region = "State";
                        if (!secilenUlke) {
                            settings.edit().putBoolean(selected, true);
                        }
                        break;
                    }
                    case 2: {
                        useList = temp;
                        region = "District";
                        if (secilenUlke) {
                            settings.edit().putBoolean(selected, true);
                        }
                        break;
                    }

                }
                for (Map.Entry<String, Integer> entr : temp) {
                    if (selected.equals(entr.getKey())) {
                        parameters.add(new AbstractMap.SimpleEntry<String, String>(region, Integer.toString(entr.getValue())));
                        new JsoupPost(IlkKurulum.this).execute(parameters);
                        break;
                    }
                }

                switch (parameters.size()) {
                    case 0: {
                        if (selectedUlke[0].equals("ABD") || selectedUlke[0].equals("TÜRKİYE") || selectedUlke[0].equals("KANADA")) {
                            secilenUlke = true;
                            if (selectedUlke[0].equals("ABD") || selectedUlke[0].equals("KANADA"))
                                setTitle("Eyalet Seçiniz");
                            else
                                setTitle("Şehir Seçiniz");
                        } else {
                            secilenUlke = false;
                        }
                    }
                    case 1: {
                        if (!secilenUlke) {
                            setTitle("Şehir Seçiniz");
                        } else {
                            if (selectedUlke[0].equals("ABD") || selectedUlke[0].equals("KANADA"))
                                setTitle("Şehir Seçiniz");
                            else
                                setTitle("İlçe Seçiniz");
                        }
                        break;
                    }

                }

            }
        });
    }

    private void fillListView(List<String> list) {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                return view;
            }
        };

        lv.setAdapter(adapter);
    }

    private List<String> convertMap2List(List<Map.Entry<String, Integer>> param) {
        List<String> backList = new ArrayList<>();
        for (int i = 0; i < param.size(); i++) {
            String s = param.get(i).getKey().toString();
            backList.add(s);
        }
        return backList;
    }

    private String getCountryNameByValue(String value) {
        for (Map.Entry<String, Integer> entr : countries_list) {
            if (Integer.parseInt(value) == entr.getValue()) {
                return entr.getKey().toString();
            }
        }
        return null;
    }

    private String getStateNameByValue(String value) {
        for (Map.Entry<String, Integer> entr : states_list) {
            if (Integer.parseInt(value) == entr.getValue()) {
                return entr.getKey().toString();
            }
        }
        return null;
    }

    private String getDistrictNameByValue(String value) {
        if (value != null) {
            for (Map.Entry<String, Integer> entr : districts_list) {
                if (Integer.parseInt(value) == entr.getValue()) {
                    return entr.getKey().toString();
                }
            }
        }
        return null;
    }

    private class JsoupPost extends AsyncTask<List<Map.Entry<String, String>>, Void, List<Map.Entry<String, Integer>>> {

        private IlkKurulum activity;

        public JsoupPost(IlkKurulum activity) {
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
                if (secilenUlke) {
                    Log.d("doInBackground", "call for getDistricts");
                    return getDictricts(params[0].get(0).getValue().toString(), params[0].get(1).getValue().toString());
                } else {
                    Log.d("doInBackground", "call for getPrayerTimes");
                    getPrayerTimes(params[0].get(0).getValue().toString(), params[0].get(1).getValue().toString(), null);
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
                if (secilenUlke) {
                    Log.d("onPostExecute", "Districts are listing..");
                    activity.districts_list = list;
                    activity.temp = list;
                    activity.fillListView(activity.convertMap2List(list));
                } else {
                    Log.d("onPostExecute", "Times received..");
                    //System.out.println(SaveData.readFromFile(getApplicationContext()));
                    Intent mainAct = new Intent(IlkKurulum.this, MainActivity.class);
                    startActivity(mainAct);
                    finish();

                }
            } else if (activity.parameters.size() == 3) {
                Log.d("onPostExecute", "Times received..");
                //System.out.println(SaveData.readFromFile(getApplicationContext()));
                Intent mainAct = new Intent(IlkKurulum.this, MainActivity.class);
                startActivity(mainAct);
                finish();
            }

        }

        private List<Map.Entry<String, Integer>> getCountries() {
            Log.d("ASYNC", "getCuontries() starting..");
            org.jsoup.nodes.Document doc = null;
            List<Map.Entry<String, Integer>> pairList = new ArrayList<>();
            try {

                doc = Jsoup.connect(url)
                        //.data("Country", Integer.toString(2))
                        //.data("State", Integer.toString(552))
                        //.data("City", Integer.toString(9676))
                        //.data("period", "Haftalik")
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();


                for (Element opt : doc.select("span").get(12).child(0).children()) {
                    //System.out.println(opt.text() + ": " + opt.attr("value").toString() + "\n--------------\n");
                    //countries_map.put(opt.text(), Integer.parseInt(opt.attr("value").toString()));
                    //countries_list.add(opt.text());
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

                doc = Jsoup.connect(url)
                        .data("Country", country_value)
                        //.data("State", Integer.toString(552))
                        //.data("City", Integer.toString(9676))
                        //.data("period", "Haftalik")
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();

        /*for(Element sp : doc.select("span"))
            System.out.println(sp);*/

                for (Element opt : doc.select("span").get(14).child(0).children()) {
                    if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                        continue;
                    //System.out.println(opt.text() + ": " + opt.attr("value").toString());
                    //countries_map.put(opt.text(), Integer.parseInt(opt.attr("value").toString()));
                    //countries_list.add(opt.text());
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

                doc = Jsoup.connect(url)
                        .data("Country", country_value)
                        .data("State", state_value)
                        //.data("City", Integer.toString(9676))
                        //.data("period", "Haftalik")
                        .userAgent("Mozilla")
                        .timeout(10 * 1000)
                        .post();

        /*for(Element sp : doc.select("span"))
            System.out.println(sp);*/

                for (Element opt : doc.select("span").get(16).child(0).children()) {
                    if (opt.text() == "Seciniz" || opt.attr("value").toString() == "")
                        continue;
                    //System.out.println(opt.text() + ": " + opt.attr("value").toString());
                    //countries_map.put(opt.text(), Integer.parseInt(opt.attr("value").toString()));
                    //countries_list.add(opt.text());
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

                SaveData.clearFile(getApplicationContext());

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
                            SaveData.writeToFile(getApplicationContext(), s + "\n");
                        }
                        SaveData.writeToFile(getApplicationContext(), "----------------------\n");
                        //System.out.println("-----------------------");
                    }
                }

                SaveData.writeToFile(getApplicationContext(), activity.getDistrictNameByValue(district_value) + "\n");
                SaveData.writeToFile(getApplicationContext(), activity.getStateNameByValue(state_value) + "\n");
                SaveData.writeToFile(getApplicationContext(), activity.getCountryNameByValue(country_value) + "\n");


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void initialize() {
        lv = (ListView) findViewById(R.id.listView);
        searchTextBox = (EditText) findViewById(R.id.searchText);
        countries_list = new ArrayList<>();
        states_list = new ArrayList<>();
        districts_list = new ArrayList<>();
        parameters = new ArrayList<>();
        temp = new ArrayList<>();
        settings = getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();
    }


}
