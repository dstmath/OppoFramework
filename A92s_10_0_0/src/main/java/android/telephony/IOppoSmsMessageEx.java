package android.telephony;

public interface IOppoSmsMessageEx {
    String getDestinationAddress();

    int getEncodingType();

    String getRecipientAddress();
}
