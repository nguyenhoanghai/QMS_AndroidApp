package com.gpro.admin.qmsevaluateonly;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.GetChars;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.zip.InflaterInputStream;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import yuku.ambilwarna.AmbilWarnaDialog;

public class LcdAreaActivity extends AppCompatActivity implements ConfirmSaveDialog.DialogListener {

    String IPAddress, IPNodeAddress, url, appType, userId = "0", counterIds = "0", serviceIds = "0";
    Integer timeRefresh = 0;
    private TextView lbTitle, lbSocketStatus, lbStatus, lbCaption1, lbCaption2, lbCaption3, lbCaption4;
    LinearLayout setting_panel;
    public RequestQueue mRequestQueue = null;
    boolean isPlaying = false, isSmallConfig = false;
    ProgressDialog progressDialog;
    SwipeRefreshLayout wipeToRefresh;
    Intent intent;
    Socket mSocket;
    ArrayList<Integer> playSounds = new ArrayList<>();
    MediaPlayer mediaPlayer = null;
    TextView[] textViewArr;

    Button btnSave, btnCancel, btnTitleColor, btnCaptionBG, btnCaptionColor, btnContentBG, btnContentColor;
    EditText txtIp, txtSocketIp, txtTitle, txtTitleSize, txtUserId, txtServiceId, txtCounterId, txtCaptionSize, txtContentSize, txtRow;
    Integer titleColor = -15859455, captionColor = -15859455, captionBG = -15859455, contentBG = -15859455, contentColor = -15859455;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();
        setContentView(R.layout.activity_lcd_area);

        progressDialog = new ProgressDialog(LcdAreaActivity.this);
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

        InitControls();
        GetAppConfig();
        InitSocketIO();
        InitListView();

