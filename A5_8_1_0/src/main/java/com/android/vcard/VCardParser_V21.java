package com.android.vcard;

import com.android.vcard.exception.VCardException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class VCardParser_V21 extends VCardParser {
    static final Set<String> sAvailableEncoding = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{VCardConstants.PARAM_ENCODING_7BIT, VCardConstants.PARAM_ENCODING_8BIT, VCardConstants.PARAM_ENCODING_QP, VCardConstants.PARAM_ENCODING_BASE64, VCardConstants.PARAM_ENCODING_B})));
    static final Set<String> sKnownPropertyNameSet = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{VCardConstants.PROPERTY_BEGIN, VCardConstants.PROPERTY_END, VCardConstants.PROPERTY_LOGO, VCardConstants.PROPERTY_PHOTO, "LABEL", VCardConstants.PROPERTY_FN, VCardConstants.PROPERTY_TITLE, VCardConstants.PROPERTY_SOUND, VCardConstants.PROPERTY_VERSION, VCardConstants.PROPERTY_TEL, VCardConstants.PROPERTY_EMAIL, "TZ", "GEO", VCardConstants.PROPERTY_NOTE, VCardConstants.PROPERTY_URL, VCardConstants.PROPERTY_BDAY, VCardConstants.PROPERTY_ROLE, VCardConstants.PROPERTY_REV, "UID", "KEY", "MAILER"})));
    static final Set<String> sKnownTypeSet = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{VCardConstants.PARAM_ADR_TYPE_DOM, VCardConstants.PARAM_ADR_TYPE_INTL, "POSTAL", VCardConstants.PARAM_ADR_TYPE_PARCEL, VCardConstants.PARAM_TYPE_HOME, VCardConstants.PARAM_TYPE_WORK, VCardConstants.PARAM_TYPE_PREF, VCardConstants.PARAM_TYPE_VOICE, VCardConstants.PARAM_TYPE_FAX, VCardConstants.PARAM_TYPE_MSG, VCardConstants.PARAM_TYPE_CELL, VCardConstants.PARAM_TYPE_PAGER, VCardConstants.PARAM_TYPE_BBS, VCardConstants.PARAM_TYPE_MODEM, VCardConstants.PARAM_TYPE_CAR, VCardConstants.PARAM_TYPE_ISDN, VCardConstants.PARAM_TYPE_VIDEO, "AOL", "APPLELINK", "ATTMAIL", "CIS", "EWORLD", VCardConstants.PARAM_TYPE_INTERNET, "IBMMAIL", "MCIMAIL", "POWERSHARE", "PRODIGY", VCardConstants.PARAM_TYPE_TLX, "X400", "GIF", "CGM", "WMF", "BMP", "MET", "PMB", "DIB", "PICT", "TIFF", "PDF", "PS", "JPEG", "QTIME", "MPEG", "MPEG2", "AVI", "WAVE", "AIFF", "PCM", "X509", "PGP"})));
    static final Set<String> sKnownValueSet = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{"INLINE", VCardConstants.PROPERTY_URL, "CONTENT-ID", "CID"})));
    private final VCardParserImpl_V21 mVCardParserImpl;

    public VCardParser_V21() {
        this.mVCardParserImpl = new VCardParserImpl_V21();
    }

    public VCardParser_V21(int vcardType) {
        this.mVCardParserImpl = new VCardParserImpl_V21(vcardType);
    }

    public void addInterpreter(VCardInterpreter interpreter) {
        this.mVCardParserImpl.addInterpreter(interpreter);
    }

    public void parse(InputStream is) throws IOException, VCardException {
        this.mVCardParserImpl.parse(is);
    }

    public void parseOne(InputStream is) throws IOException, VCardException {
        this.mVCardParserImpl.parseOne(is);
    }

    public void cancel() {
        this.mVCardParserImpl.cancel();
    }
}
