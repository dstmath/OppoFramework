package com.color.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ColorExpandableListItemAdapter<T> extends ArrayAdapter<T> {
    private static final int DEFAULTCONTENTPARENTRESID = 10001;
    private static final int DEFAULTTITLEPARENTRESID = 10000;
    private int mActionViewResId;
    private int mContentParentResId;
    private Context mContext;
    private Map<Long, View> mExpandedViews;
    private int mLimit;
    private OnExpandGroupClickListener mOnExpandGroupClickListener;
    private int mTitleParentResId;
    private int mViewLayoutResId;
    private List<Long> mVisibleIds;

    public interface OnExpandGroupClickListener {
        void onExpandGroupClick(View view);
    }

    private static class RootView extends LinearLayout {
        private ViewGroup mContentViewGroup;
        private ViewGroup mTitleViewGroup;

        public RootView(Context context) {
            super(context);
            init();
        }

        private void init() {
            setOrientation(1);
            this.mTitleViewGroup = new FrameLayout(getContext());
            this.mTitleViewGroup.setId(ColorExpandableListItemAdapter.DEFAULTTITLEPARENTRESID);
            addView(this.mTitleViewGroup);
            this.mContentViewGroup = new FrameLayout(getContext());
            this.mContentViewGroup.setId(ColorExpandableListItemAdapter.DEFAULTCONTENTPARENTRESID);
            addView(this.mContentViewGroup);
        }
    }

    private class TitleViewOnClickListener implements OnClickListener {
        private View mContentParent;

        /* synthetic */ TitleViewOnClickListener(ColorExpandableListItemAdapter this$0, View contentParent, TitleViewOnClickListener -this2) {
            this(contentParent);
        }

        private TitleViewOnClickListener(View contentParent) {
            this.mContentParent = contentParent;
        }

        public void onClick(View view) {
            if (!ColorExpandCollapseHelper.isCollapsing && !ColorExpandCollapseHelper.isExpanding) {
                boolean isVisible = this.mContentParent.getVisibility() == 0;
                if (!isVisible && ColorExpandableListItemAdapter.this.mLimit > 0 && ColorExpandableListItemAdapter.this.mVisibleIds.size() >= ColorExpandableListItemAdapter.this.mLimit) {
                    View firstEV = (View) ColorExpandableListItemAdapter.this.mExpandedViews.get((Long) ColorExpandableListItemAdapter.this.mVisibleIds.get(0));
                    if (firstEV != null) {
                        ColorExpandCollapseHelper.animateCollapsing(((ViewHolder) firstEV.getTag()).contentParent);
                        ColorExpandableListItemAdapter.this.mExpandedViews.remove(ColorExpandableListItemAdapter.this.mVisibleIds.get(0));
                    }
                    ColorExpandableListItemAdapter.this.mVisibleIds.remove(ColorExpandableListItemAdapter.this.mVisibleIds.get(0));
                }
                if (isVisible) {
                    ColorExpandCollapseHelper.animateCollapsing(this.mContentParent);
                    ColorExpandableListItemAdapter.this.mVisibleIds.remove(this.mContentParent.getTag());
                    ColorExpandableListItemAdapter.this.mExpandedViews.remove(this.mContentParent.getTag());
                } else {
                    ColorExpandCollapseHelper.animateExpanding(this.mContentParent);
                    ColorExpandableListItemAdapter.this.mVisibleIds.add((Long) this.mContentParent.getTag());
                    if (ColorExpandableListItemAdapter.this.mLimit > 0) {
                        ColorExpandableListItemAdapter.this.mExpandedViews.put((Long) this.mContentParent.getTag(), (View) this.mContentParent.getParent());
                    }
                }
                if (ColorExpandableListItemAdapter.this.mOnExpandGroupClickListener != null) {
                    ColorExpandableListItemAdapter.this.mOnExpandGroupClickListener.onExpandGroupClick(view);
                }
            }
        }
    }

    private static class ViewHolder {
        ViewGroup contentParent;
        View contentView;
        ViewGroup titleParent;
        View titleView;

        /* synthetic */ ViewHolder(ViewHolder -this0) {
            this();
        }

        private ViewHolder() {
        }
    }

    public abstract View getContentView(int i, View view, ViewGroup viewGroup);

    public abstract View getTitleView(int i, View view, ViewGroup viewGroup);

    protected ColorExpandableListItemAdapter(Context context, int resource) {
        this(context, resource, null);
    }

    protected ColorExpandableListItemAdapter(Context context, int resource, List<T> items) {
        super(context, resource, items);
        this.mContext = context;
        this.mTitleParentResId = DEFAULTTITLEPARENTRESID;
        this.mContentParentResId = DEFAULTCONTENTPARENTRESID;
        this.mVisibleIds = new ArrayList();
    }

    protected ColorExpandableListItemAdapter(Context context, int layoutResId, int titleParentResId, int contentParentResId) {
        this(context, layoutResId, titleParentResId, contentParentResId, null);
    }

    protected ColorExpandableListItemAdapter(Context context, int layoutResId, int titleParentResId, int contentParentResId, List<T> items) {
        super(context, layoutResId, items);
        this.mContext = context;
        this.mViewLayoutResId = layoutResId;
        this.mTitleParentResId = titleParentResId;
        this.mContentParentResId = contentParentResId;
        this.mVisibleIds = new ArrayList();
        this.mExpandedViews = new HashMap();
    }

    public void setActionViewResId(int resId) {
        this.mActionViewResId = resId;
    }

    public void setLimit(int limit) {
        this.mLimit = limit;
        this.mVisibleIds.clear();
        this.mExpandedViews.clear();
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        ViewGroup view = (ViewGroup) convertView;
        if (view == null) {
            view = createView(parent);
            viewHolder = new ViewHolder();
            viewHolder.titleParent = (ViewGroup) view.findViewById(this.mTitleParentResId);
            viewHolder.contentParent = (ViewGroup) view.findViewById(this.mContentParentResId);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
            viewHolder.titleParent = (ViewGroup) view.findViewById(this.mTitleParentResId);
            viewHolder.contentParent = (ViewGroup) view.findViewById(this.mContentParentResId);
        }
        if (this.mLimit > 0) {
            if (this.mVisibleIds.contains(Long.valueOf(getItemId(position)))) {
                this.mExpandedViews.put(Long.valueOf(getItemId(position)), view);
            } else if (this.mExpandedViews.containsValue(view) && (this.mVisibleIds.contains(Long.valueOf(getItemId(position))) ^ 1) != 0) {
                this.mExpandedViews.remove(Long.valueOf(getItemId(position)));
            }
        }
        View titleView = getTitleView(position, viewHolder.titleView, viewHolder.titleParent);
        if (titleView != viewHolder.titleView) {
            viewHolder.titleParent.removeAllViews();
            viewHolder.titleParent.addView(titleView);
            if (this.mActionViewResId == 0) {
                view.setOnClickListener(new TitleViewOnClickListener(this, viewHolder.contentParent, null));
            } else {
                view.findViewById(this.mActionViewResId).setOnClickListener(new TitleViewOnClickListener(this, viewHolder.contentParent, null));
            }
        }
        viewHolder.titleView = titleView;
        View contentView = getContentView(position, viewHolder.contentView, viewHolder.contentParent);
        if (contentView != viewHolder.contentView) {
            viewHolder.contentParent.removeAllViews();
            viewHolder.contentParent.addView(contentView);
        }
        viewHolder.contentView = contentView;
        viewHolder.contentParent.setVisibility(this.mVisibleIds.contains(Long.valueOf(getItemId(position))) ? 0 : 8);
        viewHolder.contentParent.setTag(Long.valueOf(getItemId(position)));
        return view;
    }

    private ViewGroup createView(ViewGroup parent) {
        if (this.mViewLayoutResId == 0) {
            return new RootView(this.mContext);
        }
        return (ViewGroup) LayoutInflater.from(this.mContext).inflate(this.mViewLayoutResId, parent, false);
    }

    public boolean isExpand(int position) {
        return this.mVisibleIds.contains(Long.valueOf(getItemId(position)));
    }

    public void setOnExpandGroupClickListener(OnExpandGroupClickListener onExpandGroupClickListener) {
        this.mOnExpandGroupClickListener = onExpandGroupClickListener;
    }
}
