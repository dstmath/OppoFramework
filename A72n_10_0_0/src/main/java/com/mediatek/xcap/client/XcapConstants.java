package com.mediatek.xcap.client;

public class XcapConstants {
    public static final String AUID_GROUPS = "org.openmobilealliance.groups";
    public static final String AUID_GROUP_USAGE_LIST = "org.openmobilealliance.group-usage-list";
    public static final String AUID_OMA_SEARCH = "org.openmobilealliance.search";
    public static final String AUID_PRES_RULES = "org.openmobilealliance.pres-rules";
    public static final String AUID_RESOURCE_LISTS = "resource-lists";
    public static final String AUID_USER_PROFILE = "org.openmobilealliance.user-profile";
    public static final String AUID_XCAP_CAPS = "xcap-caps";
    public static final String AUID_XCAP_DIRECTORY = "org.openmobilealliance.xcap-directory";
    public static final String HDR_KEY_ALLOW = "Allow";
    public static final String HDR_KEY_AUTHORIZATION = "Authorization";
    public static final String HDR_KEY_CONTENT_LENGTH = "Content-Length";
    public static final String HDR_KEY_CONTENT_TYPE = "Content-Type";
    public static final String HDR_KEY_ETAG = "ETag";
    public static final String HDR_KEY_HOST = "Host";
    public static final String HDR_KEY_IF_MATCH = "If-Match";
    public static final String HDR_KEY_IF_NONE_MATCH = "If-None-Match";
    public static final String HDR_KEY_WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String HDR_KEY_X_3GPP_ASSERTED_IDENTITY = "X-3GPP-Asserted-Identity";
    public static final String HDR_KEY_X_XCAP_ASSERTED_IDENTITY = "X-XCAP-Asserted-Identity";
    public static final String ROOT_SIMSERVS = "simservs";

    public enum Method {
        GET,
        PUT,
        DELETE
    }
}
