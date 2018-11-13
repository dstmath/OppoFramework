package com.mediatek.common.sms;

public interface IDupSmsFilterExt {
    boolean containDupSms(byte[] bArr);

    void setPhoneId(int i);
}
