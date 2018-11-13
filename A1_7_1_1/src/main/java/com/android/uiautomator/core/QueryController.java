package com.android.uiautomator.core;

import android.app.UiAutomation.OnAccessibilityEventListener;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

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
class QueryController {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = null;
    private static final boolean VERBOSE = false;
    private final Object mFindNodeLock;
    private String mLastActivityName;
    private String mLastTraversedText;
    private final Object mLock;
    private int mLogIndent;
    private int mLogParentIndent;
    private int mPatternCounter;
    private int mPatternIndexer;
    private final UiAutomatorBridge mUiAutomatorBridge;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.uiautomator.core.QueryController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.uiautomator.core.QueryController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.uiautomator.core.QueryController.<clinit>():void");
    }

    public QueryController(UiAutomatorBridge bridge) {
        this.mLock = new Object();
        this.mFindNodeLock = new Object();
        this.mLastActivityName = null;
        this.mPatternCounter = 0;
        this.mPatternIndexer = 0;
        this.mLogIndent = 0;
        this.mLogParentIndent = 0;
        this.mLastTraversedText = "";
        this.mUiAutomatorBridge = bridge;
        bridge.setOnAccessibilityEventListener(new OnAccessibilityEventListener() {
            public void onAccessibilityEvent(AccessibilityEvent event) {
                synchronized (QueryController.this.mLock) {
                    switch (event.getEventType()) {
                        case 32:
                            if (!(event.getText() == null || event.getText().size() <= 0 || event.getText().get(0) == null)) {
                                QueryController.this.mLastActivityName = ((CharSequence) event.getText().get(0)).toString();
                                break;
                            }
                        case 131072:
                            if (!(event.getText() == null || event.getText().size() <= 0 || event.getText().get(0) == null)) {
                                QueryController.this.mLastTraversedText = ((CharSequence) event.getText().get(0)).toString();
                            }
                            if (QueryController.DEBUG) {
                                Log.d(QueryController.LOG_TAG, "Last text selection reported: " + QueryController.this.mLastTraversedText);
                                break;
                            }
                            break;
                    }
                    QueryController.this.mLock.notifyAll();
                }
            }
        });
    }

    public String getLastTraversedText() {
        this.mUiAutomatorBridge.waitForIdle();
        synchronized (this.mLock) {
            if (this.mLastTraversedText.length() > 0) {
                String str = this.mLastTraversedText;
                return str;
            }
            return null;
        }
    }

    public void clearLastTraversedText() {
        this.mUiAutomatorBridge.waitForIdle();
        synchronized (this.mLock) {
            this.mLastTraversedText = "";
        }
    }

    private void initializeNewSearch() {
        this.mPatternCounter = 0;
        this.mPatternIndexer = 0;
        this.mLogIndent = 0;
        this.mLogParentIndent = 0;
    }

    public int getPatternCount(UiSelector selector) {
        findAccessibilityNodeInfo(selector, true);
        return this.mPatternCounter;
    }

    public AccessibilityNodeInfo findAccessibilityNodeInfo(UiSelector selector) {
        return findAccessibilityNodeInfo(selector, false);
    }

    protected AccessibilityNodeInfo findAccessibilityNodeInfo(UiSelector selector, boolean isCounting) {
        this.mUiAutomatorBridge.waitForIdle();
        initializeNewSearch();
        if (DEBUG) {
            Log.d(LOG_TAG, "Searching: " + selector);
        }
        synchronized (this.mFindNodeLock) {
            AccessibilityNodeInfo rootNode = getRootNode();
            if (rootNode == null) {
                Log.e(LOG_TAG, "Cannot proceed when root node is null. Aborted search");
                return null;
            }
            AccessibilityNodeInfo translateCompoundSelector = translateCompoundSelector(new UiSelector(selector), rootNode, isCounting);
            return translateCompoundSelector;
        }
    }

    protected AccessibilityNodeInfo getRootNode() {
        AccessibilityNodeInfo rootNode = null;
        for (int x = 0; x < 4; x++) {
            rootNode = this.mUiAutomatorBridge.getRootInActiveWindow();
            if (rootNode != null) {
                return rootNode;
            }
            if (x < 3) {
                Log.e(LOG_TAG, "Got null root node from accessibility - Retrying...");
                SystemClock.sleep(250);
            }
        }
        return rootNode;
    }

    private AccessibilityNodeInfo translateCompoundSelector(UiSelector selector, AccessibilityNodeInfo fromNode, boolean isCounting) {
        if (!selector.hasContainerSelector()) {
            fromNode = translateReqularSelector(selector, fromNode);
        } else if (selector.getContainerSelector().hasContainerSelector()) {
            fromNode = translateCompoundSelector(selector.getContainerSelector(), fromNode, false);
            initializeNewSearch();
        } else {
            fromNode = translateReqularSelector(selector.getContainerSelector(), fromNode);
        }
        if (fromNode == null) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Container selector not found: " + selector.dumpToString(false));
            }
            return null;
        }
        String str;
        Object[] objArr;
        if (selector.hasPatternSelector()) {
            fromNode = translatePatternSelector(selector.getPatternSelector(), fromNode, isCounting);
            if (isCounting) {
                str = LOG_TAG;
                objArr = new Object[2];
                objArr[0] = Integer.valueOf(this.mPatternCounter);
                objArr[1] = selector;
                Log.i(str, String.format("Counted %d instances of: %s", objArr));
                return null;
            } else if (fromNode == null) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Pattern selector not found: " + selector.dumpToString(false));
                }
                return null;
            }
        }
        if ((selector.hasContainerSelector() || selector.hasPatternSelector()) && (selector.hasChildSelector() || selector.hasParentSelector())) {
            fromNode = translateReqularSelector(selector, fromNode);
        }
        if (fromNode == null) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Object Not Found for selector " + selector);
            }
            return null;
        }
        str = LOG_TAG;
        objArr = new Object[2];
        objArr[0] = selector;
        objArr[1] = fromNode;
        Log.i(str, String.format("Matched selector: %s <<==>> [%s]", objArr));
        return fromNode;
    }

    private AccessibilityNodeInfo translateReqularSelector(UiSelector selector, AccessibilityNodeInfo fromNode) {
        return findNodeRegularRecursive(selector, fromNode, 0);
    }

    private AccessibilityNodeInfo findNodeRegularRecursive(UiSelector subSelector, AccessibilityNodeInfo fromNode, int index) {
        String str;
        Object[] objArr;
        if (subSelector.isMatchFor(fromNode, index)) {
            if (DEBUG) {
                str = LOG_TAG;
                objArr = new Object[1];
                objArr[0] = subSelector.dumpToString(false);
                Log.d(str, formatLog(String.format("%s", objArr)));
            }
            if (subSelector.isLeaf()) {
                return fromNode;
            }
            if (subSelector.hasChildSelector()) {
                this.mLogIndent++;
                subSelector = subSelector.getChildSelector();
                if (subSelector == null) {
                    Log.e(LOG_TAG, "Error: A child selector without content");
                    return null;
                }
            } else if (subSelector.hasParentSelector()) {
                this.mLogIndent++;
                subSelector = subSelector.getParentSelector();
                if (subSelector == null) {
                    Log.e(LOG_TAG, "Error: A parent selector without content");
                    return null;
                }
                fromNode = fromNode.getParent();
                if (fromNode == null) {
                    return null;
                }
            }
        }
        int childCount = fromNode.getChildCount();
        boolean hasNullChild = false;
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = fromNode.getChild(i);
            if (childNode == null) {
                str = LOG_TAG;
                objArr = new Object[2];
                objArr[0] = Integer.valueOf(i);
                objArr[1] = Integer.valueOf(childCount);
                Log.w(str, String.format("AccessibilityNodeInfo returned a null child (%d of %d)", objArr));
                if (!hasNullChild) {
                    str = LOG_TAG;
                    objArr = new Object[1];
                    objArr[0] = fromNode.toString();
                    Log.w(str, String.format("parent = %s", objArr));
                }
                hasNullChild = true;
            } else if (childNode.isVisibleToUser()) {
                AccessibilityNodeInfo retNode = findNodeRegularRecursive(subSelector, childNode, i);
                if (retNode != null) {
                    return retNode;
                }
            } else if (VERBOSE) {
                str = LOG_TAG;
                objArr = new Object[1];
                objArr[0] = childNode.toString();
                Log.v(str, String.format("Skipping invisible child: %s", objArr));
            }
        }
        return null;
    }

    private AccessibilityNodeInfo translatePatternSelector(UiSelector subSelector, AccessibilityNodeInfo fromNode, boolean isCounting) {
        if (subSelector.hasPatternSelector()) {
            if (isCounting) {
                this.mPatternIndexer = -1;
            } else {
                this.mPatternIndexer = subSelector.getInstance();
            }
            subSelector = subSelector.getPatternSelector();
            if (subSelector == null) {
                Log.e(LOG_TAG, "Pattern portion of the selector is null or not defined");
                return null;
            }
            int i = this.mLogIndent + 1;
            this.mLogIndent = i;
            this.mLogParentIndent = i;
            return findNodePatternRecursive(subSelector, fromNode, 0, subSelector);
        }
        Log.e(LOG_TAG, "Selector must have a pattern selector defined");
        return null;
    }

    private AccessibilityNodeInfo findNodePatternRecursive(UiSelector subSelector, AccessibilityNodeInfo fromNode, int index, UiSelector originalPattern) {
        String str;
        Object[] objArr;
        if (subSelector.isMatchFor(fromNode, index)) {
            if (!subSelector.isLeaf()) {
                if (DEBUG) {
                    str = LOG_TAG;
                    objArr = new Object[1];
                    objArr[0] = subSelector.dumpToString(false);
                    Log.d(str, formatLog(String.format("%s", objArr)));
                }
                if (subSelector.hasChildSelector()) {
                    this.mLogIndent++;
                    subSelector = subSelector.getChildSelector();
                    if (subSelector == null) {
                        Log.e(LOG_TAG, "Error: A child selector without content");
                        return null;
                    }
                } else if (subSelector.hasParentSelector()) {
                    this.mLogIndent++;
                    subSelector = subSelector.getParentSelector();
                    if (subSelector == null) {
                        Log.e(LOG_TAG, "Error: A parent selector without content");
                        return null;
                    }
                    fromNode = fromNode.getParent();
                    if (fromNode == null) {
                        return null;
                    }
                }
            } else if (this.mPatternIndexer == 0) {
                if (DEBUG) {
                    str = LOG_TAG;
                    objArr = new Object[1];
                    objArr[0] = subSelector.dumpToString(false);
                    Log.d(str, formatLog(String.format("%s", objArr)));
                }
                return fromNode;
            } else {
                if (DEBUG) {
                    str = LOG_TAG;
                    objArr = new Object[1];
                    objArr[0] = subSelector.dumpToString(false);
                    Log.d(str, formatLog(String.format("%s", objArr)));
                }
                this.mPatternCounter++;
                this.mPatternIndexer--;
                subSelector = originalPattern;
                this.mLogIndent = this.mLogParentIndent;
            }
        }
        int childCount = fromNode.getChildCount();
        boolean hasNullChild = false;
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = fromNode.getChild(i);
            if (childNode == null) {
                str = LOG_TAG;
                objArr = new Object[2];
                objArr[0] = Integer.valueOf(i);
                objArr[1] = Integer.valueOf(childCount);
                Log.w(str, String.format("AccessibilityNodeInfo returned a null child (%d of %d)", objArr));
                if (!hasNullChild) {
                    str = LOG_TAG;
                    objArr = new Object[1];
                    objArr[0] = fromNode.toString();
                    Log.w(str, String.format("parent = %s", objArr));
                }
                hasNullChild = true;
            } else if (childNode.isVisibleToUser()) {
                AccessibilityNodeInfo retNode = findNodePatternRecursive(subSelector, childNode, i, originalPattern);
                if (retNode != null) {
                    return retNode;
                }
            } else if (DEBUG) {
                str = LOG_TAG;
                objArr = new Object[1];
                objArr[0] = childNode.toString();
                Log.d(str, String.format("Skipping invisible child: %s", objArr));
            }
        }
        return null;
    }

    public AccessibilityNodeInfo getAccessibilityRootNode() {
        return this.mUiAutomatorBridge.getRootInActiveWindow();
    }

    @Deprecated
    public String getCurrentActivityName() {
        String str;
        this.mUiAutomatorBridge.waitForIdle();
        synchronized (this.mLock) {
            str = this.mLastActivityName;
        }
        return str;
    }

    public String getCurrentPackageName() {
        String str = null;
        this.mUiAutomatorBridge.waitForIdle();
        AccessibilityNodeInfo rootNode = getRootNode();
        if (rootNode == null) {
            return null;
        }
        if (rootNode.getPackageName() != null) {
            str = rootNode.getPackageName().toString();
        }
        return str;
    }

    private String formatLog(String str) {
        StringBuilder l = new StringBuilder();
        for (int space = 0; space < this.mLogIndent; space++) {
            l.append(". . ");
        }
        Object[] objArr;
        if (this.mLogIndent > 0) {
            objArr = new Object[2];
            objArr[0] = Integer.valueOf(this.mPatternCounter);
            objArr[1] = str;
            l.append(String.format(". . [%d]: %s", objArr));
        } else {
            objArr = new Object[2];
            objArr[0] = Integer.valueOf(this.mPatternCounter);
            objArr[1] = str;
            l.append(String.format(". . [%d]: %s", objArr));
        }
        return l.toString();
    }
}
