package com.android.server.alipay;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.os.AtomicFile;
import com.android.server.alipay.RomUpdateHelper.UpdateInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public abstract class RomUpdateHelper<T extends UpdateInfo> {
    public static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    public static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String TAG = "RomUpdateHelper";
    private Context mContext;
    private String mDataFilePath;
    private String mFilterName;
    private String mSystemFilePath;
    private T mUpdateInfoData;
    private T mUpdateInfoSys;

    /* access modifiers changed from: protected */
    public abstract T newUpdateInfo();

    /* access modifiers changed from: protected */
    public abstract void onUpdateInfoChanged();

    /* access modifiers changed from: protected */
    public class UpdateInfo {
        public static final long INVALID_VERSION = -1;
        protected long mVersion = -1;

        protected UpdateInfo() {
        }

        public void parseContent(String content) {
        }

        public long getVersion() {
            return this.mVersion;
        }
    }

    public RomUpdateHelper(Context context, String filterName, String systemFile, String dataFile) {
        if (context == null || filterName == null || systemFile == null || dataFile == null) {
            throw new IllegalArgumentException("The parameters must not be null");
        }
        this.mContext = context;
        this.mFilterName = filterName;
        this.mSystemFilePath = systemFile;
        this.mDataFilePath = dataFile;
    }

    public T getSystemInfo() {
        return this.mUpdateInfoSys;
    }

    public T getDataInfo() {
        return this.mUpdateInfoData;
    }

    public T getNewerInfo() {
        return this.mUpdateInfoData.mVersion > this.mUpdateInfoSys.mVersion ? this.mUpdateInfoData : this.mUpdateInfoSys;
    }

    public void initialize() {
        this.mUpdateInfoSys = newUpdateInfo();
        try {
            String str = readStringFromFile(this.mSystemFilePath);
            if (str != null) {
                this.mUpdateInfoSys.parseContent(str);
            }
        } catch (Exception e) {
            Log.d(TAG, "parse system file failed", e);
        }
        this.mUpdateInfoData = newUpdateInfo();
        try {
            String str2 = readStringFromFile(this.mDataFilePath);
            if (str2 != null) {
                this.mUpdateInfoData.parseContent(str2);
            }
        } catch (Exception e2) {
            Log.d(TAG, "parse data file failed", e2);
        }
    }

    public void registerUpdateBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.alipay.RomUpdateHelper.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (RomUpdateHelper.DEBUG) {
                    Log.d(RomUpdateHelper.TAG, "Filter = " + RomUpdateHelper.this.mFilterName + ", onReceive intent = " + intent);
                }
                if (intent != null) {
                    try {
                        ArrayList<String> configs = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                        if (configs != null && configs.contains(RomUpdateHelper.this.mFilterName)) {
                            RomUpdateHelper.this.updateFromProvider();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, filter, OPPO_COMPONENT_SAFE_PERMISSION, null);
    }

    private String readStringFromFile(String filePath) {
        Path path = Paths.get(filePath, new String[0]);
        if (!Files.exists(path, new LinkOption[0])) {
            return null;
        }
        try {
            return new String(Files.readAllBytes(path));
        } catch (Exception e) {
            Log.d(TAG, "readStringFromFile", e);
            return null;
        }
    }

    private void saveStringToFile(String content, String filePath) {
        AtomicFile file = new AtomicFile(new File(filePath));
        FileOutputStream outStream = null;
        try {
            outStream = file.startWrite();
            outStream.write(content.getBytes());
        } catch (Exception e) {
            Log.d(TAG, "saveStringToFile", e);
            file.failWrite(outStream);
        } catch (Throwable th) {
            file.finishWrite(outStream);
            throw th;
        }
        file.finishWrite(outStream);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0069, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x006a, code lost:
        if (r4 != null) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0070, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0071, code lost:
        r2.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0074, code lost:
        throw r3;
     */
    private String getDataFromProvider() {
        String content = null;
        String[] projection = {"version", COLUMN_NAME_2};
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = CONTENT_URI_WHITE_LIST;
            Cursor cursor = contentResolver.query(uri, projection, "filtername=\"" + this.mFilterName + "\"", null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    int versionColumnIndex = cursor.getColumnIndex("version");
                    int xmlColumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                    if (cursor.moveToNext()) {
                        int configVersion = cursor.getInt(versionColumnIndex);
                        content = cursor.getString(xmlColumnIndex);
                        Log.d(TAG, "config updated, version = " + configVersion);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "config update failed:" + e);
        }
        return content;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFromProvider() {
        try {
            String content = getDataFromProvider();
            if (content != null) {
                T info = newUpdateInfo();
                info.parseContent(content);
                if (info.mVersion != -1) {
                    saveStringToFile(content, this.mDataFilePath);
                    if (this.mUpdateInfoData != null) {
                        this.mUpdateInfoData = info;
                        onUpdateInfoChanged();
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "updateFromProvider", e);
        }
    }
}
