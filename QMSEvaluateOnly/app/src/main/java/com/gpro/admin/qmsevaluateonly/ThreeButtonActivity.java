package com.gpro.admin.qmsevaluateonly;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ThreeButtonActivity extends AppCompatActivity implements MessageListener {

     String IPAddress, UserName, Password, url,  requireLabel,appType;
    Button  btn1, btn2, btn3 ;
    TextView lbChuChay,lbTitle,lbTenNV;
    ImageView imgAvatar;
    Integer useQMS=0, number =0, sendSMS=0;
    JsonObjectRequest jsonRequest;
  private   final  int send_request_code = 1;
  private  final  String SENT ="SMS_SENT", DELIVERED ="SMS_DELIVERED";
  PendingIntent sendPI, deliveredPI;
    BroadcastReceiver smsSentReceiver,smsDeliveredReceiver;
    public  RequestQueue mRequestQueue= null;
    Thread guiSMSThread = null, threadSTT = null, threadLayTTNV;
    boolean isStop =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide thanh action bar to fullscreen
         getSupportActionBar().hide();

        setContentView(R.layout.activity_three_button);

        AutoStart.bindListener(ThreeButtonActivity.this);

        // Instantiate the cache
        final Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        final Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
         mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        lbTitle = (TextView)findViewById(R.id.lbTitle);
        lbTenNV = (TextView)findViewById(R.id.lbTenNV);
        lbChuChay = (TextView)findViewById(R.id.lbChuChay);
        lbChuChay.setSelected(true);
        imgAvatar  = findViewById(R.id.imAvatar);
        imgAvatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mRequestQueue.stop();
                Intent intent = new Intent(ThreeButtonActivity.this, AppConfigActivity.class);
                intent.putExtra("hold","1");
                startActivity(intent);
                return false;
            }
        });
        SetAppConfig();

       // LayThongTinNV();

        //region init button 1
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setTag(1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //region
                if (useQMS == 0 || (useQMS == 1 && number.intValue() != 0)) {
                    try {
                        String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_1&&num=" + number + "&&isUseQMS=" + useQMS);
                        RequestQueue rqQue = Volley.newRequestQueue(ThreeButtonActivity.this);
                        JsonObjectRequest jRequest = new JsonObjectRequest(
                                Request.Method.GET, str, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Boolean rs = response.optBoolean("IsSuccess");
                                        if (rs) {
                                            mRequestQueue.stop();
                                            mRequestQueue = null;
                                            isStop = true;
                                            Intent intent = new Intent(ThreeButtonActivity.this, CamOnActivity.class);
                                            intent.putExtra("ticket_number", number);
                                            intent.putExtra("SendSMS", sendSMS);
                                            intent.putExtra("AppType", appType);
                                            startActivity(intent);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(ThreeButtonActivity.this, "Đánh giá : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                        jRequest.setShouldCache(false);
                        jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        if(mRequestQueue == null){
                            mRequestQueue = new RequestQueue(cache, network);
                            mRequestQueue.start();
                        }
                        mRequestQueue.add(jRequest);
                    }
                    catch (Exception e){}
                    //endregion
                }  else
                    Toast.makeText(ThreeButtonActivity.this, "Hiện tại đang không có giao dịch nên không thể đánh giá được.", Toast.LENGTH_SHORT).show();

            }
        });
        //endregion

        //region init button 2
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setTag(2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useQMS == 0 || (useQMS == 1 && number.intValue() != 0)) {
                    //region
                    String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_2&&num=" + number + "&&isUseQMS=" + useQMS);
                    RequestQueue rqQue = Volley.newRequestQueue(ThreeButtonActivity.this);
                    JsonObjectRequest jRequest = new JsonObjectRequest(
                            Request.Method.GET, str, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Boolean rs = response.optBoolean("IsSuccess");
                                    if (rs) {
                                        mRequestQueue.stop();
                                        mRequestQueue = null;
                                        isStop = true;
                                        Intent intent = new Intent(ThreeButtonActivity.this, CamOnActivity.class);
                                        intent.putExtra("ticket_number",number);
                                        intent.putExtra("SendSMS",sendSMS);
                                        intent.putExtra("AppType", appType);
                                        startActivity(intent);
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(ThreeButtonActivity.this, "Đánh giá : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    jRequest.setShouldCache(false);
                    jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    if(mRequestQueue == null){
                        mRequestQueue = new RequestQueue(cache, network);
                        mRequestQueue.start();
                    }
                    mRequestQueue.add(jRequest);
                    //endregion
                }  else
                    Toast.makeText(ThreeButtonActivity.this, "Hiện tại đang không có giao dịch nên không thể đánh giá được.", Toast.LENGTH_SHORT).show();

            }
        });
        //endregion

        //region init button 3
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setTag(3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useQMS == 0 || (useQMS == 1 && number.intValue() != 0)) {
                //region
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_3&&num=" + number + "&&isUseQMS=" + useQMS);
                RequestQueue rqQue = Volley.newRequestQueue(ThreeButtonActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs) {
                                    mRequestQueue.stop();
                                    mRequestQueue = null;
                                    isStop = true;
                                    Intent intent = new Intent(ThreeButtonActivity.this, CamOnActivity.class);
                                    intent.putExtra("ticket_number",number);
                                    intent.putExtra("SendSMS",sendSMS);
                                    intent.putExtra("AppType", appType);
                                    startActivity(intent);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(ThreeButtonActivity.this, "Đánh giá : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jRequest.setShouldCache(false);
                jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    if(mRequestQueue == null){
                        mRequestQueue = new RequestQueue(cache, network);
                        mRequestQueue.start();
                    }
                mRequestQueue.add(jRequest);
                //endregion
            }
                else
                    Toast.makeText(ThreeButtonActivity.this, "Hiện tại đang không có giao dịch nên không thể đánh giá được.", Toast.LENGTH_SHORT).show();
        }
        });
        //endregion

        new LoadImageInternet().execute(IPAddress+"/Content/logo.png");

        sendPI = PendingIntent.getBroadcast(this,0 ,new Intent(SENT),0);
        deliveredPI = PendingIntent.getBroadcast(this,0 ,new Intent(DELIVERED),0);

        try{
            boolean gotPermission = false;
            if(CheckPermission(Manifest.permission.SEND_SMS))
            {
                gotPermission = true;
            }
            else
            {
                ActivityCompat.requestPermissions(this,
                        new String []{Manifest.permission.SEND_SMS} ,send_request_code);
            }
        }catch (Exception e){}

     //  LaySTT();

      //  if( sendSMS ==1 && isSimSupport(this))
      //  KiemTraGuiSMS();

       // if(sendSMS.intValue() ==1 && isSimSupport(this))
        //    sendSMS =1;
       // else
       //     sendSMS = 0;
        GetInfoNew();

    }

    public void GetInfoNew( ) {
        guiSMSThread = new Thread() {
            @Override
            public void run() {
                while (!isStop) try {
                    Thread.sleep(2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //  Toast.makeText(ThreeButtonActivity.this, " 3 get " , Toast.LENGTH_LONG).show();
                                url = IPAddress + "/api/serviceapi/GetAndroidInfo?username="+UserName+"&&getSTT="+useQMS+"&&getSMS="+sendSMS.intValue()+"&&getUserInfo=1"  ;
                                jsonRequest = new JsonObjectRequest(
                                        Request.Method.GET,
                                        url,
                                        null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                if (response != null) {
                                                    Integer num = response.optInt("TicketNumber");
                                                    number = num;

                                                    //region userinfo
                                                    try {
                                                        JSONObject jsUserInfo = response.optJSONObject("UserInfo");
                                                        if(jsUserInfo!= null){
                                                            String strName = "", strPosition = "";
                                                            try {
                                                                strName = jsUserInfo.getString("Name");
                                                                strPosition = jsUserInfo.getString("Position");
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                            lbTenNV.setText(strPosition + " : " + strName);
                                                        } else {
                                                            //  Toast.makeText(ThreeButtonActivity.this, "Không lấy được thông tin nhân viên.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    catch (Exception e){}
                                                    //endregion

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
                                                       // e.printStackTrace();
                                                    }
                                                    //endregion
                                                } else {
                                                    number = 0;
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
                    // Toast.makeText(ThreeButtonActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
                catch (Exception ex){}
            }
        };
        guiSMSThread.start();
    }

    public void LayThongTinNV(){
        //region lấy thông tin nhân viên
        final String urlPath = (IPAddress + "/api/serviceapi/getuserinfo?username=" + UserName );
        StringRequest jRequest = new StringRequest(
                Request.Method.GET, urlPath,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        if(!response.equals("null")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //region
                                    try {
                                        JSONObject obj = null;
                                        try {
                                            obj = new JSONObject(response);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        if (obj != null) {
                                            String strName = "", strPosition = "";
                                            try {
                                                strName = obj.getString("Name");
                                                strPosition = obj.getString("Position");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            lbTenNV.setText(strPosition + " : " + strName);
                                        } else {
                                            Toast.makeText(ThreeButtonActivity.this, "Không lấy được thông tin nhân viên.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    catch (Exception e){}
                                    //endregion
                                }
                            });
                        }
                        else
                            Toast.makeText(ThreeButtonActivity.this, "Không lấy được thông tin nhân viên.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ThreeButtonActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jRequest.setShouldCache(false);
        jRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if(mRequestQueue != null)
        mRequestQueue.add(jRequest);
        //endregion
    }

    public void LaySTT( ) {
        //region lấy so stt dang goi
          threadSTT = new Thread() {
            @Override
            public void run() {
                while ( !isStop) try {
                    Thread.sleep(2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                         //   Toast.makeText(ThreeButtonActivity.this, "STT", Toast.LENGTH_LONG);
                            //region lấy stt
                            url = IPAddress + "/api/serviceapi/getnumber?username=" + UserName;
                            jsonRequest = new JsonObjectRequest(
                                    Request.Method.GET,
                                    url,
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            if (response != null) {
                                                Integer num = response.optInt("Id");
                                                number = num;
                                            } else {
                                                number = 0;
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            //  lbNumber.setText(  "ERR");
                                        }
                                    }
                            );
                            //endregion

                            jsonRequest.setShouldCache(false);
                            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                           if(mRequestQueue != null)
                            mRequestQueue.add(jsonRequest);
                        }
                    });
                } catch (InterruptedException e) {
                   // Toast.makeText(ThreeButtonActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
                catch (Exception ex){}
            }
        };
        if(useQMS==1) {
            threadSTT.start();
        }
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
                                         //  Toast.makeText(ThreeButtonActivity.this, "SMS check three button.", Toast.LENGTH_LONG).show();
                                           try{
                                           if (response != null) {
                                                   // Loop through the array elements
                                                   for(int i=0;i<response.length();i++){
                                                       String string = response.getString(i).toString();
                                                       String[] strArr = string.split(":");
                                                       SendSMM(strArr[0].trim(),strArr[1].trim());
                                                      //  Toast.makeText(ThreeButtonActivity.this ,response.getString(i).toString(),Toast.LENGTH_LONG).show() ;
                                                   }
                                           }
                                           }catch (JSONException e){
                                           e.printStackTrace();
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
                  //  Toast.makeText(ThreeButtonActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
                catch (Exception e){}
            }
        };
        //endregion
         if(sendSMS==1) {
            guiSMSThread.start();
        }
    }

    public  boolean CheckPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this,permission);
        return  (check == PackageManager.PERMISSION_GRANTED);
    }

    public void SendSMM(String phoneNumber, String smsContent) {
        try {
           // phoneNumber = "0773169414;0786399485";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage( phoneNumber, null, smsContent , null, null);
            Toast.makeText(ThreeButtonActivity.this, "SMS sent.", Toast.LENGTH_LONG).show();
            Thread.sleep(2000);
        }
        catch (Exception e) {
            Toast.makeText(ThreeButtonActivity.this, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
          //  e.printStackTrace();
        }
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

    private  void SetAppConfig(){
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(ThreeButtonActivity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
             appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            switch (appType ){
                case "1":
                    intent = new Intent(ThreeButtonActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                      intent = new Intent(ThreeButtonActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case "3":
                    intent = new Intent(ThreeButtonActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case "4":
                    intent = new Intent(ThreeButtonActivity.this, CountersEventActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            UserName = sharedPreferences.getString("UserName", "0");
            Password = sharedPreferences.getString("Password", "0");
            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));

            lbTitle.setText(sharedPreferences.getString("ChaoDG", "Xin vui lòng đánh giá"));
            lbTitle.setTextSize(Float.parseFloat(sharedPreferences.getString("SizeChaoDG", "200")));
            useQMS = Integer.parseInt(sharedPreferences.getString("UseQMS", "0"));
            sendSMS = Integer.parseInt(sharedPreferences.getString("SendSMS", "0"));

            lbChuChay.setText(sharedPreferences.getString("Slogan", "Slogan here"));
            lbChuChay.setTextSize(Float.parseFloat(sharedPreferences.getString("SizeSlogan", "200")));
        }
    }

    //region tao menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mConfig:
                    intent = new Intent(ThreeButtonActivity.this, AppConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.mCounterEvent:
                intent = new Intent(ThreeButtonActivity.this, CountersEventActivity.class);
                startActivity(intent);
                break;
            case R.id.mExit:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Thông báo");
                alertDialogBuilder
                        .setMessage("Bạn có muốn thoát ứng dụng không ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        moveTaskToBack(true);
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                        System.exit(1);
                                    }
                                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion

    @Override
    public void messageReceived(SmsMessage smsMessage) {
        Toast.makeText(this, "New Message Received: " + smsMessage.getMessageBody(), Toast.LENGTH_SHORT).show();
    }
    private class LoadImageInternet extends AsyncTask<String,Void,Bitmap> {
        Bitmap bitmap = null;
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                InputStream is = url.openConnection().getInputStream();
                bitmap= BitmapFactory.decodeStream(is);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap){
            super.onPostExecute(bitmap);
            try {
                //  imAvatar  = findViewById(R.id.imAvatar);
                imgAvatar.setImageBitmap(bitmap);
                //  Drawable top = Drawable.createFromStream(bitmap , "src");
            }
            catch (Exception e){}
        }
    }

    public  boolean isSimSupport(Context context)    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        //Toast.makeText(ThreeButtonActivity.this,( (tm.getSimState()).to),Toast.LENGTH_LONG).show();
        return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isStop = true;
            Intent intent = getIntent();
            switch (appType ){
                case "1":
                    intent = new Intent(ThreeButtonActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(ThreeButtonActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case "3":
                    intent = new Intent(ThreeButtonActivity.this, DanhGiaActivity.class);
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
