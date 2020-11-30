package com.android.server.power;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseBooleanArray;
import com.android.server.ColorSmartDozeHelper;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.am.ColorResourcePreloadDatabaseHelper;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.power.PowerManagerService;
import com.android.server.wm.ColorAccessController;
import com.color.util.ColorTypeCastingHelper;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/* access modifiers changed from: package-private */
public class ColorPartialWakeLockCheck {
    private static final String ACTION_OPPO_CHECK_MUSIC_PLAYER = "oppo.intent.action.oppoguardelfdeepsleep.CheckMusicPlayer";
    private static final String ACTION_OPPO_CONFIRM_WAKELOCK_RELEASE = "android.intent.action.OPPO_CONFIRM_WAKELOCK_RELEASE";
    private static final String ACTION_OPPO_GUARDELF_AUDIO_STATE_DETECTED = "android.intent.action.OPPO_GUARDELF_AUDIO_STATE_DETECTED";
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR = "android.intent.action.OPPO_GUARD_ELF_MONITOR";
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    private static final String ACTION_OPPO_WHITELIST_WAKELOCK_RELEASE = "android.intent.action.OPPO_WHITELIST_WAKELOCK_RELEASE";
    private static final String ATAG = "ColorWakeLockCheck";
    private static final int AUDIO_STATE_INVALID = -1;
    private static final int AUDIO_STATE_PLAYBACK = 2;
    private static final int AUDIO_STATE_SILENCE = 1;
    private static final int AUDIO_STATE_UNKNOWN = 0;
    private static final int AUDIO_WORKSOURCE_NULL_COUNT = 3;
    private static final long DELAY_POSSIBLE_PLAYER = 10000;
    private static final long DELAY_START_MONITOR_AUDIO_DATA = 20000;
    public static final List<String> FORCE_RELEASE_LIST = Arrays.asList("com.google.android.gms", "com.google.android.googlequicksearchbox");
    private static final long INTERVAL_MONITOR_AUDIO_DATA = 90000;
    public static List<String> LIST_AUIDO_IGNORE = Arrays.asList("com.coloros.keyguard.notification", ColorAccessController.PROTECT_FILTER_USERCENTER_EXTRA_VALUE);
    public static List<String> LIST_TAG_AUDIO_APP = Arrays.asList("android.media.MediaPlayer");
    public static List<String> LIST_TAG_AUDIO_MEDIA_UID = Arrays.asList(TAG_AUDIOMIX, "AudioDirectOut", "AudioOffload", "AudioDup", "AudioUnknown");
    private static final String PACKAGE_NAME_SYSTEM = "android";
    private static final long PARTIAL_WAKELOCK_TIMEOUT_THRESHOLD = 300;
    private static final long SHORTTIME_WAKELOCK_ACQUIRE_TIME = 2000;
    private static final String SYSTEM_UI_PKG = "com.android.systemui";
    private static final String TAG_ALARM = "*alarm*";
    private static final String TAG_AOD_WAKELOCK = "AodService:wakeLock";
    private static final String TAG_AUDIOIN = "AudioIn";
    private static final String TAG_AUDIOMIX = "AudioMix";
    private static final long THRESHOLD_MIN_AUDIO_HELD_DURATION = 30000;
    private final boolean ADBG;
    private final int AUDIO_WL_HELD_UID = 1041;
    private boolean DEBUG = false;
    private int cntPlayback;
    private int cntSilence;
    private AlarmManager mAlarmManager;
    private final AlarmManager.OnAlarmListener mAudioDataAlarmListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.power.ColorPartialWakeLockCheck.AnonymousClass2 */

