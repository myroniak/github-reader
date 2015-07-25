package com.google.gitreadertest;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ReaderInfo extends ActionBarActivity {

    private static final String TAG = "myLogs";
    private ProgressDialog pDialog;

    TextView txtCompany, txtFollowers, txtFollowing, txtUsername;
    ImageView imgView;
    String userUrl;
    String username;
    Intent intent;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009688")));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        setContentView(R.layout.activity_reader);

        txtFollowers = (TextView) findViewById(R.id.textView3);
        txtFollowing = (TextView) findViewById(R.id.textView4);
        txtUsername = (TextView) findViewById(R.id.textView5);
        txtCompany = (TextView) findViewById(R.id.textView6);

        imgView = (ImageView) findViewById(R.id.profile_image);

        ListView listView = (ListView) findViewById(R.id.listView);
        TextView emptyText = (TextView) findViewById(android.R.id.empty);
        listView.setEmptyView(emptyText);

        username = getIntent().getStringExtra("username");

        dbHelper = new DBHelper(this);

        new ParseTask().execute();
    }


    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        FormattingNumbers formattingNumbers = new FormattingNumbers();


        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ReaderInfo.this);
            pDialog.setMessage("Downloading data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(Void... params) {
            // get string with MainActivity

            try {
                URL url = new URL("https://api.github.com/users/" + username);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            pDialog.dismiss();

            JSONObject dataJsonObj = null;

            try {
                dataJsonObj = new JSONObject(strJson);

                int followers = dataJsonObj.optInt("followers");
                int following = dataJsonObj.optInt("following");

                String name = dataJsonObj.optString("name");
                String company = dataJsonObj.optString("company");

                String avatar_url = dataJsonObj.optString("avatar_url");
                userUrl = dataJsonObj.optString("html_url");

                UrlImageViewHelper.setUrlDrawable(imgView, avatar_url);

                txtFollowers.setText(formattingNumbers.format(followers) + "\n" + "followers");
                txtFollowing.setText(formattingNumbers.format(following) + "\n" + "following");

                //check username field on empty result
                if (name.matches("") | name == null | name.matches("null")) {
                    txtUsername.setText("No name" + ", ");
                } else {
                    txtUsername.setText(name );
                }

                //check company field on empty result
                if (company.matches("") | company == null | company.matches("null")) {
                    txtCompany.setText("No company");
                } else {
                    txtCompany.setText(", "+ company);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void browseButton(View v) {

        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(userUrl));
        startActivity(intent);

    }

    public void sharedLinkButton(View v) {

        intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "https://github.com/" + username);
        startActivity(Intent.createChooser(intent, "Share user link"));

    }

    public void saveDataUserButton(View v) {

        ContentValues cv = new ContentValues();

        // получаем данные из полей ввода
        String userName = txtUsername.getText().toString();
        String followers = txtFollowers.getText().toString();
        String following = txtFollowing.getText().toString();

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        Log.d(TAG, "--- Insert in mytable: ---");
        // подготовим данные для вставки в виде пар: наименование столбца - значение

        cv.put("username", userName);
        cv.put("followers", followers);
        cv.put("following", following);

// вставляем запись и получаем ее ID
        long rowID = db.insert("mytable", null, cv);
        Toast toast = Toast.makeText(getApplicationContext(),
                "Added:" + "ID = " + rowID + "\n username: " + userName +
                        "\n followers: " + followers + "\n following: " + following,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        // закрываем подключение к БД
        dbHelper.close();
    }

}
