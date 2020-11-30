package android.common;

import android.common.OppoFeatureList;
import java.util.ArrayList;
import java.util.Iterator;

public class OppoFeatureCache {
    private static final String TAG = "OppoFeatureCache";
    private static ArrayList<IOppoCommonFactory> mFactoryCache = new ArrayList<>();
    private static Object[] mFeatureCache = new Object[OppoFeatureList.OppoIndex.End.ordinal()];

    static {
        mFactoryCache.add(ColorFrameworkFactory.getInstance());
        mFactoryCache.add(PswFrameworkFactory.getInstance());
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v3, types: [android.common.IOppoCommonFeature] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public static <T extends IOppoCommonFeature> T get(T def) {
        int index = getIndex(def);
        T object = (T) ((IOppoCommonFeature) mFeatureCache[def.index().ordinal()]);
        if (object == null) {
            synchronized (def.getDefault()) {
                object = (IOppoCommonFeature) mFeatureCache[index];
            }
        }
        return object != null ? object : def;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v3, types: [android.common.IOppoCommonFeature] */
    /* JADX WARN: Type inference failed for: r6v1, types: [android.common.IOppoCommonFeature] */
    /* JADX WARNING: Unknown variable types count: 2 */
    public static <T extends IOppoCommonFeature> T getOrCreate(T def, Object... vars) {
        int index = getIndex(def);
        T object = (T) ((IOppoCommonFeature) mFeatureCache[def.index().ordinal()]);
        if (object == null) {
            synchronized (def.getDefault()) {
                object = (IOppoCommonFeature) mFeatureCache[index];
                if (object == null) {
                    synchronized (mFactoryCache) {
                        Iterator<IOppoCommonFactory> it = mFactoryCache.iterator();
                        while (it.hasNext()) {
                            IOppoCommonFactory factory = it.next();
                            if (factory.isValid(index) && (object = factory.getFeature(def, vars)) != null) {
                                set(object);
                                return object;
                            }
                        }
                    }
                }
            }
        }
        return object != null ? object : def;
    }

    public static <T extends IOppoCommonFeature> void set(T impl) {
        int index = getIndex(impl);
        synchronized (impl.getDefault()) {
            mFeatureCache[index] = impl;
        }
    }

    public static <T extends IOppoCommonFactory> void addFactory(T factory) {
        if (factory != null) {
            synchronized (mFactoryCache) {
                mFactoryCache.add(factory);
            }
        }
    }

    private static <T extends IOppoCommonFeature> int getIndex(T service) {
        if (service != null) {
            int index = service.index().ordinal();
            if (index < mFeatureCache.length) {
                return index;
            }
            throw new IllegalAccessError("index = " + index + " size = " + mFeatureCache.length);
        }
        throw new IllegalArgumentException("dummy must not be null");
    }
}
