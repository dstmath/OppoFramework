package com.oppo.luckymoney;

import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.app.ILMServiceManager;
import java.util.ArrayList;
import java.util.HashMap;

public class LMManager {
    public static final String BROADCAST_ACTION_UPDATE_lUCKYMONEYINFO = "com.oppo.intent.action.UPDATE_lUCKYMONEYINFO";
    public static final String DETECT_CNN = "DETECT_CNN";
    public static final String DETECT_IMAGEVIEW = "DETECT_IMAGEVIEW";
    public static final String DETECT_NETWORK = "DETECT_NETWORK";
    public static final String DETECT_TEXT = "DETECT_TEXT";
    public static final String EVENT_ID = "LuckyMoney";
    public static final String KEY_CURRENT_TIME = "CURRENT_TIME";
    public static final String KEY_DETECT_RETURN = "DETECT_RETURN";
    public static final String KEY_SPEND_TIME = "SPEND_TIME";
    public static final String KEY_TYPE = "TYPE";
    public static final String LOGTAG = "2016101";
    public static final String LUCKY_MONEY_SERVICE = "luckymoney";
    private static final int MAX_MODE = 4;
    public static final String MM_PACKAGENAME = "com.tencent.mm";
    private static final int MOBILE_POLICY_LAST_TIME = 300;
    public static final String MODE_2_HB_HEIGHT = "hb_height";
    public static final String MODE_2_HB_WIDTH = "hb_width";
    public static final String MODE_2_HG_HASH = "hg_hash";
    public static final String MODE_2_RECEIVER_CLASS = "receiver_class";
    private static final int MODE_MAX_VALUE = 10;
    public static final String OPEN_LUCKYMONEY = "OPEN_LUCKYMONEY";
    private static final int PER_PING_TIME = 8;
    public static final String QQ_PACKAGENAME = "com.tencent.mobileqq";
    public static final String SWITCH_MODE = "SWITCH_MODE";
    private static final String TAG = "LMManager";
    private static final int TYPE_DETECT_CNN = 4;
    private static final int TYPE_DETECT_IMAGEVIEW = 2;
    private static final int TYPE_DETECT_TEXT = 3;
    public static final int TYPE_MODE_MM = 0;
    public static final int TYPE_MODE_NONE = -1;
    public static final int TYPE_MODE_QQ = 1;
    public static int luckyMoneyXmlVersion = -1;
    /* access modifiers changed from: private */
    public static boolean mCancelNewMsgDetect = false;
    static String mChatView = "";
    public static int mCurrentBoostMode = 1;
    static int mDefaultHbMode = 0;
    public static ArrayList<String> mHbHashs;
    public static ArrayList<Integer> mHbHeights;
    static ArrayList<String> mHbLayout = new ArrayList<>();
    static ArrayList<Integer> mHbLayoutNodes = new ArrayList<>();
    static String mHbText = "";
    public static ArrayList<Integer> mHbWidths;
    private static boolean mInitailized = false;
    public static boolean mIsEnable = true;
    public static boolean mModeEnable = true;
    public static HashMap<Integer, Boolean> mModeEnableInfo;
    public static ArrayList<String> mOpenHbActivity;
    public static boolean mSMEnable = true;
    public static int sBoostMode = -1;
    public static boolean sGetHash = false;
    /* access modifiers changed from: private */
    public static LMManager sLMManager = null;
    private static Handler sLastNewMsgTimeoutHandler;
    private static boolean sMODE2_LastViewItem = false;
    /* access modifiers changed from: private */
    public static boolean sMODE2_NewMsgDetected = false;
    public static byte[] sMODE_2_VALUE_HB_HASH;
    public static int sMODE_2_VALUE_HB_HEIGHT = 0;
    public static int sMODE_2_VALUE_HB_WIDTH = 0;
    public static String sMODE_2_VALUE_RECEIVER_CLASS = "";
    private static Bundle sModeData = null;
    private static Runnable sNewMsgTimeout = new Runnable() {
        /* class com.oppo.luckymoney.LMManager.AnonymousClass1 */

        public void run() {
            boolean unused = LMManager.sMODE2_NewMsgDetected = false;
        }
    };
    private static ILMServiceManager sService = null;
    /* access modifiers changed from: private */
    public float mCNNReturn = 0.0f;
    private Context mContext = null;
    /* access modifiers changed from: private */
    public volatile boolean mEnableMobileDataHongbaoPolicy = true;
    /* access modifiers changed from: private */
    public long mEndTime = 0;
    private IntentFilter mFilter = null;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    /* access modifiers changed from: private */
    public OppoLuckyMoneyUtils mLMUtils = null;
    private boolean mRUSListenerRegistered = false;
    private long mStartTime = 0;
    /* access modifiers changed from: private */
    public int mVersionCode = 0;

