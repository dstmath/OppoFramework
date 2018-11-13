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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoProcessWhiteListUtils {
    private static final int MSG_UPDATE_CONFIG = 208;
    private static final String PROCESS_FILTER_LIST_XML = "data/system/oppoguardelf/sys_ams_processfilter_list.xml";
    private static final String TAG = "OppoProcessWhiteListUtils";
    private static final String TAG_FILTER_CLEAR_USER_DATA = "filter_clear_data";
    private static final String TAG_FORBID_CLEAR_USER_DATA = "support_clear_data";
    private static OppoProcessWhiteListUtils mFileUtils;
    private List<String> mAuthorizedProcessList;
    private Object mAuthorizedProcessListLock;
    private List<String> mClearDataWhiteList;
    private FileObserverUtil mFileObserverUtil;
    private List<String> mProcessWhiteList;
    private Object mProcessWhiteListLock;
    private String mSupportClearSystemData;

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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoProcessWhiteListUtils.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoProcessWhiteListUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoProcessWhiteListUtils.<clinit>():void");
    }

    public OppoProcessWhiteListUtils() {
        this.mProcessWhiteList = new ArrayList();
        this.mAuthorizedProcessList = new ArrayList();
        this.mFileObserverUtil = null;
        this.mClearDataWhiteList = new ArrayList();
        this.mSupportClearSystemData = "false";
        this.mProcessWhiteListLock = new Object();
        this.mAuthorizedProcessListLock = new Object();
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
