package com.mediatek.server.cta.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionRecords;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoCrashClearManager;
import com.mediatek.cta.CtaUtils;
import com.mediatek.server.cta.CtaPermsController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PermRecordsController {
    static final boolean DEBUG_WRITEFILE = false;
    private static final String KEY_PERM_NAME = "PERM_NAME";
    private static final String KEY_REQUEST_TIME = "REQUEST_TIME";
    private static final String KEY_UID = "UID";
    private static final int MSG_REPORT_PERM_RECORDS = 1;
    static int RECORDS_LIMIT = 0;
    static final String TAG = "PermRecordsController";
    static final long WRITE_DELAY = 1800000;
    Context mContext;
    final AtomicFile mFile;
    PermRecordsHandler mHandler;
    private BroadcastReceiver mIntentReceiver;
    private final SparseArray<UserData> mUserDatas = new SparseArray();
    final Runnable mWriteRunner = new Runnable() {
        public void run() {
            synchronized (PermRecordsController.this) {
                PermRecordsController.this.mWriteScheduled = false;
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... voidArr) {
                        PermRecordsController.this.writeState();
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            }
        }
    };
    boolean mWriteScheduled;

    private static final class PackageData {
        public ArrayMap<String, PermissionRecord> mRecords = new ArrayMap();
        public final String packageName;

        public PackageData(String str) {
            this.packageName = str;
        }

        public void addPermissionRecord(PermissionRecord permissionRecord) {
            this.mRecords.put(permissionRecord.permission, permissionRecord);
        }
    }

    class PermRecordsHandler extends Handler {
        PermRecordsHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    PermRecordsController.this.handlePermRequestUsage(message.getData());
                    return;
                default:
                    return;
            }
        }
    }

    private static final class PermissionRecord {
        public final String permission;
        public List<Long> requestTimes = new ArrayList();

        public PermissionRecord(String str) {
            this.permission = str;
        }

        public void addUsageTime(long j) {
            this.requestTimes.add(Long.valueOf(j));
            if (this.requestTimes.size() > PermRecordsController.RECORDS_LIMIT) {
                this.requestTimes.remove(0);
            }
        }
    }

    private static final class UserData {
        public ArrayMap<String, PackageData> mPackageDatas = new ArrayMap();
        public final int userId;

        public UserData(int i) {
            this.userId = i;
        }

        public void addPackageData(PackageData packageData) {
            this.mPackageDatas.put(packageData.packageName, packageData);
        }
    }

    public PermRecordsController(Context context) {
        this.mContext = context;
        this.mFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "permission_records.xml"));
        RECORDS_LIMIT = this.mContext.getResources().getInteger(134938635);
        setupHandler();
        readState();
    }

    public void systemReady() {
        if (CtaPermsController.DEBUG) {
            Slog.d(TAG, "systemReady()");
        }
        setupReceiver();
    }

    private void setupHandler() {
        HandlerThread anonymousClass2 = new HandlerThread(TAG, -2) {
            public void run() {
                Process.setCanSelfBackground(false);
                if (StrictMode.conditionallyEnableDebugLogging()) {
                    Slog.i(PermRecordsController.TAG, "Enabled StrictMode logging for " + getName() + " looper.");
                }
                super.run();
            }
        };
        anonymousClass2.start();
        this.mHandler = new PermRecordsHandler(anonymousClass2.getLooper());
    }

    private void setupReceiver() {
        if (CtaPermsController.DEBUG) {
            Slog.d(TAG, "setupReceiver()");
        }
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String str = null;
                String action = intent.getAction();
                int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                int intExtra2 = intent.getIntExtra("android.intent.extra.UID", -1);
                Uri data = intent.getData();
                if (data != null) {
                    str = data.getSchemeSpecificPart();
                }
                boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                if (CtaPermsController.DEBUG) {
                    Slog.d(PermRecordsController.TAG, "Receive intent action = " + action + ", user = " + intExtra + ", appUid = " + intExtra2 + ", appName = " + str + ", replacing = " + booleanExtra);
                }
                if ("android.intent.action.USER_REMOVED".equals(action)) {
                    PermRecordsController.this.onUserRemoved(intExtra);
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !booleanExtra) {
                    PermRecordsController.this.onAppRemoved(intExtra, intExtra2, str);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, null);
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, null);
    }

    private void onUserRemoved(int i) {
        if (CtaPermsController.DEBUG) {
            Slog.d(TAG, "onUserRemoved() user = " + i);
        }
        synchronized (this.mUserDatas) {
            if (((UserData) this.mUserDatas.get(i)) != null) {
                this.mUserDatas.remove(i);
                return;
            }
        }
    }

    private void onAppRemoved(int i, int i2, String str) {
        if (CtaPermsController.DEBUG) {
            Slog.d(TAG, "onAppRemoved() user = " + i + ", appUid = " + i2 + ", pkgName = " + str);
        }
        synchronized (this.mUserDatas) {
            UserData userData = (UserData) this.mUserDatas.get(i);
            if (userData == null) {
            } else if (((PackageData) userData.mPackageDatas.get(str)) != null) {
                userData.mPackageDatas.remove(str);
            }
        }
    }

    public void reportPermRequestUsage(String str, int i) {
        if (!CtaUtils.isCtaSupported()) {
            return;
        }
        if (TextUtils.isEmpty(str)) {
            Slog.w(TAG, "reportPermRequestUsage() permName is null; ignoring");
        } else if (CtaUtils.isCtaMonitoredPerms(str)) {
            long currentTimeMillis = System.currentTimeMillis();
            if (isCtaEnhanceLogEnable()) {
                Slog.d(TAG, "reportPermRequestUsage() permName = " + str + ", uid = " + i + ", requestTime = " + currentTimeMillis);
            }
            Message obtain = Message.obtain();
            obtain.what = 1;
            Bundle bundle = new Bundle();
            bundle.putString(KEY_PERM_NAME, str);
            bundle.putInt(KEY_UID, i);
            bundle.putLong(KEY_REQUEST_TIME, currentTimeMillis);
            obtain.setData(bundle);
            this.mHandler.sendMessage(obtain);
        }
    }

    private boolean isCtaEnhanceLogEnable() {
        return LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("cta.log.enable"));
    }

    private void handlePermRequestUsage(Bundle bundle) {
        String string = bundle.getString(KEY_PERM_NAME);
        int i = bundle.getInt(KEY_UID);
        long j = bundle.getLong(KEY_REQUEST_TIME);
        String[] packagesForUid = this.mContext.getPackageManager().getPackagesForUid(i);
        if (packagesForUid == null || packagesForUid.length == 0) {
            Slog.w(TAG, "handlePermRequestUsage() packages for uid = " + i + " is null; ignoring");
            return;
        }
        i = UserHandle.getUserId(i);
        for (String str : packagesForUid) {
            if (isPackageRequestingPermission(str, string, i)) {
                addPermissionRecords(i, string, str, j, true);
            }
        }
    }

    private boolean isPackageRequestingPermission(String str, String str2, int i) {
        try {
            PackageInfo packageInfoAsUser = this.mContext.getPackageManager().getPackageInfoAsUser(str, 4096, i);
            if (packageInfoAsUser == null) {
                return false;
            }
            int length = packageInfoAsUser.requestedPermissions == null ? 0 : packageInfoAsUser.requestedPermissions.length;
            for (int i2 = 0; i2 < length; i2++) {
                if (str2.equals(packageInfoAsUser.requestedPermissions[i2])) {
                    return true;
                }
            }
            return false;
        } catch (NameNotFoundException e) {
            Slog.w(TAG, "Couldn't retrieve permissions for package:" + str);
            return false;
        } catch (NullPointerException e2) {
            Slog.w(TAG, "can not get pkgInfo for package:" + str);
            return false;
        }
    }

    public void addPermissionRecords(int i, String str, String str2, long j, boolean z) {
        if (isCtaEnhanceLogEnable()) {
            Slog.d(TAG, "addPermissionRecords userId = " + i + ", pkgName = " + str2 + ", permName = " + str + ", time = " + j + ", doWrite = " + z);
        }
        synchronized (this.mUserDatas) {
            UserData userData;
            PackageData packageData;
            UserData userData2 = (UserData) this.mUserDatas.get(i);
            if (userData2 != null) {
                userData = userData2;
            } else {
                userData2 = new UserData(i);
                this.mUserDatas.put(i, userData2);
                userData = userData2;
            }
            PackageData packageData2 = (PackageData) userData.mPackageDatas.get(str2);
            if (packageData2 != null) {
                packageData = packageData2;
            } else {
                packageData2 = new PackageData(str2);
                userData.addPackageData(packageData2);
                packageData = packageData2;
            }
            PermissionRecord permissionRecord = (PermissionRecord) packageData.mRecords.get(str);
            if (permissionRecord == null) {
                permissionRecord = new PermissionRecord(str);
                packageData.addPermissionRecord(permissionRecord);
            }
            permissionRecord.addUsageTime(j);
        }
        if (z) {
            scheduleWriteLocked();
        }
    }

    public List<String> getPermRecordPkgs() {
        int callUserId = getCallUserId();
        synchronized (this.mUserDatas) {
            UserData userData = (UserData) this.mUserDatas.get(callUserId);
            if (userData != null) {
                List arrayList = new ArrayList(userData.mPackageDatas.keySet());
                return arrayList;
            }
            Slog.w(TAG, "getPermRecordPkgs(), no permission records for userId = " + callUserId);
            return null;
        }
    }

    public List<String> getPermRecordPerms(String str) {
        int callUserId = getCallUserId();
        synchronized (this.mUserDatas) {
            UserData userData = (UserData) this.mUserDatas.get(callUserId);
            if (userData != null) {
                PackageData packageData = (PackageData) userData.mPackageDatas.get(str);
                if (packageData != null) {
                    List arrayList = new ArrayList(packageData.mRecords.keySet());
                    return arrayList;
                }
                Slog.w(TAG, "getPermRecordPerms(), no permission records for userId = " + callUserId + ", packageName = " + str);
                return null;
            }
            Slog.w(TAG, "getPermRecordPerms(), no permission records for userId = " + callUserId);
            return null;
        }
    }

    public PermissionRecords getPermRecords(String str, String str2) {
        int callUserId = getCallUserId();
        synchronized (this.mUserDatas) {
            UserData userData = (UserData) this.mUserDatas.get(callUserId);
            if (userData != null) {
                PackageData packageData = (PackageData) userData.mPackageDatas.get(str);
                if (packageData != null) {
                    PermissionRecord permissionRecord = (PermissionRecord) packageData.mRecords.get(str2);
                    if (permissionRecord != null) {
                        PermissionRecords permissionRecords = new PermissionRecords(str, str2, new ArrayList(permissionRecord.requestTimes));
                        return permissionRecords;
                    }
                    Slog.w(TAG, "getPermRecords(), no permission records for userId = " + callUserId + ", packageName = " + str + ", permName = " + str2);
                    return null;
                }
                Slog.w(TAG, "getPermRecords(), no permission records for userId = " + callUserId + ", packageName = " + str);
                return null;
            }
            Slog.w(TAG, "getPermRecords(), no permission records for userId = " + callUserId);
            return null;
        }
    }

    private int getCallUserId() {
        return UserHandle.getUserId(Binder.getCallingUid());
    }

    private void scheduleWriteLocked() {
        if (!this.mWriteScheduled) {
            this.mWriteScheduled = true;
            this.mHandler.postDelayed(this.mWriteRunner, 1800000);
        }
    }

    public void shutdown() {
        Object obj = null;
        Slog.d(TAG, "Writing data to file before shutdown...");
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
        synchronized (this) {
            if (this.mWriteScheduled) {
                this.mWriteScheduled = false;
                obj = 1;
            }
        }
        if (obj != null) {
            writeState();
        }
    }

    void readState() {
        Slog.d(TAG, "readState() BEGIN");
        synchronized (this.mFile) {
            synchronized (this) {
                try {
                    InputStream openRead = this.mFile.openRead();
                    this.mUserDatas.clear();
                    try {
                        int next;
                        XmlPullParser newPullParser = Xml.newPullParser();
                        newPullParser.setInput(openRead, StandardCharsets.UTF_8.name());
                        do {
                            next = newPullParser.next();
                            if (next != 2) {
                                break;
                            }
                            break;
                        } while (next != 1);
                        if (next == 2) {
                            next = newPullParser.getDepth();
                            while (true) {
                                int next2 = newPullParser.next();
                                if (next2 == 1) {
                                    break;
                                }
                                if (next2 == 3) {
                                    if (newPullParser.getDepth() <= next) {
                                        break;
                                    }
                                }
                                if (!(next2 == 3 || next2 == 4)) {
                                    if (newPullParser.getName().equals("userId")) {
                                        readUserId(newPullParser);
                                    } else {
                                        Slog.w(TAG, "Unknown element under <perm-records>: " + newPullParser.getName());
                                        XmlUtils.skipCurrentTag(newPullParser);
                                    }
                                }
                            }
                            if (openRead != null) {
                                try {
                                    openRead.close();
                                } catch (IOException e) {
                                    Slog.w(TAG, "Failed to close stream: " + e);
                                }
                            }
                        } else {
                            throw new IllegalStateException("no start tag found");
                        }
                    } catch (IllegalStateException e2) {
                        Slog.w(TAG, "Failed parsing " + e2);
                        Slog.w(TAG, "readState() fails");
                        this.mUserDatas.clear();
                        if (openRead != null) {
                            try {
                                openRead.close();
                            } catch (IOException e3) {
                                Slog.w(TAG, "Failed to close stream: " + e3);
                            }
                        }
                    } catch (NullPointerException e4) {
                        Slog.w(TAG, "Failed parsing " + e4);
                        Slog.w(TAG, "readState() fails");
                        this.mUserDatas.clear();
                        if (openRead != null) {
                            try {
                                openRead.close();
                            } catch (IOException e32) {
                                Slog.w(TAG, "Failed to close stream: " + e32);
                            }
                        }
                    } catch (NumberFormatException e5) {
                        Slog.w(TAG, "Failed parsing " + e5);
                        Slog.w(TAG, "readState() fails");
                        this.mUserDatas.clear();
                        if (openRead != null) {
                            try {
                                openRead.close();
                            } catch (IOException e322) {
                                Slog.w(TAG, "Failed to close stream: " + e322);
                            }
                        }
                    } catch (XmlPullParserException e6) {
                        Slog.w(TAG, "Failed parsing " + e6);
                        Slog.w(TAG, "readState() fails");
                        this.mUserDatas.clear();
                        if (openRead != null) {
                            try {
                                openRead.close();
                            } catch (IOException e3222) {
                                Slog.w(TAG, "Failed to close stream: " + e3222);
                            }
                        }
                    } catch (IOException e32222) {
                        Slog.w(TAG, "Failed parsing " + e32222);
                        Slog.w(TAG, "readState() fails");
                        this.mUserDatas.clear();
                        if (openRead != null) {
                            try {
                                openRead.close();
                            } catch (IOException e322222) {
                                Slog.w(TAG, "Failed to close stream: " + e322222);
                            }
                        }
                    } catch (IndexOutOfBoundsException e7) {
                        Slog.w(TAG, "Failed parsing " + e7);
                        Slog.w(TAG, "readState() fails");
                        this.mUserDatas.clear();
                        if (openRead != null) {
                            try {
                                openRead.close();
                            } catch (IOException e3222222) {
                                Slog.w(TAG, "Failed to close stream: " + e3222222);
                            }
                        }
                    } catch (Throwable th) {
                        Slog.w(TAG, "readState() fails");
                        this.mUserDatas.clear();
                        if (openRead != null) {
                            try {
                                openRead.close();
                            } catch (IOException e8) {
                                Slog.w(TAG, "Failed to close stream: " + e8);
                            }
                        }
                    }
                } catch (FileNotFoundException e9) {
                    Slog.i(TAG, "No existing permission records " + this.mFile.getBaseFile() + "; starting empty");
                    return;
                }
            }
        }
        Slog.d(TAG, "readState() END");
        return;
    }

    void readUserId(XmlPullParser xmlPullParser) throws NumberFormatException, XmlPullParserException, IOException {
        int parseInt = Integer.parseInt(xmlPullParser.getAttributeValue(null, OppoCrashClearManager.CRASH_COUNT));
        int depth = xmlPullParser.getDepth();
        while (true) {
            int next = xmlPullParser.next();
            if (next != 1) {
                if (next != 3 || xmlPullParser.getDepth() > depth) {
                    if (!(next == 3 || next == 4 || !xmlPullParser.getName().equals("pkg"))) {
                        readPackage(xmlPullParser, parseInt);
                    }
                } else {
                    return;
                }
            }
            return;
        }
    }

    void readPackage(XmlPullParser xmlPullParser, int i) throws NumberFormatException, XmlPullParserException, IOException {
        String attributeValue = xmlPullParser.getAttributeValue(null, OppoCrashClearManager.CRASH_COUNT);
        int depth = xmlPullParser.getDepth();
        while (true) {
            int next = xmlPullParser.next();
            if (next != 1) {
                if (next != 3 || xmlPullParser.getDepth() > depth) {
                    if (!(next == 3 || next == 4 || !xmlPullParser.getName().equals("perm"))) {
                        readPermission(xmlPullParser, i, attributeValue);
                    }
                } else {
                    return;
                }
            }
            return;
        }
    }

    void readPermission(XmlPullParser xmlPullParser, int i, String str) throws XmlPullParserException, IOException {
        String attributeValue = xmlPullParser.getAttributeValue(null, OppoCrashClearManager.CRASH_COUNT);
        int depth = xmlPullParser.getDepth();
        while (true) {
            int next = xmlPullParser.next();
            if (next != 1) {
                if (next != 3 || xmlPullParser.getDepth() > depth) {
                    if (!(next == 3 || next == 4 || !xmlPullParser.getName().equals("time"))) {
                        addPermissionRecords(i, attributeValue, str, Long.parseLong(xmlPullParser.getAttributeValue(null, OppoCrashClearManager.CRASH_COUNT)), false);
                    }
                } else {
                    return;
                }
            }
            return;
        }
    }

    void writeState() {
        Slog.d(TAG, "writeState() BEGIN");
        SparseArray sparseArray = new SparseArray();
        copyUserDatas(sparseArray);
        synchronized (this.mFile) {
            synchronized (this) {
                try {
                    OutputStream startWrite = this.mFile.startWrite();
                    try {
                        XmlSerializer fastXmlSerializer = new FastXmlSerializer();
                        fastXmlSerializer.setOutput(startWrite, StandardCharsets.UTF_8.name());
                        fastXmlSerializer.startDocument(null, Boolean.valueOf(true));
                        fastXmlSerializer.startTag(null, "perm-records");
                        int size = sparseArray.size();
                        for (int i = 0; i < size; i++) {
                            UserData userData = (UserData) sparseArray.valueAt(i);
                            fastXmlSerializer.startTag(null, "userId");
                            fastXmlSerializer.attribute(null, OppoCrashClearManager.CRASH_COUNT, String.valueOf(userData.userId));
                            int i2 = 0;
                            while (true) {
                                int i3 = i2;
                                if (i3 >= userData.mPackageDatas.size()) {
                                    break;
                                }
                                PackageData packageData = (PackageData) userData.mPackageDatas.valueAt(i3);
                                fastXmlSerializer.startTag(null, "pkg");
                                fastXmlSerializer.attribute(null, OppoCrashClearManager.CRASH_COUNT, packageData.packageName);
                                int i4 = 0;
                                while (true) {
                                    int i5 = i4;
                                    if (i5 >= packageData.mRecords.size()) {
                                        break;
                                    }
                                    PermissionRecord permissionRecord = (PermissionRecord) packageData.mRecords.valueAt(i5);
                                    fastXmlSerializer.startTag(null, "perm");
                                    fastXmlSerializer.attribute(null, OppoCrashClearManager.CRASH_COUNT, permissionRecord.permission);
                                    for (int i6 = 0; i6 < permissionRecord.requestTimes.size(); i6++) {
                                        fastXmlSerializer.startTag(null, "time");
                                        fastXmlSerializer.attribute(null, OppoCrashClearManager.CRASH_COUNT, String.valueOf(permissionRecord.requestTimes.get(i6)));
                                        fastXmlSerializer.endTag(null, "time");
                                    }
                                    fastXmlSerializer.endTag(null, "perm");
                                    i4 = i5 + 1;
                                }
                                fastXmlSerializer.endTag(null, "pkg");
                                i2 = i3 + 1;
                            }
                            fastXmlSerializer.endTag(null, "userId");
                        }
                        fastXmlSerializer.endTag(null, "perm-records");
                        fastXmlSerializer.endDocument();
                        this.mFile.finishWrite(startWrite);
                        if (startWrite != null) {
                            try {
                                startWrite.close();
                            } catch (IOException e) {
                                Slog.w(TAG, "Failed to close stream: " + e);
                            }
                        }
                    } catch (Throwable e2) {
                        Slog.w(TAG, "Failed to write state, restoring backup.", e2);
                        this.mFile.failWrite(startWrite);
                        if (startWrite != null) {
                            try {
                                startWrite.close();
                            } catch (IOException e3) {
                                Slog.w(TAG, "Failed to close stream: " + e3);
                            }
                        }
                    } catch (Throwable th) {
                        if (startWrite != null) {
                            try {
                                startWrite.close();
                            } catch (IOException e4) {
                                Slog.w(TAG, "Failed to close stream: " + e4);
                            }
                        }
                    }
                } catch (IOException e32) {
                    Slog.w(TAG, "Failed to write state: " + e32);
                    return;
                }
            }
        }
        Slog.d(TAG, "writeState() END");
        return;
    }

    private void copyUserDatas(SparseArray<UserData> sparseArray) {
        Slog.d(TAG, "copyUserDatas() BEGIN");
        synchronized (this.mUserDatas) {
            for (int i = 0; i < this.mUserDatas.size(); i++) {
                UserData userData = (UserData) this.mUserDatas.valueAt(i);
                for (int i2 = 0; i2 < userData.mPackageDatas.size(); i2++) {
                    PackageData packageData = (PackageData) userData.mPackageDatas.valueAt(i2);
                    for (int i3 = 0; i3 < packageData.mRecords.size(); i3++) {
                        PermissionRecord permissionRecord = (PermissionRecord) packageData.mRecords.valueAt(i3);
                        for (int i4 = 0; i4 < permissionRecord.requestTimes.size(); i4++) {
                            UserData userData2;
                            PackageData packageData2;
                            UserData userData3 = (UserData) sparseArray.get(userData.userId);
                            if (userData3 != null) {
                                userData2 = userData3;
                            } else {
                                userData3 = new UserData(userData.userId);
                                sparseArray.put(userData.userId, userData3);
                                userData2 = userData3;
                            }
                            PackageData packageData3 = (PackageData) userData2.mPackageDatas.get(packageData.packageName);
                            if (packageData3 != null) {
                                packageData2 = packageData3;
                            } else {
                                packageData3 = new PackageData(packageData.packageName);
                                userData2.addPackageData(packageData3);
                                packageData2 = packageData3;
                            }
                            PermissionRecord permissionRecord2 = (PermissionRecord) packageData2.mRecords.get(permissionRecord.permission);
                            if (permissionRecord2 == null) {
                                permissionRecord2 = new PermissionRecord(permissionRecord.permission);
                                packageData2.addPermissionRecord(permissionRecord2);
                            }
                            permissionRecord2.addUsageTime(((Long) permissionRecord.requestTimes.get(i4)).longValue());
                        }
                    }
                }
            }
        }
        Slog.d(TAG, "copyUserDatas() BEGIN");
    }
}
