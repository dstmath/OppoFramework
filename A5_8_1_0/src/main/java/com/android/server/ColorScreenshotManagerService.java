package com.android.server;

import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.IOppoWindowManager;
import android.view.IOppoWindowManagerImpl;
import com.color.screenshot.ColorLongshotDump;
import com.color.screenshot.ColorLongshotEvent;
import com.color.screenshot.ColorLongshotUtils;
import com.color.screenshot.IColorLongshot;
import com.color.screenshot.IColorLongshotCallback;
import com.color.screenshot.IColorScreenshot;
import com.color.screenshot.IColorScreenshotCallback;
import com.color.screenshot.IColorScreenshotManager.Stub;
import com.color.util.ColorLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.LinkedList;

public class ColorScreenshotManagerService extends Stub {
    private static final Comparable<ColorLongshotDump> COMPARE_LONGSHOT_COUNT = new CompareCount();
    private static final Comparable<ColorLongshotDump> COMPARE_LONGSHOT_SPEND = new CompareSpend();
    private static final ComponentName COMPONENT_LONGSHOT = new ComponentName("com.coloros.screenshot", "com.coloros.screenshot.service.LongshotService");
    private static final ComponentName COMPONENT_SCREENSHOT = new ComponentName("com.coloros.screenshot", "com.coloros.screenshot.service.ScreenshotService");
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final int MAX_DUMP_COUNT = 10;
    private static final String TAG = "LongshotDump";
    private final Context mContext;
    private final H mH = new H(this, null);
    private boolean mIsLongshotDisabled = true;
    private final LongshotConnection mLongshot = new LongshotConnection();
    private final LinkedList<ColorLongshotDump> mLongshotCountList = new LinkedList();
    private boolean mLongshotEnabled = true;
    private final LinkedList<ColorLongshotDump> mLongshotSpendList = new LinkedList();
    private final ScreenshotConnection mScreenshot = new ScreenshotConnection(this, null);
    private boolean mScreenshotEnabled = true;
    private final int mUserId;

    private interface Comparable<T> {
        int onCompare(T t, T t2);
    }

    private static class CompareCount implements Comparable<ColorLongshotDump> {
        /* synthetic */ CompareCount(CompareCount -this0) {
            this();
        }

        private CompareCount() {
        }

        public int onCompare(ColorLongshotDump obj1, ColorLongshotDump obj2) {
            return obj1.getDumpCount() - obj2.getDumpCount();
        }
    }

    private static class CompareSpend implements Comparable<ColorLongshotDump> {
        /* synthetic */ CompareSpend(CompareSpend -this0) {
            this();
        }

        private CompareSpend() {
        }

        public int onCompare(ColorLongshotDump obj1, ColorLongshotDump obj2) {
            return (int) (obj1.getTotalSpend() - obj2.getTotalSpend());
        }
    }

    private class H extends Handler {
        public static final int REPORT_LONGSHOT_DUMP_RESULT = 2;

        /* synthetic */ H(ColorScreenshotManagerService this$0, H -this1) {
            this();
        }

        private H() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    ColorLongshotDump result = msg.obj;
                    ColorScreenshotManagerService.this.addLongshotDump(ColorScreenshotManagerService.this.mLongshotSpendList, result, ColorScreenshotManagerService.COMPARE_LONGSHOT_SPEND);
                    ColorScreenshotManagerService.this.addLongshotDump(ColorScreenshotManagerService.this.mLongshotCountList, result, ColorScreenshotManagerService.COMPARE_LONGSHOT_COUNT);
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    }

    private class LongshotConnection extends IColorLongshotCallback.Stub implements ServiceConnection {
        private IColorLongshot mService = null;
        private final IOppoWindowManager mWindowManager = new IOppoWindowManagerImpl();

