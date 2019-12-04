package com.gpro.admin.qmsevaluateonly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AppConfigActivity extends AppCompatActivity {

    EditText txtIp, txtName, txtPass, txtTitle, txtSlogan;
    Spinner lvAppType;
    Button btnSave;
    SharedPreferences sharedPreferences;
    String[] arrAppType ={"3 nút đánh giá","4 nút đánh giá","Màn hình cấp phiếu","đánh giá mẫu 3"};
    ArrayAdapter appTypeAdapter ;
    Integer appType = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);
        setTitle("Cấu hình");

        txtIp = (EditText) findViewById(R.id.txtIp);
        txtName = (EditText) findViewById(R.id.txtUserName);
        txtPass = (EditText) findViewById(R.id.txtPass);
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtSlogan = (EditText)findViewById(R.id.txtSlogan);

        appTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrAppType);
        lvAppType = (Spinner) findViewById(R.id.spinnerAppType);
        lvAppType.setAdapter(appTypeAdapter);
        lvAppType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                appType = position;
                Toast.makeText(AppConfigActivity.this, appType.toString(), Toast.LENGTH_LONG).show();
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
        txtSlogan.setText(sharedPreferences.getString("Slogan", "Câu Slogan"));
          appType = Integer.parseInt(sharedPreferences.getString("APP_TYPE", "0"));
        lvAppType.setSelection( appType   );
        btnSave = (Button) findViewById(R.id.btnSave);
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
                    editor.putString("Slogan",  txtSlogan.getText().toString() );
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
                    }
                }
            }
        });
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
