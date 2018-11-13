package com.android.server.wallpaper;

final /* synthetic */ class -$Lambda$ZWcNEw3ZwVVSi_pP2mGGLvztkS0 implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f358-$f0;

    /* renamed from: com.android.server.wallpaper.-$Lambda$ZWcNEw3ZwVVSi_pP2mGGLvztkS0$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f359-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f360-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f361-$f2;

        private final /* synthetic */ void $m$0() {
            ((WallpaperManagerService) this.f359-$f0).m102x53c30189((WallpaperData) this.f360-$f1, (WallpaperData) this.f361-$f2);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2, Object obj3) {
            this.f359-$f0 = obj;
            this.f360-$f1 = obj2;
            this.f361-$f2 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((WallpaperConnection) this.f358-$f0).m103x18c95ab7();
    }

    private final /* synthetic */ void $m$1() {
        ((WallpaperConnection) this.f358-$f0).lambda$-com_android_server_wallpaper_WallpaperManagerService$WallpaperConnection_48195();
    }

    public /* synthetic */ -$Lambda$ZWcNEw3ZwVVSi_pP2mGGLvztkS0(byte b, Object obj) {
        this.$id = b;
        this.f358-$f0 = obj;
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
