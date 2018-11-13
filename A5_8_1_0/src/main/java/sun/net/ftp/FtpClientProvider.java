package sun.net.ftp;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.net.ftp.impl.DefaultFtpClientProvider;

public abstract class FtpClientProvider {
    private static final Object lock = new Object();
    private static FtpClientProvider provider = null;

    public abstract FtpClient createFtpClient();

    protected FtpClientProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("ftpClientProvider"));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x001b A:{ExcHandler: java.lang.ClassNotFoundException (r2_0 'x' java.lang.Exception), Splitter: B:5:0x000e} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x001b A:{ExcHandler: java.lang.ClassNotFoundException (r2_0 'x' java.lang.Exception), Splitter: B:5:0x000e} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x001b A:{ExcHandler: java.lang.ClassNotFoundException (r2_0 'x' java.lang.Exception), Splitter: B:5:0x000e} */
    /* JADX WARNING: Missing block: B:8:0x001b, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0025, code:
            throw new java.util.ServiceConfigurationError(r2.toString());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean loadProviderFromProperty() {
        String cm = System.getProperty("sun.net.ftpClientProvider");
        if (cm == null) {
            return false;
        }
        try {
            provider = (FtpClientProvider) Class.forName(cm, true, null).newInstance();
            return true;
        } catch (Exception x) {
        }
    }

    private static boolean loadProviderAsService() {
        return false;
    }

    public static FtpClientProvider provider() {
        synchronized (lock) {
            FtpClientProvider ftpClientProvider;
            if (provider != null) {
                ftpClientProvider = provider;
                return ftpClientProvider;
            }
            ftpClientProvider = (FtpClientProvider) AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    if (FtpClientProvider.loadProviderFromProperty()) {
                        return FtpClientProvider.provider;
                    }
                    if (FtpClientProvider.loadProviderAsService()) {
                        return FtpClientProvider.provider;
                    }
                    FtpClientProvider.provider = new DefaultFtpClientProvider();
                    return FtpClientProvider.provider;
                }
            });
            return ftpClientProvider;
        }
    }
}
