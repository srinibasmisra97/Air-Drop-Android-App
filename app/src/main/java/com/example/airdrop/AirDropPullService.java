package com.example.airdrop;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import static com.example.airdrop.App.DOWNLOAD_CHANNEL_ID;
import static com.example.airdrop.App.SERVICE_CHANNEL_ID;

import static com.example.airdrop.Constants.SERVER_HOST;

public class AirDropPullService extends Service {
    private static final String TAG = "AirDropPullService";

    private Handler serviceHandler = new Handler();

    Boolean RUN_POLL = true;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RUN_POLL = true;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setContentTitle("Air Drop Pull Service")
                .setSmallIcon(R.drawable.ic_pull_service_icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

//        Log.d(TAG, "onStartCommand: Starting Thread");
//        Log.d(TAG, "onStartCommand: RUN_POLL=" + RUN_POLL);

        PollSubscription pollSubscription = new PollSubscription(this, SERVER_HOST + "/api/pull", SERVER_HOST + "/api/download?file=");
        new Thread(pollSubscription).start();

        Toast.makeText(this, "Starting pull service!", Toast.LENGTH_LONG).show();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
//        Log.d(TAG, "onDestroy: Destroying service!");
        RUN_POLL = false;
//        Log.d(TAG, "onDestroy: RUN_POLL=" + RUN_POLL);

        Toast.makeText(this, "Stopping pull service!", Toast.LENGTH_LONG).show();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class PollSubscription implements Runnable {
        String pullEndpoint;
        String downloadEndpoint;
        Context context;

        PollSubscription(Context context, String pullEndpoint, String downloadEndpoint){
            this.context = context;
            this.pullEndpoint = pullEndpoint;
            this.downloadEndpoint = downloadEndpoint;
        }

        @Override
        public void run() {
            while (RUN_POLL) {
                try {
                    URL url = new URL(this.pullEndpoint);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");

                    final int responseCode = httpURLConnection.getResponseCode();
//                    Log.d(TAG, "run: ResponseCode=" + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK){
                        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while((inputLine = in.readLine())!=null){
                            response.append(inputLine);
                        }
//                        Log.d(TAG, "run: Response=" + response.toString());

                        JSONObject responseJSON = new JSONObject(response.toString());
                        final int count = Integer.parseInt(String.valueOf(responseJSON.getInt("count")));
                        JSONArray data = responseJSON.getJSONArray("data");
//                        Log.d(TAG, "run: New files count=" + count);

                        if (count > 0){
                            serviceHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, count + " new files added!", Toast.LENGTH_LONG).show();
                                }
                            });

                            final Random random = new Random();

                            for (int i=0 ; i<count ; i++){
                                final String filename = data.getString(i);
//                                Log.d(TAG, "run: New file=" + filename);

                                serviceHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadEndpoint + filename));
                                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
                                                .setContentTitle(filename)
                                                .setSmallIcon(R.drawable.ic_pull_service_icon)
                                                .setContentIntent(pendingIntent);

                                        int notificationId = random.nextInt(50) + 1;

                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                                        notificationManager.notify(notificationId, builder.build());
                                    }
                                });
                            }
                        }

                        in.close();
                    }
                    else {
                        serviceHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Error! HTTP " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
