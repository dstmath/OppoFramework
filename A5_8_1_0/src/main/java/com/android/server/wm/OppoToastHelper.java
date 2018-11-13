package com.android.server.wm;

import android.app.AppOpsManager;
import android.app.OppoWhiteListManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.dhcp.DhcpPacket;
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

public class OppoToastHelper {
    private static final boolean DEBUG = false;
    private static final String DEFAULT_SEITCH_OFF = "off";
    private static final String FEATURE_OPPO_CMCC_TEST = "oppo.system.cmcc.test";
    private static final int MIN_FLOATWINDOW_SIZE = 3;
    public static final String PKG_PERFIX_COLOR = "com.coloros.";
    public static final String PKG_PERFIX_NEARME = "com.nearme.";
    public static final String PKG_PERFIX_OPPO = "com.oppo.";
    private static final String STATIC_ACTION = "oppo.action.FLOAT_WINDOW_DATA_COLLECTION";
    private static final String TAG = "OppoToastHelper";
    private static final String TOAST_FILTER_FILE_PATH = "//data//oppo//coloros//toast//toast.xml";
    private static final String TOAST_WHITELIST_FILE_PATH = "//data//oppo/coloros//toast//float_window_white_list.xml";
    private static HashSet<String> sDefaultOpenSet = new HashSet();
    private static boolean sDefaultSwitch = false;
    private static boolean sIsCmccTestVersion = false;
    private static List<String> sListToastWindow = new ArrayList();
    private static DataFileListener sToastAppListener = null;
    private static Map<String, String> sToastAppMap;
    private static ReentrantLock sToastLock = new ReentrantLock();
    private static final ToastWindowHandler sToastWindowHandler = new ToastWindowHandler(sToastWindowThread.getLooper());
    private static final HandlerThread sToastWindowThread = new HandlerThread("ToastWindowThread");
    private static WindowManagerService sWMService;
    private static List<String> sWhiteList;
    private static DataFileListener sWhiteListListener = null;

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
                            OppoToastHelper.sToastAppMap = OppoToastHelper.getToastAppMapPri();
                        } else if (this.mObserverPath.equals(OppoToastHelper.TOAST_WHITELIST_FILE_PATH)) {
                            OppoToastHelper.sWhiteList = OppoToastHelper.getToastWhiteListPri();
                            if (!(OppoToastHelper.sToastAppMap == null || (OppoToastHelper.sToastAppMap.isEmpty() ^ 1) == 0)) {
                                Map<String, String> tmpToastAppMap = new HashMap();
                                tmpToastAppMap.putAll(OppoToastHelper.sToastAppMap);
                                for (String pkgName : tmpToastAppMap.keySet()) {
                                    if (OppoToastHelper.sWhiteList.contains(pkgName)) {
                                        OppoToastHelper.sToastAppMap.remove(pkgName);
                                    }
                                }
                                OppoToastHelper.setToastMap(OppoToastHelper.sToastAppMap);
                            }
                        }
                        if (OppoToastHelper.sWMService != null) {
                            Log.v(OppoToastHelper.TAG, "sWMService.updateAppOpsState()");
                            OppoToastHelper.sWMService.updateAppOpsState();
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
                        if (!(packageName == null || OppoToastHelper.sWMService == null || OppoToastHelper.sWMService.mContext == null)) {
                            OppoToastHelper.sListToastWindow.add(packageName);
                            if (OppoToastHelper.sListToastWindow.size() >= 3) {
                                Intent intent = new Intent(OppoToastHelper.STATIC_ACTION);
                                intent.putStringArrayListExtra("pkgName", new ArrayList(OppoToastHelper.sListToastWindow));
                                OppoToastHelper.sWMService.mContext.sendBroadcast(intent);
                                OppoToastHelper.sListToastWindow.clear();
                                break;
                            }
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    static {
        sToastAppMap = new HashMap();
        sWhiteList = new ArrayList();
        sToastAppMap = getToastAppMapPri();
        sWhiteList = getToastWhiteListPri();
        new DataFileListener(TOAST_FILTER_FILE_PATH).startWatching();
        new DataFileListener(TOAST_WHITELIST_FILE_PATH).startWatching();
        sToastWindowThread.start();
    }

    public static Map<String, String> getToastAppMap() {
        initObserver();
        return sToastAppMap;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x006d A:{SYNTHETIC, Splitter: B:35:0x006d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Map<String, String> getToastAppMapPri() {
        Exception e;
        Throwable th;
        Map<String, String> resultMap = new HashMap();
        sToastLock.lock();
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
                        sToastLock.unlock();
                        return resultMap;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                        }
                        sToastLock.unlock();
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
                    sToastLock.unlock();
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
            sToastLock.unlock();
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
        sToastLock.lock();
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
                sToastLock.unlock();
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
                    sToastLock.unlock();
                } catch (Throwable th2) {
                    th = th2;
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                    }
                    sToastLock.unlock();
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outputStream = outputStream2;
                if (outputStream != null) {
                }
                sToastLock.unlock();
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
        return sWhiteList;
    }

    private static List<String> getToastWhiteListPri() {
        Exception e;
        Throwable th;
        List<String> sWhiteList = new ArrayList();
        sToastLock.lock();
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
                                if (!(switchStr == null || (switchStr.isEmpty() ^ 1) == 0)) {
                                    if (switchStr.equalsIgnoreCase(DEFAULT_SEITCH_OFF)) {
                                        sDefaultSwitch = false;
                                        continue;
                                    } else {
                                        sDefaultSwitch = true;
                                    }
                                }
                            } else if ("force_open".equals(tag)) {
                                name = parser.getAttributeValue(null, "name");
                                if (!(name == null || (name.isEmpty() ^ 1) == 0)) {
                                    sWhiteList.add(name);
                                    continue;
                                }
                            } else if ("default_open".equals(tag)) {
                                name = parser.getAttributeValue(null, "name");
                                if (!(name == null || (name.isEmpty() ^ 1) == 0)) {
                                    sDefaultOpenSet.add(name);
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
                    sToastLock.unlock();
                    return sWhiteList;
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
        sToastLock.unlock();
        return sWhiteList;
    }

    public static boolean getDefaultSwitch() {
        if (sIsCmccTestVersion) {
            return true;
        }
        return sDefaultSwitch;
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
        if (packageName.startsWith("com.oppo.") || packageName.startsWith("com.coloros.") || packageName.startsWith("com.nearme.")) {
            return true;
        }
        return false;
    }

    private static boolean isSystemApp(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, packageName + " not found !");
            e.printStackTrace();
        }
        return (info.flags & 1) != 0;
    }

    public static boolean shouldCloseToast(Context context, String packageName) {
        if (!SystemProperties.getBoolean(AlertWindowNotification.PROPERTY_PERMISSION_ENABLE, false) || (skipPackage(context, packageName) ^ 1) == 0 || (getToastWhiteList().contains(packageName) ^ 1) == 0) {
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
        } else if (getDefaultSwitch() || sDefaultOpenSet.contains(packageName) || hasAppOp(context, 24, packageName)) {
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
        if (sWhiteList != null && sWhiteList.contains(packageName)) {
            return false;
        }
        if (SystemProperties.getBoolean(AlertWindowNotification.PROPERTY_PERMISSION_ENABLE, false) && getToastAppMap().get(packageName) != null && ((String) getToastAppMap().get(packageName)).equals("0")) {
            return true;
        }
        return false;
    }

    public static void setWMService(WindowManagerService service) {
        if (sWMService == null) {
            sWMService = service;
            if (sWMService.mContext != null) {
                sIsCmccTestVersion = sWMService.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_CMCC_TEST);
            }
        }
    }

    private static void sendToastDataStatic(Context context, String packageName) {
        Message msg = sToastWindowHandler.obtainMessage(103);
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
            if (sWhiteListListener == null || sToastAppListener == null) {
                sWhiteListListener = new DataFileListener(TOAST_WHITELIST_FILE_PATH);
                sToastAppListener = new DataFileListener(TOAST_FILTER_FILE_PATH);
                sWhiteListListener.startWatching();
                sToastAppListener.startWatching();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
