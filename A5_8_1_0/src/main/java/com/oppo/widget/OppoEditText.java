package com.oppo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.IntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import com.android.internal.widget.ExploreByTouchHelper;
import oppo.R;

public class OppoEditText extends EditText {
    private static final int LARGEVIEWID = 1;
    private static final boolean LOG_DBG = false;
    private static final int NOID = -1;
    private static final int SMALLVIEWID = 0;
    private static final String TAG = "OppoEditText";
    private static final int TOTALVIEWID = 2;
    private int mArea;
    private Context mContext;
    private boolean mDeletable;
    private String mDeleteButton;
    private Drawable mDeleteNormal;
    private Drawable mDeletePressed;
    private int mDrawablePadding;
    private int mDrawableSizeRight;
    private boolean mForceFinishDetach;
    private OppoTextWatcher mOppoTextWatcher;
    private OnPasswordDeletedListener mPasswordDeleteListener;
    private boolean mQuickDelete;
    boolean mShouldHandleDelete;
    private OnTextDeletedListener mTextDeleteListener;
    private final AccessibilityTouchHelper mTouchHelper;

    public class AccessibilityTouchHelper extends ExploreByTouchHelper implements OnClickListener {
        private Context mContext = null;
        private Rect mDeleteRect = null;
        private View mHostView = null;
        private Rect mViewRect = null;

        public AccessibilityTouchHelper(View hostView) {
            super(hostView);
            this.mHostView = hostView;
            this.mContext = this.mHostView.getContext();
        }

        private void initDeleteRect() {
            this.mDeleteRect = new Rect();
            this.mDeleteRect.left = OppoEditText.this.getDeleteButtonLeft();
            this.mDeleteRect.right = OppoEditText.this.getWidth();
            this.mDeleteRect.top = 0;
            this.mDeleteRect.bottom = OppoEditText.this.getHeight();
        }

        private void initViewRect() {
            this.mViewRect = new Rect();
            this.mViewRect.left = 0;
            this.mViewRect.right = OppoEditText.this.getWidth();
            this.mViewRect.top = 0;
            this.mViewRect.bottom = OppoEditText.this.getHeight();
        }

        protected int getVirtualViewAt(float x, float y) {
            if (this.mDeleteRect == null) {
                initDeleteRect();
            }
            if (x < ((float) this.mDeleteRect.left) || x > ((float) this.mDeleteRect.right) || y < ((float) this.mDeleteRect.top) || y > ((float) this.mDeleteRect.bottom) || !OppoEditText.this.isDeleteButtonExist()) {
                return Integer.MIN_VALUE;
            }
            return 0;
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setContentDescription(OppoEditText.this.mDeleteButton);
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            if (virtualViewId == 0) {
                node.setContentDescription(OppoEditText.this.mDeleteButton);
                node.setClassName(Button.class.getName());
                node.addAction(16);
            }
            node.setBoundsInParent(getItemBounds(virtualViewId));
        }

        public void onClick(View v) {
        }

        private Rect getItemBounds(int virtualViewId) {
            if (virtualViewId != 0) {
                return new Rect();
            }
            if (this.mDeleteRect == null) {
                initDeleteRect();
            }
            return this.mDeleteRect;
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            if (OppoEditText.this.isDeleteButtonExist()) {
                virtualViewIds.add(0);
            }
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (action != 16) {
                return OppoEditText.LOG_DBG;
            }
            if (virtualViewId == 0 && OppoEditText.this.isDeleteButtonExist()) {
                OppoEditText.this.onFastDelete();
            }
            return true;
        }
    }

    public interface OnPasswordDeletedListener {
        boolean onPasswordDeleted();
    }

    public interface OnTextDeletedListener {
        boolean onTextDeleted();
    }

    private class OppoTextWatcher implements TextWatcher {
        /* synthetic */ OppoTextWatcher(OppoEditText this$0, OppoTextWatcher -this1) {
            this();
        }

        private OppoTextWatcher() {
        }

        public void afterTextChanged(Editable arg0) {
            OppoEditText.this.updateDeletableStatus(OppoEditText.this.hasFocus());
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    }

    public OppoEditText(Context context) {
        this(context, null);
    }

