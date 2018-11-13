package android.support.v4.media.routing;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaRouter;
import android.media.RemoteControlClient;
import android.os.Build.VERSION;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class MediaRouterJellybean {
    public static final int ALL_ROUTE_TYPES = 8388611;
    public static final int ROUTE_TYPE_LIVE_AUDIO = 1;
    public static final int ROUTE_TYPE_LIVE_VIDEO = 2;
    public static final int ROUTE_TYPE_USER = 8388608;
    private static final String TAG = "MediaRouterJellybean";

    public interface Callback {
        void onRouteAdded(Object obj);

        void onRouteChanged(Object obj);

        void onRouteGrouped(Object obj, Object obj2, int i);

        void onRouteRemoved(Object obj);

        void onRouteSelected(int i, Object obj);

        void onRouteUngrouped(Object obj, Object obj2);

        void onRouteUnselected(int i, Object obj);

        void onRouteVolumeChanged(Object obj);
    }

    static class CallbackProxy<T extends Callback> extends android.media.MediaRouter.Callback {
        protected final T mCallback;

        public CallbackProxy(T callback) {
            this.mCallback = callback;
        }

        public void onRouteSelected(MediaRouter router, int type, android.media.MediaRouter.RouteInfo route) {
            this.mCallback.onRouteSelected(type, route);
        }

        public void onRouteUnselected(MediaRouter router, int type, android.media.MediaRouter.RouteInfo route) {
            this.mCallback.onRouteUnselected(type, route);
        }

        public void onRouteAdded(MediaRouter router, android.media.MediaRouter.RouteInfo route) {
            this.mCallback.onRouteAdded(route);
        }

        public void onRouteRemoved(MediaRouter router, android.media.MediaRouter.RouteInfo route) {
            this.mCallback.onRouteRemoved(route);
        }

        public void onRouteChanged(MediaRouter router, android.media.MediaRouter.RouteInfo route) {
            this.mCallback.onRouteChanged(route);
        }

        public void onRouteGrouped(MediaRouter router, android.media.MediaRouter.RouteInfo route, android.media.MediaRouter.RouteGroup group, int index) {
            this.mCallback.onRouteGrouped(route, group, index);
        }

        public void onRouteUngrouped(MediaRouter router, android.media.MediaRouter.RouteInfo route, android.media.MediaRouter.RouteGroup group) {
            this.mCallback.onRouteUngrouped(route, group);
        }

        public void onRouteVolumeChanged(MediaRouter router, android.media.MediaRouter.RouteInfo route) {
            this.mCallback.onRouteVolumeChanged(route);
        }
    }

    public static final class GetDefaultRouteWorkaround {
        private Method mGetSystemAudioRouteMethod;

        public GetDefaultRouteWorkaround() {
            if (VERSION.SDK_INT < 16 || VERSION.SDK_INT > 17) {
                throw new UnsupportedOperationException();
            }
            try {
                this.mGetSystemAudioRouteMethod = MediaRouter.class.getMethod("getSystemAudioRoute", new Class[0]);
            } catch (NoSuchMethodException e) {
            }
        }

        public Object getDefaultRoute(Object routerObj) {
            MediaRouter router = (MediaRouter) routerObj;
            if (this.mGetSystemAudioRouteMethod != null) {
                try {
                    return this.mGetSystemAudioRouteMethod.invoke(router, new Object[0]);
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e2) {
                }
            }
            return router.getRouteAt(0);
        }
    }

    public static final class RouteCategory {
        public static CharSequence getName(Object categoryObj, Context context) {
            return ((android.media.MediaRouter.RouteCategory) categoryObj).getName(context);
        }

        public static List getRoutes(Object categoryObj) {
            ArrayList out = new ArrayList();
            ((android.media.MediaRouter.RouteCategory) categoryObj).getRoutes(out);
            return out;
        }

        public static int getSupportedTypes(Object categoryObj) {
            return ((android.media.MediaRouter.RouteCategory) categoryObj).getSupportedTypes();
        }

        public static boolean isGroupable(Object categoryObj) {
            return ((android.media.MediaRouter.RouteCategory) categoryObj).isGroupable();
        }
    }

    public static final class RouteGroup {
        public static List getGroupedRoutes(Object groupObj) {
            android.media.MediaRouter.RouteGroup group = (android.media.MediaRouter.RouteGroup) groupObj;
            int count = group.getRouteCount();
            List out = new ArrayList(count);
            for (int i = 0; i < count; i++) {
                out.add(group.getRouteAt(i));
            }
            return out;
        }
    }

    public static final class RouteInfo {
        public static CharSequence getName(Object routeObj, Context context) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getName(context);
        }

        public static CharSequence getStatus(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getStatus();
        }

        public static int getSupportedTypes(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getSupportedTypes();
        }

        public static Object getCategory(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getCategory();
        }

        public static Drawable getIconDrawable(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getIconDrawable();
        }

        public static int getPlaybackType(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getPlaybackType();
        }

        public static int getPlaybackStream(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getPlaybackStream();
        }

        public static int getVolume(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getVolume();
        }

        public static int getVolumeMax(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getVolumeMax();
        }

        public static int getVolumeHandling(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getVolumeHandling();
        }

        public static Object getTag(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getTag();
        }

        public static void setTag(Object routeObj, Object tag) {
            ((android.media.MediaRouter.RouteInfo) routeObj).setTag(tag);
        }

        public static void requestSetVolume(Object routeObj, int volume) {
            ((android.media.MediaRouter.RouteInfo) routeObj).requestSetVolume(volume);
        }

        public static void requestUpdateVolume(Object routeObj, int direction) {
            ((android.media.MediaRouter.RouteInfo) routeObj).requestUpdateVolume(direction);
        }

        public static Object getGroup(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getGroup();
        }

        public static boolean isGroup(Object routeObj) {
            return routeObj instanceof android.media.MediaRouter.RouteGroup;
        }
    }

    public static final class SelectRouteWorkaround {
        private Method mSelectRouteIntMethod;

        public SelectRouteWorkaround() {
            if (VERSION.SDK_INT < 16 || VERSION.SDK_INT > 17) {
                throw new UnsupportedOperationException();
            }
            try {
                this.mSelectRouteIntMethod = MediaRouter.class.getMethod("selectRouteInt", new Class[]{Integer.TYPE, android.media.MediaRouter.RouteInfo.class});
            } catch (NoSuchMethodException e) {
            }
        }

        public void selectRoute(Object routerObj, int types, Object routeObj) {
            MediaRouter router = (MediaRouter) routerObj;
            android.media.MediaRouter.RouteInfo route = (android.media.MediaRouter.RouteInfo) routeObj;
            if ((8388608 & route.getSupportedTypes()) == 0) {
                if (this.mSelectRouteIntMethod != null) {
                    try {
                        this.mSelectRouteIntMethod.invoke(router, new Object[]{Integer.valueOf(types), route});
                        return;
                    } catch (IllegalAccessException ex) {
                        Log.w(MediaRouterJellybean.TAG, "Cannot programmatically select non-user route.  Media routing may not work.", ex);
                    } catch (InvocationTargetException ex2) {
                        Log.w(MediaRouterJellybean.TAG, "Cannot programmatically select non-user route.  Media routing may not work.", ex2);
                    }
                } else {
                    Log.w(MediaRouterJellybean.TAG, "Cannot programmatically select non-user route because the platform is missing the selectRouteInt() method.  Media routing may not work.");
                }
            }
            router.selectRoute(types, route);
        }
    }

    public static final class UserRouteInfo {
        public static void setName(Object routeObj, CharSequence name) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setName(name);
        }

        public static void setStatus(Object routeObj, CharSequence status) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setStatus(status);
        }

        public static void setIconDrawable(Object routeObj, Drawable icon) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setIconDrawable(icon);
        }

        public static void setPlaybackType(Object routeObj, int type) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setPlaybackType(type);
        }

        public static void setPlaybackStream(Object routeObj, int stream) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setPlaybackStream(stream);
        }

        public static void setVolume(Object routeObj, int volume) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setVolume(volume);
        }

        public static void setVolumeMax(Object routeObj, int volumeMax) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setVolumeMax(volumeMax);
        }

        public static void setVolumeHandling(Object routeObj, int volumeHandling) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setVolumeHandling(volumeHandling);
        }

        public static void setVolumeCallback(Object routeObj, Object volumeCallbackObj) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setVolumeCallback((android.media.MediaRouter.VolumeCallback) volumeCallbackObj);
        }

        public static void setRemoteControlClient(Object routeObj, Object rccObj) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setRemoteControlClient((RemoteControlClient) rccObj);
        }
    }

    public interface VolumeCallback {
        void onVolumeSetRequest(Object obj, int i);

        void onVolumeUpdateRequest(Object obj, int i);
    }

    static class VolumeCallbackProxy<T extends VolumeCallback> extends android.media.MediaRouter.VolumeCallback {
        protected final T mCallback;

        public VolumeCallbackProxy(T callback) {
            this.mCallback = callback;
        }

        public void onVolumeSetRequest(android.media.MediaRouter.RouteInfo route, int volume) {
            this.mCallback.onVolumeSetRequest(route, volume);
        }

        public void onVolumeUpdateRequest(android.media.MediaRouter.RouteInfo route, int direction) {
            this.mCallback.onVolumeUpdateRequest(route, direction);
        }
    }

    MediaRouterJellybean() {
    }

    public static Object getMediaRouter(Context context) {
        return context.getSystemService("media_router");
    }

    public static List getRoutes(Object routerObj) {
        MediaRouter router = (MediaRouter) routerObj;
        int count = router.getRouteCount();
        List out = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            out.add(router.getRouteAt(i));
        }
        return out;
    }

    public static List getCategories(Object routerObj) {
        MediaRouter router = (MediaRouter) routerObj;
        int count = router.getCategoryCount();
        List out = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            out.add(router.getCategoryAt(i));
        }
        return out;
    }

    public static Object getSelectedRoute(Object routerObj, int type) {
        return ((MediaRouter) routerObj).getSelectedRoute(type);
    }

    public static void selectRoute(Object routerObj, int types, Object routeObj) {
        ((MediaRouter) routerObj).selectRoute(types, (android.media.MediaRouter.RouteInfo) routeObj);
    }

    public static void addCallback(Object routerObj, int types, Object callbackObj) {
        ((MediaRouter) routerObj).addCallback(types, (android.media.MediaRouter.Callback) callbackObj);
    }

    public static void removeCallback(Object routerObj, Object callbackObj) {
        ((MediaRouter) routerObj).removeCallback((android.media.MediaRouter.Callback) callbackObj);
    }

    public static Object createRouteCategory(Object routerObj, String name, boolean isGroupable) {
        return ((MediaRouter) routerObj).createRouteCategory(name, isGroupable);
    }

    public static Object createUserRoute(Object routerObj, Object categoryObj) {
        return ((MediaRouter) routerObj).createUserRoute((android.media.MediaRouter.RouteCategory) categoryObj);
    }

    public static void addUserRoute(Object routerObj, Object routeObj) {
        ((MediaRouter) routerObj).addUserRoute((android.media.MediaRouter.UserRouteInfo) routeObj);
    }

    public static void removeUserRoute(Object routerObj, Object routeObj) {
        ((MediaRouter) routerObj).removeUserRoute((android.media.MediaRouter.UserRouteInfo) routeObj);
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy(callback);
    }

    public static Object createVolumeCallback(VolumeCallback callback) {
        return new VolumeCallbackProxy(callback);
    }
}
