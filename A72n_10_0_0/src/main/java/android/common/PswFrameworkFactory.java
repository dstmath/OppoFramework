package android.common;

import android.common.OppoFeatureList;
import android.util.Log;

public class PswFrameworkFactory implements IOppoCommonFactory {
    public static final String PSW_FRAMEWORK_FACTORY_IMPL_NAME = "com.oppo.common.PswFrameworkFactoryImpl";
    private static final String TAG = "PswFrameworkFactory";
    private static volatile PswFrameworkFactory sInstance = null;

    public static PswFrameworkFactory getInstance() {
        if (sInstance == null) {
            synchronized (PswFrameworkFactory.class) {
                if (sInstance == null) {
                    try {
                        sInstance = (PswFrameworkFactory) newInstance(PSW_FRAMEWORK_FACTORY_IMPL_NAME);
                    } catch (Exception e) {
                        Log.e(TAG, " Reflect exception getInstance: " + e.toString());
                        sInstance = new PswFrameworkFactory();
                    }
                }
            }
        }
        return sInstance;
    }

    @Override // android.common.IOppoCommonFactory
    public boolean isValid(int index) {
        return index < OppoFeatureList.OppoIndex.EndPswFrameworkFactory.ordinal() && index > OppoFeatureList.OppoIndex.StartPswFrameworkFactory.ordinal();
    }

    static Object newInstance(String className) throws Exception {
        return Class.forName(className).getConstructor(new Class[0]).newInstance(new Object[0]);
    }
}
