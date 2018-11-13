package com.android.server.wm;

import android.content.Context;
import android.content.res.Resources;
import android.os.Binder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.IColorWindow;
import android.view.IColorWindowImpl;
import android.view.IWindow;
import android.view.InputEvent;
import android.view.WindowManager.LayoutParams;
import com.android.internal.os.TransferPipe;
import com.android.server.wm.ColorLongshotWindowCompatible.WindowTraversalListener;
import com.color.screenshot.ColorLongshotDump;
import com.color.screenshot.ColorLongshotUtils;
import com.color.util.ColorLog;
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
    private static final String JSON_WINDOW_UNSUPPORTED = "window_unsupported";
    private static final long LONGSHOT_DUMP_TIMEOUT = 1000;
    private static final String TAG = "LongshotDump";
    private final ColorLongshotWindowCompatible mCompatible = new ColorLongshotWindowCompatible();
    private FileDescriptor mFileDescriptor = null;
    private ColorLongshotMainWindow mLastInjectWindow = null;
    private ColorLongshotMainWindow mLastMainWindow = null;
    private final Resources mResources;
    private final WindowManagerService mService;

    private class WindowDummyListener implements WindowTraversalListener {
        /* synthetic */ WindowDummyListener(ColorLongshotWindowDump this$0, WindowDummyListener -this1) {
            this();
        }

        private WindowDummyListener() {
        }

        public void printWindow(String msg, CharSequence windowName) {
        }

        public void collectSystemWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, LayoutParams winAttrs) {
        }

        public void collectFloatWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, LayoutParams winAttrs) {
        }
    }

    private class WindowDumpListener implements WindowTraversalListener {
        private final List<ColorWindowNode> mFloatWindows;
        private final List<ColorWindowNode> mSystemWindows;

        public WindowDumpListener(List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
            this.mSystemWindows = systemWindows;
            this.mFloatWindows = floatWindows;
        }

        public void printWindow(String msg, CharSequence windowName) {
            ColorLog.d(ColorLongshotWindowDump.DBG, ColorLongshotWindowDump.TAG, " findWindow [" + msg + "] " + windowName);
        }

        public void collectSystemWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, LayoutParams winAttrs) {
            collectWindows(service, windowTitle, surfaceLayer, winAttrs, this.mSystemWindows, "System");
        }

        public void collectFloatWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, LayoutParams winAttrs) {
            collectWindows(service, windowTitle, surfaceLayer, winAttrs, this.mFloatWindows, "Float");
        }

        private void collectWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, LayoutParams winAttrs, List<ColorWindowNode> windows, String tag) {
            IColorWindow client = IColorWindowImpl.asInterface(service);
            if (client != null) {
                try {
                    ColorWindowNode window = client.longshotCollectWindow(ColorLongshotUtils.isStatusBar(winAttrs.type), ColorLongshotUtils.isNavigationBar(winAttrs.type));
                    if (window != null) {
                        window.setSurfaceLayer(surfaceLayer);
                        windows.add(window);
                    }
                    ColorLog.d(ColorLongshotWindowDump.DBG, ColorLongshotWindowDump.TAG, "  ---- collect" + tag + "Windows : title=" + windowTitle + " : " + window);
                } catch (RemoteException e) {
                    ColorLog.e(ColorLongshotWindowDump.DBG, ColorLongshotWindowDump.TAG, "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e));
                } catch (Exception e2) {
                    ColorLog.e(ColorLongshotWindowDump.DBG, ColorLongshotWindowDump.TAG, "collect" + tag + "Windows ERROR : " + Log.getStackTraceString(e2));
                }
            }
        }
    }

    public ColorLongshotWindowDump(Context context, WindowManagerService service) {
        this.mResources = context.getResources();
        this.mService = service;
    }

    public void setFileDescriptor(FileDescriptor fd) {
        this.mFileDescriptor = fd;
    }

    public boolean dumpWindows(PrintWriter pw, String name) {
        boolean result = false;
        long origId = Binder.clearCallingIdentity();
        if ("longshot".equals(name) && this.mFileDescriptor != null) {
            FileDescriptor fd = this.mFileDescriptor;
            List<ColorWindowNode> systemWindows = new ArrayList();
            List<ColorWindowNode> floatWindows = new ArrayList();
            ColorLongshotMainWindow mainWindow = null;
            synchronized (this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    mainWindow = getTopWindowLocked(systemWindows, floatWindows);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (mainWindow != null) {
                boolean isUnsupported = mainWindow.isUnsupported();
                ColorLog.d(DBG, TAG, "  -------- dumpWindows : main window isUnsupported=" + isUnsupported);
                if (isUnsupported) {
                    dumpUnsupported(pw);
                } else {
                    dumpLongshot(fd, pw, mainWindow, systemWindows, floatWindows);
                }
            }
            result = true;
        }
        Binder.restoreCallingIdentity(origId);
        return result;
    }

    public void notifyConnectedLocked(DisplayContent displayContent, boolean isConnected) {
        try {
            this.mLastMainWindow = null;
            if (isConnected || (!isConnected && this.mLastMainWindow == null)) {
                DisplayContent displayContent2 = displayContent;
                this.mLastMainWindow = this.mCompatible.traversalWindows(displayContent2, getDispMetrics(displayContent), getRealMetrics(displayContent), getStatusBarHeight(), new WindowDummyListener(this, null));
            }
            if (this.mLastMainWindow != null) {
                IColorWindow client = IColorWindowImpl.asInterface(this.mLastMainWindow.getMainWindow().mClient);
                if (client != null) {
                    client.longshotNotifyConnected(isConnected);
                }
            }
        } catch (RemoteException e) {
            ColorLog.e(DBG, TAG, "notifyConnectedLocked ERROR : " + Log.getStackTraceString(e));
        } catch (Exception e2) {
            ColorLog.e(DBG, TAG, "notifyConnectedLocked ERROR : " + Log.getStackTraceString(e2));
        }
    }

    public int getSurfaceLayerLocked(DisplayContent displayContent, int type) {
        return this.mCompatible.getSurfaceLayerLocked(displayContent, type);
    }

    public void injectInputLocked(DisplayContent displayContent, InputEvent event, int mode) {
        try {
            DisplayContent displayContent2 = displayContent;
            ColorLongshotMainWindow w = this.mCompatible.traversalWindows(displayContent2, getDispMetrics(displayContent), getRealMetrics(displayContent), getStatusBarHeight(), new WindowDummyListener(this, null));
            if (w != null) {
                IColorWindow client = IColorWindowImpl.asInterface(w.getMainWindow().mClient);
                if (client != null) {
                    client.longshotInjectInput(event, mode);
                }
            }
        } catch (RemoteException e) {
            ColorLog.e(DBG, TAG, "injectInputLocked ERROR : " + Log.getStackTraceString(e));
        } catch (Exception e2) {
            ColorLog.e(DBG, TAG, "injectInputLocked ERROR : " + Log.getStackTraceString(e2));
        }
    }

    public void injectInputBeginLocked(DisplayContent displayContent) {
        try {
            this.mLastInjectWindow = null;
            DisplayContent displayContent2 = displayContent;
            this.mLastInjectWindow = this.mCompatible.traversalWindows(displayContent2, getDispMetrics(displayContent), getRealMetrics(displayContent), getStatusBarHeight(), new WindowDummyListener(this, null));
            if (this.mLastInjectWindow != null) {
                IColorWindow client = IColorWindowImpl.asInterface(this.mLastMainWindow.getMainWindow().mClient);
                if (client != null) {
                    client.longshotInjectInputBegin();
                }
            }
        } catch (RemoteException e) {
            ColorLog.e(DBG, TAG, "injectInputBeginLocked ERROR : " + Log.getStackTraceString(e));
        } catch (Exception e2) {
            ColorLog.e(DBG, TAG, "injectInputBeginLocked ERROR : " + Log.getStackTraceString(e2));
        }
    }

    public void injectInputEndLocked(DisplayContent displayContent) {
        try {
            if (this.mLastInjectWindow == null) {
                DisplayContent displayContent2 = displayContent;
                this.mLastInjectWindow = this.mCompatible.traversalWindows(displayContent2, getDispMetrics(displayContent), getRealMetrics(displayContent), getStatusBarHeight(), new WindowDummyListener(this, null));
            }
            if (this.mLastInjectWindow != null) {
                IColorWindow client = IColorWindowImpl.asInterface(this.mLastMainWindow.getMainWindow().mClient);
                if (client != null) {
                    client.longshotInjectInputEnd();
                }
            }
        } catch (RemoteException e) {
            ColorLog.e(DBG, TAG, "injectInputEndLocked ERROR : " + Log.getStackTraceString(e));
        } catch (Exception e2) {
            ColorLog.e(DBG, TAG, "injectInputEndLocked ERROR : " + Log.getStackTraceString(e2));
        }
    }

    private void dumpUnsupported(PrintWriter pw) {
        pw.flush();
        try {
            JSONObject jsonNode = new JSONObject();
            jsonNode.put(JSON_WINDOW_UNSUPPORTED, true);
            pw.println(jsonNode.toString());
        } catch (JSONException e) {
            pw.println("Failure while dumping the window for unsupported: " + e);
        }
    }

    private void dumpLongshot(FileDescriptor fd, PrintWriter pw, ColorLongshotMainWindow mainWin, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        IColorWindow client = IColorWindowImpl.asInterface(mainWin.getMainWindow().mClient);
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
        return this.mResources.getDimensionPixelSize(201654274);
    }

    private ColorLongshotMainWindow getTopWindowLocked(List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        DisplayContent displayContent = this.mService.getDefaultDisplayContentLocked();
        return this.mCompatible.traversalWindows(displayContent, getDispMetrics(displayContent), getRealMetrics(displayContent), getStatusBarHeight(), new WindowDumpListener(systemWindows, floatWindows));
    }
}
