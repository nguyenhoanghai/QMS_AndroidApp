package com.gpro.admin.qmsevaluateonly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import yuku.ambilwarna.AmbilWarnaDialog;

public class AppConfigActivity extends AppCompatActivity implements ConfirmSaveDialog.DialogListener {

    EditText txtIp,txtSocketIp, txtTitle, txtChaoDG, txtmathietbi, txtAcc, txtPass,
            txtCauCamOn, txtSizeChaoDG, txtSizeCamOn, txtTimeShowCamOn,
            txtActionParams, txtHexcode, txtSlogan, txtSizeSlogan, txtDong,
            txtCot, txtSizeNutNext, txtSizeSTTNutNext, txtbutheight, txtbutwidth,
             txtSizeSTT ;
    Spinner lvAppType;
    Button btnSave,txtbutBackColor,txtbutTextColor,txtColorSTT, btnThanks_TextColor, btnDGTitle_TextColor ;
    SharedPreferences sharedPreferences;
    String[] arrAppType = {
            "3 nút đánh giá",
            "4 nút đánh giá",
            "Màn hình cấp phiếu",
            "đánh giá mẫu 3",
            "Màn hình Quầy",
            "Màn hình cấp phiếu 2",
            "Màn hình cấp phiếu 3",
            "Hiển thị quầy",
            "Màn hình cấp phiếu 4",
            "Màn hình gọi số - Counter Soft",
            "Cấp phiếu từ SMS",
            "LCD Khu Vực",
            "LCD Phòng có 2 bàn",
            "LCD Phòng có 1 bàn",
    };
    ArrayAdapter appTypeAdapter;
    Integer appType = 0;
    Switch aSwitch, swSendSMS;
    Intent intent;
    int butBackColor, butTextColor, sttColor, thanksTextColor, dgTitleTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);
        setTitle("Cấu hình");

        txtIp = (EditText) findViewById(R.id.txtIp);
        txtSocketIp = (EditText) findViewById(R.id.txtSocketIp);
        txtmathietbi = (EditText) findViewById(R.id.txtequipcode);
        txtAcc = (EditText) findViewById(R.id.txtAcc);
        txtPass = (EditText) findViewById(R.id.txtPass);
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtCauCamOn = (EditText) findViewById(R.id.txtCauCamOn);
        txtChaoDG = (EditText) findViewById(R.id.txtChaoDanhGia);
        txtSizeChaoDG = (EditText) findViewById(R.id.txtSizeChaoDanhGia);
        txtSizeCamOn = (EditText) findViewById(R.id.txtSizeCamOn);
        txtSizeSTT = (EditText) findViewById(R.id.txtSizeSTT);

        txtTimeShowCamOn = (EditText) findViewById(R.id.txtTimeShowCamOn);
        aSwitch = (Switch) findViewById(R.id.switch2);
        swSendSMS = (Switch) findViewById(R.id.swSendSMS);
        txtSlogan = (EditText) findViewById(R.id.txtSlogan);
        txtSizeSlogan = (EditText) findViewById(R.id.txtSizeSlogan);
        txtCot = (EditText) findViewById(R.id.txtCot);
        txtDong = (EditText) findViewById(R.id.txtDong);
        txtSizeNutNext = (EditText) findViewById(R.id.txtSizeNutNext);
        txtSizeSTTNutNext = (EditText) findViewById(R.id.txtSizeSTTNutNext);
        txtHexcode = (EditText) findViewById(R.id.txtHexcode);
        txtActionParams = (EditText) findViewById(R.id.txtActionParams);
        txtbutheight = (EditText) findViewById(R.id.txtbutheight);
        txtbutwidth = (EditText) findViewById(R.id.txtbutwidth);

        txtColorSTT = (Button) findViewById(R.id.txtColorSTT);
        txtbutBackColor = (Button) findViewById(R.id.txtButBackColor);
        txtbutTextColor = (Button) findViewById(R.id.txtButTextColor);
        btnThanks_TextColor = (Button) findViewById(R.id.txtColorThanks);
        btnDGTitle_TextColor = (Button) findViewById(R.id.txtColorChaoDG);

        appTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrAppType);
        lvAppType = (Spinner) findViewById(R.id.spinnerAppType);
        lvAppType.setAdapter(appTypeAdapter);
        lvAppType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                appType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        txtIp.setText(sharedPreferences.getString("IP", "0.0.0.0"));
        txtSocketIp.setText(sharedPreferences.getString("SocketIP", "0.0.0.0"));
        txtmathietbi.setText(sharedPreferences.getString("Equipcode", "1"));
        txtAcc.setText(sharedPreferences.getString("UserName", "0"));
        txtPass.setText(sharedPreferences.getString("Password", "0"));
        txtTitle.setText(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
        txtChaoDG.setText(sharedPreferences.getString("ChaoDG", "Vui lòng đánh giá chất lượng"));
        txtCauCamOn.setText(sharedPreferences.getString("CamOn", "Xin cảm ơn quý khách."));
        txtSizeChaoDG.setText(sharedPreferences.getString("SizeChaoDG", "20"));
        txtSizeCamOn.setText(sharedPreferences.getString("SizeCamOn", "20"));
        txtTimeShowCamOn.setText(sharedPreferences.getString("TimeShowCamOn", "1"));
        txtSlogan.setText(sharedPreferences.getString("Slogan", "Slogan"));
        txtSizeSlogan.setText(sharedPreferences.getString("SizeSlogan", "20"));
        txtCot.setText(sharedPreferences.getString("Cot", "20"));
        txtDong.setText(sharedPreferences.getString("Dong", "20"));
        txtSizeNutNext.setText(sharedPreferences.getString("SizeNext", "20"));
        txtSizeSTTNutNext.setText(sharedPreferences.getString("SizeSTTNext", "20"));
        txtSizeSTT.setText(sharedPreferences.getString("SizeSTT", "20"));

        txtHexcode.setText(sharedPreferences.getString("HexCode", "8B"));
        txtActionParams.setText(sharedPreferences.getString("ActionParam", "00,00"));
        txtbutwidth.setText(sharedPreferences.getString("ButWidth", "20"));
        txtbutheight.setText(sharedPreferences.getString("ButHeight", "20"));

        Integer color ;
        try {
            color  =Integer.parseInt(sharedPreferences.getString("ColorSTT", "-15859455")) ;
             txtColorSTT.setText(color+"");
            txtColorSTT.setTextColor(color);
        }catch (Exception ex){}
        txtColorSTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("ColorSTT");
            }
        });

        try {
            color  =Integer.parseInt(sharedPreferences.getString("ButBackColor", "-15859455")) ;
            txtbutBackColor.setText(color+"");
            txtbutBackColor.setTextColor(color);
        }catch (Exception ex){}
        txtbutBackColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("But_BackColor");
            }
        });

        try {
            color  =Integer.parseInt(sharedPreferences.getString("ButTextColor", "-15859455")) ;
            txtbutTextColor.setText(color+"");
            txtbutTextColor.setTextColor(color);
        }catch (Exception ex){}
        txtbutTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("But_TextColor");
            }
        });

        try {
            color  =Integer.parseInt(sharedPreferences.getString("ThanksTextColor", "-15859455")) ;
            btnThanks_TextColor.setText(color+"");
            btnThanks_TextColor.setTextColor(color);
        }catch (Exception ex){}
        btnThanks_TextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("Thanks_TextColor");
            }
        });

        try {
            color  =Integer.parseInt(sharedPreferences.getString("DGTitle_TextColor", "-15859455")) ;
            btnDGTitle_TextColor.setText(color+"");
            btnDGTitle_TextColor.setTextColor(color);
        }catch (Exception ex){}
        btnDGTitle_TextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker("DGTitle_TextColor");
            }
        });

        aSwitch.setChecked((sharedPreferences.getString("UseQMS", "0").equals("0") ? false : true));
        swSendSMS.setChecked((sharedPreferences.getString("SendSMS", "0").equals("0") ? false : true));

        appType = Integer.parseInt(sharedPreferences.getString("APP_TYPE", "0"));
        lvAppType.setSelection(appType);

        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        Intent intent;
        String hold;
        switch (appType.intValue()) {
            case 0:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, ThreeButtonActivity.class);
                    startActivity(intent);
                }
                break;
            case 1:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                }
                break;
            case 5:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, PrintTicket_2Activity.class);
                    startActivity(intent);
                }
                break;
            case 4:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, CountersEventActivity.class);
                    startActivity(intent);
                }
                break;
            case 6:
                intent = new Intent(AppConfigActivity.this, PrintTicket_3Activity.class);
                startActivity(intent);
                break;
            case 7:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, HienThiQuay.class);
                    startActivity(intent);
                }
                break;
            case 8:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, PrintTicket_4Activity.class);
                    startActivity(intent);
                }
                break;
            case 9:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, CounterSoftActivity.class);
                    startActivity(intent);
                }
            case 10:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, ReceiveSmsActivity.class);
                    startActivity(intent);
                }
                break;
            case 11:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, LcdAreaActivity.class);
                    startActivity(intent);
                }
                break;
            case 12:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, LcdPhongKham2Activity.class);
                    startActivity(intent);
                }
                break;
            case 13:
                intent = getIntent();
                hold = intent.getStringExtra("hold");
                if (hold == null && !isFirst.booleanValue()) {
                    intent = new Intent(AppConfigActivity.this, LcdPhongKham1Activity.class);
                    startActivity(intent);
                }
                break;
        }

        btnSave = (Button) findViewById(R.id.btnSave);
        //region event
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtIp.getText().toString() == "")
                    Toast.makeText(AppConfigActivity.this, "Vui lòng nhập địa chỉ máy chủ.", Toast.LENGTH_LONG).show();
                else if (txtmathietbi.getText().toString() == "")
                    Toast.makeText(AppConfigActivity.this, "Vui lòng nhập mã thiết bị.", Toast.LENGTH_LONG).show();
                else {
                  SaveConfig();
                }
            }
        });
        //endregion
    }

    private  void  SaveConfig(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("IS_FIRTS_LAUNCHER", false);
        editor.putString("IP", txtIp.getText().toString());
        editor.putString("SocketIP", txtSocketIp.getText().toString());
        editor.putString("Equipcode", txtmathietbi.getText().toString());
        editor.putString("UserName", txtAcc.getText().toString());
        editor.putString("Password", txtPass.getText().toString());
        editor.putString("APP_TYPE", appType.toString());
        editor.putString("APP_TITLE", txtTitle.getText().toString());
        editor.putString("ChaoDG", txtChaoDG.getText().toString());
        editor.putString("SizeChaoDG", txtSizeChaoDG.getText().toString());
        editor.putString("SizeCamOn", txtSizeCamOn.getText().toString());
        editor.putString("CamOn", txtCauCamOn.getText().toString());
        editor.putString("Slogan", txtSlogan.getText().toString());
        editor.putString("SizeSlogan", txtSizeSlogan.getText().toString());
        editor.putString("Cot", txtCot.getText().toString());
        editor.putString("Dong", txtDong.getText().toString());
        editor.putString("SizeNext", txtSizeNutNext.getText().toString());
        editor.putString("SizeSTTNext", txtSizeSTTNutNext.getText().toString());
        editor.putString("SizeSTT", txtSizeSTT.getText().toString());
        editor.putString("ColorSTT", txtColorSTT.getText().toString());
        editor.putString("ButTextColor", txtbutTextColor.getText().toString());
        editor.putString("ButBackColor", txtbutBackColor.getText().toString());
        editor.putString("TimeShowCamOn", txtTimeShowCamOn.getText().toString());
        editor.putString("HexCode", txtHexcode.getText().toString());
        editor.putString("ActionParam", txtActionParams.getText().toString());
        editor.putString("UseQMS", (aSwitch.isChecked() ? "1" : "0"));
        editor.putString("SendSMS", (swSendSMS.isChecked() ? "1" : "0"));
        editor.putString("ButWidth", txtbutwidth.getText().toString());
        editor.putString("ButHeight", txtbutheight.getText().toString());
        editor.putString("ThanksTextColor", btnThanks_TextColor.getText().toString());
        editor.putString("DGTitle_TextColor", btnDGTitle_TextColor.getText().toString());

        editor.apply();
        Intent intent;
        switch (appType) {
            case 0:
                intent = new Intent(AppConfigActivity.this, ThreeButtonActivity.class);
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(AppConfigActivity.this, FourButtonActivity.class);
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(AppConfigActivity.this, PrintTicketActivity.class);
                startActivity(intent);
                break;
            case 3:
                intent = new Intent(AppConfigActivity.this, DanhGiaActivity.class);
                startActivity(intent);
                break;
            case 4:
                intent = new Intent(AppConfigActivity.this, CountersEventActivity.class);
                startActivity(intent);
                break;
            case 5:
                intent = new Intent(AppConfigActivity.this, PrintTicket_2Activity.class);
                startActivity(intent);
                break;
            case 6:
                intent = new Intent(AppConfigActivity.this, PrintTicket_3Activity.class);
                startActivity(intent);
                break;
            case 7:
                intent = new Intent(AppConfigActivity.this, HienThiQuay.class);
                startActivity(intent);
                break;
            case 8:
                intent = new Intent(AppConfigActivity.this, PrintTicket_4Activity.class);
                startActivity(intent);
                break;
            case 9:
                intent = new Intent(AppConfigActivity.this, CounterSoftActivity.class);
                startActivity(intent);
                break;
            case 10:
                intent = new Intent(AppConfigActivity.this, ReceiveSmsActivity.class);
                startActivity(intent);
                break;
            case 11:
                intent = new Intent(AppConfigActivity.this, LcdAreaActivity.class);
                startActivity(intent);
                break;
            case 12:
                intent = new Intent(AppConfigActivity.this, LcdPhongKham2Activity.class);
                startActivity(intent);
                break;
            case 13:
                intent = new Intent(AppConfigActivity.this, LcdPhongKham1Activity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent;
            switch (appType.intValue()) {
                case 0:
                    intent = new Intent(AppConfigActivity.this, ThreeButtonActivity.class);
                    startActivity(intent);
                    break;
                case 1:
                    intent = new Intent(AppConfigActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    intent = new Intent(AppConfigActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                case 3:
                    intent = new Intent(AppConfigActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
                case 4:
                    intent = new Intent(AppConfigActivity.this, CountersEventActivity.class);
                    startActivity(intent);
                    break;
                case 5:
                    intent = new Intent(AppConfigActivity.this, PrintTicket_2Activity.class);
                    startActivity(intent);
                    break;
                case 8:
                    intent = new Intent(AppConfigActivity.this, PrintTicket_4Activity.class);
                    startActivity(intent);
                    break;
                case 9:
                    intent = new Intent(AppConfigActivity.this, CounterSoftActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //tao menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mEvaluate:
                if (appType == 0)
                    intent = new Intent(AppConfigActivity.this, ThreeButtonActivity.class);
                else
                    intent = new Intent(AppConfigActivity.this, FourButtonActivity.class);
                startActivity(intent);
                break;
            case R.id.mPrinter:
                intent = new Intent(AppConfigActivity.this, PrintTicketActivity.class);
                startActivity(intent);
                break;
            case R.id.mPrinter2:
                intent = new Intent(AppConfigActivity.this, PrintTicket_2Activity.class);
                startActivity(intent);
                break;
            case R.id.mCounterEvent:
                intent = new Intent(AppConfigActivity.this, CountersEventActivity.class);
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

    public void openColorPicker(final String code) {
        int _color = -15859455;
        switch (code){
            case "ColorSTT": _color = sttColor; break;
            case "But_BackColor": _color = butBackColor; break;
            case "But_TextColor": _color = butTextColor; break;
            case "Thanks_TextColor": _color = thanksTextColor; break;
            case "DGTitle_TextColor": _color = dgTitleTextColor; break;
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

    private  void  Ok_Click(int color, String code){
        switch (code){
            case "ColorSTT":
                sttColor = color;
                txtColorSTT.setTextColor(color);
                txtColorSTT.setText(color + "");
                 break;
            case "But_BackColor":
                butBackColor = color;
                txtbutBackColor.setTextColor(color);
                txtbutBackColor.setText(color + "");
                break;
            case "But_TextColor":
                butTextColor = color;
                txtbutTextColor.setTextColor(color);
                txtbutTextColor.setText(color + "");
                break;
            case "Thanks_TextColor":
                thanksTextColor = color;
                btnThanks_TextColor.setTextColor(color);
                btnThanks_TextColor.setText(color + "");
                break;
            case "DGTitle_TextColor":
                dgTitleTextColor = color;
                btnDGTitle_TextColor.setTextColor(color);
                btnDGTitle_TextColor.setText(color + "");
                break;
        }
    }

    @Override
    public void ApplyTexts(String password) {
        if (password.equalsIgnoreCase("gproadmin") )
            SaveConfig();
        else
            Toast.makeText(AppConfigActivity.this, "Mật khẩu quản trị không đúng vui lòng nhập lại.", Toast.LENGTH_LONG).show();
    }
}
