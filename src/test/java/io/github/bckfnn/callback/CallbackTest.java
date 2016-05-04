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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class CallbackTest {
    List<Object> events = new ArrayList<>();

    @Test
    public void runOk1() {
        events.add("start");

        runOk((v, e) -> {
            events.add("continue");
            events.add(v);
        });

        Assert.assertEquals(Arrays.asList("start", "continue", "ok"), events);
    }

    @Test
    public void runFail() {
        events.add("start");
        runFail((t, e) -> {
            events.add("continue");
            events.add(e.getMessage());
        });
        Assert.assertEquals(Arrays.asList("start", "continue", "error"), events);
    }


    @Test
    public void runOk2() {
        events.add("start");
        ok2((v, e) -> {
            events.add("end");
            events.add(v);
        });

        Assert.assertEquals(Arrays.asList("start", "continue", "ok", "end", 2l), events);
    }

    private void ok2(Callback<Long> cb) {
        runOk(cb.call(t -> {
            events.add("continue");
            events.add(t);
            cb.ok(2l);
        }));
    }

    @Test
    public void runFail2() {
        events.add("start");
        fail2((v, e) -> {
            events.add("end");
            events.add(e.getMessage());
        });

        Assert.assertEquals(Arrays.asList("start", "end", "error"), events);
    }

    private void fail2(Callback<Long> cb) {
        runFail(cb.call(t -> {
            events.add("continue");
            events.add(t);
        }));
    }

    public void runOk(Callback<String> cb) {
        cb.ok("ok");
    }

    public void runFail(Callback<Void> cb) {
        cb.fail(new Error("error"));
    }


    @Test
    public void forEachList1() {
        List<String> data = Arrays.asList("a", "b", "c", "d");
        events.add("start");

        subForEachList1(data, (v, e) -> {
            events.add("done");
        });
        Assert.assertEquals(Arrays.asList("start", "a", "b", "c", "d", "done"), events);
    }

    private void subForEachList1(List<String> data, Callback<Void> cb) {
        cb.forEach(data, (elm, h) -> {
            events.add(elm);
            h.ok();
        });
    }

    @Test
    public void forEachList2() {
        List<String> data = Arrays.asList("a", "b", "c", "d");
        events.add("start");

        subForEachList2(data, (v, e) -> {
            events.add("done");
        });
        Assert.assertEquals(Arrays.asList("start", "a", "b", "c", "d", "end", "done"), events);
    }

    private void subForEachList2(List<String> data, Callback<Void> cb) {
        cb.forEach(data, (elm, h) -> {
            events.add(elm);
            h.ok();
        }, $ -> {
            events.add("end");
            cb.ok();
        });
    }
}
