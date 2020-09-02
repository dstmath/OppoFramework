package com.color.util;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Slog;
import android.util.SparseIntArray;
import com.android.internal.R;
import com.oppo.luckymoney.LMManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorNavigationBarUtil {
    private static final String ACTIVITY_COLOR = "activityColor";
    private static final String ACTIVITY_NAME = "activityName";
    private static final int ALPHA_BIT_NUM = 4;
    private static final int COLOR_ALPHA_OPAQUE = -16777216;
    private static final int COLOR_BIT_NUM = 6;
    private static final boolean DEBUG_OPPO_SYSTEMBAR = false;
    private static final String DEFAULT_COLOR = "default_color";
    public static final String ENVELOPE_CONTENT_TAG = "envelope_content_tag";
    public static final String ENVELOPE_FILTER_FIELD = "envelope_filter_field";
    public static final String ENVELOPE_FILTER_VALUE = "envelope_filter_value";
    public static final String ENVELOPE_GROUP_TAG = "envelope_group_tag";
    public static final String ENVELOPE_USER_FIELD = "envelope_user_field";
    public static final String ENVELOPE_USER_NAME_TAG_FIRST = "envelope_user_name_tag_first";
    public static final String ENVELOPE_USER_NAME_TAG_LAST = "envelope_user_name_tag_last";
    private static final int HEX_NUM = 16;
    private static final String IS_NEED_PALETTE = "is_need_palette";
    private static final int MAX_COUNT = 20;
    private static final String NAVBAR_BACKGROUND = "nav_bg";
    private static final String NAV_BG_COLOR = "bg_color";
    private static final String PKG = "pkg";
    private static final String PKG_VERSION = "pkg_version";
    /* access modifiers changed from: private */
    public static final String TAG = ColorNavigationBarUtil.class.getSimpleName();
    private static final String WECHAT_PKG_NAME = "com.tencent.mm";
    /* access modifiers changed from: private */
    public static final List<AdaptationEnvelopeInfo> mAdaptationEnvelopeInfoList = new ArrayList();
    /* access modifiers changed from: private */
    public static final List<AdaptationAppInfo> mDefaultAdapationApps = new ArrayList();
    /* access modifiers changed from: private */
    public static final List<AdaptationImeInfo> mDefaultAdapationIme = new ArrayList();
    private static final String[] mDefaultAdaptationAppNames = {"com.tencent.mm", LMManager.QQ_PACKAGENAME, "com.smile.gifmaker", "com.tencent.qqlive", "com.ss.android.article.news", "com.qiyi.video", "com.taobao.taobao", "com.tencent.mtt", "com.kugou.android", "com.snda.wifilocating", "com.tencent.news", "com.baidu.searchbox", "com.kingroot.kinguser", "com.tencent.reading", "com.ss.android.article.video", "com.tencent.karaoke", "com.tencent.qqmusic", "com.happyelements.AndroidAnimal", "com.sina.weibo", "cn.wps.moffice_eng", "com.UCMobile", "com.tencent.qqpimsecure", "com.youku.phone", "com.ss.android.ugc.live", "com.qihoo360.mobilesafe", "com.ss.android.essay.joke", "com.immomo.momo", "com.xunmeng.pinduoduo", "com.vlocker.locker", "com.baidu.BaiduMap", "com.netease.cloudmusic", "com.autonavi.minimap", "com.cleanmaster.mguard_cn", "com.tencent.android.qqdownloader", "com.sankuai.meituan", "com.hunantv.imgo.activity", "com.ifreetalk.ftalk", "com.meitu.meiyancamera", "cn.kuwo.player", "com.qihoo.magic", "com.mt.mtxx.mtxx", "com.netease.newsreader.activity", "com.qihoo.browser", "com.sdu.didi.psnger", "com.ss.android.ugc.aweme", "com.lemon.faceu", "com.ztgame.bob", "com.jifen.qukan", "com.tencent.tmgp.cf", "com.qzone", "com.ss.android.article.lite", "com.qihoo.cleandroid_cn", "com.qq.reader", "com.as.one", "com.le123.ysdq", "com.youloft.calendar", "com.moji.mjweather", "com.baidu.netdisk", "com.p1.mobile.putong", "tv.danmaku.bili", "com.jingdong.app.mall", "com.excelliance.dualaid", "com.qihoo.appstore", "com.qihoo.video", "me.ele", "com.storm.smart", "com.example.businesshall", "com.campmobile.snowcamera", "com.meitu.meipaimv", "com.tencent.wifimanager", "com.ushaqi.zhuishushenqi", "com.tencent.qt.qtl", "com.tencent.gamehelper.smoba", "com.shoujiduoduo.ringtone", "com.sdu.didi.gsui", "com.duowan.kiwi", "com.mobike.mobikeapp", "com.pplive.androidphone", "com.mahjong.sichuang", "tv.pps.mobile", "com.tencent.ttpic", "com.baidu.tieba", "com.cleanmaster.security_cn", "com.wuba", "com.wepie.snake.nearme.gamecenter", "com.xunlei.downloadprovider", "com.shuqi.controller", "com.ximalaya.ting.android", "com.quanben.novel", "com.jm.android.jumei", "air.tv.douyu.android", "com.alibaba.android.rimet", "so.ofo.labofo", "com.sohu.sohuvideo", "bubei.tingshu", "com.baidu.appsearch", "com.zengame.zrttddz.nearme.gamecenter", "com.kuaikan.comic", "com.kingroot.master", "com.m4399.gamecenter", "com.chaozh.iReaderFree", "com.oppo.camera.lock", "com.esbook.reader", "com.baidu.video", "com.mogujie", "com.baidu.homework", "com.imusic.iting", "com.cubic.autohome", "com.sina.news", "com.sdu.didi.gui", "com.handsgo.jiakao.android", "cn.xiaochuankeji.tieba", "com.taobao.qianniu", "com.jxedt", "com.kingsoft", "com.husor.beibei", "com.sogou.activity.src", "com.letv.android.client", "com.shuji.reader", "com.sohu.inputmethod.sogou", "com.tmall.wireless", "com.coohuaclient", "com.lingan.seeyou", "com.babytree.apps.pregnancy", "com.doubleopen.wxskzs", "com.chinamworld.main", "com.sankuai.meituan.takeoutnew", "com.huaqian", "com.meitu.wheecam", "com.tencent.androidqqmail", "com.icbc", "com.sitech.ac", "com.sankuai.meituan.meituanwaimaibusiness", "com.duowan.mobile", "com.sankuai.meituan.dispatch.homebrew", "com.MobileTicket", "com.babycloud.hanju", "com.taobao.idlefish", "com.duoduo.child.story", "com.kuaigeng.video", "com.Qunar", "com.sinovatech.unicom.ui", "com.cootek.smartdialer", "com.tudou.android", "com.sogou.novel", "com.tencent.tribe", "com.ophone.reader.ui", "com.telecom.video.ikan4g", "com.icbc.im", "com.whatsapp", "com.weirui.mm", "com.yel.reader", "com.sunrise.scmbhc", "com.mianfeia.book", "com.baidu.yuedu", "com.qihoo.haosou.subscribe.vertical.book", "com.panda.videoliveplatform", "com.mianfeinovel", "com.xianshu.ebook", "com.mampod.ergedd", "com.c2vl.kgamebox", "com.remennovel", "com.qq.ac.android", "com.tencent.qgame", "com.duowan.makefriends", "com.kascend.chushou", "com.nd.android.pandareader", "com.baidu.searchbox_oppo", "com.facebook.katana", "com.leb.quanbenreader", "com.bokecc.dance", "com.book2345.reader", "com.boyaa.chinesechess.nearme.gamecenter", "com.xfplay.play", "com.uc108.mobile.gamecenter", "com.ifeng.news2", "com.chaojishipin.sarrs", "com.google.android.youtube", "com.qidian.QDReader", "com.tencent.qqgame.qqhlupwvga", "com.aikan", "com.zhihu.android", "com.sogou.toptennews", "com.funshion.video.mobile", "cn.kdqbxs.reader", "com.telecom.video", "com.mewe.wolf", "com.faloo.BookReader4Android", "com.dianping.v1", "com.tencent.qqpim", "com.android.vending", "com.nuomi", "com.changba", "com.youdao.dict", "com.chinatelecom.bestpayclient", "com.meelive.ingkee", "com.dewmobile.kuaiya", "com.fenbi.android.solar", "cn.opda.a.phonoalbumshoushou", "com.yuedong.sport", "im.yixin", "com.tencent.token", "fm.qingting.qtradio", "com.jiuyan.infashion", "com.tuan800.tao800", "com.greenpoint.android.mc10086.activity", "com.wochacha", "com.ct.client", "com.gotokeep.keep", "cmb.pb", "com.gameloft.android.ANMP.GloftDMCN", "com.jsmcc", "com.baidu.lbs.waimai", "com.yx", "com.ganji.android", "cn.eclicks.wzsearch", "com.pingan.papd", "com.quvideo.xiaoying", "com.suning.mobile.ebuy", "com.meitu.makeup", "com.yiche.price", "com.pingan.lifeinsurance", "com.youan.universal", "com.fingersoft.hillclimb.noncmcc", "com.alibaba.wireless", "cn.j.hers", "com.ijinshan.browser_fast", "com.xiachufang", "cmccwm.mobilemusic", "com.meilishuo", "com.tongcheng.android", "com.tencent.map", "com.baidu.browser.apps", "cn.etouch.ecalendar", "com.chinamworld.bocmbci", "com.sohu.newsclient", "vStudio.Android.Camera360", "cn.mama.pregnant", "com.huajiao", "com.picsart.studio", "com.yixia.xiaokaxiu", "com.taobao.trip", "com.ting.mp3.android", "com.eg.android.AlipayGphone", "com.achievo.vipshop", "ctrip.android.view", "com.boly.wxmultopen", "com.sds.android.ttpod", "com.tuniu.app.ui", "com.mjb.moneymanager.jkb", "com.mjb.moneymanager.likefq", "com.jiafenqi.loanmanager", "com.quanqiujy.main.activity", "com.etsxzuoye", "com.yinhan.hunter.oppo.nearme.gamecenter", "net.crimoon.pm.nearme.gamecenter"};
    private static final int[] mDefaultAdaptationImeColors = {-2631721, -1644826, -1250068, -1907738, -3681313, -1579033, -2564640, -2564640, -14606047, Color.LTGRAY, -2891552, -14273992, -14934233, -5193269, -2565928, -854792, -5193269, -2827811, -1710360, -4997947, -2368549};
    private static final String[] mDefaultAdaptationImeNames = {"com.sohu.inputmethod.sogouoem", "com.baidu.input_oppo", "com.sohu.inputmethod.sogou", "com.baidu.input", "com.tencent.qqpinyin", "com.komoxo.octopusime", "com.iflytek.inputmethod", "com.iflytek.inputmethod.oem", "com.cootek.smartinputv5", "com.gaoxin.guangsuimenew", "com.hit.wi", "com.nuance.swype.dtc.china", "com.touchtype.swiftkey", "com.ziipin.softkeyboard", "io.liuliu.game", "com.iflytek.inputmethods.DungkarIME", "com.ziipin.softkeyboard.kazakh", "com.edujia.ime", "com.songheng.wubiime", "com.xinshuru.inputmethod", "com.menksoft.softkeyboard"};
    private static final int[] mDefaultAppColors = {-197380, -1, -1184275, -328966, -723466, -1, -1, -1, -1, -1, -1, -1, -1, -592138, -1, -1, -1, -1184275, -328966, -1, -1, -1, -1, -1, -328966, -1, -65794, -1, -1118482, -1, -65537, -1, -1, -1, -657931, -1, -592137, -657931, -131587, -11237889, -16237461, -592138, -657931, -657931, -16711422, -657931, -1184275, -723724, -16777216, -1, -1, -592138, -526345, -16777216, -526343, -65794, -13487566, -1, -526345, -262659, -1, -14339769, -1, -526345, -592138, -1, -1, -1, -15001306, -526345, -1, -1118224, -460554, -1, -13881544, -1, -1184275, -460552, -1184275, -1, -1, -65794, -1184275, -460552, -1184275, -1, -1, -1, -328966, -1, -1, -526345, -1, -1, -1, -1, -1184275, -1, -590849, -13948117, -1, -657931, -526604, -328966, -1, -1, -65794, -1, -460552, -1, -1, -1, -1, -65794, -592138, -263173, -460294, -263173, -1, -1, -1, -657931, -1, -394759, -1, -15066841, -197380, -1, -1, -1, -592138, -1, -1, -1, -1052684, -13421773, -1, -1, -13908280, -1, -1, -1, -1, -1, -2440033, -1, -591365, -723981, -1, -1, -16777216, -592138, -460552, -1, -1184275, -1, -1, -328966, -592138, -12961222, -1, -328966, -1, -1, -1, -1, -921103, -1, -1, -1, -1, -1, -9951982, -855568, -65794, -657931, -526343, -1, -1, -1184275, -1, -1, -1, -131587, -328966, -723981, -1052684, -921103, -328966, -1118482, -1, -1, -460552, -1, -65794, -1, -1, -1, -1, -1, -1, -1, -1, -1, -460552, -1, -460552, -592138, -1, -1, -1, -1, -1, -1, -1, -1, -11953422, -1, -1, -1, -1, -1, -1, -1184275, -526345, -657931, -1, -1, -1, -1, -855310, -1, -526344, -1, -394759, -1, -1, -66051, -66051, -1, -15132639, -1, -1, -328706, -328965, -1118482, -1, -460552, -592138, -460552, -460552, -1, -394759, -3157533, -16777216, -16777216};
    private static final String[] mDefaultEnvelopeInfo = {WifiEnterpriseConfig.ENGINE_DISABLE, "MainUI_User_Last_Msg_Type", "436207665", "Main_User", "@chatroom", "]", ": [微信红包]", "[微信红包]"};
    private static final List<AdaptationActivityInfo> mDefaultNotAdapationActivities = new ArrayList();
    private static final int[] mDefaultNotAdaptationActivityColors = {-197380, -16777216, -197380, -1, -1, -16777216, -16777216, -1, -1, -1, -1, -1, -13487566, -657931, -657931, -1, -1, -1184275, -1, -1, -1, -1, -1118224, -1, -1, -16777216, -1};
    private static final String[] mDefaultNotAdaptationActivityNames = {"com.tencent.mm.ui.LauncherUI", "com.tencent.mm.plugin.sns.ui.SnsBrowseUI", "com.tencent.mm.plugin.profile.ui.ContactInfoUI", "com.tencent.mobileqq.activity.SplashActivity", "com.tencent.mobileqq.activity.LoginActivity", "com.tencent.biz.qrcode.activity.ScannerActivity", "com.tencent.mobileqq.activity.richmedia.NewFlowCameraActivity", "com.kugou.android.app.MediaActivity", "com.kuaikan.comic.ui.MainActivity", "com.yy.mobile.ui.home.MainActivity", "com.yy.mobile.ui.ylink.LiveTemplateActivity", "com.taobao.trip.home.HomeActivity", "com.moji.mjweather.MainActivity", "com.coohuaclient.ui.activity.HomeActivity", "com.coohuaclient.ui.activity.ChatInfoActivity", "com.jd.lib.scan.lib.zxing.client.android.CaptureActivity", "com.leadeon.cmcc.core.zxing.CaptureActivity", "com.mobike.mobikeapp.MainActivity", "com.google.zxing.client.android.CaptureActivity", "com.tencent.karaoke.module.live.ui.LiveActivity", "org.qiyi.android.video.MainActivity", "com.xunmeng.pinduoduo.ui.activity.MainFrameActivity", "com.tencent.qt.qtl.activity.main.LauncherActivity", "cn.etouch.ecalendar.settings.cover.CoverStoryActivity", "com.tencent.av.ui.AVActivity", "com.immomo.momo.newprofile.activity.OtherProfileActivity", "com.tmall.wireless.detail.ui.TMItemDetailsActivity"};
    private static final int[] mExpDefaultAdaptationImeColors = {-1118482, -15989216, -15790321, -1315087, -1249295, -1249295, -1249295, -13553353, -13553353, -14866644, -16777216, -15197669, -14934233, -1973019};
    private static final String[] mExpDefaultAdaptationImeNames = {"panda.keyboard.emoji.theme", "com.simejikeyboard", "com.dotc.ime.latin.flash", "ninja.thiha.frozenkeyboard2", "com.google.android.inputmethod.latin", "com.google.android.inputmethod.japanese", "com.google.android.inputmethod.korean", "com.jb.emoji.gokeyboard", "com.jb.gokeyboard", "com.qisiemoji.inputmethod", "net.siamdev.nattster.manman", "com.dianxinos.duemojikeyboard", "com.touchtype.swiftkey", "com.emoji.keyboard.touchpal.oppo"};
    private static final String[] mNotFullScreenPackageName = {"com.srcb.mbank", "com.winsafe.mobilephone.syngenta", "com.city78.zipai.nearme.gamecenter", "com.android.Sortilege.UI", "com.baidu.carlife", "cxboy.android.game.puzzle"};
    /* access modifiers changed from: private */
    public static final Object mObject = new Object();
    /* access modifiers changed from: private */
    public static final List<AdaptationAppInfo> mStatusDefaultAdapationApps = new ArrayList();
    private static volatile ColorNavigationBarUtil sColorNavigationBarUtil = null;
    private static int sGrooveHeigh = 80;
    private static boolean sHasAdjustPkg = false;
    private static boolean sIsNotFullScren = false;
    private static boolean sIsWithoutGroove = false;
    private static int sNavigationbarHeigh = 144;
    private static List<String> sNotFullScreenApp = new ArrayList();
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mHasInitialized = false;
    private boolean mIsExpVersion = false;
    private boolean mIsImeInGestureMode = false;
    private boolean mIsImeProcess = false;
    /* access modifiers changed from: private */
    public boolean mReadEnvelopeData = false;
    /* access modifiers changed from: private */
    public boolean mReadImeData = false;
    /* access modifiers changed from: private */
    public boolean mReadNavData = false;
    /* access modifiers changed from: private */
    public boolean mReadStatusData = false;
    private int mUpdateEnvelopeCount = 0;
    private int mUpdateImeCount = 0;
    private int mUpdateNavCount = 0;
    private int mUpdateStaCount = 0;
    /* access modifiers changed from: private */
    public boolean mUseDefualtData = true;

    static {
        int i = 0;
        while (true) {
            String[] strArr = mNotFullScreenPackageName;
            if (i < strArr.length) {
                sNotFullScreenApp.add(strArr[i]);
                i++;
            } else {
                return;
            }
        }
    }

    class AdaptationActivityInfo {
        String mActivityName;
        int mDefaultColor;

        AdaptationActivityInfo() {
        }
    }

    /* access modifiers changed from: package-private */
    public class AdaptationAppInfo {
        Map<String, String> mActivityColorList;
        SparseIntArray mColorArray;
        int mDefaultColor;
        boolean mIsNeedPalette;
        int[] mKeys;
        String mPkg;

        AdaptationAppInfo() {
        }
    }

    /* access modifiers changed from: package-private */
    public class AdaptationImeInfo {
        int mDefaultColor;
        String mPkg;

        AdaptationImeInfo() {
        }
    }

    /* access modifiers changed from: package-private */
    public class AdaptationEnvelopeInfo {
        String mEnvelopeContentTag;
        String mEnvelopeFilterField;
        String mEnvelopeFilterValue;
        String mEnvelopeGroupTag;
        String mEnvelopeUserField;
        String mEnvelopeUserNameTagFirst;
        String mEnvelopeUserNameTagLast;
        String mPkgVersion;

        AdaptationEnvelopeInfo() {
        }
    }

    public static ColorNavigationBarUtil getInstance() {
        if (sColorNavigationBarUtil == null) {
            synchronized (ColorNavigationBarUtil.class) {
                if (sColorNavigationBarUtil == null) {
                    sColorNavigationBarUtil = new ColorNavigationBarUtil();
                }
            }
        }
        return sColorNavigationBarUtil;
    }

    private ColorNavigationBarUtil() {
    }

    public void init(Context context) {
        this.mContext = context;
        this.mIsExpVersion = context.getPackageManager().hasSystemFeature("oppo.version.exp");
        this.mHasInitialized = true;
        updateAppNavBarDefaultList();
        updateImeDefaultList();
        registerContentObserver();
        updateEnvelopeDefaultInfo();
    }

    public void initData(Context context) {
        sGrooveHeigh = context.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        sNavigationbarHeigh = context.getResources().getDimensionPixelSize(R.dimen.navigation_bar_height);
    }

    private void updateEnvelopeDefaultInfo() {
        synchronized (mObject) {
            AdaptationEnvelopeInfo info = new AdaptationEnvelopeInfo();
            info.mPkgVersion = mDefaultEnvelopeInfo[0];
            info.mEnvelopeFilterField = mDefaultEnvelopeInfo[1];
            info.mEnvelopeFilterValue = mDefaultEnvelopeInfo[2];
            info.mEnvelopeUserField = mDefaultEnvelopeInfo[3];
            info.mEnvelopeGroupTag = mDefaultEnvelopeInfo[4];
            info.mEnvelopeUserNameTagFirst = mDefaultEnvelopeInfo[5];
            info.mEnvelopeUserNameTagLast = mDefaultEnvelopeInfo[6];
            info.mEnvelopeContentTag = mDefaultEnvelopeInfo[7];
            mAdaptationEnvelopeInfoList.add(info);
        }
    }

    private void updateImeDefaultList() {
        synchronized (mObject) {
            mDefaultAdapationIme.clear();
            if (this.mIsExpVersion) {
                int size = mExpDefaultAdaptationImeNames.length;
                for (int i = 0; i < size; i++) {
                    addAdaptationIme(mExpDefaultAdaptationImeNames[i], mExpDefaultAdaptationImeColors[i]);
                }
            } else {
                int size2 = mDefaultAdaptationImeNames.length;
                for (int i2 = 0; i2 < size2; i2++) {
                    addAdaptationIme(mDefaultAdaptationImeNames[i2], mDefaultAdaptationImeColors[i2]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void addAdaptationIme(String pkg, int color) {
        AdaptationImeInfo imeInfo = new AdaptationImeInfo();
        imeInfo.mPkg = pkg;
        imeInfo.mDefaultColor = color;
        mDefaultAdapationIme.add(imeInfo);
    }

    private void registerContentObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "color navigation bar util isn't init.");
            return;
        }
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.oppo.systemui/navigationbar"), true, new NavBarContentObserver());
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.oppo.systemui/statusbar"), true, new StatusBarContentObserver());
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.oppo.systemui/imecolor"), true, new ImeColorContentObserver());
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.oppo.systemui/envelope"), true, new EnvelopeContentObserver());
    }

    /* access modifiers changed from: private */
    public void updateAppNavBarDefaultList() {
        synchronized (mObject) {
            mDefaultAdapationApps.clear();
            mDefaultNotAdapationActivities.clear();
            int size = mDefaultAdaptationAppNames.length;
            for (int i = 0; i < size; i++) {
                addAdaptationApp(mDefaultAdaptationAppNames[i], mDefaultAppColors[i]);
            }
            int size2 = mDefaultNotAdaptationActivityNames.length;
            for (int i2 = 0; i2 < size2; i2++) {
                addNotAdaptationActivity(mDefaultNotAdaptationActivityNames[i2], mDefaultNotAdaptationActivityColors[i2]);
            }
            this.mUseDefualtData = true;
        }
    }

    private void addAdaptationApp(String pkg, int color) {
        addAdaptationApp(pkg, color, false);
    }

    private void addAdaptationApp(String pkg, int color, boolean palette) {
        addAdaptationApp(pkg, color, palette, null);
    }

    /* access modifiers changed from: private */
    public void addAdaptationApp(String pkg, int color, boolean palette, Map activityColorList) {
        AdaptationAppInfo appInfo = new AdaptationAppInfo();
        appInfo.mPkg = pkg;
        appInfo.mDefaultColor = color;
        appInfo.mIsNeedPalette = palette;
        appInfo.mActivityColorList = activityColorList;
        mDefaultAdapationApps.add(appInfo);
    }

    /* access modifiers changed from: private */
    public void addStatusAdaptationApp(String pkg, int color, Map activityColorList) {
        AdaptationAppInfo appInfo = new AdaptationAppInfo();
        appInfo.mPkg = pkg;
        appInfo.mDefaultColor = color;
        appInfo.mActivityColorList = activityColorList;
        mStatusDefaultAdapationApps.add(appInfo);
    }

    private void addNotAdaptationActivity(String activityName, int color) {
        AdaptationActivityInfo activityInfo = new AdaptationActivityInfo();
        activityInfo.mActivityName = activityName;
        activityInfo.mDefaultColor = color;
        mDefaultNotAdapationActivities.add(activityInfo);
    }

    /* access modifiers changed from: private */
    public void updateNavBgColorListFromDB() {
        if (!this.mHasInitialized) {
            Log.w(TAG, "color navigation bar util isn't init.");
        } else {
            new Thread() {
                /* class com.color.util.ColorNavigationBarUtil.AnonymousClass1 */

                /* JADX WARNING: Removed duplicated region for block: B:60:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
                public void run() {
                    Cursor cursor = null;
                    Map<String, String> map = null;
                    try {
                        cursor = ColorNavigationBarUtil.this.mContext.getContentResolver().query(Uri.parse("content://com.oppo.systemui/navigationbar"), null, null, null, null);
                        if (cursor != null) {
                            if (cursor.getCount() != 0) {
                                synchronized (ColorNavigationBarUtil.mObject) {
                                    ColorNavigationBarUtil.mDefaultAdapationApps.clear();
                                    while (true) {
                                        int i = 0;
                                        boolean palette = true;
                                        if (!cursor.moveToNext()) {
                                            break;
                                        }
                                        String pkg = cursor.getString(cursor.getColumnIndex("pkg"));
                                        String defColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.DEFAULT_COLOR));
                                        if (defColor == null || defColor.equals("")) {
                                            defColor = WifiEnterpriseConfig.ENGINE_DISABLE;
                                        }
                                        int defaultColor = ColorNavigationBarUtil.this.stringColorToIntColor(defColor);
                                        if (1 != cursor.getInt(cursor.getColumnIndex(ColorNavigationBarUtil.IS_NEED_PALETTE))) {
                                            palette = false;
                                        }
                                        String activity = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_NAME));
                                        String activityColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_COLOR));
                                        if (activity != null && activityColor != null && !activity.equals("") && !activityColor.equals("")) {
                                            map = new HashMap<>();
                                            String[] actList = activity.split(SmsManager.REGEX_PREFIX_DELIMITER);
                                            String[] actcolorList = activityColor.split(SmsManager.REGEX_PREFIX_DELIMITER);
                                            while (actList.length > i && actcolorList.length > i) {
                                                map.put(actList[i], actcolorList[i]);
                                                i++;
                                            }
                                        }
                                        ColorNavigationBarUtil.this.addAdaptationApp(pkg, defaultColor, palette, map);
                                    }
                                    boolean unused = ColorNavigationBarUtil.this.mUseDefualtData = false;
                                    boolean unused2 = ColorNavigationBarUtil.this.mReadNavData = true;
                                }
                                if (cursor == null) {
                                    return;
                                }
                                cursor.close();
                            }
                        }
                        Log.w(ColorNavigationBarUtil.TAG, "cursor is null or count is 0.");
                        if (cursor == null) {
                        }
                    } catch (Exception e) {
                        ColorNavigationBarUtil.this.updateAppNavBarDefaultList();
                        Log.w(ColorNavigationBarUtil.TAG, "query error! list size " + ColorNavigationBarUtil.mDefaultAdapationApps.size() + " e:" + e);
                        if (cursor == null) {
                            return;
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                    cursor.close();
                }
            }.start();
        }
    }

    /* access modifiers changed from: private */
    public void updateStatusBgColorListFromDB() {
        if (!this.mHasInitialized) {
            Log.w(TAG, "color navigation bar util isn't init.");
        } else {
            new Thread() {
                /* class com.color.util.ColorNavigationBarUtil.AnonymousClass2 */

                /* JADX WARNING: Removed duplicated region for block: B:51:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
                public void run() {
                    Cursor cursor = null;
                    Map<String, String> map = null;
                    try {
                        cursor = ColorNavigationBarUtil.this.mContext.getContentResolver().query(Uri.parse("content://com.oppo.systemui/statusbar"), null, null, null, null);
                        if (cursor != null) {
                            if (cursor.getCount() != 0) {
                                synchronized (ColorNavigationBarUtil.mObject) {
                                    ColorNavigationBarUtil.mStatusDefaultAdapationApps.clear();
                                    while (cursor.moveToNext()) {
                                        String pkg = cursor.getString(cursor.getColumnIndex("pkg"));
                                        String defColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.DEFAULT_COLOR));
                                        if (defColor == null || defColor.equals("")) {
                                            defColor = WifiEnterpriseConfig.ENGINE_DISABLE;
                                        }
                                        int defaultColor = ColorNavigationBarUtil.this.stringColorToIntColor(defColor);
                                        String activity = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_NAME));
                                        String activityColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_COLOR));
                                        if (!activity.equals("") && !activityColor.equals("")) {
                                            map = new HashMap<>();
                                            String[] actList = activity.split(SmsManager.REGEX_PREFIX_DELIMITER);
                                            String[] actcolorList = activityColor.split(SmsManager.REGEX_PREFIX_DELIMITER);
                                            int i = 0;
                                            while (actList.length > i && actcolorList.length > i) {
                                                map.put(actList[i], actcolorList[i]);
                                                i++;
                                            }
                                        }
                                        ColorNavigationBarUtil.this.addStatusAdaptationApp(pkg, defaultColor, map);
                                    }
                                    boolean unused = ColorNavigationBarUtil.this.mReadStatusData = true;
                                }
                                if (cursor == null) {
                                    return;
                                }
                                cursor.close();
                            }
                        }
                        Log.w(ColorNavigationBarUtil.TAG, "updateStatusBgColorListFromDB cursor is null or count is 0.");
                        if (cursor == null) {
                        }
                    } catch (Exception e) {
                        ColorNavigationBarUtil.mStatusDefaultAdapationApps.clear();
                        String access$100 = ColorNavigationBarUtil.TAG;
                        Log.w(access$100, "updateStatusBgColorListFromDB query error:" + e);
                        if (cursor == null) {
                            return;
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                    cursor.close();
                }
            }.start();
        }
    }

    /* access modifiers changed from: private */
    public void updateImeBgColorListFromDB() {
        if (!this.mHasInitialized) {
            Log.w(TAG, "color navigation bar util isn't init.");
        } else {
            new Thread() {
                /* class com.color.util.ColorNavigationBarUtil.AnonymousClass3 */

                /* JADX WARNING: Removed duplicated region for block: B:39:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
                public void run() {
                    Cursor cursor = null;
                    try {
                        cursor = ColorNavigationBarUtil.this.mContext.getContentResolver().query(Uri.parse("content://com.oppo.systemui/imecolor"), null, null, null, null);
                        if (cursor != null) {
                            if (cursor.getCount() != 0) {
                                synchronized (ColorNavigationBarUtil.mObject) {
                                    ColorNavigationBarUtil.mDefaultAdapationIme.clear();
                                    while (cursor.moveToNext()) {
                                        String pkg = cursor.getString(cursor.getColumnIndex("pkg"));
                                        String defColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.DEFAULT_COLOR));
                                        if (defColor == null || defColor.equals("")) {
                                            defColor = WifiEnterpriseConfig.ENGINE_DISABLE;
                                        }
                                        ColorNavigationBarUtil.this.addAdaptationIme(pkg, ColorNavigationBarUtil.this.stringColorToIntColor(defColor));
                                    }
                                    boolean unused = ColorNavigationBarUtil.this.mReadImeData = true;
                                }
                                if (cursor == null) {
                                    return;
                                }
                                cursor.close();
                            }
                        }
                        Log.w(ColorNavigationBarUtil.TAG, "updateImeBgColorListFromDB cursor is null or count is 0.");
                        if (cursor == null) {
                        }
                    } catch (Exception e) {
                        ColorNavigationBarUtil.mDefaultAdapationIme.clear();
                        String access$100 = ColorNavigationBarUtil.TAG;
                        Log.w(access$100, "mDefaultAdapationIme query error:" + e);
                        if (cursor == null) {
                            return;
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                    cursor.close();
                }
            }.start();
        }
    }

    /* access modifiers changed from: private */
    public void updateEnvelopeInfoFromDB() {
        if (!this.mHasInitialized) {
            Log.w(TAG, "color navigation bar util isn't init.");
        } else {
            new Thread() {
                /* class com.color.util.ColorNavigationBarUtil.AnonymousClass4 */

                /* JADX WARNING: Removed duplicated region for block: B:32:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
                public void run() {
                    Cursor cursor = null;
                    try {
                        cursor = ColorNavigationBarUtil.this.mContext.getContentResolver().query(Uri.parse("content://com.oppo.systemui/envelope"), null, null, null, null);
                        if (cursor != null) {
                            if (cursor.getCount() != 0) {
                                synchronized (ColorNavigationBarUtil.mObject) {
                                    ColorNavigationBarUtil.mAdaptationEnvelopeInfoList.clear();
                                    while (cursor.moveToNext()) {
                                        AdaptationEnvelopeInfo info = new AdaptationEnvelopeInfo();
                                        info.mPkgVersion = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.PKG_VERSION));
                                        info.mEnvelopeFilterField = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ENVELOPE_FILTER_FIELD));
                                        info.mEnvelopeFilterValue = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ENVELOPE_FILTER_VALUE));
                                        info.mEnvelopeUserField = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ENVELOPE_USER_FIELD));
                                        info.mEnvelopeGroupTag = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ENVELOPE_GROUP_TAG));
                                        info.mEnvelopeUserNameTagFirst = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ENVELOPE_USER_NAME_TAG_FIRST));
                                        info.mEnvelopeUserNameTagLast = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ENVELOPE_USER_NAME_TAG_LAST));
                                        info.mEnvelopeContentTag = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ENVELOPE_CONTENT_TAG));
                                        ColorNavigationBarUtil.mAdaptationEnvelopeInfoList.add(info);
                                    }
                                    boolean unused = ColorNavigationBarUtil.this.mReadEnvelopeData = true;
                                }
                                if (cursor == null) {
                                    return;
                                }
                                cursor.close();
                            }
                        }
                        Log.w(ColorNavigationBarUtil.TAG, "updateEnvelopeInfoFromDB cursor is null or count is 0.");
                        if (cursor == null) {
                        }
                    } catch (Exception e) {
                        String access$100 = ColorNavigationBarUtil.TAG;
                        Log.w(access$100, "mAdaptationEnvelopeInfoList query error:" + e);
                        if (cursor == null) {
                            return;
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                    cursor.close();
                }
            }.start();
        }
    }

    /* access modifiers changed from: private */
    public int stringColorToIntColor(String color) {
        int length = color.length();
        if (length < 6) {
            String str = TAG;
            Slog.e(str, "Color String Error! colorString:" + color);
            return 0;
        }
        String alpha = color.substring(0, length - 6);
        String colorString = color.substring(length - 6, length);
        if (alpha.equals("")) {
            alpha = "ff";
        }
        if (colorString.equals("")) {
            return 0;
        }
        return Integer.valueOf(colorString, 16).intValue() | (Integer.valueOf(alpha, 16).intValue() << 24);
    }

    public class NavBarContentObserver extends ContentObserver {
        public NavBarContentObserver() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            ColorNavigationBarUtil.this.updateNavBgColorListFromDB();
        }
    }

    public class StatusBarContentObserver extends ContentObserver {
        public StatusBarContentObserver() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            ColorNavigationBarUtil.this.updateStatusBgColorListFromDB();
        }
    }

    public class ImeColorContentObserver extends ContentObserver {
        public ImeColorContentObserver() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            ColorNavigationBarUtil.this.updateImeBgColorListFromDB();
        }
    }

    public class EnvelopeContentObserver extends ContentObserver {
        public EnvelopeContentObserver() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            ColorNavigationBarUtil.this.updateEnvelopeInfoFromDB();
        }
    }

    public boolean isActivityNeedPalette(String pkg, String activityName) {
        if (!this.mHasInitialized) {
            Log.w(TAG, "color navigation bar util isn't init.");
            return false;
        }
        if (!this.mReadNavData && this.mUpdateNavCount < 20) {
            updateNavBgColorListFromDB();
            this.mUpdateNavCount++;
            Slog.d(TAG, "isActivityNeedPalette mUpdateNavCount:" + this.mUpdateNavCount);
        }
        synchronized (mObject) {
            int size = mDefaultAdapationApps.size();
            for (int i = 0; i < size; i++) {
                AdaptationAppInfo appInfo = mDefaultAdapationApps.get(i);
                if (appInfo.mActivityColorList != null) {
                    for (Map.Entry<String, String> entry : appInfo.mActivityColorList.entrySet()) {
                        if (entry.getKey().equals(activityName)) {
                            return false;
                        }
                    }
                }
                if (appInfo.mPkg.equals(pkg)) {
                    boolean z = appInfo.mIsNeedPalette;
                    return z;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f2, code lost:
        return 0;
     */
    public int getNavBarColorFromAdaptation(String pkg, String activityName) {
        if (!this.mHasInitialized) {
            Log.w(TAG, "color navigation bar util isn't init.");
            return 0;
        }
        if (!this.mReadNavData && this.mUpdateNavCount < 20) {
            updateNavBgColorListFromDB();
            this.mUpdateNavCount++;
            Slog.d(TAG, "getNavBarColorFromAdaptation mUpdateNavCount:" + this.mUpdateNavCount);
        }
        synchronized (mObject) {
            if (this.mUseDefualtData) {
                int size = mDefaultNotAdapationActivities.size();
                for (int i = 0; i < size; i++) {
                    AdaptationActivityInfo activityInfo = mDefaultNotAdapationActivities.get(i);
                    if (activityInfo.mActivityName.equals(activityName)) {
                        Slog.d(TAG, "the defualt activity:" + activityName + " color: " + Integer.toHexString(activityInfo.mDefaultColor));
                        int i2 = activityInfo.mDefaultColor;
                        return i2;
                    }
                }
            }
            int size2 = mDefaultAdapationApps.size();
            for (int i3 = 0; i3 < size2; i3++) {
                AdaptationAppInfo appInfo = mDefaultAdapationApps.get(i3);
                if (appInfo.mPkg.equals(pkg)) {
                    if (appInfo.mActivityColorList != null) {
                        for (Map.Entry<String, String> entry : appInfo.mActivityColorList.entrySet()) {
                            if (entry.getValue() != null) {
                                if (!entry.getValue().equals("")) {
                                    if (entry.getKey().equals(activityName)) {
                                        int intValue = -16777216 | Integer.valueOf(entry.getValue(), 16).intValue();
                                        return intValue;
                                    }
                                }
                            }
                        }
                    }
                    int i4 = appInfo.mDefaultColor;
                    return i4;
                }
            }
            return 0;
        }
    }

    public int getStatusBarColorFromAdaptation(String pkg, String activityName) {
        if (!this.mHasInitialized) {
            Log.w(TAG, "color navigation bar util isn't init.");
            return 0;
        }
        if (!this.mReadStatusData && this.mUpdateStaCount < 20) {
            updateStatusBgColorListFromDB();
            this.mUpdateStaCount++;
        }
        synchronized (mObject) {
            int size = mStatusDefaultAdapationApps.size();
            for (int i = 0; i < size; i++) {
                AdaptationAppInfo appInfo = mStatusDefaultAdapationApps.get(i);
                if (appInfo.mPkg.equals(pkg)) {
                    if (appInfo.mActivityColorList != null && !appInfo.mActivityColorList.equals("")) {
                        for (Map.Entry<String, String> entry : appInfo.mActivityColorList.entrySet()) {
                            if (entry.getKey().equals(activityName)) {
                                int stringColorToIntColor = stringColorToIntColor(entry.getValue());
                                return stringColorToIntColor;
                            }
                        }
                    }
                    int i2 = appInfo.mDefaultColor;
                    return i2;
                }
            }
            return 0;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0052, code lost:
        if (r5.mIsExpVersion == false) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005d, code lost:
        return r5.mContext.getColor(201720937);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0067, code lost:
        return r5.mContext.getColor(201720936);
     */
    public int getImeBgColorFromAdaptation(String pkg) {
        if (!this.mHasInitialized) {
            Log.w(TAG, "color navigation bar util isn't init.");
            return 0;
        }
        if (!this.mReadImeData && this.mUpdateImeCount < 20) {
            updateImeBgColorListFromDB();
            this.mUpdateImeCount++;
        }
        synchronized (mObject) {
            int size = mDefaultAdapationIme.size();
            if (size == 0) {
                updateImeDefaultList();
                size = mDefaultAdapationIme.size();
            }
            for (int i = 0; i < size; i++) {
                AdaptationImeInfo imeInfo = mDefaultAdapationIme.get(i);
                if (imeInfo.mPkg.equals(pkg)) {
                    int i2 = imeInfo.mDefaultColor;
                    return i2;
                }
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0113, code lost:
        return "";
     */
    public String getEnvelopeInfo(String key) {
        if (!this.mHasInitialized) {
            Log.w(TAG, "color navigation bar util isn't init.");
            return "";
        }
        char c = 1;
        if (!this.mReadEnvelopeData && this.mUpdateEnvelopeCount < 20) {
            updateEnvelopeInfoFromDB();
            this.mUpdateEnvelopeCount++;
        }
        synchronized (mObject) {
            if (mAdaptationEnvelopeInfoList.size() == 0) {
                updateEnvelopeDefaultInfo();
            }
            String installedVersion = getVersion(this.mContext, "com.tencent.mm");
            if (installedVersion != null && !installedVersion.isEmpty()) {
                int index = -1;
                int i = 0;
                while (i < mAdaptationEnvelopeInfoList.size() && compareVersion(mAdaptationEnvelopeInfoList.get(i).mPkgVersion, installedVersion)) {
                    index = i;
                    i++;
                }
                if (index >= 0) {
                    if (mAdaptationEnvelopeInfoList.size() == 0) {
                        updateEnvelopeDefaultInfo();
                    }
                    switch (key.hashCode()) {
                        case -2021563734:
                            if (key.equals(ENVELOPE_USER_NAME_TAG_LAST)) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case -1252397227:
                            if (key.equals(ENVELOPE_USER_FIELD)) {
                                c = 2;
                                break;
                            }
                            c = 65535;
                            break;
                        case -1030876117:
                            if (key.equals(ENVELOPE_GROUP_TAG)) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case -25660187:
                            if (key.equals(ENVELOPE_CONTENT_TAG)) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case 836559234:
                            if (key.equals(ENVELOPE_FILTER_FIELD)) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        case 851104249:
                            if (key.equals(ENVELOPE_FILTER_VALUE)) {
                                break;
                            }
                            c = 65535;
                            break;
                        case 1750730012:
                            if (key.equals(ENVELOPE_USER_NAME_TAG_FIRST)) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            String str = mAdaptationEnvelopeInfoList.get(index).mEnvelopeFilterField;
                            return str;
                        case 1:
                            String str2 = mAdaptationEnvelopeInfoList.get(index).mEnvelopeFilterValue;
                            return str2;
                        case 2:
                            String str3 = mAdaptationEnvelopeInfoList.get(index).mEnvelopeUserField;
                            return str3;
                        case 3:
                            String str4 = mAdaptationEnvelopeInfoList.get(index).mEnvelopeGroupTag;
                            return str4;
                        case 4:
                            String str5 = mAdaptationEnvelopeInfoList.get(index).mEnvelopeUserNameTagFirst;
                            return str5;
                        case 5:
                            String str6 = mAdaptationEnvelopeInfoList.get(index).mEnvelopeUserNameTagLast;
                            return str6;
                        case 6:
                            String str7 = mAdaptationEnvelopeInfoList.get(index).mEnvelopeContentTag;
                            return str7;
                    }
                }
            }
        }
    }

    public boolean isNavigationBarHiddenWithGroove(String pkg) {
        if (this.mIsImeProcess && !this.mIsImeInGestureMode) {
            return false;
        }
        if ((pkg == null || !isNotFullScreenOrWithoutGroove(pkg)) && 1 == SystemProperties.getInt("oppo.hide.navigationbar", 0)) {
            return true;
        }
        return false;
    }

    public boolean isNavigationBarHiddenWithoutGroove(String pkg) {
        if (pkg == null || !isFullScreenAndWithoutGroove(pkg)) {
            return false;
        }
        if ((!this.mIsImeProcess || this.mIsImeInGestureMode) && 1 == SystemProperties.getInt("oppo.hide.navigationbar", 0)) {
            return true;
        }
        return false;
    }

    public static int getGrooveHeight() {
        return sGrooveHeigh;
    }

    public static int getNavigationBarHeight() {
        return sNavigationbarHeigh;
    }

    private boolean isNotFullScreenOrWithoutGroove(String pkg) {
        if (pkg == null) {
            return false;
        }
        if (!sHasAdjustPkg) {
            if (this.mIsImeProcess) {
                sIsNotFullScren = false;
            } else {
                sIsNotFullScren = ColorDisplayCompatUtils.getInstance().shouldCompatAdjustForPkg(pkg);
            }
            sIsWithoutGroove = ColorDisplayCompatUtils.getInstance().shouldNonImmersiveAdjustForPkg(pkg);
            sHasAdjustPkg = true;
        }
        if (sIsNotFullScren || sIsWithoutGroove) {
            return true;
        }
        return false;
    }

    private boolean isFullScreenAndWithoutGroove(String pkg) {
        if (pkg == null) {
            return false;
        }
        if (!sHasAdjustPkg) {
            if (this.mIsImeProcess) {
                sIsNotFullScren = false;
            } else {
                sIsNotFullScren = ColorDisplayCompatUtils.getInstance().shouldCompatAdjustForPkg(pkg);
            }
            sIsWithoutGroove = ColorDisplayCompatUtils.getInstance().shouldNonImmersiveAdjustForPkg(pkg);
            sHasAdjustPkg = true;
        }
        if (sIsNotFullScren || !sIsWithoutGroove) {
            return false;
        }
        return true;
    }

    public void setImePackageInGestureMode(boolean isImeInGestureMode) {
        String str = TAG;
        Log.i(str, "setImePackageInGestureMode isImeInGestureMode:" + isImeInGestureMode);
        this.mIsImeProcess = true;
        sHasAdjustPkg = false;
        this.mIsImeInGestureMode = isImeInGestureMode;
    }

    public static String getVersion(Context context, String pkgName) {
        try {
            return context.getPackageManager().getPackageInfo(pkgName, 0).versionName;
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "GetVersion failed! e:" + e);
            return null;
        }
    }

    public static boolean compareVersion(String versionA, String versionB) {
        String str = TAG;
        Log.i(str, "A:" + versionA + " B:" + versionB);
        if (versionA == null || versionA.equals("") || versionB == null || versionB.equals("")) {
            return false;
        }
        if (versionA.equals(versionB)) {
            return true;
        }
        String[] arrayA = versionA.split("\\.");
        String[] arrayB = versionB.split("\\.");
        int length = arrayA.length < arrayB.length ? arrayA.length : arrayB.length;
        int i = 0;
        while (i < length) {
            if (Integer.parseInt(arrayB[i]) > Integer.parseInt(arrayA[i])) {
                String str2 = TAG;
                Log.i(str2, "B:" + Integer.parseInt(arrayB[i]) + " > A:" + Integer.parseInt(arrayA[i]));
                return true;
            } else if (Integer.parseInt(arrayB[i]) < Integer.parseInt(arrayA[i])) {
                String str3 = TAG;
                Log.i(str3, "B:" + Integer.parseInt(arrayB[i]) + " < A:" + Integer.parseInt(arrayA[i]));
                return false;
            } else {
                i++;
            }
        }
        return false;
    }
}
