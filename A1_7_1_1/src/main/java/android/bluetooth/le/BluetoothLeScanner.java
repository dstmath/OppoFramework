package android.bluetooth.le;

import android.app.ActivityThread;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCallbackWrapper;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.le.ScanSettings.Builder;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
public final class BluetoothLeScanner {
    private static final boolean DBG = true;
    private static final String TAG = "BluetoothLeScanner";
    private static final boolean VDBG = false;
    private BluetoothAdapter mBluetoothAdapter;
    private final IBluetoothManager mBluetoothManager;
    private final Handler mHandler;
    private final Map<ScanCallback, BleScanCallbackWrapper> mLeScanClients;

    /* renamed from: android.bluetooth.le.BluetoothLeScanner$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ BluetoothLeScanner this$0;
        final /* synthetic */ ScanCallback val$callback;
        final /* synthetic */ int val$errorCode;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeScanner.1.<init>(android.bluetooth.le.BluetoothLeScanner, int, android.bluetooth.le.ScanCallback):void, dex: 
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
        AnonymousClass1(android.bluetooth.le.BluetoothLeScanner r1, int r2, android.bluetooth.le.ScanCallback r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeScanner.1.<init>(android.bluetooth.le.BluetoothLeScanner, int, android.bluetooth.le.ScanCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.1.<init>(android.bluetooth.le.BluetoothLeScanner, int, android.bluetooth.le.ScanCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.le.BluetoothLeScanner.1.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.le.BluetoothLeScanner.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.1.run():void");
        }
    }

    private class BleScanCallbackWrapper extends BluetoothGattCallbackWrapper {
        private static final int REGISTRATION_CALLBACK_TIMEOUT_MILLIS = 2000;
        private IBluetoothGatt mBluetoothGatt;
        private int mClientIf;
        private final List<ScanFilter> mFilters;
        private List<List<ResultStorageDescriptor>> mResultStorages;
        private final ScanCallback mScanCallback;
        private ScanSettings mSettings;
        private final WorkSource mWorkSource;
        final /* synthetic */ BluetoothLeScanner this$0;

        /* renamed from: android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ BleScanCallbackWrapper this$1;
            final /* synthetic */ List val$results;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.2.<init>(android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper, java.util.List):void, dex: 
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
            AnonymousClass2(android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper r1, java.util.List r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.2.<init>(android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper, java.util.List):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.2.<init>(android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper, java.util.List):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.2.run():void, dex: 
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
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.2.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.2.run():void");
            }
        }

        /* renamed from: android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper$3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ BleScanCallbackWrapper this$1;
            final /* synthetic */ boolean val$onFound;
            final /* synthetic */ ScanResult val$scanResult;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.3.<init>(android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper, boolean, android.bluetooth.le.ScanResult):void, dex: 
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
            AnonymousClass3(android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper r1, boolean r2, android.bluetooth.le.ScanResult r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.3.<init>(android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper, boolean, android.bluetooth.le.ScanResult):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.3.<init>(android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper, boolean, android.bluetooth.le.ScanResult):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.3.run():void, dex: 
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
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.3.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.3.run():void");
            }
        }

        public BleScanCallbackWrapper(BluetoothLeScanner this$0, IBluetoothGatt bluetoothGatt, List<ScanFilter> filters, ScanSettings settings, WorkSource workSource, ScanCallback scanCallback, List<List<ResultStorageDescriptor>> resultStorages) {
            this.this$0 = this$0;
            this.mBluetoothGatt = bluetoothGatt;
            this.mFilters = filters;
            this.mSettings = settings;
            this.mWorkSource = workSource;
            this.mScanCallback = scanCallback;
            this.mClientIf = 0;
            this.mResultStorages = resultStorages;
        }

