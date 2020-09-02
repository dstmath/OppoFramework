package com.android.server.power;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.IBinder;
import android.os.WorkSource;
import com.android.server.power.PowerManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;

public interface IColorWakeLockCheck extends IOppoCommonFeature {
    public static final IColorWakeLockCheck DEFAULT = new IColorWakeLockCheck() {
        /* class com.android.server.power.IColorWakeLockCheck.AnonymousClass1 */
    };
    public static final int MSG_PARTIAL_WAKELOCK_TIMEOUT = 7;
    public static final int MSG_POSSIBLE_PLAYER = 9;
    public static final int MSG_WAKELOCK_ACQUIRE_SHORTTIME = 8;
    public static final String NAME = "IColorWakeLockCheck";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorWakeLockCheck;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void initArgs(ArrayList<PowerManagerService.WakeLock> arrayList, Object lock, Context context, PowerManagerService pms, SuspendBlocker suspendBlocker) {
    }

    default void PartialWakelockCheckStart() {
    }

    default ArrayList<Integer> getMusicPlayerList() {
        return new ArrayList<>();
    }

    default void PartialWakelockCheckStop() {
    }

    default void screenOnWakelockCheckStart() {
    }

    default void screenOnWakelockCheckStop() {
    }

    default void noteWakeLockChange(PowerManagerService.WakeLock wl, boolean acquire) {
    }

    default void noteWorkSourceChange(PowerManagerService.WakeLock wl, WorkSource newWorkSource) {
    }

    default boolean canSyncWakeLockAcq(int uid, String tag) {
        return true;
    }

    default boolean allowAcquireWakelock(String pkg, int flags, WorkSource ws, int ownerUid) {
        return true;
    }

    default void onDeviceIdle() {
    }

    default void logSwitch(boolean enable) {
    }

    default void dumpPossibleMusicPlayer(PrintWriter pw) {
        pw.println("dumpPossibleMusicPlayer in dummy \n");
    }

    default void dumpCameraState(PrintWriter pw) {
        pw.println("dumpCameraState in dummy \n");
    }

    default void allowAcquireShortimeHandle(IBinder lock, String pkgRelease, int flags, WorkSource ws, int ownerUid) {
    }
}
