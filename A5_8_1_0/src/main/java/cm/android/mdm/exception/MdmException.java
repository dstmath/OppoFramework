package cm.android.mdm.exception;

public class MdmException extends RuntimeException {
    private static final long serialVersionUID = -6046187522682247343L;

    public MdmException(String detailMessage) {
        super(detailMessage);
    }

    public MdmException(String message, Throwable cause) {
        super(message, cause);
    }

    public MdmException(Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        super(str, cause);
    }
}
