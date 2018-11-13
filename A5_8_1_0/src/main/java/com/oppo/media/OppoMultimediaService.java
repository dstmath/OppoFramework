package com.oppo.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.oppo.localservice.ILocalServiceCallback;
import com.oppo.localservice.IMultimediaLocalService;
import com.oppo.media.IOppoMultimediaService.Stub;
import com.oppo.media.OppoMultimediaServiceDefine.DaemonFun;
import com.oppo.media.OppoMultimediaServiceDefine.ModuleTag;
import java.io.IOException;
import java.util.List;

public class OppoMultimediaService extends Stub {
    private static final int MAX_CHECK_COUNT = 3;
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final String TAG = "OppoMultimediaService";
    private static final int sDelay = 3000;
    private static final int sDelayForCheckAudioSystem = 1000;
    private static final int sDelayForSetMode = 100;
    private BroadcastReceiver homeListenerReceiver = new BroadcastReceiver() {
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        static final String SYSTEM_HOME_KEY = "homekey";
        static final String SYSTEM_REASON = "reason";

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DebugLog.d(OppoMultimediaService.TAG, "homeListenerReceiver action:" + action);
            if (action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (reason != null) {
                    boolean equals = reason.equals(SYSTEM_HOME_KEY);
                }
            }
        }
    };
    private AudioServiceState mAudioServiceState;
    private boolean mBindServiceFlag = false;
    private int mCheckCount = 0;
    private BroadcastReceiver mCommonReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            DebugLog.d(OppoMultimediaService.TAG, "mCommonReceiver action = " + intent.getAction());
        }
    };
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private OppoDaemonForgroundListener mDaemonForgroundListener;
    private DaemonFunControl mDaemonFunControl;
    private boolean mHasActiveStreamBeforeIncall = false;
    private ILocalServiceCallback mILocalServiceCallback = new ILocalServiceCallback.Stub() {
        public void LocalServiceFeedBack(int event, String value) throws RemoteException {
            DebugLog.d(OppoMultimediaService.TAG, "LocalServiceFeedBack event : " + event + " value:" + value);
        }
    };
    private IMultimediaLocalService mIMultimediaLocalService;
    private OppoLocalSocketServer mLocalSocketServer;
    private MediaWatchThread mMediaWatchThread;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangedListener = new OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            DebugLog.d(OppoMultimediaService.TAG, "onSubscriptionsChanged");
            OppoMultimediaService.this.registerPhone();
        }
    };
    private OppoDaemonListHelper mOppoDaemonListHelper;
    private PhoneStateListener mOppoPhoneSimStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            DebugLog.d(OppoMultimediaService.TAG, "onCallStateChanged\t state=" + state + " incomingNumber:" + incomingNumber);
            OppoMultimediaService.this.callStateChanged(state, incomingNumber);
        }
    };
    private boolean mRecordShowHint = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            OppoMultimediaService.this.mIMultimediaLocalService = IMultimediaLocalService.Stub.asInterface(service);
            DebugLog.d(OppoMultimediaService.TAG, "onServiceConnected mIMultimediaLocalService = " + OppoMultimediaService.this.mIMultimediaLocalService);
            if (OppoMultimediaService.this.mIMultimediaLocalService != null) {
                try {
                    OppoMultimediaService.this.mIMultimediaLocalService.registerCallback(OppoMultimediaService.this.mILocalServiceCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            OppoMultimediaService.this.mBindServiceFlag = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            if (OppoMultimediaService.this.mIMultimediaLocalService != null) {
                try {
                    OppoMultimediaService.this.mIMultimediaLocalService.unRegisterCallback(OppoMultimediaService.this.mILocalServiceCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            OppoMultimediaService.this.mIMultimediaLocalService = null;
            OppoMultimediaService.this.mBindServiceFlag = false;
            DebugLog.d(OppoMultimediaService.TAG, "onServiceDisconnected");
        }
    };
    private boolean mStreamMuteChange = false;
    private TelephonyManager mTelephonyManager;
    private MediaWatchHandler mWatchHandler;

    private class MediaWatchHandler extends Handler {
        /* synthetic */ MediaWatchHandler(OppoMultimediaService this$0, MediaWatchHandler -this1) {
            this();
        }

        private MediaWatchHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                    if (OppoMultimediaService.this.mAudioServiceState != null) {
                        if (OppoMultimediaService.this.mDaemonFunControl.getDaemonFunEnable(DaemonFun.SETMODE.ordinal())) {
                            OppoMultimediaService.this.mAudioServiceState.checkAllModes(-1);
                        }
                        if (msg.what == 2 && OppoMultimediaService.this.mDaemonFunControl.getDaemonFunEnable(DaemonFun.KILLMEDIASERVER.ordinal()) && OppoMultimediaService.this.mAudioServiceState.checkAudioSystem(true)) {
                            DebugLog.d(OppoMultimediaService.TAG, "start check if kill AudioSystem mCheckCount:" + OppoMultimediaService.this.mCheckCount);
                            if (OppoMultimediaService.this.mCheckCount == 0) {
                                OppoMultimediaService.sendMsg(OppoMultimediaService.this.mWatchHandler, 13, 0, 0, 0, null, 1000);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                case 5:
                case 7:
                    if (OppoMultimediaService.this.mAudioServiceState != null && OppoMultimediaService.this.mDaemonFunControl.getDaemonFunEnable(DaemonFun.SETMODE.ordinal())) {
                        OppoMultimediaService.this.mAudioServiceState.readAudioModes((String) msg.obj);
                        if (msg.what == 5 && OppoMultimediaService.this.mAudioServiceState.isNeedSyncMode()) {
                            DebugLog.d(OppoMultimediaService.TAG, "sendMsg sync mode start");
                            OppoMultimediaService.sendMsg(OppoMultimediaService.this.mWatchHandler, 14, 0, 0, 0, null, 100);
                            return;
                        }
                        return;
                    }
                    return;
                case 8:
                case 9:
                case 19:
                    if (OppoMultimediaService.this.mAudioServiceState != null) {
                        try {
                            OppoMultimediaService.this.mAudioServiceState.broadcastRecordEvent(msg.what, Integer.parseInt(msg.obj));
                            return;
                        } catch (NumberFormatException e) {
                            return;
                        }
                    }
                    return;
                case 10:
                    DebugLog.d(OppoMultimediaService.TAG, "+mRecordShowHint : " + OppoMultimediaService.this.mRecordShowHint);
                    if (OppoMultimediaService.this.mRecordShowHint || OppoMultimediaService.this.mDaemonFunControl.getDaemonFunEnable(DaemonFun.RECORDCONFLICT.ordinal())) {
                        String info = OppoMultimediaService.this.mAudioServiceState.getRecordFailedInfo((String) msg.obj);
                        if (info != null) {
                            OppoMultimediaService.this.mAudioServiceState.showRecordHintDialog(OppoMultimediaService.this.getInfoFromLocalService("get_record_failed_info=" + info));
                            return;
                        }
                        return;
                    }
                    return;
                case 11:
                    if (OppoMultimediaService.this.mAudioServiceState != null) {
                        OppoMultimediaService.this.mAudioServiceState.releaseAudioTrack();
                        return;
                    }
                    return;
                case 13:
                    if (OppoMultimediaService.this.mAudioServiceState != null && OppoMultimediaService.this.mDaemonFunControl.getDaemonFunEnable(DaemonFun.KILLMEDIASERVER.ordinal())) {
                        if (OppoMultimediaService.this.mAudioServiceState.checkAudioSystem(false)) {
                            DebugLog.d(OppoMultimediaService.TAG, "+start check if kill AudioSystem mCheckCount :" + OppoMultimediaService.this.mCheckCount);
                            if (OppoMultimediaService.this.mCheckCount >= 3) {
                                OppoMultimediaService.this.mAudioServiceState.killAudioSystem();
                                OppoMultimediaService.this.mWatchHandler.removeMessages(13);
                                OppoMultimediaService.this.mCheckCount = 0;
                                return;
                            }
                            OppoMultimediaService oppoMultimediaService = OppoMultimediaService.this;
                            oppoMultimediaService.mCheckCount = oppoMultimediaService.mCheckCount + 1;
                            OppoMultimediaService.sendMsg(OppoMultimediaService.this.mWatchHandler, 13, 0, 0, 0, null, OppoMultimediaService.sDelay);
                            return;
                        }
                        DebugLog.d(OppoMultimediaService.TAG, "checkAudioSystem normal");
                        OppoMultimediaService.this.mWatchHandler.removeMessages(13);
                        OppoMultimediaService.this.mCheckCount = 0;
                        return;
                    }
                    return;
                case 14:
                    if (OppoMultimediaService.this.mAudioServiceState.isNeedSyncMode()) {
                        OppoMultimediaService.this.mAudioServiceState.OppoSetPhoneState(-1);
                        DebugLog.d(OppoMultimediaService.TAG, "sendMsg sync mode end");
                        return;
                    }
                    DebugLog.d(OppoMultimediaService.TAG, "no need sync mode in call state");
                    return;
                case 15:
                    if (OppoMultimediaService.this.mAudioServiceState != null) {
                        OppoMultimediaService.this.mAudioServiceState.mmListRomUpdate();
                        return;
                    }
                    return;
                case 16:
                    if (OppoMultimediaService.this.mStreamMuteChange && OppoMultimediaService.this.mAudioServiceState.isHasActiveStream()) {
                        OppoMultimediaService.this.mAudioServiceState.setStreamMute(3, true);
                        return;
                    }
                    return;
                case 17:
                    OppoMultimediaService.this.mAudioServiceState.setStreamMute(3, false);
                    return;
                case 18:
                    OppoMultimediaService.this.mAudioServiceState.setVoiceSmallFlag();
                    return;
                case 20:
                    if (OppoMultimediaService.this.mAudioServiceState != null && OppoMultimediaService.this.mDaemonFunControl.getDaemonFunEnable(DaemonFun.SETMODE.ordinal())) {
                        OppoMultimediaService.this.mAudioServiceState.updateForgoundInfo((String) msg.obj);
                        return;
                    }
                    return;
                case 103:
                    OppoMultimediaService.this.setEventToLocalService(msg.what, null);
                    return;
                case 300:
                    OppoMultimediaService.this.bindLocalService();
                    return;
                default:
                    return;
            }
        }
    }

    private class MediaWatchThread extends Thread {
        MediaWatchThread() {
            super("OppoMultimediaWatchService");
        }

        public void run() {
            Looper.prepare();
            synchronized (OppoMultimediaService.this) {
                OppoMultimediaService.this.mWatchHandler = new MediaWatchHandler(OppoMultimediaService.this, null);
                OppoMultimediaService.this.notify();
            }
            Looper.loop();
        }
    }

    public OppoMultimediaService(Context context) {
        DebugLog.d(TAG, "+OppoMultimediaService");
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        readSettings();
        this.mLocalSocketServer = new OppoLocalSocketServer(context);
        this.mLocalSocketServer.start();
        this.mDaemonForgroundListener = new OppoDaemonForgroundListener(context);
        this.mDaemonForgroundListener.start();
        createMediaWatchThread();
        this.mOppoDaemonListHelper = new OppoDaemonListHelper(this.mContext, this);
        this.mDaemonFunControl = new DaemonFunControl(this.mContext, this.mOppoDaemonListHelper);
        this.mAudioServiceState = new AudioServiceState(this.mContext, this.mOppoDaemonListHelper, this);
        registerSubInfo();
        DebugLog.d(TAG, "-OppoMultimediaService");
    }

    public void readSettings() {
        this.mRecordShowHint = this.mContext.getPackageManager().hasSystemFeature(OppoMultimediaServiceDefine.FEATURE_RECORD_CONFLICT_NAME);
    }

    private void createMediaWatchThread() {
        this.mMediaWatchThread = new MediaWatchThread();
        this.mMediaWatchThread.start();
        waitForMediaWatchHandlerCreation();
    }

    private void waitForMediaWatchHandlerCreation() {
        synchronized (this) {
            while (this.mWatchHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while waiting on other handler.");
                }
            }
        }
    }

    public void systemRunning() {
        DebugLog.d(TAG, "systemRunning");
        sendMsg(this.mWatchHandler, 300, 2, 0, 0, null, sDelay);
    }

    public void setParameters(String keyValuePairs) {
    }

    public String getParameters(String keys) {
        if (keys == null) {
            return null;
        }
        String ret = "";
        if (keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_GET_SPEAKER_AUTHORITY)) {
            if (!this.mDaemonFunControl.getDaemonFunEnable(DaemonFun.SETSPEAKERPHONEON.ordinal())) {
                ret = "true";
            } else if (isHasSpeakerAuthority(keys)) {
                ret = "true";
            } else {
                ret = "false";
            }
        } else if (keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_GET_DEVICE_CHANGE_AUTHORITY)) {
            if (isHasDeviceChangeAuthority(keys)) {
                ret = "true";
            } else {
                ret = "false";
            }
        } else if (keys.equals(OppoMultimediaServiceDefine.KEY_AUDIO_GET_RECORD_STATE)) {
            if (!this.mDaemonFunControl.getDaemonFunEnable(DaemonFun.STREAMADJUST.ordinal())) {
                ret = "true";
            } else if (this.mAudioServiceState.getRecordThreadState()) {
                ret = "true";
            } else {
                ret = "false";
            }
        } else if (keys.equals(OppoMultimediaServiceDefine.KEY_AUDIO_GET_STREAMTYPE_ADJUST_REVISE)) {
            if (!this.mDaemonFunControl.getDaemonFunEnable(DaemonFun.STREAMADJUST.ordinal())) {
                ret = "false";
            } else if (this.mAudioServiceState.isStreamTypeAdjustRevise()) {
                ret = "true";
            } else {
                ret = "false";
            }
        } else if (keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_GET_DAEMON_LISTINFO_BYPID) || keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_GET_DAEMON_LISTINFO_BYNAME) || keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_CHECK_DAEMON_LISTINFO_BYPID) || keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_CHECK_DAEMON_LISTINFO_BYNAME)) {
            ret = getDaemonListinfoByKey(keys);
        } else if (keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_GET_APPLICATION_TOPACTIVITY)) {
            ret = isTopActivity(keys);
        } else if (keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_GET_VOLUME_CONTROL_STATE)) {
            ret = isAppCanSetStreamVolume(keys);
        } else if (keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_GET_ADJUSTSTREAMVOLUME_CONTROL_STATE)) {
            ret = isAppCanAdjustStreamVolume(keys);
        } else if (keys.startsWith(OppoMultimediaServiceDefine.KEY_AUDIO_GET_DO_MUSIC_MUTE)) {
            ret = isDoMusicMute();
        }
        return ret;
    }

    public void setEventInfo(int event, String info) throws RemoteException {
        DebugLog.d(TAG, "setEventInfo event = " + event + " info = " + info);
        switch (event) {
            case 1:
                sendMsg(this.mWatchHandler, 1, 2, 0, 0, info, 0);
                return;
            case 2:
                sendMsg(this.mWatchHandler, 2, 2, 0, 0, info, 0);
                return;
            case 5:
                sendMsg(this.mWatchHandler, 5, 0, 0, 0, info, 0);
                return;
            case 16:
            case 17:
                try {
                    sendMsg(this.mWatchHandler, event, 0, 0, 0, info, Integer.parseInt(info));
                    return;
                } catch (NumberFormatException e) {
                    return;
                }
            case 19:
                sendMsg(this.mWatchHandler, 19, 0, 0, 0, info, 100);
                return;
            default:
                sendMsg(this.mWatchHandler, event, 2, 0, 0, info, 0);
                return;
        }
    }

    public void sendBroadcastToAll(Intent intent) {
        intent.addFlags(67108864);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            DebugLog.d(TAG, "sendMsg: Msg " + msg + " existed!");
            return;
        }
        handler.sendMessageAtTime(handler.obtainMessage(msg, arg1, arg2, obj), SystemClock.uptimeMillis() + ((long) delay));
    }

    public boolean isInCallState() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        long ident = Binder.clearCallingIdentity();
        boolean isInCall = telecomManager.isInCall();
        Binder.restoreCallingIdentity(ident);
        DebugLog.d(TAG, "isInCallState =" + isInCall);
        if (this.mAudioServiceState.isInCallMode()) {
            return true;
        }
        return isInCall;
    }

    public boolean isHasSpeakerAuthority(String keys) {
        boolean mIsTelName = true;
        String mPackageName = "";
        String[] strarr = keys.split("=");
        if (strarr != null && strarr.length == 2) {
            mPackageName = strarr[1];
            if (mPackageName != null) {
                mIsTelName = mPackageName.equals(OppoMultimediaServiceDefine.TEL_PACKAGE_NAME);
            }
        }
        DebugLog.d(TAG, "isHasSpeakerAuthority keys: " + keys + ",mIsTelName=" + mIsTelName);
        if (!isInCallState() || (mIsTelName ^ 1) == 0) {
            return true;
        }
        setEventToLocalService(104, mPackageName);
        return false;
    }

    public boolean isHasDeviceChangeAuthority(String keys) {
        boolean lIsPermitName = true;
        String lPackageName = "";
        String[] lStrarr = keys.split("=");
        if (lStrarr != null && lStrarr.length == 2) {
            lPackageName = lStrarr[1];
            if (lPackageName != null) {
                lIsPermitName = lPackageName.equals(OppoMultimediaServiceDefine.TEL_PACKAGE_NAME) || lPackageName.equals(OppoMultimediaServiceDefine.BLUETOOTH_PACKAGE_NAME);
            }
        }
        DebugLog.d(TAG, "isHasDeviceChangeAuthority keys: " + keys + ",lIsPermitName=" + lIsPermitName);
        if (!isInCallState() || (lIsPermitName ^ 1) == 0) {
            return true;
        }
        setEventToLocalService(104, lPackageName);
        return false;
    }

    public String getPackageNameByPid(int pid) {
        for (RunningAppProcessInfo appProcess : ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses()) {
            int tPid = appProcess.pid;
            String tPackageName = appProcess.processName;
            if (pid == tPid) {
                String packageName = tPackageName;
                return tPackageName;
            }
        }
        return null;
    }

    public int getAppImportanceByPid(int pid) {
        for (RunningAppProcessInfo appProcess : ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses()) {
            int tPid = appProcess.pid;
            DebugLog.d(TAG, "+PackageName: " + appProcess.processName + "  pid: " + tPid + " importance:" + appProcess.importance);
            if (pid == tPid) {
                try {
                    return appProcess.importance;
                } catch (Exception e) {
                    DebugLog.d(TAG, "Error>> :" + e.toString());
                }
            }
        }
        return 0;
    }

    public void killProcessByPid(int pid) {
        String command = "kill -9 " + String.valueOf(pid);
        try {
            DebugLog.d(TAG, "command :" + command);
            Runtime.getRuntime().exec(command);
            setEventToLocalService(105, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void killProcessByPackageName(String packageName) {
        DebugLog.d(TAG, "+killProcessByPackageName packageName :" + packageName);
        ((ActivityManager) this.mContext.getSystemService("activity")).forceStopPackage(packageName);
    }

    private void bindLocalService() {
        DebugLog.d(TAG, "bindService start");
        List<ResolveInfo> resolveInfos = this.mContext.getPackageManager().queryIntentServices(new Intent("com.oppo.multimedia.localservice.START"), 0);
        if (resolveInfos == null || resolveInfos.size() != 1) {
            DebugLog.d(TAG, "bindService, return not found resolveInfos.");
            return;
        }
        ResolveInfo serviceInfo = (ResolveInfo) resolveInfos.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        DebugLog.d(TAG, "packageName = " + packageName + " className = " + className);
        ComponentName component = new ComponentName(packageName, className);
        Intent iapIntent = new Intent();
        iapIntent.setComponent(component);
        this.mContext.startService(iapIntent);
        this.mContext.bindService(iapIntent, this.mServiceConnection, 1);
        DebugLog.d(TAG, "bindService end");
    }

    private void unBindLocalService() {
        if (this.mBindServiceFlag) {
            this.mContext.unbindService(this.mServiceConnection);
            this.mBindServiceFlag = false;
        }
    }

    public IMultimediaLocalService getLocalService() {
        if (this.mIMultimediaLocalService == null) {
            bindLocalService();
        }
        return this.mIMultimediaLocalService;
    }

    public void setEventToLocalService(int event, String msg) {
        DebugLog.d(TAG, "setEventToLocalService event = " + event + " msg = " + msg);
        IMultimediaLocalService localService = getLocalService();
        if (localService == null) {
            DebugLog.d(TAG, "LocalService start error!!!");
            return;
        }
        try {
            localService.setEvent(event, msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String getInfoFromLocalService(String key) {
        DebugLog.d(TAG, "getInfoFromLocalService key = " + key);
        String result = "";
        IMultimediaLocalService localService = getLocalService();
        if (localService == null) {
            DebugLog.d(TAG, "LocalService start error!!!");
            return result;
        }
        try {
            result = localService.getParameters(key);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void registerCommonReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        this.mContext.registerReceiver(this.mCommonReceiver, intentFilter);
    }

    private void registerHomeKeyReceiver(Context context) {
        context.registerReceiver(this.homeListenerReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    private void unregisterHomeKeyReceiver(Context context) {
        if (this.homeListenerReceiver != null) {
            context.unregisterReceiver(this.homeListenerReceiver);
        }
    }

    public void registerSubInfo() {
        SubscriptionManager.from(this.mContext).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
    }

    public void unregisterSubInfo() {
        SubscriptionManager.from(this.mContext).removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
    }

    public void registerPhone() {
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mTelephonyManager.listen(this.mOppoPhoneSimStateListener, 32);
    }

    private void callStateChanged(int state, String incomingNumber) {
        if (state == 1 || state == 2) {
            if (state == 2) {
                this.mAudioServiceState.setCallState(true);
            }
            if (this.mAudioServiceState.isHasActiveStream()) {
                this.mHasActiveStreamBeforeIncall = true;
            } else {
                this.mHasActiveStreamBeforeIncall = false;
            }
            if (this.mAudioServiceState.isStreamShouldMute(3, state) && !this.mStreamMuteChange) {
                if (this.mAudioServiceState.isHasActivePidInList(OppoDaemonListHelper.TAG_MODULE[ModuleTag.VOLUME.ordinal()], OppoMultimediaServiceDefine.APP_LIST_ATTRIBUTE_NO_NEED_MUTE_IN_RINGING)) {
                    if (state == 2) {
                        try {
                            this.mStreamMuteChange = true;
                            setEventInfo(16, "200");
                        } catch (RemoteException e) {
                        }
                    }
                } else if (this.mAudioServiceState.isWiredHeadsetOn()) {
                    this.mStreamMuteChange = true;
                    this.mAudioServiceState.setStreamMute(3, true);
                } else {
                    try {
                        this.mStreamMuteChange = true;
                        setEventInfo(16, "500");
                    } catch (RemoteException e2) {
                    }
                }
            }
        } else if (state == 0) {
            DebugLog.d(TAG, "mHasActiveStreamBeforeIncall :" + this.mHasActiveStreamBeforeIncall + " mStreamMuteChange :" + this.mStreamMuteChange);
            this.mAudioServiceState.setCallState(false);
            this.mWatchHandler.removeMessages(16);
            if (this.mStreamMuteChange) {
                this.mStreamMuteChange = false;
                if (this.mAudioServiceState.getMode() != 0) {
                    try {
                        setEventInfo(17, "200");
                    } catch (RemoteException e3) {
                    }
                } else {
                    this.mAudioServiceState.setStreamMute(3, false);
                }
            }
            if (!this.mHasActiveStreamBeforeIncall) {
                this.mAudioServiceState.checkAppcationAndKill();
            }
            this.mHasActiveStreamBeforeIncall = false;
        }
    }

    private String isTopActivity(String keys) {
        String ret = "false";
        String[] strarr = keys.split("=");
        if (strarr != null && strarr.length == 2 && isAppTopActivity(strarr[1])) {
            return "true";
        }
        return ret;
    }

    public boolean isAppTopActivity(String packagename) {
        if (packagename != null) {
            try {
                ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
                if (am != null) {
                    ComponentName cn = am.getTopAppName();
                    if (cn != null) {
                        String topAppName = cn.getPackageName();
                        DebugLog.d(TAG, "topAppName = " + topAppName + " getClassName = " + cn.getClassName());
                        if (topAppName != null && topAppName.equals(packagename)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private String isApplicationPlaying(String keys) {
        String ret = "false";
        String[] strarr = keys.split("=");
        if (strarr == null || strarr.length != 2) {
            return ret;
        }
        if (this.mAudioServiceState.isApplicationPlaying(strarr[1])) {
            return "true";
        }
        return "false";
    }

    private String isAppCanSetStreamVolume(String keys) {
        String valueTrue = "true";
        String valueFalse = "false";
        String[] strarr = keys.split("=");
        if (strarr == null || strarr.length != 2) {
            return valueTrue;
        }
        String lAttribute = this.mOppoDaemonListHelper.getAttributeByAppName(OppoDaemonListHelper.TAG_MODULE[ModuleTag.VOLUME.ordinal()], strarr[1]);
        if (lAttribute != null && lAttribute.equalsIgnoreCase("permit")) {
            return valueTrue;
        }
        String value = isTopActivity(keys);
        DebugLog.d(TAG, "isTopActivity value = " + value);
        if (value != null && value.equalsIgnoreCase("true")) {
            return valueTrue;
        }
        value = isApplicationPlaying(keys);
        DebugLog.d(TAG, "isApplicationPlaying value = " + value);
        if (value == null || !value.equalsIgnoreCase("true")) {
            return valueFalse;
        }
        return valueTrue;
    }

    private String isAppCanAdjustStreamVolume(String keys) {
        String valueTrue = "true";
        String valueFalse = "false";
        String[] strarr = keys.split("=");
        if (this.mAudioServiceState.getMode() == 1 && strarr != null && strarr.length == 2 && (this.mAudioServiceState.isHasActivePidInList(OppoDaemonListHelper.TAG_MODULE[ModuleTag.VOLUME.ordinal()], OppoMultimediaServiceDefine.APP_LIST_ATTRIBUTE_NO_NEED_MUTE_IN_CALL) || this.mAudioServiceState.isHasActivePidInList(OppoDaemonListHelper.TAG_MODULE[ModuleTag.VOLUME.ordinal()], OppoMultimediaServiceDefine.APP_LIST_ATTRIBUTE_NO_NEED_MUTE_IN_RINGING))) {
            return valueFalse;
        }
        return valueTrue;
    }

    private String getDaemonListinfoByKey(String keys) {
        String ret = "";
        String[] strarr = keys.split("=");
        if (strarr != null) {
            try {
                if (strarr.length == 3) {
                    if (strarr[0].equals(OppoMultimediaServiceDefine.KEY_AUDIO_CHECK_DAEMON_LISTINFO_BYPID)) {
                        ret = this.mOppoDaemonListHelper.checkIsInDaemonlistByPid(strarr[1], Integer.parseInt(strarr[2])) ? "true" : "false";
                    } else if (strarr[0].equals(OppoMultimediaServiceDefine.KEY_AUDIO_CHECK_DAEMON_LISTINFO_BYNAME)) {
                        ret = this.mOppoDaemonListHelper.checkIsInDaemonlistByName(strarr[1], strarr[2]) ? "true" : "false";
                    } else if (strarr[0].equals(OppoMultimediaServiceDefine.KEY_AUDIO_GET_DAEMON_LISTINFO_BYPID)) {
                        return this.mOppoDaemonListHelper.getAttributeByAppPid(strarr[1], Integer.parseInt(strarr[2]));
                    } else {
                        if (strarr[0].equals(OppoMultimediaServiceDefine.KEY_AUDIO_GET_DAEMON_LISTINFO_BYNAME)) {
                            return this.mOppoDaemonListHelper.getAttributeByAppName(strarr[1], strarr[2]);
                        }
                    }
                }
            } catch (NumberFormatException e) {
            }
        }
        return ret;
    }

    public boolean getFunEnable(int funindex) {
        if (this.mDaemonFunControl.getDaemonFunEnable(funindex)) {
            return true;
        }
        return false;
    }

    public String isDoMusicMute() {
        String ret = "false";
        if (this.mAudioServiceState == null || !this.mAudioServiceState.mDoMusicMute) {
            return ret;
        }
        return "true";
    }
}
