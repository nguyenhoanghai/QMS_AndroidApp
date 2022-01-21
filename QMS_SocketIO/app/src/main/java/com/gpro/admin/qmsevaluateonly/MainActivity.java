package com.gpro.admin.qmsevaluateonly;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity  {
    String IPAddress, UserName, Password, url, TicketNumber, requireLabel;

    Integer sizeTicket = 10, sizeButton = 10, sizeRequire =10, userId, number=0;

    JsonArrayRequest jsonArrayRequest;
    JsonObjectRequest jsonRequest;
    RequestQueue requestQueue;

    Handler handler ;
    Runnable runnable;

    Timer timer = new Timer();
    Bundle bundle;
    Message message;


    TableLayout tableLayout;
    TableRow tableRow;
    Button button, btn1, btn2, btn3, btn4;

    private Context mContext;
    private Activity mActivity;
    Integer first = 0,count=0;




    // Start the queue
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("sdsd");

      //  requestWindowFeature(Window.FEATURE_NO_TITLE);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN  );
        setContentView(R.layout.activity_main);
      //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
// Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
// Instantiate the RequestQueue with the cache and network.
        final RequestQueue mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();



        GetConfig();

        final TextView  textView5 = (TextView) findViewById(R.id.lbRequire);
      //  textView5.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView5.setText(requireLabel);
        textView5.setSelected(true);
        textView5.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeRequire);
       // textView5.setSingleLine(true);



        final TextView  lbNumber = (TextView) findViewById(R.id.lbNumber);
        lbNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeTicket);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DS-DIGIB.TTF");
        lbNumber.setTypeface(face);

           int displaywidth = getResources().getDisplayMetrics().widthPixels;
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setWidth(displaywidth);
        btn1.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeButton);
        btn1.setTag(1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_1&&num=" + number + "&&isUseQMS=" + 1);
                RequestQueue rqQue = Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(MainActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Đánh giá : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jsonRequest.setShouldCache(false);
                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
               // rqQue.add(jRequest);
                mRequestQueue.add(jRequest);
            }
        });

        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setWidth(displaywidth);
        btn2.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeButton);
        btn2.setTag(2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_2&&num=" + number + "&&isUseQMS=" + 1);
                RequestQueue rqQue = Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(MainActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Đánh giá : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                // rqQue.add(jRequest);
                mRequestQueue.add(jRequest);
            }
        });
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setWidth(displaywidth);
        btn3.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeButton);
        btn3.setTag(3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_3&&num=" + number + "&&isUseQMS=" + 1);
                RequestQueue rqQue = Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(MainActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Đánh giá : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jsonRequest.setShouldCache(false);
                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // rqQue.add(jRequest);
                mRequestQueue.add(jRequest);
            }
        });
//region 4
        btn4 = (Button) findViewById(R.id.btn4);
        btn4.setWidth(displaywidth);
        btn4.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeButton);
        btn4.setTag(4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=1_4&&num=" + number + "&&isUseQMS=" + 1);
                RequestQueue rqQue = Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest jRequest = new JsonObjectRequest(
                        Request.Method.GET, str, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Boolean rs = response.optBoolean("IsSuccess");
                                if (rs)
                                    Toast.makeText(MainActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Đánh giá : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                jsonRequest.setShouldCache(false);
                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(20000,20,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // rqQue.add(jRequest);
                mRequestQueue.add(jRequest);
            }
        });
//endregion

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
                                //requestQueue.add(jsonRequest);
                                mRequestQueue.add(jsonRequest);
                               // count++;
                              //  textView5.setText(String.valueOf(count) );
                            }
                        });
                    } catch (InterruptedException e) {
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                       // e.printStackTrace();
                    }
                }
            }
        } ;
        thread.start();


      /*  url = IPAddress + "/api/service/getall";
        requestQueue = Volley.newRequestQueue(this);
        jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                          try {

                            ModelSelectItem[] items = new ModelSelectItem[response.length()];
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                items[i] = new ModelSelectItem();
                                items[i].setId(obj.getInt("Id"));
                                items[i].setName(obj.getString("Name"));

                            }
                            adapter = new SpinAdapter(RegisterActivity.this, android.R.layout.simple_spinner_item, items);
                            combo.setAdapter(adapter);
                            combo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    ModelSelectItem item = adapter.getItem(position);
                                    Toast.makeText(RegisterActivity.this, ("ID: " + item.getId() + "\nName: " + item.getName()),
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Không kết nối được với máy chủ", Toast.LENGTH_LONG).show();
                    }
                }
        );
         requestQueue.add(jsonArrayRequest);

*/

