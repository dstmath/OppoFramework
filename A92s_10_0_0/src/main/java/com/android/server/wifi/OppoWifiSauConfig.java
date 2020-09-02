package com.android.server.wifi;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.os.AtomicFile;
import com.android.server.wifi.OppoWiFiSauXml;
import com.android.server.wifi.util.XmlUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoWifiSauConfig {
    private static final int BUFFERED_WRITE_ALARM_INTERVAL_MS = 10000;
    private static final int CURRENT_CONFIG_STORE_DATA_VERSION = 1;
    private static boolean DBG = false;
    private static final int INITIAL_CONFIG_STORE_DATA_VERSION = 1;
    public static final String PLATFORM_MTK = "mtk";
    public static final String PLATFORM_NONE = "none";
    public static final String PLATFORM_QCOM = "qcom";
    private static final String SAU_CONFIG_FILE = "OppoWifiSAUConfig.xml";
    private static final String SAU_CONFIG_FILE_PATH = (SAU_FILE_DIR + SAU_CONFIG_FILE);
    private static final String SAU_FILE_DIR = (Environment.getDataDirectory().getPath() + "/misc/wifi/sau/");
    private static final String SAU_FINISH_FILE = "finish";
    private static final String SAU_FINISH_FILE_PATH = (SAU_FILE_DIR + SAU_FINISH_FILE);
    private static final String SAU_UPDATE_OBJ_VERSION_FILE = (Environment.getDataDirectory().getPath() + "/misc/wifi/sau/obj_version");
    private static final String TAG = "OppoWifiSauConfig";
    private static final String WIFI_SAU_UPDATE_TAG = "SAU-AUTO_LOAD_FW-10";
    public static final String XML_TAG_DOCUMENT_HEADER = "OppoWifiSauConfig";
    public static final String XML_TAG_SECTION_HEADER_CONFIG = "WifiSauObjConfig";
    public static final String XML_TAG_SECTION_HEADER_CONFIG_LIST = "WifiSauObjConfigList";
    public static final String XML_TAG_VERSION = "Version";
    private final Clock mClock;
    private Context mContext = null;
    List<String> mDefaultMtkCfgNameList = Arrays.asList("wifi.cfg");
    List<String> mDefaultMtkFwSoc1NameList = Arrays.asList("WIFI_RAM_CODE_soc1_0_2_1.bin", "soc1_0_ram_wifi_2_1_hdr.bin");
    List<String> mDefaultMtkFwSoc2NameList = Arrays.asList("WIFI_RAM_CODE_soc2_0_3b_1.bin", "soc2_0_ram_wifi_3b_1_hdr.bin", "soc2_0_ram_bt_3b_1_hdr.bin", "soc2_0_ram_mcu_3b_1_hdr.bin", "soc2_0_patch_mcu_3b_1_hdr.bin");
    List<String> mDefaultMtkFwSoc3NameList = Arrays.asList("WIFI_RAM_CODE_soc3_0_1_1.bin", "soc3_0_ram_wifi_1_1_hdr.bin", "soc3_0_ram_wmmcu_1_1_hdr.bin", "soc3_0_patch_wmmcu_1_1_hdr.bin");
    List<String> mDefaultMtkNvNameList = Arrays.asList("WIFI");
    HashMap<String, List<String>> mDefaultMtkObjsName = new HashMap<>();
    List<String> mDefaultQcomBdfNameList = Arrays.asList("bdwlan.bin");
    List<String> mDefaultQcomFwNameList = Arrays.asList("wlandsp.mbn");
    List<String> mDefaultQcomIniNameList = Arrays.asList("WCNSS_qcom_cfg.ini");
    HashMap<String, List<String>> mDefaultQcomObjsName = new HashMap<>();
    HashMap<String, HashMap<String, List<String>>> mDefaultSauObjsNameList = new HashMap<>();
    private StoreFile mStore;
    private LinkedList<StoreData> mUpdteDataList = null;

    public interface StoreData {
        void deserializeData(XmlPullParser xmlPullParser, int i, boolean z) throws XmlPullParserException, IOException;
    }

    public OppoWifiSauConfig(Context context) {
        this.mContext = context;
        this.mClock = new Clock();
        this.mStore = createFile(SAU_CONFIG_FILE_PATH);
        this.mUpdteDataList = new LinkedList<>();
        this.mDefaultMtkObjsName.put("wifi.cfg", this.mDefaultMtkCfgNameList);
        this.mDefaultMtkObjsName.put("wifi.fw.soc3", this.mDefaultMtkFwSoc3NameList);
        this.mDefaultMtkObjsName.put("wifi.fw.soc2", this.mDefaultMtkFwSoc2NameList);
        this.mDefaultMtkObjsName.put("wifi.fw.soc1", this.mDefaultMtkFwSoc1NameList);
        this.mDefaultMtkObjsName.put("wifi.nv", this.mDefaultMtkNvNameList);
        this.mDefaultSauObjsNameList.put(PLATFORM_MTK, this.mDefaultMtkObjsName);
        this.mDefaultQcomObjsName.put("wifi.ini", this.mDefaultQcomIniNameList);
        this.mDefaultQcomObjsName.put("wifi.fw", this.mDefaultQcomFwNameList);
        this.mDefaultQcomObjsName.put("wifi.bdf", this.mDefaultQcomBdfNameList);
        this.mDefaultSauObjsNameList.put(PLATFORM_QCOM, this.mDefaultQcomObjsName);
    }

    public void enableVerboseLogging(boolean verbose) {
        DBG = verbose;
    }

    public String getFinishFilePath() {
        return SAU_FINISH_FILE_PATH;
    }

    public String getConfigFilePath() {
        return SAU_CONFIG_FILE_PATH;
    }

    public String getConfigDir() {
        return SAU_FILE_DIR;
    }

    public String getFinishFname() {
        return SAU_FINISH_FILE;
    }

    public boolean registerStoreData(StoreData storeData) {
        if (storeData == null) {
            Log.e("OppoWifiSauConfig", "Unable to register null store data");
            return false;
        }
        this.mUpdteDataList.add(storeData);
        return true;
    }

    public void read() throws XmlPullParserException, IOException {
        long readStartTime = this.mClock.getElapsedSinceBootMillis();
        byte[] dataBytes = this.mStore.readRawData();
        long readTime = this.mClock.getElapsedSinceBootMillis() - readStartTime;
        if (DBG) {
            Log.d("OppoWifiSauConfig", "Reading from stores completed in " + readTime + " ms.");
        }
        deserializeData(dataBytes, true);
    }

    private void deserializeData(byte[] dataBytes, boolean shareData) throws XmlPullParserException, IOException {
        if (dataBytes == null) {
            Log.d("OppoWifiSauConfig", "no data need parse ");
            return;
        }
        XmlPullParser in = Xml.newPullParser();
        in.setInput(new ByteArrayInputStream(dataBytes), StandardCharsets.UTF_8.name());
        int rootTagDepth = in.getDepth() + 1;
        parseDocumentStartAndVersionFromXml(in);
        String[] headerName = new String[1];
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, rootTagDepth)) {
            if (!isValidSauConfigList(headerName[0])) {
                if (DBG) {
                    Log.d("OppoWifiSauConfig", " invalid SauConfigList = " + headerName[0]);
                }
                throw new XmlPullParserException("invalid SauConfigList = " + headerName[0]);
            }
            Log.d("OppoWifiSauConfig", " start parse wifi update obj list -----------------------> ");
            OppoWifiUpdateData wifiUpdateDate = new OppoWifiUpdateData(XML_TAG_SECTION_HEADER_CONFIG_LIST, XML_TAG_SECTION_HEADER_CONFIG);
            wifiUpdateDate.deserializeData(in, rootTagDepth + 1, shareData);
            registerStoreData(wifiUpdateDate);
            Log.d("OppoWifiSauConfig", " start parse wifi update obj list <----------------------- ");
        }
    }

    public boolean isValidSauConfigList(String configList) {
        if (configList.equals(XML_TAG_SECTION_HEADER_CONFIG_LIST)) {
            return true;
        }
        return false;
    }

    public boolean sauFileNameCheck(List<OppoWifiUpdateObj> updateObjList, List<String> defaultNameList) {
        List<String> tempCheckNameList = new ArrayList<>();
        for (OppoWifiUpdateObj updateObj : updateObjList) {
            if (updateObj.fileName.isEmpty()) {
                return false;
            }
            File fl = new File(SAU_FILE_DIR + updateObj.fileName);
            if (!tempCheckNameList.contains(updateObj.fileName) && defaultNameList.contains(updateObj.fileName) && isFileExist(fl)) {
                tempCheckNameList.add(updateObj.fileName);
                if (DBG) {
                    Log.d("OppoWifiSauConfig", "sauFileNameCheck OK, fileName = " + updateObj.fileName);
                }
            } else {
                if (DBG) {
                    Log.d("OppoWifiSauConfig", "sauFileNameCheck Faild and Return, fileName = " + updateObj.fileName);
                }
                return false;
            }
        }
        return tempCheckNameList.size() == defaultNameList.size();
    }

    public boolean sauConfigObjNameCheck(String platform, String fileType, List<OppoWifiUpdateObj> updateObjList) {
        HashMap<String, List<String>> defaultSauObjsNameList = this.mDefaultSauObjsNameList.get(platform);
        if (defaultSauObjsNameList != null) {
            List<String> defaultObjsName = defaultSauObjsNameList.get(fileType);
            if (defaultObjsName != null) {
                return sauFileNameCheck(updateObjList, defaultObjsName);
            }
            Log.d("OppoWifiSauConfig", "sauConfigObjNumCheck, ignored unknown fileType: " + fileType);
            return false;
        }
        Log.d("OppoWifiSauConfig", "sauConfigObjNumCheck, ignored unknown platform: " + platform);
        return false;
    }

    public boolean sauConfigObjMd5Check(List<OppoWifiUpdateObj> updateObjList) {
        for (OppoWifiUpdateObj updateObj : updateObjList) {
            if (updateObj.fileName.isEmpty()) {
                return false;
            }
            String md5_f = getFileMD5(new File(SAU_FILE_DIR + updateObj.fileName));
            if (TextUtils.isEmpty(md5_f) || !md5_f.equals(updateObj.md5)) {
                if (!DBG) {
                    return false;
                }
                Log.d("OppoWifiSauConfig", "sauConfigObjMd5Check failed and return, fileName = " + updateObj.fileName);
                return false;
            } else if (DBG) {
                Log.d("OppoWifiSauConfig", "sauConfigObjMd5Check OK, fileName = " + updateObj.fileName);
            }
        }
        return true;
    }

    public LinkedList<StoreData> sauValidCheck() {
        boolean result;
        String pushFailReason = PLATFORM_NONE;
        OppoWifiSauStatistics dcs = new OppoWifiSauStatistics(this.mContext);
        LinkedList<StoreData> linkedList = this.mUpdteDataList;
        LinkedList<StoreData> linkedList2 = null;
        if (linkedList == null || linkedList.isEmpty()) {
            Log.e("OppoWifiSauConfig", " UpdteDataList is null or empty");
            dcs.sauPushFailed(PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, "nullUpdteDataList");
            return null;
        }
        Iterator<StoreData> it = this.mUpdteDataList.iterator();
        while (it.hasNext()) {
            OppoWifiUpdateData updateDate = (OppoWifiUpdateData) it.next();
            if (updateDate == null) {
                Log.e("OppoWifiSauConfig", "UpdateDate is null");
                dcs.sauPushFailed(PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, "nullUpdteData");
                return linkedList2;
            }
            List<OppoWifiUpdateObj> updateObjList = updateDate.getUpdateObjList();
            if (updateObjList != null) {
                if (!updateObjList.isEmpty()) {
                    OppoWifiUpdateObj firstObj = updateObjList.get(0);
                    if (!sauConfigObjNameCheck(firstObj.platform, firstObj.fileType, updateObjList)) {
                        result = false;
                        pushFailReason = "nameCheckFail";
                    } else if (!sauConfigObjMd5Check(updateObjList)) {
                        result = false;
                        pushFailReason = "md5CheckFail";
                    } else {
                        result = true;
                    }
                    updateDate.setUpdateDateVaild(result);
                    updateDate.setUpdateDateDetail(firstObj.platform, firstObj.fileType, firstObj.fileName, firstObj.effectMethod, firstObj.pushReason, firstObj.pushTime, pushFailReason);
                    linkedList2 = null;
                }
            }
            Log.e("OppoWifiSauConfig", "updateObjList is null or empty ");
            dcs.sauPushFailed(PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, PLATFORM_NONE, "nullUpdteObjList");
            return null;
        }
        return this.mUpdteDataList;
    }

    private static boolean isFileExist(File file) {
        if (!file.isFile() || !file.exists()) {
            return false;
        }
        return true;
    }

    private static String getFileMD5(File file) {
        if (!file.isFile() || !file.exists()) {
            return "";
        }
        FileInputStream in = null;
        byte[] buffer = new byte[1024];
        try {
            MessageDigest digest = MessageDigest.getInstance(OppoWiFiSauXml.WifiUpdateObjXmlUtil.XML_TAG_MD5);
            FileInputStream in2 = new FileInputStream(file);
            while (true) {
                int len = in2.read(buffer, 0, 1024);
                if (len != -1) {
                    digest.update(buffer, 0, len);
                } else {
                    in2.close();
                    return new BigInteger(1, digest.digest()).toString(16);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e2) {
                    e.printStackTrace();
                }
            }
            return "";
        }
    }

    private static int parseDocumentStartAndVersionFromXml(XmlPullParser in) throws XmlPullParserException, IOException {
        XmlUtil.gotoDocumentStart(in, "OppoWifiSauConfig");
        int version = ((Integer) XmlUtil.readNextValueWithName(in, XML_TAG_VERSION)).intValue();
        if (version >= 1 && version <= 1) {
            return version;
        }
        throw new XmlPullParserException("Invalid version of data: " + version);
    }

    private void indicateNoDataForStoreDatas(Collection<StoreData> storeDataSet, boolean shareData) throws XmlPullParserException, IOException {
        for (StoreData storeData : storeDataSet) {
            storeData.deserializeData(null, 0, shareData);
        }
    }

    private static StoreFile createFile(String fname) {
        return new StoreFile(new File(fname));
    }

    public static class StoreFile {
        private static final int FILE_MODE = 384;
        private final AtomicFile mAtomicFile;
        private String mFileName = this.mAtomicFile.getBaseFile().getAbsolutePath();
        private byte[] mWriteData;

        public StoreFile(File file) {
            this.mAtomicFile = new AtomicFile(file);
        }

        public boolean exists() {
            return this.mAtomicFile.exists();
        }

        public byte[] readRawData() throws IOException {
            try {
                return this.mAtomicFile.readFully();
            } catch (FileNotFoundException e) {
                Log.e("OppoWifiSauConfig", "readRawData : " + e);
                return null;
            }
        }
    }
}
