package org.jpromise.patterns;

import org.jpromise.Promise;
import org.jpromise.functions.OnResolved2;
import org.junit.Test;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.assertEquals;

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

        assertResolves(new Pattern2<>(SUCCESS1, SUCCESS2), pattern);
    }

    @Test
    public void join3() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 10);

        Promise<Pattern3<String, String, String>> pattern = Pattern.join(promise1, promise2, promise3);

        assertResolves(new Pattern3<>(SUCCESS1, SUCCESS2, SUCCESS3), pattern);
    }

    @Test
    public void join4() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 10);

        Promise<Pattern4<String, String, String, String>> pattern = Pattern.join(promise1, promise2, promise3, promise4);

        assertResolves(new Pattern4<>(SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4), pattern);
    }

    @Test
    public void join5() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 10);
        Promise<String> promise5 = resolveAfter(SUCCESS5, 10);

        Promise<Pattern5<String, String, String, String, String>> pattern = Pattern.join(promise1, promise2, promise3, promise4, promise5);

        assertResolves(new Pattern5<>(SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4, SUCCESS5), pattern);
    }

    @Test
    public void resolved2() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 10);

        Promise<Pattern2<String, String>> pattern = Pattern.join(promise1, promise2);

        pattern.then(Pattern.then(new OnResolved2<String, String>() {
            @Override
            public void resolved(String item1, String item2) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
            }
        })).get();
    }
}
