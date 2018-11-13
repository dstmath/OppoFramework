package com.android.server.oppo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.FileUtils;
import android.os.OppoManager;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoProcessManager;
import com.android.server.location.OppoNetworkUtil;
import com.android.server.pm.CompatibilityHelper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.json.JSONObject;

/* compiled from: OppoGrThreadFactory */
class OppoGrThread implements Runnable {
    private static final int DEF_APK_INNER_NUM = 6;
    private static final int DO_CHECK_NET = 3;
    private static final int DO_DOWNLOAD_AND_INSTALL = 1;
    private static final int DO_INSTALL = 4;
    private static final int DO_RE_INSTALL = 2;
    private static final int GMS_TIME_DIV_OTHER = 11;
    private static final int VENDING_CODE = 80641200;
    private static final String VENDING_PKG_NAME = "com.android.vending";
    private Boolean DEBUG_GR = Boolean.valueOf(OppoManager.DEBUG_GR);
    private final String TAG = "OppoGrThread";
    private Boolean mAbandon = Boolean.valueOf(false);
    private Integer mAction;
    private String mAppName;
    private String mBasePathCode;
    private HttpURLConnection mConn;
    private Context mContext;
    private Set<String> mExceptionApks;
    private String mExceptionContent;
    private Map<String, String> mExceptionMaps;
    private String mFileName;
    private LinkedBlockingQueue<File> mFileQueue = new LinkedBlockingQueue(OppoManager.GR_APK_NUMBER.intValue());
    private FileOutputStream mFos = null;
    private String mGrAbandon;
    private List<File> mGrFileList = new ArrayList();
    private String mGrOk;
    private int mIncreIndex = 0;
    private InputStream mIs = null;
    private Boolean mIsGRIn = Boolean.valueOf(false);
    private String mLastPkgName = null;
    private Integer mMaxProgress = Integer.valueOf(100);
    private List<File> mNeedBeDeletes = new ArrayList();
    private PackageInstallNoDialogObserver mNoDialogObserver = new PackageInstallNoDialogObserver(this, null);
    private PackageInstallObserver mObserver = new PackageInstallObserver(this, null);
    private PackageManager mPackageManager;
    private int mPerMax = 0;
    private int mPerSize = 0;
    private ProgressDialog mProgressDialog;
    private File mRootDirFile;
    private ScheduledExecutorService mService = Executors.newSingleThreadScheduledExecutor();
    private String mTipContent;
    private String mTipTitle;
    private int mToInstallNum = 6;

    /* compiled from: OppoGrThreadFactory */
    private class PackageDeleteObserver extends Stub {
        /* synthetic */ PackageDeleteObserver(OppoGrThread this$0, PackageDeleteObserver packageDeleteObserver) {
            this();
        }

        private PackageDeleteObserver() {
        }

        public void packageDeleted(String packageName, int returnCode) {
            if (returnCode == 1) {
                Log.d("OppoGrThread", "Geloin: we uninstalled " + packageName);
            }
        }
    }

    /* compiled from: OppoGrThreadFactory */
    private class PackageInstallNoDialogObserver extends IPackageInstallObserver.Stub {
        /* synthetic */ PackageInstallNoDialogObserver(OppoGrThread this$0, PackageInstallNoDialogObserver packageInstallNoDialogObserver) {
            this();
        }

        private PackageInstallNoDialogObserver() {
        }