        GetInfoNew(true);
        progressDialog.hide();
    }

    //region Init Controls
    private void InitControls() {
        //region decleare
        lbSocketStatus = (TextView) this.findViewById(R.id.lbSocket);
        lbStatus = (TextView) this.findViewById(R.id.lbStatus);
        lbTitle = (TextView) this.findViewById(R.id.lbTitle);

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                200,
                100,
                1.0f
        );
        layoutParams.setMargins(0, 1, 2, 1);
        lbCaption1 = (TextView) this.findViewById(R.id.lbCaption1);
        lbCaption1.setLayoutParams(layoutParams);

        lbCaption2 = (TextView) this.findViewById(R.id.lbCaption2);
        layoutParams.setMargins(2, 1, 0, 1);
        lbCaption2.setLayoutParams(layoutParams);

        lbCaption3 = (TextView) this.findViewById(R.id.lbCaption3);
        lbCaption3.setLayoutParams(layoutParams);

        lbCaption4 = (TextView) this.findViewById(R.id.lbCaption4);
        layoutParams.setMargins(1, 1, 1, 1);
        lbCaption4.setLayoutParams(layoutParams);

        //  tbContent = (TableLayout) this.findViewById(R.id.tbContent);
        //wipeToRefresh = (SwipeRefreshLayout) this.findViewById(R.id.swipeToRefresh);
        //endregion

        btnSave = (Button) this.findViewById(R.id.btnSave);
        btnCancel = (Button) this.findViewById(R.id.btnCancel);
        btnTitleColor = (Button) this.findViewById(R.id.btnTitleColor);
        btnCaptionBG = (Button) this.findViewById(R.id.btnCaptionBG);
        btnCaptionColor = (Button) this.findViewById(R.id.btnCaptionColor);
        btnContentBG = (Button) this.findViewById(R.id.btnContentBG);
        btnContentColor = (Button) this.findViewById(R.id.btnContentColor);

        txtIp = (EditText) this.findViewById(R.id.txtIp);
        txtSocketIp = (EditText) this.findViewById(R.id.txtSocketIp);
        txtTitle = (EditText) this.findViewById(R.id.txtTitle);
        txtTitleSize = (EditText) this.findViewById(R.id.txtTitleSize);
        txtUserId = (EditText) this.findViewById(R.id.txtUserId);
        txtServiceId = (EditText) this.findViewById(R.id.txtServiceId);
        txtCounterId = (EditText) this.findViewById(R.id.txtCounterId);
        txtCaptionSize = (EditText) this.findViewById(R.id.txtCaptionSize);
        txtContentSize = (EditText) this.findViewById(R.id.txtContentSize);
        txtRow = (EditText) this.findViewById(R.id.txtRow);

        setting_panel = (LinearLayout) this.findViewById(R.id.setting_panel);
        setting_panel.setVisibility(View.GONE);
        InitControlEvents();
    }

    private void InitControlEvents() {
        lbTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isSmallConfig = false;
                ConfirmSaveDialog dialog = new ConfirmSaveDialog();
                dialog.show(getSupportFragmentManager(), "Confirm");
                return false;
            }
        });

        /*
        wipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isStop = false;
                //GetInfoNew(true);
            }
        });
        */

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtIp.getText().toString() == "")
                    Toast.makeText(LcdAreaActivity.this, "Vui lòng nhập địa chỉ máy chủ.", Toast.LENGTH_LONG).show();
                else if (txtSocketIp.getText().toString() == "")
                    Toast.makeText(LcdAreaActivity.this, "Vui lòng nhập địa chỉ máy chủ socket.", Toast.LENGTH_LONG).show();
                else {
                   SaveConfig();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_panel.setVisibility(View.GONE);
            }
        });
        btnTitleColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("TitleColor");
            }
        });
        btnCaptionBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("CaptionBG");
            }
        });
        btnCaptionColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("CaptionColor");
            }
        });
        btnContentBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("ContentBG");
            }
        });
        btnContentColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("ContentColor");
            }
        });
    }

    private void SaveConfig() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("IS_FIRTS_LAUNCHER", false);
        editor.putString("IP", txtIp.getText().toString());
        editor.putString("SocketIP", txtSocketIp.getText().toString());
        editor.putString("LCD_Area_UserId", txtUserId.getText().toString());
        editor.putString("LCD_Area_CounterIds", txtCounterId.getText().toString());
        editor.putString("LCD_Area_ServiceIds", txtServiceId.getText().toString());

        editor.putString("LCD_Area_Title", txtTitle.getText().toString());
        editor.putString("LCD_Area_Title_Size", txtTitleSize.getText().toString());
        editor.putString("LCD_Area_Title_Color", btnTitleColor.getText().toString());

        editor.putString("LCD_Area_Caption_Color", btnCaptionColor.getText().toString());
        editor.putString("LCD_Area_Caption_BG", btnCaptionBG.getText().toString());
        editor.putString("LCD_Area_Caption_Size", txtCaptionSize.getText().toString());

        editor.putString("LCD_Area_Content_Color", btnContentColor.getText().toString());
        editor.putString("LCD_Area_Content_BG", btnContentBG.getText().toString());
        editor.putString("LCD_Area_Content_Size", txtContentSize.getText().toString());
        editor.putString("LCD_Area_Row", txtRow.getText().toString());
        editor.apply();
        setting_panel.setVisibility(View.GONE);
        GetAppConfig();
        InitListView();
        GetInfoNew(true);
    }
    //endregion

    //region color picker
    public void openColorPicker(final String code) {
        int _color = -15859455;
        switch (code) {
            case "TitleColor":
                _color = titleColor;
                break;
            case "CaptionBG":
                _color = captionBG;
                break;
            case "CaptionColor":
                _color = captionColor;
                break;
            case "ContentBG":
                _color = contentBG;
                break;
            case "ContentColor":
                _color = contentColor;
                break;
        }
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, _color, false, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                Ok_Click(color, code);
            }
        });
        dialog.show();
    }

    private void Ok_Click(int color, String code) {
        switch (code) {
            case "TitleColor":
                titleColor = color;
                btnTitleColor.setTextColor(color);
                btnTitleColor.setText(color + "");
                break;
            case "CaptionBG":
                captionBG = color;
                btnCaptionBG.setTextColor(color);
                btnCaptionBG.setText(color + "");
                break;
            case "CaptionColor":
                captionColor = color;
                btnCaptionColor.setTextColor(color);
                btnCaptionColor.setText(color + "");
                break;
            case "ContentBG":
                contentBG = color;
                btnContentBG.setTextColor(color);
                btnContentBG.setText(color + "");
                break;
            case "ContentColor":
                contentColor = color;
                btnContentColor.setTextColor(color);
                btnContentColor.setText(color + "");
                break;
        }
    }

    //endregion

    //region play sound
    private void PlaySound() {
        isPlaying = true;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = MediaPlayer.create(this, playSounds.get(0));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playSounds.remove(0);
                if (playSounds.size() > 0) {
                    PlaySound();
                } else {
                    mediaPlayer.stop();
                    isPlaying = false;
                }
            }
        });
        mediaPlayer.start();
    }

    //endregion

    private void GetAppConfig() {
        sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(LcdAreaActivity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            switch (appType) {
                case "1":
                    intent = new Intent(LcdAreaActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(LcdAreaActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case "3":
                    intent = new Intent(LcdAreaActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case "4":
                    intent = new Intent(LcdAreaActivity.this, CountersEventActivity.class);
                    startActivity(intent);
                    break;
                case "5":
                    intent = new Intent(LcdAreaActivity.this, PrintTicket_2Activity.class);
                    startActivity(intent);
                    break;
                case "6":
                    intent = new Intent(LcdAreaActivity.this, PrintTicket_3Activity.class);
                    startActivity(intent);
                    break;
                case "7":
                    intent = new Intent(LcdAreaActivity.this, HienThiQuay.class);
                    startActivity(intent);
                    break;
                case "8":
                    intent = new Intent(LcdAreaActivity.this, PrintTicket_4Activity.class);
                    startActivity(intent);
                    break;
                case "10":
                    intent = new Intent(LcdAreaActivity.this, ReceiveSmsActivity.class);
                    startActivity(intent);
                    break;

                case "12":
                    intent = new Intent(LcdAreaActivity.this, ReceiveSmsActivity.class);
                    startActivity(intent);
                    break;
                case "13":
                    intent = new Intent(LcdAreaActivity.this, ReceiveSmsActivity.class);
                    startActivity(intent);
                    break;
            }
            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            txtIp.setText(sharedPreferences.getString("IP", "0.0.0.0"));

            IPNodeAddress = "http://" + sharedPreferences.getString("SocketIP", "0.0.0.0");
            txtSocketIp.setText(sharedPreferences.getString("SocketIP", "0.0.0.0"));


            userId = sharedPreferences.getString("LCD_Area_UserId", "0");
            txtUserId.setText(sharedPreferences.getString("LCD_Area_UserId", "11"));

            counterIds = sharedPreferences.getString("LCD_Area_CounterIds", "1,2,3");
            txtCounterId.setText(sharedPreferences.getString("LCD_Area_CounterIds", "0"));

            serviceIds = sharedPreferences.getString("LCD_Area_ServiceIds", "1,2,3");
            txtServiceId.setText(sharedPreferences.getString("LCD_Area_ServiceIds", "0"));

            lbTitle.setText(sharedPreferences.getString("LCD_Area_Title", "Title"));
            txtTitle.setText(sharedPreferences.getString("LCD_Area_Title", "Title"));
            lbTitle.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Title_Color", "-15859455")));
            btnTitleColor.setText(sharedPreferences.getString("LCD_Area_Title_Color", "-15859455"));
            lbTitle.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_Area_Title_Size", "50")));
            txtTitleSize.setText(sharedPreferences.getString("LCD_Area_Title_Size", "50"));
            txtRow.setText(sharedPreferences.getString("LCD_Area_Row", "1"));

            lbCaption1.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Caption_Color", "-15859455")));
            lbCaption2.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Caption_Color", "-15859455")));
            lbCaption3.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Caption_Color", "-15859455")));
            lbCaption4.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Caption_Color", "-15859455")));
            btnCaptionColor.setText(sharedPreferences.getString("LCD_Area_Caption_Color", "-15859455"));

            lbCaption1.setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Caption_BG", "-15859455")));
            lbCaption2.setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Caption_BG", "-15859455")));
            lbCaption3.setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Caption_BG", "-15859455")));
            lbCaption4.setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Caption_BG", "-15859455")));
            btnCaptionBG.setText(sharedPreferences.getString("LCD_Area_Caption_BG", "-15859455"));

            lbCaption1.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_Area_Caption_Size", "50")));
            lbCaption2.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_Area_Caption_Size", "50")));
            lbCaption3.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_Area_Caption_Size", "50")));
            lbCaption4.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_Area_Caption_Size", "50")));
            txtCaptionSize.setText(sharedPreferences.getString("LCD_Area_Caption_Size", "50"));

            if (textViewArr != null && textViewArr.length > 0) {
                for (Integer i = 0; i < textViewArr.length; i++) {
                    textViewArr[i].setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Content_Color", "-15859455")));
                    textViewArr[i].setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Content_BG", "-15859455")));
                    textViewArr[i].setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_Area_Content_Size", "50")));
                }
            }

            txtContentSize.setText(sharedPreferences.getString("LCD_Area_Content_Size", "50"));
            btnContentBG.setText(sharedPreferences.getString("LCD_Area_Content_BG", "-15859455"));
            btnContentColor.setText(sharedPreferences.getString("LCD_Area_Content_Color", "-15859455"));
        }
    }

    private void InitListView() {
        int count = 0, rows = Integer.parseInt(txtRow.getText().toString());
        textViewArr = new TextView[rows * 4];
        LinearLayout root = (LinearLayout) findViewById(R.id.rootLayout);
        root.removeAllViews();
        for (int i = 0; i < rows; i++) {
            LinearLayout row = new LinearLayout(LcdAreaActivity.this);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            row.setBackgroundColor(Color.GREEN);

            for (int ii = 0; ii < 4; ii++) {
                textViewArr[count] = new TextView(LcdAreaActivity.this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                        200,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                if (ii == 0)
                    layoutParams.setMargins(0, 1, 2, 1);
                else if (ii == 3)
                    layoutParams.setMargins(2, 1, 0, 1);
                else
                    layoutParams.setMargins(1, 1, 1, 1);

                textViewArr[count].setLayoutParams(layoutParams);
                textViewArr[count].setTextColor(Integer.parseInt(btnContentColor.getText().toString()));
                textViewArr[count].setBackgroundColor(Integer.parseInt(btnContentBG.getText().toString()));
                textViewArr[count].setTextSize(Float.parseFloat(txtContentSize.getText().toString()));
                textViewArr[count].setGravity(Gravity.CENTER);
                textViewArr[count].setText("---");
                row.addView(textViewArr[count]);

                count++;
            }
            root.addView(row);
        }
    }

    public void GetInfoNew(final Boolean isReload) {
        if (!isReload)
            progressDialog.show();
        try {
            url = IPAddress + "/api/serviceapi/GetDayInfo_BV?counters=" + counterIds + "&services=" + serviceIds + "&userId=" + userId + "&getLastFiveNumbers=0";
            JsonObjectRequest jsonRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            lbStatus.setText("");
                            timeRefresh = 0;
                            progressDialog.hide();

                            JSONArray objs = response.optJSONArray("Details");
                            if (objs != null && objs.length() > 0) {
                                for (Integer i = 0; i < textViewArr.length / 2; i++) {
                                    Integer col1 = i * 2, col2 = (i * 2) + 1;
                                    if (i > objs.length() - 1) {
                                        textViewArr[col1].setText("");
                                        textViewArr[col2].setText("");
                                    } else {
                                        try {
                                            JSONObject jsObj = objs.getJSONObject(i);
                                            if (jsObj != null) {
                                                textViewArr[col1].setText(jsObj.getString("TicketNumber"));
                                                textViewArr[col2].setText(jsObj.getString("TableCode"));
                                            }
                                        } catch (JSONException e) {
                                        }
                                    }
                                }
                            }

                            //sounds process
                            JSONArray sounds = response.optJSONArray("Sounds");
                            if (sounds != null && sounds.length() > 0) {
                                for (Integer i = 0; i < sounds.length(); i++) {
                                    try {
                                        String a = sounds.getString(0);
                                        String[] arr = a.split("\\|");
                                        if (arr != null && arr.length > 0) {
                                            for (Integer ii = 0; ii < arr.length; ii++) {
                                                switch (arr[ii].toLowerCase()) {
                                                    case "_0.wav":
                                                        playSounds.add(R.raw._0);
                                                        break;
                                                    case "_1.wav":
                                                        playSounds.add(R.raw._1);
                                                        break;
                                                    case "_2.wav":
                                                        playSounds.add(R.raw._2);
                                                        break;
                                                    case "_3.wav":
                                                        playSounds.add(R.raw._3);
                                                        break;
                                                    case "_4.wav":
                                                        playSounds.add(R.raw._4);
                                                        break;
                                                    case "_5.wav":
                                                        playSounds.add(R.raw._5);
                                                        break;
                                                    case "_6.wav":
                                                        playSounds.add(R.raw._6);
                                                        break;
                                                    case "_7.wav":
                                                        playSounds.add(R.raw._7);
                                                        break;
                                                    case "_8.wav":
                                                        playSounds.add(R.raw._8);
                                                        break;
                                                    case "_9.wav":
                                                        playSounds.add(R.raw._9);
                                                        break;
                                                    case "moi_chuong_bv.wav":
                                                        playSounds.add(R.raw.moi_chuong_bv);
                                                        break;
                                                    case "q1.wav":
                                                        playSounds.add(R.raw.q1);
                                                        break;
                                                    case "q2.wav":
                                                        playSounds.add(R.raw.q2);
                                                        break;
                                                    case "q3.wav":
                                                        playSounds.add(R.raw.q3);
                                                        break;
                                                    case "q4.wav":
                                                        playSounds.add(R.raw.q4);
                                                        break;

                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                    }
                                }

                                if (playSounds.size() > 0 && !isPlaying)
                                    PlaySound();
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
                            lbStatus.setText("Kết nối máy chủ thất bại code:" + error.getMessage());
                            if (timeRefresh == 3)
                                timeRefresh = 0;
                            else {
                                timeRefresh++;
                                try {
                                    Thread.sleep(20000);
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

    //region activity events
    @Override
    protected void onStart() {
        super.onStart();
        mSocket.connect();
        mSocket.on("node-refresh-lcd", onRefresh);
        mSocket.on(Socket.EVENT_CONNECT, onSocketConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onSocketDisconnect);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSocket.disconnect();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        isSmallConfig=true;
        ConfirmSaveDialog dialog = new ConfirmSaveDialog();
        dialog.show(getSupportFragmentManager(),"Confirm");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //endregion

    //region Socket IO
    private void InitSocketIO() {
        try {
            mSocket = IO.socket(IPNodeAddress);
        } catch (URISyntaxException e) {
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
                    lbSocketStatus.setText("Online");
                    lbSocketStatus.setTextColor(Color.BLUE);
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
                    lbSocketStatus.setText("Offline");
                    lbSocketStatus.setTextColor(Color.RED);
                }
            });
        }
    };
    //endregion

    @Override
    public void ApplyTexts(String password) {
        if (password.equalsIgnoreCase("gproadmin"))
            if (!isSmallConfig) {
                intent = new Intent(LcdAreaActivity.this, AppConfigActivity.class);
                intent.putExtra("hold", "1");
                startActivity(intent);
            } else {
                if (setting_panel != null) {
                    setting_panel.setVisibility(View.VISIBLE);
                }
            }
        else
            Toast.makeText(LcdAreaActivity.this, "Mật khẩu quản trị không đúng vui lòng nhập lại.", Toast.LENGTH_LONG).show();
    }
}