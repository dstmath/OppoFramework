package cn.teddymobile.free.anteater.rule.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ViewHierarchyUtils {
    private static String[] sThirdPartyWebViewClassNames = {"com.uc.webview.export.WebView", "com.tencent.smtt.webkit.WebView", "com.tencent.smtt.sdk.WebView", "sogou.webkit.WebView", "com.baidu.webkit.sdk.WebView", "com.oppo.webview.KKWebview"};

    public static Context getActivityContext(Context context) {
        Context base;
        while ((context instanceof ContextWrapper) && !(context instanceof Activity) && (base = ((ContextWrapper) context).getBaseContext()) != context) {
            context = base;
        }
        return context;
    }

    public static Intent getIntent(View view) {
        Context context = getActivityContext(view.getContext());
        if (context instanceof Activity) {
            return ((Activity) context).getIntent();
        }
        return null;
    }

    public static View getDecorView(View view) {
        if (view == null) {
            return null;
        }
        Context context = view.getContext();
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.getWindow() != null) {
                return activity.getWindow().getDecorView();
            }
            return null;
        }
        while (!view.getClass().getName().endsWith("DecorView")) {
            if (!(view.getParent() instanceof View)) {
                return null;
            }
            view = (View) view.getParent();
        }
        return view;
    }

    public static View retrieveWebView(View view) {
        if (view == null) {
            return null;
        }
        if (view instanceof WebView) {
            return view;
        }
        for (String className : sThirdPartyWebViewClassNames) {
            if (checkClass(view, className)) {
                return view;
            }
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View webView = retrieveWebView(viewGroup.getChildAt(i));
            if (webView != null) {
                return webView;
            }
        }
        return null;
    }

    private static boolean checkClass(Object object, String className) {
        for (Class clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            if (clazz.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }
}
