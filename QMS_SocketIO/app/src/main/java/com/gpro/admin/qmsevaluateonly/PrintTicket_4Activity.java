package com.gpro.admin.qmsevaluateonly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PrintTicket_4Activity extends AppCompatActivity {
    ProgressDialog progressDialog;
    private static int
            NUM_ROW = 6 ,
            NUM_COL = 5 ,
            FONT_SIZE=12,
            FONT_SIZE_STT=12,
            BUT_WIDTH = 20,
            BUT_HEIGHT=20 ;

    Integer number = 0;
    String IPAddress;
    private  static ArrayList<Integer> requestArr ;
    static Button[] buttonArr;
    JsonArrayRequest jsonArrayRequest;
    boolean isStop = false;
    JSONObject jsonObject = null;
    JSONArray services = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_print_ticket_4);

        progressDialog = new ProgressDialog(PrintTicket_4Activity.this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Đang tải dữ liệu...");
        progressDialog.show();


        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        final RequestQueue mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        GetAppConfig();
        GetServices(mRequestQueue);
    }

    private void GetAppConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(PrintTicket_4Activity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            String appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            switch (appType) {
                case "1":
                    intent = new Intent(PrintTicket_4Activity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "0":
                    intent = new Intent(PrintTicket_4Activity.this, ThreeButtonActivity.class);
                    startActivity(intent);
                    break;
                case "9":
                    intent = new Intent(PrintTicket_4Activity.this, CounterSoftActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            NUM_COL = (Integer.parseInt(sharedPreferences.getString("Cot", "5")));
            NUM_ROW = (Integer.parseInt(sharedPreferences.getString("Dong", "5")));
            FONT_SIZE = (Integer.parseInt(sharedPreferences.getString("SizeNext", "12")));
            FONT_SIZE_STT = (Integer.parseInt(sharedPreferences.getString("SizeSTTNext", "12")));
            BUT_HEIGHT = (Integer.parseInt(sharedPreferences.getString("ButHeight", "12")));
            BUT_WIDTH = (Integer.parseInt(sharedPreferences.getString("ButWidth", "12")));
        }
    }

    private void GetServices(RequestQueue mRequestQueue) {
        progressDialog.show();
        buttonArr = new Button[(NUM_ROW * NUM_COL)];
        String str = (IPAddress + "/api/serviceapi/getservices");
        RequestQueue rqQue = Volley.newRequestQueue(PrintTicket_4Activity.this);
        JsonArrayRequest jRequest = new JsonArrayRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null && response.length() > 0) {
                            progressDialog.hide();
                            services = response;
                            /*for (int i = 0 ; i < response.length() ; i++){
                                jsonObject = null;
                                try {
                                    jsonObject = response.getJSONObject(i);
                                    services.add(new ServiceModel(jsonObject.optString("Name")  , jsonObject.optString("Code"), jsonObject.optInt("Id")));
                                } catch (JSONException e) {
                                    // e.printStackTrace();
                                }
                            }*/
                            InitListView();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(PrintTicket_4Activity.this, "Lấy Dịch vụ : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jRequest.setShouldCache(false);
        jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // rqQue.add(jRequest);
        mRequestQueue.add(jRequest);
    }

    private  void  InitListView(){
        buttonArr = new Button[services.length()];
        LinearLayout root = (LinearLayout)findViewById(R.id.rootLayout);
        JSONObject object = null;
        int count = 0;
        if(services != null && services.length() > 0){
            for (int i=0;i< NUM_ROW;i++) {
                LinearLayout row = new LinearLayout(PrintTicket_4Activity.this);
                row.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                ));
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER);

                for (int ii=0;ii< NUM_COL;ii++) {
                    object = null;
                    try {
                        object = services.getJSONObject(count);
                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }

                    if (object != null) {
                        buttonArr[count] = new Button(PrintTicket_4Activity.this);
                        buttonArr[count].setLayoutParams(new TableRow.LayoutParams(
                                BUT_WIDTH,
                                BUT_HEIGHT,
                                1.0f
                        ));
                       // buttonArr[count].setBackgroundColor(Color.BLACK);
                        buttonArr[count].setTextColor(Color.RED);
                        buttonArr[count].setTextSize(FONT_SIZE);
                        buttonArr[count].setPadding(8,8,8,8);
                         buttonArr[count].setText(object.optString("Name").toString() );
                        final JSONObject finalObject = object;
                        buttonArr[count].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Button_Click(finalObject.optInt("Id",0), finalObject.optString("Code"));
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

    public void  Button_Click(int serviceId, String thoigian){
        progressDialog.show();
        //region
        String str = (IPAddress + "/api/serviceapi/PrintNewTicket?MaPhongKham=" + serviceId+"&thoigian="+thoigian  );
        RequestQueue rqQue = Volley.newRequestQueue(PrintTicket_4Activity.this);
        JsonObjectRequest jRequest = new JsonObjectRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.hide();
                        Boolean rs = response.optBoolean("IsSuccess");
                        if (rs)
                            Toast.makeText(PrintTicket_4Activity.this, "Gửi yêu cầu cấp phiếu thành công.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(PrintTicket_4Activity.this, "Gửi yêu cầu cấp phiếu thất bại.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(PrintTicket_4Activity.this, "Gửi YC cấp phiếu : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                    }
                }
        );
        rqQue.add(jRequest);
        //endregion
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isStop = true;
            Intent intent = new Intent(PrintTicket_4Activity.this, AppConfigActivity.class);
            intent.putExtra("hold","hold");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
