package io.github.bckfnn.callback;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.vertx.core.streams.ReadStream;

public interface CallbackReadStream<T> {
    public void call(ReadStream<T> resultStream);
    
    default <R> void call(Consumer<ElementReadStream<T>> handler) {
        ElementReadStream<T> rs = new ElementReadStream<>();
        handler.accept(rs);
        call(rs);
    }
    
    default <R> CallbackReadStream<R> each(BiConsumer<Consumer<T>, R> handler) {
        ElementReadStream<T> rs = new ElementReadStream<>();
        call(rs);
        return r -> {
            r.endHandler($ -> {
                System.out.println("rs.end()");
                rs.end();
            });
            r.exceptionHandler(e -> rs.fail(e));
            r.handler(val -> {
                System.out.println("rs:" + val);
                r.pause();
                handler.accept(t -> { 
                    rs.send(t);
                    r.resume();
                }, val);
            });
        };
    }
}