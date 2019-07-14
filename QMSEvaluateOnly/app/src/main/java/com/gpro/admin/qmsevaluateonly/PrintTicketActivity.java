package com.gpro.admin.qmsevaluateonly;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
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
import android.widget.TimePicker;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class PrintTicketActivity extends AppCompatActivity {

    //region variable declare
    String IPAddress;
    Integer appType = 0,
            serviceSelectedId = 0,
            currentHour = 0,
            currentMinute = 0;
    Spinner lvService;
    Button btnPrint;
    EditText txtCarNumber, txtTime;
    String[] arrServices;
    ArrayAdapter serviceArrayAdapter;
    JSONArray jsonServices;
    JsonObjectRequest jsonRequest;
    Calendar calendar;
    TimePickerDialog timePickerDialog;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_ticket);
        setTitle("Cấp phiếu QMS");
        GetAppConfig();

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        final RequestQueue mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        GetServices(mRequestQueue);

        txtTime = (EditText) findViewById(R.id.txtTime);
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowTimePicker();
            }
        });

        txtCarNumber = (EditText) findViewById(R.id.txtCarNumber);
        btnPrint = (Button) findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrintTicket( );
            }
        });
    }

    //region method
    private void PrintTicket( ) {
        //region
        String str = (IPAddress + "/api/serviceapi/PrintTicket?soxe=" + txtCarNumber.getText() + "&&thoigian="+txtTime.getText()+"&&dichvuId=" + serviceSelectedId  );
        RequestQueue rqQue = Volley.newRequestQueue(PrintTicketActivity.this);
        JsonObjectRequest jRequest = new JsonObjectRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Boolean rs = response.optBoolean("IsSuccess");
                        if (rs)
                            Toast.makeText(PrintTicketActivity.this, "Gửi yêu cầu cấp phiếu thành công.", Toast.LENGTH_SHORT).show();
                        else
                        Toast.makeText(PrintTicketActivity.this, "Gửi yêu cầu cấp phiếu thất bại.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PrintTicketActivity.this, "Gửi YC cấp phiếu : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                    }
                }
        );
         rqQue.add(jRequest);
        //endregion
    }

    private void GetServices(RequestQueue mRequestQueue) {
        String str = (IPAddress + "/api/serviceapi/getservices");
        RequestQueue rqQue = Volley.newRequestQueue(PrintTicketActivity.this);
        JsonArrayRequest jRequest = new JsonArrayRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null) {
                            jsonServices = response;
                            arrServices = new String[response.length()];
                            for (int ii = 0; ii < response.length(); ii++) {
                                try {
                                    JSONObject jsonObject = response.getJSONObject(ii);
                                    arrServices[ii] = jsonObject.getString("Name");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            InitSpinerService();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PrintTicketActivity.this, "Lấy Dịch vụ : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jRequest.setShouldCache(false);
        jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // rqQue.add(jRequest);
        mRequestQueue.add(jRequest);
    }

    private void ShowTimePicker() {
        calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinute = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(PrintTicketActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                calendar.set(0, 0, 0, hourOfDay, minutes, 0);
                txtTime.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }, 0, 0, true);
        timePickerDialog.show();
    }

    private void InitSpinerService() {
        serviceArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrServices);
        lvService = (Spinner) findViewById(R.id.spinnerService);
        lvService.setAdapter(serviceArrayAdapter);
        lvService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
                 try {
                    JSONObject obj = jsonServices.getJSONObject(index);
                    serviceSelectedId = obj.getInt("Id");
                    txtTime.setText(obj.getString("Code"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void GetAppConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(PrintTicketActivity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            String appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            switch (appType) {
                case "1":
                    intent = new Intent(PrintTicketActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "0":
                    intent = new Intent(PrintTicketActivity.this, ThreeButtonActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
        }
    }

    //endregion

    //region khởi tao menu
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
                    intent = new Intent(PrintTicketActivity.this, ThreeButtonActivity.class);
                else
                    intent = new Intent(PrintTicketActivity.this, FourButtonActivity.class);
                startActivity(intent);
                break;
            case R.id.mConfig:
                intent = new Intent(PrintTicketActivity.this, AppConfigActivity.class);
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
    //endregion
}


