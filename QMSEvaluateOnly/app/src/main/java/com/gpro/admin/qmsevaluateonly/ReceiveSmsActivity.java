package com.gpro.admin.qmsevaluateonly;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.EditText;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReceiveSmsActivity extends AppCompatActivity implements MessageListener {
    ProgressDialog progressDialog;
    RequestQueue mRequestQueue;
    String IPAddress, Mathietbi, appType, userName;
    Integer useQMS = 0, sendSMS = 0, count = 0;
    JsonObjectRequest jsonRequest;
    boolean processing = false;
    private final int send_request_code = 1;
    private final String SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";
    ArrayList<SmsMessage> smsRequires = new ArrayList<SmsMessage>();

    TextView lbtotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_sms);
        setTitle("Nhận tin nhắn lấy số");

        //dùng cho nhận sms
        AutoStart.bindListener(ReceiveSmsActivity.this);

        try {
            boolean gotPermission = false;
            if (CheckPermission(Manifest.permission.SEND_SMS)) {
                gotPermission = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS}, send_request_code);
            }
        } catch (Exception e) {
        }

        progressDialog = new ProgressDialog(ReceiveSmsActivity.this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Đang tải dữ liệu...");
        progressDialog.show();

        // Instantiate the cache
        final Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        final Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        lbtotal = (TextView) findViewById(R.id.lbTotal);
        lbtotal.setText("0");

        lbtotal.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mRequestQueue.stop();
                Intent intent = new Intent(ReceiveSmsActivity.this, AppConfigActivity.class);
                intent.putExtra("hold","1");
                startActivity(intent);
                return false;
            }
        });
        GetAppConfig();
    }

    public boolean CheckPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private void GetAppConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(ReceiveSmsActivity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            switch (appType) {
                case "1":
                    intent = new Intent(ReceiveSmsActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(ReceiveSmsActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case "3":
                    intent = new Intent(ReceiveSmsActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case "4":
                    intent = new Intent(ReceiveSmsActivity.this, CountersEventActivity.class);
                    startActivity(intent);
                    break;
                case "5":
                    intent = new Intent(ReceiveSmsActivity.this, PrintTicket_2Activity.class);
                    startActivity(intent);
                    break;
                case "6":
                    intent = new Intent(ReceiveSmsActivity.this, PrintTicket_3Activity.class);
                    startActivity(intent);
                    break;
                case "7":
                    intent = new Intent(ReceiveSmsActivity.this, HienThiQuay.class);
                    startActivity(intent);
                    break;
                case "8":
                    intent = new Intent(ReceiveSmsActivity.this, PrintTicket_4Activity.class);
                    startActivity(intent);
                    break;
                case "9":
                    intent = new Intent(ReceiveSmsActivity.this, CounterSoftActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            Mathietbi = sharedPreferences.getString("Equipcode", "0");
            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));

            //lbTitle.setText(sharedPreferences.getString("ChaoDG", "Xin vui lòng đánh giá"));
            //lbTitle.setTextSize(Float.parseFloat(sharedPreferences.getString("SizeChaoDG", "200")));
            useQMS = Integer.parseInt(sharedPreferences.getString("UseQMS", "0"));
            sendSMS = Integer.parseInt(sharedPreferences.getString("SendSMS", "0"));
            userName = sharedPreferences.getString("UserName", "0");

            //lbChuChay.setText(sharedPreferences.getString("Slogan", "Slogan here"));
            //lbChuChay.setTextSize(Float.parseFloat(sharedPreferences.getString("SizeSlogan", "200")));
        }
        progressDialog.hide();
    }

    private void SendRequest() {
        if (smsRequires != null && smsRequires.size() > 0 && processing == false) {
            processing = true;
            SmsMessage smsMessage = smsRequires.get(0);
            smsRequires.remove(0);
            String str = (IPAddress + "/api/serviceapi/PrintTicketFromSMS?smsContent=" + smsMessage.getMessageBody() + "&sdt=" + smsMessage.getOriginatingAddress());
            RequestQueue rqQue = Volley.newRequestQueue(ReceiveSmsActivity.this);
            //region
            JsonObjectRequest jRequest = new JsonObjectRequest(
                    Request.Method.GET, str, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Integer number = 0;
                            Boolean rs = response.optBoolean("IsSuccess");
                            String phoneNumber = response.optString("Data_1");
                            String smsContent = "";
                            if (rs) {
                                try {
                                    number = response.getInt("Data");
                                    smsContent = "STT của quý khách là: " + (number + 1) + "\nCảm ơn Quý Khách đã sử dụng dịch vụ.";
                                    Toast.makeText(ReceiveSmsActivity.this, smsContent, Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    progressDialog.hide();
                                    Toast.makeText(ReceiveSmsActivity.this, "Parse STT: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                JSONArray objs = response.optJSONArray("Errors");
                                try {
                                    JSONObject obj = objs.getJSONObject(0);
                                    smsContent = obj.getString("Message");
                                    Toast.makeText(ReceiveSmsActivity.this, smsContent, Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                            //region send SMS

                            try {
                                // phoneNumber = "0773169414;0786399485";
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(phoneNumber, null, smsContent, null, null);
                                Toast.makeText(ReceiveSmsActivity.this, "SMS sent.", Toast.LENGTH_LONG).show();
                                Thread.sleep(2000);
                            } catch (Exception e) {
                                Toast.makeText(ReceiveSmsActivity.this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                                //  e.printStackTrace();
                            }
                            //endregion
/*
                        lbCurrentNumber.setText(number.toString());
                        if (number.toString() == "0") {
                            lbTotal.setText("0");
                            lbWaitting.setText("---");
                        }
                        // progressDialog.hide();
                        isStop = false;
                        GetInfoNew(false);
                          */
                            count++;
                            lbtotal.setText(count.toString());
                            processing = false;
                            if (smsRequires.size() > 0)
                                SendRequest();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.hide();
                            Toast.makeText(ReceiveSmsActivity.this, "Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            //endregion
            jRequest.setShouldCache(false);
            jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(jRequest);
        }
    }

    //TODO hàm nhận tin nhắn
    @Override
    public void messageReceived(SmsMessage smsMessage) {
        String txt = "From: " + smsMessage.getOriginatingAddress();
        txt += " | Message:" + smsMessage.getMessageBody();
        Toast.makeText(this, "ReceiveSms Activity: " + txt, Toast.LENGTH_LONG).show();
        String sms = smsMessage.getMessageBody();
        String[] _arrContent = sms.split("-");
        if(_arrContent != null && _arrContent.length>0 && _arrContent[0]=="LAYSOHANGDOI")

        smsRequires.add(smsMessage);
        if (processing == false)
            SendRequest();
    }
}