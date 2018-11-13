package android.widget;

import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.Application;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.StrictMode;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Filter;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import libcore.util.Objects;

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
public class RemoteViews implements Parcelable, Filter {
    private static final Action ACTION_NOOP = null;
    public static final Creator<RemoteViews> CREATOR = null;
    private static final OnClickHandler DEFAULT_ON_CLICK_HANDLER = null;
    static final String EXTRA_REMOTEADAPTER_APPWIDGET_ID = "remoteAdapterAppWidgetId";
    private static final String LOG_TAG = "RemoteViews";
    private static final int MODE_HAS_LANDSCAPE_AND_PORTRAIT = 1;
    private static final int MODE_NORMAL = 0;
    private static final ArrayMap<Method, Method> sAsyncMethods = null;
    private static final ThreadLocal<Object[]> sInvokeArgsTls = null;
    private static final ArrayMap<Class<? extends View>, ArrayMap<MutablePair<String, Class<?>>, Method>> sMethods = null;
    private static final Object[] sMethodsLock = null;
    private ArrayList<Action> mActions;
    private ApplicationInfo mApplication;
    private BitmapCache mBitmapCache;
    private boolean mIsRoot;
    private boolean mIsWidgetCollectionChild;
    private RemoteViews mLandscape;
    private final int mLayoutId;
    private MemoryUsageCounter mMemoryUsageCounter;
    private final MutablePair<String, Class<?>> mPair;
    private RemoteViews mPortrait;

    private static abstract class Action implements Parcelable {
        public static final int MERGE_APPEND = 1;
        public static final int MERGE_IGNORE = 2;
        public static final int MERGE_REPLACE = 0;
        int viewId;

        /* synthetic */ Action(Action action) {
            this();
        }

        public abstract void apply(View view, ViewGroup viewGroup, OnClickHandler onClickHandler) throws ActionException;

        public abstract String getActionName();

        private Action() {
        }

        public int describeContents() {
            return 0;
        }

        public void updateMemoryUsageEstimate(MemoryUsageCounter counter) {
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
        }

        public int mergeBehavior() {
            return 0;
        }

        public String getUniqueKey() {
            return getActionName() + this.viewId;
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            return this;
        }
    }

    private static abstract class RuntimeAction extends Action {
        /* synthetic */ RuntimeAction(RuntimeAction runtimeAction) {
            this();
        }

        private RuntimeAction() {
            super();
        }

        public final String getActionName() {
            return "RuntimeAction";
        }

        public final void writeToParcel(Parcel dest, int flags) {
            throw new UnsupportedOperationException();
        }
    }

    public static class ActionException extends RuntimeException {
        public ActionException(Exception ex) {
            super(ex);
        }

        public ActionException(String message) {
            super(message);
        }
    }

