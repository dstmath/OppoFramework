package com.android.internal.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.android.internal.R;
import com.color.util.ColorChangeTextUtil;
import com.color.util.ColorContextUtil;
import com.oppo.util.OppoDialogUtil;
import java.util.Locale;

class ColorInjector {

    static class AlertController {
        private static final int BIT_BUTTON_NEGATIVE = 2;
        private static final int BIT_BUTTON_NEUTRAL = 4;
        private static final int BIT_BUTTON_POSITIVE = 1;
        private static final float TEXT_SIZE_SCALE = 0.88f;

        static class CheckedTextItemAdapter extends BaseAdapter {
            private int ListItemWidth;
            private CharSequence[] items = null;
            private Context mContext;
            private boolean mHasMessage;
            private boolean mIsOppoStyle = false;
            private boolean mIsTitle;
            private int[] mListColor;
            private int mOption;
            private int mResource;
            private int[] mTextColor;
            private int mTextId1;
            private int mTextId2;
            private int mWarningColor;
            private CharSequence[] summaryItems = null;

            public boolean hasStableIds() {
                return true;
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public Object getItem(int position) {
                return null;
            }

            public int getCount() {
                if (this.items != null) {
                    return this.items.length;
                }
                return 0;
            }

            public CheckedTextItemAdapter(Context context, int resource, int textViewResourceId1, int textViewResourceId2, CharSequence[] objects, CharSequence[] summaryObjects, boolean isTitle, int option, int[] textColor, int[] listColor, boolean hasMessage) {
                this.mIsOppoStyle = ColorContextUtil.isOppoStyle(context);
                this.mContext = context;
                this.mIsTitle = isTitle;
                this.mOption = option;
                this.mTextId1 = textViewResourceId1;
                this.mTextId2 = textViewResourceId2;
                this.mTextColor = textColor;
                this.mListColor = listColor;
                this.items = objects;
                this.summaryItems = summaryObjects;
                this.mResource = resource;
                this.mWarningColor = ColorContextUtil.getAttrColor(context, 201392720);
                this.mHasMessage = hasMessage;
                this.ListItemWidth = ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getWidth() - (context.getResources().getDimensionPixelSize(201655559) * 2);
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = LayoutInflater.from(this.mContext).inflate(this.mResource, null);
                setCheckedItemView(this, convertView, this.mContext, position, this.mIsOppoStyle, this.mOption, this.mTextId1, this.mTextId2, this.mTextColor, this.mListColor, this.mIsTitle, this.items, this.summaryItems);
                return convertView;
            }

            private void setCheckedItemView(BaseAdapter adapter, View convertView, Context context, int position, boolean isOppoStyle, int option, int textId1, int textId2, int[] textColor, int[] listColor, boolean isTitle, CharSequence[] items, CharSequence[] summaryItems) {
                if (!isOppoStyle) {
                    return;
                }
                if (option == 2 || option == 3) {
                    TextView textView1 = null;
                    TextView textView = null;
                    if (convertView != null) {
                        textView1 = (TextView) convertView.findViewById(textId1);
                        textView = (TextView) convertView.findViewById(textId2);
                    }
                    if (!(items == null || textView1 == null || position >= items.length)) {
                        textView1.setText(items[position]);
                    }
                    if (summaryItems != null && textView != null && position < summaryItems.length) {
                        CharSequence text2 = summaryItems[position];
                        if (TextUtils.isEmpty(text2)) {
                            textView.setVisibility(8);
                        } else {
                            textView.setVisibility(0);
                            textView.setText(text2);
                        }
                    } else if (summaryItems != null && textView != null && position >= summaryItems.length) {
                        textView.setVisibility(8);
                    } else if (textView != null) {
                        textView.setVisibility(8);
                    }
                    if (textColor != null && position >= 0 && position < textColor.length && listColor != null) {
                        if (textColor[position] == listColor[0]) {
                            textView1.setTextColor(context.getColorStateList(201720844));
                        }
                        if (textColor[position] == listColor[1]) {
                            textView1.setTextColor(this.mWarningColor);
                        }
                    }
                    if (getCount() > 1) {
                        return;
                    }
                    if (isTitle || (this.mHasMessage ^ 1) == 0 || position != 0) {
                        AlertController.setBackgoundButKeepPadding(convertView, 201852167);
                    } else {
                        AlertController.setBackgoundButKeepPadding(convertView, 201852160);
                    }
                }
            }

            private void changeTextViewTextSize(TextView textView, int maxWidth) {
                int textSize = (int) textView.getTextSize();
                int shrinkText1 = (int) (((float) textSize) * AlertController.TEXT_SIZE_SCALE);
                int shrinkText2 = (int) ((((float) textSize) * AlertController.TEXT_SIZE_SCALE) * AlertController.TEXT_SIZE_SCALE);
                if (((int) textView.getPaint().measureText(textView.getText().toString())) > maxWidth) {
                    textView.setTextSize(0, (float) shrinkText1);
                    if (((int) textView.getPaint().measureText(textView.getText().toString())) > maxWidth) {
                        textView.setTextSize(0, (float) shrinkText2);
                    }
                }
            }
        }

