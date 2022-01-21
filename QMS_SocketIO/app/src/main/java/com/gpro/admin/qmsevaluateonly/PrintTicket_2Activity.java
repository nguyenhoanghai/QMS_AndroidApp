package com.gpro.admin.qmsevaluateonly;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class PrintTicket_2Activity extends AppCompatActivity {
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
TextView lbTitle;
ImageView logo;

    private static int
            NUM_ROW = 6 ,
            NUM_COL = 5 ,
            FONT_SIZE=12,
            FONT_SIZE_STT=12 ;
    Integer number = 0;
    private  static ArrayList<Integer> requestArr ;
    static Button [] buttonArr;
    JsonArrayRequest jsonArrayRequest;
    boolean isStop = false;
    SpannableString spannableString;
    JSONObject jsonObject = null;


    private ListView listview;
    private ListAdapter listAdapter;
    printListviewAdapter _printListviewAdapter;
    ArrayList<ServiceModel> services = new ArrayList<>();
    Button btnPlaceOrder;
    ArrayList<ServiceModel> serviceOrders = new ArrayList<>();

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();
        setContentView(R.layout.activity_print_ticket_2);

        progressDialog = new ProgressDialog(PrintTicket_2Activity.this);
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
        listview = (ListView)findViewById(R.id.listview);
        lbTitle = (TextView)findViewById(R.id.lbTitle);

        logo  = findViewById(R.id.imgLogo);
        logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mRequestQueue.stop();
                Intent intent = new Intent(PrintTicket_2Activity.this, AppConfigActivity.class);
                intent.putExtra("hold","1");
                startActivity(intent);
                return false;
            }
        });

        GetAppConfig();
        new  LoadImageInternet().execute(IPAddress+"/Content/logo.png");
         GetServices(mRequestQueue);

        //for (int i = 0 ; i < 10 ; i++) {
        //     services.add(new ServiceModel(("dich vu "+i)  , "code"+i, i));
       // }
       // InitListView();
    }


    private void GetServices(RequestQueue mRequestQueue) {
        buttonArr = new Button[(NUM_ROW * NUM_COL)];
        String str = (IPAddress + "/api/serviceapi/getservices");
        RequestQueue rqQue = Volley.newRequestQueue(PrintTicket_2Activity.this);
        JsonArrayRequest jRequest = new JsonArrayRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null && response.length() > 0) {
                            for (int i = 0 ; i < response.length() ; i++){
                                jsonObject = null;
                                try {
                                    jsonObject = response.getJSONObject(i);
                                    services.add(new ServiceModel(jsonObject.optString("Name")  , jsonObject.optString("Code"), jsonObject.optInt("Id")));
                                } catch (JSONException e) {
                                    // e.printStackTrace();
                                }
                            }
                            InitListView();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PrintTicket_2Activity.this, "Lấy Dịch vụ : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jRequest.setShouldCache(false);
        jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // rqQue.add(jRequest);
        mRequestQueue.add(jRequest);
    }

    private  void  InitListView(){
       // listAdapter = new ListAdapter(this,services);
      //  listview.setAdapter(listAdapter);

        _printListviewAdapter = new printListviewAdapter(this,services);
        listview.setAdapter(_printListviewAdapter);
    }

    public void  GridButton_Click(String serviceId, String thoigian){
        progressDialog.show();
        //region
        String str = (IPAddress + "/api/serviceapi/PrintNewTicket?MaPhongKham=" + serviceId+"&thoigian="+thoigian  );
        RequestQueue rqQue = Volley.newRequestQueue(PrintTicket_2Activity.this);
        JsonObjectRequest jRequest = new JsonObjectRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.hide();
                        Boolean rs = response.optBoolean("IsSuccess");
                        if (rs)
                            Toast.makeText(PrintTicket_2Activity.this, "Gửi yêu cầu cấp phiếu thành công.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(PrintTicket_2Activity.this, "Gửi yêu cầu cấp phiếu thất bại.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(PrintTicket_2Activity.this, "Gửi YC cấp phiếu : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                    }
                }
        );
        rqQue.add(jRequest);
        //endregion
    }

    private void GetAppConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(PrintTicket_2Activity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            String appType = sharedPreferences.getString("APP_TYPE", "0");
            Intent intent;
            switch (appType) {
                case "1":
                    intent = new Intent(PrintTicket_2Activity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
                case "0":
                    intent = new Intent(PrintTicket_2Activity.this, ThreeButtonActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            NUM_COL = (Integer.parseInt(sharedPreferences.getString("Cot", "5")));
            NUM_ROW = (Integer.parseInt(sharedPreferences.getString("Dong", "5")));
            FONT_SIZE = (Integer.parseInt(sharedPreferences.getString("SizeNext", "12")));
            FONT_SIZE_STT = (Integer.parseInt(sharedPreferences.getString("SizeSTTNext", "12")));

            lbTitle.setText(sharedPreferences.getString("ChaoDG", "Xin vui lòng đánh giá"));
            lbTitle.setTextSize(Float.parseFloat(sharedPreferences.getString("SizeChaoDG", "200")));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isStop = true;
            Intent intent = new Intent(PrintTicket_2Activity.this, AppConfigActivity.class);
            intent.putExtra("hold","hold");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class LoadImageInternet extends AsyncTask<String,Void,Bitmap> {
        Bitmap bitmap = null;
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                InputStream is = url.openConnection().getInputStream();
                bitmap= BitmapFactory.decodeStream(is);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap){
            super.onPostExecute(bitmap);
            try {
                //  imAvatar  = findViewById(R.id.imAvatar);
                logo.setImageBitmap(bitmap);
                //  Drawable top = Drawable.createFromStream(bitmap , "src");
            }
            catch (Exception e){}
        }
    }
}
