package com.gpro.admin.qmsevaluateonly;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CounterSoftActivity extends AppCompatActivity //implements MessageListener
{
    private TextView lbCurrentNumber, lbTotal, lbWaitting,lbTitle,lbUserName,  lbSocketStatus;
    private Button btnNext, btnNextUT, btnTransfer, btnRecall, btnCallAny, btnDone, btnCancel, btnEvaluate;
    Integer useQMS = 0, number = 0, sendSMS = 0, userId = 0;
    JsonObjectRequest jsonRequest;
    private final int send_request_code = 1;
    private final String SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";
    String IPAddress, Mathietbi, url, requireLabel, appType, userName;
    PendingIntent sendPI, deliveredPI;
    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;
    public RequestQueue mRequestQueue = null;
    Thread guiSMSThread = null, threadSTT = null, threadLayTTNV;
    boolean isStop = false;
    ProgressDialog progressDialog;
    SwipeRefreshLayout wipeToRefresh;
    Intent intent;
      Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();
        setContentView(R.layout.activity_counter_soft);

        //dùng cho nhận sms
        //AutoStart.bindListener(CounterSoftActivity.this);

        progressDialog = new ProgressDialog(CounterSoftActivity.this);
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

        GetAppConfig();

        //region decleare
        lbUserName= (TextView) this.findViewById(R.id.lbnv);
        lbSocketStatus= (TextView) this.findViewById(R.id.lbSocket);


        lbTitle= (TextView) this.findViewById(R.id.lbTitle);
        lbTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                intent = new Intent(CounterSoftActivity.this,AppConfigActivity.class);
                intent.putExtra("hold", "1");
                startActivity(intent);
                return false;
            }
        });

        lbWaitting = (TextView) this.findViewById(R.id.lbWaitting);
        lbWaitting.setSelected(true);  // Set focus to the textview to run marque

        lbTotal = (TextView) this.findViewById(R.id.lbTotal);
        lbCurrentNumber = (TextView) this.findViewById(R.id.lbCurrentNumber);

        btnNext = (Button) this.findViewById(R.id.btnNext);
        //region btnNext_Click
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //region
                isStop = true;
                progressDialog.show();
                String str = (IPAddress + "/api/serviceapi/CallNext?matb=" + Mathietbi + "&userId=" + userId + "&dailyType=1");
                RequestQueue rqQue = Volley.newRequestQueue(CounterSoftActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                number = 0;
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs) {
                                    JSONObject obj = response.optJSONObject("Data_3");
                                    try {
                                        number = obj.getInt("TicketNumber");
                                    } catch (JSONException e) {
                                        progressDialog.hide();
                                        Toast.makeText(CounterSoftActivity.this, "Parse STT: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                } else
                                    Toast.makeText(CounterSoftActivity.this, "Hết vé", Toast.LENGTH_LONG).show();

                                lbCurrentNumber.setText(number.toString());
                                if (number.toString() == "0") {
                                    lbTotal.setText("0");
                                    lbWaitting.setText("---");
                                }
                                // progressDialog.hide();
                                isStop = false;
                                GetInfoNew(false);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.hide();
                                Toast.makeText(CounterSoftActivity.this, "Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jRequest.setShouldCache(false);
                jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // rqQue.add(jRequest);
                mRequestQueue.add(jRequest);
                //endregion
            }
        });
        //endregion

        btnNextUT = (Button) this.findViewById(R.id.btnNextUT);
        btnNextUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnTransfer = (Button) this.findViewById(R.id.btnTransfer);
        //region btnTransfer_Click
        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (number != 0) {
                    intent = new Intent(CounterSoftActivity.this, TransferTicketActivity.class);
                    intent.putExtra("url", IPAddress);
                    intent.putExtra("matb", Mathietbi);
                    intent.putExtra("stt", number);
                    startActivity(intent);
                } else {
                        Toast.makeText(CounterSoftActivity.this,"Vui lòng gọi số.",Toast.LENGTH_LONG).show();
                    }     }
        });
        //endregion

        btnRecall = (Button) this.findViewById(R.id.btnRecall);
        //region  btnRecall_Click
        btnRecall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lbCurrentNumber.getText().toString() != "" &&
                        Integer.parseInt(lbCurrentNumber.getText().toString()) != 0) {

                    isStop = true;
                    progressDialog.show();
                    String str = (IPAddress + "/api/serviceapi/Recall?matb=" + Mathietbi + "&userId=" + userId);
                    RequestQueue rqQue = Volley.newRequestQueue(CounterSoftActivity.this);
                    JsonObjectRequest jRequest = new JsonObjectRequest(
                            Request.Method.GET, str, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    number = 0;
                                    Boolean rs = response.optBoolean("IsSuccess");
                                    if (rs) {
                                        JSONObject obj = response.optJSONObject("Data_3");
                                        try {
                                            number = obj.getInt("TicketNumber");
                                        } catch (JSONException e) {
                                            progressDialog.hide();
                                            Toast.makeText(CounterSoftActivity.this, "Parse STT: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    } else
                                        Toast.makeText(CounterSoftActivity.this, "Hết vé", Toast.LENGTH_LONG).show();

                                    lbCurrentNumber.setText(number.toString());
                                    if (number.toString() == "0") {
                                        lbTotal.setText("0");
                                        lbWaitting.setText("---");
                                    }
                                    // progressDialog.hide();
                                    isStop = false;
                                    GetInfoNew(false);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.hide();
                                    Toast.makeText(CounterSoftActivity.this, "Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    jRequest.setShouldCache(false);
                    jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    // rqQue.add(jRequest);
                    mRequestQueue.add(jRequest);
                }
            }
        });
        //endregion

        btnCallAny = (Button) this.findViewById(R.id.btnCallAny);
        //region btnCallAny_Click
        btnCallAny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lbCurrentNumber.getText().toString() == "" ||
                        Integer.parseInt(lbCurrentNumber.getText().toString()) == 0 ||
                        Integer.parseInt(lbCurrentNumber.getText().toString()) == number) {
                    Toast.makeText(CounterSoftActivity.this, "Vui lòng nhập 1 trong những số thứ tự nằm trong danh sách chờ ", Toast.LENGTH_LONG).show();
                    lbCurrentNumber.requestFocus();
                } else {

                    progressDialog.show();
                    String str = (IPAddress + "/api/serviceapi/CallAny?matb=" + Mathietbi + "&userId=" + userId + "&stt=" + lbCurrentNumber.getText().toString());
                    RequestQueue rqQue = Volley.newRequestQueue(CounterSoftActivity.this);
                    JsonObjectRequest jRequest = new JsonObjectRequest(
                            Request.Method.GET, str, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    number = 0;
                                    Boolean rs = response.optBoolean("IsSuccess");
                                    if (rs) {
                                        JSONObject obj = response.optJSONObject("Data_3");
                                        try {
                                            number = obj.getInt("TicketNumber");
                                        } catch (JSONException e) {
                                            progressDialog.hide();
                                            Toast.makeText(CounterSoftActivity.this, "Parse STT: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    } else
                                        Toast.makeText(CounterSoftActivity.this, "Hết vé", Toast.LENGTH_LONG).show();

                                    lbCurrentNumber.setText(number.toString());
                                    if (number.toString() == "0") {
                                        lbTotal.setText("0");
                                        lbWaitting.setText("---");
                                    }
                                    // progressDialog.hide();
                                    isStop = false;
                                    GetInfoNew(false);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.hide();
                                    Toast.makeText(CounterSoftActivity.this, "Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    jRequest.setShouldCache(false);
                    jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    // rqQue.add(jRequest);
                    mRequestQueue.add(jRequest);

                }
            }
        });
        //endregion

        btnDone = (Button) this.findViewById(R.id.btnDone);
        //region btnDone_Click
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lbCurrentNumber.getText().toString() != "" &&
                        Integer.parseInt(lbCurrentNumber.getText().toString()) != 0) {

                    isStop = true;
                    progressDialog.show();
                    String str = (IPAddress + "/api/serviceapi/DoneTicket?matb=" + Mathietbi + "&userId=" + userId);
                    RequestQueue rqQue = Volley.newRequestQueue(CounterSoftActivity.this);
                    JsonObjectRequest jRequest = new JsonObjectRequest(
                            Request.Method.GET, str, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    number = 0;
                                    Boolean rs = response.optBoolean("IsSuccess");
                                    if (rs) {
                                        isStop = false;
                                        GetInfoNew(false);
                                    } else
                                        Toast.makeText(CounterSoftActivity.this, "Hết vé", Toast.LENGTH_LONG).show();

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.hide();
                                    Toast.makeText(CounterSoftActivity.this, "Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    jRequest.setShouldCache(false);
                    jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    // rqQue.add(jRequest);
                    mRequestQueue.add(jRequest);
                }
            }
        });
        //endregion

        btnCancel = (Button) this.findViewById(R.id.btnCancel);
        //region btnCancel_Click
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( number != 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CounterSoftActivity.this);
                    alertDialogBuilder.setTitle("Xác nhận hủy phiếu");
                    alertDialogBuilder
                            .setMessage("Bạn có muốn hủy phiếu " + lbCurrentNumber.getText().toString() + " không ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            isStop = true;
                                            progressDialog.show();
                                            String str = (IPAddress + "/api/serviceapi/DeleteTicket?number=" + lbCurrentNumber.getText().toString());
                                            RequestQueue rqQue = Volley.newRequestQueue(CounterSoftActivity.this);
                                            JsonObjectRequest jRequest = new JsonObjectRequest(
                                                    Request.Method.GET, str, null,
                                                    new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {
                                                            number = 0;
                                                            Boolean rs = response.optBoolean("IsSuccess");
                                                            if (rs) {
                                                                isStop = false;
                                                                GetInfoNew(false);
                                                            } else
                                                                Toast.makeText(CounterSoftActivity.this, "Hủy phiếu thất bại.", Toast.LENGTH_LONG).show();

                                                        }
                                                    },
                                                    new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                            progressDialog.hide();
                                                            Toast.makeText(CounterSoftActivity.this, "Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                            );
                                            jRequest.setShouldCache(false);
                                            jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                            // rqQue.add(jRequest);
                                            mRequestQueue.add(jRequest);
                                        }
                                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                 else {
                    Toast.makeText(CounterSoftActivity.this,"Vui lòng gọi số.",Toast.LENGTH_LONG).show();
                }
            }
        });
        //endregion

        btnEvaluate = (Button) this.findViewById(R.id.btnEvaluate);
        //region btnEvaluate_Click
        btnEvaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(number!= 0) {
                    intent = new Intent(CounterSoftActivity.this, ThreeButtonActivity.class);
                    intent.putExtra("backTo", "CounterSoft");
                    startActivity(intent);
                }
                else {
                    Toast.makeText(CounterSoftActivity.this,"Vui lòng gọi số.",Toast.LENGTH_LONG).show();
                }
            }
        });
        //endregion

        wipeToRefresh = (SwipeRefreshLayout) this.findViewById(R.id.swipeToRefresh);
        wipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isStop = false;
                GetInfoNew(true);
            }
        });
        //endregion
