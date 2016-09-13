package com.olive.prayertimes;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import com.google.common.base.Charsets;
//import com.google.common.io.Files;

/**
 * Created by frkn on 05.09.2016.
 */
public class SaveData {

    private static final String TAG = "SaveData";

    public SaveData() {
    }

    public static void writeToFile(Context context, String str){
        Log.d(TAG, "writeToFile");
        File path = context.getFilesDir();
        //System.out.println(path.toString());
        File file = new File(path, "prayerTimes.bin");
        try {
            FileWriter out = new FileWriter(file, true);
            out.append(str);
            out.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
       /* try {
            Files.append(str, file, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static List<String> readFromFile(Context context){
        Log.d(TAG, "readFromFile");
        File path = context.getFilesDir();
        File file = new File(path, "prayerTimes.bin");
        List<String> contents = new ArrayList<>();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(file));
            while ((line = in.readLine()) != null)
                contents.add(line);
            return contents;
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        /*try {
            contents = Files.readLines(file, Charsets.UTF_8);
            return contents;
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return null;
    }

    public static void clearFile(Context context){
        Log.d(TAG, "clearFile");
        writeToFile(context, "");
        /*File path = context.getFilesDir();
        //System.out.println(path.toString());
        File file = new File(path, "prayerTimes.bin");
        try {
            Files.write("", file, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
