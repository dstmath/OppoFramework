package android.media;

import android.content.res.AssetManager.AssetInputStream;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.Pair;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;
import libcore.io.Streams;

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
public class ExifInterface {
    private static final Charset ASCII = null;
    private static final short BYTE_ALIGN_II = (short) 18761;
    private static final short BYTE_ALIGN_MM = (short) 19789;
    private static final boolean DEBUG = false;
    private static final byte[] EXIF_ASCII_PREFIX = null;
    private static final ExifTag[][] EXIF_TAGS = null;
    private static final byte[] IDENTIFIER_EXIF_APP1 = null;
    private static final int IFD_EXIF_HINT = 1;
    private static final ExifTag[] IFD_EXIF_TAGS = null;
    private static final int IFD_FORMAT_BYTE = 1;
    private static final int[] IFD_FORMAT_BYTES_PER_FORMAT = null;
    private static final int IFD_FORMAT_DOUBLE = 12;
    private static final String[] IFD_FORMAT_NAMES = null;
    private static final int IFD_FORMAT_SBYTE = 6;
    private static final int IFD_FORMAT_SINGLE = 11;
    private static final int IFD_FORMAT_SLONG = 9;
    private static final int IFD_FORMAT_SRATIONAL = 10;
    private static final int IFD_FORMAT_SSHORT = 8;
    private static final int IFD_FORMAT_STRING = 2;
    private static final int IFD_FORMAT_ULONG = 4;
    private static final int IFD_FORMAT_UNDEFINED = 7;
    private static final int IFD_FORMAT_URATIONAL = 5;
    private static final int IFD_FORMAT_USHORT = 3;
    private static final int IFD_GPS_HINT = 2;
    private static final ExifTag[] IFD_GPS_TAGS = null;
    private static final int IFD_INTEROPERABILITY_HINT = 3;
    private static final ExifTag[] IFD_INTEROPERABILITY_TAGS = null;
    private static final ExifTag[] IFD_POINTER_TAGS = null;
    private static final int[] IFD_POINTER_TAG_HINTS = null;
    private static final int IFD_THUMBNAIL_HINT = 4;
    private static final ExifTag[] IFD_THUMBNAIL_TAGS = null;
    private static final int IFD_TIFF_HINT = 0;
    private static final ExifTag[] IFD_TIFF_TAGS = null;
    private static final ExifTag JPEG_INTERCHANGE_FORMAT_LENGTH_TAG = null;
    private static final ExifTag JPEG_INTERCHANGE_FORMAT_TAG = null;
    private static final byte[] JPEG_SIGNATURE = null;
    private static final int JPEG_SIGNATURE_SIZE = 3;
    private static final byte MARKER = (byte) -1;
    private static final byte MARKER_APP1 = (byte) -31;
    private static final byte MARKER_COM = (byte) -2;
    private static final byte MARKER_EOI = (byte) -39;
    private static final byte MARKER_SOF0 = (byte) -64;
    private static final byte MARKER_SOF1 = (byte) -63;
    private static final byte MARKER_SOF10 = (byte) -54;
    private static final byte MARKER_SOF11 = (byte) -53;
    private static final byte MARKER_SOF13 = (byte) -51;
    private static final byte MARKER_SOF14 = (byte) -50;
    private static final byte MARKER_SOF15 = (byte) -49;
    private static final byte MARKER_SOF2 = (byte) -62;
    private static final byte MARKER_SOF3 = (byte) -61;
    private static final byte MARKER_SOF5 = (byte) -59;
    private static final byte MARKER_SOF6 = (byte) -58;
    private static final byte MARKER_SOF7 = (byte) -57;
    private static final byte MARKER_SOF9 = (byte) -55;
    private static final byte MARKER_SOI = (byte) -40;
    private static final byte MARKER_SOS = (byte) -38;
    public static final int ORIENTATION_FLIP_HORIZONTAL = 2;
    public static final int ORIENTATION_FLIP_VERTICAL = 4;
    public static final int ORIENTATION_NORMAL = 1;
    public static final int ORIENTATION_ROTATE_180 = 3;
    public static final int ORIENTATION_ROTATE_270 = 8;
    public static final int ORIENTATION_ROTATE_90 = 6;
    public static final int ORIENTATION_TRANSPOSE = 5;
    public static final int ORIENTATION_TRANSVERSE = 7;
    public static final int ORIENTATION_UNDEFINED = 0;
    private static final int READ_IMAGE_DIRECTORY_COUNT = 512;
    private static final String TAG = "ExifInterface";
    @Deprecated
    public static final String TAG_APERTURE = "FNumber";
    public static final String TAG_APERTURE_VALUE = "ApertureValue";
    public static final String TAG_ARTIST = "Artist";
    public static final String TAG_BITS_PER_SAMPLE = "BitsPerSample";
    public static final String TAG_BRIGHTNESS_VALUE = "BrightnessValue";
    public static final String TAG_CFA_PATTERN = "CFAPattern";
    public static final String TAG_COLOR_SPACE = "ColorSpace";
    public static final String TAG_COMPONENTS_CONFIGURATION = "ComponentsConfiguration";
    public static final String TAG_COMPRESSED_BITS_PER_PIXEL = "CompressedBitsPerPixel";
    public static final String TAG_COMPRESSION = "Compression";
    public static final String TAG_CONTRAST = "Contrast";
    public static final String TAG_COPYRIGHT = "Copyright";
    public static final String TAG_CUSTOM_RENDERED = "CustomRendered";
    public static final String TAG_DATETIME = "DateTime";
    public static final String TAG_DATETIME_DIGITIZED = "DateTimeDigitized";
    public static final String TAG_DATETIME_ORIGINAL = "DateTimeOriginal";
    public static final String TAG_DEVICE_SETTING_DESCRIPTION = "DeviceSettingDescription";
    public static final String TAG_DIGITAL_ZOOM_RATIO = "DigitalZoomRatio";
    private static final String TAG_EXIF_IFD_POINTER = "ExifIFDPointer";
    public static final String TAG_EXIF_VERSION = "ExifVersion";
    public static final String TAG_EXPOSURE_BIAS_VALUE = "ExposureBiasValue";
    public static final String TAG_EXPOSURE_INDEX = "ExposureIndex";
    public static final String TAG_EXPOSURE_MODE = "ExposureMode";
    public static final String TAG_EXPOSURE_PROGRAM = "ExposureProgram";
    public static final String TAG_EXPOSURE_TIME = "ExposureTime";
    public static final String TAG_FILE_SOURCE = "FileSource";
    public static final String TAG_FLASH = "Flash";
    public static final String TAG_FLASHPIX_VERSION = "FlashpixVersion";
    public static final String TAG_FLASH_ENERGY = "FlashEnergy";
    public static final String TAG_FOCAL_LENGTH = "FocalLength";
    public static final String TAG_FOCAL_LENGTH_IN_35MM_FILM = "FocalLengthIn35mmFilm";
    public static final String TAG_FOCAL_PLANE_RESOLUTION_UNIT = "FocalPlaneResolutionUnit";
    public static final String TAG_FOCAL_PLANE_X_RESOLUTION = "FocalPlaneXResolution";
    public static final String TAG_FOCAL_PLANE_Y_RESOLUTION = "FocalPlaneYResolution";
    public static final String TAG_F_NUMBER = "FNumber";
    public static final String TAG_GAIN_CONTROL = "GainControl";
    public static final String TAG_GPS_ALTITUDE = "GPSAltitude";
    public static final String TAG_GPS_ALTITUDE_REF = "GPSAltitudeRef";
    public static final String TAG_GPS_AREA_INFORMATION = "GPSAreaInformation";
    public static final String TAG_GPS_DATESTAMP = "GPSDateStamp";
    public static final String TAG_GPS_DEST_BEARING = "GPSDestBearing";
    public static final String TAG_GPS_DEST_BEARING_REF = "GPSDestBearingRef";
    public static final String TAG_GPS_DEST_DISTANCE = "GPSDestDistance";
    public static final String TAG_GPS_DEST_DISTANCE_REF = "GPSDestDistanceRef";
    public static final String TAG_GPS_DEST_LATITUDE = "GPSDestLatitude";
    public static final String TAG_GPS_DEST_LATITUDE_REF = "GPSDestLatitudeRef";
    public static final String TAG_GPS_DEST_LONGITUDE = "GPSDestLongitude";
    public static final String TAG_GPS_DEST_LONGITUDE_REF = "GPSDestLongitudeRef";
    public static final String TAG_GPS_DIFFERENTIAL = "GPSDifferential";
    public static final String TAG_GPS_DOP = "GPSDOP";
    public static final String TAG_GPS_IMG_DIRECTION = "GPSImgDirection";
    public static final String TAG_GPS_IMG_DIRECTION_REF = "GPSImgDirectionRef";
    private static final String TAG_GPS_INFO_IFD_POINTER = "GPSInfoIFDPointer";
    public static final String TAG_GPS_LATITUDE = "GPSLatitude";
    public static final String TAG_GPS_LATITUDE_REF = "GPSLatitudeRef";
    public static final String TAG_GPS_LONGITUDE = "GPSLongitude";
    public static final String TAG_GPS_LONGITUDE_REF = "GPSLongitudeRef";
    public static final String TAG_GPS_MAP_DATUM = "GPSMapDatum";
    public static final String TAG_GPS_MEASURE_MODE = "GPSMeasureMode";
    public static final String TAG_GPS_PROCESSING_METHOD = "GPSProcessingMethod";
    public static final String TAG_GPS_SATELLITES = "GPSSatellites";
    public static final String TAG_GPS_SPEED = "GPSSpeed";
    public static final String TAG_GPS_SPEED_REF = "GPSSpeedRef";
    public static final String TAG_GPS_STATUS = "GPSStatus";
    public static final String TAG_GPS_TIMESTAMP = "GPSTimeStamp";
    public static final String TAG_GPS_TRACK = "GPSTrack";
    public static final String TAG_GPS_TRACK_REF = "GPSTrackRef";
    public static final String TAG_GPS_VERSION_ID = "GPSVersionID";
    private static final String TAG_HAS_THUMBNAIL = "HasThumbnail";
    public static final String TAG_IMAGE_DESCRIPTION = "ImageDescription";
    public static final String TAG_IMAGE_LENGTH = "ImageLength";
    public static final String TAG_IMAGE_UNIQUE_ID = "ImageUniqueID";
    public static final String TAG_IMAGE_WIDTH = "ImageWidth";
    private static final String TAG_INTEROPERABILITY_IFD_POINTER = "InteroperabilityIFDPointer";
    public static final String TAG_INTEROPERABILITY_INDEX = "InteroperabilityIndex";
    @Deprecated
    public static final String TAG_ISO = "ISOSpeedRatings";
    public static final String TAG_ISO_SPEED_RATINGS = "ISOSpeedRatings";
    public static final String TAG_JPEG_INTERCHANGE_FORMAT = "JPEGInterchangeFormat";
    public static final String TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = "JPEGInterchangeFormatLength";
    public static final String TAG_LIGHT_SOURCE = "LightSource";
    public static final String TAG_MAKE = "Make";
    public static final String TAG_MAKER_NOTE = "MakerNote";
    public static final String TAG_MAX_APERTURE_VALUE = "MaxApertureValue";
    public static final String TAG_METERING_MODE = "MeteringMode";
    public static final String TAG_MODEL = "Model";
    public static final String TAG_MTK_CAMERA_REFOCUS = "MTKCameraRefocus";
    public static final String TAG_MTK_CONSHOT_FOCUS_HIGH = "MTKConshotFocusHigh";
    public static final String TAG_MTK_CONSHOT_FOCUS_LOW = "MTKConshotFocusLow";
    public static final String TAG_MTK_CONSHOT_GROUP_ID = "MTKConshotGroupID";
    public static final String TAG_MTK_CONSHOT_PIC_INDEX = "MTKConshotPicIndex";
    public static final String TAG_OECF = "OECF";
    public static final String TAG_ORIENTATION = "Orientation";
    public static final String TAG_PHOTOMETRIC_INTERPRETATION = "PhotometricInterpretation";
    public static final String TAG_PIXEL_X_DIMENSION = "PixelXDimension";
    public static final String TAG_PIXEL_Y_DIMENSION = "PixelYDimension";
    public static final String TAG_PLANAR_CONFIGURATION = "PlanarConfiguration";
    public static final String TAG_PRIMARY_CHROMATICITIES = "PrimaryChromaticities";
    public static final String TAG_REFERENCE_BLACK_WHITE = "ReferenceBlackWhite";
    public static final String TAG_RELATED_SOUND_FILE = "RelatedSoundFile";
    public static final String TAG_RESOLUTION_UNIT = "ResolutionUnit";
    public static final String TAG_ROWS_PER_STRIP = "RowsPerStrip";
    public static final String TAG_SAMPLES_PER_PIXEL = "SamplesPerPixel";
    public static final String TAG_SATURATION = "Saturation";
    public static final String TAG_SCENE_CAPTURE_TYPE = "SceneCaptureType";
    public static final String TAG_SCENE_TYPE = "SceneType";
    public static final String TAG_SENSING_METHOD = "SensingMethod";
    public static final String TAG_SHARPNESS = "Sharpness";
    public static final String TAG_SHUTTER_SPEED_VALUE = "ShutterSpeedValue";
    public static final String TAG_SOFTWARE = "Software";
    public static final String TAG_SPATIAL_FREQUENCY_RESPONSE = "SpatialFrequencyResponse";
    public static final String TAG_SPECTRAL_SENSITIVITY = "SpectralSensitivity";
    public static final String TAG_STRIP_BYTE_COUNTS = "StripByteCounts";
    public static final String TAG_STRIP_OFFSETS = "StripOffsets";
    public static final String TAG_SUBJECT_AREA = "SubjectArea";
    public static final String TAG_SUBJECT_DISTANCE = "SubjectDistance";
    public static final String TAG_SUBJECT_DISTANCE_RANGE = "SubjectDistanceRange";
    public static final String TAG_SUBJECT_LOCATION = "SubjectLocation";
    public static final String TAG_SUBSEC_TIME = "SubSecTime";
    public static final String TAG_SUBSEC_TIME_DIG = "SubSecTimeDigitized";
    public static final String TAG_SUBSEC_TIME_DIGITIZED = "SubSecTimeDigitized";
    public static final String TAG_SUBSEC_TIME_ORIG = "SubSecTimeOriginal";
    public static final String TAG_SUBSEC_TIME_ORIGINAL = "SubSecTimeOriginal";
    private static final String TAG_THUMBNAIL_DATA = "ThumbnailData";
    public static final String TAG_THUMBNAIL_IMAGE_LENGTH = "ThumbnailImageLength";
    public static final String TAG_THUMBNAIL_IMAGE_WIDTH = "ThumbnailImageWidth";
    private static final String TAG_THUMBNAIL_LENGTH = "ThumbnailLength";
    private static final String TAG_THUMBNAIL_OFFSET = "ThumbnailOffset";
    public static final String TAG_TRANSFER_FUNCTION = "TransferFunction";
    public static final String TAG_USER_COMMENT = "UserComment";
    public static final String TAG_WHITE_BALANCE = "WhiteBalance";
    public static final String TAG_WHITE_POINT = "WhitePoint";
    public static final String TAG_X_RESOLUTION = "XResolution";
    public static final String TAG_Y_CB_CR_COEFFICIENTS = "YCbCrCoefficients";
    public static final String TAG_Y_CB_CR_POSITIONING = "YCbCrPositioning";
    public static final String TAG_Y_CB_CR_SUB_SAMPLING = "YCbCrSubSampling";
    public static final String TAG_Y_RESOLUTION = "YResolution";
    public static final int WHITEBALANCE_AUTO = 0;
    public static final int WHITEBALANCE_MANUAL = 1;
    private static final HashMap[] sExifTagMapsForReading = null;
    private static final HashMap[] sExifTagMapsForWriting = null;
    private static SimpleDateFormat sFormatter;
    private static final Pattern sGpsTimestampPattern = null;
    private static final Object sLock = null;
    private static final Pattern sNonZeroTimePattern = null;
    private static final HashSet<String> sTagSetForCompatibility = null;
    private final AssetInputStream mAssetInputStream;
    private final HashMap[] mAttributes;
    private ByteOrder mExifByteOrder;
    private final String mFilename;
    private boolean mHasThumbnail;
    private InputStream mInputStream;
    private final boolean mIsInputStream;
    private boolean mIsProgressiveModePic;
    private boolean mIsRaw;
    private boolean mIsSupportedFile;
    private int mReadImageFileDirectoryCount;
    private final FileDescriptor mSeekableFileDescriptor;
    private byte[] mThumbnailBytes;
    private int mThumbnailLength;
    private int mThumbnailOffset;

