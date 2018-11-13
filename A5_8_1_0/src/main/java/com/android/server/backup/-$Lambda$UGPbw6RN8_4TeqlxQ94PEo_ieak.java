package com.android.server.backup;

import com.android.server.backup.BackupManagerService.AnonymousClass3;

final /* synthetic */ class -$Lambda$UGPbw6RN8_4TeqlxQ94PEo_ieak implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f159-$f0;

    /* renamed from: com.android.server.backup.-$Lambda$UGPbw6RN8_4TeqlxQ94PEo_ieak$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f160-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f161-$f1;

        private final /* synthetic */ void $m$0() {
            ((AnonymousClass3) this.f160-$f0).m142lambda$-com_android_server_backup_BackupManagerService$3_89738((String) this.f161-$f1);
        }

        private final /* synthetic */ void $m$1() {
            ((AnonymousClass3) this.f160-$f0).m143lambda$-com_android_server_backup_BackupManagerService$3_90951((String) this.f161-$f1);
        }

        private final /* synthetic */ void $m$2() {
            ((RefactoredBackupManagerService.AnonymousClass3) this.f160-$f0).m14x54cf60b((String) this.f161-$f1);
        }

        private final /* synthetic */ void $m$3() {
            ((RefactoredBackupManagerService.AnonymousClass3) this.f160-$f0).m15x54d720e((String) this.f161-$f1);
        }

        public /* synthetic */ AnonymousClass1(byte b, Object obj, Object obj2) {
            this.$id = b;
            this.f160-$f0 = obj;
            this.f161-$f1 = obj2;
        }

        public final void run() {
            switch (this.$id) {
                case (byte) 0:
                    $m$0();
                    return;
                case (byte) 1:
                    $m$1();
                    return;
                case (byte) 2:
                    $m$2();
                    return;
                case (byte) 3:
                    $m$3();
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    /* renamed from: com.android.server.backup.-$Lambda$UGPbw6RN8_4TeqlxQ94PEo_ieak$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f162-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f163-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f164-$f2;

        private final /* synthetic */ void $m$0() {
            ((AnonymousClass3) this.f162-$f0).m141lambda$-com_android_server_backup_BackupManagerService$3_87036((String) this.f163-$f1, (String[]) this.f164-$f2);
        }

        private final /* synthetic */ void $m$1() {
            ((RefactoredBackupManagerService.AnonymousClass3) this.f162-$f0).m13x54217cd((String) this.f163-$f1, (String[]) this.f164-$f2);
        }

        public /* synthetic */ AnonymousClass2(byte b, Object obj, Object obj2, Object obj3) {
            this.$id = b;
            this.f162-$f0 = obj;
            this.f163-$f1 = obj2;
            this.f164-$f2 = obj3;
        }

        public final void run() {
            switch (this.$id) {
                case (byte) 0:
                    $m$0();
                    return;
                case (byte) 1:
                    $m$1();
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    private final /* synthetic */ void $m$0() {
        ((BackupManagerService) this.f159-$f0).lambda$-com_android_server_backup_BackupManagerService_58832();
    }

    private final /* synthetic */ void $m$1() {
        ((RefactoredBackupManagerService) this.f159-$f0).lambda$-com_android_server_backup_RefactoredBackupManagerService_30299();
    }

    public /* synthetic */ -$Lambda$UGPbw6RN8_4TeqlxQ94PEo_ieak(byte b, Object obj) {
        this.$id = b;
        this.f159-$f0 = obj;
    }

    public final void run() {
        switch (this.$id) {
            case (byte) 0:
                $m$0();
                return;
            case (byte) 1:
                $m$1();
                return;
            default:
                throw new AssertionError();
        }
    }
}
