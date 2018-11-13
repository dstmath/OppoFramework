package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.IColorLongshotWindowManager;
import android.view.IColorWindow;
import android.view.IColorWindowImpl;
import android.view.IWindow;
import android.view.WindowManagerInternal;
import com.android.internal.os.TransferPipe;
import com.android.server.LocalServices;
import com.color.screenshot.ColorLongshotUtils;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.IColorLongshotViewCallback;
import com.color.util.ColorLog;
import com.color.view.analysis.ColorViewAnalysis;
import com.color.view.analysis.ColorViewNodeInfo;
import com.color.view.analysis.ColorWindowNode;
import com.color.view.analysis.IColorViewAnalysisCallback;
import com.color.view.analysis.IColorViewAnalysisCallback.Stub;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ColorLongshotHelper implements IColorLongshotWindowManager {
    private static final int LONGSHOT_ANALYSIS_RETRY_COUNT = 10;
    private static final long LONGSHOT_ANALYSIS_RETRY_DELAY = 150;
    private static final long LONGSHOT_DUMP_TIMEOUT = 1000;
    private long mAnalysisTime = 0;
    private final Context mContext;
    private FileDescriptor mFileDescriptor = null;
    private final H mHandler = new H(this, null);
    private int mRetryCount = 0;
    private final WindowManagerService mService;
    protected final Class<?> mTagClass = getClass();

    private class H extends Handler {
        public static final int MSG_LONGSHOT_ANALYSIS_VIEW = 0;
        public static final int MSG_LONGSHOT_NOTIFY_CONNECTED = 1;

        /* synthetic */ H(ColorLongshotHelper this$0, H h) {
            this();
        }

        private H() {
        }

        public void handleMessage(Message msg) {
            HashMap hashMap;
            switch (msg.what) {
                case 0:
                    hashMap = ColorLongshotHelper.this.mService.mWindowMap;
                    synchronized (hashMap) {
                        ColorLongshotHelper.this.analysisViewLocked(ColorLongshotHelper.this.mService.getDefaultDisplayContentLocked(), msg.obj, 1 == msg.arg1, 1 == msg.arg2);
                        break;
                    }
                case 1:
                    hashMap = ColorLongshotHelper.this.mService.mWindowMap;
                    synchronized (hashMap) {
                        ColorLongshotHelper.this.notifyConnectedLocked(ColorLongshotHelper.this.mService.getDefaultDisplayContentLocked(), 1 == msg.arg1);
                        break;
                    }
                default:
                    return;
            }
        }
    }

    private interface WindowTraversalListener {
        void collectFloatWindows(IWindow iWindow, CharSequence charSequence, int i, int i2);

        void collectSystemWindows(IWindow iWindow, CharSequence charSequence, int i, int i2);

        void printWindow(String str, CharSequence charSequence, String str2);
    }

    private abstract class WindowTraversalAdapter implements WindowTraversalListener {
        private final String mLogKey;

        public WindowTraversalAdapter(String logKey) {
            this.mLogKey = logKey;
        }

        protected String getLogKey() {
            return this.mLogKey;
        }

        public void collectSystemWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, int windowType) {
        }

        public void collectFloatWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, int windowType) {
        }
    }

    private abstract class WindowPrintAdapter extends WindowTraversalAdapter {
        public WindowPrintAdapter(String logKey) {
            super(logKey);
        }

        public void printWindow(String msg, CharSequence windowName, String tag) {
            ColorLog.d("LongshotDump", " findWindow [" + msg + "] " + windowName);
        }
    }

    private class WindowAnalysisListener extends WindowPrintAdapter {
        private final List<ColorViewNodeInfo> mFloatNodes;
        private final List<ColorViewNodeInfo> mSystemNodes;

        public WindowAnalysisListener(String logKey, List<ColorViewNodeInfo> systemNodes, List<ColorViewNodeInfo> floatNodes) {
            super(logKey);
            this.mSystemNodes = systemNodes;
            this.mFloatNodes = floatNodes;
        }

        public void collectSystemWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, int windowType) {
            collectWindows(service, windowTitle, surfaceLayer, this.mSystemNodes, "System");
        }

        public void collectFloatWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, int windowType) {
            collectWindows(service, windowTitle, surfaceLayer, this.mFloatNodes, "Float");
        }

        private void collectWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, List<ColorViewNodeInfo> nodes, String tag) {
            IColorWindow client = IColorWindowImpl.asInterface(service);
            if (client != null) {
                try {
                    client.longshotCollectRoot(nodes);
                    ColorLog.d(getLogKey(), ColorLongshotHelper.this.mTagClass, new Object[]{"  ---- collect", tag, "Windows : title=", windowTitle});
                } catch (RemoteException e) {
                    ColorLog.e(ColorLongshotHelper.this.mTagClass, new Object[]{"collect", tag, "Windows ERROR : ", Log.getStackTraceString(e)});
                } catch (Exception e2) {
                    ColorLog.e(ColorLongshotHelper.this.mTagClass, new Object[]{"collect", tag, "Windows ERROR : ", Log.getStackTraceString(e2)});
                }
            }
        }
    }

    private class WindowDummyListener extends WindowTraversalAdapter {
        public WindowDummyListener(String logKey) {
            super(logKey);
        }

        public void printWindow(String msg, CharSequence windowName, String tag) {
        }
    }

    private class WindowDumpListener extends WindowPrintAdapter {
        private final List<ColorWindowNode> mFloatWindows;
        private final List<ColorWindowNode> mSystemWindows;

        public WindowDumpListener(String logKey, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
            super(logKey);
            this.mSystemWindows = systemWindows;
            this.mFloatWindows = floatWindows;
        }

        public void collectSystemWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, int windowType) {
            collectWindows(service, windowTitle, surfaceLayer, windowType, this.mSystemWindows, "System");
        }

        public void collectFloatWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, int windowType) {
            collectWindows(service, windowTitle, surfaceLayer, windowType, this.mFloatWindows, "Float");
        }

        private void collectWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, int windowType, List<ColorWindowNode> windows, String tag) {
            IColorWindow client = IColorWindowImpl.asInterface(service);
            if (client != null) {
                try {
                    ColorWindowNode window = client.longshotCollectWindow(ColorLongshotUtils.isStatusBar(windowType), ColorLongshotUtils.isNavigationBar(windowType));
                    if (window != null) {
                        window.setSurfaceLayer(surfaceLayer);
                        windows.add(window);
                    }
                    ColorLog.d("LongshotDump", "  ---- collect" + tag + "Windows : title=" + windowTitle + " : " + window);
                } catch (RemoteException e) {
                    ColorLog.e("LongshotDump", "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e));
                } catch (Exception e2) {
                    ColorLog.e("LongshotDump", "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e2));
                }
            }
        }
    }

    private class WindowGetInfoListener extends WindowPrintAdapter {
        public WindowGetInfoListener(String logKey) {
            super(logKey);
        }
    }

    public ColorLongshotHelper(Context context, WindowManagerService service) {
        this.mContext = context;
        this.mService = service;
    }

    public void getFocusedWindowFrame(Rect frame) {
        if (frame != null) {
            WindowManagerInternal windowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            IBinder focusedWindow = windowManagerInternal.getFocusedWindowToken();
            if (focusedWindow != null) {
                windowManagerInternal.getWindowFrame(focusedWindow, frame);
            }
        }
    }

    public void getLongshotViewInfo(ColorLongshotViewInfo viewInfo) {
        synchronized (this.mService.mWindowMap) {
            getViewInfoLocked(this.mService.getDefaultDisplayContentLocked(), viewInfo);
        }
    }

    public int getLongshotSurfaceLayerByType(int type) {
        synchronized (this.mService.mWindowMap) {
            WindowList windowList = this.mService.getDefaultDisplayContentLocked().getWindowList();
            for (int winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                WindowState w = (WindowState) windowList.get(winNdx);
                if (w.mAttrs.type == type) {
                    int surfaceLayer = w.getSurfaceLayer();
                    return surfaceLayer;
                }
            }
            return 0;
        }
    }

    public int getLongshotSurfaceLayer() {
        return getLongshotSurfaceLayerByType(2303);
    }

    public void longshotNotifyConnected(boolean isConnected) {
        int i = 1;
        Message msg = this.mHandler.obtainMessage(1);
        if (!isConnected) {
            i = 0;
        }
        msg.arg1 = i;
        this.mHandler.sendMessage(msg);
    }

    public boolean isNavigationBarVisible() {
        return this.mService.mPolicy.isNavigationBarVisible();
    }

    public void longshotAnalysisView(IColorViewAnalysisCallback callback, boolean isInit) {
        int i;
        Message msg = this.mHandler.obtainMessage(0);
        if (isInit) {
            i = 1;
        } else {
            i = 0;
        }
        msg.arg1 = i;
        msg.arg2 = 0;
        msg.obj = callback;
        this.mHandler.sendMessage(msg);
    }

    public void getLongshotViewInfoAsync(ColorLongshotViewInfo viewInfo, IColorLongshotViewCallback callback) {
        synchronized (this.mService.mWindowMap) {
            getViewInfoAsyncLocked(this.mService.getDefaultDisplayContentLocked(), viewInfo, callback);
        }
    }

    public boolean isShortcutsPanelShow() {
        return this.mService.mPolicy.isShortcutsPanelShow();
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        int isInit = 0;
        ColorLongshotViewInfo viewInfo;
        int layer;
        boolean result;
        switch (code) {
            case 10016:
                Rect frame;
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() != 0) {
                    frame = (Rect) Rect.CREATOR.createFromParcel(data);
                } else {
                    frame = null;
                }
                getFocusedWindowFrame(frame);
                reply.writeNoException();
                if (frame != null) {
                    reply.writeInt(1);
                    frame.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case 10021:
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() != 0) {
                    viewInfo = (ColorLongshotViewInfo) ColorLongshotViewInfo.CREATOR.createFromParcel(data);
                } else {
                    viewInfo = null;
                }
                getLongshotViewInfo(viewInfo);
                reply.writeNoException();
                if (viewInfo != null) {
                    reply.writeInt(1);
                    viewInfo.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case 10023:
                data.enforceInterface("android.view.IWindowManager");
                layer = getLongshotSurfaceLayer();
                reply.writeNoException();
                reply.writeInt(layer);
                return true;
            case 10024:
                data.enforceInterface("android.view.IWindowManager");
                layer = getLongshotSurfaceLayerByType(data.readInt());
                reply.writeNoException();
                reply.writeInt(layer);
                return true;
            case 10026:
                boolean isInit2;
                data.enforceInterface("android.view.IWindowManager");
                IColorViewAnalysisCallback callback = Stub.asInterface(data.readStrongBinder());
                if (data.readInt() == 1) {
                    isInit2 = true;
                }
                longshotAnalysisView(callback, isInit2);
                return true;
            case 10027:
                boolean isConnected;
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() == 1) {
                    isConnected = true;
                } else {
                    isConnected = false;
                }
                longshotNotifyConnected(isConnected);
                return true;
            case 10028:
                data.enforceInterface("android.view.IWindowManager");
                result = isNavigationBarVisible();
                reply.writeNoException();
                if (result) {
                    isInit = 1;
                }
                reply.writeInt(isInit);
                return true;
            case 10029:
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() != 0) {
                    viewInfo = (ColorLongshotViewInfo) ColorLongshotViewInfo.CREATOR.createFromParcel(data);
                } else {
                    viewInfo = null;
                }
                getLongshotViewInfoAsync(viewInfo, IColorLongshotViewCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            case 10031:
                data.enforceInterface("android.view.IWindowManager");
                result = isShortcutsPanelShow();
                reply.writeNoException();
                if (result) {
                    isInit = 1;
                }
                reply.writeInt(isInit);
                return true;
            default:
                return false;
        }
    }

    public void setFileDescriptor(FileDescriptor fd) {
        this.mFileDescriptor = fd;
    }

    public boolean dumpWindows(PrintWriter pw, String name) {
        long origId = Binder.clearCallingIdentity();
        if (!"longshot".equals(name) || this.mFileDescriptor == null) {
            Binder.restoreCallingIdentity(origId);
            return false;
        }
        WindowState w;
        FileDescriptor fd = this.mFileDescriptor;
        String logKey = "log.key.long_shot.window";
        List<ColorWindowNode> systemWindows = new ArrayList();
        List<ColorWindowNode> floatWindows = new ArrayList();
        synchronized (this.mService.mWindowMap) {
            w = getTopWindowLocked(logKey, systemWindows, floatWindows);
        }
        if (w != null) {
            ColorLog.d("LongshotDump", "  -------- dumpWindows : current display window");
            dumpLongshot(fd, pw, w, systemWindows, floatWindows);
        }
        Binder.restoreCallingIdentity(origId);
        return true;
    }

    private WindowState getTopWindowLocked(String logKey, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        DisplayContent displayContent = this.mService.getDefaultDisplayContentLocked();
        return traversalWindows(displayContent, getDispMetrics(displayContent), getRealMetrics(displayContent), getStatusBarHeight(), new WindowDumpListener(logKey, systemWindows, floatWindows), "dumpLongshot");
    }

    private void dumpLongshot(FileDescriptor fd, PrintWriter pw, WindowState w, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        IColorWindow client = IColorWindowImpl.asInterface(w.mClient);
        if (client != null) {
            pw.flush();
            TransferPipe tp;
            try {
                tp = new TransferPipe();
                client.longshotDump(tp.getWriteFd().getFileDescriptor(), systemWindows, floatWindows);
                tp.go(fd, 1000);
                tp.kill();
            } catch (IOException e) {
                pw.println("Failure while dumping the window for longshot: " + e);
            } catch (RemoteException e2) {
                pw.println("Got a RemoteException while dumping the window for longshot");
            } catch (Throwable th) {
                tp.kill();
            }
        }
    }

    private String getOwningPackage(WindowState w) {
        String owningPackage = w.getOwningPackage();
        if (owningPackage != null || w.mAttachedWindow == null) {
            return owningPackage;
        }
        return w.mAttachedWindow.getOwningPackage();
    }

    private CharSequence getWindowTitle(WindowState w) {
        return w.mAttrs.getTitle();
    }

    private CharSequence getWindowName(WindowState w) {
        CharSequence title = getWindowTitle(w);
        if (TextUtils.isEmpty(title)) {
            return getOwningPackage(w);
        }
        return title;
    }

    private boolean isFullScreenWindow(WindowState w, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusBarHeight) {
        Rect contentFrame = w.getContentFrameLw();
        int width = contentFrame.width();
        if (width != dispMetrics.widthPixels && width != realMetrics.widthPixels) {
            return false;
        }
        if (contentFrame.bottom != dispMetrics.heightPixels && contentFrame.bottom != realMetrics.heightPixels) {
            return false;
        }
        if (contentFrame.top == 0 || contentFrame.top == statusBarHeight) {
            return true;
        }
        return false;
    }

    private boolean isTouchableWindow(WindowState w) {
        return (w.mAttrs.flags & 16) == 0;
    }

    private boolean needRetryAnalysis(ColorViewAnalysis result) {
        boolean needRetry = result.getCurrNodes().size() == 0 && result.getLastNodes().size() > 0;
        if (!needRetry) {
            return needRetry;
        }
        if (this.mRetryCount > 10) {
            this.mRetryCount = 0;
            return false;
        }
        this.mRetryCount++;
        ColorLog.d("log.key.long_shot.analysis", this.mTagClass, new Object[]{"retry analysis=", Integer.valueOf(this.mRetryCount)});
        return needRetry;
    }

    private void analysisViewDelayed(IColorViewAnalysisCallback callback, boolean isInit, boolean isRetry, long delayMillis) {
        int i;
        int i2 = 1;
        Message msg = this.mHandler.obtainMessage(0);
        if (isInit) {
            i = 1;
        } else {
            i = 0;
        }
        msg.arg1 = i;
        if (!isRetry) {
            i2 = 0;
        }
        msg.arg2 = i2;
        msg.obj = callback;
        this.mHandler.sendMessageDelayed(msg, delayMillis);
    }

    private DisplayMetrics getDispMetrics(DisplayContent displayContent) {
        DisplayMetrics dispMetrics = new DisplayMetrics();
        displayContent.getDisplay().getMetrics(dispMetrics);
        return dispMetrics;
    }

    private DisplayMetrics getRealMetrics(DisplayContent displayContent) {
        DisplayMetrics dispMetrics = new DisplayMetrics();
        displayContent.getDisplay().getRealMetrics(dispMetrics);
        return dispMetrics;
    }

    private int getStatusBarHeight() {
        return this.mContext.getResources().getDimensionPixelSize(201654274);
    }

    private boolean isSmallSize(int value, int total) {
        return value < total / 5;
    }

    private boolean isSmallFloatWindow(WindowState w, DisplayMetrics dipMetrics) {
        Rect contentFrame = w.getContentFrameLw();
        int width = contentFrame.width();
        if (isSmallSize(contentFrame.width(), dipMetrics.widthPixels) || isSmallSize(contentFrame.height(), dipMetrics.heightPixels)) {
            return true;
        }
        return false;
    }

    private WindowState traversalWindows(DisplayContent displayContent, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusbarHeight, WindowTraversalListener listener, String tag) {
        WindowState mainWindow = null;
        WindowList windowList = displayContent.getWindowList();
        for (int winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
            WindowState w = (WindowState) windowList.get(winNdx);
            CharSequence windowName = getWindowName(w);
            if (mainWindow != null) {
                listener.printWindow("not MainDisplayWindow  ", windowName, tag);
            } else if (w.mClient == null) {
                listener.printWindow("null WindowClient      ", windowName, tag);
            } else if (!w.isReadyForDisplay()) {
                listener.printWindow("not ReadyForDisplay    ", windowName, tag);
            } else if (!isTouchableWindow(w)) {
                listener.printWindow("not TouchableWindow    ", windowName, tag);
            } else if (ColorLongshotUtils.isInputMethodWindow(w.mAttrs.type, getWindowTitle(w))) {
                listener.printWindow("is  InputMethodWindow  ", windowName, tag);
            } else {
                String owningPackage = getOwningPackage(w);
                if (ColorLongshotUtils.isScreenshotApp(owningPackage)) {
                    listener.printWindow("is  ScreenshotApp      ", windowName, tag);
                } else if (ColorLongshotUtils.isExServiceUiApp(owningPackage)) {
                    listener.printWindow("is  ExServiceUiApp     ", windowName, tag);
                } else {
                    CharSequence windowTitle = getWindowTitle(w);
                    if (ColorLongshotUtils.isSystemUiApp(owningPackage)) {
                        if (ColorLongshotUtils.isStatusBar(w.mAttrs.type)) {
                            if (w.mAttrs.height != -1) {
                                listener.printWindow("is  SystemUi StatusBar ", windowName, tag);
                                listener.collectSystemWindows(w.mClient, windowTitle, w.getSurfaceLayer(), w.mAttrs.type);
                            }
                        } else if (ColorLongshotUtils.isSystemUiBar(w.mAttrs.type, windowTitle)) {
                            listener.printWindow("is  SystemUiApp and Bar", windowName, tag);
                            listener.collectSystemWindows(w.mClient, windowTitle, w.getSurfaceLayer(), w.mAttrs.type);
                        } else if (!isFullScreenWindow(w, dispMetrics, realMetrics, statusbarHeight)) {
                            listener.printWindow("is  SystemUiApp not Bar", windowName, tag);
                            listener.collectFloatWindows(w.mClient, windowTitle, w.getSurfaceLayer(), w.mAttrs.type);
                        }
                    }
                    if (isSmallFloatWindow(w, dispMetrics)) {
                        listener.printWindow("is  SmallFloatWindow   ", windowName, tag);
                        listener.collectFloatWindows(w.mClient, windowTitle, w.getSurfaceLayer(), w.mAttrs.type);
                    } else {
                        listener.printWindow("is  MainDisplayWindow  ", windowName, tag);
                        mainWindow = w;
                    }
                }
            }
        }
        return mainWindow;
    }

    private void getViewInfoLocked(DisplayContent displayContent, ColorLongshotViewInfo viewInfo) {
        if (viewInfo != null) {
            try {
                viewInfo.clear();
                DisplayMetrics dispMetrics = getDispMetrics(displayContent);
                DisplayMetrics realMetrics = getRealMetrics(displayContent);
                int statusBarHeight = getStatusBarHeight();
                String logKey = "log.key.long_shot.window";
                WindowState w = traversalWindows(displayContent, dispMetrics, realMetrics, statusBarHeight, new WindowGetInfoListener(logKey), "getViewInfo");
                if (w != null) {
                    IColorWindow client = IColorWindowImpl.asInterface(w.mClient);
                    if (client != null) {
                        viewInfo.setFullScreen(isFullScreenWindow(w, dispMetrics, realMetrics, statusBarHeight));
                        viewInfo.setAppToken(w.mAppToken != null);
                        client.getLongshotViewInfo(viewInfo);
                        viewInfo.setMainWindow(w.mClient.asBinder());
                        ColorLog.d(logKey, this.mTagClass, new Object[]{"  -------- getViewInfoLocked : current display window : isFullScreen=", Boolean.valueOf(isFullScreen), ", mAppToken=", w.mAppToken, ", isScrollable=", Boolean.valueOf(viewInfo.isScrollable())});
                    }
                }
            } catch (RemoteException e) {
                ColorLog.e(this.mTagClass, new Object[]{"getViewInfoLocked ERROR : ", Log.getStackTraceString(e)});
            } catch (Exception e2) {
                ColorLog.e(this.mTagClass, new Object[]{"getViewInfoLocked ERROR : ", Log.getStackTraceString(e2)});
            }
        }
    }

    private void getViewInfoAsyncLocked(DisplayContent displayContent, ColorLongshotViewInfo viewInfo, IColorLongshotViewCallback callback) {
        if (viewInfo != null) {
            try {
                viewInfo.clear();
                DisplayMetrics dispMetrics = getDispMetrics(displayContent);
                DisplayMetrics realMetrics = getRealMetrics(displayContent);
                int statusBarHeight = getStatusBarHeight();
                String logKey = "log.key.long_shot.window";
                WindowState w = traversalWindows(displayContent, dispMetrics, realMetrics, statusBarHeight, new WindowGetInfoListener(logKey), "getViewInfoAsync");
                if (w != null) {
                    IColorWindow client = IColorWindowImpl.asInterface(w.mClient);
                    if (client != null) {
                        viewInfo.setFullScreen(isFullScreenWindow(w, dispMetrics, realMetrics, statusBarHeight));
                        viewInfo.setAppToken(w.mAppToken != null);
                        viewInfo.setMainWindow(w.mClient.asBinder());
                        client.getLongshotViewInfoAsync(viewInfo, callback);
                        ColorLog.d(logKey, this.mTagClass, new Object[]{"  -------- getViewInfoAsyncLocked : current display window : isFullScreen=", Boolean.valueOf(isFullScreen), ", mAppToken=", w.mAppToken});
                    }
                }
            } catch (RemoteException e) {
                ColorLog.e(this.mTagClass, new Object[]{"getViewInfoLocked ERROR : ", Log.getStackTraceString(e)});
            } catch (Exception e2) {
                ColorLog.e(this.mTagClass, new Object[]{"getViewInfoLocked ERROR : ", Log.getStackTraceString(e2)});
            }
        }
    }

    private void analysisViewLocked(DisplayContent displayContent, IColorViewAnalysisCallback callback, boolean isInit, boolean isRetry) {
        if (!isRetry) {
            try {
                this.mAnalysisTime = SystemClock.uptimeMillis();
            } catch (RemoteException e) {
                ColorLog.e(this.mTagClass, new Object[]{"analysisViewLocked ERROR : ", Log.getStackTraceString(e)});
                return;
            } catch (Exception e2) {
                ColorLog.e(this.mTagClass, new Object[]{"analysisViewLocked ERROR : ", Log.getStackTraceString(e2)});
                return;
            }
        }
        List<ColorViewNodeInfo> systemNodes = new ArrayList();
        List<ColorViewNodeInfo> floatNodes = new ArrayList();
        String logKey = "log.key.long_shot.spend";
        WindowState w = traversalWindows(displayContent, getDispMetrics(displayContent), getRealMetrics(displayContent), getStatusBarHeight(), new WindowAnalysisListener(logKey, systemNodes, floatNodes), "analysisView");
        if (w != null) {
            ColorViewAnalysis result = new ColorViewAnalysis();
            IColorWindow client = IColorWindowImpl.asInterface(w.mClient);
            if (client != null) {
                if (ColorLog.getDebug(logKey)) {
                    ColorLog.d(this.mTagClass, new Object[]{"------------------------------------"});
                    ColorLog.d(this.mTagClass, new Object[]{"analysisViewLocked ", getWindowName(w), " : systemNodes=", Integer.valueOf(systemNodes.size()), " : floatNodes=", Integer.valueOf(floatNodes.size()), ", isInit=", Boolean.valueOf(isInit), ", isRetry=", Boolean.valueOf(isRetry)});
                }
                client.longshotAnalysisView(result, systemNodes, floatNodes, isInit, isRetry);
                result.setMainWindow(w.mClient.asBinder());
            }
            if (needRetryAnalysis(result)) {
                analysisViewDelayed(callback, isInit, true, LONGSHOT_ANALYSIS_RETRY_DELAY);
                return;
            }
            if (callback != null) {
                callback.onViewAnalysisFinished(result);
            }
            this.mAnalysisTime = SystemClock.uptimeMillis() - this.mAnalysisTime;
            ColorLog.d(logKey, this.mTagClass, new Object[]{"analysis finished! spend=", Long.valueOf(this.mAnalysisTime)});
            this.mAnalysisTime = 0;
        }
    }

    private void notifyConnectedLocked(DisplayContent displayContent, boolean isConnected) {
        try {
            WindowState w = traversalWindows(displayContent, getDispMetrics(displayContent), getRealMetrics(displayContent), getStatusBarHeight(), new WindowDummyListener("log.key.long_shot.service"), "notifyConnected");
            if (w != null) {
                IColorWindow client = IColorWindowImpl.asInterface(w.mClient);
                if (client != null) {
                    client.longshotNotifyConnected(isConnected);
                }
            }
        } catch (RemoteException e) {
            ColorLog.e(this.mTagClass, new Object[]{"notifyConnectedLocked ERROR : ", Log.getStackTraceString(e)});
        } catch (Exception e2) {
            ColorLog.e(this.mTagClass, new Object[]{"notifyConnectedLocked ERROR : ", Log.getStackTraceString(e2)});
        }
    }
}
