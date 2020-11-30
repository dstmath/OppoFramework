package android.view;

import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefLong;
import com.oppo.reflect.RefStaticMethod;

public class OppoMirrirMotionEvent {
    public static Class<?> TYPE = RefClass.load(OppoMirrirMotionEvent.class, MotionEvent.class);
    public static RefLong mNativePtr;
    @MethodParams({long.class})
    public static RefStaticMethod<Float> nativeGetXOffset;
    @MethodParams({long.class})
    public static RefStaticMethod<Float> nativeGetYOffset;
}
