package com.android.server.wm;

import android.content.ComponentName;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.net.Uri;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.OppoBaseEnvironment;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.Display;
import android.view.DisplayInfo;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.theia.NoFocusWindow;
import com.android.server.wm.OppoRefreshRateConstants;
import com.oppo.RomUpdateHelper;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;

public class OppoDisplayModeService extends IOppoScreenModeHook {
    private static final String DATA_FILE_DIR = "data/system/refresh_rate_config.xml";
    private static final String OPPO_PRODUCT_DIR = OppoBaseEnvironment.getOppoProductDirectory().getAbsolutePath();
    private static final String SYS_FILE_DIR = (OPPO_PRODUCT_DIR + "/etc/refresh_rate_config.xml");
    private SparseArray<DeathRecipient> mActiveDeathRecipients;
    private Context mContext;
    private DisplayInfo mDefaultDisplayInfo;
    private final DisplayManager.DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    private boolean mEnable;
    private boolean mInit;
    private final Object mLock;
    private final HashMap<String, Item> mMap;
    private SettingsObserver mSettingsObserver;
    private int mSupportMaxRefreshId;
    private final ArrayList<String> mTPBoostList;
    private RefreshRateConfigUpdater mUpdater;
    private SparseArray<Vote> mVotes;

    private OppoDisplayModeService() {
        this.mMap = new HashMap<>();
        this.mTPBoostList = new ArrayList<>();
        this.mLock = new Object();
        this.mDisplayListener = new DisplayManager.DisplayListener() {
            /* class com.android.server.wm.OppoDisplayModeService.AnonymousClass1 */
            private ArraySet<Integer> mLowRateDisplayId = new ArraySet<>();
            private ArrayList<String> mLowRatePkgList = new ArrayList<>(Arrays.asList("com.tencent.tmgp.speedmobile", "com.coloros.screenrecorder", "com.tencent.af", "com.miHoYo.bh3.nearme.gamecenter"));

            public void onDisplayAdded(int displayId) {
                try {
                    Display display = OppoDisplayModeService.this.mDisplayManager.getDisplay(displayId);
                    int type = display.getType();
                    if (2 == type || 3 == type || (5 == type && this.mLowRatePkgList.contains(display.getOwnerPackageName()))) {
                        this.mLowRateDisplayId.add(Integer.valueOf(displayId));
                    }
                    updateRefreshRate();
                } catch (Exception e) {
                }
            }

            public void onDisplayRemoved(int displayId) {
                this.mLowRateDisplayId.remove(Integer.valueOf(displayId));
                updateRefreshRate();
            }

            public void onDisplayChanged(int displayId) {
            }

            private void updateRefreshRate() {
                int ret;
                Vote vote = null;
                if (this.mLowRateDisplayId.size() > 0) {
                    vote = Vote.forRefreshRate(2);
                }
                synchronized (OppoDisplayModeService.this.mLock) {
                    OppoDisplayModeService.this.updateVoteLocked(3, vote);
                    ret = OppoDisplayModeService.this.getFinalDisplayModeIdLocked(OppoDisplayModeService.this.mVotes, OppoDisplayModeService.this.mDefaultDisplayInfo);
                }
                if (ret > 0) {
                    Slog.w("RefreshRate", "onDisplayChanged: votes=" + OppoDisplayModeService.this.dumpVotes() + ": useModeId=" + ret);
                    OppoDisplayModeService.this.notifyDisplayManager(ret);
                }
            }
        };
    }

    public static OppoDisplayModeService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public void init(Context context) {
        synchronized (this) {
            if (!this.mInit) {
                this.mInit = true;
                this.mEnable = true;
                this.mContext = context;
                this.mVotes = new SparseArray<>();
                this.mUpdater = new RefreshRateConfigUpdater(context);
                this.mUpdater.initUpdateBroadcastReceiver();
                this.mSettingsObserver = new SettingsObserver(context);
                this.mSettingsObserver.observe();
                this.mSupportMaxRefreshId = OppoRefreshRateUtils.getMaxRefreshRateId(this.mContext);
                this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
                ServiceManager.addService("opposcreenmode", asBinder());
            }
        }
    }

