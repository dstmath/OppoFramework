package com.android.server;

import android.common.OppoFeatureCache;
import android.util.Slog;
import com.android.server.am.IColorAbnormalAppManager;
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
import com.android.server.display.IColorAIBrightManager;
import com.android.server.job.IColorJobScheduleManager;
import com.android.server.net.IColorDozeNetworkOptimization;
import com.android.server.om.IColorLanguageManager;
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
import com.android.server.pm.IColorPkgStartInfoManager;
import com.android.server.pm.IColorPmsSupportedFunctionManager;
import com.android.server.pm.IColorRemovableAppManager;
import com.android.server.pm.IColorRuntimePermGrantPolicyManager;
import com.android.server.pm.IColorSecurePayManager;
import com.android.server.pm.IColorSellModeManager;
import com.android.server.pm.IColorSensitivePermGrantPolicyManager;
import com.android.server.pm.IColorSystemAppProtectManager;
import com.android.server.pm.IColorThirdPartyAppSignCheckManager;
import com.android.server.power.IColorBatterySaveExtend;
import com.android.server.power.IColorScreenOffOptimization;
import com.android.server.power.IColorSilentRebootManager;
import com.android.server.power.IColorWakeLockCheck;
import com.android.server.wm.IColorAccessControlLocalManager;
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
import com.android.server.wm.IColorZoomWindowManager;

public final class ColorServiceRegistry extends ColorBaseServiceRegistry {
    private static ColorServiceRegistry sInstance;

    public static ColorServiceRegistry getInstance() {
        if (sInstance == null) {
            synchronized (ColorServiceRegistry.class) {
                if (sInstance == null) {
                    sInstance = new ColorServiceRegistry();
                }
            }
        }
        return sInstance;
    }

    /* access modifiers changed from: protected */
    public void onAtmsInit() {
        Slog.i(this.TAG, "onAtmsInit");
        registerColorCustomManagerAtmsInit();
    }

