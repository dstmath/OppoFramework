package com.android.internal.telephony.uicc;

import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoUiccController extends IOppoCommonFeature {
    public static final IOppoUiccController DEFAULT = new IOppoUiccController() {
        /* class com.android.internal.telephony.uicc.IOppoUiccController.AnonymousClass1 */
    };
    public static final String TAG = "IOppoUiccController";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoUiccController;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoUiccController getDefault() {
        return DEFAULT;
    }

    default void notifyIccIdForSimPlugOut(int slotid) {
    }

    default void notifyIccIdForSimPlugIn(int slotid) {
    }

    default void notifyIccIdForTrayPlugIn(int slotid) {
    }

    default boolean isHotSwapSimReboot() {
        return false;
    }

    default boolean getSimHotSwapPlugInState() {
        return false;
    }

    default void saveSimPlugState(int index, int type) {
    }

    default void turnOffHotspot(int index) {
    }

    default void broadcastCardHotSwapState(int slotId) {
    }

    default void SendbroadcastSimInfoContentChanged() {
    }

    default void broadcastManualProvisionStatusChanged(int phoneId, int newProvisionState) {
    }

    default boolean getHaveInsertTestCard() {
        return false;
    }
}
