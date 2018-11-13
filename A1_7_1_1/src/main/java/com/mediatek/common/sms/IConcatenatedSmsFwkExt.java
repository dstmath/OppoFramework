package com.mediatek.common.sms;

import android.os.Handler;

public interface IConcatenatedSmsFwkExt {
    public static final String ACTION_CLEAR_OUT_SEGMENTS = "android.sms.ACTION_CLEAR_OUT_SEGMENTS";
    public static final int EVENT_DISPATCH_CONCATE_SMS_SEGMENTS = 3001;
    public static final int OUT_OF_DATE_TIME = 43200000;
    public static final int UPLOAD_FLAG_NEW = 1;
    public static final int UPLOAD_FLAG_NONE = 0;
    public static final String UPLOAD_FLAG_TAG = "upload_flag";
    public static final int UPLOAD_FLAG_UPDATE = 2;

    void cancelTimer(Handler handler, Object obj);

    void deleteExistedSegments(TimerRecord timerRecord);

    int getUploadFlag(TimerRecord timerRecord);

    boolean isFirstConcatenatedSegment(String str, int i);

    boolean isLastConcatenatedSegment(String str, int i, int i2);

    byte[][] queryExistedSegments(TimerRecord timerRecord);

    TimerRecord queryTimerRecord(String str, int i, int i2);

    void refreshTimer(Handler handler, Object obj);

    void setPhoneId(int i);

    void setUploadFlag(TimerRecord timerRecord);

    void startTimer(Handler handler, Object obj);
}
