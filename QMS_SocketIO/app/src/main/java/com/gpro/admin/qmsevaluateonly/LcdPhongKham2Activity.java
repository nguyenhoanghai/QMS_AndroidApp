package com.gpro.admin.qmsevaluateonly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import yuku.ambilwarna.AmbilWarnaDialog;

public class LcdPhongKham2Activity extends AppCompatActivity implements ConfirmSaveDialog.DialogListener {
    String IPAddress, IPNodeAddress, url, appType, userId = "0", counterIds = "0", serviceIds = "0";
    Integer timeRefresh = 0, processAlign = 0, contentAlign = 0;
    private TextView lbTitle, lbSocketStatus, lbStatus, lbCaption1, lbCaption2, lbProcess1, lbProcess2;
    LinearLayout setting_panel;
    public RequestQueue mRequestQueue = null;
    boolean isPlaying = false,isSmallConfig=true;
    ProgressDialog progressDialog;
    SwipeRefreshLayout wipeToRefresh;
    Intent intent;
    Socket mSocket;
    ArrayList<Integer> playSounds = new ArrayList<>();
    MediaPlayer mediaPlayer = null;
    TextView[] textViewArr;

    Button btnSave, btnCancel, btnTitleColor, btnCaptionBG, btnCaptionColor, btnContentBG, btnContentColor, btnProcessBG, btnProcessColor;
    EditText txtIp, txtSocketIp, txtTitle, txtTitleSize, txtUserId, txtServiceId, txtCounterId, txtCaptionSize, txtContentSize, txtRow, txtProcessSize;
    Integer titleColor = -15859455, captionColor = -15859455, captionBG = -15859455, contentBG = -15859455, contentColor = -15859455, processBG = -15859455, processColor = -15859455;
    SharedPreferences sharedPreferences;
    String[] arrAlign = {"Canh trái", "Canh giữa", "Canh phải"};
    ArrayAdapter alignAdapter;
    Spinner spnProcessAlign, spnContentAlign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lcd_phong_kham2);

        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(LcdPhongKham2Activity.this);
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

        lbCaption1 = (TextView) this.findViewById(R.id.lbCaption1);
        // layoutParams.setMargins(0, 2, 2, 2);
        //lbCaption1.setLayoutParams(layoutParams);

        lbCaption2 = (TextView) this.findViewById(R.id.lbCaption2);
        //layoutParams.setMargins(2, 2, 0, 2);
        //lbCaption2.setLayoutParams(layoutParams);

        lbProcess1 = (TextView) this.findViewById(R.id.lbProcess1);
        //layoutParams.setMargins(0, 2, 2, 2);
        //lbProcess1.setLayoutParams(layoutParams);

        lbProcess2 = (TextView) this.findViewById(R.id.lbProcess2);
        //layoutParams.setMargins(2, 2, 0, 2);
        //lbProcess2.setLayoutParams(layoutParams);

        //  tbContent = (TableLayout) this.findViewById(R.id.tbContent);
        //wipeToRefresh = (SwipeRefreshLayout) this.findViewById(R.id.swipeToRefresh);
        //endregion

        btnSave = (Button) this.findViewById(R.id.btnSave);
        btnCancel = (Button) this.findViewById(R.id.btnCancel);

        txtTitle = (EditText) this.findViewById(R.id.txtTitle);
        txtTitleSize = (EditText) this.findViewById(R.id.txtTitleSize);
        btnTitleColor = (Button) this.findViewById(R.id.btnTitleColor);

        txtCaptionSize = (EditText) this.findViewById(R.id.txtCaptionSize);
        btnCaptionBG = (Button) this.findViewById(R.id.btnCaptionBG);
        btnCaptionColor = (Button) this.findViewById(R.id.btnCaptionColor);

        txtProcessSize = (EditText) this.findViewById(R.id.txtProcessSize);
        btnProcessBG = (Button) this.findViewById(R.id.btnProcessBG);
        btnProcessColor = (Button) this.findViewById(R.id.btnProcessColor);

        txtContentSize = (EditText) this.findViewById(R.id.txtContentSize);
        btnContentBG = (Button) this.findViewById(R.id.btnContentBG);
        btnContentColor = (Button) this.findViewById(R.id.btnContentColor);

        txtIp = (EditText) this.findViewById(R.id.txtIp);
        txtSocketIp = (EditText) this.findViewById(R.id.txtSocketIp);
        txtUserId = (EditText) this.findViewById(R.id.txtUserId);
        txtCounterId = (EditText) this.findViewById(R.id.txtCounterId);
        txtRow = (EditText) this.findViewById(R.id.txtRow);

        setting_panel = (LinearLayout) this.findViewById(R.id.setting_panel);
        setting_panel.setVisibility(View.GONE);

        alignAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrAlign);
        spnContentAlign = (Spinner) this.findViewById(R.id.spinnerContentAlign);
        spnContentAlign.setAdapter(alignAdapter);

        spnProcessAlign = (Spinner) this.findViewById(R.id.spinnerProcessAlign);
        spnProcessAlign.setAdapter(alignAdapter);
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
                    Toast.makeText(LcdPhongKham2Activity.this, "Vui lòng nhập địa chỉ máy chủ.", Toast.LENGTH_LONG).show();
                else if (txtSocketIp.getText().toString() == "")
                    Toast.makeText(LcdPhongKham2Activity.this, "Vui lòng nhập địa chỉ máy chủ socket.", Toast.LENGTH_LONG).show();
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

        btnProcessBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("ProcessBG");
            }
        });
        btnProcessColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("ProcessColor");
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


        spnProcessAlign.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                processAlign = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spnContentAlign.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                contentAlign = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void SaveConfig() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("IS_FIRTS_LAUNCHER", false);
        editor.putString("IP", txtIp.getText().toString());
        editor.putString("SocketIP", txtSocketIp.getText().toString());
        editor.putString("LCD_PK2_UserId", txtUserId.getText().toString());
        editor.putString("LCD_PK2_CounterIds", txtCounterId.getText().toString());
        editor.putString("LCD_PK2_Row", txtRow.getText().toString());

        editor.putString("LCD_PK2_Title", txtTitle.getText().toString());
        editor.putString("LCD_PK2_Title_Size", txtTitleSize.getText().toString());
        editor.putString("LCD_PK2_Title_Color", btnTitleColor.getText().toString());

        editor.putString("LCD_PK2_Caption_Color", btnCaptionColor.getText().toString());
        editor.putString("LCD_PK2_Caption_BG", btnCaptionBG.getText().toString());
        editor.putString("LCD_PK2_Caption_Size", txtCaptionSize.getText().toString());

        editor.putString("LCD_PK2_Process_Color", btnProcessColor.getText().toString());
        editor.putString("LCD_PK2_Process_BG", btnProcessBG.getText().toString());
        editor.putString("LCD_PK2_Process_Size", txtProcessSize.getText().toString());
        editor.putString("LCD_PK2_Process_Align", processAlign.toString());

        editor.putString("LCD_PK2_Content_Color", btnContentColor.getText().toString());
        editor.putString("LCD_PK2_Content_BG", btnContentBG.getText().toString());
        editor.putString("LCD_PK2_Content_Size", txtContentSize.getText().toString());
        editor.putString("LCD_PK2_Content_Align", contentAlign.toString());

        editor.apply();
        setting_panel.setVisibility(View.GONE);
        GetAppConfig();
        InitListView();
        GetInfoNew(true);
    }

    private void ShowConfirmDialog() {
        ConfirmSaveDialog confirm = new ConfirmSaveDialog();
        confirm.show(getSupportFragmentManager(), "Confirm Dialog");
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
            case "ProcessBG":
                _color = processBG;
                break;
            case "ProcessColor":
                _color = processColor;
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
            case "ProcessBG":
                processBG = color;
                btnProcessBG.setTextColor(color);
                btnProcessBG.setText(color + "");
                break;
            case "ProcessColor":
                processColor = color;
                btnProcessColor.setTextColor(color);
                btnProcessColor.setText(color + "");
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
            Intent intent = new Intent(LcdPhongKham2Activity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            switch (appType) {
                case "1":
                    intent = new Intent(LcdPhongKham2Activity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(LcdPhongKham2Activity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case "3":
                    intent = new Intent(LcdPhongKham2Activity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case "4":
                    intent = new Intent(LcdPhongKham2Activity.this, CountersEventActivity.class);
                    startActivity(intent);
                    break;
                case "5":
                    intent = new Intent(LcdPhongKham2Activity.this, PrintTicket_2Activity.class);
                    startActivity(intent);
                    break;
                case "6":
                    intent = new Intent(LcdPhongKham2Activity.this, PrintTicket_3Activity.class);
                    startActivity(intent);
                    break;
                case "7":
                    intent = new Intent(LcdPhongKham2Activity.this, HienThiQuay.class);
                    startActivity(intent);
                    break;
                case "8":
                    intent = new Intent(LcdPhongKham2Activity.this, PrintTicket_4Activity.class);
                    startActivity(intent);
                    break;
                case "10":
                    intent = new Intent(LcdPhongKham2Activity.this, ReceiveSmsActivity.class);
                    startActivity(intent);
                    break;
                case "11":
                    intent = new Intent(LcdPhongKham2Activity.this, LcdPhongKham1Activity.class);
                    startActivity(intent);
                    break;
            }
            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            txtIp.setText(sharedPreferences.getString("IP", "0.0.0.0"));

            IPNodeAddress = "http://" + sharedPreferences.getString("SocketIP", "0.0.0.0");
            txtSocketIp.setText(sharedPreferences.getString("SocketIP", "0.0.0.0"));


            userId = sharedPreferences.getString("LCD_PK2_UserId", "0");
            txtUserId.setText(sharedPreferences.getString("LCD_PK2_UserId", "11"));

            counterIds = sharedPreferences.getString("LCD_PK2_CounterIds", "1,2,3");
            txtCounterId.setText(sharedPreferences.getString("LCD_PK2_CounterIds", "0"));

            lbTitle.setText(sharedPreferences.getString("LCD_PK2_Title", "Title"));
            txtTitle.setText(sharedPreferences.getString("LCD_PK2_Title", "Title"));
            lbTitle.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_PK1_Title_Color", "-15859455")));
            btnTitleColor.setText(sharedPreferences.getString("LCD_PK2_Title_Color", "-15859455"));
            lbTitle.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_PK2_Title_Size", "50")));
            txtTitleSize.setText(sharedPreferences.getString("LCD_PK2_Title_Size", "50"));

            txtRow.setText(sharedPreferences.getString("LCD_PK2_Row", "1"));

            lbCaption1.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Caption_Color", "-15859455")));
            lbCaption2.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Caption_Color", "-15859455")));
            btnCaptionColor.setText(sharedPreferences.getString("LCD_PK2_Caption_Color", "-15859455"));
            lbCaption1.setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Caption_BG", "-15859455")));
            lbCaption2.setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Caption_BG", "-15859455")));
            btnCaptionBG.setText(sharedPreferences.getString("LCD_PK2_Caption_BG", "-15859455"));
            lbCaption1.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_PK2_Caption_Size", "50")));
            lbCaption2.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_PK2_Caption_Size", "50")));
            txtCaptionSize.setText(sharedPreferences.getString("LCD_PK2_Caption_Size", "50"));

            processAlign = Integer.parseInt(sharedPreferences.getString("LCD_PK2_Process_Align", "0"));
            spnProcessAlign.setSelection(processAlign);
            switch (processAlign) {
                case 0:
                    lbProcess1.setGravity(Gravity.LEFT);
                    lbProcess2.setGravity(Gravity.LEFT);
                    break;
                case 1:
                    lbProcess1.setGravity(Gravity.CENTER);
                    lbProcess2.setGravity(Gravity.CENTER);
                    break;
                case 2:
                    lbProcess1.setGravity(Gravity.RIGHT);
                    lbProcess2.setGravity(Gravity.RIGHT);
                    break;
            }
            lbProcess1.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Process_Color", "-15859455")));
            lbProcess1.setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Process_BG", "-15859455")));
            lbProcess1.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_PK2_Process_Size", "50")));

            lbProcess2.setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Process_Color", "-15859455")));
            lbProcess2.setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Process_BG", "-15859455")));
            lbProcess2.setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_PK2_Process_Size", "50")));

            btnProcessColor.setText(sharedPreferences.getString("LCD_PK2_Process_Color", "-15859455"));
            btnProcessBG.setText(sharedPreferences.getString("LCD_PK2_Process_BG", "-15859455"));
            txtProcessSize.setText(sharedPreferences.getString("LCD_PK2_Process_Size", "50"));

            contentAlign = Integer.parseInt(sharedPreferences.getString("LCD_PK2_Content_Align", "0"));
            spnContentAlign.setSelection(contentAlign);
            if (textViewArr != null && textViewArr.length > 0) {
                for (Integer i = 0; i < textViewArr.length; i++) {
                    textViewArr[i].setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Content_Color", "-15859455")));
                    textViewArr[i].setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Content_BG", "-15859455")));
                    textViewArr[i].setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_PK2_Content_Size", "50")));
                    switch (contentAlign) {
                        case 0:
                            textViewArr[i].setGravity(Gravity.LEFT);
                            break;
                        case 1:
                            textViewArr[i].setGravity(Gravity.CENTER);
                            break;
                        case 2:
                            textViewArr[i].setGravity(Gravity.RIGHT);
                            break;
                    }
                }
            }

            txtContentSize.setText(sharedPreferences.getString("LCD_PK2_Content_Size", "50"));
            btnContentBG.setText(sharedPreferences.getString("LCD_PK2_Content_BG", "-15859455"));
            btnContentColor.setText(sharedPreferences.getString("LCD_PK2_Content_Color", "-15859455"));
        }
    }

    private void InitListView() {
        int count = 0, rows = Integer.parseInt(txtRow.getText().toString());
        textViewArr = new TextView[rows * 2];
        LinearLayout root = (LinearLayout) findViewById(R.id.rootLayout);
        root.removeAllViews();
        for (int i = 0; i < rows; i++) {
            LinearLayout row = new LinearLayout(LcdPhongKham2Activity.this);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            //row.setBackgroundColor(Color.GREEN);

            for (int ii = 0; ii < 2; ii++) {
                textViewArr[count] = new TextView(LcdPhongKham2Activity.this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                if (ii == 0)
                    layoutParams.setMargins(0, 0, 2, 2);
                else
                    layoutParams.setMargins(2, 0, 0, 2);

                textViewArr[count].setLayoutParams(layoutParams);
                textViewArr[count].setPadding(15, 15, 15, 15);
                textViewArr[count].setTextColor(Integer.parseInt(btnContentColor.getText().toString()));
                textViewArr[count].setBackgroundColor(Integer.parseInt(btnContentBG.getText().toString()));
                textViewArr[count].setTextSize(Float.parseFloat(txtContentSize.getText().toString()));
                textViewArr[count].setText("---");
                textViewArr[count].setText(count + "");
                switch (contentAlign) {
                    case 0:
                        textViewArr[count].setGravity(Gravity.LEFT);
                        break;
                    case 1:
                        textViewArr[count].setGravity(Gravity.CENTER);
                        break;
                    case 2:
                        textViewArr[count].setGravity(Gravity.RIGHT);
                        break;
                }
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
            url = IPAddress + "/api/serviceapi/GetDayInfo_PK?counters=" + counterIds + "&userId=" + userId;
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
                            JSONObject jsObj = null;
                            if (objs != null && objs.length() > 0) {
                                for (Integer i = 0; i < 2; i++) {
                                    try {
                                        switch (i) {
                                            case 0:
                                                jsObj = objs.getJSONObject(i);
                                                if (jsObj != null) {
                                                    lbProcess1.setText(jsObj.getString("TicketNumber"));
                                                    if (jsObj.getString("TenBN") != "null")
                                                        lbProcess1.setText(jsObj.getString("TicketNumber") + " " + jsObj.getString("TenBN"));

                                                    lbCaption1.setText(jsObj.getString("TableCode"));

                                                    JSONArray lastFiveTickets = jsObj.optJSONArray("LastFiveTickets");
                                                    if (lastFiveTickets != null && lastFiveTickets.length() > 0) {
                                                        Integer index = 0;
                                                        for (Integer ii = 0; ii < textViewArr.length; ii += 2) {
                                                            String strcontent = "---";
                                                            try {
                                                                JSONObject _obj = lastFiveTickets.getJSONObject(index);
                                                                if (jsObj != null) {
                                                                    strcontent = _obj.getString("Ticket");
                                                                    if (_obj.getString("ServiceName") != "null")
                                                                        strcontent += (" " + _obj.getString("ServiceName"));
                                                                }
                                                            } catch (JSONException e) {
                                                            }
                                                            textViewArr[ii].setText(strcontent);
                                                            index++;
                                                        }
                                                    }
                                                } else {
                                                    lbProcess1.setText("---");
                                                    lbCaption1.setText("---");

                                                    for (Integer ii = 0; ii < textViewArr.length; ii += 2) {
                                                        textViewArr[ii].setText("---");
                                                    }
                                                }
                                                break;
                                            case 1:
                                                jsObj = objs.getJSONObject(i);
                                                if (jsObj != null) {
                                                    lbProcess2.setText(jsObj.getString("TicketNumber"));
                                                    if (jsObj.getString("TenBN") != "null")
                                                        lbProcess2.setText(jsObj.getString("TicketNumber") + " " + jsObj.getString("TenBN"));

                                                    lbCaption2.setText(jsObj.getString("TableCode"));

                                                    JSONArray lastFiveTickets = jsObj.optJSONArray("LastFiveTickets");
                                                    if (lastFiveTickets != null && lastFiveTickets.length() > 0) {
                                                        Integer index = 0;
                                                        for (Integer ii = 1; ii < textViewArr.length; ii += 2) {
                                                            String strcontent = "---";
                                                            try {
                                                                JSONObject _obj = lastFiveTickets.getJSONObject(index);
                                                                if (jsObj != null) {
                                                                    strcontent = _obj.getString("Ticket");
                                                                    if (_obj.getString("ServiceName") != "null")
                                                                        strcontent += (" " + _obj.getString("ServiceName"));
                                                                }
                                                            } catch (JSONException e) {
                                                            }
                                                            textViewArr[ii].setText(strcontent);
                                                            index++;
                                                        }
                                                    }
                                                } else {
                                                    lbProcess2.setText("---");
                                                    lbCaption2.setText("---");

                                                    for (Integer ii = 1; ii < textViewArr.length; ii += 2) {
                                                        textViewArr[ii].setText("---");
                                                    }
                                                }
                                                break;
                                        }

                                    } catch (JSONException e) {
                                        switch (i) {
                                            case 0:
                                                lbProcess1.setText("---");
                                                lbCaption1.setText("---");

                                                for (Integer ii = 0; ii < textViewArr.length; ii += 2) {
                                                    textViewArr[ii].setText("---");
                                                }
                                                break;
                                            case 1:
                                                lbProcess2.setText("---");
                                                lbCaption2.setText("---");

                                                for (Integer ii = 1; ii < textViewArr.length; ii += 2) {
                                                    textViewArr[ii].setText("---");
                                                }
                                                break;
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
        if (password.equalsIgnoreCase("gproadmin") ) {
            if (!isSmallConfig) {
                intent = new Intent(LcdPhongKham2Activity.this, AppConfigActivity.class);
                intent.putExtra("hold", "1");
                startActivity(intent);
            } else {
                if (setting_panel != null) {
                    setting_panel.setVisibility(View.VISIBLE);
                }
            }
        }
        else
            Toast.makeText(LcdPhongKham2Activity.this, "Mật khẩu quản trị không đúng vui lòng nhập lại.", Toast.LENGTH_LONG).show();
    }
}