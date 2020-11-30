package mediatek.telecom;

import android.os.Bundle;
import java.util.ArrayList;

public class MtkTelecomHelper {

    public static class MtkInCallServiceHelper {
        private static final String ACTION_BLIND_OR_ASSURED_ECT = "blindAssuredEct";
        private static final String ACTION_EXPLICIT_CALL_TRANSFER = "explicitCallTransfer";
        private static final String ACTION_HANGUP_ACTIVE_AND_ANSWER_WAITING = "hangupActiveAndAnswerWaiting";
        private static final String ACTION_HANGUP_ALL = "hangupAll";
        private static final String ACTION_HANGUP_HOLD = "hangupAllHold";
        private static final String ACTION_INVITE_CONFERENCE_PARTICIPANTS = "inviteConferenceParticipants";
        private static final String ACTION_REJECT_CALL_WITH_CAUSE = "rejectcallwithcause";
        private static final String ACTION_SET_SORTED_INCOMING_CALL_LIST = "setSortedIncomingCallList";
        private static final String ACTION_START_VOICE_RECORDING = "startVoiceRecording";
        private static final String ACTION_STOP_VOICE_RECORDING = "stopVoiceRecording";
        private static final String KEY_ACTION = "key_action";
        private static final String PARAM_INT_TYPE = "param_int_type";
        private static final String PARAM_STRING_ARRAY_CALL_IDS = "param_string_array_call_ids";
        private static final String PARAM_STRING_ARRAY_LIST_NUMBERS = "param_string_array_list_numbers";
        private static final String PARAM_STRING_CALL_ID = "param_string_call_id";
        private static final String PARAM_STRING_PHONE_NUMBER = "param_string_phone_number";

        public static Bundle buildParamsForHangupHold() {
            return obtainBuilder(ACTION_HANGUP_HOLD).build();
        }

        public static Bundle buildParamsForHangupAll() {
            return obtainBuilder(ACTION_HANGUP_ALL).build();
        }

        public static Bundle buildParamsForHangupActiveAndAnswerWaiting() {
            return obtainBuilder(ACTION_HANGUP_ACTIVE_AND_ANSWER_WAITING).build();
        }

        public static Bundle buildParamsForExplicitCallTransfer(String callId) {
            return obtainBuilder(ACTION_EXPLICIT_CALL_TRANSFER).putStringParam(PARAM_STRING_CALL_ID, callId).build();
        }

        public static Bundle buildParamsForInviteConferenceParticipants(String callId, ArrayList<String> numbers) {
            return obtainBuilder(ACTION_INVITE_CONFERENCE_PARTICIPANTS).putStringParam(PARAM_STRING_CALL_ID, callId).putStringArrayListParam(PARAM_STRING_ARRAY_LIST_NUMBERS, numbers).build();
        }

        public static Bundle buildParamsForSetSortedIncomingCallList(ArrayList<String> callIds) {
            return obtainBuilder(ACTION_SET_SORTED_INCOMING_CALL_LIST).putStringArrayListParam(PARAM_STRING_ARRAY_CALL_IDS, callIds).build();
        }

        public static Bundle buildParamsForBlindOrAssuredEct(String callId, String phoneNumber, int type) {
            return obtainBuilder(ACTION_BLIND_OR_ASSURED_ECT).putStringParam(PARAM_STRING_CALL_ID, callId).putStringParam(PARAM_STRING_PHONE_NUMBER, phoneNumber).putIntParam(PARAM_INT_TYPE, type).build();
        }

        public static Bundle buildParamsForStartVoiceRecording() {
            return obtainBuilder(ACTION_START_VOICE_RECORDING).build();
        }

        public static Bundle buildParamsForStopVoiceRecording() {
            return obtainBuilder(ACTION_STOP_VOICE_RECORDING).build();
        }

        public static Bundle buildParamsForRejectCallWithCause(String callId, int cause) {
            return obtainBuilder(ACTION_REJECT_CALL_WITH_CAUSE).putStringParam(PARAM_STRING_CALL_ID, callId).putIntParam(PARAM_INT_TYPE, cause).build();
        }

        private static Builder obtainBuilder(String action) {
            return new Builder(action);
        }

        /* access modifiers changed from: private */
        public static class Builder {
            Bundle mBundle = new Bundle();

            Builder(String action) {
                this.mBundle.putString(MtkInCallServiceHelper.KEY_ACTION, action);
            }

            /* access modifiers changed from: package-private */
            public Builder putStringParam(String key, String value) {
                this.mBundle.putString(key, value);
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder putStringArrayListParam(String key, ArrayList<String> value) {
                this.mBundle.putStringArrayList(key, value);
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder putIntParam(String key, int value) {
                this.mBundle.putInt(key, value);
                return this;
            }

            /* access modifiers changed from: package-private */
            public Bundle build() {
                return this.mBundle;
            }
        }
    }

    public static class MtkInCallAdapterHelper {

        public interface ICommandProcessor {
            void blindOrAssuredEct(String str, String str2, int i);

            void explicitCallTransfer(String str);

            void hangupActiveAndAnswerWaiting();

            void hangupAll();

            void hangupHold();

            void inviteConferenceParticipants(String str, ArrayList<String> arrayList);

            void rejectWithCause(String str, int i);

            void setSortedIncomingCallList(ArrayList<String> arrayList);

            void startVoiceRecording();

            void stopVoiceRecording();
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public static void handleExtCommand(Bundle params, ICommandProcessor processor) {
            char c;
            String action = params.getString("key_action", "");
            switch (action.hashCode()) {
                case -1959827903:
                    if (action.equals("setSortedIncomingCallList")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -1041822165:
                    if (action.equals("hangupActiveAndAnswerWaiting")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -575486586:
                    if (action.equals("rejectcallwithcause")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 158621652:
                    if (action.equals("hangupAll")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 335715713:
                    if (action.equals("stopVoiceRecording")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 678192317:
                    if (action.equals("explicitCallTransfer")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 769377605:
                    if (action.equals("inviteConferenceParticipants")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1006787982:
                    if (action.equals("blindAssuredEct")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1269182945:
                    if (action.equals("startVoiceRecording")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 1979367091:
                    if (action.equals("hangupAllHold")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    processor.hangupAll();
                    return;
                case 1:
                    processor.explicitCallTransfer(params.getString("param_string_call_id"));
                    return;
                case 2:
                    processor.inviteConferenceParticipants(params.getString("param_string_call_id"), params.getStringArrayList("param_string_array_list_numbers"));
                    return;
                case 3:
                    processor.blindOrAssuredEct(params.getString("param_string_call_id"), params.getString("param_string_phone_number"), params.getInt("param_int_type"));
                    return;
                case MtkPhoneAccount.CARRIER_CAPABILITY_DISABLE_VT_OVER_WIFI /* 4 */:
                    processor.hangupActiveAndAnswerWaiting();
                    return;
                case 5:
                    processor.hangupHold();
                    return;
                case 6:
                    processor.setSortedIncomingCallList(params.getStringArrayList("param_string_array_call_ids"));
                    return;
                case 7:
                    processor.startVoiceRecording();
                    return;
                case MtkPhoneAccount.CARRIER_CAPABILITY_ROAMING_BAR_GUARD /* 8 */:
                    processor.stopVoiceRecording();
                    return;
                case '\t':
                    processor.rejectWithCause(params.getString("param_string_call_id"), params.getInt("param_int_type"));
                    return;
                default:
                    return;
            }
        }
    }
}
