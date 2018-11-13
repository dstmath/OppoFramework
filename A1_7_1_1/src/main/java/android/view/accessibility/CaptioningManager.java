package android.view.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Locale;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class CaptioningManager {
    private static final int DEFAULT_ENABLED = 0;
    private static final float DEFAULT_FONT_SCALE = 1.0f;
    private static final int DEFAULT_PRESET = 0;
    private final ContentObserver mContentObserver;
    private final ContentResolver mContentResolver;
    private final ArrayList<CaptioningChangeListener> mListeners;
    private final Runnable mStyleChangedRunnable;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static final class CaptionStyle {
        private static final CaptionStyle BLACK_ON_WHITE = null;
        private static final int COLOR_NONE_OPAQUE = 255;
        public static final int COLOR_UNSPECIFIED = 16777215;
        public static final CaptionStyle DEFAULT = null;
        private static final CaptionStyle DEFAULT_CUSTOM = null;
        public static final int EDGE_TYPE_DEPRESSED = 4;
        public static final int EDGE_TYPE_DROP_SHADOW = 2;
        public static final int EDGE_TYPE_NONE = 0;
        public static final int EDGE_TYPE_OUTLINE = 1;
        public static final int EDGE_TYPE_RAISED = 3;
        public static final int EDGE_TYPE_UNSPECIFIED = -1;
        public static final CaptionStyle[] PRESETS = null;
        public static final int PRESET_CUSTOM = -1;
        private static final CaptionStyle UNSPECIFIED = null;
        private static final CaptionStyle WHITE_ON_BLACK = null;
        private static final CaptionStyle YELLOW_ON_BLACK = null;
        private static final CaptionStyle YELLOW_ON_BLUE = null;
        public final int backgroundColor;
        public final int edgeColor;
        public final int edgeType;
        public final int foregroundColor;
        private final boolean mHasBackgroundColor;
        private final boolean mHasEdgeColor;
        private final boolean mHasEdgeType;
        private final boolean mHasForegroundColor;
        private final boolean mHasWindowColor;
        private Typeface mParsedTypeface;
        public final String mRawTypeface;
        public final int windowColor;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.accessibility.CaptioningManager.CaptionStyle.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.accessibility.CaptioningManager.CaptionStyle.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.CaptioningManager.CaptionStyle.<clinit>():void");
        }

        private CaptionStyle(int foregroundColor, int backgroundColor, int edgeType, int edgeColor, int windowColor, String rawTypeface) {
            boolean z;
            this.mHasForegroundColor = hasColor(foregroundColor);
            this.mHasBackgroundColor = hasColor(backgroundColor);
            if (edgeType != -1) {
                z = true;
            } else {
                z = false;
            }
            this.mHasEdgeType = z;
            this.mHasEdgeColor = hasColor(edgeColor);
            this.mHasWindowColor = hasColor(windowColor);
            if (!this.mHasForegroundColor) {
                foregroundColor = -1;
            }
            this.foregroundColor = foregroundColor;
            if (!this.mHasBackgroundColor) {
                backgroundColor = -16777216;
            }
            this.backgroundColor = backgroundColor;
            if (!this.mHasEdgeType) {
                edgeType = 0;
            }
            this.edgeType = edgeType;
            if (!this.mHasEdgeColor) {
                edgeColor = -16777216;
            }
            this.edgeColor = edgeColor;
            if (!this.mHasWindowColor) {
                windowColor = 255;
            }
            this.windowColor = windowColor;
            this.mRawTypeface = rawTypeface;
        }

        public static boolean hasColor(int packedColor) {
            return (packedColor >>> 24) != 0 || (16776960 & packedColor) == 0;
        }

        public CaptionStyle applyStyle(CaptionStyle overlay) {
            return new CaptionStyle(overlay.hasForegroundColor() ? overlay.foregroundColor : this.foregroundColor, overlay.hasBackgroundColor() ? overlay.backgroundColor : this.backgroundColor, overlay.hasEdgeType() ? overlay.edgeType : this.edgeType, overlay.hasEdgeColor() ? overlay.edgeColor : this.edgeColor, overlay.hasWindowColor() ? overlay.windowColor : this.windowColor, overlay.mRawTypeface != null ? overlay.mRawTypeface : this.mRawTypeface);
        }

        public boolean hasBackgroundColor() {
            return this.mHasBackgroundColor;
        }

        public boolean hasForegroundColor() {
            return this.mHasForegroundColor;
        }

        public boolean hasEdgeType() {
            return this.mHasEdgeType;
        }

        public boolean hasEdgeColor() {
            return this.mHasEdgeColor;
        }

        public boolean hasWindowColor() {
            return this.mHasWindowColor;
        }

        public Typeface getTypeface() {
            if (this.mParsedTypeface == null && !TextUtils.isEmpty(this.mRawTypeface)) {
                this.mParsedTypeface = Typeface.create(this.mRawTypeface, 0);
            }
            return this.mParsedTypeface;
        }

        public static CaptionStyle getCustomStyle(ContentResolver cr) {
            CaptionStyle defStyle = DEFAULT_CUSTOM;
            int foregroundColor = Secure.getInt(cr, "accessibility_captioning_foreground_color", defStyle.foregroundColor);
            int backgroundColor = Secure.getInt(cr, "accessibility_captioning_background_color", defStyle.backgroundColor);
            int edgeType = Secure.getInt(cr, "accessibility_captioning_edge_type", defStyle.edgeType);
            int edgeColor = Secure.getInt(cr, "accessibility_captioning_edge_color", defStyle.edgeColor);
            int windowColor = Secure.getInt(cr, "accessibility_captioning_window_color", defStyle.windowColor);
            String rawTypeface = Secure.getString(cr, "accessibility_captioning_typeface");
            if (rawTypeface == null) {
                rawTypeface = defStyle.mRawTypeface;
            }
            return new CaptionStyle(foregroundColor, backgroundColor, edgeType, edgeColor, windowColor, rawTypeface);
        }
    }

    public static abstract class CaptioningChangeListener {
        public void onEnabledChanged(boolean enabled) {
        }

        public void onUserStyleChanged(CaptionStyle userStyle) {
        }

        public void onLocaleChanged(Locale locale) {
        }

        public void onFontScaleChanged(float fontScale) {
        }
    }

    private class MyContentObserver extends ContentObserver {
        private final Handler mHandler;

        public MyContentObserver(Handler handler) {
            super(handler);
            this.mHandler = handler;
        }

        public void onChange(boolean selfChange, Uri uri) {
            String uriPath = uri.getPath();
            String name = uriPath.substring(uriPath.lastIndexOf(47) + 1);
            if ("accessibility_captioning_enabled".equals(name)) {
                CaptioningManager.this.notifyEnabledChanged();
            } else if ("accessibility_captioning_locale".equals(name)) {
                CaptioningManager.this.notifyLocaleChanged();
            } else if ("accessibility_captioning_font_scale".equals(name)) {
                CaptioningManager.this.notifyFontScaleChanged();
            } else {
                this.mHandler.removeCallbacks(CaptioningManager.this.mStyleChangedRunnable);
                this.mHandler.post(CaptioningManager.this.mStyleChangedRunnable);
            }
        }
    }

    public CaptioningManager(Context context) {
        this.mListeners = new ArrayList();
        this.mStyleChangedRunnable = new Runnable() {
            public void run() {
                CaptioningManager.this.notifyUserStyleChanged();
            }
        };
        this.mContentResolver = context.getContentResolver();
        this.mContentObserver = new MyContentObserver(new Handler(context.getMainLooper()));
    }

    public final boolean isEnabled() {
        if (Secure.getInt(this.mContentResolver, "accessibility_captioning_enabled", 0) == 1) {
            return true;
        }
        return false;
    }

    public final String getRawLocale() {
        return Secure.getString(this.mContentResolver, "accessibility_captioning_locale");
    }

    public final Locale getLocale() {
        String rawLocale = getRawLocale();
        if (!TextUtils.isEmpty(rawLocale)) {
            String[] splitLocale = rawLocale.split("_");
            switch (splitLocale.length) {
                case 1:
                    return new Locale(splitLocale[0]);
                case 2:
                    return new Locale(splitLocale[0], splitLocale[1]);
                case 3:
                    return new Locale(splitLocale[0], splitLocale[1], splitLocale[2]);
            }
        }
        return null;
    }

    public final float getFontScale() {
        return Secure.getFloat(this.mContentResolver, "accessibility_captioning_font_scale", 1.0f);
    }

    public int getRawUserStyle() {
        return Secure.getInt(this.mContentResolver, "accessibility_captioning_preset", 0);
    }

    public CaptionStyle getUserStyle() {
        int preset = getRawUserStyle();
        if (preset == -1) {
            return CaptionStyle.getCustomStyle(this.mContentResolver);
        }
        return CaptionStyle.PRESETS[preset];
    }

    public void addCaptioningChangeListener(CaptioningChangeListener listener) {
        synchronized (this.mListeners) {
            if (this.mListeners.isEmpty()) {
                registerObserver("accessibility_captioning_enabled");
                registerObserver("accessibility_captioning_foreground_color");
                registerObserver("accessibility_captioning_background_color");
                registerObserver("accessibility_captioning_window_color");
                registerObserver("accessibility_captioning_edge_type");
                registerObserver("accessibility_captioning_edge_color");
                registerObserver("accessibility_captioning_typeface");
                registerObserver("accessibility_captioning_font_scale");
                registerObserver("accessibility_captioning_locale");
                registerObserver("accessibility_captioning_preset");
            }
            this.mListeners.add(listener);
        }
    }

    private void registerObserver(String key) {
        this.mContentResolver.registerContentObserver(Secure.getUriFor(key), false, this.mContentObserver);
    }

    public void removeCaptioningChangeListener(CaptioningChangeListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
            if (this.mListeners.isEmpty()) {
                this.mContentResolver.unregisterContentObserver(this.mContentObserver);
            }
        }
    }

    private void notifyEnabledChanged() {
        boolean enabled = isEnabled();
        synchronized (this.mListeners) {
            for (CaptioningChangeListener listener : this.mListeners) {
                listener.onEnabledChanged(enabled);
            }
        }
    }

    private void notifyUserStyleChanged() {
        CaptionStyle userStyle = getUserStyle();
        synchronized (this.mListeners) {
            for (CaptioningChangeListener listener : this.mListeners) {
                listener.onUserStyleChanged(userStyle);
            }
        }
    }

    private void notifyLocaleChanged() {
        Locale locale = getLocale();
        synchronized (this.mListeners) {
            for (CaptioningChangeListener listener : this.mListeners) {
                listener.onLocaleChanged(locale);
            }
        }
    }

    private void notifyFontScaleChanged() {
        float fontScale = getFontScale();
        synchronized (this.mListeners) {
            for (CaptioningChangeListener listener : this.mListeners) {
                listener.onFontScaleChanged(fontScale);
            }
        }
    }
}
