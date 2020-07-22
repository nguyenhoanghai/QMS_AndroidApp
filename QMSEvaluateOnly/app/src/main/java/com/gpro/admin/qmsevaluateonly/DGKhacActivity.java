package com.gpro.admin.qmsevaluateonly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class DGKhacActivity extends AppCompatActivity {

    Intent intent;
    EditText txtComment;
    Button btnSend, btnBack;
    String IPAddress, matb,  TicketNumber, appType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dgkhac);
        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();
        intent = getIntent();
       IPAddress = (String) intent.getStringExtra("ip");
       matb = (String)intent.getStringExtra("matb");
       TicketNumber = (String)intent.getStringExtra("num");
       appType= (String)intent.getStringExtra("appType");

        txtComment = (EditText)findViewById(R.id.txtComment);

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
                String str = (IPAddress + "/api/serviceapi/Evaluate2?matb=" + matb + "&value=1000&num=" + TicketNumber + "&isUseQMS=" + 1+"&comment="+txtComment.getText());
                RequestQueue rqQue = Volley.newRequestQueue(DGKhacActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(DGKhacActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                                backToHome();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(DGKhacActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                rqQue.add(jRequest);
            }
        });
        //endregion
    }

    private void backToHome(){
        switch (appType ){
            case "0":
                intent = new Intent(DGKhacActivity.this, ThreeButtonActivity.class);
                startActivity(intent);
                break;
            case "1":
                intent = new Intent(DGKhacActivity.this, FourButtonActivity.class);
                startActivity(intent);
                break;
            case "2":
                intent = new Intent(DGKhacActivity.this, PrintTicketActivity.class);
                startActivity(intent);
                break;
            case "3":
                intent = new Intent(DGKhacActivity.this, DanhGiaActivity.class);
                startActivity(intent);
                break;
            case "4":
                intent = new Intent(DGKhacActivity.this, CountersEventActivity.class);
                startActivity(intent);
                break;
        }
    }
}