        AlertController() {
        }

        static void setDialogListener(AlertController dialog, Context context, Window window, int option) {
            if (ColorContextUtil.isOppoStyle(context)) {
                new OppoDialogUtil(context).setDialogDrag(dialog, window, option);
            }
        }

        static void updateWindow(AlertController dialog, Context context, Window window) {
            ColorInjector.updateWindowInternal(isCustomGravity(dialog, context), context, window, 81);
        }

        static boolean isCustomButtons(boolean isOppoStyle) {
            return isOppoStyle;
        }

        static ColorStateList setupButtons(Context context, boolean shouldCenterSingleButton, int deleteDialogOption, int whichButtons, Button buttonPositive, Button buttonNegative, Button buttonNeutral) {
            ColorStateList textFousedColor = context.getColorStateList(201720844);
            if (!shouldCenterSingleButton) {
                if (whichButtons == 1) {
                    buttonPositive.setTextColor(textFousedColor);
                    buttonPositive.setTypeface(null, 0);
                } else if (whichButtons == 2) {
                    buttonNegative.setTextColor(textFousedColor);
                } else if (whichButtons == 4) {
                    buttonNeutral.setTextColor(textFousedColor);
                }
            }
            return textFousedColor;
        }

        static void updateButtonsBackground(boolean isOppoStyle, ColorStateList textFousedColor, int deleteDialogOption, int whichButtons, Button buttonPositive, Button buttonNegative, Button buttonNeutral) {
            if (isOppoStyle) {
                int drawableEnd = 201852175;
                int drawableStart = 201852176;
                if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
                    drawableStart = 201852175;
                    drawableEnd = 201852176;
                }
                if (whichButtons == 3) {
                    buttonPositive.setTextColor(textFousedColor);
                    if (deleteDialogOption == 0) {
                        buttonPositive.setBackgroundResource(drawableEnd);
                        buttonNegative.setBackgroundResource(drawableStart);
                    }
                } else if (whichButtons == 5) {
                    buttonPositive.setTextColor(textFousedColor);
                    if (deleteDialogOption == 0) {
                        buttonPositive.setBackgroundResource(drawableEnd);
                        buttonNeutral.setBackgroundResource(drawableStart);
                    }
                } else if (whichButtons == 6) {
                    if (deleteDialogOption == 0) {
                        buttonNegative.setBackgroundResource(drawableStart);
                        buttonNeutral.setBackgroundResource(drawableEnd);
                    }
                } else if (whichButtons == 7) {
                    buttonPositive.setTextColor(textFousedColor);
                    if (deleteDialogOption == 0) {
                        buttonNegative.setBackgroundResource(drawableStart);
                        buttonNeutral.setBackgroundResource(201851127);
                        buttonPositive.setBackgroundResource(drawableEnd);
                    }
                }
            }
        }

        static int updateBottomMediumDrawable(int deleteDialogOption, int defaultDrawable) {
            if (deleteDialogOption != 0) {
                return 201850909;
            }
            return defaultDrawable;
        }

        static void updatePanelsBackground(int deleteDialogOption, View buttonPanel, View topPanel, View contentPanel, View customPanel, View buttonPositive, View buttonNeutral) {
            if (deleteDialogOption == 2 || deleteDialogOption == 3) {
                View view;
                if (buttonPanel != null) {
                    buttonPanel.setBackground(null);
                }
                boolean hasTop = topPanel != null && topPanel.getVisibility() == 0;
                if (contentPanel == null || contentPanel.getVisibility() != 0) {
                }
                if (customPanel == null || customPanel.getVisibility() != 0) {
                }
                if (buttonPositive == null || buttonPositive.getVisibility() != 0) {
                }
                if (buttonNeutral == null || buttonNeutral.getVisibility() != 0) {
                }
                View[] allViews = new View[]{topPanel, contentPanel, customPanel, buttonPositive, buttonNeutral};
                int bgTop = hasTop ? 201852168 : 201852161;
                int positionTop = -1;
                int positionBottom = -1;
                for (int i = 0; i < allViews.length; i++) {
                    view = allViews[i];
                    if (view != null && view.getVisibility() == 0) {
                        positionTop = i;
                        break;
                    }
                }
                for (int j = allViews.length - 1; j >= 0; j--) {
                    view = allViews[j];
                    if (view != null && view.getVisibility() == 0) {
                        positionBottom = j;
                        break;
                    }
                }
                if (positionTop == positionBottom && positionTop != -1) {
                    setBackgoundButKeepPadding(allViews[positionTop], bgTop);
                } else if (!(positionTop == positionBottom && positionTop == -1)) {
                    setBackgoundButKeepPadding(allViews[positionTop], bgTop);
                    setBackgoundButKeepPadding(allViews[positionBottom], 201852163);
                    for (int k = positionTop + 1; k < positionBottom; k++) {
                        setBackgoundButKeepPadding(allViews[k], 201852162);
                    }
                }
                if (contentPanel != null && deleteDialogOption == 3) {
                    allViews[positionBottom].setPadding(0, 0, 0, 0);
                }
            }
        }

