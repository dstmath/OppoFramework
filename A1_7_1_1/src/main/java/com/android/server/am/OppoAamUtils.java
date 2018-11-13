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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

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
public class OppoAamUtils {
    private static final String NOT_RESTRICT_PATH = "/data/system/oppoguardelf/not_restrict.xml";
    private static final String OPPO_GUARD_ELF_PATH = "/data/system/oppoguardelf";
    private static final String PERSIST_RESTRICT_PATH = "/data/system/oppoguardelf/persist_restrict.xml";
    private static final String SCREENOFF_RESTRICT_PATH = "/data/system/oppoguardelf/screenoff_restrict.xml";
    private static final String STARTINFO_WHITE_PATH = "/data/system/oppoguardelf/startinfo_white.xml";
    private static final String TAG = null;
    private static OppoAamUtils mAamUtils;
    private boolean DEBUG_DETAIL;
    boolean DEBUG_SWITCH;
    private FileObserverPolicy mNotRestrictFileObserver;
    private FileObserverPolicy mScreenOffRestrictFileObserver;
    private FileObserverPolicy mStartInfoWhiteFileObserver;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (this.focusPath.equals(OppoAamUtils.SCREENOFF_RESTRICT_PATH)) {
                Log.i(OppoAamUtils.TAG, "onEvent: focusPath = SCREENOFF_RESTRICT_PATH");
                OppoAbnormalAppManager.getInstance().updateScreenOffRestrictedList(OppoAamUtils.this.readScreenOffResrictFile());
            } else if (this.focusPath.equals(OppoAamUtils.STARTINFO_WHITE_PATH)) {
                Log.i(OppoAamUtils.TAG, "onEvent: focusPath = STARTINFO_WHITE_PATH");
                OppoAbnormalAppManager.getInstance().updateStartInfoWhiteList(OppoAamUtils.this.readStartInfoWhiteFile());
            } else if (this.focusPath.equals(OppoAamUtils.NOT_RESTRICT_PATH)) {
                Log.i(OppoAamUtils.TAG, "onEvent: focusPath = NOT_RESTRICT_PATH");
                OppoAbnormalAppManager.getInstance().updateNotRestrictedList(OppoAamUtils.this.readNotRestrictFile());
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAamUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAamUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAamUtils.<clinit>():void");
    }

    private OppoAamUtils() {
        this.DEBUG_DETAIL = OppoAbnormalAppManager.DEBUG_DETAIL;
        this.DEBUG_SWITCH = this.DEBUG_DETAIL;
        this.mScreenOffRestrictFileObserver = null;
        this.mStartInfoWhiteFileObserver = null;
        this.mNotRestrictFileObserver = null;
        initDir();
        initFileObserver();
    }

    public static OppoAamUtils getInstance() {
        if (mAamUtils == null) {
            mAamUtils = new OppoAamUtils();
        }
        return mAamUtils;
    }

    private void initDir() {
        Log.i(TAG, "initDir start");
        File oppoGuardElfFilePath = new File(OPPO_GUARD_ELF_PATH);
        File screenOffRestrictFile = new File(SCREENOFF_RESTRICT_PATH);
        File startInfoWhiteFile = new File(STARTINFO_WHITE_PATH);
        File persistRestrictFile = new File(PERSIST_RESTRICT_PATH);
        File notRestrictFile = new File(NOT_RESTRICT_PATH);
        try {
            if (!oppoGuardElfFilePath.exists()) {
                oppoGuardElfFilePath.mkdirs();
            }
            if (!screenOffRestrictFile.exists()) {
                screenOffRestrictFile.createNewFile();
            }
            if (!startInfoWhiteFile.exists()) {
                startInfoWhiteFile.createNewFile();
            }
            if (!persistRestrictFile.exists()) {
                persistRestrictFile.createNewFile();
            }
            if (!notRestrictFile.exists()) {
                notRestrictFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "initDir failed!!!");
            e.printStackTrace();
        }
    }