        public void packageInstalled(String packageName, int returnCode) {
            if (returnCode == 1) {
                if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                    Log.d("OppoGrThread", "changeover: we installed " + packageName);
                }
                if (!OppoGrThread.this.mFileQueue.isEmpty()) {
                    try {
                        OppoGrThread.this.installApkNoDialog((File) OppoGrThread.this.mFileQueue.take());
                    } catch (Exception e) {
                        OppoManager.canCreateDialog = Boolean.valueOf(true);
                        OppoManager.isNoDialogInstalling = Boolean.valueOf(false);
                    }
                } else if (OppoGrThread.this.mIsGRIn.booleanValue()) {
                    if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                        Log.d("OppoGrThread", "changeover: fileQueue is empty.");
                    }
                    OppoGrThread.this.deleteTmpFiles();
                    OppoManager.canCreateDialog = Boolean.valueOf(true);
                    OppoManager.isNoDialogInstalling = Boolean.valueOf(false);
                    ContentResolver resolver = OppoGrThread.this.mContext.getContentResolver();
                    Secure.putString(resolver, "changeover_status", "0");
                    String value = Secure.getString(resolver, "changeover_status");
                    if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                        Log.d("OppoGrThread", "changeover: KEY_SETTINGS_CHANGEOVER = " + value);
                    }
                }
            } else {
                if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                    Log.d("OppoGrThread", "changeover: install error returncode is " + returnCode + ", PkgName is " + packageName);
                }
                if (packageName == null || OppoManager.grList.contains(packageName)) {
                    OppoManager.canCreateDialog = Boolean.valueOf(true);
                    OppoManager.isNoDialogInstalling = Boolean.valueOf(false);
                }
            }
        }
    }

    /* compiled from: OppoGrThreadFactory */
    private class PackageInstallObserver extends IPackageInstallObserver.Stub {
        private static final int SUCCEEDED = 1;

        /* synthetic */ PackageInstallObserver(OppoGrThread this$0, PackageInstallObserver packageInstallObserver) {
            this();
        }

        private PackageInstallObserver() {
        }

        public void packageInstalled(String packageName, int returnCode) {
            Integer progress;
            if (OppoGrThread.this.mAction.intValue() == 1) {
                if (returnCode == 1) {
                    if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                        Log.d("OppoGrThread", "Geloin: we installed " + packageName);
                    }
                    if (!OppoGrThread.this.mIsGRIn.booleanValue()) {
                        OppoGrThread.this.mProgressDialog.incrementProgressBy(1);
                    }
                    progress = Integer.valueOf(OppoGrThread.this.mProgressDialog.getProgress());
                    if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                        Log.d("OppoGrThread", "Geloin: now the progress is " + progress + ", maxProgress is " + OppoGrThread.this.mMaxProgress + ", exception is " + OppoGrThread.this.mExceptionMaps.size());
                    }
                    if (!OppoGrThread.this.mFileQueue.isEmpty()) {
                        try {
                            File toInstallFile = (File) OppoGrThread.this.mFileQueue.take();
                            if (toInstallFile.getName().equals("5010a28878517c105a60f155f0c6f5c56.gr")) {
                                OppoGrThread.this.mIncreIndex = 11;
                            }
                            OppoGrThread.this.installApk(toInstallFile);
                            if (OppoGrThread.this.mIsGRIn.booleanValue()) {
                                OppoGrThread.this.mProgressDialog.setProgress(OppoGrThread.this.mPerMax);
                                OppoGrThread.this.mPerMax = ((OppoGrThread.this.mToInstallNum - OppoGrThread.this.mFileQueue.size()) + OppoGrThread.this.mIncreIndex) * OppoGrThread.this.mPerSize;
                                OppoGrThread.this.stopInstallSchedule();
                                OppoGrThread.this.startInstallSchedule(OppoGrThread.this.mPerMax);
                            }
                        } catch (Exception e) {
                            OppoGrThread.this.catchException(e, null);
                            return;
                        }
                    } else if (OppoGrThread.this.mIsGRIn.booleanValue()) {
                        if (progress.intValue() < OppoGrThread.this.mMaxProgress.intValue()) {
                            OppoGrThread.this.mProgressDialog.incrementProgressBy(OppoGrThread.this.mMaxProgress.intValue() - progress.intValue());
                        }
                        if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                            Log.d("OppoGrThread", "Geloin: fileQueue is empty.");
                        }
                        OppoGrThread.this.deleteTmpFiles();
                        OppoManager.canCreateDialog = Boolean.valueOf(true);
                        OppoGrThread.this.mProgressDialog.cancel();
                        OppoManager.doGr(null, OppoGrThread.this.mAppName, OppoGrThread.this.mLastPkgName, "DO_GR_SUCC");
                    }
                    if (!OppoGrThread.this.mIsGRIn.booleanValue()) {
                        if (progress.intValue() >= OppoGrThread.this.mMaxProgress.intValue()) {
                            if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                                Log.d("OppoGrThread", "Geloin: installer end.");
                            }
                            OppoGrThread.this.deleteTmpFiles();
                            OppoManager.canCreateDialog = Boolean.valueOf(true);
                            OppoGrThread.this.mProgressDialog.cancel();
                            OppoManager.doGr(null, OppoGrThread.this.mAppName, OppoGrThread.this.mLastPkgName, "DO_GR_SUCC");
                        } else if (progress.intValue() == OppoGrThread.this.mMaxProgress.intValue() - OppoGrThread.this.mExceptionApks.size()) {
                            if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                                Log.d("OppoGrThread", "Geloin we will install exceptions ");
                            }
                            for (String apkFilePath : OppoGrThread.this.mExceptionApks) {
                                OppoGrThread.this.installApk(new File(apkFilePath));
                            }
                        }
                    }
                } else {
                    if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                        Log.d("OppoGrThread", "Geloin: install error returncode is " + returnCode + ", PkgName is " + packageName);
                    }
                    if (packageName == null || OppoManager.grList.contains(packageName)) {
                        OppoGrThread.this.catchException(new RuntimeException("install error " + returnCode), null);
                    } else if (!OppoGrThread.this.mIsGRIn.booleanValue()) {
                        OppoGrThread.this.mProgressDialog.incrementProgressBy(1);
                    }
                }
            } else if (OppoGrThread.this.mAction.intValue() == 2) {
                if (returnCode == 1) {
                    if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                        Log.d("OppoGrThread", "Geloin: we reinstalled " + packageName);
                    }
                    progress = Integer.valueOf(OppoGrThread.this.mProgressDialog.getProgress());
                    if (progress.intValue() < OppoGrThread.this.mMaxProgress.intValue()) {
                        OppoGrThread.this.mProgressDialog.incrementProgressBy(OppoGrThread.this.mMaxProgress.intValue() - progress.intValue());
                        OppoManager.canReinstall = Boolean.valueOf(true);
                        OppoGrThread.this.mProgressDialog.cancel();
                        OppoManager.doGr(OppoGrThread.this.mAppName, null, OppoGrThread.this.mLastPkgName, "DO_GR_SUCC");
                    }
                } else {
                    OppoGrThread.this.catchException(new RuntimeException("install error" + returnCode), null);
                    if (OppoGrThread.this.DEBUG_GR.booleanValue()) {
                        Log.d("OppoGrThread", "Geloin: install error returncode is " + returnCode);
                    }
                }
            }
        }
    }

    public OppoGrThread(Context context, String basePathCode, String tipTitle, String tipContent, String grAbandon, String grOk, String exceptionContent, String appName, String lastPkgName) {
        this.mContext = context;
        this.mBasePathCode = basePathCode;
        this.mAction = Integer.valueOf(2);
        this.mTipTitle = tipTitle;
        this.mTipContent = tipContent;
        this.mGrAbandon = grAbandon;
        this.mGrOk = grOk;
        this.mExceptionContent = exceptionContent;
        this.mAppName = appName;
        this.mLastPkgName = lastPkgName;
        if (this.DEBUG_GR.booleanValue()) {
            Log.d("OppoGrThread", "Geloin: we will reinstall " + this.mBasePathCode);
        }
        this.mMaxProgress = Integer.valueOf(100);
        openDialog(this.mMaxProgress);
    }

    public OppoGrThread(String fileName, Context context, String basePathCode, String tipTitle, String tipContent, Map<String, String> exceptionMaps, String grAbandon, String grOk, String exceptionContent, String appName, String lastPkgName) {
        this.mContext = context;
        this.mBasePathCode = basePathCode;
        this.mTipTitle = tipTitle;
        this.mTipContent = tipContent;
        this.mAction = Integer.valueOf(1);
        this.mGrAbandon = grAbandon;
        this.mGrOk = grOk;
        this.mExceptionContent = exceptionContent;
        this.mExceptionMaps = exceptionMaps;
        this.mAppName = appName;
        this.mAbandon = Boolean.valueOf(false);
        this.mFileName = fileName;
        this.mLastPkgName = lastPkgName;
        if (this.mExceptionMaps == null) {
            this.mExceptionMaps = new HashMap();
        }
        this.mIsGRIn = Boolean.valueOf(true);
        for (String fp : OppoManager.mGrApkPathList) {
            File grFile = new File(fp);
            if (!grFile.exists()) {
                this.mIsGRIn = Boolean.valueOf(false);
                this.mGrFileList = new ArrayList();
                break;
            }
            this.mGrFileList.add(grFile);
        }
        if (this.mIsGRIn.booleanValue()) {
            this.mMaxProgress = Integer.valueOf(100);
            openDialog(this.mMaxProgress);
            return;
        }
        this.mMaxProgress = Integer.valueOf(OppoProcessManager.MSG_READY_ENTER_STRICTMODE);
        openDialog(this.mMaxProgress);
    }

    public OppoGrThread(Context context) {
        this.mContext = context;
        this.mAction = Integer.valueOf(3);
    }

    public OppoGrThread(Context context, Map<String, String> exceptionMaps) {
        this.mContext = context;
        this.mAction = Integer.valueOf(4);
        this.mExceptionMaps = exceptionMaps;
        if (this.mExceptionMaps == null) {
            this.mExceptionMaps = new HashMap();
        }
        this.mIsGRIn = Boolean.valueOf(true);
        for (String fp : OppoManager.mGrApkPathList) {
            File grFile = new File(fp);
            if (grFile.exists()) {
                this.mGrFileList.add(grFile);
            } else {
                this.mIsGRIn = Boolean.valueOf(false);
                this.mGrFileList = new ArrayList();
                return;
            }
        }
    }

    private void installGR() {
        if (this.mIsGRIn.booleanValue()) {
            try {
                if (this.mGrFileList == null || this.mGrFileList.size() != 6) {
                    OppoManager.canCreateDialog = Boolean.valueOf(true);
                    OppoManager.isNoDialogInstalling = Boolean.valueOf(false);
                }
                this.mPackageManager = this.mContext.getPackageManager();
                uninstallGrs();
                OppoManager.queue = new ArrayList();
                Integer apkNum = Integer.valueOf(6);
                try {
                    PackageInfo vendingInfo = this.mPackageManager.getPackageInfo(VENDING_PKG_NAME, 0);
                    if (vendingInfo != null) {
                        if (vendingInfo.versionCode < VENDING_CODE) {
                            uninstall(VENDING_PKG_NAME);
                        } else {
                            apkNum = Integer.valueOf(apkNum.intValue() - 1);
                        }
                    }
                } catch (Exception e) {
                }
                if (this.mExceptionMaps.containsKey(VENDING_PKG_NAME)) {
                    this.mExceptionMaps.remove(VENDING_PKG_NAME);
                }
                this.mFileQueue = new LinkedBlockingQueue(apkNum.intValue() + this.mExceptionMaps.size());
                Collections.sort(this.mGrFileList, new Comparator<File>() {
                    public int compare(File o1, File o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                for (int i = 0; i < apkNum.intValue(); i++) {
                    this.mFileQueue.add((File) this.mGrFileList.get(i));
                }
                for (Entry<String, String> en : this.mExceptionMaps.entrySet()) {
                    String path = (String) en.getValue();
                    if (!OppoManager.grList.contains((String) en.getKey()) && path.startsWith("/data/app")) {
                        this.mFileQueue.add(new File(path));
                    }
                }
                try {
                    installApkNoDialog((File) this.mFileQueue.take());
                } catch (Exception e2) {
                    OppoManager.canCreateDialog = Boolean.valueOf(true);
                    OppoManager.isNoDialogInstalling = Boolean.valueOf(false);
                }
            } catch (Exception e3) {
                OppoManager.canCreateDialog = Boolean.valueOf(true);
                OppoManager.isNoDialogInstalling = Boolean.valueOf(false);
            }
        }
    }

    private void installApkNoDialog(File apkFile) {
        this.mPackageManager.installPackage(Uri.fromFile(apkFile), this.mNoDialogObserver, 2, null);
    }

    public void run() {
        switch (this.mAction.intValue()) {
            case 1:
                downloadGR();
                return;
            case 2:
                reinstallApk();
                return;
            case 3:
                checkNet();
                return;
            case 4:
                installGR();
                return;
            default:
                return;
        }
    }

    private void checkNet() {
        if (OppoNetworkUtil.isNotChineseOperator(this.mContext, OppoNetworkUtil.getDataSlotId(this.mContext))) {
            if (OppoNetworkUtil.isForbidAccessGMS_inAbroadMobileNetwork(this.mContext) && !OppoNetworkUtil.checkGoogleNetwork()) {
                OppoManager.doGr(null, Boolean.valueOf(OppoNetworkUtil.isRedTeaSoftSimSupport(this.mContext)).toString(), null, "DO_GR_CHECK_INTERNET");
            }
        } else if (!OppoNetworkUtil.checkGoogleNetwork()) {
            OppoManager.doGr("isInChina", null, null, "DO_GR_CHECK_INTERNET");
        }
    }

    private void downloadGR() {
        if (this.mIsGRIn.booleanValue()) {
            installWhenInner();
        } else {
            downloadAndInstall();
        }
    }

    private void startInstallSchedule(final int max) {
        Runnable runnable = new Runnable() {
            public void run() {
                int nowProgress = OppoGrThread.this.mProgressDialog.getProgress();
                if (nowProgress == OppoGrThread.this.mMaxProgress.intValue() - 1 || nowProgress >= max) {
                    try {
                        OppoGrThread.this.mService.shutdown();
                        return;
                    } catch (Exception e) {
                        return;
                    }
                }
                OppoGrThread.this.mProgressDialog.incrementProgressBy(1);
            }
        };
        this.mService = Executors.newSingleThreadScheduledExecutor();
        this.mService.scheduleAtFixedRate(runnable, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void stopInstallSchedule() {
        try {
            this.mService.shutdown();
        } catch (Exception e) {
        }
    }

    private void installWhenInner() {
        try {
            if (this.mGrFileList == null || this.mGrFileList.size() != 6) {
                catchException(new RuntimeException("gr files not exist."), null);
            }
            this.mPackageManager = this.mContext.getPackageManager();
            uninstallGrs();
            OppoManager.queue = new ArrayList();
            Integer apkNum = Integer.valueOf(6);
            try {
                PackageInfo vendingInfo = this.mPackageManager.getPackageInfo(VENDING_PKG_NAME, 0);
                if (vendingInfo != null) {
                    if (vendingInfo.versionCode < VENDING_CODE) {
                        uninstall(VENDING_PKG_NAME);
                    } else {
                        apkNum = Integer.valueOf(apkNum.intValue() - 1);
                    }
                }
            } catch (Exception e) {
            }
            if (this.mExceptionMaps.containsKey(VENDING_PKG_NAME)) {
                this.mExceptionMaps.remove(VENDING_PKG_NAME);
            }
            this.mFileQueue = new LinkedBlockingQueue(apkNum.intValue() + this.mExceptionMaps.size());
            Collections.sort(this.mGrFileList, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (int i = 0; i < apkNum.intValue(); i++) {
                this.mFileQueue.add((File) this.mGrFileList.get(i));
            }
            for (Entry<String, String> en : this.mExceptionMaps.entrySet()) {
                String path = (String) en.getValue();
                if (!OppoManager.grList.contains((String) en.getKey()) && path.startsWith("/data/app")) {
                    this.mFileQueue.add(new File(path));
                }
            }
            this.mToInstallNum = this.mFileQueue.size();
            this.mPerSize = this.mMaxProgress.intValue() / (this.mToInstallNum + 11);
            try {
                installApk((File) this.mFileQueue.take());
                this.mPerMax = (this.mToInstallNum - this.mFileQueue.size()) * this.mPerSize;
                startInstallSchedule(this.mPerMax);
            } catch (Exception e2) {
                catchException(e2, null);
            }
        } catch (Exception e22) {
            catchException(e22, null);
        }
    }

    private void downloadAndInstall() {
        HttpURLConnection addrConn = null;
        Integer apkNum = OppoManager.GR_APK_NUMBER;
        try {
            String fromVersion = this.mFileName.substring(0, this.mFileName.lastIndexOf("."));
            if (this.DEBUG_GR.booleanValue()) {
                Log.d("OppoGrThread", "Geloin:from version is " + fromVersion);
            }
            addrConn = (HttpURLConnection) new URL("http://i.ota.coloros.com/post/QueryGr").openConnection();
            addrConn.setDoOutput(true);
            addrConn.setDoInput(true);
            addrConn.setRequestMethod("POST");
            addrConn.setUseCaches(false);
            addrConn.setInstanceFollowRedirects(true);
            addrConn.setRequestProperty("Content-Type", "application/x-javascript");
            addrConn.setConnectTimeout(10000);
            addrConn.setReadTimeout(10000);
            addrConn.connect();
            DataOutputStream dataout = new DataOutputStream(addrConn.getOutputStream());
            JSONObject jb = new JSONObject();
            jb.put(CompatibilityHelper.VERSION_NAME, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            jb.put("sys_info", fromVersion);
            dataout.writeBytes(jb.toString());
            dataout.flush();
            dataout.close();
            BufferedReader bf = new BufferedReader(new InputStreamReader(addrConn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = bf.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            bf.close();
            JSONObject jSONObject = new JSONObject(sb.toString());
            Object downUrlObj = jSONObject.get("down_url");
            Object apkNumObj = jSONObject.get("apkNumber");
            if (downUrlObj != null) {
                String downUrl = downUrlObj.toString();
                if (apkNumObj != null) {
                    apkNum = Integer.valueOf(Integer.parseInt(apkNumObj.toString()));
                    this.mFileQueue = new LinkedBlockingQueue(apkNum.intValue());
                }
                try {
                    addrConn.disconnect();
                } catch (Exception e) {
                }
                boolean containsVending = false;
                try {
                    if (apkNum.intValue() == 6 && this.mExceptionMaps.containsKey(VENDING_PKG_NAME)) {
                        this.mExceptionMaps.remove(VENDING_PKG_NAME);
                        containsVending = true;
                    }
                    this.mExceptionApks = new HashSet();
                    for (Entry<String, String> en : this.mExceptionMaps.entrySet()) {
                        String path = (String) en.getValue();
                        if (!OppoManager.grList.contains((String) en.getKey()) && path.startsWith("/data/app")) {
                            this.mExceptionApks.add(path);
                        }
                    }
                    this.mPackageManager = this.mContext.getPackageManager();
                    uninstallGrs();
                    OppoManager.queue = new ArrayList();
                    if (this.DEBUG_GR.booleanValue()) {
                        Log.d("OppoGrThread", "Geloin:download from " + downUrl + ", apk num is " + apkNum);
                    }
                    try {
                        this.mConn = (HttpURLConnection) new URL(downUrl).openConnection();
                        this.mConn.setConnectTimeout(10000);
                        this.mConn.setReadTimeout(10000);
                        Integer fileLength = Integer.valueOf(this.mConn.getContentLength());
                        if (fileLength.intValue() < 10240) {
                            catchException(new RuntimeException("Zip file must greater than 10240"), "NetworkError");
                            try {
                                this.mIs.close();
                                this.mFos.close();
                                this.mConn.disconnect();
                            } catch (Exception e2) {
                                if (this.DEBUG_GR.booleanValue()) {
                                    Log.w("OppoGrThread", "Close All connect.");
                                }
                            }
                            return;
                        }
                        Integer sepSize = Integer.valueOf(fileLength.intValue() / (((this.mMaxProgress.intValue() - apkNum.intValue()) - this.mExceptionApks.size()) - 1));
                        this.mIs = this.mConn.getInputStream();
                        String rootDir = "/data/system";
                        this.mRootDirFile = new File(rootDir);
                        if (!this.mRootDirFile.exists()) {
                            this.mRootDirFile.mkdirs();
                        }
                        String zipFilePath = rootDir + File.separator + this.mFileName;
                        File zipFile = new File(zipFilePath);
                        this.mFos = new FileOutputStream(zipFile);
                        byte[] buf = new byte[4096];
                        Integer length = Integer.valueOf(0);
                        Integer count = Integer.valueOf(0);
                        while (true) {
                            try {
                                int nbytes = this.mIs.read(buf);
                                if (nbytes <= -1) {
                                    break;
                                }
                                this.mFos.write(buf, 0, nbytes);
                                length = Integer.valueOf(length.intValue() + nbytes);
                                if (length.intValue() / sepSize.intValue() >= count.intValue()) {
                                    count = Integer.valueOf(count.intValue() + 1);
                                    this.mProgressDialog.incrementProgressBy(1);
                                }
                            } catch (Exception e3) {
                                catchException(e3, "NetworkError");
                                try {
                                    this.mIs.close();
                                    this.mFos.close();
                                    this.mConn.disconnect();
                                } catch (Exception e4) {
                                    if (this.DEBUG_GR.booleanValue()) {
                                        Log.w("OppoGrThread", "Close All connect.");
                                    }
                                }
                                return;
                            }
                        }
                        if (containsVending) {
                            uninstall(VENDING_PKG_NAME);
                        }
                        this.mNeedBeDeletes.add(zipFile);
                        List<File> files = unZipFile(zipFile, rootDir);
                        exec("chmod -R 777 " + zipFilePath);
                        if (files != null && files.size() > 0) {
                            for (int i = 0; i < files.size(); i++) {
                                File file = (File) files.get(i);
                                this.mNeedBeDeletes.add(file);
                                if (file.getName().endsWith(".apk")) {
                                    this.mGrFileList.add(file);
                                }
                            }
                            Collections.sort(this.mGrFileList, new Comparator<File>() {
                                public int compare(File o1, File o2) {
                                    return o1.getName().compareTo(o2.getName());
                                }
                            });
                            for (File f : this.mGrFileList) {
                                this.mFileQueue.add(f);
                            }
                            try {
                                installApk((File) this.mFileQueue.take());
                            } catch (Exception e32) {
                                catchException(e32, null);
                                try {
                                    this.mIs.close();
                                    this.mFos.close();
                                    this.mConn.disconnect();
                                } catch (Exception e5) {
                                    if (this.DEBUG_GR.booleanValue()) {
                                        Log.w("OppoGrThread", "Close All connect.");
                                    }
                                }
                                return;
                            }
                        }
                        try {
                            this.mIs.close();
                            this.mFos.close();
                            this.mConn.disconnect();
                        } catch (Exception e6) {
                            if (this.DEBUG_GR.booleanValue()) {
                                Log.w("OppoGrThread", "Close All connect.");
                            }
                        }
                        return;
                    } catch (Exception e322) {
                        catchException(e322, "NetworkError");
                        try {
                            this.mIs.close();
                            this.mFos.close();
                            this.mConn.disconnect();
                        } catch (Exception e7) {
                            if (this.DEBUG_GR.booleanValue()) {
                                Log.w("OppoGrThread", "Close All connect.");
                            }
                        }
                        return;
                    }
                } catch (Exception e3222) {
                    catchException(e3222, null);
                    try {
                        this.mIs.close();
                        this.mFos.close();
                        this.mConn.disconnect();
                    } catch (Exception e8) {
                        if (this.DEBUG_GR.booleanValue()) {
                            Log.w("OppoGrThread", "Close All connect.");
                        }
                    }
                    return;
                } catch (Throwable th) {
                    try {
                        this.mIs.close();
                        this.mFos.close();
                        this.mConn.disconnect();
                    } catch (Exception e9) {
                        if (this.DEBUG_GR.booleanValue()) {
                            Log.w("OppoGrThread", "Close All connect.");
                        }
                    }
                    throw th;
                }
            }
            catchException(new RuntimeException("Can not get download url."), "NetworkError");
            try {
                addrConn.disconnect();
            } catch (Exception e10) {
            }
        } catch (Exception e32222) {
            catchException(e32222, "NetworkError");
            e32222.printStackTrace();
            try {
                addrConn.disconnect();
            } catch (Exception e11) {
            }
        } catch (Throwable th2) {
            try {
                addrConn.disconnect();
            } catch (Exception e12) {
            }
            throw th2;
        }
    }

    private void reinstallApk() {
        this.mPackageManager = this.mContext.getPackageManager();
        installApk(new File(this.mBasePathCode));
        this.mService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (OppoGrThread.this.mProgressDialog.getProgress() == OppoGrThread.this.mMaxProgress.intValue() - 1) {
                    try {
                        OppoGrThread.this.mService.shutdown();
                        return;
                    } catch (Exception e) {
                        return;
                    }
                }
                OppoGrThread.this.mProgressDialog.incrementProgressBy(1);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private List<File> unZipFile(File zipFile, String rootDir) throws ZipException, IOException {
        String zipFileName = zipFile.getName();
        List<File> result = new ArrayList();
        if (!rootDir.endsWith(File.separator)) {
            rootDir = rootDir + File.separator;
        }
        ZipFile zFile = new ZipFile(zipFile);
        Enumeration zList = zFile.entries();
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) zList.nextElement();
            String filePath = rootDir + ze.getName();
            if (ze.isDirectory()) {
                new File(filePath).mkdir();
            } else {
                File targetFile = new File(filePath);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile));
                InputStream is = new BufferedInputStream(zFile.getInputStream(ze));
                while (true) {
                    int readLen = is.read(buf, 0, 1024);
                    if (readLen == -1) {
                        break;
                    }
                    os.write(buf, 0, readLen);
                }
                is.close();
                os.close();
                result.add(targetFile);
                exec("chmod 777 " + filePath);
            }
        }
        zFile.close();
        return result;
    }

    private void exec(String command) {
        try {
            Process proc = Runtime.getRuntime().exec(command);
            proc.getOutputStream().close();
            proc.waitFor();
        } catch (Exception e) {
            catchException(e, null);
        }
    }

    private void uninstallGrs() {
        List<String> grList = OppoManager.grList;
        for (int i = 0; i < grList.size(); i++) {
            try {
                this.mPackageManager.deletePackage((String) grList.get(i), new PackageDeleteObserver(this, null), 2);
            } catch (Exception e) {
            }
        }
    }

    private void uninstall(String pkgName) {
        if (pkgName != null) {
            try {
                this.mPackageManager.deletePackage(pkgName, new PackageDeleteObserver(this, null), 2);
            } catch (Exception e) {
            }
        }
    }

    private void catchException(Exception e, String exceptionType) {
        if (!this.mAbandon.booleanValue()) {
            OppoManager.canCreateDialog = Boolean.valueOf(true);
            OppoManager.isNoDialogInstalling = Boolean.valueOf(false);
            OppoManager.canReinstall = Boolean.valueOf(true);
            if (this.mProgressDialog != null) {
                this.mProgressDialog.cancel();
            }
            if (this.DEBUG_GR.booleanValue()) {
                e.printStackTrace();
            }
            deleteTmpFiles();
            OppoManager.doGr(null, null, exceptionType, "DO_GR_SHOW_EXCEPTION");
        }
    }

    public void openDialog(Integer maxProgress) {
        this.mProgressDialog = new ProgressDialog(this.mContext);
        this.mProgressDialog.getWindow().setType(2003);
        this.mProgressDialog.setTitle(this.mTipTitle);
        this.mProgressDialog.setMessage(this.mTipContent);
        this.mProgressDialog.setProgressStyle(1);
        this.mProgressDialog.setMax(maxProgress.intValue());
        this.mProgressDialog.setProgress(0);
        if (this.mAction.intValue() == 1 && !this.mIsGRIn.booleanValue()) {
            this.mProgressDialog.setButton(-2, this.mGrAbandon, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    OppoGrThread.this.mAbandon = Boolean.valueOf(true);
                    OppoGrThread.this.deleteTmpFiles();
                    if (OppoGrThread.this.mAction.intValue() == 1) {
                        OppoManager.canCreateDialog = Boolean.valueOf(true);
                    } else if (OppoGrThread.this.mAction.intValue() == 2) {
                        OppoManager.canReinstall = Boolean.valueOf(true);
                    }
                    OppoGrThread.this.mProgressDialog.cancel();
                }
            });
        }
        this.mProgressDialog.setCanceledOnTouchOutside(false);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setProgressNumberFormat(IElsaManager.EMPTY_PACKAGE);
        this.mProgressDialog.show();
    }

    private void deleteTmpFiles() {
        try {
            if (this.mNeedBeDeletes != null) {
                for (File file : this.mNeedBeDeletes) {
                    if (file.isDirectory()) {
                        FileUtils.deleteContents(file);
                    }
                    file.delete();
                }
            }
            this.mIs.close();
            this.mFos.close();
            if (this.DEBUG_GR.booleanValue()) {
                Log.w("OppoGrThread", "Close All connect.");
            }
        } catch (Exception e) {
            if (this.DEBUG_GR.booleanValue()) {
                Log.d("OppoGrThread", "Close All connect.");
            }
        }
    }

    private void installApk(File apkFile) {
        this.mPackageManager.installPackage(Uri.fromFile(apkFile), this.mObserver, 2, null);
    }
}
