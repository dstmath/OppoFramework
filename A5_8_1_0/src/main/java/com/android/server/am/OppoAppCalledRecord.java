package com.android.server.am;

public class OppoAppCalledRecord {
    private int mAppStartCount = 0;
    int mCallCount = 0;
    String mCallCpnName = "";
    String mCallType = "";
    String mCalledAppExist = "";
    String mCalledPkgName = "";
    private String mCallerAppStr = "";
    boolean mCallerIsFg = false;
    boolean mCallerIsThird = false;
    String mCallerPkgName = "";
    private int mCallingPid = 0;
    private int mGamePayCount = 0;
    private String mHostingNameStr = "";
    private String mHostingType = "";
    private int mInterceBlackListCount = 0;
    private String mPackageName = "";
    private int mPopupCount = 0;
    private String mPopupType = "";
    private String mProcessName = "";
    private int mProcessStartCount = 0;
    private String mScreenState;
    private String mStartMode = "";
    String mTopName = "";

    static OppoAppCalledRecord builder(String callerPkgName, String calledPkgName, String callCpnName, String callType, String calledAppExist) {
        OppoAppCalledRecord record = new OppoAppCalledRecord();
        record.setCallerPkgName(callerPkgName);
        record.setCalledPkgName(calledPkgName);
        record.setCallCpnName(callCpnName);
        record.setCallType(callType);
        record.setCalledAppExist(calledAppExist);
        record.increaseCallCount();
        return record;
    }

    public String getCalledPkgName() {
        return this.mCalledPkgName;
    }

    public void setCalledPkgName(String calledPkgName) {
        this.mCalledPkgName = calledPkgName;
    }

    public String getCallerPkgName() {
        return this.mCallerPkgName;
    }

    public void setCallerPkgName(String callerPkgName) {
        this.mCallerPkgName = callerPkgName;
    }

    public String getCallCpnName() {
        return this.mCallCpnName;
    }

    public void setCallCpnName(String callCpnName) {
        this.mCallCpnName = callCpnName;
    }

    public String getTopName() {
        return this.mTopName;
    }

    public void setTopName(String topName) {
        this.mTopName = topName;
    }

    public boolean getCallerIsThird() {
        return this.mCallerIsThird;
    }

    public void setCallerIsThird(boolean isThird) {
        this.mCallerIsThird = isThird;
    }

    public boolean getCallerIsFg() {
        return this.mCallerIsFg;
    }

    public void setCallerIsFg(boolean isFg) {
        this.mCallerIsFg = isFg;
    }

    public String getCallType() {
        return this.mCallType;
    }

    public void setCallType(String callType) {
        this.mCallType = callType;
    }

    public void setCalledAppExist(String calledAppExist) {
        this.mCalledAppExist = calledAppExist;
    }

    public String getCalledAppExist() {
        return this.mCalledAppExist;
    }

    public void increaseCallCount() {
        this.mCallCount++;
    }

    public void cleanup() {
    }

