package com.gpro.admin.qmsevaluateonly;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TransferTicketActivity extends AppCompatActivity {

    Spinner spinnerMajor;
    Button btnBack, btnTransfer;
    ArrayAdapter arrayAdapter;
    ArrayList<ServiceModel> majors = new ArrayList<>();
    JSONObject jsonObject = null;
    Integer majorId = 0, stt = 0;
    ProgressDialog progressDialog;
    Intent intent;
    String IPAddress, matb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_ticket);
        setTitle("GPRO CounterSoft - Chuyển phiếu");
        progressDialog = new ProgressDialog(TransferTicketActivity.this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Đang tải dữ liệu...");
        progressDialog.show();

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        final RequestQueue mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        intent = getIntent();
        IPAddress = intent.getStringExtra("url");
        matb = intent.getStringExtra("matb");
        stt = intent.getIntExtra("stt", 0);

        initButton(mRequestQueue);
        GetMajors(mRequestQueue);
    }

    private void GetMajors(RequestQueue mRequestQueue) {
        String str = (IPAddress + "/api/serviceapi/getmajors");
        RequestQueue rqQue = Volley.newRequestQueue(TransferTicketActivity.this);
        JsonArrayRequest jRequest = new JsonArrayRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null && response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                jsonObject = null;
                                try {
                                    jsonObject = response.getJSONObject(i);
                                    majors.add(new ServiceModel(jsonObject.optString("Name"), jsonObject.optString("Code"), jsonObject.optInt("Id")));
                                } catch (JSONException e) {
                                    // e.printStackTrace();
                                }
                            }
                            initSpiner();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(TransferTicketActivity.this, "Lấy nghiệp vụ : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jRequest.setShouldCache(false);
        jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // rqQue.add(jRequest);
        mRequestQueue.add(jRequest);
    }

    public void initSpiner() {
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, majors);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMajor = (Spinner) findViewById(R.id.spinnerMajor);
        spinnerMajor.setAdapter(arrayAdapter);
        spinnerMajor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ServiceModel obj = majors.get(position);
                majorId = obj.Id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        progressDialog.hide();
    }

    public void initButton(final RequestQueue mRequestQueue) {
        btnBack = (Button) findViewById(R.id.btnBack);
        //region btnBack_Click
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransferTicketActivity.this, CounterSoftActivity.class);
                startActivity(intent);
            }
        });
        //endregion

        btnTransfer = (Button) findViewById(R.id.btnTransfer);
        //region btnTransfer_Click
        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String str = (IPAddress + "/api/serviceapi/TransferTicket?matb=" + matb + "&manv=" + majorId + "&stt=" + stt);
                RequestQueue rqQue = Volley.newRequestQueue(TransferTicketActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs) {
                                    Toast.makeText(TransferTicketActivity.this, "Chuyển phiếu thành công.", Toast.LENGTH_LONG).show();
                                    intent = new Intent(TransferTicketActivity.this, CounterSoftActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(TransferTicketActivity.this, "Chuyển phiếu thất bại.", Toast.LENGTH_LONG).show();
                                    progressDialog.hide();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.hide();
                                Toast.makeText(TransferTicketActivity.this, "Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jRequest.setShouldCache(false);
                jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // rqQue.add(jRequest);
                mRequestQueue.add(jRequest);
            }
        });
        //endregion
    }
}