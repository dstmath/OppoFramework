package android.view;

import android.util.Log;

public class PswFeatureDemo implements IPswFeatureDemo {
    private static final String TAG = "PswFeatureDemo";
    private static PswFeatureDemo sInstance = null;

    public static PswFeatureDemo getInstance() {
        PswFeatureDemo pswFeatureDemo;
        synchronized (PswFeatureDemo.class) {
            if (sInstance == null) {
                sInstance = new PswFeatureDemo();
            }
            pswFeatureDemo = sInstance;
        }
        return pswFeatureDemo;
    }

    public PswFeatureDemo() {
        Log.i(TAG, "create");
    }

    public void testfunc() {
        Log.i(TAG, "testFunc");
    }
}
