package android.telephony;

import android.app.PendingIntent;
import java.util.ArrayList;

public interface IOppoSmsManagerEx {
    ArrayList<String> divideMessageOem(String str, int i);

    void sendMultipartTextMessageOem(String str, String str2, ArrayList<String> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, int i, boolean z, int i2, int i3);
}