    /* access modifiers changed from: protected */
    public void onAmsInit() {
        Slog.i(this.TAG, "onAmsInit");
        registerColorCustomManagerAmsInit();
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).init(this.mColorAmsEx, this.mColorAtmsEx);
    }

    /* access modifiers changed from: protected */
    public void onPowerInit() {
        Slog.i(this.TAG, "onPowerInit");
        registerColorCustomManagerPowerInit();
    }

    /* access modifiers changed from: protected */
    public void onPmsInit() {
        Slog.i(this.TAG, "onPmsInit");
        registerColorCustomManagerPmsInit();
    }

    /* access modifiers changed from: protected */
    public void onWmsInit() {
        Slog.i(this.TAG, "onWmsInit");
        registerColorCustomManagerWmsInit();
    }

    /* access modifiers changed from: protected */
    public void onAlarmInit() {
        Slog.i(this.TAG, "onAlarmInit");
        registerColorCustomManagerAlarmInit();
    }

    /* access modifiers changed from: protected */
    public void onDeviceIdleInit() {
        Slog.i(this.TAG, "onDeviceIdleInit");
        registerColorCustomManagerDeviceIdleInit();
    }

    /* access modifiers changed from: protected */
    public void onJobInit() {
        Slog.i(this.TAG, "onJobInit");
        registerColorJobScheduleManager();
    }

    /* access modifiers changed from: protected */
    public void onOmsInit() {
        Slog.i(this.TAG, "onOmsInit");
        registerColorLanguageManager();
    }

    /* access modifiers changed from: protected */
    public void onNetworkPolicyInit() {
        Slog.i(this.TAG, "onNetworkPolicyInit");
        registerColorNetworkPolicy();
    }

    /* access modifiers changed from: protected */
    public void onAmsReady() {
        Slog.i(this.TAG, "onAmsReady");
        registerColorCustomManagerAmsReady();
    }

    /* access modifiers changed from: protected */
    public void onAtmsReady() {
        Slog.i(this.TAG, "onAtmsReady");
    }

    /* access modifiers changed from: protected */
    public void onPowerReady() {
        Slog.i(this.TAG, "onPowerReady");
        registerColorCustomManagerPowerReady();
    }

    /* access modifiers changed from: protected */
    public void onPmsReady() {
        Slog.i(this.TAG, "onPmsReady");
        registerColorCustomManagerPmsReady();
    }

    /* access modifiers changed from: protected */
    public void onWmsReady() {
        Slog.i(this.TAG, "onWmsReady");
        registerColorCustomManagerWmsReady();
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).init(this.mColorWmsEx);
    }

    /* access modifiers changed from: protected */
    public void onAlarmReady() {
        Slog.i(this.TAG, "onAlarmReady");
        registerColorAlarmManagerReady();
    }

    /* access modifiers changed from: protected */
    public void onDeviceIdleReady() {
        Slog.i(this.TAG, "onDeviceIdleReady");
    }

    /* access modifiers changed from: protected */
    public void onJobReady() {
        Slog.i(this.TAG, "onJobReady");
    }

    /* access modifiers changed from: protected */
    public void onOmsReady() {
        Slog.i(this.TAG, "onOmsReady");
    }

    /* access modifiers changed from: protected */
    public void onNetworkPolicyReady() {
        Slog.i(this.TAG, "onNetworkPolicyReady");
    }

    /* access modifiers changed from: protected */
    public void onDmsInit() {
        Slog.i(this.TAG, "onDmsInit");
        registerColorDisplayManagerInit();
    }

    private void registerColorAlarmManagerReady() {
    }

    private void registerColorCustomManagerAtmsInit() {
        addColorAccessControlLocalManager();
        addColorLockTaskController();
    }

    private void registerColorCustomManagerAmsInit() {
        addColorBatteryProcessStats();
        addColorMultiAppManager();
        addColorAppSwitchManager();
        addColorBroadcastManager();
        addColorAbnormalAppManager();
        addColorEapManager();
        addZoomWindowManager();
        addEdgeTouchManager();
        addKeyLayoutManager();
        addKeyEventManager();
    }

    private void addKeyLayoutManager() {
        IColorKeyLayoutManager ckl = ColorServiceFactory.getInstance().getFeature(IColorKeyLayoutManager.DEFAULT, new Object[0]);
        ckl.init(this.mColorAmsEx.getActivityManagerService());
        OppoFeatureCache.set(ckl);
    }

    private void addKeyEventManager() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorKeyEventManager.DEFAULT, new Object[0]));
    }

    private void addEdgeTouchManager() {
        IColorEdgeTouchManager colorEdgeTouchManager = ColorServiceFactory.getInstance().getFeature(IColorEdgeTouchManager.DEFAULT, new Object[0]);
        colorEdgeTouchManager.init(this.mColorAmsEx.getActivityManagerService());
        OppoFeatureCache.set(colorEdgeTouchManager);
    }

    private void registerColorCustomManagerPowerInit() {
        addColorWakeLockCheck();
        addColorScreenOffOptimization();
        addColorBatterySaveExtend();
        addColorSilentRebootManager();
    }

    private void registerColorCustomManagerPmsInit() {
        addColorFullmodeManager();
        addColorDataFreeManager();
        addColorDynamicFeatureManager();
        addColorSellModeManager();
        addColorDefaultAppPolicyManager();
        addRuntimePermGrantPolicyManager();
        addColorSecurePayManager();
        addColorPackageInstallInterceptManager();
        addColorSystemAppProtectManager();
        addColorSensitivePermGrantPolicyManager();
        addColorAppListInterceptManager();
        addColorRemovableAppManager();
        addColorOtaDataManager();
        addColorExpDefaultBrowserManager();
        addColorLanguageEnableManager();
        addColorDexMetadataManager();
        addColorMergedProcessSplitManager();
        addColorPmsSupportedFunctionManager();
    }

    private void registerColorCustomManagerAlarmInit() {
        addColorAlarmAlignment();
        addColorDeepSleepHelper();
        addColorAlarmManagerHelper();
        addColorStrictModeManager();
        addColorGoogleAlarmRestrict();
        addColorAlarmTempWhitelist();
    }

    private void registerColorJobInit() {
    }

    private void registerColorCustomManagerWmsInit() {
        addColorBreenoManager();
        addColorFullScreenDisplayManager();
        addTalkbackWatermarkManager();
        addColorStartingWindowManager();
    }

    private void registerColorCustomManagerDeviceIdleInit() {
        addColorDeviceIdleHelper();
        addColorGoogleDozeRestrict();
        addColorSmartDozeHelper();
    }

    private void registerColorCustomManagerAmsReady() {
        addColorAppStartupManager();
        addColorHansManager();
        addColorResourcePreloadManager();
        addColorAppPhoneManager();
        addColorBroadcastStaticRegisterWhitelistManager();
        addColorAppStoreTraffic();
        addColorSplitWindowManager();
        addColorAppCrashClear();
        addColorIntentInterceptManager();
        addColorDynamicLogManager();
        addColorGameSpaceManager();
        registerColorSecurityPermissionManager();
        addColorFastAppManager();
        addColorAthenaManager();
        addColorAthenaAmManager();
        addColorPerfManager();
        addColorAppChildrenSpaceManager();
        addColorCommonListManager();
    }

    private void registerColorDisplayManagerInit() {
        addAIBrightManager();
    }

    private void addAIBrightManager() {
        IColorAIBrightManager iColorAIBrightManager = ColorServiceFactory.getInstance().getFeature(IColorAIBrightManager.DEFAULT, new Object[0]);
        iColorAIBrightManager.init(this.mColorDmsEx.getDisplayManagerService());
        OppoFeatureCache.set(iColorAIBrightManager);
    }

    private void addColorGameSpaceManager() {
        IColorGameSpaceManager iColorGameSpaceManager = ColorServiceFactory.getInstance().getFeature(IColorGameSpaceManager.DEFAULT, new Object[0]);
        iColorGameSpaceManager.init(this.mColorAmsEx.getActivityManagerService(), this.mColorNetworkPolicyEx);
        OppoFeatureCache.set(iColorGameSpaceManager);
    }

    public void registerColorCustomManagerPowerReady() {
    }

    private void registerColorCustomManagerPmsReady() {
        addColorMultiAppManager();
        addColorIconCachesManager();
        addColorChildrenModeInstallManager();
        addColorAppQuickFreezeManager();
        addColorPackageInstallStatisticManager();
        addColorForbidHideOrDisableManager();
        addColorClearDataProtectManager();
        addColorAppInstallProgressManager();
        addColorInstallThreadsControlManager();
        addColorForbidUninstallAppManager();
        addColorThirdPartyAppSignCheckManager();
        addColorPkgStartInfoManager();
    }

    public void registerColorCustomManagerWmsReady() {
        addColorFreeformManager();
    }

    private void addColorAppPhoneManager() {
        IColorAppPhoneManager colorAppPhoneManager = ColorServiceFactory.getInstance().getFeature(IColorAppPhoneManager.DEFAULT, new Object[0]);
        colorAppPhoneManager.init(this.mColorAtmsEx);
        OppoFeatureCache.set(colorAppPhoneManager);
    }

    private void addColorFreeformManager() {
    }

    private void addColorFullmodeManager() {
        IColorFullmodeManager cap = ColorServiceFactory.getInstance().getFeature(IColorFullmodeManager.DEFAULT, new Object[0]);
        cap.init(this.mColorPmsEx);
        OppoFeatureCache.set(cap);
    }

    private void addColorDataFreeManager() {
        IColorDataFreeManager cap = ColorServiceFactory.getInstance().getFeature(IColorDataFreeManager.DEFAULT, new Object[0]);
        cap.init();
        OppoFeatureCache.set(cap);
    }

    private void addColorWakeLockCheck() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorWakeLockCheck.DEFAULT, new Object[0]));
    }

    private void addColorIconCachesManager() {
        IColorIconCachesManager capIconCaches = ColorServiceFactory.getInstance().getFeature(IColorIconCachesManager.DEFAULT, new Object[0]);
        capIconCaches.init(this.mColorPmsEx);
        OppoFeatureCache.set(capIconCaches);
    }

    private void addColorChildrenModeInstallManager() {
        IColorChildrenModeInstallManager manager = ColorServiceFactory.getInstance().getFeature(IColorChildrenModeInstallManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx.getContext());
        OppoFeatureCache.set(manager);
    }

    private void addColorAppQuickFreezeManager() {
        IColorAppQuickFreezeManager capAppQuickFreeze = ColorServiceFactory.getInstance().getFeature(IColorAppQuickFreezeManager.DEFAULT, new Object[0]);
        capAppQuickFreeze.init(this.mColorPmsEx);
        OppoFeatureCache.set(capAppQuickFreeze);
    }

    private void addColorBroadcastStaticRegisterWhitelistManager() {
        IColorBroadcastStaticRegisterWhitelistManager manager = ColorServiceFactory.getInstance().getFeature(IColorBroadcastStaticRegisterWhitelistManager.DEFAULT, new Object[0]);
        manager.init();
        OppoFeatureCache.set(manager);
    }

    private void addColorAppStartupManager() {
        IColorAppStartupManager casm = ColorServiceFactory.getInstance().getFeature(IColorAppStartupManager.DEFAULT, new Object[0]);
        casm.init(this.mColorAmsEx);
        OppoFeatureCache.set(casm);
    }

    private void addColorDynamicFeatureManager() {
        IColorDynamicFeatureManager cdfm = ColorServiceFactory.getInstance().getFeature(IColorDynamicFeatureManager.DEFAULT, new Object[0]);
        cdfm.init(this.mColorPmsEx);
        OppoFeatureCache.set(cdfm);
    }

    private void addColorSplitWindowManager() {
        IColorSplitWindowManager cswm = ColorServiceFactory.getInstance().getFeature(IColorSplitWindowManager.DEFAULT, new Object[0]);
        cswm.init(this.mColorAmsEx);
        OppoFeatureCache.set(cswm);
    }

    private void addColorAppSwitchManager() {
        IColorAppSwitchManager casm = ColorServiceFactory.getInstance().getFeature(IColorAppSwitchManager.DEFAULT, new Object[0]);
        casm.init();
        OppoFeatureCache.set(casm);
    }

    private void addColorSellModeManager() {
        IColorSellModeManager csmm = ColorServiceFactory.getInstance().getFeature(IColorSellModeManager.DEFAULT, new Object[0]);
        csmm.init(this.mColorPmsEx);
        OppoFeatureCache.set(csmm);
    }

    private void addColorAlarmAlignment() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorAlarmAlignment.DEFAULT, new Object[0]));
    }

    private void addColorDeepSleepHelper() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorDeepSleepHelper.DEFAULT, new Object[0]));
    }

    private void addColorDefaultAppPolicyManager() {
        IColorDefaultAppPolicyManager manager = ColorServiceFactory.getInstance().getFeature(IColorDefaultAppPolicyManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorBreenoManager() {
        IColorBreenoManager colorBreenoManager = ColorServiceFactory.getInstance().getFeature(IColorBreenoManager.DEFAULT, new Object[0]);
        colorBreenoManager.init(this.mColorWmsEx);
        OppoFeatureCache.set(colorBreenoManager);
    }

    private void addColorFullScreenDisplayManager() {
        IColorFullScreenDisplayManager colorFullScreenDisplayManager = ColorServiceFactory.getInstance().getFeature(IColorFullScreenDisplayManager.DEFAULT, new Object[0]);
        colorFullScreenDisplayManager.init(this.mColorWmsEx);
        OppoFeatureCache.set(colorFullScreenDisplayManager);
    }

    private void addTalkbackWatermarkManager() {
        IColorWatermarkManager watermarkManager = ColorServiceFactory.getInstance().getFeature(IColorWatermarkManager.DEFAULT, new Object[0]);
        watermarkManager.init(this.mColorWmsEx);
        OppoFeatureCache.set(watermarkManager);
    }

    private void addColorClearDataProtectManager() {
        IColorClearDataProtectManager cap = ColorServiceFactory.getInstance().getFeature(IColorClearDataProtectManager.DEFAULT, new Object[0]);
        cap.init(this.mColorPmsEx);
        OppoFeatureCache.set(cap);
    }

    private void addColorMultiAppManager() {
        IColorMultiAppManager multiAppManager = ColorServiceFactory.getInstance().getFeature(IColorMultiAppManager.DEFAULT, new Object[0]);
        multiAppManager.init(this.mColorAmsEx, this.mColorPmsEx);
        OppoFeatureCache.set(multiAppManager);
    }

    private void addColorAlarmManagerHelper() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorAlarmManagerHelper.DEFAULT, new Object[0]));
    }

    private void addRuntimePermGrantPolicyManager() {
        IColorRuntimePermGrantPolicyManager capRuntimePermGrantPolicyManager = ColorServiceFactory.getInstance().getFeature(IColorRuntimePermGrantPolicyManager.DEFAULT, new Object[0]);
        capRuntimePermGrantPolicyManager.init(this.mColorPmsEx);
        OppoFeatureCache.set(capRuntimePermGrantPolicyManager);
    }

    private void addColorStrictModeManager() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorStrictModeManager.DEFAULT, new Object[0]));
    }

    private void addColorAppCrashClear() {
        IColorAppCrashClearManager cacc = ColorServiceFactory.getInstance().getFeature(IColorAppCrashClearManager.DEFAULT, new Object[0]);
        cacc.init(this.mColorAmsEx);
        OppoFeatureCache.set(cacc);
    }

    private void addColorPackageInstallStatisticManager() {
        IColorPackageInstallStatisticManager manager = ColorServiceFactory.getInstance().getFeature(IColorPackageInstallStatisticManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorBatterySaveExtend() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorBatterySaveExtend.DEFAULT, new Object[]{this.mColorPowerEx.getContext()}));
    }

    private void addColorForbidHideOrDisableManager() {
        IColorForbidHideOrDisableManager manager = ColorServiceFactory.getInstance().getFeature(IColorForbidHideOrDisableManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorAppInstallProgressManager() {
        IColorAppInstallProgressManager manager = ColorServiceFactory.getInstance().getFeature(IColorAppInstallProgressManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx, this.mColorAmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorHansManager() {
        IColorHansManager chm = ColorServiceFactory.getInstance().getFeature(IColorHansManager.DEFAULT, new Object[0]);
        chm.init(this.mColorAmsEx);
        OppoFeatureCache.set(chm);
    }

    private void addColorResourcePreloadManager() {
        IColorResourcePreloadManager crpm = ColorServiceFactory.getInstance().getFeature(IColorResourcePreloadManager.DEFAULT, new Object[0]);
        crpm.init(this.mColorAmsEx);
        OppoFeatureCache.set(crpm);
    }

    private void addColorInstallThreadsControlManager() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorInstallThreadsControlManager.DEFAULT, new Object[0]));
    }

    private void addColorForbidUninstallAppManager() {
        IColorForbidUninstallAppManager manager = ColorServiceFactory.getInstance().getFeature(IColorForbidUninstallAppManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorDeviceIdleHelper() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorDeviceIdleHelper.DEFAULT, new Object[0]));
    }

    private void addColorSecurePayManager() {
        IColorSecurePayManager manager = ColorServiceFactory.getInstance().getFeature(IColorSecurePayManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorPackageInstallInterceptManager() {
        IColorPackageInstallInterceptManager manager = ColorServiceFactory.getInstance().getFeature(IColorPackageInstallInterceptManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorThirdPartyAppSignCheckManager() {
        IColorThirdPartyAppSignCheckManager manager = ColorServiceFactory.getInstance().getFeature(IColorThirdPartyAppSignCheckManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorScreenOffOptimization() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorScreenOffOptimization.DEFAULT, new Object[0]));
    }

    private void addColorSystemAppProtectManager() {
        IColorSystemAppProtectManager manager = ColorServiceFactory.getInstance().getFeature(IColorSystemAppProtectManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorSensitivePermGrantPolicyManager() {
        IColorSensitivePermGrantPolicyManager manager = ColorServiceFactory.getInstance().getFeature(IColorSensitivePermGrantPolicyManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void registerColorSecurityPermissionManager() {
        IColorSecurityPermissionManager iColorSecurityPermissionManager = ColorServiceFactory.getInstance().getFeature(IColorSecurityPermissionManager.DEFAULT, new Object[0]);
        iColorSecurityPermissionManager.init(this.mColorAmsEx);
        OppoFeatureCache.set(iColorSecurityPermissionManager);
    }

    private void addColorAppListInterceptManager() {
        IColorAppListInterceptManager manager = ColorServiceFactory.getInstance().getFeature(IColorAppListInterceptManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorBroadcastManager() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorBroadcastManager.DEFAULT, new Object[0]));
    }

    private void addColorIntentInterceptManager() {
        IColorIntentInterceptManager manager = ColorServiceFactory.getInstance().getFeature(IColorIntentInterceptManager.DEFAULT, new Object[0]);
        manager.init(this.mColorAtmsEx, this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void registerColorJobScheduleManager() {
        IColorJobScheduleManager manager = ColorServiceFactory.getInstance().getFeature(IColorJobScheduleManager.DEFAULT, new Object[0]);
        manager.init(this.mColorJobEx);
        OppoFeatureCache.set(manager);
    }

    private void registerColorNetworkPolicy() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorDozeNetworkOptimization.DEFAULT, new Object[0]));
    }

    private void addColorAbnormalAppManager() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorAbnormalAppManager.DEFAULT, new Object[0]));
    }

    private void addColorGoogleDozeRestrict() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorGoogleDozeRestrict.DEFAULT, new Object[0]));
    }

    private void addColorGoogleAlarmRestrict() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorGoogleAlarmRestrict.DEFAULT, new Object[0]));
    }

    private void addColorFastAppManager() {
        IColorFastAppManager manager = ColorServiceFactory.getInstance().getFeature(IColorFastAppManager.DEFAULT, new Object[0]);
        manager.init(this.mColorAmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorDynamicLogManager() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorDynamicLogManager.DEFAULT, new Object[0]));
    }

    private void addColorAthenaManager() {
        IColorAthenaManager manager = ColorServiceFactory.getInstance().getFeature(IColorAthenaManager.DEFAULT, new Object[0]);
        manager.init(this.mColorAmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorAthenaAmManager() {
        IColorAthenaAmManager manager = ColorServiceFactory.getInstance().getFeature(IColorAthenaAmManager.DEFAULT, new Object[0]);
        manager.init(this.mColorAmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorSilentRebootManager() {
        IColorSilentRebootManager manager = ColorServiceFactory.getInstance().getFeature(IColorSilentRebootManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPowerEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorPkgStartInfoManager() {
        IColorPkgStartInfoManager manager = ColorServiceFactory.getInstance().getFeature(IColorPkgStartInfoManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorRemovableAppManager() {
        IColorRemovableAppManager manager = ColorServiceFactory.getInstance().getFeature(IColorRemovableAppManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorEapManager() {
        IColorEapManager manager = ColorServiceFactory.getInstance().getFeature(IColorEapManager.DEFAULT, new Object[0]);
        manager.init(this.mColorAmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorOtaDataManager() {
        IColorOtaDataManager manager = ColorServiceFactory.getInstance().getFeature(IColorOtaDataManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorExpDefaultBrowserManager() {
        IColorExpDefaultBrowserManager manager = ColorServiceFactory.getInstance().getFeature(IColorExpDefaultBrowserManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void registerColorLanguageManager() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorLanguageManager.DEFAULT, new Object[0]));
    }

    private void addColorPerfManager() {
        IColorPerfManager manager = ColorServiceFactory.getInstance().getFeature(IColorPerfManager.DEFAULT, new Object[0]);
        manager.init(this.mColorAmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorLanguageEnableManager() {
        IColorLanguageEnableManager manager = ColorServiceFactory.getInstance().getFeature(IColorLanguageEnableManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorAccessControlLocalManager() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorAccessControlLocalManager.DEFAULT, new Object[]{this.mColorAtmsEx.getActivityTaskManagerService()}));
    }

    private void addColorLockTaskController() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorLockTaskController.DEFAULT, new Object[0]));
    }

    private void addColorAppChildrenSpaceManager() {
        IColorAppChildrenSpaceManager manager = ColorServiceFactory.getInstance().getFeature(IColorAppChildrenSpaceManager.DEFAULT, new Object[0]);
        manager.init(this.mColorAtmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addZoomWindowManager() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorZoomWindowManager.DEFAULT, new Object[0]));
    }

    private void addColorAlarmTempWhitelist() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorAlarmTempWhitelist.DEFAULT, new Object[0]));
    }

    private void addColorAppStoreTraffic() {
        IColorAppStoreTraffic storeTraffic = ColorServiceFactory.getInstance().getFeature(IColorAppStoreTraffic.DEFAULT, new Object[0]);
        storeTraffic.init(this.mColorAtmsEx);
        OppoFeatureCache.set(storeTraffic);
    }

    private void addColorDexMetadataManager() {
        IColorDexMetadataManager manager = ColorServiceFactory.getInstance().getFeature(IColorDexMetadataManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorStartingWindowManager() {
        IColorStartingWindowManager manager = ColorServiceFactory.getInstance().getFeature(IColorStartingWindowManager.DEFAULT, new Object[0]);
        manager.init(this.mColorWmsEx.getWindowManagerService());
        OppoFeatureCache.set(manager);
    }

    private void addColorCommonListManager() {
        IColorCommonListManager manager = ColorServiceFactory.getInstance().getFeature(IColorCommonListManager.DEFAULT, new Object[0]);
        manager.init(this.mColorAmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorMergedProcessSplitManager() {
        IColorMergedProcessSplitManager manager = ColorServiceFactory.getInstance().getFeature(IColorMergedProcessSplitManager.DEFAULT, new Object[0]);
        manager.init();
        OppoFeatureCache.set(manager);
    }

    private void addColorPmsSupportedFunctionManager() {
        IColorPmsSupportedFunctionManager manager = ColorServiceFactory.getInstance().getFeature(IColorPmsSupportedFunctionManager.DEFAULT, new Object[0]);
        manager.init(this.mColorPmsEx);
        OppoFeatureCache.set(manager);
    }

    private void addColorSmartDozeHelper() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorSmartDozeHelper.DEFAULT, new Object[0]));
    }

    private void addColorBatteryProcessStats() {
        OppoFeatureCache.set(ColorServiceFactory.getInstance().getFeature(IColorBatteryProcessStats.DEFAULT, new Object[0]));
    }
}
