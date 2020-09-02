package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Message;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import com.android.internal.telephony.gsm.NetworkInfoWithAcT;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import java.util.ArrayList;

public interface IOppoSIMRecords extends IOppoCommonFeature {
    public static final IOppoSIMRecords DEFAULT = new IOppoSIMRecords() {
        /* class com.android.internal.telephony.uicc.IOppoSIMRecords.AnonymousClass1 */
    };
    public static final String TAG = "IOppoSIMRecords";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoSIMRecords;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoSIMRecords getDefault() {
        return DEFAULT;
    }

    default void oppoProcessChangeRegion(Context context, int slotId) {
    }

    default void parseEFopl(ArrayList messages) {
    }

    default void handlePlmnListData(Message response, byte[] result, Throwable ex) {
    }

    default void handleEfPOLResponse(int fileid, byte[] data, Message msg) {
    }

    default String getEonsIfExist(String plmn, int nLac, boolean bLongNameRequired) {
        return PhoneConfigurationManager.SSSS;
    }

    default void fetchCdmaPrl() {
    }

    default void getPreferedOperatorList(Message onComplete) {
    }

    default void setPOLEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
    }

    default String getPrlVersion() {
        return PhoneConfigurationManager.SSSS;
    }

    default void oppoSetSimSpn(String spn) {
    }

    default String getSIMCPHSOns() {
        return PhoneConfigurationManager.SSSS;
    }

    default void fetchCPHSOns() {
    }

    default String getSpNameInEfSpn() {
        return PhoneConfigurationManager.SSSS;
    }

    default String getFirstFullNameInEfPnn() {
        return PhoneConfigurationManager.SSSS;
    }

    default void fetchPnnAndOpl() {
    }

    default void parseEFpnn(ArrayList messages) {
    }

    default AbstractSIMRecords.OperatorName getEFpnnNetworkNames(int index) {
        return null;
    }

    default void processImsiReadComplete(String imsi) {
    }

    default void dispose() {
    }

    default void setSpnFromConfig(String carrier) {
    }
}
