package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.health.V1_0.HealthInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.lights.OppoLightsManager;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class OppoBaseLed {
    private static boolean DEBUG = false;
    private static final int DELAY_UPDATE_LIGHT = 500;
    private static final int MSG_UPDATE_LIGHT = 1;
    private static final String TAG_LED = "BatteryLed";
    private boolean mBatteryLowHint;
    private boolean mChargingHint;
    protected Context mContext = null;
    private Handler mHandler;
    private Handler mLightHandler = new Handler() {
        /* class com.android.server.OppoBaseLed.AnonymousClass3 */

        public void handleMessage(Message msg) {
            OppoBaseLed.this.onUpdateLights();
        }
    };
    protected LightsManager mLightsManager = null;
    private int mLowBatteryWarningLevel;
    private boolean mScreenOn;
    private SettingsObserver mSettingsObserver = null;
    private final int mSuperBatteryFullARGB;
    private final int mSuperBatteryLedOff;
    private final int mSuperBatteryLedOn;
    private final int mSuperBatteryLowARGB;
    private final int mSuperBatteryMediumARGB;
    private boolean mUseOppoLedPolicy = true;

    /* access modifiers changed from: private */
    public interface OnUpdateCallback {
        void onSettingUpdateCallback(String str, boolean z);
    }

    /* access modifiers changed from: protected */
    public abstract Light getBatteryLightInstance();

    /* access modifiers changed from: protected */
    public abstract void onUpdateLights();

    /* access modifiers changed from: private */
    public class SettingsObserverItem {
        private OnUpdateCallback mUpdateCallback = null;
        private String mUriKeyword = null;

        public SettingsObserverItem(String uriKeyword, OnUpdateCallback updateCallback) {
            this.mUriKeyword = uriKeyword;
            this.mUpdateCallback = updateCallback;
        }

        public String getUriKeyword() {
            return this.mUriKeyword;
        }

        public void onUpdateListener(Uri uri, boolean selfChange) {
            String str;
            OnUpdateCallback onUpdateCallback;
            String uriKeyword = uri != null ? uri.toString() : null;
            if (uriKeyword != null && (str = this.mUriKeyword) != null && str.equals(uriKeyword) && (onUpdateCallback = this.mUpdateCallback) != null) {
                onUpdateCallback.onSettingUpdateCallback(uriKeyword, selfChange);
            }
        }
    }

    /* access modifiers changed from: private */
    public class SettingsObserver extends ContentObserver {
        private ContentResolver mContentResolver = null;
        private ArrayList<SettingsObserverItem> mObserverItemsList = new ArrayList<>();
        private Context mServiceContext = null;

        public SettingsObserver(Handler handler, Context context) {
            super(handler);
            this.mServiceContext = context;
        }

        public void registerObserverItem(SettingsObserverItem observerItem) {
            if (observerItem != null) {
                this.mObserverItemsList.add(observerItem);
            }
        }

        public void startObserve() {
            Uri keywordUri;
            if (this.mContentResolver == null) {
                this.mContentResolver = this.mServiceContext.getContentResolver();
            }
            Iterator<SettingsObserverItem> it = this.mObserverItemsList.iterator();
            while (it.hasNext()) {
                String keyword = it.next().getUriKeyword();
                if (!(keyword == null || (keywordUri = Settings.System.getUriFor(keyword)) == null)) {
                    this.mContentResolver.registerContentObserver(keywordUri, false, this);
                }
            }
        }

        public int getIntValueFromSettings(String uriKeyword, int defaultValue) {
            if (uriKeyword == null) {
                Slog.e(OppoBaseLed.TAG_LED, "getIntValueFromSettings: uriKeyword illegal.");
                return -1;
            }
            if (this.mContentResolver == null) {
                this.mContentResolver = this.mServiceContext.getContentResolver();
            }
            return Settings.System.getInt(this.mContentResolver, uriKeyword, defaultValue);
        }

        public void onChange(boolean selfChange, Uri uri) {
            Iterator<SettingsObserverItem> it = this.mObserverItemsList.iterator();
            while (it.hasNext()) {
                it.next().onUpdateListener(uri, selfChange);
            }
        }
    }

    public OppoBaseLed(Context context, LightsManager lightsManager) {
        this.mContext = context;
        this.mLightsManager = lightsManager;
        this.mSuperBatteryLowARGB = context.getResources().getInteger(17694866);
        this.mSuperBatteryMediumARGB = context.getResources().getInteger(17694867);
        this.mSuperBatteryFullARGB = context.getResources().getInteger(17694863);
        this.mSuperBatteryLedOn = context.getResources().getInteger(17694865);
        this.mSuperBatteryLedOff = context.getResources().getInteger(17694864);
        this.mLowBatteryWarningLevel = context.getResources().getInteger(17694829);
        this.mHandler = new Handler();
        this.mSettingsObserver = new SettingsObserver(this.mHandler, context);
        this.mScreenOn = true;
    }

    /* access modifiers changed from: protected */
    public boolean isIgnoreUpdateLights(HealthInfo healthInfo) {
        if (!updateOppoLightsNormalLocked(healthInfo)) {
            return true;
        }
        if (!useOppoBreathLedPolicy()) {
            return false;
        }
        updateOppoLightsBatteryChargingLocked(healthInfo);
        return true;
    }

    public void turnOffBatteryLights() {
        Light lightInstance = getBatteryLightInstance();
        if (lightInstance != null) {
            lightInstance.turnOff();
        }
    }

    public void handleScreenState(boolean screenon) {
        if (screenon) {
            handleScreenOn();
        } else {
            handleScreenOff();
        }
    }

    public void setDebugSwitch(boolean on) {
        DEBUG = on;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateBatteryLowHintEnabledSettings() {
        boolean lowerPowerEnabled = this.mSettingsObserver.getIntValueFromSettings("oppo_breath_led_low_power", 0) != 0;
        if (this.mBatteryLowHint == lowerPowerEnabled) {
            return false;
        }
        this.mBatteryLowHint = lowerPowerEnabled;
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateChargingHintEnabledSettings() {
        boolean chargHintEnabled = this.mSettingsObserver.getIntValueFromSettings("oppo_breath_led_charge", 0) != 0;
        if (this.mChargingHint == chargHintEnabled) {
            return false;
        }
        this.mChargingHint = chargHintEnabled;
        return true;
    }

    private void initSettingsObserver() {
        SettingsObserverItem lowPowerObserver = new SettingsObserverItem("oppo_breath_led_low_power", new OnUpdateCallback() {
            /* class com.android.server.OppoBaseLed.AnonymousClass1 */

            @Override // com.android.server.OppoBaseLed.OnUpdateCallback
            public void onSettingUpdateCallback(String uriKeyword, boolean selfChange) {
                if (OppoBaseLed.this.updateBatteryLowHintEnabledSettings()) {
                    OppoBaseLed.this.onUpdateLights();
                }
            }
        });
        SettingsObserverItem ledChargeObserver = new SettingsObserverItem("oppo_breath_led_charge", new OnUpdateCallback() {
            /* class com.android.server.OppoBaseLed.AnonymousClass2 */

            @Override // com.android.server.OppoBaseLed.OnUpdateCallback
            public void onSettingUpdateCallback(String uriKeyword, boolean selfChange) {
                if (OppoBaseLed.this.updateChargingHintEnabledSettings()) {
                    OppoBaseLed.this.onUpdateLights();
                }
            }
        });
        this.mSettingsObserver.registerObserverItem(lowPowerObserver);
        this.mSettingsObserver.registerObserverItem(ledChargeObserver);
        this.mSettingsObserver.startObserve();
    }

    public void systemReady() {
        initSettingsObserver();
        updateBatteryLowHintEnabledSettings();
        updateChargingHintEnabledSettings();
        onUpdateLights();
    }

    public boolean useOppoBreathLedPolicy() {
        return this.mUseOppoLedPolicy;
    }

    private void handleScreenOn() {
        this.mScreenOn = true;
        this.mLightHandler.removeMessages(1);
        updateBatteryLowHintEnabledSettings();
        updateChargingHintEnabledSettings();
        turnOffBatteryLights();
    }

    private void handleScreenOff() {
        this.mScreenOn = false;
        updateBatteryLowHintEnabledSettings();
        updateChargingHintEnabledSettings();
        this.mLightHandler.sendEmptyMessageDelayed(1, 500);
    }

    public boolean updateOppoLightsNormalLocked(HealthInfo mHealthInfo) {
        boolean continueUpdate = true;
        if (DEBUG) {
            Slog.d(TAG_LED, "ScreenOn = " + this.mScreenOn + "; mBatteryLowHint = " + this.mBatteryLowHint + "; mChargingHint = " + this.mChargingHint);
        }
        if (this.mScreenOn) {
            turnOffBatteryLights();
            continueUpdate = false;
        }
        if (getLightInColorState(4)) {
            continueUpdate = false;
        }
        if (mHealthInfo != null) {
            return continueUpdate;
        }
        if (DEBUG) {
            Slog.d(TAG_LED, "mBatteryProps is null!!!");
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateOppoLightsBatteryChargingLocked(HealthInfo mHealthInfo) {
        Light batteryLight = getBatteryLightInstance();
        if (batteryLight == null) {
            Slog.e(TAG_LED, "updateOppoLightsBatteryChargingLocked: batteryLight uninit.");
            return;
        }
        int level = mHealthInfo.batteryLevel;
        int status = mHealthInfo.batteryStatus;
        if (status == 2) {
            if (!this.mChargingHint) {
                batteryLight.turnOff();
            } else if (level < 100) {
                batteryLight.setFlashing(this.mSuperBatteryMediumARGB, 1, this.mSuperBatteryLedOn, this.mSuperBatteryLedOff);
            } else {
                batteryLight.setColor(this.mSuperBatteryFullARGB);
            }
        } else if (status != 5 || level < 100) {
            if (!this.mBatteryLowHint || level >= this.mLowBatteryWarningLevel) {
                batteryLight.turnOff();
            } else {
                batteryLight.setFlashing(this.mSuperBatteryLowARGB, 1, this.mSuperBatteryLedOn, this.mSuperBatteryLedOff);
            }
        } else if (this.mChargingHint) {
            batteryLight.setColor(this.mSuperBatteryFullARGB);
        } else {
            batteryLight.turnOff();
        }
    }

    private boolean getLightInColorState(int lightId) {
        OppoLightsManager oppoLightMgr = typeCastToSub(this.mLightsManager);
        if (oppoLightMgr != null) {
            return oppoLightMgr.getLightState(lightId);
        }
        Slog.d(TAG_LED, "getLightInColorState, oppoLightMgr empty!");
        return false;
    }

    private OppoLightsManager typeCastToSub(LightsManager lightsMgr) {
        return (OppoLightsManager) ColorTypeCastingHelper.typeCasting(OppoLightsManager.class, lightsMgr);
    }
}
