package com.android.internal.app;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ColorViewRootUtil;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.internal.app.AlertController;
import com.color.util.ColorTypeCastingHelper;
import com.color.widget.ColorAlertLinearLayout;
import java.lang.ref.WeakReference;

public class ColorAlertController extends AlertController {
    private static final int DEFAULT_DISPALY_ID = 0;
    private static final int FULL_SCREEN_FLAG = -2147482112;
    private static final String TAG = "ColorAlertController";
    private TextPaint mButtonPaint;
    private ViewStub mButtonPanelStub;
    /* access modifiers changed from: private */
    public ComponentCallbacks mComponentCallbacks = new ComponentCallbacks() {
        /* class com.android.internal.app.ColorAlertController.AnonymousClass2 */

        public void onConfigurationChanged(Configuration configuration) {
            Log.d(ColorAlertController.TAG, "configurationChangedThread is " + Thread.currentThread());
            ColorAlertController.this.mHandler.sendEmptyMessage(2);
        }

        public void onLowMemory() {
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private boolean mIsValidateNavigationBar;
    /* access modifiers changed from: private */
    public ContentObserver mObserver = new ContentObserver(this.mHandler) {
        /* class com.android.internal.app.ColorAlertController.AnonymousClass1 */

        public void onChange(boolean selfChange) {
            ColorAlertController.this.mHandler.sendEmptyMessage(1);
        }
    };

    /* access modifiers changed from: private */
    public void onConfigurationChanged() {
        updateWindowAttributes();
        updateSpaceHeight();
        updateBg();
    }

    public ColorAlertController(Context context, DialogInterface di, Window window) {
        super(context, di, window);
        this.mContext = context;
        Log.d(TAG, "Handler Thread = " + Thread.currentThread());
        this.mHandler = new BottomSpaceHandler(this);
        initButtonPaint();
    }

    private void initButtonPaint() {
        this.mButtonPaint = new TextPaint();
        this.mButtonPaint.setTextSize((float) this.mContext.getResources().getDimensionPixelSize(201655750));
    }

    public void installContent() {
        this.mWindow.setContentView(selectContentViewInner());
        setupViewInner();
    }

    /* access modifiers changed from: protected */
    public int selectContentViewInner() {
        ColorBaseAlertController baseAlertController;
        if (!isCenterDialog() || (baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this)) == null) {
            return 201917617;
        }
        return baseAlertController.selectContentViewWrapper();
    }

    private boolean isCenterDialog() {
        ColorBaseAlertController baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this);
        return baseAlertController != null && baseAlertController.getDialogType() == 0;
    }

