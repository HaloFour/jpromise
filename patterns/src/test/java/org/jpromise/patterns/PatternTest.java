package org.jpromise.patterns;

import org.jpromise.Promise;
import org.junit.Test;

import static org.jpromise.PromiseHelpers.*;

public class PatternTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final String SUCCESS2 = "SUCCESS2";
    private static final String SUCCESS3 = "SUCCESS3";
    private static final String SUCCESS4 = "SUCCESS4";
    private static final String SUCCESS5 = "SUCCESS5";

    @Test
    public void join2() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 10);

        Promise<Pattern2<String, String>> pattern = Pattern.join(promise1, promise2);

        assertResolves(new Pattern2<String, String>(SUCCESS1, SUCCESS2), pattern);
    }
}
