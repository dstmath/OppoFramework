package android.content.pm;

import android.os.Parcel;
import android.util.Printer;

public abstract class OppoBaseActivityInfo extends ComponentInfo {
    public static final int COLOR_FLAG_NEED_INTERCEPT = 1;
    public int colorFlags;
    public boolean hasResizeModeInit = false;
    public int resizeModeOriginal = 2;

    public OppoBaseActivityInfo() {
    }

    public OppoBaseActivityInfo(OppoBaseActivityInfo orig) {
        super(orig);
        this.colorFlags = orig.colorFlags;
    }

    /* access modifiers changed from: protected */
    @Override // android.content.pm.ComponentInfo, android.content.pm.PackageItemInfo
    public void dumpFront(Printer pw, String prefix) {
        super.dumpFront(pw, prefix);
        if (this.colorFlags != 0) {
            pw.println(prefix + "colorFlags=0x" + Integer.toHexString(this.colorFlags));
        }
    }

    @Override // android.content.pm.ComponentInfo, android.content.pm.PackageItemInfo
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeInt(this.colorFlags);
    }

    protected OppoBaseActivityInfo(Parcel source) {
        super(source);
        this.colorFlags = source.readInt();
    }

    public boolean needIntercept() {
        return (this.colorFlags & 1) != 0;
    }
}
