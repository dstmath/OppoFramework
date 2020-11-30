package com.mediatek.internal.telephony.test;

import android.os.Bundle;
import android.telephony.ims.ImsConferenceState;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MtkTestConferenceEventPackageParser {
    private static final String LOG_TAG = "MtkTestConferenceEventPackageParser";
    private static final String PARTICIPANT_TAG = "participant";
    private InputStream mInputStream;

    public MtkTestConferenceEventPackageParser(InputStream inputStream) {
        this.mInputStream = inputStream;
    }

    public ImsConferenceState parse() {
        ImsConferenceState conferenceState = new ImsConferenceState();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(this.mInputStream, null);
            parser.nextTag();
            int outerDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                if (parser.getName().equals(PARTICIPANT_TAG)) {
                    Log.v(LOG_TAG, "Found participant.");
                    Bundle participant = parseParticipant(parser);
                    conferenceState.mParticipants.put(participant.getString("endpoint"), participant);
                }
            }
            try {
                this.mInputStream.close();
                return conferenceState;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to close test conference event package InputStream", e);
                return null;
            }
        } catch (IOException | XmlPullParserException e2) {
            Log.e(LOG_TAG, "Failed to read test conference event package from XML file", e2);
            try {
                this.mInputStream.close();
                return null;
            } catch (IOException e3) {
                Log.e(LOG_TAG, "Failed to close test conference event package InputStream", e3);
                return null;
            }
        } catch (Throwable th) {
            try {
                this.mInputStream.close();
                throw th;
            } catch (IOException e4) {
                Log.e(LOG_TAG, "Failed to close test conference event package InputStream", e4);
                return null;
            }
        }
    }

    private Bundle parseParticipant(XmlPullParser parser) throws IOException, XmlPullParserException {
        Bundle bundle = new Bundle();
        String user = "";
        String displayText = "";
        String endpoint = "";
        String status = "";
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER)) {
                parser.next();
                user = parser.getText();
            } else if (parser.getName().equals("display-text")) {
                parser.next();
                displayText = parser.getText();
            } else if (parser.getName().equals("endpoint")) {
                parser.next();
                endpoint = parser.getText();
            } else if (parser.getName().equals("status")) {
                parser.next();
                status = parser.getText();
            }
        }
        Log.v(LOG_TAG, "User: " + user);
        Log.v(LOG_TAG, "DisplayText: " + displayText);
        Log.v(LOG_TAG, "Endpoint: " + endpoint);
        Log.v(LOG_TAG, "Status: " + status);
        bundle.putString(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER, user);
        bundle.putString("display-text", displayText);
        bundle.putString("endpoint", endpoint);
        bundle.putString("status", status);
        return bundle;
    }
}
