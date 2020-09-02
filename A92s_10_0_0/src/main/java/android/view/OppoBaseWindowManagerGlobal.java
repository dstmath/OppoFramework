package android.view;

import android.common.OppoFeatureCache;
import android.os.IBinder;
import android.util.Log;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import java.lang.reflect.Field;

public class OppoBaseWindowManagerGlobal {
    public void OppoBaseWindowManagerGlobal() {
    }

    protected static void resetWindowSessionForAntiVirus(IWindowSession sWindowSession) {
        Field fieldMRemote;
        try {
            if (((IColorAntiVirusBehaviorManager) OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0])).checkNeedReplace("windowsession") && (fieldMRemote = sWindowSession.getClass().getDeclaredField("mRemote")) != null) {
                fieldMRemote.setAccessible(true);
                fieldMRemote.set(sWindowSession, ((IColorAntiVirusBehaviorManager) OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0])).getOrCreateFakeBinder((IBinder) fieldMRemote.get(sWindowSession), "windowsession"));
                fieldMRemote.setAccessible(false);
            }
        } catch (Exception et) {
            Log.e("WindowManagerGlobal", et.getMessage());
        }
    }
}
