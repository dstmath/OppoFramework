package com.suntek.mway.rcs.client.aidl.common;

public class RcsColumns {

    public class DeviceApiColumns {
        public static final String BODY = "body";
        public static final String CONTACT_NUMBER = "contact_number";
        public static final String DIRECTION = "direction";
        public static final String FILEICON = "file_icon";
        public static final String FILENAME = "file_name";
        public static final String FILE_SIZE = "file_size";
        public static final String MESSAGE_ID = "message_id";
        public static final String STATE = "message_status";
        public static final String TIMESTAMP = "time_stamp";
        public static final String TRANSFERRED = "file_transferred";
        public static final String TYPE = "mime_type";
    }

    public class GroupStatusColumns {
        public static final String GROUP_DATE = "date";
        public static final String GROUP_NUMBER = "number";
        public static final String GROUP_STATUS = "status";
        public static final String MSG_ID = "msg_id";
        public static final String _ID = "_id";
    }

    public class SmsRcsColumns {
        public static final String PHONE_ID = "phone_id";
        public static final String RCS_BURN = "rcs_burn";
        public static final String RCS_BURN_BODY = "rcs_burn_body";
        public static final String RCS_CHAT_TYPE = "rcs_chat_type";
        public static final String RCS_CONTRIBUTION_ID = "rcs_contribution_id";
        public static final String RCS_CONVERSATION_ID = "rcs_conversation_id";
        public static final String RCS_EXT_CONTACT = "rcs_ext_contact";
        public static final String RCS_FAVOURITE = "favourite";
        public static final String RCS_FILENAME = "rcs_file_name";
        public static final String RCS_FILE_ICON = "rcs_file_icon";
        public static final String RCS_FILE_RECORD = "rcs_file_record";
        public static final String RCS_FILE_SELECTOR = "rcs_file_selector";
        public static final String RCS_FILE_SIZE = "rcs_file_size";
        public static final String RCS_FILE_TRANSFERED = "rcs_file_transfered";
        public static final String RCS_FILE_TRANSFER_ID = "rcs_file_transfer_id";
        public static final String RCS_HEADER = "rcs_header";
        public static final String RCS_IS_DOWNLOAD = "rcs_is_download";
        public static final String RCS_MEDIA_PLAYED = "rcs_media_played";
        public static final String RCS_MESSAGE_ID = "rcs_message_id";
        public static final String RCS_MIME_TYPE = "rcs_mime_type";
        public static final String RCS_MSG_STATE = "rcs_msg_state";
        public static final String RCS_MSG_TYPE = "rcs_msg_type";
        public static final String RCS_NMSG_STATE = "rcs_nmsg_state";
        public static final String RCS_PATH = "rcs_file_path";
        public static final String RCS_PLAY_TIME = "rcs_play_time";
        public static final String RCS_SEND_RECEIVE = "rcs_send_receive";
        public static final String RCS_THREAD_ID = "rcs_thread_id";
        public static final String RCS_THUMB_PATH = "rcs_thumb_path";
        public static final String SUB_ID = "sub_id";
    }

    public class ThreadColumns {
        public static final String RCS_CHAT_TYPE = "msg_chat_type";
        public static final String RCS_MSG_ID = "last_msg_id";
        public static final String RCS_MSG_TYPE = "last_msg_type";
        public static final String RCS_NUMBER = "rcs_number";
        public static final String RCS_TOP = "rcs_top";
        public static final String RCS_TOP_TIME = "rcs_top_time";
        public static final String RCS_UNREAD_COUNT = "rcs_unread_count";
    }
}
