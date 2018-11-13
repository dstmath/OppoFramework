package android.graphics.drawable;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

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
public final class Icon implements Parcelable {
    public static final Creator<Icon> CREATOR = null;
    static final Mode DEFAULT_TINT_MODE = null;
    public static final int MIN_ASHMEM_ICON_SIZE = 131072;
    private static final String TAG = "Icon";
    public static final int TYPE_BITMAP = 1;
    public static final int TYPE_DATA = 3;
    public static final int TYPE_RESOURCE = 2;
    public static final int TYPE_URI = 4;
    private static final int VERSION_STREAM_SERIALIZER = 1;
    private int mInt1;
    private int mInt2;
    private Object mObj1;
    private String mString1;
    private ColorStateList mTintList;
    private Mode mTintMode;
    private final int mType;

    private class LoadDrawableTask implements Runnable {
        final Context mContext;
        final Message mMessage;
        final /* synthetic */ Icon this$0;

        /* renamed from: android.graphics.drawable.Icon$LoadDrawableTask$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ LoadDrawableTask this$1;
            final /* synthetic */ OnDrawableLoadedListener val$listener;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.graphics.drawable.Icon.LoadDrawableTask.1.<init>(android.graphics.drawable.Icon$LoadDrawableTask, android.graphics.drawable.Icon$OnDrawableLoadedListener):void, dex: 
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
            AnonymousClass1(android.graphics.drawable.Icon.LoadDrawableTask r1, android.graphics.drawable.Icon.OnDrawableLoadedListener r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.graphics.drawable.Icon.LoadDrawableTask.1.<init>(android.graphics.drawable.Icon$LoadDrawableTask, android.graphics.drawable.Icon$OnDrawableLoadedListener):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.Icon.LoadDrawableTask.1.<init>(android.graphics.drawable.Icon$LoadDrawableTask, android.graphics.drawable.Icon$OnDrawableLoadedListener):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.graphics.drawable.Icon.LoadDrawableTask.1.run():void, dex: 
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
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.graphics.drawable.Icon.LoadDrawableTask.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.Icon.LoadDrawableTask.1.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.graphics.drawable.Icon.LoadDrawableTask.<init>(android.graphics.drawable.Icon, android.content.Context, android.os.Handler, android.graphics.drawable.Icon$OnDrawableLoadedListener):void, dex: 
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
        public LoadDrawableTask(android.graphics.drawable.Icon r1, android.content.Context r2, android.os.Handler r3, android.graphics.drawable.Icon.OnDrawableLoadedListener r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.graphics.drawable.Icon.LoadDrawableTask.<init>(android.graphics.drawable.Icon, android.content.Context, android.os.Handler, android.graphics.drawable.Icon$OnDrawableLoadedListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.Icon.LoadDrawableTask.<init>(android.graphics.drawable.Icon, android.content.Context, android.os.Handler, android.graphics.drawable.Icon$OnDrawableLoadedListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.graphics.drawable.Icon.LoadDrawableTask.<init>(android.graphics.drawable.Icon, android.content.Context, android.os.Message):void, dex: 
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
        public LoadDrawableTask(android.graphics.drawable.Icon r1, android.content.Context r2, android.os.Message r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.graphics.drawable.Icon.LoadDrawableTask.<init>(android.graphics.drawable.Icon, android.content.Context, android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.Icon.LoadDrawableTask.<init>(android.graphics.drawable.Icon, android.content.Context, android.os.Message):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.graphics.drawable.Icon.LoadDrawableTask.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.graphics.drawable.Icon.LoadDrawableTask.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.Icon.LoadDrawableTask.run():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.drawable.Icon.LoadDrawableTask.runAsync():void, dex: 
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
        public void runAsync() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.drawable.Icon.LoadDrawableTask.runAsync():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.Icon.LoadDrawableTask.runAsync():void");
        }
    }

    public interface OnDrawableLoadedListener {
        void onDrawableLoaded(Drawable drawable);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.drawable.Icon.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.drawable.Icon.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.Icon.<clinit>():void");
    }

    /* synthetic */ Icon(Parcel in, Icon icon) {
        this(in);
    }

    public int getType() {
        return this.mType;
    }

    public Bitmap getBitmap() {
        if (this.mType == 1) {
            return (Bitmap) this.mObj1;
        }
        throw new IllegalStateException("called getBitmap() on " + this);
    }

    private void setBitmap(Bitmap b) {
        this.mObj1 = b;
    }

    public int getDataLength() {
        if (this.mType != 3) {
            throw new IllegalStateException("called getDataLength() on " + this);
        }
        int i;
        synchronized (this) {
            i = this.mInt1;
        }
        return i;
    }

    public int getDataOffset() {
        if (this.mType != 3) {
            throw new IllegalStateException("called getDataOffset() on " + this);
        }
        int i;
        synchronized (this) {
            i = this.mInt2;
        }
        return i;
    }

    public byte[] getDataBytes() {
        if (this.mType != 3) {
            throw new IllegalStateException("called getDataBytes() on " + this);
        }
        byte[] bArr;
        synchronized (this) {
            bArr = (byte[]) this.mObj1;
        }
        return bArr;
    }

    public Resources getResources() {
        if (this.mType == 2) {
            return (Resources) this.mObj1;
        }
        throw new IllegalStateException("called getResources() on " + this);
    }

    public String getResPackage() {
        if (this.mType == 2) {
            return this.mString1;
        }
        throw new IllegalStateException("called getResPackage() on " + this);
    }

    public int getResId() {
        if (this.mType == 2) {
            return this.mInt1;
        }
        throw new IllegalStateException("called getResId() on " + this);
    }

    public String getUriString() {
        if (this.mType == 4) {
            return this.mString1;
        }
        throw new IllegalStateException("called getUriString() on " + this);
    }

    public Uri getUri() {
        return Uri.parse(getUriString());
    }

    private static final String typeToString(int x) {
        switch (x) {
            case 1:
                return "BITMAP";
            case 2:
                return "RESOURCE";
            case 3:
                return "DATA";
            case 4:
                return "URI";
            default:
                return "UNKNOWN";
        }
    }

    public void loadDrawableAsync(Context context, Message andThen) {
        if (andThen.getTarget() == null) {
            throw new IllegalArgumentException("callback message must have a target handler");
        }
        new LoadDrawableTask(this, context, andThen).runAsync();
    }

    public void loadDrawableAsync(Context context, OnDrawableLoadedListener listener, Handler handler) {
        new LoadDrawableTask(this, context, handler, listener).runAsync();
    }

    public Drawable loadDrawable(Context context) {
        Drawable result = loadDrawableInner(context);
        if (!(result == null || (this.mTintList == null && this.mTintMode == DEFAULT_TINT_MODE))) {
            result.mutate();
            result.setTintList(this.mTintList);
            result.setTintMode(this.mTintMode);
        }
        return result;
    }

    private Drawable loadDrawableInner(Context context) {
        String str;
        Object[] objArr;
        switch (this.mType) {
            case 1:
                return new BitmapDrawable(context.getResources(), getBitmap());
            case 2:
                if (getResources() == null) {
                    String resPackage = getResPackage();
                    if (TextUtils.isEmpty(resPackage)) {
                        resPackage = context.getPackageName();
                    }
                    if (!ZenModeConfig.SYSTEM_AUTHORITY.equals(resPackage)) {
                        PackageManager pm = context.getPackageManager();
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(resPackage, 8192);
                            if (ai != null) {
                                this.mObj1 = pm.getResourcesForApplication(ai);
                            }
                        } catch (NameNotFoundException e) {
                            str = TAG;
                            objArr = new Object[2];
                            objArr[0] = resPackage;
                            objArr[1] = this;
                            Log.e(str, String.format("Unable to find pkg=%s for icon %s", objArr), e);
                            break;
                        }
                    }
                    this.mObj1 = Resources.getSystem();
                }
                try {
                    return getResources().getDrawable(getResId(), context.getTheme());
                } catch (RuntimeException e2) {
                    str = TAG;
                    objArr = new Object[2];
                    objArr[0] = Integer.valueOf(getResId());
                    objArr[1] = getResPackage();
                    Log.e(str, String.format("Unable to load resource 0x%08x from pkg=%s", objArr), e2);
                    break;
                }
                break;
            case 3:
                return new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(getDataBytes(), getDataOffset(), getDataLength()));
            case 4:
                Uri uri = getUri();
                String scheme = uri.getScheme();
                InputStream is = null;
                if ("content".equals(scheme) || "file".equals(scheme)) {
                    try {
                        is = context.getContentResolver().openInputStream(uri);
                    } catch (Exception e3) {
                        Log.w(TAG, "Unable to load image from URI: " + uri, e3);
                    }
                } else {
                    try {
                        is = new FileInputStream(new File(this.mString1));
                    } catch (FileNotFoundException e4) {
                        Log.w(TAG, "Unable to load image from path: " + uri, e4);
                    }
                }
                if (is != null) {
                    return new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is));
                }
                break;
        }
        return null;
    }

