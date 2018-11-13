package com.android.server.pm;

import com.android.server.LocationManagerService;
import java.io.PrintWriter;

public class PackageManagerServiceDynamicLogConfig {
    static void dynamicallyConfigLogTag(PackageManagerService service, PrintWriter pw, String[] args, int opti) {
        pw.println("dynamicallyConfigLogTag, opti:" + opti + ", args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]:" + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        String tag = args[1];
        boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag:" + tag + ", on:" + on);
        if ("install".equals(tag)) {
            PackageManagerService.DEBUG_INSTALL = on;
        } else if ("remove".equals(tag)) {
            PackageManagerService.DEBUG_REMOVE = on;
        } else if ("settings".equals(tag)) {
            PackageManagerService.DEBUG_SETTINGS = on;
        } else if ("scan".equals(tag)) {
            PackageManagerService.DEBUG_PACKAGE_SCANNING = on;
        } else if ("verify".equals(tag)) {
            PackageManagerService.DEBUG_VERIFY = on;
        } else if ("abi".equals(tag)) {
            PackageManagerService.DEBUG_ABI_SELECTION = on;
        } else if ("opt".equals(tag)) {
            PackageManagerService.DEBUG_DEXOPT = on;
        } else if ("match".equals(tag)) {
            PackageManagerService.DEBUG_INTENT_MATCHING = on;
        } else if ("info".equals(tag)) {
            PackageManagerService.DEBUG_PACKAGE_INFO = on;
        } else if ("preferred".equals(tag)) {
            PackageManagerService.DEBUG_PREFERRED = on;
        } else if ("upgrade".equals(tag)) {
            PackageManagerService.DEBUG_UPGRADE = on;
        } else if ("broadcast".equals(tag)) {
            PackageManagerService.DEBUG_BROADCASTS = on;
        } else if ("showinfo".equals(tag)) {
            PackageManagerService.DEBUG_SHOW_INFO = on;
        } else {
            pw.println("Failed! Invalid argument! Type cmd for help: dumpsys package log");
        }
    }

    static void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 package install:  DEBUG_INSTALL ");
        pw.println("cmd: dumpsys package log install 0/1");
        pw.println("----------------------------------");
        pw.println("2 package remove:   DEBUG_REMOVE ");
        pw.println("cmd: dumpsys package log remove 0/1");
        pw.println("----------------------------------");
        pw.println("3 package settings: DEBUG_SETTINGS ");
        pw.println("cmd: dumpsys package log settings 0/1");
        pw.println("----------------------------------");
        pw.println("4 package scan:     DEBUG_PACKAGE_SCANNING ");
        pw.println("cmd: dumpsys package log scan 0/1");
        pw.println("----------------------------------");
        pw.println("5 package verify:   DEBUG_VERIFY ");
        pw.println("cmd: dumpsys package log verify 0/1");
        pw.println("----------------------------------");
        pw.println("6 package abi:      DEBUG_ABI_SELECTION ");
        pw.println("cmd: dumpsys package log abi 0/1");
        pw.println("----------------------------------");
        pw.println("7 package opt:      DEBUG_DEXOPT ");
        pw.println("cmd: dumpsys package log opt 0/1");
        pw.println("----------------------------------");
        pw.println("8 package match:    DEBUG_INTENT_MATCHING ");
        pw.println("cmd: dumpsys package log match 0/1");
        pw.println("----------------------------------");
        pw.println("9 package info:    DEBUG_PACKAGE_INFO ");
        pw.println("cmd: dumpsys package log info 0/1");
        pw.println("----------------------------------");
        pw.println("10 package preferred:      DEBUG_PREFERRED ");
        pw.println("cmd: dumpsys package log preferred 0/1");
        pw.println("----------------------------------");
        pw.println("11 package upgrade:      DEBUG_UPGRADE ");
        pw.println("cmd: dumpsys package log upgrade 0/1");
        pw.println("----------------------------------");
        pw.println("12 package broadcast:    DEBUG_BROADCASTS ");
        pw.println("cmd: dumpsys package log broadcast 0/1");
        pw.println("----------------------------------");
        pw.println("13 package showinfo:    DEBUG_SHOW_INFO ");
        pw.println("cmd: dumpsys package log showinfo 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    static void dumpDynamicallyLogSwitch(PackageManagerService service, PrintWriter pw, String[] args, int opti) {
        if (args.length == 1) {
            pw.println(" install=" + PackageManagerService.DEBUG_INSTALL + " remove=" + PackageManagerService.DEBUG_REMOVE + " settings=" + PackageManagerService.DEBUG_SETTINGS + " scan=" + PackageManagerService.DEBUG_PACKAGE_SCANNING + " verify=" + PackageManagerService.DEBUG_VERIFY + " abi=" + PackageManagerService.DEBUG_ABI_SELECTION + " opt=" + PackageManagerService.DEBUG_DEXOPT + " match=" + PackageManagerService.DEBUG_INTENT_MATCHING + " info=" + PackageManagerService.DEBUG_PACKAGE_INFO + " preferred=" + PackageManagerService.DEBUG_PREFERRED + " upgrade=" + PackageManagerService.DEBUG_UPGRADE + " broadcast=" + PackageManagerService.DEBUG_BROADCASTS + " showinfo=" + PackageManagerService.DEBUG_SHOW_INFO);
        }
    }
}
