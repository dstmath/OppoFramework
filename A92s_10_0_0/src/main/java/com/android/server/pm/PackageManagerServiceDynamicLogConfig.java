package com.android.server.pm;

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
        boolean on = "1".equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag:" + tag + ", on:" + on);
        if ("install".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_INSTALL != null) {
                OppoMirrorPackageManagerService.DEBUG_INSTALL.set(service, on);
            }
        } else if ("remove".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_REMOVE != null) {
                OppoMirrorPackageManagerService.DEBUG_REMOVE.set(service, on);
            }
        } else if ("settings".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_SETTINGS != null) {
                OppoMirrorPackageManagerService.DEBUG_SETTINGS.set(service, on);
            }
        } else if ("scan".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_PACKAGE_SCANNING != null) {
                OppoMirrorPackageManagerService.DEBUG_PACKAGE_SCANNING.set(service, on);
            }
        } else if ("verify".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_VERIFY != null) {
                OppoMirrorPackageManagerService.DEBUG_VERIFY.set(service, on);
            }
        } else if ("abi".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_ABI_SELECTION != null) {
                OppoMirrorPackageManagerService.DEBUG_ABI_SELECTION.set(service, on);
            }
        } else if ("opt".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_DEXOPT != null) {
                OppoMirrorPackageManagerService.DEBUG_DEXOPT.set(service, on);
            }
        } else if ("match".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_INTENT_MATCHING != null) {
                OppoMirrorPackageManagerService.DEBUG_INTENT_MATCHING.set(service, on);
            }
        } else if ("info".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_PACKAGE_INFO != null) {
                OppoMirrorPackageManagerService.DEBUG_PACKAGE_INFO.set(service, on);
            }
        } else if ("preferred".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_PREFERRED != null) {
                OppoMirrorPackageManagerService.DEBUG_PREFERRED.set(service, on);
            }
        } else if ("upgrade".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_UPGRADE != null) {
                OppoMirrorPackageManagerService.DEBUG_UPGRADE.set(service, on);
            }
        } else if ("broadcast".equals(tag)) {
            if (OppoMirrorPackageManagerService.DEBUG_BROADCASTS != null) {
                OppoMirrorPackageManagerService.DEBUG_BROADCASTS.set(service, on);
            }
        } else if (!"showinfo".equals(tag)) {
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
            StringBuilder stringBuilder = new StringBuilder(" install=" + PackageManagerService.DEBUG_INSTALL + " remove=" + PackageManagerService.DEBUG_REMOVE + " settings=" + PackageManagerService.DEBUG_SETTINGS + " scan=" + PackageManagerService.DEBUG_PACKAGE_SCANNING);
            if (OppoMirrorPackageManagerService.DEBUG_VERIFY != null) {
                stringBuilder.append(" verify=" + OppoMirrorPackageManagerService.DEBUG_VERIFY.get(service));
            }
            if (OppoMirrorPackageManagerService.DEBUG_ABI_SELECTION != null) {
                stringBuilder.append(" abi=" + OppoMirrorPackageManagerService.DEBUG_ABI_SELECTION.get(service));
            }
            stringBuilder.append(" opt=" + PackageManagerService.DEBUG_DEXOPT);
            if (OppoMirrorPackageManagerService.DEBUG_INTENT_MATCHING != null) {
                stringBuilder.append(" match=" + OppoMirrorPackageManagerService.DEBUG_INTENT_MATCHING.get(service));
            }
            if (OppoMirrorPackageManagerService.DEBUG_PACKAGE_INFO != null) {
                stringBuilder.append(" info=" + OppoMirrorPackageManagerService.DEBUG_PACKAGE_INFO.get(service));
            }
            stringBuilder.append(" preferred=" + PackageManagerService.DEBUG_PREFERRED);
            stringBuilder.append(" upgrade=" + PackageManagerService.DEBUG_UPGRADE);
            if (OppoMirrorPackageManagerService.DEBUG_BROADCASTS != null) {
                stringBuilder.append(" broadcast=" + OppoMirrorPackageManagerService.DEBUG_BROADCASTS.get(service));
            }
            pw.println(stringBuilder);
        }
    }
}
