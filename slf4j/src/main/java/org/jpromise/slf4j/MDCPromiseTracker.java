package org.jpromise.slf4j;

import org.jpromise.Promise;
import org.jpromise.PromiseContinuationCompletion;
import org.jpromise.PromiseContinuationListener;
import org.jpromise.PromiseCompositionListener;
import org.slf4j.MDC;

import java.util.Map;

public class MDCPromiseTracker implements PromiseCompositionListener {
    @Override
    public PromiseContinuationListener composingContinuation(Promise<?> source, Promise<?> target) {
        final Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return new PromiseContinuationListener() {
            @Override
            public PromiseContinuationCompletion invokingContinuation(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
                final Map<String, String> previousContextMap = MDC.getCopyOfContextMap();
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                else {
                    MDC.clear();
                }
                return new PromiseContinuationCompletion() {
                    @Override
                    public void completed(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
                        if (previousContextMap != null) {
                            MDC.setContextMap(previousContextMap);
                        }
                        else {
                            MDC.clear();
                        }
                    }
                };
            }
        };
    }
}
