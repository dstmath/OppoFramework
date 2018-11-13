package com.android.vcard;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.vcard.exception.VCardAgentNotSupportedException;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardInvalidCommentLineException;
import com.android.vcard.exception.VCardInvalidLineException;
import com.android.vcard.exception.VCardVersionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class VCardParserImpl_V21 {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String DEFAULT_ENCODING = "8BIT";
    private static final String LOG_TAG = "vCard";
    private static final int STATE_GROUP_OR_PROPERTY_NAME = 0;
    private static final int STATE_PARAMS = 1;
    private static final int STATE_PARAMS_IN_DQUOTE = 2;
    private boolean mCanceled;
    protected String mCurrentCharset;
    protected String mCurrentEncoding;
    protected final String mIntermediateCharset;
    private final List<VCardInterpreter> mInterpreterList;
    protected CustomBufferedReader mReader;
    protected final Set<String> mUnknownTypeSet;
    protected final Set<String> mUnknownValueSet;

    protected static final class CustomBufferedReader extends BufferedReader {
        private String mNextLine;
        private boolean mNextLineIsValid;
        private long mTime;

        public CustomBufferedReader(Reader in) {
            super(in);
        }

        public String readLine() throws IOException {
            if (this.mNextLineIsValid) {
                String ret = this.mNextLine;
                this.mNextLine = null;
                this.mNextLineIsValid = false;
                return ret;
            }
            long start = System.currentTimeMillis();
            String line = super.readLine();
            this.mTime += System.currentTimeMillis() - start;
            return line;
        }

        public String peekLine() throws IOException {
            if (!this.mNextLineIsValid) {
                long start = System.currentTimeMillis();
                String line = super.readLine();
                this.mTime += System.currentTimeMillis() - start;
                this.mNextLine = line;
                this.mNextLineIsValid = true;
            }
            return this.mNextLine;
        }

        public long getTotalmillisecond() {
            return this.mTime;
        }
    }

    public VCardParserImpl_V21() {
        this(VCardConfig.VCARD_TYPE_DEFAULT);
    }

    public VCardParserImpl_V21(int vcardType) {
        this.mInterpreterList = new ArrayList();
        this.mUnknownTypeSet = new HashSet();
        this.mUnknownValueSet = new HashSet();
        this.mIntermediateCharset = VCardConfig.DEFAULT_INTERMEDIATE_CHARSET;
    }

    protected boolean isValidPropertyName(String propertyName) {
        boolean z;
        if (getKnownPropertyNameSet().contains(propertyName.toUpperCase())) {
            z = true;
        } else {
            z = propertyName.startsWith("X-");
        }
        if (!(z || (this.mUnknownTypeSet.contains(propertyName) ^ 1) == 0)) {
            this.mUnknownTypeSet.add(propertyName);
            Log.w(LOG_TAG, "Property name unsupported by vCard 2.1: " + propertyName);
        }
        return true;
    }

    protected String getLine() throws IOException {
        return this.mReader.readLine();
    }

    protected String peekLine() throws IOException {
        return this.mReader.peekLine();
    }

    protected String getNonEmptyLine() throws IOException, VCardException {
        String line;
        do {
            line = getLine();
            if (line == null) {
                throw new VCardException("Reached end of buffer.");
            }
        } while (line.trim().length() <= 0);
        return line;
    }

    private boolean parseOneVCard() throws IOException, VCardException {
        this.mCurrentEncoding = "8BIT";
        this.mCurrentCharset = "UTF-8";
        if (!readBeginVCard(true)) {
            return false;
        }
        for (VCardInterpreter interpreter : this.mInterpreterList) {
            interpreter.onEntryStarted();
        }
        parseItems();
        for (VCardInterpreter interpreter2 : this.mInterpreterList) {
            interpreter2.onEntryEnded();
        }
        return true;
    }

    protected boolean readBeginVCard(boolean allowGarbage) throws IOException, VCardException {
        while (true) {
            String line = getLine();
            if (line == null) {
                return false;
            }
            if (line.trim().length() > 0) {
                String[] strArray = line.split(":", 2);
                if (strArray.length == 2 && strArray[0].trim().equalsIgnoreCase(VCardConstants.PROPERTY_BEGIN) && strArray[1].trim().equalsIgnoreCase("VCARD")) {
                    return true;
                }
                if (!allowGarbage) {
                    throw new VCardException("Expected String \"BEGIN:VCARD\" did not come (Instead, \"" + line + "\" came)");
                } else if (!allowGarbage) {
                    throw new VCardException("Reached where must not be reached.");
                }
            }
        }
    }

    protected void parseItems() throws IOException, VCardException {
        boolean ended = false;
        try {
            ended = parseItem();
        } catch (VCardInvalidCommentLineException e) {
            Log.e(LOG_TAG, "Invalid line which looks like some comment was found. Ignored.");
        }
        while (!ended) {
            try {
                ended = parseItem();
            } catch (VCardInvalidCommentLineException e2) {
                Log.e(LOG_TAG, "Invalid line which looks like some comment was found. Ignored.");
            }
        }
    }

    protected boolean parseItem() throws IOException, VCardException {
        this.mCurrentEncoding = "8BIT";
        VCardProperty propertyData = constructPropertyData(getNonEmptyLine());
        String propertyNameUpper = propertyData.getName().toUpperCase();
        String propertyRawValue = propertyData.getRawValue();
        if (propertyNameUpper.equals(VCardConstants.PROPERTY_BEGIN)) {
            if (propertyRawValue.equalsIgnoreCase("VCARD")) {
                handleNest();
            } else {
                throw new VCardException("Unknown BEGIN type: " + propertyRawValue);
            }
        } else if (!propertyNameUpper.equals(VCardConstants.PROPERTY_END)) {
            parseItemInter(propertyData, propertyNameUpper);
        } else if (propertyRawValue.equalsIgnoreCase("VCARD")) {
            return true;
        } else {
            throw new VCardException("Unknown END type: " + propertyRawValue);
        }
        return false;
    }

    private void parseItemInter(VCardProperty property, String propertyNameUpper) throws IOException, VCardException {
        String propertyRawValue = property.getRawValue();
        if (propertyNameUpper.equals(VCardConstants.PROPERTY_AGENT)) {
            handleAgent(property);
        } else if (!isValidPropertyName(propertyNameUpper)) {
            throw new VCardException("Unknown property name: \"" + propertyNameUpper + "\"");
        } else if (!propertyNameUpper.equals(VCardConstants.PROPERTY_VERSION) || (propertyRawValue.equals(getVersionString()) ^ 1) == 0) {
            handlePropertyValue(property, propertyNameUpper);
        } else {
            throw new VCardVersionException("Incompatible version: " + propertyRawValue + " != " + getVersionString());
        }
    }

    private void handleNest() throws IOException, VCardException {
        for (VCardInterpreter interpreter : this.mInterpreterList) {
            interpreter.onEntryStarted();
        }
        parseItems();
        for (VCardInterpreter interpreter2 : this.mInterpreterList) {
            interpreter2.onEntryEnded();
        }
    }

    protected VCardProperty constructPropertyData(String line) throws VCardException {
        VCardProperty propertyData = new VCardProperty();
        int length = line.length();
        if (length <= 0 || line.charAt(0) != '#') {
            int state = 0;
            int nameIndex = 0;
            int i = 0;
            while (i < length) {
                char ch = line.charAt(i);
                switch (state) {
                    case 0:
                        if (ch != ':') {
                            if (ch != '.') {
                                if (ch != ';') {
                                    break;
                                }
                                propertyData.setName(line.substring(nameIndex, i));
                                nameIndex = i + 1;
                                state = 1;
                                break;
                            }
                            String groupName = line.substring(nameIndex, i);
                            if (groupName.length() == 0) {
                                Log.w(LOG_TAG, "Empty group found. Ignoring.");
                            } else {
                                propertyData.addGroup(groupName);
                            }
                            nameIndex = i + 1;
                            break;
                        }
                        propertyData.setName(line.substring(nameIndex, i));
                        propertyData.setRawValue(i < length + -1 ? line.substring(i + 1) : "");
                        return propertyData;
                    case 1:
                        if (ch != '\"') {
                            if (ch != ';') {
                                if (ch != ':') {
                                    break;
                                }
                                handleParams(propertyData, line.substring(nameIndex, i));
                                propertyData.setRawValue(i < length + -1 ? line.substring(i + 1) : "");
                                return propertyData;
                            }
                            handleParams(propertyData, line.substring(nameIndex, i));
                            nameIndex = i + 1;
                            break;
                        }
                        if (VCardConstants.VERSION_V21.equalsIgnoreCase(getVersionString())) {
                            Log.w(LOG_TAG, "Double-quoted params found in vCard 2.1. Silently allow it");
                        }
                        state = 2;
                        break;
                    case 2:
                        if (ch == '\"') {
                            if (VCardConstants.VERSION_V21.equalsIgnoreCase(getVersionString())) {
                                Log.w(LOG_TAG, "Double-quoted params found in vCard 2.1. Silently allow it");
                            }
                            state = 1;
                            break;
                        }
                        break;
                    default:
                        break;
                }
                i++;
            }
            throw new VCardInvalidLineException("Invalid line: \"" + line + "\"");
        }
        throw new VCardInvalidCommentLineException();
    }

    protected void handleParams(VCardProperty propertyData, String params) throws VCardException {
        String[] strArray = params.split("=", 2);
        if (strArray.length == 2) {
            String paramName = strArray[0].trim().toUpperCase();
            String paramValue = strArray[1].trim();
            if (paramName.equals("TYPE")) {
                handleType(propertyData, paramValue);
                return;
            } else if (paramName.equals(VCardConstants.PARAM_VALUE)) {
                handleValue(propertyData, paramValue);
                return;
            } else if (paramName.equals(VCardConstants.PARAM_ENCODING)) {
                handleEncoding(propertyData, paramValue.toUpperCase());
                return;
            } else if (paramName.equals(VCardConstants.PARAM_CHARSET)) {
                handleCharset(propertyData, paramValue);
                return;
            } else if (paramName.equals(VCardConstants.PARAM_LANGUAGE)) {
                handleLanguage(propertyData, paramValue);
                return;
            } else if (paramName.startsWith("X-")) {
                handleAnyParam(propertyData, paramName, paramValue);
                return;
            } else {
                throw new VCardException("Unknown type \"" + paramName + "\"");
            }
        }
        handleParamWithoutName(propertyData, strArray[0]);
    }

    protected void handleParamWithoutName(VCardProperty propertyData, String paramValue) {
        handleType(propertyData, paramValue);
    }

    protected void handleType(VCardProperty propertyData, String ptypeval) {
        boolean z;
        if (getKnownTypeSet().contains(ptypeval.toUpperCase())) {
            z = true;
        } else {
            z = ptypeval.startsWith("X-");
        }
        if (!(z || (this.mUnknownTypeSet.contains(ptypeval) ^ 1) == 0)) {
            this.mUnknownTypeSet.add(ptypeval);
            Log.w(LOG_TAG, String.format("TYPE unsupported by %s: ", new Object[]{Integer.valueOf(getVersion()), ptypeval}));
        }
        propertyData.addParameter("TYPE", ptypeval);
    }

    protected void handleValue(VCardProperty propertyData, String pvalueval) {
        boolean z;
        if (getKnownValueSet().contains(pvalueval.toUpperCase()) || pvalueval.startsWith("X-")) {
            z = true;
        } else {
            z = this.mUnknownValueSet.contains(pvalueval);
        }
        if (!z) {
            this.mUnknownValueSet.add(pvalueval);
            Log.w(LOG_TAG, String.format("The value unsupported by TYPE of %s: ", new Object[]{Integer.valueOf(getVersion()), pvalueval}));
        }
        propertyData.addParameter(VCardConstants.PARAM_VALUE, pvalueval);
    }

    protected void handleEncoding(VCardProperty propertyData, String pencodingval) throws VCardException {
        if (getAvailableEncodingSet().contains(pencodingval) || pencodingval.startsWith("X-")) {
            propertyData.addParameter(VCardConstants.PARAM_ENCODING, pencodingval);
            this.mCurrentEncoding = pencodingval.toUpperCase();
            return;
        }
        throw new VCardException("Unknown encoding \"" + pencodingval + "\"");
    }

    protected void handleCharset(VCardProperty propertyData, String charsetval) {
        this.mCurrentCharset = charsetval;
        propertyData.addParameter(VCardConstants.PARAM_CHARSET, charsetval);
    }

    protected void handleLanguage(VCardProperty propertyData, String langval) throws VCardException {
        String[] strArray = langval.split("-");
        if (strArray.length != 2) {
            throw new VCardException("Invalid Language: \"" + langval + "\"");
        }
        String tmp = strArray[0];
        int length = tmp.length();
        int i = 0;
        while (i < length) {
            if (isAsciiLetter(tmp.charAt(i))) {
                i++;
            } else {
                throw new VCardException("Invalid Language: \"" + langval + "\"");
            }
        }
        tmp = strArray[1];
        length = tmp.length();
        i = 0;
        while (i < length) {
            if (isAsciiLetter(tmp.charAt(i))) {
                i++;
            } else {
                throw new VCardException("Invalid Language: \"" + langval + "\"");
            }
        }
        propertyData.addParameter(VCardConstants.PARAM_LANGUAGE, langval);
    }

    private boolean isAsciiLetter(char ch) {
        if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
            return false;
        }
        return true;
    }

    protected void handleAnyParam(VCardProperty propertyData, String paramName, String paramValue) {
        propertyData.addParameter(paramName, paramValue);
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:0x0212  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void handlePropertyValue(VCardProperty property, String propertyName) throws IOException, VCardException {
        String propertyNameUpper = property.getName().toUpperCase();
        String propertyRawValue = property.getRawValue();
        String sourceCharset = VCardConfig.DEFAULT_INTERMEDIATE_CHARSET;
        Collection<String> charsetCollection = property.getParameters(VCardConstants.PARAM_CHARSET);
        CharSequence targetCharset = charsetCollection != null ? (String) charsetCollection.iterator().next() : null;
        if (TextUtils.isEmpty(targetCharset)) {
            targetCharset = "UTF-8";
        }
        if (propertyNameUpper.equals(VCardConstants.PROPERTY_ADR) || propertyNameUpper.equals(VCardConstants.PROPERTY_ORG) || propertyNameUpper.equals(VCardConstants.PROPERTY_N)) {
            handleAdrOrgN(property, propertyRawValue, VCardConfig.DEFAULT_INTERMEDIATE_CHARSET, targetCharset);
            return;
        }
        if (this.mCurrentEncoding.equals(VCardConstants.PARAM_ENCODING_QP) || (propertyNameUpper.equals(VCardConstants.PROPERTY_FN) && property.getParameters(VCardConstants.PARAM_ENCODING) == null && VCardUtils.appearsLikeAndroidVCardQuotedPrintable(propertyRawValue))) {
            String quotedPrintablePart = getQuotedPrintablePart(propertyRawValue);
            String propertyEncodedValue = VCardUtils.parseQuotedPrintable(quotedPrintablePart, false, VCardConfig.DEFAULT_INTERMEDIATE_CHARSET, targetCharset);
            property.setRawValue(quotedPrintablePart);
            property.setValues(propertyEncodedValue);
            for (VCardInterpreter interpreter : this.mInterpreterList) {
                interpreter.onPropertyCreated(property);
            }
        } else if (this.mCurrentEncoding.equals(VCardConstants.PARAM_ENCODING_BASE64) || this.mCurrentEncoding.equals(VCardConstants.PARAM_ENCODING_B)) {
            try {
                property.setByteValue(Base64.decode(getBase64(propertyRawValue), 0));
                for (VCardInterpreter interpreter2 : this.mInterpreterList) {
                    interpreter2.onPropertyCreated(property);
                }
            } catch (IllegalArgumentException e) {
                throw new VCardException("Decode error on base64 photo: " + propertyRawValue);
            } catch (OutOfMemoryError e2) {
                Log.e(LOG_TAG, "OutOfMemoryError happened during parsing BASE64 data!");
                for (VCardInterpreter interpreter22 : this.mInterpreterList) {
                    interpreter22.onPropertyCreated(property);
                }
            }
        } else {
            boolean z;
            if (this.mCurrentEncoding.equals(VCardConstants.PARAM_ENCODING_7BIT) || this.mCurrentEncoding.equals("8BIT")) {
                z = true;
            } else {
                z = this.mCurrentEncoding.startsWith("X-");
            }
            if (!z) {
                Log.w(LOG_TAG, String.format("The encoding \"%s\" is unsupported by vCard %s", new Object[]{this.mCurrentEncoding, getVersionString()}));
            }
            if (getVersion() == 0) {
                StringBuilder builder = null;
                while (true) {
                    String nextLine = peekLine();
                    if (!TextUtils.isEmpty(nextLine) && nextLine.charAt(0) == ' ' && ("END:VCARD".contains(nextLine.toUpperCase()) ^ 1) != 0) {
                        getLine();
                        if (builder == null) {
                            builder = new StringBuilder();
                            builder.append(propertyRawValue);
                        }
                        builder.append(nextLine.substring(1));
                    } else if (builder != null) {
                        propertyRawValue = builder.toString();
                    }
                }
                if (builder != null) {
                }
            }
            List propertyValueList = new ArrayList();
            propertyValueList.add(maybeUnescapeText(VCardUtils.convertStringCharset(propertyRawValue, VCardConfig.DEFAULT_INTERMEDIATE_CHARSET, targetCharset)));
            property.setValues(propertyValueList);
            for (VCardInterpreter interpreter222 : this.mInterpreterList) {
                interpreter222.onPropertyCreated(property);
            }
        }
    }

    private void handleAdrOrgN(VCardProperty property, String propertyRawValue, String sourceCharset, String targetCharset) throws VCardException, IOException {
        List encodedValueList = new ArrayList();
        if (this.mCurrentEncoding.equals(VCardConstants.PARAM_ENCODING_QP)) {
            String quotedPrintablePart = getQuotedPrintablePart(propertyRawValue);
            property.setRawValue(quotedPrintablePart);
            for (String quotedPrintableValue : VCardUtils.constructListFromValue(quotedPrintablePart, getVersion())) {
                encodedValueList.add(VCardUtils.parseQuotedPrintable(quotedPrintableValue, false, sourceCharset, targetCharset));
            }
        } else {
            for (String value : VCardUtils.constructListFromValue(VCardUtils.convertStringCharset(getPotentialMultiline(propertyRawValue), sourceCharset, targetCharset), getVersion())) {
                encodedValueList.add(value);
            }
        }
        property.setValues(encodedValueList);
        for (VCardInterpreter interpreter : this.mInterpreterList) {
            interpreter.onPropertyCreated(property);
        }
    }

    private String getQuotedPrintablePart(String firstString) throws IOException, VCardException {
        if (!firstString.trim().endsWith("=")) {
            return firstString;
        }
        int pos = firstString.length() - 1;
        do {
        } while (firstString.charAt(pos) != '=');
        StringBuilder builder = new StringBuilder();
        builder.append(firstString.substring(0, pos + 1));
        builder.append(VCardBuilder.VCARD_END_OF_LINE);
        while (true) {
            String line = getLine();
            if (line == null) {
                throw new VCardException("File ended during parsing a Quoted-Printable String");
            } else if (line.trim().endsWith("=")) {
                pos = line.length() - 1;
                do {
                } while (line.charAt(pos) != '=');
                builder.append(line.substring(0, pos + 1));
                builder.append(VCardBuilder.VCARD_END_OF_LINE);
            } else {
                builder.append(line);
                return builder.toString();
            }
        }
    }

    private String getPotentialMultiline(String firstString) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(firstString);
        while (true) {
            String line = peekLine();
            if (line != null && line.length() != 0 && getPropertyNameUpperCase(line) == null) {
                getLine();
                builder.append(" ").append(line);
            }
        }
        return builder.toString();
    }

    protected String getBase64(String firstString) throws IOException, VCardException {
        String line;
        StringBuilder builder = new StringBuilder();
        builder.append(firstString);
        while (true) {
            line = peekLine();
            if (line != null) {
                String propertyName = getPropertyNameUpperCase(line);
                if (!getKnownPropertyNameSet().contains(propertyName) && !VCardConstants.PROPERTY_X_ANDROID_CUSTOM.equals(propertyName)) {
                    getLine();
                    if (line.length() == 0) {
                        break;
                    }
                    builder.append(line.trim());
                } else {
                    Log.w(LOG_TAG, "Found a next property during parsing a BASE64 string, which must not contain semi-colon or colon. Treat the line as next property.");
                    Log.w(LOG_TAG, "Problematic line: " + line.trim());
                }
            } else {
                throw new VCardException("File ended during parsing BASE64 binary");
            }
        }
        Log.w(LOG_TAG, "Found a next property during parsing a BASE64 string, which must not contain semi-colon or colon. Treat the line as next property.");
        Log.w(LOG_TAG, "Problematic line: " + line.trim());
        return builder.toString();
    }

    private String getPropertyNameUpperCase(String line) {
        int colonIndex = line.indexOf(":");
        if (colonIndex <= -1) {
            return null;
        }
        int minIndex;
        int semiColonIndex = line.indexOf(";");
        if (colonIndex == -1) {
            minIndex = semiColonIndex;
        } else if (semiColonIndex == -1) {
            minIndex = colonIndex;
        } else {
            minIndex = Math.min(colonIndex, semiColonIndex);
        }
        return line.substring(0, minIndex).toUpperCase();
    }

    protected void handleAgent(VCardProperty property) throws VCardException {
        if (property.getRawValue().toUpperCase().contains("BEGIN:VCARD")) {
            throw new VCardAgentNotSupportedException("AGENT Property is not supported now.");
        }
        for (VCardInterpreter interpreter : this.mInterpreterList) {
            interpreter.onPropertyCreated(property);
        }
    }

    protected String maybeUnescapeText(String text) {
        return text;
    }

    protected String maybeUnescapeCharacter(char ch) {
        return unescapeCharacter(ch);
    }

    static String unescapeCharacter(char ch) {
        if (ch == '\\' || ch == ';' || ch == ':' || ch == ',') {
            return String.valueOf(ch);
        }
        return null;
    }

    protected int getVersion() {
        return 0;
    }

    protected String getVersionString() {
        return VCardConstants.VERSION_V21;
    }

    protected Set<String> getKnownPropertyNameSet() {
        return VCardParser_V21.sKnownPropertyNameSet;
    }

    protected Set<String> getKnownTypeSet() {
        return VCardParser_V21.sKnownTypeSet;
    }

    protected Set<String> getKnownValueSet() {
        return VCardParser_V21.sKnownValueSet;
    }

    protected Set<String> getAvailableEncodingSet() {
        return VCardParser_V21.sAvailableEncoding;
    }

    protected String getDefaultEncoding() {
        return "8BIT";
    }

    protected String getDefaultCharset() {
        return "UTF-8";
    }

    protected String getCurrentCharset() {
        return this.mCurrentCharset;
    }

    public void addInterpreter(VCardInterpreter interpreter) {
        this.mInterpreterList.add(interpreter);
    }

    public void parse(InputStream is) throws IOException, VCardException {
        if (is == null) {
            throw new NullPointerException("InputStream must not be null.");
        }
        this.mReader = new CustomBufferedReader(new InputStreamReader(is, this.mIntermediateCharset));
        long start = System.currentTimeMillis();
        for (VCardInterpreter interpreter : this.mInterpreterList) {
            interpreter.onVCardStarted();
        }
        do {
            synchronized (this) {
                if (this.mCanceled) {
                    Log.i(LOG_TAG, "Cancel request has come. exitting parse operation.");
                    break;
                }
            }
        } while (parseOneVCard());
        for (VCardInterpreter interpreter2 : this.mInterpreterList) {
            interpreter2.onVCardEnded();
        }
    }

    public void parseOne(InputStream is) throws IOException, VCardException {
        if (is == null) {
            throw new NullPointerException("InputStream must not be null.");
        }
        this.mReader = new CustomBufferedReader(new InputStreamReader(is, this.mIntermediateCharset));
        long start = System.currentTimeMillis();
        for (VCardInterpreter interpreter : this.mInterpreterList) {
            interpreter.onVCardStarted();
        }
        parseOneVCard();
        for (VCardInterpreter interpreter2 : this.mInterpreterList) {
            interpreter2.onVCardEnded();
        }
    }

    public final synchronized void cancel() {
        Log.i(LOG_TAG, "ParserImpl received cancel operation.");
        this.mCanceled = true;
    }
}
