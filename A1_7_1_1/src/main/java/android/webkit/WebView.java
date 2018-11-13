package android.webkit;

import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.print.PrintDocumentAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.RenderNode;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.HierarchyHandler;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.ViewHierarchyEncoder;
import android.view.ViewRootImpl;
import android.view.ViewStructure;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.AbsoluteLayout;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.perfservice.IPerfServiceWrapper;
import com.mediatek.perfservice.PerfServiceWrapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WebView extends AbsoluteLayout implements OnGlobalFocusChangeListener, OnHierarchyChangeListener, HierarchyHandler {
    public static final String DATA_REDUCTION_PROXY_SETTING_CHANGED = "android.webkit.DATA_REDUCTION_PROXY_SETTING_CHANGED";
    private static final String INCLUDE_SAVE_PAGE_WEBVIEW_PACKAGE = "com.mediatek.webview";
    private static final String LOGTAG = "WebView";
    public static final String SCHEME_GEO = "geo:0,0?q=";
    public static final String SCHEME_MAILTO = "mailto:";
    public static final String SCHEME_TEL = "tel:";
    private static final boolean TRACE = false;
    private static volatile boolean sEnforceThreadChecking;
    private static boolean sFirstLoadData;
    private static int sPerfHandle;
    private static IPerfServiceWrapper sPerfService;
    private Class<?> mCls;
    private FindListenerDistributor mFindListener;
    private WebViewProvider mProvider;
    private final Looper mWebViewThread;

    public interface FindListener {
        void onFindResultReceived(int i, int i2, boolean z);
    }

    /* renamed from: android.webkit.WebView$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ WebView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.1.<init>(android.webkit.WebView):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(android.webkit.WebView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.1.<init>(android.webkit.WebView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.1.<init>(android.webkit.WebView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.webkit.WebView.1.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.webkit.WebView.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.1.run():void");
        }
    }

    private class FindListenerDistributor implements FindListener {
        private FindListener mFindDialogFindListener;
        private FindListener mUserFindListener;
        final /* synthetic */ WebView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.FindListenerDistributor.-set0(android.webkit.WebView$FindListenerDistributor, android.webkit.WebView$FindListener):android.webkit.WebView$FindListener, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        /* renamed from: -set0 */
        static /* synthetic */ android.webkit.WebView.FindListener m58-set0(android.webkit.WebView.FindListenerDistributor r1, android.webkit.WebView.FindListener r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.FindListenerDistributor.-set0(android.webkit.WebView$FindListenerDistributor, android.webkit.WebView$FindListener):android.webkit.WebView$FindListener, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.FindListenerDistributor.-set0(android.webkit.WebView$FindListenerDistributor, android.webkit.WebView$FindListener):android.webkit.WebView$FindListener");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.FindListenerDistributor.-set1(android.webkit.WebView$FindListenerDistributor, android.webkit.WebView$FindListener):android.webkit.WebView$FindListener, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        /* renamed from: -set1 */
        static /* synthetic */ android.webkit.WebView.FindListener m59-set1(android.webkit.WebView.FindListenerDistributor r1, android.webkit.WebView.FindListener r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.FindListenerDistributor.-set1(android.webkit.WebView$FindListenerDistributor, android.webkit.WebView$FindListener):android.webkit.WebView$FindListener, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.FindListenerDistributor.-set1(android.webkit.WebView$FindListenerDistributor, android.webkit.WebView$FindListener):android.webkit.WebView$FindListener");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.FindListenerDistributor.<init>(android.webkit.WebView):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private FindListenerDistributor(android.webkit.WebView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.FindListenerDistributor.<init>(android.webkit.WebView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.FindListenerDistributor.<init>(android.webkit.WebView):void");
        }

        /* synthetic */ FindListenerDistributor(WebView this$0, FindListenerDistributor findListenerDistributor) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.webkit.WebView.FindListenerDistributor.onFindResultReceived(int, int, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onFindResultReceived(int r1, int r2, boolean r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.webkit.WebView.FindListenerDistributor.onFindResultReceived(int, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.FindListenerDistributor.onFindResultReceived(int, int, boolean):void");
        }
    }

    public static class HitTestResult {
        @Deprecated
        public static final int ANCHOR_TYPE = 1;
        public static final int EDIT_TEXT_TYPE = 9;
        public static final int EMAIL_TYPE = 4;
        public static final int GEO_TYPE = 3;
        @Deprecated
        public static final int IMAGE_ANCHOR_TYPE = 6;
        public static final int IMAGE_TYPE = 5;
        public static final int PHONE_TYPE = 2;
        public static final int SRC_ANCHOR_TYPE = 7;
        public static final int SRC_IMAGE_ANCHOR_TYPE = 8;
        public static final int UNKNOWN_TYPE = 0;
        private String mExtra;
        private String mImageAnchorUrlExtra;
        private int mType;

        public HitTestResult() {
            this.mType = 0;
        }

        public void setType(int type) {
            this.mType = type;
        }

        public void setImageAnchorUrlExtra(String imageAnchorUrl) {
            this.mImageAnchorUrlExtra = imageAnchorUrl;
        }

        public void setExtra(String extra) {
            this.mExtra = extra;
        }

        public int getType() {
            return this.mType;
        }

        public String getExtra() {
            return this.mExtra;
        }

        public String getImageAnchorUrlExtra() {
            return this.mImageAnchorUrlExtra;
        }
    }

    @Deprecated
    public interface PictureListener {
        @Deprecated
        void onNewPicture(WebView webView, Picture picture);
    }

    public class PrivateAccess {
        final /* synthetic */ WebView this$0;

        public PrivateAccess(WebView this$0) {
            this.this$0 = this$0;
        }

        public int super_getScrollBarStyle() {
            return super.getScrollBarStyle();
        }

        public void super_scrollTo(int scrollX, int scrollY) {
            super.scrollTo(scrollX, scrollY);
        }

        public void super_computeScroll() {
            super.computeScroll();
        }

        public boolean super_onHoverEvent(MotionEvent event) {
            return super.onHoverEvent(event);
        }

        public boolean super_performAccessibilityAction(int action, Bundle arguments) {
            return super.performAccessibilityActionInternal(action, arguments);
        }

        public boolean super_performLongClick() {
            return super.performLongClick();
        }

        public boolean super_setFrame(int left, int top, int right, int bottom) {
            return super.setFrame(left, top, right, bottom);
        }

        public boolean super_dispatchKeyEvent(KeyEvent event) {
            return super.dispatchKeyEvent(event);
        }

        public boolean super_onGenericMotionEvent(MotionEvent event) {
            return super.onGenericMotionEvent(event);
        }

        public boolean super_requestFocus(int direction, Rect previouslyFocusedRect) {
            return super.requestFocus(direction, previouslyFocusedRect);
        }

        public void super_setLayoutParams(LayoutParams params) {
            super.setLayoutParams(params);
        }

        public void super_startActivityForResult(Intent intent, int requestCode) {
            super.startActivityForResult(intent, requestCode);
        }

        public void overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
            this.this$0.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        }

        public void awakenScrollBars(int duration) {
            this.this$0.awakenScrollBars(duration);
        }

        public void awakenScrollBars(int duration, boolean invalidate) {
            this.this$0.awakenScrollBars(duration, invalidate);
        }

        public float getVerticalScrollFactor() {
            return this.this$0.getVerticalScrollFactor();
        }

        public float getHorizontalScrollFactor() {
            return this.this$0.getHorizontalScrollFactor();
        }

        public void setMeasuredDimension(int measuredWidth, int measuredHeight) {
            this.this$0.setMeasuredDimension(measuredWidth, measuredHeight);
        }

        public void onScrollChanged(int l, int t, int oldl, int oldt) {
            this.this$0.onScrollChanged(l, t, oldl, oldt);
        }

        public int getHorizontalScrollbarHeight() {
            return this.this$0.getHorizontalScrollbarHeight();
        }

        public void super_onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
            super.onDrawVerticalScrollBar(canvas, scrollBar, l, t, r, b);
        }

        public void setScrollXRaw(int scrollX) {
            this.this$0.mScrollX = scrollX;
        }

        public void setScrollYRaw(int scrollY) {
            this.this$0.mScrollY = scrollY;
        }
    }

    public static abstract class VisualStateCallback {
        public abstract void onComplete(long j);

        public VisualStateCallback() {
        }
    }

    public class WebViewTransport {
        private WebView mWebview;
        final /* synthetic */ WebView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.WebViewTransport.<init>(android.webkit.WebView):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public WebViewTransport(android.webkit.WebView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.WebViewTransport.<init>(android.webkit.WebView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.WebViewTransport.<init>(android.webkit.WebView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.webkit.WebView.WebViewTransport.getWebView():android.webkit.WebView, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public synchronized android.webkit.WebView getWebView() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.webkit.WebView.WebViewTransport.getWebView():android.webkit.WebView, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.WebViewTransport.getWebView():android.webkit.WebView");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.WebViewTransport.setWebView(android.webkit.WebView):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public synchronized void setWebView(android.webkit.WebView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.webkit.WebView.WebViewTransport.setWebView(android.webkit.WebView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.WebViewTransport.setWebView(android.webkit.WebView):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.webkit.WebView.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.webkit.WebView.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebView.<clinit>():void");
    }

    public WebView(Context context) {
        this(context, null);
    }

    public WebView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.webViewStyle);
    }

    public WebView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr, defStyleRes, null, false);
    }

    @Deprecated
    public WebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        this(context, attrs, defStyleAttr, 0, null, privateBrowsing);
    }

    protected WebView(Context context, AttributeSet attrs, int defStyleAttr, Map<String, Object> javaScriptInterfaces, boolean privateBrowsing) {
        this(context, attrs, defStyleAttr, 0, javaScriptInterfaces, privateBrowsing);
    }

    protected WebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, Map<String, Object> javaScriptInterfaces, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mWebViewThread = Looper.myLooper();
        if (context == null) {
            throw new IllegalArgumentException("Invalid context argument");
        }
        sEnforceThreadChecking = context.getApplicationInfo().targetSdkVersion >= 18;
        checkThread();
        Log.d(LOGTAG, "WebView<init>");
        ensureProviderCreated();
        this.mProvider.init(javaScriptInterfaces, privateBrowsing);
        CookieSyncManager.setGetInstanceIsAllowed();
    }

    @Deprecated
    public void setHorizontalScrollbarOverlay(boolean overlay) {
        Log.d(LOGTAG, "setHorizontalScrollbarOverlay=" + overlay);
    }

    @Deprecated
    public void setVerticalScrollbarOverlay(boolean overlay) {
        Log.d(LOGTAG, "setVerticalScrollbarOverlay=" + overlay);
    }

    @Deprecated
    public boolean overlayHorizontalScrollbar() {
        return true;
    }

    @Deprecated
    public boolean overlayVerticalScrollbar() {
        return false;
    }

    public int getVisibleTitleHeight() {
        checkThread();
        return this.mProvider.getVisibleTitleHeight();
    }

    public SslCertificate getCertificate() {
        checkThread();
        return this.mProvider.getCertificate();
    }

    @Deprecated
    public void setCertificate(SslCertificate certificate) {
        checkThread();
        Log.d(LOGTAG, "setCertificate");
        if (TRACE) {
            Log.d(LOGTAG, "setCertificate=" + certificate);
        }
        this.mProvider.setCertificate(certificate);
    }

    @Deprecated
    public void savePassword(String host, String username, String password) {
        checkThread();
        Log.d(LOGTAG, "savePassword");
        if (TRACE) {
            Log.d(LOGTAG, "savePassword=" + host);
        }
        this.mProvider.savePassword(host, username, password);
    }

    public void setHttpAuthUsernamePassword(String host, String realm, String username, String password) {
        checkThread();
        Log.d(LOGTAG, "setHttpAuthUsernamePassword");
        if (TRACE) {
            Log.d(LOGTAG, "setHttpAuthUsernamePassword=" + host);
        }
        this.mProvider.setHttpAuthUsernamePassword(host, realm, username, password);
    }

    public String[] getHttpAuthUsernamePassword(String host, String realm) {
        checkThread();
        return this.mProvider.getHttpAuthUsernamePassword(host, realm);
    }

    public void destroy() {
        checkThread();
        Log.d(LOGTAG, "destroy");
        this.mProvider.destroy();
        RenderNode displayList = updateDisplayListIfDirty();
        if (displayList != null) {
            displayList.discardDisplayList();
        }
    }

    @Deprecated
    public static void enablePlatformNotifications() {
    }

    @Deprecated
    public static void disablePlatformNotifications() {
    }

    public static void freeMemoryForTests() {
        getFactory().getStatics().freeMemoryForTests();
    }

    public void setNetworkAvailable(boolean networkUp) {
        checkThread();
        Log.d(LOGTAG, "setNetworkAvailable=" + networkUp);
        this.mProvider.setNetworkAvailable(networkUp);
    }

    public WebBackForwardList saveState(Bundle outState) {
        checkThread();
        Log.d(LOGTAG, "saveState");
        return this.mProvider.saveState(outState);
    }

    @Deprecated
    public boolean savePicture(Bundle b, File dest) {
        checkThread();
        Log.d(LOGTAG, "savePicture");
        if (TRACE) {
            Log.d(LOGTAG, "savePicture=" + dest.getName());
        }
        return this.mProvider.savePicture(b, dest);
    }

    @Deprecated
    public boolean restorePicture(Bundle b, File src) {
        checkThread();
        Log.d(LOGTAG, "restorePicture");
        if (TRACE) {
            Log.d(LOGTAG, "restorePicture=" + src.getName());
        }
        return this.mProvider.restorePicture(b, src);
    }

    public WebBackForwardList restoreState(Bundle inState) {
        checkThread();
        Log.d(LOGTAG, "restoreState");
        return this.mProvider.restoreState(inState);
    }

    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        checkThread();
        Log.d(LOGTAG, "loadUrl(extra headers)");
        if (TRACE) {
            StringBuilder headers = new StringBuilder();
            if (additionalHttpHeaders != null) {
                for (Entry<String, String> entry : additionalHttpHeaders.entrySet()) {
                    headers.append((String) entry.getKey()).append(":").append((String) entry.getValue()).append("\n");
                }
            }
            Log.d(LOGTAG, "loadUrl(extra headers)=" + url + "\n" + headers);
        }
        launchLoad();
        this.mProvider.loadUrl(url, additionalHttpHeaders);
    }

    public void loadUrl(String url) {
        checkThread();
        Log.d(LOGTAG, "loadUrl");
        if (TRACE) {
            Log.d(LOGTAG, "loadUrl=" + url);
        }
        launchLoad();
        this.mProvider.loadUrl(url);
    }

    public void postUrl(String url, byte[] postData) {
        checkThread();
        Log.d(LOGTAG, "postUrl");
        if (TRACE) {
            Log.d(LOGTAG, "postUrl=" + url);
        }
        launchLoad();
        if (URLUtil.isNetworkUrl(url)) {
            this.mProvider.postUrl(url, postData);
        } else {
            this.mProvider.loadUrl(url);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0018 A:{ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), Splitter: B:1:0x0001} */
    /* JADX WARNING: Missing block: B:3:0x0018, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0019, code:
            r0.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getMaxCpuFreq() {
        int maxFreq = 0;
        try {
            RandomAccessFile reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
            String stringMaxFreq = reader.readLine();
            reader.close();
            return Integer.parseInt(stringMaxFreq);
        } catch (Exception e) {
        }
    }

    private void launchLoad() {
        sFirstLoadData = false;
    }

    private boolean isFirstLoadData() {
        return sFirstLoadData;
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0025 A:{ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), Splitter: B:1:0x0001} */
    /* JADX WARNING: Missing block: B:8:0x0025, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0026, code:
            r0.printStackTrace();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int boostNormal() {
        int maxFreq = 0;
        try {
            RandomAccessFile reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
            String stringMaxFreq = reader.readLine();
            reader.close();
            maxFreq = Integer.parseInt(stringMaxFreq);
        } catch (Exception e) {
        }
        int minCPU = 2;
        if (maxFreq < 1100000) {
            minCPU = 4;
        }
        return sPerfService.userReg(minCPU, maxFreq);
    }

    private int boostFor3Cluster() {
        int handle = sPerfService.userRegScn();
        if (handle != -1) {
            sPerfService.userRegScnConfig(handle, 50, SystemProperties.getInt("chromium.boostvalue", 80), 0, 0, 0);
        }
        return handle;
    }

    private void setupPerfMode() {
        if (isFirstLoadData()) {
            launchLoad();
            if (sPerfService == null) {
                sPerfService = new PerfServiceWrapper(null);
            }
            sPerfHandle = Runtime.getRuntime().availableProcessors() <= 8 ? boostNormal() : boostFor3Cluster();
            if (sPerfHandle != -1) {
                sPerfService.userEnableTimeoutMs(sPerfHandle, 500);
                new Handler().postDelayed(new AnonymousClass1(this), 600);
            }
        }
    }

    public void loadData(String data, String mimeType, String encoding) {
        checkThread();
        Log.d(LOGTAG, "loadData");
        setupPerfMode();
        this.mProvider.loadData(data, mimeType, encoding);
    }

    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        checkThread();
        Log.d(LOGTAG, "loadDataWithBaseURL");
        if (TRACE) {
            Log.d(LOGTAG, "loadDataWithBaseURL=" + baseUrl);
        }
        launchLoad();
        this.mProvider.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    public void evaluateJavascript(String script, ValueCallback<String> resultCallback) {
        checkThread();
        Log.d(LOGTAG, "evaluateJavascript=" + script);
        this.mProvider.evaluateJavaScript(script, resultCallback);
    }

    public void saveWebArchive(String filename) {
        checkThread();
        Log.d(LOGTAG, "saveWebArchive");
        if (TRACE) {
            Log.d(LOGTAG, "saveWebArchive=" + filename);
        }
        this.mProvider.saveWebArchive(filename);
    }

    public boolean savePage() {
        checkThread();
        Log.d(LOGTAG, "savePage");
        initChromiumClassIfNeccessary();
        if (this.mCls == null) {
            Log.e(LOGTAG, "Can't get WebViewChromium Save Page Interface");
            return false;
        }
        try {
            Method savePageMethod = this.mCls.getDeclaredMethod("savePage", new Class[0]);
            if (savePageMethod != null) {
                return ((Boolean) savePageMethod.invoke(this.mProvider, new Object[0])).booleanValue();
            }
            Log.e(LOGTAG, "Get Null from webviewchromium savePage method");
            return false;
        } catch (ReflectiveOperationException ex) {
            Log.e(LOGTAG, "get Save Page Interface Exception->" + ex);
            return false;
        }
    }

    private void initChromiumClassIfNeccessary() {
        if (this.mCls == null) {
            try {
                Application initialApplication = AppGlobals.getInitialApplication();
                if (initialApplication == null) {
                    throw new ReflectiveOperationException("Applicatin not found");
                }
                Context webViewContext = initialApplication.createPackageContext(INCLUDE_SAVE_PAGE_WEBVIEW_PACKAGE, 3);
                initialApplication.getAssets().addAssetPath(webViewContext.getApplicationInfo().sourceDir);
                this.mCls = Class.forName("com.android.webview.chromium.WebViewChromium", true, webViewContext.getClassLoader());
            } catch (NameNotFoundException ex) {
                Log.e(LOGTAG, "get Webview Class Exception->" + ex);
            } catch (ReflectiveOperationException ex2) {
                Log.e(LOGTAG, "get Webview Class Exception->" + ex2);
            }
        }
    }

    public void saveWebArchive(String basename, boolean autoname, ValueCallback<String> callback) {
        checkThread();
        Log.d(LOGTAG, "saveWebArchive(auto)");
        if (TRACE) {
            Log.d(LOGTAG, "saveWebArchive(auto)=" + basename);
        }
        this.mProvider.saveWebArchive(basename, autoname, callback);
    }

    public void stopLoading() {
        checkThread();
        Log.d(LOGTAG, "stopLoading");
        this.mProvider.stopLoading();
    }

    public void reload() {
        checkThread();
        Log.d(LOGTAG, "reload");
        this.mProvider.reload();
    }

    public boolean canGoBack() {
        checkThread();
        return this.mProvider.canGoBack();
    }

    public void goBack() {
        checkThread();
        Log.d(LOGTAG, "goBack");
        this.mProvider.goBack();
    }

    public boolean canGoForward() {
        checkThread();
        return this.mProvider.canGoForward();
    }

    public void goForward() {
        checkThread();
        Log.d(LOGTAG, "goForward");
        this.mProvider.goForward();
    }

    public boolean canGoBackOrForward(int steps) {
        checkThread();
        return this.mProvider.canGoBackOrForward(steps);
    }

    public void goBackOrForward(int steps) {
        checkThread();
        Log.d(LOGTAG, "goBackOrForwad=" + steps);
        this.mProvider.goBackOrForward(steps);
    }

    public boolean isPrivateBrowsingEnabled() {
        checkThread();
        return this.mProvider.isPrivateBrowsingEnabled();
    }

    public boolean pageUp(boolean top) {
        checkThread();
        Log.d(LOGTAG, "pageUp");
        return this.mProvider.pageUp(top);
    }

    public boolean pageDown(boolean bottom) {
        checkThread();
        Log.d(LOGTAG, "pageDown");
        return this.mProvider.pageDown(bottom);
    }

    public void postVisualStateCallback(long requestId, VisualStateCallback callback) {
        checkThread();
        this.mProvider.insertVisualStateCallback(requestId, callback);
    }

    @Deprecated
    public void clearView() {
        checkThread();
        Log.d(LOGTAG, "clearView");
        this.mProvider.clearView();
    }

    @Deprecated
    public Picture capturePicture() {
        checkThread();
        Log.d(LOGTAG, "capturePicture");
        return this.mProvider.capturePicture();
    }

    @Deprecated
    public PrintDocumentAdapter createPrintDocumentAdapter() {
        checkThread();
        Log.d(LOGTAG, "createPrintDocumentAdapter");
        return this.mProvider.createPrintDocumentAdapter(PhoneConstants.APN_TYPE_DEFAULT);
    }

    public PrintDocumentAdapter createPrintDocumentAdapter(String documentName) {
        checkThread();
        Log.d(LOGTAG, "createPrintDocumentAdapter");
        return this.mProvider.createPrintDocumentAdapter(documentName);
    }

    @ExportedProperty(category = "webview")
    @Deprecated
    public float getScale() {
        checkThread();
        return this.mProvider.getScale();
    }

    public void setInitialScale(int scaleInPercent) {
        checkThread();
        Log.d(LOGTAG, "setInitialScale=" + scaleInPercent);
        this.mProvider.setInitialScale(scaleInPercent);
    }

    public void invokeZoomPicker() {
        checkThread();
        Log.d(LOGTAG, "invokeZoomPicker");
        this.mProvider.invokeZoomPicker();
    }

    public HitTestResult getHitTestResult() {
        checkThread();
        Log.d(LOGTAG, "getHitTestResult");
        return this.mProvider.getHitTestResult();
    }

    public void requestFocusNodeHref(Message hrefMsg) {
        checkThread();
        Log.d(LOGTAG, "requestFocusNodeHref");
        this.mProvider.requestFocusNodeHref(hrefMsg);
    }

    public void requestImageRef(Message msg) {
        checkThread();
        Log.d(LOGTAG, "requestImageRef");
        this.mProvider.requestImageRef(msg);
    }

    @ExportedProperty(category = "webview")
    public String getUrl() {
        checkThread();
        return this.mProvider.getUrl();
    }

    @ExportedProperty(category = "webview")
    public String getOriginalUrl() {
        checkThread();
        return this.mProvider.getOriginalUrl();
    }

    @ExportedProperty(category = "webview")
    public String getTitle() {
        checkThread();
        return this.mProvider.getTitle();
    }

    public Bitmap getFavicon() {
        checkThread();
        return this.mProvider.getFavicon();
    }

    public String getTouchIconUrl() {
        return this.mProvider.getTouchIconUrl();
    }

    public int getProgress() {
        checkThread();
        return this.mProvider.getProgress();
    }

    @ExportedProperty(category = "webview")
    public int getContentHeight() {
        checkThread();
        return this.mProvider.getContentHeight();
    }

    @ExportedProperty(category = "webview")
    public int getContentWidth() {
        return this.mProvider.getContentWidth();
    }

    public void pauseTimers() {
        checkThread();
        Log.d(LOGTAG, "pauseTimers");
        this.mProvider.pauseTimers();
    }

    public void resumeTimers() {
        checkThread();
        Log.d(LOGTAG, "resumeTimers");
        this.mProvider.resumeTimers();
    }

    public void onPause() {
        checkThread();
        Log.d(LOGTAG, "onPause");
        this.mProvider.onPause();
    }

    public void onResume() {
        checkThread();
        Log.d(LOGTAG, "onResume");
        this.mProvider.onResume();
    }

    public boolean isPaused() {
        return this.mProvider.isPaused();
    }

    @Deprecated
    public void freeMemory() {
        checkThread();
        Log.d(LOGTAG, "freeMemory");
        this.mProvider.freeMemory();
    }

    public void clearCache(boolean includeDiskFiles) {
        checkThread();
        Log.d(LOGTAG, "clearCache");
        this.mProvider.clearCache(includeDiskFiles);
    }

    public void clearFormData() {
        checkThread();
        Log.d(LOGTAG, "clearFormData");
        this.mProvider.clearFormData();
    }

    public void clearHistory() {
        checkThread();
        Log.d(LOGTAG, "clearHistory");
        this.mProvider.clearHistory();
    }

    public void clearSslPreferences() {
        checkThread();
        Log.d(LOGTAG, "clearSslPreferences");
        this.mProvider.clearSslPreferences();
    }

    public static void clearClientCertPreferences(Runnable onCleared) {
        Log.d(LOGTAG, "clearClientCertPreferences");
        getFactory().getStatics().clearClientCertPreferences(onCleared);
    }

    public WebBackForwardList copyBackForwardList() {
        checkThread();
        return this.mProvider.copyBackForwardList();
    }

    public void setFindListener(FindListener listener) {
        checkThread();
        setupFindListenerIfNeeded();
        FindListenerDistributor.m59-set1(this.mFindListener, listener);
    }

    public void findNext(boolean forward) {
        checkThread();
        Log.d(LOGTAG, "findNext");
        this.mProvider.findNext(forward);
    }

    @Deprecated
    public int findAll(String find) {
        checkThread();
        Log.d(LOGTAG, "findAll");
        StrictMode.noteSlowCall("findAll blocks UI: prefer findAllAsync");
        return this.mProvider.findAll(find);
    }

    public void findAllAsync(String find) {
        checkThread();
        Log.d(LOGTAG, "findAllAsync");
        this.mProvider.findAllAsync(find);
    }

    @Deprecated
    public boolean showFindDialog(String text, boolean showIme) {
        checkThread();
        Log.d(LOGTAG, "showFindDialog");
        return this.mProvider.showFindDialog(text, showIme);
    }

    public static String findAddress(String addr) {
        return getFactory().getStatics().findAddress(addr);
    }

    public static void enableSlowWholeDocumentDraw() {
        getFactory().getStatics().enableSlowWholeDocumentDraw();
    }

    public void clearMatches() {
        checkThread();
        Log.d(LOGTAG, "clearMatches");
        this.mProvider.clearMatches();
    }

    public void documentHasImages(Message response) {
        checkThread();
        this.mProvider.documentHasImages(response);
    }

    public void setWebViewClient(WebViewClient client) {
        checkThread();
        this.mProvider.setWebViewClient(client);
    }

    public void setSavePageClient(SavePageClient client) {
        checkThread();
        initChromiumClassIfNeccessary();
        if (this.mCls == null) {
            Log.e(LOGTAG, "Can't get WebViewChromium Save Page Interface");
            return;
        }
        try {
            Class[] p = new Class[1];
            p[0] = Object.class;
            Method setSavePageClientMethod = this.mCls.getDeclaredMethod("setSavePageClient", p);
            if (setSavePageClientMethod == null) {
                Log.e(LOGTAG, "Get Null from the webviewchromium setSavePageClient method");
                return;
            }
            WebViewProvider webViewProvider = this.mProvider;
            Object[] objArr = new Object[1];
            objArr[0] = client;
            setSavePageClientMethod.invoke(webViewProvider, objArr);
        } catch (ReflectiveOperationException ex) {
            Log.e(LOGTAG, "get set Save Page Client Interface Exception->" + ex);
        }
    }

    public void setDownloadListener(DownloadListener listener) {
        checkThread();
        this.mProvider.setDownloadListener(listener);
    }

    public void setWebChromeClient(WebChromeClient client) {
        checkThread();
        this.mProvider.setWebChromeClient(client);
    }

    @Deprecated
    public void setPictureListener(PictureListener listener) {
        checkThread();
        Log.d(LOGTAG, "setPictureListener=" + listener);
        this.mProvider.setPictureListener(listener);
    }

    public void addJavascriptInterface(Object object, String name) {
        checkThread();
        Log.d(LOGTAG, "addJavascriptInterface=" + name);
        this.mProvider.addJavascriptInterface(object, name);
    }

    public void removeJavascriptInterface(String name) {
        checkThread();
        Log.d(LOGTAG, "removeJavascriptInterface=" + name);
        this.mProvider.removeJavascriptInterface(name);
    }

    public WebMessagePort[] createWebMessageChannel() {
        checkThread();
        return this.mProvider.createWebMessageChannel();
    }

    public void postWebMessage(WebMessage message, Uri targetOrigin) {
        checkThread();
        this.mProvider.postMessageToMainFrame(message, targetOrigin);
    }

    public WebSettings getSettings() {
        checkThread();
        return this.mProvider.getSettings();
    }

    public static void setWebContentsDebuggingEnabled(boolean enabled) {
        getFactory().getStatics().setWebContentsDebuggingEnabled(enabled);
    }

    @Deprecated
    public static synchronized PluginList getPluginList() {
        PluginList pluginList;
        synchronized (WebView.class) {
            pluginList = new PluginList();
        }
        return pluginList;
    }

    @Deprecated
    public void refreshPlugins(boolean reloadOpenPages) {
        checkThread();
    }

    @Deprecated
    public void emulateShiftHeld() {
        checkThread();
    }

    @Deprecated
    public void onChildViewAdded(View parent, View child) {
    }

    @Deprecated
    public void onChildViewRemoved(View p, View child) {
    }

    @Deprecated
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
    }

    @Deprecated
    public void setMapTrackballToArrowKeys(boolean setMap) {
        checkThread();
        this.mProvider.setMapTrackballToArrowKeys(setMap);
    }

    public void flingScroll(int vx, int vy) {
        checkThread();
        Log.d(LOGTAG, "flingScroll");
        this.mProvider.flingScroll(vx, vy);
    }

    @Deprecated
    public View getZoomControls() {
        checkThread();
        return this.mProvider.getZoomControls();
    }

    @Deprecated
    public boolean canZoomIn() {
        checkThread();
        return this.mProvider.canZoomIn();
    }

    @Deprecated
    public boolean canZoomOut() {
        checkThread();
        return this.mProvider.canZoomOut();
    }

    public void zoomBy(float zoomFactor) {
        checkThread();
        if (((double) zoomFactor) < 0.01d) {
            throw new IllegalArgumentException("zoomFactor must be greater than 0.01.");
        } else if (((double) zoomFactor) > 100.0d) {
            throw new IllegalArgumentException("zoomFactor must be less than 100.");
        } else {
            this.mProvider.zoomBy(zoomFactor);
        }
    }

    public boolean zoomIn() {
        checkThread();
        return this.mProvider.zoomIn();
    }

    public boolean zoomOut() {
        checkThread();
        return this.mProvider.zoomOut();
    }

    @Deprecated
    public void debugDump() {
        checkThread();
    }

    public void dumpViewHierarchyWithProperties(BufferedWriter out, int level) {
        this.mProvider.dumpViewHierarchyWithProperties(out, level);
    }

    public View findHierarchyView(String className, int hashCode) {
        return this.mProvider.findHierarchyView(className, hashCode);
    }

    public WebViewProvider getWebViewProvider() {
        return this.mProvider;
    }

    void setFindDialogFindListener(FindListener listener) {
        checkThread();
        setupFindListenerIfNeeded();
        FindListenerDistributor.m58-set0(this.mFindListener, listener);
    }

    void notifyFindDialogDismissed() {
        checkThread();
        this.mProvider.notifyFindDialogDismissed();
        requestFocus();
    }

    private void setupFindListenerIfNeeded() {
        if (this.mFindListener == null) {
            this.mFindListener = new FindListenerDistributor(this, null);
            this.mProvider.setFindListener(this.mFindListener);
        }
    }

    private void ensureProviderCreated() {
        checkThread();
        if (this.mProvider == null) {
            this.mProvider = getFactory().createWebView(this, new PrivateAccess(this));
        }
    }

    private static synchronized WebViewFactoryProvider getFactory() {
        WebViewFactoryProvider provider;
        synchronized (WebView.class) {
            provider = WebViewFactory.getProvider();
        }
        return provider;
    }

    private void checkThread() {
        if (this.mWebViewThread != null && Looper.myLooper() != this.mWebViewThread) {
            Throwable throwable = new Throwable("A WebView method was called on thread '" + Thread.currentThread().getName() + "'. " + "All WebView methods must be called on the same thread. " + "(Expected Looper " + this.mWebViewThread + " called on " + Looper.myLooper() + ", FYI main Looper is " + Looper.getMainLooper() + ")");
            Log.w(LOGTAG, Log.getStackTraceString(throwable));
            StrictMode.onWebViewMethodCalledOnWrongThread(throwable);
            if (sEnforceThreadChecking) {
                throw new RuntimeException(throwable);
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mProvider.getViewDelegate().onAttachedToWindow();
    }

    protected void onDetachedFromWindowInternal() {
        this.mProvider.getViewDelegate().onDetachedFromWindow();
        super.onDetachedFromWindowInternal();
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            viewRootImpl.detachFunctor(0);
        }
    }

    public void setLayoutParams(LayoutParams params) {
        this.mProvider.getViewDelegate().setLayoutParams(params);
    }

    public void setOverScrollMode(int mode) {
        super.setOverScrollMode(mode);
        ensureProviderCreated();
        this.mProvider.getViewDelegate().setOverScrollMode(mode);
    }

    public void setScrollBarStyle(int style) {
        this.mProvider.getViewDelegate().setScrollBarStyle(style);
        super.setScrollBarStyle(style);
    }

    protected int computeHorizontalScrollRange() {
        return this.mProvider.getScrollDelegate().computeHorizontalScrollRange();
    }

    protected int computeHorizontalScrollOffset() {
        return this.mProvider.getScrollDelegate().computeHorizontalScrollOffset();
    }

    protected int computeVerticalScrollRange() {
        return this.mProvider.getScrollDelegate().computeVerticalScrollRange();
    }

    protected int computeVerticalScrollOffset() {
        return this.mProvider.getScrollDelegate().computeVerticalScrollOffset();
    }

    protected int computeVerticalScrollExtent() {
        return this.mProvider.getScrollDelegate().computeVerticalScrollExtent();
    }

    public void computeScroll() {
        this.mProvider.getScrollDelegate().computeScroll();
    }

    public boolean onHoverEvent(MotionEvent event) {
        return this.mProvider.getViewDelegate().onHoverEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return this.mProvider.getViewDelegate().onTouchEvent(event);
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return this.mProvider.getViewDelegate().onGenericMotionEvent(event);
    }

    public boolean onTrackballEvent(MotionEvent event) {
        return this.mProvider.getViewDelegate().onTrackballEvent(event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.mProvider.getViewDelegate().onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.mProvider.getViewDelegate().onKeyUp(keyCode, event);
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return this.mProvider.getViewDelegate().onKeyMultiple(keyCode, repeatCount, event);
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        AccessibilityNodeProvider provider = this.mProvider.getViewDelegate().getAccessibilityNodeProvider();
        return provider == null ? super.getAccessibilityNodeProvider() : provider;
    }

    @Deprecated
    public boolean shouldDelayChildPressedState() {
        return this.mProvider.getViewDelegate().shouldDelayChildPressedState();
    }

    public CharSequence getAccessibilityClassName() {
        return WebView.class.getName();
    }

    public void onProvideVirtualStructure(ViewStructure structure) {
        this.mProvider.getViewDelegate().onProvideVirtualStructure(structure);
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        this.mProvider.getViewDelegate().onInitializeAccessibilityNodeInfo(info);
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        this.mProvider.getViewDelegate().onInitializeAccessibilityEvent(event);
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        return this.mProvider.getViewDelegate().performAccessibilityAction(action, arguments);
    }

    protected void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        this.mProvider.getViewDelegate().onDrawVerticalScrollBar(canvas, scrollBar, l, t, r, b);
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        this.mProvider.getViewDelegate().onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mProvider.getViewDelegate().onWindowVisibilityChanged(visibility);
    }

    protected void onDraw(Canvas canvas) {
        this.mProvider.getViewDelegate().onDraw(canvas);
    }

    public boolean performLongClick() {
        return this.mProvider.getViewDelegate().performLongClick();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        this.mProvider.getViewDelegate().onConfigurationChanged(newConfig);
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return this.mProvider.getViewDelegate().onCreateInputConnection(outAttrs);
    }

    public boolean onDragEvent(DragEvent event) {
        try {
            return this.mProvider.getViewDelegate().onDragEvent(event);
        } catch (AbstractMethodError e) {
            return false;
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        ensureProviderCreated();
        this.mProvider.getViewDelegate().onVisibilityChanged(changedView, visibility);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        this.mProvider.getViewDelegate().onWindowFocusChanged(hasWindowFocus);
        super.onWindowFocusChanged(hasWindowFocus);
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        this.mProvider.getViewDelegate().onFocusChanged(focused, direction, previouslyFocusedRect);
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        return this.mProvider.getViewDelegate().setFrame(left, top, right, bottom);
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        this.mProvider.getViewDelegate().onSizeChanged(w, h, ow, oh);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        this.mProvider.getViewDelegate().onScrollChanged(l, t, oldl, oldt);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return this.mProvider.getViewDelegate().dispatchKeyEvent(event);
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return this.mProvider.getViewDelegate().requestFocus(direction, previouslyFocusedRect);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mProvider.getViewDelegate().onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        return this.mProvider.getViewDelegate().requestChildRectangleOnScreen(child, rect, immediate);
    }

    public void setBackgroundColor(int color) {
        this.mProvider.getViewDelegate().setBackgroundColor(color);
    }

    public void setLayerType(int layerType, Paint paint) {
        String callPackage = AppGlobals.getInitialApplication().getPackageName();
        if (callPackage != null && callPackage.equalsIgnoreCase("com.ants360.yicamera") && layerType == 1) {
            layerType = 2;
        }
        super.setLayerType(layerType, paint);
        Log.w(LOGTAG, "setLayerType()");
        if (this.mProvider != null) {
            this.mProvider.getViewDelegate().setLayerType(layerType, paint);
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        this.mProvider.getViewDelegate().preDispatchDraw(canvas);
        super.dispatchDraw(canvas);
    }

    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        this.mProvider.getViewDelegate().onStartTemporaryDetach();
    }

    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        this.mProvider.getViewDelegate().onFinishTemporaryDetach();
    }

    public Handler getHandler() {
        try {
            return this.mProvider.getViewDelegate().getHandler(super.getHandler());
        } catch (AbstractMethodError e) {
            return super.getHandler();
        }
    }

    public View findFocus() {
        try {
            return this.mProvider.getViewDelegate().findFocus(super.findFocus());
        } catch (AbstractMethodError e) {
            return super.findFocus();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            this.mProvider.getViewDelegate().onActivityResult(requestCode, resultCode, data);
        } catch (AbstractMethodError e) {
        }
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        checkThread();
        encoder.addProperty("webview:contentHeight", this.mProvider.getContentHeight());
        encoder.addProperty("webview:contentWidth", this.mProvider.getContentWidth());
        encoder.addProperty("webview:scale", this.mProvider.getScale());
        encoder.addProperty("webview:title", this.mProvider.getTitle());
        encoder.addProperty("webview:url", this.mProvider.getUrl());
        encoder.addProperty("webview:originalUrl", this.mProvider.getOriginalUrl());
    }
}
