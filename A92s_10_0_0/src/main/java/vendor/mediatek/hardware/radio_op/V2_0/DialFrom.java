package vendor.mediatek.hardware.radio_op.V2_0;

import android.hardware.radio.V1_0.Clir;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class DialFrom {
    public String address = new String();
    public int clir;
    public String fromAddress = new String();
    public boolean isVideoCall;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != DialFrom.class) {
            return false;
        }
        DialFrom other = (DialFrom) otherObject;
        if (HidlSupport.deepEquals(this.address, other.address) && HidlSupport.deepEquals(this.fromAddress, other.fromAddress) && this.clir == other.clir && this.isVideoCall == other.isVideoCall) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.address)), Integer.valueOf(HidlSupport.deepHashCode(this.fromAddress)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.clir))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isVideoCall))));
    }

    public final String toString() {
        return "{" + ".address = " + this.address + ", .fromAddress = " + this.fromAddress + ", .clir = " + Clir.toString(this.clir) + ", .isVideoCall = " + this.isVideoCall + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<DialFrom> readVectorFromParcel(HwParcel parcel) {
        ArrayList<DialFrom> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            DialFrom _hidl_vec_element = new DialFrom();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.address = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.address.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.fromAddress = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.fromAddress.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.clir = _hidl_blob.getInt32(_hidl_offset + 32);
        this.isVideoCall = _hidl_blob.getBool(_hidl_offset + 36);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<DialFrom> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.address);
        _hidl_blob.putString(16 + _hidl_offset, this.fromAddress);
        _hidl_blob.putInt32(32 + _hidl_offset, this.clir);
        _hidl_blob.putBool(36 + _hidl_offset, this.isVideoCall);
    }
}
