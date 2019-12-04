package com.gpro.admin.qmsevaluateonly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONObject;

public class HienThiQuay extends AppCompatActivity {
    Button button, btn1, btn2, btn3, btn4, btnkhac;
    String IPAddress, UserName, Password, url, TicketNumber, requireLabel,appType;
    Integer sizeTicket = 100, sizeButton = 10, sizeRequire =10, userId, number=0;
    JsonArrayRequest jsonArrayRequest;
    JsonObjectRequest jsonRequest;
    RequestQueue requestQueue;
    private Context mContext;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hien_thi_quay);

        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();

        intent = getIntent();
        SetAppConfig();

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        final RequestQueue mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        //region  lấy số thứ tự đang gọi
        final TextView lbNumber = (TextView) findViewById(R.id.lbNumber);
        lbNumber.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                intent = new Intent(HienThiQuay.this, AppConfigActivity.class);
                intent.putExtra("hold","1");
                startActivity(intent);
                return false;
            }
        });

        lbNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeTicket);
       Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DS-DIGIB.TTF");
          lbNumber.setTypeface(face);
        Thread thread = new Thread(){
            @Override
            public void run() {
                while (!isInterrupted()){
                    try {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                url = IPAddress + "/api/serviceapi/getnumber?username=" + UserName;
                                // requestQueue = Volley.newRequestQueue(MainActivity.this);
                                jsonRequest = new JsonObjectRequest(
                                        Request.Method.GET,
                                        url,
                                        null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                if (response != null) {
                                                    Integer num = response.optInt("Id");
                                                    lbNumber.setText(String.valueOf(num) );
                                                    number = num;
                                                } else {
                                                    lbNumber.setText("0000");
                                                    number = 0;
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                lbNumber.setText(  "ERR");
                                            }
                                        }
                                );
                                jsonRequest.setShouldCache(false);
                                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                mRequestQueue.add(jsonRequest);
                            }
                        });
                    } catch (InterruptedException e) {
                        Toast.makeText(HienThiQuay.this,e.getMessage(),Toast.LENGTH_LONG);
                        // e.printStackTrace();
                    }
                }
            }
        } ;
        thread.start();
        //endregion

    }
    private  void SetAppConfig(){
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(HienThiQuay.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            appType = sharedPreferences.getString("APP_TYPE", "1");
            Intent intent;
            switch (appType ){
                case "0":
                    intent = new Intent(HienThiQuay.this, ThreeButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(HienThiQuay.this, PrintTicketActivity.class);
                    startActivity(intent);
                    break;
                case "3":
                    intent = new Intent(HienThiQuay.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case "4":
                    intent = new Intent(HienThiQuay.this, CountersEventActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            UserName = sharedPreferences.getString("UserName", "0");
            Password = sharedPreferences.getString("Password", "0");
            sizeTicket =Integer.parseInt(sharedPreferences.getString("SizeChaoDG", "200")) ;
            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
        }
    }

}
