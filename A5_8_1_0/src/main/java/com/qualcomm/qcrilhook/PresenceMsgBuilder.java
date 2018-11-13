package com.qualcomm.qcrilhook;

import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiItemType;
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiStructType;
import com.qualcomm.qcrilhook.BaseQmiTypes.QmiBase;
import com.qualcomm.qcrilhook.PresenceOemHook.SubscriptionType;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiByte;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiInteger;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiNull;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiString;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class PresenceMsgBuilder {

    static class EventReport {

        static class EventReportMaskStruct extends BaseQmiItemType {
            QmiByte mMask;

            public EventReportMaskStruct(int mask) {
                this.mMask = new QmiByte(mask);
            }

            public byte[] toByteArray() {
                ByteBuffer tempBuf = QmiBase.createByteBuffer(getSize());
                tempBuf.put(this.mMask.toByteArray());
                return tempBuf.array();
            }

            public int getSize() {
                return 8;
            }

            public byte[] toTlv(short type) throws InvalidParameterException {
                ByteBuffer buf = QmiBase.createByteBuffer(getSize() + 3);
                try {
                    buf.put(PrimitiveParser.parseByte(type));
                    buf.putShort(PrimitiveParser.parseShort(getSize()));
                    buf.put(toByteArray());
                    return buf.array();
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException(e.toString());
                }
            }

            public String toString() {
                return String.format("[mMask_%s]", new Object[]{this.mMask});
            }
        }

        static class SetEventReport extends BaseQmiStructType {
            public static final short EVENT_REPORT_MASK_TYPE = (short) 1;
            EventReportMaskStruct mask;

            public SetEventReport(int mask) {
                this.mask = new EventReportMaskStruct(mask);
            }

            public BaseQmiItemType[] getItems() {
                return new BaseQmiItemType[]{this.mask};
            }

            public short[] getTypes() {
                return new short[]{(short) 1};
            }
        }

        EventReport() {
        }
    }

    static class NoTlvPayloadRequest extends BaseQmiStructType {
        public static final short IMS_ENABLER_REQ_TYPE = (short) 1;
        QmiNull noParam = null;

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.noParam};
        }

        public short[] getTypes() {
            return new short[]{(short) 1};
        }
    }

    static class NotifyFmt {

        static class SetFmt extends BaseQmiStructType {
            public static final short UPDATE_WITH_STRUCT_INFO_TYPE = (short) 1;
            QmiByte mUpdate_with_struct_info;

            public SetFmt(short flag) {
                this.mUpdate_with_struct_info = new QmiByte(flag);
            }

            public BaseQmiItemType[] getItems() {
                return new BaseQmiItemType[]{this.mUpdate_with_struct_info};
            }

            public short[] getTypes() {
                return new short[]{(short) 1};
            }
        }

        NotifyFmt() {
        }
    }

    static class Publish {

        static class Imsp_presence_info_struct extends BaseQmiItemType {
            QmiString mContact_uri;
            Imsp_presence_service_capabilities_struct mService_capabilities;
            Imsp_presence_service_description_struct mService_descriptions;

            public Imsp_presence_info_struct(String contact_uri, String description, String ver, String service_id, int is_audio_supported, int audio_capability, int is_video_supported, int video_capability) {
                this.mContact_uri = new QmiString(contact_uri);
                this.mService_descriptions = new Imsp_presence_service_description_struct(description, ver, service_id);
                this.mService_capabilities = new Imsp_presence_service_capabilities_struct(is_audio_supported, audio_capability, is_video_supported, video_capability);
            }

            public byte[] toByteArray() {
                ByteBuffer tempBuf = QmiBase.createByteBuffer(getSize());
                tempBuf.put((byte) this.mContact_uri.getSize());
                tempBuf.put(this.mContact_uri.toByteArray());
                tempBuf.put(this.mService_descriptions.toByteArray());
                tempBuf.put(this.mService_capabilities.toByteArray());
                return tempBuf.array();
            }

            public int getSize() {
                return ((this.mContact_uri.getSize() + 1) + this.mService_descriptions.getSize()) + this.mService_capabilities.getSize();
            }

            public String toString() {
                return String.format("[mContact_uri_%s], [mService_descriptions=%s], [mService_capabilities=%s] ", new Object[]{this.mContact_uri.toString(), this.mService_descriptions.toString(), this.mService_capabilities.toString()});
            }
        }

        static class Imsp_presence_service_capabilities_struct extends BaseQmiItemType {
            QmiInteger mAudio_capability;
            QmiByte mIs_audio_supported;
            QmiByte mIs_video_supported;
            QmiInteger mVideo_capability;

            public Imsp_presence_service_capabilities_struct(int is_audio_supported, int audio_capability, int is_video_supported, int video_capability) {
                this.mIs_audio_supported = new QmiByte(is_audio_supported);
                this.mAudio_capability = new QmiInteger((long) audio_capability);
                this.mIs_video_supported = new QmiByte(is_video_supported);
                this.mVideo_capability = new QmiInteger((long) video_capability);
            }

            public byte[] toByteArray() {
                ByteBuffer tempBuf = QmiBase.createByteBuffer(getSize());
                tempBuf.put(this.mIs_audio_supported.toByteArray());
                tempBuf.put(this.mAudio_capability.toByteArray());
                tempBuf.put(this.mIs_video_supported.toByteArray());
                tempBuf.put(this.mVideo_capability.toByteArray());
                return tempBuf.array();
            }

            public int getSize() {
                return ((this.mIs_audio_supported.getSize() + this.mAudio_capability.getSize()) + this.mIs_video_supported.getSize()) + this.mVideo_capability.getSize();
            }

            public byte[] toTlv(short type) throws InvalidParameterException {
                ByteBuffer buf = QmiBase.createByteBuffer(getSize() + 2);
                buf.putShort(PrimitiveParser.parseShort(getSize()));
                buf.put(toByteArray());
                return buf.array();
            }

            public String toString() {
                return String.format("[mIs_audio_supported_%s], [mAudio_capability_%s], [mIs_video_supported_%s], [mVideo_capability_%s]", new Object[]{this.mIs_audio_supported.toString(), this.mAudio_capability.toString(), this.mIs_video_supported.toString(), this.mVideo_capability.toString()});
            }
        }

        static class Imsp_presence_service_description_struct extends BaseQmiItemType {
            QmiString mDescription;
            QmiString mService_id;
            QmiString mVer;

            public Imsp_presence_service_description_struct(String description, String ver, String service_id) {
                this.mDescription = new QmiString(description);
                this.mVer = new QmiString(ver);
                this.mService_id = new QmiString(service_id);
            }

            public byte[] toByteArray() {
                ByteBuffer tempBuf = QmiBase.createByteBuffer(getSize());
                tempBuf.put((byte) this.mDescription.getSize());
                tempBuf.put(this.mDescription.toByteArray());
                tempBuf.put((byte) this.mVer.getSize());
                tempBuf.put(this.mVer.toByteArray());
                tempBuf.put((byte) this.mService_id.getSize());
                tempBuf.put(this.mService_id.toByteArray());
                return tempBuf.array();
            }

            public int getSize() {
                return ((((this.mDescription.getSize() + 1) + this.mVer.getSize()) + 1) + this.mService_id.getSize()) + 1;
            }

            public byte[] toTlv(short type) throws InvalidParameterException {
                ByteBuffer buf = QmiBase.createByteBuffer(getSize() + 2);
                buf.putShort(PrimitiveParser.parseShort(getSize()));
                buf.put(toByteArray());
                return buf.array();
            }

            public String toString() {
                return String.format("[mDescription_%s],[mVer_%s], [mService_id_%s]", new Object[]{this.mDescription.toString(), this.mVer.toString(), this.mService_id.toString()});
            }
        }

        static class PublishStructRequest extends BaseQmiStructType {
            public static final short PUBLISH_PRESENCE_INFO_TYPE = (short) 16;
            public static final short PUBLISH_STATUS_TYPE = (short) 1;
            Imsp_presence_info_struct mPresence_info;
            QmiInteger mPublish_status;

            public PublishStructRequest(int publish_status, String contact_uri, String description, String ver, String service_id, int is_audio_supported, int audio_capability, int is_video_supported, int video_capability) {
                this.mPublish_status = new QmiInteger((long) publish_status);
                this.mPresence_info = new Imsp_presence_info_struct(contact_uri, description, ver, service_id, is_audio_supported, audio_capability, is_video_supported, video_capability);
            }

            public BaseQmiItemType[] getItems() {
                return new BaseQmiItemType[]{this.mPublish_status, this.mPresence_info};
            }

            public short[] getTypes() {
                return new short[]{(short) 1, (short) 16};
            }
        }

        static class PublishXMLRequest extends BaseQmiStructType {
            public static final short PUBLISH_XML_TYPE = (short) 1;
            QmiString publishXml;

            public PublishXMLRequest(String xml) {
                this.publishXml = new QmiString(xml);
            }

            public BaseQmiItemType[] getItems() {
                return new BaseQmiItemType[]{this.publishXml};
            }

            public short[] getTypes() {
                return new short[]{(short) 1};
            }
        }

        Publish() {
        }
    }

    static class Subscribe {

        static class Imsp_user_info_struct extends BaseQmiItemType {
            Imsp_user_uri_struct imsp_user_uri;
            QmiByte subscribe_user_list_len;

            public Imsp_user_info_struct(ArrayList<String> contactList) {
                this.subscribe_user_list_len = new QmiByte(contactList.size());
                this.imsp_user_uri = new Imsp_user_uri_struct(contactList);
            }

            public byte[] toByteArray() {
                ByteBuffer tempBuf = QmiBase.createByteBuffer(getSize());
                tempBuf.put(this.subscribe_user_list_len.toByteArray());
                tempBuf.put(this.imsp_user_uri.toByteArray());
                return tempBuf.array();
            }

            public int getSize() {
                return this.subscribe_user_list_len.getSize() + this.imsp_user_uri.getSize();
            }

            public byte[] toTlv(short type) throws InvalidParameterException {
                ByteBuffer buf = QmiBase.createByteBuffer(getSize() + 3);
                try {
                    buf.put(PrimitiveParser.parseByte(type));
                    buf.putShort(PrimitiveParser.parseShort(getSize()));
                    buf.put(toByteArray());
                    return buf.array();
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException(e.toString());
                }
            }

            public String toString() {
                return String.format("[subscribe_user_list_len_%s], [imsp_user_uri=%s]", new Object[]{this.subscribe_user_list_len.toString(), this.imsp_user_uri.toString()});
            }
        }

        static class Imsp_user_uri_struct extends BaseQmiItemType {
            ArrayList<QmiString> imsp_user_uri = new ArrayList();
            ArrayList<QmiByte> imsp_user_uri_len = new ArrayList();
            int mCompleteLen = 0;
            ArrayList<String> mContactList;
            int mNum;

            public Imsp_user_uri_struct(ArrayList<String> contactList) {
                this.mNum = contactList.size();
                this.mContactList = contactList;
                for (String s : contactList) {
                    int len = s.length();
                    this.mCompleteLen += len;
                    this.imsp_user_uri_len.add(new QmiByte(len));
                    this.imsp_user_uri.add(new QmiString(s));
                }
            }

            public byte[] toByteArray() {
                ByteBuffer tempBuf = QmiBase.createByteBuffer(getSize());
                for (int i = 0; i < this.mNum; i++) {
                    tempBuf.put(((QmiByte) this.imsp_user_uri_len.get(i)).toByteArray());
                    tempBuf.put(((QmiString) this.imsp_user_uri.get(i)).toByteArray());
                }
                return tempBuf.array();
            }

            public int getSize() {
                return this.mCompleteLen + this.mNum;
            }

            public byte[] toTlv(short type) throws InvalidParameterException {
                ByteBuffer buf = QmiBase.createByteBuffer(getSize());
                buf.put(toByteArray());
                return buf.array();
            }

            public String toString() {
                String temp = "";
                for (int i = 0; i < this.mNum; i++) {
                    temp = temp + String.format("[Contact[%d]_%s]", new Object[]{Integer.valueOf(i), ((QmiString) this.imsp_user_uri.get(i)).toString()});
                }
                return temp;
            }
        }

        static class SubscribeStructRequest extends BaseQmiStructType {
            public static final short IMSP_SUBSCRIPTION_TYPE = (short) 1;
            public static final short IMSP_USER_INFO = (short) 2;
            Imsp_user_info_struct mUserInfo;
            QmiInteger subscriptionType;

            public SubscribeStructRequest(SubscriptionType subscriptionType, ArrayList<String> contactList) {
                this.subscriptionType = new QmiInteger((long) subscriptionType.ordinal());
                this.mUserInfo = new Imsp_user_info_struct(contactList);
            }

            public BaseQmiItemType[] getItems() {
                return new BaseQmiItemType[]{this.subscriptionType, this.mUserInfo};
            }

            public short[] getTypes() {
                return new short[]{(short) 1, (short) 2};
            }
        }

        static class SubscribeXMLRequest extends BaseQmiStructType {
            public static final short SUBSCRIBE_XML_TYPE = (short) 1;
            QmiString subscribeXml;

            public SubscribeXMLRequest(String xml) {
                this.subscribeXml = new QmiString(xml);
            }

            public BaseQmiItemType[] getItems() {
                return new BaseQmiItemType[]{this.subscribeXml};
            }

            public short[] getTypes() {
                return new short[]{(short) 1};
            }
        }

        Subscribe() {
        }
    }

    static class UnPublish {

        static class UnPublishRequest extends BaseQmiStructType {
            public static final short UNPUBLISH_REQ_TYPE = (short) 1;
            QmiNull noParam = new QmiNull();

            public BaseQmiItemType[] getItems() {
                return new BaseQmiItemType[]{this.noParam};
            }

            public short[] getTypes() {
                return new short[]{(short) 1};
            }
        }

        UnPublish() {
        }
    }

    static class UnSubscribe {

        static class UnSubscribeRequest extends BaseQmiStructType {
            public static final short PEER_URI_TYPE = (short) 1;
            QmiString peerURI;

            public UnSubscribeRequest(String peerURI) {
                this.peerURI = new QmiString(peerURI);
            }

            public BaseQmiItemType[] getItems() {
                return new BaseQmiItemType[]{this.peerURI};
            }

            public short[] getTypes() {
                return new short[]{(short) 1};
            }
        }

        UnSubscribe() {
        }
    }
}
