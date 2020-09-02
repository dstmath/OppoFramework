package android.util;

import android.content.Context;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BoostFramework {
    private static final String PERFORMANCE_CLASS = "com.qualcomm.qti.Performance";
    private static final String PERFORMANCE_JAR = "/system/framework/QPerformance.jar";
    private static final String TAG = "BoostFramework";
    public static final int UXE_EVENT_BINDAPP = 2;
    public static final int UXE_EVENT_DISPLAYED_ACT = 3;
    public static final int UXE_EVENT_GAME = 5;
    public static final int UXE_EVENT_KILL = 4;
    public static final int UXE_EVENT_PKG_INSTALL = 8;
    public static final int UXE_EVENT_PKG_UNINSTALL = 7;
    public static final int UXE_EVENT_SUB_LAUNCH = 6;
    public static final int UXE_TRIGGER = 1;
    private static final String UXPERFORMANCE_CLASS = "com.qualcomm.qti.UxPerformance";
    private static final String UXPERFORMANCE_JAR = "/system/framework/UxPerformance.jar";
    public static final int VENDOR_FEEDBACK_LAUNCH_END_POINT = 5634;
    public static final int VENDOR_FEEDBACK_WORKLOAD_TYPE = 5633;
    public static final int VENDOR_HINT_ACTIVITY_BOOST = 4228;
    public static final int VENDOR_HINT_ANIM_BOOST = 4227;
    public static final int VENDOR_HINT_APP_UPDATE = 4242;
    public static final int VENDOR_HINT_DRAG_BOOST = 4231;
    public static final int VENDOR_HINT_FIRST_DRAW = 4162;
    public static final int VENDOR_HINT_FIRST_LAUNCH_BOOST = 4225;
    public static final int VENDOR_HINT_KILL = 4243;
    public static final int VENDOR_HINT_MTP_BOOST = 4230;
    public static final int VENDOR_HINT_PACKAGE_INSTALL_BOOST = 4232;
    public static final int VENDOR_HINT_PERFORMANCE_MODE = 4241;
    public static final int VENDOR_HINT_ROTATION_ANIM_BOOST = 4240;
    public static final int VENDOR_HINT_ROTATION_LATENCY_BOOST = 4233;
    public static final int VENDOR_HINT_SCROLL_BOOST = 4224;
    public static final int VENDOR_HINT_SUBSEQ_LAUNCH_BOOST = 4226;
    public static final int VENDOR_HINT_TAP_EVENT = 4163;
    public static final int VENDOR_HINT_TOUCH_BOOST = 4229;
    private static Method sAcquireFunc = null;
    private static Method sFeedbackFunc = null;
    private static Method sIOPStart = null;
    private static Method sIOPStop = null;
    private static boolean sIsLoaded = false;
    private static Class<?> sPerfClass = null;
    private static Method sPerfGetPropFunc = null;
    private static Method sPerfHintFunc = null;
    private static Method sReleaseFunc = null;
    private static Method sReleaseHandlerFunc = null;
    private static Method sUXEngineEvents = null;
    private static Method sUXEngineTrigger = null;
    private static Method sUxIOPStart = null;
    private static boolean sUxIsLoaded = false;
    private static Class<?> sUxPerfClass = null;
    private Object mPerf = null;
    private Object mUxPerf = null;

    public class Scroll {
        public static final int HORIZONTAL = 2;
        public static final int PANEL_VIEW = 3;
        public static final int PREFILING = 4;
        public static final int VERTICAL = 1;

        public Scroll() {
        }
    }

    public class Launch {
        public static final int BOOST_GAME = 4;
        public static final int BOOST_V1 = 1;
        public static final int BOOST_V2 = 2;
        public static final int BOOST_V3 = 3;
        public static final int RESERVED_1 = 5;
        public static final int RESERVED_2 = 6;
        public static final int TYPE_ATTACH_APPLICATION = 103;
        public static final int TYPE_SERVICE_START = 100;
        public static final int TYPE_START_APP_FROM_BG = 102;
        public static final int TYPE_START_PROC = 101;

        public Launch() {
        }
    }

    public class Draw {
        public static final int EVENT_TYPE_V1 = 1;

        public Draw() {
        }
    }

    public class WorkloadType {
        public static final int APP = 1;
        public static final int BROWSER = 3;
        public static final int GAME = 2;
        public static final int NOT_KNOWN = 0;
        public static final int PREPROAPP = 4;

        public WorkloadType() {
        }
    }

    public BoostFramework() {
        initFunctions();
        try {
            if (sPerfClass != null) {
                this.mPerf = sPerfClass.newInstance();
            }
            if (sUxPerfClass != null) {
                this.mUxPerf = sUxPerfClass.newInstance();
            }
        } catch (Exception e) {
            Log.e(TAG, "BoostFramework() : Exception_2 = " + e);
        }
    }

    public BoostFramework(Context context) {
        Constructor cons;
        initFunctions();
        try {
            if (!(sPerfClass == null || (cons = sPerfClass.getConstructor(Context.class)) == null)) {
                this.mPerf = cons.newInstance(context);
            }
            if (sUxPerfClass != null) {
                this.mUxPerf = sUxPerfClass.newInstance();
            }
        } catch (Exception e) {
            Log.e(TAG, "BoostFramework() : Exception_3 = " + e);
        }
    }

    public BoostFramework(boolean isUntrustedDomain) {
        Constructor cons;
        initFunctions();
        try {
            if (!(sPerfClass == null || (cons = sPerfClass.getConstructor(Boolean.TYPE)) == null)) {
                this.mPerf = cons.newInstance(Boolean.valueOf(isUntrustedDomain));
            }
            if (sUxPerfClass != null) {
                this.mUxPerf = sUxPerfClass.newInstance();
            }
        } catch (Exception e) {
            Log.e(TAG, "BoostFramework() : Exception_5 = " + e);
        }
    }

    private void initFunctions() {
        synchronized (BoostFramework.class) {
            if (!sIsLoaded) {
                try {
                    sPerfClass = Class.forName(PERFORMANCE_CLASS);
                    sAcquireFunc = sPerfClass.getMethod("perfLockAcquire", Integer.TYPE, int[].class);
                    sPerfHintFunc = sPerfClass.getMethod("perfHint", Integer.TYPE, String.class, Integer.TYPE, Integer.TYPE);
                    sReleaseFunc = sPerfClass.getMethod("perfLockRelease", new Class[0]);
                    sReleaseHandlerFunc = sPerfClass.getDeclaredMethod("perfLockReleaseHandler", Integer.TYPE);
                    sFeedbackFunc = sPerfClass.getMethod("perfGetFeedback", Integer.TYPE, String.class);
                    sIOPStart = sPerfClass.getDeclaredMethod("perfIOPrefetchStart", Integer.TYPE, String.class, String.class);
                    sIOPStop = sPerfClass.getDeclaredMethod("perfIOPrefetchStop", new Class[0]);
                    sPerfGetPropFunc = sPerfClass.getMethod("perfGetProp", String.class, String.class);
                    try {
                        sUXEngineEvents = sPerfClass.getDeclaredMethod("perfUXEngine_events", Integer.TYPE, Integer.TYPE, String.class, Integer.TYPE, String.class);
                        sUXEngineTrigger = sPerfClass.getDeclaredMethod("perfUXEngine_trigger", Integer.TYPE);
                    } catch (Exception e) {
                        Log.i(TAG, "BoostFramework() : Exception_4 = PreferredApps not supported");
                    }
                    sIsLoaded = true;
                } catch (Exception e2) {
                    Log.e(TAG, "BoostFramework() : Exception_1 = " + e2);
                }
                try {
                    sUxPerfClass = Class.forName(UXPERFORMANCE_CLASS);
                    sUxIOPStart = sUxPerfClass.getDeclaredMethod("perfIOPrefetchStart", Integer.TYPE, String.class, String.class);
                    sUxIsLoaded = true;
                } catch (Exception e3) {
                    Log.e(TAG, "BoostFramework() Ux Perf: Exception = " + e3);
                }
            }
        }
    }

    public int perfLockAcquire(int duration, int... list) {
        try {
            if (sAcquireFunc == null) {
                return -1;
            }
            return ((Integer) sAcquireFunc.invoke(this.mPerf, Integer.valueOf(duration), list)).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfLockRelease() {
        try {
            if (sReleaseFunc != null) {
                return ((Integer) sReleaseFunc.invoke(this.mPerf, new Object[0])).intValue();
            }
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfLockReleaseHandler(int handle) {
        try {
            if (sReleaseHandlerFunc == null) {
                return -1;
            }
            return ((Integer) sReleaseHandlerFunc.invoke(this.mPerf, Integer.valueOf(handle))).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfHint(int hint, String userDataStr) {
        return perfHint(hint, userDataStr, -1, -1);
    }

    public int perfHint(int hint, String userDataStr, int userData) {
        return perfHint(hint, userDataStr, userData, -1);
    }

    public int perfHint(int hint, String userDataStr, int userData1, int userData2) {
        try {
            if (sPerfHintFunc == null) {
                return -1;
            }
            return ((Integer) sPerfHintFunc.invoke(this.mPerf, Integer.valueOf(hint), userDataStr, Integer.valueOf(userData1), Integer.valueOf(userData2))).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfGetFeedback(int req, String userDataStr) {
        try {
            if (sFeedbackFunc == null) {
                return -1;
            }
            return ((Integer) sFeedbackFunc.invoke(this.mPerf, Integer.valueOf(req), userDataStr)).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfIOPrefetchStart(int pid, String pkgName, String codePath) {
        int ret = -1;
        try {
            ret = ((Integer) sIOPStart.invoke(this.mPerf, Integer.valueOf(pid), pkgName, codePath)).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
        }
        try {
            return ((Integer) sUxIOPStart.invoke(this.mUxPerf, Integer.valueOf(pid), pkgName, codePath)).intValue();
        } catch (Exception e2) {
            Log.e(TAG, "Ux Perf Exception " + e2);
            return ret;
        }
    }

    public int perfIOPrefetchStop() {
        try {
            return ((Integer) sIOPStop.invoke(this.mPerf, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfUXEngine_events(int opcode, int pid, String pkgName, int lat) {
        return perfUXEngine_events(opcode, pid, pkgName, lat, null);
    }

    public int perfUXEngine_events(int opcode, int pid, String pkgName, int lat, String codePath) {
        try {
            if (sUXEngineEvents == null) {
                return -1;
            }
            return ((Integer) sUXEngineEvents.invoke(this.mPerf, Integer.valueOf(opcode), Integer.valueOf(pid), pkgName, Integer.valueOf(lat), codePath)).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public String perfUXEngine_trigger(int opcode) {
        try {
            if (sUXEngineTrigger == null) {
                return null;
            }
            return (String) sUXEngineTrigger.invoke(this.mPerf, Integer.valueOf(opcode));
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return null;
        }
    }

    public String perfGetProp(String prop_name, String def_val) {
        try {
            if (sPerfGetPropFunc == null) {
                return def_val;
            }
            return (String) sPerfGetPropFunc.invoke(this.mPerf, prop_name, def_val);
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return "";
        }
    }
}
