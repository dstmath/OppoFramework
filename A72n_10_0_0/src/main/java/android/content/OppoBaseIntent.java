package android.content;

import android.os.Parcel;

public class OppoBaseIntent {
    public static final int FLAG_RECEIVER_OPPOQUEUE = 524288;
    public static final int FLAG_RECEIVER_QUEUE_PRIOR = 1048576;
    public static final int OPPO_FLAG_ACTIVITY_KEEP_RESUM_WHEN_SLEEPING = 1073741824;
    public static final int OPPO_FLAG_ACTIVITY_SECURE_POLICY = Integer.MIN_VALUE;
    public static final int OPPO_FLAG_MULTI_APP_SKIP_CHOOSER = 2048;
    public static final int OPPO_FLAG_MUTIL_APP = 1024;
    public static final int OPPO_FLAG_MUTIL_CHOOSER = 512;
    public static final int OPPO_FLAG_RECEIVER_OPPOQUEUE = 2;
    public static final int OPPO_FLAG_RECEIVER_QUEUE_PRIOR = 1;
    int mCallingUid = -1;
    int mIsForFreeForm = 0;
    int mIsFromGameSpace = 0;
    int mOppoFlags;
    int mOppoUserId = 0;
    int mPid = -1;
    int mStackId = 0;
    int mUid = -1;

    OppoBaseIntent() {
    }

    OppoBaseIntent(OppoBaseIntent o, int copyMode) {
        this.mOppoUserId = o.mOppoUserId;
        this.mIsFromGameSpace = o.mIsFromGameSpace;
        this.mIsForFreeForm = o.mIsForFreeForm;
        this.mStackId = o.mStackId;
        this.mOppoFlags = o.mOppoFlags;
        this.mCallingUid = o.mCallingUid;
    }

    public int fillIn(OppoBaseIntent other, int flags) {
        this.mOppoUserId = other.mOppoUserId;
        this.mIsFromGameSpace = other.mIsFromGameSpace;
        this.mIsForFreeForm = other.mIsForFreeForm;
        this.mStackId = other.mStackId;
        this.mOppoFlags |= other.mOppoFlags;
        this.mCallingUid = other.mCallingUid;
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mOppoUserId);
        out.writeInt(this.mIsFromGameSpace);
        out.writeInt(this.mIsForFreeForm);
        out.writeInt(this.mStackId);
        out.writeInt(this.mOppoFlags);
        out.writeInt(this.mCallingUid);
    }

    public void readFromParcel(Parcel in) {
        this.mOppoUserId = in.readInt();
        this.mIsFromGameSpace = in.readInt();
        this.mIsForFreeForm = in.readInt();
        this.mStackId = in.readInt();
        this.mOppoFlags = in.readInt();
        this.mCallingUid = in.readInt();
    }

    public int getOppoUserId() {
        return this.mOppoUserId;
    }

    public void setOppoUserId(int oppoUserId) {
        this.mOppoUserId = oppoUserId;
    }

    public int getIsFromGameSpace() {
        return this.mIsFromGameSpace;
    }

    public void setIsFromGameSpace(int isFromGameSpace) {
        this.mIsFromGameSpace = isFromGameSpace;
    }

    public int getIsForFreeForm() {
        return this.mIsForFreeForm;
    }

    public void setIsForFreeForm(int isForFreeForm) {
        this.mIsForFreeForm = isForFreeForm;
    }

    public int getLaunchStackId() {
        return this.mStackId;
    }

    public void setLaunchStackId(int stackId) {
        this.mStackId = stackId;
    }

    public int getOppoFlags() {
        return this.mOppoFlags;
    }

    public void setOppoFlags(int oppoFlags) {
        this.mOppoFlags = oppoFlags;
    }

    public void addOppoFlags(int oppoFlags) {
        this.mOppoFlags |= oppoFlags;
    }

    public void removeOppoFlags(int oppoFlags) {
        this.mOppoFlags &= ~oppoFlags;
    }

    public void setPid(int pid) {
        this.mPid = pid;
    }

    public int getPid() {
        return this.mPid;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setCallingUid(int uid) {
        this.mCallingUid = uid;
    }

    public int getCallingUid() {
        return this.mCallingUid;
    }

    public String toString() {
        StringBuilder b = new StringBuilder(128);
        if (this.mOppoFlags != 0) {
            b.append(" oflg=0x");
            b.append(Integer.toHexString(this.mOppoFlags));
        }
        if (this.mOppoUserId != 0) {
            b.append(" ouserid=");
            b.append(this.mOppoUserId);
        }
        if (this.mIsForFreeForm != 0) {
            b.append(" freeform=");
            b.append(this.mIsForFreeForm);
        }
        if (this.mIsFromGameSpace != 0) {
            b.append(" gs=");
            b.append(this.mIsFromGameSpace);
        }
        if (this.mStackId != 0) {
            b.append(" stackid=");
            b.append(this.mStackId);
        }
        if (this.mCallingUid != -1) {
            b.append(" mCallingUid=");
            b.append(this.mCallingUid);
        }
        return b.toString();
    }
}
