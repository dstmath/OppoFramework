package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.util.OemTelephonyUtils;

public class AbstractSmsBroadcastUndelivered {
    protected static final long DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE_OEM = 86400000;
    private static final String TAG = "AbstractSmsBroadcastUndelivered";
    private static final int WAITING_TIME_DURING_MT_SMS_WHEN_SCAN_DB_MAX = 60;
    protected static SmsBroadcastUndelivered[] oemInstance;
    protected OemBroadcastReceiver mOemBroadcastReceiver;

    /* access modifiers changed from: protected */
    public long oemGetUndeliveredSmsExpirationTime(PersistableBundle bundle) {
        if (bundle != null) {
            try {
                return bundle.getLong("undelivered_sms_message_expiration_time", DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE_OEM);
            } catch (Exception e) {
            }
        }
        return DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE_OEM;
    }

    private static int oemGetPhoneCount(Context context) {
        TelephonyManager tm;
        try {
            return TelephonyManager.getDefault().getPhoneCount();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (context == null || (tm = (TelephonyManager) context.getSystemService("phone")) == null) {
                return 2;
            }
            return tm.getPhoneCount();
        }
    }

    public static int oemInitialize(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        int phoneCount = oemGetPhoneCount(context);
        if (oemInstance == null && phoneCount > 0) {
            oemInstance = new SmsBroadcastUndelivered[phoneCount];
        }
        int phoneId = -1;
        if (gsmInboundSmsHandler != null && gsmInboundSmsHandler.getPhone() != null) {
            phoneId = gsmInboundSmsHandler.getPhone().getPhoneId();
        } else if (cdmaInboundSmsHandler == null || cdmaInboundSmsHandler.getPhone() == null) {
            OppoRlog.Rlog.e(TAG, "gsmInboundSmsHandler == null && cdmaInboundSmsHandler == null");
        } else {
            phoneId = cdmaInboundSmsHandler.getPhone().getPhoneId();
        }
        OppoRlog.Rlog.d(TAG, "oemInitialize, phoneId=" + phoneId + ", phoneCount=" + phoneCount);
        if (gsmInboundSmsHandler != null) {
            gsmInboundSmsHandler.sendMessage(6);
        }
        if (cdmaInboundSmsHandler != null) {
            cdmaInboundSmsHandler.sendMessage(6);
        }
        return phoneId;
    }

    public AbstractSmsBroadcastUndelivered(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        try {
            this.mOemBroadcastReceiver = new OemBroadcastReceiver(gsmInboundSmsHandler, cdmaInboundSmsHandler);
            UserManager userManager = (UserManager) context.getSystemService("user");
            if (userManager == null) {
                OppoRlog.Rlog.d(TAG, "userManager == null");
            } else if (userManager.isUserUnlocked()) {
                new OemScanRawTableThread(context, gsmInboundSmsHandler, cdmaInboundSmsHandler).start();
            } else {
                IntentFilter userFilter = new IntentFilter();
                userFilter.addAction("android.intent.action.USER_UNLOCKED");
                context.registerReceiver(this.mOemBroadcastReceiver, userFilter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final class OemBroadcastReceiver extends BroadcastReceiver {
        private CdmaInboundSmsHandler cdmaInboundSmsHandlerInner;
        private GsmInboundSmsHandler gsmInboundSmsHandlerInner;

        public OemBroadcastReceiver(GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
            this.gsmInboundSmsHandlerInner = gsmInboundSmsHandler;
            this.cdmaInboundSmsHandlerInner = cdmaInboundSmsHandler;
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                OppoRlog.Rlog.d(AbstractSmsBroadcastUndelivered.TAG, "oem Received broadcast " + intent.getAction());
            }
            if (intent != null && "android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                new OemScanRawTableThread(context, this.gsmInboundSmsHandlerInner, this.cdmaInboundSmsHandlerInner).start();
            }
        }
    }

    private static class OemScanRawTableThread extends Thread {
        private CdmaInboundSmsHandler cdmaInboundSmsHandlerInner;
        private final Context contextInner;
        private GsmInboundSmsHandler gsmInboundSmsHandlerInner;

        public OemScanRawTableThread(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
            this.contextInner = context;
            this.gsmInboundSmsHandlerInner = gsmInboundSmsHandler;
            this.cdmaInboundSmsHandlerInner = cdmaInboundSmsHandler;
        }

        public void run() {
            try {
                OppoRlog.Rlog.d(AbstractSmsBroadcastUndelivered.TAG, "gsmin=" + this.gsmInboundSmsHandlerInner + " cdmain=" + this.cdmaInboundSmsHandlerInner);
                if (this.gsmInboundSmsHandlerInner != null) {
                    if (this.cdmaInboundSmsHandlerInner != null) {
                        AbstractInboundSmsHandler tmpgsmInboundSmsHandlerInner = (AbstractInboundSmsHandler) OemTelephonyUtils.typeCasting(AbstractInboundSmsHandler.class, this.gsmInboundSmsHandlerInner);
                        AbstractInboundSmsHandler tmpcdmaInboundSmsHandlerInner = (AbstractInboundSmsHandler) OemTelephonyUtils.typeCasting(AbstractInboundSmsHandler.class, this.cdmaInboundSmsHandlerInner);
                        for (int i = 0; i < 60; i++) {
                            if (tmpgsmInboundSmsHandlerInner.oemIsProgressing() || tmpcdmaInboundSmsHandlerInner.oemIsProgressing()) {
                                Thread.sleep(1000);
                                OppoRlog.Rlog.d(AbstractSmsBroadcastUndelivered.TAG, "scan wait time, " + i);
                            }
                        }
                        if (!tmpgsmInboundSmsHandlerInner.oemIsProgressing()) {
                            if (!tmpcdmaInboundSmsHandlerInner.oemIsProgressing()) {
                                SmsBroadcastUndelivered.scanRawTable(this.contextInner, this.cdmaInboundSmsHandlerInner, this.gsmInboundSmsHandlerInner, System.currentTimeMillis() - AbstractSmsBroadcastUndelivered.DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE_OEM);
                                InboundSmsHandler.cancelNewMessageNotification(this.contextInner);
                                return;
                            }
                        }
                        OppoRlog.Rlog.d(AbstractSmsBroadcastUndelivered.TAG, "time out, still mt sms");
                        return;
                    }
                }
                OppoRlog.Rlog.d(AbstractSmsBroadcastUndelivered.TAG, "run stop");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
