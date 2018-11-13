package com.qualcomm.qti.imscmservice.V1_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class QIMS_CM_DEVICE_CONFIG {
    public boolean bCompactFormEnabled;
    public boolean bGruuEnabled;
    public boolean bIpSecEnabled;
    public boolean bKeepAliveEnableStatus;
    public boolean bUEBehindNAT;
    public int iPCSCFClientPort;
    public int iPCSCFOldSAClientPort;
    public int iPCSCFServerPort;
    public int iSipOutBoundProxyPort;
    public int iTCPThresholdValue;
    public String pArrAuthChallenge = new String();
    public String pArrNC = new String();
    public String pSecurityVerify = new String();
    public String pServiceRoute = new String();
    public String pStrSipOutBoundProxyName = new String();
    public String sPANI = new String();
    public String sPATH = new String();
    public String sPLANI = new String();
    public String sUriUserPart = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != QIMS_CM_DEVICE_CONFIG.class) {
            return false;
        }
        QIMS_CM_DEVICE_CONFIG other = (QIMS_CM_DEVICE_CONFIG) otherObject;
        return this.bUEBehindNAT == other.bUEBehindNAT && this.bIpSecEnabled == other.bIpSecEnabled && this.bCompactFormEnabled == other.bCompactFormEnabled && this.bKeepAliveEnableStatus == other.bKeepAliveEnableStatus && this.bGruuEnabled == other.bGruuEnabled && HidlSupport.deepEquals(this.pStrSipOutBoundProxyName, other.pStrSipOutBoundProxyName) && this.iSipOutBoundProxyPort == other.iSipOutBoundProxyPort && this.iPCSCFClientPort == other.iPCSCFClientPort && this.iPCSCFServerPort == other.iPCSCFServerPort && HidlSupport.deepEquals(this.pArrAuthChallenge, other.pArrAuthChallenge) && HidlSupport.deepEquals(this.pArrNC, other.pArrNC) && HidlSupport.deepEquals(this.pServiceRoute, other.pServiceRoute) && HidlSupport.deepEquals(this.pSecurityVerify, other.pSecurityVerify) && this.iPCSCFOldSAClientPort == other.iPCSCFOldSAClientPort && this.iTCPThresholdValue == other.iTCPThresholdValue && HidlSupport.deepEquals(this.sPANI, other.sPANI) && HidlSupport.deepEquals(this.sPATH, other.sPATH) && HidlSupport.deepEquals(this.sUriUserPart, other.sUriUserPart) && HidlSupport.deepEquals(this.sPLANI, other.sPLANI);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.bUEBehindNAT))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.bIpSecEnabled))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.bCompactFormEnabled))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.bKeepAliveEnableStatus))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.bGruuEnabled))), Integer.valueOf(HidlSupport.deepHashCode(this.pStrSipOutBoundProxyName)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iSipOutBoundProxyPort))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iPCSCFClientPort))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iPCSCFServerPort))), Integer.valueOf(HidlSupport.deepHashCode(this.pArrAuthChallenge)), Integer.valueOf(HidlSupport.deepHashCode(this.pArrNC)), Integer.valueOf(HidlSupport.deepHashCode(this.pServiceRoute)), Integer.valueOf(HidlSupport.deepHashCode(this.pSecurityVerify)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iPCSCFOldSAClientPort))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.iTCPThresholdValue))), Integer.valueOf(HidlSupport.deepHashCode(this.sPANI)), Integer.valueOf(HidlSupport.deepHashCode(this.sPATH)), Integer.valueOf(HidlSupport.deepHashCode(this.sUriUserPart)), Integer.valueOf(HidlSupport.deepHashCode(this.sPLANI))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".bUEBehindNAT = ");
        builder.append(this.bUEBehindNAT);
        builder.append(", .bIpSecEnabled = ");
        builder.append(this.bIpSecEnabled);
        builder.append(", .bCompactFormEnabled = ");
        builder.append(this.bCompactFormEnabled);
        builder.append(", .bKeepAliveEnableStatus = ");
        builder.append(this.bKeepAliveEnableStatus);
        builder.append(", .bGruuEnabled = ");
        builder.append(this.bGruuEnabled);
        builder.append(", .pStrSipOutBoundProxyName = ");
        builder.append(this.pStrSipOutBoundProxyName);
        builder.append(", .iSipOutBoundProxyPort = ");
        builder.append(this.iSipOutBoundProxyPort);
        builder.append(", .iPCSCFClientPort = ");
        builder.append(this.iPCSCFClientPort);
        builder.append(", .iPCSCFServerPort = ");
        builder.append(this.iPCSCFServerPort);
        builder.append(", .pArrAuthChallenge = ");
        builder.append(this.pArrAuthChallenge);
        builder.append(", .pArrNC = ");
        builder.append(this.pArrNC);
        builder.append(", .pServiceRoute = ");
        builder.append(this.pServiceRoute);
        builder.append(", .pSecurityVerify = ");
        builder.append(this.pSecurityVerify);
        builder.append(", .iPCSCFOldSAClientPort = ");
        builder.append(this.iPCSCFOldSAClientPort);
        builder.append(", .iTCPThresholdValue = ");
        builder.append(this.iTCPThresholdValue);
        builder.append(", .sPANI = ");
        builder.append(this.sPANI);
        builder.append(", .sPATH = ");
        builder.append(this.sPATH);
        builder.append(", .sUriUserPart = ");
        builder.append(this.sUriUserPart);
        builder.append(", .sPLANI = ");
        builder.append(this.sPLANI);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(176), 0);
    }

    public static final ArrayList<QIMS_CM_DEVICE_CONFIG> readVectorFromParcel(HwParcel parcel) {
        ArrayList<QIMS_CM_DEVICE_CONFIG> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 176), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            QIMS_CM_DEVICE_CONFIG _hidl_vec_element = new QIMS_CM_DEVICE_CONFIG();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 176));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.bUEBehindNAT = _hidl_blob.getBool(0 + _hidl_offset);
        this.bIpSecEnabled = _hidl_blob.getBool(1 + _hidl_offset);
        this.bCompactFormEnabled = _hidl_blob.getBool(2 + _hidl_offset);
        this.bKeepAliveEnableStatus = _hidl_blob.getBool(3 + _hidl_offset);
        this.bGruuEnabled = _hidl_blob.getBool(4 + _hidl_offset);
        this.pStrSipOutBoundProxyName = _hidl_blob.getString(8 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pStrSipOutBoundProxyName.getBytes().length + 1), _hidl_blob.handle(), 0 + (8 + _hidl_offset), false);
        this.iSipOutBoundProxyPort = _hidl_blob.getInt32(24 + _hidl_offset);
        this.iPCSCFClientPort = _hidl_blob.getInt32(28 + _hidl_offset);
        this.iPCSCFServerPort = _hidl_blob.getInt32(32 + _hidl_offset);
        this.pArrAuthChallenge = _hidl_blob.getString(40 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pArrAuthChallenge.getBytes().length + 1), _hidl_blob.handle(), 0 + (40 + _hidl_offset), false);
        this.pArrNC = _hidl_blob.getString(56 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pArrNC.getBytes().length + 1), _hidl_blob.handle(), 0 + (56 + _hidl_offset), false);
        this.pServiceRoute = _hidl_blob.getString(72 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pServiceRoute.getBytes().length + 1), _hidl_blob.handle(), 0 + (72 + _hidl_offset), false);
        this.pSecurityVerify = _hidl_blob.getString(88 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pSecurityVerify.getBytes().length + 1), _hidl_blob.handle(), 0 + (88 + _hidl_offset), false);
        this.iPCSCFOldSAClientPort = _hidl_blob.getInt32(104 + _hidl_offset);
        this.iTCPThresholdValue = _hidl_blob.getInt32(108 + _hidl_offset);
        this.sPANI = _hidl_blob.getString(112 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.sPANI.getBytes().length + 1), _hidl_blob.handle(), 0 + (112 + _hidl_offset), false);
        this.sPATH = _hidl_blob.getString(128 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.sPATH.getBytes().length + 1), _hidl_blob.handle(), 0 + (128 + _hidl_offset), false);
        this.sUriUserPart = _hidl_blob.getString(144 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.sUriUserPart.getBytes().length + 1), _hidl_blob.handle(), 0 + (144 + _hidl_offset), false);
        this.sPLANI = _hidl_blob.getString(160 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.sPLANI.getBytes().length + 1), _hidl_blob.handle(), 0 + (160 + _hidl_offset), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(176);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<QIMS_CM_DEVICE_CONFIG> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 176);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((QIMS_CM_DEVICE_CONFIG) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 176));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putBool(0 + _hidl_offset, this.bUEBehindNAT);
        _hidl_blob.putBool(1 + _hidl_offset, this.bIpSecEnabled);
        _hidl_blob.putBool(2 + _hidl_offset, this.bCompactFormEnabled);
        _hidl_blob.putBool(3 + _hidl_offset, this.bKeepAliveEnableStatus);
        _hidl_blob.putBool(4 + _hidl_offset, this.bGruuEnabled);
        _hidl_blob.putString(8 + _hidl_offset, this.pStrSipOutBoundProxyName);
        _hidl_blob.putInt32(24 + _hidl_offset, this.iSipOutBoundProxyPort);
        _hidl_blob.putInt32(28 + _hidl_offset, this.iPCSCFClientPort);
        _hidl_blob.putInt32(32 + _hidl_offset, this.iPCSCFServerPort);
        _hidl_blob.putString(40 + _hidl_offset, this.pArrAuthChallenge);
        _hidl_blob.putString(56 + _hidl_offset, this.pArrNC);
        _hidl_blob.putString(72 + _hidl_offset, this.pServiceRoute);
        _hidl_blob.putString(88 + _hidl_offset, this.pSecurityVerify);
        _hidl_blob.putInt32(104 + _hidl_offset, this.iPCSCFOldSAClientPort);
        _hidl_blob.putInt32(108 + _hidl_offset, this.iTCPThresholdValue);
        _hidl_blob.putString(112 + _hidl_offset, this.sPANI);
        _hidl_blob.putString(128 + _hidl_offset, this.sPATH);
        _hidl_blob.putString(144 + _hidl_offset, this.sUriUserPart);
        _hidl_blob.putString(160 + _hidl_offset, this.sPLANI);
    }
}