    private class AsyncApplyTask extends AsyncTask<Void, Void, ViewTree> implements OnCancelListener {
        private Action[] mActions;
        final Context mContext;
        private Exception mError;
        final OnClickHandler mHandler;
        final OnViewAppliedListener mListener;
        final ViewGroup mParent;
        final RemoteViews mRV;
        private View mResult;
        private ViewTree mTree;
        final /* synthetic */ RemoteViews this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.AsyncApplyTask.-get0(android.widget.RemoteViews$AsyncApplyTask):android.view.View, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ android.view.View m227-get0(android.widget.RemoteViews.AsyncApplyTask r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.AsyncApplyTask.-get0(android.widget.RemoteViews$AsyncApplyTask):android.view.View, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.AsyncApplyTask.-get0(android.widget.RemoteViews$AsyncApplyTask):android.view.View");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.AsyncApplyTask.<init>(android.widget.RemoteViews, android.widget.RemoteViews, android.view.ViewGroup, android.content.Context, android.widget.RemoteViews$OnViewAppliedListener, android.widget.RemoteViews$OnClickHandler, android.view.View):void, dex: 
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
        private AsyncApplyTask(android.widget.RemoteViews r1, android.widget.RemoteViews r2, android.view.ViewGroup r3, android.content.Context r4, android.widget.RemoteViews.OnViewAppliedListener r5, android.widget.RemoteViews.OnClickHandler r6, android.view.View r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.AsyncApplyTask.<init>(android.widget.RemoteViews, android.widget.RemoteViews, android.view.ViewGroup, android.content.Context, android.widget.RemoteViews$OnViewAppliedListener, android.widget.RemoteViews$OnClickHandler, android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.AsyncApplyTask.<init>(android.widget.RemoteViews, android.widget.RemoteViews, android.view.ViewGroup, android.content.Context, android.widget.RemoteViews$OnViewAppliedListener, android.widget.RemoteViews$OnClickHandler, android.view.View):void");
        }

        /* synthetic */ AsyncApplyTask(RemoteViews this$0, RemoteViews rv, ViewGroup parent, Context context, OnViewAppliedListener listener, OnClickHandler handler, View result, AsyncApplyTask asyncApplyTask) {
            this(this$0, rv, parent, context, listener, handler, result);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 6 in method: android.widget.RemoteViews.AsyncApplyTask.doInBackground(java.lang.Void[]):android.widget.RemoteViews$ViewTree, dex:  in method: android.widget.RemoteViews.AsyncApplyTask.doInBackground(java.lang.Void[]):android.widget.RemoteViews$ViewTree, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 6 in method: android.widget.RemoteViews.AsyncApplyTask.doInBackground(java.lang.Void[]):android.widget.RemoteViews$ViewTree, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus registerCount: 6
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        protected android.widget.RemoteViews.ViewTree doInBackground(java.lang.Void... r1) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: 6 in method: android.widget.RemoteViews.AsyncApplyTask.doInBackground(java.lang.Void[]):android.widget.RemoteViews$ViewTree, dex:  in method: android.widget.RemoteViews.AsyncApplyTask.doInBackground(java.lang.Void[]):android.widget.RemoteViews$ViewTree, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.AsyncApplyTask.doInBackground(java.lang.Void[]):android.widget.RemoteViews$ViewTree");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.AsyncApplyTask.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected /* bridge */ /* synthetic */ java.lang.Object doInBackground(java.lang.Object[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.AsyncApplyTask.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.AsyncApplyTask.doInBackground(java.lang.Object[]):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.AsyncApplyTask.onCancel():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onCancel() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.AsyncApplyTask.onCancel():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.AsyncApplyTask.onCancel():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.AsyncApplyTask.onPostExecute(android.widget.RemoteViews$ViewTree):void, dex: 
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
        protected void onPostExecute(android.widget.RemoteViews.ViewTree r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.AsyncApplyTask.onPostExecute(android.widget.RemoteViews$ViewTree):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.AsyncApplyTask.onPostExecute(android.widget.RemoteViews$ViewTree):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.AsyncApplyTask.onPostExecute(java.lang.Object):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected /* bridge */ /* synthetic */ void onPostExecute(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.AsyncApplyTask.onPostExecute(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.AsyncApplyTask.onPostExecute(java.lang.Object):void");
        }
    }

    private static class BitmapCache {
        ArrayList<Bitmap> mBitmaps;

        public BitmapCache() {
            this.mBitmaps = new ArrayList();
        }

        public BitmapCache(Parcel source) {
            int count = source.readInt();
            this.mBitmaps = new ArrayList();
            for (int i = 0; i < count; i++) {
                this.mBitmaps.add((Bitmap) Bitmap.CREATOR.createFromParcel(source));
            }
        }

        public int getBitmapId(Bitmap b) {
            if (b == null) {
                return -1;
            }
            if (this.mBitmaps.contains(b)) {
                return this.mBitmaps.indexOf(b);
            }
            this.mBitmaps.add(b);
            return this.mBitmaps.size() - 1;
        }

        public Bitmap getBitmapForId(int id) {
            if (id == -1 || id >= this.mBitmaps.size()) {
                return null;
            }
            return (Bitmap) this.mBitmaps.get(id);
        }

        public void writeBitmapsToParcel(Parcel dest, int flags) {
            int count = this.mBitmaps.size();
            dest.writeInt(count);
            for (int i = 0; i < count; i++) {
                ((Bitmap) this.mBitmaps.get(i)).writeToParcel(dest, flags);
            }
        }

        public void assimilate(BitmapCache bitmapCache) {
            ArrayList<Bitmap> bitmapsToBeAdded = bitmapCache.mBitmaps;
            int count = bitmapsToBeAdded.size();
            for (int i = 0; i < count; i++) {
                Bitmap b = (Bitmap) bitmapsToBeAdded.get(i);
                if (!this.mBitmaps.contains(b)) {
                    this.mBitmaps.add(b);
                }
            }
        }

        public void addBitmapMemory(MemoryUsageCounter memoryCounter) {
            for (int i = 0; i < this.mBitmaps.size(); i++) {
                memoryCounter.addBitmapMemory((Bitmap) this.mBitmaps.get(i));
            }
        }

        protected /* bridge */ /* synthetic */ Object clone() throws CloneNotSupportedException {
            return clone();
        }

        protected BitmapCache clone() {
            BitmapCache bitmapCache = new BitmapCache();
            bitmapCache.mBitmaps.addAll(this.mBitmaps);
            return bitmapCache;
        }
    }

    private class BitmapReflectionAction extends Action {
        public static final int TAG = 12;
        Bitmap bitmap;
        int bitmapId;
        String methodName;
        final /* synthetic */ RemoteViews this$0;

        BitmapReflectionAction(RemoteViews this$0, int viewId, String methodName, Bitmap bitmap) {
            this.this$0 = this$0;
            super();
            this.bitmap = bitmap;
            this.viewId = viewId;
            this.methodName = methodName;
            this.bitmapId = this$0.mBitmapCache.getBitmapId(bitmap);
        }

        BitmapReflectionAction(RemoteViews this$0, Parcel in) {
            this.this$0 = this$0;
            super();
            this.viewId = in.readInt();
            this.methodName = in.readString();
            this.bitmapId = in.readInt();
            this.bitmap = this$0.mBitmapCache.getBitmapForId(this.bitmapId);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(12);
            dest.writeInt(this.viewId);
            dest.writeString(this.methodName);
            dest.writeInt(this.bitmapId);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) throws ActionException {
            new ReflectionAction(this.this$0, this.viewId, this.methodName, 12, this.bitmap).apply(root, rootParent, handler);
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
            this.bitmapId = bitmapCache.getBitmapId(this.bitmap);
        }

        public String getActionName() {
            return "BitmapReflectionAction";
        }
    }

    private static class LayoutParamAction extends Action {
        public static final int LAYOUT_MARGIN_BOTTOM_DIMEN = 3;
        public static final int LAYOUT_MARGIN_END_DIMEN = 1;
        public static final int LAYOUT_WIDTH = 2;
        public static final int TAG = 19;
        int property;
        int value;

        public LayoutParamAction(int viewId, int property, int value) {
            super();
            this.viewId = viewId;
            this.property = property;
            this.value = value;
        }

        public LayoutParamAction(Parcel parcel) {
            super();
            this.viewId = parcel.readInt();
            this.property = parcel.readInt();
            this.value = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(19);
            dest.writeInt(this.viewId);
            dest.writeInt(this.property);
            dest.writeInt(this.value);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                LayoutParams layoutParams = target.getLayoutParams();
                if (layoutParams != null) {
                    switch (this.property) {
                        case 1:
                            if (layoutParams instanceof MarginLayoutParams) {
                                ((MarginLayoutParams) layoutParams).setMarginEnd(resolveDimenPixelOffset(target, this.value));
                                target.setLayoutParams(layoutParams);
                                break;
                            }
                            break;
                        case 2:
                            layoutParams.width = this.value;
                            target.setLayoutParams(layoutParams);
                            break;
                        case 3:
                            if (layoutParams instanceof MarginLayoutParams) {
                                ((MarginLayoutParams) layoutParams).bottomMargin = resolveDimenPixelOffset(target, this.value);
                                target.setLayoutParams(layoutParams);
                                break;
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown property " + this.property);
                    }
                }
            }
        }

        private static int resolveDimenPixelOffset(View target, int value) {
            if (value == 0) {
                return 0;
            }
            return target.getContext().getResources().getDimensionPixelOffset(value);
        }

        public String getActionName() {
            return "LayoutParamAction" + this.property + ".";
        }
    }

    private class MemoryUsageCounter {
        /* renamed from: -android-graphics-Bitmap$ConfigSwitchesValues */
        private static final /* synthetic */ int[] f11-android-graphics-Bitmap$ConfigSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$android$graphics$Bitmap$Config;
        int mMemoryUsage;
        final /* synthetic */ RemoteViews this$0;

        /* renamed from: -getandroid-graphics-Bitmap$ConfigSwitchesValues */
        private static /* synthetic */ int[] m228-getandroid-graphics-Bitmap$ConfigSwitchesValues() {
            if (f11-android-graphics-Bitmap$ConfigSwitchesValues != null) {
                return f11-android-graphics-Bitmap$ConfigSwitchesValues;
            }
            int[] iArr = new int[Config.values().length];
            try {
                iArr[Config.ALPHA_8.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Config.ARGB_4444.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Config.ARGB_8888.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Config.RGB_565.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            f11-android-graphics-Bitmap$ConfigSwitchesValues = iArr;
            return iArr;
        }

        /* synthetic */ MemoryUsageCounter(RemoteViews this$0, MemoryUsageCounter memoryUsageCounter) {
            this(this$0);
        }

        private MemoryUsageCounter(RemoteViews this$0) {
            this.this$0 = this$0;
        }

        public void clear() {
            this.mMemoryUsage = 0;
        }

        public void increment(int numBytes) {
            this.mMemoryUsage += numBytes;
        }

        public int getMemoryUsage() {
            return this.mMemoryUsage;
        }

        public void addBitmapMemory(Bitmap b) {
            Config c = b.getConfig();
            int bpp = 4;
            if (c != null) {
                switch (m228-getandroid-graphics-Bitmap$ConfigSwitchesValues()[c.ordinal()]) {
                    case 1:
                        bpp = 1;
                        break;
                    case 2:
                    case 4:
                        bpp = 2;
                        break;
                    case 3:
                        bpp = 4;
                        break;
                }
            }
            increment((b.getWidth() * b.getHeight()) * bpp);
        }
    }

    static class MutablePair<F, S> {
        F first;
        S second;

        MutablePair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof MutablePair)) {
                return false;
            }
            MutablePair<?, ?> p = (MutablePair) o;
            if (Objects.equal(p.first, this.first)) {
                z = Objects.equal(p.second, this.second);
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = this.first == null ? 0 : this.first.hashCode();
            if (this.second != null) {
                i = this.second.hashCode();
            }
            return hashCode ^ i;
        }
    }

    public static class OnClickHandler {
        private int mEnterAnimationId;

        public OnClickHandler() {
        }

        public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent) {
            return onClickHandler(view, pendingIntent, fillInIntent, -1);
        }

        public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent, int launchStackId) {
            try {
                ActivityOptions opts;
                Context context = view.getContext();
                if (this.mEnterAnimationId != 0) {
                    opts = ActivityOptions.makeCustomAnimation(context, this.mEnterAnimationId, 0);
                } else {
                    opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                }
                if (launchStackId != -1) {
                    opts.setLaunchStackId(launchStackId);
                }
                context.startIntentSender(pendingIntent.getIntentSender(), fillInIntent, 268435456, 268435456, 0, opts.toBundle());
                return true;
            } catch (SendIntentException e) {
                Log.e(RemoteViews.LOG_TAG, "Cannot send pending intent: ", e);
                return false;
            } catch (Exception e2) {
                Log.e(RemoteViews.LOG_TAG, "Cannot send pending intent due to unknown exception: ", e2);
                return false;
            }
        }

        public void setEnterAnimationId(int enterAnimationId) {
            this.mEnterAnimationId = enterAnimationId;
        }
    }

    public interface OnViewAppliedListener {
        void onError(Exception exception);

        void onViewApplied(View view);
    }

    private final class ReflectionAction extends Action {
        static final int BITMAP = 12;
        static final int BOOLEAN = 1;
        static final int BUNDLE = 13;
        static final int BYTE = 2;
        static final int CHAR = 8;
        static final int CHAR_SEQUENCE = 10;
        static final int COLOR_STATE_LIST = 15;
        static final int DOUBLE = 7;
        static final int FLOAT = 6;
        static final int ICON = 16;
        static final int INT = 4;
        static final int INTENT = 14;
        static final int LONG = 5;
        static final int SHORT = 3;
        static final int STRING = 9;
        static final int TAG = 2;
        static final int URI = 11;
        String methodName;
        final /* synthetic */ RemoteViews this$0;
        int type;
        Object value;

        ReflectionAction(RemoteViews this$0, int viewId, String methodName, int type, Object value) {
            this.this$0 = this$0;
            super();
            this.viewId = viewId;
            this.methodName = methodName;
            this.type = type;
            this.value = value;
        }

        ReflectionAction(RemoteViews this$0, Parcel in) {
            boolean z = false;
            this.this$0 = this$0;
            super();
            this.viewId = in.readInt();
            this.methodName = in.readString();
            this.type = in.readInt();
            switch (this.type) {
                case 1:
                    if (in.readInt() != 0) {
                        z = true;
                    }
                    this.value = Boolean.valueOf(z);
                    return;
                case 2:
                    this.value = Byte.valueOf(in.readByte());
                    return;
                case 3:
                    this.value = Short.valueOf((short) in.readInt());
                    return;
                case 4:
                    this.value = Integer.valueOf(in.readInt());
                    return;
                case 5:
                    this.value = Long.valueOf(in.readLong());
                    return;
                case 6:
                    this.value = Float.valueOf(in.readFloat());
                    return;
                case 7:
                    this.value = Double.valueOf(in.readDouble());
                    return;
                case 8:
                    this.value = Character.valueOf((char) in.readInt());
                    return;
                case 9:
                    this.value = in.readString();
                    return;
                case 10:
                    this.value = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
                    return;
                case 11:
                    if (in.readInt() != 0) {
                        this.value = Uri.CREATOR.createFromParcel(in);
                        return;
                    }
                    return;
                case 12:
                    if (in.readInt() != 0) {
                        this.value = Bitmap.CREATOR.createFromParcel(in);
                        return;
                    }
                    return;
                case 13:
                    this.value = in.readBundle();
                    return;
                case 14:
                    if (in.readInt() != 0) {
                        this.value = Intent.CREATOR.createFromParcel(in);
                        return;
                    }
                    return;
                case 15:
                    if (in.readInt() != 0) {
                        this.value = ColorStateList.CREATOR.createFromParcel(in);
                        return;
                    }
                    return;
                case 16:
                    if (in.readInt() != 0) {
                        this.value = Icon.CREATOR.createFromParcel(in);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            int i = 1;
            out.writeInt(2);
            out.writeInt(this.viewId);
            out.writeString(this.methodName);
            out.writeInt(this.type);
            switch (this.type) {
                case 1:
                    out.writeInt(((Boolean) this.value).booleanValue() ? 1 : 0);
                    return;
                case 2:
                    out.writeByte(((Byte) this.value).byteValue());
                    return;
                case 3:
                    out.writeInt(((Short) this.value).shortValue());
                    return;
                case 4:
                    out.writeInt(((Integer) this.value).intValue());
                    return;
                case 5:
                    out.writeLong(((Long) this.value).longValue());
                    return;
                case 6:
                    out.writeFloat(((Float) this.value).floatValue());
                    return;
                case 7:
                    out.writeDouble(((Double) this.value).doubleValue());
                    return;
                case 8:
                    out.writeInt(((Character) this.value).charValue());
                    return;
                case 9:
                    out.writeString((String) this.value);
                    return;
                case 10:
                    TextUtils.writeToParcel((CharSequence) this.value, out, flags);
                    return;
                case 11:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((Uri) this.value).writeToParcel(out, flags);
                        return;
                    }
                    return;
                case 12:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((Bitmap) this.value).writeToParcel(out, flags);
                        return;
                    }
                    return;
                case 13:
                    out.writeBundle((Bundle) this.value);
                    return;
                case 14:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((Intent) this.value).writeToParcel(out, flags);
                        return;
                    }
                    return;
                case 15:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((ColorStateList) this.value).writeToParcel(out, flags);
                        return;
                    }
                    return;
                case 16:
                    if (this.value == null) {
                        i = 0;
                    }
                    out.writeInt(i);
                    if (this.value != null) {
                        ((Icon) this.value).writeToParcel(out, flags);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private Class<?> getParameterType() {
            switch (this.type) {
                case 1:
                    return Boolean.TYPE;
                case 2:
                    return Byte.TYPE;
                case 3:
                    return Short.TYPE;
                case 4:
                    return Integer.TYPE;
                case 5:
                    return Long.TYPE;
                case 6:
                    return Float.TYPE;
                case 7:
                    return Double.TYPE;
                case 8:
                    return Character.TYPE;
                case 9:
                    return String.class;
                case 10:
                    return CharSequence.class;
                case 11:
                    return Uri.class;
                case 12:
                    return Bitmap.class;
                case 13:
                    return Bundle.class;
                case 14:
                    return Intent.class;
                case 15:
                    return ColorStateList.class;
                case 16:
                    return Icon.class;
                default:
                    return null;
            }
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View view = root.findViewById(this.viewId);
            if (view != null) {
                Class<?> param = getParameterType();
                if (param == null) {
                    throw new ActionException("bad type: " + this.type);
                }
                try {
                    this.this$0.getMethod(view, this.methodName, param).invoke(view, RemoteViews.wrapArg(this.value));
                } catch (ActionException e) {
                    throw e;
                } catch (Exception ex) {
                    throw new ActionException(ex);
                }
            }
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            View view = root.findViewById(this.viewId);
            if (view == null) {
                return RemoteViews.ACTION_NOOP;
            }
            Class<?> param = getParameterType();
            if (param == null) {
                throw new ActionException("bad type: " + this.type);
            }
            try {
                Method asyncMethod = this.this$0.getAsyncMethod(this.this$0.getMethod(view, this.methodName, param));
                if (asyncMethod == null) {
                    return this;
                }
                Runnable endAction = (Runnable) asyncMethod.invoke(view, RemoteViews.wrapArg(this.value));
                if (endAction == null) {
                    return RemoteViews.ACTION_NOOP;
                }
                return new RunnableAction(endAction);
            } catch (ActionException e) {
                throw e;
            } catch (Exception ex) {
                throw new ActionException(ex);
            }
        }

        public int mergeBehavior() {
            if (this.methodName.equals("smoothScrollBy")) {
                return 1;
            }
            return 0;
        }

        public String getActionName() {
            return "ReflectionAction" + this.methodName + this.type;
        }
    }

    private final class ReflectionActionWithoutParams extends Action {
        public static final int TAG = 5;
        final String methodName;
        final /* synthetic */ RemoteViews this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.<init>(android.widget.RemoteViews, int, java.lang.String):void, dex: 
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
        ReflectionActionWithoutParams(android.widget.RemoteViews r1, int r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.<init>(android.widget.RemoteViews, int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ReflectionActionWithoutParams.<init>(android.widget.RemoteViews, int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
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
        ReflectionActionWithoutParams(android.widget.RemoteViews r1, android.os.Parcel r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ReflectionActionWithoutParams.<init>(android.widget.RemoteViews, android.os.Parcel):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ReflectionActionWithoutParams.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.mergeBehavior():int, dex: 
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
        public int mergeBehavior() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.mergeBehavior():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ReflectionActionWithoutParams.mergeBehavior():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.writeToParcel(android.os.Parcel, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.ReflectionActionWithoutParams.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ReflectionActionWithoutParams.writeToParcel(android.os.Parcel, int):void");
        }

        public String getActionName() {
            return "ReflectionActionWithoutParams";
        }
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RemoteView {
    }

    private static final class RunnableAction extends RuntimeAction {
        private final Runnable mRunnable;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.RunnableAction.<init>(java.lang.Runnable):void, dex: 
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
        RunnableAction(java.lang.Runnable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.RunnableAction.<init>(java.lang.Runnable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.RunnableAction.<init>(java.lang.Runnable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.RunnableAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
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
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.RunnableAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.RunnableAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }
    }

    private class SetDrawableParameters extends Action {
        public static final int TAG = 3;
        int alpha;
        int colorFilter;
        Mode filterMode;
        int level;
        boolean targetBackground;
        final /* synthetic */ RemoteViews this$0;

        public SetDrawableParameters(RemoteViews this$0, int id, boolean targetBackground, int alpha, int colorFilter, Mode mode, int level) {
            this.this$0 = this$0;
            super();
            this.viewId = id;
            this.targetBackground = targetBackground;
            this.alpha = alpha;
            this.colorFilter = colorFilter;
            this.filterMode = mode;
            this.level = level;
        }

        public SetDrawableParameters(RemoteViews this$0, Parcel parcel) {
            boolean z;
            boolean hasMode;
            this.this$0 = this$0;
            super();
            this.viewId = parcel.readInt();
            if (parcel.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.targetBackground = z;
            this.alpha = parcel.readInt();
            this.colorFilter = parcel.readInt();
            if (parcel.readInt() != 0) {
                hasMode = true;
            } else {
                hasMode = false;
            }
            if (hasMode) {
                this.filterMode = Mode.valueOf(parcel.readString());
            } else {
                this.filterMode = null;
            }
            this.level = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(3);
            dest.writeInt(this.viewId);
            if (this.targetBackground) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.alpha);
            dest.writeInt(this.colorFilter);
            if (this.filterMode != null) {
                dest.writeInt(1);
                dest.writeString(this.filterMode.toString());
            } else {
                dest.writeInt(0);
            }
            dest.writeInt(this.level);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                Drawable targetDrawable = null;
                if (this.targetBackground) {
                    targetDrawable = target.getBackground();
                } else if (target instanceof ImageView) {
                    targetDrawable = ((ImageView) target).getDrawable();
                }
                if (targetDrawable != null) {
                    if (this.alpha != -1) {
                        targetDrawable.mutate().setAlpha(this.alpha);
                    }
                    if (this.filterMode != null) {
                        targetDrawable.mutate().setColorFilter(this.colorFilter, this.filterMode);
                    }
                    if (this.level != -1) {
                        targetDrawable.mutate().setLevel(this.level);
                    }
                }
            }
        }

        public String getActionName() {
            return "SetDrawableParameters";
        }
    }

    private class SetEmptyView extends Action {
        public static final int TAG = 6;
        int emptyViewId;
        final /* synthetic */ RemoteViews this$0;
        int viewId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetEmptyView.<init>(android.widget.RemoteViews, int, int):void, dex: 
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
        SetEmptyView(android.widget.RemoteViews r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetEmptyView.<init>(android.widget.RemoteViews, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetEmptyView.<init>(android.widget.RemoteViews, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetEmptyView.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
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
        SetEmptyView(android.widget.RemoteViews r1, android.os.Parcel r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetEmptyView.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetEmptyView.<init>(android.widget.RemoteViews, android.os.Parcel):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetEmptyView.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetEmptyView.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetEmptyView.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetEmptyView.writeToParcel(android.os.Parcel, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetEmptyView.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetEmptyView.writeToParcel(android.os.Parcel, int):void");
        }

        public String getActionName() {
            return "SetEmptyView";
        }
    }

    private class SetOnClickFillInIntent extends Action {
        public static final int TAG = 9;
        Intent fillInIntent;
        final /* synthetic */ RemoteViews this$0;

        /* renamed from: android.widget.RemoteViews$SetOnClickFillInIntent$1 */
        class AnonymousClass1 implements OnClickListener {
            final /* synthetic */ SetOnClickFillInIntent this$1;
            final /* synthetic */ OnClickHandler val$handler;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetOnClickFillInIntent.1.<init>(android.widget.RemoteViews$SetOnClickFillInIntent, android.widget.RemoteViews$OnClickHandler):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass1(android.widget.RemoteViews.SetOnClickFillInIntent r1, android.widget.RemoteViews.OnClickHandler r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetOnClickFillInIntent.1.<init>(android.widget.RemoteViews$SetOnClickFillInIntent, android.widget.RemoteViews$OnClickHandler):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetOnClickFillInIntent.1.<init>(android.widget.RemoteViews$SetOnClickFillInIntent, android.widget.RemoteViews$OnClickHandler):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetOnClickFillInIntent.1.onClick(android.view.View):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public void onClick(android.view.View r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetOnClickFillInIntent.1.onClick(android.view.View):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetOnClickFillInIntent.1.onClick(android.view.View):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetOnClickFillInIntent.<init>(android.widget.RemoteViews, int, android.content.Intent):void, dex: 
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
        public SetOnClickFillInIntent(android.widget.RemoteViews r1, int r2, android.content.Intent r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetOnClickFillInIntent.<init>(android.widget.RemoteViews, int, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetOnClickFillInIntent.<init>(android.widget.RemoteViews, int, android.content.Intent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetOnClickFillInIntent.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
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
        public SetOnClickFillInIntent(android.widget.RemoteViews r1, android.os.Parcel r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetOnClickFillInIntent.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetOnClickFillInIntent.<init>(android.widget.RemoteViews, android.os.Parcel):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetOnClickFillInIntent.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetOnClickFillInIntent.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetOnClickFillInIntent.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetOnClickFillInIntent.writeToParcel(android.os.Parcel, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetOnClickFillInIntent.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetOnClickFillInIntent.writeToParcel(android.os.Parcel, int):void");
        }

        public String getActionName() {
            return "SetOnClickFillInIntent";
        }
    }

    private class SetOnClickPendingIntent extends Action {
        public static final int TAG = 1;
        PendingIntent pendingIntent;
        final /* synthetic */ RemoteViews this$0;

        public SetOnClickPendingIntent(RemoteViews this$0, int id, PendingIntent pendingIntent) {
            this.this$0 = this$0;
            super();
            this.viewId = id;
            this.pendingIntent = pendingIntent;
        }

        public SetOnClickPendingIntent(RemoteViews this$0, Parcel parcel) {
            this.this$0 = this$0;
            super();
            this.viewId = parcel.readInt();
            if (parcel.readInt() != 0) {
                this.pendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(parcel);
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i = 1;
            dest.writeInt(1);
            dest.writeInt(this.viewId);
            if (this.pendingIntent == null) {
                i = 0;
            }
            dest.writeInt(i);
            if (this.pendingIntent != null) {
                this.pendingIntent.writeToParcel(dest, 0);
            }
        }

        public void apply(View root, ViewGroup rootParent, final OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                if (this.this$0.mIsWidgetCollectionChild) {
                    Log.w(RemoteViews.LOG_TAG, "Cannot setOnClickPendingIntent for collection item (id: " + this.viewId + ")");
                    ApplicationInfo appInfo = root.getContext().getApplicationInfo();
                    if (appInfo != null && appInfo.targetSdkVersion >= 16) {
                        return;
                    }
                }
                OnClickListener onClickListener = null;
                if (this.pendingIntent != null) {
                    onClickListener = new OnClickListener(this) {
                        final /* synthetic */ SetOnClickPendingIntent this$1;

                        public void onClick(View v) {
                            Rect rect = RemoteViews.getSourceBounds(v);
                            Intent intent = new Intent();
                            intent.setSourceBounds(rect);
                            handler.onClickHandler(v, this.this$1.pendingIntent, intent);
                        }
                    };
                }
                target.setOnClickListener(onClickListener);
            }
        }

        public String getActionName() {
            return "SetOnClickPendingIntent";
        }
    }

    private class SetPendingIntentTemplate extends Action {
        public static final int TAG = 8;
        PendingIntent pendingIntentTemplate;
        final /* synthetic */ RemoteViews this$0;

        /* renamed from: android.widget.RemoteViews$SetPendingIntentTemplate$1 */
        class AnonymousClass1 implements OnItemClickListener {
            final /* synthetic */ SetPendingIntentTemplate this$1;
            final /* synthetic */ OnClickHandler val$handler;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetPendingIntentTemplate.1.<init>(android.widget.RemoteViews$SetPendingIntentTemplate, android.widget.RemoteViews$OnClickHandler):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass1(android.widget.RemoteViews.SetPendingIntentTemplate r1, android.widget.RemoteViews.OnClickHandler r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetPendingIntentTemplate.1.<init>(android.widget.RemoteViews$SetPendingIntentTemplate, android.widget.RemoteViews$OnClickHandler):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetPendingIntentTemplate.1.<init>(android.widget.RemoteViews$SetPendingIntentTemplate, android.widget.RemoteViews$OnClickHandler):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetPendingIntentTemplate.1.onItemClick(android.widget.AdapterView, android.view.View, int, long):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public void onItemClick(android.widget.AdapterView<?> r1, android.view.View r2, int r3, long r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetPendingIntentTemplate.1.onItemClick(android.widget.AdapterView, android.view.View, int, long):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetPendingIntentTemplate.1.onItemClick(android.widget.AdapterView, android.view.View, int, long):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetPendingIntentTemplate.<init>(android.widget.RemoteViews, int, android.app.PendingIntent):void, dex: 
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
        public SetPendingIntentTemplate(android.widget.RemoteViews r1, int r2, android.app.PendingIntent r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetPendingIntentTemplate.<init>(android.widget.RemoteViews, int, android.app.PendingIntent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetPendingIntentTemplate.<init>(android.widget.RemoteViews, int, android.app.PendingIntent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetPendingIntentTemplate.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
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
        public SetPendingIntentTemplate(android.widget.RemoteViews r1, android.os.Parcel r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetPendingIntentTemplate.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetPendingIntentTemplate.<init>(android.widget.RemoteViews, android.os.Parcel):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetPendingIntentTemplate.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetPendingIntentTemplate.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetPendingIntentTemplate.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetPendingIntentTemplate.writeToParcel(android.os.Parcel, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetPendingIntentTemplate.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetPendingIntentTemplate.writeToParcel(android.os.Parcel, int):void");
        }

        public String getActionName() {
            return "SetPendingIntentTemplate";
        }
    }

    private class SetRemoteInputsAction extends Action {
        public static final int TAG = 18;
        final Parcelable[] remoteInputs;
        final /* synthetic */ RemoteViews this$0;

        public SetRemoteInputsAction(RemoteViews this$0, int viewId, RemoteInput[] remoteInputs) {
            this.this$0 = this$0;
            super();
            this.viewId = viewId;
            this.remoteInputs = remoteInputs;
        }

        public SetRemoteInputsAction(RemoteViews this$0, Parcel parcel) {
            this.this$0 = this$0;
            super();
            this.viewId = parcel.readInt();
            this.remoteInputs = (Parcelable[]) parcel.createTypedArray(RemoteInput.CREATOR);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(18);
            dest.writeInt(this.viewId);
            dest.writeTypedArray(this.remoteInputs, flags);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            TextView target = (TextView) root.findViewById(this.viewId);
            if (target != null) {
                target.setTagInternal(R.id.remote_input_tag, this.remoteInputs);
            }
        }

        public String getActionName() {
            return "SetRemoteInputsAction";
        }
    }

    private class SetRemoteViewsAdapterIntent extends Action {
        public static final int TAG = 10;
        Intent intent;
        final /* synthetic */ RemoteViews this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.<init>(android.widget.RemoteViews, int, android.content.Intent):void, dex: 
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
        public SetRemoteViewsAdapterIntent(android.widget.RemoteViews r1, int r2, android.content.Intent r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.<init>(android.widget.RemoteViews, int, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.<init>(android.widget.RemoteViews, int, android.content.Intent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
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
        public SetRemoteViewsAdapterIntent(android.widget.RemoteViews r1, android.os.Parcel r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.<init>(android.widget.RemoteViews, android.os.Parcel):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.writeToParcel(android.os.Parcel, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetRemoteViewsAdapterIntent.writeToParcel(android.os.Parcel, int):void");
        }

        public String getActionName() {
            return "SetRemoteViewsAdapterIntent";
        }
    }

    private class SetRemoteViewsAdapterList extends Action {
        public static final int TAG = 15;
        ArrayList<RemoteViews> list;
        final /* synthetic */ RemoteViews this$0;
        int viewTypeCount;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.<init>(android.widget.RemoteViews, int, java.util.ArrayList, int):void, dex:  in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.<init>(android.widget.RemoteViews, int, java.util.ArrayList, int):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.<init>(android.widget.RemoteViews, int, java.util.ArrayList, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public SetRemoteViewsAdapterList(android.widget.RemoteViews r1, int r2, java.util.ArrayList<android.widget.RemoteViews> r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.<init>(android.widget.RemoteViews, int, java.util.ArrayList, int):void, dex:  in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.<init>(android.widget.RemoteViews, int, java.util.ArrayList, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetRemoteViewsAdapterList.<init>(android.widget.RemoteViews, int, java.util.ArrayList, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
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
        public SetRemoteViewsAdapterList(android.widget.RemoteViews r1, android.os.Parcel r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetRemoteViewsAdapterList.<init>(android.widget.RemoteViews, android.os.Parcel):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetRemoteViewsAdapterList.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.writeToParcel(android.os.Parcel, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.SetRemoteViewsAdapterList.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.SetRemoteViewsAdapterList.writeToParcel(android.os.Parcel, int):void");
        }

        public String getActionName() {
            return "SetRemoteViewsAdapterList";
        }
    }

    private class TextViewDrawableAction extends Action {
        public static final int TAG = 11;
        int d1;
        int d2;
        int d3;
        int d4;
        boolean drawablesLoaded;
        Icon i1;
        Icon i2;
        Icon i3;
        Icon i4;
        Drawable id1;
        Drawable id2;
        Drawable id3;
        Drawable id4;
        boolean isRelative;
        final /* synthetic */ RemoteViews this$0;
        boolean useIcons;

        public TextViewDrawableAction(RemoteViews this$0, int viewId, boolean isRelative, int d1, int d2, int d3, int d4) {
            this.this$0 = this$0;
            super();
            this.isRelative = false;
            this.useIcons = false;
            this.drawablesLoaded = false;
            this.viewId = viewId;
            this.isRelative = isRelative;
            this.useIcons = false;
            this.d1 = d1;
            this.d2 = d2;
            this.d3 = d3;
            this.d4 = d4;
        }

        public TextViewDrawableAction(RemoteViews this$0, int viewId, boolean isRelative, Icon i1, Icon i2, Icon i3, Icon i4) {
            this.this$0 = this$0;
            super();
            this.isRelative = false;
            this.useIcons = false;
            this.drawablesLoaded = false;
            this.viewId = viewId;
            this.isRelative = isRelative;
            this.useIcons = true;
            this.i1 = i1;
            this.i2 = i2;
            this.i3 = i3;
            this.i4 = i4;
        }

        public TextViewDrawableAction(RemoteViews this$0, Parcel parcel) {
            boolean z;
            boolean z2 = true;
            this.this$0 = this$0;
            super();
            this.isRelative = false;
            this.useIcons = false;
            this.drawablesLoaded = false;
            this.viewId = parcel.readInt();
            if (parcel.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.isRelative = z;
            if (parcel.readInt() == 0) {
                z2 = false;
            }
            this.useIcons = z2;
            if (this.useIcons) {
                if (parcel.readInt() != 0) {
                    this.i1 = (Icon) Icon.CREATOR.createFromParcel(parcel);
                }
                if (parcel.readInt() != 0) {
                    this.i2 = (Icon) Icon.CREATOR.createFromParcel(parcel);
                }
                if (parcel.readInt() != 0) {
                    this.i3 = (Icon) Icon.CREATOR.createFromParcel(parcel);
                }
                if (parcel.readInt() != 0) {
                    this.i4 = (Icon) Icon.CREATOR.createFromParcel(parcel);
                    return;
                }
                return;
            }
            this.d1 = parcel.readInt();
            this.d2 = parcel.readInt();
            this.d3 = parcel.readInt();
            this.d4 = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(11);
            dest.writeInt(this.viewId);
            if (this.isRelative) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            if (this.useIcons) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            if (this.useIcons) {
                if (this.i1 != null) {
                    dest.writeInt(1);
                    this.i1.writeToParcel(dest, 0);
                } else {
                    dest.writeInt(0);
                }
                if (this.i2 != null) {
                    dest.writeInt(1);
                    this.i2.writeToParcel(dest, 0);
                } else {
                    dest.writeInt(0);
                }
                if (this.i3 != null) {
                    dest.writeInt(1);
                    this.i3.writeToParcel(dest, 0);
                } else {
                    dest.writeInt(0);
                }
                if (this.i4 != null) {
                    dest.writeInt(1);
                    this.i4.writeToParcel(dest, 0);
                    return;
                }
                dest.writeInt(0);
                return;
            }
            dest.writeInt(this.d1);
            dest.writeInt(this.d2);
            dest.writeInt(this.d3);
            dest.writeInt(this.d4);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            TextView target = (TextView) root.findViewById(this.viewId);
            if (target != null) {
                if (this.drawablesLoaded) {
                    if (this.isRelative) {
                        target.setCompoundDrawablesRelativeWithIntrinsicBounds(this.id1, this.id2, this.id3, this.id4);
                    } else {
                        target.setCompoundDrawablesWithIntrinsicBounds(this.id1, this.id2, this.id3, this.id4);
                    }
                } else if (this.useIcons) {
                    Context ctx = target.getContext();
                    Drawable id1 = this.i1 == null ? null : this.i1.loadDrawable(ctx);
                    Drawable id2 = this.i2 == null ? null : this.i2.loadDrawable(ctx);
                    Drawable id3 = this.i3 == null ? null : this.i3.loadDrawable(ctx);
                    Drawable id4 = this.i4 == null ? null : this.i4.loadDrawable(ctx);
                    if (this.isRelative) {
                        target.setCompoundDrawablesRelativeWithIntrinsicBounds(id1, id2, id3, id4);
                    } else {
                        target.setCompoundDrawablesWithIntrinsicBounds(id1, id2, id3, id4);
                    }
                } else if (this.isRelative) {
                    target.setCompoundDrawablesRelativeWithIntrinsicBounds(this.d1, this.d2, this.d3, this.d4);
                } else {
                    target.setCompoundDrawablesWithIntrinsicBounds(this.d1, this.d2, this.d3, this.d4);
                }
            }
        }

        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            Drawable drawable = null;
            TextView target = (TextView) root.findViewById(this.viewId);
            if (target == null) {
                return RemoteViews.ACTION_NOOP;
            }
            TextViewDrawableAction copy;
            if (this.useIcons) {
                copy = new TextViewDrawableAction(this.this$0, this.viewId, this.isRelative, this.i1, this.i2, this.i3, this.i4);
            } else {
                copy = new TextViewDrawableAction(this.this$0, this.viewId, this.isRelative, this.d1, this.d2, this.d3, this.d4);
            }
            copy.drawablesLoaded = true;
            Context ctx = target.getContext();
            if (this.useIcons) {
                copy.id1 = this.i1 == null ? null : this.i1.loadDrawable(ctx);
                copy.id2 = this.i2 == null ? null : this.i2.loadDrawable(ctx);
                copy.id3 = this.i3 == null ? null : this.i3.loadDrawable(ctx);
                if (this.i4 != null) {
                    drawable = this.i4.loadDrawable(ctx);
                }
                copy.id4 = drawable;
            } else {
                copy.id1 = this.d1 == 0 ? null : ctx.getDrawable(this.d1);
                copy.id2 = this.d2 == 0 ? null : ctx.getDrawable(this.d2);
                copy.id3 = this.d3 == 0 ? null : ctx.getDrawable(this.d3);
                if (this.d4 != 0) {
                    drawable = ctx.getDrawable(this.d4);
                }
                copy.id4 = drawable;
            }
            return copy;
        }

        public String getActionName() {
            return "TextViewDrawableAction";
        }
    }

    private class TextViewDrawableColorFilterAction extends Action {
        public static final int TAG = 17;
        final int color;
        final int index;
        final boolean isRelative;
        final Mode mode;
        final /* synthetic */ RemoteViews this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.<init>(android.widget.RemoteViews, int, boolean, int, int, android.graphics.PorterDuff$Mode):void, dex: 
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
        public TextViewDrawableColorFilterAction(android.widget.RemoteViews r1, int r2, boolean r3, int r4, int r5, android.graphics.PorterDuff.Mode r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.<init>(android.widget.RemoteViews, int, boolean, int, int, android.graphics.PorterDuff$Mode):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.TextViewDrawableColorFilterAction.<init>(android.widget.RemoteViews, int, boolean, int, int, android.graphics.PorterDuff$Mode):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
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
        public TextViewDrawableColorFilterAction(android.widget.RemoteViews r1, android.os.Parcel r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.TextViewDrawableColorFilterAction.<init>(android.widget.RemoteViews, android.os.Parcel):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.readPorterDuffMode(android.os.Parcel):android.graphics.PorterDuff$Mode, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private android.graphics.PorterDuff.Mode readPorterDuffMode(android.os.Parcel r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.readPorterDuffMode(android.os.Parcel):android.graphics.PorterDuff$Mode, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.TextViewDrawableColorFilterAction.readPorterDuffMode(android.os.Parcel):android.graphics.PorterDuff$Mode");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.TextViewDrawableColorFilterAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.writeToParcel(android.os.Parcel, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.TextViewDrawableColorFilterAction.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.TextViewDrawableColorFilterAction.writeToParcel(android.os.Parcel, int):void");
        }

        public String getActionName() {
            return "TextViewDrawableColorFilterAction";
        }
    }

    private class TextViewSizeAction extends Action {
        public static final int TAG = 13;
        float size;
        final /* synthetic */ RemoteViews this$0;
        int units;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, int, int, float):void, dex:  in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, int, int, float):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, int, int, float):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public TextViewSizeAction(android.widget.RemoteViews r1, int r2, int r3, float r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, int, int, float):void, dex:  in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, int, int, float):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, int, int, float):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex:  in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public TextViewSizeAction(android.widget.RemoteViews r1, android.os.Parcel r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex:  in method: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, android.os.Parcel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.TextViewSizeAction.<init>(android.widget.RemoteViews, android.os.Parcel):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.TextViewSizeAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.TextViewSizeAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.TextViewSizeAction.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.TextViewSizeAction.writeToParcel(android.os.Parcel, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.TextViewSizeAction.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.TextViewSizeAction.writeToParcel(android.os.Parcel, int):void");
        }

        public String getActionName() {
            return "TextViewSizeAction";
        }
    }

    private class ViewGroupAction extends Action {
        public static final int TAG = 4;
        RemoteViews nestedViews;
        final /* synthetic */ RemoteViews this$0;

        /* renamed from: android.widget.RemoteViews$ViewGroupAction$1 */
        class AnonymousClass1 extends RuntimeAction {
            final /* synthetic */ ViewGroupAction this$1;
            final /* synthetic */ AsyncApplyTask val$task;
            final /* synthetic */ ViewTree val$tree;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ViewGroupAction.1.<init>(android.widget.RemoteViews$ViewGroupAction, android.widget.RemoteViews$AsyncApplyTask, android.widget.RemoteViews$ViewTree):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass1(android.widget.RemoteViews.ViewGroupAction r1, android.widget.RemoteViews.AsyncApplyTask r2, android.widget.RemoteViews.ViewTree r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ViewGroupAction.1.<init>(android.widget.RemoteViews$ViewGroupAction, android.widget.RemoteViews$AsyncApplyTask, android.widget.RemoteViews$ViewTree):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewGroupAction.1.<init>(android.widget.RemoteViews$ViewGroupAction, android.widget.RemoteViews$AsyncApplyTask, android.widget.RemoteViews$ViewTree):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.ViewGroupAction.1.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void apply(android.view.View r1, android.view.ViewGroup r2, android.widget.RemoteViews.OnClickHandler r3) throws android.widget.RemoteViews.ActionException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.RemoteViews.ViewGroupAction.1.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewGroupAction.1.apply(android.view.View, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):void");
            }
        }

        public ViewGroupAction(RemoteViews this$0, int viewId, RemoteViews nestedViews) {
            this.this$0 = this$0;
            super();
            this.viewId = viewId;
            this.nestedViews = nestedViews;
            if (nestedViews != null) {
                this$0.configureRemoteViewsAsChild(nestedViews);
            }
        }

        public ViewGroupAction(RemoteViews this$0, Parcel parcel, BitmapCache bitmapCache) {
            boolean nestedViewsNull = false;
            this.this$0 = this$0;
            super();
            this.viewId = parcel.readInt();
            if (parcel.readInt() == 0) {
                nestedViewsNull = true;
            }
            if (nestedViewsNull) {
                this.nestedViews = null;
            } else {
                this.nestedViews = new RemoteViews(parcel, bitmapCache, null);
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(4);
            dest.writeInt(this.viewId);
            if (this.nestedViews != null) {
                dest.writeInt(1);
                this.nestedViews.writeToParcel(dest, flags);
                return;
            }
            dest.writeInt(0);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            Context context = root.getContext();
            ViewGroup target = (ViewGroup) root.findViewById(this.viewId);
            if (target != null) {
                if (this.nestedViews != null) {
                    target.addView(this.nestedViews.apply(context, target, handler));
                } else {
                    target.removeAllViews();
                }
            }
        }

        /*  JADX ERROR: NullPointerException in pass: ModVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
            	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
            	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
            	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
            	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
            	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public android.widget.RemoteViews.Action initActionAsync(android.widget.RemoteViews.ViewTree r8, android.view.ViewGroup r9, android.widget.RemoteViews.OnClickHandler r10) {
            /*
            r7 = this;
            r6 = 0;
            r8.createTree();
            r4 = r7.viewId;
            r1 = r8.findViewTreeById(r4);
            if (r1 == 0) goto L_0x001c;
        L_0x000c:
            r4 = android.widget.RemoteViews.ViewTree.m229-get0(r1);
            r4 = r4 instanceof android.view.ViewGroup;
            if (r4 == 0) goto L_0x001c;
        L_0x0014:
            r4 = r7.nestedViews;
            if (r4 != 0) goto L_0x0021;
        L_0x0018:
            android.widget.RemoteViews.ViewTree.m230-set0(r1, r6);
            return r7;
        L_0x001c:
            r4 = android.widget.RemoteViews.ACTION_NOOP;
            return r4;
        L_0x0021:
            r4 = android.widget.RemoteViews.ViewTree.m229-get0(r8);
            r0 = r4.getContext();
            r5 = r7.nestedViews;
            r4 = android.widget.RemoteViews.ViewTree.m229-get0(r1);
            r4 = (android.view.ViewGroup) r4;
            r2 = r5.getAsyncApplyTask(r0, r4, r6, r10);
            r4 = 0;
            r4 = new java.lang.Void[r4];
            r3 = r2.doInBackground(r4);
            r1.addChild(r3);
            r4 = new android.widget.RemoteViews$ViewGroupAction$1;
            r4.<init>(r7, r2, r3);
            return r4;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewGroupAction.initActionAsync(android.widget.RemoteViews$ViewTree, android.view.ViewGroup, android.widget.RemoteViews$OnClickHandler):android.widget.RemoteViews$Action");
        }

        public void updateMemoryUsageEstimate(MemoryUsageCounter counter) {
            if (this.nestedViews != null) {
                counter.increment(this.nestedViews.estimateMemoryUsage());
            }
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
            if (this.nestedViews != null) {
                this.nestedViews.setBitmapCache(bitmapCache);
            }
        }

        public String getActionName() {
            return "ViewGroupAction" + (this.nestedViews == null ? "Remove" : "Add");
        }

        public int mergeBehavior() {
            return 1;
        }
    }

    private class ViewPaddingAction extends Action {
        public static final int TAG = 14;
        int bottom;
        int left;
        int right;
        final /* synthetic */ RemoteViews this$0;
        int top;

        public ViewPaddingAction(RemoteViews this$0, int viewId, int left, int top, int right, int bottom) {
            this.this$0 = this$0;
            super();
            this.viewId = viewId;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public ViewPaddingAction(RemoteViews this$0, Parcel parcel) {
            this.this$0 = this$0;
            super();
            this.viewId = parcel.readInt();
            this.left = parcel.readInt();
            this.top = parcel.readInt();
            this.right = parcel.readInt();
            this.bottom = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(14);
            dest.writeInt(this.viewId);
            dest.writeInt(this.left);
            dest.writeInt(this.top);
            dest.writeInt(this.right);
            dest.writeInt(this.bottom);
        }

        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            View target = root.findViewById(this.viewId);
            if (target != null) {
                target.setPadding(this.left, this.top, this.right, this.bottom);
            }
        }

        public String getActionName() {
            return "ViewPaddingAction";
        }
    }

    private static class ViewTree {
        private ArrayList<ViewTree> mChildren;
        private final View mRoot;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.-get0(android.widget.RemoteViews$ViewTree):android.view.View, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ android.view.View m229-get0(android.widget.RemoteViews.ViewTree r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.-get0(android.widget.RemoteViews$ViewTree):android.view.View, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewTree.-get0(android.widget.RemoteViews$ViewTree):android.view.View");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ViewTree.-set0(android.widget.RemoteViews$ViewTree, java.util.ArrayList):java.util.ArrayList, dex: 
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
        static /* synthetic */ java.util.ArrayList m230-set0(android.widget.RemoteViews.ViewTree r1, java.util.ArrayList r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ViewTree.-set0(android.widget.RemoteViews$ViewTree, java.util.ArrayList):java.util.ArrayList, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewTree.-set0(android.widget.RemoteViews$ViewTree, java.util.ArrayList):java.util.ArrayList");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ViewTree.<init>(android.view.View):void, dex: 
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
        private ViewTree(android.view.View r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.RemoteViews.ViewTree.<init>(android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewTree.<init>(android.view.View):void");
        }

        /* synthetic */ ViewTree(View root, ViewTree viewTree) {
            this(root);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.ViewTree.addViewChild(android.view.View):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private void addViewChild(android.view.View r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.RemoteViews.ViewTree.addViewChild(android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewTree.addViewChild(android.view.View):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.addChild(android.widget.RemoteViews$ViewTree):void, dex: 
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
        public void addChild(android.widget.RemoteViews.ViewTree r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.addChild(android.widget.RemoteViews$ViewTree):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewTree.addChild(android.widget.RemoteViews$ViewTree):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.createTree():void, dex: 
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
        public void createTree() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.createTree():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewTree.createTree():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.findViewById(int):android.view.View, dex: 
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
        public android.view.View findViewById(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.findViewById(int):android.view.View, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewTree.findViewById(int):android.view.View");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.findViewTreeById(int):android.widget.RemoteViews$ViewTree, dex: 
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
        public android.widget.RemoteViews.ViewTree findViewTreeById(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.RemoteViews.ViewTree.findViewTreeById(int):android.widget.RemoteViews$ViewTree, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.ViewTree.findViewTreeById(int):android.widget.RemoteViews$ViewTree");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.RemoteViews.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.RemoteViews.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViews.<clinit>():void");
    }

    /* synthetic */ RemoteViews(Parcel parcel, BitmapCache bitmapCache, RemoteViews remoteViews) {
        this(parcel, bitmapCache);
    }

    public void setRemoteInputs(int viewId, RemoteInput[] remoteInputs) {
        this.mActions.add(new SetRemoteInputsAction(this, viewId, remoteInputs));
    }

    public void mergeRemoteViews(RemoteViews newRv) {
        if (newRv != null) {
            int i;
            Action a;
            RemoteViews copy = newRv.clone();
            HashMap<String, Action> map = new HashMap();
            if (this.mActions == null) {
                this.mActions = new ArrayList();
            }
            int count = this.mActions.size();
            for (i = 0; i < count; i++) {
                a = (Action) this.mActions.get(i);
                map.put(a.getUniqueKey(), a);
            }
            ArrayList<Action> newActions = copy.mActions;
            if (newActions != null) {
                count = newActions.size();
                for (i = 0; i < count; i++) {
                    a = (Action) newActions.get(i);
                    String key = ((Action) newActions.get(i)).getUniqueKey();
                    int mergeBehavior = ((Action) newActions.get(i)).mergeBehavior();
                    if (map.containsKey(key) && mergeBehavior == 0) {
                        this.mActions.remove(map.get(key));
                        map.remove(key);
                    }
                    if (mergeBehavior == 0 || mergeBehavior == 1) {
                        this.mActions.add(a);
                    }
                }
                this.mBitmapCache = new BitmapCache();
                setBitmapCache(this.mBitmapCache);
            }
        }
    }

    private static Rect getSourceBounds(View v) {
        float appScale = v.getContext().getResources().getCompatibilityInfo().applicationScale;
        int[] pos = new int[2];
        v.getLocationOnScreen(pos);
        Rect rect = new Rect();
        rect.left = (int) ((((float) pos[0]) * appScale) + 0.5f);
        rect.top = (int) ((((float) pos[1]) * appScale) + 0.5f);
        rect.right = (int) ((((float) (pos[0] + v.getWidth())) * appScale) + 0.5f);
        rect.bottom = (int) ((((float) (pos[1] + v.getHeight())) * appScale) + 0.5f);
        return rect;
    }

    private Method getMethod(View view, String methodName, Class<?> paramType) {
        Method method;
        Class<? extends View> klass = view.getClass();
        synchronized (sMethodsLock) {
            ArrayMap<MutablePair<String, Class<?>>, Method> methods = (ArrayMap) sMethods.get(klass);
            if (methods == null) {
                methods = new ArrayMap();
                sMethods.put(klass, methods);
            }
            this.mPair.first = methodName;
            this.mPair.second = paramType;
            method = (Method) methods.get(this.mPair);
            if (method == null) {
                if (paramType == null) {
                    try {
                        method = klass.getMethod(methodName, new Class[0]);
                    } catch (NoSuchMethodException e) {
                        throw new ActionException("view: " + klass.getName() + " doesn't have method: " + methodName + getParameters(paramType));
                    }
                }
                Class[] clsArr = new Class[1];
                clsArr[0] = paramType;
                method = klass.getMethod(methodName, clsArr);
                if (method.isAnnotationPresent(RemotableViewMethod.class)) {
                    methods.put(new MutablePair(methodName, paramType), method);
                } else {
                    throw new ActionException("view: " + klass.getName() + " can't use method with RemoteViews: " + methodName + getParameters(paramType));
                }
            }
        }
        return method;
    }

    private Method getAsyncMethod(Method method) {
        synchronized (sAsyncMethods) {
            int valueIndex = sAsyncMethods.indexOfKey(method);
            if (valueIndex >= 0) {
                Method method2 = (Method) sAsyncMethods.valueAt(valueIndex);
                return method2;
            }
            RemotableViewMethod annotation = (RemotableViewMethod) method.getAnnotation(RemotableViewMethod.class);
            Method method3 = null;
            if (!annotation.asyncImpl().isEmpty()) {
                try {
                    method3 = method.getDeclaringClass().getMethod(annotation.asyncImpl(), method.getParameterTypes());
                    if (!method3.getReturnType().equals(Runnable.class)) {
                        throw new ActionException("Async implementation for " + method.getName() + " does not return a Runnable");
                    }
                } catch (NoSuchMethodException e) {
                    throw new ActionException("Async implementation declared but not defined for " + method.getName());
                }
            }
            sAsyncMethods.put(method, method3);
            return method3;
        }
    }

    private static String getParameters(Class<?> paramType) {
        if (paramType == null) {
            return "()";
        }
        return "(" + paramType + ")";
    }

    private static Object[] wrapArg(Object value) {
        Object[] args = (Object[]) sInvokeArgsTls.get();
        args[0] = value;
        return args;
    }

    private void configureRemoteViewsAsChild(RemoteViews rv) {
        this.mBitmapCache.assimilate(rv.mBitmapCache);
        rv.setBitmapCache(this.mBitmapCache);
        rv.setNotRoot();
    }

    void setNotRoot() {
        this.mIsRoot = false;
    }

    public RemoteViews(String packageName, int layoutId) {
        this(getApplicationInfo(packageName, UserHandle.myUserId()), layoutId);
    }

    public RemoteViews(String packageName, int userId, int layoutId) {
        this(getApplicationInfo(packageName, userId), layoutId);
    }

    protected RemoteViews(ApplicationInfo application, int layoutId) {
        this.mIsRoot = true;
        this.mLandscape = null;
        this.mPortrait = null;
        this.mIsWidgetCollectionChild = false;
        this.mPair = new MutablePair(null, null);
        this.mApplication = application;
        this.mLayoutId = layoutId;
        this.mBitmapCache = new BitmapCache();
        this.mMemoryUsageCounter = new MemoryUsageCounter(this, null);
        recalculateMemoryUsage();
    }

    private boolean hasLandscapeAndPortraitLayouts() {
        return (this.mLandscape == null || this.mPortrait == null) ? false : true;
    }

    public RemoteViews(RemoteViews landscape, RemoteViews portrait) {
        this.mIsRoot = true;
        this.mLandscape = null;
        this.mPortrait = null;
        this.mIsWidgetCollectionChild = false;
        this.mPair = new MutablePair(null, null);
        if (landscape == null || portrait == null) {
            throw new RuntimeException("Both RemoteViews must be non-null");
        } else if (landscape.mApplication.uid == portrait.mApplication.uid && landscape.mApplication.packageName.equals(portrait.mApplication.packageName)) {
            this.mApplication = portrait.mApplication;
            this.mLayoutId = portrait.getLayoutId();
            this.mLandscape = landscape;
            this.mPortrait = portrait;
            this.mMemoryUsageCounter = new MemoryUsageCounter(this, null);
            this.mBitmapCache = new BitmapCache();
            configureRemoteViewsAsChild(landscape);
            configureRemoteViewsAsChild(portrait);
            recalculateMemoryUsage();
        } else {
            throw new RuntimeException("Both RemoteViews must share the same package and user");
        }
    }

    public RemoteViews(Parcel parcel) {
        this(parcel, null);
    }

    private RemoteViews(Parcel parcel, BitmapCache bitmapCache) {
        this.mIsRoot = true;
        this.mLandscape = null;
        this.mPortrait = null;
        this.mIsWidgetCollectionChild = false;
        this.mPair = new MutablePair(null, null);
        int mode = parcel.readInt();
        if (bitmapCache == null) {
            this.mBitmapCache = new BitmapCache(parcel);
        } else {
            setBitmapCache(bitmapCache);
            setNotRoot();
        }
        if (mode == 0) {
            boolean z;
            this.mApplication = (ApplicationInfo) parcel.readParcelable(null);
            this.mLayoutId = parcel.readInt();
            if (parcel.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            this.mIsWidgetCollectionChild = z;
            int count = parcel.readInt();
            if (count > 0) {
                this.mActions = new ArrayList(count);
                for (int i = 0; i < count; i++) {
                    int tag = parcel.readInt();
                    switch (tag) {
                        case 1:
                            this.mActions.add(new SetOnClickPendingIntent(this, parcel));
                            break;
                        case 2:
                            this.mActions.add(new ReflectionAction(this, parcel));
                            break;
                        case 3:
                            this.mActions.add(new SetDrawableParameters(this, parcel));
                            break;
                        case 4:
                            this.mActions.add(new ViewGroupAction(this, parcel, this.mBitmapCache));
                            break;
                        case 5:
                            this.mActions.add(new ReflectionActionWithoutParams(this, parcel));
                            break;
                        case 6:
                            this.mActions.add(new SetEmptyView(this, parcel));
                            break;
                        case 8:
                            this.mActions.add(new SetPendingIntentTemplate(this, parcel));
                            break;
                        case 9:
                            this.mActions.add(new SetOnClickFillInIntent(this, parcel));
                            break;
                        case 10:
                            this.mActions.add(new SetRemoteViewsAdapterIntent(this, parcel));
                            break;
                        case 11:
                            this.mActions.add(new TextViewDrawableAction(this, parcel));
                            break;
                        case 12:
                            this.mActions.add(new BitmapReflectionAction(this, parcel));
                            break;
                        case 13:
                            this.mActions.add(new TextViewSizeAction(this, parcel));
                            break;
                        case 14:
                            this.mActions.add(new ViewPaddingAction(this, parcel));
                            break;
                        case 15:
                            this.mActions.add(new SetRemoteViewsAdapterList(this, parcel));
                            break;
                        case 17:
                            this.mActions.add(new TextViewDrawableColorFilterAction(this, parcel));
                            break;
                        case 18:
                            this.mActions.add(new SetRemoteInputsAction(this, parcel));
                            break;
                        case 19:
                            this.mActions.add(new LayoutParamAction(parcel));
                            break;
                        default:
                            throw new ActionException("Tag " + tag + " not found");
                    }
                }
            }
        } else {
            this.mLandscape = new RemoteViews(parcel, this.mBitmapCache);
            this.mPortrait = new RemoteViews(parcel, this.mBitmapCache);
            this.mApplication = this.mPortrait.mApplication;
            this.mLayoutId = this.mPortrait.getLayoutId();
        }
        this.mMemoryUsageCounter = new MemoryUsageCounter(this, null);
        recalculateMemoryUsage();
    }

    public /* bridge */ /* synthetic */ Object clone() throws CloneNotSupportedException {
        return clone();
    }

    public RemoteViews clone() {
        Preconditions.checkState(this.mIsRoot, "RemoteView has been attached to another RemoteView. May only clone the root of a RemoteView hierarchy.");
        Parcel p = Parcel.obtain();
        this.mIsRoot = false;
        writeToParcel(p, 0);
        p.setDataPosition(0);
        this.mIsRoot = true;
        RemoteViews rv = new RemoteViews(p, this.mBitmapCache.clone());
        rv.mIsRoot = true;
        p.recycle();
        return rv;
    }

    public String getPackage() {
        return this.mApplication != null ? this.mApplication.packageName : null;
    }

    public int getLayoutId() {
        return this.mLayoutId;
    }

    void setIsWidgetCollectionChild(boolean isWidgetCollectionChild) {
        this.mIsWidgetCollectionChild = isWidgetCollectionChild;
    }

    private void recalculateMemoryUsage() {
        this.mMemoryUsageCounter.clear();
        if (hasLandscapeAndPortraitLayouts()) {
            this.mMemoryUsageCounter.increment(this.mLandscape.estimateMemoryUsage());
            this.mMemoryUsageCounter.increment(this.mPortrait.estimateMemoryUsage());
            this.mBitmapCache.addBitmapMemory(this.mMemoryUsageCounter);
            return;
        }
        if (this.mActions != null) {
            int count = this.mActions.size();
            for (int i = 0; i < count; i++) {
                ((Action) this.mActions.get(i)).updateMemoryUsageEstimate(this.mMemoryUsageCounter);
            }
        }
        if (this.mIsRoot) {
            this.mBitmapCache.addBitmapMemory(this.mMemoryUsageCounter);
        }
    }

    private void setBitmapCache(BitmapCache bitmapCache) {
        this.mBitmapCache = bitmapCache;
        if (hasLandscapeAndPortraitLayouts()) {
            this.mLandscape.setBitmapCache(bitmapCache);
            this.mPortrait.setBitmapCache(bitmapCache);
        } else if (this.mActions != null) {
            int count = this.mActions.size();
            for (int i = 0; i < count; i++) {
                ((Action) this.mActions.get(i)).setBitmapCache(bitmapCache);
            }
        }
    }

    public int estimateMemoryUsage() {
        return this.mMemoryUsageCounter.getMemoryUsage();
    }

    private void addAction(Action a) {
        if (hasLandscapeAndPortraitLayouts()) {
            throw new RuntimeException("RemoteViews specifying separate landscape and portrait layouts cannot be modified. Instead, fully configure the landscape and portrait layouts individually before constructing the combined layout.");
        }
        if (this.mActions == null) {
            this.mActions = new ArrayList();
        }
        this.mActions.add(a);
        a.updateMemoryUsageEstimate(this.mMemoryUsageCounter);
    }

    public void addView(int viewId, RemoteViews nestedView) {
        addAction(new ViewGroupAction(this, viewId, nestedView));
    }

    public void removeAllViews(int viewId) {
        addAction(new ViewGroupAction(this, viewId, null));
    }

    public void showNext(int viewId) {
        addAction(new ReflectionActionWithoutParams(this, viewId, "showNext"));
    }

    public void showPrevious(int viewId) {
        addAction(new ReflectionActionWithoutParams(this, viewId, "showPrevious"));
    }

    public void setDisplayedChild(int viewId, int childIndex) {
        setInt(viewId, "setDisplayedChild", childIndex);
    }

    public void setViewVisibility(int viewId, int visibility) {
        setInt(viewId, "setVisibility", visibility);
    }

    public void setTextViewText(int viewId, CharSequence text) {
        setCharSequence(viewId, "setText", text);
    }

    public void setTextViewTextSize(int viewId, int units, float size) {
        addAction(new TextViewSizeAction(this, viewId, units, size));
    }

    public void setTextViewCompoundDrawables(int viewId, int left, int top, int right, int bottom) {
        addAction(new TextViewDrawableAction(this, viewId, false, left, top, right, bottom));
    }

    public void setTextViewCompoundDrawablesRelative(int viewId, int start, int top, int end, int bottom) {
        addAction(new TextViewDrawableAction(this, viewId, true, start, top, end, bottom));
    }

    public void setTextViewCompoundDrawablesRelativeColorFilter(int viewId, int index, int color, Mode mode) {
        if (index < 0 || index >= 4) {
            throw new IllegalArgumentException("index must be in range [0, 3].");
        }
        addAction(new TextViewDrawableColorFilterAction(this, viewId, true, index, color, mode));
    }

    public void setTextViewCompoundDrawables(int viewId, Icon left, Icon top, Icon right, Icon bottom) {
        addAction(new TextViewDrawableAction(this, viewId, false, left, top, right, bottom));
    }

    public void setTextViewCompoundDrawablesRelative(int viewId, Icon start, Icon top, Icon end, Icon bottom) {
        addAction(new TextViewDrawableAction(this, viewId, true, start, top, end, bottom));
    }

    public void setImageViewResource(int viewId, int srcId) {
        setInt(viewId, "setImageResource", srcId);
    }

    public void setImageViewUri(int viewId, Uri uri) {
        setUri(viewId, "setImageURI", uri);
    }

    public void setImageViewBitmap(int viewId, Bitmap bitmap) {
        setBitmap(viewId, "setImageBitmap", bitmap);
    }

    public void setImageViewIcon(int viewId, Icon icon) {
        setIcon(viewId, "setImageIcon", icon);
    }

    public void setEmptyView(int viewId, int emptyViewId) {
        addAction(new SetEmptyView(this, viewId, emptyViewId));
    }

    public void setChronometer(int viewId, long base, String format, boolean started) {
        setLong(viewId, "setBase", base);
        setString(viewId, "setFormat", format);
        setBoolean(viewId, "setStarted", started);
    }

    public void setChronometerCountDown(int viewId, boolean isCountDown) {
        setBoolean(viewId, "setCountDown", isCountDown);
    }

    public void setProgressBar(int viewId, int max, int progress, boolean indeterminate) {
        setBoolean(viewId, "setIndeterminate", indeterminate);
        if (!indeterminate) {
            setInt(viewId, "setMax", max);
            setInt(viewId, "setProgress", progress);
        }
    }

    public void setOnClickPendingIntent(int viewId, PendingIntent pendingIntent) {
        addAction(new SetOnClickPendingIntent(this, viewId, pendingIntent));
    }

    public void setPendingIntentTemplate(int viewId, PendingIntent pendingIntentTemplate) {
        addAction(new SetPendingIntentTemplate(this, viewId, pendingIntentTemplate));
    }

    public void setOnClickFillInIntent(int viewId, Intent fillInIntent) {
        addAction(new SetOnClickFillInIntent(this, viewId, fillInIntent));
    }

    public void setDrawableParameters(int viewId, boolean targetBackground, int alpha, int colorFilter, Mode mode, int level) {
        addAction(new SetDrawableParameters(this, viewId, targetBackground, alpha, colorFilter, mode, level));
    }

    public void setProgressTintList(int viewId, ColorStateList tint) {
        addAction(new ReflectionAction(this, viewId, "setProgressTintList", 15, tint));
    }

    public void setProgressBackgroundTintList(int viewId, ColorStateList tint) {
        addAction(new ReflectionAction(this, viewId, "setProgressBackgroundTintList", 15, tint));
    }

    public void setProgressIndeterminateTintList(int viewId, ColorStateList tint) {
        addAction(new ReflectionAction(this, viewId, "setIndeterminateTintList", 15, tint));
    }

    public void setTextColor(int viewId, int color) {
        setInt(viewId, "setTextColor", color);
    }

    public void setTextColor(int viewId, ColorStateList colors) {
        addAction(new ReflectionAction(this, viewId, "setTextColor", 15, colors));
    }

    @Deprecated
    public void setRemoteAdapter(int appWidgetId, int viewId, Intent intent) {
        setRemoteAdapter(viewId, intent);
    }

    public void setRemoteAdapter(int viewId, Intent intent) {
        addAction(new SetRemoteViewsAdapterIntent(this, viewId, intent));
    }

    public void setRemoteAdapter(int viewId, ArrayList<RemoteViews> list, int viewTypeCount) {
        addAction(new SetRemoteViewsAdapterList(this, viewId, list, viewTypeCount));
    }

    public void setScrollPosition(int viewId, int position) {
        setInt(viewId, "smoothScrollToPosition", position);
    }

    public void setRelativeScrollPosition(int viewId, int offset) {
        setInt(viewId, "smoothScrollByOffset", offset);
    }

    public void setViewPadding(int viewId, int left, int top, int right, int bottom) {
        addAction(new ViewPaddingAction(this, viewId, left, top, right, bottom));
    }

    public void setViewLayoutMarginEndDimen(int viewId, int endMarginDimen) {
        addAction(new LayoutParamAction(viewId, 1, endMarginDimen));
    }

    public void setViewLayoutMarginBottomDimen(int viewId, int bottomMarginDimen) {
        addAction(new LayoutParamAction(viewId, 3, bottomMarginDimen));
    }

    public void setViewLayoutWidth(int viewId, int layoutWidth) {
        if (layoutWidth == 0 || layoutWidth == -1 || layoutWidth == -2) {
            this.mActions.add(new LayoutParamAction(viewId, 2, layoutWidth));
            return;
        }
        throw new IllegalArgumentException("Only supports 0, WRAP_CONTENT and MATCH_PARENT");
    }

    public void setBoolean(int viewId, String methodName, boolean value) {
        addAction(new ReflectionAction(this, viewId, methodName, 1, Boolean.valueOf(value)));
    }

    public void setByte(int viewId, String methodName, byte value) {
        addAction(new ReflectionAction(this, viewId, methodName, 2, Byte.valueOf(value)));
    }

    public void setShort(int viewId, String methodName, short value) {
        addAction(new ReflectionAction(this, viewId, methodName, 3, Short.valueOf(value)));
    }

    public void setInt(int viewId, String methodName, int value) {
        addAction(new ReflectionAction(this, viewId, methodName, 4, Integer.valueOf(value)));
    }

    public void setLong(int viewId, String methodName, long value) {
        addAction(new ReflectionAction(this, viewId, methodName, 5, Long.valueOf(value)));
    }

    public void setFloat(int viewId, String methodName, float value) {
        addAction(new ReflectionAction(this, viewId, methodName, 6, Float.valueOf(value)));
    }

    public void setDouble(int viewId, String methodName, double value) {
        addAction(new ReflectionAction(this, viewId, methodName, 7, Double.valueOf(value)));
    }

    public void setChar(int viewId, String methodName, char value) {
        addAction(new ReflectionAction(this, viewId, methodName, 8, Character.valueOf(value)));
    }

    public void setString(int viewId, String methodName, String value) {
        addAction(new ReflectionAction(this, viewId, methodName, 9, value));
    }

    public void setCharSequence(int viewId, String methodName, CharSequence value) {
        addAction(new ReflectionAction(this, viewId, methodName, 10, value));
    }

    public void setUri(int viewId, String methodName, Uri value) {
        if (value != null) {
            value = value.getCanonicalUri();
            if (StrictMode.vmFileUriExposureEnabled()) {
                value.checkFileUriExposed("RemoteViews.setUri()");
            }
        }
        addAction(new ReflectionAction(this, viewId, methodName, 11, value));
    }

    public void setBitmap(int viewId, String methodName, Bitmap value) {
        addAction(new BitmapReflectionAction(this, viewId, methodName, value));
    }

    public void setBundle(int viewId, String methodName, Bundle value) {
        addAction(new ReflectionAction(this, viewId, methodName, 13, value));
    }

    public void setIntent(int viewId, String methodName, Intent value) {
        addAction(new ReflectionAction(this, viewId, methodName, 14, value));
    }

    public void setIcon(int viewId, String methodName, Icon value) {
        addAction(new ReflectionAction(this, viewId, methodName, 16, value));
    }

    public void setContentDescription(int viewId, CharSequence contentDescription) {
        setCharSequence(viewId, "setContentDescription", contentDescription);
    }

    public void setAccessibilityTraversalBefore(int viewId, int nextId) {
        setInt(viewId, "setAccessibilityTraversalBefore", nextId);
    }

    public void setAccessibilityTraversalAfter(int viewId, int nextId) {
        setInt(viewId, "setAccessibilityTraversalAfter", nextId);
    }

    public void setLabelFor(int viewId, int labeledId) {
        setInt(viewId, "setLabelFor", labeledId);
    }

    private RemoteViews getRemoteViewsToApply(Context context) {
        if (!hasLandscapeAndPortraitLayouts()) {
            return this;
        }
        if (context.getResources().getConfiguration().orientation == 2) {
            return this.mLandscape;
        }
        return this.mPortrait;
    }

    public View apply(Context context, ViewGroup parent) {
        return apply(context, parent, null);
    }

    public View apply(Context context, ViewGroup parent, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);
        View result = inflateView(context, rvToApply, parent);
        loadTransitionOverride(context, handler);
        rvToApply.performApply(result, parent, handler);
        return result;
    }

    private View inflateView(Context context, RemoteViews rv, ViewGroup parent) {
        final Context contextForResources = getContextForResources(context);
        LayoutInflater inflater = ((LayoutInflater) context.getSystemService("layout_inflater")).cloneInContext(new ContextWrapper(context) {
            public Resources getResources() {
                return contextForResources.getResources();
            }

            public Theme getTheme() {
                return contextForResources.getTheme();
            }

            public String getPackageName() {
                return contextForResources.getPackageName();
            }
        });
        inflater.setFilter(this);
        View v = inflater.inflate(rv.getLayoutId(), parent, false);
        v.setTagInternal(R.id.widget_frame, Integer.valueOf(rv.getLayoutId()));
        return v;
    }

    private static void loadTransitionOverride(Context context, OnClickHandler handler) {
        if (handler != null && context.getResources().getBoolean(R.bool.config_overrideRemoteViewsActivityTransition)) {
            TypedArray windowStyle = context.getTheme().obtainStyledAttributes(R.styleable.Window);
            TypedArray windowAnimationStyle = context.obtainStyledAttributes(windowStyle.getResourceId(8, 0), R.styleable.WindowAnimation);
            handler.setEnterAnimationId(windowAnimationStyle.getResourceId(26, 0));
            windowStyle.recycle();
            windowAnimationStyle.recycle();
        }
    }

    public CancellationSignal applyAsync(Context context, ViewGroup parent, Executor executor, OnViewAppliedListener listener) {
        return applyAsync(context, parent, executor, listener, null);
    }

    private CancellationSignal startTaskOnExecutor(AsyncApplyTask task, Executor executor) {
        CancellationSignal cancelSignal = new CancellationSignal();
        cancelSignal.setOnCancelListener(task);
        if (executor == null) {
            executor = AsyncTask.THREAD_POOL_EXECUTOR;
        }
        task.executeOnExecutor(executor, new Void[0]);
        return cancelSignal;
    }

    public CancellationSignal applyAsync(Context context, ViewGroup parent, Executor executor, OnViewAppliedListener listener, OnClickHandler handler) {
        return startTaskOnExecutor(getAsyncApplyTask(context, parent, listener, handler), executor);
    }

    private AsyncApplyTask getAsyncApplyTask(Context context, ViewGroup parent, OnViewAppliedListener listener, OnClickHandler handler) {
        return new AsyncApplyTask(this, getRemoteViewsToApply(context), parent, context, listener, handler, null, null);
    }

    public void reapply(Context context, View v) {
        reapply(context, v, null);
    }

    public void reapply(Context context, View v, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);
        if (!hasLandscapeAndPortraitLayouts() || ((Integer) v.getTag(R.id.widget_frame)).intValue() == rvToApply.getLayoutId()) {
            rvToApply.performApply(v, (ViewGroup) v.getParent(), handler);
            return;
        }
        throw new RuntimeException("Attempting to re-apply RemoteViews to a view that that does not share the same root layout id.");
    }

    public CancellationSignal reapplyAsync(Context context, View v, Executor executor, OnViewAppliedListener listener) {
        return reapplyAsync(context, v, executor, listener, null);
    }

    public CancellationSignal reapplyAsync(Context context, View v, Executor executor, OnViewAppliedListener listener, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);
        if (!hasLandscapeAndPortraitLayouts() || ((Integer) v.getTag(R.id.widget_frame)).intValue() == rvToApply.getLayoutId()) {
            return startTaskOnExecutor(new AsyncApplyTask(this, rvToApply, (ViewGroup) v.getParent(), context, listener, handler, v, null), executor);
        }
        throw new RuntimeException("Attempting to re-apply RemoteViews to a view that that does not share the same root layout id.");
    }

    private void performApply(View v, ViewGroup parent, OnClickHandler handler) {
        if (this.mActions != null) {
            if (handler == null) {
                handler = DEFAULT_ON_CLICK_HANDLER;
            }
            int count = this.mActions.size();
            for (int i = 0; i < count; i++) {
                ((Action) this.mActions.get(i)).apply(v, parent, handler);
            }
        }
    }

    private Context getContextForResources(Context context) {
        if (this.mApplication != null) {
            if (context.getUserId() == UserHandle.getUserId(this.mApplication.uid) && context.getPackageName().equals(this.mApplication.packageName)) {
                return context;
            }
            try {
                return context.createApplicationContext(this.mApplication, 4);
            } catch (NameNotFoundException e) {
                Log.e(LOG_TAG, "Package name " + this.mApplication.packageName + " not found");
            }
        }
        return context;
    }

    public int getSequenceNumber() {
        return this.mActions == null ? 0 : this.mActions.size();
    }

    public boolean onLoadClass(Class clazz) {
        return clazz.isAnnotationPresent(RemoteView.class);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        if (hasLandscapeAndPortraitLayouts()) {
            dest.writeInt(1);
            if (this.mIsRoot) {
                this.mBitmapCache.writeBitmapsToParcel(dest, flags);
            }
            this.mLandscape.writeToParcel(dest, flags);
            this.mPortrait.writeToParcel(dest, flags);
            return;
        }
        int count;
        dest.writeInt(0);
        if (this.mIsRoot) {
            this.mBitmapCache.writeBitmapsToParcel(dest, flags);
        }
        dest.writeParcelable(this.mApplication, flags);
        dest.writeInt(this.mLayoutId);
        if (!this.mIsWidgetCollectionChild) {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mActions != null) {
            count = this.mActions.size();
        } else {
            count = 0;
        }
        dest.writeInt(count);
        for (int i2 = 0; i2 < count; i2++) {
            ((Action) this.mActions.get(i2)).writeToParcel(dest, 0);
        }
    }

    private static ApplicationInfo getApplicationInfo(String packageName, int userId) {
        if (packageName == null) {
            return null;
        }
        Application application = ActivityThread.currentApplication();
        if (application == null) {
            throw new IllegalStateException("Cannot create remote views out of an aplication.");
        }
        ApplicationInfo applicationInfo = application.getApplicationInfo();
        if (!(UserHandle.getUserId(applicationInfo.uid) == userId && applicationInfo.packageName.equals(packageName))) {
            try {
                applicationInfo = application.getBaseContext().createPackageContextAsUser(packageName, 0, new UserHandle(userId)).getApplicationInfo();
            } catch (NameNotFoundException e) {
                throw new IllegalArgumentException("No such package " + packageName);
            }
        }
        return applicationInfo;
    }
}
