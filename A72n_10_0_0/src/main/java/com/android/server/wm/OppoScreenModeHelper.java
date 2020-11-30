package com.android.server.wm;

import android.content.Context;
import android.os.OppoBaseEnvironment;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.oppo.OppoRomUpdateHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoScreenModeHelper extends OppoRomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/refresh_rate_config.xml";
    static final boolean DEBUG = OppoScreenModeService.DEBUG_PANIC;
    public static final String FILTER_NAME = "sys_refresh_rate_config";
    private static final int REFRESH_RATE_120 = 3;
    private static final int REFRESH_RATE_FULL_SPEED = 7;
    private static final String SYS_FILE_DIR = (OppoBaseEnvironment.getOppoProductDirectory().getAbsolutePath() + "/etc/refresh_rate_config.xml");
    static final String TAG = "OppoScreenModeHelper";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_GAME = "game";
    private static final String TAG_INSERTFRAME = "insertframe";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_RECORD = "record";
    private static final String TAG_TPPKG = "TPPkg";
    private static final String TAG_VERSION = "version";
    private static final String TAG_VIDEO = "video";
    private static final int TYPE_GAME = 2;
    private static final int TYPE_OVERRIDE = 3;
    private static final int TYPE_VEDIO = 1;
    private static final int TYPE_VERSION = 4;
    private Callback mDataChangeCallBack = null;
    private long mLastVersion = 0;
    int mRefreshRateFullspeed = -1;

    public interface Callback {
        void onDataChange();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeFilePermisson(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            boolean result = file.setReadable(true, DEBUG);
            Slog.i(TAG, "setReadable result :" + result);
            return;
        }
        Log.i(TAG, "filename :" + filename + " is not exist");
    }

    /* access modifiers changed from: private */
    public class OppoScreenModeInfo extends OppoRomUpdateHelper.UpdateInfo {
        private HashMap<String, Integer> mActivityMap = new HashMap<>();
        private HashMap<String, Integer> mGameMap = new HashMap<>();
        private HashMap<String, Integer> mPackageMap = new HashMap<>();
        private ArrayList<String> mRecordList = new ArrayList<>();
        private ArrayList<String> mTPWhiteList = new ArrayList<>();
        private HashMap<String, Integer> mVideoMap = new HashMap<>();

        public OppoScreenModeInfo() {
            super(OppoScreenModeHelper.this);
        }

        /* JADX INFO: Multiple debug info for r10v14 'eventType'  int: [D('eventType' int), D('mode' int)] */
        /* JADX WARNING: Removed duplicated region for block: B:105:0x030d A[SYNTHETIC, Splitter:B:105:0x030d] */
        /* JADX WARNING: Removed duplicated region for block: B:110:0x0316 A[Catch:{ IOException -> 0x0311 }] */
        /* JADX WARNING: Removed duplicated region for block: B:115:0x0324 A[SYNTHETIC, Splitter:B:115:0x0324] */
        /* JADX WARNING: Removed duplicated region for block: B:120:0x032d A[Catch:{ IOException -> 0x0328 }] */
        /* JADX WARNING: Removed duplicated region for block: B:135:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:138:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:94:0x02f1 A[SYNTHETIC, Splitter:B:94:0x02f1] */
        /* JADX WARNING: Removed duplicated region for block: B:99:0x02fa A[Catch:{ IOException -> 0x02f5 }] */
        public void parseContentFromXML(String content) {
            Throwable th;
            XmlPullParserException e;
            IOException e2;
            String name;
            int eventType;
            int mode;
            String packagename;
            if (content != null) {
                OppoScreenModeHelper.this.changeFilePermisson(OppoScreenModeHelper.DATA_FILE_DIR);
                FileReader xmlReader = null;
                StringReader strReader = null;
                String name2 = null;
                clearCache();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    strReader = new StringReader(content);
                    parser.setInput(strReader);
                    int eventType2 = parser.getEventType();
                    while (eventType2 != 1) {
                        if (eventType2 == 0) {
                            name = name2;
                        } else if (eventType2 != 2) {
                            name = name2;
                        } else {
                            String tag = parser.getName();
                            if (OppoScreenModeHelper.DEBUG) {
                                try {
                                    Slog.d(OppoScreenModeHelper.TAG, "initializing tag:" + tag);
                                } catch (XmlPullParserException e3) {
                                    e = e3;
                                } catch (IOException e4) {
                                    e2 = e4;
                                    try {
                                        Slog.i(OppoScreenModeHelper.TAG, "Got execption parsing permissions.", e2);
                                        if (0 != 0) {
                                        }
                                        if (strReader != null) {
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (0 != 0) {
                                            try {
                                                xmlReader.close();
                                            } catch (IOException e5) {
                                                Slog.i(OppoScreenModeHelper.TAG, "Got execption close permReader.", e5);
                                                throw th;
                                            }
                                        }
                                        if (strReader != null) {
                                            strReader.close();
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    if (0 != 0) {
                                    }
                                    if (strReader != null) {
                                    }
                                    throw th;
                                }
                            }
                            if (OppoScreenModeHelper.TAG_PACKAGE.equals(tag)) {
                                int eventType3 = parser.next();
                                String[] result = parser.getText().split(",");
                                String packagename2 = result[0];
                                int mode2 = Integer.parseInt(result[1]);
                                if (OppoScreenModeHelper.DEBUG) {
                                    StringBuilder sb = new StringBuilder();
                                    name = name2;
                                    try {
                                        sb.append("initializing list package:");
                                        packagename = packagename2;
                                        sb.append(packagename);
                                        sb.append(" mode:");
                                        eventType = eventType3;
                                        mode = mode2;
                                        sb.append(OppoScreenModeHelper.this.speedFreshRateSwitch(mode));
                                        sb.append(" type:");
                                        sb.append(tag);
                                        Slog.d(OppoScreenModeHelper.TAG, sb.toString());
                                    } catch (XmlPullParserException e6) {
                                        e = e6;
                                        Slog.i(OppoScreenModeHelper.TAG, "Got execption parsing permissions.", e);
                                        if (0 != 0) {
                                        }
                                        if (strReader == null) {
                                        }
                                    } catch (IOException e7) {
                                        e2 = e7;
                                        Slog.i(OppoScreenModeHelper.TAG, "Got execption parsing permissions.", e2);
                                        if (0 != 0) {
                                        }
                                        if (strReader != null) {
                                        }
                                    }
                                } else {
                                    name = name2;
                                    eventType = eventType3;
                                    mode = mode2;
                                    packagename = packagename2;
                                }
                                this.mPackageMap.put(packagename, Integer.valueOf(OppoScreenModeHelper.this.speedFreshRateSwitch(mode)));
                            } else {
                                name = name2;
                                if (OppoScreenModeHelper.TAG_ACTIVITY.equals(tag)) {
                                    parser.next();
                                    String[] result2 = parser.getText().split(",");
                                    String activityName = result2[0];
                                    int mode3 = Integer.parseInt(result2[1]);
                                    if (OppoScreenModeHelper.DEBUG) {
                                        Slog.d(OppoScreenModeHelper.TAG, "initializing list activityName:" + activityName + " mode:" + OppoScreenModeHelper.this.speedFreshRateSwitch(mode3) + " type:" + tag);
                                    }
                                    this.mActivityMap.put(activityName, Integer.valueOf(OppoScreenModeHelper.this.speedFreshRateSwitch(mode3)));
                                } else if (OppoScreenModeHelper.TAG_GAME.equals(tag)) {
                                    parser.next();
                                    String[] result3 = parser.getText().split(",");
                                    String gameName = result3[0];
                                    int mode4 = Integer.parseInt(result3[1]);
                                    if (OppoScreenModeHelper.DEBUG) {
                                        Slog.d(OppoScreenModeHelper.TAG, "initializing list gameName:" + gameName + " mode:" + OppoScreenModeHelper.this.speedFreshRateSwitch(mode4) + " type:" + tag);
                                    }
                                    this.mGameMap.put(gameName, Integer.valueOf(OppoScreenModeHelper.this.speedFreshRateSwitch(mode4)));
                                } else if (OppoScreenModeHelper.TAG_VIDEO.equals(tag)) {
                                    parser.next();
                                    String[] result4 = parser.getText().split(",");
                                    String videoName = result4[0];
                                    int mode5 = Integer.parseInt(result4[1]);
                                    if (OppoScreenModeHelper.DEBUG) {
                                        Slog.d(OppoScreenModeHelper.TAG, "initializing list videoName:" + videoName + " mode:" + OppoScreenModeHelper.this.speedFreshRateSwitch(mode5) + " type:" + tag);
                                    }
                                    this.mVideoMap.put(videoName, Integer.valueOf(OppoScreenModeHelper.this.speedFreshRateSwitch(mode5)));
                                } else if (OppoScreenModeHelper.TAG_RECORD.equals(tag)) {
                                    parser.next();
                                    String info = parser.getText();
                                    if (OppoScreenModeHelper.DEBUG) {
                                        Slog.d(OppoScreenModeHelper.TAG, "initializing list info:" + info + " type:" + tag);
                                    }
                                    this.mRecordList.add(info);
                                } else if (OppoScreenModeHelper.TAG_TPPKG.equals(tag)) {
                                    parser.next();
                                    String info2 = parser.getText();
                                    if (OppoScreenModeHelper.DEBUG) {
                                        Slog.d(OppoScreenModeHelper.TAG, "initializing list info:" + info2 + " type:" + tag);
                                    }
                                    this.mTPWhiteList.add(info2);
                                } else if (OppoScreenModeHelper.TAG_VERSION.equals(tag)) {
                                    parser.next();
                                    String info3 = parser.getText();
                                    Slog.d(OppoScreenModeHelper.TAG, "verion :" + info3 + " type:" + tag);
                                    OppoScreenModeHelper.this.mLastVersion = Long.parseLong(info3);
                                }
                            }
                        }
                        eventType2 = parser.next();
                        name2 = name;
                    }
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e8) {
                            Slog.i(OppoScreenModeHelper.TAG, "Got execption close permReader.", e8);
                        }
                    }
                    strReader.close();
                    if (OppoScreenModeHelper.DEBUG) {
                        Slog.d(OppoScreenModeHelper.TAG, "load data end ");
                    }
                    OppoScreenModeHelper.this.dataChange();
                } catch (XmlPullParserException e9) {
                    e = e9;
                    Slog.i(OppoScreenModeHelper.TAG, "Got execption parsing permissions.", e);
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e10) {
                            Slog.i(OppoScreenModeHelper.TAG, "Got execption close permReader.", e10);
                            return;
                        }
                    }
                    if (strReader == null) {
                        strReader.close();
                    }
                } catch (IOException e11) {
                    e2 = e11;
                    Slog.i(OppoScreenModeHelper.TAG, "Got execption parsing permissions.", e2);
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e12) {
                            Slog.i(OppoScreenModeHelper.TAG, "Got execption close permReader.", e12);
                            return;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (Throwable th4) {
                    th = th4;
                    if (0 != 0) {
                    }
                    if (strReader != null) {
                    }
                    throw th;
                }
            }
        }

        public boolean updateToLowerVersion(String newContent) {
            long dataversion = OppoScreenModeHelper.this.getConfigVersion(newContent, OppoScreenModeHelper.DEBUG);
            Slog.d(OppoScreenModeHelper.TAG, "dataversion =" + dataversion + ",version =" + OppoScreenModeHelper.this.mLastVersion);
            if (dataversion > OppoScreenModeHelper.this.mLastVersion) {
                return OppoScreenModeHelper.DEBUG;
            }
            Slog.d(OppoScreenModeHelper.TAG, " data version is low! ");
            return true;
        }

        private void clearCache() {
            this.mPackageMap.clear();
            this.mActivityMap.clear();
            this.mGameMap.clear();
            this.mVideoMap.clear();
            this.mTPWhiteList.clear();
            this.mRecordList.clear();
        }

        public Integer getPackageMode(String pkg) {
            return this.mPackageMap.get(pkg);
        }

        public Integer getActivityMode(String componentname) {
            return this.mActivityMap.get(componentname);
        }

        public Integer getGameMode(String componentname) {
            return this.mGameMap.get(componentname);
        }

        public ArrayList getGameList() {
            ArrayList<String> game_l = new ArrayList<>(this.mGameMap.size());
            for (String str : this.mGameMap.keySet()) {
                game_l.add(str);
            }
            Slog.d(OppoScreenModeHelper.TAG, " getGameList size= " + game_l.size());
            return game_l;
        }

        public Integer getVideoMode(String componentname) {
            return this.mVideoMap.get(componentname);
        }

        public boolean onRecordList(String pkg) {
            return this.mRecordList.contains(pkg);
        }

        public boolean onTPWhiteList(String pkg) {
            return this.mTPWhiteList.contains(pkg);
        }

        public boolean supportTPRrate() {
            if (this.mTPWhiteList.size() > 0) {
                return true;
            }
            return OppoScreenModeHelper.DEBUG;
        }
    }

    public OppoScreenModeHelper(Context context, Callback callback, int speedRate) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mDataChangeCallBack = callback;
        this.mRefreshRateFullspeed = speedRate;
        setUpdateInfo(new OppoScreenModeInfo(), new OppoScreenModeInfo());
        try {
            init();
            changeFilePermisson(DATA_FILE_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        File datafile = new File(DATA_FILE_DIR);
        File sysfile = new File(SYS_FILE_DIR);
        if (!datafile.exists()) {
            Slog.d(TAG, "datafile not exist try to load from system");
            parseContentFromXML(readFromFile(sysfile));
            return;
        }
        long dataversion = getConfigVersion(DATA_FILE_DIR, true);
        long sysversion = getConfigVersion(SYS_FILE_DIR, true);
        Slog.d(TAG, "dataversion:" + dataversion + " sysversion:" + sysversion);
        if (dataversion >= sysversion) {
            parseContentFromXML(readFromFile(datafile));
        } else {
            parseContentFromXML(readFromFile(sysfile));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getConfigVersion(String content, boolean isPath) {
        Reader xmlReader;
        if (content == null) {
            return 0;
        }
        Slog.d(TAG, "getConfigVersion content length:" + content.length() + "," + isPath);
        long version = 0;
        if (isPath) {
            try {
                xmlReader = new FileReader(content);
            } catch (FileNotFoundException e) {
                return 0;
            }
        } else {
            xmlReader = new StringReader(content);
        }
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(xmlReader);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String tagName = parser.getName();
                        Slog.d(TAG, "getConfigVersion called  tagname:" + tagName);
                        if (TAG_VERSION.equals(tagName)) {
                            parser.next();
                            String text = parser.getText();
                            if (text.length() > 8) {
                                text = text.substring(0, 8);
                            }
                            try {
                                version = Long.parseLong(text);
                            } catch (NumberFormatException e2) {
                                Slog.e(TAG, "version convert fail");
                            }
                            try {
                                xmlReader.close();
                            } catch (IOException e3) {
                                Slog.e(TAG, StringUtils.EMPTY + e3);
                            }
                            return version;
                        }
                    }
                }
            }
            try {
                xmlReader.close();
            } catch (IOException e4) {
                Slog.e(TAG, StringUtils.EMPTY + e4);
            }
            return 0;
        } catch (XmlPullParserException e5) {
            Slog.e(TAG, StringUtils.EMPTY + e5);
            try {
                xmlReader.close();
            } catch (IOException e6) {
                Slog.e(TAG, StringUtils.EMPTY + e6);
            }
            return 0;
        } catch (Exception e7) {
            Slog.e(TAG, StringUtils.EMPTY + e7);
            try {
                xmlReader.close();
            } catch (IOException e8) {
                Slog.e(TAG, StringUtils.EMPTY + e8);
            }
            return 0;
        } catch (Throwable th) {
            try {
                xmlReader.close();
            } catch (IOException e9) {
                Slog.e(TAG, StringUtils.EMPTY + e9);
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int speedFreshRateSwitch(int freshRate) {
        if (this.mRefreshRateFullspeed >= 3 || (3 != freshRate && REFRESH_RATE_FULL_SPEED != freshRate)) {
            return freshRate;
        }
        return this.mRefreshRateFullspeed;
    }

    public Integer getActivityMode(String packageName) {
        return getUpdateInfo(true).getActivityMode(packageName);
    }

    public Integer getPackageMode(String packageName) {
        return getUpdateInfo(true).getPackageMode(packageName);
    }

    public Integer getGameMode(String packageName) {
        return getUpdateInfo(true).getGameMode(packageName);
    }

    public ArrayList getGameList() {
        return getUpdateInfo(true).getGameList();
    }

    public Integer getVideoMode(String packageName) {
        return getUpdateInfo(true).getVideoMode(packageName);
    }

    public boolean onRecordList(String pkg) {
        return getUpdateInfo(true).onRecordList(pkg);
    }

    public boolean onTPWhiteList(String pkg) {
        return getUpdateInfo(true).onTPWhiteList(pkg);
    }

    public boolean supportTPRrate() {
        return getUpdateInfo(true).supportTPRrate();
    }

    /* access modifiers changed from: package-private */
    public void dataChange() {
        if (DEBUG) {
            Slog.d(TAG, " dataChange callback = " + this.mDataChangeCallBack);
        }
        Callback callback = this.mDataChangeCallBack;
        if (callback != null) {
            callback.onDataChange();
        }
    }

    /* access modifiers changed from: package-private */
    public void setCallback(Callback callback) {
        this.mDataChangeCallBack = callback;
    }
}
