package com.mediatek.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;

public class MtkContactsContract {

    public interface ContactsColumns {
        public static final String FILTER = "filter";
        public static final int FILTER_NONE = 0;
        public static final int FILTER_WIDGET = 1;
        public static final String INDEX_IN_SIM = "index_in_sim";
        public static final String INDICATE_PHONE_SIM = "indicate_phone_or_sim_contact";
        public static final String IS_SDN_CONTACT = "is_sdn_contact";
        public static final String SEND_TO_VOICEMAIL_SIP = "send_to_voicemail_sip";
        public static final String SEND_TO_VOICEMAIL_VT = "send_to_voicemail_vt";
    }

    public interface DataColumns {
        public static final String IS_ADDITIONAL_NUMBER = "is_additional_number";
    }

    public static final class Groups {
        public static final String QUERY_WITH_GROUP_ID = "query_with_group_id";
    }

    public static final class PhoneLookup implements ContactsColumns {
    }

    public static final class RawContacts {
        public static final int INDICATE_PHONE = -1;
        public static final String TIMESTAMP = "timestamp";
    }

    public interface RawContactsColumns {
        public static final String INDEX_IN_SIM = "index_in_sim";
        public static final String INDICATE_PHONE_SIM = "indicate_phone_or_sim_contact";
        public static final String IS_SDN_CONTACT = "is_sdn_contact";
        public static final String SEND_TO_VOICEMAIL_SIP = "send_to_voicemail_sip";
        public static final String SEND_TO_VOICEMAIL_VT = "send_to_voicemail_vt";
    }

    public static final class CommonDataKinds {

        public static final class Phone {
            public static final CharSequence getTypeLabel(Context context, int type, CharSequence label) {
                if (type == 102) {
                    return "";
                }
                if (type != 101) {
                    return ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, label);
                }
                if (!TextUtils.isEmpty(label)) {
                    return Aas.getLabel(context.getContentResolver(), label);
                }
                return "";
            }
        }

        public static final class ImsCall {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/ims";
            public static final String DATA = "data1";
            public static final String LABEL = "data3";
            public static final String TYPE = "data2";
            public static final String URL = "data1";

            private ImsCall() {
            }
        }
    }

    public static final class Aas {
        public static final String AAS_METHOD = "get_aas";
        public static final String ENCODE_SYMBOL = "-";
        public static final String KEY_AAS = "aas";
        public static final String LABEL_EMPTY = "";
        public static final int PHONE_TYPE_AAS = 101;
        public static final int PHONE_TYPE_EMPTY = 102;

        public static final String buildIndicator(int subId, int indexInSim) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(subId);
            stringBuffer.append(ENCODE_SYMBOL);
            stringBuffer.append(indexInSim);
            return stringBuffer.toString();
        }

        public static CharSequence getLabel(ContentResolver resolver, CharSequence indicator) {
            Bundle response = resolver.call(ContactsContract.AUTHORITY_URI, AAS_METHOD, indicator.toString(), (Bundle) null);
            if (response != null) {
                return response.getCharSequence(KEY_AAS, "");
            }
            return "";
        }
    }
}
