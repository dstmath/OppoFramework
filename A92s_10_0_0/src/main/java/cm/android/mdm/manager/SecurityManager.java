package cm.android.mdm.manager;

import android.content.Context;
import android.graphics.Bitmap;
import cm.android.mdm.interfaces.ISecurityManager;
import cm.android.mdm.util.CustomizeServiceManager;

public class SecurityManager implements ISecurityManager {
    Context mContext;

    public SecurityManager(Context context) {
        this.mContext = context;
    }

    @Override // cm.android.mdm.interfaces.ISecurityManager
    public Bitmap captureScreen() {
        return CustomizeServiceManager.captureScreen();
    }
}
