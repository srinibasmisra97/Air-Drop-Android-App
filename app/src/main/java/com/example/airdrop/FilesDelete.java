package com.example.airdrop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.airdrop.Constants.SERVER_HOST;

public class FilesDelete extends AppCompatActivity {

    private static final String TAG = "FilesDelete";
    private Handler mainHandler = new Handler();
    private CheckBox checkBoxes[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_delete);

        LinearLayout layout = (LinearLayout)findViewById(R.id.filesDeleteInternalLayout);

        LoadFiles loadFiles = new LoadFiles(this, layout, SERVER_HOST + "/api/files");
        new Thread(loadFiles).start();
    }

    public void filesDelete(View v){
        int count = 0;

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                count++;
            }
        }

        String filenames[] = new String[count];

        int i=0;
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                filenames[i++] = checkBox.getText().toString();
            }
        }

        DeleteFiles deleteFiles = new DeleteFiles(this, filenames, SERVER_HOST + "/api/files");
        new Thread(deleteFiles).start();
    }

    class DeleteFiles implements Runnable {
        Context context;
        String filenames[];
        String deleteEndpoint;

        DeleteFiles(Context context, String filenames[], String deleteEndpoint){
            this.context = context;
            this.filenames = filenames;
            this.deleteEndpoint = deleteEndpoint;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(this.deleteEndpoint);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("DELETE");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                httpURLConnection.setRequestProperty("Accept", "application.json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("files", new JSONArray(filenames));

                Log.d(TAG, "run: Sending request payload = " + jsonObject.toString());

                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                dataOutputStream.writeBytes(jsonObject.toString());

                final int responseCode = httpURLConnection.getResponseCode();

                dataOutputStream.flush();
                dataOutputStream.close();

                if (responseCode == HttpURLConnection.HTTP_OK){
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String inputLine;
                    final StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    final JSONObject responseJSON = new JSONObject(response.toString());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(context, responseJSON.getInt("count") + " files deleted!", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "SOME ERROR! HTTP " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

                int responseCode = httpURLConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK){
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JSONObject responseJSON = new JSONObject(response.toString());
                    final JSONArray filesList = responseJSON.getJSONArray("files");

                    checkBoxes = new CheckBox[filesList.length()];

                    for (int i=0 ; i < filesList.length() ; i++){
                        final String filename = filesList.getString(i);

                        final int finalI = i;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                CheckBox checkBox = new CheckBox(context);
                                checkBox.setId(finalI + 100);
                                checkBox.setText(filename);
                                checkBoxes[finalI] = checkBox;
                                layout.addView(checkBox);
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
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}