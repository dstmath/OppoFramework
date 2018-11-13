package com.qualcomm.qti.imscmservice.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class IMSCM_CONFIG_DATA {
    public final IMSCM_AUTOCONFIG_DATA autoConfig = new IMSCM_AUTOCONFIG_DATA();
    public final QIMS_CM_DEVICE_CONFIG deviceConfig = new QIMS_CM_DEVICE_CONFIG();
    public final QIMSCM_USER_CONFIG userConfig = new QIMSCM_USER_CONFIG();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != IMSCM_CONFIG_DATA.class) {
            return false;
        }
        IMSCM_CONFIG_DATA other = (IMSCM_CONFIG_DATA) otherObject;
        return HidlSupport.deepEquals(this.userConfig, other.userConfig) && HidlSupport.deepEquals(this.deviceConfig, other.deviceConfig) && HidlSupport.deepEquals(this.autoConfig, other.autoConfig);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.userConfig)), Integer.valueOf(HidlSupport.deepHashCode(this.deviceConfig)), Integer.valueOf(HidlSupport.deepHashCode(this.autoConfig))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".userConfig = ");
        builder.append(this.userConfig);
        builder.append(", .deviceConfig = ");
        builder.append(this.deviceConfig);
        builder.append(", .autoConfig = ");
        builder.append(this.autoConfig);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(328), 0);
    }

    public static final ArrayList<IMSCM_CONFIG_DATA> readVectorFromParcel(HwParcel parcel) {
        ArrayList<IMSCM_CONFIG_DATA> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 328), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            IMSCM_CONFIG_DATA _hidl_vec_element = new IMSCM_CONFIG_DATA();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 328));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.userConfig.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.deviceConfig.readEmbeddedFromParcel(parcel, _hidl_blob, 160 + _hidl_offset);
        this.autoConfig.readEmbeddedFromParcel(parcel, _hidl_blob, 304 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(328);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<IMSCM_CONFIG_DATA> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 328);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((IMSCM_CONFIG_DATA) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 328));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.userConfig.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        this.deviceConfig.writeEmbeddedToBlob(_hidl_blob, 160 + _hidl_offset);
        this.autoConfig.writeEmbeddedToBlob(_hidl_blob, 304 + _hidl_offset);
    }
}
