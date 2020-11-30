package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.common.OppoFeatureManager;
import android.content.Context;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ColorAbnormalAppManager;
import com.android.server.am.ColorActivityManagerServiceEx;
import com.android.server.am.ColorAppCrashClearManager;
import com.android.server.am.ColorAppStartupManager;
import com.android.server.am.ColorAthenaAmManager;
import com.android.server.am.ColorBatteryProcessStats;
import com.android.server.am.ColorBroadcastManager;
import com.android.server.am.ColorBroadcastStaticRegisterWhitelistManager;
import com.android.server.am.ColorCommonListManager;
import com.android.server.am.ColorEapManager;
import com.android.server.am.ColorEdgeTouchManagerService;
import com.android.server.am.ColorFastAppManager;
import com.android.server.am.ColorGameSpaceManager;
import com.android.server.am.ColorHansManager;
import com.android.server.am.ColorHansRestriction;
import com.android.server.am.ColorKeyEventManagerService;
import com.android.server.am.ColorKeyLayoutManagerService;
import com.android.server.am.ColorMultiAppManagerService;
import com.android.server.am.ColorPerfManager;
import com.android.server.am.ColorResourcePreloadManager;
import com.android.server.am.ColorSecurityPermissionManager;
import com.android.server.am.IColorAbnormalAppManager;
import com.android.server.am.IColorActivityManagerServiceEx;
import com.android.server.am.IColorAppCrashClearManager;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.IColorAthenaAmManager;
import com.android.server.am.IColorBatteryProcessStats;
import com.android.server.am.IColorBroadcastManager;
import com.android.server.am.IColorBroadcastStaticRegisterWhitelistManager;
import com.android.server.am.IColorCommonListManager;
import com.android.server.am.IColorEapManager;
import com.android.server.am.IColorEdgeTouchManager;
import com.android.server.am.IColorFastAppManager;
import com.android.server.am.IColorGameSpaceManager;
import com.android.server.am.IColorHansManager;
import com.android.server.am.IColorKeyEventManager;
import com.android.server.am.IColorKeyLayoutManager;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.am.IColorPerfManager;
import com.android.server.am.IColorResourcePreloadManager;
import com.android.server.am.IColorSecurityPermissionManager;
import com.android.server.display.ColorAIBrightManager;
import com.android.server.display.ColorDisplayManagerServiceEx;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.IColorAIBrightManager;
import com.android.server.display.IColorDisplayManagerServiceEx;
import com.android.server.display.IColorEyeProtectManager;
import com.android.server.display.color.ColorEyeProtectManager;
import com.android.server.inputmethod.ColorInputMethodManagerService;
import com.android.server.inputmethod.ColorInputMethodManagerServiceEx;
import com.android.server.inputmethod.IColorInputMethodManagerServiceEx;
import com.android.server.inputmethod.InputMethodManagerService;
import com.android.server.job.ColorBatteryIdleManager;
import com.android.server.job.ColorJobScheduleManager;
import com.android.server.job.ColorJobSchedulerServiceEx;
import com.android.server.job.IBatteryIdleController;
import com.android.server.job.IColorJobScheduleManager;
import com.android.server.job.IColorJobSchedulerServiceEx;
import com.android.server.job.JobSchedulerService;
import com.android.server.locksettings.ColorLockSettingsService;
import com.android.server.locksettings.LockSettingsService;
import com.android.server.net.ColorDozeNetworkOptimization;
import com.android.server.net.ColorNetworkPolicyManagerServiceEx;
import com.android.server.net.IColorDozeNetworkOptimization;
import com.android.server.net.IColorNetworkPolicyManagerServiceEx;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.notification.ColorNotificationManagerServiceEx;
import com.android.server.notification.IColorNotificationManagerServiceEx;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.OppoNotificationManager;
import com.android.server.om.ColorLanguageManager;
import com.android.server.om.ColorOverlayManagerServiceEx;
import com.android.server.om.IColorLanguageManager;
import com.android.server.om.IColorOverlayManagerServiceEx;
import com.android.server.pm.ColorAppInstallProgressManager;
import com.android.server.pm.ColorAppListInterceptManager;
import com.android.server.pm.ColorAppQuickFreezeManager;
import com.android.server.pm.ColorChildrenModeInstallManager;
import com.android.server.pm.ColorClearDataProtectManager;
import com.android.server.pm.ColorDataFreeManager;
import com.android.server.pm.ColorDefaultAppPolicyManager;
import com.android.server.pm.ColorDexMetadataManager;
import com.android.server.pm.ColorDynamicFeatureManager;
import com.android.server.pm.ColorExpDefaultBrowserManager;
import com.android.server.pm.ColorForbidHideOrDisableManager;
import com.android.server.pm.ColorForbidUninstallAppManager;
import com.android.server.pm.ColorFullmodeManager;
import com.android.server.pm.ColorIconCachesManager;
import com.android.server.pm.ColorInstallThreadsControlManager;
import com.android.server.pm.ColorLanguageEnableManager;
import com.android.server.pm.ColorMergedProcessSplitManager;
import com.android.server.pm.ColorOtaDataManager;
import com.android.server.pm.ColorPackageInstallInterceptManager;
import com.android.server.pm.ColorPackageInstallStatisticManager;
import com.android.server.pm.ColorPackageManagerServiceEx;
import com.android.server.pm.ColorPkgStartInfoManager;
import com.android.server.pm.ColorPmsSupportedFunctionManager;
import com.android.server.pm.ColorRemovableAppManager;
import com.android.server.pm.ColorRuntimePermGrantPolicyManager;
import com.android.server.pm.ColorSecurePayManager;
import com.android.server.pm.ColorSellModeManager;
import com.android.server.pm.ColorSensitivePermGrantPolicyManager;
import com.android.server.pm.ColorSystemAppProtectManager;
import com.android.server.pm.ColorThirdPartyAppSignCheckManager;
import com.android.server.pm.IColorAppInstallProgressManager;
import com.android.server.pm.IColorAppListInterceptManager;
import com.android.server.pm.IColorAppQuickFreezeManager;
import com.android.server.pm.IColorChildrenModeInstallManager;
import com.android.server.pm.IColorClearDataProtectManager;
import com.android.server.pm.IColorDataFreeManager;
import com.android.server.pm.IColorDefaultAppPolicyManager;
import com.android.server.pm.IColorDexMetadataManager;
import com.android.server.pm.IColorDynamicFeatureManager;
import com.android.server.pm.IColorExpDefaultBrowserManager;
import com.android.server.pm.IColorForbidHideOrDisableManager;
import com.android.server.pm.IColorForbidUninstallAppManager;
import com.android.server.pm.IColorFullmodeManager;
import com.android.server.pm.IColorIconCachesManager;
import com.android.server.pm.IColorInstallThreadsControlManager;
import com.android.server.pm.IColorLanguageEnableManager;
import com.android.server.pm.IColorMergedProcessSplitManager;
import com.android.server.pm.IColorOtaDataManager;
import com.android.server.pm.IColorPackageInstallInterceptManager;
import com.android.server.pm.IColorPackageInstallStatisticManager;
import com.android.server.pm.IColorPackageManagerServiceEx;
import com.android.server.pm.IColorPkgStartInfoManager;
import com.android.server.pm.IColorPmsSupportedFunctionManager;
import com.android.server.pm.IColorRemovableAppManager;
import com.android.server.pm.IColorRuntimePermGrantPolicyManager;
import com.android.server.pm.IColorSecurePayManager;
import com.android.server.pm.IColorSellModeManager;
import com.android.server.pm.IColorSensitivePermGrantPolicyManager;
import com.android.server.pm.IColorSystemAppProtectManager;
import com.android.server.pm.IColorThirdPartyAppSignCheckManager;
import com.android.server.pm.PackageManagerService;
import com.android.server.power.ColorBatterySaveExtend;
import com.android.server.power.ColorPowerManagerServiceEx;
import com.android.server.power.ColorScreenOffOptimization;
import com.android.server.power.ColorSilentRebootManager;
import com.android.server.power.ColorWakeLockCheck;
import com.android.server.power.IColorBatterySaveExtend;
import com.android.server.power.IColorPowerManagerServiceEx;
import com.android.server.power.IColorScreenOffOptimization;
import com.android.server.power.IColorSilentRebootManager;
import com.android.server.power.IColorWakeLockCheck;
import com.android.server.power.PowerManagerService;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.ColorAccessControlManagerService;
import com.android.server.wm.ColorActivityTaskManagerServiceEx;
import com.android.server.wm.ColorAppChildrenSpaceManager;
import com.android.server.wm.ColorAppPhoneManager;
import com.android.server.wm.ColorAppStoreTraffic;
import com.android.server.wm.ColorAppSwitchManager;
import com.android.server.wm.ColorAthenaManager;
import com.android.server.wm.ColorBreenoManagerService;
import com.android.server.wm.ColorFullScreenDisplayManager;
import com.android.server.wm.ColorIntentInterceptManager;
import com.android.server.wm.ColorLockTaskController;
import com.android.server.wm.ColorSplitWindowManagerService;
import com.android.server.wm.ColorStartingWindowManager;
import com.android.server.wm.ColorWatermarkManager;
import com.android.server.wm.ColorWindowManagerServiceEx;
import com.android.server.wm.ColorZoomWindowManagerService;
import com.android.server.wm.IColorAccessControlLocalManager;
import com.android.server.wm.IColorActivityTaskManagerServiceEx;
import com.android.server.wm.IColorAppChildrenSpaceManager;
import com.android.server.wm.IColorAppPhoneManager;
import com.android.server.wm.IColorAppStoreTraffic;
import com.android.server.wm.IColorAppSwitchManager;
import com.android.server.wm.IColorAthenaManager;
import com.android.server.wm.IColorBreenoManager;
import com.android.server.wm.IColorFullScreenDisplayManager;
import com.android.server.wm.IColorIntentInterceptManager;
import com.android.server.wm.IColorLockTaskController;
import com.android.server.wm.IColorSplitWindowManager;
import com.android.server.wm.IColorStartingWindowManager;
import com.android.server.wm.IColorWatermarkManager;
import com.android.server.wm.IColorWindowManagerServiceEx;
import com.android.server.wm.IColorZoomWindowManager;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;