    public Drawable loadDrawableAsUser(Context context, int userId) {
        if (this.mType == 2) {
            String resPackage = getResPackage();
            if (TextUtils.isEmpty(resPackage)) {
                resPackage = context.getPackageName();
            }
            if (getResources() == null && !getResPackage().equals(ZenModeConfig.SYSTEM_AUTHORITY)) {
                try {
                    this.mObj1 = context.getPackageManager().getResourcesForApplicationAsUser(resPackage, userId);
                } catch (NameNotFoundException e) {
                    String str = TAG;
                    Object[] objArr = new Object[2];
                    objArr[0] = getResPackage();
                    objArr[1] = Integer.valueOf(userId);
                    Log.e(str, String.format("Unable to find pkg=%s user=%d", objArr), e);
                }
            }
        }
        return loadDrawable(context);
    }

    public void convertToAshmem() {
        if (this.mType == 1 && getBitmap().isMutable() && getBitmap().getAllocationByteCount() >= 131072) {
            setBitmap(getBitmap().createAshmemBitmap());
        }
    }

    public void writeToStream(OutputStream stream) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(stream);
        dataStream.writeInt(1);
        dataStream.writeByte(this.mType);
        switch (this.mType) {
            case 1:
                getBitmap().compress(CompressFormat.PNG, 100, dataStream);
                return;
            case 2:
                dataStream.writeUTF(getResPackage());
                dataStream.writeInt(getResId());
                return;
            case 3:
                dataStream.writeInt(getDataLength());
                dataStream.write(getDataBytes(), getDataOffset(), getDataLength());
                return;
            case 4:
                dataStream.writeUTF(getUriString());
                return;
            default:
                return;
        }
    }

