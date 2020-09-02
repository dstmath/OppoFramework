package cn.teddymobile.free.anteater.rule.html.javascript;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.html.HtmlRule;
import cn.teddymobile.free.anteater.rule.utils.ViewHierarchyUtils;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;

public class JavaScriptRule implements HtmlRule {
    /* access modifiers changed from: private */
    public static final String TAG = JavaScriptRule.class.getSimpleName();

    public String toString() {
        return "[JavaScriptRule]";
    }

    @Override // cn.teddymobile.free.anteater.rule.html.HtmlRule
    public String getHtml(View view) {
        String result = null;
        View decorView = ViewHierarchyUtils.getDecorView(view);
        if (decorView != null) {
            View webView = ViewHierarchyUtils.retrieveWebView(decorView);
            if (webView != null) {
                WebViewHandler handler = new WebViewHandler();
                handler.enableJavaScript(webView);
                result = handler.getHtml(webView);
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
        /* access modifiers changed from: private */
        public String mResult;

        private WebViewHandler() {
            super(Looper.getMainLooper());
        }

        /* access modifiers changed from: private */
        public void enableJavaScript(final View webView) {
            Logger.i(JavaScriptRule.TAG, "Enable JavaScript.");
            String access$300 = JavaScriptRule.TAG;
            Logger.i(access$300, "WebView = " + webView.getClass().getName());
            final CountDownLatch latch = new CountDownLatch(1);
            post(new Runnable() {
                /* class cn.teddymobile.free.anteater.rule.html.javascript.JavaScriptRule.WebViewHandler.AnonymousClass1 */

                public void run() {
                    try {
                        Method method = webView.getClass().getMethod("getSettings", new Class[0]);
                        method.setAccessible(true);
                        Object settings = method.invoke(webView, new Object[0]);
                        if (settings != null) {
                            Method method2 = settings.getClass().getMethod("setJavaScriptEnabled", Boolean.TYPE);
                            method2.setAccessible(true);
                            method2.invoke(settings, true);
                        }
                    } catch (Exception e) {
                        Logger.w(JavaScriptRule.TAG, e.getMessage(), e);
                    }
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
            }
            Logger.i(JavaScriptRule.TAG, "Enable JavaScript Done.");
        }

        /* access modifiers changed from: private */
        public String getHtml(final View webView) {
            Logger.i(JavaScriptRule.TAG, "Get Html.");
            String access$300 = JavaScriptRule.TAG;
            Logger.i(access$300, "WebView = " + webView.getClass().getName());
            final CountDownLatch latch = new CountDownLatch(1);
            post(new Runnable() {
                /* class cn.teddymobile.free.anteater.rule.html.javascript.JavaScriptRule.WebViewHandler.AnonymousClass2 */

                public void run() {
                    try {
                        Method[] methods = webView.getClass().getMethods();
                        int length = methods.length;
                        int i = 0;
                        while (i < length) {
                            Method method = methods[i];
                            if (!method.getName().equals("evaluateJavascript") || method.getParameterTypes().length != 2) {
                                i++;
                            } else {
                                Class<?> callbackClass = method.getParameterTypes()[1];
                                Object callback = Proxy.newProxyInstance(callbackClass.getClassLoader(), new Class[]{callbackClass}, new InvocationHandler() {
                                    /* class cn.teddymobile.free.anteater.rule.html.javascript.JavaScriptRule.WebViewHandler.AnonymousClass2.AnonymousClass1 */

                                    @Override // java.lang.reflect.InvocationHandler
                                    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                                        if (objects.length != 1 || !(objects[0] instanceof String) || latch.getCount() <= 0) {
                                            return null;
                                        }
                                        String unused = WebViewHandler.this.mResult = (String) objects[0];
                                        latch.countDown();
                                        return null;
                                    }
                                });
                                method.setAccessible(true);
                                method.invoke(webView, "document.body.innerHTML", callback);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Logger.w(JavaScriptRule.TAG, e.getMessage(), e);
                    }
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
            }
            Logger.i(JavaScriptRule.TAG, "Get Html Done.");
            return this.mResult;
        }
    }
}
