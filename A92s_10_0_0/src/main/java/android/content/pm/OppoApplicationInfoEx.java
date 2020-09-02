package android.content.pm;

import android.os.Parcel;
import android.util.Log;
import android.util.proto.ProtoOutputStream;

public class OppoApplicationInfoEx {
    public static final int OEM_PRIVATE_FLAG_SIGNED_WITH_OPPO_KEY = 1;
    public static final int OPPO_PRIVATE_FLAG_DEX2OAT_ROLLBACK = 1;
    public static final int OPPO_PRIVATE_FLAG_IGNORE_OPENNDK = 4;
    public static final int OPPO_PRIVATE_FLAG_IGNORE_TOAST = 2;
    public static final int PRIVATE_FLAG_ENABLE_DISPLAY_COMPAT = Integer.MIN_VALUE;
    private float appInvscale = 1.0f;
    public float appscale = 1.0f;
    private int compatDensity = 0;
    private OverrideDensityChangedListener mChangingListener = null;
    public float newappscale = 1.0f;
    public int oemPrivateFlags;
    public int oppoFreezeState;
    private int overrideDensity = 0;

    public interface OverrideDensityChangedListener {
        void onOverrideDensityChanged(int i);
    }

    public void setAppScale(float scale) {
        this.appscale = scale;
    }

    public float getAppScale() {
        return this.appscale;
    }

    public void setNewAppScale(float scale) {
        this.newappscale = scale;
    }

    public float getNewAppScale() {
        return this.newappscale;
    }

    public void setAppInvScale(float scale) {
        this.appInvscale = scale;
    }

    public float getAppInvScale() {
        return this.appInvscale;
    }

    public boolean isSignedWithOppoKey() {
        return (this.oemPrivateFlags & 1) != 0;
    }

    public void setOverrideDensityChangedListener(OverrideDensityChangedListener listener) {
        this.mChangingListener = listener;
    }

    public int getOverrideDensity() {
        return this.overrideDensity;
    }

    public void setOverrideDensity(int newValue) {
        this.overrideDensity = newValue;
        OverrideDensityChangedListener overrideDensityChangedListener = this.mChangingListener;
        if (overrideDensityChangedListener != null) {
            overrideDensityChangedListener.onOverrideDensityChanged(newValue);
        }
    }

    public int getCompatDensity() {
        return this.compatDensity;
    }

    public void setCompatDensity(int newValue) {
        this.compatDensity = newValue;
    }

    public OppoApplicationInfoEx() {
    }

    public OppoApplicationInfoEx(OppoApplicationInfoEx orig) {
        this.appscale = orig.appscale;
        this.newappscale = orig.newappscale;
        this.oppoFreezeState = orig.oppoFreezeState;
        this.overrideDensity = orig.overrideDensity;
        this.appInvscale = orig.appInvscale;
        this.compatDensity = orig.compatDensity;
    }

    protected OppoApplicationInfoEx(Parcel source) {
        this.appscale = source.readFloat();
        this.newappscale = source.readFloat();
        this.oppoFreezeState = source.readInt();
        this.overrideDensity = source.readInt();
        this.appInvscale = source.readFloat();
        this.compatDensity = source.readInt();
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeFloat(this.appscale);
        dest.writeFloat(this.newappscale);
        dest.writeInt(this.oppoFreezeState);
        dest.writeInt(this.overrideDensity);
        dest.writeFloat(this.appInvscale);
        dest.writeInt(this.compatDensity);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, int dumpFlags) {
    }

    public static OppoApplicationInfoEx getOppoAppInfoExFromAppInfoRef(ApplicationInfo appInfo) {
        if (appInfo == null || OppoMirrorApplicationInfo.mOppoApplicationInfoEx == null) {
            return null;
        }
        try {
            return OppoMirrorApplicationInfo.mOppoApplicationInfoEx.get(appInfo);
        } catch (Exception e) {
            Log.e("OppoApplicationInfoEx", "getOppoAppInfoExFromAppInfoRef failed!", e);
            return null;
        }
    }
}
