package android.hardware.biometrics;

public interface IOppoBiometricFaceConstantsEx {
    public static final int FACE_ACQUIRED_CAMERA_PREVIEW = 1001;
    public static final int FACE_ACQUIRED_DEPTH_TOO_NEARLY = 303;
    public static final int FACE_ACQUIRED_DOE_CHECK = 307;
    public static final int FACE_ACQUIRED_DOE_PRECHECK = 306;
    public static final int FACE_ACQUIRED_FACEDOE_IMAGE_READY = 308;
    public static final int FACE_ACQUIRED_HACKER = 104;
    public static final int FACE_ACQUIRED_IR_HACKER = 305;
    public static final int FACE_ACQUIRED_IR_PATTERN = 304;
    public static final int FACE_ACQUIRED_MOUTH_OCCLUSION = 113;
    public static final int FACE_ACQUIRED_MULTI_FACE = 116;
    public static final int FACE_ACQUIRED_NOSE_OCCLUSION = 115;
    public static final int FACE_ACQUIRED_NOT_FRONTAL_FACE = 114;
    public static final int FACE_ACQUIRED_NO_FACE = 101;
    public static final int FACE_ACQUIRED_NO_FOCUS = 112;
    public static final int FACE_ACQUIRED_SWITCH_DEPTH = 302;
    public static final int FACE_ACQUIRED_SWITCH_IR = 301;
    public static final int FACE_AUTHENTICATE_AUTO = 0;
    public static final int FACE_AUTHENTICATE_BY_FINGERPRINT = 3;
    public static final int FACE_AUTHENTICATE_BY_USER = 1;
    public static final int FACE_AUTHENTICATE_BY_USER_WITH_ANIM = 2;
    public static final int FACE_AUTHENTICATE_PAY = 4;
    public static final int FACE_ERROR_CAMERA_UNAVAILABLE = 0;
    public static final String FACE_KEYGUARD_CANCELED_BY_SCREEN_OFF = "cancelRecognitionByScreenOff";
    public static final int FACE_WITH_EYES_CLOSED = 111;
}
