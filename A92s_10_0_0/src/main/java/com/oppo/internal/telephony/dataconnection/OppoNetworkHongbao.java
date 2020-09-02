package com.oppo.internal.telephony.dataconnection;

import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.oppo.internal.telephony.OppoRIL;
import com.oppo.internal.telephony.OppoTelephonyController;

public class OppoNetworkHongbao {
    private static final int EVENT_OEM_SCREEN_CHANGED = 1;
    private static final String LOG_TAG = "OppoNetworkHongbao";
    private Handler mHandler = new Handler() {
        /* class com.oppo.internal.telephony.dataconnection.OppoNetworkHongbao.AnonymousClass1 */

        public void handleMessage(Message msg) {
            OppoNetworkHongbao oppoNetworkHongbao = OppoNetworkHongbao.this;
            oppoNetworkHongbao.logd("handleMessage msg.what=" + msg.what);
            if (msg.what == 1) {
                OppoNetworkHongbao oppoNetworkHongbao2 = OppoNetworkHongbao.this;
                oppoNetworkHongbao2.mIsOppoScreenOn = OppoTelephonyController.getInstance(oppoNetworkHongbao2.mPhone.getContext()).isScreenOn();
                OppoNetworkHongbao oppoNetworkHongbao3 = OppoNetworkHongbao.this;
                oppoNetworkHongbao3.logd("EVENT_OEM_SCREEN_CHANGED mIsOppoScreenOn: " + OppoNetworkHongbao.this.mIsOppoScreenOn);
                if (!OppoNetworkHongbao.this.mIsOppoScreenOn && OppoNetworkHongbao.this.mIsHongbaoRun) {
                    boolean unused = OppoNetworkHongbao.this.mIsHongbaoRun = false;
                    OppoNetworkHongbao oppoNetworkHongbao4 = OppoNetworkHongbao.this;
                    oppoNetworkHongbao4.logd("cmd = " + "AT+EGCMD=307,1,\"000000\"" + " cmd1 = " + "AT+EGCMD=300,2,\"0000\"");
                    OppoNetworkHongbao.this.mPhone.invokeOemRilRequestStrings(new String[]{"AT+EGCMD=307,1,\"000000\"", ""}, (Message) null);
                    OppoNetworkHongbao.this.mPhone.invokeOemRilRequestStrings(new String[]{"AT+EGCMD=300,2,\"0000\"", ""}, (Message) null);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsHongbaoRun = false;
    boolean mIsOppoScreenOn = false;
    Phone mPhone;

    public OppoNetworkHongbao(Phone phone) {
        this.mPhone = phone;
        OppoTelephonyController.getInstance(this.mPhone.getContext()).registerForOemScreenChanged(this.mHandler, 1, null);
    }

    public void startMobileDataHongbaoPolicy(int time1, int time2, String value1, String value2) {
        Rlog.d(LOG_TAG, "startMobileDataHongbaoPolicy time1 = " + time1 + " time2 = " + time2);
        requestDrx(time1);
        requestFakePing(time1, time2);
    }

    public void requestDrx(int timePeriod) {
        Rlog.d(LOG_TAG, "requestDrx timePeriod: " + timePeriod);
        if (timePeriod > 65535) {
            timePeriod = 65535;
        }
        String timePeriodHex = String.format("%04X", Integer.valueOf(timePeriod));
        if (this.mIsOppoScreenOn) {
            this.mIsHongbaoRun = true;
            Phone phone = this.mPhone;
            phone.invokeOemRilRequestStrings(new String[]{"AT+EGCMD=300,2,\"" + timePeriodHex + "\"", ""}, (Message) null);
        }
    }

    public void requestFakePing(int timePeriod, int timer) {
        if (timePeriod > 65535) {
            timePeriod = 65535;
        }
        String timePeriodHex = String.format("%04X", Integer.valueOf(timePeriod));
        if (timer > 255) {
            timer = OppoRIL.MAX_MODEM_CRASH_CAUSE_LEN;
        }
        String content = "AT+EGCMD=307,1,\"" + (timePeriodHex + String.format("%02X", Integer.valueOf(timer))) + "\"";
        Rlog.d(LOG_TAG, "requestFakePing content: " + content);
        if (this.mIsOppoScreenOn) {
            this.mPhone.invokeOemRilRequestStrings(new String[]{content, ""}, (Message) null);
        }
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(LOG_TAG, s);
        }
    }
}
