package android.os;

import android.content.Context;
import com.color.util.ColorBaseServiceManager;

public abstract class ColorBaseIPowerManager extends ColorBaseServiceManager {
    public ColorBaseIPowerManager() {
        super(Context.POWER_SERVICE);
    }

    /* access modifiers changed from: protected */
    @Override // com.color.util.ColorBaseServiceManager
    public void init(IBinder remote) {
    }
}
