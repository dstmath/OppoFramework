package com.android.internal.telephony.regionlock;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.SpnOverride;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public final class RegionLockPlmnListService extends Thread {
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final boolean DEBUG = false;
    private static final String TAG = "RegionLockPlmnListService";
    private static final int UPDATE_SUCCESS = 21;
    private static final int UPDATE_VER_READ_ERROR = 20;
    private static final String mNetCodeFilterName = "netcode_config";
    private static final Uri mNetCodeInfoDataUri = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String mNetLockCodeInfoPath = "etc/netcode_config.xml";
    private static final String mNetLockCodeVersionPath = "etc/netcode_version.xml";
    private static ArrayList<PlmnCodeEntry> sCustomizedBlackPlmnList = new ArrayList();
    private static ArrayList<PlmnCodeEntry> sCustomizedWhitePlmnList = new ArrayList();
    private static RegionLockPlmnListService sInstance = null;
    private Context mContext;
    private String mReturnDataStr = null;

    public static class PlmnCodeEntry {
        public static final String BLACK_LIST = "0";
        public static final String CATEGORY_ATTR = "category";
        public static final String COUNTRY_ATTR = "country";
        public static final String MCC_ATTR = "mcc";
        public static final String MNC_ATTR = "mnc";
        public static final String REGION_ATTR = "region";
        public static final String WHITE_LIST = "1";
        private String mCategory = new String(SpnOverride.MVNO_TYPE_NONE);
        private String mCountry = new String(SpnOverride.MVNO_TYPE_NONE);
        private String mMcc = new String(SpnOverride.MVNO_TYPE_NONE);
        private String mMnc = new String(SpnOverride.MVNO_TYPE_NONE);
        private String mRegion = new String(SpnOverride.MVNO_TYPE_NONE);

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

    public static RegionLockPlmnListService getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RegionLockPlmnListService(context);
            Rlog.d(TAG, "sInstance run");
            sInstance.start();
        }
        return sInstance;
    }

    public RegionLockPlmnListService(Context context) {
        this.mContext = context;
    }

    public void run() {
        Rlog.d(TAG, "RegionLockPlmnListService run");
        getDataFromRomupdateProvider();
    }

    private void getDataFromRomupdateProvider() {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(mNetCodeInfoDataUri, new String[]{COLUMN_NAME_1, COLUMN_NAME_2}, "filterName=\"netcode_config\"", null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                getRegionLockPlmnList(false);
            } else {
                boolean z;
                int versioncolumnIndex = cursor.getColumnIndex(COLUMN_NAME_1);
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                int remoteVersion = cursor.getInt(versioncolumnIndex);
                this.mReturnDataStr = cursor.getString(xmlcolumnIndex);
                if (getXmlVersion(Environment.getRootDirectory() + "/" + mNetLockCodeVersionPath) < remoteVersion) {
                    z = true;
                } else {
                    z = false;
                }
                getRegionLockPlmnList(z);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Rlog.e(TAG, "We can not get mtk plmn update list from provider, because of " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x0295 A:{SYNTHETIC, Splitter: B:71:0x0295} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x01b0 A:{SYNTHETIC, Splitter: B:32:0x01b0} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0265 A:{SYNTHETIC, Splitter: B:53:0x0265} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0295 A:{SYNTHETIC, Splitter: B:71:0x0295} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getRegionLockPlmnList(boolean isFromDB) {
        Throwable e;
        Throwable th;
        Throwable e2;
        Rlog.d(TAG, "getRegionLockPlmnList,isFromDB==" + isFromDB);
        if (!sCustomizedWhitePlmnList.isEmpty()) {
            sCustomizedWhitePlmnList.clear();
        }
        if (!sCustomizedBlackPlmnList.isEmpty()) {
            sCustomizedBlackPlmnList.clear();
        }
        FileReader fileReader = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser confparser = factory.newPullParser();
            Reader stringReader;
            if (isFromDB) {
                stringReader = new StringReader(this.mReturnDataStr);
                Reader reader;
                try {
                    confparser.setInput(stringReader);
                    reader = stringReader;
                } catch (FileNotFoundException e3) {
                    e = e3;
                    reader = stringReader;
                    try {
                        Rlog.e(TAG, "getXmlFile file not found", e);
                        if (fileReader != null) {
                            try {
                                fileReader.close();
                            } catch (IOException e4) {
                            }
                        }
                        return 20;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileReader != null) {
                            try {
                                fileReader.close();
                            } catch (IOException e5) {
                            }
                        }
                        throw th;
                    }
                } catch (Exception e6) {
                    e2 = e6;
                    reader = stringReader;
                    Rlog.e(TAG, "getXmlFile Exception while parsing", e2);
                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e7) {
                        }
                    }
                    return 20;
                } catch (Throwable th3) {
                    th = th3;
                    reader = stringReader;
                    if (fileReader != null) {
                    }
                    throw th;
                }
            }
            stringReader = new FileReader(Environment.getRootDirectory() + "/" + mNetLockCodeInfoPath);
            Reader fileReader2;
            try {
                confparser.setInput(stringReader);
                fileReader2 = stringReader;
            } catch (FileNotFoundException e8) {
                e = e8;
                fileReader2 = stringReader;
                Rlog.e(TAG, "getXmlFile file not found", e);
                if (fileReader != null) {
                }
                return 20;
            } catch (Exception e9) {
                e2 = e9;
                fileReader2 = stringReader;
                Rlog.e(TAG, "getXmlFile Exception while parsing", e2);
                if (fileReader != null) {
                }
                return 20;
            } catch (Throwable th4) {
                th = th4;
                fileReader2 = stringReader;
                if (fileReader != null) {
                }
                throw th;
            }
            if (confparser == null) {
                Rlog.d(TAG, "confparser==null");
            }
            confparser.next();
            confparser.require(2, null, "netcode");
            int version = Integer.parseInt(confparser.getAttributeValue(null, COLUMN_NAME_1));
            Rlog.d(TAG, "new version =" + version);
            confparser.nextTag();
            String regionTag = confparser.getName();
            Rlog.d(TAG, "the select name=" + confparser.getName());
            RegionLockDesUtils desUtils = new RegionLockDesUtils();
            while (true) {
                if (regionTag.equals("endcode")) {
                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e10) {
                        }
                    }
                    if (version != -1) {
                        return 21;
                    }
                    Rlog.e(TAG, "getXmlFile version not found");
                    return 20;
                }
                confparser.require(2, null, regionTag);
                String region = confparser.getAttributeValue(null, PlmnCodeEntry.REGION_ATTR);
                Rlog.d(TAG, "<regionTag " + regionTag + ",region=" + region + ">");
                if (confparser.nextTag() != 3) {
                    confparser.require(2, null, "whitelist");
                }
                while (confparser.nextTag() != 3) {
                    confparser.require(2, null, "plmn");
                    setRegionLockPlmnList(region, desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.COUNTRY_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.MCC_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.MNC_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.CATEGORY_ATTR)));
                    confparser.nextTag();
                }
                if (confparser.nextTag() != 3) {
                    confparser.require(2, null, "blacklist");
                }
                while (confparser.nextTag() != 3) {
                    confparser.require(2, null, "plmn");
                    String str = region;
                    setRegionLockPlmnList(str, desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.COUNTRY_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.MCC_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.MNC_ATTR)), desUtils.decrypt(confparser.getAttributeValue(null, PlmnCodeEntry.CATEGORY_ATTR)));
                    confparser.nextTag();
                }
                confparser.nextTag();
                confparser.nextTag();
                regionTag = confparser.getName();
            }
        } catch (FileNotFoundException e11) {
            e = e11;
        } catch (Exception e12) {
            e2 = e12;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0065 A:{SYNTHETIC, Splitter: B:27:0x0065} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0052 A:{SYNTHETIC, Splitter: B:15:0x0052} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getXmlVersion(String filePath) {
        Exception e;
        Throwable th;
        Rlog.d(TAG, "getXmlVersion");
        int version = -1;
        FileReader fileReader = null;
        try {
            FileReader fileReader2 = new FileReader(new File(filePath));
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser confparser = factory.newPullParser();
                confparser.setInput(fileReader2);
                confparser.next();
                confparser.require(2, null, "netcode");
                version = Integer.parseInt(confparser.getAttributeValue(null, COLUMN_NAME_1));
                if (fileReader2 != null) {
                    try {
                        fileReader2.close();
                    } catch (IOException e2) {
                    }
                }
                fileReader = fileReader2;
            } catch (FileNotFoundException e3) {
                fileReader = fileReader2;
            } catch (Exception e4) {
                e = e4;
                fileReader = fileReader2;
                try {
                    Rlog.e(TAG, "getXmlVersion Exception while parsing ", e);
                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e5) {
                        }
                    }
                    return version;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileReader != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileReader = fileReader2;
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e8) {
                }
            }
            return -1;
        } catch (Exception e9) {
            e = e9;
            Rlog.e(TAG, "getXmlVersion Exception while parsing ", e);
            if (fileReader != null) {
            }
            return version;
        }
        return version;
    }

    private void setRegionLockPlmnList(String region, String country, String mcc, String mnc, String category) {
        PlmnCodeEntry record = new PlmnCodeEntry();
        record.setRegion(region);
        record.setCountry(country);
        record.setMcc(mcc);
        record.setMnc(mnc);
        record.setCategory(category);
        if (TextUtils.isEmpty(category) || !"1".equals(category)) {
            sCustomizedBlackPlmnList.add(record);
        } else {
            sCustomizedWhitePlmnList.add(record);
        }
    }

    private boolean oppoIsTestModeWhiteList(String region, String plmn) {
        if (!(sCustomizedWhitePlmnList == null || (sCustomizedWhitePlmnList.isEmpty() ^ 1) == 0)) {
            for (PlmnCodeEntry plmnCodeEntry : sCustomizedWhitePlmnList) {
                if (plmnCodeEntry.getRegion().equals(region) && plmnCodeEntry.getCategory().equals("1")) {
                    String mcc = plmnCodeEntry.getMcc();
                    String mnc = plmnCodeEntry.getMnc();
                    if (!TextUtils.isEmpty(plmn) && plmn.equals(mcc + mnc)) {
                        Rlog.d(TAG, "oppoIsTestModeWhiteList,mcc==" + mcc + ",mnc==" + mnc + ",plmn=" + plmn);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean oppoIsTestModeBlackList(String region, String plmn) {
        if (!(sCustomizedBlackPlmnList == null || (sCustomizedBlackPlmnList.isEmpty() ^ 1) == 0)) {
            for (PlmnCodeEntry plmnCodeEntry : sCustomizedBlackPlmnList) {
                if (plmnCodeEntry.getRegion().equals(region) && plmnCodeEntry.getCategory().equals("0")) {
                    String country = plmnCodeEntry.getCountry();
                    String mcc = plmnCodeEntry.getMcc();
                    String mnc = plmnCodeEntry.getMnc();
                    if (!TextUtils.isEmpty(plmn) && plmn.equals(mcc + mnc)) {
                        Rlog.d(TAG, "oppoIsTestModeBlackList,country=" + country + ",mcc==" + mcc + ",mnc==" + mnc + ",plmn=" + plmn);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean oppoIsWhiteListNetwork(String region, String plmn) {
        boolean whiteListNetwork = false;
        boolean tempBlackNetwork = false;
        boolean hasWhiteInRegion = false;
        region = getCurrentNetLockRegion(region);
        Rlog.d(TAG, "oppoIsWhiteListNetwork,region==" + region);
        if (isDomesticTestMode()) {
            if (oppoIsTestModeWhiteList(region, plmn)) {
                whiteListNetwork = true;
            }
            return whiteListNetwork;
        }
        String mcc;
        if (sCustomizedWhitePlmnList != null && (sCustomizedWhitePlmnList.isEmpty() ^ 1) != 0) {
            for (PlmnCodeEntry plmnCodeEntry : sCustomizedWhitePlmnList) {
                if (plmnCodeEntry.getRegion().equals(region) && plmnCodeEntry.getCategory().equals("1")) {
                    String country = plmnCodeEntry.getCountry();
                    mcc = plmnCodeEntry.getMcc();
                    hasWhiteInRegion = true;
                    if (!TextUtils.isEmpty(plmn) && plmn.startsWith(mcc)) {
                        Rlog.d(TAG, "oppoIsWhiteListNetwork,mcc==" + mcc + ",country==" + country);
                        whiteListNetwork = true;
                        break;
                    }
                }
            }
        }
        if (!(hasWhiteInRegion || sCustomizedBlackPlmnList == null || (sCustomizedBlackPlmnList.isEmpty() ^ 1) == 0)) {
            for (PlmnCodeEntry plmnCodeEntry2 : sCustomizedBlackPlmnList) {
                if (plmnCodeEntry2.getRegion().equals(region) && plmnCodeEntry2.getCategory().equals("0")) {
                    Rlog.d(TAG, "oppoIsWhiteListNetwork,use black list solution");
                    mcc = plmnCodeEntry2.getMcc();
                    if (!TextUtils.isEmpty(plmn) && plmn.startsWith(mcc)) {
                        tempBlackNetwork = true;
                        break;
                    }
                }
            }
            whiteListNetwork = tempBlackNetwork ^ 1;
        }
        Rlog.d(TAG, "oppoIsWhiteListNetwork is whiteListNetwork:" + whiteListNetwork);
        return whiteListNetwork;
    }

    public boolean oppoIsBlackListNetwork(String region, String plmn) {
        boolean blackListNetwork = false;
        boolean hasBlackInRegion = false;
        boolean tempWhiteNetwork = false;
        region = getCurrentNetLockRegion(region);
        Rlog.d(TAG, "oppoIsBlackListNetwork,region==" + region);
        if (isDomesticTestMode()) {
            if (oppoIsTestModeBlackList(region, plmn)) {
                blackListNetwork = true;
            }
            return blackListNetwork;
        }
        String mcc;
        if (sCustomizedBlackPlmnList != null && (sCustomizedBlackPlmnList.isEmpty() ^ 1) != 0) {
            for (PlmnCodeEntry plmnCodeEntry : sCustomizedBlackPlmnList) {
                if (plmnCodeEntry.getRegion().equals(region) && plmnCodeEntry.getCategory().equals("0")) {
                    String country = plmnCodeEntry.getCountry();
                    mcc = plmnCodeEntry.getMcc();
                    hasBlackInRegion = true;
                    if (!TextUtils.isEmpty(plmn) && plmn.startsWith(mcc)) {
                        Rlog.d(TAG, "oppoIsBlackListNetwork,mcc==" + mcc + ",country==" + country);
                        blackListNetwork = true;
                        break;
                    }
                }
            }
        }
        if (!(hasBlackInRegion || sCustomizedWhitePlmnList == null || (sCustomizedWhitePlmnList.isEmpty() ^ 1) == 0)) {
            for (PlmnCodeEntry plmnCodeEntry2 : sCustomizedWhitePlmnList) {
                if (plmnCodeEntry2.getRegion().equals(region) && plmnCodeEntry2.getCategory().equals("1")) {
                    Rlog.d(TAG, "oppoIsBlackListNetwork,register no whilte list network");
                    mcc = plmnCodeEntry2.getMcc();
                    Rlog.d(TAG, "plmn=" + plmn + ",mcc==" + mcc + ",mnc==" + plmnCodeEntry2.getMnc());
                    if (!TextUtils.isEmpty(plmn) && plmn.startsWith(mcc)) {
                        tempWhiteNetwork = true;
                        break;
                    }
                }
            }
            blackListNetwork = tempWhiteNetwork ^ 1;
        }
        Rlog.d(TAG, "oppoIsBlackListNetwork is blackListNetwork:" + blackListNetwork);
        return blackListNetwork;
    }

    private boolean isDomesticTestMode() {
        return SystemProperties.get(RegionLockConstant.PERSIST_TEST_OP, RegionLockConstant.TEST_OP_DEFAULT).equals(RegionLockConstant.TEST_OP_DEFAULT) ^ 1;
    }

    private String getCurrentNetLockRegion(String region) {
        if (SystemProperties.get(RegionLockConstant.PERSIST_TEST_OP, RegionLockConstant.TEST_OP_DEFAULT).equals("0")) {
            return "testcu";
        }
        if (SystemProperties.get(RegionLockConstant.PERSIST_TEST_OP, RegionLockConstant.TEST_OP_DEFAULT).equals("1")) {
            return "testcmcc";
        }
        if (SystemProperties.get(RegionLockConstant.PERSIST_TEST_OP, RegionLockConstant.TEST_OP_DEFAULT).equals(RegionLockConstant.TEST_OP_CUANDCMCC)) {
            return "testall";
        }
        return region;
    }
}
