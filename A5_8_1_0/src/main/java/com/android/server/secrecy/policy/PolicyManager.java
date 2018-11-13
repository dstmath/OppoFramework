package com.android.server.secrecy.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Looper;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import com.android.server.secrecy.SecrecyConfig;
import com.android.server.secrecy.SecrecyService;
import com.android.server.secrecy.policy.SecrecySwitchHelper.ISwitchUpdateListener;
import com.android.server.secrecy.policy.util.LogUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;

public class PolicyManager implements ISwitchUpdateListener {
    public static final boolean POLICY_DECRYPTED = false;
    public static final boolean POLICY_ENCRYPTED = true;
    private static final String TAG = "SecrecyService.PolicyManager";
    private static final Object mLock = new Object();
    private static PolicyManager sInstance;
    private Context mContext;
    private int mCountDown;
    private DecryptTool mDecryptTool;
    private DownloadInfo mDownloadInfo;
    private boolean mIsEncryptAdb;
    private boolean mIsEncryptApp;
    private boolean mIsEncryptLog;
    private Looper mLooper;
    private ResetCountDownTimer mResetCountDownTimer;
    private final SecrecyConfig mSecrecyConfig = SecrecyConfig.getInstance();
    private SecrecyService mSecrecyService;
    private SecrecySwitchHelper mSecrecySwitchHelper;
    private boolean mSystemReady;
    private BroadcastReceiver mTimeTickReceiver;
    private int mTimeTicks;

