package androidx.core.view;

import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowInsets;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewCompat {
    private static boolean sAccessibilityDelegateCheckFailed = false;
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static ThreadLocal<Rect> sThreadLocalRect;
    private static WeakHashMap<View, String> sTransitionNameMap;
    private static WeakHashMap<View, Object> sViewPropertyAnimatorMap = null;

    private static Rect getEmptyTempRect() {
        if (sThreadLocalRect == null) {
            sThreadLocalRect = new ThreadLocal<>();
        }
        Rect rect = sThreadLocalRect.get();
        if (rect == null) {
            rect = new Rect();
            sThreadLocalRect.set(rect);
        }
        rect.setEmpty();
        return rect;
    }

    public static void setAccessibilityDelegate(View v, AccessibilityDelegateCompat delegate) {
        v.setAccessibilityDelegate(delegate == null ? null : delegate.getBridge());
    }

    public static void postInvalidateOnAnimation(View view) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.postInvalidateOnAnimation();
        } else {
            view.postInvalidate();
        }
    }

    public static void postInvalidateOnAnimation(View view, int left, int top, int right, int bottom) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.postInvalidateOnAnimation(left, top, right, bottom);
        } else {
            view.postInvalidate(left, top, right, bottom);
        }
    }

    public static void postOnAnimation(View view, Runnable action) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.postOnAnimation(action);
        } else {
            view.postDelayed(action, ValueAnimator.getFrameDelay());
        }
    }

    public static int getImportantForAccessibility(View view) {
        if (Build.VERSION.SDK_INT >= 16) {
            return view.getImportantForAccessibility();
        }
        return 0;
    }

    public static void setImportantForAccessibility(View view, int mode) {
        if (Build.VERSION.SDK_INT >= 19) {
            view.setImportantForAccessibility(mode);
        } else if (Build.VERSION.SDK_INT >= 16) {
            if (mode == 4) {
                mode = 2;
            }
            view.setImportantForAccessibility(mode);
        }
    }

    public static void setLayerPaint(View view, Paint paint) {
        if (Build.VERSION.SDK_INT >= 17) {
            view.setLayerPaint(paint);
            return;
        }
        view.setLayerType(view.getLayerType(), paint);
        view.invalidate();
    }

    public static int getLayoutDirection(View view) {
        if (Build.VERSION.SDK_INT >= 17) {
            return view.getLayoutDirection();
        }
        return 0;
    }

    public static void setElevation(View view, float elevation) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setElevation(elevation);
        }
    }

    public static float getElevation(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            return view.getElevation();
        }
        return 0.0f;
    }

    public static void setTransitionName(View view, String transitionName) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setTransitionName(transitionName);
            return;
        }
        if (sTransitionNameMap == null) {
            sTransitionNameMap = new WeakHashMap<>();
        }
        sTransitionNameMap.put(view, transitionName);
    }

    public static String getTransitionName(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            return view.getTransitionName();
        }
        if (sTransitionNameMap == null) {
            return null;
        }
        return sTransitionNameMap.get(view);
    }

    public static void requestApplyInsets(View view) {
        if (Build.VERSION.SDK_INT >= 20) {
            view.requestApplyInsets();
        } else if (Build.VERSION.SDK_INT >= 16) {
            view.requestFitSystemWindows();
        }
    }

    public static boolean getFitsSystemWindows(View v) {
        if (Build.VERSION.SDK_INT >= 16) {
            return v.getFitsSystemWindows();
        }
        return false;
    }

    public static void setOnApplyWindowInsetsListener(View v, final OnApplyWindowInsetsListener listener) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        if (listener == null) {
            v.setOnApplyWindowInsetsListener(null);
        } else {
            v.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                /* class androidx.core.view.ViewCompat.AnonymousClass1 */

                public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                    return (WindowInsets) WindowInsetsCompat.unwrap(listener.onApplyWindowInsets(view, WindowInsetsCompat.wrap(insets)));
                }
            });
        }
    }

    public static boolean hasOverlappingRendering(View view) {
        if (Build.VERSION.SDK_INT >= 16) {
            return view.hasOverlappingRendering();
        }
        return true;
    }

    public static void stopNestedScroll(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.stopNestedScroll();
        } else if (view instanceof NestedScrollingChild) {
            ((NestedScrollingChild) view).stopNestedScroll();
        }
    }

    public static boolean isLaidOut(View view) {
        if (Build.VERSION.SDK_INT >= 19) {
            return view.isLaidOut();
        }
        return view.getWidth() > 0 && view.getHeight() > 0;
    }

    public static float getZ(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            return view.getZ();
        }
        return 0.0f;
    }

    public static void offsetTopAndBottom(View view, int offset) {
        if (Build.VERSION.SDK_INT >= 23) {
            view.offsetTopAndBottom(offset);
        } else if (Build.VERSION.SDK_INT >= 21) {
            Rect parentRect = getEmptyTempRect();
            boolean needInvalidateWorkaround = false;
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                View p = (View) parent;
                parentRect.set(p.getLeft(), p.getTop(), p.getRight(), p.getBottom());
                needInvalidateWorkaround = !parentRect.intersects(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
            }
            compatOffsetTopAndBottom(view, offset);
            if (needInvalidateWorkaround && parentRect.intersect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())) {
                ((View) parent).invalidate(parentRect);
            }
        } else {
            compatOffsetTopAndBottom(view, offset);
        }
    }

    private static void compatOffsetTopAndBottom(View view, int offset) {
        view.offsetTopAndBottom(offset);
        if (view.getVisibility() == 0) {
            tickleInvalidationFlag(view);
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                tickleInvalidationFlag((View) parent);
            }
        }
    }

    public static void offsetLeftAndRight(View view, int offset) {
        if (Build.VERSION.SDK_INT >= 23) {
            view.offsetLeftAndRight(offset);
        } else if (Build.VERSION.SDK_INT >= 21) {
            Rect parentRect = getEmptyTempRect();
            boolean needInvalidateWorkaround = false;
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                View p = (View) parent;
                parentRect.set(p.getLeft(), p.getTop(), p.getRight(), p.getBottom());
                needInvalidateWorkaround = !parentRect.intersects(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
            }
            compatOffsetLeftAndRight(view, offset);
            if (needInvalidateWorkaround && parentRect.intersect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())) {
                ((View) parent).invalidate(parentRect);
            }
        } else {
            compatOffsetLeftAndRight(view, offset);
        }
    }

    private static void compatOffsetLeftAndRight(View view, int offset) {
        view.offsetLeftAndRight(offset);
        if (view.getVisibility() == 0) {
            tickleInvalidationFlag(view);
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                tickleInvalidationFlag((View) parent);
            }
        }
    }

    private static void tickleInvalidationFlag(View view) {
        float y = view.getTranslationY();
        view.setTranslationY(1.0f + y);
        view.setTranslationY(y);
    }

    public static boolean isAttachedToWindow(View view) {
        if (Build.VERSION.SDK_INT >= 19) {
            return view.isAttachedToWindow();
        }
        return view.getWindowToken() != null;
    }
}
