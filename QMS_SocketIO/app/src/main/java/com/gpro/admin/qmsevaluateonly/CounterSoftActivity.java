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
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.KeyEvent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CounterSoftActivity extends AppCompatActivity implements ConfirmSaveDialog.DialogListener
{
    private TextView lbCurrentNumber, lbTotal, lbWaitting, lbTitle, lbUserName, lbSocketStatus, lbStatus;
    private Button btnNext, btnNextUT, btnTransfer, btnRecall, btnCallAny, btnDone, btnCancel, btnEvaluate;
    Integer useQMS = 0, number = 0, sendSMS = 0, userId = 0, soundIndex = 0, timeRefresh = 0;
    JsonObjectRequest jsonRequest;
    private final int send_request_code = 1;
    private final String SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";
    String IPAddress, IPNodeAddress, Mathietbi, url, requireLabel, appType, userName;
    PendingIntent sendPI, deliveredPI;
    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;
    public RequestQueue mRequestQueue = null;
    Thread guiSMSThread = null, threadSTT = null, threadLayTTNV;
    boolean isStop = false  ;
    ProgressDialog progressDialog;
    SwipeRefreshLayout wipeToRefresh;
    Intent intent;
    Socket mSocket;
    CountDownTimer countDownTimer = null;

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
        // progressDialog.show();

        // Instantiate the cache
        final Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        final Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        GetAppConfig();
    }

    public void GetInfoNew(final Boolean isReload) {
     //   if (!isReload)
         //   progressDialog.show();

        try {
            url = IPAddress + "/api/serviceapi/GetAndroidInfo3?userName=" + userName + "&matb=" + Mathietbi + "&getSTT=" + useQMS + "&getSMS=" + sendSMS.intValue() + "&getUserInfo=1";
            //Thread.sleep(1000);
            jsonRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!isReload) {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                }
                            }
                            progressDialog.hide();
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
                                        }
                                        lbUserName.setText(strName);
                                    } else {
                                        //  Toast.makeText(ThreeButtonActivity.this, "Không lấy được thông tin nhân viên.", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                }
                                //endregion

                                lbCurrentNumber.setText(number.toString());
                                lbTotal.setText(totalWaitting.toString());
                                lbWaitting.setText(counterWattings);

                                if (isReload) {
                                    //Toast.makeText(CounterSoftActivity.this, "Page Refresh", Toast.LENGTH_LONG).show();
                                    wipeToRefresh.setRefreshing(false);
                                }

                                lbStatus.setText("");

                                if(number == 0 && totalWaitting==0 && timeRefresh<3){
                                    lbStatus.setText("num = 0 -> reload" );
                                    timeRefresh++;
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                    }
                                    GetInfoNew(true);
                                }
                                else
                                timeRefresh = 0;
                            }
                            else {
                                // number = 0;
                                if (timeRefresh == 3)
                                    timeRefresh = 0;
                                else {
                                    timeRefresh++;
                                    lbStatus.setText("Reconnect:" + timeRefresh);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                    }
                                    GetInfoNew(true);
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            progressDialog.hide();
                            if (isReload) {
                                //Toast.makeText(CounterSoftActivity.this, "Page Refresh", Toast.LENGTH_LONG).show();
                                wipeToRefresh.setRefreshing(false);
                            }
                            lbStatus.setText(timeRefresh + "- Kết nối máy chủ thất bại code:" + error.getMessage());
                            if (timeRefresh == 3)
                                timeRefresh = 0;
                            else {
                                timeRefresh++;
                                lbStatus.setText("Reconnect:" + timeRefresh);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                }
                                GetInfoNew(true);
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
                //Toast.makeText(CounterSoftActivity.this, "Page Refresh", Toast.LENGTH_LONG).show();
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
                case "11":
                    intent = new Intent(CounterSoftActivity.this, LcdAreaActivity.class);
                    startActivity(intent);
                    break;
                case "12":
                    intent = new Intent(CounterSoftActivity.this, ReceiveSmsActivity.class);
                    startActivity(intent);
                    break;
                case "13":
                    intent = new Intent(CounterSoftActivity.this, ReceiveSmsActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "138.168.31.246:92");
            IPNodeAddress = "http://" + sharedPreferences.getString("SocketIP", "138.168.31.246:91");
            Mathietbi = sharedPreferences.getString("Equipcode", "0");
            setTitle(sharedPreferences.getString("APP_TITLE", "GPRO-QMS-482CS"));

            //lbTitle.setText(sharedPreferences.getString("ChaoDG", "Xin vui lòng đánh giá"));
            //lbTitle.setTextSize(Float.parseFloat(sharedPreferences.getString("SizeChaoDG", "200")));
            useQMS = Integer.parseInt(sharedPreferences.getString("UseQMS", "0"));
            sendSMS = Integer.parseInt(sharedPreferences.getString("SendSMS", "0"));
            userName = sharedPreferences.getString("UserName", "0");

            //lbChuChay.setText(sharedPreferences.getString("Slogan", "Slogan here"));
            //lbChuChay.setTextSize(Float.parseFloat(sharedPreferences.getString("SizeSlogan", "200")));

            InitControls();
            initSocketIO();
            GetInfoNew(false);
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

    private void InitControls() {
        //region decleare
        lbUserName = (TextView) this.findViewById(R.id.lbnv);
        lbSocketStatus = (TextView) this.findViewById(R.id.lbSocket);
        lbStatus = (TextView) this.findViewById(R.id.lbStatus);
        lbTitle = (TextView) this.findViewById(R.id.lbTitle);
        lbWaitting = (TextView) this.findViewById(R.id.lbWaitting);
        lbWaitting.setSelected(true);  // Set focus to the textview to run marque

        lbTotal = (TextView) this.findViewById(R.id.lbTotal);
        lbCurrentNumber = (TextView) this.findViewById(R.id.lbCurrentNumber);

        btnNext = (Button) this.findViewById(R.id.btnNext);
        btnNextUT = (Button) this.findViewById(R.id.btnNextUT);
        btnTransfer = (Button) this.findViewById(R.id.btnTransfer);
        btnRecall = (Button) this.findViewById(R.id.btnRecall);
        btnCallAny = (Button) this.findViewById(R.id.btnCallAny);
        btnDone = (Button) this.findViewById(R.id.btnDone);
        btnCancel = (Button) this.findViewById(R.id.btnCancel);
        btnEvaluate = (Button) this.findViewById(R.id.btnEvaluate);
        wipeToRefresh = (SwipeRefreshLayout) this.findViewById(R.id.swipeToRefresh);
        //endregion

        InitControlEvents();
    }

    private void InitControlEvents() {
        lbTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                  ConfirmSaveDialog dialog = new ConfirmSaveDialog();
                dialog.show(getSupportFragmentManager(), "Confirm");
                return false;
            }
        });

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
                                progressDialog.hide();
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs) {
                                    JSONObject obj = response.optJSONObject("Data_3");
                                    try {
                                        number = obj.getInt("TicketNumber");
                                    } catch (Exception e) {
                                        progressDialog.hide();
                                        Toast.makeText(CounterSoftActivity.this, "Parse STT: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }

                                    String _counterIds = response.optString("Data_4");
                                    mSocket.emit("qms-system-counter-next", _counterIds);
                                } else
                                    Toast.makeText(CounterSoftActivity.this, "Hết vé", Toast.LENGTH_LONG).show();

                                lbCurrentNumber.setText(number.toString());
                                if (number.toString() == "0") {
                                    lbTotal.setText("0");
                                    lbWaitting.setText("---");
                                }

                                isStop = false;

                                // GetInfoNew(false);
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

        btnNextUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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
                    Toast.makeText(CounterSoftActivity.this, "Vui lòng gọi số.", Toast.LENGTH_LONG).show();
                }
            }
        });
        //endregion

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
                                    progressDialog.hide();
                                    Boolean rs = response.optBoolean("IsSuccess");
                                    if (rs) {
                                        JSONObject obj = response.optJSONObject("Data_3");
                                        try {
                                            number = obj.getInt("TicketNumber");
                                        } catch (JSONException e) {
                                            progressDialog.hide();
                                            Toast.makeText(CounterSoftActivity.this, "Parse STT: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                       // mSocket.emit("qms-system-counter-next", Mathietbi);
                                        String _counterIds = response.optString("Data_4");
                                        mSocket.emit("qms-system-counter-next", _counterIds);
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
                                    progressDialog.hide();
                                    Boolean rs = response.optBoolean("IsSuccess");
                                    if (rs) {
                                        JSONObject obj = response.optJSONObject("Data_3");
                                        try {
                                            number = obj.getInt("TicketNumber");
                                        } catch (JSONException e) {
                                            progressDialog.hide();
                                            Toast.makeText(CounterSoftActivity.this, "Parse STT: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                        String _counterIds = response.optString("Data_4");
                                        mSocket.emit("qms-system-counter-next", _counterIds);
                                    } else
                                        Toast.makeText(CounterSoftActivity.this, "Hết vé", Toast.LENGTH_LONG).show();

                                    lbCurrentNumber.setText(number.toString());
                                    if (number.toString() == "0") {
                                        lbTotal.setText("0");
                                        lbWaitting.setText("---");
                                    }
                                    // progressDialog.hide();
                                    isStop = false;
                                    //GetInfoNew(false);
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
                                    progressDialog.hide();
                                    Boolean rs = response.optBoolean("IsSuccess");
                                    if (rs) {
                                        isStop = false;
                                        String _counterIds = response.optString("Data_4");
                                        mSocket.emit("qms-system-counter-next", _counterIds);
                                        //GetInfoNew(false);
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

        //region btnCancel_Click
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (number != 0) {
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
                                            String str = (IPAddress + "/api/serviceapi/DeleteTicket?number=" + lbCurrentNumber.getText().toString()+ "&userId=" + userId);
                                            RequestQueue rqQue = Volley.newRequestQueue(CounterSoftActivity.this);
                                            JsonObjectRequest jRequest = new JsonObjectRequest(
                                                    Request.Method.GET, str, null,
                                                    new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {
                                                            number = 0;
                                                            progressDialog.hide();
                                                            Boolean rs = response.optBoolean("IsSuccess");
                                                            if (rs) {
                                                                isStop = false;
                                                                String _counterIds = response.optString("Data_4");
                                                                mSocket.emit("qms-system-counter-next", _counterIds);
                                                                //GetInfoNew(false);
                                                            } else
                                                                Toast.makeText(CounterSoftActivity.this, "Hủy phiếu thất bại.", Toast.LENGTH_LONG).show();
                                                            progressDialog.hide();
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
                } else {
                    Toast.makeText(CounterSoftActivity.this, "Vui lòng gọi số.", Toast.LENGTH_LONG).show();
                }
            }
        });
        //endregion

        //region btnEvaluate_Click
        btnEvaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (number != 0) {
                    intent = new Intent(CounterSoftActivity.this, ThreeButtonActivity.class);
                    intent.putExtra("backTo", "CounterSoft");
                    startActivity(intent);
                } else {
                    Toast.makeText(CounterSoftActivity.this, "Vui lòng gọi số.", Toast.LENGTH_LONG).show();
                }
            }
        });
        //endregion

        wipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isStop = false;
                GetInfoNew(true);
            }
        });
    }

    private void initSocketIO() {
        try {
            mSocket = IO.socket(IPNodeAddress);
        } catch (URISyntaxException e) {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mSocket != null) {
            mSocket.connect();
            mSocket.on("node-counter-next", onRefresh);
            mSocket.on(Socket.EVENT_CONNECT, onSocketConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onSocketDisconnect);

        }
    }

    private Emitter.Listener onRefresh = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GetInfoNew(true);
                }
            });
        }
    };

    private Emitter.Listener onSocketConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lbSocketStatus.setText(mSocket.id());
                    lbSocketStatus.setTextColor(Color.BLUE);

                    //socketid|counterid|serviceId|userid
                    mSocket.emit("android-send-device-info",mSocket.id()+"|"+Mathietbi+"||"+userId);
                }
            });
        }
    };

    private Emitter.Listener onSocketDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lbSocketStatus.setText("Socket Disconnected");
                    lbSocketStatus.setTextColor(Color.RED);
                }
            });
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(mSocket != null) {
            mSocket.disconnect();
        }
    }

    @Override
    public void ApplyTexts(String password) {
        if (password.equalsIgnoreCase("123") ) {
            intent = new Intent(CounterSoftActivity.this, AppConfigActivity.class);
            intent.putExtra("hold", "1");
            startActivity(intent);
        }
        else
            Toast.makeText(CounterSoftActivity.this, "Mật khẩu quản trị không đúng vui lòng nhập lại.", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
             ConfirmSaveDialog dialog = new ConfirmSaveDialog();
             dialog.show(getSupportFragmentManager(),"Confirm");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}