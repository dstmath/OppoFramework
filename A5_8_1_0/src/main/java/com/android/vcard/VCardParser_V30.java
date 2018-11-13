package com.android.vcard;

import com.android.vcard.exception.VCardException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VCardParser_V30 extends VCardParser {
    static final Set<String> sAcceptableEncoding = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{VCardConstants.PARAM_ENCODING_7BIT, VCardConstants.PARAM_ENCODING_8BIT, VCardConstants.PARAM_ENCODING_BASE64, VCardConstants.PARAM_ENCODING_B})));
    static final Set<String> sKnownPropertyNameSet = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{VCardConstants.PROPERTY_BEGIN, VCardConstants.PROPERTY_END, VCardConstants.PROPERTY_LOGO, VCardConstants.PROPERTY_PHOTO, "LABEL", VCardConstants.PROPERTY_FN, VCardConstants.PROPERTY_TITLE, VCardConstants.PROPERTY_SOUND, VCardConstants.PROPERTY_VERSION, VCardConstants.PROPERTY_TEL, VCardConstants.PROPERTY_EMAIL, "TZ", "GEO", VCardConstants.PROPERTY_NOTE, VCardConstants.PROPERTY_URL, VCardConstants.PROPERTY_BDAY, VCardConstants.PROPERTY_ROLE, VCardConstants.PROPERTY_REV, "UID", "KEY", "MAILER", VCardConstants.PROPERTY_NAME, "PROFILE", "SOURCE", VCardConstants.PROPERTY_NICKNAME, "CLASS", VCardConstants.PROPERTY_SORT_STRING, VCardConstants.PROPERTY_CATEGORIES, VCardConstants.PROPERTY_PRODID, VCardConstants.PROPERTY_IMPP})));
    private final VCardParserImpl_V30 mVCardParserImpl;

    public VCardParser_V30() {
        this.mVCardParserImpl = new VCardParserImpl_V30();
    }

    public VCardParser_V30(int vcardType) {
        this.mVCardParserImpl = new VCardParserImpl_V30(vcardType);
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