    /* access modifiers changed from: protected */
    public void setupViewInner() {
        updateWindowAttributes();
        setupAnimationAndGravity();
        addBottomSpace();
        updateBg();
        ListView listView = getListView();
        if (listView instanceof ColorRecyclerListView) {
            ((ColorRecyclerListView) listView).setNeedClip(needClipListView());
        }
        this.mButtonPanelStub = (ViewStub) this.mWindow.findViewById(201459058);
        if (!isCenterDialog()) {
            setButtonsVertical();
        } else if (needSetButtonsVertical()) {
            setButtonsVertical();
        } else {
            setButtonsHorizontal();
        }
        ColorBaseAlertController baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this);
        if (baseAlertController != null) {
            baseAlertController.setupViewWrapper();
        }
    }

    /* access modifiers changed from: protected */
    public void setupContent(final ViewGroup contentPanel) {
        ColorAlertController.super.setupContent(contentPanel);
        ViewGroup listPanel = (ViewGroup) contentPanel.findViewById(201458934);
        if (!(this.mMessage == null || listPanel == null || this.mListView == null)) {
            listPanel.addView(this.mListView, new ViewGroup.LayoutParams(-1, -1));
        }
        if (!isCenterDialog()) {
            relayoutListAndMessage(listPanel);
        } else if (this.mMessage != null) {
            relayoutMessageView(contentPanel);
        }
        contentPanel.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            /* class com.android.internal.app.ColorAlertController.AnonymousClass3 */

            public void onViewAttachedToWindow(View view) {
            }

            public void onViewDetachedFromWindow(View view) {
                contentPanel.removeOnAttachStateChangeListener(this);
                if (ColorAlertController.this.mComponentCallbacks != null) {
                    ColorAlertController.this.mContext.unregisterComponentCallbacks(ColorAlertController.this.mComponentCallbacks);
                    ComponentCallbacks unused = ColorAlertController.this.mComponentCallbacks = null;
                }
                ColorAlertController.this.mContext.getContentResolver().unregisterContentObserver(ColorAlertController.this.mObserver);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setupButtons(ViewGroup buttonPanel) {
        ColorAlertController.super.setupButtons(buttonPanel);
        resetButtonsPadding();
        setButtonsBackground();
    }

    private void relayoutListAndMessage(ViewGroup listPanel) {
        ColorBaseAlertController baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this);
        if (baseAlertController != null && baseAlertController.isMessageNeedScroll()) {
            if (this.mScrollView != null) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mScrollView.getLayoutParams();
                lp.height = 0;
                lp.weight = 1.0f;
                this.mScrollView.setLayoutParams(lp);
            }
            if (listPanel != 0) {
                LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) listPanel.getLayoutParams();
                lp2.height = 0;
                lp2.weight = 1.0f;
                listPanel.setLayoutParams(lp2);
            }
        }
    }

    private void relayoutMessageView(ViewGroup contentPanel) {
        final TextView messageView = (TextView) contentPanel.findViewById(16908299);
        messageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            /* class com.android.internal.app.ColorAlertController.AnonymousClass4 */

            public void onGlobalLayout() {
                if (messageView.getLineCount() > 1) {
                    messageView.setTextAlignment(2);
                } else {
                    messageView.setTextAlignment(4);
                }
                TextView textView = messageView;
                textView.setText(textView.getText());
                messageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void updateWindowAttributes() {
        Point realSize = getScreenSize();
        boolean port = realSize.x < realSize.y;
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        WindowManager.LayoutParams params = this.mWindow.getAttributes();
        if (port) {
            this.mWindow.setGravity(81);
            this.mWindow.clearFlags(FULL_SCREEN_FLAG);
            params.width = Math.min(realSize.x, displayMetrics.widthPixels);
            params.height = -2;
            return;
        }
        this.mWindow.setGravity(17);
        this.mWindow.addFlags(FULL_SCREEN_FLAG);
        params.width = Math.min(realSize.y, displayMetrics.widthPixels);
        params.height = this.mContext.getResources().getDimensionPixelSize(201655811);
    }

    private void setupAnimationAndGravity() {
        WindowManager.LayoutParams params = this.mWindow.getAttributes();
        if (isCenterDialog()) {
            params.windowAnimations = 201524237;
            params.gravity = 17;
        } else {
            params.windowAnimations = 201524236;
        }
        this.mWindow.setAttributes(params);
    }

    private void addBottomSpace() {
        if (!isCenterDialog()) {
            observeHideNavigationBar();
            this.mContext.registerComponentCallbacks(this.mComponentCallbacks);
        }
        if (needAddBottomView()) {
            updateSpaceHeight();
            updateWindowFlag();
            WindowManager.LayoutParams params = this.mWindow.getAttributes();
            addPrivateFlag(params);
            if (isSystemDialog(params)) {
                params.y -= spaceHeight();
            }
            this.mWindow.setAttributes(params);
        }
    }

    private void observeHideNavigationBar() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("manual_hide_navigationbar"), false, this.mObserver);
    }

    private int spaceHeight() {
        int result;
        if (!isFullScreen()) {
            return 0;
        }
        if (isGravityCenter()) {
            result = 0;
        } else if (isNavigationBarShow()) {
            result = navigationBarHeight();
        } else {
            result = this.mContext.getResources().getDimensionPixelSize(201655804);
        }
        if (!this.mIsValidateNavigationBar) {
            return 0;
        }
        return result;
    }

    private int navigationBarHeight() {
        Resources resources = this.mContext.getResources();
        return resources.getDimensionPixelSize(resources.getIdentifier("navigation_bar_height", "dimen", "android"));
    }

    private boolean isNavigationBarShow() {
        if (!supportNavigationBar()) {
            return false;
        }
        int navigationBarStatus = Settings.Secure.getInt(this.mContext.getContentResolver(), ColorViewRootUtil.KEY_NAVIGATIONBAR_MODE, 0);
        int navigationBarHideStatus = Settings.Secure.getInt(this.mContext.getContentResolver(), "manual_hide_navigationbar", 0);
        this.mIsValidateNavigationBar = (navigationBarStatus == -1 || navigationBarHideStatus == -1) ? false : true;
        if (navigationBarStatus == 0 || (navigationBarStatus == 1 && navigationBarHideStatus == 0)) {
            return true;
        }
        return false;
    }

    private boolean supportNavigationBar() {
        try {
            return WindowManagerGlobal.getWindowManagerService().hasNavigationBar(0);
        } catch (RemoteException e) {
            Log.d(TAG, "fail to get navigationBar's status, return false");
            return false;
        }
    }

    private boolean isGravityCenter() {
        return this.mWindow.getAttributes().gravity == 17;
    }

    private boolean needAddBottomView() {
        return !isCenterDialog() && isFullScreen();
    }

    private boolean isFullScreen() {
        try {
            if (WindowManagerGlobal.getWindowManagerService().getDockedStackSide() == -1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.d(TAG, "isFullScreen operation failed.Return false.Failed msg is " + e.getMessage());
            return false;
        }
    }

    private boolean isPortrait() {
        return getScreenSize().x < getScreenSize().y;
    }

    private Point getScreenSize() {
        Point point = new Point();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealSize(point);
        return point;
    }

    private void updateWindowFlag() {
        if (isGravityCenter()) {
            this.mWindow.clearFlags(FULL_SCREEN_FLAG);
        } else if (isNavigationBarShow()) {
            this.mWindow.setNavigationBarColor(-1);
            this.mWindow.clearFlags(134217728);
            this.mWindow.getDecorView().setSystemUiVisibility(16);
            this.mWindow.addFlags(FULL_SCREEN_FLAG);
        }
    }

    private void addPrivateFlag(WindowManager.LayoutParams params) {
        params.privateFlags |= 16777216;
        params.privateFlags |= 64;
    }

    private void updateBg() {
        View parentPanel = this.mWindow.findViewById(16909216);
        if (parentPanel != null && (parentPanel instanceof ColorAlertLinearLayout)) {
            ColorAlertLinearLayout colorAlertLinearLayout = (ColorAlertLinearLayout) parentPanel;
            if (this.mWindow.getAttributes().gravity == 17) {
                colorAlertLinearLayout.setNeedClip(true);
                colorAlertLinearLayout.setHasShadow(true);
                return;
            }
            colorAlertLinearLayout.setNeedClip(false);
            colorAlertLinearLayout.setHasShadow(false);
        }
    }

    private boolean needClipListView() {
        return !hasMessage() && !hasTitle() && !isCenterDialog();
    }

    private void resetButtonsPadding() {
        ColorBaseAlertController baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this);
        if (baseAlertController != null) {
            int paddingLeft = baseAlertController.getAlertControllerButtonNeutral().getPaddingLeft();
            int paddingTop = baseAlertController.getAlertControllerButtonNeutral().getPaddingTop();
            int paddingRight = baseAlertController.getAlertControllerButtonNeutral().getPaddingRight();
            int paddingBottom = baseAlertController.getAlertControllerButtonNeutral().getPaddingBottom();
            int paddingOffset = this.mContext.getResources().getDimensionPixelSize(201655805);
            if (!isCenterDialog()) {
                int buttonColor = this.mContext.getResources().getColor(201720995);
                baseAlertController.getAlertControllerButtonPositive().setTextColor(buttonColor);
                baseAlertController.getAlertControllerButtonNegative().setTextColor(buttonColor);
                boolean hasMessage = !TextUtils.isEmpty(this.mMessage);
                boolean hasTitle = !TextUtils.isEmpty(baseAlertController.getAlertControllerTitle());
                baseAlertController.getAlertControllerButtonNeutral().setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + paddingOffset);
                baseAlertController.getAlertControllerButtonNeutral().setMinHeight(baseAlertController.getAlertControllerButtonNeutral().getMinHeight() + paddingOffset);
                if (!hasMessage && !hasTitle && hasNeutralText() && !hasPositiveText()) {
                    baseAlertController.getAlertControllerButtonNeutral().setPadding(paddingLeft, paddingTop + paddingOffset, paddingRight, paddingBottom);
                    baseAlertController.getAlertControllerButtonNeutral().setMinHeight(baseAlertController.getAlertControllerButtonNeutral().getMinHeight() + paddingOffset);
                }
            }
            if (needSetButtonsVertical()) {
                baseAlertController.getAlertControllerButtonPositive().setPadding(paddingLeft, paddingTop + paddingOffset, paddingRight, paddingBottom);
                baseAlertController.getAlertControllerButtonPositive().setMinHeight(baseAlertController.getAlertControllerButtonPositive().getMinHeight() + paddingOffset);
                baseAlertController.getAlertControllerButtonNegative().setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + paddingOffset);
                baseAlertController.getAlertControllerButtonNegative().setMinHeight(baseAlertController.getAlertControllerButtonNegative().getMinHeight() + paddingOffset);
            }
        }
    }

    private void setButtonsBackground() {
        ColorBaseAlertController baseAlertController;
        Button target;
        if (!isCenterDialog() && !hasTitle() && !hasMessage() && this.mListView == null && !hasCustomView() && (baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this)) != null) {
            if (isSingleButton()) {
                if (hasPositiveText()) {
                    target = baseAlertController.getAlertControllerButtonPositive();
                } else {
                    target = hasNeutralText() ? baseAlertController.getAlertControllerButtonNeutral() : baseAlertController.getAlertControllerButtonNegative();
                }
                target.setBackgroundResource(201852323);
            } else if (isDoubleButtons()) {
                (hasPositiveText() ? baseAlertController.getAlertControllerButtonPositive() : baseAlertController.getAlertControllerButtonNeutral()).setBackgroundResource(201852323);
            } else if (isTripleButtons()) {
                baseAlertController.getAlertControllerButtonPositive().setBackgroundResource(201852323);
            }
        }
    }

    private boolean hasCustomView() {
        return ((FrameLayout) this.mWindow.findViewById(16908331)).getChildCount() != 0;
    }

    private boolean isSingleButton() {
        return buttonCount() == 1;
    }

    private boolean isDoubleButtons() {
        return buttonCount() == 2;
    }

    private boolean isTripleButtons() {
        return buttonCount() == 3;
    }

    private boolean needSetButtonsVertical() {
        ColorBaseAlertController baseAlertController;
        if (buttonCount() == 0 || (baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this)) == null) {
            return false;
        }
        int buttonWidth = (parentWidth() / buttonCount()) - (this.mContext.getResources().getDimensionPixelOffset(201655806) * 2);
        int positiveTextWidth = hasPositiveText() ? (int) this.mButtonPaint.measureText(baseAlertController.getAlertControllerButtonPositiveText().toString()) : 0;
        int negativeTextWidth = hasNegativeText() ? (int) this.mButtonPaint.measureText(baseAlertController.getAlertControllerButtonNegativeText().toString()) : 0;
        int neutralTextWidth = hasNeutralText() ? (int) this.mButtonPaint.measureText(baseAlertController.getAlertControllerButtonNeutralText().toString()) : 0;
        if (positiveTextWidth > buttonWidth || negativeTextWidth > buttonWidth || neutralTextWidth > buttonWidth) {
            return true;
        }
        return false;
    }

    private int buttonCount() {
        int count = 0;
        if (hasPositiveText()) {
            count = 0 + 1;
        }
        if (hasNegativeText()) {
            count++;
        }
        if (hasNeutralText()) {
            return count + 1;
        }
        return count;
    }

    private boolean hasPositiveText() {
        ColorBaseAlertController baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this);
        return baseAlertController != null && !TextUtils.isEmpty(baseAlertController.getAlertControllerButtonPositiveText());
    }

    private boolean hasNegativeText() {
        ColorBaseAlertController baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this);
        return baseAlertController != null && !TextUtils.isEmpty(baseAlertController.getAlertControllerButtonNegativeText());
    }

    private boolean hasNeutralText() {
        ColorBaseAlertController baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this);
        return baseAlertController != null && !TextUtils.isEmpty(baseAlertController.getAlertControllerButtonNeutralText());
    }

    private boolean hasMessage() {
        return !TextUtils.isEmpty(this.mMessage);
    }

    private boolean hasTitle() {
        ColorBaseAlertController baseAlertController = (ColorBaseAlertController) ColorTypeCastingHelper.typeCasting(ColorBaseAlertController.class, this);
        return baseAlertController != null && !TextUtils.isEmpty(baseAlertController.getAlertControllerTitle());
    }

    private int parentWidth() {
        View parentPanel = this.mWindow.findViewById(16909216);
        int parentPadding = 0;
        if (parentPanel != null) {
            parentPadding = parentPanel.getPaddingLeft();
        }
        return this.mWindow.getAttributes().width - (parentPadding * 2);
    }

    private void setButtonsVertical() {
        this.mButtonPanelStub.setLayoutResource(201917618);
        this.mButtonPanelStub.inflate();
        View divider1 = this.mWindow.findViewById(201459059);
        View divider2 = this.mWindow.findViewById(201459060);
        if (!isCenterDialog() || TextUtils.isEmpty(this.mMessage)) {
            divider2.setVisibility(0);
        } else {
            divider1.setVisibility(0);
        }
    }

    private void setButtonsHorizontal() {
        this.mButtonPanelStub.setLayoutResource(201917619);
        this.mButtonPanelStub.inflate();
        showHorizontalDivider();
    }

    private void showHorizontalDivider() {
        ImageView dividerOne = (ImageView) this.mWindow.findViewById(201459026);
        ImageView dividerTwo = (ImageView) this.mWindow.findViewById(201459027);
        if (buttonCount() == 2) {
            dividerOne.setVisibility(0);
        }
        if (buttonCount() == 3) {
            dividerOne.setVisibility(0);
            dividerTwo.setVisibility(0);
        }
    }

    /* access modifiers changed from: private */
    public void updateSpaceHeight() {
        View space;
        ViewGroup parent = (ViewGroup) this.mWindow.findViewById(16909216);
        if (!(parent == null || (space = parent.findViewById(201459052)) == null)) {
            ViewGroup.LayoutParams params = space.getLayoutParams();
            params.height = spaceHeight();
            space.setLayoutParams(params);
        }
        updateWindowFlag();
        WindowManager.LayoutParams params2 = this.mWindow.getAttributes();
        if (isSystemDialog(params2)) {
            if (!isNavigationBarShow()) {
                params2.y = 0;
            } else if (isGravityCenter()) {
                params2.y = 0;
            }
        }
        this.mWindow.setAttributes(params2);
    }

    private boolean isSystemDialog(WindowManager.LayoutParams params) {
        return params.type == 2003 || params.type == 2038;
    }

    private static final class BottomSpaceHandler extends Handler {
        private static final int MSG_CONFIGURATION_CHANGED = 2;
        private static final int MSG_UPDATE_SPACE_HEIGHT = 1;
        private WeakReference<ColorAlertController> mReference;

        public BottomSpaceHandler(ColorAlertController controller) {
            this.mReference = new WeakReference<>(controller);
        }

        public void handleMessage(Message msg) {
            ColorAlertController controller = this.mReference.get();
            if (controller != null) {
                int i = msg.what;
                if (i == 1) {
                    controller.updateSpaceHeight();
                } else if (i == 2) {
                    controller.onConfigurationChanged();
                }
            }
        }
    }

    public static class FadingScrollView extends ScrollView {
        public FadingScrollView(Context context) {
            super(context);
        }

        public FadingScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public FadingScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
    }

    public static class ColorRecyclerListView extends AlertController.RecycleListView {
        private Path mClipPath;
        private int mCornerRadius;
        private boolean mNeedClip;

        public ColorRecyclerListView(Context context) {
            this(context, null);
        }

        public ColorRecyclerListView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mCornerRadius = context.getResources().getDimensionPixelOffset(201655807);
        }

        public void draw(Canvas canvas) {
            canvas.save();
            if (this.mNeedClip) {
                clipRoundBounds(canvas);
            }
            ColorAlertController.super.draw(canvas);
            canvas.restore();
        }

        private void clipRoundBounds(Canvas canvas) {
            int i = this.mCornerRadius;
            float[] cornerRadius = {(float) i, (float) i, (float) i, (float) i, 0.0f, 0.0f, 0.0f, 0.0f};
            if (this.mClipPath == null) {
                this.mClipPath = new Path();
                this.mClipPath.addRoundRect((float) getLeft(), (float) getTop(), (float) getRight(), (float) getBottom(), cornerRadius, Path.Direction.CW);
            }
            canvas.clipPath(this.mClipPath);
        }

        public void setNeedClip(boolean needClip) {
            this.mNeedClip = needClip;
        }
    }
}
