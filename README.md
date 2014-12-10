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

// Creating a promise from an rx.Observable<T>
Observable<String> observable = Observable.from(new String[] { "Hello World!" });
Promise<String> promise = new ObservablePromise(observable);

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
Promise<String> promise1 = Promise.resolved("Hello World!");

Promise<String> promise2 = promise1.then(new OnResolved<String>() {
    @Override public void resolved(String result) {
        System.out.println(result);
    }
});

Promise<String> promise3 = promise2.thenApply(new OnResolvedFunction<String, String>() {
    @Override public String resolved(String result) {
        return new StringBuilder(result).reverse().toString();
    }
});

Promise<String> promise4 = promise3.thenCompose(new OnResolvedFunction<String, Future<String>>() {
    @Override public Future<String> resolved(String result) {
        return Promise.resolved("Goodbye world!");
    }
});

Promise<String> promise5 = promise4.whenRejected(new OnRejected<Throwable>() {
    @Override public void rejected(Throwable exception) {
        System.err.println(exception.toString());
    }
});

Promise<String> promise6 = promise5.withHandler(new OnRejectedHandler<Throwable, String>() {
    @Override public String handle(Throwable exception) {
        return "Oops, this oughta fix it.";
    }
});

Promise<String> promise7 = promise6.withFallback(new OnRejectedHandler<Throwable, Future<String>>() {
    @Override public Future<String> handle(Throwable exception) {
        return Promise.resolved("Got a good value from somewhere else.");
    }
});

Promise<String> promise8 = promise7.whenCompleted(new OnCompleted<String>() {
    @Override public void completed(Promise<String> promise, String result, Throwable exception) {
        switch (promise.state()) {
            case RESOLVED:
                System.out.printf("The promise was successful: %s%n", result);
                break;
            case REJECTED:
                System.err.printf("Oops, the promise failed: %s%n", exception.getMessage());
                break;
        }
    }
});
```

The rejected promise methods each have overloads that accept an argument of `Class<? extends Throwable>` which
specify that the callback is typed to a specific exception type or any of its subclasses.

```java
Promise<String> promise9 = promise8.whenRejected(IllegalArgumentException.class, new OnRejected<IllegalArgumentException>() {
    @Override public void rejected(IllegalArgumentException exception) {
        // This callback will only be called if the exception is an instance of IllegalArgumentException
    }
});
```

All of the composable methods also accept an `Executor` instance on which the callback can be executed.  If an
executor is not specified the callbacks will be executed asynchronously on a common ForkJoinPool.

```java
ForkJoinPool mainPool = ...;

Promise<String> promise10 = promise9.then(mainPool, new OnResolved<String>() {
    @Override public void resolved(String result) {
        // executed asynchronously on the specified ForkJoinPool
    }
});

Promise<String> promise11 = promise10.then(PromiseExecutors.CURRENT_THREAD, new OnResolved<String>() {
    @Override public void resolved(String result) {
        // executed synchronously on the thread that resolved the promise
    }
});
```

All of the composable methods are written specifically so that they play well with Java 8 lambdas, eliminating
overload ambiguity.  The following are identical to the examples above.

```java
Promise<String> promise1 = Promise.resolved("Hello World!");

Promise<String> promise2 = promise1.then(result -> { System.out.println(result); });

Promise<String> promise3 = promise2.thenApply(result -> new StringBuilder(result).reverse().toString());

Promise<String> promise4 = promise3.thenCompose(result -> Promise.resolved("Goodbye world!"));

Promise<String> promise5 = promise4.whenRejected(exception -> { System.err.println(exception.toString()); });

Promise<String> promise6 = promise5.withHandler(exception -> "Oops, this oughta fix it.");

Promise<String> promise7 = promise6.withFallback(exception -> Promise.resolved("Got a good value from somewhere else."));

Promise<String> promise8 = promise7.whenCompleted((p, result, exception) -> {
    switch (promise.state()) {
        case RESOLVED:
            System.out.printf("The promise was successful: %s%n", result);
            break;
        case REJECTED:
            System.err.printf("Oops, the promise failed: %s%n", exception.getMessage());
            break;
    }
});
```

There's much more, including methods to wait on the completion of multiple promises, methods to race promises,
methods to join promise results into patterns and then deconstruct their results and methods to convert to/from
Rx Observables.