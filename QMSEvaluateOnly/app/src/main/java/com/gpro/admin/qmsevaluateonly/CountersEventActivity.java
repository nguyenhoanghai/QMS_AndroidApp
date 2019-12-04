package com.gpro.admin.qmsevaluateonly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CountersEventActivity extends AppCompatActivity {

    private static int
            NUM_ROW = 6 ,
            NUM_COL = 5 ,
            FONT_SIZE=12,
            FONT_SIZE_STT=12 ;
    Integer number = 0;
    private  static ArrayList<Integer> requestArr,
            waitResponseButtons;

    RequestQueue mRequestQueue;
    String IPAddress="" ,
            appType="",
            urlPath  ="",
    hexCode ="",
    actionParam="",
            text1="",
            text2="";
    static Button [] buttonArr;
    JsonArrayRequest jsonArrayRequest;
    boolean isStop = false;
    SpannableString spannableString;
    JSONObject jsonObject = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();

        setContentView(R.layout.activity_counters_event);
        requestArr = new ArrayList<Integer>();
        waitResponseButtons = new ArrayList<Integer>();
        // Instantiate the cache
        final Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        final Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
        SetAppConfig();
        GenerateButton();
    }

    public void GridButton_Click(final String equipCode){
        boolean isWait = false;
        for(int i = 0; i < waitResponseButtons.size();i++ ){
            if(Integer.parseInt(equipCode) == waitResponseButtons.get(i)){
                isWait = true;
            }
        }
        if(isWait == true){
            Toast.makeText(CountersEventActivity.this,(""+equipCode+ " đã được nhấn và đang được xử lý vui lòng chờ."),Toast.LENGTH_LONG).show();
        }
        else {
            //Toast.makeText(this,(""+equipCode+ " - 8B"),Toast.LENGTH_SHORT).show();
            waitResponseButtons.add(Integer.parseInt(equipCode));
            String str = (IPAddress + "/api/serviceapi/CounterEvent?counterId=" + equipCode + "&action="+hexCode+"&param="+actionParam);
            RequestQueue rqQue = Volley.newRequestQueue(CountersEventActivity.this);
            JsonObjectRequest jRequest = new JsonObjectRequest(
                    Request.Method.GET, str, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            int index = -1;
                            for(int i = 0; i < waitResponseButtons.size();i++ ){
                                if(Integer.parseInt(equipCode) == waitResponseButtons.get(i)){
                                    index = i;
                                     }
                            }
                            if(index >= 0){
                                waitResponseButtons.remove(index);
                            }

                            Boolean rs = response.optBoolean("IsSuccess");
                            if (rs) {

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            int index = -1;
                            for(int i = 0; i < waitResponseButtons.size();i++ ){
                                if(Integer.parseInt(equipCode) == waitResponseButtons.get(i)){
                                    index = i;
                                }
                            }
                            if(index >= 0){
                                waitResponseButtons.remove(index);
                            }
                            Toast.makeText(CountersEventActivity.this, "Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            jRequest.setShouldCache(false);
            jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // rqQue.add(jRequest);
            mRequestQueue.add(jRequest);
        }
    }

    public void GenerateButton(){
        buttonArr = new Button[(NUM_ROW * NUM_COL)];
        //region tao buttons
          urlPath = (IPAddress + "/api/serviceapi/getequipments"  );
          jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlPath, null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null && response.length() > 0) {
                            int index = 0;
                            TableLayout tb = (TableLayout)findViewById(R.id.tb);
                            for (int i=0;i< NUM_ROW;i++){
                                TableRow tableRow = new TableRow(CountersEventActivity.this);
                                tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                        TableLayout.LayoutParams.MATCH_PARENT,
                                        TableLayout.LayoutParams.MATCH_PARENT,
                                        1.0f
                                ));
                                tb.addView(tableRow);
                                for (int ii=0;ii<NUM_COL;ii++){
                                    jsonObject = null;
                                    try {
                                        jsonObject = response.getJSONObject(index);
                                    } catch (JSONException e) {
                                       // e.printStackTrace();
                                    }
                                    if(jsonObject != null){
                                        final int row = i, col = ii;
                                        final String equipCode = jsonObject.optString("Code");
                                         number = jsonObject.optInt("Data");
                                        requestArr.add(Integer.parseInt(equipCode));
                                          buttonArr[index] = new Button(CountersEventActivity.this);
                                        buttonArr[index].setLayoutParams(new TableRow.LayoutParams(
                                                TableRow.LayoutParams.MATCH_PARENT,
                                                TableRow.LayoutParams.MATCH_PARENT,
                                                1.0f
                                        ));
                                        buttonArr[index].setTextSize(FONT_SIZE);
                                        buttonArr[index].setPadding(0,-30,0,0);
                                        text1 = jsonObject.optString("Name");
                                        text2 =  (number.intValue()+"");
                                        spannableString =new SpannableString(text1 + "\n" + text2);
                                      //  spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, text1.length(), 0);
                                      spannableString.setSpan(new AbsoluteSizeSpan(FONT_SIZE), 0, text1.length(), 0);
                                        spannableString.setSpan(new ForegroundColorSpan(Color.RED), text1.length(), spannableString.length(), 0);
                                        spannableString.setSpan(new AbsoluteSizeSpan(FONT_SIZE_STT), text1.length(), spannableString.length(), 0);

                                        buttonArr[index].setText(spannableString);
                                        buttonArr[index].setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                GridButton_Click(equipCode);
                                            }
                                        });
                                        tableRow.addView(buttonArr[index]);
                                        index++;
                                    }
                                }
                            }
                            ReloadTicketInfo();
                        }
                    }
                },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                //  lbNumber.setText(  "ERR");
            }
        });

        if(mRequestQueue != null)
            mRequestQueue.add(jsonArrayRequest);
        //endregion
    }

    private void ReloadTicketInfo(){
        //region lấy so stt dang goi
    Thread thread = new Thread() {
        @Override
        public void run() {
            while (!isInterrupted()&& !isStop) {
                try {
                    Thread.sleep(2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //region tao buttons
                            urlPath = (IPAddress + "/api/serviceapi/getequipments"  );
                            jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlPath, null,
                                    new Response.Listener<JSONArray>(){
                                        @Override
                                        public void onResponse(JSONArray response) {
                                            //region response
                                            if (response != null && response.length() > 0) {
                                                for (int i=0;i< buttonArr.length;i++) {
                                                    jsonObject = null;
                                                    try {
                                                        jsonObject = response.getJSONObject(i);
                                                    } catch (JSONException e) {
                                                      //  e.printStackTrace();
                                                    }
                                                    if(jsonObject != null){
                                                         number = jsonObject.optInt("Data");
                                                         text1 = jsonObject.optString("Name");
                                                         text2 =  (number.intValue()+"");
                                                         spannableString =new SpannableString(text1 + "\n" + text2);
                                                         spannableString.setSpan(new AbsoluteSizeSpan(FONT_SIZE), 0, text1.length(), 0);
                                                        spannableString.setSpan(new ForegroundColorSpan(Color.RED), text1.length(), spannableString.length(), 0);
                                                        spannableString.setSpan(new AbsoluteSizeSpan(FONT_SIZE_STT), text1.length(), spannableString.length(), 0);
                                                        buttonArr[i].setText(spannableString);
                                                    }
                                                }
                                            }
                                            //endregion
                                        }
                                    },new Response.ErrorListener(){
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            });

                            if(mRequestQueue != null) {
                                jsonArrayRequest.setShouldCache(false);
                                jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                mRequestQueue.add(jsonArrayRequest);
                            }
                            //endregion
                        }
                    });
                } catch (InterruptedException e) {
                    Toast.makeText(CountersEventActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }
    };
    thread.start();
    //endregion
    }

    private  void SetAppConfig(){
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(CountersEventActivity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            switch (appType ){
                case "1":
                    intent = new Intent(CountersEventActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(CountersEventActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case "3":
                    intent = new Intent(CountersEventActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case "7":
                    intent = new Intent(CountersEventActivity.this, HienThiQuay.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            NUM_COL = (Integer.parseInt(sharedPreferences.getString("Cot", "5")));
            NUM_ROW = (Integer.parseInt(sharedPreferences.getString("Dong", "5")));
            FONT_SIZE = (Integer.parseInt(sharedPreferences.getString("SizeNext", "12")));
            FONT_SIZE_STT = (Integer.parseInt(sharedPreferences.getString("SizeSTTNext", "12")));
            hexCode =  sharedPreferences.getString("HexCode", "8B") ;
            actionParam =  sharedPreferences.getString("ActionParam", "00,00") ;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isStop = true;
            Intent intent = new Intent(CountersEventActivity.this, AppConfigActivity.class);
            intent.putExtra("hold","1");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}
