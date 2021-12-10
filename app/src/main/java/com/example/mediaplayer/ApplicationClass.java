package com.example.mediaplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {
    public static final String CHANNEL_ID_1 = "CHANNEL_1";
    public static final String CHANNEL_ID_2 = "CHANNEL_2";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_NEXT = "ACTION_NEXT";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            // CHANNEL ONE
            NotificationChannel channel_1 = new NotificationChannel(CHANNEL_ID_1, "Channel(1)",
                    NotificationManager.IMPORTANCE_HIGH);
            channel_1.setDescription("Channel 1 Description");

            // CHANNEL TWO
            NotificationChannel channel_2 = new NotificationChannel(CHANNEL_ID_1, "Channel(2)",
                    NotificationManager.IMPORTANCE_HIGH);
            channel_1.setDescription("Channel 2 Description");

            // NOTIFICATION MANAGER
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel_1);
            notificationManager.createNotificationChannel(channel_2);

        }

    }
}
