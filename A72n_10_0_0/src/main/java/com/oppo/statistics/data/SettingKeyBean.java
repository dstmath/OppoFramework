package com.oppo.statistics.data;

public class SettingKeyBean {
    public static final String DEFAULE_VALUE = "default_value";
    public static final String HTTP_POST_KEY = "http_post_key";
    public static final String METHOD_NAME = "method_name";
    public static final String SETTING_KEY = "setting_key";
    private String mDefaultValue;
    private String mHttpPostKey;
    private String mMethodName;
    private String mSettingKey;

    public SettingKeyBean() {
    }

    public SettingKeyBean(String settingKey, String methodName) {
        this.mSettingKey = settingKey;
        this.mMethodName = methodName;
    }

    public SettingKeyBean(String settingKey, String methodName, String httpPostKey) {
        this.mSettingKey = settingKey;
        this.mMethodName = methodName;
        this.mHttpPostKey = httpPostKey;
    }

    public String getSettingKey() {
        return this.mSettingKey;
    }

    public void setSettingKey(String settingKey) {
        this.mSettingKey = settingKey;
    }

    public String getHttpPostKey() {
        return this.mHttpPostKey;
    }

    public void setHttpPostKey(String httpPostKey) {
        this.mHttpPostKey = httpPostKey;
    }

    public String getMethodName() {
        return this.mMethodName;
    }

    public void setMethodName(String methodName) {
        this.mMethodName = methodName;
    }

    public String getDefaultValue() {
        return this.mDefaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.mDefaultValue = defaultValue;
    }
}
