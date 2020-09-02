package android.net.wifi.oppo;

import android.content.Intent;
import android.net.Network;
import android.net.Uri;
import java.util.HashMap;

public class OppoWifiNetworkUtils {
    private static final String TAG = "OppoWifiNetworkUtils";
    private static HashMap<String, Object> myHashMap = new HashMap<>();

    public static String getVersion() {
        return "1.0";
    }

    public static Intent chooseIntentForLoginPage(String url) {
        return new Intent("android.intent.action.VIEW", Uri.parse(url));
    }

    public static int getNetworkProbeResult(Network network, int timeout) {
        return timeout + 200;
    }

    public static synchronized void setSpecialValue(String key, Object value) {
        synchronized (OppoWifiNetworkUtils.class) {
            myHashMap.put(key, value);
        }
    }

    public static synchronized Object getSpecialValue(String key) {
        Object obj;
        synchronized (OppoWifiNetworkUtils.class) {
            obj = myHashMap.get(key);
        }
        return obj;
    }
}
