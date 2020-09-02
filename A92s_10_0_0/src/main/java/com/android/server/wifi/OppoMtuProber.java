package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class OppoMtuProber {
    private static final String CONTENT_BOUNDARY = "-----------hello word-----------\r\n";
    private static final int DATA_HEAD_LEN = 110;
    private static boolean DBG = false;
    private static final String DEFAULT_MTU_SERVERS = "conn1.oppomobile.com,conn2.oppomobile.com,www.baidu.com,www.jd.com,www.taobao.com,www.qq.com";
    private static final String DEFAULT_MTU_SERVERS_EXP = "www.google.com";
    private static final String TAG = "OppoMtuProber";
    private static Random mRandom;
    private Handler mAsyncHandler;
    private Context mContext;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);

    public OppoMtuProber(Context context) {
        this.mContext = context;
        mRandom = new Random();
        HandlerThread handlerThread = new HandlerThread("Mtuprobe");
        handlerThread.start();
        this.mAsyncHandler = new Handler(handlerThread.getLooper());
    }

    private String getMtuServer() {
        String value;
        Boolean isExp = Boolean.valueOf(OppoClientModeImplUtil.isNotChineseOperator());
        if (isExp.booleanValue()) {
            value = this.mWifiRomUpdateHelper.getValue("NETWORK_MTU_SERVER_EXP", (String) null);
        } else {
            value = this.mWifiRomUpdateHelper.getValue("NETWORK_MTU_SERVER", (String) null);
        }
        if (value == null) {
            value = isExp.booleanValue() ? DEFAULT_MTU_SERVERS_EXP : DEFAULT_MTU_SERVERS;
        }
        String[] mtuServers = value.split(",");
        return mtuServers[mRandom.nextInt(mtuServers.length)];
    }

    public void StartMtuProber() {
        final String url = getMtuServer();
        if (url == null || this.mAsyncHandler == null) {
            log("MtuServer or mAsyncHandler is null.");
            return;
        }
        this.mAsyncHandler.post(new Runnable() {
            /* class com.android.server.wifi.OppoMtuProber.AnonymousClass1 */

            public void run() {
                OppoMtuProber.this.connectToMtuServer(url);
            }
        });
    }

    private String buildContent() {
        return CONTENT_BOUNDARY + new String(new char[(this.mWifiRomUpdateHelper.getIntegerValue("NETWORK_MTU", 1500).intValue() - 110)]) + "\r\n" + CONTENT_BOUNDARY;
    }

    /* access modifiers changed from: private */
    public void connectToMtuServer(String mtuServer) {
        HttpURLConnection urlConnection = null;
        InputStream inStream = null;
        try {
            urlConnection = (HttpURLConnection) new URL("http://" + mtuServer).openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=-----------hello word-----------\r\n");
            urlConnection.setRequestProperty("Charsert", "UTF-8");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");
            OutputStream outStream = urlConnection.getOutputStream();
            outStream.write(buildContent().getBytes("utf-8"));
            outStream.close();
            InputStream inStream2 = urlConnection.getInputStream();
            try {
                urlConnection.disconnect();
                if (inStream2 != null) {
                    inStream2.close();
                }
            } catch (Exception e) {
            }
        } catch (Exception e2) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inStream != null) {
                inStream.close();
            }
        } catch (Throwable th) {
            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (Exception e3) {
                    throw th;
                }
            }
            if (inStream != null) {
                inStream.close();
            }
            throw th;
        }
    }

    public void enableVerboseLogging(boolean enable) {
        DBG = enable;
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    protected static void log(String s) {
        if (DBG) {
            Log.d(TAG, " " + s);
        }
    }
}
