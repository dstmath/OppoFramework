package androidx.test.internal.platform;

import android.os.StrictMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public final class ServiceLoaderWrapper {
    public static <T> List<T> loadService(Class<T> serviceClass) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        ArrayList arrayList = new ArrayList();
        Iterator it = ServiceLoader.load(serviceClass).iterator();
        while (it.hasNext()) {
            arrayList.add(it.next());
        }
        StrictMode.setThreadPolicy(oldPolicy);
        return arrayList;
    }
}
