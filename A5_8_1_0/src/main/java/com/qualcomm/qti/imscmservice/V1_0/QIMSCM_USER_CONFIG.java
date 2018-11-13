package com.qualcomm.qti.imscmservice.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class QIMSCM_USER_CONFIG {
    public int eIpType;
    public int iUEClientPort;
    public int iUEOldSAClientPort;
    public int iUEPublicPort;
    public int iUEServerPort;
    public String pAssociatedURI = new String();
    public String pIMEIStr = new String();
    public String pLocalHostIPAddress = new String();
    public String pSipHomeDomain = new String();
    public String pSipPrivateUserId = new String();
    public String pSipPublicUserId = new String();
    public String pUEPubGruu = new String();
    public String pUEPublicIPAddress = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != QIMSCM_USER_CONFIG.class) {
            return false;
        }
        QIMSCM_USER_CONFIG other = (QIMSCM_USER_CONFIG) otherObject;
        return this.iUEClientPort == other.iUEClientPort && this.iUEServerPort == other.iUEServerPort && HidlSupport.deepEquals(this.pAssociatedURI, other.pAssociatedURI) && HidlSupport.deepEquals(this.pUEPublicIPAddress, other.pUEPublicIPAddress) && this.iUEPublicPort == other.iUEPublicPort && HidlSupport.deepEquals(this.pSipPublicUserId, other.pSipPublicUserId) && HidlSupport.deepEquals(this.pSipPrivateUserId, other.pSipPrivateUserId) && HidlSupport.deepEquals(this.pSipHomeDomain, other.pSipHomeDomain) && HidlSupport.deepEquals(this.pUEPubGruu, other.pUEPubGruu) && HidlSupport.deepEquals(this.pLocalHostIPAddress, other.pLocalHostIPAddress) && this.eIpType == other.eIpType && HidlSupport.deepEquals(this.pIMEIStr, other.pIMEIStr) && this.iUEOldSAClientPort == other.iUEOldSAClientPort;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iUEClientPort))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iUEServerPort))), Integer.valueOf(HidlSupport.deepHashCode(this.pAssociatedURI)), Integer.valueOf(HidlSupport.deepHashCode(this.pUEPublicIPAddress)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iUEPublicPort))), Integer.valueOf(HidlSupport.deepHashCode(this.pSipPublicUserId)), Integer.valueOf(HidlSupport.deepHashCode(this.pSipPrivateUserId)), Integer.valueOf(HidlSupport.deepHashCode(this.pSipHomeDomain)), Integer.valueOf(HidlSupport.deepHashCode(this.pUEPubGruu)), Integer.valueOf(HidlSupport.deepHashCode(this.pLocalHostIPAddress)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.eIpType))), Integer.valueOf(HidlSupport.deepHashCode(this.pIMEIStr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iUEOldSAClientPort)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".iUEClientPort = ");
        builder.append(this.iUEClientPort);
        builder.append(", .iUEServerPort = ");
        builder.append(this.iUEServerPort);
        builder.append(", .pAssociatedURI = ");
        builder.append(this.pAssociatedURI);
        builder.append(", .pUEPublicIPAddress = ");
        builder.append(this.pUEPublicIPAddress);
        builder.append(", .iUEPublicPort = ");
        builder.append(this.iUEPublicPort);
        builder.append(", .pSipPublicUserId = ");
        builder.append(this.pSipPublicUserId);
        builder.append(", .pSipPrivateUserId = ");
        builder.append(this.pSipPrivateUserId);
        builder.append(", .pSipHomeDomain = ");
        builder.append(this.pSipHomeDomain);
        builder.append(", .pUEPubGruu = ");
        builder.append(this.pUEPubGruu);
        builder.append(", .pLocalHostIPAddress = ");
        builder.append(this.pLocalHostIPAddress);
        builder.append(", .eIpType = ");
        builder.append(QIMSCM_IPTYPE_ENUM.toString(this.eIpType));
        builder.append(", .pIMEIStr = ");
        builder.append(this.pIMEIStr);
        builder.append(", .iUEOldSAClientPort = ");
        builder.append(this.iUEOldSAClientPort);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(160), 0);
    }

    public static final ArrayList<QIMSCM_USER_CONFIG> readVectorFromParcel(HwParcel parcel) {
        ArrayList<QIMSCM_USER_CONFIG> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 160), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            QIMSCM_USER_CONFIG _hidl_vec_element = new QIMSCM_USER_CONFIG();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 160));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.iUEClientPort = _hidl_blob.getInt32(0 + _hidl_offset);
        this.iUEServerPort = _hidl_blob.getInt32(4 + _hidl_offset);
        this.pAssociatedURI = _hidl_blob.getString(8 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pAssociatedURI.getBytes().length + 1), _hidl_blob.handle(), 0 + (8 + _hidl_offset), false);
        this.pUEPublicIPAddress = _hidl_blob.getString(24 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pUEPublicIPAddress.getBytes().length + 1), _hidl_blob.handle(), 0 + (24 + _hidl_offset), false);
        this.iUEPublicPort = _hidl_blob.getInt32(40 + _hidl_offset);
        this.pSipPublicUserId = _hidl_blob.getString(48 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pSipPublicUserId.getBytes().length + 1), _hidl_blob.handle(), 0 + (48 + _hidl_offset), false);
        this.pSipPrivateUserId = _hidl_blob.getString(64 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pSipPrivateUserId.getBytes().length + 1), _hidl_blob.handle(), 0 + (64 + _hidl_offset), false);
        this.pSipHomeDomain = _hidl_blob.getString(80 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pSipHomeDomain.getBytes().length + 1), _hidl_blob.handle(), 0 + (80 + _hidl_offset), false);
        this.pUEPubGruu = _hidl_blob.getString(96 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pUEPubGruu.getBytes().length + 1), _hidl_blob.handle(), 0 + (96 + _hidl_offset), false);
        this.pLocalHostIPAddress = _hidl_blob.getString(112 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pLocalHostIPAddress.getBytes().length + 1), _hidl_blob.handle(), 0 + (112 + _hidl_offset), false);
        this.eIpType = _hidl_blob.getInt32(128 + _hidl_offset);
        this.pIMEIStr = _hidl_blob.getString(136 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pIMEIStr.getBytes().length + 1), _hidl_blob.handle(), 0 + (136 + _hidl_offset), false);
        this.iUEOldSAClientPort = _hidl_blob.getInt32(152 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(160);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<QIMSCM_USER_CONFIG> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 160);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((QIMSCM_USER_CONFIG) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 160));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.iUEClientPort);
        _hidl_blob.putInt32(4 + _hidl_offset, this.iUEServerPort);
        _hidl_blob.putString(8 + _hidl_offset, this.pAssociatedURI);
        _hidl_blob.putString(24 + _hidl_offset, this.pUEPublicIPAddress);
        _hidl_blob.putInt32(40 + _hidl_offset, this.iUEPublicPort);
        _hidl_blob.putString(48 + _hidl_offset, this.pSipPublicUserId);
        _hidl_blob.putString(64 + _hidl_offset, this.pSipPrivateUserId);
        _hidl_blob.putString(80 + _hidl_offset, this.pSipHomeDomain);
        _hidl_blob.putString(96 + _hidl_offset, this.pUEPubGruu);
        _hidl_blob.putString(112 + _hidl_offset, this.pLocalHostIPAddress);
        _hidl_blob.putInt32(128 + _hidl_offset, this.eIpType);
        _hidl_blob.putString(136 + _hidl_offset, this.pIMEIStr);
        _hidl_blob.putInt32(152 + _hidl_offset, this.iUEOldSAClientPort);
    }
}
