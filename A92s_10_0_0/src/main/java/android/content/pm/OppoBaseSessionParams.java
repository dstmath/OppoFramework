package android.content.pm;

import android.os.Parcel;

public class OppoBaseSessionParams {
    public int extraInstallFlags;
    public String extraSessionInfo;

    public void initFromParcel(Parcel source) {
        this.extraInstallFlags = source.readInt();
        this.extraSessionInfo = source.readString();
    }

    public void setExtraSessionInfo(String extraSessionInfo2) {
        this.extraSessionInfo = extraSessionInfo2;
    }

    public void setExtraInstallFlags(int installFlags) {
        this.extraInstallFlags |= installFlags;
    }

    public void baseWriteToParcel(Parcel dest) {
        dest.writeInt(this.extraInstallFlags);
        dest.writeString(this.extraSessionInfo);
    }
}
