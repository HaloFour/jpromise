package org.jpromise.ws.rs.container;

import org.jpromise.Promise;
import org.jpromise.functions.OnCompleted;

import javax.ws.rs.container.AsyncResponse;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

/**
 * Callback that can resume the a suspended {@link javax.ws.rs.container.AsyncResponse} with the result of the
 * completion of a {@link org.jpromise.Promise}.
 * @param <RESPONSE> The type of the response.  It can be either a general-purpose {@link javax.ws.rs.core.Response}
 *                   or the anticipated response entity type.
 */
public class ResumeResponse<RESPONSE> implements OnCompleted<RESPONSE> {
    private final AsyncResponse response;

    /**
     * Initializes a new instance of the {@link org.jpromise.ws.rs.container.ResumeResponse} class with the
     * specified {@link javax.ws.rs.container.AsyncResponse}.
     * @param response The suspected {@link javax.ws.rs.container.AsyncResponse} to be resumed on the completion
     *                 of the {@link org.jpromise.Promise}.
     */
    public ResumeResponse(AsyncResponse response) {
        if (response == null) throw new IllegalArgumentException(mustNotBeNull("response"));
        this.response = response;
    }

    /**
     * Resumes the suspended {@link javax.ws.rs.container.AsyncResponse} using the completed result of the
     * {@link org.jpromise.Promise} to which this callback has been registered.
     * @param promise The promise that has completed.
     * @param result The result of the promise if the promise has fulfilled successfully, otherwise {@code null}.
     * @param exception The exception that caused the promise to be rejected, otherwise {@code null}.
     * @throws Throwable
     */
    @Override
    public final void completed(Promise<RESPONSE> promise, RESPONSE result, Throwable exception) throws Throwable {
        switch (promise.state()) {
            case FULFILLED:
                response.resume(result);
                break;
            case REJECTED:
                response.resume(exception);
                break;
        }
    }
}
