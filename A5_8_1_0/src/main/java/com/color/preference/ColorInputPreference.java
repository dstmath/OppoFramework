package com.color.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.Preference;
import android.preference.Preference.BaseSavedState;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import com.oppo.widget.OppoEditText;
import oppo.R;

public class ColorInputPreference extends Preference {
    private static final String ELLIPSIS = "...";
    private static final int TIME_DELAY = 100;
    private CharSequence mContent;
    private Context mContext;
    private ImageView mDeleteButton;
    private OppoEditText mEditText;
    private boolean mEllipsisMode;
    private boolean mHasFocus;
    private CharSequence mHint;
    private boolean mNoDivider;
    private boolean mPasswordMode;
    private View mPreferenceView;
    private boolean mRTL;
    private ContentWatcher mWatcher;

    private class ContentWatcher implements TextWatcher {
        /* synthetic */ ContentWatcher(ColorInputPreference this$0, ContentWatcher -this1) {
            this();
        }

        private ContentWatcher() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (!s.toString().contains(ColorInputPreference.ELLIPSIS)) {
                ColorInputPreference.this.mContent = s.toString();
            }
            ColorInputPreference.this.updateDeleteButton(true, TextUtils.isEmpty(ColorInputPreference.this.mContent));
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String text;

        public SavedState(Parcel source) {
            super(source);
            this.text = source.readString();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public ColorInputPreference(Context context) {
        this(context, null);
    }

    public ColorInputPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 201393333);
    }

