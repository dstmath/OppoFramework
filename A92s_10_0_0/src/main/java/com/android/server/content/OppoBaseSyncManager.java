package com.android.server.content;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.provider.Settings;
import android.util.Slog;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.wm.IColorAppSwitchManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class OppoBaseSyncManager {
    private static final String KEY_POWER_SAVE_SYNC = "power_save_sync_state";
    private static final String TAG = "OppoBaseSyncManager";
    private IColorAppSwitchManager.ActivityChangedListener mActivityChangedListener = new IColorAppSwitchManager.ActivityChangedListener() {
        /* class com.android.server.content.OppoBaseSyncManager.AnonymousClass2 */

        @Override // com.android.server.wm.IColorAppSwitchManager.ActivityChangedListener
        public void onActivityChanged(String prePkg, String nextPkg) {
            if (nextPkg != null && !nextPkg.equals(OppoBaseSyncManager.this.mTopPkg) && !"com.coloros.recents".equals(nextPkg)) {
                OppoBaseSyncManager.this.mTopPkg = nextPkg;
            }
        }
    };
    protected AtomicBoolean mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
    private Context mContext;
    private boolean mIsSubSwitchReged = false;
    private PowerManagerInternal mPowerManagerInternal;
    protected AtomicBoolean mPowerSaveDisableSync = new AtomicBoolean(false);
    protected volatile String mTopPkg = "";

    public OppoBaseSyncManager(Context context, boolean factoryTest) {
        this.mContext = context;
    }

    public void onBootPhase(int phase) {
        if (phase == 550) {
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
            this.mPowerManagerInternal.registerLowPowerModeObserver(new PowerManagerInternal.LowPowerModeListener() {
                /* class com.android.server.content.OppoBaseSyncManager.AnonymousClass1 */

                public int getServiceType() {
                    return 0;
                }

                public void onLowPowerModeChanged(PowerSaveState state) {
                    OppoBaseSyncManager.this.onLowPowerModeChangedInternal(state.batterySaverEnabled);
                }
            });
            OppoFeatureCache.get(IColorAppSwitchManager.DEFAULT).setActivityChangedListener(this.mActivityChangedListener);
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, boolean dumpAll) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        StringBuilder sb = new StringBuilder();
        sb.append("Power Save Dis Sync ");
        sb.append(this.mPowerSaveDisableSync.get() && this.mColorOsLowPowerModeEnabled.get());
        ipw.println(sb.toString());
    }

    /* access modifiers changed from: private */
    public void onLowPowerModeChangedInternal(boolean enabled) {
        this.mColorOsLowPowerModeEnabled.set(enabled);
        setPowerSaveDisableSync();
        regPowerSaveSubSwitch();
    }

    /* access modifiers changed from: private */
    public void setPowerSaveDisableSync() {
        boolean z = true;
        int ret = Settings.System.getInt(this.mContext.getContentResolver(), KEY_POWER_SAVE_SYNC, 1);
        AtomicBoolean atomicBoolean = this.mPowerSaveDisableSync;
        if (ret == 0) {
            z = false;
        }
        atomicBoolean.set(z);
        Slog.d(TAG, "power save dis sync : ret=" + this.mPowerSaveDisableSync.get());
    }

    private void regPowerSaveSubSwitch() {
        if (!this.mIsSubSwitchReged) {
            this.mIsSubSwitchReged = true;
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_POWER_SAVE_SYNC), false, new ContentObserver(new Handler()) {
                /* class com.android.server.content.OppoBaseSyncManager.AnonymousClass3 */

                public void onChange(boolean selfChange) {
                    Slog.d(OppoBaseSyncManager.TAG, "Power Save sync change.");
                    OppoBaseSyncManager.this.setPowerSaveDisableSync();
                }
            });
        }
    }
}
