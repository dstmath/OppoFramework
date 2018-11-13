package com.suntek.mway.rcs.client.api.parse;

import com.suntek.mway.rcs.client.aidl.constant.Constants.FavoriteMessageProvider.FavoriteMessage;
import com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile.CloudFileMessage;
import java.math.BigDecimal;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CloudFileMessageParser extends DefaultHandler {
    private StringBuilder builder = new StringBuilder();
    private CloudFileMessage message;

    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        this.builder.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        String s = this.builder.toString();
        if (localName.equals(FavoriteMessage.FILE_NAME)) {
            this.message.setFileName(s);
        } else if (localName.equals(FavoriteMessage.FILE_SIZE)) {
            long l;
            int index = s.indexOf("KB");
            if (index != -1) {
                l = Long.parseLong(s.substring(0, index));
            } else {
                try {
                    l = new BigDecimal(Double.parseDouble(s) / 1024.0d).setScale(0, 4).longValue();
                } catch (Exception e) {
                    l = 0;
                }
            }
            this.message.setFileSize(l);
        } else if (localName.equals("downloadurl")) {
            this.message.setShareUrl(s);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        this.builder.setLength(0);
        if (localName.equals("cloudfile")) {
            this.message = new CloudFileMessage();
        }
    }

    public CloudFileMessage getMessage() {
        return this.message;
    }
}
