package com.android.server.coloros;

import android.os.FileObserver;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OppoBrowserInterceptManager {
    private static final String INTERCEPT_MANGER_PATH = "/data/oppo/coloros/startup";
    private static final String INTERCEPT_WHITE_FILE = "/data/oppo/coloros/startup/browserWhiteList.txt";
    private static final String TAG = "OppoBrowserInterceptManager";
    private static OppoBrowserInterceptManager mBrowserIntercept = null;
    private boolean DEBUG_SWITCH = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private final List<String> mDefaultWhiteList = Arrays.asList(new String[]{"com.sohu.inputmethod.sogou", "com.oppo.mp_battery_autotest", "com.oppo.autotest.agingautotesttool", "com.oppo.PhenixTestServer", "com.oppo.networkautotest", "com.oppo.community", "com.nearme.note", "com.tencent.tvoem", "com.oppo.ubeauty", "com.android.email", "com.facebook.orca"});
    private FileObserverPolicy mInterceptFileObserver = null;
    private List<String> mInterceptWhiteList = new ArrayList();
    private final Object mInterceptWhiteListLock = new Object();

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(OppoBrowserInterceptManager.INTERCEPT_WHITE_FILE)) {
                Log.i(OppoBrowserInterceptManager.TAG, "focusPath BROWSER_INTERCEPT_WHITE_FILE!");
                OppoBrowserInterceptManager.this.readWhiteListFile();
            }
        }
    }

    private OppoBrowserInterceptManager() {
        initDir();
        initFileObserver();
        readWhiteListFile();
    }

    public static OppoBrowserInterceptManager getInstance() {
        if (mBrowserIntercept == null) {
            mBrowserIntercept = new OppoBrowserInterceptManager();
        }
        return mBrowserIntercept;
    }

    public void init() {
    }

    private void initDir() {
        File interceptPath = new File(INTERCEPT_MANGER_PATH);
        File interceptWhiteFile = new File(INTERCEPT_WHITE_FILE);
        try {
            if (!interceptPath.exists()) {
                interceptPath.mkdirs();
            }
            if (!interceptWhiteFile.exists()) {
                interceptWhiteFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "initDir failed!!!");
            e.printStackTrace();
        }
    }

    private void initFileObserver() {
        this.mInterceptFileObserver = new FileObserverPolicy(INTERCEPT_WHITE_FILE);
        this.mInterceptFileObserver.startWatching();
    }

    public List<String> getBrowserInterceptWhiteList() {
        List<String> list;
        synchronized (this.mInterceptWhiteListLock) {
            list = this.mInterceptWhiteList;
        }
        return list;
    }

    public boolean isInWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mInterceptWhiteListLock) {
            result = !this.mInterceptWhiteList.contains(pkgName) ? this.mDefaultWhiteList.contains(pkgName) : true;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052 A:{SYNTHETIC, Splitter: B:23:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:75:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0090 A:{SYNTHETIC, Splitter: B:53:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0095  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readWhiteListFile() {
        Exception e;
        Throwable th;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readWhiteListFile start");
        }
        File interceptWhiteListFile = new File(INTERCEPT_WHITE_FILE);
        if (!interceptWhiteListFile.exists()) {
            Log.e(TAG, "whiteListFile isn't exist!");
        }
        boolean isException = false;
        List<String> allowList = new ArrayList();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(interceptWhiteListFile);
            try {
                BufferedReader reader = new BufferedReader(fr2);
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    } else if (!TextUtils.isEmpty(line)) {
                        allowList.add(line.trim());
                    }
                }
                if (fr2 != null) {
                    try {
                        fr2.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (null == null) {
                    synchronized (this.mInterceptWhiteListLock) {
                        this.mInterceptWhiteList.clear();
                        this.mInterceptWhiteList.addAll(allowList);
                    }
                }
            } catch (Exception e3) {
                e2 = e3;
                fr = fr2;
                isException = true;
                try {
                    e2.printStackTrace();
                    if (fr != null) {
                    }
                    if (1 != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (!isException) {
                        synchronized (this.mInterceptWhiteListLock) {
                            this.mInterceptWhiteList.clear();
                            this.mInterceptWhiteList.addAll(allowList);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fr = fr2;
                if (fr != null) {
                }
                if (isException) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e22 = e4;
            isException = true;
            e22.printStackTrace();
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            if (1 != null) {
                synchronized (this.mInterceptWhiteListLock) {
                    this.mInterceptWhiteList.clear();
                    this.mInterceptWhiteList.addAll(allowList);
                }
            }
        }
    }
}
