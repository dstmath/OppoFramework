package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Message;

public interface IColorFullScreenDisplayManager extends IOppoCommonFeature {
    public static final int ADD_DISPLAY_FULL_SCREEN_WINDOW = 1302;
    public static final int COLOR_FULL_SCREEN_DISPLAY_MESSAGE_INDEX = 1301;
    public static final int COLOR_FULL_SCREEN_DISPLAY_MESSAGE_MAX = 1308;
    public static final int CONFIG_DISPLAY_FULL_SCREEN_WINDOW = 1306;
    public static final int CREATE_DISPLAY_FULL_SCREEN_WINDOW = 1304;
    public static final IColorFullScreenDisplayManager DEFAULT = new IColorFullScreenDisplayManager() {
        /* class com.android.server.wm.IColorFullScreenDisplayManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorFullScreenDisplayManager";
    public static final int REMOVE_DISPLAY_FULL_SCREEN_WINDOW = 1303;
    public static final int RESET_DISPLAY_FULL_SCREEN_WINDOW = 1307;
    public static final int UPDATE_DISPLAY_FULL_SCREEN_WINDOW = 1305;

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorFullScreenDisplayManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorWindowManagerServiceEx wms) {
    }

    default DisplayPolicy createDisplayPolicy(WindowManagerService service, DisplayContent displayContent) {
        return new DisplayPolicy(service, displayContent);
    }

    default void sendMessageToWmService(int messageWhat, int messageWhat1) {
    }

    default void hanleFullScreenDisplayMessage(Message msg) {
    }

    default void initColorDisplayCompat(String packageName, WindowState windowState) {
    }

    default void injectorGetSurfaceTouchableRegion(WindowState windowState, Region region) {
    }

    default void injectorDisplayContentConsumer(DisplayContent displayContent, WindowState w, WindowState mCurrentFocus) {
    }

    default boolean performLayoutNoTrace(DisplayPolicy displayPolicy, DisplayFrames mDisplayFrames, int uiMode) {
        return false;
    }

    default int calculateCompatBoundsTransformation(int offsetX, Rect viewportBounds, float viewportW, float contentW, float mSizeCompatScale) {
        return offsetX;
    }

    default int calculateCompatBoundsOffsetX(int offsetX, Configuration config, ComponentName componentName, Rect viewportBounds, Rect contentBounds, float sizeCompatScale) {
        return offsetX;
    }

    default float getmaxAspectRatio(ActivityInfo info) {
        return info.maxAspectRatio;
    }

    default boolean injectLayoutWindowLwDisplayPolicy(int cutoutMode) {
        return true;
    }
}
