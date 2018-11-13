package sun.net.spi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import sun.misc.REException;
import sun.misc.RegexpPool;
import sun.net.NetProperties;
import sun.net.SocksProxy;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DefaultProxySelector extends ProxySelector {
    private static final String SOCKS_PROXY_VERSION = "socksProxyVersion";
    private static boolean hasSystemProxies;
    static final String[][] props = null;

    static class NonProxyInfo {
        static final String defStringVal = "localhost|127.*|[::1]|0.0.0.0|[::0]";
        static NonProxyInfo ftpNonProxyInfo;
        static NonProxyInfo httpNonProxyInfo;
        static NonProxyInfo httpsNonProxyInfo;
        final String defaultVal;
        RegexpPool hostsPool;
        String hostsSource;
        final String property;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.net.spi.DefaultProxySelector.NonProxyInfo.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.net.spi.DefaultProxySelector.NonProxyInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.net.spi.DefaultProxySelector.NonProxyInfo.<clinit>():void");
        }

        NonProxyInfo(String p, String s, RegexpPool pool, String d) {
            this.property = p;
            this.hostsSource = s;
            this.hostsPool = pool;
            this.defaultVal = d;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.net.spi.DefaultProxySelector.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.net.spi.DefaultProxySelector.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.spi.DefaultProxySelector.<clinit>():void");
    }

    public List<Proxy> select(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }
        final String protocol = uri.getScheme();
        String host = uri.getHost();
        if (host == null) {
            String auth = uri.getAuthority();
            if (auth != null) {
                int i = auth.indexOf(64);
                if (i >= 0) {
                    auth = auth.substring(i + 1);
                }
                i = auth.lastIndexOf(58);
                if (i >= 0) {
                    auth = auth.substring(0, i);
                }
                host = auth;
            }
        }
        if (protocol == null || host == null) {
            throw new IllegalArgumentException("protocol = " + protocol + " host = " + host);
        }
        List<Proxy> proxyl = new ArrayList(1);
        NonProxyInfo pinfo = null;
        if ("http".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.httpNonProxyInfo;
        } else if ("https".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.httpsNonProxyInfo;
        } else if ("ftp".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.ftpNonProxyInfo;
        }
        String proto = protocol;
        final NonProxyInfo nprop = pinfo;
        final String urlhost = host.toLowerCase();
        proxyl.add((Proxy) AccessController.doPrivileged(new PrivilegedAction<Proxy>() {
            public Proxy run() {
                String phost = null;
                int i = 0;
                while (i < DefaultProxySelector.props.length) {
                    if (DefaultProxySelector.props[i][0].equalsIgnoreCase(protocol)) {
                        int j = 1;
                        while (j < DefaultProxySelector.props[i].length) {
                            phost = NetProperties.get(DefaultProxySelector.props[i][j] + "Host");
                            if (phost != null && phost.length() != 0) {
                                break;
                            }
                            j++;
                        }
                        if (phost == null || phost.length() == 0) {
                            return Proxy.NO_PROXY;
                        }
                        if (nprop != null) {
                            String nphosts = NetProperties.get(nprop.property);
                            synchronized (nprop) {
                                if (nphosts == null) {
                                    if (nprop.defaultVal != null) {
                                        nphosts = nprop.defaultVal;
                                    } else {
                                        nprop.hostsSource = null;
                                        nprop.hostsPool = null;
                                    }
                                } else if (nphosts.length() != 0) {
                                    nphosts = nphosts + "|localhost|127.*|[::1]|0.0.0.0|[::0]";
                                }
                                if (!(nphosts == null || nphosts.equals(nprop.hostsSource))) {
                                    RegexpPool pool = new RegexpPool();
                                    StringTokenizer st = new StringTokenizer(nphosts, "|", false);
                                    while (st.hasMoreTokens()) {
                                        try {
                                            pool.add(st.nextToken().toLowerCase(), Boolean.TRUE);
                                        } catch (REException e) {
                                        }
                                    }
                                    nprop.hostsPool = pool;
                                    nprop.hostsSource = nphosts;
                                }
                                if (nprop.hostsPool == null || nprop.hostsPool.match(urlhost) == null) {
                                } else {
                                    Proxy proxy = Proxy.NO_PROXY;
                                    return proxy;
                                }
                            }
                        }
                        int pport = NetProperties.getInteger(DefaultProxySelector.props[i][j] + "Port", 0).intValue();
                        if (pport == 0 && j < DefaultProxySelector.props[i].length - 1) {
                            for (int k = 1; k < DefaultProxySelector.props[i].length - 1; k++) {
                                if (k != j && pport == 0) {
                                    pport = NetProperties.getInteger(DefaultProxySelector.props[i][k] + "Port", 0).intValue();
                                }
                            }
                        }
                        if (pport == 0) {
                            if (j == DefaultProxySelector.props[i].length - 1) {
                                pport = DefaultProxySelector.this.defaultPort("socket");
                            } else {
                                pport = DefaultProxySelector.this.defaultPort(protocol);
                            }
                        }
                        InetSocketAddress saddr = InetSocketAddress.createUnresolved(phost, pport);
                        if (j == DefaultProxySelector.props[i].length - 1) {
                            return SocksProxy.create(saddr, NetProperties.getInteger(DefaultProxySelector.SOCKS_PROXY_VERSION, 5).intValue());
                        }
                        return new Proxy(Type.HTTP, saddr);
                    }
                    i++;
                }
                return Proxy.NO_PROXY;
            }
        }));
        return proxyl;
    }

    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (uri == null || sa == null || ioe == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
    }

    private int defaultPort(String protocol) {
        if ("http".equalsIgnoreCase(protocol)) {
            return 80;
        }
        if ("https".equalsIgnoreCase(protocol)) {
            return 443;
        }
        if ("ftp".equalsIgnoreCase(protocol)) {
            return 80;
        }
        if ("socket".equalsIgnoreCase(protocol)) {
            return SocksConsts.DEFAULT_PORT;
        }
        if ("gopher".equalsIgnoreCase(protocol)) {
            return 80;
        }
        return -1;
    }
}
