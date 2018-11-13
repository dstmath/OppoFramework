package vendor.qti.hardware.radio.qtiradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class OPPO_RIL_Rx_Chain_info {
    public int ecio;
    public byte is_radio_turned;
    public int phase;
    public int rscp;
    public int rsrp;
    public int rx_pwr;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != OPPO_RIL_Rx_Chain_info.class) {
            return false;
        }
        OPPO_RIL_Rx_Chain_info other = (OPPO_RIL_Rx_Chain_info) otherObject;
        return this.is_radio_turned == other.is_radio_turned && this.rx_pwr == other.rx_pwr && this.ecio == other.ecio && this.rscp == other.rscp && this.rsrp == other.rsrp && this.phase == other.phase;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.is_radio_turned))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rx_pwr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ecio))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rscp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rsrp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.phase)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".is_radio_turned = ");
        builder.append(this.is_radio_turned);
        builder.append(", .rx_pwr = ");
        builder.append(this.rx_pwr);
        builder.append(", .ecio = ");
        builder.append(this.ecio);
        builder.append(", .rscp = ");
        builder.append(this.rscp);
        builder.append(", .rsrp = ");
        builder.append(this.rsrp);
        builder.append(", .phase = ");
        builder.append(this.phase);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<OPPO_RIL_Rx_Chain_info> readVectorFromParcel(HwParcel parcel) {
        ArrayList<OPPO_RIL_Rx_Chain_info> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            OPPO_RIL_Rx_Chain_info _hidl_vec_element = new OPPO_RIL_Rx_Chain_info();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.is_radio_turned = _hidl_blob.getInt8(0 + _hidl_offset);
        this.rx_pwr = _hidl_blob.getInt32(4 + _hidl_offset);
        this.ecio = _hidl_blob.getInt32(8 + _hidl_offset);
        this.rscp = _hidl_blob.getInt32(12 + _hidl_offset);
        this.rsrp = _hidl_blob.getInt32(16 + _hidl_offset);
        this.phase = _hidl_blob.getInt32(20 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<OPPO_RIL_Rx_Chain_info> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((OPPO_RIL_Rx_Chain_info) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(0 + _hidl_offset, this.is_radio_turned);
        _hidl_blob.putInt32(4 + _hidl_offset, this.rx_pwr);
        _hidl_blob.putInt32(8 + _hidl_offset, this.ecio);
        _hidl_blob.putInt32(12 + _hidl_offset, this.rscp);
        _hidl_blob.putInt32(16 + _hidl_offset, this.rsrp);
        _hidl_blob.putInt32(20 + _hidl_offset, this.phase);
    }
}
