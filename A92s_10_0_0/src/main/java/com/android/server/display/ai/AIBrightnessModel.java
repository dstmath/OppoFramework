package com.android.server.display.ai;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.server.display.ai.MonotoneSplineManager;
import com.android.server.display.ai.bean.BrightnessPoint;
import com.android.server.display.ai.bean.ModelConfig;
import com.android.server.display.ai.broadcastreceiver.AIBrightnessTrainSwitch;
import com.android.server.display.ai.broadcastreceiver.BootCompletedReceiver;
import com.android.server.display.ai.broadcastreceiver.LogSwitchObserver;
import com.android.server.display.ai.broadcastreceiver.ScreenOffReceiver;
import com.android.server.display.ai.broadcastreceiver.TrainedReceiver;
import com.android.server.display.ai.mode.HighBrightnessMode;
import com.android.server.display.ai.report.EventUploader;
import com.android.server.display.ai.sensorrecorder.SensorRecorder;
import com.android.server.display.ai.utils.AppSwitchDetectorUtil;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.display.ai.utils.ModelConfigUtil;
import com.android.server.display.ai.utils.RomUpdateHelper;
import com.android.server.policy.OppoPhoneWindowManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import oppo.util.OppoStatistics;

public class AIBrightnessModel implements SensorRecorder.SensorPlayback, AppSwitchDetectorUtil.IAppSwitchListener {
    private static final String ACTION_BRIGHTNESS_EVENT = "oppo.intent.action.ai.brightness.BRIGHTNESS_EVENT";
    private static final int DELAY_NEXT_LUX_WILL_CHANGED = 200;
    private static final int DELAY_SET_BRIGHTNESS_BY_USER_OVER = 3000;
    private static final int DELAY_UNLOCK_BRIGHTNESS_EXCEPTION = 2000;
    private static final int EVENT_TYPE_BRIGHTNESS_CHANGE_BY_APP_SWITCH = 3;
    private static final int EVENT_TYPE_BRIGHTNESS_CHANGE_BY_SENSOR_CHANGE = 1;
    private static final int EVENT_TYPE_RESET = 2;
    private static final int EVENT_TYPE_USER_SET_BRIGHTNESS = 0;
    private static final String EXTRA_BRIGHTNESS = "brightness";
    private static final String EXTRA_DATE = "date";
    private static final String EXTRA_EVENT_TYPE = "event_type";
    private static final String EXTRA_LUX = "lux";
    private static final float HBM_SCALE = 8.0f;
    private static final float HIGH_BRIGHTNESS_PERCENT = 0.6f;
    private static final float HIGH_SCALE = 2.0f;
    private static final float LOWEST_AREA = 50.0f;
    private static final float LOWEST_SCALE = 3.0f;
    private static final float LOW_BRIGHTNESS_PERCENT = 0.15f;
    private static final float LOW_SCALE = 3.5f;
    private static final float LOW_UP_SCALE = 1.5f;
    private static final float LUX_CHECK_LIMIT_PERCENT = 0.03f;
    private static final int LUX_CHECK_SIZE_WHEN_DOWN = 6;
    private static final int LUX_CHECK_SIZE_WHEN_UP = 4;
    private static final int MSG_APP_SWITCH = 7;
    private static final int MSG_CURRENT_SPLINE_UPDATE = 9;
    private static final int MSG_FORCE_UPDATE_BRIGHTNESS = 4;
    private static final int MSG_GAME_STATE_CHANGE = 8;
    private static final int MSG_LOCK_BRIGHTNESS_EXCEPTION = 10;
    private static final int MSG_LUX_CHANGED = 3;
    private static final int MSG_NEXT_LUX_WILL_CHANGED = 6;
    private static final int MSG_RESET_DRAG_LINE = 5;
    private static final int MSG_SET_BRIGHTNESS_BY_USER = 1;
    private static final int MSG_SET_BRIGHTNESS_BY_USER_OVER = 2;
    private static final int NOISE_LUX_LIMIT_COUNT = 1;
    private static final float NORMAL_BRIGHTNESS_MIN = 2.0f;
    private static final int NORMAL_MAX_SCALE = 1000;
    private static final float ONE_FRAME_TIME = 0.016f;
    private static final String POINT_SPLIT_SIGN = ",";
    private static final String SAVE_BUNDLE_CURR_POINT = "curr_point";
    private static final String SAVE_BUNDLE_DRAG_POINT = "drag_point";
    private static final int SELL_MODE_CLOSE = 0;
    private static final int SELL_MODE_OPEN = 1;
    private static final String SETTINGS_AI_BRIGHTNESS_CURR_POINT = "ai_brightness_current_point";
    private static final String SETTINGS_AI_BRIGHTNESS_DRAG_POINT = "ai_brightness_drag_point";
    private static final String SETTINGS_GLOBAL_HBM_SELL_MODE = "global_hbm_sell_mode";
    private static final String TAG = "AIBrightnessModel";
    private AIBrightnessTrainSwitch mAIBrightnessTrainSwitch;
    private BrightnessModeObserver mBrightnessModeObserver;
    private BrightnessPoint mBrightnessPoint;
    private Handler mCallbackHandler;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public MonotoneSplineManager.Spline mCurrentSpline;
    /* access modifiers changed from: private */
    public float mDarkLuxThreshold;
    private float mDeltaDownScale;
    private float mDeltaUpScale;
    /* access modifiers changed from: private */
    public EventUploader mEventUploader;
    private HighBrightnessMode mHighBrightnessMode;
    private boolean mInitEnd;
    /* access modifiers changed from: private */
    public float mInstantLux;
    /* access modifiers changed from: private */
    public volatile boolean mIsDragging;
    /* access modifiers changed from: private */
    public boolean mIsNeedLockBright;
    /* access modifiers changed from: private */
    public int mLastScreenBrightnessModeSetting = 1;
    private List<String> mLauncherAppList;
    /* access modifiers changed from: private */
    public float mLeftScaleInDarkEnv;
    private LogSwitchObserver mLogSwitchObserver;
    /* access modifiers changed from: private */
    public float mLux;
    private final LimitedQueue<Float> mLuxQueue = new LimitedQueue<>(Math.max(4, 6));
    /* access modifiers changed from: private */
    public ModelConfig mModelConfig;
    private MonotoneSplineManager mMonotoneSplineManager;
    /* access modifiers changed from: private */
    public volatile boolean mNextLuxWillChanged;
    private int mNoiseLuxCount = 0;
    /* access modifiers changed from: private */
    public float mNormalMaxBrightness;
    /* access modifiers changed from: private */
    public float mRightScaleInDarkEnv;
    private RomUpdateHelper mRomUpdateHelper;
    private ScreenOffReceiver mScreenOffReceiver;
    private SensorRecorder mSensorRecorder;
    /* access modifiers changed from: private */
    public final ShockAbsorber mShockAbsorber = new ShockAbsorber();
    /* access modifiers changed from: private */
    public float mStableRightMinLux;
    /* access modifiers changed from: private */
    public float mStableSmallChangeScaleInDarkEnv;
    /* access modifiers changed from: private */
    public float mStableSmallRightMinLux;
    /* access modifiers changed from: private */
    public BrightnessPoint mTargetBrightnessPoint;
    private TrainedReceiver mTrainedReceiver;
    /* access modifiers changed from: private */
    public Handler mWorkHandler;

    private static class WorkHandler extends Handler {
        private WeakReference mRef;

