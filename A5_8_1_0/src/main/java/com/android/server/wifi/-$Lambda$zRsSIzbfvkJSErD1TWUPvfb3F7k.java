package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifi.getChipCallback;
import android.hardware.wifi.V1_0.IWifi.getChipIdsCallback;
import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChip.createApIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.createNanIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.createP2pIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.createRttControllerCallback;
import android.hardware.wifi.V1_0.IWifiChip.createStaIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getApIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getApIfaceNamesCallback;
import android.hardware.wifi.V1_0.IWifiChip.getAvailableModesCallback;
import android.hardware.wifi.V1_0.IWifiChip.getIdCallback;
import android.hardware.wifi.V1_0.IWifiChip.getModeCallback;
import android.hardware.wifi.V1_0.IWifiChip.getNanIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getNanIfaceNamesCallback;
import android.hardware.wifi.V1_0.IWifiChip.getP2pIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getP2pIfaceNamesCallback;
import android.hardware.wifi.V1_0.IWifiChip.getStaIfaceCallback;
import android.hardware.wifi.V1_0.IWifiChip.getStaIfaceNamesCallback;
import android.hardware.wifi.V1_0.IWifiIface.getNameCallback;
import android.hardware.wifi.V1_0.IWifiIface.getTypeCallback;
import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.IWifiP2pIface;
import android.hardware.wifi.V1_0.IWifiRttController;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.WifiStatus;
import android.util.MutableBoolean;
import android.util.MutableInt;
import java.util.ArrayList;

