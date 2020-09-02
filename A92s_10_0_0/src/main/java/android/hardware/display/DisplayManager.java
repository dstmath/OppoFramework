package android.hardware.display;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.graphics.Point;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Display;
import android.view.Surface;
import java.util.ArrayList;
import java.util.List;

public final class DisplayManager {
    @UnsupportedAppUsage
    public static final String ACTION_WIFI_DISPLAY_STATUS_CHANGED = "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED";
    private static final boolean DEBUG = false;
    public static final String DISPLAY_CATEGORY_PRESENTATION = "android.hardware.display.category.PRESENTATION";
    @UnsupportedAppUsage
    public static final String EXTRA_WIFI_DISPLAY_STATUS = "android.hardware.display.extra.WIFI_DISPLAY_STATUS";
    private static final String TAG = "DisplayManager";
    public static final int VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR = 16;
    public static final int VIRTUAL_DISPLAY_FLAG_CAN_SHOW_WITH_INSECURE_KEYGUARD = 32;
    public static final int VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL = 256;
    public static final int VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY = 8;
    public static final int VIRTUAL_DISPLAY_FLAG_PRESENTATION = 2;
    public static final int VIRTUAL_DISPLAY_FLAG_PUBLIC = 1;
    public static final int VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT = 128;
    public static final int VIRTUAL_DISPLAY_FLAG_SECURE = 4;
    public static final int VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS = 512;
    public static final int VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH = 64;
    private final Context mContext;
    private final SparseArray<Display> mDisplays = new SparseArray<>();
    private final DisplayManagerGlobal mGlobal;
    private final Object mLock = new Object();
    private final ArrayList<Display> mTempDisplays = new ArrayList<>();

    public interface DisplayListener {
        void onDisplayAdded(int i);

        void onDisplayChanged(int i);

        void onDisplayRemoved(int i);
    }

    public DisplayManager(Context context) {
        this.mContext = context;
        this.mGlobal = DisplayManagerGlobal.getInstance();
    }

    public Display getDisplay(int displayId) {
        Display orCreateDisplayLocked;
        synchronized (this.mLock) {
            orCreateDisplayLocked = getOrCreateDisplayLocked(displayId, false);
        }
        return orCreateDisplayLocked;
    }

    public Display[] getDisplays() {
        return getDisplays(null);
    }

    public Display[] getDisplays(String category) {
        Display[] displayArr;
        int[] displayIds = this.mGlobal.getDisplayIds();
        synchronized (this.mLock) {
            if (category == null) {
                try {
                    addAllDisplaysLocked(this.mTempDisplays, displayIds);
                } catch (Throwable th) {
                    this.mTempDisplays.clear();
                    throw th;
                }
            } else if (category.equals(DISPLAY_CATEGORY_PRESENTATION)) {
                addPresentationDisplaysLocked(this.mTempDisplays, displayIds, 3);
                addPresentationDisplaysLocked(this.mTempDisplays, displayIds, 2);
                addPresentationDisplaysLocked(this.mTempDisplays, displayIds, 4);
                addPresentationDisplaysLocked(this.mTempDisplays, displayIds, 5);
            }
            displayArr = (Display[]) this.mTempDisplays.toArray(new Display[this.mTempDisplays.size()]);
            this.mTempDisplays.clear();
        }
        return displayArr;
    }

    private void addAllDisplaysLocked(ArrayList<Display> displays, int[] displayIds) {
        for (int i : displayIds) {
            Display display = getOrCreateDisplayLocked(i, true);
            if (display != null) {
                displays.add(display);
            }
        }
    }

    private void addPresentationDisplaysLocked(ArrayList<Display> displays, int[] displayIds, int matchType) {
        for (int i : displayIds) {
            Display display = getOrCreateDisplayLocked(i, true);
            if (!(display == null || (display.getFlags() & 8) == 0 || display.getType() != matchType)) {
                displays.add(display);
            }
        }
    }

    private Display getOrCreateDisplayLocked(int displayId, boolean assumeValid) {
        Display display = this.mDisplays.get(displayId);
        if (display == null) {
            Display display2 = this.mGlobal.getCompatibleDisplay(displayId, (this.mContext.getDisplayId() == displayId ? this.mContext : this.mContext.getApplicationContext()).getResources());
            if (display2 == null) {
                return display2;
            }
            this.mDisplays.put(displayId, display2);
            return display2;
        } else if (assumeValid || display.isValid()) {
            return display;
        } else {
            return null;
        }
    }

    public void registerDisplayListener(DisplayListener listener, Handler handler) {
        this.mGlobal.registerDisplayListener(listener, handler);
    }

