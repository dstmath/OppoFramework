package com.android.internal.app;

import android.app.ColorSystemUpdateDialog.ListItemAttr;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.android.internal.R;

public class ColorAlertControllerUpdate {
    private ListAdapter mAdapter;
    private int mAlertDialogLayout;
    private int mBoldTextPosition = -1;
    private int mCheckedItem = -1;
    private final Context mContext;
    private View mCustomTitleView;
    private final DialogInterface mDialogInterface;
    private boolean mForceInverseBackground;
    private Drawable mIcon;
    private int mIconId = 0;
    private ImageView mIconView;
    private int mListItemLayout;
    private int mListLayout;
    private ListView mListView;
    private CharSequence mMessage;
    private TextView mMessageView;
    private ScrollView mScrollView;
    private CharSequence mTitle;
    private TextView mTitleView;
    private View mView;
    private int mViewLayoutResId;
    private int mViewSpacingBottom;
    private int mViewSpacingLeft;
    private int mViewSpacingRight;
    private boolean mViewSpacingSpecified = false;
    private int mViewSpacingTop;
    private final Window mWindow;

    public static class AlertParams {
        public ListAdapter mAdapter;
        public boolean mCancelable;
        public int mCheckedItem = -1;
        public boolean[] mCheckedItems;
        public final Context mContext;
        public Cursor mCursor;
        public View mCustomTitleView;
        public boolean mForceInverseBackground;
        public final LayoutInflater mInflater;
        public String mIsCheckedColumn;
        public CharSequence[] mItems;
        public ListItemAttr[] mItemsAttrs;
        public String mLabelColumn;
        public CharSequence mMessage;
        public OnCancelListener mOnCancelListener;
        public OnClickListener mOnClickListener;
        public OnDismissListener mOnDismissListener;
        public OnItemSelectedListener mOnItemSelectedListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public boolean mRecycleOnMeasure = true;
        public CharSequence mTitle;
        public View mView;
        public int mViewLayoutResId;
        public int mViewSpacingBottom;
        public int mViewSpacingLeft;
        public int mViewSpacingRight;
        public boolean mViewSpacingSpecified = false;
        public int mViewSpacingTop;

        public interface OnPrepareListViewListener {
            void onPrepareListView(ListView listView);
        }

        public AlertParams(Context context) {
            this.mContext = context;
            this.mCancelable = true;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public void apply(ColorAlertControllerUpdate dialog) {
            if (this.mCustomTitleView != null) {
                dialog.setCustomTitle(this.mCustomTitleView);
            } else if (this.mTitle != null) {
                dialog.setTitle(this.mTitle);
            }
            if (this.mMessage != null) {
                dialog.setMessage(this.mMessage);
            }
            if (this.mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            if (!(this.mItems == null && this.mCursor == null && this.mAdapter == null)) {
                createListView(dialog);
            }
            if (this.mView != null) {
                if (this.mViewSpacingSpecified) {
                    dialog.setView(this.mView, this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
                    return;
                }
                dialog.setView(this.mView);
            } else if (this.mViewLayoutResId != 0) {
                dialog.setView(this.mViewLayoutResId);
            }
        }

        private void createListView(final ColorAlertControllerUpdate dialog) {
            ListAdapter adapter;
            RecycleListView listView = (RecycleListView) this.mInflater.inflate(dialog.mListLayout, null);
            int layout = dialog.mListItemLayout;
            if (this.mCursor != null) {
                adapter = new SimpleCursorAdapter(this.mContext, layout, this.mCursor, new String[]{this.mLabelColumn}, new int[]{R.id.text1});
            } else if (this.mAdapter != null) {
                adapter = this.mAdapter;
            } else {
                adapter = new CheckedItemAdapter(this.mContext, layout, R.id.text1, this.mItems, this.mItemsAttrs);
            }
            if (this.mOnPrepareListViewListener != null) {
                this.mOnPrepareListViewListener.onPrepareListView(listView);
            }
            dialog.mAdapter = adapter;
            dialog.mCheckedItem = this.mCheckedItem;
            if (this.mOnClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                        AlertParams.this.mOnClickListener.onClick(dialog.mDialogInterface, position);
                        dialog.mDialogInterface.dismiss();
                    }
                });
            }
            if (this.mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(this.mOnItemSelectedListener);
            }
            listView.mRecycleOnMeasure = this.mRecycleOnMeasure;
            listView.setSelector(201720878);
            dialog.mListView = listView;
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        private ListItemAttr[] mItemsAttrs;
        private boolean textBold;
        private int textColor;
        private int textId;

        public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
            super(context, resource, textViewResourceId, (Object[]) objects);
        }

