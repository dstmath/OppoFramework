package com.suntek.mway.rcs.client.api.parse;

import com.suntek.mway.rcs.client.aidl.constant.Constants.FavoriteMessageProvider.FavoriteMessage;
import com.suntek.mway.rcs.client.aidl.constant.Parameter;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicMediaMessage;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicMediaMessage.PublicMediaContent;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PublicMediaMessageParser extends DefaultHandler {
    private StringBuilder builder = new StringBuilder();
    private PublicMediaContent content;
    private PublicMediaMessage message;

    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        this.builder.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        String s = this.builder.toString();
        if (localName.equals("create_time")) {
            this.message.setCreatetime(s);
        } else if (localName.equals("forwardable")) {
            this.message.setForwardable(Integer.parseInt(s));
        } else if (localName.equals("media_type")) {
            this.message.setMsgtype(s);
        } else if (localName.equals("activeStatus")) {
            this.message.setActiveStatus(Integer.parseInt(s));
        } else if (localName.equals("pa_uuid")) {
            this.message.setPaUuid(s);
        } else if (localName.equals("thumb_link")) {
            this.content.setThumbLink(s);
        } else if (localName.equals("original_link")) {
            this.content.setOriginalLink(s);
        } else if (localName.equals(Parameter.EXTRA_DISPLAY_TITLE)) {
            this.content.setTitle(s);
        } else if (localName.equals(FavoriteMessage.FILE_SIZE)) {
            this.content.setFileSize(s);
        } else if (localName.equals("duration")) {
            this.content.setDuration(s);
        } else if (localName.equals("filetype")) {
            this.content.setFileType(s);
        } else if (localName.equals("media_uuid")) {
            this.content.setMediaUuid(s);
        } else if (localName.equals("pic") || localName.equals("audio") || localName.equals("video")) {
            this.message.setMedia(this.content);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        this.builder.setLength(0);
        if (localName.equals("msg_content")) {
            this.message = new PublicMediaMessage();
        } else if (localName.equals("pic") || localName.equals("audio") || localName.equals("video")) {
            this.content = new PublicMediaContent();
        }
    }

    public PublicMediaMessage getMessage() {
        return this.message;
    }
}
