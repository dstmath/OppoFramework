package com.color.inner.net;

import android.net.WebAddress;

public class WebAddressWrapper {
    private WebAddress mWebAddress;

    public WebAddressWrapper(String address) {
        this.mWebAddress = new WebAddress(address);
    }

    public String toString() {
        return this.mWebAddress.toString();
    }

    public String getScheme() {
        return this.mWebAddress.getScheme();
    }

    public String getHost() {
        return this.mWebAddress.getHost();
    }

    public void setPath(String path) {
        this.mWebAddress.setPath(path);
    }

    public String getPath() {
        return this.mWebAddress.getPath();
    }
}