    public static boolean getNewMsgDetected() {
        return sMODE2_NewMsgDetected;
    }

    public static boolean getLastViewItem() {
        return sMODE2_LastViewItem;
    }

    public static int getMode() {
        return SystemProperties.getInt("persist.oppo.debug.luckymoney", sBoostMode);
    }

    public static String getHbText() {
        return mHbText;
    }

    public static ArrayList<String> getHbLayout() {
        return mHbLayout;
    }

    public static ArrayList<String> getOpenHbActivity() {
        return mOpenHbActivity;
    }

    public static ArrayList<Integer> getHbLayoutNodes() {
        return mHbLayoutNodes;
    }

    public static void setLastViewItem(boolean isLastItem) {
        if (!mIsEnable || !mModeEnable) {
            sMODE2_LastViewItem = false;
            Log.w(TAG, "disable luckmoney detect, can not setLastViewItem");
            return;
        }
        sMODE2_LastViewItem = isLastItem;
    }

    public static void setNewMsgDetected(Handler h, boolean value) {
        sMODE2_NewMsgDetected = value;
        if (h != null) {
            h.removeCallbacks(sNewMsgTimeout);
            sLastNewMsgTimeoutHandler = h;
            if (value) {
                h.postDelayed(sNewMsgTimeout, 1000);
                return;
            }
            return;
        }
        Handler handler = sLastNewMsgTimeoutHandler;
        if (handler != null) {
            handler.removeCallbacks(sNewMsgTimeout);
        }
    }

    public void noteObtainedLastViewItem(View child) {
        if (child != null && !mChatView.isEmpty() && child.getClass().getName().contains(mChatView)) {
            if (!mIsEnable || !mModeEnable) {
                Log.w(TAG, "disable luckmoney detect");
                return;
            }
            int mode = getMode();
            if (mode != 2) {
                if (mode != 3) {
                    if (mode == 4 && sMODE2_NewMsgDetected) {
                        Log.d(TAG, " preProcessLuckMoney start");
                        this.mStartTime = System.currentTimeMillis();
                        View view = preProcessLuckMoney(child, mHbLayoutNodes, mHbLayout);
                        if (view != null) {
                            Log.d(TAG, " loadBitmapFromView start");
                            Bitmap bitmap = loadBitmapFromView(view);
                            Log.d(TAG, " compareBitmapAsync start");
                            compareBitmapAsync(bitmap);
                        }
                    }
                } else if (sMODE2_NewMsgDetected) {
                    this.mStartTime = System.currentTimeMillis();
                    if (findLuckyMoneyByText(child, mHbLayoutNodes, mHbLayout)) {
                        this.mEndTime = System.currentTimeMillis();
                        getLMManager().enableBoost(0, 2018);
                        setNewMsgDetected(null, false);
                    }
                }
            } else if (sMODE2_LastViewItem) {
                sMODE2_LastViewItem = false;
            }
        }
    }