    private void setFeatureEnable(boolean enable) {
        this.mEnable = enable;
    }

    /* access modifiers changed from: package-private */
    public void setDefaultDisplayInfo(DisplayInfo info) {
        this.mDefaultDisplayInfo = info;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int opti;
        if (DumpUtils.checkDumpPermission(this.mContext, "RefreshRate", pw)) {
            if (0 < args.length) {
                String cmd = args[0];
                if ("set_enable".equals(cmd)) {
                    int opti2 = 0 + 1;
                    if (opti2 < args.length) {
                        setFeatureEnable(TemperatureProvider.SWITCH_ON.equals(args[opti2]));
                    }
                } else if (!this.mEnable) {
                    pw.println("Disabled");
                } else if ("get_config".equals(cmd)) {
                    int opti3 = 0 + 1;
                    if (opti3 < args.length) {
                        String input = args[opti3];
                        ComponentName cmp = ComponentName.unflattenFromString(input);
                        Item item = this.mMap.get(cmp != null ? Item.getTag(cmp.getPackageName(), cmp.getClassName()) : input);
                        pw.println(item != null ? item.toString() : "Not Found");
                    }
                } else if ("set_config".equals(cmd)) {
                    if (0 + 2 < args.length) {
                        String target = args[0 + 1];
                        String mode = args[0 + 2];
                        ComponentName cmp2 = ComponentName.unflattenFromString(target);
                        Item item2 = new Item(cmp2 != null ? cmp2.getPackageName() : target, cmp2 != null ? cmp2.getClassName() : null, mode);
                        this.mMap.put(item2.getTag(), item2);
                    }
                } else if ("set_debug".equals(cmd)) {
                    int opti4 = 0 + 1;
                    if (opti4 < args.length) {
                        OppoRefreshRateConstants.DEBUG = args[opti4].equals(NoFocusWindow.HUNG_CONFIG_ENABLE);
                    }
                } else if ("simulate_ratingapp".equals(cmd)) {
                    int opti5 = 0 + 1;
                    if (opti5 < args.length) {
                        boolean open = args[opti5].equals(NoFocusWindow.HUNG_CONFIG_ENABLE);
                        int rateId = 0;
                        if (open) {
                            try {
                                rateId = Integer.parseInt(args[opti5 + 1]);
                            } catch (Exception e) {
                            }
                        }
                        requestRefreshRate(open, rateId);
                    }
                } else if ("simulate_hightemprature".equals(cmd) && (opti = 0 + 1) < args.length) {
                    setHighTemperatureStatus(args[opti].equals(NoFocusWindow.HUNG_CONFIG_ENABLE) ? 1 : 0, 0);
                }
            } else if (!this.mEnable) {
                pw.println("Disabled");
            } else {
                pw.println("Current Version:" + this.mUpdater.CURRENT_VERSION);
                pw.println("  mVotes:");
                for (int p = 3; p >= 0; p--) {
                    Vote vote = this.mVotes.get(p);
                    if (vote != null) {
                        pw.println("      " + Vote.priorityToString(p) + " -> " + vote);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setVendorPreferredRefreshRate(WindowState w) {
        Item item;
        if (this.mEnable && (item = findPolicyItem(w)) != null) {
            w.mRefreshRateData.init(item.mPreferredRefreshRateId);
        }
    }

    /* access modifiers changed from: package-private */
    public int getCurrentSettingMode() {
        return this.mSettingsObserver.getSettingMode();
    }

    /* access modifiers changed from: package-private */
    public boolean needTpBoost(String pkg) {
        return this.mTPBoostList.contains(pkg);
    }

    /* access modifiers changed from: package-private */
    public int getPreferredModeId(int windowPreferredId, DisplayInfo info) {
        if (!this.mEnable) {
            return 0;
        }
        int windowPreferredId2 = OppoRefreshRateUtils.minRefreshRateId(windowPreferredId, this.mSupportMaxRefreshId);
        int ret = 0;
        if (windowPreferredId2 > 0) {
            synchronized (this.mLock) {
                updateVoteLocked(0, Vote.forRefreshRate(windowPreferredId2));
                ret = getFinalDisplayModeIdLocked(this.mVotes, info);
            }
        }
        if (ret > 0 && OppoRefreshRateConstants.DEBUG) {
            Slog.w("RefreshRate", "app request modeId: votes=" + dumpVotes() + ": useModeId=" + ret);
        }
        return ret;
    }

    private Item findPolicyItem(WindowState w) {
        return findPolicyItem(w.getOwningPackage(), w.mAppToken != null ? w.mAppToken.mActivityComponent.getClassName() : null);
    }

    private Item findPolicyItem(String pkgName, String actName) {
        long startTime = SystemClock.uptimeMillis();
        Item item = this.mMap.get(Item.getTag(pkgName, actName));
        if (item == null && actName != null) {
            item = this.mMap.get(Item.getTag(pkgName, null));
        }
        int elapse = (int) (SystemClock.uptimeMillis() - startTime);
        if (elapse > 1 && OppoRefreshRateConstants.DEBUG) {
            Slog.d("RefreshRate", "check app " + pkgName + " took " + elapse + " ms");
        }
        return item;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String dumpVotes() {
        StringBuilder sb = new StringBuilder();
        for (int p = 3; p >= 0; p--) {
            Vote vote = this.mVotes.get(p);
            if (vote != null) {
                sb.append(Vote.priorityToString(p) + ":" + vote);
                sb.append(";");
            }
        }
        return sb.toString();
    }

    private int[] getDisplayModeId(Display.Mode[] supportedModes, int width, int height, float minRefreshRate, float maxRefreshRate) {
        ArrayList<Display.Mode> availableModes = new ArrayList<>();
        for (Display.Mode mode : supportedModes) {
            if (mode.getPhysicalWidth() == width && mode.getPhysicalHeight() == height) {
                float refreshRate = mode.getRefreshRate();
                if (refreshRate >= minRefreshRate - 1.0f && refreshRate <= maxRefreshRate + 1.0f) {
                    availableModes.add(mode);
                }
            }
        }
        int size = availableModes.size();
        int[] availableModeIds = new int[size];
        for (int i = 0; i < size; i++) {
            availableModeIds[i] = availableModes.get(i).getModeId();
        }
        return availableModeIds;
    }

    private boolean checkRefreshRateId(int refreshRateId) {
        if (refreshRateId >= 1 && refreshRateId <= 3) {
            return true;
        }
        Slog.w("RefreshRate", "Received an invalid refreshRateId, ignoring", new Throwable());
        return false;
    }

    @Override // com.android.server.wm.IOppoScreenModeHook
    public String getDisableOverrideViewList(String key) {
        String result = null;
        Item item = findPolicyItem(key, null);
        if (item == null) {
            return null;
        }
        if (item.mDisableViewOverride) {
            result = "All";
        }
        if (!TextUtils.isEmpty(item.mDisableOverrideViewList)) {
            return item.mDisableOverrideViewList;
        }
        return result;
    }

    @Override // com.android.server.wm.IOppoScreenModeHook
    public boolean requestRefreshRate(boolean open, int rate) {
        return requestRefreshRateWithToken(open, rate, null);
    }

    @Override // com.android.server.wm.IOppoScreenModeHook
    public boolean requestRefreshRateWithToken(boolean open, int rate, IBinder token) {
        int ret;
        if (open && !checkRefreshRateId(rate)) {
            return false;
        }
        Vote vote = null;
        if (open) {
            vote = Vote.forRefreshRate(rate);
        }
        synchronized (this.mLock) {
            updateVoteLocked(1, vote);
            ret = getFinalDisplayModeIdLocked(this.mVotes, this.mDefaultDisplayInfo);
        }
        if (token != null) {
            DeathRecipient death = getOrCreateDeathRecipient(1);
            if (open) {
                death.relink(token);
            } else {
                death.unlinkDeath();
            }
        }
        if (ret <= 0) {
            return false;
        }
        Slog.w("RefreshRate", "requestRefreshRate: votes=" + dumpVotes() + ": useModeId=" + ret);
        notifyDisplayManager(ret);
        return true;
    }

    @Override // com.android.server.wm.IOppoScreenModeHook
    public boolean setHighTemperatureStatus(int status, int rate) {
        int ret;
        Vote vote = null;
        if (status != 0) {
            vote = Vote.forRefreshRate(2);
        }
        synchronized (this.mLock) {
            updateVoteLocked(2, vote);
            ret = getFinalDisplayModeIdLocked(this.mVotes, this.mDefaultDisplayInfo);
        }
        if (ret <= 0) {
            return false;
        }
        Slog.w("RefreshRate", "setHighTemperatureStatus: votes=" + dumpVotes() + ": useModeId=" + ret);
        notifyDisplayManager(ret);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVoteLocked(int priority, Vote vote) {
        if (priority < 0 || priority > 3) {
            Slog.w("RefreshRate", "Received a vote with an invalid priority, ignoring: priority=" + Vote.priorityToString(priority) + ", vote=" + vote, new Throwable());
            return;
        }
        SparseArray<Vote> votes = this.mVotes;
        if (vote != null) {
            votes.put(priority, vote);
        } else {
            votes.remove(priority);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getFinalDisplayModeIdLocked(SparseArray<Vote> votes, DisplayInfo info) {
        int refreshRateId = -1;
        int priority = 3;
        while (true) {
            if (priority >= 0) {
                Vote vote = votes.get(priority);
                if (vote != null) {
                    refreshRateId = vote.refreshRateId;
                    break;
                }
                priority--;
            } else {
                break;
            }
        }
        if (refreshRateId <= 0) {
            return 0;
        }
        float refreshRate = OppoRefreshRateUtils.getRefreshRateById(refreshRateId);
        Display.Mode defaultMode = info.getDefaultMode();
        int[] availableModeIds = getDisplayModeId(info.supportedModes, defaultMode.getPhysicalWidth(), defaultMode.getPhysicalHeight(), refreshRate, refreshRate);
        if (availableModeIds == null || availableModeIds.length <= 0) {
            return 0;
        }
        return availableModeIds[0];
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyDisplayManager(int displayModeId) {
        ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class)).setDisplayProperties(0, true, (float) OppoBrightUtils.MIN_LUX_LIMITI, displayModeId, false);
    }

    private DeathRecipient getOrCreateDeathRecipient(int type) {
        if (this.mActiveDeathRecipients == null) {
            this.mActiveDeathRecipients = new SparseArray<>();
        }
        if (this.mActiveDeathRecipients.indexOfKey(type) >= 0) {
            return this.mActiveDeathRecipients.get(type);
        }
        DeathRecipient death = new DeathRecipient(type);
        this.mActiveDeathRecipients.put(type, death);
        return death;
    }

    /* access modifiers changed from: private */
    public static final class Vote {
        public static final int MAX_PRIORITY = 3;
        public static final int MIN_PRIORITY = 0;
        public static final int PRIORITY_APP_REQUEST = 0;
        public static final int PRIORITY_HIGH_TEMPERATURE = 2;
        public static final int PRIORITY_LOWRATE_DISPLAY = 3;
        public static final int PRIORITY_RATING_TEST = 1;
        public final int refreshRateId;

        private Vote(int refreshRateId2) {
            this.refreshRateId = refreshRateId2;
        }

        public static Vote forRefreshRate(int refreshRateId2) {
            return new Vote(refreshRateId2);
        }

        public static String priorityToString(int priority) {
            if (priority == 0) {
                return "PRIORITY_APP_REQUEST";
            }
            if (priority == 1) {
                return "PRIORITY_RATING_TEST";
            }
            if (priority == 2) {
                return "PRIORITY_HIGH_TEMPERATURE";
            }
            if (priority != 3) {
                return Integer.toString(priority);
            }
            return "PRIORITY_LOWRATE_DISPLAY";
        }

        public String toString() {
            return "Vote{refreshRateId=" + this.refreshRateId + "}";
        }
    }

    /* access modifiers changed from: private */
    public static class LazyHolder {
        private static final OppoDisplayModeService INSTANCE = new OppoDisplayModeService();

        private LazyHolder() {
        }
    }

    /* access modifiers changed from: private */
    public static class Item {
        String mActivityName;
        String mDisableOverrideViewList;
        boolean mDisableViewOverride;
        String mPkgName;
        private OppoRefreshRateConstants.PreferredRefreshRateData mPreferredRefreshRateId;

        Item(String pkgName, String actName, String refreshRateStr) {
            this(pkgName, actName, refreshRateStr, false, null);
        }

        Item(String pkgName, String actName, String refreshRateStr, boolean disableOverride, String viewList) {
            this.mPkgName = pkgName;
            this.mActivityName = actName;
            this.mPreferredRefreshRateId = OppoRefreshRateUtils.parsePreferredRefreshRate(refreshRateStr);
            this.mDisableOverrideViewList = viewList;
            this.mDisableViewOverride = disableOverride;
        }

        static String getTag(String pkgName, String actName) {
            if (TextUtils.isEmpty(actName)) {
                return pkgName;
            }
            return pkgName + SliceClientPermissions.SliceAuthority.DELIMITER + actName;
        }

        /* access modifiers changed from: package-private */
        public String getTag() {
            return getTag(this.mPkgName, this.mActivityName);
        }

        public String toString() {
            return "Item{mPkgName='" + this.mPkgName + "', mActivityName='" + this.mActivityName + "', mPreferredRefreshRateId=" + this.mPreferredRefreshRateId + ", mDisableViewOverride=" + this.mDisableViewOverride + ", mDisableOverrideViewList=" + this.mDisableOverrideViewList + '}';
        }
    }

    /* access modifiers changed from: private */
    public static class SettingsObserver extends ContentObserver {
        private final String REFRESH_RATE_SETTING;
        private final Context mContext;
        private int mCurrentPeakMode;
        private final Uri mRefreshRateSetting;

        SettingsObserver(Context context) {
            this(context, new Handler());
        }

        SettingsObserver(Context context, Handler handler) {
            super(handler);
            this.REFRESH_RATE_SETTING = "coloros_screen_refresh_rate";
            this.mRefreshRateSetting = Settings.Secure.getUriFor("coloros_screen_refresh_rate");
            this.mContext = context;
        }

        public void observe() {
            this.mContext.getContentResolver().registerContentObserver(this.mRefreshRateSetting, false, this, 0);
            onRefreshRateSettingChange();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (this.mRefreshRateSetting.equals(uri)) {
                onRefreshRateSettingChange();
            }
        }

        private void onRefreshRateSettingChange() {
            this.mCurrentPeakMode = Settings.Secure.getInt(this.mContext.getContentResolver(), "coloros_screen_refresh_rate", 0);
        }

        /* access modifiers changed from: package-private */
        public int getSettingMode() {
            return this.mCurrentPeakMode;
        }
    }

    /* access modifiers changed from: private */
    public class DeathRecipient implements IBinder.DeathRecipient {
        private IBinder token;
        private int type;

        DeathRecipient(int type2) {
            this.type = type2;
        }

        public void binderDied() {
            if (this.type == 1) {
                OppoDisplayModeService.this.requestRefreshRate(false, 0);
            }
        }

        /* access modifiers changed from: package-private */
        public void relink(IBinder token2) {
            if (this.token != token2) {
                unlinkDeath();
                this.token = token2;
                linkDeath();
            }
        }

        private void linkDeath() {
            try {
                if (this.token != null) {
                    this.token.linkToDeath(this, 0);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /* access modifiers changed from: package-private */
        public void unlinkDeath() {
            IBinder iBinder = this.token;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
                this.token = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public class RefreshRateConfigUpdater extends RomUpdateHelper {
        private static final String ATTR_ACTNAME = "activityName";
        private static final String ATTR_DISABLEOVERRIDE = "disableViewOverride";
        private static final String ATTR_PKGNAME = "packageName";
        private static final String ATTR_PREFERREDREFRESH = "preferredRefreshRateId";
        private static final String ATTR_VERSION = "version";
        private static final String ATTR_VIEWLIST = "viewList";
        private static final String FILTER_NAME = "sys_refresh_rate_config";
        private static final String TAG = "RefreshRateConfig";
        private static final String TAG_CONFIG = "refresh_rate_config";
        private static final String TAG_ITEM = "item";
        private static final String TAG_TP_ITEM = "tpitem";
        private int CURRENT_VERSION;

        private RefreshRateConfigUpdater(Context context) {
            super(context, FILTER_NAME, OppoDisplayModeService.SYS_FILE_DIR, OppoDisplayModeService.DATA_FILE_DIR);
            this.CURRENT_VERSION = 1;
            setUpdateInfo(new ConfigUpdateInfo(), new ConfigUpdateInfo());
            new Thread(new Runnable(OppoDisplayModeService.this) {
                /* class com.android.server.wm.OppoDisplayModeService.RefreshRateConfigUpdater.AnonymousClass1 */

                public void run() {
                    try {
                        RefreshRateConfigUpdater.this.init();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        public void init() {
            String data_config_content = getFileContent(OppoDisplayModeService.DATA_FILE_DIR);
            int data_config_version = getConfigVersion(data_config_content);
            String system_config_content = getFileContent(OppoDisplayModeService.SYS_FILE_DIR);
            String content = getConfigVersion(system_config_content) > data_config_version ? system_config_content : data_config_content;
            if (getUpdateInfo(true) != null) {
                getUpdateInfo(true).parseContentFromXML(content);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Item restoreRow(XmlPullParser parser, int depth) throws IOException {
            return new Item(XmlUtils.readStringAttribute(parser, "packageName"), XmlUtils.readStringAttribute(parser, ATTR_ACTNAME), XmlUtils.readStringAttribute(parser, ATTR_PREFERREDREFRESH), XmlUtils.readBooleanAttribute(parser, ATTR_DISABLEOVERRIDE, false), XmlUtils.readStringAttribute(parser, ATTR_VIEWLIST));
        }

        private int getConfigVersion(String content) {
            if (TextUtils.isEmpty(content)) {
                return 0;
            }
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(new StringReader(content));
                XmlUtils.beginDocument(parser, TAG_CONFIG);
                return XmlUtils.readIntAttribute(parser, "version");
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        private String getFileContent(String path) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    return FileUtils.readTextFile(file, 0, null);
                }
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        private class ConfigUpdateInfo extends RomUpdateHelper.UpdateInfo {
            private ConfigUpdateInfo() {
                super(RefreshRateConfigUpdater.this);
            }

            /* JADX WARNING: Removed duplicated region for block: B:22:0x008f A[Catch:{ Exception -> 0x00be }] */
            /* JADX WARNING: Removed duplicated region for block: B:25:0x00a4 A[Catch:{ Exception -> 0x00be }] */
            public void parseContentFromXML(String content) {
                if (!TextUtils.isEmpty(content)) {
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(new StringReader(content));
                        XmlUtils.beginDocument(parser, RefreshRateConfigUpdater.TAG_CONFIG);
                        int version = XmlUtils.readIntAttribute(parser, "version");
                        if (version > RefreshRateConfigUpdater.this.CURRENT_VERSION) {
                            Log.i(RefreshRateConfigUpdater.TAG, "update refresh rate config oldver:" + RefreshRateConfigUpdater.this.CURRENT_VERSION + ", newver:" + version);
                            RefreshRateConfigUpdater.this.CURRENT_VERSION = version;
                            OppoDisplayModeService.this.mMap.clear();
                            int depth = parser.getDepth();
                            while (XmlUtils.nextElementWithin(parser, depth)) {
                                String name = parser.getName();
                                char c = 65535;
                                int hashCode = name.hashCode();
                                if (hashCode != -867308657) {
                                    if (hashCode == 3242771 && name.equals("item")) {
                                        c = 0;
                                        if (c == 0) {
                                            Item item = RefreshRateConfigUpdater.this.restoreRow(parser, depth + 1);
                                            OppoDisplayModeService.this.mMap.put(item.getTag(), item);
                                        } else if (c == 1) {
                                            OppoDisplayModeService.this.mTPBoostList.add(XmlUtils.readStringAttribute(parser, "packageName"));
                                        }
                                    }
                                } else if (name.equals(RefreshRateConfigUpdater.TAG_TP_ITEM)) {
                                    c = 1;
                                    if (c == 0) {
                                    }
                                }
                                if (c == 0) {
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(RefreshRateConfigUpdater.TAG, "update refresh rate config failed! error:" + e.getMessage());
                    }
                }
            }
        }
    }
}
