package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.os.Parcel;
import android.os.Parcelable;
import java.nio.ByteBuffer;

public class MarshalQueryableParcelable<T extends Parcelable> implements MarshalQueryable<T> {
    private static final boolean DEBUG = false;
    private static final String FIELD_CREATOR = "CREATOR";
    private static final String TAG = "MarshalParcelable";

    private class MarshalerParcelable extends Marshaler<T> {
        private final Class<T> mClass;
        private final Parcelable.Creator<T> mCreator;

        @Override // android.hardware.camera2.marshal.Marshaler
        public /* bridge */ /* synthetic */ int calculateMarshalSize(Object obj) {
            return calculateMarshalSize((Parcelable) ((Parcelable) obj));
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: MutableMD:(android.os.Parcelable, java.nio.ByteBuffer):void
         arg types: [T, java.nio.ByteBuffer]
         candidates:
          android.hardware.camera2.marshal.impl.MarshalQueryableParcelable.MarshalerParcelable.marshal(java.lang.Object, java.nio.ByteBuffer):void
          MutableMD:(java.lang.Object, java.nio.ByteBuffer):void
          MutableMD:(android.os.Parcelable, java.nio.ByteBuffer):void */
        @Override // android.hardware.camera2.marshal.Marshaler
        public /* bridge */ /* synthetic */ void marshal(Object obj, ByteBuffer byteBuffer) {
            marshal((Parcelable) ((Parcelable) obj), byteBuffer);
        }

        protected MarshalerParcelable(TypeReference<T> typeReference, int nativeType) {
            super(MarshalQueryableParcelable.this, typeReference, nativeType);
            this.mClass = typeReference.getRawType();
            try {
                try {
                    this.mCreator = (Parcelable.Creator) this.mClass.getDeclaredField(MarshalQueryableParcelable.FIELD_CREATOR).get(null);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                } catch (IllegalArgumentException e2) {
                    throw new AssertionError(e2);
                }
            } catch (NoSuchFieldException e3) {
                throw new AssertionError(e3);
            }
        }

        /* JADX INFO: finally extract failed */
        public void marshal(T value, ByteBuffer buffer) {
            Parcel parcel = Parcel.obtain();
            try {
                value.writeToParcel(parcel, 0);
                if (!parcel.hasFileDescriptors()) {
                    byte[] parcelContents = parcel.marshall();
                    parcel.recycle();
                    if (parcelContents.length != 0) {
                        buffer.put(parcelContents);
                        return;
                    }
                    throw new AssertionError("No data marshaled for " + ((Object) value));
                }
                throw new UnsupportedOperationException("Parcelable " + ((Object) value) + " must not have file descriptors");
            } catch (Throwable th) {
                parcel.recycle();
                throw th;
            }
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public T unmarshal(ByteBuffer buffer) {
            buffer.mark();
            Parcel parcel = Parcel.obtain();
            try {
                int maxLength = buffer.remaining();
                byte[] remaining = new byte[maxLength];
                buffer.get(remaining);
                parcel.unmarshall(remaining, 0, maxLength);
                parcel.setDataPosition(0);
                T value = this.mCreator.createFromParcel(parcel);
                int actualLength = parcel.dataPosition();
                if (actualLength != 0) {
                    buffer.reset();
                    buffer.position(buffer.position() + actualLength);
                    return this.mClass.cast(value);
                }
                throw new AssertionError("No data marshaled for " + ((Object) value));
            } finally {
                parcel.recycle();
            }
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int getNativeSize() {
            return NATIVE_SIZE_DYNAMIC;
        }

        public int calculateMarshalSize(T value) {
            Parcel parcel = Parcel.obtain();
            try {
                value.writeToParcel(parcel, 0);
                return parcel.marshall().length;
            } finally {
                parcel.recycle();
            }
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<T> createMarshaler(TypeReference<T> managedType, int nativeType) {
        return new MarshalerParcelable(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<T> managedType, int nativeType) {
        return Parcelable.class.isAssignableFrom(managedType.getRawType());
    }
}
