package com.android.server.wm;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.IColorWindow;
import android.view.IColorWindowImpl;
import android.view.IWindow;
import android.view.InputEvent;
import android.view.WindowManager;
import com.android.internal.os.TransferPipe;
import com.color.screenshot.ColorLongshotDump;
import com.color.util.ColorLog;
import com.color.view.ColorWindowUtils;
import com.color.view.analysis.ColorWindowNode;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class ColorLongshotWindowDump {
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final String JSON_STATUS_FULLSCREEN = "status_fullScreen";
    private static final String JSON_WINDOW_CONTENT = "window_content";
    private static final String JSON_WINDOW_SECURE = "window_secure";
    private static final String JSON_WINDOW_UNSUPPORTED = "window_unsupported";
    private static final String JSON_WINDOW_VOLUME = "window_volume";
    private static final long LONGSHOT_DUMP_TIMEOUT = 1000;
    private static final String TAG = "LongshotDump";
    private final ColorLongshotWindowCompatible mCompatible;
    private FileDescriptor mFileDescriptor = null;
    private ColorLongshotMainWindow mLastInjectWindow = null;
    private ColorLongshotMainWindow mLastMainWindow = null;
    private final WindowManagerService mService;

    public ColorLongshotWindowDump(Context context, WindowManagerService service) {
        this.mCompatible = new ColorLongshotWindowCompatible(context);
        this.mService = service;
    }

    public void setFileDescriptor(FileDescriptor fd) {
        this.mFileDescriptor = fd;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0035, code lost:
        if (r0 == null) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
        r0 = com.android.server.wm.ColorLongshotWindowDump.DBG;
        com.color.util.ColorLog.d(r0, com.android.server.wm.ColorLongshotWindowDump.TAG, "  -------- dumpWindows : " + r0);
        flushWriter(r20);
        dumpFrame(r20, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005b, code lost:
        if (r0.hasSecure() == false) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005d, code lost:
        dumpSecure(r20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x006b, code lost:
        if (r0.hasUnsupported() == false) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006d, code lost:
        dumpUnsupported(r20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x007b, code lost:
        if (r0.hasVolume() == false) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007d, code lost:
        dumpVolume(r20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0080, code lost:
        dumpForShot(r15, r20, r0, r0, r0, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0091, code lost:
        com.color.util.ColorLog.d(com.android.server.wm.ColorLongshotWindowDump.DBG, com.android.server.wm.ColorLongshotWindowDump.TAG, "  -------- dumpWindows : can not find main window, disable longshot");
        flushWriter(r20);
        dumpUnsupported(r20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a6, code lost:
        printStatusFull(r20);
        r10 = true;
     */
    public boolean dumpWindows(PrintWriter pw, String name, String[] args) {
        Throwable th;
        boolean result = DBG;
        long origId = Binder.clearCallingIdentity();
        boolean isLongshot = isDumpLongshot(name);
        boolean isScreenshot = isDumpScreenshot(name);
        if ((isLongshot || isScreenshot) && this.mFileDescriptor != null) {
            FileDescriptor fd = this.mFileDescriptor;
            List<ColorWindowNode> systemWindows = new ArrayList<>();
            List<ColorWindowNode> floatWindows = new ArrayList<>();
            synchronized (this.mService.mWindowMap) {
                try {
                    ColorLongshotMainWindow mainWindow = getTopWindowLocked(systemWindows, floatWindows, args);
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
        Binder.restoreCallingIdentity(origId);
        return result;
    }

    public void notifyConnectedLocked(DisplayContent displayContent, boolean isConnected) {
        IColorWindow client;
        try {
            this.mLastMainWindow = null;
            if (isConnected || (!isConnected && this.mLastMainWindow == null)) {
                this.mLastMainWindow = this.mCompatible.traversalWindows(displayContent, this.mCompatible.getDispMetrics(displayContent), this.mCompatible.getRealMetrics(displayContent), this.mCompatible.getStatusBarHeight(), new WindowDummyListener(), null);
            }
            if (this.mLastMainWindow != null && (client = IColorWindowImpl.asInterface(this.mLastMainWindow.getMainWindow().mClient)) != null) {
                client.longshotNotifyConnected(isConnected);
            }
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "notifyConnectedLocked ERROR : " + Log.getStackTraceString(e));
        } catch (Exception e2) {
            boolean z2 = DBG;
            ColorLog.e(z2, TAG, "notifyConnectedLocked ERROR : " + Log.getStackTraceString(e2));
        }
    }

    public int getSurfaceLayerLocked(DisplayContent displayContent, int type) {
        return this.mCompatible.getSurfaceLayerLocked(displayContent, type);
    }

    public void injectInputLocked(DisplayContent displayContent, InputEvent event, int mode) {
        IColorWindow client;
        try {
            ColorLongshotMainWindow w = this.mCompatible.traversalWindows(displayContent, this.mCompatible.getDispMetrics(displayContent), this.mCompatible.getRealMetrics(displayContent), this.mCompatible.getStatusBarHeight(), new WindowDummyListener(), null);
            if (w != null && (client = IColorWindowImpl.asInterface(w.getMainWindow().mClient)) != null) {
                client.longshotInjectInput(event, mode);
            }
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "injectInputLocked ERROR : " + Log.getStackTraceString(e));
        } catch (Exception e2) {
            boolean z2 = DBG;
            ColorLog.e(z2, TAG, "injectInputLocked ERROR : " + Log.getStackTraceString(e2));
        }
    }

    public void injectInputBeginLocked(DisplayContent displayContent) {
        IColorWindow client;
        try {
            this.mLastInjectWindow = null;
            this.mLastInjectWindow = this.mCompatible.traversalWindows(displayContent, this.mCompatible.getDispMetrics(displayContent), this.mCompatible.getRealMetrics(displayContent), this.mCompatible.getStatusBarHeight(), new WindowDummyListener(), null);
            if (this.mLastInjectWindow != null && (client = IColorWindowImpl.asInterface(this.mLastMainWindow.getMainWindow().mClient)) != null) {
                client.longshotInjectInputBegin();
            }
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "injectInputBeginLocked ERROR : " + Log.getStackTraceString(e));
        } catch (Exception e2) {
            boolean z2 = DBG;
            ColorLog.e(z2, TAG, "injectInputBeginLocked ERROR : " + Log.getStackTraceString(e2));
        }
    }

    public void injectInputEndLocked(DisplayContent displayContent) {
        IColorWindow client;
        try {
            if (this.mLastInjectWindow == null) {
                this.mLastInjectWindow = this.mCompatible.traversalWindows(displayContent, this.mCompatible.getDispMetrics(displayContent), this.mCompatible.getRealMetrics(displayContent), this.mCompatible.getStatusBarHeight(), new WindowDummyListener(), null);
            }
            if (this.mLastInjectWindow != null && (client = IColorWindowImpl.asInterface(this.mLastMainWindow.getMainWindow().mClient)) != null) {
                client.longshotInjectInputEnd();
            }
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "injectInputEndLocked ERROR : " + Log.getStackTraceString(e));
        } catch (Exception e2) {
            boolean z2 = DBG;
            ColorLog.e(z2, TAG, "injectInputEndLocked ERROR : " + Log.getStackTraceString(e2));
        }
    }

    public IBinder getLongshotWindowByTypeLocked(DisplayContent displayContent, int type) {
        DisplayContent topFocusedDisplay;
        WindowManagerService windowManagerService = this.mService;
        if (windowManagerService == null || (topFocusedDisplay = windowManagerService.mRoot.getTopFocusedDisplayContent()) == null) {
            return null;
        }
        return this.mCompatible.getWindowByTypeLocked(displayContent, type, topFocusedDisplay.mFocusedApp);
    }

    private void flushWriter(PrintWriter pw) {
        pw.flush();
    }

    private void dumpFrame(PrintWriter pw, ColorLongshotMainWindow win) {
        try {
            JSONObject jsonNode = new JSONObject();
            jsonNode.put(JSON_WINDOW_CONTENT, win.getContentFrame().flattenToString());
            pw.println(jsonNode.toString());
        } catch (JSONException e) {
            pw.println("Failure while dumping the window for frame : " + e);
        }
    }

    private void dumpBoolean(PrintWriter pw, String key) {
        try {
            JSONObject jsonNode = new JSONObject();
            jsonNode.put(key, true);
            pw.println(jsonNode.toString());
        } catch (JSONException e) {
            pw.println("Failure while dumping the window for " + key + " : " + e);
        }
    }

    private void dumpSecure(PrintWriter pw) {
        dumpBoolean(pw, JSON_WINDOW_SECURE);
    }

    private void dumpUnsupported(PrintWriter pw) {
        dumpBoolean(pw, JSON_WINDOW_UNSUPPORTED);
    }

    private void dumpVolume(PrintWriter pw) {
        dumpBoolean(pw, JSON_WINDOW_VOLUME);
    }

    private void dumpForShot(FileDescriptor fd, PrintWriter pw, ColorLongshotMainWindow mainWin, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows, boolean isLongshot) {
        IColorWindow client = IColorWindowImpl.asInterface(mainWin.getMainWindow().mClient);
        if (client != null) {
            try {
                TransferPipe tp = new TransferPipe();
                try {
                    FileDescriptor wfd = tp.getWriteFd().getFileDescriptor();
                    if (isLongshot) {
                        client.longshotDump(wfd, systemWindows, floatWindows);
                    } else {
                        client.screenshotDump(wfd);
                    }
                    tp.go(fd, (long) LONGSHOT_DUMP_TIMEOUT);
                } finally {
                    tp.kill();
                }
            } catch (IOException e) {
                pw.println("Failure while dumping the window for longshot: " + e);
            } catch (RemoteException e2) {
                pw.println("Got a RemoteException while dumping the window for longshot");
            }
        }
    }

    private ColorLongshotMainWindow getTopWindowLocked(List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows, String[] args) {
        DisplayContent displayContent = this.mService.getDefaultDisplayContentLocked();
        return this.mCompatible.traversalWindows(displayContent, this.mCompatible.getDispMetrics(displayContent), this.mCompatible.getRealMetrics(displayContent), this.mCompatible.getStatusBarHeight(), new WindowDumpListener(systemWindows, floatWindows), args);
    }

    private void printStatusFull(PrintWriter pw) {
        boolean isFullBar = DBG;
        try {
            WindowState windowState = this.mService.mRoot.getTopFocusedDisplayContent().mCurrentFocus;
            if (windowState != null && windowState.getAppToken() == null && windowState.mAttrs.type == 2000) {
                isFullBar = true;
            }
            JSONObject jsonNode = new JSONObject();
            jsonNode.put(JSON_STATUS_FULLSCREEN, isFullBar);
            pw.println("/" + jsonNode.toString());
        } catch (JSONException e) {
            pw.println("Failure while dumping printStatusFull: " + e);
        } catch (Exception e2) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "printStatusFull ERROR : " + Log.getStackTraceString(e2));
        }
    }

    private boolean isDumpLongshot(String name) {
        return "longshot".equals(name);
    }

    private boolean isDumpScreenshot(String name) {
        return "screenshot".equals(name);
    }

    private class WindowDummyListener implements ColorWindowTraversalListener {
        private WindowDummyListener() {
        }

        public void printDetect(String msg, CharSequence windowName) {
        }

        public void printWindow(String msg, CharSequence windowName) {
        }

        public void collectSystemWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, WindowManager.LayoutParams winAttrs) {
        }

        public void collectFloatWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, WindowManager.LayoutParams winAttrs) {
        }

        public boolean hasSystemDocorView(IWindow service) {
            return ColorLongshotWindowDump.DBG;
        }
    }

    /* access modifiers changed from: private */
    public class WindowDumpListener implements ColorWindowTraversalListener {
        private final List<ColorWindowNode> mFloatWindows;
        private final List<ColorWindowNode> mSystemWindows;

        public WindowDumpListener(List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
            this.mSystemWindows = systemWindows;
            this.mFloatWindows = floatWindows;
        }

        public void printDetect(String msg, CharSequence windowName) {
            boolean z = ColorLongshotWindowDump.DBG;
            ColorLog.d(z, ColorLongshotWindowDump.TAG, "            [Detected][" + msg + "] " + ((Object) windowName));
        }

        public void printWindow(String msg, CharSequence windowName) {
            boolean z = ColorLongshotWindowDump.DBG;
            ColorLog.d(z, ColorLongshotWindowDump.TAG, " findWindow [" + msg + "] " + ((Object) windowName));
        }

        public void collectSystemWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, WindowManager.LayoutParams winAttrs) {
            collectWindows(service, windowTitle, surfaceLayer, winAttrs, this.mSystemWindows, "System");
        }

        public void collectFloatWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, WindowManager.LayoutParams winAttrs) {
            collectWindows(service, windowTitle, surfaceLayer, winAttrs, this.mFloatWindows, "Float");
        }

        public boolean hasSystemDocorView(IWindow service) {
            IColorWindow client = IColorWindowImpl.asInterface(service);
            if (client == null) {
                return ColorLongshotWindowDump.DBG;
            }
            try {
                ColorWindowNode window = client.longshotCollectWindow((boolean) ColorLongshotWindowDump.DBG, (boolean) ColorLongshotWindowDump.DBG);
                if (window == null || !isDecorView(window.getClassName())) {
                    return ColorLongshotWindowDump.DBG;
                }
                return true;
            } catch (RemoteException e) {
                boolean z = ColorLongshotWindowDump.DBG;
                ColorLog.e(z, ColorLongshotWindowDump.TAG, "collectDecorWindows ERROR : " + Log.getStackTraceString(e));
                return ColorLongshotWindowDump.DBG;
            } catch (Exception e2) {
                boolean z2 = ColorLongshotWindowDump.DBG;
                ColorLog.e(z2, ColorLongshotWindowDump.TAG, "collectDecorWindows ERROR : " + Log.getStackTraceString(e2));
                return ColorLongshotWindowDump.DBG;
            }
        }

        private boolean isDecorView(String className) {
            if (className.endsWith("DecorView")) {
                return true;
            }
            return ColorLongshotWindowDump.DBG;
        }

        private void collectWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, WindowManager.LayoutParams winAttrs, List<ColorWindowNode> windows, String tag) {
            RemoteException e;
            Exception e2;
            IColorWindow client = IColorWindowImpl.asInterface(service);
            if (client != null) {
                try {
                    ColorWindowNode window = client.longshotCollectWindow(ColorWindowUtils.isStatusBar(winAttrs.type), ColorWindowUtils.isNavigationBar(winAttrs.type));
                    if (window != null) {
                        try {
                            window.setSurfaceLayer(surfaceLayer);
                        } catch (RemoteException e3) {
                            e = e3;
                            boolean z = ColorLongshotWindowDump.DBG;
                            ColorLog.e(z, ColorLongshotWindowDump.TAG, "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e));
                        } catch (Exception e4) {
                            e2 = e4;
                            boolean z2 = ColorLongshotWindowDump.DBG;
                            ColorLog.e(z2, ColorLongshotWindowDump.TAG, "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e2));
                        }
                        try {
                            windows.add(window);
                        } catch (RemoteException e5) {
                            e = e5;
                            boolean z3 = ColorLongshotWindowDump.DBG;
                            ColorLog.e(z3, ColorLongshotWindowDump.TAG, "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e));
                        } catch (Exception e6) {
                            e2 = e6;
                            boolean z22 = ColorLongshotWindowDump.DBG;
                            ColorLog.e(z22, ColorLongshotWindowDump.TAG, "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e2));
                        }
                    }
                    boolean z4 = ColorLongshotWindowDump.DBG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("  ---- collect");
                    sb.append(tag);
                    sb.append("Windows : title=");
                    try {
                        sb.append((Object) windowTitle);
                        sb.append(" : ");
                        sb.append(window);
                        ColorLog.d(z4, ColorLongshotWindowDump.TAG, sb.toString());
                    } catch (RemoteException e7) {
                        e = e7;
                    } catch (Exception e8) {
                        e2 = e8;
                        boolean z222 = ColorLongshotWindowDump.DBG;
                        ColorLog.e(z222, ColorLongshotWindowDump.TAG, "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e2));
                    }
                } catch (RemoteException e9) {
                    e = e9;
                    boolean z32 = ColorLongshotWindowDump.DBG;
                    ColorLog.e(z32, ColorLongshotWindowDump.TAG, "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e));
                } catch (Exception e10) {
                    e2 = e10;
                    boolean z2222 = ColorLongshotWindowDump.DBG;
                    ColorLog.e(z2222, ColorLongshotWindowDump.TAG, "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e2));
                }
            }
        }
    }
}
