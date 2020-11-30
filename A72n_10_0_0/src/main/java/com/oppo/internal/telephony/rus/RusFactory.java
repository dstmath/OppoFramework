package com.oppo.internal.telephony.rus;

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
    private static final String ACTION_ROM_UPDATE_CONFIG_SUCCESS = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String TAG = "RusFactory";
    private static final String mFilterName = "nw_all_config";
    private static final Uri mRomupdateInfoDataUri = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final Object sInstSync = new Object();
    private static RusFactory sInstance;
    private static boolean sOppoMtkPlatform = false;
    private static boolean sOppoQcomPlatform = false;
    private Context mContext;

    /* access modifiers changed from: package-private */
    public class SenderRusServer implements Runnable {
        private boolean mIsRusAction = false;
        private String mRusXml = "";

        public SenderRusServer(boolean isRusAction, String rusXml) {
            this.mIsRusAction = isRusAction;
            this.mRusXml = rusXml;
        }

        public void run() {
            try {
                RusFactory rusFactory = RusFactory.this;
                rusFactory.printLog(RusFactory.TAG, "SenderRusServer run() mIsRusAction:" + this.mIsRusAction);
                RusServerHelper rusHelper = new RusServerHelper();
                if (this.mIsRusAction) {
                    rusHelper.executeHelpRusAction(this.mRusXml);
                } else {
                    rusHelper.executeHelpRebootAction();
                }
            } catch (Exception e) {
                RusFactory rusFactory2 = RusFactory.this;
                rusFactory2.printLog(RusFactory.TAG, "Exception while thread start" + e);
            }
        }
    }

    public void startRusThread(String rusXml) {
        if (rusXml != null && !rusXml.isEmpty()) {
            new Thread(new SenderRusServer(true, rusXml)).start();
        }
    }

    public void startRebootThread() {
        new Thread(new SenderRusServer(false, "")).start();
    }

    private RusFactory(Context context) {
        if (context != null) {
            this.mContext = context;
            sOppoMtkPlatform = context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
            sOppoQcomPlatform = context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.qualcomm");
        }
        initUpdateBroadcastReceiver();
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

    public static boolean isBasedOnMtk() {
        return sOppoMtkPlatform;
    }

    public static boolean isBasedOnQcom() {
        return sOppoQcomPlatform;
    }

    private void initUpdateBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ROM_UPDATE_CONFIG_SUCCESS);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.oppo.internal.telephony.rus.RusFactory.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    try {
                        ArrayList<String> tmp = intent.getStringArrayListExtra(RusFactory.ROM_UPDATE_CONFIG_LIST);
                        RusFactory rusFactory = RusFactory.this;
                        rusFactory.printLog(RusFactory.TAG, "onReceive, list = " + tmp);
                        if (tmp != null && tmp.contains(RusFactory.mFilterName)) {
                            String rusxXml = RusFactory.this.getDataFromRomupdateProvider();
                            if (!rusxXml.isEmpty()) {
                                RusFactory.this.startRusThread(rusxXml);
                            }
                        }
                        Intent rusIntent = new Intent(intent);
                        rusIntent.setAction("oppo.intent.action.RUS_UPDATE_CARRIERCONFIG");
                        rusIntent.addFlags(16777216);
                        context.sendBroadcast(rusIntent);
                    } catch (Exception e) {
                        RusFactory rusFactory2 = RusFactory.this;
                        rusFactory2.printLog(RusFactory.TAG, "onReceive Exception of " + e);
                    }
                }
            }
        }, filter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0083, code lost:
        if (0 == 0) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0086, code lost:
        return r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0065, code lost:
        if (r1 != null) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0067, code lost:
        r1.close();
     */
    private String getDataFromRomupdateProvider() {
        Cursor cursor = null;
        String columnXml = "";
        try {
            cursor = this.mContext.getContentResolver().query(mRomupdateInfoDataUri, new String[]{COLUMN_NAME_1, COLUMN_NAME_2}, "filterName=\"nw_all_config\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex(COLUMN_NAME_1);
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                String columnVersion = cursor.getString(versioncolumnIndex);
                columnXml = cursor.getString(xmlcolumnIndex);
                printLog(TAG, "network updated list, version = " + columnVersion);
                printLog(TAG, "network updated list, file = " + columnXml);
            }
        } catch (Exception e) {
            printLog(TAG, "romupdate network failed,We can not get qual network update list from provider, because of " + e);
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void printLog(String tag, String msg) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(tag, msg);
        }
    }
}
