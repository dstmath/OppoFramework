package android.hardware.face;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class FaceRusNativeData implements Parcelable {
    public static final Creator<FaceRusNativeData> CREATOR = new Creator<FaceRusNativeData>() {
        public FaceRusNativeData createFromParcel(Parcel in) {
            return new FaceRusNativeData(in);
        }

        public FaceRusNativeData[] newArray(int size) {
            return new FaceRusNativeData[size];
        }
    };
    private static final String TAG = "FaceRusNativeData";
    public float mHacknessThreshold;

    public FaceRusNativeData(float hacknessThreshold) {
        this.mHacknessThreshold = hacknessThreshold;
    }

    public FaceRusNativeData(Parcel in) {
        this.mHacknessThreshold = in.readFloat();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.mHacknessThreshold);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "hacknessThreshold: " + this.mHacknessThreshold;
    }
}
