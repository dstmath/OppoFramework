package com.android.server.wm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.Locale;

public class ColorInputMethodKeyboardPositionManager {
    private static final String HIDE_NAVIGATIONBAR_ENABLE = "hide_navigationbar_enable";
    private static final int KEYBOARD_LIFT = 1;
    public static final String KEYBOARD_POSITION = "keyboard_position";
    private static final String LANGUAGE_CHANGE_ACTION = "android.intent.action.LOCALE_CHANGED";
    public static final int MODE_NAVIGATIONBAR_GESTURE = 2;
    public static final int MODE_NAVIGATIONBAR_GESTURE_SIDE = 3;
    private static final String REGION_CHANGE_ACTION = "android.settings.OPPO_REGION_CHANGED";
    private static final String REGION_DEFAULT = "CN";
    private static final String REGION_JAPAN = "JP";
    private static ColorInputMethodKeyboardPositionManager mInstance;
    /* access modifiers changed from: private */
    public Context mContext = null;
    private int mInputMethodPaddingBottom = 0;
    /* access modifiers changed from: private */
    public boolean mIsJapan;
    private boolean mIsLargeRadius;
    /* access modifiers changed from: private */
    public int mKeyboardPosition;
    private int mNavigationBarEnableStatus = 0;
    public BroadcastReceiver mRegionOrLanguageChangeReceiver = new BroadcastReceiver() {
        /* class com.android.server.wm.ColorInputMethodKeyboardPositionManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            ColorInputMethodKeyboardPositionManager colorInputMethodKeyboardPositionManager = ColorInputMethodKeyboardPositionManager.this;
            boolean unused = colorInputMethodKeyboardPositionManager.mIsJapan = colorInputMethodKeyboardPositionManager.isJapan();
            ColorInputMethodKeyboardPositionManager colorInputMethodKeyboardPositionManager2 = ColorInputMethodKeyboardPositionManager.this;
            int unused2 = colorInputMethodKeyboardPositionManager2.mKeyboardPosition = Settings.Secure.getIntForUser(colorInputMethodKeyboardPositionManager2.mContext.getContentResolver(), ColorInputMethodKeyboardPositionManager.KEYBOARD_POSITION, 1, -2);
            ColorInputMethodKeyboardPositionManager colorInputMethodKeyboardPositionManager3 = ColorInputMethodKeyboardPositionManager.this;
            colorInputMethodKeyboardPositionManager3.calculateMethodPaddingInfo(colorInputMethodKeyboardPositionManager3.mContext.getResources());
        }
    };
    WindowManagerService mWms;

    public ColorInputMethodKeyboardPositionManager(Context context) {
        this.mContext = context;
    }

    public static ColorInputMethodKeyboardPositionManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ColorInputMethodKeyboardPositionManager(context);
        }
        return mInstance;
    }

    public void init() {
        IntentFilter regionAndLanguageChangeFilter = new IntentFilter();
        regionAndLanguageChangeFilter.addAction(REGION_CHANGE_ACTION);
        regionAndLanguageChangeFilter.addAction(LANGUAGE_CHANGE_ACTION);
        this.mContext.registerReceiver(this.mRegionOrLanguageChangeReceiver, regionAndLanguageChangeFilter);
        this.mIsJapan = isJapan();
        calculateMethodPaddingInfo(this.mContext.getResources());
        this.mKeyboardPosition = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEYBOARD_POSITION, 1, -2);
    }

    public void updateInputMethodPaddingBottom(Rect cf, Rect vf, DisplayFrames displayFrames) {
        int rotation = displayFrames.mRotation;
        if (isNavGestureMode() && cf != null && vf != null) {
            if ((rotation == 0 || rotation == 2) && getWindowDisplayStatus() && needLiftKeyboard()) {
                cf.bottom -= this.mInputMethodPaddingBottom;
                vf.bottom -= this.mInputMethodPaddingBottom;
            }
        }
    }

    public void updateKeyboardPosition() {
        this.mKeyboardPosition = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEYBOARD_POSITION, 1, -2);
    }

    private boolean needLiftKeyboard() {
        this.mKeyboardPosition = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEYBOARD_POSITION, 1, -2);
        if (this.mKeyboardPosition != 1 || (!this.mIsLargeRadius && !this.mIsJapan)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean isJapan() {
        Locale currentLocal = this.mContext.getResources().getConfiguration().getLocales().get(0);
        boolean isConfigJapan = Locale.JAPAN.equals(currentLocal) || Locale.JAPANESE.equals(currentLocal);
        boolean isRegionJapan = REGION_JAPAN.equals(SystemProperties.get("persist.sys.oppo.region", REGION_DEFAULT));
        if (isConfigJapan || isRegionJapan) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void calculateMethodPaddingInfo(Resources resources) {
        int paddintBottom20 = resources.getDimensionPixelSize(201655617);
        int paddintBottom25 = resources.getDimensionPixelSize(201655618);
        int paddintBottom30 = resources.getDimensionPixelSize(201655619);
        int padding20Threshold = resources.getDimensionPixelSize(201655620);
        int padding25Threshold = resources.getDimensionPixelSize(201655621);
        int padding30Threshold = resources.getDimensionPixelSize(201655622);
        String radiusProperty = SystemProperties.get("ro.display.rc.size");
        int i = 0;
        if (!TextUtils.isEmpty(radiusProperty)) {
            String[] radius = radiusProperty.split("\\,");
            int length = radius.length;
            boolean z = true;
            if (length == 1) {
                this.mIsLargeRadius = true;
                this.mInputMethodPaddingBottom = paddintBottom20;
                return;
            }
            try {
                int bottomRightRadius = Integer.parseInt(radius[length - 1]);
                if (bottomRightRadius <= padding20Threshold) {
                    z = false;
                }
                this.mIsLargeRadius = z;
                if (bottomRightRadius <= padding20Threshold) {
                    this.mInputMethodPaddingBottom = this.mIsJapan ? paddintBottom20 : 0;
                } else if (bottomRightRadius <= padding25Threshold) {
                    this.mInputMethodPaddingBottom = paddintBottom20;
                } else if (bottomRightRadius <= padding30Threshold) {
                    this.mInputMethodPaddingBottom = paddintBottom25;
                } else {
                    this.mInputMethodPaddingBottom = paddintBottom30;
                }
            } catch (Exception e) {
                this.mIsLargeRadius = false;
                this.mInputMethodPaddingBottom = 0;
            }
        } else {
            if (this.mIsJapan) {
                i = paddintBottom20;
            }
            this.mInputMethodPaddingBottom = i;
        }
    }

    private boolean isNavGestureMode() {
        this.mNavigationBarEnableStatus = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), HIDE_NAVIGATIONBAR_ENABLE, 0, -2);
        int i = this.mNavigationBarEnableStatus;
        if (i == 2 || i == 3) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void addWindowManagerService(WindowManagerService service) {
        this.mWms = service;
    }

    private boolean getWindowDisplayStatus() {
        WindowManagerService windowManagerService = this.mWms;
        return windowManagerService != null && !windowManagerService.getDefaultDisplayContentLocked().isStackVisible(5) && !this.mWms.getDefaultDisplayContentLocked().isStackVisible(3);
    }
}
