package com.android.server.power;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.mediatek.appworkingset.AWSDBHelper.PackageProcessList;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class OppoPartialWakeLockCheck {
    private static final String ACTION_OPPO_CONFIRM_WAKELOCK_RELEASE = "android.intent.action.OPPO_CONFIRM_WAKELOCK_RELEASE";
    private static final String ACTION_OPPO_GUARDELF_AUDIO_STATE_DETECTED = "android.intent.action.OPPO_GUARDELF_AUDIO_STATE_DETECTED";
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR = "android.intent.action.OPPO_GUARD_ELF_MONITOR";
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    private static final String ATAG = "OppoWakeLockCheck";
    private static final int AUDIO_STATE_INVALID = -1;
    private static final int AUDIO_STATE_PLAYBACK = 2;
    private static final int AUDIO_STATE_SILENCE = 1;
    private static final int AUDIO_STATE_UNKNOWN = 0;
    private static final long DELAY_POSSIBLE_PLAYER = 10000;
    private static final long DELAY_START_MONITOR_AUDIO_DATA = 20000;
    public static final List<String> FORCE_RELEASE_LIST = null;
    private static final long INTERVAL_MONITOR_AUDIO_DATA = 90000;
    public static List<String> LIST_AUIDO_IGNORE = null;
    public static List<String> LIST_TAG_AUDIO_APP = null;
    public static List<String> LIST_TAG_AUDIO_MEDIA_UID = null;
    private static final String PACKAGE_NAME_SYSTEM = "android";
    private static final long PARTIAL_WAKELOCK_TIMEOUT_THRESHOLD = 300;
    private static final String TAG_ALARM = "*alarm*";
    private static final String TAG_AUDIOIN = "AudioIn";
    private static final String TAG_AUDIOMIX = "AudioMix";
    private static final long THRESHOLD_MIN_AUDIO_HELD_DURATION = 30000;
    private final boolean ADBG;
    private final int AUDIO_WL_HELD_UID;
    private boolean DEBUG;
    private int cntPlayback;
    private int cntSilence;
    private AlarmManager mAlarmManager;
    private final OnAlarmListener mAudioDataAlarmListener;
    private AtomicInteger mAudioState;
    private BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private Handler mHandler;
    private ArrayList<String> mIgnorePkgList;
    private long mIntervalCheck;
    private final Object mLock;
    private final Object mLockPartialWL;
    private boolean mMonitorAudioDataStarted;
    private ArrayList<String> mMusicPlayerList;
    private final PowerManagerService mPms;
    private ArrayList<String> mPossibleMusicPlayerList;
    private ArrayMap<String, SingleWasteWakeLock> mSingleWasteWakeLock;
    private ArrayList<String> mSyncWakeLock;
    private OppoSysStateManager mSysState;
    private long mThrshWakeLockTimeOut;
    private long mTimeStampScreenoff;
    private final CommonUtil mUtil;
    private final ArrayList<WakeLock> mWakeLocks;
    private ArrayMap<String, WakelockStats> mWakelockStats;
    private ArrayMap<String, WakelockStats> mWastPowerWakelock;
    private boolean needCheck;

    private class SingleWasteWakeLock {
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

        private String getReportString() {
            long now = SystemClock.uptimeMillis();
            long nowElapsed = SystemClock.elapsedRealtime();
            StringBuilder sb = new StringBuilder();
            sb.append("[ ").append(this.mPackageName).append(" ]    ").append(this.mHoldTime / 1000).append("s    single    ").append("{ ").append(this.mTag).append(" }    ");
            sb.append((nowElapsed - OppoPartialWakeLockCheck.this.mTimeStampScreenoff) / 1000).append("s    ");
            return sb.toString();
        }
    }

    private class WakelockStats {
        private static final int MAX_TAG = 5;
        public boolean mIsWastPower = false;
        private String mPkgName = null;
        ArrayMap<String, Tag> mTag = new ArrayMap();
        public long mThreshold;
        private int mUid;
        private int mWakeNesting;
        private long mWakeStartMs;
        public long mWakeSumMs;

        class Tag {
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
                if (this.mNesting > 0) {
                    int i = this.mNesting;
                    this.mNesting = i - 1;
                    if (i == 1) {
                        if (this.mStartMs == 0) {
                            if (OppoPartialWakeLockCheck.this.ADBG) {
                                Slog.w("OppoWakeLockCheck", "tag stopWakeLock: mTagName=" + this.mTagName + ", mStartMs=0, ignore!!!");
                            }
                            return;
                        }
                        long delta = now - this.mStartMs;
                        if (delta > 0) {
                            this.mHeldTime += delta;
                        } else if (delta < 0 && OppoPartialWakeLockCheck.this.ADBG) {
                            Slog.w("OppoWakeLockCheck", "tag stopWakeLock: mTagName=" + this.mTagName + ", delta=" + delta);
                        }
                        this.mStartMs = 0;
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

        public WakelockStats(int uid, String pkgName) {
            this.mUid = uid;
            this.mPkgName = pkgName;
            this.mThreshold = OppoPartialWakeLockCheck.this.mThrshWakeLockTimeOut;
        }

        private Tag getTag(String tagName) {
            Tag tag;
            synchronized (this.mTag) {
                tag = (Tag) this.mTag.get(tagName);
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
            Message msg = OppoPartialWakeLockCheck.this.mHandler.obtainMessage(7, this);
            OppoPartialWakeLockCheck.this.mHandler.removeMessages(7, this);
            OppoPartialWakeLockCheck.this.mHandler.sendMessageDelayed(msg, delay);
            if (OppoPartialWakeLockCheck.this.DEBUG) {
                Slog.w("OppoWakeLockCheck", "scheduleWakeLockTimeoutCheck:  mPkgName=" + this.mPkgName + ", mThreshold=" + this.mThreshold + ", mWakeSumMs=" + this.mWakeSumMs + ",getWakeSumMs(now)=" + getWakeSumMs(now) + ",delay=" + delay);
            }
        }

        public void startWakeLock(String tagName, long now, boolean isAudio) {
            int i = this.mWakeNesting;
            this.mWakeNesting = i + 1;
            if (i == 0) {
                this.mWakeStartMs = now;
                if (!(OppoPartialWakeLockCheck.this.mSysState.isNotRestrictPkg(this.mPkgName) || OppoPartialWakeLockCheck.this.isInIgnoreList(this.mPkgName)) || OppoPartialWakeLockCheck.FORCE_RELEASE_LIST.contains(this.mPkgName)) {
                    scheduleWakeLockTimeoutCheck(now);
                }
            }
            if (isAudio && !OppoPartialWakeLockCheck.this.isPossiblePlayer(this.mPkgName)) {
                if (OppoPartialWakeLockCheck.this.DEBUG) {
                    Slog.d("OppoWakeLockCheck", "startWakeLock: schedule possible player. mPkgName=" + this.mPkgName + ", tagName=" + tagName);
                }
                Message msg = OppoPartialWakeLockCheck.this.mHandler.obtainMessage(9, this);
                OppoPartialWakeLockCheck.this.mHandler.removeMessages(9, this);
                OppoPartialWakeLockCheck.this.mHandler.sendMessageDelayed(msg, 10000);
            }
            synchronized (this.mTag) {
                Tag tag = getTag(tagName);
                if (tag != null) {
                    tag.startWakeLock(now);
                }
            }
        }

        public void stopWakeLock(String tagName, long now) {
            if (this.mWakeNesting > 0) {
                int i = this.mWakeNesting;
                this.mWakeNesting = i - 1;
                if (i == 1) {
                    OppoPartialWakeLockCheck.this.mHandler.removeMessages(7, this);
                    OppoPartialWakeLockCheck.this.mHandler.removeMessages(9, this);
                    if (this.mWakeStartMs != 0) {
                        long delta = now - this.mWakeStartMs;
                        if (delta > 0) {
                            this.mWakeSumMs += delta;
                        } else if (delta < 0 && OppoPartialWakeLockCheck.this.ADBG) {
                            Slog.w("OppoWakeLockCheck", "stopWakeLock: tagName=" + tagName + ", delta=" + delta);
                        }
                        if (OppoPartialWakeLockCheck.this.DEBUG) {
                            Slog.w("OppoWakeLockCheck", "stopWakeLock: mPkgName=" + this.mPkgName + ", mUid=" + this.mUid + ", mWakeSumMs=" + this.mWakeSumMs + ", delta=" + delta);
                        }
                        this.mWakeStartMs = 0;
                    } else if (OppoPartialWakeLockCheck.this.ADBG) {
                        Slog.w("OppoWakeLockCheck", "stopWakeLock: tagName=" + tagName + ", mWakeStartMs=0, ignore!!!");
                    }
                }
            }
            synchronized (this.mTag) {
                Tag tag = (Tag) this.mTag.get(tagName);
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

        private String getReportString() {
            long now = SystemClock.uptimeMillis();
            long nowElapsed = SystemClock.elapsedRealtime();
            StringBuilder sb = new StringBuilder();
            sb.append("[ ").append(this.mPkgName).append(" ]    ").append(getWakeSumMs(now) / 1000).append("s    ");
            synchronized (this.mTag) {
                int NS = this.mTag.size();
                for (int is = 0; is < NS; is++) {
                    Tag tag = (Tag) this.mTag.valueAt(is);
                    sb.append("{ ").append((String) this.mTag.keyAt(is)).append(" ").append(tag.getWakeSumMs(now) / 1000).append("s ").append(tag.count).append("counts }    ");
                }
            }
            sb.append((nowElapsed - OppoPartialWakeLockCheck.this.mTimeStampScreenoff) / 1000).append("s    ");
            return sb.toString();
        }

        public boolean hasAudioWLHeld() {
            synchronized (this.mTag) {
                long now = SystemClock.uptimeMillis();
                int NS = this.mTag.size();
                for (int is = 0; is < NS; is++) {
                    Tag tag = (Tag) this.mTag.valueAt(is);
                    if ((OppoPartialWakeLockCheck.LIST_TAG_AUDIO_MEDIA_UID.contains(tag.mTagName) || OppoPartialWakeLockCheck.LIST_TAG_AUDIO_APP.contains(tag.mTagName)) && tag.getWakeSumMs(now) > OppoPartialWakeLockCheck.THRESHOLD_MIN_AUDIO_HELD_DURATION) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.power.OppoPartialWakeLockCheck.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.power.OppoPartialWakeLockCheck.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.OppoPartialWakeLockCheck.<clinit>():void");
    }

    public OppoPartialWakeLockCheck(ArrayList<WakeLock> wakeLocks, Object lock, Context context, PowerManagerService pms, CommonUtil util, boolean dbg, Handler handler) {
        this.DEBUG = false;
        this.mLockPartialWL = new Object();
        this.needCheck = false;
        this.mThrshWakeLockTimeOut = 300000;
        this.mIntervalCheck = 300000;
        this.AUDIO_WL_HELD_UID = 1041;
        this.mWakelockStats = new ArrayMap();
        this.mWastPowerWakelock = new ArrayMap();
        this.mSingleWasteWakeLock = new ArrayMap();
        this.mSyncWakeLock = new ArrayList();
        this.mPossibleMusicPlayerList = new ArrayList();
        this.mMusicPlayerList = new ArrayList();
        this.mIgnorePkgList = new ArrayList();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(OppoPartialWakeLockCheck.ACTION_OPPO_CONFIRM_WAKELOCK_RELEASE)) {
                    OppoPartialWakeLockCheck.this.confirmWakelockRelease(intent.getStringExtra("package"));
                }
            }
        };
        this.mMonitorAudioDataStarted = false;
        this.mAudioState = new AtomicInteger(0);
        this.mAudioDataAlarmListener = new OnAlarmListener() {
            public void onAlarm() {
                OppoPartialWakeLockCheck.this.monitorAudioData();
            }
        };
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
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, "oppo.permission.OPPO_COMPONENT_SAFE", this.mHandler);
    }

    public boolean canSyncWakeLockAcq(int uid, String tag) {
        synchronized (this.mSyncWakeLock) {
            if (uid == 1000) {
                if (this.mSyncWakeLock.contains(tag)) {
                    Slog.w("OppoWakeLockCheck", "canSyncWakeLockAcq: tag{" + tag + "} can not Acquire, for it's detected waste power before!");
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

    public void noteWakeLockChange(WakeLock wl, int message, Handler handler, WorkSource ws) {
        synchronized (this.mLockPartialWL) {
            if (!this.needCheck) {
            } else if (ignoreCheck(wl)) {
            } else {
                Message msg = handler.obtainMessage();
                msg.what = message;
                msg.setData(makeWakeLockData(wl, ws));
                handler.sendMessage(msg);
            }
        }
    }

    public void wakeLockTimeout(Message msg) {
        WakelockStats ws = msg.obj;
        if (this.needCheck) {
            if (this.DEBUG) {
                Slog.w("OppoWakeLockCheck", "wakeLockTimeout: pkgName=" + ws.mPkgName);
            }
            long now = SystemClock.uptimeMillis();
            reportTimeoutWakeLockForceStop(ws, now);
            if (!(this.mSysState.isNotRestrictPkg(ws.mPkgName) || isInIgnoreList(ws.mPkgName))) {
                ws.scheduleWakeLockTimeoutCheck(now);
            }
            return;
        }
        if (this.DEBUG) {
            Slog.w("OppoWakeLockCheck", "wakeLockTimeout: needCheck is false. pkgName=" + ws.mPkgName);
        }
    }

    public void onScreenOff(int msgContinuousCheck) {
        if (this.DEBUG) {
            Slog.w("OppoWakeLockCheck", "onScreenoff: start");
        }
        this.mWakelockStats.clear();
        if (this.mSysState.isCharging()) {
            if (this.DEBUG) {
                Slog.w("OppoWakeLockCheck", "onScreenoff: isCharging. do nothing!!!");
            }
            return;
        }
        synchronized (this.mPossibleMusicPlayerList) {
            this.mPossibleMusicPlayerList.clear();
        }
        synchronized (this.mIgnorePkgList) {
            this.mIgnorePkgList.clear();
        }
        synchronized (this.mMusicPlayerList) {
            this.mMusicPlayerList.clear();
        }
        this.mMonitorAudioDataStarted = false;
        this.mAudioState.set(0);
        this.cntSilence = 0;
        this.cntPlayback = 0;
        ArrayList<Bundle> dataList = new ArrayList();
        synchronized (this.mLock) {
            for (int index = 0; index < this.mWakeLocks.size(); index++) {
                WakeLock wl = (WakeLock) this.mWakeLocks.get(index);
                if (!ignoreCheck(wl)) {
                    dataList.add(makeWakeLockData(wl, wl.mWorkSource));
                }
            }
            synchronized (this.mLockPartialWL) {
                this.needCheck = true;
                for (Bundle data : dataList) {
                    noteStartWakeLock(data);
                }
                this.mThrshWakeLockTimeOut = OppoGuardElfConfigUtil.getInstance().getThresholdWakeLockTimeout() * 1000;
                this.mIntervalCheck = this.mThrshWakeLockTimeOut;
                this.mTimeStampScreenoff = SystemClock.elapsedRealtime();
                this.mHandler.sendEmptyMessageDelayed(msgContinuousCheck, this.mIntervalCheck);
            }
        }
        if (this.DEBUG) {
            Slog.w("OppoWakeLockCheck", "onScreenoff: exit. mThrshWakeLockTimeOut=" + this.mThrshWakeLockTimeOut);
        }
    }

    /* JADX WARNING: Missing block: B:15:0x004d, code:
            if (r4.mAlarmManager == null) goto L_0x0056;
     */
    /* JADX WARNING: Missing block: B:16:0x004f, code:
            r4.mAlarmManager.cancel(r4.mAudioDataAlarmListener);
     */
    /* JADX WARNING: Missing block: B:18:0x0058, code:
            if (r4.DEBUG == false) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:19:0x005a, code:
            android.util.Slog.w("OppoWakeLockCheck", "onScreenon: exit");
     */
    /* JADX WARNING: Missing block: B:20:0x0063, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onScreenOn(int msgContinuousCheck) {
        if (this.DEBUG) {
            Slog.w("OppoWakeLockCheck", "onScreenon: start. needCheck=" + this.needCheck);
        }
        synchronized (this.mLockPartialWL) {
            if (this.needCheck) {
                this.needCheck = false;
                this.mHandler.removeMessages(7);
                this.mHandler.removeMessages(9);
                this.mHandler.removeMessages(msgContinuousCheck);
                dumpAllWakeLockStats();
                this.mWakelockStats.clear();
                ArrayList<String> uidList = getTimeoutWakeLock();
            }
        }
    }

    public void monitorAudioData() {
        String isSilence = ((AudioManager) this.mContext.getSystemService("audio")).getParameters("get_silence");
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(isSilence)) {
            this.cntSilence++;
            this.cntPlayback = 0;
        } else if ("0".equals(isSilence)) {
            this.cntPlayback++;
            this.cntSilence = 0;
        } else {
            this.mAudioState.set(-1);
            if (this.ADBG) {
                Slog.d("OppoWakeLockCheck", "monitorAudioData: get_silence not supported!");
            }
            return;
        }
        if (this.cntPlayback >= 3) {
            this.mAudioState.set(2);
            for (int i = 0; i < this.mWakelockStats.size(); i++) {
                collectMusicPlayer((WakelockStats) this.mWakelockStats.valueAt(i));
            }
            this.mContext.sendBroadcastAsUser(new Intent(ACTION_OPPO_GUARDELF_AUDIO_STATE_DETECTED), UserHandle.ALL);
        } else if (this.cntSilence >= 3) {
            this.mAudioState.set(1);
            this.mContext.sendBroadcastAsUser(new Intent(ACTION_OPPO_GUARDELF_AUDIO_STATE_DETECTED), UserHandle.ALL);
        } else if (this.mAlarmManager != null) {
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + INTERVAL_MONITOR_AUDIO_DATA, "monitorAudioData", this.mAudioDataAlarmListener, this.mHandler);
        }
        if (this.ADBG) {
            Slog.d("OppoWakeLockCheck", "monitorAudioData: cntPlayback=" + this.cntPlayback + ", cntSilence=" + this.cntSilence);
        }
    }

    public void noteStartWakeLock(Bundle data) {
        if (data != null && this.needCheck) {
            String tagName = data.getString("tagName");
            if (tagName != null) {
                long now = data.getLong("now");
                int ownerUid = data.getInt(PackageProcessList.KEY_UID);
                WorkSource ws = (WorkSource) data.getParcelable("workSource");
                String pkgName;
                WakelockStats wakeStats;
                if (ws != null) {
                    int size = ws.size();
                    for (int k = 0; k < size; k++) {
                        int uid = ws.get(k);
                        pkgName = ws.getName(k);
                        if (pkgName != null) {
                            wakeStats = getWakelockStatsLocked(uid, pkgName);
                            wakeStats.startWakeLock(tagName, now, isAudioWakelock(uid, ownerUid, pkgName, tagName));
                            ignoreForceStop(wakeStats, uid, ownerUid, pkgName, tagName);
                            if (this.DEBUG) {
                                Slog.w("OppoWakeLockCheck", "noteStartWakeLock: worksource name. pkgName=" + pkgName + ", tagName=" + tagName + ", uid=" + uid);
                            }
                        } else if (uid >= 10000) {
                            String[] packages = this.mUtil.getPackagesForUid(uid);
                            if (packages != null) {
                                for (String pkg : packages) {
                                    wakeStats = getWakelockStatsLocked(uid, pkg);
                                    wakeStats.startWakeLock(tagName, now, isAudioWakelock(uid, ownerUid, pkg, tagName));
                                    ignoreForceStop(wakeStats, uid, ownerUid, pkg, tagName);
                                    if (this.DEBUG) {
                                        Slog.w("OppoWakeLockCheck", "noteStartWakeLock: worksource uid. pkg=" + pkg + ", tagName=" + tagName + ", uid=" + uid);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    pkgName = data.getString("pkgName");
                    if (pkgName != null) {
                        wakeStats = getWakelockStatsLocked(ownerUid, pkgName);
                        wakeStats.startWakeLock(tagName, now, isAudioWakelock(ownerUid, ownerUid, pkgName, tagName));
                        ignoreForceStop(wakeStats, ownerUid, ownerUid, pkgName, tagName);
                        if (this.DEBUG) {
                            Slog.w("OppoWakeLockCheck", "noteStartWakeLock: pkgName=" + pkgName + ", tagName=" + tagName + ", uid=" + ownerUid);
                        }
                    }
                }
            }
        }
    }

    public void noteStopWakeLock(Bundle data) {
        if (data != null && this.needCheck) {
            String tagName = data.getString("tagName");
            if (tagName != null) {
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
                            if (uid >= 10000) {
                                String[] packages = this.mUtil.getPackagesForUid(uid);
                                if (packages != null) {
                                    for (String stopWakeLock : packages) {
                                        stopWakeLock(stopWakeLock, tagName, now);
                                    }
                                } else {
                                    return;
                                }
                            }
                            return;
                        }
                    }
                } else {
                    stopWakeLock(data.getString("pkgName"), tagName, now);
                }
            }
        }
    }

    private void stopWakeLock(String pkg, String tagName, long now) {
        if (pkg != null) {
            WakelockStats wakeStats = (WakelockStats) this.mWakelockStats.get(pkg);
            if (wakeStats == null) {
                if (this.ADBG) {
                    Slog.w("OppoWakeLockCheck", "stopWakeLock: wakeStats is null!!! pkgName=" + pkg);
                }
                return;
            }
            wakeStats.stopWakeLock(tagName, now);
            reportTimeoutWakeLockForceStop(wakeStats, now);
            if (this.DEBUG) {
                Slog.w("OppoWakeLockCheck", "stopWakeLock: pkg=" + pkg + ", tagName=" + tagName);
            }
        }
    }

    public void wakeLockHeldContinuouslyCheck(int msgContinuousCheck) {
        if (this.needCheck) {
            long now = SystemClock.uptimeMillis();
            ArrayList<String> tagSyncList = new ArrayList();
            synchronized (this.mLock) {
                int index = 0;
                while (index < this.mWakeLocks.size()) {
                    WakeLock wl = (WakeLock) this.mWakeLocks.get(index);
                    if ((wl.mFlags & 65535) == 1 && !wl.mDisabled) {
                        long hold = now - wl.mAcquireTime;
                        if (hold >= this.mThrshWakeLockTimeOut) {
                            hold /= 1000;
                            if (TAG_ALARM.equals(wl.mTag) && wl.mOwnerUid == 1000) {
                                this.mPms.releaseWakeLockByGuardElf(wl.mLock, wl.mFlags);
                                index--;
                                Slog.v("OppoWakeLockCheck", "wakeLockHeldContinuouslyCheck: wakelock{" + wl.mTag + "} has held for " + hold + " s, Internally releasing it.");
                            } else {
                                if (wl.mTag.startsWith("*sync*") && wl.mOwnerUid == 1000) {
                                    this.mPms.releaseWakeLockByGuardElf(wl.mLock, wl.mFlags);
                                    index--;
                                    tagSyncList.add(wl.mTag);
                                    Slog.v("OppoWakeLockCheck", "wakeLockHeldContinuouslyCheck: Internally releasing the wakelock{" + wl.mTag + "} acquired by SyncManager");
                                }
                                if (wl.mWorkSource != null) {
                                    int size = wl.mWorkSource.size();
                                    for (int k = 0; k < size; k++) {
                                        int uid = wl.mWorkSource.get(k);
                                        if (this.ADBG) {
                                            Slog.w("OppoWakeLockCheck", "wakeLockHeldContinuouslyCheck: has worksource. WakeLock { " + wl.mTag + " } has been hold for " + hold + "s, uids[" + k + "]=" + uid);
                                        }
                                    }
                                } else if (this.ADBG) {
                                    Slog.w("OppoWakeLockCheck", "wakeLockHeldContinuouslyCheck: WakeLock { " + wl.mTag + " }, has been hold for " + hold + "s, pkgName=" + wl.mPackageName);
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                    index++;
                }
            }
            synchronized (this.mSyncWakeLock) {
                for (String tag : tagSyncList) {
                    if (!this.mSyncWakeLock.contains(tag)) {
                        this.mSyncWakeLock.add(tag);
                    }
                }
            }
            this.mHandler.sendEmptyMessageDelayed(msgContinuousCheck, this.mIntervalCheck);
        }
    }

    /* JADX WARNING: Missing block: B:19:0x008a, code:
            if (r8.mMonitorAudioDataStarted != false) goto L_0x00a7;
     */
    /* JADX WARNING: Missing block: B:20:0x008c, code:
            r8.mMonitorAudioDataStarted = true;
     */
    /* JADX WARNING: Missing block: B:21:0x0091, code:
            if (r8.mAlarmManager == null) goto L_0x00a7;
     */
    /* JADX WARNING: Missing block: B:22:0x0093, code:
            r8.mAlarmManager.setExact(2, android.os.SystemClock.elapsedRealtime() + DELAY_START_MONITOR_AUDIO_DATA, "monitorAudioData", r8.mAudioDataAlarmListener, r8.mHandler);
     */
    /* JADX WARNING: Missing block: B:23:0x00a7, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void possiblePlayer(Message msg) {
        WakelockStats ws = msg.obj;
        if (this.needCheck) {
            if (this.DEBUG) {
                Slog.d("OppoWakeLockCheck", "possiblePlayer: pkgName=" + ws.mPkgName);
            }
            synchronized (this.mPossibleMusicPlayerList) {
                if (this.mPossibleMusicPlayerList.contains(ws.mPkgName)) {
                    return;
                }
                this.mPossibleMusicPlayerList.add(ws.mPkgName);
                if (this.ADBG) {
                    Slog.d("OppoWakeLockCheck", "possiblePlayer: add " + ws.mPkgName);
                }
            }
        } else {
            if (this.DEBUG) {
                Slog.d("OppoWakeLockCheck", "possiblePlayer: needCheck is false. pkgName=" + ws.mPkgName);
            }
        }
    }

    public void logSwitch(boolean enable) {
        this.DEBUG = enable;
    }

    private void reportSingleWasteWakeLockForceStop(String tag, String pkgName, long holdTime) {
        SingleWasteWakeLock singleWL = (SingleWasteWakeLock) this.mSingleWasteWakeLock.get(pkgName);
        if (singleWL == null) {
            singleWL = new SingleWasteWakeLock(tag, pkgName, holdTime);
            this.mSingleWasteWakeLock.put(pkgName, singleWL);
        } else {
            singleWL.update(tag, holdTime);
        }
        if (!this.mSysState.isNotRestrictPkg(pkgName) && !isInIgnoreList(pkgName)) {
            ArrayList<String> uidList = new ArrayList();
            String reportString = singleWL.getReportString();
            uidList.add(reportString);
            Intent intent = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
            intent.putStringArrayListExtra("data", uidList);
            intent.putExtra(SoundModelContract.KEY_TYPE, "wakelock");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            if (this.ADBG) {
                Slog.w("OppoWakeLockCheck", "SingleWakeLockF: reportString=" + reportString);
            }
        }
    }

    private Bundle makeWakeLockData(WakeLock wl, WorkSource ws) {
        Bundle data = new Bundle();
        data.putString("tagName", wl.mTag);
        data.putString("pkgName", wl.mPackageName);
        data.putLong("now", SystemClock.uptimeMillis());
        data.putInt(PackageProcessList.KEY_UID, wl.mOwnerUid);
        if (ws != null) {
            data.putParcelable("workSource", new WorkSource(ws));
        }
        return data;
    }

    private boolean ignoreCheck(WakeLock wl) {
        if ((wl.mFlags & 65535) != 1) {
            return true;
        }
        if (TAG_ALARM.equals(wl.mTag) && wl.mOwnerUid == 1000) {
            return true;
        }
        return false;
    }

    private WakelockStats getWakelockStatsLocked(int uid, String pkgName) {
        WakelockStats ws = (WakelockStats) this.mWakelockStats.get(pkgName);
        if (ws != null) {
            return ws;
        }
        ws = new WakelockStats(uid, pkgName);
        this.mWakelockStats.put(pkgName, ws);
        return ws;
    }

    /* JADX WARNING: Missing block: B:38:0x00ff, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void reportTimeoutWakeLockForceStop(WakelockStats wakeStats, long now) {
        String pkgName = wakeStats.mPkgName;
        long wakeSumMs = wakeStats.getWakeSumMs(now);
        if (wakeSumMs >= wakeStats.mThreshold) {
            if (this.ADBG) {
                Slog.d("OppoWakeLockCheck", "TimeoutWakeLockF: pkgName=" + pkgName);
            }
            collectMusicPlayer(wakeStats);
            if (FORCE_RELEASE_LIST.size() > 0) {
                for (String filterPkg : FORCE_RELEASE_LIST) {
                    if (TextUtils.equals(filterPkg, pkgName)) {
                        synchronized (this.mLock) {
                            for (int index = 0; index < this.mWakeLocks.size(); index++) {
                                WakeLock wl = (WakeLock) this.mWakeLocks.get(index);
                                if ((wl.mFlags & 65535) == 1) {
                                    String wlpkgName = wl.mPackageName;
                                    if (wlpkgName == null) {
                                        wlpkgName = this.mUtil.getPkgNameForUid(wl.mOwnerUid);
                                    }
                                    if (wlpkgName != null && wlpkgName.equals(pkgName)) {
                                        this.mPms.releaseWakeLockByGuardElf(wl.mLock, wl.mFlags);
                                        Slog.w("OppoWakeLockCheck", "check: Internally releasing the wakelock{" + wl.mTag + "} acquired by " + pkgName);
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
            if (!this.mSysState.isNotRestrictPkg(pkgName) && !isInIgnoreList(pkgName) && !handleAudioWorksourceNull(wakeStats, wakeSumMs)) {
                ArrayList<String> uidList = new ArrayList();
                String reportString = wakeStats.getReportString();
                uidList.add(reportString);
                Intent intent = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
                intent.putStringArrayListExtra("data", uidList);
                intent.putExtra(SoundModelContract.KEY_TYPE, "wakelock");
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                if (this.ADBG) {
                    Slog.d("OppoWakeLockCheck", "TimeoutWakeLockF: reportString=" + reportString);
                }
            }
        }
    }

    private boolean handleAudioWorksourceNull(WakelockStats wakeStats, long hold) {
        boolean handled = false;
        if (wakeStats.mUid == 1041 && wakeStats.hasAudioWLHeld()) {
            String[] pids = this.mUtil.getActiveAudioPids();
            if (pids == null) {
                return false;
            }
            for (int j = 0; j < pids.length; j++) {
                if (!pids[j].isEmpty()) {
                    RunningAppProcessInfo trackApp = this.mUtil.getProcessForPid(pids[j]);
                    if (trackApp != null) {
                        reportSingleWasteWakeLockForceStop(TAG_AUDIOMIX, trackApp.processName, hold);
                    }
                }
            }
            handled = true;
        }
        return handled;
    }

    /* JADX WARNING: Missing block: B:16:0x0057, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void collectMusicPlayer(WakelockStats wakeStats) {
        if (this.mAudioState.get() == 2 && this.mPossibleMusicPlayerList.contains(wakeStats.mPkgName) && wakeStats.hasAudioWLHeld()) {
            synchronized (this.mMusicPlayerList) {
                if (!this.mMusicPlayerList.contains(wakeStats.mPkgName)) {
                    this.mMusicPlayerList.add(wakeStats.mPkgName);
                    if (this.ADBG) {
                        Slog.d("OppoWakeLockCheck", "MusicPlayer " + wakeStats.mPkgName);
                    }
                }
            }
        }
    }

    private ArrayList<String> getTimeoutWakeLock() {
        int i;
        String reportString;
        ArrayList<String> uidList = new ArrayList();
        int len = this.mWastPowerWakelock.size();
        for (i = 0; i < len; i++) {
            reportString = ((WakelockStats) this.mWastPowerWakelock.valueAt(i)).getReportString();
            uidList.add(reportString);
            if (this.ADBG) {
                Slog.w("OppoWakeLockCheck", "TimeoutWakeLock: " + reportString);
            }
        }
        len = this.mSingleWasteWakeLock.size();
        for (i = 0; i < len; i++) {
            reportString = ((SingleWasteWakeLock) this.mSingleWasteWakeLock.valueAt(i)).getReportString();
            uidList.add(reportString);
            if (this.ADBG) {
                Slog.w("OppoWakeLockCheck", "TimeoutWakeLock: " + reportString);
            }
        }
        this.mWastPowerWakelock.clear();
        this.mSingleWasteWakeLock.clear();
        return uidList;
    }

    private void confirmWakelockRelease(String pkgRelease) {
        if (pkgRelease != null) {
            int i;
            if (this.ADBG) {
                Slog.d("OppoWakeLockCheck", "confirmWakelockRelease: pkgRelease=" + pkgRelease);
            }
            ArrayList<WakeLock> wakeLocksTmp = new ArrayList();
            synchronized (this.mLock) {
                int N = this.mWakeLocks.size();
                for (i = 0; i < N; i++) {
                    wakeLocksTmp.add(this.mPms.cloneWakeLock((WakeLock) this.mWakeLocks.get(i)));
                }
            }
            for (int index = 0; index < wakeLocksTmp.size(); index++) {
                WakeLock wl = (WakeLock) wakeLocksTmp.get(index);
                if ((wl.mFlags & 65535) == 1 && !wl.mDisabled) {
                    boolean matched = false;
                    if (wl.mWorkSource != null) {
                        WorkSource ws = wl.mWorkSource;
                        int size = ws.size();
                        for (int k = 0; k < size && !matched; k++) {
                            if (ws.getName(k) == null) {
                                String[] packages = this.mUtil.getPackagesForUid(ws.get(k));
                                if (packages != null) {
                                    for (Object equals : packages) {
                                        if (pkgRelease.equals(equals)) {
                                            matched = true;
                                            break;
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
                            Slog.w("OppoWakeLockCheck", "CONFIRM_WAKELOCK_RELEASE: Internally releasing the wakelock{" + wl.mTag + "} acquired by " + pkgRelease);
                        }
                        this.mPms.releaseWakeLockByGuardElf(wl.mLock, wl.mFlags);
                    }
                }
            }
        }
    }

    private void dumpAllWakeLockStats() {
        if (this.DEBUG) {
            for (int i = 0; i < this.mWakelockStats.size(); i++) {
                Slog.d("OppoWakeLockCheck", "dumpAllWakeLockStats: reportString(" + i + ")=" + ((WakelockStats) this.mWakelockStats.valueAt(i)).getReportString());
            }
        }
    }

    private boolean isAudioWakelock(int actualOwnerUid, int directOwnerUid, String pkgName, String tagName) {
        if (LIST_AUIDO_IGNORE.contains(pkgName)) {
            return false;
        }
        boolean isAudio = false;
        if (directOwnerUid == 1041 && LIST_TAG_AUDIO_MEDIA_UID.contains(tagName)) {
            isAudio = true;
        } else if (LIST_TAG_AUDIO_APP.contains(tagName)) {
            isAudio = true;
        }
        return isAudio;
    }

    private boolean isPossiblePlayer(String pkg) {
        synchronized (this.mPossibleMusicPlayerList) {
            if (this.mPossibleMusicPlayerList.contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    private void ignoreForceStop(WakelockStats wakeStats, int actualOwnerUid, int directOwnerUid, String pkgName, String tagName) {
        synchronized (this.mIgnorePkgList) {
            if (!this.mIgnorePkgList.contains(pkgName) && TAG_AUDIOIN.equals(tagName) && directOwnerUid == 1041) {
                this.mIgnorePkgList.add(pkgName);
                if (this.DEBUG) {
                    Slog.d("OppoWakeLockCheck", "ignoreForceStop pkgName=" + pkgName + ", tagName=" + tagName);
                }
            }
        }
    }

    private boolean isInIgnoreList(String pkg) {
        synchronized (this.mIgnorePkgList) {
            if (this.mIgnorePkgList.contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    public void dumpPossibleMusicPlayer(PrintWriter pw) {
        pw.println("mPossibleMusicPlayerList:");
        synchronized (this.mPossibleMusicPlayerList) {
            for (String pkgName : this.mPossibleMusicPlayerList) {
                pw.println("PossibleMusicPlayer:" + pkgName);
            }
        }
        synchronized (this.mMusicPlayerList) {
            for (String pkgName2 : this.mMusicPlayerList) {
                pw.println("MusicPlayer:" + pkgName2);
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
}
