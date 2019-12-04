package com.gpro.admin.qmsevaluateonly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DanhGiaActivity extends AppCompatActivity {

    String IPAddress, UserName, Password, url, TicketNumber, requireLabel,strName="", strPosition="";
    Integer sizeTicket = 10, sizeButton = 10, sizeRequire =10, userId, number=0;
    JsonArrayRequest jsonArrayRequest;
    JsonObjectRequest jsonRequest;
    RequestQueue requestQueue;
    private Context mContext;

    Button btn1, btn2, btn3, btn4;
    ImageView imgLogo, imgAvatar;
     Boolean isLoadLogo = true;
   public  TextView tvName;
  public TextView tvPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_gia);
        tvName = (TextView) findViewById(R.id.tvName);

        SetAppConfig();
        //Load logo
         new LoadImage().execute(IPAddress+"/Content/logo.png");

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        final RequestQueue mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        //region lấy thông tin nhân viên

         final String urlPath = (IPAddress + "/api/serviceapi/getuserinfo?username=" + UserName );
         StringRequest jRequest = new StringRequest(
                Request.Method.GET, urlPath,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        if(!response.equals("null")) {
                            Thread thread = new Thread(new Runnable(){
                                @Override
                                public void run() {
                                    //region
                                    JSONObject obj = null;
                                    try {
                                          obj = new JSONObject(response);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if(obj!= null){
                                        String avatarUrl = null;
                                        try {
                                           strName = obj.getString("Name");
                                           strPosition =obj.getString("Position");
                                            avatarUrl = obj.getString("Avatar");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        if( avatarUrl != "null" && avatarUrl!="")
                                        {
                                              avatarUrl = (IPAddress+ avatarUrl );
                                           // avatarUrl = (IPAddress+ obj.getString("Avatar").substring(1,response.length()-1)  );
                                            isLoadLogo=false;
                                            new DanhGiaActivity.LoadImage().execute(avatarUrl);
                                        }
                                        SetUserInfo();
                                    }
                                    else {
                                        Toast.makeText(DanhGiaActivity.this, "Không lấy được thông tin nhân viên.", Toast.LENGTH_SHORT).show();
                                    }
                                    //endregion
                                }
                            });
                            thread.start();
                        }
                        else
                            Toast.makeText(DanhGiaActivity.this, "Không lấy được hình nhân viên.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(DanhGiaActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jRequest.setShouldCache(false);
        jRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(jRequest);

        //endregion

        //region init button 1
         btn1 = (Button) findViewById(R.id.btn1);
         btn1.setTag(1);
         btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //region
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_1&&num=0&&isUseQMS=0");
                RequestQueue rqQue = Volley.newRequestQueue(DanhGiaActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(DanhGiaActivity.this, "Xin cám ơn Quý Khách đã đánh giá.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(DanhGiaActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jRequest.setShouldCache(false);
                jRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // rqQue.add(jRequest);
                mRequestQueue.add(jRequest);
                //endregion
            }
        });
        //endregion

        //region init button 2
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setTag(2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //region
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_2&&num=0&&isUseQMS=" + 0);
                RequestQueue rqQue = Volley.newRequestQueue(DanhGiaActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(DanhGiaActivity.this, "Xin cám ơn Quý Khách đã đánh giá.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(DanhGiaActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jRequest.setShouldCache(false);
                jRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                mRequestQueue.add(jRequest);
                //endregion
            }
        });
        //endregion

        //region init button 3
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setTag(3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //region
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_3&&num=0&&isUseQMS=" + 0);
                RequestQueue rqQue = Volley.newRequestQueue(DanhGiaActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(DanhGiaActivity.this, "Xin cám ơn Quý Khách đã đánh giá.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(DanhGiaActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jRequest.setShouldCache(false);
                jRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                mRequestQueue.add(jRequest);
                //endregion
            }
        });
        //endregion

        //region init button 4
        btn4 = (Button) findViewById(R.id.btn4);
        btn4.setTag(4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //region
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_4&&num=0&&isUseQMS=" + 0);
                RequestQueue rqQue = Volley.newRequestQueue(DanhGiaActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(DanhGiaActivity.this, "Xin cám ơn Quý Khách đã đánh giá.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(DanhGiaActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jRequest.setShouldCache(false);
                jRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                mRequestQueue.add(jRequest);
                //endregion
            }
        });
        //endregion

    }

    private  void  SetUserInfo(){

        tvName.setText(strName);
        tvPos = (TextView)findViewById(R.id.tvPos);
        tvPos.setText(strPosition);
    }

    private  void SetAppConfig(){
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(DanhGiaActivity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            String appType = sharedPreferences.getString("APP_TYPE", "1");
            Intent intent;
            switch (appType ){
                case "0":
                    intent = new Intent(DanhGiaActivity.this, ThreeButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(DanhGiaActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                    break;
                case "1":
                    intent = new Intent(DanhGiaActivity.this, FourButtonActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            UserName = sharedPreferences.getString("UserName", "0");
            Password = sharedPreferences.getString("Password", "0");
            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
        }
    }

    //region

    private class LoadImage extends AsyncTask<String,Void,Bitmap> {
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
            if(isLoadLogo){
                imgLogo  = findViewById(R.id.imgLogo);
                imgLogo.setImageBitmap(bitmap);
            }
            else {
                imgAvatar  = findViewById(R.id.imgAvatar);
                imgAvatar.setImageBitmap(bitmap);
            }

            //  Drawable top = Drawable.createFromStream(bitmap , "src");
        }
    }

    //endregion

    //region tao menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mConfig:
                intent = new Intent(DanhGiaActivity.this, AppConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.mPrinter:
                intent = new Intent(DanhGiaActivity.this, PrintTicketActivity.class);
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
