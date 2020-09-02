package com.android.server.wifi.p2p;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import com.android.internal.util.StateMachine;

public class OppoWifiP2pNotifyService {
    private static final int BASE = 143360;
    private static final int DROP_WIFI_USER_ACCEPT = 143364;
    private static final int DROP_WIFI_USER_REJECT = 143365;
    private static final int PEER_CONNECTION_USER_ACCEPT = 143362;
    private static final int PEER_CONNECTION_USER_REJECT = 143363;
    private static final String TAG = OppoWifiP2pNotifyService.class.getSimpleName();
    /* access modifiers changed from: private */
    public StateMachine mP2pStateMachine;
    /* access modifiers changed from: private */
    public boolean mVerboseLoggingEnabled = false;

    OppoWifiP2pNotifyService(StateMachine p2pStateMachine) {
        this.mP2pStateMachine = p2pStateMachine;
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
    }

    public void notifyP2pEnableFailure(Context context) {
        Resources r = Resources.getSystem();
        AlertDialog dialog = new AlertDialog.Builder(context, 201523207).setTitle(r.getString(201653508)).setPositiveButton(r.getString(201653509), (DialogInterface.OnClickListener) null).create();
        dialog.setCancelable(false);
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.ignoreHomeMenuKey = 1;
        dialog.getWindow().setAttributes(p);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setType(2003);
        WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
        attrs.privateFlags = 16;
        dialog.getWindow().setAttributes(attrs);
        dialog.show();
        TextView msg = (TextView) dialog.findViewById(16908299);
        if (msg != null) {
            msg.setGravity(17);
        } else {
            loge("textview is null");
        }
    }