    public String formatToString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append(getCallerPkgName()).append(" ").append(getCalledPkgName()).append(" ").append(getCallCpnName()).append(" ").append(getCallType()).append(" ").append(getCalledAppExist()).append(" ").append(this.mCallCount);
        return sb.toString();
    }

    public String toString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append(getCallerPkgName()).append(" call ").append(getCalledPkgName()).append(" / ").append(getCallCpnName()).append(" with ").append(getCallType()).append(" ").append(" count is ").append(this.mCallCount);
        return sb.toString();
    }

    static OppoAppCalledRecord builderProcessStart(String packageName, String processName, String hostingType, String startMode, String hostingNameStr) {
        OppoAppCalledRecord record = new OppoAppCalledRecord();
        record.setPackageName(packageName);
        record.setProcessName(processName);
        record.setHostingType(hostingType);
        record.setStartMode(startMode);
        record.setHostingNameStr(hostingNameStr);
        record.increaseStartCount();
        return record;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getProcessName() {
        return this.mProcessName;
    }

    public void setProcessName(String processName) {
        this.mProcessName = processName;
    }

    public String getHostingType() {
        return this.mHostingType;
    }

    public void setHostingType(String hostingType) {
        this.mHostingType = hostingType;
    }

    public String getStartMode() {
        return this.mStartMode;
    }

    public void setStartMode(String startMode) {
        this.mStartMode = startMode;
    }

    protected String getHostingNameStr() {
        return this.mHostingNameStr;
    }

    protected void setHostingNameStr(String mHostingNameStr) {
        this.mHostingNameStr = mHostingNameStr;
    }

    public void increaseStartCount() {
        this.mProcessStartCount++;
    }

    public int getProcStartCount() {
        return this.mProcessStartCount;
    }

    public String formatProcessStartToString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append(getPackageName()).append(" ").append(getProcessName()).append(" ").append(getHostingType()).append(" ").append(getStartMode()).append(" ").append(this.mProcessStartCount);
        return sb.toString();
    }

    static OppoAppCalledRecord builderBlackListIntercept(String calledPkg, String calledCpn, String startMode) {
        OppoAppCalledRecord record = new OppoAppCalledRecord();
        record.setCalledPkgName(calledPkg);
        record.setCallCpnName(calledCpn);
        record.setStartMode(startMode);
        record.increaseInterceBlackListCount();
        return record;
    }

    public void increaseInterceBlackListCount() {
        this.mInterceBlackListCount++;
    }

    public int getInterceptBlackListCount() {
        return this.mInterceBlackListCount;
    }

    public String formatInterceptBlackListToString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append(getCalledPkgName()).append(" ").append(getCallCpnName()).append(" ").append(getStartMode()).append(" ").append(this.mInterceBlackListCount);
        return sb.toString();
    }

    static OppoAppCalledRecord builderGamePay(String callerPkg, String calledPkg, String calledCpn) {
        OppoAppCalledRecord record = new OppoAppCalledRecord();
        record.setCallerPkgName(callerPkg);
        record.setCalledPkgName(calledPkg);
        record.setCallCpnName(calledCpn);
        record.increaseGamePayCount();
        return record;
    }

    public void increaseGamePayCount() {
        this.mGamePayCount++;
    }

    public int getGamePayCount() {
        return this.mGamePayCount;
    }

    public String formatGamePayListToString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append(getCallerPkgName()).append(" ").append(getCalledPkgName()).append(" ").append(getCallCpnName()).append(" ").append(this.mGamePayCount);
        return sb.toString();
    }

    static OppoAppCalledRecord buildAppStart(int callingPid, String callerAppStr, String calledPkg, String cpnName, String hostingType, String startMode) {
        OppoAppCalledRecord record = new OppoAppCalledRecord();
        record.setCallingPid(callingPid);
        record.setCallerAppStr(callerAppStr);
        record.setCalledPkgName(calledPkg);
        record.setCallCpnName(cpnName);
        record.setHostingType(hostingType);
        record.setStartMode(startMode);
        record.increaseAppStartCount();
        return record;
    }

    protected int getCallingPid() {
        return this.mCallingPid;
    }

    protected String getCallerAppStr() {
        return this.mCallerAppStr;
    }

    protected void setCallingPid(int mCallingPid) {
        this.mCallingPid = mCallingPid;
    }

    protected void setCallerAppStr(String mCallerAppStr) {
        this.mCallerAppStr = mCallerAppStr;
    }

    protected int getAppStartCount() {
        return this.mAppStartCount;
    }

    protected void increaseAppStartCount() {
        this.mAppStartCount++;
    }

    static OppoAppCalledRecord buildPopupActivityStart(String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        OppoAppCalledRecord record = new OppoAppCalledRecord();
        record.setCalledPkgName(calledPkg);
        record.setCallCpnName(calledCpnName);
        record.setTopName(topPkg);
        record.setScreenState(screenState);
        record.setPopupType(type);
        record.increasePopupCount();
        return record;
    }

    protected String getPopupType() {
        return this.mPopupType;
    }

    protected String getScreenState() {
        return this.mScreenState;
    }

    protected void setPopupType(String mPopupType) {
        this.mPopupType = mPopupType;
    }

    protected void setScreenState(String mScreenState) {
        this.mScreenState = mScreenState;
    }

    protected void increasePopupCount() {
        this.mPopupCount++;
    }

    protected int getPopupCount() {
        return this.mPopupCount;
    }
}
