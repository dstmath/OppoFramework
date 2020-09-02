package com.android.server;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.ColorFreeformManagerService;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ColorFeatureStatisticsImpl extends ColorFeatureStatistics {
    private static String TAG_M = "ColorFeatureStatisticsImpl";
    private int CHECK_CREATE;
    private int DELAYTIME;
    private String FEATURE_CREATE;
    /* access modifiers changed from: private */
    public String FEATURE_NAME;
    private String FEATURE_SHOW;
    private String FILE_PATH;
    /* access modifiers changed from: private */
    public int FINISH_EXECUTION;
    /* access modifiers changed from: private */
    public String METHOD_NAME;
    /* access modifiers changed from: private */
    public int START_EXECUTION;
    /* access modifiers changed from: private */
    public String TIME_1;
    /* access modifiers changed from: private */
    public String TIME_2;
    private String TOTAL_COUNT;
    private String TOTAL_TIME;
    private long featureStartTime;
    private Handler handler;
    private HashMap<String, HashMap<String, long[]>> recordMap;
    private HandlerThread statisticsThread;

    public ColorFeatureStatisticsImpl() {
        this.FEATURE_CREATE = "sys.oppo.create_feature_txt";
        this.FEATURE_SHOW = ColorAILog.OPPO_LOG_KEY;
        this.FILE_PATH = "/data/color_feature.txt";
        this.FEATURE_NAME = "featureName";
        this.METHOD_NAME = "methodName";
        this.TOTAL_TIME = "totoalTime";
        this.TOTAL_COUNT = "totalCount";
        this.statisticsThread = null;
        this.handler = null;
        this.START_EXECUTION = 1;
        this.FINISH_EXECUTION = 2;
        this.CHECK_CREATE = 3;
        this.DELAYTIME = 3000;
        this.TIME_1 = "time1";
        this.TIME_2 = "time2";
        this.recordMap = new HashMap<>();
        this.featureStartTime = 0;
        this.statisticsThread = new HandlerThread("statistics-thread");
        this.statisticsThread.start();
        this.handler = new Handler(this.statisticsThread.getLooper()) {
            /* class com.android.server.ColorFeatureStatisticsImpl.AnonymousClass1 */

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle b = msg.getData();
                long time = (long) ((b.getInt(ColorFeatureStatisticsImpl.this.TIME_1) * ColorFreeformManagerService.FREEFORM_CALLER_UID) + b.getInt(ColorFeatureStatisticsImpl.this.TIME_2));
                String feature = b.getString(ColorFeatureStatisticsImpl.this.FEATURE_NAME);
                String method = b.getString(ColorFeatureStatisticsImpl.this.METHOD_NAME);
                int i = msg.what;
                if (i == 1) {
                    ColorFeatureStatisticsImpl colorFeatureStatisticsImpl = ColorFeatureStatisticsImpl.this;
                    colorFeatureStatisticsImpl.recordInfo(colorFeatureStatisticsImpl.START_EXECUTION, feature, method, time);
                } else if (i == 2) {
                    ColorFeatureStatisticsImpl colorFeatureStatisticsImpl2 = ColorFeatureStatisticsImpl.this;
                    colorFeatureStatisticsImpl2.recordInfo(colorFeatureStatisticsImpl2.FINISH_EXECUTION, feature, method, time);
                } else if (i == 3) {
                    ColorFeatureStatisticsImpl.this.checkIfCreatTxt();
                }
            }
        };
        Message check = this.handler.obtainMessage();
        check.what = this.CHECK_CREATE;
        this.handler.sendMessageDelayed(check, (long) this.DELAYTIME);
    }

    public void startExecution(String feature, String method, long time) {
        if (this.featureStartTime == 0) {
            this.featureStartTime = time;
        }
        hanldeInfo(this.START_EXECUTION, feature, method, time);
    }

    public void finishExecution(String feature, String method, long time) {
        hanldeInfo(this.FINISH_EXECUTION, feature, method, time);
    }

    private void hanldeInfo(int m, String feature, String method, long time) {
        Message msg = this.handler.obtainMessage();
        msg.what = m;
        Bundle date = new Bundle();
        date.putInt(this.TIME_1, (int) (time / 1000));
        date.putInt(this.TIME_2, (int) (time % 1000));
        date.putString(this.FEATURE_NAME, feature);
        date.putString(this.METHOD_NAME, method);
        msg.setData(date);
        msg.sendToTarget();
    }

    /* JADX INFO: Multiple debug info for r3v9 long[]: [D('tmpMap' java.util.HashMap<java.lang.String, long[]>), D('tmpInfo' long[])] */
    /* access modifiers changed from: private */
    public void recordInfo(int m, String feature, String method, long time) {
        long[] tmpInfo;
        if (m == this.START_EXECUTION) {
            if (this.recordMap.containsKey(feature)) {
                HashMap<String, long[]> tmpMap = this.recordMap.get(feature);
                if (tmpMap.containsKey(method)) {
                    long[] tmpInfo2 = tmpMap.get(method);
                    tmpInfo2[2] = time;
                    tmpInfo = tmpInfo2;
                } else {
                    tmpInfo = new long[]{0, 0, time};
                }
                tmpMap.put(method, tmpInfo);
                this.recordMap.put(feature, tmpMap);
                return;
            }
            HashMap<String, long[]> tmpMap2 = new HashMap<>();
            tmpMap2.put(method, new long[]{0, 0, time});
            this.recordMap.put(feature, tmpMap2);
        } else if (this.recordMap.containsKey(feature)) {
            HashMap<String, long[]> tmpMap3 = this.recordMap.get(feature);
            if (tmpMap3.containsKey(method)) {
                long[] tmpInfo3 = tmpMap3.get(method);
                tmpInfo3[0] = tmpInfo3[0] + 1;
                long thisTime = time - tmpInfo3[2];
                tmpInfo3[1] = tmpInfo3[1] + thisTime;
                if (SystemProperties.getBoolean(this.FEATURE_SHOW, false) && thisTime > 1) {
                    Log.e(feature, "method:" + method + " execution cost:" + thisTime + "ms");
                }
                tmpInfo3[2] = 0;
                tmpMap3.put(method, tmpInfo3);
                this.recordMap.put(feature, tmpMap3);
                return;
            }
            Log.e(TAG_M, "not find method");
        } else {
            Log.e(TAG_M, "not find feature");
        }
    }

    /* access modifiers changed from: private */
    public void checkIfCreatTxt() {
        if (SystemProperties.getBoolean(this.FEATURE_CREATE, false)) {
            File file = new File(this.FILE_PATH);
            FileOutputStream outStream = null;
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
                outStream = new FileOutputStream(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String result = ((((((("" + this.FEATURE_NAME) + fillSpace(this.FEATURE_NAME.length())) + this.METHOD_NAME) + fillSpace(this.METHOD_NAME.length())) + this.TOTAL_COUNT) + fillSpace(this.TOTAL_COUNT.length())) + this.TOTAL_TIME) + "\r\n";
            for (Map.Entry entry : this.recordMap.entrySet()) {
                String key = entry.getKey();
                for (Map.Entry entry1 : entry.getValue().entrySet()) {
                    String key1 = entry1.getKey();
                    long[] tmpinfo = entry1.getValue();
                    result = (((((((result + key) + fillSpace(key.length())) + key1) + fillSpace(key1.length())) + String.valueOf(tmpinfo[0])) + fillSpace(String.valueOf(tmpinfo[0]).length())) + String.valueOf(tmpinfo[1])) + "\r\n";
                }
            }
            try {
                outStream.write(result.getBytes());
                outStream.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            SystemProperties.set(this.FEATURE_CREATE, "false");
        }
        Message check = this.handler.obtainMessage();
        check.what = this.CHECK_CREATE;
        this.handler.sendMessageDelayed(check, (long) this.DELAYTIME);
    }

    private String fillSpace(int len) {
        String res = "";
        for (int i = 0; i < 50 - len; i++) {
            res = res + " ";
        }
        return res;
    }
}
