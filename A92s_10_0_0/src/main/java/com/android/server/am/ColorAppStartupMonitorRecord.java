package com.android.server.am;

public class ColorAppStartupMonitorRecord {
    private int mAppStartCount = 0;
    private String mBundle = "";
    int mCallCount = 0;
    String mCallCpnName = "";
    String mCallType = "";
    String mCalledAppExist = "";
    String mCalledPkgName = "";
    private String mCallerAppStr = "";
    String mCallerCpnName = "";
    boolean mCallerIsFg = false;
    boolean mCallerIsThird = false;
    String mCallerPkgName = "";
    private int mCallingPid = 0;
    private int mGamePayCount = 0;
    private String mHostingNameStr = "";
    private String mHostingType = "";
    private String mIntent = "";
    private int mInterceBlackListCount = 0;
    private String mPackageName = "";
    private int mPopupCount = 0;
    private String mPopupType = "";
    private String mProcessName = "";
    private int mProcessStartCount = 0;
    private String mScreenState;
    private String mStartMode = "";
    String mTopName = "";

    static ColorAppStartupMonitorRecord builder(String callerPkgName, String calledPkgName, String callCpnName, String callType, String calledAppExist) {
        ColorAppStartupMonitorRecord record = new ColorAppStartupMonitorRecord();
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

    public String getCallerCpnName() {
        return this.mCallerCpnName;
    }

    public void setCallerCpnName(String cpnName) {
        this.mCallerCpnName = cpnName;
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
        StringBuilder sb = new StringBuilder(256);
        sb.append(getCallerPkgName());
        sb.append(" ");
        sb.append(getCalledPkgName());
        sb.append(" ");
        sb.append(getCallCpnName());
        sb.append(" ");
        sb.append(getCallType());
        sb.append(" ");
        sb.append(getCalledAppExist());
        sb.append(" ");
        sb.append(this.mCallCount);
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append(getCallerPkgName());
        sb.append(" call ");
        sb.append(getCalledPkgName());
        sb.append(" / ");
        sb.append(getCallCpnName());
        sb.append(" with ");
        sb.append(getCallType());
        sb.append(" ");
        sb.append(" count is ");
        sb.append(this.mCallCount);
        return sb.toString();
    }

    static ColorAppStartupMonitorRecord builderProcessStart(String packageName, String processName, String hostingType, String startMode, String hostingNameStr) {
        ColorAppStartupMonitorRecord record = new ColorAppStartupMonitorRecord();
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

    /* access modifiers changed from: protected */
    public String getHostingNameStr() {
        return this.mHostingNameStr;
    }

    /* access modifiers changed from: protected */
    public void setHostingNameStr(String mHostingNameStr2) {
        this.mHostingNameStr = mHostingNameStr2;
    }

    public void increaseStartCount() {
        this.mProcessStartCount++;
    }

    public int getProcStartCount() {
        return this.mProcessStartCount;
    }

    public String formatProcessStartToString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append(getPackageName());
        sb.append(" ");
        sb.append(getProcessName());
        sb.append(" ");
        sb.append(getHostingType());
        sb.append(" ");
        sb.append(getStartMode());
        sb.append(" ");
        sb.append(this.mProcessStartCount);
        return sb.toString();
    }

    static ColorAppStartupMonitorRecord builderBlackListIntercept(String calledPkg, String calledCpn, String startMode) {
        ColorAppStartupMonitorRecord record = new ColorAppStartupMonitorRecord();
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
        StringBuilder sb = new StringBuilder(256);
        sb.append(getCalledPkgName());
        sb.append(" ");
        sb.append(getCallCpnName());
        sb.append(" ");
        sb.append(getStartMode());
        sb.append(" ");
        sb.append(this.mInterceBlackListCount);
        return sb.toString();
    }

    static ColorAppStartupMonitorRecord builderGamePay(String callerPkg, String callerCpn, String calledPkg, String calledCpn) {
        ColorAppStartupMonitorRecord record = new ColorAppStartupMonitorRecord();
        record.setCallerPkgName(callerPkg);
        record.setCallerCpnName(callerCpn);
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
        StringBuilder sb = new StringBuilder(256);
        sb.append(getCallerPkgName());
        sb.append(" ");
        sb.append(getCalledPkgName());
        sb.append(" ");
        sb.append(getCallCpnName());
        sb.append(" ");
        sb.append(this.mGamePayCount);
        return sb.toString();
    }

    static ColorAppStartupMonitorRecord buildAppStart(int callingPid, String callerAppStr, String calledPkg, String cpnName, String hostingType, String startMode) {
        ColorAppStartupMonitorRecord record = new ColorAppStartupMonitorRecord();
        record.setCallingPid(callingPid);
        record.setCallerAppStr(callerAppStr);
        record.setCalledPkgName(calledPkg);
        record.setCallCpnName(cpnName);
        record.setHostingType(hostingType);
        record.setStartMode(startMode);
        record.increaseAppStartCount();
        return record;
    }

    /* access modifiers changed from: protected */
    public int getCallingPid() {
        return this.mCallingPid;
    }

    /* access modifiers changed from: protected */
    public String getCallerAppStr() {
        return this.mCallerAppStr;
    }

    /* access modifiers changed from: protected */
    public void setCallingPid(int mCallingPid2) {
        this.mCallingPid = mCallingPid2;
    }

    /* access modifiers changed from: protected */
    public void setCallerAppStr(String mCallerAppStr2) {
        this.mCallerAppStr = mCallerAppStr2;
    }

    /* access modifiers changed from: protected */
    public int getAppStartCount() {
        return this.mAppStartCount;
    }

    /* access modifiers changed from: protected */
    public void increaseAppStartCount() {
        this.mAppStartCount++;
    }

    static ColorAppStartupMonitorRecord buildPopupActivityStart(String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        ColorAppStartupMonitorRecord record = new ColorAppStartupMonitorRecord();
        record.setCalledPkgName(calledPkg);
        record.setCallCpnName(calledCpnName);
        record.setTopName(topPkg);
        record.setScreenState(screenState);
        record.setPopupType(type);
        record.increasePopupCount();
        return record;
    }

    /* access modifiers changed from: protected */
    public String getPopupType() {
        return this.mPopupType;
    }

    /* access modifiers changed from: protected */
    public String getScreenState() {
        return this.mScreenState;
    }

    /* access modifiers changed from: protected */
    public void setPopupType(String mPopupType2) {
        this.mPopupType = mPopupType2;
    }

    /* access modifiers changed from: protected */
    public void setScreenState(String mScreenState2) {
        this.mScreenState = mScreenState2;
    }

    /* access modifiers changed from: protected */
    public void increasePopupCount() {
        this.mPopupCount++;
    }

    /* access modifiers changed from: protected */
    public int getPopupCount() {
        return this.mPopupCount;
    }

    static ColorAppStartupMonitorRecord buildTrackingMonitorInfo(String callerPkg, String callerCpn, String calleePkg, String calleeCpn, String intent, String bundle) {
        ColorAppStartupMonitorRecord record = new ColorAppStartupMonitorRecord();
        record.setCallerPkgName(callerPkg);
        record.setCallerCpnName(callerCpn);
        record.setCalledPkgName(calleePkg);
        record.setCallCpnName(calleeCpn);
        record.setIntent(intent);
        record.setBundle(bundle);
        record.increaseCallCount();
        return record;
    }

    private void setIntent(String intent) {
        this.mIntent = intent;
    }

    private void setBundle(String bundle) {
        this.mBundle = bundle;
    }

    /* access modifiers changed from: protected */
    public String getIntent() {
        return this.mIntent;
    }

    /* access modifiers changed from: protected */
    public String getBundle() {
        return this.mBundle;
    }
}
