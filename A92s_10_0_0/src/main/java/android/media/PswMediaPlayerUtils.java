package android.media;

import android.app.ActivityThread;
import android.content.Context;
import android.os.Parcel;
import android.os.Process;
import android.util.Log;
import java.lang.reflect.Method;

public class PswMediaPlayerUtils implements IPswMediaPlayerUtils {
    private static final String ATLAS_KEY_AUDIO_CHECK_LISTINFO_BYNAME = "check_listinfo_byname";
    private static final String ATLAS_KEY_AUDIO_GET_LISTINFO_BYNAME = "get_listinfo_byname";
    public static final int MUTE_FLAG = 1;
    private static final String SYSTEM_NOTIFICATION_AUDIO_PATH = "/system/media/audio/notifications/";
    private static final String TAG = "PswMediaPlayerUtils";
    public static final int UNMUTE_FLAG = 0;
    private Method mAtlasGetParameters;
    private Object mAtlasInstance;
    private boolean mInterceptFlag = false;
    private boolean mNeedMute = false;
    private Object mNotificationManager;
    private boolean mRecoverFlag = false;
    private int mStreamType = Integer.MIN_VALUE;
    private Method mshouldInterceptSound;

    public PswMediaPlayerUtils() {
        Log.d(TAG, "new PswMediaPlayerUtils");
        try {
            Class atlasManager = Class.forName("com.oppo.atlas.OppoAtlasManager");
            Method methodGetInstance = atlasManager.getMethod("getInstance", Context.class);
            this.mAtlasGetParameters = atlasManager.getMethod("getParameters", String.class);
            this.mAtlasInstance = methodGetInstance.invoke(new Object(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class clazz = Class.forName("android.app.ColorNotificationManager");
            this.mshouldInterceptSound = clazz.getMethod("shouldInterceptSound", String.class, Integer.TYPE);
            this.mNotificationManager = clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
            this.mshouldInterceptSound.setAccessible(true);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public Parcel checkZenMode() {
        Method method;
        String packageName = ActivityThread.currentPackageName();
        if (packageName == null || packageName.length() <= 0) {
            return null;
        }
        String result = null;
        try {
            result = (String) this.mAtlasGetParameters.invoke(this.mAtlasInstance, "check_listinfo_byname=zenmode=" + packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == null || !result.equals("true")) {
            return null;
        }
        Log.i(TAG, "The package name is " + packageName + "qq & wecha zenmode control !");
        int uid = Process.myUid();
        Object obj = this.mNotificationManager;
        if (!(obj == null || (method = this.mshouldInterceptSound) == null)) {
            try {
                this.mInterceptFlag = ((Boolean) method.invoke(obj, packageName, Integer.valueOf(uid))).booleanValue();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        Parcel pMute = Parcel.obtain();
        pMute.writeInt(this.mInterceptFlag ? 1 : 0);
        return pMute;
    }

    public Parcel checkWechatMute() {
        String packageName = ActivityThread.currentPackageName();
        if (packageName == null || packageName.length() <= 0) {
            return null;
        }
        String result = null;
        try {
            result = (String) this.mAtlasGetParameters.invoke(this.mAtlasInstance, "get_listinfo_byname=zenmode=" + packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result != null && result.equals("wechat_mute")) {
            Log.i(TAG, "The package name is " + packageName + "wecha zenmode control !");
            int i = this.mStreamType;
            if (i == 5 || i == 2) {
                Parcel pMute = Parcel.obtain();
                pMute.writeInt(this.mInterceptFlag ? 1 : 0);
                return pMute;
            }
        }
        return null;
    }

    public void resetZenModeFlag() {
        this.mInterceptFlag = false;
    }

    public void setAudioStreamType(int type) {
        this.mStreamType = type;
    }
}
