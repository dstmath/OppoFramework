package android.os;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorLinearMotorVibratorManager {
    public static Class<?> TYPE = RefClass.load(OppoMirrorLinearMotorVibratorManager.class, "android.os.LinearMotorVibratorManager");
    public static RefMethod<Void> turnOffLinearMotorVibrator;
    public static RefMethod<Void> turnOnLinearmotorVibrator;
}
