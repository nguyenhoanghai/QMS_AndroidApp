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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {
    EditText txtIp, txtName, txtPass, txtSizeTicket, txtSizeButton,txtRequireTitle,txtRequireSize;

    Button btnSave;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        txtIp = (EditText) findViewById(R.id.txtIp);
        txtName = (EditText) findViewById(R.id.txtUserName);
        txtPass = (EditText) findViewById(R.id.txtPass);
        txtSizeTicket = (EditText) findViewById(R.id.txtSizeTicket);
        txtSizeButton = (EditText) findViewById(R.id.txtSizeButton);
        txtRequireTitle = (EditText) findViewById(R.id.txtRequireTitle);
        txtRequireSize = (EditText) findViewById(R.id.txtRequireSize);

        sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        txtIp.setText(sharedPreferences.getString("IP", "0.0.0.0"));
        txtName.setText(sharedPreferences.getString("UserName", "0"));
        txtPass.setText(sharedPreferences.getString("Password", "0"));
        txtSizeTicket.setText(sharedPreferences.getString("SIZE_LABEL", "10"));
        txtSizeButton.setText(sharedPreferences.getString("SIZE_BUTTON", "10"));
        txtRequireTitle.setText(sharedPreferences.getString("RE_LABEL", "10"));
        txtRequireSize.setText(sharedPreferences.getString("RE_SIZE", "10"));
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtIp.getText().toString() == "")
                    Toast.makeText(SettingActivity.this, "Vui lòng nhập địa chỉ máy chủ.", Toast.LENGTH_LONG).show();
                else if (txtName.getText().toString() == "")
                    Toast.makeText(SettingActivity.this, "Vui lòng nhập Tên tài khoản đăng nhập mặt định.", Toast.LENGTH_LONG).show();
                else if (txtPass.getText().toString() == "")
                    Toast.makeText(SettingActivity.this, "Vui lòng nhập mật khẩu dăng nhập.", Toast.LENGTH_LONG).show();
                else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("IS_FIRTS_LAUNCHER", false);
                    editor.putString("IP", txtIp.getText().toString());
                    editor.putString("UserName", txtName.getText().toString());
                    editor.putString("Password", txtPass.getText().toString());
                    editor.putString("SIZE_LABEL", txtSizeTicket.getText().toString());
                    editor.putString("SIZE_BUTTON", txtSizeButton.getText().toString());
                    editor.putString("RE_LABEL", txtRequireTitle.getText().toString());
                    editor.putString("RE_SIZE", txtRequireSize.getText().toString());
                    editor.apply();
                    Toast.makeText(SettingActivity.this, "Lưu dữ liệu thành công!", Toast.LENGTH_LONG).show();
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
                  intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.mPrinter:
                intent = new Intent(SettingActivity.this, PrintTicketActivity.class);
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