    private Icon(int mType) {
        this.mTintMode = DEFAULT_TINT_MODE;
        this.mType = mType;
    }

    public static Icon createFromStream(InputStream stream) throws IOException {
        DataInputStream inputStream = new DataInputStream(stream);
        if (inputStream.readInt() >= 1) {
            switch (inputStream.readByte()) {
                case 1:
                    return createWithBitmap(BitmapFactory.decodeStream(inputStream));
                case 2:
                    return createWithResource(inputStream.readUTF(), inputStream.readInt());
                case 3:
                    int length = inputStream.readInt();
                    byte[] data = new byte[length];
                    inputStream.read(data, 0, length);
                    return createWithData(data, 0, length);
                case 4:
                    return createWithContentUri(inputStream.readUTF());
            }
        }
        return null;
    }

    public boolean sameAs(Icon otherIcon) {
        boolean z = true;
        boolean z2 = false;
        if (otherIcon == this) {
            return true;
        }
        if (this.mType != otherIcon.getType()) {
            return false;
        }
        switch (this.mType) {
            case 1:
                if (getBitmap() != otherIcon.getBitmap()) {
                    z = false;
                }
                return z;
            case 2:
                if (getResId() == otherIcon.getResId()) {
                    z2 = Objects.equals(getResPackage(), otherIcon.getResPackage());
                }
                return z2;
            case 3:
                if (getDataLength() != otherIcon.getDataLength() || getDataOffset() != otherIcon.getDataOffset()) {
                    z = false;
                } else if (getDataBytes() != otherIcon.getDataBytes()) {
                    z = false;
                }
                return z;
            case 4:
                return Objects.equals(getUriString(), otherIcon.getUriString());
            default:
                return false;
        }
    }

