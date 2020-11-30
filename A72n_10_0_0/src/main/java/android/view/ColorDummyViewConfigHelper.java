package android.view;

public class ColorDummyViewConfigHelper implements IColorViewConfigHelper {
    @Override // android.view.IColorViewConfigHelper
    public int getScaledOverscrollDistance(int dist) {
        return dist;
    }

    @Override // android.view.IColorViewConfigHelper
    public int getScaledOverflingDistance(int dist) {
        return dist;
    }

    @Override // android.view.IColorViewConfigHelper
    public int calcRealOverScrollDist(int dist, int scrollY) {
        return dist;
    }

    @Override // android.view.IColorViewConfigHelper
    public int calcRealOverScrollDist(int dist, int scrollY, int range) {
        return dist;
    }
}
