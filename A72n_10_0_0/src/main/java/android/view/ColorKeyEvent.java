package android.view;

public class ColorKeyEvent extends KeyEvent {
    public static final int KEYCODE_GIMBAL_POWER = 717;
    public static final int KEYCODE_GIMBAL_SWITCH_CAMERA = 706;

    private ColorKeyEvent(int action, int code) {
        super(action, code);
    }
}
