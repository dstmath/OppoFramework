package android.support.v4.media.routing;

import android.media.MediaRouter;
import android.media.MediaRouter.Callback;

class MediaRouterJellybeanMr2 extends MediaRouterJellybeanMr1 {

    public static final class RouteInfo {
        public static CharSequence getDescription(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getDescription();
        }

        public static boolean isConnecting(Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).isConnecting();
        }
    }

    public static final class UserRouteInfo {
        public static void setDescription(Object routeObj, CharSequence description) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setDescription(description);
        }
    }

    MediaRouterJellybeanMr2() {
    }

    public static Object getDefaultRoute(Object routerObj) {
        return ((MediaRouter) routerObj).getDefaultRoute();
    }

    public static void addCallback(Object routerObj, int types, Object callbackObj, int flags) {
        ((MediaRouter) routerObj).addCallback(types, (Callback) callbackObj, flags);
    }
}
