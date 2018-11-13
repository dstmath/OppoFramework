package com.android.internal.telephony.test;

import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import com.android.ims.ImsConferenceState;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TestConferenceEventPackageParser {
    private static final String LOG_TAG = "TestConferenceEventPackageParser";
    private static final String PARTICIPANT_TAG = "participant";
    private InputStream mInputStream;

    public TestConferenceEventPackageParser(InputStream inputStream) {
        this.mInputStream = inputStream;
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0044 A:{Splitter: B:1:0x0006, ExcHandler: java.io.IOException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0044, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:?, code:
            android.util.Log.e(LOG_TAG, "Failed to read test conference event package from XML file", r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            return conferenceState;
        } catch (Exception e) {
        } finally {
            try {
                this.mInputStream.close();
            } catch (IOException e2) {
                Log.e(LOG_TAG, "Failed to close test conference event package InputStream", e2);
                return null;
            }
        }
        return null;
    }

    private Bundle parseParticipant(XmlPullParser parser) throws IOException, XmlPullParserException {
        Bundle bundle = new Bundle();
        String user = SpnOverride.MVNO_TYPE_NONE;
        String displayText = SpnOverride.MVNO_TYPE_NONE;
        String endpoint = SpnOverride.MVNO_TYPE_NONE;
        String status = SpnOverride.MVNO_TYPE_NONE;
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals("user")) {
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
        bundle.putString("user", user);
        bundle.putString("display-text", displayText);
        bundle.putString("endpoint", endpoint);
        bundle.putString("status", status);
        return bundle;
    }
}
