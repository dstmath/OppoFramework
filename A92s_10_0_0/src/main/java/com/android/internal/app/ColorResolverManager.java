package com.android.internal.app;

import android.app.ActivityManager;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Environment;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.app.ColorBaseResolverActivity;
import com.android.internal.app.ResolverActivity;
import com.color.multiapp.ColorMultiAppManager;
import com.color.util.ColorTypeCastingHelper;
import com.color.widget.ColorResolveInfoHelper;
import com.color.widget.ColorResolverDialogHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ColorResolverManager implements IColorResolverManager {
    private static final String GALLERY_PIN_LIST = "gallery_pin_list";
    private static final String PINNED_SHARED_PREFS_NAME = "chooser_pin_settings";
    private static final String TAG = "ColorResolverManager";
    private static final String TYPE_GALLERY = "gallery";
    private ColorBaseResolverActivity mBaseResolverActivity;
    private ComponentCallbacks mComponentCallbacks = new ComponentCallbacks() {
        /* class com.android.internal.app.ColorResolverManager.AnonymousClass1 */

        public void onConfigurationChanged(Configuration configuration) {
            ColorResolverManager.this.updateWindowAttributes();
        }

        public void onLowMemory() {
        }
    };
    private String mGalleryPinList;
    protected boolean mIsActionSend = false;
    private String mLaunchedFromPackage;
    protected boolean mOpenFlag;
    /* access modifiers changed from: private */
    public SharedPreferences mPinnedSharedPrefs;
    /* access modifiers changed from: private */
    public ColorResolveInfoHelper mResolveInfoHelper;
    /* access modifiers changed from: private */
    public ColorResolverDialogHelper mResolverDialogHelper;

    public boolean isLoadTheme() {
        return false;
    }

    public boolean isMultiApp(ResolverActivity.DisplayResolveInfo dri, ResolverActivity.DisplayResolveInfo existingInfo) {
        return ((ColorBaseDisplayResolveInfo) ColorTypeCastingHelper.typeCasting(ColorBaseDisplayResolveInfo.class, dri)).getIsMultiApp() == ((ColorBaseDisplayResolveInfo) ColorTypeCastingHelper.typeCasting(ColorBaseDisplayResolveInfo.class, existingInfo)).getIsMultiApp();
    }

    public boolean isOriginUi() {
        return false;
    }

    public boolean isOneAppFinish(int count, ResolverActivity.DisplayResolveInfo resolveInfo) {
        return count == 1 && resolveInfo == null && !this.mIsActionSend && !this.mBaseResolverActivity.isFinishing();
    }

    public void onCreate(ColorBaseResolverActivity activity) {
        this.mBaseResolverActivity = activity;
        activity.setTheme(201524238);
        activity.getWindow().addFlags(16777216);
        this.mBaseResolverActivity.getWindow().addFlags(Integer.MIN_VALUE);
        this.mBaseResolverActivity.overridePendingTransition(201982984, 201982985);
        this.mBaseResolverActivity.registerComponentCallbacks(this.mComponentCallbacks);
    }

    public void onResume() {
        ColorResolverDialogHelper colorResolverDialogHelper = this.mResolverDialogHelper;
        if (colorResolverDialogHelper != null) {
            colorResolverDialogHelper.oShareResume();
        }
    }

    public void onPause() {
        ColorResolverDialogHelper colorResolverDialogHelper = this.mResolverDialogHelper;
        if (colorResolverDialogHelper != null) {
            colorResolverDialogHelper.oSharePause();
        }
    }

    public void onDestroy() {
        ColorResolverDialogHelper colorResolverDialogHelper = this.mResolverDialogHelper;
        if (colorResolverDialogHelper != null) {
            colorResolverDialogHelper.dialogHelperDestroy();
        }
        this.mBaseResolverActivity.unregisterComponentCallbacks(this.mComponentCallbacks);
    }

    public void initActionSend() {
        String mActionStr = this.mBaseResolverActivity.getTargetIntent().getAction();
        if (mActionStr == null || ((!mActionStr.equalsIgnoreCase("android.intent.action.SEND") && !mActionStr.equalsIgnoreCase("android.intent.action.SEND_MULTIPLE")) || getTargetIntent().getPackage() != null)) {
            this.mIsActionSend = false;
        } else {
            this.mIsActionSend = true;
        }
    }

    public void fixIntents(int launchedFromUid, Intent intent, ArrayList<Intent> intents, Intent[] initialIntents) {
        try {
            this.mLaunchedFromPackage = ActivityManager.getService().getLaunchedFromPackage(this.mBaseResolverActivity.getActivityToken());
            if (999 == UserHandle.getUserId(launchedFromUid) && ColorMultiAppManager.getInstance().isCreatedMultiApp(this.mLaunchedFromPackage)) {
                if (intent != null && ColorMultiAppManager.getInstance().isMultiAppUri(intent, this.mLaunchedFromPackage)) {
                    intent.fixUris(999);
                }
                if (intents != null) {
                    for (int i = 0; i < intents.size(); i++) {
                        if (ColorMultiAppManager.getInstance().isMultiAppUri(intents.get(i), this.mLaunchedFromPackage)) {
                            intents.get(i).fixUris(999);
                        }
                    }
                }
                if (initialIntents != null) {
                    for (int i2 = 0; i2 < initialIntents.length; i2++) {
                        if (ColorMultiAppManager.getInstance().isMultiAppUri(initialIntents[i2], this.mLaunchedFromPackage)) {
                            initialIntents[i2].fixUris(999);
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void fixInfo(Intent intent, ResolverActivity.DisplayResolveInfo displayResolveInfo) {
        ColorBaseDisplayResolveInfo baseDisplayResolveInfo;
        if (intent != null && intent.getCategories() != null && intent.getCategories().contains("com.multiple.launcher") && (baseDisplayResolveInfo = (ColorBaseDisplayResolveInfo) ColorTypeCastingHelper.typeCasting(ColorBaseDisplayResolveInfo.class, displayResolveInfo)) != null) {
            baseDisplayResolveInfo.setIsMultiApp(true);
        }
    }

    public int fixUserId(int userId, Intent resolvedIntent, int launchedFromUid) {
        if (!ColorMultiAppManager.getInstance().isMultiAppSupport()) {
            return userId;
        }
        if (!(resolvedIntent == null || resolvedIntent.getComponent() == null || resolvedIntent.getComponent().getPackageName() == null || this.mLaunchedFromPackage == null)) {
            String pkgName = resolvedIntent.getComponent().getPackageName();
            if (UserHandle.getUserId(launchedFromUid) == 999 && this.mLaunchedFromPackage.equals(pkgName)) {
                userId = 999;
            }
        }
        if (UserHandle.getUserId(launchedFromUid) == 0 && resolvedIntent != null && resolvedIntent.hasCategory("com.multiple.launcher") && ("android.intent.action.SEND".equals(resolvedIntent.getAction()) || "android.intent.action.SEND_MULTIPLE".equals(resolvedIntent.getAction()))) {
            resolvedIntent.prepareToLeaveUser(0);
        }
        return userId;
    }

    public void setLastChosen(ResolverListController controller, Intent intent, IntentFilter filter, int bestMatch) throws RemoteException {
        if (!this.mOpenFlag) {
            controller.setLastChosen(intent, filter, bestMatch);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, ?[OBJECT, ARRAY], int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    public void initView(AbsListView adapterView, int count, List<ResolverActivity.ResolvedComponentInfo> placeholderResolveList, boolean safeForwardingMode, boolean supportsAlwaysUseOption, ColorBaseResolverActivity baseResolverActivity) {
        initDialogHelper(supportsAlwaysUseOption);
        if (count != 0 || (placeholderResolveList != null && !placeholderResolveList.isEmpty())) {
            updateView(placeholderResolveList, safeForwardingMode, supportsAlwaysUseOption);
        } else {
            CharSequence message = this.mBaseResolverActivity.getText(17040471);
            setContentView(LayoutInflater.from(this.mBaseResolverActivity).inflate(201917518, (ViewGroup) null, false));
            TextView emptyView = (TextView) this.mBaseResolverActivity.findViewById(16908299);
            emptyView.setVisibility(0);
            emptyView.setText(message);
            this.mBaseResolverActivity.findViewById(201459065).setVisibility(8);
        }
        this.mResolverDialogHelper.initChooserTopBroadcast();
    }

    public void updateView(List<ResolverActivity.ResolvedComponentInfo> componentInfos, boolean safeForwardingMode, boolean supportsAlwaysUseOption) {
        int i;
        if (this.mResolveInfoHelper != null && this.mResolverDialogHelper != null && this.mBaseResolverActivity.findViewById(201458852) == null) {
            List<ResolveInfo> resolveInfos = getUnsortResolveInfo(componentInfos);
            int size = this.mResolveInfoHelper.getExpandSizeWithoutMoreIcon(resolveInfos, getTargetIntent());
            ColorResolverDialogHelper colorResolverDialogHelper = this.mResolverDialogHelper;
            if (this.mResolveInfoHelper.isChooserAction(getTargetIntent()) || size <= 0 || size >= resolveInfos.size()) {
                i = resolveInfos.size();
            } else {
                i = size + 1;
            }
            View view = colorResolverDialogHelper.createView(safeForwardingMode, i);
            setContentView(view);
            this.mOpenFlag = this.mResolverDialogHelper.initCheckBox(this.mBaseResolverActivity.getIntent(), view, supportsAlwaysUseOption);
            this.mResolverDialogHelper.showRecommend(this.mBaseResolverActivity);
            ItemClickListener clickListener = new ItemClickListener();
            this.mResolverDialogHelper.setOnItemClickListener(clickListener);
            if (this.mIsActionSend) {
                this.mResolverDialogHelper.setOnItemLongClickListener(clickListener);
                this.mResolverDialogHelper.initOShareView(view);
                this.mBaseResolverActivity.getWindow().getDecorView().post(new Runnable() {
                    /* class com.android.internal.app.$$Lambda$ColorResolverManager$BOZ8hvHf3bA3aIJn_lvuOxrsjRg */

                    public final void run() {
                        ColorResolverManager.this.lambda$updateView$0$ColorResolverManager();
                    }
                });
                return;
            }
            this.mResolverDialogHelper.initNfcView(view);
        }
    }

    public /* synthetic */ void lambda$updateView$0$ColorResolverManager() {
        this.mResolverDialogHelper.initOShareService();
    }

    public ResolverActivity.ResolveListAdapter createAdapter(ChooserActivity chooserActivity, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed, ResolverListController controller) {
        Objects.requireNonNull(chooserActivity);
        return new ResolverActivity.ResolveListAdapter(chooserActivity, this.mBaseResolverActivity, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed, controller);
    }

    public void onMultiWindowModeChanged() {
        updateWindowAttributes();
        getResolverAdapter().handlePackagesChanged();
    }

    public void setResolverContent() {
        if (this.mResolverDialogHelper != null && !this.mBaseResolverActivity.isFinishing() && this.mBaseResolverActivity.getWindow().getDecorView() != null && !getResolverAdapter().mDisplayList.isEmpty()) {
            this.mResolverDialogHelper.resortListAndNotifyChange(getResolveInfoList(getResolverAdapter().mDisplayList));
            resortDisplayList(this.mResolverDialogHelper.getResolveInforList());
        }
    }

    public void initPreferenceAndPinList() {
        if (this.mResolveInfoHelper != null) {
            this.mPinnedSharedPrefs = getPinnedSharedPrefs(this.mBaseResolverActivity);
            boolean isChoose = this.mResolveInfoHelper.isChooserAction(getTargetIntent());
            String type = this.mResolveInfoHelper.getIntentType(getTargetIntent());
            if (isChoose && TYPE_GALLERY.equals(type)) {
                this.mGalleryPinList = Settings.Secure.getString(this.mBaseResolverActivity.getContentResolver(), GALLERY_PIN_LIST);
            }
        }
    }

    public void statisticsData(ResolveInfo ri, int which) {
        String referrerPackage = this.mBaseResolverActivity.getReferrerPackageName();
        ColorResolveInfoHelper colorResolveInfoHelper = this.mResolveInfoHelper;
        if (colorResolveInfoHelper != null) {
            colorResolveInfoHelper.statisticsData(ri, getTargetIntent(), which, referrerPackage);
        }
    }

    public ResolveInfo getResolveInfo(Intent ii, ColorBaseResolverActivity baseResolverActivity) {
        ResolveInfo ri = null;
        ActivityInfo ai = null;
        if (ii.getComponent() != null) {
            try {
                ai = this.mBaseResolverActivity.getPackageManager().getActivityInfo(ii.getComponent(), 0);
                ri = new ResolveInfo();
                ri.activityInfo = ai;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        if (ai == null) {
            ri = this.mBaseResolverActivity.getPackageManager().resolveActivity(ii, 65536);
            ai = ri != null ? ri.activityInfo : null;
        }
        if (ai != null) {
            return ri;
        }
        Log.w(TAG, "No activity found for " + ii);
        return null;
    }

    /* access modifiers changed from: private */
    public ResolverActivity.ResolveListAdapter getResolverAdapter() {
        return this.mBaseResolverActivity.getResolverAdapter();
    }

    /* access modifiers changed from: private */
    public Intent getTargetIntent() {
        return this.mBaseResolverActivity.getTargetIntent();
    }

    private List<ResolveInfo> getUnsortResolveInfo(List<ResolverActivity.ResolvedComponentInfo> componentInfos) {
        List<ResolveInfo> resolveInfos = new ArrayList<>();
        boolean hasProcessSort = true;
        if (getResolverAdapter().mDisplayList == null || getResolverAdapter().mDisplayList.isEmpty()) {
            hasProcessSort = false;
            ColorBaseResolverActivity.ColorBaseResolveListAdapter cbrla = (ColorBaseResolverActivity.ColorBaseResolveListAdapter) ColorTypeCastingHelper.typeCasting(ColorBaseResolverActivity.ColorBaseResolveListAdapter.class, getResolverAdapter());
            if (cbrla != null) {
                cbrla.processSortedListWrapper(componentInfos);
            }
        }
        if (((ResolverActivity.ResolveListAdapter) getResolverAdapter()).mDisplayList != null && !getResolverAdapter().mDisplayList.isEmpty()) {
            for (ResolverActivity.DisplayResolveInfo resolveInfo : getResolverAdapter().mDisplayList) {
                if (!(resolveInfo == null || resolveInfo.getResolveInfo() == null)) {
                    resolveInfos.add(resolveInfo.getResolveInfo());
                }
            }
        }
        if (!hasProcessSort && getResolverAdapter().mDisplayList != null) {
            getResolverAdapter().mDisplayList.clear();
        }
        return resolveInfos;
    }

    private void resortDisplayList(List<ResolveInfo> list) {
        ArrayList arrayList = new ArrayList();
        if (list != null) {
            for (ResolveInfo resolveInfo : list) {
                for (ResolverActivity.DisplayResolveInfo dresolveInfo : getResolverAdapter().mDisplayList) {
                    if (resolveInfo.equals(dresolveInfo.getResolveInfo())) {
                        arrayList.add(dresolveInfo);
                    }
                }
            }
            getResolverAdapter().mDisplayList.clear();
            getResolverAdapter().mDisplayList.addAll(arrayList);
        }
    }

    private void initDialogHelper(boolean supportsAlwaysUseOption) {
        this.mResolveInfoHelper = ColorResolveInfoHelper.getInstance(this.mBaseResolverActivity);
        this.mResolverDialogHelper = new ColorResolverDialogHelper(this.mBaseResolverActivity, getTargetIntent(), (Intent[]) null, supportsAlwaysUseOption, getResolveInfoList(getResolverAdapter().mDisplayList));
    }

    private List<ResolveInfo> getResolveInfoList(List<ResolverActivity.DisplayResolveInfo> drlist) {
        List<ResolveInfo> mRiList = new ArrayList<>();
        if (drlist != null) {
            mRiList.clear();
            for (ResolverActivity.DisplayResolveInfo resolveInfo : drlist) {
                if (resolveInfo.getResolveInfo() != null) {
                    mRiList.add(resolveInfo.getResolveInfo());
                }
            }
        }
        return mRiList;
    }

    class ItemClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        ItemClickListener() {
        }

        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            ColorResolverManager.this.handleClickEvent(view, position);
        }

        @Override // android.widget.AdapterView.OnItemLongClickListener
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (!ColorResolverManager.this.mIsActionSend) {
                return true;
            }
            String type = ColorResolverManager.this.mResolveInfoHelper.getIntentType(ColorResolverManager.this.getTargetIntent());
            ColorResolverManager.this.mResolverDialogHelper.showTargetDetails(ColorResolverManager.this.mResolverDialogHelper.getResolveInforList().get(position), ColorResolverManager.this.mPinnedSharedPrefs, type, ColorResolverManager.this.getResolverAdapter());
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void handleClickEvent(View view, int position) {
        boolean always = false;
        CheckBox alwaysOption = (CheckBox) this.mBaseResolverActivity.findViewById(201458856);
        if (alwaysOption != null && alwaysOption.getVisibility() == 0) {
            always = alwaysOption.isChecked();
        }
        this.mBaseResolverActivity.startSelected(position, always, false);
    }

    static SharedPreferences getPinnedSharedPrefs(Context context) {
        return context.getSharedPreferences(new File(new File(Environment.getDataUserCePackageDirectory(StorageManager.UUID_PRIVATE_INTERNAL, context.getUserId(), context.getPackageName()), "shared_prefs"), "chooser_pin_settings.xml"), 0);
    }

    private void setContentView(View view) {
        this.mBaseResolverActivity.setContentView(view);
        this.mBaseResolverActivity.findViewById(16908314).setOnClickListener(new View.OnClickListener() {
            /* class com.android.internal.app.$$Lambda$ColorResolverManager$gFjNjL0bx54O0ETk_9j6M5lvfuo */

            public final void onClick(View view) {
                ColorResolverManager.this.lambda$setContentView$1$ColorResolverManager(view);
            }
        });
        updateWindowAttributes();
    }

    public /* synthetic */ void lambda$setContentView$1$ColorResolverManager(View v) {
        this.mBaseResolverActivity.finish();
    }

    private Point getScreenSize() {
        Point point = new Point();
        ((WindowManager) this.mBaseResolverActivity.getSystemService("window")).getDefaultDisplay().getRealSize(point);
        return point;
    }

    /* access modifiers changed from: private */
    public void updateWindowAttributes() {
        int width;
        Point realSize = getScreenSize();
        boolean port = realSize.x < realSize.y;
        DisplayMetrics displayMetrics = this.mBaseResolverActivity.getResources().getDisplayMetrics();
        LinearLayout parentPanel = (LinearLayout) this.mBaseResolverActivity.getWindow().findViewById(201458852);
        if (parentPanel != null) {
            Window window = this.mBaseResolverActivity.getWindow();
            if (port) {
                window.setNavigationBarColor(this.mBaseResolverActivity.getColor(201721001));
                ((LinearLayout) parentPanel.getParent()).setGravity(81);
                width = Math.min(realSize.x, displayMetrics.widthPixels);
            } else {
                window.setNavigationBarColor(this.mBaseResolverActivity.getColor(201720878));
                ((LinearLayout) parentPanel.getParent()).setGravity(17);
                float dialogParentPaddingLeft = (float) this.mBaseResolverActivity.getResources().getDimensionPixelSize(201655677);
                width = Math.min(realSize.y, displayMetrics.widthPixels);
                if (!this.mBaseResolverActivity.isInMultiWindowMode()) {
                    width += ((int) dialogParentPaddingLeft) * 2;
                }
            }
            parentPanel.getLayoutParams().width = width;
            parentPanel.setLayoutParams(parentPanel.getLayoutParams());
        }
    }
}
