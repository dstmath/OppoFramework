package android.view.inputmethod;

import android.app.AppGlobals;
import android.app.OppoMirrorActivityThread;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Binder;
import android.os.Debug;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Printer;
import android.view.View;
import com.oppo.hypnus.Hypnus;
import com.oppo.hypnus.HypnusManager;

public class OppoInputMethodManager {
    public static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.imelog", false);
    public static boolean DEBUG_IME_ACTIVE = (DEBUG);
    static boolean DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static boolean DEBUG_TOGGLE_SOFT = false;
    protected static final int IME_SKIP_TMP_DETACH = 686;
    static final String TAG = "InputMethodManager";
    private boolean mAlreadyInitCpt = false;
    protected boolean mApplyCompatibilityPatch = false;
    protected int mCurPid = 0;
    protected int mCurUid = 0;
    protected HypnusManager mHypnusManager = null;
    private boolean mInCptWhiteList = false;
    protected boolean mInitCompatibilityFlag = false;

    static {
        boolean z = true;
        if (!DEBUG) {
            z = false;
        }
        DEBUG_TOGGLE_SOFT = z;
    }

    public OppoInputMethodManager() {
        getDebugFlag();
        this.mCurPid = Process.myPid();
        this.mCurUid = Process.myUid();
        this.mHypnusManager = HypnusManager.getHypnusManager();
    }

    public static boolean getDebugFlag() {
        boolean z = false;
        if (DEBUG || SystemProperties.getBoolean("persist.sys.assert.imelog", false)) {
            z = true;
        }
        DEBUG = z;
        return DEBUG;
    }

    public void printCallPidAndUid(String string) {
        if (DEBUG_PANIC) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            Log.d(TAG, "call from uid:" + callingUid + ", pid:" + callingPid + " caller:" + Debug.getCallers(8));
        }
    }

    public void printCallPidAndUid(String string, int showFlags, int hideFlags) {
        if (DEBUG_PANIC) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            Log.d(TAG, "call from uid:" + callingUid + ", pid:" + callingPid + ", showFlags:" + showFlags + ", hideFlags:" + hideFlags + " caller:" + Debug.getCallers(8));
        }
    }

    public int adjustFlag(int flags, int show) {
        boolean adjust = false;
        if (OppoMirrorActivityThread.inCptWhiteList != null) {
            adjust = OppoMirrorActivityThread.inCptWhiteList.call(null, 701, AppGlobals.getInitialPackage()).booleanValue();
        }
        if (flags != show || !adjust) {
            return flags;
        }
        return 0;
    }

    public void setHypnusManager() {
        HypnusManager hypnusManager = this.mHypnusManager;
        if (hypnusManager != null) {
            hypnusManager.hypnusSetSignatureAction(12, 400, Hypnus.getLocalSignature());
        }
        if (DEBUG_PANIC) {
            printCallPidAndUid("showSoftInput");
        }
    }

    public void extendInputMethodCompatible(View view) {
        if (!this.mInitCompatibilityFlag && view != null) {
            String packageName = view.getContext().getPackageName();
            this.mApplyCompatibilityPatch = false;
            if (OppoMirrorActivityThread.inCptWhiteList != null) {
                this.mApplyCompatibilityPatch = OppoMirrorActivityThread.inCptWhiteList.call(null, 686, packageName).booleanValue();
            }
            if (DEBUG) {
                Log.v(TAG, "focusIn, mApplyCompatibilityPatch:" + this.mApplyCompatibilityPatch + ", packageName:" + packageName);
            }
        }
    }

    public boolean dynamicallyConfigImsLogTag(Printer printer, String[] args) {
        if (args.length != 3 || !"log".equals(args[0])) {
            return false;
        }
        String logCategoryTag = args[1];
        boolean on = WifiEnterpriseConfig.ENGINE_ENABLE.equals(args[2]);
        if ("all".equals(logCategoryTag) || "client".equals(logCategoryTag)) {
            DEBUG = on;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isCurrectViewSetEnableAndInCpt(View currectView) {
        if (currectView != null && !currectView.isEnabled()) {
            if (!this.mInCptWhiteList && !this.mAlreadyInitCpt) {
                if (OppoMirrorActivityThread.inCptWhiteList != null) {
                    this.mInCptWhiteList = OppoMirrorActivityThread.inCptWhiteList.call(null, 706, currectView.getContext().getPackageName()).booleanValue();
                }
                this.mAlreadyInitCpt = true;
            }
            if (this.mInCptWhiteList) {
                return true;
            }
        }
        return false;
    }
}
