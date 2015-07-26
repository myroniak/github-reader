package com.google.gitreadertest;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReaderInfo extends ActionBarActivity {

    private static final String TAG = "myLogs";


    TextView tvCompany, tvFollowers, tvFollowing, tvUsername, emptyText;
    ArrayList<Timetable> items_list;
    String username;
    ProgressDialog pDialog;
    JSONObject dataJsonObj;
    ImageView imgView;
    ListView listView;
    Intent intent;
    int followers, following;
    DBHelper dbHelper;
    ShortThousand formattingNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009688")));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        setContentView(R.layout.activity_reader);

        tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        tvFollowing = (TextView) findViewById(R.id.tvFollowing);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvCompany = (TextView) findViewById(R.id.tvCompany);
        emptyText = (TextView) findViewById(android.R.id.empty);

        imgView = (ImageView) findViewById(R.id.profile_image);

        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView(emptyText);

        username = getIntent().getStringExtra("username");

        dbHelper = new DBHelper(this);


        new ParseTask(this, "https://api.github.com/users/" + username, pDialog) {

            // override method for get all data about user
            @Override
            protected void onPostExecute(String strJson) {
                pDialog.dismiss();

                try {
                    dataJsonObj = new JSONObject(strJson);

                    followers = dataJsonObj.optInt("followers");
                    following = dataJsonObj.optInt("following");

                    String name = dataJsonObj.optString("name");
                    String company = dataJsonObj.optString("company");
                    String login = dataJsonObj.optString("login");
                    Log.d(TAG,"login: "+login);
                    String message = dataJsonObj.optString("message");
                    Log.d(TAG,"message"+message);

                    String avatar_url = dataJsonObj.optString("avatar_url");


                    //check found user
                    if (message == "") {
                        UrlImageViewHelper.setUrlDrawable(imgView, avatar_url);

                        tvFollowers.setText(formattingNumbers.format(followers) + "\n" + "followers");
                        tvFollowing.setText(formattingNumbers.format(following) + "\n" + "following");

                        //check username field on empty result
                        if (name.matches("") | name == null | name.matches("null")) {
                            tvUsername.setText("No name");
                        } else {
                            tvUsername.setText(name);
                        }

                        //check company field on empty result
                        if (company.matches("") | company == null | company.matches("null")) {
                            tvCompany.setText(", No company");
                        } else {
                            tvCompany.setText("("+login+")" +", " + company);
                        }

                    } else {

                        new AlertDialog.Builder(context)
                                .setTitle("Error")
                                .setMessage(message)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setIcon(R.drawable.error_icon)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();


        new ParseTask(ReaderInfo.this, "https://api.github.com/users/" + username + "/repos", pDialog) {

            // override method for get repositories current user
            @Override
            protected void onPostExecute(String strJson) {
                pDialog.dismiss();

                formattingNumbers = new ShortThousand();
                items_list = new ArrayList();

                try {

                    JSONArray jsonArray = new JSONArray(strJson);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonobject = jsonArray.getJSONObject(i);

                        int countStar = jsonobject.getInt("stargazers_count");
                        int countFork = jsonobject.getInt("forks_count");

                        String language = jsonobject.getString("language");
                        String name = jsonobject.getString("name");

                        if (language.matches("") | language == null | language.matches("null")) {
                            language="unknown";
                        }
                        items_list.add(new Timetable(name, language, formattingNumbers.format(countFork),
                                formattingNumbers.format(countStar)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                TimetableAdapter adapter = new TimetableAdapter(ReaderInfo.this, items_list);
                listView.setAdapter(adapter);

            }
        }.execute();
    }

    public void browseButton(View v) {

        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/"+username));
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

        // get data with editText
        String userName = tvUsername.getText().toString();

        // connect to DB
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //paste data
        cv.put("username", userName);
        cv.put("followers", followers);
        cv.put("following", following);

        // insert a record and get her ID
        long rowID = db.insert("mytable", null, cv);
        Toast toast = Toast.makeText(getApplicationContext(),
                "Added:" + "ID = " + rowID + "\n username: " + userName +
                        "\n followers: " + followers + "\n following: " + following,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        // close connect to DB
        dbHelper.close();
    }
}