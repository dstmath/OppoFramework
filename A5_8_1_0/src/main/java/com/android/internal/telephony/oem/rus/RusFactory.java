package com.android.internal.telephony.oem.rus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.Rlog;
import com.android.internal.telephony.OemConstant;
import java.util.ArrayList;

public class RusFactory {
    public static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCESS = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    public static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String TAG = "RusFactory";
    private static final String mAllPlatIndex_end = "/all_platform>";
    private static final String mAllPlatIndex_start = "<all_platform";
    private static final String mAllPlatformFileName = "allplatform_list.xml";
    private static final String mNetworkFilterName = "nw_all_config";
    private static final Uri mNetworkRomupdateInfoDataUri = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String mPath = "/data/data/com.android.phone/";
    private static int sAllIndexCount = 14;
    static final Object sInstSync = new Object();
    private static RusFactory sInstance;
    private String mAllPlatContent = null;
    private Context mContext;
    private FileUtils mFileUtils;
    private String mProviderContent = null;
    Thread mRusServerThread;
    SenderRusServer mSenderRusServer;

    class SenderRusServer implements Runnable {
        RusServerHelper mRsh;

        public SenderRusServer() {
            this.mRsh = null;
            this.mRsh = new RusServerHelper();
        }

        public void run() {
            try {
                this.mRsh.setProviderContent(RusFactory.this.getProiverData());
                this.mRsh.executeRus();
            } catch (Exception e) {
                RusFactory.this.printLog(RusFactory.TAG, "Exception while thread start" + e);
            }
        }
    }

    private RusFactory(Context context) {
        this.mContext = context;
        this.mFileUtils = new FileUtils();
        initUpdateBroadcastReceiver();
    }

    private void init() {
        this.mSenderRusServer = new SenderRusServer();
        this.mRusServerThread = new Thread(this.mSenderRusServer);
        this.mRusServerThread.start();
    }

    public static RusFactory getInstance(Context context) {
        RusFactory rusFactory;
        synchronized (sInstSync) {
            if (sInstance == null) {
                sInstance = new RusFactory(context);
            }
            rusFactory = sInstance;
        }
        return rusFactory;
    }

    public void initUpdateBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCESS);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    ArrayList<String> tmp = intent.getStringArrayListExtra(RusFactory.ROM_UPDATE_CONFIG_LIST);
                    RusFactory.this.printLog(RusFactory.TAG, "onReceive, list = " + tmp);
                    if (tmp != null && tmp.contains(RusFactory.mNetworkFilterName)) {
                        RusFactory.this.getUpdateFromProvider();
                        RusFactory.this.init();
                    }
                }
            }
        }, filter);
    }

    private void getUpdateFromProvider() {
        try {
            printLog(TAG, "start to get data");
            String dataProvider = getDataFromRomupdateProvider();
            printLog(TAG, " get data is =" + dataProvider);
            if (dataProvider != null) {
                setProviderData(dataProvider);
                this.mAllPlatContent = createNetworkRomupdateXmlFile(dataProvider, mAllPlatformFileName, mAllPlatIndex_start, mAllPlatIndex_end, sAllIndexCount);
                this.mFileUtils.saveSpnToFile(this.mAllPlatContent, "/data/data/com.android.phone/allplatform_list.xml");
            }
        } catch (Exception e) {
            printLog(TAG, "romupdate network failed,createplatContent, because of " + e);
        }
    }

    public void setProviderData(String providerData) {
        this.mProviderContent = providerData;
    }

    public String getProiverData() {
        return this.mProviderContent;
    }

    private String getDataFromRomupdateProvider() {
        Cursor cursor = null;
        String returnStr = null;
        try {
            cursor = this.mContext.getContentResolver().query(mNetworkRomupdateInfoDataUri, new String[]{COLUMN_NAME_1, COLUMN_NAME_2}, "filterName=\"nw_all_config\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex(COLUMN_NAME_1);
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                int configVersion = cursor.getInt(versioncolumnIndex);
                returnStr = cursor.getString(xmlcolumnIndex);
                printLog(TAG, "network updated list, version = " + configVersion);
                printLog(TAG, "network updated list, file = " + returnStr);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            printLog(TAG, "romupdate network failed,We can not get qual network update list from provider, because of " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return returnStr;
    }

    private String createNetworkRomupdateXmlFile(String providerString, String networkInfoFileName, String networkIndexStart, String networkIndexEnd, int subStringCount) {
        if (providerString == null) {
            return null;
        }
        printLog(TAG, "romupdate networkInfoFileName path=" + (mPath + networkInfoFileName));
        int networkStartIndex = providerString.indexOf(networkIndexStart);
        int networkEndIndex = providerString.indexOf(networkIndexEnd);
        printLog(TAG, "network_index start=" + networkStartIndex + " network_indext end" + networkEndIndex);
        String networkContent = providerString.substring(networkStartIndex, networkEndIndex + subStringCount);
        printLog(TAG, "Network xml string is:" + networkContent);
        return networkContent;
    }

    private void printLog(String tag, String msg) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(tag, msg);
        }
    }
}
