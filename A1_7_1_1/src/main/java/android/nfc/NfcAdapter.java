package android.nfc;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.OnActivityPausedListener;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.ITagRemovedCallback.Stub;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.mediatek.nfcgsma_extras.INfcAdapterGsmaExtras;
import java.util.HashMap;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class NfcAdapter {
    public static final String ACTION_ADAPTER_STATE_CHANGED = "android.nfc.action.ADAPTER_STATE_CHANGED";
    public static final String ACTION_HANDOVER_TRANSFER_DONE = "android.nfc.action.HANDOVER_TRANSFER_DONE";
    public static final String ACTION_HANDOVER_TRANSFER_STARTED = "android.nfc.action.HANDOVER_TRANSFER_STARTED";
    public static final String ACTION_NDEF_DISCOVERED = "android.nfc.action.NDEF_DISCOVERED";
    public static final String ACTION_TAG_DISCOVERED = "android.nfc.action.TAG_DISCOVERED";
    public static final String ACTION_TAG_LEFT_FIELD = "android.nfc.action.TAG_LOST";
    public static final String ACTION_TECH_DISCOVERED = "android.nfc.action.TECH_DISCOVERED";
    public static final String EXTRA_ADAPTER_STATE = "android.nfc.extra.ADAPTER_STATE";
    public static final String EXTRA_HANDOVER_TRANSFER_STATUS = "android.nfc.extra.HANDOVER_TRANSFER_STATUS";
    public static final String EXTRA_HANDOVER_TRANSFER_URI = "android.nfc.extra.HANDOVER_TRANSFER_URI";
    public static final String EXTRA_ID = "android.nfc.extra.ID";
    public static final String EXTRA_NDEF_MESSAGES = "android.nfc.extra.NDEF_MESSAGES";
    public static final String EXTRA_READER_PRESENCE_CHECK_DELAY = "presence";
    public static final String EXTRA_TAG = "android.nfc.extra.TAG";
    public static final int FLAG_NDEF_PUSH_NO_CONFIRM = 1;
    public static final int FLAG_OFF = 0;
    public static final int FLAG_ON = 1;
    public static final int FLAG_READER_NFC_A = 1;
    public static final int FLAG_READER_NFC_B = 2;
    public static final int FLAG_READER_NFC_BARCODE = 16;
    public static final int FLAG_READER_NFC_F = 4;
    public static final int FLAG_READER_NFC_V = 8;
    public static final int FLAG_READER_NO_PLATFORM_SOUNDS = 256;
    public static final int FLAG_READER_SKIP_NDEF_CHECK = 128;
    public static final int HANDOVER_TRANSFER_STATUS_FAILURE = 1;
    public static final int HANDOVER_TRANSFER_STATUS_SUCCESS = 0;
    public static final int MODE_CARD = 2;
    public static final int MODE_P2P = 4;
    public static final int MODE_READER = 1;
    public static final int SIM_1 = 1;
    public static final int SIM_2 = 2;
    public static final int SIM_3 = 3;
    public static final int STATE_OFF = 1;
    public static final int STATE_ON = 3;
    public static final int STATE_TURNING_OFF = 4;
    public static final int STATE_TURNING_ON = 2;
    static final String TAG = "NFC";
    static INfcCardEmulation sCardEmulationService;
    static boolean sHasNfcFeature;
    static boolean sIsInitialized;
    static HashMap<Context, NfcAdapter> sNfcAdapters;
    static INfcFCardEmulation sNfcFCardEmulationService;
    static NfcAdapter sNullContextNfcAdapter;
    static INfcAdapter sService;
    static INfcTag sTagService;
    final Context mContext;
    OnActivityPausedListener mForegroundDispatchListener;
    final Object mLock;
    final NfcActivityManager mNfcActivityManager;
    final HashMap<NfcUnlockHandler, INfcUnlockHandler> mNfcUnlockHandlers;
    ITagRemovedCallback mTagRemovedListener;

    /* renamed from: android.nfc.NfcAdapter$2 */
    class AnonymousClass2 extends Stub {
        final /* synthetic */ NfcAdapter this$0;
        final /* synthetic */ Handler val$handler;
        final /* synthetic */ OnTagRemovedListener val$tagRemovedListener;

        /* renamed from: android.nfc.NfcAdapter$2$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ AnonymousClass2 this$1;
            final /* synthetic */ OnTagRemovedListener val$tagRemovedListener;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.nfc.NfcAdapter.2.1.<init>(android.nfc.NfcAdapter$2, android.nfc.NfcAdapter$OnTagRemovedListener):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            AnonymousClass1(android.nfc.NfcAdapter.AnonymousClass2 r1, android.nfc.NfcAdapter.OnTagRemovedListener r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.nfc.NfcAdapter.2.1.<init>(android.nfc.NfcAdapter$2, android.nfc.NfcAdapter$OnTagRemovedListener):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.2.1.<init>(android.nfc.NfcAdapter$2, android.nfc.NfcAdapter$OnTagRemovedListener):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.nfc.NfcAdapter.2.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.nfc.NfcAdapter.2.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.2.1.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.nfc.NfcAdapter.2.<init>(android.nfc.NfcAdapter, android.os.Handler, android.nfc.NfcAdapter$OnTagRemovedListener):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(android.nfc.NfcAdapter r1, android.os.Handler r2, android.nfc.NfcAdapter.OnTagRemovedListener r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.nfc.NfcAdapter.2.<init>(android.nfc.NfcAdapter, android.os.Handler, android.nfc.NfcAdapter$OnTagRemovedListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.2.<init>(android.nfc.NfcAdapter, android.os.Handler, android.nfc.NfcAdapter$OnTagRemovedListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.nfc.NfcAdapter.2.onTagRemoved():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onTagRemoved() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.nfc.NfcAdapter.2.onTagRemoved():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.2.onTagRemoved():void");
        }
    }

    /* renamed from: android.nfc.NfcAdapter$3 */
    class AnonymousClass3 extends INfcUnlockHandler.Stub {
        final /* synthetic */ NfcAdapter this$0;
        final /* synthetic */ NfcUnlockHandler val$unlockHandler;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.nfc.NfcAdapter.3.<init>(android.nfc.NfcAdapter, android.nfc.NfcAdapter$NfcUnlockHandler):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass3(android.nfc.NfcAdapter r1, android.nfc.NfcAdapter.NfcUnlockHandler r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.nfc.NfcAdapter.3.<init>(android.nfc.NfcAdapter, android.nfc.NfcAdapter$NfcUnlockHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.3.<init>(android.nfc.NfcAdapter, android.nfc.NfcAdapter$NfcUnlockHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.nfc.NfcAdapter.3.onUnlockAttempted(android.nfc.Tag):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean onUnlockAttempted(android.nfc.Tag r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.nfc.NfcAdapter.3.onUnlockAttempted(android.nfc.Tag):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.3.onUnlockAttempted(android.nfc.Tag):boolean");
        }
    }

    public interface CreateBeamUrisCallback {
        Uri[] createBeamUris(NfcEvent nfcEvent);
    }

    public interface CreateNdefMessageCallback {
        NdefMessage createNdefMessage(NfcEvent nfcEvent);
    }

    public interface NfcUnlockHandler {
        boolean onUnlockAttempted(Tag tag);
    }

    public interface OnNdefPushCompleteCallback {
        void onNdefPushComplete(NfcEvent nfcEvent);
    }

    public interface OnTagRemovedListener {
        void onTagRemoved();
    }

    public interface ReaderCallback {
        void onTagDiscovered(Tag tag);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.nfc.NfcAdapter.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.nfc.NfcAdapter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.<clinit>():void");
    }

    private static boolean hasNfcFeature() {
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "Cannot get package manager, assuming no NFC feature");
            return false;
        }
        try {
            return pm.hasSystemFeature(PackageManager.FEATURE_NFC, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "Package manager query failed, assuming no NFC feature", e);
            return false;
        }
    }

    private static boolean hasNfcHceFeature() {
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "Cannot get package manager, assuming no NFC feature");
            return false;
        }
        try {
            boolean z;
            if (pm.hasSystemFeature("android.hardware.nfc.hce", 0)) {
                z = true;
            } else {
                z = pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF, 0);
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "Package manager query failed, assuming no NFC feature", e);
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:51:0x00ba, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized NfcAdapter getNfcAdapter(Context context) {
        synchronized (NfcAdapter.class) {
            if (!sIsInitialized) {
                sHasNfcFeature = hasNfcFeature();
                boolean hasHceFeature = hasNfcHceFeature();
                if (sHasNfcFeature || hasHceFeature) {
                    sService = getServiceInterface();
                    if (sService == null) {
                        Log.e(TAG, "could not retrieve NFC service");
                        throw new UnsupportedOperationException();
                    }
                    if (sHasNfcFeature) {
                        try {
                            sTagService = sService.getNfcTagInterface();
                        } catch (RemoteException e) {
                            Log.e(TAG, "could not retrieve card emulation service");
                            throw new UnsupportedOperationException();
                        } catch (RemoteException e2) {
                            Log.e(TAG, "could not retrieve NFC-F card emulation service");
                            throw new UnsupportedOperationException();
                        } catch (RemoteException e3) {
                            Log.e(TAG, "could not retrieve NFC Tag service");
                            throw new UnsupportedOperationException();
                        }
                    }
                    if (hasHceFeature) {
                        sNfcFCardEmulationService = sService.getNfcFCardEmulationInterface();
                        sCardEmulationService = sService.getNfcCardEmulationInterface();
                    }
                    sIsInitialized = true;
                } else {
                    Log.v(TAG, "this device does not have NFC support");
                    throw new UnsupportedOperationException();
                }
            }
            if (context == null) {
                if (sNullContextNfcAdapter == null) {
                    sNullContextNfcAdapter = new NfcAdapter(null);
                }
                NfcAdapter nfcAdapter = sNullContextNfcAdapter;
                return nfcAdapter;
            }
            NfcAdapter adapter = (NfcAdapter) sNfcAdapters.get(context);
            if (adapter == null) {
                adapter = new NfcAdapter(context);
                sNfcAdapters.put(context, adapter);
            }
        }
    }

    private static INfcAdapter getServiceInterface() {
        IBinder b = ServiceManager.getService("nfc");
        if (b == null) {
            return null;
        }
        return INfcAdapter.Stub.asInterface(b);
    }

    public static NfcAdapter getDefaultAdapter(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }
        context = context.getApplicationContext();
        if (context == null) {
            throw new IllegalArgumentException("context not associated with any application (using a mock context?)");
        }
        NfcManager manager = (NfcManager) context.getSystemService("nfc");
        if (manager == null) {
            return null;
        }
        return manager.getDefaultAdapter();
    }

    @Deprecated
    public static NfcAdapter getDefaultAdapter() {
        Log.w(TAG, "WARNING: NfcAdapter.getDefaultAdapter() is deprecated, use NfcAdapter.getDefaultAdapter(Context) instead", new Exception());
        return getNfcAdapter(null);
    }

    NfcAdapter(Context context) {
        this.mForegroundDispatchListener = new OnActivityPausedListener() {
            public void onPaused(Activity activity) {
                NfcAdapter.this.disableForegroundDispatchInternal(activity, true);
            }
        };
        this.mContext = context;
        this.mNfcActivityManager = new NfcActivityManager(this);
        this.mNfcUnlockHandlers = new HashMap();
        this.mTagRemovedListener = null;
        this.mLock = new Object();
    }

    public Context getContext() {
        return this.mContext;
    }

    public INfcAdapter getService() {
        isEnabled();
        return sService;
    }

    public INfcTag getTagService() {
        isEnabled();
        return sTagService;
    }

    public INfcCardEmulation getCardEmulationService() {
        isEnabled();
        try {
            INfcCardEmulation service = sService.getNfcCardEmulationInterface();
            Log.v(TAG, "Update INfcCardEmulation interface");
            synchronized (NfcAdapter.class) {
                sCardEmulationService = service;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Could not retrieve card emulation service from NfcAdapter, now");
        }
        return sCardEmulationService;
    }

    public INfcFCardEmulation getNfcFCardEmulationService() {
        isEnabled();
        return sNfcFCardEmulationService;
    }

    public void attemptDeadServiceRecovery(Exception e) {
        Log.e(TAG, "NFC service dead - attempting to recover", e);
        INfcAdapter service = getServiceInterface();
        if (service == null) {
            Log.e(TAG, "could not retrieve NFC service during service recovery");
            return;
        }
        sService = service;
        try {
            sTagService = service.getNfcTagInterface();
            try {
                sCardEmulationService = service.getNfcCardEmulationInterface();
            } catch (RemoteException e2) {
                Log.e(TAG, "could not retrieve NFC card emulation service during service recovery");
            }
            try {
                sNfcFCardEmulationService = service.getNfcFCardEmulationInterface();
            } catch (RemoteException e3) {
                Log.e(TAG, "could not retrieve NFC-F card emulation service during service recovery");
            }
        } catch (RemoteException e4) {
            Log.e(TAG, "could not retrieve NFC tag service during service recovery");
        }
    }

    public boolean isEnabled() {
        boolean z = false;
        try {
            if (sService.getState() == 3) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public int getAdapterState() {
        try {
            return sService.getState();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return 1;
        }
    }

    public boolean enable() {
        try {
            return sService.enable();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean disable() {
        try {
            return sService.disable(true);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean disable(boolean persist) {
        try {
            return sService.disable(persist);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public void pausePolling(int timeoutInMs) {
        try {
            sService.pausePolling(timeoutInMs);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void resumePolling() {
        try {
            sService.resumePolling();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setBeamPushUris(Uri[] uris, Activity activity) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        if (activity == null) {
            throw new NullPointerException("activity cannot be null");
        }
        if (uris != null) {
            for (Uri uri : uris) {
                if (uri == null) {
                    throw new NullPointerException("Uri not allowed to be null");
                }
                String scheme = uri.getScheme();
                if (scheme == null || !(scheme.equalsIgnoreCase("file") || scheme.equalsIgnoreCase("content"))) {
                    throw new IllegalArgumentException("URI needs to have either scheme file or scheme content");
                }
            }
        }
        this.mNfcActivityManager.setNdefPushContentUri(activity, uris);
    }

    public void setBeamPushUrisCallback(CreateBeamUrisCallback callback, Activity activity) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        if (activity == null) {
            throw new NullPointerException("activity cannot be null");
        }
        this.mNfcActivityManager.setNdefPushContentUriCallback(activity, callback);
    }

    public void setNdefPushMessage(NdefMessage message, Activity activity, Activity... activities) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        int targetSdkVersion = getSdkVersion();
        if (activity == null) {
            try {
                throw new NullPointerException("activity cannot be null");
            } catch (IllegalStateException e) {
                if (targetSdkVersion < 16) {
                    Log.e(TAG, "Cannot call API with Activity that has already been destroyed", e);
                    return;
                }
                throw e;
            }
        }
        this.mNfcActivityManager.setNdefPushMessage(activity, message, 0);
        for (Activity a : activities) {
            if (a == null) {
                throw new NullPointerException("activities cannot contain null");
            }
            this.mNfcActivityManager.setNdefPushMessage(a, message, 0);
        }
    }

    public void setNdefPushMessage(NdefMessage message, Activity activity, int flags) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        if (activity == null) {
            throw new NullPointerException("activity cannot be null");
        }
        this.mNfcActivityManager.setNdefPushMessage(activity, message, flags);
    }

    public void setNdefPushMessageCallback(CreateNdefMessageCallback callback, Activity activity, Activity... activities) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        int targetSdkVersion = getSdkVersion();
        if (activity == null) {
            try {
                throw new NullPointerException("activity cannot be null");
            } catch (IllegalStateException e) {
                if (targetSdkVersion < 16) {
                    Log.e(TAG, "Cannot call API with Activity that has already been destroyed", e);
                    return;
                }
                throw e;
            }
        }
        this.mNfcActivityManager.setNdefPushMessageCallback(activity, callback, 0);
        for (Activity a : activities) {
            if (a == null) {
                throw new NullPointerException("activities cannot contain null");
            }
            this.mNfcActivityManager.setNdefPushMessageCallback(a, callback, 0);
        }
    }

    public void setNdefPushMessageCallback(CreateNdefMessageCallback callback, Activity activity, int flags) {
        if (activity == null) {
            throw new NullPointerException("activity cannot be null");
        }
        this.mNfcActivityManager.setNdefPushMessageCallback(activity, callback, flags);
    }

    public void setOnNdefPushCompleteCallback(OnNdefPushCompleteCallback callback, Activity activity, Activity... activities) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        int targetSdkVersion = getSdkVersion();
        if (activity == null) {
            try {
                throw new NullPointerException("activity cannot be null");
            } catch (IllegalStateException e) {
                if (targetSdkVersion < 16) {
                    Log.e(TAG, "Cannot call API with Activity that has already been destroyed", e);
                    return;
                }
                throw e;
            }
        }
        this.mNfcActivityManager.setOnNdefPushCompleteCallback(activity, callback);
        for (Activity a : activities) {
            if (a == null) {
                throw new NullPointerException("activities cannot contain null");
            }
            this.mNfcActivityManager.setOnNdefPushCompleteCallback(a, callback);
        }
    }

    public void enableForegroundDispatch(Activity activity, PendingIntent intent, IntentFilter[] filters, String[][] techLists) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        if (activity == null || intent == null) {
            throw new NullPointerException();
        } else if (activity.isResumed()) {
            TechListParcel parcel = null;
            if (techLists != null) {
                try {
                    if (techLists.length > 0) {
                        parcel = new TechListParcel(techLists);
                    }
                } catch (RemoteException e) {
                    attemptDeadServiceRecovery(e);
                    return;
                }
            }
            ActivityThread.currentActivityThread().registerOnActivityPausedListener(activity, this.mForegroundDispatchListener);
            sService.setForegroundDispatch(intent, filters, parcel);
        } else {
            throw new IllegalStateException("Foreground dispatch can only be enabled when your activity is resumed");
        }
    }

    public void disableForegroundDispatch(Activity activity) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        ActivityThread.currentActivityThread().unregisterOnActivityPausedListener(activity, this.mForegroundDispatchListener);
        disableForegroundDispatchInternal(activity, false);
    }

    void disableForegroundDispatchInternal(Activity activity, boolean force) {
        try {
            sService.setForegroundDispatch(null, null, null);
            if (!force && !activity.isResumed()) {
                throw new IllegalStateException("You must disable foreground dispatching while your activity is still resumed");
            }
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void enableReaderMode(Activity activity, ReaderCallback callback, int flags, Bundle extras) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        this.mNfcActivityManager.enableReaderMode(activity, callback, flags, extras);
    }

    public void disableReaderMode(Activity activity) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        this.mNfcActivityManager.disableReaderMode(activity);
    }

    public boolean invokeBeam(Activity activity) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        if (activity == null) {
            throw new NullPointerException("activity may not be null.");
        }
        enforceResumed(activity);
        try {
            sService.invokeBeam();
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "invokeBeam: NFC process has died.");
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean invokeBeam(BeamShareData shareData) {
        try {
            Log.e(TAG, "invokeBeamInternal()");
            sService.invokeBeamInternal(shareData);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "invokeBeam: NFC process has died.");
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    @Deprecated
    public void enableForegroundNdefPush(Activity activity, NdefMessage message) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        if (activity == null || message == null) {
            throw new NullPointerException();
        }
        enforceResumed(activity);
        this.mNfcActivityManager.setNdefPushMessage(activity, message, 0);
    }

    @Deprecated
    public void disableForegroundNdefPush(Activity activity) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        if (activity == null) {
            throw new NullPointerException();
        }
        enforceResumed(activity);
        this.mNfcActivityManager.setNdefPushMessage(activity, null, 0);
        this.mNfcActivityManager.setNdefPushMessageCallback(activity, null, 0);
        this.mNfcActivityManager.setOnNdefPushCompleteCallback(activity, null);
    }

    public boolean enableNdefPush() {
        if (sHasNfcFeature) {
            try {
                return sService.enableNdefPush();
            } catch (RemoteException e) {
                attemptDeadServiceRecovery(e);
                return false;
            }
        }
        throw new UnsupportedOperationException();
    }

    public boolean disableNdefPush() {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        try {
            return sService.disableNdefPush();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean isNdefPushEnabled() {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        try {
            return sService.isNdefPushEnabled();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public boolean ignore(android.nfc.Tag r5, int r6, android.nfc.NfcAdapter.OnTagRemovedListener r7, android.os.Handler r8) {
        /*
        r4 = this;
        r1 = 0;
        if (r7 == 0) goto L_0x0008;
    L_0x0003:
        r1 = new android.nfc.NfcAdapter$2;
        r1.<init>(r4, r8, r7);
    L_0x0008:
        r2 = r4.mLock;
        monitor-enter(r2);
        r4.mTagRemovedListener = r1;	 Catch:{ all -> 0x0019 }
        monitor-exit(r2);
        r2 = sService;	 Catch:{ RemoteException -> 0x001c }
        r3 = r5.getServiceHandle();	 Catch:{ RemoteException -> 0x001c }
        r2 = r2.ignore(r3, r6, r1);	 Catch:{ RemoteException -> 0x001c }
        return r2;
    L_0x0019:
        r3 = move-exception;
        monitor-exit(r2);
        throw r3;
    L_0x001c:
        r0 = move-exception;
        r2 = 0;
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.ignore(android.nfc.Tag, int, android.nfc.NfcAdapter$OnTagRemovedListener, android.os.Handler):boolean");
    }

    public void dispatch(Tag tag) {
        if (tag == null) {
            throw new NullPointerException("tag cannot be null");
        }
        try {
            sService.dispatch(tag);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setP2pModes(int initiatorModes, int targetModes) {
        try {
            sService.setP2pModes(initiatorModes, targetModes);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public boolean addNfcUnlockHandler(android.nfc.NfcAdapter.NfcUnlockHandler r8, java.lang.String[] r9) {
        /*
        r7 = this;
        r6 = 0;
        r4 = android.nfc.NfcAdapter.class;
        monitor-enter(r4);
        r3 = sHasNfcFeature;	 Catch:{ all -> 0x000e }
        if (r3 != 0) goto L_0x0011;	 Catch:{ all -> 0x000e }
    L_0x0008:
        r3 = new java.lang.UnsupportedOperationException;	 Catch:{ all -> 0x000e }
        r3.<init>();	 Catch:{ all -> 0x000e }
        throw r3;	 Catch:{ all -> 0x000e }
    L_0x000e:
        r3 = move-exception;
        monitor-exit(r4);
        throw r3;
    L_0x0011:
        monitor-exit(r4);
        r3 = r9.length;
        if (r3 != 0) goto L_0x0016;
    L_0x0015:
        return r6;
    L_0x0016:
        r4 = r7.mLock;	 Catch:{ RemoteException -> 0x004c, IllegalArgumentException -> 0x0051 }
        monitor-enter(r4);	 Catch:{ RemoteException -> 0x004c, IllegalArgumentException -> 0x0051 }
        r3 = r7.mNfcUnlockHandlers;	 Catch:{ all -> 0x0049 }
        r3 = r3.containsKey(r8);	 Catch:{ all -> 0x0049 }
        if (r3 == 0) goto L_0x0033;	 Catch:{ all -> 0x0049 }
    L_0x0021:
        r5 = sService;	 Catch:{ all -> 0x0049 }
        r3 = r7.mNfcUnlockHandlers;	 Catch:{ all -> 0x0049 }
        r3 = r3.get(r8);	 Catch:{ all -> 0x0049 }
        r3 = (android.nfc.INfcUnlockHandler) r3;	 Catch:{ all -> 0x0049 }
        r5.removeNfcUnlockHandler(r3);	 Catch:{ all -> 0x0049 }
        r3 = r7.mNfcUnlockHandlers;	 Catch:{ all -> 0x0049 }
        r3.remove(r8);	 Catch:{ all -> 0x0049 }
    L_0x0033:
        r2 = new android.nfc.NfcAdapter$3;	 Catch:{ all -> 0x0049 }
        r2.<init>(r7, r8);	 Catch:{ all -> 0x0049 }
        r3 = sService;	 Catch:{ all -> 0x0049 }
        r5 = android.nfc.Tag.getTechCodesFromStrings(r9);	 Catch:{ all -> 0x0049 }
        r3.addNfcUnlockHandler(r2, r5);	 Catch:{ all -> 0x0049 }
        r3 = r7.mNfcUnlockHandlers;	 Catch:{ all -> 0x0049 }
        r3.put(r8, r2);	 Catch:{ all -> 0x0049 }
        monitor-exit(r4);	 Catch:{ RemoteException -> 0x004c, IllegalArgumentException -> 0x0051 }
        r3 = 1;	 Catch:{ RemoteException -> 0x004c, IllegalArgumentException -> 0x0051 }
        return r3;	 Catch:{ RemoteException -> 0x004c, IllegalArgumentException -> 0x0051 }
    L_0x0049:
        r3 = move-exception;	 Catch:{ RemoteException -> 0x004c, IllegalArgumentException -> 0x0051 }
        monitor-exit(r4);	 Catch:{ RemoteException -> 0x004c, IllegalArgumentException -> 0x0051 }
        throw r3;	 Catch:{ RemoteException -> 0x004c, IllegalArgumentException -> 0x0051 }
    L_0x004c:
        r0 = move-exception;
        r7.attemptDeadServiceRecovery(r0);
        return r6;
    L_0x0051:
        r1 = move-exception;
        r3 = "NFC";
        r4 = "Unable to register LockscreenDispatch";
        android.util.Log.e(r3, r4, r1);
        return r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.addNfcUnlockHandler(android.nfc.NfcAdapter$NfcUnlockHandler, java.lang.String[]):boolean");
    }

    public boolean removeNfcUnlockHandler(NfcUnlockHandler unlockHandler) {
        synchronized (NfcAdapter.class) {
            if (sHasNfcFeature) {
            } else {
                throw new UnsupportedOperationException();
            }
        }
        try {
            synchronized (this.mLock) {
                if (this.mNfcUnlockHandlers.containsKey(unlockHandler)) {
                    sService.removeNfcUnlockHandler((INfcUnlockHandler) this.mNfcUnlockHandlers.remove(unlockHandler));
                }
            }
            return true;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public int getModeFlag(int mode) {
        try {
            return sService.getModeFlag(mode);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return -1;
        }
    }

    public void setModeFlag(int mode, int flag) {
        try {
            sService.setModeFlag(mode, flag);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public INfcAdapterGsmaExtras getNfcAdapterGsmaExtrasInterface() {
        if (this.mContext == null) {
            throw new UnsupportedOperationException("You need a context on NfcAdapter to use the  NFC gsma extras APIs");
        }
        try {
            return sService.getNfcAdapterGsmaExtrasInterface();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public INfcAdapterExtras getNfcAdapterExtrasInterface() {
        if (this.mContext == null) {
            throw new UnsupportedOperationException("You need a context on NfcAdapter to use the  NFC extras APIs");
        }
        try {
            return sService.getNfcAdapterExtrasInterface(this.mContext.getPackageName());
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    void enforceResumed(Activity activity) {
        if (!activity.isResumed()) {
            throw new IllegalStateException("API cannot be called while activity is paused");
        }
    }

    int getSdkVersion() {
        if (this.mContext == null) {
            return 9;
        }
        return this.mContext.getApplicationInfo().targetSdkVersion;
    }
}
