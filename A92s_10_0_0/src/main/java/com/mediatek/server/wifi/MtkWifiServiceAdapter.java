package com.mediatek.server.wifi;

import android.content.Context;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.State;
import com.android.server.wifi.ScanDetail;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class MtkWifiServiceAdapter {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "MtkWifiServiceAdapter";
    private static IMtkWifiService sMWS;

    public interface IMtkWifiService {
        void handleScanResults(List<ScanDetail> list, List<ScanDetail> list2);

        void initialize();

        boolean needCustomEvaluator();

        boolean postProcessMessage(State state, Message message, Object... objArr);

        boolean preProcessMessage(State state, Message message);

        void triggerNetworkEvaluatorCallBack();

        void updateRSSI(Integer num, int i, int i2);
    }

    public static void initialize(Context context) {
        log("[initialize]: " + context);
        try {
            sMWS = (IMtkWifiService) Class.forName("com.mediatek.server.wifi.MtkWifiService", false, new PathClassLoader("/system/framework/mtk-wifi-service.jar", MtkWifiServiceAdapter.class.getClassLoader())).getConstructor(Context.class).newInstance(context);
            sMWS.initialize();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e1) {
            throw new Error(e1);
        } catch (ClassNotFoundException e2) {
            log("No extension found");
            e2.printStackTrace();
        }
    }

    public static void log(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void handleScanResults(List<ScanDetail> full, List<ScanDetail> unsaved) {
        IMtkWifiService iMtkWifiService = sMWS;
        if (iMtkWifiService != null) {
            iMtkWifiService.handleScanResults(full, unsaved);
        }
    }

    public static void updateRSSI(Integer newRssi, int ipAddr, int lastNetworkId) {
        IMtkWifiService iMtkWifiService = sMWS;
        if (iMtkWifiService != null) {
            iMtkWifiService.updateRSSI(newRssi, ipAddr, lastNetworkId);
        }
    }

    public static boolean preProcessMessage(State state, Message msg) {
        IMtkWifiService iMtkWifiService = sMWS;
        if (iMtkWifiService != null) {
            return iMtkWifiService.preProcessMessage(state, msg);
        }
        return false;
    }

    public static boolean postProcessMessage(State state, Message msg, Object... args) {
        IMtkWifiService iMtkWifiService = sMWS;
        if (iMtkWifiService != null) {
            return iMtkWifiService.postProcessMessage(state, msg, args);
        }
        return false;
    }

    public static void triggerNetworkEvaluatorCallBack() {
        IMtkWifiService iMtkWifiService = sMWS;
        if (iMtkWifiService != null) {
            iMtkWifiService.triggerNetworkEvaluatorCallBack();
        }
    }

    public static boolean needCustomEvaluator() {
        String operator = SystemProperties.get("ro.vendor.operator.optr", "");
        if (operator == null || !operator.equalsIgnoreCase("OP01")) {
            return false;
        }
        Log.i(TAG, "[needCustomEvaluator] true for OP01");
        return true;
    }
}
