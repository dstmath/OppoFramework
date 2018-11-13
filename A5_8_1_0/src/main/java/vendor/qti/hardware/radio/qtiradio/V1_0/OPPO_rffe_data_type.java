package vendor.qti.hardware.radio.qtiradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class OPPO_rffe_data_type {
    public short address;
    public byte channel;
    public byte data;
    public byte ext;
    public byte halfspeed;
    public byte readwrite;
    public byte slave;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != OPPO_rffe_data_type.class) {
            return false;
        }
        OPPO_rffe_data_type other = (OPPO_rffe_data_type) otherObject;
        return this.ext == other.ext && this.readwrite == other.readwrite && this.channel == other.channel && this.slave == other.slave && this.address == other.address && this.data == other.data && this.halfspeed == other.halfspeed;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.ext))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.readwrite))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.channel))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.slave))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.address))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.data))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.halfspeed)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".ext = ");
        builder.append(this.ext);
        builder.append(", .readwrite = ");
        builder.append(this.readwrite);
        builder.append(", .channel = ");
        builder.append(this.channel);
        builder.append(", .slave = ");
        builder.append(this.slave);
        builder.append(", .address = ");
        builder.append(this.address);
        builder.append(", .data = ");
        builder.append(this.data);
        builder.append(", .halfspeed = ");
        builder.append(this.halfspeed);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(8), 0);
    }

    public static final ArrayList<OPPO_rffe_data_type> readVectorFromParcel(HwParcel parcel) {
        ArrayList<OPPO_rffe_data_type> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 8), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            OPPO_rffe_data_type _hidl_vec_element = new OPPO_rffe_data_type();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 8));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.ext = _hidl_blob.getInt8(0 + _hidl_offset);
        this.readwrite = _hidl_blob.getInt8(1 + _hidl_offset);
        this.channel = _hidl_blob.getInt8(2 + _hidl_offset);
        this.slave = _hidl_blob.getInt8(3 + _hidl_offset);
        this.address = _hidl_blob.getInt16(4 + _hidl_offset);
        this.data = _hidl_blob.getInt8(6 + _hidl_offset);
        this.halfspeed = _hidl_blob.getInt8(7 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(8);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<OPPO_rffe_data_type> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 8);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((OPPO_rffe_data_type) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 8));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(0 + _hidl_offset, this.ext);
        _hidl_blob.putInt8(1 + _hidl_offset, this.readwrite);
        _hidl_blob.putInt8(2 + _hidl_offset, this.channel);
        _hidl_blob.putInt8(3 + _hidl_offset, this.slave);
        _hidl_blob.putInt16(4 + _hidl_offset, this.address);
        _hidl_blob.putInt8(6 + _hidl_offset, this.data);
        _hidl_blob.putInt8(7 + _hidl_offset, this.halfspeed);
    }
}