        private WorkHandler(AIBrightnessModel o, Looper looper) {
            super(looper);
            this.mRef = new WeakReference(o);
        }

        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            long date = 0;
            if (data != null) {
                date = data.getLong(AIBrightnessModel.EXTRA_DATE);
            }
            AIBrightnessModel model = (AIBrightnessModel) this.mRef.get();
            if (model != null) {
                switch (msg.what) {
                    case 1:
                        model.handleBrightnessByUser(model.mLux, ((Float) msg.obj).floatValue());
                        model.reportBrightnessEvent(0, ((Float) msg.obj).floatValue(), model.mLux, date);
                        removeMessages(2);
                        sendEmptyMessageDelayed(2, 3000);
                        return;
                    case 2:
                        boolean unused = model.mIsDragging = false;
                        return;
                    case 3:
                        ColorAILog.d(AIBrightnessModel.TAG, "handleMessage MSG_LUX_CHANGED, mLux:" + model.mLux);
                        if (model.handleUpdateBrightness(model.mLux, date)) {
                            model.onTargetBrightnessChange();
                            return;
                        }
                        return;
                    case 4:
                        ColorAILog.i(AIBrightnessModel.TAG, "handleMessage MSG_FORCE_UPDATE_BRIGHTNESS.");
                        model.onTargetBrightnessChange();
                        return;
                    case 5:
                        ColorAILog.i(AIBrightnessModel.TAG, "handleMessage MSG_RESET_DRAG_LINE.");
                        model.reset();
                        if (model.handleUpdateBrightness(model.mInstantLux, true, date)) {
                            model.onTargetBrightnessChange();
                        }
                        model.reportBrightnessEvent(2, model.mTargetBrightnessPoint.y, 0.0f, date);
                        return;
                    case 6:
                        boolean unused2 = model.mNextLuxWillChanged = true;
                        return;
                    case 7:
                        MonotoneSplineManager.Spline curSpline = MonotoneSplineManager.getInstance(model.mContext).getCurrentSpline();
                        if (curSpline != null) {
                            MonotoneSplineManager.Spline unused3 = model.mCurrentSpline = curSpline;
                            BrightnessPoint curPoint = curSpline.findPointByX(curSpline.restoreTransX(model.mTargetBrightnessPoint.x));
                            ColorAILog.d(AIBrightnessModel.TAG, "Update brightness when spline is changed:" + curPoint);
                            if (curPoint != null) {
                                model.mTargetBrightnessPoint.y = curPoint.y;
                                model.onTargetBrightnessChange();
                                model.reportBrightnessEvent(3, curSpline.restoreTransX(model.mTargetBrightnessPoint.x), curPoint.y, date);
                                return;
                            }
                            return;
                        }
                        return;
                    case 8:
                        boolean unused4 = model.mIsNeedLockBright = ((Boolean) msg.obj).booleanValue();
                        return;
                    case 9:
                        ColorAILog.i(AIBrightnessModel.TAG, "handleMessage MSG_CURRENT_SPLINE_UPDATE.");
                        MonotoneSplineManager.Spline newCurSpline = MonotoneSplineManager.getInstance(model.mContext).getCurrentSpline();
                        MonotoneSplineManager.Spline oldCurSpline = model.mCurrentSpline;
                        if (newCurSpline != null) {
                            BrightnessPoint dragPoint = null;
                            if (oldCurSpline != null) {
                                dragPoint = oldCurSpline.getRealDragPoint();
                            }
                            MonotoneSplineManager.Spline unused5 = model.mCurrentSpline = newCurSpline;
                            ColorAILog.d(AIBrightnessModel.TAG, "mCurrentSpline = " + model.mCurrentSpline);
                            if (dragPoint != null) {
                                MonotoneSplineManager.getInstance(model.mContext).drag(dragPoint.x, dragPoint.y);
                                return;
                            }
                            return;
                        }
                        return;
                    case 10:
                        ColorAILog.i(AIBrightnessModel.TAG, "Unlock brightness timeout, report exception!");
                        model.mEventUploader.reportEvent(3);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public AIBrightnessModel(Context context, ModelConfig modelConfig) {
        this.mContext = context;
        init(context, modelConfig);
        readRestoreDataFromSettings();
    }

    @Override // com.android.server.display.ai.utils.AppSwitchDetectorUtil.IAppSwitchListener
    public void onAppSwitchToForeground(String pkgName) {
        List<String> list = this.mLauncherAppList;
        if (list != null && list.contains(pkgName) && this.mIsNeedLockBright) {
            Handler handler = this.mWorkHandler;
            if (handler != null) {
                handler.sendEmptyMessageDelayed(10, 2000);
            }
            this.mIsNeedLockBright = false;
            ColorAILog.i(TAG, "Force unlock brightness when switch to launcher.");
        }
    }

    private void readRestoreDataFromSettings() {
        BrightnessPoint currPoint = null;
        BrightnessPoint dragPoint = null;
        String currPointStr = Settings.System.getString(this.mContext.getContentResolver(), SETTINGS_AI_BRIGHTNESS_CURR_POINT);
        if (currPointStr != null) {
            String[] splitStr = currPointStr.split(POINT_SPLIT_SIGN);
            if (splitStr.length == 2) {
                try {
                    currPoint = new BrightnessPoint(Float.parseFloat(splitStr[0]), Float.parseFloat(splitStr[1]));
                } catch (NumberFormatException e) {
                    ColorAILog.w(TAG, "readRestoreDataFromSettings read currPoint error, " + e);
                }
            }
        }
        String dragPointStr = Settings.System.getString(this.mContext.getContentResolver(), SETTINGS_AI_BRIGHTNESS_DRAG_POINT);
        if (dragPointStr != null) {
            String[] splitStr2 = dragPointStr.split(POINT_SPLIT_SIGN);
            if (splitStr2.length == 2) {
                try {
                    dragPoint = new BrightnessPoint(Float.parseFloat(splitStr2[0]), Float.parseFloat(splitStr2[1]));
                } catch (NumberFormatException e2) {
                    ColorAILog.w(TAG, "readRestoreDataFromSettings read dragPoint error, " + e2);
                }
            }
        }
        if (dragPoint != null) {
            ColorAILog.i(TAG, "readRestoreDataFromSettings restore dragPoint:" + dragPoint);
            this.mEventUploader.reportEvent("ABM create, restore drag point.");
            handleBrightnessByUser(dragPoint.x, dragPoint.y);
        } else {
            ColorAILog.w(TAG, "readRestoreDataFromSettings not dragPoint, skip this step");
            this.mEventUploader.reportEvent("ABM create, no drag point.");
        }
        if (currPoint != null) {
            ColorAILog.i(TAG, "readRestoreDataFromSettings restore currPoint:" + currPoint);
            this.mEventUploader.reportEvent("ABM create, restore current point.");
            BrightnessPoint point = this.mCurrentSpline.findPointByX(currPoint.x);
            if (point != null) {
                this.mLux = this.mCurrentSpline.restoreTransX(point.x);
                this.mBrightnessPoint.x = point.x;
                this.mBrightnessPoint.y = point.y;
                this.mTargetBrightnessPoint.x = point.x;
                this.mTargetBrightnessPoint.y = point.y;
                return;
            }
            return;
        }
        ColorAILog.w(TAG, "readRestoreDataFromSettings currPoint == null, use default state.");
        this.mEventUploader.reportEvent("ABM create, no current point.");
    }

    public void setBrightnessByUser(float brightness) {
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            this.mIsDragging = true;
            Message msg = handler.obtainMessage(1, Float.valueOf(brightness));
            Bundle data = new Bundle();
            data.putLong(EXTRA_DATE, System.currentTimeMillis());
            msg.setData(data);
            handler.removeMessages(1);
            handler.sendMessageDelayed(msg, 50);
        }
    }

    public void setStateChanged(int msgId, Bundle extraData) {
        if (extraData == null) {
            ColorAILog.e(TAG, "setStateChanged bundle is null.");
            return;
        }
        ColorAILog.i(TAG, "setStateChanged : " + msgId + " with extra : " + logBundle(extraData));
        if (msgId == 0) {
            boolean isOpened = extraData.getBoolean("game_lock_switch", false);
            Handler handler = this.mWorkHandler;
            if (handler != null) {
                handler.obtainMessage(8, Boolean.valueOf(isOpened)).sendToTarget();
                if (!isOpened) {
                    handler.removeMessages(10);
                }
            }
        }
    }

    private static String logBundle(Bundle extraData) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : extraData.keySet()) {
            stringBuilder.append("[ ");
            stringBuilder.append(key);
            stringBuilder.append(", ");
            stringBuilder.append(extraData.getBoolean(key));
            stringBuilder.append("]");
        }
        return stringBuilder.toString();
    }

    public void setLux(float lux) {
        if (BrightnessConstants.sDebugDetailsLux) {
            ColorAILog.i(TAG, "setLux, lux:" + lux);
        }
        SensorRecorder sensorRecorder = this.mSensorRecorder;
        if (!(sensorRecorder != null && sensorRecorder.isPlaybacking())) {
            setLuxInner(lux);
            SensorRecorder sensorRecorder2 = this.mSensorRecorder;
            if (sensorRecorder2 != null) {
                sensorRecorder2.write(lux);
            }
        }
    }

    public void onScreenEvent(boolean isScreenOn) {
        Handler handler;
        ColorAILog.d(TAG, "onScreenEvent, isScreenOn:" + isScreenOn);
        if (!isScreenOn && (handler = this.mWorkHandler) != null) {
            handler.sendEmptyMessageDelayed(6, 200);
        }
        this.mShockAbsorber.resetDampingFactor();
    }

    @Override // com.android.server.display.ai.sensorrecorder.SensorRecorder.SensorPlayback
    public void onSensorPlayback(float lux) {
        if (BrightnessConstants.sDebugDetailsLux) {
            ColorAILog.i(TAG, "onSensorPlayback, lux:" + lux);
        }
        setLuxInner(lux);
    }

