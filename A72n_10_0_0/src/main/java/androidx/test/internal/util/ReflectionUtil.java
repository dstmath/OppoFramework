package androidx.test.internal.util;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public static void reflectivelyInvokeRemoteMethod(String className, String methodName) {
        Checks.checkNotNull(className);
        Checks.checkNotNull(methodName);
        String valueOf = String.valueOf(methodName);
        Log.i("ReflectionUtil", valueOf.length() != 0 ? "Attempting to reflectively call: ".concat(valueOf) : new String("Attempting to reflectively call: "));
        try {
            Method m = Class.forName(className).getDeclaredMethod(methodName, new Class[0]);
            m.setAccessible(true);
            m.invoke(null, new Object[0]);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.e("ReflectionUtil", "Reflective call failed: ", e);
        }
    }
}
