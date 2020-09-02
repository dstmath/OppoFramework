package com.mediatek.common.jpe;

public class JpeException extends SecurityException {
    private String errorMessage = null;

    public JpeException(String message) {
        super(message, null);
        this.errorMessage = message;
    }

    public String getMessage() {
        StringBuffer value = new StringBuffer();
        if (this.errorMessage != null) {
            value.append("error - ");
            value.append(this.errorMessage);
            value.append("\n");
        } else {
            value.append(super.getMessage());
        }
        return value.toString();
    }
}
