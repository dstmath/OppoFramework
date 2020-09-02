package cm.android.mdm.interfaces;

import java.util.List;
import java.util.Map;

public interface INetworkManager {

    public static final class Carriers {
        public static final String APN = "apn";
        public static final String AUTH_TYPE = "authtype";
        public static final String BEARER = "bearer";
        public static final String CARRIER_ENABLED = "carrier_enabled";
        public static final String CURRENT = "current";
        public static final String MCC = "mcc";
        public static final String MMSC = "mmsc";
        public static final String MMSPORT = "mmsport";
        public static final String MMSPROXY = "mmsproxy";
        public static final String MNC = "mnc";
        public static final String MVNO_MATCH_DATA = "mvno_match_data";
        public static final String MVNO_TYPE = "mvno_type";
        public static final String NAME = "name";
        public static final String NUMERIC = "numeric";
        public static final String PASSWORD = "password";
        public static final String PORT = "port";
        public static final String PROTOCOL = "protocol";
        public static final String PROXY = "proxy";
        public static final String ROAMING_PROTOCOL = "roaming_protocol";
        public static final String SERVER = "server";
        public static final String SUBSCRIPTION_ID = "sub_id";
        public static final String TYPE = "type";
        public static final String USER = "user";
    }

    void addApn(Map<String, String> map);

    void addNetworkRestriction(int i, List<String> list);

    Map<String, String> getApnInfo(String str);

    List<String> getSupportMethods();

    List<String> queryApn(Map<String, String> map);

    void removeApn(String str);

    void removeNetworkRestriction(int i);

    void removeNetworkRestriction(int i, List<String> list);

    void setNetworkRestriction(int i);

    void setPreferApn(String str);

    void updateApn(Map<String, String> map, String str);
}
