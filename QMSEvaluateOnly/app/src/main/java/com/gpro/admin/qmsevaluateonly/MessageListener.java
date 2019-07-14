package com.gpro.admin.qmsevaluateonly;

import android.telephony.SmsMessage;

public interface MessageListener {
    void messageReceived(SmsMessage smsMessage);
}
