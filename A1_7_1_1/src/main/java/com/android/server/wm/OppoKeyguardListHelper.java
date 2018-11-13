package com.android.server.wm;

import android.os.FileObserver;
import android.util.Slog;
import android.util.Xml;
import com.android.server.oppo.IElsaManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoKeyguardListHelper {
    private static final String INTERCEPT_FEATURE = "open";
    private static final String OPPO_INTERCEPT_CONFIG = "/data/system/config/sys_keyguard_unlock_control_list.xml";
    private static final String OPPO_INTERCEPT_PATH = "/data/system/config";
    private static final String SKIP_NAME = "skip";
    private static final String TAG = "OppoKeyguardListHelper";
    private static OppoKeyguardListHelper mInstance;
    private static final Object mLock = null;
    private FileObserverPolicy mConfigFileObserver;
    private boolean mInterceptFeature;
    protected List<String> mSkipNameList;
    private List<String> skipApp;

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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.OppoKeyguardListHelper.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.OppoKeyguardListHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.OppoKeyguardListHelper.<clinit>():void");
    }

    public static OppoKeyguardListHelper getInstance() {
        OppoKeyguardListHelper oppoKeyguardListHelper;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new OppoKeyguardListHelper();
            }
            oppoKeyguardListHelper = mInstance;
        }
        return oppoKeyguardListHelper;
    }

    public List<String> getSkipNameList() {
        return this.mSkipNameList;
    }

    private OppoKeyguardListHelper() {
        this.mConfigFileObserver = null;
        this.mInterceptFeature = true;
        String[] strArr = new String[6];
        strArr[0] = "com.tencent.mobileqq";
        strArr[1] = "com.tencent.mm";
        strArr[2] = "com.tencent.tim";
        strArr[3] = "com.sdu.didi.gsui";
        strArr[4] = "com.sdu.didi.gui";
        strArr[5] = "com.oppo.owallet";
        this.skipApp = Arrays.asList(strArr);
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
                                this.mSkipNameList = this.skipApp;
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
                                    this.mSkipNameList = this.skipApp;
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
                                    this.mSkipNameList = this.skipApp;
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
                        this.mSkipNameList = this.skipApp;
                        if (fileReader != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Slog.w(TAG, "Couldn't find or open sys_keyguard_unlock_control_list file " + xmlFile);
                    try {
                        if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                            this.mSkipNameList = this.skipApp;
                        }
                    } catch (IOException e2222) {
                        Slog.w(TAG, "Got execption close permReader.", e2222);
                    }
                }
            } catch (Exception e5) {
                e = e5;
            }
        } else {
            this.mSkipNameList = this.skipApp;
            Slog.d(TAG, "xml file is not exist.");
        }
    }

    private void updateListName(String tagName, List<String> list) {
        if (tagName != null && tagName != IElsaManager.EMPTY_PACKAGE && list != null) {
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