    public ColorInputPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorInputPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        this.mEditText = new OppoEditText(context, attrs);
        this.mEditText.setId(16908297);
        this.mEditText.setBackground(null);
        this.mEditText.forceFinishDetach();
        this.mEditText.setPadding(0, 0, context.getResources().getDimensionPixelSize(201654311), 0);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorInputPreference, defStyleAttr, 0);
        this.mContent = a.getText(0);
        this.mHint = a.getText(1);
        this.mNoDivider = a.getBoolean(2, false);
        this.mEditText.setGravity(8388627);
        this.mEditText.setTextAlignment(5);
        a.recycle();
        TypedArray b = context.obtainStyledAttributes(attrs, android.R.styleable.TextView, defStyleAttr, 0);
        if (a.getBoolean(33, false)) {
            this.mEditText.postDelayed(new Runnable() {
                public void run() {
                    ColorInputPreference.this.mEditText.selectAll();
                }
            }, 100);
        }
        b.recycle();
    }

    public CharSequence getContent() {
        return this.mContent;
    }

    public void setContent(CharSequence content) {
        if (TextUtils.equals(this.mContent, content) ^ 1) {
            notifyChanged();
        }
        boolean wasBlocking = shouldDisableDependents();
        this.mContent = content;
        if (content != null) {
            persistString(content.toString());
        }
        boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    public CharSequence getHint() {
        return this.mHint;
    }

    public void setHint(CharSequence hint) {
        if ((hint == null && this.mHint != null) || (hint != null && (hint.equals(this.mHint) ^ 1) != 0)) {
            this.mHint = hint;
            notifyChanged();
        }
    }

    public View getPreferenceView() {
        return this.mPreferenceView;
    }

    public OppoEditText getEditText() {
        return this.mEditText;
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (!TextUtils.isEmpty(this.mContent)) {
            String value;
            if (restoreValue) {
                value = getPersistedString(this.mContent.toString());
            } else {
                value = (String) defaultValue;
            }
            setContent(value);
        }
    }

    public boolean shouldDisableDependents() {
        return !TextUtils.isEmpty(this.mContent) ? super.shouldDisableDependents() : true;
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.mPreferenceView = view;
        OppoEditText editText = this.mEditText;
        View oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            ViewGroup container = (ViewGroup) view.findViewById(16908842);
            if (container != null) {
                container.addView(editText, -1, -2);
            }
        }
        ImageView divider = (ImageView) view.findViewById(16908288);
        if (divider != null && this.mNoDivider) {
            divider.setVisibility(8);
        }
        this.mDeleteButton = (ImageView) view.findViewById(16908313);
        if (this.mEditText != null && this.mDeleteButton != null) {
            this.mDeleteButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ColorInputPreference.this.mEditText.setText("");
                    ColorInputPreference.this.mEditText.requestFocus();
                    ColorInputPreference.this.mDeleteButton.setVisibility(4);
                }
            });
            this.mDeleteButton.setVisibility(4);
            this.mPasswordMode = this.mEditText.getTransformationMethod() instanceof PasswordTransformationMethod;
            final CharSequence content = getContent();
            if (!TextUtils.isEmpty(content)) {
                this.mEditText.post(new Runnable() {
                    public void run() {
                        int textWidth = (ColorInputPreference.this.mEditText.getWidth() - ColorInputPreference.this.mEditText.getCompoundPaddingLeft()) - ColorInputPreference.this.mEditText.getCompoundPaddingRight();
                        String text = content.toString();
                        int breakIndex = ColorInputPreference.this.mEditText.getPaint().breakText(text, true, (float) textWidth, null);
                        if (breakIndex == text.length() || (ColorInputPreference.this.mPasswordMode ^ 1) == 0) {
                            ColorInputPreference.this.mEllipsisMode = false;
                        } else {
                            if (ColorInputPreference.this.isEmojiCharacter(text.charAt(breakIndex))) {
                                text = text.substring(0, breakIndex - 2) + ColorInputPreference.ELLIPSIS;
                            } else {
                                text = text.substring(0, breakIndex - 1) + ColorInputPreference.ELLIPSIS;
                            }
                            ColorInputPreference.this.mEllipsisMode = true;
                        }
                        ColorInputPreference.this.mEditText.setText(text);
                    }
                });
            }
            CharSequence hint = getHint();
            if (!TextUtils.isEmpty(hint)) {
                this.mEditText.setHint(hint);
            }
            if (this.mHasFocus) {
                this.mEditText.requestFocus();
            } else {
                this.mEditText.clearFocus();
            }
            this.mEditText.setEnabled(isEnabled());
            this.mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText edit = (EditText) v;
                    ColorInputPreference.this.mHasFocus = hasFocus;
                    if (ColorInputPreference.this.mWatcher == null) {
                        ColorInputPreference.this.mWatcher = new ContentWatcher(ColorInputPreference.this, null);
                    }
                    CharSequence mText = ColorInputPreference.this.mContent;
                    if (hasFocus) {
                        if (ColorInputPreference.this.mEllipsisMode) {
                            boolean isSelectAll = false;
                            int startIndex = ColorInputPreference.this.mEditText.getSelectionStart();
                            int endIndex = ColorInputPreference.this.mEditText.getSelectionEnd();
                            if (mText != null && startIndex == 0 && endIndex == mText.length()) {
                                isSelectAll = true;
                            }
                            edit.setText(ColorInputPreference.this.mContent);
                            if (isSelectAll) {
                                edit.selectAll();
                            }
                        }
                        edit.addTextChangedListener(ColorInputPreference.this.mWatcher);
                    } else {
                        edit.removeTextChangedListener(ColorInputPreference.this.mWatcher);
                        if (ColorInputPreference.this.callChangeListener(ColorInputPreference.this.mContent)) {
                            ColorInputPreference.this.setContent(ColorInputPreference.this.mContent);
                        }
                        if (mText != null) {
                            int textWidth = (ColorInputPreference.this.mEditText.getWidth() - ColorInputPreference.this.mEditText.getCompoundPaddingLeft()) - ColorInputPreference.this.mEditText.getCompoundPaddingRight();
                            String text = mText.toString();
                            int breakIndex = edit.getPaint().breakText(text, true, (float) textWidth, null);
                            if (breakIndex == text.length() || (ColorInputPreference.this.mPasswordMode ^ 1) == 0) {
                                ColorInputPreference.this.mEllipsisMode = false;
                            } else {
                                if (ColorInputPreference.this.isEmojiCharacter(text.charAt(breakIndex))) {
                                    text = text.substring(0, breakIndex - 2) + ColorInputPreference.ELLIPSIS;
                                } else {
                                    text = text.substring(0, breakIndex - 1) + ColorInputPreference.ELLIPSIS;
                                }
                                ColorInputPreference.this.mEllipsisMode = true;
                                edit.setText(text);
                            }
                        }
                    }
                    ColorInputPreference.this.updateDeleteButton(hasFocus, TextUtils.isEmpty(ColorInputPreference.this.mContent));
                }
            });
        }
    }

    private boolean isEmojiCharacter(char codePoint) {
        if (codePoint == 0 || codePoint == 9 || codePoint == 10 || codePoint == 13 || ((codePoint >= ' ' && codePoint <= 55295) || (codePoint >= 57344 && codePoint <= 65533))) {
            return false;
        }
        return codePoint < 0 || codePoint > 65535;
    }

    private void updateDeleteButton(boolean hasFocus, boolean noText) {
        if (this.mDeleteButton != null) {
            if (this.mRTL) {
                this.mDeleteButton.setVisibility(4);
                return;
            }
            if (!hasFocus || (noText ^ 1) == 0) {
                this.mDeleteButton.setVisibility(4);
            } else {
                this.mDeleteButton.setVisibility(0);
            }
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.text = getContent().toString();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || (state.getClass().equals(SavedState.class) ^ 1) != 0) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setContent(myState.text);
    }
}
