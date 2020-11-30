package com.android.server.am;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ColorBroadcastStaticRegisterWhitelistManager implements IColorBroadcastStaticRegisterWhitelistManager {
    public static final String TAG = "ColorBroadcastStaticRegisterWhitelistManager";
    private static final ArrayList<String> sBroadcastList = new ArrayList<>();
    private static ColorBroadcastStaticRegisterWhitelistManager sColorBroadcastStaticRegisterWhitelistManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    boolean mDynamicDebug = false;

    static {
        sBroadcastList.add("android.net.conn.CONNECTIVITY_CHANGE");
    }

    public static ColorBroadcastStaticRegisterWhitelistManager getInstance() {
        if (sColorBroadcastStaticRegisterWhitelistManager == null) {
            sColorBroadcastStaticRegisterWhitelistManager = new ColorBroadcastStaticRegisterWhitelistManager();
        }
        return sColorBroadcastStaticRegisterWhitelistManager;
    }

    private ColorBroadcastStaticRegisterWhitelistManager() {
    }

    public void init() {
        registerLogModule();
    }

    public boolean isSkipThisStaticBroadcastReceivers(Intent intent, ResolveInfo info) {
        String dstPkg;
        if (intent == null || info == null) {
            Slog.v(TAG, "the intent or info is null, skip!");
            return true;
        }
        String action = intent.getAction();
        if (action == null) {
            Slog.v(TAG, "the action of intent is null, skip!");
            return true;
        } else if (!sBroadcastList.contains(action) || (dstPkg = info.activityInfo.applicationInfo.packageName) == null || !OppoListManager.getInstance().getAllowManifestNetBroList().contains(dstPkg)) {
            return true;
        } else {
            return false;
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog#### mDynamicDebug = " + this.mDynamicDebug);
        setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + this.mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorBroadcastStaticRegisterWhitelistManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }
}
