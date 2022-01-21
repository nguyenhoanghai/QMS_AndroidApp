package com.gpro.admin.qmsevaluateonly;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PrintTicket_3Activity extends AppCompatActivity {

    TextView txtTitle;
    SpannableString spannableString;
 static   Button[] buttonArr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_print_ticket_3);
        txtTitle = (TextView)findViewById(R.id.lbTitle);
        String  text1 = "NGÂN HÀNG QUÂN ĐỘI" ,
                text2="MB BANK TÂN ĐỊNH",
                text3="";

        spannableString =new SpannableString(  text1+"\n"+text2  );
        spannableString.setSpan(new AbsoluteSizeSpan(54), 0, text1.length(), 0);

        spannableString.setSpan(new AbsoluteSizeSpan(54), text1.length(),spannableString.length(),  0);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE),0, spannableString.length() , 0);
        txtTitle.setText(spannableString);

        int index = 0;
        buttonArr = new Button[6];
        TableLayout tb = (TableLayout)findViewById(R.id.tb);
        String[] nameArr = new String[]{"Phùng Hướng Hoài Thương","Đặng Thái Hiền","Nguyễn Thị Bích Hồng"};
        String[] counterNameArr = new String[]{ "Quầy 2","Quầy 3" ,"Quầy Tư Vấn"};
        int[] percentArr = new int[]{80,95, 100};
        for (int i=0;i< 5;i++) {
            TableRow tableRow = new TableRow(PrintTicket_3Activity.this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            tb.addView(tableRow);
            for (int ii=0;ii< 1;ii++) {
                if(index<= 2) {
                    buttonArr[index] = new Button(PrintTicket_3Activity.this);
                    buttonArr[index].setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.MATCH_PARENT,
                            1.0f
                    ));
                    buttonArr[index].setTextSize(64);
                    buttonArr[index].setPadding(0, 0, 0, 30);
                    text1 = nameArr[index];
                    text2 = "Hài lòng " + percentArr[index] + "%";
                    text3 =   counterNameArr[index]  ;
                    spannableString = new SpannableString( text3+ "\n" +text1 + "\n" + text2);
                    spannableString.setSpan(new AbsoluteSizeSpan(54), 0, text3.length(), 0);
                    spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text3.length(), 0);

                    spannableString.setSpan(new AbsoluteSizeSpan(74), text3.length(), (text3.length()+text1.length()+1), 0);
                    spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), text3.length(), (text3.length() +text1.length()+1), 0);

                    spannableString.setSpan(new AbsoluteSizeSpan(64), (text3.length() +text1.length()+1), spannableString.length(), 0);
                    spannableString.setSpan(new ForegroundColorSpan(Color.RED), (text3.length() +text1.length()+1), spannableString.length(), 0);
                    buttonArr[index].setText(spannableString);
                    tableRow.addView(buttonArr[index]);
                    index++;
                }
            }
        }
    }
}
