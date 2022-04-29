package com.gpro.admin.qmsevaluateonly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import yuku.ambilwarna.AmbilWarnaDialog;

public class TraKQ_SocketActivity extends AppCompatActivity implements ConfirmSaveDialog.DialogListener {

    String IPAddress, IPNodeAddress,  appType ;
    LinearLayout setting_panel , statusPanel;
    public RequestQueue mRequestQueue = null;
    ProgressDialog progressDialog;
    SwipeRefreshLayout wipeToRefresh;
    Intent intent;
    Socket mSocket;
    TextView[] nameArr, infoArr;
    Button btnSave, btnCancel, btnTitleColor, btnCaptionBG, btnCaptionColor, btnContentBG, btnContentColor,btnNameBG,btnNameColor ;
    EditText txtIp, txtSocketIp, txtTitle, txtTitleSize,  txtCaptionSize, txtContentSize, txtRow, txtNameSize,txtInterval ;
    Integer titleColor = -15859455, captionColor = -15859455, captionBG = -15859455, contentBG = -15859455, contentColor = -15859455, nameBG = -15859455, nameColor = -15859455, startIndex = 0;
    SharedPreferences sharedPreferences;
    String[] arrAlign = {"Canh trái", "Canh giữa", "Canh phải"};
    ArrayAdapter alignAdapter;
    Spinner spinnerNameAlign, spnContentAlign;
    private TextView lbTitle, lbSocketStatus, lbStatus, lbCaption1 ;
    boolean isSmallConfig=true;
    Integer timeRefresh = 0, nameAlign = 0, contentAlign = 0;
    JSONArray showDatas = null;
    Thread threadChange = null;
    Handler handler = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();
        setContentView(R.layout.activity_tra_k_q__socket);

        progressDialog = new ProgressDialog(TraKQ_SocketActivity.this);
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
        progressDialog.hide();
        handler = new Handler();
//StartHandler();
       // BindNhay();
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
        // lbCaption1.setLayoutParams(layoutParams);

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

         txtNameSize = (EditText) this.findViewById(R.id.txtNameSize);
         btnNameBG = (Button) this.findViewById(R.id.btnNameBG);
        btnNameColor = (Button) this.findViewById(R.id.btnNameColor);

        txtContentSize = (EditText) this.findViewById(R.id.txtContentSize);
        btnContentBG = (Button) this.findViewById(R.id.btnContentBG);
        btnContentColor = (Button) this.findViewById(R.id.btnContentColor);

        txtIp = (EditText) this.findViewById(R.id.txtIp);
        txtSocketIp = (EditText) this.findViewById(R.id.txtSocketIp);

        txtRow = (EditText) this.findViewById(R.id.txtRow);
        txtInterval = (EditText) this.findViewById(R.id.txtInterval);

        statusPanel = (LinearLayout) this.findViewById(R.id.statusPanel);

        setting_panel = (LinearLayout) this.findViewById(R.id.setting_panel);
        setting_panel.setVisibility(View.GONE);

        alignAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrAlign);
        spnContentAlign = (Spinner) this.findViewById(R.id.spinnerContentAlign);
        spnContentAlign.setAdapter(alignAdapter);

        spinnerNameAlign = (Spinner) this.findViewById(R.id.spinnerNameAlign);
        spinnerNameAlign.setAdapter(alignAdapter);
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
                    Toast.makeText(TraKQ_SocketActivity.this, "Vui lòng nhập địa chỉ máy chủ.", Toast.LENGTH_LONG).show();
                else if (txtSocketIp.getText().toString() == "")
                    Toast.makeText(TraKQ_SocketActivity.this, "Vui lòng nhập địa chỉ máy chủ socket.", Toast.LENGTH_LONG).show();
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

        btnNameBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("NameBG");
            }
        });
        btnNameColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("NameColor");
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


        spinnerNameAlign.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                nameAlign = position;
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

    private  void  SaveConfig(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("IS_FIRTS_LAUNCHER", false);
        editor.putString("IP", txtIp.getText().toString());
        editor.putString("SocketIP", txtSocketIp.getText().toString());
        editor.putString("LCD_TraKQ_Row", txtRow.getText().toString());

        editor.putString("LCD_TraKQ_Title", txtTitle.getText().toString());
        editor.putString("LCD_TraKQ_Title_Size", txtTitleSize.getText().toString());
        editor.putString("LCD_TraKQ_Title_Color", btnTitleColor.getText().toString());

        editor.putString("LCD_TraKQ_Caption_Color", btnCaptionColor.getText().toString());
        editor.putString("LCD_TraKQ_Caption_BG", btnCaptionBG.getText().toString());
        editor.putString("LCD_TraKQ_Caption_Size", txtCaptionSize.getText().toString());

         editor.putString("LCD_TraKQ_Name_Color", btnNameColor.getText().toString());
         editor.putString("LCD_TraKQ_Name_BG", btnNameBG.getText().toString());
         editor.putString("LCD_TraKQ_Name_Size", txtNameSize.getText().toString());
        editor.putString("LCD_TraKQ_Name_Align", nameAlign.toString());

        editor.putString("LCD_TraKQ_Content_Color", btnContentColor.getText().toString());
        editor.putString("LCD_TraKQ_Content_BG", btnContentBG.getText().toString());
        editor.putString("LCD_TraKQ_Content_Size", txtContentSize.getText().toString());
        editor.putString("LCD_TraKQ_Content_Align", contentAlign.toString());

        editor.apply();
        setting_panel.setVisibility(View.GONE);
        GetAppConfig();
        isSmallConfig = false;

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
            case "NameBG":
                _color = nameBG;
                break;
            case "NameColor":
                _color = nameColor;
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
            case "NameBG":
                nameBG = color;
                 btnNameBG.setTextColor(color);
                btnNameBG.setText(color + "");
                break;
            case "NameColor":
                nameColor = color;
                 btnNameColor.setTextColor(color);
                btnNameColor.setText(color + "");
                break;
        }
    }

    //endregion

    private void GetAppConfig() {
        sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(TraKQ_SocketActivity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            String _value = "";
            switch (appType) {
                case "1":
                    intent = new Intent(TraKQ_SocketActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(TraKQ_SocketActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case "3":
                    intent = new Intent(TraKQ_SocketActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case "4":
                    intent = new Intent(TraKQ_SocketActivity.this, CountersEventActivity.class);
                    startActivity(intent);
                    break;
                case "5":
                    intent = new Intent(TraKQ_SocketActivity.this, PrintTicket_2Activity.class);
                    startActivity(intent);
                    break;
                case "6":
                    intent = new Intent(TraKQ_SocketActivity.this, PrintTicket_3Activity.class);
                    startActivity(intent);
                    break;
                case "7":
                    intent = new Intent(TraKQ_SocketActivity.this, HienThiQuay.class);
                    startActivity(intent);
                    break;
                case "8":
                    intent = new Intent(TraKQ_SocketActivity.this, PrintTicket_4Activity.class);
                    startActivity(intent);
                    break;
                case "10":
                    intent = new Intent(TraKQ_SocketActivity.this, ReceiveSmsActivity.class);
                    startActivity(intent);
                    break;
                case "11":
                    intent = new Intent(TraKQ_SocketActivity.this, LcdAreaActivity.class);
                    startActivity(intent);
                    break;
                case "12":
                    intent = new Intent(TraKQ_SocketActivity.this, LcdPhongKham2Activity.class);
                    startActivity(intent);
                    break;
                case "13":
                    intent = new Intent(TraKQ_SocketActivity.this, LcdPhongKham1Activity.class);
                    startActivity(intent);
                    break;
            }
            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
            _value = sharedPreferences.getString("IP", "138.168.31.246:92");
            IPAddress = "http://" + _value;
            txtIp.setText(_value);

            _value=sharedPreferences.getString("SocketIP", "138.168.31.246:91");
            IPNodeAddress = "http://" + _value;
            txtSocketIp.setText(_value);

            _value= sharedPreferences.getString("LCD_TraKQ_Title", "Bệnh viện đa khoa tỉnh trà vinh");
            lbTitle.setText(_value);
            txtTitle.setText(_value);
            _value = sharedPreferences.getString("LCD_TraKQ_Title_Color", "-15593237");
            lbTitle.setTextColor(Integer.parseInt(_value));
            btnTitleColor.setText(_value);
            _value = sharedPreferences.getString("LCD_TraKQ_Title_Size", "50");
            lbTitle.setTextSize(Float.parseFloat(_value));
            txtTitleSize.setText(_value);

            txtRow.setText(sharedPreferences.getString("LCD_TraKQ_Row", "3"));

            _value = sharedPreferences.getString("LCD_TraKQ_Caption_Color", "-1046520");
            lbCaption1.setTextColor(Integer.parseInt(_value));
            btnCaptionColor.setText(_value);
            _value = sharedPreferences.getString("LCD_TraKQ_Caption_BG", "-15859455");
            lbCaption1.setBackgroundColor(Integer.parseInt(_value));
            btnCaptionBG.setText(_value);
            _value = sharedPreferences.getString("LCD_TraKQ_Caption_Size", "50");
            lbCaption1.setTextSize(Float.parseFloat(_value));
            txtCaptionSize.setText(_value);

            contentAlign = Integer.parseInt(sharedPreferences.getString("LCD_TraKQ_Content_Align", "1"));
            spnContentAlign.setSelection(contentAlign);
            if (nameArr != null && nameArr.length > 0) {
                for (Integer i = 0; i < nameArr.length; i++) {
                    infoArr[i].setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_TraKQ_Content_Color", "-15593237")));
                    infoArr[i].setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_TraKQ_Content_BG", "-1")));
                    infoArr[i].setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_TraKQ_Content_Size", "55")));
                    switch (contentAlign) {
                        case 0:
                            infoArr[i].setGravity(Gravity.LEFT);
                            break;
                        case 1:
                            infoArr[i].setGravity(Gravity.CENTER);
                            break;
                        case 2:
                            infoArr[i].setGravity(Gravity.RIGHT);
                            break;
                    }

                    nameArr[i].setTextColor(Integer.parseInt(sharedPreferences.getString("LCD_TraKQ_Name_Color", "-15593237")));
                    nameArr[i].setBackgroundColor(Integer.parseInt(sharedPreferences.getString("LCD_TraKQ_Name_BG", "-1")));
                    nameArr[i].setTextSize(Float.parseFloat(sharedPreferences.getString("LCD_TraKQ_Name_Size", "55")));
                    switch (Integer.parseInt(sharedPreferences.getString("LCD_TraKQ_Name_Align", "1"))) {
                        case 0:
                            nameArr[i].setGravity(Gravity.LEFT);
                            break;
                        case 1:
                            nameArr[i].setGravity(Gravity.CENTER);
                            break;
                        case 2:
                            nameArr[i].setGravity(Gravity.RIGHT);
                            break;
                    }
                }
            }

            txtContentSize.setText(sharedPreferences.getString("LCD_TraKQ_Content_Size", "55"));
            btnContentBG.setText(sharedPreferences.getString("LCD_TraKQ_Content_BG", "-1"));
            btnContentColor.setText(sharedPreferences.getString("LCD_TraKQ_Content_Color", "-15593237"));

            txtNameSize.setText(sharedPreferences.getString("LCD_TraKQ_Name_Size", "55"));
            btnNameBG.setText(sharedPreferences.getString("LCD_TraKQ_Name_BG", "-1"));
            btnNameColor.setText(sharedPreferences.getString("LCD_TraKQ_Name_Color", "-15593237"));
            nameAlign = Integer.parseInt(sharedPreferences.getString("LCD_TraKQ_Name_Align", "1"));
            spinnerNameAlign.setSelection(nameAlign);

            InitSocketIO();
            InitListView();
             GetInfoNew(true);
        }
    }

    //region activity events
    @Override
    protected void onStart() {
        super.onStart();
        if(mSocket != null) {

            mSocket.on("android-show-refresh", onGetData);
            mSocket.on(Socket.EVENT_CONNECT, onSocketConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onSocketDisconnect);
            mSocket.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mSocket != null)
            mSocket.disconnect();

       // if(threadChange != null)
       //     threadChange.interrupt();
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

    private Emitter.Listener onGetData = new Emitter.Listener() {
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
                    statusPanel.setBackgroundColor(Color.BLUE);
                    //socketid|counterid|serviceId|userid
                    //mSocket.emit("android-send-device-info",mSocket.id()+"|"+counterIds+"|"+serviceIds+"|"+userId);
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
                    statusPanel.setBackgroundColor(Color.RED);
                }
            });
        }
    };
    //endregion

    @Override
    public void ApplyTexts(String password) {
        if (password.equalsIgnoreCase("123") ) {
            if (isSmallConfig) {
                if (setting_panel != null) {
                    setting_panel.setVisibility(View.VISIBLE);
                }
            }
            else {
                intent = new Intent(TraKQ_SocketActivity.this, AppConfigActivity.class);
                intent.putExtra("hold", "1");
                startActivity(intent);
            }
        }
        else
            Toast.makeText(TraKQ_SocketActivity.this, "Mật khẩu quản trị không đúng vui lòng nhập lại.", Toast.LENGTH_LONG).show();
    }

    private void InitListView() {
        int count = 0, rows = Integer.parseInt(txtRow.getText().toString());
        nameArr = new TextView[rows];
        infoArr = new TextView[rows];
        LinearLayout root = (LinearLayout) findViewById(R.id.rootLayout);
        root.removeAllViews();
        for (int i = 0; i < rows; i++) {
             LinearLayout row = new LinearLayout(TraKQ_SocketActivity.this);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));
             row.setOrientation(LinearLayout.VERTICAL);
            //row.setGravity(Gravity.CENTER);


            nameArr[count] = new TextView(TraKQ_SocketActivity.this);
            infoArr[count] = new TextView(TraKQ_SocketActivity.this);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            layoutParams.setMargins(0, 2, 0, 2);

           // nameArr[count].setLayoutParams(layoutParams);
            //nameArr[count].setPadding(15, 15, 15, 15);
            nameArr[count].setTextColor(Integer.parseInt(btnNameColor.getText().toString()));
            nameArr[count].setBackgroundColor(Integer.parseInt(btnNameBG.getText().toString()));
            nameArr[count].setTextSize(Float.parseFloat(txtNameSize.getText().toString()));
            nameArr[count].setText("--");
            switch (nameAlign) {
                case 0:
                    nameArr[count].setGravity(Gravity.LEFT);
                    break;
                case 1:
                    nameArr[count].setGravity(Gravity.CENTER);
                    break;
                case 2:
                    nameArr[count].setGravity(Gravity.RIGHT);
                    break;
            }

           // infoArr[count].setLayoutParams(layoutParams);
           // infoArr[count].setPadding(15, 0, 15, 0);
            infoArr[count].setTextColor(Integer.parseInt(btnContentColor.getText().toString()));
           infoArr[count].setBackgroundColor(Integer.parseInt(btnContentBG.getText().toString()));
            infoArr[count].setTextSize(Float.parseFloat(txtContentSize.getText().toString()));
            infoArr[count].setText("--");
           switch (contentAlign) {
                case 0:
                    infoArr[count].setGravity(Gravity.LEFT);
                    break;
                case 1:
                    infoArr[count].setGravity(Gravity.CENTER);
                    break;
                case 2:
                    infoArr[count].setGravity(Gravity.RIGHT);
                    break;
            }
            if(i%2==0){
                nameArr[i].setBackgroundColor(-1315861);
                infoArr[i].setBackgroundColor(-1315861);
            }

            row.addView(nameArr[count]);
            row.addView(infoArr[count]);

            root.addView(row);
            count++;
        }
    }

    public void GetInfoNew(final Boolean isReload) {
        if (!isReload)
            progressDialog.show();
        try {
           String url = IPAddress + "/api/serviceapi/GetShowTVs";
            JsonArrayRequest jsonRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray objs) {

                            lbStatus.setText("");
                            timeRefresh = 0;
                            progressDialog.hide();
                            showDatas = objs;

                            if (objs != null && objs.length() > 0) {
                                if(showDatas.length() <= nameArr.length){
                                    StopHandler();
                                    BindNormal();
                                }
                                else {
                                    if(startIndex> showDatas.length())
                                        startIndex=0;
                                    BindNhay();
                                   StartHandler();
                                }
                            }
                            else
                            {
                                BindEmpty();
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

    void  BindNormal(){
        for (Integer i = 0; i< nameArr.length ; i++){
            try {
                JSONObject jsObj = showDatas.getJSONObject(i);
                if (jsObj != null) {
                    nameArr[i].setText(jsObj.getString("Name")  );
                    if (jsObj.getString("Note") != "null")
                        infoArr[i].setText( jsObj.getString("Note"));
                }
                else {
                    nameArr[i].setText("---");
                    infoArr[i].setText("---");
                }
            } catch (JSONException e) {
                nameArr[i].setText("---");
                infoArr[i].setText("---");
            }
        }
    }

    void  BindEmpty(){
      StopHandler();
        for (Integer i = 0; i < nameArr.length; i++) {
            nameArr[i].setText("---");
            infoArr[i].setText("---");
        }
    }

    void  BindNhay(){
        Integer _ii=startIndex;
        for (Integer i = 0; i< nameArr.length ; i++){
            try {
                JSONObject jsObj = showDatas.getJSONObject(_ii);
                if (jsObj != null) {
                    nameArr[i].setText(jsObj.getString("Name")  );
                    if (jsObj.getString("Note") != "null")
                        infoArr[i].setText( jsObj.getString("Note"));
                }
                else {
                    nameArr[i].setText("---");
                    infoArr[i].setText("---");
                }
            } catch (JSONException e) {
                nameArr[i].setText("---");
                infoArr[i].setText("---");
            }
            if(_ii < showDatas.length()-1)
                _ii++;
            else _ii=0;
        }
        if(startIndex < showDatas.length()-1)
            startIndex++;
        else  startIndex =0;

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
          //  startIndex++;
            handler.postDelayed(this,3000);
BindNhay();
        }
    };

    void  StartHandler (){
        runnable.run();
    }

    void  StopHandler(){
        handler.removeCallbacks(runnable);
    }
}