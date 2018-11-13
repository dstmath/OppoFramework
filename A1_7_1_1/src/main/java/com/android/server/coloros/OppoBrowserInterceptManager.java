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
public class OppoBrowserInterceptManager {
    private static final String INTERCEPT_MANGER_PATH = "/data/oppo/boot";
    private static final String INTERCEPT_WHITE_FILE = "/data/oppo/boot/browserWhiteList.txt";
    private static final String TAG = "OppoBrowserInterceptManager";
    private static OppoBrowserInterceptManager mBrowserIntercept;
    private boolean DEBUG_SWITCH;
    private final List<String> mDefaultWhiteList;
    private FileObserverPolicy mInterceptFileObserver;
    private List<String> mInterceptWhiteList;
    private final Object mInterceptWhiteListLock;

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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.coloros.OppoBrowserInterceptManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.coloros.OppoBrowserInterceptManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.coloros.OppoBrowserInterceptManager.<clinit>():void");
    }

    private OppoBrowserInterceptManager() {
        this.DEBUG_SWITCH = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mInterceptWhiteListLock = new Object();
        this.mInterceptWhiteList = new ArrayList();
        this.mInterceptFileObserver = null;
        String[] strArr = new String[11];
        strArr[0] = "com.sohu.inputmethod.sogou";
        strArr[1] = "com.oppo.mp_battery_autotest";
        strArr[2] = "com.oppo.autotest.agingautotesttool";
        strArr[3] = "com.oppo.PhenixTestServer";
        strArr[4] = "com.oppo.networkautotest";
        strArr[5] = "com.oppo.community";
        strArr[6] = "com.nearme.note";
        strArr[7] = "com.tencent.tvoem";
        strArr[8] = "com.oppo.ubeauty";
        strArr[9] = "com.android.email";
        strArr[10] = "com.facebook.orca";
        this.mDefaultWhiteList = Arrays.asList(strArr);
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
