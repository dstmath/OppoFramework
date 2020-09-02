package com.android.server;

import android.common.OppoFeatureList;
import android.content.Context;
import android.util.Slog;
import com.android.server.display.ColorDummyDisplayManagerServiceEx;
import com.android.server.display.ColorDummyEyeProtectManager;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.IColorDisplayManagerServiceEx;
import com.android.server.display.IColorEyeProtectManager;
import com.android.server.inputmethod.ColorDummyInputMethodManagerServiceEx;
import com.android.server.inputmethod.IColorInputMethodManagerServiceEx;
import com.android.server.inputmethod.InputMethodManagerService;
import com.android.server.job.ColorDummyJobSchedulerServiceEx;
import com.android.server.job.IColorJobSchedulerServiceEx;
import com.android.server.job.JobSchedulerService;
import com.android.server.locksettings.LockSettingsService;
import com.android.server.net.ColorDummyNetworkPolicyManagerServiceEx;
import com.android.server.net.IColorNetworkPolicyManagerServiceEx;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.notification.ColorDummyNotificationManagerServiceEx;
import com.android.server.notification.IColorNotificationManagerServiceEx;
import com.android.server.notification.NotificationManagerService;
import com.android.server.om.ColorDummyOverlayManagerServiceEx;
import com.android.server.om.IColorOverlayManagerServiceEx;
import com.android.server.power.IColorPowerManagerServiceEx;
import com.android.server.power.PowerManagerService;
import java.lang.reflect.InvocationTargetException;

public class ColorServiceFactory extends OppoCommonServiceFactory {
    private static final String CLASSNAME = "com.android.server.ColorServiceFactoryImpl";
    private static final String TAG = "ColorServiceFactory";
    private static ColorServiceFactory sInstance;

    public static ColorServiceFactory getInstance() {
        if (sInstance == null) {
            synchronized (ColorServiceFactory.class) {
                try {
                    if (sInstance == null) {
                        sInstance = (ColorServiceFactory) newInstance(CLASSNAME);
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "WindowManagerService Reflect exception getInstance: " + e.toString());
                    if (sInstance == null) {
                        sInstance = new ColorServiceFactory();
                    }
                }
            }
        }
        return sInstance;
    }

    public boolean isValid(int index) {
        return index < OppoFeatureList.OppoIndex.EndColorServiceFactory.ordinal() && index > OppoFeatureList.OppoIndex.StartColorServiceFactory.ordinal();
    }

    public IColorAlarmManagerServiceEx getColorAlarmManagerServiceEx(Context context, AlarmManagerService ams) {
        warn("getColorAlarmManagerServiceEx dummy");
        return new ColorDummyAlarmManagerServiceEx(context, ams);
    }

    public IColorDeviceIdleControllerEx getColorDeviceIdleControllerEx(Context context, DeviceIdleController dic) {
        warn("getColorDeviceIdleControllerEx dummy");
        return new ColorDummyDeviceIdleControllerEx(context, dic);
    }

    public IColorJobSchedulerServiceEx getColorJobSchedulerServiceEx(Context context, JobSchedulerService jss) {
        warn("getColorJobSchedulerServiceEx dummy");
        return new ColorDummyJobSchedulerServiceEx(context, jss);
    }

    public IColorNotificationManagerServiceEx getColorNotificationManagerServiceEx(Context context, NotificationManagerService nms) {
        warn("getColorNotificationManagerServiceEx dummy");
        return new ColorDummyNotificationManagerServiceEx(context, nms);
    }

    public int getColorSystemThemeEx(int theme) {
        warn("getColorSystemThemeEx dummy");
        return theme;
    }

    public IColorMasterClearEx getColorMasterClearEx(Context context) {
        warn("getColorMasterClearEx dummy");
        return new ColorDummyMasterClearEx(context);
    }

    public IColorOverlayManagerServiceEx getColorOverlayManagerServiceEx(Context context) {
        warn("getColorOverylayManagerServiceEx dummy");
        return new ColorDummyOverlayManagerServiceEx(context);
    }

    public IColorInputMethodManagerServiceEx getColorInputMethodManagerServiceEx(Context context, InputMethodManagerService imms) {
        warn("getColorInputMethodManagerServiceEx dummy");
        return new ColorDummyInputMethodManagerServiceEx(context, imms);
    }

    public InputMethodManagerService getColorInputMethodManagerService(Context context) {
        warn("getInputMethodManagerService");
        return new InputMethodManagerService(context);
    }

    public IColorEyeProtectManager getColorEyeProtectManager() {
        return ColorDummyEyeProtectManager.getInstance();
    }

    public LockSettingsService getLockSettingsService(Context context) {
        warn("getLockSettingsService");
        return new LockSettingsService(context);
    }

    public IColorDisplayManagerServiceEx getColorDisplayManagerServiceEx(Context context, DisplayManagerService dms) {
        warn("getColorDisplayManagerServiceEx dummy");
        return new ColorDummyDisplayManagerServiceEx(context, dms);
    }

    private static IColorPowerManagerServiceEx createDummyPowerManagerServiceEx(Context context, PowerManagerService pms) {
        try {
            return (IColorPowerManagerServiceEx) Class.forName("com.android.server.power.ColorDummyPowerManagerServiceEx").getDeclaredConstructor(Context.class, PowerManagerService.class).newInstance(context, pms);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InstantiationException e4) {
            e4.printStackTrace();
            return null;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            return null;
        }
    }

    public IColorNetworkPolicyManagerServiceEx getColorNetworkPolicyManagerServiceEx(Context context, NetworkPolicyManagerService nms) {
        warn("getColorNetworkPolicyManagerServiceEx dummy");
        return new ColorDummyNetworkPolicyManagerServiceEx(context, nms);
    }
}
