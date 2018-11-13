package android.support.v4.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.InsetDrawable;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.MenuItem;
import android.view.View;

@Deprecated
public class ActionBarDrawerToggle implements DrawerListener {
    private static final int ID_HOME = 16908332;
    private static final ActionBarDrawerToggleImpl IMPL;
    private static final float TOGGLE_DRAWABLE_OFFSET = 0.33333334f;
    private final Activity mActivity;
    private final Delegate mActivityImpl;
    private final int mCloseDrawerContentDescRes;
    private Drawable mDrawerImage;
    private final int mDrawerImageResource;
    private boolean mDrawerIndicatorEnabled;
    private final DrawerLayout mDrawerLayout;
    private boolean mHasCustomUpIndicator;
    private Drawable mHomeAsUpIndicator;
    private final int mOpenDrawerContentDescRes;
    private Object mSetIndicatorInfo;
    private SlideDrawable mSlider;

    private interface ActionBarDrawerToggleImpl {
        Drawable getThemeUpIndicator(Activity activity);

        Object setActionBarDescription(Object obj, Activity activity, int i);

        Object setActionBarUpIndicator(Object obj, Activity activity, Drawable drawable, int i);
    }

    private static class ActionBarDrawerToggleImplBase implements ActionBarDrawerToggleImpl {
        /* synthetic */ ActionBarDrawerToggleImplBase(ActionBarDrawerToggleImplBase -this0) {
            this();
        }

        private ActionBarDrawerToggleImplBase() {
        }

        public Drawable getThemeUpIndicator(Activity activity) {
            return null;
        }

        public Object setActionBarUpIndicator(Object info, Activity activity, Drawable themeImage, int contentDescRes) {
            return info;
        }

        public Object setActionBarDescription(Object info, Activity activity, int contentDescRes) {
            return info;
        }
    }

    private static class ActionBarDrawerToggleImplHC implements ActionBarDrawerToggleImpl {
        /* synthetic */ ActionBarDrawerToggleImplHC(ActionBarDrawerToggleImplHC -this0) {
            this();
        }

        private ActionBarDrawerToggleImplHC() {
        }

        public Drawable getThemeUpIndicator(Activity activity) {
            return ActionBarDrawerToggleHoneycomb.getThemeUpIndicator(activity);
        }

        public Object setActionBarUpIndicator(Object info, Activity activity, Drawable themeImage, int contentDescRes) {
            return ActionBarDrawerToggleHoneycomb.setActionBarUpIndicator(info, activity, themeImage, contentDescRes);
        }

        public Object setActionBarDescription(Object info, Activity activity, int contentDescRes) {
            return ActionBarDrawerToggleHoneycomb.setActionBarDescription(info, activity, contentDescRes);
        }
    }

    private static class ActionBarDrawerToggleImplJellybeanMR2 implements ActionBarDrawerToggleImpl {
        /* synthetic */ ActionBarDrawerToggleImplJellybeanMR2(ActionBarDrawerToggleImplJellybeanMR2 -this0) {
            this();
        }

        private ActionBarDrawerToggleImplJellybeanMR2() {
        }

        public Drawable getThemeUpIndicator(Activity activity) {
            return ActionBarDrawerToggleJellybeanMR2.getThemeUpIndicator(activity);
        }

        public Object setActionBarUpIndicator(Object info, Activity activity, Drawable themeImage, int contentDescRes) {
            return ActionBarDrawerToggleJellybeanMR2.setActionBarUpIndicator(info, activity, themeImage, contentDescRes);
        }

        public Object setActionBarDescription(Object info, Activity activity, int contentDescRes) {
            return ActionBarDrawerToggleJellybeanMR2.setActionBarDescription(info, activity, contentDescRes);
        }
    }

    public interface Delegate {
        @Nullable
        Drawable getThemeUpIndicator();

        void setActionBarDescription(int i);

        void setActionBarUpIndicator(Drawable drawable, int i);
    }

