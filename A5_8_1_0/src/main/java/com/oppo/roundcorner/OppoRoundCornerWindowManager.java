package com.oppo.roundcorner;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class OppoRoundCornerWindowManager {
    public static final int BOTTOMWINDOW = 2;
    public static final int LEFTWINDOW = 3;
    private static final int MSG_UPDATEORIENTATION = 101;
    private static final int MSG_UPDATEORIENTATION_DELAY = 0;
    public static final int RIGHTWINDOW = 4;
    public static final String TAG = "OPPORoundCorner";
    public static final int TOPWINDOW = 1;
    private static Context mContext;
    private static Handler mHandler = null;
    private static boolean mHeteromorphism = false;
    private static final CharSequence mWindowNameBottom = "OPPORCViewBottom";
    private static final CharSequence mWindowNameLeft = "OPPORCViewLeft";
    private static final CharSequence mWindowNameRight = "OPPORCViewRight";
    private static final CharSequence mWindowNameTop = "OPPORCViewTop";
    private static int sCornerHeightBottom = 21;
    private static int sCornerHeightTop = 21;
    private static LayoutParams sLandBottomParams;
    private static OppoRoundCornerView sLandBottomWindow;
    private static LayoutParams sLandTopParams;
    private static OppoRoundCornerView sLandTopWindow;
    private static int sOrientation = 0;
    private static LayoutParams sPortBottomParams;
    private static OppoRoundCornerView sPortBottomWindow;
    private static LayoutParams sPortTopParams;
    private static OppoRoundCornerView sPortTopWindow;
    private static int sScreenHeight = 2160;
    private static int sScreenWidth = 1080;
    private static int sTempOrientation = 0;
    private static WindowManager sWindowManager;

    public static void getScreenInfo(Context context) {
        WindowManager windowManager = getWindowManager(context);
        DisplayMetrics dm = new DisplayMetrics();
        mContext = context;
        mHandler = new Handler(mContext.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:
                        OppoRoundCornerWindowManager.setOrientation(OppoRoundCornerWindowManager.sTempOrientation);
                        OppoRoundCornerWindowManager.updateLayout(OppoRoundCornerWindowManager.mContext);
                        return;
                    default:
                        return;
                }
            }
        };
        mHeteromorphism = context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        windowManager.getDefaultDisplay().getRealMetrics(dm);
        sScreenWidth = dm.widthPixels;
        sScreenHeight = dm.heightPixels;
        Log.d("OPPORoundCorner", "sScreenWidth=" + sScreenWidth + ", sScreenHeight=" + sScreenHeight + " mHeteromorphism=" + mHeteromorphism);
    }

    public static void createPortraitWindow(Context context) {
        LayoutParams layoutParams;
        WindowManager windowManager = getWindowManager(context);
        if (sPortTopWindow == null) {
            sPortTopWindow = new OppoRoundCornerView(context, 1);
            sPortTopWindow.setFlag(1);
            sPortTopWindow.setCornerType(1);
            if (sPortTopParams == null) {
                sPortTopParams = new LayoutParams();
                sPortTopParams.setTitle(mWindowNameTop);
                sPortTopParams.type = 2302;
                sPortTopParams.format = 1;
                sPortTopParams.flags = 24;
                layoutParams = sPortTopParams;
                layoutParams.flags |= 67108864;
                sPortTopParams.gravity = 8388659;
                sPortTopParams.width = sScreenWidth;
                sPortTopParams.height = sCornerHeightTop;
                sPortTopParams.x = 0;
                sPortTopParams.y = 0;
            }
            windowManager.addView(sPortTopWindow, sPortTopParams);
        }
        if (sPortBottomWindow == null) {
            sPortBottomWindow = new OppoRoundCornerView(context, 2);
            sPortBottomWindow.setFlag(2);
            sPortBottomWindow.setCornerType(2);
            if (sPortBottomParams == null) {
                sPortBottomParams = new LayoutParams();
                sPortBottomParams.setTitle(mWindowNameBottom);
                sPortBottomParams.type = 2302;
                sPortBottomParams.format = 1;
                sPortBottomParams.flags = 24;
                layoutParams = sPortBottomParams;
                layoutParams.flags |= 67108864;
                sPortBottomParams.gravity = 8388659;
                sPortBottomParams.width = sScreenWidth;
                sPortBottomParams.height = sCornerHeightBottom;
                sPortBottomParams.x = 0;
                sPortBottomParams.y = sScreenHeight - sCornerHeightBottom;
            }
            windowManager.addView(sPortBottomWindow, sPortBottomParams);
        }
    }

    public static void createLandscapeWindow(Context context) {
        LayoutParams layoutParams;
        WindowManager windowManager = getWindowManager(context);
        if (sLandTopWindow == null) {
            sLandTopWindow = new OppoRoundCornerView(context, 3);
            sLandTopWindow.setFlag(3);
            sLandTopWindow.setCornerType(3);
            sLandTopWindow.setVisibility(8);
            if (sLandTopParams == null) {
                sLandTopParams = new LayoutParams();
                sLandTopParams.setTitle(mWindowNameLeft);
                sLandTopParams.type = 2302;
                sLandTopParams.format = 1;
                sLandTopParams.flags = 24;
                layoutParams = sLandTopParams;
                layoutParams.flags |= 67108864;
                sLandTopParams.gravity = 8388659;
                sLandTopParams.width = sCornerHeightTop;
                sLandTopParams.height = sScreenWidth;
                sLandTopParams.x = 0;
                sLandTopParams.y = 0;
            }
            windowManager.addView(sLandTopWindow, sLandTopParams);
        }
        if (sLandBottomWindow == null) {
            sLandBottomWindow = new OppoRoundCornerView(context, 4);
            sLandBottomWindow.setFlag(4);
            sLandBottomWindow.setCornerType(4);
            sLandBottomWindow.setVisibility(8);
            if (sLandBottomParams == null) {
                sLandBottomParams = new LayoutParams();
                sLandBottomParams.setTitle(mWindowNameRight);
                sLandBottomParams.type = 2302;
                sLandBottomParams.format = 1;
                sLandBottomParams.flags = 24;
                layoutParams = sLandBottomParams;
                layoutParams.flags |= 67108864;
                sLandBottomParams.gravity = 8388659;
                sLandBottomParams.width = sCornerHeightBottom;
                sLandBottomParams.height = sScreenWidth;
                sLandBottomParams.x = sScreenHeight - sCornerHeightBottom;
                sLandBottomParams.y = 0;
            }
            windowManager.addView(sLandBottomWindow, sLandBottomParams);
        }
    }

    public static void setOrientation(int orientation) {
        if (sOrientation != orientation) {
            sOrientation = orientation;
            if (orientation == 0) {
                sPortTopWindow.setCornerType(1);
                sPortBottomWindow.setCornerType(2);
                sPortTopWindow.setVisibility(0);
                sPortBottomWindow.setVisibility(0);
                sLandTopWindow.setVisibility(8);
                sLandBottomWindow.setVisibility(8);
            } else if (orientation == 1) {
                sLandTopWindow.setCornerType(3);
                sLandBottomWindow.setCornerType(4);
                sLandTopWindow.setVisibility(0);
                sLandBottomWindow.setVisibility(0);
                sPortTopWindow.setVisibility(8);
                sPortBottomWindow.setVisibility(8);
            } else if (orientation == 2) {
                sPortTopWindow.setCornerType(2);
                sPortBottomWindow.setCornerType(1);
                sPortTopWindow.setVisibility(0);
                sPortBottomWindow.setVisibility(0);
                sLandTopWindow.setVisibility(8);
                sLandBottomWindow.setVisibility(8);
            } else if (orientation == 3) {
                sLandTopWindow.setCornerType(4);
                sLandBottomWindow.setCornerType(3);
                sLandTopWindow.setVisibility(0);
                sLandBottomWindow.setVisibility(0);
                sPortTopWindow.setVisibility(8);
                sPortBottomWindow.setVisibility(8);
            }
        }
    }

    public static WindowManager getWindowManager(Context context) {
        if (sWindowManager == null) {
            sWindowManager = (WindowManager) context.getSystemService("window");
        }
        return sWindowManager;
    }

    public static void updateLayout(Context context) {
        WindowManager windowManager = getWindowManager(context);
        sPortTopParams.height = sPortTopWindow.getBitmapHeight();
        sPortBottomParams.height = sPortBottomWindow.getBitmapHeight();
        sPortBottomParams.y = sScreenHeight - sPortBottomWindow.getBitmapHeight();
        sLandTopParams.width = sLandTopWindow.getBitmapWidth();
        sLandBottomParams.width = sLandBottomWindow.getBitmapWidth();
        sLandBottomParams.x = sScreenHeight - sLandBottomWindow.getBitmapWidth();
        windowManager.updateViewLayout(sPortTopWindow, sPortTopParams);
        windowManager.updateViewLayout(sPortBottomWindow, sPortBottomParams);
        windowManager.updateViewLayout(sLandTopWindow, sLandTopParams);
        windowManager.updateViewLayout(sLandBottomWindow, sLandBottomParams);
        sPortTopWindow.invalidate();
        sPortBottomWindow.invalidate();
        sLandTopWindow.invalidate();
        sLandTopWindow.invalidate();
    }

    public static void UpdateOrientation(int orientation) {
        if (mHeteromorphism) {
            Log.d("OPPORoundCorner", "UpdateOrientation orientation= " + orientation + " sTempOrientation= " + sTempOrientation);
            sTempOrientation = orientation;
            if (mHandler != null) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(101), 0);
            }
        }
    }
}
