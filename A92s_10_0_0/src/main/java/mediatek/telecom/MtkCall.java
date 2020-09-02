package mediatek.telecom;

import android.telecom.Call;

public class MtkCall {

    public static class MtkDetails {
        private static final int MTK_CAPABILITY_BASE = 16777216;
        public static final int MTK_CAPABILITY_BLIND_OR_ASSURED_ECT = 134217728;
        public static final int MTK_CAPABILITY_CALL_RECORDING = 16777216;
        public static final int MTK_CAPABILITY_CONSULTATIVE_ECT = 33554432;
        public static final int MTK_CAPABILITY_INVITE_PARTICIPANTS = 67108864;
        public static final int MTK_CAPABILITY_VIDEO_RINGTONE = 268435456;
        private static final int MTK_PROPERTY_BASE = 65536;
        public static final int MTK_PROPERTY_CDMA = 131072;
        public static final int MTK_PROPERTY_VOICE_RECORDING = 262144;
        public static final int MTK_PROPERTY_VOLTE = 65536;
        public static final int PROPERTY_CONFERENCE_PARTICIPANT = 2097152;
        public static final int PROPERTY_GTT_LOCAL = 524288;
        public static final int PROPERTY_GTT_REMOTE = 1048576;
        public static final int PROPERTY_RTT_SUPPORT_LOCAL = 8388608;
        public static final int PROPERTY_RTT_SUPPORT_REMOTE = 4194304;

        public static String capabilitiesToStringShort(int capabilities) {
            StringBuilder builder = new StringBuilder();
            builder.append("[Capabilities:");
            if (can(capabilities, 1)) {
                builder.append(" hld");
            }
            if (can(capabilities, 2)) {
                builder.append(" sup_hld");
            }
            if (can(capabilities, 4)) {
                builder.append(" mrg_cnf");
            }
            if (can(capabilities, 8)) {
                builder.append(" swp_cnf");
            }
            if (can(capabilities, 32)) {
                builder.append(" rsp_v_txt");
            }
            if (can(capabilities, 64)) {
                builder.append(" mut");
            }
            if (can(capabilities, MtkPhoneAccount.CARRIER_CAPABILITY_DISABLE_MO_CALL_DURING_CONFERENCE)) {
                builder.append(" mng_cnf");
            }
            if (can(capabilities, MtkPhoneAccount.CARRIER_CAPABILITY_SUPPORT_MTK_ENHANCED_CALL_BLOCKING)) {
                builder.append(" VTlrx");
            }
            if (can(capabilities, 512)) {
                builder.append(" VTltx");
            }
            if (can(capabilities, 1024)) {
                builder.append(" VTrrx");
            }
            if (can(capabilities, 2048)) {
                builder.append(" VTrtx");
            }
            if (can(capabilities, PROPERTY_RTT_SUPPORT_REMOTE)) {
                builder.append(" !v2a");
            }
            if (can(capabilities, 262144)) {
                builder.append(" spd_aud");
            }
            if (can(capabilities, PROPERTY_GTT_LOCAL)) {
                builder.append(" a2v");
            }
            if (can(capabilities, PROPERTY_GTT_REMOTE)) {
                builder.append(" paus_VT");
            }
            if (can(capabilities, PROPERTY_RTT_SUPPORT_LOCAL)) {
                builder.append(" pull");
            }
            if (can(capabilities, 16777216)) {
                builder.append(" m_rcrd");
            }
            if (can(capabilities, MTK_CAPABILITY_CONSULTATIVE_ECT)) {
                builder.append(" m_ect");
            }
            if (can(capabilities, MTK_CAPABILITY_INVITE_PARTICIPANTS)) {
                builder.append(" m_invite");
            }
            if (can(capabilities, 134217728)) {
                builder.append(" m_b|a_ect");
            }
            if (can(capabilities, 268435456)) {
                builder.append(" m_vt_tone");
            }
            builder.append("]");
            return builder.toString();
        }

        public static String propertiesToStringShort(int properties) {
            StringBuilder builder = new StringBuilder();
            builder.append("[Properties:");
            if (hasProperty(properties, 1)) {
                builder.append(" cnf");
            }
            if (hasProperty(properties, 2)) {
                builder.append(" gen_cnf");
            }
            if (hasProperty(properties, 8)) {
                builder.append(" wifi");
            }
            if (hasProperty(properties, 16)) {
                builder.append(" HD");
            }
            if (hasProperty(properties, 4)) {
                builder.append(" ecbm");
            }
            if (hasProperty(properties, 64)) {
                builder.append(" xtrnl");
            }
            if (hasProperty(properties, MtkPhoneAccount.CARRIER_CAPABILITY_DISABLE_MO_CALL_DURING_CONFERENCE)) {
                builder.append(" priv");
            }
            if (hasProperty(properties, 131072)) {
                builder.append(" m_cdma");
            }
            if (hasProperty(properties, 262144)) {
                builder.append(" m_rcrding");
            }
            if (hasProperty(properties, 65536)) {
                builder.append(" m_volte");
            }
            if (hasProperty(properties, PROPERTY_GTT_LOCAL)) {
                builder.append(" m_gtt_l");
            }
            if (hasProperty(properties, PROPERTY_GTT_REMOTE)) {
                builder.append(" m_gtt_r");
            }
            if (hasProperty(properties, PROPERTY_CONFERENCE_PARTICIPANT)) {
                builder.append(" m_cnf_chld");
            }
            if (hasProperty(properties, PROPERTY_RTT_SUPPORT_LOCAL)) {
                builder.append(" m_rtt_l");
            }
            if (hasProperty(properties, PROPERTY_RTT_SUPPORT_REMOTE)) {
                builder.append(" m_rtt_r");
            }
            builder.append("]");
            return builder.toString();
        }

        public static String deltaPropertiesToStringShort(int previousProp, int newProp) {
            int xorProperties = previousProp ^ newProp;
            return "Delta Properties Added: " + propertiesToStringShort(newProp & xorProperties) + ", Removed: " + propertiesToStringShort(previousProp & xorProperties);
        }

        public static String deltaCapabilitiesToStringShort(int previousCap, int newCap) {
            int xorProperties = previousCap ^ newCap;
            return "Delta Properties Added: " + capabilitiesToStringShort(newCap & xorProperties) + ", Removed: " + capabilitiesToStringShort(previousCap & xorProperties);
        }

        public static boolean can(int capabilities, int capability) {
            return Call.Details.can(capabilities, capability);
        }

        public static boolean hasProperty(int properties, int property) {
            return Call.Details.hasProperty(properties, property);
        }
    }
}