    public void unregisterDisplayListener(DisplayListener listener) {
        this.mGlobal.unregisterDisplayListener(listener);
    }

    @UnsupportedAppUsage
    public void startWifiDisplayScan() {
        this.mGlobal.startWifiDisplayScan();
    }

    @UnsupportedAppUsage
    public void stopWifiDisplayScan() {
        this.mGlobal.stopWifiDisplayScan();
    }

    @UnsupportedAppUsage
    public void connectWifiDisplay(String deviceAddress) {
        this.mGlobal.connectWifiDisplay(deviceAddress);
    }

    @UnsupportedAppUsage
    public void pauseWifiDisplay() {
        this.mGlobal.pauseWifiDisplay();
    }

    @UnsupportedAppUsage
    public void resumeWifiDisplay() {
        this.mGlobal.resumeWifiDisplay();
    }

    @UnsupportedAppUsage
    public void disconnectWifiDisplay() {
        this.mGlobal.disconnectWifiDisplay();
    }

    @UnsupportedAppUsage
    public void renameWifiDisplay(String deviceAddress, String alias) {
        this.mGlobal.renameWifiDisplay(deviceAddress, alias);
    }

    @UnsupportedAppUsage
    public void forgetWifiDisplay(String deviceAddress) {
        this.mGlobal.forgetWifiDisplay(deviceAddress);
    }

    @UnsupportedAppUsage
    public WifiDisplayStatus getWifiDisplayStatus() {
        return this.mGlobal.getWifiDisplayStatus();
    }

    @SystemApi
    public void setSaturationLevel(float level) {
        if (level < 0.0f || level > 1.0f) {
            throw new IllegalArgumentException("Saturation level must be between 0 and 1");
        }
        ((ColorDisplayManager) this.mContext.getSystemService(ColorDisplayManager.class)).setSaturationLevel(Math.round(100.0f * level));
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int densityDpi, Surface surface, int flags) {
        return createVirtualDisplay(name, width, height, densityDpi, surface, flags, null, null);
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int densityDpi, Surface surface, int flags, VirtualDisplay.Callback callback, Handler handler) {
        return createVirtualDisplay(null, name, width, height, densityDpi, surface, flags, callback, handler, null);
    }

    public VirtualDisplay createVirtualDisplay(MediaProjection projection, String name, int width, int height, int densityDpi, Surface surface, int flags, VirtualDisplay.Callback callback, Handler handler, String uniqueId) {
        return this.mGlobal.createVirtualDisplay(this.mContext, projection, name, width, height, densityDpi, surface, flags, callback, handler, uniqueId);
    }

    @SystemApi
    public Point getStableDisplaySize() {
        return this.mGlobal.getStableDisplaySize();
    }

    @SystemApi
    public List<BrightnessChangeEvent> getBrightnessEvents() {
        return this.mGlobal.getBrightnessEvents(this.mContext.getOpPackageName());
    }

    @SystemApi
    public List<AmbientBrightnessDayStats> getAmbientBrightnessStats() {
        return this.mGlobal.getAmbientBrightnessStats();
    }

    @SystemApi
    public void setBrightnessConfiguration(BrightnessConfiguration c) {
        setBrightnessConfigurationForUser(c, this.mContext.getUserId(), this.mContext.getPackageName());
    }

    public void setBrightnessConfigurationForUser(BrightnessConfiguration c, int userId, String packageName) {
        this.mGlobal.setBrightnessConfigurationForUser(c, userId, packageName);
    }

    @SystemApi
    public BrightnessConfiguration getBrightnessConfiguration() {
        return getBrightnessConfigurationForUser(this.mContext.getUserId());
    }

    public BrightnessConfiguration getBrightnessConfigurationForUser(int userId) {
        return this.mGlobal.getBrightnessConfigurationForUser(userId);
    }

    @SystemApi
    public BrightnessConfiguration getDefaultBrightnessConfiguration() {
        return this.mGlobal.getDefaultBrightnessConfiguration();
    }

    public void setRotateState(boolean isStart) {
        this.mGlobal.setRotateState(isStart);
    }

    public void setTemporaryBrightness(int brightness) {
        this.mGlobal.setTemporaryBrightness(brightness);
    }

    public void setTemporaryAutoBrightnessAdjustment(float adjustment) {
        this.mGlobal.setTemporaryAutoBrightnessAdjustment(adjustment);
    }

    @SystemApi
    public Pair<float[], float[]> getMinimumBrightnessCurve() {
        return this.mGlobal.getMinimumBrightnessCurve();
    }
}
