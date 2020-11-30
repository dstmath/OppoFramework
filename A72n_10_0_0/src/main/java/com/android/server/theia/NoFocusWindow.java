package com.android.server.theia;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public final class NoFocusWindow {
    public static final int CHECK_FOCUS_WINDOW_ERROR_MSG = 1;
    public static final String FOCUS_ACTIVITY_PARAM = "focusedActivityName";
    public static final String FOCUS_PACKAGE_PARAM = "focusedPackageName";
    public static final String FOCUS_WINDOW_PARAM = "focusedWindowName";
    public static final String HUNG_CONFIG_ENABLE = "1";
    private static final String NULL_STRING = "null";
    private static final String TAG = "Theia.NoFocusWindow";
    public static boolean isNoFocusNow = false;
    private static int mCheckFreezeScreenDelayTime = 6000;
    private static NoFocusWindow mNoFocusWindow = null;
    private Context mContext;
    private boolean mEnable = false;
    private StringBuilder mFocusWindowInfo = new StringBuilder();
    private String mFocusedActivity = null;
    private String mFocusedPackage = null;
    private String mFocusedWindow = null;
    private String mHungConfigEnable = NULL_STRING;
    private int mHungConfigStatus = -1;
    private TheiaXMLParser mTheiaXMLParser = null;
    private NFWindowHandler nfWindowHandler = null;

    private class NFWindowHandler extends Handler {
        public NFWindowHandler(Looper looper) {
            super(looper, null, false);
        }

        public void gzipFile(String source_filepath, String destinaton_zip_filepath) {
            byte[] buffer = new byte[1024];
            try {
                GZIPOutputStream gzipOuputStream = new GZIPOutputStream(new FileOutputStream(destinaton_zip_filepath));
                FileInputStream fileInput = new FileInputStream(source_filepath);
                while (true) {
                    int bytes_read = fileInput.read(buffer);
                    if (bytes_read > 0) {
                        gzipOuputStream.write(buffer, 0, bytes_read);
                    } else {
                        fileInput.close();
                        gzipOuputStream.finish();
                        gzipOuputStream.close();
                        Slog.d(NoFocusWindow.TAG, "The file was compressed successfully!");
                        return;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                NoFocusWindow.isNoFocusNow = true;
                Log.e(NoFocusWindow.TAG, "handleMessage2 CHECK_FOCUS_WINDOW_ERROR_MSG " + NoFocusWindow.this.mFocusedPackage);
                SystemProperties.set("sys.theia.focus_pkg", NoFocusWindow.this.mFocusedPackage);
                SystemProperties.set("sys.theia.critical_head", "Exception: NoFocusWindow");
                TheiaUtil.getInstance().sendTheiaEvent(NoFocusWindow.this.mFocusedPackage, TheiaConst.THEIA_ST_NFW, NoFocusWindow.this.mContext);
            }
        }
    }

    public NoFocusWindow(Context context) {
        this.mContext = context;
        this.nfWindowHandler = new NFWindowHandler(BackgroundThread.getHandler().getLooper());
        this.mTheiaXMLParser = new TheiaXMLParser(context);
        this.mEnable = this.mTheiaXMLParser.getNoFocusWindowEnable();
        this.mTheiaXMLParser.initUpdateBroadcastReceiver();
    }

    public static synchronized NoFocusWindow getInstance(Context context) {
        NoFocusWindow noFocusWindow;
        synchronized (NoFocusWindow.class) {
            synchronized (NoFocusWindow.class) {
                if (mNoFocusWindow == null) {
                    mNoFocusWindow = new NoFocusWindow(context);
                }
                noFocusWindow = mNoFocusWindow;
            }
            return noFocusWindow;
        }
        return noFocusWindow;
    }

    public int init() {
        return 0;
    }

    public boolean check(String pkgName) {
        if (!this.mEnable) {
            return true;
        }
        try {
            init();
            this.mFocusedPackage = pkgName;
            this.nfWindowHandler.removeMessages(1);
            Message msg = this.nfWindowHandler.obtainMessage(1);
            if (msg != null) {
                msg.obj = pkgName;
                this.nfWindowHandler.sendMessageDelayed(msg, (long) mCheckFreezeScreenDelayTime);
                Log.d(TAG, "FocusWindowErrorScene CheckFreezeScreen");
            }
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }

    public boolean cancelCheck(String pkgName) {
        if (!this.mEnable) {
            return true;
        }
        try {
            this.mFocusedPackage = pkgName;
            if (this.nfWindowHandler != null && this.nfWindowHandler.hasMessages(1)) {
                this.nfWindowHandler.removeMessages(1);
                Log.d(TAG, "FocusWindowErrorScene cancelCheckFreezeScreen");
            }
            this.mFocusWindowInfo.delete(0, this.mFocusWindowInfo.length());
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }
}
