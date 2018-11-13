package com.oppo.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.QwertyKeyListener;
import android.util.AttributeSet;
import android.widget.Filter;
import android.widget.MultiAutoCompleteTextView.Tokenizer;
import com.oppo.widget.OppoAutoCompleteTextView.Validator;

public class OppoMultiAutoCompleteTextView extends OppoAutoCompleteTextView {
    private Tokenizer mTokenizer;

    public OppoMultiAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public OppoMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842859);
    }

    public OppoMultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void finishInit() {
    }

    public void setTokenizer(Tokenizer t) {
        this.mTokenizer = t;
    }

    protected void performFiltering(CharSequence text, int keyCode) {
        if (enoughToFilter()) {
            int end = getSelectionEnd();
            performFiltering(text, this.mTokenizer.findTokenStart(text, end), end, keyCode);
            return;
        }
        dismissDropDown();
        Filter f = getFilter();
        if (f != null) {
            f.filter(null);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x000f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean enoughToFilter() {
        Editable text = getText();
        int end = getSelectionEnd();
        if (end < 0 || this.mTokenizer == null || end - this.mTokenizer.findTokenStart(text, end) < getThreshold()) {
            return false;
        }
        return true;
    }

    public void performValidation() {
        Validator v = getValidator();
        if (v != null && this.mTokenizer != null) {
            Editable e = getText();
            int i = getText().length();
            while (i > 0) {
                int start = this.mTokenizer.findTokenStart(e, i);
                CharSequence sub = e.subSequence(start, this.mTokenizer.findTokenEnd(e, start));
                if (TextUtils.isEmpty(sub)) {
                    e.replace(start, i, "");
                } else if (!v.isValid(sub)) {
                    e.replace(start, i, this.mTokenizer.terminateToken(v.fixText(sub)));
                }
                i = start;
            }
        }
    }

    protected void performFiltering(CharSequence text, int start, int end, int keyCode) {
        getFilter().filter(text.subSequence(start, end), this);
    }

    protected void replaceText(CharSequence text) {
        clearComposingText();
        int end = getSelectionEnd();
        int start = this.mTokenizer.findTokenStart(getText(), end);
        Editable editable = getText();
        QwertyKeyListener.markAsReplaced(editable, start, end, TextUtils.substring(editable, start, end));
        editable.replace(start, end, this.mTokenizer.terminateToken(text));
    }
}
