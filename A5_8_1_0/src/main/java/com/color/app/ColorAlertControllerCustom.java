package com.color.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.color.app.ColorAlertController.RecycleListView;
import com.color.app.ColorAlertDialogCustom.ListItemAttr;

public class ColorAlertControllerCustom extends ColorAlertController {
    private static final String TAG = "ColorAlertControllerCustom";
    private boolean mIsDynaChange = false;
    private ListItemAttr[] mItemsAttrs;

    public static class AlertParams extends com.color.app.ColorAlertController.AlertParams {
        public ListItemAttr[] mItemsAttrs;

        public AlertParams(Context context) {
            super(context);
        }

        protected void createListView(final ColorAlertController dialog) {
            ListAdapter adapter;
            final ColorAlertControllerCustom customDialog = (ColorAlertControllerCustom) dialog;
            dialog.mListLayout = 201917561;
            RecycleListView listView = (RecycleListView) this.mInflater.inflate(dialog.mListLayout, null);
            if (this.mCursor != null) {
                ListAdapter simpleCursorAdapter = new SimpleCursorAdapter(this.mContext, 201917553, this.mCursor, new String[]{this.mLabelColumn}, new int[]{16908308});
            } else if (this.mAdapter != null) {
                adapter = this.mAdapter;
            } else {
                adapter = new CheckedItemAdapter(customDialog, this.mContext, 201917553, 16908308, this.mItems, this.mItemsAttrs);
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
                        if (customDialog.mItemsAttrs == null || customDialog.mItemsAttrs[position] == null || (customDialog.mItemsAttrs[position].getItemEnable().booleanValue() ^ 1) == 0) {
                            dialog.mDialogInterface.dismiss();
                        }
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
        private Context context;
        private ColorAlertControllerCustom dialog;
        private ListItemAttr[] mItemsAttrs;
        private boolean textBold;
        private int textColor;
        private int textId;

        public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public CheckedItemAdapter(ColorAlertControllerCustom dialog, Context context, int resource, int textViewResourceId, CharSequence[] objects, ListItemAttr[] itemsAttrs) {
            super(context, resource, textViewResourceId, objects);
            this.mItemsAttrs = itemsAttrs;
            this.textId = textViewResourceId;
            this.context = context;
            this.dialog = dialog;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View item = super.getView(position, convertView, parent);
            if (this.mItemsAttrs == null) {
                this.mItemsAttrs = this.dialog.mItemsAttrs;
            }
            if (this.mItemsAttrs != null && this.dialog.mIsDynaChange) {
                this.mItemsAttrs = this.dialog.mItemsAttrs;
            }
            if (item != null) {
                TextView textView = (TextView) item.findViewById(this.textId);
                if (this.mItemsAttrs == null || this.mItemsAttrs[position] == null) {
                    textView.setTextColor(this.context.getResources().getColorStateList(201719832));
                    textView.getPaint().setFakeBoldText(false);
                    item.setEnabled(true);
                } else {
                    if (this.mItemsAttrs[position].getItemColor() != null) {
                        textView.setTextColor(this.mItemsAttrs[position].getItemColor().intValue());
                    }
                    if (this.mItemsAttrs[position].getItemBold() != null) {
                        textView.getPaint().setFakeBoldText(this.mItemsAttrs[position].getItemBold().booleanValue());
                    }
                    if (this.mItemsAttrs[position].getItemEnable() != null) {
                        item.setEnabled(this.mItemsAttrs[position].getItemEnable().booleanValue());
                    }
                    if (!this.mItemsAttrs[position].getItemEnable().booleanValue() && this.mItemsAttrs[position].getItemColor() == null) {
                        textView.setTextColor(this.context.getResources().getColorStateList(201720883));
                    } else if (this.mItemsAttrs[position].getItemEnable().booleanValue() && this.mItemsAttrs[position].getItemColor() == null) {
                        textView.setTextColor(this.context.getResources().getColorStateList(201719832));
                    }
                }
            }
            int count = getCount();
            if (count > 1) {
                if (position == count - 1) {
                    item.setBackgroundResource(201852219);
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

    public ColorAlertControllerCustom(Context context, DialogInterface di, Window window) {
        super(context, di, window);
    }

    public int selectContentView() {
        this.mAlertDialogLayout = 201917560;
        return this.mAlertDialogLayout;
    }

    protected void setupContent(ViewGroup contentPanel) {
        this.mScrollView = (ScrollView) this.mWindow.findViewById(201458941);
        this.mScrollView.setFocusable(false);
        ImageView divider = (ImageView) this.mWindow.findViewById(201458951);
        LinearLayout linearyLayout = (LinearLayout) this.mWindow.findViewById(201458952);
        this.mMessageView = (TextView) this.mWindow.findViewById(201458841);
        if (this.mMessageView != null) {
            if (this.mMessage != null) {
                this.mMessageView.setText(this.mMessage);
            } else {
                this.mMessageView.setVisibility(8);
                this.mScrollView.removeView(this.mMessageView);
            }
            if (this.mListView != null) {
                divider.setVisibility(0);
                if (this.mMessage == null) {
                    ViewGroup scrollParent = (ViewGroup) this.mScrollView.getParent();
                    scrollParent.removeViewAt(scrollParent.indexOfChild(this.mScrollView));
                    scrollParent.removeViewAt(scrollParent.indexOfChild(linearyLayout));
                    scrollParent.addView(this.mListView, new LayoutParams(-1, -1));
                } else if (this.mMessage != null) {
                    linearyLayout.addView(this.mListView, new LayoutParams(-1, -1));
                }
            }
            if (this.mMessage == null && this.mListView == null) {
                contentPanel.setVisibility(8);
            }
        }
    }

    public void setListItemState(ListItemAttr[] itemsAttrs, boolean isDynaChange) {
        this.mItemsAttrs = itemsAttrs;
        this.mIsDynaChange = isDynaChange;
    }

    public void setButtonIsBold(int positive, int negative, int neutral) {
        if (positive == -1) {
            this.mButtonPositive.setTextAppearance(this.mContext, 201524241);
        }
        if (negative == -2) {
            this.mButtonNegative.getPaint().setFakeBoldText(true);
        }
        if (neutral == -3) {
            this.mButtonNeutral.getPaint().setFakeBoldText(true);
        }
    }
}
