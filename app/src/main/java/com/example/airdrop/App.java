package com.example.airdrop;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import static com.example.airdrop.Constants.PULL_NOTIFICATION_CHANNEL_TITLE;
import static com.example.airdrop.Constants.DOWNLOAD_NOTIFICATION_CHANNEL_TITLE;

public class App extends Application {
    public static final String SERVICE_CHANNEL_ID = "airDropPullServiceChannel";
    public static final String DOWNLOAD_CHANNEL_ID = "downloadNotificationsChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        createDownloadNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    SERVICE_CHANNEL_ID,
                    PULL_NOTIFICATION_CHANNEL_TITLE,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void createDownloadNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel downloadChannel = new NotificationChannel(
                    DOWNLOAD_CHANNEL_ID,
                    DOWNLOAD_NOTIFICATION_CHANNEL_TITLE,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(downloadChannel);
        }
    }
}
