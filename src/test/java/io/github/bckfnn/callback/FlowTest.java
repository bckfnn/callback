package io.github.bckfnn.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.Future;

public class FlowTest {

    @Test
    public void testForeach() {
        List<String> lst = Arrays.asList("a", "b", "c");
        List<String> result = new ArrayList<>();
        
        Flow.forEach(lst, (i, h) -> {
            result.add(i);
            h.handle(Future.succeededFuture());
        }, res -> {
            result.add("end");
            
            Assert.assertEquals(Arrays.asList("a", "b", "c", "end"), result);
        });
    }
}
