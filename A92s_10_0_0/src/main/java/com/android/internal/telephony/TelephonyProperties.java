package com.android.internal.telephony;

public interface TelephonyProperties {
    public static final String ADD_PARTICIPANT_KEY = "add_participant";
    public static final String CURRENT_ACTIVE_PHONE = "gsm.current.phone-type";
    public static final String DISPLAY_OPPORTUNISTIC_SUBSCRIPTION_CARRIER_TEXT_PROPERTY_NAME = "persist.radio.display_opportunistic_carrier";
    public static final String EXTRA_DIAL_CONFERENCE_URI = "org.codeaurora.extra.DIAL_CONFERENCE_URI";
    public static final String EXTRA_SKIP_SCHEMA_PARSING = "org.codeaurora.extra.SKIP_SCHEMA_PARSING";
    public static final String PROPERTY_ACTIVE_MD = "ril.active.md";
    public static final String PROPERTY_BASEBAND_CAPABILITY = "gsm.baseband.capability";
    public static final String PROPERTY_BASEBAND_CAPABILITY_MD2 = "gsm.baseband.capability.md2";
    public static final String PROPERTY_BASEBAND_VERSION = "gsm.version.baseband";
    public static final String PROPERTY_BASEBAND_VERSION_2 = "gsm.version.baseband.2";
    public static final String PROPERTY_CALL_RING_DELAY = "ro.telephony.call_ring.delay";
    public static final String PROPERTY_CDMA_MSG_ID = "persist.radio.cdma.msgid";
    public static final String PROPERTY_CS_NETWORK_TYPE = "gsm.cs.network.type";
    public static final String PROPERTY_CS_NETWORK_TYPE_2 = "gsm.cs.network.type.2";
    public static final String PROPERTY_CS_NETWORK_TYPE_3 = "gsm.cs.network.type.3";
    public static final String PROPERTY_CS_NETWORK_TYPE_4 = "gsm.cs.network.type.4";
    public static final String PROPERTY_CS_NETWORK_TYPE_LTEDC = "gsm.cs.network.type.ltedc";
    public static final String PROPERTY_DATA_NETWORK_TYPE = "gsm.network.type";
    public static final String PROPERTY_DATA_NETWORK_TYPE_2 = "gsm.network.type.2";
    public static final String PROPERTY_DATA_NETWORK_TYPE_3 = "gsm.network.type.3";
    public static final String PROPERTY_DATA_NETWORK_TYPE_4 = "gsm.network.type.4";
    public static final String PROPERTY_DATA_NETWORK_TYPE_LTEDC = "gsm.network.type.ltedc";
    public static final String PROPERTY_DEFAULT_SUBSCRIPTION = "persist.radio.default.sub";
    public static final String PROPERTY_DISABLE_CALL = "ro.telephony.disable-call";
    public static final String PROPERTY_ECM_EXIT_TIMER = "ro.cdma.ecmexittimer";
    public static final String PROPERTY_GSM_SIM_INSERTED = "gsm.sim.inserted";
    public static final String PROPERTY_ICC_OPERATOR_ALPHA = "gsm.sim.operator.alpha";
    public static final String PROPERTY_ICC_OPERATOR_ALPHA_2 = "gsm.sim.operator.alpha.2";
    public static final String PROPERTY_ICC_OPERATOR_ALPHA_3 = "gsm.sim.operator.alpha.3";
    public static final String PROPERTY_ICC_OPERATOR_ALPHA_4 = "gsm.sim.operator.alpha.4";
    public static final String PROPERTY_ICC_OPERATOR_DEFAULT_NAME = "gsm.sim.operator.default-name";
    public static final String PROPERTY_ICC_OPERATOR_DEFAULT_NAME_2 = "gsm.sim.operator.default-name.2";
    public static final String PROPERTY_ICC_OPERATOR_DEFAULT_NAME_3 = "gsm.sim.operator.default-name.3";
    public static final String PROPERTY_ICC_OPERATOR_DEFAULT_NAME_4 = "gsm.sim.operator.default-name.4";
    public static final String PROPERTY_ICC_OPERATOR_IMSI = "gsm.sim.operator.imsi";
    public static final String PROPERTY_ICC_OPERATOR_IMSI_2 = "gsm.sim.operator.imsi.2";
    public static final String PROPERTY_ICC_OPERATOR_IMSI_3 = "gsm.sim.operator.imsi.3";
    public static final String PROPERTY_ICC_OPERATOR_IMSI_4 = "gsm.sim.operator.imsi.4";
    public static final String PROPERTY_ICC_OPERATOR_ISO_COUNTRY = "gsm.sim.operator.iso-country";
    public static final String PROPERTY_ICC_OPERATOR_ISO_COUNTRY_2 = "gsm.sim.operator.iso-country.2";
    public static final String PROPERTY_ICC_OPERATOR_ISO_COUNTRY_3 = "gsm.sim.operator.iso-country.3";
    public static final String PROPERTY_ICC_OPERATOR_ISO_COUNTRY_4 = "gsm.sim.operator.iso-country.4";
    public static final String PROPERTY_ICC_OPERATOR_NUMERIC = "gsm.sim.operator.numeric";
    public static final String PROPERTY_ICC_OPERATOR_NUMERIC_2 = "gsm.sim.operator.numeric.2";
    public static final String PROPERTY_ICC_OPERATOR_NUMERIC_3 = "gsm.sim.operator.numeric.3";
    public static final String PROPERTY_ICC_OPERATOR_NUMERIC_4 = "gsm.sim.operator.numeric.4";
    public static final String PROPERTY_IGNORE_NITZ = "telephony.test.ignore.nitz";
    public static final String PROPERTY_INECM_MODE = "ril.cdma.inecmmode";
    public static final String PROPERTY_LTE_ON_CDMA_DEVICE = "telephony.lteOnCdmaDevice";
    public static final String PROPERTY_LTE_ON_CDMA_PRODUCT_TYPE = "telephony.lteOnCdmaProductType";
    public static final String PROPERTY_MMS_TRANSACTION = "mms.transaction";
    public static final String PROPERTY_MULTI_SIM_CONFIG = "persist.radio.multisim.config";
    public static final String PROPERTY_NITZ_OPER_CODE = "persist.radio.nitz_oper_code";
    public static final String PROPERTY_NITZ_OPER_CODE2 = "persist.radio.nitz_oper_code2";
    public static final String PROPERTY_NITZ_OPER_CODE3 = "persist.radio.nitz_oper_code3";
    public static final String PROPERTY_NITZ_OPER_CODE4 = "persist.radio.nitz_oper_code4";
    public static final String PROPERTY_NITZ_OPER_LNAME = "persist.radio.nitz_oper_lname";
    public static final String PROPERTY_NITZ_OPER_LNAME2 = "persist.radio.nitz_oper_lname2";
    public static final String PROPERTY_NITZ_OPER_LNAME3 = "persist.radio.nitz_oper_lname3";
    public static final String PROPERTY_NITZ_OPER_LNAME4 = "persist.radio.nitz_oper_lname4";
    public static final String PROPERTY_NITZ_OPER_SNAME = "persist.radio.nitz_oper_sname";
    public static final String PROPERTY_NITZ_OPER_SNAME2 = "persist.radio.nitz_oper_sname2";
    public static final String PROPERTY_NITZ_OPER_SNAME3 = "persist.radio.nitz_oper_sname3";
    public static final String PROPERTY_NITZ_OPER_SNAME4 = "persist.radio.nitz_oper_sname4";
    public static final String PROPERTY_OPERATOR_ALPHA = "gsm.operator.alpha";
    public static final String PROPERTY_OPERATOR_ALPHA_2 = "gsm.operator.alpha.2";
    public static final String PROPERTY_OPERATOR_ALPHA_3 = "gsm.operator.alpha.3";
    public static final String PROPERTY_OPERATOR_ALPHA_4 = "gsm.operator.alpha.4";
    public static final String PROPERTY_OPERATOR_ALPHA_LTEDC = "gsm.operator.alpha.ltedc";
    public static final String PROPERTY_OPERATOR_IDP_STRING = "gsm.operator.idpstring";
    public static final String PROPERTY_OPERATOR_ISMANUAL = "operator.ismanual";
    public static final String PROPERTY_OPERATOR_ISMANUAL_2 = "operator.ismanual.2";
    public static final String PROPERTY_OPERATOR_ISMANUAL_3 = "operator.ismanual.3";
    public static final String PROPERTY_OPERATOR_ISMANUAL_4 = "operator.ismanual.4";
    public static final String PROPERTY_OPERATOR_ISO_COUNTRY = "gsm.operator.iso-country";
    public static final String PROPERTY_OPERATOR_ISO_COUNTRY_2 = "gsm.operator.iso-country.2";
    public static final String PROPERTY_OPERATOR_ISO_COUNTRY_3 = "gsm.operator.iso-country.3";
    public static final String PROPERTY_OPERATOR_ISO_COUNTRY_4 = "gsm.operator.iso-country.4";
    public static final String PROPERTY_OPERATOR_ISO_COUNTRY_LTEDC = "gsm.operator.iso-country.ltedc";
    public static final String PROPERTY_OPERATOR_ISROAMING = "gsm.operator.isroaming";
    public static final String PROPERTY_OPERATOR_ISROAMING_2 = "gsm.operator.isroaming.2";
    public static final String PROPERTY_OPERATOR_ISROAMING_3 = "gsm.operator.isroaming.3";
    public static final String PROPERTY_OPERATOR_ISROAMING_4 = "gsm.operator.isroaming.4";
    public static final String PROPERTY_OPERATOR_ISROAMING_LTEDC = "gsm.operator.isroaming.ltedc";
    public static final String PROPERTY_OPERATOR_NUMERIC = "gsm.operator.numeric";
    public static final String PROPERTY_OPERATOR_NUMERIC_2 = "gsm.operator.numeric.2";
    public static final String PROPERTY_OPERATOR_NUMERIC_3 = "gsm.operator.numeric.3";
    public static final String PROPERTY_OPERATOR_NUMERIC_4 = "gsm.operator.numeric.4";
    public static final String PROPERTY_OPERATOR_NUMERIC_LTEDC = "gsm.operator.numeric.ltedc";
    public static final String PROPERTY_OPERATOR_ROAMING_TYPE = "gsm.operator.roaming.type";
    public static final String PROPERTY_OTASP_NUM_SCHEMA = "ro.cdma.otaspnumschema";
    public static final String PROPERTY_PROJECT = "gsm.project.baseband";
    public static final String PROPERTY_PROJECT_2 = "gsm.project.baseband.2";
    public static final String PROPERTY_RADIO_SVLTE_MODE = "persist.radio.svlte.mode";
    public static final String PROPERTY_REBOOT_REQUIRED_ON_MODEM_CHANGE = "persist.radio.reboot_on_modem_change";
    public static final String PROPERTY_RESET_ON_RADIO_TECH_CHANGE = "persist.radio.reset_on_switch";
    public static final String PROPERTY_RIL_IMPL = "gsm.version.ril-impl";
    public static final String PROPERTY_RIL_SENDS_MULTIPLE_CALL_RING = "ro.telephony.call_ring.multiple";
    public static final String PROPERTY_ROAMING_INDICATOR_NEEDED = "gsm.roaming.indicator.needed";
    public static final String PROPERTY_ROAMING_INDICATOR_NEEDED_2 = "gsm.roaming.indicator.needed.2";
    public static final String PROPERTY_ROAMING_INDICATOR_NEEDED_3 = "gsm.roaming.indicator.needed.3";
    public static final String PROPERTY_ROAMING_INDICATOR_NEEDED_4 = "gsm.roaming.indicator.needed.4";
    public static final String PROPERTY_ROAMING_INDICATOR_NEEDED_LTEDC = "gsm.roaming.indicator.ltedc";
    public static final String PROPERTY_SIM_COUNT = "ro.telephony.sim.count";
    public static final String PROPERTY_SIM_INFO_READY = "gsm.siminfo.ready";
    public static final String PROPERTY_SIM_LOCALE_SETTINGS = "gsm.sim.locale.waiting";
    public static final String PROPERTY_SIM_STATE = "gsm.sim.state";
    public static final String PROPERTY_SIM_STATE_2 = "gsm.sim.state.2";
    public static final String PROPERTY_SIM_STATE_3 = "gsm.sim.state.3";
    public static final String PROPERTY_SIM_STATE_4 = "gsm.sim.state.4";
    public static final String PROPERTY_SMS_RECEIVE = "telephony.sms.receive";
    public static final String PROPERTY_SMS_SEND = "telephony.sms.send";
    public static final String PROPERTY_TERMINAL_BASED_CALL_WAITING_MODE = "persist.radio.terminal-based.cw";
    public static final String PROPERTY_TEST_CSIM = "persist.radio.test-csim";
    public static final String PROPERTY_UT_CFU_NOTIFICATION_MODE = "persist.radio.ut.cfu.mode";
    public static final String PROPERTY_VIDEOCALL_AUDIO_OUTPUT = "persist.radio.call.audio.output";
    public static final String PROPERTY_WAKE_LOCK_TIMEOUT = "ro.ril.wake_lock_timeout";
    public static final String TERMINAL_BASED_CALL_WAITING_DISABLED = "disabled_tbcw";
    public static final String TERMINAL_BASED_CALL_WAITING_ENABLED_OFF = "enabled_tbcw_off";
    public static final String TERMINAL_BASED_CALL_WAITING_ENABLED_ON = "enabled_tbcw_on";
    public static final String UT_CFU_NOTIFICATION_MODE_DISABLED = "disabled_ut_cfu_mode";
    public static final String UT_CFU_NOTIFICATION_MODE_OFF = "enabled_ut_cfu_mode_off";
    public static final String UT_CFU_NOTIFICATION_MODE_ON = "enabled_ut_cfu_mode_on";
}
