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

public class AppConfigActivity extends AppCompatActivity {

    EditText txtIp, txtName, txtPass, txtTitle,txtChaoDG,
            txtCauCamOn,txtSizeChaoDG,txtSizeCamOn,txtTimeShowCamOn,
            txtSlogan,txtSizeSlogan, txtDong,txtCot,txtSizeNutNext,txtSizeSTTNutNext;
    Spinner lvAppType;
    Button btnSave;
    SharedPreferences sharedPreferences;
    String[] arrAppType ={"3 nút đánh giá","4 nút đánh giá","Màn hình cấp phiếu","đánh giá mẫu 3","Màn hình Quầy"};
    ArrayAdapter appTypeAdapter ;
    Integer appType = 0;
    Switch aSwitch, swSendSMS;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);
        setTitle("Cấu hình");

        txtIp = (EditText) findViewById(R.id.txtIp);
        txtName = (EditText) findViewById(R.id.txtUserName);
        txtPass = (EditText) findViewById(R.id.txtPass);
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtCauCamOn = (EditText) findViewById(R.id.txtCauCamOn);
        txtChaoDG = (EditText) findViewById(R.id.txtChaoDanhGia);
        txtSizeChaoDG = (EditText) findViewById(R.id.txtSizeChaoDanhGia);
        txtSizeCamOn = (EditText) findViewById(R.id.txtSizeCamOn);
        txtTimeShowCamOn = (EditText) findViewById(R.id.txtTimeShowCamOn);
        aSwitch =(Switch)findViewById(R.id.switch2) ;
        swSendSMS =(Switch)findViewById(R.id.swSendSMS) ;
        txtSlogan = (EditText) findViewById(R.id.txtSlogan);
        txtSizeSlogan = (EditText) findViewById(R.id.txtSizeSlogan);
        txtCot = (EditText) findViewById(R.id.txtCot);
        txtDong = (EditText) findViewById(R.id.txtDong);
        txtSizeNutNext = (EditText) findViewById(R.id.txtSizeNutNext);
        txtSizeSTTNutNext = (EditText) findViewById(R.id.txtSizeSTTNutNext);

        appTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrAppType);
        lvAppType = (Spinner) findViewById(R.id.spinnerAppType);
        lvAppType.setAdapter(appTypeAdapter);
        lvAppType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                appType = position;
              switch (position){
                  case 0:
                      break;
                  case 1:
                      break;
                  case 2:

                      break;
                  case 4:
                    //  layoutCompat.setVisibility(View.VISIBLE);
                      break;
              }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        txtIp.setText(sharedPreferences.getString("IP", "0.0.0.0"));
        txtName.setText(sharedPreferences.getString("UserName", "0"));
        txtPass.setText(sharedPreferences.getString("Password", "0"));
        txtTitle.setText(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
        txtChaoDG.setText(sharedPreferences.getString("ChaoDG", "Vui lòng đánh giá chất lượng"));
        txtCauCamOn.setText(sharedPreferences.getString("CamOn", "Xin cảm ơn quý khách."));
        txtSizeChaoDG.setText(sharedPreferences.getString("SizeChaoDG", "200"));
        txtSizeCamOn.setText(sharedPreferences.getString("SizeCamOn", "200"));
        txtTimeShowCamOn.setText(sharedPreferences.getString("TimeShowCamOn", "1"));
        txtSlogan.setText(sharedPreferences.getString("Slogan", "Slogan"));
        txtSizeSlogan.setText(sharedPreferences.getString("SizeSlogan", "200"));
        txtCot.setText(sharedPreferences.getString("Cot", "200"));
        txtDong.setText(sharedPreferences.getString("Dong", "200"));
        txtSizeNutNext.setText(sharedPreferences.getString("SizeNext", "200"));
        txtSizeSTTNutNext.setText(sharedPreferences.getString("SizeSTTNext", "200"));

        aSwitch.setChecked( (sharedPreferences.getString("UseQMS", "0").equals("0")?false:true));
        swSendSMS.setChecked( (sharedPreferences.getString("SendSMS", "0").equals("0")?false:true)  );

        appType = Integer.parseInt(sharedPreferences.getString("APP_TYPE", "0"));
        lvAppType.setSelection( appType  );

        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);

   switch (appType.intValue() ){
            case 0:
             Intent intent = getIntent();
               String hold =   intent.getStringExtra("hold" )  ;
             if(hold == null && !isFirst.booleanValue() ){
                  intent = new Intent(AppConfigActivity.this, ThreeButtonActivity.class);
                    startActivity(intent);
               }
                break;
         /*   case 1:
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
                break;*/
        }

        btnSave = (Button) findViewById(R.id.btnSave);
        //region event
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtIp.getText().toString() == "")
                    Toast.makeText(AppConfigActivity.this, "Vui lòng nhập địa chỉ máy chủ.", Toast.LENGTH_LONG).show();
                else if (txtName.getText().toString() == "")
                    Toast.makeText(AppConfigActivity.this, "Vui lòng nhập Tên tài khoản đăng nhập mặt định.", Toast.LENGTH_LONG).show();
                else if (txtPass.getText().toString() == "")
                    Toast.makeText(AppConfigActivity.this, "Vui lòng nhập mật khẩu dăng nhập.", Toast.LENGTH_LONG).show();
                else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("IS_FIRTS_LAUNCHER", false);
                    editor.putString("IP", txtIp.getText().toString());
                    editor.putString("UserName", txtName.getText().toString());
                    editor.putString("Password", txtPass.getText().toString());
                    editor.putString("APP_TYPE", appType.toString() );
                    editor.putString("APP_TITLE",  txtTitle.getText().toString() );
                    editor.putString("ChaoDG",  txtChaoDG.getText().toString() );
                    editor.putString("SizeChaoDG",  txtSizeChaoDG.getText().toString() );
                    editor.putString("SizeCamOn",  txtSizeCamOn.getText().toString() );
                    editor.putString("CamOn",  txtCauCamOn.getText().toString() );
                    editor.putString("Slogan",  txtSlogan.getText().toString() );
                    editor.putString("SizeSlogan",  txtSizeSlogan.getText().toString() );
                    editor.putString("Cot",  txtCot.getText().toString() );
                    editor.putString("Dong",  txtDong.getText().toString() );
                    editor.putString("SizeNext",  txtSizeNutNext.getText().toString() );
                    editor.putString("SizeSTTNext",  txtSizeSTTNutNext.getText().toString() );
                    editor.putString("TimeShowCamOn",  txtTimeShowCamOn.getText().toString() );
                    editor.putString("UseQMS",  (aSwitch.isChecked()?"1":"0") );
                    editor.putString("SendSMS",  (swSendSMS.isChecked()?"1":"0")  );
                    editor.apply();
                    Intent intent;
                    switch (appType){
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
                    }
                }
            }
        });
        //endregion
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
             Intent intent;
            switch (appType.intValue() ){
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
                if(appType == 0)
                intent = new Intent(AppConfigActivity.this, ThreeButtonActivity.class);
                else
                    intent = new Intent(AppConfigActivity.this, FourButtonActivity.class);
                startActivity(intent);
                break;
            case R.id.mPrinter:
                intent = new Intent(AppConfigActivity.this, PrintTicketActivity.class);
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
}
