package com.oppo.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RomUpdateHelper {
    public static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    public static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String TAG = "MM_RomUpdateHelper";
    public Context mContext = null;
    private String mDataFilePath = "";
    private String mFilterName = "";
    private String mSystemFilePath = "";
    private UpdateInfo mUpdateInfo1;
    private UpdateInfo mUpdateInfo2;
    private boolean mWhich2Use = true;

    protected class UpdateInfo {
        protected long mVersion = -1;

        protected UpdateInfo() {
        }

        public void parseContentFromXML(String content) {
        }

        public boolean clone(UpdateInfo other) {
            return false;
        }

        public boolean insert(int type, String verifyStr) {
            return false;
        }

        public void clear() {
        }

        public void dump() {
        }

        public long getVersion() {
            return this.mVersion;
        }

        public boolean updateToLowerVersion(String newContent) {
            return false;
        }
    }

    public RomUpdateHelper(Context context, String filterName, String systemFile, String dataFile) {
        this.mContext = context;
        this.mFilterName = filterName;
        this.mSystemFilePath = systemFile;
        this.mDataFilePath = dataFile;
    }

    public void init() {
        if (this.mDataFilePath != null && this.mSystemFilePath != null) {
            File file = new File(this.mDataFilePath);
            if (file.exists()) {
                parseContentFromXML(readFromFile(file));
            }
            file = new File(this.mSystemFilePath);
            if (file.exists()) {
                parseContentFromXML(readFromFile(file));
            }
        }
    }

    protected void setUpdateInfo(UpdateInfo updateInfo1, UpdateInfo updateInfo2) {
        this.mUpdateInfo1 = updateInfo1;
        this.mUpdateInfo2 = updateInfo2;
    }

    public void initUpdateBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (RomUpdateHelper.DEBUG) {
                    Log.d(RomUpdateHelper.TAG, this + ", " + RomUpdateHelper.this.getFilterName() + ", onReceive intent = " + intent);
                }
                if (intent != null) {
                    ArrayList<String> tmp = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                    if (tmp != null && tmp.contains(RomUpdateHelper.this.mFilterName)) {
                        RomUpdateHelper.this.getUpdateFromProvider();
                    }
                }
            }
        }, filter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
    }

    protected UpdateInfo getUpdateInfo(boolean b) {
        if (b) {
            return this.mWhich2Use ? this.mUpdateInfo1 : this.mUpdateInfo2;
        }
        return this.mWhich2Use ? this.mUpdateInfo2 : this.mUpdateInfo1;
    }

    private void setFlip() {
        this.mWhich2Use ^= 1;
    }

    private boolean saveToFile(String content, String filePath) {
        try {
            FileOutputStream outStream = new FileOutputStream(new File(filePath));
            outStream.write(content.getBytes());
            outStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getFilterName() {
        return this.mFilterName;
    }

    private String getDataFromProvider() {
        Cursor cursor = null;
        String returnStr = null;
        String[] projection = new String[]{"version", COLUMN_NAME_2};
        try {
            if (this.mContext == null) {
                return null;
            }
            cursor = this.mContext.getContentResolver().query(CONTENT_URI_WHITE_LIST, projection, "filtername=\"" + this.mFilterName + "\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                int configVersion = cursor.getInt(versioncolumnIndex);
                returnStr = cursor.getString(xmlcolumnIndex);
                Log.d(TAG, "White List updated, version = " + configVersion);
            }
            if (cursor != null) {
                cursor.close();
            }
            return returnStr;
        } catch (Exception e) {
            Log.w(TAG, "We can not get white list data from provider, because of " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0049 A:{SYNTHETIC, Splitter: B:33:0x0049} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x005a A:{SYNTHETIC, Splitter: B:41:0x005a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String readFromFile(File path) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        if (path == null) {
            return "";
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(is2));
                StringBuffer buffer = new StringBuffer();
                String str = "";
                while (true) {
                    str = in.readLine();
                    if (str == null) {
                        break;
                    }
                    buffer.append(str);
                }
                String stringBuffer = buffer.toString();
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return stringBuffer;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                is = is2;
            } catch (IOException e5) {
                e3 = e5;
                is = is2;
                e3.printStackTrace();
                if (is != null) {
                }
                return null;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            try {
                e2.printStackTrace();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (is != null) {
                }
                throw th;
            }
        } catch (IOException e7) {
            e322 = e7;
            e322.printStackTrace();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            return null;
        }
    }

    public void parseContentFromXML(String content) {
        if (getUpdateInfo(true) != null) {
            getUpdateInfo(true).parseContentFromXML(content);
        }
    }

    public void getUpdateFromProvider() {
        try {
            String tmp = getDataFromProvider();
            if (tmp != null && !updateToLowerVersion(tmp)) {
                saveToFile(tmp, this.mDataFilePath);
                if (getUpdateInfo(false) != null) {
                    getUpdateInfo(false).parseContentFromXML(tmp);
                    setFlip();
                    getUpdateInfo(false).clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean updateToLowerVersion(String newContent) {
        UpdateInfo updateInfo = getUpdateInfo(true);
        if (updateInfo == null || !updateInfo.updateToLowerVersion(newContent)) {
            return false;
        }
        Log.d(TAG, "updateToLowerVersion true, " + updateInfo.hashCode());
        return true;
    }

    protected boolean insertValueInList(int type, String verifyStr) {
        if (getUpdateInfo(false).clone(getUpdateInfo(true)) && getUpdateInfo(false).insert(type, verifyStr)) {
            setFlip();
            getUpdateInfo(false).clear();
            return true;
        }
        log("Failed to insert!");
        return false;
    }

    public void dump() {
        log("mWhich2Use = " + this.mWhich2Use);
        this.mUpdateInfo1.dump();
        this.mUpdateInfo2.dump();
    }

    public void log(String log) {
        if (DEBUG) {
            Log.d(TAG, log);
        }
    }

    public void log(String log, Exception e) {
        if (DEBUG) {
            Log.d(TAG, log);
        }
    }
}
