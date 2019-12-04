package com.gpro.admin.qmsevaluateonly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class FourButtonActivity extends AppCompatActivity {

    Button button, btn1, btn2, btn3, btn4;
    String IPAddress, UserName, Password, url, TicketNumber, requireLabel;
    Integer sizeTicket = 10, sizeButton = 10, sizeRequire =10, userId, number=0;
    JsonArrayRequest jsonArrayRequest;
    JsonObjectRequest jsonRequest;
    RequestQueue requestQueue;
    private Context mContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four_button);

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        final RequestQueue mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        //region  lấy số thứ tự đang gọi
        final TextView lbNumber = (TextView) findViewById(R.id.lbNumber);
        //lbNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeTicket);
       // Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DS-DIGIB.TTF");
      //  lbNumber.setTypeface(face);
        Thread thread = new Thread(){
            @Override
            public void run() {
                while (!isInterrupted()){
                    try {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                url = IPAddress + "/api/serviceapi/getnumber?username=" + UserName;
                                // requestQueue = Volley.newRequestQueue(MainActivity.this);
                                jsonRequest = new JsonObjectRequest(
                                        Request.Method.GET,
                                        url,
                                        null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                if (response != null) {
                                                    Integer num = response.optInt("Id");
                                                    lbNumber.setText(String.valueOf(num) );
                                                    number = num;
                                                } else {
                                                    lbNumber.setText("0000");
                                                    number = 0;
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                lbNumber.setText(  "ERR");
                                            }
                                        }
                                );
                                jsonRequest.setShouldCache(false);
                                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                 mRequestQueue.add(jsonRequest);
                            }
                        });
                    } catch (InterruptedException e) {
                        Toast.makeText(FourButtonActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                        // e.printStackTrace();
                    }
                }
            }
        } ;
        thread.start();
        //endregion

        //region init button 1
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setTag(1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //region
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_1&&num=" + number + "&&isUseQMS=" + 1);
                RequestQueue rqQue = Volley.newRequestQueue(FourButtonActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(FourButtonActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(FourButtonActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jsonRequest.setShouldCache(false);
                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_2&&num=" + number + "&&isUseQMS=" + 1);
                RequestQueue rqQue = Volley.newRequestQueue(FourButtonActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(FourButtonActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(FourButtonActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jsonRequest.setShouldCache(false);
                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_3&&num=" + number + "&&isUseQMS=" + 1);
                RequestQueue rqQue = Volley.newRequestQueue(FourButtonActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(FourButtonActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(FourButtonActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jsonRequest.setShouldCache(false);
                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_4&&num=" + number + "&&isUseQMS=" + 1);
                RequestQueue rqQue = Volley.newRequestQueue(FourButtonActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(FourButtonActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(FourButtonActivity.this, "Đánh giá : Không kết nối được với máy chủ." , Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jsonRequest.setShouldCache(false);
                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                mRequestQueue.add(jRequest);
                //endregion
            }
        });
        //endregion

        SetAppConfig();
    }

    private  void SetAppConfig(){
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(FourButtonActivity.this, AppConfigActivity.class);
            startActivity(intent);
        } else {
            String appType = sharedPreferences.getString("APP_TYPE", "1");
            Intent intent;
            switch (appType ){
                case "0":
                    intent = new Intent(FourButtonActivity.this, ThreeButtonActivity.class);
                    startActivity(intent);
                    break;
                case "2":
                    intent = new Intent(FourButtonActivity.this, PrintTicketActivity.class);
                    startActivity(intent);
                    break;
                case "3":
                    intent = new Intent(FourButtonActivity.this, DanhGiaActivity.class);
                    startActivity(intent);
                    break;
            }
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            UserName = sharedPreferences.getString("UserName", "0");
            Password = sharedPreferences.getString("Password", "0");
            setTitle(sharedPreferences.getString("APP_TITLE", "Phần mềm đánh giá GPRO"));
        }
    }

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
                intent = new Intent(FourButtonActivity.this, AppConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.mPrinter:
                intent = new Intent(FourButtonActivity.this, PrintTicketActivity.class);
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


    //region comment
    /*  Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("https://www.notebookcheck.net/fileadmin/Notebooks/News/_nc3/android_wallpaper5_2560x1600_1.jpg");
                    Drawable top = Drawable.createFromStream( (InputStream)url.getContent() , null);
                    btn1.setCompoundDrawables(null,top,null,null);

                    //Bitmap bmp = BitmapFactory.decodeStream((InputStream)url.getContent());
                    // imageView.setImageBitmap(bmp);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    private class LoadImageInternet extends AsyncTask<String,Void,Bitmap> {
        Bitmap bitmap = null;
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL("https://www.notebookcheck.net/fileadmin/Notebooks/News/_nc3/android_wallpaper5_2560x1600_1.jpg");
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
          //  Drawable top = Drawable.createFromStream(bitmap , "src");
        }
    }
    */
    //endregion
}