        /* JADX WARNING: Removed duplicated region for block: B:15:0x004c A:{ExcHandler: java.lang.InterruptedException (r0_0 'e' java.lang.Exception), Splitter: B:7:0x0008} */
        /* JADX WARNING: Missing block: B:14:0x004b, code:
            return;
     */
        /* JADX WARNING: Missing block: B:15:0x004c, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:17:?, code:
            android.util.Log.e(android.bluetooth.le.BluetoothLeScanner.TAG, "application registeration exception", r0);
            android.bluetooth.le.BluetoothLeScanner.-wrap0(r5.this$0, r5.mScanCallback, 3);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void startRegisteration() {
            synchronized (this) {
                if (this.mClientIf == -1) {
                    return;
                }
                try {
                    this.mBluetoothGatt.registerClient(new ParcelUuid(UUID.randomUUID()), this);
                    wait(2000);
                } catch (Exception e) {
                }
                if (this.mClientIf > 0) {
                    this.this$0.mLeScanClients.put(this.mScanCallback, this);
                    Log.i(BluetoothLeScanner.TAG, "startRegisteration: mLeScanClients=" + this.this$0.mLeScanClients);
                } else {
                    if (this.mClientIf == 0) {
                        this.mClientIf = -1;
                    }
                    this.this$0.postCallbackError(this.mScanCallback, 2);
                }
            }
        }

        public void stopLeScan() {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mClientIf);
                    return;
                }
                try {
                    this.mBluetoothGatt.stopScan(this.mClientIf, false);
                    this.mBluetoothGatt.unregisterClient(this.mClientIf);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to stop scan and unregister", e);
                }
                this.mClientIf = -1;
                return;
            }
        }

        void flushPendingBatchResults() {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mClientIf);
                    return;
                }
                try {
                    this.mBluetoothGatt.flushPendingBatchResults(this.mClientIf, false);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to get pending scan results", e);
                }
            }
        }

        public void onClientRegistered(int status, int clientIf) {
            Log.d(BluetoothLeScanner.TAG, "onClientRegistered() - status=" + status + " clientIf=" + clientIf);
            synchronized (this) {
                Log.d(BluetoothLeScanner.TAG, "mClientIf=" + this.mClientIf);
                if (status == 0) {
                    try {
                        if (this.mClientIf == -1) {
                            this.mBluetoothGatt.unregisterClient(clientIf);
                        } else {
                            this.mClientIf = clientIf;
                            this.mBluetoothGatt.startScan(this.mClientIf, false, this.mSettings, this.mFilters, this.mWorkSource, this.mResultStorages, ActivityThread.currentOpPackageName());
                        }
                    } catch (RemoteException e) {
                        Log.e(BluetoothLeScanner.TAG, "fail to start le scan: " + e);
                        this.mClientIf = -1;
                    }
                } else {
                    this.mClientIf = -1;
                }
                notifyAll();
            }
            return;
        }

        public void onScanResult(final ScanResult scanResult) {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable(this) {
                    final /* synthetic */ BleScanCallbackWrapper this$1;

