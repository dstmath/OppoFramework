package android.app;

import android.content.res.Configuration;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.color.screenshot.ColorScreenshotManager;
import com.oppo.util.OppoDialogUtil;

class ColorInjector {

    static class Dialog {
        Dialog() {
        }

        static void initButtonBackground(Dialog dialog, int flag, int option) {
            if (dialog.isOppoStyle()) {
                new OppoDialogUtil(dialog.getContext()).setDialogButtonFlag(dialog.getWindow(), flag, option);
            }
        }

        static void initGravity(Dialog dialog, Window w) {
            Configuration cfg = dialog.getContext().getResources().getConfiguration();
            if (dialog.isOppoStyle() && cfg.orientation == 1) {
                w.setGravity(81);
            } else {
                w.setGravity(17);
            }
        }

        static void initWidth(Dialog dialog, LayoutParams lp) {
        }

        static void initProgress(Dialog dialog, LayoutParams lp) {
            if (dialog.isOppoStyle()) {
                lp.width = -2;
                lp.height = -2;
                View parentPanel = dialog.getWindow().findViewById(16909137);
                if (parentPanel != null) {
                    parentPanel.setBackground(null);
                }
            }
        }
    }

    static class SystemServiceRegistry {
        SystemServiceRegistry() {
        }

        static void registerServices() {
            SystemServiceRegistry.registerService("color_screenshot", ColorScreenshotManager.class, new CachedServiceFetcher<ColorScreenshotManager>() {
                public ColorScreenshotManager createService(ContextImpl ctx) {
                    return ColorScreenshotManager.getInstance();
                }
            });
        }
    }

    ColorInjector() {
    }
}
