package android.hardware.sensor;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.oppo.OppoRomUpdateHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoSensorManagerHelper extends OppoRomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/sys_sensor_black_list.xml";
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String FILTER_NAME = "sys_sensor_black_list";
    private static final String SYS_FILE_DIR = "system/etc/sys_sensor_black_list.xml";
    static final String TAG = "OppoSensorManagerHelper";
    private static final String TAG_PKGNAME = "packagename";
    private static final String TAG_VERSION = "version";
    private Callback mDataChangeCallBack = null;
    private long mLastVersion = 0;

    public interface Callback {
        void onDataChange();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeFilePermisson(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            boolean result = file.setReadable(true, false);
            Slog.i(TAG, "setReadable result :" + result);
            return;
        }
        Log.i(TAG, "filename :" + filename + " is not exist");
    }

    private class OppoSensorManagerInfo extends OppoRomUpdateHelper.UpdateInfo {
        private ArrayList<String> mPackageNameBlackList = new ArrayList<>();

        public OppoSensorManagerInfo() {
            super();
        }

        @Override // com.oppo.OppoRomUpdateHelper.UpdateInfo
        public void parseContentFromXML(String content) {
            if (content != null) {
                OppoSensorManagerHelper.this.changeFilePermisson(OppoSensorManagerHelper.DATA_FILE_DIR);
                FileReader xmlReader = null;
                StringReader strReader = null;
                clearCache();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    parser.setInput(strReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                String tag = parser.getName();
                                if (OppoSensorManagerHelper.DEBUG) {
                                    Slog.d(OppoSensorManagerHelper.TAG, "initializing tag:" + tag);
                                }
                                if (OppoSensorManagerHelper.TAG_PKGNAME.equals(tag)) {
                                    parser.next();
                                    String info = parser.getText();
                                    if (OppoSensorManagerHelper.DEBUG) {
                                        Slog.d(OppoSensorManagerHelper.TAG, "initializing list info:" + info + " type:" + tag);
                                    }
                                    this.mPackageNameBlackList.add(info);
                                } else if ("version".equals(tag)) {
                                    parser.next();
                                    String info2 = parser.getText();
                                    Slog.d(OppoSensorManagerHelper.TAG, "verion :" + info2 + " type:" + tag);
                                    OppoSensorManagerHelper.this.mLastVersion = Long.parseLong(info2);
                                }
                            }
                        }
                    }
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e) {
                            Slog.i(OppoSensorManagerHelper.TAG, "Got execption close permReader.", e);
                        }
                    }
                    strReader2.close();
                    if (OppoSensorManagerHelper.DEBUG) {
                        Slog.d(OppoSensorManagerHelper.TAG, "load data end ");
                    }
                    OppoSensorManagerHelper.this.dataChange();
                } catch (XmlPullParserException e2) {
                    Slog.i(OppoSensorManagerHelper.TAG, "Got execption parsing permissions.", e2);
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e3) {
                            Slog.i(OppoSensorManagerHelper.TAG, "Got execption close permReader.", e3);
                            return;
                        }
                    }
                    if (0 != 0) {
                        strReader.close();
                    }
                } catch (IOException e4) {
                    Slog.i(OppoSensorManagerHelper.TAG, "Got execption parsing permissions.", e4);
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e5) {
                            Slog.i(OppoSensorManagerHelper.TAG, "Got execption close permReader.", e5);
                            return;
                        }
                    }
                    if (0 != 0) {
                        strReader.close();
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e6) {
                            Slog.i(OppoSensorManagerHelper.TAG, "Got execption close permReader.", e6);
                            throw th;
                        }
                    }
                    if (0 != 0) {
                        strReader.close();
                    }
                    throw th;
                }
            }
        }

        @Override // com.oppo.OppoRomUpdateHelper.UpdateInfo
        public boolean updateToLowerVersion(String newContent) {
            long dataversion = OppoSensorManagerHelper.this.getConfigVersion(newContent, false);
            Slog.d(OppoSensorManagerHelper.TAG, "dataversion =" + dataversion + ",version =" + OppoSensorManagerHelper.this.mLastVersion);
            if (dataversion > OppoSensorManagerHelper.this.mLastVersion) {
                return false;
            }
            Slog.d(OppoSensorManagerHelper.TAG, " data version is low! ");
            return true;
        }

        private void clearCache() {
            this.mPackageNameBlackList.clear();
        }

        public boolean onPackageNameBlackList(String pkg) {
            boolean isContain = false;
            if (pkg == null) {
                return false;
            }
            if (this.mPackageNameBlackList.contains(pkg)) {
                return true;
            }
            Iterator<String> it = this.mPackageNameBlackList.iterator();
            while (it.hasNext()) {
                if (pkg.indexOf(it.next()) != -1) {
                    isContain = true;
                }
            }
            return isContain;
        }
    }

    public OppoSensorManagerHelper(Context context, Callback callback) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mDataChangeCallBack = callback;
        setUpdateInfo(new OppoSensorManagerInfo(), new OppoSensorManagerInfo());
        try {
            init();
            changeFilePermisson(DATA_FILE_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.OppoRomUpdateHelper
    public void init() {
        File datafile = new File(DATA_FILE_DIR);
        File sysfile = new File(SYS_FILE_DIR);
        if (!datafile.exists()) {
            Slog.d(TAG, "datafile not exist try to load from system");
            parseContentFromXML(readFromFile(sysfile));
            return;
        }
        long dataversion = getConfigVersion(DATA_FILE_DIR, true);
        long sysversion = getConfigVersion(SYS_FILE_DIR, true);
        Slog.d(TAG, "dataversion:" + dataversion + " sysversion:" + sysversion);
        if (dataversion >= sysversion) {
            parseContentFromXML(readFromFile(datafile));
        } else {
            parseContentFromXML(readFromFile(sysfile));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getConfigVersion(String content, boolean isPath) {
        Reader xmlReader;
        if (content == null) {
            return 0;
        }
        Slog.d(TAG, "getConfigVersion content length:" + content.length() + SmsManager.REGEX_PREFIX_DELIMITER + isPath);
        long version = 0;
        if (isPath) {
            try {
                xmlReader = new FileReader(content);
            } catch (FileNotFoundException e) {
                return 0;
            }
        } else {
            xmlReader = new StringReader(content);
        }
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(xmlReader);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String tagName = parser.getName();
                        Slog.d(TAG, "getConfigVersion called  tagname:" + tagName);
                        if ("version".equals(tagName)) {
                            parser.next();
                            String text = parser.getText();
                            if (text.length() > 8) {
                                text = text.substring(0, 8);
                            }
                            try {
                                version = Long.parseLong(text);
                            } catch (NumberFormatException e2) {
                                Slog.e(TAG, "version convert fail");
                            }
                            try {
                                xmlReader.close();
                            } catch (IOException e3) {
                                Slog.e(TAG, "" + e3);
                            }
                            return version;
                        }
                    }
                }
            }
            try {
                xmlReader.close();
            } catch (IOException e4) {
                Slog.e(TAG, "" + e4);
            }
            return 0;
        } catch (XmlPullParserException e5) {
            Slog.e(TAG, "" + e5);
            try {
                xmlReader.close();
            } catch (IOException e6) {
                Slog.e(TAG, "" + e6);
            }
            return 0;
        } catch (Exception e7) {
            Slog.e(TAG, "" + e7);
            try {
                xmlReader.close();
            } catch (IOException e8) {
                Slog.e(TAG, "" + e8);
            }
            return 0;
        } catch (Throwable th) {
            try {
                xmlReader.close();
            } catch (IOException e9) {
                Slog.e(TAG, "" + e9);
            }
            throw th;
        }
    }

    public boolean onPackageNameBlackList(String pkg) {
        return ((OppoSensorManagerInfo) getUpdateInfo(true)).onPackageNameBlackList(pkg);
    }

    /* access modifiers changed from: package-private */
    public void dataChange() {
        if (DEBUG) {
            Slog.d(TAG, " dataChange callback = " + this.mDataChangeCallBack);
        }
        Callback callback = this.mDataChangeCallBack;
        if (callback != null) {
            callback.onDataChange();
        }
    }

    /* access modifiers changed from: package-private */
    public void setCallback(Callback callback) {
        this.mDataChangeCallBack = callback;
    }
}