/*
        tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        for(int i=0;i<3;i++){
            tableRow = new TableRow(MainActivity.this);
            for(int x=0;x<3;x++){
                button = new Button(MainActivity.this);
                button.setText("Button "+x);
                tableRow.addView(button);
            }

            tableLayout.addView(tableRow);
        }
        */

    }




    private void GetNumber() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle bd = msg.getData();
             //   lbNumber.setText(bd.getString("ticket"));
            }
        };

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                url = IPAddress + "/api/serviceapi/getnumber?username=" + UserName;
                requestQueue = Volley.newRequestQueue(MainActivity.this);
                jsonRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                bundle = new Bundle();
                                message = new Message();
                                if (response != null) {
                                    Integer num = response.optInt("Id");
                                    bundle.putString("ticket", num.toString());
                                } else
                                    bundle.putString("ticket", "0000");
                                message.setData(bundle);
                                handler.sendMessage(message);
                                first = 0;
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                bundle = new Bundle();
                                message = new Message();
                                bundle.putString("ticket", "ERR");
                                message.setData(bundle);
                                handler.sendMessage(message);
                                if (first == 0) {
                                    Toast.makeText(MainActivity.this, "Lấy số : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                                    first = 1;
                                }
                            }
                        }
                );
                requestQueue.add(jsonRequest);
            }
        }, 2000, 2000);
    }

    private void GetConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences("QMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            IPAddress = "http://" + sharedPreferences.getString("IP", "0.0.0.0");
            UserName = sharedPreferences.getString("UserName", "0");
            Password = sharedPreferences.getString("Password", "0");
            requireLabel = sharedPreferences.getString("RE_LABEL", "");
            sizeTicket = Integer.parseInt(sharedPreferences.getString("SIZE_LABEL", "10"));
            sizeButton = Integer.parseInt(sharedPreferences.getString("SIZE_BUTTON", "10"));
            sizeRequire = Integer.parseInt(sharedPreferences.getString("RE_SIZE", "10"));
            // Login();
          // GetNumber();
        }
    }

    private void Login() {
        url = IPAddress + "/api/user/login/" + UserName + "/" + Password;
        requestQueue = Volley.newRequestQueue(this);
        jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        userId = response.optInt("UserId");
                        //  Toast.makeText(MainActivity.this, ("Rest susscess response :" + userId.toString()), Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Không kết nối được với máy chủ Login : " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        requestQueue.add(jsonRequest);
    }

    //tao menu


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
        //   case R.id.mEvaluate2:
           //    intent = new Intent(MainActivity.this, QMSActivity.class);
           //      startActivity(intent);
            //    break;
            case R.id.mConfig:
                intent = new Intent(MainActivity.this, AppConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.mExit:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Thoát");
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
/*
    @Override
    public void onClick(View v) {
        String view = v.getTag().toString();
        String value = "";
        switch (view) {
            case "1":
                value = "1_1";
                break;
            case "2":
                value = "1_2";
                break;
            case "3":
                value = "1_3";
                break;
            case "4":
                value = "1_4";
                break;
        }
        String str = (IPAddress + "/api/serviceapi/Evaluate?username=" + UserName + "&&value=" + value + "&&num=" + number + "&&isUseQMS=" + 1);
        RequestQueue rqQue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jRequest = new JsonObjectRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Boolean rs = response.optBoolean("IsSuccess");
                        if (rs)
                            Toast.makeText(MainActivity.this, "Xin cám ơn Quý Khách.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Đánh giá : Không kết nối được với máy chủ.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        rqQue.add(jRequest);
    }
*/
}
