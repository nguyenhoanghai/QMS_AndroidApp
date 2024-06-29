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
    LinearLayout setting_panel, panelStatus;
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
    EditText txtIp, txtSocketIp, txtTitle, txtTitleSize, txtUserId, txtServiceId, txtCounterId, txtCaptionSize, txtContentSize_stt,txtContentSize, txtRow, txtProcessSize_stt,txtProcessSize;
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

        txtProcessSize_stt = (EditText) this.findViewById(R.id.txtProcessSize_stt);
        txtProcessSize = (EditText) this.findViewById(R.id.txtProcessSize);
        btnProcessBG = (Button) this.findViewById(R.id.btnProcessBG);
        btnProcessColor = (Button) this.findViewById(R.id.btnProcessColor);

        txtContentSize = (EditText) this.findViewById(R.id.txtContentSize);
        txtContentSize_stt = (EditText) this.findViewById(R.id.txtContentSize_stt);
        btnContentBG = (Button) this.findViewById(R.id.btnContentBG);
        btnContentColor = (Button) this.findViewById(R.id.btnContentColor);

        txtIp = (EditText) this.findViewById(R.id.txtIp);
        txtSocketIp = (EditText) this.findViewById(R.id.txtSocketIp);
        txtUserId = (EditText) this.findViewById(R.id.txtUserId);
        txtCounterId = (EditText) this.findViewById(R.id.txtCounterId);
        txtRow = (EditText) this.findViewById(R.id.txtRow);

        panelStatus = (LinearLayout) this.findViewById(R.id.panelStatus);
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
                isSmallConfig = true;
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
        editor.putString("LCD_PK2_Process_Size_STT", txtProcessSize_stt.getText().toString());
        editor.putString("LCD_PK2_Process_Size", txtProcessSize.getText().toString());
        editor.putString("LCD_PK2_Process_Align", processAlign.toString());

        editor.putString("LCD_PK2_Content_Color", btnContentColor.getText().toString());
        editor.putString("LCD_PK2_Content_BG", btnContentBG.getText().toString());
        editor.putString("LCD_PK2_Content_Size_STT", txtContentSize_stt.getText().toString());
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
            String _value = "";
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
            _value = sharedPreferences.getString("IP", "138.168.31.246:92");
            IPAddress = "http://" + _value;
            txtIp.setText(_value);

            _value = sharedPreferences.getString("SocketIP", "138.168.31.246:91");
            IPNodeAddress = "http://" + _value;
            txtSocketIp.setText(_value);

