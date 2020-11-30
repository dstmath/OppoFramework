package com.android.server.engineer;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.ProfilerInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.OppoServiceBootPhase;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.engineer.util.IdProviderUtils;
import com.android.server.engineer.util.SecrecyServiceHelper;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.wm.WindowManagerService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.regex.Pattern;
import vendor.oppo.engnative.engineer.V1_0.IEngineer;

public class OppoEngineerShell extends ShellCommand {
    private static final boolean DEBUG = true;
    private static final String TAG = OppoEngineerShell.class.getSimpleName();
    private Context mContext;
    private LocalServerSocket mLocalServerSocket;
    private LocalSocket mLocalSocket;
    private OppoEngineerService mOppoEngineerService;

    OppoEngineerShell(OppoEngineerService oppoEngineerService, Context context) {
        this.mOppoEngineerService = oppoEngineerService;
        this.mContext = context;
    }

    public int onCommand(String s) {
        if (TextUtils.isEmpty(s)) {
            return 0;
        }
        PrintWriter printWriter = getOutPrintWriter();
        long token = Binder.clearCallingIdentity();
        try {
            if (SecrecyServiceHelper.isSecrecySupported() && SecrecyServiceHelper.getSecrecyState(4)) {
                Binder.restoreCallingIdentity(token);
                return 0;
            } else if (this.mOppoEngineerService.isShellCommandInBlackListInternal(s)) {
                Binder.restoreCallingIdentity(token);
                return 0;
            } else {
                Slog.d(TAG, "+++ s = " + s);
                char c = 65535;
                switch (s.hashCode()) {
                    case -2088691726:
                        if (s.equals("--verify_keybox")) {
                            c = '9';
                            break;
                        }
                        break;
                    case -2006928703:
                        if (s.equals("--query_version")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -1949319228:
                        if (s.equals("--execute_stop_dsocket")) {
                            c = '(';
                            break;
                        }
                        break;
                    case -1869321372:
                        if (s.equals("--execute_detect_ms_result")) {
                            c = 31;
                            break;
                        }
                        break;
                    case -1783115275:
                        if (s.equals("--execute_shutdown_device")) {
                            c = '*';
                            break;
                        }
                        break;
                    case -1777399994:
                        if (s.equals("--execute_reboot_device")) {
                            c = '#';
                            break;
                        }
                        break;
                    case -1697223522:
                        if (s.equals("--update_serial_number")) {
                            c = 18;
                            break;
                        }
                        break;
                    case -1666414628:
                        if (s.equals("--hdcp_verify_keybox")) {
                            c = '7';
                            break;
                        }
                        break;
                    case -1626848838:
                        if (s.equals("--query_back_cover_color")) {
                            c = 2;
                            break;
                        }
                        break;
                    case -1614897061:
                        if (s.equals("--get_device_id")) {
                            c = '1';
                            break;
                        }
                        break;
                    case -1614157928:
                        if (s.equals("--crypto_eng_verify")) {
                            c = '3';
                            break;
                        }
                        break;
                    case -1543741350:
                        if (s.equals("--execute_master_clear")) {
                            c = 26;
                            break;
                        }
                        break;
                    case -1415223598:
                        if (s.equals("--query_sim_state")) {
                            c = 11;
                            break;
                        }
                        break;
                    case -1101530093:
                        if (s.equals("--reset_back_cover_color")) {
                            c = 20;
                            break;
                        }
                        break;
                    case -1075742606:
                        if (s.equals("--query_download_status")) {
                            c = 4;
                            break;
                        }
                        break;
                    case -1033108208:
                        if (s.equals("--execute_start_dsocket")) {
                            c = '\'';
                            break;
                        }
                        break;
                    case -989620010:
                        if (s.equals("--query_engineer_result")) {
                            c = 6;
                            break;
                        }
                        break;
                    case -865574960:
                        if (s.equals("--install_keybox")) {
                            c = '5';
                            break;
                        }
                        break;
                    case -712323242:
                        if (s.equals("--provision_keybox")) {
                            c = '8';
                            break;
                        }
                        break;
                    case -683930006:
                        if (s.equals("--reset_download_mode")) {
                            c = 23;
                            break;
                        }
                        break;
                    case -614506708:
                        if (s.equals("--hdcp_provision_keybox")) {
                            c = '6';
                            break;
                        }
                        break;
                    case -577415713:
                        if (s.equals("--reset_write_protect")) {
                            c = 21;
                            break;
                        }
                        break;
                    case -551205278:
                        if (s.equals("--verify_attk_key_pair_only")) {
                            c = '.';
                            break;
                        }
                        break;
                    case -424502248:
                        if (s.equals("--verify_ali_key")) {
                            c = '/';
                            break;
                        }
                        break;
                    case -387321349:
                        if (s.equals("--query_battery_level")) {
                            c = 3;
                            break;
                        }
                        break;
                    case -311469421:
                        if (s.equals("--execute_check_key_status")) {
                            c = '&';
                            break;
                        }
                        break;
                    case -233355281:
                        if (s.equals("--enable_rpmb")) {
                            c = '2';
                            break;
                        }
                        break;
                    case -210852304:
                        if (s.equals("--query_pcb_number")) {
                            c = '\n';
                            break;
                        }
                        break;
                    case -140701283:
                        if (s.equals("--query_indicate_info")) {
                            c = '\r';
                            break;
                        }
                        break;
                    case -118855923:
                        if (s.equals("--execute_remove_cover_ui")) {
                            c = '%';
                            break;
                        }
                        break;
                    case -92364746:
                        if (s.equals("--execute_stop_mmi")) {
                            c = 25;
                            break;
                        }
                        break;
                    case -92262373:
                        if (s.equals("--query_lcd_status")) {
                            c = 15;
                            break;
                        }
                        break;
                    case -30237543:
                        if (s.equals("--update_back_cover_color")) {
                            c = 16;
                            break;
                        }
                        break;
                    case -5057884:
                        if (s.equals("--get_tee_version")) {
                            c = '4';
                            break;
                        }
                        break;
                    case 31176723:
                        if (s.equals("--export_attk_public_key")) {
                            c = '0';
                            break;
                        }
                        break;
                    case 60211637:
                        if (s.equals("--query_product_info")) {
                            c = 14;
                            break;
                        }
                        break;
                    case 135704413:
                        if (s.equals("--reset_engineer_result")) {
                            c = 22;
                            break;
                        }
                        break;
                    case 254018706:
                        if (s.equals("--execute_model_test")) {
                            c = 28;
                            break;
                        }
                        break;
                    case 424345972:
                        if (s.equals("--query_sd_card_exists")) {
                            c = '\f';
                            break;
                        }
                        break;
                    case 521601571:
                        if (s.equals("--query_download_mode")) {
                            c = 7;
                            break;
                        }
                        break;
                    case 531892169:
                        if (s.equals("--verify_attk_key_pair")) {
                            c = '-';
                            break;
                        }
                        break;
                    case 628115864:
                        if (s.equals("--query_write_protect")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 647245131:
                        if (s.equals("--execute_power_off")) {
                            c = 27;
                            break;
                        }
                        break;
                    case 802341661:
                        if (s.equals("--query_serial_number")) {
                            c = '\b';
                            break;
                        }
                        break;
                    case 901886176:
                        if (s.equals("--execute_switch_wifi_mmi")) {
                            c = '!';
                            break;
                        }
                        break;
                    case 1089225669:
                        if (s.equals("--generate_attk_key_pair")) {
                            c = ',';
                            break;
                        }
                        break;
                    case 1091586139:
                        if (s.equals("--query_battery_status")) {
                            c = 5;
                            break;
                        }
                        break;
                    case 1163213860:
                        if (s.equals("--execute_add_cover_ui")) {
                            c = '$';
                            break;
                        }
                        break;
                    case 1433406376:
                        if (s.equals("--execute_check_calibration_status")) {
                            c = '+';
                            break;
                        }
                        break;
                    case 1470948161:
                        if (s.equals("--execute_launch_wifi_mmi")) {
                            c = '\"';
                            break;
                        }
                        break;
                    case 1681696040:
                        if (s.equals("--execute_engineer_order")) {
                            c = 29;
                            break;
                        }
                        break;
                    case 1787655176:
                        if (s.equals("--execute_switch_production_mode")) {
                            c = 30;
                            break;
                        }
                        break;
                    case 1888570009:
                        if (s.equals("--execute_show_production_message")) {
                            c = ' ';
                            break;
                        }
                        break;
                    case 1967745922:
                        if (s.equals("--execute_start_mmi")) {
                            c = 24;
                            break;
                        }
                        break;
                    case 2057790484:
                        if (s.equals("--update_product_info")) {
                            c = 19;
                            break;
                        }
                        break;
                    case 2086669659:
                        if (s.equals("--query_cplc")) {
                            c = '\t';
                            break;
                        }
                        break;
                    case 2096971875:
                        if (s.equals("--execute_switch_log")) {
                            c = ')';
                            break;
                        }
                        break;
                    case 2109924887:
                        if (s.equals("--update_engineer_result")) {
                            c = 17;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case '\b':
                    case '\t':
                    case '\n':
                    case 11:
                    case '\f':
                    case '\r':
                    case 14:
                    case 15:
                        handleQueryCommand(s, printWriter);
                        break;
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                        handleUpdateCommand(s, printWriter);
                        break;
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                        handleResetCommand(s, printWriter);
                        break;
                    case 24:
                    case 25:
                    case OppoServiceBootPhase.PHASE_ALARM_READY /* 26 */:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                    case ' ':
                    case '!':
                    case '\"':
                    case '#':
                    case '$':
                    case '%':
                    case '&':
                    case '\'':
                    case '(':
                    case ')':
                    case '*':
                    case '+':
                        handleExecuteCommand(s, printWriter);
                        break;
                    case ',':
                    case NetworkPolicyManagerService.TYPE_RAPID /* 45 */:
                    case WindowManagerService.H.WINDOW_REPLACEMENT_TIMEOUT /* 46 */:
                    case HdmiCecKeycode.CEC_KEYCODE_NEXT_FAVORITE /* 47 */:
                    case '0':
                    case HdmiCecKeycode.CEC_KEYCODE_CHANNEL_DOWN /* 49 */:
                    case HdmiCecKeycode.CEC_KEYCODE_PREVIOUS_CHANNEL /* 50 */:
                    case '3':
                    case '4':
                        try {
                            handleAttkKeyCommand(s, printWriter);
                            break;
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            break;
                        }
                    case '5':
                        try {
                            dumpGoogleKeybox(s, printWriter);
                            break;
                        } catch (RemoteException e2) {
                            e2.printStackTrace();
                            break;
                        }
                    case '6':
                    case '7':
                        try {
                            dumpHdcpKey(s, printWriter);
                            break;
                        } catch (RemoteException e3) {
                            e3.printStackTrace();
                            break;
                        }
                    case '8':
                    case '9':
                        try {
                            dumpWidevineKeyBox(s, printWriter);
                            break;
                        } catch (RemoteException e4) {
                            e4.printStackTrace();
                            break;
                        }
                }
                Slog.d(TAG, "--- s = " + s);
                Binder.restoreCallingIdentity(token);
                return 0;
            }
        } catch (IllegalArgumentException e5) {
            e5.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public void onHelp() {
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void handleQueryCommand(String cmd, PrintWriter printWriter) {
        char c;
        String pcb;
        switch (cmd.hashCode()) {
            case -2006928703:
                if (cmd.equals("--query_version")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1626848838:
                if (cmd.equals("--query_back_cover_color")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1415223598:
                if (cmd.equals("--query_sim_state")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1075742606:
                if (cmd.equals("--query_download_status")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -989620010:
                if (cmd.equals("--query_engineer_result")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -387321349:
                if (cmd.equals("--query_battery_level")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -210852304:
                if (cmd.equals("--query_pcb_number")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -140701283:
                if (cmd.equals("--query_indicate_info")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -92262373:
                if (cmd.equals("--query_lcd_status")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 60211637:
                if (cmd.equals("--query_product_info")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 424345972:
                if (cmd.equals("--query_sd_card_exists")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 521601571:
                if (cmd.equals("--query_download_mode")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 628115864:
                if (cmd.equals("--query_write_protect")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 802341661:
                if (cmd.equals("--query_serial_number")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1091586139:
                if (cmd.equals("--query_battery_status")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 2086669659:
                if (cmd.equals("--query_cplc")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                printResult(cmd, printWriter, "OK:190920");
                return;
            case 1:
                if (!OppoEngineerUtils.isMtkPlatform()) {
                    return;
                }
                if (!OppoEngineerNative.nativeGetPartionWriteProtectState()) {
                    printResult(cmd, printWriter, "OK:WP ON");
                    return;
                } else {
                    printResult(cmd, printWriter, "OK:WP OFF");
                    return;
                }
            case 2:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent = new Intent();
                        ComponentName componentName = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.assistant.CheckThemeConfig");
                        intent.setComponent(componentName);
                        String serverName = String.format(Locale.US, "%s_%s", cmd.replace("--", ""), Integer.valueOf(hashCode()));
                        intent.putExtra("RESPONSE_SERVER_NAME", serverName);
                        if (componentName.equals(this.mContext.startServiceAsUser(intent, UserHandle.CURRENT))) {
                            String cmdResult = new WaitForResult().apply(serverName);
                            if (!TextUtils.isEmpty(cmdResult)) {
                                printResult(cmd, printWriter, cmdResult);
                            } else {
                                printResult(cmd, printWriter, "FAIL:Error");
                            }
                        } else {
                            printResult(cmd, printWriter, "FAIL:Check Theme Code Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e) {
                    Slog.d(TAG, "exception caught : " + e.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 3:
                printResult(cmd, printWriter, "OK:" + OppoEngineerUtils.readIntFromFile("/sys/class/power_supply/battery/capacity", -1));
                return;
            case 4:
                String downloadStatus = this.mOppoEngineerService.getDownloadStatusInternal();
                if (downloadStatus == null || !downloadStatus.contains("download over")) {
                    printResult(cmd, printWriter, "FAIL:Download Not Finished");
                    return;
                } else {
                    printResult(cmd, printWriter, "OK:Download Over");
                    return;
                }
            case 5:
                int batteryStatus = OppoEngineerUtils.readIntFromFile("/sys/class/power_supply/battery/short_c_hw_status", 1);
                boolean isWriteToCriticalData = false;
                String bsarg = getNextArg();
                StringBuilder stringBuilder = new StringBuilder();
                if (bsarg == null || bsarg.length() != 12 || !Pattern.compile("[0-9]*").matcher(bsarg).matches()) {
                    stringBuilder.append("check battery stats time invalidate");
                    stringBuilder.append('\n');
                } else {
                    String time = bsarg.substring(0, 4) + SliceClientPermissions.SliceAuthority.DELIMITER + bsarg.substring(4, 6) + SliceClientPermissions.SliceAuthority.DELIMITER + bsarg.substring(6, 8) + SliceClientPermissions.SliceAuthority.DELIMITER + bsarg.substring(8, 12);
                    isWriteToCriticalData = true;
                    stringBuilder.append("check battery stats time:" + time);
                    stringBuilder.append('\n');
                    OppoEngineerUtils.writeCriticalData(70, "batterystatus check time " + time);
                }
                if (batteryStatus == 1) {
                    stringBuilder.append("OK:HW Check Battery Status Okay");
                    stringBuilder.append('\n');
                    if (isWriteToCriticalData) {
                        OppoEngineerUtils.writeCriticalData(70, "HW Check Battery Status Okay");
                    }
                } else {
                    stringBuilder.append("FAIL:HW Check  Battery DET Exception");
                    stringBuilder.append('\n');
                    if (isWriteToCriticalData) {
                        OppoEngineerUtils.writeCriticalData(70, "IC Check Battery Status Fail");
                    }
                }
                if (OppoEngineerUtils.readIntFromFile("/sys/class/power_supply/battery/short_ic_otp_status", 1) == 1) {
                    stringBuilder.append("OK:IC Check Battery Status Okay");
                    stringBuilder.append('\n');
                    if (isWriteToCriticalData) {
                        OppoEngineerUtils.writeCriticalData(70, "IC Check Battery Status Okay");
                    }
                } else {
                    stringBuilder.append("FAIL:IC Check Battery DET Exception");
                    stringBuilder.append('\n');
                    if (isWriteToCriticalData) {
                        OppoEngineerUtils.writeCriticalData(70, "IC Check Battery Status Error");
                    }
                }
                if (isWriteToCriticalData) {
                    OppoEngineerUtils.syncCacheToEmmc();
                    Intent battery_status_intent = new Intent("oppo.intent.action.BATTERY_STATUS_MANUAL_CHECK");
                    battery_status_intent.putExtra("check_time", bsarg);
                    this.mContext.sendBroadcastAsUser(new Intent(battery_status_intent), UserHandle.CURRENT, "oppo.permission.OPPO_COMPONENT_SAFE");
                }
                if (SystemProperties.get("persist.sys.hw_status_flag", "").equals(Integer.toString(6))) {
                    stringBuilder.append("hw_errcode:6(battery hw check err)");
                    stringBuilder.append('\n');
                } else if (SystemProperties.get("persist.sys.hw_status_flag", "").equals(Integer.toString(-6))) {
                    stringBuilder.append("hw_errcode:-6(battery hw check repair)");
                    stringBuilder.append('\n');
                } else {
                    stringBuilder.append("hw_errcode:0(no error)");
                    stringBuilder.append('\n');
                }
                String errTime = SystemProperties.get("persist.sys.hw_errTime", "");
                if (!TextUtils.isEmpty(errTime)) {
                    stringBuilder.append("hw_err_time:" + errTime);
                    stringBuilder.append('\n');
                } else {
                    stringBuilder.append("hw_err_time:0000/00/00/0000");
                    stringBuilder.append('\n');
                }
                if (SystemProperties.get("persist.sys.ic_status_flag", "").equals(Integer.toString(7))) {
                    stringBuilder.append("ic_errcode:7(battery ic check err)");
                    stringBuilder.append('\n');
                } else if (SystemProperties.get("persist.sys.ic_status_flag", "").equals(Integer.toString(-7))) {
                    stringBuilder.append("ic_errcode:-7(battery ic check repair)");
                    stringBuilder.append('\n');
                } else {
                    stringBuilder.append("ic_errcode:0(no error)");
                    stringBuilder.append('\n');
                }
                String errTime2 = SystemProperties.get("persist.sys.ic_errTime", "");
                if (!TextUtils.isEmpty(errTime2)) {
                    stringBuilder.append("ic_err_time:" + errTime2);
                    stringBuilder.append('\n');
                } else {
                    stringBuilder.append("ic_err_time:0000/00/00/0000");
                    stringBuilder.append('\n');
                }
                printResult(cmd, printWriter, stringBuilder.toString().trim());
                return;
            case 6:
                byte[] result = OppoEngineerNative.nativeGetProductLineTestResult();
                if (result == null || result.length <= 0) {
                    printResult(cmd, printWriter, "FAIL:Access Fail");
                    return;
                }
                printResult(cmd, printWriter, "OK:" + OppoEngineerUtils.bytesToHexString(result));
                return;
            case 7:
                String downloadMode = this.mOppoEngineerService.getDownloadModeInternal();
                if (TextUtils.isEmpty(downloadMode) || !downloadMode.contains("\"login_mode\":\t\"production\"")) {
                    printResult(cmd, printWriter, "OK:Switch is Off");
                    return;
                } else {
                    printResult(cmd, printWriter, "OK:Switch is On");
                    return;
                }
            case '\b':
                String serial = SystemProperties.get("persist.sys.oppo.serialno", "");
                if (OppoEngineerUtils.isOppoSerialNoValid(serial)) {
                    printResult(cmd, printWriter, String.format(Locale.US, "OK:%s", serial));
                    return;
                }
                printResult(cmd, printWriter, "FAIL:Access Fail");
                return;
            case '\t':
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
                if (nfcAdapter != null && !SystemProperties.getBoolean("nfc.support_japan_felica", false)) {
                    try {
                        String cplc = (String) Class.forName("android.nfc.NfcAdapter").getMethod("getCplc", new Class[0]).invoke(nfcAdapter, new Object[0]);
                        if (!TextUtils.isEmpty(cplc)) {
                            printResult(cmd, printWriter, "OK:" + cplc);
                            return;
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                printResult(cmd, printWriter, "FAIL:Access Fail");
                return;
            case '\n':
                if (OppoEngineerUtils.isMtkPlatform()) {
                    pcb = SystemProperties.get("vendor.gsm.serial", "");
                } else {
                    pcb = SystemProperties.get("gsm.serial", "");
                }
                if (TextUtils.isEmpty(pcb)) {
                    printResult(cmd, printWriter, "FAIL:Access Fail");
                    return;
                }
                printResult(cmd, printWriter, "OK:" + pcb);
                return;
            case 11:
                String state = SystemProperties.get("gsm.sim.state");
                if (!TextUtils.isEmpty(state)) {
                    printResult(cmd, printWriter, "OK:" + state);
                    return;
                }
                printResult(cmd, printWriter, "FAIL:Access Fail");
                return;
            case '\f':
                boolean isSdExists = false;
                try {
                    List<VolumeInfo> volumes = ((StorageManager) this.mContext.getSystemService("storage")).getVolumes();
                    if (volumes != null) {
                        Iterator<VolumeInfo> it = volumes.iterator();
                        while (true) {
                            if (it.hasNext()) {
                                VolumeInfo volume = it.next();
                                if (volume.disk != null && volume.disk.isSd()) {
                                    isSdExists = true;
                                }
                            }
                        }
                    }
                    if (isSdExists) {
                        printResult(cmd, printWriter, "OK:SD Card Found");
                        return;
                    } else {
                        printResult(cmd, printWriter, "OK:SD Card Not Found");
                        return;
                    }
                } catch (Exception e3) {
                    e3.printStackTrace();
                    printResult(cmd, printWriter, "FAIL:Access Fail");
                    return;
                }
            case '\r':
                printWriter.println("OK:" + getIndicateInfo());
                return;
            case 14:
                byte[] bInfo = OppoEngineerNative.nativeReadEngineerData(1000062);
                if (bInfo != null && bInfo.length > 256) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    for (int i = 0; i < bInfo.length / 256; i++) {
                        byte[] bTemp = new byte[256];
                        System.arraycopy(bInfo, i * 256, bTemp, 0, 256);
                        int length = 0;
                        int j = 0;
                        while (true) {
                            try {
                                if (j < bTemp.length) {
                                    if (bTemp[j] == 0) {
                                        length = j;
                                    } else {
                                        j++;
                                    }
                                }
                            } catch (Exception e4) {
                                e4.printStackTrace();
                            }
                        }
                        String sTemp = new String(bTemp, 0, length, StandardCharsets.UTF_8);
                        if (!TextUtils.isEmpty(sTemp)) {
                            stringBuilder2.append(sTemp);
                            stringBuilder2.append(StringUtils.LF);
                        }
                    }
                    String info = stringBuilder2.toString();
                    if (!TextUtils.isEmpty(info)) {
                        printResult(cmd, printWriter, String.format(Locale.US, "OK:%s", info.trim()));
                        return;
                    }
                }
                printResult(cmd, printWriter, "FAIL:Query Product Info Fail");
                return;
            case 15:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent2 = new Intent();
                        ComponentName componentName2 = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.assistant.CheckLCDStatus");
                        intent2.setComponent(componentName2);
                        String serverName2 = String.format(Locale.US, "%s_%s", cmd.replace("--", ""), Integer.valueOf(hashCode()));
                        intent2.putExtra("RESPONSE_SERVER_NAME", serverName2);
                        if (componentName2.equals(this.mContext.startServiceAsUser(intent2, UserHandle.CURRENT))) {
                            String cmdResult2 = new WaitForResult().apply(serverName2);
                            if (!TextUtils.isEmpty(cmdResult2)) {
                                printResult(cmd, printWriter, cmdResult2);
                            } else {
                                printResult(cmd, printWriter, "FAIL:Error");
                            }
                        } else {
                            printResult(cmd, printWriter, "FAIL:Check LCD Status Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e5) {
                    Slog.d(TAG, "exception caught : " + e5.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            default:
                return;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void handleUpdateCommand(String cmd, PrintWriter printWriter) {
        char c;
        switch (cmd.hashCode()) {
            case -1697223522:
                if (cmd.equals("--update_serial_number")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -30237543:
                if (cmd.equals("--update_back_cover_color")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 2057790484:
                if (cmd.equals("--update_product_info")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2109924887:
                if (cmd.equals("--update_engineer_result")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            String value = getNextArgRequired();
            String fullCommand = String.format(Locale.US, "%s colorID=%s", cmd, value);
            if (!OppoEngineerUtils.isBackCoverColorIdValid(value)) {
                printResult(fullCommand, printWriter, "FAIL:Invalid Parameter");
            } else if (this.mOppoEngineerService.setBackCoverColorIdInternal(value)) {
                printResult(fullCommand, printWriter, "OK:Update Success");
            } else {
                printResult(fullCommand, printWriter, "FAIL:Update Fail");
            }
        } else if (c == 1) {
            try {
                String position = getNextArgRequired();
                String value2 = getNextArgRequired();
                String fullCommand2 = String.format(Locale.US, "%s position=%s, value=%s", cmd, position, value2);
                try {
                    int targetPosition = Integer.valueOf(position).intValue();
                    int targetValue = Integer.valueOf(value2).intValue();
                    if (targetPosition < 0 || targetPosition >= 128 || targetValue < 0 || targetValue >= 128) {
                        printResult(fullCommand2, printWriter, "FAIL:Unknown Argument");
                    } else if (OppoEngineerNative.nativeSetProductLineTestResult(targetPosition, targetValue)) {
                        printResult(fullCommand2, printWriter, "OK:Update Success");
                    } else {
                        printResult(fullCommand2, printWriter, "FAIL:Update Fail");
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    printResult(fullCommand2, printWriter, "FAIL:Unknown Argument");
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                printResult(cmd, printWriter, "FAIL:ERROR");
            }
        } else if (c == 2) {
            String value3 = getNextArgRequired();
            String fullCommand3 = String.format(Locale.US, "%s sn=%s", cmd, value3);
            if (OppoEngineerUtils.isOppoSerialNoValid(value3)) {
                byte[] data = value3.getBytes(StandardCharsets.UTF_8);
                if (OppoEngineerNative.nativeSaveEngineerData(1000058, data, data.length)) {
                    try {
                        SystemProperties.set("persist.sys.oppo.serialno", value3);
                    } catch (Exception e3) {
                        Slog.i(TAG, "set oppo serial no caught exception : " + e3.getMessage());
                    }
                    printResult(fullCommand3, printWriter, "OK:Update Success");
                    return;
                }
                printResult(fullCommand3, printWriter, "FAIL:Update Fail");
                return;
            }
            printResult(fullCommand3, printWriter, "FAIL:Invalid Parameter");
        } else if (c == 3) {
            String value4 = getNextArgRequired();
            String fullCommand4 = String.format(Locale.US, "%s product_info=%s", cmd, value4);
            if (TextUtils.isEmpty(value4) || !value4.contains("=") || value4.length() >= 256) {
                printResult(fullCommand4, printWriter, "FAIL:Invalid Parameter");
                return;
            }
            byte[] data2 = value4.trim().getBytes(StandardCharsets.UTF_8);
            if (OppoEngineerNative.nativeSaveEngineerData(1000061, data2, data2.length)) {
                printResult(fullCommand4, printWriter, "OK:Update Success");
            } else {
                printResult(fullCommand4, printWriter, "FAIL:Update Fail");
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void handleResetCommand(String cmd, PrintWriter printWriter) {
        char c;
        switch (cmd.hashCode()) {
            case -1101530093:
                if (cmd.equals("--reset_back_cover_color")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -683930006:
                if (cmd.equals("--reset_download_mode")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -577415713:
                if (cmd.equals("--reset_write_protect")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 135704413:
                if (cmd.equals("--reset_engineer_result")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c != 0) {
            if (c != 1) {
                if (c != 2) {
                    if (c == 3) {
                        if (OppoEngineerNative.nativeSaveEngineerData(1000056, new byte[1], 1)) {
                            printResult(cmd, printWriter, "OK:Reset Success");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Reset Fail");
                        }
                    }
                } else if (OppoEngineerNative.nativeResetProductLineTestResult()) {
                    printResult(cmd, printWriter, "OK:Reset Success");
                } else {
                    printResult(cmd, printWriter, "FAIL:Reset Fail");
                }
            } else if (OppoEngineerUtils.isMtkPlatform()) {
                if (this.mOppoEngineerService.resetWriteProtectStateInternal()) {
                    printResult(cmd, printWriter, "OK:Reset Success");
                } else {
                    printResult(cmd, printWriter, "FAIL:Reset Fail");
                }
            }
        } else if (this.mOppoEngineerService.setBackCoverColorIdInternal(null)) {
            printResult(cmd, printWriter, "OK:Reset Success");
        } else {
            printResult(cmd, printWriter, "FAIL:Reset Fail");
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void handleExecuteCommand(String cmd, PrintWriter printWriter) {
        char c;
        switch (cmd.hashCode()) {
            case -1949319228:
                if (cmd.equals("--execute_stop_dsocket")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1869321372:
                if (cmd.equals("--execute_detect_ms_result")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1783115275:
                if (cmd.equals("--execute_shutdown_device")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1777399994:
                if (cmd.equals("--execute_reboot_device")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1543741350:
                if (cmd.equals("--execute_master_clear")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1033108208:
                if (cmd.equals("--execute_start_dsocket")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -311469421:
                if (cmd.equals("--execute_check_key_status")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -118855923:
                if (cmd.equals("--execute_remove_cover_ui")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -92364746:
                if (cmd.equals("--execute_stop_mmi")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 254018706:
                if (cmd.equals("--execute_model_test")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 647245131:
                if (cmd.equals("--execute_power_off")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 901886176:
                if (cmd.equals("--execute_switch_wifi_mmi")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 1163213860:
                if (cmd.equals("--execute_add_cover_ui")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 1433406376:
                if (cmd.equals("--execute_check_calibration_status")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 1470948161:
                if (cmd.equals("--execute_launch_wifi_mmi")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1681696040:
                if (cmd.equals("--execute_engineer_order")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1787655176:
                if (cmd.equals("--execute_switch_production_mode")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1888570009:
                if (cmd.equals("--execute_show_production_message")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1967745922:
                if (cmd.equals("--execute_start_mmi")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 2096971875:
                if (cmd.equals("--execute_switch_log")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent serverIntent = new Intent();
                        ComponentName componentName = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.mmi.connector.AutoTestServer");
                        serverIntent.setComponent(componentName);
                        if (componentName.equals(this.mContext.startServiceAsUser(serverIntent, UserHandle.CURRENT))) {
                            SystemClock.sleep(500);
                            printResult(cmd, printWriter, "OK:Start MMI Server Success");
                            this.mContext.sendBroadcastAsUser(new Intent("oppo.intent.action.START_OPPO_AT_SERVER"), UserHandle.CURRENT, "oppo.permission.OPPO_COMPONENT_SAFE");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Start MMI Server Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e) {
                    Slog.d(TAG, "start mmi server exception caught : " + e.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 1:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent serverIntent2 = new Intent();
                        serverIntent2.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.mmi.connector.AutoTestServer"));
                        if (this.mContext.stopServiceAsUser(serverIntent2, UserHandle.CURRENT)) {
                            SystemClock.sleep(500);
                            printResult(cmd, printWriter, "OK:Stop MMI Server Success");
                            this.mContext.sendBroadcastAsUser(new Intent("oppo.intent.action.STOP_OPPO_AT_SERVER"), UserHandle.CURRENT, "oppo.permission.OPPO_COMPONENT_SAFE");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Stop MMI Server Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e2) {
                    Slog.d(TAG, "stop mmi server exception caught : " + e2.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 2:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent = parseCommandArgs(this);
                        intent.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.manualtest.MasterClear"));
                        String serverName = String.format(Locale.US, "%s_%s", cmd.replace("--", ""), Integer.valueOf(hashCode()));
                        intent.putExtra("RESPONSE_SERVER_NAME", serverName);
                        intent.addFlags(268435456);
                        intent.addFlags(32768);
                        int result = startActivityAsUserInternal(this.mContext, intent, 0);
                        if (ActivityManager.isStartResultSuccessful(result)) {
                            String cmdResult = new WaitForResult().apply(serverName);
                            if (!TextUtils.isEmpty(cmdResult)) {
                                printResult(cmd, printWriter, cmdResult);
                            } else {
                                printResult(cmd, printWriter, "FAIL:Error");
                            }
                        } else {
                            printResult(cmd, printWriter, "FAIL:Start Master Clear Fail, Error Code " + result);
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e3) {
                    Slog.i(TAG, "exception caught " + e3.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 3:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent2 = new Intent();
                        intent2.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.PowerOff"));
                        String serverName2 = String.format(Locale.US, "%s_%s", cmd.replace("--", ""), Integer.valueOf(hashCode()));
                        intent2.putExtra("RESPONSE_SERVER_NAME", serverName2);
                        intent2.addFlags(268435456);
                        intent2.addFlags(32768);
                        int result2 = startActivityAsUserInternal(this.mContext, intent2, 0);
                        if (ActivityManager.isStartResultSuccessful(result2)) {
                            String cmdResult2 = new WaitForResult().apply(serverName2);
                            if (!TextUtils.isEmpty(cmdResult2)) {
                                printResult(cmd, printWriter, cmdResult2);
                            } else {
                                printResult(cmd, printWriter, "FAIL:Error");
                            }
                        } else {
                            printResult(cmd, printWriter, "FAIL:Start Power Off Fail, Error Code " + result2);
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e4) {
                    Slog.i(TAG, "exception caught " + e4.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 4:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent3 = parseCommandArgs(this);
                        intent3.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.manualtest.modeltest.ModelTestImpl"));
                        intent3.addFlags(268435456);
                        intent3.addFlags(32768);
                        int result3 = startActivityAsUserInternal(this.mContext, intent3, 0);
                        if (ActivityManager.isStartResultSuccessful(result3)) {
                            printResult(cmd, printWriter, "OK:Start ModelTest Success");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Start ModelTest Fail, Error Code " + result3);
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e5) {
                    Slog.i(TAG, "exception caught " + e5.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 5:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent4 = parseCommandArgs(this);
                        intent4.setAction("com.oppo.engineermode.EngineerModeMain");
                        intent4.setPackage("com.oppo.engineermode");
                        intent4.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
                        intent4.addFlags(268435456);
                        this.mContext.sendBroadcastAsUser(intent4, UserHandle.CURRENT, "oppo.permission.OPPO_COMPONENT_SAFE");
                        printResult(cmd, printWriter, "OK:Start Engineer Order Success");
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e6) {
                    Slog.i(TAG, "exception caught " + e6.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 6:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent5 = parseCommandArgs(this);
                        ComponentName componentName2 = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.assistant.SwitchProductionMode");
                        intent5.setComponent(componentName2);
                        if (componentName2.equals(this.mContext.startServiceAsUser(intent5, UserHandle.CURRENT))) {
                            printResult(cmd, printWriter, "OK:Switch Mode Success");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Switch Mode Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e7) {
                    Slog.i(TAG, "exception caught " + e7.getMessage());
                    printResult(cmd, printWriter, "FAIL:ERROR");
                    return;
                }
            case 7:
                if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                    Intent intent6 = new Intent();
                    intent6.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.DetectMasterClearResult"));
                    String serverName3 = String.format(Locale.US, "%s_%s", cmd.replace("--", ""), Integer.valueOf(hashCode()));
                    intent6.putExtra("RESPONSE_SERVER_NAME", serverName3);
                    intent6.addFlags(268435456);
                    int result4 = startActivityAsUserInternal(this.mContext, intent6, 0);
                    if (ActivityManager.isStartResultSuccessful(result4)) {
                        String cmdResult3 = new WaitForResult().apply(serverName3);
                        if (!TextUtils.isEmpty(cmdResult3)) {
                            printResult(cmd, printWriter, cmdResult3);
                            return;
                        } else {
                            printResult(cmd, printWriter, "FAIL:Error");
                            return;
                        }
                    } else {
                        printResult(cmd, printWriter, "FAIL:Error Code " + result4);
                        return;
                    }
                } else {
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                }
            case '\b':
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent7 = parseCommandArgs(this);
                        intent7.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.ShowRemoteMessage"));
                        intent7.addFlags(268435456);
                        int result5 = startActivityAsUserInternal(this.mContext, intent7, 0);
                        if (ActivityManager.isStartResultSuccessful(result5)) {
                            printResult(cmd, printWriter, "OK:Show Message Success");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Show Message Fail, Error Code " + result5);
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e8) {
                    Slog.i(TAG, "exception caught " + e8.getMessage());
                    printResult(cmd, printWriter, "FAIL:ERROR");
                    return;
                }
            case '\t':
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent8 = parseCommandArgs(this);
                        ComponentName componentName3 = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.assistant.SwitchWifiMMI");
                        intent8.setComponent(componentName3);
                        if (componentName3.equals(this.mContext.startServiceAsUser(intent8, UserHandle.CURRENT))) {
                            printResult(cmd, printWriter, "OK:Switch Wifi MMI Success");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Switch Wifi MMI Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e9) {
                    Slog.i(TAG, "exception caught " + e9.getMessage());
                    printResult(cmd, printWriter, "FAIL:ERROR");
                    return;
                }
            case '\n':
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent9 = parseCommandArgs(this);
                        intent9.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.wireless.WifiAdbHelper"));
                        intent9.addFlags(268435456);
                        int result6 = startActivityAsUserInternal(this.mContext, intent9, 0);
                        if (ActivityManager.isStartResultSuccessful(result6)) {
                            printResult(cmd, printWriter, "OK:Launch Wifi MMI Success");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Launch Wifi MMI Fail, Error Code " + result6);
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e10) {
                    Slog.i(TAG, "exception caught " + e10.getMessage());
                    printResult(cmd, printWriter, "FAIL:ERROR");
                    return;
                }
            case 11:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent10 = new Intent("android.intent.action.REBOOT");
                        intent10.putExtra("android.intent.extra.KEY_CONFIRM", false);
                        intent10.setFlags(268435456);
                        int result7 = startActivityAsUserInternal(this.mContext, intent10, 0);
                        if (ActivityManager.isStartResultSuccessful(result7)) {
                            printResult(cmd, printWriter, "OK:Reboot Started");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Reboot Fail, Error Code " + result7);
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e11) {
                    Slog.i(TAG, "exception caught " + e11.getMessage());
                    printResult(cmd, printWriter, "FAIL:ERROR");
                    return;
                }
            case '\f':
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent11 = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                        intent11.putExtra("android.intent.extra.KEY_CONFIRM", false);
                        intent11.setFlags(268435456);
                        int result8 = startActivityAsUserInternal(this.mContext, intent11, 0);
                        if (ActivityManager.isStartResultSuccessful(result8)) {
                            printResult(cmd, printWriter, "OK:Shutdown Started");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Shutdown Fail, Error Code " + result8);
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e12) {
                    Slog.i(TAG, "exception caught " + e12.getMessage());
                    printResult(cmd, printWriter, "FAIL:ERROR");
                    return;
                }
            case '\r':
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent12 = parseCommandArgs(this);
                        ComponentName componentName4 = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.assistant.SmartCoverUIService");
                        intent12.setComponent(componentName4);
                        if (componentName4.equals(this.mContext.startServiceAsUser(intent12, UserHandle.CURRENT))) {
                            printResult(cmd, printWriter, "OK:Add Cover UI Success");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Add Cover UI Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e13) {
                    Slog.i(TAG, "exception caught " + e13.getMessage());
                    printResult(cmd, printWriter, "FAIL:ERROR");
                    return;
                }
            case 14:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent serverIntent3 = new Intent();
                        serverIntent3.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.assistant.SmartCoverUIService"));
                        if (this.mContext.stopServiceAsUser(serverIntent3, UserHandle.CURRENT)) {
                            printResult(cmd, printWriter, "OK:Remove Cover UI Success");
                        } else {
                            printResult(cmd, printWriter, "FAIL:Remove Cover UI Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e14) {
                    Slog.d(TAG, "exception caught : " + e14.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 15:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent13 = new Intent();
                        ComponentName componentName5 = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.assistant.CheckKeyStatus");
                        intent13.setComponent(componentName5);
                        String serverName4 = String.format(Locale.US, "%s_%s", cmd.replace("--", ""), Integer.valueOf(hashCode()));
                        intent13.putExtra("RESPONSE_SERVER_NAME", serverName4);
                        if (componentName5.equals(this.mContext.startServiceAsUser(intent13, UserHandle.CURRENT))) {
                            String cmdResult4 = new WaitForResult().apply(serverName4);
                            if (!TextUtils.isEmpty(cmdResult4)) {
                                printResult(cmd, printWriter, cmdResult4);
                            } else {
                                printResult(cmd, printWriter, "FAIL:Error");
                            }
                        } else {
                            printResult(cmd, printWriter, "FAIL:Check Key Status Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e15) {
                    Slog.d(TAG, "exception caught : " + e15.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 16:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        String argument = getNextArgRequired();
                        Intent intent14 = new Intent();
                        ComponentName componentName6 = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.assistant.SwitchWirelessSupport");
                        intent14.setComponent(componentName6);
                        String serverName5 = String.format(Locale.US, "%s_%s", cmd.replace("--", ""), Integer.valueOf(hashCode()));
                        intent14.putExtra("RESPONSE_SERVER_NAME", serverName5);
                        intent14.putExtra("COMMAND_TYPE", "START");
                        intent14.putExtra("SERVER_IP_ADDRESS", argument);
                        if (componentName6.equals(this.mContext.startServiceAsUser(intent14, UserHandle.CURRENT))) {
                            String cmdResult5 = new WaitForResult().apply(serverName5);
                            if (!TextUtils.isEmpty(cmdResult5)) {
                                printResult(cmd, printWriter, cmdResult5);
                            } else {
                                printResult(cmd, printWriter, "FAIL:Error");
                            }
                        } else {
                            printResult(cmd, printWriter, "FAIL:Start Service Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e16) {
                    Slog.d(TAG, "exception caught : " + e16.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 17:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent15 = new Intent();
                        ComponentName componentName7 = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.assistant.SwitchWirelessSupport");
                        intent15.setComponent(componentName7);
                        String serverName6 = String.format(Locale.US, "%s_%s", cmd.replace("--", ""), Integer.valueOf(hashCode()));
                        intent15.putExtra("RESPONSE_SERVER_NAME", serverName6);
                        intent15.putExtra("COMMAND_TYPE", "STOP");
                        if (componentName7.equals(this.mContext.startServiceAsUser(intent15, UserHandle.CURRENT))) {
                            String cmdResult6 = new WaitForResult().apply(serverName6);
                            if (!TextUtils.isEmpty(cmdResult6)) {
                                printResult(cmd, printWriter, cmdResult6);
                            } else {
                                printResult(cmd, printWriter, "FAIL:Error");
                            }
                        } else {
                            printResult(cmd, printWriter, "FAIL:Start Service Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e17) {
                    Slog.d(TAG, "exception caught : " + e17.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            case 18:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent16 = parseCommandArgs(this);
                        intent16.setComponent(new ComponentName("com.oppo.logkit", "com.oppo.logkit.service.LogKitSwitchReceiver"));
                        intent16.setAction("oppo.intent.action.StartOrStopLogcat");
                        intent16.addFlags(268435456);
                        intent16.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
                        this.mContext.sendBroadcastAsUser(intent16, UserHandle.CURRENT, "oppo.permission.OPPO_COMPONENT_SAFE");
                        SystemProperties.set("persist.sys.log.user", "0");
                        printResult(cmd, printWriter, "OK:Switch Log Done");
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e18) {
                    Slog.i(TAG, "exception caught " + e18.getMessage());
                    printResult(cmd, printWriter, "FAIL:ERROR");
                    return;
                }
            case 19:
                try {
                    if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                        Intent intent17 = parseCommandArgs(this);
                        ComponentName componentName8 = new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.productiondata.ProductionDataDetectService");
                        intent17.setComponent(componentName8);
                        String serverName7 = String.format(Locale.US, "%s_%s", cmd.replace("--", ""), Integer.valueOf(hashCode()));
                        intent17.putExtra("RESPONSE_SERVER_NAME", serverName7);
                        if (componentName8.equals(this.mContext.startServiceAsUser(intent17, UserHandle.CURRENT))) {
                            String cmdResult7 = new WaitForResult().apply(serverName7);
                            if (!TextUtils.isEmpty(cmdResult7)) {
                                printResult(cmd, printWriter, cmdResult7);
                            } else {
                                printResult(cmd, printWriter, "FAIL:Error");
                            }
                        } else {
                            printResult(cmd, printWriter, "FAIL:Check Calibration Status Fail");
                        }
                        return;
                    }
                    printResult(cmd, printWriter, "FAIL:System Not Ready");
                    return;
                } catch (Exception e19) {
                    Slog.d(TAG, "exception caught : " + e19.getMessage());
                    printResult(cmd, printWriter, "FAIL:Error");
                    return;
                }
            default:
                return;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void handleAttkKeyCommand(String cmd, PrintWriter printWriter) throws RemoteException {
        char c;
        int attk_ret;
        int ali_key_status;
        int crypto_ret;
        if ("--enable_rpmb".equals(cmd) || this.mOppoEngineerService.getRpmbEnableState() == 0) {
            Object fpPayDev = OppoEngineerUtils.getFingerprintPaySerice();
            IEngineer engineer = IEngineer.getService();
            if (fpPayDev == null || engineer == null) {
                printResult(cmd, printWriter, "IFingerprintPay or IEngineer service is null!");
                return;
            }
            switch (cmd.hashCode()) {
                case -1614897061:
                    if (cmd.equals("--get_device_id")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1614157928:
                    if (cmd.equals("--crypto_eng_verify")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -551205278:
                    if (cmd.equals("--verify_attk_key_pair_only")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -424502248:
                    if (cmd.equals("--verify_ali_key")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -233355281:
                    if (cmd.equals("--enable_rpmb")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -5057884:
                    if (cmd.equals("--get_tee_version")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 31176723:
                    if (cmd.equals("--export_attk_public_key")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 531892169:
                    if (cmd.equals("--verify_attk_key_pair")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1089225669:
                    if (cmd.equals("--generate_attk_key_pair")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    if (engineer.verifyAttkKeyPair() == 0 || engineer.generateAttkKeyPair(3) == 0) {
                        attk_ret = 0;
                        Slog.d(TAG, "write ATTK successed!");
                    } else {
                        attk_ret = 10000;
                        Slog.d(TAG, "write ATTK failed!");
                    }
                    if (OppoEngineerUtils.alikeyVerify(fpPayDev) == 0 || OppoEngineerUtils.alikeyWrite(fpPayDev) == 0) {
                        ali_key_status = 0;
                        Slog.d(TAG, "write alikey successed");
                    } else {
                        ali_key_status = 20000;
                        Slog.d(TAG, "write alikey failed!");
                    }
                    if (SystemProperties.getBoolean("ro.build.release_type", false)) {
                        if (runCryptoCmd((byte) 1) == 0 || runCryptoCmd((byte) 2) == 0) {
                            crypto_ret = 0;
                            Slog.d(TAG, "write crypto successed");
                        } else {
                            crypto_ret = 400000;
                            Slog.d(TAG, "write crypto failed!");
                        }
                    } else if (runCryptoCmd((byte) 2) != 0) {
                        crypto_ret = 400000;
                        Slog.d(TAG, "write crypto failed!");
                    } else {
                        crypto_ret = 0;
                        Slog.d(TAG, "write crypto successed");
                    }
                    printResult(cmd, printWriter, "ret: " + (attk_ret | ali_key_status | crypto_ret));
                    return;
                case 1:
                    int ret = engineer.verifyAttkKeyPair();
                    int ali_key_status2 = OppoEngineerUtils.getAlikeyStatus(fpPayDev);
                    Slog.d(TAG, "get_alikey_status " + ali_key_status2);
                    if (ali_key_status2 == -1) {
                        ali_key_status2 = 0;
                    }
                    int crypto_ret2 = runCryptoCmd((byte) 1);
                    Slog.d(TAG, "verify crypto " + crypto_ret2);
                    printResult(cmd, printWriter, "ret: " + (ret | ali_key_status2 | crypto_ret2));
                    return;
                case 2:
                    printResult(cmd, printWriter, "ret: " + engineer.verifyAttkKeyPair());
                    return;
                case 3:
                    printResult(cmd, printWriter, "ret: " + OppoEngineerUtils.getAlikeyStatus(fpPayDev));
                    return;
                case 4:
                    engineer.exportAttkKeyPair(new IEngineer.exportAttkKeyPairCallback(cmd, printWriter) {
                        /* class com.android.server.engineer.$$Lambda$OppoEngineerShell$i2ds_xMdLjny9JsnysJmOntTAI */
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ PrintWriter f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer.exportAttkKeyPairCallback
                        public final void onValues(int i, String str) {
                            OppoEngineerShell.this.lambda$handleAttkKeyCommand$0$OppoEngineerShell(this.f$1, this.f$2, i, str);
                        }
                    });
                    return;
                case 5:
                    engineer.getDeviceId(new IEngineer.getDeviceIdCallback(cmd, printWriter) {
                        /* class com.android.server.engineer.$$Lambda$OppoEngineerShell$N6aM5bQuDouOrqdT7SLf3BqSBRw */
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ PrintWriter f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer.getDeviceIdCallback
                        public final void onValues(int i, String str) {
                            OppoEngineerShell.this.lambda$handleAttkKeyCommand$1$OppoEngineerShell(this.f$1, this.f$2, i, str);
                        }
                    });
                    return;
                case 6:
                    if (OppoEngineerUtils.enableRpmb(fpPayDev) != 0) {
                        printResult(cmd, printWriter, "rpmb_enable failed");
                        return;
                    } else {
                        printResult(cmd, printWriter, "rpmb_enable success");
                        return;
                    }
                case 7:
                    printResult(cmd, printWriter, String.valueOf(runCryptoCmd((byte) 1)));
                    return;
                case '\b':
                    if (OppoEngineerUtils.isMtkPlatform()) {
                        printResult(cmd, printWriter, "mtk_trustonic_tee_version 1.0\n");
                        return;
                    }
                    return;
                default:
                    return;
            }
        } else {
            printResult(cmd, printWriter, "Fail, RPMB ENABLE sTATE ERROR");
            Slog.d(TAG, "rpmb enable state error");
        }
    }

    public /* synthetic */ void lambda$handleAttkKeyCommand$0$OppoEngineerShell(String cmd, PrintWriter printWriter, int ret, String key_blob) {
        printResult(cmd, printWriter, "ret: " + ret);
        printResult(cmd, printWriter, "public key: length = " + key_blob.length());
        printResult(cmd, printWriter, key_blob);
    }

    public /* synthetic */ void lambda$handleAttkKeyCommand$1$OppoEngineerShell(String cmd, PrintWriter printWriter, int ret, String device_id) {
        printResult(cmd, printWriter, "ret: " + ret);
        printResult(cmd, printWriter, "device id: length = " + device_id.length());
        printResult(cmd, printWriter, device_id);
    }

    private int runCryptoCmd(byte cmd_id) throws RemoteException {
        ArrayList<Byte> out_buf;
        ArrayList<Byte> crypto = new ArrayList<>();
        crypto.add(Byte.valueOf(cmd_id));
        crypto.add((byte) 0);
        crypto.add((byte) 0);
        crypto.add((byte) 0);
        if (OppoEngineerUtils.getCryptoSerice() != null && (out_buf = OppoEngineerUtils.cryptoengInvokeCommand(crypto)) != null && out_buf.size() == 4 && out_buf.get(0).byteValue() == 0 && out_buf.get(1).byteValue() == 0 && out_buf.get(2).byteValue() == 0 && out_buf.get(3).byteValue() == 0) {
            return 0;
        }
        return -1;
    }

    private void dumpGoogleKeybox(String cmd, PrintWriter printWriter) throws RemoteException {
        if (this.mOppoEngineerService.getRpmbEnableState() != 0) {
            printResult(cmd, printWriter, "Fail, RPMB ENABLE sTATE ERROR");
            Slog.d(TAG, "rpmb enable state error");
        } else if (runCryptoCmd((byte) 3) == 0 || runCryptoCmd((byte) 4) == 0) {
            printResult(cmd, printWriter, "OK\n");
            Slog.d(TAG, "write crypto successed");
        } else {
            printResult(cmd, printWriter, "ERROR\n");
            Slog.d(TAG, "write crypto failed!");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x005b  */
    private void dumpHdcpKey(String cmd, PrintWriter printWriter) throws RemoteException {
        char c;
        int hashCode = cmd.hashCode();
        if (hashCode != -1666414628) {
            if (hashCode == -614506708 && cmd.equals("--hdcp_provision_keybox")) {
                c = 0;
                if (c != 0) {
                    if (c != 1) {
                        printResult(cmd, printWriter, "Invalid parameter\n");
                        Slog.d(TAG, "hdcp Invalid parameter");
                        return;
                    } else if (runCryptoCmd((byte) 52) != 0) {
                        printResult(cmd, printWriter, "Fail\n");
                        Slog.d(TAG, "verify hdcp failed!");
                        return;
                    } else {
                        printResult(cmd, printWriter, "Pass\n");
                        Slog.d(TAG, "verify hdcp successed");
                        return;
                    }
                } else if (runCryptoCmd((byte) 51) != 0) {
                    printResult(cmd, printWriter, "Fail\n");
                    Slog.d(TAG, "write hdcp failed!");
                    return;
                } else {
                    printResult(cmd, printWriter, "Pass\n");
                    Slog.d(TAG, "write hdcp successed");
                    return;
                }
            }
        } else if (cmd.equals("--hdcp_verify_keybox")) {
            c = 1;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    private void dumpWidevineKeyBox(String cmd, PrintWriter printWriter) throws RemoteException {
        ArrayList<Byte> outBuf;
        ArrayList<Byte> outBuf2;
        if (this.mOppoEngineerService.getRpmbEnableState() != 0) {
            printResult(cmd, printWriter, "Fail, RPMB ENABLE sTATE ERROR");
            Slog.d(TAG, "rpmb enable state error");
            return;
        }
        char c = 65535;
        int hashCode = cmd.hashCode();
        if (hashCode != -2088691726) {
            if (hashCode == -712323242 && cmd.equals("--provision_keybox")) {
                c = 0;
            }
        } else if (cmd.equals("--verify_keybox")) {
            c = 1;
        }
        if (c == 0) {
            String argument = getNextArgRequired();
            if (!TextUtils.isEmpty(argument)) {
                byte[] key = argument.getBytes(StandardCharsets.UTF_8);
                ArrayList<Byte> crypto = new ArrayList<>();
                crypto.add((byte) 9);
                crypto.add((byte) 0);
                crypto.add((byte) 0);
                crypto.add((byte) 0);
                for (byte ele : key) {
                    crypto.add(Byte.valueOf(ele));
                }
                if (!(OppoEngineerUtils.getCryptoSerice() == null || (outBuf = OppoEngineerUtils.cryptoengInvokeCommand(crypto)) == null || outBuf.size() <= 4)) {
                    if (outBuf.get(0).byteValue() == 0 && outBuf.get(1).byteValue() == 0 && outBuf.get(2).byteValue() == 0 && outBuf.get(3).byteValue() == 0) {
                        printResult(cmd, printWriter, "Pass\n");
                        return;
                    }
                    for (int i = 0; i < 4; i++) {
                        outBuf.remove(0);
                    }
                    String info = OppoEngineerUtils.transferByteListToString(outBuf);
                    if (info != null) {
                        printResult(cmd, printWriter, String.format(Locale.US, "Fail:%s\n", info));
                        return;
                    } else {
                        printResult(cmd, printWriter, "Fail\n");
                        return;
                    }
                }
            }
            printResult(cmd, printWriter, "Fail\n");
        } else if (c == 1) {
            ArrayList<Byte> crypto2 = new ArrayList<>();
            crypto2.add((byte) 10);
            crypto2.add((byte) 0);
            crypto2.add((byte) 0);
            crypto2.add((byte) 0);
            if (OppoEngineerUtils.getCryptoSerice() == null || (outBuf2 = OppoEngineerUtils.cryptoengInvokeCommand(crypto2)) == null || outBuf2.size() <= 4) {
                printResult(cmd, printWriter, "Fail\n");
            } else if (outBuf2.get(0).byteValue() == 0 && outBuf2.get(1).byteValue() == 0 && outBuf2.get(2).byteValue() == 0 && outBuf2.get(3).byteValue() == 0) {
                printResult(cmd, printWriter, "Pass\n");
            } else {
                for (int i2 = 0; i2 < 4; i2++) {
                    outBuf2.remove(0);
                }
                String info2 = OppoEngineerUtils.transferByteListToString(outBuf2);
                if (info2 != null) {
                    printResult(cmd, printWriter, String.format(Locale.US, "Fail:%s\n", info2));
                } else {
                    printResult(cmd, printWriter, "Fail\n");
                }
            }
        }
    }

    private int startActivityAsUserInternal(Context context, Intent intent, int userId) {
        try {
            return ActivityManagerNative.getDefault().startActivityAsUser(context.getIApplicationThread(), context.getBasePackageName(), intent, intent.resolveTypeIfNeeded(context.getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, (Bundle) null, userId);
        } catch (Exception e) {
            return -96;
        }
    }

    private String getIndicateInfo() {
        String pcba;
        String[] macAddresses;
        String macAddress = null;
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            SystemClock.sleep(500);
        }
        if (!(wifiManager == null || (macAddresses = wifiManager.getFactoryMacAddresses()) == null || macAddresses.length <= 0)) {
            macAddress = macAddresses[0];
        }
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = "null";
        }
        String btAddress = null;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            btAddress = bluetoothAdapter.getAddress();
        }
        if (TextUtils.isEmpty(btAddress)) {
            btAddress = "null";
        }
        if (OppoEngineerUtils.isMtkPlatform()) {
            pcba = SystemProperties.get("vendor.gsm.serial", "null");
        } else {
            pcba = SystemProperties.get("gsm.serial", "null");
        }
        if (TextUtils.isEmpty(pcba)) {
            pcba = "null";
        }
        String carrierName = OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000008));
        if (TextUtils.isEmpty(carrierName)) {
            carrierName = "null";
        }
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        String imei1 = null;
        String imei2 = null;
        if (telephonyManager != null) {
            imei1 = telephonyManager.getImei(0);
            if (telephonyManager.getPhoneCount() == 2) {
                imei2 = telephonyManager.getImei(1);
            } else {
                imei2 = imei1;
            }
        } else {
            Slog.e(TAG, "TelephonyManager is null");
        }
        if (TextUtils.isEmpty(imei1)) {
            imei1 = "null";
        }
        if (TextUtils.isEmpty(imei2)) {
            imei2 = "null";
        }
        String meid = "";
        if (telephonyManager != null) {
            meid = telephonyManager.getMeid();
        }
        if (TextUtils.isEmpty(meid)) {
            meid = "null";
        }
        String guid = IdProviderUtils.getOppoID(this.mContext);
        if (TextUtils.isEmpty(guid)) {
            guid = "null";
        }
        return "WIFI: " + macAddress + "\nWIFI2: null\nBT: " + btAddress + "\nPCBA: " + pcba + "\nCarrier: " + carrierName + "\nIMEI1: " + imei1 + "\nIMEI2: " + imei2 + "\nMEID: " + meid + "\nGUID: " + guid;
    }

    private Intent parseCommandArgs(ShellCommand cmd) {
        Intent intent = new Intent();
        while (true) {
            String opt = cmd.getNextOption();
            if (opt != null) {
                char c = 65535;
                boolean arg = false;
                switch (opt.hashCode()) {
                    case 1496:
                        if (opt.equals("-e")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 1507:
                        if (opt.equals("-p")) {
                            c = '\r';
                            break;
                        }
                        break;
                    case 1387073:
                        if (opt.equals("--ef")) {
                            c = 11;
                            break;
                        }
                        break;
                    case 1387076:
                        if (opt.equals("--ei")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1387079:
                        if (opt.equals("--el")) {
                            c = '\b';
                            break;
                        }
                        break;
                    case 1387086:
                        if (opt.equals("--es")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1387088:
                        if (opt.equals("--eu")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1387093:
                        if (opt.equals("--ez")) {
                            c = '\f';
                            break;
                        }
                        break;
                    case 42999280:
                        if (opt.equals("--ecn")) {
                            c = 5;
                            break;
                        }
                        break;
                    case 42999453:
                        if (opt.equals("--eia")) {
                            c = 6;
                            break;
                        }
                        break;
                    case 42999546:
                        if (opt.equals("--ela")) {
                            c = '\t';
                            break;
                        }
                        break;
                    case 42999776:
                        if (opt.equals("--esn")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1332983151:
                        if (opt.equals("--eial")) {
                            c = 7;
                            break;
                        }
                        break;
                    case 1332986034:
                        if (opt.equals("--elal")) {
                            c = '\n';
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                    case 1:
                        intent.putExtra(cmd.getNextArgRequired(), cmd.getNextArgRequired());
                        break;
                    case 2:
                        intent.putExtra(cmd.getNextArgRequired(), (String) null);
                        break;
                    case 3:
                        intent.putExtra(cmd.getNextArgRequired(), Integer.decode(cmd.getNextArgRequired()));
                        break;
                    case 4:
                        intent.putExtra(cmd.getNextArgRequired(), Uri.parse(cmd.getNextArgRequired()));
                        break;
                    case 5:
                        String key = cmd.getNextArgRequired();
                        String value = cmd.getNextArgRequired();
                        ComponentName cn = ComponentName.unflattenFromString(value);
                        if (cn != null) {
                            intent.putExtra(key, cn);
                            break;
                        } else {
                            throw new IllegalArgumentException("Bad component name: " + value);
                        }
                    case 6:
                        String key2 = cmd.getNextArgRequired();
                        String[] strings = cmd.getNextArgRequired().split(",");
                        int[] list = new int[strings.length];
                        for (int i = 0; i < strings.length; i++) {
                            list[i] = Integer.decode(strings[i]).intValue();
                        }
                        intent.putExtra(key2, list);
                        break;
                    case 7:
                        String key3 = cmd.getNextArgRequired();
                        String[] strings2 = cmd.getNextArgRequired().split(",");
                        ArrayList<Integer> list2 = new ArrayList<>(strings2.length);
                        for (String string : strings2) {
                            list2.add(Integer.decode(string));
                        }
                        intent.putExtra(key3, list2);
                        break;
                    case '\b':
                        intent.putExtra(cmd.getNextArgRequired(), Long.valueOf(cmd.getNextArgRequired()));
                        break;
                    case '\t':
                        String key4 = cmd.getNextArgRequired();
                        String[] strings3 = cmd.getNextArgRequired().split(",");
                        long[] list3 = new long[strings3.length];
                        for (int i2 = 0; i2 < strings3.length; i2++) {
                            list3[i2] = Long.valueOf(strings3[i2]).longValue();
                        }
                        intent.putExtra(key4, list3);
                        break;
                    case '\n':
                        String key5 = cmd.getNextArgRequired();
                        String[] strings4 = cmd.getNextArgRequired().split(",");
                        ArrayList<Long> list4 = new ArrayList<>(strings4.length);
                        for (String string2 : strings4) {
                            list4.add(Long.valueOf(string2));
                        }
                        intent.putExtra(key5, list4);
                        break;
                    case 11:
                        intent.putExtra(cmd.getNextArgRequired(), Float.valueOf(cmd.getNextArgRequired()));
                        break;
                    case '\f':
                        String key6 = cmd.getNextArgRequired();
                        String value2 = cmd.getNextArgRequired().toLowerCase();
                        if (TemperatureProvider.SWITCH_ON.equals(value2) || "t".equals(value2)) {
                            arg = true;
                        } else if (TemperatureProvider.SWITCH_OFF.equals(value2) || "f".equals(value2)) {
                            arg = false;
                        } else {
                            try {
                                if (Integer.decode(value2).intValue() != 0) {
                                    arg = true;
                                }
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid boolean value: " + value2);
                            }
                        }
                        intent.putExtra(key6, arg);
                        break;
                    case '\r':
                        intent.setPackage(cmd.getNextArgRequired());
                        break;
                }
            } else {
                return intent;
            }
        }
    }

    private void printResult(String cmd, PrintWriter printWriter, String result) {
        if (TextUtils.isEmpty(cmd) || printWriter == null || TextUtils.isEmpty(result)) {
            Slog.e(TAG, "printResult invalid para");
            return;
        }
        String timeStamp = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss.SSS", Locale.US).format(new Date());
        String buildDate = SystemProperties.get("ro.build.date.YmdHM", Build.DISPLAY);
        String content = String.format(Locale.US, "[%s][%s][run command : %s, result is { %s }]\n", buildDate, timeStamp, cmd, result);
        Slog.i("EMLog", content);
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        OppoEngineerNative.nativeWriteData("/mnt/vendor/opporeserve/media/engineermode/engineermode_log", 0, true, data.length, data);
        printWriter.println(result);
    }

    /* access modifiers changed from: private */
    public class WaitForResult implements Function<String, String> {
        private WaitForResult() {
        }

        /* JADX INFO: finally extract failed */
        public String apply(String serverName) {
            int count;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                OppoEngineerShell.this.mLocalSocket = null;
                OppoEngineerShell.this.mLocalServerSocket = new LocalServerSocket(serverName);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    /* class com.android.server.engineer.OppoEngineerShell.WaitForResult.AnonymousClass1 */

                    public void run() {
                        if (OppoEngineerShell.this.mLocalServerSocket != null) {
                            if (OppoEngineerShell.this.mLocalSocket == null) {
                                LocalSocket localSocket = new LocalSocket();
                                try {
                                    localSocket.connect(OppoEngineerShell.this.mLocalServerSocket.getLocalSocketAddress());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    localSocket.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                            try {
                                Slog.d(OppoEngineerShell.TAG, "time's up, auto close server socket");
                                OppoEngineerShell.this.mLocalServerSocket.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                                OppoEngineerShell.this.mLocalServerSocket = null;
                            }
                        }
                    }
                }, 10000);
                OppoEngineerShell.this.mLocalSocket = OppoEngineerShell.this.mLocalServerSocket.accept();
                Slog.i(OppoEngineerShell.TAG, "already connect");
                timer.cancel();
                OppoEngineerShell.this.mLocalSocket.setReceiveBufferSize(128);
                OppoEngineerShell.this.mLocalSocket.setSoTimeout(3000);
                InputStream inputStream = OppoEngineerShell.this.mLocalSocket.getInputStream();
                OutputStream outputStream = OppoEngineerShell.this.mLocalSocket.getOutputStream();
                byte[] buffer = new byte[128];
                if (inputStream != null && (count = inputStream.read(buffer, 0, 128)) > 0) {
                    byte[] resp = new byte[count];
                    System.arraycopy(buffer, 0, resp, 0, count);
                    String command = new String(resp);
                    String str = OppoEngineerShell.TAG;
                    Slog.v(str, "Get command [" + command + "], size=" + count);
                    if (outputStream != null) {
                        outputStream.write(command.getBytes());
                        outputStream.flush();
                        if (!TextUtils.isEmpty(command)) {
                            stringBuilder.append(command);
                        }
                    }
                }
                if (OppoEngineerShell.this.mLocalSocket != null) {
                    try {
                        OppoEngineerShell.this.mLocalSocket.shutdownInput();
                        OppoEngineerShell.this.mLocalSocket.shutdownOutput();
                        try {
                            OppoEngineerShell.this.mLocalSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        OppoEngineerShell.this.mLocalSocket.close();
                    } catch (Throwable th) {
                        try {
                            OppoEngineerShell.this.mLocalSocket.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                        throw th;
                    }
                }
                if (OppoEngineerShell.this.mLocalServerSocket != null) {
                    try {
                        OppoEngineerShell.this.mLocalServerSocket.close();
                    } catch (Exception e4) {
                        e4.printStackTrace();
                    }
                }
            } catch (Exception e5) {
                String str2 = OppoEngineerShell.TAG;
                Slog.i(str2, "exception caught " + e5.getMessage());
                if (OppoEngineerShell.this.mLocalSocket != null) {
                    try {
                        OppoEngineerShell.this.mLocalSocket.shutdownInput();
                        OppoEngineerShell.this.mLocalSocket.shutdownOutput();
                        try {
                            OppoEngineerShell.this.mLocalSocket.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                    } catch (IOException e7) {
                        e7.printStackTrace();
                        OppoEngineerShell.this.mLocalSocket.close();
                    } catch (Throwable th2) {
                        try {
                            OppoEngineerShell.this.mLocalSocket.close();
                        } catch (IOException e8) {
                            e8.printStackTrace();
                        }
                        throw th2;
                    }
                }
                if (OppoEngineerShell.this.mLocalServerSocket != null) {
                    OppoEngineerShell.this.mLocalServerSocket.close();
                }
            } catch (Throwable th3) {
                if (OppoEngineerShell.this.mLocalSocket != null) {
                    try {
                        OppoEngineerShell.this.mLocalSocket.shutdownInput();
                        OppoEngineerShell.this.mLocalSocket.shutdownOutput();
                        try {
                            OppoEngineerShell.this.mLocalSocket.close();
                        } catch (IOException e9) {
                            e9.printStackTrace();
                        }
                    } catch (IOException e10) {
                        e10.printStackTrace();
                        OppoEngineerShell.this.mLocalSocket.close();
                    } catch (Throwable th4) {
                        try {
                            OppoEngineerShell.this.mLocalSocket.close();
                        } catch (IOException e11) {
                            e11.printStackTrace();
                        }
                        throw th4;
                    }
                }
                if (OppoEngineerShell.this.mLocalServerSocket != null) {
                    try {
                        OppoEngineerShell.this.mLocalServerSocket.close();
                    } catch (Exception e12) {
                        e12.printStackTrace();
                    }
                }
                throw th3;
            }
            return stringBuilder.toString();
        }
    }
}
