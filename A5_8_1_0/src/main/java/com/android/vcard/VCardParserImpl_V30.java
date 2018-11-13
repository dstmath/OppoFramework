package com.android.vcard;

import android.util.Log;
import com.android.vcard.exception.VCardException;
import java.io.IOException;
import java.util.Set;

class VCardParserImpl_V30 extends VCardParserImpl_V21 {
    private static final String LOG_TAG = "vCard";
    private boolean mEmittedAgentWarning = false;
    private String mPreviousLine;

    public VCardParserImpl_V30(int vcardType) {
        super(vcardType);
    }

    protected int getVersion() {
        return 1;
    }

    protected String getVersionString() {
        return VCardConstants.VERSION_V30;
    }

    protected String peekLine() throws IOException {
        if (this.mPreviousLine != null) {
            return this.mPreviousLine;
        }
        return this.mReader.peekLine();
    }

    protected String getLine() throws IOException {
        if (this.mPreviousLine == null) {
            return this.mReader.readLine();
        }
        String ret = this.mPreviousLine;
        this.mPreviousLine = null;
        return ret;
    }

    protected String getNonEmptyLine() throws IOException, VCardException {
        String line;
        StringBuilder stringBuilder = null;
        while (true) {
            line = this.mReader.readLine();
            if (line != null) {
                if (line.length() != 0) {
                    if (line.charAt(0) != ' ' && line.charAt(0) != 9) {
                        if (stringBuilder != null || this.mPreviousLine != null) {
                            break;
                        }
                        this.mPreviousLine = line;
                    } else {
                        if (stringBuilder == null) {
                            stringBuilder = new StringBuilder();
                        }
                        if (this.mPreviousLine != null) {
                            stringBuilder.append(this.mPreviousLine);
                            this.mPreviousLine = null;
                        }
                        stringBuilder.append(line.substring(1));
                    }
                }
            } else {
                break;
            }
        }
        String ret = null;
        if (stringBuilder != null) {
            ret = stringBuilder.toString();
        } else if (this.mPreviousLine != null) {
            ret = this.mPreviousLine;
        }
        this.mPreviousLine = line;
        if (ret != null) {
            return ret;
        }
        throw new VCardException("Reached end of buffer.");
    }

    protected boolean readBeginVCard(boolean allowGarbage) throws IOException, VCardException {
        return super.readBeginVCard(allowGarbage);
    }

    protected void handleParams(VCardProperty propertyData, String params) throws VCardException {
        try {
            super.handleParams(propertyData, params);
        } catch (VCardException e) {
            String[] strArray = params.split("=", 2);
            if (strArray.length == 2) {
                handleAnyParam(propertyData, strArray[0], strArray[1]);
                return;
            }
            throw new VCardException("Unknown params value: " + params);
        }
    }

    protected void handleAnyParam(VCardProperty propertyData, String paramName, String paramValue) {
        splitAndPutParam(propertyData, paramName, paramValue);
    }

    protected void handleParamWithoutName(VCardProperty property, String paramValue) {
        handleType(property, paramValue);
    }

    protected void handleType(VCardProperty property, String paramValue) {
        splitAndPutParam(property, "TYPE", paramValue);
    }

    private void splitAndPutParam(VCardProperty property, String paramName, String paramValue) {
        StringBuilder builder = null;
        boolean insideDquote = false;
        int length = paramValue.length();
        for (int i = 0; i < length; i++) {
            char ch = paramValue.charAt(i);
            if (ch == '\"') {
                if (insideDquote) {
                    property.addParameter(paramName, encodeParamValue(builder.toString()));
                    builder = null;
                    insideDquote = false;
                } else {
                    if (builder != null) {
                        if (builder.length() > 0) {
                            Log.w(LOG_TAG, "Unexpected Dquote inside property.");
                        } else {
                            property.addParameter(paramName, encodeParamValue(builder.toString()));
                        }
                    }
                    insideDquote = true;
                }
            } else if (ch != ',' || (insideDquote ^ 1) == 0) {
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(ch);
            } else if (builder == null) {
                Log.w(LOG_TAG, "Comma is used before actual string comes. (" + paramValue + ")");
            } else {
                property.addParameter(paramName, encodeParamValue(builder.toString()));
                builder = null;
            }
        }
        if (insideDquote) {
            Log.d(LOG_TAG, "Dangling Dquote.");
        }
        if (builder == null) {
            return;
        }
        if (builder.length() == 0) {
            Log.w(LOG_TAG, "Unintended behavior. We must not see empty StringBuilder at the end of parameter value parsing.");
        } else {
            property.addParameter(paramName, encodeParamValue(builder.toString()));
        }
    }

    protected String encodeParamValue(String paramValue) {
        return VCardUtils.convertStringCharset(paramValue, VCardConfig.DEFAULT_INTERMEDIATE_CHARSET, "UTF-8");
    }

    protected void handleAgent(VCardProperty property) {
        if (!this.mEmittedAgentWarning) {
            Log.w(LOG_TAG, "AGENT in vCard 3.0 is not supported yet. Ignore it");
            this.mEmittedAgentWarning = true;
        }
    }

    protected String getBase64(String firstString) throws IOException, VCardException {
        return firstString;
    }

    protected String maybeUnescapeText(String text) {
        return unescapeText(text);
    }

    public static String unescapeText(String text) {
        StringBuilder builder = new StringBuilder();
        int length = text.length();
        int i = 0;
        while (i < length) {
            char ch = text.charAt(i);
            if (ch != '\\' || i >= length - 1) {
                builder.append(ch);
            } else {
                i++;
                char next_ch = text.charAt(i);
                if (next_ch == 'n' || next_ch == 'N') {
                    builder.append("\n");
                } else {
                    builder.append(next_ch);
                }
            }
            i++;
        }
        return builder.toString();
    }

    protected String maybeUnescapeCharacter(char ch) {
        return unescapeCharacter(ch);
    }

    public static String unescapeCharacter(char ch) {
        if (ch == 'n' || ch == 'N') {
            return "\n";
        }
        return String.valueOf(ch);
    }

    protected Set<String> getKnownPropertyNameSet() {
        return VCardParser_V30.sKnownPropertyNameSet;
    }
}
