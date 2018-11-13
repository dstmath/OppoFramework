package com.suntek.rcs.ui.common.mms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.suntek.mway.rcs.client.aidl.constant.Actions.GroupChatAction;

public class GroupChatManagerReceiver extends BroadcastReceiver {
    private GroupChatNotifyCallback mCallback;

    public interface GroupChatNotifyCallback {
        void onBootMe(Bundle bundle);

        void onCreateNotActive(Bundle bundle);

        void onDeparted(Bundle bundle);

        void onDisband(Bundle bundle);

        void onGroupChatCreate(Bundle bundle);

        void onGroupGone(Bundle bundle);

        void onGroupInviteExpired(Bundle bundle);

        void onMemberAliasChange(Bundle bundle);

        void onUpdateRemark(Bundle bundle);

        void onUpdateSubject(Bundle bundle);
    }

    public GroupChatManagerReceiver(GroupChatNotifyCallback callback) {
        this.mCallback = callback;
    }

    public void onReceive(Context context, Intent intent) {
        if (GroupChatAction.ACTION_GROUP_CHAT_MANAGE_NOTIFY.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            int actionType = intent.getIntExtra("type", 0);
            if (1 == actionType) {
                if (this.mCallback != null) {
                    this.mCallback.onGroupChatCreate(extras);
                }
            } else if (5 == actionType) {
                if (this.mCallback != null) {
                    this.mCallback.onMemberAliasChange(extras);
                }
            } else if (9 == actionType) {
                if (this.mCallback != null) {
                    this.mCallback.onDisband(extras);
                }
            } else if (8 == actionType) {
                if (this.mCallback != null) {
                    this.mCallback.onDeparted(extras);
                }
            } else if (3 == actionType) {
                if (this.mCallback != null) {
                    this.mCallback.onUpdateSubject(extras);
                }
            } else if (4 == actionType) {
                if (this.mCallback != null) {
                    this.mCallback.onUpdateRemark(extras);
                }
            } else if (10 == actionType) {
                if (this.mCallback != null) {
                    this.mCallback.onCreateNotActive(extras);
                }
            } else if (7 == actionType) {
                if (this.mCallback != null) {
                    this.mCallback.onBootMe(extras);
                }
            } else if (10 == actionType) {
                if (this.mCallback != null) {
                    this.mCallback.onGroupGone(extras);
                }
            } else if (14 == actionType && this.mCallback != null) {
                this.mCallback.onGroupInviteExpired(extras);
            }
        }
    }
}
