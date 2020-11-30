package com.mediatek.internal.telephony;

import android.telephony.Rlog;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.test.MtkSimulatedCommandsBase;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MtkOperatorUtils {
    private static final String TAG = "MtkOperatorUtils";
    private static final Map<OPID, List> mOPMap = new HashMap<OPID, List>() {
        /* class com.mediatek.internal.telephony.MtkOperatorUtils.AnonymousClass1 */

        {
            put(OPID.OP01, Arrays.asList("46000", "46002", "46004", "46007", "46008"));
            put(OPID.OP02, Arrays.asList("46001", "46006", "46009", "45407"));
            put(OPID.OP03, Arrays.asList("20801", "20802", "23101"));
            put(OPID.OP05, Arrays.asList("20201", "20202", "20416", "20420", "21630", "21901", "22004", "23001", "23203", "23204", "23430", "26002", "26201", "26206", "26278", "29702"));
            put(OPID.OP06, Arrays.asList("20205", "20404", "21401", "21406", "21670", "22210", "22601", "23415", "23591", "26202", "26204", "26209", "26801", "27201", "27402", "27403", "27801", "28602", "90128"));
            put(OPID.OP07, Arrays.asList("31030", "31070", "31080", "31090", "310150", "310170", "310280", "310380", "310410", "310560", "310680", "311180"));
            put(OPID.OP08, Arrays.asList("310160", MtkSimulatedCommandsBase.FAKE_MCC_MNC, "310490", "310580", "310660"));
            put(OPID.OP09, Arrays.asList("45502", "46003", "46011", "46012", "45507"));
            put(OPID.OP11, Arrays.asList("23420", "23594"));
            put(OPID.OP15, Arrays.asList("26203", "26207", "26208", "26211", "26277"));
            put(OPID.OP16, Arrays.asList("23430", "23431", "23432", "23433", "23434", "23486", "23501", "23502"));
            put(OPID.OP18, Arrays.asList("405840", "405854", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874"));
            put(OPID.OP19, Arrays.asList("50501", "50511", "50571", "50572"));
            put(OPID.OP50, Arrays.asList("44020"));
            put(OPID.OP100, Arrays.asList("45400", "45402", "45410", "45418"));
            put(OPID.OP101, Arrays.asList("45416", "45419", "45420", "45429"));
            put(OPID.OP102, Arrays.asList("45406", "45415", "45417", "45500", "45506"));
            put(OPID.OP103, Arrays.asList("52501", "52502", "52507"));
            put(OPID.OP106, Arrays.asList("45403", "45404", "45405"));
            put(OPID.OP107, Arrays.asList("20809", "20810", "20811", "20813"));
            put(OPID.OP108, Arrays.asList("46697"));
            put(OPID.OP109, Arrays.asList("46692"));
            put(OPID.OP110, Arrays.asList("46601", "46602", "46603", "46606", "46607", "46688"));
            put(OPID.OP117, Arrays.asList("51009", "51028"));
            put(OPID.OP118, Arrays.asList("502152"));
            put(OPID.OP124, Arrays.asList("46605"));
            put(OPID.OP125, Arrays.asList("52005", "52018"));
            put(OPID.OP128, Arrays.asList("24403", "24404", "24412", "24413"));
            put(OPID.OP131, Arrays.asList("52000", "52004", "52088", "52099"));
            put(OPID.OP132, Arrays.asList("72406", "72410", "72411", "72423"));
            put(OPID.OP134, Arrays.asList("24405", "24421"));
            put(OPID.OP137, Arrays.asList("20402", "25020"));
            put(OPID.OP143, Arrays.asList("28601"));
            put(OPID.OP150, Arrays.asList("22801", "29501"));
            put(OPID.OP147, Arrays.asList("40410", "40431", "40440", "40445", "40449", "40551", "40552", "40553", "40554", "40555", "40556", "40490", "40492", "40493", "40494", "40495", "40496", "40497", "40498", "40402", "40403", "40416", "40470"));
            put(OPID.OP148, Arrays.asList("45611"));
            put(OPID.OP152, Arrays.asList("50502"));
            put(OPID.OP154, Arrays.asList("24001", "24005"));
            put(OPID.OP156, Arrays.asList("23802", "23877"));
            put(OPID.OP161, Arrays.asList("26006"));
            put(OPID.OP175, Arrays.asList("23801", "23810"));
            put(OPID.OP176, Arrays.asList("46689"));
        }
    };

    public enum OPID {
        OP01,
        OP02,
        OP03,
        OP05,
        OP06,
        OP07,
        OP08,
        OP09,
        OP11,
        OP15,
        OP16,
        OP18,
        OP19,
        OP50,
        OP100,
        OP101,
        OP102,
        OP103,
        OP106,
        OP107,
        OP108,
        OP109,
        OP110,
        OP117,
        OP118,
        OP124,
        OP125,
        OP128,
        OP131,
        OP132,
        OP134,
        OP137,
        OP143,
        OP147,
        OP148,
        OP150,
        OP152,
        OP154,
        OP156,
        OP161,
        OP175,
        OP176
    }

    public static boolean isOperator(String mccMnc, OPID id) {
        boolean r = false;
        if (mOPMap.get(id).contains(mccMnc)) {
            r = true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("");
        sb.append(mccMnc);
        sb.append(r ? " = " : " != ");
        sb.append(idToString(id));
        Rlog.d(TAG, sb.toString());
        return r;
    }

    private static String idToString(OPID id) {
        if (id == OPID.OP01) {
            return DataSubConstants.OPERATOR_OP01;
        }
        if (id == OPID.OP02) {
            return DataSubConstants.OPERATOR_OP02;
        }
        if (id == OPID.OP03) {
            return "OP03";
        }
        if (id == OPID.OP05) {
            return "OP05";
        }
        if (id == OPID.OP06) {
            return "OP06";
        }
        if (id == OPID.OP07) {
            return "OP07";
        }
        if (id == OPID.OP08) {
            return "OP08";
        }
        if (id == OPID.OP09) {
            return DataSubConstants.OPERATOR_OP09;
        }
        if (id == OPID.OP11) {
            return "OP11";
        }
        if (id == OPID.OP15) {
            return "OP15";
        }
        if (id == OPID.OP16) {
            return "OP16";
        }
        if (id == OPID.OP18) {
            return DataSubConstants.OPERATOR_OP18;
        }
        if (id == OPID.OP50) {
            return "OP50";
        }
        if (id == OPID.OP100) {
            return "OP100";
        }
        if (id == OPID.OP101) {
            return "OP101";
        }
        if (id == OPID.OP102) {
            return "OP102";
        }
        if (id == OPID.OP106) {
            return "OP106";
        }
        if (id == OPID.OP107) {
            return "OP107";
        }
        if (id == OPID.OP108) {
            return "OP108";
        }
        if (id == OPID.OP109) {
            return "OP109";
        }
        if (id == OPID.OP110) {
            return "OP110";
        }
        if (id == OPID.OP118) {
            return "OP118";
        }
        if (id == OPID.OP124) {
            return "OP124";
        }
        if (id == OPID.OP128) {
            return "OP128";
        }
        if (id == OPID.OP131) {
            return "OP131";
        }
        if (id == OPID.OP134) {
            return "OP134";
        }
        if (id == OPID.OP137) {
            return "OP137";
        }
        if (id == OPID.OP143) {
            return "OP143";
        }
        if (id == OPID.OP152) {
            return "OP152";
        }
        if (id == OPID.OP154) {
            return "OP154";
        }
        if (id == OPID.OP156) {
            return "OP156";
        }
        if (id == OPID.OP161) {
            return "OP161";
        }
        if (id == OPID.OP175) {
            return "OP175";
        }
        if (id == OPID.OP176) {
            return "OP176";
        }
        return "ERR";
    }
}
