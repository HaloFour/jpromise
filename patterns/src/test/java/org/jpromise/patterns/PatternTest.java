package org.jpromise.patterns;

import org.jpromise.Promise;
import org.jpromise.Promises;
import org.jpromise.functions.*;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.Future;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;

public class PatternTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final String SUCCESS2 = "SUCCESS2";
    private static final String SUCCESS3 = "SUCCESS3";
    private static final String SUCCESS4 = "SUCCESS4";
    private static final String SUCCESS5 = "SUCCESS5";

    private Promise<Pattern2<String, String>> get2() {
        return Pattern.join(fulfillAfter(SUCCESS1, 10), fulfillAfter(SUCCESS2, 10));
    }

    private Promise<Pattern3<String, String, String>> get3() {
        return Pattern.join(fulfillAfter(SUCCESS1, 10), fulfillAfter(SUCCESS2, 10), fulfillAfter(SUCCESS3, 10));
    }

    private Promise<Pattern4<String, String, String, String>> get4() {
        return Pattern.join(fulfillAfter(SUCCESS1, 10), fulfillAfter(SUCCESS2, 10), fulfillAfter(SUCCESS3, 10), fulfillAfter(SUCCESS4, 10));
    }

    private Promise<Pattern5<String, String, String, String, String>> get5() {
        return Pattern.join(fulfillAfter(SUCCESS1, 10), fulfillAfter(SUCCESS2, 10), fulfillAfter(SUCCESS3, 10), fulfillAfter(SUCCESS4, 10), fulfillAfter(SUCCESS5, 10));
    }

    @Test(expected = IllegalStateException.class)
    public void PatternCannotBeCreated() throws Throwable {
        Class<Pattern> patternClass = Pattern.class;
        Constructor<?>[] constructors = patternClass.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        Constructor<?> constructor = constructors[0];
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        }
        catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    @Test
    public void join2() throws Throwable {
        Pattern2<String, String> pattern = assertFulfills(Pattern.of(SUCCESS1, SUCCESS2), get2());
        assertEquals(SUCCESS1, pattern.get(0));
        assertEquals(SUCCESS2, pattern.get(1));
        assertEquals("( " + SUCCESS1 + ", " + SUCCESS2 + " )", pattern.toString());
    }

    @Test
    public void join3() throws Throwable {
        Pattern3<String, String, String> pattern = assertFulfills(Pattern.of(SUCCESS1, SUCCESS2, SUCCESS3), get3());
        assertEquals(SUCCESS1, pattern.get(0));
        assertEquals(SUCCESS2, pattern.get(1));
        assertEquals(SUCCESS3, pattern.get(2));
        assertEquals("( " + SUCCESS1 + ", " + SUCCESS2 + ", " + SUCCESS3 + " )", pattern.toString());
    }

    @Test
    public void join4() throws Throwable {
        Pattern4<String, String, String, String> pattern = assertFulfills(Pattern.of(SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4), get4());
        assertEquals(SUCCESS1, pattern.get(0));
        assertEquals(SUCCESS2, pattern.get(1));
        assertEquals(SUCCESS3, pattern.get(2));
        assertEquals(SUCCESS4, pattern.get(3));
        assertEquals("( " + SUCCESS1 + ", " + SUCCESS2 + ", " + SUCCESS3 + ", " + SUCCESS4 + " )", pattern.toString());
    }

    @Test
    public void join5() throws Throwable {
        Pattern5<String, String, String, String, String> pattern = assertFulfills(Pattern.of(SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4, SUCCESS5), get5());
        assertEquals(SUCCESS1, pattern.get(0));
        assertEquals(SUCCESS2, pattern.get(1));
        assertEquals(SUCCESS3, pattern.get(2));
        assertEquals(SUCCESS4, pattern.get(3));
        assertEquals(SUCCESS5, pattern.get(4));
        assertEquals("( " + SUCCESS1 + ", " + SUCCESS2 + ", " + SUCCESS3 + ", " + SUCCESS4 + ", " + SUCCESS5 + " )", pattern.toString());
    }

    @Test
    public void joinWithNullPromise() throws Throwable {
        Promise<Pattern2<String, String>> promise = Pattern.join(Promises.fulfilled(SUCCESS1), null);
        assertFulfills(Pattern.<String, String>of(SUCCESS1, null), promise);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getUnderBoundsThrows() throws Throwable {
        Pattern2<String, String> pattern = Pattern.of(SUCCESS1, SUCCESS2);
        Object ignored = pattern.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getOverBoundsThrows() throws Throwable {
        Pattern2<String, String> pattern = Pattern.of(SUCCESS1, SUCCESS2);
        Object ignored = pattern.get(2);
    }

    @Test
    public void getHashCodeImpl() {
        Pattern5<String, String, String, String, String> pattern1 = Pattern.of(SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4, SUCCESS5);
        Pattern5<String, String, String, String, String> pattern2 = Pattern.of(SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4, SUCCESS5);
        Pattern5<String, String, String, String, String> pattern3 = Pattern.of(SUCCESS2, SUCCESS1, SUCCESS3, SUCCESS4, SUCCESS5);
        assertEquals(pattern1.hashCode(), pattern2.hashCode());
        assertNotEquals(pattern1.hashCode(), pattern3.hashCode());
    }

    @Test
    public void spread2() throws Throwable {
        get2().then(Pattern.spread2(new OnFulfilled2<String, String>() {
                @Override
            public void fulfilled(String item1, String item2) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
            }
        })).get();
    }

    @Test
    public void spread2WithNullPromise() throws Throwable {
        Promise<Pattern2<String, String>> promise = Promises.fulfilled(null);
        promise.then(Pattern.spread2(new OnFulfilled2<String, String>() {
            @Override
            public void fulfilled(String item1, String item2) throws Throwable {
                assertNull(item1);
                assertNull(item2);
            }
        })).get();
    }

    @Test
    public void spread3() throws Throwable {
        get3().then(Pattern.spread3(new OnFulfilled3<String, String, String>() {
            @Override
            public void fulfilled(String item1, String item2, String item3) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
            }
        })).get();
    }

    @Test
    public void spread3WithNullPromise() throws Throwable {
        Promise<Pattern3<String, String, String>> promise = Promises.fulfilled(null);
        promise.then(Pattern.spread3(new OnFulfilled3<String, String, String>() {
            @Override
            public void fulfilled(String item1, String item2, String item3) throws Throwable {
                assertNull(item1);
                assertNull(item2);
                assertNull(item3);
            }
        })).get();
    }

    @Test
    public void spread4() throws Throwable {
        get4().then(Pattern.spread4(new OnFulfilled4<String, String, String, String>() {
            @Override
            public void fulfilled(String item1, String item2, String item3, String item4) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
            }
        })).get();
    }

    @Test
    public void spread4WithNullPromise() throws Throwable {
        Promise<Pattern4<String, String, String, String>> promise = Promises.fulfilled(null);
        promise.then(Pattern.spread4(new OnFulfilled4<String, String, String, String>() {
            @Override
            public void fulfilled(String item1, String item2, String item3, String item4) throws Throwable {
                assertNull(item1);
                assertNull(item2);
                assertNull(item3);
                assertNull(item4);
            }
        })).get();
    }

    @Test
    public void spread5() throws Throwable {
        get5().then(Pattern.spread5(new OnFulfilled5<String, String, String, String, String>() {
            @Override
            public void fulfilled(String item1, String item2, String item3, String item4, String item5) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                assertEquals(SUCCESS5, item5);
            }
        })).get();
    }

    @Test
    public void spread5WithNullPromise() throws Throwable {
        Promise<Pattern5<String, String, String, String, String>> promise = Promises.fulfilled(null);
        promise.then(Pattern.spread5(new OnFulfilled5<String, String, String, String, String>() {
            @Override
            public void fulfilled(String item1, String item2, String item3, String item4, String item5) throws Throwable {
                assertNull(item1);
                assertNull(item2);
                assertNull(item3);
                assertNull(item4);
                assertNull(item5);
            }
        })).get();
    }

    @Test
    public void apply2() throws Throwable {
        Promise<String> promise = get2().thenApply(Pattern.apply2(new OnFulfilledFunction2<String, String, String>() {
            @Override
            public String fulfilled(String item1, String item2) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                return item1 + item2;
            }
        }));

        assertFulfills(SUCCESS1 + SUCCESS2, promise);
    }

    @Test
    public void apply2WithNullPromise() throws Throwable {
        Promise<Pattern2<String, String>> promise1 = Promises.fulfilled(null);
        Promise<String> promise2 = promise1.thenApply(Pattern.apply2(new OnFulfilledFunction2<String, String, String>() {
            @Override
            public String fulfilled(String item1, String item2) throws Throwable {
                assertNull(item1);
                assertNull(item2);
                return null;
            }
        }));

        assertFulfills(null, promise2);
    }

    @Test
    public void apply3() throws Throwable {
        Promise<String> promise = get3().thenApply(Pattern.apply3(new OnFulfilledFunction3<String, String, String, String>() {
            @Override
            public String fulfilled(String item1, String item2, String item3) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                return item1 + item2 + item3;
            }
        }));

        assertFulfills(SUCCESS1 + SUCCESS2 + SUCCESS3, promise);
    }

    @Test
    public void apply3WithNullPromise() throws Throwable {
        Promise<Pattern3<String, String, String>> promise1 = Promises.fulfilled(null);
        Promise<String> promise2 = promise1.thenApply(Pattern.apply3(new OnFulfilledFunction3<String, String, String, String>() {
            @Override
            public String fulfilled(String item1, String item2, String item3) throws Throwable {
                assertNull(item1);
                assertNull(item2);
                assertNull(item3);
                return null;
            }
        }));

        assertFulfills(null, promise2);
    }

    @Test
    public void apply4() throws Throwable {
        Promise<String> promise = get4().thenApply(Pattern.apply4(new OnFulfilledFunction4<String, String, String, String, String>() {
            @Override
            public String fulfilled(String item1, String item2, String item3, String item4) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                return item1 + item2 + item3 + item4;
            }
        }));

        assertFulfills(SUCCESS1 + SUCCESS2 + SUCCESS3 + SUCCESS4, promise);
    }

    @Test
    public void apply4WithNullPromise() throws Throwable {
        Promise<Pattern4<String, String, String, String>> promise1 = Promises.fulfilled(null);
        Promise<String> promise2 = promise1.thenApply(Pattern.apply4(new OnFulfilledFunction4<String, String, String, String, String>() {
            @Override
            public String fulfilled(String item1, String item2, String item3, String item4) throws Throwable {
                assertNull(item1);
                assertNull(item2);
                assertNull(item3);
                assertNull(item4);
                return null;
            }
        }));

        assertFulfills(null, promise2);
    }

    @Test
    public void apply5() throws Throwable {
        Promise<String> promise = get5().thenApply(Pattern.apply5(new OnFulfilledFunction5<String, String, String, String, String, String>() {
            @Override
            public String fulfilled(String item1, String item2, String item3, String item4, String item5) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                assertEquals(SUCCESS5, item5);
                return item1 + item2 + item3 + item4 + item5;
            }
        }));

        assertFulfills(SUCCESS1 + SUCCESS2 + SUCCESS3 + SUCCESS4 + SUCCESS5, promise);
    }

    @Test
    public void apply5WithNullPromise() throws Throwable {
        Promise<Pattern5<String, String, String, String, String>> promise1 = Promises.fulfilled(null);
        Promise<String> promise2 = promise1.thenApply(Pattern.apply5(new OnFulfilledFunction5<String, String, String, String, String, String>() {
            @Override
            public String fulfilled(String item1, String item2, String item3, String item4, String item5) throws Throwable {
                assertNull(item1);
                assertNull(item2);
                assertNull(item3);
                assertNull(item4);
                assertNull(item5);
                return null;
            }
        }));

        assertFulfills(null, promise2);
    }

    @Test
    public void compose2() throws Throwable {
        Promise<String> promise = get2().thenCompose(Pattern.compose2(new OnFulfilledFunction2<String, String, Future<String>>() {
            @Override
            public Future<String> fulfilled(String item1, String item2) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                return fulfillAfter(item1 + item2, 10);
            }
        }));

        assertFulfills(SUCCESS1 + SUCCESS2, promise);
    }

    @Test
    public void compose3() throws Throwable {
        Promise<String> promise = get3().thenCompose(Pattern.compose3(new OnFulfilledFunction3<String, String, String, Future<String>>() {
            @Override
            public Future<String> fulfilled(String item1, String item2, String item3) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                return fulfillAfter(item1 + item2 + item3, 10);
            }
        }));

        assertFulfills(SUCCESS1 + SUCCESS2 + SUCCESS3, promise);
    }

    @Test
    public void compose4() throws Throwable {
        Promise<String> promise = get4().thenCompose(Pattern.compose4(new OnFulfilledFunction4<String, String, String, String, Future<String>>() {
            @Override
            public Future<String> fulfilled(String item1, String item2, String item3, String item4) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                return fulfillAfter(item1 + item2 + item3 + item4, 10);
            }
        }));

        assertFulfills(SUCCESS1 + SUCCESS2 + SUCCESS3 + SUCCESS4, promise);
    }

    @Test
    public void compose5() throws Throwable {
        Promise<String> promise = get5().thenCompose(Pattern.compose5(new OnFulfilledFunction5<String, String, String, String, String, Future<String>>() {
            @Override
            public Future<String> fulfilled(String item1, String item2, String item3, String item4, String item5) throws Throwable {
                assertEquals(SUCCESS1, item1);
                assertEquals(SUCCESS2, item2);
                assertEquals(SUCCESS3, item3);
                assertEquals(SUCCESS4, item4);
                assertEquals(SUCCESS5, item5);
                return fulfillAfter(item1 + item2 + item3 + item4 + item5, 10);
            }
        }));

        assertFulfills(SUCCESS1 + SUCCESS2 + SUCCESS3 + SUCCESS4 + SUCCESS5, promise);
    }
}
