package vendor.qti.hardware.radio.qtiradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class Oem_key_log_err_msg_type {
    public short errcode;
    public short is_message;
    public String msg = new String();
    public short rat;
    public short type;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != Oem_key_log_err_msg_type.class) {
            return false;
        }
        Oem_key_log_err_msg_type other = (Oem_key_log_err_msg_type) otherObject;
        return this.type == other.type && this.rat == other.rat && this.errcode == other.errcode && this.is_message == other.is_message && HidlSupport.deepEquals(this.msg, other.msg);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.rat))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.errcode))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.is_message))), Integer.valueOf(HidlSupport.deepHashCode(this.msg))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".type = ");
        builder.append(this.type);
        builder.append(", .rat = ");
        builder.append(this.rat);
        builder.append(", .errcode = ");
        builder.append(this.errcode);
        builder.append(", .is_message = ");
        builder.append(this.is_message);
        builder.append(", .msg = ");
        builder.append(this.msg);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<Oem_key_log_err_msg_type> readVectorFromParcel(HwParcel parcel) {
        ArrayList<Oem_key_log_err_msg_type> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            Oem_key_log_err_msg_type _hidl_vec_element = new Oem_key_log_err_msg_type();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.type = _hidl_blob.getInt16(_hidl_offset + 0);
        this.rat = _hidl_blob.getInt16(2 + _hidl_offset);
        this.errcode = _hidl_blob.getInt16(4 + _hidl_offset);
        this.is_message = _hidl_blob.getInt16(6 + _hidl_offset);
        this.msg = _hidl_blob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.msg.getBytes().length + 1), _hidl_blob.handle(), 0 + (_hidl_offset + 8), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<Oem_key_log_err_msg_type> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((Oem_key_log_err_msg_type) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt16(0 + _hidl_offset, this.type);
        _hidl_blob.putInt16(2 + _hidl_offset, this.rat);
        _hidl_blob.putInt16(4 + _hidl_offset, this.errcode);
        _hidl_blob.putInt16(6 + _hidl_offset, this.is_message);
        _hidl_blob.putString(8 + _hidl_offset, this.msg);
    }
}
