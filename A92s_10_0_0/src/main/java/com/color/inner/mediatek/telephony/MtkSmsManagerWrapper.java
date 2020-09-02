package com.color.inner.mediatek.telephony;

import android.app.PendingIntent;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MtkSmsManagerWrapper {
    private static final String TAG = "MtkSmsManagerWrapper";
    private Object mMtkSmsManager;

    private MtkSmsManagerWrapper(Object mtkSmsManager) {
        this.mMtkSmsManager = mtkSmsManager;
    }

    public static MtkSmsManagerWrapper getDefault() {
        try {
            return new MtkSmsManagerWrapper(Class.forName("mediatek.telephony.MtkSmsManager").getDeclaredMethod("getDefault", new Class[0]).invoke(null, new Object[0]));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public MtkIccSmsStorageStatusWrapper getSmsSimMemoryStatus() {
        try {
            return new MtkIccSmsStorageStatusWrapper(this.mMtkSmsManager.getClass().getDeclaredMethod("getSmsSimMemoryStatus", new Class[0]).invoke(this.mMtkSmsManager, new Object[0]));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static MtkSmsManagerWrapper getSmsManagerForSubscriptionId(int subId) {
        try {
            return new MtkSmsManagerWrapper(Class.forName("mediatek.telephony.MtkSmsManager").getDeclaredMethod("getSmsManagerForSubscriptionId", Integer.TYPE).invoke(null, Integer.valueOf(subId)));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        try {
            return ((Integer) this.mMtkSmsManager.getClass().getDeclaredMethod("copyTextMessageToIccCard", String.class, String.class, List.class, Integer.TYPE, Long.TYPE).invoke(this.mMtkSmsManager, scAddress, address, text, Integer.valueOf(status), Long.valueOf(timestamp))).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public ArrayList<MtkSmsMessageWrapper> getAllMessagesFromIcc() {
        ArrayList<MtkSmsMessageWrapper> managerWrapperArrayList = new ArrayList<>();
        try {
            ArrayList objects = (ArrayList) this.mMtkSmsManager.getClass().getDeclaredMethod("getAllMessagesFromIcc", new Class[0]).invoke(this.mMtkSmsManager, new Object[0]);
            if (objects != null && objects.size() > 0) {
                Iterator it = objects.iterator();
                while (it.hasNext()) {
                    managerWrapperArrayList.add(new MtkSmsMessageWrapper(it.next()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return managerWrapperArrayList;
    }

    public void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, short originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        try {
            this.mMtkSmsManager.getClass().getDeclaredMethod("sendDataMessage", String.class, String.class, Short.TYPE, Short.TYPE, byte[].class, PendingIntent.class, PendingIntent.class).invoke(this.mMtkSmsManager, destinationAddress, scAddress, Short.valueOf(destinationPort), Short.valueOf(originalPort), data, sentIntent, deliveryIntent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public ArrayList<String> divideMessage(String text, int encodingType) {
        try {
            return (ArrayList) this.mMtkSmsManager.getClass().getDeclaredMethod("divideMessage", String.class, Integer.TYPE).invoke(this.mMtkSmsManager, text, Integer.valueOf(encodingType));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public void sendMultipartTextMessageWithExtraParams(String destAddr, String scAddr, ArrayList<String> parts, Bundle extraParams, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        try {
            this.mMtkSmsManager.getClass().getDeclaredMethod("sendMultipartTextMessageWithExtraParams", String.class, String.class, ArrayList.class, Bundle.class, ArrayList.class, ArrayList.class).invoke(this.mMtkSmsManager, destAddr, scAddr, parts, extraParams, sentIntents, deliveryIntents);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void sendMultipartTextMessageWithEncodingType(String destAddr, String scAddr, ArrayList<String> parts, int encodingType, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        try {
            this.mMtkSmsManager.getClass().getDeclaredMethod("sendMultipartTextMessageWithEncodingType", String.class, String.class, ArrayList.class, Integer.TYPE, ArrayList.class, ArrayList.class).invoke(this.mMtkSmsManager, destAddr, scAddr, parts, Integer.valueOf(encodingType), sentIntents, deliveryIntents);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
