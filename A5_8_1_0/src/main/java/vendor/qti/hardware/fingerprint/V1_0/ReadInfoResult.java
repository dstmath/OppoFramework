package vendor.qti.hardware.fingerprint.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class ReadInfoResult {
    public int chipId;
    public int chipSn;
    public int fwVersion;
    public byte livenessSupport;
    public final ArrayList<Byte> lotId = new ArrayList();
    public int sensorHeight;
    public int sensorWidth;
    public int status;
    public final ArrayList<Byte> tftId = new ArrayList();
    public final ArrayList<Byte> waferId = new ArrayList();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != ReadInfoResult.class) {
            return false;
        }
        ReadInfoResult other = (ReadInfoResult) otherObject;
        return this.status == other.status && this.fwVersion == other.fwVersion && this.chipId == other.chipId && this.chipSn == other.chipSn && HidlSupport.deepEquals(this.lotId, other.lotId) && HidlSupport.deepEquals(this.waferId, other.waferId) && this.sensorWidth == other.sensorWidth && this.sensorHeight == other.sensorHeight && this.livenessSupport == other.livenessSupport && HidlSupport.deepEquals(this.tftId, other.tftId);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.fwVersion))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.chipId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.chipSn))), Integer.valueOf(HidlSupport.deepHashCode(this.lotId)), Integer.valueOf(HidlSupport.deepHashCode(this.waferId)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sensorWidth))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sensorHeight))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.livenessSupport))), Integer.valueOf(HidlSupport.deepHashCode(this.tftId))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".status = ");
        builder.append(Status.toString(this.status));
        builder.append(", .fwVersion = ");
        builder.append(this.fwVersion);
        builder.append(", .chipId = ");
        builder.append(this.chipId);
        builder.append(", .chipSn = ");
        builder.append(this.chipSn);
        builder.append(", .lotId = ");
        builder.append(this.lotId);
        builder.append(", .waferId = ");
        builder.append(this.waferId);
        builder.append(", .sensorWidth = ");
        builder.append(this.sensorWidth);
        builder.append(", .sensorHeight = ");
        builder.append(this.sensorHeight);
        builder.append(", .livenessSupport = ");
        builder.append(this.livenessSupport);
        builder.append(", .tftId = ");
        builder.append(this.tftId);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(80), 0);
    }

    public static final ArrayList<ReadInfoResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<ReadInfoResult> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 80), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ReadInfoResult _hidl_vec_element = new ReadInfoResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 80));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        this.status = _hidl_blob.getInt32(0 + _hidl_offset);
        this.fwVersion = _hidl_blob.getInt32(4 + _hidl_offset);
        this.chipId = _hidl_blob.getInt32(8 + _hidl_offset);
        this.chipSn = _hidl_blob.getInt32(12 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((16 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (16 + _hidl_offset) + 0, true);
        this.lotId.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.lotId.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        _hidl_vec_size = _hidl_blob.getInt32((32 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (32 + _hidl_offset) + 0, true);
        this.waferId.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.waferId.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        this.sensorWidth = _hidl_blob.getInt32(48 + _hidl_offset);
        this.sensorHeight = _hidl_blob.getInt32(52 + _hidl_offset);
        this.livenessSupport = _hidl_blob.getInt8(56 + _hidl_offset);
        _hidl_vec_size = _hidl_blob.getInt32((64 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (64 + _hidl_offset) + 0, true);
        this.tftId.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.tftId.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(80);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ReadInfoResult> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 80);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((ReadInfoResult) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 80));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt32(0 + _hidl_offset, this.status);
        _hidl_blob.putInt32(4 + _hidl_offset, this.fwVersion);
        _hidl_blob.putInt32(8 + _hidl_offset, this.chipId);
        _hidl_blob.putInt32(12 + _hidl_offset, this.chipSn);
        int _hidl_vec_size = this.lotId.size();
        _hidl_blob.putInt32((16 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((16 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.lotId.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((16 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.waferId.size();
        _hidl_blob.putInt32((32 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((32 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.waferId.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((32 + _hidl_offset) + 0, childBlob);
        _hidl_blob.putInt32(48 + _hidl_offset, this.sensorWidth);
        _hidl_blob.putInt32(52 + _hidl_offset, this.sensorHeight);
        _hidl_blob.putInt8(56 + _hidl_offset, this.livenessSupport);
        _hidl_vec_size = this.tftId.size();
        _hidl_blob.putInt32((64 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((64 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.tftId.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((64 + _hidl_offset) + 0, childBlob);
    }
}
