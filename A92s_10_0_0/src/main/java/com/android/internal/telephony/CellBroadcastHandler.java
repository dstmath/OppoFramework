package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.Parcelable;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SmsCbMessage;
import android.telephony.SubscriptionManager;
import android.util.LocalLog;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class CellBroadcastHandler extends WakeLockStateMachine {
    private static final String EXTRA_MESSAGE = "message";
    private final LocalLog mLocalLog;

    private CellBroadcastHandler(Context context, Phone phone) {
        this("CellBroadcastHandler", context, phone);
    }

    protected CellBroadcastHandler(String debugTag, Context context, Phone phone) {
        super(debugTag, context, phone);
        this.mLocalLog = new LocalLog(100);
    }

    protected CellBroadcastHandler(String debugTag, Context context, Phone phone, Object dummy) {
        super(debugTag, context, phone, dummy);
        this.mLocalLog = new LocalLog(100);
    }

    public static CellBroadcastHandler makeCellBroadcastHandler(Context context, Phone phone) {
        CellBroadcastHandler handler = new CellBroadcastHandler(context, phone);
        handler.start();
        return handler;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.WakeLockStateMachine
    public boolean handleSmsMessage(Message message) {
        try {
            if (message.obj instanceof SmsCbMessage) {
                handleBroadcastSms((SmsCbMessage) message.obj);
                return true;
            }
            loge("handleMessage got object of type: " + message.obj.getClass().getName());
            return false;
        } catch (Exception e) {
            log("CellBroadcastHandler--error");
            return false;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
     arg types: [java.lang.String, android.telephony.SmsCbMessage]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent} */
    /* access modifiers changed from: protected */
    public void handleBroadcastSms(SmsCbMessage message) {
        String additionalPackage;
        TelephonyMetrics.getInstance().writeNewCBSms(this.mPhone.getPhoneId(), message.getMessageFormat(), message.getMessagePriority(), message.isCmasMessage(), message.isEtwsMessage(), message.getServiceCategory(), message.getSerialNumber(), System.currentTimeMillis());
        if (message.isEmergencyMessage()) {
            String msg = "Dispatching emergency SMS CB, SmsCbMessage is: " + message;
            log(msg);
            this.mLocalLog.log(msg);
            Intent intent = new Intent("android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED");
            intent.addFlags(268435456);
            intent.putExtra(EXTRA_MESSAGE, (Parcelable) message);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            if (Build.IS_DEBUGGABLE && (additionalPackage = Settings.Secure.getString(this.mContext.getContentResolver(), "cmas_additional_broadcast_pkg")) != null) {
                Intent additionalIntent = new Intent(intent);
                additionalIntent.setPackage(additionalPackage);
                this.mContext.sendOrderedBroadcastAsUser(additionalIntent, UserHandle.ALL, "android.permission.RECEIVE_EMERGENCY_BROADCAST", 17, null, getHandler(), -1, null, null);
            }
            String[] pkgs = this.mContext.getResources().getStringArray(17236006);
            this.mReceiverCount.addAndGet(pkgs.length);
            for (String pkg : pkgs) {
                intent.setPackage(pkg);
                this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, "android.permission.RECEIVE_EMERGENCY_BROADCAST", 17, this.mReceiver, getHandler(), -1, null, null);
            }
            return;
        }
        String msg2 = "Dispatching SMS CB, SmsCbMessage is: " + message;
        log(msg2);
        this.mLocalLog.log(msg2);
        Intent intent2 = new Intent("android.provider.Telephony.SMS_CB_RECEIVED");
        intent2.addFlags(16777216);
        intent2.putExtra(EXTRA_MESSAGE, (Parcelable) message);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent2, this.mPhone.getPhoneId());
        this.mReceiverCount.incrementAndGet();
        this.mContext.sendOrderedBroadcastAsUser(intent2, UserHandle.ALL, "android.permission.RECEIVE_SMS", 16, this.mReceiver, getHandler(), -1, null, null);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CellBroadcastHandler:");
        this.mLocalLog.dump(fd, pw, args);
        pw.flush();
    }
}