        public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects, ListItemAttr[] itemsAttrs) {
            super(context, resource, textViewResourceId, (Object[]) objects);
            this.mItemsAttrs = itemsAttrs;
            this.textId = textViewResourceId;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View item = super.getView(position, convertView, parent);
            if (item != null) {
                TextView textView = (TextView) item.findViewById(this.textId);
                if (!(this.mItemsAttrs == null || this.mItemsAttrs[position] == null)) {
                    if (this.mItemsAttrs[position].getItemColor() != null) {
                        textView.setTextColor(this.mItemsAttrs[position].getItemColor().intValue());
                    }
                    if (this.mItemsAttrs[position].getItemBold() != null) {
                        textView.getPaint().setFakeBoldText(this.mItemsAttrs[position].getItemBold().booleanValue());
                    }
                }
            }
            int count = getCount();
            if (count > 1) {
                if (position == count - 1) {
                    item.setBackgroundResource(201852167);
                } else {
                    item.setBackgroundResource(201852166);
                }
            }
            return item;
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    public static class RecycleListView extends ListView {
        boolean mRecycleOnMeasure = true;

        public RecycleListView(Context context) {
            super(context);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        protected boolean recycleOnMeasure() {
            return this.mRecycleOnMeasure;
        }
    }

    public ColorAlertControllerUpdate(Context context, DialogInterface di, Window window) {
        this.mContext = context;
        this.mDialogInterface = di;
        this.mWindow = window;
        this.mListItemLayout = 201917553;
        this.mListLayout = 201917552;
        this.mAlertDialogLayout = 201917551;
    }

    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            if (canTextInput(vg.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    public void installContent() {
        this.mWindow.requestFeature(1);
        this.mWindow.setContentView(selectContentView());
        ColorAlertControllerUpdate.updateWindow(this, this.mContext, this.mWindow);
        setupView();
        setupDecor();
    }

    private int selectContentView() {
        return this.mAlertDialogLayout;
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
        }
    }

    public void setCustomTitle(View customTitleView) {
        this.mCustomTitleView = customTitleView;
    }

    public void setMessage(CharSequence message) {
        this.mMessage = message;
        if (this.mMessageView != null) {
            this.mMessageView.setText(message);
        }
    }

    public void setView(int layoutResId) {
        this.mView = null;
        this.mViewLayoutResId = layoutResId;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View view) {
        this.mView = view;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        this.mView = view;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = true;
        this.mViewSpacingLeft = viewSpacingLeft;
        this.mViewSpacingTop = viewSpacingTop;
        this.mViewSpacingRight = viewSpacingRight;
        this.mViewSpacingBottom = viewSpacingBottom;
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        this.mForceInverseBackground = forceInverseBackground;
    }

    public ListView getListView() {
        return this.mListView;
    }

    private void setupDecor() {
        View decor = this.mWindow.getDecorView();
        final View parent = this.mWindow.findViewById(R.id.parentPanel);
        if (parent != null && decor != null) {
            decor.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                    if (insets.isRound()) {
                        int roundOffset = ColorAlertControllerUpdate.this.mContext.getResources().getDimensionPixelOffset(R.dimen.alert_dialog_round_padding);
                        parent.setPadding(roundOffset, roundOffset, roundOffset, roundOffset);
                    }
                    return insets.consumeSystemWindowInsets();
                }
            });
            decor.setFitsSystemWindows(true);
            decor.requestApplyInsets();
        }
    }

    private void setupView() {
        View customView;
        ViewGroup contentPanel = (ViewGroup) this.mWindow.findViewById(R.id.contentPanel);
        setupContent(contentPanel);
        ViewGroup topPanel = (ViewGroup) this.mWindow.findViewById(R.id.topPanel);
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.AlertDialog, R.attr.alertDialogStyle, 0);
        boolean hasTitle = setupTitle(topPanel);
        ViewGroup customPanel = (FrameLayout) this.mWindow.findViewById(R.id.customPanel);
        if (this.mView != null) {
            customView = this.mView;
        } else if (this.mViewLayoutResId != 0) {
            customView = LayoutInflater.from(this.mContext).inflate(this.mViewLayoutResId, customPanel, false);
        } else {
            customView = null;
        }
        boolean hasCustomView = customView != null;
        if (!(hasCustomView && (canTextInput(customView) ^ 1) == 0)) {
            this.mWindow.setFlags(131072, 131072);
        }
        if (hasCustomView) {
            FrameLayout custom = (FrameLayout) this.mWindow.findViewById(R.id.custom);
            custom.addView(customView, new LayoutParams(-1, -1));
            if (this.mViewSpacingSpecified) {
                custom.setPadding(this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
            }
            if (this.mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0.0f;
            }
        } else {
            customPanel.setVisibility(8);
        }
        if (hasTitle) {
            View divider;
            if (this.mMessage == null && customView == null && this.mListView == null) {
                divider = this.mWindow.findViewById(R.id.titleDividerTop);
            } else {
                divider = this.mWindow.findViewById(R.id.titleDivider);
            }
            if (divider != null) {
                divider.setVisibility(0);
            }
        }
        setBackground(a, topPanel, contentPanel, customPanel, hasTitle, hasCustomView);
        a.recycle();
    }

