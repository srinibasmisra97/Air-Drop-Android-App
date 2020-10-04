package com.example.airdrop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.airdrop.Constants.SERVER_HOST;

public class FilesDownload extends AppCompatActivity {

    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_download);

        LinearLayout layout = (LinearLayout)findViewById(R.id.filesDownloadLayout);

        LoadFiles loadFiles = new LoadFiles(this, layout, SERVER_HOST + "/api/files");
        new Thread(loadFiles).start();
    }

    class LoadFiles implements Runnable {
        String filesEndpoint;
        Context context;
        LinearLayout layout;

        LoadFiles(Context context, LinearLayout layout, String filesEndpoint){
            this.context = context;
            this.layout = layout;
            this.filesEndpoint = filesEndpoint;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(this.filesEndpoint);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");

                final int responseCode = httpURLConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK){
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JSONObject responseJSON = new JSONObject(response.toString());
                    final JSONArray filesList = responseJSON.getJSONArray("files");

                    for (int i=0 ; i < filesList.length() ; i++){
                        final String filename = filesList.getString(i);

                        final int finalI = i;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Button fileButton = new Button(context);
                                fileButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                fileButton.setText(filename);
                                fileButton.setId(finalI +20);
                                fileButton.setPadding(2,5,2,5);
                                fileButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SERVER_HOST + "/api/download?file=" + filename));
                                        startActivity(intent);
                                    }
                                });

                                layout.addView(fileButton);
                            }
                        });
                    }

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, filesList.length() + " files loaded!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Error! HTTP " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}