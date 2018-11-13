package com.android.server.am;

import android.util.Log;
import com.android.server.oppo.IElsaManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class OppoAppMonitorInfo {
    public static final String TAG = null;
    List<OppoAppCalledRecord> mAppStartList;
    List<OppoAppCalledRecord> mBlackListInterceptList;
    long mCallCount;
    List<OppoAppCalledRecord> mCallList;
    String mCalledPkgName;
    boolean mCallerIsThird;
    String mCallerPkgName;
    List<OppoAppCalledRecord> mGamePayList;
    List<OppoAppCalledRecord> mPopupActivityList;
    String mProcessName;
    List<OppoAppCalledRecord> mProcessStartList;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppMonitorInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppMonitorInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAppMonitorInfo.<clinit>():void");
    }

    public OppoAppMonitorInfo() {
        this.mCallerIsThird = false;
        this.mCallCount = 0;
        this.mCallerPkgName = IElsaManager.EMPTY_PACKAGE;
        this.mProcessName = IElsaManager.EMPTY_PACKAGE;
        this.mCalledPkgName = IElsaManager.EMPTY_PACKAGE;
        this.mCallList = new ArrayList();
        this.mProcessStartList = new ArrayList();
        this.mBlackListInterceptList = new ArrayList();
        this.mGamePayList = new ArrayList();
        this.mAppStartList = new ArrayList();
        this.mPopupActivityList = new ArrayList();
    }

    static OppoAppMonitorInfo builder(String callerPkgName, String calledPkgName, String callCpnName, String callType) {
        OppoAppMonitorInfo appMonitorInfo = new OppoAppMonitorInfo();
        appMonitorInfo.setCallerPkgName(callerPkgName);
        appMonitorInfo.increaseCallCount(calledPkgName, callCpnName, callType);
        return appMonitorInfo;
    }

    static OppoAppMonitorInfo builderPorcessInfo(String packageName, String processName, String hostingType, String startMode, String hostingNameStr) {
        OppoAppMonitorInfo appStartInfo = new OppoAppMonitorInfo();
        appStartInfo.setProcessName(processName);
        appStartInfo.increaseProcessStartCount(packageName, processName, hostingType, startMode, hostingNameStr);
        return appStartInfo;
    }

    static OppoAppMonitorInfo builder(String calleePkg, String calleeCpn, String startMode) {
        OppoAppMonitorInfo appStartInfo = new OppoAppMonitorInfo();
        appStartInfo.setCalledPkgName(calleePkg);
        appStartInfo.increaseBlackListInterceptCount(calleePkg, calleeCpn, startMode);
        return appStartInfo;
    }

    static OppoAppMonitorInfo builderGamePay(String callerPkg, String calledPkg, String calledCpn) {
        OppoAppMonitorInfo appStartInfo = new OppoAppMonitorInfo();
        appStartInfo.setCallerPkgName(callerPkg);
        appStartInfo.increaseGamePayCount(callerPkg, calledPkg, calledCpn);
        return appStartInfo;
    }

    static OppoAppMonitorInfo buildAppStart(String callerPkg, int callingPid, String callerAppStr, String calledPkg, String cpnName, String hostingType, String startMode) {
        OppoAppMonitorInfo appStartInfo = new OppoAppMonitorInfo();
        appStartInfo.setCallerPkgName(callerPkg);
        appStartInfo.increaseAppStartCount(callingPid, callerAppStr, calledPkg, cpnName, hostingType, startMode);
        return appStartInfo;
    }

    static OppoAppMonitorInfo buildPopupActivity(String callerPkg, String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        OppoAppMonitorInfo appStartInfo = new OppoAppMonitorInfo();
        appStartInfo.setCallerPkgName(callerPkg);
        appStartInfo.increasePopupActivityCount(calledPkg, calledCpnName, topPkg, screenState, type);
        return appStartInfo;
    }

    public String getCallerPkgName() {
        return this.mCallerPkgName;
    }

    public void setCallerPkgName(String callerPkgName) {
        this.mCallerPkgName = callerPkgName;
    }

    public boolean getCallerIsThird() {
        return this.mCallerIsThird;
    }

    public void setCallerIsThird(boolean isThird) {
        this.mCallerIsThird = isThird;
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

    public String getProcesName() {
        return this.mProcessName;
    }

    public void increaseCallCount(String calledPkgName, String callCpnName, String callType) {
        this.mCallCount++;
        OppoAppCalledRecord record = getCallRecordInList(calledPkgName, callCpnName, callType);
        if (record == null) {
            this.mCallList.add(OppoAppCalledRecord.builder(this.mCallerPkgName, calledPkgName, callCpnName, callType));
            return;
        }
        record.increaseCallCount();
    }

    public OppoAppCalledRecord getCallRecordInList(String calledPkgName, String callCpnName, String callType) {
        for (OppoAppCalledRecord record : this.mCallList) {
            if (record.getCalledPkgName().equals(calledPkgName) && record.getCallCpnName().equals(callCpnName) && record.getCallType().equals(callType)) {
                return record;
            }
        }
        return null;
    }

    protected void increaseProcessStartCount(String packageName, String processName, String hostingType, String startMode, String hostingNameStr) {
        OppoAppCalledRecord record = getProcessStartInList(processName, hostingType, startMode);
        if (record == null) {
            this.mProcessStartList.add(OppoAppCalledRecord.builderProcessStart(packageName, processName, hostingType, startMode, hostingNameStr));
            return;
        }
        record.increaseStartCount();
    }

    private OppoAppCalledRecord getProcessStartInList(String processName, String hostingType, String startMode) {
        for (OppoAppCalledRecord record : this.mProcessStartList) {
            if (record.getProcessName().equals(processName) && record.getHostingType().equals(hostingType) && record.getStartMode().equals(startMode)) {
                return record;
            }
        }
        return null;
    }

    protected void increaseBlackListInterceptCount(String calledPkg, String calledCpn, String startMode) {
        OppoAppCalledRecord record = getBlackListInterceptInList(calledPkg, calledCpn, startMode);
        if (record == null) {
            this.mBlackListInterceptList.add(OppoAppCalledRecord.builderBlackListIntercept(calledPkg, calledCpn, startMode));
            return;
        }
        record.increaseInterceBlackListCount();
    }

    private OppoAppCalledRecord getBlackListInterceptInList(String calledPkg, String calledCpn, String startMode) {
        for (OppoAppCalledRecord record : this.mBlackListInterceptList) {
            if (record.getStartMode().equals(startMode)) {
                return record;
            }
        }
        return null;
    }

    protected void increaseGamePayCount(String callerPkg, String calledPkg, String calledCpn) {
        OppoAppCalledRecord record = getGamePayList(calledPkg, calledCpn);
        if (record == null) {
            this.mGamePayList.add(OppoAppCalledRecord.builderGamePay(callerPkg, calledPkg, calledCpn));
            return;
        }
        record.increaseGamePayCount();
    }

    private OppoAppCalledRecord getGamePayList(String calledPkg, String calledCpn) {
        for (OppoAppCalledRecord record : this.mGamePayList) {
            if (record.getCalledPkgName().equals(calledPkg) && record.getCallCpnName().equals(calledCpn)) {
                return record;
            }
        }
        return null;
    }

    protected void increaseAppStartCount(int callingPid, String callerAppStr, String calledPkg, String cpnName, String hostingType, String startMode) {
        OppoAppCalledRecord record = getAppStartInList(calledPkg, cpnName, hostingType);
        if (record == null) {
            this.mAppStartList.add(OppoAppCalledRecord.buildAppStart(callingPid, callerAppStr, calledPkg, cpnName, hostingType, startMode));
            return;
        }
        record.increaseAppStartCount();
    }

    private OppoAppCalledRecord getAppStartInList(String calledPkg, String cpnName, String hostingType) {
        for (OppoAppCalledRecord record : this.mAppStartList) {
            if (record.getCalledPkgName().equals(calledPkg) && record.getCallCpnName().equals(cpnName) && record.getHostingType().equals(hostingType)) {
                return record;
            }
        }
        return null;
    }

    protected void increasePopupActivityCount(String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        OppoAppCalledRecord record = getPopupActivityInList(calledPkg, calledCpnName, topPkg);
        if (record == null) {
            this.mPopupActivityList.add(OppoAppCalledRecord.buildPopupActivityStart(calledPkg, calledCpnName, topPkg, screenState, type));
            return;
        }
        record.increasePopupCount();
    }

    private OppoAppCalledRecord getPopupActivityInList(String calledPkg, String calledCpnName, String topPkg) {
        for (OppoAppCalledRecord record : this.mPopupActivityList) {
            if (record.getCalledPkgName().equals(calledPkg) && record.getCallCpnName().equals(calledCpnName) && record.getTopName().equals(topPkg)) {
                return record;
            }
        }
        return null;
    }

    public List<String> formatCallInfo() {
        List<String> callList = new ArrayList();
        for (OppoAppCalledRecord record : this.mCallList) {
            callList.add(record.formatToString());
        }
        return callList;
    }

    public void clearCallList() {
        this.mCallList.clear();
    }

    public void dumpInfo() {
        for (OppoAppCalledRecord record : this.mCallList) {
            Log.v(TAG, record.toString());
        }
    }

    public List<Map<String, String>> getProcessStartMap() {
        List<Map<String, String>> processStartList = new ArrayList();
        for (OppoAppCalledRecord record : this.mProcessStartList) {
            Map<String, String> procMap = new HashMap();
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
        for (OppoAppCalledRecord record : this.mProcessStartList) {
            Log.v(TAG, record.formatProcessStartToString());
        }
    }

    public List<Map<String, String>> getBlackListInterceptMap() {
        List<Map<String, String>> interceptInfoMap = new ArrayList();
        for (OppoAppCalledRecord record : this.mBlackListInterceptList) {
            Map<String, String> procMap = new HashMap();
            procMap.put("callerPkg", record.getCalledPkgName());
            procMap.put("cpn", record.getCallCpnName());
            procMap.put("start_mode", record.getStartMode());
            procMap.put("count", Integer.toString(record.getInterceptBlackListCount()));
            interceptInfoMap.add(procMap);
        }
        return interceptInfoMap;
    }

    public void dumpBlackListIntercep() {
        for (OppoAppCalledRecord record : this.mBlackListInterceptList) {
            Log.v(TAG, record.formatInterceptBlackListToString());
        }
    }

    public void clearBlackListInterceptList() {
        this.mBlackListInterceptList.clear();
    }

    public List<Map<String, String>> getGamePayMap() {
        List<Map<String, String>> gamePayMap = new ArrayList();
        for (OppoAppCalledRecord record : this.mGamePayList) {
            Map<String, String> procMap = new HashMap();
            procMap.put("callerPkg", record.getCallerPkgName());
            procMap.put("calledPkg", record.getCalledPkgName());
            procMap.put("cpn", record.getCallCpnName());
            procMap.put("count", Integer.toString(record.getGamePayCount()));
            gamePayMap.add(procMap);
        }
        return gamePayMap;
    }

    public void dumpGamePay() {
        for (OppoAppCalledRecord record : this.mGamePayList) {
            Log.v(TAG, record.formatGamePayListToString());
        }
    }

    public void clearGamePay() {
        this.mGamePayList.clear();
    }

    protected List<Map<String, String>> getAppStartMap() {
        List<Map<String, String>> appStartList = new ArrayList();
        for (OppoAppCalledRecord record : this.mAppStartList) {
            Map<String, String> procMap = new HashMap();
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

    protected void clearAppStartList() {
        this.mAppStartList.clear();
    }

    protected List<Map<String, String>> getPopupActivityMap() {
        List<Map<String, String>> popupInfoList = new ArrayList();
        for (OppoAppCalledRecord record : this.mPopupActivityList) {
            Map<String, String> procMap = new HashMap();
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

    protected void clearPopupActivityList() {
        this.mPopupActivityList.clear();
    }
}
