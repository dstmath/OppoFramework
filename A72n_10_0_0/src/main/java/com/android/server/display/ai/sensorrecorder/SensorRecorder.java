package com.android.server.display.ai.sensorrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.server.display.ai.utils.ColorAILog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensorRecorder {
    private static final String EXTRA_LUX_RECORD = "lux_record";
    private static final String INTENT_LUX_RECORD_FRAMEWORK = "oppo.intent.aibrightness.LUX_RECORD_FRAMEWORK";
    private static final String INTENT_PLAY_RECORD_FRAMEWORK = "oppo.intent.aibrightness.PLAY_LUX_RECORD_FRAMEWORK";
    private static final String INTENT_START_RECORD = "oppo.intent.aibrightness.START_RECORD";
    private static final String INTENT_STOP_PLAY_RECORD = "oppo.intent.aibrightness.STOP_PLAY_LUX_RECORD";
    private static final String INTENT_STOP_RECORD = "oppo.intent.aibrightness.STOP_RECORD";
    private static final int RECORD_LIMIT_SIZE = 1500;
    private static final String TAG = "SensorRecorder";
    private final BroadcastReceiver mActionReceiver = new BroadcastReceiver() {
        /* class com.android.server.display.ai.sensorrecorder.SensorRecorder.AnonymousClass1 */

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            ColorAILog.i(SensorRecorder.TAG, "onReceive:" + action);
            switch (action.hashCode()) {
                case -230251521:
                    if (action.equals(SensorRecorder.INTENT_START_RECORD)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 318912096:
                    if (action.equals(SensorRecorder.INTENT_STOP_PLAY_RECORD)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1316455546:
                    if (action.equals(SensorRecorder.INTENT_PLAY_RECORD_FRAMEWORK)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1640091645:
                    if (action.equals(SensorRecorder.INTENT_STOP_RECORD)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                SensorRecorder.this.stopAll();
                SensorRecorder.this.mOpenRecord = true;
            } else if (c == 1) {
                SensorRecorder.this.mOpenRecord = false;
                SensorRecorder.this.mLastTime = 0;
                SensorRecorder.this.sendRecordData();
            } else if (c == 2) {
                SensorRecorder.this.play(intent);
            } else if (c == 3) {
                SensorRecorder.this.stopAll();
            }
        }
    };
    private final Context mContext;
    private volatile boolean mIsStopPlay = true;
    private long mLastTime = 0;
    private final ArrayList<LuxData> mLuxRecodeList = new ArrayList<>();
    private boolean mOpenRecord;
    private final ConcurrentLinkedQueue<Intent> mPlaybackIntentQueue = new ConcurrentLinkedQueue<>();
    private SensorPlayback mSensorPlayback;
    private ExecutorService mSingleThread;

    public interface SensorPlayback {
        void onSensorPlayback(float f);
    }

    public SensorRecorder(Context context) {
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_START_RECORD);
        intentFilter.addAction(INTENT_STOP_RECORD);
        intentFilter.addAction(INTENT_PLAY_RECORD_FRAMEWORK);
        intentFilter.addAction(INTENT_STOP_PLAY_RECORD);
        context.registerReceiver(this.mActionReceiver, intentFilter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
    }

    public void release() {
        try {
            this.mContext.unregisterReceiver(this.mActionReceiver);
        } catch (Exception e) {
            ColorAILog.w(TAG, "release unregisterReceiver error:" + e);
        }
    }

    public void registerPlayback(SensorPlayback sensorPlayback) {
        this.mSensorPlayback = sensorPlayback;
    }

    public void write(float lux) {
        if (this.mOpenRecord) {
            long now = System.currentTimeMillis();
            if (this.mLastTime == 0) {
                this.mLastTime = now;
            }
            this.mLastTime = now;
            this.mLuxRecodeList.add(new LuxData(lux, now - this.mLastTime));
            if (this.mLuxRecodeList.size() == RECORD_LIMIT_SIZE) {
                sendRecordData();
            }
        }
    }

    public boolean isPlaybacking() {
        return !this.mIsStopPlay;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void play(Intent intent) {
        if (this.mIsStopPlay) {
            this.mIsStopPlay = false;
            if (this.mSingleThread == null) {
                this.mSingleThread = Executors.newSingleThreadExecutor();
            }
            this.mSingleThread.execute(new PlaybackRunnable());
        }
        this.mPlaybackIntentQueue.offer(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void stopAll() {
        this.mIsStopPlay = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendRecordData() {
        synchronized (this.mLuxRecodeList) {
            ArrayList<LuxData> luxList = new ArrayList<>(this.mLuxRecodeList);
            Intent intent = new Intent(INTENT_LUX_RECORD_FRAMEWORK);
            intent.putParcelableArrayListExtra(EXTRA_LUX_RECORD, luxList);
            this.mContext.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
            this.mLuxRecodeList.clear();
        }
    }

    /* access modifiers changed from: private */
    public class PlaybackRunnable implements Runnable {
        ArrayList<LuxData> mLuxList;

        private PlaybackRunnable() {
        }

        public void run() {
            while (!SensorRecorder.this.mIsStopPlay) {
                Intent intent = (Intent) SensorRecorder.this.mPlaybackIntentQueue.poll();
                if (intent != null) {
                    this.mLuxList = intent.getParcelableArrayListExtra(SensorRecorder.EXTRA_LUX_RECORD);
                    if (this.mLuxList != null && SensorRecorder.this.mSensorPlayback != null) {
                        Iterator<LuxData> it = this.mLuxList.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            LuxData luxData = it.next();
                            if (SensorRecorder.this.mIsStopPlay) {
                                ColorAILog.i(SensorRecorder.TAG, "PlaybackRunnable run stop play back, runnable exit.");
                                break;
                            }
                            try {
                                Thread.sleep(luxData.mTime);
                            } catch (InterruptedException e) {
                                ColorAILog.e(SensorRecorder.TAG, "PlaybackRunnable Exception1: " + e.getMessage());
                            }
                            SensorRecorder.this.mSensorPlayback.onSensorPlayback(luxData.mLux);
                        }
                    } else {
                        ColorAILog.w(SensorRecorder.TAG, "PlaybackRunnable run mLuxList:" + this.mLuxList + ", mSensorPlayback:" + SensorRecorder.this.mSensorPlayback);
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        ColorAILog.e(SensorRecorder.TAG, "PlaybackRunnable Exception2: " + e2.getMessage());
                    }
                }
            }
            ColorAILog.w(SensorRecorder.TAG, "PlaybackRunnable run over, mIsStopPlay:" + SensorRecorder.this.mIsStopPlay);
        }
    }
}
