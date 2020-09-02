package vendor.oppo.hardware.biometrics.face.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class FaceInfoWrapper {
    public int ID;
    public float eye_dist;
    public float pitch;
    public float roll;
    public float score;
    public float yaw;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != FaceInfoWrapper.class) {
            return false;
        }
        FaceInfoWrapper other = (FaceInfoWrapper) otherObject;
        if (this.score == other.score && this.yaw == other.yaw && this.pitch == other.pitch && this.roll == other.roll && this.eye_dist == other.eye_dist && this.ID == other.ID) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.score))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.yaw))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.pitch))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.roll))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.eye_dist))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ID))));
    }

    public final String toString() {
        return "{" + ".score = " + this.score + ", .yaw = " + this.yaw + ", .pitch = " + this.pitch + ", .roll = " + this.roll + ", .eye_dist = " + this.eye_dist + ", .ID = " + this.ID + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<FaceInfoWrapper> readVectorFromParcel(HwParcel parcel) {
        ArrayList<FaceInfoWrapper> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            FaceInfoWrapper _hidl_vec_element = new FaceInfoWrapper();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.score = _hidl_blob.getFloat(0 + _hidl_offset);
        this.yaw = _hidl_blob.getFloat(4 + _hidl_offset);
        this.pitch = _hidl_blob.getFloat(8 + _hidl_offset);
        this.roll = _hidl_blob.getFloat(12 + _hidl_offset);
        this.eye_dist = _hidl_blob.getFloat(16 + _hidl_offset);
        this.ID = _hidl_blob.getInt32(20 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<FaceInfoWrapper> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putFloat(0 + _hidl_offset, this.score);
        _hidl_blob.putFloat(4 + _hidl_offset, this.yaw);
        _hidl_blob.putFloat(8 + _hidl_offset, this.pitch);
        _hidl_blob.putFloat(12 + _hidl_offset, this.roll);
        _hidl_blob.putFloat(16 + _hidl_offset, this.eye_dist);
        _hidl_blob.putInt32(20 + _hidl_offset, this.ID);
    }
}