final /* synthetic */ class -$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k implements createRttControllerCallback {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f61-$f0;

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$10 */
    final /* synthetic */ class AnonymousClass10 implements getAvailableModesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f62-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f63-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.m6lambda$-com_android_server_wifi_HalDeviceManager_33834((MutableBoolean) this.f62-$f0, (Mutable) this.f63-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass10(Object obj, Object obj2) {
            this.f62-$f0 = obj;
            this.f63-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$11 */
    final /* synthetic */ class AnonymousClass11 implements getIdCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f64-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f65-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, int arg1) {
            HalDeviceManager.m12lambda$-com_android_server_wifi_HalDeviceManager_50900((MutableInt) this.f64-$f0, (MutableBoolean) this.f65-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass11(Object obj, Object obj2) {
            this.f64-$f0 = obj;
            this.f65-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, int i) {
            $m$0(wifiStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$12 */
    final /* synthetic */ class AnonymousClass12 implements getNanIfaceNamesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f66-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f67-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.m11lambda$-com_android_server_wifi_HalDeviceManager_40964((MutableBoolean) this.f66-$f0, (Mutable) this.f67-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass12(Object obj, Object obj2) {
            this.f66-$f0 = obj;
            this.f67-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$13 */
    final /* synthetic */ class AnonymousClass13 implements getP2pIfaceNamesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f68-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f69-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.m10lambda$-com_android_server_wifi_HalDeviceManager_39158((MutableBoolean) this.f68-$f0, (Mutable) this.f69-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass13(Object obj, Object obj2) {
            this.f68-$f0 = obj;
            this.f69-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$14 */
    final /* synthetic */ class AnonymousClass14 implements getStaIfaceNamesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f70-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f71-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.m8lambda$-com_android_server_wifi_HalDeviceManager_35553((MutableBoolean) this.f70-$f0, (Mutable) this.f71-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass14(Object obj, Object obj2) {
            this.f70-$f0 = obj;
            this.f71-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$15 */
    final /* synthetic */ class AnonymousClass15 implements getModeCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f72-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f73-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f74-$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, int arg1) {
            HalDeviceManager.m7lambda$-com_android_server_wifi_HalDeviceManager_34615((MutableBoolean) this.f72-$f0, (MutableBoolean) this.f73-$f1, (MutableInt) this.f74-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass15(Object obj, Object obj2, Object obj3) {
            this.f72-$f0 = obj;
            this.f73-$f1 = obj2;
            this.f74-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, int i) {
            $m$0(wifiStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$16 */
    final /* synthetic */ class AnonymousClass16 implements getApIfaceCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f75-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f76-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f77-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f78-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f79-$f4;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiApIface arg1) {
            ((HalDeviceManager) this.f75-$f0).m21lambda$-com_android_server_wifi_HalDeviceManager_38184((MutableBoolean) this.f76-$f1, (String) this.f77-$f2, (WifiIfaceInfo[]) this.f78-$f3, (MutableInt) this.f79-$f4, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass16(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.f75-$f0 = obj;
            this.f76-$f1 = obj2;
            this.f77-$f2 = obj3;
            this.f78-$f3 = obj4;
            this.f79-$f4 = obj5;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface) {
            $m$0(wifiStatus, iWifiApIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$17 */
    final /* synthetic */ class AnonymousClass17 implements getNanIfaceCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f80-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f81-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f82-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f83-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f84-$f4;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiNanIface arg1) {
            ((HalDeviceManager) this.f80-$f0).m23lambda$-com_android_server_wifi_HalDeviceManager_41793((MutableBoolean) this.f81-$f1, (String) this.f82-$f2, (WifiIfaceInfo[]) this.f83-$f3, (MutableInt) this.f84-$f4, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass17(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.f80-$f0 = obj;
            this.f81-$f1 = obj2;
            this.f82-$f2 = obj3;
            this.f83-$f3 = obj4;
            this.f84-$f4 = obj5;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
            $m$0(wifiStatus, iWifiNanIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$18 */
    final /* synthetic */ class AnonymousClass18 implements getP2pIfaceCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f85-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f86-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f87-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f88-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f89-$f4;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiP2pIface arg1) {
            ((HalDeviceManager) this.f85-$f0).m22lambda$-com_android_server_wifi_HalDeviceManager_39987((MutableBoolean) this.f86-$f1, (String) this.f87-$f2, (WifiIfaceInfo[]) this.f88-$f3, (MutableInt) this.f89-$f4, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass18(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.f85-$f0 = obj;
            this.f86-$f1 = obj2;
            this.f87-$f2 = obj3;
            this.f88-$f3 = obj4;
            this.f89-$f4 = obj5;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface) {
            $m$0(wifiStatus, iWifiP2pIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$19 */
    final /* synthetic */ class AnonymousClass19 implements getStaIfaceCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f90-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f91-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f92-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f93-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f94-$f4;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiStaIface arg1) {
            ((HalDeviceManager) this.f90-$f0).m20lambda$-com_android_server_wifi_HalDeviceManager_36382((MutableBoolean) this.f91-$f1, (String) this.f92-$f2, (WifiIfaceInfo[]) this.f93-$f3, (MutableInt) this.f94-$f4, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass19(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.f90-$f0 = obj;
            this.f91-$f1 = obj2;
            this.f92-$f2 = obj3;
            this.f93-$f3 = obj4;
            this.f94-$f4 = obj5;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
            $m$0(wifiStatus, iWifiStaIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$1 */
    final /* synthetic */ class AnonymousClass1 implements getNameCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f95-$f0;

        private final /* synthetic */ void $m$0(WifiStatus arg0, String arg1) {
            HalDeviceManager.m0lambda$-com_android_server_wifi_HalDeviceManager_13566((Mutable) this.f95-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f95-$f0 = obj;
        }

        public final void onValues(WifiStatus wifiStatus, String str) {
            $m$0(wifiStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$2 */
    final /* synthetic */ class AnonymousClass2 implements getTypeCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f96-$f0;

        private final /* synthetic */ void $m$0(WifiStatus arg0, int arg1) {
            HalDeviceManager.m17lambda$-com_android_server_wifi_HalDeviceManager_79351((MutableInt) this.f96-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.f96-$f0 = obj;
        }

        public final void onValues(WifiStatus wifiStatus, int i) {
            $m$0(wifiStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$3 */
    final /* synthetic */ class AnonymousClass3 implements getChipCallback {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f97-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f98-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiChip arg1) {
            HalDeviceManager.m5lambda$-com_android_server_wifi_HalDeviceManager_33168((MutableBoolean) this.f97-$f0, (Mutable) this.f98-$f1, arg0, arg1);
        }

        private final /* synthetic */ void $m$1(WifiStatus arg0, IWifiChip arg1) {
            HalDeviceManager.m3lambda$-com_android_server_wifi_HalDeviceManager_28035((MutableBoolean) this.f97-$f0, (Mutable) this.f98-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass3(byte b, Object obj, Object obj2) {
            this.$id = b;
            this.f97-$f0 = obj;
            this.f98-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiChip iWifiChip) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(wifiStatus, iWifiChip);
                    return;
                case (byte) 1:
                    $m$1(wifiStatus, iWifiChip);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$4 */
    final /* synthetic */ class AnonymousClass4 implements getChipIdsCallback {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f99-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f100-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.m4lambda$-com_android_server_wifi_HalDeviceManager_32169((MutableBoolean) this.f99-$f0, (Mutable) this.f100-$f1, arg0, arg1);
        }

        private final /* synthetic */ void $m$1(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.m2lambda$-com_android_server_wifi_HalDeviceManager_27120((MutableBoolean) this.f99-$f0, (Mutable) this.f100-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass4(byte b, Object obj, Object obj2) {
            this.$id = b;
            this.f99-$f0 = obj;
            this.f100-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(wifiStatus, arrayList);
                    return;
                case (byte) 1:
                    $m$1(wifiStatus, arrayList);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$5 */
    final /* synthetic */ class AnonymousClass5 implements createApIfaceCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f101-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f102-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiApIface arg1) {
            HalDeviceManager.m14lambda$-com_android_server_wifi_HalDeviceManager_69237((Mutable) this.f101-$f0, (Mutable) this.f102-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj, Object obj2) {
            this.f101-$f0 = obj;
            this.f102-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface) {
            $m$0(wifiStatus, iWifiApIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$6 */
    final /* synthetic */ class AnonymousClass6 implements createNanIfaceCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f103-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f104-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiNanIface arg1) {
            HalDeviceManager.m16lambda$-com_android_server_wifi_HalDeviceManager_69998((Mutable) this.f103-$f0, (Mutable) this.f104-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj, Object obj2) {
            this.f103-$f0 = obj;
            this.f104-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
            $m$0(wifiStatus, iWifiNanIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$7 */
    final /* synthetic */ class AnonymousClass7 implements createP2pIfaceCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f105-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f106-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiP2pIface arg1) {
            HalDeviceManager.m15lambda$-com_android_server_wifi_HalDeviceManager_69617((Mutable) this.f105-$f0, (Mutable) this.f106-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(Object obj, Object obj2) {
            this.f105-$f0 = obj;
            this.f106-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface) {
            $m$0(wifiStatus, iWifiP2pIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$8 */
    final /* synthetic */ class AnonymousClass8 implements createStaIfaceCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f107-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f108-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiStaIface arg1) {
            HalDeviceManager.m13lambda$-com_android_server_wifi_HalDeviceManager_68858((Mutable) this.f107-$f0, (Mutable) this.f108-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass8(Object obj, Object obj2) {
            this.f107-$f0 = obj;
            this.f108-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
            $m$0(wifiStatus, iWifiStaIface);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k$9 */
    final /* synthetic */ class AnonymousClass9 implements getApIfaceNamesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f109-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f110-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            HalDeviceManager.m9lambda$-com_android_server_wifi_HalDeviceManager_37358((MutableBoolean) this.f109-$f0, (Mutable) this.f110-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass9(Object obj, Object obj2) {
            this.f109-$f0 = obj;
            this.f110-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    private final /* synthetic */ void $m$0(WifiStatus arg0, IWifiRttController arg1) {
        HalDeviceManager.m1lambda$-com_android_server_wifi_HalDeviceManager_16042((Mutable) this.f61-$f0, arg0, arg1);
    }

    public /* synthetic */ -$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k(Object obj) {
        this.f61-$f0 = obj;
    }

    public final void onValues(WifiStatus wifiStatus, IWifiRttController iWifiRttController) {
        $m$0(wifiStatus, iWifiRttController);
    }
}
