package android.os.storage;

import android.app.ActivityThread;
import android.bluetooth.BluetoothClass.Device.Major;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.IPackageMoveObserver;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Binder;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IObbActionListener.Stub;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Preconditions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public class StorageManager {
    public static final String ACTION_MANAGE_STORAGE = "android.os.storage.action.MANAGE_STORAGE";
    public static final String ACTION_MEDIA_MOUNTED_RO = "android.intent.action.MEDIA_MOUNTED_RO";
    public static final int CRYPT_TYPE_DEFAULT = 1;
    public static final int CRYPT_TYPE_PASSWORD = 0;
    public static final int CRYPT_TYPE_PATTERN = 2;
    public static final int CRYPT_TYPE_PIN = 3;
    public static final int DEBUG_EMULATE_FBE = 2;
    public static final int DEBUG_FORCE_ADOPTABLE = 1;
    public static final int DEBUG_SDCARDFS_FORCE_OFF = 8;
    public static final int DEBUG_SDCARDFS_FORCE_ON = 4;
    private static final long DEFAULT_FULL_THRESHOLD_BYTES = 1048576;
    private static final long DEFAULT_THRESHOLD_MAX_BYTES = 104857600;
    private static final int DEFAULT_THRESHOLD_PERCENTAGE = 10;
    public static final int FLAG_FOR_WRITE = 256;
    public static final int FLAG_INCLUDE_INVISIBLE = 1024;
    public static final int FLAG_REAL_STATE = 512;
    public static final int FLAG_STORAGE_CE = 2;
    public static final int FLAG_STORAGE_DE = 1;
    private static final int INTERNAL_STORAGE_SECTOR_SIZE = 512;
    private static final String[] INTERNAL_STORAGE_SIZE_PATHS = null;
    public static final String OWNER_INFO_KEY = "OwnerInfo";
    public static final String PASSWORD_VISIBLE_KEY = "PasswordVisible";
    public static final String PATTERN_VISIBLE_KEY = "PatternVisible";
    public static final String PROP_EMULATE_FBE = "persist.sys.emulate_fbe";
    public static final String PROP_FORCE_ADOPTABLE = "persist.fw.force_adoptable";
    public static final String PROP_HAS_ADOPTABLE = "vold.has_adoptable";
    public static final String PROP_PRIMARY_PHYSICAL = "ro.vold.primary_physical";
    public static final String PROP_SDCARDFS = "persist.sys.sdcardfs";
    public static final String SYSTEM_LOCALE_KEY = "SystemLocale";
    private static final String TAG = "StorageManager";
    public static final String UUID_PRIMARY_PHYSICAL = "primary_physical";
    public static final String UUID_PRIVATE_INTERNAL = null;
    private static boolean mPanic;
    private static volatile IMountService sMountService;
    private final Context mContext;
    private final ArrayList<StorageEventListenerDelegate> mDelegates;
    private final Looper mLooper;
    private final IMountService mMountService;
    private final AtomicInteger mNextNonce;
    private final ObbActionListener mObbActionListener;
    private final ContentResolver mResolver;

    private class ObbActionListener extends Stub {
        private SparseArray<ObbListenerDelegate> mListeners;

        /* synthetic */ ObbActionListener(StorageManager this$0, ObbActionListener obbActionListener) {
            this();
        }

        private ObbActionListener() {
            this.mListeners = new SparseArray();
        }

        public void onObbResult(String filename, int nonce, int status) {
            ObbListenerDelegate delegate;
            synchronized (this.mListeners) {
                delegate = (ObbListenerDelegate) this.mListeners.get(nonce);
                if (delegate != null) {
                    this.mListeners.remove(nonce);
                }
            }
            if (delegate != null) {
                delegate.sendObbStateChanged(filename, status);
            }
        }

        public int addListener(OnObbStateChangeListener listener) {
            ObbListenerDelegate delegate = new ObbListenerDelegate(StorageManager.this, listener);
            synchronized (this.mListeners) {
                this.mListeners.put(ObbListenerDelegate.m43-get0(delegate), delegate);
            }
            return ObbListenerDelegate.m43-get0(delegate);
        }
    }

    private class ObbListenerDelegate {
        private final Handler mHandler;
        private final WeakReference<OnObbStateChangeListener> mObbEventListenerRef;
        private final int nonce;
        final /* synthetic */ StorageManager this$0;

        /* renamed from: android.os.storage.StorageManager$ObbListenerDelegate$1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ ObbListenerDelegate this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.os.storage.StorageManager.ObbListenerDelegate.1.<init>(android.os.storage.StorageManager$ObbListenerDelegate, android.os.Looper):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass1(android.os.storage.StorageManager.ObbListenerDelegate r1, android.os.Looper r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.os.storage.StorageManager.ObbListenerDelegate.1.<init>(android.os.storage.StorageManager$ObbListenerDelegate, android.os.Looper):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.os.storage.StorageManager.ObbListenerDelegate.1.<init>(android.os.storage.StorageManager$ObbListenerDelegate, android.os.Looper):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.os.storage.StorageManager.ObbListenerDelegate.1.handleMessage(android.os.Message):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void handleMessage(android.os.Message r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.os.storage.StorageManager.ObbListenerDelegate.1.handleMessage(android.os.Message):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.os.storage.StorageManager.ObbListenerDelegate.1.handleMessage(android.os.Message):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.os.storage.StorageManager.ObbListenerDelegate.-get0(android.os.storage.StorageManager$ObbListenerDelegate):int, dex:  in method: android.os.storage.StorageManager.ObbListenerDelegate.-get0(android.os.storage.StorageManager$ObbListenerDelegate):int, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.os.storage.StorageManager.ObbListenerDelegate.-get0(android.os.storage.StorageManager$ObbListenerDelegate):int, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ int m43-get0(android.os.storage.StorageManager.ObbListenerDelegate r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.os.storage.StorageManager.ObbListenerDelegate.-get0(android.os.storage.StorageManager$ObbListenerDelegate):int, dex:  in method: android.os.storage.StorageManager.ObbListenerDelegate.-get0(android.os.storage.StorageManager$ObbListenerDelegate):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.storage.StorageManager.ObbListenerDelegate.-get0(android.os.storage.StorageManager$ObbListenerDelegate):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.os.storage.StorageManager.ObbListenerDelegate.<init>(android.os.storage.StorageManager, android.os.storage.OnObbStateChangeListener):void, dex: 
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
        ObbListenerDelegate(android.os.storage.StorageManager r1, android.os.storage.OnObbStateChangeListener r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.os.storage.StorageManager.ObbListenerDelegate.<init>(android.os.storage.StorageManager, android.os.storage.OnObbStateChangeListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.storage.StorageManager.ObbListenerDelegate.<init>(android.os.storage.StorageManager, android.os.storage.OnObbStateChangeListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.os.storage.StorageManager.ObbListenerDelegate.getListener():android.os.storage.OnObbStateChangeListener, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        android.os.storage.OnObbStateChangeListener getListener() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.os.storage.StorageManager.ObbListenerDelegate.getListener():android.os.storage.OnObbStateChangeListener, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.storage.StorageManager.ObbListenerDelegate.getListener():android.os.storage.OnObbStateChangeListener");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.os.storage.StorageManager.ObbListenerDelegate.sendObbStateChanged(java.lang.String, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        void sendObbStateChanged(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.os.storage.StorageManager.ObbListenerDelegate.sendObbStateChanged(java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.storage.StorageManager.ObbListenerDelegate.sendObbStateChanged(java.lang.String, int):void");
        }
    }

    private static class StorageEventListenerDelegate extends IMountServiceListener.Stub implements Callback {
        private static final int MSG_DISK_DESTROYED = 6;
        private static final int MSG_DISK_SCANNED = 5;
        private static final int MSG_STORAGE_STATE_CHANGED = 1;
        private static final int MSG_VOLUME_FORGOTTEN = 4;
        private static final int MSG_VOLUME_RECORD_CHANGED = 3;
        private static final int MSG_VOLUME_STATE_CHANGED = 2;
        final StorageEventListener mCallback;
        final Handler mHandler;

        public StorageEventListenerDelegate(StorageEventListener callback, Looper looper) {
            this.mCallback = callback;
            this.mHandler = new Handler(looper, (Callback) this);
        }

        public boolean handleMessage(Message msg) {
            SomeArgs args = msg.obj;
            switch (msg.what) {
                case 1:
                    this.mCallback.onStorageStateChanged((String) args.arg1, (String) args.arg2, (String) args.arg3);
                    args.recycle();
                    return true;
                case 2:
                    this.mCallback.onVolumeStateChanged((VolumeInfo) args.arg1, args.argi2, args.argi3);
                    args.recycle();
                    return true;
                case 3:
                    this.mCallback.onVolumeRecordChanged((VolumeRecord) args.arg1);
                    args.recycle();
                    return true;
                case 4:
                    this.mCallback.onVolumeForgotten((String) args.arg1);
                    args.recycle();
                    return true;
                case 5:
                    this.mCallback.onDiskScanned((DiskInfo) args.arg1, args.argi2);
                    args.recycle();
                    return true;
                case 6:
                    this.mCallback.onDiskDestroyed((DiskInfo) args.arg1);
                    args.recycle();
                    return true;
                default:
                    args.recycle();
                    return false;
            }
        }

        public void onUsbMassStorageConnectionChanged(boolean connected) throws RemoteException {
        }

        public void onStorageStateChanged(String path, String oldState, String newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = path;
            args.arg2 = oldState;
            args.arg3 = newState;
            this.mHandler.obtainMessage(1, args).sendToTarget();
        }

        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = vol;
            args.argi2 = oldState;
            args.argi3 = newState;
            this.mHandler.obtainMessage(2, args).sendToTarget();
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = rec;
            this.mHandler.obtainMessage(3, args).sendToTarget();
        }

        public void onVolumeForgotten(String fsUuid) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fsUuid;
            this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        public void onDiskScanned(DiskInfo disk, int volumeCount) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk;
            args.argi2 = volumeCount;
            this.mHandler.obtainMessage(5, args).sendToTarget();
        }

        public void onDiskDestroyed(DiskInfo disk) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk;
            this.mHandler.obtainMessage(6, args).sendToTarget();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.os.storage.StorageManager.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.os.storage.StorageManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.storage.StorageManager.<clinit>():void");
    }

    private int getNextNonce() {
        return this.mNextNonce.getAndIncrement();
    }

    @Deprecated
    public static StorageManager from(Context context) {
        return (StorageManager) context.getSystemService(StorageManager.class);
    }

    public StorageManager(Context context, Looper looper) {
        this.mNextNonce = new AtomicInteger(0);
        this.mDelegates = new ArrayList();
        this.mObbActionListener = new ObbActionListener(this, null);
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mLooper = looper;
        this.mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        if (this.mMountService == null) {
            throw new IllegalStateException("Failed to find running mount service");
        }
    }

    public void registerListener(StorageEventListener listener) {
        synchronized (this.mDelegates) {
            StorageEventListenerDelegate delegate = new StorageEventListenerDelegate(listener, this.mLooper);
            try {
                this.mMountService.registerListener(delegate);
                this.mDelegates.add(delegate);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void unregisterListener(StorageEventListener listener) {
        synchronized (this.mDelegates) {
            Iterator<StorageEventListenerDelegate> i = this.mDelegates.iterator();
            while (i.hasNext()) {
                StorageEventListenerDelegate delegate = (StorageEventListenerDelegate) i.next();
                if (delegate.mCallback == listener) {
                    try {
                        this.mMountService.unregisterListener(delegate);
                        i.remove();
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    @Deprecated
    public void enableUsbMassStorage() {
        try {
            this.mMountService.setUsbMassStorageEnabled(true);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to enable UMS", ex);
        }
    }

    @Deprecated
    public void disableUsbMassStorage() {
        try {
            this.mMountService.setUsbMassStorageEnabled(false);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to disable UMS", ex);
        }
    }

    @Deprecated
    public boolean isUsbMassStorageConnected() {
        try {
            return this.mMountService.isUsbMassStorageConnected();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to get UMS connection state", ex);
            return false;
        }
    }

    @Deprecated
    public boolean isUsbMassStorageEnabled() {
        try {
            return this.mMountService.isUsbMassStorageEnabled();
        } catch (RemoteException rex) {
            Log.e(TAG, "Failed to get UMS enable state", rex);
            return false;
        }
    }

    public boolean mountObb(String rawPath, String key, OnObbStateChangeListener listener) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        try {
            String str = rawPath;
            String str2 = key;
            this.mMountService.mountObb(str, new File(rawPath).getCanonicalPath(), str2, this.mObbActionListener, this.mObbActionListener.addListener(listener));
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to resolve path: " + rawPath, e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public boolean unmountObb(String rawPath, boolean force, OnObbStateChangeListener listener) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        try {
            this.mMountService.unmountObb(rawPath, force, this.mObbActionListener, this.mObbActionListener.addListener(listener));
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isObbMounted(String rawPath) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        try {
            return this.mMountService.isObbMounted(rawPath);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getMountedObbPath(String rawPath) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        try {
            return this.mMountService.getMountedObbPath(rawPath);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<DiskInfo> getDisks() {
        try {
            return Arrays.asList(this.mMountService.getDisks());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public DiskInfo findDiskById(String id) {
        Preconditions.checkNotNull(id);
        for (DiskInfo disk : getDisks()) {
            if (Objects.equals(disk.id, id)) {
                return disk;
            }
        }
        return null;
    }

    public VolumeInfo findVolumeById(String id) {
        Preconditions.checkNotNull(id);
        for (VolumeInfo vol : getVolumes()) {
            if (Objects.equals(vol.id, id)) {
                return vol;
            }
        }
        return null;
    }

    public VolumeInfo findVolumeByUuid(String fsUuid) {
        Preconditions.checkNotNull(fsUuid);
        for (VolumeInfo vol : getVolumes()) {
            if (Objects.equals(vol.fsUuid, fsUuid)) {
                return vol;
            }
        }
        return null;
    }

    public VolumeRecord findRecordByUuid(String fsUuid) {
        Preconditions.checkNotNull(fsUuid);
        for (VolumeRecord rec : getVolumeRecords()) {
            if (Objects.equals(rec.fsUuid, fsUuid)) {
                return rec;
            }
        }
        return null;
    }

    public VolumeInfo findPrivateForEmulated(VolumeInfo emulatedVol) {
        if (emulatedVol != null) {
            return findVolumeById(emulatedVol.getId().replace(VolumeInfo.ID_EMULATED_INTERNAL, VolumeInfo.ID_PRIVATE_INTERNAL));
        }
        return null;
    }

    public VolumeInfo findEmulatedForPrivate(VolumeInfo privateVol) {
        if (privateVol != null) {
            return findVolumeById(privateVol.getId().replace(VolumeInfo.ID_PRIVATE_INTERNAL, VolumeInfo.ID_EMULATED_INTERNAL));
        }
        return null;
    }

    public VolumeInfo findVolumeByQualifiedUuid(String volumeUuid) {
        if (Objects.equals(UUID_PRIVATE_INTERNAL, volumeUuid)) {
            return findVolumeById(VolumeInfo.ID_PRIVATE_INTERNAL);
        }
        if (Objects.equals(UUID_PRIMARY_PHYSICAL, volumeUuid)) {
            return getPrimaryPhysicalVolume();
        }
        return findVolumeByUuid(volumeUuid);
    }

    public List<VolumeInfo> getVolumes() {
        try {
            return Arrays.asList(this.mMountService.getVolumes(0));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<VolumeInfo> getWritablePrivateVolumes() {
        try {
            ArrayList<VolumeInfo> res = new ArrayList();
            for (VolumeInfo vol : this.mMountService.getVolumes(0)) {
                if (vol.getType() == 1 && vol.isMountedWritable()) {
                    res.add(vol);
                }
            }
            return res;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<VolumeRecord> getVolumeRecords() {
        try {
            return Arrays.asList(this.mMountService.getVolumeRecords(0));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getBestVolumeDescription(VolumeInfo vol) {
        if (vol == null) {
            return null;
        }
        if (!TextUtils.isEmpty(vol.fsUuid)) {
            VolumeRecord rec = findRecordByUuid(vol.fsUuid);
            if (!(rec == null || TextUtils.isEmpty(rec.nickname))) {
                return rec.nickname;
            }
        }
        if (vol.disk != null && vol.disk.isSd()) {
            return this.mContext.getString(17040597);
        }
        if (vol.isPhoneStorage()) {
            String volumeName = this.mContext.getResources().getString(134545636);
            Slog.i(TAG, "getBestVolumeDescription, return volumeName=" + volumeName);
            return volumeName;
        } else if (!TextUtils.isEmpty(vol.getDescription())) {
            return vol.getDescription();
        } else {
            if (vol.disk != null) {
                return vol.disk.getDescription();
            }
            return null;
        }
    }

    public VolumeInfo getPrimaryPhysicalVolume() {
        for (VolumeInfo vol : getVolumes()) {
            if (vol.isPrimaryPhysical()) {
                return vol;
            }
        }
        return null;
    }

    public void mount(String volId) {
        try {
            this.mMountService.mount(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unmount(String volId) {
        try {
            this.mMountService.unmount(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void format(String volId) {
        try {
            this.mMountService.format(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long benchmark(String volId) {
        try {
            return this.mMountService.benchmark(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void partitionPublic(String diskId) {
        try {
            this.mMountService.partitionPublic(diskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void partitionPrivate(String diskId) {
        try {
            this.mMountService.partitionPrivate(diskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void partitionMixed(String diskId, int ratio) {
        try {
            this.mMountService.partitionMixed(diskId, ratio);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void wipeAdoptableDisks() {
        for (DiskInfo disk : getDisks()) {
            String diskId = disk.getId();
            if (disk.isAdoptable()) {
                Slog.d(TAG, "Found adoptable " + diskId + "; wiping");
                try {
                    this.mMountService.partitionPublic(diskId);
                } catch (Exception e) {
                    Slog.w(TAG, "Failed to wipe " + diskId + ", but soldiering onward", e);
                }
            } else {
                Slog.d(TAG, "Ignorning non-adoptable disk " + disk.getId());
            }
        }
    }

    public void setVolumeNickname(String fsUuid, String nickname) {
        try {
            this.mMountService.setVolumeNickname(fsUuid, nickname);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setVolumeInited(String fsUuid, boolean inited) {
        int i = 1;
        try {
            IMountService iMountService = this.mMountService;
            if (!inited) {
                i = 0;
            }
            iMountService.setVolumeUserFlags(fsUuid, i, 1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setVolumeSnoozed(String fsUuid, boolean snoozed) {
        int i = 2;
        try {
            IMountService iMountService = this.mMountService;
            if (!snoozed) {
                i = 0;
            }
            iMountService.setVolumeUserFlags(fsUuid, i, 2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void forgetVolume(String fsUuid) {
        try {
            this.mMountService.forgetVolume(fsUuid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getPrimaryStorageUuid() {
        try {
            return this.mMountService.getPrimaryStorageUuid();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) {
        try {
            this.mMountService.setPrimaryStorageUuid(volumeUuid, callback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public StorageVolume getStorageVolume(File file) {
        return getStorageVolume(getVolumeList(), file);
    }

    public static StorageVolume getStorageVolume(File file, int userId) {
        return getStorageVolume(getVolumeList(userId, 0), file);
    }

    private static StorageVolume getStorageVolume(StorageVolume[] volumes, File file) {
        if (file == null) {
            return null;
        }
        try {
            file = file.getCanonicalFile();
            int i = 0;
            int length = volumes.length;
            while (i < length) {
                StorageVolume volume = volumes[i];
                try {
                    if (FileUtils.contains(volume.getPathFile().getCanonicalFile(), file)) {
                        return volume;
                    }
                    i++;
                } catch (IOException e) {
                }
            }
            return null;
        } catch (IOException e2) {
            Slog.d(TAG, "Could not get canonical path for " + file);
            return null;
        }
    }

    @Deprecated
    public String getVolumeState(String mountPoint) {
        StorageVolume vol = getStorageVolume(new File(mountPoint));
        if (vol != null) {
            return vol.getState();
        }
        return "unknown";
    }

    public List<StorageVolume> getStorageVolumes() {
        ArrayList<StorageVolume> res = new ArrayList();
        Collections.addAll(res, getVolumeList(UserHandle.myUserId(), Major.IMAGING));
        return res;
    }

    public StorageVolume getPrimaryStorageVolume() {
        return getVolumeList(UserHandle.myUserId(), Major.IMAGING)[0];
    }

    public long getPrimaryStorageSize() {
        String path = null;
        boolean UFSDevice = SystemProperties.get("ro.mtk_ufs_booting").equals(WifiEnterpriseConfig.ENGINE_ENABLE);
        boolean EMMCDevice = SystemProperties.get("ro.mtk_emmc_support").equals(WifiEnterpriseConfig.ENGINE_ENABLE);
        Slog.d(TAG, "Current device, UFS: " + UFSDevice + "Emmc: " + EMMCDevice);
        if (UFSDevice) {
            path = "/sys/block/sdc/size";
        } else if (EMMCDevice) {
            path = "/sys/block/mmcblk0/size";
        }
        Slog.d(TAG, "Current device Path: " + path);
        long numberBlocks = readLong(path);
        Slog.d(TAG, "Current device numberBlocks: " + numberBlocks);
        if (numberBlocks > 0) {
            return 512 * numberBlocks;
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x005d A:{SYNTHETIC, Splitter: B:35:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0063 A:{SYNTHETIC, Splitter: B:39:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0080 A:{Catch:{ Exception -> 0x006a }} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0069 A:{SYNTHETIC, Splitter: B:43:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x005d A:{SYNTHETIC, Splitter: B:35:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0063 A:{SYNTHETIC, Splitter: B:39:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0069 A:{SYNTHETIC, Splitter: B:43:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0080 A:{Catch:{ Exception -> 0x006a }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private long readLong(String path) {
        Throwable th;
        Throwable th2;
        Exception e;
        Throwable th3 = null;
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2;
            FileInputStream fis2 = new FileInputStream(path);
            try {
                reader2 = new BufferedReader(new InputStreamReader(fis2));
            } catch (Throwable th4) {
                th = th4;
                fis = fis2;
                if (reader != null) {
                }
                th2 = th3;
                if (fis != null) {
                }
                th3 = th2;
                if (th3 != null) {
                }
            }
            try {
                long parseLong = Long.parseLong(reader2.readLine());
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Throwable th5) {
                        th3 = th5;
                    }
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (Throwable th6) {
                        th = th6;
                        if (th3 != null) {
                            if (th3 != th) {
                                th3.addSuppressed(th);
                                th = th3;
                            }
                        }
                    }
                }
                th = th3;
                if (th == null) {
                    return parseLong;
                }
                try {
                    throw th;
                } catch (Exception e2) {
                    e = e2;
                    fis = fis2;
                }
            } catch (Throwable th7) {
                th = th7;
                reader = reader2;
                fis = fis2;
                if (reader != null) {
                }
                th2 = th3;
                if (fis != null) {
                }
                th3 = th2;
                if (th3 != null) {
                }
            }
        } catch (Throwable th8) {
            th = th8;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable th9) {
                    th2 = th9;
                    if (th3 != null) {
                        if (th3 != th2) {
                            th3.addSuppressed(th2);
                            th2 = th3;
                        }
                    }
                }
            }
            th2 = th3;
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable th10) {
                    th3 = th10;
                    if (th2 != null) {
                        if (th2 != th3) {
                            th2.addSuppressed(th3);
                            th3 = th2;
                        }
                    }
                }
            }
            th3 = th2;
            if (th3 != null) {
                try {
                    throw th3;
                } catch (Exception e3) {
                    e = e3;
                    Slog.w(TAG, "Could not read " + path, e);
                    return 0;
                }
            }
            throw th;
        }
    }

    public StorageVolume[] getVolumeList() {
        return getVolumeList(this.mContext.getUserId(), 0);
    }

    public static StorageVolume[] getVolumeList(int userId, int flags) {
        IMountService mountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        if (mountService == null) {
            Slog.d(TAG, "ServiceManager not able to return a valid MountService instance");
            new Exception().printStackTrace();
            return new StorageVolume[0];
        }
        try {
            String packageName = ActivityThread.currentOpPackageName();
            if (packageName == null) {
                String[] packageNames = ActivityThread.getPackageManager().getPackagesForUid(Process.myUid());
                if (packageNames == null || packageNames.length <= 0) {
                    return new StorageVolume[0];
                }
                packageName = packageNames[0];
            }
            int uid = ActivityThread.getPackageManager().getPackageUid(packageName, 268435456, userId);
            if (uid <= 0) {
                return new StorageVolume[0];
            }
            return mountService.getVolumeList(uid, packageName, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public String[] getVolumePaths() {
        StorageVolume[] volumes = getVolumeList();
        int count = volumes.length;
        String[] paths = new String[count];
        for (int i = 0; i < count; i++) {
            paths[i] = volumes[i].getPath();
        }
        return paths;
    }

    public StorageVolume getPrimaryVolume() {
        return getPrimaryVolume(getVolumeList());
    }

    public static StorageVolume getPrimaryVolume(StorageVolume[] volumes) {
        for (StorageVolume volume : volumes) {
            if (volume.isPrimary()) {
                return volume;
            }
        }
        throw new IllegalStateException("Missing primary storage");
    }

    public long getStorageBytesUntilLow(File path) {
        return path.getUsableSpace() - getStorageFullBytes(path);
    }

    public long getStorageLowBytes(File path) {
        long lowPercent = (long) Global.getInt(this.mResolver, Global.SYS_STORAGE_THRESHOLD_PERCENTAGE, 10);
        long lowBytes = (path.getTotalSpace() * lowPercent) / 100;
        if (mPanic) {
            Slog.w(TAG, "lowPercent=" + lowPercent + ",DEFAULT_THRESHOLD_PERCENTAGE=" + 10);
            Slog.w(TAG, "TotalSpace=" + path.getTotalSpace() + ",lowBytes=" + lowBytes);
        }
        long maxLowBytes = Global.getLong(this.mResolver, Global.SYS_STORAGE_THRESHOLD_MAX_BYTES, DEFAULT_THRESHOLD_MAX_BYTES);
        if (mPanic) {
            Slog.w(TAG, "maxLowBytes=" + maxLowBytes + ",DEFAULT_THRESHOLD_MAX_BYTES=" + DEFAULT_THRESHOLD_MAX_BYTES);
        }
        return Math.min(lowBytes, maxLowBytes);
    }

    public long getStorageFullBytes(File path) {
        return Global.getLong(this.mResolver, Global.SYS_STORAGE_FULL_THRESHOLD_BYTES, 1048576);
    }

    public void createUserKey(int userId, int serialNumber, boolean ephemeral) {
        try {
            this.mMountService.createUserKey(userId, serialNumber, ephemeral);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void destroyUserKey(int userId) {
        try {
            this.mMountService.destroyUserKey(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            this.mMountService.unlockUserKey(userId, serialNumber, token, secret);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void lockUserKey(int userId) {
        try {
            this.mMountService.lockUserKey(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) {
        try {
            this.mMountService.prepareUserStorage(volumeUuid, userId, serialNumber, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void destroyUserStorage(String volumeUuid, int userId, int flags) {
        try {
            this.mMountService.destroyUserStorage(volumeUuid, userId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean isUserKeyUnlocked(int userId) {
        if (sMountService == null) {
            sMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        }
        if (sMountService == null) {
            Slog.w(TAG, "Early during boot, assuming locked");
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            boolean isUserKeyUnlocked = sMountService.isUserKeyUnlocked(userId);
            Binder.restoreCallingIdentity(token);
            return isUserKeyUnlocked;
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean isEncrypted(File file) {
        if (FileUtils.contains(Environment.getDataDirectory(), file)) {
            return isEncrypted();
        }
        if (FileUtils.contains(Environment.getExpandDirectory(), file)) {
            return true;
        }
        return false;
    }

    public static boolean isEncryptable() {
        return !"unsupported".equalsIgnoreCase(SystemProperties.get("ro.crypto.state", "unsupported"));
    }

    public static boolean isEncrypted() {
        return "encrypted".equalsIgnoreCase(SystemProperties.get("ro.crypto.state", ""));
    }

    public static boolean isFileEncryptedNativeOnly() {
        if (!isEncrypted()) {
            return false;
        }
        return "file".equalsIgnoreCase(SystemProperties.get("ro.crypto.type", ""));
    }

    public static boolean isBlockEncrypted() {
        if (!isEncrypted()) {
            return false;
        }
        return "block".equalsIgnoreCase(SystemProperties.get("ro.crypto.type", ""));
    }

    public static boolean isNonDefaultBlockEncrypted() {
        boolean z = true;
        if (!isBlockEncrypted()) {
            return false;
        }
        try {
            if (IMountService.Stub.asInterface(ServiceManager.getService("mount")).getPasswordType() == 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting encryption type");
            return false;
        }
    }

    public static boolean isBlockEncrypting() {
        return !"".equalsIgnoreCase(SystemProperties.get("vold.encrypt_progress", ""));
    }

    public static boolean inCryptKeeperBounce() {
        return "trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"));
    }

    public static boolean isFileEncryptedEmulatedOnly() {
        return SystemProperties.getBoolean(PROP_EMULATE_FBE, false);
    }

    public static boolean isFileEncryptedNativeOrEmulated() {
        if (isFileEncryptedNativeOnly()) {
            return true;
        }
        return isFileEncryptedEmulatedOnly();
    }

    public static File maybeTranslateEmulatedPathToInternal(File path) {
        try {
            for (VolumeInfo vol : IMountService.Stub.asInterface(ServiceManager.getService("mount")).getVolumes(0)) {
                if ((vol.getType() == 2 || vol.getType() == 0) && vol.isMountedReadable()) {
                    File internalPath = FileUtils.rewriteAfterRename(vol.getPath(), vol.getInternalPath(), path);
                    if (internalPath != null && internalPath.exists()) {
                        return internalPath;
                    }
                }
            }
            return path;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ParcelFileDescriptor mountAppFuse(String name) {
        try {
            return this.mMountService.mountAppFuse(name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
