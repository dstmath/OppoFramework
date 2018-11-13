package com.android.server.am;

import android.os.FileObserver;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoProcessWhiteListUtils {
    private static final int MSG_UPDATE_CONFIG = 208;
    private static final String PROCESS_FILTER_LIST_XML = "data/oppo/coloros/oppoguardelf/sys_ams_processfilter_list.xml";
    private static final String TAG = "OppoProcessWhiteListUtils";
    private static final String TAG_FILTER_CLEAR_USER_DATA = "filter_clear_data";
    private static final String TAG_FORBID_CLEAR_USER_DATA = "support_clear_data";
    private static OppoProcessWhiteListUtils mFileUtils = null;
    private List<String> mAuthorizedProcessList = new ArrayList();
    private Object mAuthorizedProcessListLock = new Object();
    private List<String> mClearDataWhiteList = new ArrayList();
    private FileObserverUtil mFileObserverUtil = null;
    private List<String> mProcessWhiteList = new ArrayList();
    private Object mProcessWhiteListLock = new Object();
    private String mSupportClearSystemData = "false";

    public class FileObserverUtil extends FileObserver {
        public FileObserverUtil(String path) {
            super(path, 2);
        }

        public void onEvent(int event, String path) {
            if (event == 2) {
                Log.v(OppoProcessWhiteListUtils.TAG, "file is changing: " + path);
                OppoProcessWhiteListUtils.this.readXmlFile();
            }
        }
    }

    public OppoProcessWhiteListUtils() {
        initDir();
        initDefaultWhiteList();
        readXmlFile();
        initFileObserver();
    }

    public static OppoProcessWhiteListUtils getInstance() {
        if (mFileUtils == null) {
            mFileUtils = new OppoProcessWhiteListUtils();
        }
        return mFileUtils;
    }

    public boolean isClearDataWhiteApp(String name) {
        if (this.mClearDataWhiteList.size() < 1) {
            return false;
        }
        return this.mClearDataWhiteList.contains(name);
    }

    public boolean isSupportClearSystemAppData() {
        return "true".equals(this.mSupportClearSystemData);
    }

    public void initDir() {
        File processWhiteListFile = new File(PROCESS_FILTER_LIST_XML);
        try {
            if (!processWhiteListFile.exists()) {
                processWhiteListFile.createNewFile();
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void initDefaultWhiteList() {
        synchronized (this.mProcessWhiteListLock) {
            this.mProcessWhiteList.add("com.coloros.sau");
            this.mProcessWhiteList.add("com.oppo.ota");
            this.mProcessWhiteList.add("com.nearme.romupdate");
            this.mProcessWhiteList.add("com.oppo.im");
            this.mProcessWhiteList.add("com.oppo.mo");
        }
        synchronized (this.mAuthorizedProcessListLock) {
            this.mAuthorizedProcessList.add("com.nearme.sync");
            this.mAuthorizedProcessList.add("com.android.cts.security");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:90:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:90:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00e5 A:{SYNTHETIC, Splitter: B:50:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:90:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x014a A:{SYNTHETIC, Splitter: B:65:0x014a} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:90:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0190 A:{SYNTHETIC, Splitter: B:73:0x0190} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readXmlFile() {
        IOException ie;
        Throwable th;
        XmlPullParserException xe;
        File processWhiteListFile = new File(PROCESS_FILTER_LIST_XML);
        List<String> processWhiteList = new ArrayList();
        List<String> authorizedProcessList = new ArrayList();
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(processWhiteListFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, "UTF-8");
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String packageName;
                        String tagName = parser.getName();
                        if (tagName.equals(OppoCrashClearManager.CRASH_CLEAR_NAME)) {
                            packageName = parser.getAttributeValue(null, "att");
                            if (packageName != null) {
                                processWhiteList.add(packageName);
                            }
                        }
                        if (tagName.equals("q")) {
                            packageName = parser.getAttributeValue(null, "att");
                            if (packageName != null) {
                                authorizedProcessList.add(packageName);
                            }
                        } else if (tagName.equals(TAG_FORBID_CLEAR_USER_DATA)) {
                            String supportClear = parser.getAttributeValue(null, "att");
                            if (supportClear != null) {
                                Log.d(TAG, "supportClear = " + supportClear);
                                this.mSupportClearSystemData = supportClear;
                            }
                        } else if (tagName.equals(TAG_FILTER_CLEAR_USER_DATA)) {
                            String clearName = parser.getAttributeValue(null, "att");
                            if (clearName != null) {
                                Log.d(TAG, "clearName = " + clearName);
                                this.mClearDataWhiteList.add(clearName);
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e);
                    }
                }
                stream = stream2;
            } catch (IOException e2) {
                ie = e2;
                stream = stream2;
                try {
                    ie.printStackTrace();
                    if (stream != null) {
                    }
                    if (!processWhiteList.isEmpty()) {
                    }
                    if (authorizedProcessList.isEmpty()) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (XmlPullParserException e3) {
                xe = e3;
                stream = stream2;
                xe.printStackTrace();
                if (stream != null) {
                }
                if (processWhiteList.isEmpty()) {
                }
                if (authorizedProcessList.isEmpty()) {
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e4);
                    }
                }
                throw th;
            }
        } catch (IOException e5) {
            ie = e5;
            ie.printStackTrace();
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e42) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e42);
                }
            }
            if (processWhiteList.isEmpty()) {
            }
            if (authorizedProcessList.isEmpty()) {
            }
        } catch (XmlPullParserException e6) {
            xe = e6;
            xe.printStackTrace();
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e422) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e422);
                }
            }
            if (processWhiteList.isEmpty()) {
            }
            if (authorizedProcessList.isEmpty()) {
            }
        }
        if (processWhiteList.isEmpty()) {
            synchronized (this.mProcessWhiteListLock) {
                this.mProcessWhiteList.clear();
                this.mProcessWhiteList.addAll(processWhiteList);
            }
            processWhiteList.clear();
        }
        if (authorizedProcessList.isEmpty()) {
            synchronized (this.mAuthorizedProcessListLock) {
                this.mAuthorizedProcessList.clear();
                this.mAuthorizedProcessList.addAll(authorizedProcessList);
            }
            authorizedProcessList.clear();
        }
    }

    public List<String> getProcessWhiteList() {
        List<String> list;
        synchronized (this.mProcessWhiteListLock) {
            list = this.mProcessWhiteList;
        }
        return list;
    }

    public List<String> getAuthorizedProcessList() {
        List<String> list;
        synchronized (this.mAuthorizedProcessListLock) {
            list = this.mAuthorizedProcessList;
        }
        return list;
    }

    public void initFileObserver() {
        this.mFileObserverUtil = new FileObserverUtil(PROCESS_FILTER_LIST_XML);
        this.mFileObserverUtil.startWatching();
    }
}
