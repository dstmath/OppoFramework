package mediatek.telephony;

import android.os.Parcel;
import android.os.Parcelable;

public class MtkSmsParameters implements Parcelable {
    public static final Parcelable.Creator<MtkSmsParameters> CREATOR = new Parcelable.Creator<MtkSmsParameters>() {
        /* class mediatek.telephony.MtkSmsParameters.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkSmsParameters createFromParcel(Parcel source) {
            return new MtkSmsParameters(source.readInt(), source.readInt(), source.readInt(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public MtkSmsParameters[] newArray(int size) {
            return new MtkSmsParameters[size];
        }
    };
    public int dcs;
    public int format;
    public int pid;
    public int vp;

    public MtkSmsParameters(int format2, int vp2, int pid2, int dcs2) {
        this.format = format2;
        this.vp = vp2;
        this.pid = pid2;
        this.dcs = dcs2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.format);
        dest.writeInt(this.vp);
        dest.writeInt(this.pid);
        dest.writeInt(this.dcs);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        sb.append("[");
        sb.append(this.format);
        sb.append(", ");
        sb.append(this.vp);
        sb.append(", ");
        sb.append(this.pid);
        sb.append(", ");
        sb.append(this.dcs);
        sb.append("]");
        return sb.toString();
    }
}
