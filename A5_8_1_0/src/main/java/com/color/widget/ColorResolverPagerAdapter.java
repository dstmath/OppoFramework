package com.color.widget;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.AuthorityEntry;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.color.widget.ColorGridView.OnItemClickListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColorResolverPagerAdapter extends ColorPagerAdapter implements OnItemClickListener {
    public static final String TAG = "ColorResolverPagerAdapter";
    private List<ColorGridView> colorGridList;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private BroadcastReceiver mAPKChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ColorResolverPagerAdapter.this.dismiss();
        }
    };
    private Activity mActivity = null;
    private Dialog mAlertDialog;
    private ColorItem[][] mAppInfoTop;
    private IntentSender mChosenComponentSender;
    private Context mContext;
    private boolean mIsChecked = false;
    private boolean mIsChooser;
    private boolean mIsRegistered;
    private LruCache<Integer, ColorItem[][]> mMemoryCache;
    private int mMoreIconPageCount;
    private int mMoreIconPosition;
    private int mMoreIconTotalPosition;
    private ColorItem mMoreItem = new ColorItem();
    private boolean mNeedAnim;
    private boolean mNeedMoreIcon = false;
    private Intent mOriginIntent;
    private int mPageCount;
    private int mPagerSize = 4;
    private ColorResolveInfoHelper mResolveInfoHelper;
    private List<ResolveInfo> mRiList = new ArrayList();
    private boolean mSafeForwardingMode;

    private class BitmapTask implements Callable<ColorItem[][]> {
        int position;

        BitmapTask(int p) {
            this.position = p;
        }

        public ColorItem[][] call() {
            int size;
            ColorResolveInfoHelper -get2 = ColorResolverPagerAdapter.this.mResolveInfoHelper;
            List -get3 = ColorResolverPagerAdapter.this.mRiList;
            int -get1 = ColorResolverPagerAdapter.this.mPagerSize * this.position;
            if ((this.position + 1) * ColorResolverPagerAdapter.this.mPagerSize > ColorResolverPagerAdapter.this.mRiList.size()) {
                size = ColorResolverPagerAdapter.this.mRiList.size();
            } else {
                size = (this.position + 1) * ColorResolverPagerAdapter.this.mPagerSize;
            }
            ColorItem[][] appinfo = -get2.getAppInfo(-get3.subList(-get1, size), ColorResolverPagerAdapter.this.mContext.getPackageManager());
            ColorResolverPagerAdapter.this.addBitmapToMemoryCache(Integer.valueOf(this.position), appinfo);
            return appinfo;
        }
    }

    public ColorResolverPagerAdapter(Context context, List<ColorGridView> listColorGridView, List<ResolveInfo> riList, int pagecount, Intent intent, CheckBox mCheckbox, Dialog alertDialog, boolean safeForwardingMode) {
        this.mActivity = (Activity) context;
        this.colorGridList = listColorGridView;
        setCache();
        this.mPageCount = pagecount;
        this.mContext = context;
        this.mRiList = riList;
        this.mResolveInfoHelper = ColorResolveInfoHelper.getInstance(context);
        this.mMoreItem.setText(this.mContext.getString(201590158));
        this.mMoreItem.setIcon(this.mContext.getDrawable(201852207));
        this.mOriginIntent = intent;
        this.mAlertDialog = alertDialog;
        this.mSafeForwardingMode = safeForwardingMode;
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            this.mPagerSize = 4;
        } else {
            this.mPagerSize = 8;
        }
        if ((this.mActivity instanceof Activity) && this.mActivity.isInMultiWindowMode()) {
            this.mPagerSize = 4;
        }
        this.mIsChooser = this.mResolveInfoHelper.isChooserAction(intent);
        int resolveTopSize = this.mResolveInfoHelper.getResolveTopSize();
        if (this.mIsChooser || resolveTopSize <= 0 || resolveTopSize >= this.mRiList.size()) {
            this.mNeedMoreIcon = false;
        } else {
            this.mNeedMoreIcon = true;
            this.mMoreIconPosition = resolveTopSize % this.mPagerSize;
            this.mMoreIconPageCount = (int) Math.ceil(((double) (resolveTopSize + 1)) / ((double) this.mPagerSize));
            this.mMoreIconTotalPosition = resolveTopSize;
        }
        Log.d(TAG, "init:resolveTopSize=" + resolveTopSize + ",mRiList=" + this.mRiList.size() + ",mNeedMoreIcon=" + this.mNeedMoreIcon + ",mMoreIconPosition=" + this.mMoreIconPosition + ",mMoreIconPageCount=" + this.mMoreIconPageCount);
        if (mCheckbox != null) {
            mCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ColorResolverPagerAdapter.this.mIsChecked = isChecked;
                }
            });
        }
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mAPKChangedReceiver, packageFilter);
        this.mIsRegistered = true;
    }

    public int getCount() {
        if (this.mNeedMoreIcon) {
            return this.mMoreIconPageCount;
        }
        return this.mPageCount;
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ColorGridView oppoGridView = new ColorGridView(this.mContext);
        ((ColorResolverDialogViewPager) container).addView(oppoGridView);
        loadBitmap(position, oppoGridView);
        return oppoGridView;
    }

    public void destroyItem(View container, int position, Object object) {
        ((ColorResolverDialogViewPager) container).removeView((ColorGridView) object);
    }

    public int getItemPosition(Object object) {
        return -2;
    }

    public boolean needMoreIcon() {
        return this.mNeedMoreIcon;
    }

    public void setNeedMoreIcon(boolean value) {
        this.mNeedMoreIcon = value;
    }

    public int getMoreIconPosition() {
        return this.mMoreIconPosition;
    }

    public int getMoreIconTotalPosition() {
        return this.mMoreIconTotalPosition;
    }

    public int getMoreIconPageCount() {
        return this.mMoreIconPageCount;
    }

    public void setNeedAnim(boolean needAnim) {
        this.mNeedAnim = needAnim;
    }

    private void setCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        this.mMemoryCache = new LruCache<Integer, ColorItem[][]>(maxMemory / 8) {
            protected int sizeOf(Integer key, ColorItem[][] appinfo) {
                return maxMemory;
            }
        };
    }

    public void loadBitmap(int position, ColorGridView colorGridView) {
        ColorItem[][] appinfo = getBitmapFromMemCache(Integer.valueOf(position));
        if (appinfo == null) {
            try {
                appinfo = (ColorItem[][]) this.executor.submit(new BitmapTask(position)).get();
            } catch (CancellationException ce) {
                ce.printStackTrace();
            } catch (ExecutionException ee) {
                ee.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        if (appinfo != null) {
            if (this.mNeedMoreIcon && this.mMoreIconPageCount == position + 1) {
                initAppInfoTop(appinfo, this.mMoreIconPosition);
                colorGridView.setAppInfo(this.mAppInfoTop);
            } else {
                colorGridView.setAppInfo(appinfo);
                if (this.mNeedAnim && this.mMoreIconPageCount == position + 1) {
                    colorGridView.setMoreIconIndex(this.mMoreIconPosition);
                    colorGridView.startExpandAnimation();
                    this.mNeedAnim = false;
                }
            }
            colorGridView.setPagerSize(this.mPagerSize);
            colorGridView.setPageCount(position + 1);
            colorGridView.setOnItemClickListener(this);
        }
    }

    private void initAppInfoTop(ColorItem[][] appinfo, int iconPosition) {
        if (iconPosition < 4) {
            this.mAppInfoTop = (ColorItem[][]) Array.newInstance(ColorItem.class, new int[]{1, 4});
        } else {
            this.mAppInfoTop = (ColorItem[][]) Array.newInstance(ColorItem.class, new int[]{2, 4});
        }
        int index = 1;
        for (int i = 0; i < appinfo.length; i++) {
            for (int j = 0; j < appinfo[i].length; j++) {
                if (index < iconPosition + 1) {
                    this.mAppInfoTop[i][j] = appinfo[i][j];
                } else if (index == iconPosition + 1) {
                    this.mAppInfoTop[i][j] = this.mMoreItem;
                } else {
                    return;
                }
                index++;
            }
        }
    }

    public ColorItem[][] getBitmapFromMemCache(Integer key) {
        return (ColorItem[][]) this.mMemoryCache.get(key);
    }

    public void addBitmapToMemoryCache(Integer key, ColorItem[][] appinfo) {
        if (getBitmapFromMemCache(key) == null) {
            this.mMemoryCache.put(key, appinfo);
        }
    }

    public void OnItemClick(int position) {
        if (this.mColorResolverItemEventListener != null) {
            this.mColorResolverItemEventListener.OnItemClick(position);
        } else {
            IntentFilter filter = new IntentFilter();
            Intent intent = new Intent(this.mOriginIntent);
            intent.addFlags(50331648);
            if (position < this.mRiList.size() && position >= 0) {
                Log.d(TAG, "onItemClick : " + position + ", " + this.mRiList);
                ActivityInfo ai = ((ResolveInfo) this.mRiList.get(position)).activityInfo;
                intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
                if (!isInLockTaskMode()) {
                    safelyStartActivity(intent, this.mActivity, this.mSafeForwardingMode);
                    this.mActivity.overridePendingTransition(201981964, 201981968);
                }
                ResolveInfo ri = (ResolveInfo) this.mRiList.get(position);
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
                    filter.addCategory("android.intent.category.DEFAULT");
                    int cat2 = ri.match & 268369920;
                    Uri data = intent.getData();
                    if (cat2 == 6291456) {
                        String mimeType = intent.resolveType(this.mContext);
                        if (mimeType != null) {
                            try {
                                filter.addDataType(mimeType);
                            } catch (MalformedMimeTypeException e) {
                                filter = null;
                            }
                        }
                    }
                    if (data != null && data.getScheme() != null && (cat2 != 6291456 || (!"file".equals(data.getScheme()) && ("content".equals(data.getScheme()) ^ 1) != 0))) {
                        filter.addDataScheme(data.getScheme());
                        if (ri.filter != null) {
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
                            Iterator<PatternMatcher> pIt = ri.filter.pathsIterator();
                            if (pIt != null) {
                                String path = data.getPath();
                                while (path != null && pIt.hasNext()) {
                                    PatternMatcher p = (PatternMatcher) pIt.next();
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
                    for (int i = 0; i < N; i++) {
                        ResolveInfo r = (ResolveInfo) this.mRiList.get(i);
                        set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                        if (r.match > bestMatch) {
                            bestMatch = r.match;
                        }
                    }
                    this.mContext.getPackageManager().addPreferredActivity(filter, bestMatch, set, intent.getComponent());
                }
                dismiss();
            }
        }
    }

    public void OnItemLongClick(int position) {
        if (this.mColorResolverItemEventListener != null) {
            this.mColorResolverItemEventListener.OnItemLongClick(position);
        }
    }

    public void dismiss() {
        if (this.mAlertDialog != null) {
            this.mAlertDialog.dismiss();
        } else if (this.mActivity != null && (this.mActivity.isFinishing() ^ 1) != 0) {
            unRegister();
            this.mActivity.finish();
        }
    }

    public void unRegister() {
        if (this.mIsRegistered && this.mActivity != null) {
            try {
                this.mActivity.unregisterReceiver(this.mAPKChangedReceiver);
            } catch (Exception e) {
            }
            this.mIsRegistered = false;
        }
    }

    private static boolean isInLockTaskMode() {
        try {
            return ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void safelyStartActivity(Intent intent, Activity activity, boolean safeForwardingMode) {
        if (safeForwardingMode) {
            try {
                activity.startActivityAsCaller(intent, null, false, -10000);
                onActivityStarted(intent);
            } catch (RuntimeException e) {
                String launchedFromPackage;
                try {
                    launchedFromPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(activity.getActivityToken());
                } catch (RemoteException e2) {
                    launchedFromPackage = "??";
                }
                Log.d(TAG, " safelyStartActivity : " + launchedFromPackage);
            }
            return;
        }
        try {
            activity.startActivity(intent);
            onActivityStarted(intent);
        } catch (RuntimeException e3) {
            e3.printStackTrace();
        }
    }

    void onActivityStarted(Intent intent) {
        if (this.mChosenComponentSender != null) {
            ComponentName target = intent.getComponent();
            if (target != null) {
                try {
                    this.mChosenComponentSender.sendIntent(this.mContext, -1, new Intent().putExtra("android.intent.extra.CHOSEN_COMPONENT", target), null, null);
                } catch (SendIntentException e) {
                }
            }
        }
    }

    public void setChosenComponentSender(IntentSender is) {
        this.mChosenComponentSender = is;
    }

    public void updateIntent(Intent intent) {
        this.mOriginIntent = intent;
    }
}
