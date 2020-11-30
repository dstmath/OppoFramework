package com.android.server.power;

import android.app.KeyguardManager;
import android.app.OppoActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.app.ColorAppInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import oppo.util.OppoStatistics;

public class ColorScreenOffOptimization implements IColorScreenOffOptimization {
    private static final int CNT_NO_PLAYBACK = 3;
    private static final long DELAY_CHECK_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED = 7000;
    private static final long DELAY_RECHECK_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED = 3000;
    private static final int KEYGUARD_LOCK_SCREENOFF_TIMEOUT_MAINTAIN = 2;
    private static final int KEYGUARD_LOCK_SCREENOFF_TIMEOUT_RECHECK = 3;
    private static final int KEYGUARD_LOCK_SCREENOFF_TIMEOUT_RESETTED = 1;
    private static List<String> LIST_TAG_AUDIO_APP = Arrays.asList("android.media.MediaPlayer");
    private static List<String> LIST_TAG_AUDIO_MEDIA_UID = Arrays.asList("AudioMix", "AudioDirectOut", "AudioOffload", "AudioDup", "AudioUnknown");
    private static final long SCREENOFF_TIMEOUT_KEYGUARD_LOCKED = 15000;
    private static final String START_MMI_TEST_CMD = "oppo.intent.action.START_OPPO_AT_SERVER";
    private static final String STOP_MMI_TEST_CMD = "oppo.intent.action.STOP_OPPO_AT_SERVER";
    private static ColorScreenOffOptimization mInstance = null;
    private final int AUDIO_WL_HELD_UID = 1041;
    private boolean DEBUG_PANIC = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private final String TAG = "ColorScreenOffOptimization";
    private AtomicInteger mCntNoPlayback = new AtomicInteger(0);
    private Context mContext;
    private Handler mHandler;
    private IColorPowerManagerServiceInner mInner;
    private boolean mIsMmiTesting = false;
    private KeyguardManager mKeyguardManager;
    private Object mLock;
    private final OppoActivityManager mOppoAm = new OppoActivityManager();
    private PowerManagerService mPowerMS;

    public static ColorScreenOffOptimization getInstance() {
        if (mInstance == null) {
            synchronized (ColorScreenOffOptimization.class) {
                if (mInstance == null) {
                    mInstance = new ColorScreenOffOptimization();
                }
            }
        }
        return mInstance;
    }

    private ColorScreenOffOptimization() {
    }

