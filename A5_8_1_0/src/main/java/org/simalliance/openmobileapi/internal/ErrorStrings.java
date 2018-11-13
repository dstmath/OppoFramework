package org.simalliance.openmobileapi.internal;

public final class ErrorStrings {
    public static final String APDU_BAD_RESPONSE = "Response APDU has a bad coding.";
    public static final String APDU_WRONG_FORMAT = "APDU has an invalid format.";
    public static final String AUTH_METHOD_BLOCKED = "PIN is blocked.";
    public static final String CHANNEL_CLOSED = "Channel is closed.";
    public static final String FILE_NOT_FOUND = "File not found.";
    public static final String INVALID_CLA = "Invalid class";
    public static final String INVALID_FID = "Invalid File ID. File ID must be between ";
    public static final String INVALID_SFI = "Invalid Short File ID. SFI must be between ";
    public static final String MEMORY_FAILURE = "Memory failure.";
    public static final String NOT_ENOUGH_MEMORY = "Not enough memory space in the file.";
    public static final String NO_CURRENT_FILE = "No file is currently selected.";
    public static final String NO_RECORD_BASED_FILE = "The selected file is not record-based.";
    public static final String NO_TRANSPARENT_FILE = "The selected file is not transparent.";
    public static final String OFFSET_OUTSIDE_EF = "Offset outside EF.";
    public static final String OPERATION_NOT_SUPORTED = "This operation is not supported by the selected Applet.";
    public static final String PIN_BLOCKED = "Referenced PIN is blocked.";
    public static final String PIN_REF_NOT_FOUND = "Referenced PIN could not be found.";
    public static final String PIN_WRONG = "Wrong PIN.";
    public static final String PKCS15_NO_FS = "No PKCS#15 file structure found.";
    public static final String RECORD_NOT_FOUND = "Record not found.";
    public static final String REF_NOT_FOUND = "Referenced data not found.";
    public static final String SECURITY_STATUS_NOT_SATISFIED = "Security status not satisfied.";
    public static final String SES_APP_NOT_PRESENT = "Channel is not connected to a Secure Storage applet.";
    public static final String SES_CHANNEL_CLOSE = "The channel is close";
    public static final String SES_CREATE_FAILED_MEMORY = "File creation failed due to memory issues.";
    public static final String SES_EMPTY_TITLE = "The title is empty";
    public static final String SES_ENTRY_NOT_EXISTS = "Referenced data not found (if no SeS entry not exists";
    public static final String SES_INCORRECT_TITLE = "The title is incorrect: bad encoding or wrong length.";
    public static final String SES_IOERROR_READ = "The entry could not be read because an incomplete read procedure.";
    public static final String SES_IOERROR_WRITE = "The entry could'b be read because an incomplete write procedure.";
    public static final String SES_LONG_TITLE = "The title is too long (max value 60 chars).";
    public static final String SES_NOT_ENOUGH_MEMORY = "Not enough memory space";
    public static final String SES_NO_ENTRY_SELECTED = "Referenced data not found (if no SeS entry is currently selected";
    public static final String SES_SECURITY_EXCEPTION = "The PIN to access the Secure Storage Applet has not been verified";
    public static final String SES_TITLE_EXISTS = "The specified title already exists.";
    public static final String SES_TITLE_NOT_EXISTS = "The written title does not exist.";
    public static final String TLV_INVALID_LENGTH = "Invalid length field.";
    public static final String TLV_INVALID_TAG = "Invalid tag.";
    public static final String TLV_TAG_NOT_FOUND = "Tag not found.";
    public static final String TLV_TAG_UNEXPECTED = "Unexpected tag.";
    public static final String WRONG_LENGTH = "Wrong length.";

    private ErrorStrings() {
    }

    public static String paramNull(String parameterName) {
        return "Parameter " + parameterName + " must not be null.";
    }

    public static String paramInvalidArrayLength(String parameterName) {
        return "Parameter " + parameterName + " has an invalid length.";
    }

    public static String paramInvalidValue(String parameterName) {
        return "Parameter " + parameterName + " has an invalid value.";
    }

    public static String unexpectedStatusWord(int swValue) {
        return "Unexpected Status Word: 0x" + String.format("%02x", new Object[]{Integer.valueOf(swValue)}) + ".";
    }
}
