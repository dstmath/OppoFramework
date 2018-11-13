package com.oppo.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.oppo.media.OppoMultimediaServiceDefine.DaemonFun;
import com.oppo.media.OppoMultimediaServiceDefine.ModuleTag;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class AudioServiceState {
    private static final String AUDIO_FLINGER = "media.audio_flinger";
    private static final String AUDIO_INPUT_THREAD = "Input thread";
    private static final String AUDIO_POLICY = "media.audio_policy";
    private static final String AUDIO_POLICY_STREAM_INFOS = "Stream volume refCount muteCount";
    public static final long AudioCheckIntervals = 60000;
    private static final long CREATE_TRACK_MAX_COUNT = 32;
    private static final long CREATE_TRACK_MAX_FAILED = 10;
    private static final int RECORD_FAIL_HINT_DEFAULT_INTERVAL = 1000;
    private static final String TAG = "OppoMultimediaService_Audio";
    private String mActiveTracksPid;
    private final HashMap<Integer, Integer> mAppsMode = new HashMap();
    private final AudioManager mAudioManager;
    private final Object mAudioServiceLock = new Object();
    private boolean mBlocking = false;
    private boolean mCallStateOffhook = false;
    private final Context mContext;
    private int mCreateTrackFailedCount = 0;
    public boolean mDoMusicMute = false;
    private boolean mIsHasCallMode = false;
    private KeyguardManager mKeyguardManager;
    private long mLastCheckAudioTime = SystemClock.elapsedRealtime();
    private int mLastRecordEvent = 9;
    private int mLastRecordPid = -1;
    private long mLastShowRecordFailedHintTime = SystemClock.elapsedRealtime();
    private OppoDaemonListHelper mOppoDaemonListHelper;
    private final OppoMultimediaService mOppoMultimediaService;
    private String mRecordFailedPackage = null;
    private boolean mShowAlertDialog = false;
    private int mShowRecordFailedHintCount = 0;
    private int mShowRecordFailedHintCurrentPid = 0;
    private int mShowRecordFailedHintInterval = 1000;
    private int mShowRecordFailedHintLastPid = -1;
    private int mShowRecordHintCurrentRunningPid = 0;
    private int mShowRecorddHintLastRunningPid = -1;

    AudioServiceState(Context context, OppoDaemonListHelper listhelper, OppoMultimediaService oms) {
        DebugLog.d(TAG, "AudioServiceState");
        this.mContext = context;
        this.mOppoMultimediaService = oms;
        this.mOppoDaemonListHelper = listhelper;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        readSettings();
    }

    public void readSettings() {
        DebugLog.d(TAG, "readSettings");
    }

    public void readAudioModes(String modes) {
        DebugLog.d(TAG, "readAudioModes = " + modes);
        synchronized (this.mAudioServiceLock) {
            this.mAppsMode.clear();
            this.mIsHasCallMode = false;
            if (modes == null) {
                DebugLog.v(TAG, "current system mode list is empty");
                return;
            }
            String[] allmodes = modes.split(",");
            if (allmodes != null && allmodes.length > 1) {
                DebugLog.d(TAG, "allmodes length = " + allmodes.length);
                boolean hasCallMode = false;
                int i = 0;
                while (i < allmodes.length && i + 1 != allmodes.length) {
                    try {
                        int pid = Integer.parseInt(allmodes[i]);
                        int mode = Integer.parseInt(allmodes[i + 1]);
                        DebugLog.d(TAG, "pid  = " + pid + " mode = " + mode);
                        if (2 == mode) {
                            hasCallMode = true;
                        }
                        this.mAppsMode.put(Integer.valueOf(pid), Integer.valueOf(mode));
                        i = (i + 1) + 1;
                    } catch (NumberFormatException e) {
                    }
                }
                this.mIsHasCallMode = hasCallMode;
                DebugLog.d(TAG, "mAppsMode size= " + this.mAppsMode.size() + " mIsHasCallMode :" + this.mIsHasCallMode);
            }
            broadcastAudioModeInfo();
        }
    }

    public boolean isInCallMode() {
        DebugLog.d(TAG, "mIsHasCallMode :" + this.mIsHasCallMode + " mCallStateOffhook:" + this.mCallStateOffhook);
        if (this.mIsHasCallMode || this.mCallStateOffhook) {
            return true;
        }
        return false;
    }

    public void setCallState(boolean callState) {
        this.mCallStateOffhook = callState;
    }

    public boolean isNeedSyncMode() {
        if (this.mOppoMultimediaService.isInCallState() || this.mAudioManager.isBluetoothScoOn()) {
            return false;
        }
        return true;
    }

    private String getPackageNamesFromModeInfo() {
        String packageNames = "";
        if (!this.mAppsMode.isEmpty()) {
            for (Entry<Integer, Integer> entry : this.mAppsMode.entrySet()) {
                int pid = ((Integer) entry.getKey()).intValue();
                if (((Integer) entry.getValue()).intValue() != 0) {
                    String packageName = this.mOppoMultimediaService.getPackageNameByPid(pid);
                    if (packageName != null) {
                        packageNames = packageNames + packageName + ",";
                    }
                }
            }
        }
        DebugLog.d(TAG, "packageNames :" + packageNames);
        return packageNames;
    }

    private int matchMunicationMode(int pid) {
        DebugLog.d(TAG, "matchMunicationMode pid :" + pid + " modeinfo isEmpty :" + this.mAppsMode.isEmpty());
        String appName = this.mOppoMultimediaService.getPackageNameByPid(pid);
        if (!this.mAppsMode.isEmpty()) {
            for (Entry<Integer, Integer> entry : this.mAppsMode.entrySet()) {
                int tPid = ((Integer) entry.getKey()).intValue();
                if (((Integer) entry.getValue()).intValue() == 3) {
                    String packageName = this.mOppoMultimediaService.getPackageNameByPid(tPid);
                    DebugLog.d(TAG, "packageName :" + packageName + " appName:" + appName);
                    if (!(packageName == null || appName == null)) {
                        if (packageName.contains(appName) || appName.contains(packageName)) {
                            return tPid;
                        }
                    }
                }
            }
        }
        return pid;
    }

    private void broadcastAudioModeInfo() {
        try {
            String packageNames = getPackageNamesFromModeInfo();
            DebugLog.d(TAG, "broadcastAudioModeInfo packageNames = " + packageNames);
            if (packageNames != null && packageNames.indexOf(",") > 0 && ActivityManagerNative.isSystemReady()) {
                DebugLog.d(TAG, "broadcast AudioModeInfo");
                Intent broadcast = new Intent(OppoMultimediaServiceDefine.ACTION_AUDIO_MODE_INFO);
                broadcast.putExtra(OppoMultimediaServiceDefine.EXTRA_MODE_INFO, packageNames);
                this.mOppoMultimediaService.sendBroadcastToAll(broadcast);
                this.mRecordFailedPackage = null;
            }
        } catch (Exception e) {
        }
    }

    private boolean isAudioPlayEffectByMode() {
        DebugLog.d(TAG, "isAudioPlayEffectByMode");
        String activeAudioPidsStr = getActiveAudioPids();
        if (activeAudioPidsStr == null) {
            DebugLog.d(TAG, "activeAudioPidsStr is null return true");
            return true;
        }
        String[] activeAudioPids = activeAudioPidsStr.split(":");
        if (activeAudioPids == null || activeAudioPids.length < 1) {
            DebugLog.d(TAG, "activeAudioPids is null return true");
            return true;
        }
        int i = 0;
        while (i < activeAudioPids.length) {
            if (this.mAppsMode.size() > 0) {
                DebugLog.d(TAG, "activeAudioPids[" + i + "] = " + activeAudioPids[i]);
                try {
                    if (activeAudioPids[i] != null && this.mAppsMode.containsKey(Integer.valueOf(Integer.parseInt(activeAudioPids[i])))) {
                        int mode = ((Integer) this.mAppsMode.get(Integer.valueOf(Integer.parseInt(activeAudioPids[i])))).intValue();
                        DebugLog.d(TAG, "mode = " + mode + " pid = " + activeAudioPids[i]);
                        if (mode != 0) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
            }
            i++;
        }
        DebugLog.d(TAG, "return false");
        return false;
    }

    private boolean isAudioRecordEffectByMode(int pid) {
        DebugLog.d(TAG, "isAudioRecordEffectByMode :" + pid);
        if (pid > 0) {
            HashMap<String, String> mAudioInfos = getAudioInfoFromAudioSystem();
            if (!(mAudioInfos == null || (mAudioInfos.isEmpty() ^ 1) == 0 || !mAudioInfos.containsKey(OppoMultimediaServiceDefine.KEY_AUDIO_GET_RECORD_INFOS))) {
                String moduleInfos = (String) mAudioInfos.get(OppoMultimediaServiceDefine.KEY_AUDIO_GET_RECORD_INFOS);
                DebugLog.d(TAG, "moduleInfos = " + moduleInfos);
                if (moduleInfos != null) {
                    String[] str1 = moduleInfos.split(":");
                    if (str1 != null && str1.length > 1) {
                        int i = 0;
                        while (i < str1.length) {
                            try {
                                if (!(str1[i] == null || str1[i + 1] == null)) {
                                    int tSession = Integer.parseInt(str1[i]);
                                    if (pid == Integer.parseInt(str1[i + 1])) {
                                        return true;
                                    }
                                }
                                i += 2;
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public String getActiveAudioPids() {
        String pids = this.mAudioManager.getParameters("get_pid");
        if (pids == null || pids.length() == 0) {
            DebugLog.d(TAG, "getActiveAudioPids is null");
            return null;
        }
        DebugLog.d(TAG, "getActiveAudioPids pids = " + pids);
        return pids;
    }

    private ArrayList<String> getActiveAudioAppList(String pID) {
        if (pID == null) {
            return null;
        }
        String[] activePid = pID.split(":");
        ArrayList<Integer> activePidList = new ArrayList();
        if (activePid == null) {
            return null;
        }
        for (String str : activePid) {
            if (str != null) {
                try {
                    if (str.length() > 0) {
                        activePidList.add(Integer.valueOf(Integer.parseInt(str)));
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        DebugLog.d(TAG, "activePidList = " + activePidList);
        ArrayList<String> activeAudioAppList = new ArrayList();
        for (RunningAppProcessInfo appProcess : ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses()) {
            if (activePidList.contains(Integer.valueOf(appProcess.pid))) {
                activeAudioAppList.add(appProcess.processName);
            }
        }
        DebugLog.d(TAG, "activeAudioAppList = " + activeAudioAppList + " applist size :" + activeAudioAppList.size());
        return activeAudioAppList;
    }

    public boolean isApplicationPlaying(String caller) {
        DebugLog.d(TAG, "isApplicationPlaying caller = " + caller);
        ArrayList<String> activeAudioAppList = getActiveAudioAppList(getActiveAudioPids());
        DebugLog.d(TAG, "activeAudioAppList = " + activeAudioAppList);
        if (activeAudioAppList == null) {
            return false;
        }
        for (String appName : activeAudioAppList) {
            if (appName.contains(caller)) {
                DebugLog.d(TAG, "caller = " + caller + " isplaying");
                return true;
            }
        }
        return false;
    }

    private boolean isAfMusicActiveRecently() {
        if (AudioSystem.isStreamActive(3, 1000)) {
            return true;
        }
        return AudioSystem.isStreamActiveRemotely(3, 1000);
    }

    private boolean isNeedGuard() {
        if (this.mAudioManager.getMode() != 3 || (this.mOppoMultimediaService.isInCallState() ^ 1) == 0 || (this.mAudioManager.isBluetoothScoOn() ^ 1) == 0) {
            return false;
        }
        return true;
    }

    public void updateForgoundInfo(String info) {
        DebugLog.d(TAG, "++updataForgoundInfo :" + info + " mode = " + oppoGetMode());
        synchronized (this.mAudioServiceLock) {
            if (info != null) {
                if (isNeedGuard()) {
                    String[] arrStr = info.split(",");
                    if (arrStr != null && arrStr.length == 4) {
                        try {
                            int sceneID = Integer.parseInt(arrStr[0]);
                            int forgroundPid = Integer.parseInt(arrStr[1]);
                            int forgroundFlag = Integer.parseInt(arrStr[2]);
                            DebugLog.d(TAG, "sceneID :" + sceneID + " forgroundPid:" + forgroundPid + " forgroundFlag:" + forgroundFlag);
                            String attribute = this.mOppoDaemonListHelper.getAttributeByAppPid("display-notify", forgroundPid);
                            DebugLog.d(TAG, "attribute :" + attribute);
                            if (attribute != null) {
                                if (attribute.equals("mode")) {
                                    String forgroundAppName = packageNameFormat(this.mOppoMultimediaService.getPackageNameByPid(forgroundPid));
                                    if (forgroundFlag == 0) {
                                        if (getRecordThreadState()) {
                                            DebugLog.d(TAG, "In Recording Status");
                                            return;
                                        }
                                        int matchPid = matchMunicationMode(forgroundPid);
                                        DebugLog.d(TAG, "matchPid :" + matchPid);
                                        if (matchPid > 0 && getActiveAudioPids() == null) {
                                            this.mAudioManager.removeMode(matchPid, forgroundAppName);
                                            DebugLog.d(TAG, "setPhoneState MODE_NORMAL");
                                            AudioSystem.setPhoneState(0);
                                            this.mOppoMultimediaService.setEventToLocalService(102, getPackageNamesFromModeInfo());
                                        }
                                    }
                                } else if (attribute.equals(OppoMultimediaServiceDefine.KEY_AUDIO_GAME_SCENE)) {
                                    String keys = "";
                                    if (forgroundFlag == 1) {
                                        this.mAudioManager.setParameters("gamescenechange=1");
                                    } else if (this.mOppoMultimediaService.getAppImportanceByPid(forgroundPid) == 400) {
                                        this.mAudioManager.setParameters("gamescenechange=0");
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
        }
    }

    public void checkAllModes(int pid) {
        DebugLog.d(TAG, "checkAllModes pid :" + pid);
        synchronized (this.mAudioServiceLock) {
            DebugLog.d(TAG, "mode list size  :" + this.mAppsMode.size());
            broadcastCameraUserPid();
            if (this.mAudioManager.getMode() != 3 || (this.mOppoMultimediaService.isInCallState() ^ 1) == 0 || (this.mAudioManager.isBluetoothScoOn() ^ 1) == 0) {
                DebugLog.d(TAG, "checkAllModes,do nothing");
            } else if (isAudioPlayEffectByMode() || (isAudioRecordEffectByMode(pid) ^ 1) == 0) {
                DebugLog.d(TAG, "setPhoneState getMode");
                AudioSystem.setPhoneState(this.mAudioManager.oppoGetMode());
            } else {
                DebugLog.d(TAG, "setPhoneState MODE_NORMAL");
                AudioSystem.setPhoneState(0);
                this.mOppoMultimediaService.setEventToLocalService(102, getPackageNamesFromModeInfo());
            }
        }
    }

    public String execCommand(String args) {
        DebugLog.d(TAG, "execCommand = " + args);
        String result = null;
        try {
            Process pro = Runtime.getRuntime().exec(args);
            InputStream is = pro.getInputStream();
            byte[] buffer = new byte[1024];
            StringBuilder builder = new StringBuilder();
            while (true) {
                int len = is.read(buffer);
                if (len != -1) {
                    builder.append(new String(buffer, 0, len));
                } else {
                    result = builder.toString();
                    pro.destroy();
                    return result;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }
    }

    private String getDumpCommandInfo(String cmd) {
        DebugLog.d(TAG, "+getDumpCommandInfo");
        String result = null;
        String commandInfo = execCommand("dumpsys " + cmd);
        if (commandInfo != null) {
            String tempStr = commandInfo.replaceAll("\n", " ");
            if (tempStr != null) {
                result = tempStr.replaceAll(" +", " ");
            }
        }
        DebugLog.d(TAG, "-getDumpCommandInfo");
        return result;
    }

    private String getAudioPolicyInfo() {
        String policyInfos = this.mAudioManager.getParameters("audio_policy_infos");
        if (policyInfos == null || policyInfos.length() == 0) {
            DebugLog.d(TAG, "getAudioPolicyInfo is null");
            return null;
        }
        if (policyInfos != null) {
            policyInfos = policyInfos.replaceAll("\n", " ");
            if (policyInfos != null) {
                policyInfos = policyInfos.replaceAll(" +", " ");
            }
        }
        DebugLog.d(TAG, "getAudioPolicyInfo : " + policyInfos);
        return policyInfos;
    }

    public void killAudioSystem() {
        String cmds = "ps";
        String tagStart = "audioserver ";
        String tagEnd = " audioserver";
        if (!this.mOppoMultimediaService.isInCallState()) {
            String processinfo = execCommand(cmds);
            if (processinfo != null) {
                while (true) {
                    int foundindex = processinfo.indexOf(tagStart);
                    if (foundindex >= 0) {
                        try {
                            String mediaInfo;
                            processinfo = processinfo.substring(foundindex);
                            if (processinfo.length() < 100) {
                                mediaInfo = processinfo;
                            } else {
                                mediaInfo = processinfo.substring(0, 100);
                            }
                            DebugLog.d(TAG, "mediaInfo :" + mediaInfo);
                            if (mediaInfo == null) {
                                return;
                            }
                            if (mediaInfo.contains(tagEnd)) {
                                try {
                                    int mediaPid = Integer.parseInt(mediaInfo.replaceAll(" +", " ").split(" ")[1]);
                                    DebugLog.d(TAG, "mediaPid = " + mediaPid);
                                    this.mOppoMultimediaService.killProcessByPid(mediaPid);
                                    return;
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                processinfo = processinfo.substring(tagStart.length());
                            }
                        } catch (StringIndexOutOfBoundsException e2) {
                            e2.printStackTrace();
                            return;
                        }
                    }
                    return;
                }
            }
        }
    }

    private boolean isNeedCheckAudioSystem() {
        long intervals = SystemClock.elapsedRealtime() - this.mLastCheckAudioTime;
        DebugLog.d(TAG, "intervals :" + intervals);
        if (intervals < AudioCheckIntervals) {
            DebugLog.d(TAG, "check intervals must at least 60s");
            return false;
        }
        this.mLastCheckAudioTime = SystemClock.elapsedRealtime();
        return true;
    }

    public boolean checkAudioSystem(boolean isCheck) {
        DebugLog.d(TAG, "checkAudioSystem isCheck :" + isCheck);
        if (!isNeedCheckAudioSystem() && isCheck) {
            DebugLog.d(TAG, "no need check again");
            return false;
        } else if (this.mOppoMultimediaService.isInCallState()) {
            DebugLog.d(TAG, "in calling no need check");
            return false;
        } else if (getRecordThreadState()) {
            DebugLog.d(TAG, "audiosystem in recording state");
            return false;
        } else {
            DebugLog.d(TAG, "start check audiosystem");
            String activeAudioPidsStr = getActiveAudioPids();
            if (activeAudioPidsStr == null) {
                String audioPolicyInfos = getAudioPolicyInfo();
                if (audioPolicyInfos == null) {
                    return false;
                }
                while (true) {
                    int foundindex = audioPolicyInfos.indexOf(AUDIO_POLICY_STREAM_INFOS);
                    if (foundindex < 0) {
                        return false;
                    }
                    String policyStreamInfos = audioPolicyInfos.substring(foundindex);
                    if (parseStreamInfo(policyStreamInfos.substring(0, 200)) && getActiveAudioPids() == null && (getRecordThreadState() ^ 1) != 0) {
                        DebugLog.d(TAG, "AudioPolicyManager has exception");
                        return true;
                    }
                    audioPolicyInfos = policyStreamInfos.substring(AUDIO_POLICY_STREAM_INFOS.length() + 1);
                }
            } else {
                ArrayList<String> activeAudioAppList = getActiveAudioAppList(activeAudioPidsStr);
                if (activeAudioAppList == null || activeAudioAppList.isEmpty()) {
                    if (isCheck) {
                        DebugLog.d(TAG, "AudioFlinger maybe has exception");
                        return true;
                    } else if (isAudioInSlienceState()) {
                        DebugLog.d(TAG, "AudioFlinger has exception");
                        return true;
                    }
                }
                DebugLog.d(TAG, "-checkAudioSystem");
                return false;
            }
        }
    }

    private boolean isAudioInSlienceState() {
        String slienceState = this.mAudioManager.getParameters("get_silence");
        if (slienceState == null || slienceState.length() == 0) {
            return false;
        }
        DebugLog.d(TAG, "slienceState:" + slienceState);
        if (slienceState.equals("1")) {
            return true;
        }
        return false;
    }

    private boolean parseStreamInfo(String info) {
        if (info == null) {
            return false;
        }
        String streamInfo = info.substring(AUDIO_POLICY_STREAM_INFOS.length() + 1);
        String[] arrinfo = streamInfo.split(" ");
        DebugLog.d(TAG, "info :" + streamInfo);
        int i = 0;
        while (i < arrinfo.length) {
            try {
                int streamType = Integer.parseInt(arrinfo[i]);
                float volume = Float.parseFloat(arrinfo[i + 1]);
                int refCount = Integer.parseInt(arrinfo[i + 2]);
                int muteCount = Integer.parseInt(arrinfo[i + 3]);
                DebugLog.d(TAG, "streamType = " + streamType + " volume = " + volume + " refCount = " + refCount + " muteCount = " + muteCount);
                if (streamType > 5) {
                    return false;
                }
                if (streamType == 0) {
                    if (refCount > 0 || muteCount > 0) {
                        return true;
                    }
                } else if ((streamType == 3 || streamType == 2) && muteCount > 0) {
                    return true;
                }
                i += 4;
            } catch (NumberFormatException e) {
            }
        }
        return false;
    }

    public boolean isStreamTypeAdjustRevise() {
        DebugLog.d(TAG, "isStreamTypeAdjustRevise");
        if (this.mAudioManager.getMode() != 3 || (this.mOppoMultimediaService.isInCallState() ^ 1) == 0 || getRecordThreadState() || AudioSystem.isStreamActive(0, 0)) {
            return false;
        }
        if (!isAfMusicActiveRecently()) {
            return getModeFromAudioPolicyManager() == 0;
        } else {
            OppoMultimediaManager.getInstance(this.mContext).setEventInfo(103, null);
            return true;
        }
    }

    public boolean getRecordThreadState() {
        int recordThreadActiveNum = 0;
        HashMap<String, String> mAudioInfos = getAudioInfoFromAudioSystem();
        if (mAudioInfos == null || (mAudioInfos.isEmpty() ^ 1) == 0 || !mAudioInfos.containsKey(OppoMultimediaServiceDefine.KEY_AUDIO_GET_RECORD_STATE)) {
            return false;
        }
        String recordNum = (String) mAudioInfos.get(OppoMultimediaServiceDefine.KEY_AUDIO_GET_RECORD_STATE);
        if (recordNum == null) {
            return false;
        }
        try {
            recordThreadActiveNum = Integer.parseInt(recordNum);
        } catch (NumberFormatException e) {
        }
        if (recordThreadActiveNum > 0) {
            return true;
        }
        return false;
    }

    public void OppoSetPhoneState(int mode) {
        DebugLog.d(TAG, "OppoSetPhoneState mode = " + mode);
        synchronized (this.mAudioServiceLock) {
            if (mode < 0) {
                AudioSystem.setPhoneState(oppoGetMode());
            } else {
                AudioSystem.setPhoneState(mode);
            }
        }
    }

    public int getMode() {
        return this.mAudioManager.getMode();
    }

    public int oppoGetMode() {
        return this.mAudioManager.oppoGetMode();
    }

    private int getModeFromAudioPolicyManager() {
        int mode = oppoGetMode();
        String strMode = this.mAudioManager.getParameters("phone_state");
        DebugLog.d(TAG, "mode  = " + mode + " strMode = " + strMode);
        if (strMode == null || strMode.length() == 0) {
            return mode;
        }
        try {
            mode = Integer.parseInt(strMode);
        } catch (NumberFormatException e) {
        }
        DebugLog.d(TAG, "mode :" + mode);
        return mode;
    }

    private HashMap<String, String> getAudioInfoFromAudioSystem() {
        HashMap<String, String> audiInfoMaps = new HashMap();
        String audioinfos = this.mAudioManager.getParameters(OppoMultimediaServiceDefine.KEY_AUDIO_GET_AUDIO_INFO);
        if (audioinfos == null || audioinfos.length() == 0) {
            return null;
        }
        String[] str1 = audioinfos.split(";");
        if (str1 != null) {
            for (int i = 0; i < str1.length; i++) {
                if (str1[i] != null) {
                    String[] str2 = str1[i].split(",");
                    if (str2.length == 2) {
                        DebugLog.d(TAG, "index = " + i + " key = " + str2[0] + " value = " + str2[1]);
                        audiInfoMaps.put(str2[0], str2[1]);
                    }
                }
            }
        }
        return audiInfoMaps;
    }

    public int getPidBySession(int session, String module) {
        synchronized (this.mAudioServiceLock) {
            HashMap<String, String> mAudioInfos = getAudioInfoFromAudioSystem();
            if (!(mAudioInfos == null || (mAudioInfos.isEmpty() ^ 1) == 0 || !mAudioInfos.containsKey(module))) {
                String moduleInfos = (String) mAudioInfos.get(module);
                DebugLog.d(TAG, "moduleInfos = " + moduleInfos);
                if (moduleInfos != null) {
                    String[] str1 = moduleInfos.split(":");
                    if (str1 != null && str1.length > 1) {
                        int i = 0;
                        while (i < str1.length) {
                            try {
                                if (!(str1[i] == null || str1[i + 1] == null)) {
                                    int tSession = Integer.parseInt(str1[i]);
                                    int tPid = Integer.parseInt(str1[i + 1]);
                                    if (tSession == session) {
                                        return tPid;
                                    }
                                }
                                i += 2;
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    /* JADX WARNING: Missing block: B:3:0x0023, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showRecordHintDialog(String info) {
        DebugLog.d(TAG, "+showRecordHintDialog info = " + info);
        if (isNeedShowRecordFailedHint() && info != null && !isKeyguardLocked()) {
            String[] strarr = info.split("=");
            DebugLog.d(TAG, "mShowAlertDialog :" + this.mShowAlertDialog);
            if (!(strarr == null || strarr.length != 2 || (this.mShowAlertDialog ^ 1) == 0)) {
                AlertDialog mAlertDialog = new Builder(this.mContext).setTitle(strarr[0]).setPositiveButton(strarr[1], new OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                    }
                }).create();
                if (mAlertDialog != null) {
                    mAlertDialog.setOnDismissListener(new OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            DebugLog.d(AudioServiceState.TAG, "onDismiss");
                            AudioServiceState.this.broadcastRecordInvalid();
                            AudioServiceState.this.mShowAlertDialog = false;
                        }
                    });
                    mAlertDialog.getWindow().setType(2003);
                    mAlertDialog.show();
                    this.mShowAlertDialog = true;
                }
            }
            DebugLog.d(TAG, "-showRecordHintDialog");
        }
    }

    public void setVoiceSmallFlag() {
        this.mBlocking = true;
    }

    private boolean isKeyguardLocked() {
        boolean locked;
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        }
        if (this.mKeyguardManager != null) {
            locked = this.mKeyguardManager.isKeyguardLocked();
        } else {
            locked = false;
        }
        DebugLog.d(TAG, "isKeyguardLocked() locked=" + locked + ", mKeyguardManager=" + this.mKeyguardManager);
        return locked;
    }

    private String packageNameFormat(String packageName) {
        if (packageName == null || packageName.indexOf(":") == -1) {
            return packageName;
        }
        String[] arrStr = packageName.split(":");
        if (arrStr == null || arrStr.length <= 0) {
            return packageName;
        }
        return arrStr[0];
    }

    public String getRecordFailedInfo(String info) {
        String recordInfo = "";
        String package1 = "";
        String package2 = "";
        if (info == null) {
            return null;
        }
        String[] pids = info.split(",");
        if (pids == null || pids.length > 2) {
            return null;
        }
        try {
            int pid1 = Integer.parseInt(pids[0]);
            if (pid1 < 0) {
                return null;
            }
            package1 = this.mOppoMultimediaService.getPackageNameByPid(pid1);
            if (package1 == null) {
                DebugLog.d(TAG, "getRecordFailedInfo process :" + pid1 + " is not exist!!!");
                killAudioSystem();
                return null;
            }
            package1 = packageNameFormat(package1);
            if (pids.length == 2) {
                int pid2 = Integer.parseInt(pids[1]);
                if (pid2 < 0) {
                    return null;
                }
                package2 = this.mOppoMultimediaService.getPackageNameByPid(pid2);
                this.mShowRecordFailedHintCurrentPid = pid2;
                this.mShowRecordHintCurrentRunningPid = pid1;
                this.mRecordFailedPackage = package2;
            }
            recordInfo = package1 + "," + package2;
            DebugLog.d(TAG, "+recordInfo = " + recordInfo);
            if (package1 == null || package2 == null) {
                return null;
            }
            package2 = packageNameFormat(package2);
            if (package2 != null && package1.equals(package2)) {
                return null;
            }
            if (this.mOppoDaemonListHelper.checkIsInDaemonlistByName(OppoDaemonListHelper.TAG_MODULE[ModuleTag.RECORD_CONFLICT.ordinal()], package2)) {
                String value = this.mOppoDaemonListHelper.getAttributeByAppName(OppoDaemonListHelper.TAG_MODULE[ModuleTag.RECORD_CONFLICT.ordinal()], package2);
                DebugLog.d(TAG, "record-conflict value :" + value);
                if (value == null) {
                    this.mShowRecordFailedHintInterval = 1000;
                } else if (value.equals(OppoMultimediaServiceDefine.APP_LIST_ATTRIBUTE_RECORD_NO_HINT)) {
                    return null;
                } else {
                    try {
                        this.mShowRecordFailedHintInterval = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        this.mShowRecordFailedHintInterval = 1000;
                    }
                }
            } else {
                this.mShowRecordFailedHintInterval = 1000;
            }
            return recordInfo;
        } catch (NumberFormatException e2) {
        }
    }

    private boolean isNeedShowRecordFailedHint() {
        DebugLog.d(TAG, "mShowRecordFailedHintCurrentPid = " + this.mShowRecordFailedHintCurrentPid + " mShowRecordFailedHintLastPid = " + this.mShowRecordFailedHintLastPid);
        DebugLog.d(TAG, "mShowRecordHintCurrentRunningPid = " + this.mShowRecordHintCurrentRunningPid + " mShowRecorddHintLastRunningPid = " + this.mShowRecorddHintLastRunningPid);
        DebugLog.d(TAG, "elapsedRealtime = " + SystemClock.elapsedRealtime() + " mLastShowRecordFailedHintTime = " + this.mLastShowRecordFailedHintTime + " mShowRecordFailedHintCount = " + this.mShowRecordFailedHintCount + " mShowRecordFailedHintInterval" + this.mShowRecordFailedHintInterval);
        if (this.mRecordFailedPackage != null && this.mRecordFailedPackage.equals(OppoMultimediaServiceDefine.CAMERA_PACKAGE_NAME)) {
            this.mShowRecordFailedHintCount = 0;
            DebugLog.d(TAG, "camera start failed");
            return true;
        } else if (SystemClock.elapsedRealtime() - this.mLastShowRecordFailedHintTime < ((long) this.mShowRecordFailedHintInterval)) {
            return false;
        } else {
            if (this.mShowRecordFailedHintCurrentPid == this.mShowRecordFailedHintLastPid && this.mShowRecordHintCurrentRunningPid == this.mShowRecorddHintLastRunningPid) {
                this.mShowRecordFailedHintCount++;
            } else {
                this.mShowRecordFailedHintCount = 0;
            }
            this.mShowRecordFailedHintLastPid = this.mShowRecordFailedHintCurrentPid;
            this.mShowRecorddHintLastRunningPid = this.mShowRecordHintCurrentRunningPid;
            this.mLastShowRecordFailedHintTime = SystemClock.elapsedRealtime();
            return true;
        }
    }

    private void broadcastRecordInvalid() {
        try {
            DebugLog.d(TAG, "broadcastRecordInvalid mRecordFailedPackage = " + this.mRecordFailedPackage);
            if (ActivityManagerNative.isSystemReady()) {
                Intent broadcast = new Intent(OppoMultimediaServiceDefine.ACTION_AUDIO_RECORD_INVALID);
                broadcast.putExtra(OppoMultimediaServiceDefine.EXTRA_RECORD_START_PACKAGE_TYPE, this.mRecordFailedPackage);
                this.mOppoMultimediaService.sendBroadcastToAll(broadcast);
                this.mRecordFailedPackage = null;
            }
        } catch (Exception e) {
        }
    }

    public void broadcastRecordEvent(int event, int pid) {
        try {
            DebugLog.d(TAG, "broadcastRecordEvent event = " + event + " pid:" + pid + " mLastRecordPid:" + this.mLastRecordPid + " mLastRecordEvent:" + this.mLastRecordEvent);
            if (ActivityManagerNative.isSystemReady()) {
                String msg = null;
                if (event == 19) {
                    int session = pid;
                    if (getPidBySession(pid, OppoMultimediaServiceDefine.KEY_AUDIO_GET_RECORD_INFOS) < 0) {
                        event = 9;
                    } else {
                        return;
                    }
                }
                if (event != 9) {
                    if (pid >= 0) {
                        if (this.mLastRecordPid > 0 && this.mLastRecordPid == pid && this.mLastRecordEvent == 8) {
                            return;
                        }
                    }
                    return;
                }
                if (event == 8) {
                    msg = OppoMultimediaServiceDefine.ACTION_AUDIO_RECORD_START;
                    this.mLastRecordPid = pid;
                    broadcastCameraUserPid();
                    checkAllModes(pid);
                } else if (event == 9) {
                    msg = OppoMultimediaServiceDefine.ACTION_AUDIO_RECORD_STOP;
                    this.mLastRecordPid = -1;
                    DebugLog.d(TAG, "broadcastRecordEvent");
                    if (this.mBlocking && AudioSystem.getDeviceConnectionState(-2147483632, "") == 0) {
                        DebugLog.d(TAG, "broadcastRecordEvent mBlocking = true");
                        this.mOppoMultimediaService.setEventToLocalService(OppoMultimediaServiceDefine.EVENT_LOCALSERVICE_VOICE_SMALL, "");
                        this.mBlocking = false;
                    }
                }
                Intent broadcast = new Intent(msg);
                broadcast.putExtra(OppoMultimediaServiceDefine.EXTRA_RECORD_ACTION_PID, pid);
                this.mOppoMultimediaService.sendBroadcastToAll(broadcast);
                this.mLastRecordEvent = event;
            }
        } catch (Exception e) {
        }
    }

    public void checkAppcationAndKill() {
        DebugLog.d(TAG, "checkAppcationAndKill");
        if (this.mOppoMultimediaService.getFunEnable(DaemonFun.KILLAPP.ordinal())) {
            String pids = getActiveAudioPids();
            if (pids != null) {
                String[] activePid = pids.split(":");
                if (activePid != null) {
                    for (String str : activePid) {
                        try {
                            OppoDaemonListInfo listObject = this.mOppoDaemonListHelper.getListObjectByAppPid(OppoDaemonListHelper.TAG_MODULE[ModuleTag.VOLUME.ordinal()], Integer.parseInt(str));
                            if (listObject == null) {
                                continue;
                            } else {
                                String attribute = listObject.getAttribute();
                                if (attribute != null && attribute.equals(OppoMultimediaServiceDefine.APP_LIST_ATTRIBUTE_KILL)) {
                                    String appName = listObject.getName();
                                    if (!(appName == null || (this.mOppoMultimediaService.isAppTopActivity(appName) ^ 1) == 0)) {
                                        this.mOppoMultimediaService.killProcessByPackageName(appName);
                                        this.mOppoMultimediaService.setEventToLocalService(107, appName);
                                    }
                                    return;
                                }
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
        }
    }

    public boolean isHasActiveStream() {
        String pids = getActiveAudioPids();
        if (pids == null || pids.split(":") == null || !this.mAudioManager.isMusicActive()) {
            return false;
        }
        return true;
    }

    public boolean isStreamShouldMute(int streamType, int callState) {
        if (!this.mOppoMultimediaService.getFunEnable(DaemonFun.STREAMMUTE.ordinal())) {
            return false;
        }
        if (isStreamMute(streamType)) {
            return false;
        }
        String pids = getActiveAudioPids();
        if (pids == null) {
            return false;
        }
        String[] activePid = pids.split(":");
        if (activePid == null) {
            return false;
        }
        if (callState == 1 && isWiredHeadsetOn()) {
            return false;
        }
        if (!this.mAudioManager.isMusicActive()) {
            return false;
        }
        String mutePackageName = "";
        int i = 0;
        int length = activePid.length;
        while (i < length) {
            String str = activePid[i];
            try {
                String attribute = this.mOppoDaemonListHelper.getAttributeByAppPid(OppoDaemonListHelper.TAG_MODULE[ModuleTag.VOLUME.ordinal()], Integer.parseInt(str));
                if (attribute != null && attribute.equals(OppoMultimediaServiceDefine.APP_LIST_ATTRIBUTE_NO_NEED_MUTE_IN_CALL)) {
                    return false;
                }
                String packageName = this.mOppoMultimediaService.getPackageNameByPid(Integer.parseInt(str));
                if (packageName != null) {
                    mutePackageName = packageName;
                }
                i++;
            } catch (NumberFormatException e) {
            }
        }
        this.mOppoMultimediaService.setEventToLocalService(106, mutePackageName);
        return true;
    }

    public boolean isHasActivePidInList(String module, String feature) {
        String pids = getActiveAudioPids();
        if (pids == null) {
            return false;
        }
        String[] activePid = pids.split(":");
        if (activePid == null) {
            return false;
        }
        for (String str : activePid) {
            try {
                String attribute = this.mOppoDaemonListHelper.getAttributeByAppPid(module, Integer.parseInt(str));
                if (attribute != null && attribute.equals(feature)) {
                    return true;
                }
            } catch (NumberFormatException e) {
            }
        }
        return false;
    }

    public boolean isWiredHeadsetOn() {
        boolean isHeadsetConnect = false;
        if (this.mAudioManager.isWiredHeadsetOn() || this.mAudioManager.isBluetoothScoOn() || this.mAudioManager.isBluetoothA2dpOn()) {
            isHeadsetConnect = true;
        }
        DebugLog.d(TAG, "isWiredHeadsetOn isHeadsetConnect :" + isHeadsetConnect);
        return isHeadsetConnect;
    }

    public void setStreamMute(int streamType, boolean state) {
        DebugLog.d(TAG, "+setStreamMute streamType = " + streamType + " state:" + state);
        if (state && isStreamMute(streamType)) {
            this.mDoMusicMute = false;
            return;
        }
        this.mAudioManager.setStreamMute(streamType, state);
        if (state) {
            this.mDoMusicMute = true;
        } else {
            this.mDoMusicMute = false;
        }
        DebugLog.d(TAG, "-setStreamMute streamType = " + streamType + " state:" + state);
    }

    public boolean isStreamMute(int streamType) {
        return this.mAudioManager.isStreamMute(streamType);
    }

    public void releaseAudioTrack() {
        DebugLog.d(TAG, "releaseAudioTrack");
        if (this.mOppoMultimediaService.getFunEnable(DaemonFun.RRLEASEAUDIOTRACK.ordinal())) {
            synchronized (this.mAudioServiceLock) {
                HashMap<Integer, Integer> playbackThreadInfos = new HashMap();
                HashMap<String, String> mAudioInfos = getAudioInfoFromAudioSystem();
                if (!(mAudioInfos == null || (mAudioInfos.isEmpty() ^ 1) == 0 || !mAudioInfos.containsKey(OppoMultimediaServiceDefine.KEY_AUDIO_GET_PLAYBACK_INFOS))) {
                    String playbackThreadStr = (String) mAudioInfos.get(OppoMultimediaServiceDefine.KEY_AUDIO_GET_PLAYBACK_INFOS);
                    DebugLog.d(TAG, "playbackThreadStr = " + playbackThreadStr);
                    if (playbackThreadStr != null) {
                        String[] str1 = playbackThreadStr.split(":");
                        if (str1 != null && str1.length > 1) {
                            int foundPid = -1;
                            int maxRefCount = 0;
                            int totalTrackCount = 0;
                            DebugLog.d(TAG, "str1 length = " + str1.length);
                            int i = 0;
                            while (i < str1.length) {
                                try {
                                    if (!(str1[i] == null || str1[i + 1] == null)) {
                                        int tSession = Integer.parseInt(str1[i]);
                                        int tPid = Integer.parseInt(str1[i + 1]);
                                        if (playbackThreadInfos.containsKey(Integer.valueOf(tPid))) {
                                            int curRefCount = ((Integer) playbackThreadInfos.get(Integer.valueOf(tPid))).intValue() + 1;
                                            playbackThreadInfos.put(Integer.valueOf(tPid), Integer.valueOf(curRefCount));
                                            if (curRefCount > maxRefCount) {
                                                maxRefCount = curRefCount;
                                                foundPid = tPid;
                                            }
                                        } else {
                                            playbackThreadInfos.put(Integer.valueOf(tPid), Integer.valueOf(0));
                                        }
                                        totalTrackCount++;
                                    }
                                    i += 2;
                                } catch (NumberFormatException e) {
                                }
                            }
                            DebugLog.d(TAG, "foundPid : " + foundPid + " maxRefCount:" + maxRefCount + " totalTrackCount :" + totalTrackCount);
                            if (foundPid > 0 && ((long) totalTrackCount) >= CREATE_TRACK_MAX_COUNT) {
                                String packageName = this.mOppoMultimediaService.getPackageNameByPid(foundPid);
                                boolean isAppTop = this.mOppoMultimediaService.isAppTopActivity(packageName);
                                DebugLog.d(TAG, "foundPid : " + foundPid + " packageName:" + packageName + " isAppTop :" + isAppTop + " mCreateTrackFailedCount :" + this.mCreateTrackFailedCount);
                                if (!(packageName == null || (isAppTop ^ 1) == 0)) {
                                    this.mCreateTrackFailedCount++;
                                    if (((long) this.mCreateTrackFailedCount) > CREATE_TRACK_MAX_FAILED) {
                                        this.mOppoMultimediaService.killProcessByPackageName(packageName);
                                        this.mOppoMultimediaService.setEventToLocalService(OppoMultimediaServiceDefine.EVENT_LOCALSERVICE_RELEASE_AUDIOTRACK, packageName);
                                        this.mCreateTrackFailedCount = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void mmListRomUpdate() {
        DebugLog.d(TAG, "+mmListRomUpdate");
        this.mAudioManager.setParameters("rom_update=true");
        DebugLog.d(TAG, "-mmListRomUpdate");
    }

    private int getCameraUserPid() {
        String sPid = SystemProperties.get("oppo.camera.pid", "-1");
        DebugLog.d(TAG, "sPid:" + sPid);
        try {
            return Integer.parseInt(sPid);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void broadcastCameraUserPid() {
        int cameraUserPid = getCameraUserPid();
        DebugLog.d(TAG, "cameraUserPid :" + cameraUserPid);
        if (cameraUserPid > 0) {
            boolean isRecording = false;
            boolean isPlaying = false;
            try {
                DebugLog.d(TAG, "broadcastCameraUserPid");
                HashMap<String, String> mAudioInfos = getAudioInfoFromAudioSystem();
                if (!(mAudioInfos == null || (mAudioInfos.isEmpty() ^ 1) == 0)) {
                    String[] str1;
                    int i;
                    int tSession;
                    int tPid;
                    if (mAudioInfos.containsKey(OppoMultimediaServiceDefine.KEY_AUDIO_GET_RECORD_INFOS)) {
                        String recordPidInfo = "";
                        str1 = ((String) mAudioInfos.get(OppoMultimediaServiceDefine.KEY_AUDIO_GET_RECORD_INFOS)).split(":");
                        if (str1 != null && str1.length > 1) {
                            i = 0;
                            while (i < str1.length) {
                                try {
                                    if (!(str1[i] == null || str1[i + 1] == null)) {
                                        tSession = Integer.parseInt(str1[i]);
                                        tPid = Integer.parseInt(str1[i + 1]);
                                        DebugLog.d(TAG, "tSession :" + tSession + " tPid:" + tPid);
                                        if (tPid == cameraUserPid) {
                                            isRecording = true;
                                            break;
                                        }
                                    }
                                    i += 2;
                                } catch (NumberFormatException e) {
                                }
                            }
                        }
                    }
                    if (isRecording) {
                        if (mAudioInfos.containsKey(OppoMultimediaServiceDefine.KEY_AUDIO_GET_PLAYBACK_INFOS)) {
                            String playbackPidInfo = "";
                            str1 = ((String) mAudioInfos.get(OppoMultimediaServiceDefine.KEY_AUDIO_GET_PLAYBACK_INFOS)).split(":");
                            if (str1 != null && str1.length > 1) {
                                i = 0;
                                while (i < str1.length) {
                                    try {
                                        if (!(str1[i] == null || str1[i + 1] == null)) {
                                            tSession = Integer.parseInt(str1[i]);
                                            tPid = Integer.parseInt(str1[i + 1]);
                                            DebugLog.d(TAG, "tSession :" + tSession + " tPid:" + tPid);
                                            if (tPid == cameraUserPid) {
                                                isPlaying = true;
                                                break;
                                            }
                                        }
                                        i += 2;
                                    } catch (NumberFormatException e2) {
                                    }
                                }
                            }
                            DebugLog.d(TAG, "playbackPidInfo :" + playbackPidInfo);
                        }
                    }
                }
            } catch (Exception e3) {
            }
            if ((isRecording & isPlaying) != 0) {
                try {
                    Intent broadcast = new Intent(OppoMultimediaServiceDefine.ACTION_AUDIO_THREAD_INFO);
                    broadcast.putExtra(OppoMultimediaServiceDefine.EXTRA_RECORD_AND_PLAY_THREAD_INFO, cameraUserPid);
                    this.mOppoMultimediaService.sendBroadcastToAll(broadcast);
                } catch (NumberFormatException e4) {
                }
            }
        }
    }
}
