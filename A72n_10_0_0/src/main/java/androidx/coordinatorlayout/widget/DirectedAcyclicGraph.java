package androidx.coordinatorlayout.widget;

import androidx.collection.SimpleArrayMap;
import androidx.core.util.Pools;
import java.util.ArrayList;
import java.util.HashSet;

public final class DirectedAcyclicGraph<T> {
    private final SimpleArrayMap<T, ArrayList<T>> mGraph;
    private final Pools.Pool<ArrayList<T>> mListPool;
    private final ArrayList<T> mSortResult;
    private final HashSet<T> mSortTmpMarked;

    public void addNode(T node) {
        if (!this.mGraph.containsKey(node)) {
            this.mGraph.put(node, null);
        }
    }

    public boolean contains(T node) {
        return this.mGraph.containsKey(node);
    }

    public void addEdge(T node, T incomingEdge) {
        if (!this.mGraph.containsKey(node) || !this.mGraph.containsKey(incomingEdge)) {
            throw new IllegalArgumentException("All nodes must be present in the graph before being added as an edge");
        }
        ArrayList<T> edges = this.mGraph.get(node);
        if (edges == null) {
            edges = getEmptyList();
            this.mGraph.put(node, edges);
        }
        edges.add(incomingEdge);
    }

    public boolean hasOutgoingEdges(T node) {
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList<T> edges = this.mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList<T> edges = this.mGraph.valueAt(i);
            if (edges != null) {
                poolList(edges);
            }
        }
        this.mGraph.clear();
    }

    public ArrayList<T> getSortedList() {
        this.mSortResult.clear();
        this.mSortTmpMarked.clear();
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            dfs(this.mGraph.keyAt(i), this.mSortResult, this.mSortTmpMarked);
        }
        return this.mSortResult;
    }

    private void dfs(T node, ArrayList<T> result, HashSet<T> tmpMarked) {
        if (!result.contains(node)) {
            if (!tmpMarked.contains(node)) {
                tmpMarked.add(node);
                ArrayList<T> edges = this.mGraph.get(node);
                if (edges != null) {
                    int size = edges.size();
                    for (int i = 0; i < size; i++) {
                        dfs(edges.get(i), result, tmpMarked);
                    }
                }
                tmpMarked.remove(node);
                result.add(node);
                return;
            }
            throw new RuntimeException("This graph contains cyclic dependencies");
        }
    }

    private ArrayList<T> getEmptyList() {
        ArrayList<T> list = this.mListPool.acquire();
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private void poolList(ArrayList<T> list) {
        list.clear();
        this.mListPool.release(list);
    }
}
