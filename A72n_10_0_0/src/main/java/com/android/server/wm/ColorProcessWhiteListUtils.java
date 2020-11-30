package com.android.server.wm;

import android.os.FileObserver;
import android.util.Log;
import android.util.Xml;
import com.android.server.am.ColorAppCrashClearManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorProcessWhiteListUtils {
    private static final String PROCESS_FILTER_LIST_XML = "data/oppo/coloros/oppoguardelf/sys_ams_processfilter_list.xml";
    private static final String TAG = "ColorProcessWhiteList";
    private static final String TAG_FILTER_CLEAR_USER_DATA = "filter_clear_data";
    private static final String TAG_FORBID_CLEAR_USER_DATA = "support_clear_data";
    private final List<String> mAuthorizedProcessList = new ArrayList();
    private final List<String> mClearDataWhiteList = new ArrayList();
    private FileObserverUtil mFileObserverUtil = null;
    private final List<String> mProcessWhiteList = new ArrayList();
    private String mSupportClearSystemData = "false";

    ColorProcessWhiteListUtils() {
        initDir();
        initDefaultWhiteList();
        readXmlFile();
        initFileObserver();
    }

    /* access modifiers changed from: private */
    public static class ColorProcessWhiteListUtilsInstance {
        private static final ColorProcessWhiteListUtils sInstance = new ColorProcessWhiteListUtils();

        private ColorProcessWhiteListUtilsInstance() {
        }
    }

    public static ColorProcessWhiteListUtils getInstance() {
        return ColorProcessWhiteListUtilsInstance.sInstance;
    }

    public boolean isClearDataWhiteApp(String name) {
        return !this.mClearDataWhiteList.isEmpty() && this.mClearDataWhiteList.contains(name);
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
        synchronized (this.mProcessWhiteList) {
            this.mProcessWhiteList.add("com.coloros.sau");
            this.mProcessWhiteList.add("com.oppo.ota");
            this.mProcessWhiteList.add("com.nearme.romupdate");
            this.mProcessWhiteList.add("com.oppo.im");
            this.mProcessWhiteList.add("com.oppo.mo");
        }
        synchronized (this.mAuthorizedProcessList) {
            this.mAuthorizedProcessList.add("com.nearme.sync");
            this.mAuthorizedProcessList.add("com.android.cts.security");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x010f  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private void readXmlFile() {
        StringBuilder sb;
        int type;
        String clearName;
        File processWhiteListFile = new File(PROCESS_FILTER_LIST_XML);
        List<String> processWhiteList = new ArrayList<>();
        List<String> authorizedProcessList = new ArrayList<>();
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(processWhiteListFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, "UTF-8");
            do {
                type = parser.next();
                if (type == 2) {
                    String tagName = parser.getName();
                    if (tagName.equals(ColorAppCrashClearManager.CRASH_CLEAR_NAME)) {
                        String packageName = parser.getAttributeValue(null, "att");
                        if (packageName != null) {
                            processWhiteList.add(packageName);
                        }
                    } else if (tagName.equals("q")) {
                        String packageName2 = parser.getAttributeValue(null, "att");
                        if (packageName2 != null) {
                            authorizedProcessList.add(packageName2);
                        }
                    } else if (tagName.equals(TAG_FORBID_CLEAR_USER_DATA)) {
                        String supportClear = parser.getAttributeValue(null, "att");
                        if (supportClear != null) {
                            Log.d(TAG, "supportClear = " + supportClear);
                            this.mSupportClearSystemData = supportClear;
                        }
                    } else if (tagName.equals(TAG_FILTER_CLEAR_USER_DATA) && (clearName = parser.getAttributeValue(null, "att")) != null) {
                        Log.d(TAG, "clearName = " + clearName);
                        this.mClearDataWhiteList.add(clearName);
                    }
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (IOException ie) {
            ie.printStackTrace();
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e2) {
                    e = e2;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException xe) {
            xe.printStackTrace();
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e4);
                }
            }
            throw th;
        }
        if (!processWhiteList.isEmpty()) {
            synchronized (this.mProcessWhiteList) {
                this.mProcessWhiteList.clear();
                this.mProcessWhiteList.addAll(processWhiteList);
            }
            processWhiteList.clear();
        }
        if (authorizedProcessList.isEmpty()) {
            synchronized (this.mAuthorizedProcessList) {
                this.mAuthorizedProcessList.clear();
                this.mAuthorizedProcessList.addAll(authorizedProcessList);
            }
            authorizedProcessList.clear();
            return;
        }
        return;
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Log.e(TAG, sb.toString());
        if (!processWhiteList.isEmpty()) {
        }
        if (authorizedProcessList.isEmpty()) {
        }
    }

    public List<String> getProcessWhiteList() {
        ArrayList arrayList;
        synchronized (this.mProcessWhiteList) {
            arrayList = new ArrayList(this.mProcessWhiteList);
        }
        return arrayList;
    }

    public List<String> getAuthorizedProcessList() {
        ArrayList arrayList;
        synchronized (this.mAuthorizedProcessList) {
            arrayList = new ArrayList(this.mAuthorizedProcessList);
        }
        return arrayList;
    }

    public void initFileObserver() {
        this.mFileObserverUtil = new FileObserverUtil(PROCESS_FILTER_LIST_XML);
        this.mFileObserverUtil.startWatching();
    }

    public class FileObserverUtil extends FileObserver {
        FileObserverUtil(String path) {
            super(path, 2);
        }

        public void onEvent(int event, String path) {
            if (event == 2) {
                Log.v(ColorProcessWhiteListUtils.TAG, "file is changing: " + path);
                ColorProcessWhiteListUtils.this.readXmlFile();
            }
        }
    }
}
