package android.util;

import android.os.SystemProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BoostFramework {
    private static final String PERFORMANCE_CLASS = "com.qualcomm.qti.Performance";
    private static final String PERFORMANCE_JAR = "/system/framework/QPerformance.jar";
    private static final String TAG = "BoostFramework";
    public static final int VENDOR_HINT_ACTIVITY_BOOST = 4228;
    public static final int VENDOR_HINT_ANIM_BOOST = 4227;
    public static final int VENDOR_HINT_DRAG_BOOST = 4231;
    public static final int VENDOR_HINT_FIRST_DRAW = 4162;
    public static final int VENDOR_HINT_FIRST_LAUNCH_BOOST = 4225;
    public static final int VENDOR_HINT_MTP_BOOST = 4230;
    public static final int VENDOR_HINT_PACKAGE_INSTALL_BOOST = 4232;
    public static final int VENDOR_HINT_SCROLL_BOOST = 4224;
    public static final int VENDOR_HINT_SUBSEQ_LAUNCH_BOOST = 4226;
    public static final int VENDOR_HINT_TAP_EVENT = 4163;
    public static final int VENDOR_HINT_TOUCH_BOOST = 4229;
    private static Method mAcquireFunc = null;
    private static Constructor<Class> mConstructor = null;
    private static Method mIOPStart = null;
    private static Method mIOPStop = null;
    private static int mIopv2 = SystemProperties.getInt("iop.enable_uxe", 0);
    private static boolean mIsLoaded = false;
    private static Class<?> mPerfClass = null;
    private static Method mPerfHintFunc = null;
    private static Method mReleaseFunc = null;
    private static Method mReleaseHandlerFunc = null;
    private static Method mUXEngine_events = null;
    private static Method mUXEngine_trigger = null;
    private Object mPerf = null;

    public class Draw {
        public static final int EVENT_TYPE_V1 = 1;
    }

    public class Launch {
        public static final int BOOST_V1 = 1;
        public static final int BOOST_V2 = 2;
        public static final int BOOST_V3 = 3;
        public static final int TYPE_SERVICE_START = 100;
    }

    public class Scroll {
        public static final int HORIZONTAL = 2;
        public static final int PANEL_VIEW = 3;
        public static final int PREFILING = 4;
        public static final int VERTICAL = 1;
    }

    public BoostFramework() {
        synchronized (BoostFramework.class) {
            if (!mIsLoaded) {
                try {
                    mPerfClass = Class.forName(PERFORMANCE_CLASS);
                    mAcquireFunc = mPerfClass.getMethod("perfLockAcquire", new Class[]{Integer.TYPE, int[].class});
                    mPerfHintFunc = mPerfClass.getMethod("perfHint", new Class[]{Integer.TYPE, String.class, Integer.TYPE, Integer.TYPE});
                    mReleaseFunc = mPerfClass.getMethod("perfLockRelease", new Class[0]);
                    mReleaseHandlerFunc = mPerfClass.getDeclaredMethod("perfLockReleaseHandler", new Class[]{Integer.TYPE});
                    mIOPStart = mPerfClass.getDeclaredMethod("perfIOPrefetchStart", new Class[]{Integer.TYPE, String.class, String.class});
                    mIOPStop = mPerfClass.getDeclaredMethod("perfIOPrefetchStop", new Class[0]);
                    if (mIopv2 == 1) {
                        mUXEngine_events = mPerfClass.getDeclaredMethod("perfUXEngine_events", new Class[]{Integer.TYPE, Integer.TYPE, String.class, Integer.TYPE});
                        mUXEngine_trigger = mPerfClass.getDeclaredMethod("perfUXEngine_trigger", new Class[]{Integer.TYPE});
                    }
                    mIsLoaded = true;
                } catch (Exception e) {
                    Log.e(TAG, "BoostFramework() : Exception_1 = " + e);
                }
            }
        }
        try {
            if (mPerfClass != null) {
                this.mPerf = mPerfClass.newInstance();
                return;
            }
            return;
        } catch (Exception e2) {
            Log.e(TAG, "BoostFramework() : Exception_2 = " + e2);
            return;
        }
    }

    public int perfLockAcquire(int duration, int... list) {
        int ret = -1;
        try {
            return ((Integer) mAcquireFunc.invoke(this.mPerf, new Object[]{Integer.valueOf(duration), list})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return ret;
        }
    }

    public int perfLockRelease() {
        int ret = -1;
        try {
            return ((Integer) mReleaseFunc.invoke(this.mPerf, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return ret;
        }
    }

    public int perfLockReleaseHandler(int handle) {
        int ret = -1;
        try {
            return ((Integer) mReleaseHandlerFunc.invoke(this.mPerf, new Object[]{Integer.valueOf(handle)})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return ret;
        }
    }

    public int perfHint(int hint, String userDataStr) {
        return perfHint(hint, userDataStr, -1, -1);
    }

    public int perfHint(int hint, String userDataStr, int userData) {
        return perfHint(hint, userDataStr, userData, -1);
    }

    public int perfHint(int hint, String userDataStr, int userData1, int userData2) {
        int ret = -1;
        try {
            return ((Integer) mPerfHintFunc.invoke(this.mPerf, new Object[]{Integer.valueOf(hint), userDataStr, Integer.valueOf(userData1), Integer.valueOf(userData2)})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return ret;
        }
    }

    public int perfIOPrefetchStart(int pid, String pkg_name, String code_path) {
        int ret = -1;
        try {
            return ((Integer) mIOPStart.invoke(this.mPerf, new Object[]{Integer.valueOf(pid), pkg_name, code_path})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return ret;
        }
    }

    public int perfIOPrefetchStop() {
        int ret = -1;
        try {
            return ((Integer) mIOPStop.invoke(this.mPerf, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return ret;
        }
    }

    public int perfUXEngine_events(int opcode, int pid, String pkg_name, int lat) {
        int ret = -1;
        try {
            if (mIopv2 == 0 || mUXEngine_events == null) {
                return -1;
            }
            ret = ((Integer) mUXEngine_events.invoke(this.mPerf, new Object[]{Integer.valueOf(opcode), Integer.valueOf(pid), pkg_name, Integer.valueOf(lat)})).intValue();
            return ret;
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
        }
    }

    public String perfUXEngine_trigger(int opcode) {
        String ret = null;
        try {
            if (mIopv2 == 0 || mUXEngine_trigger == null) {
                return null;
            }
            ret = (String) mUXEngine_trigger.invoke(this.mPerf, new Object[]{Integer.valueOf(opcode)});
            return ret;
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
        }
    }
}
