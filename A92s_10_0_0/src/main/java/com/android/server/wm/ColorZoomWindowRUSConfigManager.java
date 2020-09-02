package com.android.server.wm;

import android.os.Handler;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.server.IoThread;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class ColorZoomWindowRUSConfigManager {
    private static final String CORNER_RADIUS_TAG = "corner_radius";
    private static final String CPN_PKG_LIST_TAG = "support_zoom_cpn_list";
    private static final String DEFAULT_ZOOM_WINDOW_CONFIG_DIR_PATH = "/system/oppo/";
    private static final String DEFAULT_ZOOM_WINDOW_CONFIG_FILE_PATH = "/system/oppo/sys_zoom_window_config.xml";
    private static final String OPPO_ZOOM_CONFIG_FILE_PATH = "/data/oppo/coloros/zoom/sys_zoom_window_config.xml";
    private static final String OPPO_ZOOM_DIR_PATH = "/data/oppo/coloros/zoom/";
    private static final String REGION_TAG = "exclude_touch_region";
    private static final String REPLY_PKG_LIST_TAG = "support_reply_pkg_list";
    private static final String START_TAG = "filter-conf";
    private static final String SUPPORT_PKG_LIST_TAG = "support_pkg_list";
    private static final String TAG = "ColorZoomWindowRUSConfigManager";
    private static final String UNSUPPORT_CPN_LIST_TAG = "unsupport_zoom_cpn_list";
    private static final String VERSION_TAG = "version";
    private static final int WRITE_CONFIG_INFO_DELAY = 5000;
    private static final String ZOOM_WINDOW_CONFIG_FILE_FORMAT = ".xml";
    private static final String ZOOM_WINDOW_CONFIG_FILE_NAME = "sys_zoom_window_config.xml";
    private static final String ZOOM_WINDOW_CONFIG_FILTER_NAME = "sys_zoom_window_config";
    private static final String ZOOM_WINDOW_SIZE_TAG = "zoom_window_size";
    private static final String ZOOM_WINDOW_SWITCH_TAG = "zoom_window_switch";
    private static volatile ColorZoomWindowRUSConfigManager sConfigManager = null;
    private static boolean sDebugSwitch = IColorZoomWindowManager.sDebugfDetail;
    private ColorZoomWindowRUSConfig mConfig = new ColorZoomWindowRUSConfig();
    private final Object mConfigManagerLock = new Object();
    private Handler mHandler = IoThread.getHandler();
    private Runnable mWriteConfigRunnable = new Runnable() {
        /* class com.android.server.wm.ColorZoomWindowRUSConfigManager.AnonymousClass1 */

        public void run() {
            ColorZoomWindowRUSConfigManager.this.writeConfigInfoToFile();
        }
    };

    private ColorZoomWindowRUSConfigManager() {
    }

    public static ColorZoomWindowRUSConfigManager getInstance() {
        if (sConfigManager == null) {
            synchronized (ColorZoomWindowRUSConfigManager.class) {
                if (sConfigManager == null) {
                    sConfigManager = new ColorZoomWindowRUSConfigManager();
                }
            }
        }
        return sConfigManager;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void parseConfig(File file, ColorZoomWindowRUSConfig config) {
        int type;
        String tagName;
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                char c = 2;
                if (type == 2 && (tagName = parser.getName()) != null && !tagName.isEmpty()) {
                    switch (tagName.hashCode()) {
                        case -747490474:
                            if (tagName.equals(REPLY_PKG_LIST_TAG)) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case -708925953:
                            if (tagName.equals(UNSUPPORT_CPN_LIST_TAG)) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case -157035785:
                            if (tagName.equals(ZOOM_WINDOW_SWITCH_TAG)) {
                                c = 1;
                                break;
                            }
                            c = 65535;
                            break;
                        case 48012732:
                            if (tagName.equals(CORNER_RADIUS_TAG)) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case 351608024:
                            if (tagName.equals("version")) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        case 530410232:
                            if (tagName.equals(CPN_PKG_LIST_TAG)) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        case 782434721:
                            if (tagName.equals(SUPPORT_PKG_LIST_TAG)) {
                                break;
                            }
                            c = 65535;
                            break;
                        case 1067978884:
                            if (tagName.equals(ZOOM_WINDOW_SIZE_TAG)) {
                                c = 7;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1278070329:
                            if (tagName.equals(REGION_TAG)) {
                                c = 8;
                                break;
                            }
                            c = 65535;
                            break;
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            config.setVersion(Integer.parseInt(parser.nextText()));
                            continue;
                        case 1:
                            config.setZoomWindowSwitch(Boolean.parseBoolean(parser.nextText()));
                            continue;
                        case 2:
                            ColorZoomWindowSupportListParser.getInstance().parseRUSFile(parser, config.getPkgList());
                            continue;
                        case 3:
                            ColorZoomWindowSupportReplyListParser.getInstance().parseRUSFile(parser, config.getReplyPkgList());
                            continue;
                        case 4:
                            ColorZoomWindowSupportCpnListParser.getInstance().parseRUSFile(parser, config.getCpnList());
                            continue;
                        case 5:
                            ColorZoomWindowUnSupportCpnListParser.getInstance().parseRUSFile(parser, config.getUnSupportCpnList());
                            continue;
                        case 6:
                            config.setCornerRadius(Float.parseFloat(parser.nextText()));
                            continue;
                        case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /*{ENCODED_INT: 7}*/:
                            ColorZoomWindowSizeParser.getInstance().parseZoomWindowSize(parser, config);
                            continue;
                        case ColorStartingWindowRUSHelper.SPLASH_SNAPSHOT_WHITE_THIRD_PARTY_APP /*{ENCODED_INT: 8}*/:
                            ColorZoomWindowRegionParser.getInstance().parseZoomWindowRegion(parser, config);
                            continue;
                        default:
                            continue;
                    }
                }
            } while (type != 1);
            try {
                if (parser instanceof Closeable) {
                    ((Closeable) parser).close();
                }
                stream2.close();
            } catch (IOException e) {
                Slog.e(TAG, "Failed to close!");
                e.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (0 instanceof Closeable) {
                null.close();
            }
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            try {
                if (0 instanceof Closeable) {
                    null.close();
                }
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e3) {
                Slog.e(TAG, "Failed to close!");
                e3.printStackTrace();
            }
            throw th;
        }
    }

    private boolean checkFileExist(String configFilePath) {
        if (new File(configFilePath).exists()) {
            return true;
        }
        Slog.e(TAG, "Config file in path :" + configFilePath + " is not exists!");
        return false;
    }

    public boolean confirmZoomWindowDir() {
        File zoomWindowDir = new File(OPPO_ZOOM_DIR_PATH);
        if (zoomWindowDir.exists()) {
            return true;
        }
        Slog.i(TAG, "mkdir for path: /data/oppo/coloros/zoom/");
        if (zoomWindowDir.mkdirs()) {
            return true;
        }
        Slog.e(TAG, "mkdir failed for path: /data/oppo/coloros/zoom/");
        return false;
    }

    public ColorZoomWindowRUSConfig readZoomWindowConfig() {
        ColorZoomWindowRUSConfig config;
        if (sDebugSwitch) {
            Slog.i(TAG, "readZoomWindowConfig start");
        }
        ColorZoomWindowRUSConfig config2 = new ColorZoomWindowRUSConfig();
        if (!confirmZoomWindowDir()) {
            Slog.w(TAG, "confirmZoomWindowDir failed!");
        }
        if (!checkFileExist(DEFAULT_ZOOM_WINDOW_CONFIG_FILE_PATH)) {
            return config2;
        }
        if (checkFileExist(OPPO_ZOOM_CONFIG_FILE_PATH)) {
            config = readZoomWindowConfigFile(OPPO_ZOOM_CONFIG_FILE_PATH);
        } else {
            config = readZoomWindowConfigFile(DEFAULT_ZOOM_WINDOW_CONFIG_FILE_PATH);
        }
        if (sDebugSwitch) {
            Slog.i(TAG, "readZoomWindowConfig end");
        }
        return config;
    }

    public ColorZoomWindowRUSConfig readZoomWindowConfigFile(String configFilePath) {
        ColorZoomWindowRUSConfig config = new ColorZoomWindowRUSConfig();
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            Slog.e(TAG, "No config file in path:" + configFilePath);
            Slog.e(TAG, "readZoomWindowConfigFile failed!");
            return config;
        }
        Slog.i(TAG, "readZoomWindowConfigFile start in path:" + configFilePath);
        parseConfig(configFile, config);
        Slog.i(TAG, "zoom window config = " + config.toString());
        return config;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:104:?, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0277, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x0278, code lost:
        android.util.Slog.e(r4, r3 + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0193, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0194, code lost:
        r4 = com.android.server.wm.ColorZoomWindowRUSConfigManager.TAG;
        r3 = r17;
        r5 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x019d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x019e, code lost:
        r4 = com.android.server.wm.ColorZoomWindowRUSConfigManager.TAG;
        r3 = r17;
        r5 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x01a7, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x01a8, code lost:
        r4 = com.android.server.wm.ColorZoomWindowRUSConfigManager.TAG;
        r3 = r17;
        r5 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x01b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x01b2, code lost:
        r4 = com.android.server.wm.ColorZoomWindowRUSConfigManager.TAG;
        r3 = r17;
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x01ba, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x01bb, code lost:
        r4 = com.android.server.wm.ColorZoomWindowRUSConfigManager.TAG;
        r3 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01db, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01dc, code lost:
        r0 = r0;
        r2 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0210, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0211, code lost:
        r0 = r0;
        r2 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x023a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x023b, code lost:
        r0 = r0;
        r2 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:?, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0264, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0265, code lost:
        r0 = r0;
        r2 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x0273 A[SYNTHETIC, Splitter:B:103:0x0273] */
    /* JADX WARNING: Removed duplicated region for block: B:110:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:111:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:112:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:113:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x01b1 A[ExcHandler: all (r0v27 'th' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r13 
      PHI: (r13v9 'fos' java.io.FileOutputStream) = (r13v1 'fos' java.io.FileOutputStream), (r13v1 'fos' java.io.FileOutputStream), (r13v10 'fos' java.io.FileOutputStream), (r13v10 'fos' java.io.FileOutputStream) binds: [B:18:0x007c, B:19:?, B:21:0x0081, B:22:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:18:0x007c] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01ba A[ExcHandler: Exception (e java.lang.Exception), PHI: r13 
      PHI: (r13v8 'fos' java.io.FileOutputStream) = (r13v1 'fos' java.io.FileOutputStream), (r13v1 'fos' java.io.FileOutputStream), (r13v10 'fos' java.io.FileOutputStream), (r13v10 'fos' java.io.FileOutputStream) binds: [B:18:0x007c, B:19:?, B:21:0x0081, B:22:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:18:0x007c] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01d6 A[SYNTHETIC, Splitter:B:71:0x01d6] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x020b A[SYNTHETIC, Splitter:B:80:0x020b] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0235 A[SYNTHETIC, Splitter:B:88:0x0235] */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x025f A[SYNTHETIC, Splitter:B:96:0x025f] */
    public void writeZoomWindowConfigFile(ColorZoomWindowRUSConfig config) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        IOException e;
        StringBuilder sb;
        String str6;
        String str7;
        String str8;
        String str9;
        String str10;
        String str11;
        String sb2;
        String str12;
        String str13;
        Slog.i(TAG, "writeZoomWindowConfigFile start path:/data/oppo/coloros/zoom/sys_zoom_window_config.xml");
        if (!confirmZoomWindowDir()) {
            Slog.e(TAG, "confirmZoomWindowDir failed!");
            Slog.e(TAG, "Failed to writeZoomWindowConfigFile path:/data/oppo/coloros/zoom/sys_zoom_window_config.xml");
            return;
        }
        File file = new File(OPPO_ZOOM_CONFIG_FILE_PATH);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "Failed create file /data/oppo/coloros/zoom/sys_zoom_window_config.xml");
                }
                str2 = "failed write file ";
                str = "failed close stream ";
            } catch (IOException e2) {
                str2 = "failed write file ";
                StringBuilder sb3 = new StringBuilder();
                str = "failed close stream ";
                sb3.append("failed create file ");
                sb3.append(e2);
                Slog.i(TAG, sb3.toString());
            }
        } else {
            str2 = "failed write file ";
            str = "failed close stream ";
        }
        if (file.exists()) {
            AtomicFile destination = new AtomicFile(file);
            FileOutputStream fos = null;
            try {
                fos = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                try {
                    serializer.setOutput(fos, StandardCharsets.UTF_8.name());
                } catch (IllegalArgumentException e3) {
                    e = e3;
                    str6 = TAG;
                    str7 = str;
                    str5 = str2;
                    Slog.e(str3, str5 + e);
                    if (fos == null) {
                    }
                } catch (IllegalStateException e4) {
                    e = e4;
                    str9 = TAG;
                    str10 = str;
                    str8 = str2;
                    Slog.e(str3, str8 + e);
                    if (fos == null) {
                    }
                } catch (IOException e5) {
                    e = e5;
                    str3 = TAG;
                    str4 = str;
                    str11 = str2;
                    Slog.e(str3, str11 + e);
                    if (fos == null) {
                    }
                } catch (Exception e6) {
                    e = e6;
                    str12 = TAG;
                    str13 = str;
                    try {
                        Slog.e(str3, str2 + e);
                        if (fos != null) {
                        }
                    } catch (Throwable th) {
                        th = th;
                        Throwable th2 = th;
                        if (fos != null) {
                        }
                        throw th2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    str3 = TAG;
                    str4 = str;
                    Throwable th22 = th;
                    if (fos != null) {
                    }
                    throw th22;
                }
                try {
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                    serializer.startDocument(null, true);
                    serializer.startTag(null, START_TAG);
                    serializer.startTag(null, "version");
                    serializer.text(String.valueOf(config.getVersion()));
                    serializer.endTag(null, "version");
                    serializer.startTag(null, ZOOM_WINDOW_SWITCH_TAG);
                    serializer.text(String.valueOf(config.getZoomWindowSwitch()));
                    serializer.endTag(null, ZOOM_WINDOW_SWITCH_TAG);
                    serializer.startTag(null, SUPPORT_PKG_LIST_TAG);
                    ColorZoomWindowSupportListParser.getInstance().writeXMLFile(serializer, config);
                    serializer.endTag(null, SUPPORT_PKG_LIST_TAG);
                    serializer.startTag(null, REPLY_PKG_LIST_TAG);
                    ColorZoomWindowSupportReplyListParser.getInstance().writeXMLFile(serializer, config);
                    serializer.endTag(null, REPLY_PKG_LIST_TAG);
                    serializer.startTag(null, CPN_PKG_LIST_TAG);
                    ColorZoomWindowSupportCpnListParser.getInstance().writeXMLFile(serializer, config);
                    serializer.endTag(null, CPN_PKG_LIST_TAG);
                    serializer.startTag(null, UNSUPPORT_CPN_LIST_TAG);
                    ColorZoomWindowUnSupportCpnListParser.getInstance().writeXMLFile(serializer, config);
                    serializer.endTag(null, UNSUPPORT_CPN_LIST_TAG);
                    serializer.startTag(null, CORNER_RADIUS_TAG);
                    serializer.text(String.valueOf(config.getCornerRadius()));
                    serializer.endTag(null, CORNER_RADIUS_TAG);
                    serializer.startTag(null, ZOOM_WINDOW_SIZE_TAG);
                    ColorZoomWindowSizeParser.getInstance().writeXMLFile(serializer, config);
                    serializer.endTag(null, ZOOM_WINDOW_SIZE_TAG);
                    serializer.startTag(null, REGION_TAG);
                    ColorZoomWindowRegionParser.getInstance().writeXMLFile(serializer, config);
                    serializer.endTag(null, REGION_TAG);
                    serializer.endTag(null, START_TAG);
                    serializer.endDocument();
                    destination.finishWrite(fos);
                    if (fos != null) {
                        try {
                            fos.close();
                            return;
                        } catch (IOException e7) {
                            sb2 = str + e7;
                            str3 = TAG;
                        }
                    } else {
                        return;
                    }
                } catch (IllegalArgumentException e8) {
                    e = e8;
                    str7 = str;
                    str6 = TAG;
                    str5 = str2;
                    Slog.e(str3, str5 + e);
                    if (fos == null) {
                    }
                } catch (IllegalStateException e9) {
                    e = e9;
                    str10 = str;
                    str9 = TAG;
                    str8 = str2;
                    Slog.e(str3, str8 + e);
                    if (fos == null) {
                    }
                } catch (IOException e10) {
                    e = e10;
                    str4 = str;
                    str3 = TAG;
                    str11 = str2;
                    Slog.e(str3, str11 + e);
                    if (fos == null) {
                    }
                } catch (Exception e11) {
                    e = e11;
                    str13 = str;
                    str12 = TAG;
                    Slog.e(str3, str2 + e);
                    if (fos != null) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    str4 = str;
                    str3 = TAG;
                    Throwable th222 = th;
                    if (fos != null) {
                    }
                    throw th222;
                }
            } catch (IllegalArgumentException e12) {
                e = e12;
                str6 = TAG;
                str5 = str2;
                str7 = str;
                Slog.e(str3, str5 + e);
                if (fos == null) {
                }
            } catch (IllegalStateException e13) {
                e = e13;
                str9 = TAG;
                str8 = str2;
                str10 = str;
                Slog.e(str3, str8 + e);
                if (fos == null) {
                }
            } catch (IOException e14) {
                e = e14;
                str3 = TAG;
                str11 = str2;
                str4 = str;
                Slog.e(str3, str11 + e);
                if (fos == null) {
                }
            } catch (Exception e15) {
            } catch (Throwable th5) {
            }
        } else {
            return;
        }
        sb.append(str4);
        sb.append(e);
        sb2 = sb.toString();
        Slog.e(str3, sb2);
        Slog.e(str3, sb2);
    }

    /* access modifiers changed from: private */
    public void writeConfigInfoToFile() {
        synchronized (this.mConfigManagerLock) {
            writeZoomWindowConfigFile(this.mConfig);
        }
    }

    public void scheduleWriteConfig(ColorZoomWindowRUSConfig config) {
        Slog.i(TAG, "scheduleWriteConfig start");
        this.mConfig = config;
        if (!this.mHandler.hasCallbacks(this.mWriteConfigRunnable)) {
            this.mHandler.postDelayed(this.mWriteConfigRunnable, 5000);
        }
    }
}
