package com.android.internal.telephony;

import android.telephony.Rlog;
import com.android.internal.telephony.uicc.SpnOverride;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OppoRegSrvInfo {
    private static final String LOG_TAG = "OppoRegSrvInfo";
    private static final int SYS_OEM_NW_COLLECT_STATE_DOING = 1;
    private static final int SYS_OEM_NW_COLLECT_STATE_FINISHED = 2;
    private static final int SYS_OEM_NW_COLLECT_STATE_NOT_DOING = 0;
    private static final int SYS_OEM_NW_COLLECT_STATE_REPORTED = 3;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_ON = 75;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MCC = 73;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MNC = 74;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_RAT = 72;
    private static final int SYS_RAT_GSM_RADIO_ACCESS = 0;
    private static final int SYS_RAT_LTE_RADIO_ACCESS = 2;
    private static final int SYS_RAT_TDS_RADIO_ACCESS = 3;
    private static final int SYS_RAT_UMTS_RADIO_ACCESS = 1;
    private static RegSrvInfo[] g_regsrv_info;
    private static boolean isReboot = true;

    private static class RegSrvInfo {
        private static final String DATE_FORMAT_TAG = "yyyy-MM-dd HH:mm:ss";
        private int collect_state = 0;
        private float duration = 0.0f;
        private StringBuffer plmn_list = new StringBuffer();
        private Date start_time = new Date();
        private int subid = 0;

        public int getSubId() {
            return this.subid;
        }

        public int getCollectState() {
            return this.collect_state;
        }

        public float getDuration() {
            return this.duration;
        }

        public Date getStartTime() {
            return this.start_time;
        }

        public String getStartTimeStr() {
            return new SimpleDateFormat(DATE_FORMAT_TAG).format(this.start_time);
        }

        public StringBuffer getPlmnList() {
            return this.plmn_list;
        }

        public void setSubId(int slotid) {
            if (slotid == 0) {
                this.subid = 1;
            } else if (slotid == 1) {
                this.subid = 2;
            } else {
                Rlog.d(OppoRegSrvInfo.LOG_TAG, "setSubId() sun id invalid:" + slotid);
                this.subid = 1;
            }
        }

        public void setCollectState(int state) {
            this.collect_state = state;
        }

        public void setDuration() {
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_TAG);
            Date inservice_time = new Date(System.currentTimeMillis());
            Rlog.d(OppoRegSrvInfo.LOG_TAG, "start_time:" + formatter.format(this.start_time));
            Rlog.d(OppoRegSrvInfo.LOG_TAG, "inservice_time:" + formatter.format(inservice_time));
            this.duration = (float) (inservice_time.getTime() - this.start_time.getTime());
            Rlog.d(OppoRegSrvInfo.LOG_TAG, "duration(ms):" + this.duration);
            this.duration /= 1000.0f;
            Rlog.d(OppoRegSrvInfo.LOG_TAG, "duration(s):" + this.duration);
        }

        public void setStartTime() {
            this.start_time = new Date(System.currentTimeMillis());
        }

        public void setPlmnList(String value) {
            this.plmn_list = new StringBuffer(value);
        }
    }

    private static String ratToString(int rat) {
        switch (rat) {
            case 0:
                return "GSM";
            case 1:
                return "WCDMA";
            case 2:
                return "LTE";
            case 3:
                return "TDS";
            default:
                return "UnKonwn";
        }
    }

    public static void initRegSrvInfo() {
        g_regsrv_info = new RegSrvInfo[2];
        g_regsrv_info[0] = new RegSrvInfo();
        g_regsrv_info[1] = new RegSrvInfo();
    }

    public static boolean isCollectNotDoing(int sub) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub >= 0 && sub <= 1) {
            return g_regsrv_info[sub].getCollectState() == 0;
        } else {
            Rlog.d(LOG_TAG, "isCollectNotDoing() sun id invalid:" + sub);
            return false;
        }
    }

    public static boolean isCollectDoing(int sub) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub >= 0 && sub <= 1) {
            return g_regsrv_info[sub].getCollectState() == 1;
        } else {
            Rlog.d(LOG_TAG, "isCollectDoing() sun id invalid:" + sub);
            return false;
        }
    }

    public static boolean isCollectFinished(int sub) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub >= 0 && sub <= 1) {
            return g_regsrv_info[sub].getCollectState() == 2;
        } else {
            Rlog.d(LOG_TAG, "isCollectFinished() sun id invalid:" + sub);
            return false;
        }
    }

    public static boolean isCollectReported(int sub) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub >= 0 && sub <= 1) {
            return g_regsrv_info[sub].getCollectState() == 3;
        } else {
            Rlog.d(LOG_TAG, "isCollectReported() sun id invalid:" + sub);
            return false;
        }
    }

    public static void setCollectNotDoing(int sub) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub < 0 || sub > 1) {
            Rlog.d(LOG_TAG, "setCollectNotDoing() sun id invalid:" + sub);
        } else {
            g_regsrv_info[sub].setCollectState(0);
        }
    }

    public static void setCollectDoing(int sub) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub < 0 || sub > 1) {
            Rlog.d(LOG_TAG, "setCollectDoing() sun id invalid:" + sub);
        } else {
            g_regsrv_info[sub].setCollectState(1);
        }
    }

    public static void setCollectFinished(int sub) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub < 0 || sub > 1) {
            Rlog.d(LOG_TAG, "setCollectFinished() sun id invalid:" + sub);
        } else {
            g_regsrv_info[sub].setCollectState(2);
        }
    }

    public static void setCollectReported(int sub) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub < 0 || sub > 1) {
            Rlog.d(LOG_TAG, "setCollectReported() sun id invalid:" + sub);
        } else {
            g_regsrv_info[sub].setCollectState(3);
        }
    }

    public static String getCollectInfo(int sub) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub < 0 || sub > 1) {
            Rlog.d(LOG_TAG, "getCollectInfo() sun id invalid:" + sub);
            return SpnOverride.MVNO_TYPE_NONE;
        }
        String str_sub = "kacao:" + g_regsrv_info[sub].getSubId();
        String str_start_time = "start_time:" + g_regsrv_info[sub].getStartTimeStr();
        String str_duration = "duration:" + g_regsrv_info[sub].getDuration();
        return str_sub + str_start_time + str_duration + ("plmn_list:" + g_regsrv_info[sub].getPlmnList().toString());
    }

    public static boolean collectRegSrvInfo(int msgtype, int sub, int code) {
        if (sub == 1) {
            sub = 0;
        } else if (sub == 2) {
            sub = 1;
        }
        if (sub < 0 || sub > 1) {
            Rlog.d(LOG_TAG, "collectRegSrvInfo() sun id invalid:" + sub);
            return false;
        }
        if (isReboot) {
            isReboot = false;
            initRegSrvInfo();
        }
        switch (msgtype) {
            case 72:
                if (isCollectNotDoing(sub) || isCollectReported(sub)) {
                    g_regsrv_info[sub].setStartTime();
                    g_regsrv_info[sub].setSubId(sub);
                    g_regsrv_info[sub].setCollectState(1);
                    g_regsrv_info[sub].setPlmnList(ratToString(code));
                } else {
                    g_regsrv_info[sub].getPlmnList().append("|" + ratToString(code));
                }
                Rlog.d(LOG_TAG, "collectRegSrvInfo() REG_SRV_REQ msgtype:" + msgtype);
                break;
            case 73:
                if (isCollectDoing(sub)) {
                    g_regsrv_info[sub].getPlmnList().append("_" + String.valueOf(code));
                }
                Rlog.d(LOG_TAG, "collectRegSrvInfo() REG_SRV_MCC msgtype:" + msgtype);
                break;
            case 74:
                if (isCollectDoing(sub)) {
                    g_regsrv_info[sub].getPlmnList().append("_" + String.valueOf(code));
                }
                Rlog.d(LOG_TAG, "collectRegSrvInfo() REG_SRV_MNC msgtype:" + msgtype);
                break;
            case 75:
                if (isCollectDoing(sub)) {
                    Rlog.d(LOG_TAG, "collectRegSrvInfo() REG_SRV_IN_INFO msgtype:" + msgtype);
                    g_regsrv_info[sub].setDuration();
                    g_regsrv_info[sub].setCollectState(2);
                    break;
                }
                break;
            default:
                Rlog.d(LOG_TAG, "collectRegSrvInfo() ignore msgtype:" + msgtype);
                break;
        }
        if (msgtype < 72 || msgtype > 75) {
            return false;
        }
        Rlog.d(LOG_TAG, "collectRegSrvInfo() results sub:" + g_regsrv_info[sub].getSubId() + ",start_time:" + g_regsrv_info[sub].getStartTimeStr() + ",duration:" + g_regsrv_info[sub].getDuration() + ",plmn_list:" + g_regsrv_info[sub].getPlmnList().toString());
        return true;
    }
}