    public interface DelegateProvider {
        @Nullable
        Delegate getDrawerToggleDelegate();
    }

    private class SlideDrawable extends InsetDrawable implements Callback {
        private final boolean mHasMirroring;
        private float mOffset;
        private float mPosition;
        private final Rect mTmpRect;
        final /* synthetic */ ActionBarDrawerToggle this$0;

        /* synthetic */ SlideDrawable(ActionBarDrawerToggle this$0, Drawable wrapped, SlideDrawable -this2) {
            this(this$0, wrapped);
        }

        private SlideDrawable(ActionBarDrawerToggle this$0, Drawable wrapped) {
            boolean z = false;
            this.this$0 = this$0;
            super(wrapped, 0);
            if (VERSION.SDK_INT > 18) {
                z = true;
            }
            this.mHasMirroring = z;
            this.mTmpRect = new Rect();
        }

        public void setPosition(float position) {
            this.mPosition = position;
            invalidateSelf();
        }

        public float getPosition() {
            return this.mPosition;
        }

        public void setOffset(float offset) {
            this.mOffset = offset;
            invalidateSelf();
        }

        public void draw(Canvas canvas) {
            copyBounds(this.mTmpRect);
            canvas.save();
            boolean isLayoutRTL = ViewCompat.getLayoutDirection(this.this$0.mActivity.getWindow().getDecorView()) == 1;
            int flipRtl = isLayoutRTL ? -1 : 1;
            int width = this.mTmpRect.width();
            canvas.translate((((-this.mOffset) * ((float) width)) * this.mPosition) * ((float) flipRtl), 0.0f);
            if (isLayoutRTL && (this.mHasMirroring ^ 1) != 0) {
                canvas.translate((float) width, 0.0f);
                canvas.scale(-1.0f, 1.0f);
            }
            super.draw(canvas);
            canvas.restore();
        }
    }

    static {
        int version = VERSION.SDK_INT;
        if (version >= 18) {
            IMPL = new ActionBarDrawerToggleImplJellybeanMR2();
        } else if (version >= 11) {
            IMPL = new ActionBarDrawerToggleImplHC();
        } else {
            IMPL = new ActionBarDrawerToggleImplBase();
        }
    }

    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        this(activity, drawerLayout, assumeMaterial(activity) ^ 1, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    private static boolean assumeMaterial(Context context) {
        if (context.getApplicationInfo().targetSdkVersion < 21 || VERSION.SDK_INT < 21) {
            return false;
        }
        return true;
    }

    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, boolean animate, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        this.mDrawerIndicatorEnabled = true;
        this.mActivity = activity;
        if (activity instanceof DelegateProvider) {
            this.mActivityImpl = ((DelegateProvider) activity).getDrawerToggleDelegate();
        } else {
            this.mActivityImpl = null;
        }
        this.mDrawerLayout = drawerLayout;
        this.mDrawerImageResource = drawerImageRes;
        this.mOpenDrawerContentDescRes = openDrawerContentDescRes;
        this.mCloseDrawerContentDescRes = closeDrawerContentDescRes;
        this.mHomeAsUpIndicator = getThemeUpIndicator();
        this.mDrawerImage = ContextCompat.getDrawable(activity, drawerImageRes);
        this.mSlider = new SlideDrawable(this, this.mDrawerImage, null);
        this.mSlider.setOffset(animate ? TOGGLE_DRAWABLE_OFFSET : 0.0f);
    }

    public void syncState() {
        if (this.mDrawerLayout.isDrawerOpen(8388611)) {
            this.mSlider.setPosition(1.0f);
        } else {
            this.mSlider.setPosition(0.0f);
        }
        if (this.mDrawerIndicatorEnabled) {
            setActionBarUpIndicator(this.mSlider, this.mDrawerLayout.isDrawerOpen(8388611) ? this.mCloseDrawerContentDescRes : this.mOpenDrawerContentDescRes);
        }
    }

