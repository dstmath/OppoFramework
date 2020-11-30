package android.content.res;

import android.app.backup.FullBackup;
import android.content.res.OppoThemeZipFile;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.XmlUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipFile;
import oppo.util.OppoDisplayUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class OppoBaseFile {
    protected static final String ACCESSIBLE = "accessible";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PACKAGE = "package";
    protected static final String COLORS_XML = "colors.xml";
    protected static final boolean DEBUG_THEME = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int INPUT_STREAM_CACHE_BYTE_COUNT = 8192;
    protected static final String NINE_SUFFIX = ".9.png";
    protected static final String PATH_DIVIDER = "/";
    protected static final String PATH_SUFFIX = "#*.png";
    protected static final String PNG_SUFFIX = ".png";
    protected static final String TAG = "OppoBaseFile";
    private static final String TAG_BOOLEAN = "bool";
    private static final String TAG_COLOR = "color";
    private static final String TAG_DIMEN = "dimen";
    private static final String TAG_DRAWABLE = "drawable";
    private static final String TAG_INTEGER = "integer";
    private static final String TAG_STRING = "string";
    private static final String TRUE = "true";
    protected static final String XML_SUFFIX = ".xml";
    protected static ArrayList<String> mNightWhites = new ArrayList<>();
    protected static Map<String, WeakReference<OppoBaseFile>> sCacheFiles = new ConcurrentHashMap();
    protected static int[] sDensities = OppoDisplayUtils.getBestDensityOrder(sDensity);
    protected static int sDensity = DisplayMetrics.DENSITY_DEVICE_STABLE;
    protected ColorBaseResourcesImpl mBaseResources;
    protected SparseArray mCharSequences;
    protected SparseArray mIntegers;
    protected String mPackageName;
    protected ResourcesImpl mResources;
    private boolean mSupportChar;
    private boolean mSupportFile;
    private boolean mSupportInt;

    static {
        mNightWhites.add("icons");
        mNightWhites.add(OppoThemeResources.LOCKSCREEN_PACKAGE);
        mNightWhites.add("com.oppo.launcher");
    }

    public OppoBaseFile(String packageName, ColorBaseResourcesImpl baseResources, boolean supportInt, boolean supportChar, boolean supportFile) {
        this.mIntegers = null;
        this.mCharSequences = null;
        this.mResources = null;
        this.mBaseResources = null;
        this.mPackageName = null;
        this.mSupportChar = false;
        this.mSupportFile = false;
        this.mSupportInt = false;
        this.mIntegers = new SparseArray();
        this.mCharSequences = new SparseArray();
        this.mPackageName = packageName;
        this.mSupportInt = supportInt;
        this.mSupportChar = supportChar;
        this.mSupportFile = supportFile;
        this.mBaseResources = baseResources;
        ColorBaseResourcesImpl colorBaseResourcesImpl = this.mBaseResources;
        this.mResources = colorBaseResourcesImpl.typeCasting(colorBaseResourcesImpl);
    }

    public void setResource(ColorBaseResourcesImpl baseResources) {
        this.mBaseResources = baseResources;
        ColorBaseResourcesImpl colorBaseResourcesImpl = this.mBaseResources;
        this.mResources = colorBaseResourcesImpl.typeCasting(colorBaseResourcesImpl);
    }

    public static boolean rejectTheme(ResourcesImpl resources, String packageName) {
        return rejectNightMode(resources) && !mNightWhites.contains(packageName);
    }

    public static boolean rejectNightMode(ResourcesImpl resources) {
        Configuration configuration = resources.getConfiguration();
        if (configuration != null && (configuration.uiMode & 48) == 32) {
            return true;
        }
        return false;
    }

    protected static String getPackageName(String packageName) {
        if (OppoThemeResources.FRAMEWORK_NAME.equals(packageName) || "icons".equals(packageName)) {
            return "android";
        }
        if (OppoThemeResources.OPPO_NAME.equals(packageName) || OppoThemeResources.LOCKSCREEN_PACKAGE.equals(packageName)) {
            return OppoThemeResources.OPPO_PACKAGE;
        }
        return packageName;
    }

    /* access modifiers changed from: protected */
    public CharSequence getCharSequence(int i) {
        return (CharSequence) this.mCharSequences.get(i);
    }

    /* access modifiers changed from: protected */
    public Integer getInt(int id) {
        return (Integer) this.mIntegers.get(id);
    }

    /* access modifiers changed from: protected */
    public boolean hasValues() {
        if (this.mIntegers.size() > 0 || this.mCharSequences.size() > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public synchronized void clean(ZipFile file) {
        closeZipFile(file);
        this.mIntegers.clear();
        this.mCharSequences.clear();
        sCacheFiles.clear();
    }

    /* access modifiers changed from: protected */
    public synchronized void closeZipFile(ZipFile file) {
        if (file != null) {
            try {
                file.close();
            } catch (Exception exception) {
                if (DEBUG_THEME) {
                    Log.w(TAG, "OppoThemeZipFile Exception exception: " + exception);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0055, code lost:
        r5.append("in IOException happened when loadThemeValues, msg = ");
        r5.append(r1.toString());
        android.util.Log.e(android.content.res.OppoBaseFile.TAG, r5.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        return;
     */
    public void parseXmlStream(int index, OppoThemeZipFile.ThemeFileInfo themeFileInfo) {
        StringBuilder sb;
        if (themeFileInfo != null) {
            InputStream in = null;
            BufferedInputStream bufferedinputstream = null;
            try {
                in = themeFileInfo.mInput;
                XmlPullParser xmlpullparser = XmlPullParserFactory.newInstance().newPullParser();
                bufferedinputstream = new BufferedInputStream(in, 8192);
                xmlpullparser.setInput(bufferedinputstream, null);
                mergeThemeValues(index, xmlpullparser);
                try {
                    bufferedinputstream.close();
                } catch (IOException e) {
                    if (DEBUG_THEME) {
                        Log.e(TAG, "bufferedinputstream IOException happened when loadThemeValues, msg = " + e.toString());
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e2) {
                        e = e2;
                        if (DEBUG_THEME) {
                            sb = new StringBuilder();
                        }
                    }
                }
            } catch (XmlPullParserException e3) {
                if (DEBUG_THEME) {
                    Log.e(TAG, "XmlPullParserException happened when loadThemeValues, msg = " + e3.toString());
                }
                if (bufferedinputstream != null) {
                    try {
                        bufferedinputstream.close();
                    } catch (IOException e4) {
                        if (DEBUG_THEME) {
                            Log.e(TAG, "bufferedinputstream IOException happened when loadThemeValues, msg = " + e4.toString());
                        }
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                        e = e5;
                        if (DEBUG_THEME) {
                            sb = new StringBuilder();
                        }
                    }
                }
            } catch (Throwable th) {
                if (bufferedinputstream != null) {
                    try {
                        bufferedinputstream.close();
                    } catch (IOException e6) {
                        if (DEBUG_THEME) {
                            Log.e(TAG, "bufferedinputstream IOException happened when loadThemeValues, msg = " + e6.toString());
                        }
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e7) {
                        if (DEBUG_THEME) {
                            Log.e(TAG, "in IOException happened when loadThemeValues, msg = " + e7.toString());
                        }
                    }
                }
                throw th;
            }
        }
    }

    private void mergeThemeValues(int index, XmlPullParser xmlpullparser) {
        String packageName = null;
        String resourceName = null;
        try {
            int eventType = xmlpullparser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String resourceType = xmlpullparser.getName().trim();
                        if (!TextUtils.isEmpty(resourceType)) {
                            int count = xmlpullparser.getAttributeCount();
                            if (count > 0) {
                                for (int i = 0; i < count; i++) {
                                    String attributeName = xmlpullparser.getAttributeName(i).trim();
                                    if (attributeName.equals("name")) {
                                        resourceName = xmlpullparser.getAttributeValue(i);
                                    } else if (attributeName.equals("package")) {
                                        packageName = xmlpullparser.getAttributeValue(i);
                                    }
                                }
                                String resourceValue = xmlpullparser.nextText();
                                if (!TextUtils.isEmpty(resourceName)) {
                                    if (!TextUtils.isEmpty(resourceValue)) {
                                        if (TextUtils.isEmpty(packageName)) {
                                            if (index != 0) {
                                                if (index <= 2) {
                                                    if (index == 1) {
                                                        packageName = "android";
                                                    } else if (index == 2) {
                                                        packageName = OppoThemeResources.OPPO_PACKAGE;
                                                    }
                                                }
                                            }
                                            packageName = this.mPackageName;
                                        }
                                        int resourceId = this.mResources.getIdentifier(resourceName, resourceType, packageName);
                                        if (resourceId > 0) {
                                            if (!resourceType.equals(TAG_BOOLEAN)) {
                                                if (!resourceType.equals("color") && !resourceType.equals(TAG_INTEGER)) {
                                                    if (!resourceType.equals(TAG_DRAWABLE)) {
                                                        if (resourceType.equals(TAG_STRING)) {
                                                            if (this.mSupportChar && this.mCharSequences.indexOfKey(resourceId) < 0) {
                                                                this.mCharSequences.put(resourceId, resourceValue);
                                                            }
                                                        } else if (resourceType.equals(TAG_DIMEN) && this.mSupportInt && this.mIntegers.indexOfKey(resourceId) < 0) {
                                                            Integer integer = parseDimension(this.mResources, resourceValue.toString());
                                                            if (integer != null) {
                                                                this.mIntegers.put(resourceId, integer);
                                                            }
                                                        }
                                                    }
                                                }
                                                if (this.mSupportInt && this.mIntegers.indexOfKey(resourceId) < 0) {
                                                    try {
                                                        this.mIntegers.put(resourceId, Integer.valueOf(XmlUtils.convertValueToUnsignedInt(resourceValue.trim(), 0)));
                                                    } catch (NumberFormatException e) {
                                                        if (DEBUG_THEME) {
                                                            Log.e(TAG, "mergeThemeValues NumberFormatException happened when loadThemeValues, msg = " + e.getMessage());
                                                        }
                                                    }
                                                }
                                            } else if (this.mSupportInt && this.mIntegers.indexOfKey(resourceId) < 0) {
                                                if (TRUE.equals(resourceValue.trim())) {
                                                    this.mIntegers.put(resourceId, 1);
                                                } else {
                                                    this.mIntegers.put(resourceId, 0);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (eventType != 3) {
                    }
                }
                eventType = xmlpullparser.next();
            }
        } catch (XmlPullParserException e2) {
            if (DEBUG_THEME) {
                Log.e(TAG, "mergeThemeValues XmlPullParserException happened when loadThemeValues, msg = " + e2.getMessage());
            }
        } catch (IOException e3) {
            if (DEBUG_THEME) {
                Log.e(TAG, "mergeThemeValues IOException happened when loadThemeValues, msg = " + e3.getMessage());
            }
        }
    }

    private Integer parseDimension(ResourcesImpl res, String value) {
        int shift;
        int radix;
        int unitType;
        int intPos = -4;
        int dotPos = -3;
        int fractionPos = -2;
        int unitPos = -1;
        int i = 0;
        while (true) {
            if (i >= value.length()) {
                break;
            }
            char c = value.charAt(i);
            if (intPos == -4 && c >= '0' && c <= '9') {
                intPos = i;
            }
            if (dotPos == -3 && c == '.') {
                dotPos = i;
            }
            if (dotPos != -3 && c >= '0' && c <= '9') {
                fractionPos = i;
            }
            if (c >= 'a' && c <= 'z') {
                unitPos = i;
                break;
            }
            i++;
        }
        if (unitPos == -1 || ((dotPos >= fractionPos && fractionPos != -2) || fractionPos >= unitPos)) {
            return null;
        }
        boolean neg = false;
        try {
            float f = Float.parseFloat(value.substring(0, unitPos));
            if (f < 0.0f) {
                neg = true;
            }
            if (neg) {
                f = -f;
            }
            long bits = (long) ((8388608.0f * f) + 0.5f);
            if ((8388607 & bits) == 0) {
                radix = 0;
                shift = 23;
            } else if ((-8388608 & bits) == 0) {
                radix = 3;
                shift = 0;
            } else if ((-2147483648L & bits) == 0) {
                radix = 2;
                shift = 8;
            } else if ((-549755813888L & bits) == 0) {
                radix = 1;
                shift = 16;
            } else {
                radix = 0;
                shift = 23;
            }
            String unit = value.substring(unitPos);
            if (unit.equals("px")) {
                unitType = 0;
            } else {
                if (!unit.equals("dp")) {
                    if (!unit.equals("dip")) {
                        if (unit.equals(FullBackup.SHAREDPREFS_TREE_TOKEN)) {
                            unitType = 2;
                        } else if (unit.equals("pt")) {
                            unitType = 3;
                        } else if (unit.equals("in")) {
                            unitType = 4;
                        } else if (!unit.equals("mm")) {
                            return null;
                        } else {
                            unitType = 5;
                        }
                    }
                }
                unitType = 1;
            }
            int mantissa = (int) ((bits >> shift) & 16777215);
            if (neg) {
                mantissa = (-mantissa) & 16777215;
            }
            return Integer.valueOf((mantissa << 8) | (radix << 4) | (unitType << 0));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
