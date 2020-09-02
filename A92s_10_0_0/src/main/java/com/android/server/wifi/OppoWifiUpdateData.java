package com.android.server.wifi;

import android.util.Log;
import com.android.server.wifi.OppoWiFiSauXml;
import com.android.server.wifi.OppoWifiOCloudImpl;
import com.android.server.wifi.OppoWifiSauConfig;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoWifiUpdateData implements OppoWifiSauConfig.StoreData {
    private static final String TAG = "OppoWifiUpdateData";
    public String mEffectMethod;
    public String mFileName;
    public String mFileType;
    public boolean mIsValid;
    public String mPlatform;
    public String mPushFail;
    public String mPushReason;
    public String mPushTime;
    public List<OppoWifiUpdateObj> mUpdateObjList;
    public String mWifiUpdateObjConfig;
    public String mWifiUpdateObjConfigList;

    public OppoWifiUpdateData(String objconfigList, String objconfig) {
        this.mIsValid = true;
        this.mPlatform = OppoWifiSauConfig.PLATFORM_NONE;
        this.mFileType = OppoWifiSauConfig.PLATFORM_NONE;
        this.mFileName = OppoWifiSauConfig.PLATFORM_NONE;
        this.mEffectMethod = "general";
        this.mPushTime = "20200220";
        this.mPushReason = OppoWifiOCloudImpl.SimpleWifiConfig.UPDATE;
        this.mPushFail = OppoWifiSauConfig.PLATFORM_NONE;
        this.mUpdateObjList = null;
        this.mUpdateObjList = new ArrayList();
        this.mWifiUpdateObjConfigList = objconfigList;
        this.mWifiUpdateObjConfig = objconfig;
    }

    public void setUpdateDateVaild(boolean isVaild) {
        this.mIsValid = isVaild;
    }

    public void setUpdateDateFailReason(String failReason) {
        this.mPushFail = failReason;
    }

    public void setUpdateDateDetail(String platform, String type, String name, String effectMethod, String reason, String pushTime, String pushfail) {
        this.mPlatform = platform;
        this.mFileType = type;
        this.mFileName = name;
        this.mEffectMethod = effectMethod;
        this.mPushReason = reason;
        this.mPushTime = pushTime;
        this.mPushFail = pushfail;
    }

    @Override // com.android.server.wifi.OppoWifiSauConfig.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth, boolean shared) throws XmlPullParserException, IOException {
        String[] headerName = new String[1];
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
            try {
                if (headerName[0].equals(this.mWifiUpdateObjConfig)) {
                    Log.d(TAG, " start parse wifi update obj >>>> ");
                    OppoWifiUpdateObj updateObj = OppoWiFiSauXml.WifiUpdateObjXmlUtil.parseFromXml(in, outerTagDepth + 1);
                    if (updateObj != null) {
                        Log.d(TAG, " end parse wifi update obj <<<< ");
                        this.mUpdateObjList.add(updateObj);
                    } else {
                        Log.e(TAG, "The wifi update obj config is invalid!");
                    }
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to parse config. Skipping...", e);
            } catch (Exception e2) {
                Log.e(TAG, "parse abnormally.", e2);
            } catch (Throwable th) {
            }
        }
    }

    public List<OppoWifiUpdateObj> getUpdateObjList() {
        List<OppoWifiUpdateObj> list = this.mUpdateObjList;
        if (list == null) {
            return new ArrayList();
        }
        return list;
    }
}
