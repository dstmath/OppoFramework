package androidx.lifecycle;

public interface Observer<T> {
    void onChanged(T t);
}
