package com.android.server.wm;

import android.content.Context;
import android.os.OppoBaseEnvironment;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.oppo.OppoRomUpdateHelper;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    int mRefreshRateFullspeed = -1;

    public interface Callback {
        void onDataChange();
    }

    /* access modifiers changed from: private */
    public void changeFilePermisson(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            boolean result = file.setReadable(true, DEBUG);
            Slog.i(TAG, "setReadable result :" + result);
            return;
        }
        Log.i(TAG, "filename :" + filename + " is not exist");
    }

    private class OppoScreenModeInfo extends OppoRomUpdateHelper.UpdateInfo {
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
        /* JADX WARNING: Removed duplicated region for block: B:104:0x0327 A[Catch:{ IOException -> 0x0322 }] */
        /* JADX WARNING: Removed duplicated region for block: B:110:0x033a A[SYNTHETIC, Splitter:B:110:0x033a] */
        /* JADX WARNING: Removed duplicated region for block: B:115:0x0343 A[Catch:{ IOException -> 0x033e }] */
        /* JADX WARNING: Removed duplicated region for block: B:120:0x0351 A[SYNTHETIC, Splitter:B:120:0x0351] */
        /* JADX WARNING: Removed duplicated region for block: B:125:0x035a A[Catch:{ IOException -> 0x0355 }] */
        /* JADX WARNING: Removed duplicated region for block: B:132:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:135:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:99:0x031e A[SYNTHETIC, Splitter:B:99:0x031e] */
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
                this.mPackageMap.clear();
                this.mActivityMap.clear();
                this.mGameMap.clear();
                this.mVideoMap.clear();
                this.mTPWhiteList.clear();
                this.mRecordList.clear();
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
                                        if (xmlReader != null) {
                                        }
                                        if (strReader != null) {
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (xmlReader != null) {
                                        }
                                        if (strReader != null) {
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    if (xmlReader != null) {
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
                                    } catch (XmlPullParserException e5) {
                                        e = e5;
                                        Slog.i(OppoScreenModeHelper.TAG, "Got execption parsing permissions.", e);
                                        if (xmlReader != null) {
                                        }
                                        if (strReader == null) {
                                        }
                                    } catch (IOException e6) {
                                        e2 = e6;
                                        Slog.i(OppoScreenModeHelper.TAG, "Got execption parsing permissions.", e2);
                                        if (xmlReader != null) {
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
                                    if (OppoScreenModeHelper.DEBUG) {
                                        Slog.d(OppoScreenModeHelper.TAG, "verion :" + parser.getText() + " type:" + tag);
                                    }
                                }
                            }
                        }
                        eventType2 = parser.next();
                        if (OppoScreenModeHelper.DEBUG) {
                            Slog.d(OppoScreenModeHelper.TAG, "initializing eventType:" + eventType2);
                        }
                        name2 = name;
                    }
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e7) {
                            Slog.i(OppoScreenModeHelper.TAG, "Got execption close permReader.", e7);
                        }
                    }
                    strReader.close();
                    if (OppoScreenModeHelper.DEBUG) {
                        Slog.d(OppoScreenModeHelper.TAG, "load data end ");
                    }
                    OppoScreenModeHelper.this.dataChange();
                } catch (XmlPullParserException e8) {
                    e = e8;
                    Slog.i(OppoScreenModeHelper.TAG, "Got execption parsing permissions.", e);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e9) {
                            Slog.i(OppoScreenModeHelper.TAG, "Got execption close permReader.", e9);
                            return;
                        }
                    }
                    if (strReader == null) {
                        strReader.close();
                    }
                } catch (IOException e10) {
                    e2 = e10;
                    Slog.i(OppoScreenModeHelper.TAG, "Got execption parsing permissions.", e2);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e11) {
                            Slog.i(OppoScreenModeHelper.TAG, "Got execption close permReader.", e11);
                            return;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (Throwable th4) {
                    th = th4;
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e12) {
                            Slog.i(OppoScreenModeHelper.TAG, "Got execption close permReader.", e12);
                            throw th;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                    throw th;
                }
            }
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

        public Integer getVideoMode(String componentname) {
            return this.mVideoMap.get(componentname);
        }

        public boolean onRecordList(String pkg) {
            return this.mRecordList.contains(pkg);
        }

        public boolean onTPWhiteList(String pkg) {
            return this.mTPWhiteList.contains(pkg);
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

    /* access modifiers changed from: private */
    public int speedFreshRateSwitch(int freshRate) {
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

    public Integer getVideoMode(String packageName) {
        return getUpdateInfo(true).getVideoMode(packageName);
    }

    public boolean onRecordList(String pkg) {
        return getUpdateInfo(true).onRecordList(pkg);
    }

    public boolean onTPWhiteList(String pkg) {
        return getUpdateInfo(true).onTPWhiteList(pkg);
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
