/*
 * Copyright 2016 Finn Bock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bckfnn.callback;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

/**
 * A funtional interface that can signal a success value or a failure condition.
 *
 * @param <T> type of the success value.
 */
public interface Callback<T> {
    /**
     * Called when the callback occur.
     * @param result success value, when error is null
     * @param error when not null, this is the error condition.
     */
    public void call(T result, Throwable error);

    public static <R> Callback<R> callback(Handler<AsyncResult<R>> handler) {
        return (result, error) -> {
            if (error != null) {
                handler.handle(Future.failedFuture(error));
            } else {
                handler.handle(Future.succeededFuture(result));
            }
        };
    }
    /**
     * Return a new callback where the handler is invoked on success and a error condition is passed to this callback.
     * @param handler the handler to call on success.
     * @param <R> type of the returned success value.
     * @return a new callback.
     * <h2>Example</h2>
     * <pre>
     *     public void sub(Callback&lt;Long&gt; cb) {
     *         subsub(cb.call(value -&gt; {
     *             cb.ok(value + 1);
     *         }
     *     }
     *
     *     public void main() {
     *         sub((v, e) -&gt; {
     *             System.out.println(v + " " + e);
     *         }
     *     }
     *
     *     public void subsub(Callback&lt;Long&gt; cb) {
     *         cb.ok(42);
     *     }
     * </pre>
     */
    default <R> Callback<R> call(Consumer<R> handler) {
        return (t, e) -> {
            if (e != null) {
                call(null, e);
            } else {
                try {
                    handler.accept(t);
                } catch (Throwable exc) {
                    call(null, exc);
                }
            }
        };
    }

    /**
     * Return a new {@code Handler<AsyncResult<R>>} where the handler argument is invoked on success and a error condition is passed to this callback.
     * @param handler the handler to call on success.
     * @param <R> type of the returned success value.
     * @return a new vertx {@code Handler<AsyncResult<R>>}.
     *
     * <h2>Example</h2>
     * <pre>
     *     public void main() {
     *         sub((v, e) -&gt; {
     *             System.out.println(v + " " + e);
     *         }
     *     }
     *
     *     public void sub(Callback&lt;Void&gt; cb) {
     *         vertx.fileSystem().open("filename.txt", new OpenOptions(), cb.handler(asyncfile -&gt; {
     *             asyncfile.close(cb.handler($ -&gt; {
     *                 cb.ok());
     *             }
     *         }
     *     }
     * </pre>
     */
    default <R> Handler<AsyncResult<R>> handler(Consumer<R> handler) {
        return ar -> {
            if (ar.failed()) {
                call(null, ar.cause());
            } else {
                try {
                    handler.accept(ar.result());
                } catch (Throwable exc) {
                    call(null, exc);
                }
            }
        };
    }

    /**
     * finish the callback with an success value.
     * @param value the value to send back.
     */
    default void ok(T value) {
        call(value, null);
    }

    /**
     * Finish the callback with a null success value.
     */
    default void ok() {
        call(null, null);
    }

    /**
     * Finish the callback with a failure condition.
     * @param error the error to send back.
     */
    default void fail(Throwable error) {
        call(null, error);
    }

    /**
     * Iterate sequentially over the elements in the list. For each element call the elmHandler.
     * @param <E> type of elements in the list
     * @param list the list of data
     * @param elmHandler the elmHandler to call for each element.
     *
     * The elmHandler take two arguments, the actual element and a Callback that must be used to signal success or failure
     * of handling the element. Only when the callback's ok() method is called will the next element be iterated.
     *
     * Calling fail() of the callback will stop the iteration.
     */
    default <E> void forEach(List<E> list, BiConsumer<E, Callback<Void>> elmHandler) {
        forEach(list, elmHandler, this::ok);
    }

    /**
     * Iterate sequentially over the elements in the list. For each element call the elmHandler.
     * @param <E> type of elements in the list
     * @param list the list of data
     * @param elmHandler the elmHandler to call for each element.
     * @param done the done handler that is invoked when all elements have been iterated successfully.
     *
     * The elmHandler take two arguments, the actual element and a Callback that must be used to signal success or failure
     * of handling the element. Only when the callback's ok() method is called will the next element be iterated.
     *
     * Calling fail() of the callback will stop the iteration and the done handler will not be called.
     */
    default <E> void forEach(List<E> list, BiConsumer<E, Callback<Void>> elmHandler, Consumer<T> done) {
        Callback<T> thiz = this;

        Callback<Void> h = new Callback<Void>() {
            AtomicInteger cnt = new AtomicInteger(0);
            AtomicInteger completed = new AtomicInteger(0);
            int depth;

            @Override
            public void call(Void $, Throwable e) {
                depth++;
                //System.out.println("forEach call:" + cnt + " " + completed + " " + depth);
                if (e != null) {
                    //System.out.println("forEach stopped");
                    thiz.fail(e);
                    return;
                }
                if (depth == 1) {
                    while (true) {
                        //System.out.println("forEach loop:" + cnt + " " + completed);
                        int i = cnt.get();
                        if (i >= list.size()) {
                            if (completed != null && i == completed.get()) {
                                completed = null;
                                done.accept(null);
                            }
                            //log.trace("forEach.done {} items", list.size());
                            break;
                        }

                        cnt.incrementAndGet();
                        //System.out.println("forEach accept");
                        elmHandler.accept(list.get(i), this);
                        if (completed == null || cnt.get() > completed.get()) {
                            break;
                        }

                    }
                }
                //System.out.println("forEach exit:" + cnt + " " + completed);
                depth--;
            }

            @Override
            public void ok(Void t) {
                completed.incrementAndGet();
                Callback.super.ok(t);
            }

            @Override
            public void ok() {
                completed.incrementAndGet();
                Callback.super.ok();
            }

        };
        h.call(null, null);
    }
    
    default <E> void forEach(ReadStream<E> readStream, BiConsumer<E, Callback<Void>> elmHandler, Consumer<T> done) {
        readStream.exceptionHandler(error -> {
            fail(error);
        });
        readStream.endHandler($ -> {
            done.accept(null);
        });
        readStream.handler(elm -> {
            readStream.pause();
            elmHandler.accept(elm, ((v, e) -> {
                if (e != null) {
                    fail(e);
                    return;
                }
                readStream.resume();
            }));
        });
    }
    
    default <E> CallbackReadStream<E> one(Consumer<E> consumer) {
        AtomicReference<E> value = new AtomicReference<>();
        return rs -> {
            rs.endHandler($ -> {
                if (value.get() != null) {
                    consumer.accept(value.get());
                } else {
                    fail(new RuntimeException("value missing"));
                }
            });
            rs.exceptionHandler(err -> {
                fail(err);
            });
            rs.handler(val -> {
                if (value.get() == null) {
                    value.set(val);
                } else {
                    rs.pause();
                    fail(new RuntimeException("multiple values"));
                }
            });
        };
    }
}