    private void init(Context context, ModelConfig modelConfig) {
        this.mEventUploader = new EventUploader(this.mContext);
        boolean isSellModeOpen = false;
        BrightnessConstants.sDebugDetailsLux = Settings.System.getInt(this.mContext.getContentResolver(), BrightnessConstants.DETAILS_LUX, 0) != 0;
        initModelConfig(context, modelConfig);
        registerBroadcast();
        ColorAILog.d(TAG, "init mModelConfig:" + this.mModelConfig);
        if (Settings.System.getInt(context.getContentResolver(), SETTINGS_GLOBAL_HBM_SELL_MODE, 0) == 1) {
            isSellModeOpen = true;
        }
        boolean isGlobalHBMOpen = context.getPackageManager().hasSystemFeature("oppo.display.screen.gloablehbm.support");
        ColorAILog.d(TAG, "init isGlobalHBMOpen:" + isGlobalHBMOpen + ", isSellModeOpen:" + isSellModeOpen);
        if (isGlobalHBMOpen && !isSellModeOpen) {
            this.mHighBrightnessMode = new HighBrightnessMode(this.mModelConfig);
        }
        HandlerThread handlerThread = new HandlerThread("auto_brightness_handler");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        if (looper != null) {
            this.mWorkHandler = new WorkHandler(looper);
        } else {
            ColorAILog.e(TAG, "AIBrightnessModel init error, looper == null!!");
        }
        this.mMonotoneSplineManager = MonotoneSplineManager.getInstance(this.mContext);
        this.mMonotoneSplineManager.addSplineListener(new MonotoneSplineManager.ISplineListener() {
            /* class com.android.server.display.ai.AIBrightnessModel.AnonymousClass1 */

            @Override // com.android.server.display.ai.MonotoneSplineManager.ISplineListener
            public void onSplineChanged(String splineName) {
                ColorAILog.d(AIBrightnessModel.TAG, "onSplineChanged, Spline is changed:" + splineName);
                Message message = Message.obtain();
                message.what = 7;
                message.obj = splineName;
                Bundle data = new Bundle();
                data.putLong(AIBrightnessModel.EXTRA_DATE, System.currentTimeMillis());
                message.setData(data);
                AIBrightnessModel.this.mWorkHandler.sendMessage(message);
            }

            @Override // com.android.server.display.ai.MonotoneSplineManager.ISplineListener
            public void onSplineUpdated(String splineName) {
                ColorAILog.d(AIBrightnessModel.TAG, "onSplineUpdated, Spline is updated:" + splineName);
                Handler handler = AIBrightnessModel.this.mWorkHandler;
                if (handler != null) {
                    Message message = Message.obtain();
                    message.what = 9;
                    handler.sendMessage(message);
                }
            }
        });
        AppSwitchDetectorUtil appSwitchDetectorUtil = AppSwitchDetectorUtil.getInstance(this.mContext);
        this.mLauncherAppList = appSwitchDetectorUtil.getLauncherAppList(this.mContext);
        if (!this.mLauncherAppList.isEmpty()) {
            appSwitchDetectorUtil.register(this.mLauncherAppList);
            appSwitchDetectorUtil.addListener(this);
        }
        this.mCurrentSpline = this.mMonotoneSplineManager.getCurrentSpline();
        ColorAILog.d(TAG, "init(), mCurrentSpline: " + this.mCurrentSpline);
        this.mDeltaUpScale = this.mModelConfig.getDeltaUpScale();
        this.mDeltaDownScale = this.mModelConfig.getDeltaDownScale();
        this.mNormalMaxBrightness = this.mModelConfig.getNormalMaxBrightness();
        this.mDarkLuxThreshold = this.mModelConfig.getDarkLuxThreshold();
        this.mLeftScaleInDarkEnv = this.mModelConfig.getLeftScaleInDarkEnv();
        this.mRightScaleInDarkEnv = this.mModelConfig.getRightScaleInDarkEnv();
        this.mStableSmallChangeScaleInDarkEnv = this.mModelConfig.getStableSmallChangeScaleInDarkEnv();
        this.mStableRightMinLux = this.mModelConfig.getStableRightMinLux();
        this.mStableSmallRightMinLux = this.mModelConfig.getStableSmallRightMinLux();
        this.mBrightnessPoint = new BrightnessPoint(0.0f, this.mModelConfig.getDefaultBrightness());
        this.mTargetBrightnessPoint = BrightnessPoint.createPoint(this.mBrightnessPoint);
        this.mSensorRecorder = new SensorRecorder(context);
        this.mSensorRecorder.registerPlayback(this);
        if (BrightnessConstants.sDebugDetailsLux) {
            ColorAILog.i(TAG, this.mModelConfig.toString());
        }
        this.mBrightnessModeObserver = new BrightnessModeObserver(this.mWorkHandler);
        this.mBrightnessModeObserver.register(this.mContext);
        this.mInitEnd = true;
    }

    private void registerBroadcast() {
        this.mTrainedReceiver = TrainedReceiver.getInstance();
        this.mTrainedReceiver.register(this.mContext);
        this.mRomUpdateHelper = RomUpdateHelper.getInstance();
        this.mRomUpdateHelper.register(this.mContext);
        this.mLogSwitchObserver = LogSwitchObserver.getInstance(this.mContext);
        this.mLogSwitchObserver.register();
        this.mScreenOffReceiver = ScreenOffReceiver.getInstance();
        this.mScreenOffReceiver.register(this.mContext);
        this.mScreenOffReceiver.setScreenListener(new ScreenOffReceiver.IScreenListener() {
            /* class com.android.server.display.ai.$$Lambda$AIBrightnessModel$WNlKlMw_RK5ka0NMQgJ9J7Z2xI */

            @Override // com.android.server.display.ai.broadcastreceiver.ScreenOffReceiver.IScreenListener
            public final void onScreenOff() {
                AIBrightnessModel.this.lambda$registerBroadcast$0$AIBrightnessModel();
            }
        });
        this.mAIBrightnessTrainSwitch = AIBrightnessTrainSwitch.getInstance();
        this.mAIBrightnessTrainSwitch.register(this.mContext);
    }