_value = sharedPreferences.getString("LCD_PK2_UserId", "0");
            userId = _value;
            txtUserId.setText(_value);

            _value =sharedPreferences.getString("LCD_PK2_CounterIds", "0");
            counterIds = _value;
            txtCounterId.setText(_value);

            _value = sharedPreferences.getString("LCD_PK2_Title", "Bệnh viện đa khoa tỉnh trà vinh");
            lbTitle.setText(_value);
            txtTitle.setText(_value);
            _value = sharedPreferences.getString("LCD_PK1_Title_Color", "-15593237");
            lbTitle.setTextColor(Integer.parseInt(_value));
            btnTitleColor.setText(_value);
            _value = sharedPreferences.getString("LCD_PK2_Title_Size", "50");
            lbTitle.setTextSize(Float.parseFloat(_value));
            txtTitleSize.setText(_value);

            txtRow.setText(sharedPreferences.getString("LCD_PK2_Row", "3"));

            _value = sharedPreferences.getString("LCD_PK2_Caption_Color", "-1046520");
            lbCaption1.setTextColor(Integer.parseInt(_value));
            lbCaption2.setTextColor(Integer.parseInt(_value));
            btnCaptionColor.setText(_value);

            _value = sharedPreferences.getString("LCD_PK2_Caption_BG", "-15859455");
            lbCaption1.setBackgroundColor(Integer.parseInt(_value));
            lbCaption2.setBackgroundColor(Integer.parseInt(_value));
            btnCaptionBG.setText(_value);

            _value = sharedPreferences.getString("LCD_PK2_Caption_Size", "50");
            lbCaption1.setTextSize(Float.parseFloat(_value));
            lbCaption2.setTextSize(Float.parseFloat(_value));
            txtCaptionSize.setText(_value);

            processAlign = Integer.parseInt(sharedPreferences.getString("LCD_PK2_Process_Align", "1"));
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
            _value = sharedPreferences.getString("LCD_PK2_Process_Color", "-1046520");
            lbProcess1.setTextColor(Integer.parseInt(_value));
            lbProcess2.setTextColor(Integer.parseInt(_value));
            btnProcessColor.setText(_value);

            _value = sharedPreferences.getString("LCD_PK2_Process_BG", "-1");
            lbProcess1.setBackgroundColor(Integer.parseInt(_value));
            lbProcess2.setBackgroundColor(Integer.parseInt(_value));
            btnProcessBG.setText(_value);

            _value = sharedPreferences.getString("LCD_PK2_Process_Size_STT", "100");
            lbProcess1.setTextSize(Float.parseFloat(_value));
            txtProcessSize_stt.setText(_value);

            _value = sharedPreferences.getString("LCD_PK2_Process_Size", "100");
            lbProcess2.setTextSize(Float.parseFloat(_value));
            txtProcessSize.setText(_value);

            contentAlign = Integer.parseInt(sharedPreferences.getString("LCD_PK2_Content_Align", "1"));
            spnContentAlign.setSelection(contentAlign);
            if (textViewArr != null && textViewArr.length > 0) {
                for (Integer i = 0; i < textViewArr.length; i++) {
                    textViewArr[i].setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Content_Color", "-15593237")));
                    textViewArr[i].setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_PK2_Content_BG", "-1")));
                    if(i%2==0){
                        //stt
                        textViewArr[i].setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_PK2_Content_Size_STT", "55")));
                    }
                    else {
                        //ten quay
                        textViewArr[i].setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_PK2_Content_Size", "55")));
                    }

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

            txtContentSize.setText(sharedPreferences.getString("LCD_PK2_Content_Size", "55"));
            txtContentSize_stt.setText(sharedPreferences.getString("LCD_PK2_Content_Size_STT", "55"));
            btnContentBG.setText(sharedPreferences.getString("LCD_PK2_Content_BG", "-1"));
            btnContentColor.setText(sharedPreferences.getString("LCD_PK2_Content_Color", "-15593237"));

            InitSocketIO();
            InitListView();

            GetInfoNew(true);
            progressDialog.hide();
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
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            //row.setBackgroundColor(Color.GREEN);

            for (int ii = 0; ii < 2; ii++) {
                textViewArr[count] = new TextView(LcdPhongKham2Activity.this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
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
                if(ii==0){
                    //stt
                    textViewArr[count].setTextSize(Float.parseFloat(txtContentSize_stt.getText().toString()));
                }
                else
                {
                    //ten quay
                    textViewArr[count].setTextSize(Float.parseFloat(txtContentSize.getText().toString()));
                }

                textViewArr[count].setText("---");
                //textViewArr[count].setText(count + "");
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
                                                    else{
                                                        for (Integer ii = 0; ii < textViewArr.length; ii += 2) {
                                                            textViewArr[ii].setText("---");
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
                                                    else{
                                                        for (Integer ii = 1; ii < textViewArr.length; ii += 2) {
                                                            textViewArr[ii].setText("---");
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
                                        String a = sounds.getString(i);
                                        String[] arr = a.split("\\|");
                                        if (arr != null && arr.length > 0) {
                                            for (Integer ii = 0; ii < arr.length; ii++) {
                                                switch (arr[ii].toLowerCase()) {
                                                    case "_0.mp3":  playSounds.add(R.raw._0);   break;
                                                    case "_1.mp3":   playSounds.add(R.raw._1);  break;
                                                    case "_2.mp3":  playSounds.add(R.raw._2);   break;
                                                    case "_3.mp3":  playSounds.add(R.raw._3);     break;
                                                    case "_4.mp3":   playSounds.add(R.raw._4);  break;
                                                    case "_5.mp3":     playSounds.add(R.raw._5);    break;
                                                    case "_6.mp3":     playSounds.add(R.raw._6);  break;
                                                    case "_7.mp3":  playSounds.add(R.raw._7);   break;
                                                    case "_8.mp3":   playSounds.add(R.raw._8);    break;
                                                    case "_9.mp3":   playSounds.add(R.raw._9);   break;
                                                    case "moi_bn.mp3": playSounds.add(R.raw.moi_bn);   break;
                                                    case "moi_qk.mp3":  playSounds.add(R.raw.moi_qk);  break;
                                                    case "arv.mp3":  playSounds.add(R.raw.arv);  break;
                                                    case "bobot.mp3":   playSounds.add(R.raw.bobot);  break;
                                                    case "capcuungoaitru.mp3": playSounds.add(R.raw.capcuungoaitru);  break;
                                                    case "chamcuu_nam.mp3":  playSounds.add(R.raw.chamcuu_nam);  break;
                                                    case "chamcuu_nu.mp3":    playSounds.add(R.raw.chamcuu_nu); break;
                                                    case "chinhhinh.mp3":  playSounds.add(R.raw.chinhhinh);  break;
                                                    case "dalieu.mp3":   playSounds.add(R.raw.dalieu);  break;
                                                    case "dien_trilieu1.mp3": playSounds.add(R.raw.dien_trilieu1);  break;
                                                    case "dien_trilieu2.mp3":  playSounds.add(R.raw.dien_trilieu2);  break;
                                                    case "dodiennao.mp3":    playSounds.add(R.raw.dodiennao); break;
                                                    case "dodientim.mp3":  playSounds.add(R.raw.dodientim);  break;
                                                    case "dodientim_khua.mp3":   playSounds.add(R.raw.dodientim_khua);  break;
                                                    case "giamdinhykhoa.mp3": playSounds.add(R.raw.giamdinhykhoa);  break;
                                                    case "hanhchanh.mp3":  playSounds.add(R.raw.hanhchanh);  break;
                                                    case "kham_a1.mp3":    playSounds.add(R.raw.kham_a1); break;
                                                    case "kham_a2.mp3":  playSounds.add(R.raw.kham_a2);  break;
                                                    case "kham_a3.mp3":   playSounds.add(R.raw.kham_a3);  break;
                                                    case "kham_rhm.mp3": playSounds.add(R.raw.kham_rhm);  break;
                                                    case "kham_vatlytrilieu.mp3":  playSounds.add(R.raw.kham_vatlytrilieu);  break;
                                                    case "khammat.mp3":    playSounds.add(R.raw.khammat); break;
                                                    case "khamsanloc.mp3":  playSounds.add(R.raw.khamsanloc);  break;
                                                    case "khamsuckhoe.mp3":   playSounds.add(R.raw.khamsuckhoe);  break;
                                                    case "khamtieuphau.mp3": playSounds.add(R.raw.khamtieuphau);  break;
                                                    case "labo.mp3":  playSounds.add(R.raw.labo);  break;
                                                    case "laokhoa1.mp3":    playSounds.add(R.raw.laokhoa1); break;
                                                    case "laokhoa2.mp3":  playSounds.add(R.raw.laokhoa2);  break;
                                                    case "laser.mp3":   playSounds.add(R.raw.laser);  break;
                                                    case "laser_cham.mp3": playSounds.add(R.raw.laser_cham);  break;
                                                    case "laser_daymat.mp3":  playSounds.add(R.raw.laser_daymat);  break;
                                                    case "laymau.mp3":    playSounds.add(R.raw.laymau); break;
                                                    case "maykeogiancotsong.mp3":  playSounds.add(R.raw.maykeogiancotsong);  break;
                                                    case "ngoai_chanthuong.mp3":   playSounds.add(R.raw.ngoai_chanthuong);  break;
                                                    case "ngoai_thankinh.mp3": playSounds.add(R.raw.ngoai_thankinh);  break;
                                                    case "ngoai_tietnieu.mp3":  playSounds.add(R.raw.ngoai_tietnieu);  break;
                                                    case "ngoai_tonghop.mp3":    playSounds.add(R.raw.ngoai_tonghop); break;
                                                    case "ngoai_ungbuou.mp3":  playSounds.add(R.raw.ngoai_ungbuou);  break;
                                                    case "nhanbenh_khua.mp3":   playSounds.add(R.raw.nhanbenh_khua);  break;
                                                    case "nhanbenh_taptrung.mp3": playSounds.add(R.raw.nhanbenh_taptrung);  break;
                                                    case "nhanbenh_uutien.mp3":  playSounds.add(R.raw.nhanbenh_uutien);  break;
                                                    case "nhiem.mp3":    playSounds.add(R.raw.nhiem); break;
                                                    case "nhorang.mp3":  playSounds.add(R.raw.nhorang);  break;
                                                    case "noisoi.mp3":   playSounds.add(R.raw.noisoi);  break;
                                                    case "noisoi_tmh.mp3": playSounds.add(R.raw.noisoi_tmh);  break;
                                                    case "noitiet1.mp3":  playSounds.add(R.raw.noitiet1);  break;
                                                    case "noitiet2.mp3":    playSounds.add(R.raw.noitiet2); break;
                                                    case "noitongquat.mp3":  playSounds.add(R.raw.noitongquat);  break;
                                                    case "noptoa_bhyt.mp3":   playSounds.add(R.raw.noptoa_bhyt);  break;
                                                    case "noptoa_bhyt_uutien.mp3": playSounds.add(R.raw.noptoa_bhyt_uutien);  break;
                                                    case "noptoathuoc.mp3":  playSounds.add(R.raw.noptoathuoc);  break;
                                                    case "phatthuoc.mp3":    playSounds.add(R.raw.phatthuoc); break;
                                                    case "phatthuoc_bhyt.mp3":  playSounds.add(R.raw.phatthuoc_bhyt);  break;
                                                    case "phatthuoc_khoaa.mp3":   playSounds.add(R.raw.phatthuoc_khoaa);  break;
                                                    case "phongxquang.mp3": playSounds.add(R.raw.phongxquang);  break;
                                                    case "sieuam.mp3":  playSounds.add(R.raw.sieuam);  break;
                                                    case "taimuihong1.mp3":    playSounds.add(R.raw.taimuihong1); break;
                                                    case "taimuihong2.mp3":  playSounds.add(R.raw.taimuihong2);  break;
                                                    case "taimuihong3.mp3":   playSounds.add(R.raw.taimuihong3);  break;
                                                    case "taimuihong4.mp3": playSounds.add(R.raw.taimuihong4);  break;
                                                    case "taimuihong5.mp3":  playSounds.add(R.raw.taimuihong5);  break;
                                                    case "tamthan.mp3":    playSounds.add(R.raw.tamthan); break;
                                                    case "thankinh.mp3":  playSounds.add(R.raw.thankinh);  break;
                                                    case "thiluc_thukinh.mp3":   playSounds.add(R.raw.thiluc_thukinh);  break;
                                                    case "thuycham_nam.mp3": playSounds.add(R.raw.thuycham_nam);  break;
                                                    case "thuycham_nu.mp3":  playSounds.add(R.raw.thuycham_nu);  break;
                                                    case "tiepbenh1.mp3":    playSounds.add(R.raw.tiepbenh1); break;
                                                    case "tiepbenh2.mp3":  playSounds.add(R.raw.tiepbenh2);  break;
                                                    case "tiepbenh3.mp3":   playSounds.add(R.raw.tiepbenh3);  break;
                                                    case "tiepbenh4.mp3": playSounds.add(R.raw.tiepbenh4);  break;
                                                    case "tiepbenh5.mp3":  playSounds.add(R.raw.tiepbenh5);  break;
                                                    case "tiepbenh6.mp3":    playSounds.add(R.raw.tiepbenh6); break;
                                                    case "tiepbenh7.mp3":  playSounds.add(R.raw.tiepbenh7);  break;
                                                    case "tieuhoaganmat.mp3":   playSounds.add(R.raw.tieuhoaganmat);  break;
                                                    case "tieuphau.mp3": playSounds.add(R.raw.tieuphau);  break;
                                                    case "tieuphau_rhm.mp3":  playSounds.add(R.raw.tieuphau_rhm);  break;
                                                    case "timmach1.mp3":    playSounds.add(R.raw.timmach1); break;
                                                    case "timmach2.mp3":  playSounds.add(R.raw.timmach2);  break;
                                                    case "trakqxquang.mp3":   playSounds.add(R.raw.trakqxquang);  break;
                                                    case "tranoisoi.mp3": playSounds.add(R.raw.tranoisoi);  break;
                                                    case "trasieuam.mp3":  playSounds.add(R.raw.trasieuam);  break;
                                                    case "truongkhoa.mp3":    playSounds.add(R.raw.truongkhoa); break;
                                                    case "tutruongvasongngan.mp3":  playSounds.add(R.raw.tutruongvasongngan);  break;
                                                    case "tuvan_gdsk.mp3":   playSounds.add(R.raw.tuvan_gdsk);  break;
                                                    case "vandong_trilieu.mp3": playSounds.add(R.raw.vandong_trilieu);  break;
                                                    case "vatlytrilieu.mp3":  playSounds.add(R.raw.vatlytrilieu);  break;
                                                    case "vienphi1.mp3":    playSounds.add(R.raw.vienphi1); break;
                                                    case "vienphi2.mp3":  playSounds.add(R.raw.vienphi2);  break;
                                                    case "xquang.mp3":   playSounds.add(R.raw.xquang);  break;
                                                    case "yhct1.mp3": playSounds.add(R.raw.yhct1);  break;
                                                    case "yhct2.mp3":  playSounds.add(R.raw.yhct2);  break;

                                                    case "q1.mp3":  playSounds.add(R.raw.q1);  break;
                                                    case "q2.mp3":  playSounds.add(R.raw.q2);  break;
                                                    case "q3.mp3":  playSounds.add(R.raw.q3);  break;
                                                    case "q4.mp3":  playSounds.add(R.raw.q4);  break;
                                                    case "q5.mp3":  playSounds.add(R.raw.q5);  break;
                                                    case "q6.mp3":  playSounds.add(R.raw.q6);  break;
                                                    case "q7.mp3":  playSounds.add(R.raw.q7);  break;
                                                    case "q8.mp3":  playSounds.add(R.raw.q8);  break;
                                                    case "q9.mp3":  playSounds.add(R.raw.q9);  break;
                                                    case "coxuongkhop.mp3":  playSounds.add(R.raw.coxuongkhop);  break;
                                                    case "laokhoa.mp3":  playSounds.add(R.raw.laokhoa);  break;
                                                    case "timmach.mp3":  playSounds.add(R.raw.timmach);  break;
                                                    case "yhct4.mp3":  playSounds.add(R.raw.yhct4);  break;
                                                    case "yhct5.mp3":  playSounds.add(R.raw.yhct5);  break;
                                                    case "yhct6.mp3":  playSounds.add(R.raw.yhct6);  break;
                                                    case "sieuam_ct1.mp3":  playSounds.add(R.raw.sieuam_ct1);  break;
                                                    case "sieuam_ct2.mp3":  playSounds.add(R.raw.sieuam_ct2);  break;
                                                    case "sieuam3.mp3":  playSounds.add(R.raw.sieuam3);  break;
                                                    case "sieuam4.mp3":  playSounds.add(R.raw.sieuam4);  break;
                                                    case "sieuam5.mp3":  playSounds.add(R.raw.sieuam5);  break;
                                                    case "sieuam6.mp3":  playSounds.add(R.raw.sieuam6);  break;
                                                    case "sieuam7.mp3":  playSounds.add(R.raw.sieuam7);  break;
                                                    case "sieuam8.mp3":  playSounds.add(R.raw.sieuam8);  break;
                                                    case "sieuam9.mp3":  playSounds.add(R.raw.sieuam9);  break;
                                                    case "sieuam10.mp3":  playSounds.add(R.raw.sieuam10);  break;
                                                    case "xquangct1.mp3":  playSounds.add(R.raw.xquangct1);  break;
                                                    case "xquangct2.mp3":  playSounds.add(R.raw.xquangct2);  break;
                                                    case "xquangct3.mp3":  playSounds.add(R.raw.xquangct3);  break;
                                                    case "xquangth7.mp3":  playSounds.add(R.raw.xquangth7);  break;
                                                    case "xquangth8.mp3":  playSounds.add(R.raw.xquangth8);  break;
                                                    case "xquangth9.mp3":  playSounds.add(R.raw.xquangth9);  break;
                                                    case "xquangth10.mp3":  playSounds.add(R.raw.xquangth10);  break;
                                                    case "xquangth11.mp3":  playSounds.add(R.raw.xquangth11);  break;
                                                    case "xquangth12.mp3":  playSounds.add(R.raw.xquangth12);  break;
                                                    case "scanner1.mp3":  playSounds.add(R.raw.scanner1);  break;
                                                    case "scanner2.mp3":  playSounds.add(R.raw.scanner2);  break;
                                                    case "scanner3.mp3":  playSounds.add(R.raw.scanner3);  break;
                                                    case "mri1.mp3":  playSounds.add(R.raw.mri1);  break;
                                                    case "mri2.mp3":  playSounds.add(R.raw.mri2);  break;
                                                    case "mri3.mp3":  playSounds.add(R.raw.mri3);  break;
                                                    case "mri4.mp3":  playSounds.add(R.raw.mri4);  break;
                                                    case "mri5.mp3":  playSounds.add(R.raw.mri5);  break;
                                                    case "khammat_01.mp3":  playSounds.add(R.raw.khammat_01);  break;
                                                    case "khammat_02.mp3":  playSounds.add(R.raw.khammat_02);  break;
                                                    case "khammat_03.mp3":  playSounds.add(R.raw.khammat_03);  break;
                                                    case "phatthuoc_ko_ut.mp3":  playSounds.add(R.raw.phatthuoc_ko_ut);  break;
                                                    case "phatthuoc_uutien.mp3":  playSounds.add(R.raw.phatthuoc_uutien);  break;
                                                    case "doloangxuong.mp3":  playSounds.add(R.raw.doloangxuong);  break;
                                                    case "nhiem1.mp3":  playSounds.add(R.raw.nhiem1);  break;
                                                    case "nhiem2.mp3":  playSounds.add(R.raw.nhiem2);  break;
                                                    case "timmachcanthiep.mp3":  playSounds.add(R.raw.timmachcanthiep);  break;
                                                    case "viemgan.mp3":  playSounds.add(R.raw.viemgan);  break;
                                                    case "xquangth13.mp3":  playSounds.add(R.raw.xquangth13);  break;
                                                    case "scanner64.mp3":  playSounds.add(R.raw.scanner64);  break;
                                                    case "scanner128.mp3":  playSounds.add(R.raw.scanner128);  break;
                                                    case "scanner256.mp3":  playSounds.add(R.raw.scanner256);  break;
                                                    case "mri.mp3":  playSounds.add(R.raw.mri);  break;
                                                    case "noisoi_tatrang1.mp3":  playSounds.add(R.raw.noisoi_tatrang1);  break;
                                                    case "noisoi_tatrang2.mp3":  playSounds.add(R.raw.noisoi_tatrang2);  break;
                                                    case "noisoi_tatrang3.mp3":  playSounds.add(R.raw.noisoi_tatrang3);  break;
                                                    case "_0_hc.mp3":  playSounds.add(R.raw._0_hc);   break;
                                                    case "_1_hc.mp3":   playSounds.add(R.raw._1_hc);  break;
                                                    case "_2_hc.mp3":  playSounds.add(R.raw._2_hc);   break;
                                                    case "_3_hc.mp3":  playSounds.add(R.raw._3_hc);     break;
                                                    case "_4_hc.mp3":   playSounds.add(R.raw._4_hc);  break;
                                                    case "_5_hc.mp3":     playSounds.add(R.raw._5_hc);    break;
                                                    case "_6_hc.mp3":     playSounds.add(R.raw._6_hc);  break;
                                                    case "_7_hc.mp3":  playSounds.add(R.raw._7_hc);   break;
                                                    case "_8_hc.mp3":   playSounds.add(R.raw._8_hc);    break;
                                                    case "_9_hc.mp3":   playSounds.add(R.raw._9_hc);   break;
                                                    case "moi_hc.mp3": playSounds.add(R.raw.moi_hc);   break;
                                                    case "q1_hc.mp3": playSounds.add(R.raw.q1_hc);   break;
                                                    case "q2_hc.mp3": playSounds.add(R.raw.q2_hc);   break;
                                                    case "q3_hc.mp3": playSounds.add(R.raw.q3_hc);   break;
                                                    case "q4_hc.mp3": playSounds.add(R.raw.q4_hc);   break;
                                                    case "q5_hc.mp3": playSounds.add(R.raw.q5_hc);   break;
                                                    case "q6_hc.mp3": playSounds.add(R.raw.q6_hc);   break;
                                                    case "q7_hc.mp3": playSounds.add(R.raw.q7_hc);   break;
                                                    case "q8_hc.mp3": playSounds.add(R.raw.q8_hc);   break;
                                                    case "q9_hc.mp3": playSounds.add(R.raw.q9_hc);   break;
                                                    case "q10_hc.mp3": playSounds.add(R.raw.q10_hc);   break;
                                                    case "q11_hc.mp3": playSounds.add(R.raw.q11_hc);   break;
                                                    case "q12_hc.mp3": playSounds.add(R.raw.q12_hc);   break;
                                                    case "q13_hc.mp3": playSounds.add(R.raw.q13_hc);   break;
                                                    case "q14_hc.mp3": playSounds.add(R.raw.q14_hc);   break;
                                                    case "q15_hc.mp3": playSounds.add(R.raw.q15_hc);   break;
                                                    case "q16_hc.mp3": playSounds.add(R.raw.q16_hc);   break;
                                                    case "q17_hc.mp3": playSounds.add(R.raw.q17_hc);   break;
                                                    case "q18_hc.mp3": playSounds.add(R.raw.q18_hc);   break;
                                                    case "q19_hc.mp3": playSounds.add(R.raw.q19_hc);   break;
                                                    case "q20_hc.mp3": playSounds.add(R.raw.q20_hc);   break;
                                                    case "q21_hc.mp3": playSounds.add(R.raw.q21_hc);   break;
                                                    case "q22_hc.mp3": playSounds.add(R.raw.q22_hc);   break;
                                                    case "q23_hc.mp3": playSounds.add(R.raw.q23_hc);   break;
                                                    case "q24_hc.mp3": playSounds.add(R.raw.q24_hc);   break;
                                                    case "q25_hc.mp3": playSounds.add(R.raw.q25_hc);   break;
                                                    case "q26_hc.mp3": playSounds.add(R.raw.q26_hc);   break;
                                                    case "q27_hc.mp3": playSounds.add(R.raw.q27_hc);   break;
                                                    case "q28_hc.mp3": playSounds.add(R.raw.q28_hc);   break;
                                                    case "q29_hc.mp3": playSounds.add(R.raw.q29_hc);   break;
                                                    case "q30_hc.mp3": playSounds.add(R.raw.q30_hc);   break;
                                                    case "q31_hc.mp3": playSounds.add(R.raw.q31_hc);   break;
                                                    case "q32_hc.mp3": playSounds.add(R.raw.q32_hc);   break;
                                                    case "q33_hc.mp3": playSounds.add(R.raw.q33_hc);   break;
                                                    case "q34_hc.mp3": playSounds.add(R.raw.q34_hc);   break;
                                                    case "q35_hc.mp3": playSounds.add(R.raw.q35_hc);   break;
                                                    case "q36_hc.mp3": playSounds.add(R.raw.q36_hc);   break;
                                                    case "q37_hc.mp3": playSounds.add(R.raw.q37_hc);   break;
                                                    case "q38_hc.mp3": playSounds.add(R.raw.q38_hc);   break;
                                                    case "q39_hc.mp3": playSounds.add(R.raw.q39_hc);   break;
                                                    case "q40_hc.mp3": playSounds.add(R.raw.q40_hc);   break;
                                                    case "q41_hc.mp3": playSounds.add(R.raw.q41_hc);   break;
                                                    case "q42_hc.mp3": playSounds.add(R.raw.q42_hc);   break;
                                                    case "q43_hc.mp3": playSounds.add(R.raw.q43_hc);   break;
                                                    case "q44_hc.mp3": playSounds.add(R.raw.q44_hc);   break;
                                                    case "q45_hc.mp3": playSounds.add(R.raw.q45_hc);   break;
                                                    case "q46_hc.mp3": playSounds.add(R.raw.q46_hc);   break;
                                                    case "q47_hc.mp3": playSounds.add(R.raw.q47_hc);   break;
                                                    case "q48_hc.mp3": playSounds.add(R.raw.q48_hc);   break;
                                                    case "q49_hc.mp3": playSounds.add(R.raw.q49_hc);   break;
                                                    case "q50_hc.mp3": playSounds.add(R.raw.q50_hc);   break;
                                                    case "khoa_hc.mp3": playSounds.add(R.raw.khoa_hc);   break;
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
                                if(wipeToRefresh!= null)
                                wipeToRefresh.setRefreshing(false);
                            }
                            lbStatus.setText("Kết nối máy chủ thất bại code:" + error.getMessage());
                            if (timeRefresh == 3)
                                timeRefresh = 0;
                            else {
                                timeRefresh++;
                                try {
                                    Thread.sleep(5000);
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
        if(mSocket != null) {
            mSocket.connect();
            mSocket.on("node-refresh-lcd", onRefresh);
            mSocket.on(Socket.EVENT_CONNECT, onSocketConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onSocketDisconnect);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mSocket != null)
        mSocket.disconnect();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isSmallConfig=false;
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
                    lbSocketStatus.setText("Id thiết bị: "+mSocket.id());
                    lbSocketStatus.setTextColor(Color.WHITE);
                    panelStatus.setBackgroundColor(Color.BLUE);
                    //socketid|counterid|serviceId|userid
                    mSocket.emit("android-send-device-info",mSocket.id()+"|"+counterIds+"|"+serviceIds+"|"+userId);
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
                    lbSocketStatus.setText("Thiết bị không kết nối được máy chủ");
                    lbSocketStatus.setTextColor(Color.WHITE);
                    panelStatus.setBackgroundColor(Color.RED);
                }
            });
        }
    };
    //endregion

    @Override
    public void ApplyTexts(String password) {
        if (password.equalsIgnoreCase("123") ) {
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