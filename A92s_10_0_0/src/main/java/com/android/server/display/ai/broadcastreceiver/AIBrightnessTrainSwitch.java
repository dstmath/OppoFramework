package com.android.server.display.ai.broadcastreceiver;

import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.display.ai.utils.DataHelper;

public class AIBrightnessTrainSwitch {
    private static final String TAG = "AIBrightnessTrainSwitch";
    private static volatile AIBrightnessTrainSwitch sAIBrightnessTrainSwitch;
    private boolean mIsRegistered = false;
    private boolean mRusEnable = true;
    /* access modifiers changed from: private */
    public boolean mSettingsEnable = true;
    private ContentObserver mTrainSwitchObserver;

    private AIBrightnessTrainSwitch() {
    }

    public static AIBrightnessTrainSwitch getInstance() {
        if (sAIBrightnessTrainSwitch == null) {
            synchronized (AIBrightnessTrainSwitch.class) {
                if (sAIBrightnessTrainSwitch == null) {
                    sAIBrightnessTrainSwitch = new AIBrightnessTrainSwitch();
                }
            }
        }
        return sAIBrightnessTrainSwitch;
    }

    public boolean isTrainEnable() {
        StringBuilder sb = new StringBuilder();
        sb.append("isTrainEnable: mRusEnable:");
        sb.append(this.mRusEnable);
        sb.append(" mSettingsEnable:");
        sb.append(this.mSettingsEnable);
        sb.append(" mRusEnable & mSettingsEnable:");
        sb.append(this.mRusEnable && this.mSettingsEnable);
        ColorAILog.d(TAG, sb.toString());
        return this.mRusEnable && this.mSettingsEnable;
    }

    public void setRusSwitch(boolean enable) {
        ColorAILog.d(TAG, "setRusSwitch:" + enable);
        this.mRusEnable = enable;
        removeLocalFile();
    }

    /* access modifiers changed from: private */
    public void removeLocalFile() {
        if (!isTrainEnable()) {
            DataHelper.getInstance().deleteAppSpline();
        }
    }

    private void startObservingSettings(Context context) {
        try {
            context.getContentResolver().registerContentObserver(Settings.System.getUriFor(BrightnessConstants.BrightnessTrainSwitch.SETTINGS_AIBRIGHTNESS_TRAIN_ENABLE), false, this.mTrainSwitchObserver);
        } catch (Exception e) {
            ColorAILog.e(TAG, "Failed to registerContentObserver:" + e);
        }
    }

    public void register(final Context context) {
        this.mSettingsEnable = Settings.System.getInt(context.getContentResolver(), BrightnessConstants.BrightnessTrainSwitch.SETTINGS_AIBRIGHTNESS_TRAIN_ENABLE, 1) == 1;
        ColorAILog.d(TAG, "init--mSettingsEnable:" + this.mSettingsEnable);
        if (!this.mIsRegistered) {
            this.mTrainSwitchObserver = new ContentObserver(null) {
                /* class com.android.server.display.ai.broadcastreceiver.AIBrightnessTrainSwitch.AnonymousClass1 */

                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    boolean z = true;
                    int enable = Settings.System.getInt(context.getContentResolver(), BrightnessConstants.BrightnessTrainSwitch.SETTINGS_AIBRIGHTNESS_TRAIN_ENABLE, 1);
                    AIBrightnessTrainSwitch aIBrightnessTrainSwitch = AIBrightnessTrainSwitch.this;
                    if (enable != 1) {
                        z = false;
                    }
                    boolean unused = aIBrightnessTrainSwitch.mSettingsEnable = z;
                    ColorAILog.d(AIBrightnessTrainSwitch.TAG, "Observe mSettingsEnable changed:" + enable + " mSettingsEnable:" + AIBrightnessTrainSwitch.this.mSettingsEnable);
                    AIBrightnessTrainSwitch.this.removeLocalFile();
                }
            };
            startObservingSettings(context);
            this.mIsRegistered = true;
        }
    }

    public void unregister(Context context) {
        try {
            context.getContentResolver().unregisterContentObserver(this.mTrainSwitchObserver);
            this.mIsRegistered = false;
        } catch (Exception e) {
            ColorAILog.e(TAG, "Failed to unregisterContentObserver:" + e);
        }
    }
}
