package com.android.internal.widget;

import android.os.AsyncTask;
import com.android.internal.widget.LockPatternUtils.CheckCredentialProgressCallback;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    */
public final class LockPatternChecker {

    /* renamed from: com.android.internal.widget.LockPatternChecker$1 */
    static class AnonymousClass1 extends AsyncTask<Void, Void, byte[]> {
        private int mThrottleTimeout;
        final /* synthetic */ OnVerifyCallback val$callback;
        final /* synthetic */ long val$challenge;
        final /* synthetic */ List val$pattern;
        final /* synthetic */ int val$userId;
        final /* synthetic */ LockPatternUtils val$utils;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.1.<init>(com.android.internal.widget.LockPatternUtils, java.util.List, long, int, com.android.internal.widget.LockPatternChecker$OnVerifyCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass1(com.android.internal.widget.LockPatternUtils r1, java.util.List r2, long r3, int r5, com.android.internal.widget.LockPatternChecker.OnVerifyCallback r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.1.<init>(com.android.internal.widget.LockPatternUtils, java.util.List, long, int, com.android.internal.widget.LockPatternChecker$OnVerifyCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.1.<init>(com.android.internal.widget.LockPatternUtils, java.util.List, long, int, com.android.internal.widget.LockPatternChecker$OnVerifyCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.1.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected /* bridge */ /* synthetic */ java.lang.Object doInBackground(java.lang.Object[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.1.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.1.doInBackground(java.lang.Object[]):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.1.doInBackground(java.lang.Void[]):byte[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected byte[] doInBackground(java.lang.Void... r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.1.doInBackground(java.lang.Void[]):byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.1.doInBackground(java.lang.Void[]):byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.1.onPostExecute(java.lang.Object):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected /* bridge */ /* synthetic */ void onPostExecute(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.1.onPostExecute(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.1.onPostExecute(java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.1.onPostExecute(byte[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected void onPostExecute(byte[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.1.onPostExecute(byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.1.onPostExecute(byte[]):void");
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternChecker$2 */
    static class AnonymousClass2 extends AsyncTask<Void, Void, Boolean> {
        private int mThrottleTimeout;
        final /* synthetic */ OnCheckCallback val$callback;
        final /* synthetic */ List val$pattern;
        final /* synthetic */ int val$userId;
        final /* synthetic */ LockPatternUtils val$utils;

        final /* synthetic */ class -java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0 implements CheckCredentialProgressCallback {
            /* renamed from: val$-lambdaCtx */
            private /* synthetic */ OnCheckCallback f18val$-lambdaCtx;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.2.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.<init>(com.android.internal.widget.LockPatternChecker$OnCheckCallback):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public /* synthetic */ -java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0(com.android.internal.widget.LockPatternChecker.OnCheckCallback r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.2.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.<init>(com.android.internal.widget.LockPatternChecker$OnCheckCallback):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.2.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.<init>(com.android.internal.widget.LockPatternChecker$OnCheckCallback):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.2.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.onEarlyMatched():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void onEarlyMatched() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.2.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.onEarlyMatched():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.2.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.onEarlyMatched():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.LockPatternChecker.2.-com_android_internal_widget_LockPatternChecker$2-mthref-0(com.android.internal.widget.LockPatternChecker$OnCheckCallback):void, dex: 
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
        /* renamed from: -com_android_internal_widget_LockPatternChecker$2-mthref-0 */
        static /* synthetic */ void m661-com_android_internal_widget_LockPatternChecker$2-mthref-0(com.android.internal.widget.LockPatternChecker.OnCheckCallback r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.LockPatternChecker.2.-com_android_internal_widget_LockPatternChecker$2-mthref-0(com.android.internal.widget.LockPatternChecker$OnCheckCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.2.-com_android_internal_widget_LockPatternChecker$2-mthref-0(com.android.internal.widget.LockPatternChecker$OnCheckCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.2.<init>(com.android.internal.widget.LockPatternUtils, java.util.List, int, com.android.internal.widget.LockPatternChecker$OnCheckCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(com.android.internal.widget.LockPatternUtils r1, java.util.List r2, int r3, com.android.internal.widget.LockPatternChecker.OnCheckCallback r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.2.<init>(com.android.internal.widget.LockPatternUtils, java.util.List, int, com.android.internal.widget.LockPatternChecker$OnCheckCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.2.<init>(com.android.internal.widget.LockPatternUtils, java.util.List, int, com.android.internal.widget.LockPatternChecker$OnCheckCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.2.doInBackground(java.lang.Void[]):java.lang.Boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected java.lang.Boolean doInBackground(java.lang.Void... r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.2.doInBackground(java.lang.Void[]):java.lang.Boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.2.doInBackground(java.lang.Void[]):java.lang.Boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.2.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected /* bridge */ /* synthetic */ java.lang.Object doInBackground(java.lang.Object[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.2.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.2.doInBackground(java.lang.Object[]):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.2.onPostExecute(java.lang.Boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected void onPostExecute(java.lang.Boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.2.onPostExecute(java.lang.Boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.2.onPostExecute(java.lang.Boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.2.onPostExecute(java.lang.Object):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected /* bridge */ /* synthetic */ void onPostExecute(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.2.onPostExecute(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.2.onPostExecute(java.lang.Object):void");
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternChecker$4 */
    static class AnonymousClass4 extends AsyncTask<Void, Void, byte[]> {
        private int mThrottleTimeout;
        final /* synthetic */ OnVerifyCallback val$callback;
        final /* synthetic */ long val$challenge;
        final /* synthetic */ boolean val$isPattern;
        final /* synthetic */ String val$password;
        final /* synthetic */ int val$userId;
        final /* synthetic */ LockPatternUtils val$utils;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.4.<init>(com.android.internal.widget.LockPatternUtils, java.lang.String, boolean, long, int, com.android.internal.widget.LockPatternChecker$OnVerifyCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass4(com.android.internal.widget.LockPatternUtils r1, java.lang.String r2, boolean r3, long r4, int r6, com.android.internal.widget.LockPatternChecker.OnVerifyCallback r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.4.<init>(com.android.internal.widget.LockPatternUtils, java.lang.String, boolean, long, int, com.android.internal.widget.LockPatternChecker$OnVerifyCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.4.<init>(com.android.internal.widget.LockPatternUtils, java.lang.String, boolean, long, int, com.android.internal.widget.LockPatternChecker$OnVerifyCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.4.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected /* bridge */ /* synthetic */ java.lang.Object doInBackground(java.lang.Object[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.4.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.4.doInBackground(java.lang.Object[]):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.4.doInBackground(java.lang.Void[]):byte[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected byte[] doInBackground(java.lang.Void... r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.4.doInBackground(java.lang.Void[]):byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.4.doInBackground(java.lang.Void[]):byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.4.onPostExecute(java.lang.Object):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected /* bridge */ /* synthetic */ void onPostExecute(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.widget.LockPatternChecker.4.onPostExecute(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.4.onPostExecute(java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.4.onPostExecute(byte[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected void onPostExecute(byte[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.4.onPostExecute(byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.4.onPostExecute(byte[]):void");
        }
    }

    public interface OnCheckCallback {
        void onChecked(boolean z, int i);

        void onEarlyMatched() {
        }
    }

    public interface OnVerifyCallback {
        void onVerified(byte[] bArr, int i);
    }

    public LockPatternChecker() {
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static android.os.AsyncTask<?, ?, ?> verifyPattern(com.android.internal.widget.LockPatternUtils r8, java.util.List<com.android.internal.widget.LockPatternView.Cell> r9, long r10, int r12, com.android.internal.widget.LockPatternChecker.OnVerifyCallback r13) {
        /*
        r1 = new com.android.internal.widget.LockPatternChecker$1;
        r2 = r8;
        r3 = r9;
        r4 = r10;
        r6 = r12;
        r7 = r13;
        r1.<init>(r2, r3, r4, r6, r7);
        r0 = 0;
        r0 = new java.lang.Void[r0];
        r1.execute(r0);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.verifyPattern(com.android.internal.widget.LockPatternUtils, java.util.List, long, int, com.android.internal.widget.LockPatternChecker$OnVerifyCallback):android.os.AsyncTask<?, ?, ?>");
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static android.os.AsyncTask<?, ?, ?> checkPattern(com.android.internal.widget.LockPatternUtils r2, java.util.List<com.android.internal.widget.LockPatternView.Cell> r3, int r4, com.android.internal.widget.LockPatternChecker.OnCheckCallback r5) {
        /*
        r0 = new com.android.internal.widget.LockPatternChecker$2;
        r0.<init>(r2, r3, r4, r5);
        r1 = 0;
        r1 = new java.lang.Void[r1];
        r0.execute(r1);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.checkPattern(com.android.internal.widget.LockPatternUtils, java.util.List, int, com.android.internal.widget.LockPatternChecker$OnCheckCallback):android.os.AsyncTask<?, ?, ?>");
    }

    public static AsyncTask<?, ?, ?> verifyPassword(LockPatternUtils utils, String password, long challenge, int userId, OnVerifyCallback callback) {
        final LockPatternUtils lockPatternUtils = utils;
        final String str = password;
        final long j = challenge;
        final int i = userId;
        final OnVerifyCallback onVerifyCallback = callback;
        AsyncTask<Void, Void, byte[]> task = new AsyncTask<Void, Void, byte[]>() {
            private int mThrottleTimeout;

            protected /* bridge */ /* synthetic */ Object doInBackground(Object[] args) {
                return doInBackground((Void[]) args);
            }

            protected byte[] doInBackground(Void... args) {
                try {
                    return lockPatternUtils.verifyPassword(str, j, i);
                } catch (RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return null;
                }
            }

            protected /* bridge */ /* synthetic */ void onPostExecute(Object result) {
                onPostExecute((byte[]) result);
            }

            protected void onPostExecute(byte[] result) {
                onVerifyCallback.onVerified(result, this.mThrottleTimeout);
            }
        };
        task.execute(new Void[0]);
        return task;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static android.os.AsyncTask<?, ?, ?> verifyTiedProfileChallenge(com.android.internal.widget.LockPatternUtils r9, java.lang.String r10, boolean r11, long r12, int r14, com.android.internal.widget.LockPatternChecker.OnVerifyCallback r15) {
        /*
        r0 = new com.android.internal.widget.LockPatternChecker$4;
        r1 = r9;
        r2 = r10;
        r3 = r11;
        r4 = r12;
        r6 = r14;
        r7 = r15;
        r0.<init>(r1, r2, r3, r4, r6, r7);
        r1 = 0;
        r1 = new java.lang.Void[r1];
        r0.execute(r1);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.verifyTiedProfileChallenge(com.android.internal.widget.LockPatternUtils, java.lang.String, boolean, long, int, com.android.internal.widget.LockPatternChecker$OnVerifyCallback):android.os.AsyncTask<?, ?, ?>");
    }

    public static AsyncTask<?, ?, ?> checkPassword(final LockPatternUtils utils, final String password, final int userId, final OnCheckCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;

            final /* synthetic */ class -java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0 implements CheckCredentialProgressCallback {
                /* renamed from: val$-lambdaCtx */
                private /* synthetic */ OnCheckCallback f19val$-lambdaCtx;

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.5.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.<init>(com.android.internal.widget.LockPatternChecker$OnCheckCallback):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                    Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                    	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                    	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                    	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                    	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                    	... 7 more
                    */
                public /* synthetic */ -java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0(com.android.internal.widget.LockPatternChecker.OnCheckCallback r1) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternChecker.5.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.<init>(com.android.internal.widget.LockPatternChecker$OnCheckCallback):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.5.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.<init>(com.android.internal.widget.LockPatternChecker$OnCheckCallback):void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.5.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.onEarlyMatched():void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                    Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                    	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                    	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                    	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                    	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                    	... 7 more
                    */
                public void onEarlyMatched() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternChecker.5.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.onEarlyMatched():void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternChecker.5.-java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0.onEarlyMatched():void");
                }
            }

            protected /* bridge */ /* synthetic */ Object doInBackground(Object[] args) {
                return doInBackground((Void[]) args);
            }

            protected Boolean doInBackground(Void... args) {
                try {
                    LockPatternUtils lockPatternUtils = utils;
                    String str = password;
                    int i = userId;
                    OnCheckCallback onCheckCallback = callback;
                    onCheckCallback.getClass();
                    return Boolean.valueOf(lockPatternUtils.checkPassword(str, i, new -java_lang_Boolean_doInBackground_java_lang_Void__args_LambdaImpl0(onCheckCallback)));
                } catch (RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return Boolean.valueOf(false);
                }
            }

            protected /* bridge */ /* synthetic */ void onPostExecute(Object result) {
                onPostExecute((Boolean) result);
            }

            protected void onPostExecute(Boolean result) {
                callback.onChecked(result.booleanValue(), this.mThrottleTimeout);
            }
        };
        task.execute(new Void[0]);
        return task;
    }
}
