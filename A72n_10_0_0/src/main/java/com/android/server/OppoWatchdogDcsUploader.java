package com.android.server;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoWatchdogDcsUploader {
    private static final String EVENT_TAG = "event:";
    private static final String LOGMAP_TAG = "logmap:";
    private static final String TAG = "OppoWatchdogDcsUploader";
    private Context mContext = null;
    private final String mDcsTag = DcsFingerprintStatisticsUtil.DCS_LOG_TAG;
    private boolean mDcsUpdateNow = SystemProperties.getBoolean("persist.sys.oppo.dcsnowtest", false);
    private final String mLogFile = "/data/system/OppoWatchdogDscLog.txt";

    public OppoWatchdogDcsUploader(Context context) {
        this.mContext = context;
        uploadLogFile();
    }

    private void upload(String event, Map<String, String> logMap) {
        Log.i(TAG, "Uploading: " + event);
        for (Map.Entry<String, String> entry : logMap.entrySet()) {
            Log.i(TAG, "  \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");
        }
        OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.DCS_LOG_TAG, event, logMap, this.mDcsUpdateNow);
    }

    private void uploadLogFile() {
        String event = null;
        Map<String, String> logMap = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader("/data/system/OppoWatchdogDscLog.txt"));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith(EVENT_TAG)) {
                    event = line.substring(EVENT_TAG.length());
                } else if (line.startsWith(LOGMAP_TAG) && (logMap = stringToLogMap(line.substring(LOGMAP_TAG.length()))) == null) {
                    event = null;
                }
                if (!(event == null || logMap == null)) {
                    upload(event, logMap);
                    event = null;
                    logMap = null;
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
            Log.e(TAG, "Failed to upload log file", e2);
        }
        new File("/data/system/OppoWatchdogDscLog.txt").delete();
    }

    private Map<String, String> stringToLogMap(String logStr) {
        Map<String, String> logMap = new HashMap<>();
        String str = logStr;
        while (str.startsWith("{{")) {
            int keyEnd = str.indexOf("==");
            if (keyEnd < 0) {
                Log.e(TAG, "Cannot find map key: " + logStr);
                return null;
            }
            String key = str.substring(2, keyEnd);
            int valueEnd = str.indexOf("}}");
            if (valueEnd < 0) {
                Log.e(TAG, "Cannot find map value: " + logStr);
                return null;
            }
            logMap.put(key, str.substring(keyEnd + 2, valueEnd));
            str = str.substring(valueEnd + 2);
        }
        if (str.length() <= 0) {
            return logMap;
        }
        Log.e(TAG, "Cannot parse the whole line: " + logStr);
        return null;
    }

    private String logMapToString(Map<String, String> logMap) {
        StringBuilder sb = new StringBuilder("");
        for (Map.Entry<String, String> entry : logMap.entrySet()) {
            sb.append("{{");
            sb.append(entry.getKey());
            sb.append("==");
            sb.append(entry.getValue());
            sb.append("}}");
        }
        return sb.toString();
    }

    public void storeLog(String event, Map<String, String> logMap) {
        if (logMap == null) {
            Log.e(TAG, "logMap is null");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream("/data/system/OppoWatchdogDscLog.txt", true);
            try {
                FileWriter out = new FileWriter(fos.getFD());
                out.write(EVENT_TAG + event + StringUtils.LF);
                out.write(LOGMAP_TAG + logMapToString(logMap) + StringUtils.LF);
                out.close();
                fos.getFD().sync();
            } catch (IOException e) {
                Log.e(TAG, "Failed to store log", e);
                fos.getFD().sync();
            } catch (Throwable th) {
                fos.getFD().sync();
                fos.close();
                throw th;
            }
            fos.close();
        } catch (Exception e2) {
            Log.e(TAG, "Failed to store log", e2);
        }
    }
}