        public void stop() {
            ColorScreenshotManagerService.this.mContext.unbindService(this);
            onServiceDisconnectedBy("stop");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            ColorLog.d(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, "onServiceConnected : " + name);
            this.mService = IColorLongshot.Stub.asInterface(service);
            if (start()) {
                try {
                    this.mWindowManager.longshotNotifyConnected(true);
                } catch (RemoteException e) {
                    ColorLog.e(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, e.toString());
                } catch (Exception e2) {
                    ColorLog.e(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, Log.getStackTraceString(e2));
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            onServiceDisconnectedBy("onServiceDisconnected");
        }

        public boolean start() {
            if (this.mService == null) {
                return false;
            }
            try {
                this.mService.start(this);
                return true;
            } catch (RemoteException e) {
                ColorLog.e(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, e.toString());
                return false;
            } catch (Exception e2) {
                ColorLog.e(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, Log.getStackTraceString(e2));
                return false;
            }
        }

        private void onServiceDisconnectedBy(String msg) {
            ColorLog.d(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, msg);
            this.mService = null;
            try {
                this.mWindowManager.longshotNotifyConnected(false);
            } catch (RemoteException e) {
                ColorLog.e(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, e.toString());
            } catch (Exception e2) {
                ColorLog.e(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, Log.getStackTraceString(e2));
            }
        }
    }

    private class ScreenshotConnection extends IColorScreenshotCallback.Stub implements ServiceConnection {
        private IColorScreenshot mService;

        /* synthetic */ ScreenshotConnection(ColorScreenshotManagerService this$0, ScreenshotConnection -this1) {
            this();
        }

        private ScreenshotConnection() {
            this.mService = null;
        }

        public void stop() {
            ColorScreenshotManagerService.this.mContext.unbindService(this);
            onServiceDisconnectedBy("stop");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            ColorLog.d(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, "onServiceConnected : " + name);
            this.mService = IColorScreenshot.Stub.asInterface(service);
            start();
        }

        public void onServiceDisconnected(ComponentName name) {
            onServiceDisconnectedBy("onServiceDisconnected");
        }

        public boolean start() {
            if (this.mService == null) {
                return false;
            }
            try {
                this.mService.start(this);
                return true;
            } catch (RemoteException e) {
                ColorLog.e(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, e.toString());
                return false;
            } catch (Exception e2) {
                ColorLog.e(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, Log.getStackTraceString(e2));
                return false;
            }
        }

        private void onServiceDisconnectedBy(String msg) {
            ColorLog.d(ColorScreenshotManagerService.DBG, ColorScreenshotManagerService.TAG, msg);
            this.mService = null;
        }
    }

    public ColorScreenshotManagerService(Context context) {
        this.mContext = context;
        this.mUserId = getUserId();
        this.mIsLongshotDisabled = ColorLongshotUtils.isDisabled(this.mContext);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump ColorScreenshotManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        int opti = 0;
        while (opti < args.length) {
            String opt = args[opti];
            if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                break;
            }
            opti++;
            if ("-h".equals(opt)) {
                pw.println("ColorOS screenshot manager dump options:");
                pw.println("  [-h] [cmd] ...");
                pw.println("  cmd may be one of:");
                pw.println("    l[ongshot]: longshot information");
                return;
            }
            pw.println("Unknown argument: " + opt + "; use -h for help");
        }
        if (opti < args.length) {
            String cmd = args[opti];
            if ("longshot".equals(cmd) || "l".equals(cmd)) {
                dumpLongshot(pw);
            } else {
                pw.println("Bad ColorOS screenshot command : " + cmd);
                pw.println("Use -h for help.");
            }
        }
    }

    public void takeScreenshot(Bundle extras) {
        if (isScreenshotEnabled()) {
            if (this.mScreenshot.mService == null) {
                Intent intent = new Intent();
                intent.setComponent(COMPONENT_SCREENSHOT);
                intent.putExtras(extras);
                ColorLog.i(DBG, TAG, "takeScreenshot : bindService " + intent.getComponent() + "=" + bindService(intent, this.mScreenshot, 1));
            } else {
                ColorLog.i(DBG, TAG, "takeScreenshot : startService=" + this.mScreenshot.start());
            }
            return;
        }
        ColorLog.i(DBG, TAG, "takeScreenshot : not enabled");
    }

    public boolean isScreenshotMode() {
        return this.mScreenshot.mService != null;
    }

