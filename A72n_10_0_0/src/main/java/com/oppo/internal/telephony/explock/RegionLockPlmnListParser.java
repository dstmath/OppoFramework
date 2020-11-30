package com.oppo.internal.telephony.explock;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public final class RegionLockPlmnListParser extends Thread {
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final boolean DEBUG = false;
    private static final String TAG = "RegionLockPlmnListParser";
    private static final int UPDATE_SUCCESS = 21;
    private static final int UPDATE_VER_READ_ERROR = 20;
    private static ArrayList<PlmnCodeEntry> mCustomizedBlackPlmnList = new ArrayList<>();
    private static ArrayList<PlmnCodeEntry> mCustomizedWhitePlmnList = new ArrayList<>();
    private static final String mNetCodeFilterName = "netcode_config";
    private static final Uri mNetCodeInfoDataUri = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String mNetLockCodeInfoPath = "etc/netcode_config.xml";
    private static final String mNetLockCodeVersionPath = "etc/netcode_version.xml";
    private static final String mRusNetLockCodeInfoPath = "/data/data/com.android.phone/region_netcode_config.xml";
    private static RegionLockPlmnListParser sInstance = null;
    private Context mContext;
    private String returnDataStr = null;

    public static RegionLockPlmnListParser getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RegionLockPlmnListParser(context);
            Rlog.d(TAG, "sInstance run");
            sInstance.start();
        }
        return sInstance;
    }

    public RegionLockPlmnListParser(Context context) {
        this.mContext = context;
    }

    public void run() {
        getDataFromRomupdateProvider();
    }

    private void getDataFromRomupdateProvider() {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(mNetCodeInfoDataUri, new String[]{COLUMN_NAME_1, COLUMN_NAME_2}, "filterName=\"netcode_config\"", null, null);
            boolean z = false;
            if (cursor == null || cursor.getCount() <= 0) {
                getRegionLockPlmnList(false);
            } else {
                int versioncolumnIndex = cursor.getColumnIndex(COLUMN_NAME_1);
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                int remoteVersion = cursor.getInt(versioncolumnIndex);
                this.returnDataStr = cursor.getString(xmlcolumnIndex);
                if (getXmlVersion(Environment.getRootDirectory() + "/" + mNetLockCodeVersionPath) < remoteVersion) {
                    z = true;
                }
                getRegionLockPlmnList(z);
            }
            if (cursor == null) {
                return;
            }
        } catch (Exception e) {
            Rlog.e(TAG, "We can not get mtk plmn update list from provider, because of " + e);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
    }

    public void updateRegionLockPlmnList() {
        if (OemLockUtils.isRegionLock()) {
            getRegionLockPlmnList(false);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0075, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0076, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0084, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00cb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00cc, code lost:
        android.telephony.Rlog.d(com.oppo.internal.telephony.explock.RegionLockPlmnListParser.TAG, r0.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00d4, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00d5, code lost:
        android.telephony.Rlog.d(com.oppo.internal.telephony.explock.RegionLockPlmnListParser.TAG, r0.toString());
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x0287 A[SYNTHETIC, Splitter:B:100:0x0287] */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0293 A[SYNTHETIC, Splitter:B:106:0x0293] */
    /* JADX WARNING: Removed duplicated region for block: B:116:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:119:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0075 A[ExcHandler: all (r0v49 'th' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r3 
      PHI: (r3v10 'fileReader' java.io.FileReader) = (r3v4 'fileReader' java.io.FileReader), (r3v4 'fileReader' java.io.FileReader), (r3v0 'fileReader' java.io.FileReader), (r3v12 'fileReader' java.io.FileReader) binds: [B:33:0x00c1, B:34:?, B:20:0x0069, B:21:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:20:0x0069] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0084 A[ExcHandler: FileNotFoundException (e java.io.FileNotFoundException), PHI: r3 
      PHI: (r3v9 'fileReader' java.io.FileReader) = (r3v4 'fileReader' java.io.FileReader), (r3v4 'fileReader' java.io.FileReader), (r3v0 'fileReader' java.io.FileReader), (r3v12 'fileReader' java.io.FileReader) binds: [B:33:0x00c1, B:34:?, B:20:0x0069, B:21:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:20:0x0069] */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0271 A[SYNTHETIC, Splitter:B:93:0x0271] */
    private int getRegionLockPlmnList(boolean isFromRus) {
        Throwable th;
        FileNotFoundException e;
        Exception e2;
        String decryptBlackCountry;
        String blackCategory;
        StringBuilder sb = new StringBuilder();
        sb.append("getRegionLockPlmnList,isFromRus = ");
        boolean isFromRus2 = isFromRus;
        sb.append(isFromRus2);
        Rlog.d(TAG, sb.toString());
        if (!mCustomizedWhitePlmnList.isEmpty()) {
            mCustomizedWhitePlmnList.clear();
        }
        if (!mCustomizedBlackPlmnList.isEmpty()) {
            mCustomizedBlackPlmnList.clear();
        }
        FileReader fileReader = null;
        StringReader strReader = null;
        int version = -1;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser confparser = factory.newPullParser();
            if (netlockFileExists()) {
                try {
                    isFromRus2 = isUseRusNetlockInfo();
                } catch (FileNotFoundException e3) {
                    e = e3;
                    Rlog.e(TAG, "getXmlFile file not found", e);
                    if (fileReader == null) {
                        return 20;
                    }
                    try {
                        fileReader.close();
                        return 20;
                    } catch (IOException e4) {
                        return 20;
                    }
                } catch (Exception e5) {
                    e2 = e5;
                    try {
                        Rlog.e(TAG, "getXmlFile Exception while parsing", e2);
                        if (fileReader != null) {
                            return 20;
                        }
                        try {
                            fileReader.close();
                            return 20;
                        } catch (IOException e6) {
                            return 20;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e7) {
                        }
                    }
                    throw th;
                }
            }
            if (isFromRus2) {
                try {
                    fileReader = new FileReader(mRusNetLockCodeInfoPath);
                    confparser.setInput(fileReader);
                } catch (FileNotFoundException e8) {
                } catch (Exception e9) {
                    e2 = e9;
                    Rlog.e(TAG, "getXmlFile Exception while parsing", e2);
                    if (fileReader != null) {
                    }
                } catch (Throwable th4) {
                }
            } else {
                try {
                    fileReader = new FileReader(Environment.getRootDirectory() + "/" + mNetLockCodeInfoPath);
                    confparser.setInput(fileReader);
                } catch (FileNotFoundException e10) {
                    e = e10;
                    Rlog.e(TAG, "getXmlFile file not found", e);
                    if (fileReader == null) {
                    }
                } catch (Exception e11) {
                    e2 = e11;
                    Rlog.e(TAG, "getXmlFile Exception while parsing", e2);
                    if (fileReader != null) {
                    }
                } catch (Throwable th5) {
                    th = th5;
                    if (fileReader != null) {
                    }
                    throw th;
                }
            }
            if (confparser == null) {
                Rlog.d(TAG, "confparser==null");
            }
            confparser.next();
            int i = 2;
            String str = null;
            confparser.require(2, null, "netcode");
            version = Integer.parseInt(confparser.getAttributeValue(null, COLUMN_NAME_1));
            Rlog.d(TAG, "new version =" + version);
            confparser.nextTag();
            String regionTag = confparser.getName();
            Rlog.d(TAG, "the select name=" + confparser.getName());
            RegionLockDesUtils desUtils = new RegionLockDesUtils();
            String regionTag2 = regionTag;
            while (!regionTag2.equals("endcode")) {
                confparser.require(i, str, regionTag2);
                String region = confparser.getAttributeValue(str, PlmnCodeEntry.REGION_ATTR);
                Rlog.d(TAG, "<regionTag " + regionTag2 + ",region=" + region + ">");
                int i2 = 3;
                if (confparser.nextTag() != 3) {
                    confparser.require(i, str, "whitelist");
                }
                while (true) {
                    int nextTag = confparser.nextTag();
                    decryptBlackCountry = PlmnCodeEntry.CATEGORY_ATTR;
                    blackCategory = "plmn";
                    if (nextTag == i2) {
                        break;
                    }
                    try {
                        confparser.require(2, null, blackCategory);
                    } catch (FileNotFoundException e12) {
                        e = e12;
                        Rlog.e(TAG, "getXmlFile file not found", e);
                        if (fileReader == null) {
                        }
                    } catch (Exception e13) {
                        e2 = e13;
                        Rlog.e(TAG, "getXmlFile Exception while parsing", e2);
                        if (fileReader != null) {
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        if (fileReader != null) {
                        }
                        throw th;
                    }
                    try {
                        setRegionLockPlmnList(region, desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.COUNTRY_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.MCC_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.MNC_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, decryptBlackCountry)));
                        confparser.nextTag();
                        i2 = 3;
                        isFromRus2 = isFromRus2;
                        strReader = strReader;
                    } catch (FileNotFoundException e14) {
                        e = e14;
                        Rlog.e(TAG, "getXmlFile file not found", e);
                        if (fileReader == null) {
                        }
                    } catch (Exception e15) {
                        e2 = e15;
                        Rlog.e(TAG, "getXmlFile Exception while parsing", e2);
                        if (fileReader != null) {
                        }
                    }
                }
                if (confparser.nextTag() != i2) {
                    confparser.require(2, null, "blacklist");
                }
                while (confparser.nextTag() != 3) {
                    confparser.require(2, null, blackCategory);
                    setRegionLockPlmnList(region, desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.COUNTRY_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.MCC_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.MNC_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, decryptBlackCountry)));
                    confparser.nextTag();
                    blackCategory = blackCategory;
                    decryptBlackCountry = decryptBlackCountry;
                }
                confparser.nextTag();
                confparser.nextTag();
                regionTag2 = confparser.getName();
                isFromRus2 = isFromRus2;
                strReader = strReader;
                str = null;
                i = 2;
            }
            try {
                fileReader.close();
            } catch (IOException e16) {
            }
            if (version != -1) {
                return 21;
            }
            Rlog.e(TAG, "getXmlFile version not found");
            return 20;
        } catch (FileNotFoundException e17) {
            e = e17;
            Rlog.e(TAG, "getXmlFile file not found", e);
            if (fileReader == null) {
            }
        } catch (Exception e18) {
            e2 = e18;
            Rlog.e(TAG, "getXmlFile Exception while parsing", e2);
            if (fileReader != null) {
            }
        } catch (Throwable th7) {
            th = th7;
            if (fileReader != null) {
            }
            throw th;
        }
    }

    private int getXmlVersion(String filePath) {
        Rlog.d(TAG, "getXmlVersion");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(new File(filePath));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser confparser = factory.newPullParser();
            confparser.setInput(fileReader);
            confparser.next();
            confparser.require(2, null, "netcode");
            int version = Integer.parseInt(confparser.getAttributeValue(null, COLUMN_NAME_1));
            try {
                fileReader.close();
            } catch (IOException e) {
            }
            return version;
        } catch (FileNotFoundException e2) {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e3) {
                }
            }
            return -1;
        } catch (NumberFormatException e4) {
            Rlog.d(TAG, e4.toString());
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e5) {
                }
            }
            return -1;
        } catch (Exception e6) {
            Rlog.e(TAG, "getXmlVersion Exception while parsing ", e6);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e7) {
                }
            }
            return -1;
        } catch (Throwable th) {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
    }

    private void setRegionLockPlmnList(String region, String country, String mcc, String mnc, String category) {
        PlmnCodeEntry record = new PlmnCodeEntry();
        record.setRegion(region);
        record.setCountry(country);
        record.setMcc(mcc);
        record.setMnc(mnc);
        record.setCategory(category);
        if (TextUtils.isEmpty(category) || !"1".equals(category)) {
            mCustomizedBlackPlmnList.add(record);
        } else {
            mCustomizedWhitePlmnList.add(record);
        }
    }

    private boolean oppoIsTestModeWhiteList(String region, String plmn) {
        ArrayList<PlmnCodeEntry> arrayList = mCustomizedWhitePlmnList;
        if (arrayList == null || arrayList.isEmpty()) {
            return false;
        }
        Iterator<PlmnCodeEntry> it = mCustomizedWhitePlmnList.iterator();
        while (it.hasNext()) {
            PlmnCodeEntry plmnCodeEntry = it.next();
            if (plmnCodeEntry.getRegion().equals(region) && plmnCodeEntry.getCategory().equals("1")) {
                String mcc = plmnCodeEntry.getMcc();
                String mnc = plmnCodeEntry.getMnc();
                if (!TextUtils.isEmpty(plmn)) {
                    if (plmn.equals(mcc + mnc)) {
                        Rlog.d(TAG, "oppoIsTestModeWhiteList,mcc==" + mcc + ",mnc==" + mnc + ",plmn=" + plmn);
                        return true;
                    }
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    private boolean oppoIsTestModeBlackList(String region, String plmn) {
        ArrayList<PlmnCodeEntry> arrayList = mCustomizedBlackPlmnList;
        if (arrayList == null || arrayList.isEmpty()) {
            return false;
        }
        Iterator<PlmnCodeEntry> it = mCustomizedBlackPlmnList.iterator();
        while (it.hasNext()) {
            PlmnCodeEntry plmnCodeEntry = it.next();
            if (plmnCodeEntry.getRegion().equals(region) && plmnCodeEntry.getCategory().equals("0")) {
                String country = plmnCodeEntry.getCountry();
                String mcc = plmnCodeEntry.getMcc();
                String mnc = plmnCodeEntry.getMnc();
                if (!TextUtils.isEmpty(plmn)) {
                    if (plmn.equals(mcc + mnc)) {
                        Rlog.d(TAG, "oppoIsTestModeBlackList,country=" + country + ",mcc==" + mcc + ",mnc==" + mnc + ",plmn=" + plmn);
                        return true;
                    }
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    public boolean oppoIsWhiteListNetwork(String plmn) {
        ArrayList<PlmnCodeEntry> arrayList;
        boolean whiteListNetwork = false;
        boolean tempBlackNetwork = false;
        boolean hasWhiteInRegion = false;
        String region = getCurrentNetLockRegion(OemLockUtils.getRegionLockVersion());
        if (!isDomesticTestMode()) {
            ArrayList<PlmnCodeEntry> arrayList2 = mCustomizedWhitePlmnList;
            if (arrayList2 != null && !arrayList2.isEmpty()) {
                Iterator<PlmnCodeEntry> it = mCustomizedWhitePlmnList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    PlmnCodeEntry plmnCodeEntry = it.next();
                    if (plmnCodeEntry.getRegion().equals(region) && plmnCodeEntry.getCategory().equals("1")) {
                        String country = plmnCodeEntry.getCountry();
                        String mcc = plmnCodeEntry.getMcc();
                        hasWhiteInRegion = true;
                        if (!TextUtils.isEmpty(plmn) && plmn.startsWith(mcc)) {
                            Rlog.d(TAG, "oppoIsWhiteListNetwork,mcc==" + mcc + ",country==" + country);
                            whiteListNetwork = true;
                            break;
                        }
                    }
                }
            }
            if (!hasWhiteInRegion && (arrayList = mCustomizedBlackPlmnList) != null && !arrayList.isEmpty()) {
                Iterator<PlmnCodeEntry> it2 = mCustomizedBlackPlmnList.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    PlmnCodeEntry plmnCodeEntry2 = it2.next();
                    if (plmnCodeEntry2.getRegion().equals(region) && plmnCodeEntry2.getCategory().equals("0")) {
                        Rlog.d(TAG, "oppoIsWhiteListNetwork,use black list solution");
                        String mcc2 = plmnCodeEntry2.getMcc();
                        if (!TextUtils.isEmpty(plmn) && plmn.startsWith(mcc2)) {
                            tempBlackNetwork = true;
                            break;
                        }
                    }
                }
                whiteListNetwork = !tempBlackNetwork;
            }
            Rlog.d(TAG, "oppoIsWhiteListNetwork is whiteListNetwork:" + whiteListNetwork);
            return whiteListNetwork;
        } else if (oppoIsTestModeWhiteList(region, plmn)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean oppoIsBlackListNetwork(String plmn) {
        ArrayList<PlmnCodeEntry> arrayList;
        boolean blackListNetwork = false;
        boolean hasBlackInRegion = false;
        boolean tempWhiteNetwork = false;
        String region = getCurrentNetLockRegion(OemLockUtils.getRegionLockVersion());
        if (!isDomesticTestMode()) {
            ArrayList<PlmnCodeEntry> arrayList2 = mCustomizedBlackPlmnList;
            if (arrayList2 != null && !arrayList2.isEmpty()) {
                Iterator<PlmnCodeEntry> it = mCustomizedBlackPlmnList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    PlmnCodeEntry plmnCodeEntry = it.next();
                    if (plmnCodeEntry.getRegion().equals(region) && plmnCodeEntry.getCategory().equals("0")) {
                        String country = plmnCodeEntry.getCountry();
                        String mcc = plmnCodeEntry.getMcc();
                        hasBlackInRegion = true;
                        if (!TextUtils.isEmpty(plmn) && plmn.startsWith(mcc)) {
                            Rlog.d(TAG, "oppoIsBlackListNetwork,mcc==" + mcc + ",country==" + country);
                            blackListNetwork = true;
                            break;
                        }
                    }
                }
            }
            if (!hasBlackInRegion && (arrayList = mCustomizedWhitePlmnList) != null && !arrayList.isEmpty()) {
                Iterator<PlmnCodeEntry> it2 = mCustomizedWhitePlmnList.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    PlmnCodeEntry plmnCodeEntry2 = it2.next();
                    if (plmnCodeEntry2.getRegion().equals(region) && plmnCodeEntry2.getCategory().equals("1")) {
                        Rlog.d(TAG, "oppoIsBlackListNetwork,register no whilte list network");
                        String mcc2 = plmnCodeEntry2.getMcc();
                        String mnc = plmnCodeEntry2.getMnc();
                        Rlog.d(TAG, "plmn=" + plmn + ",mcc==" + mcc2 + ",mnc==" + mnc);
                        if (!TextUtils.isEmpty(plmn) && plmn.startsWith(mcc2)) {
                            tempWhiteNetwork = true;
                            break;
                        }
                    }
                }
                blackListNetwork = !tempWhiteNetwork;
            }
            Rlog.d(TAG, "oppoIsBlackListNetwork is blackListNetwork:" + blackListNetwork);
            return blackListNetwork;
        } else if (oppoIsTestModeBlackList(region, plmn)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isDomesticTestMode() {
        return !SystemProperties.get(RegionLockConstant.PERSIST_TEST_OP, RegionLockConstant.TEST_OP_DEFAULT).equals(RegionLockConstant.TEST_OP_DEFAULT);
    }

    private String getCurrentNetLockRegion(String region) {
        if (OemLockUtils.isEnableUpRlock()) {
            region = region + "_V2";
        }
        if (RegionLockConstant.TEST_OP.equals("0")) {
            return "testcu";
        }
        if (RegionLockConstant.TEST_OP.equals("1")) {
            return "testcmcc";
        }
        if (RegionLockConstant.TEST_OP.equals(RegionLockConstant.TEST_OP_CUANDCMCC)) {
            return "testall";
        }
        return region;
    }

    public static class PlmnCodeEntry {
        public static final String BLACK_LIST = "0";
        public static final String CATEGORY_ATTR = "category";
        public static final String COUNTRY_ATTR = "country";
        public static final String MCC_ATTR = "mcc";
        public static final String MNC_ATTR = "mnc";
        public static final String REGION_ATTR = "region";
        public static final String WHITE_LIST = "1";
        private String mCategory = new String("");
        private String mCountry = new String("");
        private String mMcc = new String("");
        private String mMnc = new String("");
        private String mRegion = new String("");

        public void setRegion(String strRegion) {
            this.mRegion = strRegion;
        }

        public void setCountry(String strCountry) {
            this.mCountry = strCountry;
        }

        public void setMcc(String strMcc) {
            this.mMcc = strMcc;
        }

        public void setMnc(String strMnc) {
            this.mMnc = strMnc;
        }

        public void setCategory(String strCategory) {
            this.mCategory = strCategory;
        }

        public String getRegion() {
            return this.mRegion;
        }

        public String getCountry() {
            return this.mCountry;
        }

        public String getMcc() {
            return this.mMcc;
        }

        public String getMnc() {
            return this.mMnc;
        }

        public String getCategory() {
            return this.mCategory;
        }

        public String toString() {
            return "\nregion=" + getRegion() + ", " + COUNTRY_ATTR + "=" + getCountry() + ", " + MCC_ATTR + "=" + getMcc() + "," + MNC_ATTR + "=" + getMnc() + "," + CATEGORY_ATTR + "=" + getCategory();
        }
    }

    private boolean isUseRusNetlockInfo() {
        int localVersion = getXmlVersion(Environment.getRootDirectory() + "/" + mNetLockCodeVersionPath);
        int rusVersion = getXmlVersion(mRusNetLockCodeInfoPath);
        Rlog.d(TAG, "isUseRusNetlockInfo localVersion " + localVersion + "rusVersion = " + rusVersion);
        return rusVersion > localVersion;
    }

    public boolean netlockFileExists() {
        if (new File(mRusNetLockCodeInfoPath).exists()) {
            return true;
        }
        Rlog.d(TAG, "netlockFileExists path is not exist,return false");
        return false;
    }
}
