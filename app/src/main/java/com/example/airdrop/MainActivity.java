package com.example.airdrop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.airdrop.Constants.SERVER_HOST;

public class MainActivity extends AppCompatActivity {

    Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View v) {
        Intent serviceIntent = new Intent(this, AirDropPullService.class);

        startService(serviceIntent);
    }

    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, AirDropPullService.class);
        stopService(serviceIntent);
    }

    public void openFilesDownloadActivity(View v){
        Intent intent = new Intent(this, FilesDownload.class);
        startActivity(intent);
    }

    public void openNginxLogs(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SERVER_HOST + "/lg/nginx.access.txt"));
        startActivity(intent);
    }

    public void openServerLogs(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SERVER_HOST + "/lg/server.err.txt"));
        startActivity(intent);
    }

    public void healthCheck(View v){
        HealthCheck healthCheck = new HealthCheck(this);
        new Thread(healthCheck).start();
    }

    class HealthCheck implements Runnable {
        Context context;

        HealthCheck(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(SERVER_HOST + "/api/health");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");

                final int responseCode = httpURLConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK){
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Health is OK!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Health is not OK! HTTP Code: " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}