package androidx.test.orchestrator.listeners;

public final class OrchestrationListenerManager {

    public enum TestEvent {
        TEST_RUN_STARTED,
        TEST_RUN_FINISHED,
        TEST_STARTED,
        TEST_FINISHED,
        TEST_FAILURE,
        TEST_ASSUMPTION_FAILURE,
        TEST_IGNORED
    }
}
