package com.oppo.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.RemoteException;
import android.util.Xml;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoDaemonListHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/oppo/multimedia/Multimedia_Daemon_Online_List.xml";
    public static final String FILTER_NAME = "Multimedia_Daemon_List";
    private static final String SYS_FILE_DIR = "system/etc/Multimedia_Daemon_List.xml";
    private static final String TAG = "OppoMultimediaService_ListHelper";
    public static final String[] TAG_ELEMENT = new String[]{"name", "attribute"};
    public static final String TAG_FILTER_NAME = "filter-name";
    public static final String[] TAG_MODULE = new String[]{"dirac", "volume", "control", "zenmode", "record-conflict", "change-streamtype", "wechat-encode", "oppo-getstreamvolume", "setvoipmode-incall", "at-sampleRate", "display-notify", "not-addplayerbase"};
    public static final String TAG_VERSION = "version";
    private ActivityManager mActivityManager;
    private HashMap<String, HashMap<String, OppoDaemonListInfo>> mDaemonModuleInfos = new HashMap();
    private int mLastVersion = -1;
    private final OppoMultimediaService mOppoMultimediaService;

    public enum ListTag {
        NONE,
        MODULE,
        ELEMENT,
        AUTO
    }

    public class OppoDaemonListParser extends UpdateInfo {
        HashMap<String, OppoDaemonListInfo> mDaemonListInfos = null;
        OppoDaemonListInfo mListinfo = null;

        public OppoDaemonListParser() {
            super();
        }

        public void parseContentFromXML(String content) {
            XmlPullParserException e;
            IOException e2;
            Throwable th;
            DebugLog.d(OppoDaemonListHelper.TAG, "parseContentFromXML content = " + content);
            if (content != null) {
                StringReader stringReader = null;
                boolean isNeedUpate = false;
                String module = null;
                String name = null;
                try {
                    String listEndTag = OppoDaemonListHelper.TAG_ELEMENT[1];
                    XmlPullParser parser = Xml.newPullParser();
                    Reader stringReader2 = new StringReader(content);
                    try {
                        Reader strReader;
                        parser.setInput(stringReader2);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    String value = parser.getName();
                                    if (getTagType(value) == ListTag.MODULE) {
                                        module = value;
                                        this.mDaemonListInfos = new HashMap();
                                        break;
                                    }
                                    if (getTagType(value) == ListTag.ELEMENT) {
                                        eventType = parser.next();
                                        if (!value.equals(OppoDaemonListHelper.TAG_ELEMENT[0])) {
                                            if (!value.equals(OppoDaemonListHelper.TAG_ELEMENT[1])) {
                                                break;
                                            }
                                            String attribute = parser.getText();
                                            this.mListinfo = new OppoDaemonListInfo();
                                            this.mListinfo.setModule(module);
                                            this.mListinfo.setName(name);
                                            this.mListinfo.setAttribute(attribute);
                                            break;
                                        }
                                        name = parser.getText();
                                        break;
                                    } else if (value.equals(OppoDaemonListHelper.TAG_VERSION)) {
                                        int currentVersion = OppoDaemonListHelper.this.mLastVersion;
                                        eventType = parser.next();
                                        try {
                                            currentVersion = Integer.valueOf(parser.getText()).intValue();
                                            DebugLog.d(OppoDaemonListHelper.TAG, "currentVersion :" + currentVersion + " mLastVersion:" + OppoDaemonListHelper.this.mLastVersion);
                                            if (currentVersion > OppoDaemonListHelper.this.mLastVersion && OppoDaemonListHelper.this.mLastVersion > 0) {
                                                isNeedUpate = true;
                                            }
                                            DebugLog.d(OppoDaemonListHelper.TAG, "isNeedUpate :" + isNeedUpate + " mVersion :" + this.mVersion);
                                            if (isNeedUpate || OppoDaemonListHelper.this.mLastVersion <= 0) {
                                                if (currentVersion <= OppoDaemonListHelper.this.mLastVersion) {
                                                    break;
                                                }
                                                OppoDaemonListHelper.this.mLastVersion = currentVersion;
                                                this.mVersion = (long) OppoDaemonListHelper.this.mLastVersion;
                                                if (this.mDaemonListInfos != null) {
                                                    this.mDaemonListInfos.clear();
                                                    this.mDaemonListInfos = null;
                                                }
                                                if (OppoDaemonListHelper.this.mDaemonModuleInfos == null) {
                                                    break;
                                                }
                                                OppoDaemonListHelper.this.mDaemonModuleInfos.clear();
                                                break;
                                            }
                                            if (stringReader2 != null) {
                                                stringReader2.close();
                                            } else {
                                                strReader = stringReader2;
                                            }
                                            if (isNeedUpate) {
                                                try {
                                                    if (OppoDaemonListHelper.this.mOppoMultimediaService != null) {
                                                        OppoDaemonListHelper.this.changeFilePermisson(OppoDaemonListHelper.DATA_FILE_DIR);
                                                        OppoDaemonListHelper.this.mOppoMultimediaService.setEventInfo(15, null);
                                                    }
                                                } catch (RemoteException e3) {
                                                }
                                            }
                                            return;
                                        } catch (NumberFormatException e4) {
                                            e4.printStackTrace();
                                            break;
                                        }
                                    } else {
                                        continue;
                                    }
                                case 3:
                                    if (!parser.getName().equals(listEndTag) || this.mListinfo == null || this.mDaemonListInfos == null) {
                                        if (!parser.getName().equals(module)) {
                                            break;
                                        }
                                        OppoDaemonListHelper.this.mDaemonModuleInfos.put(module, this.mDaemonListInfos);
                                        module = null;
                                        this.mDaemonListInfos = null;
                                        break;
                                    }
                                    this.mDaemonListInfos.put(name, this.mListinfo);
                                    name = null;
                                    this.mListinfo = null;
                                    break;
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (stringReader2 != null) {
                            stringReader2.close();
                        } else {
                            strReader = stringReader2;
                        }
                        if (isNeedUpate) {
                            try {
                                if (OppoDaemonListHelper.this.mOppoMultimediaService != null) {
                                    OppoDaemonListHelper.this.changeFilePermisson(OppoDaemonListHelper.DATA_FILE_DIR);
                                    OppoDaemonListHelper.this.mOppoMultimediaService.setEventInfo(15, null);
                                }
                            } catch (RemoteException e5) {
                            }
                        }
                    } catch (XmlPullParserException e6) {
                        e = e6;
                        stringReader = stringReader2;
                    } catch (IOException e7) {
                        e2 = e7;
                        stringReader = stringReader2;
                    } catch (Throwable th2) {
                        th = th2;
                        stringReader = stringReader2;
                    }
                } catch (XmlPullParserException e8) {
                    e = e8;
                    try {
                        e.printStackTrace();
                        if (stringReader != null) {
                            stringReader.close();
                        }
                        if (isNeedUpate) {
                            try {
                                if (OppoDaemonListHelper.this.mOppoMultimediaService != null) {
                                    OppoDaemonListHelper.this.changeFilePermisson(OppoDaemonListHelper.DATA_FILE_DIR);
                                    OppoDaemonListHelper.this.mOppoMultimediaService.setEventInfo(15, null);
                                }
                            } catch (RemoteException e9) {
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (stringReader != null) {
                            stringReader.close();
                        }
                        if (isNeedUpate) {
                            try {
                                if (OppoDaemonListHelper.this.mOppoMultimediaService != null) {
                                    OppoDaemonListHelper.this.changeFilePermisson(OppoDaemonListHelper.DATA_FILE_DIR);
                                    OppoDaemonListHelper.this.mOppoMultimediaService.setEventInfo(15, null);
                                }
                            } catch (RemoteException e10) {
                            }
                        }
                        throw th;
                    }
                } catch (IOException e11) {
                    e2 = e11;
                    e2.printStackTrace();
                    if (stringReader != null) {
                        stringReader.close();
                    }
                    if (isNeedUpate) {
                        try {
                            if (OppoDaemonListHelper.this.mOppoMultimediaService != null) {
                                OppoDaemonListHelper.this.changeFilePermisson(OppoDaemonListHelper.DATA_FILE_DIR);
                                OppoDaemonListHelper.this.mOppoMultimediaService.setEventInfo(15, null);
                            }
                        } catch (RemoteException e12) {
                        }
                    }
                }
            }
        }

        public ListTag getTagType(String tag) {
            int i = 0;
            for (String str : OppoDaemonListHelper.TAG_MODULE) {
                if (tag.equals(str)) {
                    return ListTag.MODULE;
                }
            }
            String[] strArr = OppoDaemonListHelper.TAG_ELEMENT;
            int length = strArr.length;
            while (i < length) {
                if (tag.equals(strArr[i])) {
                    return ListTag.ELEMENT;
                }
                i++;
            }
            return ListTag.NONE;
        }

        /* JADX WARNING: Removed duplicated region for block: B:43:0x0097  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0023  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0081  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0077  */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0089  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private long getContentVersion(String content) {
            Throwable th;
            long version = -1;
            if (content == null) {
                return -1;
            }
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                StringReader strReader2 = new StringReader(content);
                try {
                    parser.setInput(strReader2);
                    boolean found = false;
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        switch (eventType) {
                            case 2:
                                if (OppoDaemonListHelper.TAG_VERSION.equals(parser.getName())) {
                                    eventType = parser.next();
                                    DebugLog.d(OppoDaemonListHelper.TAG, "eventType = " + eventType + ", text = " + parser.getText());
                                    version = (long) Integer.parseInt(parser.getText());
                                    found = true;
                                    break;
                                }
                                break;
                        }
                        if (found) {
                            if (strReader2 == null) {
                                strReader2.close();
                            }
                            return version;
                        }
                    }
                    if (strReader2 == null) {
                    }
                    return version;
                } catch (XmlPullParserException e) {
                    strReader = strReader2;
                    if (strReader != null) {
                    }
                    return -1;
                } catch (IOException e2) {
                    strReader = strReader2;
                    if (strReader != null) {
                    }
                    return -1;
                } catch (Throwable th2) {
                    th = th2;
                    strReader = strReader2;
                    if (strReader != null) {
                    }
                    throw th;
                }
            } catch (XmlPullParserException e3) {
                if (strReader != null) {
                    strReader.close();
                }
                return -1;
            } catch (IOException e4) {
                if (strReader != null) {
                    strReader.close();
                }
                return -1;
            } catch (Throwable th3) {
                th = th3;
                if (strReader != null) {
                    strReader.close();
                }
                throw th;
            }
        }

        public boolean updateToLowerVersion(String content) {
            long newVersion = getContentVersion(content);
            DebugLog.d(OppoDaemonListHelper.TAG, "updateToLowerVersion, newVersion = " + newVersion + ", mVersion = " + this.mVersion);
            return newVersion < this.mVersion;
        }

        public void dump() {
        }
    }

    private void changeFilePermisson(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            DebugLog.d(TAG, "setReadable result :" + file.setReadable(true, false));
            return;
        }
        DebugLog.d(TAG, "filename :" + filename + " is not exist");
    }

    public OppoDaemonListHelper(Context context, OppoMultimediaService oms) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mOppoMultimediaService = oms;
        setUpdateInfo(new OppoDaemonListParser(), new OppoDaemonListParser());
        try {
            init();
            initUpdateBroadcastReceiver();
            changeFilePermisson(DATA_FILE_DIR);
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkIsInDaemonlistByName(String module, String packageName) {
        String processName = packageNameFormat(packageName);
        String moduleName = module;
        if (!this.mDaemonModuleInfos.isEmpty() && this.mDaemonModuleInfos.containsKey(module)) {
            HashMap<String, OppoDaemonListInfo> moduleListInfos = (HashMap) this.mDaemonModuleInfos.get(module);
            if (moduleListInfos != null && moduleListInfos.containsKey(processName)) {
                OppoDaemonListInfo info = (OppoDaemonListInfo) moduleListInfos.get(processName);
                if (info != null) {
                    DebugLog.d(TAG, "info :" + info.getListInfo());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkIsInDaemonlistByPid(String module, int pid) {
        for (RunningAppProcessInfo appProcess : this.mActivityManager.getRunningAppProcesses()) {
            if (pid == appProcess.pid) {
                return checkIsInDaemonlistByName(module, appProcess.processName);
            }
        }
        return false;
    }

    public OppoDaemonListInfo getListObjectByAppPid(String module, int pid) {
        for (RunningAppProcessInfo appProcess : this.mActivityManager.getRunningAppProcesses()) {
            if (pid == appProcess.pid) {
                return getListObjectByAppName(module, appProcess.processName);
            }
        }
        return null;
    }

    public OppoDaemonListInfo getListObjectByAppName(String module, String packageName) {
        String processName = packageName;
        String moduleName = module;
        if (!this.mDaemonModuleInfos.isEmpty() && this.mDaemonModuleInfos.containsKey(module)) {
            HashMap<String, OppoDaemonListInfo> moduleListInfos = (HashMap) this.mDaemonModuleInfos.get(module);
            if (moduleListInfos != null && moduleListInfos.containsKey(packageName)) {
                OppoDaemonListInfo info = (OppoDaemonListInfo) moduleListInfos.get(packageName);
                if (info != null) {
                    return info;
                }
            }
        }
        return null;
    }

    public String getAttributeByAppName(String module, String packageName) {
        OppoDaemonListInfo listInfo = getListObjectByAppName(module, packageName);
        if (listInfo != null) {
            return listInfo.getAttribute();
        }
        return null;
    }

    public String getAttributeByAppPid(String module, int pid) {
        OppoDaemonListInfo listInfo = getListObjectByAppPid(module, pid);
        if (listInfo != null) {
            return listInfo.getAttribute();
        }
        return null;
    }

    private String packageNameFormat(String packageName) {
        if (packageName == null || packageName.indexOf(":") == -1) {
            return packageName;
        }
        String[] arrStr = packageName.split(":");
        if (arrStr == null || arrStr.length <= 0) {
            return packageName;
        }
        return arrStr[0];
    }

    public void dump() {
        if (this.mDaemonModuleInfos != null) {
            for (Entry moduleEntry : this.mDaemonModuleInfos.entrySet()) {
                String muduleKey = (String) moduleEntry.getKey();
                HashMap<String, OppoDaemonListInfo> daemonListInfos = (HashMap) moduleEntry.getValue();
                if (daemonListInfos != null) {
                    for (Entry listEntry : daemonListInfos.entrySet()) {
                        DebugLog.d(TAG, "listKey = " + ((String) listEntry.getKey()) + " value = " + ((OppoDaemonListInfo) listEntry.getValue()).getListInfo());
                    }
                }
            }
        }
    }
}
