package com.oppo.hypnus;

import android.app.AppGlobals;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telecom.Logging.Session;
import android.util.Log;
import java.util.HashMap;

public class Hypnus {
    public static final int ACTION_AGAINST_IDLE = 16;
    public static final int ACTION_ANIMATION = 11;
    public static final int ACTION_ANIMATION_GPU = 34;
    public static final int ACTION_ANIMATION_PRO = 38;
    public static final int ACTION_AUDIO_PLAYBACK = 2;
    public static final int ACTION_BURST_ANR = 19;
    public static final int ACTION_BURST_BM = 20;
    public static final int ACTION_BURST_GC = 17;
    public static final int ACTION_BURST_LM = 18;
    public static final int ACTION_DOWNLOAD = 3;
    public static final int ACTION_HEAVY_GPU = 33;
    public static final int ACTION_HEAVY_GPU_CPU = 21;
    public static final int ACTION_IDLE = 0;
    public static final int ACTION_INSTALLATION = 15;
    public static final int ACTION_INSTALLATION_PRO = 36;
    public static final int ACTION_IO = 12;
    public static final int ACTION_LAUNCH = 13;
    public static final int ACTION_LAUNCH1 = 22;
    public static final int ACTION_LAUNCH_GPU = 35;
    public static final int ACTION_LAUNCH_GPU_CPU = 27;
    public static final int ACTION_LAUNCH_GPU_PRO = 39;
    public static final int ACTION_LAUNCH_PRO = 29;
    public static final int ACTION_NONE = 1;
    public static final int ACTION_PERFD = 99;
    public static final int ACTION_PREVIEW = 5;
    public static final int ACTION_PRE_LAUNCH = 10;
    public static final int ACTION_REG_NETWORK = 26;
    public static final int ACTION_RESUME = 9;
    public static final int ACTION_SCROLLING_H = 8;
    public static final int ACTION_SCROLLING_V = 7;
    public static final int ACTION_SNAPSHOT = 14;
    public static final int ACTION_VIDEO_ENCODING = 6;
    public static final int ACTION_VIDEO_PLAYBACK = 4;
    public static final int BURST_TYPE_GC = 1;
    public static final int BURST_TYPE_LM = 2;
    public static final Boolean HYPNUS_STATICS_ON = Boolean.valueOf(SystemProperties.getBoolean("persist.sys.hypnus.statics", false));
    public static final int SCENE_BEAUTY_CAMERA = 21;
    public static final int SCENE_BENCHMARK = 6;
    public static final int SCENE_BOOT = 13;
    public static final int SCENE_BROWSER = 3;
    public static final int SCENE_CAMERA = 2;
    public static final int SCENE_EBOOK = 11;
    public static final int SCENE_GALLERY = 9;
    public static final int SCENE_HEAVY_GAME = 5;
    public static final int SCENE_HEAVY_GPU = 20;
    public static final int SCENE_IO = 14;
    public static final int SCENE_LAUNCHER = 12;
    public static final int SCENE_LIGHT_GAME = 4;
    public static final int SCENE_LISTVIEW = 8;
    public static final int SCENE_MUSIC = 1;
    public static final int SCENE_NAVIGATION = 10;
    public static final int SCENE_NORMAL = 0;
    public static final int SCENE_OPTIMGAME = 19;
    public static final int SCENE_ORIGINAL = 17;
    public static final int SCENE_POWERSAVE = 16;
    public static final int SCENE_SUPERAPP = 15;
    public static final int SCENE_SUPERGAME = 18;
    public static final int SCENE_VIDEO = 7;
    private static final String TAG = "Hypnus";
    public static final int TIME_ANIMATION = 600;
    public static final int TIME_ANIMATION_100 = 100;
    public static final int TIME_ANIMATION_300 = 300;
    public static final int TIME_ANIMATION_500 = 500;
    public static final int TIME_BM = 50;
    public static final int TIME_BURST = 199;
    public static final int TIME_DEX2OAT = 20000;
    public static final int TIME_INSTALLATION = 30000;
    public static final int TIME_LAUNCH = 2000;
    public static final int TIME_MAX = 600000;
    public static final int TIME_PRE_LAUNCH = 150;
    public static final int TIME_SERVICE_DELAY = 100000;
    public static final String VERSION = "M08";
    private static boolean mHypnusOK = true;
    private static String mName;
    private static int mPid;
    private static String mVersion;
    private static Hypnus sHypnus;
    public static volatile HashMap<String, Long> staticsCount = new HashMap<>();
    private boolean DEBUG = false;
    private boolean HYPNUSD_ENABLE = false;
    private boolean mInIO = false;

    private native int HypnusClearControlData();

    private native int HypnusSetControlData(int[] iArr);

    private native int HypnusSetSceneData(int i);

    public Hypnus() {
        if (SystemProperties.getBoolean("persist.debug.hypnus", false)) {
            this.DEBUG = true;
        }
        this.HYPNUSD_ENABLE = SystemProperties.getBoolean("persist.sys.hypnus.daemon.enable", false);
    }

    public void hypnusSetNotification(int msg_src, int msg_type) {
        hypnusSetNotification(msg_src, msg_type, 0, 0, 0, 0);
    }

    public String hypnusGetHighPerfModeState() {
        if (this.HYPNUSD_ENABLE) {
            return HypnusDaemonUtil.getInstance().hypnusGetHighPerfModeState();
        }
        return null;
    }

    public String hypnusGetPMState() {
        if (this.HYPNUSD_ENABLE) {
            return HypnusDaemonUtil.getInstance().hypnusGetPMState();
        }
        return null;
    }

