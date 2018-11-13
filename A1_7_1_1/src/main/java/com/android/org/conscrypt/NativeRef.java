package com.android.org.conscrypt;

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
public abstract class NativeRef {
    final long context;

    public static class EC_GROUP extends NativeRef {
        public EC_GROUP(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EC_GROUP_clear_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class EC_POINT extends NativeRef {
        public EC_POINT(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EC_POINT_clear_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class EVP_AEAD_CTX extends NativeRef {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.NativeRef.EVP_AEAD_CTX.<init>(long):void, dex: 
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
        public EVP_AEAD_CTX(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.NativeRef.EVP_AEAD_CTX.<init>(long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.NativeRef.EVP_AEAD_CTX.<init>(long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: com.android.org.conscrypt.NativeRef.EVP_AEAD_CTX.finalize():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected void finalize() throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: com.android.org.conscrypt.NativeRef.EVP_AEAD_CTX.finalize():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.NativeRef.EVP_AEAD_CTX.finalize():void");
        }
    }

    public static class EVP_CIPHER_CTX extends NativeRef {
        public EVP_CIPHER_CTX(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EVP_CIPHER_CTX_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class EVP_MD_CTX extends NativeRef {
        public EVP_MD_CTX(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EVP_MD_CTX_destroy(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class EVP_PKEY extends NativeRef {
        public EVP_PKEY(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EVP_PKEY_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class HMAC_CTX extends NativeRef {
        public HMAC_CTX(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.HMAC_CTX_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public NativeRef(long ctx) {
        if (ctx == 0) {
            throw new NullPointerException("ctx == 0");
        }
        this.context = ctx;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof NativeRef)) {
            return false;
        }
        if (((NativeRef) o).context == this.context) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (int) this.context;
    }
}
