package com.android.server.display.ai.utils;

import android.text.TextUtils;
import android.util.Xml;
import com.android.server.display.ai.bean.BrightnessPoint;
import com.android.server.display.ai.broadcastreceiver.AIBrightnessTrainSwitch;
import com.android.server.display.ai.utils.BrightnessConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class DataHelper {
    private static final String TAG = "DataHelper";
    private static volatile DataHelper sDataHelper;

    private DataHelper() {
    }

    public static DataHelper getInstance() {
        if (sDataHelper == null) {
            synchronized (DataHelper.class) {
                if (sDataHelper == null) {
                    sDataHelper = new DataHelper();
                }
            }
        }
        return sDataHelper;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d6, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d7, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00da, code lost:
        throw r4;
     */
    public synchronized Map<String, List<BrightnessPoint>> getTrainedAppSplinesFromXml() {
        Map<String, List<BrightnessPoint>> appSpline;
        appSpline = null;
        File file = new File("/data/oppo/coloros/deepthinker/brightness/spline/spline.xml");
        if (!file.exists()) {
            ColorAILog.d(TAG, "No trained app splines available!");
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, "utf-8");
            String pkgName = null;
            List<BrightnessPoint> points = null;
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String tagName = parser.getName();
                if (eventType != 2) {
                    if (eventType == 3) {
                        if (BrightnessConstants.AppSplineXml.TAG_PACKAGE.equals(tagName)) {
                            if (points != null && points.size() > 0 && !TextUtils.isEmpty(pkgName)) {
                                if (appSpline == null) {
                                    appSpline = new HashMap<>();
                                }
                                ColorAILog.d(TAG, "add trained spline:" + pkgName);
                                appSpline.put(pkgName, points);
                            }
                            points = null;
                        }
                    }
                } else if (BrightnessConstants.AppSplineXml.TAG_PACKAGE.equals(tagName)) {
                    pkgName = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
                } else if (BrightnessConstants.AppSplineXml.TAG_POINT.equals(tagName)) {
                    String pointStr = parser.nextText();
                    if (!TextUtils.isEmpty(pointStr)) {
                        BrightnessPoint point = new BrightnessPoint();
                        String[] pointArray = pointStr.split(",");
                        if (pointArray.length == 2) {
                            point.x = Float.valueOf(pointArray[0]).floatValue();
                            point.y = Float.valueOf(pointArray[1]).floatValue();
                            if (points == null) {
                                points = new ArrayList<>();
                            }
                            points.add(point);
                        }
                    }
                }
            }
            $closeResource(null, fis);
        } catch (Exception e) {
            ColorAILog.e(TAG, "getTrainedAppSplinesFromXml Exception: " + e.getMessage());
        }
        return appSpline;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.io.FileOutputStream.<init>(java.lang.String, boolean):void throws java.io.FileNotFoundException}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException}
      ClspMth{java.io.FileOutputStream.<init>(java.lang.String, boolean):void throws java.io.FileNotFoundException} */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0140, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0141, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0144, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x01b8, code lost:
        return;
     */
    public synchronized void saveAppSplineToXml(Map<String, List<BrightnessPoint>> points) {
        if (!AIBrightnessTrainSwitch.getInstance().isTrainEnable()) {
            ColorAILog.d(TAG, "Failed to saveAppSplineToXml for the train switch is disabled");
        } else if (points != null && !points.isEmpty()) {
            File file = new File(BrightnessConstants.AppSplineXml.PATH);
            if (file.exists() || file.mkdirs()) {
                try {
                    BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/data/oppo/coloros/deepthinker/brightness/spline/spline.xml", true), StandardCharsets.UTF_8));
                    XmlSerializer xmlSerializer = Xml.newSerializer();
                    StringWriter writer = new StringWriter();
                    xmlSerializer.setOutput(writer);
                    xmlSerializer.startDocument("UTF-8", true);
                    xmlSerializer.startTag(null, BrightnessConstants.AppSplineXml.TAG_APP);
                    xmlSerializer.startTag(null, BrightnessConstants.AppSplineXml.TAG_TRAIN_TIME);
                    xmlSerializer.text(System.currentTimeMillis() + "");
                    xmlSerializer.endTag(null, BrightnessConstants.AppSplineXml.TAG_TRAIN_TIME);
                    xmlSerializer.startTag(null, BrightnessConstants.AppSplineXml.TAG_VERIFIED);
                    xmlSerializer.text(System.currentTimeMillis() + "");
                    xmlSerializer.endTag(null, BrightnessConstants.AppSplineXml.TAG_VERIFIED);
                    ColorAILog.d(TAG, "save splines to:/data/oppo/coloros/deepthinker/brightness/spline/spline.xml");
                    for (Map.Entry<String, List<BrightnessPoint>> entry : points.entrySet()) {
                        String pkgName = entry.getKey();
                        xmlSerializer.startTag(null, BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                        xmlSerializer.attribute(null, BrightnessConstants.AppSplineXml.TAG_NAME, pkgName);
                        for (BrightnessPoint point : entry.getValue()) {
                            xmlSerializer.startTag(null, BrightnessConstants.AppSplineXml.TAG_POINT);
                            xmlSerializer.text(Math.round(point.x) + "," + Math.round(point.y));
                            xmlSerializer.endTag(null, BrightnessConstants.AppSplineXml.TAG_POINT);
                        }
                        xmlSerializer.endTag(null, BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                        ColorAILog.d(TAG, pkgName);
                    }
                    xmlSerializer.endTag(null, BrightnessConstants.AppSplineXml.TAG_APP);
                    xmlSerializer.endDocument();
                    xmlSerializer.flush();
                    fileWriter.write(writer.toString());
                    $closeResource(null, fileWriter);
                } catch (FileNotFoundException e) {
                    ColorAILog.e(TAG, "saveAppSplineToXml FileNotFoundException: " + e.getMessage());
                } catch (IllegalArgumentException e2) {
                    ColorAILog.e(TAG, "saveAppSplineToXml IllegalArgumentException1: " + e2.getMessage());
                } catch (IllegalStateException e3) {
                    ColorAILog.e(TAG, "saveAppSplineToXml IllegalArgumentException2: " + e3.getMessage());
                } catch (IOException e4) {
                    ColorAILog.e(TAG, "saveAppSplineToXml IOException: " + e4.getMessage());
                }
            } else {
                ColorAILog.e(TAG, "error to mkdir");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002d, code lost:
        return;
     */
    public synchronized void deleteAppSpline() {
        File file = new File("/data/oppo/coloros/deepthinker/brightness/spline/spline.xml");
        if (!file.exists()) {
            ColorAILog.w(TAG, "Failed to delete spline.xml for it doesn't exist.");
        } else if (file.delete()) {
            ColorAILog.d(TAG, "Delete spline.xml successfully.");
        } else {
            ColorAILog.w(TAG, "Oops! Failed to delete spline.xml.");
        }
    }
}
