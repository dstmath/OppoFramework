package com.alibaba.fastjson;

public class JSONPathException extends JSONException {
    public JSONPathException(String message) {
        super(message);
    }

    public JSONPathException(String message, Throwable cause) {
        super(message, cause);
    }
}
