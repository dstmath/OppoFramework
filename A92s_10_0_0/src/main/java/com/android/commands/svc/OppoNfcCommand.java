package com.android.commands.svc;

import android.content.pm.IPackageManager;
import android.nfc.INfcAdapter;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.commands.svc.Svc;
import java.io.PrintStream;

public class OppoNfcCommand extends Svc.Command {
    public OppoNfcCommand() {
        super("opponfc");
    }

    @Override // com.android.commands.svc.Svc.Command
    public String shortHelp() {
        return "Control NFC functions";
    }

    @Override // com.android.commands.svc.Svc.Command
    public String longHelp() {
        return shortHelp() + "\n\nusage: svc nfc [enable|disable]\n         Turn NFC on or off.\n\n";
    }

    @Override // com.android.commands.svc.Svc.Command
    public void run(String[] args) {
        boolean validCommand = false;
        if (args.length >= 2) {
            int flag = -1;
            if ("enable".equals(args[1])) {
                flag = 0;
                validCommand = true;
            } else if ("disable".equals(args[1])) {
                flag = 1;
                validCommand = true;
            } else if ("get-cplc".equals(args[1])) {
                flag = 2;
                validCommand = true;
            } else if ("get-die-id".equals(args[1])) {
                flag = 3;
                validCommand = true;
            }
            if (!validCommand) {
                System.err.println(longHelp());
                return;
            }
            try {
                if (!IPackageManager.Stub.asInterface(ServiceManager.getService("package")).hasSystemFeature("android.hardware.nfc", 0)) {
                    System.err.println("NFC feature not supported.");
                    return;
                }
                INfcAdapter nfc = INfcAdapter.Stub.asInterface(ServiceManager.getService("nfc"));
                if (flag != 0 && flag != 1 && flag == 2) {
                    try {
                        String cplc = nfc.getCplc();
                        PrintStream printStream = System.err;
                        printStream.println("cplc:" + cplc);
                    } catch (RemoteException e) {
                        PrintStream printStream2 = System.err;
                        printStream2.println("NFC operation failed: " + e);
                    }
                }
            } catch (RemoteException e2) {
                System.err.println("RemoteException while calling PackageManager, is the system running?");
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
