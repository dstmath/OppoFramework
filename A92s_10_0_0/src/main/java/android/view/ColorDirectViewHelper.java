package android.view;

import android.os.Parcel;
import com.color.direct.ColorDirectFindCmd;
import com.color.direct.ColorDirectUtils;
import java.lang.ref.WeakReference;

public class ColorDirectViewHelper extends ColorDummyDirectViewHelper {
    private static final boolean DBG = ColorDirectUtils.DBG;
    private static final String TAG = "DirectService";
    private final ColorDirectViewDump mDump = new ColorDirectViewDump();
    private final WeakReference<ViewRootImpl> mViewAncestor;

    public ColorDirectViewHelper(WeakReference<ViewRootImpl> viewAncestor) {
        super(viewAncestor);
        this.mViewAncestor = viewAncestor;
    }

    public void directFindCmd(ColorDirectFindCmd findCmd) {
        if (findCmd != null) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor == null || viewAncestor.mView == null) {
                ColorDirectUtils.onFindFailed(findCmd.getCallback(), "no_view");
            } else {
                this.mDump.findCmd(viewAncestor, findCmd);
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        ColorDirectFindCmd findCmd;
        if (code != 10008) {
            return false;
        }
        data.enforceInterface("android.view.IWindow");
        if (data.readInt() != 0) {
            findCmd = (ColorDirectFindCmd) ColorDirectFindCmd.CREATOR.createFromParcel(data);
        } else {
            findCmd = null;
        }
        directFindCmd(findCmd);
        return true;
    }
}
