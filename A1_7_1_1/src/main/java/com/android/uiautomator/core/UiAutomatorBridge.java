package com.android.uiautomator.core;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.app.UiAutomation.AccessibilityEventFilter;
import android.app.UiAutomation.OnAccessibilityEventListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.Display;
import android.view.InputEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class UiAutomatorBridge {
    private static final String LOG_TAG = null;
    private static final long QUIET_TIME_TO_BE_CONSIDERD_IDLE_STATE = 500;
    private static final long TOTAL_TIME_TO_WAIT_FOR_IDLE_STATE = 10000;
    private final InteractionController mInteractionController;
    private final QueryController mQueryController;
    private final UiAutomation mUiAutomation;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.uiautomator.core.UiAutomatorBridge.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.uiautomator.core.UiAutomatorBridge.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.uiautomator.core.UiAutomatorBridge.<clinit>():void");
    }

    public abstract Display getDefaultDisplay();

    public abstract int getRotation();

    public abstract long getSystemLongPressTime();

    public abstract boolean isScreenOn();

    UiAutomatorBridge(UiAutomation uiAutomation) {
        this.mUiAutomation = uiAutomation;
        this.mInteractionController = new InteractionController(this);
        this.mQueryController = new QueryController(this);
    }

    InteractionController getInteractionController() {
        return this.mInteractionController;
    }

    QueryController getQueryController() {
        return this.mQueryController;
    }

    public void setOnAccessibilityEventListener(OnAccessibilityEventListener listener) {
        this.mUiAutomation.setOnAccessibilityEventListener(listener);
    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        return this.mUiAutomation.getRootInActiveWindow();
    }

    public boolean injectInputEvent(InputEvent event, boolean sync) {
        return this.mUiAutomation.injectInputEvent(event, sync);
    }

    public boolean setRotation(int rotation) {
        return this.mUiAutomation.setRotation(rotation);
    }

    public void setCompressedLayoutHierarchy(boolean compressed) {
        AccessibilityServiceInfo info = this.mUiAutomation.getServiceInfo();
        if (compressed) {
            info.flags &= -3;
        } else {
            info.flags |= 2;
        }
        this.mUiAutomation.setServiceInfo(info);
    }

    public void waitForIdle() {
        waitForIdle(TOTAL_TIME_TO_WAIT_FOR_IDLE_STATE);
    }

    public void waitForIdle(long timeout) {
        try {
            this.mUiAutomation.waitForIdle(QUIET_TIME_TO_BE_CONSIDERD_IDLE_STATE, timeout);
        } catch (TimeoutException te) {
            Log.w(LOG_TAG, "Could not detect idle state.", te);
        }
    }

    public AccessibilityEvent executeCommandAndWaitForAccessibilityEvent(Runnable command, AccessibilityEventFilter filter, long timeoutMillis) throws TimeoutException {
        return this.mUiAutomation.executeAndWaitForEvent(command, filter, timeoutMillis);
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0036 A:{SYNTHETIC, Splitter: B:19:0x0036} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0042 A:{SYNTHETIC, Splitter: B:26:0x0042} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean takeScreenshot(File storePath, int quality) {
        IOException ioe;
        Throwable th;
        Bitmap screenshot = this.mUiAutomation.takeScreenshot();
        if (screenshot == null) {
            return false;
        }
        BufferedOutputStream bos = null;
        try {
            BufferedOutputStream bos2 = new BufferedOutputStream(new FileOutputStream(storePath));
            if (bos2 != null) {
                try {
                    screenshot.compress(CompressFormat.PNG, quality, bos2);
                    bos2.flush();
                } catch (IOException e) {
                    ioe = e;
                    bos = bos2;
                    try {
                        Log.e(LOG_TAG, "failed to save screen shot to file", ioe);
                        if (bos != null) {
                        }
                        screenshot.recycle();
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bos != null) {
                            try {
                                bos.close();
                            } catch (IOException e2) {
                            }
                        }
                        screenshot.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bos = bos2;
                    if (bos != null) {
                    }
                    screenshot.recycle();
                    throw th;
                }
            }
            if (bos2 != null) {
                try {
                    bos2.close();
                } catch (IOException e3) {
                }
            }
            screenshot.recycle();
            return true;
        } catch (IOException e4) {
            ioe = e4;
            Log.e(LOG_TAG, "failed to save screen shot to file", ioe);
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e5) {
                }
            }
            screenshot.recycle();
            return false;
        }
    }

    public boolean performGlobalAction(int action) {
        return this.mUiAutomation.performGlobalAction(action);
    }
}
