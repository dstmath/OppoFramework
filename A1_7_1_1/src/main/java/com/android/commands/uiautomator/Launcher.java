package com.android.commands.uiautomator;

import android.os.Process;
import java.io.PrintStream;
import java.util.Arrays;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class Launcher {
    private static Command[] COMMANDS;
    private static Command HELP_COMMAND;

    public static abstract class Command {
        private String mName;

        public abstract String detailedOptions();

        public abstract void run(String[] strArr);

        public abstract String shortHelp();

        public Command(String name) {
            this.mName = name;
        }

        public String name() {
            return this.mName;
        }
    }

    /* renamed from: com.android.commands.uiautomator.Launcher$1 */
    static class AnonymousClass1 extends Command {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void run(String[] args) {
            System.err.println("Usage: uiautomator <subcommand> [options]\n");
            System.err.println("Available subcommands:\n");
            for (Command command : Launcher.COMMANDS) {
                String shortHelp = command.shortHelp();
                String detailedOptions = command.detailedOptions();
                if (shortHelp == null) {
                    shortHelp = "";
                }
                if (detailedOptions == null) {
                    detailedOptions = "";
                }
                PrintStream printStream = System.err;
                Object[] objArr = new Object[2];
                objArr[0] = command.name();
                objArr[1] = shortHelp;
                printStream.println(String.format("%s: %s", objArr));
                System.err.println(detailedOptions);
            }
        }

        public String detailedOptions() {
            return null;
        }

        public String shortHelp() {
            return "displays help message";
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.commands.uiautomator.Launcher.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.commands.uiautomator.Launcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.uiautomator.Launcher.<clinit>():void");
    }

    public static void main(String[] args) {
        Process.setArgV0("uiautomator");
        if (args.length >= 1) {
            Command command = findCommand(args[0]);
            if (command != null) {
                String[] args2 = new String[0];
                if (args.length > 1) {
                    args2 = (String[]) Arrays.copyOfRange(args, 1, args.length);
                }
                command.run(args2);
                return;
            }
        }
        HELP_COMMAND.run(args);
    }

    private static Command findCommand(String name) {
        for (Command command : COMMANDS) {
            if (command.name().equals(name)) {
                return command;
            }
        }
        return null;
    }
}
