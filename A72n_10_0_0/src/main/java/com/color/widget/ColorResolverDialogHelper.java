package com.color.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.CheckBox;
import com.android.internal.app.ResolverActivity;
import com.color.widget.ColorPagerAdapter;
import com.color.widget.ColorViewPager;
import com.color.widget.indicator.ColorPageIndicator;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import oppo.util.OppoStatistics;

public class ColorResolverDialogHelper {
    private static final String ACTION_CHOOSER_STOP = "oppo.intent.action.STOP_CHOOSER";
    private static final String APP_OPPO_MARKET = "com.oppo.market";
    private static final String CODE = "20120";
    private static final String GALLERY_PIN_LIST = "gallery_pin_list";
    private static final String KEY = "49";
    private static final String KEY_TYPE = "type";
    private static final String NEW_APP_OPPO_MARKET = "com.heytap.market";
    private static final String RECOMMEND_EVENT_ID = "resolver_recommend";
    private static final String SECRET = "be7a52eaeb67a660ecfdcff7c742c8a2";
    private static final String TAG = "ColorResolverDialogHelper";
    private static final String TYPE_GALLERY = "gallery";
    private BroadcastReceiver mAPKChangedReceiver = new BroadcastReceiver() {
        /* class com.color.widget.ColorResolverDialogHelper.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (ColorResolverDialogHelper.this.mActivity != null && !ColorResolverDialogHelper.this.mActivity.isFinishing()) {
                ColorResolverDialogHelper.this.mActivity.finish();
            }
        }
    };
    private Activity mActivity;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.color.widget.ColorResolverDialogHelper.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (ColorResolverDialogHelper.ACTION_CHOOSER_STOP.equals(intent.getAction()) && ColorResolverDialogHelper.this.mActivity != null && !ColorResolverDialogHelper.this.mActivity.isFinishing()) {
                ColorResolverDialogHelper.this.mActivity.finish();
            }
        }
    };
    private ColorResolverOshare mColorResolverOshare;
    private Context mContext;
    private List<ResolveInfo> mList = new ArrayList();
    private AdapterView.OnItemLongClickListener mLongClickListener = null;
    private AdapterView.OnItemClickListener mOnItemClickListener = null;
    private Intent mOriginIntent;
    private ColorResolverPagerAdapter mPagerAdapter;
    private ColorResolveInfoHelper mResolveInfoHelper;
    private List<ResolveInfo> mRiList = new ArrayList();
    private ColorResolverDialogViewPager mViewPager;

    public void initOShareService() {
        this.mColorResolverOshare.initOShareService();
    }

    public void initOShareView(View oShareView) {
        this.mColorResolverOshare.initOShareView(oShareView);
    }

    public ColorResolverDialogHelper(Activity context, Intent intent, Intent[] initialIntents, boolean alwaysUseOption, List<ResolveInfo> displayResolverlist) {
        this.mContext = context;
        this.mActivity = context;
        this.mOriginIntent = intent;
        this.mColorResolverOshare = new ColorResolverOshare(context, intent);
        if (displayResolverlist != null) {
            this.mRiList = displayResolverlist;
        } else if (intent != null || initialIntents != null) {
            int i = 0;
            this.mRiList = context.getPackageManager().queryIntentActivities(intent, 0);
            if (this.mRiList.size() == 0) {
                Intent in = new Intent();
                if (intent.getAction() != null) {
                    in.setAction(intent.getAction());
                }
                if (intent.getType() != null) {
                    in.setType(intent.getType());
                }
                if (intent.getExtras() != null) {
                    in.putExtras(intent.getExtras());
                }
                this.mRiList = context.getPackageManager().queryIntentActivities(in, (alwaysUseOption ? 64 : i) | 65536);
            }
            addInitiaIntents(initialIntents);
        } else {
            return;
        }
        Log.d(TAG, "init " + this.mRiList + ", " + this.mOriginIntent);
        this.mResolveInfoHelper = ColorResolveInfoHelper.getInstance(context);
        this.mResolveInfoHelper.resort(this.mRiList, intent);
        this.mList.addAll(this.mRiList);
        Log.d(TAG, "resort " + this.mRiList);
    }

    public List<ResolveInfo> getResolveInforList() {
        return this.mList;
    }

    private void addInitiaIntents(Intent[] initialIntents) {
        if (initialIntents != null) {
            for (Intent ii : initialIntents) {
                if (ii != null) {
                    ActivityInfo ai = ii.resolveActivityInfo(this.mContext.getPackageManager(), 0);
                    if (ai == null) {
                        Log.w(TAG, "No activity found for " + ii);
                    } else {
                        ResolveInfo ri = new ResolveInfo();
                        ri.activityInfo = ai;
                        if (ii instanceof LabeledIntent) {
                            LabeledIntent li = (LabeledIntent) ii;
                            ri.resolvePackageName = li.getSourcePackage();
                            ri.labelRes = li.getLabelResource();
                            ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                            ri.icon = li.getIconResource();
                        }
                        this.mList.add(ri);
                    }
                }
            }
        }
    }

    private void setResolveView(final ColorResolverDialogViewPager viewPager, final ColorPageIndicator dotView, CheckBox mCheckbox, boolean safeForwardingMode, int placeholderCount) {
        this.mPagerAdapter = new ColorResolverPagerAdapter(this.mContext, this.mList, this.mOriginIntent, mCheckbox, safeForwardingMode);
        this.mViewPager = viewPager;
        this.mPagerAdapter.setPlaceholderCount(placeholderCount);
        viewPager.setAdapter(this.mPagerAdapter);
        viewPager.setColorResolverItemEventListener(new ColorPagerAdapter.ColorResolverItemEventListener() {
            /* class com.color.widget.ColorResolverDialogHelper.AnonymousClass3 */

            @Override // com.color.widget.ColorPagerAdapter.ColorResolverItemEventListener
            public void OnItemLongClick(int position) {
                if (ColorResolverDialogHelper.this.mLongClickListener != null) {
                    ColorResolverDialogHelper.this.mLongClickListener.onItemLongClick(null, null, position, -1);
                    viewPager.performHapticFeedback(0);
                }
            }

            @Override // com.color.widget.ColorPagerAdapter.ColorResolverItemEventListener
            public void OnItemClick(int position) {
                ColorResolverDialogHelper colorResolverDialogHelper = ColorResolverDialogHelper.this;
                if (!colorResolverDialogHelper.clickMoreIcon(colorResolverDialogHelper.mActivity, position) && ColorResolverDialogHelper.this.mOnItemClickListener != null) {
                    ColorResolverDialogHelper.this.mOnItemClickListener.onItemClick(null, null, position, -1);
                }
            }
        });
        viewPager.setOnPageChangeListener(new ColorViewPager.OnPageChangeListener() {
            /* class com.color.widget.ColorResolverDialogHelper.AnonymousClass4 */

            @Override // com.color.widget.ColorViewPager.OnPageChangeListener
            public void onPageSelected(int position) {
                dotView.onPageSelected(position);
            }

            @Override // com.color.widget.ColorViewPager.OnPageChangeListener
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                dotView.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override // com.color.widget.ColorViewPager.OnPageChangeListener
            public void onPageScrollStateChanged(int state) {
                dotView.onPageScrollStateChanged(state);
            }
        });
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener longclickListener) {
        this.mLongClickListener = longclickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public ColorResolverPagerAdapter getPagerAdapter() {
        return this.mPagerAdapter;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean clickMoreIcon(Activity activity, int position) {
        Log.d(TAG, "clickMoreIcon : " + position);
        if (!this.mPagerAdapter.isMoreIconPositionAndClick(position)) {
            return false;
        }
        ((ColorPageIndicator) activity.findViewById(201458890)).setDotsCount(this.mPagerAdapter.getCount());
        return true;
    }

    public void showTargetDetails(ResolveInfo ri, SharedPreferences prefs, String type, ResolverActivity.ResolveListAdapter adapter) {
        Set<String> pinPrefList;
        boolean pinned;
        String componentName = ri.activityInfo.getComponentName().flattenToShortString();
        Set<String> pinPrefList2 = null;
        if (TYPE_GALLERY.equals(type)) {
            String galleryPinList = Settings.Secure.getString(this.mContext.getContentResolver(), GALLERY_PIN_LIST);
            if (!TextUtils.isEmpty(galleryPinList)) {
                pinPrefList2 = new HashSet<>(Arrays.asList(galleryPinList.split(";")));
            }
            pinPrefList = pinPrefList2;
        } else {
            pinPrefList = prefs.getStringSet(type, null);
        }
        if (pinPrefList != null) {
            pinned = pinPrefList.contains(componentName);
        } else {
            pinned = false;
        }
        Log.d(TAG, "showTargetDetails : " + pinPrefList + ", type : " + type + ", componentName : " + componentName + ", isPinned : " + pinned);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        try {
            Method setDialogType = builder.getClass().getSuperclass().getDeclaredMethod("setDialogType", Integer.TYPE);
            setDialogType.setAccessible(true);
            setDialogType.invoke(builder, 1);
        } catch (Exception e) {
            Log.w(TAG, "Failed to reflect setDialogType: " + e.getMessage());
        }
        builder.setItems(pinned ? 201786395 : 201786394, new DialogInterface.OnClickListener(prefs, componentName, type, adapter, ri) {
            /* class com.color.widget.$$Lambda$ColorResolverDialogHelper$HMYwB33EGSbIF417ihnU6EpdZzU */
            private final /* synthetic */ SharedPreferences f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ String f$3;
            private final /* synthetic */ ResolverActivity.ResolveListAdapter f$4;
            private final /* synthetic */ ResolveInfo f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                ColorResolverDialogHelper.this.lambda$showTargetDetails$0$ColorResolverDialogHelper(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, dialogInterface, i);
            }
        }).setNegativeButton(17039360, $$Lambda$ColorResolverDialogHelper$rHrO0W4jE0lylGibvjy2qt6gpUU.INSTANCE).create().show();
    }

    public /* synthetic */ void lambda$showTargetDetails$0$ColorResolverDialogHelper(SharedPreferences prefs, String componentName, String type, ResolverActivity.ResolveListAdapter adapter, ResolveInfo ri, DialogInterface dialog12, int which) {
        if (which == 0) {
            updatePinnedData(prefs, componentName, type);
            adapter.handlePackagesChanged();
        } else if (which == 1) {
            this.mContext.startActivity(new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", ri.activityInfo.packageName, null)).addFlags(524288));
            ((Activity) this.mContext).overridePendingTransition(201981964, 201981968);
        }
    }

    static /* synthetic */ void lambda$showTargetDetails$1(DialogInterface dialog1, int whichButton) {
    }

    private void updatePinnedData(SharedPreferences prefs, String componentName, String type) {
        boolean isPinned = false;
        if (!TYPE_GALLERY.equals(type)) {
            Set<String> pinPrefList = prefs.getStringSet(type, null);
            Set<String> newList = new HashSet<>();
            if (pinPrefList != null) {
                isPinned = pinPrefList.contains(componentName);
                newList = new HashSet<>(pinPrefList);
            }
            Log.d(TAG, "newList = " + newList);
            if (isPinned) {
                prefs.edit().remove(type).apply();
                newList.remove(componentName);
                prefs.edit().putStringSet(type, newList).apply();
                Log.d(TAG, "remove : " + componentName);
                return;
            }
            prefs.edit().remove(type).apply();
            newList.add(componentName);
            prefs.edit().putStringSet(type, newList).apply();
            Log.d(TAG, "add : " + componentName);
            return;
        }
        String galleryPinList = Settings.Secure.getString(this.mContext.getContentResolver(), GALLERY_PIN_LIST);
        Log.d(TAG, "galleryPinList = " + galleryPinList);
        List<String> newList2 = new ArrayList<>();
        if (!TextUtils.isEmpty(galleryPinList)) {
            List<String> list = Arrays.asList(galleryPinList.split(";"));
            isPinned = list.contains(componentName);
            newList2 = new ArrayList<>(list);
        }
        Log.d(TAG, "newList = " + newList2);
        if (isPinned) {
            newList2.remove(componentName);
            Log.d(TAG, "remove : " + componentName);
        } else {
            newList2.add(componentName);
            Log.d(TAG, "add : " + componentName);
        }
        String newString = listToString(newList2, ';');
        Settings.Secure.putString(this.mContext.getContentResolver(), GALLERY_PIN_LIST, newString);
        Log.d(TAG, "putStringForUser : " + newString);
    }

    private String listToString(List<String> list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                sb.append(list.get(i));
            } else {
                sb.append(list.get(i));
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public void showRecommend(Activity activity) {
        final View marketJump = activity.findViewById(201458997);
        if (marketJump != null) {
            if (!isMarketEnable()) {
                marketJump.setVisibility(8);
                Log.d(TAG, "OPPO Market is disable");
                return;
            }
            final String intentType = this.mResolveInfoHelper.getIntentType(this.mOriginIntent);
            if (!this.mResolveInfoHelper.isMarketRecommendType(intentType)) {
                marketJump.setVisibility(8);
                Log.d(TAG, "not is MarketRecommend Type");
                return;
            }
            new AsyncTask<Void, Void, Boolean>() {
                /* class com.color.widget.ColorResolverDialogHelper.AnonymousClass5 */

                /* access modifiers changed from: protected */
                public Boolean doInBackground(Void... param) {
                    ColorResolverDialogHelper colorResolverDialogHelper = ColorResolverDialogHelper.this;
                    return Boolean.valueOf(colorResolverDialogHelper.support(colorResolverDialogHelper.mContext));
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Boolean result) {
                    ColorResolverDialogHelper.this.showMarket(result.booleanValue(), marketJump, intentType);
                }
            }.execute(new Void[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMarket(boolean support, View marketJump, String intentType) {
        if (!support) {
            marketJump.setVisibility(8);
            return;
        }
        marketJump.setVisibility(0);
        if ("txt".equals(intentType)) {
            intentType = "text";
        }
        marketJump.setOnClickListener(new View.OnClickListener(intentType) {
            /* class com.color.widget.$$Lambda$ColorResolverDialogHelper$Cgpr8jW5UkZ8V_Vurdo4NsTIGH8 */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ColorResolverDialogHelper.this.lambda$showMarket$3$ColorResolverDialogHelper(this.f$1, view);
            }
        });
    }

    public /* synthetic */ void lambda$showMarket$2$ColorResolverDialogHelper(String type) {
        startRecommend(this.mContext, type);
    }

    public /* synthetic */ void lambda$showMarket$3$ColorResolverDialogHelper(String type, View v) {
        new Thread(new Runnable(type) {
            /* class com.color.widget.$$Lambda$ColorResolverDialogHelper$szXS5IHlQjmZR1GwAT2uoivuEsQ */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ColorResolverDialogHelper.this.lambda$showMarket$2$ColorResolverDialogHelper(this.f$1);
            }
        }).start();
    }

    private boolean startRecommend(Context context, String type) {
        int code = 0;
        try {
            Uri uri = Uri.parse("content://oaps_mk");
            Bundle bundle = new Bundle();
            bundle.putString("rtp", type);
            bundle.putString("goback", WifiEnterpriseConfig.ENGINE_ENABLE);
            bundle.putString("secret", SECRET);
            bundle.putString("enterId", KEY);
            bundle.putString("sgtp", WifiEnterpriseConfig.ENGINE_ENABLE);
            Bundle responseBundle = call(context, uri, "/recapp", bundle);
            if (responseBundle != null && responseBundle.containsKey("code")) {
                code = responseBundle.getInt("code");
            }
            Log.d(TAG, "startRecommend:" + type + ",response:" + code);
            if (code == 1) {
                HashMap<String, String> map = new HashMap<>();
                map.put("type", type);
                OppoStatistics.onCommon(this.mContext, CODE, RECOMMEND_EVENT_ID, (Map<String, String>) map, false);
                Log.d(TAG, "statistics data [resolver_recommend]: " + map);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return code == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean support(Context context) {
        Uri uri = Uri.parse("content://oaps_mk");
        Bundle bundle = new Bundle();
        bundle.putString("tp", "/recapp");
        bundle.putString("secret", SECRET);
        bundle.putString("enterId", KEY);
        bundle.putString("sgtp", WifiEnterpriseConfig.ENGINE_ENABLE);
        int code = 0;
        try {
            Bundle responseBundle = call(context, uri, "/support", bundle);
            if (responseBundle != null && responseBundle.containsKey("code")) {
                code = responseBundle.getInt("code");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Log.d(TAG, "oaps support:" + code);
        return code == 1;
    }

    private Bundle call(Context context, Uri uri, String path, Bundle bundle) {
        try {
            return context.getContentResolver().call(uri, path, "", bundle);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private boolean isMarketEnable() {
        boolean exist = false;
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = this.mContext.getPackageManager().getPackageInfo(APP_OPPO_MARKET, 8192);
        } catch (Exception e) {
            try {
                pkgInfo = this.mContext.getPackageManager().getPackageInfo(NEW_APP_OPPO_MARKET, 8192);
            } catch (Exception e2) {
                exist = false;
            }
        }
        if (pkgInfo == null || pkgInfo.applicationInfo == null || !pkgInfo.applicationInfo.enabled) {
            return exist;
        }
        return true;
    }

    public void dialogHelperDestroy() {
        this.mColorResolverOshare.onDestroy();
        try {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        } catch (Exception e) {
            Log.d(TAG, "fail to unregister receiver, " + e);
        }
        try {
            this.mContext.unregisterReceiver(this.mAPKChangedReceiver);
        } catch (Exception e2) {
            Log.d(TAG, "fail to unregister receiver, " + e2);
        }
    }

    public void oSharePause() {
        this.mColorResolverOshare.onPause();
    }

    public void oShareResume() {
        this.mColorResolverOshare.onResume();
    }

    public void initNfcView(View rootView) {
        Intent intent = this.mOriginIntent;
        if (intent != null || intent.getAction() != null) {
            String action = this.mOriginIntent.getAction();
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                View nfcPanel = rootView.findViewById(201459047);
                if (nfcPanel instanceof ViewStub) {
                    ((ViewStub) nfcPanel).inflate();
                }
            }
        }
    }

    public void initChooserTopBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHOOSER_STOP);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mAPKChangedReceiver, packageFilter);
    }

    public View createView(boolean safeForwardingMode, int count) {
        View view = this.mActivity.getLayoutInflater().inflate(201917518, (ViewGroup) null);
        ColorPageIndicator dotView = (ColorPageIndicator) view.findViewById(201458890);
        setResolveView((ColorResolverDialogViewPager) view.findViewById(201458889), dotView, null, safeForwardingMode, count);
        updateDotView(dotView);
        return view;
    }

    private void updateDotView(View dotView) {
        ColorResolverPagerAdapter colorResolverPagerAdapter = this.mPagerAdapter;
        if (colorResolverPagerAdapter != null && dotView != null && (dotView instanceof ColorPageIndicator)) {
            ((ColorPageIndicator) dotView).setDotsCount(colorResolverPagerAdapter.getCount());
        }
    }

    public boolean initCheckBox(Intent intent, View view, boolean alwaysUseOption) {
        View checkBoxContainer = null;
        if (alwaysUseOption) {
            checkBoxContainer = view.findViewById(201458919);
            if (checkBoxContainer instanceof ViewStub) {
                checkBoxContainer = ((ViewStub) checkBoxContainer).inflate();
            }
        }
        boolean openFlag = false;
        try {
            openFlag = intent.getBooleanExtra("oppo_filemanager_openflag", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (openFlag && checkBoxContainer != null) {
            checkBoxContainer.setVisibility(8);
        }
        return openFlag;
    }

    public void resortListAndNotifyChange(List<ResolveInfo> displayResolverlist) {
        int oldMoreIcon = getPagerAdapter().getMoreIconTotalPosition();
        int oldCount = this.mList.size();
        int oldPageCount = this.mPagerAdapter.getCount();
        if (displayResolverlist != null) {
            this.mRiList = displayResolverlist;
        }
        this.mResolveInfoHelper.resort(this.mRiList, this.mOriginIntent);
        this.mList.clear();
        this.mList.addAll(this.mRiList);
        Log.d(TAG, "sort " + this.mRiList + ", " + this.mOriginIntent);
        updatePageSize();
        boolean needUpdateMoreIcon = ((this.mResolveInfoHelper.isChooserAction(this.mOriginIntent) || oldMoreIcon == this.mResolveInfoHelper.getResolveTopSize(this.mOriginIntent)) && oldCount == this.mList.size() && oldPageCount == this.mPagerAdapter.getCount()) ? false : true;
        ColorResolverPagerAdapter colorResolverPagerAdapter = this.mPagerAdapter;
        if (colorResolverPagerAdapter != null) {
            if (needUpdateMoreIcon) {
                colorResolverPagerAdapter.updateNeedMoreIcon(this.mOriginIntent);
                updateDotView(this.mActivity.findViewById(201458890));
            }
            this.mPagerAdapter.notifyDataSetChanged();
        }
        ColorResolverDialogViewPager colorResolverDialogViewPager = this.mViewPager;
        if (colorResolverDialogViewPager != null && needUpdateMoreIcon) {
            colorResolverDialogViewPager.setCurrentItem(0);
        }
    }

    private void updatePageSize() {
        ColorResolverPagerAdapter colorResolverPagerAdapter = this.mPagerAdapter;
        if (colorResolverPagerAdapter != null) {
            colorResolverPagerAdapter.updatePageSize();
        }
    }
}
