package com.gpro.admin.qmsevaluateonly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class DGKhac2Activity extends AppCompatActivity {
    Intent intent;
    EditText txtComment;
    Button btnSend, btnBack;
    String IPAddress, matb,  TicketNumber, appType;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_g_khac2);

        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();
        intent = getIntent();
        IPAddress = (String) intent.getStringExtra("ip");
        matb = (String)intent.getStringExtra("matb");
        TicketNumber = (String)intent.getStringExtra("num");
        appType= (String)intent.getStringExtra("appType");

        txtComment = (EditText)findViewById(R.id.txtComment);

        progressDialog = new ProgressDialog(DGKhac2Activity.this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Đang tải dữ liệu...");

        //region btn back
        btnBack = (Button)findViewById(R.id.btnCancel);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToHome();
            }
        });
        //endregion

        //region btn send
        btnSend = (Button)findViewById(R.id.btnSave);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String str = (IPAddress + "/api/serviceapi/Evaluate2?matb=" + matb + "&value=1000&num=" + TicketNumber + "&isUseQMS=" + 1+"&comment="+txtComment.getText());
                RequestQueue rqQue = Volley.newRequestQueue(DGKhac2Activity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                progressDialog.hide();
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs){
                                    intent = new Intent(DGKhac2Activity.this, CamOn2Activity.class);
                                    startActivity(intent);
                                }
                                   // Toast.makeText(DGKhac2Activity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                               // backToHome();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.hide();
                                Toast.makeText(DGKhac2Activity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                rqQue.add(jRequest);
            }
        });
        //endregion
    }

    private void backToHome(){
        intent = new Intent(DGKhac2Activity.this, HienThiQuay2Activity.class);
        startActivity(intent);
    }
}