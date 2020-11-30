package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.common.OppoFeatureManager;
import android.content.Context;
import android.os.Looper;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.IPswActivityManagerDynamicLogConfigFeature;
import com.android.server.am.IPswActivityManagerServiceEx;
import com.android.server.am.PswActivityManagerDynamicLogConfigFeature;
import com.android.server.am.PswActivityManagerServiceEx;
import com.android.server.connectivity.DnsManager;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.gatewayconflict.OppoGatewayState;
import com.android.server.connectivity.gatewayconflict.OppoIPConflictDetector;
import com.android.server.connectivity.networkrecovery.OPPODnsSelfrecoveryEngine;
import com.android.server.connectivity.oppo.IOPPODnsSelfrecoveryEngine;
import com.android.server.connectivity.oppo.IOppoArpPeer;
import com.android.server.connectivity.oppo.IOppoGatewayState;
import com.android.server.display.IOppoBrightness;
import com.android.server.display.IPswFeatureBrightness;
import com.android.server.display.IPswFeatureReduceBrightness;
import com.android.server.display.PswFeatureBrightness;
import com.android.server.display.PswFeatureReduceBrightness;
import com.android.server.location.AbstractLocationProvider;
import com.android.server.location.FastNetworkLocation;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.NavigationStatusController;
import com.android.server.location.OppoCoarseToFine;
import com.android.server.location.OppoGnssDiagnosticTool;
import com.android.server.location.OppoGnssDuration;
import com.android.server.location.OppoGnssWhiteListProxy;
import com.android.server.location.OppoLbsCustomize;
import com.android.server.location.OppoLbsRepairer;
import com.android.server.location.OppoLbsRomUpdateUtil;
import com.android.server.location.OppoLocationBlacklistUtil;
import com.android.server.location.OppoLocationStatistics;
import com.android.server.location.OppoLocationStatusMonitor;
import com.android.server.location.OppoNlpProxy;
import com.android.server.location.OppoSuplController;
import com.android.server.location.interfaces.IPswCoarseToFine;
import com.android.server.location.interfaces.IPswFastNetworkLocation;
import com.android.server.location.interfaces.IPswGnssDiagnosticTool;
import com.android.server.location.interfaces.IPswGnssDuration;
import com.android.server.location.interfaces.IPswLbsCustomize;
import com.android.server.location.interfaces.IPswLbsRepairer;
import com.android.server.location.interfaces.IPswLbsRomUpdateUtil;
import com.android.server.location.interfaces.IPswLocationBlacklistUtil;
import com.android.server.location.interfaces.IPswLocationStatistics;
import com.android.server.location.interfaces.IPswLocationStatusMonitor;
import com.android.server.location.interfaces.IPswNavigationStatusController;
import com.android.server.location.interfaces.IPswNlpProxy;
import com.android.server.location.interfaces.IPswOppoGnssWhiteListProxy;
import com.android.server.location.interfaces.IPswSuplController;
import com.android.server.pm.IPswPackageManagerServiceEx;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PswPackageManagerServiceEx;
import com.android.server.power.IPswFeatureAOD;
import com.android.server.power.IPswPowerManagerServiceEx;
import com.android.server.power.IPswShutdownFeature;
import com.android.server.power.PowerManagerService;
import com.android.server.power.PswFeatureAOD;
import com.android.server.power.PswPowerManagerServiceEx;
import com.android.server.power.PswShutdownFeature;
import com.android.server.storage.OppoStorageManagerFeature;
import com.android.server.usb.IOppoUsbDeviceFeature;
import com.android.server.usb.OppoUsbDeviceFeature;
import com.android.server.vibrator.OppoVibratorFeature;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.IOppoScreenModeManagerFeature;
import com.android.server.wm.IPswActivityTaskManagerServiceEx;
import com.android.server.wm.IPswOppoAmsUtilsFeatrue;
import com.android.server.wm.IPswOppoArmyControllerFeatrue;
import com.android.server.wm.IPswOppoArmyServiceFeatrue;
import com.android.server.wm.IPswWindowManagerServiceEx;
import com.android.server.wm.OppoScreenModeManagerFeature;
import com.android.server.wm.PswActivityTaskManagerServiceEx;
import com.android.server.wm.PswOppoAmsUtilsFeatrue;
import com.android.server.wm.PswOppoArmyControllerFeatrue;
import com.android.server.wm.PswOppoArmyServiceFeatrue;
import com.android.server.wm.PswWindowManagerServiceEx;
import com.android.server.wm.WindowManagerService;
import java.lang.reflect.InvocationTargetException;

