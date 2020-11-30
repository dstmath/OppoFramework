package com.android.server.wm;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Surface;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import com.android.server.UiModeManagerService;
import com.android.server.UiThread;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.WindowOrientationListener;
import com.android.server.statusbar.StatusBarManagerInternal;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DisplayRotation {
    static final int FIXED_TO_USER_ROTATION_DEFAULT = 0;
    static final int FIXED_TO_USER_ROTATION_DISABLED = 1;
    static final int FIXED_TO_USER_ROTATION_ENABLED = 2;
    private static final String TAG = "WindowManager";
    public final boolean isDefaultDisplay;
    private int mAllowAllRotations;
    private final int mCarDockRotation;
    private final Context mContext;
    private int mCurrentAppOrientation;
    private boolean mDefaultFixedToUserRotation;
    private int mDemoHdmiRotation;
    private boolean mDemoHdmiRotationLock;
    private int mDemoRotation;
    private boolean mDemoRotationLock;
    private final int mDeskDockRotation;
    private final DisplayContent mDisplayContent;
    private final DisplayPolicy mDisplayPolicy;
    private final DisplayWindowSettings mDisplayWindowSettings;
    private int mFixedToUserRotation;
    @VisibleForTesting
    int mLandscapeRotation;
    private final int mLidOpenRotation;
    private final Object mLock;
    private OrientationListener mOrientationListener;
    @VisibleForTesting
    int mPortraitRotation;
    @VisibleForTesting
    int mSeascapeRotation;
    private final WindowManagerService mService;
    private SettingsObserver mSettingsObserver;
    private int mShowRotationSuggestions;
    private StatusBarManagerInternal mStatusBarManagerInternal;
    private final boolean mSupportAutoRotation;
    private final int mUndockedHdmiRotation;
    @VisibleForTesting
    int mUpsideDownRotation;
    private int mUserRotation;
    private int mUserRotationMode;

    @VisibleForTesting
    interface ContentObserverRegister {
        void registerContentObserver(Uri uri, boolean z, ContentObserver contentObserver, int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface FixedToUserRotation {
    }

    DisplayRotation(WindowManagerService service, DisplayContent displayContent) {
        this(service, displayContent, displayContent.getDisplayPolicy(), service.mDisplayWindowSettings, service.mContext, service.getWindowManagerLock());
    }

    @VisibleForTesting
    DisplayRotation(WindowManagerService service, DisplayContent displayContent, DisplayPolicy displayPolicy, DisplayWindowSettings displayWindowSettings, Context context, Object lock) {
        this.mCurrentAppOrientation = -1;
        this.mAllowAllRotations = -1;
        this.mUserRotationMode = 0;
        this.mUserRotation = 0;
        this.mFixedToUserRotation = 0;
        this.mService = service;
        this.mDisplayContent = displayContent;
        this.mDisplayPolicy = displayPolicy;
        this.mDisplayWindowSettings = displayWindowSettings;
        this.mContext = context;
        this.mLock = lock;
        this.isDefaultDisplay = displayContent.isDefaultDisplay;
        this.mSupportAutoRotation = this.mContext.getResources().getBoolean(17891530);
        this.mLidOpenRotation = readRotation(17694821);
        this.mCarDockRotation = readRotation(17694761);
        this.mDeskDockRotation = readRotation(17694785);
        this.mUndockedHdmiRotation = readRotation(17694908);
        if (this.isDefaultDisplay) {
            Handler uiHandler = UiThread.getHandler();
            this.mOrientationListener = new OrientationListener(this.mContext, uiHandler);
            this.mOrientationListener.setCurrentRotation(displayContent.getRotation());
            this.mSettingsObserver = new SettingsObserver(uiHandler);
            this.mSettingsObserver.observe();
        }
    }

    private int readRotation(int resID) {
        try {
            int rotation = this.mContext.getResources().getInteger(resID);
            if (rotation == 0) {
                return 0;
            }
            if (rotation == 90) {
                return 1;
            }
            if (rotation == 180) {
                return 2;
            }
            if (rotation != 270) {
                return -1;
            }
            return 3;
        } catch (Resources.NotFoundException e) {
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public void configure(int width, int height, int shortSizeDp, int longSizeDp) {
        Resources res = this.mContext.getResources();
        boolean z = true;
        if (width > height) {
            this.mLandscapeRotation = 0;
            this.mSeascapeRotation = 2;
            if (res.getBoolean(17891503)) {
                this.mPortraitRotation = 1;
                this.mUpsideDownRotation = 3;
            } else {
                this.mPortraitRotation = 3;
                this.mUpsideDownRotation = 1;
            }
        } else {
            this.mPortraitRotation = 0;
            this.mUpsideDownRotation = 2;
            if (res.getBoolean(17891503)) {
                this.mLandscapeRotation = 3;
                this.mSeascapeRotation = 1;
            } else {
                this.mLandscapeRotation = 1;
                this.mSeascapeRotation = 3;
            }
        }
        if ("portrait".equals(SystemProperties.get("persist.demo.hdmirotation"))) {
            this.mDemoHdmiRotation = this.mPortraitRotation;
        } else {
            this.mDemoHdmiRotation = this.mLandscapeRotation;
        }
        this.mDemoHdmiRotationLock = SystemProperties.getBoolean("persist.demo.hdmirotationlock", false);
        if ("portrait".equals(SystemProperties.get("persist.demo.remoterotation"))) {
            this.mDemoRotation = this.mPortraitRotation;
        } else {
            this.mDemoRotation = this.mLandscapeRotation;
        }
        this.mDemoRotationLock = SystemProperties.getBoolean("persist.demo.rotationlock", false);
        boolean isCar = this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
        boolean isTv = this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        boolean forceDesktopMode = this.mService.mForceDesktopModeOnExternalDisplays && !this.isDefaultDisplay;
        if ((!isCar && !isTv && !this.mService.mIsPc && !forceDesktopMode) || TemperatureProvider.SWITCH_ON.equals(SystemProperties.get("config.override_forced_orient"))) {
            z = false;
        }
        this.mDefaultFixedToUserRotation = z;
    }

    /* access modifiers changed from: package-private */
    public void setRotation(int rotation) {
        OrientationListener orientationListener = this.mOrientationListener;
        if (orientationListener != null) {
            orientationListener.setCurrentRotation(rotation);
        }
    }

    /* access modifiers changed from: package-private */
    public void setCurrentOrientation(int newOrientation) {
        if (newOrientation != this.mCurrentAppOrientation) {
            this.mCurrentAppOrientation = newOrientation;
            if (this.isDefaultDisplay) {
                updateOrientationListenerLw();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void restoreSettings(int userRotationMode, int userRotation, int fixedToUserRotation) {
        this.mFixedToUserRotation = fixedToUserRotation;
        if (!this.isDefaultDisplay) {
            if (!(userRotationMode == 0 || userRotationMode == 1)) {
                Slog.w("WindowManager", "Trying to restore an invalid user rotation mode " + userRotationMode + " for " + this.mDisplayContent);
                userRotationMode = 0;
            }
            if (userRotation < 0 || userRotation > 3) {
                Slog.w("WindowManager", "Trying to restore an invalid user rotation " + userRotation + " for " + this.mDisplayContent);
                userRotation = 0;
            }
            this.mUserRotationMode = userRotationMode;
            this.mUserRotation = userRotation;
        }
    }

    /* access modifiers changed from: package-private */
    public void setFixedToUserRotation(int fixedToUserRotation) {
        if (this.mFixedToUserRotation != fixedToUserRotation) {
            this.mFixedToUserRotation = fixedToUserRotation;
            this.mDisplayWindowSettings.setFixedToUserRotation(this.mDisplayContent, fixedToUserRotation);
            this.mService.updateRotation(true, false);
        }
    }

    private void setUserRotation(int userRotationMode, int userRotation) {
        int accelerometerRotation = 0;
        if (this.isDefaultDisplay) {
            ContentResolver res = this.mContext.getContentResolver();
            if (userRotationMode != 1) {
                accelerometerRotation = 1;
            }
            Settings.System.putIntForUser(res, "accelerometer_rotation", accelerometerRotation, -2);
            Settings.System.putIntForUser(res, "user_rotation", userRotation, -2);
            return;
        }
        boolean changed = false;
        if (this.mUserRotationMode != userRotationMode) {
            this.mUserRotationMode = userRotationMode;
            changed = true;
        }
        if (this.mUserRotation != userRotation) {
            this.mUserRotation = userRotation;
            changed = true;
        }
        this.mDisplayWindowSettings.setUserRotation(this.mDisplayContent, userRotationMode, userRotation);
        if (changed) {
            this.mService.updateRotation(true, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void freezeRotation(int rotation) {
        setUserRotation(1, rotation == -1 ? this.mDisplayContent.getRotation() : rotation);
    }

    /* access modifiers changed from: package-private */
    public void thawRotation() {
        setUserRotation(0, this.mUserRotation);
    }

    /* access modifiers changed from: package-private */
    public boolean isRotationFrozen() {
        return !this.isDefaultDisplay ? this.mUserRotationMode == 1 : Settings.System.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 0, -2) == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isFixedToUserRotation() {
        int i = this.mFixedToUserRotation;
        if (i == 1) {
            return false;
        }
        if (i != 2) {
            return this.mDefaultFixedToUserRotation;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean respectAppRequestedOrientation() {
        return !isFixedToUserRotation();
    }

    public int getLandscapeRotation() {
        return this.mLandscapeRotation;
    }

    public int getSeascapeRotation() {
        return this.mSeascapeRotation;
    }

    public int getPortraitRotation() {
        return this.mPortraitRotation;
    }

    public int getUpsideDownRotation() {
        return this.mUpsideDownRotation;
    }

    public int getCurrentAppOrientation() {
        return this.mCurrentAppOrientation;
    }

    public DisplayPolicy getDisplayPolicy() {
        return this.mDisplayPolicy;
    }

    public WindowOrientationListener getOrientationListener() {
        return this.mOrientationListener;
    }

    public int getUserRotation() {
        return this.mUserRotation;
    }

    public int getUserRotationMode() {
        return this.mUserRotationMode;
    }

    public void updateOrientationListener() {
        synchronized (this.mLock) {
            updateOrientationListenerLw();
        }
    }

    private void updateOrientationListenerLw() {
        OrientationListener orientationListener = this.mOrientationListener;
        if (orientationListener != null && orientationListener.canDetectOrientation()) {
            boolean screenOnEarly = this.mDisplayPolicy.isScreenOnEarly();
            boolean awake = this.mDisplayPolicy.isAwake();
            boolean keyguardDrawComplete = this.mDisplayPolicy.isKeyguardDrawComplete();
            boolean windowManagerDrawComplete = this.mDisplayPolicy.isWindowManagerDrawComplete();
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "screenOnEarly=" + screenOnEarly + ", awake=" + awake + ", currentAppOrientation=" + this.mCurrentAppOrientation + ", orientationSensorEnabled=" + this.mOrientationListener.mEnabled + ", keyguardDrawComplete=" + keyguardDrawComplete + ", windowManagerDrawComplete=" + windowManagerDrawComplete);
            }
            boolean disable = true;
            if (screenOnEarly && awake && keyguardDrawComplete && windowManagerDrawComplete && needSensorRunning()) {
                disable = false;
                if (!this.mOrientationListener.mEnabled) {
                    this.mOrientationListener.enable(true);
                }
            }
            if (disable && this.mOrientationListener.mEnabled) {
                this.mOrientationListener.disable();
            }
        }
    }

    private boolean needSensorRunning() {
        int i;
        if (isFixedToUserRotation()) {
            return false;
        }
        if (this.mSupportAutoRotation && ((i = this.mCurrentAppOrientation) == 4 || i == 10 || i == 7 || i == 6)) {
            return true;
        }
        int dockMode = this.mDisplayPolicy.getDockMode();
        if ((this.mDisplayPolicy.isCarDockEnablesAccelerometer() && dockMode == 2) || (this.mDisplayPolicy.isDeskDockEnablesAccelerometer() && (dockMode == 1 || dockMode == 3 || dockMode == 4))) {
            return true;
        }
        if (this.mUserRotationMode != 1) {
            return this.mSupportAutoRotation;
        }
        if (!this.mSupportAutoRotation || this.mShowRotationSuggestions != 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int rotationForOrientation(int orientation, int lastRotation) {
        int sensorRotation;
        int preferredRotation;
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            StringBuilder sb = new StringBuilder();
            sb.append("rotationForOrientation(orient=");
            sb.append(orientation);
            sb.append(", last=");
            sb.append(lastRotation);
            sb.append("); user=");
            sb.append(this.mUserRotation);
            sb.append(StringUtils.SPACE);
            sb.append(this.mUserRotationMode == 1 ? "USER_ROTATION_LOCKED" : "");
            Slog.v("WindowManager", sb.toString());
        }
        if (isFixedToUserRotation()) {
            return this.mUserRotation;
        }
        OrientationListener orientationListener = this.mOrientationListener;
        if (orientationListener != null) {
            sensorRotation = orientationListener.getProposedRotation();
        } else {
            sensorRotation = -1;
        }
        if (sensorRotation < 0) {
            sensorRotation = lastRotation;
        }
        int lidState = this.mDisplayPolicy.getLidState();
        int dockMode = this.mDisplayPolicy.getDockMode();
        boolean hdmiPlugged = this.mDisplayPolicy.isHdmiPlugged();
        boolean carDockEnablesAccelerometer = this.mDisplayPolicy.isCarDockEnablesAccelerometer();
        boolean deskDockEnablesAccelerometer = this.mDisplayPolicy.isDeskDockEnablesAccelerometer();
        if (!this.isDefaultDisplay) {
            preferredRotation = this.mUserRotation;
        } else if (lidState == 1 && this.mLidOpenRotation >= 0) {
            preferredRotation = this.mLidOpenRotation;
        } else if (dockMode == 2 && (carDockEnablesAccelerometer || this.mCarDockRotation >= 0)) {
            preferredRotation = carDockEnablesAccelerometer ? sensorRotation : this.mCarDockRotation;
        } else if ((dockMode == 1 || dockMode == 3 || dockMode == 4) && (deskDockEnablesAccelerometer || this.mDeskDockRotation >= 0)) {
            preferredRotation = deskDockEnablesAccelerometer ? sensorRotation : this.mDeskDockRotation;
        } else if (hdmiPlugged && this.mDemoHdmiRotationLock) {
            preferredRotation = this.mDemoHdmiRotation;
        } else if (hdmiPlugged && dockMode == 0 && this.mUndockedHdmiRotation >= 0) {
            preferredRotation = this.mUndockedHdmiRotation;
        } else if (this.mDemoRotationLock) {
            preferredRotation = this.mDemoRotation;
        } else if (this.mDisplayPolicy.isPersistentVrModeEnabled()) {
            preferredRotation = this.mPortraitRotation;
        } else if (orientation == 14) {
            preferredRotation = lastRotation;
        } else if (!this.mSupportAutoRotation) {
            preferredRotation = -1;
        } else if ((this.mUserRotationMode == 0 && (orientation == 2 || orientation == -1 || orientation == 11 || orientation == 12 || orientation == 13)) || orientation == 4 || orientation == 10 || orientation == 6 || orientation == 7) {
            if (this.mAllowAllRotations < 0) {
                this.mAllowAllRotations = this.mContext.getResources().getBoolean(17891340) ? 1 : 0;
            }
            preferredRotation = (sensorRotation != 2 || this.mAllowAllRotations == 1 || orientation == 10 || orientation == 13) ? sensorRotation : lastRotation;
        } else {
            preferredRotation = (this.mUserRotationMode != 1 || orientation == 5) ? -1 : this.mUserRotation;
        }
        if (WindowManagerDebugConfig.DEBUG_WMS || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v("WindowManager", "orientation " + orientation + " preferredRotation " + preferredRotation + " sensorRotation " + sensorRotation);
        }
        if (orientation != 0) {
            if (orientation != 1) {
                if (orientation != 11) {
                    if (orientation != 12) {
                        switch (orientation) {
                            case 6:
                                break;
                            case 7:
                                break;
                            case 8:
                                if (isLandscapeOrSeascape(preferredRotation)) {
                                    return preferredRotation;
                                }
                                return this.mSeascapeRotation;
                            case 9:
                                if (isAnyPortrait(preferredRotation)) {
                                    return preferredRotation;
                                }
                                return this.mUpsideDownRotation;
                            default:
                                if (preferredRotation >= 0) {
                                    return preferredRotation;
                                }
                                return 0;
                        }
                    }
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    }
                    if (isAnyPortrait(lastRotation)) {
                        return lastRotation;
                    }
                    return this.mPortraitRotation;
                }
                if (isLandscapeOrSeascape(preferredRotation)) {
                    return preferredRotation;
                }
                if (isLandscapeOrSeascape(lastRotation)) {
                    return lastRotation;
                }
                return this.mLandscapeRotation;
            } else if (isAnyPortrait(preferredRotation)) {
                return preferredRotation;
            } else {
                return this.mPortraitRotation;
            }
        } else if (isLandscapeOrSeascape(preferredRotation)) {
            return preferredRotation;
        } else {
            return this.mLandscapeRotation;
        }
    }

    private boolean isLandscapeOrSeascape(int rotation) {
        return rotation == this.mLandscapeRotation || rotation == this.mSeascapeRotation;
    }

    private boolean isAnyPortrait(int rotation) {
        return rotation == this.mPortraitRotation || rotation == this.mUpsideDownRotation;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidRotationChoice(int preferredRotation) {
        int i = this.mCurrentAppOrientation;
        if (i == -1 || i == 2) {
            return preferredRotation >= 0 && preferredRotation != this.mUpsideDownRotation;
        }
        switch (i) {
            case 11:
                return isLandscapeOrSeascape(preferredRotation);
            case 12:
                return preferredRotation == this.mPortraitRotation;
            case 13:
                return preferredRotation >= 0;
            default:
                return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isRotationChoicePossible(int orientation) {
        int dockMode;
        if (this.mUserRotationMode != 1 || isFixedToUserRotation()) {
            return false;
        }
        if ((this.mDisplayPolicy.getLidState() == 1 && this.mLidOpenRotation >= 0) || (dockMode = this.mDisplayPolicy.getDockMode()) == 2) {
            return false;
        }
        boolean deskDockEnablesAccelerometer = this.mDisplayPolicy.isDeskDockEnablesAccelerometer();
        if ((dockMode == 1 || dockMode == 3 || dockMode == 4) && !deskDockEnablesAccelerometer) {
            return false;
        }
        boolean hdmiPlugged = this.mDisplayPolicy.isHdmiPlugged();
        if (hdmiPlugged && this.mDemoHdmiRotationLock) {
            return false;
        }
        if ((hdmiPlugged && dockMode == 0 && this.mUndockedHdmiRotation >= 0) || this.mDemoRotationLock || this.mDisplayPolicy.isPersistentVrModeEnabled() || !this.mSupportAutoRotation) {
            return false;
        }
        if (!(orientation == -1 || orientation == 2)) {
            switch (orientation) {
                case 11:
                case 12:
                case 13:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendProposedRotationChangeToStatusBarInternal(int rotation, boolean isValid) {
        if (this.mStatusBarManagerInternal == null) {
            this.mStatusBarManagerInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
        }
        StatusBarManagerInternal statusBarManagerInternal = this.mStatusBarManagerInternal;
        if (statusBarManagerInternal != null) {
            statusBarManagerInternal.onProposedRotationChanged(rotation, isValid);
        }
    }

    private static String allowAllRotationsToString(int allowAll) {
        if (allowAll == -1) {
            return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        }
        if (allowAll == 0) {
            return TemperatureProvider.SWITCH_OFF;
        }
        if (allowAll != 1) {
            return Integer.toString(allowAll);
        }
        return TemperatureProvider.SWITCH_ON;
    }

    public void onUserSwitch() {
        SettingsObserver settingsObserver = this.mSettingsObserver;
        if (settingsObserver != null) {
            settingsObserver.onChange(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateSettings() {
        int showRotationSuggestions;
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean shouldUpdateRotation = false;
        synchronized (this.mLock) {
            boolean shouldUpdateOrientationListener = false;
            int userRotationMode = 1;
            if (ActivityManager.isLowRamDeviceStatic()) {
                showRotationSuggestions = 0;
            } else {
                showRotationSuggestions = Settings.Secure.getIntForUser(resolver, "show_rotation_suggestions", 1, -2);
            }
            if (this.mShowRotationSuggestions != showRotationSuggestions) {
                this.mShowRotationSuggestions = showRotationSuggestions;
                shouldUpdateOrientationListener = true;
            }
            int userRotation = Settings.System.getIntForUser(resolver, "user_rotation", 0, -2);
            if (this.mUserRotation != userRotation) {
                this.mUserRotation = userRotation;
                shouldUpdateRotation = true;
            }
            if (Settings.System.getIntForUser(resolver, "accelerometer_rotation", 0, -2) != 0) {
                userRotationMode = 0;
            }
            if (this.mUserRotationMode != userRotationMode) {
                this.mUserRotationMode = userRotationMode;
                shouldUpdateOrientationListener = true;
                shouldUpdateRotation = true;
            }
            if (shouldUpdateOrientationListener) {
                updateOrientationListenerLw();
            }
        }
        return shouldUpdateRotation;
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "DisplayRotation");
        pw.println(prefix + "  mCurrentAppOrientation=" + ActivityInfo.screenOrientationToString(this.mCurrentAppOrientation));
        pw.print(prefix + "  mLandscapeRotation=" + Surface.rotationToString(this.mLandscapeRotation));
        StringBuilder sb = new StringBuilder();
        sb.append(" mSeascapeRotation=");
        sb.append(Surface.rotationToString(this.mSeascapeRotation));
        pw.println(sb.toString());
        pw.print(prefix + "  mPortraitRotation=" + Surface.rotationToString(this.mPortraitRotation));
        StringBuilder sb2 = new StringBuilder();
        sb2.append(" mUpsideDownRotation=");
        sb2.append(Surface.rotationToString(this.mUpsideDownRotation));
        pw.println(sb2.toString());
        pw.println(prefix + "  mSupportAutoRotation=" + this.mSupportAutoRotation);
        OrientationListener orientationListener = this.mOrientationListener;
        if (orientationListener != null) {
            orientationListener.dump(pw, prefix + "  ");
        }
        pw.println();
        pw.print(prefix + "  mCarDockRotation=" + Surface.rotationToString(this.mCarDockRotation));
        StringBuilder sb3 = new StringBuilder();
        sb3.append(" mDeskDockRotation=");
        sb3.append(Surface.rotationToString(this.mDeskDockRotation));
        pw.println(sb3.toString());
        pw.print(prefix + "  mUserRotationMode=" + WindowManagerPolicy.userRotationModeToString(this.mUserRotationMode));
        StringBuilder sb4 = new StringBuilder();
        sb4.append(" mUserRotation=");
        sb4.append(Surface.rotationToString(this.mUserRotation));
        pw.print(sb4.toString());
        pw.println(" mAllowAllRotations=" + allowAllRotationsToString(this.mAllowAllRotations));
        pw.print(prefix + "  mDemoHdmiRotation=" + Surface.rotationToString(this.mDemoHdmiRotation));
        StringBuilder sb5 = new StringBuilder();
        sb5.append(" mDemoHdmiRotationLock=");
        sb5.append(this.mDemoHdmiRotationLock);
        pw.print(sb5.toString());
        pw.println(" mUndockedHdmiRotation=" + Surface.rotationToString(this.mUndockedHdmiRotation));
        pw.println(prefix + "  mLidOpenRotation=" + Surface.rotationToString(this.mLidOpenRotation));
        pw.println(prefix + "  mFixedToUserRotation=" + isFixedToUserRotation());
    }

    /* access modifiers changed from: private */
    public class OrientationListener extends WindowOrientationListener {
        boolean mEnabled;
        final SparseArray<Runnable> mRunnableCache = new SparseArray<>(5);

        OrientationListener(Context context, Handler handler) {
            super(context, handler);
        }

        private class UpdateRunnable implements Runnable {
            final int mRotation;

            UpdateRunnable(int rotation) {
                this.mRotation = rotation;
            }

            public void run() {
                DisplayRotation.this.mService.mPowerManagerInternal.powerHint(2, 0);
                if (DisplayRotation.this.isRotationChoicePossible(DisplayRotation.this.mCurrentAppOrientation)) {
                    DisplayRotation.this.sendProposedRotationChangeToStatusBarInternal(this.mRotation, DisplayRotation.this.isValidRotationChoice(this.mRotation));
                    return;
                }
                DisplayRotation.this.mService.updateRotation(false, false);
            }
        }

        @Override // com.android.server.policy.WindowOrientationListener
        public void onProposedRotationChanged(int rotation) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "onProposedRotationChanged, rotation=" + rotation);
            }
            Runnable r = this.mRunnableCache.get(rotation, null);
            if (r == null) {
                r = new UpdateRunnable(rotation);
                this.mRunnableCache.put(rotation, r);
            }
            getHandler().post(r);
        }

        @Override // com.android.server.policy.WindowOrientationListener
        public void enable(boolean clearCurrentRotation) {
            super.enable(clearCurrentRotation);
            this.mEnabled = true;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Enabling listeners");
            }
        }

        @Override // com.android.server.policy.WindowOrientationListener
        public void disable() {
            super.disable();
            this.mEnabled = false;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Disabling listeners");
            }
        }
    }

    /* access modifiers changed from: private */
    public class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver resolver = DisplayRotation.this.mContext.getContentResolver();
            resolver.registerContentObserver(Settings.Secure.getUriFor("show_rotation_suggestions"), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor("accelerometer_rotation"), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor("user_rotation"), false, this, -1);
            DisplayRotation.this.updateSettings();
        }

        public void onChange(boolean selfChange) {
            if (DisplayRotation.this.updateSettings()) {
                DisplayRotation.this.mService.updateRotation(true, false);
            }
        }
    }
}
