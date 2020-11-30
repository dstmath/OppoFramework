package com.alibaba.fastjson;

public class JSONException extends RuntimeException {
    public JSONException() {
    }

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
