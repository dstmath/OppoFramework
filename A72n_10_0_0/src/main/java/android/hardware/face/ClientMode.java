package android.hardware.face;

public enum ClientMode {
    NONE(0),
    ENROLL(1),
    AUTHEN(2),
    REMOVE(3),
    ENGINEERING_INFO(4),
    UPDATE_FEATURE(5);
    
    final int mode;

    private ClientMode(int i) {
        this.mode = i;
    }

    public int getValue() {
        return this.mode;
    }
}
