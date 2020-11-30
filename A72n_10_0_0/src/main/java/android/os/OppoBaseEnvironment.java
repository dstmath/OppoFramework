package android.os;

import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class OppoBaseEnvironment {
    private static final String DEVICE_INFO_PATH = "/proc/devinfo/emmc";
    private static final String DEVICE_MANUFACTURE = "manufacture";
    private static final String DEVICE_MANUFACTURE_HYNIX = "HYNIX";
    private static final String DEVICE_MANUFACTURE_MICRON = "MICRON";
    private static final String DEVICE_MANUFACTURE_SANDISK = "SANDISK";
    private static final String DEVICE_VERSION = "version";
    private static final String DEVICE_VERSION_MICRON = "S0J9F8";
    private static File DIR_OPPO_COTA_ROOT = null;
    private static File DIR_OPPO_CUSTOM_ROOT = null;
    private static File DIR_OPPO_ENGINEER_ROOT = null;
    private static File DIR_OPPO_PRODUCT_ROOT = null;
    private static final String DIR_OPPO_RESERVE = "/mnt/vendor/opporeserve";
    private static File DIR_OPPO_VERSION_ROOT = null;
    private static final String ENV_OPPO_COTA_ROOT = "OPPO_COTA_ROOT";
    private static final String ENV_OPPO_CUSTOM_ROOT = "OPPO_CUSTOM_ROOT";
    private static final String ENV_OPPO_ENGINEER_ROOT = "OPPO_ENGINEER_ROOT";
    private static final String ENV_OPPO_PRODUCT_ROOT = "OPPO_PRODUCT_ROOT";
    private static final String ENV_OPPO_VERSION_ROOT = "OPPO_VERSION_ROOT";
    public static final boolean NOT_ALLOW_EXT4_ACCESS = true;
    private static final String TAG = "OppoBaseEnvironment";

    public static File getReserveDirectory() {
        return new File(DIR_OPPO_RESERVE);
    }

    private static Map<String, String> getDeviceInfo() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(DEVICE_INFO_PATH), 256);
            Map<String, String> results = new HashMap<>();
            while (true) {
                String str = bufferedReader.readLine();
                if (str == null) {
                    bufferedReader.close();
                    Log.d(TAG, "getDeviceInfo,map=" + results.toString());
                    return results;
                } else if (str.contains(DEVICE_MANUFACTURE)) {
                    results.put(DEVICE_MANUFACTURE, str.split("\\s+")[2]);
                } else if (str.contains("version")) {
                    results.put("version", str.split("\\s+")[2]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isWhiteListMcp() {
        Map<String, String> results = getDeviceInfo();
        if (results == null) {
            Log.d(TAG, "isWhiteListMcp,getDeviceInfo result is null,return false");
            return false;
        }
        String deviceVersion = null;
        String deviceManufacture = null;
        for (Map.Entry<String, String> entry : results.entrySet()) {
            String key = entry.getKey();
            if (key.equals(DEVICE_MANUFACTURE)) {
                deviceManufacture = entry.getValue();
            } else if (key.equals("version")) {
                deviceVersion = entry.getValue();
            }
        }
        if (deviceVersion == null || deviceManufacture == null || (!deviceManufacture.equalsIgnoreCase(DEVICE_MANUFACTURE_HYNIX) && !deviceManufacture.equalsIgnoreCase(DEVICE_MANUFACTURE_MICRON) && !deviceManufacture.equalsIgnoreCase(DEVICE_MANUFACTURE_SANDISK))) {
            return false;
        }
        return true;
    }

    protected static File getDirectorySup(String variableName, String defaultPath) {
        File file;
        String path = System.getenv(variableName);
        if (path != null) {
            file = new File(path);
        }
        return file;
    }

    public static File getOppoCustomDirectory() {
        if (DIR_OPPO_CUSTOM_ROOT == null) {
            DIR_OPPO_CUSTOM_ROOT = getDirectorySup(ENV_OPPO_CUSTOM_ROOT, "/my_custom");
        }
        return DIR_OPPO_CUSTOM_ROOT;
    }

    public static File getOppoCotaDirectory() {
        if (DIR_OPPO_COTA_ROOT == null) {
            DIR_OPPO_COTA_ROOT = getDirectorySup(ENV_OPPO_COTA_ROOT, "/my_cota");
        }
        return DIR_OPPO_COTA_ROOT;
    }

    public static File getResourceDirectory() {
        if (SystemProperties.get("sys.cotaimg.verify", WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_ENABLE)) {
            return getOppoCotaDirectory();
        }
        return getOppoCustomDirectory();
    }

    public static File getOppoProductDirectory() {
        if (DIR_OPPO_PRODUCT_ROOT == null) {
            DIR_OPPO_PRODUCT_ROOT = getDirectorySup(ENV_OPPO_PRODUCT_ROOT, "/oppo_product");
        }
        return DIR_OPPO_PRODUCT_ROOT;
    }

    public static File getOppoEngineerDirectory() {
        if (DIR_OPPO_ENGINEER_ROOT == null) {
            DIR_OPPO_ENGINEER_ROOT = getDirectorySup(ENV_OPPO_ENGINEER_ROOT, "/oppo_engineering");
        }
        return DIR_OPPO_ENGINEER_ROOT;
    }

    public static File getOppoVersionDirectory() {
        if (DIR_OPPO_VERSION_ROOT == null) {
            DIR_OPPO_VERSION_ROOT = getDirectorySup(ENV_OPPO_VERSION_ROOT, "/oppo_version");
        }
        return DIR_OPPO_VERSION_ROOT;
    }
}
