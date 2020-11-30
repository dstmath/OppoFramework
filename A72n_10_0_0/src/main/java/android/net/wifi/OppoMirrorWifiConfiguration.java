package android.net.wifi;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefInt;
import com.oppo.reflect.RefObject;

public class OppoMirrorWifiConfiguration {
    public static Class<?> TYPE = RefClass.load(OppoMirrorWifiConfiguration.class, WifiConfiguration.class);
    public static RefObject<String> wapiCertSel;
    public static RefInt wapiCertSelMode;
    public static RefObject<String> wapiPsk;
    public static RefInt wapiPskType;
}
