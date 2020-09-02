package android.hardware.face;

import android.os.Parcel;
import android.os.Parcelable;

public class FaceRusNativeData implements Parcelable {
    public static final Parcelable.Creator<FaceRusNativeData> CREATOR = new Parcelable.Creator<FaceRusNativeData>() {
        /* class android.hardware.face.FaceRusNativeData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FaceRusNativeData createFromParcel(Parcel in) {
            return new FaceRusNativeData(in);
        }

        @Override // android.os.Parcelable.Creator
        public FaceRusNativeData[] newArray(int size) {
            return new FaceRusNativeData[size];
        }
    };
    private static final String TAG = "FaceRusNativeData";
    public float mHacknessThreshold;

    public FaceRusNativeData() {
    }

    public FaceRusNativeData(float hacknessThreshold) {
        this.mHacknessThreshold = hacknessThreshold;
    }

    public FaceRusNativeData(Parcel in) {
        this.mHacknessThreshold = in.readFloat();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.mHacknessThreshold);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "hacknessThreshold: " + this.mHacknessThreshold;
    }
}