                    public void run() {
                        this.this$1.mScanCallback.onScanResult(1, scanResult);
                    }
                });
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
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public void onBatchScanResults(java.util.List<android.bluetooth.le.ScanResult> r3) {
            /*
            r2 = this;
            r0 = new android.os.Handler;
            r1 = android.os.Looper.getMainLooper();
            r0.<init>(r1);
            r1 = new android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper$2;
            r1.<init>(r2, r3);
            r0.post(r1);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.onBatchScanResults(java.util.List):void");
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
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public void onFoundOrLost(boolean r3, android.bluetooth.le.ScanResult r4) {
            /*
            r2 = this;
            monitor-enter(r2);
            r1 = r2.mClientIf;	 Catch:{ all -> 0x001a }
            if (r1 > 0) goto L_0x0007;
        L_0x0005:
            monitor-exit(r2);
            return;
        L_0x0007:
            monitor-exit(r2);
            r0 = new android.os.Handler;
            r1 = android.os.Looper.getMainLooper();
            r0.<init>(r1);
            r1 = new android.bluetooth.le.BluetoothLeScanner$BleScanCallbackWrapper$3;
            r1.<init>(r2, r3, r4);
            r0.post(r1);
            return;
        L_0x001a:
            r1 = move-exception;
            monitor-exit(r2);
            throw r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.onFoundOrLost(boolean, android.bluetooth.le.ScanResult):void");
        }

        public void onScanManagerErrorCallback(int errorCode) {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    return;
                }
                this.this$0.postCallbackError(this.mScanCallback, errorCode);
            }
        }
    }

    public BluetoothLeScanner(IBluetoothManager bluetoothManager) {
        this.mBluetoothManager = bluetoothManager;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mLeScanClients = new HashMap();
    }

    public void startScan(ScanCallback callback) {
        startScan(null, new Builder().build(), callback);
    }

    public void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
        startScan(filters, settings, null, callback, null);
    }

    public void startScanFromSource(WorkSource workSource, ScanCallback callback) {
        startScanFromSource(null, new Builder().build(), workSource, callback);
    }

    public void startScanFromSource(List<ScanFilter> filters, ScanSettings settings, WorkSource workSource, ScanCallback callback) {
        startScan(filters, settings, workSource, callback, null);
    }

    private void startScan(List<ScanFilter> filters, ScanSettings settings, WorkSource workSource, ScanCallback callback, List<List<ResultStorageDescriptor>> resultStorages) {
        Log.d(TAG, "startScan");
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        } else if (settings == null) {
            throw new IllegalArgumentException("settings is null");
        } else {
            synchronized (this.mLeScanClients) {
                if (this.mLeScanClients.containsKey(callback)) {
                    postCallbackError(callback, 1);
                    return;
                }
                IBluetoothGatt gatt;
                try {
                    gatt = this.mBluetoothManager.getBluetoothGatt();
                } catch (RemoteException e) {
                    gatt = null;
                }
                if (gatt == null) {
                    postCallbackError(callback, 3);
                } else if (!isSettingsConfigAllowedForScan(settings)) {
                    postCallbackError(callback, 4);
                } else if (!isHardwareResourcesAvailableForScan(settings)) {
                    postCallbackError(callback, 5);
                } else if (isSettingsAndFilterComboAllowed(settings, filters)) {
                    new BleScanCallbackWrapper(this, gatt, filters, settings, workSource, callback, resultStorages).startRegisteration();
                } else {
                    postCallbackError(callback, 4);
                }
            }
        }
    }

    public void stopScan(ScanCallback callback) {
        Log.d(TAG, "stopScan");
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        synchronized (this.mLeScanClients) {
            Log.i(TAG, "startRegisteration: mLeScanClients=" + this.mLeScanClients + " ,callback=" + callback);
            BleScanCallbackWrapper wrapper = (BleScanCallbackWrapper) this.mLeScanClients.remove(callback);
            if (wrapper == null) {
                Log.d(TAG, "could not find callback wrapper");
                return;
            }
            wrapper.stopLeScan();
        }
    }

    public void flushPendingScanResults(ScanCallback callback) {
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null!");
        }
        synchronized (this.mLeScanClients) {
            BleScanCallbackWrapper wrapper = (BleScanCallbackWrapper) this.mLeScanClients.get(callback);
            if (wrapper == null) {
                return;
            }
            wrapper.flushPendingBatchResults();
        }
    }

    public void startTruncatedScan(List<TruncatedFilter> truncatedFilters, ScanSettings settings, ScanCallback callback) {
        int filterSize = truncatedFilters.size();
        List<ScanFilter> scanFilters = new ArrayList(filterSize);
        List<List<ResultStorageDescriptor>> scanStorages = new ArrayList(filterSize);
        for (TruncatedFilter filter : truncatedFilters) {
            scanFilters.add(filter.getFilter());
            scanStorages.add(filter.getStorageDescriptors());
        }
        startScan(scanFilters, settings, null, callback, scanStorages);
    }

    public void cleanup() {
        Log.d(TAG, "cleanup");
        this.mLeScanClients.clear();
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
    private void postCallbackError(android.bluetooth.le.ScanCallback r3, int r4) {
        /*
        r2 = this;
        r0 = r2.mHandler;
        r1 = new android.bluetooth.le.BluetoothLeScanner$1;
        r1.<init>(r2, r4, r3);
        r0.post(r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.postCallbackError(android.bluetooth.le.ScanCallback, int):void");
    }

    private boolean isSettingsConfigAllowedForScan(ScanSettings settings) {
        if (this.mBluetoothAdapter.isOffloadedFilteringSupported()) {
            return true;
        }
        if (settings.getCallbackType() == 1 && settings.getReportDelayMillis() == 0) {
            return true;
        }
        return false;
    }

    private boolean isSettingsAndFilterComboAllowed(ScanSettings settings, List<ScanFilter> filterList) {
        if ((settings.getCallbackType() & 6) != 0) {
            if (filterList == null) {
                return false;
            }
            for (ScanFilter filter : filterList) {
                if (filter.isAllFieldsEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isHardwareResourcesAvailableForScan(ScanSettings settings) {
        boolean z = false;
        int callbackType = settings.getCallbackType();
        if ((callbackType & 2) == 0 && (callbackType & 4) == 0) {
            return true;
        }
        if (this.mBluetoothAdapter.isOffloadedFilteringSupported()) {
            z = this.mBluetoothAdapter.isHardwareTrackingFiltersAvailable();
        }
        return z;
    }
}
