package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.MarshalRegistry;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.util.Log;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MarshalQueryableArray<T> implements MarshalQueryable<T> {
    private static final boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static final String TAG = MarshalQueryableArray.class.getSimpleName();

    private class MarshalerArray extends Marshaler<T> {
        private final Class<T> mClass;
        private final Class<?> mComponentClass;
        private final Marshaler<?> mComponentMarshaler;

        protected MarshalerArray(TypeReference<T> typeReference, int nativeType) {
            super(MarshalQueryableArray.this, typeReference, nativeType);
            this.mClass = typeReference.getRawType();
            TypeReference<?> componentToken = typeReference.getComponentType();
            this.mComponentMarshaler = MarshalRegistry.getMarshaler(componentToken, this.mNativeType);
            this.mComponentClass = componentToken.getRawType();
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public void marshal(T value, ByteBuffer buffer) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                marshalArrayElement(this.mComponentMarshaler, buffer, value, i);
            }
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public T unmarshal(ByteBuffer buffer) {
            Object array;
            int elementSize = this.mComponentMarshaler.getNativeSize();
            if (elementSize != Marshaler.NATIVE_SIZE_DYNAMIC) {
                int remaining = buffer.remaining();
                int arraySize = remaining / elementSize;
                if (remaining % elementSize == 0) {
                    array = Array.newInstance(this.mComponentClass, arraySize);
                    for (int i = 0; i < arraySize; i++) {
                        Array.set(array, i, this.mComponentMarshaler.unmarshal(buffer));
                    }
                } else {
                    throw new UnsupportedOperationException("Arrays for " + this.mTypeReference + " must be packed tighly into a multiple of " + elementSize + "; but there are " + (remaining % elementSize) + " left over bytes");
                }
            } else {
                ArrayList<Object> arrayList = new ArrayList<>();
                while (buffer.hasRemaining()) {
                    arrayList.add(this.mComponentMarshaler.unmarshal(buffer));
                }
                array = copyListToArray(arrayList, Array.newInstance(this.mComponentClass, arrayList.size()));
            }
            if (buffer.remaining() != 0) {
                String access$000 = MarshalQueryableArray.TAG;
                Log.e(access$000, "Trailing bytes (" + buffer.remaining() + ") left over after unpacking " + this.mClass);
            }
            return this.mClass.cast(array);
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int getNativeSize() {
            return NATIVE_SIZE_DYNAMIC;
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int calculateMarshalSize(T value) {
            int elementSize = this.mComponentMarshaler.getNativeSize();
            int arrayLength = Array.getLength(value);
            if (elementSize != Marshaler.NATIVE_SIZE_DYNAMIC) {
                return elementSize * arrayLength;
            }
            int size = 0;
            for (int i = 0; i < arrayLength; i++) {
                size += calculateElementMarshalSize(this.mComponentMarshaler, value, i);
            }
            return size;
        }

        private <TElem> void marshalArrayElement(Marshaler<TElem> marshaler, ByteBuffer buffer, Object array, int index) {
            marshaler.marshal(Array.get(array, index), buffer);
        }

        private Object copyListToArray(ArrayList<?> arrayList, Object arrayDest) {
            return arrayList.toArray((Object[]) arrayDest);
        }

        private <TElem> int calculateElementMarshalSize(Marshaler<TElem> marshaler, Object array, int index) {
            return marshaler.calculateMarshalSize(Array.get(array, index));
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<T> createMarshaler(TypeReference<T> managedType, int nativeType) {
        return new MarshalerArray(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<T> managedType, int nativeType) {
        return managedType.getRawType().isArray();
    }
}
