org.jpromise - Composable Promise Library for Java 6
========

Purpose of this project is to bring a comprehensive asynchronous library to Java loosely based on
JavaScript A+ Promise while blending in conventions from Java.

Very much a work-in-progress.  The API is volatile but the core functionality is pretty stable with a fairly
comprehensive set of unit tests.  I do intent to fully flesh out the javadocs and to populate the Wiki with
documentation and tutorials.

Status:
======

[![Build Status](https://travis-ci.org/HaloFour/jpromise.svg?branch=master)](https://travis-ci.org/HaloFour/jpromise)

Basics:
=======

Creating promises:
------------------

```java

// Creating an already fulfilled promise
Promise<String> promise1 = Promises.fulfilled("Hello World!");

// Creating an already rejected promise
Promise<String> promise2 = Promises.rejected(new Exception("Oops!"));

// Creating a deferred promise that will be fulfilled or rejected later
Deferred<String> deferred = Promises.defer();
Promise<String> promise3 = deferred.promise();
// later
deferred.fulfill("Hello World!");
// or
deferred.reject(new Exception("Dang!"));

// Creating a promise from a Callable using the specified Executor
Executor executor = Executors.newCachedThreadPool();
PromiseService service = new ExecutorPromiseService(executor);
Promise<String> promise4 = service.submit(new Callable<String>() {
    @Override public String call() {
        return "Hello World!";
    }
});

// Creating a promise from an existing Future
Future<String> future = ...;
Promise<String> promise = Promises.fromFuture(future);

// Creating a promise from an rx.Observable<T>
Observable<String> observable = Observable.from(new String[] { "Hello World!" });
Promise<String> promise = new ObservablePromise<String>(observable);

// Creating a promise from a CompletionStage
CompletableFuture<String> future = new CompletableFuture<>();
Promise<String> promise = new CompletablePromise<>(future);
```

Composing promises:
-------------------

Note that every single one of these methods returns a new promise.  That promise is completed when the callback
is finishes.  For `thenCompose` and `withFallback` the returned promise propagates the result from the future or
promise returned in the callback.  In all cases if the callback throws an exception, either checked or unchecked,
the returned promise is rejected with that exception.

```java
Promise<String> promise1 = Promises.fulfilled("Hello World!");

// Calls the callback when the promise fulfills passing the result of the promise.  When the callback
// completes the returned promise will resolve with the same result.  However, if the callback throws
// an exception the returned promise will be rejected with that exception.
Promise<String> promise2 = promise1.then(new OnFulfilled<String>() {
    @Override public void fulfilled(String result) {
        System.out.println(result);
    }
});

// Calls the callback when the promise fulfills passing the result of the promise.  The callback can
// then synchronously transform the result into a new value of any type.  The returned promise will
// be fulfilled with that transformed value when the callback returns.  If the callback throws an
// exception the returned promise will be rejected with that exception.
Promise<String> promise3 = promise2.thenApply(new OnFulfilledFunction<String, String>() {
    @Override public String fulfilled(String result) {
        return new StringBuilder(result).reverse().toString();
    }
});

// Calls the callback when the promise fulfills passing the result of the promise.  The callback
// can then return any Future representing an asynchronous operation, including another Promise.
// The promise returned by this method will be fulfilled or rejected with the same result as that
// returned Future.
Promise<String> promise4 = promise3.thenCompose(new OnFulfilledFunction<String, Future<String>>() {
    @Override public Future<String> fulfilled(String result) {
        return Promises.fulfilled("Goodbye world!");
    }
});

// Calls the callback when the promise is rejected passing the exception.  When the callback completes
// the returned promise will be rejected with the same exception.  If the callback throws an exception the
// returned promise will be rejected with that exception.
Promise<String> promise5 = promise4.whenRejected(new OnRejected<Throwable>() {
    @Override public void rejected(Throwable exception) {
        System.err.println(exception.toString());
    }
});

// Calls the callback when the promise is rejected passing the exception.  The callback can then
// synchronously resolve to a value of the original promise type.  The returned promise will be
// fulfilled with that new value when the callback returns.  If the callback throws an
// exception the returned promise will be rejected with that exception.
Promise<String> promise6 = promise5.withHandler(new OnRejectedHandler<Throwable, String>() {
    @Override public String handle(Throwable exception) {
        return "Oops, this oughta fix it.";
    }
});

// Calls the callback when the promise is rejected passing the exception.  The callback can then
// return a Future of the original promise type representing an asynchronous operation.  The promise
// returned by this method will be fulfilled or rejected with the same result as that returned Future.
Promise<String> promise7 = promise6.withFallback(new OnRejectedHandler<Throwable, Future<String>>() {
    @Override public Future<String> handle(Throwable exception) {
        return Promises.fulfilled("Got a good value from somewhere else.");
    }
});

// Calls the callback when the promise is completed with either a resolution or a rejection.  When
// the callback completes the returned promise will be fulfilled or rejected with the same result.
// If the callback throws an exception the returned promise will be rejected with that exception.
Promise<String> promise8 = promise7.whenCompleted(new OnCompleted<String>() {
    @Override public void completed(Promise<String> promise, String result, Throwable exception) {
        switch (promise.state()) {
            case FULFILLED:
                System.out.printf("The promise was successful: %s%n", result);
                break;
            case REJECTED:
                System.err.printf("Oops, the promise failed: %s%n", exception.getMessage());
                break;
        }
    }
});

// Calls the callback when the promise is completed with either a resolution or a rejection.  The
// callback can then synchronously transform the result into a new value of any type.  The returned
// promise will be fulfilled with that transformed value when the callback returns.  If the
// callback throws an exception the returned promise will be rejected with that exception.
Promise<String> promise9 = promise8.thenApply(new OnCompletedFunction<String, String>() {
    @Override public String completed(Promise<String> promise, String result, Throwable exception) {
        switch (promise.state()) {
            case FULFILLED:
                return new StringBuilder(result).reverse().toString();
            default:
                return exception.getMessage();
        }
    }
});

// Calls the callback when the promise is completed with either a resolution or a rejection.  The
// callback can then return any Future representing an asynchronous operation, including another Promise.
// The promise returned by this method will be fulfilled or rejected with the same result as that
// returned Future.
Promise<String> promise10 = promise9.thenCompose(new OnCompletedFunction<String, Future<String>>() {
    @Override public Future<String> completed(Promise<String> promise, String result, Throwable exception) {
        switch (promise.state()) {
            case FULFILLED:
                return service.submit(new Callable<String>() {
                    @Override public String call() {
                        return String.format("Hello %s!", result);
                    }
                });
            default:
                return Promises.fulfilled("Goodbye world!");
        }
    }
});
```

The rejected promise methods each have overloads that accept an argument of `Class<? extends Throwable>` which
specify that the callback is typed to a specific exception type or any of its subclasses.

```java
Promise<String> promise11 = promise10.whenRejected(IllegalArgumentException.class, new OnRejected<IllegalArgumentException>() {
    @Override public void rejected(IllegalArgumentException exception) {
        // This callback will only be called if the exception is an instance of IllegalArgumentException
    }
});
```

All of the composable methods also accept an `Executor` instance on which the callback can be executed.  If an
executor is not specified the callbacks will be executed asynchronously on a common ForkJoinPool.

```java
ForkJoinPool mainPool = ...;

Promise<String> promise12 = promise11.then(mainPool, new OnResolved<String>() {
    @Override public void fulfilled(String result) {
        // executed asynchronously on the specified ForkJoinPool
    }
});

Promise<String> promise13 = promise12.then(PromiseExecutors.CURRENT_THREAD, new OnResolved<String>() {
    @Override public void fulfilled(String result) {
        // executed synchronously on the thread that fulfilled the promise
    }
});
```

All of the composable methods are written specifically so that they play well with Java 8 lambdas, eliminating
overload ambiguity.  The following are identical to the examples above.

```java
Promise<String> promise1 = Promises.fulfilled("Hello World!");

Promise<String> promise2 = promise1.then(result -> { System.out.println(result); });

Promise<String> promise3 = promise2.thenApply(result -> new StringBuilder(result).reverse().toString());

Promise<String> promise4 = promise3.thenCompose(result -> Promises.fulfilled("Goodbye world!"));

Promise<String> promise5 = promise4.whenRejected(exception -> { System.err.println(exception.toString()); });

Promise<String> promise6 = promise5.withHandler(exception -> "Oops, this oughta fix it.");

Promise<String> promise7 = promise6.withFallback(exception -> Promises.fulfilled("Got a good value from somewhere else."));

Promise<String> promise8 = promise7.whenCompleted((p, result, exception) -> {
    switch (promise.state()) {
        case FULFILLED:
            System.out.printf("The promise was successful: %s%n", result);
            break;
        case REJECTED:
            System.err.printf("Oops, the promise failed: %s%n", exception.getMessage());
            break;
    }
});

Promise<String> promise9 = promise7.thenApply((p, result, exception) -> {
    switch (promise.state()) {
        case FULFILLED:
            return new StringBuilder(result).reverse().toString();
        default:
            return exception.getMessage();
    }
});

Promise<String> promise10 = promise9.thenCompose((p, result, exception) -> {
    switch (promise.state()) {
        case FULFILLED:
            return service.submit(() -> String.format("Hello %s!", result));
        default:
            return Promises.fulfilled("Goodbye world!");
    }
});
```

Joining Promises:
-----------------

There are several methods available to join the results of multiple promises into a single promise.  The simplest
is to join two-to-five separate heterogeneous promises into a pattern which behaves like an immutable tuple.

```java
Promise<String> promise1 = ...;
Promise<Integer> promise2 = ...;

// create a single promise that will resolve when all of the specified promises have fulfilled
Promise<Pattern2<String, Integer>> joined = Pattern.join(promise1, promise2);

// helper methods on the Pattern class can deconstruct the result into the individual arguments
joined.then(Pattern.spread2(new OnFulfilled2<String, Integer>() {
    @Override public void fulfilled(String result1, Integer result2) {
        System.out.printf("Both promises completed successfully: %s, %d%n", result1, result2);
    }
}));
```

You can also asynchronously wait on the completion or resolution of any number of promises.

```java
List<Promise<?>> promises = ...;

// Returns a promise that will resolve when all of the specified promises have either been fulfilled or rejected
Promise<Void> completed = PromiseManager.whenAllCompleted(promises);

// Returns a promise that will resolve when all of the specified promises have been fulfilled.  If any of the
// promises is rejected then the returned promise will propagate that rejection.
Promise<Void> fulfilled = PromiseManager.whenAllFulfilled(promises);
```

There are methods to race the completion of one or more promises.

```java
List<Promise<String>> promises = ...;

// Returns a promise that propagates the result or rejection of the first promise that completes.
Promise<String> completed = PromiseManager.whenAnyCompleted(promises);

// Returns a promise that propagates the result of the first promise that resolves.
Promise<String> fulfilled = PromiseManager.whenAnyFulfilled(promises);
```

The `PromiseStream` class provides basic query and collection functionality over any number of homogeneous promises.

```java
List<Promise<String>> promises = ...;

PromiseStream<String> stream = PromiseStreams.from(promises);

Promise<Integer[]> promise = stream
    // transform the results of the individual promises
    .map(new OnFulfilledFunction<String, Integer>() {
        @Override public Integer fulfilled(String result) {
            return Integer.valueOf(result, 10);
        }
    })
    // filter out any rejected promises with the specified exception class
    .filterRejected(NumberFormatException.class)
    // collect the results into an array via an ArrayList with the specified initial capacity
    .toArray(Integer.class, 10);
    
promise.then(new OnFulfilled<Integer[]>() {
    @Override public void fulfilled(Integer[] result) {
        // use the final results here
    }
});

// or, in Java 8

PromiseStreams.from(promises)
    .map(result -> Integer.valueOf(result, 10))
    .filterRejected(NumberFormatException.class)
    .toArray(Integer.class, 10)
    .then(array -> { ... });
```