package org.jpromise.patterns;

import org.jpromise.Promise;
import org.jpromise.functions.*;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.assertEquals;

public class PatternTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final String SUCCESS2 = "SUCCESS2";
    private static final String SUCCESS3 = "SUCCESS3";
    private static final String SUCCESS4 = "SUCCESS4";
    private static final String SUCCESS5 = "SUCCESS5";

    private Promise<Pattern2<String, String>> get2() {
        return Pattern.join(resolveAfter(SUCCESS1, 10), resolveAfter(SUCCESS2, 10));
    }

    private Promise<Pattern3<String, String, String>> get3() {
        return Pattern.join(resolveAfter(SUCCESS1, 10), resolveAfter(SUCCESS2, 10), resolveAfter(SUCCESS3, 10));
    }

    private Promise<Pattern4<String, String, String, String>> get4() {
        return Pattern.join(resolveAfter(SUCCESS1, 10), resolveAfter(SUCCESS2, 10), resolveAfter(SUCCESS3, 10), resolveAfter(SUCCESS4, 10));
    }

    private Promise<Pattern5<String, String, String, String, String>> get5() {
        return Pattern.join(resolveAfter(SUCCESS1, 10), resolveAfter(SUCCESS2, 10), resolveAfter(SUCCESS3, 10), resolveAfter(SUCCESS4, 10), resolveAfter(SUCCESS5, 10));
    }

    @Test
    public void join2() throws Throwable {
        assertResolves(new Pattern2<>(SUCCESS1, SUCCESS2), get2());
    }

    @Test
    public void join3() throws Throwable {
        assertResolves(new Pattern3<>(SUCCESS1, SUCCESS2, SUCCESS3), get3());
    }

    @Test
    public void join4() throws Throwable {
        assertResolves(new Pattern4<>(SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4), get4());
    }

    @Test
    public void join5() throws Throwable {
        assertResolves(new Pattern5<>(SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4, SUCCESS5), get5());
    }

    @Test
    public void spread2() throws Throwable {
        get2().then(Pattern.spread2(new OnResolved2<String, String>() {
                @Override
            public void resolved(String item1, String item2) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
            }
        })).get();
    }

    @Test
    public void spread3() throws Throwable {
        get3().then(Pattern.spread3(new OnResolved3<String, String, String>() {
            @Override
            public void resolved(String item1, String item2, String item3) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
            }
        })).get();
    }

    @Test
    public void spread4() throws Throwable {
        get4().then(Pattern.spread4(new OnResolved4<String, String, String, String>() {
            @Override
            public void resolved(String item1, String item2, String item3, String item4) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
            }
        })).get();
    }

    @Test
    public void spread5() throws Throwable {
        get5().then(Pattern.spread5(new OnResolved5<String, String, String, String, String>() {
            @Override
            public void resolved(String item1, String item2, String item3, String item4, String item5) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                assertEquals(SUCCESS5, item5);
            }
        })).get();
    }

    @Test
    public void apply2() throws Throwable {
        Promise<String> promise = get2().thenApply(Pattern.apply2(new OnResolvedFunction2<String, String, String>() {
            @Override
            public String resolved(String item1, String item2) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                return item1 + item2;
            }
        }));

        assertResolves(SUCCESS1 + SUCCESS2, promise);
    }

    @Test
    public void apply3() throws Throwable {
        Promise<String> promise = get3().thenApply(Pattern.apply3(new OnResolvedFunction3<String, String, String, String>() {
            @Override
            public String resolved(String item1, String item2, String item3) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                return item1 + item2 + item3;
            }
        }));

        assertResolves(SUCCESS1 + SUCCESS2 + SUCCESS3, promise);
    }

    @Test
    public void apply4() throws Throwable {
        Promise<String> promise = get4().thenApply(Pattern.apply4(new OnResolvedFunction4<String, String, String, String, String>() {
            @Override
            public String resolved(String item1, String item2, String item3, String item4) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                return item1 + item2 + item3 + item4;
            }
        }));

        assertResolves(SUCCESS1 + SUCCESS2 + SUCCESS3 + SUCCESS4, promise);
    }

    @Test
    public void apply5() throws Throwable {
        Promise<String> promise = get5().thenApply(Pattern.apply5(new OnResolvedFunction5<String, String, String, String, String, String>() {
            @Override
            public String resolved(String item1, String item2, String item3, String item4, String item5) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                assertEquals(SUCCESS5, item5);
                return item1 + item2 + item3 + item4 + item5;
            }
        }));

        assertResolves(SUCCESS1 + SUCCESS2 + SUCCESS3 + SUCCESS4 + SUCCESS5, promise);
    }

    @Test
    public void compose2() throws Throwable {
        Promise<String> promise = get2().thenCompose(Pattern.compose2(new OnResolvedFunction2<String, String, Future<String>>() {
            @Override
            public Future<String> resolved(String item1, String item2) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                return resolveAfter(item1 + item2, 10);
            }
        }));

        assertResolves(SUCCESS1 + SUCCESS2, promise);
    }

    @Test
    public void compose3() throws Throwable {
        Promise<String> promise = get3().thenCompose(Pattern.apply3(new OnResolvedFunction3<String, String, String, Future<String>>() {
            @Override
            public Future<String> resolved(String item1, String item2, String item3) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                return resolveAfter(item1 + item2 + item3, 10);
            }
        }));

        assertResolves(SUCCESS1 + SUCCESS2 + SUCCESS3, promise);
    }

    @Test
    public void compose4() throws Throwable {
        Promise<String> promise = get4().thenCompose(Pattern.compose4(new OnResolvedFunction4<String, String, String, String, Future<String>>() {
            @Override
            public Future<String> resolved(String item1, String item2, String item3, String item4) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                return resolveAfter(item1 + item2 + item3 + item4, 10);
            }
        }));

        assertResolves(SUCCESS1 + SUCCESS2 + SUCCESS3 + SUCCESS4, promise);
    }

    @Test
    public void compose5() throws Throwable {
        Promise<String> promise = get5().thenCompose(Pattern.compose5(new OnResolvedFunction5<String, String, String, String, String, Future<String>>() {
            @Override
            public Future<String> resolved(String item1, String item2, String item3, String item4, String item5) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                assertEquals(SUCCESS5, item5);
                return resolveAfter(item1 + item2 + item3 + item4 + item5, 10);
            }
        }));

        assertResolves(SUCCESS1 + SUCCESS2 + SUCCESS3 + SUCCESS4 + SUCCESS5, promise);
    }
}
