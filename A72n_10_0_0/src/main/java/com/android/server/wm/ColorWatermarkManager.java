package com.android.server.wm;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import com.color.util.ColorAccessibilityUtil;

public class ColorWatermarkManager implements IColorWatermarkManager {
    static final String ACCESIBILITY_SWITCH = "enabled_accessibility_services";
    private static final String TAG = "ColorWatermarkManager";
    static final String TALKBACK_WATERMARK_SWITCH = "accessibility_services_talkback_float_hint";
    private static ColorWatermarkManager mInstance = null;
    private final Uri mAccessbilitySwitch = Settings.Secure.getUriFor(ACCESIBILITY_SWITCH);
    ColorTalkbackWatermark mColorTalkbackWatermark;
    private Context mContext;
    private boolean mIsShowing = false;
    private SwitchObserver mObserver;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.wm.ColorWatermarkManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            ColorWatermarkManager.this.handleSwitchChange();
            ColorWatermarkManager colorWatermarkManager = ColorWatermarkManager.this;
            colorWatermarkManager.mObserver = new SwitchObserver();
        }
    };
    private final Uri mWatermarkSwitch = Settings.Secure.getUriFor(TALKBACK_WATERMARK_SWITCH);
    private WindowManagerService mWms;

    private ColorWatermarkManager() {
    }

    public static ColorWatermarkManager getInstance() {
        if (mInstance == null) {
            synchronized (ColorWatermarkManager.class) {
                if (mInstance == null) {
                    mInstance = new ColorWatermarkManager();
                }
            }
        }
        return mInstance;
    }

    public void init(IColorWindowManagerServiceEx wms) {
        this.mWms = wms.getWindowManagerService();
        this.mContext = this.mWms.mContext;
        this.mObserver = new SwitchObserver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_BACKGROUND");
        filter.addAction("android.intent.action.USER_FOREGROUND");
        filter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public boolean shouldShowTalkbackWatermark(Context context) {
        if (Settings.Secure.getIntForUser(context.getContentResolver(), TALKBACK_WATERMARK_SWITCH, 0, -2) == 0 || !ColorAccessibilityUtil.isTalkbackEnabled(context)) {
            return false;
        }
        return true;
    }

    public void createTalkbackWatermark() {
    }

    public void showWatermarkIfNeeded(boolean flag) {
        if (this.mIsShowing != flag) {
            this.mIsShowing = flag;
            this.mWms.openSurfaceTransaction();
            if (flag) {
                try {
                    this.mColorTalkbackWatermark = new ColorTalkbackWatermark(this.mContext, this.mWms.getDefaultDisplayContentLocked());
                    this.mColorTalkbackWatermark.showWatermark();
                } catch (Throwable th) {
                    this.mWms.closeSurfaceTransaction("createWatermarkInTransaction");
                    throw th;
                }
            } else if (this.mColorTalkbackWatermark != null) {
                this.mColorTalkbackWatermark.hideWatermark();
                this.mColorTalkbackWatermark = null;
            }
            this.mWms.closeSurfaceTransaction("createWatermarkInTransaction");
        }
    }

    public void draw() {
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (this.mIsShowing) {
            showWatermarkIfNeeded(false);
            showWatermarkIfNeeded(true);
        }
    }

    public void positionSurface(int defaultDw, int defaultDh) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSwitchChange() {
        Message message = Message.obtain();
        message.what = 1002;
        message.obj = Boolean.valueOf(shouldShowTalkbackWatermark(this.mContext));
        this.mWms.mH.sendMessage(message);
    }

    /* access modifiers changed from: private */
    public class SwitchObserver extends ContentObserver {
        SwitchObserver() {
            super(new Handler());
            ContentResolver resolver = ColorWatermarkManager.this.mContext.getContentResolver();
            resolver.registerContentObserver(ColorWatermarkManager.this.mAccessbilitySwitch, false, this, -2);
            resolver.registerContentObserver(ColorWatermarkManager.this.mWatermarkSwitch, false, this, -2);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (ColorWatermarkManager.this.mAccessbilitySwitch.equals(uri) || ColorWatermarkManager.this.mWatermarkSwitch.equals(uri)) {
                ColorWatermarkManager.this.handleSwitchChange();
            }
        }
    }
}
