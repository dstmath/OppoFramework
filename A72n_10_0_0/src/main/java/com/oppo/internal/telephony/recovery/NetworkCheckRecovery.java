package com.oppo.internal.telephony.recovery;

import android.os.Handler;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NetworkCheckRecovery {
    private static final String DEFAULT_GOOGLE_WEBSITE_EXCEPT_CHINA = "www.google.com";
    private static final String DEFAULT_OPPO_WEBSITE_IN_CHINA = "conn3.coloros.com";
    public static final int NETWORK_FAIL = 1;
    public static final int NETWORK_OK = 0;
    private static final String NET_CHECK_TEMPLATE = "received, 0% packet loss";
    private static final String TAG = "NetworkCheckRecovery";
    private int mEventId;
    private Handler mHandler;
    private Object mObject;
    private int mPingTimeout = 1;
    private int mSendCount = 2;
    private Thread mThread;

    NetworkCheckRecovery(Handler handler, int eventId, Object o) {
        this.mHandler = handler;
        this.mEventId = eventId;
        this.mObject = o;
        Rlog.d(TAG, "NetworkCheckRecovery init");
        this.mThread = new Thread() {
            /* class com.oppo.internal.telephony.recovery.NetworkCheckRecovery.AnonymousClass1 */

            public void run() {
                NetworkCheckRecovery.this.doNetworkCheck();
            }
        };
    }

    public void start() {
        this.mThread.start();
    }

    private boolean isNotChineseOperator() {
        String mcc = SystemProperties.get("android.telephony.mcc_change", "");
        String mcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (TextUtils.isEmpty(mcc) && TextUtils.isEmpty(mcc2)) {
            return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
        }
        if ("460".equals(mcc) || "460".equals(mcc2)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doNetworkCheck() {
        try {
            String webSite = isNotChineseOperator() ? DEFAULT_GOOGLE_WEBSITE_EXCEPT_CHINA : DEFAULT_OPPO_WEBSITE_IN_CHINA;
            StringBuffer pingResult = new StringBuffer();
            Runtime runtime = Runtime.getRuntime();
            String cmd = "ping -c " + this.mSendCount + " -W " + this.mPingTimeout + " " + webSite;
            Rlog.d(TAG, "doNetworkCheck start cmd:" + cmd);
            Process proc = runtime.exec(cmd);
            proc.waitFor();
            if (proc.exitValue() == 0) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    pingResult.append(line + "\n");
                }
                String result = pingResult.toString();
                Rlog.d(TAG, "doNetworkCheck result:" + result);
                if (result.contains(this.mSendCount + " " + NET_CHECK_TEMPLATE)) {
                    this.mHandler.obtainMessage(this.mEventId, 0, 0, this.mObject).sendToTarget();
                } else {
                    this.mHandler.obtainMessage(this.mEventId, 1, 0, this.mObject).sendToTarget();
                }
                bufferedReader.close();
                return;
            }
            this.mHandler.obtainMessage(this.mEventId, 1, 0, this.mObject).sendToTarget();
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "doNetworkCheck failed! " + e.getMessage());
        }
    }
}
