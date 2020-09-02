package com.color.widget;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.color.widget.ColorResolverGridAdapter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ColorResolverPagerAdapter extends ColorPagerAdapter implements ColorResolverGridAdapter.OnItemClickListener {
    public static final int COLUMN_SIZE = 4;
    public static final String TAG = "ColorResolverPagerAdapter";
    private Activity mActivity;
    private IntentSender mChosenComponentSender;
    private Context mContext;
    private boolean mIsChecked = false;
    private Intent mOriginIntent;
    private ColorResolverPagerAdapterHelper mPagerAdapterHelper;
    private int mPagerSize = 4;
    private int mPlaceholderCount;
    private List<ResolveInfo> mRiList;
    private boolean mSafeForwardingMode;

    @Deprecated
    public ColorResolverPagerAdapter(Context context, List<ColorGridView> list, List<ResolveInfo> riList, int pagecount, Intent intent, CheckBox checkbox, Dialog alertDialog, boolean safeForwardingMode) {
        this.mActivity = (Activity) context;
        this.mContext = context;
        this.mRiList = riList;
        this.mOriginIntent = intent;
        this.mSafeForwardingMode = safeForwardingMode;
        this.mPagerAdapterHelper = new ColorResolverPagerAdapterHelper(context, alertDialog);
        this.mPagerAdapterHelper.setColorResolverItemEventListener(this);
        updatePageSize();
        if (checkbox != null) {
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                /* class com.color.widget.$$Lambda$ColorResolverPagerAdapter$M7KYLijzV1C0asMulpkxYD60YU */

                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    ColorResolverPagerAdapter.this.lambda$new$0$ColorResolverPagerAdapter(compoundButton, z);
                }
            });
        }
    }

    public /* synthetic */ void lambda$new$0$ColorResolverPagerAdapter(CompoundButton buttonView, boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    public ColorResolverPagerAdapter(Context context, List<ResolveInfo> riList, Intent intent, CheckBox checkbox, boolean safeForwardingMode) {
        this.mActivity = (Activity) context;
        this.mContext = context;
        this.mRiList = riList;
        this.mOriginIntent = intent;
        this.mSafeForwardingMode = safeForwardingMode;
        this.mPagerAdapterHelper = new ColorResolverPagerAdapterHelper(context, null);
        this.mPagerAdapterHelper.setColorResolverItemEventListener(this);
        updatePageSize();
        if (checkbox != null) {
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                /* class com.color.widget.$$Lambda$ColorResolverPagerAdapter$IQDwDAKeBMKCFO_FtbYegxDb0 */

                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    ColorResolverPagerAdapter.this.lambda$new$1$ColorResolverPagerAdapter(compoundButton, z);
                }
            });
        }
    }

    public /* synthetic */ void lambda$new$1$ColorResolverPagerAdapter(CompoundButton buttonView, boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    @Override // com.color.widget.ColorPagerAdapter
    public int getCount() {
        List<ResolveInfo> list = this.mRiList;
        if (list == null || list.isEmpty()) {
            return (int) Math.ceil(((double) this.mPlaceholderCount) / ((double) this.mPagerSize));
        }
        if (this.mPagerAdapterHelper.isNeedMoreIcon()) {
            return (int) Math.ceil(((double) (this.mPagerAdapterHelper.getMoreIconTotalPosition() + 1)) / ((double) this.mPagerSize));
        }
        return (int) Math.ceil(((double) this.mRiList.size()) / ((double) this.mPagerSize));
    }

    @Override // com.color.widget.ColorPagerAdapter
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override // com.color.widget.ColorPagerAdapter
    public Object instantiateItem(ViewGroup container, int position) {
        List<ColorItem> appInfo = this.mPagerAdapterHelper.loadBitmap(this.mRiList, position, this.mPagerSize, this.mPlaceholderCount);
        onInstantiateDataFinished(position, appInfo);
        View child = this.mPagerAdapterHelper.createPagerView(appInfo, position, this.mPagerSize);
        container.addView(child);
        return child;
    }

    @Override // com.color.widget.ColorPagerAdapter
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override // com.color.widget.ColorPagerAdapter
    public int getItemPosition(Object object) {
        return -2;
    }

    public void onInstantiateDataFinished(int pagerNumber, List<ColorItem> appList) {
        ColorGridView gridView = new ColorGridView(this.mContext);
        gridView.setAppInfo(this.mPagerAdapterHelper.listToArray(appList));
        loadBitmap(pagerNumber, gridView);
    }

    public void updatePageSize() {
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            this.mPagerSize = 4;
        } else {
            this.mPagerSize = 8;
        }
        Activity activity = this.mActivity;
        if ((activity instanceof Activity) && activity.isInMultiWindowMode()) {
            this.mPagerSize = 4;
        }
    }

    public void updateNeedMoreIcon(Intent intent) {
        this.mPagerAdapterHelper.updateNeedMoreIcon(intent, this.mRiList.size());
    }

    public boolean isMoreIconPositionAndClick(int position) {
        boolean isMoreIconPosition = this.mPagerAdapterHelper.isMoreIconPosition(position);
        if (isMoreIconPosition) {
            this.mPagerAdapterHelper.clickMoreIcon();
            notifyDataSetChanged();
        }
        return isMoreIconPosition;
    }

    public int getMoreIconTotalPosition() {
        return this.mPagerAdapterHelper.getMoreIconTotalPosition();
    }

    public void setPlaceholderCount(int placeholderCount) {
        this.mPlaceholderCount = placeholderCount;
    }

    @Override // com.color.widget.ColorResolverGridAdapter.OnItemClickListener
    public void onItemClick(int pagerNumber, int position) {
        OnItemClick((this.mPagerSize * pagerNumber) + position);
    }

    public void OnItemClick(int position) {
        String mimeType;
        if (this.mColorResolverItemEventListener != null) {
            this.mColorResolverItemEventListener.OnItemClick(position);
            return;
        }
        IntentFilter filter = new IntentFilter();
        Intent intent = new Intent(this.mOriginIntent);
        intent.addFlags(View.SCROLLBARS_OUTSIDE_INSET);
        if (position < this.mRiList.size() && position >= 0) {
            Log.d(TAG, "onItemClick : " + position + ", " + this.mRiList);
            ActivityInfo ai = this.mRiList.get(position).activityInfo;
            intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
            if (!isInLockTaskMode()) {
                safelyStartActivity(intent, this.mActivity, this.mSafeForwardingMode);
                this.mActivity.overridePendingTransition(201981964, 201981968);
            }
            ResolveInfo ri = this.mRiList.get(position);
            if (this.mIsChecked) {
                if (intent.getAction() != null) {
                    filter.addAction(intent.getAction());
                }
                Set<String> categories = intent.getCategories();
                if (categories != null) {
                    for (String cat : categories) {
                        filter.addCategory(cat);
                    }
                }
                filter.addCategory(Intent.CATEGORY_DEFAULT);
                int cat2 = 268369920 & ri.match;
                Uri data = intent.getData();
                if (cat2 == 6291456 && (mimeType = intent.resolveType(this.mContext)) != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        filter = null;
                    }
                }
                if (data != null && data.getScheme() != null && (cat2 != 6291456 || (!ContentResolver.SCHEME_FILE.equals(data.getScheme()) && !"content".equals(data.getScheme())))) {
                    filter.addDataScheme(data.getScheme());
                    if (ri.filter != null) {
                        Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                        if (aIt != null) {
                            while (true) {
                                if (!aIt.hasNext()) {
                                    break;
                                }
                                IntentFilter.AuthorityEntry a = aIt.next();
                                if (a.match(data) >= 0) {
                                    int port = a.getPort();
                                    filter.addDataAuthority(a.getHost(), port >= 0 ? Integer.toString(port) : null);
                                }
                            }
                        }
                        Iterator<PatternMatcher> pIt = ri.filter.pathsIterator();
                        if (pIt != null) {
                            String path = data.getPath();
                            while (true) {
                                if (path == null || !pIt.hasNext()) {
                                    break;
                                }
                                PatternMatcher p = pIt.next();
                                if (p.match(path)) {
                                    filter.addDataPath(p.getPath(), p.getType());
                                    break;
                                }
                            }
                        }
                    }
                }
                int N = this.mRiList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                int i = 0;
                while (i < N) {
                    ResolveInfo r = this.mRiList.get(i);
                    set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                    if (r.match > bestMatch) {
                        bestMatch = r.match;
                    }
                    i++;
                    N = N;
                }
                this.mContext.getPackageManager().addPreferredActivity(filter, bestMatch, set, intent.getComponent());
            }
            this.mPagerAdapterHelper.dismiss();
        }
    }

    @Override // com.color.widget.ColorResolverGridAdapter.OnItemClickListener
    public void onItemLongClick(int pagerNumber, int position) {
        OnItemLongClick((this.mPagerSize * pagerNumber) + position);
    }

    public void OnItemLongClick(int position) {
        if (this.mColorResolverItemEventListener != null) {
            this.mColorResolverItemEventListener.OnItemLongClick(position);
        }
    }

    @Deprecated
    public void unRegister() {
    }

    private static boolean isInLockTaskMode() {
        try {
            return ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    private void safelyStartActivity(Intent intent, Activity activity, boolean safeForwardingMode) {
        String launchedFromPackage;
        if (!safeForwardingMode) {
            try {
                activity.startActivity(intent);
                onActivityStarted(intent);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            try {
                activity.startActivityAsCaller(intent, null, null, false, -10000);
                onActivityStarted(intent);
            } catch (RuntimeException e2) {
                try {
                    launchedFromPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(activity.getActivityToken());
                } catch (RemoteException e3) {
                    launchedFromPackage = "??";
                }
                Log.d(TAG, " safelyStartActivity : " + launchedFromPackage);
            }
        }
    }

    private void onActivityStarted(Intent intent) {
        ComponentName target;
        if (this.mChosenComponentSender != null && (target = intent.getComponent()) != null) {
            try {
                this.mChosenComponentSender.sendIntent(this.mContext, -1, new Intent().putExtra(Intent.EXTRA_CHOSEN_COMPONENT, target), null, null);
            } catch (IntentSender.SendIntentException e) {
            }
        }
    }

    @Deprecated
    public void loadBitmap(int pagerNumber, ColorGridView gridView) {
    }

    public void setChosenComponentSender(IntentSender is) {
        this.mChosenComponentSender = is;
    }

    public void updateIntent(Intent intent) {
        this.mOriginIntent = intent;
    }
}
