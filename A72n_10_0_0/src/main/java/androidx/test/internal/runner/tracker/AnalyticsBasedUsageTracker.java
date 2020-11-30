package androidx.test.internal.runner.tracker;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import androidx.test.internal.util.Checks;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AnalyticsBasedUsageTracker implements UsageTracker {
    private final URL analyticsURI;
    private final String apiLevel;
    private final String model;
    private final String screenResolution;
    private final String targetPackage;
    private final String trackingId;
    private final Map<String, String> usageTypeToVersion;
    private final String userId;

    private AnalyticsBasedUsageTracker(Builder builder) {
        this.usageTypeToVersion = new HashMap();
        this.trackingId = (String) Checks.checkNotNull(builder.trackingId);
        this.targetPackage = (String) Checks.checkNotNull(builder.targetPackage);
        this.analyticsURI = (URL) Checks.checkNotNull(builder.analyticsURI);
        this.apiLevel = (String) Checks.checkNotNull(builder.apiLevel);
        this.model = (String) Checks.checkNotNull(builder.model);
        this.screenResolution = (String) Checks.checkNotNull(builder.screenResolution);
        this.userId = (String) Checks.checkNotNull(builder.userId);
    }

    public static class Builder {
        private URL analyticsURI;
        private Uri analyticsUri = new Uri.Builder().scheme("https").authority("www.google-analytics.com").path("collect").build();
        private String apiLevel = String.valueOf(Build.VERSION.SDK_INT);
        private boolean hashed;
        private String model = Build.MODEL;
        private String screenResolution;
        private final Context targetContext;
        private String targetPackage;
        private String trackingId = "UA-36650409-3";
        private String userId;

        public Builder(Context targetContext2) {
            if (targetContext2 != null) {
                this.targetContext = targetContext2;
                return;
            }
            throw new NullPointerException("Context null!?");
        }

        public Builder withTargetPackage(String targetPackage2) {
            this.hashed = false;
            this.targetPackage = targetPackage2;
            return this;
        }

        public UsageTracker buildIfPossible() {
            if (!hasInternetPermission()) {
                Log.d("InfraTrack", "Tracking disabled due to lack of internet permissions");
                return null;
            }
            if (this.targetPackage == null) {
                withTargetPackage(this.targetContext.getPackageName());
            }
            if (this.targetPackage.contains("com.google.analytics")) {
                Log.d("InfraTrack", "Refusing to use analytics while testing analytics.");
                return null;
            }
            try {
                if (!this.targetPackage.startsWith("com.google.") && !this.targetPackage.startsWith("com.android.")) {
                    if (!this.targetPackage.startsWith("android.support.")) {
                        if (!this.hashed) {
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            digest.reset();
                            digest.update(this.targetPackage.getBytes("UTF-8"));
                            String valueOf = String.valueOf(new BigInteger(digest.digest()).toString(16));
                            this.targetPackage = valueOf.length() != 0 ? "sha256-".concat(valueOf) : new String("sha256-");
                        }
                        this.hashed = true;
                    }
                }
                try {
                    this.analyticsURI = new URL(this.analyticsUri.toString());
                    if (this.screenResolution == null) {
                        Display display = ((WindowManager) this.targetContext.getSystemService("window")).getDefaultDisplay();
                        if (display == null) {
                            this.screenResolution = "0x0";
                        } else {
                            this.screenResolution = display.getWidth() + "x" + display.getHeight();
                        }
                    }
                    if (this.userId == null) {
                        this.userId = UUID.randomUUID().toString();
                    }
                    return new AnalyticsBasedUsageTracker(this);
                } catch (MalformedURLException mule) {
                    String valueOf2 = String.valueOf(this.analyticsUri.toString());
                    Log.w("InfraTrack", valueOf2.length() != 0 ? "Tracking disabled bad url: ".concat(valueOf2) : new String("Tracking disabled bad url: "), mule);
                    return null;
                }
            } catch (NoSuchAlgorithmException nsae) {
                Log.d("InfraTrack", "Cannot hash package name.", nsae);
                return null;
            } catch (UnsupportedEncodingException uee) {
                Log.d("InfraTrack", "Impossible - no utf-8 encoding?", uee);
                return null;
            }
        }

        private boolean hasInternetPermission() {
            return this.targetContext.checkCallingOrSelfPermission("android.permission.INTERNET") == 0;
        }
    }

    @Override // androidx.test.internal.runner.tracker.UsageTracker
    public void trackUsage(String usageType, String version) {
        synchronized (this.usageTypeToVersion) {
            this.usageTypeToVersion.put(usageType, version);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x00a0, code lost:
        r2 = "an=" + java.net.URLEncoder.encode(r14.targetPackage, "UTF-8") + "&tid=" + java.net.URLEncoder.encode(r14.trackingId, "UTF-8") + "&v=1&z=" + android.os.SystemClock.uptimeMillis() + "&cid=" + java.net.URLEncoder.encode(r14.userId, "UTF-8") + "&sr=" + java.net.URLEncoder.encode(r14.screenResolution, "UTF-8") + "&cd2=" + java.net.URLEncoder.encode(r14.apiLevel, "UTF-8") + "&cd3=" + java.net.URLEncoder.encode(r14.model, "UTF-8") + "&t=appview&sc=start";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x00a2, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x00a3, code lost:
        android.util.Log.w("InfraTrack", "Impossible error happened. analytics disabled.", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
        r2 = null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00b8  */
    @Override // androidx.test.internal.runner.tracker.UsageTracker
    public void sendUsages() {
        Map<String, String> myUsages;
        String baseBody;
        synchronized (this.usageTypeToVersion) {
            if (!this.usageTypeToVersion.isEmpty()) {
                myUsages = new HashMap<>(this.usageTypeToVersion);
                this.usageTypeToVersion.clear();
            } else {
                return;
            }
        }
        for (Map.Entry<String, String> usage : myUsages.entrySet()) {
            HttpURLConnection analyticsConnection = null;
            try {
                analyticsConnection = (HttpURLConnection) this.analyticsURI.openConnection();
                byte[] body = (baseBody + "&cd=" + URLEncoder.encode(usage.getKey(), "UTF-8") + "&av=" + URLEncoder.encode(usage.getValue(), "UTF-8")).getBytes();
                analyticsConnection.setConnectTimeout(3000);
                analyticsConnection.setReadTimeout(5000);
                analyticsConnection.setDoOutput(true);
                analyticsConnection.setFixedLengthStreamingMode(body.length);
                analyticsConnection.getOutputStream().write(body);
                if (analyticsConnection.getResponseCode() / 100 != 2) {
                    String valueOf = String.valueOf(usage);
                    int responseCode = analyticsConnection.getResponseCode();
                    String responseMessage = analyticsConnection.getResponseMessage();
                    StringBuilder sb = new StringBuilder(45 + String.valueOf(valueOf).length() + String.valueOf(responseMessage).length());
                    sb.append("Analytics post: ");
                    sb.append(valueOf);
                    sb.append(" failed. code: ");
                    sb.append(responseCode);
                    sb.append(" - ");
                    sb.append(responseMessage);
                    Log.w("InfraTrack", sb.toString());
                }
                if (analyticsConnection == null) {
                }
            } catch (IOException ioe) {
                String valueOf2 = String.valueOf(usage);
                StringBuilder sb2 = new StringBuilder(25 + String.valueOf(valueOf2).length());
                sb2.append("Analytics post: ");
                sb2.append(valueOf2);
                sb2.append(" failed. ");
                Log.w("InfraTrack", sb2.toString(), ioe);
                if (0 == 0) {
                }
                analyticsConnection.disconnect();
            } catch (Throwable th) {
                if (0 != 0) {
                    analyticsConnection.disconnect();
                }
                throw th;
            }
            analyticsConnection.disconnect();
        }
        while (r3.hasNext()) {
        }
    }
}
