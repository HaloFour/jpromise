package org.jpromise;

import org.jpromise.functions.OnResolvedFunction;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Future;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;

public class PromiseStreamTest {
    public static final String SUCCESS1 = "SUCCESS1";
    public static final String SUCCESS2 = "SUCCESS2";
    public static final String SUCCESS3 = "SUCCESS3";
    public static final String SUCCESS4 = "SUCCESS";
    public static final String SUCCESS5 = "SUCCES";

    @Test
    public void test() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 10);
        Promise<String> promise5 = resolveAfter(SUCCESS5, 10);

        Promise<Map<Integer, String[]>> promise = PromiseStream.from(promise1, promise2, promise3, promise4, promise5)
                .collect(PromiseCollectors.groupingBy(Integer.class, new OnResolvedFunction<String, Integer>() {
                            @Override
                            public Integer resolved(String result) throws Throwable {
                                return result.length();
                            }
                        }, PromiseCollectors.toArray(String.class)));


        Map<Integer, String[]> grouped = assertResolves(promise);

        assertEquals(3, grouped.size());

    }
}
