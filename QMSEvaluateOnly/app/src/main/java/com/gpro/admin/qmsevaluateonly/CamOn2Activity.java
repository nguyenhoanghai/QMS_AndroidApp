package com.gpro.admin.qmsevaluateonly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
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
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CamOn2Activity extends AppCompatActivity {

    TextView lbCauCamOn;
    CountDownTimer countDownTimer;
    private long time= 60000, countDown;
    String IPAddress, matb,url;
    JsonObjectRequest jsonRequest;
    Integer ticketNumber = 0,SendSMS = 0;
    Thread threadSTT = null,guiSMSThread = null;
    RequestQueue mRequestQueue = null;
    boolean isStop = false, stopThread = false;
    Integer useQMS = 0;
    BroadcastReceiver smsSentReceiver,smsDeliveredReceiver;
    private  final  String SENT ="SMS_SENT", DELIVERED ="SMS_DELIVERED";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_on2);

        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();

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
                    Intent intent = new Intent(CamOn2Activity.this, DanhGiaActivity.class);
                    startActivity(intent);
                }
            }.start();
        }
        else
        {
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            matb = sharedPreferences.getString("Equipcode", "0");

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
                            if(stopThread)
                                return;
                            try {
                                // Toast.makeText(CamOnActivity.this, "cam on get ", Toast.LENGTH_LONG).show();
                                url = IPAddress + "/api/serviceapi/GetAndroidInfo2?matb="+matb+"&&getSTT="+useQMS.intValue()+"&&getSMS="+SendSMS.intValue()+"&&getUserInfo=0"  ;
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
                                                        Intent intent = new Intent(CamOn2Activity.this, HienThiQuay2Activity.class);
                                                        startActivity(intent);
                                                        stopThread=true;
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
                    Toast.makeText(CamOn2Activity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        };
        guiSMSThread.start();
    }

    public void SendSMM(String phoneNumber, String smsContent) {
        try {
            // phoneNumber = "0773169414;0786399485";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage( phoneNumber, null, smsContent , null, null);
            Toast.makeText(CamOn2Activity.this, "SMS sent.", Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(CamOn2Activity.this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            //e.printStackTrace();
        }
    }
}