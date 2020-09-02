package oppo.net.wifi;

import android.net.wifi.WifiEnterpriseConfig;
import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorWifiEnterpriseConfig {
    public static Class<?> TYPE = RefClass.load(OppoMirrorWifiEnterpriseConfig.class, WifiEnterpriseConfig.class);
    public static RefMethod<String> getSimNum;
    @MethodParams({int.class})
    public static RefMethod<Void> setSimNum;
}
