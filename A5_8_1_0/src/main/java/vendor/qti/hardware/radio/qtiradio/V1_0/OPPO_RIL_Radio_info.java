package vendor.qti.hardware.radio.qtiradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class OPPO_RIL_Radio_info {
    public int arfcn;
    public int band;
    public int cellid;
    public int lac;
    public int mcc;
    public int mnc;
    public final ArrayList<OPPO_RIL_Ncell_Info> ncells = new ArrayList();
    public int ncells_len;
    public int rat;
    public int rrstatus;
    public int rssi;
    public int sinr;
    public int tx_power;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != OPPO_RIL_Radio_info.class) {
            return false;
        }
        OPPO_RIL_Radio_info other = (OPPO_RIL_Radio_info) otherObject;
        return this.rat == other.rat && this.mcc == other.mcc && this.mnc == other.mnc && this.lac == other.lac && this.cellid == other.cellid && this.arfcn == other.arfcn && this.band == other.band && this.rssi == other.rssi && this.sinr == other.sinr && this.rrstatus == other.rrstatus && this.tx_power == other.tx_power && this.ncells_len == other.ncells_len && HidlSupport.deepEquals(this.ncells, other.ncells);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rat))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.mcc))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.mnc))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lac))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cellid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.arfcn))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.band))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rssi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sinr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rrstatus))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.tx_power))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ncells_len))), Integer.valueOf(HidlSupport.deepHashCode(this.ncells))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".rat = ");
        builder.append(this.rat);
        builder.append(", .mcc = ");
        builder.append(this.mcc);
        builder.append(", .mnc = ");
        builder.append(this.mnc);
        builder.append(", .lac = ");
        builder.append(this.lac);
        builder.append(", .cellid = ");
        builder.append(this.cellid);
        builder.append(", .arfcn = ");
        builder.append(this.arfcn);
        builder.append(", .band = ");
        builder.append(this.band);
        builder.append(", .rssi = ");
        builder.append(this.rssi);
        builder.append(", .sinr = ");
        builder.append(this.sinr);
        builder.append(", .rrstatus = ");
        builder.append(this.rrstatus);
        builder.append(", .tx_power = ");
        builder.append(this.tx_power);
        builder.append(", .ncells_len = ");
        builder.append(this.ncells_len);
        builder.append(", .ncells = ");
        builder.append(this.ncells);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(64), 0);
    }

    public static final ArrayList<OPPO_RIL_Radio_info> readVectorFromParcel(HwParcel parcel) {
        ArrayList<OPPO_RIL_Radio_info> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            OPPO_RIL_Radio_info _hidl_vec_element = new OPPO_RIL_Radio_info();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.rat = _hidl_blob.getInt32(0 + _hidl_offset);
        this.mcc = _hidl_blob.getInt32(4 + _hidl_offset);
        this.mnc = _hidl_blob.getInt32(8 + _hidl_offset);
        this.lac = _hidl_blob.getInt32(12 + _hidl_offset);
        this.cellid = _hidl_blob.getInt32(16 + _hidl_offset);
        this.arfcn = _hidl_blob.getInt32(20 + _hidl_offset);
        this.band = _hidl_blob.getInt32(24 + _hidl_offset);
        this.rssi = _hidl_blob.getInt32(28 + _hidl_offset);
        this.sinr = _hidl_blob.getInt32(32 + _hidl_offset);
        this.rrstatus = _hidl_blob.getInt32(36 + _hidl_offset);
        this.tx_power = _hidl_blob.getInt32(40 + _hidl_offset);
        this.ncells_len = _hidl_blob.getInt32(44 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((48 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), (48 + _hidl_offset) + 0, true);
        this.ncells.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            OPPO_RIL_Ncell_Info _hidl_vec_element = new OPPO_RIL_Ncell_Info();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
            this.ncells.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(64);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<OPPO_RIL_Radio_info> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((OPPO_RIL_Radio_info) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.rat);
        _hidl_blob.putInt32(4 + _hidl_offset, this.mcc);
        _hidl_blob.putInt32(8 + _hidl_offset, this.mnc);
        _hidl_blob.putInt32(12 + _hidl_offset, this.lac);
        _hidl_blob.putInt32(16 + _hidl_offset, this.cellid);
        _hidl_blob.putInt32(20 + _hidl_offset, this.arfcn);
        _hidl_blob.putInt32(24 + _hidl_offset, this.band);
        _hidl_blob.putInt32(28 + _hidl_offset, this.rssi);
        _hidl_blob.putInt32(32 + _hidl_offset, this.sinr);
        _hidl_blob.putInt32(36 + _hidl_offset, this.rrstatus);
        _hidl_blob.putInt32(40 + _hidl_offset, this.tx_power);
        _hidl_blob.putInt32(44 + _hidl_offset, this.ncells_len);
        int _hidl_vec_size = this.ncells.size();
        _hidl_blob.putInt32((48 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((48 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((OPPO_RIL_Ncell_Info) this.ncells.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
        }
        _hidl_blob.putBlob((48 + _hidl_offset) + 0, childBlob);
    }
}
