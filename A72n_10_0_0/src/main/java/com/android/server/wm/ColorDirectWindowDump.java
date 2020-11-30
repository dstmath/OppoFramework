package com.android.server.wm;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.view.IColorWindow;
import android.view.IColorWindowImpl;
import android.view.IWindow;
import android.view.WindowManager;
import com.color.direct.ColorDirectFindCmd;
import com.color.direct.ColorDirectUtils;
import com.color.util.ColorLog;

public class ColorDirectWindowDump {
    private static final boolean DBG = ColorDirectUtils.DBG;
    private static final String TAG = "DirectService";
    private final ColorDirectWindowCompatible mCompatible;
    private final WindowManagerService mService;

    public ColorDirectWindowDump(Context context, WindowManagerService service) {
        this.mCompatible = new ColorDirectWindowCompatible(context);
        this.mService = service;
    }

    public void findCmdLocked(DisplayContent displayContent, ColorDirectFindCmd findCmd) {
        if (findCmd != null) {
            try {
                ColorDirectMainWindow mainWindow = this.mCompatible.traversalWindows(displayContent, this.mCompatible.getDispMetrics(displayContent), this.mCompatible.getRealMetrics(displayContent), this.mCompatible.getStatusBarHeight(), new WindowPrintListener());
                if (mainWindow == null) {
                    ColorDirectUtils.onFindFailed(findCmd.getCallback(), "no_mainwin");
                    return;
                }
                IColorWindow client = IColorWindowImpl.asInterface(mainWindow.getMainWindow().mClient);
                if (client == null) {
                    ColorDirectUtils.onFindFailed(findCmd.getCallback(), "no_viewroot");
                } else {
                    client.directFindCmd(findCmd);
                }
            } catch (RemoteException e) {
                boolean z = DBG;
                ColorLog.e(z, TAG, "findCmdLocked ERROR : " + Log.getStackTraceString(e));
            } catch (Exception e2) {
                boolean z2 = DBG;
                ColorLog.e(z2, TAG, "findCmdLocked ERROR : " + Log.getStackTraceString(e2));
            }
        }
    }

    private class WindowPrintListener implements ColorWindowTraversalListener {
        private WindowPrintListener() {
        }

        public void printDetect(String msg, CharSequence windowName) {
        }

        public void printWindow(String msg, CharSequence windowName) {
            boolean z = ColorDirectWindowDump.DBG;
            ColorLog.d(z, ColorDirectWindowDump.TAG, " findWindow [" + msg + "] " + ((Object) windowName));
        }

        public void collectSystemWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, WindowManager.LayoutParams winAttrs) {
        }

        public void collectFloatWindows(IWindow service, CharSequence windowTitle, int surfaceLayer, WindowManager.LayoutParams winAttrs) {
        }

        public boolean hasSystemDocorView(IWindow service) {
            return ColorDirectWindowDump.DBG;
        }
    }
}
