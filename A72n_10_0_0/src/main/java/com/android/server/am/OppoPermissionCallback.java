package com.android.server.am;

import android.annotation.TargetApi;
import android.app.AppGlobals;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.OppoPermissionManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.display.ai.utils.ColorAILog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OppoPermissionCallback {
    private static final String CTA_FEATURE = "oppo.cta.support";
    private static final String CTA_TAG = "ctaifs";
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String DELETE_CALENDAR_PERMISSION = "android.permission.WRITE_CALENDAR_DELETE";
    private static final String DELETE_CALL_PERMISSION = "android.permission.WRITE_CALL_LOG_DELETE";
    private static final String DELETE_CONTACTS_PERMISSION = "android.permission.WRITE_CONTACTS_DELETE";
    private static final String DELETE_MMS_PERMISSION = "android.permission.WRITE_MMS_DELETE";
    private static final String DELETE_SMS_PERMISSION = "android.permission.WRITE_SMS_DELETE";
    private static final int MAX_CALLBACK_COUNT = 10;
    private static final long MAX_PERMISSION_MAP_COUNT = 10;
    private static final long MIN_PERMISSION_VERIFY_TIME = 500;
    private static final List<String> OPPO_INTERCEPT_URI_PERMISSIONS = Arrays.asList(OppoPermissionConstants.PERMISSION_READ_CALL_LOG, OppoPermissionConstants.PERMISSION_READ_SMS, OppoPermissionConstants.PERMISSION_READ_CONTACTS, OppoPermissionConstants.PERMISSION_READ_CALENDAR, OppoPermissionConstants.PERMISSION_WRITE_CALL_LOG, OppoPermissionConstants.PERMISSION_WRITE_SMS, OppoPermissionConstants.PERMISSION_WRITE_CONTACTS, OppoPermissionConstants.PERMISSION_WRITE_CALENDAR, OppoPermissionConstants.PERMISSION_ADD_VOICEMAIL);
    private static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    private static final String READ_MMS_PERMISSION = "android.permission.READ_MMS";
    private static final String SEND_MMS_PERMISSION = "android.permission.SEND_MMS";
    private static final String SMS_SEPARATOR = "#";
    private static final String TAG = "OppoPermissionCallback";
    private static final long WAIT_TIME_LONG = 50000;
    private static final long WAIT_TIME_SHORT = 20000;
    private static final String WRITE_MMS_PERMISSION = "android.permission.WRITE_MMS";
    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    private static boolean isCTAVersion = DEBUG;
    private static boolean isFirstCheckCTA = true;
    private static Map<Integer, OppoPermissionCallback> sCallbackMap = Collections.synchronizedMap(new HashMap());
    private static Map<String, Long> sFrequentPermissionCheckMap = new ConcurrentHashMap();
    private static ArrayList<CTAMessage> sOppoCtaList = new ArrayList<>();
    public String permission;
    public int pid;
    public int res;

    static {
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_WIFI, "", "", "", "changenetworkstate", "", "change_network_state", "开启移动通信网络连接", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_CAMERA, "", "", "", "camera", "", "openCamera", "拍照/摄像", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_READ_SMS, "", "", "", "readsms", "", "getMessageBody", "读取短信数据", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_WRITE_SMS, "", "", "", "writesms", "", "write_sms", "修改短信数据", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_SEND_SMS, "", "", "", "sendsms", "", "sendTextMessageInternal", "发送短信", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_RECEIVE_SMS, "", "", "", "receivesms", "", "receive_sms", "接收短信", ""));
        sOppoCtaList.add(new CTAMessage("android.permission.WRITE_SMS_DELETE", "", "", "", "deletesms", "", "delete_sms", "删除短信数据", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_READ_CONTACTS, "", "", "", "readcontact", "", "read_contact", "读取电话本数据", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_ACCESS, "", "", "", "readlocation", "", "read_location", "读取定位信息", ""));
        sOppoCtaList.add(new CTAMessage("android.permission.ACCESS_COARSE_LOCATION", "", "", "", "readlocation", "", "read_location", "读取定位信息", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_READ_CALL_LOG, "", "", "", "readcalllog", "", "read_call_log", "读取通话记录", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_READ_HISTORY_BOOKMARKS, "", "", "", "readhistory", "", "read_history", "读取上网记录", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_READ_CALENDAR, "", "", "", "readcalendar", "", "read_calendar", "读取日程表数据", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_WRITE_CALENDAR, "", "", "", "writecalendar", "", "write_calendar", "修改日程表数据", ""));
        sOppoCtaList.add(new CTAMessage("android.permission.WRITE_CALENDAR_DELETE", "", "", "", "deletecalendar", "", "delete_calendar", "删除日程表数据", ""));
        sOppoCtaList.add(new CTAMessage("android.permission.WRITE_CONTACTS_DELETE", "", "", "", "deletecontacts", "", "delete_contacts", "删除电话本数据", ""));
        sOppoCtaList.add(new CTAMessage("android.permission.WRITE_CALL_LOG_DELETE", "", "", "", "deletecall", "", "delete_call", "删除通话记录", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_WRITE_CONTACTS, "", "", "", "writecontacts", "", "write_contacts", "修改用户电话本数据", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_WRITE_CALL_LOG, "", "", "", "write_calllog", "", "write_call_log", "修改通话记录", ""));
        sOppoCtaList.add(new CTAMessage(OppoPermissionConstants.PERMISSION_CALL_PHONE, "", "", "", "callphone", "", "call_phone", "拨打电话", ""));
        sOppoCtaList.add(new CTAMessage("android.permission.WRITE_MMS_DELETE", "", "", "", "deletemms", "", "delete_mms", "删除彩信数据", ""));
        sOppoCtaList.add(new CTAMessage("android.permission.WRITE_MMS", "", "", "", "writemms", "", "write_mms", "修改彩信数据", ""));
        sOppoCtaList.add(new CTAMessage("android.permission.SEND_MMS", "", "", "", "sendmms", "", "send_mms", "发送彩信", ""));
        sOppoCtaList.add(new CTAMessage("android.permission.READ_MMS", "", "", "", "readmms", "", "read_mms", "读取彩信数据", ""));
    }

    /* access modifiers changed from: private */
    public static class CTAMessage {
        private String mChineseName;
        private String mFunctionName;
        private String mKeyword;
        private String mOperation;
        private String mPackageName;
        private String mParameter;
        private String mPermission;
        private String mProcessName;
        private String mSystemTime;

        public CTAMessage(String permission, String systemTime, String packageName, String chineseName, String keyword, String processName, String functionName, String operation, String parameter) {
            this.mPermission = permission;
            this.mSystemTime = systemTime;
            this.mPackageName = packageName;
            this.mChineseName = chineseName;
            this.mKeyword = keyword;
            this.mProcessName = processName;
            this.mFunctionName = functionName;
            this.mOperation = operation;
            this.mParameter = parameter;
        }
    }

    private static boolean isCTAPermission(String permission2, CTAMessage ctaMessage) {
        if (isFirstCheckCTA) {
            try {
                isCTAVersion = AppGlobals.getPackageManager().hasSystemFeature(CTA_FEATURE, 0);
                isFirstCheckCTA = DEBUG;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!isCTAVersion) {
            return DEBUG;
        }
        Iterator<CTAMessage> it = sOppoCtaList.iterator();
        while (it.hasNext()) {
            CTAMessage cta = it.next();
            if (cta != null && cta.mPermission != null && permission2 != null && cta.mPermission.equals(permission2)) {
                ctaMessage.mSystemTime = getTime();
                ctaMessage.mKeyword = cta.mKeyword;
                ctaMessage.mFunctionName = cta.mFunctionName;
                ctaMessage.mOperation = cta.mOperation;
                return true;
            }
        }
        return DEBUG;
    }

    private static String getTime() {
        return new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).format(new Date(System.currentTimeMillis()));
    }

    private static void printCTALog(boolean isCTAPermissions, CTAMessage ctaMessage) {
        if (isCTAPermissions && ctaMessage != null) {
            Log.d(CTA_TAG, ctaMessage.mSystemTime + " <" + ctaMessage.mChineseName + ">[" + ctaMessage.mKeyword + "][" + ctaMessage.mProcessName + "]:[" + ctaMessage.mFunctionName + "]" + ctaMessage.mOperation + ".." + ctaMessage.mParameter);
        }
    }

    private static void getChineseName(ActivityManagerService activityManagerService, ProcessRecord processRecord, CTAMessage ctaMessage) {
        try {
            PackageManager mPackageManager = activityManagerService.mContext.getPackageManager();
            if (processRecord != null && processRecord.info != null) {
                ApplicationInfo info = processRecord.info;
                ctaMessage.mProcessName = info.processName;
                ctaMessage.mChineseName = info.loadLabel(mPackageManager).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int notifyApplication(String permission2, int pid2, int allowed, int token) {
        OppoPermissionCallback callback;
        if (DEBUG) {
            Log.d(TAG, "context notifyApplication, permission=" + permission2 + ", pid=" + pid2 + ", allowed=" + allowed + ", token=" + token);
        }
        synchronized (sCallbackMap) {
            callback = sCallbackMap.get(Integer.valueOf(token));
        }
        if (callback == null) {
            return 1;
        }
        synchronized (callback) {
            callback.res = allowed;
            callback.notifyAll();
        }
        return 1;
    }

    public String getPermission() {
        return this.permission;
    }

    public int getPid() {
        return this.pid;
    }

    public static ProcessRecord getProcessForPid(ActivityManagerService mService, int pid2) {
        synchronized (mService.mPidsSelfLocked) {
            ProcessRecord rec = mService.mPidsSelfLocked.get(pid2);
            if (rec != null) {
                return rec;
            }
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:199:0x049f, code lost:
        if (com.android.server.am.OppoPermissionCallback.DEBUG == false) goto L_0x04e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:200:0x04a1, code lost:
        android.util.Log.d(com.android.server.am.OppoPermissionCallback.TAG, "checkOppoPermission, finally callback size=" + com.android.server.am.OppoPermissionCallback.sCallbackMap.size() + ", permission=" + r0.getPermission() + ", token=" + r0.hashCode() + ", Thread=" + java.lang.Thread.currentThread().getId());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x04e5, code lost:
        r2 = com.android.server.am.OppoPermissionCallback.sCallbackMap;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:0x04e7, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:?, code lost:
        com.android.server.am.OppoPermissionCallback.sCallbackMap.remove(java.lang.Integer.valueOf(r0.hashCode()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x04f5, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x04f6, code lost:
        r0 = r25;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x053f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:0x0541, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x0561  */
    /* JADX WARNING: Removed duplicated region for block: B:245:0x05a8 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:256:0x05c3  */
    /* JADX WARNING: Removed duplicated region for block: B:259:0x060a A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:266:0x061c  */
    /* JADX WARNING: Removed duplicated region for block: B:269:0x062a  */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x0645  */
    /* JADX WARNING: Removed duplicated region for block: B:301:0x06e7  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0195  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0199  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x01b2  */
    public static int checkOppoPermission(String permission2, int pid2, int uid, ActivityManagerService mSelf) {
        String smsContent;
        boolean hasMMSAddress;
        String callNumber;
        boolean isExpVersion;
        IPackageManager pm;
        String tmpPermission;
        IPackageManager pm2;
        CTAMessage ctaMessage;
        boolean ctaResult;
        int result;
        int result2;
        Throwable th;
        Exception e;
        boolean ctaResult2;
        Throwable th2;
        StringBuilder sb;
        Exception e2;
        IPackageManager pm3;
        Exception e3;
        int separator;
        int separator2;
        String permission3 = permission2;
        ProcessRecord pr = getProcessForPid(mSelf, pid2);
        String tmpPermission2 = permission2;
        String callNumber2 = "";
        String smsContent2 = "";
        String mmsContent = "";
        boolean hasMMSAddress2 = DEBUG;
        if (permission3.startsWith(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
            if (!permission3.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && (separator2 = (smsContent2 = permission3.substring(OppoPermissionConstants.PERMISSION_SEND_SMS.length(), permission2.length())).lastIndexOf(SMS_SEPARATOR)) != -1) {
                callNumber2 = smsContent2.substring(separator2 + 1);
                smsContent2 = smsContent2.substring(0, separator2);
            }
            permission3 = OppoPermissionConstants.PERMISSION_SEND_SMS;
            smsContent = smsContent2;
            hasMMSAddress = false;
            callNumber = callNumber2;
        } else if (permission3.startsWith(OppoPermissionConstants.PERMISSION_CALL_PHONE)) {
            String callNumber3 = permission3.substring(OppoPermissionConstants.PERMISSION_CALL_PHONE.length(), permission2.length());
            if (DEBUG) {
                Log.d(TAG, "checkOppoPermission=" + permission3 + ", pid=" + pid2 + ",callNumber:" + callNumber3);
            }
            if (!permission3.equals(OppoPermissionConstants.PERMISSION_CALL_PHONE) && !OppoMmiCode.isServiceCodeCallForwarding(mSelf.mContext, callNumber3)) {
                callNumber3 = Uri.parse(callNumber3).getSchemeSpecificPart();
            }
            if (OppoMmiCode.isServiceCodeCallForwarding(mSelf.mContext, callNumber3)) {
                permission3 = OppoPermissionConstants.PERMISSION_CALL_FORWARDING;
                smsContent = smsContent2;
                hasMMSAddress = false;
                callNumber = callNumber3;
            } else {
                permission3 = OppoPermissionConstants.PERMISSION_CALL_PHONE;
                smsContent = smsContent2;
                hasMMSAddress = false;
                callNumber = callNumber3;
            }
        } else if (permission3.startsWith(PERMISSION_ACCESS_MEDIA_PROVIDER)) {
            permission3 = PERMISSION_ACCESS_MEDIA_PROVIDER;
            smsContent = smsContent2;
            hasMMSAddress = false;
            callNumber = callNumber2;
        } else if (permission3.startsWith(OppoPermissionConstants.PERMISSION_CAMERA)) {
            permission3 = OppoPermissionConstants.PERMISSION_CAMERA;
            smsContent = smsContent2;
            hasMMSAddress = false;
            callNumber = callNumber2;
        } else if (permission3.startsWith("android.permission.SEND_MMS")) {
            if (!permission3.equals("android.permission.SEND_MMS") && (separator = (mmsContent = permission3.substring("android.permission.SEND_MMS".length(), permission2.length())).lastIndexOf(SMS_SEPARATOR)) != -1) {
                hasMMSAddress2 = true;
                callNumber2 = mmsContent.substring(separator + 1);
                mmsContent = mmsContent.substring(0, separator);
            }
            permission3 = "android.permission.SEND_MMS";
            smsContent = smsContent2;
            hasMMSAddress = hasMMSAddress2;
            callNumber = callNumber2;
        } else {
            smsContent = smsContent2;
            hasMMSAddress = false;
            callNumber = callNumber2;
        }
        CTAMessage ctaMessage2 = new CTAMessage("", "", "", "", "", "", "", "", "");
        boolean ctaResult3 = isCTAPermission(permission3, ctaMessage2);
        String permission4 = permission3;
        for (String writePermission : OppoPermissionManager.WRITE_PERMISSIONS) {
            if (permission4.startsWith(writePermission)) {
                permission4 = writePermission;
                tmpPermission2 = writePermission;
            }
        }
        try {
            pm3 = AppGlobals.getPackageManager();
            try {
                isExpVersion = pm3.hasSystemFeature("oppo.version.exp", 0);
                pm = pm3;
            } catch (Exception e4) {
                e3 = e4;
                e3.printStackTrace();
                isExpVersion = false;
                pm = pm3;
                if (!tmpPermission2.equals("android.permission.CAMERA_MEDIA_RECORD")) {
                }
                if (ctaResult3) {
                }
                if (!OppoPermissionManager.sInterceptingPermissions.contains(permission4)) {
                }
            }
        } catch (Exception e5) {
            e3 = e5;
            pm3 = null;
            e3.printStackTrace();
            isExpVersion = false;
            pm = pm3;
            if (!tmpPermission2.equals("android.permission.CAMERA_MEDIA_RECORD")) {
            }
            if (ctaResult3) {
            }
            if (!OppoPermissionManager.sInterceptingPermissions.contains(permission4)) {
            }
        }
        if (!tmpPermission2.equals("android.permission.CAMERA_MEDIA_RECORD")) {
            tmpPermission = tmpPermission2;
        } else if (isExpVersion) {
            return 0;
        } else {
            tmpPermission = OppoPermissionConstants.PERMISSION_CAMERA;
        }
        if (ctaResult3) {
            getChineseName(mSelf, pr, ctaMessage2);
            ctaMessage2.mParameter = callNumber;
            if (!hasMMSAddress && permission4.equals("android.permission.SEND_MMS")) {
                ctaResult3 = DEBUG;
            }
        }
        if (!OppoPermissionManager.sInterceptingPermissions.contains(permission4)) {
            printCTALog(ctaResult3, ctaMessage2);
            return mSelf.checkPermission(permission4, pid2, uid);
        } else if (!SystemProperties.getBoolean("persist.sys.permission.enable", (boolean) DEBUG) || pr == null || pr.info == null || (pr.info.flags & 1) != 0) {
            if (OppoPermissionManager.OPPO_DEFINED_PERMISSIONS.contains(permission4)) {
                printCTALog(ctaResult3, ctaMessage2);
                return 0;
            } else if (OPPO_INTERCEPT_URI_PERMISSIONS.contains(permission4)) {
                printCTALog(ctaResult3, ctaMessage2);
                return 0;
            } else if (OppoPermissionConstants.PERMISSION_ACCESS.equals(permission4)) {
                int result3 = mSelf.checkPermission(permission4, pid2, uid);
                if (result3 == -1) {
                    result3 = mSelf.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid2, uid);
                }
                printCTALog(ctaResult3, ctaMessage2);
                return result3;
            } else {
                printCTALog(ctaResult3, ctaMessage2);
                return mSelf.checkPermission(permission4, pid2, uid);
            }
        } else if (isSinglePermissionTargetSdkM(pm, permission4, pr.info.packageName, uid)) {
            printCTALog(ctaResult3, ctaMessage2);
            int checkResult = mSelf.checkPermission(permission4, pid2, uid);
            OppoPermissionInterceptPolicy.getInstance(mSelf).notifyPermissionRecord(pr.info.packageName, permission4, System.currentTimeMillis(), checkResult);
            return checkResult;
        } else {
            int result4 = 3;
            boolean needCheckFrequent = needCheckFrequentPermission(permission4, pr.info.packageName);
            if (needCheckFrequent) {
                try {
                    Long lastVerifyTime = sFrequentPermissionCheckMap.get(permission4 + uid);
                    if (lastVerifyTime != null) {
                        long difftime = SystemClock.elapsedRealtime() - lastVerifyTime.longValue();
                        if (difftime <= 0 || difftime >= MIN_PERMISSION_VERIFY_TIME) {
                            pm2 = pm;
                        } else {
                            try {
                                if (DEBUG) {
                                    StringBuilder sb2 = new StringBuilder();
                                    pm2 = pm;
                                    try {
                                        sb2.append("checkOppoPermission is frequent:");
                                        sb2.append(difftime);
                                        sb2.append(",permission:");
                                        sb2.append(permission4);
                                        sb2.append(",uid:");
                                        sb2.append(uid);
                                        Log.d(TAG, sb2.toString());
                                    } catch (Exception e6) {
                                        e2 = e6;
                                        e2.printStackTrace();
                                        if (sCallbackMap.size() > 10) {
                                        }
                                        if (-1 == result) {
                                        }
                                        printCTALog(ctaResult, ctaMessage);
                                        return result2;
                                    }
                                }
                                if (OppoPermissionInterceptPolicy.sAllowPermissionRecord) {
                                    OppoPermissionInterceptPolicy.getInstance(mSelf).notifyPermissionRecord(pr.info.packageName, permission4, System.currentTimeMillis(), 0);
                                }
                                printCTALog(ctaResult3, ctaMessage2);
                                return 0;
                            } catch (Exception e7) {
                                e2 = e7;
                                pm2 = pm;
                                e2.printStackTrace();
                                if (sCallbackMap.size() > 10) {
                                }
                                if (-1 == result) {
                                }
                                printCTALog(ctaResult, ctaMessage);
                                return result2;
                            }
                        }
                    } else {
                        pm2 = pm;
                    }
                    if (((long) sFrequentPermissionCheckMap.size()) > MAX_PERMISSION_MAP_COUNT) {
                        sFrequentPermissionCheckMap.clear();
                    }
                } catch (Exception e8) {
                    e2 = e8;
                    pm2 = pm;
                    e2.printStackTrace();
                    if (sCallbackMap.size() > 10) {
                    }
                    if (-1 == result) {
                    }
                    printCTALog(ctaResult, ctaMessage);
                    return result2;
                }
            } else {
                pm2 = pm;
            }
            if (sCallbackMap.size() > 10) {
                OppoPermissionCallback callback = new OppoPermissionCallback();
                try {
                    callback.permission = permission4;
                    callback.pid = pid2;
                    callback.res = 3;
                    synchronized (sCallbackMap) {
                        sCallbackMap.put(Integer.valueOf(callback.hashCode()), callback);
                    }
                    if (DEBUG) {
                        try {
                            sb = new StringBuilder();
                            sb.append("checkOppoPermission, permission=");
                            sb.append(permission4);
                            sb.append(", pid=");
                            sb.append(pid2);
                            sb.append(", uid=");
                            sb.append(uid);
                            sb.append(", token=");
                            sb.append(callback.hashCode());
                            sb.append(", Thread=");
                            ctaResult2 = ctaResult3;
                        } catch (Exception e9) {
                            e = e9;
                            ctaMessage = ctaMessage2;
                            ctaResult = ctaResult3;
                            try {
                                e.printStackTrace();
                                if (DEBUG) {
                                }
                                synchronized (sCallbackMap) {
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                if (DEBUG) {
                                }
                                synchronized (sCallbackMap) {
                                }
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            if (DEBUG) {
                            }
                            synchronized (sCallbackMap) {
                            }
                        }
                        try {
                            sb.append(Thread.currentThread().getId());
                            Log.d(TAG, sb.toString());
                        } catch (Exception e10) {
                            e = e10;
                            ctaMessage = ctaMessage2;
                            ctaResult = ctaResult2;
                        } catch (Throwable th5) {
                            th = th5;
                            if (DEBUG) {
                                Log.d(TAG, "checkOppoPermission, finally callback size=" + sCallbackMap.size() + ", permission=" + callback.getPermission() + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId());
                            }
                            synchronized (sCallbackMap) {
                                sCallbackMap.remove(Integer.valueOf(callback.hashCode()));
                            }
                            throw th;
                        }
                    } else {
                        ctaResult2 = ctaResult3;
                    }
                    try {
                        synchronized (callback) {
                            try {
                                ctaResult = ctaResult2;
                                ctaMessage = ctaMessage2;
                                try {
                                    int res2 = OppoPermissionInterceptPolicy.getInstance(mSelf).checkPermissionForProc(tmpPermission, pid2, uid, callback.hashCode(), callback);
                                    try {
                                        if (DEBUG) {
                                            try {
                                                Log.d(TAG, "checkOppoPermission, checkPermissionForProc return res=" + res2 + ", permission=" + callback.getPermission() + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId() + " , m size " + sCallbackMap.size());
                                            } catch (Throwable th6) {
                                                th2 = th6;
                                                permission4 = permission4;
                                            }
                                        }
                                        if (res2 == 1) {
                                            result4 = -1;
                                            permission4 = permission4;
                                        } else if (res2 == 0) {
                                            if (needCheckFrequent) {
                                                try {
                                                    Map<String, Long> map = sFrequentPermissionCheckMap;
                                                    StringBuilder sb3 = new StringBuilder();
                                                    permission4 = permission4;
                                                    try {
                                                        sb3.append(permission4);
                                                        sb3.append(uid);
                                                        map.put(sb3.toString(), Long.valueOf(SystemClock.elapsedRealtime()));
                                                    } catch (Throwable th7) {
                                                        th2 = th7;
                                                        result4 = 0;
                                                    }
                                                } catch (Throwable th8) {
                                                    th2 = th8;
                                                    permission4 = permission4;
                                                    result4 = 0;
                                                    while (true) {
                                                        try {
                                                            break;
                                                        } catch (Throwable th9) {
                                                            th2 = th9;
                                                        }
                                                    }
                                                    throw th2;
                                                }
                                            } else {
                                                permission4 = permission4;
                                            }
                                            result4 = 0;
                                        } else {
                                            permission4 = permission4;
                                            if (res2 == 2) {
                                                try {
                                                    if (permission4.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                                                        callback.wait(WAIT_TIME_LONG);
                                                    } else if (permission4.equals(OppoPermissionConstants.PERMISSION_CAMERA)) {
                                                        callback.wait(WAIT_TIME_LONG);
                                                    } else {
                                                        callback.wait(WAIT_TIME_SHORT);
                                                    }
                                                    if (DEBUG) {
                                                        Log.d(TAG, "checkOppoPermission, notify continue callback.res=" + callback.res + ", permission=" + callback.getPermission() + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId());
                                                    }
                                                    if (callback.res != 1) {
                                                        if (callback.res != 3 || !permission4.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                                                            if (callback.res == 0) {
                                                                result4 = 0;
                                                            }
                                                        }
                                                    }
                                                    result4 = -1;
                                                } catch (Throwable th10) {
                                                    th2 = th10;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th2;
                                                }
                                            }
                                        }
                                    } catch (Throwable th11) {
                                        th2 = th11;
                                        permission4 = permission4;
                                        while (true) {
                                            break;
                                        }
                                        throw th2;
                                    }
                                } catch (Throwable th12) {
                                    th2 = th12;
                                    permission4 = permission4;
                                    while (true) {
                                        break;
                                    }
                                    throw th2;
                                }
                            } catch (Throwable th13) {
                                th2 = th13;
                                ctaMessage = ctaMessage2;
                                ctaResult = ctaResult2;
                                while (true) {
                                    break;
                                }
                                throw th2;
                            }
                        }
                    } catch (Exception e11) {
                        e = e11;
                        ctaMessage = ctaMessage2;
                        ctaResult = ctaResult2;
                        e.printStackTrace();
                        if (DEBUG) {
                        }
                        synchronized (sCallbackMap) {
                        }
                    } catch (Throwable th14) {
                        th = th14;
                        if (DEBUG) {
                        }
                        synchronized (sCallbackMap) {
                        }
                    }
                } catch (Exception e12) {
                    e = e12;
                    ctaMessage = ctaMessage2;
                    ctaResult = ctaResult3;
                    e.printStackTrace();
                    if (DEBUG) {
                        Log.d(TAG, "checkOppoPermission, finally callback size=" + sCallbackMap.size() + ", permission=" + callback.getPermission() + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId());
                    }
                    synchronized (sCallbackMap) {
                        sCallbackMap.remove(Integer.valueOf(callback.hashCode()));
                    }
                    result = result4;
                    if (-1 == result) {
                    }
                    printCTALog(ctaResult, ctaMessage);
                    return result2;
                } catch (Throwable th15) {
                    th = th15;
                    if (DEBUG) {
                    }
                    synchronized (sCallbackMap) {
                    }
                }
            } else {
                ctaMessage = ctaMessage2;
                ctaResult = ctaResult3;
                result = 3;
            }
            if (-1 == result) {
                result2 = -1;
                if (OppoPermissionInterceptPolicy.sAllowPermissionRecord) {
                    OppoPermissionInterceptPolicy.getInstance(mSelf).notifyPermissionRecord(pr.info.packageName, permission4, System.currentTimeMillis(), -1);
                }
            } else if (result == 0) {
                result2 = 0;
                if (OppoPermissionInterceptPolicy.sAllowPermissionRecord) {
                    OppoPermissionInterceptPolicy.getInstance(mSelf).notifyPermissionRecord(pr.info.packageName, permission4, System.currentTimeMillis(), 0);
                }
            } else {
                result2 = 0;
                if (OppoPermissionInterceptPolicy.sAllowPermissionRecord) {
                    if (OppoPermissionManager.OPPO_DEFINED_PERMISSIONS.contains(permission4)) {
                        OppoPermissionInterceptPolicy.getInstance(mSelf).notifyPermissionRecord(pr.info.packageName, permission4, System.currentTimeMillis(), 0);
                    } else {
                        OppoPermissionInterceptPolicy.getInstance(mSelf).notifyPermissionRecord(pr.info.packageName, permission4, System.currentTimeMillis(), mSelf.checkPermission(permission4, pid2, uid));
                    }
                }
            }
            printCTALog(ctaResult, ctaMessage);
            return result2;
        }
        while (true) {
        }
    }

    public static boolean needCheckFrequentPermission(String permission2, String packageName) {
        if (permission2 == null || OppoPermissionInterceptPolicy.sIsCtaVersion.booleanValue()) {
            return DEBUG;
        }
        try {
            for (String frequentPermission : OppoPermissionManager.OPPO_DETECT_FREQUENT_CHECK_PERMISSIONS) {
                if (frequentPermission.contains(SMS_SEPARATOR)) {
                    if (packageName != null) {
                        String[] splitArray = frequentPermission.split(SMS_SEPARATOR);
                        if (permission2.equals(splitArray[0]) && packageName.equals(splitArray[1])) {
                            return true;
                        }
                    }
                } else if (permission2.equals(frequentPermission)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DEBUG;
    }

    @TargetApi(4)
    private static boolean isSinglePermissionTargetSdkM(IPackageManager pm, String permission2, String pkgName, int uid) {
        if ((OppoPermissionConstants.PERMISSION_SENSORS.equals(permission2) || OppoPermissionConstants.PERMISSION_ACTIVITY_RECOGNITION.equals(permission2) || OppoPermissionConstants.PERMISSION_READ_PHONE_STATE.equals(permission2)) && pm != null) {
            try {
                if (pm.getApplicationInfo(pkgName, 0, UserHandle.getUserId(uid)).targetSdkVersion >= 23) {
                    return true;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return DEBUG;
    }
}
