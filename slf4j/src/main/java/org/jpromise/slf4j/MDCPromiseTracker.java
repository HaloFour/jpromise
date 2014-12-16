package org.jpromise.slf4j;

import org.jpromise.Promise;
import org.jpromise.PromiseCallbackCompletion;
import org.jpromise.PromiseCallbackListener;
import org.jpromise.PromiseCompositionListener;
import org.slf4j.MDC;

import java.util.Map;

public class MDCPromiseTracker implements PromiseCompositionListener {
    @Override
    public PromiseCallbackListener composingCallback(Promise<?> source, Promise<?> target) {
        final Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return new PromiseCallbackListener() {
            @Override
            public PromiseCallbackCompletion invokingPromiseCallback(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
                final Map<String, String> previousContextMap = MDC.getCopyOfContextMap();
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                else {
                    MDC.clear();
                }
                return new PromiseCallbackCompletion() {
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
