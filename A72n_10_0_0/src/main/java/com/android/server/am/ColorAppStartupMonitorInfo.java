package com.android.server.am;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorAppStartupMonitorInfo {
    public static final String TAG = "ColorAppStartupManager";
    List<ColorAppStartupMonitorRecord> mAppStartList = new ArrayList();
    List<ColorAppStartupMonitorRecord> mBlackListInterceptList = new ArrayList();
    long mCallCount = 0;
    List<ColorAppStartupMonitorRecord> mCallList = new ArrayList();
    String mCalledPkgName = "";
    boolean mCallerIsThird = false;
    String mCallerPkgName = "";
    List<ColorAppStartupMonitorRecord> mGamePayList = new ArrayList();
    List<ColorAppStartupMonitorRecord> mPopupActivityList = new ArrayList();
    String mProcessName = "";
    List<ColorAppStartupMonitorRecord> mProcessStartList = new ArrayList();
    List<ColorAppStartupMonitorRecord> mTrackingMonitorList = new ArrayList();

    static ColorAppStartupMonitorInfo builder(String callerPkgName, String calledPkgName, String callCpnName, String callType, String calledAppExist) {
        ColorAppStartupMonitorInfo appMonitorInfo = new ColorAppStartupMonitorInfo();
        appMonitorInfo.setCallerPkgName(callerPkgName);
        appMonitorInfo.increaseCallCount(calledPkgName, callCpnName, callType, calledAppExist);
        return appMonitorInfo;
    }

    static ColorAppStartupMonitorInfo builderProcessInfo(String packageName, String processName, String hostingType, String startMode, String hostingNameStr) {
        ColorAppStartupMonitorInfo appStartInfo = new ColorAppStartupMonitorInfo();
        appStartInfo.setProcessName(processName);
        appStartInfo.increaseProcessStartCount(packageName, processName, hostingType, startMode, hostingNameStr);
        return appStartInfo;
    }

    static ColorAppStartupMonitorInfo builder(String calleePkg, String calleeCpn, String startMode) {
        ColorAppStartupMonitorInfo appStartInfo = new ColorAppStartupMonitorInfo();
        appStartInfo.setCalledPkgName(calleePkg);
        appStartInfo.increaseBlackListInterceptCount(calleePkg, calleeCpn, startMode);
        return appStartInfo;
    }

    static ColorAppStartupMonitorInfo builderGamePay(String callerPkg, String callerCpn, String calledPkg, String calledCpn) {
        ColorAppStartupMonitorInfo appStartInfo = new ColorAppStartupMonitorInfo();
        appStartInfo.setCallerPkgName(callerPkg);
        appStartInfo.increaseGamePayCount(callerPkg, callerCpn, calledPkg, calledCpn);
        return appStartInfo;
    }

    static ColorAppStartupMonitorInfo buildAppStart(String callerPkg, int callingPid, String callerAppStr, String calledPkg, String cpnName, String hostingType, String startMode) {
        ColorAppStartupMonitorInfo appStartInfo = new ColorAppStartupMonitorInfo();
        appStartInfo.setCallerPkgName(callerPkg);
        appStartInfo.increaseAppStartCount(callingPid, callerAppStr, calledPkg, cpnName, hostingType, startMode);
        return appStartInfo;
    }

    static ColorAppStartupMonitorInfo buildPopupActivity(String callerPkg, String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        ColorAppStartupMonitorInfo appStartInfo = new ColorAppStartupMonitorInfo();
        appStartInfo.setCallerPkgName(callerPkg);
        appStartInfo.increasePopupActivityCount(calledPkg, calledCpnName, topPkg, screenState, type);
        return appStartInfo;
    }

    public static ColorAppStartupMonitorInfo buildTrackingMonitor(String callerPkg, String callerCpn, String calleePkg, String calleeCpn, String intent, String bundle) {
        ColorAppStartupMonitorInfo trackingInfo = new ColorAppStartupMonitorInfo();
        trackingInfo.setCallerPkgName(callerPkg);
        trackingInfo.increaseTrackingMonitorCount(callerPkg, callerCpn, calleePkg, calleeCpn, intent, bundle);
        return trackingInfo;
    }

    public String getCallerPkgName() {
        return this.mCallerPkgName;
    }

    public void setCallerPkgName(String callerPkgName) {
        this.mCallerPkgName = callerPkgName;
    }

    public void setCalledPkgName(String calledPkgName) {
        this.mCalledPkgName = calledPkgName;
    }

    public String getCalledPkgName() {
        return this.mCalledPkgName;
    }

    public void setProcessName(String processName) {
        this.mProcessName = processName;
    }

    public String getProcessName() {
        return this.mProcessName;
    }

    public void increaseCallCount(String calledPkgName, String callCpnName, String callType, String calledAppExist) {
        this.mCallCount++;
        ColorAppStartupMonitorRecord record = getCallRecordInList(calledPkgName, callCpnName, callType);
        if (record == null) {
            this.mCallList.add(ColorAppStartupMonitorRecord.builder(this.mCallerPkgName, calledPkgName, callCpnName, callType, calledAppExist));
            return;
        }
        record.increaseCallCount();
    }

    public ColorAppStartupMonitorRecord getCallRecordInList(String calledPkgName, String callCpnName, String callType) {
        for (ColorAppStartupMonitorRecord record : this.mCallList) {
            if (record.getCalledPkgName().equals(calledPkgName) && record.getCallCpnName().equals(callCpnName) && record.getCallType().equals(callType)) {
                return record;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void increaseProcessStartCount(String packageName, String processName, String hostingType, String startMode, String hostingNameStr) {
        ColorAppStartupMonitorRecord record = getProcessStartInList(processName, hostingType, startMode);
        if (record == null) {
            this.mProcessStartList.add(ColorAppStartupMonitorRecord.builderProcessStart(packageName, processName, hostingType, startMode, hostingNameStr));
            return;
        }
        record.increaseStartCount();
    }

    private ColorAppStartupMonitorRecord getProcessStartInList(String processName, String hostingType, String startMode) {
        for (ColorAppStartupMonitorRecord record : this.mProcessStartList) {
            if (record.getProcessName().equals(processName) && record.getHostingType().equals(hostingType) && record.getStartMode().equals(startMode)) {
                return record;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void increaseBlackListInterceptCount(String calledPkg, String calledCpn, String startMode) {
        ColorAppStartupMonitorRecord record = getBlackListInterceptInList(calledPkg, calledCpn, startMode);
        if (record == null) {
            this.mBlackListInterceptList.add(ColorAppStartupMonitorRecord.builderBlackListIntercept(calledPkg, calledCpn, startMode));
            return;
        }
        record.increaseInterceBlackListCount();
    }

    private ColorAppStartupMonitorRecord getBlackListInterceptInList(String calledPkg, String calledCpn, String startMode) {
        for (ColorAppStartupMonitorRecord record : this.mBlackListInterceptList) {
            if (record.getStartMode().equals(startMode)) {
                return record;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void increaseGamePayCount(String callerPkg, String callerCpn, String calledPkg, String calledCpn) {
        ColorAppStartupMonitorRecord record = getGamePayList(callerCpn, calledPkg, calledCpn);
        if (record == null) {
            this.mGamePayList.add(ColorAppStartupMonitorRecord.builderGamePay(callerPkg, callerCpn, calledPkg, calledCpn));
            return;
        }
        record.increaseGamePayCount();
    }

    private ColorAppStartupMonitorRecord getGamePayList(String callerCpn, String calledPkg, String calledCpn) {
        for (ColorAppStartupMonitorRecord record : this.mGamePayList) {
            if (record.getCallerCpnName().equals(callerCpn) && record.getCalledPkgName().equals(calledPkg) && record.getCallCpnName().equals(calledCpn)) {
                return record;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void increaseAppStartCount(int callingPid, String callerAppStr, String calledPkg, String cpnName, String hostingType, String startMode) {
        ColorAppStartupMonitorRecord record = getAppStartInList(calledPkg, cpnName, hostingType);
        if (record == null) {
            this.mAppStartList.add(ColorAppStartupMonitorRecord.buildAppStart(callingPid, callerAppStr, calledPkg, cpnName, hostingType, startMode));
            return;
        }
        record.increaseAppStartCount();
    }

    private ColorAppStartupMonitorRecord getAppStartInList(String calledPkg, String cpnName, String hostingType) {
        for (ColorAppStartupMonitorRecord record : this.mAppStartList) {
            if (record.getCalledPkgName().equals(calledPkg) && record.getCallCpnName().equals(cpnName) && record.getHostingType().equals(hostingType)) {
                return record;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void increasePopupActivityCount(String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        ColorAppStartupMonitorRecord record = getPopupActivityInList(calledPkg, calledCpnName, topPkg, type);
        if (record == null) {
            this.mPopupActivityList.add(ColorAppStartupMonitorRecord.buildPopupActivityStart(calledPkg, calledCpnName, topPkg, screenState, type));
            return;
        }
        record.increasePopupCount();
    }

    private ColorAppStartupMonitorRecord getPopupActivityInList(String calledPkg, String calledCpnName, String topPkg, String type) {
        for (ColorAppStartupMonitorRecord record : this.mPopupActivityList) {
            if (record.getCalledPkgName().equals(calledPkg) && record.getCallCpnName().equals(calledCpnName) && record.getTopName().equals(topPkg) && record.getPopupType().equals(type)) {
                return record;
            }
        }
        return null;
    }

    public void increaseTrackingMonitorCount(String callerPkg, String callerCpn, String calleePkg, String calleeCpn, String intent, String bundle) {
        ColorAppStartupMonitorRecord record = getTrackingMonitorInList(callerPkg, callerCpn, calleePkg, calleeCpn, intent, bundle);
        if (record == null) {
            this.mTrackingMonitorList.add(ColorAppStartupMonitorRecord.buildTrackingMonitorInfo(callerPkg, callerCpn, calleePkg, calleeCpn, intent, bundle));
            return;
        }
        record.increaseCallCount();
    }

    private ColorAppStartupMonitorRecord getTrackingMonitorInList(String callerPkg, String callerCpn, String calleePkg, String calleeCpn, String intent, String bundle) {
        for (ColorAppStartupMonitorRecord record : this.mTrackingMonitorList) {
            if (record.getCallerPkgName().equals(callerPkg) && record.getCallerCpnName().equals(callerCpn) && record.getCalledPkgName().equals(calleePkg) && record.getCallCpnName().equals(calleeCpn) && record.getIntent().equals(intent)) {
                return record;
            }
        }
        return null;
    }

    public List<String> formatCallInfo() {
        List<String> callList = new ArrayList<>();
        for (ColorAppStartupMonitorRecord record : this.mCallList) {
            callList.add(record.formatToString());
        }
        return callList;
    }

    public void clearCallList() {
        this.mCallList.clear();
    }

    public void dumpInfo() {
        for (ColorAppStartupMonitorRecord record : this.mCallList) {
            Log.v(TAG, record.toString());
        }
    }

    public List<Map<String, String>> getProcessStartMap() {
        List<Map<String, String>> processStartList = new ArrayList<>();
        for (ColorAppStartupMonitorRecord record : this.mProcessStartList) {
            Map<String, String> procMap = new HashMap<>();
            procMap.put("callerPkg", record.getPackageName());
            procMap.put("proc", record.getProcessName());
            procMap.put("host_type", record.getHostingType());
            procMap.put("start_mode", record.getStartMode());
            procMap.put("cpn", record.getHostingNameStr());
            procMap.put("count", Integer.toString(record.getProcStartCount()));
            processStartList.add(procMap);
        }
        return processStartList;
    }

    public void clearProcessStartList() {
        this.mProcessStartList.clear();
    }

    public void dumpProcessStartInfo() {
        for (ColorAppStartupMonitorRecord record : this.mProcessStartList) {
            Log.v(TAG, record.formatProcessStartToString());
        }
    }

    public List<Map<String, String>> getBlackListInterceptMap() {
        List<Map<String, String>> interceptInfoMap = new ArrayList<>();
        for (ColorAppStartupMonitorRecord record : this.mBlackListInterceptList) {
            Map<String, String> procMap = new HashMap<>();
            procMap.put("callerPkg", record.getCalledPkgName());
            procMap.put("cpn", record.getCallCpnName());
            procMap.put("start_mode", record.getStartMode());
            procMap.put("count", Integer.toString(record.getInterceptBlackListCount()));
            interceptInfoMap.add(procMap);
        }
        return interceptInfoMap;
    }

    public void dumpBlackListIntercep() {
        for (ColorAppStartupMonitorRecord record : this.mBlackListInterceptList) {
            Log.v(TAG, record.formatInterceptBlackListToString());
        }
    }

    public void clearBlackListInterceptList() {
        this.mBlackListInterceptList.clear();
    }

    public List<Map<String, String>> getGamePayMap() {
        List<Map<String, String>> gamePayMap = new ArrayList<>();
        for (ColorAppStartupMonitorRecord record : this.mGamePayList) {
            Map<String, String> procMap = new HashMap<>();
            procMap.put("callerPkg", record.getCallerPkgName());
            procMap.put("callerCpn", record.getCallerCpnName());
            procMap.put("calledPkg", record.getCalledPkgName());
            procMap.put("cpn", record.getCallCpnName());
            procMap.put("count", Integer.toString(record.getGamePayCount()));
            gamePayMap.add(procMap);
        }
        return gamePayMap;
    }

    public void dumpGamePay() {
        for (ColorAppStartupMonitorRecord record : this.mGamePayList) {
            Log.v(TAG, record.formatGamePayListToString());
        }
    }

    public void clearGamePay() {
        this.mGamePayList.clear();
    }

    /* access modifiers changed from: protected */
    public List<Map<String, String>> getAppStartMap() {
        List<Map<String, String>> appStartList = new ArrayList<>();
        for (ColorAppStartupMonitorRecord record : this.mAppStartList) {
            Map<String, String> procMap = new HashMap<>();
            procMap.put("callerPkg", getCallerPkgName());
            procMap.put("callerpid", Integer.toString(record.getCallingPid()));
            procMap.put("callerapp", record.getCallerAppStr());
            procMap.put("calledPkg", record.getCalledPkgName());
            procMap.put("cpn", record.getCallCpnName());
            procMap.put("host_type", record.getHostingType());
            procMap.put("start_mode", record.getStartMode());
            procMap.put("count", Integer.toString(record.getAppStartCount()));
            appStartList.add(procMap);
        }
        return appStartList;
    }

    /* access modifiers changed from: protected */
    public void clearAppStartList() {
        this.mAppStartList.clear();
    }

    /* access modifiers changed from: protected */
    public List<Map<String, String>> getPopupActivityMap() {
        List<Map<String, String>> popupInfoList = new ArrayList<>();
        for (ColorAppStartupMonitorRecord record : this.mPopupActivityList) {
            Map<String, String> procMap = new HashMap<>();
            procMap.put("callerPkg", getCallerPkgName());
            procMap.put("calledPkg", record.getCalledPkgName());
            procMap.put("cpn", record.getCallCpnName());
            procMap.put("topPkg", record.getTopName());
            procMap.put("screenState", record.getScreenState());
            procMap.put("popup_type", record.getPopupType());
            procMap.put("count", Integer.toString(record.getPopupCount()));
            popupInfoList.add(procMap);
        }
        return popupInfoList;
    }

    /* access modifiers changed from: protected */
    public void clearPopupActivityList() {
        this.mPopupActivityList.clear();
    }

    public List<List<String>> getTrackingMonitorList() {
        List<List<String>> trackingInfoList = new ArrayList<>();
        for (ColorAppStartupMonitorRecord record : this.mTrackingMonitorList) {
            List<String> infoList = new ArrayList<>();
            infoList.add(getCallerPkgName());
            infoList.add(record.getCallerCpnName());
            infoList.add(record.getCalledPkgName());
            infoList.add(record.getCallCpnName());
            infoList.add(record.getIntent());
            infoList.add(Integer.toString(record.mCallCount));
            infoList.add(record.getBundle());
            trackingInfoList.add(infoList);
        }
        return trackingInfoList;
    }

    public void clearTrackingMonitorList() {
        this.mTrackingMonitorList.clear();
    }
}
