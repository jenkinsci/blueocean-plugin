package io.jenkins.blueocean.rest.model;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import io.jenkins.blueocean.rest.model.Containers.CombinedIterable;

public class ContainerTests {
    @Test
    public void testCombinedIterable() {
        Iterable<String> i1 = Arrays.asList("One", "Two", "Three");
        Iterable<Integer> i2 = Arrays.asList(4, 5, 6);
        Iterable<Long> i3 = Arrays.asList(7l, 8l, 9l);
        
        CombinedIterable<Object> container = new CombinedIterable<Object>(i1, i2, i3);
        
        // make sure no exception iterating the whole thing and 9 items returned
        int total = 0;
        Iterator<?> i = container.iterator();
        Object last = null;
        while (i.hasNext()) {
            last = i.next();
            total++;
        }
        
        assert(total == 9) : "count doesn't match";
        assert(Long.valueOf(9).equals(last)) : "wrong last element";
        
        total = 0;
        i = container.iterator(1, -1);
        last = null;
        while (i.hasNext()) {
            last = i.next();
            total++;
        }
        
        assert(total == 8) : "count doesn't match";
        assert(Long.valueOf(9).equals(last)) : "wrong last element";
        
        total = 0;
        i = container.iterator(1, 3);
        last = null;
        while (i.hasNext()) {
            last = i.next();
            total++;
        }
        
        assert(total == 3) : "count doesn't match";
        assert(Integer.valueOf(4).equals(last)) : "wrong last element";
        
        
        total = 0;
        i = container.iterator(0, 3);
        last = null;
        while (i.hasNext()) {
            last = i.next();
            total++;
        }
        
        assert(total == 3) : "count doesn't match";
        assert("Three".equals(last)) : "wrong last element";
    }
}