        static int initListLayout(AlertController dialog, boolean isOppoStyle, Context context, int defaultLayout) {
            int layout = defaultLayout;
            if (!isOppoStyle) {
                return layout;
            }
            switch (dialog.getDeleteDialogOption()) {
                case 2:
                    return 201917547;
                case 3:
                    return 201917545;
                default:
                    return layout;
            }
        }

        static boolean initRecycleListView(AlertController dialog, boolean isOppoStyle, ListView listView, CharSequence title, boolean isScroll, boolean isMessageScroll) {
            if (!isOppoStyle || (dialog.getDeleteDialogOption() != 2 && dialog.getDeleteDialogOption() != 3)) {
                return false;
            }
            AlertController.mScroll = isScroll;
            dialog.mMessageScroll = isMessageScroll;
            if (isScroll) {
                listView.setOverScrollMode(0);
            } else {
                listView.setOverScrollMode(2);
            }
            return TextUtils.isEmpty(title) ^ 1;
        }

        static boolean isCustomAdapter(boolean isOppoStyle) {
            return isOppoStyle;
        }

        static boolean isDialogThree(AlertController dialog) {
            return dialog.getDeleteDialogOption() == 3;
        }

        static ListAdapter initCheckedItemAdapter(AlertController dialog, String labelColumn, Cursor cursor, Context context, CharSequence[] items, CharSequence[] summaryItems, boolean isTitle, int[] textColor, int defaultLayout, ListAdapter defaultAdapter, boolean hasMessage) {
            int layout = defaultLayout;
            if (dialog.getDeleteDialogOption() == 3) {
                layout = 201917546;
            }
            ListAdapter adapter = defaultAdapter;
            if (cursor != null) {
                return new SimpleCursorAdapter(context, layout, cursor, new String[]{labelColumn}, new int[]{R.id.text1, 201458935});
            } else if (defaultAdapter != null) {
                return defaultAdapter;
            } else {
                return new CheckedTextItemAdapter(context, layout, R.id.text1, 201458935, items, summaryItems, isTitle, dialog.getDeleteDialogOption(), textColor, AlertDialog.LIST_TEXT_COLOR, hasMessage);
            }
        }

        static int initLayout(int deleteDialogOption, int defaultLayout) {
            switch (deleteDialogOption) {
                case 1:
                    return 201917542;
                case 2:
                    return 201917543;
                case 3:
                    return 201917544;
                default:
                    return defaultLayout;
            }
        }

        static void addListView(ListView listView, CharSequence message, TextView messageView, ScrollView scrollView, ImageView divider, LinearLayout linearyLayout, ViewGroup contentPanel, boolean isScroll, boolean isMessageScroll) {
            if (isScroll) {
                LayoutParams lp;
                if (isMessageScroll) {
                    if (scrollView != null) {
                        ((LayoutParams) scrollView.getLayoutParams()).weight = 0.0f;
                    }
                    if (linearyLayout != null) {
                        lp = (LayoutParams) linearyLayout.getLayoutParams();
                        lp.height = 0;
                        lp.weight = 1.0f;
                    }
                } else {
                    if (scrollView != null) {
                        ((LayoutParams) scrollView.getLayoutParams()).height = 0;
                    }
                    if (linearyLayout != null) {
                        lp = (LayoutParams) linearyLayout.getLayoutParams();
                        lp.height = 0;
                        lp.weight = 1.0f;
                    }
                }
            }
            if (listView != null) {
                if (message == null) {
                    messageView.setVisibility(8);
                    scrollView.removeView(messageView);
                    ViewGroup scrollParent = (ViewGroup) scrollView.getParent();
                    scrollParent.removeViewAt(scrollParent.indexOfChild(scrollView));
                    divider.setVisibility(8);
                    scrollParent.removeViewAt(scrollParent.indexOfChild(divider));
                    scrollParent.removeViewAt(scrollParent.indexOfChild(linearyLayout));
                    scrollParent.addView((View) listView, new ViewGroup.LayoutParams(-1, -1));
                } else if (message != null) {
                    messageView.setText(message);
                    linearyLayout.addView((View) listView, new ViewGroup.LayoutParams(-1, -1));
                }
            }
            if (message == null && listView == null) {
                contentPanel.setVisibility(8);
            }
        }

