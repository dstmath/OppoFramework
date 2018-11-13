package com.suntek.mway.rcs.client.api;

import android.content.Context;
import com.suntek.mway.rcs.client.api.basic.BasicApi;
import com.suntek.mway.rcs.client.api.blacklist.BlackListApi;
import com.suntek.mway.rcs.client.api.capability.CapabilityApi;
import com.suntek.mway.rcs.client.api.cloudfile.CloudFileApi;
import com.suntek.mway.rcs.client.api.contact.ContactApi;
import com.suntek.mway.rcs.client.api.emoticon.EmoticonApi;
import com.suntek.mway.rcs.client.api.groupchat.GroupChatApi;
import com.suntek.mway.rcs.client.api.message.MessageApi;
import com.suntek.mway.rcs.client.api.profile.ProfileApi;
import com.suntek.mway.rcs.client.api.publicaccount.PublicAccountApi;
import com.suntek.mway.rcs.client.api.richscreen.RichScreenApi;
import com.suntek.mway.rcs.client.api.specialnumber.SpecialServiceNumApi;
import com.suntek.mway.rcs.client.api.support.SupportApi;

public class ClientApi {
    public void init(Context context, ServiceListener serviceListener, ServiceListener pluginListener) {
        if (serviceListener != null) {
            ServiceApi.getInstance().init(context, serviceListener);
        }
        if (pluginListener != null) {
            PluginApi.getInstance().init(context, pluginListener);
        }
    }

    public void destory(Context context) {
        ServiceApi.getInstance().destory(context);
        PluginApi.getInstance().destory(context);
    }

    public BasicApi getBasicApi() {
        return BasicApi.getInstance();
    }

    public BlackListApi getBlackListApi() {
        return BlackListApi.getInstance();
    }

    public CapabilityApi getCapabilityApi() {
        return CapabilityApi.getInstance();
    }

    public GroupChatApi getGroupChatApi() {
        return GroupChatApi.getInstance();
    }

    public MessageApi getMessageApi() {
        return MessageApi.getInstance();
    }

    public SpecialServiceNumApi getSpecialServiceNumApi() {
        return SpecialServiceNumApi.getInstance();
    }

    public SupportApi getSupportApi() {
        return SupportApi.getInstance();
    }

    public ProfileApi getProfileApi() {
        return ProfileApi.getInstance();
    }

    public PublicAccountApi getPublicAccountApi() {
        return PublicAccountApi.getInstance();
    }

    public ContactApi getContactApi() {
        return ContactApi.getInstance();
    }

    public CloudFileApi getCloudFileApi() {
        return CloudFileApi.getInstance();
    }

    public EmoticonApi getEmoticonApi() {
        return EmoticonApi.getInstance();
    }

    public RichScreenApi getRichScreenApi() {
        return RichScreenApi.getInstance();
    }
}
