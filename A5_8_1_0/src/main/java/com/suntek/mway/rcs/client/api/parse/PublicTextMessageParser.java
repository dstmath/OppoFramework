package com.suntek.mway.rcs.client.api.parse;

import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicTextMessage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PublicTextMessageParser extends DefaultHandler {
    private StringBuilder builder = new StringBuilder();
    private PublicTextMessage message;

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
        } else if (localName.equals("text")) {
            this.message.setContent(s);
        } else if (localName.equals("activeStatus")) {
            this.message.setActiveStatus(Integer.parseInt(s));
        } else if (localName.equals("pa_uuid")) {
            this.message.setPaUuid(s);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        this.builder.setLength(0);
        if (localName.equals("msg_content")) {
            this.message = new PublicTextMessage();
        }
    }

    public PublicTextMessage getMessage() {
        return this.message;
    }
}
