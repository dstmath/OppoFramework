package androidx.core.app;

import android.view.View;
import java.util.List;
import java.util.Map;

public abstract class SharedElementCallback {
    public void onSharedElementStart(List<String> list, List<View> list2, List<View> list3) {
    }

    public void onSharedElementEnd(List<String> list, List<View> list2, List<View> list3) {
    }

    public void onMapSharedElements(List<String> list, Map<String, View> map) {
    }
}
