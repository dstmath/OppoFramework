package android.graphics;

public class OppoBaseShader {
    public Shader getDarkModeShader() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void resetLocalMatrix(Shader shader) {
        if (shader != null) {
            Matrix matrix = new Matrix();
            if (shader.getLocalMatrix(matrix)) {
                shader.setLocalMatrix(matrix);
            }
        }
    }

    public int[] convertColors(long[] colorLongs) {
        if (colorLongs.length < 2) {
            return null;
        }
        int[] colors = new int[colorLongs.length];
        for (int i = 0; i < colorLongs.length; i++) {
            colors[i] = Color.toArgb(colorLongs[i]);
        }
        return colors;
    }
}
