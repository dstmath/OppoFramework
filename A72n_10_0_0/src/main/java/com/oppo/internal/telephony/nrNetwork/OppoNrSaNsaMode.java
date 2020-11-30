package com.oppo.internal.telephony.nrNetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.oppo.internal.telephony.OppoNewNitzStateMachine;

public class OppoNrSaNsaMode {
    public static final String AT_CMD_QUERY_5G = "AT+E5GOPT?";
    public static final String AT_CMD_QUERY_5G_RESP = "+E5GOPT:";
    public static final String AT_CMD_SET_5G = "AT+E5GOPT=";
    public static final int EVENT_ON_DDS_CHANGED = 1;
    public static final int EVENT_ON_DEFAULT_SET_MODE_DONE = 4;
    public static final int EVENT_ON_SET_NSA_MODE_DONE = 3;
    public static final int EVENT_ON_SET_SA_NSA_MODE_DONE = 2;
    private static final String MODEM_NR_MODE_SLOT0 = "persist.vendor.radio.modem_nr_slot0_mode";
    private static final String MODEM_NR_MODE_SLOT1 = "persist.vendor.radio.modem_nr_slot1_mode";
    private static final int NR5G_DISABLE_MODE_INVALID = -1;
    private static final int NR5G_DISABLE_MODE_NONE_V01 = 7;
    private static final int NR5G_DISABLE_MODE_NSA_V01 = 3;
    private static final int NR5G_DISABLE_MODE_SA_NSA_V01 = 1;
    private static final int NR5G_DISABLE_MODE_SA_V01 = 5;
    private static final String USER_NR_MODE = "persist.vendor.radio.user_nr_mode";
    public static int mDefaultDataSlotId = -1;
    private static int mIsInsertedWhiteCard = 0;
    private static int mSaNsaMode = -1;
    private static int mWhiteCardInsertSlotId = -1;
    private static volatile OppoNrSaNsaMode sInstance = null;
    private String LOG_TAG = "OppoNrSaNsaMode";
    public Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread(this.LOG_TAG);
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.nrNetwork.OppoNrSaNsaMode.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (((action.hashCode() == -25388475 && action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) ? (char) 0 : 65535) == 0) {
                OppoNrSaNsaMode.this.logd("onDefaultDataSlotIdChanged");
                OppoNrSaNsaMode.this.mHandler.sendMessageDelayed(OppoNrSaNsaMode.this.mHandler.obtainMessage(1), OppoNewNitzStateMachine.SYSTEM_NTP_INTERVAL_OEM);
            }
        }
    };
    public int mPhoneNum = TelephonyManager.getDefault().getPhoneCount();
    private SubscriptionManager mSubscriptionManager = null;

    public static OppoNrSaNsaMode make(Context context) {
        OppoNrSaNsaMode oppoNrSaNsaMode;
        synchronized (OppoNrSaNsaMode.class) {
            if (sInstance == null) {
                sInstance = new OppoNrSaNsaMode(context);
            }
            oppoNrSaNsaMode = sInstance;
        }
        return oppoNrSaNsaMode;
    }

    public static OppoNrSaNsaMode getInstance(Context context) {
        if (sInstance == null) {
            synchronized (OppoNrSaNsaMode.class) {
                if (sInstance == null) {
                    sInstance = new OppoNrSaNsaMode(context);
                }
            }
        }
        return sInstance;
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int phoneId = msg.arg1;
            AsyncResult ar = (AsyncResult) msg.obj;
            int i = msg.what;
            if (i == 1) {
                OppoNrSaNsaMode.this.onDefaultDataSlotIdChanged();
            } else if (i != 2 && i != 3 && i != 4) {
            } else {
                if (ar == null || ar.exception != null || ar.result == null) {
                    OppoNrSaNsaMode oppoNrSaNsaMode = OppoNrSaNsaMode.this;
                    oppoNrSaNsaMode.loge(msg.what + ", error!");
                    return;
                }
                String[] strArr = (String[]) ar.result;
                OppoNrSaNsaMode oppoNrSaNsaMode2 = OppoNrSaNsaMode.this;
                oppoNrSaNsaMode2.logd("phone [" + phoneId + "] set " + msg.what);
            }
        }
    }

    public void setSaNsaMode(int slotId, int mode, int event) {
        logd("slotId = " + slotId + ",mode = " + mode + ",event = " + event);
        Phone mPhone = PhoneFactory.getPhone(slotId);
        if (mPhone == null) {
            logd("phone = null, maybe no permission MODIFY_PHONE_STATE");
            return;
        }
        Message msg = this.mHandler.obtainMessage(event, slotId, -1);
        try {
            int mNrModeSlot0_modem = Integer.valueOf(SystemProperties.get(MODEM_NR_MODE_SLOT0, "0")).intValue();
            int mNrModeSlot1_modem = Integer.valueOf(SystemProperties.get(MODEM_NR_MODE_SLOT1, "0")).intValue();
            logd("mNrModeSlot0_modem:" + mNrModeSlot0_modem + ",mNrModeSlot1_modem:" + mNrModeSlot1_modem + ",target_mode:" + mode);
            if ((slotId == 0 && mNrModeSlot0_modem == mode) || (slotId == 1 && mNrModeSlot1_modem == mode)) {
                logd("target mode and modem mode is same, not sent cmd");
                return;
            }
            mPhone.invokeOemRilRequestStrings(new String[]{AT_CMD_SET_5G + mode, ""}, msg);
            logd("set 5g mode:" + mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSaNsaMode(int slotId, Message msg) {
        logd("slotId = " + slotId);
        Phone mPhone = PhoneFactory.getPhone(slotId);
        if (mPhone == null) {
            logd("ril = null, maybe no permission MODIFY_PHONE_STATE");
            return;
        }
        mPhone.invokeOemRilRequestStrings(new String[]{AT_CMD_QUERY_5G, AT_CMD_QUERY_5G_RESP}, msg);
        logd("query current 5g mode");
    }

    public OppoNrSaNsaMode(Context context) {
        this.mContext = context;
        this.LOG_TAG += "/";
        logd("create OppoNrSaNsaMode");
        this.mHandlerThread.start();
        this.mHandler = new MyHandler(this.mHandlerThread.getLooper());
        this.mContext.registerReceiver(this.mIntentReceiver, new IntentFilter("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED"));
        this.mSubscriptionManager = (SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class);
        mDefaultDataSlotId = this.mSubscriptionManager.getDefaultDataPhoneId();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDefaultDataSlotIdChanged() {
        logd("onDefaultDataSlotIdChanged start!");
        int i = 0;
        while (true) {
            if (i >= this.mPhoneNum) {
                break;
            }
            int simState = SubscriptionManager.getSimStateForSlotIndex(i);
            if (simState == 10 || simState == 5) {
                String plmn = TelephonyManager.getDefault().getSimOperatorNumericForPhone(i);
                if (plmn.startsWith("00101")) {
                    mIsInsertedWhiteCard = 1;
                    mWhiteCardInsertSlotId = i;
                    break;
                }
                mIsInsertedWhiteCard = 0;
                logd("slot:" + i + ",plmn:" + plmn);
            }
            i++;
        }
        mDefaultDataSlotId = this.mSubscriptionManager.getDefaultDataPhoneId();
        logd("mDefaultDataSlotId = " + mDefaultDataSlotId);
        if (mIsInsertedWhiteCard > 0 && isValidDefaultDataSlotId(mDefaultDataSlotId)) {
            logd("onDefaultDataSlotIdChanged white sim card insert, set sa nas mode");
            int i2 = mWhiteCardInsertSlotId;
            int i3 = mDefaultDataSlotId;
            if (i2 == i3) {
                sendNrModeTestCard(i3, "AT+E5GOPT=7");
                return;
            }
            mSaNsaMode = updateNrMode();
            logd("onDefaultDataSlotIdChanged, mWhiteCardInsertSlotId:" + mWhiteCardInsertSlotId + ",mDefaultDataSlotId:" + mDefaultDataSlotId + ",mSaNsaMode:" + mSaNsaMode);
            setSaNsaMode(mDefaultDataSlotId, mSaNsaMode, 4);
        } else if (mIsInsertedWhiteCard != 0 || !isValidDefaultDataSlotId(mDefaultDataSlotId)) {
            logd("mDefaultDataSlotId is invalid, value = " + mDefaultDataSlotId);
        } else {
            mSaNsaMode = updateNrMode();
            logd("mDefaultDataSlotId = " + mDefaultDataSlotId + " mIsInsertedWhiteCard = " + mIsInsertedWhiteCard + " mSaNsaMode = " + mSaNsaMode);
            setSaNsaMode(mDefaultDataSlotId, mSaNsaMode, 4);
        }
    }

    private boolean isNumeric(String str) {
        if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != '-') {
            return false;
        }
        int i = str.length();
        do {
            i--;
            if (i <= 0) {
                return true;
            }
        } while (Character.isDigit(str.charAt(i)));
        return false;
    }

    public boolean isValidDefaultDataSlotId(int slotId) {
        logd("isValidDefaultDataSlotId slotId: " + slotId);
        return slotId < this.mPhoneNum && slotId > -1;
    }

    public int updateNrMode() {
        String UserNrMode = SystemProperties.get(USER_NR_MODE, "0");
        logd("UserNrMode:persist.vendor.radio.user_nr_mode");
        if ("7".equals(UserNrMode)) {
            return 7;
        }
        if ("3".equals(UserNrMode)) {
            return 3;
        }
        if (!"5".equals(UserNrMode) && "1".equals(UserNrMode)) {
            return 1;
        }
        return 5;
    }

    private void sendNrModeTestCard(int slotId, String cmdLine) {
        try {
            byte[] rawData = cmdLine.getBytes();
            byte[] cmdByte = new byte[(rawData.length + 1)];
            System.arraycopy(rawData, 0, cmdByte, 0, rawData.length);
            cmdByte[cmdByte.length - 1] = 0;
            Phone tmpPhone = PhoneFactory.getPhone(slotId);
            if (tmpPhone != null) {
                tmpPhone.invokeOemRilRequestRaw(cmdByte, (Message) null);
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }
}
