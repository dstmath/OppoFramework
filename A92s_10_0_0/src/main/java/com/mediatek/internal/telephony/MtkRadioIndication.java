package com.mediatek.internal.telephony;

import android.content.Intent;
import android.hardware.radio.V1_2.CellIdentityOperatorNames;
import android.hardware.radio.V1_2.CellInfo;
import android.hardware.radio.V1_2.CellInfoGsm;
import android.hardware.radio.V1_2.CellInfoLte;
import android.hardware.radio.V1_2.CellInfoWcdma;
import android.hardware.radio.V1_2.NetworkScanResult;
import android.os.UserHandle;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RadioIndication;
import com.mediatek.internal.telephony.ppl.IPplSmsFilter;
import java.util.ArrayList;
import java.util.Iterator;

public class MtkRadioIndication extends RadioIndication {
    private static final String TAG = "MtkRadioInd";
    private MtkRIL mMtkRil;

    MtkRadioIndication(RIL ril) {
        super(ril);
        this.mMtkRil = (MtkRIL) ril;
    }

    public void rilConnected(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLog(1034);
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.setCdmaSubscriptionSource(mtkRIL.mCdmaSubscription, null);
        this.mMtkRil.setCellInfoListRate();
        this.mMtkRil.notifyRegistrantsRilConnectionChanged(15);
    }

    public void radioStateChanged(int indicationType, int radioState) {
        int oldState = this.mMtkRil.getRadioState();
        MtkRadioIndication.super.radioStateChanged(indicationType, radioState);
        if (this.mMtkRil.getRadioState() != oldState) {
            Intent intent = new Intent("com.mediatek.intent.action.RADIO_STATE_CHANGED");
            int transferedState = getRadioStateFromInt(radioState);
            intent.putExtra("radioState", transferedState);
            intent.putExtra(IPplSmsFilter.KEY_SUB_ID, MtkSubscriptionManager.getSubIdUsingPhoneId(this.mMtkRil.mInstanceId.intValue()));
            this.mMtkRil.mMtkContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            MtkRIL mtkRIL = this.mMtkRil;
            mtkRIL.riljLog("Broadcast for RadioStateChanged: state=" + transferedState);
        }
    }