        public void onAlarm() {
            ColorPartialWakeLockCheck.this.monitorAudioData();
        }
    };
    private ArrayList<String> mAudioInList = new ArrayList<>();
    private AtomicInteger mAudioState = new AtomicInteger(0);
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.power.ColorPartialWakeLockCheck.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ColorPartialWakeLockCheck.ACTION_OPPO_CONFIRM_WAKELOCK_RELEASE.equals(action)) {
                ColorPartialWakeLockCheck.this.confirmWakelockRelease(intent.getStringExtra(BrightnessConstants.AppSplineXml.TAG_PACKAGE));
            } else if (ColorPartialWakeLockCheck.ACTION_OPPO_WHITELIST_WAKELOCK_RELEASE.equals(action)) {
                ColorPartialWakeLockCheck.this.whitelistWakelockRelease(intent.getStringExtra(BrightnessConstants.AppSplineXml.TAG_PACKAGE));
            } else if (ColorPartialWakeLockCheck.ACTION_OPPO_CHECK_MUSIC_PLAYER.equals(action)) {
                ColorPartialWakeLockCheck.this.doCheckPossibleMusicPlayer();
            }
        }
    };
    private int mCntAudioWorksourceNull;
    private final Context mContext;
    private Handler mHandler;
    private long mIntervalCheck = ColorSmartDozeHelper.DEBUG_GPS_EXEPTION_TIME;
    private boolean mIsDeepIdle = false;
    private ArrayList<String> mListForbidHeldWakelock = new ArrayList<>();
    private final Object mLock;
    private final Object mLockPartialWL = new Object();
    private boolean mMonitorAudioDataStarted = false;
    private ArrayList<String> mMusicPlayerList = new ArrayList<>();
    private ArrayList<Integer> mMusicPlayerUids = new ArrayList<>();
    private final PowerManagerService mPms;
    private ArrayList<String> mPossibleAudioInList = new ArrayList<>();
    private ArrayList<String> mPossibleMusicPlayerList = new ArrayList<>();
    private ArrayMap<String, SingleWasteWakeLock> mSingleWasteWakeLock = new ArrayMap<>();
    private ArrayList<String> mSportsFitness = new ArrayList<>();
    private ArrayList<String> mSyncWakeLock = new ArrayList<>();
    private OppoSysStateManager mSysState;
    private long mThrshWakeLockTimeOut = ColorSmartDozeHelper.DEBUG_GPS_EXEPTION_TIME;
    private long mTimeStampScreenoff;
    private SparseBooleanArray mUidsForbidHeldWakelock = new SparseBooleanArray();
    private final CommonUtil mUtil;
    private final ArrayList<PowerManagerService.WakeLock> mWakeLocks;
    private ArrayMap<String, WakelockStats> mWakelockStats = new ArrayMap<>();
    private ArrayMap<String, WakelockStats> mWastPowerWakelock = new ArrayMap<>();
    private boolean needCheck = false;

    public ColorPartialWakeLockCheck(ArrayList<PowerManagerService.WakeLock> wakeLocks, Object lock, Context context, PowerManagerService pms, CommonUtil util, boolean dbg, Handler handler) {
        this.mWakeLocks = wakeLocks;
        this.mLock = lock;
        this.mContext = context;
        this.mPms = pms;
        this.mUtil = util;
        this.ADBG = dbg;
        this.mHandler = handler;
        this.mSysState = OppoSysStateManager.getInstance();
        this.mSysState.initOppoGuardElfRcv(context, handler);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OPPO_CONFIRM_WAKELOCK_RELEASE);
        filter.addAction(ACTION_OPPO_WHITELIST_WAKELOCK_RELEASE);
        filter.addAction(ACTION_OPPO_CHECK_MUSIC_PLAYER);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, "oppo.permission.OPPO_COMPONENT_SAFE", this.mHandler);
    }

    public boolean canSyncWakeLockAcq(int uid, String tag) {
        synchronized (this.mSyncWakeLock) {
            if (uid == 1000) {
                if (this.mSyncWakeLock.contains(tag)) {
                    Slog.w(ATAG, "canSyncWakeLockAcq: tag{" + tag + "} can not Acquire, for it's detected waste power before!");
                    return false;
                }
            }
            return true;
        }
    }

    public void clearSyncWakelock() {
        synchronized (this.mSyncWakeLock) {
            this.mSyncWakeLock.clear();
        }
    }

    public ArrayList<Integer> getMusicPlayerList() {
        ArrayList<Integer> arrayList;
        synchronized (this.mMusicPlayerUids) {
            arrayList = this.mMusicPlayerUids;
        }
        return arrayList;
    }

    public void noteWakeLockChange(PowerManagerService.WakeLock wl, int message, Handler handler, WorkSource ws) {
        synchronized (this.mLockPartialWL) {
            if (this.needCheck) {
                if (!ignoreCheck(wl)) {
                    Message msg = handler.obtainMessage();
                    msg.what = message;
                    msg.setData(makeWakeLockData(wl, ws));
                    handler.sendMessage(msg);
                }
            }
        }
    }

    public void wakeLockTimeout(Message msg) {
        WakelockStats ws = (WakelockStats) msg.obj;
        if (this.needCheck) {
            if (this.DEBUG) {
                Slog.w(ATAG, "wakeLockTimeout: pkgName=" + ws.mPkgName);
            }
            long now = SystemClock.uptimeMillis();
            reportTimeoutWakeLockForceStop(ws, now);
            if (!this.mSysState.isNotRestrictPkgWakeLock(ws.mPkgName)) {
                ws.scheduleWakeLockTimeoutCheck(now);
            }
        } else if (this.DEBUG) {
            Slog.w(ATAG, "wakeLockTimeout: needCheck is false. pkgName=" + ws.mPkgName);
        }
    }

    public void onScreenOff(int msgContinuousCheck) {
        if (this.DEBUG) {
            Slog.w(ATAG, "onScreenoff: start");
        }
        this.mWakelockStats.clear();
        this.mIsDeepIdle = false;
        if (!this.mSysState.isCharging()) {
            synchronized (this.mPossibleMusicPlayerList) {
                this.mPossibleMusicPlayerList.clear();
            }
            synchronized (this.mPossibleAudioInList) {
                this.mPossibleAudioInList.clear();
            }
            synchronized (this.mAudioInList) {
                this.mAudioInList.clear();
            }
            synchronized (this.mSportsFitness) {
                this.mSportsFitness.clear();
            }
            synchronized (this.mListForbidHeldWakelock) {
                this.mListForbidHeldWakelock.clear();
            }
            synchronized (this.mUidsForbidHeldWakelock) {
                this.mUidsForbidHeldWakelock.clear();
            }
            synchronized (this.mMusicPlayerList) {
                this.mMusicPlayerList.clear();
            }
            synchronized (this.mMusicPlayerUids) {
                this.mMusicPlayerUids.clear();
            }
            this.mMonitorAudioDataStarted = false;
            this.mAudioState.set(0);
            this.cntSilence = 0;
            this.cntPlayback = 0;
            ArrayList<Bundle> dataList = new ArrayList<>();
            synchronized (this.mLock) {
                for (int index = 0; index < this.mWakeLocks.size(); index++) {
                    PowerManagerService.WakeLock wl = this.mWakeLocks.get(index);
                    if (!ignoreCheck(wl)) {
                        dataList.add(makeWakeLockData(wl, wl.mWorkSource));
                    }
                }
                synchronized (this.mLockPartialWL) {
                    this.needCheck = true;
                    Iterator<Bundle> it = dataList.iterator();
                    while (it.hasNext()) {
                        noteStartWakeLock(it.next());
                    }
                    this.mThrshWakeLockTimeOut = OppoGuardElfConfigUtil.getInstance().getThresholdWakeLockTimeout() * 1000;
                    this.mIntervalCheck = this.mThrshWakeLockTimeOut;
                    this.mTimeStampScreenoff = SystemClock.elapsedRealtime();
                    this.mHandler.sendEmptyMessageDelayed(msgContinuousCheck, this.mIntervalCheck);
                }
            }
            this.mCntAudioWorksourceNull = 0;
            if (this.DEBUG) {
                Slog.w(ATAG, "onScreenoff: exit. mThrshWakeLockTimeOut=" + this.mThrshWakeLockTimeOut);
            }
        } else if (this.DEBUG) {
            Slog.w(ATAG, "onScreenoff: isCharging. do nothing!!!");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005b, code lost:
        r1 = r3.mAlarmManager;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005d, code lost:
        if (r1 == null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005f, code lost:
        r1.cancel(r3.mAudioDataAlarmListener);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0066, code lost:
        if (r3.DEBUG == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0068, code lost:
        android.util.Slog.w(com.android.server.power.ColorPartialWakeLockCheck.ATAG, "onScreenon: exit");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        return;
     */
    public void onScreenOn(int msgContinuousCheck) {
        if (this.DEBUG) {
            Slog.w(ATAG, "onScreenon: start. needCheck=" + this.needCheck);
        }
        this.mIsDeepIdle = false;
        synchronized (this.mListForbidHeldWakelock) {
            this.mListForbidHeldWakelock.clear();
        }
        synchronized (this.mUidsForbidHeldWakelock) {
            this.mUidsForbidHeldWakelock.clear();
        }
        synchronized (this.mLockPartialWL) {
            if (this.needCheck) {
                this.needCheck = false;
                this.mHandler.removeMessages(7);
                this.mHandler.removeMessages(9);
                this.mHandler.removeMessages(msgContinuousCheck);
                dumpAllWakeLockStats();
                this.mWakelockStats.clear();
                getTimeoutWakeLock();
            }
        }
    }

    public void monitorAudioData() {
        String isSilence = ((AudioManager) this.mContext.getSystemService("audio")).getParameters("get_silence");
        if ("1".equals(isSilence)) {
            this.cntSilence++;
            this.cntPlayback = 0;
        } else if ("0".equals(isSilence)) {
            this.cntPlayback++;
            this.cntSilence = 0;
        } else {
            this.mAudioState.set(-1);
            if (this.ADBG) {
                Slog.d(ATAG, "monitorAudioData: get_silence not supported!");
                return;
            }
            return;
        }
        if (this.cntPlayback >= 3) {
            this.mAudioState.set(2);
            for (int i = 0; i < this.mWakelockStats.size(); i++) {
                collectMusicPlayer(this.mWakelockStats.valueAt(i));
            }
            this.mContext.sendBroadcastAsUser(new Intent(ACTION_OPPO_GUARDELF_AUDIO_STATE_DETECTED), UserHandle.ALL);
        } else if (this.cntSilence >= 3) {
            this.mAudioState.set(1);
            this.mContext.sendBroadcastAsUser(new Intent(ACTION_OPPO_GUARDELF_AUDIO_STATE_DETECTED), UserHandle.ALL);
        } else {
            AlarmManager alarmManager = this.mAlarmManager;
            if (alarmManager != null) {
                alarmManager.setExact(2, INTERVAL_MONITOR_AUDIO_DATA + SystemClock.elapsedRealtime(), "monitorAudioData", this.mAudioDataAlarmListener, this.mHandler);
            }
        }
        if (this.ADBG) {
            Slog.d(ATAG, "monitorAudioData: cntPlayback=" + this.cntPlayback + ", cntSilence=" + this.cntSilence);
        }
    }

    public void relasShortTimeWl(Bundle data) {
        if (this.needCheck && data != null) {
            IBinder lock = data.getBinder("wlBinder");
            if (lock == null) {
                Slog.d(ATAG, "relasShortTimeWl: lock is null.");
                return;
            }
            int flags = data.getInt("flags");
            String pkgName = data.getString("pkgName");
            synchronized (this.mLock) {
                int i = this.mWakeLocks.size() - 1;
                while (true) {
                    if (i < 0) {
                        break;
                    } else if (this.mWakeLocks.get(i).mLock == lock) {
                        getInner().releaseWakeLockInternal(lock, flags);
                        if (this.ADBG) {
                            Slog.d(ATAG, "relasShortTimeWl: find pkgName=" + pkgName);
                        }
                    } else {
                        i--;
                    }
                }
            }
        }
    }

    public void noteStartWakeLock(Bundle data) {
        String tagName;
        WorkSource ws;
        String[] packages;
        if (data != null && this.needCheck && (tagName = data.getString("tagName")) != null) {
            long now = data.getLong("now");
            int ownerUid = data.getInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID);
            WorkSource ws2 = (WorkSource) data.getParcelable("workSource");
            if (ws2 != null) {
                int size = ws2.size();
                int k = 0;
                while (k < size) {
                    int uid = ws2.get(k);
                    String pkgName = ws2.getName(k);
                    if (pkgName != null) {
                        getWakelockStatsLocked(uid, pkgName).startWakeLock(tagName, now, isAudioWakelock(uid, ownerUid, pkgName, tagName));
                        collectPossibleAudioInRecorder(ownerUid, pkgName, tagName);
                        if (this.DEBUG) {
                            StringBuilder sb = new StringBuilder();
                            ws = ws2;
                            sb.append("noteStartWakeLock: worksource name. pkgName=");
                            sb.append(pkgName);
                            sb.append(", tagName=");
                            sb.append(tagName);
                            sb.append(", uid=");
                            sb.append(uid);
                            Slog.w(ATAG, sb.toString());
                        } else {
                            ws = ws2;
                        }
                    } else {
                        ws = ws2;
                        if (uid >= 10000 && (packages = this.mUtil.getPackagesForUid(uid)) != null) {
                            int i = 0;
                            while (i < packages.length) {
                                String pkg = packages[i];
                                getWakelockStatsLocked(uid, pkg).startWakeLock(tagName, now, isAudioWakelock(uid, ownerUid, pkg, tagName));
                                collectPossibleAudioInRecorder(ownerUid, pkg, tagName);
                                if (this.DEBUG) {
                                    Slog.w(ATAG, "noteStartWakeLock: worksource uid. pkg=" + pkg + ", tagName=" + tagName + ", uid=" + uid);
                                }
                                i++;
                                packages = packages;
                                size = size;
                            }
                        }
                    }
                    k++;
                    ws2 = ws;
                    size = size;
                }
                return;
            }
            String pkgName2 = data.getString("pkgName");
            if (pkgName2 != null) {
                getWakelockStatsLocked(ownerUid, pkgName2).startWakeLock(tagName, now, isAudioWakelock(ownerUid, ownerUid, pkgName2, tagName));
                collectPossibleAudioInRecorder(ownerUid, pkgName2, tagName);
                if (this.DEBUG) {
                    Slog.w(ATAG, "noteStartWakeLock: pkgName=" + pkgName2 + ", tagName=" + tagName + ", uid=" + ownerUid);
                }
            }
        }
    }

    public void noteStopWakeLock(Bundle data) {
        String tagName;
        String[] packages;
        if (!(data == null || !this.needCheck || (tagName = data.getString("tagName")) == null)) {
            long now = data.getLong("now");
            WorkSource ws = (WorkSource) data.getParcelable("workSource");
            if (ws != null) {
                int size = ws.size();
                for (int k = 0; k < size; k++) {
                    String pkgName = ws.getName(k);
                    if (pkgName != null) {
                        stopWakeLock(pkgName, tagName, now);
                    } else {
                        int uid = ws.get(k);
                        if (uid >= 10000 && (packages = this.mUtil.getPackagesForUid(uid)) != null) {
                            for (String str : packages) {
                                stopWakeLock(str, tagName, now);
                            }
                        } else {
                            return;
                        }
                    }
                }
                return;
            }
            stopWakeLock(data.getString("pkgName"), tagName, now);
        }
    }

    private void stopWakeLock(String pkg, String tagName, long now) {
        if (pkg != null) {
            WakelockStats wakeStats = this.mWakelockStats.get(pkg);
            if (wakeStats != null) {
                wakeStats.stopWakeLock(tagName, now);
                reportTimeoutWakeLockForceStop(wakeStats, now);
                if (this.DEBUG) {
                    Slog.w(ATAG, "stopWakeLock: pkg=" + pkg + ", tagName=" + tagName);
                }
            } else if (this.ADBG) {
                Slog.w(ATAG, "stopWakeLock: wakeStats is null!!! pkgName=" + pkg);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0174, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x017b, code lost:
        r0 = th;
     */
    public void wakeLockHeldContinuouslyCheck(int msgContinuousCheck) {
        if (this.needCheck) {
            long now = SystemClock.uptimeMillis();
            ArrayList<String> tagSyncList = new ArrayList<>();
            synchronized (this.mLock) {
                int index = 0;
                while (index < this.mWakeLocks.size()) {
                    PowerManagerService.WakeLock wl = this.mWakeLocks.get(index);
                    if ((wl.mFlags & 65535) == 1) {
                        if (!wl.mDisabled) {
                            long hold = now - wl.mAcquireTime;
                            if (hold >= this.mThrshWakeLockTimeOut) {
                                long hold2 = hold / 1000;
                                if (!TAG_ALARM.equals(wl.mTag) || wl.mOwnerUid != 1000) {
                                    if (wl.mTag.startsWith("*sync*") && wl.mOwnerUid == 1000) {
                                        getInner().releaseWakeLockInternal(wl.mLock, wl.mFlags);
                                        index--;
                                        tagSyncList.add(wl.mTag);
                                        Slog.v(ATAG, "wakeLockHeldContinuouslyCheck: Internally releasing the wakelock{" + wl.mTag + "} acquired by SyncManager");
                                    }
                                    if (wl.mWorkSource != null) {
                                        int size = wl.mWorkSource.size();
                                        for (int k = 0; k < size; k++) {
                                            int uid = wl.mWorkSource.get(k);
                                            if (this.ADBG) {
                                                Slog.w(ATAG, "wakeLockHeldContinuouslyCheck: has worksource. WakeLock { " + wl.mTag + " } has been hold for " + hold2 + "s, uids[" + k + "]=" + uid);
                                            }
                                        }
                                    } else if (this.ADBG) {
                                        Slog.w(ATAG, "wakeLockHeldContinuouslyCheck: WakeLock { " + wl.mTag + " }, has been hold for " + hold2 + "s, pkgName=" + wl.mPackageName);
                                    }
                                } else {
                                    getInner().releaseWakeLockInternal(wl.mLock, wl.mFlags);
                                    index--;
                                    Slog.v(ATAG, "wakeLockHeldContinuouslyCheck: wakelock{" + wl.mTag + "} has held for " + hold2 + " s, Internally releasing it.");
                                }
                            }
                        }
                    }
                    index++;
                }
            }
            synchronized (this.mSyncWakeLock) {
                Iterator<String> it = tagSyncList.iterator();
                while (it.hasNext()) {
                    String tag = it.next();
                    if (!this.mSyncWakeLock.contains(tag)) {
                        this.mSyncWakeLock.add(tag);
                    }
                }
            }
            this.mHandler.sendEmptyMessageDelayed(msgContinuousCheck, this.mIntervalCheck);
            return;
        }
        return;
        while (true) {
        }
        while (true) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x007e, code lost:
        if (r9.mMonitorAudioDataStarted != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0080, code lost:
        r9.mMonitorAudioDataStarted = true;
        r2 = r9.mAlarmManager;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0085, code lost:
        if (r2 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0087, code lost:
        r2.setExact(2, android.os.SystemClock.elapsedRealtime() + com.android.server.power.ColorPartialWakeLockCheck.DELAY_START_MONITOR_AUDIO_DATA, "monitorAudioData", r9.mAudioDataAlarmListener, r9.mHandler);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    public void possiblePlayer(Message msg) {
        WakelockStats ws = (WakelockStats) msg.obj;
        if (this.needCheck) {
            if (this.DEBUG) {
                Slog.d(ATAG, "possiblePlayer: pkgName=" + ws.mPkgName);
            }
            synchronized (this.mPossibleMusicPlayerList) {
                if (!this.mPossibleMusicPlayerList.contains(ws.mPkgName)) {
                    this.mPossibleMusicPlayerList.add(ws.mPkgName);
                    if (this.ADBG) {
                        Slog.d(ATAG, "possiblePlayer: add " + ws.mPkgName);
                    }
                }
            }
        } else if (this.DEBUG) {
            Slog.d(ATAG, "possiblePlayer: needCheck is false. pkgName=" + ws.mPkgName);
        }
    }

    public void logSwitch(boolean enable) {
        this.DEBUG = enable;
    }

    private void reportSingleWasteWakeLockForceStop(String tag, String pkgName, long holdTime) {
        SingleWasteWakeLock singleWL = this.mSingleWasteWakeLock.get(pkgName);
        if (singleWL == null) {
            singleWL = new SingleWasteWakeLock(tag, pkgName, holdTime);
            this.mSingleWasteWakeLock.put(pkgName, singleWL);
        } else {
            singleWL.update(tag, holdTime);
        }
        if (!this.mSysState.isNotRestrictPkgWakeLock(pkgName)) {
            ArrayList<String> uidList = new ArrayList<>();
            String reportString = singleWL.getReportString();
            uidList.add(reportString);
            Intent intent = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
            intent.putStringArrayListExtra("data", uidList);
            intent.putExtra("type", "wakelock");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            if (this.ADBG) {
                Slog.w(ATAG, "SingleWakeLockF: reportString=" + reportString);
            }
        }
    }

    private Bundle makeWakeLockData(PowerManagerService.WakeLock wl, WorkSource ws) {
        Bundle data = new Bundle();
        data.putString("tagName", wl.mTag);
        data.putString("pkgName", wl.mPackageName);
        data.putLong("now", SystemClock.uptimeMillis());
        data.putInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, wl.mOwnerUid);
        if (ws != null) {
            data.putParcelable("workSource", new WorkSource(ws));
        }
        return data;
    }

    private boolean ignoreCheck(PowerManagerService.WakeLock wl) {
        if ((wl.mFlags & 65535) != 1) {
            return true;
        }
        if (!TAG_ALARM.equals(wl.mTag) || wl.mOwnerUid != 1000) {
            return false;
        }
        return true;
    }

    private WakelockStats getWakelockStatsLocked(int uid, String pkgName) {
        WakelockStats ws = this.mWakelockStats.get(pkgName);
        if (ws != null) {
            return ws;
        }
        WakelockStats ws2 = new WakelockStats(uid, pkgName);
        this.mWakelockStats.put(pkgName, ws2);
        return ws2;
    }

    private void reportTimeoutWakeLockForceStop(WakelockStats wakeStats, long now) {
        String pkgName = wakeStats.mPkgName;
        long wakeSumMs = wakeStats.getWakeSumMs(now);
        if (wakeSumMs >= wakeStats.mThreshold) {
            if (this.ADBG) {
                Slog.d(ATAG, "TimeoutWakeLockF: pkgName=" + pkgName);
            }
            collectMusicPlayer(wakeStats);
            collectAudioInRecorder(wakeStats);
            if (FORCE_RELEASE_LIST.size() > 0) {
                for (String filterPkg : FORCE_RELEASE_LIST) {
                    if (TextUtils.equals(filterPkg, pkgName)) {
                        synchronized (this.mLock) {
                            for (int index = 0; index < this.mWakeLocks.size(); index++) {
                                PowerManagerService.WakeLock wl = this.mWakeLocks.get(index);
                                if ((wl.mFlags & 65535) == 1) {
                                    String wlpkgName = wl.mPackageName;
                                    if (wlpkgName == null) {
                                        wlpkgName = this.mUtil.getPkgNameForUid(wl.mOwnerUid);
                                    }
                                    if (wlpkgName != null && wlpkgName.equals(pkgName)) {
                                        getInner().releaseWakeLockInternal(wl.mLock, wl.mFlags);
                                        Slog.w(ATAG, "check: Internally releasing the wakelock{" + wl.mTag + "} acquired by " + pkgName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            wakeStats.mIsWastPower = true;
            this.mWastPowerWakelock.put(pkgName, wakeStats);
            wakeStats.mThreshold += this.mThrshWakeLockTimeOut;
            if (!this.mSysState.isNotRestrictPkgWakeLock(pkgName) && !handleAudioWorksourceNull(wakeStats, wakeSumMs)) {
                ArrayList<String> uidList = new ArrayList<>();
                String reportString = wakeStats.getReportString();
                uidList.add(reportString);
                Intent intent = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
                intent.putStringArrayListExtra("data", uidList);
                intent.putExtra("type", "wakelock");
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                if (this.ADBG) {
                    Slog.d(ATAG, "TimeoutWakeLockF: reportString=" + reportString);
                }
            }
        }
    }

    private boolean handleAudioWorksourceNull(WakelockStats wakeStats, long hold) {
        ActivityManager.RunningAppProcessInfo trackApp;
        boolean handled = false;
        if (wakeStats.mUid != 1041 || !wakeStats.hasAudioWLHeld()) {
            return false;
        }
        String[] pids = this.mUtil.getActiveAudioPids();
        if (pids != null) {
            for (int j = 0; j < pids.length; j++) {
                if (!pids[j].isEmpty() && (trackApp = this.mUtil.getProcessForPid(pids[j])) != null) {
                    reportSingleWasteWakeLockForceStop(TAG_AUDIOMIX, trackApp.processName, hold);
                    handled = true;
                }
            }
        }
        if (handled) {
            return handled;
        }
        int i = this.mCntAudioWorksourceNull + 1;
        this.mCntAudioWorksourceNull = i;
        if (i < 3) {
            return true;
        }
        return handled;
    }

    private void collectMusicPlayer(WakelockStats wakeStats) {
        if (this.mAudioState.get() == 2 && isPossiblePlayer(wakeStats.mPkgName) && wakeStats.hasAudioWLHeld()) {
            synchronized (this.mMusicPlayerList) {
                if (!this.mMusicPlayerList.contains(wakeStats.mPkgName)) {
                    this.mMusicPlayerList.add(wakeStats.mPkgName);
                    if (this.ADBG) {
                        Slog.d(ATAG, "MusicPlayer " + wakeStats.mPkgName);
                    }
                }
            }
            synchronized (this.mMusicPlayerUids) {
                if (!this.mMusicPlayerUids.contains(Integer.valueOf(wakeStats.mUid))) {
                    this.mMusicPlayerUids.add(Integer.valueOf(wakeStats.mUid));
                }
            }
        }
    }

    private ArrayList<String> getTimeoutWakeLock() {
        ArrayList<String> uidList = new ArrayList<>();
        int len = this.mWastPowerWakelock.size();
        for (int i = 0; i < len; i++) {
            String reportString = this.mWastPowerWakelock.valueAt(i).getReportString();
            uidList.add(reportString);
            if (this.ADBG) {
                Slog.w(ATAG, "TimeoutWakeLock: " + reportString);
            }
        }
        int len2 = this.mSingleWasteWakeLock.size();
        for (int i2 = 0; i2 < len2; i2++) {
            String reportString2 = this.mSingleWasteWakeLock.valueAt(i2).getReportString();
            uidList.add(reportString2);
            if (this.ADBG) {
                Slog.w(ATAG, "TimeoutWakeLock: " + reportString2);
            }
        }
        this.mWastPowerWakelock.clear();
        this.mSingleWasteWakeLock.clear();
        return uidList;
    }

    public void onDeviceIdle() {
        this.mIsDeepIdle = true;
        maybeReleaseSportFitness();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0024, code lost:
        if (r0 >= r1.size()) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0026, code lost:
        r2 = r1.get(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
        if (r7.ADBG == false) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
        android.util.Slog.d(com.android.server.power.ColorPartialWakeLockCheck.ATAG, "wlist sports wl R: pkg=" + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0046, code lost:
        r3 = r7.mUtil.getUidForPkgName(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004c, code lost:
        if (r3 >= 0) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0050, code lost:
        if (r7.ADBG == false) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0052, code lost:
        android.util.Slog.d(com.android.server.power.ColorPartialWakeLockCheck.ATAG, "wlist sports wl R: ignore. uid=" + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0069, code lost:
        confirmWakelockRelease(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006e, code lost:
        if (r3 < 10000) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0070, code lost:
        r4 = r7.mListForbidHeldWakelock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0072, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r7.mListForbidHeldWakelock.add(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0078, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0079, code lost:
        r5 = r7.mUidsForbidHeldWakelock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007b, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r7.mUidsForbidHeldWakelock.put(r3, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0082, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x008a, code lost:
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x008d, code lost:
        return;
     */
    private void maybeReleaseSportFitness() {
        if (this.mIsDeepIdle) {
            synchronized (this.mSportsFitness) {
                if (!this.mSportsFitness.isEmpty()) {
                    ArrayList<String> sportsFitness = new ArrayList<>(this.mSportsFitness);
                    this.mSportsFitness.clear();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0022, code lost:
        r2 = r7.mUidsForbidHeldWakelock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0024, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r0 = r7.mUidsForbidHeldWakelock.clone();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002b, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002c, code lost:
        if (r11 == null) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002e, code lost:
        r2 = r11.size();
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0033, code lost:
        if (r3 >= r2) goto L_0x00b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0039, code lost:
        if (r11.getName(r3) == null) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0043, code lost:
        if (r1.contains(r11.getName(r3)) == false) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0045, code lost:
        sendWakelockAcqireShortTimeMsg(r8, r10, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004a, code lost:
        if (r7.ADBG == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004c, code lost:
        android.util.Slog.d(com.android.server.power.ColorPartialWakeLockCheck.ATAG, "Shortime: ws pkg=" + r11.getName(r3));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0067, code lost:
        r4 = r11.get(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006f, code lost:
        if (r0.get(r4) == false) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0071, code lost:
        sendWakelockAcqireShortTimeMsg(r8, r10, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0076, code lost:
        if (r7.ADBG == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0078, code lost:
        android.util.Slog.d(com.android.server.power.ColorPartialWakeLockCheck.ATAG, "Shortime: ws uid=" + r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008f, code lost:
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0097, code lost:
        if (r1.contains(r9) == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0099, code lost:
        sendWakelockAcqireShortTimeMsg(r8, r10, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x009e, code lost:
        if (r7.ADBG == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a0, code lost:
        android.util.Slog.d(com.android.server.power.ColorPartialWakeLockCheck.ATAG, "Shortime: pkg=" + r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        return;
     */
    public void allowAcquireShortimeHandle(IBinder lock, String pkgRelease, int flags, WorkSource ws, int ownerUid) {
        if ((65535 & flags) == 1 && ownerUid >= 10000) {
            synchronized (this.mListForbidHeldWakelock) {
                if (!this.mListForbidHeldWakelock.isEmpty()) {
                    ArrayList<String> pkgsNotAllow = new ArrayList<>(this.mListForbidHeldWakelock);
                }
            }
        }
    }

    private void sendWakelockAcqireShortTimeMsg(IBinder lock, int flags, String pkgName) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 8;
        Bundle data = new Bundle();
        data.putBinder("wlBinder", lock);
        data.putInt("flags", flags);
        data.putString("pkgName", pkgName);
        msg.setData(data);
        this.mHandler.sendMessageDelayed(msg, SHORTTIME_WAKELOCK_ACQUIRE_TIME);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void whitelistWakelockRelease(String pkg) {
        if (pkg != null) {
            if (this.ADBG) {
                Slog.d(ATAG, "wlist wl R: in. pkg=" + pkg);
            }
            WakelockStats wakeStats = this.mWakelockStats.get(pkg);
            if (wakeStats != null) {
                long now = SystemClock.uptimeMillis();
                long nowElapsed = SystemClock.elapsedRealtime();
                long wakeSumMs = wakeStats.getWakeSumMs(now);
                long screenoffDuration = nowElapsed - this.mTimeStampScreenoff;
                if (wakeSumMs >= this.mThrshWakeLockTimeOut * 2 || 10 * wakeSumMs >= screenoffDuration) {
                    int uid = this.mUtil.getUidForPkgName(pkg);
                    if (uid < 0) {
                        if (this.ADBG) {
                            Slog.d(ATAG, "wlist wl R: ignore. uid=" + uid);
                        }
                    } else if (this.mSysState.isSensorUsedEver(uid, 1) || this.mSysState.isSensorUsedEver(uid, 19)) {
                        synchronized (this.mSportsFitness) {
                            if (!this.mSportsFitness.contains(pkg)) {
                                this.mSportsFitness.add(pkg);
                            }
                        }
                        if (this.ADBG) {
                            Slog.d(ATAG, "wlist wl R: is sports fitness. pkg=" + pkg);
                        }
                        maybeReleaseSportFitness();
                    } else {
                        confirmWakelockRelease(pkg);
                        if (uid >= 10000) {
                            synchronized (this.mListForbidHeldWakelock) {
                                this.mListForbidHeldWakelock.add(pkg);
                            }
                            synchronized (this.mUidsForbidHeldWakelock) {
                                this.mUidsForbidHeldWakelock.put(uid, true);
                            }
                        }
                    }
                } else if (this.ADBG) {
                    Slog.d(ATAG, "wlist wl R: wakelock held ratio is low. pkgName=" + pkg);
                }
            } else if (this.ADBG) {
                Slog.d(ATAG, "wlist wl R: wakeStats is null!!! pkgName=" + pkg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void confirmWakelockRelease(String pkgRelease) {
        if (pkgRelease != null) {
            if (this.ADBG) {
                Slog.d(ATAG, "confirm wl R: pkg=" + pkgRelease);
            }
            ArrayList<PowerManagerService.WakeLock> wakeLocksTmp = new ArrayList<>();
            synchronized (this.mLock) {
                int N = this.mWakeLocks.size();
                for (int i = 0; i < N; i++) {
                    wakeLocksTmp.add(getInner().cloneWakeLock(this.mWakeLocks.get(i)));
                }
            }
            for (int index = 0; index < wakeLocksTmp.size(); index++) {
                PowerManagerService.WakeLock wl = wakeLocksTmp.get(index);
                if ((wl.mFlags & 65535) == 1 && wl.mOwnerUid >= 10000) {
                    boolean matched = false;
                    if (wl.mWorkSource != null) {
                        WorkSource ws = wl.mWorkSource;
                        int size = ws.size();
                        for (int k = 0; k < size && !matched; k++) {
                            if (ws.getName(k) == null) {
                                String[] packages = this.mUtil.getPackagesForUid(ws.get(k));
                                if (packages != null) {
                                    int i2 = 0;
                                    while (true) {
                                        if (i2 >= packages.length) {
                                            break;
                                        } else if (pkgRelease.equals(packages[i2])) {
                                            matched = true;
                                            break;
                                        } else {
                                            i2++;
                                        }
                                    }
                                }
                            } else if (pkgRelease.equals(ws.getName(k))) {
                                matched = true;
                            }
                        }
                    } else if (pkgRelease.equals(wl.mPackageName)) {
                        matched = true;
                    }
                    if (matched) {
                        if (this.ADBG) {
                            Slog.w(ATAG, "Internally r {" + wl.mTag + "} acquired by " + pkgRelease);
                        }
                        getInner().releaseWakeLockInternal(wl.mLock, wl.mFlags);
                    }
                }
            }
        }
    }

    private void dumpAllWakeLockStats() {
        if (this.DEBUG) {
            for (int i = 0; i < this.mWakelockStats.size(); i++) {
                Slog.d(ATAG, "dumpAllWakeLockStats: reportString(" + i + ")=" + this.mWakelockStats.valueAt(i).getReportString());
            }
        }
    }

    private boolean isAudioWakelock(int actualOwnerUid, int directOwnerUid, String pkgName, String tagName) {
        if (LIST_AUIDO_IGNORE.contains(pkgName)) {
            return false;
        }
        if ((directOwnerUid != 1041 || !LIST_TAG_AUDIO_MEDIA_UID.contains(tagName)) && !LIST_TAG_AUDIO_APP.contains(tagName)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPossiblePlayer(String pkg) {
        synchronized (this.mPossibleMusicPlayerList) {
            if (this.mPossibleMusicPlayerList.contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    private void collectPossibleAudioInRecorder(int directOwnerUid, String pkgName, String tagName) {
        synchronized (this.mPossibleAudioInList) {
            if (!this.mPossibleAudioInList.contains(pkgName) && TAG_AUDIOIN.equals(tagName) && directOwnerUid == 1041) {
                this.mPossibleAudioInList.add(pkgName);
                if (this.ADBG) {
                    Slog.d(ATAG, "possible Audio In pkg" + pkgName + ", tagName=" + tagName);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0018, code lost:
        r0 = r4.mAudioInList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
        if (r4.mAudioInList.contains(r5.mPkgName) != false) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0027, code lost:
        r4.mAudioInList.add(r5.mPkgName);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0032, code lost:
        if (r4.ADBG == false) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0034, code lost:
        android.util.Slog.d(com.android.server.power.ColorPartialWakeLockCheck.ATAG, "Audio In Recorder pkg " + r5.mPkgName);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004e, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0016, code lost:
        if (r5.hasAudioInWLHeld() == false) goto L_?;
     */
    private void collectAudioInRecorder(WakelockStats wakeStats) {
        synchronized (this.mPossibleAudioInList) {
            if (!this.mPossibleAudioInList.contains(wakeStats.mPkgName)) {
            }
        }
    }

    public void dumpPossibleMusicPlayer(PrintWriter pw) {
        pw.println("mPossibleMusicPlayerList:");
        synchronized (this.mPossibleMusicPlayerList) {
            Iterator<String> it = this.mPossibleMusicPlayerList.iterator();
            while (it.hasNext()) {
                pw.println("PossibleMusicPlayer:" + it.next());
            }
        }
        synchronized (this.mMusicPlayerList) {
            Iterator<String> it2 = this.mMusicPlayerList.iterator();
            while (it2.hasNext()) {
                pw.println("MusicPlayer:" + it2.next());
            }
        }
        synchronized (this.mAudioInList) {
            Iterator<String> it3 = this.mAudioInList.iterator();
            while (it3.hasNext()) {
                pw.println("AudioInRecorder:" + it3.next());
            }
        }
        if (this.mAudioState.get() == 2) {
            pw.println("AudioState:Playback");
        } else if (this.mAudioState.get() == 1) {
            pw.println("AudioState:Silence");
        } else if (this.mAudioState.get() == 0) {
            pw.println("AudioState:Unknown");
        } else {
            pw.println("AudioState:Invalid");
        }
    }

    public void dumpCameraState(PrintWriter pw) {
        int uid = this.mSysState.getCameraWorkingUid();
        if (uid < 0) {
            pw.println("Camera is not working.");
            return;
        }
        pw.println("uid(" + uid + ") using Camera.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doCheckPossibleMusicPlayer() {
        this.cntSilence = 0;
        this.cntPlayback = 0;
        this.mAudioState.set(0);
        AlarmManager alarmManager = this.mAlarmManager;
        if (alarmManager != null) {
            alarmManager.cancel(this.mAudioDataAlarmListener);
        }
        monitorAudioData();
    }

    /* access modifiers changed from: private */
    public class SingleWasteWakeLock {
        long mHoldTime;
        String mPackageName;
        int mReportCnt;
        String mTag;

        public SingleWasteWakeLock(String tag, String pkgName, long holdTime) {
            this.mTag = tag;
            this.mPackageName = pkgName;
            this.mHoldTime = holdTime;
        }

        public void update(String tag, long holdTime) {
            this.mTag = tag;
            this.mHoldTime = holdTime;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getReportString() {
            SystemClock.uptimeMillis();
            long nowElapsed = SystemClock.elapsedRealtime();
            return "[ " + this.mPackageName + " ]    " + (this.mHoldTime / 1000) + "s    single    { " + this.mTag + " }    " + ((nowElapsed - ColorPartialWakeLockCheck.this.mTimeStampScreenoff) / 1000) + "s    ";
        }
    }

    /* access modifiers changed from: private */
    public class WakelockStats {
        private static final int MAX_TAG = 5;
        public boolean mIsWastPower = false;
        private String mPkgName = null;
        ArrayMap<String, Tag> mTag = new ArrayMap<>();
        public long mThreshold;
        private int mUid;
        private int mWakeNesting;
        private long mWakeStartMs;
        public long mWakeSumMs;

        public WakelockStats(int uid, String pkgName) {
            this.mUid = uid;
            this.mPkgName = pkgName;
            this.mThreshold = ColorPartialWakeLockCheck.this.mThrshWakeLockTimeOut;
        }

        private Tag getTag(String tagName) {
            Tag tag;
            synchronized (this.mTag) {
                tag = this.mTag.get(tagName);
                if (tag == null && this.mTag.size() < 5) {
                    tag = new Tag(tagName);
                    this.mTag.put(tagName, tag);
                }
            }
            return tag;
        }

        public void scheduleWakeLockTimeoutCheck(long now) {
            long delay = this.mThreshold - getWakeSumMs(now);
            if (delay < 0) {
                delay = 0;
            }
            Message msg = ColorPartialWakeLockCheck.this.mHandler.obtainMessage(7, this);
            ColorPartialWakeLockCheck.this.mHandler.removeMessages(7, this);
            ColorPartialWakeLockCheck.this.mHandler.sendMessageDelayed(msg, delay);
            if (ColorPartialWakeLockCheck.this.DEBUG) {
                Slog.w(ColorPartialWakeLockCheck.ATAG, "scheduleWakeLockTimeoutCheck:  mPkgName=" + this.mPkgName + ", mThreshold=" + this.mThreshold + ", mWakeSumMs=" + this.mWakeSumMs + ",getWakeSumMs(now)=" + getWakeSumMs(now) + ",delay=" + delay);
            }
        }

        public void startWakeLock(String tagName, long now, boolean isAudio) {
            int i = this.mWakeNesting;
            this.mWakeNesting = i + 1;
            if (i == 0) {
                this.mWakeStartMs = now;
                if (!ColorPartialWakeLockCheck.this.mSysState.isNotRestrictPkgWakeLock(this.mPkgName) || ColorPartialWakeLockCheck.FORCE_RELEASE_LIST.contains(this.mPkgName)) {
                    scheduleWakeLockTimeoutCheck(now);
                }
            }
            if (isAudio && !ColorPartialWakeLockCheck.this.isPossiblePlayer(this.mPkgName)) {
                if (ColorPartialWakeLockCheck.this.DEBUG) {
                    Slog.d(ColorPartialWakeLockCheck.ATAG, "startWakeLock: schedule possible player. mPkgName=" + this.mPkgName + ", tagName=" + tagName);
                }
                Message msg = ColorPartialWakeLockCheck.this.mHandler.obtainMessage(9, this);
                ColorPartialWakeLockCheck.this.mHandler.removeMessages(9, this);
                ColorPartialWakeLockCheck.this.mHandler.sendMessageDelayed(msg, ColorPartialWakeLockCheck.DELAY_POSSIBLE_PLAYER);
            }
            synchronized (this.mTag) {
                Tag tag = getTag(tagName);
                if (tag != null) {
                    tag.startWakeLock(now);
                }
            }
        }

        public void stopWakeLock(String tagName, long now) {
            int i = this.mWakeNesting;
            if (i > 0) {
                this.mWakeNesting = i - 1;
                if (i == 1) {
                    ColorPartialWakeLockCheck.this.mHandler.removeMessages(7, this);
                    ColorPartialWakeLockCheck.this.mHandler.removeMessages(9, this);
                    long j = this.mWakeStartMs;
                    if (j != 0) {
                        long delta = now - j;
                        if (delta > 0 && !ignoreWakeLockTag(tagName)) {
                            this.mWakeSumMs += delta;
                        } else if (delta < 0 && ColorPartialWakeLockCheck.this.ADBG) {
                            Slog.w(ColorPartialWakeLockCheck.ATAG, "stopWakeLock: tagName=" + tagName + ", delta=" + delta);
                        }
                        if (ColorPartialWakeLockCheck.this.DEBUG) {
                            Slog.w(ColorPartialWakeLockCheck.ATAG, "stopWakeLock: mPkgName=" + this.mPkgName + ", mUid=" + this.mUid + ", mWakeSumMs=" + this.mWakeSumMs + ", delta=" + delta);
                        }
                        this.mWakeStartMs = 0;
                    } else if (ColorPartialWakeLockCheck.this.ADBG) {
                        Slog.w(ColorPartialWakeLockCheck.ATAG, "stopWakeLock: tagName=" + tagName + ", mWakeStartMs=0, ignore!!!");
                    }
                }
            }
            synchronized (this.mTag) {
                Tag tag = this.mTag.get(tagName);
                if (tag != null) {
                    tag.stopWakeLock(now);
                }
            }
        }

        public long getWakeSumMs(long now) {
            long sum = this.mWakeSumMs;
            if (this.mWakeNesting <= 0) {
                return sum;
            }
            long delta = now - this.mWakeStartMs;
            if (delta > 0) {
                return sum + delta;
            }
            return sum;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getReportString() {
            long now = SystemClock.uptimeMillis();
            long nowElapsed = SystemClock.elapsedRealtime();
            StringBuilder sb = new StringBuilder();
            sb.append("[ ");
            sb.append(this.mPkgName);
            sb.append(" ]    ");
            sb.append(getWakeSumMs(now) / 1000);
            sb.append("s    ");
            synchronized (this.mTag) {
                int NS = this.mTag.size();
                for (int is = 0; is < NS; is++) {
                    Tag tag = this.mTag.valueAt(is);
                    sb.append("{ ");
                    sb.append(this.mTag.keyAt(is));
                    sb.append(" ");
                    sb.append(tag.getWakeSumMs(now) / 1000);
                    sb.append("s ");
                    sb.append(tag.count);
                    sb.append("counts }    ");
                }
            }
            sb.append((nowElapsed - ColorPartialWakeLockCheck.this.mTimeStampScreenoff) / 1000);
            sb.append("s    ");
            return sb.toString();
        }

        public boolean hasAudioWLHeld() {
            synchronized (this.mTag) {
                long now = SystemClock.uptimeMillis();
                int NS = this.mTag.size();
                for (int is = 0; is < NS; is++) {
                    Tag tag = this.mTag.valueAt(is);
                    if ((ColorPartialWakeLockCheck.LIST_TAG_AUDIO_MEDIA_UID.contains(tag.mTagName) || ColorPartialWakeLockCheck.LIST_TAG_AUDIO_APP.contains(tag.mTagName)) && tag.getWakeSumMs(now) > 30000) {
                        return true;
                    }
                }
                return false;
            }
        }

        public boolean hasAudioInWLHeld() {
            synchronized (this.mTag) {
                long now = SystemClock.uptimeMillis();
                int NS = this.mTag.size();
                for (int is = 0; is < NS; is++) {
                    Tag tag = this.mTag.valueAt(is);
                    if (ColorPartialWakeLockCheck.TAG_AUDIOIN.equals(tag.mTagName) && tag.getWakeSumMs(now) > 30000) {
                        return true;
                    }
                }
                return false;
            }
        }

        public boolean ignoreWakeLockTag(String tagName) {
            String str = this.mPkgName;
            if (str == null || tagName == null || !str.equals("com.android.systemui") || !tagName.equals(ColorPartialWakeLockCheck.TAG_AOD_WAKELOCK)) {
                return false;
            }
            if (!ColorPartialWakeLockCheck.this.ADBG) {
                return true;
            }
            Slog.w(ColorPartialWakeLockCheck.ATAG, "ignoreWakeLockTag: tagName=" + tagName + ", mPkgName=" + this.mPkgName);
            return true;
        }

        /* access modifiers changed from: package-private */
        public class Tag {
            int count;
            long mHeldTime;
            int mNesting;
            long mStartMs;
            String mTagName;

            public Tag(String TagName) {
                this.mTagName = TagName;
            }

            public void startWakeLock(long now) {
                this.count++;
                int i = this.mNesting;
                this.mNesting = i + 1;
                if (i == 0) {
                    this.mStartMs = now;
                }
            }

            public void stopWakeLock(long now) {
                int i = this.mNesting;
                if (i > 0) {
                    this.mNesting = i - 1;
                    if (i == 1) {
                        long j = this.mStartMs;
                        if (j != 0) {
                            long delta = now - j;
                            if (delta > 0) {
                                this.mHeldTime += delta;
                            } else if (delta < 0 && ColorPartialWakeLockCheck.this.ADBG) {
                                Slog.w(ColorPartialWakeLockCheck.ATAG, "tag stopWakeLock: mTagName=" + this.mTagName + ", delta=" + delta);
                            }
                            this.mStartMs = 0;
                        } else if (ColorPartialWakeLockCheck.this.ADBG) {
                            Slog.w(ColorPartialWakeLockCheck.ATAG, "tag stopWakeLock: mTagName=" + this.mTagName + ", mStartMs=0, ignore!!!");
                        }
                    }
                }
            }

            public long getWakeSumMs(long now) {
                long sum = this.mHeldTime;
                if (this.mNesting <= 0) {
                    return sum;
                }
                long delta = now - this.mStartMs;
                if (delta > 0) {
                    return sum + delta;
                }
                return sum;
            }
        }
    }

    private IColorPowerManagerServiceInner getInner() {
        OppoBasePowerManagerService basePms;
        PowerManagerService powerManagerService = this.mPms;
        if (powerManagerService == null || (basePms = (OppoBasePowerManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePowerManagerService.class, powerManagerService)) == null || basePms.mColorPowerMSInner == null) {
            return IColorPowerManagerServiceInner.DEFAULT;
        }
        return basePms.mColorPowerMSInner;
    }
}
