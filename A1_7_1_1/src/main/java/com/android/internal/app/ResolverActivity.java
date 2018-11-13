package com.android.internal.app;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.VoiceInteractor.PickOptionRequest;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.app.VoiceInteractor.Prompt;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.AuthorityEntry;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.ims.ImsConferenceState;
import com.android.internal.R;
import com.android.internal.app.AlertController.AlertParams;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.widget.ResolverDrawerLayout;
import com.color.oshare.ColorOshareDevice;
import com.color.oshare.ColorOshareServiceUtil;
import com.color.oshare.IColorOshareCallback;
import com.color.oshare.IColorOshareInitListener;
import com.color.oshare.IColorOshareInitListener.Stub;
import com.color.widget.ColorDotView;
import com.color.widget.ColorLinearLayoutManager;
import com.color.widget.ColorRecyclerView;
import com.color.widget.ColorRecyclerView.Adapter;
import com.color.widget.ColorResolverDialogHelper;
import com.color.widget.ColorResolverDialogViewPager;
import com.color.widget.ColorTransferProgress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import oppo.util.OppoMultiLauncherUtil;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@OppoHook(level = OppoHookType.CHANGE_BASE_CLASS, note = "Changwei.Li@Plf.SDK, 2015-01-07 : Modify for ColorOS Resolver Style", property = OppoRomType.ROM)
public class ResolverActivity extends AlertActivity {
    private static final boolean DEBUG = false;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "ResolverActivity";
    private ResolveListAdapter mAdapter;
    private AbsListView mAdapterView;
    private Button mAlwaysButton;
    private boolean mAlwaysUseOption;
    private boolean mBegineOshare;
    private ArrayList<ColorOshareDevice> mDeviceList;
    private ComponentName[] mFilteredComponents;
    private int mIconDpi;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "JianHui.Yu@Plf.SDK, 2016-06-16 : Add for Upgrade to Android6.0", property = OppoRomType.ROM)
    private int mIconSize;
    private final ArrayList<Intent> mIntents;
    private boolean mIsActionSend;
    private boolean mIsOshareOn;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Fei.Wang@ROM.SysApp.Graphics, 2014-10-08 : Add for short click the item in the dialog", property = OppoRomType.ROM)
    private boolean mItemClickFlag;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK, 2014-08-14 : Add for Long click the item in the file management,then click the back key,the android stop the service", property = OppoRomType.ROM)
    private boolean mItemLongClickFlag;
    private int mLastSelected;
    protected String mLaunchedFromPackage;
    private int mLaunchedFromUid;
    private View mNoticeHelpView;
    private View mNoticeOpenOshareView;
    private IColorOshareCallback mOShareCallback;
    private IColorOshareInitListener mOShareInitListener;
    private boolean mOShareServiceInited;
    private ColorOshareServiceUtil mOShareServiceUtil;
    private Button mOnceButton;
    private View mOpenOsharePanel;
    private View mOpenWifiBlueToothView;
    private View mOshareIcon;
    private View mOshareingPanel;
    private final PackageMonitor mPackageMonitor;
    private PickTargetOptionRequest mPickOptionRequest;
    private PackageManager mPm;
    private int mProfileSwitchMessageId;
    private View mProfileView;
    private ColorRecyclerView mRecyclerView;
    private boolean mRegistered;
    private ResolverComparator mResolverComparator;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Shuai.Zhang@Plf.SDK, 2016-01-20 : Add for resolverDialog", property = OppoRomType.ROM)
    private ColorResolverDialogHelper mResolverDialogHelper;
    protected ResolverDrawerLayout mResolverDrawerLayout;
    private ResolverOshareingAdapter mResolverOshareingAdapter;
    private boolean mResolvingHome;
    private boolean mSafeForwardingMode;

    public class ResolveListAdapter extends BaseAdapter {
        private final List<ResolveInfo> mBaseResolveList;
        List<DisplayResolveInfo> mDisplayList;
        private boolean mFilterLastUsed;
        private boolean mHasExtendedInfo;
        protected final LayoutInflater mInflater;
        private final Intent[] mInitialIntents;
        private final List<Intent> mIntents;
        private ResolveInfo mLastChosen;
        private int mLastChosenPosition = -1;
        private final int mLaunchedFromUid;
        List<ResolvedComponentInfo> mOrigResolveList;
        private DisplayResolveInfo mOtherProfile;

        public ResolveListAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
            this.mIntents = payloadIntents;
            this.mInitialIntents = initialIntents;
            this.mBaseResolveList = rList;
            this.mLaunchedFromUid = launchedFromUid;
            this.mInflater = LayoutInflater.from(context);
            this.mDisplayList = new ArrayList();
            this.mFilterLastUsed = filterLastUsed;
            rebuildList();
        }

        public void handlePackagesChanged() {
            rebuildList();
            notifyDataSetChanged();
            if (getCount() == 0) {
                ResolverActivity.this.finish();
            }
        }

        public DisplayResolveInfo getFilteredItem() {
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return null;
            }
            return (DisplayResolveInfo) this.mDisplayList.get(this.mLastChosenPosition);
        }

        public DisplayResolveInfo getOtherProfile() {
            return this.mOtherProfile;
        }

        public int getFilteredPosition() {
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return -1;
            }
            return this.mLastChosenPosition;
        }

        public boolean hasFilteredItem() {
            return this.mFilterLastUsed && this.mLastChosenPosition >= 0;
        }

        public float getScore(DisplayResolveInfo target) {
            return ResolverActivity.this.mResolverComparator.getScore(target.getResolvedComponentName());
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Hairong.Zou@Plf.SDK, 2014-03-24 : Add for System APP and User APP sorting seperately; Suying.You@Plf.SDK,2014-07-16: Add for solve the default selected APK invalid", property = OppoRomType.ROM)
        private void rebuildList() {
            int N;
            int i;
            ResolveInfo ri;
            ActivityInfo ai;
            UserManager userManager;
            LabeledIntent li;
            List currentResolveList = null;
            try {
                Intent primaryIntent = ResolverActivity.this.getTargetIntent();
                this.mLastChosen = AppGlobals.getPackageManager().getLastChosenActivity(primaryIntent, primaryIntent.resolveTypeIfNeeded(ResolverActivity.this.getContentResolver()), 65536);
            } catch (RemoteException re) {
                Log.d(ResolverActivity.TAG, "Error calling setLastChosenActivity\n" + re);
            }
            this.mOtherProfile = null;
            this.mDisplayList.clear();
            if (this.mBaseResolveList != null) {
                currentResolveList = new ArrayList();
                this.mOrigResolveList = currentResolveList;
                addResolveListDedupe(currentResolveList, ResolverActivity.this.getTargetIntent(), this.mBaseResolveList);
            } else {
                boolean shouldGetResolvedFilter = shouldGetResolvedFilter();
                boolean shouldGetActivityMetadata = ResolverActivity.this.shouldGetActivityMetadata();
                N = this.mIntents.size();
                for (i = 0; i < N; i++) {
                    Intent intent = (Intent) this.mIntents.get(i);
                    List<ResolveInfo> infos = ResolverActivity.this.mPm.queryIntentActivities(intent, (shouldGetActivityMetadata ? 128 : 0) | (65536 | (shouldGetResolvedFilter ? 64 : 0)));
                    if (infos != null) {
                        if (currentResolveList == null) {
                            currentResolveList = new ArrayList();
                            this.mOrigResolveList = currentResolveList;
                        }
                        addResolveListDedupe(currentResolveList, intent, infos);
                    }
                }
                if (currentResolveList != null) {
                    for (i = currentResolveList.size() - 1; i >= 0; i--) {
                        ComponentInfo ai2 = ((ResolvedComponentInfo) currentResolveList.get(i)).getResolveInfoAt(0).activityInfo;
                        int granted = ActivityManager.checkComponentPermission(ai2.permission, this.mLaunchedFromUid, ai2.applicationInfo.uid, ai2.exported);
                        boolean suspended = (ai2.applicationInfo.flags & 1073741824) != 0;
                        if (granted != 0 || suspended || ResolverActivity.this.isComponentFiltered(ai2)) {
                            if (this.mOrigResolveList == currentResolveList) {
                                this.mOrigResolveList = new ArrayList(this.mOrigResolveList);
                            }
                            currentResolveList.remove(i);
                        }
                    }
                }
            }
            if (currentResolveList != null) {
                N = currentResolveList.size();
                if (N > 0) {
                    ResolveInfo r0 = ((ResolvedComponentInfo) currentResolveList.get(0)).getResolveInfoAt(0);
                    for (i = 1; i < N; i++) {
                        ri = ((ResolvedComponentInfo) currentResolveList.get(i)).getResolveInfoAt(0);
                        if (r0.priority != ri.priority || r0.isDefault != ri.isDefault) {
                            while (i < N) {
                                if (this.mOrigResolveList == currentResolveList) {
                                    this.mOrigResolveList = new ArrayList(this.mOrigResolveList);
                                }
                                currentResolveList.remove(i);
                                N--;
                            }
                        }
                    }
                    if (N > 1) {
                        ResolverActivity.this.mResolverComparator.compute(currentResolveList);
                        Collections.sort(currentResolveList, ResolverActivity.this.mResolverComparator);
                    }
                    if (this.mInitialIntents != null) {
                        for (Intent ii : this.mInitialIntents) {
                            if (ii != null) {
                                ai = ii.resolveActivityInfo(ResolverActivity.this.getPackageManager(), 0);
                                if (ai == null) {
                                    Log.w(ResolverActivity.TAG, "No activity found for " + ii);
                                } else {
                                    ri = new ResolveInfo();
                                    ri.activityInfo = ai;
                                    userManager = (UserManager) ResolverActivity.this.getSystemService(ImsConferenceState.USER);
                                    if (ii instanceof LabeledIntent) {
                                        li = (LabeledIntent) ii;
                                        ri.resolvePackageName = li.getSourcePackage();
                                        ri.labelRes = li.getLabelResource();
                                        ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                                        ri.icon = li.getIconResource();
                                        ri.iconResourceId = ri.icon;
                                    }
                                    if (userManager.isManagedProfile()) {
                                        ri.noResourceId = true;
                                        ri.icon = 0;
                                    }
                                    addResolveInfo(new DisplayResolveInfo(ResolverActivity.this, ii, ri, ri.loadLabel(ResolverActivity.this.getPackageManager()), null, ii));
                                }
                            }
                        }
                    }
                    ResolvedComponentInfo rci0 = (ResolvedComponentInfo) currentResolveList.get(0);
                    r0 = rci0.getResolveInfoAt(0);
                    int start = 0;
                    CharSequence r0Label = r0.loadLabel(ResolverActivity.this.mPm);
                    this.mHasExtendedInfo = false;
                    for (i = 1; i < N; i++) {
                        if (r0Label == null) {
                            r0Label = r0.activityInfo.packageName;
                        }
                        ResolvedComponentInfo rci = (ResolvedComponentInfo) currentResolveList.get(i);
                        ri = rci.getResolveInfoAt(0);
                        CharSequence riLabel = ri.loadLabel(ResolverActivity.this.mPm);
                        if (riLabel == null) {
                            riLabel = ri.activityInfo.packageName;
                        }
                        if (!riLabel.equals(r0Label)) {
                            processGroup(currentResolveList, start, i - 1, rci0, r0Label);
                            rci0 = rci;
                            r0 = ri;
                            r0Label = riLabel;
                            start = i;
                        }
                    }
                    processGroup(currentResolveList, start, N - 1, rci0, r0Label);
                }
            }
            if (!(this.mInitialIntents == null || currentResolveList == null || currentResolveList.size() != 0)) {
                for (Intent ii2 : this.mInitialIntents) {
                    if (ii2 != null) {
                        ai = ii2.resolveActivityInfo(ResolverActivity.this.getPackageManager(), 0);
                        if (ai == null) {
                            Log.w(ResolverActivity.TAG, "No activity found for " + ii2);
                        } else {
                            ri = new ResolveInfo();
                            ri.activityInfo = ai;
                            userManager = (UserManager) ResolverActivity.this.getSystemService(ImsConferenceState.USER);
                            if (ii2 instanceof LabeledIntent) {
                                li = (LabeledIntent) ii2;
                                ri.resolvePackageName = li.getSourcePackage();
                                ri.labelRes = li.getLabelResource();
                                ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                                ri.icon = li.getIconResource();
                                ri.iconResourceId = ri.icon;
                            }
                            if (userManager.isManagedProfile()) {
                                ri.noResourceId = true;
                                ri.icon = 0;
                            }
                            Log.d(ResolverActivity.TAG, "rebuildList --> initialIntents =" + ii2.toInsecureString());
                            addResolveInfo(new DisplayResolveInfo(ResolverActivity.this, ii2, ri, ri.loadLabel(ResolverActivity.this.getPackageManager()), null, ii2));
                        }
                    }
                }
            }
            if (this.mOtherProfile != null && this.mLastChosenPosition >= 0) {
                this.mLastChosenPosition = -1;
                this.mFilterLastUsed = false;
            }
            onListRebuilt();
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Tongxi.Li@Plf.Framework, 2016-08-06 : Modify for multi app", property = OppoRomType.ROM)
        private void addResolveListDedupe(List<ResolvedComponentInfo> into, Intent intent, List<ResolveInfo> from) {
            int fromCount = from.size();
            int intoCount = into.size();
            ResolveInfo newInfo;
            if ((intent.getFlags() & 1024) != 0) {
                newInfo = (ResolveInfo) from.get(0);
                ResolveInfo newInfo2 = new ResolveInfo(newInfo);
                Intent intent2 = new Intent(intent);
                intent2.addCategory(OppoMultiLauncherUtil.MULTI_TAG);
                newInfo2.isMultiApp = true;
                into.add(new ResolvedComponentInfo(new ComponentName(newInfo.activityInfo.packageName, newInfo.activityInfo.name), intent, newInfo));
                into.add(new ResolvedComponentInfo(new ComponentName(newInfo2.activityInfo.packageName, newInfo2.activityInfo.name), intent2, newInfo2));
                return;
            }
            for (int i = 0; i < fromCount; i++) {
                ResolvedComponentInfo rci;
                newInfo = (ResolveInfo) from.get(i);
                boolean found = false;
                for (int j = 0; j < intoCount; j++) {
                    rci = (ResolvedComponentInfo) into.get(j);
                    if (isSameResolvedComponent(newInfo, rci)) {
                        found = true;
                        rci.add(intent, newInfo);
                        break;
                    }
                }
                if (!found) {
                    ComponentName name = new ComponentName(newInfo.activityInfo.packageName, newInfo.activityInfo.name);
                    rci = new ResolvedComponentInfo(name, intent, newInfo);
                    rci.setPinned(isComponentPinned(name));
                    into.add(rci);
                }
            }
        }

        private boolean isSameResolvedComponent(ResolveInfo a, ResolvedComponentInfo b) {
            ActivityInfo ai = a.activityInfo;
            if (ai.packageName.equals(b.name.getPackageName())) {
                return ai.name.equals(b.name.getClassName());
            }
            return false;
        }

        public void onListRebuilt() {
        }

        public boolean shouldGetResolvedFilter() {
            return this.mFilterLastUsed;
        }

        private void processGroup(List<ResolvedComponentInfo> rList, int start, int end, ResolvedComponentInfo ro, CharSequence roLabel) {
            if ((end - start) + 1 == 1) {
                addResolveInfoWithAlternates(ro, null, roLabel);
                return;
            }
            this.mHasExtendedInfo = true;
            boolean usePkg = false;
            CharSequence startApp = ro.getResolveInfoAt(0).activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm);
            if (startApp == null) {
                usePkg = true;
            }
            if (!usePkg) {
                HashSet<CharSequence> duplicates = new HashSet();
                duplicates.add(startApp);
                int j = start + 1;
                while (j <= end) {
                    CharSequence jApp = ((ResolvedComponentInfo) rList.get(j)).getResolveInfoAt(0).activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm);
                    if (jApp == null || duplicates.contains(jApp)) {
                        usePkg = true;
                        break;
                    } else {
                        duplicates.add(jApp);
                        j++;
                    }
                }
                duplicates.clear();
            }
            for (int k = start; k <= end; k++) {
                CharSequence extraInfo;
                ResolvedComponentInfo rci = (ResolvedComponentInfo) rList.get(k);
                ResolveInfo add = rci.getResolveInfoAt(0);
                if (usePkg) {
                    extraInfo = add.activityInfo.packageName;
                } else {
                    extraInfo = add.activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm);
                }
                addResolveInfoWithAlternates(rci, extraInfo, roLabel);
            }
        }

        private void addResolveInfoWithAlternates(ResolvedComponentInfo rci, CharSequence extraInfo, CharSequence roLabel) {
            int count = rci.getCount();
            Intent intent = rci.getIntentAt(0);
            ResolveInfo add = rci.getResolveInfoAt(0);
            Intent replaceIntent = ResolverActivity.this.getReplacementIntent(add.activityInfo, intent);
            DisplayResolveInfo dri = new DisplayResolveInfo(ResolverActivity.this, intent, add, roLabel, extraInfo, replaceIntent);
            dri.setPinned(rci.isPinned());
            if (intent.getCategories() != null && intent.getCategories().contains(OppoMultiLauncherUtil.MULTI_TAG)) {
                dri.mIsMultiApp = true;
            }
            addResolveInfo(dri);
            if (replaceIntent == intent) {
                int N = count;
                for (int i = 1; i < count; i++) {
                    dri.addAlternateSourceIntent(rci.getIntentAt(i));
                }
            }
            updateLastChosenPosition(add);
        }

        private void updateLastChosenPosition(ResolveInfo info) {
            if (this.mLastChosen != null && this.mLastChosen.activityInfo.packageName.equals(info.activityInfo.packageName) && this.mLastChosen.activityInfo.name.equals(info.activityInfo.name)) {
                this.mLastChosenPosition = this.mDisplayList.size() - 1;
            }
        }

        @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK : Add for CTS test", property = OppoRomType.ROM)
        private void addResolveInfo(DisplayResolveInfo dri) {
            if (dri.mResolveInfo.targetUserId == -2 || this.mOtherProfile != null) {
                this.mDisplayList.add(dri);
                return;
            }
            this.mOtherProfile = dri;
            this.mDisplayList.add(this.mOtherProfile);
        }

        public ResolveInfo resolveInfoForPosition(int position, boolean filtered) {
            return (filtered ? getItem(position) : (TargetInfo) this.mDisplayList.get(position)).getResolveInfo();
        }

        public TargetInfo targetInfoForPosition(int position, boolean filtered) {
            return filtered ? getItem(position) : (TargetInfo) this.mDisplayList.get(position);
        }

        public int getCount() {
            int result = this.mDisplayList.size();
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return result;
            }
            return result - 1;
        }

        public int getUnfilteredCount() {
            return this.mDisplayList.size();
        }

        public int getDisplayInfoCount() {
            return this.mDisplayList.size();
        }

        public DisplayResolveInfo getDisplayInfoAt(int index) {
            return (DisplayResolveInfo) this.mDisplayList.get(index);
        }

        public TargetInfo getItem(int position) {
            if (this.mFilterLastUsed && this.mLastChosenPosition >= 0 && position >= this.mLastChosenPosition) {
                position++;
            }
            return (TargetInfo) this.mDisplayList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public boolean hasExtendedInfo() {
            return this.mHasExtendedInfo;
        }

        public boolean hasResolvedTarget(ResolveInfo info) {
            int N = this.mDisplayList.size();
            for (int i = 0; i < N; i++) {
                if (ResolverActivity.resolveInfoMatch(info, ((DisplayResolveInfo) this.mDisplayList.get(i)).getResolveInfo())) {
                    return true;
                }
            }
            return false;
        }

        public int getDisplayResolveInfoCount() {
            return this.mDisplayList.size();
        }

        public DisplayResolveInfo getDisplayResolveInfo(int index) {
            return (DisplayResolveInfo) this.mDisplayList.get(index);
        }

        public final View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                view = createView(parent);
            }
            onBindView(view, getItem(position));
            return view;
        }

        @OppoHook(level = OppoHookType.NEW_FIELD, note = "Changwei.Li@Plf.SDK, 2015-01-07 : Modify for fix the icon size", property = OppoRomType.ROM)
        public final View createView(ViewGroup parent) {
            View view = onCreateView(parent);
            ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
            LayoutParams lp = holder.icon.getLayoutParams();
            int -get2 = ResolverActivity.this.mIconSize;
            lp.height = -get2;
            lp.width = -get2;
            return view;
        }

        public View onCreateView(ViewGroup parent) {
            return this.mInflater.inflate((int) R.layout.resolve_list_item, parent, false);
        }

        public boolean showsExtendedInfo(TargetInfo info) {
            return !TextUtils.isEmpty(info.getExtendedInfo());
        }

        public boolean isComponentPinned(ComponentName name) {
            return false;
        }

        public final void bindView(int position, View view) {
            onBindView(view, getItem(position));
        }

        private void onBindView(View view, TargetInfo info) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (!TextUtils.equals(holder.text.getText(), info.getDisplayLabel())) {
                holder.text.setText(info.getDisplayLabel());
            }
            if (showsExtendedInfo(info)) {
                holder.text2.setVisibility(0);
                holder.text2.setText(info.getExtendedInfo());
            } else {
                holder.text2.setVisibility(8);
            }
            if ((info instanceof DisplayResolveInfo) && !((DisplayResolveInfo) info).hasDisplayIcon()) {
                new LoadAdapterIconTask(ResolverActivity.this, (DisplayResolveInfo) info).execute(new Void[0]);
            }
            holder.icon.setImageDrawable(info.getDisplayIcon());
            if (holder.badge != null) {
                Drawable badge = info.getBadgeIcon();
                if (badge != null) {
                    holder.badge.setImageDrawable(badge);
                    holder.badge.setContentDescription(info.getBadgeContentDescription());
                    holder.badge.setVisibility(0);
                    return;
                }
                holder.badge.setVisibility(8);
            }
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "Hairong.Zou@Plf.SDK, 2014-03-24 : Add for System APP and User APP sorting seperately; JianHui.Yu@Plf.SDK, 2016-06-16 : Modify for Upgrade to Android6.0", property = OppoRomType.ROM)
        private List<ResolvedComponentInfo> resortList(List<ResolvedComponentInfo> list) {
            List<ResolvedComponentInfo> mSystemAppResolveList = new ArrayList();
            List<ResolvedComponentInfo> mUserAppResolveList = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                ApplicationInfo ai;
                ResolvedComponentInfo rci = (ResolvedComponentInfo) list.get(i);
                try {
                    ai = ResolverActivity.this.mPm.getApplicationInfo(rci.name.getPackageName(), 0);
                } catch (Exception e) {
                    ai = new ApplicationInfo();
                }
                if ((ai.flags & 1) == 0) {
                    mUserAppResolveList.add(rci);
                } else {
                    mSystemAppResolveList.add(rci);
                }
            }
            mSystemAppResolveList.addAll(mUserAppResolveList);
            return mSystemAppResolveList;
        }
    }

    public interface TargetInfo {
        TargetInfo cloneFilledIn(Intent intent, int i);

        List<Intent> getAllSourceIntents();

        CharSequence getBadgeContentDescription();

        Drawable getBadgeIcon();

        Drawable getDisplayIcon();

        CharSequence getDisplayLabel();

        CharSequence getExtendedInfo();

        ResolveInfo getResolveInfo();

        ComponentName getResolvedComponentName();

        Intent getResolvedIntent();

        boolean isPinned();

        boolean start(Activity activity, Bundle bundle);

        boolean startAsCaller(Activity activity, Bundle bundle, int i);

        boolean startAsUser(Activity activity, Bundle bundle, UserHandle userHandle);
    }

    /* renamed from: com.android.internal.app.ResolverActivity$1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ ResolverActivity this$0;

        /* renamed from: com.android.internal.app.ResolverActivity$1$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ AnonymousClass1 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.1.1.<init>(com.android.internal.app.ResolverActivity$1):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(com.android.internal.app.ResolverActivity.AnonymousClass1 r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.1.1.<init>(com.android.internal.app.ResolverActivity$1):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.1.1.<init>(com.android.internal.app.ResolverActivity$1):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.1.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.1.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.1.1.run():void");
            }
        }

        AnonymousClass1(ResolverActivity this$0) {
            this.this$0 = this$0;
        }

        public void onShareUninit() throws RemoteException {
            this.this$0.mOShareServiceInited = false;
            Log.i("OShare", "onShareUninit--------------");
            if (this.this$0.mOShareServiceUtil != null) {
                this.this$0.mOShareServiceUtil.unregisterCallback(this.this$0.mOShareCallback);
            }
        }

        public void onShareInit() throws RemoteException {
            this.this$0.mOShareServiceInited = true;
            Log.i("OShare", "onShareInit--------------");
            this.this$0.runOnUiThread(new AnonymousClass1(this));
            if (this.this$0.mOShareServiceUtil != null) {
                this.this$0.mOShareServiceUtil.registerCallback(this.this$0.mOShareCallback);
            }
        }
    }

    /* renamed from: com.android.internal.app.ResolverActivity$2 */
    class AnonymousClass2 extends IColorOshareCallback.Stub {
        final /* synthetic */ ResolverActivity this$0;

        /* renamed from: com.android.internal.app.ResolverActivity$2$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ AnonymousClass2 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.2.1.<init>(com.android.internal.app.ResolverActivity$2):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(com.android.internal.app.ResolverActivity.AnonymousClass2 r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.2.1.<init>(com.android.internal.app.ResolverActivity$2):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.2.1.<init>(com.android.internal.app.ResolverActivity$2):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.2.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.2.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.2.1.run():void");
            }
        }

        /* renamed from: com.android.internal.app.ResolverActivity$2$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ AnonymousClass2 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.2.2.<init>(com.android.internal.app.ResolverActivity$2):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass2(com.android.internal.app.ResolverActivity.AnonymousClass2 r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.2.2.<init>(com.android.internal.app.ResolverActivity$2):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.2.2.<init>(com.android.internal.app.ResolverActivity$2):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.2.2.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.2.2.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.2.2.run():void");
            }
        }

        AnonymousClass2(ResolverActivity this$0) {
            this.this$0 = this$0;
        }

        public void onDeviceChanged(List<ColorOshareDevice> deviceList) throws RemoteException {
            this.this$0.mDeviceList = (ArrayList) deviceList;
            this.this$0.mResolverOshareingAdapter.setDeviceList(this.this$0.mDeviceList);
            this.this$0.runOnUiThread(new AnonymousClass1(this));
        }

        public void onSendSwitchChanged(boolean isOn) {
            this.this$0.runOnUiThread(new AnonymousClass2(this));
        }
    }

    /* renamed from: com.android.internal.app.ResolverActivity$3 */
    class AnonymousClass3 extends PackageMonitor {
        final /* synthetic */ ResolverActivity this$0;

        AnonymousClass3(ResolverActivity this$0) {
            this.this$0 = this$0;
        }

        public void onSomePackagesChanged() {
            this.this$0.mAdapter.handlePackagesChanged();
            if (this.this$0.mProfileView != null) {
                this.this$0.bindProfileView();
            }
        }
    }

    /* renamed from: com.android.internal.app.ResolverActivity$4 */
    class AnonymousClass4 implements OnClickListener {
        final /* synthetic */ ResolverActivity this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.4.<init>(com.android.internal.app.ResolverActivity):void, dex: 
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
        AnonymousClass4(com.android.internal.app.ResolverActivity r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.4.<init>(com.android.internal.app.ResolverActivity):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.4.<init>(com.android.internal.app.ResolverActivity):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.4.onClick(android.view.View):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onClick(android.view.View r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.4.onClick(android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.4.onClick(android.view.View):void");
        }
    }

    /* renamed from: com.android.internal.app.ResolverActivity$5 */
    class AnonymousClass5 implements OnClickListener {
        final /* synthetic */ ResolverActivity this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.5.<init>(com.android.internal.app.ResolverActivity):void, dex: 
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
        AnonymousClass5(com.android.internal.app.ResolverActivity r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.5.<init>(com.android.internal.app.ResolverActivity):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.5.<init>(com.android.internal.app.ResolverActivity):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 8 in method: com.android.internal.app.ResolverActivity.5.onClick(android.view.View):void, dex:  in method: com.android.internal.app.ResolverActivity.5.onClick(android.view.View):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 8 in method: com.android.internal.app.ResolverActivity.5.onClick(android.view.View):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: 8
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void onClick(android.view.View r1) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: 8 in method: com.android.internal.app.ResolverActivity.5.onClick(android.view.View):void, dex:  in method: com.android.internal.app.ResolverActivity.5.onClick(android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.5.onClick(android.view.View):void");
        }
    }

    /* renamed from: com.android.internal.app.ResolverActivity$6 */
    class AnonymousClass6 implements OnClickListener {
        final /* synthetic */ ResolverActivity this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.6.<init>(com.android.internal.app.ResolverActivity):void, dex: 
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
        AnonymousClass6(com.android.internal.app.ResolverActivity r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.6.<init>(com.android.internal.app.ResolverActivity):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.6.<init>(com.android.internal.app.ResolverActivity):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 8 in method: com.android.internal.app.ResolverActivity.6.onClick(android.view.View):void, dex:  in method: com.android.internal.app.ResolverActivity.6.onClick(android.view.View):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 8 in method: com.android.internal.app.ResolverActivity.6.onClick(android.view.View):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: 8
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void onClick(android.view.View r1) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: 8 in method: com.android.internal.app.ResolverActivity.6.onClick(android.view.View):void, dex:  in method: com.android.internal.app.ResolverActivity.6.onClick(android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.6.onClick(android.view.View):void");
        }
    }

    /* renamed from: com.android.internal.app.ResolverActivity$7 */
    class AnonymousClass7 implements OnClickListener {
        final /* synthetic */ ResolverActivity this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.7.<init>(com.android.internal.app.ResolverActivity):void, dex: 
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
        AnonymousClass7(com.android.internal.app.ResolverActivity r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.7.<init>(com.android.internal.app.ResolverActivity):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.7.<init>(com.android.internal.app.ResolverActivity):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.7.onClick(android.view.View):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onClick(android.view.View r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.7.onClick(android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.7.onClick(android.view.View):void");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Shuai.Zhang@Plf.SDK, 2015-12-16 : Modify for ResolverDialog; Suying.You@Plf.SDK : Add for the name of title is the whichApplication or whichHomeApplication; Jianhua.Lin@Plf.SDK, 2013-12-24 : Add for Oppo Theme", property = OppoRomType.ROM)
    private enum ActionTitle {
        ;
        
        public final String action;
        public final int labelRes;
        public final int namedTitleRes;
        public final int titleRes;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.ResolverActivity.ActionTitle.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.ResolverActivity.ActionTitle.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ActionTitle.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.app.ResolverActivity.ActionTitle.<init>(java.lang.String, int, java.lang.String, int, int, int):void, dex:  in method: com.android.internal.app.ResolverActivity.ActionTitle.<init>(java.lang.String, int, java.lang.String, int, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.app.ResolverActivity.ActionTitle.<init>(java.lang.String, int, java.lang.String, int, int, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private ActionTitle(java.lang.String r3, int r4, int r5, int r6) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.app.ResolverActivity.ActionTitle.<init>(java.lang.String, int, java.lang.String, int, int, int):void, dex:  in method: com.android.internal.app.ResolverActivity.ActionTitle.<init>(java.lang.String, int, java.lang.String, int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ActionTitle.<init>(java.lang.String, int, java.lang.String, int, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ActionTitle.forAction(java.lang.String):com.android.internal.app.ResolverActivity$ActionTitle, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public static com.android.internal.app.ResolverActivity.ActionTitle forAction(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ActionTitle.forAction(java.lang.String):com.android.internal.app.ResolverActivity$ActionTitle, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ActionTitle.forAction(java.lang.String):com.android.internal.app.ResolverActivity$ActionTitle");
        }
    }

    public final class DisplayResolveInfo implements TargetInfo {
        private Drawable mBadge;
        private Drawable mDisplayIcon;
        private final CharSequence mDisplayLabel;
        private final CharSequence mExtendedInfo;
        public boolean mIsMultiApp;
        private boolean mPinned;
        private final ResolveInfo mResolveInfo;
        private final Intent mResolvedIntent;
        private final List<Intent> mSourceIntents;
        final /* synthetic */ ResolverActivity this$0;

        public DisplayResolveInfo(ResolverActivity this$0, Intent originalIntent, ResolveInfo pri, CharSequence pLabel, CharSequence pInfo, Intent pOrigIntent) {
            this.this$0 = this$0;
            this.mSourceIntents = new ArrayList();
            this.mIsMultiApp = false;
            this.mSourceIntents.add(originalIntent);
            this.mResolveInfo = pri;
            this.mDisplayLabel = pLabel;
            this.mExtendedInfo = pInfo;
            if (pOrigIntent == null) {
                pOrigIntent = this$0.getReplacementIntent(pri.activityInfo, this$0.getTargetIntent());
            }
            Intent intent = new Intent(pOrigIntent);
            intent.addFlags(View.SCROLLBARS_OUTSIDE_INSET);
            ActivityInfo ai = this.mResolveInfo.activityInfo;
            intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
            this.mResolvedIntent = intent;
        }

        private DisplayResolveInfo(ResolverActivity this$0, DisplayResolveInfo other, Intent fillInIntent, int flags) {
            this.this$0 = this$0;
            this.mSourceIntents = new ArrayList();
            this.mIsMultiApp = false;
            this.mSourceIntents.addAll(other.getAllSourceIntents());
            this.mResolveInfo = other.mResolveInfo;
            this.mDisplayLabel = other.mDisplayLabel;
            this.mDisplayIcon = other.mDisplayIcon;
            this.mExtendedInfo = other.mExtendedInfo;
            this.mResolvedIntent = new Intent(other.mResolvedIntent);
            this.mResolvedIntent.fillIn(fillInIntent, flags);
            this.mPinned = other.mPinned;
        }

        public ResolveInfo getResolveInfo() {
            return this.mResolveInfo;
        }

        public CharSequence getDisplayLabel() {
            return this.mDisplayLabel;
        }

        public Drawable getDisplayIcon() {
            return this.mDisplayIcon;
        }

        public Drawable getBadgeIcon() {
            if (TextUtils.isEmpty(getExtendedInfo())) {
                return null;
            }
            if (!(this.mBadge != null || this.mResolveInfo == null || this.mResolveInfo.activityInfo == null || this.mResolveInfo.activityInfo.applicationInfo == null)) {
                if (this.mResolveInfo.activityInfo.icon == 0 || this.mResolveInfo.activityInfo.icon == this.mResolveInfo.activityInfo.applicationInfo.icon) {
                    return null;
                }
                this.mBadge = this.mResolveInfo.activityInfo.applicationInfo.loadIcon(this.this$0.mPm);
            }
            return this.mBadge;
        }

        public CharSequence getBadgeContentDescription() {
            return null;
        }

        public TargetInfo cloneFilledIn(Intent fillInIntent, int flags) {
            return new DisplayResolveInfo(this.this$0, this, fillInIntent, flags);
        }

        public List<Intent> getAllSourceIntents() {
            return this.mSourceIntents;
        }

        public void addAlternateSourceIntent(Intent alt) {
            this.mSourceIntents.add(alt);
        }

        public void setDisplayIcon(Drawable icon) {
            this.mDisplayIcon = icon;
        }

        public boolean hasDisplayIcon() {
            return this.mDisplayIcon != null;
        }

        public CharSequence getExtendedInfo() {
            return this.mExtendedInfo;
        }

        public Intent getResolvedIntent() {
            return this.mResolvedIntent;
        }

        public ComponentName getResolvedComponentName() {
            return new ComponentName(this.mResolveInfo.activityInfo.packageName, this.mResolveInfo.activityInfo.name);
        }

        public boolean start(Activity activity, Bundle options) {
            activity.startActivity(this.mResolvedIntent, options);
            return true;
        }

        public boolean startAsCaller(Activity activity, Bundle options, int userId) {
            if (this.mIsMultiApp) {
                this.mResolvedIntent.addCategory(OppoMultiLauncherUtil.MULTI_TAG);
            }
            if (!(this.mResolvedIntent == null || this.mResolvedIntent.getComponent() == null || this.mResolvedIntent.getComponent().getPackageName() == null || this.this$0.mLaunchedFromPackage == null)) {
                String pkgName = this.mResolvedIntent.getComponent().getPackageName();
                if (UserHandle.getUserId(this.this$0.mLaunchedFromUid) == OppoMultiLauncherUtil.USER_ID && this.this$0.mLaunchedFromPackage.equals(pkgName)) {
                    userId = OppoMultiLauncherUtil.USER_ID;
                }
            }
            activity.startActivityAsCaller(this.mResolvedIntent, options, false, userId);
            return true;
        }

        public boolean startAsUser(Activity activity, Bundle options, UserHandle user) {
            activity.startActivityAsUser(this.mResolvedIntent, options, user);
            return false;
        }

        public boolean isPinned() {
            return this.mPinned;
        }

        public void setPinned(boolean pinned) {
            this.mPinned = pinned;
        }
    }

    @OppoHook(level = OppoHookType.NEW_CLASS, note = "Jianhua.Lin@Plf.SDK : Add for Oppo Theme", property = OppoRomType.ROM)
    static class Injector {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.ResolverActivity.Injector.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        Injector() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.ResolverActivity.Injector.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.Injector.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.addNegativeButton(com.android.internal.app.ResolverActivity, com.android.internal.app.AlertController$AlertParams):void, dex: 
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
        static void addNegativeButton(com.android.internal.app.ResolverActivity r1, com.android.internal.app.AlertController.AlertParams r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.addNegativeButton(com.android.internal.app.ResolverActivity, com.android.internal.app.AlertController$AlertParams):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.Injector.addNegativeButton(com.android.internal.app.ResolverActivity, com.android.internal.app.AlertController$AlertParams):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.handleClickEvent(com.android.internal.app.ResolverActivity, android.view.View, int):void, dex: 
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
        static void handleClickEvent(com.android.internal.app.ResolverActivity r1, android.view.View r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.handleClickEvent(com.android.internal.app.ResolverActivity, android.view.View, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.Injector.handleClickEvent(com.android.internal.app.ResolverActivity, android.view.View, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.initialize(com.android.internal.app.ResolverActivity, boolean):void, dex: 
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
        static void initialize(com.android.internal.app.ResolverActivity r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.initialize(com.android.internal.app.ResolverActivity, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.Injector.initialize(com.android.internal.app.ResolverActivity, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.oppoLoadIconForResolveInfo(android.content.pm.ResolveInfo, android.content.pm.PackageManager):android.graphics.drawable.Drawable, dex: 
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
        static android.graphics.drawable.Drawable oppoLoadIconForResolveInfo(android.content.pm.ResolveInfo r1, android.content.pm.PackageManager r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.oppoLoadIconForResolveInfo(android.content.pm.ResolveInfo, android.content.pm.PackageManager):android.graphics.drawable.Drawable, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.Injector.oppoLoadIconForResolveInfo(android.content.pm.ResolveInfo, android.content.pm.PackageManager):android.graphics.drawable.Drawable");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.rePackageResolverInfoList(java.util.List):java.util.List<android.content.pm.ResolveInfo>, dex: 
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
        static java.util.List<android.content.pm.ResolveInfo> rePackageResolverInfoList(java.util.List<com.android.internal.app.ResolverActivity.DisplayResolveInfo> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.rePackageResolverInfoList(java.util.List):java.util.List<android.content.pm.ResolveInfo>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.Injector.rePackageResolverInfoList(java.util.List):java.util.List<android.content.pm.ResolveInfo>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.setIconSize(com.android.internal.app.ResolverActivity):int, dex: 
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
        static int setIconSize(com.android.internal.app.ResolverActivity r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.Injector.setIconSize(com.android.internal.app.ResolverActivity):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.Injector.setIconSize(com.android.internal.app.ResolverActivity):int");
        }
    }

    class ItemClickListener implements OnItemClickListener, OnItemLongClickListener {
        final /* synthetic */ ResolverActivity this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.ItemClickListener.<init>(com.android.internal.app.ResolverActivity):void, dex: 
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
        ItemClickListener(com.android.internal.app.ResolverActivity r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.ItemClickListener.<init>(com.android.internal.app.ResolverActivity):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ItemClickListener.<init>(com.android.internal.app.ResolverActivity):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ItemClickListener.onItemClick(android.widget.AdapterView, android.view.View, int, long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @android.annotation.OppoHook(level = android.annotation.OppoHook.OppoHookType.CHANGE_CODE, note = "Jianhua.Lin@Plf.SDK, 2013-12-24 : Modify for Oppo Theme;Changwei.Li@Plf.SDK, 2015-01-07 : Modify for ColorOS Resolver Style", property = android.annotation.OppoHook.OppoRomType.ROM)
        public void onItemClick(android.widget.AdapterView<?> r1, android.view.View r2, int r3, long r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ItemClickListener.onItemClick(android.widget.AdapterView, android.view.View, int, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ItemClickListener.onItemClick(android.widget.AdapterView, android.view.View, int, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ItemClickListener.onItemLongClick(android.widget.AdapterView, android.view.View, int, long):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @android.annotation.OppoHook(level = android.annotation.OppoHook.OppoHookType.CHANGE_CODE, note = "Changwei.Li@Plf.SDK, 2015-01-07 : Modify for ColorOS Resolver Style; Suying.You@Plf.SDK, 2014-08-14 : Add for", property = android.annotation.OppoHook.OppoRomType.ROM)
        public boolean onItemLongClick(android.widget.AdapterView<?> r1, android.view.View r2, int r3, long r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ItemClickListener.onItemLongClick(android.widget.AdapterView, android.view.View, int, long):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ItemClickListener.onItemLongClick(android.widget.AdapterView, android.view.View, int, long):boolean");
        }
    }

    abstract class LoadIconTask extends AsyncTask<Void, Void, Drawable> {
        protected final DisplayResolveInfo mDisplayResolveInfo;
        private final ResolveInfo mResolveInfo;
        final /* synthetic */ ResolverActivity this$0;

        public LoadIconTask(ResolverActivity this$0, DisplayResolveInfo dri) {
            this.this$0 = this$0;
            this.mDisplayResolveInfo = dri;
            this.mResolveInfo = dri.getResolveInfo();
        }

        protected /* bridge */ /* synthetic */ Object doInBackground(Object[] params) {
            return doInBackground((Void[]) params);
        }

        protected Drawable doInBackground(Void... params) {
            return this.this$0.loadIconForResolveInfo(this.mResolveInfo);
        }

        protected /* bridge */ /* synthetic */ void onPostExecute(Object d) {
            onPostExecute((Drawable) d);
        }

        protected void onPostExecute(Drawable d) {
            this.mDisplayResolveInfo.setDisplayIcon(d);
        }
    }

    class LoadAdapterIconTask extends LoadIconTask {
        final /* synthetic */ ResolverActivity this$0;

        public LoadAdapterIconTask(ResolverActivity this$0, DisplayResolveInfo dri) {
            this.this$0 = this$0;
            super(this$0, dri);
        }

        protected void onPostExecute(Drawable d) {
            super.onPostExecute(d);
            if (this.this$0.mProfileView != null && this.this$0.mAdapter.getOtherProfile() == this.mDisplayResolveInfo) {
                this.this$0.bindProfileView();
            }
            this.this$0.mAdapter.notifyDataSetChanged();
        }
    }

    class LoadIconIntoViewTask extends LoadIconTask {
        private final ImageView mTargetView;
        final /* synthetic */ ResolverActivity this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.LoadIconIntoViewTask.<init>(com.android.internal.app.ResolverActivity, com.android.internal.app.ResolverActivity$DisplayResolveInfo, android.widget.ImageView):void, dex: 
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
        public LoadIconIntoViewTask(com.android.internal.app.ResolverActivity r1, com.android.internal.app.ResolverActivity.DisplayResolveInfo r2, android.widget.ImageView r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.LoadIconIntoViewTask.<init>(com.android.internal.app.ResolverActivity, com.android.internal.app.ResolverActivity$DisplayResolveInfo, android.widget.ImageView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.LoadIconIntoViewTask.<init>(com.android.internal.app.ResolverActivity, com.android.internal.app.ResolverActivity$DisplayResolveInfo, android.widget.ImageView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.LoadIconIntoViewTask.onPostExecute(android.graphics.drawable.Drawable):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected void onPostExecute(android.graphics.drawable.Drawable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.LoadIconIntoViewTask.onPostExecute(android.graphics.drawable.Drawable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.LoadIconIntoViewTask.onPostExecute(android.graphics.drawable.Drawable):void");
        }
    }

    public class MyViewHolder extends com.color.widget.ColorRecyclerView.ViewHolder {
        final /* synthetic */ ResolverActivity this$0;
        public final TextView userName;
        public final View userPanel;
        public final ImageView userPic;
        public final ColorTransferProgress userPreogerss;
        public final TextView userStatus;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.MyViewHolder.<init>(com.android.internal.app.ResolverActivity, android.view.View):void, dex: 
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
        public MyViewHolder(com.android.internal.app.ResolverActivity r1, android.view.View r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.MyViewHolder.<init>(com.android.internal.app.ResolverActivity, android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.MyViewHolder.<init>(com.android.internal.app.ResolverActivity, android.view.View):void");
        }
    }

    static class PickTargetOptionRequest extends PickOptionRequest {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.ResolverActivity.PickTargetOptionRequest.<init>(android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public PickTargetOptionRequest(android.app.VoiceInteractor.Prompt r1, android.app.VoiceInteractor.PickOptionRequest.Option[] r2, android.os.Bundle r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.ResolverActivity.PickTargetOptionRequest.<init>(android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.PickTargetOptionRequest.<init>(android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.PickTargetOptionRequest.onCancel():void, dex: 
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
        public void onCancel() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.PickTargetOptionRequest.onCancel():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.PickTargetOptionRequest.onCancel():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.ResolverActivity.PickTargetOptionRequest.onPickOptionResult(boolean, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onPickOptionResult(boolean r1, android.app.VoiceInteractor.PickOptionRequest.Option[] r2, android.os.Bundle r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.ResolverActivity.PickTargetOptionRequest.onPickOptionResult(boolean, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.PickTargetOptionRequest.onPickOptionResult(boolean, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void");
        }
    }

    static final class ResolvedComponentInfo {
        private final List<Intent> mIntents;
        private boolean mPinned;
        private final List<ResolveInfo> mResolveInfos;
        public final ComponentName name;

        public ResolvedComponentInfo(ComponentName name, Intent intent, ResolveInfo info) {
            this.mIntents = new ArrayList();
            this.mResolveInfos = new ArrayList();
            this.name = name;
            add(intent, info);
        }

        public void add(Intent intent, ResolveInfo info) {
            this.mIntents.add(intent);
            this.mResolveInfos.add(info);
        }

        public int getCount() {
            return this.mIntents.size();
        }

        public Intent getIntentAt(int index) {
            return index >= 0 ? (Intent) this.mIntents.get(index) : null;
        }

        public ResolveInfo getResolveInfoAt(int index) {
            return index >= 0 ? (ResolveInfo) this.mResolveInfos.get(index) : null;
        }

        public int findIntent(Intent intent) {
            int N = this.mIntents.size();
            for (int i = 0; i < N; i++) {
                if (intent.equals(this.mIntents.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        public int findResolveInfo(ResolveInfo info) {
            int N = this.mResolveInfos.size();
            for (int i = 0; i < N; i++) {
                if (info.equals(this.mResolveInfos.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        public boolean isPinned() {
            return this.mPinned;
        }

        public void setPinned(boolean pinned) {
            this.mPinned = pinned;
        }
    }

    private class ResolverOshareingAdapter extends Adapter<MyViewHolder> {
        /* renamed from: -com-color-oshare-ColorOshareStateSwitchesValues */
        private static final /* synthetic */ int[] f15-com-color-oshare-ColorOshareStateSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$color$oshare$ColorOshareState;
        String BUSUY_STR;
        String CANCEL_STR;
        String CANCEL_WAIT_STR;
        String READY_STR;
        String TRANSITING_STR;
        String TRANSIT_FAILED_STR;
        String TRANSIT_REJECT_STR;
        String TRANSIT_SUCCESS_STR;
        String TRANSIT_TIMEOUT_STR;
        String TRANSIT_WAIT_STR;
        private Context mContext;
        private ArrayList<ColorOshareDevice> mDeviceList;
        int mStateTextColorFail;
        int mStateTextColorNomarl;
        int mStateTextColorSucces;
        final /* synthetic */ ResolverActivity this$0;

        /* renamed from: com.android.internal.app.ResolverActivity$ResolverOshareingAdapter$1 */
        class AnonymousClass1 implements OnClickListener {
            final /* synthetic */ ResolverOshareingAdapter this$1;
            final /* synthetic */ ColorOshareDevice val$receiver;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.1.<init>(com.android.internal.app.ResolverActivity$ResolverOshareingAdapter, com.color.oshare.ColorOshareDevice):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(com.android.internal.app.ResolverActivity.ResolverOshareingAdapter r1, com.color.oshare.ColorOshareDevice r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.1.<init>(com.android.internal.app.ResolverActivity$ResolverOshareingAdapter, com.color.oshare.ColorOshareDevice):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.1.<init>(com.android.internal.app.ResolverActivity$ResolverOshareingAdapter, com.color.oshare.ColorOshareDevice):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 8 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.1.onClick(android.view.View):void, dex:  in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.1.onClick(android.view.View):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 8 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.1.onClick(android.view.View):void, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: com.android.dex.DexException: bogus registerCount: 8
                	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
                	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
                	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            public void onClick(android.view.View r1) {
                /*
                // Can't load method instructions: Load method exception: bogus registerCount: 8 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.1.onClick(android.view.View):void, dex:  in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.1.onClick(android.view.View):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.1.onClick(android.view.View):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.-getcom-color-oshare-ColorOshareStateSwitchesValues():int[], dex: 
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
        /* renamed from: -getcom-color-oshare-ColorOshareStateSwitchesValues */
        private static /* synthetic */ int[] m429-getcom-color-oshare-ColorOshareStateSwitchesValues() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.-getcom-color-oshare-ColorOshareStateSwitchesValues():int[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.-getcom-color-oshare-ColorOshareStateSwitchesValues():int[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.<init>(com.android.internal.app.ResolverActivity, android.content.Context):void, dex: 
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
        public ResolverOshareingAdapter(com.android.internal.app.ResolverActivity r1, android.content.Context r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.<init>(com.android.internal.app.ResolverActivity, android.content.Context):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.<init>(com.android.internal.app.ResolverActivity, android.content.Context):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.getStateColor(com.color.oshare.ColorOshareState):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private int getStateColor(com.color.oshare.ColorOshareState r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.getStateColor(com.color.oshare.ColorOshareState):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.getStateColor(com.color.oshare.ColorOshareState):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.getStateString(com.color.oshare.ColorOshareState):java.lang.String, dex: 
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
        private java.lang.String getStateString(com.color.oshare.ColorOshareState r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.getStateString(com.color.oshare.ColorOshareState):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.getStateString(com.color.oshare.ColorOshareState):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.getItemCount():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public int getItemCount() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.getItemCount():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.getItemCount():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onBindViewHolder(com.android.internal.app.ResolverActivity$MyViewHolder, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onBindViewHolder(com.android.internal.app.ResolverActivity.MyViewHolder r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onBindViewHolder(com.android.internal.app.ResolverActivity$MyViewHolder, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onBindViewHolder(com.android.internal.app.ResolverActivity$MyViewHolder, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onBindViewHolder(com.color.widget.ColorRecyclerView$ViewHolder, int):void, dex: 
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
        public /* bridge */ /* synthetic */ void onBindViewHolder(com.color.widget.ColorRecyclerView.ViewHolder r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onBindViewHolder(com.color.widget.ColorRecyclerView$ViewHolder, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onBindViewHolder(com.color.widget.ColorRecyclerView$ViewHolder, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onCreateViewHolder(android.view.ViewGroup, int):com.android.internal.app.ResolverActivity$MyViewHolder, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public com.android.internal.app.ResolverActivity.MyViewHolder onCreateViewHolder(android.view.ViewGroup r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onCreateViewHolder(android.view.ViewGroup, int):com.android.internal.app.ResolverActivity$MyViewHolder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onCreateViewHolder(android.view.ViewGroup, int):com.android.internal.app.ResolverActivity$MyViewHolder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onCreateViewHolder(android.view.ViewGroup, int):com.color.widget.ColorRecyclerView$ViewHolder, dex: 
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
        public /* bridge */ /* synthetic */ com.color.widget.ColorRecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onCreateViewHolder(android.view.ViewGroup, int):com.color.widget.ColorRecyclerView$ViewHolder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.onCreateViewHolder(android.view.ViewGroup, int):com.color.widget.ColorRecyclerView$ViewHolder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.setDeviceList(java.util.ArrayList):void, dex: 
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
        public void setDeviceList(java.util.ArrayList<com.color.oshare.ColorOshareDevice> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.setDeviceList(java.util.ArrayList):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ResolverOshareingAdapter.setDeviceList(java.util.ArrayList):void");
        }
    }

    static class ViewHolder {
        public ImageView badge;
        public ImageView icon;
        public TextView text;
        public TextView text2;

        public ViewHolder(View view) {
            this.text = (TextView) view.findViewById(R.id.text1);
            this.text2 = (TextView) view.findViewById(R.id.text2);
            this.icon = (ImageView) view.findViewById(R.id.icon);
            this.badge = (ImageView) view.findViewById(R.id.target_badge);
        }
    }

    public ResolverActivity() {
        this.mLastSelected = -1;
        this.mResolvingHome = false;
        this.mProfileSwitchMessageId = -1;
        this.mIntents = new ArrayList();
        this.mIsActionSend = false;
        this.mIsOshareOn = false;
        this.mOShareServiceInited = false;
        this.mBegineOshare = false;
        this.mOShareInitListener = new AnonymousClass1(this);
        this.mOShareCallback = new AnonymousClass2(this);
        this.mPackageMonitor = new AnonymousClass3(this);
        this.mItemClickFlag = false;
        this.mItemLongClickFlag = false;
    }

    private void updateOShareUI() {
        if (this.mOShareServiceUtil == null || !this.mOShareServiceUtil.isSendOn()) {
            this.mIsOshareOn = false;
            if (this.mOpenOsharePanel != null) {
                this.mOpenOsharePanel.setVisibility(0);
            }
            if (this.mOpenWifiBlueToothView != null) {
                this.mOpenWifiBlueToothView.setVisibility(0);
            }
            if (this.mNoticeOpenOshareView != null) {
                this.mNoticeOpenOshareView.setVisibility(8);
            }
            if (this.mOshareIcon != null) {
                this.mOshareIcon.setBackgroundResource(201852187);
            }
            if (this.mOshareingPanel != null) {
                this.mOshareingPanel.setVisibility(8);
                return;
            }
            return;
        }
        this.mOpenWifiBlueToothView.setVisibility(8);
        this.mIsOshareOn = true;
        if (this.mDeviceList == null || this.mDeviceList.size() < 1) {
            if (this.mOpenOsharePanel != null) {
                this.mOpenOsharePanel.setVisibility(0);
            }
            if (this.mOshareIcon != null) {
                this.mOshareIcon.setBackgroundResource(201852185);
            }
            if (this.mNoticeOpenOshareView != null) {
                this.mNoticeOpenOshareView.setVisibility(0);
            }
            if (this.mOshareingPanel != null) {
                this.mOshareingPanel.setVisibility(8);
                return;
            }
            return;
        }
        if (this.mOshareingPanel != null) {
            this.mOshareingPanel.setVisibility(0);
        }
        if (this.mOpenOsharePanel != null) {
            this.mOpenOsharePanel.setVisibility(8);
        }
    }

    public void finish() {
        super.finish();
        if (this.mOShareServiceUtil != null) {
            this.mOShareServiceUtil.stop();
            this.mOShareServiceUtil.unregisterCallback(this.mOShareCallback);
            this.mOShareServiceUtil = null;
        }
    }

    public static int getLabelRes(String action) {
        return ActionTitle.forAction(action).labelRes;
    }

    private Intent makeMyIntent() {
        Intent intent = new Intent(getIntent());
        intent.setComponent(null);
        intent.setFlags(intent.getFlags() & -8388609);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = makeMyIntent();
        Set<String> categories = intent.getCategories();
        if ("android.intent.action.MAIN".equals(intent.getAction()) && categories != null && categories.size() == 1 && categories.contains("android.intent.category.HOME")) {
            this.mResolvingHome = true;
        }
        setSafeForwardingMode(true);
        onCreate(savedInstanceState, intent, null, 0, null, null, true);
    }

    protected void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, Intent[] initialIntents, List<ResolveInfo> rList, boolean alwaysUseOption) {
        onCreate(savedInstanceState, intent, title, 0, initialIntents, rList, alwaysUseOption);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jianjun.Dan@Plf.SDK : Modify for change oppo theme; Jianhua.Lin@Plf.SDK, 2013-12-24 : Add for Oppo Theme; Changwei.Li@Plf.SDK, 2015-01-07 : Modify for ColorOS Resolver Style; Suying.You@Plf.SDK, 2015-02-04 : Modify for mAlwaysButton and mOnceButton is not visible", property = OppoRomType.ROM)
    protected void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, int defaultTitleRes, Intent[] initialIntents, List<ResolveInfo> rList, boolean alwaysUseOption) {
        ResolverActivity.setTheme(this);
        super.onCreate(savedInstanceState);
        if (title == null) {
            title = getTitleForAction(intent.getAction(), defaultTitleRes);
        }
        setProfileSwitchMessageId(intent.getContentUserHint());
        try {
            this.mLaunchedFromUid = ActivityManagerNative.getDefault().getLaunchedFromUid(getActivityToken());
            this.mLaunchedFromPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(getActivityToken());
            if (OppoMultiLauncherUtil.USER_ID == UserHandle.getUserId(this.mLaunchedFromUid) && OppoMultiLauncherUtil.getInstance().isMultiApp(this.mLaunchedFromPackage)) {
                int i;
                if (intent != null && OppoMultiLauncherUtil.getInstance().isMultiAppUri(intent, this.mLaunchedFromPackage)) {
                    intent.fixUris(OppoMultiLauncherUtil.USER_ID);
                }
                if (this.mIntents != null) {
                    for (i = 0; i < this.mIntents.size(); i++) {
                        if (OppoMultiLauncherUtil.getInstance().isMultiAppUri((Intent) this.mIntents.get(i), this.mLaunchedFromPackage)) {
                            ((Intent) this.mIntents.get(i)).fixUris(OppoMultiLauncherUtil.USER_ID);
                        }
                    }
                }
                if (initialIntents != null) {
                    for (i = 0; i < initialIntents.length; i++) {
                        if (OppoMultiLauncherUtil.getInstance().isMultiAppUri(initialIntents[i], this.mLaunchedFromPackage)) {
                            initialIntents[i].fixUris(OppoMultiLauncherUtil.USER_ID);
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            this.mLaunchedFromUid = -1;
        }
        if (this.mLaunchedFromUid < 0 || UserHandle.isIsolated(this.mLaunchedFromUid)) {
            finish();
            return;
        }
        this.mPm = getPackageManager();
        AlertParams ap = this.mAlertParams;
        this.mPackageMonitor.register(this, getMainLooper(), false);
        this.mRegistered = true;
        this.mIconDpi = ((ActivityManager) getSystemService("activity")).getLauncherLargeIconDensity();
        this.mIntents.add(0, new Intent(intent));
        this.mResolverComparator = new ResolverComparator(this, getTargetIntent(), getReferrerPackageName());
        if (!configureContentView(this.mIntents, initialIntents, rList, alwaysUseOption)) {
            int i2;
            this.mIconSize = Injector.setIconSize(this);
            this.mAlert = new AlertController(this, this, getWindow(), 2);
            ap.mNegativeButtonText = getResources().getText(R.string.cancel);
            setupAlert();
            Injector.initialize(this, alwaysUseOption);
            this.mProfileView = findViewById(R.id.profile_button);
            if (this.mProfileView != null) {
                this.mProfileView.setOnClickListener(new AnonymousClass4(this));
                bindProfileView();
            }
            if (isVoiceInteraction()) {
                onSetupVoiceInteraction();
            }
            Set<String> categories = intent.getCategories();
            if (this.mAdapter.hasFilteredItem()) {
                i2 = MetricsEvent.ACTION_SHOW_APP_DISAMBIG_APP_FEATURED;
            } else {
                i2 = MetricsEvent.ACTION_SHOW_APP_DISAMBIG_NONE_FEATURED;
            }
            MetricsLogger.action((Context) this, i2, intent.getAction() + ":" + intent.getType() + ":" + (categories != null ? Arrays.toString(categories.toArray()) : PhoneConstants.MVNO_TYPE_NONE));
        }
    }

    public final void setFilteredComponents(ComponentName[] components) {
        this.mFilteredComponents = components;
    }

    public final boolean isComponentFiltered(ComponentInfo component) {
        if (this.mFilteredComponents == null) {
            return false;
        }
        ComponentName checkName = component.getComponentName();
        for (ComponentName name : this.mFilteredComponents) {
            if (name.equals(checkName)) {
                return true;
            }
        }
        return false;
    }

    public void onSetupVoiceInteraction() {
        sendVoiceChoicesIfNeeded();
    }

    public void sendVoiceChoicesIfNeeded() {
        if (isVoiceInteraction()) {
            Option[] options = new Option[this.mAdapter.getCount()];
            int N = options.length;
            for (int i = 0; i < N; i++) {
                options[i] = optionForChooserTarget(this.mAdapter.getItem(i), i);
            }
            this.mPickOptionRequest = new PickTargetOptionRequest(new Prompt(getTitle()), options, null);
            getVoiceInteractor().submitRequest(this.mPickOptionRequest);
        }
    }

    Option optionForChooserTarget(TargetInfo target, int index) {
        return new Option(target.getDisplayLabel(), index);
    }

    protected final void setAdditionalTargets(Intent[] intents) {
        if (intents != null) {
            for (Intent intent : intents) {
                this.mIntents.add(intent);
            }
        }
    }

    public Intent getTargetIntent() {
        return this.mIntents.isEmpty() ? null : (Intent) this.mIntents.get(0);
    }

    private String getReferrerPackageName() {
        Uri referrer = getReferrer();
        if (referrer == null || !"android-app".equals(referrer.getScheme())) {
            return null;
        }
        return referrer.getHost();
    }

    public int getLayoutResource() {
        return R.layout.resolver_list;
    }

    void bindProfileView() {
        DisplayResolveInfo dri = this.mAdapter.getOtherProfile();
        if (dri != null) {
            this.mProfileView.setVisibility(0);
            ((TextView) this.mProfileView.findViewById(R.id.profile_button)).setText(dri.getDisplayLabel());
            return;
        }
        this.mProfileView.setVisibility(8);
    }

    private void setProfileSwitchMessageId(int contentUserHint) {
        if (contentUserHint != -2 && contentUserHint != UserHandle.myUserId()) {
            boolean originIsManaged;
            UserManager userManager = (UserManager) getSystemService(ImsConferenceState.USER);
            UserInfo originUserInfo = userManager.getUserInfo(contentUserHint);
            if (originUserInfo != null) {
                originIsManaged = originUserInfo.isManagedProfile();
            } else {
                originIsManaged = false;
            }
            boolean targetIsManaged = userManager.isManagedProfile();
            if (originIsManaged && !targetIsManaged) {
                this.mProfileSwitchMessageId = R.string.forward_intent_to_owner;
            } else if (!originIsManaged && targetIsManaged) {
                this.mProfileSwitchMessageId = R.string.forward_intent_to_work;
            }
        }
    }

    public void setSafeForwardingMode(boolean safeForwarding) {
        this.mSafeForwardingMode = safeForwarding;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Add for he name of the title is the whichApplication or whichHomeApplication", property = OppoRomType.ROM)
    protected CharSequence getTitleForAction(String action, int defaultTitleRes) {
        ActionTitle title = this.mResolvingHome ? ActionTitle.HOME : ActionTitle.forAction(action);
        boolean named = this.mAdapter != null ? this.mAdapter.hasFilteredItem() : false;
        if (title == ActionTitle.DEFAULT && defaultTitleRes != 0) {
            return getString(defaultTitleRes);
        }
        CharSequence string;
        if (named) {
            string = getString(title.namedTitleRes, new Object[]{this.mAdapter.getFilteredItem().getDisplayLabel()});
        } else {
            string = getString(title.titleRes);
        }
        return string;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE_AND_ACCESS, note = "Changwei.Li@Plf.SDK, 2015-01-07 : [+public] Modify for ColorOS Resolver Style", property = OppoRomType.ROM)
    public void dismiss() {
        super.dismiss();
    }

    Drawable getIcon(Resources res, int resId) {
        try {
            return res.getDrawableForDensity(resId, this.mIconDpi);
        } catch (NotFoundException e) {
            return null;
        }
    }

    Drawable loadIconForResolveInfo(ResolveInfo ri) {
        try {
            Drawable dr;
            if (!(ri.resolvePackageName == null || ri.icon == 0)) {
                dr = getIcon(this.mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon);
                if (dr != null) {
                    return dr;
                }
            }
            int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                dr = getIcon(this.mPm.getResourcesForApplication(ri.activityInfo.packageName), iconRes);
                if (dr != null) {
                    return dr;
                }
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Couldn't find resources for package", e);
        }
        return ri.loadIcon(this.mPm);
    }

    protected void onRestart() {
        super.onRestart();
        if (!this.mRegistered) {
            this.mPackageMonitor.register(this, getMainLooper(), false);
            this.mRegistered = true;
        }
        this.mAdapter.handlePackagesChanged();
        reConfigureContentView();
        if (this.mProfileView != null) {
            bindProfileView();
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Yujun.Feng@Plf.SDK, 2016-10-17 : Modify for restart ResolverActivity", property = OppoRomType.ROM)
    public void reConfigureContentView() {
        this.mResolverDialogHelper.resortList(Injector.rePackageResolverInfoList(this.mAdapter.mDisplayList));
        int count = this.mAdapter.getUnfilteredCount();
        if (count == 1 && this.mAdapter.getOtherProfile() == null && !this.mIsActionSend) {
            TargetInfo target = this.mAdapter.targetInfoForPosition(0, false);
            if (shouldAutoLaunchSingleChoice(target)) {
                safelyStartActivity(target);
                this.mPackageMonitor.unregister();
                this.mRegistered = false;
                finish();
                return;
            }
        }
        if (count > 0) {
            this.mResolverDialogHelper.resortList(Injector.rePackageResolverInfoList(this.mAdapter.mDisplayList));
            this.mResolverDialogHelper.setResolveView((ColorResolverDialogViewPager) this.mAlertParams.mView.findViewById(201458889), (ColorDotView) this.mAlertParams.mView.findViewById(201458890), (CheckBox) this.mAlertParams.mView.findViewById(201458856), this.mSafeForwardingMode);
            reSortDisplayResolveInfoList(this.mResolverDialogHelper.getResolveInforList());
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Shuai.Zhang@Plf.SDK, 2016-01-21 : Modify for ResolverDialog; Suying.You@Plf.SDK : Modify for Long click the item in the  file management, then click the back key,the android stop the service", property = OppoRomType.ROM)
    protected void onStop() {
        super.onStop();
        if (this.mRegistered) {
            this.mPackageMonitor.unregister();
            this.mRegistered = false;
        }
        if (this.mResolverDialogHelper != null) {
            this.mResolverDialogHelper.unRegister();
        }
        if ((getIntent().getFlags() & 268435456) != 0 && !isVoiceInteraction() && !this.mResolvingHome && !this.mItemLongClickFlag) {
            this.mItemLongClickFlag = false;
            if (!isChangingConfigurations()) {
                finish();
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations() && this.mPickOptionRequest != null) {
            this.mPickOptionRequest.cancel();
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Shuai.Zhang@Plf.SDK, 2015-09-11 : Modify for ResolverDialog", property = OppoRomType.ROM)
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private boolean hasManagedProfile() {
        UserManager userManager = (UserManager) getSystemService(ImsConferenceState.USER);
        if (userManager == null) {
            return false;
        }
        try {
            for (UserInfo userInfo : userManager.getProfiles(getUserId())) {
                if (userInfo != null && userInfo.isManagedProfile()) {
                    return true;
                }
            }
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    private boolean supportsManagedProfiles(ResolveInfo resolveInfo) {
        boolean z = false;
        try {
            if (getPackageManager().getApplicationInfo(resolveInfo.activityInfo.packageName, 0).targetSdkVersion >= 21) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void setAlwaysButtonEnabled(boolean hasValidSelection, int checkedPos, boolean filtered) {
        boolean enabled = false;
        if (hasValidSelection && this.mAdapter.resolveInfoForPosition(checkedPos, filtered).targetUserId == -2) {
            enabled = true;
        }
        this.mAlwaysButton.setEnabled(enabled);
    }

    public void onButtonClick(View v) {
        startSelected(this.mAlwaysUseOption ? this.mAdapterView.getCheckedItemPosition() : this.mAdapter.getFilteredPosition(), v.getId() == R.id.button_always, this.mAlwaysUseOption);
    }

    public void startSelected(int which, boolean always, boolean filtered) {
        if (!isFinishing()) {
            ResolveInfo ri = this.mAdapter.resolveInfoForPosition(which, filtered);
            if (this.mResolvingHome && hasManagedProfile() && !supportsManagedProfiles(ri)) {
                Toast.makeText((Context) this, String.format(getResources().getString(R.string.activity_resolver_work_profiles_support), new Object[]{ri.activityInfo.loadLabel(getPackageManager()).toString()}), 1).show();
                return;
            }
            if (onTargetSelected(this.mAdapter.targetInfoForPosition(which, filtered), always)) {
                int i;
                if (always && filtered) {
                    MetricsLogger.action(this, MetricsEvent.ACTION_APP_DISAMBIG_ALWAYS);
                } else if (filtered) {
                    MetricsLogger.action(this, MetricsEvent.ACTION_APP_DISAMBIG_JUST_ONCE);
                } else {
                    MetricsLogger.action(this, MetricsEvent.ACTION_APP_DISAMBIG_TAP);
                }
                if (this.mAdapter.hasFilteredItem()) {
                    i = MetricsEvent.ACTION_HIDE_APP_DISAMBIG_APP_FEATURED;
                } else {
                    i = MetricsEvent.ACTION_HIDE_APP_DISAMBIG_NONE_FEATURED;
                }
                MetricsLogger.action(this, i);
                finish();
            }
        }
    }

    public Intent getReplacementIntent(ActivityInfo aInfo, Intent defIntent) {
        return defIntent;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Xiaokang.Feng@Plf.SDK, 2013-06-08 : Modify for change acitivity animation; Suying.You@Plf.SDK : Modify for BUG 658215; Fei.Wang@ROM.SysApp.Graphics, 2014-10-08 : Modify for short click the item in the dialog", property = OppoRomType.ROM)
    protected boolean onTargetSelected(TargetInfo target, boolean alwaysCheck) {
        ResolveInfo ri = target.getResolveInfo();
        Intent intent = target != null ? target.getResolvedIntent() : null;
        if (intent != null && ((this.mAlwaysUseOption || this.mAdapter.hasFilteredItem()) && this.mAdapter.mOrigResolveList != null)) {
            Intent filterIntent;
            IntentFilter filter = new IntentFilter();
            if (intent.getSelector() != null) {
                filterIntent = intent.getSelector();
            } else {
                filterIntent = intent;
            }
            String action = filterIntent.getAction();
            if (action != null) {
                filter.addAction(action);
            }
            Set<String> categories = filterIntent.getCategories();
            if (categories != null) {
                for (String cat : categories) {
                    filter.addCategory(cat);
                }
            }
            filter.addCategory("android.intent.category.DEFAULT");
            int cat2 = ri.match & 268369920;
            Uri data = filterIntent.getData();
            if (cat2 == 6291456) {
                String mimeType = filterIntent.resolveType(this);
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (Throwable e) {
                        Log.w(TAG, e);
                        filter = null;
                    }
                }
            }
            if (data != null && data.getScheme() != null && (cat2 != 6291456 || (!"file".equals(data.getScheme()) && !VoiceInteractionSession.KEY_CONTENT.equals(data.getScheme())))) {
                PatternMatcher p;
                filter.addDataScheme(data.getScheme());
                Iterator<PatternMatcher> pIt = ri.filter.schemeSpecificPartsIterator();
                if (pIt != null) {
                    String ssp = data.getSchemeSpecificPart();
                    while (ssp != null && pIt.hasNext()) {
                        p = (PatternMatcher) pIt.next();
                        if (p.match(ssp)) {
                            filter.addDataSchemeSpecificPart(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
                Iterator<AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                if (aIt != null) {
                    while (aIt.hasNext()) {
                        AuthorityEntry a = (AuthorityEntry) aIt.next();
                        if (a.match(data) >= 0) {
                            int port = a.getPort();
                            filter.addDataAuthority(a.getHost(), port >= 0 ? Integer.toString(port) : null);
                        }
                    }
                }
                pIt = ri.filter.pathsIterator();
                if (pIt != null) {
                    String path = data.getPath();
                    while (path != null && pIt.hasNext()) {
                        p = (PatternMatcher) pIt.next();
                        if (p.match(path)) {
                            filter.addDataPath(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
            }
            if (filter != null) {
                int N = this.mAdapter.mOrigResolveList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                for (int i = 0; i < N; i++) {
                    ResolveInfo r = ((ResolvedComponentInfo) this.mAdapter.mOrigResolveList.get(i)).getResolveInfoAt(0);
                    set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                    if (r.match > bestMatch) {
                        bestMatch = r.match;
                    }
                }
                if (alwaysCheck) {
                    int userId = getUserId();
                    PackageManager pm = getPackageManager();
                    pm.addPreferredActivity(filter, bestMatch, set, intent.getComponent());
                    if (!ri.handleAllWebDataURI) {
                        boolean isHttpOrHttps;
                        String packageName = intent.getComponent().getPackageName();
                        String dataScheme = data != null ? data.getScheme() : null;
                        if (dataScheme != null) {
                            if (dataScheme.equals("http")) {
                                isHttpOrHttps = true;
                            } else {
                                isHttpOrHttps = dataScheme.equals("https");
                            }
                        } else {
                            isHttpOrHttps = false;
                        }
                        boolean isViewAction = action != null ? action.equals("android.intent.action.VIEW") : false;
                        boolean hasCategoryBrowsable;
                        if (categories != null) {
                            hasCategoryBrowsable = categories.contains("android.intent.category.BROWSABLE");
                        } else {
                            hasCategoryBrowsable = false;
                        }
                        if (isHttpOrHttps && isViewAction && hasCategoryBrowsable) {
                            pm.updateIntentVerificationStatusAsUser(packageName, 2, userId);
                        }
                    } else if (TextUtils.isEmpty(pm.getDefaultBrowserPackageNameAsUser(userId))) {
                        pm.setDefaultBrowserPackageNameAsUser(ri.activityInfo.packageName, userId);
                    }
                } else {
                    try {
                        AppGlobals.getPackageManager().setLastChosenActivity(intent, intent.resolveType(getContentResolver()), 65536, filter, bestMatch, intent.getComponent());
                    } catch (RemoteException re) {
                        Log.d(TAG, "Error calling setLastChosenActivity\n" + re);
                    }
                }
            }
        }
        if (target != null) {
            safelyStartActivity(target);
            overridePendingTransition(201981964, 201981968);
            this.mItemClickFlag = true;
        }
        return true;
    }

    public void safelyStartActivity(TargetInfo cti) {
        StrictMode.disableDeathOnFileUriExposure();
        try {
            safelyStartActivityInternal(cti);
        } finally {
            StrictMode.enableDeathOnFileUriExposure();
        }
    }

    private void safelyStartActivityInternal(TargetInfo cti) {
        if (this.mProfileSwitchMessageId != -1) {
            Toast.makeText((Context) this, getString(this.mProfileSwitchMessageId), 1).show();
        }
        if (this.mSafeForwardingMode) {
            try {
                if (cti.startAsCaller(this, null, -10000)) {
                    onActivityStarted(cti);
                }
            } catch (RuntimeException e) {
                String launchedFromPackage;
                try {
                    launchedFromPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(getActivityToken());
                } catch (RemoteException e2) {
                    launchedFromPackage = "??";
                }
                Slog.wtf(TAG, "Unable to launch as uid " + this.mLaunchedFromUid + " package " + launchedFromPackage + ", while running in " + ActivityThread.currentProcessName(), e);
            }
            return;
        }
        if (cti.start(this, null)) {
            onActivityStarted(cti);
        }
    }

    public void onActivityStarted(TargetInfo cti) {
    }

    public boolean shouldGetActivityMetadata() {
        return false;
    }

    public boolean shouldAutoLaunchSingleChoice(TargetInfo target) {
        return true;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Xiaokang.Feng@Plf.SDK, 2013-06-08 : Modify for change acitivity animation", property = OppoRomType.ROM)
    public void showTargetDetails(ResolveInfo ri) {
        startActivity(new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", ri.activityInfo.packageName, null)).addFlags(524288));
        overridePendingTransition(201981964, 201981968);
    }

    public ResolveListAdapter createAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
        return new ResolveListAdapter(context, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK, 2015-02-04 : Modify for alwaysUseOption need not change; Changwei.Li@Plf.SDK, 2015-01-06 : Modify for ColorOS Resolver Style; Shuai.Zhang@Plf.SDK, 2015-06-24 : Add for ResolverDialog", property = OppoRomType.ROM)
    public boolean configureContentView(List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, boolean alwaysUseOption) {
        int i = this.mLaunchedFromUid;
        boolean z = alwaysUseOption && !isVoiceInteraction();
        this.mAdapter = createAdapter(this, payloadIntents, initialIntents, rList, i, z);
        this.mAlwaysUseOption = alwaysUseOption;
        this.mResolverDialogHelper = new ColorResolverDialogHelper(this, (Intent) payloadIntents.get(0), initialIntents, this.mAlwaysUseOption, Injector.rePackageResolverInfoList(this.mAdapter.mDisplayList));
        int count = this.mAdapter.getUnfilteredCount();
        String mActionStr = ((Intent) payloadIntents.get(0)).getAction();
        if (mActionStr == null || !(mActionStr.equalsIgnoreCase("android.intent.action.SEND") || mActionStr.equalsIgnoreCase("android.intent.action.SEND_MULTIPLE"))) {
            this.mIsActionSend = false;
        } else {
            this.mIsActionSend = true;
        }
        if (count == 1 && this.mAdapter.getOtherProfile() == null && !this.mIsActionSend) {
            TargetInfo target = this.mAdapter.targetInfoForPosition(0, false);
            if (shouldAutoLaunchSingleChoice(target)) {
                safelyStartActivity(target);
                this.mPackageMonitor.unregister();
                this.mRegistered = false;
                finish();
                return true;
            }
        }
        if (count > 0) {
            this.mAlertParams.mView = getLayoutInflater().inflate(201917518, null);
            ColorResolverDialogViewPager viewPager = (ColorResolverDialogViewPager) this.mAlertParams.mView.findViewById(201458889);
            ColorDotView dotView = (ColorDotView) this.mAlertParams.mView.findViewById(201458890);
            CheckBox checkBox = (CheckBox) this.mAlertParams.mView.findViewById(201458856);
            if (this.mIsActionSend) {
                this.mIsOshareOn = false;
                this.mOShareServiceUtil = new ColorOshareServiceUtil(this, this.mOShareInitListener);
                this.mOShareServiceUtil.initShareEngine();
                this.mAlertParams.mView.findViewById(201458983).setVisibility(0);
                this.mOshareingPanel = this.mAlertParams.mView.findViewById(201458974);
                this.mRecyclerView = (ColorRecyclerView) this.mAlertParams.mView.findViewById(201458975);
                this.mResolverOshareingAdapter = new ResolverOshareingAdapter(this, this);
                ColorLinearLayoutManager layoutManager = new ColorLinearLayoutManager(this, 0, false);
                this.mRecyclerView.setAdapter(this.mResolverOshareingAdapter);
                this.mRecyclerView.setLayoutManager(layoutManager);
                this.mOshareIcon = this.mAlertParams.mView.findViewById(201458976);
                this.mOpenWifiBlueToothView = this.mAlertParams.mView.findViewById(201458977);
                this.mNoticeOpenOshareView = this.mAlertParams.mView.findViewById(201458978);
                this.mOpenOsharePanel = this.mAlertParams.mView.findViewById(201458973);
                this.mOpenWifiBlueToothView.setOnClickListener(new AnonymousClass5(this));
                this.mOshareIcon.setOnClickListener(new AnonymousClass6(this));
                this.mNoticeHelpView = this.mAlertParams.mView.findViewById(201458979);
                this.mNoticeHelpView.setOnClickListener(new AnonymousClass7(this));
            }
            this.mResolverDialogHelper.setOnItemClickListener(new ItemClickListener(this));
            this.mResolverDialogHelper.setResolveView(viewPager, dotView, checkBox, this.mSafeForwardingMode);
            reSortDisplayResolveInfoList(this.mResolverDialogHelper.getResolveInforList());
            overridePendingTransition(201982984, 201982985);
        } else {
            this.mAlertParams.mMessage = getResources().getText(R.string.noApplications);
        }
        return false;
    }

    public void onPrepareAdapterView(AbsListView adapterView, ResolveListAdapter adapter, boolean alwaysUseOption) {
        boolean useHeader = adapter.hasFilteredItem();
        ViewGroup listView = adapterView instanceof ListView ? (ListView) adapterView : null;
        adapterView.setAdapter(this.mAdapter);
        ItemClickListener listener = new ItemClickListener(this);
        adapterView.setOnItemClickListener(listener);
        adapterView.setOnItemLongClickListener(listener);
        if (alwaysUseOption) {
            listView.setChoiceMode(1);
        }
        if (useHeader && listView != null) {
            listView.addHeaderView(LayoutInflater.from(this).inflate((int) R.layout.resolver_different_item_header, listView, false));
        }
    }

    static boolean resolveInfoMatch(ResolveInfo lhs, ResolveInfo rhs) {
        if (lhs == null) {
            return rhs == null;
        } else {
            if (lhs.activityInfo != null) {
                return Objects.equals(lhs.activityInfo.name, rhs.activityInfo.name) ? Objects.equals(lhs.activityInfo.packageName, rhs.activityInfo.packageName) : false;
            } else {
                if (rhs.activityInfo != null) {
                    return false;
                }
                return true;
            }
        }
    }

    static final boolean isSpecificUriMatch(int match) {
        match &= 268369920;
        if (match < 3145728 || match > 5242880) {
            return false;
        }
        return true;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Shuai.Zhang@Plf.SDK, 2016-04-05 : Add for ResolverActivity", property = OppoRomType.ROM)
    void reSortDisplayResolveInfoList(List<ResolveInfo> list) {
        List<DisplayResolveInfo> mDRIList = new ArrayList();
        if (list != null) {
            mDRIList.clear();
            for (ResolveInfo resolveInfo : list) {
                for (DisplayResolveInfo dresolveInfo : this.mAdapter.mDisplayList) {
                    if (resolveInfo.equals(dresolveInfo.getResolveInfo())) {
                        mDRIList.add(dresolveInfo);
                    }
                }
            }
            this.mAdapter.mDisplayList.clear();
            this.mAdapter.mDisplayList.addAll(mDRIList);
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Jianjun.Dan@Plf.SDK,2013.10.05: Add for bug 453649", property = OppoRomType.ROM)
    protected void onPause() {
        super.onPause();
        if (this.mOShareServiceUtil != null) {
            try {
                this.mOShareServiceUtil.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mItemLongClickFlag = false;
        this.mItemClickFlag = false;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Xiaokang.Feng@Plf.SDK,2017.05.09: Add for OShare", property = OppoRomType.ROM)
    protected void onResume() {
        super.onResume();
        if (this.mOShareServiceUtil != null) {
            try {
                this.mOShareServiceUtil.resume();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