    private void initFileObserver() {
        this.mScreenOffRestrictFileObserver = new FileObserverPolicy(SCREENOFF_RESTRICT_PATH);
        this.mScreenOffRestrictFileObserver.startWatching();
        this.mStartInfoWhiteFileObserver = new FileObserverPolicy(STARTINFO_WHITE_PATH);
        this.mStartInfoWhiteFileObserver.startWatching();
        this.mNotRestrictFileObserver = new FileObserverPolicy(NOT_RESTRICT_PATH);
        this.mNotRestrictFileObserver.startWatching();
    }

    private void ensureFileExist(File file, FileObserverPolicy fileObserver) {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fileObserver != null) {
                new FileObserverPolicy(SCREENOFF_RESTRICT_PATH).startWatching();
            }
        }
    }

    public List<String> readScreenOffResrictFile() {
        List<String> readFromFileLocked;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readScreenOffResrictFile start");
        }
        File screenOffResrictFile = new File(SCREENOFF_RESTRICT_PATH);
        synchronized (mAamUtils) {
            readFromFileLocked = readFromFileLocked(screenOffResrictFile);
        }
        return readFromFileLocked;
    }

    public void saveScreenOffResrictFile(List<String> list) {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "saveScreenOffResrictFile start");
        }
        File screenOffResrictFile = new File(SCREENOFF_RESTRICT_PATH);
        ensureFileExist(screenOffResrictFile, this.mScreenOffRestrictFileObserver);
        synchronized (mAamUtils) {
            writeToFileLocked(screenOffResrictFile, list);
        }
    }

    public List<String> readStartInfoWhiteFile() {
        List<String> readFromFileLocked;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readStartInfoWhiteFile start");
        }
        File startInfoWhiteFile = new File(STARTINFO_WHITE_PATH);
        synchronized (mAamUtils) {
            readFromFileLocked = readFromFileLocked(startInfoWhiteFile);
        }
        return readFromFileLocked;
    }

    public void saveStartInfoWhiteFile(List<String> list) {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "saveStartInfoWhiteFile start");
        }
        File startInfoWhiteFile = new File(STARTINFO_WHITE_PATH);
        ensureFileExist(startInfoWhiteFile, this.mStartInfoWhiteFileObserver);
        synchronized (mAamUtils) {
            writeToFileLocked(startInfoWhiteFile, list);
        }
    }

    public List<String> readPersistRestrictFile() {
        List<String> readFromFileLocked;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readPersistRestrictFile start");
        }
        File persistRestrictFile = new File(PERSIST_RESTRICT_PATH);
        synchronized (mAamUtils) {
            readFromFileLocked = readFromFileLocked(persistRestrictFile);
        }
        return readFromFileLocked;
    }

    public void savePersistRestrictFile(List<String> list) {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "savePersistRestrictFile start");
        }
        File persistRestrictFile = new File(PERSIST_RESTRICT_PATH);
        ensureFileExist(persistRestrictFile, null);
        synchronized (mAamUtils) {
            writeToFileLocked(persistRestrictFile, list);
        }
    }

    public List<String> readNotRestrictFile() {
        List<String> readFromFileLocked;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readNotRestrictFile start");
        }
        File file = new File(NOT_RESTRICT_PATH);
        synchronized (mAamUtils) {
            readFromFileLocked = readFromFileLocked(file);
        }
        return readFromFileLocked;
    }

    /* JADX WARNING: Removed duplicated region for block: B:72:0x015c A:{SYNTHETIC, Splitter: B:72:0x015c} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x013a A:{SYNTHETIC, Splitter: B:65:0x013a} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0110 A:{SYNTHETIC, Splitter: B:56:0x0110} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00e6 A:{SYNTHETIC, Splitter: B:47:0x00e6} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00bc A:{SYNTHETIC, Splitter: B:38:0x00bc} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0092 A:{SYNTHETIC, Splitter: B:29:0x0092} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<String> readFromFileLocked(File file) {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readFromFileLocked start");
        }
        FileInputStream stream = null;
        List<String> list = new ArrayList();
        try {
            FileInputStream stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                            String pkg = parser.getAttributeValue(null, "att");
                            if (pkg != null) {
                                list.add(pkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, "readFromFileLocked  pkg == " + pkg);
                                }
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e6) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6);
                    }
                }
                return list;
            } catch (NullPointerException e7) {
                e2 = e7;
                stream = stream2;
                Log.e(TAG, "failed parsing ", e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e62);
                    }
                }
                return list;
            } catch (NumberFormatException e8) {
                e3 = e8;
                stream = stream2;
                Log.e(TAG, "failed parsing ", e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e622);
                    }
                }
                return list;
            } catch (XmlPullParserException e9) {
                e4 = e9;
                stream = stream2;
                Log.e(TAG, "failed parsing ", e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6222);
                    }
                }
                return list;
            } catch (IOException e10) {
                e6222 = e10;
                stream = stream2;
                Log.e(TAG, "failed IOException ", e6222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e62222);
                    }
                }
                return list;
            } catch (IndexOutOfBoundsException e11) {
                e5 = e11;
                stream = stream2;
                try {
                    Log.e(TAG, "failed parsing ", e5);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e622222) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e622222);
                        }
                    }
                    return list;
                } catch (Throwable th) {
                    if (stream != null) {
                    }
                    return list;
                }
            } catch (Throwable th2) {
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6222222);
                    }
                }
                return list;
            }
        } catch (NullPointerException e12) {
            e2 = e12;
            Log.e(TAG, "failed parsing ", e2);
            if (stream != null) {
            }
            return list;
        } catch (NumberFormatException e13) {
            e3 = e13;
            Log.e(TAG, "failed parsing ", e3);
            if (stream != null) {
            }
            return list;
        } catch (XmlPullParserException e14) {
            e4 = e14;
            Log.e(TAG, "failed parsing ", e4);
            if (stream != null) {
            }
            return list;
        } catch (IOException e15) {
            e6222222 = e15;
            Log.e(TAG, "failed IOException ", e6222222);
            if (stream != null) {
            }
            return list;
        } catch (IndexOutOfBoundsException e16) {
            e5 = e16;
            Log.e(TAG, "failed parsing ", e5);
            if (stream != null) {
            }
            return list;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x009c A:{SYNTHETIC, Splitter: B:24:0x009c} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00be A:{SYNTHETIC, Splitter: B:30:0x00be} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeToFileLocked(File file, List<String> list) {
        IOException e;
        Throwable th;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "writeToFileLocked!!!");
        }
        FileOutputStream stream = null;
        try {
            FileOutputStream stream2 = new FileOutputStream(file);
            try {
                XmlSerializer out = Xml.newSerializer();
                out.setOutput(stream2, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "gs");
                int N = list.size();
                for (int i = 0; i < N; i++) {
                    String pkg = (String) list.get(i);
                    if (pkg != null) {
                        out.startTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                        out.attribute(null, "att", pkg);
                        out.endTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                    }
                }
                out.endTag(null, "gs");
                out.endDocument();
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e2);
                    }
                }
                stream = stream2;
            } catch (IOException e3) {
                e2 = e3;
                stream = stream2;
                try {
                    Log.e(TAG, "Failed to write IOException: " + e2);
                    if (stream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e22);
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
            e22 = e4;
            Log.e(TAG, "Failed to write IOException: " + e22);
            if (stream == null) {
                try {
                    stream.close();
                } catch (IOException e222) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e222);
                }
            }
        }
    }

    public void setDynamicDebugSwitch() {
        this.DEBUG_SWITCH = this.DEBUG_DETAIL | OppoAppStartupManager.getInstance().DynamicDebug;
    }
}
