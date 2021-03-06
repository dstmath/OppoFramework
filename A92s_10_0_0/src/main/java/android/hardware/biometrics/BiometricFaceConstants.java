package android.hardware.biometrics;

public interface BiometricFaceConstants extends IOppoBiometricFaceConstantsEx {
    public static final int BIOMETRIC_ERROR_NO_DEVICE_CREDENTIAL = 14;
    public static final int FACE_ACQUIRED_CAMERA_PREVIEW = 1001;
    public static final int FACE_ACQUIRED_DEPTH_TOO_NEARLY = 303;
    public static final int FACE_ACQUIRED_DOE_CHECK = 307;
    public static final int FACE_ACQUIRED_DOE_PRECHECK = 306;
    public static final int FACE_ACQUIRED_FACEDOE_IMAGE_READY = 308;
    public static final int FACE_ACQUIRED_FACE_OBSCURED = 19;
    public static final int FACE_ACQUIRED_GOOD = 0;
    public static final int FACE_ACQUIRED_HACKER = 104;
    public static final int FACE_ACQUIRED_INSUFFICIENT = 1;
    public static final int FACE_ACQUIRED_IR_HACKER = 305;
    public static final int FACE_ACQUIRED_IR_PATTERN = 304;
    public static final int FACE_ACQUIRED_MOUTH_OCCLUSION = 113;
    public static final int FACE_ACQUIRED_MULTI_FACE = 116;
    public static final int FACE_ACQUIRED_NOSE_OCCLUSION = 115;
    public static final int FACE_ACQUIRED_NOT_DETECTED = 11;
    public static final int FACE_ACQUIRED_NOT_FRONTAL_FACE = 114;
    public static final int FACE_ACQUIRED_NO_FACE = 101;
    public static final int FACE_ACQUIRED_NO_FOCUS = 112;
    public static final int FACE_ACQUIRED_PAN_TOO_EXTREME = 16;
    public static final int FACE_ACQUIRED_POOR_GAZE = 10;
    public static final int FACE_ACQUIRED_RECALIBRATE = 13;
    public static final int FACE_ACQUIRED_ROLL_TOO_EXTREME = 18;
    public static final int FACE_ACQUIRED_SENSOR_DIRTY = 21;
    public static final int FACE_ACQUIRED_START = 20;
    public static final int FACE_ACQUIRED_SWITCH_DEPTH = 302;
    public static final int FACE_ACQUIRED_SWITCH_IR = 301;
    public static final int FACE_ACQUIRED_TILT_TOO_EXTREME = 17;
    public static final int FACE_ACQUIRED_TOO_BRIGHT = 2;
    public static final int FACE_ACQUIRED_TOO_CLOSE = 4;
    public static final int FACE_ACQUIRED_TOO_DARK = 3;
    public static final int FACE_ACQUIRED_TOO_DIFFERENT = 14;
    public static final int FACE_ACQUIRED_TOO_FAR = 5;
    public static final int FACE_ACQUIRED_TOO_HIGH = 6;
    public static final int FACE_ACQUIRED_TOO_LEFT = 9;
    public static final int FACE_ACQUIRED_TOO_LOW = 7;
    public static final int FACE_ACQUIRED_TOO_MUCH_MOTION = 12;
    public static final int FACE_ACQUIRED_TOO_RIGHT = 8;
    public static final int FACE_ACQUIRED_TOO_SIMILAR = 15;
    public static final int FACE_ACQUIRED_VENDOR = 22;
    public static final int FACE_ACQUIRED_VENDOR_BASE = 1000;
    public static final int FACE_AUTHENTICATE_AUTO = 0;
    public static final int FACE_AUTHENTICATE_BY_FINGERPRINT = 3;
    public static final int FACE_AUTHENTICATE_BY_USER = 1;
    public static final int FACE_AUTHENTICATE_BY_USER_WITH_ANIM = 2;
    public static final int FACE_AUTHENTICATE_PAY = 4;
    public static final int FACE_ERROR_CAMERA_UNAVAILABLE = 0;
    public static final int FACE_ERROR_CANCELED = 5;
    public static final int FACE_ERROR_HW_NOT_PRESENT = 12;
    public static final int FACE_ERROR_HW_UNAVAILABLE = 1;
    public static final int FACE_ERROR_LOCKOUT = 7;
    public static final int FACE_ERROR_LOCKOUT_PERMANENT = 9;
    public static final int FACE_ERROR_NEGATIVE_BUTTON = 13;
    public static final int FACE_ERROR_NOT_ENROLLED = 11;
    public static final int FACE_ERROR_NO_SPACE = 4;
    public static final int FACE_ERROR_TIMEOUT = 3;
    public static final int FACE_ERROR_UNABLE_TO_PROCESS = 2;
    public static final int FACE_ERROR_UNABLE_TO_REMOVE = 6;
    public static final int FACE_ERROR_USER_CANCELED = 10;
    public static final int FACE_ERROR_VENDOR = 8;
    public static final int FACE_ERROR_VENDOR_BASE = 1000;
    public static final String FACE_KEYGUARD_CANCELED_BY_SCREEN_OFF = "cancelRecognitionByScreenOff";
    public static final int FACE_WITH_EYES_CLOSED = 111;
    public static final int FEATURE_REQUIRE_ATTENTION = 1;
    public static final int FEATURE_REQUIRE_REQUIRE_DIVERSITY = 2;
}
