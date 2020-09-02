package com.color.darkmode;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewProvider;
import java.lang.ref.WeakReference;

public class ColorDarkModeWebManager {
    private static final boolean DBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean DEBUG = (false | DBG);
    private static final String TAG = "ColorDarkModeWebManager";
    private ColorDarkModeManager mColorDarkModeManager;
    private int mOriginForceDarkSettings = -1;
    private boolean mShouldChangeWebSettings;

    public ColorDarkModeWebManager(ColorDarkModeManager mColorDarkModeManager2) {
        this.mColorDarkModeManager = mColorDarkModeManager2;
    }

    public class DelayHandler extends Handler {
        public static final int MSG_START_DELAY = 1;
        private WeakReference<View> mWeak;

        public DelayHandler(Looper looper, View view) {
            super(looper);
            this.mWeak = new WeakReference<>(view);
        }

        public DelayHandler(View view) {
            this.mWeak = new WeakReference<>(view);
        }

        public void handleMessage(Message msg) {
            View view;
            WeakReference<View> weakReference = this.mWeak;
            if (weakReference != null && (view = weakReference.get()) != null && msg.what == 1 && (view instanceof WebView)) {
                ColorDarkModeWebManager.this.injectJS((WebView) view);
            }
        }
    }

    public void refreshView(View view) {
        if (view instanceof WebView) {
            injectJS((WebView) view);
        }
    }

    public static Context getActivityContext(Context context) {
        Context base;
        while ((context instanceof ContextWrapper) && !(context instanceof Activity) && (base = ((ContextWrapper) context).getBaseContext()) != context) {
            context = base;
        }
        return context;
    }

    public void injectJS(WebView webView) {
        ColorDarkModeManager colorDarkModeManager;
        if (webView != null && webView.getContext() != null && (colorDarkModeManager = this.mColorDarkModeManager) != null && colorDarkModeManager.isConfigurationChanged()) {
            handleSoftWebViewForceDark(webView);
            WebViewProvider provider = webView.getWebViewProvider();
            if (provider != null && shouldInjectJS(webView)) {
                if (webView.getWebViewClient() == null) {
                    webView.setWebViewClient(new WebViewClient());
                }
                if (this.mColorDarkModeManager.isUseColorForceDark()) {
                    provider.loadUrl("javascript:" + ColorDarkModeWebConst.DEFAULT_JS);
                    provider.loadUrl("javascript:applyNightMode()");
                    log("add JS");
                    return;
                }
                provider.loadUrl("javascript:" + ColorDarkModeWebConst.DEFAULT_JS);
                provider.loadUrl("javascript:removeNightMode()");
                log("remove JS");
            }
        }
    }

    public void startDelayInjectJS(WebView webView) {
        if (webView != null) {
            handleSoftWebViewForceDark(webView);
            if (shouldInjectJS(webView)) {
                DelayHandler handler = new DelayHandler(webView);
                Message message = Message.obtain();
                message.what = 1;
                handler.sendMessageDelayed(message, ColorDarkModeWebConst.DEFAULT_DELAY_TIME);
            }
        }
    }

    public WebViewClient createWebViewClientWrapper(WebView webView, WebViewClient client) {
        if (webView != null && shouldInjectJS(webView)) {
            return new ColorDarkModeWebClientWrapper(client, this);
        }
        return client;
    }

    private boolean shouldInjectJS(WebView webView) {
        Context context = webView.getContext();
        if (context == null || context.getPackageName() == null) {
            return false;
        }
        String packageName = context.getPackageName();
        int i = 0;
        if ("com.ss.android.article.news".equals(packageName)) {
            if ("MyWebViewV9".equals(webView.getClass().getSimpleName())) {
                return true;
            }
            String activityClassName = getActivityContext(context).getClass().getSimpleName();
            if (!"SearchActivity".equals(activityClassName) && !"MainActivity".equals(activityClassName)) {
                return false;
            }
            if (this.mOriginForceDarkSettings == -1) {
                this.mOriginForceDarkSettings = webView.getSettings().getForceDark();
            }
            WebSettings settings = webView.getSettings();
            if (!this.mColorDarkModeManager.isUseColorForceDark()) {
                i = this.mOriginForceDarkSettings;
            }
            settings.setForceDark(i);
            return true;
        } else if ("com.ss.android.article.lite".equals(packageName)) {
            return "MyWebViewV9".equals(webView.getClass().getSimpleName());
        } else {
            return ColorDarkModeWebConst.mJSInjectApps.contains(context.getPackageName());
        }
    }

    public static void log(String content) {
        if (DEBUG) {
            Log.d(TAG, content);
        }
    }

    public static void log(String tag, String content) {
        if (DEBUG) {
            Log.d(tag, content);
        }
    }

    private void handleSoftWebViewForceDark(WebView webView) {
        if (!this.mShouldChangeWebSettings) {
            this.mShouldChangeWebSettings = this.mColorDarkModeManager.isUseColorSoftDraw();
        }
        if (this.mShouldChangeWebSettings) {
            if (this.mOriginForceDarkSettings == -1) {
                this.mOriginForceDarkSettings = webView.getSettings().getForceDark();
            }
            webView.getSettings().setForceDark(this.mColorDarkModeManager.isUseColorForceDark() ? 2 : this.mOriginForceDarkSettings);
            log("handleSoftWebViewForceDark->" + this.mColorDarkModeManager.isUseColorForceDark());
        }
    }

    public void tryRefreshWebView(View view) {
        if (view instanceof WebView) {
            injectJS((WebView) view);
        } else if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                tryRefreshWebView(parent.getChildAt(i));
            }
        }
    }
}
