package com.gpro.admin.qmsevaluateonly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
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
}
