package com.qualcomm.wfd;

/* compiled from: ExtendedRemoteDisplay */
enum ERDConstants {
    PLAY_CALLBACK(0),
    PAUSE_CALLBACK(1),
    STANDBY_CALLBACK(2),
    UIBC_ACTION_COMPLETED_CALLBACK(3),
    TEARDOWN_CALLBACK(4),
    SERVICE_BOUND_CALLBACK(5),
    ESTABLISHED_CALLBACK(6),
    INIT_CALLBACK(7),
    MM_STREAM_STARTED_CALLBACK(8),
    INVALID_STATE_CALLBACK(9),
    TEARDOWN_START_CALLBACK(10),
    START_CMD(11),
    END_CMD(12),
    INVALID_ERD_CONSTANT(-1);
    
    private static final ERDConstants[] values = null;
    private final int value;

    static {
        values = values();
    }

    private ERDConstants(int value) {
        this.value = value;
    }

    public final int value() {
        return this.value;
    }

    public static final ERDConstants getConstant(int value) {
        try {
            return values[value];
        } catch (ArrayIndexOutOfBoundsException e) {
            return INVALID_ERD_CONSTANT;
        }
    }
}