        static void setButtonBold(int positive, int negative, int neutral, Button buttonPositive, Button buttonNegative, Button buttonNeutral, Context context) {
            if (positive == -1) {
                buttonPositive.setTextAppearance(context, 201524241);
            }
            if (negative == -2) {
                buttonNegative.getPaint().setFakeBoldText(true);
            }
            if (neutral == -3) {
                buttonNeutral.getPaint().setFakeBoldText(true);
            }
        }

        private static void setBackgoundButKeepPadding(View view, int bg) {
            if (view != null) {
                view.setBackgroundResource(bg);
            }
        }

        private static boolean isCustomGravity(AlertController dialog, Context context) {
            if (dialog.getDeleteDialogOption() != 0 || context == null || context.getThemeResId() == 201524238) {
                return false;
            }
            return true;
        }

        static void changeButtonArrangeStyles(Context context, ViewGroup buttonPanel, int deleteDialogOption, int whichButtons, Button buttonPositive, Button buttonNegative, Button buttonNeutral, CharSequence buttonPositiveText, CharSequence buttonNegativeText, CharSequence buttonNeutralText) {
            int wButton = whichButtons;
            final Context context2 = context;
            final Button button = buttonPositive;
            final Button button2 = buttonNegative;
            final Button button3 = buttonNeutral;
            final CharSequence charSequence = buttonPositiveText;
            final CharSequence charSequence2 = buttonNegativeText;
            final CharSequence charSequence3 = buttonNeutralText;
            final int i = deleteDialogOption;
            final int i2 = whichButtons;
            final ViewGroup viewGroup = buttonPanel;
            buttonPositive.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    int buttonNegativeWidth = 0;
                    int buttonPositiveWidth = 0;
                    int buttonNeutralWidth = 0;
                    int positiveTextWidth = 0;
                    int negativeTextWidth = 0;
                    int neutralTextWidth = 0;
                    AlertController.changeButtonTextSize(context2, button, button2, button3);
                    if (button.getVisibility() == 0) {
                        buttonPositiveWidth = button.getWidth() - (button.getPaddingLeft() + button.getPaddingRight());
                        positiveTextWidth = (int) button.getPaint().measureText(charSequence.toString());
                    }
                    if (button2.getVisibility() == 0) {
                        buttonNegativeWidth = button2.getWidth() - (button2.getPaddingLeft() + button2.getPaddingRight());
                        negativeTextWidth = (int) button2.getPaint().measureText(charSequence2.toString());
                    }
                    if (button3.getVisibility() == 0) {
                        buttonNeutralWidth = button3.getWidth() - (button3.getPaddingLeft() + button3.getPaddingRight());
                        neutralTextWidth = (int) button3.getPaint().measureText(charSequence3.toString());
                    }
                    button.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    if (i != 0 || !AlertController.isSetButtonPanelFromHtV(i2)) {
                        return;
                    }
                    if (negativeTextWidth > buttonNegativeWidth || positiveTextWidth > buttonPositiveWidth || neutralTextWidth > buttonNeutralWidth) {
                        AlertController.setButtonPanelFromHtV(context2, viewGroup, i2, button, button2, button3);
                    }
                }
            });
        }

        static boolean isSetButtonPanelFromHtV(int whichButtons) {
            if (whichButtons == 3 || whichButtons == 5 || whichButtons == 6 || whichButtons == 7) {
                return true;
            }
            return false;
        }

        public static void setButtonStyle(Button button, Context context) {
            LayoutParams params = (LayoutParams) button.getLayoutParams();
            params.width = -1;
            params.height = -2;
            button.-wrap18(params);
        }

        static void setVerticalButtonsStyle(Context context, LinearLayout container, int whichButtons, Button buttonPositive, Button buttonNegative, Button buttonNeutral) {
            if (whichButtons == 3) {
                container.removeView(buttonNegative);
                container.addView(buttonNegative);
                setButtonStyle(buttonPositive, context);
                setButtonStyle(buttonNegative, context);
            } else if (whichButtons == 5) {
                container.removeView(buttonNeutral);
                container.addView(buttonNeutral);
                setButtonStyle(buttonPositive, context);
                setButtonStyle(buttonNeutral, context);
            } else if (whichButtons == 6) {
                container.removeView(buttonNegative);
                container.addView(buttonNegative);
                setButtonStyle(buttonNegative, context);
                setButtonStyle(buttonNeutral, context);
            } else if (whichButtons == 7) {
                container.removeView(buttonNegative);
                container.removeView(buttonNeutral);
                container.addView(buttonNeutral);
                container.addView(buttonNegative);
                setButtonStyle(buttonNegative, context);
                setButtonStyle(buttonNeutral, context);
                setButtonStyle(buttonPositive, context);
            }
        }

        static void setVerticalButtonsBackground(int whichButtons, Button buttonPositive, Button buttonNegative, Button buttonNeutral) {
            if (whichButtons == 3) {
                buttonPositive.setBackgroundResource(201852204);
                buttonNegative.setBackgroundResource(201852205);
            } else if (whichButtons == 5) {
                buttonPositive.setBackgroundResource(201852204);
                buttonNeutral.setBackgroundResource(201852205);
            } else if (whichButtons == 6) {
                buttonNegative.setBackgroundResource(201852205);
                buttonNeutral.setBackgroundResource(201852204);
            } else if (whichButtons == 7) {
                buttonPositive.setBackgroundResource(201852204);
                buttonNeutral.setBackgroundResource(201852204);
                buttonNegative.setBackgroundResource(201852205);
            }
        }

        public static void setButtonPanelFromHtV(Context context, ViewGroup buttonPanel, int whichButtons, Button buttonPositive, Button buttonNegative, Button buttonNeutral) {
            LayoutParams lp = (LayoutParams) buttonPanel.getLayoutParams();
            lp.width = -1;
            lp.height = -2;
            buttonPanel.-wrap18(lp);
            LinearLayout buttonPanelContainer = (LinearLayout) buttonPositive.getParent();
            buttonPanelContainer.setOrientation(1);
            buttonPanelContainer.-wrap18(new LayoutParams(-1, -2));
            setVerticalButtonsStyle(context, buttonPanelContainer, whichButtons, buttonPositive, buttonNegative, buttonNeutral);
            setVerticalButtonsBackground(whichButtons, buttonPositive, buttonNegative, buttonNeutral);
        }

        private static void changeButtonTextSize(Button btn, int maxWidth, CharSequence text) {
        }

        private static void changeButtonTextSize(Context context, Button buttonPositive, Button buttonNegative, Button buttonNeutral) {
            float textSize = ColorChangeTextUtil.getSuitableFontSize((float) context.getResources().getDimensionPixelSize(201654415), context.getResources().getConfiguration().fontScale, 5);
            if (buttonPositive.getVisibility() == 0) {
                buttonPositive.setTextSize(0, (float) ((int) textSize));
            }
            if (buttonNegative.getVisibility() == 0) {
                buttonNegative.setTextSize(0, (float) ((int) textSize));
            }
            if (buttonNeutral.getVisibility() == 0) {
                buttonNeutral.setTextSize(0, (float) ((int) textSize));
            }
        }
    }

    static class ColorAlertControllerUpdate {
        ColorAlertControllerUpdate() {
        }

        static void updateWindow(ColorAlertControllerUpdate dialog, Context context, Window window) {
            boolean z;
            if (context != null) {
                z = true;
            } else {
                z = false;
            }
            ColorInjector.updateWindowInternal(z, context, window, 0);
        }
    }

    static class ResolverActivity {
        ResolverActivity() {
        }

        static void setTheme(Context context) {
            context.setTheme(201524238);
        }
    }

    ColorInjector() {
    }

    private static void updateWindowInternal(boolean isCustomGravity, Context context, Window window, int defaultGravity) {
        if (ColorContextUtil.isOppoStyle(context) && window.getAttributes().type != WindowManager.LayoutParams.TYPE_BOOT_PROGRESS) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            WindowManager.LayoutParams params = window.getAttributes();
            TypedArray a = context.obtainStyledAttributes(null, oppo.R.styleable.ColorAlertDialog, R.attr.alertDialogStyle, 0);
            if (isCustomGravity) {
                params.windowAnimations = 201524237;
                window.setAttributes(params);
                window.setGravity(a.getInt(1, 17));
            } else if (defaultGravity != 0) {
                window.setGravity(defaultGravity);
            }
            a.recycle();
        }
    }
}
