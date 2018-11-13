package com.android.commands.settings;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IActivityManager.ContentProviderHolder;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SettingsCmd {
    /* renamed from: -com-android-commands-settings-SettingsCmd$CommandVerbSwitchesValues */
    private static final /* synthetic */ int[] f0xc47d3e85 = null;
    static String[] mArgs;
    String mKey = null;
    int mNextArg;
    String mTable = null;
    int mUser = -1;
    String mValue = null;
    CommandVerb mVerb = CommandVerb.UNSPECIFIED;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    enum CommandVerb {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.commands.settings.SettingsCmd.CommandVerb.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.commands.settings.SettingsCmd.CommandVerb.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.commands.settings.SettingsCmd.CommandVerb.<clinit>():void");
        }
    }

    /* renamed from: -getcom-android-commands-settings-SettingsCmd$CommandVerbSwitchesValues */
    private static /* synthetic */ int[] m0x2d213d29() {
        if (f0xc47d3e85 != null) {
            return f0xc47d3e85;
        }
        int[] iArr = new int[CommandVerb.values().length];
        try {
            iArr[CommandVerb.DELETE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandVerb.GET.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandVerb.LIST.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandVerb.PUT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommandVerb.UNSPECIFIED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        f0xc47d3e85 = iArr;
        return iArr;
    }

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            printUsage();
            return;
        }
        mArgs = args;
        try {
            new SettingsCmd().run();
        } catch (Exception e) {
            System.err.println("Unable to run settings command");
        }
    }

    /* JADX WARNING: Missing block: B:71:0x0143, code:
            if (r14.mNextArg < mArgs.length) goto L_0x0148;
     */
    /* JADX WARNING: Missing block: B:72:0x0145, code:
            r13 = true;
     */
    /* JADX WARNING: Missing block: B:73:0x0148, code:
            java.lang.System.err.println("Too many arguments");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        boolean valid = false;
        while (true) {
            try {
                String arg = nextArg();
                if (arg == null) {
                    break;
                } else if ("--user".equals(arg)) {
                    if (this.mUser != -1) {
                        break;
                    }
                    arg = nextArg();
                    if ("current".equals(arg) || "cur".equals(arg)) {
                        this.mUser = -2;
                    } else {
                        this.mUser = Integer.parseInt(arg);
                    }
                } else if (this.mVerb == CommandVerb.UNSPECIFIED) {
                    if (!"get".equalsIgnoreCase(arg)) {
                        if (!"put".equalsIgnoreCase(arg)) {
                            if (!"delete".equalsIgnoreCase(arg)) {
                                if (!"list".equalsIgnoreCase(arg)) {
                                    System.err.println("Invalid command: " + arg);
                                    break;
                                }
                                this.mVerb = CommandVerb.LIST;
                            } else {
                                this.mVerb = CommandVerb.DELETE;
                            }
                        } else {
                            this.mVerb = CommandVerb.PUT;
                        }
                    } else {
                        this.mVerb = CommandVerb.GET;
                    }
                } else if (this.mTable == null) {
                    if (!"system".equalsIgnoreCase(arg) && !"secure".equalsIgnoreCase(arg) && !"global".equalsIgnoreCase(arg)) {
                        System.err.println("Invalid namespace '" + arg + "'");
                        break;
                    }
                    this.mTable = arg.toLowerCase();
                    if (this.mVerb == CommandVerb.LIST) {
                        valid = true;
                        break;
                    }
                } else if (this.mVerb == CommandVerb.GET || this.mVerb == CommandVerb.DELETE) {
                    this.mKey = arg;
                } else if (this.mKey == null) {
                    this.mKey = arg;
                } else {
                    this.mValue = arg;
                    if (this.mNextArg >= mArgs.length) {
                        valid = true;
                    } else {
                        System.err.println("Too many arguments");
                    }
                }
            } catch (Exception e) {
                valid = false;
            }
        }
        if (valid) {
            IActivityManager activityManager;
            IContentProvider provider;
            IBinder token;
            try {
                activityManager = ActivityManagerNative.getDefault();
                if (this.mUser == -2) {
                    this.mUser = activityManager.getCurrentUser().id;
                }
                if (this.mUser < 0) {
                    this.mUser = 0;
                }
                provider = null;
                token = new Binder();
                ContentProviderHolder holder = activityManager.getContentProviderExternal("settings", 0, token);
                if (holder == null) {
                    throw new IllegalStateException("Could not find settings provider");
                }
                provider = holder.provider;
                switch (m0x2d213d29()[this.mVerb.ordinal()]) {
                    case 1:
                        System.out.println("Deleted " + deleteForUser(provider, this.mUser, this.mTable, this.mKey) + " rows");
                        break;
                    case 2:
                        System.out.println(getForUser(provider, this.mUser, this.mTable, this.mKey));
                        break;
                    case 3:
                        for (String line : listForUser(provider, this.mUser, this.mTable)) {
                            System.out.println(line);
                        }
                        break;
                    case 4:
                        putForUser(provider, this.mUser, this.mTable, this.mKey, this.mValue);
                        break;
                    default:
                        System.err.println("Unspecified command");
                        break;
                }
                if (provider != null) {
                    activityManager.removeContentProviderExternal("settings", token);
                    return;
                }
                return;
            } catch (Exception e2) {
                System.err.println("Error while accessing settings provider");
                e2.printStackTrace();
                return;
            } catch (Throwable th) {
                if (provider != null) {
                    activityManager.removeContentProviderExternal("settings", token);
                }
            }
        }
        printUsage();
    }

    private List<String> listForUser(IContentProvider provider, int userHandle, String table) {
        Uri uri;
        if ("system".equals(table)) {
            uri = System.CONTENT_URI;
        } else if ("secure".equals(table)) {
            uri = Secure.CONTENT_URI;
        } else if ("global".equals(table)) {
            uri = Global.CONTENT_URI;
        } else {
            uri = null;
        }
        ArrayList<String> lines = new ArrayList();
        if (uri == null) {
            return lines;
        }
        Cursor cursor;
        try {
            cursor = provider.query(resolveCallingPackage(), uri, null, null, null, null, null);
            while (cursor != null) {
                if (!cursor.moveToNext()) {
                    break;
                }
                lines.add(cursor.getString(1) + "=" + cursor.getString(2));
            }
            if (cursor != null) {
                cursor.close();
            }
            Collections.sort(lines);
        } catch (RemoteException e) {
            System.err.println("List failed in " + table + " for user " + userHandle);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return lines;
    }

    private String nextArg() {
        if (this.mNextArg >= mArgs.length) {
            return null;
        }
        String arg = mArgs[this.mNextArg];
        this.mNextArg++;
        return arg;
    }

    String getForUser(IContentProvider provider, int userHandle, String table, String key) {
        String callGetCommand;
        if ("system".equals(table)) {
            callGetCommand = "GET_system";
        } else if ("secure".equals(table)) {
            callGetCommand = "GET_secure";
        } else if ("global".equals(table)) {
            callGetCommand = "GET_global";
        } else {
            System.err.println("Invalid table; no put performed");
            throw new IllegalArgumentException("Invalid table " + table);
        }
        try {
            Bundle arg = new Bundle();
            arg.putInt("_user", userHandle);
            Bundle b = provider.call(resolveCallingPackage(), callGetCommand, key, arg);
            if (b != null) {
                return b.getPairValue();
            }
            return null;
        } catch (RemoteException e) {
            System.err.println("Can't read key " + key + " in " + table + " for user " + userHandle);
            return null;
        }
    }

    void putForUser(IContentProvider provider, int userHandle, String table, String key, String value) {
        String callPutCommand;
        if ("system".equals(table)) {
            callPutCommand = "PUT_system";
        } else if ("secure".equals(table)) {
            callPutCommand = "PUT_secure";
        } else if ("global".equals(table)) {
            callPutCommand = "PUT_global";
        } else {
            System.err.println("Invalid table; no put performed");
            return;
        }
        try {
            Bundle arg = new Bundle();
            arg.putString("value", value);
            arg.putInt("_user", userHandle);
            provider.call(resolveCallingPackage(), callPutCommand, key, arg);
        } catch (RemoteException e) {
            System.err.println("Can't set key " + key + " in " + table + " for user " + userHandle);
        }
    }

    int deleteForUser(IContentProvider provider, int userHandle, String table, String key) {
        Uri targetUri;
        if ("system".equals(table)) {
            targetUri = System.getUriFor(key);
        } else if ("secure".equals(table)) {
            targetUri = Secure.getUriFor(key);
        } else if ("global".equals(table)) {
            targetUri = Global.getUriFor(key);
        } else {
            System.err.println("Invalid table; no delete performed");
            throw new IllegalArgumentException("Invalid table " + table);
        }
        int num = 0;
        try {
            return provider.delete(resolveCallingPackage(), targetUri, null, null);
        } catch (RemoteException e) {
            System.err.println("Can't clear key " + key + " in " + table + " for user " + userHandle);
            return num;
        }
    }

    private static void printUsage() {
        System.err.println("usage:  settings [--user <USER_ID> | current] get namespace key");
        System.err.println("        settings [--user <USER_ID> | current] put namespace key value");
        System.err.println("        settings [--user <USER_ID> | current] delete namespace key");
        System.err.println("        settings [--user <USER_ID> | current] list namespace");
        System.err.println("\n'namespace' is one of {system, secure, global}, case-insensitive");
        System.err.println("If '--user <USER_ID> | current' is not given, the operations are performed on the system user.");
    }

    public static String resolveCallingPackage() {
        switch (Process.myUid()) {
            case 0:
                return "root";
            case 2000:
                return "com.android.shell";
            default:
                return null;
        }
    }
}
