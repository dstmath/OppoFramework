package cn.teddymobile.free.anteater.rule.attribute.webview;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.attribute.AttributeRule;
import cn.teddymobile.free.anteater.rule.utils.ViewHierarchyUtils;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import org.json.JSONException;
import org.json.JSONObject;

public class WebViewRule implements AttributeRule {
    private static final String JSON_FIELD_EXTRACT_TITLE = "extract_title";
    private static final String JSON_FIELD_EXTRACT_URL = "extract_url";
    private static final String JSON_FIELD_TITLE = "title";
    private static final String JSON_FIELD_URL = "url";
    private static final String TAG = WebViewRule.class.getSimpleName();
    private boolean mExtractTitle = false;
    private boolean mExtractUrl = false;

    public String toString() {
        return "[WebViewRule] ExtractTitle = " + this.mExtractTitle + "\nExtractUrl = " + this.mExtractUrl;
    }

    @Override // cn.teddymobile.free.anteater.rule.attribute.AttributeRule
    public void loadFromJSON(JSONObject ruleObject) throws JSONException {
        this.mExtractTitle = ruleObject.getBoolean(JSON_FIELD_EXTRACT_TITLE);
        this.mExtractUrl = ruleObject.getBoolean(JSON_FIELD_EXTRACT_URL);
    }

    @Override // cn.teddymobile.free.anteater.rule.attribute.AttributeRule
    public JSONObject extractAttribute(View view) {
        String url;
        String title;
        JSONObject result = null;
        View decorView = ViewHierarchyUtils.getDecorView(view);
        if (decorView != null) {
            View webView = ViewHierarchyUtils.retrieveWebView(decorView);
            if (webView != null) {
                WebViewHandler handler = new WebViewHandler();
                try {
                    result = new JSONObject();
                    if (this.mExtractTitle && (title = handler.getTitle(webView)) != null) {
                        result.put("title", title);
                    }
                    if (this.mExtractUrl && (url = handler.getUrl(webView)) != null) {
                        result.put("url", url);
                    }
                } catch (JSONException e) {
                }
            } else {
                Logger.w(TAG, "WebView is null.");
            }
        } else {
            Logger.w(TAG, "DecorView is null.");
        }
        String str = TAG;
        Logger.i(str, getClass().getSimpleName() + " Result = " + result);
        return result;
    }

    private static class WebViewHandler extends Handler {
        private String mTitle;
        private String mUrl;

        private WebViewHandler() {
            super(Looper.getMainLooper());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getTitle(final View webView) {
            final CountDownLatch latch = new CountDownLatch(1);
            post(new Runnable() {
                /* class cn.teddymobile.free.anteater.rule.attribute.webview.WebViewRule.WebViewHandler.AnonymousClass1 */

                public void run() {
                    try {
                        Method method = webView.getClass().getMethod("getTitle", new Class[0]);
                        method.setAccessible(true);
                        Object result = method.invoke(webView, new Object[0]);
                        if (result instanceof String) {
                            WebViewHandler.this.mTitle = (String) result;
                            latch.countDown();
                            return;
                        }
                    } catch (Exception e) {
                    }
                    WebViewHandler.this.mTitle = null;
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
            }
            return this.mTitle;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getUrl(final View webView) {
            final CountDownLatch latch = new CountDownLatch(1);
            post(new Runnable() {
                /* class cn.teddymobile.free.anteater.rule.attribute.webview.WebViewRule.WebViewHandler.AnonymousClass2 */

                public void run() {
                    try {
                        Method method = webView.getClass().getMethod("getUrl", new Class[0]);
                        method.setAccessible(true);
                        Object result = method.invoke(webView, new Object[0]);
                        if (result instanceof String) {
                            WebViewHandler.this.mUrl = (String) result;
                            latch.countDown();
                            return;
                        }
                    } catch (Exception e) {
                    }
                    WebViewHandler.this.mUrl = null;
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
            }
            return this.mUrl;
        }
    }
}
