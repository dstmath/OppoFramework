package android.os;

import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefStaticMethod;

public class OppoMirrorProcess {
    public static Class<?> TYPE = RefClass.load(OppoMirrorProcess.class, Process.class);
    @MethodParams({int.class})
    public static RefStaticMethod<String> getProcessNameByPid;
}