    public static PolicyManager getInstance() {
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new PolicyManager();
            }
        }
        return sInstance;
    }

    public void setSecrecyService(Context context, SecrecyService secrecyService, Looper looper) {
        this.mLooper = looper;
        this.mContext = context;
        this.mSecrecyService = secrecyService;
        this.mDownloadInfo = new DownloadInfo();
        this.mDecryptTool = new DecryptTool(context, this);
        this.mResetCountDownTimer = new ResetCountDownTimer(this.mContext, this);
        this.mSecrecySwitchHelper = new SecrecySwitchHelper(this.mContext, this);
    }

    public void systemReady(boolean secrecySupport) {
        this.mSystemReady = true;
        this.mDownloadInfo.readDownloadInfo();
        initSecreyConfig();
        if (secrecySupport) {
            initUpdateBroadcastReceiver();
            LogUtil.d(TAG, "systemReady, initUpdateBroadcastReceiver");
        }
        this.mResetCountDownTimer.updateCountDownTime(this.mSecrecySwitchHelper.getMacTimeout(), this.mSecrecySwitchHelper.getIdTimeout());
    }

    private void initSecreyConfig() {
        this.mSecrecyConfig.setPolicyManager(this, this.mLooper);
        this.mSecrecyConfig.loadSecrecyConfig();
    }

    public void initUpdateBroadcastReceiver() {
        this.mSecrecySwitchHelper.initUpdateBroadcastReceiver();
    }

    public void createPolicyState(Map<Integer, Boolean> secrecyStateMap, boolean policyState) {
        secrecyStateMap.put(Integer.valueOf(2), Boolean.valueOf(policyState));
        secrecyStateMap.put(Integer.valueOf(1), Boolean.valueOf(policyState));
        secrecyStateMap.put(Integer.valueOf(4), Boolean.valueOf(policyState));
    }

    public void updatePolicyState(Map<Integer, Boolean> newPolicyState, String unlockType) {
        updatePolicyState(newPolicyState, unlockType, true);
    }

    public void updatePolicyState(Map<Integer, Boolean> newPolicyState, String unlockType, boolean needToSave) {
        updatePolicyStateLocked(newPolicyState, unlockType, needToSave);
    }

    private void updatePolicyStateLocked(Map<Integer, Boolean> changedPolicyState, String unlockType, boolean needToSave) {
        Map updatedSecrecyStateMap = new ArrayMap();
        boolean changedToDecrypted = false;
        LogUtil.d(TAG, "updatePolicyStateLocked, changedPolicyState = " + changedPolicyState + ", unlockType = " + unlockType, new Throwable("Kevin_DEBUG"));
        synchronized (mLock) {
            if (changedPolicyState.get(Integer.valueOf(2)) != null) {
                boolean newIsEncryptApp = ((Boolean) changedPolicyState.get(Integer.valueOf(2))).booleanValue();
                if (newIsEncryptApp != this.mIsEncryptApp) {
                    LogUtil.d(TAG, "updatePolicyStateLocked, mIsEncryptApp : " + this.mIsEncryptApp + " -> " + newIsEncryptApp);
                    this.mIsEncryptApp = newIsEncryptApp;
                    updatedSecrecyStateMap.put(Integer.valueOf(2), Boolean.valueOf(this.mIsEncryptApp));
                    changedToDecrypted = !this.mIsEncryptApp;
                }
            }
            if (changedPolicyState.get(Integer.valueOf(1)) != null) {
                boolean newIsEncryptLog = ((Boolean) changedPolicyState.get(Integer.valueOf(1))).booleanValue();
                if (newIsEncryptLog != this.mIsEncryptLog) {
                    LogUtil.d(TAG, "updatePolicyStateLocked, mIsEncryptLog : " + this.mIsEncryptLog + " -> " + newIsEncryptLog);
                    this.mIsEncryptLog = newIsEncryptLog;
                    updatedSecrecyStateMap.put(Integer.valueOf(1), Boolean.valueOf(this.mIsEncryptLog));
                    changedToDecrypted = !this.mIsEncryptLog;
                }
            }
            if (changedPolicyState.get(Integer.valueOf(4)) != null) {
                boolean newIsEncryptAdb = ((Boolean) changedPolicyState.get(Integer.valueOf(4))).booleanValue();
                if (newIsEncryptAdb != this.mIsEncryptAdb) {
                    LogUtil.d(TAG, "updatePolicyStateLocked, mIsEncryptAdb : " + this.mIsEncryptAdb + " -> " + newIsEncryptAdb);
                    this.mIsEncryptAdb = newIsEncryptAdb;
                    updatedSecrecyStateMap.put(Integer.valueOf(4), Boolean.valueOf(this.mIsEncryptAdb));
                    changedToDecrypted = !this.mIsEncryptAdb;
                }
            }
        }
        if (!updatedSecrecyStateMap.isEmpty()) {
            LogUtil.d(TAG, "updatePolicyStateLocked, unlockType = " + unlockType + ", changedToDecrypted = " + changedToDecrypted);
            if (needToSave) {
                SecrecyConfig.getInstance().saveSecrecyConfig(updatedSecrecyStateMap);
            }
            startCountDownTimerIfNessesary(unlockType, changedToDecrypted);
        }
    }

    private void notifySecrecyConfigChanged(Map map) {
        if (map.get(Integer.valueOf(4)) != null) {
            if ("0".equals(SystemProperties.get("persist.sys.adb.engineermode")) && Build.HARDWARE.startsWith("qcom")) {
                String usb_config = "diag,adb";
                SystemProperties.set("persist.sys.usb.config", usb_config);
                SystemProperties.set("sys.usb.config", usb_config);
            }
            SystemProperties.set("ctl.restart", "adbd");
            LogUtil.d(TAG, "restart adbd");
        }
    }

    private void startCountDownTimerIfNessesary(String unlockType, boolean changedToDecrypted) {
        if (changedToDecrypted && DecryptTool.UNLOCK_TYPE_MAC.equals(unlockType)) {
            setCountdown(this.mResetCountDownTimer.getCountDownTimer(unlockType), true);
        }
    }

    public boolean getDefaultPolicy() {
        if (this.mSecrecyService.isSecrecySupportLocal() && !this.mDownloadInfo.isCurrentDownloadInternal()) {
            return true;
        }
        return false;
    }

    public DecryptTool getDecryptTool() {
        return this.mDecryptTool;
    }

    public void importRC4Key(String key_arg) {
        this.mSecrecyConfig.saveImei(getImei());
        this.mSecrecyConfig.saveRC4Key(key_arg);
        this.mSecrecyConfig.saveSecrecyConfig(null);
    }

    public boolean getSecrecyKey(byte[] key) {
        return this.mSecrecyConfig.getSecrecyKey(key);
    }

    public void onConfigLoadFinished() {
    }

    public void onConfigSaveFinished(Map<Integer, Boolean> updatedSecrecyStateMap) {
        if (updatedSecrecyStateMap == null || updatedSecrecyStateMap.isEmpty()) {
            LogUtil.d(TAG, "onConfigSaveFinished return for no secrecy state changed");
            return;
        }
        this.mSecrecyService.notifySecrecyState(updatedSecrecyStateMap);
        notifySecrecyConfigChanged(updatedSecrecyStateMap);
    }

    public void onSecrecySwitchUpdate() {
    }

    public void onSecrecyUpdateFromProvider() {
        if (this.mSystemReady) {
            boolean support = this.mSecrecySwitchHelper.getSupportSwitch();
            boolean activity = this.mSecrecySwitchHelper.getActivitySwitch();
            boolean log = this.mSecrecySwitchHelper.getLogSwitch();
            boolean adb = this.mSecrecySwitchHelper.getAdbSwitch();
            LogUtil.d(TAG, "onSecrecySwitchUpdate, support = " + support);
            this.mResetCountDownTimer.updateCountDownTime(this.mSecrecySwitchHelper.getMacTimeout(), this.mSecrecySwitchHelper.getIdTimeout());
            Map<Integer, Boolean> policyStateMap = new ArrayMap();
            policyStateMap.put(Integer.valueOf(2), Boolean.valueOf(activity));
            policyStateMap.put(Integer.valueOf(1), Boolean.valueOf(log));
            policyStateMap.put(Integer.valueOf(4), Boolean.valueOf(adb));
        }
    }

    public boolean getTestSwitch() {
        return this.mSecrecySwitchHelper.getTestSwitch();
    }

    public void onCountDownTimerExpired() {
        LogUtil.d(TAG, "onCountDownTimerExpired change all types to POLICY_ENCRYPTED");
        Map<Integer, Boolean> policyStateMap = new ArrayMap();
        createPolicyState(policyStateMap, true);
        updatePolicyState(policyStateMap, "countDown_TimerExpired");
    }

    public boolean getPolicyState(int type) {
        synchronized (mLock) {
            boolean z;
            switch (type) {
                case 1:
                    z = this.mIsEncryptLog;
                    return z;
                case 2:
                    z = this.mIsEncryptApp;
                    return z;
                case 4:
                    z = this.mIsEncryptAdb;
                    return z;
                default:
                    return false;
            }
        }
    }

    public String getImei() {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).getImei(0);
    }

    public static String typeToString(int type) {
        switch (type) {
            case 1:
                return "LOG_TYPE";
            case 2:
                return "APP_TYPE";
            case 4:
                return "ADB_TYPE";
            default:
                return "Unknown_TYPE";
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println("PolicyManager dump");
        synchronized (mLock) {
            pw.print(prefix);
            pw.println("    mIsEncryptApp = " + this.mIsEncryptApp);
            pw.print(prefix);
            pw.println("    mIsEncryptLog = " + this.mIsEncryptLog);
            pw.print(prefix);
            pw.println("    mIsEncryptAdb = " + this.mIsEncryptAdb);
            pw.println("    mCountDown = " + this.mCountDown);
            pw.println("    mTimeTicks = " + this.mTimeTicks);
        }
        this.mDownloadInfo.dump(fd, pw, prefix + "    ");
        this.mSecrecyConfig.dump(fd, pw, prefix + "    ");
        if (!getPolicyState(2)) {
            this.mResetCountDownTimer.dump(fd, pw, prefix + "    ");
            this.mSecrecySwitchHelper.dump(fd, pw, prefix + "    ");
        }
        pw.print("\n");
    }

    public void status(FileDescriptor fd, PrintWriter pw) {
        synchronized (mLock) {
            pw.println("secrecy_on=" + this.mSecrecyService.isSecrecySupportLocal());
            StringBuilder append = new StringBuilder().append("encrypt_all=");
            boolean z = (this.mIsEncryptApp && this.mIsEncryptLog) ? this.mIsEncryptAdb : false;
            pw.println(append.append(z).toString());
            pw.println("encrypt_app=" + this.mIsEncryptApp);
            pw.println("encrypt_log=" + this.mIsEncryptLog);
            pw.println("encrypt_adb=" + this.mIsEncryptAdb);
            append = new StringBuilder().append("decrypt_all=");
            int i = (this.mIsEncryptApp || this.mIsEncryptLog) ? 1 : this.mIsEncryptAdb;
            pw.println(append.append(i ^ 1).toString());
        }
    }

    public Long getCurrentDownloadTimeInMills() {
        return Long.valueOf(this.mDownloadInfo.getCurrentDownloadTimeInMills());
    }

    public void setLastDownloadTimeInMills(String lastDownloadTimeInMillis) {
        this.mDownloadInfo.setLastDownloadTimeInMills(lastDownloadTimeInMillis);
        checkChangeToPolicyDecryptedIfNeeded();
    }

    private void checkChangeToPolicyDecryptedIfNeeded() {
        long currentDownloadTimeInMills = this.mDownloadInfo.getCurrentDownloadTimeInMills();
        long lastDownloadTimeInMills = this.mDownloadInfo.getLastDownloadTimeInMills();
        boolean currentDownloadInternal = this.mDownloadInfo.isCurrentDownloadInternal();
        LogUtil.d(TAG, "checkChangeToPolicyDecryptedIfNeeded, currentDownloadTimeInMills = " + currentDownloadTimeInMills);
        LogUtil.d(TAG, "checkChangeToPolicyDecryptedIfNeeded, lastDownloadTimeInMills    = " + lastDownloadTimeInMills);
        LogUtil.d(TAG, "checkChangeToPolicyDecryptedIfNeeded, lastDownloadTimeInMills    = " + lastDownloadTimeInMills);
        if (currentDownloadTimeInMills != lastDownloadTimeInMills && currentDownloadInternal) {
            Map<Integer, Boolean> policyStateMap = new ArrayMap();
            createPolicyState(policyStateMap, false);
            updatePolicyState(policyStateMap, "newDownloadInternal");
        }
    }

    /* JADX WARNING: Missing block: B:15:0x003f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setCountdown(int countdown, boolean needToSave) {
        LogUtil.d(TAG, "setCountdown = " + countdown);
        if (countdown >= 0) {
            this.mCountDown = countdown;
            this.mTimeTicks = 0;
            if (needToSave) {
                SecrecyConfig.getInstance().saveSecrecyConfig(null);
            }
            if (countdown != 0) {
                if (this.mTimeTickReceiver == null) {
                    this.mTimeTickReceiver = new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent) {
                            PolicyManager policyManager = PolicyManager.this;
                            policyManager.mCountDown = policyManager.mCountDown - 1;
                            policyManager = PolicyManager.this;
                            policyManager.mTimeTicks = policyManager.mTimeTicks + 1;
                            if (PolicyManager.this.mCountDown <= 0) {
                                Map<Integer, Boolean> states = new ArrayMap();
                                states.put(Integer.valueOf(2), Boolean.valueOf(true));
                                states.put(Integer.valueOf(1), Boolean.valueOf(true));
                                states.put(Integer.valueOf(4), Boolean.valueOf(true));
                                PolicyManager.this.updatePolicyStateLocked(states, DecryptTool.COUNTDOWN, true);
                                PolicyManager.this.mContext.unregisterReceiver(this);
                                synchronized (PolicyManager.this) {
                                    PolicyManager.this.mTimeTickReceiver = null;
                                }
                                PolicyManager.this.mTimeTicks = 0;
                            } else if (PolicyManager.this.mTimeTicks >= 30) {
                                SecrecyConfig.getInstance().saveSecrecyConfig(null);
                                PolicyManager.this.mTimeTicks = 0;
                            }
                        }
                    };
                }
                this.mContext.registerReceiver(this.mTimeTickReceiver, new IntentFilter("android.intent.action.TIME_TICK"));
            } else if (this.mTimeTickReceiver != null) {
                this.mContext.unregisterReceiver(this.mTimeTickReceiver);
                this.mTimeTickReceiver = null;
            }
        }
    }

    public int getCountdown() {
        LogUtil.d(TAG, "getCountdown = " + this.mCountDown);
        return this.mCountDown;
    }
}
