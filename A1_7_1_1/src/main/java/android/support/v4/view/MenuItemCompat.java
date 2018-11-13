package android.support.v4.view;

import android.support.v4.internal.view.SupportMenuItem;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class MenuItemCompat {
    static final MenuVersionImpl IMPL = null;
    public static final int SHOW_AS_ACTION_ALWAYS = 2;
    public static final int SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW = 8;
    public static final int SHOW_AS_ACTION_IF_ROOM = 1;
    public static final int SHOW_AS_ACTION_NEVER = 0;
    public static final int SHOW_AS_ACTION_WITH_TEXT = 4;
    private static final String TAG = "MenuItemCompat";

    interface MenuVersionImpl {
        boolean collapseActionView(MenuItem menuItem);

        boolean expandActionView(MenuItem menuItem);

        View getActionView(MenuItem menuItem);

        boolean isActionViewExpanded(MenuItem menuItem);

        MenuItem setActionView(MenuItem menuItem, int i);

        MenuItem setActionView(MenuItem menuItem, View view);

        MenuItem setOnActionExpandListener(MenuItem menuItem, OnActionExpandListener onActionExpandListener);

        void setShowAsAction(MenuItem menuItem, int i);
    }

    static class BaseMenuVersionImpl implements MenuVersionImpl {
        BaseMenuVersionImpl() {
        }

        public void setShowAsAction(MenuItem item, int actionEnum) {
        }

        public MenuItem setActionView(MenuItem item, View view) {
            return item;
        }

        public MenuItem setActionView(MenuItem item, int resId) {
            return item;
        }

        public View getActionView(MenuItem item) {
            return null;
        }

        public boolean expandActionView(MenuItem item) {
            return false;
        }

        public boolean collapseActionView(MenuItem item) {
            return false;
        }

        public boolean isActionViewExpanded(MenuItem item) {
            return false;
        }

        public MenuItem setOnActionExpandListener(MenuItem item, OnActionExpandListener listener) {
            return item;
        }
    }

    static class HoneycombMenuVersionImpl implements MenuVersionImpl {
        HoneycombMenuVersionImpl() {
        }

        public void setShowAsAction(MenuItem item, int actionEnum) {
            MenuItemCompatHoneycomb.setShowAsAction(item, actionEnum);
        }

        public MenuItem setActionView(MenuItem item, View view) {
            return MenuItemCompatHoneycomb.setActionView(item, view);
        }

        public MenuItem setActionView(MenuItem item, int resId) {
            return MenuItemCompatHoneycomb.setActionView(item, resId);
        }

        public View getActionView(MenuItem item) {
            return MenuItemCompatHoneycomb.getActionView(item);
        }

        public boolean expandActionView(MenuItem item) {
            return false;
        }

        public boolean collapseActionView(MenuItem item) {
            return false;
        }

        public boolean isActionViewExpanded(MenuItem item) {
            return false;
        }

        public MenuItem setOnActionExpandListener(MenuItem item, OnActionExpandListener listener) {
            return item;
        }
    }

    static class IcsMenuVersionImpl extends HoneycombMenuVersionImpl {
        IcsMenuVersionImpl() {
        }

        public boolean expandActionView(MenuItem item) {
            return MenuItemCompatIcs.expandActionView(item);
        }

        public boolean collapseActionView(MenuItem item) {
            return MenuItemCompatIcs.collapseActionView(item);
        }

        public boolean isActionViewExpanded(MenuItem item) {
            return MenuItemCompatIcs.isActionViewExpanded(item);
        }

        public MenuItem setOnActionExpandListener(MenuItem item, final OnActionExpandListener listener) {
            if (listener == null) {
                return MenuItemCompatIcs.setOnActionExpandListener(item, null);
            }
            return MenuItemCompatIcs.setOnActionExpandListener(item, new SupportActionExpandProxy() {
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return listener.onMenuItemActionExpand(item);
                }

                public boolean onMenuItemActionCollapse(MenuItem item) {
                    return listener.onMenuItemActionCollapse(item);
                }
            });
        }
    }

    public interface OnActionExpandListener {
        boolean onMenuItemActionCollapse(MenuItem menuItem);

        boolean onMenuItemActionExpand(MenuItem menuItem);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.support.v4.view.MenuItemCompat.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.support.v4.view.MenuItemCompat.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.view.MenuItemCompat.<clinit>():void");
    }

    public static void setShowAsAction(MenuItem item, int actionEnum) {
        if (item instanceof SupportMenuItem) {
            ((SupportMenuItem) item).setShowAsAction(actionEnum);
        } else {
            IMPL.setShowAsAction(item, actionEnum);
        }
    }

    public static MenuItem setActionView(MenuItem item, View view) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).setActionView(view);
        }
        return IMPL.setActionView(item, view);
    }

    public static MenuItem setActionView(MenuItem item, int resId) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).setActionView(resId);
        }
        return IMPL.setActionView(item, resId);
    }

    public static View getActionView(MenuItem item) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).getActionView();
        }
        return IMPL.getActionView(item);
    }

    public static MenuItem setActionProvider(MenuItem item, ActionProvider provider) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).setSupportActionProvider(provider);
        }
        Log.w(TAG, "setActionProvider: item does not implement SupportMenuItem; ignoring");
        return item;
    }

    public static ActionProvider getActionProvider(MenuItem item) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).getSupportActionProvider();
        }
        Log.w(TAG, "getActionProvider: item does not implement SupportMenuItem; returning null");
        return null;
    }

    public static boolean expandActionView(MenuItem item) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).expandActionView();
        }
        return IMPL.expandActionView(item);
    }

    public static boolean collapseActionView(MenuItem item) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).collapseActionView();
        }
        return IMPL.collapseActionView(item);
    }

    public static boolean isActionViewExpanded(MenuItem item) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).isActionViewExpanded();
        }
        return IMPL.isActionViewExpanded(item);
    }

    public static MenuItem setOnActionExpandListener(MenuItem item, OnActionExpandListener listener) {
        if (item instanceof SupportMenuItem) {
            return ((SupportMenuItem) item).setSupportOnActionExpandListener(listener);
        }
        return IMPL.setOnActionExpandListener(item, listener);
    }
}
