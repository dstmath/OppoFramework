package gov.nist.javax.sip.clientauthutils;

import gov.nist.javax.sip.SipStackImpl;
import java.util.Timer;
import javax.sip.header.HeaderFactory;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AuthenticationHelperImpl implements AuthenticationHelper {
    private Object accountManager;
    private CredentialsCache cachedCredentials;
    private HeaderFactory headerFactory;
    private SipStackImpl sipStack;
    Timer timer;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.<init>(gov.nist.javax.sip.SipStackImpl, gov.nist.javax.sip.clientauthutils.AccountManager, javax.sip.header.HeaderFactory):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public AuthenticationHelperImpl(gov.nist.javax.sip.SipStackImpl r1, gov.nist.javax.sip.clientauthutils.AccountManager r2, javax.sip.header.HeaderFactory r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.<init>(gov.nist.javax.sip.SipStackImpl, gov.nist.javax.sip.clientauthutils.AccountManager, javax.sip.header.HeaderFactory):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.<init>(gov.nist.javax.sip.SipStackImpl, gov.nist.javax.sip.clientauthutils.AccountManager, javax.sip.header.HeaderFactory):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.<init>(gov.nist.javax.sip.SipStackImpl, gov.nist.javax.sip.clientauthutils.SecureAccountManager, javax.sip.header.HeaderFactory):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public AuthenticationHelperImpl(gov.nist.javax.sip.SipStackImpl r1, gov.nist.javax.sip.clientauthutils.SecureAccountManager r2, javax.sip.header.HeaderFactory r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.<init>(gov.nist.javax.sip.SipStackImpl, gov.nist.javax.sip.clientauthutils.SecureAccountManager, javax.sip.header.HeaderFactory):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.<init>(gov.nist.javax.sip.SipStackImpl, gov.nist.javax.sip.clientauthutils.SecureAccountManager, javax.sip.header.HeaderFactory):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.getAuthorization(java.lang.String, java.lang.String, java.lang.String, javax.sip.header.WWWAuthenticateHeader, gov.nist.javax.sip.clientauthutils.UserCredentialHash):javax.sip.header.AuthorizationHeader, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.header.AuthorizationHeader getAuthorization(java.lang.String r1, java.lang.String r2, java.lang.String r3, javax.sip.header.WWWAuthenticateHeader r4, gov.nist.javax.sip.clientauthutils.UserCredentialHash r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.getAuthorization(java.lang.String, java.lang.String, java.lang.String, javax.sip.header.WWWAuthenticateHeader, gov.nist.javax.sip.clientauthutils.UserCredentialHash):javax.sip.header.AuthorizationHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.getAuthorization(java.lang.String, java.lang.String, java.lang.String, javax.sip.header.WWWAuthenticateHeader, gov.nist.javax.sip.clientauthutils.UserCredentialHash):javax.sip.header.AuthorizationHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.getAuthorization(java.lang.String, java.lang.String, java.lang.String, javax.sip.header.WWWAuthenticateHeader, gov.nist.javax.sip.clientauthutils.UserCredentials):javax.sip.header.AuthorizationHeader, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.header.AuthorizationHeader getAuthorization(java.lang.String r1, java.lang.String r2, java.lang.String r3, javax.sip.header.WWWAuthenticateHeader r4, gov.nist.javax.sip.clientauthutils.UserCredentials r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.getAuthorization(java.lang.String, java.lang.String, java.lang.String, javax.sip.header.WWWAuthenticateHeader, gov.nist.javax.sip.clientauthutils.UserCredentials):javax.sip.header.AuthorizationHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.getAuthorization(java.lang.String, java.lang.String, java.lang.String, javax.sip.header.WWWAuthenticateHeader, gov.nist.javax.sip.clientauthutils.UserCredentials):javax.sip.header.AuthorizationHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.removeBranchID(javax.sip.message.Request):void, dex: 
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
    private void removeBranchID(javax.sip.message.Request r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.removeBranchID(javax.sip.message.Request):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.removeBranchID(javax.sip.message.Request):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.handleChallenge(javax.sip.message.Response, javax.sip.ClientTransaction, javax.sip.SipProvider, int):javax.sip.ClientTransaction, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public javax.sip.ClientTransaction handleChallenge(javax.sip.message.Response r1, javax.sip.ClientTransaction r2, javax.sip.SipProvider r3, int r4) throws javax.sip.SipException, java.lang.NullPointerException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.handleChallenge(javax.sip.message.Response, javax.sip.ClientTransaction, javax.sip.SipProvider, int):javax.sip.ClientTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.handleChallenge(javax.sip.message.Response, javax.sip.ClientTransaction, javax.sip.SipProvider, int):javax.sip.ClientTransaction");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.removeCachedAuthenticationHeaders(java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void removeCachedAuthenticationHeaders(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.removeCachedAuthenticationHeaders(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.removeCachedAuthenticationHeaders(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.setAuthenticationHeaders(javax.sip.message.Request):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void setAuthenticationHeaders(javax.sip.message.Request r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.setAuthenticationHeaders(javax.sip.message.Request):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl.setAuthenticationHeaders(javax.sip.message.Request):void");
    }
}
