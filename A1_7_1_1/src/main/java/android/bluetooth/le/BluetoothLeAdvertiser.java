package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCallbackWrapper;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
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
public final class BluetoothLeAdvertiser {
    private static final int FLAGS_FIELD_BYTES = 3;
    private static final int MANUFACTURER_SPECIFIC_DATA_LENGTH = 2;
    private static final int MAX_ADVERTISING_DATA_BYTES = 31;
    private static final int OVERHEAD_BYTES_PER_FIELD = 2;
    private static final int SERVICE_DATA_UUID_LENGTH = 2;
    private static final String TAG = "BluetoothLeAdvertiser";
    private BluetoothAdapter mBluetoothAdapter;
    private final IBluetoothManager mBluetoothManager;
    private final Handler mHandler;
    private final Map<AdvertiseCallback, AdvertiseCallbackWrapper> mLeAdvertisers;

    /* renamed from: android.bluetooth.le.BluetoothLeAdvertiser$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ BluetoothLeAdvertiser this$0;
        final /* synthetic */ AdvertiseCallback val$callback;
        final /* synthetic */ int val$error;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeAdvertiser.1.<init>(android.bluetooth.le.BluetoothLeAdvertiser, int, android.bluetooth.le.AdvertiseCallback):void, dex: 
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
        AnonymousClass1(android.bluetooth.le.BluetoothLeAdvertiser r1, int r2, android.bluetooth.le.AdvertiseCallback r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeAdvertiser.1.<init>(android.bluetooth.le.BluetoothLeAdvertiser, int, android.bluetooth.le.AdvertiseCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.1.<init>(android.bluetooth.le.BluetoothLeAdvertiser, int, android.bluetooth.le.AdvertiseCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.bluetooth.le.BluetoothLeAdvertiser.1.run():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.bluetooth.le.BluetoothLeAdvertiser.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.1.run():void");
        }
    }

    /* renamed from: android.bluetooth.le.BluetoothLeAdvertiser$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ BluetoothLeAdvertiser this$0;
        final /* synthetic */ AdvertiseCallback val$callback;
        final /* synthetic */ AdvertiseSettings val$settings;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeAdvertiser.2.<init>(android.bluetooth.le.BluetoothLeAdvertiser, android.bluetooth.le.AdvertiseCallback, android.bluetooth.le.AdvertiseSettings):void, dex: 
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
        AnonymousClass2(android.bluetooth.le.BluetoothLeAdvertiser r1, android.bluetooth.le.AdvertiseCallback r2, android.bluetooth.le.AdvertiseSettings r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeAdvertiser.2.<init>(android.bluetooth.le.BluetoothLeAdvertiser, android.bluetooth.le.AdvertiseCallback, android.bluetooth.le.AdvertiseSettings):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.2.<init>(android.bluetooth.le.BluetoothLeAdvertiser, android.bluetooth.le.AdvertiseCallback, android.bluetooth.le.AdvertiseSettings):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.le.BluetoothLeAdvertiser.2.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.le.BluetoothLeAdvertiser.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.2.run():void");
        }
    }

