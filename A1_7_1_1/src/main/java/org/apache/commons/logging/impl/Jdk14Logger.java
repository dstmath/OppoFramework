package org.apache.commons.logging.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;

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
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@Deprecated
public class Jdk14Logger implements Log, Serializable {
    protected static final Level dummyLevel = null;
    protected static int sLogLevel;
    protected transient Logger logger;
    protected String name;

    private class HttpSimpleFormatter extends Formatter {
        final /* synthetic */ Jdk14Logger this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.impl.Jdk14Logger.HttpSimpleFormatter.<init>(org.apache.commons.logging.impl.Jdk14Logger):void, dex: 
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
        HttpSimpleFormatter(org.apache.commons.logging.impl.Jdk14Logger r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.impl.Jdk14Logger.HttpSimpleFormatter.<init>(org.apache.commons.logging.impl.Jdk14Logger):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.impl.Jdk14Logger.HttpSimpleFormatter.<init>(org.apache.commons.logging.impl.Jdk14Logger):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.commons.logging.impl.Jdk14Logger.HttpSimpleFormatter.format(java.util.logging.LogRecord):java.lang.String, dex: 
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
        public java.lang.String format(java.util.logging.LogRecord r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.commons.logging.impl.Jdk14Logger.HttpSimpleFormatter.format(java.util.logging.LogRecord):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.impl.Jdk14Logger.HttpSimpleFormatter.format(java.util.logging.LogRecord):java.lang.String");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.commons.logging.impl.Jdk14Logger.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.commons.logging.impl.Jdk14Logger.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.impl.Jdk14Logger.<clinit>():void");
    }

    public Jdk14Logger(String name) {
        this.logger = null;
        this.name = null;
        this.name = name;
        this.logger = getLogger();
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Class[] clsArr = new Class[2];
            clsArr[0] = String.class;
            clsArr[1] = Integer.TYPE;
            Method getIntMethod = classType.getDeclaredMethod("getInt", clsArr);
            Object[] objArr = new Object[2];
            objArr[0] = "net.httplog.level";
            objArr[1] = Integer.valueOf(0);
            sLogLevel = ((Integer) getIntMethod.invoke(classType, objArr)).intValue();
            if (sLogLevel == 2 && name.indexOf("org.apache.http.wire") != -1) {
                configLogFile();
            }
        } catch (ClassNotFoundException c) {
            System.out.println("error:" + c.getMessage());
        } catch (NoSuchMethodException ie) {
            System.out.println("error:" + ie.getMessage());
        } catch (InvocationTargetException iee) {
            System.out.println("error:" + iee.getMessage());
        } catch (IllegalAccessException iae) {
            System.out.println("error:" + iae.getMessage());
        }
    }

    private void configLogFile() {
        try {
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Thread t = Thread.currentThread();
            File folder = new File("/data/http");
            if (!folder.isDirectory()) {
                folder.mkdir();
            }
            String filePath = "%thttp.log";
            System.out.println("http log file path:" + System.getProperty("java.io.tmpdir") + ":" + filePath);
            FileHandler fh = new FileHandler(filePath, true);
            fh.setFormatter(new HttpSimpleFormatter(this));
            this.logger.addHandler(fh);
        } catch (SecurityException e) {
            System.out.println("error:" + e.getMessage());
        } catch (IOException ioe) {
            System.out.println("error:" + ioe.getMessage());
        }
    }

    private void log(Level level, String msg, Throwable ex) {
        Logger logger = getLogger();
        if (logger.isLoggable(level)) {
            StackTraceElement[] locations = new Throwable().getStackTrace();
            String cname = "unknown";
            String method = "unknown";
            if (locations != null && locations.length > 2) {
                StackTraceElement caller = locations[2];
                cname = caller.getClassName();
                method = caller.getMethodName();
            }
            if (ex == null) {
                if (sLogLevel == 1) {
                    if (this.name.indexOf("org.apache.http.headers") != -1) {
                        System.out.println(this.name + ":" + method + ":" + msg);
                    }
                } else if (sLogLevel == 2) {
                    if (this.name.indexOf("org.apache.http.wire") != -1) {
                        System.out.println(this.name + ":" + method + ":" + msg);
                    }
                } else if (sLogLevel == 3) {
                    System.out.println(this.name + ":" + method + ":" + msg);
                }
                logger.logp(level, cname, method, msg);
                return;
            }
            logger.logp(level, cname, method, msg, ex);
        }
    }

    public void debug(Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }

    public void debug(Object message, Throwable exception) {
        log(Level.FINE, String.valueOf(message), exception);
    }

    public void error(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    public void error(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    public void fatal(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    public void fatal(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    public Logger getLogger() {
        if (this.logger == null) {
            this.logger = Logger.getLogger(this.name);
        }
        if (sLogLevel > 0) {
            this.logger.setLevel(Level.ALL);
        }
        return this.logger;
    }

    public void info(Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }

    public void info(Object message, Throwable exception) {
        log(Level.INFO, String.valueOf(message), exception);
    }

    public boolean isDebugEnabled() {
        return getLogger().isLoggable(Level.FINE);
    }

    public boolean isErrorEnabled() {
        return getLogger().isLoggable(Level.SEVERE);
    }

    public boolean isFatalEnabled() {
        return getLogger().isLoggable(Level.SEVERE);
    }

    public boolean isInfoEnabled() {
        return getLogger().isLoggable(Level.INFO);
    }

    public boolean isTraceEnabled() {
        return getLogger().isLoggable(Level.FINEST);
    }

    public boolean isWarnEnabled() {
        return getLogger().isLoggable(Level.WARNING);
    }

    public void trace(Object message) {
        log(Level.FINEST, String.valueOf(message), null);
    }

    public void trace(Object message, Throwable exception) {
        log(Level.FINEST, String.valueOf(message), exception);
    }

    public void warn(Object message) {
        log(Level.WARNING, String.valueOf(message), null);
    }

    public void warn(Object message, Throwable exception) {
        log(Level.WARNING, String.valueOf(message), exception);
    }
}