    private View preProcessLuckMoney(View pView, ArrayList<Integer> nodes, ArrayList<String> nodeNameArray) {
        if (nodes.size() != nodeNameArray.size()) {
            Log.e(TAG, "Encounter bad configuration");
            return null;
        }
        View cView = preProcessLuckMoneyInternal(pView, nodes, nodeNameArray, 0);
        if (cView == null || cView.getHeight() == 0 || cView.getWidth() == 0) {
            return null;
        }
        float wRatio = ((float) cView.getWidth()) / ((float) pView.getWidth());
        float lmRatio = ((float) cView.getHeight()) / ((float) cView.getWidth());
        Log.d(TAG, "preProcessLuckMoney wRatio:" + wRatio + " lmRatio:" + lmRatio);
        if (0.28d >= ((double) lmRatio) || ((double) lmRatio) >= 0.42d || ((double) wRatio) <= 0.5d) {
            return null;
        }
        return cView;
    }

    private View preProcessLuckMoneyInternal(View view, ArrayList<Integer> nodes, ArrayList<String> nodeNameArray, int layout) {
        View childView;
        int nr_layout = nodes.size();
        int node = nodes.get(layout).intValue();
        String nodeName = nodeNameArray.get(layout);
        int next_layout = layout + 1;
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            if (vp.getChildCount() <= node || (childView = vp.getChildAt(node)) == null || !childView.getClass().getName().contains(nodeName)) {
                return null;
            }
            if (next_layout < nr_layout) {
                return preProcessLuckMoneyInternal(childView, nodes, nodeNameArray, next_layout);
            }
            Log.w(TAG, "LinearLayout width: " + childView.getWidth() + " height:" + childView.getHeight());
            return childView;
        }
        Log.w(TAG, "preProcessLuckMoneyInternal not view group");
        return null;
    }

    private Bitmap loadBitmapFromView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        if (w == 0 || h == 0) {
            return null;
        }
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(-1);
        v.layout(0, 0, w, h);
        v.draw(c);
        System.currentTimeMillis();
        return bmp;
    }

    private boolean findLuckyMoneyByText(View view, ArrayList<Integer> nodes, ArrayList<String> nodeNameArray) {
        View txtView;
        if (view == null || nodes.isEmpty() || nodeNameArray.isEmpty() || (txtView = findLuckMoneyInternal(view, nodes.get(0).intValue(), nodeNameArray.get(0), 0, nodes, nodeNameArray)) == null || !(txtView instanceof TextView) || !mHbText.equals(((TextView) txtView).getText().toString())) {
            return false;
        }
        Log.w(TAG, "find LM successful_3");
        return true;
    }

    private View findLuckMoneyInternal(View view, int node, String nodeName, int layer, ArrayList<Integer> nodes, ArrayList<String> nodeNameArray) {
        int totalLayer = nodes.size();
        int nextlayer = layer + 1;
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            if (vp.getChildCount() <= node) {
                return null;
            }
            View childView = vp.getChildAt(node);
            if (childView != null) {
                if (childView.getClass().getName().contains(nodeName)) {
                    if (nextlayer < totalLayer) {
                        return findLuckMoneyInternal(childView, nodes.get(nextlayer).intValue(), nodeNameArray.get(nextlayer), nextlayer, nodes, nodeNameArray);
                    }
                    return childView;
                }
            }
            return null;
        }
        Log.w(TAG, "findLMInternal not view group");
        return null;
    }

    private LMManager(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mFilter = new IntentFilter();
            this.mFilter.addAction(BROADCAST_ACTION_UPDATE_lUCKYMONEYINFO);
            return;
        }
        Log.e(TAG, "LMManager init with null Context");
    }

    public static synchronized LMManager getLMManager(Context context) {
        LMManager lMManager;
        synchronized (LMManager.class) {
            if (sLMManager == null) {
                sLMManager = new LMManager(context);
            }
            sLMManager.tryToInit();
            lMManager = sLMManager;
        }
        return lMManager;
    }

    public static LMManager getLMManager() {
        return getLMManager(null);
    }

    private boolean tryToInit() {
        if (sService == null) {
            sService = ILMServiceManager.Stub.asInterface(ServiceManager.getService(LUCKY_MONEY_SERVICE));
        }
        try {
            if (!mInitailized && sService != null && sService.isInitialized()) {
                init();
                mInitailized = true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return mInitailized;
    }

    private void init() {
        Context context;
        ILMServiceManager iLMServiceManager = sService;
        if (iLMServiceManager != null) {
            try {
                Bundle switchInfo = iLMServiceManager.getSwitchInfo();
                mIsEnable = switchInfo.getBoolean("isEnable", true);
                mSMEnable = switchInfo.getBoolean("smEnable", true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Slog.e(TAG, "get LUCKY_MONEY_SERVICE for switchinfo failed");
        }
        if (mIsEnable) {
            if (this.mHandlerThread == null) {
                this.mHandlerThread = new HandlerThread("luckmoney");
                this.mHandlerThread.start();
            }
            if (sBoostMode >= 0) {
                return;
            }
            if (MM_PACKAGENAME.equals(ActivityThread.currentPackageName())) {
                sBoostMode = 1;
                final ApplicationInfo appInfo = ActivityThread.currentApplication().getApplicationInfo();
                if (appInfo != null) {
                    if (mSMEnable) {
                        SharedPreferences sp = ActivityThread.currentApplication().getSharedPreferences("lm_prefs", 0);
                        int version = sp.getInt("lm_version", 0);
                        int mode = sp.getInt("lm_default_mode", -1);
                        Log.e(TAG, "sp.getInt lm_version version " + version + " appInfo.versionCode " + appInfo.versionCode);
                        if (version == appInfo.versionCode || (version == 0 && mode != -1)) {
                            getModeData(0, appInfo.versionCode, mode);
                        } else {
                            saveLMInPreference(true);
                            getModeData(0, appInfo.versionCode);
                        }
                        getModeEnableInfo(0, appInfo.versionCode);
                    } else {
                        rmLMInPreference();
                        getModeData(0, appInfo.versionCode);
                    }
                    this.mVersionCode = appInfo.versionCode;
                    if (!this.mRUSListenerRegistered && (context = this.mContext) != null) {
                        context.registerReceiver(new BroadcastReceiver() {
                            /* class com.oppo.luckymoney.LMManager.AnonymousClass2 */

                            @Override // android.content.BroadcastReceiver
                            public void onReceive(Context context, Intent intent) {
                                Log.d(LMManager.TAG, "Rus updated broadcast received");
                                LMManager.this.updateLuckyMoneyInfo(0, appInfo.versionCode);
                            }
                        }, this.mFilter);
                        this.mRUSListenerRegistered = true;
                    }
                }
            } else if (!QQ_PACKAGENAME.equals(ActivityThread.currentPackageName())) {
                sBoostMode = 0;
                sModeData = new Bundle();
            }
        }
    }

    private void initQuickValue() {
        Bundle bundle = sModeData;
        if (bundle != null) {
            luckyMoneyXmlVersion = bundle.getInt("xmlVersion", -1);
            mIsEnable = sModeData.getBoolean("isEnable", true);
            mSMEnable = sModeData.getBoolean("smEnable", true);
            if (luckyMoneyXmlVersion == 1) {
                sBoostMode = sModeData.getInt("mode", 0);
                mModeEnable = sModeData.getBoolean("isModeEnable", true);
                mChatView = sModeData.getString("chatView", "");
                sMODE_2_VALUE_RECEIVER_CLASS = sModeData.getString(MODE_2_RECEIVER_CLASS, "");
                mHbHashs = sModeData.getStringArrayList("hbHashs");
                mOpenHbActivity = sModeData.getStringArrayList("openHbActivity");
                mHbWidths = sModeData.getIntegerArrayList("hbWidths");
                mHbHeights = sModeData.getIntegerArrayList("hbHeights");
                mHbText = sModeData.getString("hbText", "");
                ArrayList<String> tempHbLayout = sModeData.getStringArrayList("hbLayout");
                if (tempHbLayout != null) {
                    mHbLayout = tempHbLayout;
                }
                ArrayList<Integer> tempHbLayoutNodes = sModeData.getIntegerArrayList("hbLayoutNodes");
                if (tempHbLayoutNodes != null) {
                    mHbLayoutNodes = tempHbLayoutNodes;
                }
            } else {
                sBoostMode = sModeData.getInt("mode", 0);
                sGetHash = sModeData.getBoolean(LuckyMoneyHelper.MODE_2_GET_HASH_MODE, false);
                sMODE_2_VALUE_RECEIVER_CLASS = sModeData.getString(MODE_2_RECEIVER_CLASS, "");
                sMODE_2_VALUE_HB_HASH = sModeData.getByteArray("hg_hash");
                sMODE_2_VALUE_HB_WIDTH = Integer.valueOf(sModeData.getString(MODE_2_HB_WIDTH, WifiEnterpriseConfig.ENGINE_DISABLE)).intValue();
                sMODE_2_VALUE_HB_HEIGHT = Integer.valueOf(sModeData.getString(MODE_2_HB_HEIGHT, WifiEnterpriseConfig.ENGINE_DISABLE)).intValue();
            }
            Log.e(TAG, "initQuickValue sBoostMode :" + sBoostMode + " mModeEnable:" + mModeEnable + " mIsEnable:" + mIsEnable + " mSMEnable:" + mSMEnable);
            if (sBoostMode == 4) {
                getHandler().post(new Runnable() {
                    /* class com.oppo.luckymoney.LMManager.AnonymousClass3 */

                    public void run() {
                        OppoLuckyMoneyUtils unused = LMManager.this.mLMUtils = OppoLuckyMoneyUtils.getInstance();
                    }
                });
            }
        }
    }

    private Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new Handler(this.mHandlerThread.getLooper());
        }
        return this.mHandler;
    }

    /* access modifiers changed from: private */
    public void updateLuckyMoneyInfo(int type, int versionCode) {
        getModeData(type, versionCode);
    }

    private void compareBitmapAsync(Bitmap src) {
        getHandler().post(new compareBitmapRunnable(src, getMode()));
    }

    private class compareBitmapRunnable implements Runnable {
        int mMethod;
        Bitmap mSrcBitmap;

        public compareBitmapRunnable(Bitmap src, int method) {
            this.mSrcBitmap = src;
            this.mMethod = method;
        }

        private Bitmap preProcessBitmap(Bitmap origin) {
            Log.e(LMManager.TAG, " start preProcessBitmap bitmap");
            Bitmap bmp = scaleBitmap(origin, 128, 128);
            if (!origin.isRecycled()) {
                origin.recycle();
            }
            return bmp;
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: android.graphics.Bitmap.createBitmap(android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, boolean):android.graphics.Bitmap
         arg types: [android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, int]
         candidates:
          android.graphics.Bitmap.createBitmap(android.util.DisplayMetrics, int[], int, int, int, int, android.graphics.Bitmap$Config):android.graphics.Bitmap
          android.graphics.Bitmap.createBitmap(android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, boolean):android.graphics.Bitmap */
        private Bitmap scaleBitmap(Bitmap origin, int new_width, int new_height) {
            int width = origin.getWidth();
            int height = origin.getHeight();
            Matrix matrix = new Matrix();
            matrix.preScale(((float) new_width) / ((float) width), ((float) new_height) / ((float) height));
            Bitmap newBmp = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
            if (!origin.isRecycled()) {
                origin.recycle();
            }
            return newBmp;
        }

        public void run() {
            Bitmap dstBitmap = preProcessBitmap(this.mSrcBitmap);
            if (this.mMethod == 4 && LMManager.this.mLMUtils != null) {
                Log.e(LMManager.TAG, "start detect using CNN");
                float ret = LMManager.this.mLMUtils.nativeDetect(dstBitmap);
                float unused = LMManager.this.mCNNReturn = ret;
                Log.e(LMManager.TAG, "compare CNN:" + ret);
                if (((double) ret) > 0.9d) {
                    Log.w(LMManager.TAG, "find LM using cnn");
                    if (SystemProperties.get("persist.oppo.debug.cnndisable", WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
                        Log.w(LMManager.TAG, "find LM successful_4");
                        long unused2 = LMManager.this.mEndTime = System.currentTimeMillis();
                        LMManager.this.enableBoost(0, 2019);
                        LMManager.setNewMsgDetected(null, false);
                    }
                }
                float unused3 = LMManager.this.mCNNReturn = 0.0f;
            }
            if (!dstBitmap.isRecycled()) {
                dstBitmap.recycle();
            }
        }
    }

    public void noteOpenLuckMoney() {
        if (!mIsEnable || !mSMEnable) {
            Log.w(TAG, "disable luckmoney change mode");
            return;
        }
        writeDCS(2020);
        getHandler().post(new SwitchDetectLMRunnable());
    }

    /* access modifiers changed from: private */
    public boolean isSwitchDetect(SharedPreferences sp) {
        if (sp.getInt("stop_detect", 0) == 0) {
            return true;
        }
        Log.e(TAG, "stop_detect");
        return false;
    }

    final class SwitchDetectLMRunnable implements Runnable {
        SwitchDetectLMRunnable() {
        }

        public void run() {
            try {
                if (ActivityThread.currentApplication() == null) {
                    return;
                }
                if (LMManager.sMODE2_NewMsgDetected) {
                    Log.w(LMManager.TAG, "SwitchDetectLMRunnable mCancelNewMsgDetect:" + LMManager.mCancelNewMsgDetect);
                    SharedPreferences sp = ActivityThread.currentApplication().getSharedPreferences("lm_prefs", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    if (!LMManager.this.isSwitchDetect(sp)) {
                        Log.e(LMManager.TAG, "isSwitchDetect(sp) == false");
                    } else if (!LMManager.mCancelNewMsgDetect) {
                        int count = sp.getInt("lm_invalid_count", 0);
                        if (!LMManager.mModeEnable || count >= 3) {
                            boolean unused = LMManager.mCancelNewMsgDetect = true;
                            return;
                        }
                        editor.remove("lm_invalid_count");
                        editor.putInt("lm_invalid_count", count + 1);
                        editor.commit();
                    } else {
                        ApplicationInfo appInfo = ActivityThread.currentApplication().getApplicationInfo();
                        if (appInfo != null) {
                            LMManager.mCurrentBoostMode = sp.getInt("lm_default_mode", 1);
                            LMManager.mCurrentBoostMode++;
                            if (LMManager.mModeEnableInfo != null) {
                                while (LMManager.mCurrentBoostMode <= 4 && !LMManager.mModeEnableInfo.get(Integer.valueOf(LMManager.mCurrentBoostMode)).booleanValue()) {
                                    Log.d(LMManager.TAG, "skip over boost mode " + LMManager.mCurrentBoostMode);
                                    LMManager.mCurrentBoostMode = LMManager.mCurrentBoostMode + 1;
                                }
                            } else {
                                Log.d(LMManager.TAG, "unable to skip disabled modes");
                            }
                            Log.w(LMManager.TAG, "SwitchDetectLMRunnable mCurrentBoostMode:" + LMManager.mCurrentBoostMode);
                            if (LMManager.mCurrentBoostMode > 4) {
                                editor.remove("stop_detect");
                                editor.putInt("stop_detect", 1);
                            } else {
                                editor.remove("lm_default_mode");
                                editor.putInt("lm_default_mode", LMManager.mCurrentBoostMode);
                                Bundle unused2 = LMManager.this.getModeData(0, appInfo.versionCode, LMManager.mCurrentBoostMode);
                                LMManager.this.writeDCS(2021);
                            }
                        }
                        boolean unused3 = LMManager.mCancelNewMsgDetect = false;
                        editor.remove("lm_invalid_count");
                        editor.putInt("lm_invalid_count", 0);
                        editor.commit();
                    }
                }
            } catch (Exception e) {
                Log.e(LMManager.TAG, "saveCacheDrawableIdInPreference fail");
                e.printStackTrace();
            }
        }
    }

    private HashMap<Integer, Boolean> getModeEnableInfo(int type, int versionCode) {
        if (tryToInit()) {
            try {
                mModeEnableInfo = (HashMap) sService.getModeEnableInfo(type, versionCode).getSerializable("modeEnableInfo");
                if (mModeEnableInfo == null) {
                    Log.d(TAG, "got null modeEnableInfo");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Service not ready for getting mode info");
        }
        return mModeEnableInfo;
    }

    public int getBoostMode() {
        return sBoostMode;
    }

    public Bundle getModeData() {
        return sModeData;
    }

    public boolean isGetHash() {
        return sGetHash;
    }

    public byte[] getHBHash() {
        return sMODE_2_VALUE_HB_HASH;
    }

    public static boolean initBitmapCheck(int width, int height) {
        int heightIndex = mHbHeights.indexOf(Integer.valueOf(height));
        if (heightIndex != mHbWidths.indexOf(Integer.valueOf(width)) || heightIndex < 0) {
            return false;
        }
        sMODE_2_VALUE_HB_WIDTH = width;
        sMODE_2_VALUE_HB_HEIGHT = height;
        if (mHbHashs.size() <= heightIndex) {
            return false;
        }
        sMODE_2_VALUE_HB_HASH = getHBHash(mHbHashs.get(heightIndex));
        return true;
    }

    private static byte[] getHBHash(String hash) {
        if (hash == null || hash.length() != 32) {
            return null;
        }
        byte[] cc = new byte[(hash.length() / 2)];
        int i = 0;
        while (i < cc.length) {
            try {
                cc[i] = (byte) (Integer.parseInt(hash.substring(i * 2, (i * 2) + 2), 16) & 255);
                i++;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }
        }
        return cc;
    }

    public void enableBoost(int timeout, int code) {
        if (!mIsEnable) {
            Slog.d(TAG, "enableBoost is disable");
        } else if (tryToInit()) {
            try {
                if (mSMEnable) {
                    saveLMInPreference(false);
                }
                boolean enable = sService.enableBoost(Process.myPid(), Process.myUid(), timeout, code);
                writeDCS(code);
                if (enable && this.mEnableMobileDataHongbaoPolicy) {
                    synchronized (sLMManager) {
                        this.mEnableMobileDataHongbaoPolicy = false;
                    }
                    sService.enableMobileBoost();
                    getHandler().postDelayed(new Runnable() {
                        /* class com.oppo.luckymoney.LMManager.AnonymousClass4 */

                        public void run() {
                            synchronized (LMManager.sLMManager) {
                                boolean unused = LMManager.this.mEnableMobileDataHongbaoPolicy = true;
                                Log.e(LMManager.TAG, "mEnableMobileDataHongbaoPolicy = true");
                            }
                        }
                    }, 500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Service not ready for boosting");
        }
    }

    private void saveLMInPreference(final boolean clear) {
        new AsyncTask<Void, Void, Void>() {
            /* class com.oppo.luckymoney.LMManager.AnonymousClass5 */

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... params) {
                try {
                    if (ActivityThread.currentApplication() != null) {
                        if (LMManager.MM_PACKAGENAME.equals(ActivityThread.currentApplication().getPackageName())) {
                            Log.d(LMManager.TAG, "AsyncTask, saveLMPreference");
                            SharedPreferences sp = ActivityThread.currentApplication().getSharedPreferences("lm_prefs", 0);
                            SharedPreferences.Editor editor = sp.edit();
                            if (clear) {
                                editor.clear().commit();
                                Log.d(LMManager.TAG, "clear LMInPreference");
                            } else {
                                int prefVersionCode = sp.getInt("lm_version", 0);
                                if (prefVersionCode != LMManager.this.mVersionCode) {
                                    editor.remove("lm_version");
                                    editor.putInt("lm_version", LMManager.this.mVersionCode);
                                    editor.remove("lm_detect");
                                    editor.putInt("lm_detect", 1);
                                    Log.w(LMManager.TAG, "save detect lm");
                                    editor.commit();
                                } else {
                                    Log.d(LMManager.TAG, "prefVersionCode: " + prefVersionCode + "; mVersionCode: " + LMManager.this.mVersionCode);
                                }
                            }
                            return null;
                        }
                    }
                    Log.d(LMManager.TAG, "currentApplication: " + ActivityThread.currentApplication().getPackageName());
                    return null;
                } catch (Exception e) {
                    Log.e(LMManager.TAG, "save detect lm fail " + ActivityThread.currentApplication());
                    e.printStackTrace();
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void result) {
                super.onPostExecute((Object) result);
            }
        }.execute(new Void[0]);
    }

    private void rmLMInPreference() {
        new AsyncTask<Void, Void, Void>() {
            /* class com.oppo.luckymoney.LMManager.AnonymousClass6 */

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... params) {
                try {
                    Log.d(LMManager.TAG, "AsyncTask, rmLMPreference");
                    if (ActivityThread.currentApplication() != null) {
                        if (LMManager.MM_PACKAGENAME.equals(ActivityThread.currentApplication().getPackageName())) {
                            ActivityThread.currentApplication().deleteSharedPreferences("lm_prefs");
                            return null;
                        }
                    }
                    return null;
                } catch (Exception e) {
                    Log.e(LMManager.TAG, "delete lm fail " + ActivityThread.currentApplication());
                    e.printStackTrace();
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void result) {
                super.onPostExecute((Object) result);
            }
        }.execute(new Void[0]);
    }

    public String getLuckyMoneyInfo(int type) {
        if (tryToInit()) {
            try {
                return sService.getLuckyMoneyInfo(type);
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e(TAG, "Service not ready for getting lucky info");
            return null;
        }
    }

    private Bundle getModeData(int type, int versionCode) {
        return getModeData(type, versionCode, -1);
    }

    /* access modifiers changed from: private */
    public Bundle getModeData(int type, int versionCode, int defaultValue) {
        ILMServiceManager iLMServiceManager = sService;
        if (iLMServiceManager != null) {
            try {
                sModeData = iLMServiceManager.getModeData(type, versionCode, defaultValue);
                initQuickValue();
                return sModeData;
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Slog.e(TAG, "Can't get service.");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void writeDCS(int type) {
        Bundle DCSData = new Bundle();
        DCSData.putString(KEY_TYPE, getenableBoostType(type));
        DCSData.putString(KEY_DETECT_RETURN, String.valueOf(this.mCNNReturn));
        if (type == 2020 || type == 2021) {
            DCSData.putString(KEY_SPEND_TIME, WifiEnterpriseConfig.ENGINE_DISABLE);
        } else {
            DCSData.putString(KEY_SPEND_TIME, String.valueOf(this.mEndTime - this.mStartTime));
        }
        DCSData.putString(KEY_CURRENT_TIME, String.valueOf(System.currentTimeMillis()));
        ILMServiceManager iLMServiceManager = sService;
        if (iLMServiceManager != null) {
            try {
                iLMServiceManager.writeDCS(DCSData);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Slog.e(TAG, "writeDCS Can't get service.");
        }
    }

    private String getenableBoostType(int code) {
        switch (code) {
            case 2015:
                return DETECT_NETWORK;
            case 2016:
            default:
                return "";
            case 2017:
                return DETECT_IMAGEVIEW;
            case 2018:
                return DETECT_TEXT;
            case 2019:
                return DETECT_CNN;
            case 2020:
                return OPEN_LUCKYMONEY;
            case 2021:
                return SWITCH_MODE;
        }
    }
}
