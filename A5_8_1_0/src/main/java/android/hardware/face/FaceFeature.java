package android.hardware.face;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class FaceFeature implements Parcelable {
    public static final Creator<FaceFeature> CREATOR = new Creator<FaceFeature>() {
        public FaceFeature createFromParcel(Parcel in) {
            return new FaceFeature(in, null);
        }

        public FaceFeature[] newArray(int size) {
            return new FaceFeature[size];
        }
    };
    private long mDeviceId;
    private int mFaceFeatureId;
    private int mGroupId;
    private CharSequence mName;

    /* synthetic */ FaceFeature(Parcel in, FaceFeature -this1) {
        this(in);
    }

    public FaceFeature(CharSequence name, int groupId, int faceFeatureId, long deviceId) {
        this.mName = name;
        this.mGroupId = groupId;
        this.mFaceFeatureId = faceFeatureId;
        this.mDeviceId = deviceId;
    }

    private FaceFeature(Parcel in) {
        this.mName = in.readString();
        this.mGroupId = in.readInt();
        this.mFaceFeatureId = in.readInt();
        this.mDeviceId = in.readLong();
    }

    public CharSequence getName() {
        return this.mName;
    }

    public int getFaceFeatureId() {
        return this.mFaceFeatureId;
    }

    public int getGroupId() {
        return this.mGroupId;
    }

    public long getDeviceId() {
        return this.mDeviceId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mName.toString());
        out.writeInt(this.mGroupId);
        out.writeInt(this.mFaceFeatureId);
        out.writeLong(this.mDeviceId);
    }

    public String toString() {
        return "FaceFeature: [mName = " + this.mName + ", mFaceFeatureId = " + this.mFaceFeatureId + ", mGroupId =" + this.mGroupId + ", mDeviceId = " + this.mDeviceId + "]";
    }
}