    private boolean setupTitle(ViewGroup topPanel) {
        if (this.mCustomTitleView != null) {
            topPanel.addView(this.mCustomTitleView, 0, new LayoutParams(-1, -2));
            this.mWindow.findViewById(R.id.title_template).setVisibility(8);
            return true;
        }
        this.mIconView = (ImageView) this.mWindow.findViewById(R.id.icon);
        if (TextUtils.isEmpty(this.mTitle) ^ 1) {
            this.mTitleView = (TextView) this.mWindow.findViewById(R.id.alertTitle);
            this.mTitleView.setText(this.mTitle);
            if (this.mIconId != 0) {
                this.mIconView.setImageResource(this.mIconId);
                return true;
            } else if (this.mIcon != null) {
                this.mIconView.setImageDrawable(this.mIcon);
                return true;
            } else {
                this.mTitleView.setPadding(this.mIconView.getPaddingLeft(), this.mIconView.getPaddingTop(), this.mIconView.getPaddingRight(), this.mIconView.getPaddingBottom());
                this.mIconView.setVisibility(8);
                return true;
            }
        }
        this.mWindow.findViewById(R.id.title_template).setVisibility(8);
        this.mIconView.setVisibility(8);
        topPanel.setVisibility(8);
        return false;
    }

    private void setupContent(ViewGroup contentPanel) {
        this.mScrollView = (ScrollView) this.mWindow.findViewById(R.id.scrollView);
        this.mScrollView.setFocusable(false);
        this.mMessageView = (TextView) this.mWindow.findViewById(R.id.message);
        if (this.mMessageView != null) {
            if (this.mMessage != null) {
                this.mMessageView.setText(this.mMessage);
            } else {
                this.mMessageView.setVisibility(8);
                this.mScrollView.removeView(this.mMessageView);
            }
            if (this.mListView != null) {
                ViewGroup scrollParent = (ViewGroup) this.mScrollView.getParent();
                int childIndex = scrollParent.indexOfChild(this.mScrollView);
                scrollParent.removeViewAt(childIndex);
                scrollParent.addView(this.mListView, childIndex, new LayoutParams(-1, -1));
            } else {
                ((ViewGroup) this.mScrollView.getParent()).setVisibility(8);
            }
        }
    }

    private void setBackground(TypedArray a, View topPanel, View contentPanel, View customPanel, boolean hasTitle, boolean hasCustomView) {
        int i;
        int fullDark = 0;
        int topDark = 0;
        int centerDark = 0;
        int bottomDark = 0;
        int fullBright = 0;
        int topBright = 0;
        int centerBright = 0;
        int bottomBright = 0;
        int bottomMedium = 0;
        if (a.getBoolean(17, true)) {
            fullDark = R.drawable.popup_full_dark;
            topDark = R.drawable.popup_top_dark;
            centerDark = R.drawable.popup_center_dark;
            bottomDark = R.drawable.popup_bottom_dark;
            fullBright = R.drawable.popup_full_bright;
            topBright = R.drawable.popup_top_bright;
            centerBright = R.drawable.popup_center_bright;
            bottomBright = R.drawable.popup_bottom_bright;
            bottomMedium = R.drawable.popup_bottom_medium;
        }
        topBright = a.getResourceId(5, topBright);
        topDark = a.getResourceId(1, topDark);
        centerBright = a.getResourceId(6, centerBright);
        centerDark = a.getResourceId(2, centerDark);
        View[] views = new View[4];
        boolean[] light = new boolean[4];
        View lastView = null;
        boolean lastLight = false;
        int pos = 0;
        if (hasTitle) {
            views[0] = topPanel;
            light[0] = false;
            pos = 1;
        }
        if (contentPanel.getVisibility() == 8) {
            contentPanel = null;
        }
        views[pos] = contentPanel;
        light[pos] = this.mListView != null;
        pos++;
        if (hasCustomView) {
            views[pos] = customPanel;
            light[pos] = this.mForceInverseBackground;
            pos++;
        }
        boolean setView = false;
        for (pos = 0; pos < views.length; pos++) {
            View v = views[pos];
            if (v != null) {
                if (lastView != null) {
                    if (setView) {
                        lastView.setBackgroundResource(lastLight ? centerBright : centerDark);
                    } else {
                        if (lastLight) {
                            i = topBright;
                        } else {
                            i = topDark;
                        }
                        lastView.setBackgroundResource(i);
                    }
                    setView = true;
                }
                lastView = v;
                lastLight = light[pos];
            }
        }
        if (lastView != null) {
            if (setView) {
                bottomBright = a.getResourceId(7, bottomBright);
                bottomMedium = a.getResourceId(8, bottomMedium);
                bottomDark = a.getResourceId(3, bottomDark);
                if (lastLight) {
                    i = bottomBright;
                } else {
                    i = bottomDark;
                }
                lastView.setBackgroundResource(i);
            } else {
                lastView.setBackgroundResource(lastLight ? a.getResourceId(4, fullBright) : a.getResourceId(0, fullDark));
            }
        }
        ListView listView = this.mListView;
        if (listView != null && this.mAdapter != null) {
            listView.setAdapter(this.mAdapter);
            int checkedItem = this.mCheckedItem;
            if (checkedItem > -1) {
                listView.setItemChecked(checkedItem, true);
                listView.setSelection(checkedItem);
            }
            this.mListView.setOverScrollMode(2);
        }
    }
}
