package com.suntek.mway.rcs.client.aidl.common;

public interface DeviceApiConstant {

    public interface BlacklistProvider {
        public static final String CALL_ID = "BLACKLIST_CALL_ID";
        public static final String NAME = "BLACKLIST_NAME";
        public static final String PHONE_NUMBER = "BLACKLIST_PHONE_NUMBER";
    }

    public interface CapabilityProvider {
        public static final String CAPABILITY_EXTENSIONS = "CAPABILITY_EXTENSIONS";
        public static final String CAPABILITY_FILE_TRANSFER = "CAPABILITY_FILE_TRANSFER";
        public static final String CAPABILITY_GEOLOC_PUSH = "CAPABILITY_GEOLOC_PUSH";
        public static final String CAPABILITY_IMAGE_SHARING = "CAPABILITY_IMAGE_SHARING";
        public static final String CAPABILITY_IM_SESSION = "CAPABILITY_IM_SESSION";
        public static final String CAPABILITY_IP_VIDEO_CALL = "CAPABILITY_IP_VIDEO_CALL";
        public static final String CAPABILITY_IP_VOICE_CALL = "CAPABILITY_IP_VOICE_CALL";
        public static final String CAPABILITY_VIDEO_SHARING = "CAPABILITY_VIDEO_SHARING";
        public static final String CONTACT_NUMBER = "CONTACT_NUMBER";
    }

    public interface ChatProvider {
        public static final String BODY = "CHATMESSAGE_BODY";
        public static final String CHAT_ID = "CHATMESSAGE_CHAT_ID";
        public static final String CONTACT_NUMBER = "CHATMESSAGE_CONTACT_NUMBER";
        public static final String CONVERSATION = "CHATMESSAGE_CONVERSATION";
        public static final String CONVERSATION_ID = "CHATMESSAGE_CONVERSATION_ID";
        public static final int DELIVERED = 7;
        public static final String DIRECTION = "CHATMESSAGE_DIRECTION";
        public static final int FAILED = 5;
        public static final String FAVORITE = "CHATMESSAGE_FAVORITE";
        public static final String FILEICON = "CHATMESSAGE_FILEICON";
        public static final String FILENAME = "CHATMESSAGE_FILENAME";
        public static final String FILESIZE = "CHATMESSAGE_FILESIZE";
        public static final String FLAG = "CHATMESSAGE_FLAG";
        public static final int FT = 5;
        public static final int IM = 3;
        public static final int INCOMING = 0;
        public static final String ISBLOCKED = "CHATMESSAGE_ISBLOCKED";
        public static final String MESSAGE_COUNT = "CHATMESSAGE_MESSAGE_COUNT";
        public static final String MESSAGE_ID = "CHATMESSAGE_MESSAGE_ID";
        public static final String MESSAGE_STATUS = "CHATMESSAGE_MESSAGE_STATUS";
        public static final String MIME_TYPE = "CHATMESSAGE_MIME_TYPE";
        public static final int MMS = 2;
        public static final int MTM = 3;
        public static final int OFFICIAL = 4;
        public static final int OTM = 2;
        public static final int OTO = 1;
        public static final int OUTGOING = 1;
        public static final int READ = 2;
        public static final String RECIPIENTS = "CHATMESSAGE_RECIPIENTS";
        public static final int SENDING = 3;
        public static final int SENT = 4;
        public static final int SMS = 1;
        public static final int SMSMMS = 0;
        public static final String TIMESTAMP = "CHATMESSAGE_TIMESTAMP";
        public static final int TO_SEND = 6;
        public static final String TYPE = "CHATMESSAGE_TYPE";
        public static final int UNREAD = 0;
        public static final String UNREAD_COUNT = "CHATMESSAGE_UNREAD_COUNT";
        public static final int XML = 4;
    }

    public interface FileTransferProvider {
        public static final int ABORTED = 4;
        public static final String CONTACT_NUMBER = "CONTACT_NUMBER";
        public static final String DIRECTION = "DIRECTION";
        public static final int FAILED = 5;
        public static final String FILEICON = "FILEICON";
        public static final String FILENAME = "FILENAME";
        public static final String FILE_SIZE = "FILE_SIZE";
        public static final String FT_ID = "FT_ID";
        public static final int INITIATED = 1;
        public static final int INVITED = 0;
        public static final int PAUSED = 6;
        public static final int STARTED = 2;
        public static final String STATE = "STATE";
        public static final String TIMESTAMP = "TIMESTAMP";
        public static final int TRANSFERRED = 3;
        public static final String TRANSFERRED_SIZE = "TRANSFERRED";
        public static final String TYPE = "TYPE";
    }

