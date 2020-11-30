package android.telecom;

public class OppoCallProxy {
    private static final String TAG = "OppoCallProxy";
    private Call mCall;

    private OppoCallProxy(Call call) {
        this.mCall = call;
    }

    public static OppoCallProxy map(Call call) {
        return new OppoCallProxy(call);
    }

    public String internalGetCallId() {
        Call call = this.mCall;
        if (call == null) {
            return null;
        }
        call.internalGetCallId();
        return null;
    }
}
