package androidx.lifecycle;

public class ViewModelProvider {
    private final Factory mFactory;
    private final ViewModelStore mViewModelStore;

    public interface Factory {
        <T extends ViewModel> T create(Class<T> cls);
    }

    public ViewModelProvider(ViewModelStore store, Factory factory) {
        this.mFactory = factory;
        this.mViewModelStore = store;
    }

    public <T extends ViewModel> T get(Class<T> modelClass) {
        String canonicalName = modelClass.getCanonicalName();
        if (canonicalName != null) {
            return (T) get("androidx.lifecycle.ViewModelProvider.DefaultKey:" + canonicalName, modelClass);
        }
        throw new IllegalArgumentException("Local and anonymous classes can not be ViewModels");
    }

    public <T extends ViewModel> T get(String key, Class<T> modelClass) {
        T t = (T) this.mViewModelStore.get(key);
        if (modelClass.isInstance(t)) {
            return t;
        }
        T t2 = (T) this.mFactory.create(modelClass);
        this.mViewModelStore.put(key, t2);
        return t2;
    }
}
