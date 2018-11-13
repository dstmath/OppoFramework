package com.color.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.color.util.ColorChangeTextUtil;

public class ColorSearchView extends SearchView {
    private ImageButton mImageButton;
    private boolean mIsHintTextSize;
    private OnClickCloseButtonListener mOnClickCloseButtonListener;
    private OnClickSearchImageListener mOnClickSearchImageListener;
    private OnSearchViewClickListener mOnSearchViewClickListener;
    private OnSearchViewLongClickListener mOnSearchViewLongClickListener;
    private Drawable mSearchDrawable;
    private ImageView mSearchImage;
    private final OnClickListener mSearchOnClickListener;
    private final OnEditorActionListener mSearchOnEditorActionListener;
    private final OnLongClickListener mSearchOnLongClickListener;
    private LinearLayout mSearchSrc;
    private int mSearchSrcPaddingR;
    private boolean mSearchVisible;
    private Runnable mShowImeRunnable;

    public interface OnClickCloseButtonListener {
        void onClickCloseButton();
    }

    public interface OnClickSearchImageListener {
        boolean onClickSearchImage();
    }

    public interface OnSearchViewClickListener {
        void onSearchViewClick();
    }

    public interface OnSearchViewLongClickListener {
        void onSearchViewLongClick();
    }

    public ColorSearchView(Context context) {
        this(context, null);
    }

