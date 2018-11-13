package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import java.io.FileDescriptor;
import java.io.PrintWriter;
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class CarrierServiceBindHelper {
    private static final int EVENT_REBIND = 0;
    private static final String LOG_TAG = "CarrierSvcBindHelper";
    private AppBinding[] mBindings;
    private Context mContext;
    private Handler mHandler;
    private String[] mLastSimState;
    private final PackageMonitor mPackageMonitor;
    private BroadcastReceiver mUserUnlockedReceiver;

    private class AppBinding {
        private int bindCount;
        private String carrierPackage;
        private String carrierServiceClass;
        private CarrierServiceConnection connection;
        private long lastBindStartMillis;
        private long lastUnbindMillis;
        private int phoneId;
        private int unbindCount;

        public AppBinding(int phoneId) {
            this.phoneId = phoneId;
        }

        public int getPhoneId() {
            return this.phoneId;
        }

        public String getPackage() {
            return this.carrierPackage;
        }

        void rebind() {
            List<String> carrierPackageNames = TelephonyManager.from(CarrierServiceBindHelper.this.mContext).getCarrierPackageNamesForIntentAndPhone(new Intent("android.service.carrier.CarrierService"), this.phoneId);
            if (carrierPackageNames == null || carrierPackageNames.size() <= 0) {
                CarrierServiceBindHelper.log("No carrier app for: " + this.phoneId);
                unbind();
                return;
            }
            CarrierServiceBindHelper.log("Found carrier app: " + carrierPackageNames);
            String candidateCarrierPackage = (String) carrierPackageNames.get(0);
            if (!TextUtils.equals(this.carrierPackage, candidateCarrierPackage)) {
                unbind();
            }
            Intent carrierService = new Intent("android.service.carrier.CarrierService");
            carrierService.setPackage(candidateCarrierPackage);
            ResolveInfo carrierResolveInfo = CarrierServiceBindHelper.this.mContext.getPackageManager().resolveService(carrierService, 128);
            Bundle metadata = null;
            Object candidateServiceClass = null;
            if (carrierResolveInfo != null) {
                metadata = carrierResolveInfo.serviceInfo.metaData;
                candidateServiceClass = carrierResolveInfo.getComponentInfo().getComponentName().getClassName();
            }
            if (metadata == null || !metadata.getBoolean("android.service.carrier.LONG_LIVED_BINDING", false)) {
                CarrierServiceBindHelper.log("Carrier app does not want a long lived binding");
                unbind();
                return;
            }
            if (!TextUtils.equals(this.carrierServiceClass, candidateServiceClass)) {
                unbind();
            } else if (this.connection != null) {
                return;
            }
            this.carrierPackage = candidateCarrierPackage;
            this.carrierServiceClass = candidateServiceClass;
            CarrierServiceBindHelper.log("Binding to " + this.carrierPackage + " for phone " + this.phoneId);
            this.bindCount++;
            this.lastBindStartMillis = System.currentTimeMillis();
            this.connection = new CarrierServiceConnection(CarrierServiceBindHelper.this, null);
            String error;
            try {
                if (!CarrierServiceBindHelper.this.mContext.bindService(carrierService, this.connection, 67108865)) {
                    error = "bindService returned false";
                    CarrierServiceBindHelper.log("Unable to bind to " + this.carrierPackage + " for phone " + this.phoneId + ". Error: " + error);
                    unbind();
                }
            } catch (SecurityException ex) {
                error = ex.getMessage();
            }
        }

        void unbind() {
            if (this.connection != null) {
                this.unbindCount++;
                this.lastUnbindMillis = System.currentTimeMillis();
                this.carrierPackage = null;
                this.carrierServiceClass = null;
                CarrierServiceBindHelper.log("Unbinding from carrier app");
                CarrierServiceBindHelper.this.mContext.unbindService(this.connection);
                this.connection = null;
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("Carrier app binding for phone " + this.phoneId);
            pw.println("  connection: " + this.connection);
            pw.println("  bindCount: " + this.bindCount);
            pw.println("  lastBindStartMillis: " + this.lastBindStartMillis);
            pw.println("  unbindCount: " + this.unbindCount);
            pw.println("  lastUnbindMillis: " + this.lastUnbindMillis);
            pw.println();
        }
    }

    private class CarrierServiceConnection implements ServiceConnection {
        private boolean connected;
        final /* synthetic */ CarrierServiceBindHelper this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.<init>(com.android.internal.telephony.CarrierServiceBindHelper):void, dex: 
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
        private CarrierServiceConnection(com.android.internal.telephony.CarrierServiceBindHelper r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.<init>(com.android.internal.telephony.CarrierServiceBindHelper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.<init>(com.android.internal.telephony.CarrierServiceBindHelper):void");
        }

        /* synthetic */ CarrierServiceConnection(CarrierServiceBindHelper this$0, CarrierServiceConnection carrierServiceConnection) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.onServiceConnected(android.content.ComponentName, android.os.IBinder):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onServiceConnected(android.content.ComponentName r1, android.os.IBinder r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.onServiceConnected(android.content.ComponentName, android.os.IBinder):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.onServiceConnected(android.content.ComponentName, android.os.IBinder):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.onServiceDisconnected(android.content.ComponentName):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onServiceDisconnected(android.content.ComponentName r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.onServiceDisconnected(android.content.ComponentName):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.onServiceDisconnected(android.content.ComponentName):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CarrierServiceBindHelper.CarrierServiceConnection.toString():java.lang.String");
        }
    }

    private class CarrierServicePackageMonitor extends PackageMonitor {
        final /* synthetic */ CarrierServiceBindHelper this$0;

        /* synthetic */ CarrierServicePackageMonitor(CarrierServiceBindHelper this$0, CarrierServicePackageMonitor carrierServicePackageMonitor) {
            this(this$0);
        }

        private CarrierServicePackageMonitor(CarrierServiceBindHelper this$0) {
            this.this$0 = this$0;
        }

        public void onPackageAdded(String packageName, int reason) {
            evaluateBinding(packageName, true);
        }

        public void onPackageRemoved(String packageName, int reason) {
            evaluateBinding(packageName, true);
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            evaluateBinding(packageName, true);
        }

        public void onPackageModified(String packageName) {
            evaluateBinding(packageName, false);
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            if (doit) {
                for (String packageName : packages) {
                    evaluateBinding(packageName, true);
                }
            }
            return super.onHandleForceStop(intent, packages, uid, doit);
        }

        private void evaluateBinding(String carrierPackageName, boolean forceUnbind) {
            for (AppBinding appBinding : this.this$0.mBindings) {
                String appBindingPackage = appBinding.getPackage();
                boolean isBindingForPackage = carrierPackageName.equals(appBindingPackage);
                if (isBindingForPackage) {
                    CarrierServiceBindHelper.log(carrierPackageName + " changed and corresponds to a phone. Rebinding.");
                }
                if (appBindingPackage == null || isBindingForPackage) {
                    if (forceUnbind) {
                        appBinding.unbind();
                    }
                    appBinding.rebind();
                }
            }
        }
    }

    public CarrierServiceBindHelper(Context context) {
        this.mPackageMonitor = new CarrierServicePackageMonitor(this, null);
        this.mUserUnlockedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                CarrierServiceBindHelper.log("Received " + action);
                if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    for (AppBinding rebind : CarrierServiceBindHelper.this.mBindings) {
                        rebind.rebind();
                    }
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                CarrierServiceBindHelper.log("mHandler: " + msg.what);
                switch (msg.what) {
                    case 0:
                        AppBinding binding = msg.obj;
                        CarrierServiceBindHelper.log("Rebinding if necessary for phoneId: " + binding.getPhoneId());
                        binding.rebind();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mContext = context;
        int numPhones = TelephonyManager.from(context).getPhoneCount();
        this.mBindings = new AppBinding[numPhones];
        this.mLastSimState = new String[numPhones];
        for (int phoneId = 0; phoneId < numPhones; phoneId++) {
            this.mBindings[phoneId] = new AppBinding(phoneId);
        }
        this.mPackageMonitor.register(context, this.mHandler.getLooper(), UserHandle.ALL, false);
        this.mContext.registerReceiverAsUser(this.mUserUnlockedReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.USER_UNLOCKED"), null, this.mHandler);
    }

    void updateForPhoneId(int phoneId, String simState) {
        log("update binding for phoneId: " + phoneId + " simState: " + simState);
        if (SubscriptionManager.isValidPhoneId(phoneId) && !TextUtils.isEmpty(simState) && !simState.equals(this.mLastSimState[phoneId])) {
            this.mLastSimState[phoneId] = simState;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0, this.mBindings[phoneId]));
        }
    }

    private static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CarrierServiceBindHelper:");
        for (AppBinding binding : this.mBindings) {
            binding.dump(fd, pw, args);
        }
    }
}