    private class AdvertiseCallbackWrapper extends BluetoothGattCallbackWrapper {
        private static final int LE_CALLBACK_TIMEOUT_MILLIS = 2000;
        private final AdvertiseCallback mAdvertiseCallback;
        private final AdvertiseData mAdvertisement;
        private final IBluetoothGatt mBluetoothGatt;
        private int mClientIf;
        private boolean mIsAdvertising;
        private final AdvertiseData mScanResponse;
        private final AdvertiseSettings mSettings;
        final /* synthetic */ BluetoothLeAdvertiser this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.<init>(android.bluetooth.le.BluetoothLeAdvertiser, android.bluetooth.le.AdvertiseCallback, android.bluetooth.le.AdvertiseData, android.bluetooth.le.AdvertiseData, android.bluetooth.le.AdvertiseSettings, android.bluetooth.IBluetoothGatt):void, dex: 
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
        public AdvertiseCallbackWrapper(android.bluetooth.le.BluetoothLeAdvertiser r1, android.bluetooth.le.AdvertiseCallback r2, android.bluetooth.le.AdvertiseData r3, android.bluetooth.le.AdvertiseData r4, android.bluetooth.le.AdvertiseSettings r5, android.bluetooth.IBluetoothGatt r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.<init>(android.bluetooth.le.BluetoothLeAdvertiser, android.bluetooth.le.AdvertiseCallback, android.bluetooth.le.AdvertiseData, android.bluetooth.le.AdvertiseData, android.bluetooth.le.AdvertiseSettings, android.bluetooth.IBluetoothGatt):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.<init>(android.bluetooth.le.BluetoothLeAdvertiser, android.bluetooth.le.AdvertiseCallback, android.bluetooth.le.AdvertiseData, android.bluetooth.le.AdvertiseData, android.bluetooth.le.AdvertiseSettings, android.bluetooth.IBluetoothGatt):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.onClientRegistered(int, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onClientRegistered(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.onClientRegistered(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.onClientRegistered(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.onMultiAdvertiseCallback(int, boolean, android.bluetooth.le.AdvertiseSettings):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onMultiAdvertiseCallback(int r1, boolean r2, android.bluetooth.le.AdvertiseSettings r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.onMultiAdvertiseCallback(int, boolean, android.bluetooth.le.AdvertiseSettings):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.onMultiAdvertiseCallback(int, boolean, android.bluetooth.le.AdvertiseSettings):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.startRegisteration():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void startRegisteration() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.startRegisteration():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.startRegisteration():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.stopAdvertising():void, dex: 
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
        public void stopAdvertising() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.stopAdvertising():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.stopAdvertising():void");
        }
    }

    public BluetoothLeAdvertiser(IBluetoothManager bluetoothManager) {
        this.mLeAdvertisers = new HashMap();
        this.mBluetoothManager = bluetoothManager;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void startAdvertising(AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseCallback callback) {
        startAdvertising(settings, advertiseData, null, callback);
    }

    public void startAdvertising(AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseData scanResponse, AdvertiseCallback callback) {
        Log.d(TAG, "startAdvertising");
        synchronized (this.mLeAdvertisers) {
            BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            } else if (!this.mBluetoothAdapter.isMultipleAdvertisementSupported() && !this.mBluetoothAdapter.isPeripheralModeSupported()) {
                postStartFailure(callback, 5);
            } else if (totalBytes(advertiseData, settings.isConnectable()) > 31 || totalBytes(scanResponse, false) > 31) {
                postStartFailure(callback, 1);
            } else if (this.mLeAdvertisers.containsKey(callback)) {
                postStartFailure(callback, 3);
            } else {
                try {
                    new AdvertiseCallbackWrapper(this, callback, advertiseData, scanResponse, settings, this.mBluetoothManager.getBluetoothGatt()).startRegisteration();
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to get Bluetooth gatt - ", e);
                    postStartFailure(callback, 4);
                }
            }
        }
    }

    public void stopAdvertising(AdvertiseCallback callback) {
        Log.d(TAG, "stopAdvertising");
        synchronized (this.mLeAdvertisers) {
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            }
            AdvertiseCallbackWrapper wrapper = (AdvertiseCallbackWrapper) this.mLeAdvertisers.get(callback);
            if (wrapper == null) {
                return;
            }
            wrapper.stopAdvertising();
        }
    }

    public void cleanup() {
        Log.d(TAG, "cleanup");
        this.mLeAdvertisers.clear();
    }

    private int totalBytes(AdvertiseData data, boolean isFlagsIncluded) {
        if (data == null) {
            return 0;
        }
        int size = isFlagsIncluded ? 3 : 0;
        if (data.getServiceUuids() != null) {
            int num16BitUuids = 0;
            int num32BitUuids = 0;
            int num128BitUuids = 0;
            for (ParcelUuid uuid : data.getServiceUuids()) {
                if (BluetoothUuid.is16BitUuid(uuid)) {
                    num16BitUuids++;
                } else if (BluetoothUuid.is32BitUuid(uuid)) {
                    num32BitUuids++;
                } else {
                    num128BitUuids++;
                }
            }
            if (num16BitUuids != 0) {
                size += (num16BitUuids * 2) + 2;
            }
            if (num32BitUuids != 0) {
                size += (num32BitUuids * 4) + 2;
            }
            if (num128BitUuids != 0) {
                size += (num128BitUuids * 16) + 2;
            }
        }
        for (ParcelUuid uuid2 : data.getServiceData().keySet()) {
            size += byteLength((byte[]) data.getServiceData().get(uuid2)) + 4;
        }
        for (int i = 0; i < data.getManufacturerSpecificData().size(); i++) {
            size += byteLength((byte[]) data.getManufacturerSpecificData().valueAt(i)) + 4;
        }
        if (data.getIncludeTxPowerLevel()) {
            size += 3;
        }
        if (data.getIncludeDeviceName() && this.mBluetoothAdapter.getName() != null) {
            size += this.mBluetoothAdapter.getName().length() + 2;
        }
        return size;
    }

    private int byteLength(byte[] array) {
        return array == null ? 0 : array.length;
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void postStartFailure(android.bluetooth.le.AdvertiseCallback r3, int r4) {
        /*
        r2 = this;
        r0 = r2.mHandler;
        r1 = new android.bluetooth.le.BluetoothLeAdvertiser$1;
        r1.<init>(r2, r4, r3);
        r0.post(r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.postStartFailure(android.bluetooth.le.AdvertiseCallback, int):void");
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void postStartSuccess(android.bluetooth.le.AdvertiseCallback r3, android.bluetooth.le.AdvertiseSettings r4) {
        /*
        r2 = this;
        r0 = r2.mHandler;
        r1 = new android.bluetooth.le.BluetoothLeAdvertiser$2;
        r1.<init>(r2, r3, r4);
        r0.post(r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.postStartSuccess(android.bluetooth.le.AdvertiseCallback, android.bluetooth.le.AdvertiseSettings):void");
    }
}
