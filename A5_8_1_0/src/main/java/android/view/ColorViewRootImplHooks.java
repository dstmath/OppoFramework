package android.view;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.screenshot.ColorLongshotViewRoot;
import com.color.util.ColorAccidentallyTouchUtils;

public class ColorViewRootImplHooks {
    private final ColorLongshotViewRoot mLongshotViewRoot = new ColorLongshotViewRoot();
    private final ViewRootImpl mViewRootImpl;

    static class ColorW extends W {
        private final ColorLongshotViewHelper mLongshotHelper = new ColorLongshotViewHelper(this.mViewAncestor);

        ColorW(ViewRootImpl viewAncestor) {
            super(viewAncestor);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (this.mLongshotHelper.onTransact(code, data, reply, flags)) {
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    public ColorViewRootImplHooks(ViewRootImpl viewRootImpl, Context context) {
        this.mViewRootImpl = viewRootImpl;
        ColorAccidentallyTouchUtils.getInstance().initData(context);
    }

    public ColorLongshotViewRoot getLongshotViewRoot() {
        return this.mLongshotViewRoot;
    }

    W createWindowClient(ViewRootImpl viewAncestor) {
        return new ColorW(viewAncestor);
    }
}
