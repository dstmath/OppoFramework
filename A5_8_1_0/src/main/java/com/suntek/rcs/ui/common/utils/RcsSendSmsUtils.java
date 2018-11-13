package com.suntek.rcs.ui.common.utils;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import com.suntek.mway.rcs.client.aidl.constant.Constants.MessageProvider.Message;

public class RcsSendSmsUtils {
    public static void startSendSmsActivity(Context context) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setType("vnd.android-dir/mms-sms");
        if (isActivityIntentAvailable(context, intent)) {
            context.startActivity(intent);
        }
    }

    public static void startSendSmsActivity(Context context, String[] phoneNumbers) {
        int i = 0;
        if (phoneNumbers != null && phoneNumbers.length != 0) {
            int length = phoneNumbers.length;
            while (i < length) {
                if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumbers[i])) {
                    i++;
                } else {
                    return;
                }
            }
            StringBuffer buffer = new StringBuffer();
            for (int i2 = 0; i2 < phoneNumbers.length; i2++) {
                buffer.append(phoneNumbers[i2]);
                if (i2 + 1 < phoneNumbers.length) {
                    buffer.append(";");
                }
            }
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.putExtra(Message.NUMBER, buffer.toString());
            intent.setType("vnd.android-dir/mms-sms");
            if (isActivityIntentAvailable(context, intent)) {
                context.startActivity(intent);
            }
        }
    }

    public static boolean isActivityIntentAvailable(Context context, Intent intent) {
        if (context.getPackageManager().queryIntentActivities(intent, 65536).size() > 0) {
            return true;
        }
        return false;
    }
}