    public /* synthetic */ void lambda$registerBroadcast$0$AIBrightnessModel() {
        onScreenEvent(false);
        Handler handler = this.mCallbackHandler;
        if (handler != null) {
            handler.removeMessages(4);
            this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(4));
            ColorAILog.i(TAG, "onScreenOff.");
        }
    }

    private void unregisterBroadcast() {
        TrainedReceiver trainedReceiver = this.mTrainedReceiver;
        if (trainedReceiver != null) {
            trainedReceiver.unregister();
        }
        RomUpdateHelper romUpdateHelper = this.mRomUpdateHelper;
        if (romUpdateHelper != null) {
            romUpdateHelper.unregister(this.mContext);
        }
        BootCompletedReceiver.getInstance(this.mContext).unregister();
        LogSwitchObserver logSwitchObserver = this.mLogSwitchObserver;
        if (logSwitchObserver != null) {
            logSwitchObserver.unregister();
        }
        ScreenOffReceiver screenOffReceiver = this.mScreenOffReceiver;
        if (screenOffReceiver != null) {
            screenOffReceiver.unregister();
        }
        AIBrightnessTrainSwitch aIBrightnessTrainSwitch = this.mAIBrightnessTrainSwitch;
        if (aIBrightnessTrainSwitch != null) {
            aIBrightnessTrainSwitch.unregister(this.mContext);
        }
        BrightnessModeObserver brightnessModeObserver = this.mBrightnessModeObserver;
        if (brightnessModeObserver != null) {
            brightnessModeObserver.unregister(this.mContext);
        }
    }

    private void initModelConfig(Context context, ModelConfig modelConfig) {
        ModelConfigUtil modelConfigUtil = ModelConfigUtil.getInstance();
        modelConfigUtil.initialize(context);
        modelConfigUtil.setDefaultModelConfig(modelConfig);
        this.mModelConfig = ModelConfigUtil.getInstance().getModelConfig(context);
        ColorAILog.d(TAG, "AIBrightnessModel mModelConfig:" + this.mModelConfig);
    }

    private void setLuxInner(float lux) {
        this.mInstantLux = lux;
        if (!this.mIsDragging) {
            stableLuxInOneSide(lux);
        } else if (BrightnessConstants.sDebugDetailsLux) {
            ColorAILog.i(TAG, "setLuxInner, is dragging, do not receive sensor data.");
        }
    }

    private float findNearestLux(float lux, LimitedQueue<Float> queue) {
        float distance = 2.14748365E9f;
        float finalLux = 0.0f;
        Iterator it = queue.iterator();
        while (it.hasNext()) {
            float luxOne = ((Float) it.next()).floatValue();
            float distanceTmp = Math.abs(lux - luxOne);
            if (distanceTmp < distance) {
                finalLux = luxOne;
                distance = distanceTmp;
            }
        }
        return finalLux;
    }

    private float findSecondNearestLux(float lux, LimitedQueue<Float> queue) {
        if (BrightnessConstants.sDebugDetailsLux) {
            ColorAILog.i(TAG, "findSecondNearestLux, lux");
        }
        dumpLuxQueue(lux);
        float nearestMaxLux = 2.14748365E9f;
        float secondNearestLux = 2.14748365E9f;
        Iterator it = queue.iterator();
        while (it.hasNext()) {
            float luxOne = ((Float) it.next()).floatValue();
            float distanceTmp = Math.abs(lux - luxOne);
            if (distanceTmp < Math.abs(lux - nearestMaxLux)) {
                secondNearestLux = nearestMaxLux;
                nearestMaxLux = luxOne;
            } else if (distanceTmp < Math.abs(lux - secondNearestLux)) {
                secondNearestLux = luxOne;
            }
        }
        if (BrightnessConstants.sDebugDetailsLux) {
            ColorAILog.i(TAG, "findSecondNearestLux, nearestMaxLux:" + nearestMaxLux);
            ColorAILog.i(TAG, "findSecondNearestLux, secondNearestLux:" + secondNearestLux);
        }
        return secondNearestLux;
    }

    private void updateLux(LimitedQueue<Float> queue) {
        this.mLux = findSecondNearestLux(this.mLux, queue);
    }

    private void clearLuxQueue() {
        this.mLuxQueue.clear();
        this.mNoiseLuxCount = 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x015d, code lost:
        if (com.android.server.display.ai.utils.BrightnessConstants.sDebugDetailsLux == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x015f, code lost:
        com.android.server.display.ai.utils.ColorAILog.i(com.android.server.display.ai.AIBrightnessModel.TAG, "stableLuxInOneSide end");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        return;
     */
    private void stableLuxInOneSide(float lux) {
        synchronized (this.mLuxQueue) {
            float lastStableLux = this.mLux;
            boolean isStableLux = false;
            if (this.mNextLuxWillChanged) {
                this.mNextLuxWillChanged = false;
                clearLuxQueue();
                this.mLux = lux;
                if (BrightnessConstants.sDebugDetailsLux) {
                    ColorAILog.i(TAG, "stableLuxInOneSide, force LUX_CHANGED, lux:" + this.mLux);
                }
                Handler handler = this.mWorkHandler;
                if (handler != null) {
                    Message msg = handler.obtainMessage(3);
                    Bundle data = new Bundle();
                    data.putLong(EXTRA_DATE, System.currentTimeMillis());
                    msg.setData(data);
                    msg.sendToTarget();
                }
            } else if (this.mLuxQueue.size() == 0) {
                this.mLuxQueue.add(Float.valueOf(lux));
                dumpLuxQueue(lux);
            } else {
                float luxFirst = this.mLuxQueue.get(0).floatValue();
                if (lux - lastStableLux != 0.0f && (luxFirst - lastStableLux) / (lux - lastStableLux) > 0.0f) {
                    isStableLux = true;
                }
                int checkSize = lux > lastStableLux ? 4 : 6;
                if (!isStableLux) {
                    this.mNoiseLuxCount++;
                    if (this.mNoiseLuxCount >= 1) {
                        if (BrightnessConstants.sDebugDetailsLux) {
                            ColorAILog.i(TAG, "stableLuxInOneSide, noise is overflow(" + this.mNoiseLuxCount + ") and reset list, this one noise lux is :" + lux + ", lastStableLux: " + lastStableLux);
                        }
                        clearLuxQueue();
                        this.mLuxQueue.add(Float.valueOf(lux));
                    } else if (BrightnessConstants.sDebugDetailsLux) {
                        ColorAILog.i(TAG, "stableLuxInOneSide, noise is (" + this.mNoiseLuxCount + "), lux:" + lux + ", lastStableLux: " + lastStableLux);
                    }
                } else {
                    this.mLuxQueue.add(Float.valueOf(lux));
                    if (this.mLuxQueue.size() >= checkSize) {
                        updateLux(this.mLuxQueue);
                        Handler handler2 = this.mWorkHandler;
                        if (handler2 != null) {
                            Message msg2 = handler2.obtainMessage(3);
                            Bundle data2 = new Bundle();
                            data2.putLong(EXTRA_DATE, System.currentTimeMillis());
                            msg2.setData(data2);
                            msg2.sendToTarget();
                        }
                        if (BrightnessConstants.sDebugDetailsLux) {
                            ColorAILog.i(TAG, "stableLuxInOneSide, do update, mLux:" + this.mLux);
                        }
                        clearLuxQueue();
                    } else if (BrightnessConstants.sDebugDetailsLux) {
                        ColorAILog.i(TAG, "stableLuxInOneSide, not enough, continue check");
                    }
                }
                dumpLuxQueue(lux);
            }
        }
    }

    /* access modifiers changed from: private */
    public void resetByUser() {
        ColorAILog.d(TAG, "resetByUser");
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage(5);
            Bundle data = new Bundle();
            data.putLong(EXTRA_DATE, System.currentTimeMillis());
            msg.setData(data);
            msg.setData(data);
            msg.sendToTarget();
        }
    }

    public void setCallbackHandler(Handler callbackHandler) {
        ColorAILog.d(TAG, "setCallbackHandler");
        this.mCallbackHandler = callbackHandler;
        this.mRomUpdateHelper.setRUSChangeHandler(callbackHandler);
        Handler workHandler = this.mWorkHandler;
        if (workHandler != null) {
            workHandler.obtainMessage(4).sendToTarget();
        }
    }

    public float getNextChange(int targetY, float currY, float timeDelta) {
        float deltaY = Math.abs(((float) targetY) - currY);
        boolean isUp = ((float) targetY) - currY > 0.0f;
        float scale = isUp ? this.mDeltaUpScale : this.mDeltaDownScale;
        float yMax = this.mNormalMaxBrightness;
        float minDistance = this.mModelConfig.getMinBrightnessChange();
        if (isUp) {
            if (((float) targetY) > HIGH_BRIGHTNESS_PERCENT * yMax) {
                scale *= 2.0f;
            } else if (((float) targetY) < LOW_BRIGHTNESS_PERCENT * yMax) {
                scale /= LOW_UP_SCALE;
            }
        } else if (currY < LOWEST_AREA) {
            minDistance *= LOWEST_SCALE;
        } else if (currY < LOW_BRIGHTNESS_PERCENT * yMax) {
            scale /= LOW_SCALE;
        }
        if (currY > this.mModelConfig.getNormalMaxBrightness()) {
            scale *= HBM_SCALE;
        }
        if (((int) currY) == targetY) {
            return deltaY;
        }
        if (deltaY > 1.0f) {
            deltaY = Math.abs((timeDelta / ONE_FRAME_TIME) * Math.max(minDistance, Math.min(this.mModelConfig.getSpeedMultipleInDistance() * deltaY, this.mModelConfig.getMaxBrightnessChange())) * scale);
        }
        return deltaY * ((float) ((int) (this.mNormalMaxBrightness / 1000.0f)));
    }

    public void setManualBrightnessByUser(float brightness) {
    }

    private void saveCurrPointToSettings(BrightnessPoint currPoint) {
        String value;
        if (currPoint != null) {
            value = this.mCurrentSpline.restoreTransX(currPoint.x) + POINT_SPLIT_SIGN + currPoint.y;
        } else {
            value = null;
        }
        Settings.System.putString(this.mContext.getContentResolver(), SETTINGS_AI_BRIGHTNESS_CURR_POINT, value);
        ColorAILog.i(TAG, "saveCurrPointToSettings, currPoint:" + value);
    }

    private void saveDragPointToSettings(BrightnessPoint dragPoint) {
        String value;
        if (dragPoint != null) {
            value = String.valueOf(this.mCurrentSpline.restoreTransX(dragPoint.x) + POINT_SPLIT_SIGN + String.valueOf(dragPoint.y));
        } else {
            value = null;
        }
        Settings.System.putString(this.mContext.getContentResolver(), SETTINGS_AI_BRIGHTNESS_DRAG_POINT, value);
        ColorAILog.i(TAG, "saveDragPointToSettings, dragPoint:" + value);
    }

    private void dumpLuxQueue(float lux) {
        if (BrightnessConstants.sDebugDetailsLux) {
            StringBuilder sb = new StringBuilder();
            sb.append("setLux, lux:");
            sb.append(lux);
            sb.append(", [");
            int luxLength = this.mLuxQueue.size();
            for (int i = 0; i < luxLength; i++) {
                sb.append(this.mLuxQueue.get(i));
                if (i == luxLength - 1) {
                    sb.append("]");
                } else {
                    sb.append(POINT_SPLIT_SIGN);
                }
            }
            ColorAILog.i(TAG, sb.toString());
        }
    }

    /* access modifiers changed from: private */
    public void handleBrightnessByUser(float x, float y) {
        if (y < 2.0f) {
            ColorAILog.w(TAG, "handleBrightnessByUser warning, y is " + y);
            return;
        }
        BrightnessPoint draggingPoint = this.mMonotoneSplineManager.drag(x, y);
        ColorAILog.d(TAG, "handleBrightnessByUser before set. mBrightnessPoint.y:" + this.mBrightnessPoint.y + ", mTargetBrightnessPoint.y:" + this.mTargetBrightnessPoint.y);
        reportDraggingEvent();
        if (draggingPoint != null) {
            setPointImmediately(draggingPoint);
            saveCurrPointToSettings(this.mTargetBrightnessPoint);
            saveDragPointToSettings(draggingPoint);
        }
    }

    /* access modifiers changed from: private */
    public void reset() {
        synchronized (this.mLuxQueue) {
            this.mLuxQueue.clear();
        }
        this.mMonotoneSplineManager.reset();
        saveCurrPointToSettings(null);
        saveDragPointToSettings(null);
        ColorAILog.d(TAG, "reset targetBrightnessPoint.");
    }

    private void setPointImmediately(BrightnessPoint point) {
        this.mBrightnessPoint.x = point.x;
        this.mBrightnessPoint.y = point.y;
        this.mTargetBrightnessPoint.x = point.x;
        this.mTargetBrightnessPoint.y = point.y;
    }

    /* access modifiers changed from: private */
    public boolean handleUpdateBrightness(float lux, long date) {
        if (!this.mIsNeedLockBright) {
            return handleUpdateBrightness(lux, false, date);
        }
        ColorAILog.i(TAG, "don`t update bright because is was locked.");
        return false;
    }

    /* access modifiers changed from: private */
    public boolean handleUpdateBrightness(float lux, boolean isReset, long date) {
        BrightnessPoint nextPoint;
        int i = 0;
        if (!this.mInitEnd) {
            return false;
        }
        this.mMonotoneSplineManager.updateDayNightMode();
        ColorAILog.d(TAG, "handleUpdateBrightness mLux:" + lux);
        HighBrightnessMode highBrightnessMode = this.mHighBrightnessMode;
        BrightnessPoint hbmPoint = highBrightnessMode != null ? highBrightnessMode.getNextPoint(this.mTargetBrightnessPoint.y, lux, isReset) : null;
        if (hbmPoint == null) {
            BrightnessPoint currentInLinePoint = this.mCurrentSpline.findPointByX(lux);
            StablePoints stablePoints = getBrightnessStablePoints(!isReset);
            if (stablePoints == null) {
                ColorAILog.w(TAG, "handleUpdateBrightness stablePoints == null");
                return false;
            } else if (currentInLinePoint != null) {
                BrightnessPoint nextPoint2 = stablePoints.getNextPoint(currentInLinePoint);
                if (!stablePoints.verifyPoint(this.mTargetBrightnessPoint, currentInLinePoint, nextPoint2)) {
                    ColorAILog.w(TAG, "handleUpdateBrightness verify error, try again");
                    StablePoints stablePoints2 = getBrightnessStablePoints(!isReset);
                    if (stablePoints2 == null) {
                        ColorAILog.w(TAG, "handleUpdateBrightness stablePoints == null");
                        return false;
                    }
                    BrightnessPoint nextPoint3 = stablePoints2.getNextPoint(currentInLinePoint);
                    ColorAILog.w(TAG, "handleUpdateBrightness try again verifyResult:" + stablePoints2.verifyPoint(this.mTargetBrightnessPoint, currentInLinePoint, nextPoint3));
                    nextPoint = nextPoint3;
                } else {
                    nextPoint = nextPoint2;
                }
            } else {
                nextPoint = null;
            }
        } else if (hbmPoint == HighBrightnessMode.NO_NEED_CHANGE_POINT) {
            ColorAILog.i(TAG, "handleUpdateBrightness hbm but not change. " + lux + ", " + this.mTargetBrightnessPoint.y);
            return false;
        } else {
            BrightnessPoint nextPoint4 = transPoint(hbmPoint);
            ColorAILog.i(TAG, "handleUpdateBrightness use hbm point, hbmPoint:" + hbmPoint);
            nextPoint = nextPoint4;
        }
        if (nextPoint == null) {
            ColorAILog.d(TAG, "not find nextPoint, do nothing.");
            return false;
        } else if (this.mTargetBrightnessPoint.y == nextPoint.y) {
            ColorAILog.d(TAG, "handleUpdateBrightness mTargetBrightnessPoint.y == nextPoint.y");
            return false;
        } else {
            ColorAILog.i(TAG, "handleUpdateBrightness brightness change from " + this.mTargetBrightnessPoint.y + ", to " + nextPoint.y);
            boolean isLowLux = this.mCurrentSpline.restoreTransX(nextPoint.x) < this.mStableRightMinLux;
            ShockAbsorber shockAbsorber = this.mShockAbsorber;
            if (nextPoint.y > this.mTargetBrightnessPoint.y) {
                i = 1;
            }
            shockAbsorber.setBrightnessDirection(i, isLowLux);
            this.mTargetBrightnessPoint.x = nextPoint.x;
            this.mTargetBrightnessPoint.y = nextPoint.y;
            ColorAILog.i(TAG, "handleUpdateBrightness TargetBrightnessPoint update, targetPoint: (" + this.mCurrentSpline.restoreTransX(this.mTargetBrightnessPoint.x) + ", " + this.mTargetBrightnessPoint.y + ")");
            reportBrightnessEvent(1, this.mTargetBrightnessPoint.y, Float.valueOf(lux).floatValue(), date);
            return true;
        }
    }

    private BrightnessPoint transPoint(BrightnessPoint point) {
        point.x = this.mCurrentSpline.transX(point.x);
        return point;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.max(float, float):float}
     arg types: [int, float]
     candidates:
      ClspMth{java.lang.Math.max(double, double):double}
      ClspMth{java.lang.Math.max(int, int):int}
      ClspMth{java.lang.Math.max(long, long):long}
      ClspMth{java.lang.Math.max(float, float):float} */
    /* access modifiers changed from: private */
    public void onTargetBrightnessChange() {
        Handler handler = this.mCallbackHandler;
        if (handler != null) {
            handler.removeMessages(1);
            float targetBrightness = Math.max(2.0f, this.mTargetBrightnessPoint.y);
            this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(1, Float.valueOf(targetBrightness)));
            ColorAILog.i(TAG, "onTargetBrightnessChange brightness change to " + targetBrightness);
            saveCurrPointToSettings(this.mTargetBrightnessPoint);
        }
    }

    public StablePoints getBrightnessStablePoints(boolean darkFix) {
        BrightnessPoint brightnessPoint = this.mTargetBrightnessPoint;
        if (brightnessPoint != null) {
            return new StablePoints(brightnessPoint, darkFix);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void reportBrightnessEvent(int eventType, float brightness, float lux, long date) {
        if (this.mContext != null) {
            Intent intent = new Intent(ACTION_BRIGHTNESS_EVENT);
            intent.putExtra(EXTRA_EVENT_TYPE, eventType);
            intent.putExtra(EXTRA_BRIGHTNESS, brightness);
            intent.putExtra(EXTRA_LUX, lux);
            if (date <= 0) {
                date = System.currentTimeMillis();
            }
            intent.putExtra(EXTRA_DATE, date);
            UserHandle userHandleAll = UserHandle.ALL;
            if (userHandleAll == null) {
                ColorAILog.e(TAG, "reportBrightnessEvent create userHandleAll failed.");
                return;
            }
            this.mContext.sendBroadcastAsUser(intent, userHandleAll, "oppo.permission.OPPO_COMPONENT_SAFE");
            ColorAILog.i(TAG, "reportBrightnessEvent intent:" + intent);
        }
    }

    private void reportDraggingEvent() {
        if (this.mEventUploader != null) {
            String dragPointDumpString = "mDragSrcPoints is null";
            MonotoneSplineManager.Spline spline = this.mCurrentSpline;
            if (spline != null) {
                dragPointDumpString = spline.getDragPointDumpString();
            }
            EventUploader eventUploader = this.mEventUploader;
            eventUploader.reportEvent(1, "dragging, getDragPointDumpString:" + dragPointDumpString);
        }
    }

    public void release() {
        unregisterBroadcast();
        AppSwitchDetectorUtil.getInstance(this.mContext).unregister();
        MonotoneSplineManager monotoneSplineManager = this.mMonotoneSplineManager;
        if (monotoneSplineManager != null) {
            monotoneSplineManager.onDestroy();
        }
        this.mCallbackHandler = null;
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            handler.getLooper().quit();
            this.mWorkHandler = null;
        }
        this.mSensorRecorder.release();
        this.mEventUploader.release();
    }

    private class StablePoints {
        /* access modifiers changed from: private */
        public BrightnessPoint[] mLargeChangeStablePoints;
        /* access modifiers changed from: private */
        public BrightnessPoint[] mSmallChangeStablePoints;
        private BrightnessPoint[] mSmallChangeTargetPoints;

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: com.android.server.display.ai.AIBrightnessModel.StablePoints.<init>(com.android.server.display.ai.AIBrightnessModel, com.android.server.display.ai.bean.BrightnessPoint, boolean):void
         arg types: [com.android.server.display.ai.AIBrightnessModel, com.android.server.display.ai.bean.BrightnessPoint, int]
         candidates:
          com.android.server.display.ai.AIBrightnessModel.StablePoints.<init>(com.android.server.display.ai.AIBrightnessModel, com.android.server.display.ai.bean.BrightnessPoint, com.android.server.display.ai.AIBrightnessModel$1):void
          com.android.server.display.ai.AIBrightnessModel.StablePoints.<init>(com.android.server.display.ai.AIBrightnessModel, com.android.server.display.ai.bean.BrightnessPoint, boolean):void */
        private StablePoints(AIBrightnessModel aIBrightnessModel, BrightnessPoint targetPoint) {
            this(targetPoint, true);
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: ClspMth{java.lang.Math.max(float, float):float}
         arg types: [int, float]
         candidates:
          ClspMth{java.lang.Math.max(double, double):double}
          ClspMth{java.lang.Math.max(int, int):int}
          ClspMth{java.lang.Math.max(long, long):long}
          ClspMth{java.lang.Math.max(float, float):float} */
        private StablePoints(BrightnessPoint targetPoint, boolean darkFix) {
            float smallChangePercent;
            float rightY;
            float realX = AIBrightnessModel.this.mCurrentSpline.restoreTransX(targetPoint.x);
            float y = targetPoint.y;
            List<BrightnessPoint> currPoints = AIBrightnessModel.this.mCurrentSpline.getCurrFinalPoints();
            if (currPoints != null && !currPoints.isEmpty()) {
                float yMinInCurrLine = currPoints.get(0).y;
                float yMaxInCurrLine = currPoints.get(currPoints.size() - 1).y;
                y = y < yMinInCurrLine ? yMinInCurrLine : y;
                if (y > yMaxInCurrLine) {
                    y = yMaxInCurrLine;
                }
            }
            float yMax = AIBrightnessModel.this.mNormalMaxBrightness;
            this.mLargeChangeStablePoints = new BrightnessPoint[2];
            this.mSmallChangeStablePoints = new BrightnessPoint[2];
            this.mSmallChangeTargetPoints = new BrightnessPoint[2];
            boolean darkLuxFix = darkFix && realX < AIBrightnessModel.this.mDarkLuxThreshold;
            if (darkLuxFix) {
                smallChangePercent = AIBrightnessModel.this.mModelConfig.getStableSmallChangePercent() * AIBrightnessModel.this.mStableSmallChangeScaleInDarkEnv;
            } else {
                smallChangePercent = AIBrightnessModel.this.mModelConfig.getStableSmallChangePercent();
            }
            float y1Left = Math.max(0.0f, y - (AIBrightnessModel.this.mModelConfig.getStableDownMinPercent() * yMax));
            float y2Left = AIBrightnessModel.this.mModelConfig.getStableDownSelfPercent() * y;
            float leftY = Math.max(y1Left, y2Left);
            leftY = darkLuxFix ? Math.max(0.0f, (AIBrightnessModel.this.mLeftScaleInDarkEnv * (leftY - y)) + y) : leftY;
            this.mLargeChangeStablePoints[0] = AIBrightnessModel.this.mCurrentSpline.findPointByY(leftY);
            this.mSmallChangeStablePoints[0] = AIBrightnessModel.this.mCurrentSpline.findPointByY(y + ((leftY - y) * smallChangePercent));
            this.mSmallChangeTargetPoints[0] = AIBrightnessModel.this.mCurrentSpline.findPointByY((AIBrightnessModel.this.mModelConfig.getStableSmallChangeTargetPercent() * (leftY - y)) + y);
            ColorAILog.i(AIBrightnessModel.TAG, "StablePoints y1Left:" + y1Left + ", y2Left:" + y2Left + ", leftY:" + leftY);
            float rightY2 = Math.min(Math.min(yMax, (AIBrightnessModel.this.mModelConfig.getStableUpMinPercent() * yMax) + y), AIBrightnessModel.this.mModelConfig.getStableUpSelfPercent() * y);
            if (darkLuxFix) {
                rightY = Math.min(yMax, y + (AIBrightnessModel.this.mRightScaleInDarkEnv * (rightY2 - y)));
                BrightnessPoint minRightPoint = AIBrightnessModel.this.mCurrentSpline.findPointByX(AIBrightnessModel.this.mStableRightMinLux);
                if (minRightPoint != null) {
                    rightY = Math.max(rightY, minRightPoint.y);
                }
            } else {
                rightY = rightY2;
            }
            this.mLargeChangeStablePoints[1] = AIBrightnessModel.this.mCurrentSpline.findPointByY(rightY);
            this.mSmallChangeStablePoints[1] = AIBrightnessModel.this.mCurrentSpline.findPointByY(y + ((rightY - y) * smallChangePercent));
            if (darkLuxFix && AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeStablePoints[1].x) < AIBrightnessModel.this.mStableSmallRightMinLux) {
                this.mSmallChangeStablePoints[1].x = AIBrightnessModel.this.mCurrentSpline.transX(AIBrightnessModel.this.mStableSmallRightMinLux);
            }
            this.mSmallChangeTargetPoints[1] = AIBrightnessModel.this.mCurrentSpline.findPointByY((AIBrightnessModel.this.mModelConfig.getStableSmallChangeTargetPercent() * (rightY - y)) + y);
            ColorAILog.i(AIBrightnessModel.TAG, "StablePoints (x,y):(" + realX + AIBrightnessModel.POINT_SPLIT_SIGN + targetPoint.y + "), fix y:" + y + "\ny1Left:" + y1Left + ", y2Left:" + y2Left + ", leftY:" + leftY + "\nStablePoints, largeLeftX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[0].x) + ", largeRightX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[1].x) + "\ndrag spline:" + AIBrightnessModel.this.mCurrentSpline.getDragPointDumpString());
            float leftX = AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[0].x);
            float rightX = AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[1].x);
            if (realX < leftX || realX > rightX) {
                ColorAILog.i(AIBrightnessModel.TAG, "StablePoints, target point is not in line, do not use ShockAbsorber.");
                return;
            }
            float smallLeftX = AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeStablePoints[0].x);
            float smallRightX = AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeStablePoints[1].x);
            float[] dampingFactor = AIBrightnessModel.this.mShockAbsorber.getDampingFactor();
            float leftX2 = Math.max(0.0f, realX - Math.max(2.0f, (realX - leftX) * dampingFactor[0]));
            float smallLeftX2 = Math.max(0.0f, realX - Math.max(2.0f, (realX - smallLeftX) * dampingFactor[0]));
            float rightX2 = realX + Math.max(2.0f, (rightX - realX) * dampingFactor[1]);
            this.mLargeChangeStablePoints[0].x = AIBrightnessModel.this.mCurrentSpline.transX(leftX2);
            this.mLargeChangeStablePoints[1].x = AIBrightnessModel.this.mCurrentSpline.transX(rightX2);
            this.mSmallChangeStablePoints[0].x = AIBrightnessModel.this.mCurrentSpline.transX(smallLeftX2);
            this.mSmallChangeStablePoints[1].x = AIBrightnessModel.this.mCurrentSpline.transX(Math.max(2.0f, (smallRightX - realX) * dampingFactor[1]) + realX);
            ColorAILog.i(AIBrightnessModel.TAG, "StablePoints, DAMPING_DIRECTION_DOWN:" + dampingFactor[0] + ", DAMPING_DIRECTION_UP:" + dampingFactor[1] + "\nStablePoints, largeLeftX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[0].x) + ", largeRightX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[1].x) + ", smallLeftX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeStablePoints[0].x) + ", smallRightX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeStablePoints[1].x));
        }

        /* access modifiers changed from: private */
        public boolean verifyPoint(BrightnessPoint lastPoint, BrightnessPoint currPoint, BrightnessPoint nextPoint) {
            BrightnessPoint[] brightnessPointArr = this.mLargeChangeStablePoints;
            if (!(brightnessPointArr == null || this.mSmallChangeStablePoints == null || this.mSmallChangeTargetPoints == null)) {
                boolean verifyLeft = brightnessPointArr[0].y <= this.mSmallChangeStablePoints[0].y && this.mSmallChangeStablePoints[0].y <= this.mSmallChangeTargetPoints[0].y;
                boolean verifyRight = this.mLargeChangeStablePoints[1].y >= this.mSmallChangeStablePoints[1].y && this.mSmallChangeStablePoints[1].y >= this.mSmallChangeTargetPoints[1].y;
                if (!verifyLeft || !verifyRight) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("leftLarge:");
                    sb.append(AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[0].x));
                    sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                    sb.append(this.mLargeChangeStablePoints[0].y);
                    sb.append(", leftSmall:");
                    sb.append(AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeStablePoints[0].x));
                    sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                    sb.append(this.mSmallChangeStablePoints[0].y);
                    sb.append(", leftSmallTarget:");
                    sb.append(AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeTargetPoints[0].x));
                    sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                    sb.append(this.mSmallChangeTargetPoints[0].y);
                    sb.append(", rightLarge:");
                    sb.append(AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[1].x));
                    sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                    sb.append(this.mLargeChangeStablePoints[1].y);
                    sb.append(", rightSmall:");
                    sb.append(AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeStablePoints[1].x));
                    sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                    sb.append(this.mSmallChangeStablePoints[1].y);
                    sb.append(", rightSmallTarget:");
                    sb.append(AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeTargetPoints[1].x));
                    sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                    sb.append(this.mSmallChangeTargetPoints[1].y);
                    if (lastPoint != null) {
                        sb.append(", lastPoint:");
                        sb.append(AIBrightnessModel.this.mCurrentSpline.restoreTransX(lastPoint.x));
                        sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                        sb.append(lastPoint.y);
                    } else {
                        sb.append(", lastPoint:null");
                    }
                    if (currPoint != null) {
                        sb.append(", currPoint:");
                        sb.append(AIBrightnessModel.this.mCurrentSpline.restoreTransX(currPoint.x));
                        sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                        sb.append(currPoint.y);
                    } else {
                        sb.append(", currPoint:null");
                    }
                    if (nextPoint != null) {
                        sb.append(", nextPoint:");
                        sb.append(AIBrightnessModel.this.mCurrentSpline.restoreTransX(nextPoint.x));
                        sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                        sb.append(nextPoint.y);
                    } else {
                        sb.append(", nextPoint:null");
                    }
                    if (AIBrightnessModel.this.mCurrentSpline.getDragPoints() != null) {
                        sb.append(AIBrightnessModel.POINT_SPLIT_SIGN);
                        sb.append(AIBrightnessModel.this.mCurrentSpline.getDragPointDumpString());
                    }
                    String logString = sb.toString();
                    ColorAILog.w(AIBrightnessModel.TAG, "verifyPoint error, verifyLeft:" + verifyLeft + ", verifyRight:" + verifyRight + AIBrightnessModel.POINT_SPLIT_SIGN + logString);
                    HashMap<String, String> logMap = new HashMap<>();
                    logMap.put(BrightnessConstants.Statistics.KEY_VERIFY_POINT, logString);
                    OppoStatistics.onCommon(AIBrightnessModel.this.mContext, "ai_brightness", BrightnessConstants.Statistics.EVENT_ID_VERIFY, logMap, false);
                    return false;
                }
                ColorAILog.d(AIBrightnessModel.TAG, "verifyPoint ok");
            }
            return true;
        }

        /* access modifiers changed from: private */
        public BrightnessPoint getNextPoint(BrightnessPoint point) {
            BrightnessPoint nextPoint = null;
            boolean isLargeChange = this.mLargeChangeStablePoints == null || point.x <= this.mLargeChangeStablePoints[0].x || point.x >= this.mLargeChangeStablePoints[1].x;
            String log = "";
            ColorAILog.d(AIBrightnessModel.TAG, "current spline:" + AIBrightnessModel.this.mCurrentSpline.getName() + " isTrained:" + AIBrightnessModel.this.mCurrentSpline.isTrained() + " points: " + AIBrightnessModel.this.mCurrentSpline.getPoints());
            if (isLargeChange) {
                if (this.mLargeChangeStablePoints != null) {
                    log = "getNextPoint isLargeChange, point.x: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(point.x) + ", point.y: " + point.y + ", largeLeftX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[0].x) + ", largeRightX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mLargeChangeStablePoints[1].x);
                    ColorAILog.i(AIBrightnessModel.TAG, log);
                } else {
                    ColorAILog.i(AIBrightnessModel.TAG, "getNextPoint isLargeChange, point.x: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(point.x) + ", point.y: " + point.y + ", mLargeChangeStablePoints is null");
                }
                nextPoint = new BrightnessPoint(point.x, point.y);
            } else {
                if (this.mSmallChangeStablePoints != null) {
                    ColorAILog.i(AIBrightnessModel.TAG, "getNextPoint point.x: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(point.x) + ", point.y: " + point.y + ", smallLeftX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeStablePoints[0].x) + ", smallRightX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeStablePoints[1].x));
                } else {
                    ColorAILog.w(AIBrightnessModel.TAG, "mSmallChangeStablePoints == null");
                }
                boolean isSmallChangeLeft = this.mSmallChangeStablePoints != null && point.x <= this.mSmallChangeStablePoints[0].x;
                boolean isSmallChangeRight = this.mSmallChangeStablePoints != null && point.x >= this.mSmallChangeStablePoints[1].x;
                if (isSmallChangeLeft) {
                    log = "getNextPoint isSmallChangeLeft, point.x: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(point.x) + ", point.y: " + point.y + ", smallTargetX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeTargetPoints[0].x) + ", smallTargetY: " + this.mSmallChangeTargetPoints[0].y;
                    ColorAILog.i(AIBrightnessModel.TAG, log);
                    nextPoint = new BrightnessPoint(this.mSmallChangeTargetPoints[0].x, this.mSmallChangeTargetPoints[0].y);
                } else if (isSmallChangeRight) {
                    log = "getNextPoint isSmallChangeRight, point.x: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(point.x) + ", point.y: " + point.y + ", smallTargetX: " + AIBrightnessModel.this.mCurrentSpline.restoreTransX(this.mSmallChangeTargetPoints[1].x) + ", smallTargetY: " + this.mSmallChangeTargetPoints[1].y;
                    ColorAILog.i(AIBrightnessModel.TAG, log);
                    nextPoint = new BrightnessPoint(this.mSmallChangeTargetPoints[1].x, this.mSmallChangeTargetPoints[1].y);
                }
            }
            float[] ys = AIBrightnessModel.this.mModelConfig.getYs();
            float minBrightness = (ys == null || ys.length <= 0) ? 0.0f : ys[0];
            if (nextPoint != null && nextPoint.y < minBrightness && !TextUtils.isEmpty(log)) {
                AIBrightnessModel.this.mEventUploader.reportEvent(2, log);
            }
            if (nextPoint == null) {
                ColorAILog.i(AIBrightnessModel.TAG, "getNextPoint return null, is not largeChange and not smallChange");
            }
            return nextPoint;
        }
    }

    /* access modifiers changed from: private */
    public static class ShockAbsorber {
        private static final int DAMPING_DIRECTION_DOWN = 0;
        private static final int DAMPING_DIRECTION_UP = 1;
        private static final float DAMPING_FACTOR = 1.05f;
        private static final float DAMPING_FACTOR_DEFAULT = 1.0f;
        private static final float DAMPING_FACTOR_LOW_LIGHT = 1.2f;
        private static final float DAMP_START_COUNT = 2.0f;
        private static final float MAX_DAMPING_FACTOR = 1.4f;
        private static final float MAX_DAMPING_FACTOR_LOW_LIGHT_UP = 1.6f;
        private static final float MIN_LUX = 2.0f;
        private int mDampingDirection;
        private float[] mDampingFactor;
        private int mShakeCount;

        private ShockAbsorber() {
            this.mDampingFactor = new float[]{DAMPING_FACTOR_DEFAULT, DAMPING_FACTOR_DEFAULT};
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: ClspMth{java.lang.Math.min(float, float):float}
         arg types: [int, float]
         candidates:
          ClspMth{java.lang.Math.min(double, double):double}
          ClspMth{java.lang.Math.min(long, long):long}
          ClspMth{java.lang.Math.min(int, int):int}
          ClspMth{java.lang.Math.min(float, float):float} */
        /* access modifiers changed from: private */
        public void setBrightnessDirection(int direction, boolean isLowLux) {
            if (this.mDampingDirection == direction) {
                ColorAILog.i(AIBrightnessModel.TAG, "setBrightnessDirection, reset default, direction:" + direction);
                resetDampingFactor();
            } else {
                this.mShakeCount++;
                if (((float) this.mShakeCount) > 2.0f) {
                    ColorAILog.i(AIBrightnessModel.TAG, "setBrightnessDirection, before direction:" + direction + ", mDampingFactor:" + Arrays.toString(this.mDampingFactor));
                    if (direction == 0) {
                        if (isLowLux) {
                            float[] fArr = this.mDampingFactor;
                            fArr[1] = Math.min((float) MAX_DAMPING_FACTOR_LOW_LIGHT_UP, fArr[0] * DAMPING_FACTOR_LOW_LIGHT);
                        } else {
                            float[] fArr2 = this.mDampingFactor;
                            fArr2[1] = Math.min(1.4f, fArr2[0] * DAMPING_FACTOR);
                        }
                        this.mDampingFactor[0] = 1.0f;
                    } else if (direction == 1) {
                        float[] fArr3 = this.mDampingFactor;
                        fArr3[0] = Math.min(1.4f, fArr3[1] * DAMPING_FACTOR);
                        this.mDampingFactor[1] = 1.0f;
                    }
                    ColorAILog.i(AIBrightnessModel.TAG, "setBrightnessDirection, after direction:" + direction + ", mDampingFactor:" + Arrays.toString(this.mDampingFactor));
                } else {
                    ColorAILog.i(AIBrightnessModel.TAG, "setBrightnessDirection, shake not enough, ignore:" + this.mShakeCount);
                }
            }
            this.mDampingDirection = direction;
        }

        /* access modifiers changed from: private */
        public float[] getDampingFactor() {
            return (float[]) this.mDampingFactor.clone();
        }

        /* access modifiers changed from: private */
        public void resetDampingFactor() {
            ColorAILog.i(AIBrightnessModel.TAG, "resetDampingFactor");
            this.mShakeCount = 0;
            float[] fArr = this.mDampingFactor;
            fArr[0] = 1.0f;
            fArr[1] = 1.0f;
        }
    }

    private static class LimitedQueue<E> extends LinkedList<E> {
        private int limit;

        public LimitedQueue(int limit2) {
            this.limit = limit2;
        }

        @Override // java.util.AbstractCollection, java.util.List, java.util.Collection, java.util.AbstractList, java.util.Queue, java.util.LinkedList, java.util.Deque
        public boolean add(E o) {
            super.add(o);
            while (size() > this.limit) {
                super.remove();
            }
            return true;
        }
    }

    private class BrightnessModeObserver extends ContentObserver {
        private final Uri BRIGHTNESS_MODE_URI;
        private boolean mHasRegistered;

        private BrightnessModeObserver(Handler handler) {
            super(handler);
            this.BRIGHTNESS_MODE_URI = Settings.System.getUriFor("screen_brightness_mode");
            this.mHasRegistered = false;
        }

        public void onChange(boolean selfChange) {
            ColorAILog.i(AIBrightnessModel.TAG, "onChange");
            int autoMode = Settings.System.getIntForUser(AIBrightnessModel.this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2);
            if (AIBrightnessModel.this.mLastScreenBrightnessModeSetting != autoMode && autoMode == 0) {
                AIBrightnessModel.this.resetByUser();
            }
            int unused = AIBrightnessModel.this.mLastScreenBrightnessModeSetting = autoMode;
            if (AIBrightnessModel.this.mEventUploader != null) {
                EventUploader access$1500 = AIBrightnessModel.this.mEventUploader;
                access$1500.reportEvent(0, Integer.valueOf(autoMode), "mode change:" + autoMode);
            }
        }

        /* access modifiers changed from: private */
        public void register(Context context) {
            if (!this.mHasRegistered) {
                try {
                    context.getContentResolver().registerContentObserver(this.BRIGHTNESS_MODE_URI, false, this, -1);
                    this.mHasRegistered = true;
                } catch (Exception e) {
                    ColorAILog.e(AIBrightnessModel.TAG, "register exception " + e.getMessage());
                }
            }
        }

        /* access modifiers changed from: private */
        public void unregister(Context context) {
            try {
                context.getContentResolver().unregisterContentObserver(this);
                this.mHasRegistered = false;
            } catch (Exception e) {
                ColorAILog.e(AIBrightnessModel.TAG, "unregister exception " + e.getMessage());
            }
        }
    }

    public void dump(PrintWriter pw) {
        boolean isGlobalHBMOpen = this.mContext.getPackageManager().hasSystemFeature("oppo.display.screen.gloablehbm.support");
        pw.println();
        pw.println("Color Auto Configuration:");
        StringBuilder sb = new StringBuilder();
        sb.append("  isSupportHBM = ");
        sb.append(isGlobalHBMOpen ? "True" : "False");
        pw.println(sb.toString());
        pw.println("  luxArray = " + Arrays.toString(this.mModelConfig.getXs()));
        pw.println("  levelArray = " + Arrays.toString(this.mModelConfig.getYs()));
        if (isGlobalHBMOpen) {
            pw.println("  HBMLuxArray = " + Arrays.toString(this.mModelConfig.getHbmXs()));
            pw.println("  HBMLevelArray = " + Arrays.toString(this.mModelConfig.getHbmYs()));
        }
    }

    public void printAutoLuxInterval() {
        printAutoLuxIntervalInner(this.mCurrentSpline);
    }

    private void printAutoLuxIntervalInner(final MonotoneSplineManager.Spline splineline) {
        new Thread(new Runnable() {
            /* class com.android.server.display.ai.AIBrightnessModel.AnonymousClass2 */

            public void run() {
                List<BrightnessPoint> finalPoints = splineline.getMonotonePoints();
                List<ArrayList<Integer>> csvCache = new ArrayList<>();
                if (finalPoints != null) {
                    for (BrightnessPoint point : finalPoints) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        StablePoints stablePoints = new StablePoints(point);
                        int originX = (int) splineline.restoreTransX(point.x);
                        int originY = (int) point.y;
                        int largeChangeXLeft = (int) splineline.restoreTransX(stablePoints.mLargeChangeStablePoints[0].x);
                        int largeChangeXRight = (int) splineline.restoreTransX(stablePoints.mLargeChangeStablePoints[1].x);
                        int largeChangeYLeft = (int) stablePoints.mLargeChangeStablePoints[0].y;
                        int largeChangeYRight = (int) stablePoints.mLargeChangeStablePoints[1].y;
                        int smallChangeXLeft = (int) splineline.restoreTransX(stablePoints.mSmallChangeStablePoints[0].x);
                        int smallChangeXRight = (int) splineline.restoreTransX(stablePoints.mSmallChangeStablePoints[1].x);
                        ColorAILog.i(AIBrightnessModel.TAG, "SplineLine " + originX + ", " + originY + ", " + largeChangeXLeft + ", " + largeChangeXRight + ", " + largeChangeYLeft + ", " + largeChangeYRight + ", " + smallChangeXLeft + ", " + smallChangeXRight);
                        ArrayList<Integer> rowData = new ArrayList<>();
                        rowData.clear();
                        rowData.add(Integer.valueOf(originX));
                        rowData.add(Integer.valueOf(originY));
                        rowData.add(Integer.valueOf(largeChangeXLeft));
                        rowData.add(Integer.valueOf(largeChangeXRight));
                        rowData.add(Integer.valueOf(largeChangeYLeft));
                        rowData.add(Integer.valueOf(largeChangeYRight));
                        rowData.add(Integer.valueOf(smallChangeXLeft));
                        rowData.add(Integer.valueOf(smallChangeXRight));
                        csvCache.add(rowData);
                    }
                    AIBrightnessModel.this.saveToCsvFile(csvCache);
                    return;
                }
                ColorAILog.w(AIBrightnessModel.TAG, "mDefaultSplineLine is not inited.");
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void saveToCsvFile(List<ArrayList<Integer>> csvCache) {
        ArrayList<String> csvHead = new ArrayList<String>() {
            /* class com.android.server.display.ai.AIBrightnessModel.AnonymousClass3 */

            {
                add("originX");
                add("originY");
                add("largeChangeXLeft");
                add("largeChangeXRight");
                add("largeChangeYLeft");
                add("largeChangeYRight");
                add("smallChangeXLeft");
                add("smallChangeXRight");
            }
        };
        File file = new File(BrightnessConstants.INTERVAL_PATH);
        File path = file.getParentFile();
        BufferedWriter csvWriter = null;
        try {
            if (!path.exists()) {
                path.mkdir();
            }
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            BufferedWriter csvWriter2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE);
            writeRow(csvHead, csvWriter2);
            for (ArrayList<Integer> rowData : csvCache) {
                csvWriter2.newLine();
                writeRow(rowData, csvWriter2);
            }
            try {
                csvWriter2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (Throwable th) {
            if (csvWriter != null) {
                try {
                    csvWriter.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
    }

    private void writeRow(ArrayList<? extends Object> rowDatas, BufferedWriter bufferedWriter) throws IOException {
        for (int i = 0; i < rowDatas.size(); i++) {
            if (i != 0) {
                bufferedWriter.write(POINT_SPLIT_SIGN);
            }
            bufferedWriter.write("\"" + rowDatas.get(i) + "\"");
        }
    }
}