    private static class ByteOrderAwarenessDataInputStream extends ByteArrayInputStream {
        private static final ByteOrder BIG_ENDIAN = null;
        private static final ByteOrder LITTLE_ENDIAN = null;
        private ByteOrder mByteOrder;
        private final long mLength;
        private long mPosition;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.ExifInterface.ByteOrderAwarenessDataInputStream.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.ExifInterface.ByteOrderAwarenessDataInputStream.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.ExifInterface.ByteOrderAwarenessDataInputStream.<clinit>():void");
        }

        public ByteOrderAwarenessDataInputStream(byte[] bytes) {
            super(bytes);
            this.mByteOrder = ByteOrder.BIG_ENDIAN;
            this.mLength = (long) bytes.length;
            this.mPosition = 0;
        }

        public void setByteOrder(ByteOrder byteOrder) {
            this.mByteOrder = byteOrder;
        }

        public void seek(long byteCount) throws IOException {
            this.mPosition = 0;
            reset();
            if (skip(byteCount) != byteCount) {
                throw new IOException("Couldn't seek up to the byteCount");
            }
        }

        public long peek() {
            return this.mPosition;
        }

        public void readFully(byte[] buffer) throws IOException {
            this.mPosition += (long) buffer.length;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            } else if (super.read(buffer, 0, buffer.length) != buffer.length) {
                throw new IOException("Couldn't read up to the length of buffer");
            }
        }

        public byte readByte() throws IOException {
            this.mPosition++;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch = super.read();
            if (ch >= 0) {
                return (byte) ch;
            }
            throw new EOFException();
        }

        public short readShort() throws IOException {
            this.mPosition += 2;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch1 = super.read();
            int ch2 = super.read();
            if ((ch1 | ch2) < 0) {
                throw new EOFException();
            } else if (this.mByteOrder == LITTLE_ENDIAN) {
                return (short) ((ch2 << 8) + ch1);
            } else {
                if (this.mByteOrder == BIG_ENDIAN) {
                    return (short) ((ch1 << 8) + ch2);
                }
                throw new IOException("Invalid byte order: " + this.mByteOrder);
            }
        }

        public int readInt() throws IOException {
            this.mPosition += 4;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch1 = super.read();
            int ch2 = super.read();
            int ch3 = super.read();
            int ch4 = super.read();
            if ((((ch1 | ch2) | ch3) | ch4) < 0) {
                throw new EOFException();
            } else if (this.mByteOrder == LITTLE_ENDIAN) {
                return (((ch4 << 24) + (ch3 << 16)) + (ch2 << 8)) + ch1;
            } else {
                if (this.mByteOrder == BIG_ENDIAN) {
                    return (((ch1 << 24) + (ch2 << 16)) + (ch3 << 8)) + ch4;
                }
                throw new IOException("Invalid byte order: " + this.mByteOrder);
            }
        }

        public long skip(long byteCount) {
            long skipped = super.skip(Math.min(byteCount, this.mLength - this.mPosition));
            this.mPosition += skipped;
            return skipped;
        }

        public int readUnsignedShort() throws IOException {
            this.mPosition += 2;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch1 = super.read();
            int ch2 = super.read();
            if ((ch1 | ch2) < 0) {
                throw new EOFException();
            } else if (this.mByteOrder == LITTLE_ENDIAN) {
                return (ch2 << 8) + ch1;
            } else {
                if (this.mByteOrder == BIG_ENDIAN) {
                    return (ch1 << 8) + ch2;
                }
                throw new IOException("Invalid byte order: " + this.mByteOrder);
            }
        }

        public long readUnsignedInt() throws IOException {
            return ((long) readInt()) & KeymasterArguments.UINT32_MAX_VALUE;
        }

        public long readLong() throws IOException {
            this.mPosition += 8;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch1 = super.read();
            int ch2 = super.read();
            int ch3 = super.read();
            int ch4 = super.read();
            int ch5 = super.read();
            int ch6 = super.read();
            int ch7 = super.read();
            int ch8 = super.read();
            if ((((((((ch1 | ch2) | ch3) | ch4) | ch5) | ch6) | ch7) | ch8) < 0) {
                throw new EOFException();
            } else if (this.mByteOrder == LITTLE_ENDIAN) {
                return (((((((((long) ch8) << 56) + (((long) ch7) << 48)) + (((long) ch6) << 40)) + (((long) ch5) << 32)) + (((long) ch4) << 24)) + (((long) ch3) << 16)) + (((long) ch2) << 8)) + ((long) ch1);
            } else {
                if (this.mByteOrder == BIG_ENDIAN) {
                    return (((((((((long) ch1) << 56) + (((long) ch2) << 48)) + (((long) ch3) << 40)) + (((long) ch4) << 32)) + (((long) ch5) << 24)) + (((long) ch6) << 16)) + (((long) ch7) << 8)) + ((long) ch8);
                }
                throw new IOException("Invalid byte order: " + this.mByteOrder);
            }
        }

        public float readFloat() throws IOException {
            return Float.intBitsToFloat(readInt());
        }

        public double readDouble() throws IOException {
            return Double.longBitsToDouble(readLong());
        }
    }

    private static class ByteOrderAwarenessDataOutputStream extends FilterOutputStream {
        private ByteOrder mByteOrder;
        private final OutputStream mOutputStream;

        public ByteOrderAwarenessDataOutputStream(OutputStream out, ByteOrder byteOrder) {
            super(out);
            this.mOutputStream = out;
            this.mByteOrder = byteOrder;
        }

        public void setByteOrder(ByteOrder byteOrder) {
            this.mByteOrder = byteOrder;
        }

        public void write(byte[] bytes) throws IOException {
            this.mOutputStream.write(bytes);
        }

        public void write(byte[] bytes, int offset, int length) throws IOException {
            this.mOutputStream.write(bytes, offset, length);
        }

        public void writeByte(int val) throws IOException {
            this.mOutputStream.write(val);
        }

        public void writeShort(short val) throws IOException {
            if (this.mByteOrder == ByteOrder.LITTLE_ENDIAN) {
                this.mOutputStream.write((val >>> 0) & 255);
                this.mOutputStream.write((val >>> 8) & 255);
            } else if (this.mByteOrder == ByteOrder.BIG_ENDIAN) {
                this.mOutputStream.write((val >>> 8) & 255);
                this.mOutputStream.write((val >>> 0) & 255);
            }
        }

        public void writeInt(int val) throws IOException {
            if (this.mByteOrder == ByteOrder.LITTLE_ENDIAN) {
                this.mOutputStream.write((val >>> 0) & 255);
                this.mOutputStream.write((val >>> 8) & 255);
                this.mOutputStream.write((val >>> 16) & 255);
                this.mOutputStream.write((val >>> 24) & 255);
            } else if (this.mByteOrder == ByteOrder.BIG_ENDIAN) {
                this.mOutputStream.write((val >>> 24) & 255);
                this.mOutputStream.write((val >>> 16) & 255);
                this.mOutputStream.write((val >>> 8) & 255);
                this.mOutputStream.write((val >>> 0) & 255);
            }
        }

        public void writeUnsignedShort(int val) throws IOException {
            writeShort((short) val);
        }

        public void writeUnsignedInt(long val) throws IOException {
            writeInt((int) val);
        }
    }

    private static class ExifAttribute {
        public final byte[] bytes;
        public final int format;
        public final int numberOfComponents;

        /* synthetic */ ExifAttribute(int format, int numberOfComponents, byte[] bytes, ExifAttribute exifAttribute) {
            this(format, numberOfComponents, bytes);
        }

        private ExifAttribute(int format, int numberOfComponents, byte[] bytes) {
            this.format = format;
            this.numberOfComponents = numberOfComponents;
            this.bytes = bytes;
        }

        public static ExifAttribute createUShort(int[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[3] * values.length)]);
            buffer.order(byteOrder);
            for (int value : values) {
                buffer.putShort((short) value);
            }
            return new ExifAttribute(3, values.length, buffer.array());
        }

        public static ExifAttribute createUShort(int value, ByteOrder byteOrder) {
            int[] iArr = new int[1];
            iArr[0] = value;
            return createUShort(iArr, byteOrder);
        }

        public static ExifAttribute createULong(long[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[4] * values.length)]);
            buffer.order(byteOrder);
            for (long value : values) {
                buffer.putInt((int) value);
            }
            return new ExifAttribute(4, values.length, buffer.array());
        }

        public static ExifAttribute createULong(long value, ByteOrder byteOrder) {
            long[] jArr = new long[1];
            jArr[0] = value;
            return createULong(jArr, byteOrder);
        }

        public static ExifAttribute createSLong(int[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[9] * values.length)]);
            buffer.order(byteOrder);
            for (int value : values) {
                buffer.putInt(value);
            }
            return new ExifAttribute(9, values.length, buffer.array());
        }

        public static ExifAttribute createSLong(int value, ByteOrder byteOrder) {
            int[] iArr = new int[1];
            iArr[0] = value;
            return createSLong(iArr, byteOrder);
        }

        public static ExifAttribute createByte(String value) {
            if (value.length() != 1 || value.charAt(0) < '0' || value.charAt(0) > '1') {
                byte[] ascii = value.getBytes(ExifInterface.ASCII);
                return new ExifAttribute(1, ascii.length, ascii);
            }
            byte[] bytes = new byte[1];
            bytes[0] = (byte) (value.charAt(0) - 48);
            return new ExifAttribute(1, bytes.length, bytes);
        }

        public static ExifAttribute createString(String value) {
            byte[] ascii = (value + 0).getBytes(ExifInterface.ASCII);
            return new ExifAttribute(2, ascii.length, ascii);
        }

        public static ExifAttribute createURational(Rational[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[5] * values.length)]);
            buffer.order(byteOrder);
            for (Rational value : values) {
                buffer.putInt((int) value.numerator);
                buffer.putInt((int) value.denominator);
            }
            return new ExifAttribute(5, values.length, buffer.array());
        }

        public static ExifAttribute createURational(Rational value, ByteOrder byteOrder) {
            Rational[] rationalArr = new Rational[1];
            rationalArr[0] = value;
            return createURational(rationalArr, byteOrder);
        }

        public static ExifAttribute createSRational(Rational[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[10] * values.length)]);
            buffer.order(byteOrder);
            for (Rational value : values) {
                buffer.putInt((int) value.numerator);
                buffer.putInt((int) value.denominator);
            }
            return new ExifAttribute(10, values.length, buffer.array());
        }

        public static ExifAttribute createSRational(Rational value, ByteOrder byteOrder) {
            Rational[] rationalArr = new Rational[1];
            rationalArr[0] = value;
            return createSRational(rationalArr, byteOrder);
        }

        public static ExifAttribute createDouble(double[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[12] * values.length)]);
            buffer.order(byteOrder);
            for (double value : values) {
                buffer.putDouble(value);
            }
            return new ExifAttribute(12, values.length, buffer.array());
        }

        public static ExifAttribute createDouble(double value, ByteOrder byteOrder) {
            double[] dArr = new double[1];
            dArr[0] = value;
            return createDouble(dArr, byteOrder);
        }

        public String toString() {
            return "(" + ExifInterface.IFD_FORMAT_NAMES[this.format] + ", data length:" + this.bytes.length + ")";
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x0086 A:{Catch:{ IOException -> 0x00b7 }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Object getValue(ByteOrder byteOrder) {
            try {
                ByteOrderAwarenessDataInputStream inputStream = new ByteOrderAwarenessDataInputStream(this.bytes);
                inputStream.setByteOrder(byteOrder);
                int i;
                int[] values;
                Rational[] values2;
                double[] values3;
                switch (this.format) {
                    case 1:
                    case 6:
                        if (this.bytes.length != 1 || this.bytes[0] < (byte) 0 || this.bytes[0] > (byte) 1) {
                            return new String(this.bytes, ExifInterface.ASCII);
                        }
                        char[] cArr = new char[1];
                        cArr[0] = (char) (this.bytes[0] + 48);
                        return new String(cArr);
                    case 2:
                    case 7:
                        int index = 0;
                        if (this.numberOfComponents >= ExifInterface.EXIF_ASCII_PREFIX.length) {
                            boolean same = true;
                            i = 0;
                            while (i < ExifInterface.EXIF_ASCII_PREFIX.length) {
                                if (this.bytes[i] != ExifInterface.EXIF_ASCII_PREFIX[i]) {
                                    same = false;
                                    if (same) {
                                        index = ExifInterface.EXIF_ASCII_PREFIX.length;
                                    }
                                } else {
                                    i++;
                                }
                            }
                            if (same) {
                            }
                        }
                        StringBuilder stringBuilder = new StringBuilder();
                        for (index = 
/*
Method generation error in method: android.media.ExifInterface.ExifAttribute.getValue(java.nio.ByteOrder):java.lang.Object, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r11_2 'index' int) = (r11_0 'index' int), (r11_0 'index' int), (r11_1 'index' int) binds: {(r11_0 'index' int)=B:16:0x006a, (r11_0 'index' int)=B:23:0x0084, (r11_1 'index' int)=B:24:0x0086} in method: android.media.ExifInterface.ExifAttribute.getValue(java.nio.ByteOrder):java.lang.Object, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:183)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:265)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:278)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:234)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:220)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 28 more

*/

        public double getDoubleValue(ByteOrder byteOrder) {
            Object value = getValue(byteOrder);
            if (value == null) {
                throw new NumberFormatException("NULL can't be converted to a double value");
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            } else {
                if (value instanceof long[]) {
                    long[] array = (long[]) value;
                    if (array.length == 1) {
                        return (double) array[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else if (value instanceof int[]) {
                    int[] array2 = (int[]) value;
                    if (array2.length == 1) {
                        return (double) array2[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else if (value instanceof double[]) {
                    double[] array3 = (double[]) value;
                    if (array3.length == 1) {
                        return array3[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else if (value instanceof Rational[]) {
                    Rational[] array4 = (Rational[]) value;
                    if (array4.length == 1) {
                        return array4[0].calculate();
                    }
                    throw new NumberFormatException("There are more than one component");
                } else {
                    throw new NumberFormatException("Couldn't find a double value");
                }
            }
        }

        public int getIntValue(ByteOrder byteOrder) {
            Object value = getValue(byteOrder);
            if (value == null) {
                throw new NumberFormatException("NULL can't be converted to a integer value");
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else {
                if (value instanceof long[]) {
                    long[] array = (long[]) value;
                    if (array.length == 1) {
                        return (int) array[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else if (value instanceof int[]) {
                    int[] array2 = (int[]) value;
                    if (array2.length == 1) {
                        return array2[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else {
                    throw new NumberFormatException("Couldn't find a integer value");
                }
            }
        }

        public String getStringValue(ByteOrder byteOrder) {
            Object value = getValue(byteOrder);
            if (value == null) {
                return null;
            }
            if (value instanceof String) {
                return (String) value;
            }
            StringBuilder stringBuilder = new StringBuilder();
            int i;
            if (value instanceof long[]) {
                long[] array = (long[]) value;
                for (i = 0; i < array.length; i++) {
                    stringBuilder.append(array[i]);
                    if (i + 1 != array.length) {
                        stringBuilder.append(",");
                    }
                }
                return stringBuilder.toString();
            } else if (value instanceof int[]) {
                int[] array2 = (int[]) value;
                for (i = 0; i < array2.length; i++) {
                    stringBuilder.append(array2[i]);
                    if (i + 1 != array2.length) {
                        stringBuilder.append(",");
                    }
                }
                return stringBuilder.toString();
            } else if (value instanceof double[]) {
                double[] array3 = (double[]) value;
                for (i = 0; i < array3.length; i++) {
                    stringBuilder.append(array3[i]);
                    if (i + 1 != array3.length) {
                        stringBuilder.append(",");
                    }
                }
                return stringBuilder.toString();
            } else if (!(value instanceof Rational[])) {
                return null;
            } else {
                Rational[] array4 = (Rational[]) value;
                for (i = 0; i < array4.length; i++) {
                    stringBuilder.append(array4[i].numerator);
                    stringBuilder.append('/');
                    stringBuilder.append(array4[i].denominator);
                    if (i + 1 != array4.length) {
                        stringBuilder.append(",");
                    }
                }
                return stringBuilder.toString();
            }
        }

        public int size() {
            return ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[this.format] * this.numberOfComponents;
        }
    }

    private static class ExifTag {
        public final String name;
        public final int number;
        public final int primaryFormat;
        public final int secondaryFormat;

        /* synthetic */ ExifTag(String name, int number, int primaryFormat, int secondaryFormat, ExifTag exifTag) {
            this(name, number, primaryFormat, secondaryFormat);
        }

        private ExifTag(String name, int number, int format) {
            this.name = name;
            this.number = number;
            this.primaryFormat = format;
            this.secondaryFormat = -1;
        }

        private ExifTag(String name, int number, int primaryFormat, int secondaryFormat) {
            this.name = name;
            this.number = number;
            this.primaryFormat = primaryFormat;
            this.secondaryFormat = secondaryFormat;
        }
    }

    private static class Rational {
        public final long denominator;
        public final long numerator;

        /* synthetic */ Rational(long numerator, long denominator, Rational rational) {
            this(numerator, denominator);
        }

        private Rational(long numerator, long denominator) {
            if (denominator == 0) {
                this.numerator = 0;
                this.denominator = 1;
                return;
            }
            this.numerator = numerator;
            this.denominator = denominator;
        }

        public String toString() {
            return this.numerator + "/" + this.denominator;
        }

        public double calculate() {
            return ((double) this.numerator) / ((double) this.denominator);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.ExifInterface.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.ExifInterface.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.ExifInterface.<clinit>():void");
    }

    private static native HashMap nativeGetRawAttributesFromAsset(long j);

    private static native HashMap nativeGetRawAttributesFromFileDescriptor(FileDescriptor fileDescriptor);

    private static native HashMap nativeGetRawAttributesFromInputStream(InputStream inputStream);

    private static native byte[] nativeGetThumbnailFromAsset(long j, int i, int i2);

    private static native void nativeInitRaw();

    public ExifInterface(String filename) throws IOException {
        Throwable th;
        this.mReadImageFileDirectoryCount = 0;
        this.mIsProgressiveModePic = false;
        this.mAttributes = new HashMap[EXIF_TAGS.length];
        this.mExifByteOrder = ByteOrder.BIG_ENDIAN;
        if (filename == null) {
            throw new IllegalArgumentException("filename cannot be null");
        }
        AutoCloseable autoCloseable = null;
        this.mAssetInputStream = null;
        this.mFilename = filename;
        this.mIsInputStream = false;
        this.mReadImageFileDirectoryCount = 0;
        try {
            FileInputStream in = new FileInputStream(filename);
            try {
                if (isSeekableFD(in.getFD())) {
                    this.mSeekableFileDescriptor = in.getFD();
                } else {
                    this.mSeekableFileDescriptor = null;
                }
                loadAttributes(in);
                IoUtils.closeQuietly(in);
            } catch (Throwable th2) {
                th = th2;
                autoCloseable = in;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(autoCloseable);
            throw th;
        }
    }

    public ExifInterface(FileDescriptor fileDescriptor) throws IOException {
        Throwable th;
        this.mReadImageFileDirectoryCount = 0;
        this.mIsProgressiveModePic = false;
        this.mAttributes = new HashMap[EXIF_TAGS.length];
        this.mExifByteOrder = ByteOrder.BIG_ENDIAN;
        if (fileDescriptor == null) {
            throw new IllegalArgumentException("fileDescriptor cannot be null");
        }
        this.mAssetInputStream = null;
        this.mFilename = null;
        if (isSeekableFD(fileDescriptor)) {
            this.mSeekableFileDescriptor = fileDescriptor;
            try {
                fileDescriptor = Os.dup(fileDescriptor);
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        }
        this.mSeekableFileDescriptor = null;
        this.mIsInputStream = false;
        FileInputStream in = null;
        this.mReadImageFileDirectoryCount = 0;
        try {
            FileInputStream in2 = new FileInputStream(fileDescriptor);
            try {
                loadAttributes(in2);
                IoUtils.closeQuietly(in2);
            } catch (Throwable th2) {
                th = th2;
                in = in2;
                IoUtils.closeQuietly(in);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(in);
            throw th;
        }
    }

    public ExifInterface(InputStream inputStream) throws IOException {
        this.mReadImageFileDirectoryCount = 0;
        this.mIsProgressiveModePic = false;
        this.mAttributes = new HashMap[EXIF_TAGS.length];
        this.mExifByteOrder = ByteOrder.BIG_ENDIAN;
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream cannot be null");
        }
        this.mFilename = null;
        if (inputStream instanceof AssetInputStream) {
            this.mAssetInputStream = (AssetInputStream) inputStream;
            this.mSeekableFileDescriptor = null;
        } else if ((inputStream instanceof FileInputStream) && isSeekableFD(((FileInputStream) inputStream).getFD())) {
            this.mAssetInputStream = null;
            this.mSeekableFileDescriptor = ((FileInputStream) inputStream).getFD();
        } else {
            this.mAssetInputStream = null;
            this.mSeekableFileDescriptor = null;
        }
        this.mIsInputStream = true;
        this.mReadImageFileDirectoryCount = 0;
        loadAttributes(inputStream);
    }

    private ExifAttribute getExifAttribute(String tag) {
        for (int i = 0; i < EXIF_TAGS.length; i++) {
            Object value = this.mAttributes[i].get(tag);
            if (value != null) {
                return (ExifAttribute) value;
            }
        }
        return null;
    }

    public String getAttribute(String tag) {
        ExifAttribute attribute = getExifAttribute(tag);
        if (attribute == null) {
            return null;
        }
        if (!sTagSetForCompatibility.contains(tag)) {
            return attribute.getStringValue(this.mExifByteOrder);
        }
        if (!tag.equals(TAG_GPS_TIMESTAMP)) {
            try {
                return Double.toString(attribute.getDoubleValue(this.mExifByteOrder));
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (attribute.format != 5 && attribute.format != 10) {
            return null;
        } else {
            Rational[] array = (Rational[]) attribute.getValue(this.mExifByteOrder);
            if (array.length != 3) {
                return null;
            }
            Object[] objArr = new Object[3];
            objArr[0] = Integer.valueOf((int) (((float) array[0].numerator) / ((float) array[0].denominator)));
            objArr[1] = Integer.valueOf((int) (((float) array[1].numerator) / ((float) array[1].denominator)));
            objArr[2] = Integer.valueOf((int) (((float) array[2].numerator) / ((float) array[2].denominator)));
            return String.format("%02d:%02d:%02d", objArr);
        }
    }

    public int getAttributeInt(String tag, int defaultValue) {
        ExifAttribute exifAttribute = getExifAttribute(tag);
        if (exifAttribute == null) {
            return defaultValue;
        }
        try {
            return exifAttribute.getIntValue(this.mExifByteOrder);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getAttributeDouble(String tag, double defaultValue) {
        ExifAttribute exifAttribute = getExifAttribute(tag);
        if (exifAttribute == null) {
            return defaultValue;
        }
        try {
            return exifAttribute.getDoubleValue(this.mExifByteOrder);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void setAttribute(String tag, String value) {
        if (value != null && sTagSetForCompatibility.contains(tag)) {
            if (tag.equals(TAG_GPS_TIMESTAMP)) {
                Matcher m = sGpsTimestampPattern.matcher(value);
                if (m.find()) {
                    value = Integer.parseInt(m.group(1)) + "/1," + Integer.parseInt(m.group(2)) + "/1," + Integer.parseInt(m.group(3)) + "/1";
                } else {
                    Log.w(TAG, "Invalid value for " + tag + " : " + value);
                    return;
                }
            }
            try {
                value = ((long) (10000.0d * Double.parseDouble(value))) + "/10000";
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid value for " + tag + " : " + value);
                return;
            }
        }
        for (int i = 0; i < EXIF_TAGS.length; i++) {
            if (i != 4 || this.mHasThumbnail) {
                ExifTag obj = sExifTagMapsForWriting[i].get(tag);
                if (obj != null) {
                    if (value != null) {
                        int dataFormat;
                        ExifTag exifTag = obj;
                        Pair<Integer, Integer> guess = guessDataFormat(value);
                        if (exifTag.primaryFormat == ((Integer) guess.first).intValue() || exifTag.primaryFormat == ((Integer) guess.second).intValue()) {
                            dataFormat = exifTag.primaryFormat;
                        } else if (exifTag.secondaryFormat != -1 && (exifTag.secondaryFormat == ((Integer) guess.first).intValue() || exifTag.secondaryFormat == ((Integer) guess.second).intValue())) {
                            dataFormat = exifTag.secondaryFormat;
                        } else if (exifTag.primaryFormat == 1 || exifTag.primaryFormat == 7 || exifTag.primaryFormat == 2) {
                            dataFormat = exifTag.primaryFormat;
                        } else {
                            Log.w(TAG, "Given tag (" + tag + ") value didn't match with one of expected " + "formats: " + IFD_FORMAT_NAMES[exifTag.primaryFormat] + (exifTag.secondaryFormat == -1 ? "" : ", " + IFD_FORMAT_NAMES[exifTag.secondaryFormat]) + " (guess: " + IFD_FORMAT_NAMES[((Integer) guess.first).intValue()] + (((Integer) guess.second).intValue() == -1 ? "" : ", " + IFD_FORMAT_NAMES[((Integer) guess.second).intValue()]) + ")");
                        }
                        String[] values;
                        int[] intArray;
                        int j;
                        Rational[] rationalArray;
                        String[] numbers;
                        switch (dataFormat) {
                            case 1:
                                this.mAttributes[i].put(tag, ExifAttribute.createByte(value));
                                break;
                            case 2:
                            case 7:
                                this.mAttributes[i].put(tag, ExifAttribute.createString(value));
                                break;
                            case 3:
                                values = value.split(",");
                                intArray = new int[values.length];
                                for (j = 0; j < values.length; j++) {
                                    intArray[j] = Integer.parseInt(values[j]);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createUShort(intArray, this.mExifByteOrder));
                                break;
                            case 4:
                                values = value.split(",");
                                long[] longArray = new long[values.length];
                                for (j = 0; j < values.length; j++) {
                                    longArray[j] = Long.parseLong(values[j]);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createULong(longArray, this.mExifByteOrder));
                                break;
                            case 5:
                                values = value.split(",");
                                rationalArray = new Rational[values.length];
                                for (j = 0; j < values.length; j++) {
                                    numbers = values[j].split("/");
                                    rationalArray[j] = new Rational(Long.parseLong(numbers[0]), Long.parseLong(numbers[1]), null);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createURational(rationalArray, this.mExifByteOrder));
                                break;
                            case 9:
                                values = value.split(",");
                                intArray = new int[values.length];
                                for (j = 0; j < values.length; j++) {
                                    intArray[j] = Integer.parseInt(values[j]);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createSLong(intArray, this.mExifByteOrder));
                                break;
                            case 10:
                                values = value.split(",");
                                rationalArray = new Rational[values.length];
                                for (j = 0; j < values.length; j++) {
                                    numbers = values[j].split("/");
                                    rationalArray[j] = new Rational(Long.parseLong(numbers[0]), Long.parseLong(numbers[1]), null);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createSRational(rationalArray, this.mExifByteOrder));
                                break;
                            case 12:
                                values = value.split(",");
                                double[] doubleArray = new double[values.length];
                                for (j = 0; j < values.length; j++) {
                                    doubleArray[j] = Double.parseDouble(values[j]);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createDouble(doubleArray, this.mExifByteOrder));
                                break;
                            default:
                                Log.w(TAG, "Data format isn't one of expected formats: " + dataFormat);
                                break;
                        }
                    }
                    this.mAttributes[i].remove(tag);
                }
            }
        }
    }

    private boolean updateAttribute(String tag, ExifAttribute value) {
        boolean updated = false;
        for (int i = 0; i < EXIF_TAGS.length; i++) {
            if (this.mAttributes[i].containsKey(tag)) {
                this.mAttributes[i].put(tag, value);
                updated = true;
            }
        }
        return updated;
    }

    private void removeAttribute(String tag) {
        for (int i = 0; i < EXIF_TAGS.length; i++) {
            this.mAttributes[i].remove(tag);
        }
    }

    private void loadAttributes(InputStream in) throws IOException {
        IOException e;
        Throwable th;
        int i = 0;
        while (i < EXIF_TAGS.length) {
            try {
                this.mAttributes[i] = new HashMap();
                i++;
            } catch (IOException e2) {
                e = e2;
                try {
                    this.mIsSupportedFile = false;
                    Log.w(TAG, "Invalid image: ExifInterface got an unsupported image format file(ExifInterface supports JPEG and some RAW image formats only) or a corrupted JPEG file to ExifInterface.", e);
                    addDefaultValuesForCompatibility();
                } catch (Throwable th2) {
                    th = th2;
                    addDefaultValuesForCompatibility();
                    throw th;
                }
            }
        }
        if (this.mAssetInputStream != null) {
            if (handleRawResult(nativeGetRawAttributesFromAsset(this.mAssetInputStream.getNativeAsset()))) {
                addDefaultValuesForCompatibility();
                return;
            }
        } else if (this.mSeekableFileDescriptor == null) {
            InputStream in2 = new BufferedInputStream(in, 3);
            try {
                if (isJpegInputStream((BufferedInputStream) in2)) {
                    in = in2;
                } else if (handleRawResult(nativeGetRawAttributesFromInputStream(in2))) {
                    addDefaultValuesForCompatibility();
                    return;
                } else {
                    in = in2;
                }
            } catch (IOException e3) {
                e = e3;
                in = in2;
                this.mIsSupportedFile = false;
                Log.w(TAG, "Invalid image: ExifInterface got an unsupported image format file(ExifInterface supports JPEG and some RAW image formats only) or a corrupted JPEG file to ExifInterface.", e);
                addDefaultValuesForCompatibility();
            } catch (Throwable th3) {
                th = th3;
                in = in2;
                addDefaultValuesForCompatibility();
                throw th;
            }
        } else if (handleRawResult(nativeGetRawAttributesFromFileDescriptor(this.mSeekableFileDescriptor))) {
            addDefaultValuesForCompatibility();
            return;
        }
        getJpegAttributes(in);
        this.mIsSupportedFile = true;
        addDefaultValuesForCompatibility();
    }

    private static boolean isJpegInputStream(BufferedInputStream in) throws IOException {
        in.mark(3);
        byte[] signatureBytes = new byte[3];
        if (in.read(signatureBytes) != 3) {
            throw new EOFException();
        }
        boolean isJpeg = Arrays.equals(JPEG_SIGNATURE, signatureBytes);
        in.reset();
        return isJpeg;
    }

    private boolean handleRawResult(HashMap map) {
        boolean z = false;
        if (map == null) {
            return false;
        }
        this.mIsRaw = true;
        String value = (String) map.remove(TAG_HAS_THUMBNAIL);
        if (value != null) {
            z = value.equalsIgnoreCase("true");
        }
        this.mHasThumbnail = z;
        value = (String) map.remove(TAG_THUMBNAIL_OFFSET);
        if (value != null) {
            this.mThumbnailOffset = Integer.parseInt(value);
        }
        value = (String) map.remove(TAG_THUMBNAIL_LENGTH);
        if (value != null) {
            this.mThumbnailLength = Integer.parseInt(value);
        }
        this.mThumbnailBytes = (byte[]) map.remove(TAG_THUMBNAIL_DATA);
        for (Entry entry : map.entrySet()) {
            setAttribute((String) entry.getKey(), (String) entry.getValue());
        }
        return true;
    }

    private static boolean isSeekableFD(FileDescriptor fd) throws IOException {
        try {
            Os.lseek(fd, 0, OsConstants.SEEK_CUR);
            return true;
        } catch (ErrnoException e) {
            return false;
        }
    }

    private void printAttributes() {
        for (int i = 0; i < this.mAttributes.length; i++) {
            Log.d(TAG, "The size of tag group[" + i + "]: " + this.mAttributes[i].size());
            for (Entry entry : this.mAttributes[i].entrySet()) {
                ExifAttribute tagValue = (ExifAttribute) entry.getValue();
                Log.d(TAG, "tagName: " + entry.getKey() + ", tagType: " + tagValue.toString() + ", tagValue: '" + tagValue.getStringValue(this.mExifByteOrder) + "'");
            }
        }
    }

    public void saveAttributes() throws IOException {
        ErrnoException e;
        Throwable th;
        Object out;
        Object in;
        if (!this.mIsSupportedFile || this.mIsRaw) {
            throw new IOException("ExifInterface only supports saving attributes on JPEG formats.");
        } else if (this.mIsInputStream || (this.mSeekableFileDescriptor == null && this.mFilename == null)) {
            throw new IOException("ExifInterface does not support saving attributes for the current input.");
        } else {
            this.mThumbnailBytes = getThumbnail();
            AutoCloseable in2 = null;
            AutoCloseable out2 = null;
            File tempFile = null;
            try {
                FileInputStream in3;
                if (this.mFilename != null) {
                    File tempFile2 = new File(this.mFilename + ".tmp");
                    try {
                        if (new File(this.mFilename).renameTo(tempFile2)) {
                            tempFile = tempFile2;
                        } else {
                            throw new IOException("Could'nt rename to " + tempFile2.getAbsolutePath());
                        }
                    } catch (ErrnoException e2) {
                        e = e2;
                        tempFile = tempFile2;
                        try {
                            throw e.rethrowAsIOException();
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(in2);
                            IoUtils.closeQuietly(out2);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        tempFile = tempFile2;
                        IoUtils.closeQuietly(in2);
                        IoUtils.closeQuietly(out2);
                        throw th;
                    }
                } else if (this.mSeekableFileDescriptor != null) {
                    tempFile = File.createTempFile("temp", "jpg");
                    Os.lseek(this.mSeekableFileDescriptor, 0, OsConstants.SEEK_SET);
                    in3 = new FileInputStream(this.mSeekableFileDescriptor);
                    try {
                        FileOutputStream out3 = new FileOutputStream(tempFile);
                        try {
                            Streams.copy(in3, out3);
                            out2 = out3;
                            in2 = in3;
                        } catch (ErrnoException e3) {
                            e = e3;
                            out = out3;
                            in = in3;
                            throw e.rethrowAsIOException();
                        } catch (Throwable th4) {
                            th = th4;
                            out = out3;
                            in = in3;
                            IoUtils.closeQuietly(in2);
                            IoUtils.closeQuietly(out2);
                            throw th;
                        }
                    } catch (ErrnoException e4) {
                        e = e4;
                        in = in3;
                        throw e.rethrowAsIOException();
                    } catch (Throwable th5) {
                        th = th5;
                        in = in3;
                        IoUtils.closeQuietly(in2);
                        IoUtils.closeQuietly(out2);
                        throw th;
                    }
                }
                IoUtils.closeQuietly(in2);
                IoUtils.closeQuietly(out2);
                in2 = null;
                out2 = null;
                try {
                    in3 = new FileInputStream(tempFile);
                    try {
                        if (this.mFilename != null) {
                            out2 = new FileOutputStream(this.mFilename);
                        } else if (this.mSeekableFileDescriptor != null) {
                            Os.lseek(this.mSeekableFileDescriptor, 0, OsConstants.SEEK_SET);
                            out = new FileOutputStream(this.mSeekableFileDescriptor);
                        }
                        saveJpegAttributes(in3, out2);
                        IoUtils.closeQuietly(in3);
                        IoUtils.closeQuietly(out2);
                        tempFile.delete();
                        this.mThumbnailBytes = null;
                    } catch (ErrnoException e5) {
                        e = e5;
                        in = in3;
                        try {
                            throw e.rethrowAsIOException();
                        } catch (Throwable th6) {
                            th = th6;
                            IoUtils.closeQuietly(in2);
                            IoUtils.closeQuietly(null);
                            tempFile.delete();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        in = in3;
                        IoUtils.closeQuietly(in2);
                        IoUtils.closeQuietly(null);
                        tempFile.delete();
                        throw th;
                    }
                } catch (ErrnoException e6) {
                    e = e6;
                    throw e.rethrowAsIOException();
                }
            } catch (ErrnoException e7) {
                e = e7;
                throw e.rethrowAsIOException();
            }
        }
    }

    public boolean hasThumbnail() {
        return this.mHasThumbnail;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0038 A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:8:0x000e, PHI: r3 r5 } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] getThumbnail() {
        if (!this.mHasThumbnail) {
            return null;
        }
        byte[] bArr = this.mThumbnailBytes;
        if (bArr != null) {
            return this.mThumbnailBytes;
        }
        AutoCloseable in = null;
        try {
            bArr = this.mAssetInputStream;
            if (bArr != null) {
                bArr = nativeGetThumbnailFromAsset(this.mAssetInputStream.getNativeAsset(), this.mThumbnailOffset, this.mThumbnailLength);
                return bArr;
            }
            bArr = this.mFilename;
            if (bArr != null) {
                in = new FileInputStream(this.mFilename);
            } else if (this.mSeekableFileDescriptor != null) {
                FileDescriptor fileDescriptor = Os.dup(this.mSeekableFileDescriptor);
                Os.lseek(fileDescriptor, 0, OsConstants.SEEK_SET);
                Object in2 = new FileInputStream(fileDescriptor);
            }
            if (in == null) {
                throw new FileNotFoundException();
            }
            bArr = (in.skip((long) this.mThumbnailOffset) > ((long) this.mThumbnailOffset) ? 1 : (in.skip((long) this.mThumbnailOffset) == ((long) this.mThumbnailOffset) ? 0 : -1));
            if (bArr != null) {
                throw new IOException("Corrupted image");
            }
            byte[] buffer = new byte[this.mThumbnailLength];
            if (in.read(buffer) != this.mThumbnailLength) {
                throw new IOException("Corrupted image");
            }
            IoUtils.closeQuietly(in);
            return buffer;
        } catch (IOException e) {
        } finally {
            IoUtils.closeQuietly(in);
        }
        return null;
    }

    public long[] getThumbnailRange() {
        if (!this.mHasThumbnail) {
            return null;
        }
        long[] range = new long[2];
        range[0] = (long) this.mThumbnailOffset;
        range[1] = (long) this.mThumbnailLength;
        return range;
    }

    public boolean getLatLong(float[] output) {
        String latValue = getAttribute(TAG_GPS_LATITUDE);
        String latRef = getAttribute(TAG_GPS_LATITUDE_REF);
        String lngValue = getAttribute(TAG_GPS_LONGITUDE);
        String lngRef = getAttribute(TAG_GPS_LONGITUDE_REF);
        if (!(latValue == null || latRef == null || lngValue == null || lngRef == null)) {
            try {
                output[0] = convertRationalLatLonToFloat(latValue, latRef);
                output[1] = convertRationalLatLonToFloat(lngValue, lngRef);
                return true;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "getLatLong: IllegalArgumentException!", e);
            }
        }
        return false;
    }

    public double getAltitude(double defaultValue) {
        int i = -1;
        double altitude = getAttributeDouble(TAG_GPS_ALTITUDE, -1.0d);
        int ref = getAttributeInt(TAG_GPS_ALTITUDE_REF, -1);
        if (altitude < 0.0d || ref < 0) {
            return defaultValue;
        }
        if (ref != 1) {
            i = 1;
        }
        return ((double) i) * altitude;
    }

    public long getDateTime() {
        String dateTimeString = getAttribute(TAG_DATETIME);
        if (dateTimeString == null || !sNonZeroTimePattern.matcher(dateTimeString).matches()) {
            return -1;
        }
        try {
            Date datetime = sFormatter.parse(dateTimeString, new ParsePosition(0));
            if (datetime == null) {
                return -1;
            }
            long msecs = datetime.getTime();
            String subSecs = getAttribute(TAG_SUBSEC_TIME);
            if (subSecs != null) {
                try {
                    long sub = Long.valueOf(subSecs).longValue();
                    while (sub > 1000) {
                        sub /= 10;
                    }
                    msecs += sub;
                } catch (NumberFormatException e) {
                }
            }
            return msecs;
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "getDateTime: IllegalArgumentException!", ex);
            return -1;
        }
    }

    public long getGpsDateTime() {
        String date = getAttribute(TAG_GPS_DATESTAMP);
        String time = getAttribute(TAG_GPS_TIMESTAMP);
        if (date == null || time == null || (!sNonZeroTimePattern.matcher(date).matches() && !sNonZeroTimePattern.matcher(time).matches())) {
            return -1;
        }
        try {
            Date datetime = sFormatter.parse(date + ' ' + time, new ParsePosition(0));
            if (datetime == null) {
                return -1;
            }
            return datetime.getTime();
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "getGpsDateTime: IllegalArgumentException!", ex);
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0098 A:{ExcHandler: java.lang.NumberFormatException (e java.lang.NumberFormatException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:11:0x009e, code:
            throw new java.lang.IllegalArgumentException();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static float convertRationalLatLonToFloat(String rationalString, String ref) {
        try {
            String[] parts = rationalString.split(",");
            String[] pair = parts[0].split("/");
            double degrees = Double.parseDouble(pair[0].trim()) / Double.parseDouble(pair[1].trim());
            pair = parts[1].split("/");
            double minutes = Double.parseDouble(pair[0].trim()) / Double.parseDouble(pair[1].trim());
            pair = parts[2].split("/");
            double result = ((minutes / 60.0d) + degrees) + ((Double.parseDouble(pair[0].trim()) / Double.parseDouble(pair[1].trim())) / 3600.0d);
            if (!ref.equals("S")) {
                if (!ref.equals("W")) {
                    return (float) result;
                }
            }
            return (float) (-result);
        } catch (NumberFormatException e) {
        }
    }

    private void getJpegAttributes(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        byte marker = dataInputStream.readByte();
        if (marker != (byte) -1) {
            throw new IOException("Invalid marker: " + Integer.toHexString(marker & 255));
        } else if (dataInputStream.readByte() != MARKER_SOI) {
            throw new IOException("Invalid marker: " + Integer.toHexString(marker & 255));
        } else {
            int bytesRead = 1 + 1;
            while (true) {
                marker = dataInputStream.readByte();
                if (marker != (byte) -1) {
                    throw new IOException("Invalid marker:" + Integer.toHexString(marker & 255));
                }
                bytesRead++;
                marker = dataInputStream.readByte();
                bytesRead++;
                if (marker != MARKER_EOI && marker != MARKER_SOS) {
                    int length = dataInputStream.readUnsignedShort() - 2;
                    bytesRead += 2;
                    if (length < 0) {
                        throw new IOException("Invalid length");
                    }
                    byte[] bytes;
                    switch (marker) {
                        case (byte) -64:
                        case (byte) -63:
                        case (byte) -62:
                        case (byte) -61:
                        case KeymasterDefs.KM_ERROR_UNSUPPORTED_MIN_MAC_LENGTH /*-59*/:
                        case KeymasterDefs.KM_ERROR_MISSING_MIN_MAC_LENGTH /*-58*/:
                        case KeymasterDefs.KM_ERROR_INVALID_MAC_LENGTH /*-57*/:
                        case KeymasterDefs.KM_ERROR_CALLER_NONCE_PROHIBITED /*-55*/:
                        case KeymasterDefs.KM_ERROR_KEY_RATE_LIMIT_EXCEEDED /*-54*/:
                        case KeymasterDefs.KM_ERROR_MISSING_MAC_LENGTH /*-53*/:
                        case KeymasterDefs.KM_ERROR_MISSING_NONCE /*-51*/:
                        case KeymasterDefs.KM_ERROR_UNSUPPORTED_EC_FIELD /*-50*/:
                        case KeymasterDefs.KM_ERROR_SECURE_HW_COMMUNICATION_FAILED /*-49*/:
                            if (marker == MARKER_SOF2) {
                                this.mIsProgressiveModePic = true;
                                Log.d(TAG, "the picture is progressive jpeg");
                            }
                            if (dataInputStream.skipBytes(1) == 1) {
                                this.mAttributes[0].put(TAG_IMAGE_LENGTH, ExifAttribute.createULong((long) dataInputStream.readUnsignedShort(), this.mExifByteOrder));
                                this.mAttributes[0].put(TAG_IMAGE_WIDTH, ExifAttribute.createULong((long) dataInputStream.readUnsignedShort(), this.mExifByteOrder));
                                length -= 5;
                                break;
                            }
                            throw new IOException("Invalid SOFx");
                        case (byte) -31:
                            if (length >= 6) {
                                byte[] identifier = new byte[6];
                                if (inputStream.read(identifier) == 6) {
                                    bytesRead += 6;
                                    length -= 6;
                                    if (Arrays.equals(identifier, IDENTIFIER_EXIF_APP1)) {
                                        if (length > 0) {
                                            bytes = new byte[length];
                                            if (dataInputStream.read(bytes) == length) {
                                                readExifSegment(bytes, bytesRead);
                                                bytesRead += length;
                                                length = 0;
                                                break;
                                            }
                                            throw new IOException("Invalid exif");
                                        }
                                        throw new IOException("Invalid exif");
                                    }
                                }
                                throw new IOException("Invalid exif");
                            }
                            break;
                        case (byte) -2:
                            bytes = new byte[length];
                            if (dataInputStream.read(bytes) == length) {
                                length = 0;
                                if (getAttribute(TAG_USER_COMMENT) == null) {
                                    this.mAttributes[1].put(TAG_USER_COMMENT, ExifAttribute.createString(new String(bytes, ASCII)));
                                    break;
                                }
                            }
                            throw new IOException("Invalid exif");
                            break;
                    }
                    if (length < 0) {
                        throw new IOException("Invalid length");
                    } else if (dataInputStream.skipBytes(length) != length) {
                        throw new IOException("Invalid JPEG segment");
                    } else {
                        bytesRead += length;
                    }
                } else {
                    return;
                }
            }
        }
    }

    private void saveJpegAttributes(InputStream inputStream, OutputStream outputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        ByteOrderAwarenessDataOutputStream dataOutputStream = new ByteOrderAwarenessDataOutputStream(outputStream, ByteOrder.BIG_ENDIAN);
        if (dataInputStream.readByte() != (byte) -1) {
            throw new IOException("Invalid marker");
        }
        dataOutputStream.writeByte(-1);
        if (dataInputStream.readByte() != MARKER_SOI) {
            throw new IOException("Invalid marker");
        }
        dataOutputStream.writeByte(-40);
        dataOutputStream.writeByte(-1);
        dataOutputStream.writeByte(-31);
        writeExifSegment(dataOutputStream, 6);
        byte[] bytes = new byte[4096];
        while (dataInputStream.readByte() == (byte) -1) {
            byte marker = dataInputStream.readByte();
            int length;
            int read;
            switch (marker) {
                case KeymasterDefs.KM_ERROR_UNSUPPORTED_TAG /*-39*/:
                case (byte) -38:
                    dataOutputStream.writeByte(-1);
                    dataOutputStream.writeByte(marker);
                    Streams.copy(dataInputStream, dataOutputStream);
                    return;
                case (byte) -31:
                    length = dataInputStream.readUnsignedShort() - 2;
                    if (length >= 0) {
                        byte[] identifier = new byte[6];
                        if (length >= 6) {
                            if (dataInputStream.read(identifier) == 6) {
                                if (Arrays.equals(identifier, IDENTIFIER_EXIF_APP1)) {
                                    if (dataInputStream.skip((long) (length - 6)) == ((long) (length - 6))) {
                                        break;
                                    }
                                    throw new IOException("Invalid length");
                                }
                            }
                            throw new IOException("Invalid exif");
                        }
                        dataOutputStream.writeByte(-1);
                        dataOutputStream.writeByte(marker);
                        dataOutputStream.writeUnsignedShort(length + 2);
                        if (length >= 6) {
                            length -= 6;
                            dataOutputStream.write(identifier);
                        }
                        while (length > 0) {
                            read = dataInputStream.read(bytes, 0, Math.min(length, bytes.length));
                            if (read < 0) {
                                break;
                            }
                            dataOutputStream.write(bytes, 0, read);
                            length -= read;
                        }
                        break;
                    }
                    throw new IOException("Invalid length");
                default:
                    dataOutputStream.writeByte(-1);
                    dataOutputStream.writeByte(marker);
                    length = dataInputStream.readUnsignedShort();
                    dataOutputStream.writeUnsignedShort(length);
                    length -= 2;
                    if (length >= 0) {
                        while (length > 0) {
                            read = dataInputStream.read(bytes, 0, Math.min(length, bytes.length));
                            if (read < 0) {
                                break;
                            }
                            dataOutputStream.write(bytes, 0, read);
                            length -= read;
                        }
                        break;
                    }
                    throw new IOException("Invalid length");
            }
        }
        throw new IOException("Invalid marker");
    }

    private void readExifSegment(byte[] exifBytes, int exifOffsetFromBeginning) throws IOException {
        ByteOrderAwarenessDataInputStream dataInputStream = new ByteOrderAwarenessDataInputStream(exifBytes);
        short byteOrder = dataInputStream.readShort();
        switch (byteOrder) {
            case (short) 18761:
                this.mExifByteOrder = ByteOrder.LITTLE_ENDIAN;
                break;
            case (short) 19789:
                this.mExifByteOrder = ByteOrder.BIG_ENDIAN;
                break;
            default:
                throw new IOException("Invalid byte order: " + Integer.toHexString(byteOrder));
        }
        dataInputStream.setByteOrder(this.mExifByteOrder);
        int startCode = dataInputStream.readUnsignedShort();
        if (startCode != 42) {
            throw new IOException("Invalid exif start: " + Integer.toHexString(startCode));
        }
        long firstIfdOffset = dataInputStream.readUnsignedInt();
        if (firstIfdOffset < 8 || firstIfdOffset >= ((long) exifBytes.length)) {
            throw new IOException("Invalid first Ifd offset: " + firstIfdOffset);
        }
        firstIfdOffset -= 8;
        if (firstIfdOffset <= 0 || dataInputStream.skip(firstIfdOffset) == firstIfdOffset) {
            this.mReadImageFileDirectoryCount = 0;
            readImageFileDirectory(dataInputStream, 0);
            String jpegInterchangeFormatString = getAttribute(JPEG_INTERCHANGE_FORMAT_TAG.name);
            String jpegInterchangeFormatLengthString = getAttribute(JPEG_INTERCHANGE_FORMAT_LENGTH_TAG.name);
            if (jpegInterchangeFormatString != null && jpegInterchangeFormatLengthString != null) {
                try {
                    int jpegInterchangeFormat = Integer.parseInt(jpegInterchangeFormatString);
                    int jpegInterchangeFormatLength = Math.min(jpegInterchangeFormat + Integer.parseInt(jpegInterchangeFormatLengthString), exifBytes.length) - jpegInterchangeFormat;
                    if (jpegInterchangeFormat > 0 && jpegInterchangeFormatLength > 0) {
                        this.mHasThumbnail = true;
                        this.mThumbnailOffset = exifOffsetFromBeginning + jpegInterchangeFormat;
                        this.mThumbnailLength = jpegInterchangeFormatLength;
                        if (this.mFilename == null && this.mAssetInputStream == null && this.mSeekableFileDescriptor == null) {
                            byte[] thumbnailBytes = new byte[jpegInterchangeFormatLength];
                            dataInputStream.seek((long) jpegInterchangeFormat);
                            dataInputStream.readFully(thumbnailBytes);
                            this.mThumbnailBytes = thumbnailBytes;
                            return;
                        }
                        return;
                    }
                    return;
                } catch (NumberFormatException e) {
                    return;
                }
            }
            return;
        }
        throw new IOException("Couldn't jump to first Ifd: " + firstIfdOffset);
    }

    private void addDefaultValuesForCompatibility() {
        String valueOfDateTimeOriginal = getAttribute(TAG_DATETIME_ORIGINAL);
        if (valueOfDateTimeOriginal != null) {
            this.mAttributes[0].put(TAG_DATETIME, ExifAttribute.createString(valueOfDateTimeOriginal));
        }
        if (getAttribute(TAG_IMAGE_WIDTH) == null) {
            this.mAttributes[0].put(TAG_IMAGE_WIDTH, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (getAttribute(TAG_IMAGE_LENGTH) == null) {
            this.mAttributes[0].put(TAG_IMAGE_LENGTH, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (getAttribute(TAG_ORIENTATION) == null) {
            this.mAttributes[0].put(TAG_ORIENTATION, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (getAttribute(TAG_LIGHT_SOURCE) == null) {
            this.mAttributes[1].put(TAG_LIGHT_SOURCE, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
    }

    private void readImageFileDirectory(ByteOrderAwarenessDataInputStream dataInputStream, int hint) throws IOException {
        if (dataInputStream.peek() + 2 <= dataInputStream.mLength) {
            short numberOfDirectoryEntry = dataInputStream.readShort();
            if (dataInputStream.peek() + ((long) (numberOfDirectoryEntry * 12)) <= dataInputStream.mLength) {
                this.mReadImageFileDirectoryCount++;
                if (this.mReadImageFileDirectoryCount > 512) {
                    Log.w(TAG, "readImageFileDirectory maybe can't breakout, so return.");
                    return;
                }
                for (short i = (short) 0; i < numberOfDirectoryEntry; i = (short) (i + 1)) {
                    int tagNumber = dataInputStream.readUnsignedShort();
                    int dataFormat = dataInputStream.readUnsignedShort();
                    int numberOfComponents = dataInputStream.readInt();
                    long nextEntryOffset = dataInputStream.peek() + 4;
                    ExifTag tag = (ExifTag) sExifTagMapsForReading[hint].get(Integer.valueOf(tagNumber));
                    if (tag == null || dataFormat <= 0 || dataFormat >= IFD_FORMAT_BYTES_PER_FORMAT.length) {
                        if (tag == null) {
                        }
                        dataInputStream.seek(nextEntryOffset);
                    } else {
                        long offset;
                        int byteCount = numberOfComponents * IFD_FORMAT_BYTES_PER_FORMAT[dataFormat];
                        if (byteCount > 4) {
                            offset = dataInputStream.readUnsignedInt();
                            if (((long) byteCount) + offset <= dataInputStream.mLength) {
                                dataInputStream.seek(offset);
                            } else {
                                Log.w(TAG, "Skip the tag entry since data offset is invalid: " + offset);
                                dataInputStream.seek(nextEntryOffset);
                            }
                        }
                        int innerIfdHint = getIfdHintFromTagNumber(tagNumber);
                        if (innerIfdHint >= 0) {
                            offset = -1;
                            switch (dataFormat) {
                                case 3:
                                    offset = (long) dataInputStream.readUnsignedShort();
                                    break;
                                case 4:
                                    offset = dataInputStream.readUnsignedInt();
                                    break;
                                case 8:
                                    offset = (long) dataInputStream.readShort();
                                    break;
                                case 9:
                                    offset = (long) dataInputStream.readInt();
                                    break;
                            }
                            if (offset <= 0 || offset >= dataInputStream.mLength) {
                                Log.w(TAG, "Skip jump into the IFD since its offset is invalid: " + offset);
                            } else {
                                dataInputStream.seek(offset);
                                readImageFileDirectory(dataInputStream, innerIfdHint);
                            }
                            dataInputStream.seek(nextEntryOffset);
                        } else {
                            if (IFD_FORMAT_BYTES_PER_FORMAT[dataFormat] * numberOfComponents >= 0) {
                                byte[] bytes = new byte[(IFD_FORMAT_BYTES_PER_FORMAT[dataFormat] * numberOfComponents)];
                                dataInputStream.readFully(bytes);
                                this.mAttributes[hint].put(tag.name, new ExifAttribute(dataFormat, numberOfComponents, bytes, null));
                            }
                            if (dataInputStream.peek() != nextEntryOffset) {
                                dataInputStream.seek(nextEntryOffset);
                            }
                        }
                    }
                }
                if (dataInputStream.peek() + 4 <= dataInputStream.mLength) {
                    long nextIfdOffset = dataInputStream.readUnsignedInt();
                    if (nextIfdOffset > 8 && nextIfdOffset < dataInputStream.mLength) {
                        dataInputStream.seek(nextIfdOffset);
                        readImageFileDirectory(dataInputStream, 4);
                    }
                }
            }
        }
    }

    private static int getIfdHintFromTagNumber(int tagNumber) {
        for (int i = 0; i < IFD_POINTER_TAG_HINTS.length; i++) {
            if (IFD_POINTER_TAGS[i].number == tagNumber) {
                return IFD_POINTER_TAG_HINTS[i];
            }
        }
        return -1;
    }

    private int writeExifSegment(ByteOrderAwarenessDataOutputStream dataOutputStream, int exifOffsetFromBeginning) throws IOException {
        int hint;
        int i;
        int size;
        int[] ifdOffsets = new int[EXIF_TAGS.length];
        int[] ifdDataSizes = new int[EXIF_TAGS.length];
        for (ExifTag tag : IFD_POINTER_TAGS) {
            removeAttribute(tag.name);
        }
        removeAttribute(JPEG_INTERCHANGE_FORMAT_TAG.name);
        removeAttribute(JPEG_INTERCHANGE_FORMAT_LENGTH_TAG.name);
        for (hint = 0; hint < EXIF_TAGS.length; hint++) {
            for (Entry entry : this.mAttributes[hint].entrySet().toArray()) {
                if (entry.getValue() == null) {
                    this.mAttributes[hint].remove(entry.getKey());
                }
            }
        }
        if (!this.mAttributes[3].isEmpty()) {
            this.mAttributes[1].put(IFD_POINTER_TAGS[2].name, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (!this.mAttributes[1].isEmpty()) {
            this.mAttributes[0].put(IFD_POINTER_TAGS[0].name, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (!this.mAttributes[2].isEmpty()) {
            this.mAttributes[0].put(IFD_POINTER_TAGS[1].name, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (this.mHasThumbnail) {
            this.mAttributes[0].put(JPEG_INTERCHANGE_FORMAT_TAG.name, ExifAttribute.createULong(0, this.mExifByteOrder));
            this.mAttributes[0].put(JPEG_INTERCHANGE_FORMAT_LENGTH_TAG.name, ExifAttribute.createULong((long) this.mThumbnailLength, this.mExifByteOrder));
        }
        for (i = 0; i < EXIF_TAGS.length; i++) {
            int sum = 0;
            for (Entry entry2 : this.mAttributes[i].entrySet()) {
                size = ((ExifAttribute) entry2.getValue()).size();
                if (size > 4) {
                    sum += size;
                }
            }
            ifdDataSizes[i] = ifdDataSizes[i] + sum;
        }
        int position = 8;
        for (hint = 0; hint < EXIF_TAGS.length; hint++) {
            if (!this.mAttributes[hint].isEmpty()) {
                ifdOffsets[hint] = position;
                position += (((this.mAttributes[hint].size() * 12) + 2) + 4) + ifdDataSizes[hint];
            }
        }
        if (this.mHasThumbnail) {
            int thumbnailOffset = position;
            this.mAttributes[0].put(JPEG_INTERCHANGE_FORMAT_TAG.name, ExifAttribute.createULong((long) thumbnailOffset, this.mExifByteOrder));
            this.mThumbnailOffset = exifOffsetFromBeginning + thumbnailOffset;
            position += this.mThumbnailLength;
        }
        int totalSize = position + 8;
        if (!this.mAttributes[1].isEmpty()) {
            this.mAttributes[0].put(IFD_POINTER_TAGS[0].name, ExifAttribute.createULong((long) ifdOffsets[1], this.mExifByteOrder));
        }
        if (!this.mAttributes[2].isEmpty()) {
            this.mAttributes[0].put(IFD_POINTER_TAGS[1].name, ExifAttribute.createULong((long) ifdOffsets[2], this.mExifByteOrder));
        }
        if (!this.mAttributes[3].isEmpty()) {
            this.mAttributes[1].put(IFD_POINTER_TAGS[2].name, ExifAttribute.createULong((long) ifdOffsets[3], this.mExifByteOrder));
        }
        dataOutputStream.writeUnsignedShort(totalSize);
        dataOutputStream.write(IDENTIFIER_EXIF_APP1);
        dataOutputStream.writeShort(this.mExifByteOrder == ByteOrder.BIG_ENDIAN ? BYTE_ALIGN_MM : BYTE_ALIGN_II);
        dataOutputStream.setByteOrder(this.mExifByteOrder);
        dataOutputStream.writeUnsignedShort(42);
        dataOutputStream.writeUnsignedInt(8);
        for (hint = 0; hint < EXIF_TAGS.length; hint++) {
            if (!this.mAttributes[hint].isEmpty()) {
                ExifAttribute attribute;
                dataOutputStream.writeUnsignedShort(this.mAttributes[hint].size());
                int dataOffset = ((ifdOffsets[hint] + 2) + (this.mAttributes[hint].size() * 12)) + 4;
                for (Entry entry22 : this.mAttributes[hint].entrySet()) {
                    int tagNumber = ((ExifTag) sExifTagMapsForWriting[hint].get(entry22.getKey())).number;
                    attribute = (ExifAttribute) entry22.getValue();
                    size = attribute.size();
                    dataOutputStream.writeUnsignedShort(tagNumber);
                    dataOutputStream.writeUnsignedShort(attribute.format);
                    dataOutputStream.writeInt(attribute.numberOfComponents);
                    if (size > 4) {
                        dataOutputStream.writeUnsignedInt((long) dataOffset);
                        dataOffset += size;
                    } else {
                        dataOutputStream.write(attribute.bytes);
                        if (size < 4) {
                            for (i = size; i < 4; i++) {
                                dataOutputStream.writeByte(0);
                            }
                        }
                    }
                }
                if (hint != 0 || this.mAttributes[4].isEmpty()) {
                    dataOutputStream.writeUnsignedInt(0);
                } else {
                    dataOutputStream.writeUnsignedInt((long) ifdOffsets[4]);
                }
                for (Entry entry222 : this.mAttributes[hint].entrySet()) {
                    attribute = (ExifAttribute) entry222.getValue();
                    if (attribute.bytes.length > 4) {
                        dataOutputStream.write(attribute.bytes, 0, attribute.bytes.length);
                    }
                }
            }
        }
        if (this.mHasThumbnail) {
            dataOutputStream.write(getThumbnail());
        }
        dataOutputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
        return totalSize;
    }

    private static Pair<Integer, Integer> guessDataFormat(String entryValue) {
        if (entryValue.contains(",")) {
            String[] entryValues = entryValue.split(",");
            Pair<Integer, Integer> dataFormat = guessDataFormat(entryValues[0]);
            if (((Integer) dataFormat.first).intValue() == 2) {
                return dataFormat;
            }
            for (int i = 1; i < entryValues.length; i++) {
                Pair<Integer, Integer> guessDataFormat = guessDataFormat(entryValues[i]);
                int first = -1;
                int second = -1;
                if (guessDataFormat.first == dataFormat.first || guessDataFormat.second == dataFormat.first) {
                    first = ((Integer) dataFormat.first).intValue();
                }
                if (((Integer) dataFormat.second).intValue() != -1 && (guessDataFormat.first == dataFormat.second || guessDataFormat.second == dataFormat.second)) {
                    second = ((Integer) dataFormat.second).intValue();
                }
                if (first == -1 && second == -1) {
                    return new Pair(Integer.valueOf(2), Integer.valueOf(-1));
                }
                if (first == -1) {
                    dataFormat = new Pair(Integer.valueOf(second), Integer.valueOf(-1));
                } else if (second == -1) {
                    dataFormat = new Pair(Integer.valueOf(first), Integer.valueOf(-1));
                }
            }
            return dataFormat;
        }
        if (entryValue.contains("/")) {
            String[] rationalNumber = entryValue.split("/");
            if (rationalNumber.length == 2) {
                try {
                    long numerator = Long.parseLong(rationalNumber[0]);
                    long denominator = Long.parseLong(rationalNumber[1]);
                    if (numerator < 0 || denominator < 0) {
                        return new Pair(Integer.valueOf(10), Integer.valueOf(-1));
                    }
                    if (numerator > 2147483647L || denominator > 2147483647L) {
                        return new Pair(Integer.valueOf(5), Integer.valueOf(-1));
                    }
                    return new Pair(Integer.valueOf(10), Integer.valueOf(5));
                } catch (NumberFormatException e) {
                }
            }
            return new Pair(Integer.valueOf(2), Integer.valueOf(-1));
        }
        try {
            Long longValue = Long.valueOf(Long.parseLong(entryValue));
            if (longValue.longValue() >= 0 && longValue.longValue() <= 65535) {
                return new Pair(Integer.valueOf(3), Integer.valueOf(4));
            }
            if (longValue.longValue() < 0) {
                return new Pair(Integer.valueOf(9), Integer.valueOf(-1));
            }
            return new Pair(Integer.valueOf(4), Integer.valueOf(-1));
        } catch (NumberFormatException e2) {
            try {
                Double.parseDouble(entryValue);
                return new Pair(Integer.valueOf(12), Integer.valueOf(-1));
            } catch (NumberFormatException e3) {
                return new Pair(Integer.valueOf(2), Integer.valueOf(-1));
            }
        }
    }
}