    private int getSubId(int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds == null || subIds.length <= 0) {
            return -1;
        }
        return subIds[0];
    }

    public void networkScanResult_1_2(int indicationType, NetworkScanResult result) {
        MtkRIL mtkRIL = this.mMtkRil;
        Iterator<CellInfo> it = result.networkInfos.iterator();
        while (it.hasNext()) {
            String mccmnc = null;
            CellInfo record = it.next();
            int i = record.cellInfoType;
            if (i == 1) {
                CellInfoGsm cellInfoGsm = (CellInfoGsm) record.gsm.get(0);
                mccmnc = cellInfoGsm.cellIdentityGsm.base.mcc + cellInfoGsm.cellIdentityGsm.base.mnc;
                int nLac = cellInfoGsm.cellIdentityGsm.base.lac;
                CellIdentityOperatorNames cellIdentityOperatorNames = cellInfoGsm.cellIdentityGsm.operatorNames;
                MtkRIL mtkRIL2 = this.mMtkRil;
                cellIdentityOperatorNames.alphaLong = mtkRIL2.lookupOperatorName(getSubId(mtkRIL2.mInstanceId.intValue()), mccmnc, true, nLac);
                CellIdentityOperatorNames cellIdentityOperatorNames2 = cellInfoGsm.cellIdentityGsm.operatorNames;
                MtkRIL mtkRIL3 = this.mMtkRil;
                cellIdentityOperatorNames2.alphaShort = mtkRIL3.lookupOperatorName(getSubId(mtkRIL3.mInstanceId.intValue()), mccmnc, false, nLac);
                if (1 != 0) {
                    cellInfoGsm.cellIdentityGsm.operatorNames.alphaLong = cellInfoGsm.cellIdentityGsm.operatorNames.alphaLong.concat(" 2G");
                    cellInfoGsm.cellIdentityGsm.operatorNames.alphaShort = cellInfoGsm.cellIdentityGsm.operatorNames.alphaShort.concat(" 2G");
                }
                this.mMtkRil.riljLog("mccmnc=" + mccmnc + ", lac=" + nLac + ", longName=" + cellInfoGsm.cellIdentityGsm.operatorNames.alphaLong + " shortName=" + cellInfoGsm.cellIdentityGsm.operatorNames.alphaShort);
            } else if (i != 2) {
                if (i == 3) {
                    CellInfoLte cellInfoLte = (CellInfoLte) record.lte.get(0);
                    mccmnc = cellInfoLte.cellIdentityLte.base.mcc + cellInfoLte.cellIdentityLte.base.mnc;
                    int nLac2 = cellInfoLte.cellIdentityLte.base.tac;
                    CellIdentityOperatorNames cellIdentityOperatorNames3 = cellInfoLte.cellIdentityLte.operatorNames;
                    MtkRIL mtkRIL4 = this.mMtkRil;
                    cellIdentityOperatorNames3.alphaLong = mtkRIL4.lookupOperatorName(getSubId(mtkRIL4.mInstanceId.intValue()), mccmnc, true, nLac2);
                    CellIdentityOperatorNames cellIdentityOperatorNames4 = cellInfoLte.cellIdentityLte.operatorNames;
                    MtkRIL mtkRIL5 = this.mMtkRil;
                    cellIdentityOperatorNames4.alphaShort = mtkRIL5.lookupOperatorName(getSubId(mtkRIL5.mInstanceId.intValue()), mccmnc, false, nLac2);
                    if (1 != 0) {
                        cellInfoLte.cellIdentityLte.operatorNames.alphaLong = cellInfoLte.cellIdentityLte.operatorNames.alphaLong.concat(" 4G");
                        cellInfoLte.cellIdentityLte.operatorNames.alphaShort = cellInfoLte.cellIdentityLte.operatorNames.alphaShort.concat(" 4G");
                    }
                    this.mMtkRil.riljLog("mccmnc=" + mccmnc + ", lac=" + nLac2 + ", longName=" + cellInfoLte.cellIdentityLte.operatorNames.alphaLong + " shortName=" + cellInfoLte.cellIdentityLte.operatorNames.alphaShort);
                } else if (i == 4) {
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) record.wcdma.get(0);
                    mccmnc = cellInfoWcdma.cellIdentityWcdma.base.mcc + cellInfoWcdma.cellIdentityWcdma.base.mnc;
                    int nLac3 = cellInfoWcdma.cellIdentityWcdma.base.lac;
                    if (mccmnc != null && !mccmnc.equals("52000")) {
                        CellIdentityOperatorNames cellIdentityOperatorNames5 = cellInfoWcdma.cellIdentityWcdma.operatorNames;
                        MtkRIL mtkRIL6 = this.mMtkRil;
                        cellIdentityOperatorNames5.alphaLong = mtkRIL6.lookupOperatorName(getSubId(mtkRIL6.mInstanceId.intValue()), mccmnc, true, nLac3);
                        CellIdentityOperatorNames cellIdentityOperatorNames6 = cellInfoWcdma.cellIdentityWcdma.operatorNames;
                        MtkRIL mtkRIL7 = this.mMtkRil;
                        cellIdentityOperatorNames6.alphaShort = mtkRIL7.lookupOperatorName(getSubId(mtkRIL7.mInstanceId.intValue()), mccmnc, false, nLac3);
                    }
                    if (1 != 0) {
                        cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaLong = cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaLong.concat(" 3G");
                        cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaShort = cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaShort.concat(" 3G");
                    }
                    this.mMtkRil.riljLog("mccmnc=" + mccmnc + ", lac=" + nLac3 + ", longName=" + cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaLong + " shortName=" + cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaShort);
                } else {
                    throw new RuntimeException("unexpected cellinfotype: " + record.cellInfoType);
                }
            }
            if (this.mMtkRil.hidePLMN(mccmnc)) {
                it.remove();
                this.mMtkRil.riljLog("remove this one " + mccmnc);
            }
        }
        MtkRadioIndication.super.networkScanResult_1_2(indicationType, result);
    }

    public void networkScanResult_1_4(int indicationType, android.hardware.radio.V1_4.NetworkScanResult result) {
        MtkRIL mtkRIL = this.mMtkRil;
        Iterator<android.hardware.radio.V1_4.CellInfo> it = result.networkInfos.iterator();
        while (it.hasNext()) {
            String mccmnc = null;
            android.hardware.radio.V1_4.CellInfo record = it.next();
            byte discriminator = record.info.getDiscriminator();
            if (discriminator == 0) {
                CellInfoGsm cellInfoGsm = record.info.gsm();
                mccmnc = cellInfoGsm.cellIdentityGsm.base.mcc + cellInfoGsm.cellIdentityGsm.base.mnc;
                int nLac = cellInfoGsm.cellIdentityGsm.base.lac;
                CellIdentityOperatorNames cellIdentityOperatorNames = cellInfoGsm.cellIdentityGsm.operatorNames;
                MtkRIL mtkRIL2 = this.mMtkRil;
                cellIdentityOperatorNames.alphaLong = mtkRIL2.lookupOperatorName(getSubId(mtkRIL2.mInstanceId.intValue()), mccmnc, true, nLac);
                CellIdentityOperatorNames cellIdentityOperatorNames2 = cellInfoGsm.cellIdentityGsm.operatorNames;
                MtkRIL mtkRIL3 = this.mMtkRil;
                cellIdentityOperatorNames2.alphaShort = mtkRIL3.lookupOperatorName(getSubId(mtkRIL3.mInstanceId.intValue()), mccmnc, false, nLac);
                if (1 != 0) {
                    cellInfoGsm.cellIdentityGsm.operatorNames.alphaLong = cellInfoGsm.cellIdentityGsm.operatorNames.alphaLong.concat(" 2G");
                    cellInfoGsm.cellIdentityGsm.operatorNames.alphaShort = cellInfoGsm.cellIdentityGsm.operatorNames.alphaShort.concat(" 2G");
                }
                this.mMtkRil.riljLog("mccmnc=" + mccmnc + ", lac=" + nLac + ", longName=" + cellInfoGsm.cellIdentityGsm.operatorNames.alphaLong + " shortName=" + cellInfoGsm.cellIdentityGsm.operatorNames.alphaShort);
            } else if (discriminator != 1) {
                if (discriminator == 2) {
                    CellInfoWcdma cellInfoWcdma = record.info.wcdma();
                    mccmnc = cellInfoWcdma.cellIdentityWcdma.base.mcc + cellInfoWcdma.cellIdentityWcdma.base.mnc;
                    int nLac2 = cellInfoWcdma.cellIdentityWcdma.base.lac;
                    CellIdentityOperatorNames cellIdentityOperatorNames3 = cellInfoWcdma.cellIdentityWcdma.operatorNames;
                    MtkRIL mtkRIL4 = this.mMtkRil;
                    cellIdentityOperatorNames3.alphaLong = mtkRIL4.lookupOperatorName(getSubId(mtkRIL4.mInstanceId.intValue()), mccmnc, true, nLac2);
                    CellIdentityOperatorNames cellIdentityOperatorNames4 = cellInfoWcdma.cellIdentityWcdma.operatorNames;
                    MtkRIL mtkRIL5 = this.mMtkRil;
                    cellIdentityOperatorNames4.alphaShort = mtkRIL5.lookupOperatorName(getSubId(mtkRIL5.mInstanceId.intValue()), mccmnc, false, nLac2);
                    if (1 != 0) {
                        cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaLong = cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaLong.concat(" 3G");
                        cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaShort = cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaShort.concat(" 3G");
                    }
                    this.mMtkRil.riljLog("mccmnc=" + mccmnc + ", lac=" + nLac2 + ", longName=" + cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaLong + " shortName=" + cellInfoWcdma.cellIdentityWcdma.operatorNames.alphaShort);
                } else if (discriminator == 4) {
                    android.hardware.radio.V1_4.CellInfoLte cellInfoLte = record.info.lte();
                    mccmnc = cellInfoLte.base.cellIdentityLte.base.mcc + cellInfoLte.base.cellIdentityLte.base.mnc;
                    int nLac3 = cellInfoLte.base.cellIdentityLte.base.tac;
                    CellIdentityOperatorNames cellIdentityOperatorNames5 = cellInfoLte.base.cellIdentityLte.operatorNames;
                    MtkRIL mtkRIL6 = this.mMtkRil;
                    cellIdentityOperatorNames5.alphaLong = mtkRIL6.lookupOperatorName(getSubId(mtkRIL6.mInstanceId.intValue()), mccmnc, true, nLac3);
                    CellIdentityOperatorNames cellIdentityOperatorNames6 = cellInfoLte.base.cellIdentityLte.operatorNames;
                    MtkRIL mtkRIL7 = this.mMtkRil;
                    cellIdentityOperatorNames6.alphaShort = mtkRIL7.lookupOperatorName(getSubId(mtkRIL7.mInstanceId.intValue()), mccmnc, false, nLac3);
                    if (1 != 0) {
                        cellInfoLte.base.cellIdentityLte.operatorNames.alphaLong = cellInfoLte.base.cellIdentityLte.operatorNames.alphaLong.concat(" 4G");
                        cellInfoLte.base.cellIdentityLte.operatorNames.alphaShort = cellInfoLte.base.cellIdentityLte.operatorNames.alphaShort.concat(" 4G");
                    }
                    this.mMtkRil.riljLog("mccmnc=" + mccmnc + ", lac=" + nLac3 + ", longName=" + cellInfoLte.base.cellIdentityLte.operatorNames.alphaLong + " shortName=" + cellInfoLte.base.cellIdentityLte.operatorNames.alphaShort);
                } else {
                    throw new RuntimeException("unexpected cellinfotype: " + ((int) record.info.getDiscriminator()));
                }
            }
            if (this.mMtkRil.hidePLMN(mccmnc)) {
                it.remove();
                this.mMtkRil.riljLog("remove this one " + mccmnc);
            }
        }
        MtkRadioIndication.super.networkScanResult_1_4(indicationType, result);
    }

    public void cellInfoList_1_4(int indicationType, ArrayList<android.hardware.radio.V1_4.CellInfo> records) {
        this.mRil.processIndication(indicationType);
        ArrayList<android.telephony.CellInfo> response = this.mMtkRil.mtkConvertHalCellInfoList_1_4(records);
        this.mMtkRil.unsljLogRet(1036, response);
        this.mMtkRil.notifyCellInfoListRegistrants(response);
    }
}
