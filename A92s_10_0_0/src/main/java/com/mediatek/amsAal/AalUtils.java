package com.mediatek.amsAal;

import android.content.ComponentName;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class AalUtils {
    private static final int AAL_DEFAULT_LEVEL = 128;
    private static final int AAL_MAX_LEVEL = 256;
    private static final int AAL_MIN_LEVEL = 0;
    public static final int AAL_MODE_BALANCE = 1;
    public static final int AAL_MODE_LOWPOWER = 2;
    public static final int AAL_MODE_PERFORMANCE = 0;
    public static final int AAL_MODE_SIZE = 3;
    private static final int AAL_NULL_LEVEL = -1;
    private static final String TAG = "AalUtils";
    private static String sAalConfigXMLPath = "/system/etc/ams_aal_config.xml";
    private static boolean sDebug = false;
    private static boolean sEnabled;
    private static AalUtils sInstance = null;
    private static boolean sIsAalSupported = SystemProperties.get("ro.vendor.mtk_aal_support").equals("1");
    private int mAalMode = 1;
    private Map<AalIndex, Integer> mConfig = new HashMap();
    private AalConfig mCurrentConfig = null;

    private native void setSmartBacklightStrength(int i);

    static {
        boolean z = false;
        if (sIsAalSupported && SystemProperties.get("persist.vendor.sys.mtk_app_aal_support").equals("1")) {
            z = true;
        }
        sEnabled = z;
    }

    AalUtils() {
        if (sIsAalSupported) {
            try {
                parseXML();
            } catch (XmlPullParserException e) {
                Slog.d(TAG, "XmlPullParserException fail to parseXML, " + e);
            } catch (IOException e2) {
                Slog.d(TAG, "IOException fail to parseXML, " + e2);
            } catch (Exception e3) {
                Slog.d(TAG, "fail to parseXML, " + e3);
            }
        } else if (sDebug) {
            Slog.d(TAG, "AAL is not supported");
        }
    }

    public static boolean isSupported() {
        if (sDebug) {
            Slog.d(TAG, "isSupported = " + sIsAalSupported);
        }
        return sIsAalSupported;
    }

    public static AalUtils getInstance() {
        if (sInstance == null) {
            sInstance = new AalUtils();
        }
        return sInstance;
    }

    public void setAalMode(int mode) {
        if (sIsAalSupported) {
            setAalModeInternal(mode);
        } else if (sDebug) {
            Slog.d(TAG, "AAL is not supported");
        }
    }

    public void setEnabled(boolean enabled) {
        if (sIsAalSupported) {
            setEnabledInternal(enabled);
        } else if (sDebug) {
            Slog.d(TAG, "AAL is not supported");
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005e, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0011, code lost:
        return "AAL is not enabled";
     */
    public synchronized String setAalModeInternal(int mode) {
        String msg;
        if (sEnabled) {
            if (mode < 0 || mode >= 3) {
                msg = "unknown mode " + mode;
            } else {
                this.mAalMode = mode;
                msg = "setAalModeInternal " + this.mAalMode + "(" + modeToString(this.mAalMode) + ")";
            }
            if (sDebug) {
                Slog.d(TAG, msg);
            }
        } else if (sDebug) {
            Slog.d(TAG, "AAL is not enabled");
        }
    }

    public synchronized void setEnabledInternal(boolean enabled) {
        sEnabled = enabled;
        if (!sEnabled) {
            setDefaultSmartBacklightInternal("disabled");
            SystemProperties.set("persist.vendor.sys.mtk_app_aal_support", "0");
        } else {
            SystemProperties.set("persist.vendor.sys.mtk_app_aal_support", "1");
        }
        if (sDebug) {
            Slog.d(TAG, "setEnabledInternal(" + sEnabled + ")");
        }
    }

    public synchronized void setSmartBacklightInternal(ComponentName name) {
        setSmartBacklightInternal(name, this.mAalMode);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b9, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d5, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0011, code lost:
        return;
     */
    public synchronized void setSmartBacklightInternal(ComponentName name, int mode) {
        if (!sEnabled) {
            if (sDebug) {
                Slog.d(TAG, "AAL is not enabled");
            }
        } else if (mode >= 0 && mode < 3) {
            if (this.mCurrentConfig == null) {
                if (sDebug) {
                    Slog.d(TAG, "mCurrentConfig == null");
                }
                this.mCurrentConfig = new AalConfig(null, AAL_DEFAULT_LEVEL);
            }
            AalIndex index = new AalIndex(mode, name.flattenToShortString());
            AalConfig config = getAalConfig(index);
            if (AAL_NULL_LEVEL == config.mLevel) {
                index = new AalIndex(mode, name.getPackageName());
                config = getAalConfig(index);
                if (AAL_NULL_LEVEL == config.mLevel) {
                    config.mLevel = AAL_DEFAULT_LEVEL;
                }
            }
            int validLevel = ensureBacklightLevel(config.mLevel);
            if (sDebug) {
                Slog.d(TAG, "setSmartBacklight current level: " + this.mCurrentConfig.mLevel + " for " + index);
            }
            if (this.mCurrentConfig.mLevel != validLevel) {
                Slog.d(TAG, "setSmartBacklightStrength(" + validLevel + ") for " + index);
                this.mCurrentConfig.mLevel = validLevel;
                this.mCurrentConfig.mName = index.getIndexName();
                setSmartBacklightStrength(validLevel);
            }
        } else if (sDebug) {
            Slog.d(TAG, "Unknown mode: " + mode);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0041, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0011, code lost:
        return;
     */
    public synchronized void setDefaultSmartBacklightInternal(String reason) {
        if (!sEnabled) {
            if (sDebug) {
                Slog.d(TAG, "AAL is not enabled");
            }
        } else if (!(this.mCurrentConfig == null || this.mCurrentConfig.mLevel == AAL_DEFAULT_LEVEL)) {
            Slog.d(TAG, "setSmartBacklightStrength(128) " + reason);
            this.mCurrentConfig.mLevel = AAL_DEFAULT_LEVEL;
            this.mCurrentConfig.mName = null;
            setSmartBacklightStrength(AAL_DEFAULT_LEVEL);
        }
    }

    public void onAfterActivityResumed(String packageName, String activityName) {
        setSmartBacklightInternal(new ComponentName(packageName, activityName));
    }

    public void onUpdateSleep(boolean wasSleeping, boolean isSleepingAfterUpdate) {
        if (sDebug) {
            Slog.d(TAG, "onUpdateSleep before=" + wasSleeping + " after=" + isSleepingAfterUpdate);
        }
        if (wasSleeping != isSleepingAfterUpdate && isSleepingAfterUpdate) {
            setDefaultSmartBacklightInternal("for sleep");
        }
    }

    private int ensureBacklightLevel(int level) {
        if (level < 0) {
            if (!sDebug) {
                return 0;
            }
            Slog.e(TAG, "Invalid AAL backlight level: " + level);
            return 0;
        } else if (level <= 256) {
            return level;
        } else {
            if (sDebug) {
                Slog.e(TAG, "Invalid AAL backlight level: " + level);
            }
            return 256;
        }
    }

    private AalConfig getAalConfig(AalIndex index) {
        int level = AAL_NULL_LEVEL;
        if (this.mConfig.containsKey(index)) {
            level = this.mConfig.get(index).intValue();
        } else if (sDebug) {
            Slog.d(TAG, "No config for " + index);
        }
        return new AalConfig(index.getIndexName(), level);
    }

    /* access modifiers changed from: private */
    public String modeToString(int mode) {
        if (mode == 0) {
            return "AAL_MODE_PERFORMANCE";
        }
        if (mode == 1) {
            return "AAL_MODE_BALANCE";
        }
        if (mode == 2) {
            return "AAL_MODE_LOWPOWER";
        }
        return "Unknown mode: " + mode;
    }

    private class AalConfig {
        public int mLevel = AalUtils.AAL_NULL_LEVEL;
        public String mName = null;

        public AalConfig(String name, int level) {
            this.mName = name;
            this.mLevel = level;
        }
    }

    private class AalIndex {
        private int mMode;
        private String mName;

        AalIndex(int mode, String name) {
            set(mode, name);
        }

        private void set(int mode, String name) {
            this.mMode = mode;
            this.mName = name;
        }

        public int getMode() {
            return this.mMode;
        }

        public String getIndexName() {
            return this.mName;
        }

        public String toString() {
            return "(" + this.mMode + ": " + AalUtils.this.modeToString(this.mMode) + ", " + this.mName + ")";
        }

        public boolean equals(Object obj) {
            String str;
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AalIndex)) {
                return false;
            }
            AalIndex index = (AalIndex) obj;
            if (this.mName != null || index.mName != null) {
                String str2 = this.mName;
                if (str2 == null || (str = index.mName) == null || this.mMode != index.mMode || !str2.equals(str)) {
                    return false;
                }
                return true;
            } else if (this.mMode == index.mMode) {
                return true;
            } else {
                return false;
            }
        }

        public int hashCode() {
            String hashString = Integer.toString(this.mMode) + ":";
            if (this.mName != null) {
                hashString = hashString + Integer.toString(this.mName.hashCode());
            }
            return hashString.hashCode();
        }
    }

    public int dump(PrintWriter pw, String[] args, int opti) {
        if (!sIsAalSupported) {
            pw.println("Not support App-based AAL");
            return opti;
        } else if (args.length <= 1) {
            pw.println(dumpDebugUsageInternal());
            return opti;
        } else {
            String option = args[opti];
            if ("dump".equals(option) && args.length == 2) {
                pw.println(dumpInternal());
                return opti;
            } else if ("debugon".equals(option) && args.length == 2) {
                pw.println(setDebugInternal(true));
                pw.println("App-based AAL debug on");
                return opti;
            } else if ("debugoff".equals(option) && args.length == 2) {
                pw.println(setDebugInternal(false));
                pw.println("App-based AAL debug off");
                return opti;
            } else if ("on".equals(option) && args.length == 2) {
                setEnabledInternal(true);
                pw.println("App-based AAL on");
                return opti;
            } else if ("off".equals(option) && args.length == 2) {
                setEnabledInternal(false);
                pw.println("App-based AAL off");
                return opti;
            } else if ("mode".equals(option) && args.length == 3) {
                int opti2 = opti + 1;
                pw.println(setAalModeInternal(Integer.parseInt(args[opti2])));
                pw.println("Done");
                return opti2;
            } else if (!"set".equals(option) || !(args.length == 4 || args.length == 5)) {
                pw.println(dumpDebugUsageInternal());
                return opti;
            } else {
                int opti3 = opti + 1;
                String pkgName = args[opti3];
                int opti4 = opti3 + 1;
                int value = Integer.parseInt(args[opti4]);
                if (args.length == 4) {
                    pw.println(setSmartBacklightTableInternal(pkgName, value));
                } else {
                    opti4++;
                    pw.println(setSmartBacklightTableInternal(pkgName, value, Integer.parseInt(args[opti4])));
                }
                pw.println("Done");
                return opti4;
            }
        }
    }

    private String dumpDebugUsageInternal() {
        return "\nUsage:\n" + "1. App-based AAL help:\n" + "    adb shell dumpsys activity aal\n" + "2. Dump App-based AAL settings:\n" + "    adb shell dumpsys activity aal dump\n" + "1. App-based AAL debug on:\n" + "    adb shell dumpsys activity aal debugon\n" + "1. App-based AAL debug off:\n" + "    adb shell dumpsys activity aal debugoff\n" + "3. Enable App-based AAL:\n" + "    adb shell dumpsys activity aal on\n" + "4. Disable App-based AAL:\n" + "    adb shell dumpsys activity aal off\n" + "5. Set App-based AAL mode:\n" + "    adb shell dumpsys activity aal mode <mode>\n" + "6. Set App-based AAL config for current mode:\n" + "    adb shell dumpsys activity aal set <component> <value>\n" + "7. Set App-based AAL config for the mode:\n" + "    adb shell dumpsys activity aal set <component> <value> <mode>\n";
    }

    private synchronized String dumpInternal() {
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append("\nApp-based AAL Mode: " + this.mAalMode + "(" + modeToString(this.mAalMode) + "), Supported: " + sIsAalSupported + ", Enabled: " + sEnabled + ", Debug: " + sDebug + "\n");
        int i = 1;
        for (AalIndex index : this.mConfig.keySet()) {
            String level = Integer.toString(this.mConfig.get(index).intValue());
            sb.append("\n" + i + ". " + index + " - " + level);
            i++;
        }
        if (i == 1) {
            sb.append("\nThere is no App-based AAL configuration.\n");
            sb.append(dumpDebugUsageInternal());
        }
        if (sDebug) {
            Slog.d(TAG, "dump config: " + sb.toString());
        }
        return sb.toString();
    }

    private synchronized String setDebugInternal(boolean debug) {
        sDebug = debug;
        return "Set Debug: " + debug;
    }

    private synchronized String setSmartBacklightTableInternal(String name, int value) {
        return setSmartBacklightTableInternal(name, value, this.mAalMode);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007e, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0011, code lost:
        return "AAL is not enabled";
     */
    private synchronized String setSmartBacklightTableInternal(String name, int value, int mode) {
        if (!sEnabled) {
            if (sDebug) {
                Slog.d(TAG, "AAL is not enabled");
            }
        } else if (mode < 0 || mode >= 3) {
            String msg = "Unknown mode: " + mode;
            if (sDebug) {
                Slog.d(TAG, msg);
            }
        } else {
            AalIndex index = new AalIndex(mode, name);
            if (sDebug) {
                Slog.d(TAG, "setSmartBacklightTable(" + value + ") for " + index);
            }
            this.mConfig.put(index, Integer.valueOf(value));
            return "Set(" + value + ") for " + index;
        }
    }

    private void parseXML() throws XmlPullParserException, IOException {
        if (new File(sAalConfigXMLPath).exists()) {
            FileReader fileReader = new FileReader(sAalConfigXMLPath);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileReader);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0 && eventType == 2 && parser.getName().equals("config")) {
                    this.mConfig.put(new AalIndex(Integer.parseInt(parser.getAttributeValue(0)), parser.getAttributeValue(1)), Integer.valueOf(Integer.parseInt(parser.getAttributeValue(2))));
                }
            }
            fileReader.close();
        } else if (sDebug) {
            Slog.d(TAG, "parseXML file not exists: " + sAalConfigXMLPath);
        }
    }
}
