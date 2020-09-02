package mediatek.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.Rlog;

public class MtkSimSmsInsertStatus implements Parcelable {
    public static final Parcelable.Creator<MtkSimSmsInsertStatus> CREATOR = new Parcelable.Creator<MtkSimSmsInsertStatus>() {
        /* class mediatek.telephony.MtkSimSmsInsertStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkSimSmsInsertStatus createFromParcel(Parcel source) {
            return new MtkSimSmsInsertStatus(source.readInt(), source.readString());
        }

        @Override // android.os.Parcelable.Creator
        public MtkSimSmsInsertStatus[] newArray(int size) {
            return new MtkSimSmsInsertStatus[size];
        }
    };
    private static final String TAG = "MtkSimSmsInsertStatus";
    public String indexInIcc = null;
    public int insertStatus = 0;

    public MtkSimSmsInsertStatus(int status, String index) {
        this.insertStatus = status;
        this.indexInIcc = index;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.insertStatus);
        dest.writeString(this.indexInIcc);
    }

    public int[] getIndex() {
        String str = this.indexInIcc;
        if (str == null) {
            return null;
        }
        String[] temp = str.split(",");
        if (temp == null || temp.length <= 0) {
            Rlog.d(TAG, "should not arrive here");
            return null;
        }
        int[] ret = new int[temp.length];
        for (int i = 0; i < ret.length; i++) {
            try {
                ret[i] = Integer.parseInt(temp[i]);
                Rlog.d(TAG, "index is " + ret[i]);
            } catch (NumberFormatException e) {
                Rlog.d("TAG", "fail to parse index");
                ret[i] = -1;
            }
        }
        return ret;
    }
}