    public ColorSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSearchDrawable = null;
        this.mOnClickSearchImageListener = null;
        this.mOnClickCloseButtonListener = null;
        this.mOnSearchViewLongClickListener = null;
        this.mSearchVisible = false;
        this.mSearchImage = null;
        this.mSearchSrc = null;
        this.mImageButton = null;
        this.mSearchSrcPaddingR = 0;
        this.mSearchOnLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (v == ColorSearchView.this.mSearchSrcTextView) {
                    if (ColorSearchView.this.mOnSearchViewLongClickListener != null) {
                        ColorSearchView.this.mOnSearchViewLongClickListener.onSearchViewLongClick();
                    }
                } else if (v == ColorSearchView.this.mSearchSrc) {
                    if (ColorSearchView.this.mOnSearchViewLongClickListener != null) {
                        ColorSearchView.this.mOnSearchViewLongClickListener.onSearchViewLongClick();
                    }
                } else if (v == ColorSearchView.this.mImageButton && ColorSearchView.this.mOnSearchViewLongClickListener != null) {
                    ColorSearchView.this.mOnSearchViewLongClickListener.onSearchViewLongClick();
                }
                return false;
            }
        };
        this.mSearchOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (v == ColorSearchView.this.mSearchButton) {
                    ColorSearchView.this.onSearchClicked();
                } else if (v == ColorSearchView.this.mCloseButton) {
                    ColorSearchView.this.onCloseClicked();
                    if (ColorSearchView.this.mOnClickCloseButtonListener != null) {
                        ColorSearchView.this.mOnClickCloseButtonListener.onClickCloseButton();
                    }
                } else if (v == ColorSearchView.this.mGoButton) {
                    ColorSearchView.this.onSubmitQuery();
                } else if (v == ColorSearchView.this.mVoiceButton) {
                    ColorSearchView.this.onVoiceClicked();
                } else if (v == ColorSearchView.this.mSearchSrcTextView) {
                    ColorSearchView.this.forceSuggestionQuery();
                    if (ColorSearchView.this.mOnSearchViewClickListener != null) {
                        ColorSearchView.this.mOnSearchViewClickListener.onSearchViewClick();
                    }
                    ColorSearchView.this.setTextViewFocus(true);
                } else if (v == ColorSearchView.this.mSearchImage) {
                    if (ColorSearchView.this.mOnClickSearchImageListener != null) {
                        ColorSearchView.this.mOnClickSearchImageListener.onClickSearchImage();
                    }
                } else if (v == ColorSearchView.this.mSearchSrc) {
                    ColorSearchView.this.forceSuggestionQuery();
                    if (ColorSearchView.this.mOnSearchViewClickListener != null) {
                        ColorSearchView.this.mOnSearchViewClickListener.onSearchViewClick();
                    }
                    ColorSearchView.this.setTextViewFocus(true);
                } else if (v == ColorSearchView.this.mImageButton) {
                    ColorSearchView.this.forceSuggestionQuery();
                    if (ColorSearchView.this.mOnSearchViewClickListener != null) {
                        ColorSearchView.this.mOnSearchViewClickListener.onSearchViewClick();
                    }
                    ColorSearchView.this.setTextViewFocus(true);
                }
            }
        };
        this.mSearchOnEditorActionListener = new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                ColorSearchView.this.onSubmitQuery();
                ColorSearchView.this.setTextViewFocus(false);
                return true;
            }
        };
        this.mIsHintTextSize = true;
        this.mOnSearchViewClickListener = null;
        this.mShowImeRunnable = new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) ColorSearchView.this.getContext().getSystemService(InputMethodManager.class);
                if (imm != null) {
                    imm.showSoftInputUnchecked(0, null);
                }
            }
        };
        this.mSearchImage = (ImageView) findViewById(201458959);
        this.mSearchSrc = (LinearLayout) findViewById(201458965);
        this.mImageButton = (ImageButton) findViewById(201458964);
        this.mCloseButton.setBackgroundDrawable(null);
        this.mSearchSrcPaddingR = context.getResources().getDimensionPixelSize(201655513);
        if (this.mSearchImage != null) {
            this.mSearchImage.setOnClickListener(this.mSearchOnClickListener);
        }
        if (this.mImageButton != null) {
            this.mImageButton.setOnClickListener(this.mSearchOnClickListener);
            this.mImageButton.setImportantForAccessibility(2);
        }
        if (this.mSearchSrc != null) {
            this.mSearchSrc.setOnClickListener(this.mSearchOnClickListener);
        }
        this.mSearchButton.setOnClickListener(this.mSearchOnClickListener);
        this.mCloseButton.setOnClickListener(this.mSearchOnClickListener);
        this.mGoButton.setOnClickListener(this.mSearchOnClickListener);
        this.mVoiceButton.setOnClickListener(this.mSearchOnClickListener);
        this.mSearchSrcTextView.setOnClickListener(this.mSearchOnClickListener);
        this.mSearchSrcTextView.setOnEditorActionListener(this.mSearchOnEditorActionListener);
        this.mSearchSrcTextView.setOnLongClickListener(this.mSearchOnLongClickListener);
        this.mSearchSrcTextView.setHapticFeedbackEnabled(false);
        updateSearchImage();
    }

    public void updateCloseButton() {
        int i = 8;
        boolean hasText = TextUtils.isEmpty(this.mSearchSrcTextView.getText()) ^ 1;
        int showClose = !hasText ? this.mIconifiedByDefault ? this.mExpandedInActionView ^ 1 : 0 : 1;
        if (showClose != 0) {
            if (this.mSearchImage != null && this.mSearchImage.getVisibility() == 0) {
                this.mSearchImage.setVisibility(8);
            }
        } else if (this.mSearchImage != null && this.mSearchVisible && this.mSearchImage.getVisibility() == 8) {
            this.mSearchImage.setVisibility(0);
        }
        ImageView imageView = this.mCloseButton;
        if (showClose != 0) {
            i = 0;
        }
        imageView.setVisibility(i);
        if (this.mCloseButton.isShown()) {
            resetSearchSrcPadding(0, 0);
        }
        Drawable closeButtonImg = this.mCloseButton.getDrawable();
        if (closeButtonImg != null) {
            closeButtonImg.setState(hasText ? ENABLED_STATE_SET : EMPTY_STATE_SET);
        }
    }

    public void onCloseClicked() {
        if (!TextUtils.isEmpty(this.mSearchSrcTextView.getText())) {
            if (!(this.mSearchSrcTextView.getHint() == null || (this.mSearchSrcTextView.getHint().toString().isEmpty() ^ 1) == 0)) {
                resetSearchSrcPadding(0, this.mSearchSrcPaddingR);
            }
            this.mSearchSrcTextView.setFocusable(true);
            this.mSearchSrcTextView.setFocusableInTouchMode(true);
        }
        super.onCloseClicked();
    }

    public void onTextChanged(CharSequence newText) {
        super.onTextChanged(newText);
        changeTextSize(newText.toString());
        updateSearchImage();
    }

    public void updateFocusedState() {
        boolean focused = this.mSearchSrcTextView.hasFocus();
        int[] stateSet = this.mSearchSrcTextView.isEnabled() ? focused ? FOCUSED_STATE_SET : ENABLED_FOCUSED_STATE_SET : focused ? FOCUSED_STATE_SET : EMPTY_STATE_SET;
        Drawable searchPlateBg = this.mSearchPlate.getBackground();
        if (searchPlateBg != null) {
            searchPlateBg.setState(stateSet);
        }
        Drawable submitAreaBg = this.mSubmitArea.getBackground();
        if (submitAreaBg != null) {
            submitAreaBg.setState(stateSet);
        }
        invalidate();
    }

    public CharSequence getDecoratedHint(CharSequence hintText) {
        return hintText;
    }

    private void changeTextSize(String newText) {
        if (newText.isEmpty()) {
            this.mSearchSrcTextView.setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) getResources().getDimensionPixelSize(201655354), getResources().getConfiguration().fontScale, 2)));
            this.mIsHintTextSize = true;
        } else if (this.mIsHintTextSize) {
            this.mSearchSrcTextView.setTextSize(0, (float) getResources().getDimensionPixelSize(((double) getResources().getConfiguration().fontScale) <= 1.0d ? 201654414 : 201654418));
            this.mIsHintTextSize = false;
        }
        this.mSearchSrcTextView.getPaint().setFakeBoldText(newText.isEmpty());
    }

    public void setOnSearchViewClickListener(OnSearchViewClickListener listener) {
        this.mOnSearchViewClickListener = listener;
    }

    public AutoCompleteTextView getSearchAutoComplete() {
        return this.mSearchSrcTextView;
    }

    public void setSearchViewBackground() {
        int[] stateSet = this.mSearchSrcTextView.isEnabled() ? ENABLED_FOCUSED_STATE_SET : EMPTY_STATE_SET;
        Drawable searchPlateBg = this.mSearchPlate.getBackground();
        if (searchPlateBg != null) {
            searchPlateBg.setState(stateSet);
        }
        Drawable submitAreaBg = this.mSubmitArea.getBackground();
        if (submitAreaBg != null) {
            submitAreaBg.setState(stateSet);
        }
        Drawable imagebuttonBg = this.mImageButton.getBackground();
        if (imagebuttonBg != null) {
            imagebuttonBg.setState(stateSet);
        }
        invalidate();
    }

    public void setSearchSrcTextViewLength(int length) {
        if (length >= 0 && this.mSearchSrcTextView != null) {
            this.mSearchSrcTextView.setFilters(new InputFilter[]{new LengthFilter(length)});
        }
    }

    private void setTextViewFocus(boolean focusable) {
        boolean hasText = TextUtils.isEmpty(this.mSearchSrcTextView.getText());
        if (!focusable && (hasText ^ 1) != 0) {
            this.mSearchSrcTextView.setFocusable(false);
        } else if (!hasText) {
            this.mSearchSrcTextView.setFocusable(true);
            this.mSearchSrcTextView.setFocusableInTouchMode(true);
            this.mSearchSrcTextView.requestFocus();
            setImeVisibility(true);
        }
    }

    public void setImeVisibility(boolean visible) {
        if (visible) {
            post(this.mShowImeRunnable);
            return;
        }
        removeCallbacks(this.mShowImeRunnable);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    private void resetSearchSrcPadding(int paddingLeft, int paddingRight) {
        if (this.mSearchSrc != null) {
            this.mSearchSrc.setPadding(paddingLeft, 0, paddingRight, 0);
        }
    }

    public void setSearchImage(int resId) {
        setSearchImage(getResources().getDrawable(resId));
    }

    public void setSearchImage(Drawable d) {
        this.mSearchDrawable = d;
    }

    public void searchImageIsVisible(boolean isVisible) {
        this.mSearchVisible = isVisible;
        if (isVisible) {
            if (this.mSearchImage != null && this.mSearchDrawable != null) {
                this.mSearchImage.setVisibility(0);
                int pddingLeft = this.mSearchImage.getPaddingLeft();
                int paddingTop = this.mSearchImage.getPaddingTop();
                int paddingRight = this.mSearchImage.getPaddingRight();
                int paddingBottom = this.mSearchImage.getPaddingBottom();
                this.mSearchImage.setBackground(this.mSearchDrawable);
                this.mSearchImage.setPadding(pddingLeft, paddingTop, paddingRight, paddingBottom);
            }
        } else if (this.mSearchImage != null) {
            this.mSearchImage.setVisibility(8);
        }
    }

    public void setOnClickSearchImageListener(OnClickSearchImageListener listener) {
        this.mOnClickSearchImageListener = listener;
    }

    public void setOnClickCloseButtonListener(OnClickCloseButtonListener listener) {
        this.mOnClickCloseButtonListener = listener;
    }

    private void updateSearchImage() {
        int i = 0;
        boolean isvisible = false;
        if (this.mCloseButton != null && this.mCloseButton.getVisibility() == 0) {
            isvisible = false;
        } else if (this.mSearchVisible) {
            isvisible = true;
        }
        int showClose = !(TextUtils.isEmpty(this.mSearchSrcTextView.getText()) ^ 1) ? this.mIconifiedByDefault ? this.mExpandedInActionView ^ 1 : 0 : 1;
        ImageView imageView = this.mCloseButton;
        if (showClose == 0) {
            i = 8;
        }
        imageView.setVisibility(i);
        Drawable searchImageImg = this.mSearchImage.getDrawable();
        if (searchImageImg != null) {
            searchImageImg.setState(isvisible ? ENABLED_STATE_SET : EMPTY_STATE_SET);
        }
    }

    public void setOnSearchViewLongClickListener(OnSearchViewLongClickListener listener) {
        this.mOnSearchViewLongClickListener = listener;
    }
}
