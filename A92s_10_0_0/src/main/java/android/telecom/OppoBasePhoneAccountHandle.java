package android.telecom;

import android.os.Parcel;

public class OppoBasePhoneAccountHandle {
    protected int mSlotId = -1;
    protected int mSubId = -1;

    /* access modifiers changed from: protected */
    public void initSubAndSlotId(int subId, int slotId) {
        this.mSubId = subId;
        this.mSlotId = slotId;
    }

    public int getSubId() {
        return this.mSubId;
    }

    public int getSlotId() {
        return this.mSlotId;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mSubId);
        out.writeInt(this.mSlotId);
    }

    public void readFromParcel(Parcel in) {
        this.mSubId = in.readInt();
        this.mSlotId = in.readInt();
    }
}
