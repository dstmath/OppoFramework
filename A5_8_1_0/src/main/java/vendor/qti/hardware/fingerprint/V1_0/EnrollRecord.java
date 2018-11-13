package vendor.qti.hardware.fingerprint.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class EnrollRecord {
    public int dbStatus;
    public String enrolleeId = new String();
    public long enrollmentDate;
    public final ArrayList<String> fingers = new ArrayList();
    public int status;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != EnrollRecord.class) {
            return false;
        }
        EnrollRecord other = (EnrollRecord) otherObject;
        return this.status == other.status && HidlSupport.deepEquals(this.enrolleeId, other.enrolleeId) && this.enrollmentDate == other.enrollmentDate && HidlSupport.deepEquals(this.fingers, other.fingers) && this.dbStatus == other.dbStatus;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(this.enrolleeId)), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.enrollmentDate))), Integer.valueOf(HidlSupport.deepHashCode(this.fingers)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.dbStatus)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".status = ");
        builder.append(Status.toString(this.status));
        builder.append(", .enrolleeId = ");
        builder.append(this.enrolleeId);
        builder.append(", .enrollmentDate = ");
        builder.append(this.enrollmentDate);
        builder.append(", .fingers = ");
        builder.append(this.fingers);
        builder.append(", .dbStatus = ");
        builder.append(this.dbStatus);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<EnrollRecord> readVectorFromParcel(HwParcel parcel) {
        ArrayList<EnrollRecord> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            EnrollRecord _hidl_vec_element = new EnrollRecord();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.status = _hidl_blob.getInt32(0 + _hidl_offset);
        this.enrolleeId = _hidl_blob.getString(8 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.enrolleeId.getBytes().length + 1), _hidl_blob.handle(), (8 + _hidl_offset) + 0, false);
        this.enrollmentDate = _hidl_blob.getInt64(24 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((32 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), (32 + _hidl_offset) + 0, true);
        this.fingers.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            String _hidl_vec_element = new String();
            _hidl_vec_element = childBlob.getString((long) (_hidl_index_0 * 16));
            HwParcel hwParcel = parcel;
            hwParcel.readEmbeddedBuffer((long) (_hidl_vec_element.getBytes().length + 1), childBlob.handle(), (long) ((_hidl_index_0 * 16) + 0), false);
            this.fingers.add(_hidl_vec_element);
        }
        this.dbStatus = _hidl_blob.getInt32(48 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<EnrollRecord> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((EnrollRecord) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.status);
        _hidl_blob.putString(8 + _hidl_offset, this.enrolleeId);
        _hidl_blob.putInt64(24 + _hidl_offset, this.enrollmentDate);
        int _hidl_vec_size = this.fingers.size();
        _hidl_blob.putInt32((32 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((32 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putString((long) (_hidl_index_0 * 16), (String) this.fingers.get(_hidl_index_0));
        }
        _hidl_blob.putBlob((32 + _hidl_offset) + 0, childBlob);
        _hidl_blob.putInt32(48 + _hidl_offset, this.dbStatus);
    }
}