public class PswServiceFactoryImpl extends PswServiceFactory {
    private static final String TAG = "PswServiceFactoryImpl";

    public <T extends IOppoCommonFeature> T getFeature(T def, Object... vars) {
        verityParams(def);
        if (!OppoFeatureManager.isSupport(def)) {
            return def;
        }
        switch (AnonymousClass1.$SwitchMap$android$common$OppoFeatureList$OppoIndex[def.index().ordinal()]) {
            case 1:
                return (T) OppoFeatureManager.getTraceMonitor(getPswSystemServerEx(vars));
            case 2:
                return (T) OppoFeatureManager.getTraceMonitor(getPswActivityManagerServiceEx(vars));
            case 3:
                return (T) OppoFeatureManager.getTraceMonitor(getPswActivityTaskManagerServiceEx(vars));
            case 4:
                return (T) OppoFeatureManager.getTraceMonitor(getPswWindowManagerService(vars));
            case 5:
                return (T) OppoFeatureManager.getTraceMonitor(getPswPackageManagerService(vars));
            case 6:
                return (T) OppoFeatureManager.getTraceMonitor(getPswPowerManagerService(vars));
            case 7:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoLocationBlacklistUtil(vars));
            case 8:
                return (T) OppoFeatureManager.getTraceMonitor(getNavigationStatusController(vars));
            case 9:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoGnssWhiteListProxy(vars));
            case 10:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoNlpProxy(vars));
            case 11:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoSuplController(vars));
            case 12:
                return (T) OppoFeatureManager.getTraceMonitor(getGnssDiagnosticTool(vars));
            case 13:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoLbsCustomize(vars));
            case 14:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoLbsRepairer(vars));
            case 15:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoLbsRomUpdateUtil(vars));
            case 16:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoGnssDuration(vars));
            case 17:
                return (T) OppoFeatureManager.getTraceMonitor(getFastNetworkLocation(vars));
            case 18:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoCoarseToFine(vars));
            case 19:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoLocationStatistics(vars));
            case 20:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoLocationStatusMonitor(vars));
            case 21:
                return (T) OppoFeatureManager.getTraceMonitor(getPswFeatureAOD(vars));
            case 22:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoGatewayState(vars));
            case 23:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoIPConflictDetector(vars));
            case 24:
                return (T) OppoFeatureManager.getTraceMonitor(getOPPODnsSelfrecoveryEngine(vars));
            case 25:
                return (T) OppoFeatureManager.getTraceMonitor(getPswShutdownFeature(vars));
            case 26:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoBrightness(vars));
            case 27:
                return (T) OppoFeatureManager.getTraceMonitor(getPswFeatureReduceBrightness(vars));
            case 28:
                return (T) OppoFeatureManager.getTraceMonitor(getPswFeatureBrightness(vars));
            case 29:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getOppoUsbDeviceManagerFeature(vars));
            case 30:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getOppoVibratorFeature(vars));
            case 31:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getOppoStorageManagerFeature(vars));
            case 32:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getPswAlarmManagerFeature(vars));
            case 33:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getPswNewNetworkTimeUpdateServiceFeature(vars));
            case 34:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getPswActivityManagerDynamicLogConfigFeature(vars));
            case 35:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getPswOppoArmyControllerFeatrue(vars));
            case 36:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getPswOppoArmyServiceFeatrue(vars));
            case 37:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getPswOppoAmsUtilsFeatrue(vars));
            case 38:
                Slog.i(TAG, "get feature:" + def.index().name());
                return (T) OppoFeatureManager.getTraceMonitor(getOppoScreenModeManagerFeature(vars));
            default:
                Slog.i(TAG, "Unknow feature:" + def.index().name());
                return def;
        }
    }

    /* renamed from: com.android.server.PswServiceFactoryImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$common$OppoFeatureList$OppoIndex = new int[OppoFeatureList.OppoIndex.values().length];

        static {
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswSystemServerEx.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswActivityManagerServiceEx.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswActivityTaskManagerServiceEx.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswWindowManagerServiceEx.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswPackageManagerServiceEx.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswPowerManagerServiceEx.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswLocationBlacklistUtil.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswNavigationStatusController.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswOppoGnssWhiteListProxy.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswNlpProxy.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswSuplController.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswGnssDiagnosticTool.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswLbsCustomize.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswLbsRepairer.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswLbsRomUpdateUtil.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswGnssDuration.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswFastNetworkLocation.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswCoarseToFine.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswLocationStatistics.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswLocationStatusMonitor.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswFeatureAOD.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoGatewayState.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoIPConflictDetector.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOPPODnsSelfrecoveryEngine.ordinal()] = 24;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswShutdownFeature.ordinal()] = 25;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoBrightness.ordinal()] = 26;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswFeatureReduceBrightness.ordinal()] = 27;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswFeatureBrightness.ordinal()] = 28;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoUsbDeviceFeature.ordinal()] = 29;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoVibratorFeature.ordinal()] = 30;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoStorageManagerFeature.ordinal()] = 31;
            } catch (NoSuchFieldError e31) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswAlarmManagerFeature.ordinal()] = 32;
            } catch (NoSuchFieldError e32) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswNewNetworkTimeUpdateServiceFeature.ordinal()] = 33;
            } catch (NoSuchFieldError e33) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswActivityManagerDynamicLogConfigFeature.ordinal()] = 34;
            } catch (NoSuchFieldError e34) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswOppoArmyControllerFeatrue.ordinal()] = 35;
            } catch (NoSuchFieldError e35) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswOppoArmyServiceFeatrue.ordinal()] = 36;
            } catch (NoSuchFieldError e36) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IPswOppoAmsUtilsFeatrue.ordinal()] = 37;
            } catch (NoSuchFieldError e37) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoScreenModeManagerFeature.ordinal()] = 38;
            } catch (NoSuchFieldError e38) {
            }
        }
    }

    private IPswSystemServerEx getPswSystemServerEx(Object... vars) {
        verityParamsType("getPswSystemServerEx", vars, 1, new Class[]{Context.class});
        return new PswSystemServerEx((Context) vars[0]);
    }

    private IPswActivityManagerServiceEx getPswActivityManagerServiceEx(Object... vars) {
        verityParamsType("getPswActivityManagerServiceEx", vars, 2, new Class[]{Context.class, ActivityManagerService.class});
        Context context = (Context) vars[0];
        ActivityManagerService ams = (ActivityManagerService) vars[1];
        Slog.i(TAG, "getColorActivityManagerServiceEx size = " + vars.length + " context = " + context + " ams = " + ams);
        return new PswActivityManagerServiceEx(context, ams);
    }

    private IPswActivityTaskManagerServiceEx getPswActivityTaskManagerServiceEx(Object... vars) {
        verityParamsType("getPswActivityTaskManagerServiceEx", vars, 2, new Class[]{Context.class, ActivityTaskManagerService.class});
        return new PswActivityTaskManagerServiceEx((Context) vars[0], (ActivityTaskManagerService) vars[1]);
    }

    private IPswWindowManagerServiceEx getPswWindowManagerService(Object... vars) {
        verityParamsType("getPswWindowManagerService", vars, 2, new Class[]{Context.class, WindowManagerService.class});
        return new PswWindowManagerServiceEx((Context) vars[0], (WindowManagerService) vars[1]);
    }

    private IPswPackageManagerServiceEx getPswPackageManagerService(Object... vars) {
        verityParamsType("getPswPackageManagerService", vars, 2, new Class[]{Context.class, PackageManagerService.class});
        return new PswPackageManagerServiceEx((Context) vars[0], (PackageManagerService) vars[1]);
    }

    private IPswPowerManagerServiceEx getPswPowerManagerService(Object... vars) {
        verityParamsType("getPswPowerManagerService", vars, 2, new Class[]{Context.class, PowerManagerService.class});
        return new PswPowerManagerServiceEx((Context) vars[0], (PowerManagerService) vars[1]);
    }

    private IPswLocationBlacklistUtil getOppoLocationBlacklistUtil(Object... vars) {
        verityParamsType("getOppoLocationBlacklistUtil", vars, 0, new Class[0]);
        return OppoLocationBlacklistUtil.getInstance();
    }

    private IPswNavigationStatusController getNavigationStatusController(Object... vars) {
        verityParamsType("getNavigationStatusController", vars, 2, new Class[]{Context.class, GnssLocationProvider.class});
        return NavigationStatusController.getInstance((Context) vars[0], (GnssLocationProvider) vars[1]);
    }

    private IPswOppoGnssWhiteListProxy getOppoGnssWhiteListProxy(Object... vars) {
        verityParamsType("getOppoGnssWhiteListProxy", vars, 1, new Class[]{Context.class});
        return OppoGnssWhiteListProxy.getInstall((Context) vars[0]);
    }

    private IPswNlpProxy getOppoNlpProxy(Object... vars) {
        verityParamsType("getOppoNlpProxy", vars, 2, new Class[]{Context.class, AbstractLocationProvider.LocationProviderManager.class});
        return new OppoNlpProxy((Context) vars[0], (AbstractLocationProvider.LocationProviderManager) vars[1]);
    }

    private IPswSuplController getOppoSuplController(Object... vars) {
        verityParamsType("getOppoSuplController", vars, 1, new Class[]{Context.class});
        return OppoSuplController.getInstaller((Context) vars[0]);
    }

    private IPswGnssDiagnosticTool getGnssDiagnosticTool(Object... vars) {
        verityParamsType("getGnssDiagnosticTool", vars, 1, new Class[]{Context.class});
        return OppoGnssDiagnosticTool.getInstall((Context) vars[0]);
    }

    private IPswLbsCustomize getOppoLbsCustomize(Object... vars) {
        verityParamsType("getOppoLbsCustomize", vars, 1, new Class[]{Context.class});
        return OppoLbsCustomize.getInstall((Context) vars[0]);
    }

    private IPswLbsRepairer getOppoLbsRepairer(Object... vars) {
        verityParamsType("getOppoLbsRepairer", vars, 1, new Class[]{Context.class});
        return OppoLbsRepairer.getInstance((Context) vars[0]);
    }

    private IPswLbsRomUpdateUtil getOppoLbsRomUpdateUtil(Object... vars) {
        verityParamsType("getOppoLbsRomUpdateUtil", vars, 1, new Class[]{Context.class});
        return OppoLbsRomUpdateUtil.getInstall((Context) vars[0]);
    }

    private IPswGnssDuration getOppoGnssDuration(Object... vars) {
        verityParamsType("getOppoGnssDuration", vars, 1, new Class[]{Context.class});
        return new OppoGnssDuration((Context) vars[0]);
    }

    private IPswFastNetworkLocation getFastNetworkLocation(Object... vars) {
        verityParamsType("getFastNetworkLocation", vars, 2, new Class[]{Context.class, Looper.class});
        return new FastNetworkLocation((Context) vars[0], (Looper) vars[1]);
    }

    private IPswCoarseToFine getOppoCoarseToFine(Object... vars) {
        verityParamsType("getOppoCoarseToFine", vars, 1, new Class[]{Context.class});
        return OppoCoarseToFine.getInstall((Context) vars[0]);
    }

    private IPswLocationStatistics getOppoLocationStatistics(Object... vars) {
        verityParamsType("getOppoLocationStatistics", vars, 0, new Class[0]);
        return OppoLocationStatistics.getInstance();
    }

    private IPswLocationStatusMonitor getOppoLocationStatusMonitor(Object... vars) {
        verityParamsType("getOppoLocationStatusMonitor", vars, 0, new Class[0]);
        return OppoLocationStatusMonitor.getInstance();
    }

    private IPswFeatureAOD getPswFeatureAOD(Object... vars) {
        return PswFeatureAOD.getInstance(vars);
    }

    private IOppoGatewayState getOppoGatewayState(Object... vars) {
        verityParamsType("getOppoGatewayState", vars, 2, new Class[]{Context.class, NetworkAgentInfo.class});
        return new OppoGatewayState((Context) vars[0], (NetworkAgentInfo) vars[1]);
    }

    private IOppoArpPeer getOppoIPConflictDetector(Object... vars) {
        return new OppoIPConflictDetector((Context) vars[0], (NetworkAgentInfo) vars[1], (IOppoArpPeer.ArpPeerChangeCallback) vars[2]);
    }

    private IOPPODnsSelfrecoveryEngine getOPPODnsSelfrecoveryEngine(Object... vars) {
        verityParamsType("getOPPODnsSelfrecoveryEngine", vars, 2, new Class[]{Context.class, DnsManager.class});
        return new OPPODnsSelfrecoveryEngine((Context) vars[0], (DnsManager) vars[1]);
    }

    private IPswShutdownFeature getPswShutdownFeature(Object... vars) {
        verityParamsType("getPswShutdownFeature", vars, 1, new Class[]{Context.class});
        return PswShutdownFeature.getInstance((Context) vars[0]);
    }

    private IOppoBrightness getOppoBrightness(Object... vars) {
        return createOppoBrightness();
    }

    private IOppoBrightness createOppoBrightness() {
        try {
            return (IOppoBrightness) Class.forName("com.android.server.display.OppoBrightUtils").getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return IOppoBrightness.DEFAULT;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return IOppoBrightness.DEFAULT;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return IOppoBrightness.DEFAULT;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return IOppoBrightness.DEFAULT;
        }
    }

    private IPswFeatureReduceBrightness getPswFeatureReduceBrightness(Object... vars) {
        Slog.i(TAG, "vars is :" + vars.length);
        if (vars.length != 0) {
            return PswFeatureReduceBrightness.getInstance(vars);
        }
        try {
            return (IPswFeatureReduceBrightness) Class.forName("com.android.server.display.PswFeatureReduceBrightness").getDeclaredMethod("getMethod", new Class[0]).invoke(null, new Object[0]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return IPswFeatureReduceBrightness.DEFAULT;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return IPswFeatureReduceBrightness.DEFAULT;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return IPswFeatureReduceBrightness.DEFAULT;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return IPswFeatureReduceBrightness.DEFAULT;
        }
    }

    private IPswFeatureBrightness getPswFeatureBrightness(Object... vars) {
        return PswFeatureBrightness.getInstance(vars);
    }

    private IOppoUsbDeviceFeature getOppoUsbDeviceManagerFeature(Object... vars) {
        return OppoUsbDeviceFeature.getInstance();
    }

    private IPswNewNetworkTimeUpdateServiceFeature getPswNewNetworkTimeUpdateServiceFeature(Object... vars) {
        verityParamsType("getPswShutdownFeature", vars, 1, new Class[]{Context.class});
        return PswNewNetworkTimeUpdateServiceFeature.getInstance((Context) vars[0]);
    }

    private IOppoVibratorFeature getOppoVibratorFeature(Object... vars) {
        verityParamsType("getPswShutdownFeature", vars, 1, new Class[]{Context.class});
        return OppoVibratorFeature.getInstance((Context) vars[0]);
    }

    private IOppoStorageManagerFeature getOppoStorageManagerFeature(Object... vars) {
        verityParamsType("getOppoStorageManagerFeature", vars, 1, new Class[]{Context.class});
        return OppoStorageManagerFeature.getInstance((Context) vars[0]);
    }

    private IPswAlarmManagerFeature getPswAlarmManagerFeature(Object... vars) {
        verityParamsType("getPswAlarmManagerFeature", vars, 1, new Class[]{Context.class});
        return PswAlarmManagerFeature.getInstance((Context) vars[0]);
    }

    private IPswActivityManagerDynamicLogConfigFeature getPswActivityManagerDynamicLogConfigFeature(Object... vars) {
        return PswActivityManagerDynamicLogConfigFeature.getInstance();
    }

    private IPswOppoArmyControllerFeatrue getPswOppoArmyControllerFeatrue(Object... vars) {
        return PswOppoArmyControllerFeatrue.getInstance();
    }

    private IPswOppoArmyServiceFeatrue getPswOppoArmyServiceFeatrue(Object... vars) {
        return PswOppoArmyServiceFeatrue.getInstance();
    }

    private IPswOppoAmsUtilsFeatrue getPswOppoAmsUtilsFeatrue(Object... vars) {
        return PswOppoAmsUtilsFeatrue.getInstance();
    }

    private IOppoScreenModeManagerFeature getOppoScreenModeManagerFeature(Object... vars) {
        return OppoScreenModeManagerFeature.getInstance();
    }
}
