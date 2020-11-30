package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoUiccManager extends IOppoCommonFeature {
    public static final IOppoUiccManager DEFAULT = new IOppoUiccManager() {
        /* class com.android.internal.telephony.IOppoUiccManager.AnonymousClass1 */
    };
    public static final String TAG = "IOppoUiccManager";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoUiccManager;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoUiccManager getDefault() {
        return DEFAULT;
    }

    default void checkSoftSimCard(Context context) {
    }

    default boolean isHasSoftSimCard() {
        return false;
    }

    default int getSoftSimCardSlotId() {
        return 0;
    }

    default boolean isSoftSimSubId(int subId) {
        return false;
    }

    default boolean ifInterceptPopupTextMsg(CatCmdMessage cmdMsg, CatService catService) {
        return false;
    }

    default CatCmdMessage syncCurrentCmd(CatCmdMessage currntCmd, CatCmdMessage sCurrntCmd, CatService catService, int slotId) {
        return null;
    }

    default void enableHypnusAction() {
    }

    default void enableHypnusAction(int action, int timeout) {
    }

    default String getOemOperator(Context context, String plmn) {
        return PhoneConfigurationManager.SSSS;
    }

    default String getCarrierName(Context context, String name, String imsi, String iccid, int slotId) {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean isUiccSlotForbid(int slotId) {
        return false;
    }

    default void updateSubscriptionInfoByIccIdExt(Context context, String[] iccid) {
    }

    default void updateSimReadyExt(int slotId) {
    }

    default void updateSimLoadedExt(int slotId, String[] iccid) {
    }

    default String updateSimNameIfNeed(Context context, int slotId, int subId, String iccid, String defaultVaule) {
        return PhoneConfigurationManager.SSSS;
    }

    default void broadcastSimCardTypeReady(Context context, String slotid, String subid, int cardType) {
    }

    default int getCardType(String imsi, String iccid) {
        return 0;
    }

    default boolean updateMsisdnToSim(Context context, String number, int subId, Phone phone) {
        return false;
    }

    default int getSubState(int subId) {
        return 0;
    }

    default boolean OppoSpecialProcessForLockState(Intent simIntent, int slotId) {
        return false;
    }

    default OppoSimlockManager createOppoSimlockManager(Phone[] phone, CommandsInterface[] ci, Context context) {
        return null;
    }

    default OppoSimlockManager getOppoSimlockManager() {
        return null;
    }

    default boolean isRecordsDoNotExist(String currentIccid, int currentSlotId, Cursor cursor) {
        return false;
    }
}