    public OppoEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 16842862);
    }

    public OppoEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OppoEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mShouldHandleDelete = LOG_DBG;
        this.mDeleteButton = null;
        this.mQuickDelete = LOG_DBG;
        this.mDeletable = LOG_DBG;
        this.mTextDeleteListener = null;
        this.mPasswordDeleteListener = null;
        this.mOppoTextWatcher = null;
        this.mForceFinishDetach = LOG_DBG;
        this.mContext = context;
        boolean quickDelete = LOG_DBG;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OppoEditText, 0, 0);
        if (a != null) {
            int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case 1:
                        quickDelete = a.getBoolean(attr, LOG_DBG);
                        break;
                    default:
                        break;
                }
            }
            a.recycle();
        }
        setFastDeletable(quickDelete);
        this.mDeleteNormal = context.getDrawable(201852137);
        if (this.mDeleteNormal != null) {
            this.mArea = this.mDeleteNormal.getIntrinsicWidth();
            this.mDeleteNormal.setBounds(0, 0, this.mArea, this.mArea);
        }
        this.mDeletePressed = context.getDrawable(201852136);
        if (this.mDeletePressed != null) {
            this.mDeletePressed.setBounds(0, 0, this.mArea, this.mArea);
        }
        this.mTouchHelper = new AccessibilityTouchHelper(this);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(1);
        this.mDeleteButton = this.mContext.getString(201590171);
        this.mTouchHelper.invalidateRoot();
    }

    public void setFastDeletable(boolean quickDelete) {
        if (this.mQuickDelete != quickDelete) {
            this.mQuickDelete = quickDelete;
            if (this.mQuickDelete) {
                if (this.mOppoTextWatcher == null) {
                    this.mOppoTextWatcher = new OppoTextWatcher(this, null);
                    addTextChangedListener(this.mOppoTextWatcher);
                }
                this.mDrawablePadding = this.mContext.getResources().getDimensionPixelSize(201655514);
                setCompoundDrawablePadding(this.mDrawablePadding);
            }
        }
    }

    public boolean isFastDeletable() {
        return this.mQuickDelete;
    }

    private void updateDeletableStatus(boolean foucus) {
        if (TextUtils.isEmpty(getText().toString())) {
            setCompoundDrawables(null, null, null, null);
            this.mDeletable = LOG_DBG;
        } else if (foucus) {
            if (this.mDeleteNormal != null && (this.mDeletable ^ 1) != 0) {
                setCompoundDrawables(null, null, this.mDeleteNormal, null);
                this.mDeletable = true;
            }
        } else if (this.mDeletable) {
            setCompoundDrawables(null, null, null, null);
            this.mDeletable = LOG_DBG;
        }
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (this.mQuickDelete) {
            updateDeletableStatus(gainFocus);
        }
    }

    public void setOnTextDeletedListener(OnTextDeletedListener textDeleteListener) {
        this.mTextDeleteListener = textDeleteListener;
    }

    public void setTextDeletedListener(OnPasswordDeletedListener passwordDeletedListener) {
        this.mPasswordDeleteListener = passwordDeletedListener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mQuickDelete && (isEmpty(getText().toString()) ^ 1) != 0 && hasFocus()) {
            int deltX = ((this.mRight - this.mLeft) - this.mPaddingRight) - this.mDrawableSizeRight;
            if (getWidth() >= (this.mDrawableSizeRight + this.mPaddingRight) + this.mPaddingLeft) {
                int curX = (int) event.getX();
                int curY = (int) event.getY();
                boolean touchOnQuickDelete = getLayoutDirection() == 1 ? curX < (this.mLeft + this.mPaddingLeft) + this.mDrawableSizeRight ? true : LOG_DBG : curX > deltX ? true : LOG_DBG;
                switch (event.getAction()) {
                    case 0:
                        this.mTouchHelper.sendEventForVirtualView(0, 16);
                        if (touchOnQuickDelete && this.mDeletable) {
                            this.mShouldHandleDelete = true;
                            if (this.mDeletePressed != null) {
                                setCompoundDrawables(null, null, this.mDeletePressed, null);
                            }
                            return true;
                        }
                    case 1:
                        if (touchOnQuickDelete && this.mDeletable && this.mShouldHandleDelete) {
                            if (this.mDeleteNormal != null) {
                                setCompoundDrawables(null, null, this.mDeleteNormal, null);
                            }
                            if (this.mTextDeleteListener == null || !this.mTextDeleteListener.onTextDeleted()) {
                                this.mTouchHelper.sendEventForVirtualView(1, 1);
                                onFastDelete();
                                this.mShouldHandleDelete = LOG_DBG;
                                return true;
                            }
                        }
                    case 2:
                        if ((!touchOnQuickDelete || curY < 0 || curY > getHeight()) && this.mDeleteNormal != null) {
                            setCompoundDrawables(null, null, this.mDeleteNormal, null);
                            break;
                        }
                    case 3:
                    case 4:
                        if (this.mDeleteNormal != null) {
                            setCompoundDrawables(null, null, this.mDeleteNormal, null);
                            break;
                        }
                        break;
                }
            }
            return LOG_DBG;
        }
        return super.onTouchEvent(event);
    }

    private void onFastDelete() {
        CharSequence mText = getText();
        ((Editable) mText).delete(0, mText.length());
        setText("");
    }

    private boolean isEmpty(String currentText) {
        if (currentText == null) {
            return LOG_DBG;
        }
        return TextUtils.isEmpty(currentText);
    }

    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        setCompoundDrawablesRelative(left, top, right, bottom);
        if (right != null) {
            this.mDrawableSizeRight = right.getBounds().width();
        } else {
            this.mDrawableSizeRight = 0;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!this.mQuickDelete || keyCode != 67) {
            return super.onKeyDown(keyCode, event);
        }
        super.onKeyDown(keyCode, event);
        if (this.mPasswordDeleteListener != null) {
            this.mPasswordDeleteListener.onPasswordDeleted();
        }
        return true;
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        Selection.setSelection(getText(), length());
    }

    public void forceFinishDetach() {
        this.mForceFinishDetach = true;
    }

    public void dispatchStartTemporaryDetach() {
        if (this.mForceFinishDetach) {
            onStartTemporaryDetach();
        } else {
            super.dispatchStartTemporaryDetach();
        }
    }

    public boolean isDeleteButtonExist() {
        return (!this.mQuickDelete || (isEmpty(getText().toString()) ^ 1) == 0) ? LOG_DBG : hasFocus();
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        if (isDeleteButtonExist() && this.mTouchHelper != null && this.mTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    public int getDeleteButtonLeft() {
        int drawableSizeRight = 0;
        if (this.mDeleteNormal != null) {
            drawableSizeRight = this.mDeleteNormal.getIntrinsicWidth();
        }
        return ((getRight() - getLeft()) - getPaddingRight()) - drawableSizeRight;
    }
}
