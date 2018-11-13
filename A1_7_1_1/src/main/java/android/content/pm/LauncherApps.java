package android.content.pm;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IOnAppsChangedListener.Stub;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public class LauncherApps {
    static final boolean DEBUG = false;
    static final String TAG = "LauncherApps";
    private Stub mAppsChangedListener;
    private List<CallbackMessageHandler> mCallbacks;
    private Context mContext;
    private PackageManager mPm;
    private ILauncherApps mService;

    public static abstract class Callback {
        public abstract void onPackageAdded(String str, UserHandle userHandle);

        public abstract void onPackageChanged(String str, UserHandle userHandle);

        public abstract void onPackageRemoved(String str, UserHandle userHandle);

        public abstract void onPackagesAvailable(String[] strArr, UserHandle userHandle, boolean z);

        public abstract void onPackagesUnavailable(String[] strArr, UserHandle userHandle, boolean z);

        public void onPackagesSuspended(String[] packageNames, UserHandle user) {
        }

        public void onPackagesUnsuspended(String[] packageNames, UserHandle user) {
        }

        public void onShortcutsChanged(String packageName, List<ShortcutInfo> list, UserHandle user) {
        }
    }

    private static class CallbackMessageHandler extends Handler {
        private static final int MSG_ADDED = 1;
        private static final int MSG_AVAILABLE = 4;
        private static final int MSG_CHANGED = 3;
        private static final int MSG_REMOVED = 2;
        private static final int MSG_SHORTCUT_CHANGED = 8;
        private static final int MSG_SUSPENDED = 6;
        private static final int MSG_UNAVAILABLE = 5;
        private static final int MSG_UNSUSPENDED = 7;
        private Callback mCallback;

        private static class CallbackInfo {
            String packageName;
            String[] packageNames;
            boolean replacing;
            List<ShortcutInfo> shortcuts;
            UserHandle user;

            /* synthetic */ CallbackInfo(CallbackInfo callbackInfo) {
                this();
            }

            private CallbackInfo() {
            }
        }

        public CallbackMessageHandler(Looper looper, Callback callback) {
            super(looper, null, true);
            this.mCallback = callback;
        }

        public void handleMessage(Message msg) {
            if (this.mCallback != null && (msg.obj instanceof CallbackInfo)) {
                CallbackInfo info = msg.obj;
                switch (msg.what) {
                    case 1:
                        this.mCallback.onPackageAdded(info.packageName, info.user);
                        break;
                    case 2:
                        this.mCallback.onPackageRemoved(info.packageName, info.user);
                        break;
                    case 3:
                        this.mCallback.onPackageChanged(info.packageName, info.user);
                        break;
                    case 4:
                        this.mCallback.onPackagesAvailable(info.packageNames, info.user, info.replacing);
                        break;
                    case 5:
                        this.mCallback.onPackagesUnavailable(info.packageNames, info.user, info.replacing);
                        break;
                    case 6:
                        this.mCallback.onPackagesSuspended(info.packageNames, info.user);
                        break;
                    case 7:
                        this.mCallback.onPackagesUnsuspended(info.packageNames, info.user);
                        break;
                    case 8:
                        this.mCallback.onShortcutsChanged(info.packageName, info.shortcuts, info.user);
                        break;
                }
            }
        }

        public void postOnPackageAdded(String packageName, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            obtainMessage(1, info).sendToTarget();
        }

        public void postOnPackageRemoved(String packageName, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            obtainMessage(2, info).sendToTarget();
        }

        public void postOnPackageChanged(String packageName, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            obtainMessage(3, info).sendToTarget();
        }

        public void postOnPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.replacing = replacing;
            info.user = user;
            obtainMessage(4, info).sendToTarget();
        }

        public void postOnPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.replacing = replacing;
            info.user = user;
            obtainMessage(5, info).sendToTarget();
        }

        public void postOnPackagesSuspended(String[] packageNames, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.user = user;
            obtainMessage(6, info).sendToTarget();
        }

        public void postOnPackagesUnsuspended(String[] packageNames, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.user = user;
            obtainMessage(7, info).sendToTarget();
        }

        public void postOnShortcutChanged(String packageName, UserHandle user, List<ShortcutInfo> shortcuts) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            info.shortcuts = shortcuts;
            obtainMessage(8, info).sendToTarget();
        }
    }

    public static class ShortcutQuery {
        @Deprecated
        public static final int FLAG_GET_ALL_KINDS = 11;
        @Deprecated
        public static final int FLAG_GET_DYNAMIC = 1;
        public static final int FLAG_GET_KEY_FIELDS_ONLY = 4;
        @Deprecated
        public static final int FLAG_GET_MANIFEST = 8;
        @Deprecated
        public static final int FLAG_GET_PINNED = 2;
        public static final int FLAG_MATCH_ALL_KINDS = 11;
        public static final int FLAG_MATCH_DYNAMIC = 1;
        public static final int FLAG_MATCH_MANIFEST = 8;
        public static final int FLAG_MATCH_PINNED = 2;
        ComponentName mActivity;
        long mChangedSince;
        String mPackage;
        int mQueryFlags;
        List<String> mShortcutIds;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.pm.LauncherApps.ShortcutQuery.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public ShortcutQuery() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.pm.LauncherApps.ShortcutQuery.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.LauncherApps.ShortcutQuery.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.content.pm.LauncherApps.ShortcutQuery.setActivity(android.content.ComponentName):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.content.pm.LauncherApps.ShortcutQuery setActivity(android.content.ComponentName r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.content.pm.LauncherApps.ShortcutQuery.setActivity(android.content.ComponentName):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.LauncherApps.ShortcutQuery.setActivity(android.content.ComponentName):android.content.pm.LauncherApps$ShortcutQuery");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.content.pm.LauncherApps.ShortcutQuery.setChangedSince(long):android.content.pm.LauncherApps$ShortcutQuery, dex:  in method: android.content.pm.LauncherApps.ShortcutQuery.setChangedSince(long):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.content.pm.LauncherApps.ShortcutQuery.setChangedSince(long):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public android.content.pm.LauncherApps.ShortcutQuery setChangedSince(long r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.content.pm.LauncherApps.ShortcutQuery.setChangedSince(long):android.content.pm.LauncherApps$ShortcutQuery, dex:  in method: android.content.pm.LauncherApps.ShortcutQuery.setChangedSince(long):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.LauncherApps.ShortcutQuery.setChangedSince(long):android.content.pm.LauncherApps$ShortcutQuery");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.content.pm.LauncherApps.ShortcutQuery.setPackage(java.lang.String):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.content.pm.LauncherApps.ShortcutQuery setPackage(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.content.pm.LauncherApps.ShortcutQuery.setPackage(java.lang.String):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.LauncherApps.ShortcutQuery.setPackage(java.lang.String):android.content.pm.LauncherApps$ShortcutQuery");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.content.pm.LauncherApps.ShortcutQuery.setQueryFlags(int):android.content.pm.LauncherApps$ShortcutQuery, dex:  in method: android.content.pm.LauncherApps.ShortcutQuery.setQueryFlags(int):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.content.pm.LauncherApps.ShortcutQuery.setQueryFlags(int):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public android.content.pm.LauncherApps.ShortcutQuery setQueryFlags(int r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.content.pm.LauncherApps.ShortcutQuery.setQueryFlags(int):android.content.pm.LauncherApps$ShortcutQuery, dex:  in method: android.content.pm.LauncherApps.ShortcutQuery.setQueryFlags(int):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.LauncherApps.ShortcutQuery.setQueryFlags(int):android.content.pm.LauncherApps$ShortcutQuery");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.content.pm.LauncherApps.ShortcutQuery.setShortcutIds(java.util.List):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.content.pm.LauncherApps.ShortcutQuery setShortcutIds(java.util.List<java.lang.String> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.content.pm.LauncherApps.ShortcutQuery.setShortcutIds(java.util.List):android.content.pm.LauncherApps$ShortcutQuery, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.LauncherApps.ShortcutQuery.setShortcutIds(java.util.List):android.content.pm.LauncherApps$ShortcutQuery");
        }
    }

    public LauncherApps(Context context, ILauncherApps service) {
        this.mCallbacks = new ArrayList();
        this.mAppsChangedListener = new Stub() {
            public void onPackageRemoved(UserHandle user, String packageName) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackageRemoved(packageName, user);
                    }
                }
            }

            public void onPackageChanged(UserHandle user, String packageName) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackageChanged(packageName, user);
                    }
                }
            }

            public void onPackageAdded(UserHandle user, String packageName) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackageAdded(packageName, user);
                    }
                }
            }

            public void onPackagesAvailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesAvailable(packageNames, user, replacing);
                    }
                }
            }

            public void onPackagesUnavailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesUnavailable(packageNames, user, replacing);
                    }
                }
            }

            public void onPackagesSuspended(UserHandle user, String[] packageNames) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesSuspended(packageNames, user);
                    }
                }
            }

            public void onPackagesUnsuspended(UserHandle user, String[] packageNames) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesUnsuspended(packageNames, user);
                    }
                }
            }

            public void onShortcutChanged(UserHandle user, String packageName, ParceledListSlice shortcuts) {
                List<ShortcutInfo> list = shortcuts.getList();
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnShortcutChanged(packageName, user, list);
                    }
                }
            }
        };
        this.mContext = context;
        this.mService = service;
        this.mPm = context.getPackageManager();
    }

    public LauncherApps(Context context) {
        this(context, ILauncherApps.Stub.asInterface(ServiceManager.getService(Context.LAUNCHER_APPS_SERVICE)));
    }

    public List<LauncherActivityInfo> getActivityList(String packageName, UserHandle user) {
        try {
            ParceledListSlice<ResolveInfo> activities = this.mService.getLauncherActivities(packageName, user);
            if (activities == null) {
                return Collections.EMPTY_LIST;
            }
            ArrayList<LauncherActivityInfo> lais = new ArrayList();
            for (ResolveInfo ri : activities.getList()) {
                lais.add(new LauncherActivityInfo(this.mContext, ri.activityInfo, user));
            }
            return lais;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public LauncherActivityInfo resolveActivity(Intent intent, UserHandle user) {
        try {
            ActivityInfo ai = this.mService.resolveActivity(intent.getComponent(), user);
            if (ai != null) {
                return new LauncherActivityInfo(this.mContext, ai, user);
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void startMainActivity(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        try {
            this.mService.startActivityAsUser(component, sourceBounds, opts, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void startAppDetailsActivity(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        try {
            this.mService.showAppDetailsAsUser(component, sourceBounds, opts, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isPackageEnabled(String packageName, UserHandle user) {
        try {
            return this.mService.isPackageEnabled(packageName, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags, UserHandle user) {
        try {
            return this.mService.getApplicationInfo(packageName, flags, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isActivityEnabled(ComponentName component, UserHandle user) {
        try {
            return this.mService.isActivityEnabled(component, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean hasShortcutHostPermission() {
        try {
            return this.mService.hasShortcutHostPermission(this.mContext.getPackageName());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<ShortcutInfo> getShortcuts(ShortcutQuery query, UserHandle user) {
        try {
            return this.mService.getShortcuts(this.mContext.getPackageName(), query.mChangedSince, query.mPackage, query.mShortcutIds, query.mActivity, query.mQueryFlags, user).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public List<ShortcutInfo> getShortcutInfo(String packageName, List<String> ids, UserHandle user) {
        ShortcutQuery q = new ShortcutQuery();
        q.setPackage(packageName);
        q.setShortcutIds(ids);
        q.setQueryFlags(11);
        return getShortcuts(q, user);
    }

    public void pinShortcuts(String packageName, List<String> shortcutIds, UserHandle user) {
        try {
            this.mService.pinShortcuts(this.mContext.getPackageName(), packageName, shortcutIds, user);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public int getShortcutIconResId(ShortcutInfo shortcut) {
        return shortcut.getIconResourceId();
    }

    @Deprecated
    public int getShortcutIconResId(String packageName, String shortcutId, UserHandle user) {
        ShortcutQuery q = new ShortcutQuery();
        q.setPackage(packageName);
        q.setShortcutIds(Arrays.asList(new String[]{shortcutId}));
        q.setQueryFlags(11);
        List<ShortcutInfo> shortcuts = getShortcuts(q, user);
        if (shortcuts.size() > 0) {
            return ((ShortcutInfo) shortcuts.get(0)).getIconResourceId();
        }
        return 0;
    }

    public ParcelFileDescriptor getShortcutIconFd(ShortcutInfo shortcut) {
        return getShortcutIconFd(shortcut.getPackage(), shortcut.getId(), shortcut.getUserId());
    }

    public ParcelFileDescriptor getShortcutIconFd(String packageName, String shortcutId, UserHandle user) {
        return getShortcutIconFd(packageName, shortcutId, user.getIdentifier());
    }

    private ParcelFileDescriptor getShortcutIconFd(String packageName, String shortcutId, int userId) {
        try {
            return this.mService.getShortcutIconFd(this.mContext.getPackageName(), packageName, shortcutId, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x005a A:{ExcHandler: android.content.pm.PackageManager.NameNotFoundException (e android.content.pm.PackageManager$NameNotFoundException), Splitter: B:18:0x0037} */
    /* JADX WARNING: Missing block: B:25:0x005b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Drawable getShortcutIconDrawable(ShortcutInfo shortcut, int density) {
        Drawable drawable = null;
        if (shortcut.hasIconFile()) {
            ParcelFileDescriptor pfd = getShortcutIconFd(shortcut);
            if (pfd == null) {
                return null;
            }
            try {
                Bitmap bmp = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                if (bmp != null) {
                    drawable = new BitmapDrawable(this.mContext.getResources(), bmp);
                }
                return drawable;
            } finally {
                try {
                    pfd.close();
                } catch (IOException e) {
                }
            }
        } else if (!shortcut.hasIconResource()) {
            return null;
        } else {
            try {
                int resId = shortcut.getIconResourceId();
                if (resId == 0) {
                    return null;
                }
                return this.mContext.getPackageManager().getResourcesForApplication(getApplicationInfo(shortcut.getPackage(), 0, shortcut.getUserHandle())).getDrawableForDensity(resId, density);
            } catch (NameNotFoundException e2) {
            }
        }
    }

    public Drawable getShortcutBadgedIconDrawable(ShortcutInfo shortcut, int density) {
        Drawable originalIcon = getShortcutIconDrawable(shortcut, density);
        if (originalIcon == null) {
            return null;
        }
        return this.mContext.getPackageManager().getUserBadgedIcon(originalIcon, shortcut.getUserHandle());
    }

    public void startShortcut(String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, UserHandle user) {
        startShortcut(packageName, shortcutId, sourceBounds, startActivityOptions, user.getIdentifier());
    }

    public void startShortcut(ShortcutInfo shortcut, Rect sourceBounds, Bundle startActivityOptions) {
        startShortcut(shortcut.getPackage(), shortcut.getId(), sourceBounds, startActivityOptions, shortcut.getUserId());
    }

    private void startShortcut(String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, int userId) {
        try {
            if (!this.mService.startShortcut(this.mContext.getPackageName(), packageName, shortcutId, sourceBounds, startActivityOptions, userId)) {
                throw new ActivityNotFoundException("Shortcut could not be started");
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerCallback(Callback callback) {
        registerCallback(callback, null);
    }

    public void registerCallback(Callback callback, Handler handler) {
        synchronized (this) {
            if (callback != null) {
                if (findCallbackLocked(callback) < 0) {
                    boolean addedFirstCallback = this.mCallbacks.size() == 0;
                    addCallbackLocked(callback, handler);
                    if (addedFirstCallback) {
                        try {
                            this.mService.addOnAppsChangedListener(this.mContext.getPackageName(), this.mAppsChangedListener);
                        } catch (RemoteException re) {
                            throw re.rethrowFromSystemServer();
                        }
                    }
                }
            }
        }
    }

    public void unregisterCallback(Callback callback) {
        synchronized (this) {
            removeCallbackLocked(callback);
            if (this.mCallbacks.size() == 0) {
                try {
                    this.mService.removeOnAppsChangedListener(this.mAppsChangedListener);
                } catch (RemoteException re) {
                    throw re.rethrowFromSystemServer();
                }
            }
        }
    }

    private int findCallbackLocked(Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            if (((CallbackMessageHandler) this.mCallbacks.get(i)).mCallback == callback) {
                return i;
            }
        }
        return -1;
    }

    private void removeCallbackLocked(Callback callback) {
        int pos = findCallbackLocked(callback);
        if (pos >= 0) {
            this.mCallbacks.remove(pos);
        }
    }

    private void addCallbackLocked(Callback callback, Handler handler) {
        removeCallbackLocked(callback);
        if (handler == null) {
            handler = new Handler();
        }
        this.mCallbacks.add(new CallbackMessageHandler(handler.getLooper(), callback));
    }
}
