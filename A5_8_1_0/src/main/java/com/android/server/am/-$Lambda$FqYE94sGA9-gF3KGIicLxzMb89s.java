package com.android.server.am;

import android.app.ITaskStackListener;
import android.os.Message;
import com.android.server.am.TaskChangeNotificationController.TaskStackConsumer;
import com.android.server.usb.descriptors.UsbDescriptor;

final /* synthetic */ class -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s implements TaskStackConsumer {
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$0 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 0);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$1 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 1);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$10 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 10);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$11 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 11);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$12 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 12);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$13 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 13);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$14 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s(UsbDescriptor.CLASSID_VIDEO);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$15 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 15);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$16 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 16);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$2 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 2);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$3 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 3);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$4 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 4);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$5 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 5);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$6 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 6);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$7 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 7);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$8 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 8);
    public static final /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s $INST$9 = new -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s((byte) 9);
    private final /* synthetic */ byte $id;

    private /* synthetic */ -$Lambda$FqYE94sGA9-gF3KGIicLxzMb89s(byte b) {
        this.$id = b;
    }

    public final void accept(ITaskStackListener iTaskStackListener, Message message) {
        switch (this.$id) {
            case (byte) 0:
                $m$0(iTaskStackListener, message);
                return;
            case (byte) 1:
                $m$1(iTaskStackListener, message);
                return;
            case (byte) 2:
                $m$2(iTaskStackListener, message);
                return;
            case (byte) 3:
                $m$3(iTaskStackListener, message);
                return;
            case (byte) 4:
                $m$4(iTaskStackListener, message);
                return;
            case (byte) 5:
                $m$5(iTaskStackListener, message);
                return;
            case (byte) 6:
                $m$6(iTaskStackListener, message);
                return;
            case (byte) 7:
                $m$7(iTaskStackListener, message);
                return;
            case (byte) 8:
                $m$8(iTaskStackListener, message);
                return;
            case (byte) 9:
                $m$9(iTaskStackListener, message);
                return;
            case (byte) 10:
                $m$10(iTaskStackListener, message);
                return;
            case (byte) 11:
                $m$11(iTaskStackListener, message);
                return;
            case (byte) 12:
                $m$12(iTaskStackListener, message);
                return;
            case (byte) 13:
                $m$13(iTaskStackListener, message);
                return;
            case (byte) 14:
                $m$14(iTaskStackListener, message);
                return;
            case (byte) 15:
                $m$15(iTaskStackListener, message);
                return;
            case (byte) 16:
                $m$16(iTaskStackListener, message);
                return;
            default:
                throw new AssertionError();
        }
    }
}
