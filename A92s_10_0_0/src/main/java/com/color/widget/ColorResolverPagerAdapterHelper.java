package com.color.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ColorBaseResolveInfo;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import com.android.internal.widget.DefaultItemAnimator;
import com.android.internal.widget.GridLayoutManager;
import com.android.internal.widget.RecyclerView;
import com.color.util.ColorTypeCastingHelper;
import com.color.widget.ColorResolverGridAdapter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColorResolverPagerAdapterHelper implements ColorResolverGridAdapter.OnItemClickListener {
    private static final String TAG = "ColorResolverPagerAdapterHelper";
    private ExecutorService executor = Executors.newCachedThreadPool();
    private Dialog mAlertDialog;
    /* access modifiers changed from: private */
    public Context mContext;
    private ColorResolverGridAdapter.OnItemClickListener mItemClickListener;
    private LruCache<String, ColorItem> mMemoryCache = new LruCache<>(100);
    private int mMoreIconTotalPosition;
    private ColorItem mMoreItem = new ColorItem();
    private boolean mNeedAnimation = false;
    private boolean mNeedMoreIcon = false;
    /* access modifiers changed from: private */
    public ColorResolveInfoHelper mResolveInfoHelper;

    public ColorResolverPagerAdapterHelper(Context context, Dialog dialog) {
        this.mContext = context;
        this.mAlertDialog = dialog;
        this.mResolveInfoHelper = ColorResolveInfoHelper.getInstance(context);
        this.mMoreItem.setText(this.mContext.getString(201590158));
        this.mMoreItem.setIcon(this.mContext.getDrawable(201852207));
    }

    public View createPagerView(List<ColorItem> appinfo, int pagerNumber, int pagerSize) {
        RecyclerView oppoGridView = new RecyclerView(this.mContext);
        oppoGridView.setOverScrollMode(2);
        oppoGridView.setItemAnimator(new DefaultItemAnimator());
        oppoGridView.setLayoutManager(new GridLayoutManager(this.mContext, 4));
        ColorResolverGridAdapter adapter = new ColorResolverGridAdapter(this.mContext);
        if (appinfo != null) {
            int i = this.mMoreIconTotalPosition;
            int moreIconPosition = i % pagerSize;
            int moreIconPageCount = (int) Math.ceil(((double) (i + 1)) / ((double) pagerSize));
            if (!this.mNeedMoreIcon || moreIconPageCount != pagerNumber + 1) {
                adapter.setColorItems(appinfo);
                if (this.mNeedAnimation && moreIconPageCount == pagerNumber + 1) {
                    this.mNeedAnimation = false;
                    adapter.startMoreAnimation(moreIconPosition);
                }
            } else {
                adapter.setColorItems(initAppInfoTop(appinfo, moreIconPosition));
            }
            adapter.setPagerNumber(pagerNumber);
            adapter.setOnItemClickListener(this);
        }
        oppoGridView.setAdapter(adapter);
        return oppoGridView;
    }

    /* access modifiers changed from: package-private */
    public ColorItem[][] listToArray(List<ColorItem> colorItems) {
        int rowCounts = (int) Math.min(Math.ceil(((double) colorItems.size()) / 4.0d), 2.0d);
        ColorItem[][] array = (ColorItem[][]) Array.newInstance(ColorItem.class, rowCounts, 4);
        int start = 0;
        int end = 0 + 4;
        int i = 0;
        while (start < colorItems.size() && i < rowCounts) {
            List<ColorItem> l = colorItems.subList(start, end < colorItems.size() ? end : colorItems.size());
            System.arraycopy(l.toArray(), 0, array[i], 0, l.size());
            start = end;
            end = start + 4;
            i++;
        }
        return array;
    }

    /* access modifiers changed from: package-private */
    public void setColorResolverItemEventListener(ColorResolverGridAdapter.OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void clickMoreIcon() {
        this.mNeedAnimation = true;
        this.mNeedMoreIcon = false;
    }

    /* access modifiers changed from: package-private */
    public boolean isNeedMoreIcon() {
        return this.mNeedMoreIcon;
    }

    /* access modifiers changed from: package-private */
    public int getMoreIconTotalPosition() {
        return this.mMoreIconTotalPosition;
    }

    /* access modifiers changed from: package-private */
    public boolean isMoreIconPosition(int position) {
        return this.mNeedMoreIcon && this.mMoreIconTotalPosition == position;
    }

    /* access modifiers changed from: package-private */
    public void updateNeedMoreIcon(Intent intent, int allRiListSize) {
        boolean isChooser = this.mResolveInfoHelper.isChooserAction(intent);
        int resolveTopSize = this.mResolveInfoHelper.getResolveTopSize(intent);
        if (isChooser || resolveTopSize <= 0 || resolveTopSize >= allRiListSize) {
            this.mNeedMoreIcon = false;
            this.mMoreIconTotalPosition = 0;
        } else {
            this.mNeedMoreIcon = true;
            this.mMoreIconTotalPosition = resolveTopSize;
        }
        Log.d(TAG, "init:resolveTopSize=" + resolveTopSize + ",mRiList=" + allRiListSize + ",mNeedMoreIcon=" + this.mNeedMoreIcon);
    }

    /* access modifiers changed from: package-private */
    public void dismiss() {
        Dialog dialog = this.mAlertDialog;
        if (dialog == null || !dialog.isShowing()) {
            Context context = this.mContext;
            if (context != null && (context instanceof Activity) && !((Activity) context).isFinishing()) {
                ((Activity) this.mContext).finish();
                return;
            }
            return;
        }
        this.mAlertDialog.dismiss();
    }

    public List<ColorItem> loadBitmap(List<ResolveInfo> riList, int pagerNumber, int pagerSize, int placeholderCount) {
        int i;
        if (riList == null || riList.isEmpty()) {
            ColorResolveInfoHelper colorResolveInfoHelper = this.mResolveInfoHelper;
            if ((pagerNumber + 1) * pagerSize > placeholderCount) {
                i = placeholderCount - (pagerNumber * pagerSize);
            } else {
                i = pagerSize;
            }
            return colorResolveInfoHelper.getDefaultAppInfo(i);
        }
        try {
            return (List) this.executor.submit(new BitmapTask(riList, pagerNumber, pagerSize)).get();
        } catch (CancellationException ce) {
            ce.printStackTrace();
            return null;
        } catch (ExecutionException ee) {
            ee.printStackTrace();
            return null;
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            return null;
        }
    }

    private List<ColorItem> initAppInfoTop(List<ColorItem> appinfo, int iconPosition) {
        List<ColorItem> appInfoTop = new ArrayList<>();
        int i = 0;
        int length = appinfo.size();
        while (true) {
            if (i >= length) {
                break;
            } else if (i == iconPosition) {
                appInfoTop.add(this.mMoreItem);
                break;
            } else {
                appInfoTop.add(appinfo.get(i));
                i++;
            }
        }
        return appInfoTop;
    }

    /* access modifiers changed from: private */
    public ColorItem getBitmapFromMemCache(String key) {
        return this.mMemoryCache.get(key);
    }

    /* access modifiers changed from: private */
    public void addBitmapToMemoryCache(String key, ColorItem appinfo) {
        if (getBitmapFromMemCache(key) == null) {
            this.mMemoryCache.put(key, appinfo);
        }
    }

    @Override // com.color.widget.ColorResolverGridAdapter.OnItemClickListener
    public void onItemClick(int pagerNumber, int position) {
        ColorResolverGridAdapter.OnItemClickListener onItemClickListener = this.mItemClickListener;
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(pagerNumber, position);
        }
    }

    @Override // com.color.widget.ColorResolverGridAdapter.OnItemClickListener
    public void onItemLongClick(int pagerNumber, int position) {
        ColorResolverGridAdapter.OnItemClickListener onItemClickListener = this.mItemClickListener;
        if (onItemClickListener != null) {
            onItemClickListener.onItemLongClick(pagerNumber, position);
        }
    }

    private class BitmapTask implements Callable<List<ColorItem>> {
        private int mPagerNumber;
        private int mPagerSize;
        private List<ResolveInfo> mRiList;

        BitmapTask(List<ResolveInfo> riList, int pagerNumber, int pagerSize) {
            this.mRiList = riList;
            this.mPagerNumber = pagerNumber;
            this.mPagerSize = pagerSize;
        }

        @Override // java.util.concurrent.Callable
        public List<ColorItem> call() {
            int i;
            List<ColorItem> colorItems = new ArrayList<>();
            List<ResolveInfo> list = this.mRiList;
            int i2 = this.mPagerNumber;
            int i3 = this.mPagerSize;
            int i4 = i2 * i3;
            if ((i2 + 1) * i3 > list.size()) {
                i = this.mRiList.size();
            } else {
                i = (this.mPagerNumber + 1) * this.mPagerSize;
            }
            for (ResolveInfo info : list.subList(i4, i)) {
                boolean isMulti = ColorResolverPagerAdapterHelper.this.isMultiApp(info);
                ActivityInfo ai = info.activityInfo;
                String key = ai.applicationInfo.packageName + ai.name + isMulti;
                ColorItem item = ColorResolverPagerAdapterHelper.this.getBitmapFromMemCache(key);
                if (item == null) {
                    item = ColorResolverPagerAdapterHelper.this.mResolveInfoHelper.getAppInfo(info, ColorResolverPagerAdapterHelper.this.mContext.getPackageManager(), isMulti);
                }
                if (item != null) {
                    colorItems.add(item);
                    ColorResolverPagerAdapterHelper.this.addBitmapToMemoryCache(key, item);
                }
            }
            return colorItems;
        }
    }

    /* access modifiers changed from: private */
    public boolean isMultiApp(ResolveInfo resolveInfo) {
        ColorBaseResolveInfo baseResolveInfo = (ColorBaseResolveInfo) ColorTypeCastingHelper.typeCasting(ColorBaseResolveInfo.class, resolveInfo);
        if (baseResolveInfo != null) {
            return baseResolveInfo.isMultiApp;
        }
        return false;
    }
}
