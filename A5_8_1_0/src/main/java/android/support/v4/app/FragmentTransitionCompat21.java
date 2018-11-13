package android.support.v4.app;

import android.graphics.Rect;
import android.transition.Transition;
import android.transition.Transition.EpicenterCallback;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

class FragmentTransitionCompat21 {

    public interface ViewRetriever {
        View getView();
    }

    public static class EpicenterView {
        public View epicenter;
    }

    FragmentTransitionCompat21() {
    }

    public static String getTransitionName(View view) {
        return view.getTransitionName();
    }

    public static Object cloneTransition(Object transition) {
        if (transition != null) {
            return ((Transition) transition).clone();
        }
        return transition;
    }

    public static Object captureExitingViews(Object exitTransition, View root, ArrayList<View> viewList, Map<String, View> namedViews) {
        if (exitTransition == null) {
            return exitTransition;
        }
        captureTransitioningViews(viewList, root);
        if (namedViews != null) {
            viewList.removeAll(namedViews.values());
        }
        if (viewList.isEmpty()) {
            return null;
        }
        addTargets((Transition) exitTransition, viewList);
        return exitTransition;
    }

    public static void excludeTarget(Object transitionObject, View view, boolean exclude) {
        ((Transition) transitionObject).excludeTarget(view, exclude);
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot, Object transitionObject) {
        TransitionManager.beginDelayedTransition(sceneRoot, (Transition) transitionObject);
    }

    public static void setEpicenter(Object transitionObject, View view) {
        Transition transition = (Transition) transitionObject;
        final Rect epicenter = getBoundsOnScreen(view);
        transition.setEpicenterCallback(new EpicenterCallback() {
            public Rect onGetEpicenter(Transition transition) {
                return epicenter;
            }
        });
    }

