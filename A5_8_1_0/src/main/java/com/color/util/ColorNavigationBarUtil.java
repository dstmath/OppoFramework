package com.color.util;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.SparseIntArray;
import com.android.internal.R;
import com.android.internal.policy.PhoneWindow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ColorNavigationBarUtil {
    private static final String ACTIVITY_COLOR = "activityColor";
    private static final String ACTIVITY_NAME = "activityName";
    private static final int ALPHA_BIT_NUM = 4;
    private static final int COLOR_ALPHA_OPAQUE = -16777216;
    private static final int COLOR_BIT_NUM = 6;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_OPPO_SYSTEMBAR = false;
    private static final String DEFAULT_COLOR = "default_color";
    private static final int HEX_NUM = 16;
    private static final String IS_NEED_PALETTE = "is_need_palette";
    private static final int MAX_COUNT = 20;
    private static final String NAVBAR_BACKGROUND = "nav_bg";
    private static final String NAV_BG_COLOR = "bg_color";
    private static final String PKG = "pkg";
    private static final String PKG_VERSION = "pkg_version";
    private static final String TAG = ColorNavigationBarUtil.class.getSimpleName();
    private static final List<AdaptationAppInfo> mDefaultAdapationApps = new ArrayList();
    private static final String[] mDefaultAdaptationAppNames = new String[]{"com.tencent.mm", "com.tencent.mobileqq", "com.smile.gifmaker", "com.tencent.qqlive", "com.ss.android.article.news", "com.qiyi.video", "com.taobao.taobao", "com.tencent.mtt", "com.kugou.android", "com.snda.wifilocating", "com.tencent.news", "com.baidu.searchbox", "com.kingroot.kinguser", "com.tencent.reading", "com.ss.android.article.video", "com.tencent.karaoke", "com.tencent.qqmusic", "com.happyelements.AndroidAnimal", "com.sina.weibo", "cn.wps.moffice_eng", "com.UCMobile", "com.tencent.qqpimsecure", "com.youku.phone", "com.ss.android.ugc.live", "com.qihoo360.mobilesafe", "com.ss.android.essay.joke", "com.immomo.momo", "com.xunmeng.pinduoduo", "com.vlocker.locker", "com.baidu.BaiduMap", "com.netease.cloudmusic", "com.autonavi.minimap", "com.cleanmaster.mguard_cn", "com.tencent.android.qqdownloader", "com.sankuai.meituan", "com.hunantv.imgo.activity", "com.ifreetalk.ftalk", "com.meitu.meiyancamera", "cn.kuwo.player", "com.qihoo.magic", "com.mt.mtxx.mtxx", "com.netease.newsreader.activity", "com.qihoo.browser", "com.sdu.didi.psnger", "com.ss.android.ugc.aweme", "com.lemon.faceu", "com.ztgame.bob", "com.jifen.qukan", "com.tencent.tmgp.cf", "com.qzone", "com.ss.android.article.lite", "com.qihoo.cleandroid_cn", "com.qq.reader", "com.as.one", "com.le123.ysdq", "com.youloft.calendar", "com.moji.mjweather", "com.baidu.netdisk", "com.p1.mobile.putong", "tv.danmaku.bili", "com.jingdong.app.mall", "com.excelliance.dualaid", "com.qihoo.appstore", "com.qihoo.video", "me.ele", "com.storm.smart", "com.example.businesshall", "com.campmobile.snowcamera", "com.meitu.meipaimv", "com.tencent.wifimanager", "com.ushaqi.zhuishushenqi", "com.tencent.qt.qtl", "com.tencent.gamehelper.smoba", "com.shoujiduoduo.ringtone", "com.sdu.didi.gsui", "com.duowan.kiwi", "com.mobike.mobikeapp", "com.pplive.androidphone", "com.mahjong.sichuang", "tv.pps.mobile", "com.tencent.ttpic", "com.baidu.tieba", "com.cleanmaster.security_cn", "com.wuba", "com.wepie.snake.nearme.gamecenter", "com.xunlei.downloadprovider", "com.shuqi.controller", "com.ximalaya.ting.android", "com.quanben.novel", "com.jm.android.jumei", "air.tv.douyu.android", "com.alibaba.android.rimet", "so.ofo.labofo", "com.sohu.sohuvideo", "bubei.tingshu", "com.baidu.appsearch", "com.zengame.zrttddz.nearme.gamecenter", "com.kuaikan.comic", "com.kingroot.master", "com.m4399.gamecenter", "com.chaozh.iReaderFree", "com.oppo.camera.lock", "com.esbook.reader", "com.baidu.video", "com.mogujie", "com.baidu.homework", "com.imusic.iting", "com.cubic.autohome", "com.sina.news", "com.sdu.didi.gui", "com.handsgo.jiakao.android", "cn.xiaochuankeji.tieba", "com.taobao.qianniu", "com.jxedt", "com.kingsoft", "com.husor.beibei", "com.sogou.activity.src", "com.letv.android.client", "com.shuji.reader", "com.sohu.inputmethod.sogou", "com.tmall.wireless", "com.coohuaclient", "com.lingan.seeyou", "com.babytree.apps.pregnancy", "com.doubleopen.wxskzs", "com.chinamworld.main", "com.sankuai.meituan.takeoutnew", "com.huaqian", "com.meitu.wheecam", "com.tencent.androidqqmail", "com.icbc", "com.sitech.ac", "com.sankuai.meituan.meituanwaimaibusiness", "com.duowan.mobile", "com.sankuai.meituan.dispatch.homebrew", "com.MobileTicket", "com.babycloud.hanju", "com.taobao.idlefish", "com.duoduo.child.story", "com.kuaigeng.video", "com.Qunar", "com.sinovatech.unicom.ui", "com.cootek.smartdialer", "com.tudou.android", "com.sogou.novel", "com.tencent.tribe", "com.ophone.reader.ui", "com.telecom.video.ikan4g", "com.icbc.im", "com.whatsapp", "com.weirui.mm", "com.yel.reader", "com.sunrise.scmbhc", "com.mianfeia.book", "com.baidu.yuedu", "com.qihoo.haosou.subscribe.vertical.book", "com.panda.videoliveplatform", "com.mianfeinovel", "com.xianshu.ebook", "com.mampod.ergedd", "com.c2vl.kgamebox", "com.remennovel", "com.qq.ac.android", "com.tencent.qgame", "com.duowan.makefriends", "com.kascend.chushou", "com.nd.android.pandareader", "com.baidu.searchbox_oppo", "com.facebook.katana", "com.leb.quanbenreader", "com.bokecc.dance", "com.book2345.reader", "com.boyaa.chinesechess.nearme.gamecenter", "com.xfplay.play", "com.uc108.mobile.gamecenter", "com.ifeng.news2", "com.chaojishipin.sarrs", "com.google.android.youtube", "com.qidian.QDReader", "com.tencent.qqgame.qqhlupwvga", "com.aikan", "com.zhihu.android", "com.sogou.toptennews", "com.funshion.video.mobile", "cn.kdqbxs.reader", "com.telecom.video", "com.mewe.wolf", "com.faloo.BookReader4Android", "com.dianping.v1", "com.tencent.qqpim", "com.android.vending", "com.nuomi", "com.changba", "com.youdao.dict", "com.chinatelecom.bestpayclient", "com.meelive.ingkee", "com.dewmobile.kuaiya", "com.fenbi.android.solar", "cn.opda.a.phonoalbumshoushou", "com.yuedong.sport", "im.yixin", "com.tencent.token", "fm.qingting.qtradio", "com.jiuyan.infashion", "com.tuan800.tao800", "com.greenpoint.android.mc10086.activity", "com.wochacha", "com.ct.client", "com.gotokeep.keep", "cmb.pb", "com.gameloft.android.ANMP.GloftDMCN", "com.jsmcc", "com.baidu.lbs.waimai", "com.yx", "com.ganji.android", "cn.eclicks.wzsearch", "com.pingan.papd", "com.quvideo.xiaoying", "com.suning.mobile.ebuy", "com.meitu.makeup", "com.yiche.price", "com.pingan.lifeinsurance", "com.youan.universal", "com.fingersoft.hillclimb.noncmcc", "com.alibaba.wireless", "cn.j.hers", "com.ijinshan.browser_fast", "com.xiachufang", "cmccwm.mobilemusic", "com.meilishuo", "com.tongcheng.android", "com.tencent.map", "com.baidu.browser.apps", "cn.etouch.ecalendar", "com.chinamworld.bocmbci", "com.sohu.newsclient", "vStudio.Android.Camera360", "cn.mama.pregnant", "com.huajiao", "com.picsart.studio", "com.yixia.xiaokaxiu", "com.taobao.trip", "com.ting.mp3.android", "com.eg.android.AlipayGphone", "com.achievo.vipshop", "ctrip.android.view", "com.boly.wxmultopen", "com.sds.android.ttpod", "com.tuniu.app.ui", "com.mjb.moneymanager.jkb", "com.mjb.moneymanager.likefq", "com.jiafenqi.loanmanager", "com.quanqiujy.main.activity", "com.etsxzuoye", "com.yinhan.hunter.oppo.nearme.gamecenter", "net.crimoon.pm.nearme.gamecenter"};
    private static final int[] mDefaultAppColors = new int[]{-197380, -1, -1184275, -328966, -723466, -1, -1, -1, -1, -1, -1, -1, -1, -592138, -1, -1, -1, -1184275, -328966, -1, -1, -1, -1, -1, -328966, -1, -65794, -1, -1118482, -1, -65537, -1, -1, -1, -657931, -1, -592137, -657931, -131587, -11237889, -16237461, -592138, -657931, -657931, -16711422, -657931, -1184275, -723724, -16777216, -1, -1, -592138, -526345, -16777216, -526343, -65794, -13487566, -1, -526345, -262659, -1, -14339769, -1, -526345, -592138, -1, -1, -1, -15001306, -526345, -1, -1118224, -460554, -1, -13881544, -1, -1184275, -460552, -1184275, -1, -1, -65794, -1184275, -460552, -1184275, -1, -1, -1, -328966, -1, -1, -526345, -1, -1, -1, -1, -1184275, -1, -590849, -13948117, -1, -657931, -526604, -328966, -1, -1, -65794, -1, -460552, -1, -1, -1, -1, -65794, -592138, PhoneWindow.NAVIGATION_BAR_COLOR_GRAY, -460294, PhoneWindow.NAVIGATION_BAR_COLOR_GRAY, -1, -1, -1, -657931, -1, -394759, -1, -15066841, -197380, -1, -1, -1, -592138, -1, -1, -1, -1052684, -13421773, -1, -1, -13908280, -1, -1, -1, -1, -1, -2440033, -1, -591365, -723981, -1, -1, -16777216, -592138, -460552, -1, -1184275, -1, -1, -328966, -592138, -12961222, -1, -328966, -1, -1, -1, -1, -921103, -1, -1, -1, -1, -1, -9951982, -855568, -65794, -657931, -526343, -1, -1, -1184275, -1, -1, -1, -131587, -328966, -723981, -1052684, -921103, -328966, -1118482, -1, -1, -460552, -1, -65794, -1, -1, -1, -1, -1, -1, -1, -1, -1, -460552, -1, -460552, -592138, -1, -1, -1, -1, -1, -1, -1, -1, -11953422, -1, -1, -1, -1, -1, -1, -1184275, -526345, -657931, -1, -1, -1, -1, -855310, -1, -526344, -1, -394759, -1, -1, -66051, -66051, -1, -15132639, -1, -1, -328706, -328965, -1118482, -1, -460552, -592138, -460552, -460552, -1, -394759, -3157533, -16777216, -16777216};
    private static final List<AdaptationActivityInfo> mDefaultNotAdapationActivities = new ArrayList();
    private static final int[] mDefaultNotAdaptationActivityColors = new int[]{-197380, -16777216, -197380, -1, -1, -16777216, -16777216, -1, -1, -1, -1, -1, -13487566, -657931, -657931, -1, -1, -1184275, -1, -1, -1, -1, -1118224, -1, -1, -16777216, -1};
    private static final String[] mDefaultNotAdaptationActivityNames = new String[]{"com.tencent.mm.ui.LauncherUI", "com.tencent.mm.plugin.sns.ui.SnsBrowseUI", "com.tencent.mm.plugin.profile.ui.ContactInfoUI", "com.tencent.mobileqq.activity.SplashActivity", "com.tencent.mobileqq.activity.LoginActivity", "com.tencent.biz.qrcode.activity.ScannerActivity", "com.tencent.mobileqq.activity.richmedia.NewFlowCameraActivity", "com.kugou.android.app.MediaActivity", "com.kuaikan.comic.ui.MainActivity", "com.yy.mobile.ui.home.MainActivity", "com.yy.mobile.ui.ylink.LiveTemplateActivity", "com.taobao.trip.home.HomeActivity", "com.moji.mjweather.MainActivity", "com.coohuaclient.ui.activity.HomeActivity", "com.coohuaclient.ui.activity.ChatInfoActivity", "com.jd.lib.scan.lib.zxing.client.android.CaptureActivity", "com.leadeon.cmcc.core.zxing.CaptureActivity", "com.mobike.mobikeapp.MainActivity", "com.google.zxing.client.android.CaptureActivity", "com.tencent.karaoke.module.live.ui.LiveActivity", "org.qiyi.android.video.MainActivity", "com.xunmeng.pinduoduo.ui.activity.MainFrameActivity", "com.tencent.qt.qtl.activity.main.LauncherActivity", "cn.etouch.ecalendar.settings.cover.CoverStoryActivity", "com.tencent.av.ui.AVActivity", "com.immomo.momo.newprofile.activity.OtherProfileActivity", "com.tmall.wireless.detail.ui.TMItemDetailsActivity"};
    private static final String[] mNotFullScreenPackageName = new String[]{"com.srcb.mbank", "com.winsafe.mobilephone.syngenta", "com.city78.zipai.nearme.gamecenter", "com.android.Sortilege.UI", "com.baidu.carlife", "cxboy.android.game.puzzle"};
    private static final Object mObject = new Object();
    private static final List<AdaptationAppInfo> mStatusDefaultAdapationApps = new ArrayList();
    private static volatile ColorNavigationBarUtil sColorNavigationBarUtil = null;
    private static int sGrooveHeigh = 80;
    private static boolean sHasAdjustPkg = false;
    private static boolean sIsNotFullScren = false;
    private static boolean sIsWithoutGroove = false;
    private static int sNavigationbarHeigh = 144;
    private static List<String> sNotFullScreenApp = new ArrayList();
    private Context mContext;
    private boolean mHasInitialized = false;
    private boolean mIsImeInGestureMode = false;
    private boolean mIsImeProcess = false;
    private boolean mReadNavData = false;
    private boolean mReadStatusData = false;
    private int mUpdateNavCount = 0;
    private int mUpdateStaCount = 0;
    private boolean mUseDefualtData = true;

    class AdaptationActivityInfo {
        String mActivityName;
        int mDefaultColor;

        AdaptationActivityInfo() {
        }
    }

    class AdaptationAppInfo {
        Map<String, String> mActivityColorList;
        SparseIntArray mColorArray;
        int mDefaultColor;
        boolean mIsNeedPalette;
        int[] mKeys;
        String mPkg;

        AdaptationAppInfo() {
        }
    }

    public class NavBarContentObserver extends ContentObserver {
        public NavBarContentObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            ColorNavigationBarUtil.this.updateNavBgColorListFromDB();
        }
    }

    public class StatusBarContentObserver extends ContentObserver {
        public StatusBarContentObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            ColorNavigationBarUtil.this.updateStatusBgColorListFromDB();
        }
    }

    static {
        for (Object add : mNotFullScreenPackageName) {
            sNotFullScreenApp.add(add);
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
        this.mHasInitialized = true;
        updateAppNavBarDefaultList();
        registerContentObserver();
    }

    public void initData(Context context) {
        sGrooveHeigh = context.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        sNavigationbarHeigh = context.getResources().getDimensionPixelSize(R.dimen.navigation_bar_height);
    }

    private void registerContentObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "color navigation bar util isn't init.");
            return;
        }
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.oppo.systemui/navigationbar"), true, new NavBarContentObserver());
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.oppo.systemui/statusbar"), true, new StatusBarContentObserver());
    }

    private void updateAppNavBarDefaultList() {
        synchronized (mObject) {
            int i;
            mDefaultAdapationApps.clear();
            mDefaultNotAdapationActivities.clear();
            int size = mDefaultAdaptationAppNames.length;
            for (i = 0; i < size; i++) {
                addAdaptationApp(mDefaultAdaptationAppNames[i], mDefaultAppColors[i]);
            }
            size = mDefaultNotAdaptationActivityNames.length;
            for (i = 0; i < size; i++) {
                addNotAdaptationActivity(mDefaultNotAdaptationActivityNames[i], mDefaultNotAdaptationActivityColors[i]);
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

    private void addAdaptationApp(String pkg, int color, boolean palette, Map activityColorList) {
        AdaptationAppInfo appInfo = new AdaptationAppInfo();
        appInfo.mPkg = pkg;
        appInfo.mDefaultColor = color;
        appInfo.mIsNeedPalette = palette;
        appInfo.mActivityColorList = activityColorList;
        mDefaultAdapationApps.add(appInfo);
    }

    private void addStatusAdaptationApp(String pkg, int color, Map activityColorList) {
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

    private void updateNavBgColorListFromDB() {
        if (this.mHasInitialized) {
            new Thread() {
                /* JADX WARNING: Removed duplicated region for block: B:57:0x014b  */
                /* JADX WARNING: Removed duplicated region for block: B:70:? A:{SYNTHETIC, RETURN} */
                /* JADX WARNING: Removed duplicated region for block: B:54:0x0143  */
                /* JADX WARNING: Missing block: B:45:0x0102, code:
            r18 = r19;
     */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    Throwable th;
                    Exception e;
                    Cursor cursor = null;
                    Map<String, String> map = null;
                    try {
                        cursor = ColorNavigationBarUtil.this.mContext.getContentResolver().query(Uri.parse("content://com.oppo.systemui/navigationbar"), null, null, null, null);
                        if (cursor == null || cursor.getCount() == 0) {
                            Log.w(ColorNavigationBarUtil.TAG, "cursor is null or count is 0.");
                        } else {
                            synchronized (ColorNavigationBarUtil.mObject) {
                                Map<String, String> map2;
                                try {
                                    ColorNavigationBarUtil.mDefaultAdapationApps.clear();
                                    while (true) {
                                        try {
                                            map2 = map;
                                            if (!cursor.moveToNext()) {
                                                break;
                                            }
                                            String pkg = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.PKG));
                                            String defColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.DEFAULT_COLOR));
                                            if (defColor == null || defColor.equals("")) {
                                                defColor = "0";
                                            }
                                            int defaultColor = ColorNavigationBarUtil.this.stringColorToIntColor(defColor);
                                            boolean palette = 1 == cursor.getInt(cursor.getColumnIndex(ColorNavigationBarUtil.IS_NEED_PALETTE));
                                            String activity = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_NAME));
                                            String activityColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_COLOR));
                                            if (activity.equals("")) {
                                                map = map2;
                                            } else if ((activityColor.equals("") ^ 1) != 0) {
                                                map = new HashMap();
                                                String[] actList = activity.split(",");
                                                String[] actcolorList = activityColor.split(",");
                                                int i = 0;
                                                while (actList.length > i && actcolorList.length > i) {
                                                    map.put(actList[i], actcolorList[i]);
                                                    i++;
                                                }
                                            } else {
                                                map = map2;
                                            }
                                            ColorNavigationBarUtil.this.addAdaptationApp(pkg, defaultColor, palette, map);
                                        } catch (Throwable th2) {
                                            th = th2;
                                            map = map2;
                                            throw th;
                                        }
                                    }
                                    ColorNavigationBarUtil.this.mUseDefualtData = false;
                                    ColorNavigationBarUtil.this.mReadNavData = true;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                                try {
                                } catch (Exception e2) {
                                    e = e2;
                                    map = map2;
                                    try {
                                        ColorNavigationBarUtil.this.updateAppNavBarDefaultList();
                                        Log.w(ColorNavigationBarUtil.TAG, "query error! list size " + ColorNavigationBarUtil.mDefaultAdapationApps.size() + " e:" + e);
                                        if (cursor == null) {
                                            cursor.close();
                                            return;
                                        }
                                        return;
                                    } catch (Throwable th4) {
                                        th = th4;
                                        if (cursor != null) {
                                            cursor.close();
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    map = map2;
                                    if (cursor != null) {
                                    }
                                    throw th;
                                }
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Exception e3) {
                        e = e3;
                        ColorNavigationBarUtil.this.updateAppNavBarDefaultList();
                        Log.w(ColorNavigationBarUtil.TAG, "query error! list size " + ColorNavigationBarUtil.mDefaultAdapationApps.size() + " e:" + e);
                        if (cursor == null) {
                        }
                    }
                }
            }.start();
        } else {
            Log.w(TAG, "color navigation bar util isn't init.");
        }
    }

    private void updateStatusBgColorListFromDB() {
        if (this.mHasInitialized) {
            new Thread() {
                /* JADX WARNING: Removed duplicated region for block: B:66:? A:{SYNTHETIC, RETURN} */
                /* JADX WARNING: Removed duplicated region for block: B:50:0x0111  */
                /* JADX WARNING: Removed duplicated region for block: B:53:0x0119  */
                /* JADX WARNING: Missing block: B:41:0x00e5, code:
            r17 = r18;
     */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    Throwable th;
                    Exception e;
                    Cursor cursor = null;
                    Map<String, String> map = null;
                    try {
                        cursor = ColorNavigationBarUtil.this.mContext.getContentResolver().query(Uri.parse("content://com.oppo.systemui/statusbar"), null, null, null, null);
                        if (cursor == null || cursor.getCount() == 0) {
                            Log.w(ColorNavigationBarUtil.TAG, "updateStatusBgColorListFromDB cursor is null or count is 0.");
                        } else {
                            synchronized (ColorNavigationBarUtil.mObject) {
                                try {
                                    Map<String, String> map2;
                                    ColorNavigationBarUtil.mStatusDefaultAdapationApps.clear();
                                    while (true) {
                                        try {
                                            map2 = map;
                                            if (!cursor.moveToNext()) {
                                                break;
                                            }
                                            String pkg = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.PKG));
                                            String defColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.DEFAULT_COLOR));
                                            if (defColor == null || defColor.equals("")) {
                                                defColor = "0";
                                            }
                                            int defaultColor = ColorNavigationBarUtil.this.stringColorToIntColor(defColor);
                                            String activity = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_NAME));
                                            String activityColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_COLOR));
                                            if (activity.equals("")) {
                                                map = map2;
                                            } else if ((activityColor.equals("") ^ 1) != 0) {
                                                map = new HashMap();
                                                String[] actList = activity.split(",");
                                                String[] actcolorList = activityColor.split(",");
                                                int i = 0;
                                                while (actList.length > i && actcolorList.length > i) {
                                                    map.put(actList[i], actcolorList[i]);
                                                    i++;
                                                }
                                            } else {
                                                map = map2;
                                            }
                                            ColorNavigationBarUtil.this.addStatusAdaptationApp(pkg, defaultColor, map);
                                        } catch (Throwable th2) {
                                            th = th2;
                                            map = map2;
                                            throw th;
                                        }
                                    }
                                    ColorNavigationBarUtil.this.mReadStatusData = true;
                                    try {
                                    } catch (Exception e2) {
                                        e = e2;
                                        map = map2;
                                        try {
                                            ColorNavigationBarUtil.mStatusDefaultAdapationApps.clear();
                                            Log.w(ColorNavigationBarUtil.TAG, "updateStatusBgColorListFromDB query error:" + e);
                                            if (cursor == null) {
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            if (cursor != null) {
                                                cursor.close();
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th4) {
                                        th = th4;
                                        map = map2;
                                        if (cursor != null) {
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                }
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Exception e3) {
                        e = e3;
                        ColorNavigationBarUtil.mStatusDefaultAdapationApps.clear();
                        Log.w(ColorNavigationBarUtil.TAG, "updateStatusBgColorListFromDB query error:" + e);
                        if (cursor == null) {
                            cursor.close();
                        }
                    }
                }
            }.start();
        } else {
            Log.w(TAG, "color navigation bar util isn't init.");
        }
    }

    private int stringColorToIntColor(String color) {
        int length = color.length();
        if (length < 6) {
            Slog.e(TAG, "Color String Error! colorString:" + color);
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
        return (Integer.valueOf(alpha, 16).intValue() << 24) | Integer.valueOf(colorString, 16).intValue();
    }

    public boolean isActivityNeedPalette(String pkg, String activityName) {
        if (this.mHasInitialized) {
            if (!this.mReadNavData && this.mUpdateNavCount < 20) {
                updateNavBgColorListFromDB();
                this.mUpdateNavCount++;
                Slog.d(TAG, "isActivityNeedPalette mUpdateNavCount:" + this.mUpdateNavCount);
            }
            synchronized (mObject) {
                int size = mDefaultAdapationApps.size();
                for (int i = 0; i < size; i++) {
                    AdaptationAppInfo appInfo = (AdaptationAppInfo) mDefaultAdapationApps.get(i);
                    if (appInfo.mActivityColorList != null) {
                        for (Entry<String, String> entry : appInfo.mActivityColorList.entrySet()) {
                            if (((String) entry.getKey()).equals(activityName)) {
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
        Log.w(TAG, "color navigation bar util isn't init.");
        return false;
    }

    /* JADX WARNING: Missing block: B:36:0x00cf, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getNavBarColorFromAdaptation(String pkg, String activityName) {
        if (this.mHasInitialized) {
            if (!this.mReadNavData && this.mUpdateNavCount < 20) {
                updateNavBgColorListFromDB();
                this.mUpdateNavCount++;
                Slog.d(TAG, "getNavBarColorFromAdaptation mUpdateNavCount:" + this.mUpdateNavCount);
            }
            synchronized (mObject) {
                int size;
                int i;
                int i2;
                if (this.mUseDefualtData) {
                    size = mDefaultNotAdapationActivities.size();
                    for (i = 0; i < size; i++) {
                        AdaptationActivityInfo activityInfo = (AdaptationActivityInfo) mDefaultNotAdapationActivities.get(i);
                        if (activityInfo.mActivityName.equals(activityName)) {
                            Slog.d(TAG, "the defualt activity:" + activityName + " color: " + Integer.toHexString(activityInfo.mDefaultColor));
                            i2 = activityInfo.mDefaultColor;
                            return i2;
                        }
                    }
                }
                size = mDefaultAdapationApps.size();
                for (i = 0; i < size; i++) {
                    AdaptationAppInfo appInfo = (AdaptationAppInfo) mDefaultAdapationApps.get(i);
                    if (appInfo.mActivityColorList != null) {
                        for (Entry<String, String> entry : appInfo.mActivityColorList.entrySet()) {
                            if (entry.getValue() == null || ((String) entry.getValue()).equals("")) {
                            } else if (((String) entry.getKey()).equals(activityName)) {
                                i2 = Integer.valueOf((String) entry.getValue(), 16).intValue() | -16777216;
                                return i2;
                            }
                        }
                    }
                    if (appInfo.mPkg.equals(pkg)) {
                        i2 = appInfo.mDefaultColor;
                        return i2;
                    }
                }
                return 0;
            }
        }
        Log.w(TAG, "color navigation bar util isn't init.");
        return 0;
    }

    public int getStatusBarColorFromAdaptation(String pkg, String activityName) {
        if (this.mHasInitialized) {
            if (!this.mReadStatusData && this.mUpdateStaCount < 20) {
                updateStatusBgColorListFromDB();
                this.mUpdateStaCount++;
            }
            synchronized (mObject) {
                int size = mStatusDefaultAdapationApps.size();
                for (int i = 0; i < size; i++) {
                    AdaptationAppInfo appInfo = (AdaptationAppInfo) mStatusDefaultAdapationApps.get(i);
                    if (appInfo.mPkg.equals(pkg)) {
                        int stringColorToIntColor;
                        if (!(appInfo.mActivityColorList == null || (appInfo.mActivityColorList.equals("") ^ 1) == 0)) {
                            for (Entry<String, String> entry : appInfo.mActivityColorList.entrySet()) {
                                if (((String) entry.getKey()).equals(activityName)) {
                                    stringColorToIntColor = stringColorToIntColor((String) entry.getValue());
                                    return stringColorToIntColor;
                                }
                            }
                        }
                        stringColorToIntColor = appInfo.mDefaultColor;
                        return stringColorToIntColor;
                    }
                }
                return 0;
            }
        }
        Log.w(TAG, "color navigation bar util isn't init.");
        return 0;
    }

    public boolean isNavigationBarHiddenWithGroove(String pkg) {
        boolean z = true;
        if (this.mIsImeProcess && (this.mIsImeInGestureMode ^ 1) != 0) {
            return false;
        }
        if (pkg != null && (isNotFullScreenOrWithoutGroove(pkg) || (sNotFullScreenApp != null && sNotFullScreenApp.contains(pkg)))) {
            return false;
        }
        if (1 != SystemProperties.getInt("oppo.hide.navigationbar", 0)) {
            z = false;
        }
        return z;
    }

    public boolean isNavigationBarHiddenWithoutGroove(String pkg) {
        boolean z = false;
        if (pkg != null) {
            int contains;
            if (sNotFullScreenApp != null) {
                contains = sNotFullScreenApp.contains(pkg);
            } else {
                contains = 0;
            }
            if ((contains ^ 1) != 0 && isFullScreenAndWithoutGroove(pkg)) {
                if (this.mIsImeProcess && (this.mIsImeInGestureMode ^ 1) != 0) {
                    return false;
                }
                if (1 == SystemProperties.getInt("oppo.hide.navigationbar", 0)) {
                    z = true;
                }
                return z;
            }
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
        boolean z = true;
        if (pkg == null) {
            return false;
        }
        if (!sHasAdjustPkg) {
            sIsNotFullScren = ColorDisplayCompatUtils.getInstance().shouldCompatAdjustForPkg(pkg);
            sIsWithoutGroove = ColorDisplayCompatUtils.getInstance().shouldNonImmersiveAdjustForPkg(pkg);
            sHasAdjustPkg = true;
        }
        if (!sIsNotFullScren) {
            z = sIsWithoutGroove;
        }
        return z;
    }

    private boolean isFullScreenAndWithoutGroove(String pkg) {
        boolean z = false;
        if (pkg == null) {
            return false;
        }
        if (!sHasAdjustPkg) {
            sIsNotFullScren = ColorDisplayCompatUtils.getInstance().shouldCompatAdjustForPkg(pkg);
            sIsWithoutGroove = ColorDisplayCompatUtils.getInstance().shouldNonImmersiveAdjustForPkg(pkg);
            sHasAdjustPkg = true;
        }
        if (!sIsNotFullScren) {
            z = sIsWithoutGroove;
        }
        return z;
    }

    public void setImePackageInGestureMode(boolean isImeInGestureMode) {
        Log.i(TAG, "setImePackageInGestureMode isImeInGestureMode:" + isImeInGestureMode);
        this.mIsImeProcess = true;
        this.mIsImeInGestureMode = isImeInGestureMode;
    }
}
