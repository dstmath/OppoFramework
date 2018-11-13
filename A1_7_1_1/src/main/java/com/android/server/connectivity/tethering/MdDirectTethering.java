package com.android.server.connectivity.tethering;

import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.os.INetworkManagementService;
import android.os.SystemProperties;
import android.util.Log;
import java.util.Arrays;

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
public class MdDirectTethering {
    private static final int BR_PREFIX_LENGTH = 24;
    private static final String BR_SUB_IFACE_ADDR = "0.0.0.0";
    private static final String MDT_IFACE_BR_SUB1 = "rndis0";
    private static final String MDT_IFACE_BR_SUB2 = "ccmni-lan";
    private static final String SYSTEM_PROPERTY_MDT_BRIDGE_NAME = "ro.tethering.bridge.interface";
    private static final String SYSTEM_PROPERTY_MDT_ENABLE = "sys.mtk_md_direct_tether_enable";
    private static final String SYSTEM_PROPERTY_MDT_MODE_CHANG = "sys.usb.rndis.direct";
    private static final String SYSTEM_PROPERTY_MDT_SUPPORT = "ro.mtk_md_direct_tethering";
    private static final String TAG = null;
    private static final boolean sMdtSupport = false;
    private boolean mMdtEnable;
    private final INetworkManagementService mNMService;
    private String[] mTetherableMdtUsbRegexs;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.tethering.MdDirectTethering.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.tethering.MdDirectTethering.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.tethering.MdDirectTethering.<clinit>():void");
    }

    public MdDirectTethering(INetworkManagementService nmService) {
        this.mNMService = nmService;
        this.mMdtEnable = SystemProperties.getBoolean(SYSTEM_PROPERTY_MDT_ENABLE, false);
        Log.d(TAG, "MDT support: " + this.mMdtEnable);
        this.mTetherableMdtUsbRegexs = new String[1];
        this.mTetherableMdtUsbRegexs[0] = SystemProperties.get(SYSTEM_PROPERTY_MDT_BRIDGE_NAME, "mdbr0");
        Log.d(TAG, "UsbRegexs:" + Arrays.toString(this.mTetherableMdtUsbRegexs));
    }

    public boolean isMdtEnable() {
        this.mMdtEnable = SystemProperties.getBoolean(SYSTEM_PROPERTY_MDT_ENABLE, false);
        if (sMdtSupport) {
            return this.mMdtEnable;
        }
        return false;
    }

    public boolean isMdtEnable(int ifaceType) {
        this.mMdtEnable = SystemProperties.getBoolean(SYSTEM_PROPERTY_MDT_ENABLE, false);
        if (!sMdtSupport || !this.mMdtEnable) {
            return false;
        }
        if (ifaceType == 1) {
            return true;
        }
        return false;
    }

    private void enableMdtFunction(boolean enabled) {
        SystemProperties.set(SYSTEM_PROPERTY_MDT_MODE_CHANG, String.valueOf(enabled));
    }

    public String[] getMdtTetherableUsbRegexs() {
        return this.mTetherableMdtUsbRegexs;
    }

    public boolean clearBridgeMac(String iface) {
        try {
            this.mNMService.clearBridgeMac(iface);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error clearBridgeMac: " + e);
            return false;
        }
    }

    public boolean configureMdtIface(String iface, boolean enabled) {
        Log.d(TAG, "configureMdtIface:" + enabled);
        try {
            InterfaceConfiguration ifcg = new InterfaceConfiguration();
            ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(BR_SUB_IFACE_ADDR), 24));
            if (enabled) {
                ifcg.setInterfaceUp();
                this.mNMService.addBridgeInterface(iface, MDT_IFACE_BR_SUB1);
                this.mNMService.addBridgeInterface(iface, MDT_IFACE_BR_SUB2);
            } else {
                ifcg.setInterfaceDown();
            }
            this.mNMService.setInterfaceConfig(MDT_IFACE_BR_SUB2, ifcg);
            this.mNMService.setInterfaceConfig(MDT_IFACE_BR_SUB1, ifcg);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error configureMdtIface: " + e);
            return false;
        }
    }

    public void addBridgeInterface(String iface, boolean isAdded) {
        Log.d(TAG, "addBridgeInterface:" + iface + ":" + isAdded);
        try {
            enableMdtFunction(isAdded);
            if (isAdded) {
                this.mNMService.addBridgeInterface(iface, MDT_IFACE_BR_SUB2);
                InterfaceConfiguration ifcg = new InterfaceConfiguration();
                ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(BR_SUB_IFACE_ADDR), 24));
                ifcg.setInterfaceUp();
                this.mNMService.setInterfaceConfig(MDT_IFACE_BR_SUB2, ifcg);
                return;
            }
            this.mNMService.deleteBridgeInterface(iface, MDT_IFACE_BR_SUB2);
        } catch (Exception e) {
            Log.e(TAG, "Error addBridgeInterface: " + e);
        }
    }

    public boolean resetMdtInterface() {
        Log.d(TAG, "resetMdtInterface");
        enableMdtFunction(false);
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(BR_SUB_IFACE_ADDR), 24));
        ifcg.setInterfaceDown();
        try {
            this.mNMService.setInterfaceConfig(MDT_IFACE_BR_SUB2, ifcg);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error resetMdtInterface: " + e);
            return false;
        }
    }

    public boolean isMobileUpstream(int upType) {
        if (upType == 0 || 4 == upType || 5 == upType) {
            Log.d(TAG, "isMobileUpstream: true");
            return true;
        }
        Log.d(TAG, "isMobileUpstream: false");
        return false;
    }

    public boolean shouldUseMdt(int radioNetworkType) {
        if (isGsm(radioNetworkType)) {
            return true;
        }
        return false;
    }

    private boolean isGsm(int radioNetworkType) {
        Log.d(TAG, "isGsm :" + radioNetworkType);
        if (radioNetworkType == 1 || radioNetworkType == 2 || radioNetworkType == 3 || radioNetworkType == 8 || radioNetworkType == 9 || radioNetworkType == 10 || radioNetworkType == 13 || radioNetworkType == 15 || radioNetworkType == 16 || radioNetworkType == 17) {
            return true;
        }
        return false;
    }
}
