package com.android.server.am;

import android.os.FileObserver;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

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
public class OppoMultiAppManagerUtil {
    private static boolean DEBUG_MULTI_APP = false;
    private static final String OPPO_ALLOWED_APP_FILE = "/data/multiapp/oppo_allowed_app.xml";
    private static final List<String> OPPO_INIT_ALLOWED_APP = null;
    private static final List<String> OPPO_MULTIAPP_GMS_PACKAGE = null;
    private static final String OPPO_MULTI_APP_ALIAS_FILE = "/data/multiapp/oppo_multi_app_alias.xml";
    private static final String OPPO_MULTI_APP_CREATED_FILE = "/data/multiapp/oppo_multi_app.xml";
    private static final String TAG = null;
    private static final ReentrantLock mAllowedAppLock = null;
    private static OppoMultiAppManagerUtil mUtil;
    private List<String> mAllowedAppList;
    private FileObserverUtil mObserver;

    private class FileObserverUtil extends FileObserver {
        private String mFileName;

        public FileObserverUtil(String path) {
            super(path, 8);
            this.mFileName = path;
        }

        public void onEvent(int event, String path) {
            int e = event & 4095;
            switch (e) {
                case 8:
                    if (OppoMultiAppManagerUtil.DEBUG_MULTI_APP) {
                        Log.v(OppoMultiAppManagerUtil.TAG, "file is changing: " + this.mFileName + " event= " + e);
                    }
                    OppoMultiAppManagerUtil.this.readXmlFile(this.mFileName);
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.OppoMultiAppManagerUtil.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.OppoMultiAppManagerUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoMultiAppManagerUtil.<clinit>():void");
    }

    public static synchronized OppoMultiAppManagerUtil getInstance() {
        OppoMultiAppManagerUtil oppoMultiAppManagerUtil;
        synchronized (OppoMultiAppManagerUtil.class) {
            if (mUtil == null) {
                mUtil = new OppoMultiAppManagerUtil();
            }
            oppoMultiAppManagerUtil = mUtil;
        }
        return oppoMultiAppManagerUtil;
    }

    public void initMultiAppFiles() {
        initFile(OPPO_ALLOWED_APP_FILE);
        initFile(OPPO_MULTI_APP_CREATED_FILE);
        initFile(OPPO_MULTI_APP_ALIAS_FILE);
    }

    public boolean isGms(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        for (String name : OPPO_MULTIAPP_GMS_PACKAGE) {
            if (name.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMultiAllowedApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        if (this.mAllowedAppList.isEmpty() && OPPO_INIT_ALLOWED_APP.contains(pkgName)) {
            if (DEBUG_MULTI_APP) {
                Log.d(TAG, pkgName + " is default to be allowed app.");
            }
            return true;
        } else if (!this.mAllowedAppList.contains(pkgName)) {
            return false;
        } else {
            if (DEBUG_MULTI_APP) {
                Log.d(TAG, pkgName + " is allowed to be created as a cloned app.");
            }
            return true;
        }
    }

    private OppoMultiAppManagerUtil() {
        this.mAllowedAppList = new ArrayList();
        initFile(OPPO_ALLOWED_APP_FILE);
        readXmlFile(OPPO_ALLOWED_APP_FILE);
        initFileObserver(OPPO_ALLOWED_APP_FILE);
    }

    private void initFile(String fileName) {
        if (DEBUG_MULTI_APP) {
            Log.d(TAG, "initFile() " + fileName);
        }
        File file = new File(fileName);
        try {
            if (file.getParentFile().exists() || file.getParentFile().mkdir()) {
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        if (DEBUG_MULTI_APP) {
                            Log.e(TAG, "init file failed: file-" + fileName);
                        }
                        return;
                    } else if (fileName.equals(OPPO_ALLOWED_APP_FILE)) {
                        mAllowedAppLock.lock();
                        writeListToFile(OPPO_INIT_ALLOWED_APP, OPPO_ALLOWED_APP_FILE);
                        mAllowedAppLock.unlock();
                    }
                }
                changeModFile(fileName);
                return;
            }
            if (DEBUG_MULTI_APP) {
                Log.d(TAG, "initFile() " + fileName + " mkdir() failed");
            }
        } catch (IOException e) {
        } catch (Throwable th) {
            mAllowedAppLock.unlock();
        }
    }

    private void changeModFile(String fileName) {
        try {
            Runtime.getRuntime().exec("chmod 745 /data/multiapp");
            Runtime.getRuntime().exec("chmod 744 " + fileName);
        } catch (IOException e) {
            Log.w(TAG, " " + e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b3 A:{SYNTHETIC, Splitter: B:39:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0091 A:{SYNTHETIC, Splitter: B:33:0x0091} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00d6 A:{SYNTHETIC, Splitter: B:45:0x00d6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readXmlFile(String filePath) {
        Throwable th;
        if (filePath != null) {
            File multiAppFile = new File(filePath);
            List<String> tempList = new ArrayList();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(multiAppFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, "UTF-8");
                    while (true) {
                        int type = parser.next();
                        if (type == 2 && parser.getName().equals("allowedapp")) {
                            String pkgName = parser.nextText();
                            if (pkgName != null) {
                                tempList.add(pkgName);
                            }
                        }
                        if (type == 1) {
                            break;
                        }
                    }
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e);
                        }
                    }
                    stream = stream2;
                } catch (IOException e2) {
                    stream = stream2;
                    if (stream != null) {
                    }
                    if (filePath.equals(OPPO_ALLOWED_APP_FILE)) {
                    }
                } catch (XmlPullParserException e3) {
                    stream = stream2;
                    if (stream != null) {
                    }
                    if (filePath.equals(OPPO_ALLOWED_APP_FILE)) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    stream = stream2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e5);
                    }
                }
                if (filePath.equals(OPPO_ALLOWED_APP_FILE)) {
                }
            } catch (XmlPullParserException e6) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e52) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e52);
                    }
                }
                if (filePath.equals(OPPO_ALLOWED_APP_FILE)) {
                }
            } catch (Throwable th3) {
                th = th3;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e522) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e522);
                    }
                }
                throw th;
            }
            if (filePath.equals(OPPO_ALLOWED_APP_FILE)) {
                mAllowedAppLock.lock();
                try {
                    if (!this.mAllowedAppList.isEmpty()) {
                        this.mAllowedAppList.clear();
                    }
                    this.mAllowedAppList.addAll(tempList);
                } finally {
                    mAllowedAppLock.unlock();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0072 A:{SYNTHETIC, Splitter: B:27:0x0072} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x007e A:{SYNTHETIC, Splitter: B:33:0x007e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeListToFile(List<String> list, String filePath) {
        IOException ie;
        Throwable th;
        if (list != null && filePath != null) {
            FileOutputStream stream = null;
            try {
                FileOutputStream stream2 = new FileOutputStream(new File(filePath));
                try {
                    XmlSerializer out = Xml.newSerializer();
                    out.setOutput(stream2, "UTF-8");
                    out.startDocument(null, Boolean.valueOf(true));
                    out.startTag(null, "gs");
                    int N = list.size();
                    if (filePath.equals(OPPO_ALLOWED_APP_FILE)) {
                        for (int i = 0; i < N; i++) {
                            String pkg = (String) list.get(i);
                            if (pkg != null) {
                                out.startTag(null, "allowedapp");
                                out.text(pkg);
                                out.endTag(null, "allowedapp");
                            }
                        }
                    }
                    out.endTag(null, "gs");
                    out.endDocument();
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
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
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                ie = e4;
                ie.printStackTrace();
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
            }
        }
    }

    private void initFileObserver(String fileName) {
        if (fileName != null && fileName.equals(OPPO_ALLOWED_APP_FILE)) {
            this.mObserver = new FileObserverUtil(fileName);
            this.mObserver.startWatching();
        }
    }
}
