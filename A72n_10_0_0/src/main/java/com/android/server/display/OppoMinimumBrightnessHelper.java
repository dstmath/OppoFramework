package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.UiModeManagerService;
import java.util.Calendar;

public class OppoMinimumBrightnessHelper {
    private static final int BAD_VALUE = -1;
    private static boolean DEBUG = false;
    private static final String DEBUG_PRO = "oppo.brightness.debug";
    private static final int MSG_FIRST_INDEX = 100;
    private static final int MSG_FORCE_UPDATE_BRIGHTNESS = 101;
    private static final String TAG = "OppoMinimumBrightnessHelper";
    private static final int mBlackNightStatus = 3;
    private static final int mBrightDayStatus = 1;
    private static int mCurrentEnvironmentStatus = -1;
    private static final int mDuskStatus = 2;
    private static final int mMorningStatus = 0;
    private static final int mTenNitConvertBrightnessValue = 80;
    private IntentFilter intentFilter;
    private String[] mActions = {"android.intent.action.TIME_TICK", "android.intent.action.TIME_SET"};
    private Calendar mCalendar;
    private Context mContext;
    private MinimumAutoBrightnessHandler mHandler = null;
    private boolean mNeedInit = true;
    private OppoAutomaticBrightnessController mOppoAutomaticBrightnessController;
    private OppoBrightUtils mOppoBrightUtils;
    private boolean mStatusChanged = false;
    private TimeChangeReceiver timeChangeReceiver;

    public OppoMinimumBrightnessHelper(OppoAutomaticBrightnessController abc, Context context, Handler handler) {
        this.mOppoAutomaticBrightnessController = abc;
        this.mContext = context;
        this.mHandler = new MinimumAutoBrightnessHandler(handler.getLooper());
        this.mOppoBrightUtils = OppoBrightUtils.getInstance();
        init();
    }

    private void init() {
        if (this.mNeedInit) {
            if (DEBUG) {
                Slog.d(TAG, "init OppoMinimumBrightnessHelper");
            }
            updateDebugFlag();
            setReceiver(this.mActions);
            updateCurrentEnvironmentStatus();
            this.mNeedInit = false;
        }
    }

    private void updateDebugFlag() {
        if ("user".equals(SystemProperties.get("ro.build.type", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN))) {
            DEBUG = false;
        } else {
            DEBUG = SystemProperties.getBoolean(DEBUG_PRO, true);
        }
    }

    private void setReceiver(String[] actions) {
        this.intentFilter = new IntentFilter();
        for (String str : actions) {
            this.intentFilter.addAction(str);
        }
        this.timeChangeReceiver = new TimeChangeReceiver();
        this.mContext.registerReceiver(this.timeChangeReceiver, this.intentFilter);
    }

    /* access modifiers changed from: package-private */
    public class TimeChangeReceiver extends BroadcastReceiver {
        TimeChangeReceiver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:18:0x003d  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0064  */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode != -1513032534) {
                if (hashCode != 502473491) {
                    if (hashCode == 505380757 && action.equals("android.intent.action.TIME_SET")) {
                        c = 0;
                        if (c != 0) {
                            OppoMinimumBrightnessHelper.this.updateCurrentEnvironmentStatus();
                            if (OppoMinimumBrightnessHelper.this.mStatusChanged) {
                                OppoMinimumBrightnessHelper.this.mHandler.sendMessage(OppoMinimumBrightnessHelper.this.mHandler.obtainMessage(101));
                                return;
                            }
                            return;
                        } else if (c != 1) {
                            if (c != 2) {
                            }
                            return;
                        } else {
                            OppoMinimumBrightnessHelper.this.updateCurrentEnvironmentStatus();
                            if (OppoMinimumBrightnessHelper.this.mStatusChanged) {
                                OppoMinimumBrightnessHelper.this.mHandler.sendMessage(OppoMinimumBrightnessHelper.this.mHandler.obtainMessage(101));
                                return;
                            }
                            return;
                        }
                    }
                } else if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                    c = 2;
                    if (c != 0) {
                    }
                }
            } else if (action.equals("android.intent.action.TIME_TICK")) {
                c = 1;
                if (c != 0) {
                }
            }
            c = 65535;
            if (c != 0) {
            }
        }
    }

    public int checkMiniumBrightness(int brightness) {
        if (brightness > 0 && brightness < 80 && !this.mOppoBrightUtils.getManualBrightnessFlag()) {
            int i = mCurrentEnvironmentStatus;
            if (i != 0) {
                if (i == 1) {
                    brightness = 80;
                    if (DEBUG) {
                        Slog.d(TAG, "BrightDayStatus, change current brightness = 80");
                    }
                } else if (i == 2) {
                    brightness = 80;
                    if (DEBUG) {
                        Slog.d(TAG, "DuskStatus, change current brightness = 80");
                    }
                } else if (i != 3) {
                    if (DEBUG) {
                        Slog.d(TAG, "BAD_VALUE, skip this brightness = " + brightness);
                    }
                } else if (DEBUG) {
                    Slog.d(TAG, "BlackNightStatus, apply current brightness = " + brightness);
                }
            } else if (DEBUG) {
                Slog.d(TAG, "MorningStatus, apply current brightness = " + brightness);
            }
        }
        if (brightness >= 80 && this.mOppoBrightUtils.getManualBrightnessFlag()) {
            this.mOppoBrightUtils.setManualBrightnessFlag(false);
        }
        return brightness;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCurrentEnvironmentStatus() {
        this.mCalendar = Calendar.getInstance();
        int tempStatus = mCurrentEnvironmentStatus;
        Calendar calendar = this.mCalendar;
        if (calendar != null) {
            int normalizationValue = (calendar.get(11) * 100) + this.mCalendar.get(12);
            if (DEBUG) {
                Slog.d(TAG, "normalizationValue = " + normalizationValue);
            }
            if (normalizationValue >= 530 && normalizationValue < 700) {
                mCurrentEnvironmentStatus = 0;
            } else if (normalizationValue >= 700 && normalizationValue < 1700) {
                mCurrentEnvironmentStatus = 1;
            } else if (normalizationValue >= 1700 && normalizationValue < 2100) {
                mCurrentEnvironmentStatus = 2;
            } else if (normalizationValue < 2100 || normalizationValue >= 2400) {
                mCurrentEnvironmentStatus = 3;
            } else {
                mCurrentEnvironmentStatus = 3;
            }
            if (tempStatus == mCurrentEnvironmentStatus) {
                this.mStatusChanged = false;
            } else {
                this.mStatusChanged = true;
            }
        } else {
            mCurrentEnvironmentStatus = -1;
        }
    }

    /* access modifiers changed from: private */
    public class MinimumAutoBrightnessHandler extends Handler {
        public MinimumAutoBrightnessHandler(Looper looper) {
            super(looper);
            Slog.d(OppoMinimumBrightnessHelper.TAG, "MinimumAutoBrightnessHandler init");
        }

        public void handleMessage(Message msg) {
            Slog.d(OppoMinimumBrightnessHelper.TAG, "handleMessage:" + msg.what);
            if (msg.what == 101) {
                OppoMinimumBrightnessHelper.this.mOppoAutomaticBrightnessController.setForceUpdate(true);
                OppoMinimumBrightnessHelper.this.mOppoAutomaticBrightnessController.callbackUpdateBrightness();
            }
        }
    }
}
