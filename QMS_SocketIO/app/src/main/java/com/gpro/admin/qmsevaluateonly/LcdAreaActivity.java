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
    LinearLayout setting_panel, panelStatus;
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
    EditText txtIp, txtSocketIp, txtTitle, txtTitleSize, txtUserId, txtServiceId, txtCounterId, txtCaptionSize, txtContentSize,txtContentSize_stt, txtRow;
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

         /*
         //TODO TEST SOUNDS
        playSounds.add(R.raw.moi_qk);
        playSounds.add(R.raw._1);
        playSounds.add(R.raw._7);
        playSounds.add(R.raw._5);
        playSounds.add(R.raw._9);
        playSounds.add(R.raw.q3);
        playSounds.add(R.raw.moi_bn_);
        playSounds.add(R.raw._1);
        playSounds.add(R.raw._0);
        playSounds.add(R.raw._5);
        playSounds.add(R.raw._9);
        playSounds.add(R.raw.q1);

        if (playSounds.size() > 0 && !isPlaying)
            PlaySound();
        */
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
        txtContentSize_stt = (EditText) this.findViewById(R.id.txtContentSize_stt);
        txtRow = (EditText) this.findViewById(R.id.txtRow);

        panelStatus = (LinearLayout) this.findViewById(R.id.panelStatus);
        setting_panel = (LinearLayout) this.findViewById(R.id.setting_panel);
        setting_panel.setVisibility(View.GONE);
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
        editor.putString("LCD_Area_Content_Size_STT", txtContentSize_stt.getText().toString());
        editor.putString("LCD_Area_Row", txtRow.getText().toString());
        editor.apply();
        setting_panel.setVisibility(View.GONE);
        GetAppConfig();
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
             Intent _intent = new Intent(LcdAreaActivity.this, AppConfigActivity.class);
             startActivity(_intent);
         } else {
            appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            if(!appType.equals( "11") ) {
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
                        intent = new Intent(LcdAreaActivity.this, LcdPhongKham2Activity.class);
                        startActivity(intent);
                        break;
                    case "13":
                        intent = new Intent(LcdAreaActivity.this, LcdPhongKham1Activity.class);
                        startActivity(intent);
                        break;
                    case "14":
                        intent = new Intent(LcdAreaActivity.this, TraKQ_SocketActivity.class);
                        startActivity(intent);
                        break;
                }
            }
            else {
                setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
                String _value = "";
                _value = sharedPreferences.getString("IP", "138.168.31.246:92");
                IPAddress = "http://" + _value;
                txtIp.setText(_value);

                _value = sharedPreferences.getString("SocketIP", "138.168.31.246:91");
                IPNodeAddress = "http://" + _value;
                txtSocketIp.setText(_value);

                _value = sharedPreferences.getString("LCD_Area_UserId", "0");
                userId = _value;
                txtUserId.setText(_value);

                _value = sharedPreferences.getString("LCD_Area_CounterIds", "0");
                counterIds = _value;
                txtCounterId.setText(_value);

                _value = sharedPreferences.getString("LCD_Area_ServiceIds", "0");
                serviceIds = _value;
                txtServiceId.setText(_value);

                _value = sharedPreferences.getString("LCD_Area_Title", "Bệnh viện đa khoa tỉnh trà vinh");
                lbTitle.setText(_value);
                txtTitle.setText(_value);

                _value = sharedPreferences.getString("LCD_Area_Title_Color", "-15593237");
                lbTitle.setTextColor(Integer.parseInt(_value));
                btnTitleColor.setText(_value);
                _value = sharedPreferences.getString("LCD_Area_Title_Size", "50");
                lbTitle.setTextSize(Float.parseFloat(_value));
                txtTitleSize.setText(_value);
                txtRow.setText(sharedPreferences.getString("LCD_Area_Row", "4"));

                _value = sharedPreferences.getString("LCD_Area_Caption_Color", "-1046520");
                lbCaption1.setTextColor(Integer.parseInt(_value));
                lbCaption2.setTextColor(Integer.parseInt(_value));
                lbCaption3.setTextColor(Integer.parseInt(_value));
                lbCaption4.setTextColor(Integer.parseInt(_value));
                btnCaptionColor.setText(_value);

                _value = sharedPreferences.getString("LCD_Area_Caption_BG", "-15859455");
                lbCaption1.setBackgroundColor(Integer.parseInt(_value));
                lbCaption2.setBackgroundColor(Integer.parseInt(_value));
                lbCaption3.setBackgroundColor(Integer.parseInt(_value));
                lbCaption4.setBackgroundColor(Integer.parseInt(_value));
                btnCaptionBG.setText(_value);

                _value = sharedPreferences.getString("LCD_Area_Caption_Size", "35");
                lbCaption1.setTextSize(Float.parseFloat(_value));
                lbCaption2.setTextSize(Float.parseFloat(_value));
                lbCaption3.setTextSize(Float.parseFloat(_value));
                lbCaption4.setTextSize(Float.parseFloat(_value));
                txtCaptionSize.setText(_value);

                if (textViewArr != null && textViewArr.length > 0) {
                    for (Integer i = 0; i < textViewArr.length; i++) {
                        textViewArr[i].setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Content_Color", "-15593237")));
                        textViewArr[i].setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_Area_Content_BG", "-1")));
                        if (i % 2 == 0) {
                            //stt
                            textViewArr[i].setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_Area_Content_Size_STT", "70")));
                        } else {
                            //ten quay
                            textViewArr[i].setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_Area_Content_Size", "40")));
                        }
                    }
                }

                txtContentSize.setText(sharedPreferences.getString("LCD_Area_Content_Size", "40"));
                txtContentSize_stt.setText(sharedPreferences.getString("LCD_Area_Content_Size_STT", "70"));
                btnContentBG.setText(sharedPreferences.getString("LCD_Area_Content_BG", "-1"));
                btnContentColor.setText(sharedPreferences.getString("LCD_Area_Content_Color", "-15593237"));
                InitSocketIO();
                InitListView();
                GetInfoNew(true);
            }
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
            row.setBackgroundColor(Color.GRAY);

            for (int ii = 0; ii < 4; ii++) {
                textViewArr[count] = new TextView(LcdAreaActivity.this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                        200,
                        ViewGroup.LayoutParams.MATCH_PARENT,
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
                if(ii == 0 || ii == 2){
                    //STT
                    textViewArr[count].setTextSize(Float.parseFloat(txtContentSize_stt.getText().toString()));
                }
                else
                {
                    //ten quay
                    textViewArr[count].setTextSize(Float.parseFloat(txtContentSize.getText().toString()));
                }

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
        if(!isPlaying)
            playSounds.clear();

        //lbSocketStatus.setText("before: "+ playSounds.size()+ " files");
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
                                                    case "phatthuoc_ko_ut.mp3":  playSounds.add(R.raw.phatthuoc_ko_ut);  break;
                                                    case "phatthuoc_uutien.mp3":  playSounds.add(R.raw.phatthuoc_uutien);  break;
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
                                                    case "coxuongkhop.mp3":  playSounds.add(R.raw.coxuongkhop);  break;
                                                    case "laokhoa.mp3":  playSounds.add(R.raw.laokhoa);  break;
                                                    case "timmach.mp3":  playSounds.add(R.raw.timmach);  break;
                                                    case "yhct4.mp3":  playSounds.add(R.raw.yhct4);  break;
                                                    case "yhct5.mp3":  playSounds.add(R.raw.yhct5);  break;
                                                    case "yhct6.mp3":  playSounds.add(R.raw.yhct6);  break;
                                                    case "sieuam1.mp3":  playSounds.add(R.raw.sieuam1);  break;
                                                    case "sieuam2.mp3":  playSounds.add(R.raw.sieuam2);  break;
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
                                                }
  }
                                        }
                                    } catch (Exception e) {
                                    }
                                }
//lbSocketStatus.setText("after :"+sounds.length() +" -- "+playSounds.size());
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
                                if(wipeToRefresh != null)
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
        if(mSocket!= null){
            mSocket.disconnect();
        }
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
        } catch (Exception e) {
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
        if (password.equalsIgnoreCase("123"))
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