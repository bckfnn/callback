package io.github.bckfnn.callback;

import java.util.LinkedList;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

public class ElementReadStream<T> implements ReadStream<T> {
    private LinkedList<T> elements = new LinkedList<T>();
    private Handler<Throwable> exceptionHandler;
    private Handler<T> dataHandler;
    private Handler<Void> endHandler;
    private boolean pause = true;
    private boolean endPending = false;

    @Override
    public ReadStream<T> exceptionHandler(Handler<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public ReadStream<T> handler(Handler<T> dataHandler) {
        this.dataHandler = dataHandler;
        resume();
        return this;
    }

    @Override
    public ReadStream<T> endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    @Override
    public ReadStream<T> pause() {
        pause = true;
        emit();
        return this;
    }

    @Override
    public ReadStream<T> resume() {
        pause = false;
        emit();
        return this;
    }

    private void emit() {
        while (elements.size() > 0 && !pause) {
            dataHandler.handle(elements.removeFirst());
        }
        if (!pause && elements.size() == 0 & endPending) {
            endHandler.handle(null);
        }
    }


    public boolean isPaused() {
        return pause;
    }

    public void send(T element) {
        elements.add(element);
        emit();
    }

    public void end() {
        if (elements.size() == 0 && !pause && endHandler != null) {
            endHandler.handle(null);
        } else {
            endPending = true;
        }
    }

    public void fail(Throwable t) {
        exceptionHandler.handle(t);
        endPending = false;
        pause = true;
        elements.clear();
    }
}