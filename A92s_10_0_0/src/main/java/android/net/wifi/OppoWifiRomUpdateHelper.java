package android.net.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.OppoRomUpdateHelper;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoWifiRomUpdateHelper extends OppoRomUpdateHelper implements IWifiRomUpdateHelper {
    public static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String DATA_FILE_PATH = "/data/misc/wifi/sys_wifi_par_config_list.xml";
    private static final String FILE_NAME = "sys_wifi_par_config_list";
    private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    public static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String SYS_FILE_PATH = "/system/etc/sys_wifi_par_config_list.xml";
    private static final String TAG = "OppoWifiRomUpdateHelper";
    private static OppoWifiRomUpdateHelper sInstance;
    /* access modifiers changed from: private */
    public boolean DEBUG = false;
    /* access modifiers changed from: private */
    public String[] mDownloadApps = null;
    /* access modifiers changed from: private */
    public String[] mDualStaApps = {"com.heytap.browser", "com.android.browser", "com.coloros.browser", "com.UCMobile", "com.tencent.mm", "com.tencent.mobileqq", "com.sina.weibo", "com.netease.newsreader.activity", "com.ss.android.article.news", "com.jingdong.app.mall", "com.taobao.taobao", "com.tmall.wireless", "com.achievo.vipshop", "com.xunmeng.pinduoduo", "com.baidu.tieba", "com.qzone", "com.zhihu.android", "com.xingin.xhs", "com.baidu.browser.apps", "com.tencent.mtt", "com.eg.android.AlipayGphone", "me.ele", "com.sankuai.meituan", "com.sankuai.meituan.takeoutnew", "com.dianping.v1", "com.moji.mjweather", "ctrip.android.view", "com.Qunar", "com.tencent.news", "com.tencent.reading", "com.tencent.qqlive", "com.youku.phone", "com.qiyi.video", "com.sohu.sohuvideo", "com.tencent.android.qqdownloader", "com.oppo.market", "com.nearme.gamecenter", "com.xunlei.downloadprovider", "tv.danmaku.bili", "com.ss.android.ugc.aweme", "com.smile.gifmaker", "air.tv.douyu.android", "com.ss.android.ugc.live", "com.hunantv.imgo.activity", "com.ss.android.article.video", "com.duowan.kiwi", "com.netease.cloudmusic", "com.kugou.android", "com.tencent.qqmusic"};
    /* access modifiers changed from: private */
    public String[] mDualStaAppsExp = {"com.whatsapp", "in.mohalla.sharechat", "app.buzz.share", "com.facebook.orca", "com.UCMobile.intl", "com.mcent.browser", "com.redefine.welike", "com.instagram.android", "com.heytap.browser", "com.android.browser", "com.coloros.browser", "com.android.chrome", "com.facebook.katana", "org.mozilla.firefox", "com.opera.browser"};
    /* access modifiers changed from: private */
    public String[] mDualStaBlackList = null;
    /* access modifiers changed from: private */
    public String[] mDualStaCapHostBlackList = null;
    /* access modifiers changed from: private */
    public String[] mDualStaDisableMcc = {"200-299"};
    /* access modifiers changed from: private */
    public HashMap<String, String> mKeyValuePair = new HashMap<>();
    /* access modifiers changed from: private */
    public String[] mLmParams = {"1350#1200#0#4#17f10304", "1400#1250#0#4#17f10305", "1800#1250#0#4#17f10306", "1800#1250#0#4#17f10307"};
    private String[] mMtuServer = {"conn1.oppomobile.com", "conn2.oppomobile.com", "www.baidu.com", "www.jd.com", "www.taobao.com", "www.qq.com"};
    /* access modifiers changed from: private */
    public String[] mSkipDestroySocketApps = {"com.tencent.mm", "com.tencent.mobileqq"};
    /* access modifiers changed from: private */
    public String[] mSlaApps = {"com.heytap.browser", "com.android.browser", "com.coloros.browser", "com.UCMobile", "com.tencent.mm", "com.tencent.mobileqq", "com.sina.weibo", "com.netease.newsreader.activity", "com.ss.android.article.news", "com.jingdong.app.mall", "com.taobao.taobao", "com.tmall.wireless", "com.achievo.vipshop", "com.xunmeng.pinduoduo", "com.baidu.tieba", "com.qzone", "com.zhihu.android", "com.xingin.xhs", "com.baidu.browser.apps", "com.tencent.mtt", "com.eg.android.AlipayGphone", "me.ele", "com.sankuai.meituan", "com.sankuai.meituan.takeoutnew", "com.dianping.v1", "com.moji.mjweather", "ctrip.android.view", "com.Qunar", "com.tencent.news", "com.tencent.reading"};
    /* access modifiers changed from: private */
    public String[] mSlaAppsExp = {"com.whatsapp", "in.mohalla.sharechat", "app.buzz.share", "com.facebook.orca", "com.UCMobile.intl", "com.mcent.browser", "com.redefine.welike", "com.instagram.android", "com.heytap.browser", "com.android.browser", "com.coloros.browser", "com.android.chrome", "com.facebook.katana", "org.mozilla.firefox", "com.opera.browser", "com.heytap.browser"};
    /* access modifiers changed from: private */
    public String[] mSlaBlackList = null;
    /* access modifiers changed from: private */
    public String[] mSlaEnabledMCC = {"460", "404-405-406"};
    /* access modifiers changed from: private */
    public String[] mSlaGameApps = {"not.defined", "com.tencent.tmgp.sgame", "com.tencent.tmgp.pubgmhd"};
    /* access modifiers changed from: private */
    public String[] mSlaGameAppsExp = {"not.defined", "not.defined", "com.tencent.ig"};
    /* access modifiers changed from: private */
    public String[] mSlaGameParams = {"4#8#0000000100010003", "4#8#000003e900040003", "5#5#0864100118", "5#5#0865100018"};
    /* access modifiers changed from: private */
    public String[] mSlaParams = {"200", "500", "1000", "500", "230", "200", "220", "55", "75", "2000", "2000", "200", "55"};
    private int[] mSpeedRttParams = {150, 100, 250, 200, 150, 5, 10, 15, 10, 5, 5};
    /* access modifiers changed from: private */
    public String[] mVideoApps = null;

    public class WifiRomUpdateInfo extends OppoRomUpdateHelper.UpdateInfo {
        @Override // android.net.wifi.OppoRomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ void clear() {
            super.clear();
        }

        @Override // android.net.wifi.OppoRomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ boolean clone(OppoRomUpdateHelper.UpdateInfo updateInfo) {
            return super.clone(updateInfo);
        }

        @Override // android.net.wifi.OppoRomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ void dump() {
            super.dump();
        }

        @Override // android.net.wifi.OppoRomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ long getVersion() {
            return super.getVersion();
        }

        @Override // android.net.wifi.OppoRomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ boolean insert(int i, String str) {
            return super.insert(i, str);
        }

        @Override // android.net.wifi.OppoRomUpdateHelper.UpdateInfo
        public /* bridge */ /* synthetic */ boolean updateToLowerVersion(String str) {
            return super.updateToLowerVersion(str);
        }

        public WifiRomUpdateInfo() {
            super();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:87:0x01f1, code lost:
            if (r4 == null) goto L_0x01f4;
         */
        @Override // android.net.wifi.OppoRomUpdateHelper.UpdateInfo
        public void parseContentFromXML(String content) {
            if (OppoWifiRomUpdateHelper.this.DEBUG) {
                Log.d(OppoWifiRomUpdateHelper.TAG, "parseContentFromXML");
            }
            if (content == null) {
                Log.d(OppoWifiRomUpdateHelper.TAG, "\tcontent is null");
                return;
            }
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                strReader = new StringReader(content);
                parser.setInput(strReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0) {
                        if (eventType == 2) {
                            String mTagName = parser.getName();
                            parser.next();
                            String mText = parser.getText();
                            if (OppoWifiRomUpdateHelper.this.DEBUG) {
                                Log.d(OppoWifiRomUpdateHelper.TAG, "\t" + mTagName + ":" + mText);
                            }
                            if ("NETWORK_SLA_APPS".equals(mTagName)) {
                                String[] unused = OppoWifiRomUpdateHelper.this.mSlaApps = mText.split(",");
                            } else if ("NETWORK_SLA_APPS_EXP".equals(mTagName)) {
                                String[] unused2 = OppoWifiRomUpdateHelper.this.mSlaAppsExp = mText.split(",");
                            } else if ("NETWORK_SLA_BLACK_LIST".equals(mTagName)) {
                                String[] unused3 = OppoWifiRomUpdateHelper.this.mSlaBlackList = mText.split(",");
                            } else if ("NETWORK_SLA_GAME_APPS".equals(mTagName)) {
                                String[] unused4 = OppoWifiRomUpdateHelper.this.mSlaGameApps = mText.split(",");
                            } else if ("NETWORK_SLA_GAME_APPS_EXP".equals(mTagName)) {
                                String[] unused5 = OppoWifiRomUpdateHelper.this.mSlaGameAppsExp = mText.split(",");
                            } else if ("NETWORK_SLA_ENABLED_MCC".equals(mTagName)) {
                                String[] unused6 = OppoWifiRomUpdateHelper.this.mSlaEnabledMCC = mText.split(",");
                            } else if ("OPPO_DUAL_STA_DISABLED_MCC".equals(mTagName)) {
                                String[] unused7 = OppoWifiRomUpdateHelper.this.mDualStaDisableMcc = mText.split(",");
                            } else if ("NETWORK_SLA_PARAMS".equals(mTagName)) {
                                String[] unused8 = OppoWifiRomUpdateHelper.this.mSlaParams = mText.split(",");
                            } else if ("NETWORK_SPEED_RTT_PARAMS".equals(mTagName)) {
                                OppoWifiRomUpdateHelper.this.setSpeedRttParams(mText);
                            } else if ("NETWORK_SLA_GAME_PARAMS".equals(mTagName)) {
                                String[] unused9 = OppoWifiRomUpdateHelper.this.mSlaGameParams = mText.split(",");
                            } else if ("NETWORK_DUAL_STA_APPS".equals(mTagName)) {
                                String[] unused10 = OppoWifiRomUpdateHelper.this.mDualStaApps = mText.split(",");
                            } else if ("NETWORK_DUAL_STA_APPS_EXP".equals(mTagName)) {
                                String[] unused11 = OppoWifiRomUpdateHelper.this.mDualStaAppsExp = mText.split(",");
                            } else if ("NETWORK_DUAL_STA_BLACK_LIST".equals(mTagName)) {
                                String[] unused12 = OppoWifiRomUpdateHelper.this.mDualStaBlackList = mText.split(",");
                            } else if ("NETWORK_WECHAT_LM_PARAMS".equals(mTagName)) {
                                String[] unused13 = OppoWifiRomUpdateHelper.this.mLmParams = mText.split(",");
                            } else if ("NETWORK_VIDEO_APPS".equals(mTagName)) {
                                String[] unused14 = OppoWifiRomUpdateHelper.this.mVideoApps = mText.split(",");
                            } else if ("NETWORK_DOWNLOAD_APPS".equals(mTagName)) {
                                String[] unused15 = OppoWifiRomUpdateHelper.this.mDownloadApps = mText.split(",");
                            } else if ("NETWORK_DUAL_STA_CAP_HOST_BLACK_LIST".equals(mTagName)) {
                                String[] unused16 = OppoWifiRomUpdateHelper.this.mDualStaCapHostBlackList = mText.split(",");
                            } else if ("NETWORK_SKIP_DESTROY_SOCKET_APPS".equals(mTagName)) {
                                String[] unused17 = OppoWifiRomUpdateHelper.this.mSkipDestroySocketApps = mText.split(",");
                            } else if ("DEFAULT_MAC_RANDOMIZATION_SETTING".equals(mTagName)) {
                                OppoWifiRomUpdateHelper.this.setDefaultMacRandomizationSetting(mText);
                            } else {
                                OppoWifiRomUpdateHelper.this.mKeyValuePair.put(mTagName, mText);
                            }
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                OppoWifiRomUpdateHelper.this.log("Got execption parsing permissions.", e);
            } catch (IOException e2) {
                OppoWifiRomUpdateHelper.this.log("Got execption parsing permissions.", e2);
                if (strReader != null) {
                }
            } catch (Throwable th) {
                if (strReader != null) {
                    strReader.close();
                }
                throw th;
            }
            strReader.close();
            OppoWifiRomUpdateHelper.this.sendWifiRomUpdateChangedBroadcast();
            if (OppoWifiRomUpdateHelper.this.DEBUG) {
                Log.d(OppoWifiRomUpdateHelper.TAG, "\txml file parse end!");
            }
        }
    }

    private OppoWifiRomUpdateHelper(Context context) {
        super(context, FILE_NAME, SYS_FILE_PATH, DATA_FILE_PATH);
        setUpdateInfo(new WifiRomUpdateInfo(), new WifiRomUpdateInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized OppoWifiRomUpdateHelper getInstance(Context context) {
        OppoWifiRomUpdateHelper oppoWifiRomUpdateHelper;
        synchronized (OppoWifiRomUpdateHelper.class) {
            if (sInstance == null) {
                synchronized (OppoWifiRomUpdateHelper.class) {
                    if (sInstance == null) {
                        sInstance = new OppoWifiRomUpdateHelper(context);
                    }
                }
            }
            oppoWifiRomUpdateHelper = sInstance;
        }
        return oppoWifiRomUpdateHelper;
    }

    public String getValue(String key, String defaultVal) {
        String value = this.mKeyValuePair.get(key);
        if (value == null) {
            return defaultVal;
        }
        return value;
    }

    public Integer getIntegerValue(String key, Integer defaultVal) {
        String value = this.mKeyValuePair.get(key);
        if (value == null) {
            return defaultVal;
        }
        try {
            return Integer.valueOf(Integer.parseInt(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            return defaultVal;
        }
    }

    public boolean getBooleanValue(String key, boolean defaultVal) {
        Boolean result;
        String value = this.mKeyValuePair.get(key);
        Boolean.valueOf(defaultVal);
        if (value == null) {
            return defaultVal;
        }
        try {
            result = Boolean.valueOf(Boolean.parseBoolean(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            result = Boolean.valueOf(defaultVal);
        }
        return result.booleanValue();
    }

    public Double getFloatValue(String key, Double defaultVal) {
        String value = this.mKeyValuePair.get(key);
        if (value == null) {
            return defaultVal;
        }
        try {
            return Double.valueOf(Double.parseDouble(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            return defaultVal;
        }
    }

    public Long getLongValue(String key, Long defaultVal) {
        String value = this.mKeyValuePair.get(key);
        if (value == null) {
            return defaultVal;
        }
        try {
            return Long.valueOf(Long.parseLong(value));
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse exception:" + ex);
            return defaultVal;
        }
    }

    public void enableVerboseLogging(int level) {
        this.DEBUG = level > 0;
    }

    @Override // android.net.wifi.OppoRomUpdateHelper
    public void dump() {
        if (this.DEBUG) {
            Log.d(TAG, "dump:");
        }
        for (String key : this.mKeyValuePair.keySet()) {
            String value = this.mKeyValuePair.get(key);
            Log.d(TAG, "\t" + key + ":" + value);
        }
    }

    public String[] getMtuServer() {
        return this.mMtuServer;
    }

    public String[] getSlaWhiteListApps() {
        return this.mSlaApps;
    }

    public String[] getSlaWhiteListAppsExp() {
        return this.mSlaAppsExp;
    }

    public String[] getSlaBlackListApps() {
        return this.mSlaBlackList;
    }

    public String[] getSlaGameApps() {
        return this.mSlaGameApps;
    }

    public String[] getSlaGameAppsExp() {
        return this.mSlaGameAppsExp;
    }

    public String[] getSlaEnabledMcc() {
        return this.mSlaEnabledMCC;
    }

    public String[] getSlaParams() {
        return this.mSlaParams;
    }

    public int[] getSpeedRttParams() {
        return this.mSpeedRttParams;
    }

    /* access modifiers changed from: private */
    public void setSpeedRttParams(String text) {
        String[] params;
        if (text != null && (params = text.split(",")) != null && params.length == 11) {
            try {
                this.mSpeedRttParams = new int[]{Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Integer.parseInt(params[5]), Integer.parseInt(params[6]), Integer.parseInt(params[7]), Integer.parseInt(params[8]), Integer.parseInt(params[9]), Integer.parseInt(params[10])};
            } catch (Exception e) {
                Log.e(TAG, "setSpeedRttParams failed to parse params:" + text);
                this.mSpeedRttParams = null;
            }
        }
    }

    public String[] getSlaGameParams() {
        return this.mSlaGameParams;
    }

    public String[] getDualStaWhiteListApps() {
        return this.mDualStaApps;
    }

    public String[] getDualStaWhiteListAppsExp() {
        return this.mDualStaAppsExp;
    }

    public String[] getDualStaBlackListApps() {
        return this.mDualStaBlackList;
    }

    public String[] getDualStaBlackListCapHosts() {
        return this.mDualStaCapHostBlackList;
    }

    public String[] getDualStaDisabledMcc() {
        return this.mDualStaDisableMcc;
    }

    public String[] getAllVideoApps() {
        return this.mVideoApps;
    }

    public String[] getDownloadApps() {
        return this.mDownloadApps;
    }

    public String[] getSkipDestroySocketApps() {
        return this.mSkipDestroySocketApps;
    }

    public String[] getWechatLmParams() {
        return this.mLmParams;
    }

    /* access modifiers changed from: private */
    public void sendWifiRomUpdateChangedBroadcast() {
        Intent intent = new Intent("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED");
        intent.addFlags(67108864);
        if (this.mContext != null) {
            this.mContext.sendBroadcast(intent, OPPO_COMPONENT_SAFE_PERMISSION);
        }
    }

    /* access modifiers changed from: private */
    public void setDefaultMacRandomizationSetting(String value) {
        int result = 0;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            Log.d(TAG, "parse int exception:" + ex);
        }
        if (result == 0 || result == 1) {
            SystemProperties.set("persist.sys.wifi.mac_randomization", Integer.toString(result));
            return;
        }
        Log.d(TAG, "random mac value invalid!");
        SystemProperties.set("persist.sys.wifi.mac_randomization", Integer.toString(0));
    }
}
