package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.OppoBaseActivityInfo;
import android.content.pm.PackageParser;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.IoThread;
import com.android.server.am.ColorAppStartupManager;
import com.android.server.am.ColorInterceptSettings;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.IColorPackageManagerServiceEx;
import com.android.server.pm.IColorPackageManagerServiceInner;
import com.color.content.ColorRuleInfo;
import com.color.util.ColorTypeCastingHelper;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorIntentInterceptManager implements IColorIntentInterceptManager {
    private static final int BUFFER_SIZE = 128;
    private static boolean DEBUG_CII = DEBUG_PANIC;
    private static boolean DEBUG_PANIC = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static final String PKG_SOUGOU_INPUTMETHOD = "com.sohu.inputmethod.sogou";
    private static final String REGEX_PATTERN = "keyword=(.*?)&";
    private static final String SITE_SOUGOU = "https://wisd.sogou.com/web/searchList.jsp";
    private static final String TAG = "CII_ColorIntentInterceptManager";
    static final int WRITE_RULE_INFO_DELAY = 5000;
    private static volatile ColorIntentInterceptManager sColorIntentInterceptManager = null;
    protected ActivityTaskManagerService mAtms = null;
    private Object mCiimLock = new Object();
    private boolean mCiimTargetChanged = false;
    private List<String> mCiimTargetList = null;
    protected IColorActivityTaskManagerServiceEx mColorAtmsEx = null;
    private Handler mHandler = IoThread.getHandler();
    private IColorPackageManagerServiceInner mPmsInner;
    private List<ColorRuleInfo> mRuleInfoList;
    private Object mRuleLock = new Object();
    private List<String> mRuleTargetList = new ArrayList();
    private ColorInterceptSettings mSettings = new ColorInterceptSettings();
    private Runnable mWriteRuleRunnable = new Runnable() {
        /* class com.android.server.wm.ColorIntentInterceptManager.AnonymousClass1 */

        public void run() {
            Slog.i(ColorIntentInterceptManager.TAG, "write rule info to file");
            ColorIntentInterceptManager.this.writeRuleInfoToFile();
        }
    };

    private ColorIntentInterceptManager() {
    }

    public static ColorIntentInterceptManager getInstance() {
        if (sColorIntentInterceptManager == null) {
            synchronized (ColorIntentInterceptManager.class) {
                if (sColorIntentInterceptManager == null) {
                    sColorIntentInterceptManager = new ColorIntentInterceptManager();
                }
            }
        }
        return sColorIntentInterceptManager;
    }

    public void init(IColorActivityTaskManagerServiceEx atmsEx, IColorPackageManagerServiceEx pmsEx) {
        if (atmsEx == null || pmsEx == null) {
            Slog.e(TAG, "init error, amsEx = " + atmsEx + ", pmsEx = " + pmsEx);
            return;
        }
        this.mColorAtmsEx = atmsEx;
        this.mAtms = atmsEx.getActivityTaskManagerService();
        if (this.mAtms == null) {
            Slog.e(TAG, "init error, mAms is null");
            return;
        }
        this.mPmsInner = pmsEx.getColorPackageManagerServiceInner();
        if (this.mPmsInner == null) {
            Slog.e(TAG, "init error, mPmsInner is null");
            return;
        }
        ColorInterceptSettings colorInterceptSettings = this.mSettings;
        if (colorInterceptSettings != null) {
            this.mRuleInfoList = colorInterceptSettings.loadRuleList();
            extractTargetInfo();
        }
    }

    public Intent interceptGPIfNeeded(String resolvedType, int callingUid, int userId, String callingPackage, ActivityRecord aRecord, Intent intent, ActivityInfo aInfo) {
        if (this.mColorAtmsEx != null) {
            if (this.mAtms != null) {
                OppoBaseActivityInfo baseInfo = typeCasting(aInfo);
                boolean z = true;
                boolean needSkipIntercept = (intent.getFlags() & 256) != 0;
                if (DEBUG_CII) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("interceptGPIfNeeded needSkip = ");
                    sb.append(needSkipIntercept);
                    sb.append(" needCheckIntercept = ");
                    if (baseInfo == null || !baseInfo.needIntercept()) {
                        z = false;
                    }
                    sb.append(z);
                    Slog.i(TAG, sb.toString());
                }
                if (!needSkipIntercept && aInfo != null && baseInfo != null && baseInfo.needIntercept()) {
                    if (checkInteceptIntent(aInfo.getComponentName().flattenToString(), callingPackage, intent)) {
                        Intent newIntent = OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).handleInterceptActivity(aInfo, callingPackage, callingUid, userId, intent, resolvedType, getColorActivityRecordEx(aRecord));
                        Slog.i(TAG, "interceptGPIfNeeded, start intercept, newIntent = " + newIntent);
                        return newIntent;
                    }
                }
                return null;
            }
        }
        Slog.i(TAG, "interceptGPIfNeeded return, reason: mColorAmsEx = " + this.mColorAtmsEx + ", mAms = " + this.mAtms);
        return null;
    }

    private boolean setInterceptRuleInfosInternal(List<ColorRuleInfo> infos) {
        synchronized (this.mRuleLock) {
            this.mRuleInfoList.clear();
            if (infos != null && infos.size() > 0) {
                for (ColorRuleInfo info : infos) {
                    this.mRuleInfoList.add(info);
                }
            }
            extractTargetInfo();
        }
        scheduleWriteRuleInfo();
        return true;
    }

    public List<ColorRuleInfo> getInterceptRuleInfos() {
        List<ColorRuleInfo> list;
        synchronized (this.mRuleLock) {
            list = this.mRuleInfoList;
        }
        return list;
    }

    private void extractTargetInfo() {
        this.mRuleTargetList.clear();
        List<ColorRuleInfo> list = this.mRuleInfoList;
        if (list != null && list.size() > 0) {
            for (ColorRuleInfo info : this.mRuleInfoList) {
                if (info.mTargetComponentList != null && info.mTargetComponentList.size() > 0) {
                    for (String cpn : info.mTargetComponentList) {
                        this.mRuleTargetList.add(cpn);
                    }
                }
            }
        }
    }

    private List<String> getRuleTargetList() {
        List<String> list;
        synchronized (this.mRuleLock) {
            list = this.mRuleTargetList;
        }
        return list;
    }

    private boolean checkInteceptIntent(String cpn, String callingPkgName, Intent intent) {
        boolean needIntercept = false;
        if (cpn == null || intent == null) {
            Slog.e(TAG, "checkInterceptIntent invalid params");
        }
        synchronized (this.mRuleLock) {
            if (this.mRuleInfoList != null && this.mRuleInfoList.size() > 0) {
                Iterator<ColorRuleInfo> it = this.mRuleInfoList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    needIntercept = it.next().needIntercept(cpn, callingPkgName, intent);
                    if (needIntercept) {
                        break;
                    }
                }
            }
        }
        if (DEBUG_CII) {
            Slog.i(TAG, "checkInterceptIntent targetCpn = " + cpn + " sourcePkg = " + callingPkgName + " intent = " + intent.toString() + "needIntercept =" + needIntercept);
        }
        return needIntercept;
    }

    private void scheduleWriteRuleInfo() {
        Slog.i(TAG, "scheduleWriteRuleInfo");
        if (!this.mHandler.hasCallbacks(this.mWriteRuleRunnable)) {
            this.mHandler.postDelayed(this.mWriteRuleRunnable, 5000);
        }
    }

    /* access modifiers changed from: private */
    public void writeRuleInfoToFile() {
        synchronized (this.mRuleLock) {
            if (this.mSettings != null) {
                this.mSettings.writeLpr(this.mRuleInfoList);
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        if (1 < args.length) {
            int opti = 1 + 1;
            if ("log".equals(args[1]) && opti < args.length) {
                String state = args[opti];
                if ("enable".equals(state) || "on".equals(state)) {
                    setDebugState(pw, true);
                } else if ("disable".equals(state) || "off".equals(state)) {
                    setDebugState(pw, false);
                }
            }
        } else {
            pw.println("ColorIntentInterceptManager:");
            pw.println("  DEBUG_CII = " + DEBUG_CII);
            pw.println("  RuleInfoList :");
            synchronized (this.mRuleLock) {
                if (this.mRuleInfoList == null || this.mRuleInfoList.size() <= 0) {
                    pw.println("    NULL");
                } else {
                    for (ColorRuleInfo info : this.mRuleInfoList) {
                        info.dump(pw, "    ");
                    }
                }
            }
            pw.println("  Settings : ");
            ColorInterceptSettings colorInterceptSettings = this.mSettings;
            if (colorInterceptSettings != null) {
                colorInterceptSettings.dump(pw, args, "    ");
            }
        }
    }

    private void setDebugState(PrintWriter pw, boolean enable) {
        DEBUG_CII = enable;
        ColorInterceptSettings.setDeugEnable(enable);
        ColorRuleInfo.setDebugEnable(enable);
        pw.println("setDebugState enable = " + enable);
    }

    public boolean setInterceptRuleInfos(List<ColorRuleInfo> infos) {
        Slog.i(TAG, "setInterceptRuleInfos");
        markActivityInfo(getCIIMTargetList(), false);
        synchronized (this.mCiimLock) {
            this.mCiimTargetChanged = true;
        }
        setInterceptRuleInfosInternal(infos);
        markActivityInfo(getCIIMTargetList(), true);
        return true;
    }

    private void markActivityInfo(List<String> list, boolean set) {
        IColorPackageManagerServiceInner iColorPackageManagerServiceInner = this.mPmsInner;
        if (iColorPackageManagerServiceInner == null) {
            Slog.i(TAG, "markActivityInfo failure, mPmsInner is null!");
            return;
        }
        synchronized (iColorPackageManagerServiceInner.getPackages()) {
            if (list != null) {
                if (list.size() > 0) {
                    for (String name : list) {
                        PackageParser.Activity a = this.mPmsInner.getPackageParserActivity(ComponentName.unflattenFromString(name));
                        if (DEBUG_CII) {
                            Slog.i(TAG, "markActivityInfo cpn = " + name + ", set = " + set + ", a = " + a);
                        }
                        if (!(a == null || a.info == null)) {
                            OppoBaseActivityInfo baseInfo = typeCasting(a.info);
                            if (baseInfo == null) {
                                if (DEBUG_CII) {
                                    Slog.i(TAG, "markActivityInfo unsupport info!");
                                }
                            } else if (set) {
                                baseInfo.colorFlags |= 1;
                            } else {
                                baseInfo.colorFlags &= -2;
                            }
                        }
                    }
                }
            }
        }
    }

    public void markActivityInfoIfNeeded(ActivityInfo aInfo) {
        OppoBaseActivityInfo baseInfo = typeCasting(aInfo);
        if (aInfo == null || aInfo.getComponentName() == null) {
            Slog.i(TAG, "markActivityInfo return, aInfo : " + aInfo);
            return;
        }
        List<String> targetCpnList = getCIIMTargetList();
        if (targetCpnList != null && targetCpnList.contains(aInfo.getComponentName().flattenToString())) {
            if (DEBUG_CII) {
                Slog.i(TAG, "addActivity name : " + aInfo.name + " match intercept target");
            }
            baseInfo.colorFlags |= 1;
        }
    }

    private List<String> getCIIMTargetList() {
        synchronized (this.mCiimLock) {
            if (this.mCiimTargetChanged || this.mCiimTargetList == null) {
                this.mCiimTargetList = getRuleTargetList();
                this.mCiimTargetChanged = false;
            }
        }
        return this.mCiimTargetList;
    }

    public boolean interceptSougouSiteIfNeeded(String callingPackage, ActivityStack stack, Intent intent) {
        Uri uri;
        String key;
        if (!PKG_SOUGOU_INPUTMETHOD.equals(callingPackage) || stack == null) {
            return false;
        }
        ActivityRecord ar = stack.topRunningActivityLocked();
        String keyword = null;
        if (!(ar == null || !ColorAppStartupManager.BROWSER_LIST.contains(ar.packageName) || (uri = intent.getData()) == null)) {
            StringBuilder sb = new StringBuilder((int) BUFFER_SIZE);
            sb.append(uri.getScheme());
            sb.append("://");
            sb.append(uri.getHost());
            sb.append(uri.getPath());
            String site = sb.toString();
            Slog.i(TAG, "site::" + site);
            List<String> sites = OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).getSougouSite();
            if (sites != null && sites.isEmpty()) {
                sites.add(SITE_SOUGOU);
            }
            for (String webSite : sites) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "site in sites::" + webSite);
                }
            }
            if (sites.contains(site) && (key = uri.getQuery()) != null) {
                Matcher m = Pattern.compile(REGEX_PATTERN).matcher(key);
                if (m.find()) {
                    keyword = m.group(1);
                }
            }
        }
        if (keyword == null) {
            return false;
        }
        OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).sendOppoSiteMsg(keyword);
        return true;
    }

    private OppoBaseActivityInfo typeCasting(ActivityInfo info) {
        if (info != null) {
            return (OppoBaseActivityInfo) ColorTypeCastingHelper.typeCasting(OppoBaseActivityInfo.class, info);
        }
        return null;
    }

    private OppoBaseActivityRecord typeCasting(ActivityRecord ar) {
        if (ar != null) {
            return (OppoBaseActivityRecord) ColorTypeCastingHelper.typeCasting(OppoBaseActivityRecord.class, ar);
        }
        return null;
    }

    private IColorActivityRecordEx getColorActivityRecordEx(ActivityRecord ar) {
        OppoBaseActivityRecord baseAr = typeCasting(ar);
        if (baseAr != null) {
            return baseAr.mColorArEx;
        }
        return null;
    }
}
