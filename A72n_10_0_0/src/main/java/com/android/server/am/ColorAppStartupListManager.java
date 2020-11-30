package com.android.server.am;

import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import java.util.ArrayList;
import java.util.List;

public class ColorAppStartupListManager {
    private static ColorAppStartupListManager sColorAppStartupListManager = null;
    private List<String> mAssociateWhiteList = new ArrayList();
    private List<String> mAutoWhiteList = new ArrayList();
    private List<String> mBindServiceCpnList = new ArrayList();
    private List<String> mProtectWhiteList = new ArrayList();
    private List<String> mProviderCallerPkgList = new ArrayList();
    private List<String> mProviderCpnList = new ArrayList();

    private ColorAppStartupListManager() {
        initWhiteList();
    }

    public static final ColorAppStartupListManager getInstance() {
        if (sColorAppStartupListManager == null) {
            sColorAppStartupListManager = new ColorAppStartupListManager();
        }
        return sColorAppStartupListManager;
    }

    private void initWhiteList() {
        initBindServiceCpnList();
        initProviderCpnList();
        initAssociateWhiteList();
        initAutoWhiteList();
        initProtectWhiteList();
        initProviderCallerPkgList();
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
        this.mAssociateWhiteList.add(ColorStartingWindowContants.WECHAT_PACKAGE_NAME);
        this.mAssociateWhiteList.add("com.eg.android.AlipayGphone");
        this.mAssociateWhiteList.add("com.tencent.mobileqq");
        this.mAssociateWhiteList.add("com.tencent.tim");
        this.mAssociateWhiteList.add("com.oneplus.market");
        this.mAssociateWhiteList.add("com.coloros.apprecover");
        this.mAssociateWhiteList.add("com.heytap.gamecenter");
        this.mAssociateWhiteList.add("com.nearme.note");
        this.mAssociateWhiteList.add("com.nearme.play");
        this.mAssociateWhiteList.add("com.heytap.health");
        this.mAssociateWhiteList.add("com.heytap.reader");
        this.mAssociateWhiteList.add("com.redteamobile.oppo.roaming");
        this.mAssociateWhiteList.add("com.coloros.gamedock");
        this.mAssociateWhiteList.add("com.coloros.screenrecorder");
        this.mAssociateWhiteList.add("com.coloros.wallet");
        this.mAssociateWhiteList.add("com.coloros.familyguard");
        this.mAssociateWhiteList.add("com.standardar.service");
        this.mAssociateWhiteList.add("com.oppo.community");
        this.mAssociateWhiteList.add("com.coloros.securityguard");
        this.mAssociateWhiteList.add("com.redteamobile.roaming");
        this.mAssociateWhiteList.add("com.oppo.im");
        this.mAssociateWhiteList.add("com.nearme.gamecenter");
        this.mAssociateWhiteList.add("com.coloros.note");
        this.mAssociateWhiteList.add("com.coloros.yoli");
        this.mAssociateWhiteList.add("com.coloros.digitalwellbeing");
        this.mAssociateWhiteList.add("com.coloros.colorfilestand");
        this.mAssociateWhiteList.add("com.coloros.flashnote");
        this.mAssociateWhiteList.add("com.coloros.oppopods");
        this.mAssociateWhiteList.add("com.coloros.personalassistant");
        this.mAssociateWhiteList.add("com.coloros.gamespaceui");
        this.mAssociateWhiteList.add("com.oppo.reader");
        this.mAssociateWhiteList.add("com.oppo.ohome");
        this.mAssociateWhiteList.add("com.oppo.store");
        this.mAssociateWhiteList.add("com.coloros.lives");
        this.mAssociateWhiteList.add("com.coloros.soundrecorder");
        this.mAssociateWhiteList.add("com.heytap.book");
        this.mAssociateWhiteList.add("com.heytap.yoli");
        this.mAssociateWhiteList.add("com.coloros.favorite");
        this.mAssociateWhiteList.add("com.heytap.smarthome");
        this.mAssociateWhiteList.add("com.oppo.book");
        this.mAssociateWhiteList.add("com.oppo.news");
    }

    private void initAutoWhiteList() {
        this.mAutoWhiteList.clear();
        this.mAutoWhiteList.add("com.coloros.shortcuts");
    }

    private void initProtectWhiteList() {
        this.mProtectWhiteList.clear();
        this.mProtectWhiteList.add("com.alipay.security.mobile.authenticator");
        this.mProtectWhiteList.add("com.google.android.gms");
        this.mProtectWhiteList.add("com.google.android.gmss.name");
        this.mProtectWhiteList.add("com.google.android.gsf");
    }

    private void initProviderCallerPkgList() {
        this.mProviderCallerPkgList.clear();
        this.mProviderCallerPkgList.add("com.android.packageinstaller");
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

    public boolean isInAutoWhiteList(String pkgName) {
        if (this.mAutoWhiteList.contains(pkgName)) {
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

    public boolean isInProviderCallerPkgList(String pkgName) {
        if (this.mProviderCallerPkgList.contains(pkgName)) {
            return true;
        }
        return false;
    }
}
