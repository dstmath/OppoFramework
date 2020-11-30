package androidx.fragment.app;

public abstract class FragmentTransaction {
    public abstract FragmentTransaction add(int i, Fragment fragment, String str);

    public abstract FragmentTransaction attach(Fragment fragment);

    public abstract int commit();

    public abstract FragmentTransaction detach(Fragment fragment);
}
