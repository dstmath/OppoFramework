package com.android.internal.util;

public final class GrowingArrayUtils {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f50-assertionsDisabled = (GrowingArrayUtils.class.desiredAssertionStatus() ^ 1);

    public static <T> T[] append(T[] array, int currentSize, T element) {
        if (f50-assertionsDisabled || currentSize <= array.length) {
            if (currentSize + 1 > array.length) {
                T[] newArray = ArrayUtils.newUnpaddedArray(array.getClass().getComponentType(), growSize(currentSize));
                System.arraycopy(array, 0, newArray, 0, currentSize);
                array = newArray;
            }
            array[currentSize] = element;
            return array;
        }
        throw new AssertionError();
    }

    public static int[] append(int[] array, int currentSize, int element) {
        if (f50-assertionsDisabled || currentSize <= array.length) {
            if (currentSize + 1 > array.length) {
                int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
                System.arraycopy(array, 0, newArray, 0, currentSize);
                array = newArray;
            }
            array[currentSize] = element;
            return array;
        }
        throw new AssertionError();
    }

    public static long[] append(long[] array, int currentSize, long element) {
        if (f50-assertionsDisabled || currentSize <= array.length) {
            if (currentSize + 1 > array.length) {
                long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
                System.arraycopy(array, 0, newArray, 0, currentSize);
                array = newArray;
            }
            array[currentSize] = element;
            return array;
        }
        throw new AssertionError();
    }

    public static boolean[] append(boolean[] array, int currentSize, boolean element) {
        if (f50-assertionsDisabled || currentSize <= array.length) {
            if (currentSize + 1 > array.length) {
                boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
                System.arraycopy(array, 0, newArray, 0, currentSize);
                array = newArray;
            }
            array[currentSize] = element;
            return array;
        }
        throw new AssertionError();
    }

    public static float[] append(float[] array, int currentSize, float element) {
        if (f50-assertionsDisabled || currentSize <= array.length) {
            if (currentSize + 1 > array.length) {
                float[] newArray = ArrayUtils.newUnpaddedFloatArray(growSize(currentSize));
                System.arraycopy(array, 0, newArray, 0, currentSize);
                array = newArray;
            }
            array[currentSize] = element;
            return array;
        }
        throw new AssertionError();
    }

    public static <T> T[] insert(T[] array, int currentSize, int index, T element) {
        if (!f50-assertionsDisabled && currentSize > array.length) {
            throw new AssertionError();
        } else if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        } else {
            T[] newArray = ArrayUtils.newUnpaddedArray(array.getClass().getComponentType(), growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        }
    }

    public static int[] insert(int[] array, int currentSize, int index, int element) {
        if (!f50-assertionsDisabled && currentSize > array.length) {
            throw new AssertionError();
        } else if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        } else {
            int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        }
    }

    public static long[] insert(long[] array, int currentSize, int index, long element) {
        if (!f50-assertionsDisabled && currentSize > array.length) {
            throw new AssertionError();
        } else if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        } else {
            long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        }
    }

    public static boolean[] insert(boolean[] array, int currentSize, int index, boolean element) {
        if (!f50-assertionsDisabled && currentSize > array.length) {
            throw new AssertionError();
        } else if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        } else {
            boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        }
    }

    public static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize * 2;
    }

    private GrowingArrayUtils() {
    }
}
