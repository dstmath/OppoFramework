package com.android.server.am;

import android.os.FileUtils;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.content.ColorRuleInfo;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ColorInterceptSettings {
    private static boolean DEBUG = DEBUG_PANIC;
    private static boolean DEBUG_PANIC = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static final String INTERCEPT_INTENT_FILE = "/data/oppo/coloros/trafficProtect/color_intercept_fw_config.xml";
    private static final String TAG = "CII_ColorInterceptSettings";
    private static final String TAG_RULE = "rule";
    private static final String TAG_RULE_ITEM = "item";
    private Object mSettingLock = new Object();
    private File mSettingsFile;

    public static void setDeugEnable(boolean enable) {
        DEBUG = enable;
    }

    public void writeLpr(List<ColorRuleInfo> infos) {
        this.mSettingsFile = initFile(INTERCEPT_INTENT_FILE);
        File file = this.mSettingsFile;
        if (file != null) {
            try {
                FileOutputStream fstr = new FileOutputStream(file);
                BufferedOutputStream str = new BufferedOutputStream(fstr);
                XmlSerializer serializer = new FastXmlSerializer();
                serializer.setOutput(str, StandardCharsets.UTF_8.name());
                serializer.startDocument(null, true);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, TAG_RULE);
                if (infos != null && infos.size() > 0) {
                    for (ColorRuleInfo info : infos) {
                        serializer.startTag(null, TAG_RULE_ITEM);
                        info.writeToXml(serializer);
                        serializer.endTag(null, TAG_RULE_ITEM);
                    }
                }
                serializer.endTag(null, TAG_RULE);
                serializer.endDocument();
                str.flush();
                FileUtils.sync(fstr);
                str.close();
            } catch (IOException e) {
                Slog.wtf(TAG, "Unable to write package manager settings, current changes will be lost at reboot", e);
            }
        }
    }

    public List<ColorRuleInfo> readLpw(File file) {
        Slog.i(TAG, "readLpw file = " + file.getName());
        List<ColorRuleInfo> infoList = new ArrayList<>();
        try {
            FileInputStream str = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(str, StandardCharsets.UTF_8.name());
            while (true) {
                int type = parser.next();
                if (type == 2 || type == 1) {
                    int outerDepth = parser.getDepth();
                }
            }
            int outerDepth2 = parser.getDepth();
            while (true) {
                int type2 = parser.next();
                if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth2)) {
                    break;
                } else if (type2 != 3) {
                    if (type2 != 4) {
                        String tagName = parser.getName();
                        if (DEBUG) {
                            Slog.i(TAG, "readLpw tagName = " + tagName);
                        }
                        if (tagName.equals(TAG_RULE_ITEM)) {
                            ColorRuleInfo info = new ColorRuleInfo();
                            info.readFromXml(parser);
                            infoList.add(info);
                        } else {
                            Slog.e(TAG, "unknow tagName = " + tagName);
                            XmlUtils.skipCurrentTag(parser);
                        }
                        str.close();
                    }
                }
            }
        } catch (XmlPullParserException e) {
            Slog.e(TAG, "XmlPullParserException readLpw");
        } catch (IOException e2) {
            Slog.e(TAG, "IOException readLpw");
        }
        return infoList;
    }

    private File initFile(String fileName) {
        File file = new File(fileName);
        try {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdir()) {
                Log.e(TAG, "initFile() " + fileName + " mkdir() failed");
                return null;
            } else if (file.exists() || file.createNewFile()) {
                return file;
            } else {
                Log.e(TAG, "init file failed: file-" + fileName);
                return null;
            }
        } catch (IOException e) {
            Slog.e(TAG, "can NOT create file : " + fileName);
            return null;
        }
    }

    public List<ColorRuleInfo> loadRuleList() {
        File file = new File(INTERCEPT_INTENT_FILE);
        Slog.i(TAG, " file = /data/oppo/coloros/trafficProtect/color_intercept_fw_config.xml exist = " + file.exists());
        if (file.exists()) {
            Slog.i(TAG, "load file rule list");
            return loadRuleListFromXml(file);
        }
        Slog.i(TAG, "load default rule list");
        return loadDefaultRuleList();
    }

    private List<ColorRuleInfo> loadDefaultRuleList() {
        return new ArrayList<>();
    }

    private List<ColorRuleInfo> loadRuleListFromXml(File file) {
        return readLpw(file);
    }

    public void dump(PrintWriter pw, String[] args, String prefix) {
        pw.print(prefix);
        pw.println("DEBUG = " + DEBUG);
        pw.print(prefix);
        pw.print("File Path = ");
        pw.println(INTERCEPT_INTENT_FILE);
    }
}
