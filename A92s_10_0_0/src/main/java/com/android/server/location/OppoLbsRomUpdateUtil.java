package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.interfaces.IPswLbsRomUpdateUtil;
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
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class OppoLbsRomUpdateUtil implements IPswLbsRomUpdateUtil {
    private static final String ACTION_ROM_UPDATE_CONFIG = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String ACTION_TEST_LBS_RUS = "com.oppo.lbs.rus.test";
    private static final String ATTR_NAME = "name";
    private static final String COLUMN_NAME_VERSION = "version";
    private static final String COLUMN_NAME_XML = "xml";
    private static final String DATA_FILE_PATH = "/data/system/sys_gps_lbs_config.xml";
    private static final String EXTRA_ROM_UPDATE_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String FILE_NAME = "sys_gps_lbs_config";
    private static final Uri NEARME_ROM_UPDATE_URI = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String OPPO_LBS_CONFIG_UPDATE_ACTION = "com.android.location.oppo.lbsconfig.update.success";
    private static final String SYS_FILE_DIR = "system/etc/sys_gps_lbs_config.xml";
    private static final String TAG = "OppoLbsRomUpdateUtil";
    private static final String TAG_BOOLEAN = "bool";
    private static final String TAG_INTEGER = "integer";
    private static final String TAG_INTEGER_ARRAY = "integer-array";
    private static final String TAG_ITEM = "item";
    private static final String TAG_STRING = "string";
    private static final String TAG_STRING_ARRAY = "string-array";
    private static final String TAG_VERSION = "version";
    private static OppoLbsRomUpdateUtil mInstall = null;
    /* access modifiers changed from: private */
    public static boolean mIsDebug = false;
    private HashMap<String, Boolean> mBooleanHash = new HashMap<>();
    private Context mContext = null;
    private ArrayList<Integer> mCurrIntegerList = null;
    private ArrayList<String> mCurrStringList = null;
    private HashMap<String, Float> mFloatHash = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> mIntArrayHash = new HashMap<>();
    private HashMap<String, Integer> mIntHash = new HashMap<>();
    private String mIntegerArrayAttName = null;
    private BroadcastReceiver mRomUpdateReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.OppoLbsRomUpdateUtil.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (OppoLbsRomUpdateUtil.ACTION_ROM_UPDATE_CONFIG.equals(action)) {
                List<String> list = intent.getStringArrayListExtra(OppoLbsRomUpdateUtil.EXTRA_ROM_UPDATE_LIST);
                if (list == null || list.isEmpty()) {
                    if (OppoLbsRomUpdateUtil.mIsDebug) {
                        Log.d(OppoLbsRomUpdateUtil.TAG, "Get the extend rom update list is null");
                    }
                } else if (list.contains(OppoLbsRomUpdateUtil.FILE_NAME)) {
                    OppoLbsRomUpdateUtil.this.loadXML();
                }
            } else if (OppoLbsRomUpdateUtil.mIsDebug && OppoLbsRomUpdateUtil.ACTION_TEST_LBS_RUS.equals(action)) {
                OppoLbsRomUpdateUtil.this.loadXML();
            }
        }
    };
    private HashMap<String, ArrayList<String>> mStrArrayHash = new HashMap<>();
    private String mStringArrayAttName = null;
    private HashMap<String, String> mStringHash = new HashMap<>();
    private int mVersion = 0;

    public static OppoLbsRomUpdateUtil getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new OppoLbsRomUpdateUtil(context);
        }
        return mInstall;
    }

    private OppoLbsRomUpdateUtil(Context context) {
        this.mContext = context;
        if (!initValueFromFile()) {
            initDefaultValues();
        }
        registBroadcast();
    }

    private boolean initValueFromFile() {
        File file = new File(DATA_FILE_PATH);
        if (!file.exists()) {
            file = new File(SYS_FILE_DIR);
        }
        String content = readFromFile(file);
        if (content != null) {
            return parseContentFromXML(content);
        }
        return false;
    }

    public static void setDebug(boolean isDebug) {
        mIsDebug = isDebug;
    }

    private void initDefaultValues() {
        this.mVersion = 20190118;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public ArrayList<String> getStringArray(String key) {
        return this.mStrArrayHash.get(key);
    }

    public ArrayList<ArrayList<String>> getMatchStringArray(String key) {
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        for (String matchKey : this.mStrArrayHash.keySet()) {
            Log.d(TAG, "match Key " + matchKey + ", " + matchKey.startsWith(key));
            if (matchKey.startsWith(key)) {
                list.add(this.mStrArrayHash.get(matchKey));
            }
        }
        return list;
    }

    public ArrayList<Integer> getIntegerArray(String key) {
        return this.mIntArrayHash.get(key);
    }

    public String getString(String key) {
        return this.mStringHash.get(key);
    }

    public int getInt(String key) {
        Integer values = this.mIntHash.get(key);
        if (values != null) {
            return values.intValue();
        }
        return -1;
    }

    public float getFloat(String key) {
        Float values = this.mFloatHash.get(key);
        if (values != null) {
            return values.floatValue();
        }
        return -1.0f;
    }

    public boolean getBoolean(String key) {
        Boolean values = this.mBooleanHash.get(key);
        if (values != null) {
            return values.booleanValue();
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005f, code lost:
        if (r11 == null) goto L_0x0065;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0061, code lost:
        r11.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0065, code lost:
        if (r10 == 0) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0067, code lost:
        if (r9 != null) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        parseContentFromXML(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006f, code lost:
        android.util.Log.e(com.android.server.location.OppoLbsRomUpdateUtil.TAG, "Parsing the xml content error!!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007c, code lost:
        if (com.android.server.location.OppoLbsRomUpdateUtil.mIsDebug == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x007e, code lost:
        android.util.Log.d(com.android.server.location.OppoLbsRomUpdateUtil.TAG, "Get the xml content is wrong!!!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0053, code lost:
        if (r11 != null) goto L_0x0061;
     */
    public void loadXML() {
        String xml = null;
        int version = 0;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(NEARME_ROM_UPDATE_URI, new String[]{"version", COLUMN_NAME_XML}, "filtername=\"sys_gps_lbs_config\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_XML);
                cursor.moveToNext();
                version = cursor.getInt(versioncolumnIndex);
                xml = cursor.getString(xmlcolumnIndex);
                if (mIsDebug) {
                    Log.d(TAG, "White List updated, version = " + version);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Get xml from database fail!!");
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        saveToFile(xml, DATA_FILE_PATH);
    }

    private boolean saveToFile(String content, String filePath) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(new File(filePath));
            outStream.write(content.getBytes());
            outStream.close();
            try {
                outStream.close();
            } catch (Exception e) {
                Log.e(TAG, "saveToFile outStream close error!!");
            }
            return true;
        } catch (Exception e2) {
            e2.printStackTrace();
            if (outStream == null) {
                return false;
            }
            try {
                outStream.close();
                return false;
            } catch (Exception e3) {
                Log.e(TAG, "saveToFile outStream close error!!");
                return false;
            }
        } catch (Throwable th) {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e4) {
                    Log.e(TAG, "saveToFile outStream close error!!");
                }
            }
            throw th;
        }
    }

    public String readFromFile(File path) {
        if (path == null) {
            return null;
        }
        InputStream inputStrm = null;
        try {
            InputStream inputStrm2 = new FileInputStream(path);
            BufferedReader inputRdr = new BufferedReader(new InputStreamReader(inputStrm2));
            StringBuffer buffer = new StringBuffer();
            while (true) {
                String line = inputRdr.readLine();
                if (line == null) {
                    break;
                }
                buffer.append(line + StringUtils.LF);
            }
            String stringBuffer = buffer.toString();
            try {
                inputStrm2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuffer;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (inputStrm != null) {
                inputStrm.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (inputStrm != null) {
                try {
                    inputStrm.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inputStrm != null) {
                try {
                    inputStrm.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
        return null;
    }

    private boolean parseContentFromXML(String content) {
        if (content == null) {
            Log.e(TAG, "parse content is null");
            return false;
        }
        int version = 0;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            if (!TextUtils.isEmpty(content)) {
                parser.setInput(new StringReader(content));
            }
            parser.nextTag();
            int eventType = parser.getEventType();
            while (true) {
                if (eventType == 1) {
                    break;
                }
                String tagName = parser.getName();
                if (2 == eventType) {
                    if (tagName.equals("version")) {
                        try {
                            version = Integer.valueOf(parser.nextText()).intValue();
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                        }
                        if (this.mVersion > version) {
                            Log.e(TAG, "Version is old, Don't need update anything from the xml file!");
                            break;
                        }
                        this.mVersion = version;
                    } else if (tagName.equals(TAG_STRING_ARRAY)) {
                        this.mStringArrayAttName = parser.getAttributeValue(null, ATTR_NAME);
                        this.mCurrStringList = new ArrayList<>();
                    } else if (tagName.equals(TAG_INTEGER_ARRAY)) {
                        this.mIntegerArrayAttName = parser.getAttributeValue(null, ATTR_NAME);
                        this.mCurrIntegerList = new ArrayList<>();
                    } else if (tagName.equals(TAG_ITEM)) {
                        if (this.mCurrStringList != null) {
                            this.mCurrStringList.add(parser.nextText());
                        } else if (this.mCurrIntegerList != null) {
                            this.mCurrIntegerList.add(Integer.valueOf(parser.nextText()));
                        } else {
                            this.mFloatHash.put(parser.getAttributeValue(null, ATTR_NAME), new Float(parser.nextText()));
                        }
                    } else if (tagName.equals(TAG_STRING)) {
                        this.mStringHash.put(parser.getAttributeValue(null, ATTR_NAME), parser.nextText());
                    } else if (tagName.equals(TAG_INTEGER)) {
                        try {
                            this.mIntHash.put(parser.getAttributeValue(null, ATTR_NAME), Integer.valueOf(parser.nextText()));
                        } catch (NumberFormatException e2) {
                            Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                        }
                    } else if (tagName.equals(TAG_BOOLEAN)) {
                        try {
                            this.mBooleanHash.put(parser.getAttributeValue(null, ATTR_NAME), Boolean.valueOf(Boolean.parseBoolean(parser.nextText())));
                        } catch (NumberFormatException e3) {
                            Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                        }
                    }
                } else if (3 == eventType) {
                    if (tagName.equals(TAG_STRING_ARRAY)) {
                        if (!(this.mStringArrayAttName == null || this.mCurrStringList == null)) {
                            this.mStrArrayHash.put(this.mStringArrayAttName, this.mCurrStringList);
                            this.mCurrStringList = null;
                            this.mStringArrayAttName = null;
                        }
                    } else if (!(!tagName.equals(TAG_INTEGER_ARRAY) || this.mIntegerArrayAttName == null || this.mCurrIntegerList == null)) {
                        this.mIntArrayHash.put(this.mIntegerArrayAttName, this.mCurrIntegerList);
                        this.mCurrIntegerList = null;
                        this.mIntegerArrayAttName = null;
                    }
                }
                eventType = parser.next();
            }
            if (mIsDebug) {
                Log.d(TAG, "Parse gnss content done!");
            }
            hasRomUpdated();
            return true;
        } catch (XmlPullParserException e4) {
            Log.e(TAG, "Got XmlPullParser exception parsing!");
            if (mIsDebug) {
                Log.d(TAG, "Parse gnss content done!");
            }
            return false;
        } catch (IOException e5) {
            Log.e(TAG, "Got IO exception parsing!!");
            if (mIsDebug) {
                Log.d(TAG, "Parse gnss content done!");
            }
            return false;
        } catch (Throwable th) {
            if (mIsDebug) {
                Log.d(TAG, "Parse gnss content done!");
            }
            throw th;
        }
    }

    private void hasRomUpdated() {
        if (mIsDebug) {
            Log.d(TAG, "Will send udate broadcast!");
        }
        Intent intent = new Intent();
        intent.setAction(OPPO_LBS_CONFIG_UPDATE_ACTION);
        this.mContext.sendBroadcast(intent);
    }

    private void registBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ROM_UPDATE_CONFIG);
        intentFilter.addAction(ACTION_TEST_LBS_RUS);
        this.mContext.registerReceiver(this.mRomUpdateReceiver, intentFilter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
    }
}
