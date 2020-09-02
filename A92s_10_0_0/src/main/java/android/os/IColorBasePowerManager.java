package android.os;

import com.color.util.IColorBaseServiceManager;

public interface IColorBasePowerManager extends IColorBaseServiceManager {
    public static final String DESCRIPTOR = "android.os.IPowerManager";
    public static final int TRANSACTION_COMMON = 10001;
}
