package com.android.server.location;

import android.net.TrafficStats;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GpsXtraDownloader {
    private static final int CONNECTION_TIMEOUT_MS = ((int) TimeUnit.SECONDS.toMillis(30));
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String DEFAULT_USER_AGENT = "Android";
    private static final long MAXIMUM_CONTENT_LENGTH_BYTES = 1000000;
    private static final int READ_TIMEOUT_MS = ((int) TimeUnit.SECONDS.toMillis(60));
    private static final String TAG = "GpsXtraDownloader";
    private int mNextServerIndex;
    private final String mUserAgent;
    private final String[] mXtraServers;

    GpsXtraDownloader(Properties properties) {
        int count = 0;
        String server1 = properties.getProperty("XTRA_SERVER_1");
        String server2 = properties.getProperty("XTRA_SERVER_2");
        String server3 = properties.getProperty("XTRA_SERVER_3");
        if (server1 != null) {
            count = 1;
        }
        if (server2 != null) {
            count++;
        }
        if (server3 != null) {
            count++;
        }
        String agent = properties.getProperty("XTRA_USER_AGENT");
        if (TextUtils.isEmpty(agent)) {
            this.mUserAgent = DEFAULT_USER_AGENT;
        } else {
            this.mUserAgent = agent;
        }
        if (count == 0) {
            Log.e(TAG, "No XTRA servers were specified in the GPS configuration");
            this.mXtraServers = null;
            return;
        }
        int count2;
        this.mXtraServers = new String[count];
        if (server1 != null) {
            this.mXtraServers[0] = server1;
            count2 = 1;
        } else {
            count2 = 0;
        }
        if (server2 != null) {
            count = count2 + 1;
            this.mXtraServers[count2] = server2;
            count2 = count;
        }
        if (server3 != null) {
            count = count2 + 1;
            this.mXtraServers[count2] = server3;
        } else {
            count = count2;
        }
        this.mNextServerIndex = new Random().nextInt(count);
    }

    byte[] downloadXtraData() {
        byte[] result = null;
        int startIndex = this.mNextServerIndex;
        if (this.mXtraServers == null) {
            return null;
        }
        loop0:
        while (result == null) {
            int oldTag = TrafficStats.getAndSetThreadStatsTag(-188);
            try {
                result = doDownload(this.mXtraServers[this.mNextServerIndex]);
                this.mNextServerIndex++;
                if (this.mNextServerIndex == this.mXtraServers.length) {
                    this.mNextServerIndex = 0;
                }
                if (this.mNextServerIndex == startIndex) {
                    break loop0;
                }
            } finally {
                TrafficStats.setThreadStatsTag(oldTag);
            }
        }
        return result;
    }

    protected byte[] doDownload(String url) {
        Throwable th;
        if (DEBUG) {
            Log.d(TAG, "Downloading XTRA data from " + url);
        }
        HttpURLConnection connection = null;
        try {
            Throwable th2;
            InputStream inputStream;
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Accept", "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic");
            connection.setRequestProperty("x-wap-profile", "http://www.openmobilealliance.org/tech/profiles/UAPROF/ccppschema-20021212#");
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.connect();
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                if (DEBUG) {
                    Log.d(TAG, "HTTP error downloading gps XTRA: " + statusCode);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                return null;
            }
            th2 = null;
            inputStream = null;
            try {
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                do {
                    int count = inputStream.read(buffer);
                    if (count != -1) {
                        bytes.write(buffer, 0, count);
                    } else {
                        byte[] toByteArray = bytes.toByteArray();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        }
                        if (th2 != null) {
                            throw th2;
                        } else {
                            if (connection != null) {
                                connection.disconnect();
                            }
                            return toByteArray;
                        }
                    }
                } while (((long) bytes.size()) <= MAXIMUM_CONTENT_LENGTH_BYTES);
                if (DEBUG) {
                    Log.d(TAG, "XTRA file too large");
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th4) {
                        th2 = th4;
                    }
                }
                if (th2 != null) {
                    throw th2;
                } else {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    return null;
                }
            } catch (Throwable th22) {
                Throwable th5 = th22;
                th22 = th;
                th = th5;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th6) {
                    if (th22 == null) {
                        th22 = th6;
                    } else if (th22 != th6) {
                        th22.addSuppressed(th6);
                    }
                }
            }
            if (th22 != null) {
                throw th22;
            }
            throw th;
        } catch (IOException ioe) {
            if (DEBUG) {
                Log.d(TAG, "Error downloading gps XTRA: ", ioe);
            }
            if (connection != null) {
                connection.disconnect();
            }
            return null;
        } catch (Throwable th7) {
            if (connection != null) {
                connection.disconnect();
            }
            throw th7;
        }
    }
}
