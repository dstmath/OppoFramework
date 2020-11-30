package mediatek.app;

import android.app.ContextImpl;
import android.app.SystemServiceRegistry;
import android.net.ConnectivityThread;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;
import com.mediatek.search.SearchEngineManager;
import java.lang.reflect.Constructor;
import java.util.Optional;

public final class MtkSystemServiceRegistry {
    private static final String TAG = "MtkSystemServiceRegistry";
    private static ArrayMap<String, SystemServiceRegistry.ServiceFetcher<?>> sSystemServiceFetchers;
    private static ArrayMap<Class<?>, String> sSystemServiceNames;

    private MtkSystemServiceRegistry() {
    }

    public static void registerAllService() {
        Log.i(TAG, "registerAllService start");
        registerService(SearchEngineManager.SEARCH_ENGINE_SERVICE, SearchEngineManager.class, new SystemServiceRegistry.CachedServiceFetcher<SearchEngineManager>() {
            /* class mediatek.app.MtkSystemServiceRegistry.AnonymousClass1 */

            public SearchEngineManager createService(ContextImpl ctx) {
                return new SearchEngineManager(ctx);
            }
        });
        registerFmService();
        registerOmadmService();
    }

    public static void setMtkSystemServiceName(ArrayMap<Class<?>, String> names, ArrayMap<String, SystemServiceRegistry.ServiceFetcher<?>> fetchers) {
        Log.i(TAG, "setMtkSystemServiceName start names" + names + ",fetchers" + fetchers);
        sSystemServiceNames = names;
        sSystemServiceFetchers = fetchers;
    }

    private static <T> void registerService(String serviceName, Class<T> serviceClass, SystemServiceRegistry.ServiceFetcher<T> serviceFetcher) {
        sSystemServiceNames.put(serviceClass, serviceName);
        sSystemServiceFetchers.put(serviceName, serviceFetcher);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0022, code lost:
        r4 = r6.getConstructor(android.content.Context.class, android.os.Looper.class);
     */
    public static void registerFmService() {
        final Constructor constructor;
        try {
            Class<?> clazz = Class.forName("com.mediatek.fmradio.FmRadioPackageManager");
            if (clazz != null && (clazz = Class.forName((String) clazz.getMethod("getPackageName", null).invoke(null, new Object[0]))) != null && constructor != null) {
                registerService("fm_radio_service", Optional.class, new SystemServiceRegistry.CachedServiceFetcher<Optional>() {
                    /* class mediatek.app.MtkSystemServiceRegistry.AnonymousClass2 */

                    public Optional createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                        Optional optObj = Optional.empty();
                        try {
                            return Optional.of(constructor.newInstance(ctx, ConnectivityThread.getInstanceLooper()));
                        } catch (Exception e) {
                            Log.e(MtkSystemServiceRegistry.TAG, "Exception while creating FmRadioManager object");
                            return optObj;
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting FmRadioPackageManager class");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000a, code lost:
        r2 = r2.getConstructor(android.content.Context.class);
     */
    public static void registerOmadmService() {
        final Constructor constructor;
        try {
            Class<?> clazz = Class.forName("com.mediatek.common.omadm.OmadmManager");
            if (clazz != null && constructor != null) {
                registerService("omadm_service", Optional.class, new SystemServiceRegistry.CachedServiceFetcher<Optional>() {
                    /* class mediatek.app.MtkSystemServiceRegistry.AnonymousClass3 */

                    public Optional createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                        Optional optObj = Optional.empty();
                        try {
                            return Optional.of(constructor.newInstance(ctx));
                        } catch (Exception e) {
                            Log.e(MtkSystemServiceRegistry.TAG, "Exception while creating OmadmManager object");
                            return optObj;
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting OmadmManager class");
        }
    }
}
