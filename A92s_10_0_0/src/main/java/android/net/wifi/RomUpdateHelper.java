package android.net.wifi;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RomUpdateHelper {
    public static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    /* access modifiers changed from: private */
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    public static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String TAG = "RomUpdateHelper";
    public Context mContext = null;
    private String mDataFilePath = "";
    /* access modifiers changed from: private */
    public String mFilterName = "";
    private String mSystemFilePath = "";
    private UpdateInfo mUpdateInfo1;
    private UpdateInfo mUpdateInfo2;
    private boolean which2use = true;

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
        String str = this.mDataFilePath;
        if (str != null && this.mSystemFilePath != null) {
            new File(str);
            File dataFile = new File(this.mDataFilePath);
            File systemFile = new File(this.mSystemFilePath);
            if (dataFile.exists() || systemFile.exists()) {
                parseContentFromXML(readFromFile(getVersion(readFromFile(dataFile)) > getVersion(readFromFile(systemFile)) ? dataFile : systemFile));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setUpdateInfo(UpdateInfo updateInfo1, UpdateInfo updateInfo2) {
        this.mUpdateInfo1 = updateInfo1;
        this.mUpdateInfo2 = updateInfo2;
    }

    public void initUpdateBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class android.net.wifi.RomUpdateHelper.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                ArrayList<String> tmp;
                if (RomUpdateHelper.DEBUG) {
                    Log.d(RomUpdateHelper.TAG, this + ", " + RomUpdateHelper.this.getFilterName() + ", onReceive intent = " + intent);
                }
                if (intent != null && (tmp = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST")) != null && tmp.contains(RomUpdateHelper.this.mFilterName)) {
                    RomUpdateHelper.this.getUpdateFromProvider();
                }
            }
        }, filter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
    }

    /* access modifiers changed from: protected */
    public UpdateInfo getUpdateInfo(boolean b) {
        return b ? this.which2use ? this.mUpdateInfo1 : this.mUpdateInfo2 : this.which2use ? this.mUpdateInfo2 : this.mUpdateInfo1;
    }

    private void setFlip() {
        this.which2use = !this.which2use;
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0072, code lost:
        if (r1 != null) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x008d, code lost:
        if (r1 == null) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x008f, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0093, code lost:
        return r3;
     */
    private String getDataFromProvider() {
        Cursor cursor = null;
        String returnStr = null;
        String[] projection = {"version", COLUMN_NAME_2};
        try {
            if (this.mContext == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = CONTENT_URI_WHITE_LIST;
            cursor = contentResolver.query(uri, projection, "filtername=\"" + this.mFilterName + "\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                int configVersion = cursor.getInt(versioncolumnIndex);
                returnStr = cursor.getString(xmlcolumnIndex);
                Log.d(TAG, "White List updated, version = " + configVersion);
            }
        } catch (Exception e) {
            Log.w(TAG, "We can not get white list data from provider, because of " + e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public String readFromFile(File path) {
        if (path == null || !path.exists()) {
            return "";
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            BufferedReader in = new BufferedReader(new InputStreamReader(is2));
            StringBuffer buffer = new StringBuffer();
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                buffer.append(line);
            }
            String stringBuffer = buffer.toString();
            try {
                is2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuffer;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (is == null) {
                return null;
            }
            is.close();
            return null;
        } catch (IOException e3) {
            e3.printStackTrace();
            if (is == null) {
                return null;
            }
            try {
                is.close();
                return null;
            } catch (IOException e4) {
                e4.printStackTrace();
                return null;
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0085, code lost:
        if (r6 == null) goto L_0x0088;
     */
    private long getVersion(String content) {
        if (content == null) {
            Log.d(TAG, "\tcontent is null");
            return -1;
        }
        StringReader strReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            strReader = new StringReader(content);
            parser.setInput(strReader);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String mTagName = parser.getName();
                        parser.next();
                        String mText = parser.getText();
                        if ("version".equals(mTagName) && mText != null) {
                            strReader.close();
                            Log.d(TAG, "fileVersion = " + mText);
                            long parseInt = (long) Integer.parseInt(mText);
                            strReader.close();
                            return parseInt;
                        }
                    }
                }
            }
        } catch (XmlPullParserException e) {
            log("Got execption parsing permissions.", e);
        } catch (IOException e2) {
            log("Got execption parsing permissions.", e2);
            if (strReader != null) {
            }
        } catch (Throwable th) {
            if (strReader != null) {
                strReader.close();
            }
            throw th;
        }
        strReader.close();
        return -1;
    }

    public void parseContentFromXML(String content) {
        if (getUpdateInfo(true) != null) {
            getUpdateInfo(true).parseContentFromXML(content);
        }
    }

    public void getUpdateFromProvider() {
        try {
            String tmp = getDataFromProvider();
            File dataFile = new File(this.mDataFilePath);
            File systemFile = new File(this.mSystemFilePath);
            if (tmp != null && !updateToLowerVersion(tmp)) {
                long tmpVersion = getVersion(tmp);
                if (tmpVersion > getVersion(readFromFile(dataFile))) {
                    if (tmpVersion > getVersion(readFromFile(systemFile))) {
                        saveToFile(tmp, this.mDataFilePath);
                        if (getUpdateInfo(false) != null) {
                            getUpdateInfo(false).parseContentFromXML(tmp);
                            setFlip();
                            getUpdateInfo(false).clear();
                            return;
                        }
                        return;
                    }
                }
                Log.d(TAG, "getUpdateFromProvider version is older than current file verison");
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

    /* access modifiers changed from: protected */
    public boolean insertValueInList(int type, String verifyStr) {
        if (!getUpdateInfo(false).clone(getUpdateInfo(true)) || !getUpdateInfo(false).insert(type, verifyStr)) {
            log("Failed to insert!");
            return false;
        }
        setFlip();
        getUpdateInfo(false).clear();
        return true;
    }

    public void dump() {
        log("which2use = " + this.which2use);
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
