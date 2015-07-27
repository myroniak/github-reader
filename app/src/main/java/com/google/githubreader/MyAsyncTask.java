package com.google.githubreader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class MyAsyncTask extends AsyncTask<Void, Void, String> {

    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    ProgressDialog pDialog;
    String urlLink, line, progress, resultJson = "";
    Context context;

    public MyAsyncTask(Context context, String urlLink, ProgressDialog pDialog, String progress) {

        this.pDialog = pDialog;
        this.urlLink = urlLink;
        this.context = context;
        this.progress = progress;

    }

    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(progress);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {


        try {
            URL url = new URL(urlLink);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            resultJson = buffer.toString();

        } catch (IOException e) {
            Log.e("ERROR", "ERROR IN CODE: " + e.toString());
            e.printStackTrace();

            return "ERROR_IN_CODE";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    protected abstract void onPostExecute(String strJson);
}