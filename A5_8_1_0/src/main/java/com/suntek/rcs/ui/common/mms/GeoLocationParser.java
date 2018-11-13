package com.suntek.rcs.ui.common.mms;

import java.io.InputStream;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class GeoLocationParser extends DefaultHandler {
    private StringBuffer accumulator;
    private GeoLocation geoLocation = new GeoLocation();

    public GeoLocationParser(InputStream input) throws Exception {
        SAXParserFactory.newInstance().newSAXParser().parse(input, this);
    }

    public void characters(char[] buffer, int start, int length) {
        this.accumulator.append(buffer, start, length);
    }

    public void endDocument() throws SAXException {
        super.endDocument();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        String value = this.accumulator.toString();
        if (localName.equals("pos")) {
            String[] geoLocs = value.split(" ");
            double lat = 0.0d;
            double lng = 0.0d;
            if (geoLocs.length >= 2) {
                try {
                    lat = Double.parseDouble(geoLocs[0]);
                    lng = Double.parseDouble(geoLocs[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.geoLocation.setLng(lng);
            this.geoLocation.setLat(lat);
        }
    }

    public void startDocument() throws SAXException {
        super.startDocument();
        this.accumulator = new StringBuffer();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.accumulator.setLength(0);
        if (localName.equals("rcspushlocation")) {
            this.geoLocation.setLabel(attributes.getValue("label"));
        }
    }

    public void warning(SAXParseException exception) {
    }

    public void error(SAXParseException exception) {
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }

    public GeoLocation getGeoLocation() {
        return this.geoLocation;
    }
}
