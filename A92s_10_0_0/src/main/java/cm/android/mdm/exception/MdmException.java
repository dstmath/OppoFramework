package cm.android.mdm.exception;

public class MdmException extends RuntimeException {
    private static final long serialVersionUID = -6046187522682247343L;

    public MdmException() {
    }

    public MdmException(String detailMessage) {
        super(detailMessage);
    }

    public MdmException(String message, Throwable cause) {
        super(message, cause);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MdmException(Throwable cause) {
        super(cause == null ? null : cause.toString(), cause);
    }
}
