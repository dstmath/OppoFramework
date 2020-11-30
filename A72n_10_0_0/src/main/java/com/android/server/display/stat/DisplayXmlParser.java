package com.android.server.display.stat;

import android.content.Context;
import android.os.OppoBaseEnvironment;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.oppo.OppoRomUpdateHelper;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DisplayXmlParser extends OppoRomUpdateHelper {
    public static final String BACKLIGHT_STAT = "backlightStat";
    public static final String BACKLIGHT_STAT_APP_LEVELS = "appLevels";
    public static final String BACKLIGHT_STAT_DUR_LEVELS = "durLevels";
    public static final String BACKLIGHT_STAT_LUXS = "luxs";
    public static final String BACKLIGHT_STAT_MAX_LUX = "maxLux";
    public static final String BACKLIGHT_STAT_SUPPORT = "support";
    public static final String BACKLIGHT_STAT_VERSION = "version";
    public static final String DATA_FILE_DIR = "data/system/oppo_app_scale_list.xml";
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String FILTER_NAME = "oppo_app_scale_list";
    public static final String SYS_FILE_DIR = (OppoBaseEnvironment.getOppoProductDirectory().getAbsolutePath() + "/vendor/etc/oppo_app_scale_list.xml");
    private static final String TAG = "DisplayXmlParser";
    private static volatile DisplayXmlParser sDisplayXmlParser;
    private ArrayList<Integer> mAppLevels = new ArrayList<>();
    private Context mContext = null;
    private ArrayList<Integer> mDurLevels = new ArrayList<>();
    private ArrayList<Integer> mLuxLevels = new ArrayList<>();
    private int mMaxLux = 8600;
    private boolean mSPStat = false;
    private String mVer = null;

    interface Callback {
        void onChange();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeFilePermisson(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            boolean result = file.setReadable(true, false);
            Slog.i(TAG, "setReadable result :" + result);
            return;
        }
        Slog.i(TAG, "filename :" + filename + " is not exist");
    }

    private DisplayXmlParser(Context context) {
        super(context, "oppo_app_scale_list", SYS_FILE_DIR, DATA_FILE_DIR);
        this.mContext = context;
        setUpdateInfo(new DisplayUpdateInfo(), new DisplayUpdateInfo());
        try {
            init();
            changeFilePermisson(DATA_FILE_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getConfigVersion(String content, boolean isPath) {
        Reader xmlReader;
        StringBuilder sb;
        if (content == null) {
            return 0;
        }
        Slog.d(TAG, "getConfigVersion content length:" + content.length() + "," + isPath);
        long version = 0;
        if (isPath) {
            try {
                xmlReader = new FileReader(content);
            } catch (Exception e) {
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
                        String tagName = parser.getName().trim();
                        Slog.d(TAG, "getConfigVersion called  TagName:" + tagName);
                        if ("version".equals(tagName)) {
                            parser.next();
                            String text = parser.getText().trim();
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
                                Slog.e(TAG, "" + e3);
                            }
                            return version;
                        }
                    }
                }
            }
            try {
                xmlReader.close();
            } catch (IOException e4) {
                e = e4;
                sb = new StringBuilder();
            }
        } catch (XmlPullParserException e5) {
            Slog.e(TAG, "" + e5);
            version = 0;
            try {
                xmlReader.close();
            } catch (IOException e6) {
                e = e6;
                sb = new StringBuilder();
            }
        } catch (Exception e7) {
            Slog.e(TAG, "" + e7);
            version = 0;
            try {
                xmlReader.close();
            } catch (IOException e8) {
                e = e8;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            try {
                xmlReader.close();
            } catch (IOException e9) {
                Slog.e(TAG, "" + e9);
            }
            throw th;
        }
        return version;
        sb.append("");
        sb.append(e);
        Slog.e(TAG, sb.toString());
        return version;
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
        if (dataversion > sysversion) {
            parseContentFromXML(readFromFile(datafile));
        } else {
            parseContentFromXML(readFromFile(sysfile));
        }
    }

    public static DisplayXmlParser getInstance(Context context) {
        if (sDisplayXmlParser == null) {
            synchronized (DisplayXmlParser.class) {
                if (sDisplayXmlParser == null) {
                    sDisplayXmlParser = new DisplayXmlParser(context);
                }
            }
        }
        return sDisplayXmlParser;
    }

    private class DisplayUpdateInfo extends OppoRomUpdateHelper.UpdateInfo {
        private DisplayUpdateInfo() {
            super(DisplayXmlParser.this);
        }

        public void parseContentFromXML(String content) {
            if (content == null) {
                Slog.e(DisplayXmlParser.TAG, "parseContentFromXML content is null");
                return;
            }
            DisplayXmlParser.this.changeFilePermisson(DisplayXmlParser.DATA_FILE_DIR);
            StringReader strReader = null;
            String startTag = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                strReader = new StringReader(content);
                parser.setInput(strReader);
                int eventType = parser.getEventType();
                while (true) {
                    boolean z = true;
                    if (eventType == 1) {
                        break;
                    }
                    if (eventType == 0) {
                        Slog.i(DisplayXmlParser.TAG, "Start document");
                    } else if (eventType == 2) {
                        startTag = parser.getName();
                    } else if (eventType == 3) {
                        parser.getName();
                    } else if (eventType == 4) {
                        String text = parser.getText();
                        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(startTag)) {
                            if (startTag.equals(DisplayXmlParser.BACKLIGHT_STAT_APP_LEVELS)) {
                                DisplayXmlParser.this.mAppLevels.clear();
                                String[] texts = text.trim().split(",");
                                for (String str : texts) {
                                    DisplayXmlParser.this.mAppLevels.add(Integer.valueOf(Integer.parseInt(str)));
                                }
                            } else if (startTag.equals(DisplayXmlParser.BACKLIGHT_STAT_LUXS)) {
                                DisplayXmlParser.this.mLuxLevels.clear();
                                String[] texts2 = text.trim().split(",");
                                for (String str2 : texts2) {
                                    DisplayXmlParser.this.mLuxLevels.add(Integer.valueOf(Integer.parseInt(str2)));
                                }
                            } else if (startTag.equals(DisplayXmlParser.BACKLIGHT_STAT_DUR_LEVELS)) {
                                DisplayXmlParser.this.mDurLevels.clear();
                                String[] texts3 = text.trim().split(",");
                                for (String str3 : texts3) {
                                    DisplayXmlParser.this.mDurLevels.add(Integer.valueOf(Integer.parseInt(str3)));
                                }
                            } else if (startTag.equals("version")) {
                                DisplayXmlParser.this.mVer = text.trim();
                            } else if (startTag.equals(DisplayXmlParser.BACKLIGHT_STAT_MAX_LUX)) {
                                DisplayXmlParser.this.mMaxLux = Integer.parseInt(text.trim());
                            } else if (startTag.equals(DisplayXmlParser.BACKLIGHT_STAT_SUPPORT)) {
                                int spInt = Integer.parseInt(text.trim());
                                DisplayXmlParser displayXmlParser = DisplayXmlParser.this;
                                if (spInt != 1) {
                                    z = false;
                                }
                                displayXmlParser.mSPStat = z;
                            }
                        }
                        startTag = null;
                    }
                    eventType = parser.next();
                }
                Slog.i(DisplayXmlParser.TAG, "End document ver:" + DisplayXmlParser.this.mVer + " sp=" + DisplayXmlParser.this.mSPStat);
                if (DisplayXmlParser.DEBUG) {
                    Slog.d(DisplayXmlParser.TAG, " levs:" + DisplayXmlParser.this.mAppLevels + " lus:" + DisplayXmlParser.this.mLuxLevels + " durLevs:" + DisplayXmlParser.this.mDurLevels + " maxLux=" + DisplayXmlParser.this.mMaxLux);
                }
            } catch (XmlPullParserException e) {
                Slog.e(DisplayXmlParser.TAG, "parseContentFromXML", e);
                if (0 == 0) {
                    return;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                if (0 == 0) {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    strReader.close();
                }
                throw th;
            }
            strReader.close();
        }
    }

    public ArrayList<Integer> getBackLightStatAppLevels() {
        return this.mAppLevels;
    }

    public ArrayList<Integer> getBackLightStatLuxLevels() {
        return this.mLuxLevels;
    }

    public ArrayList<Integer> getBackLightStatDurLevels() {
        return this.mDurLevels;
    }

    public int getBackLightStatMaxLux() {
        return this.mMaxLux;
    }

    public String getVersion() {
        return this.mVer;
    }

    public boolean getBackLightStatSupport() {
        return this.mSPStat;
    }
}
