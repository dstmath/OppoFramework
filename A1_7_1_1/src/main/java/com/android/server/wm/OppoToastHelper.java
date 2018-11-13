package com.android.server.wm;

import android.app.AppOpsManager;
import android.app.OppoWhiteListManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoCrashClearManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.xmlpull.v1.XmlPullParser;
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
public class OppoToastHelper {
    private static final boolean DEBUG = false;
    private static final String DEFAULT_SEITCH_OFF = "off";
    private static final String FEATURE_OPPO_CMCC_TEST = "oppo.system.cmcc.test";
    public static final String PKG_PERFIX_COLOR = "com.coloros.";
    public static final String PKG_PERFIX_NEARME = "com.nearme.";
    public static final String PKG_PERFIX_OPPO = "com.oppo.";
    private static final String STATIC_ACTION = "oppo.action.FLOAT_WINDOW_DATA_COLLECTION";
    private static final String TAG = "OppoToastHelper";
    private static final String TOAST_FILTER_FILE_PATH = "//data//toast//toast.xml";
    private static final String TOAST_WHITELIST_FILE_PATH = "//data//toast//float_window_white_list.xml";
    private static boolean defaultSwitch;
    private static HashSet<String> mDefaultOpenSet;
    private static boolean mIsCmccTestVersion;
    private static List<String> mListToastWindow;
    private static ReentrantLock mToastLock;
    private static final ToastWindowHandler mToastWindowHandler = null;
    private static final HandlerThread mToastWindowThread = null;
    private static WindowManagerService mWMService;
    private static DataFileListener toastAppListener;
    private static Map<String, String> toastAppMap;
    private static List<String> whiteList;
    private static DataFileListener whiteListListener;

    private static class DataFileListener extends FileObserver {
        String mObserverPath = null;

