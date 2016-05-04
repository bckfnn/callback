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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface Callback<T> {
    public void call(T result, Throwable error);

    default <R> Callback<R> call(Consumer<R> handler) {
        return (t, e) -> {
            if (e != null) {
                call(null, e);
            } else {
                handler.accept(t);
            }
        };
    }

    default <R> Handler<AsyncResult<R>> handler(Consumer<R> handler) {
        return ar -> {
            if (ar.failed()) {
                call(null, ar.cause());
            } else {
                handler.accept(ar.result());
            }
        };
    }

    default void ok(T value) {
        call(value, null);
    }

    default void ok() {
        call(null, null);
    }


    default void fail(Throwable error) {
        call(null, error);
    }


    default <E> void forEach(List<E> list, BiConsumer<E, Callback<Void>> x) {
        forEach(list, x, this::ok);
    }

    default <E> void forEach(List<E> list, BiConsumer<E, Callback<Void>> x, Consumer<T> done) {
        AtomicInteger cnt = new AtomicInteger(0);
        Callback<Void> h = new Callback<Void>() {
            @Override
            public void call(Void $, Throwable e) {
                if (e != null) {
                    System.out.println("forEach stopped");
                    fail(e);
                    return;
                }
                int i = cnt.getAndIncrement();
                if (i >= list.size()) {
                    //log.trace("forEach.done {} items", list.size());
                    done.accept(null);
                } else {
                    x.accept(list.get(i), this);
                }
            }
        };
        h.call(null, null);
    }

}