package android.app;

import android.content.pm.ApplicationInfo;
import android.content.pm.OppoMirrorApplicationInfo;
import android.util.Slog;
import com.android.internal.logging.nano.MetricsProto;
import dalvik.system.VMRuntime;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;

public class OppoLoadedApkHelper {
    public static void addSpecialLibraries(ApplicationInfo aInfo, List<String> outLibPaths) {
        int len;
        String dirPrefix;
        if (aInfo != null) {
            Object libDirsObj = null;
            try {
                Field field = Class.forName("android.content.pm.ApplicationInfo").getDeclaredField("specialNativeLibraryDirs");
                field.setAccessible(true);
                libDirsObj = field.get(aInfo);
            } catch (Exception e) {
                Slog.e("OppoLoadedApkHelper", "addSpecialLibraries failed for get specialNativeLibraryDirs!", e);
            }
            if (libDirsObj != null && libDirsObj.getClass().isArray() && (len = Array.getLength(libDirsObj)) > 0) {
                if (VMRuntime.getRuntime().is64Bit()) {
                    dirPrefix = "/system/lib64/";
                } else {
                    dirPrefix = "/system/lib/";
                }
                for (int index = 0; index < len; index++) {
                    Object item = Array.get(libDirsObj, index);
                    if (item != null && (item instanceof String)) {
                        String itemValue = (String) item;
                        if (!outLibPaths.contains(itemValue)) {
                            outLibPaths.add(0, dirPrefix + itemValue);
                        }
                    }
                }
            }
            int oppoPrivateFlagsValue = 0;
            if (OppoMirrorApplicationInfo.oppoPrivateFlags != null) {
                oppoPrivateFlagsValue = OppoMirrorApplicationInfo.oppoPrivateFlags.get(aInfo);
            }
            if ((oppoPrivateFlagsValue & 4) != 0) {
                outLibPaths.add(System.getProperty("java.library.path"));
            }
        }
    }

    public static void addSpecialZipPaths(ApplicationInfo aInfo, List<String> outZipPaths) {
        if (aInfo != null && aInfo.processName != null && OppoMirrorActivityThread.inCptWhiteList != null) {
            if (OppoMirrorActivityThread.inCptWhiteList.call(null, Integer.valueOf((int) MetricsProto.MetricsEvent.ACTION_PERMISSION_REQUEST_READ_SMS), aInfo.processName).booleanValue()) {
                outZipPaths.add("/system/framework/org.apache.http.legacy.boot.jar");
            }
        }
    }
}
