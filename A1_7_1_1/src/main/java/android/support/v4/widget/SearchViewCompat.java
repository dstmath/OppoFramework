package android.support.v4.widget;

import android.content.ComponentName;
import android.content.Context;
import android.view.View;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SearchViewCompat {
    private static final SearchViewCompatImpl IMPL = null;

    public static abstract class OnCloseListenerCompat {
        final Object mListener = SearchViewCompat.IMPL.newOnCloseListener(this);

        public boolean onClose() {
            return false;
        }
    }

    public static abstract class OnQueryTextListenerCompat {
        final Object mListener = SearchViewCompat.IMPL.newOnQueryTextListener(this);

        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    interface SearchViewCompatImpl {
        CharSequence getQuery(View view);

        boolean isIconified(View view);

        boolean isQueryRefinementEnabled(View view);

        boolean isSubmitButtonEnabled(View view);

        Object newOnCloseListener(OnCloseListenerCompat onCloseListenerCompat);

        Object newOnQueryTextListener(OnQueryTextListenerCompat onQueryTextListenerCompat);

        View newSearchView(Context context);

        void setIconified(View view, boolean z);

        void setImeOptions(View view, int i);

        void setInputType(View view, int i);

        void setMaxWidth(View view, int i);

        void setOnCloseListener(Object obj, Object obj2);

        void setOnQueryTextListener(Object obj, Object obj2);

        void setQuery(View view, CharSequence charSequence, boolean z);

        void setQueryHint(View view, CharSequence charSequence);

        void setQueryRefinementEnabled(View view, boolean z);

        void setSearchableInfo(View view, ComponentName componentName);

        void setSubmitButtonEnabled(View view, boolean z);
    }

    static class SearchViewCompatStubImpl implements SearchViewCompatImpl {
        SearchViewCompatStubImpl() {
        }

        public View newSearchView(Context context) {
            return null;
        }

        public void setSearchableInfo(View searchView, ComponentName searchableComponent) {
        }

        public void setImeOptions(View searchView, int imeOptions) {
        }

        public void setInputType(View searchView, int inputType) {
        }

        public Object newOnQueryTextListener(OnQueryTextListenerCompat listener) {
            return null;
        }

        public void setOnQueryTextListener(Object searchView, Object listener) {
        }

        public Object newOnCloseListener(OnCloseListenerCompat listener) {
            return null;
        }

        public void setOnCloseListener(Object searchView, Object listener) {
        }

        public CharSequence getQuery(View searchView) {
            return null;
        }

        public void setQuery(View searchView, CharSequence query, boolean submit) {
        }

        public void setQueryHint(View searchView, CharSequence hint) {
        }

        public void setIconified(View searchView, boolean iconify) {
        }

        public boolean isIconified(View searchView) {
            return true;
        }

        public void setSubmitButtonEnabled(View searchView, boolean enabled) {
        }

        public boolean isSubmitButtonEnabled(View searchView) {
            return false;
        }

        public void setQueryRefinementEnabled(View searchView, boolean enable) {
        }

        public boolean isQueryRefinementEnabled(View searchView) {
            return false;
        }

        public void setMaxWidth(View searchView, int maxpixels) {
        }
    }

    static class SearchViewCompatHoneycombImpl extends SearchViewCompatStubImpl {
        SearchViewCompatHoneycombImpl() {
        }

        public View newSearchView(Context context) {
            return SearchViewCompatHoneycomb.newSearchView(context);
        }

        public void setSearchableInfo(View searchView, ComponentName searchableComponent) {
            SearchViewCompatHoneycomb.setSearchableInfo(searchView, searchableComponent);
        }

        public Object newOnQueryTextListener(final OnQueryTextListenerCompat listener) {
            return SearchViewCompatHoneycomb.newOnQueryTextListener(new OnQueryTextListenerCompatBridge() {
                public boolean onQueryTextSubmit(String query) {
                    return listener.onQueryTextSubmit(query);
                }

                public boolean onQueryTextChange(String newText) {
                    return listener.onQueryTextChange(newText);
                }
            });
        }

        public void setOnQueryTextListener(Object searchView, Object listener) {
            SearchViewCompatHoneycomb.setOnQueryTextListener(searchView, listener);
        }

        public Object newOnCloseListener(final OnCloseListenerCompat listener) {
            return SearchViewCompatHoneycomb.newOnCloseListener(new OnCloseListenerCompatBridge() {
                public boolean onClose() {
                    return listener.onClose();
                }
            });
        }

        public void setOnCloseListener(Object searchView, Object listener) {
            SearchViewCompatHoneycomb.setOnCloseListener(searchView, listener);
        }

        public CharSequence getQuery(View searchView) {
            return SearchViewCompatHoneycomb.getQuery(searchView);
        }

        public void setQuery(View searchView, CharSequence query, boolean submit) {
            SearchViewCompatHoneycomb.setQuery(searchView, query, submit);
        }

        public void setQueryHint(View searchView, CharSequence hint) {
            SearchViewCompatHoneycomb.setQueryHint(searchView, hint);
        }

        public void setIconified(View searchView, boolean iconify) {
            SearchViewCompatHoneycomb.setIconified(searchView, iconify);
        }

        public boolean isIconified(View searchView) {
            return SearchViewCompatHoneycomb.isIconified(searchView);
        }

        public void setSubmitButtonEnabled(View searchView, boolean enabled) {
            SearchViewCompatHoneycomb.setSubmitButtonEnabled(searchView, enabled);
        }

        public boolean isSubmitButtonEnabled(View searchView) {
            return SearchViewCompatHoneycomb.isSubmitButtonEnabled(searchView);
        }

        public void setQueryRefinementEnabled(View searchView, boolean enable) {
            SearchViewCompatHoneycomb.setQueryRefinementEnabled(searchView, enable);
        }

        public boolean isQueryRefinementEnabled(View searchView) {
            return SearchViewCompatHoneycomb.isQueryRefinementEnabled(searchView);
        }

        public void setMaxWidth(View searchView, int maxpixels) {
            SearchViewCompatHoneycomb.setMaxWidth(searchView, maxpixels);
        }
    }

    static class SearchViewCompatIcsImpl extends SearchViewCompatHoneycombImpl {
        SearchViewCompatIcsImpl() {
        }

        public View newSearchView(Context context) {
            return SearchViewCompatIcs.newSearchView(context);
        }

        public void setImeOptions(View searchView, int imeOptions) {
            SearchViewCompatIcs.setImeOptions(searchView, imeOptions);
        }

        public void setInputType(View searchView, int inputType) {
            SearchViewCompatIcs.setInputType(searchView, inputType);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.support.v4.widget.SearchViewCompat.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.support.v4.widget.SearchViewCompat.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.widget.SearchViewCompat.<clinit>():void");
    }

    private SearchViewCompat(Context context) {
    }

    public static View newSearchView(Context context) {
        return IMPL.newSearchView(context);
    }

    public static void setSearchableInfo(View searchView, ComponentName searchableComponent) {
        IMPL.setSearchableInfo(searchView, searchableComponent);
    }

    public static void setImeOptions(View searchView, int imeOptions) {
        IMPL.setImeOptions(searchView, imeOptions);
    }

    public static void setInputType(View searchView, int inputType) {
        IMPL.setInputType(searchView, inputType);
    }

    public static void setOnQueryTextListener(View searchView, OnQueryTextListenerCompat listener) {
        IMPL.setOnQueryTextListener(searchView, listener.mListener);
    }

    public static void setOnCloseListener(View searchView, OnCloseListenerCompat listener) {
        IMPL.setOnCloseListener(searchView, listener.mListener);
    }

    public static CharSequence getQuery(View searchView) {
        return IMPL.getQuery(searchView);
    }

    public static void setQuery(View searchView, CharSequence query, boolean submit) {
        IMPL.setQuery(searchView, query, submit);
    }

    public static void setQueryHint(View searchView, CharSequence hint) {
        IMPL.setQueryHint(searchView, hint);
    }

    public static void setIconified(View searchView, boolean iconify) {
        IMPL.setIconified(searchView, iconify);
    }

    public static boolean isIconified(View searchView) {
        return IMPL.isIconified(searchView);
    }

    public static void setSubmitButtonEnabled(View searchView, boolean enabled) {
        IMPL.setSubmitButtonEnabled(searchView, enabled);
    }

    public static boolean isSubmitButtonEnabled(View searchView) {
        return IMPL.isSubmitButtonEnabled(searchView);
    }

    public static void setQueryRefinementEnabled(View searchView, boolean enable) {
        IMPL.setQueryRefinementEnabled(searchView, enable);
    }

    public static boolean isQueryRefinementEnabled(View searchView) {
        return IMPL.isQueryRefinementEnabled(searchView);
    }

    public static void setMaxWidth(View searchView, int maxpixels) {
        IMPL.setMaxWidth(searchView, maxpixels);
    }
}
