package com.gpro.admin.qmsevaluateonly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class AutoStart  extends BroadcastReceiver {
    private static MessageListener mListener;
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        SmsMessage[] smsArr = null;
        String infoSMS = "";
        if (extras != null){
            Object[] objects = (Object[])extras.get("pdus");
            smsArr = new SmsMessage[objects.length];
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) objects[0]);

            mListener.messageReceived(smsMessage);
            /*
            for (int i = 0 ; i<objects.length; i++) {
                smsArr[i] = SmsMessage.createFromPdu((byte[]) objects[i]);
                infoSMS += smsArr[i].getMessageBody().toString();
                infoSMS += "\n";
            }
            Toast.makeText(context, infoSMS,Toast.LENGTH_LONG).show();
            */
        }




        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }



        /* String action = intent.getAction();
        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
            Toast.makeText(context, "Service_PowerUp Started",
                    Toast.LENGTH_LONG).show();
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Toast.makeText(context, "Service_PowerUp Stoped", Toast.LENGTH_LONG)
                    .show();
        }*/
    }

    public  static void  bindListener(MessageListener listener)
    {
        mListener = listener;
    }
}
