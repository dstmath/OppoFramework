package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.SystemProperties;
import android.view.KeyEvent;
import com.android.server.net.IColorNetworkPolicyManagerServiceEx;
import com.oppo.app.IOppoGameSpaceController;
import java.util.List;

public interface IColorGameSpaceManager extends IOppoCommonFeature {
    public static final IColorGameSpaceManager DEFAULT = new IColorGameSpaceManager() {
        /* class com.android.server.am.IColorGameSpaceManager.AnonymousClass1 */
    };
    public static final int MSG_SCREEN_OFF = 121;
    public static final String NAME = "IColorGameSpaceManager";
    public static final String TAG = "ColorGameSpaceManager";
    public static final boolean sDebugfDetail = SystemProperties.getBoolean("persist.sys.assert.panic", false);

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorGameSpaceManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean getDynamicDebug() {
        return false;
    }

    default void init(ActivityManagerService ams, IColorNetworkPolicyManagerServiceEx nmsEx) {
    }

    default boolean handleVideoComingNotification(Intent intent, ActivityInfo aInfo) {
        return false;
    }

    default void setGameSpaceController(IOppoGameSpaceController controller) {
    }

    default void handleApplicationSwitch(String prePkgName, String nextPkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp, boolean isPreForFreeForm, boolean isNextForFreeForm) {
    }

    default boolean inGameSpacePkgList(String pkg) {
        return false;
    }

    default boolean isGameSpaceMode() {
        return false;
    }

    default void sendGameSpaceEmptyMessage(int what, long delay) {
    }

    default boolean inNetWhiteAppIdList(int appId) {
        return false;
    }

    default List<Integer> getNetWhiteAppIdlist() {
        return null;
    }

    default boolean isBpmEnable() {
        return false;
    }

    default int getDefaultInputMethodAppId() {
        return 0;
    }

    default boolean isDefaultInputMethodAppId(int appId) {
        return false;
    }

    default List<Integer> getDozeRuleWhiteAppIdlist() {
        return null;
    }

    default boolean inDozeRuleAppIdList(int appId) {
        return false;
    }

    default void addPkgToDisplayDeviceList(String pkgName) {
    }

    default void removePkgFromDisplayDeviceList(String pkgName) {
    }

    default boolean requestGameDockIfNecessary() {
        return false;
    }

    default int getSystemUIFlagAfterGesture(int lastSystemUIFlag) {
        return lastSystemUIFlag;
    }

    default void setSystemGesturePointerPosition(int screenWidth, int statusBarHeight, float x, float y) {
    }

    default void sendDeviceUpdateMessage() {
    }

    default boolean isInterceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        return false;
    }
}
