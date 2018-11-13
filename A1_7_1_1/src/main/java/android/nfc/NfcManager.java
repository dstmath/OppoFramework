package android.nfc;

import android.content.Context;

public final class NfcManager {
    private final NfcAdapter mAdapter;

    public NfcManager(Context context) {
        context = context.getApplicationContext();
        if (context == null) {
            throw new IllegalArgumentException("context not associated with any application (using a mock context?)");
        }
        NfcAdapter adapter;
        try {
            adapter = NfcAdapter.getNfcAdapter(context);
        } catch (UnsupportedOperationException e) {
            adapter = null;
        }
        this.mAdapter = adapter;
    }

    public NfcAdapter getDefaultAdapter() {
        return this.mAdapter;
    }
}
