package com.suntek.mway.rcs.client.api.util;

import java.io.StringReader;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XmlUtil {
    public static DefaultHandler parse(String text, DefaultHandler handler) {
        try {
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            xr.setContentHandler(handler);
            xr.parse(new InputSource(new StringReader(text)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }
}
