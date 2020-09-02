package com.color.darkmode;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ColorDarkModeWebClientWrapper extends WebViewClient {
    private WebViewClient mBase;
    private ColorDarkModeWebManager mDarkModeWebManager;

    public ColorDarkModeWebClientWrapper(WebViewClient mBase2, ColorDarkModeWebManager webManager) {
        this.mBase = mBase2;
        this.mDarkModeWebManager = webManager;
    }

    @Override // android.webkit.WebViewClient
    @Deprecated
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            return super.shouldOverrideUrlLoading(view, url);
        }
        return webViewClient.shouldOverrideUrlLoading(view, url);
    }

    @Override // android.webkit.WebViewClient
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            return super.shouldOverrideUrlLoading(view, request);
        }
        return webViewClient.shouldOverrideUrlLoading(view, request);
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onPageStarted(view, url, favicon);
        } else {
            webViewClient.onPageStarted(view, url, favicon);
        }
    }

    public void onPageFinished(WebView view, String url) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onPageFinished(view, url);
        } else {
            webViewClient.onPageFinished(view, url);
        }
        this.mDarkModeWebManager.injectJS(view);
    }

    public void onLoadResource(WebView view, String url) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onLoadResource(view, url);
        } else {
            webViewClient.onLoadResource(view, url);
        }
    }

    public void onPageCommitVisible(WebView view, String url) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onPageCommitVisible(view, url);
        } else {
            webViewClient.onPageCommitVisible(view, url);
        }
    }

    @Override // android.webkit.WebViewClient
    @Deprecated
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            return super.shouldInterceptRequest(view, url);
        }
        return webViewClient.shouldInterceptRequest(view, url);
    }

    @Override // android.webkit.WebViewClient
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            return super.shouldInterceptRequest(view, request);
        }
        return webViewClient.shouldInterceptRequest(view, request);
    }

    @Deprecated
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onTooManyRedirects(view, cancelMsg, continueMsg);
        } else {
            webViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
        }
    }

    @Deprecated
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        } else {
            webViewClient.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onReceivedError(view, request, error);
        } else {
            webViewClient.onReceivedError(view, request, error);
        }
    }

    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onReceivedHttpError(view, request, errorResponse);
        } else {
            webViewClient.onReceivedHttpError(view, request, errorResponse);
        }
    }

    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onFormResubmission(view, dontResend, resend);
        } else {
            webViewClient.onFormResubmission(view, dontResend, resend);
        }
    }

    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.doUpdateVisitedHistory(view, url, isReload);
        } else {
            webViewClient.doUpdateVisitedHistory(view, url, isReload);
        }
    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onReceivedSslError(view, handler, error);
        } else {
            webViewClient.onReceivedSslError(view, handler, error);
        }
    }

    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onReceivedClientCertRequest(view, request);
        } else {
            webViewClient.onReceivedClientCertRequest(view, request);
        }
    }

    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        } else {
            webViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
        }
    }

    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            return super.shouldOverrideKeyEvent(view, event);
        }
        return webViewClient.shouldOverrideKeyEvent(view, event);
    }

    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onUnhandledKeyEvent(view, event);
        } else {
            webViewClient.onUnhandledKeyEvent(view, event);
        }
    }

    public void onUnhandledInputEvent(WebView view, InputEvent event) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onUnhandledInputEvent(view, event);
        } else {
            webViewClient.onUnhandledInputEvent(view, event);
        }
    }

    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onScaleChanged(view, oldScale, newScale);
        } else {
            webViewClient.onScaleChanged(view, oldScale, newScale);
        }
    }

    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onReceivedLoginRequest(view, realm, account, args);
        } else {
            webViewClient.onReceivedLoginRequest(view, realm, account, args);
        }
    }

    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            return super.onRenderProcessGone(view, detail);
        }
        return webViewClient.onRenderProcessGone(view, detail);
    }

    public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType, SafeBrowsingResponse callback) {
        WebViewClient webViewClient = this.mBase;
        if (webViewClient == null) {
            super.onSafeBrowsingHit(view, request, threatType, callback);
        } else {
            webViewClient.onSafeBrowsingHit(view, request, threatType, callback);
        }
    }
}
