package com.suntek.mway.rcs.client.aidl.constant;

public class Actions {
    public static final String ACTION_ERROR = "com.suntek.mway.rcs.app.service.ACTION_ERROR";

    public static class DmsAction {
        public static final String ACTION_DMS_CONFIRM_USE_NEW_IMSI = "com.suntek.mway.rcs.app.service.ACTION_DMS_CONFIRM_USE_NEW_IMSI";
        public static final String ACTION_DMS_FETCH_CONFIG_ERROR = "com.suntek.mway.rcs.app.service.ACTION_DMS_FETCH_CONFIG_ERROR";
        public static final String ACTION_DMS_FETCH_CONFIG_FINISH = "com.suntek.mway.rcs.app.service.ACTION_DMS_FETCH_CONFIG_FINISH";
        public static final String ACTION_DMS_INPUT_SMS_VERIFY_CODE = "com.suntek.mway.rcs.app.service.ACTION_DMS_INPUT_SMS_VERIFY_CODE";
        public static final String ACTION_DMS_NEW_CONFIG = "com.suntek.mway.rcs.app.service.ACTION_DMS_NEW_CONFIG";
        public static final String ACTION_DMS_OPEN_ACCOUNT = "com.suntek.mway.rcs.app.service.ACTION_DMS_OPEN_ACCOUNT";
        public static final String ACTION_DMS_OPEN_ACCOUNT_RESULT = "com.suntek.mway.rcs.app.service.ACTION_DMS_OPEN_ACCOUNT_RESULT";
        public static final String ACTION_DMS_OPEN_PS = "com.suntek.mway.rcs.app.service.ACTION_DMS_OPEN_PS";
        public static final String ACTION_DMS_SHOW_DIALOG_INFO = "com.suntek.mway.rcs.app.service.ACTION_DMS_SHOW_DIALOG_INFO";
        public static final String ACTION_DMS_UPDATE_CONFIG = "com.suntek.mway.rcs.app.service.ACTION_DMS_UPDATE_CONFIG";
        public static final String ACTION_DMS_USER_STATUS_CHANGED = "com.suntek.mway.rcs.app.service.ACTION_DMS_USER_STATUS_CHANGED";
        public static final String ACTION_DMS_VERSION_REFRESH = "com.suntek.mway.rcs.ACTION_UI_RCS_DM_VERSION_REFRESH";
        public static final String ACTION_RCS_ENABLE_CHANGED = "com.suntek.mway.rcs.ACTION_RCS_ENABLE_CHANGED";
        public static final String ACTION_UI_SIM_NOT_BELONG_CMCC = "com.suntek.mway.rcs.app.service.ACTION_UI_SIM_NOT_BELONG_CMCC";
    }

    public static class GroupChatAction {
        public static final String ACTION_GROUP_CHAT_INVITE = "com.suntek.mway.rcs.app.service.ACTION_GROUP_CHAT_INVITE";
        public static final String ACTION_GROUP_CHAT_MANAGE_FAILED = "com.suntek.mway.rcs.app.service.ACTION_GROUP_CHAT_MANAGE_FAILED";
        public static final String ACTION_GROUP_CHAT_MANAGE_NOTIFY = "com.suntek.mway.rcs.app.service.ACTION_GROUP_CHAT_MANAGE_NOTIFY";
    }

