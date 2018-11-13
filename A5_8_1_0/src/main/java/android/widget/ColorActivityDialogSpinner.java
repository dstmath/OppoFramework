package android.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AdapterView.OnItemClickListener;
import com.android.internal.R;
import com.color.actionbar.app.ColorActionBarUtil;

public class ColorActivityDialogSpinner extends Spinner {
    private static final int MODE_THEME = -1;
    public static final int RightLeft = 1;
    public static final int STYLE_GROUP = 0;
    public static final int STYLE_TILE = 1;
    private static final String TAG = "ColorActivityDialogSpinner";
    public static final int UpDown = 0;
    private String mDefaultBackTitle;
    private int mDialogPopuTheme;
    private int mGroupPadding;
    private int mListViewListStyle;

    private class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context context, int resource, CharSequence[] objects) {
            super(context, resource, (Object[]) objects);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(LayoutInflater.from(ColorActivityDialogSpinner.this.mContext), position, convertView, parent, 201917550);
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        private View createViewFromResource(LayoutInflater inflater, int position, View convertView, ViewGroup parent, int resource) {
            View view;
            if (convertView == null) {
                view = inflater.inflate(resource, parent, false);
            } else {
                view = convertView;
            }
            TextView text = (TextView) view.findViewById(201458933);
            if (text != null) {
                Object item = getItem(position);
                if (item instanceof CharSequence) {
                    text.setText((CharSequence) item);
                } else {
                    text.setText(item.toString());
                }
            }
            int groupSize = getCount();
            View diver = view.findViewById(201458963);
            if (groupSize == 1 || position == groupSize - 1) {
                if (diver != null) {
                    diver.setVisibility(8);
                }
            } else if (diver != null) {
                diver.setVisibility(0);
            }
            return view;
        }
    }

    private class ColorActivityDialogPopup implements SpinnerPopup, OnClickListener {
        private ListAdapter mListAdapter;
        private Dialog mPopup;
        private CharSequence mPrompt;
        private int mSelectedItem;

        /* synthetic */ ColorActivityDialogPopup(ColorActivityDialogSpinner this$0, ColorActivityDialogPopup -this1) {
            this();
        }

        private ColorActivityDialogPopup() {
        }

        public void dismiss() {
            if (this.mPopup != null) {
                this.mPopup.dismiss();
                this.mPopup = null;
            }
        }

        public boolean isShowing() {
            return this.mPopup != null ? this.mPopup.isShowing() : false;
        }

        public void setAdapter(ListAdapter adapter) {
            this.mListAdapter = adapter;
        }

        public void setPromptText(CharSequence hintText) {
            this.mPrompt = hintText;
        }

        public CharSequence getHintText() {
            return this.mPrompt;
        }

        public void show(int textDirection, int textAlignment) {
            if (this.mListAdapter != null) {
                this.mSelectedItem = ColorActivityDialogSpinner.this.getSelectedItemPosition();
                this.mPopup = new Dialog(ColorActivityDialogSpinner.this.getContext(), ColorActivityDialogSpinner.this.mDialogPopuTheme) {
                    public boolean onMenuItemSelected(int featureId, MenuItem item) {
                        if (item.getItemId() != R.id.home) {
                            return super.onMenuItemSelected(featureId, item);
                        }
                        dismiss();
                        return true;
                    }
                };
                this.mPopup.getWindow().addFlags(Integer.MIN_VALUE);
                this.mPopup.getWindow().getDecorView().setSystemUiVisibility(1280);
                this.mPopup.getWindow().getDecorView().setSystemUiVisibility(8192);
                final ListView list = (ListView) LayoutInflater.from(ColorActivityDialogSpinner.this.getContext()).inflate(201917549, null);
                if (!(list == null || ColorActivityDialogSpinner.this.mAdapter == null)) {
                    list.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                            if (!(v == null || insets == null)) {
                                v.setPadding(v.getPaddingStart(), insets.getSystemWindowInsetTop() + ColorActivityDialogSpinner.this.getContext().getResources().getDimensionPixelOffset(201654459), v.getPaddingEnd(), v.getPaddingBottom());
                            }
                            return insets;
                        }
                    });
                    list.setChoiceMode(1);
                    list.setAdapter(this.mListAdapter);
                    list.setTextDirection(textDirection);
                    list.setTextAlignment(textAlignment);
                    int checkedItem = ColorActivityDialogSpinner.this.getSelectedItemPosition();
                    if (checkedItem > -1) {
                        list.setItemChecked(list.getHeaderViewsCount() + checkedItem, true);
                        list.setSelection(checkedItem);
                    }
                }
                list.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                        list.setItemChecked(position, true);
                        ColorActivityDialogSpinner.this.setSelection(position - list.getHeaderViewsCount());
                        ColorActivityDialogPopup.this.mPopup.dismiss();
                    }
                });
                this.mPopup.setContentView(list);
                this.mPopup.show();
                CharSequence charSequence = null;
                if (ColorActivityDialogSpinner.this.getContext() instanceof Activity) {
                    ActionBar actActionbar = ((Activity) ColorActivityDialogSpinner.this.getContext()).getActionBar();
                    if (actActionbar != null) {
                        charSequence = (String) actActionbar.getTitle();
                    }
                }
                ActionBar act = this.mPopup.getActionBar();
                if (act != null) {
                    if (this.mPrompt != null) {
                        this.mPopup.setTitle(this.mPrompt);
                        act.setTitle(this.mPrompt);
                    }
                    act.setDisplayHomeAsUpEnabled(true);
                    if (charSequence == null || charSequence.equals("")) {
                        charSequence = ColorActivityDialogSpinner.this.mDefaultBackTitle;
                    }
                    ColorActionBarUtil.setBackTitle(act, charSequence);
                }
            }
        }

        public void onClick(DialogInterface dialog, int which) {
            ColorActivityDialogSpinner.this.setSelection(which);
            if (ColorActivityDialogSpinner.this.mOnItemClickListener != null) {
                ColorActivityDialogSpinner.this.performItemClick(null, which, this.mListAdapter.getItemId(which));
            }
            dismiss();
        }

        public void setBackgroundDrawable(Drawable bg) {
            Log.e(ColorActivityDialogSpinner.TAG, "Cannot set popup background for MODE_DIALOG, ignoring");
        }

        public void setVerticalOffset(int px) {
            Log.e(ColorActivityDialogSpinner.TAG, "Cannot set vertical offset for MODE_DIALOG, ignoring");
        }

        public void setHorizontalOffset(int px) {
            Log.e(ColorActivityDialogSpinner.TAG, "Cannot set horizontal offset for MODE_DIALOG, ignoring");
        }

        public Drawable getBackground() {
            return null;
        }

        public int getVerticalOffset() {
            return 0;
        }

        public int getHorizontalOffset() {
            return 0;
        }
    }

    public ColorActivityDialogSpinner(Context context) {
        this(context, null);
    }

    public ColorActivityDialogSpinner(Context context, int mode) {
        this(context, null, 201393313, mode);
    }

    public ColorActivityDialogSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 201393313);
    }

    public ColorActivityDialogSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0, -1);
    }

    public ColorActivityDialogSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, 0, mode);
    }

    public ColorActivityDialogSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
        this.mListViewListStyle = 0;
        this.mDialogPopuTheme = 201524239;
        this.mGroupPadding = 0;
        this.mPopup = new ColorActivityDialogPopup(this, null);
        TypedArray a = context.obtainStyledAttributes(attrs, android.R.styleable.Spinner, defStyleAttr, defStyleRes);
        this.mPopup.setPromptText(a.getString(3));
        a.recycle();
        TypedArray abs = context.obtainStyledAttributes(attrs, android.R.styleable.AbsSpinner, defStyleAttr, defStyleRes);
        CharSequence[] entries = abs.getTextArray(0);
        if (entries != null) {
            setAdapter(new CheckedItemAdapter(context, R.layout.simple_spinner_item, entries));
        }
        abs.recycle();
        this.mGroupPadding = context.getResources().getDimensionPixelSize(201655564);
        this.mDefaultBackTitle = context.getResources().getString(201590122);
    }

    public void setDialogPopup(int theme) {
        this.mDialogPopuTheme = theme;
    }

    public void setAnimationMode(int mode) {
        if (mode == 0) {
            this.mDialogPopuTheme = 201524240;
        } else if (mode == 1) {
            this.mDialogPopuTheme = 201524239;
        } else {
            throw new RuntimeException("mode is error! a mode:" + mode);
        }
    }

    public void setListStyle(int style) {
        this.mListViewListStyle = style;
    }
}
