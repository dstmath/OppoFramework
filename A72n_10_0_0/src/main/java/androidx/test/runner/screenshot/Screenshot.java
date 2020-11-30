package androidx.test.runner.screenshot;

import android.os.Build;
import androidx.test.runner.screenshot.TakeScreenshotCallable;
import java.util.HashSet;
import java.util.Set;

public final class Screenshot {
    private static int androidRuntimeVersion = Build.VERSION.SDK_INT;
    private static Set<ScreenCaptureProcessor> screenCaptureProcessorSet = new HashSet();
    private static TakeScreenshotCallable.Factory takeScreenshotCallableFactory = new TakeScreenshotCallable.Factory();
    private static UiAutomationWrapper uiWrapper = new UiAutomationWrapper();

    public static void addScreenCaptureProcessors(Set<ScreenCaptureProcessor> screenCaptureProcessors) {
        screenCaptureProcessorSet.addAll(screenCaptureProcessors);
    }

    static void setTakeScreenshotCallableFactory(TakeScreenshotCallable.Factory factory) {
        takeScreenshotCallableFactory = factory;
    }

    static void setUiAutomationWrapper(UiAutomationWrapper wrapper) {
        uiWrapper = wrapper;
    }

    static void setAndroidRuntimeVersion(int sdkInt) {
        androidRuntimeVersion = sdkInt;
    }
}
