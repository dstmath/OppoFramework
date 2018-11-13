package vendor.qti.hardware.fingerprint.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class BiasTestResult {
    public int bias;
    public int median;
    public int q5;
    public int q95;
    public int status;
    public int stddev;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != BiasTestResult.class) {
            return false;
        }
        BiasTestResult other = (BiasTestResult) otherObject;
        return this.status == other.status && this.bias == other.bias && this.median == other.median && this.stddev == other.stddev && this.q5 == other.q5 && this.q95 == other.q95;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bias))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.median))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.stddev))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.q5))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.q95)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".status = ");
        builder.append(Status.toString(this.status));
        builder.append(", .bias = ");
        builder.append(this.bias);
        builder.append(", .median = ");
        builder.append(this.median);
        builder.append(", .stddev = ");
        builder.append(this.stddev);
        builder.append(", .q5 = ");
        builder.append(this.q5);
        builder.append(", .q95 = ");
        builder.append(this.q95);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<BiasTestResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<BiasTestResult> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            BiasTestResult _hidl_vec_element = new BiasTestResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.status = _hidl_blob.getInt32(0 + _hidl_offset);
        this.bias = _hidl_blob.getInt32(4 + _hidl_offset);
        this.median = _hidl_blob.getInt32(8 + _hidl_offset);
        this.stddev = _hidl_blob.getInt32(12 + _hidl_offset);
        this.q5 = _hidl_blob.getInt32(16 + _hidl_offset);
        this.q95 = _hidl_blob.getInt32(20 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<BiasTestResult> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((BiasTestResult) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.status);
        _hidl_blob.putInt32(4 + _hidl_offset, this.bias);
        _hidl_blob.putInt32(8 + _hidl_offset, this.median);
        _hidl_blob.putInt32(12 + _hidl_offset, this.stddev);
        _hidl_blob.putInt32(16 + _hidl_offset, this.q5);
        _hidl_blob.putInt32(20 + _hidl_offset, this.q95);
    }
}
