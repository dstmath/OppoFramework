package vendor.mediatek.hardware.radio_op.V2_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RsuResponseInfo {
    public String data = new String();
    public int errCode;
    public int opId;
    public int requestId;
    public int reserveInt1;
    public int reserveInt2;
    public String reserveString1 = new String();
    public int status;
    public long time;
    public int version;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RsuResponseInfo.class) {
            return false;
        }
        RsuResponseInfo other = (RsuResponseInfo) otherObject;
        if (this.opId == other.opId && this.requestId == other.requestId && this.errCode == other.errCode && HidlSupport.deepEquals(this.data, other.data) && this.time == other.time && this.version == other.version && this.status == other.status && this.reserveInt1 == other.reserveInt1 && this.reserveInt2 == other.reserveInt2 && HidlSupport.deepEquals(this.reserveString1, other.reserveString1)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.opId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.requestId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.errCode))), Integer.valueOf(HidlSupport.deepHashCode(this.data)), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.time))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.version))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reserveInt1))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reserveInt2))), Integer.valueOf(HidlSupport.deepHashCode(this.reserveString1)));
    }

    public final String toString() {
        return "{" + ".opId = " + this.opId + ", .requestId = " + this.requestId + ", .errCode = " + this.errCode + ", .data = " + this.data + ", .time = " + this.time + ", .version = " + this.version + ", .status = " + this.status + ", .reserveInt1 = " + this.reserveInt1 + ", .reserveInt2 = " + this.reserveInt2 + ", .reserveString1 = " + this.reserveString1 + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(72), 0);
    }

    public static final ArrayList<RsuResponseInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RsuResponseInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RsuResponseInfo _hidl_vec_element = new RsuResponseInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.opId = _hidl_blob.getInt32(_hidl_offset + 0);
        this.requestId = _hidl_blob.getInt32(_hidl_offset + 4);
        this.errCode = _hidl_blob.getInt32(_hidl_offset + 8);
        this.data = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.data.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.time = _hidl_blob.getInt64(_hidl_offset + 32);
        this.version = _hidl_blob.getInt32(_hidl_offset + 40);
        this.status = _hidl_blob.getInt32(_hidl_offset + 44);
        this.reserveInt1 = _hidl_blob.getInt32(_hidl_offset + 48);
        this.reserveInt2 = _hidl_blob.getInt32(_hidl_offset + 52);
        this.reserveString1 = _hidl_blob.getString(_hidl_offset + 56);
        parcel.readEmbeddedBuffer((long) (this.reserveString1.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 56 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(72);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RsuResponseInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 72);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 72));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.opId);
        _hidl_blob.putInt32(4 + _hidl_offset, this.requestId);
        _hidl_blob.putInt32(8 + _hidl_offset, this.errCode);
        _hidl_blob.putString(16 + _hidl_offset, this.data);
        _hidl_blob.putInt64(32 + _hidl_offset, this.time);
        _hidl_blob.putInt32(40 + _hidl_offset, this.version);
        _hidl_blob.putInt32(44 + _hidl_offset, this.status);
        _hidl_blob.putInt32(48 + _hidl_offset, this.reserveInt1);
        _hidl_blob.putInt32(52 + _hidl_offset, this.reserveInt2);
        _hidl_blob.putString(56 + _hidl_offset, this.reserveString1);
    }
}
