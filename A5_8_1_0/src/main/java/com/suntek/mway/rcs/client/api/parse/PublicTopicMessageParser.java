package com.suntek.mway.rcs.client.api.parse;

import com.suntek.mway.rcs.client.aidl.constant.Parameter;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicTopicMessage;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicTopicMessage.PublicTopicContent;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PublicTopicMessageParser extends DefaultHandler {
    private StringBuilder builder = new StringBuilder();
    private PublicTopicContent content;
    private List<PublicTopicContent> contents;
    private PublicTopicMessage message;

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
        } else if (localName.equals(Parameter.EXTRA_DISPLAY_TITLE)) {
            this.content.setTitle(s);
        } else if (localName.equals("author")) {
            this.content.setAuthor(s);
        } else if (localName.equals("thumb_link")) {
            this.content.setThumbLink(s);
        } else if (localName.equals("original_link")) {
            this.content.setOriginalLink(s);
        } else if (localName.equals("source_link")) {
            this.content.setSourceLink(s);
        } else if (localName.equals("media_uuid")) {
            this.content.setMediaUuid(s);
        } else if (localName.equals("main_text")) {
            this.content.setMainText(s);
        } else if (localName.equals("body_link")) {
            this.content.setBodyLink(s);
        } else if (localName.equals("mediaarticle")) {
            this.contents.add(this.content);
        } else if (localName.equals("article")) {
            this.message.setTopics(this.contents);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        this.builder.setLength(0);
        if (localName.equals("msg_content")) {
            this.message = new PublicTopicMessage();
        } else if (localName.equals("article")) {
            this.contents = new ArrayList();
        } else if (localName.equals("mediaarticle")) {
            this.content = new PublicTopicContent();
        }
    }

    public PublicTopicMessage getMessage() {
        return this.message;
    }
}