    public static void addTransitionTargets(Object enterTransitionObject, Object sharedElementTransitionObject, View container, ViewRetriever inFragment, View nonExistentView, EpicenterView epicenterView, Map<String, String> nameOverrides, ArrayList<View> enteringViews, Map<String, View> renamedViews, ArrayList<View> sharedElementTargets) {
        if (enterTransitionObject != null || sharedElementTransitionObject != null) {
            final Transition enterTransition = (Transition) enterTransitionObject;
            if (enterTransition != null) {
                enterTransition.addTarget(nonExistentView);
            }
            if (sharedElementTransitionObject != null) {
                addTargets((Transition) sharedElementTransitionObject, sharedElementTargets);
            }
            if (inFragment != null) {
                final View view = container;
                final ViewRetriever viewRetriever = inFragment;
                final Map<String, String> map = nameOverrides;
                final Map<String, View> map2 = renamedViews;
                final ArrayList<View> arrayList = enteringViews;
                container.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                    public boolean onPreDraw() {
                        view.getViewTreeObserver().removeOnPreDrawListener(this);
                        View fragmentView = viewRetriever.getView();
                        if (fragmentView != null) {
                            if (!map.isEmpty()) {
                                FragmentTransitionCompat21.findNamedViews(map2, fragmentView);
                                map2.keySet().retainAll(map.values());
                                for (Entry<String, String> entry : map.entrySet()) {
                                    View view = (View) map2.get((String) entry.getValue());
                                    if (view != null) {
                                        view.setTransitionName((String) entry.getKey());
                                    }
                                }
                            }
                            if (enterTransition != null) {
                                FragmentTransitionCompat21.captureTransitioningViews(arrayList, fragmentView);
                                arrayList.removeAll(map2.values());
                                FragmentTransitionCompat21.addTargets(enterTransition, arrayList);
                            }
                        }
                        return true;
                    }
                });
            }
            setSharedElementEpicenter(enterTransition, epicenterView);
        }
    }

    public static Object mergeTransitions(Object enterTransitionObject, Object exitTransitionObject, Object sharedElementTransitionObject, boolean allowOverlap) {
        boolean overlap = true;
        Transition enterTransition = (Transition) enterTransitionObject;
        Transition exitTransition = (Transition) exitTransitionObject;
        Transition sharedElementTransition = (Transition) sharedElementTransitionObject;
        if (!(enterTransition == null || exitTransition == null)) {
            overlap = allowOverlap;
        }
        if (overlap) {
            Transition transitionSet = new TransitionSet();
            if (enterTransition != null) {
                transitionSet.addTransition(enterTransition);
            }
            if (exitTransition != null) {
                transitionSet.addTransition(exitTransition);
            }
            if (sharedElementTransition != null) {
                transitionSet.addTransition(sharedElementTransition);
            }
            return transitionSet;
        }
        Transition staggered = null;
        if (exitTransition != null && enterTransition != null) {
            staggered = new TransitionSet().addTransition(exitTransition).addTransition(enterTransition).setOrdering(1);
        } else if (exitTransition != null) {
            staggered = exitTransition;
        } else if (enterTransition != null) {
            staggered = enterTransition;
        }
        if (sharedElementTransition == null) {
            return staggered;
        }
        Transition together = new TransitionSet();
        if (staggered != null) {
            together.addTransition(staggered);
        }
        together.addTransition(sharedElementTransition);
        return together;
    }

    private static void setSharedElementEpicenter(Transition transition, final EpicenterView epicenterView) {
        if (transition != null) {
            transition.setEpicenterCallback(new EpicenterCallback() {
                private Rect mEpicenter;

                public Rect onGetEpicenter(Transition transition) {
                    if (this.mEpicenter == null && epicenterView.epicenter != null) {
                        this.mEpicenter = FragmentTransitionCompat21.getBoundsOnScreen(epicenterView.epicenter);
                    }
                    return this.mEpicenter;
                }
            });
        }
    }

    private static Rect getBoundsOnScreen(View view) {
        Rect epicenter = new Rect();
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        epicenter.set(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
        return epicenter;
    }

    private static void captureTransitioningViews(ArrayList<View> transitioningViews, View view) {
        if (view.getVisibility() != 0) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (viewGroup.isTransitionGroup()) {
                transitioningViews.add(viewGroup);
                return;
            }
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                captureTransitioningViews(transitioningViews, viewGroup.getChildAt(i));
            }
            return;
        }
        transitioningViews.add(view);
    }

    public static void findNamedViews(Map<String, View> namedViews, View view) {
        if (view.getVisibility() == 0) {
            String transitionName = view.getTransitionName();
            if (transitionName != null) {
                namedViews.put(transitionName, view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    findNamedViews(namedViews, viewGroup.getChildAt(i));
                }
            }
        }
    }

    public static void cleanupTransitions(View sceneRoot, View nonExistentView, Object enterTransitionObject, ArrayList<View> enteringViews, Object exitTransitionObject, ArrayList<View> exitingViews, Object sharedElementTransitionObject, ArrayList<View> sharedElementTargets, Object overallTransitionObject, ArrayList<View> hiddenViews, Map<String, View> renamedViews) {
        final Transition enterTransition = (Transition) enterTransitionObject;
        final Transition exitTransition = (Transition) exitTransitionObject;
        final Transition sharedElementTransition = (Transition) sharedElementTransitionObject;
        final Transition overallTransition = (Transition) overallTransitionObject;
        if (overallTransition != null) {
            final View view = sceneRoot;
            final View view2 = nonExistentView;
            final ArrayList<View> arrayList = enteringViews;
            final ArrayList<View> arrayList2 = exitingViews;
            final ArrayList<View> arrayList3 = sharedElementTargets;
            final Map<String, View> map = renamedViews;
            final ArrayList<View> arrayList4 = hiddenViews;
            sceneRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (enterTransition != null) {
                        enterTransition.removeTarget(view2);
                        FragmentTransitionCompat21.removeTargets(enterTransition, arrayList);
                    }
                    if (exitTransition != null) {
                        FragmentTransitionCompat21.removeTargets(exitTransition, arrayList2);
                    }
                    if (sharedElementTransition != null) {
                        FragmentTransitionCompat21.removeTargets(sharedElementTransition, arrayList3);
                    }
                    for (Entry<String, View> entry : map.entrySet()) {
                        ((View) entry.getValue()).setTransitionName((String) entry.getKey());
                    }
                    int numViews = arrayList4.size();
                    for (int i = 0; i < numViews; i++) {
                        overallTransition.excludeTarget((View) arrayList4.get(i), false);
                    }
                    overallTransition.excludeTarget(view2, false);
                    return true;
                }
            });
        }
    }

    public static void removeTargets(Object transitionObject, ArrayList<View> views) {
        Transition transition = (Transition) transitionObject;
        int numViews = views.size();
        for (int i = 0; i < numViews; i++) {
            transition.removeTarget((View) views.get(i));
        }
    }

    public static void addTargets(Object transitionObject, ArrayList<View> views) {
        Transition transition = (Transition) transitionObject;
        int numViews = views.size();
        for (int i = 0; i < numViews; i++) {
            transition.addTarget((View) views.get(i));
        }
    }
}
