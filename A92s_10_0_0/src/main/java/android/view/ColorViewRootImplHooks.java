package android.view;

import android.common.ColorFrameworkFactory;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import com.color.favorite.IColorFavoriteManager;
import com.color.screenshot.ColorLongshotViewRoot;
import com.color.util.ColorTypeCastingHelper;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

public class ColorViewRootImplHooks {
    private final String TAG = "ColorViewRootImplHooks";
    private final ColorLongshotViewRoot mLongshotViewRoot = new ColorLongshotViewRoot();
    private final ViewRootImpl mViewRootImpl;

    public ColorViewRootImplHooks(ViewRootImpl viewRootImpl, Context context) {
        this.mViewRootImpl = viewRootImpl;
    }

    public ColorLongshotViewRoot getLongshotViewRoot() {
        return this.mLongshotViewRoot;
    }

    public void setView(View view) {
        if (view != null) {
            ((IColorFavoriteManager) OppoFeatureCache.getOrCreate(IColorFavoriteManager.DEFAULT, new Object[0])).init(view.getContext());
        }
    }

    public void markUserDefinedToast(View view, WindowManager.LayoutParams attrs) {
        ColorBaseLayoutParams params;
        if (view == null || attrs == null) {
            Log.w("ColorViewRootImplHooks", "markUserDefinedToast invalid args, view=" + view + ", attrs=" + attrs);
        } else if (attrs.type == 2005 && view.mID != 201458937 && (params = (ColorBaseLayoutParams) ColorTypeCastingHelper.typeCasting(ColorBaseLayoutParams.class, attrs)) != null) {
            params.colorFlags |= 1;
        }
    }

    public void dispatchDetachedFromWindow(View view) {
        if (view != null) {
            ((IColorFavoriteManager) OppoFeatureCache.getOrCreate(IColorFavoriteManager.DEFAULT, new Object[0])).release();
        }
    }

    /* access modifiers changed from: package-private */
    public ViewRootImpl.W createWindowClient(ViewRootImpl viewAncestor) {
        return new ColorW(viewAncestor);
    }

    public MotionEvent updatePointerEvent(MotionEvent event, View mView, Configuration mLastConfiguration) {
        return ((IColorAccidentallyTouchHelper) OppoFeatureCache.getOrCreate(IColorAccidentallyTouchHelper.DEFAULT, new Object[0])).updatePointerEvent(event, mView, mLastConfiguration);
    }

    static class ColorW extends ViewRootImpl.W {
        private IColorDirectViewHelper mDirectHelper;
        private IColorLongshotViewHelper mLongshotHelper;

        ColorW(ViewRootImpl viewAncestor) {
            super(viewAncestor);
            WeakReference<ViewRootImpl> reference = null;
            try {
                Field viewAncestorField = ColorW.class.getSuperclass().getDeclaredField("mViewAncestor");
                viewAncestorField.setAccessible(true);
                reference = (WeakReference) viewAncestorField.get(this);
            } catch (NoSuchFieldException e) {
                Log.w("ColorViewRootImplHooks", "NoSuchFieldException reflect to get mViewAncestor from ViewRootImpl");
            } catch (IllegalAccessException e2) {
                Log.w("ColorViewRootImplHooks", "IllegalAccessException reflect to get mViewAncestor from ViewRootImpl");
            }
            if (reference != null) {
                this.mLongshotHelper = ((IColorViewRootUtil) OppoFeatureCache.getOrCreate(IColorViewRootUtil.DEFAULT, new Object[0])).getColorLongshotViewHelper(reference);
                this.mDirectHelper = (IColorDirectViewHelper) ColorFrameworkFactory.getInstance().getFeature(IColorDirectViewHelper.DEFAULT, reference);
            }
        }

        @Override // android.os.Binder, android.view.IWindow.Stub
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IColorLongshotViewHelper iColorLongshotViewHelper = this.mLongshotHelper;
            if (iColorLongshotViewHelper != null && iColorLongshotViewHelper.onTransact(code, data, reply, flags)) {
                return true;
            }
            IColorDirectViewHelper iColorDirectViewHelper = this.mDirectHelper;
            if (iColorDirectViewHelper == null || !iColorDirectViewHelper.onTransact(code, data, reply, flags)) {
                return super.onTransact(code, data, reply, flags);
            }
            return true;
        }
    }
}