initSocketIO();

        GetInfoNew(false);
    }

    private void initSocketIO(){
        try {
            mSocket = IO.socket("http://192.168.1.8:3000");
            mSocket.connect();
            lbSocketStatus.setText("Socket Connected");
            lbSocketStatus.setTextColor(Color.BLUE);
            //  Log.d("success", mSocket.id());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            lbSocketStatus.setText("Socket Disconnected");
            lbSocketStatus.setTextColor(Color.RED);
            // Log.d("fail", "Failed to connect");
        }

    }

    public void GetInfoNew(final Boolean isReload) {
        progressDialog.show();
        try {
            url = IPAddress + "/api/serviceapi/GetAndroidInfo3?userName=" + userName + "&matb=" + Mathietbi + "&getSTT=" + useQMS + "&getSMS=" + sendSMS.intValue() + "&getUserInfo=1";
            jsonRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Integer totalWaitting = 0;
                            String counterWattings = "";
                            if (response != null) {
                                Integer num = response.optInt("TicketNumber");
                                number = num;

                                userId = response.optInt("UserId");

                                totalWaitting = response.optInt("TotalWaiting");
                                counterWattings = (response.optString("CounterWaitings") == "null" ? "---" : response.optString("CounterWaitings"));

                                //region userinfo
                                                    try {
                                                        JSONObject jsUserInfo = response.optJSONObject("UserInfo");
                                                        if (jsUserInfo != null) {
                                                            String strName = "", strPosition = "";
                                                            try {
                                                                strName = jsUserInfo.getString("UserName");
                                                               // strPosition = jsUserInfo.getString("Position");
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                            lbUserName.setText(  strName);
                                                        } else {
                                                            //  Toast.makeText(ThreeButtonActivity.this, "Không lấy được thông tin nhân viên.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (Exception e) {
                                                    }
                                //endregion

                                // region Send SMS
                                                    /*
                                                    try {
                                                        JSONArray jsonArrSMS = response.optJSONArray("SMS");
                                                        if (jsonArrSMS != null && jsonArrSMS.length() > 0) {
                                                            // Loop through the array elements
                                                            for (int i = 0; i < jsonArrSMS.length(); i++) {
                                                                String string = jsonArrSMS.getString(i).toString();
                                                                String[] strArr = string.split(":");
                                                                SendSMM(strArr[0].trim(), strArr[1].trim());
                                                            }
                                                        }
                                                    } catch (JSONException e) {
                                                        // e.printStackTrace();
                                                    } */
                                //endregion
                            } else {
                                number = 0;
                            }
                            lbCurrentNumber.setText(number.toString());
                            lbTotal.setText(totalWaitting.toString());
                            lbWaitting.setText(counterWattings);
                            progressDialog.hide();

                            if (isReload) {
                                Toast.makeText(CounterSoftActivity.this, "Page Refresh", Toast.LENGTH_LONG).show();
                                wipeToRefresh.setRefreshing(false);
                            }

                            if (totalWaitting.toString() == "0") {
                                //khi counter ko có vé cho ngủ 1phut sau đó quét lại tới khi nào có phiếu thì ngưng
                                try {
                                    Thread.sleep(60000);
                                    isStop = false;
                                    GetInfoNew(false);
                                } catch (InterruptedException e) {
                                    // e.printStackTrace();
                                    progressDialog.hide();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.hide();
                            if (isReload) {
                                Toast.makeText(CounterSoftActivity.this, "Page Refresh", Toast.LENGTH_LONG).show();
                                wipeToRefresh.setRefreshing(false);
                            }
                        }
                    }
            );
            //endregion

            jsonRequest.setShouldCache(false);
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (mRequestQueue != null)
                mRequestQueue.add(jsonRequest);
        } catch (Exception e) {
            progressDialog.hide();
            if (isReload) {
                Toast.makeText(CounterSoftActivity.this, "Page Refresh", Toast.LENGTH_LONG).show();
                wipeToRefresh.setRefreshing(false);
            }
        }
    }

    private void GetAppConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(CounterSoftActivity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            switch (appType) {
                case "1":
                    intent = new Intent(CounterSoftActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(CounterSoftActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case "3":
                    intent = new Intent(CounterSoftActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case "4":
                    intent = new Intent(CounterSoftActivity.this, CountersEventActivity.class);
                    startActivity(intent);
                    break;
                case "5":
                    intent = new Intent(CounterSoftActivity.this, PrintTicket_2Activity.class);
                    startActivity(intent);
                    break;
                case "6":
                    intent = new Intent(CounterSoftActivity.this, PrintTicket_3Activity.class);
                    startActivity(intent);
                    break;
                case "7":
                    intent = new Intent(CounterSoftActivity.this, HienThiQuay.class);
                    startActivity(intent);
                    break;
                case "8":
                    intent = new Intent(CounterSoftActivity.this, PrintTicket_4Activity.class);
                    startActivity(intent);
                    break;
                case "10":
                    intent = new Intent(CounterSoftActivity.this, ReceiveSmsActivity.class);
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
    }

    //TODO hàm nhận tin nhắn
   /* @Override
    public void messageReceived(SmsMessage smsMessage) {
        String txt = "From: " + smsMessage.getOriginatingAddress();
        txt += " | Message:" + smsMessage.getMessageBody();
        Toast.makeText(this, "Counter soft Activity: " + txt, Toast.LENGTH_SHORT).show();
    }
    */
}