    public AlertDialog notifyFrequencyConflict(Context context, String peerDeviceName) {
        logd("Notify frequency conflict");
        Resources r = Resources.getSystem();
        AlertDialog dialog = new AlertDialog.Builder(context, 201523207).setTitle(r.getString(201653506, peerDeviceName)).setMessage(r.getString(17041292)).setPositiveButton(r.getString(201653507), new DialogInterface.OnClickListener() {
            /* class com.android.server.wifi.p2p.OppoWifiP2pNotifyService.AnonymousClass3 */

            public void onClick(DialogInterface dialog, int which) {
                OppoWifiP2pNotifyService.this.mP2pStateMachine.sendMessage((int) OppoWifiP2pNotifyService.DROP_WIFI_USER_ACCEPT);
            }
        }).setNegativeButton(r.getString(17039360), new DialogInterface.OnClickListener() {
            /* class com.android.server.wifi.p2p.OppoWifiP2pNotifyService.AnonymousClass2 */

            public void onClick(DialogInterface dialog, int which) {
                OppoWifiP2pNotifyService.this.mP2pStateMachine.sendMessage((int) OppoWifiP2pNotifyService.DROP_WIFI_USER_REJECT);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            /* class com.android.server.wifi.p2p.OppoWifiP2pNotifyService.AnonymousClass1 */

            public void onCancel(DialogInterface arg0) {
                OppoWifiP2pNotifyService.this.mP2pStateMachine.sendMessage((int) OppoWifiP2pNotifyService.DROP_WIFI_USER_REJECT);
            }
        }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.ignoreHomeMenuKey = 1;
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().setType(2003);
        WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
        attrs.privateFlags = 16;
        dialog.getWindow().setAttributes(attrs);
        dialog.show();
        TextView msg = (TextView) dialog.findViewById(16908299);
        if (msg != null) {
            msg.setGravity(17);
        } else {
            loge("textview is null");
        }
        return dialog;
    }

    public void notifyInvitationReceived(Context context, final WifiP2pConfig savedPeerConfig, String peerDeviceName, final String stateName, boolean isExternalShareConnect) {
        Resources r = Resources.getSystem();
        final WpsInfo wps = savedPeerConfig.wps;
        View textEntryView = LayoutInflater.from(context).inflate(17367339, (ViewGroup) null);
        ViewGroup group = (ViewGroup) textEntryView.findViewById(16909018);
        addRowToDialog(group, 17041293, peerDeviceName, context);
        final EditText pin = (EditText) textEntryView.findViewById(16909560);
        if (!isExternalShareConnect || wps.setup != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (wps.setup == 2) {
                builder.setTitle(r.getString(17041295));
                builder.setView(textEntryView);
            } else {
                builder.setTitle(r.getString(201653619, peerDeviceName));
            }
            AlertDialog dialog = builder.setPositiveButton(r.getString(17039430), new DialogInterface.OnClickListener() {
                /* class com.android.server.wifi.p2p.OppoWifiP2pNotifyService.AnonymousClass6 */

                public void onClick(DialogInterface dialog, int which) {
                    if (wps.setup == 2) {
                        savedPeerConfig.wps.pin = pin.getText().toString();
                    }
                    if (OppoWifiP2pNotifyService.this.mVerboseLoggingEnabled) {
                        OppoWifiP2pNotifyService oppoWifiP2pNotifyService = OppoWifiP2pNotifyService.this;
                        oppoWifiP2pNotifyService.logd(stateName + " accept invitation " + savedPeerConfig);
                    }
                    OppoWifiP2pNotifyService.this.mP2pStateMachine.sendMessage((int) OppoWifiP2pNotifyService.PEER_CONNECTION_USER_ACCEPT);
                }
            }).setNegativeButton(r.getString(17039360), new DialogInterface.OnClickListener() {
                /* class com.android.server.wifi.p2p.OppoWifiP2pNotifyService.AnonymousClass5 */

                public void onClick(DialogInterface dialog, int which) {
                    if (OppoWifiP2pNotifyService.this.mVerboseLoggingEnabled) {
                        OppoWifiP2pNotifyService oppoWifiP2pNotifyService = OppoWifiP2pNotifyService.this;
                        oppoWifiP2pNotifyService.logd(stateName + " ignore connect");
                    }
                    OppoWifiP2pNotifyService.this.mP2pStateMachine.sendMessage((int) OppoWifiP2pNotifyService.PEER_CONNECTION_USER_REJECT);
                }
            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                /* class com.android.server.wifi.p2p.OppoWifiP2pNotifyService.AnonymousClass4 */

                public void onCancel(DialogInterface arg0) {
                    if (OppoWifiP2pNotifyService.this.mVerboseLoggingEnabled) {
                        OppoWifiP2pNotifyService oppoWifiP2pNotifyService = OppoWifiP2pNotifyService.this;
                        oppoWifiP2pNotifyService.logd(stateName + " ignore connect");
                    }
                    OppoWifiP2pNotifyService.this.mP2pStateMachine.sendMessage((int) OppoWifiP2pNotifyService.PEER_CONNECTION_USER_REJECT);
                }
            }).create();
            dialog.setCanceledOnTouchOutside(false);
            int i = wps.setup;
            if (i == 1) {
                if (this.mVerboseLoggingEnabled) {
                    logd("Shown pin section visible");
                }
                addRowToDialog(group, 17041296, wps.pin, context);
            } else if (i == 2) {
                if (this.mVerboseLoggingEnabled) {
                    logd("Enter pin section visible");
                }
                textEntryView.findViewById(16908894).setVisibility(0);
            }
            if ((r.getConfiguration().uiMode & 5) == 5) {
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    /* class com.android.server.wifi.p2p.OppoWifiP2pNotifyService.AnonymousClass7 */

                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode != 164) {
                            return false;
                        }
                        OppoWifiP2pNotifyService.this.mP2pStateMachine.sendMessage((int) OppoWifiP2pNotifyService.PEER_CONNECTION_USER_ACCEPT);
                        dialog.dismiss();
                        return true;
                    }
                });
            }
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
            p.ignoreHomeMenuKey = 1;
            dialog.getWindow().setAttributes(p);
            dialog.getWindow().setType(2003);
            WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
            return;
        }
        logd("is external share connect accept");
        this.mP2pStateMachine.sendMessage((int) PEER_CONNECTION_USER_ACCEPT);
    }

    private void loge(String s) {
        Slog.e(TAG, s);
    }

    /* access modifiers changed from: private */
    public void logd(String s) {
        Slog.d(TAG, s);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
     arg types: [int, android.view.ViewGroup, int]
     candidates:
      ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
      ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
    private void addRowToDialog(ViewGroup group, int stringId, String value, Context context) {
        Resources r = Resources.getSystem();
        View row = LayoutInflater.from(context).inflate(17367340, group, false);
        ((TextView) row.findViewById(16909136)).setText(r.getString(stringId));
        ((TextView) row.findViewById(16909539)).setText(value);
        group.addView(row);
    }
}
