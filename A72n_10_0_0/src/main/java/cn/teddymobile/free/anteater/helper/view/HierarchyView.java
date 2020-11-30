package cn.teddymobile.free.anteater.helper.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.teddymobile.free.anteater.helper.logger.Logger;
import cn.teddymobile.free.anteater.helper.utils.ViewHierarchyUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HierarchyView extends ScrollView {
    private static final int PADDING = 20;
    private static final String TAG = HierarchyView.class.getSimpleName();
    private static final int TEXT_SIZE = 13;
    private int mChildIndex = 0;
    private List<HierarchyView> mChildViewList = null;
    private LinearLayout mContainer = null;
    private boolean mExpanded = false;
    private View mRootView = null;

    public HierarchyView(Context context) {
        super(context);
        initView();
    }

    public static TextView createTextView(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(13.0f);
        textView.setBackgroundColor(-1);
        textView.setTextColor(-16777216);
        textView.setPadding(20, 20, 20, 20);
        if (Build.VERSION.SDK_INT >= 23) {
            textView.setBreakStrategy(0);
        }
        return textView;
    }

    public void updateModel(View rootView, int childIndex) {
        if (rootView != null) {
            this.mRootView = rootView;
            this.mChildIndex = childIndex;
            addRootTextView();
            addChildrenTextView();
        }
    }

    private void initView() {
        this.mContainer = new LinearLayout(getContext());
        this.mContainer.setOrientation(1);
        addView(this.mContainer);
    }

    private void addRootTextView() {
        TextView textview = createTextView(getContext(), "(" + this.mChildIndex + ")\n" + this.mRootView.getClass().getName());
        textview.setTextColor(this.mExpanded ? -65536 : -16777216);
        textview.setOnClickListener(new View.OnClickListener() {
            /* class cn.teddymobile.free.anteater.helper.view.HierarchyView.AnonymousClass1 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                HierarchyView hierarchyView = HierarchyView.this;
                hierarchyView.mExpanded = !hierarchyView.mExpanded;
                for (ViewGroup childView : HierarchyView.this.mChildViewList) {
                    childView.setVisibility(HierarchyView.this.mExpanded ? 0 : 8);
                }
                ((TextView) v).setTextColor(HierarchyView.this.mExpanded ? -65536 : -16777216);
            }
        });
        textview.setOnLongClickListener(new View.OnLongClickListener() {
            /* class cn.teddymobile.free.anteater.helper.view.HierarchyView.AnonymousClass2 */

            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View v) {
                AttributeView attributeView = new AttributeView(HierarchyView.this.getContext());
                attributeView.updateModel(HierarchyView.this.mRootView, HierarchyView.this.mRootView.getClass());
                new AlertDialog.Builder(HierarchyView.this.getContext()).setView(attributeView).create().show();
                HierarchyView hierarchyView = HierarchyView.this;
                hierarchyView.logIdAndResName(hierarchyView.mRootView);
                HierarchyView hierarchyView2 = HierarchyView.this;
                hierarchyView2.logViewHierarchy(hierarchyView2.mRootView);
                HierarchyView hierarchyView3 = HierarchyView.this;
                hierarchyView3.logWebView(hierarchyView3.mRootView);
                return true;
            }
        });
        this.mContainer.addView(textview);
    }

    private void addChildrenTextView() {
        this.mChildViewList = new ArrayList();
        View view = this.mRootView;
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childView = viewGroup.getChildAt(i);
                HierarchyView hierarchyView = new HierarchyView(getContext());
                hierarchyView.updateModel(childView, i);
                hierarchyView.setPadding(30, 0, 0, 0);
                hierarchyView.setVisibility(8);
                this.mChildViewList.add(hierarchyView);
                this.mContainer.addView(hierarchyView);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logIdAndResName(View view) {
        int id = view.getId();
        String resName = null;
        if (id != -1) {
            try {
                resName = getResources().getResourceName(id);
            } catch (Exception e) {
            }
        }
        String str = TAG;
        Logger.i(str, "id = " + id);
        String str2 = TAG;
        Logger.i(str2, "resName = " + resName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logViewHierarchy(View view) {
        List<String> classList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();
        View currentView = view;
        while (!currentView.getClass().getName().endsWith("DecorView")) {
            ViewGroup parent = (ViewGroup) currentView.getParent();
            classList.add(currentView.getClass().getName());
            indexList.add(Integer.valueOf(parent.indexOfChild(currentView)));
            currentView = parent;
        }
        StringBuilder info = new StringBuilder("[");
        for (int i = classList.size() - 1; i >= 0; i--) {
            info.append(String.format(Locale.getDefault(), "{\"class_name\": \"%s\",\"child_index\": %d}", classList.get(i), Integer.valueOf(indexList.get(i).intValue())));
            if (i != 0) {
                info.append(SmsManager.REGEX_PREFIX_DELIMITER);
            }
        }
        info.append("]");
        Logger.i(TAG, "ViewHierarchy = \n" + info.toString());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logWebView(View view) {
        View view2 = ViewHierarchyUtils.retrieveWebView(view);
        if (view2 == null) {
            return;
        }
        if (view2 instanceof WebView) {
            WebView webView = (WebView) view2;
            String str = TAG;
            Logger.i(str, view2.getClass().getName() + " is a native WebView");
            String str2 = TAG;
            Logger.i(str2, "Title = " + webView.getTitle());
            String str3 = TAG;
            Logger.i(str3, "Url = " + webView.getUrl());
            Logger.i(TAG, "WebView Hierarchy : ");
            logViewHierarchy(view2);
            return;
        }
        String str4 = TAG;
        Logger.i(str4, view2.getClass().getName() + " is a ThirdParty WebView ");
        try {
            Method method = view2.getClass().getMethod("getTitle", new Class[0]);
            method.setAccessible(true);
            String str5 = TAG;
            Logger.i(str5, "Title = " + method.invoke(view2, new Object[0]));
            Method method2 = view2.getClass().getMethod("getUrl", new Class[0]);
            method2.setAccessible(true);
            String str6 = TAG;
            Logger.i(str6, "Url = " + method2.invoke(view2, new Object[0]));
        } catch (Exception e) {
            Logger.w(TAG, e.getMessage(), e);
        }
        Logger.i(TAG, "WebView Hierarchy :");
        logViewHierarchy(view2);
    }
}
