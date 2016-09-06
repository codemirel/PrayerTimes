package com.olive.prayertimes;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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
        File file = new File(path, "prayerTimes.txt");
        try {
            Files.append(str, file, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> readFromFile(Context context){
        Log.d(TAG, "readFromFile");
        File path = context.getFilesDir();
        File file = new File(path, "prayerTimes.txt");
        List<String> contents = new ArrayList<>();
        try {
            contents = Files.readLines(file, Charsets.UTF_8);
            return contents;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clearFile(Context context){
        Log.d(TAG, "clearFile");
        File path = context.getFilesDir();
        //System.out.println(path.toString());
        File file = new File(path, "prayerTimes.txt");
        try {
            Files.write("", file, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