    public interface GroupChatProvider {
        public static final int ABORTED = 6;
        public static final String CHAIRMEN = "GROUPCHATSERVICE_CHAIRMEN";
        public static final String CHAT_ID = "GROUPCHATSERVICE_CHAT_ID";
        public static final int CLOSED_BY_USER = 5;
        public static final String DIRECTION = "GROUPCHATSERVICE_DIRECTION";
        public static final int FAILED = 7;
        public static final int INITIATED = 2;
        public static final int INVITED = 1;
        public static final String MEMBER_NAME = "GROUPCHATSERVICE_MEMBER_NAME";
        public static final String NICK_NAME = "GROUPCHATSERVICE_NICK_NAME";
        public static final String PHONE_NUMBER = "GROUPCHATSERVICE_PHONE_NUMBER";
        public static final String PROTRAIT = "GROUPCHATSERVICE_PORTRAIT";
        public static final String PROTRAIT_TYPE = "GROUPCHATSERVICE_PORTRAIT_TYPE";
        public static final int STARTED = 3;
        public static final String STATE = "GROUPCHATSERVICE_STATE";
        public static final String SUBJECT = "GROUPCHATSERVICE_SUBJECT";
        public static final int TERMINATED = 4;
        public static final String TIME_STAMP = "GROUPCHATSERVICE_TIME_STAMP";
    }

    public interface ProfileProvider {
        public static final String ADDRESS = "PROFILE_ADDRESS";
        public static final String BIRTHDAY = "PROFILE_BIRTHDAY";
        public static final String BMP = "BMP";
        public static final String COMPANY = "PROFILE_COMPANY";
        public static final String COMPANY_ADDR = "PROFILE_COMPANY_ADDR";
        public static final String COMPANY_FAX = "PROFILE_COMPANY_FAX";
        public static final String COMPANY_TEL = "PROFILE_COMPANY_TEL";
        public static final String EMAIL = "PROFILE_EMAIL";
        public static final String FIRST_NAME = "PROFILE_FIRST_NAME";
        public static final String GIF = "GIF";
        public static final String HOME1 = "PROFILE_HOME1";
        public static final String HOME2 = "PROFILE_HOME2";
        public static final String HOME3 = "PROFILE_HOME3";
        public static final String HOME4 = "PROFILE_HOME4";
        public static final String HOME5 = "PROFILE_HOME5";
        public static final String HOME6 = "PROFILE_HOME6";
        public static final String JPEG = "JPEG";
        public static final String JPG = "JPG";
        public static final String LAST_NAME = "PROFILE_LAST_NAME";
        public static final String OTHER1 = "PROFILE_OTHER1";
        public static final String OTHER2 = "PROFILE_OTHER2";
        public static final String OTHER3 = "PROFILE_OTHER3";
        public static final String OTHER4 = "PROFILE_OTHER4";
        public static final String OTHER5 = "PROFILE_OTHER5";
        public static final String OTHER6 = "PROFILE_OTHER6";
        public static final String PHONE_NUMBER = "PROFILE_PHONENUMBER";
        public static final String PHONE_NUMBER_SECOND = "PROFILE_PHONE_NUMBER_SECOND";
        public static final String PNG = "PNG";
        public static final String PROTRAIT = "PROFILE_PORTRAIT";
        public static final String PROTRAIT_TYPE = "PROFILE_PORTRAIT_TYPE";
        public static final String TITLE = "PROFILE_TITLE";
        public static final String WORK1 = "PROFILE_WORK1";
        public static final String WORK2 = "PROFILE_WORK2";
        public static final String WORK3 = "PROFILE_WORK3";
        public static final String WORK4 = "PROFILE_WORK4";
        public static final String WORK5 = "PROFILE_WORK5";
        public static final String WORK6 = "PROFILE_WORK6";
    }

    public interface PublicAccountProvider {
        public static final String ACCOUNT = "PUBLICACCOUNTSERVICE_ACCOUNT";
        public static final String BMP = "BMP";
        public static final String BODY = "PUBLICACCOUNTSERVICE_BODY";
        public static final String BRIEF_INTRODUCTION = "PUBLICACCOUNTSERVICE_BRIEF_INTRODUCTION";
        public static final String CONFIG = "PUBLICACCOUNTSERVICE_CONFIG";
        public static final int DELIVERED = 7;
        public static final String DIRECTION = "PUBLICACCOUNTSERVICE_DIRECTION";
        public static final int FAILED = 5;
        public static final int FT = 5;
        public static final String GIF = "GIF";
        public static final String ID = "PUBLICACCOUNTSERVICE_ID";
        public static final int INCOMING = 0;
        public static final String JPEG = "JPEG";
        public static final String MESSAGE_ID = "PUBLICACCOUNTSERVICE_MESSAGE_ID";
        public static final String MESSAGE_STATUS = "PUBLICACCOUNTSERVICE_MESSAGE_STATUS";
        public static final String MIME_TYPE = "PUBLICACCOUNTSERVICE_MIME_TYPE";
        public static final String NAME = "PUBLICACCOUNTSERVICE_NAME";
        public static final int OUTGOING = 1;
        public static final String PNG = "PNG";
        public static final String PORTRAIT = "PUBLICACCOUNTSERVICE_PORTRAIT";
        public static final String PORTRAIT_TYPE = "PUBLICACCOUNTSERVICE_PORTRAIT_TYPE";
        public static final int READ = 2;
        public static final int SENDING = 3;
        public static final int SENT = 4;
        public static final String STATE = "PUBLICACCOUNTSERVICE_STATE";
        public static final String TIMESTAMP = "PUBLICACCOUNTSERVICE_TIMESTAMP";
        public static final int TO_SEND = 6;
        public static final String TYPE = "PUBLICACCOUNTSERVICE_TYPE";
        public static final int UNREAD = 0;
        public static final int XML = 4;
    }
}
