package android.view;

import android.graphics.Rect;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import com.color.screenshot.ColorLongshotComponent;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.IColorLongshotViewCallback;
import com.color.view.analysis.ColorWindowNode;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.json.JSONObject;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ColorLongshotViewHelper implements IColorLongshotWindow {
    private static final ContentComparator CONTENT_COMPARATOR = null;
    private static final boolean DBG = true;
    private static final boolean FEATURE_ADJUST_WINDOW = false;
    private static final boolean FEATURE_DUMP_MIN_SIZE = false;
    private static final String JSON_CHILD_HASH = "child_hash";
    private static final String JSON_CHILD_LIST = "child_list";
    private static final String JSON_CHILD_RECT_CLIP = "child_rect_clip";
    private static final String JSON_CHILD_RECT_FULL = "child_rect_full";
    private static final String JSON_CHILD_SCROLLY = "child_scrollY";
    private static final String JSON_FLOAT_LIST = "float_list";
    private static final String JSON_FLOAT_RECT = "float_rect";
    private static final String JSON_PARENT_HASH = "parent_hash";
    private static final String JSON_PARENT_RECT_CLIP = "parent_rect_clip";
    private static final String JSON_PARENT_RECT_FULL = "parent_rect_full";
    private static final String JSON_SCROLL_CHILD = "scroll_child";
    private static final String JSON_SCROLL_LIST = "scroll_list";
    private static final String JSON_SCROLL_RECT = "scroll_rect";
    private static final String JSON_SIDE_LIST = "side_list";
    private static final String JSON_SIDE_RECT = "side_rect";
    private static final String JSON_WINDOW_LAYER = "window_layer";
    private static final String JSON_WINDOW_LIST = "window_list";
    private static final String JSON_WINDOW_NAVIBAR = "window_navibar";
    private static final String JSON_WINDOW_RECT_DECOR = "window_rect_decor";
    private static final String JSON_WINDOW_RECT_VISIBLE = "window_rect_visible";
    private static final String JSON_WINDOW_STATBAR = "window_statbar";
    private static final ColorLongshotComponent[] SKIP_COLLECT_ROOTS = null;
    private int mCoverHeight;
    private int mDumpCount;
    private final List<Rect> mFloatRects;
    private final H mHandler;
    private int mMinListHeight;
    private int mMinScrollDistance;
    private int mMinScrollHeight;
    private int mScreenHeight;
    private int mScreenWidght;
    private final List<ViewNode> mScrollNodes;
    private final List<Rect> mSideRects;
    private final List<View> mSmallViews;
    protected final Class<?> mTagClass;
    private final Rect mTempRect1;
    private final Rect mTempRect2;
    private final WeakReference<ViewRootImpl> mViewAncestor;

    private static class ContentComparator implements Comparator<ColorLongshotViewContent> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.ContentComparator.<init>():void, dex: 
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
        private ContentComparator() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.ContentComparator.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ContentComparator.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.ContentComparator.<init>(android.view.ColorLongshotViewHelper$ContentComparator):void, dex: 
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
        /* synthetic */ ContentComparator(android.view.ColorLongshotViewHelper.ContentComparator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.ContentComparator.<init>(android.view.ColorLongshotViewHelper$ContentComparator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ContentComparator.<init>(android.view.ColorLongshotViewHelper$ContentComparator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.view.ColorLongshotViewHelper.ContentComparator.rectCompare(android.graphics.Rect, android.graphics.Rect):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private int rectCompare(android.graphics.Rect r1, android.graphics.Rect r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.view.ColorLongshotViewHelper.ContentComparator.rectCompare(android.graphics.Rect, android.graphics.Rect):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ContentComparator.rectCompare(android.graphics.Rect, android.graphics.Rect):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.ContentComparator.compare(android.view.ColorLongshotViewContent, android.view.ColorLongshotViewContent):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public int compare(android.view.ColorLongshotViewContent r1, android.view.ColorLongshotViewContent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.ContentComparator.compare(android.view.ColorLongshotViewContent, android.view.ColorLongshotViewContent):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ContentComparator.compare(android.view.ColorLongshotViewContent, android.view.ColorLongshotViewContent):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.ContentComparator.compare(java.lang.Object, java.lang.Object):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public /* bridge */ /* synthetic */ int compare(java.lang.Object r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.ContentComparator.compare(java.lang.Object, java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ContentComparator.compare(java.lang.Object, java.lang.Object):int");
        }
    }

    private static final class DumpInfoData {
        private final List<ColorWindowNode> mFloatWindows;
        private final ParcelFileDescriptor mParcelFileDescriptor;
        private final List<ColorWindowNode> mSystemWindows;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.view.ColorLongshotViewHelper.DumpInfoData.<init>(android.os.ParcelFileDescriptor, java.util.List, java.util.List):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public DumpInfoData(android.os.ParcelFileDescriptor r1, java.util.List<com.color.view.analysis.ColorWindowNode> r2, java.util.List<com.color.view.analysis.ColorWindowNode> r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.view.ColorLongshotViewHelper.DumpInfoData.<init>(android.os.ParcelFileDescriptor, java.util.List, java.util.List):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.DumpInfoData.<init>(android.os.ParcelFileDescriptor, java.util.List, java.util.List):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.DumpInfoData.getFloatWindows():java.util.List<com.color.view.analysis.ColorWindowNode>, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.util.List<com.color.view.analysis.ColorWindowNode> getFloatWindows() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.DumpInfoData.getFloatWindows():java.util.List<com.color.view.analysis.ColorWindowNode>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.DumpInfoData.getFloatWindows():java.util.List<com.color.view.analysis.ColorWindowNode>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.DumpInfoData.getParcelFileDescriptor():android.os.ParcelFileDescriptor, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public android.os.ParcelFileDescriptor getParcelFileDescriptor() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.DumpInfoData.getParcelFileDescriptor():android.os.ParcelFileDescriptor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.DumpInfoData.getParcelFileDescriptor():android.os.ParcelFileDescriptor");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.DumpInfoData.getSystemWindows():java.util.List<com.color.view.analysis.ColorWindowNode>, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.util.List<com.color.view.analysis.ColorWindowNode> getSystemWindows() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.DumpInfoData.getSystemWindows():java.util.List<com.color.view.analysis.ColorWindowNode>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.DumpInfoData.getSystemWindows():java.util.List<com.color.view.analysis.ColorWindowNode>");
        }
    }

    private class H extends Handler {
        public static final int MSG_DUMP_VIEW_HIERARCHY = 1;
        public static final int MSG_GET_VIEWINFO = 0;
        private final WeakReference<ViewRootImpl> mViewAncestor;
        final /* synthetic */ ColorLongshotViewHelper this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.view.ColorLongshotViewHelper.H.<init>(android.view.ColorLongshotViewHelper, java.lang.ref.WeakReference):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public H(android.view.ColorLongshotViewHelper r1, java.lang.ref.WeakReference<android.view.ViewRootImpl> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.view.ColorLongshotViewHelper.H.<init>(android.view.ColorLongshotViewHelper, java.lang.ref.WeakReference):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.H.<init>(android.view.ColorLongshotViewHelper, java.lang.ref.WeakReference):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.view.ColorLongshotViewHelper.H.handleMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.view.ColorLongshotViewHelper.H.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.H.handleMessage(android.os.Message):void");
        }
    }

    private static class ViewInfoData {
        private final IColorLongshotViewCallback mCallback;
        private final ColorLongshotViewInfo mViewInfo;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.view.ColorLongshotViewHelper.ViewInfoData.<init>(com.color.screenshot.ColorLongshotViewInfo, com.color.screenshot.IColorLongshotViewCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public ViewInfoData(com.color.screenshot.ColorLongshotViewInfo r1, com.color.screenshot.IColorLongshotViewCallback r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.view.ColorLongshotViewHelper.ViewInfoData.<init>(com.color.screenshot.ColorLongshotViewInfo, com.color.screenshot.IColorLongshotViewCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewInfoData.<init>(com.color.screenshot.ColorLongshotViewInfo, com.color.screenshot.IColorLongshotViewCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewInfoData.getCallback():com.color.screenshot.IColorLongshotViewCallback, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public com.color.screenshot.IColorLongshotViewCallback getCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewInfoData.getCallback():com.color.screenshot.IColorLongshotViewCallback, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewInfoData.getCallback():com.color.screenshot.IColorLongshotViewCallback");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewInfoData.getViewInfo():com.color.screenshot.ColorLongshotViewInfo, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public com.color.screenshot.ColorLongshotViewInfo getViewInfo() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewInfoData.getViewInfo():com.color.screenshot.ColorLongshotViewInfo, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewInfoData.getViewInfo():com.color.screenshot.ColorLongshotViewInfo");
        }
    }

    private static final class ViewNode {
        private final CharSequence mAccessibilityName;
        private final List<ViewNode> mChildList;
        private final CharSequence mClassName;
        private final Rect mClipRect;
        private final Rect mFullRect;
        private final Rect mScrollRect;
        private long mSpend;
        private final View mView;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.view.ColorLongshotViewHelper.ViewNode.<init>(android.view.View, java.lang.CharSequence, android.graphics.Rect, android.graphics.Rect):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public ViewNode(android.view.View r1, java.lang.CharSequence r2, android.graphics.Rect r3, android.graphics.Rect r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.view.ColorLongshotViewHelper.ViewNode.<init>(android.view.View, java.lang.CharSequence, android.graphics.Rect, android.graphics.Rect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.<init>(android.view.View, java.lang.CharSequence, android.graphics.Rect, android.graphics.Rect):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.addChild(android.view.ColorLongshotViewHelper$ViewNode):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void addChild(android.view.ColorLongshotViewHelper.ViewNode r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.addChild(android.view.ColorLongshotViewHelper$ViewNode):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.addChild(android.view.ColorLongshotViewHelper$ViewNode):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getAccessibilityName():java.lang.CharSequence, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.CharSequence getAccessibilityName() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getAccessibilityName():java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.getAccessibilityName():java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getChildList():java.util.List<android.view.ColorLongshotViewHelper$ViewNode>, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.util.List<android.view.ColorLongshotViewHelper.ViewNode> getChildList() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getChildList():java.util.List<android.view.ColorLongshotViewHelper$ViewNode>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.getChildList():java.util.List<android.view.ColorLongshotViewHelper$ViewNode>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getClassName():java.lang.CharSequence, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.CharSequence getClassName() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getClassName():java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.getClassName():java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.view.ColorLongshotViewHelper.ViewNode.getClipRect():android.graphics.Rect, dex:  in method: android.view.ColorLongshotViewHelper.ViewNode.getClipRect():android.graphics.Rect, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.view.ColorLongshotViewHelper.ViewNode.getClipRect():android.graphics.Rect, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public android.graphics.Rect getClipRect() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.view.ColorLongshotViewHelper.ViewNode.getClipRect():android.graphics.Rect, dex:  in method: android.view.ColorLongshotViewHelper.ViewNode.getClipRect():android.graphics.Rect, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.getClipRect():android.graphics.Rect");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.view.ColorLongshotViewHelper.ViewNode.getFullRect():android.graphics.Rect, dex:  in method: android.view.ColorLongshotViewHelper.ViewNode.getFullRect():android.graphics.Rect, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.view.ColorLongshotViewHelper.ViewNode.getFullRect():android.graphics.Rect, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public android.graphics.Rect getFullRect() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.view.ColorLongshotViewHelper.ViewNode.getFullRect():android.graphics.Rect, dex:  in method: android.view.ColorLongshotViewHelper.ViewNode.getFullRect():android.graphics.Rect, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.getFullRect():android.graphics.Rect");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getScrollRect():android.graphics.Rect, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public android.graphics.Rect getScrollRect() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getScrollRect():android.graphics.Rect, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.getScrollRect():android.graphics.Rect");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getView():android.view.View, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public android.view.View getView() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.getView():android.view.View, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.getView():android.view.View");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.setScrollRect(android.graphics.Rect):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void setScrollRect(android.graphics.Rect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.ViewNode.setScrollRect(android.graphics.Rect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.setScrollRect(android.graphics.Rect):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: android.view.ColorLongshotViewHelper.ViewNode.setSpend(long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void setSpend(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: android.view.ColorLongshotViewHelper.ViewNode.setSpend(long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.setSpend(long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: d in method: android.view.ColorLongshotViewHelper.ViewNode.toString():java.lang.String, dex:  in method: android.view.ColorLongshotViewHelper.ViewNode.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: d in method: android.view.ColorLongshotViewHelper.ViewNode.toString():java.lang.String, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: d
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: d in method: android.view.ColorLongshotViewHelper.ViewNode.toString():java.lang.String, dex:  in method: android.view.ColorLongshotViewHelper.ViewNode.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.ViewNode.toString():java.lang.String");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.-get0(android.view.ColorLongshotViewHelper):java.util.List, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get0 */
    static /* synthetic */ java.util.List m81-get0(android.view.ColorLongshotViewHelper r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.-get0(android.view.ColorLongshotViewHelper):java.util.List, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-get0(android.view.ColorLongshotViewHelper):java.util.List");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.-get1(android.view.ColorLongshotViewHelper):java.util.List, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get1 */
    static /* synthetic */ java.util.List m82-get1(android.view.ColorLongshotViewHelper r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.-get1(android.view.ColorLongshotViewHelper):java.util.List, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-get1(android.view.ColorLongshotViewHelper):java.util.List");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.view.ColorLongshotViewHelper.-get2(android.view.ColorLongshotViewHelper):java.util.List, dex:  in method: android.view.ColorLongshotViewHelper.-get2(android.view.ColorLongshotViewHelper):java.util.List, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.view.ColorLongshotViewHelper.-get2(android.view.ColorLongshotViewHelper):java.util.List, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get2 */
    static /* synthetic */ java.util.List m83-get2(android.view.ColorLongshotViewHelper r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.view.ColorLongshotViewHelper.-get2(android.view.ColorLongshotViewHelper):java.util.List, dex:  in method: android.view.ColorLongshotViewHelper.-get2(android.view.ColorLongshotViewHelper):java.util.List, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-get2(android.view.ColorLongshotViewHelper):java.util.List");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.view.ColorLongshotViewHelper.-set0(android.view.ColorLongshotViewHelper, int):int, dex:  in method: android.view.ColorLongshotViewHelper.-set0(android.view.ColorLongshotViewHelper, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.view.ColorLongshotViewHelper.-set0(android.view.ColorLongshotViewHelper, int):int, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -set0 */
    static /* synthetic */ int m84-set0(android.view.ColorLongshotViewHelper r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.view.ColorLongshotViewHelper.-set0(android.view.ColorLongshotViewHelper, int):int, dex:  in method: android.view.ColorLongshotViewHelper.-set0(android.view.ColorLongshotViewHelper, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-set0(android.view.ColorLongshotViewHelper, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set1(android.view.ColorLongshotViewHelper, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set1 */
    static /* synthetic */ int m85-set1(android.view.ColorLongshotViewHelper r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set1(android.view.ColorLongshotViewHelper, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-set1(android.view.ColorLongshotViewHelper, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set2(android.view.ColorLongshotViewHelper, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set2 */
    static /* synthetic */ int m86-set2(android.view.ColorLongshotViewHelper r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set2(android.view.ColorLongshotViewHelper, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-set2(android.view.ColorLongshotViewHelper, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set3(android.view.ColorLongshotViewHelper, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set3 */
    static /* synthetic */ int m87-set3(android.view.ColorLongshotViewHelper r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set3(android.view.ColorLongshotViewHelper, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-set3(android.view.ColorLongshotViewHelper, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set4(android.view.ColorLongshotViewHelper, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set4 */
    static /* synthetic */ int m88-set4(android.view.ColorLongshotViewHelper r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set4(android.view.ColorLongshotViewHelper, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-set4(android.view.ColorLongshotViewHelper, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set5(android.view.ColorLongshotViewHelper, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set5 */
    static /* synthetic */ int m89-set5(android.view.ColorLongshotViewHelper r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set5(android.view.ColorLongshotViewHelper, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-set5(android.view.ColorLongshotViewHelper, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set6(android.view.ColorLongshotViewHelper, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set6 */
    static /* synthetic */ int m90-set6(android.view.ColorLongshotViewHelper r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.view.ColorLongshotViewHelper.-set6(android.view.ColorLongshotViewHelper, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-set6(android.view.ColorLongshotViewHelper, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.-wrap1(android.view.ColorLongshotViewHelper, com.color.screenshot.ColorLongshotDump, android.view.ColorLongshotViewUtils, java.util.List, java.util.List, java.util.List):void, dex: 
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
    /* renamed from: -wrap1 */
    static /* synthetic */ void m91-wrap1(android.view.ColorLongshotViewHelper r1, com.color.screenshot.ColorLongshotDump r2, android.view.ColorLongshotViewUtils r3, java.util.List r4, java.util.List r5, java.util.List r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.-wrap1(android.view.ColorLongshotViewHelper, com.color.screenshot.ColorLongshotDump, android.view.ColorLongshotViewUtils, java.util.List, java.util.List, java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-wrap1(android.view.ColorLongshotViewHelper, com.color.screenshot.ColorLongshotDump, android.view.ColorLongshotViewUtils, java.util.List, java.util.List, java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.-wrap2(android.view.ColorLongshotViewHelper, com.color.screenshot.ColorLongshotDump, android.view.View):void, dex: 
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
    /* renamed from: -wrap2 */
    static /* synthetic */ void m92-wrap2(android.view.ColorLongshotViewHelper r1, com.color.screenshot.ColorLongshotDump r2, android.view.View r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.-wrap2(android.view.ColorLongshotViewHelper, com.color.screenshot.ColorLongshotDump, android.view.View):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-wrap2(android.view.ColorLongshotViewHelper, com.color.screenshot.ColorLongshotDump, android.view.View):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.-wrap3(android.view.ColorLongshotViewHelper, java.util.List):void, dex: 
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
    /* renamed from: -wrap3 */
    static /* synthetic */ void m93-wrap3(android.view.ColorLongshotViewHelper r1, java.util.List r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.-wrap3(android.view.ColorLongshotViewHelper, java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.-wrap3(android.view.ColorLongshotViewHelper, java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.<init>(java.lang.ref.WeakReference):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public ColorLongshotViewHelper(java.lang.ref.WeakReference<android.view.ViewRootImpl> r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.<init>(java.lang.ref.WeakReference):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.<init>(java.lang.ref.WeakReference):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.calcScrollRect(com.color.screenshot.ColorLongshotDump, android.view.ColorLongshotViewUtils, android.view.ColorLongshotViewHelper$ViewNode, java.util.List, java.util.List):void, dex: 
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
    private void calcScrollRect(com.color.screenshot.ColorLongshotDump r1, android.view.ColorLongshotViewUtils r2, android.view.ColorLongshotViewHelper.ViewNode r3, java.util.List<com.color.view.analysis.ColorWindowNode> r4, java.util.List<com.color.view.analysis.ColorWindowNode> r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.calcScrollRect(com.color.screenshot.ColorLongshotDump, android.view.ColorLongshotViewUtils, android.view.ColorLongshotViewHelper$ViewNode, java.util.List, java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.calcScrollRect(com.color.screenshot.ColorLongshotDump, android.view.ColorLongshotViewUtils, android.view.ColorLongshotViewHelper$ViewNode, java.util.List, java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.view.ColorLongshotViewHelper.calcScrollRectForViews(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, android.view.View):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void calcScrollRectForViews(android.view.ColorLongshotViewUtils r1, android.graphics.Rect r2, android.graphics.Rect r3, android.view.View r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.view.ColorLongshotViewHelper.calcScrollRectForViews(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, android.view.View):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.calcScrollRectForViews(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, android.view.View):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.calcScrollRectForWindow(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, com.color.view.analysis.ColorWindowNode):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void calcScrollRectForWindow(android.view.ColorLongshotViewUtils r1, android.graphics.Rect r2, android.graphics.Rect r3, com.color.view.analysis.ColorWindowNode r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.calcScrollRectForWindow(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, com.color.view.analysis.ColorWindowNode):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.calcScrollRectForWindow(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, com.color.view.analysis.ColorWindowNode):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.calcScrollRectForWindows(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, java.util.List):void, dex: 
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
    private void calcScrollRectForWindows(android.view.ColorLongshotViewUtils r1, android.graphics.Rect r2, android.graphics.Rect r3, java.util.List<com.color.view.analysis.ColorWindowNode> r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.calcScrollRectForWindows(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.calcScrollRectForWindows(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.calcScrollRects(com.color.screenshot.ColorLongshotDump, android.view.ColorLongshotViewUtils, java.util.List, java.util.List, java.util.List):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void calcScrollRects(com.color.screenshot.ColorLongshotDump r1, android.view.ColorLongshotViewUtils r2, java.util.List<android.view.ColorLongshotViewHelper.ViewNode> r3, java.util.List<com.color.view.analysis.ColorWindowNode> r4, java.util.List<com.color.view.analysis.ColorWindowNode> r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.calcScrollRects(com.color.screenshot.ColorLongshotDump, android.view.ColorLongshotViewUtils, java.util.List, java.util.List, java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.calcScrollRects(com.color.screenshot.ColorLongshotDump, android.view.ColorLongshotViewUtils, java.util.List, java.util.List, java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.canScrollVertically(android.view.View):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean canScrollVertically(android.view.View r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.canScrollVertically(android.view.View):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.canScrollVertically(android.view.View):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.checkCoverContents(android.view.ColorLongshotViewUtils, java.util.List, android.graphics.Rect):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void checkCoverContents(android.view.ColorLongshotViewUtils r1, java.util.List<android.view.ColorLongshotViewContent> r2, android.graphics.Rect r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.checkCoverContents(android.view.ColorLongshotViewUtils, java.util.List, android.graphics.Rect):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.checkCoverContents(android.view.ColorLongshotViewUtils, java.util.List, android.graphics.Rect):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus element_width: 0ebd in method: android.view.ColorLongshotViewHelper.dumpScrollNodes(android.view.ColorLongshotViewHelper$ViewNode, android.view.View, android.graphics.Point, java.util.List, int):void, dex:  in method: android.view.ColorLongshotViewHelper.dumpScrollNodes(android.view.ColorLongshotViewHelper$ViewNode, android.view.View, android.graphics.Point, java.util.List, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus element_width: 0ebd in method: android.view.ColorLongshotViewHelper.dumpScrollNodes(android.view.ColorLongshotViewHelper$ViewNode, android.view.View, android.graphics.Point, java.util.List, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus element_width: 0ebd
        	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:871)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void dumpScrollNodes(android.view.ColorLongshotViewHelper.ViewNode r1, android.view.View r2, android.graphics.Point r3, java.util.List<android.view.View> r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus element_width: 0ebd in method: android.view.ColorLongshotViewHelper.dumpScrollNodes(android.view.ColorLongshotViewHelper$ViewNode, android.view.View, android.graphics.Point, java.util.List, int):void, dex:  in method: android.view.ColorLongshotViewHelper.dumpScrollNodes(android.view.ColorLongshotViewHelper$ViewNode, android.view.View, android.graphics.Point, java.util.List, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.dumpScrollNodes(android.view.ColorLongshotViewHelper$ViewNode, android.view.View, android.graphics.Point, java.util.List, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.dumpViewHierarchy(com.color.screenshot.ColorLongshotDump, android.view.View):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void dumpViewHierarchy(com.color.screenshot.ColorLongshotDump r1, android.view.View r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.dumpViewHierarchy(com.color.screenshot.ColorLongshotDump, android.view.View):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.dumpViewHierarchy(com.color.screenshot.ColorLongshotDump, android.view.View):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.floatRectsToJson(java.util.List):org.json.JSONArray, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private org.json.JSONArray floatRectsToJson(java.util.List<android.graphics.Rect> r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.floatRectsToJson(java.util.List):org.json.JSONArray, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.floatRectsToJson(java.util.List):org.json.JSONArray");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.getAccessibilityName(android.view.ColorLongshotViewHelper$ViewNode):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private java.lang.String getAccessibilityName(android.view.ColorLongshotViewHelper.ViewNode r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.getAccessibilityName(android.view.ColorLongshotViewHelper$ViewNode):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.getAccessibilityName(android.view.ColorLongshotViewHelper$ViewNode):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isInvalidIntersect(android.graphics.Rect, android.graphics.Rect):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean isInvalidIntersect(android.graphics.Rect r1, android.graphics.Rect r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isInvalidIntersect(android.graphics.Rect, android.graphics.Rect):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.isInvalidIntersect(android.graphics.Rect, android.graphics.Rect):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isLargeHeight(android.graphics.Rect, android.graphics.Rect):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean isLargeHeight(android.graphics.Rect r1, android.graphics.Rect r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isLargeHeight(android.graphics.Rect, android.graphics.Rect):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.isLargeHeight(android.graphics.Rect, android.graphics.Rect):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isScrollableView(android.view.View):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean isScrollableView(android.view.View r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isScrollableView(android.view.View):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.isScrollableView(android.view.View):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isSmallWidth(android.graphics.Rect, android.graphics.Rect):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean isSmallWidth(android.graphics.Rect r1, android.graphics.Rect r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isSmallWidth(android.graphics.Rect, android.graphics.Rect):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.isSmallWidth(android.graphics.Rect, android.graphics.Rect):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isValidScrollNode(android.view.View):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean isValidScrollNode(android.view.View r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.isValidScrollNode(android.view.View):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.isValidScrollNode(android.view.View):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.packJsonNode(com.color.screenshot.ColorLongshotDump, java.io.PrintWriter, java.util.List, java.util.List):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private java.lang.String packJsonNode(com.color.screenshot.ColorLongshotDump r1, java.io.PrintWriter r2, java.util.List<com.color.view.analysis.ColorWindowNode> r3, java.util.List<com.color.view.analysis.ColorWindowNode> r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.packJsonNode(com.color.screenshot.ColorLongshotDump, java.io.PrintWriter, java.util.List, java.util.List):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.packJsonNode(com.color.screenshot.ColorLongshotDump, java.io.PrintWriter, java.util.List, java.util.List):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.postLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo, com.color.screenshot.IColorLongshotViewCallback):void, dex: 
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
    private void postLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo r1, com.color.screenshot.IColorLongshotViewCallback r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.postLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo, com.color.screenshot.IColorLongshotViewCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.postLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo, com.color.screenshot.IColorLongshotViewCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.printContentView(android.view.ColorLongshotViewContent, java.lang.String, android.graphics.Rect):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void printContentView(android.view.ColorLongshotViewContent r1, java.lang.String r2, android.graphics.Rect r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.printContentView(android.view.ColorLongshotViewContent, java.lang.String, android.graphics.Rect):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.printContentView(android.view.ColorLongshotViewContent, java.lang.String, android.graphics.Rect):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.printMsg(java.lang.String):void, dex: 
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
    private void printMsg(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.printMsg(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.printMsg(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.printScrollNodes(java.lang.String, java.util.List):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void printScrollNodes(java.lang.String r1, java.util.List<android.view.ColorLongshotViewHelper.ViewNode> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.printScrollNodes(java.lang.String, java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.printScrollNodes(java.lang.String, java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.printSpend(java.lang.String, long):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void printSpend(java.lang.String r1, long r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.printSpend(java.lang.String, long):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.printSpend(java.lang.String, long):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.printTag(java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void printTag(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.printTag(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.printTag(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.reportDumpResult(android.content.Context, com.color.screenshot.ColorLongshotDump):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void reportDumpResult(android.content.Context r1, com.color.screenshot.ColorLongshotDump r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.reportDumpResult(android.content.Context, com.color.screenshot.ColorLongshotDump):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.reportDumpResult(android.content.Context, com.color.screenshot.ColorLongshotDump):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus element_width: 0a69 in method: android.view.ColorLongshotViewHelper.scrollNodesToJson(org.json.JSONArray, java.util.List, boolean):void, dex:  in method: android.view.ColorLongshotViewHelper.scrollNodesToJson(org.json.JSONArray, java.util.List, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus element_width: 0a69 in method: android.view.ColorLongshotViewHelper.scrollNodesToJson(org.json.JSONArray, java.util.List, boolean):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus element_width: 0a69
        	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:871)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void scrollNodesToJson(org.json.JSONArray r1, java.util.List<android.view.ColorLongshotViewHelper.ViewNode> r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus element_width: 0a69 in method: android.view.ColorLongshotViewHelper.scrollNodesToJson(org.json.JSONArray, java.util.List, boolean):void, dex:  in method: android.view.ColorLongshotViewHelper.scrollNodesToJson(org.json.JSONArray, java.util.List, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.scrollNodesToJson(org.json.JSONArray, java.util.List, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.selectScrollNodes(java.util.List):void, dex: 
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
    private void selectScrollNodes(java.util.List<android.view.ColorLongshotViewHelper.ViewNode> r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.selectScrollNodes(java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.selectScrollNodes(java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.sendMessage(int, java.lang.Object, int, int, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void sendMessage(int r1, java.lang.Object r2, int r3, int r4, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.sendMessage(int, java.lang.Object, int, int, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.sendMessage(int, java.lang.Object, int, int, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.sideRectsToJson(java.util.List):org.json.JSONArray, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private org.json.JSONArray sideRectsToJson(java.util.List<android.graphics.Rect> r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.sideRectsToJson(java.util.List):org.json.JSONArray, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.sideRectsToJson(java.util.List):org.json.JSONArray");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.skipCollectFloatWindow(android.view.View):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean skipCollectFloatWindow(android.view.View r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.skipCollectFloatWindow(android.view.View):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.skipCollectFloatWindow(android.view.View):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.view.ColorLongshotViewHelper.updateCoverRect(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean updateCoverRect(android.view.ColorLongshotViewUtils r1, android.graphics.Rect r2, android.graphics.Rect r3, android.graphics.Rect r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.view.ColorLongshotViewHelper.updateCoverRect(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.updateCoverRect(android.view.ColorLongshotViewUtils, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.windowNodesToJson(java.util.List, java.util.List):org.json.JSONArray, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private org.json.JSONArray windowNodesToJson(java.util.List<com.color.view.analysis.ColorWindowNode> r1, java.util.List<com.color.view.analysis.ColorWindowNode> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.windowNodesToJson(java.util.List, java.util.List):org.json.JSONArray, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.windowNodesToJson(java.util.List, java.util.List):org.json.JSONArray");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.getLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void getLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.getLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.getLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.getLongshotViewInfoAsync(com.color.screenshot.ColorLongshotViewInfo, com.color.screenshot.IColorLongshotViewCallback):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void getLongshotViewInfoAsync(com.color.screenshot.ColorLongshotViewInfo r1, com.color.screenshot.IColorLongshotViewCallback r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.getLongshotViewInfoAsync(com.color.screenshot.ColorLongshotViewInfo, com.color.screenshot.IColorLongshotViewCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.getLongshotViewInfoAsync(com.color.screenshot.ColorLongshotViewInfo, com.color.screenshot.IColorLongshotViewCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotAnalysisView(com.color.view.analysis.ColorViewAnalysis, java.util.List, java.util.List, boolean, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void longshotAnalysisView(com.color.view.analysis.ColorViewAnalysis r1, java.util.List<com.color.view.analysis.ColorViewNodeInfo> r2, java.util.List<com.color.view.analysis.ColorViewNodeInfo> r3, boolean r4, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotAnalysisView(com.color.view.analysis.ColorViewAnalysis, java.util.List, java.util.List, boolean, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.longshotAnalysisView(com.color.view.analysis.ColorViewAnalysis, java.util.List, java.util.List, boolean, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotCollectRoot(java.util.List):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void longshotCollectRoot(java.util.List<com.color.view.analysis.ColorViewNodeInfo> r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotCollectRoot(java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.longshotCollectRoot(java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: b in method: android.view.ColorLongshotViewHelper.longshotCollectWindow(boolean, boolean):com.color.view.analysis.ColorWindowNode, dex:  in method: android.view.ColorLongshotViewHelper.longshotCollectWindow(boolean, boolean):com.color.view.analysis.ColorWindowNode, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: b in method: android.view.ColorLongshotViewHelper.longshotCollectWindow(boolean, boolean):com.color.view.analysis.ColorWindowNode, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: b
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public com.color.view.analysis.ColorWindowNode longshotCollectWindow(boolean r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: b in method: android.view.ColorLongshotViewHelper.longshotCollectWindow(boolean, boolean):com.color.view.analysis.ColorWindowNode, dex:  in method: android.view.ColorLongshotViewHelper.longshotCollectWindow(boolean, boolean):com.color.view.analysis.ColorWindowNode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.longshotCollectWindow(boolean, boolean):com.color.view.analysis.ColorWindowNode");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.longshotDump(java.io.FileDescriptor, java.util.List, java.util.List):void, dex: 
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
    public void longshotDump(java.io.FileDescriptor r1, java.util.List<com.color.view.analysis.ColorWindowNode> r2, java.util.List<com.color.view.analysis.ColorWindowNode> r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ColorLongshotViewHelper.longshotDump(java.io.FileDescriptor, java.util.List, java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.longshotDump(java.io.FileDescriptor, java.util.List, java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotEndScroll():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void longshotEndScroll() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotEndScroll():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.longshotEndScroll():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotInjectScroll(com.color.view.inject.IColorInjectScrollCallback):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void longshotInjectScroll(com.color.view.inject.IColorInjectScrollCallback r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotInjectScroll(com.color.view.inject.IColorInjectScrollCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.longshotInjectScroll(com.color.view.inject.IColorInjectScrollCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotNotifyConnected(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void longshotNotifyConnected(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotNotifyConnected(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.longshotNotifyConnected(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotSetScrollMode(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void longshotSetScrollMode(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ColorLongshotViewHelper.longshotSetScrollMode(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.longshotSetScrollMode(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean onTransact(int r1, android.os.Parcel r2, android.os.Parcel r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ColorLongshotViewHelper.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ColorLongshotViewHelper.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
    }

    private long getTimeSpend(long timeStart) {
        return SystemClock.uptimeMillis() - timeStart;
    }

    private boolean isScrollable(View view) {
        if (canScrollVertically(view) || isScrollableView(view)) {
            return true;
        }
        return false;
    }

    private boolean isVerticalBar(Rect dst, Rect src) {
        return isLargeHeight(dst, src) ? isSmallWidth(dst, src) : false;
    }

    private List<ColorWindowNode> mergeWindowList(List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        List<ColorWindowNode> windows = new ArrayList();
        if (systemWindows != null) {
            windows.addAll(systemWindows);
        }
        if (floatWindows != null) {
            windows.addAll(floatWindows);
        }
        return windows;
    }

    private JSONObject getJSONObject(JSONObject jsonNode) {
        if (jsonNode == null) {
            return new JSONObject();
        }
        return jsonNode;
    }
}
