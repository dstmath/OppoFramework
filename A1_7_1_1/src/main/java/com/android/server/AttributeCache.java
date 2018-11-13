package com.android.server;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.LruCache;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;

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
public final class AttributeCache {
    private static final int CACHE_SIZE = 4;
    private static AttributeCache sInstance;
    @GuardedBy("this")
    private final Configuration mConfiguration;
    private final Context mContext;
    @GuardedBy("this")
    private final LruCache<String, Package> mPackages;

    public static final class Entry {
        public final TypedArray array;
        public final Context context;

        public Entry(Context c, TypedArray ta) {
            this.context = c;
            this.array = ta;
        }

        void recycle() {
            if (this.array != null) {
                this.array.recycle();
            }
        }
    }

    public static final class Package {
        public final Context context;
        private final SparseArray<ArrayMap<int[], Entry>> mMap = new SparseArray();

        public Package(Context c) {
            this.context = c;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.AttributeCache.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.AttributeCache.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.AttributeCache.<clinit>():void");
    }

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new AttributeCache(context);
        }
    }

    public static AttributeCache instance() {
        return sInstance;
    }

    public AttributeCache(Context context) {
        this.mPackages = new LruCache(4);
        this.mConfiguration = new Configuration();
        this.mContext = context;
    }

    public void removePackage(String packageName) {
        synchronized (this) {
            Package pkg = (Package) this.mPackages.remove(packageName);
            if (pkg != null) {
                for (int i = 0; i < pkg.mMap.size(); i++) {
                    ArrayMap<int[], Entry> map = (ArrayMap) pkg.mMap.valueAt(i);
                    for (int j = 0; j < map.size(); j++) {
                        ((Entry) map.valueAt(j)).recycle();
                    }
                }
                pkg.context.getResources().flushLayoutCache();
            }
        }
    }

    public void updateConfiguration(Configuration config) {
        synchronized (this) {
            if ((-1073741985 & this.mConfiguration.updateFrom(config)) != 0) {
                this.mPackages.evictAll();
            }
        }
    }

    public Entry get(String packageName, int resId, int[] styleable, int userId) {
        Entry ent;
        synchronized (this) {
            Package pkg = (Package) this.mPackages.get(packageName);
            ArrayMap map = null;
            Entry ent2 = null;
            if (pkg != null) {
                map = (ArrayMap) pkg.mMap.get(resId);
                if (map != null) {
                    ent2 = (Entry) map.get(styleable);
                    if (ent2 != null) {
                        return ent2;
                    }
                }
                ent = ent2;
            } else {
                try {
                    Context context = this.mContext.createPackageContextAsUser(packageName, 0, new UserHandle(userId));
                    if (context == null) {
                        return null;
                    }
                    pkg = new Package(context);
                    this.mPackages.put(packageName, pkg);
                    ent = null;
                } catch (NameNotFoundException e) {
                    return null;
                }
            }
            if (map == null) {
                map = new ArrayMap();
                pkg.mMap.put(resId, map);
            }
            try {
                ent2 = new Entry(pkg.context, pkg.context.obtainStyledAttributes(resId, styleable));
                try {
                    map.put(styleable, ent2);
                    return ent2;
                } catch (NotFoundException e2) {
                    return null;
                }
            } catch (NotFoundException e3) {
                ent2 = ent;
                return null;
            }
        }
    }
}
