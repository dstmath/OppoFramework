package java.util;

public class Observable {
    private boolean changed = false;
    private final ArrayList<Observer> observers = new ArrayList();

    public synchronized void addObserver(Observer o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (!this.observers.contains(o)) {
            this.observers.add(o);
        }
    }

    public synchronized void deleteObserver(Observer o) {
        this.observers.remove((Object) o);
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    /* JADX WARNING: Missing block: B:9:0x001d, code:
            r1 = r0.length - 1;
     */
    /* JADX WARNING: Missing block: B:10:0x0020, code:
            if (r1 < 0) goto L_0x002d;
     */
    /* JADX WARNING: Missing block: B:11:0x0022, code:
            r0[r1].update(r4, r5);
            r1 = r1 - 1;
     */
    /* JADX WARNING: Missing block: B:15:0x002d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyObservers(Object arg) {
        synchronized (this) {
            if (hasChanged()) {
                Observer[] arrLocal = (Observer[]) this.observers.toArray(new Observer[this.observers.size()]);
                clearChanged();
            }
        }
    }

    public synchronized void deleteObservers() {
        this.observers.clear();
    }

    protected synchronized void setChanged() {
        this.changed = true;
    }

    protected synchronized void clearChanged() {
        this.changed = false;
    }

    public synchronized boolean hasChanged() {
        return this.changed;
    }

    public synchronized int countObservers() {
        return this.observers.size();
    }
}
