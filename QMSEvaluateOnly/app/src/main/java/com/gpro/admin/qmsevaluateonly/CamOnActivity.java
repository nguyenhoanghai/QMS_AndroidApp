package com.gpro.admin.qmsevaluateonly;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
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
import org.json.JSONException;
import org.json.JSONObject;

public class CamOnActivity extends AppCompatActivity {

    TextView lbCauCamOn;
    CountDownTimer countDownTimer;
   private long time= 60000, countDown;
    String IPAddress, UserName, Password,url;
    JsonObjectRequest jsonRequest;
Integer ticketNumber = 0,SendSMS = 0;
Thread threadSTT = null,guiSMSThread = null;
    RequestQueue mRequestQueue = null;
    boolean isStop = false;
    Integer useQMS = 0;
    BroadcastReceiver smsSentReceiver,smsDeliveredReceiver;
    private  final  String SENT ="SMS_SENT", DELIVERED ="SMS_DELIVERED";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();

        setContentView(R.layout.activity_cam_on);
        // Get the transferred data from source activity.
        Intent intent = getIntent();
        ticketNumber =  ((Integer)intent.getIntExtra("ticket_number",0)) ;
        SendSMS =  ((Integer)intent.getIntExtra("SendSMS",0)) ;

        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        lbCauCamOn =(TextView)findViewById(R.id.lbCauCamOn) ;
        lbCauCamOn.setText(sharedPreferences.getString("CamOn", "Xin cám ơn quý khách đã đánh giá"));
        lbCauCamOn.setTextSize(Float.parseFloat(sharedPreferences.getString("SizeCamOn", "200")));
        useQMS = Integer.parseInt(sharedPreferences.getString("UseQMS", "0"));
        if(sharedPreferences.getString("UseQMS", "0")=="0")
        {
            countDownTimer = new CountDownTimer(20000,60000) {
                @Override
                public void onTick(long millisUntilFinished) {  }

                @Override
                public void onFinish() {
                    Intent intent = new Intent(CamOnActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                }
            }.start();
        }
        else
        {
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            UserName = sharedPreferences.getString("UserName", "0");
            Password = sharedPreferences.getString("Password", "0");

            // Instantiate the cache
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());
            // Instantiate the RequestQueue with the cache and network.
            mRequestQueue  = new RequestQueue(cache, network);
            mRequestQueue.start();
           // LaySTT();
          // if( SendSMS==1 && isSimSupport(this))
         //  KiemTraGuiSMS();
            GetInfoNew();
        }
    }

    public void GetInfoNew( ) {
        guiSMSThread = new Thread() {
            @Override
            public void run() {
                while ( !isStop) try {
                    Thread.sleep(2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Toast.makeText(CamOnActivity.this, "cam on get ", Toast.LENGTH_LONG).show();
                                url = IPAddress + "/api/serviceapi/GetAndroidInfo?username="+UserName+"&&getSTT="+useQMS.intValue()+"&&getSMS="+SendSMS.intValue()+"&&getUserInfo=0"  ;
                                jsonRequest = new JsonObjectRequest(
                                        Request.Method.GET,
                                        url,
                                        null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                if (response != null) {
                                                    Integer num = response.optInt("TicketNumber");
                                                    Boolean hasEvalute = response.optBoolean("HasEvaluate");
                                                    if ((!num.equals(ticketNumber) && num.intValue() != 0) || (num.intValue()==-1 && !hasEvalute.booleanValue())) {
                                                        isStop = true;
                                                        mRequestQueue.stop();
                                                        mRequestQueue = null;
                                                        Intent intent = new Intent(CamOnActivity.this, DanhGiaActivity.class);
                                                        startActivity(intent);
                                                    }
                                                    // region Send SMS

                                                    try{
                                                        JSONArray jsonArrSMS = response.optJSONArray("SMS");
                                                        if (jsonArrSMS != null && jsonArrSMS.length() > 0) {
                                                            // Loop through the array elements
                                                            for(int i=0;i<jsonArrSMS.length();i++){
                                                                String string = jsonArrSMS.getString(i).toString();
                                                                String[] strArr = string.split(":");
                                                                SendSMM(strArr[0].trim(),strArr[1].trim());
                                                            }
                                                        }
                                                    }catch (JSONException e){
                                                      //  e.printStackTrace();
                                                    }
                                                    //endregion
                                                }

                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {

                                            }
                                        }
                                );
                                //endregion

                                jsonRequest.setShouldCache(false);
                                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                if(mRequestQueue != null)
                                    mRequestQueue.add(jsonRequest);
                            }catch (Exception e){

                            }
                        }
                    });
                } catch (InterruptedException e) {
                    Toast.makeText(CamOnActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        };
        guiSMSThread.start();
    }


    private void LaySTT(){
        try {
            //region lấy so stt dang goi
            url = IPAddress + "/api/serviceapi/getnumber?username=" + UserName;
            jsonRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (response != null) {
                                Integer num = ((Integer) response.optInt("Id"));
                                if (!num.equals(ticketNumber) && num != 0) {
                                    isStop = true;
                                       mRequestQueue.stop();
                                       mRequestQueue = null;
                                    Intent intent = new Intent(CamOnActivity.this, DanhGiaActivity.class);
                                    startActivity(intent);
                                } else {
                                    LaySTT();
                                }
                                if(num.intValue()==-1)
                                    ticketNumber = 0;
                            } else {

                                LaySTT();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            LaySTT();
                        }
                    }
            );
            jsonRequest.setShouldCache(false);
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(jsonRequest);
        }
        catch (Exception e){}
        //endregion

    }

    public  void KiemTraGuiSMS(){
        //region KiemTraGuiSMS
        guiSMSThread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted() && !isStop) try {
                    Thread.sleep(2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //region lấy yc gửi tin nhắn
                            url = IPAddress + "/api/serviceapi/GetRequireSendSMS";
                            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                                    new Response.Listener<JSONArray>(){
                                        @Override
                                        public void onResponse(JSONArray response) {
                                           // Toast.makeText(CamOnActivity.this, "SMS check camon.", Toast.LENGTH_LONG).show();
                                            if (response != null) {
                                                try{
                                                    // Loop through the array elements
                                                    for(int i=0;i<response.length();i++){
                                                        String string = response.getString(i).toString();
                                                        String[] strArr = string.split(":");
                                                       // Toast.makeText(CamOnActivity.this ,response.getString(i).toString(),Toast.LENGTH_LONG).show() ;
                                                        SendSMM(strArr[0].trim(),strArr[1].trim());

                                                    }

                                                }catch (JSONException e){
                                                  //  e.printStackTrace();
                                                }
                                            }
                                        }
                                    },new Response.ErrorListener(){
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //  lbNumber.setText(  "ERR");
                                }
                            });
                            //endregion
                            jsonRequest.setShouldCache(false);
                            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            if(mRequestQueue != null)
                                mRequestQueue.add(jsonArrayRequest);

                        }
                    });
                } catch (InterruptedException e) {
                    Toast.makeText(CamOnActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        };
        //endregion
        if(SendSMS.intValue()==1) {
            guiSMSThread.start();
        }
    }

    public void SendSMM(String phoneNumber, String smsContent) {
        try {
            // phoneNumber = "0773169414;0786399485";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage( phoneNumber, null, smsContent , null, null);
            Toast.makeText(CamOnActivity.this, "SMS sent.", Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(CamOnActivity.this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            //e.printStackTrace();
        }
    }

    public  boolean isSimSupport(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
      //  Toast.makeText(CamOnActivity.this,( (tm.getSimState())),Toast.LENGTH_LONG).show();
        return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);

    }

    @Override
    protected  void  onPause(){
        super.onPause();
        unregisterReceiver(smsSentReceiver);
        unregisterReceiver(smsDeliveredReceiver);
    }
    @Override
    protected  void  onResume() {
        super.onResume();
        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch ( getResultCode())
                {
                    case Activity.RESULT_OK :
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE :
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU :
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF :
                        break;
                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                switch ( getResultCode())
                {
                    case Activity.RESULT_OK :
                        break;
                    case Activity.RESULT_CANCELED :
                        break;
                }
            }
        };
        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = getIntent();
           String appType =  (intent.getStringExtra("AppType")).toString() ;
            switch (appType ){
                case "0":
                    intent = new Intent(CamOnActivity.this, ThreeButtonActivity.class);
                    startActivity(intent);
                    break;
                case "1":
                    intent = new Intent(CamOnActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(CamOnActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case "3":
                    intent = new Intent(CamOnActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
            }
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
