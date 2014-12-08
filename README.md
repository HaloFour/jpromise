org.jpromise - Composable Promise Library for Java 7
========

Purpose of this project is to bring a comprehensive asynchronous library to Java loosely based on
JavaScript A+ Promise while blending in conventions from Java.

Very much a work-in-progress.  The API is volatile but the core functionality is pretty stable with a fairly
comprehensive set of unit tests.  I do intent to fully flesh out the javadocs and to populate the Wiki with
documentation and tutorials.

Basics:
=======

Creating promises:
------------------

```java

// Creating an already resolved promise
Promise<String> promise1 = Promise.resolved("Hello World!");

// Creating an already rejected promise
Promise<String> promise2 = Promise.rejected(new Exception("Oops!");

// Creating a deferred promise that will be resolved or rejected later
Deferred<String> deferred = Promise.defer();
Promise<String> promise3 = deferred.promise();
// later
deferred.resolve("Hello World!");

// Creating a promise from a Callable
ForkJoinPool mainPool = ...;
Promise<String> promise4 = PromiseManager.create(mainPool, new Callable<String>() {
    @Override public String call() {
        return "Hello World!";
    }
});

// Creating a promise from an existing Future
Future<String> future = ...;
Promise<String> promise = PromiseManager.fromFuture(future);
```

Composing promises:
-------------------

Note that every single one of these methods returns a new promise.  That promise is completed when the callback
is finishes.  For `thenCompose` and `withFallback` the returned promise propagates the result from the future or
promise returned in the callback.  In all cases if the callback throws an exception, either checked or unchecked,
the returned promise is rejected with that exception.

```java
Promise<String> promise1 = Promise.resolved("Hello World!");

Promise<String> promise2 = promise1.then(new OnResolved<String>() {
    @Override public void resolved(String result) {
        System.out.println(result);
    }
});

Promise<String> promise3 = promise.thenApply(new OnResolvedFunction<String, String>() {
    @Override public String resolved(String result) {
        return new StringBuilder(result).reverse().toString();
    }
});

Promise<String> promise4 = promise.thenCompose(new OnResolvedFunction<String, Future<String>>() {
    @Override public Future<String> resolved(String result) {
        return Promise.resolved("Goodbye world!");
    }
});

Promise<String> promise5 = promise.rejected(new OnRejected<Throwable>() {
    @Override public void rejected(Throwable exception) {
        System.err.println(exception.toString());
    }
});

Promise<String> promise6 = promise.withHandler(new OnRejectedHandler<Throwable, String>() {
    @Override public String handle(Throwable exception) {
        return "Oops, this oughta fix it.";
    }
});

Promise<String> promise7 = promise.withFallback(new OnRejectedHandler<Throwable, Future<String>>() {
    @Override public Future<String> handle(Throwable exception) {
        return Promise.resolved("Got a good value from somewhere else.");
    }
});
```

The rejected promise methods each have overloads that accept an argument of `Class<? extends Throwable>` which
specify that the callback is typed to a specific exception type or any of its subclasses.

```java
Promise<String> promise8 = promise.rejected(IllegalArgumentException.class, new OnRejected<IllegalArgumentException>() {
    @Override public void rejected(IllegalArgumentException exception) {
        // This callback will only be called if the exception is an instance of IllegalArgumentException
    }
});
```

All of the composable methods also accept an `Executor` instance on which the callback can be executed.  Currently
the callbacks are executed by the current thread, meaning that if the promise is already completed the callback
is invoked immediately and the composing method will not return until the callback returns.

```java
ForkJoinPool mainPool = ...;

Promise<String> promise9 = promise.then(mainPool, new OnResolved<String>() {
    @Override public void resolved(String result) {
        // executed on the ForkJoinPool
    }
});

```

All of the composable methods are written specifically so that they play well with Java 8 lambdas, eliminating
overload ambiguity.  The following are identical to the examples above.

```java
Promise<String> promise1 = Promise.resolved("Hello World!");

Promise<String> promise2 = promise1.then(result -> { System.out.println(result); });

Promise<String> promise3 = promise.thenApply(result -> new StringBuilder(result).reverse().toString());

Promise<String> promise4 = promise.thenCompose(result -> Promise.resolved("Goodbye world!"));

Promise<String> promise5 = promise.rejected(exception -> { System.err.println(exception.toString()); });

Promise<String> promise6 = promise.withHandler(exception -> "Oops, this oughta fix it.");

Promise<String> promise7 = promise.withFallback(exception -> Promise.resolved("Got a good value from somewhere else."));
```

There's much more, including methods to wait on the completion of multiple promises, methods to race promises,
methods to join promise results into patterns and then deconstruct their results and methods to convert to/from
Rx Observables.