        public DataFileListener(String path) {
            super(path, DhcpPacket.MIN_PACKET_LENGTH_L3);
            this.mObserverPath = path;
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case 8:
                    if (this.mObserverPath != null) {
                        File file = new File(this.mObserverPath);
                        if (!file.exists()) {
                            if (!file.getParentFile().exists()) {
                                file.getParentFile().mkdirs();
                            }
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (this.mObserverPath.equals(OppoToastHelper.TOAST_FILTER_FILE_PATH)) {
                            OppoToastHelper.toastAppMap = OppoToastHelper.getToastAppMapPri();
                        } else if (this.mObserverPath.equals(OppoToastHelper.TOAST_WHITELIST_FILE_PATH)) {
                            OppoToastHelper.whiteList = OppoToastHelper.getToastWhiteListPri();
                            if (!(OppoToastHelper.toastAppMap == null || OppoToastHelper.toastAppMap.isEmpty())) {
                                Map<String, String> tmpToastAppMap = new HashMap();
                                tmpToastAppMap.putAll(OppoToastHelper.toastAppMap);
                                for (String pkgName : tmpToastAppMap.keySet()) {
                                    if (OppoToastHelper.whiteList.contains(pkgName)) {
                                        OppoToastHelper.toastAppMap.remove(pkgName);
                                    }
                                }
                                OppoToastHelper.setToastMap(OppoToastHelper.toastAppMap);
                            }
                        }
                        if (OppoToastHelper.mWMService != null) {
                            Log.v(OppoToastHelper.TAG, "mWMService.updateAppOpsState()");
                            OppoToastHelper.mWMService.updateAppOpsState();
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private static class ToastWindowHandler extends Handler {
        static final int MSG_CLOSE_TOAST_DATA = 103;

        public ToastWindowHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 103:
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        String packageName = bundle.getString("packageName");
                        if (!(packageName == null || OppoToastHelper.mWMService == null || OppoToastHelper.mWMService.mContext == null)) {
                            OppoToastHelper.mListToastWindow.add(packageName);
                            if (OppoToastHelper.mListToastWindow.size() >= 3) {
                                Intent intent = new Intent(OppoToastHelper.STATIC_ACTION);
                                intent.putStringArrayListExtra("pkgName", new ArrayList(OppoToastHelper.mListToastWindow));
                                OppoToastHelper.mWMService.mContext.sendBroadcast(intent);
                                OppoToastHelper.mListToastWindow.clear();
                                break;
                            }
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.wm.OppoToastHelper.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.wm.OppoToastHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.OppoToastHelper.<clinit>():void");
    }

    public static Map<String, String> getToastAppMap() {
        initObserver();
        return toastAppMap;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x006d A:{SYNTHETIC, Splitter: B:35:0x006d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Map<String, String> getToastAppMapPri() {
        Exception e;
        Throwable th;
        Map<String, String> resultMap = new HashMap();
        mToastLock.lock();
        FileInputStream inputStream = null;
        try {
            File file = new File(TOAST_FILTER_FILE_PATH);
            if (file.exists()) {
                FileInputStream inputStream2 = new FileInputStream(file);
                try {
                    resultMap = readToastFromXML(inputStream2);
                    inputStream = inputStream2;
                } catch (Exception e2) {
                    e = e2;
                    inputStream = inputStream2;
                    try {
                        Log.e(TAG, "getToastAppMap() error !");
                        e.printStackTrace();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        mToastLock.unlock();
                        return resultMap;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                        }
                        mToastLock.unlock();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = inputStream2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    mToastLock.unlock();
                    throw th;
                }
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e322) {
                e322.printStackTrace();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            mToastLock.unlock();
        } catch (Exception e4) {
            e = e4;
        }
        return resultMap;
    }

    private static Map<String, String> readToastFromXML(FileInputStream stream) {
        Map<String, String> toastMap = new HashMap();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            int type;
            do {
                type = parser.next();
                if (type == 2) {
                    if ("toast".equals(parser.getName())) {
                        Object obj = null;
                        Object text = null;
                        try {
                            obj = parser.getAttributeValue(null, "packagename");
                            text = parser.nextText();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!(obj == null || text == null)) {
                            toastMap.put(obj, text);
                        }
                    }
                }
            } while (type != 1);
        } catch (Exception e2) {
            Log.e(TAG, "readToastFromXML() error");
            e2.printStackTrace();
        }
        return toastMap;
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0078 A:{SYNTHETIC, Splitter: B:39:0x0078} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void setToastMap(Map<String, String> toastMap) {
        Exception e;
        Throwable th;
        if (toastMap == null || toastMap.isEmpty()) {
            Log.e(TAG, "setToastMap() empty map, return.");
            return;
        }
        mToastLock.lock();
        FileOutputStream outputStream = null;
        try {
            File file = new File(TOAST_FILTER_FILE_PATH);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    file.createNewFile();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            FileOutputStream outputStream2 = new FileOutputStream(file);
            try {
                writeToastToXML(outputStream2, toastMap);
                if (outputStream2 != null) {
                    try {
                        outputStream2.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
                mToastLock.unlock();
                outputStream = outputStream2;
            } catch (Exception e3) {
                e = e3;
                outputStream = outputStream2;
                try {
                    Log.e(TAG, "setToastMap() error!");
                    e.printStackTrace();
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    mToastLock.unlock();
                } catch (Throwable th2) {
                    th = th2;
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                    }
                    mToastLock.unlock();
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outputStream = outputStream2;
                if (outputStream != null) {
                }
                mToastLock.unlock();
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
        }
    }

    private static boolean writeToastToXML(FileOutputStream stream, Map<String, String> marketMap) {
        try {
            XmlSerializer out = Xml.newSerializer();
            out.setOutput(stream, "utf-8");
            out.startDocument(null, Boolean.valueOf(true));
            out.text("\r\n");
            out.startTag(null, "toastlist");
            for (String name : marketMap.keySet()) {
                String value = (String) marketMap.get(name);
                out.text("\r\n");
                out.text("\t");
                out.startTag(null, "toast");
                out.attribute(null, "packagename", name);
                out.text(value);
                out.endTag(null, "toast");
            }
            out.text("\r\n");
            out.endTag(null, "toastlist");
            out.text("\r\n");
            out.endDocument();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "writeToastToXML() error!");
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getToastWhiteList() {
        return whiteList;
    }

    private static List<String> getToastWhiteListPri() {
        Exception e;
        Throwable th;
        List<String> whiteList = new ArrayList();
        mToastLock.lock();
        File file = new File(TOAST_WHITELIST_FILE_PATH);
        FileInputStream fileInputStream = null;
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(inputStream, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            String tag = parser.getName();
                            String name;
                            if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(tag)) {
                                String switchStr = parser.getAttributeValue(null, "defaultswitch");
                                if (!(switchStr == null || switchStr.isEmpty())) {
                                    if (switchStr.equalsIgnoreCase(DEFAULT_SEITCH_OFF)) {
                                        defaultSwitch = false;
                                        continue;
                                    } else {
                                        defaultSwitch = true;
                                    }
                                }
                            } else if ("force_open".equals(tag)) {
                                name = parser.getAttributeValue(null, "name");
                                if (!(name == null || name.isEmpty())) {
                                    whiteList.add(name);
                                    continue;
                                }
                            } else if ("default_open".equals(tag)) {
                                name = parser.getAttributeValue(null, "name");
                                if (!(name == null || name.isEmpty())) {
                                    mDefaultOpenSet.add(name);
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        }
                    } while (type != 1);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    fileInputStream = inputStream;
                } catch (Exception e3) {
                    e = e3;
                    fileInputStream = inputStream;
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = inputStream;
                }
            } catch (Exception e4) {
                e = e4;
                try {
                    Log.e(TAG, "getToastWhiteList() error!");
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    mToastLock.unlock();
                    return whiteList;
                } catch (Throwable th3) {
                    th = th3;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e2222) {
            e2222.printStackTrace();
        }
        mToastLock.unlock();
        return whiteList;
    }

    public static boolean getDefaultSwitch() {
        if (mIsCmccTestVersion) {
            return true;
        }
        return defaultSwitch;
    }

    public static boolean skipPackage(Context context, String packageName) {
        boolean z = true;
        List<String> globalWhiteList = new OppoWhiteListManager(context).getGlobalWhiteList();
        if (globalWhiteList != null) {
            if (!(isOppoApp(packageName) || isSystemApp(context, packageName))) {
                z = globalWhiteList.contains(packageName);
            }
            return z;
        }
        if (!isOppoApp(packageName)) {
            z = isSystemApp(context, packageName);
        }
        return z;
    }

    private static boolean isOppoApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (packageName.startsWith(PKG_PERFIX_OPPO) || packageName.startsWith(PKG_PERFIX_COLOR) || packageName.startsWith(PKG_PERFIX_NEARME)) {
            return true;
        }
        return false;
    }

    private static boolean isSystemApp(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        boolean result;
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, packageName + " not found !");
            e.printStackTrace();
        }
        if ((info.flags & 1) != 0) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public static boolean shouldCloseToast(Context context, String packageName) {
        if (!SystemProperties.getBoolean("persist.sys.permission.enable", false) || skipPackage(context, packageName) || getToastWhiteList().contains(packageName)) {
            return false;
        }
        Map<String, String> toastMap = getToastAppMap();
        if (toastMap.containsKey(packageName)) {
            if (!((String) toastMap.get(packageName)).equals("0")) {
                return false;
            }
            Log.v(TAG, "package : " + packageName + " ; view : " + packageName);
            sendToastDataStatic(context, packageName);
            return true;
        } else if (getDefaultSwitch() || mDefaultOpenSet.contains(packageName) || hasAppOp(context, 24, packageName)) {
            toastMap.put(packageName, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            setToastMap(toastMap);
            return false;
        } else {
            toastMap.put(packageName, "0");
            setToastMap(toastMap);
            Log.v(TAG, "package : " + packageName + " ; view : " + packageName);
            sendToastDataStatic(context, packageName);
            return true;
        }
    }

    public static boolean isPackageToastClosed(String packageName) {
        if (whiteList != null && whiteList.contains(packageName)) {
            return false;
        }
        if (SystemProperties.getBoolean("persist.sys.permission.enable", false) && getToastAppMap().get(packageName) != null && ((String) getToastAppMap().get(packageName)).equals("0")) {
            return true;
        }
        return false;
    }

    public static void setWMService(WindowManagerService service) {
        if (mWMService == null) {
            mWMService = service;
            if (mWMService.mContext != null) {
                mIsCmccTestVersion = mWMService.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_CMCC_TEST);
            }
        }
    }

    private static void sendToastDataStatic(Context context, String packageName) {
        Message msg = mToastWindowHandler.obtainMessage(103);
        Bundle bundle = new Bundle();
        bundle.putString("packageName", packageName);
        msg.setData(bundle);
        msg.sendToTarget();
    }

    private static boolean hasAppOp(Context context, int appOp, String packageName) {
        try {
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService("appops");
            ApplicationInfo appInfo = null;
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 4096);
            if (packageInfo != null) {
                appInfo = packageInfo.applicationInfo;
            }
            if (appInfo != null) {
                return appOpsManager.checkOpNoThrow(appOp, appInfo.uid, packageName) == 0;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, "check op error!");
            return false;
        }
    }

    private static void initObserver() {
        try {
            if (whiteListListener == null || toastAppListener == null) {
                whiteListListener = new DataFileListener(TOAST_WHITELIST_FILE_PATH);
                toastAppListener = new DataFileListener(TOAST_FILTER_FILE_PATH);
                whiteListListener.startWatching();
                toastAppListener.startWatching();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