    public void initArgs(Context context, Object lock, PowerManagerService PowerMS, Handler handler, IColorPowerManagerServiceInner inner) {
        this.mContext = context;
        this.mLock = lock;
        this.mPowerMS = PowerMS;
        this.mHandler = handler;
        this.mInner = inner;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b1, code lost:
        if (r4 == false) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00b7, code lost:
        if (isSystemPlayback() == false) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00bb, code lost:
        if (r8.DEBUG_PANIC == false) goto L_0x00c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00bd, code lost:
        android.util.Slog.d("ColorScreenOffOptimization", "keyguardLockedScreenoffTimeout. is playback.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00c4, code lost:
        r9.put("palyback", "true");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00cb, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00d3, code lost:
        if (r8.mCntNoPlayback.incrementAndGet() >= 3) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00d5, code lost:
        return 3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00d6, code lost:
        r9.put("palyback", "false");
        r1 = r8.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00df, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00e0, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00e3, code lost:
        if (r8.mInner == null) goto L_0x00f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00e5, code lost:
        r8.mInner.setUserActivityValue((long) com.android.server.power.ColorScreenOffOptimization.SCREENOFF_TIMEOUT_KEYGUARD_LOCKED);
        r3 = r8.mInner.getMsgUserActivityTimeoutValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00f3, code lost:
        r4 = r8.mHandler.obtainMessage(r3);
        r4.setAsynchronous(true);
        r8.mHandler.sendMessage(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0104, code lost:
        if (r8.DEBUG_PANIC == false) goto L_0x010d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0106, code lost:
        android.util.Slog.d("ColorScreenOffOptimization", "keyguardLockedScreenoffTimeout. reset");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x010d, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x010e, code lost:
        return 1;
     */
    private int keyguardLockedScreenoffTimeout(HashMap<String, String> eventMap) {
        IColorPowerManagerServiceInner iColorPowerManagerServiceInner = this.mInner;
        if (iColorPowerManagerServiceInner != null && !iColorPowerManagerServiceInner.isInteractiveInternal()) {
            if (this.DEBUG_PANIC) {
                Slog.d("ColorScreenOffOptimization", "keyguardLockedScreenoffTimeout, is screen off.");
            }
            eventMap.put("screenOff", "true");
            return 2;
        } else if (TelephonyManager.from(this.mContext).getCallState() != 0) {
            if (this.DEBUG_PANIC) {
                Slog.d("ColorScreenOffOptimization", "keyguardLockedScreenoffTimeout. call state is not idle.");
            }
            eventMap.put("isPhoneInCall", "true");
            return 2;
        } else {
            eventMap.put("isPhoneInCall", "false");
            List<String> listTopPkg = getAllTopPkgName();
            if (this.DEBUG_PANIC) {
                Slog.d("ColorScreenOffOptimization", "keyguardLockedScreenoffTimeout: listTopPkg=" + listTopPkg);
            }
            if (listTopPkg == null || (!listTopPkg.contains("com.coloros.pictorial") && !listTopPkg.contains("com.heytap.pictorial"))) {
                eventMap.put("pictorial", "false");
                synchronized (this.mLock) {
                    if (this.mIsMmiTesting) {
                        if (this.DEBUG_PANIC) {
                            Slog.d("ColorScreenOffOptimization", "keyguardLockedScreenoffTimeout, MMI test.");
                        }
                        return 2;
                    } else if (!isScreenOffTimeoutNeedResetLocked(eventMap)) {
                        return 2;
                    } else {
                        boolean hasAudioWL = hasAudioWakelockLocked();
                    }
                }
            } else {
                if (this.DEBUG_PANIC) {
                    Slog.d("ColorScreenOffOptimization", "keyguardLockedScreenoffTimeout. pictorial.");
                }
                eventMap.put("pictorial", "true");
                return 2;
            }
        }
    }

    private boolean isScreenOffTimeoutNeedResetLocked(HashMap<String, String> eventMap) {
        IColorPowerManagerServiceInner iColorPowerManagerServiceInner = this.mInner;
        if (iColorPowerManagerServiceInner == null) {
            return true;
        }
        long sleepTimeout = iColorPowerManagerServiceInner.getSleepTimeoutLocked();
        long screenOffTimeout = this.mInner.getScreenOffTimeoutLocked(sleepTimeout);
        long mUserActivityTimeoutOverrideFromWindowManager = this.mInner.getUserActivityValue();
        long mScreenOffTimeoutSetting = this.mInner.getScreenOffTimeoutSettingValue();
        if (this.DEBUG_PANIC) {
            Slog.d("ColorScreenOffOptimization", "MSG_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED. sleepTimeout=" + sleepTimeout + ", screenOffTimeout=" + screenOffTimeout + ", mScreenOffTimeoutSetting=" + mScreenOffTimeoutSetting + ", mUserActivityTimeoutOverrideFromWindowManager=" + mUserActivityTimeoutOverrideFromWindowManager);
        }
        eventMap.put("activityTimeout", String.valueOf(mUserActivityTimeoutOverrideFromWindowManager));
        eventMap.put("screenOffTimeout", String.valueOf(screenOffTimeout));
        eventMap.put("screenOffTimeoutSetting", String.valueOf(mScreenOffTimeoutSetting));
        if (screenOffTimeout == mScreenOffTimeoutSetting && mUserActivityTimeoutOverrideFromWindowManager < 0 && screenOffTimeout > SCREENOFF_TIMEOUT_KEYGUARD_LOCKED) {
            return true;
        }
        if (!this.DEBUG_PANIC) {
            return false;
        }
        Slog.d("ColorScreenOffOptimization", "keyguardLockedScreenoffTimeout. normal.");
        return false;
    }

    private boolean hasAudioWakelockLocked() {
        boolean hasAudio = false;
        IColorPowerManagerServiceInner iColorPowerManagerServiceInner = this.mInner;
        if (iColorPowerManagerServiceInner != null) {
            int numWakeLocks = iColorPowerManagerServiceInner.getArrayListOfWakeLocks().size();
            int i = 0;
            while (true) {
                if (i >= numWakeLocks) {
                    break;
                }
                IColorWakeLockEx wl = (IColorWakeLockEx) this.mInner.getArrayListOfWakeLocks().get(i);
                int directOwnerUid = wl.getOwnerUid();
                String tagName = wl.getTagName();
                if ((wl.getWakeLockFlags() & 65535) == 1) {
                    if (directOwnerUid == 1041 && LIST_TAG_AUDIO_MEDIA_UID.contains(tagName)) {
                        hasAudio = true;
                        if (this.DEBUG_PANIC) {
                            Slog.d("ColorScreenOffOptimization", "hasAudioWakelockLocked: wl=" + wl);
                        }
                    } else if (LIST_TAG_AUDIO_APP.contains(tagName)) {
                        hasAudio = true;
                        if (this.DEBUG_PANIC) {
                            Slog.d("ColorScreenOffOptimization", "hasAudioWakelockLocked: wl=" + wl);
                        }
                    }
                }
                i++;
            }
        }
        return hasAudio;
    }

    private boolean isSystemPlayback() {
        boolean isPlayback = true;
        if ("1".equals(((AudioManager) this.mContext.getSystemService("audio")).getParameters("get_silence"))) {
            isPlayback = false;
        }
        if (this.DEBUG_PANIC) {
            Slog.d("ColorScreenOffOptimization", "isSystemPlayback: " + isPlayback);
        }
        return isPlayback;
    }

    private void uploadScreenoffTimeoutDcs(HashMap<String, String> eventMap, boolean resetted) {
        eventMap.put("resetTimeout", String.valueOf(resetted));
        eventMap.put("topApp", getTopAppName());
        OppoStatistics.onCommon(this.mContext, "20120", "Screenoff_timeout_reset", eventMap, false);
    }

    private String getTopAppName() {
        ComponentName cn = null;
        try {
            cn = this.mOppoAm.getTopActivityComponentName();
        } catch (RemoteException e) {
        }
        return cn != null ? cn.getPackageName() : "";
    }

    private final class OppoUserPresentReceiver extends BroadcastReceiver {
        private OppoUserPresentReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (ColorScreenOffOptimization.this.DEBUG_PANIC) {
                Slog.d("ColorScreenOffOptimization", "action:" + intent.getAction());
            }
            synchronized (ColorScreenOffOptimization.this.mLock) {
                if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                    ColorScreenOffOptimization.this.mHandler.removeMessages(101);
                    if (ColorScreenOffOptimization.this.mInner != null) {
                        ColorScreenOffOptimization.this.mInner.setKeyguardLockEverUnlockValue(true);
                    }
                } else if (ColorScreenOffOptimization.START_MMI_TEST_CMD.equals(intent.getAction())) {
                    ColorScreenOffOptimization.this.mIsMmiTesting = true;
                } else if (ColorScreenOffOptimization.STOP_MMI_TEST_CMD.equals(intent.getAction())) {
                    ColorScreenOffOptimization.this.mIsMmiTesting = false;
                }
            }
        }
    }

    private List<String> getAllTopPkgName() {
        try {
            OppoActivityManager mOppoAms = new OppoActivityManager();
            List<String> listTopPkg = new ArrayList<>();
            List<ColorAppInfo> listTopPkgTmp = mOppoAms.getAllTopAppInfos();
            if (listTopPkgTmp == null) {
                return null;
            }
            for (int i = 0; i < listTopPkgTmp.size(); i++) {
                String pkg = listTopPkgTmp.get(i).appInfo.packageName;
                if (pkg != null && !"".equals(pkg)) {
                    listTopPkg.add(pkg);
                }
            }
            if (this.DEBUG_PANIC) {
                Slog.i("ColorScreenOffOptimization", "getAllTopPkgName: listTopPkg=" + listTopPkg + ", listTopPkgTmp=" + listTopPkgTmp);
            }
            return listTopPkg;
        } catch (Exception e) {
            Slog.w("ColorScreenOffOptimization", "getAllTopPkgName exception");
            return null;
        }
    }

    public void registerOppoUserPresentReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction(START_MMI_TEST_CMD);
        filter.addAction(STOP_MMI_TEST_CMD);
        this.mContext.registerReceiver(new OppoUserPresentReceiver(), filter, null, this.mHandler);
    }

    public void onBootPhaseStep() {
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
    }

    public void handleScreenOffTimeOutKeyGuardLocked() {
        KeyguardManager keyguardManager = this.mKeyguardManager;
        if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
            HashMap<String, String> eventMap = new HashMap<>();
            int ret = keyguardLockedScreenoffTimeout(eventMap);
            if (1 == ret) {
                uploadScreenoffTimeoutDcs(eventMap, true);
            } else if (2 == ret) {
                uploadScreenoffTimeoutDcs(eventMap, false);
            } else if (3 == ret) {
                this.mHandler.sendEmptyMessageDelayed(101, DELAY_RECHECK_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED);
                if (this.DEBUG_PANIC) {
                    Slog.d("ColorScreenOffOptimization", "MSG_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED, recheck");
                }
            }
        } else if (this.DEBUG_PANIC) {
            Slog.d("ColorScreenOffOptimization", "MSG_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED, not in keyguard.");
        }
    }

    public void readySendScreenOffTimeOffMessage() {
        this.mCntNoPlayback.set(0);
        this.mHandler.sendEmptyMessageDelayed(101, DELAY_CHECK_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED);
    }

    public void removeScreenOffTimeOutMessage() {
        this.mHandler.removeMessages(101);
    }
}
