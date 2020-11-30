package android.os;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.media.MediaDrm;
import android.media.midi.MidiDeviceInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.IDeviceIdentifiersPolicyService;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.telephony.TelephonyProperties;
import dalvik.system.VMRuntime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Build {
    public static final String BOARD = getString("ro.product.board");
    public static final String BOOTLOADER = getString("ro.bootloader");
    public static final String BRAND = getString("ro.product.brand");
    @Deprecated
    public static final String CPU_ABI;
    @Deprecated
    public static final String CPU_ABI2;
    public static final String DEVICE = getString("ro.product.device");
    public static final String DISPLAY = getString("ro.build.display.id");
    public static final String FINGERPRINT = deriveFingerprint();
    public static final String HARDWARE = getString("ro.hardware");
    public static final String HOST = getString("ro.build.host");
    public static final String ID = getString("ro.build.id");
    public static final boolean IS_CONTAINER = SystemProperties.getBoolean("ro.boot.container", false);
    @UnsupportedAppUsage
    public static final boolean IS_DEBUGGABLE;
    public static final boolean IS_EMULATOR = getString("ro.kernel.qemu").equals(WifiEnterpriseConfig.ENGINE_ENABLE);
    public static final boolean IS_ENG = "eng".equals(TYPE);
    public static final boolean IS_TREBLE_ENABLED = SystemProperties.getBoolean("ro.treble.enabled", false);
    public static final boolean IS_USER = "user".equals(TYPE);
    public static final boolean IS_USERDEBUG = "userdebug".equals(TYPE);
    public static final String MANUFACTURER = getString("ro.product.manufacturer");
    public static final String MODEL = getString("ro.product.model");
    @SystemApi
    public static final boolean PERMISSIONS_REVIEW_REQUIRED = true;
    public static final String PRODUCT = getString("ro.product.name");
    @Deprecated
    public static final String RADIO = getString(TelephonyProperties.PROPERTY_BASEBAND_VERSION);
    @Deprecated
    public static final String SERIAL = getString("no.such.thing");
    public static final String[] SUPPORTED_32_BIT_ABIS = getStringList("ro.product.cpu.abilist32", SmsManager.REGEX_PREFIX_DELIMITER);
    public static final String[] SUPPORTED_64_BIT_ABIS = getStringList("ro.product.cpu.abilist64", SmsManager.REGEX_PREFIX_DELIMITER);
    public static final String[] SUPPORTED_ABIS = getStringList("ro.product.cpu.abilist", SmsManager.REGEX_PREFIX_DELIMITER);
    private static final String TAG = "Build";
    public static final String TAGS = getString("ro.build.tags");
    public static final long TIME = (getLong("ro.build.date.utc") * 1000);
    public static final String TYPE = getString("ro.build.type");
    public static final String UNKNOWN = "unknown";
    public static final String USER = getString("ro.build.user");

    public static class VERSION {
        @UnsupportedAppUsage
        public static final String[] ACTIVE_CODENAMES = ("REL".equals(ALL_CODENAMES[0]) ? new String[0] : ALL_CODENAMES);
        private static final String[] ALL_CODENAMES = Build.getStringList("ro.build.version.all_codenames", SmsManager.REGEX_PREFIX_DELIMITER);
        public static final String BASE_OS = SystemProperties.get("ro.build.version.base_os", "");
        public static final String CODENAME = Build.getString("ro.build.version.codename");
        public static final int FIRST_SDK_INT = SystemProperties.getInt("ro.product.first_api_level", 0);
        public static final String INCREMENTAL = Build.getString("ro.build.version.incremental");
        public static final int MIN_SUPPORTED_TARGET_SDK_INT = SystemProperties.getInt("ro.build.version.min_supported_target_sdk", 0);
        @SystemApi
        public static final String PREVIEW_SDK_FINGERPRINT = SystemProperties.get("ro.build.version.preview_sdk_fingerprint", "REL");
        public static final int PREVIEW_SDK_INT = SystemProperties.getInt("ro.build.version.preview_sdk", 0);
        public static final String RELEASE = Build.getString("ro.build.version.release");
        public static final int RESOURCES_SDK_INT = (SDK_INT + ACTIVE_CODENAMES.length);
        @Deprecated
        public static final String SDK = Build.getString("ro.build.version.sdk");
        public static final int SDK_INT = SystemProperties.getInt("ro.build.version.sdk", 0);
        public static final String SECURITY_PATCH = SystemProperties.get("ro.build.version.security_patch", "");
    }

    public static class VERSION_CODES {
        public static final int BASE = 1;
        public static final int BASE_1_1 = 2;
        public static final int CUPCAKE = 3;
        public static final int CUR_DEVELOPMENT = 10000;
        public static final int DONUT = 4;
        public static final int ECLAIR = 5;
        public static final int ECLAIR_0_1 = 6;
        public static final int ECLAIR_MR1 = 7;
        public static final int FROYO = 8;
        public static final int GINGERBREAD = 9;
        public static final int GINGERBREAD_MR1 = 10;
        public static final int HONEYCOMB = 11;
        public static final int HONEYCOMB_MR1 = 12;
        public static final int HONEYCOMB_MR2 = 13;
        public static final int ICE_CREAM_SANDWICH = 14;
        public static final int ICE_CREAM_SANDWICH_MR1 = 15;
        public static final int JELLY_BEAN = 16;
        public static final int JELLY_BEAN_MR1 = 17;
        public static final int JELLY_BEAN_MR2 = 18;
        public static final int KITKAT = 19;
        public static final int KITKAT_WATCH = 20;
        public static final int L = 21;
        public static final int LOLLIPOP = 21;
        public static final int LOLLIPOP_MR1 = 22;
        public static final int M = 23;
        public static final int N = 24;
        public static final int N_MR1 = 25;
        public static final int O = 26;
        public static final int O_MR1 = 27;
        public static final int P = 28;
        public static final int Q = 29;
    }

    static {
        String[] abiList;
        if (VMRuntime.getRuntime().is64Bit()) {
            abiList = SUPPORTED_64_BIT_ABIS;
        } else {
            abiList = SUPPORTED_32_BIT_ABIS;
        }
        CPU_ABI = abiList[0];
        boolean z = true;
        if (abiList.length > 1) {
            CPU_ABI2 = abiList[1];
        } else {
            CPU_ABI2 = "";
        }
        if (SystemProperties.getInt("ro.debuggable", 0) != 1) {
            z = false;
        }
        IS_DEBUGGABLE = z;
    }

    public static String getSerial() {
        IDeviceIdentifiersPolicyService service = IDeviceIdentifiersPolicyService.Stub.asInterface(ServiceManager.getService(Context.DEVICE_IDENTIFIERS_SERVICE));
        try {
            Application application = ActivityThread.currentApplication();
            return service.getSerialForPackage(application != null ? application.getPackageName() : null);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return "unknown";
        }
    }

    public static boolean is64BitAbi(String abi) {
        return VMRuntime.is64BitAbi(abi);
    }

    private static String deriveFingerprint() {
        String finger = SystemProperties.get("ro.build.fingerprint");
        if (!TextUtils.isEmpty(finger)) {
            return finger;
        }
        return getString("ro.product.brand") + '/' + getString("ro.product.name") + '/' + getString("ro.product.device") + ':' + getString("ro.build.version.release") + '/' + getString("ro.build.id") + '/' + getString("ro.build.version.incremental") + ':' + getString("ro.build.type") + '/' + getString("ro.build.tags");
    }

    public static void ensureFingerprintProperty() {
        if (TextUtils.isEmpty(SystemProperties.get("ro.build.fingerprint"))) {
            try {
                SystemProperties.set("ro.build.fingerprint", FINGERPRINT);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "Failed to set fingerprint property", e);
            }
        }
    }

    public static boolean isBuildConsistent() {
        if (IS_ENG) {
            return true;
        }
        if (IS_TREBLE_ENABLED) {
            int result = VintfObject.verifyWithoutAvb();
            if (result != 0) {
                Slog.e(TAG, "Vendor interface is incompatible, error=" + String.valueOf(result));
            }
            if (result == 0) {
                return true;
            }
            return false;
        }
        String system = SystemProperties.get("ro.build.fingerprint");
        String vendor2 = SystemProperties.get("ro.vendor.build.fingerprint");
        SystemProperties.get("ro.bootimage.build.fingerprint");
        SystemProperties.get("ro.build.expect.bootloader");
        SystemProperties.get("ro.bootloader");
        SystemProperties.get("ro.build.expect.baseband");
        SystemProperties.get(TelephonyProperties.PROPERTY_BASEBAND_VERSION);
        if (TextUtils.isEmpty(system)) {
            Slog.e(TAG, "Required ro.build.fingerprint is empty!");
            return false;
        } else if (TextUtils.isEmpty(vendor2) || Objects.equals(system, vendor2)) {
            return true;
        } else {
            Slog.e(TAG, "Mismatched fingerprints; system reported " + system + " but vendor reported " + vendor2);
            return false;
        }
    }

    public static class Partition {
        public static final String PARTITION_NAME_SYSTEM = "system";
        private final String mFingerprint;
        private final String mName;
        private final long mTimeMs;

        private Partition(String name, String fingerprint, long timeMs) {
            this.mName = name;
            this.mFingerprint = fingerprint;
            this.mTimeMs = timeMs;
        }

        public String getName() {
            return this.mName;
        }

        public String getFingerprint() {
            return this.mFingerprint;
        }

        public long getBuildTimeMillis() {
            return this.mTimeMs;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Partition)) {
                return false;
            }
            Partition op = (Partition) o;
            if (!this.mName.equals(op.mName) || !this.mFingerprint.equals(op.mFingerprint) || this.mTimeMs != op.mTimeMs) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mName, this.mFingerprint, Long.valueOf(this.mTimeMs));
        }
    }

    public static List<Partition> getFingerprintedPartitions() {
        ArrayList<Partition> partitions = new ArrayList<>();
        String[] names = {"bootimage", "odm", MidiDeviceInfo.PROPERTY_PRODUCT, "product_services", "system", MediaDrm.PROPERTY_VENDOR};
        for (String name : names) {
            String fingerprint = SystemProperties.get("ro." + name + ".build.fingerprint");
            if (!TextUtils.isEmpty(fingerprint)) {
                partitions.add(new Partition(name, fingerprint, getLong("ro." + name + ".build.date.utc") * 1000));
            }
        }
        return partitions;
    }

    public static String getRadioVersion() {
        String propVal = SystemProperties.get(TelephonyProperties.PROPERTY_BASEBAND_VERSION);
        if (TextUtils.isEmpty(propVal)) {
            return null;
        }
        return propVal;
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public static String getString(String property) {
        return SystemProperties.get(property, "unknown");
    }

    /* access modifiers changed from: private */
    public static String[] getStringList(String property, String separator) {
        String value = SystemProperties.get(property);
        if (value.isEmpty()) {
            return new String[0];
        }
        return value.split(separator);
    }

    @UnsupportedAppUsage
    private static long getLong(String property) {
        try {
            return Long.parseLong(SystemProperties.get(property));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
