package com.olive.prayertimes;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;


/**
 * Created by mdemirelcs on 9/7/16.
 */
public class BackgroundServices extends Service {

    Handler handler;
    TimeUpdater timeUpdater;

    String[] timesOfUpdate = {"03:00:00", "09:00:00", ""};

    @Override
    public void onCreate() {
        Log.d("BACKGROUND", "SERVICE CREATE");
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d("BACKGROUND", "SERVICE START");
    }

    @Override
    public void onDestroy() {
        Log.d("BACKGROUND", "SERVICE DESTROY");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BACKGROUND", "SERVICE COMMAND");
        handler = new Handler();
        timeUpdater = new TimeUpdater(this);
        timeUpdater.fillTimesOfDays();
        handler.postDelayed(runnable, 0);
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void send_notification(String show){
        Log.d("NOTIF", show);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setLargeIcon(bitmap)
                        .setSmallIcon(getNotificationIcon())
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(show))
                        .setContentTitle(show)
                        .setOngoing(true)
                        .setContentIntent(resultPendingIntent)
                        .setContentText(timeUpdater.getCurrentDate());
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(101, mBuilder.build());
    }

    private int getNotificationIcon() {
        return R.drawable.ic_action_kible;
    }


    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */

            Log.d("Flag", String.valueOf(timeUpdater.flagUpdate));

            if (!timeUpdater.flagUpdate && isOnline()){
                Log.d("updateTime", timeUpdater.getCurrentDate() + " <=> " + timeUpdater.getCurrentTime());
                timeUpdater.getStateInfo();
                timeUpdater.receiveNewData();
                timeUpdater.flagUpdate = true;
            }

            if (timeUpdater.getCurrentTime().equals("00:00:00")) {
                timeUpdater.flagUpdate = false;
                timeUpdater.fillTimesOfDays();
            }
            timeUpdater.calcDiffInTime();
            System.out.println(timeUpdater.getCurrentTime());
            send_notification(timeUpdater.toNotif);

      /* and here comes the "trick" */
            handler.postDelayed(this, 1000);
        }
    };

    private Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

}
