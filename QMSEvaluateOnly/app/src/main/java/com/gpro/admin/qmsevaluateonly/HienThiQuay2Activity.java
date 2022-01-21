package com.gpro.admin.qmsevaluateonly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class HienThiQuay2Activity extends AppCompatActivity {
    TextView lbTieuDeDG, lbThanks, lbNumber;
    String IPAddress, UserName, Password, url, TicketNumber, requireLabel, appType;
    Integer sizeTicket = 100, sizeButton = 10, sizeRequire = 10, userId, number = 0, evaluating = 0, useQMS = 0;
    JsonArrayRequest jsonArrayRequest;
    JsonObjectRequest jsonRequest;
    RequestQueue requestQueue;
    private Context mContext;
    Intent intent;
    Boolean stopThread = false;

    private static int
            NUM_ROW = 2,
            NUM_COL = 5,
            FONT_SIZE = 50,
            FONT_SIZE_STT = 12,
            BUT_WIDTH = 200,
            BUT_HEIGHT = 200,
            But_BackgroundColor = -15859455,
            But_TextColor = -15859455;
    static Button[] buttonArr;
    JSONArray evaluates = null;
    ProgressDialog progressDialog;
    Thread thread;
    public RequestQueue mRequestQueue = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hien_thi_quay2);

        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(HienThiQuay2Activity.this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Đang tải dữ liệu...");
        progressDialog.show();

        intent = getIntent();
        lbNumber = (TextView) findViewById(R.id.lbNumber);
        SetAppConfig();

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
     mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        //region  lấy số thứ tự đang gọi
        lbNumber.setTextSize(sizeTicket);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DS-DIGIB.TTF");
        lbNumber.setTypeface(face);

        lbNumber.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                intent = new Intent(HienThiQuay2Activity.this, AppConfigActivity.class);
                intent.putExtra("hold", "1");
                startActivity(intent);
                return false;
            }
        });

          thread = new Thread() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(stopThread)
                                    return;

                                url = IPAddress + "/api/serviceapi/getnumber?username=" + UserName;
                                // requestQueue = Volley.newRequestQueue(MainActivity.this);
                                jsonRequest = new JsonObjectRequest(
                                        Request.Method.GET,
                                        url,
                                        null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                progressDialog.hide();
                                                if (response != null) {
                                                    Integer num = response.optInt("Id");
                                                    lbNumber.setText(String.valueOf(num));
                                                    number = num;

                                                    Integer status = response.optInt("Data");
                                                    if (status == 1) {
                                                        mRequestQueue.stop();
                                                        mRequestQueue = null;
                                                         intent = new Intent(HienThiQuay2Activity.this, ThreeButton2Activity.class);
                                                       // intent.putExtra("hold", "1");
                                                        startActivity(intent);
                                                        //thread.stop();
                                                        stopThread=true;
                                                    }
                                                } else {
                                                    lbNumber.setText("0000");
                                                    number = 0;
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                progressDialog.hide();
                                                lbNumber.setText("ERR");
                                            }
                                        }
                                );
                                jsonRequest.setShouldCache(false);
                                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                mRequestQueue.add(jsonRequest);
                            }

                            public void cancel() { interrupt(); }
                        });
                    } catch (InterruptedException e) {
                        //progressDialog.hide();
                        //Toast.makeText(HienThiQuay2Activity.this, e.getMessage(), Toast.LENGTH_LONG);
                        // e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        //endregion
    }

    private void SetAppConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(HienThiQuay2Activity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            appType = sharedPreferences.getString("APP_TYPE", "1");
            Intent intent;
            switch (appType) {
                case "0":
                    intent = new Intent(HienThiQuay2Activity.this, ThreeButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(HienThiQuay2Activity.this, PrintTicketActivity.class);
                    startActivity(intent);
                    break;
                case "3":
                    intent = new Intent(HienThiQuay2Activity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case "4":
                    intent = new Intent(HienThiQuay2Activity.this, CountersEventActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            UserName = sharedPreferences.getString("UserName", "0");
            Password = sharedPreferences.getString("Password", "0");

            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));

            FONT_SIZE = (Integer.parseInt(sharedPreferences.getString("SizeNext", "12")));
            FONT_SIZE_STT = (Integer.parseInt(sharedPreferences.getString("SizeSTTNext", "12")));
            useQMS = Integer.parseInt(sharedPreferences.getString("UseQMS", "0"));

            sizeTicket = (Integer.parseInt(sharedPreferences.getString("SizeSTT", "200")));
            try {
                lbNumber.setTextColor(Integer.parseInt(sharedPreferences.getString("ColorSTT", "-15859455")));
            } catch (Exception ex) {  }

        }
    }

    @Override
    public void onDestroy() {
        thread.interrupt();
        super.onDestroy();
    }
}