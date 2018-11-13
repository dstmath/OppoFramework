package com.android.server.wm;

import android.os.FileObserver;
import android.util.Slog;
import android.util.Xml;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class OppoKeyguardListHelper {
    private static final String INTERCEPT_FEATURE = "open";
    private static final String OPPO_INTERCEPT_CONFIG = "/data/oppo/coloros/systemui/sys_keyguard_unlock_control_list.xml";
    private static final String OPPO_INTERCEPT_PATH = "/data/oppo/coloros/systemui";
    private static final String SKIP_NAME = "skip";
    private static final String TAG = "OppoKeyguardListHelper";
    private static final Object mLock = new Object();
    private static OppoKeyguardListHelper sInstance = null;
    private FileObserverPolicy mConfigFileObserver = null;
    private boolean mInterceptFeature = true;
    private List<String> mSkipApp = Arrays.asList(new String[]{"com.tencent.mobileqq", "com.tencent.mm", "com.tencent.tim", "com.sdu.didi.gsui", "com.sdu.didi.gui", "com.oppo.owallet"});
    protected List<String> mSkipNameList;

    private class FileObserverPolicy extends FileObserver {
        public FileObserverPolicy(String path) {
            super(path, 8);
        }

        public void onEvent(int event, String path) {
            Slog.i(OppoKeyguardListHelper.TAG, "onEvent: " + event);
            if (event == 8) {
                Slog.i(OppoKeyguardListHelper.TAG, "onEvent: focusPath = KEYGUARD_INTERCEPT_CONFIG");
                OppoKeyguardListHelper.this.readConfigFile();
            }
        }
    }

    public static OppoKeyguardListHelper getInstance() {
        OppoKeyguardListHelper oppoKeyguardListHelper;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new OppoKeyguardListHelper();
            }
            oppoKeyguardListHelper = sInstance;
        }
        return oppoKeyguardListHelper;
    }

    public List<String> getSkipNameList() {
        return this.mSkipNameList;
    }

    private OppoKeyguardListHelper() {
        initDir();
        readConfigFile();
        initFileObserver();
    }

    private void initDir() {
        File interceptPath = new File(OPPO_INTERCEPT_PATH);
        File interceptConfig = new File(OPPO_INTERCEPT_CONFIG);
        try {
            if (!interceptPath.exists()) {
                interceptPath.mkdirs();
            }
            if (!interceptConfig.exists()) {
                interceptConfig.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "init interceptConfig Dir failed!!!");
        }
    }

    private void initFileObserver() {
        this.mConfigFileObserver = new FileObserverPolicy(OPPO_INTERCEPT_CONFIG);
        this.mConfigFileObserver.startWatching();
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x00e9 A:{Catch:{ IOException -> 0x011a }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile() {
        Exception e;
        Throwable th;
        File xmlFile = new File(OPPO_INTERCEPT_CONFIG);
        if (xmlFile.exists()) {
            FileReader fileReader = null;
            try {
                this.mSkipNameList = new ArrayList();
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader = new FileReader(xmlFile);
                    try {
                        parser.setInput(xmlReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if (!parser.getName().equals(SKIP_NAME)) {
                                        if (parser.getName().equals(INTERCEPT_FEATURE)) {
                                            eventType = parser.next();
                                            updateFeature(parser.getAttributeValue(null, "att"));
                                            break;
                                        }
                                    }
                                    eventType = parser.next();
                                    updateListName(parser.getAttributeValue(null, "att"), this.mSkipNameList);
                                    break;
                                    break;
                            }
                        }
                        try {
                            if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                                this.mSkipNameList = this.mSkipApp;
                            }
                            if (xmlReader != null) {
                                xmlReader.close();
                            }
                        } catch (IOException e2) {
                            Slog.w(TAG, "Got execption close permReader.", e2);
                        }
                    } catch (Exception e3) {
                        e = e3;
                        fileReader = xmlReader;
                        try {
                            Slog.w(TAG, "Got execption parsing permissions.", e);
                            try {
                                if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                                    this.mSkipNameList = this.mSkipApp;
                                }
                                if (fileReader != null) {
                                    fileReader.close();
                                }
                            } catch (IOException e22) {
                                Slog.w(TAG, "Got execption close permReader.", e22);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            try {
                                if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                                    this.mSkipNameList = this.mSkipApp;
                                }
                                if (fileReader != null) {
                                    fileReader.close();
                                }
                            } catch (IOException e222) {
                                Slog.w(TAG, "Got execption close permReader.", e222);
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileReader = xmlReader;
                        this.mSkipNameList = this.mSkipApp;
                        if (fileReader != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Slog.w(TAG, "Couldn't find or open sys_keyguard_unlock_control_list file " + xmlFile);
                    try {
                        if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                            this.mSkipNameList = this.mSkipApp;
                        }
                    } catch (IOException e2222) {
                        Slog.w(TAG, "Got execption close permReader.", e2222);
                    }
                }
            } catch (Exception e5) {
                e = e5;
            }
        } else {
            this.mSkipNameList = this.mSkipApp;
            Slog.d(TAG, "xml file is not exist.");
        }
    }

    private void updateListName(String tagName, List<String> list) {
        if (tagName != null && tagName != "" && list != null) {
            list.add(tagName);
        }
    }

    private void updateFeature(String feature) {
        if (feature != null) {
            try {
                this.mInterceptFeature = Boolean.parseBoolean(feature);
            } catch (NumberFormatException e) {
                this.mInterceptFeature = true;
                Slog.e(TAG, "updateFeature NumberFormatException: ", e);
            }
        }
    }
}
