package vendor.qti.hardware.radio.qtiradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class OPPO_RIL_Tx_Rx_info {
    public final OPPO_RIL_Rx_Chain_info rx_chain_0 = new OPPO_RIL_Rx_Chain_info();
    public byte rx_chain_0_valid;
    public final OPPO_RIL_Rx_Chain_info rx_chain_1 = new OPPO_RIL_Rx_Chain_info();
    public byte rx_chain_1_valid;
    public final OPPO_RIL_Rx_Chain_info rx_chain_2 = new OPPO_RIL_Rx_Chain_info();
    public byte rx_chain_2_valid;
    public final OPPO_RIL_Rx_Chain_info rx_chain_3 = new OPPO_RIL_Rx_Chain_info();
    public byte rx_chain_3_valid;
    public final OPPO_RIL_Tx_info tx = new OPPO_RIL_Tx_info();
    public byte tx_valid;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != OPPO_RIL_Tx_Rx_info.class) {
            return false;
        }
        OPPO_RIL_Tx_Rx_info other = (OPPO_RIL_Tx_Rx_info) otherObject;
        return this.rx_chain_0_valid == other.rx_chain_0_valid && HidlSupport.deepEquals(this.rx_chain_0, other.rx_chain_0) && this.rx_chain_1_valid == other.rx_chain_1_valid && HidlSupport.deepEquals(this.rx_chain_1, other.rx_chain_1) && this.rx_chain_2_valid == other.rx_chain_2_valid && HidlSupport.deepEquals(this.rx_chain_2, other.rx_chain_2) && this.rx_chain_3_valid == other.rx_chain_3_valid && HidlSupport.deepEquals(this.rx_chain_3, other.rx_chain_3) && this.tx_valid == other.tx_valid && HidlSupport.deepEquals(this.tx, other.tx);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rx_chain_0_valid))), Integer.valueOf(HidlSupport.deepHashCode(this.rx_chain_0)), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rx_chain_1_valid))), Integer.valueOf(HidlSupport.deepHashCode(this.rx_chain_1)), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rx_chain_2_valid))), Integer.valueOf(HidlSupport.deepHashCode(this.rx_chain_2)), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rx_chain_3_valid))), Integer.valueOf(HidlSupport.deepHashCode(this.rx_chain_3)), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.tx_valid))), Integer.valueOf(HidlSupport.deepHashCode(this.tx))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".rx_chain_0_valid = ");
        builder.append(this.rx_chain_0_valid);
        builder.append(", .rx_chain_0 = ");
        builder.append(this.rx_chain_0);
        builder.append(", .rx_chain_1_valid = ");
        builder.append(this.rx_chain_1_valid);
        builder.append(", .rx_chain_1 = ");
        builder.append(this.rx_chain_1);
        builder.append(", .rx_chain_2_valid = ");
        builder.append(this.rx_chain_2_valid);
        builder.append(", .rx_chain_2 = ");
        builder.append(this.rx_chain_2);
        builder.append(", .rx_chain_3_valid = ");
        builder.append(this.rx_chain_3_valid);
        builder.append(", .rx_chain_3 = ");
        builder.append(this.rx_chain_3);
        builder.append(", .tx_valid = ");
        builder.append(this.tx_valid);
        builder.append(", .tx = ");
        builder.append(this.tx);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(124), 0);
    }

    public static final ArrayList<OPPO_RIL_Tx_Rx_info> readVectorFromParcel(HwParcel parcel) {
        ArrayList<OPPO_RIL_Tx_Rx_info> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 124), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            OPPO_RIL_Tx_Rx_info _hidl_vec_element = new OPPO_RIL_Tx_Rx_info();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 124));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.rx_chain_0_valid = _hidl_blob.getInt8(0 + _hidl_offset);
        this.rx_chain_0.readEmbeddedFromParcel(parcel, _hidl_blob, 4 + _hidl_offset);
        this.rx_chain_1_valid = _hidl_blob.getInt8(28 + _hidl_offset);
        this.rx_chain_1.readEmbeddedFromParcel(parcel, _hidl_blob, 32 + _hidl_offset);
        this.rx_chain_2_valid = _hidl_blob.getInt8(56 + _hidl_offset);
        this.rx_chain_2.readEmbeddedFromParcel(parcel, _hidl_blob, 60 + _hidl_offset);
        this.rx_chain_3_valid = _hidl_blob.getInt8(84 + _hidl_offset);
        this.rx_chain_3.readEmbeddedFromParcel(parcel, _hidl_blob, 88 + _hidl_offset);
        this.tx_valid = _hidl_blob.getInt8(112 + _hidl_offset);
        this.tx.readEmbeddedFromParcel(parcel, _hidl_blob, 116 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(124);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<OPPO_RIL_Tx_Rx_info> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 124);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((OPPO_RIL_Tx_Rx_info) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 124));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(0 + _hidl_offset, this.rx_chain_0_valid);
        this.rx_chain_0.writeEmbeddedToBlob(_hidl_blob, 4 + _hidl_offset);
        _hidl_blob.putInt8(28 + _hidl_offset, this.rx_chain_1_valid);
        this.rx_chain_1.writeEmbeddedToBlob(_hidl_blob, 32 + _hidl_offset);
        _hidl_blob.putInt8(56 + _hidl_offset, this.rx_chain_2_valid);
        this.rx_chain_2.writeEmbeddedToBlob(_hidl_blob, 60 + _hidl_offset);
        _hidl_blob.putInt8(84 + _hidl_offset, this.rx_chain_3_valid);
        this.rx_chain_3.writeEmbeddedToBlob(_hidl_blob, 88 + _hidl_offset);
        _hidl_blob.putInt8(112 + _hidl_offset, this.tx_valid);
        this.tx.writeEmbeddedToBlob(_hidl_blob, 116 + _hidl_offset);
    }
}
