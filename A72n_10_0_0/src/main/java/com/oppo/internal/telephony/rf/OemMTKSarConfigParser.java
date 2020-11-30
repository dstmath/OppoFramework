package com.oppo.internal.telephony.rf;

import android.os.Environment;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.OemConstant;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class OemMTKSarConfigParser {
    private static final boolean DBG = OemConstant.SWITCH_LOG;
    private static final String LOG_TAG = "OemMTKSarConfigParser";
    private static final String REGION_DEFAULT = "CE";
    private static final String SAR_CONFIG_FILE_PATH = "etc/sar_config.xml";
    private static OemMTKSarConfigParser instance = null;
    private Map<Integer, Map<Integer, Integer>> mCmdIndexMap = new LinkedHashMap();
    private Map<Integer, ArrayList<String[]>> mCmdValueRealSimMap = new HashMap();
    private Map<Integer, ArrayList<String[]>> mCmdValueTestSimMap = new HashMap();
    private String mProduct;
    private String mRegion;
    private int mXmlScenariosBit = 0;

    public static synchronized OemMTKSarConfigParser getInstance() {
        OemMTKSarConfigParser oemMTKSarConfigParser;
        synchronized (OemMTKSarConfigParser.class) {
            if (instance == null) {
                instance = new OemMTKSarConfigParser();
            }
            oemMTKSarConfigParser = instance;
        }
        return oemMTKSarConfigParser;
    }

    private OemMTKSarConfigParser() {
        initSettings();
    }

    public void initSettings() {
        this.mProduct = SystemProperties.get("ro.product.hw", "oppo");
        this.mRegion = SystemProperties.get("ro.oppo.regionmark", "None");
        this.mXmlScenariosBit = 0;
        this.mCmdIndexMap.clear();
        this.mCmdValueRealSimMap.clear();
        this.mCmdValueTestSimMap.clear();
        parse();
        if (DBG) {
            printDebug();
        }
    }

    private void printDebug() {
        Rlog.d(LOG_TAG, "============ print mXmlScenariosBit start ============");
        Rlog.d(LOG_TAG, "mXmlScenariosBit: 0b " + Integer.toBinaryString(this.mXmlScenariosBit));
        if (!this.mCmdIndexMap.isEmpty()) {
            Rlog.d(LOG_TAG, "============ print mCmdIndexMap start ============");
            for (Map.Entry<Integer, Map<Integer, Integer>> entryMask : this.mCmdIndexMap.entrySet()) {
                Integer keyMask = entryMask.getKey();
                for (Map.Entry<Integer, Integer> entryIndex : entryMask.getValue().entrySet()) {
                    Rlog.d(LOG_TAG, "<" + Integer.toBinaryString(keyMask.intValue()) + ", <" + entryIndex.getKey() + ", " + entryIndex.getValue() + ">>");
                }
            }
        }
        if (!this.mCmdValueRealSimMap.isEmpty()) {
            Rlog.d(LOG_TAG, "============ print mCmdValueRealSimMap start ============");
            for (Map.Entry<Integer, ArrayList<String[]>> entryCmd : this.mCmdValueRealSimMap.entrySet()) {
                Integer keyCmd = entryCmd.getKey();
                Iterator<String[]> it = entryCmd.getValue().iterator();
                while (it.hasNext()) {
                    String[] cmd = it.next();
                    Rlog.d(LOG_TAG, "<" + keyCmd + ": {\"" + cmd[0] + "\", \"" + cmd[1] + "\"}>");
                }
            }
        }
        if (!this.mCmdValueTestSimMap.isEmpty()) {
            Rlog.d(LOG_TAG, "============ print mCmdValueTestSimMap start ============");
            for (Map.Entry<Integer, ArrayList<String[]>> entryCmd2 : this.mCmdValueTestSimMap.entrySet()) {
                Integer keyCmd2 = entryCmd2.getKey();
                Iterator<String[]> it2 = entryCmd2.getValue().iterator();
                while (it2.hasNext()) {
                    String[] cmd2 = it2.next();
                    Rlog.d(LOG_TAG, "<" + keyCmd2 + ": {\"" + cmd2[0] + "\", \"" + cmd2[1] + "\"}>");
                }
            }
        }
    }

    public File getOppoProductDirectory() {
        try {
            Method method = Environment.class.getMethod("getOppoProductDirectory", new Class[0]);
            method.setAccessible(true);
            Object product = method.invoke(null, new Object[0]);
            if (product != null) {
                return (File) product;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parse() {
        FileReader sarConfigReader = null;
        File oppoProductDirectory = getOppoProductDirectory();
        if (oppoProductDirectory != null) {
            try {
                FileReader sarConfigReader2 = new FileReader(oppoProductDirectory + "/" + SAR_CONFIG_FILE_PATH);
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
                parser.setInput(sarConfigReader2);
                if (DBG) {
                    Rlog.d(LOG_TAG, "============ start parsing============");
                }
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0) {
                        if (eventType == 2) {
                            if (DBG) {
                                Rlog.d(LOG_TAG, "Start tag [" + parser.getName() + "]");
                            }
                            String startTag = parser.getName();
                            if ("Product".equalsIgnoreCase(startTag)) {
                                readProduct(parser);
                            } else if ("AllRegions".equalsIgnoreCase(startTag)) {
                                readAllRegions(parser);
                            } else if ("Region".equalsIgnoreCase(startTag)) {
                                readRegion(parser);
                            } else if ("Scenarios".equalsIgnoreCase(startTag)) {
                                readScenarios(parser);
                            } else if ("Scene".equalsIgnoreCase(startTag)) {
                                readScene(parser);
                            } else if ("Data".equalsIgnoreCase(startTag)) {
                                readData(parser);
                            }
                        }
                    } else if (DBG) {
                        Rlog.d(LOG_TAG, "Start document");
                    }
                }
                if (DBG) {
                    Rlog.d(LOG_TAG, "============ end parsing============");
                }
                try {
                    sarConfigReader2.close();
                } catch (IOException e) {
                }
            } catch (FileNotFoundException e2) {
                if (DBG) {
                    Rlog.d(LOG_TAG, "Couldn't find or open config file " + oppoProductDirectory + "/" + SAR_CONFIG_FILE_PATH);
                }
                if (0 != 0) {
                    sarConfigReader.close();
                }
            } catch (Exception e3) {
                e3.printStackTrace();
                if (0 != 0) {
                    sarConfigReader.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        sarConfigReader.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } else if (DBG) {
            Rlog.d(LOG_TAG, "getOppoProductDirectory fail");
        }
    }

    private void readProduct(XmlPullParser parser) throws XmlPullParserException, IOException {
        String xmlProductString = parser.getAttributeValue(null, "name");
        if (DBG) {
            Rlog.d(LOG_TAG, "[Product] xmlProductString = " + xmlProductString + ", mProduct = " + this.mProduct);
        }
        if (xmlProductString != null) {
            String[] xmlProduct = xmlProductString.split(":");
            boolean found = false;
            int length = xmlProduct.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (this.mProduct.equalsIgnoreCase(xmlProduct[i].trim())) {
                    found = true;
                    break;
                }
                i++;
            }
            if (!found) {
                skip(parser);
                return;
            }
            return;
        }
        throw new IllegalStateException();
    }

    private void readAllRegions(XmlPullParser parser) throws XmlPullParserException, IOException {
        String xmlAllRegionString = parser.getAttributeValue(null, "name");
        String xmlDefaultRegion = parser.nextText();
        if (DBG) {
            Rlog.d(LOG_TAG, "[AllRegions] xmlAllRegionString = " + xmlAllRegionString + ", mRegion = " + this.mRegion);
        }
        if (xmlAllRegionString != null) {
            String[] xmlRegions = xmlAllRegionString.split(":");
            boolean found = false;
            int length = xmlRegions.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (this.mRegion.equalsIgnoreCase(xmlRegions[i].trim())) {
                    found = true;
                    break;
                }
                i++;
            }
            if (!found) {
                if (xmlDefaultRegion == null || xmlDefaultRegion.isEmpty()) {
                    this.mRegion = REGION_DEFAULT;
                } else {
                    this.mRegion = xmlDefaultRegion.trim();
                }
            }
            if (DBG) {
                Rlog.d(LOG_TAG, "[AllRegions] after handled default region, mRegion = " + this.mRegion);
                return;
            }
            return;
        }
        throw new IllegalStateException();
    }

    private void readRegion(XmlPullParser parser) throws XmlPullParserException, IOException {
        String xmlRegionString = parser.getAttributeValue(null, "name");
        if (DBG) {
            Rlog.d(LOG_TAG, "[Region] xmlRegionString = " + xmlRegionString + ", mRegion = " + this.mRegion);
        }
        if (xmlRegionString != null) {
            String[] xmlRegion = xmlRegionString.split(":");
            boolean found = false;
            int length = xmlRegion.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (this.mRegion.equalsIgnoreCase(xmlRegion[i].trim())) {
                    found = true;
                    break;
                }
                i++;
            }
            if (!found) {
                skip(parser);
                return;
            }
            return;
        }
        throw new IllegalStateException();
    }

    private void readScenarios(XmlPullParser parser) throws XmlPullParserException, IOException {
        String xmlScenariosText = parser.nextText();
        this.mXmlScenariosBit = stringToInt(xmlScenariosText, 2, 0);
        if (DBG) {
            Rlog.d(LOG_TAG, "[Scenarios] xmlScenariosText = " + xmlScenariosText + ", mXmlScenariosBit = " + this.mXmlScenariosBit);
        }
    }

    private void readScene(XmlPullParser parser) throws XmlPullParserException, IOException {
        String xmlSceneString = parser.getAttributeValue(null, "mask");
        int mask = -1;
        Map<Integer, Integer> sceneMap = new LinkedHashMap<>();
        if (xmlSceneString != null) {
            mask = stringToInt(xmlSceneString, 2, -1);
        }
        int eventType = parser.getEventType();
        while (true) {
            if (eventType != 3 || (eventType == 3 && !"Scene".equalsIgnoreCase(parser.getName()))) {
                if (eventType == 2 && "Sce".equalsIgnoreCase(parser.getName())) {
                    int index = stringToInt(parser.getAttributeValue(null, "index"), 10, -1);
                    int indexData = stringToInt(parser.nextText(), 10, -1);
                    if (!(index == -1 || indexData == -1)) {
                        sceneMap.put(Integer.valueOf(index), Integer.valueOf(indexData));
                    }
                }
                eventType = parser.next();
            }
        }
        if (mask != -1 && !sceneMap.isEmpty()) {
            this.mCmdIndexMap.put(Integer.valueOf(mask), sceneMap);
        }
        if (DBG) {
            Rlog.d(LOG_TAG, "[Scene&Sce] xmlSceneString = " + xmlSceneString + ", mCmdIndexMap = " + this.mCmdIndexMap);
        }
    }

    private void readData(XmlPullParser parser) throws XmlPullParserException, IOException {
        int index;
        int eType;
        while (true) {
            int eventType = parser.next();
            int eType2 = 2;
            if (eventType == 2 && "Id".equalsIgnoreCase(parser.getName())) {
                ArrayList<String[]> cmdRealSimArrayList = new ArrayList<>();
                ArrayList<String[]> cmdTestSimArrayList = new ArrayList<>();
                String str = null;
                int index2 = stringToInt(parser.getAttributeValue(null, "index"), 10, -1);
                while (true) {
                    int eType3 = parser.next();
                    if (eType3 == eType2) {
                        String xmlRatString = parser.getName();
                        String xmlTypeString = parser.getAttributeValue(str, "type");
                        String xmlBandString = parser.getAttributeValue(str, "band");
                        String valueText = parser.nextText();
                        eType = eType3;
                        index = index2;
                        String[] cmdString = formatCmdString(false, xmlRatString, xmlTypeString, xmlBandString, valueText);
                        if (cmdString != null && cmdString.length > 0) {
                            cmdRealSimArrayList.add(cmdString);
                        }
                        String[] cmdString2 = formatCmdString(true, xmlRatString, xmlTypeString, xmlBandString, valueText);
                        if (cmdString2 != null && cmdString2.length > 0) {
                            cmdTestSimArrayList.add(cmdString2);
                        }
                    } else {
                        eType = eType3;
                        index = index2;
                    }
                    if (eType != 3) {
                        index2 = index;
                    } else if (eType == 3 && !"Id".equalsIgnoreCase(parser.getName())) {
                        index2 = index;
                    }
                    eType2 = 2;
                    str = null;
                }
                if (index != -1) {
                    if (!cmdRealSimArrayList.isEmpty()) {
                        this.mCmdValueRealSimMap.put(Integer.valueOf(index), cmdRealSimArrayList);
                    }
                    if (!cmdTestSimArrayList.isEmpty()) {
                        this.mCmdValueTestSimMap.put(Integer.valueOf(index), cmdTestSimArrayList);
                    }
                }
            }
            if (eventType != 3 || (eventType == 3 && !"Data".equalsIgnoreCase(parser.getName()))) {
            }
        }
        if (DBG) {
            Rlog.d(LOG_TAG, "[Data] mCmdValueRealSimMap = " + this.mCmdValueRealSimMap + ", mCmdValueTestSimMap = " + this.mCmdValueTestSimMap);
        }
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() == 2) {
            int depth = 1;
            while (depth != 0) {
                int next = parser.next();
                if (next == 2) {
                    depth++;
                } else if (next == 3) {
                    depth--;
                }
            }
            return;
        }
        throw new IllegalStateException();
    }

    private static int stringToInt(String s, int radix, int invalid) {
        if (s == null) {
            return invalid;
        }
        try {
            return Integer.parseInt(s.trim(), radix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return invalid;
        }
    }

    private static double stringToDouble(String s, int invalid) {
        double result = (double) invalid;
        if (s == null) {
            return result;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return (double) invalid;
        }
    }

    /* access modifiers changed from: package-private */
    public String[] formatCmdString(boolean simType, String rat, String cmdType, String bandString, String valueText) {
        String[] cmd = {"", ""};
        if ("sar".equalsIgnoreCase(cmdType)) {
            cmd = formatSarCmdString(simType, rat, bandString, valueText);
        } else if ("force_antenna".equalsIgnoreCase(cmdType)) {
            cmd = formatForceAntCmdString(simType, rat, bandString, valueText);
        }
        boolean valid = false;
        if (cmd != null) {
            for (int i = 0; i < cmd.length; i++) {
                if (cmd[i] != null && !cmd[i].isEmpty()) {
                    valid = true;
                }
            }
        }
        if (!valid) {
            return null;
        }
        return cmd;
    }

    /* access modifiers changed from: package-private */
    public String[] formatSarCmdString(boolean testSim, String rat, String bandString, String valueText) {
        String[] result = {"", ""};
        if (rat == null || valueText == null) {
            return result;
        }
        String SAR_WCDMA_PREFIX = "AT+ERFTX=10,2,";
        String SAR_TDSCDMA_PREFIX = "T_AT+ERFTX=10,2,";
        String SAR_GSM_PREFIX = "AT+ERFTX=10,1,";
        if ("All".equalsIgnoreCase(rat)) {
            result[0] = "AT+ERFTX=1,0,0,0";
        } else if ("Dsi".equalsIgnoreCase(rat)) {
            String dsiIndex = valueText.trim();
            result[0] = "AT+ERFIDX=1," + dsiIndex;
        } else {
            String str = ",";
            if ("Lte".equalsIgnoreCase(rat)) {
                String[] values = valueText.split(str);
                if (values.length != 2) {
                    return result;
                }
                String strValue0 = String.valueOf((int) Math.ceil(stringToDouble(values[0].trim(), 0) * 8.0d));
                String strValue1 = String.valueOf((int) Math.ceil(stringToDouble(values[1].trim(), 0) * 8.0d));
                result[0] = "AT+ERFTX=10,3," + bandString + str + strValue0 + str + strValue1;
            } else if ("Wcdma".equalsIgnoreCase(rat)) {
                String[] bands = bandString.split(str);
                String[] values2 = valueText.split(str);
                if (bands.length * 2 != values2.length) {
                    return result;
                }
                String[][] strValues = (String[][]) Array.newInstance(String.class, 20 * 2, 2);
                int i = 0;
                while (i < 20 * 2) {
                    for (int j = 0; j < 2; j++) {
                        strValues[i][j] = "";
                    }
                    i++;
                    SAR_WCDMA_PREFIX = SAR_WCDMA_PREFIX;
                }
                int k = 0;
                while (k < bands.length) {
                    int band = stringToInt(bands[k], 10, 1);
                    String strValue02 = String.valueOf((int) Math.ceil(stringToDouble(values2[k * 2], 0) * 8.0d));
                    String strValue12 = String.valueOf((int) Math.ceil(stringToDouble(values2[(k * 2) + 1], 0) * 8.0d));
                    strValues[band - 1][0] = strValue02;
                    strValues[band - 1][1] = strValue02;
                    strValues[(band - 1) + 20][0] = strValue12;
                    strValues[(band - 1) + 20][1] = strValue12;
                    k++;
                    SAR_TDSCDMA_PREFIX = SAR_TDSCDMA_PREFIX;
                    SAR_GSM_PREFIX = SAR_GSM_PREFIX;
                }
                StringBuilder builder = new StringBuilder();
                for (int m = 0; m < 20 * 2; m++) {
                    for (int n = 0; n < 2; n++) {
                        builder.append(strValues[m][n]);
                        builder.append(str);
                    }
                }
                builder.deleteCharAt(builder.length() - 1);
                result[0] = "AT+ERFTX=10,2," + builder.toString();
            } else if ("Tdscdma".equalsIgnoreCase(rat)) {
                String[] bands2 = bandString.split(str);
                String[] values3 = valueText.split(str);
                if (bands2.length * 2 != values3.length) {
                    return result;
                }
                String[][] strValues2 = (String[][]) Array.newInstance(String.class, 20 * 2, 2);
                for (int i2 = 0; i2 < 20 * 2; i2++) {
                    for (int j2 = 0; j2 < 2; j2++) {
                        strValues2[i2][j2] = "";
                    }
                }
                int k2 = 0;
                while (k2 < bands2.length) {
                    int band2 = stringToInt(bands2[k2], 10, 1);
                    String strValue03 = String.valueOf((int) Math.ceil(stringToDouble(values3[k2 * 2], 0) * 8.0d));
                    String strValue13 = String.valueOf((int) Math.ceil(stringToDouble(values3[(k2 * 2) + 1], 0) * 8.0d));
                    strValues2[band2 - 1][0] = strValue03;
                    strValues2[band2 - 1][1] = strValue03;
                    strValues2[(band2 - 1) + 20][0] = strValue13;
                    strValues2[(band2 - 1) + 20][1] = strValue13;
                    k2++;
                    bands2 = bands2;
                }
                StringBuilder builder2 = new StringBuilder();
                for (int m2 = 0; m2 < 20 * 2; m2++) {
                    for (int n2 = 0; n2 < 2; n2++) {
                        builder2.append(strValues2[m2][n2]);
                        builder2.append(str);
                    }
                }
                builder2.deleteCharAt(builder2.length() - 1);
                result[0] = "T_AT+ERFTX=10,2," + builder2.toString();
            } else if ("Gsm".equalsIgnoreCase(rat)) {
                String[] bands3 = bandString.split(str);
                String[] values4 = valueText.split(str);
                if (bands3.length * 2 != values4.length) {
                    return result;
                }
                String[][][] strValues3 = (String[][][]) Array.newInstance(String.class, 4 * 2, 2, 4);
                for (int i3 = 0; i3 < 4 * 2; i3++) {
                    for (int j3 = 0; j3 < 2; j3++) {
                        for (int k3 = 0; k3 < 4; k3++) {
                            strValues3[i3][j3][k3] = "0";
                        }
                    }
                }
                int m3 = 0;
                while (m3 < bands3.length) {
                    int band3 = stringToInt(bands3[m3], 10, 1);
                    String strValue04 = String.valueOf((int) Math.ceil(stringToDouble(values4[m3 * 2], 0) * 8.0d));
                    String strValue14 = String.valueOf((int) Math.ceil(stringToDouble(values4[(m3 * 2) + 1], 0) * 8.0d));
                    for (int o = 0; o < 2; o++) {
                        for (int p = 0; p < 4; p++) {
                            strValues3[band3 - 1][o][p] = strValue04;
                            strValues3[(band3 - 1) + 4][o][p] = strValue14;
                        }
                    }
                    m3++;
                    str = str;
                }
                StringBuilder builder3 = new StringBuilder();
                for (int m4 = 0; m4 < 4 * 2; m4++) {
                    for (int n3 = 0; n3 < 2; n3++) {
                        for (int o2 = 0; o2 < 4; o2++) {
                            builder3.append(strValues3[m4][n3][o2]);
                            builder3.append(str);
                        }
                    }
                }
                builder3.deleteCharAt(builder3.length() - 1);
                result[0] = "AT+ERFTX=10,1," + builder3.toString();
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public String[] formatForceAntCmdString(boolean testSim, String rat, String band, String valueText) {
        String[] result = {"", ""};
        if (rat == null || valueText == null || testSim) {
            return result;
        }
        if ("Gsm".equalsIgnoreCase(rat)) {
            if ("0".equals(valueText) || "1".equals(valueText)) {
                result[0] = "AT+ETXANT=4,1," + valueText + "," + band;
            } else {
                result[0] = "AT+ETXANT=3,1,," + band;
            }
        } else if ("Wcdma".equalsIgnoreCase(rat)) {
            if ("0".equals(valueText) || "1".equals(valueText)) {
                result[0] = "AT+ETXANT=4,2," + valueText + "," + band;
            } else {
                result[0] = "AT+ETXANT=3,2,," + band;
            }
        } else if ("Lte".equalsIgnoreCase(rat)) {
            if ("0".equals(valueText) || "1".equals(valueText)) {
                result[0] = "AT+ETXANT=4,3," + valueText + "," + band;
            } else {
                result[0] = "AT+ETXANT=3,3,," + band;
            }
        }
        return result;
    }

    public int getXmlScenariosBit() {
        return this.mXmlScenariosBit;
    }

    public ArrayList<String[]> getFinalCmdbyScene(int scene, boolean isFdd, boolean isTestSim) {
        Map<Integer, ArrayList<String[]>> cmdMap;
        int sce;
        ArrayList<String[]> cmd;
        char c;
        ArrayList<String[]> cmdArrayList = new ArrayList<>();
        if (!isTestSim) {
            cmdMap = this.mCmdValueRealSimMap;
        } else {
            cmdMap = this.mCmdValueTestSimMap;
        }
        if (!this.mCmdIndexMap.isEmpty() && !cmdMap.isEmpty()) {
            for (Map.Entry<Integer, Map<Integer, Integer>> entryMask : this.mCmdIndexMap.entrySet()) {
                Integer keyMask = entryMask.getKey();
                Map<Integer, Integer> valueMask = entryMask.getValue();
                char c2 = 0;
                try {
                    sce = Integer.valueOf(keyMask.intValue() & scene);
                } catch (NumberFormatException e) {
                    sce = 0;
                }
                Integer dataIndex = valueMask.get(sce);
                if (!(dataIndex == null || (cmd = cmdMap.get(dataIndex)) == null || cmd.size() <= 0)) {
                    Iterator<String[]> it = cmd.iterator();
                    while (it.hasNext()) {
                        String[] c3 = it.next();
                        if (c3[c2].startsWith("AT+ERFTX=10,2,")) {
                            if (!isFdd) {
                                dataIndex = dataIndex;
                                c2 = 0;
                            }
                        } else if (c3[0].startsWith("T_AT+ERFTX=10,2,")) {
                            if (isFdd) {
                                dataIndex = dataIndex;
                                c2 = 0;
                            } else {
                                c3[0] = c3[0].substring(2);
                            }
                        }
                        cmdArrayList.add(c3);
                        if (DBG) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("getFinalCmdbyScene, scene=");
                            sb.append(scene);
                            sb.append(", sce=");
                            sb.append(sce);
                            sb.append(", c[0]=\"");
                            c = 0;
                            sb.append(c3[0]);
                            sb.append("\"");
                            Rlog.d(LOG_TAG, sb.toString());
                        } else {
                            c = 0;
                        }
                        c2 = c;
                        dataIndex = dataIndex;
                    }
                }
            }
        }
        return cmdArrayList;
    }
}