    public boolean isScreenshotEdit() {
        boolean result = false;
        IColorScreenshot service = this.mScreenshot.mService;
        if (service == null) {
            return result;
        }
        try {
            return service.isEdit();
        } catch (RemoteException e) {
            ColorLog.e(DBG, TAG, "isScreenshotEdit ERROR : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, TAG, Log.getStackTraceString(e2));
            return result;
        }
    }

    public void takeLongshot(boolean statusBarVisible, boolean navBarVisible) {
        if (isLongshotDisabled()) {
            ColorLog.i(DBG, TAG, "takeLongshot : feature disabled");
        } else if (isLongshotEnabled()) {
            Intent intent = new Intent();
            intent.setComponent(COMPONENT_LONGSHOT);
            intent.putExtra("statusbar_visible", statusBarVisible);
            intent.putExtra("navigationbar_visible", navBarVisible);
            ColorLog.i(DBG, TAG, "takeLongshot : bindService " + intent.getComponent() + "=" + bindService(intent, this.mLongshot, 1));
        } else {
            ColorLog.i(DBG, TAG, "takeLongshot : not enabled");
        }
    }

    public void stopLongshot() {
        IColorLongshot service = this.mLongshot.mService;
        if (service != null) {
            try {
                ColorLog.i(DBG, TAG, "stopLongshot");
                service.stop();
            } catch (RemoteException e) {
                ColorLog.e(DBG, TAG, "stopLongshot ERROR : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, TAG, Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isLongshotMode() {
        return this.mLongshot.mService != null;
    }

    public boolean isLongshotDisabled() {
        return this.mIsLongshotDisabled;
    }

    public void reportLongshotDumpResult(ColorLongshotDump result) {
        sendMessage(2, result, 0, 0);
    }

    public void setScreenshotEnabled(boolean enabled) {
        ColorLog.i(DBG, TAG, "setScreenshotEnabled : " + enabled);
        this.mScreenshotEnabled = enabled;
    }

    public boolean isScreenshotEnabled() {
        return this.mScreenshotEnabled;
    }

    public void setLongshotEnabled(boolean enabled) {
        ColorLog.i(DBG, TAG, "setLongshotEnabled : " + enabled);
        this.mLongshotEnabled = enabled;
    }

    public boolean isLongshotEnabled() {
        return this.mLongshotEnabled;
    }

    public void notifyOverScroll(ColorLongshotEvent event) {
        IColorLongshot service = this.mLongshot.mService;
        if (service != null) {
            try {
                ColorLog.i(DBG, TAG, "notifyOverScroll : " + event);
                service.notifyOverScroll(event);
            } catch (RemoteException e) {
                ColorLog.e(DBG, TAG, "notifyOverScroll ERROR : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, TAG, Log.getStackTraceString(e2));
            }
        }
    }

    private int getUserId() {
        int userId = 0;
        try {
            return ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            ColorLog.w(DBG, TAG, "Couldn't get current user ID; guessing it's 0", e);
            return userId;
        }
    }

    private boolean bindService(Intent service, ServiceConnection conn, int flags) {
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mUserId));
        }
        ColorLog.e(DBG, TAG, "bindService failed: service=" + service + ", conn=" + conn);
        return false;
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        this.mH.sendMessage(msg);
    }

    private boolean tryInsertLocked(LinkedList<ColorLongshotDump> list, ColorLongshotDump result, Comparable<ColorLongshotDump> callback) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (callback.onCompare(result, (ColorLongshotDump) list.get(i)) > 0) {
                list.add(i, result);
                return true;
            }
        }
        return false;
    }

    private void addLongshotDump(LinkedList<ColorLongshotDump> list, ColorLongshotDump result, Comparable<ColorLongshotDump> callback) {
        synchronized (list) {
            if (!tryInsertLocked(list, result, callback)) {
                list.addLast(result);
            }
            if (list.size() > 10) {
                list.removeLast();
            }
        }
    }

    private boolean dumpLongshotList(PrintWriter pw, LinkedList<ColorLongshotDump> list, String tag) {
        synchronized (list) {
            if (list.isEmpty()) {
                return false;
            }
            for (ColorLongshotDump dump : list) {
                pw.println(tag + ":" + dump);
            }
            return true;
        }
    }

    private void dumpLongshot(PrintWriter pw) {
        if (!(dumpLongshotList(pw, this.mLongshotSpendList, "spend") | dumpLongshotList(pw, this.mLongshotCountList, "count"))) {
            pw.println("null");
        }
    }
}