    public String hypnusGetBenchModeState() {
        if (this.HYPNUSD_ENABLE) {
            return HypnusDaemonUtil.getInstance().hypnusGetBenchModeState();
        }
        return null;
    }

    public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) {
        if (this.HYPNUSD_ENABLE) {
            HypnusDaemonUtil.getInstance().hypnusSetNotification(msg_src, msg_type, msg_time, pid, v0, v1);
        }
    }

    public void hypnusSetScene(int pid, String processName) {
        if (this.HYPNUSD_ENABLE) {
            HypnusDaemonUtil.getInstance().hypnusSetScene(pid, processName);
        }
    }

    public void hypnusSendBootComplete() {
        if (this.DEBUG) {
            Log.d(TAG, "hypnusSendBootComplete");
        }
    }

    public void hypnusSetAction(int action, int timeout) {
        if (HYPNUS_STATICS_ON.booleanValue()) {
            try {
                String pkgnameinfo = AppGlobals.getPackageManager().getNameForUid(Process.myUid());
                if (pkgnameinfo == null) {
                    pkgnameinfo = "nopackagename";
                }
                int splitIndex = pkgnameinfo.indexOf(58);
                if (splitIndex > 0) {
                    hypnusSetAction(action, timeout, pkgnameinfo.substring(0, splitIndex));
                } else {
                    hypnusSetAction(action, timeout, pkgnameinfo);
                }
            } catch (RemoteException e) {
                hypnusSetAction(action, timeout, "exception");
                e.printStackTrace();
            }
        } else {
            hypnusSetAction(action, timeout, null);
        }
    }

    public void HypnusSetDisplayState(int state) {
        if (this.DEBUG) {
            Log.d(TAG, "HypnusSetDisplayState:" + state);
        }
        if (this.HYPNUSD_ENABLE) {
            HypnusDaemonUtil.getInstance().HypnusSetDisplayState(state);
        }
    }

    public void recordActionCount(int action, int timeout, String pkgname) {
        if (staticsCount.get(pkgname + Session.SESSION_SEPARATION_CHAR_CHILD + action) != null) {
            staticsCount.put(pkgname + Session.SESSION_SEPARATION_CHAR_CHILD + action, Long.valueOf(staticsCount.get(pkgname + Session.SESSION_SEPARATION_CHAR_CHILD + action).longValue() + 1));
            return;
        }
        staticsCount.put(pkgname + Session.SESSION_SEPARATION_CHAR_CHILD + action, 1L);
    }

    public void hypnusSetAction(int action, int timeout, String pkgname) {
        if (timeout > 180000) {
            Log.e(TAG, "hypnusSetAction: timeout longer than 180s, preven it. timeout value: " + timeout);
            timeout = 180000;
        }
        if (this.HYPNUSD_ENABLE) {
            HypnusDaemonUtil.getInstance().hypnusSetAction(action, timeout);
        }
        if (this.DEBUG) {
            Log.d(TAG, "hypnusSetAction:" + action + " timeout:" + timeout);
        }
    }

    public void hypnusSetBurst(int tid, int type, int timeout) {
        int act;
        if (type == 1) {
            act = 17;
            timeout = tid > 0 ? 199 : 0;
        } else if (type != 2) {
            Log.e(TAG, "hypnusSetBurst: Inavlid burst type:" + type);
            return;
        } else {
            act = 18;
        }
        if (this.HYPNUSD_ENABLE) {
            HypnusDaemonUtil.getInstance().hypnusSetAction(act, timeout);
        }
        if (this.DEBUG) {
            Log.d(TAG, "hypnusSetBurst tid:" + tid + " act:" + act + " timeout:" + timeout);
        }
    }

    public boolean isHypnusOK() {
        return mHypnusOK;
    }

    public static synchronized Hypnus getHypnus() {
        Hypnus hypnus;
        synchronized (Hypnus.class) {
            if (sHypnus == null) {
                sHypnus = new Hypnus();
            }
            if (sHypnus == null) {
                Log.e(TAG, "Hypnus is null");
            }
            hypnus = sHypnus;
        }
        return hypnus;
    }

    public int HypnusSetPerfData(int small_max, int small_min, int small_cores, int big_max, int big_min, int big_cores, int gpu_max, int gpu_min, int gpu_cores, int flags) {
        if (!this.HYPNUSD_ENABLE) {
            return 0;
        }
        HypnusDaemonUtil.getInstance().HypnusSetPerfData(small_max, small_min, small_cores, big_max, big_min, big_cores, gpu_max, gpu_min, gpu_cores, flags);
        return 0;
    }

    public int HypnusClrPerfData() {
        if (!this.HYPNUSD_ENABLE) {
            return 0;
        }
        HypnusDaemonUtil.getInstance().HypnusClrPerfData();
        return 0;
    }

    public int HypnusSetScenePerfData(int scene) {
        if (!this.HYPNUSD_ENABLE) {
            return 0;
        }
        HypnusDaemonUtil.getInstance().HypnusSetScenePerfData(scene);
        return 0;
    }

    public static String getLocalSignature() {
        return "308203633082024ba00302010202040875ec17300d06092a864886f70d01010b05003062310b300906035504061302383631123010060355040813096775616e67646f6e673111300f060355040713087368656e7a68656e310e300c060355040a13056368696e61310e300c060355040b13056368696e61310c300a06035504031303726f6d301e170d3135303130373036343930325a170d3235303130343036343930325a3062310b300906035504061302383631123010060355040813096775616e67646f6e673111300f060355040713087368656e7a68656e310e300c060355040a13056368696e61310e300c060355040b13056368696e61310c300a06035504031303726f6d30820122300d06092a864886f70d01010105000382010f003082010a0282010100a4677dd7cdd8d842b767d4a4";
    }
}