public class ColorServiceFactoryImpl extends ColorServiceFactory {
    private static final String TAG = "ColorServiceFactoryImpl";

    public <T extends IOppoCommonFeature> T getFeature(T def, Object... vars) {
        verityParams(def);
        if (!OppoFeatureManager.isSupport(def)) {
            return def;
        }
        switch (AnonymousClass1.$SwitchMap$android$common$OppoFeatureList$OppoIndex[def.index().ordinal()]) {
            case 1:
                return (T) OppoFeatureManager.getTraceMonitor(getColorSystemServerEx(vars));
            case 2:
                return (T) OppoFeatureManager.getTraceMonitor(getColorActivityManagerServiceEx(vars));
            case 3:
                return (T) OppoFeatureManager.getTraceMonitor(getColorActivityTaskManagerServiceEx(vars));
            case 4:
                return (T) OppoFeatureManager.getTraceMonitor(getColorPackageManagerServiceEx(vars));
            case 5:
                return (T) OppoFeatureManager.getTraceMonitor(getColorWindowManagerServiceEx(vars));
            case 6:
                return (T) OppoFeatureManager.getTraceMonitor(getColorPowerManagerServiceEx(vars));
            case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /* 7 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAlarmManagerServiceEx(vars));
            case 8:
                return (T) OppoFeatureManager.getTraceMonitor(getColorDeviceIdleControllerEx(vars));
            case ColorStartingWindowRUSHelper.FORCE_USE_COLOR_DRAWABLE_WHEN_SPLASH_WINDOW_TRANSLUCENT /* 9 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorJobSchedulerServiceEx(vars));
            case ColorStartingWindowRUSHelper.STARTING_WINDOW_EXIT_LONG_DURATION_PACKAGE /* 10 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorNotificationManagerServiceEx(vars));
            case ColorStartingWindowRUSHelper.SNAPSHOT_FORCE_CLEAR_WHEN_DIFF_ORIENTATION /* 11 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorOverlayManagerServiceEx(vars));
            case ColorStartingWindowRUSHelper.USE_TRANSLUCENT_DRAWABLE_FOR_SPLASH_WINDOW /* 12 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorInputMethodManagerServiceEx(vars));
            case 13:
                return (T) OppoFeatureManager.getTraceMonitor(getColorDisplayManagerServiceEx(vars));
            case 14:
                return (T) OppoFeatureManager.getTraceMonitor(getColorNetworkPolicyManagerServiceEx(vars));
            case 15:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAccessControlManagerService(vars));
            case ColorHansRestriction.HANS_RESTRICTION_BLOCK_ALARM /* 16 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorLockTaskController(vars));
            case 17:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAppStoreTraffic(vars));
            case 18:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAppPhoneManager(vars));
            case 19:
                return (T) OppoFeatureManager.getTraceMonitor(getColorFullmodeManager(vars));
            case ColorHansManager.HansMainHandler.HANS_MSG_KILL_ABNORMAL_APP /* 20 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorDataFreeManager(vars));
            case ColorHansManager.HansMainHandler.MSG_CHECK_JOB_WAKELOCK /* 21 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorIconCachesManager(vars));
            case 22:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAppStartupManager(vars));
            case 23:
                return (T) OppoFeatureManager.getTraceMonitor(getColorChildrenModeInstallManager(vars));
            case 24:
                return (T) OppoFeatureManager.getTraceMonitor(getColorDynamicFeatureManager(vars));
            case 25:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAppCrashClearManager(vars));
            case OppoNotificationManager.SDK_INT_26 /* 26 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAppSwitchManager(vars));
            case 27:
                return (T) OppoFeatureManager.getTraceMonitor(getColorBroadcastStaticRegisterWhitelistManager(vars));
            case 28:
                return (T) OppoFeatureManager.getTraceMonitor(getColorSellModeManager(vars));
            case 29:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAppInstallProgressManager(vars));
            case 30:
                return (T) OppoFeatureManager.getTraceMonitor(getColorGameSpaceManager(vars));
            case 31:
                return (T) OppoFeatureManager.getTraceMonitor(getColorScreenOffOptimization(vars));
            case ColorHansRestriction.HANS_RESTRICTION_BLOCK_SYNC /* 32 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorSplitWindowManagerService(vars));
            case 33:
                return (T) OppoFeatureManager.getTraceMonitor(getColorMultiAppManagerService(vars));
            case 34:
                return (T) OppoFeatureManager.getTraceMonitor(getColorBatterySaveExtend(vars));
            case 35:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAppListInterceptManager(vars));
            case 36:
                return (T) OppoFeatureManager.getTraceMonitor(getColorIntentInterceptManager(vars));
            case 37:
                return (T) OppoFeatureManager.getTraceMonitor(getColorDeepSleepHelper(vars));
            case 38:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAppQuickFreezeManager(vars));
            case 39:
                return (T) OppoFeatureManager.getTraceMonitor(getColorRuntimePermGrantPolicyManager(vars));
            case 40:
                return (T) OppoFeatureManager.getTraceMonitor(getColorDefaultAppPolicyManager(vars));
            case 41:
                return (T) OppoFeatureManager.getTraceMonitor(getColorDozeNetworkOptimization(vars));
            case 42:
                return (T) OppoFeatureManager.getTraceMonitor(getColorClearDataProtectManager(vars));
            case 43:
                return (T) OppoFeatureManager.getTraceMonitor(getColorGoogleAlarmRestrict(vars));
            case 44:
                return (T) OppoFeatureManager.getTraceMonitor(getColorGoogleDozeRestrict(vars));
            case 45:
                return (T) OppoFeatureManager.getTraceMonitor(getColorPackageInstallStatisticManager(vars));
            case 46:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAlarmManagerHelper(vars));
            case 47:
                return (T) OppoFeatureManager.getTraceMonitor(getColorJobScheduleManager(vars));
            case 48:
                return (T) OppoFeatureManager.getTraceMonitor(getColorSecurePayManager(vars));
            case 49:
                return (T) OppoFeatureManager.getTraceMonitor(getColorPackageInstallInterceptManager(vars));
            case 50:
                return (T) OppoFeatureManager.getTraceMonitor(getColorSensitivePermGrantPolicyManager(vars));
            case 51:
                return (T) OppoFeatureManager.getTraceMonitor(getColorInstallThreadsControlManager(vars));
            case 52:
                return (T) OppoFeatureManager.getTraceMonitor(getColorHansManager(vars));
            case 53:
                return (T) OppoFeatureManager.getTraceMonitor(getColorCommonListManagerr(vars));
            case 54:
                return (T) OppoFeatureManager.getTraceMonitor(getColorThirdPartyAppSignCheckManager(vars));
            case 55:
                return (T) OppoFeatureManager.getTraceMonitor(getColorBroadcastManager(vars));
            case 56:
                return (T) OppoFeatureManager.getTraceMonitor(getColorForbidUninstallAppManager(vars));
            case 57:
                return (T) OppoFeatureManager.getTraceMonitor(getColorStrictModeManager(vars));
            case 58:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAbnormalAppManager(vars));
            case 59:
                return (T) OppoFeatureManager.getTraceMonitor(getColorFastAppManager(vars));
            case 60:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAlarmAlignment(vars));
            case 61:
                return (T) OppoFeatureManager.getTraceMonitor(getColorSecurityPermissionManager(vars));
            case 62:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAlarmTempWhitelist(vars));
            case 63:
                return (T) OppoFeatureManager.getTraceMonitor(getColorDeviceIdleHelper(vars));
            case ColorHansRestriction.HANS_RESTRICTION_BLOCK_JOB /* 64 */:
                return (T) OppoFeatureManager.getTraceMonitor(getColorSmartDozeHelper(vars));
            case 65:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoDynamicLogManager(vars));
            case 66:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAIBrightManager(vars));
            case 67:
                return (T) OppoFeatureManager.getTraceMonitor(getColorLanguageManager(vars));
            case 68:
                return (T) OppoFeatureManager.getTraceMonitor(getColorExpDefaultBrowserManager(vars));
            case 69:
                return (T) OppoFeatureManager.getTraceMonitor(getColorForbidHideOrDisableManager(vars));
            case 70:
                return (T) OppoFeatureManager.getTraceMonitor(getColorLanguageEnableManager(vars));
            case 71:
                return (T) OppoFeatureManager.getTraceMonitor(getColorPkgStartInfoManager(vars));
            case 72:
                return (T) OppoFeatureManager.getTraceMonitor(getColorOtaManager(vars));
            case 73:
                return (T) OppoFeatureManager.getTraceMonitor(getColorBreenoManager(vars));
            case 74:
                return (T) OppoFeatureManager.getTraceMonitor(getColorFullScreenDisplayManager(vars));
            case 75:
                return (T) OppoFeatureManager.getTraceMonitor(getColorRemovableAppManager(vars));
            case 76:
                return (T) OppoFeatureManager.getTraceMonitor(getColorSystemAppProtectManager(vars));
            case 77:
                return (T) OppoFeatureManager.getTraceMonitor(getColorSilentRebootManager(vars));
            case 78:
                return (T) OppoFeatureManager.getTraceMonitor(getColorWakeLockCheck(vars));
            case 79:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAppChildrenSpaceManager(vars));
            case 80:
                return (T) OppoFeatureManager.getTraceMonitor(getColorZoomWindowManagerService(vars));
            case 81:
                return (T) OppoFeatureManager.getTraceMonitor(getColorPerfManager(vars));
            case 82:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAthenaAmManager(vars));
            case 83:
                return (T) OppoFeatureManager.getTraceMonitor(getColorAthenaManager(vars));
            case 84:
                return (T) OppoFeatureManager.getTraceMonitor(getColorWatermarkManager(vars));
            case 85:
                return (T) OppoFeatureManager.getTraceMonitor(getColorEapManager(vars));
            case 86:
                return (T) OppoFeatureManager.getTraceMonitor(getColorDexMetadataManager(vars));
            case 87:
                return (T) OppoFeatureManager.getTraceMonitor(getColorBatteryIdleManager(vars));
            case 88:
                return (T) OppoFeatureManager.getTraceMonitor(getColorStartingWindowManager(vars));
            case 89:
                return (T) OppoFeatureManager.getTraceMonitor(getColorEdgeTouchManager(vars));
            case 90:
                return (T) OppoFeatureManager.getTraceMonitor(getColorKeyLayoutManagerService(vars));
            case 91:
                return (T) OppoFeatureManager.getTraceMonitor(getColorMergedProcessSplitManager(vars));
            case 92:
                return (T) OppoFeatureManager.getTraceMonitor(getColorPmsSupportedFunctionManager(vars));
            case 93:
                return (T) OppoFeatureManager.getTraceMonitor(getColorResourcePreloadManager(vars));
            case 94:
                return (T) OppoFeatureManager.getTraceMonitor(getColorKeyEventManager(vars));
            case 95:
                return (T) OppoFeatureManager.getTraceMonitor(getColorBatteryProcessStats(vars));
            default:
                Slog.i(TAG, "Unknow feature:" + def.index().name());
                return def;
        }
    }

    /* renamed from: com.android.server.ColorServiceFactoryImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$common$OppoFeatureList$OppoIndex = new int[OppoFeatureList.OppoIndex.values().length];

        static {
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorSystemServerEx.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorActivityManagerServiceEx.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorActivityTaskManagerServiceEx.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorPackageManagerServiceEx.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorWindowManagerServiceEx.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorPowerManagerServiceEx.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAlarmManagerServiceEx.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDeviceIdleControllerEx.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorJobSchedulerServiceEx.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorNotificationManagerServiceEx.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorOverlayManagerServiceEx.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorInputMethodManagerServiceEx.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDisplayManagerServiceEx.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorNetworkPolicyManagerServiceEx.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAccessControlLocalManager.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorLockTaskController.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAppStoreTraffic.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAppPhoneManager.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorFullmodeManager.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDataFreeManager.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorIconCachesManager.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAppStartupManager.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorChildrenModeInstallManager.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDynamicFeatureManager.ordinal()] = 24;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAppCrashClearManager.ordinal()] = 25;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAppSwitchManager.ordinal()] = 26;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorBroadcastStaticRegisterWhitelistManager.ordinal()] = 27;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorSellModeManager.ordinal()] = 28;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAppInstallProgressManager.ordinal()] = 29;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorGameSpaceManager.ordinal()] = 30;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorScreenOffOptimization.ordinal()] = 31;
            } catch (NoSuchFieldError e31) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorSplitWindowManager.ordinal()] = 32;
            } catch (NoSuchFieldError e32) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorMultiAppManager.ordinal()] = 33;
            } catch (NoSuchFieldError e33) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorBatterySaveExtend.ordinal()] = 34;
            } catch (NoSuchFieldError e34) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAppListInterceptManager.ordinal()] = 35;
            } catch (NoSuchFieldError e35) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorIntentInterceptManager.ordinal()] = 36;
            } catch (NoSuchFieldError e36) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDeepSleepHelper.ordinal()] = 37;
            } catch (NoSuchFieldError e37) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAppQuickFreezeManager.ordinal()] = 38;
            } catch (NoSuchFieldError e38) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorRuntimePermGrantPolicyManager.ordinal()] = 39;
            } catch (NoSuchFieldError e39) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDefaultAppPolicyManager.ordinal()] = 40;
            } catch (NoSuchFieldError e40) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDozeNetworkOptimization.ordinal()] = 41;
            } catch (NoSuchFieldError e41) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorClearDataProtectManager.ordinal()] = 42;
            } catch (NoSuchFieldError e42) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorGoogleAlarmRestrict.ordinal()] = 43;
            } catch (NoSuchFieldError e43) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorGoogleDozeRestrict.ordinal()] = 44;
            } catch (NoSuchFieldError e44) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorPackageInstallStatisticManager.ordinal()] = 45;
            } catch (NoSuchFieldError e45) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAlarmManagerHelper.ordinal()] = 46;
            } catch (NoSuchFieldError e46) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorJobScheduleManager.ordinal()] = 47;
            } catch (NoSuchFieldError e47) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorSecurePayManager.ordinal()] = 48;
            } catch (NoSuchFieldError e48) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorPackageInstallInterceptManager.ordinal()] = 49;
            } catch (NoSuchFieldError e49) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorSensitivePermGrantPolicyManager.ordinal()] = 50;
            } catch (NoSuchFieldError e50) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorInstallThreadsControlManager.ordinal()] = 51;
            } catch (NoSuchFieldError e51) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorHansManager.ordinal()] = 52;
            } catch (NoSuchFieldError e52) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorCommonListManager.ordinal()] = 53;
            } catch (NoSuchFieldError e53) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorThirdPartyAppSignCheckManager.ordinal()] = 54;
            } catch (NoSuchFieldError e54) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorBroadcastManager.ordinal()] = 55;
            } catch (NoSuchFieldError e55) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorForbidUninstallAppManager.ordinal()] = 56;
            } catch (NoSuchFieldError e56) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorStrictModeManager.ordinal()] = 57;
            } catch (NoSuchFieldError e57) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAbnormalAppManager.ordinal()] = 58;
            } catch (NoSuchFieldError e58) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorFastAppManager.ordinal()] = 59;
            } catch (NoSuchFieldError e59) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAlarmAlignment.ordinal()] = 60;
            } catch (NoSuchFieldError e60) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorSecurityPermissionManager.ordinal()] = 61;
            } catch (NoSuchFieldError e61) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAlarmTempWhitelist.ordinal()] = 62;
            } catch (NoSuchFieldError e62) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDeviceIdleHelper.ordinal()] = 63;
            } catch (NoSuchFieldError e63) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorSmartDozeHelper.ordinal()] = 64;
            } catch (NoSuchFieldError e64) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDynamicLogManager.ordinal()] = 65;
            } catch (NoSuchFieldError e65) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAIBrightManager.ordinal()] = 66;
            } catch (NoSuchFieldError e66) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorLanguageManager.ordinal()] = 67;
            } catch (NoSuchFieldError e67) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorExpDefaultBrowserManager.ordinal()] = 68;
            } catch (NoSuchFieldError e68) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorForbidHideOrDisableManager.ordinal()] = 69;
            } catch (NoSuchFieldError e69) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorLanguageEnableManager.ordinal()] = 70;
            } catch (NoSuchFieldError e70) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorPkgStartInfoManager.ordinal()] = 71;
            } catch (NoSuchFieldError e71) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorOtaDataManager.ordinal()] = 72;
            } catch (NoSuchFieldError e72) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorBreenoManager.ordinal()] = 73;
            } catch (NoSuchFieldError e73) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorFullScreenDisplayManager.ordinal()] = 74;
            } catch (NoSuchFieldError e74) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorRemovableAppManager.ordinal()] = 75;
            } catch (NoSuchFieldError e75) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorSystemAppProtectManager.ordinal()] = 76;
            } catch (NoSuchFieldError e76) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorSilentRebootManager.ordinal()] = 77;
            } catch (NoSuchFieldError e77) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorWakeLockCheck.ordinal()] = 78;
            } catch (NoSuchFieldError e78) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAppChildrenSpaceManager.ordinal()] = 79;
            } catch (NoSuchFieldError e79) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorZoomWindowManager.ordinal()] = 80;
            } catch (NoSuchFieldError e80) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorPerfManager.ordinal()] = 81;
            } catch (NoSuchFieldError e81) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAthenaAmManager.ordinal()] = 82;
            } catch (NoSuchFieldError e82) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAthenaManager.ordinal()] = 83;
            } catch (NoSuchFieldError e83) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorWatermarkManager.ordinal()] = 84;
            } catch (NoSuchFieldError e84) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorEapManager.ordinal()] = 85;
            } catch (NoSuchFieldError e85) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDexMetadataManager.ordinal()] = 86;
            } catch (NoSuchFieldError e86) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IBatteryIdleController.ordinal()] = 87;
            } catch (NoSuchFieldError e87) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorStartingWindowManager.ordinal()] = 88;
            } catch (NoSuchFieldError e88) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorEdgeTouchManager.ordinal()] = 89;
            } catch (NoSuchFieldError e89) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorKeyLayoutManager.ordinal()] = 90;
            } catch (NoSuchFieldError e90) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorMergedProcessSplitManager.ordinal()] = 91;
            } catch (NoSuchFieldError e91) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorPmsSupportedFunctionManager.ordinal()] = 92;
            } catch (NoSuchFieldError e92) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorResourcePreloadManager.ordinal()] = 93;
            } catch (NoSuchFieldError e93) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorKeyEventManager.ordinal()] = 94;
            } catch (NoSuchFieldError e94) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorBatteryProcessStats.ordinal()] = 95;
            } catch (NoSuchFieldError e95) {
            }
        }
    }

    private IColorSystemServerEx getColorSystemServerEx(Object... vars) {
        Slog.i(TAG, "getColorSystemServerEx");
        verityParamsType("getColorSystemServerEx", vars, 1, new Class[]{Context.class});
        return new ColorSystemServerEx((Context) vars[0]);
    }

    private IColorActivityManagerServiceEx getColorActivityManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorActivityManagerServiceEx");
        verityParamsType("getColorActivityManagerServiceEx", vars, 2, new Class[]{Context.class, ActivityManagerService.class});
        return new ColorActivityManagerServiceEx((Context) vars[0], (ActivityManagerService) vars[1]);
    }

    private IColorActivityTaskManagerServiceEx getColorActivityTaskManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorActivityTaskManagerServiceEx");
        verityParamsType("getColorActivityTaskManagerServiceEx", vars, 2, new Class[]{Context.class, ActivityTaskManagerService.class});
        return new ColorActivityTaskManagerServiceEx((Context) vars[0], (ActivityTaskManagerService) vars[1]);
    }

    private IColorPackageManagerServiceEx getColorPackageManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorPackageManagerServiceEx");
        verityParamsType("getColorPackageManagerServiceEx", vars, 2, new Class[]{Context.class, PackageManagerService.class});
        return new ColorPackageManagerServiceEx((Context) vars[0], (PackageManagerService) vars[1]);
    }

    private IColorPowerManagerServiceEx getColorPowerManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorPowerManagerServiceEx");
        verityParamsType("getColorPowerManagerServiceEx", vars, 2, new Class[]{Context.class, PowerManagerService.class});
        return new ColorPowerManagerServiceEx((Context) vars[0], (PowerManagerService) vars[1]);
    }

    private IColorAlarmManagerServiceEx getColorAlarmManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorAlarmManagerServiceEx");
        verityParamsType("getColorAlarmManagerServiceEx", vars, 2, new Class[]{Context.class, AlarmManagerService.class});
        return new ColorAlarmManagerServiceEx((Context) vars[0], (AlarmManagerService) vars[1]);
    }

    private IColorDeviceIdleControllerEx getColorDeviceIdleControllerEx(Object... vars) {
        Slog.i(TAG, "getColorDeviceIdleControllerEx impl");
        verityParamsType("getColorDeviceIdleControllerEx", vars, 2, new Class[]{Context.class, DeviceIdleController.class});
        return new ColorDeviceIdleControllerEx((Context) vars[0], (DeviceIdleController) vars[1]);
    }

    private IColorJobSchedulerServiceEx getColorJobSchedulerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorJobSchedulerServiceEx impl");
        verityParamsType("getColorJobSchedulerServiceEx", vars, 2, new Class[]{Context.class, JobSchedulerService.class});
        return new ColorJobSchedulerServiceEx((Context) vars[0], (JobSchedulerService) vars[1]);
    }

    private IColorWindowManagerServiceEx getColorWindowManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorWindowManagerServiceEx impl");
        verityParamsType("getColorWindowManagerServiceEx", vars, 2, new Class[]{Context.class, WindowManagerService.class});
        return new ColorWindowManagerServiceEx((Context) vars[0], (WindowManagerService) vars[1]);
    }

    private IColorNotificationManagerServiceEx getColorNotificationManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorNotificationManagerServiceEx impl");
        verityParamsType("getColorNotificationManagerServiceEx", vars, 2, new Class[]{Context.class, NotificationManagerService.class});
        return new ColorNotificationManagerServiceEx((Context) vars[0], (NotificationManagerService) vars[1]);
    }

    private int getColorSystemThemeEx(Object... vars) {
        Slog.i(TAG, "getColorSystemThemeEx impl");
        return ColorSystemThemeChooser.getDefaultSystemTheme();
    }

    private IColorMasterClearEx getColorMasterClearEx(Object... vars) {
        Slog.i(TAG, "getColorMasterClearEx impl");
        verityParamsType("getColorMasterClearEx", vars, 1, new Class[]{Context.class});
        return new ColorMasterClearEx((Context) vars[0]);
    }

    private IColorOverlayManagerServiceEx getColorOverlayManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorOverylayManagerServiceEx impl");
        verityParamsType("getColorOverlayManagerServiceEx", vars, 1, new Class[]{Context.class});
        return new ColorOverlayManagerServiceEx((Context) vars[0]);
    }

    private InputMethodManagerService getColorInputMethodManagerService(Object... vars) {
        Slog.i(TAG, "getColorInputMethodManagerService");
        verityParamsType("getColorInputMethodManagerService", vars, 1, new Class[]{Context.class});
        return new ColorInputMethodManagerService((Context) vars[0]);
    }

    private IColorNetworkPolicyManagerServiceEx getColorNetworkPolicyManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorNetworkPolicyManagerServiceEx impl");
        verityParamsType("getColorNetworkPolicyManagerServiceEx", vars, 2, new Class[]{Context.class, NetworkPolicyManagerService.class});
        return new ColorNetworkPolicyManagerServiceEx((Context) vars[0], (NetworkPolicyManagerService) vars[1]);
    }

    private LockSettingsService getLockSettingsService(Object... vars) {
        Slog.i(TAG, "getLockSettingsService");
        verityParamsType("getLockSettingsService", vars, 1, new Class[]{Context.class});
        return new ColorLockSettingsService((Context) vars[0]);
    }

    private IColorInputMethodManagerServiceEx getColorInputMethodManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorInputMethodManagerServiceEx");
        verityParamsType("getColorInputMethodManagerServiceEx", vars, 2, new Class[]{Context.class, InputMethodManagerService.class});
        return new ColorInputMethodManagerServiceEx((Context) vars[0], (InputMethodManagerService) vars[1]);
    }

    private IColorEyeProtectManager getColorEyeProtectManager(Object... vars) {
        Slog.i(TAG, "getColorEyeProtectManager");
        return ColorEyeProtectManager.getInstance();
    }

    private IColorStartingWindowManager getColorStartingWindowManager(Object... vars) {
        return ColorStartingWindowManager.getInstance();
    }

    private IColorDisplayManagerServiceEx getColorDisplayManagerServiceEx(Object... vars) {
        Slog.i(TAG, "getColorDisplayManagerServiceEx");
        verityParamsType("getColorDisplayManagerServiceEx", vars, 2, new Class[]{Context.class, DisplayManagerService.class});
        return new ColorDisplayManagerServiceEx((Context) vars[0], (DisplayManagerService) vars[1]);
    }

    private IColorAccessControlLocalManager getColorAccessControlManagerService(Object... vars) {
        Slog.i(TAG, "getColorAccessControlManagerService");
        verityParamsType("getColorAccessControlManagerService", vars, 1, new Class[]{ActivityTaskManagerService.class});
        return new ColorAccessControlManagerService((ActivityTaskManagerService) vars[0]);
    }

    private IColorLockTaskController getColorLockTaskController(Object... vars) {
        Slog.i(TAG, "getColorLockTaskController");
        verityParamsType("getColorLockTaskController", vars, 0, new Class[0]);
        return ColorLockTaskController.getInstance();
    }

    private IColorAppPhoneManager getColorAppPhoneManager(Object... vars) {
        Slog.i(TAG, "getColorAppPhoneManager");
        verityParamsType("getColorAppPhoneManager", vars, 0, new Class[0]);
        return ColorAppPhoneManager.getInstance();
    }

    private IColorAppStoreTraffic getColorAppStoreTraffic(Object... vars) {
        Slog.i(TAG, "getColorAppStoreTraffic");
        verityParamsType("getColorAppStoreTraffic", vars, 0, new Class[0]);
        return ColorAppStoreTraffic.getInstance();
    }

    private IColorFullmodeManager getColorFullmodeManager(Object... vars) {
        Slog.i(TAG, "getColorFullmodeManager");
        verityParamsType("getColorFullmodeManager", vars, 0, new Class[0]);
        return ColorFullmodeManager.getInstance();
    }

    private IColorDataFreeManager getColorDataFreeManager(Object... vars) {
        Slog.i(TAG, "getColorDataFreeManager");
        verityParamsType("getColorDataFreeManager", vars, 0, new Class[0]);
        return ColorDataFreeManager.getInstance();
    }

    private IColorIconCachesManager getColorIconCachesManager(Object... vars) {
        Slog.i(TAG, "getColorIconCachesManager");
        verityParamsType("getColorIconCachesManager", vars, 0, new Class[0]);
        return ColorIconCachesManager.getInstance();
    }

    private IColorAppStartupManager getColorAppStartupManager(Object... vars) {
        Slog.i(TAG, "getColorAppStartupManager");
        verityParamsType("getColorAppStartupManager", vars, 0, new Class[0]);
        return ColorAppStartupManager.getInstance();
    }

    private IColorChildrenModeInstallManager getColorChildrenModeInstallManager(Object... vars) {
        Slog.i(TAG, "getColorChildrenModeInstallManager");
        verityParamsType("getColorChildrenModeInstallManager", vars, 0, new Class[0]);
        return ColorChildrenModeInstallManager.getInstance();
    }

    private IColorDynamicFeatureManager getColorDynamicFeatureManager(Object... vars) {
        Slog.i(TAG, "getColorDynamicFeatureManager");
        verityParamsType("getColorDynamicFeatureManager", vars, 0, new Class[0]);
        return ColorDynamicFeatureManager.getInstance();
    }

    private IColorAppCrashClearManager getColorAppCrashClearManager(Object... vars) {
        Slog.i(TAG, "getColorAppCrashClearManager");
        verityParamsType("getColorAppCrashClearManager", vars, 0, new Class[0]);
        return ColorAppCrashClearManager.getInstance();
    }

    private IColorAppSwitchManager getColorAppSwitchManager(Object... vars) {
        Slog.i(TAG, "getColorAppSwitchManager");
        verityParamsType("getColorAppSwitchManager", vars, 0, new Class[0]);
        return ColorAppSwitchManager.getInstance();
    }

    private IColorBroadcastStaticRegisterWhitelistManager getColorBroadcastStaticRegisterWhitelistManager(Object... vars) {
        Slog.i(TAG, "getColorBroadcastStaticRegisterWhitelistManager");
        verityParamsType("getColorBroadcastStaticRegisterWhitelistManager", vars, 0, new Class[0]);
        return ColorBroadcastStaticRegisterWhitelistManager.getInstance();
    }

    private IColorSellModeManager getColorSellModeManager(Object... vars) {
        Slog.i(TAG, "getColorSellModeManager");
        verityParamsType("getColorSellModeManager", vars, 0, new Class[0]);
        return ColorSellModeManager.getInstance();
    }

    private IColorAppInstallProgressManager getColorAppInstallProgressManager(Object... vars) {
        Slog.i(TAG, "getColorAppInstallProgressManager");
        verityParamsType("getColorAppInstallProgressManager", vars, 0, new Class[0]);
        return ColorAppInstallProgressManager.getInstance();
    }

    private IColorGameSpaceManager getColorGameSpaceManager(Object... vars) {
        Slog.i(TAG, "getColorGameSpaceManager");
        verityParamsType("getColorGameSpaceManager", vars, 0, new Class[0]);
        return ColorGameSpaceManager.getInstance();
    }

    private IColorScreenOffOptimization getColorScreenOffOptimization(Object... vars) {
        Slog.i(TAG, "getColorScreenOffOptimization");
        verityParamsType("getColorScreenOffOptimization", vars, 0, new Class[0]);
        return ColorScreenOffOptimization.getInstance();
    }

    private IColorSplitWindowManager getColorSplitWindowManagerService(Object... vars) {
        Slog.i(TAG, "getColorSplitWindowManagerService");
        verityParamsType("getColorSplitWindowManagerService", vars, 0, new Class[0]);
        return ColorSplitWindowManagerService.getInstance();
    }

    private IColorMultiAppManager getColorMultiAppManagerService(Object... vars) {
        Slog.i(TAG, "getColorMultiAppManagerService");
        verityParamsType("getColorMultiAppManagerService", vars, 0, new Class[0]);
        return ColorMultiAppManagerService.getInstance();
    }

    private IColorBatterySaveExtend getColorBatterySaveExtend(Object... vars) {
        Slog.i(TAG, "getColorBatterySaveExtend");
        verityParamsType("getColorBatterySaveExtend", vars, 1, new Class[]{Context.class});
        return ColorBatterySaveExtend.getInstance((Context) vars[0]);
    }

    private IColorAppListInterceptManager getColorAppListInterceptManager(Object... vars) {
        Slog.i(TAG, "getColorAppListInterceptManager");
        verityParamsType("getColorAppListInterceptManager", vars, 0, new Class[0]);
        return ColorAppListInterceptManager.getInstance();
    }

    private IColorIntentInterceptManager getColorIntentInterceptManager(Object... vars) {
        Slog.i(TAG, "getColorIntentInterceptManager");
        verityParamsType("getColorIntentInterceptManager", vars, 0, new Class[0]);
        return ColorIntentInterceptManager.getInstance();
    }

    private IColorDeepSleepHelper getColorDeepSleepHelper(Object... vars) {
        Slog.i(TAG, "getColorDeepSleepHelper");
        verityParamsType("getColorDeepSleepHelper", vars, 0, new Class[0]);
        return ColorDeepSleepHelper.getInstance();
    }

    private IColorAppQuickFreezeManager getColorAppQuickFreezeManager(Object... vars) {
        Slog.i(TAG, "getColorAppQuickFreezeManager");
        verityParamsType("getColorAppQuickFreezeManager", vars, 0, new Class[0]);
        return ColorAppQuickFreezeManager.getInstance();
    }

    private IColorRuntimePermGrantPolicyManager getColorRuntimePermGrantPolicyManager(Object... vars) {
        Slog.i(TAG, "getColorRuntimePermGrantPolicyManager");
        verityParamsType("getColorRuntimePermGrantPolicyManager", vars, 0, new Class[0]);
        return ColorRuntimePermGrantPolicyManager.getInstance();
    }

    private IColorDefaultAppPolicyManager getColorDefaultAppPolicyManager(Object... vars) {
        Slog.i(TAG, "getColorDefaultAppPolicyManager");
        verityParamsType("getColorDefaultAppPolicyManager", vars, 0, new Class[0]);
        return ColorDefaultAppPolicyManager.getInstance();
    }

    private IColorDozeNetworkOptimization getColorDozeNetworkOptimization(Object... vars) {
        Slog.i(TAG, "getColorDozeNetworkOptimization");
        verityParamsType("getColorDozeNetworkOptimization", vars, 0, new Class[0]);
        return new ColorDozeNetworkOptimization();
    }

    private IColorClearDataProtectManager getColorClearDataProtectManager(Object... vars) {
        Slog.i(TAG, "getColorClearDataProtectManager");
        verityParamsType("getColorClearDataProtectManager", vars, 0, new Class[0]);
        return ColorClearDataProtectManager.getInstance();
    }

    private IColorGoogleAlarmRestrict getColorGoogleAlarmRestrict(Object... vars) {
        Slog.i(TAG, "getColorGoogleAlarmRestrict");
        verityParamsType("getColorGoogleAlarmRestrict", vars, 0, new Class[0]);
        return new ColorGoogleAlarmRestrict();
    }

    private IColorGoogleDozeRestrict getColorGoogleDozeRestrict(Object... vars) {
        Slog.i(TAG, "getColorGoogleDozeRestrict");
        verityParamsType("getColorGoogleDozeRestrict", vars, 0, new Class[0]);
        return new ColorGoogleDozeRestrict();
    }

    private IColorPackageInstallStatisticManager getColorPackageInstallStatisticManager(Object... vars) {
        Slog.i(TAG, "getColorPackageInstallStatisticManager");
        verityParamsType("getColorPackageInstallStatisticManager", vars, 0, new Class[0]);
        return ColorPackageInstallStatisticManager.getInstance();
    }

    private IColorAlarmManagerHelper getColorAlarmManagerHelper(Object... vars) {
        Slog.i(TAG, "getColorAlarmManagerHelper");
        verityParamsType("getColorAlarmManagerHelper", vars, 0, new Class[0]);
        return ColorAlarmManagerHelper.getInstance();
    }

    private IColorJobScheduleManager getColorJobScheduleManager(Object... vars) {
        Slog.i(TAG, "getColorJobScheduleManager");
        verityParamsType("getColorJobScheduleManager", vars, 0, new Class[0]);
        return ColorJobScheduleManager.getInstance();
    }

    private IColorSecurePayManager getColorSecurePayManager(Object... vars) {
        Slog.i(TAG, "getColorSecurePayManager");
        verityParamsType("getColorSecurePayManager", vars, 0, new Class[0]);
        return ColorSecurePayManager.getInstance();
    }

    private IColorPackageInstallInterceptManager getColorPackageInstallInterceptManager(Object... vars) {
        Slog.i(TAG, "getColorPackageInstallInterceptManager");
        verityParamsType("getColorPackageInstallInterceptManager", vars, 0, new Class[0]);
        return ColorPackageInstallInterceptManager.getInstance();
    }

    private IColorSensitivePermGrantPolicyManager getColorSensitivePermGrantPolicyManager(Object... vars) {
        Slog.i(TAG, "getColorSensitivePermGrantPolicyManager");
        verityParamsType("getColorSensitivePermGrantPolicyManager", vars, 0, new Class[0]);
        return ColorSensitivePermGrantPolicyManager.getInstance();
    }

    private IColorInstallThreadsControlManager getColorInstallThreadsControlManager(Object... vars) {
        Slog.i(TAG, "getColorInstallThreadsControlManager");
        verityParamsType("getColorInstallThreadsControlManager", vars, 0, new Class[0]);
        return ColorInstallThreadsControlManager.getInstance();
    }

    private IColorHansManager getColorHansManager(Object... vars) {
        Slog.i(TAG, "getColorHansManager");
        verityParamsType("getColorHansManager", vars, 0, new Class[0]);
        return ColorHansManager.getInstance();
    }

    private IColorCommonListManager getColorCommonListManagerr(Object... vars) {
        Slog.i(TAG, "getColorCommonListManagerr");
        verityParamsType("getColorCommonListManagerr", vars, 0, new Class[0]);
        return ColorCommonListManager.getInstance();
    }

    private IColorThirdPartyAppSignCheckManager getColorThirdPartyAppSignCheckManager(Object... vars) {
        Slog.i(TAG, "getColorThirdPartyAppSignCheckManager");
        verityParamsType("getColorThirdPartyAppSignCheckManager", vars, 0, new Class[0]);
        return ColorThirdPartyAppSignCheckManager.getInstance();
    }

    private IColorBroadcastManager getColorBroadcastManager(Object... vars) {
        Slog.i(TAG, "getColorBroadcastManager");
        verityParamsType("getColorBroadcastManager", vars, 0, new Class[0]);
        return ColorBroadcastManager.getInstance();
    }

    private IColorForbidUninstallAppManager getColorForbidUninstallAppManager(Object... vars) {
        Slog.i(TAG, "getColorForbidUninstallAppManager");
        verityParamsType("getColorForbidUninstallAppManager", vars, 0, new Class[0]);
        return ColorForbidUninstallAppManager.getInstance();
    }

    private IColorStrictModeManager getColorStrictModeManager(Object... vars) {
        Slog.i(TAG, "getColorStrictModeManager");
        verityParamsType("getColorStrictModeManager", vars, 0, new Class[0]);
        return ColorStrictModeManager.getInstance();
    }

    private IColorAbnormalAppManager getColorAbnormalAppManager(Object... vars) {
        Slog.i(TAG, "getColorAbnormalAppManager");
        verityParamsType("getColorAbnormalAppManager", vars, 0, new Class[0]);
        return ColorAbnormalAppManager.getInstance();
    }

    private IColorFastAppManager getColorFastAppManager(Object... vars) {
        Slog.i(TAG, "getColorFastAppManager");
        verityParamsType("getColorFastAppManager", vars, 0, new Class[0]);
        return ColorFastAppManager.getInstance();
    }

    private IColorAlarmAlignment getColorAlarmAlignment(Object... vars) {
        Slog.i(TAG, "getColorAlarmAlignment");
        verityParamsType("getColorAlarmAlignment", vars, 0, new Class[0]);
        return ColorAlarmAlignment.getInstance();
    }

    private IColorSecurityPermissionManager getColorSecurityPermissionManager(Object... vars) {
        Slog.i(TAG, "getColorSecurityPermissionManager");
        verityParamsType("getColorSecurityPermissionManager", vars, 0, new Class[0]);
        return ColorSecurityPermissionManager.getInstance();
    }

    private IColorAlarmTempWhitelist getColorAlarmTempWhitelist(Object... vars) {
        Slog.i(TAG, "getColorAlarmTempWhitelist");
        verityParamsType("getColorAlarmTempWhitelist", vars, 0, new Class[0]);
        return new ColorAlarmTempWhitelist();
    }

    private IColorDeviceIdleHelper getColorDeviceIdleHelper(Object... vars) {
        Slog.i(TAG, "getColorDeviceIdleHelper");
        verityParamsType("getColorDeviceIdleHelper", vars, 0, new Class[0]);
        return ColorDeviceIdleHelper.getInstance();
    }

    private IColorSmartDozeHelper getColorSmartDozeHelper(Object... vars) {
        Slog.i(TAG, "getColorSmartDozeHelper");
        verityParamsType("getColorSmartDozeHelper", vars, 0, new Class[0]);
        return ColorSmartDozeHelper.getInstance();
    }

    private IColorDynamicLogManager getOppoDynamicLogManager(Object... vars) {
        Slog.i(TAG, "getOppoDynamicLogManager");
        verityParamsType("getOppoDynamicLogManager", vars, 0, new Class[0]);
        return OppoDynamicLogManager.getInstance();
    }

    private IColorAIBrightManager getColorAIBrightManager(Object... vars) {
        Slog.i(TAG, "getColorAIBrightManager");
        verityParamsType("getColorAIBrightManager", vars, 0, new Class[0]);
        return ColorAIBrightManager.getInstance();
    }

    private IColorLanguageManager getColorLanguageManager(Object... vars) {
        Slog.i(TAG, "getColorLanguageManager");
        verityParamsType("getColorLanguageManager", vars, 0, new Class[0]);
        return ColorLanguageManager.getInstance();
    }

    private IColorExpDefaultBrowserManager getColorExpDefaultBrowserManager(Object... vars) {
        Slog.i(TAG, "getColorExpDefaultBrowserManager");
        verityParamsType("getColorExpDefaultBrowserManager", vars, 0, new Class[0]);
        return ColorExpDefaultBrowserManager.getInstance();
    }

    private IColorForbidHideOrDisableManager getColorForbidHideOrDisableManager(Object... vars) {
        Slog.i(TAG, "getColorForbidHideOrDisableManager");
        verityParamsType("getColorForbidHideOrDisableManager", vars, 0, new Class[0]);
        return ColorForbidHideOrDisableManager.getInstance();
    }

    private IColorLanguageEnableManager getColorLanguageEnableManager(Object... vars) {
        Slog.i(TAG, "getColorLanguageEnableManager");
        verityParamsType("getColorLanguageEnableManager", vars, 0, new Class[0]);
        return ColorLanguageEnableManager.getInstance();
    }

    private IColorPkgStartInfoManager getColorPkgStartInfoManager(Object... vars) {
        Slog.i(TAG, "getColorPkgStartInfoManager");
        verityParamsType("getColorPkgStartInfoManager", vars, 0, new Class[0]);
        return ColorPkgStartInfoManager.getInstance();
    }

    private IColorOtaDataManager getColorOtaManager(Object... vars) {
        Slog.i(TAG, "getColorOtaDataManager");
        verityParamsType("getColorOtaDataManager", vars, 0, new Class[0]);
        return ColorOtaDataManager.getInstance();
    }

    private IColorBreenoManager getColorBreenoManager(Object... vars) {
        Slog.i(TAG, "getColorBreenoManager");
        verityParamsType("getColorBreenoManager", vars, 0, new Class[0]);
        return ColorBreenoManagerService.getInstance();
    }

    private IColorFullScreenDisplayManager getColorFullScreenDisplayManager(Object... vars) {
        Slog.i(TAG, "getColorFullScreenDisplayManager");
        verityParamsType("getColorFullScreenDisplayManager", vars, 0, new Class[0]);
        return ColorFullScreenDisplayManager.getInstance();
    }

    private IColorRemovableAppManager getColorRemovableAppManager(Object... vars) {
        Slog.i(TAG, "getColorRemovableAppManager");
        verityParamsType("getColorRemovableAppManager", vars, 0, new Class[0]);
        return ColorRemovableAppManager.getInstance();
    }

    private IColorSystemAppProtectManager getColorSystemAppProtectManager(Object... vars) {
        Slog.i(TAG, "getColorSystemAppProtectManager");
        verityParamsType("getColorSystemAppProtectManager", vars, 0, new Class[0]);
        return ColorSystemAppProtectManager.getInstance();
    }

    private IColorSilentRebootManager getColorSilentRebootManager(Object... vars) {
        Slog.i(TAG, "getColorSilentRebootManager");
        verityParamsType("getColorSilentRebootManager", vars, 0, new Class[0]);
        return ColorSilentRebootManager.getInstance();
    }

    private IColorWakeLockCheck getColorWakeLockCheck(Object... vars) {
        Slog.i(TAG, "getColorWakeLockCheck");
        verityParamsType("getColorWakeLockCheck", vars, 0, new Class[0]);
        return new ColorWakeLockCheck();
    }

    private IColorAppChildrenSpaceManager getColorAppChildrenSpaceManager(Object... vars) {
        Slog.i(TAG, "getColorAppChildrenSpaceManager");
        verityParamsType("getColorAppChildrenSpaceManager", vars, 0, new Class[0]);
        return ColorAppChildrenSpaceManager.getInstance();
    }

    private IColorZoomWindowManager getColorZoomWindowManagerService(Object... vars) {
        Slog.i(TAG, "getColorZoomWindowManagerService");
        verityParamsType("getColorZoomWindowManagerService", vars, 0, new Class[0]);
        return ColorZoomWindowManagerService.getInstance();
    }

    private IColorPerfManager getColorPerfManager(Object... vars) {
        Slog.i(TAG, "getColorPerfManager");
        verityParamsType("getColorPerfManager", vars, 0, new Class[0]);
        return ColorPerfManager.getInstance();
    }

    private IColorAthenaAmManager getColorAthenaAmManager(Object... vars) {
        Slog.i(TAG, "getColorAthenaAmManager");
        verityParamsType("getColorAthenaAmManager", vars, 0, new Class[0]);
        return ColorAthenaAmManager.getInstance();
    }

    private IColorAthenaManager getColorAthenaManager(Object... vars) {
        Slog.i(TAG, "getColorAthenaManager");
        verityParamsType("getColorAthenaManager", vars, 0, new Class[0]);
        return ColorAthenaManager.getInstance();
    }

    private IColorWatermarkManager getColorWatermarkManager(Object... vars) {
        Slog.i(TAG, "getColorWatermarkManager");
        verityParamsType("getColorWatermarkManager", vars, 0, new Class[0]);
        return ColorWatermarkManager.getInstance();
    }

    private IColorEapManager getColorEapManager(Object... vars) {
        Slog.i(TAG, "getColorEapManager");
        verityParamsType("getColorEapManager", vars, 0, new Class[0]);
        return ColorEapManager.getInstance();
    }

    private IColorDexMetadataManager getColorDexMetadataManager(Object... vars) {
        Slog.i(TAG, "getColorDexMetadataManager");
        verityParamsType("getColorDexMetadataManager", vars, 0, new Class[0]);
        return ColorDexMetadataManager.getInstance();
    }

    private IColorKeyLayoutManager getColorKeyLayoutManagerService(Object... vars) {
        Slog.i(TAG, "getColorKeyLayoutManagerService");
        verityParamsType("getColorKeyLayoutManagerService", vars, 0, new Class[0]);
        return ColorKeyLayoutManagerService.getInstance();
    }

    private IBatteryIdleController getColorBatteryIdleManager(Object... vars) {
        Slog.i(TAG, "getColorBatteryIdleManager");
        verityParamsType("getColorBatteryIdleManager", vars, 0, new Class[0]);
        return new ColorBatteryIdleManager();
    }

    private IColorEdgeTouchManager getColorEdgeTouchManager(Object... vars) {
        Slog.i(TAG, "IColorEdgeTouchManager");
        verityParamsType("getColorEdgeTouchManager", vars, 0, new Class[0]);
        return ColorEdgeTouchManagerService.getInstance();
    }

    private IColorMergedProcessSplitManager getColorMergedProcessSplitManager(Object... vars) {
        Slog.i(TAG, "IColorMergedProcessSplitManager");
        verityParamsType("getColorMergedProcessSplitManager", vars, 0, new Class[0]);
        return ColorMergedProcessSplitManager.getInstance();
    }

    private IColorPmsSupportedFunctionManager getColorPmsSupportedFunctionManager(Object... vars) {
        Slog.i(TAG, "getColorPmsSupportedFunctionManager");
        verityParamsType("getColorPmsSupportedFunctionManager", vars, 0, new Class[0]);
        return ColorPmsSupportedFunctionManager.getInstance();
    }

    private IColorResourcePreloadManager getColorResourcePreloadManager(Object... vars) {
        Slog.i(TAG, "getColorResourcePreloadManager");
        verityParamsType("getColorResourcePreloadManager", vars, 0, new Class[0]);
        return ColorResourcePreloadManager.getInstance();
    }

    private IColorKeyEventManager getColorKeyEventManager(Object... vars) {
        Slog.i(TAG, "getColorKeyEventManager");
        verityParamsType("getColorKeyEventManager", vars, 0, new Class[0]);
        return ColorKeyEventManagerService.getInstance();
    }

    private IColorBatteryProcessStats getColorBatteryProcessStats(Object... vars) {
        Slog.i(TAG, "getColorBatteryProcessStats");
        verityParamsType("getColorBatteryProcessStats", vars, 0, new Class[0]);
        return new ColorBatteryProcessStats();
    }

    public IColorAlarmManagerServiceEx getColorAlarmManagerServiceEx(Context context, AlarmManagerService ams) {
        warn("ColorAlarmManagerServiceEx");
        return new ColorAlarmManagerServiceEx(context, ams);
    }

    public IColorDeviceIdleControllerEx getColorDeviceIdleControllerEx(Context context, DeviceIdleController dic) {
        warn("getColorDeviceIdleControllerEx impl");
        return new ColorDeviceIdleControllerEx(context, dic);
    }

    public IColorJobSchedulerServiceEx getColorJobSchedulerServiceEx(Context context, JobSchedulerService jss) {
        warn("getColorJobSchedulerServiceEx impl");
        return new ColorJobSchedulerServiceEx(context, jss);
    }

    public IColorNotificationManagerServiceEx getColorNotificationManagerServiceEx(Context context, NotificationManagerService nms) {
        warn("getColorNotificationManagerServiceEx impl");
        return new ColorNotificationManagerServiceEx(context, nms);
    }

    public int getColorSystemThemeEx(int theme) {
        warn("getColorSystemThemeEx impl");
        return ColorSystemThemeEx.DEFAULT_SYSTEM_THEME;
    }

    public IColorMasterClearEx getColorMasterClearEx(Context context) {
        warn("getColorMasterClearEx impl");
        return new ColorMasterClearEx(context);
    }

    public IColorOverlayManagerServiceEx getColorOverlayManagerServiceEx(Context context) {
        warn("getColorOverylayManagerServiceEx impl");
        return new ColorOverlayManagerServiceEx(context);
    }

    public InputMethodManagerService getColorInputMethodManagerService(Context context) {
        warn("getColorInputMethodManagerService");
        return new ColorInputMethodManagerService(context);
    }

    public IColorNetworkPolicyManagerServiceEx getColorNetworkPolicyManagerServiceEx(Context context, NetworkPolicyManagerService nms) {
        warn("getColorNetworkPolicyManagerServiceEx dummy");
        return new ColorNetworkPolicyManagerServiceEx(context, nms);
    }

    public LockSettingsService getLockSettingsService(Context context) {
        return new ColorLockSettingsService(context);
    }

    public IColorInputMethodManagerServiceEx getColorInputMethodManagerServiceEx(Context context, InputMethodManagerService imms) {
        return new ColorInputMethodManagerServiceEx(context, imms);
    }

    public IColorEyeProtectManager getColorEyeProtectManager() {
        return ColorEyeProtectManager.getInstance();
    }

    public IColorDisplayManagerServiceEx getColorDisplayManagerServiceEx(Context context, DisplayManagerService dms) {
        return new ColorDisplayManagerServiceEx(context, dms);
    }
}
