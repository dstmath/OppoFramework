package com.oppo.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OppoModemLogManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import com.oppo.internal.telephony.utils.OppoManagerHelper;
import java.util.ArrayList;
import java.util.HashMap;

public class OppoRIL {
    public static final String ACTION_FORCE_MODEM_CRASH = "oppo.intent.action.FORCE_MODEM_CRASH";
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT = 60000;
    static final int EVENT_ACK_WAKE_LOCK_TIMEOUT = 4;
    static final int EVENT_BLOCKING_RESPONSE_TIMEOUT = 5;
    public static final int EVENT_PROCESS_SOLICITED = 2;
    public static final int EVENT_PROCESS_UNSOLICITED = 1;
    static final int EVENT_RADIO_PROXY_DEAD = 6;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 3;
    public static final String ISSUE_SYS_OEM_NW_ANSWER_HANGUP_FAIL = "answer_hangup_fail";
    public static final String ISSUE_SYS_OEM_NW_CALL_NUMBER_UNKNOWN = "call_number_unknown";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_ACQ_CNT = "acq_cnt";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_APN_REASON_DATA_CALL_FAIL = "apn_reason_data_call_fail";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_AS_FAILED = "as_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT = "authentication_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_C2K_CALL_QUALITY = "speech_issue_c2k";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CALL_DROP = "call_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK = "card_drop_rx_break";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT = "card_drop_time_out";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CM_SERV_REJ = "mo_csfb_cm_serv_rej";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CONGEST_RATIO = "congest_ratio";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_AIRPLANE_NUM = "data_airplane_num";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_CALL_ERROR = "data_call_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_DISCONNECT_CALL_ERROR = "data_disconnect_call_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_DL_HIGH_BLER = "data_dl_high_bler";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_DORECOVERY_KPI = "data_dorecovery_kpi";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_DORECOVERY_RESULT = "data_dorecovery_result";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_GAME_LATENCY = "data_game_latency";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NOT_ALLOWED = "data_not_allowed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN = "data_no_available_apn";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_ONE_WAY_PASS = "data_pdcp_ul_one_way_pass";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_OUT_OF_BUFFER = "data_pdcp_ul_out_of_buffer";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT = "data_pdcp_ul_timeout";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_PDN_ACTIVATION_DURATION = "data_pdn_activation_duration";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_RATE_LIMIT_ON_LTE = "data_limit_on_lte";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_RECOVERY_SUCC_NUM = "data_recovery_succ_num";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_RLC_UL_RLF = "data_rlc_ul_rlf";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR = "data_setup_data_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_STALL_ERROR = "data_stall_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_USER_DATA_ENABLE_NUM = "data_user_data_enable_num";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_FAKE_BS = "fake_bs";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_FAKE_BS_ONLY = "fake_bs_only";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED = "gsm_t3126_expired";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_GWL_CALL_QUALITY = "speech_issue_gwl";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IA_APN_ERROR = "data_ia_apn_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_DISC_ABNORMAL = "ims_call_disc_abnormal";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_HO_FAIL = "ims_call_handover_fail";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_NORMAL = "ims_call_normal";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_MO_CALL_DROP = "ims_mo_call_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_MT_CALL_DROP = "ims_mt_call_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_REG_FAIL = "ims_registration_fail";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_CALL_DROP = "srvcc_call_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_CANCEL = "srvcc_cancel";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_COMPLETED = "srvcc_completed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_FAILED = "srvcc_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_INACTIVE_FULLBAND_CNT = "inactive_fullband_cnt";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_A2_RELEASE_CELL = "a2_release_cell";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_ABNORMAL_DETACH = "lte_abnormal_detach";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AS_FAILED = "lte_as_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AUTHENTICATION_REJECT = "lte_authentication_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_BAR_BAD_FAKE_CELL = "4g_bar_cell";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_HANDOVER_FAILURE = "lte_handover_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BANDWIDTH_SCELL = "lte_narrow_bandwidth_scell";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING = "lte_narrow_bw_monitoring";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT = "lte_reg_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE = "lte_reg_without_lte";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_ABNORMAL_BAR = "lte_rrc_abnormal_bar";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_ABNORMAL_TIMEOUT = "lte_rrc_abnormal_timeout";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_CONN_HOLD = "lte_rrc_conn_hold";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_SERVICE_REJECT = "lte_service_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_TAU_REJECT = "lte_tau_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED = "mcfg_iccid_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MCFG_LOAD_ISSUE = "mcfg_load_fail";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_CALL_DROP = "mo_call_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_DROP = "mo_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_MT_DROP_RATE = "mo_mt_drop_rate";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CALL_DROP = "mt_call_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CSFB = "mt_csfb";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_DISC = "mt_disconnect";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PAGE_FAIL = "mt_fail_after_page";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PCH = "mt_pch";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RACH = "mt_rach";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_REJECT = "mt_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RLF = "mt_rlf";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RRC = "mt_rrc";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_NOT_APN_REASON_DATA_CALL_FAIL = "not_apn_reason_data_call_fail";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_NO_RECEIVE_DATA_ERROR = "no_receive_data_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_NO_RESPONSE_FOR_DATA_CALL = "no_response_for_data_call";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_PCI_MODE3_INTERFERENCE = "pci_mode3_interference";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_PDP_ACT_ERROR = "data_pdp_active_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_PDP_DEACT_ERROR = "data_pdp_deactive_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_1X_MAP = "1x_reg_map_fail";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_BACKOFF_PLMN = "4g_backoff_plmn";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_BAR_BAD_FAKE_CELL = "4g_bar_cell";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_FAIL_5TIMES = "4g_reg_fail_5";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_INT_FAIL = "4g_integ_fail";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_REDIREC_UNEXPECT = "4g_redir_unexp";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_SUCCESS_AFTER_REJECT = "4g_reg_ok_afte_rej";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_TYPE_UNEXPECT = "4g_type_unexp";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_FORBIDDEN_TAI_OPT = "4g_forbid_ta_opt";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_LTE_REMOVED_CAUSE = "lte_removed_from_mode_pref";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT = "reg_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT_OUT_OF_CREDIT = "reg_reject_out_of_credit";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_ON = "srv_on";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_OOS = "srv_oos";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MCC = "srv_req_mcc";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MNC = "srv_req_mnc";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_RAT = "srv_req_rat";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RFFE_MISSING_NONFATAL = "rffe_missing_nonfatal";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_4G_PA_DAMAGE = "4R_PA_DAMAGE";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_ENTER_SAR_FORCE_DOWN_ANT = "enter_sar_force_down_ant";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED = "rf_mipi_hw_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_SIGNAL_STATIS = "rf_signal_statis";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_XO_OFFSET = "rf_xo_offset";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCREEN_OFF_ACQ_CNT = "screen_off_acq_cnt";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_DURATION = "screen_on_duration";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_NW_SRCH = "screen_on_nw_srch";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_TRIGGER_NW_SRCH = "screen_on_trigger_nw_srch";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_SKIP_ACQ_CNT = "skip_acq_cnt";
    public static final String ISSUE_SYS_OEM_NW_NO_SERVICE_AND_NW_SEARCH = "no_service_and_nw_search";
    private static final int LENGTH_1_BYTE = 1;
    private static final int LENGTH_2_BYTES = 2;
    private static final int LENGTH_4_BYTES = 4;
    public static final int MAX_MODEM_CRASH_CAUSE_LEN = 255;
    private static final String[] MODEM_CRASH_WHITE_LIST = {"for debug.", "for power consumption log."};
    private static long PreviousCurrentValue = -1;
    private static final int SYS_MTK_URC_3GPP_OOS_LOW_POWER_FOR_DMFAPP = 701;
    private static final int SYS_MTK_URC_ATTACH_REJ_FOR_DMFAPP = 304;
    private static final int SYS_MTK_URC_AUTHENTICATION_REJECT = 395;
    private static final int SYS_MTK_URC_AUTHEN_REJ_FOR_DMFAPP = 305;
    private static final int SYS_MTK_URC_CALL_C2K_CALL_DROP_FOR_DMFAPP = 508;
    private static final int SYS_MTK_URC_CALL_C2K_CALL_QUALITY_FOR_DMFAPP = 512;
    private static final int SYS_MTK_URC_CALL_CS_CALL_DROP_FOR_DMFAPP = 505;
    private static final int SYS_MTK_URC_CALL_GWL_CALL_QUALITY_FOR_DMFAPP = 511;
    private static final int SYS_MTK_URC_CALL_MO_C2K_CALL_DROP_FOR_DMFAPP = 507;
    private static final int SYS_MTK_URC_CALL_MO_CSFB_CALL_DROP_FOR_DMFAPP = 503;
    private static final int SYS_MTK_URC_CALL_MO_CS_CALL_DROP_FOR_DMFAPP = 504;
    private static final int SYS_MTK_URC_CALL_MT_C2K_CALL_DROP_FOR_DMFAPP = 506;
    private static final int SYS_MTK_URC_CALL_MT_CSFB_CALL_DROP_FOR_DMFAPP = 501;
    private static final int SYS_MTK_URC_CALL_MT_CS_CALL_DROP_FOR_DMFAPP = 502;
    private static final int SYS_MTK_URC_CARD_DROP = 89;
    private static final int SYS_MTK_URC_CARD_DROP_FOR_DMFAPP = 601;
    private static final int SYS_MTK_URC_DATA_DL_HIGH_BLER = 970;
    private static final int SYS_MTK_URC_DATA_DL_HIGH_BLER_FOR_DMFAPP = 404;
    private static final int SYS_MTK_URC_DATA_PDCP_UL_ONE_WAY_PASS = 947;
    private static final int SYS_MTK_URC_DATA_PDCP_UL_ONE_WAY_PASS_FOR_DMFAPP = 403;
    private static final int SYS_MTK_URC_DATA_PDCP_UL_OUT_OF_BUFFER = 949;
    private static final int SYS_MTK_URC_DATA_PDCP_UL_OUT_OF_BUFFER_FOR_DMFAPP = 402;
    private static final int SYS_MTK_URC_DATA_PDCP_UL_TIMEOUT = 948;
    private static final int SYS_MTK_URC_DATA_PDCP_UL_TIMEOUT_FOR_DMFAPP = 401;
    private static final int SYS_MTK_URC_DATA_RLC_UL_RLF = 400;
    private static final int SYS_MTK_URC_FOR_DMFAPP = 2;
    private static final int SYS_MTK_URC_IA_APN_ERROR = 1019;
    private static final int SYS_MTK_URC_IA_APN_ERROR_FOR_DMFAPP = 201;
    private static final int SYS_MTK_URC_IMS_CALL_HO_FAIL_FOR_DMFAPP = 510;
    private static final int SYS_MTK_URC_IMS_REG_FAIL_FOR_DMFAPP = 801;
    private static final int SYS_MTK_URC_LTE_A2_RELEASE_FOR_DMFAPP = 109;
    private static final int SYS_MTK_URC_LTE_AUTHENTICATION_REJECT = 628;
    private static final int SYS_MTK_URC_LTE_CELL_BAR_FOR_DMFAPP = 102;
    private static final int SYS_MTK_URC_LTE_CELL_REESTABLISHMENT_FAIL_FOR_DMFAPP = 103;
    private static final int SYS_MTK_URC_LTE_FAKE_CELL_BAR_FOR_DMFAPP = 108;
    private static final int SYS_MTK_URC_LTE_HANDOVER_FAILURE = 357;
    private static final int SYS_MTK_URC_LTE_HANDOVER_FAILURE_FOR_DMFAPP = 101;
    private static final int SYS_MTK_URC_LTE_MODE3_INTERFERENCE_FOR_DMFAPP = 104;
    private static final int SYS_MTK_URC_LTE_NARROW_BANDWIDTH_SCELL = 334;
    private static final int SYS_MTK_URC_LTE_NARROW_BANDWIDTH_SCELL_FOR_DMFAPP = 106;
    private static final int SYS_MTK_URC_LTE_NARROW_BW_MONITORING = 402;
    private static final int SYS_MTK_URC_LTE_NARROW_BW_MONITORING_FOR_DMFAPP = 107;
    private static final int SYS_MTK_URC_LTE_REG_REJECT = 625;
    private static final int SYS_MTK_URC_LTE_RLC_UL_RLF_FOR_DMFAPP = 105;
    private static final int SYS_MTK_URC_LTE_RRC_ABNORMAL_BAR = 356;
    private static final int SYS_MTK_URC_LTE_RRC_ABNORMAL_TIMEOUT = 401;
    private static final int SYS_MTK_URC_LU_REJ_FOR_DMFAPP = 301;
    private static final int SYS_MTK_URC_MT_CSFB = 393;
    private static final int SYS_MTK_URC_MT_RACH = 25;
    private static final int SYS_MTK_URC_MT_REJECT = 256;
    private static final int SYS_MTK_URC_MT_RLF_GSM = 26;
    private static final int SYS_MTK_URC_MT_RRC = 133;
    private static final int SYS_MTK_URC_NETWORK_DETACH_FOR_DMFAPP = 307;
    private static final int SYS_MTK_URC_OUT_OF_CREDIT_REJECT = 234;
    private static final int SYS_MTK_URC_OUT_OF_CREDIT_REJECT_FOR_DMFAPP = 10000;
    private static final int SYS_MTK_URC_PCI_MODE3_INTERFERENCE = 355;
    private static final int SYS_MTK_URC_PDP_ACT_ERROR_FOR_DMFAPP = 203;
    private static final int SYS_MTK_URC_PDP_DEACT_ERROR = 1018;
    private static final int SYS_MTK_URC_PDP_DEACT_ERROR_FOR_DMFAPP = 202;
    private static final int SYS_MTK_URC_RAU_REJ_FOR_DMFAPP = 302;
    private static final int SYS_MTK_URC_REG_REJECT = 394;
    private static final int SYS_MTK_URC_RF_MIPI_HW_FAILED = 108;
    private static final int SYS_MTK_URC_RF_MIPI_HW_FAILED_FOR_DMFAPP = 602;
    private static final int SYS_MTK_URC_SCREEN_ON_TRIGGER_NW_SRCH = 659;
    private static final int SYS_MTK_URC_SCREEN_ON_TRIGGER_NW_SRCH_FOR_DMFAPP = 702;
    private static final int SYS_MTK_URC_SERVICE_REJECT_FOR_DMFAPP = 306;
    private static final int SYS_MTK_URC_SMART_IDLE_TIMEOUT_MONITOR_FOR_DMFAPP = 703;
    private static final int SYS_MTK_URC_TAU_DETACH_SERVICE_REJECT = 233;
    private static final int SYS_MTK_URC_TAU_REJ_FOR_DMFAPP = 303;
    private static final int SYS_MTK_URC_VOLTE_CALL_DROP_FOR_DMFAPP = 509;
    public static final int SYS_OEM_NW_DIAG_CAUSE_APN_REASON_DATA_CALL_FAIL = 31;
    public static final int SYS_OEM_NW_DIAG_CAUSE_AS_FAILED = 65;
    public static final int SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT = 64;
    public static final int SYS_OEM_NW_DIAG_CAUSE_C2K_CALL_QUALITY = 41;
    private static final int SYS_OEM_NW_DIAG_CAUSE_CALL_BASE = 10;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK = 160;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT = 161;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CAUSE_ACQ_CNT = 93;
    private static final int SYS_OEM_NW_DIAG_CAUSE_CAUSE_ENDC_PWR_OPT = 103;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CAUSE_INACTIVE_FULLBAND_CNT = 96;
    private static final int SYS_OEM_NW_DIAG_CAUSE_CAUSE_SCENES_INFO = 99;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CAUSE_SCREEN_OFF_ACQ_CNT = 94;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CAUSE_SCREEN_ON_NW_SRCH = 91;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CAUSE_SCREEN_ON_TRIGGER_NW_SRCH = 92;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CAUSE_SKIP_ACQ_CNT = 95;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CM_SERV_REJ = 22;
    public static final int SYS_OEM_NW_DIAG_CAUSE_CONGEST_RATIO = 36;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_AIRPLANE_NUM = 26;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_BASE = 110;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_CALL_ERROR = 34;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_DISCONNECT_CALL_ERROR = 30;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_DL_HIGH_BLER = 123;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_DNS_FAIL = 115;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_DORECOVERY_KPI = 132;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_DORECOVERY_RESULT = 131;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_GAME_LATENCY = 114;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_NOT_ALLOWED = 110;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN = 111;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_ONE_WAY_PASS = 122;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_OUT_OF_BUFFER = 121;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT = 120;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_PDN_ACTIVATION_DURATION = 29;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_RATE_LIMIT_ON_LTE = 116;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_RECOVERY_SUCC_NUM = 28;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_RLC_UL_RLF = 127;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR = 112;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_STALL_ERROR = 113;
    public static final int SYS_OEM_NW_DIAG_CAUSE_DATA_USER_DATA_ENABLE_NUM = 27;
    public static final int SYS_OEM_NW_DIAG_CAUSE_FAKE_BS = 70;
    public static final int SYS_OEM_NW_DIAG_CAUSE_FAKE_BS_ONLY = 71;
    public static final int SYS_OEM_NW_DIAG_CAUSE_FORBIDDEN_TAI_OPT = 84;
    public static final int SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED = 66;
    public static final int SYS_OEM_NW_DIAG_CAUSE_GWL_CALL_QUALITY = 39;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IA_APN_ERROR = 117;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_BASE = 260;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_DISC_ABNORMAL = 264;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_HO_FAIL = 42;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_NORMAL = 265;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_MO_CALL_DROP = 230;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_MT_CALL_DROP = 231;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_MT_CALL_MISSED = 232;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_REG_FAIL = 43;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_CALL_DROP = 263;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_CANCEL = 262;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_COMPLETED = 260;
    public static final int SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_FAILED = 261;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_A2_RELEASE_CELL = 108;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_ABNORMAL_DETACH = 104;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_AS_FAILED = 60;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_AUTHENTICATION_REJECT = 68;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_BACKOFF_PLMN = 86;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_BAR_BAD_FAKE_CELL = 85;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_HANDOVER_FAILURE = 105;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BANDWIDTH_SCELL = 97;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING = 129;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_REG_FAIL_5TIMES = 87;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT = 61;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_REG_SUCCESS_AFTER_REJECT = 88;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE = 62;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_ABNORMAL_BAR = 106;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_ABNORMAL_TIMEOUT = 107;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_CONN_HOLD = 118;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_SERVICE_REJECT = 102;
    public static final int SYS_OEM_NW_DIAG_CAUSE_LTE_TAU_REJECT = 101;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MCFG_CONFIG_CHANGE = 76;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED = 67;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MO_CALL_DROP = 23;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MO_DROP = 10;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MO_MT_DROP_RATE = 25;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MT_CALL_DROP = 24;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MT_CSFB = 14;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MT_DISC = 20;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MT_PAGE_FAIL = 19;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MT_PCH = 13;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MT_RACH = 11;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MT_REJECT = 15;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MT_RLF = 12;
    public static final int SYS_OEM_NW_DIAG_CAUSE_MT_RRC = 16;
    public static final int SYS_OEM_NW_DIAG_CAUSE_NOT_APN_REASON_DATA_CALL_FAIL = 32;
    public static final int SYS_OEM_NW_DIAG_CAUSE_NO_RECEIVE_DATA_ERROR = 37;
    public static final int SYS_OEM_NW_DIAG_CAUSE_NO_RESPONSE_FOR_DATA_CALL = 35;
    public static final int SYS_OEM_NW_DIAG_CAUSE_PCI_MODE3_INTERFERENCE = 109;
    public static final int SYS_OEM_NW_DIAG_CAUSE_PDP_ACT_ERROR = 124;
    public static final int SYS_OEM_NW_DIAG_CAUSE_PDP_DEACT_ERROR = 125;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_1X_MAP = 90;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_BASE = 60;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_LTE_INT_FAIL = 81;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_LTE_REDIREC_UNEXPECT = 82;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_LTE_TYPE_UNEXPECT = 83;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_MCC_CHANGE = 69;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_REJECT = 63;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_REJECT_OUT_OF_CREDIT = 89;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_ON = 75;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_OOS = 80;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MCC = 73;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MNC = 74;
    public static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_RAT = 72;
    public static final int SYS_OEM_NW_DIAG_CAUSE_RFFE_MISSING_NONFATAL = 211;
    public static final int SYS_OEM_NW_DIAG_CAUSE_RF_4G_PA_DAMAGE = 216;
    public static final int SYS_OEM_NW_DIAG_CAUSE_RF_BASE = 210;
    public static final int SYS_OEM_NW_DIAG_CAUSE_RF_ENTER_SAR_FORCE_DOWN_ANT = 215;
    public static final int SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED = 210;
    public static final int SYS_OEM_NW_DIAG_CAUSE_RF_MODEM_CRASH = 213;
    public static final int SYS_OEM_NW_DIAG_CAUSE_RF_XO_FREQ_OFFSET = 214;
    public static final int SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_DURATION = 33;
    public static final int SYS_OEM_NW_DIAG_CAUSE_SIGNAL_STATISTIC = 212;
    public static final int SYS_OEM_NW_DIAG_CAUSE_UIM_BASE = 160;
    public static final int SYS_OEM_NW_DIAG_RAT_CDMA = 3;
    public static final int SYS_OEM_NW_DIAG_RAT_GSM = 0;
    public static final int SYS_OEM_NW_DIAG_RAT_HDR = 4;
    public static final int SYS_OEM_NW_DIAG_RAT_LTE = 5;
    public static final int SYS_OEM_NW_DIAG_RAT_NONE = -1;
    public static final int SYS_OEM_NW_DIAG_RAT_TDS = 1;
    public static final int SYS_OEM_NW_DIAG_RAT_WCDMA = 2;
    private String TAG = "RILJ-Oppo";
    private long accelerate_to_large_bw_cell_count = 0;
    private long block_to_small_bw_cell_count = 0;
    private long change_to_large_bw_cell_count = 0;
    private long change_to_small_bw_cell_count = 0;
    private long conn_accelerate_to_large_bw_cell_count = 0;
    private long conn_block_to_small_bw_cell_count = 0;
    private long conn_change_to_large_bw_cell_count = 0;
    private long conn_change_to_small_bw_cell_count = 0;
    private long conn_large_bw_cell_stayed_time = 0;
    private long conn_small_bw_cell_stayed_time = 0;
    private long large_bw_cell_stayed_time = 0;
    public Context mContext;
    private int mFakeBSArfcn = -1;
    private final EventHandler mHandler;
    public long mHangupTime = -1;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.OppoRIL.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            OppoRIL.this.logd("mIntentReceiver");
        }
    };
    protected boolean mIsMobileNetworkSupported;
    public Handler mKeylogHandler = new Handler() {
        /* class com.oppo.internal.telephony.OppoRIL.AnonymousClass2 */

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 1 && i == 2) {
                OppoRIL.this.logd("EVENT_PROCESS_SOLICITED");
                OppoRIL.this.oppoProcessSolOemKeyLogErrMsg(msg.arg1, msg.arg2);
            }
        }
    };
    private RegistrantList mLteCARegistrants = new RegistrantList();
    protected int[] mLteCaInfo = null;
    private Phone mPhone;
    private int mPhoneId = 0;
    protected int mPsStateInfo = 0;
    private RegistrantList mPsStateRegistrants = new RegistrantList();
    int mRequestMessagesWaiting;
    /* access modifiers changed from: private */
    public ArrayList<OppoRILRequest> mRequestsList = new ArrayList<>();
    PowerManager.WakeLock mWakeLock;
    int mWakeLockTimeout;
    private long small_bw_cell_stayed_time = 0;
    private long small_bw_time = 0;

    private class CriticalLogInfo {
        long errcode;
        String extra;
        String issue;
        long rat;
        long type;

        public CriticalLogInfo(int type2, int errcode2, int rat2, String extra2, String issue2) {
            this.type = (long) type2;
            this.errcode = (long) errcode2;
            this.rat = (long) rat2;
            this.extra = extra2;
            this.issue = issue2;
        }
    }

    private class EventHandler extends Handler {
        private EventHandler() {
        }

        public void handleMessage(Message msg) {
            OppoRIL oppoRIL = OppoRIL.this;
            oppoRIL.logd("EventHandler:" + msg.what);
            if (msg.what == 3) {
                synchronized (OppoRIL.this.mWakeLock) {
                    if (OppoRIL.this.mWakeLock.isHeld()) {
                        if (OppoRIL.this.mRequestMessagesWaiting != 0) {
                            OppoRIL oppoRIL2 = OppoRIL.this;
                            oppoRIL2.logd("NOTE: mReqWaiting is NOT 0 but " + OppoRIL.this.mRequestMessagesWaiting + " at TIMEOUT, reset! There still msg waiting for response");
                            OppoRIL.this.mRequestMessagesWaiting = 0;
                            synchronized (OppoRIL.this.mRequestsList) {
                                int count = OppoRIL.this.mRequestsList.size();
                                OppoRIL oppoRIL3 = OppoRIL.this;
                                oppoRIL3.logd("WAKE_LOCK_TIMEOUT  mRequestList=" + count);
                                for (int i = 0; i < count; i++) {
                                    OppoRILRequest rr = (OppoRILRequest) OppoRIL.this.mRequestsList.get(i);
                                    OppoRIL oppoRIL4 = OppoRIL.this;
                                    oppoRIL4.logd(i + ": [" + rr.mSerial + "] " + OppoRIL.requestToString(rr.mRequest));
                                }
                            }
                        }
                        OppoRIL.this.mWakeLock.release();
                    }
                }
            }
        }
    }

    public static class OppoRILRequest {
        private static final int MAX_POOL_SIZE = 4;
        static final String TAG = "OppoRILRequest";
        static int sNextSerial = 1000;
        private static OppoRILRequest sPool = null;
        private static int sPoolSize = 0;
        private static Object sPoolSync = new Object();
        static Object sSerialMonitor = new Object();
        OppoRILRequest mNext;
        int mRequest;
        Message mResult;
        int mSerial;

        static OppoRILRequest obtain(int request, Message result) {
            OppoRILRequest rr;
            OppoRILRequest rr2 = null;
            synchronized (sPoolSync) {
                if (sPool != null) {
                    rr2 = sPool;
                    sPool = rr2.mNext;
                    rr2.mNext = null;
                    sPoolSize--;
                }
            }
            if (rr2 == null) {
                rr = new OppoRILRequest();
            } else {
                rr = rr2;
            }
            synchronized (sSerialMonitor) {
                int i = sNextSerial;
                sNextSerial = i + 1;
                rr.mSerial = i;
            }
            rr.mRequest = request;
            rr.mResult = result;
            if (result == null || result.getTarget() != null) {
                return rr;
            }
            throw new NullPointerException("Message target must not be null");
        }

        /* access modifiers changed from: package-private */
        public void release() {
            synchronized (sPoolSync) {
                if (sPoolSize < 4) {
                    this.mNext = sPool;
                    sPool = this;
                    sPoolSize++;
                    this.mResult = null;
                }
            }
        }

        private OppoRILRequest() {
        }

        /* access modifiers changed from: package-private */
        public void resetSerial() {
            synchronized (sSerialMonitor) {
                sNextSerial = 1000;
            }
        }

        /* access modifiers changed from: package-private */
        public String serialString() {
            StringBuilder sb = new StringBuilder(8);
            String sn = Integer.toString(this.mSerial);
            sb.append('[');
            int s = sn.length();
            for (int i = 0; i < 4 - s; i++) {
                sb.append('0');
            }
            sb.append(sn);
            sb.append(']');
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public void onError(int error, Object ret) {
            CommandException ex = CommandException.fromRilErrno(error);
            Rlog.e(TAG, serialString() + " < " + OppoRIL.requestToString(this.mRequest) + " error: " + ex);
            Message message = this.mResult;
            if (message != null) {
                AsyncResult.forMessage(message, ret, ex);
                this.mResult.sendToTarget();
            }
        }
    }

    private OppoRILRequest obtainRequest(int request, Message result) {
        acquireWakeLock();
        OppoRILRequest rr = OppoRILRequest.obtain(request, result);
        synchronized (this.mRequestsList) {
            this.mRequestsList.add(rr);
            this.mRequestMessagesWaiting++;
        }
        return rr;
    }

    private void acquireWakeLock() {
        synchronized (this.mWakeLock) {
            this.mWakeLock.acquire();
            this.mHandler.removeMessages(3);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), (long) this.mWakeLockTimeout);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
        return r3;
     */
    private OppoRILRequest findAndRemoveRequestFromList(int serial) {
        synchronized (this.mRequestsList) {
            int i = 0;
            int s = this.mRequestsList.size();
            while (i < s) {
                OppoRILRequest rr = this.mRequestsList.get(i);
                if (rr.mSerial == serial) {
                    this.mRequestsList.remove(i);
                    if (this.mRequestMessagesWaiting > 0) {
                        this.mRequestMessagesWaiting--;
                    }
                } else {
                    i++;
                }
            }
            return null;
        }
    }

    private void releaseWakeLockIfDone() {
        synchronized (this.mWakeLock) {
            if (this.mWakeLock.isHeld() && this.mRequestMessagesWaiting == 0) {
                this.mHandler.removeMessages(3);
                this.mWakeLock.release();
            }
        }
    }

    private void clearRequestsList(int error) {
        synchronized (this.mRequestsList) {
            int count = this.mRequestsList.size();
            logd("mRequestList count=" + count);
            for (int i = 0; i < count; i++) {
                OppoRILRequest rr = this.mRequestsList.get(i);
                logd(i + ": [" + rr.serialString() + "] " + requestToString(rr.mRequest));
                sendResponse(rr, error, null);
            }
            this.mRequestsList.clear();
            this.mRequestMessagesWaiting = 0;
        }
    }

    private void sendResponse(OppoRILRequest req, int error, Object ret) {
        if (req == null) {
            return;
        }
        if (error != 0) {
            req.onError(error, ret);
            req.release();
        } else if (req.mResult != null) {
            logd(req.mResult.toString());
            AsyncResult.forMessage(req.mResult, ret, (Throwable) null);
            req.mResult.sendToTarget();
        }
    }

    static String requestToString(int request) {
        if (request == 1) {
            return "FACTORY_MODE_NV_PROCESS";
        }
        if (request == 2) {
            return "FACTORY_MODE_MODEM_GPIO";
        }
        if (request == 5) {
            return "GET_RFFE_DEV_INFO";
        }
        if (request == 24) {
            return "OPPO_GET_ASDIV_STATE";
        }
        if (request == 25) {
            return "OEM_COMMON_REQ";
        }
        if (request == 30) {
            return "OPPO_GET_NW_SEARCH_COUNT";
        }
        if (request == 31) {
            return "RIL_REQUEST_OEM_SET_ECC_LIST";
        }
        switch (request) {
            case 8:
                return "GO_TO_ERROR_FATAL";
            case 9:
                return "GET_MDM_BASEBAND";
            case 10:
                return "SET_TDD_LTE";
            default:
                switch (request) {
                    case 12:
                        return "OPPO_GET_RADIO_INFO";
                    case 13:
                        return "OPPO_SET_FILTER_ARFCN";
                    case 14:
                        return "OPPO_SET_PPLMN_LIST";
                    case 15:
                        return "OPPO_GET_TX_RX_INFO";
                    case 16:
                        return "OPPO_EXP_IND_REGION_CHANGED_FOR_RIL_ECCLIST";
                    case 17:
                        return "OPPO_SET_FAKEBS_WEIGHT";
                    case 18:
                        return "OPPO_SET_VOLTE_FR2";
                    case 19:
                        return "OPPO_SET_VOLTE_FR1";
                    case 20:
                        return "OPPO_LOCK_GSM_ARFCN";
                    case NetworkDiagnoseUtils.RF_BAND21 /*{ENCODED_INT: 21}*/:
                        return "OPPO_RFFE_CMD";
                    default:
                        return "<unknown request>,please case it";
                }
        }
    }

    private void enforceModifyPermission(String msg) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", msg);
    }

    public OppoRIL(Context context, Phone phone) {
        logd("OppoRIL enter");
        this.mPhone = phone;
        this.mContext = context;
        this.mPhoneId = phone.getPhoneId();
        this.TAG += "/" + this.mPhoneId;
        this.mIsMobileNetworkSupported = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0);
        this.mContext.registerReceiver(this.mIntentReceiver, new IntentFilter());
        this.mHandler = new EventHandler();
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, this.TAG);
        this.mWakeLock.setReferenceCounted(false);
        this.mWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", 60000);
        this.mRequestMessagesWaiting = 0;
    }

    private int isInWhiteList(String cause) {
        if (cause == null) {
            return -1;
        }
        int i = 0;
        while (true) {
            String[] strArr = MODEM_CRASH_WHITE_LIST;
            if (i >= strArr.length) {
                return -1;
            }
            if (cause.equals(strArr[i])) {
                return i;
            }
            i++;
        }
    }

    public void notifyLteCARegistrants(int[] caInfo) {
        if (this.mLteCARegistrants != null) {
            String str = this.TAG;
            Rlog.d(str, "OPPO_DBG: notifyLteCARegistrants: size=" + this.mLteCARegistrants.size());
            this.mLteCARegistrants.notifyRegistrants(new AsyncResult((Object) null, caInfo, (Throwable) null));
        }
        this.mLteCaInfo = caInfo;
        int[] iArr = this.mLteCaInfo;
        if (iArr != null && iArr.length == 21 && iArr[10] == 0 && iArr[17] == 0) {
            logd("OPPO_DBG: notifyLteCARegistrants: deconfigured ");
            this.mLteCaInfo = null;
        }
    }

    public void oppoProcessSolOemKeyLogErrMsg(int request, int error) {
        String log_string;
        int size;
        Rlog.d(this.TAG, "dial or answer or hangup call failure");
        if (request == 10) {
            log_string = OemTelephonyUtils.getOemRes(this.mContext, "zz_oppo_critical_log_10", "");
        } else {
            log_string = OemTelephonyUtils.getOemRes(this.mContext, "zz_oppo_critical_log_17", "");
        }
        if (log_string.equals("")) {
            logd("Can not get resource of identifier zz_oppo_critical_log");
            return;
        }
        String[] log_array = log_string.split(",");
        int log_type = Integer.valueOf(log_array[0]).intValue();
        String log_desc = log_array[1];
        logd("log_type:" + log_type + "log_desc:" + log_desc);
        if (request == 10) {
            Rlog.d(this.TAG, "dial call failure");
            size = OppoManagerHelper.writeLogToPartition(log_type, getCellLocation() + ", mo drop cause: dial failure, imscall:false", ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_DROP, log_desc);
            Context context = this.mContext;
            OppoModemLogManager.saveModemLogPostBack(context, "" + log_type, ", mo drop cause: dial failure, imscall:false");
        } else {
            size = OppoManagerHelper.writeLogToPartition(log_type, getCellLocation() + ", request:" + request + ", error:" + error, ISSUE_SYS_OEM_NW_ANSWER_HANGUP_FAIL, log_desc);
        }
        logd("Write log, return:" + size);
    }

    public void getBandMode(Message response) {
    }

    public void registerForLteCAState(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mLteCARegistrants.add(r);
        int[] iArr = this.mLteCaInfo;
        if (iArr != null) {
            r.notifyRegistrant(new AsyncResult((Object) null, iArr, (Throwable) null));
        }
    }

    public void unregisterForLteCAState(Handler h) {
        this.mLteCARegistrants.remove(h);
    }

    public void registerForPsState(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsStateRegistrants.add(r);
        r.notifyRegistrant(new AsyncResult((Object) null, Integer.valueOf(this.mPsStateInfo), (Throwable) null));
    }

    public void unregisterForPsState(Handler h) {
        this.mPsStateRegistrants.remove(h);
    }

    private static long getValueFromByte(String data, int start, int bytes, boolean signed) {
        if (data.length() < (bytes * 2) + start) {
            return 0;
        }
        if (bytes == 1) {
            String sub = data.substring(start, start + 2);
            if (signed) {
                return (long) Byte.valueOf((byte) Short.valueOf(sub, 16).shortValue()).byteValue();
            }
            return (long) Short.valueOf(sub, 16).shortValue();
        } else if (bytes == 2) {
            String low = data.substring(start, start + 2);
            String reverse = data.substring(start + 2, start + 4) + low;
            if (signed) {
                return (long) Short.valueOf((short) Integer.valueOf(reverse, 16).intValue()).shortValue();
            }
            return (long) Integer.valueOf(reverse, 16).intValue();
        } else if (bytes != 4) {
            return 0;
        } else {
            try {
                String byte1 = data.substring(start, start + 2);
                String byte2 = data.substring(start + 2, start + 4);
                String byte3 = data.substring(start + 4, start + 6);
                String reverse2 = data.substring(start + 6, start + 8) + byte3 + byte2 + byte1;
                if (signed) {
                    return (long) Integer.valueOf((int) Long.valueOf(reverse2, 16).longValue()).intValue();
                }
                return Long.valueOf(reverse2, 16).longValue();
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r5v34 int: [D('i' int), D('procedure' int)] */
    private void oppoGetInfoFromURC(String[] data, CriticalLogInfo CLInfo) {
        try {
            switch (Integer.parseInt(data[0])) {
                case 2:
                    logd("SYS_MTK_URC_FOR_DMFAPP");
                    CriticalLogInfo CLInfo_forDMF = new CriticalLogInfo(-1, -1, -1, "", "");
                    oppoGetDMFAppInfoFromURC(data[1], CLInfo_forDMF);
                    CLInfo.type = CLInfo_forDMF.type;
                    CLInfo.extra = CLInfo_forDMF.extra;
                    CLInfo.rat = CLInfo_forDMF.rat;
                    CLInfo.issue = CLInfo_forDMF.issue;
                    CLInfo.errcode = CLInfo_forDMF.errcode;
                    return;
                case 25:
                    if (getValueFromByte(data[1], 0, 1, true) == 1) {
                        long serv_cell_arfcn = getValueFromByte(data[1], 2, 2, true);
                        CLInfo.type = 11;
                        CLInfo.extra = String.valueOf(serv_cell_arfcn);
                        CLInfo.rat = (long) oppoGetRatFromType(11);
                        CLInfo.issue = oppoGetStringFromType(11);
                        return;
                    }
                    return;
                case 26:
                    long serv_cell_arfcn2 = getValueFromByte(data[1], 0, 2, true);
                    long serv_cell_band = getValueFromByte(data[1], 5, 1, true);
                    CLInfo.type = 12;
                    CLInfo.extra = String.valueOf(serv_cell_arfcn2);
                    CLInfo.rat = (long) oppoGetRatFromType(12);
                    CLInfo.issue = oppoGetStringFromType(12);
                    CLInfo.errcode = serv_cell_band;
                    return;
                case 89:
                    long event_type = getValueFromByte(data[1], 0, 4, true);
                    if (event_type == 0 || event_type == 1 || event_type == 4) {
                        CLInfo.type = 160;
                        CLInfo.errcode = event_type;
                        CLInfo.rat = (long) oppoGetRatFromType(160);
                        CLInfo.issue = oppoGetStringFromType(160);
                        return;
                    }
                    return;
                case SYS_OEM_NW_DIAG_CAUSE_LTE_A2_RELEASE_CELL /*{ENCODED_INT: 108}*/:
                    if (getValueFromByte(data[1], 0, 1, true) == 1) {
                        CLInfo.type = 210;
                        CLInfo.rat = (long) oppoGetRatFromType(210);
                        CLInfo.issue = oppoGetStringFromType(210);
                        return;
                    }
                    return;
                case SYS_MTK_URC_MT_RRC /*{ENCODED_INT: 133}*/:
                    long rrc_conn_status = getValueFromByte(data[1], 0, 1, true);
                    long est_cause = getValueFromByte(data[1], 2, 1, true);
                    if (rrc_conn_status == 3 && est_cause == 5) {
                        long rrc_cause = getValueFromByte(data[1], 4, 1, true);
                        CLInfo.type = 16;
                        CLInfo.errcode = rrc_cause;
                        CLInfo.rat = (long) oppoGetRatFromType(16);
                        CLInfo.issue = oppoGetStringFromType(16);
                        return;
                    }
                    return;
                case SYS_MTK_URC_TAU_DETACH_SERVICE_REJECT /*{ENCODED_INT: 233}*/:
                    long last_rcv_emm_msg_type = getValueFromByte(data[1], 2, 1, true);
                    if (last_rcv_emm_msg_type == 75) {
                        long tau_rej_type = getValueFromByte(data[1], 4, 1, true);
                        long tau_rej_cause = getValueFromByte(data[1], 6, 1, true);
                        CLInfo.type = 101;
                        CLInfo.errcode = (tau_rej_type << 8) | (255 & tau_rej_cause & 32767);
                        CLInfo.rat = 5;
                        CLInfo.issue = oppoGetStringFromType(101);
                        return;
                    } else if (last_rcv_emm_msg_type == 78) {
                        long sr_rej_cause = getValueFromByte(data[1], 6, 1, true);
                        CLInfo.type = 102;
                        CLInfo.errcode = sr_rej_cause;
                        CLInfo.rat = 5;
                        CLInfo.issue = oppoGetStringFromType(102);
                        return;
                    } else if (last_rcv_emm_msg_type == 69) {
                        long detach_type = getValueFromByte(data[1], 4, 1, true);
                        long detach_cause = getValueFromByte(data[1], 6, 1, true);
                        CLInfo.type = 104;
                        CLInfo.errcode = (detach_type << 8) | (255 & detach_cause & 32767);
                        CLInfo.rat = 5;
                        CLInfo.issue = oppoGetStringFromType(104);
                        return;
                    } else {
                        return;
                    }
                case SYS_MTK_URC_OUT_OF_CREDIT_REJECT /*{ENCODED_INT: 234}*/:
                    CLInfo.type = 89;
                    CLInfo.rat = (long) oppoGetRatFromType(89);
                    CLInfo.issue = oppoGetStringFromType(89);
                    CLInfo.errcode = 0;
                    return;
                case SYS_MTK_URC_MT_REJECT /*{ENCODED_INT: 256}*/:
                    long service_request_type = getValueFromByte(data[1], 0, 1, true);
                    long service_request_cause = getValueFromByte(data[1], 2, 1, true);
                    if (service_request_type == 2 && service_request_cause == 2) {
                        long ext_service_reject_cause = getValueFromByte(data[1], 8, 1, true);
                        CLInfo.type = 15;
                        CLInfo.errcode = ext_service_reject_cause;
                        CLInfo.rat = (long) oppoGetRatFromType(15);
                        CLInfo.issue = oppoGetStringFromType(15);
                        return;
                    }
                    return;
                case SYS_MTK_URC_LTE_NARROW_BANDWIDTH_SCELL /*{ENCODED_INT: 334}*/:
                    if (getValueFromByte(data[1], 0, 1, true) == 1) {
                        getValueFromByte(data[1], 8, 4, true);
                        getValueFromByte(data[1], 16, 4, true);
                        getValueFromByte(data[1], 24, 2, true);
                        getValueFromByte(data[1], 32, 4, true);
                        getValueFromByte(data[1], 40, 2, true);
                        long dl_bandwidth = getValueFromByte(data[1], 44, 2, true);
                        long ul_bandwidth = getValueFromByte(data[1], 48, 2, true);
                        getValueFromByte(data[1], 52, 1, true);
                        getValueFromByte(data[1], 54, 1, true);
                        getValueFromByte(data[1], 56, 4, true);
                        getValueFromByte(data[1], 64, 1, true);
                        long def_paging_cycle = getValueFromByte(data[1], 66, 1, true);
                        long band = getValueFromByte(data[1], 68, 2, true);
                        logd("oppoGetInfoFromURC, SYS_MTK_URC_LTE_NARROW_BANDWIDTH_SCELL,dl_bandwidth:" + dl_bandwidth + "ul_bandwidth:" + ul_bandwidth + "def_paging_cycle:" + def_paging_cycle + "band:" + band);
                        CLInfo.errcode = (def_paging_cycle << 48) | (band << 32) | (ul_bandwidth << 16) | dl_bandwidth;
                        CLInfo.type = 97;
                        CLInfo.rat = (long) oppoGetRatFromType(97);
                        CLInfo.issue = oppoGetStringFromType(97);
                        return;
                    }
                    return;
                case SYS_MTK_URC_PCI_MODE3_INTERFERENCE /*{ENCODED_INT: 355}*/:
                    if (getValueFromByte(data[1], 0, 1, true) == 1) {
                        long earfcn = getValueFromByte(data[1], 8, 4, false);
                        long pci = getValueFromByte(data[1], 16, 2, true);
                        long rsrp = getValueFromByte(data[1], 24, 4, true);
                        long rsrq = getValueFromByte(data[1], 32, 4, true);
                        getValueFromByte(data[1], 40, 4, false);
                        getValueFromByte(data[1], 48, 2, true);
                        getValueFromByte(data[1], 56, 4, true);
                        getValueFromByte(data[1], 64, 4, true);
                        getValueFromByte(data[1], 72, 4, false);
                        getValueFromByte(data[1], 80, 2, true);
                        getValueFromByte(data[1], 88, 4, true);
                        getValueFromByte(data[1], 96, 4, true);
                        getValueFromByte(data[1], 104, 4, false);
                        getValueFromByte(data[1], SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR, 2, true);
                        getValueFromByte(data[1], SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT, 4, true);
                        getValueFromByte(data[1], NetworkDiagnoseUtils.RF_GSM_850_ARFCN_BOTTOM, 4, true);
                        CLInfo.errcode = (rsrq << 56) | (rsrp << 48) | (pci << 32) | earfcn;
                        CLInfo.type = 109;
                        CLInfo.rat = (long) oppoGetRatFromType(109);
                        CLInfo.issue = oppoGetStringFromType(109);
                        return;
                    }
                    return;
                case SYS_MTK_URC_LTE_RRC_ABNORMAL_BAR /*{ENCODED_INT: 356}*/:
                    long em_bar_cause = getValueFromByte(data[1], 0, 1, true);
                    long bar_time = getValueFromByte(data[1], 8, 4, true);
                    long ext_wait_time = getValueFromByte(data[1], 16, 4, true);
                    long Is_bar_time_valid = getValueFromByte(data[1], 24, 1, true);
                    long Is_ext_wait_time_valid = getValueFromByte(data[1], 26, 1, true);
                    logd("oppoGetInfoFromURC, SYS_MTK_URC_LTE_RRC_ABNORMAL_BAR, em_bar_cause:" + em_bar_cause + "bar_time:" + bar_time + "ext_wait_time:" + ext_wait_time + "Is_bar_time_valid:" + Is_bar_time_valid);
                    CLInfo.errcode = (ext_wait_time << 27) | (Is_ext_wait_time_valid << 26) | (bar_time << 9) | (Is_bar_time_valid << 8) | em_bar_cause;
                    CLInfo.type = 106;
                    CLInfo.rat = (long) oppoGetRatFromType(106);
                    CLInfo.issue = oppoGetStringFromType(106);
                    return;
                case SYS_MTK_URC_LTE_HANDOVER_FAILURE /*{ENCODED_INT: 357}*/:
                    CLInfo.errcode = getValueFromByte(data[1], 0, 1, true);
                    CLInfo.type = 105;
                    CLInfo.rat = (long) oppoGetRatFromType(105);
                    CLInfo.issue = oppoGetStringFromType(105);
                    return;
                case SYS_MTK_URC_MT_CSFB /*{ENCODED_INT: 393}*/:
                    if (getValueFromByte(data[1], 2, 1, true) == 1) {
                        long Is_mt_csfb_lu_needed = getValueFromByte(data[1], 0, 1, true);
                        CLInfo.type = 14;
                        CLInfo.extra = String.valueOf(Is_mt_csfb_lu_needed);
                        CLInfo.rat = (long) oppoGetRatFromType(14);
                        CLInfo.issue = oppoGetStringFromType(14);
                        return;
                    }
                    return;
                case SYS_MTK_URC_REG_REJECT /*{ENCODED_INT: 394}*/:
                    long lu_type = getValueFromByte(data[1], 2, 1, true);
                    long attach_type = getValueFromByte(data[1], 6, 1, true);
                    long rau_type = getValueFromByte(data[1], 10, 1, true);
                    if (attach_type == 5 && rau_type == 4 && lu_type != 3) {
                        long lu_rej_cause = getValueFromByte(data[1], 4, 1, true);
                        long current_rat = getValueFromByte(data[1], 0, 1, true);
                        CLInfo.type = 63;
                        CLInfo.errcode = lu_rej_cause;
                        CLInfo.rat = current_rat;
                        CLInfo.extra = "LUR";
                        CLInfo.issue = oppoGetStringFromType(63);
                        return;
                    } else if (lu_type == 3 && rau_type == 4 && attach_type != 5) {
                        long attach_rej_cause = getValueFromByte(data[1], 8, 1, true);
                        long current_rat2 = getValueFromByte(data[1], 0, 1, true);
                        CLInfo.type = 63;
                        CLInfo.errcode = attach_rej_cause;
                        CLInfo.rat = current_rat2;
                        CLInfo.extra = "GAR";
                        CLInfo.issue = oppoGetStringFromType(63);
                        return;
                    } else if (lu_type == 3 && attach_type == 5 && rau_type != 4) {
                        long rau_rej_cause = getValueFromByte(data[1], 12, 1, true);
                        long current_rat3 = getValueFromByte(data[1], 0, 1, true);
                        CLInfo.type = 63;
                        CLInfo.errcode = rau_rej_cause;
                        CLInfo.rat = current_rat3;
                        CLInfo.extra = "RAUR";
                        CLInfo.issue = oppoGetStringFromType(63);
                        return;
                    } else {
                        return;
                    }
                case SYS_MTK_URC_AUTHENTICATION_REJECT /*{ENCODED_INT: 395}*/:
                    long auth_rej_type = getValueFromByte(data[1], 2, 1, true);
                    long current_rat4 = getValueFromByte(data[1], 0, 1, true);
                    CLInfo.type = 64;
                    CLInfo.errcode = auth_rej_type;
                    CLInfo.rat = current_rat4;
                    CLInfo.issue = oppoGetStringFromType(64);
                    return;
                case SYS_MTK_URC_DATA_RLC_UL_RLF /*{ENCODED_INT: 400}*/:
                    CLInfo.type = 127;
                    CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_RLC_UL_RLF);
                    CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_RLC_UL_RLF);
                    return;
                case 401:
                    long tmr_type = getValueFromByte(data[1], 0, 1, true);
                    long timer_length = getValueFromByte(data[1], 8, 4, true);
                    logd("oppoGetInfoFromURC, SYS_MTK_URC_LTE_RRC_ABNORMAL_TIMEOUT, tmr_type:" + tmr_type + "timer_length:" + timer_length);
                    CLInfo.errcode = (tmr_type << 32) | timer_length;
                    CLInfo.type = 107;
                    CLInfo.rat = (long) oppoGetRatFromType(107);
                    CLInfo.issue = oppoGetStringFromType(107);
                    return;
                case 402:
                    long endtime = SystemClock.elapsedRealtime();
                    if (PhoneFactory.getPhone(this.mPhoneId).getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
                        this.conn_large_bw_cell_stayed_time += getValueFromByte(data[1], 0, 4, false);
                        this.conn_small_bw_cell_stayed_time += getValueFromByte(data[1], 8, 4, false);
                        this.conn_change_to_large_bw_cell_count += getValueFromByte(data[1], 16, 2, false);
                        this.conn_change_to_small_bw_cell_count += getValueFromByte(data[1], 20, 2, false);
                        this.conn_block_to_small_bw_cell_count += getValueFromByte(data[1], 24, 2, false);
                        this.conn_accelerate_to_large_bw_cell_count += getValueFromByte(data[1], 28, 2, false);
                        this.large_bw_cell_stayed_time += getValueFromByte(data[1], 32, 4, false);
                        this.small_bw_cell_stayed_time += getValueFromByte(data[1], 40, 4, false);
                        this.change_to_large_bw_cell_count += getValueFromByte(data[1], 48, 2, false);
                        this.change_to_small_bw_cell_count += getValueFromByte(data[1], 52, 2, false);
                        this.block_to_small_bw_cell_count += getValueFromByte(data[1], 56, 2, false);
                        this.accelerate_to_large_bw_cell_count += getValueFromByte(data[1], 60, 2, false);
                        logd("oppoGetInfoFromURC, SYS_MTK_URC_LTE_NARROW_BW_MONITORING,conn_large_bw_cell_stayed_time:" + this.conn_large_bw_cell_stayed_time + "conn_small_bw_cell_stayed_time:" + this.conn_small_bw_cell_stayed_time + "conn_change_to_large_bw_cell_count:" + this.conn_change_to_large_bw_cell_count + "conn_block_to_small_bw_cell_count:" + this.conn_block_to_small_bw_cell_count + "conn_accelerate_to_large_bw_cell_count:" + this.conn_accelerate_to_large_bw_cell_count);
                        long j = this.small_bw_time;
                        if (j > 0 && endtime - j > 300000) {
                            CLInfo.errcode = ((this.conn_large_bw_cell_stayed_time / 1000) << 48) | ((this.conn_small_bw_cell_stayed_time / 1000) << 32) | (this.conn_change_to_large_bw_cell_count << 16) | (this.conn_block_to_small_bw_cell_count << 12) | (this.conn_accelerate_to_large_bw_cell_count << 8) | (this.block_to_small_bw_cell_count << 4) | this.accelerate_to_large_bw_cell_count;
                            CLInfo.type = 129;
                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING);
                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING);
                            this.conn_large_bw_cell_stayed_time = 0;
                            this.conn_small_bw_cell_stayed_time = 0;
                            this.conn_change_to_large_bw_cell_count = 0;
                            this.conn_change_to_small_bw_cell_count = 0;
                            this.conn_block_to_small_bw_cell_count = 0;
                            this.conn_accelerate_to_large_bw_cell_count = 0;
                            this.large_bw_cell_stayed_time = 0;
                            this.small_bw_cell_stayed_time = 0;
                            this.change_to_large_bw_cell_count = 0;
                            this.change_to_small_bw_cell_count = 0;
                            this.block_to_small_bw_cell_count = 0;
                            this.accelerate_to_large_bw_cell_count = 0;
                            this.small_bw_time = endtime;
                            return;
                        } else if (this.small_bw_time == 0) {
                            CLInfo.errcode = ((this.conn_large_bw_cell_stayed_time / 1000) << 48) | ((this.conn_small_bw_cell_stayed_time / 1000) << 32) | (this.conn_change_to_large_bw_cell_count << 16) | (this.conn_block_to_small_bw_cell_count << 12) | (this.conn_accelerate_to_large_bw_cell_count << 8) | (this.block_to_small_bw_cell_count << 4) | this.accelerate_to_large_bw_cell_count;
                            CLInfo.type = 129;
                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING);
                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING);
                            this.small_bw_time = endtime;
                            return;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case SYS_MTK_URC_LTE_REG_REJECT /*{ENCODED_INT: 625}*/:
                    long emm_attach_rej_cause = getValueFromByte(data[1], 2, 1, true);
                    CLInfo.type = 61;
                    CLInfo.errcode = emm_attach_rej_cause;
                    CLInfo.rat = (long) oppoGetRatFromType(61);
                    CLInfo.issue = oppoGetStringFromType(61);
                    return;
                case SYS_MTK_URC_LTE_AUTHENTICATION_REJECT /*{ENCODED_INT: 628}*/:
                    long is_auth_rej = getValueFromByte(data[1], 0, 1, true);
                    if (is_auth_rej == 1) {
                        CLInfo.type = 68;
                        CLInfo.errcode = is_auth_rej;
                        CLInfo.rat = (long) oppoGetRatFromType(68);
                        CLInfo.issue = oppoGetStringFromType(68);
                        return;
                    }
                    return;
                case SYS_MTK_URC_SCREEN_ON_TRIGGER_NW_SRCH /*{ENCODED_INT: 659}*/:
                    long result = getValueFromByte(data[1], 0, 1, true);
                    logd("oppoGetInfoFromURC, SYS_MTK_URC_SCREEN_ON_TRIGGER_NW_SRCH: " + result);
                    if (result > 0) {
                        CLInfo.errcode = result;
                        CLInfo.type = 92;
                        CLInfo.rat = (long) oppoGetRatFromType(92);
                        CLInfo.issue = oppoGetStringFromType(92);
                        return;
                    }
                    return;
                case SYS_MTK_URC_DATA_PDCP_UL_ONE_WAY_PASS /*{ENCODED_INT: 947}*/:
                    CLInfo.type = 122;
                    CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_ONE_WAY_PASS);
                    CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_ONE_WAY_PASS);
                    return;
                case SYS_MTK_URC_DATA_PDCP_UL_TIMEOUT /*{ENCODED_INT: 948}*/:
                    CLInfo.type = 120;
                    CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT);
                    CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT);
                    return;
                case SYS_MTK_URC_DATA_PDCP_UL_OUT_OF_BUFFER /*{ENCODED_INT: 949}*/:
                    CLInfo.type = 121;
                    CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_OUT_OF_BUFFER);
                    CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_OUT_OF_BUFFER);
                    return;
                case SYS_MTK_URC_DATA_DL_HIGH_BLER /*{ENCODED_INT: 970}*/:
                    CLInfo.type = 123;
                    CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_DL_HIGH_BLER);
                    CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_DL_HIGH_BLER);
                    return;
                case SYS_MTK_URC_PDP_DEACT_ERROR /*{ENCODED_INT: 1018}*/:
                    CLInfo.errcode = (getValueFromByte(data[1], 0, 4, true) << 16) | (65535 & getValueFromByte(data[1], 8, 2, true));
                    CLInfo.type = 125;
                    CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_PDP_DEACT_ERROR);
                    CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_PDP_DEACT_ERROR);
                    return;
                case SYS_MTK_URC_IA_APN_ERROR /*{ENCODED_INT: 1019}*/:
                    CLInfo.errcode = (getValueFromByte(data[1], 0, 4, true) << 16) | (65535 & getValueFromByte(data[1], 8, 2, true));
                    CLInfo.type = 117;
                    CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_IA_APN_ERROR);
                    CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_IA_APN_ERROR);
                    return;
                case 10000:
                    logd("SYS_MTK_URC_OUT_OF_CREDIT_REJECT_FOR_DMFAPP");
                    String[] rowdata = data[1].split(",", -1);
                    int length = rowdata.length;
                    int[] rowdata_int = new int[length];
                    for (int i = 0; i < length; i++) {
                        rowdata_int[i] = Integer.parseInt(rowdata[i]);
                    }
                    int procedure = rowdata_int[1];
                    int reject_cause = rowdata_int[2];
                    CLInfo.type = 89;
                    CLInfo.rat = (long) oppoGetRatFromType(89);
                    CLInfo.issue = oppoGetStringFromType(89);
                    CLInfo.errcode = (long) ((reject_cause * 10) + procedure);
                    return;
                default:
                    CLInfo.type = -1;
                    return;
            }
        } catch (NumberFormatException e) {
            logd("Return EM_ID type error");
        } catch (Exception e2) {
            logd(e2.toString());
        }
    }

    /* JADX INFO: Multiple debug info for r8v2 long: [D('emm_mm_cause' long), D('mt_csfb_call_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r10v2 long: [D('errc_rrc_rr_cause' long), D('mt_csfb_signal_quality' long)] */
    /* JADX INFO: Multiple debug info for r6v4 long: [D('mt_csfb_call_drop_mnc' long), D('mt_csfb_call_drop_mcc' long)] */
    /* JADX INFO: Multiple debug info for r6v5 long: [D('mt_csfb_call_drop_mnc' long), D('mt_csfb_call_drop_lac' long)] */
    /* JADX INFO: Multiple debug info for r6v6 long: [D('mt_csfb_call_drop_lac' long), D('mt_csfb_call_drop_cell' long)] */
    /* JADX INFO: Multiple debug info for r8v4 long: [D('mt_cs_emm_mm_cause' long), D('mt_cs_call_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r5v4 long: [D('mt_call_drop_mcc' long), D('mt_call_drop_mnc' long)] */
    /* JADX INFO: Multiple debug info for r5v5 long: [D('mt_call_drop_mnc' long), D('mt_call_drop_lac' long)] */
    /* JADX INFO: Multiple debug info for r5v6 long: [D('mt_call_drop_cell' long), D('mt_call_drop_lac' long)] */
    /* JADX INFO: Multiple debug info for r6v19 long: [D('current_rat_for_cs_call_drop' long), D('cs_emm_mm_cause' long)] */
    /* JADX INFO: Multiple debug info for r8v6 long: [D('cs_errc_rrc_rr_cause' long), D('cs_call_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r10v8 long: [D('cs_call_signal_quality' long), D('cs_call_drop_mcc' long)] */
    /* JADX INFO: Multiple debug info for r4v27 long: [D('cs_call_drop_lac' long), D('cs_call_drop_cell' long)] */
    /* JADX INFO: Multiple debug info for r0v19 long: [D('c2k_mt_call_drop_sid' long), D('rssi' long)] */
    /* JADX INFO: Multiple debug info for r0v20 long: [D('c2k_mt_call_drop_nid' long), D('c2k_mt_call_drop_sid' long)] */
    /* JADX INFO: Multiple debug info for r0v21 long: [D('c2k_mt_call_drop_nid' long), D('c2k_mt_call_drop_pn_offset' long)] */
    /* JADX INFO: Multiple debug info for r0v22 long: [D('c2k_mt_call_drop_bid' long), D('c2k_mt_call_drop_pn_offset' long)] */
    /* JADX INFO: Multiple debug info for r10v16 long: [D('irf_tac' long), D('irf_pdn_cause' long)] */
    /* JADX INFO: Multiple debug info for r10v17 long: [D('irf_pdn_cause' long), D('irf_rat' long)] */
    /* JADX INFO: Multiple debug info for r8v24 long: [D('ho_fail_signal_strength' long), D('ho_mcc' long)] */
    /* JADX INFO: Multiple debug info for r10v19 long: [D('ho_mnc' long), D('ho_fail_signal_quality' long)] */
    /* JADX INFO: Multiple debug info for r0v53 long: [D('ho_cell' long), D('ho_tac' long)] */
    /* JADX INFO: Multiple debug info for r8v26 long: [D('bar_signal_strength' long), D('bar_time' long)] */
    /* JADX INFO: Multiple debug info for r10v21 long: [D('bar_signal_quality' long), D('ext_wait_time' long)] */
    /* JADX INFO: Multiple debug info for r10v22 long: [D('ext_wait_time' long), D('bar_mcc' long)] */
    /* JADX INFO: Multiple debug info for r8v28 long: [D('time_length' long), D('reestablishment_fail_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r10v24 long: [D('reestablishment_fail_signal_quality' long), D('reestablishment_fail_mcc' long)] */
    /* JADX INFO: Multiple debug info for r8v30 long: [D('nbr_cell_number' long), D('mode3_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r8v31 long: [D('nbr_cell_number' long), D('mode3_mcc' long)] */
    /* JADX INFO: Multiple debug info for r10v26 long: [D('mode3_signal_quality' long), D('mode3_mnc' long)] */
    /* JADX INFO: Multiple debug info for r0v80 long: [D('mode3_tac' long), D('mode3_cell' long)] */
    /* JADX INFO: Multiple debug info for r8v34 long: [D('ul_bandwidth' long), D('signal_strength_nb' long)] */
    /* JADX INFO: Multiple debug info for r10v29 long: [D('narrow_bandwidth_mcc' long), D('signal_quality_nb' long)] */
    /* JADX INFO: Multiple debug info for r3v38 long: [D('fake_ind' long), D('fake_cell_bar_protocal_id' long)] */
    /* JADX INFO: Multiple debug info for r3v39 long: [D('fake_ind' long), D('fake_cell_mcc' long)] */
    /* JADX INFO: Multiple debug info for r3v40 long: [D('fake_cell_mcc' long), D('fake_cell_mnc' long)] */
    /* JADX INFO: Multiple debug info for r3v41 long: [D('fake_cell_mnc' long), D('fake_cell_tac' long)] */
    /* JADX INFO: Multiple debug info for r13v20 long: [D('apn_error_cell' long), D('apn_error_tac' long)] */
    /* JADX INFO: Multiple debug info for r3v61 long: [D('pdp_deact_error_cell' long), D('pdp_deact_error_tac' long)] */
    /* JADX INFO: Multiple debug info for r13v37 long: [D('pdp_act_error_tac' long), D('pdp_act_error_cell' long)] */
    /* JADX INFO: Multiple debug info for r13v53 long: [D('lu_rej_lac' long), D('lu_ori_rej_cause' long)] */
    /* JADX INFO: Multiple debug info for r10v41 long: [D('lu_rej_mnc' long), D('lu_rej_cell' long)] */
    /* JADX INFO: Multiple debug info for r13v56 long: [D('rau_rej_lac' long), D('rau_ori_rej_cause' long)] */
    /* JADX INFO: Multiple debug info for r10v52 long: [D('rau_rej_cell' long), D('rau_rej_mnc' long)] */
    /* JADX INFO: Multiple debug info for r13v59 long: [D('tau_ori_rej_cause' long), D('tau_rej_tac' long)] */
    /* JADX INFO: Multiple debug info for r10v63 long: [D('tau_rej_cell' long), D('tau_rej_mnc' long)] */
    /* JADX INFO: Multiple debug info for r13v62 long: [D('attach_rej_tac' long), D('attach_ori_rej_cause' long)] */
    /* JADX INFO: Multiple debug info for r10v72 long: [D('attach_rej_mnc' long), D('attach_rej_cell' long)] */
    /* JADX INFO: Multiple debug info for r3v106 long: [D('authen_rej_tac' long), D('authen_rej_cell' long)] */
    /* JADX INFO: Multiple debug info for r3v125 long: [D('service_rej_tac' long), D('service_rej_mnc' long)] */
    /* JADX INFO: Multiple debug info for r3v126 long: [D('service_rej_tac' long), D('service_ori_rej_cause' long)] */
    /* JADX INFO: Multiple debug info for r13v65 long: [D('service_rej_cell' long), D('service_rej_mcc' long)] */
    /* JADX INFO: Multiple debug info for r13v74 long: [D('detach_tac' long), D('detach_ori_rej_cause' long)] */
    /* JADX INFO: Multiple debug info for r10v86 long: [D('detach_mcc' long), D('detach_cell' long)] */
    /* JADX INFO: Multiple debug info for r14v11 long: [D('pdcp_ul_timeout_cell' long), D('pdcp_ul_timeout_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r14v12 long: [D('pdcp_ul_timeout_signal_strength' long), D('pdcp_ul_timeout_signal_quality' long)] */
    /* JADX INFO: Multiple debug info for r14v13 long: [D('pdcp_ul_timeout_signal_quality' long), D('pdcp_ul_timeout_ims_exit_flag' long)] */
    /* JADX INFO: Multiple debug info for r14v18 long: [D('pdcp_ul_out_of_buf_cell' long), D('pdcp_ul_out_of_buf_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r14v19 long: [D('pdcp_ul_out_of_buf_signal_quality' long), D('pdcp_ul_out_of_buf_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r14v20 long: [D('pdcp_ul_out_of_buf_signal_quality' long), D('pdcp_ul_out_of_buf_ims_exit_flag' long)] */
    /* JADX INFO: Multiple debug info for r14v25 long: [D('pdcp_ul_one_way_pass_cell' long), D('pdcp_ul_one_way_pass_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r14v26 long: [D('pdcp_ul_one_way_pass_signal_quality' long), D('pdcp_ul_one_way_pass_signal_strength' long)] */
    /* JADX INFO: Multiple debug info for r14v27 long: [D('pdcp_ul_one_way_pass_signal_quality' long), D('pdcp_ul_one_way_pass_ims_exit_flag' long)] */
    /* JADX INFO: Multiple debug info for r14v35 long: [D('dl_high_bler_signal_strength' long), D('dl_high_bler_cell' long)] */
    /* JADX INFO: Multiple debug info for r14v36 long: [D('dl_high_bler_signal_strength' long), D('dl_high_bler_signal_quality' long)] */
    /* JADX INFO: Multiple debug info for r14v37 long: [D('dl_high_bler_ims_exit_flag' long), D('dl_high_bler_signal_quality' long)] */
    /* JADX INFO: Multiple debug info for r14v43 long: [D('vcd_cid' long), D('vcd_ss' long)] */
    /* JADX INFO: Multiple debug info for r14v44 long: [D('vcd_sq' long), D('vcd_ss' long)] */
    /* JADX INFO: Multiple debug info for r14v45 long: [D('vcd_sq' long), D('vcd_rat' long)] */
    /* JADX INFO: Multiple debug info for r8v72 long: [D('vcd_call_type' long), D('vcd_tac' long)] */
    /* JADX INFO: Multiple debug info for r8v73 long: [D('vcd_call_type' long), D('vcd_is_mt_call' long)] */
    /* JADX INFO: Multiple debug info for r5v76 long: [D('vcd_mnc' long), D('vcd_sip_call_state' long)] */
    /* JADX INFO: Multiple debug info for r5v77 long: [D('vcd_sip_call_state' long), D('vcd_net_type' long)] */
    /* JADX INFO: Multiple debug info for r5v78 long: [D('vcd_net_type' long), D('vcd_sip_cause' long)] */
    /* JADX INFO: Multiple debug info for r3v162 long: [D('vcd_mcc' long), D('vcd_emm_mm_cause' long)] */
    /* JADX INFO: Multiple debug info for r3v163 long: [D('vcd_errc_rrc_rr_cause' long), D('vcd_emm_mm_cause' long)] */
    /* JADX INFO: Multiple debug info for r3v164 long: [D('vcd_errc_rrc_rr_cause' long), D('vcd_ori_errc_rrc_rr_cause' long)] */
    /* JADX INFO: Multiple debug info for r9v15 long: [D('lte_snr' long), D('gwl_call_quality_status' long)] */
    /* JADX INFO: Multiple debug info for r5v83 long: [D('gwl_call_quality_signal_strength' long), D('gwl_call_quality_mnc' long)] */
    /* JADX INFO: Multiple debug info for r3v198 long: [D('gwl_call_quality_lac' long), D('gwl_call_quality_signal_quality' long)] */
    /* JADX INFO: Multiple debug info for r14v48 long: [D('gwl_call_quality_cell' long), D('gwl_call_dl_have_sound' long)] */
    /* JADX INFO: Multiple debug info for r5v86 long: [D('c2k_call_quality_ecio' long), D('c2k_call_quality_mnc' long)] */
    /* JADX INFO: Multiple debug info for r3v206 long: [D('c2k_call_quality_rssi' long), D('c2k_call_quality_nid' long)] */
    /* JADX INFO: Multiple debug info for r3v207 long: [D('c2k_call_quality_nid' long), D('c2k_call_quality_bid' long)] */
    /* JADX INFO: Multiple debug info for r3v208 long: [D('c2k_call_dl_have_sound' long), D('c2k_call_quality_bid' long)] */
    /* JADX INFO: Multiple debug info for r3v262 long: [D('mtk_urc_type_for_dmf' int), D('hold_a2_with_tau_num' long)] */
    /* JADX INFO: Multiple debug info for r5v100 long: [D('hold_tmo_cnt_on_others' long), D('length' int)] */
    private void oppoGetDMFAppInfoFromURC(String data, CriticalLogInfo CLInfo) {
        long cs_call_signal_strength;
        String[] rowdata = data.split(",", -1);
        int length = rowdata.length;
        int[] rowdata_int = new int[length];
        for (int i = 0; i < length; i++) {
            if (!(i == 1 || i == 2)) {
                rowdata_int[i] = Integer.parseInt(rowdata[i]);
            }
        }
        int mtk_urc_type_for_dmf = rowdata_int[0];
        if (mtk_urc_type_for_dmf == SYS_MTK_URC_CALL_MT_CSFB_CALL_DROP_FOR_DMFAPP) {
            long current_rat_for_mt_csfb_call_drop = (long) rowdata_int[3];
            long mt_csfb_call_signal_strength = (long) rowdata_int[7];
            long mt_csfb_signal_quality = (long) rowdata_int[8];
            long cc_cause = (long) rowdata_int[9];
            long mt_csfb_call_signal_strength2 = (long) rowdata_int[10];
            long mt_csfb_signal_quality2 = (long) rowdata_int[11];
            long mt_csfb_state = (long) rowdata_int[12];
            long mt_csfb_call_drop_mcc = (long) rowdata_int[13];
            long mt_csfb_call_drop_mcc2 = (long) rowdata_int[14];
            long mt_csfb_call_drop_lac = (long) rowdata_int[16];
            long mt_csfb_call_drop_cell = (long) Integer.parseInt(rowdata[2]);
            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_DATA_MT_CSFB_CALL_DROP_FOR_DMFAPP,mt_csfb_state:" + mt_csfb_state + "emm_mm_cause:" + mt_csfb_call_signal_strength2 + "errc_rrc_rr_cause:" + mt_csfb_signal_quality2);
            CLInfo.errcode = (mt_csfb_state << 48) | (cc_cause << 32) | (mt_csfb_call_signal_strength2 << 16) | (mt_csfb_signal_quality2 & 65535);
            CLInfo.rat = current_rat_for_mt_csfb_call_drop;
            CLInfo.issue = oppoGetStringFromType(19);
            CLInfo.type = 19;
            CLInfo.extra = "MCC:" + mt_csfb_call_drop_mcc + ",MNC:" + mt_csfb_call_drop_mcc2 + ",LAC:" + mt_csfb_call_drop_lac + ",CID:" + mt_csfb_call_drop_cell + ",signalquality:" + mt_csfb_signal_quality + ",signalstrength:" + mt_csfb_call_signal_strength + ",mt_csfb_state:" + mt_csfb_state + ",cc_cause:" + cc_cause + ",emm_mm_cause:" + mt_csfb_call_signal_strength2 + ",errc_rrc_rr_cause:" + mt_csfb_signal_quality2;
        } else if (mtk_urc_type_for_dmf == SYS_MTK_URC_CALL_MT_CS_CALL_DROP_FOR_DMFAPP) {
            long current_rat_for_mt_cs_call_drop = (long) rowdata_int[3];
            long mt_cs_call_signal_strength = (long) rowdata_int[7];
            long mt_cs_call_signal_quality = (long) rowdata_int[8];
            long mt_cs_cc_cause = (long) rowdata_int[9];
            long mt_cs_call_signal_strength2 = (long) rowdata_int[10];
            long mt_cs_errc_rrc_rr_cause = (long) rowdata_int[11];
            long mt_call_drop_mcc = (long) rowdata_int[12];
            long mt_call_drop_mnc = (long) rowdata_int[13];
            long mt_call_drop_lac = (long) rowdata_int[15];
            long mt_call_drop_lac2 = (long) Integer.parseInt(rowdata[2]);
            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_DATA_MT_CS_CALL_DROP_FOR_DMFAPP,mt_cs_cc_cause:" + mt_cs_cc_cause + "mt_cs_emm_mm_cause:" + mt_cs_call_signal_strength2 + "mt_cs_errc_rrc_rr_cause:" + mt_cs_errc_rrc_rr_cause);
            CLInfo.errcode = (mt_cs_call_signal_quality << 48) | (mt_cs_cc_cause << 32) | (mt_cs_call_signal_strength2 << 16) | (mt_cs_errc_rrc_rr_cause & 65535);
            CLInfo.rat = current_rat_for_mt_cs_call_drop;
            CLInfo.issue = oppoGetStringFromType(19);
            CLInfo.type = 19;
            CLInfo.extra = "MCC:" + mt_call_drop_mcc + ",MNC:" + mt_call_drop_mnc + ",LAC:" + mt_call_drop_lac + ",CID:" + mt_call_drop_lac2 + ",signalquality:" + mt_cs_call_signal_quality + ",signalstrength:" + mt_cs_call_signal_strength + ",mt_cs_calldrop_cc_cause:" + mt_cs_cc_cause + ",mt_cs_calldrop_emm_mm_cause:" + mt_cs_call_signal_strength2 + ",mt_cs_calldrop_errc_rrc_rr_cause:" + mt_cs_errc_rrc_rr_cause;
        } else if (mtk_urc_type_for_dmf == SYS_MTK_URC_CALL_CS_CALL_DROP_FOR_DMFAPP) {
            long current_rat_for_cs_call_drop = (long) rowdata_int[3];
            long cs_call_signal_strength2 = (long) rowdata_int[7];
            long cs_call_signal_quality = (long) rowdata_int[8];
            long cs_cc_cause = (long) rowdata_int[9];
            long cs_emm_mm_cause = (long) rowdata_int[10];
            long cs_call_signal_strength3 = (long) rowdata_int[11];
            long cs_call_drop_mcc = (long) rowdata_int[12];
            long cs_call_drop_mnc = (long) rowdata_int[13];
            long cs_call_drop_lac = (long) rowdata_int[15];
            long cs_call_drop_cell = (long) Integer.parseInt(rowdata[2]);
            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_DATA_MT_CS_CALL_DROP_FOR_DMFAPP,cs_cc_cause:" + cs_cc_cause + ",cs_emm_mm_cause:" + cs_emm_mm_cause + ",cs_errc_rrc_rr_cause:" + cs_call_signal_strength3);
            CLInfo.extra = "MCC:" + cs_call_drop_mcc + ",MNC:" + cs_call_drop_mnc + ",LAC:" + cs_call_drop_lac + ",CID:" + cs_call_drop_cell + ",signalquality:" + cs_call_signal_quality + ",signalstrength:" + cs_call_signal_strength2 + ",cs_errc_rrc_rr_cause:" + cs_call_signal_strength3 + ",cs_emm_mm_cause:" + cs_emm_mm_cause;
            if (803 == cs_call_signal_strength3 || 801 == cs_call_signal_strength3 || 404 == cs_call_signal_strength3 || 104 == cs_call_signal_strength3 || 105 == cs_call_signal_strength3 || 106 == cs_call_signal_strength3 || 107 == cs_call_signal_strength3 || 154 == cs_call_signal_strength3 || 156 == cs_call_signal_strength3 || 157 == cs_call_signal_strength3 || 158 == cs_call_signal_strength3) {
                cs_call_signal_strength = current_rat_for_cs_call_drop;
            } else if (161 == cs_call_signal_strength3) {
                cs_call_signal_strength = current_rat_for_cs_call_drop;
            } else if (802 == cs_call_signal_strength3 || 401 == cs_call_signal_strength3) {
                CLInfo.errcode = (cs_call_signal_quality << 48) | (cs_call_signal_strength3 << 32) | (cs_emm_mm_cause << 16) | (cs_call_signal_strength3 & 65535);
                CLInfo.rat = current_rat_for_cs_call_drop;
                CLInfo.issue = oppoGetStringFromType(11);
                CLInfo.type = 11;
                return;
            } else {
                return;
            }
            CLInfo.errcode = (cs_call_signal_quality << 48) | (cs_call_signal_strength3 << 32) | (cs_emm_mm_cause << 16) | (65535 & cs_call_signal_strength3);
            CLInfo.rat = cs_call_signal_strength;
            CLInfo.issue = oppoGetStringFromType(12);
            CLInfo.type = 12;
        } else if (mtk_urc_type_for_dmf == SYS_MTK_URC_CALL_MT_C2K_CALL_DROP_FOR_DMFAPP) {
            long rssi = (long) rowdata_int[8];
            long ecno = (long) rowdata_int[9];
            long c2k_cause = (long) rowdata_int[10];
            long c2k_mt_call_drop_mcc = (long) Integer.parseInt(rowdata[1]);
            long rssi2 = (long) rowdata_int[3];
            long c2k_mt_call_drop_sid = (long) rowdata_int[4];
            long c2k_mt_call_drop_pn_offset = (long) rowdata_int[5];
            long c2k_mt_call_drop_pn_offset2 = (long) rowdata_int[11];
            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_DATA_MT_C2K_CALL_DROP_FOR_DMFAPP, c2k_cause:" + c2k_cause);
            CLInfo.errcode = (ecno << 16) | (c2k_cause & 65535);
            CLInfo.rat = 3;
            CLInfo.issue = oppoGetStringFromType(19);
            CLInfo.type = 19;
            CLInfo.extra = "MCC:" + c2k_mt_call_drop_mcc + ",MNC:" + ((long) Integer.parseInt(rowdata[2])) + ",SID:" + rssi2 + ",NID:" + c2k_mt_call_drop_sid + ",BID:" + c2k_mt_call_drop_pn_offset2 + ",signalstrength:" + rssi + ",signalquality:" + ecno + ",c2k_cause:" + c2k_cause + ",pn_offset:" + c2k_mt_call_drop_pn_offset;
        } else if (mtk_urc_type_for_dmf == SYS_MTK_URC_CARD_DROP_FOR_DMFAPP) {
            rowdata_int[1] = Integer.parseInt(rowdata[1]);
            CLInfo.type = 160;
            CLInfo.errcode = (long) rowdata_int[1];
            CLInfo.rat = (long) oppoGetRatFromType(160);
            CLInfo.issue = oppoGetStringFromType(160);
        } else if (mtk_urc_type_for_dmf == SYS_MTK_URC_RF_MIPI_HW_FAILED_FOR_DMFAPP) {
            rowdata_int[1] = Integer.parseInt(rowdata[1]);
            if (((long) rowdata_int[1]) == 1) {
                CLInfo.type = 210;
                CLInfo.rat = (long) oppoGetRatFromType(210);
                CLInfo.issue = oppoGetStringFromType(210);
            }
        } else if (mtk_urc_type_for_dmf != SYS_MTK_URC_IMS_REG_FAIL_FOR_DMFAPP) {
            switch (mtk_urc_type_for_dmf) {
                case 101:
                    long frequency = (long) rowdata_int[6];
                    long ho_fail_signal_strength = (long) rowdata_int[7];
                    long ho_fail_signal_quality = (long) rowdata_int[8];
                    long ho_result = (long) rowdata_int[9];
                    long ho_mcc = (long) rowdata_int[10];
                    long ho_fail_signal_quality2 = (long) rowdata_int[11];
                    long ho_tac = (long) Integer.parseInt(rowdata[2]);
                    long ho_tac2 = (long) rowdata_int[3];
                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_LTE_HANDOVER_FAILURE_FOR_DMFAPP,frequency:" + frequency + "ho_result:" + ho_result);
                    CLInfo.errcode = ho_result;
                    CLInfo.type = 105;
                    CLInfo.rat = (long) oppoGetRatFromType(105);
                    CLInfo.issue = oppoGetStringFromType(105);
                    CLInfo.extra = "MCC:" + ho_mcc + ",MNC:" + ho_fail_signal_quality2 + ",LAC:" + ho_tac + ",CID:" + ho_tac2 + ",signalquality:" + ho_fail_signal_quality + ",signalstrength:" + ho_fail_signal_strength;
                    return;
                case 102:
                    long frequency_2 = (long) rowdata_int[6];
                    long bar_signal_strength = (long) rowdata_int[7];
                    long bar_signal_quality = (long) rowdata_int[8];
                    long bar_cause = (long) rowdata_int[9];
                    long bar_time = (long) rowdata_int[10];
                    long ext_wait_time = (long) rowdata_int[11];
                    long bar_mcc = (long) rowdata_int[12];
                    long bar_mnc = (long) rowdata_int[13];
                    long bar_tac = (long) Integer.parseInt(rowdata[2]);
                    long bar_cell = (long) rowdata_int[3];
                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_LTE_CELL_BAR_FOR_DMFAPP,bar_cause:" + bar_cause + "bar_time:" + bar_time);
                    CLInfo.errcode = frequency_2;
                    CLInfo.type = 106;
                    CLInfo.rat = (long) oppoGetRatFromType(106);
                    CLInfo.issue = oppoGetStringFromType(106);
                    CLInfo.extra = "MCC:" + bar_mcc + ",MNC:" + bar_mnc + ",LAC:" + bar_tac + ",CID:" + bar_cell + ",signalquality:" + bar_signal_quality + ",signalstrength:" + bar_signal_strength + ",bar_time:" + bar_time + ",bar_ext_wait_time:" + ext_wait_time + ",bar_cause:" + bar_cause;
                    return;
                case 103:
                    long frequency_3 = (long) rowdata_int[6];
                    long reestablishment_fail_signal_strength = (long) rowdata_int[7];
                    long reestablishment_fail_signal_quality = (long) rowdata_int[8];
                    long expired_timer = (long) rowdata_int[9];
                    long reestablishment_fail_signal_strength2 = (long) rowdata_int[10];
                    long reestablishment_fail_mcc = (long) rowdata_int[11];
                    long reestablishment_fail_tac = (long) Integer.parseInt(rowdata[2]);
                    long reestablishment_fail_cell = (long) rowdata_int[3];
                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_LTE_CELL_REESTABLISHMENT_FAIL_FOR_DMFAPP,expired_timer:" + expired_timer + "time_length:" + reestablishment_fail_signal_strength2);
                    CLInfo.errcode = frequency_3;
                    CLInfo.type = 107;
                    CLInfo.rat = (long) oppoGetRatFromType(107);
                    CLInfo.issue = oppoGetStringFromType(107);
                    CLInfo.extra = "MCC:" + reestablishment_fail_mcc + ",MNC:" + ((long) rowdata_int[12]) + ",LAC:" + reestablishment_fail_tac + ",CID:" + reestablishment_fail_cell + ",signalquality:" + reestablishment_fail_signal_quality + ",signalstrength:" + reestablishment_fail_signal_strength + ",expired_timer:" + expired_timer + ",time_length:" + reestablishment_fail_signal_strength2 + ",frequency:" + frequency_3;
                    return;
                case 104:
                    long frequency_4 = (long) rowdata_int[6];
                    long mode3_signal_strength = (long) rowdata_int[7];
                    long mode3_signal_quality = (long) rowdata_int[8];
                    long pci = (long) rowdata_int[9];
                    long mode3_signal_strength2 = (long) rowdata_int[10];
                    long mode3_mcc = (long) rowdata_int[20];
                    long mode3_mnc = (long) rowdata_int[21];
                    long mode3_tac = (long) Integer.parseInt(rowdata[2]);
                    long mode3_cell = (long) rowdata_int[3];
                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_LTE_MODE3_INTERFERENCE_FOR_DMFAPP,frequency_4:" + frequency_4 + "pci:" + pci);
                    CLInfo.errcode = frequency_4;
                    CLInfo.type = 109;
                    CLInfo.rat = (long) oppoGetRatFromType(109);
                    CLInfo.issue = oppoGetStringFromType(109);
                    CLInfo.extra = "MCC:" + mode3_mcc + ",MNC:" + mode3_mnc + ",LAC:" + mode3_tac + ",CID:" + mode3_cell + ",signalquality:" + mode3_signal_quality + ",signalstrength:" + mode3_signal_strength + ",frequency:" + frequency_4 + ",nbr_cell_number:" + mode3_signal_strength2 + ",PCI:" + pci;
                    return;
                case 105:
                    long frequency_5 = (long) rowdata_int[6];
                    long signal_strength = (long) rowdata_int[7];
                    long signal_quality = (long) rowdata_int[8];
                    long ul_rlf_mnc = (long) rowdata_int[10];
                    long ul_rlf_tac = (long) Integer.parseInt(rowdata[2]);
                    long ul_rlf_cell = (long) rowdata_int[3];
                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_LTE_MODE3_INTERFERENCE_FOR_DMFAPP,frequency_5:" + frequency_5 + "signal_strength:" + signal_strength + "signal_quality:" + signal_quality);
                    CLInfo.errcode = frequency_5;
                    CLInfo.type = 127;
                    CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_RLC_UL_RLF);
                    CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_RLC_UL_RLF);
                    CLInfo.extra = "MCC:" + ((long) rowdata_int[9]) + ",MNC:" + ul_rlf_mnc + ",LAC:" + ul_rlf_tac + ",CID:" + ul_rlf_cell + ",signalquality:" + signal_quality + ",signalstrength:" + signal_strength;
                    return;
                case 106:
                    long frequency_6 = (long) rowdata_int[6];
                    long signal_strength_nb = (long) rowdata_int[7];
                    long signal_quality_nb = (long) rowdata_int[8];
                    long dl_bandwidth = (long) rowdata_int[9];
                    long signal_strength_nb2 = (long) rowdata_int[10];
                    long signal_quality_nb2 = (long) rowdata_int[11];
                    long narrow_bandwidth_tac = (long) Integer.parseInt(rowdata[2]);
                    long narrow_bandwidth_cell = (long) rowdata_int[3];
                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_LTE_NARROW_BANDWIDTH_SCELL_FOR_DMFAPP,frequency_6:" + frequency_6 + "ul_bandwidth:" + signal_strength_nb2 + "dl_bandwidth:" + dl_bandwidth);
                    CLInfo.errcode = (frequency_6 << 32) | (signal_strength_nb2 << 16) | dl_bandwidth;
                    CLInfo.type = 97;
                    CLInfo.rat = (long) oppoGetRatFromType(97);
                    CLInfo.issue = oppoGetStringFromType(97);
                    CLInfo.extra = "MCC:" + signal_quality_nb2 + ",MNC:" + ((long) rowdata_int[12]) + ",LAC:" + narrow_bandwidth_tac + ",CID:" + narrow_bandwidth_cell + ",signalquality:" + signal_quality_nb + ",signalstrength:" + signal_strength_nb + ",frequency:" + frequency_6 + ",ul_bandwidth:" + signal_strength_nb2 + ",dl_bandwidth:" + dl_bandwidth;
                    return;
                case 107:
                    rowdata_int[1] = Integer.parseInt(rowdata[1]);
                    rowdata_int[2] = Integer.parseInt(rowdata[2]);
                    long endtime = SystemClock.elapsedRealtime();
                    if (PhoneFactory.getPhone(this.mPhoneId).getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
                        this.conn_large_bw_cell_stayed_time += (long) rowdata_int[1];
                        this.conn_small_bw_cell_stayed_time += (long) rowdata_int[2];
                        this.conn_change_to_large_bw_cell_count += (long) rowdata_int[3];
                        this.conn_change_to_small_bw_cell_count += (long) rowdata_int[4];
                        this.conn_block_to_small_bw_cell_count += (long) rowdata_int[5];
                        this.conn_accelerate_to_large_bw_cell_count += (long) rowdata_int[6];
                        this.large_bw_cell_stayed_time += (long) rowdata_int[7];
                        this.small_bw_cell_stayed_time += (long) rowdata_int[8];
                        this.change_to_large_bw_cell_count += (long) rowdata_int[9];
                        this.change_to_small_bw_cell_count += (long) rowdata_int[10];
                        this.block_to_small_bw_cell_count += (long) rowdata_int[11];
                        this.accelerate_to_large_bw_cell_count += (long) rowdata_int[12];
                        logd("oppoGetInfoFromURC, SYS_MTK_URC_LTE_NARROW_BW_MONITORING,conn_large_bw_cell_stayed_time:" + this.conn_large_bw_cell_stayed_time + "conn_small_bw_cell_stayed_time:" + this.conn_small_bw_cell_stayed_time + "conn_change_to_large_bw_cell_count:" + this.conn_change_to_large_bw_cell_count + "conn_block_to_small_bw_cell_count:" + this.conn_block_to_small_bw_cell_count + "conn_accelerate_to_large_bw_cell_count:" + this.conn_accelerate_to_large_bw_cell_count);
                        long j = this.small_bw_time;
                        if (j > 0 && endtime - j > 300000) {
                            CLInfo.errcode = ((this.conn_large_bw_cell_stayed_time / 1000) << 48) | ((this.conn_small_bw_cell_stayed_time / 1000) << 32) | (this.conn_change_to_large_bw_cell_count << 16) | (this.conn_block_to_small_bw_cell_count << 12) | (this.conn_accelerate_to_large_bw_cell_count << 8) | (this.block_to_small_bw_cell_count << 4) | this.accelerate_to_large_bw_cell_count;
                            CLInfo.type = 129;
                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING);
                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING);
                            this.conn_large_bw_cell_stayed_time = 0;
                            this.conn_small_bw_cell_stayed_time = 0;
                            this.conn_change_to_large_bw_cell_count = 0;
                            this.conn_change_to_small_bw_cell_count = 0;
                            this.conn_block_to_small_bw_cell_count = 0;
                            this.conn_accelerate_to_large_bw_cell_count = 0;
                            this.large_bw_cell_stayed_time = 0;
                            this.small_bw_cell_stayed_time = 0;
                            this.change_to_large_bw_cell_count = 0;
                            this.change_to_small_bw_cell_count = 0;
                            this.block_to_small_bw_cell_count = 0;
                            this.accelerate_to_large_bw_cell_count = 0;
                            this.small_bw_time = endtime;
                            return;
                        } else if (this.small_bw_time == 0) {
                            CLInfo.errcode = ((this.conn_large_bw_cell_stayed_time / 1000) << 48) | ((this.conn_small_bw_cell_stayed_time / 1000) << 32) | (this.conn_change_to_large_bw_cell_count << 16) | (this.conn_block_to_small_bw_cell_count << 12) | (this.conn_accelerate_to_large_bw_cell_count << 8) | (this.block_to_small_bw_cell_count << 4) | this.accelerate_to_large_bw_cell_count;
                            CLInfo.type = 129;
                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING);
                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING);
                            this.small_bw_time = endtime;
                            return;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case SYS_OEM_NW_DIAG_CAUSE_LTE_A2_RELEASE_CELL /*{ENCODED_INT: 108}*/:
                    long fake_cell_bar_duration = (long) Integer.parseInt(rowdata[1]);
                    long fake_cell_bar_earfcn = (long) Integer.parseInt(rowdata[2]);
                    long fake_cell_bar_pci = (long) rowdata_int[3];
                    long fake_cell_bar_cell_id = (long) rowdata_int[4];
                    long fake_cell_bar_protocal_id = (long) rowdata_int[5];
                    long fake_cause = (long) rowdata_int[6];
                    long fake_cell_bar_protocal_id2 = (long) rowdata_int[7];
                    long fake_cell_mcc = (long) rowdata_int[8];
                    long fake_cell_mnc = (long) rowdata_int[9];
                    long fake_cell_tac = (long) rowdata_int[11];
                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_LTE_FAKE_CELL_BAR_FOR_DMFAPP, bar_duration:" + fake_cell_bar_duration + ", earfcn:" + fake_cell_bar_earfcn + ", pci:" + fake_cell_bar_pci + ", cell_id:" + fake_cell_bar_cell_id + ", fake_cause:" + fake_cause + ", fake_ind:" + fake_cell_bar_protocal_id2 + ", protocal_id:" + fake_cell_bar_protocal_id);
                    if (fake_cell_bar_duration >= 0) {
                        CLInfo.errcode = fake_cause;
                        CLInfo.type = 85;
                        CLInfo.rat = (long) oppoGetRatFromType(85);
                        CLInfo.issue = oppoGetStringFromType(85);
                        CLInfo.extra = "MCC:" + fake_cell_mcc + ",MNC:" + fake_cell_mnc + ",LAC:" + fake_cell_tac + ",CID:" + fake_cell_bar_cell_id + ",earfcn:" + fake_cell_bar_earfcn + ",pci:" + fake_cell_bar_pci + ", fake_ind:" + fake_cell_bar_protocal_id2 + ", protocal_id:" + fake_cell_bar_protocal_id;
                        return;
                    }
                    return;
                case 109:
                    long a2_success_rate = (long) rowdata_int[6];
                    CLInfo.errcode = a2_success_rate;
                    CLInfo.type = 108;
                    CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_A2_RELEASE_CELL);
                    CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_A2_RELEASE_CELL);
                    CLInfo.extra = "MCC:" + ((long) Integer.parseInt(rowdata[1])) + ",MNC:" + ((long) Integer.parseInt(rowdata[2])) + ",a2_failure_count:" + ((long) rowdata_int[5]) + ",a2_success_count:" + ((long) rowdata_int[4]) + ",a2_success_rate:" + a2_success_rate;
                    return;
                default:
                    switch (mtk_urc_type_for_dmf) {
                        case SYS_MTK_URC_IA_APN_ERROR_FOR_DMFAPP /*{ENCODED_INT: 201}*/:
                            long cid = (long) rowdata_int[4];
                            long error_cause = (long) rowdata_int[5];
                            long apn_error_tac = (long) rowdata_int[9];
                            long apn_error_tac2 = (long) Integer.parseInt(rowdata[2]);
                            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_IA_APN_ERROR_FOR_DMFAPP,cid:" + cid + "error_cause:" + error_cause);
                            CLInfo.errcode = (cid << 16) | (error_cause & 65535);
                            CLInfo.type = 117;
                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_IA_APN_ERROR);
                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_IA_APN_ERROR);
                            CLInfo.extra = "MCC:" + ((long) rowdata_int[6]) + ",MNC:" + ((long) rowdata_int[7]) + ",LAC:" + apn_error_tac + ",CID:" + apn_error_tac2;
                            return;
                        case SYS_MTK_URC_PDP_DEACT_ERROR_FOR_DMFAPP /*{ENCODED_INT: 202}*/:
                            long cid_2 = (long) rowdata_int[4];
                            long error_cause_2 = (long) rowdata_int[5];
                            long pdp_deact_error_mcc = (long) rowdata_int[6];
                            long pdp_deact_error_mnc = (long) rowdata_int[7];
                            long pdp_deact_error_tac = (long) Integer.parseInt(rowdata[2]);
                            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_PDP_DEACT_ERROR_FOR_DMFAPP,cid:" + cid_2 + "error_cause:" + error_cause_2);
                            CLInfo.errcode = (cid_2 << 16) | (error_cause_2 & 65535);
                            CLInfo.type = 125;
                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_PDP_DEACT_ERROR);
                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_PDP_DEACT_ERROR);
                            CLInfo.extra = "MCC:" + pdp_deact_error_mcc + ",MNC:" + pdp_deact_error_mnc + ",LAC:" + ((long) rowdata_int[9]) + ",CID:" + pdp_deact_error_tac;
                            return;
                        case SYS_MTK_URC_PDP_ACT_ERROR_FOR_DMFAPP /*{ENCODED_INT: 203}*/:
                            long cid_3 = (long) rowdata_int[4];
                            long error_cause_3 = (long) rowdata_int[5];
                            long pdp_act_error_tac = (long) rowdata_int[9];
                            long pdp_act_error_cell = (long) Integer.parseInt(rowdata[2]);
                            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_PDP_DEACT_ERROR_FOR_DMFAPP,cid:" + cid_3 + "error_cause:" + error_cause_3);
                            CLInfo.errcode = (cid_3 << 16) | (error_cause_3 & 65535);
                            CLInfo.type = 124;
                            CLInfo.rat = (long) oppoGetRatFromType(124);
                            CLInfo.issue = oppoGetStringFromType(124);
                            CLInfo.extra = "MCC:" + ((long) rowdata_int[6]) + ",MNC:" + ((long) rowdata_int[7]) + ",LAC:" + pdp_act_error_tac + ",CID:" + pdp_act_error_cell;
                            return;
                        default:
                            switch (mtk_urc_type_for_dmf) {
                                case SYS_MTK_URC_LU_REJ_FOR_DMFAPP /*{ENCODED_INT: 301}*/:
                                    long current_rat = (long) rowdata_int[3];
                                    long lu_rej_mnc = (long) rowdata_int[7];
                                    long lu_rej_lac = (long) rowdata_int[9];
                                    long lu_ori_rej_cause = (long) rowdata_int[10];
                                    long lu_rej_cell = (long) Integer.parseInt(rowdata[2]);
                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_LU_REJ_FOR_DMFAPP,reject_cause:" + ((long) rowdata_int[5]) + "current_rat:" + current_rat);
                                    CLInfo.type = 63;
                                    CLInfo.errcode = lu_ori_rej_cause;
                                    CLInfo.rat = current_rat;
                                    CLInfo.issue = oppoGetStringFromType(63);
                                    CLInfo.extra = "MCC:" + ((long) rowdata_int[6]) + ",MNC:" + lu_rej_mnc + ",LAC:" + lu_rej_lac + ",CID:" + lu_rej_cell;
                                    return;
                                case SYS_MTK_URC_RAU_REJ_FOR_DMFAPP /*{ENCODED_INT: 302}*/:
                                    long current_rat_for_rau = (long) rowdata_int[3];
                                    long rau_rej_mnc = (long) rowdata_int[7];
                                    long rau_rej_lac = (long) rowdata_int[9];
                                    long rau_ori_rej_cause = (long) rowdata_int[10];
                                    long rau_rej_mnc2 = (long) Integer.parseInt(rowdata[2]);
                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_RAU_REJ_FOR_DMFAPP,reject_cause_for_rau:" + ((long) rowdata_int[5]) + "current_rat_for_rau:" + current_rat_for_rau);
                                    CLInfo.type = 63;
                                    CLInfo.errcode = rau_ori_rej_cause;
                                    CLInfo.rat = current_rat_for_rau;
                                    CLInfo.issue = oppoGetStringFromType(63);
                                    CLInfo.extra = "MCC:" + ((long) rowdata_int[6]) + ",MNC:" + rau_rej_mnc + ",LAC:" + rau_rej_lac + ",CID:" + rau_rej_mnc2;
                                    return;
                                case SYS_MTK_URC_TAU_REJ_FOR_DMFAPP /*{ENCODED_INT: 303}*/:
                                    long current_rat_for_tau = (long) rowdata_int[3];
                                    long tau_rej_mnc = (long) rowdata_int[7];
                                    long tau_rej_tac = (long) rowdata_int[9];
                                    long tau_rej_tac2 = (long) rowdata_int[10];
                                    long tau_rej_mnc2 = (long) Integer.parseInt(rowdata[2]);
                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_TAU_REJ_FOR_DMFAPP,reject_cause_for_tau:" + ((long) rowdata_int[5]) + "current_rat_for_tau:" + current_rat_for_tau);
                                    CLInfo.type = 101;
                                    CLInfo.errcode = tau_rej_tac2;
                                    CLInfo.rat = current_rat_for_tau;
                                    CLInfo.extra = "MCC:" + ((long) rowdata_int[6]) + ",MNC:" + tau_rej_mnc + ",LAC:" + tau_rej_tac + ",CID:" + tau_rej_mnc2;
                                    CLInfo.issue = oppoGetStringFromType(101);
                                    return;
                                case SYS_MTK_URC_ATTACH_REJ_FOR_DMFAPP /*{ENCODED_INT: 304}*/:
                                    long current_rat_for_attach = (long) rowdata_int[3];
                                    long attach_rej_mcc = (long) rowdata_int[6];
                                    long attach_rej_mnc = (long) rowdata_int[7];
                                    long attach_rej_tac = (long) rowdata_int[9];
                                    long attach_ori_rej_cause = (long) rowdata_int[10];
                                    long attach_rej_cell = (long) Integer.parseInt(rowdata[2]);
                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_ATTACH_REJ_FOR_DMFAPP,reject_cause_for_attach:" + ((long) rowdata_int[5]) + "current_rat_for_attach:" + current_rat_for_attach);
                                    if (current_rat_for_attach != 5) {
                                        CLInfo.type = 63;
                                        CLInfo.errcode = attach_ori_rej_cause;
                                        CLInfo.rat = current_rat_for_attach;
                                        CLInfo.extra = "MCC:" + attach_rej_mcc + ",MNC:" + attach_rej_mnc + ",LAC:" + attach_rej_tac + ",CID:" + attach_rej_cell;
                                        CLInfo.issue = oppoGetStringFromType(63);
                                        return;
                                    }
                                    CLInfo.type = 61;
                                    CLInfo.errcode = attach_ori_rej_cause;
                                    CLInfo.rat = current_rat_for_attach;
                                    CLInfo.extra = "MCC:" + attach_rej_mcc + ",MNC:" + attach_rej_mnc + ",LAC:" + attach_rej_tac + ",CID:" + attach_rej_cell;
                                    CLInfo.issue = oppoGetStringFromType(61);
                                    return;
                                case SYS_MTK_URC_AUTHEN_REJ_FOR_DMFAPP /*{ENCODED_INT: 305}*/:
                                    long reject_type_for_authen = (long) rowdata_int[4];
                                    long current_rat_for_authen = (long) rowdata_int[3];
                                    long authen_rej_mcc = (long) rowdata_int[5];
                                    long authen_rej_mnc = (long) rowdata_int[6];
                                    long authen_rej_tac = (long) rowdata_int[8];
                                    long authen_rej_cell = (long) Integer.parseInt(rowdata[2]);
                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_AUTHEN_REJ_FOR_DMFAPP,reject_type_for_authen:" + reject_type_for_authen + "current_rat_for_authen:" + current_rat_for_authen);
                                    if (current_rat_for_authen != 5) {
                                        CLInfo.type = 64;
                                        CLInfo.errcode = reject_type_for_authen;
                                        CLInfo.rat = current_rat_for_authen;
                                        CLInfo.issue = oppoGetStringFromType(64);
                                        CLInfo.extra = "MCC:" + authen_rej_mcc + ",MNC:" + authen_rej_mnc + ",LAC:" + authen_rej_tac + ",CID:" + authen_rej_cell;
                                        return;
                                    }
                                    CLInfo.type = 68;
                                    CLInfo.errcode = reject_type_for_authen;
                                    CLInfo.rat = current_rat_for_authen;
                                    CLInfo.issue = oppoGetStringFromType(68);
                                    CLInfo.extra = "MCC:" + authen_rej_mcc + ",MNC:" + authen_rej_mnc + ",LAC:" + authen_rej_tac + ",CID:" + authen_rej_cell;
                                    return;
                                case SYS_MTK_URC_SERVICE_REJECT_FOR_DMFAPP /*{ENCODED_INT: 306}*/:
                                    long current_rat_for_service_rej = (long) rowdata_int[3];
                                    long reject_type_for_service_rej = (long) rowdata_int[4];
                                    long reject_cause_for_service_rej = (long) rowdata_int[5];
                                    long service_rej_mcc = (long) rowdata_int[6];
                                    long service_rej_mnc = (long) rowdata_int[9];
                                    long service_ori_rej_cause = (long) rowdata_int[10];
                                    long service_rej_mcc2 = (long) Integer.parseInt(rowdata[2]);
                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_SERVICE_REJECT_FOR_DMFAPP,reject_type_for_service_rej:" + reject_type_for_service_rej + "reject_cause_for_service_rej:" + reject_cause_for_service_rej);
                                    CLInfo.errcode = service_ori_rej_cause;
                                    CLInfo.rat = current_rat_for_service_rej;
                                    CLInfo.issue = oppoGetStringFromType(102);
                                    CLInfo.type = 102;
                                    CLInfo.extra = "MCC:" + service_rej_mcc + ",MNC:" + ((long) rowdata_int[7]) + ",LAC:" + service_rej_mnc + ",CID:" + service_rej_mcc2 + ",reject_cause_for_service_rej:" + reject_cause_for_service_rej + ",reject_type_for_service_rej:" + reject_type_for_service_rej;
                                    return;
                                case SYS_MTK_URC_NETWORK_DETACH_FOR_DMFAPP /*{ENCODED_INT: 307}*/:
                                    long detach_type = (long) rowdata_int[4];
                                    long detach_cause = (long) rowdata_int[5];
                                    long detach_mcc = (long) rowdata_int[6];
                                    long detach_tac = (long) rowdata_int[9];
                                    long detach_ori_rej_cause = (long) rowdata_int[10];
                                    long detach_cell = (long) Integer.parseInt(rowdata[2]);
                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_NETWORK_DETACH_FOR_DMFAPP,detach_type:" + detach_type + "detach_cause:" + detach_cause);
                                    CLInfo.errcode = detach_ori_rej_cause;
                                    CLInfo.rat = (long) rowdata_int[3];
                                    CLInfo.issue = oppoGetStringFromType(104);
                                    CLInfo.type = 104;
                                    CLInfo.extra = "MCC:" + detach_mcc + ",MNC:" + ((long) rowdata_int[7]) + ",LAC:" + detach_tac + ",CID:" + detach_cell + ",detach_cause:" + detach_cause + ",detach_type:" + detach_type;
                                    return;
                                default:
                                    switch (mtk_urc_type_for_dmf) {
                                        case 401:
                                            long pdcp_ul_timeout_cell = (long) Integer.parseInt(rowdata[2]);
                                            long pdcp_ul_timeout_signal_strength = (long) rowdata_int[8];
                                            long pdcp_ul_timeout_signal_quality = (long) rowdata_int[9];
                                            long pdcp_ul_timeout_ims_exit_flag = (long) rowdata_int[10];
                                            CLInfo.type = 120;
                                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT);
                                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT);
                                            CLInfo.extra = "MCC:" + ((long) rowdata_int[4]) + ",MNC:" + ((long) rowdata_int[5]) + ",LAC:" + ((long) rowdata_int[7]) + ",CID:" + pdcp_ul_timeout_cell + ",signalstrength:" + pdcp_ul_timeout_signal_strength + ",signalquality:" + pdcp_ul_timeout_signal_quality + ",ims_call_exit:" + pdcp_ul_timeout_ims_exit_flag;
                                            return;
                                        case 402:
                                            long pdcp_ul_out_of_buf_cell = (long) Integer.parseInt(rowdata[2]);
                                            long pdcp_ul_out_of_buf_signal_strength = (long) rowdata_int[8];
                                            long pdcp_ul_out_of_buf_signal_strength2 = (long) rowdata_int[9];
                                            long pdcp_ul_out_of_buf_ims_exit_flag = (long) rowdata_int[10];
                                            CLInfo.type = 121;
                                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_OUT_OF_BUFFER);
                                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_OUT_OF_BUFFER);
                                            CLInfo.extra = "MCC:" + ((long) rowdata_int[4]) + ",MNC:" + ((long) rowdata_int[5]) + ",LAC:" + ((long) rowdata_int[7]) + ",CID:" + pdcp_ul_out_of_buf_cell + ",signalstrength:" + pdcp_ul_out_of_buf_signal_strength + ",signalquality:" + pdcp_ul_out_of_buf_signal_strength2 + ",ims_call_exit:" + pdcp_ul_out_of_buf_ims_exit_flag;
                                            return;
                                        case SYS_MTK_URC_DATA_PDCP_UL_ONE_WAY_PASS_FOR_DMFAPP /*{ENCODED_INT: 403}*/:
                                            long pdcp_ul_one_way_pass_cell = (long) Integer.parseInt(rowdata[2]);
                                            long pdcp_ul_one_way_pass_signal_strength = (long) rowdata_int[8];
                                            long pdcp_ul_one_way_pass_signal_strength2 = (long) rowdata_int[9];
                                            long pdcp_ul_one_way_pass_ims_exit_flag = (long) rowdata_int[10];
                                            CLInfo.type = 122;
                                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_ONE_WAY_PASS);
                                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_ONE_WAY_PASS);
                                            CLInfo.extra = "MCC:" + ((long) rowdata_int[4]) + ",MNC:" + ((long) rowdata_int[5]) + ",LAC:" + ((long) rowdata_int[7]) + ",CID:" + pdcp_ul_one_way_pass_cell + ",signalstrength:" + pdcp_ul_one_way_pass_signal_strength + ",signalquality:" + pdcp_ul_one_way_pass_signal_strength2 + ",ims_call_exit:" + pdcp_ul_one_way_pass_ims_exit_flag;
                                            return;
                                        case SYS_MTK_URC_DATA_DL_HIGH_BLER_FOR_DMFAPP /*{ENCODED_INT: 404}*/:
                                            long dl_high_bler_cell = (long) Integer.parseInt(rowdata[2]);
                                            long dl_high_bler_cell2 = (long) rowdata_int[8];
                                            long dl_high_bler_signal_quality = (long) rowdata_int[9];
                                            long dl_high_bler_signal_quality2 = (long) rowdata_int[10];
                                            CLInfo.type = 123;
                                            CLInfo.rat = (long) oppoGetRatFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_DL_HIGH_BLER);
                                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_DATA_DL_HIGH_BLER);
                                            CLInfo.extra = "MCC:" + ((long) rowdata_int[4]) + ",MNC:" + ((long) rowdata_int[5]) + ",LAC:" + ((long) rowdata_int[7]) + ",CID:" + dl_high_bler_cell + ",signalstrength:" + dl_high_bler_cell2 + ",signalquality:" + dl_high_bler_signal_quality + ",ims_call_exit:" + dl_high_bler_signal_quality2;
                                            return;
                                        default:
                                            switch (mtk_urc_type_for_dmf) {
                                                case SYS_MTK_URC_VOLTE_CALL_DROP_FOR_DMFAPP /*{ENCODED_INT: 509}*/:
                                                    long vcd_mcc = (long) rowdata_int[17];
                                                    long vcd_mnc = (long) rowdata_int[18];
                                                    long vcd_tac = (long) rowdata_int[20];
                                                    long vcd_cid = (long) Integer.parseInt(rowdata[2]);
                                                    long vcd_ss = (long) rowdata_int[7];
                                                    long vcd_ss2 = (long) rowdata_int[8];
                                                    long vcd_rat = (long) rowdata_int[3];
                                                    long vcd_tac2 = (long) rowdata_int[9];
                                                    long vcd_is_mt_call = (long) rowdata_int[10];
                                                    long vcd_sip_call_state = (long) rowdata_int[11];
                                                    long vcd_net_type = (long) rowdata_int[12];
                                                    long vcd_sip_cause = (long) rowdata_int[13];
                                                    long vcd_call_cause = (long) rowdata_int[14];
                                                    long vcd_emm_mm_cause = (long) rowdata_int[15];
                                                    long vcd_emm_mm_cause2 = (long) rowdata_int[16];
                                                    long vcd_ori_errc_rrc_rr_cause = (long) rowdata_int[21];
                                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_VOLTE_CALL_DROP_FOR_DMFAPP, raw_data: " + data);
                                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_VOLTE_CALL_DROP_FOR_DMFAPP, call_cause: " + vcd_call_cause + ", sip_cause: " + vcd_sip_cause);
                                                    CLInfo.errcode = vcd_call_cause;
                                                    CLInfo.type = (long) (1 == vcd_is_mt_call ? SYS_OEM_NW_DIAG_CAUSE_IMS_MT_CALL_DROP : SYS_OEM_NW_DIAG_CAUSE_IMS_MO_CALL_DROP);
                                                    CLInfo.rat = vcd_rat;
                                                    CLInfo.issue = oppoGetStringFromType(1 == vcd_is_mt_call ? SYS_OEM_NW_DIAG_CAUSE_IMS_MT_CALL_DROP : SYS_OEM_NW_DIAG_CAUSE_IMS_MO_CALL_DROP);
                                                    CLInfo.extra = "MCC:" + vcd_mcc + ",MNC:" + vcd_mnc + ",LAC:" + vcd_tac + ",CID:" + vcd_cid + ",signalquality:" + vcd_ss + ",signalstrength:" + vcd_ss2 + ",call_type:" + vcd_tac2 + ",is_mt_call:" + vcd_is_mt_call + ",sip_call_state:" + vcd_sip_call_state + ",net_type:" + vcd_net_type + ",sip_cause:" + vcd_sip_cause + ",emm_mm_cause:" + vcd_emm_mm_cause + ",errc_rrc_rr_cause:" + vcd_emm_mm_cause2 + ",vcd_ori_errc_rrc_rr_cause:" + vcd_ori_errc_rrc_rr_cause;
                                                    return;
                                                case SYS_MTK_URC_IMS_CALL_HO_FAIL_FOR_DMFAPP /*{ENCODED_INT: 510}*/:
                                                    long ichf_ho_type = (long) Integer.parseInt(rowdata[1]);
                                                    long ichf_fail_cause = (long) Integer.parseInt(rowdata[2]);
                                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_IMS_CALL_HO_FAIL_FOR_DMFAPP, raw_data: " + data);
                                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_IMS_CALL_HO_FAIL_FOR_DMFAPP, ho_type: " + ichf_ho_type + ", fail_cause: " + ichf_fail_cause);
                                                    CLInfo.errcode = ichf_ho_type;
                                                    CLInfo.type = 42;
                                                    CLInfo.rat = (long) oppoGetRatFromType(42);
                                                    CLInfo.issue = oppoGetStringFromType(42);
                                                    CLInfo.extra = "fail_cause:" + ichf_fail_cause;
                                                    return;
                                                case SYS_MTK_URC_CALL_GWL_CALL_QUALITY_FOR_DMFAPP /*{ENCODED_INT: 511}*/:
                                                    long gwl_call_quality_signal_quality = (long) rowdata_int[11];
                                                    long gwl_call_quality_signal_strength = (long) rowdata_int[10];
                                                    long lte_snr = (long) rowdata_int[12];
                                                    long gwl_call_quality_status = (long) rowdata_int[13];
                                                    long gwl_call_quality_mnc = (long) Integer.parseInt(rowdata[2]);
                                                    long gwl_call_quality_signal_quality2 = (long) rowdata_int[4];
                                                    long gwl_call_quality_cell = (long) rowdata_int[5];
                                                    long gwl_call_dl_have_sound = (long) rowdata_int[14];
                                                    logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_CALL_GWL_CALL_QUALITY_FOR_DMFAPP, gwl_call_quality_status:" + gwl_call_quality_status);
                                                    CLInfo.rat = (long) rowdata_int[6];
                                                    CLInfo.errcode = gwl_call_quality_status;
                                                    CLInfo.issue = oppoGetStringFromType(39);
                                                    CLInfo.type = 39;
                                                    CLInfo.extra = "MCC:" + ((long) Integer.parseInt(rowdata[1])) + ",MNC:" + gwl_call_quality_mnc + ",LAC:" + gwl_call_quality_signal_quality2 + ",CID:" + gwl_call_quality_cell + ",signalquality:" + gwl_call_quality_signal_quality + ",signalstrength:" + gwl_call_quality_signal_strength + ",snr:" + lte_snr + ",dl_have_sound:" + gwl_call_dl_have_sound;
                                                    return;
                                                case 512:
                                                    long c2k_call_quality_rssi = (long) rowdata_int[8];
                                                    long c2k_call_quality_ecio = (long) rowdata_int[9];
                                                    long c2k_call_quality_mnc = (long) Integer.parseInt(rowdata[2]);
                                                    long c2k_call_quality_nid = (long) rowdata_int[5];
                                                    long c2k_call_quality_bid = (long) rowdata_int[6];
                                                    CLInfo.errcode = (long) rowdata_int[12];
                                                    CLInfo.rat = 3;
                                                    CLInfo.issue = oppoGetStringFromType(41);
                                                    CLInfo.type = 41;
                                                    CLInfo.extra = "MCC:" + ((long) Integer.parseInt(rowdata[1])) + ",MNC:" + c2k_call_quality_mnc + ",SID:" + ((long) rowdata_int[4]) + ",NID:" + c2k_call_quality_nid + ",BID:" + c2k_call_quality_bid + ",signalstrength:" + c2k_call_quality_rssi + ",signalquality:" + c2k_call_quality_ecio + ",dl_have_sound:" + ((long) rowdata_int[13]);
                                                    return;
                                                default:
                                                    switch (mtk_urc_type_for_dmf) {
                                                        case SYS_MTK_URC_3GPP_OOS_LOW_POWER_FOR_DMFAPP /*{ENCODED_INT: 701}*/:
                                                            rowdata_int[1] = Integer.parseInt(rowdata[1]);
                                                            rowdata_int[2] = Integer.parseInt(rowdata[2]);
                                                            logd("oppoGetDMFAppInfoFromURC, 3GPP_OOS_LOW_POWER,type:" + rowdata_int[1] + "value:" + rowdata_int[2]);
                                                            if (rowdata_int[1] == 0) {
                                                                CLInfo.errcode = (long) rowdata_int[2];
                                                                CLInfo.type = 93;
                                                                CLInfo.rat = (long) oppoGetRatFromType(93);
                                                                CLInfo.issue = oppoGetStringFromType(93);
                                                                return;
                                                            } else if (rowdata_int[1] == 1) {
                                                                CLInfo.errcode = (long) rowdata_int[2];
                                                                CLInfo.type = 94;
                                                                CLInfo.rat = (long) oppoGetRatFromType(94);
                                                                CLInfo.issue = oppoGetStringFromType(94);
                                                                return;
                                                            } else if (rowdata_int[1] == 2) {
                                                                CLInfo.errcode = (long) rowdata_int[2];
                                                                CLInfo.type = 95;
                                                                CLInfo.rat = (long) oppoGetRatFromType(95);
                                                                CLInfo.issue = oppoGetStringFromType(95);
                                                                return;
                                                            } else if (rowdata_int[1] == 3) {
                                                                CLInfo.errcode = (long) rowdata_int[2];
                                                                CLInfo.type = 96;
                                                                CLInfo.rat = (long) oppoGetRatFromType(96);
                                                                CLInfo.issue = oppoGetStringFromType(96);
                                                                return;
                                                            } else {
                                                                return;
                                                            }
                                                        case SYS_MTK_URC_SCREEN_ON_TRIGGER_NW_SRCH_FOR_DMFAPP /*{ENCODED_INT: 702}*/:
                                                            long is_nw_srch_triggered = (long) Integer.parseInt(rowdata[1]);
                                                            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_SCREEN_ON_TRIGGER_NW_SRCH,is_nw_srch_triggered:" + is_nw_srch_triggered);
                                                            if (is_nw_srch_triggered > 0) {
                                                                CLInfo.errcode = is_nw_srch_triggered;
                                                                CLInfo.type = 92;
                                                                CLInfo.rat = (long) oppoGetRatFromType(92);
                                                                CLInfo.issue = oppoGetStringFromType(92);
                                                                return;
                                                            }
                                                            return;
                                                        case SYS_MTK_URC_SMART_IDLE_TIMEOUT_MONITOR_FOR_DMFAPP /*{ENCODED_INT: 703}*/:
                                                            long parseInt = (long) Integer.parseInt(rowdata[1]);
                                                            long parseInt2 = (long) Integer.parseInt(rowdata[2]);
                                                            long hold_a2_with_tau_num = (long) rowdata_int[8];
                                                            long hold_tmo_cnt_on_others = (long) rowdata_int[9];
                                                            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_SMART_IDLE_TIMEOUT_MONITOR_FOR_DMFAPP: raw_data: " + data);
                                                            logd("> hold_cell_num: " + ((long) rowdata_int[5]) + ", tmo_cnt_on_others: " + hold_tmo_cnt_on_others);
                                                            logd("> force_rel_num: " + ((long) rowdata_int[6]) + ", local_rel_num: " + ((long) rowdata_int[7]) + ", a2_with_tau_num: " + hold_a2_with_tau_num);
                                                            CLInfo.rat = (long) rowdata_int[4];
                                                            CLInfo.issue = oppoGetStringFromType(SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_CONN_HOLD);
                                                            CLInfo.type = 118;
                                                            CLInfo.extra = data;
                                                            return;
                                                        default:
                                                            CLInfo.type = -1;
                                                            return;
                                                    }
                                            }
                                    }
                            }
                    }
            }
        } else {
            long irf_mcc = (long) rowdata_int[12];
            long irf_mnc = (long) rowdata_int[13];
            long irf_tac = (long) rowdata_int[15];
            long irf_cid = (long) Integer.parseInt(rowdata[2]);
            long irf_sq = (long) rowdata_int[8];
            long irf_fail_state = (long) rowdata_int[9];
            long irf_sip_cause = (long) rowdata_int[10];
            long irf_pdn_cause = (long) rowdata_int[11];
            long irf_rat = (long) rowdata_int[3];
            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_IMS_REG_FAIL_FOR_DMFAPP, raw_data: " + data);
            logd("oppoGetDMFAppInfoFromURC, SYS_MTK_URC_IMS_REG_FAIL_FOR_DMFAPP, rat: " + irf_rat + ", fail_state: " + irf_fail_state + ", sip_cause: " + irf_sip_cause);
            CLInfo.errcode = irf_fail_state;
            CLInfo.type = 43;
            CLInfo.rat = irf_rat;
            CLInfo.issue = oppoGetStringFromType(43);
            CLInfo.extra = "MCC:" + irf_mcc + ",MNC:" + irf_mnc + ",LAC:" + irf_tac + ",CID:" + irf_cid + ",signalquality:" + ((long) rowdata_int[7]) + ",signalstrength:" + irf_sq + ",sip_cause:" + irf_sip_cause + ",pdn_cause:" + irf_pdn_cause;
        }
    }

    private String oppoGetStringFromType(int type) {
        if (type == 19) {
            return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PAGE_FAIL;
        }
        if (type == 20) {
            return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_DISC;
        }
        if (type == 101) {
            return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_TAU_REJECT;
        }
        if (type == 102) {
            return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_SERVICE_REJECT;
        }
        switch (type) {
            case 11:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RACH;
            case 12:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RLF;
            case 13:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PCH;
            case 14:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CSFB;
            case 15:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_REJECT;
            case 16:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RRC;
            default:
                switch (type) {
                    case 22:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_CM_SERV_REJ;
                    case SYS_OEM_NW_DIAG_CAUSE_MO_CALL_DROP /*{ENCODED_INT: 23}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_CALL_DROP;
                    case SYS_OEM_NW_DIAG_CAUSE_MT_CALL_DROP /*{ENCODED_INT: 24}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CALL_DROP;
                    case 25:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_MT_DROP_RATE;
                    case 26:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_AIRPLANE_NUM;
                    case SYS_OEM_NW_DIAG_CAUSE_DATA_USER_DATA_ENABLE_NUM /*{ENCODED_INT: 27}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_USER_DATA_ENABLE_NUM;
                    case 28:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_RECOVERY_SUCC_NUM;
                    case SYS_OEM_NW_DIAG_CAUSE_DATA_PDN_ACTIVATION_DURATION /*{ENCODED_INT: 29}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_PDN_ACTIVATION_DURATION;
                    case SYS_OEM_NW_DIAG_CAUSE_DATA_DISCONNECT_CALL_ERROR /*{ENCODED_INT: 30}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_DISCONNECT_CALL_ERROR;
                    case SYS_OEM_NW_DIAG_CAUSE_APN_REASON_DATA_CALL_FAIL /*{ENCODED_INT: 31}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_APN_REASON_DATA_CALL_FAIL;
                    case SYS_OEM_NW_DIAG_CAUSE_NOT_APN_REASON_DATA_CALL_FAIL /*{ENCODED_INT: 32}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_NOT_APN_REASON_DATA_CALL_FAIL;
                    case SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_DURATION /*{ENCODED_INT: 33}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_DURATION;
                    case 34:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_CALL_ERROR;
                    case SYS_OEM_NW_DIAG_CAUSE_NO_RESPONSE_FOR_DATA_CALL /*{ENCODED_INT: 35}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_NO_RESPONSE_FOR_DATA_CALL;
                    case SYS_OEM_NW_DIAG_CAUSE_CONGEST_RATIO /*{ENCODED_INT: 36}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_CONGEST_RATIO;
                    case SYS_OEM_NW_DIAG_CAUSE_NO_RECEIVE_DATA_ERROR /*{ENCODED_INT: 37}*/:
                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_NO_RECEIVE_DATA_ERROR;
                    default:
                        switch (type) {
                            case 39:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_GWL_CALL_QUALITY;
                            case SYS_OEM_NW_DIAG_CAUSE_REG_SRV_OOS /*{ENCODED_INT: 80}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_OOS;
                            case SYS_OEM_NW_DIAG_CAUSE_REG_LTE_INT_FAIL /*{ENCODED_INT: 81}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_INT_FAIL;
                            case SYS_OEM_NW_DIAG_CAUSE_REG_LTE_REDIREC_UNEXPECT /*{ENCODED_INT: 82}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_REDIREC_UNEXPECT;
                            case SYS_OEM_NW_DIAG_CAUSE_REG_LTE_TYPE_UNEXPECT /*{ENCODED_INT: 83}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_TYPE_UNEXPECT;
                            case SYS_OEM_NW_DIAG_CAUSE_FORBIDDEN_TAI_OPT /*{ENCODED_INT: 84}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_FORBIDDEN_TAI_OPT;
                            case SYS_OEM_NW_DIAG_CAUSE_LTE_BAR_BAD_FAKE_CELL /*{ENCODED_INT: 85}*/:
                                return "4g_bar_cell";
                            case SYS_OEM_NW_DIAG_CAUSE_LTE_BACKOFF_PLMN /*{ENCODED_INT: 86}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_BACKOFF_PLMN;
                            case SYS_OEM_NW_DIAG_CAUSE_LTE_REG_FAIL_5TIMES /*{ENCODED_INT: 87}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_FAIL_5TIMES;
                            case SYS_OEM_NW_DIAG_CAUSE_LTE_REG_SUCCESS_AFTER_REJECT /*{ENCODED_INT: 88}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_4G_SUCCESS_AFTER_REJECT;
                            case 89:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT_OUT_OF_CREDIT;
                            case SYS_OEM_NW_DIAG_CAUSE_REG_1X_MAP /*{ENCODED_INT: 90}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_1X_MAP;
                            case SYS_OEM_NW_DIAG_CAUSE_CAUSE_SCREEN_ON_NW_SRCH /*{ENCODED_INT: 91}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_NW_SRCH;
                            case SYS_OEM_NW_DIAG_CAUSE_CAUSE_SCREEN_ON_TRIGGER_NW_SRCH /*{ENCODED_INT: 92}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_TRIGGER_NW_SRCH;
                            case SYS_OEM_NW_DIAG_CAUSE_CAUSE_ACQ_CNT /*{ENCODED_INT: 93}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_ACQ_CNT;
                            case SYS_OEM_NW_DIAG_CAUSE_CAUSE_SCREEN_OFF_ACQ_CNT /*{ENCODED_INT: 94}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCREEN_OFF_ACQ_CNT;
                            case SYS_OEM_NW_DIAG_CAUSE_CAUSE_SKIP_ACQ_CNT /*{ENCODED_INT: 95}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_SKIP_ACQ_CNT;
                            case SYS_OEM_NW_DIAG_CAUSE_CAUSE_INACTIVE_FULLBAND_CNT /*{ENCODED_INT: 96}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_INACTIVE_FULLBAND_CNT;
                            case SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BANDWIDTH_SCELL /*{ENCODED_INT: 97}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BANDWIDTH_SCELL;
                            case SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT /*{ENCODED_INT: 120}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT;
                            case SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_OUT_OF_BUFFER /*{ENCODED_INT: 121}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_OUT_OF_BUFFER;
                            case SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_ONE_WAY_PASS /*{ENCODED_INT: 122}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_ONE_WAY_PASS;
                            case SYS_OEM_NW_DIAG_CAUSE_DATA_DL_HIGH_BLER /*{ENCODED_INT: 123}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_DL_HIGH_BLER;
                            case 124:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_PDP_ACT_ERROR;
                            case SYS_OEM_NW_DIAG_CAUSE_PDP_DEACT_ERROR /*{ENCODED_INT: 125}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_PDP_DEACT_ERROR;
                            case SYS_OEM_NW_DIAG_CAUSE_DATA_RLC_UL_RLF /*{ENCODED_INT: 127}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_RLC_UL_RLF;
                            case SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING /*{ENCODED_INT: 129}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_NARROW_BW_MONITORING;
                            case 160:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK;
                            case SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT /*{ENCODED_INT: 161}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT;
                            case 210:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED;
                            case SYS_OEM_NW_DIAG_CAUSE_RFFE_MISSING_NONFATAL /*{ENCODED_INT: 211}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_RFFE_MISSING_NONFATAL;
                            case SYS_OEM_NW_DIAG_CAUSE_RF_XO_FREQ_OFFSET /*{ENCODED_INT: 214}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_XO_OFFSET;
                            case SYS_OEM_NW_DIAG_CAUSE_RF_ENTER_SAR_FORCE_DOWN_ANT /*{ENCODED_INT: 215}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_ENTER_SAR_FORCE_DOWN_ANT;
                            case SYS_OEM_NW_DIAG_CAUSE_RF_4G_PA_DAMAGE /*{ENCODED_INT: 216}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_4G_PA_DAMAGE;
                            case SYS_OEM_NW_DIAG_CAUSE_IMS_MO_CALL_DROP /*{ENCODED_INT: 230}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_MO_CALL_DROP;
                            case SYS_OEM_NW_DIAG_CAUSE_IMS_MT_CALL_DROP /*{ENCODED_INT: 231}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_MT_CALL_DROP;
                            case 260:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_COMPLETED;
                            case SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_FAILED /*{ENCODED_INT: 261}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_FAILED;
                            case SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_CANCEL /*{ENCODED_INT: 262}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_CANCEL;
                            case SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_CALL_DROP /*{ENCODED_INT: 263}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_CALL_DROP;
                            case SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_DISC_ABNORMAL /*{ENCODED_INT: 264}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_DISC_ABNORMAL;
                            case SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_NORMAL /*{ENCODED_INT: 265}*/:
                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_NORMAL;
                            default:
                                switch (type) {
                                    case 41:
                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_C2K_CALL_QUALITY;
                                    case SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_HO_FAIL /*{ENCODED_INT: 42}*/:
                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_CALL_HO_FAIL;
                                    case SYS_OEM_NW_DIAG_CAUSE_IMS_REG_FAIL /*{ENCODED_INT: 43}*/:
                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_REG_FAIL;
                                    default:
                                        switch (type) {
                                            case 60:
                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AS_FAILED;
                                            case SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT /*{ENCODED_INT: 61}*/:
                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT;
                                            case SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE /*{ENCODED_INT: 62}*/:
                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE;
                                            case SYS_OEM_NW_DIAG_CAUSE_REG_REJECT /*{ENCODED_INT: 63}*/:
                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT;
                                            case SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT /*{ENCODED_INT: 64}*/:
                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT;
                                            case SYS_OEM_NW_DIAG_CAUSE_AS_FAILED /*{ENCODED_INT: 65}*/:
                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_AS_FAILED;
                                            case SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED /*{ENCODED_INT: 66}*/:
                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED;
                                            case SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED /*{ENCODED_INT: 67}*/:
                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED;
                                            case SYS_OEM_NW_DIAG_CAUSE_LTE_AUTHENTICATION_REJECT /*{ENCODED_INT: 68}*/:
                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AUTHENTICATION_REJECT;
                                            default:
                                                switch (type) {
                                                    case SYS_OEM_NW_DIAG_CAUSE_FAKE_BS /*{ENCODED_INT: 70}*/:
                                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_FAKE_BS;
                                                    case SYS_OEM_NW_DIAG_CAUSE_FAKE_BS_ONLY /*{ENCODED_INT: 71}*/:
                                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_FAKE_BS_ONLY;
                                                    case SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_RAT /*{ENCODED_INT: 72}*/:
                                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_RAT;
                                                    case SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MCC /*{ENCODED_INT: 73}*/:
                                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MCC;
                                                    case SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MNC /*{ENCODED_INT: 74}*/:
                                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MNC;
                                                    case SYS_OEM_NW_DIAG_CAUSE_REG_SRV_ON /*{ENCODED_INT: 75}*/:
                                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_ON;
                                                    default:
                                                        switch (type) {
                                                            case 104:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_ABNORMAL_DETACH;
                                                            case 105:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_HANDOVER_FAILURE;
                                                            case 106:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_ABNORMAL_BAR;
                                                            case 107:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_ABNORMAL_TIMEOUT;
                                                            case SYS_OEM_NW_DIAG_CAUSE_LTE_A2_RELEASE_CELL /*{ENCODED_INT: 108}*/:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_A2_RELEASE_CELL;
                                                            case 109:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_PCI_MODE3_INTERFERENCE;
                                                            case 110:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NOT_ALLOWED;
                                                            case SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN /*{ENCODED_INT: 111}*/:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN;
                                                            case SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR /*{ENCODED_INT: 112}*/:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR;
                                                            case SYS_OEM_NW_DIAG_CAUSE_DATA_STALL_ERROR /*{ENCODED_INT: 113}*/:
                                                                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_STALL_ERROR;
                                                            default:
                                                                switch (type) {
                                                                    case SYS_OEM_NW_DIAG_CAUSE_DATA_RATE_LIMIT_ON_LTE /*{ENCODED_INT: 116}*/:
                                                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_RATE_LIMIT_ON_LTE;
                                                                    case SYS_OEM_NW_DIAG_CAUSE_IA_APN_ERROR /*{ENCODED_INT: 117}*/:
                                                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_IA_APN_ERROR;
                                                                    case SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_CONN_HOLD /*{ENCODED_INT: 118}*/:
                                                                        return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_RRC_CONN_HOLD;
                                                                    default:
                                                                        return "";
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private int oppoGetRatFromType(int type) {
        if (!(type == 61 || type == 68 || type == 85 || type == 97 || type == 117 || type == 127 || type == 129)) {
            if (type == 160 || type == 63 || type == 64) {
                return -1;
            }
            if (type == 101 || type == 102) {
                return 5;
            }
            switch (type) {
                case 11:
                    return 0;
                case 12:
                    return 0;
                case 13:
                    return 0;
                case 14:
                    return 0;
                case 15:
                    return 5;
                case 16:
                    return 1;
                default:
                    switch (type) {
                        case 104:
                        case 105:
                        case 106:
                        case 107:
                        case SYS_OEM_NW_DIAG_CAUSE_LTE_A2_RELEASE_CELL /*{ENCODED_INT: 108}*/:
                        case 109:
                            break;
                        default:
                            switch (type) {
                                case SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_TIMEOUT /*{ENCODED_INT: 120}*/:
                                case SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_OUT_OF_BUFFER /*{ENCODED_INT: 121}*/:
                                case SYS_OEM_NW_DIAG_CAUSE_DATA_PDCP_UL_ONE_WAY_PASS /*{ENCODED_INT: 122}*/:
                                case SYS_OEM_NW_DIAG_CAUSE_DATA_DL_HIGH_BLER /*{ENCODED_INT: 123}*/:
                                case 124:
                                case SYS_OEM_NW_DIAG_CAUSE_PDP_DEACT_ERROR /*{ENCODED_INT: 125}*/:
                                    break;
                                default:
                                    return -1;
                            }
                    }
            }
        }
        return 5;
    }

    public void oppoProcessUnsolOemKeyLogErrMsg(Object ret) {
        CriticalLogInfo CLInfo = new CriticalLogInfo(-1, -1, -1, "", "");
        logd("oppoProcessUnsolOemKeyLogErrMsg is coming!");
        oppoGetInfoFromURC((String[]) ret, CLInfo);
        if (CLInfo.type == -1) {
            logd("EM_ID does not belong to critical log!");
            return;
        }
        logd("Get message, issue:" + CLInfo.issue + ", type:" + CLInfo.type + ", rat:" + CLInfo.rat + ", errcode:" + CLInfo.errcode + ", extra:" + CLInfo.extra);
        if (CLInfo.issue != null && !CLInfo.issue.equals("")) {
            String log = "type:" + CLInfo.type + ", rat:" + CLInfo.rat + ", errcode:" + CLInfo.errcode + ", " + CLInfo.extra;
            logd(log);
            logd("Write log, return:" + OppoManagerHelper.writeLogToPartition(this.mContext, "zz_oppo_critical_log_" + CLInfo.type, log, CLInfo.issue));
            if (CLInfo.type == 19) {
                HashMap<String, String> calldrop = new HashMap<>();
                calldrop.put(ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PAGE_FAIL, log);
                OppoManagerHelper.onStamp("050101", calldrop);
            }
            if (CLInfo.type == 89) {
                HashMap<String, String> dataAbnormalMap = new HashMap<>();
                dataAbnormalMap.put(ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT_OUT_OF_CREDIT, log);
                OppoManagerHelper.onStamp("050401", dataAbnormalMap);
            }
        }
    }

    public String getCellLocation() {
        Phone phone = PhoneFactory.getPhone(this.mPhoneId);
        int subId = -1;
        int slotId = -1;
        String mccMnc = "";
        String prop = SystemProperties.get("gsm.operator.numeric", "");
        if (prop != null && prop.length() > 0) {
            String[] values = prop.split(",");
            int i = this.mPhoneId;
            if (i >= 0 && i < values.length && values[i] != null) {
                mccMnc = values[i];
            }
        }
        int mcc = 0;
        int mnc = 0;
        if (mccMnc != null) {
            try {
                if (mccMnc.length() >= 3) {
                    mcc = Integer.parseInt(mccMnc.substring(0, 3));
                    mnc = Integer.parseInt(mccMnc.substring(3));
                    subId = phone.getSubId();
                    slotId = SubscriptionManager.getSlotIndex(subId);
                }
            } catch (Exception e) {
                logd("couldn't parse mcc/mnc: " + mccMnc);
            }
        }
        if (mcc == 460) {
            if (mnc == 2 || mnc == 7 || mnc == 8) {
                mnc = 0;
            }
            if (mnc == 6 || mnc == 9) {
                mnc = 1;
            }
            if (mnc == 3) {
                mnc = 11;
            }
        }
        String loc = "MCC:" + mcc + ", MNC:" + mnc + ", subId:" + subId + ", slotId:" + slotId;
        if (phone != null) {
            CellLocation cell = phone.getCellLocation();
            if (cell instanceof GsmCellLocation) {
                loc = loc + ", LAC:" + ((GsmCellLocation) cell).getLac() + ", CID:" + ((GsmCellLocation) cell).getCid();
            } else if (cell instanceof CdmaCellLocation) {
                loc = loc + ", SID:" + ((CdmaCellLocation) cell).getSystemId() + ", NID:" + ((CdmaCellLocation) cell).getNetworkId() + ", BID:" + ((CdmaCellLocation) cell).getBaseStationId();
            }
            SignalStrength signal = phone.getSignalStrength();
            if (signal != null) {
                loc = loc + ", signalstrength:" + signal.getDbm() + ", signallevel:" + signal.getLevel();
            }
        }
        logd("getCellLocation:" + loc);
        return loc;
    }

    /* access modifiers changed from: private */
    public void logd(String msg) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(this.TAG, msg);
        }
    }

    private void logi(String msg) {
        Rlog.i(this.TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(this.TAG, msg);
    }
}