    public void setHomeAsUpIndicator(Drawable indicator) {
        if (indicator == null) {
            this.mHomeAsUpIndicator = getThemeUpIndicator();
            this.mHasCustomUpIndicator = false;
        } else {
            this.mHomeAsUpIndicator = indicator;
            this.mHasCustomUpIndicator = true;
        }
        if (!this.mDrawerIndicatorEnabled) {
            setActionBarUpIndicator(this.mHomeAsUpIndicator, 0);
        }
    }

    public void setHomeAsUpIndicator(int resId) {
        Drawable indicator = null;
        if (resId != 0) {
            indicator = ContextCompat.getDrawable(this.mActivity, resId);
        }
        setHomeAsUpIndicator(indicator);
    }

    public void setDrawerIndicatorEnabled(boolean enable) {
        if (enable != this.mDrawerIndicatorEnabled) {
            if (enable) {
                setActionBarUpIndicator(this.mSlider, this.mDrawerLayout.isDrawerOpen(8388611) ? this.mCloseDrawerContentDescRes : this.mOpenDrawerContentDescRes);
            } else {
                setActionBarUpIndicator(this.mHomeAsUpIndicator, 0);
            }
            this.mDrawerIndicatorEnabled = enable;
        }
    }

    public boolean isDrawerIndicatorEnabled() {
        return this.mDrawerIndicatorEnabled;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!this.mHasCustomUpIndicator) {
            this.mHomeAsUpIndicator = getThemeUpIndicator();
        }
        this.mDrawerImage = ContextCompat.getDrawable(this.mActivity, this.mDrawerImageResource);
        syncState();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null || item.getItemId() != ID_HOME || !this.mDrawerIndicatorEnabled) {
            return false;
        }
        if (this.mDrawerLayout.isDrawerVisible(8388611)) {
            this.mDrawerLayout.closeDrawer(8388611);
        } else {
            this.mDrawerLayout.openDrawer(8388611);
        }
        return true;
    }

    public void onDrawerSlide(View drawerView, float slideOffset) {
        float glyphOffset = this.mSlider.getPosition();
        if (slideOffset > 0.5f) {
            glyphOffset = Math.max(glyphOffset, Math.max(0.0f, slideOffset - 0.5f) * 2.0f);
        } else {
            glyphOffset = Math.min(glyphOffset, slideOffset * 2.0f);
        }
        this.mSlider.setPosition(glyphOffset);
    }

    public void onDrawerOpened(View drawerView) {
        this.mSlider.setPosition(1.0f);
        if (this.mDrawerIndicatorEnabled) {
            setActionBarDescription(this.mCloseDrawerContentDescRes);
        }
    }

    public void onDrawerClosed(View drawerView) {
        this.mSlider.setPosition(0.0f);
        if (this.mDrawerIndicatorEnabled) {
            setActionBarDescription(this.mOpenDrawerContentDescRes);
        }
    }

    public void onDrawerStateChanged(int newState) {
    }

    Drawable getThemeUpIndicator() {
        if (this.mActivityImpl != null) {
            return this.mActivityImpl.getThemeUpIndicator();
        }
        return IMPL.getThemeUpIndicator(this.mActivity);
    }

    void setActionBarUpIndicator(Drawable upDrawable, int contentDescRes) {
        if (this.mActivityImpl != null) {
            this.mActivityImpl.setActionBarUpIndicator(upDrawable, contentDescRes);
        } else {
            this.mSetIndicatorInfo = IMPL.setActionBarUpIndicator(this.mSetIndicatorInfo, this.mActivity, upDrawable, contentDescRes);
        }
    }

    void setActionBarDescription(int contentDescRes) {
        if (this.mActivityImpl != null) {
            this.mActivityImpl.setActionBarDescription(contentDescRes);
        } else {
            this.mSetIndicatorInfo = IMPL.setActionBarDescription(this.mSetIndicatorInfo, this.mActivity, contentDescRes);
        }
    }
}
