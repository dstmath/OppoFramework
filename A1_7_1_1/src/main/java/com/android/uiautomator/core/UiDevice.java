package com.android.uiautomator.core;

import android.app.UiAutomation.AccessibilityEventFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@Deprecated
public class UiDevice {
    private static final long KEY_PRESS_EVENT_TIMEOUT = 1000;
    private static final String LOG_TAG = null;
    private static UiDevice sDevice;
    private boolean mInWatcherContext;
    private UiAutomatorBridge mUiAutomationBridge;
    private final HashMap<String, UiWatcher> mWatchers;
    private final List<String> mWatchersTriggers;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.uiautomator.core.UiDevice.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.uiautomator.core.UiDevice.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.uiautomator.core.UiDevice.<clinit>():void");
    }

    private UiDevice() {
        this.mWatchers = new HashMap();
        this.mWatchersTriggers = new ArrayList();
        this.mInWatcherContext = false;
    }

    public void initialize(UiAutomatorBridge uiAutomatorBridge) {
        this.mUiAutomationBridge = uiAutomatorBridge;
    }

    boolean isInWatcherContext() {
        return this.mInWatcherContext;
    }

    UiAutomatorBridge getAutomatorBridge() {
        if (this.mUiAutomationBridge != null) {
            return this.mUiAutomationBridge;
        }
        throw new RuntimeException("UiDevice not initialized");
    }

    public void setCompressedLayoutHeirarchy(boolean compressed) {
        getAutomatorBridge().setCompressedLayoutHierarchy(compressed);
    }

    public static UiDevice getInstance() {
        if (sDevice == null) {
            sDevice = new UiDevice();
        }
        return sDevice;
    }

    public Point getDisplaySizeDp() {
        Tracer.trace(new Object[0]);
        Display display = getAutomatorBridge().getDefaultDisplay();
        Point p = new Point();
        display.getRealSize(p);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        float dpy = ((float) p.y) / metrics.density;
        p.x = Math.round(((float) p.x) / metrics.density);
        p.y = Math.round(dpy);
        return p;
    }

    public String getProductName() {
        Tracer.trace(new Object[0]);
        return Build.PRODUCT;
    }

    public String getLastTraversedText() {
        Tracer.trace(new Object[0]);
        return getAutomatorBridge().getQueryController().getLastTraversedText();
    }

    public void clearLastTraversedText() {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getQueryController().clearLastTraversedText();
    }

    public boolean pressMenu() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKeyAndWaitForEvent(82, 0, 2048, KEY_PRESS_EVENT_TIMEOUT);
    }

    public boolean pressBack() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKeyAndWaitForEvent(4, 0, 2048, KEY_PRESS_EVENT_TIMEOUT);
    }

    public boolean pressHome() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKeyAndWaitForEvent(3, 0, 2048, KEY_PRESS_EVENT_TIMEOUT);
    }

    public boolean pressSearch() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(84);
    }

    public boolean pressDPadCenter() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(23);
    }

    public boolean pressDPadDown() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(20);
    }

    public boolean pressDPadUp() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(19);
    }

    public boolean pressDPadLeft() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(21);
    }

    public boolean pressDPadRight() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(22);
    }

    public boolean pressDelete() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(67);
    }

    public boolean pressEnter() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(66);
    }

    public boolean pressKeyCode(int keyCode) {
        Object[] objArr = new Object[1];
        objArr[0] = Integer.valueOf(keyCode);
        Tracer.trace(objArr);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKey(keyCode, 0);
    }

    public boolean pressKeyCode(int keyCode, int metaState) {
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(keyCode);
        objArr[1] = Integer.valueOf(metaState);
        Tracer.trace(objArr);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKey(keyCode, metaState);
    }

    public boolean pressRecentApps() throws RemoteException {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().toggleRecentApps();
    }

    public boolean openNotification() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().openNotification();
    }

    public boolean openQuickSettings() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().openQuickSettings();
    }

    public int getDisplayWidth() {
        Tracer.trace(new Object[0]);
        Display display = getAutomatorBridge().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        return p.x;
    }

    public int getDisplayHeight() {
        Tracer.trace(new Object[0]);
        Display display = getAutomatorBridge().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        return p.y;
    }

    public boolean click(int x, int y) {
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(x);
        objArr[1] = Integer.valueOf(y);
        Tracer.trace(objArr);
        if (x >= getDisplayWidth() || y >= getDisplayHeight()) {
            return false;
        }
        return getAutomatorBridge().getInteractionController().clickNoSync(x, y);
    }

    public boolean swipe(int startX, int startY, int endX, int endY, int steps) {
        Object[] objArr = new Object[5];
        objArr[0] = Integer.valueOf(startX);
        objArr[1] = Integer.valueOf(startY);
        objArr[2] = Integer.valueOf(endX);
        objArr[3] = Integer.valueOf(endY);
        objArr[4] = Integer.valueOf(steps);
        Tracer.trace(objArr);
        return getAutomatorBridge().getInteractionController().swipe(startX, startY, endX, endY, steps);
    }

    public boolean drag(int startX, int startY, int endX, int endY, int steps) {
        Object[] objArr = new Object[5];
        objArr[0] = Integer.valueOf(startX);
        objArr[1] = Integer.valueOf(startY);
        objArr[2] = Integer.valueOf(endX);
        objArr[3] = Integer.valueOf(endY);
        objArr[4] = Integer.valueOf(steps);
        Tracer.trace(objArr);
        return getAutomatorBridge().getInteractionController().swipe(startX, startY, endX, endY, steps, true);
    }

    public boolean swipe(Point[] segments, int segmentSteps) {
        Object[] objArr = new Object[2];
        objArr[0] = segments;
        objArr[1] = Integer.valueOf(segmentSteps);
        Tracer.trace(objArr);
        return getAutomatorBridge().getInteractionController().swipe(segments, segmentSteps);
    }

    public void waitForIdle() {
        Tracer.trace(new Object[0]);
        waitForIdle(Configurator.getInstance().getWaitForIdleTimeout());
    }

    public void waitForIdle(long timeout) {
        Object[] objArr = new Object[1];
        objArr[0] = Long.valueOf(timeout);
        Tracer.trace(objArr);
        getAutomatorBridge().waitForIdle(timeout);
    }

    @Deprecated
    public String getCurrentActivityName() {
        Tracer.trace(new Object[0]);
        return getAutomatorBridge().getQueryController().getCurrentActivityName();
    }

    public String getCurrentPackageName() {
        Tracer.trace(new Object[0]);
        return getAutomatorBridge().getQueryController().getCurrentPackageName();
    }

    public void registerWatcher(String name, UiWatcher watcher) {
        Object[] objArr = new Object[2];
        objArr[0] = name;
        objArr[1] = watcher;
        Tracer.trace(objArr);
        if (this.mInWatcherContext) {
            throw new IllegalStateException("Cannot register new watcher from within another");
        }
        this.mWatchers.put(name, watcher);
    }

    public void removeWatcher(String name) {
        Object[] objArr = new Object[1];
        objArr[0] = name;
        Tracer.trace(objArr);
        if (this.mInWatcherContext) {
            throw new IllegalStateException("Cannot remove a watcher from within another");
        }
        this.mWatchers.remove(name);
    }

    public void runWatchers() {
        Tracer.trace(new Object[0]);
        if (!this.mInWatcherContext) {
            for (String watcherName : this.mWatchers.keySet()) {
                UiWatcher watcher = (UiWatcher) this.mWatchers.get(watcherName);
                if (watcher != null) {
                    try {
                        this.mInWatcherContext = true;
                        if (watcher.checkForCondition()) {
                            setWatcherTriggered(watcherName);
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exceuting watcher: " + watcherName, e);
                    } catch (Throwable th) {
                        this.mInWatcherContext = false;
                    }
                    this.mInWatcherContext = false;
                }
            }
        }
    }

    public void resetWatcherTriggers() {
        Tracer.trace(new Object[0]);
        this.mWatchersTriggers.clear();
    }

    public boolean hasWatcherTriggered(String watcherName) {
        Object[] objArr = new Object[1];
        objArr[0] = watcherName;
        Tracer.trace(objArr);
        return this.mWatchersTriggers.contains(watcherName);
    }

    public boolean hasAnyWatcherTriggered() {
        Tracer.trace(new Object[0]);
        if (this.mWatchersTriggers.size() > 0) {
            return true;
        }
        return false;
    }

    private void setWatcherTriggered(String watcherName) {
        Object[] objArr = new Object[1];
        objArr[0] = watcherName;
        Tracer.trace(objArr);
        if (!hasWatcherTriggered(watcherName)) {
            this.mWatchersTriggers.add(watcherName);
        }
    }

    public boolean isNaturalOrientation() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        int ret = getAutomatorBridge().getRotation();
        if (ret == 0 || ret == 2) {
            return true;
        }
        return false;
    }

    public int getDisplayRotation() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getRotation();
    }

    public void freezeRotation() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().freezeRotation();
    }

    public void unfreezeRotation() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().unfreezeRotation();
    }

    public void setOrientationLeft() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().setRotationLeft();
        waitForIdle();
    }

    public void setOrientationRight() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().setRotationRight();
        waitForIdle();
    }

    public void setOrientationNatural() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().setRotationNatural();
        waitForIdle();
    }

    public void wakeUp() throws RemoteException {
        Tracer.trace(new Object[0]);
        if (getAutomatorBridge().getInteractionController().wakeDevice()) {
            SystemClock.sleep(500);
        }
    }

    public boolean isScreenOn() throws RemoteException {
        Tracer.trace(new Object[0]);
        return getAutomatorBridge().getInteractionController().isScreenOn();
    }

    public void sleep() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().sleepDevice();
    }

    public void dumpWindowHierarchy(String fileName) {
        Object[] objArr = new Object[1];
        objArr[0] = fileName;
        Tracer.trace(objArr);
        AccessibilityNodeInfo root = getAutomatorBridge().getQueryController().getAccessibilityRootNode();
        if (root != null) {
            Display display = getAutomatorBridge().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            AccessibilityNodeInfoDumper.dumpWindowToFile(root, new File(new File(Environment.getDataDirectory(), "local/tmp"), fileName), display.getRotation(), size.x, size.y);
        }
    }

    public boolean waitForWindowUpdate(final String packageName, long timeout) {
        Object[] objArr = new Object[2];
        objArr[0] = packageName;
        objArr[1] = Long.valueOf(timeout);
        Tracer.trace(objArr);
        if (packageName != null && !packageName.equals(getCurrentPackageName())) {
            return false;
        }
        try {
            getAutomatorBridge().executeCommandAndWaitForAccessibilityEvent(new Runnable() {
                public void run() {
                }
            }, new AccessibilityEventFilter() {
                public boolean accept(AccessibilityEvent t) {
                    if (t.getEventType() != 2048) {
                        return false;
                    }
                    return packageName != null ? packageName.equals(t.getPackageName()) : true;
                }
            }, timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        } catch (Exception e2) {
            Log.e(LOG_TAG, "waitForWindowUpdate: general exception from bridge", e2);
            return false;
        }
    }

    public boolean takeScreenshot(File storePath) {
        Object[] objArr = new Object[1];
        objArr[0] = storePath;
        Tracer.trace(objArr);
        return takeScreenshot(storePath, 1.0f, 90);
    }

    public boolean takeScreenshot(File storePath, float scale, int quality) {
        Object[] objArr = new Object[3];
        objArr[0] = storePath;
        objArr[1] = Float.valueOf(scale);
        objArr[2] = Integer.valueOf(quality);
        Tracer.trace(objArr);
        return getAutomatorBridge().takeScreenshot(storePath, quality);
    }
}
