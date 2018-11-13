package com.android.uiautomator.core;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IActivityManager.ContentProviderHolder;
import android.app.UiAutomation;
import android.content.IContentProvider;
import android.database.Cursor;
import android.hardware.display.DisplayManagerGlobal;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager.Stub;

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
public class ShellUiAutomatorBridge extends UiAutomatorBridge {
    private static final String LOG_TAG = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.uiautomator.core.ShellUiAutomatorBridge.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.uiautomator.core.ShellUiAutomatorBridge.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.uiautomator.core.ShellUiAutomatorBridge.<clinit>():void");
    }

    public ShellUiAutomatorBridge(UiAutomation uiAutomation) {
        super(uiAutomation);
    }

    public Display getDefaultDisplay() {
        return DisplayManagerGlobal.getInstance().getRealDisplay(0);
    }

    public long getSystemLongPressTime() {
        long longPressTimeout = 0;
        Cursor cursor = null;
        IActivityManager activityManager;
        String providerName;
        IBinder token;
        try {
            activityManager = ActivityManagerNative.getDefault();
            providerName = Secure.CONTENT_URI.getAuthority();
            token = new Binder();
            ContentProviderHolder holder = activityManager.getContentProviderExternal(providerName, 0, token);
            if (holder == null) {
                throw new IllegalStateException("Could not find provider: " + providerName);
            }
            IContentProvider provider = holder.provider;
            Uri uri = Secure.CONTENT_URI;
            String[] strArr = new String[1];
            strArr[0] = "value";
            String[] strArr2 = new String[1];
            strArr2[0] = "long_press_timeout";
            cursor = provider.query(null, uri, strArr, "name=?", strArr2, null, null);
            if (cursor.moveToFirst()) {
                longPressTimeout = (long) cursor.getInt(0);
            }
            if (cursor != null) {
                cursor.close();
            }
            if (provider != null) {
                activityManager.removeContentProviderExternal(providerName, token);
            }
            return longPressTimeout;
        } catch (RemoteException e) {
            String message = "Error reading long press timeout setting.";
            Log.e(LOG_TAG, message, e);
            throw new RuntimeException(message, e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            if (null != null) {
                activityManager.removeContentProviderExternal(providerName, token);
            }
        }
    }

    public int getRotation() {
        try {
            return Stub.asInterface(ServiceManager.getService("window")).getRotation();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error getting screen rotation", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isScreenOn() {
        try {
            return IPowerManager.Stub.asInterface(ServiceManager.getService("power")).isInteractive();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error getting screen status", e);
            throw new RuntimeException(e);
        }
    }
}
