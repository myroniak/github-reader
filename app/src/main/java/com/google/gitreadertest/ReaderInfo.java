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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ReaderInfo extends ActionBarActivity {

    private static final String TAG = "myLogs";
    ProgressDialog pDialog;
    FormattingNumbers formattingNumbers = new FormattingNumbers();
    TextView txtCompany, txtFollowers, txtFollowing, txtUsername;
    ImageView imgView;
    String userUrl;
    String username;
    Intent intent;
    DBHelper dbHelper;
    ArrayList<Timetable> items_monday;
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

        new ParseTask(ReaderInfo.this,"https://api.github.com/users/"+username, pDialog) {
            @Override
            protected void onPostExecute(String strJson)  {
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
                        txtUsername.setText("No name");
                    } else {
                        txtUsername.setText(name );
                    }

                    //check company field on empty result
                    if (company.matches("") | company == null | company.matches("null")) {
                        txtCompany.setText(", No company");
                    } else {
                        txtCompany.setText(", "+ company);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();


        new ParseTask(ReaderInfo.this,"https://api.github.com/users/"+username+"/repos", pDialog) {
            @Override
            protected void onPostExecute(String strJson)  {
                pDialog.dismiss();
                items_monday = new ArrayList();

                try {

                    JSONArray jsonArray = new JSONArray(strJson);
                    Log.d(TAG, "jsAr:" + jsonArray);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonobject = jsonArray.getJSONObject(i);
                        int countStar = jsonobject.getInt("stargazers_count");
                        int countFork = jsonobject.getInt("forks_count");
                        String language = jsonobject.getString("language");
                        String name = jsonobject.getString("name");
                        items_monday.add(new Timetable(name, language, format(countFork), format(countStar)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ListView listView = (ListView) findViewById(R.id.listView);
                TextView emptyText = (TextView)findViewById(android.R.id.empty);
                listView.setEmptyView(emptyText);
                TimetableAdapter adapter = new TimetableAdapter(ReaderInfo.this, items_monday);
                listView.setAdapter(adapter);


            }}.execute();
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


    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
