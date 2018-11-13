package android.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.transition.TransitionUtils.MatrixEvaluator;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.Map;

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
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ChangeImageTransform extends Transition {
    private static Property<ImageView, Matrix> ANIMATED_TRANSFORM_PROPERTY = null;
    private static TypeEvaluator<Matrix> NULL_MATRIX_EVALUATOR = null;
    private static final String PROPNAME_BOUNDS = "android:changeImageTransform:bounds";
    private static final String PROPNAME_MATRIX = "android:changeImageTransform:matrix";
    private static final String TAG = "ChangeImageTransform";
    private static final String[] sTransitionProperties = null;

    /* renamed from: android.transition.ChangeImageTransform$2 */
    static class AnonymousClass2 extends Property<ImageView, Matrix> {
        AnonymousClass2(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void set(ImageView object, Matrix value) {
            object.animateTransform(value);
        }

        public Matrix get(ImageView object) {
            return null;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.transition.ChangeImageTransform.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.transition.ChangeImageTransform.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeImageTransform.<clinit>():void");
    }

    public ChangeImageTransform(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void captureValues(TransitionValues transitionValues) {
        View view = transitionValues.view;
        if ((view instanceof ImageView) && view.getVisibility() == 0) {
            ImageView imageView = (ImageView) view;
            Drawable drawable = imageView.getDrawable();
            if (drawable != null) {
                Object matrix;
                Map<String, Object> values = transitionValues.values;
                Rect bounds = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                values.put(PROPNAME_BOUNDS, bounds);
                if (imageView.getScaleType() == ScaleType.FIT_XY) {
                    Matrix matrix2 = imageView.getImageMatrix();
                    if (matrix2.isIdentity()) {
                        int drawableWidth = drawable.getIntrinsicWidth();
                        int drawableHeight = drawable.getIntrinsicHeight();
                        if (drawableWidth <= 0 || drawableHeight <= 0) {
                            matrix = null;
                        } else {
                            float scaleX = ((float) bounds.width()) / ((float) drawableWidth);
                            float scaleY = ((float) bounds.height()) / ((float) drawableHeight);
                            matrix = new Matrix();
                            matrix.setScale(scaleX, scaleY);
                        }
                    } else {
                        matrix = new Matrix(matrix2);
                    }
                } else {
                    matrix = new Matrix(imageView.getImageMatrix());
                }
                values.put(PROPNAME_MATRIX, matrix);
            }
        }
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
        Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);
        if (startBounds == null || endBounds == null) {
            return null;
        }
        Matrix startMatrix = (Matrix) startValues.values.get(PROPNAME_MATRIX);
        Matrix endMatrix = (Matrix) endValues.values.get(PROPNAME_MATRIX);
        boolean matricesEqual = (startMatrix == null && endMatrix == null) ? true : startMatrix != null ? startMatrix.equals(endMatrix) : false;
        if (startBounds.equals(endBounds) && matricesEqual) {
            return null;
        }
        ObjectAnimator animator;
        ImageView imageView = endValues.view;
        Drawable drawable = imageView.getDrawable();
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth == 0 || drawableHeight == 0) {
            animator = createNullAnimator(imageView);
        } else {
            if (startMatrix == null) {
                startMatrix = Matrix.IDENTITY_MATRIX;
            }
            if (endMatrix == null) {
                endMatrix = Matrix.IDENTITY_MATRIX;
            }
            ANIMATED_TRANSFORM_PROPERTY.set(imageView, startMatrix);
            animator = createMatrixAnimator(imageView, startMatrix, endMatrix);
        }
        return animator;
    }

    private ObjectAnimator createNullAnimator(ImageView imageView) {
        Property property = ANIMATED_TRANSFORM_PROPERTY;
        TypeEvaluator typeEvaluator = NULL_MATRIX_EVALUATOR;
        Matrix[] matrixArr = new Matrix[2];
        matrixArr[0] = null;
        matrixArr[1] = null;
        return ObjectAnimator.ofObject(imageView, property, typeEvaluator, matrixArr);
    }

    private ObjectAnimator createMatrixAnimator(ImageView imageView, Matrix startMatrix, Matrix endMatrix) {
        Property property = ANIMATED_TRANSFORM_PROPERTY;
        TypeEvaluator matrixEvaluator = new MatrixEvaluator();
        Matrix[] matrixArr = new Matrix[2];
        matrixArr[0] = startMatrix;
        matrixArr[1] = endMatrix;
        return ObjectAnimator.ofObject(imageView, property, matrixEvaluator, matrixArr);
    }
}
