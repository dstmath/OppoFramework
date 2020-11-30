package com.mediatek.internal.telephony;

import com.android.internal.telephony.IccCardConstants;

public class MtkIccCardConstants extends IccCardConstants {
    public static final String INTENT_KEY_SML_SLOT_DETECTED_TYPE = "DETECTED_TYPE";
    public static final String INTENT_KEY_SML_SLOT_DEVICE_LOCK_POLICY = "DEVICE_LOCK_POLICY";
    public static final String INTENT_KEY_SML_SLOT_DEVICE_LOCK_STATE = "DEVICE_LOCK_STATE";
    public static final String INTENT_KEY_SML_SLOT_SIM1_VALID = "SML_SIM1_VALID";
    public static final String INTENT_KEY_SML_SLOT_SIM2_VALID = "SML_SIM2_VALID";
    public static final String INTENT_KEY_SML_SLOT_SIM_COUNT = "SML_SIM_COUNT";
    public static final String INTENT_KEY_SML_SLOT_SIM_SERVICE_CAPABILITY = "SIM_SERVICE_CAPABILITY";
    public static final String INTENT_KEY_SML_SLOT_SIM_VALID = "SIM_VALID";
    public static final String INTENT_VALUE_LOCKED_CORPORATE = "CORPORATE";
    public static final String INTENT_VALUE_LOCKED_CORPORATE_PUK = "CORPORATE_PUK";
    public static final String INTENT_VALUE_LOCKED_NETWORK_PUK = "NETWORK_PUK";
    public static final String INTENT_VALUE_LOCKED_NETWORK_SUBSET = "NETWORK_SUBSET";
    public static final String INTENT_VALUE_LOCKED_NETWORK_SUBSET_PUK = "NETWORK_SUBSET_PUK";
    public static final String INTENT_VALUE_LOCKED_SERVICE_PROVIDER = "SERVICE_PROVIDER";
    public static final String INTENT_VALUE_LOCKED_SERVICE_PROVIDER_PUK = "SERVICE_PROVIDER_PUK";
    public static final String INTENT_VALUE_LOCKED_SIM = "SIM";
    public static final String INTENT_VALUE_LOCKED_SIM_PUK = "SIM_PUK";
    public static final int SML_SLOT_LOCK_POLICY_ALL_SLOTS_INDIVIDUAL = 3;
    public static final int SML_SLOT_LOCK_POLICY_ALL_SLOTS_INDIVIDUAL_AND_RSU_VZW = 11;
    public static final int SML_SLOT_LOCK_POLICY_LEGACY = 255;
    public static final int SML_SLOT_LOCK_POLICY_LK_SLOT1 = 4;
    public static final int SML_SLOT_LOCK_POLICY_LK_SLOT2 = 5;
    public static final int SML_SLOT_LOCK_POLICY_LK_SLOTA = 6;
    public static final int SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_INVALID_CS = 7;
    public static final int SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_INVALID_ECC_FOR_VALID_NO_SERVICE = 9;
    public static final int SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_INVALID_VOICE = 10;
    public static final int SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_REVERSE = 8;
    public static final int SML_SLOT_LOCK_POLICY_LOCK_STATE_NO = 1;
    public static final int SML_SLOT_LOCK_POLICY_LOCK_STATE_UNKNOWN = -1;
    public static final int SML_SLOT_LOCK_POLICY_LOCK_STATE_YES = 0;
    public static final int SML_SLOT_LOCK_POLICY_NONE = 0;
    public static final int SML_SLOT_LOCK_POLICY_ONLY_SLOT1 = 1;
    public static final int SML_SLOT_LOCK_POLICY_ONLY_SLOT2 = 2;
    public static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_CS_ONLY = 1;
    public static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_ECC_ONLY = 3;
    public static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_FULL = 0;
    public static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_NO_SERVICE = 4;
    public static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_PS_ONLY = 2;
    public static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_UNKNOWN = -1;
    public static final int SML_SLOT_LOCK_POLICY_UNKNOWN = -1;
    public static final int SML_SLOT_LOCK_POLICY_UNLOCK_GENERAL_FAIL = 2;
    public static final int SML_SLOT_LOCK_POLICY_UNLOCK_INCORRECT_PASSWORD = 1;
    public static final int SML_SLOT_LOCK_POLICY_UNLOCK_NO_LOCK_POLICY = 0;
    public static final int SML_SLOT_LOCK_POLICY_UNLOCK_SUCCESS = 3;
    public static final int SML_SLOT_LOCK_POLICY_UNLOCK_UNKNOWN = -1;
    public static final int SML_SLOT_LOCK_POLICY_VALID_CARD_ABSENT = 2;
    public static final int SML_SLOT_LOCK_POLICY_VALID_CARD_NO = 1;
    public static final int SML_SLOT_LOCK_POLICY_VALID_CARD_UNKNOWN = -1;
    public static final int SML_SLOT_LOCK_POLICY_VALID_CARD_YES = 0;

    @Deprecated
    public enum CardType {
        UIM_CARD(1),
        SIM_CARD(2),
        UIM_SIM_CARD(3),
        UNKNOW_CARD(4),
        CT_3G_UIM_CARD(5),
        CT_UIM_SIM_CARD(6),
        PIN_LOCK_CARD(7),
        CT_4G_UICC_CARD(8),
        NOT_CT_UICC_CARD(9),
        CT_EXCEL_GG_CARD(10),
        LOCKED_CARD(18),
        CARD_NOT_INSERTED(255);
        
        private int mValue;

        public int getValue() {
            return this.mValue;
        }

        public static CardType getCardTypeFromInt(int cardTypeInt) {
            CardType cardType = UNKNOW_CARD;
            CardType[] cardTypes = values();
            for (int i = 0; i < cardTypes.length; i++) {
                if (cardTypes[i].getValue() == cardTypeInt) {
                    return cardTypes[i];
                }
            }
            return cardType;
        }

        public boolean is4GCard() {
            return this == CT_4G_UICC_CARD || this == NOT_CT_UICC_CARD;
        }

        private CardType(int value) {
            this.mValue = value;
        }
    }

    public enum VsimType {
        LOCAL_SIM,
        REMOTE_SIM,
        SOFT_AKA_SIM,
        PHYSICAL_AKA_SIM,
        PHYSICAL_SIM;

        public boolean isUserDataAllowed() {
            return this == SOFT_AKA_SIM || this == PHYSICAL_AKA_SIM;
        }

        public boolean isDataRoamingAllowed() {
            return this == SOFT_AKA_SIM || this == REMOTE_SIM;
        }

        public boolean isAllowVsimConnection() {
            return this == SOFT_AKA_SIM || this == PHYSICAL_AKA_SIM;
        }

        public boolean isAllowReqNonVsimNetwork() {
            return this != SOFT_AKA_SIM;
        }

        public boolean isAllowOnlyVsimNetwork() {
            return this == SOFT_AKA_SIM;
        }

        public boolean isVsimCard() {
            return (this == PHYSICAL_SIM || this == PHYSICAL_AKA_SIM) ? false : true;
        }
    }
}
