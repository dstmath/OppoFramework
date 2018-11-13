package com.android.server.am;

import java.util.ArrayList;
import java.util.List;

public class OppoAppStartupListManager {
    private static OppoAppStartupListManager sOppoAppStartupListManager = null;
    private List<String> mAssociateWhiteList = new ArrayList();
    private List<String> mBindServiceCpnList = new ArrayList();
    private List<String> mProtectWhiteList = new ArrayList();
    private List<String> mProviderCpnList = new ArrayList();

    private OppoAppStartupListManager() {
        initWhiteList();
    }

    public static final OppoAppStartupListManager getInstance() {
        if (sOppoAppStartupListManager == null) {
            sOppoAppStartupListManager = new OppoAppStartupListManager();
        }
        return sOppoAppStartupListManager;
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
