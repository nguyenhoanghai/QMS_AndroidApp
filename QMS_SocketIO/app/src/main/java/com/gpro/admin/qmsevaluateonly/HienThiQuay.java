package com.gpro.admin.qmsevaluateonly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HienThiQuay extends AppCompatActivity {
    Button button, btn1, btn2, btn3, btn4, btnkhac;
    TextView lbTieuDeDG, lbThanks, lbNumber;
    String IPAddress, UserName, Password, url, TicketNumber, requireLabel, appType;
    Integer sizeTicket = 100, sizeButton = 10, sizeRequire = 10, userId, number = 0, evaluating = 0, useQMS = 0;
    JsonArrayRequest jsonArrayRequest;
    JsonObjectRequest jsonRequest;
    RequestQueue requestQueue;
    private Context mContext;
    Intent intent;

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
    LinearLayout viewDG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hien_thi_quay);

        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(HienThiQuay.this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Đang tải dữ liệu...");
        progressDialog.show();

        intent = getIntent();
        viewDG = (LinearLayout) findViewById(R.id.rootLayout);
        lbNumber = (TextView) findViewById(R.id.lbNumber);
        SetAppConfig();

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        final RequestQueue mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        //region  lấy số thứ tự đang gọi
        lbNumber.setTextSize(sizeTicket);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DS-DIGIB.TTF");
        lbNumber.setTypeface(face);

        lbNumber.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                intent = new Intent(HienThiQuay.this, AppConfigActivity.class);
                intent.putExtra("hold", "1");
                startActivity(intent);
                return false;
            }
        });


        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
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
                                                    lbNumber.setText(String.valueOf(num));
                                                    number = num;

                                                    Integer status = response.optInt("Data");
                                                    if (status == 1) {
                                                        viewDG.setVisibility(View.VISIBLE);
                                                        lbThanks.setVisibility(View.GONE);
                                                        lbNumber.setVisibility(View.GONE);
                                                    } else {
                                                        if (number == 0 && evaluating == 1) {
                                                            viewDG.setVisibility(View.GONE);
                                                            lbThanks.setVisibility(View.VISIBLE);
                                                            lbNumber.setVisibility(View.GONE);
                                                        } else {
                                                            viewDG.setVisibility(View.GONE);
                                                            lbThanks.setVisibility(View.GONE);
                                                            lbNumber.setVisibility(View.VISIBLE);
                                                            evaluating = 0;
                                                        }
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
                                                lbNumber.setText("ERR");
                                            }
                                        }
                                );
                                jsonRequest.setShouldCache(false);
                                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                mRequestQueue.add(jsonRequest);
                            }
                        });
                    } catch (InterruptedException e) {
                        Toast.makeText(HienThiQuay.this, e.getMessage(), Toast.LENGTH_LONG);
                        // e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        //endregion

        GetEvaluates(mRequestQueue);
    }

    private void SetAppConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(HienThiQuay.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            appType = sharedPreferences.getString("APP_TYPE", "1");
            Intent intent;
            switch (appType) {
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

            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));

            NUM_COL = (Integer.parseInt(sharedPreferences.getString("Cot", "5")));
            NUM_ROW = (Integer.parseInt(sharedPreferences.getString("Dong", "5")));
            FONT_SIZE = (Integer.parseInt(sharedPreferences.getString("SizeNext", "12")));
            FONT_SIZE_STT = (Integer.parseInt(sharedPreferences.getString("SizeSTTNext", "12")));
            BUT_HEIGHT = (Integer.parseInt(sharedPreferences.getString("ButHeight", "12")));
            BUT_WIDTH = (Integer.parseInt(sharedPreferences.getString("ButWidth", "12")));
            useQMS = Integer.parseInt(sharedPreferences.getString("UseQMS", "0"));
            But_TextColor = Integer.parseInt(sharedPreferences.getString("ButTextColor", "-15859455"));
            But_BackgroundColor = Integer.parseInt(sharedPreferences.getString("ButBackColor", "-15859455"));

            sizeTicket = (Integer.parseInt(sharedPreferences.getString("SizeSTT", "200")));
            try {
                lbNumber.setTextColor(Integer.parseInt(sharedPreferences.getString("ColorSTT", "-15859455")));
            } catch (Exception ex) {
            }

            lbTieuDeDG = (TextView) findViewById(R.id.lbTieuDeDG);
            try {
                lbTieuDeDG.setTextColor(Integer.parseInt(sharedPreferences.getString("DGTitle_TextColor", "-15859455")));
            } catch (Exception ex) {
            }
            //lbTieuDeDG.setTextColor(Color.RED);
            lbTieuDeDG.setText(sharedPreferences.getString("ChaoDG", "Vui lòng đánh giá chất lượng giao dịch"));
            lbTieuDeDG.setTextSize(Integer.parseInt(sharedPreferences.getString("SizeChaoDG", "100")));

            lbThanks = (TextView) findViewById(R.id.lbThanks);
            try {
                lbThanks.setTextColor(Integer.parseInt(sharedPreferences.getString("ThanksTextColor", "-15859455")));
            } catch (Exception ex) {
            }
            //lbThanks.setTextColor(Color.RED);
            lbThanks.setText(sharedPreferences.getString("CamOn", "Xin cảm ơn quý khách."));
            lbThanks.setTextSize(Integer.parseInt(sharedPreferences.getString("SizeCamOn", "100")));
        }
    }

    private void GetEvaluates(RequestQueue mRequestQueue) {
        progressDialog.show();
        // buttonArr = new Button[(NUM_ROW * NUM_COL)];
        String str = (IPAddress + "/api/serviceapi/GetEvaluates");
        RequestQueue rqQue = Volley.newRequestQueue(HienThiQuay.this);
        JsonArrayRequest jRequest = new JsonArrayRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null && response.length() > 0) {
                            progressDialog.hide();
                            evaluates = response;
                            InitDanhGiaView();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(HienThiQuay.this, "Lấy tiêu chí đánh giá: Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jRequest.setShouldCache(false);
        jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // rqQue.add(jRequest);
        mRequestQueue.add(jRequest);
    }

    private void InitDanhGiaView() {
        buttonArr = new Button[evaluates.length()];
        LinearLayout root = (LinearLayout) findViewById(R.id.rootDG);
        JSONObject object = null;
        int count = 0;
        if (evaluates != null && evaluates.length() > 0) {
            for (int i = 0; i < NUM_ROW; i++) {
                LinearLayout row = new LinearLayout(HienThiQuay.this);
                row.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                ));
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER);

                for (int ii = 0; ii < NUM_COL; ii++) {
                    object = null;
                    try {
                        object = evaluates.getJSONObject(count);
                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }

                    if (object != null) {
                        buttonArr[count] = new Button(HienThiQuay.this);
                        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                                BUT_WIDTH,
                                BUT_HEIGHT,
                                1.0f
                        );
                        layoutParams.setMargins(5, 5, 5, 5);
                        buttonArr[count].setLayoutParams(layoutParams);

                        try {
                            buttonArr[count].setBackgroundColor(But_BackgroundColor);
                            buttonArr[count].setTextColor(But_TextColor);
                        } catch (Exception ex) {
                        }

                        buttonArr[count].setTextSize(FONT_SIZE);
                        buttonArr[count].setPadding(8, 8, 8, 8);
                        buttonArr[count].setText(object.optString("Name").toString());
                        final JSONObject finalObject = object;
                        buttonArr[count].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String value = finalObject.optInt("Id", 0) + "_" + finalObject.optInt("Data", 0);
                                Button_Click(value);
                            }
                        });
                        row.addView(buttonArr[count]);
                    }
                    count++;
                }
                root.addView(row);
            }
        }
    }

    public void Button_Click(String value) {
        progressDialog.show();
        //region
        if (useQMS == 0 || (useQMS == 1 && number.intValue() > 0)) {
            String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&value=" + value + "&num=" + number + "&isUseQMS=" + useQMS + "&comment=");
            RequestQueue rqQue = Volley.newRequestQueue(HienThiQuay.this);
            JsonObjectRequest jRequest = new JsonObjectRequest(
                    Request.Method.GET, str, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.hide();
                            Boolean rs = response.optBoolean("IsSuccess");
                            if (rs) {
                                evaluating = 1;
                                viewDG.setVisibility(View.GONE);
                                lbThanks.setVisibility(View.VISIBLE);
                                lbNumber.setVisibility(View.GONE);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.hide();
                            Toast.makeText(HienThiQuay.this, "Đánh giá : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            rqQue.add(jRequest);
        }
        //endregion
    }

}