    public static Icon createWithResource(Context context, int resId) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        Icon rep = new Icon(2);
        rep.mInt1 = resId;
        rep.mString1 = context.getPackageName();
        return rep;
    }

    public static Icon createWithResource(Resources res, int resId) {
        if (res == null) {
            throw new IllegalArgumentException("Resource must not be null.");
        }
        Icon rep = new Icon(2);
        rep.mInt1 = resId;
        rep.mString1 = res.getResourcePackageName(resId);
        return rep;
    }

    public static Icon createWithResource(String resPackage, int resId) {
        if (resPackage == null) {
            throw new IllegalArgumentException("Resource package name must not be null.");
        }
        Icon rep = new Icon(2);
        rep.mInt1 = resId;
        rep.mString1 = resPackage;
        return rep;
    }

    public static Icon createWithBitmap(Bitmap bits) {
        if (bits == null) {
            throw new IllegalArgumentException("Bitmap must not be null.");
        }
        Icon rep = new Icon(1);
        rep.setBitmap(bits);
        return rep;
    }

    public static Icon createWithData(byte[] data, int offset, int length) {
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null.");
        }
        Icon rep = new Icon(3);
        rep.mObj1 = data;
        rep.mInt1 = length;
        rep.mInt2 = offset;
        return rep;
    }

    public static Icon createWithContentUri(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Uri must not be null.");
        }
        Icon rep = new Icon(4);
        rep.mString1 = uri;
        return rep;
    }

    public static Icon createWithContentUri(Uri uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Uri must not be null.");
        }
        Icon rep = new Icon(4);
        rep.mString1 = uri.toString();
        return rep;
    }

    public Icon setTint(int tint) {
        return setTintList(ColorStateList.valueOf(tint));
    }

    public Icon setTintList(ColorStateList tintList) {
        this.mTintList = tintList;
        return this;
    }

    public Icon setTintMode(Mode mode) {
        this.mTintMode = mode;
        return this;
    }

    public boolean hasTint() {
        return (this.mTintList == null && this.mTintMode == DEFAULT_TINT_MODE) ? false : true;
    }

    public static Icon createWithFilePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null.");
        }
        Icon rep = new Icon(4);
        rep.mString1 = path;
        return rep;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Icon(typ=").append(typeToString(this.mType));
        switch (this.mType) {
            case 1:
                sb.append(" size=").append(getBitmap().getWidth()).append("x").append(getBitmap().getHeight());
                break;
            case 2:
                StringBuilder append = sb.append(" pkg=").append(getResPackage()).append(" id=");
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(getResId());
                append.append(String.format("0x%08x", objArr));
                break;
            case 3:
                sb.append(" len=").append(getDataLength());
                if (getDataOffset() != 0) {
                    sb.append(" off=").append(getDataOffset());
                    break;
                }
                break;
            case 4:
                sb.append(" uri=").append(getUriString());
                break;
        }
        if (this.mTintList != null) {
            sb.append(" tint=");
            String sep = "";
            for (int c : this.mTintList.getColors()) {
                Object[] objArr2 = new Object[2];
                objArr2[0] = sep;
                objArr2[1] = Integer.valueOf(c);
                sb.append(String.format("%s0x%08x", objArr2));
                sep = "|";
            }
        }
        if (this.mTintMode != DEFAULT_TINT_MODE) {
            sb.append(" mode=").append(this.mTintMode);
        }
        sb.append(")");
        return sb.toString();
    }

    public int describeContents() {
        if (this.mType == 1 || this.mType == 3) {
            return 1;
        }
        return 0;
    }

    private Icon(Parcel in) {
        this(in.readInt());
        switch (this.mType) {
            case 1:
                this.mObj1 = (Bitmap) Bitmap.CREATOR.createFromParcel(in);
                break;
            case 2:
                String pkg = in.readString();
                int resId = in.readInt();
                this.mString1 = pkg;
                this.mInt1 = resId;
                break;
            case 3:
                int len = in.readInt();
                byte[] a = in.readBlob();
                if (len == a.length) {
                    this.mInt1 = len;
                    this.mObj1 = a;
                    break;
                }
                throw new RuntimeException("internal unparceling error: blob length (" + a.length + ") != expected length (" + len + ")");
            case 4:
                this.mString1 = in.readString();
                break;
            default:
                throw new RuntimeException("invalid " + getClass().getSimpleName() + " type in parcel: " + this.mType);
        }
        if (in.readInt() == 1) {
            this.mTintList = (ColorStateList) ColorStateList.CREATOR.createFromParcel(in);
        }
        this.mTintMode = PorterDuff.intToMode(in.readInt());
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        switch (this.mType) {
            case 1:
                Bitmap bits = getBitmap();
                getBitmap().writeToParcel(dest, flags);
                break;
            case 2:
                dest.writeString(getResPackage());
                dest.writeInt(getResId());
                break;
            case 3:
                dest.writeInt(getDataLength());
                dest.writeBlob(getDataBytes(), getDataOffset(), getDataLength());
                break;
            case 4:
                dest.writeString(getUriString());
                break;
        }
        if (this.mTintList == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.mTintList.writeToParcel(dest, flags);
        }
        dest.writeInt(PorterDuff.modeToInt(this.mTintMode));
    }
}