    public static class MessageAction {
        public static final String ACTION_MESSAGE_BACKUP = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_BACKUP";
        public static final String ACTION_MESSAGE_DOWNLOAD_EMOTICON_RESULT = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_DOWNLOAD_EMOTICON_RESULT";
        public static final String ACTION_MESSAGE_EMOTICON_DOWNLOAD_RESULT = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_EMOTICON_DOWNLOAD_RESULT";
        public static final String ACTION_MESSAGE_FILE_TRANSFER_PROGRESS = "com.suntek.rcs.action.ACTION_MESSAGE_FILE_TRANSFER_PROGRESS";
        public static final String ACTION_MESSAGE_FIREWALL_BLOCK_RECORD = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_FIREWALL_BLOCK_RECORD";
        public static final String ACTION_MESSAGE_GROUP_CHAT_NOTIFY = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_GROUP_CHAT_NOTIFY";
        public static final String ACTION_MESSAGE_NOTIFY = "com.suntek.rcs.action.ACTION_MESSAGE_NOTIFY";
        public static final String ACTION_MESSAGE_REPORT = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_REPORT";
        public static final String ACTION_MESSAGE_RESTORE = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_RESTORE";
        public static final String ACTION_MESSAGE_SEND_FAILED = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_SEND_FAILED";
        public static final String ACTION_MESSAGE_SHOW_COMPOSING = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_SHOW_COMPOSING";
        public static final String ACTION_MESSAGE_SMS_POLICY_NOT_SET = "com.suntek.rcs.action.ACTION_MESSAGE_SMS_POLICY_NOT_SET";
        public static final String ACTION_MESSAGE_STATUS_CHANGED = "com.suntek.rcs.action.ACTION_MESSAGE_STATUS_CHANGED";
        public static final String ACTION_MESSAGE_TRANSFERED_SMS = "com.suntek.mway.rcs.app.service.ACTION_MESSAGE_TRANSFERED_SMS";
        public static final String ACTION_O2M_DETAIL_MESSAGE_STATUS_CHANGED = "com.suntek.mway.rcs.app.service.ACTION_O2M_DETAIL_MESSAGE_STATUS_CHANGED";
    }

    public static class PermissionAction {
        public static final String ACTION_PERMISSION_NO_GRANTED_GBA = "com.cmcc.proxy.ACTION_PERMISSION_NO_GRANTED";
        public static final String ACTION_PERMISSION_NO_GRANTED_PLUGIN = "com.suntek.mway.rcs.app.plugin.ACTION_PERMISSION_NO_GRANTED";
        public static final String ACTION_PERMISSION_NO_GRANTED_SERVICE = "com.suntek.mway.rcs.app.service.ACTION_PERMISSION_NO_GRANTED";
    }

    public static class PluginAction {
        public static final String ACTION_MCLOUD_DOWNLOAD_FILE_FROM_URL = "com.suntek.mway.rcs.app.service.ACTION_MCLOUD_DOWNLOAD_FILE_FROM_URL";
        public static final String ACTION_MCLOUD_GET_REMOTE_FILE_LIST = "com.suntek.mway.rcs.app.service.ACTION_MCLOUD_GET_REMOTE_FILE_LIST";
        public static final String ACTION_MCLOUD_GET_SHARE_FILE_LIST = "com.suntek.mway.rcs.app.service.ACTION_MCLOUD_GET_SHARE_FILE_LIST";
        public static final String ACTION_MCLOUD_PUT_FILE = "com.suntek.mway.rcs.app.service.ACTION_MCLOUD_PUT_FILE";
        public static final String ACTION_MCLOUD_SHARE_AND_SEND_FILE = "com.suntek.mway.rcs.app.service.ACTION_MCLOUD_SHARE_AND_SEND_FILE";
        public static final String ACTION_MCLOUD_SHARE_FILE = "com.suntek.mway.rcs.app.service.ACTION_MCLOUD_SHARE_FILE";
        public static final String ACTION_PLUGIN_APK_UNINSTALLED = "com.suntek.mway.rcs.app.service.ACTION_PLUGIN_APK_UNINSTALLED";
        public static final String ACTION_PLUGIN_INIT_FAILED = "com.suntek.mway.rcs.app.service.ACTION_PLUGIN_INIT_FAILED";
        public static final String ACTION_PLUGIN_INIT_START = "com.suntek.mway.rcs.app.service.ACTION_PLUGIN_INIT_START";
        public static final String ACTION_PLUGIN_INIT_SUCCESS = "com.suntek.mway.rcs.app.service.ACTION_PLUGIN_INIT_SUCCESS";
        public static final String ACTION_PROFILE_UPDATE_CONTACT = "com.suntek.mway.rcs.app.service.ACTION_PROFILE_UPDATE_CONTACT";
    }

    public static class RegisterAction {
        public static final String ACTION_REGISTER_STATUS_CHANGED = "com.suntek.mway.rcs.app.service.ACTION_REGISTER_STATUS_CHANGED";
        public static final String ACTION_UNREGISTER_FINISHED = "com.suntek.mway.rcs.app.service.ACTION_UNREGISTER_FINISHED";
    }
}
