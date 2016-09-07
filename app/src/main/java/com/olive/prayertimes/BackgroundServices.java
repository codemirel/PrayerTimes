package com.olive.prayertimes;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;


/**
 * Created by mdemirelcs on 9/7/16.
 */
public class BackgroundServices extends IntentService {

    public BackgroundServices() {
        super("BackgroundServices");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        String show = intent.getDataString();
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setLargeIcon(bitmap)
                        .setSmallIcon(android.R.color.transparent)
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(show))
                        .setContentTitle(show)
                        .setOngoing(true)
                        .setContentText(MainActivity.getCurrentDate());
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(101, mBuilder.build());
    }
}
