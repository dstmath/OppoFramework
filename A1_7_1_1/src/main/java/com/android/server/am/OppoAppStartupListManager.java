package com.android.server.am;

import java.util.ArrayList;
import java.util.List;

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
public class OppoAppStartupListManager {
    private static OppoAppStartupListManager mOppoAppStartupListManager;
    private List<String> mAssociateWhiteList;
    private List<String> mBindServiceCpnList;
    private List<String> mProtectWhiteList;
    private List<String> mProviderCpnList;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppStartupListManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppStartupListManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAppStartupListManager.<clinit>():void");
    }

    private OppoAppStartupListManager() {
        this.mBindServiceCpnList = new ArrayList();
        this.mProviderCpnList = new ArrayList();
        this.mAssociateWhiteList = new ArrayList();
        this.mProtectWhiteList = new ArrayList();
        initWhiteList();
    }

    public static final OppoAppStartupListManager getInstance() {
        if (mOppoAppStartupListManager == null) {
            mOppoAppStartupListManager = new OppoAppStartupListManager();
        }
        return mOppoAppStartupListManager;
    }

    private void initWhiteList() {
        initBindServiceCpnList();
        initProviderCpnList();
        initAssociateWhiteList();
        initProtectWhiteList();
    }

    private void initBindServiceCpnList() {
        this.mBindServiceCpnList.clear();
        this.mBindServiceCpnList.add("com.alipay.android.app.MspService");
        this.mBindServiceCpnList.add("com.sina.weibo.business.RemoteSSOService");
        this.mBindServiceCpnList.add("com.tencent.assistant.sdk.SDKSupportService");
        this.mBindServiceCpnList.add("com.iflytek.speechcloud.binder.BinderService");
        this.mBindServiceCpnList.add("com.iflytek.vflynote.binder.BinderService");
        this.mBindServiceCpnList.add("com.amazon.venezia.service.command.CommandServiceImpl");
        this.mBindServiceCpnList.add("com.tenpay.android.service.TenpayService");
    }

    private void initProviderCpnList() {
        this.mProviderCpnList.clear();
        this.mProviderCpnList.add("com.tencent.mm.plugin.base.stub.MMPluginProvider");
        this.mProviderCpnList.add("com.tencent.mm.plugin.base.stub.WXCommProvider");
        this.mProviderCpnList.add("com.sina.weibo.provider.SinaWeiboSdkProvider");
        this.mProviderCpnList.add("com.immomo.momo.sdk.support.MomoSdkSupportProvider");
    }

    private void initAssociateWhiteList() {
        this.mAssociateWhiteList.clear();
        this.mAssociateWhiteList.add("com.tencent.mm");
        this.mAssociateWhiteList.add("com.eg.android.AlipayGphone");
        this.mAssociateWhiteList.add("com.tencent.mobileqq");
        this.mAssociateWhiteList.add("com.tencent.tim");
    }

    private void initProtectWhiteList() {
        this.mProtectWhiteList.clear();
        this.mProtectWhiteList.add("com.alipay.security.mobile.authenticator");
        this.mProtectWhiteList.add("com.google.android.gms");
        this.mProtectWhiteList.add("com.google.android.gmss.name");
        this.mProtectWhiteList.add("com.google.android.gsf");
    }

    public boolean isInBindServiceCpnList(String cpnName) {
        if (this.mBindServiceCpnList.contains(cpnName)) {
            return true;
        }
        return false;
    }

    public boolean isInProviderCpnList(String cpnName) {
        if (this.mProviderCpnList.contains(cpnName)) {
            return true;
        }
        return false;
    }

    public boolean isInAssociateWhiteList(String pkgName) {
        if (this.mAssociateWhiteList.contains(pkgName)) {
            return true;
        }
        return false;
    }

    public boolean isInProtectWhiteList(String pkgName) {
        if (this.mProtectWhiteList.contains(pkgName)) {
            return true;
        }
        return false;
    }
}
